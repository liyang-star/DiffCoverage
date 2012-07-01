package com.alibaba.qa.diffcoverage.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.openide.filesystems.FileUtil;

import com.google.common.collect.Lists;

/**
 * 实现由gcc编译得到C/C++目标文件的解析
 * 
 * @author garcia.wul@alibaba-inc.com
 * 
 */
public class GccObjectFileParser implements IObjectFileParser {
	private Logger logger = Logger.getRootLogger();

	@Override
	public String lookForGcdaPath(String objectFile, String basePath) {
		String content = null;
		try {
			content = FileUtils.readFileToString(new File(objectFile));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String regex = String.format("(%s.*?\\.gcda)", basePath);
		RE pattern = new RE(regex);
		if (!pattern.match(content))
		    return null;
		String gcdaFile = pattern.getParen(0);
		
		return FileUtil.normalizePath(gcdaFile);
	}

	@Override
	public String lookForSourceFile(String objectFile, String basePath) {
		String content = null;
		try {
			content = FileUtils.readFileToString(new File(objectFile));
		} catch (IOException e) {
		    e.printStackTrace();
			return null;
		}

		String[] fields = content.split("\\x00");
		List<String> sourceFilenames = Lists.newArrayList();
		
		// TODO 2012-05-29 garcia.wul 
		// 目前只能支持以*.c/*.cc/*.cpp/*.cxx结尾的文件
		for (String field : fields) {
			field = removeInvalidCharacters(field);
			if (field.endsWith(".cc") || 
			    field.endsWith(".cpp") || 
			    field.endsWith(".c") ||
			    field.endsWith(".cxx")) {
				sourceFilenames.add(field);
			}
		}
		if (sourceFilenames.size() <= 0) {
		    logger.debug(String.format("Can not parse %s's source file", objectFile));
		    return null;
		}
		
		for (String sourceFilename: sourceFilenames) {
    		for (String field : fields) {
    			field = removeInvalidCharacters(field);
    			if (!field.startsWith(basePath.toString()))
    				continue;
    			File file = new File(field);
    			if (!file.exists())
    				continue;
    			file = new File(file, sourceFilename);
    			if (!file.exists())
    				continue;
    			file = FileUtil.normalizeFile(file);
    			return file.getAbsolutePath();
    		}
		}
		return null;
	}

	private String removeInvalidCharacters(String s) {
		for (int i = 0; i <= 26; ++i) {
			s = s.replace(Character.toString((char) i), "");
		}
		return s;
	}
	
	public static void main(String[] args) {
	    String objectFile = "/home/wul/cppcheck/lib/checkclass.o";
	    String basePath = "/home/wul/cppcheck";
	    GccObjectFileParser parser = new GccObjectFileParser();
	    String gcdaFile = parser.lookForGcdaPath(objectFile, basePath);
	    System.out.println(gcdaFile);
	}
}
