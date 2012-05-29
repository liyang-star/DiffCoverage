package com.alibaba.qa.diffcoverage.cmdline;

import java.util.List;
import java.util.Queue;

import com.alibaba.qa.diffcoverage.core.ICoverage;
import com.alibaba.qa.diffcoverage.model.CompilationUnit;
import com.alibaba.qa.diffcoverage.model.FileProperty;

public class DiffCoverageRunnable implements Runnable{
    private List<FileProperty> fileProperties = null;
    private Queue<String> objectFiles = null;
    private ICoverage coverage = null;
    private CommandLineParser commandLineParser = null;
    
    public DiffCoverageRunnable(Queue<String> objectFiles, 
        ICoverage coverage,
        CommandLineParser commandLineParser,
        List<FileProperty> fileProperties) {
        this.objectFiles = objectFiles;
        this.coverage = coverage;
        this.commandLineParser = commandLineParser;
        this.fileProperties = fileProperties;
    }

    @Override
    public void run(){
        while (!objectFiles.isEmpty()) {
            String objectFile = objectFiles.remove();
            CompilationUnit compilationUnit = coverage.findCompilationUnit(
                commandLineParser.getProjectPath().getAbsolutePath(), objectFile);
            if (compilationUnit == null)
                return;
            FileProperty fileProperty = coverage.analyseCoverageFiles(
                commandLineParser.getProjectPath().getAbsolutePath(), compilationUnit);
            if (fileProperty == null)
                return;
            fileProperties.add(fileProperty);
        }
    }

}
