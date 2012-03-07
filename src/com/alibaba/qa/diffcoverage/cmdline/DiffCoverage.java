package com.alibaba.qa.diffcoverage.cmdline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.linuxtools.gcov.parser.SourceFile;
import org.ho.yaml.Yaml;
import org.kohsuke.args4j.CmdLineException;
import org.openide.filesystems.FileUtil;

import com.alibaba.qa.diffcoverage.core.FullCoverage;
import com.alibaba.qa.diffcoverage.core.ICoverage;
import com.alibaba.qa.diffcoverage.core.UpgradeCoverage;
import com.alibaba.qa.diffcoverage.model.ASTFileLocation;
import com.alibaba.qa.diffcoverage.model.CompilationUnit;
import com.alibaba.qa.diffcoverage.model.ConfigProperty;
import com.alibaba.qa.diffcoverage.model.FileProperty;
import com.alibaba.qa.diffcoverage.model.IgnorePattern;
import com.alibaba.qa.diffcoverage.model.PathProperty;
import com.alibaba.qa.diffcoverage.parser.IDiffParser;
import com.alibaba.qa.diffcoverage.parser.NormalDiffFormatParser;
import com.alibaba.qa.diffcoverage.parser.UnifiedDiffFormatParser;
import com.alibaba.qa.diffcoverage.utility.PathPropertiesExchanger;
import com.google.common.collect.Lists;

public class DiffCoverage {
	private static String version = "0.6.3";

    public static void main(String[] args) throws IOException, InterruptedException {
        CommandLineParser commandLineParser = new CommandLineParser();
        try {
            commandLineParser.parseArgument(args);
        } catch (CmdLineException e) {
        	if (!commandLineParser.isPrintVersion() && 
        	    !commandLineParser.isPrintVersion()) {
	            e.printStackTrace();
	            commandLineParser.getCmdlineParser().printUsage(System.err);
	            System.exit(1);
        	}
        }
        
        if (commandLineParser.isPrintVersion()) {
        	System.out.println(String.format("DiffCoverage Version: %s", version));
        	System.exit(0);
        }
        
        if (commandLineParser.isPrintUsage()) {
        	commandLineParser.getCmdlineParser().printUsage(System.out);
        	System.exit(0);
        }
                
        // 初始化logger配置信息
        ConfigProperty configProperty = new ConfigProperty();
        if (commandLineParser.isDebug())
            PropertyConfigurator.configure(configProperty.getDebugLoggerProperties());
        else
            PropertyConfigurator.configure(configProperty.getLoggerProperties());
        Logger logger = Logger.getRootLogger();
        
        if (! new File(commandLineParser.getProjectPath()).exists()) {
        	logger.error(String.format("Project path: %s does not exist", 
        	    commandLineParser.getProjectPath()));
        	System.exit(2);
        }
        
        commandLineParser.setProjectPath(
    		FileUtil.normalizePath(commandLineParser.getProjectPath()));
        
        if (commandLineParser.getIgnoreFile() != null) {
        	if (!new File(commandLineParser.getIgnoreFile()).exists()) {
        		logger.error(String.format("Ignore File: %s does not exist", 
        		    commandLineParser.getIgnoreFile()));
        		System.exit(3);
        	}
        	
        	FileInputStream fileInputStream = new FileInputStream(
        	    commandLineParser.getIgnoreFile());
        	try{
        	    configProperty.setIgnorePattern(
        	        Yaml.loadType(fileInputStream, IgnorePattern.class));
            }catch (Exception e){
                logger.error(String.format("The format of %s is not correct", 
                    commandLineParser.getIgnoreFile()));
                fileInputStream.close();
                System.exit(4);
            }
        	fileInputStream.close();
        }
        // 是否统计所有的文件
        configProperty.setAllFile(commandLineParser.isAllFiles());

        // 清除之前的目录
        if (new File(commandLineParser.getOutput()).exists()) {
            logger.debug(String.format("Output: %s existed, will remove it", 
                commandLineParser.getOutput()));
            if (new File(commandLineParser.getOutput()).isDirectory())
                try {
                    FileUtils.deleteDirectory(new File(commandLineParser.getOutput()));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(5);
                }
            else
                if (!new File(commandLineParser.getOutput()).delete()) {
                	logger.error(String.format("Delete Output: %s failed", 
                	    commandLineParser.getOutput()));
                	System.exit(6);
                }
        }
        
        logger.info(String.format("Now will mkdir output: %s", 
            commandLineParser.getOutput()));
        if (! new File(commandLineParser.getOutput()).mkdirs()) {
        	logger.error(String.format("Mkdir output: %s failed", 
        	    commandLineParser.getOutput()));
        	System.exit(7);
        }

        ICoverage coverage = null;

        // 如果是计算增量覆盖率
        if (commandLineParser.isIncrement()) {
            logger.info(
                String.format("Analysing Increment Coverage for %s, DiffFile is: %s", 
                commandLineParser.getProjectPath(), commandLineParser.getDiffFile()));
        	if (!new File(commandLineParser.getDiffFile()).exists()) {
        		logger.error(String.format("DiffFile: %s does not exist", 
        		    commandLineParser.getDiffFile()));
        		commandLineParser.getCmdlineParser().printUsage(System.err);
        		System.exit(8);
        	}
        	
            coverage = new UpgradeCoverage(configProperty);
            IDiffParser diffParser = null;
            if (commandLineParser.isNormalDiffFormat())
            	diffParser = new NormalDiffFormatParser(commandLineParser.getProjectPath());
            else
            	diffParser = new UnifiedDiffFormatParser(
            	    commandLineParser.getProjectPath());
            
            List<ASTFileLocation> fileLocations = 
        		diffParser.parse(FileUtils.readFileToString(
    		    new File(commandLineParser.getDiffFile())));
            ((UpgradeCoverage) coverage).setFileLocations(fileLocations);
        }
        else {
            coverage = new FullCoverage(configProperty);
        }
        
        
        // 查找所有的目标文件
        List<String> objectFiles = coverage.findObjectFiles(
            commandLineParser.getProjectPath());
        if ((objectFiles == null) || (objectFiles.size() <= 0)) {
            logger.error(String.format("Can not found any object files"));
            System.exit(9);
        }
        
        List<FileProperty> fileProperties = Lists.newArrayList();
        for (String objectFile: objectFiles) {
            CompilationUnit compilationUnit = coverage.findCompilationUnit(
                commandLineParser.getProjectPath(), objectFile);
            
            if (compilationUnit == null)
                continue;
            FileProperty fileProperty = coverage.analyseCoverageFiles(
                commandLineParser.getProjectPath(), compilationUnit);
            if (fileProperty == null)
                continue;
            fileProperties.add(fileProperty);
        }
        // 增加对头文件覆盖率信息的统计
        for (Entry<String, SourceFile> entry: 
            coverage.getCoverageFileParser().getHeaderFiles().entrySet()) {
            FileProperty fileProperty = coverage.getCoverageFileParser().parseHeader(entry);
            if (fileProperty == null)
                continue;
            fileProperties.add(fileProperty);
        }
        if (fileProperties.size() <= 0) {
            logger.error(String.format("Analysed coverage file failed"));
            System.exit(10);
        }
        
        if (configProperty.isAllFile()) {
            for (String sourceFile: coverage.getZeroFiles()) {
                FileProperty fileProperty = 
                    coverage.getCoverageFileParser().parseZeroFile(sourceFile);
                if (fileProperty == null)
                    continue;
                fileProperties.add(fileProperty);
            }
        }
        
        // 换一个覆盖率信息的纬度
        PathPropertiesExchanger pathPropertiesExchanger = new PathPropertiesExchanger();
        pathPropertiesExchanger.setBasePath(commandLineParser.getProjectPath());
        List<PathProperty> pathProperties = 
            pathPropertiesExchanger.fromFileProperties(fileProperties);
        
        // HTML输出
        coverage.dumpHtml(commandLineParser.getProjectPath(), 
    		commandLineParser.getOutput(), pathProperties);
        
        System.exit(0);
    }

}
