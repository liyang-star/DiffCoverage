package com.alibaba.qa.diffcoverage.parser;


/**
 * 分析C/C++的目标文件的接口
 * @author garcia.wul@alibaba-inc.com
 *
 */
public interface IObjectFileParser {

	public String lookForSourceFile(String objectFile, String basePath);

    /**
     * 从目标文件中解析出相应的gcda文件
     * @param objectFile
     * @return 如果解析/打开文件失败，则返回null;否则返回解析得到的gcda文件
     */
    public String lookForGcdaPath(String objectFile, String basePath);
}
