package com.alibaba.qa.diffcoverage.parser;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.openide.filesystems.FileUtil;

/**
 * 实现由gcc编译得到C/C++目标文件的解析
 * 
 * @author garcia.wul@alibaba-inc.com
 * 
 */
public class GccObjectFileParser implements IObjectFileParser {
	private Logger logger = Logger.getRootLogger();

	@Override
	public String lookForGcdaPath(String objectFile) {
		String content = null;
		try {
			content = FileUtils.readFileToString(new File(objectFile));
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}

		String gcdaFile = null;
		String[] fields = content.split("\\x00");
		for (String field : fields) {
			if (field.endsWith(".gcda")) {
				gcdaFile = field;
				break;
			}
		}
		if (gcdaFile == null) {
			logger.debug(String.format(
		        "Can not parse gcda file from object file: %s", objectFile));
			return null;
		}
		// 2011-12-07 garcia.wul 测试中，发现通过\x00
		// split后，还有一些是以\x03/home/admin/***.gcda的情况，因此通过
		// 正则表达式把这些去掉
		RE pattern = new RE(String.format("%s", "(\\/.*?\\.gcda)"));
		if (!pattern.match(gcdaFile)) {
			return null;
		}
		gcdaFile = pattern.getParen(0);
		return FileUtil.normalizePath(gcdaFile);
	}

	@Override
	public String lookForSourceFile(String objectFile, String basePath) {
		basePath = FileUtil.normalizePath(basePath);
		String content = null;
		try {
			content = FileUtils.readFileToString(new File(objectFile));
		} catch (IOException e) {
		    e.printStackTrace();
			return null;
		}

		String sourceFilename = null;
		String[] fields = content.split("\\x00");
		for (String field : fields) {
			field = removeInvalidCharacters(field);
			if (field.endsWith(".cc") || (field.endsWith(".cpp"))
			        || (field.endsWith(".c"))) {
				sourceFilename = field;
				break;
			}
		}
		if (sourceFilename == null) {
			logger.debug(String.format(
		        "Can not parse source filename from object file: %s", objectFile));
			return null;
		}
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
		return null;
	}

	private String removeInvalidCharacters(String s) {
		for (int i = 0; i <= 26; ++i) {
			s = s.replace(Character.toString((char) i), "");
		}
		return s;
	}
}
