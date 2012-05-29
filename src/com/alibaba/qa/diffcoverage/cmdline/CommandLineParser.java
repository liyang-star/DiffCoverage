package com.alibaba.qa.diffcoverage.cmdline;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openide.filesystems.FileUtil;

/**
 * 用于解析命令行参数
 * @author garcia.wul
 *
 */
public class CommandLineParser {
    @Getter @Setter @Option(name="--projectPath", usage="project path", 
        metaVar="projectPath", required=true)
    private File projectPath = 
        FileUtil.normalizeFile(new File(".").getAbsoluteFile());

    @Getter @Option(name="--output", usage="HTML's output path", 
        metaVar="Output", required=true)
    private File output = null;

    @Getter @Option(name="--ignoreFile", usage="Used to ignore some files",
        metaVar="IgnoreFile", required=false)
    private File ignoreFile = null;

    @Getter @Option(name="--isIncrement", usage="Is analyse increment coverage?", 
        required=false)
    private boolean isIncrement = false;

    @Getter @Option(name="--diffFile", usage="The diff file", 
        metaVar="DiffFile", required=false)
    private File diffFile = null;

    @Getter @Option(name="--isNormalDiffFormat", 
        usage="Your diff file is normal format", required=false)
    private boolean isNormalDiffFormat = false;

    @Getter @Option(name="--debug", usage="Enable debug information", 
        required=false)
    private boolean isDebug = false;
    
    @Getter @Option(name="--version", usage="Print out version", required=false)
    private boolean isPrintVersion = false;
    
    @Getter @Option(name="--help", usage="Print usage", required=false)
    private boolean isPrintUsage = false;
    
    @Getter
    @Option(name="--isAllFile", usage="Include all files", required=false)
    private boolean isAllFile = false;
    
    @Getter 
    @Option(name="--threadsNum", usage="Threads Num", required=false)
    private int threadsNum = 1;

    @Getter
    private CmdLineParser cmdlineParser = new CmdLineParser(this);
    {
    	cmdlineParser.setUsageWidth(120);
    }
    
    public void parseArgument(String[] args) throws CmdLineException {
        cmdlineParser.parseArgument(args);
    }
}
