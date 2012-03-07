package com.alibaba.qa.diffcoverage.parser;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.linuxtools.gcov.parser.SourceFile;

import com.alibaba.qa.diffcoverage.model.CompilationUnit;
import com.alibaba.qa.diffcoverage.model.FileProperty;

/**
 * 对覆盖率文件进行解析
 * <p>
 * 在解析前请先确保每一个gcda文件所在的路径下，有一个同名的gcno文件，否则会解析失败
 * </p>
 * @author garcia.wul@alibaba-inc.com
 *
 */
public interface ICoverageFileParser {
    /**
     * 解析所有的gcda文件
     * @param gcdaFiles
     * @param basePath 代码的根目录
     * @return
     */
    public FileProperty parse(String basePath, CompilationUnit compilationUnits);
    
    public Map<String, SourceFile> getHeaderFiles();
    
    public FileProperty parseHeader(Entry<String, SourceFile> headerFile);
    
    public FileProperty parseZeroFile(String sourceFile);

}
