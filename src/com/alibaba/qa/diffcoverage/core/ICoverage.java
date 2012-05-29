package com.alibaba.qa.diffcoverage.core;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.alibaba.qa.diffcoverage.model.CompilationUnit;
import com.alibaba.qa.diffcoverage.model.FileProperty;
import com.alibaba.qa.diffcoverage.model.PathProperty;
import com.alibaba.qa.diffcoverage.parser.ICoverageFileParser;


public interface ICoverage {
	/**
	 * 分析得到指定文件的编译单元
	 * @param path
	 * @return
	 */
	public CompilationUnit findCompilationUnit(String basePath, String objectFile);

    /**
     * 分析覆盖率文件
     * @return 返回的是所有源文件的信息
     */
    public FileProperty analyseCoverageFiles(String basePath, 
        CompilationUnit compilationUnit);

    /**
     * 输出HTML结果到指定的目录
     * @return
     */
    public void dumpHtml(String basePath, String output, List<PathProperty> pathProperties);
    
    /**
     * 查找所有的目标文件
     * @param path
     * @return
     */
    public Queue<String> findObjectFiles(String path);
    
    public ICoverageFileParser getCoverageFileParser();
    
    public Set<String> getZeroFiles();

}
