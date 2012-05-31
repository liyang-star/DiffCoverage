package com.alibaba.qa.diffcoverage.parser;

import static com.google.common.collect.Collections2.filter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.linuxtools.gcov.parser.CovManager;
import org.eclipse.linuxtools.gcov.parser.Line;
import org.eclipse.linuxtools.gcov.parser.SourceFile;
import org.openide.filesystems.FileUtil;

import com.alibaba.qa.astanalyzer.core.ASTStatementsVisitor;
import com.alibaba.qa.astanalyzer.core.ASTTranslationUnitCore;
import com.alibaba.qa.astanalyzer.core.FunctionDefinitionVisitor;
import com.alibaba.qa.diffcoverage.model.ASTFileLocation;
import com.alibaba.qa.diffcoverage.model.BranchProperty;
import com.alibaba.qa.diffcoverage.model.CompilationUnit;
import com.alibaba.qa.diffcoverage.model.FileProperty;
import com.alibaba.qa.diffcoverage.model.FunctionProperty;
import com.alibaba.qa.diffcoverage.model.LineProperty;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * 对ICoverageFileParser接口的具体实现
 * @author garcia.wul@alibaba-inc.com
 *
 */
public class CoverageFileParser implements ICoverageFileParser {
    @Setter private Logger logger = Logger.getRootLogger();
    @Getter @Setter
    private List<ASTFileLocation> fileLocations = null;
    
    // 所有的头文件
    @Getter
    private Map<String, SourceFile> headerFiles = Maps.newHashMap();

    @Override
    public FileProperty parse(String basePath, CompilationUnit compilationUnit) {
		List<String> gcdaFiles = Lists.newArrayList(compilationUnit.getGcdaFile());
		CovManager covManager = new CovManager();
		try {
			covManager.processCovFiles(gcdaFiles, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
		SourceFile sourceFile = null;
		for (SourceFile sourceFile2: covManager.getAllSrcs()) {
			if (compilationUnit.getSourceFile().endsWith(sourceFile2.getName())) {
				sourceFile = sourceFile2;
				break;
			}
		}
		if (sourceFile == null) {
		    logger.error(String.format("Can not parse %s 's source file", 
		        compilationUnit.getGcdaFile()));
		    return null;
		}
		
		// 查找其他文件，比如头文件
		for (SourceFile sourceFile2: covManager.getAllSrcs()) {
		    // TODO garcia.wul 2012-05-13 有没有更好地识别头文件的办法?
		    if (!sourceFile2.getName().endsWith(".h") && 
		        !sourceFile2.getName().endsWith(".hh") &&
		        !sourceFile2.getName().endsWith(".hpp") &&
		        !sourceFile2.getName().endsWith(".hxx"))
		        continue;
		    
		    // 这个flag是用于增量覆盖率的
		    boolean flag = true;
		    if (fileLocations != null) {
		        for (ASTFileLocation fileLocation: fileLocations) {
		            if (fileLocation.getFilename().endsWith(sourceFile.getName()))
		                flag = false;
		        }
		    }
		    if (!flag)
		        continue;
		    
		    // 如果是本项目目录下的,这种头文件肯定需要的
		    // TODO garcia.wul 2012-05-31 garcia.wul 
		    // 这里查找所依赖的头文件的办法可能需要一种更为合理的办法
		    File headerFile = null;
		    if (new File(sourceFile2.getName()).isAbsolute()) {
		        headerFile = new File(sourceFile2.getName());
		        headerFile = FileUtil.normalizeFile(headerFile);
		        if (!headerFile.getAbsolutePath().startsWith(basePath))
		            continue;
		    }
		    else {
		        headerFile = new File(basePath, sourceFile2.getName());
		        if (!headerFile.exists()) {
		            headerFile = new File(basePath, 
		                new File(compilationUnit.getSourceFile()).getParent());
		            headerFile = new File(headerFile, sourceFile2.getName());
		            if (!headerFile.exists())
		                headerFile = null;
		        }
		    }
		    if (headerFile == null) {
		        logger.warn(String.format("Can not find header file: [%s]", 
		            sourceFile2.getName()));
		        continue;
		    }
		    else {
		        headerFile = FileUtil.normalizeFile(headerFile);
		        logger.debug(String.format("Found [%s]'s header file: [%s]", 
		            compilationUnit.getSourceFile(), headerFile));
		    }
		    if (!headerFiles.keySet().contains(headerFile.getAbsolutePath())) {
		        headerFiles.put(headerFile.getAbsolutePath(), sourceFile2);
		    }
		}
		IASTTranslationUnit translationUnit = 
			ASTTranslationUnitCore.parse(new File(compilationUnit.getSourceFile()));
		List<String> fileLines = null;
		try {
            fileLines = FileUtils.readLines(new File(compilationUnit.getSourceFile()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
		FileProperty fileProperty = linesToFileProperty(
		    sourceFile, translationUnit, fileLines);
		fileProperty.setHtmlLink(
		    new File(fileProperty.getFilename()).getName() + ".html");
		return fileProperty;
    }
    
    @Override
    public FileProperty parseHeader(Entry<String, SourceFile> headerFile) {
        IASTTranslationUnit translationUnit = ASTTranslationUnitCore.parse(
            new File(headerFile.getKey()));
        List<String> fileLines = null;
        try{
            fileLines = FileUtils.readLines(new File(headerFile.getKey()));
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
        FileProperty fileProperty = linesToFileProperty(
            headerFile.getValue(), translationUnit, fileLines);
        fileProperty.setHtmlLink(
            new File(fileProperty.getFilename()).getName() + ".html");
        return fileProperty;
    }
    
    @Override
    public FileProperty parseZeroFile(String sourceFile) {
        List<Line> lines = Lists.newArrayList();
        List<String> fileLines = null;
        try{
            fileLines = FileUtils.readLines(new File(sourceFile));
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
        for (int i = 0; i != fileLines.size(); ++i) {
            Line line = new Line(i);
            line.setCount(0);
            line.setExists(false);
            lines.add(line);
        }
        IASTTranslationUnit translationUnit = ASTTranslationUnitCore.parse(
            new File(sourceFile));
        if (fileLocations == null) {
            FunctionDefinitionVisitor functionDefinitionVisitor = 
                new FunctionDefinitionVisitor();
            translationUnit.accept(functionDefinitionVisitor);
            for (IASTFunctionDefinition functionDefinition: 
                functionDefinitionVisitor.getFunctionDefinitions()) {
                for (int i = functionDefinition.getFileLocation().getStartingLineNumber() - 1; 
                    i < functionDefinition.getFileLocation().getEndingLineNumber(); ++i) {
                    lines.get(i).setExists(true);
                }
            }
        }
        else {
            for (ASTFileLocation fileLocation: fileLocations) {
                if (!fileLocation.getFilename().equals(sourceFile))
                    continue;
                for (int i = fileLocation.getStartingLineNumber() - 1;
                    i < fileLocation.getEndingLineNumber(); ++i)
                    lines.get(i).setExists(true);
            }
        }
        
        SourceFile sourceFile2 = new SourceFile(sourceFile, 0);
        sourceFile2.setLines(lines);
        FileProperty fileProperty = linesToFileProperty(
            sourceFile2, translationUnit, fileLines);
        fileProperty.setHtmlLink(
            new File(fileProperty.getFilename()).getName() + ".html");
        return fileProperty;
    }
    
    private FileProperty linesToFileProperty(SourceFile sourceFile,
        IASTTranslationUnit translationUnit,
        List<String> fileLines) {
        FileProperty fileProperty = new FileProperty();
        fileProperty.setFilename(FileUtil.normalizePath(translationUnit.getContainingFilename()));
        fileProperty.setLines(linesToLineProperties(sourceFile.getLines(), translationUnit, fileLines));
        fileProperty.setFunctionProperties(linesToFunctionProperties(sourceFile.getLines(),
            translationUnit, fileLines));
        fileProperty.setBranchProperty(linesToBranchProperties(sourceFile, translationUnit, fileLines));
        return fileProperty;
    }

    private List<BranchProperty> linesToBranchProperties(SourceFile sourceFile,
        IASTTranslationUnit translationUnit, List<String> fileLines) {
        List<BranchProperty> blockProperties = Lists.newArrayList();
        ASTStatementsVisitor statementsVisitor = new ASTStatementsVisitor();
        translationUnit.accept(statementsVisitor);
        for (IASTStatement statement: statementsVisitor.getStatements()) {
            BranchProperty branchProperty = linesToBranchProperty(sourceFile, statement, fileLines);
            if (branchProperty == null)
                continue;
            blockProperties.add(branchProperty);
        }
        return blockProperties;
    }

    private BranchProperty linesToBranchProperty(SourceFile sourceFile,
        IASTStatement statement, List<String> fileLines) {
        if (!isBlockStatement(statement))
            return null;
        if (isIgnoreBlock(statement))
        	return null;
        
        BranchProperty blockProperty = new BranchProperty();
        blockProperty.setFilename(statement.getContainingFilename());
        blockProperty.setEndingLineNumber(statement.getFileLocation().getEndingLineNumber());
        blockProperty.setStartingLineNumber(statement.getFileLocation().getStartingLineNumber());
        List<Line> childLines = getChildLines(sourceFile.getLines(), statement);
        blockProperty.setLines(linesToLineProperties(childLines, statement.getTranslationUnit(), fileLines));
        blockProperty.setCoveraged(isCoveragedBlock(blockProperty.getLines()));
        return blockProperty;
    }

    private boolean isBlockStatement(IASTStatement statement) {
        // TODO 对于else语句，这里暂时没有考虑
        if (statement instanceof IASTIfStatement)
            return true;
        if (statement instanceof IASTWhileStatement)
            return true;
        if (statement instanceof IASTDoStatement)
            return true;
        if (statement instanceof IASTSwitchStatement)
            return true;
        if (statement instanceof IASTCaseStatement)
            return true;
        if (statement instanceof IASTDefaultStatement)
            return true;
        if (statement instanceof IASTForStatement)
            return true;
        if (statement instanceof IASTGotoStatement)
            return true;
        if (statement instanceof ICPPASTTryBlockStatement)
            return true;
        if (statement instanceof IASTBreakStatement)
            return true;
        if (statement instanceof IASTContinueStatement)
            return true;
        return false;
    }

    private List<FunctionProperty> linesToFunctionProperties(List<Line> lines,
        IASTTranslationUnit translationUnit,
        List<String> fileLines) {
        List<FunctionProperty> functionProperties = Lists.newArrayList();
        FunctionDefinitionVisitor functionDefinitionVisitor = new FunctionDefinitionVisitor();
        translationUnit.accept(functionDefinitionVisitor);
        for (IASTFunctionDefinition functionDefinition: functionDefinitionVisitor.getFunctionDefinitions()) {
        	FunctionProperty functionProperty = linesToFunctionProperty(lines, functionDefinition, fileLines);
        	if (functionProperty == null)
        		continue;
            functionProperties.add(functionProperty);
        }
        return functionProperties;
    }

    private List<Line> getChildLines(List<Line> lines, final IASTNode node) {
        return Lists.newArrayList(filter(lines, new Predicate<Line>() {
            @Override
            public boolean apply(Line line) {
                return (
                    (line.getLineNumber() >= node.getFileLocation().getStartingLineNumber()) &&
                    (line.getLineNumber() <= node.getFileLocation().getEndingLineNumber()));
            }
        }));
    }

    private boolean isCoveragedBlock(List<LineProperty> lineProperties) {
        Boolean isCoveraged = false;
        
        for (LineProperty lineProperty: lineProperties) {
            if (lineProperty.getCoveragedNum() > 0) {
                isCoveraged = true;
                break;
            }
        }
        if (!isCoveraged) {
            int counter = Lists.newArrayList(filter(lineProperties, new Predicate<LineProperty>() {
                @Override
                public boolean apply(LineProperty lineProperty) {
                    if (lineProperty.isShouldIgnore())
                        return true;
                    return false;
                }
                })).size();
            if ((counter == lineProperties.size()) && (counter != 0))
                isCoveraged = true;
        }
        return isCoveraged;
    }

    private FunctionProperty linesToFunctionProperty(List<Line> lines,
        IASTFunctionDefinition functionDefinition,
        List<String> fileLines) {
    	if (isIgnoreBlock(functionDefinition))
    		return null;
        FunctionProperty functionProperty = new FunctionProperty();
        functionProperty.setEndingLineNumber(functionDefinition.getFileLocation().getEndingLineNumber());
        functionProperty.setFilename(FileUtil.normalizePath(functionDefinition.getFileLocation().getFileName()));
        functionProperty.setStartingLineNumber(functionDefinition.getFileLocation().getStartingLineNumber());
        List<Line> childLines = getChildLines(lines, functionDefinition);
        functionProperty.setLines(linesToLineProperties(childLines,
            functionDefinition.getTranslationUnit(), fileLines));
        functionProperty.setCoveraged(isCoveragedBlock(functionProperty.getLines()));
        return functionProperty;
    }
    
    private boolean isIgnoreBlock(IASTNode node) {
    	if (fileLocations == null)
    		return false;
    	
    	for (ASTFileLocation fileLocation: fileLocations) {
    		if (!fileLocation.getFilename().equals(node.getContainingFilename()))
    			continue;
    		if ((node.getFileLocation().getStartingLineNumber() >= fileLocation.getStartingLineNumber()) && 
    			(node.getFileLocation().getEndingLineNumber() <= fileLocation.getEndingLineNumber()))
    			return false;
    	}
    	return true;
    }

    /**
     * 将指定的所有lines转换成List<LineProperty>集合
     * @param lines
     * @param translationUnit
     * @param fileLines
     * @return
     */
    private List<LineProperty> linesToLineProperties(List<Line> lines,
        IASTTranslationUnit translationUnit,
        List<String> fileLines) {
        List<LineProperty> lineProperties = Lists.newArrayList();
        for (Line line: lines) {
            LineProperty lineProperty = lineToLineProperty(line, translationUnit, fileLines);
            if (lineProperty == null)
                continue;
            lineProperties.add(lineProperty);
        }
        return lineProperties;
    }

    /**
     * 将gcov的Line实例转换成LineProperty对象
     * @param line
     * @param translationUnit
     * @return
     */
    private LineProperty lineToLineProperty(Line line,
        IASTTranslationUnit translationUnit,
        List<String> fileLines) {
        // 不知道为什么，gcov取得行号是从0开始的，因此忽略行号为0的情况
        if (line.getLineNumber() <= 0)
            return null;

        LineProperty lineProperty = new LineProperty();
        lineProperty.setCoveragedNum(Long.valueOf(line.getCount()).intValue());
        lineProperty.setLineNumber(line.getLineNumber());
        lineProperty.setFilename(FileUtil.normalizePath(translationUnit.getContainingFilename()));
        lineProperty.setShouldIgnore(shouldIgnore(line, translationUnit, fileLines));
        return lineProperty;
    }

    /**
     * 判断改行在源文件中是否是应该被忽略的
     * @param line 需要判断的那行
     * @param translationUnit 该文件的节点对象
     * @param fileLines 该文件所有行的内容
     * @return
     */
    private boolean shouldIgnore(Line line, IASTTranslationUnit translationUnit,
        List<String> fileLines) {
    	// 如果在line信息中这行的exist是false的，说明就应该忽略
    	if (!line.isExists()) {
    		return true;
    	}
    	// fileLocations等于null，说明是计算全量的覆盖率
    	if (fileLocations != null) {
    		// 如果该行落在了某一个diff函数之中的话，则不应该去忽略它
    		boolean flag = false;
    		for (ASTFileLocation fileLocation: fileLocations) {
    			// 如果不是这个文件，则忽略
    			if (!fileLocation.getFilename().equals(translationUnit.getContainingFilename()))
    				continue;
    			if ((line.getLineNumber() >= fileLocation.getStartingLineNumber()) && 
    				(line.getLineNumber() <= fileLocation.getEndingLineNumber())) {
    				flag = true;
    				break;
    			}
    		}
    		if (flag)
    			return false;
    		else
    			return true;
    	}
        return false;
    }
}
