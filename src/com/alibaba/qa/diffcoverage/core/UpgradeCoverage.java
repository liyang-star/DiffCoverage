package com.alibaba.qa.diffcoverage.core;

import java.io.File;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.openide.filesystems.FileUtil;

import com.alibaba.qa.diffcoverage.model.ASTFileLocation;
import com.alibaba.qa.diffcoverage.model.CompilationUnit;
import com.alibaba.qa.diffcoverage.model.ConfigProperty;
import com.alibaba.qa.diffcoverage.model.FileProperty;
import com.alibaba.qa.diffcoverage.parser.CoverageFileParser;
import com.google.common.collect.Lists;

public class UpgradeCoverage extends AbstractCoverage {
	@Getter @Setter
	private List<ASTFileLocation> fileLocations = Lists.newArrayList();

	public UpgradeCoverage(ConfigProperty configProperty) {
	    super(configProperty);
    }
	
	/**
	 * 对所有有改动的文件，做编译单元的查询
	 */
	@Override
    public CompilationUnit findCompilationUnit(String basePath, String objectFile) {
	    List<String> diffFiles = Lists.newArrayList();
	    for (ASTFileLocation fileLocation: fileLocations) {
	        if (!new File(fileLocation.getFilename()).exists()) 
	            continue;
	        diffFiles.add(fileLocation.getFilename());
	    }
        String sourceFile = objectFileParser.lookForSourceFile(objectFile, basePath);
        if (sourceFile == null) {
            logger.error(String.format(
                "Can not parse source file from %s, you should compile it with \"-g\" options", 
                objectFile));
            return null;
        }
        if (isIgnoreByPattern(new File(sourceFile)))
            return null;
        if (!diffFiles.contains(sourceFile.toString()))
            return null;

        String gcdaFile = objectFileParser.lookForGcdaPath(objectFile);
        if (gcdaFile == null) {
            if (configProperty.isAllFile())
                zeroFiles.add(sourceFile);
            else
                logger.error(
                    String.format("Can not parse data file from %s, you should compile it with \"-ftest-coverage -fprofile-arcs\" options", 
                        objectFile));
            return null;
        }
        // TODO 2012-01-09 garcia.wul 如果某一个文件一行代码都没有被覆盖到,则数据文件将不会被生成
        if (!new File(gcdaFile).exists()) {
            if (configProperty.isAllFile())
                zeroFiles.add(sourceFile);
            else
                logger.warn(String.format("%s does not exist, maybe the file was not coveraged", 
                    gcdaFile));
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
	
    @Override
    public FileProperty analyseCoverageFiles(String basePath, 
        CompilationUnit compilationUnit) {
		boolean flag = false;
		for (ASTFileLocation fileLocation: fileLocations) {
			if (compilationUnit.getSourceFile().equals(fileLocation.getFilename())) {
				flag = true;
				break;
			}
		}
		if (!flag) {
			logger.error(String.format("Can not parse ASTFileLocation: %s", 
				compilationUnit.getSourceFile()));
			return null;
		}
    	((CoverageFileParser) coverageFileParser).setFileLocations(fileLocations);
        return coverageFileParser.parse(basePath, compilationUnit);
    }

}
