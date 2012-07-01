package com.alibaba.qa.diffcoverage.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.activation.MimetypesFileTypeMap;

import lombok.Getter;
import lombok.Setter;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openide.filesystems.FileUtil;

import com.alibaba.qa.diffcoverage.parser.GccObjectFileParser;
import com.alibaba.qa.diffcoverage.parser.IObjectFileParser;

public class CheckObjectFile {
    @Getter @Setter @Option(name="--projectPath", usage="project path", 
        metaVar="projectPath", required=false)
    private File projectPath = 
        FileUtil.normalizeFile(new File(".").getAbsoluteFile());
    
    @Getter
    private CmdLineParser cmdlineParser = new CmdLineParser(this);
    {
        cmdlineParser.setUsageWidth(120);
    }
    
    public void parseArgument(String[] args) throws CmdLineException {
        cmdlineParser.parseArgument(args);
    }
    
    static private String getFileMimeType(File file) {
        return new MimetypesFileTypeMap().getContentType(file);
    }
    
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException{
        CheckObjectFile checkObjectFile = new CheckObjectFile();
        try{
            checkObjectFile.parseArgument(args);
        }catch (CmdLineException e){
            e.printStackTrace();
            checkObjectFile.getCmdlineParser().printUsage(System.err);
            System.exit(1);
        }
        
        
        IObjectFileParser objectFileParser = new GccObjectFileParser();
        
        BufferedReader bufferedReader = 
            new BufferedReader(new InputStreamReader(System.in));
        String objectFile = null;
        
        long gcdaTotalTime = 0;
        long sourceTotalTime = 0;
        int counter = 0;
        
        while ((objectFile = bufferedReader.readLine()) != null) {
            System.out.println(String.format("[INFO]%s 的文件类型: %s", objectFile, 
                getFileMimeType(new File(objectFile))));
            
            long startTime = System.currentTimeMillis();
            String gcdaFile = objectFileParser.lookForGcdaPath(objectFile, 
                checkObjectFile.getProjectPath().getAbsolutePath());
            gcdaTotalTime += (System.currentTimeMillis() - startTime);
            
            if (gcdaFile != null) {
                System.out.println(String.format("[INFO]解析得到 %s 的覆盖率文件: %s", 
                    objectFile, gcdaFile));
            }
            
            else {
                System.out.println(String.format("[ERROR]没有分析得到 %s 的覆盖率文件", 
                    objectFile));
            }
            
            startTime = System.currentTimeMillis();
            String sourceFile = objectFileParser.lookForSourceFile(objectFile, 
                checkObjectFile.getProjectPath().getAbsolutePath());
            sourceTotalTime += (System.currentTimeMillis() - startTime);
            if (sourceFile != null) {
                System.out.println(String.format("[INFO]解析得到 %s 的源文件: %s", 
                    objectFile, sourceFile));
            }
            else {
                System.out.println(String.format("[ERROR]没有解析得到 %s 的源文件", objectFile));
            }
            
            ++ counter;
        }
        
        System.out.println(String.format("GCDA花费时间: %s ms", gcdaTotalTime));
        System.out.println(String.format("GCDA平均时间: %s ms", gcdaTotalTime / counter));
        System.out.println(String.format("源文件花费时间: %s ms", sourceTotalTime));
        System.out.println(String.format("源文件平均时间: %s ms", sourceTotalTime / counter));
        System.out.println(String.format("文件总数: %s", counter));
    }
}
