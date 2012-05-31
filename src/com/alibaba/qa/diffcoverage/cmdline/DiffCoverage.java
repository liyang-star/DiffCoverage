package com.alibaba.qa.diffcoverage.cmdline;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

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
	private static String version = "0.6.4";

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
        
        // 如果项目的代码路径不存在
        if (!commandLineParser.getProjectPath().exists()) {
        	logger.error(String.format("Project path: %s does not exist", 
        	    commandLineParser.getProjectPath()));
        	System.exit(2);
        }
        commandLineParser.setProjectPath(
    		FileUtil.normalizeFile(commandLineParser.getProjectPath()));
        
        if (commandLineParser.getIgnoreFile() != null) {
        	if (!commandLineParser.getIgnoreFile().exists()) {
        		logger.error(String.format("Ignore File: %s does not exist", 
        		    commandLineParser.getIgnoreFile()));
        		System.exit(3);
        	}
        	
        	FileInputStream fileInputStream = new FileInputStream(
        	    commandLineParser.getIgnoreFile());
        	try{
        	    configProperty.setIgnorePattern(
        	        Yaml.loadType(fileInputStream, IgnorePattern.class));
        	    if (configProperty.getIgnorePattern().getIgnoreFiles().size() > 0) {
            	    logger.info(String.format("Ignore File Patterns: "));
            	    for (String pattern :
            	        configProperty.getIgnorePattern().getIgnoreFiles()) {
            	        logger.info(String.format("\t- %s", pattern));
            	    }
        	    }
        	    if (configProperty.getIgnorePattern().getIgnoreDirs().size() > 0) {
        	        logger.info(String.format("Ignore Dir Patterns: "));
        	        for (String pattern: 
        	            configProperty.getIgnorePattern().getIgnoreDirs()) {
        	            logger.info(String.format("\t- %s", pattern));
        	        }
        	    }
            }catch (Exception e){
                logger.error(String.format("The format of %s is not correct", 
                    commandLineParser.getIgnoreFile()));
                System.exit(4);
            }finally {
                fileInputStream.close();
            }
        }

        // 清除之前的目录
        if (commandLineParser.getOutput().exists()) {
            if (commandLineParser.getOutput().isDirectory())
                FileUtils.deleteDirectory(commandLineParser.getOutput());
            else
                if (!commandLineParser.getOutput().delete()) 
                	System.exit(6);
        }
        
        if (!commandLineParser.getOutput().mkdirs()) {
        	logger.error(String.format("Mkdir output: %s failed", 
        	    commandLineParser.getOutput()));
        	System.exit(7);
        }
        
        ICoverage coverage = null;

        // 如果是计算增量覆盖率
        if (commandLineParser.isIncrement()) {
        	if (!commandLineParser.getDiffFile().exists()) {
        		logger.error(String.format("DiffFile: %s does not exist", 
        		    commandLineParser.getDiffFile()));
        		commandLineParser.getCmdlineParser().printUsage(System.err);
        		System.exit(8);
        	}
        	
            coverage = new UpgradeCoverage(configProperty);
            IDiffParser diffParser = null;
            if (commandLineParser.isNormalDiffFormat())
            	diffParser = new NormalDiffFormatParser(
            	    commandLineParser.getProjectPath().getAbsolutePath());
            else
            	diffParser = new UnifiedDiffFormatParser(
            	    commandLineParser.getProjectPath().getAbsolutePath());
            
            List<ASTFileLocation> fileLocations = 
        		diffParser.parse(FileUtils.readFileToString(
    		    commandLineParser.getDiffFile()));
            ((UpgradeCoverage) coverage).setFileLocations(fileLocations);
        }
        else {
            coverage = new FullCoverage(configProperty);
        }
        
        
        // 查找所有的目标文件
        logger.info(String.format("Start analysing all object files"));
        Queue<String> objectFiles = coverage.findObjectFiles(
            commandLineParser.getProjectPath().getAbsolutePath());
        if ((objectFiles == null) || (objectFiles.size() <= 0)) {
            logger.error(String.format("Can not found any object files"));
            System.exit(9);
        }
        
        List<FileProperty> fileProperties = Lists.newArrayList();
        List<Thread> threads = Lists.newArrayList();
        for (int i = 0; i != commandLineParser.getThreadsNum(); ++i) {
            DiffCoverageRunnable runnable = new DiffCoverageRunnable(
                objectFiles, coverage, commandLineParser, fileProperties);
            threads.add(new Thread(runnable));
        }
        for (Thread thread: threads)
            thread.start();
        for (Thread thread: threads)
            thread.join();
            logger.info(String.format("Finish to analysed all object files"));
        
        // 增加对头文件覆盖率信息的统计
        logger.info(String.format("Start analysing all headers' coverage"));
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
        
        Collections.sort(fileProperties, new Comparator<FileProperty>(){
            @Override
            public int compare(FileProperty o1, FileProperty o2){
                return o1.getFilename().compareToIgnoreCase(o2.getFilename());
            }
        });
        
        for (String sourceFile: coverage.getZeroFiles()) {
            FileProperty fileProperty = 
                coverage.getCoverageFileParser().parseZeroFile(sourceFile);
            if (fileProperty == null)
                continue;
            fileProperties.add(fileProperty);
        }
        
        // 换一个覆盖率信息的纬度
        PathPropertiesExchanger pathPropertiesExchanger = new PathPropertiesExchanger();
        pathPropertiesExchanger.setBasePath(commandLineParser.getProjectPath().getAbsolutePath());
        List<PathProperty> pathProperties = 
            pathPropertiesExchanger.fromFileProperties(fileProperties);
        
        // HTML输出
        coverage.dumpHtml(commandLineParser.getProjectPath().getAbsolutePath(), 
    		commandLineParser.getOutput().getAbsolutePath(), pathProperties);
        
        System.exit(0);
    }

}
