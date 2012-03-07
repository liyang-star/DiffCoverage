package com.alibaba.qa.astanalyzer.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;

import com.ibm.icu.text.CharsetDetector;

/**
 * 用于解析一段C/C++代码，然后得到IASTTranslationUnit对象
 * @author wul
 *
 */
public class ASTTranslationUnitCore {
    public final static String TEST_CODE = "<testcode>";
    protected static final IParserLogService NULL_LOG = new NullLogService();

    /**
     * 创建一个IScanner实例
     * @param codeReader
     * @param language C or C++
     * @param parserMode 解析类型
     * @param scannerInfo
     * @return
     */
    private IScanner createScanner(
        FileContent codeReader,
        ParserLanguage language,
        ParserMode parserMode,
        IScannerInfo scannerInfo
        ) {
        IScannerExtensionConfiguration configuration =
            language == ParserLanguage.C ?
            GCCScannerExtensionConfiguration.getInstance() :
            GPPScannerExtensionConfiguration.getInstance();
        IScanner scanner = new CPreprocessor(codeReader, scannerInfo, language,
            NULL_LOG, configuration,
            null);
        return scanner;
    }

    /**
     * 轻便的对code进行解析
     * @param code
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static IASTTranslationUnit parse(byte[] code) {
        return parse(code, ParserLanguage.CPP, false, false);
    }

    public static IASTTranslationUnit parse(File file) {
        return parse(file, ParserLanguage.CPP, false, false);
    }

    public static IASTTranslationUnit parse(byte[] code, File file) {
        return parse(code, file, ParserLanguage.CPP, false, false);
    }

    /**
     * 对一个文件进行解析，得到IASTTranslationUnit对象
     * @param file
     * @param language
     * @param useGNUExtensions
     * @param skipTrivialInitializers
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static IASTTranslationUnit parse(File file,
        ParserLanguage language,
        boolean useGNUExtensions,
        boolean skipTrivialInitializers
        ) {
        return ASTTranslationUnitCore.parse(null, file, language,
            useGNUExtensions, skipTrivialInitializers);
    }

    /**
     * 解析一段C/C++代码，得到IASTTranslationUnit对象
     * @param code 需要分析的源代码
     * @param language C ? C++
     * @param useGNUExtensions 使用GNU拓展
     * @param skipTrivialInitializers 忽略不重要的初始化
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static IASTTranslationUnit parse(byte[] code,
        ParserLanguage language,
        boolean useGNUExtensions,
        boolean skipTrivialInitializers
        ) {
        return ASTTranslationUnitCore.parse(code, null, language,
            useGNUExtensions,
            skipTrivialInitializers);
    }

    /**
     * 根据code和file来解析得到对应的IASTTranslationUnit对象
     * @param code C/C++代码
     * @param file C/C++文件
     * @param language C/C++
     * @param useGNUExtensions 是否使用GNU拓展
     * @param skipTrivialInitializers
     * @return IASTTranslationUnit对象
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static IASTTranslationUnit parse(byte[] code,
        File file,
        ParserLanguage language,
        boolean useGNUExtensions,
        boolean skipTrivialInitializers
        ) {
        ASTTranslationUnitCore astTranslationUnitCore = new ASTTranslationUnitCore();
        IScanner scanner = null;

        try {
            if (file != null)
                scanner = astTranslationUnitCore.createScanner(
                    FileContent.create(file.toString(),
                    new String(FileUtils.readFileToByteArray(file), getFileCharset(file)).toCharArray()),
                    language,
                    ParserMode.COMPLETE_PARSE,
                    new ScannerInfo());
            else
                scanner = astTranslationUnitCore.createScanner(
                    FileContent.create(TEST_CODE,
                            new String(code, getCharset(code)).toCharArray()),
                            language,
                            ParserMode.COMPLETE_PARSE,
                            new ScannerInfo());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        AbstractGNUSourceCodeParser sourceCodeParser = null;
        if (language == ParserLanguage.CPP) {
            ICPPParserExtensionConfiguration config = useGNUExtensions ?
                new GPPParserExtensionConfiguration() :
                new ANSICPPParserExtensionConfiguration();
            sourceCodeParser = new GNUCPPSourceParser(scanner,
                ParserMode.COMPLETE_PARSE, NULL_LOG, config, null);
        }
        else {
            ICParserExtensionConfiguration config = useGNUExtensions ?
                new GCCParserExtensionConfiguration() :
                new ANSICParserExtensionConfiguration();
            sourceCodeParser = new GNUCSourceParser(scanner,
                ParserMode.COMPLETE_PARSE, NULL_LOG, config, null);
        }

        if (skipTrivialInitializers)
            sourceCodeParser.setSkipTrivialExpressionsInAggregateInitializers(true);

        return sourceCodeParser.parse();
    }

    static private String getFileCharset(File file) throws IOException {
        String charset = getCharset(FileUtils.readFileToByteArray(file));
        if (charset.startsWith("IBM"))
            return "utf-8";
        else
            return charset;
    }

    static private String getCharset(byte[] bytes) {
        CharsetDetector charsetDetector = new CharsetDetector();
        charsetDetector.setText(bytes);
        String charset = charsetDetector.detect().getName();
        return charset;
    }

}
