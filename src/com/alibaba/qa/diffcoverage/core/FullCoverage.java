package com.alibaba.qa.diffcoverage.core;

import java.io.File;

import org.openide.filesystems.FileUtil;

import com.alibaba.qa.diffcoverage.model.CompilationUnit;
import com.alibaba.qa.diffcoverage.model.ConfigProperty;
import com.alibaba.qa.diffcoverage.model.FileProperty;

public class FullCoverage extends AbstractCoverage {

	public FullCoverage(ConfigProperty configProperty) {
	    super(configProperty);
    }
	
    @Override
    public FileProperty analyseCoverageFiles(String basePath, 
        CompilationUnit compilationUnit) {
        return coverageFileParser.parse(basePath, compilationUnit);
    }
	
    @Override
    public CompilationUnit findCompilationUnit(String basePath, String objectFile) {
        String sourceFile = objectFileParser.lookForSourceFile(objectFile, basePath);
        if (sourceFile == null) {
            logger.error(String.format("Can not parse source file from: %s, " +
        		"you should compile it with \"-g\" option", objectFile));
            return null;
        }
        
        if (isIgnoreByPattern(new File(sourceFile)))
            return null;

        String gcdaFile = objectFileParser.lookForGcdaPath(objectFile);
        if (gcdaFile == null) {
//            if (configProperty.isAllFile())
//                zeroFiles.add(sourceFile);
//            else
//                logger.error(String.format(
//                    "Can not parse data file from %s, you should compile it with \"-ftest-coverage -fprofile-arcs\" options", 
//                    objectFile));
            return null;
        }
        // TODO 2012-01-09 garcia.wul 如果某一个文件一行代码都没有被覆盖到,则数据文件将不会被生成
        if (!new File(gcdaFile).exists()) {
//            if (configProperty.isAllFile())
//                zeroFiles.add(sourceFile);
//            else
//                logger.warn(String.format("%s does not exist, maybe the file was not coveraged", 
//                    gcdaFile));
            return null;
        }
        // TODO 2011-12-07 garcia.wul 如果有文件类似X.gcda.gcda的话，这块会有问题
        File gcnoFile = new File(gcdaFile.toString().replace(".gcda", ".gcno"));
        if (!gcnoFile.exists()) {
            logger.error(String.format("%s does not exist, you should compile it with \"-ftest-coverage -fprofile-arcs\" options", 
                gcnoFile));
            return null;
        }
        
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setObjectFile(FileUtil.normalizePath(objectFile));
        compilationUnit.setSourceFile(FileUtil.normalizePath(sourceFile));
        compilationUnit.setGcdaFile(FileUtil.normalizePath(gcdaFile));
        return compilationUnit;
    }

}
