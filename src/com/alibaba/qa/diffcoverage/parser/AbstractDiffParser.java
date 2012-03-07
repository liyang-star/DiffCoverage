package com.alibaba.qa.diffcoverage.parser;

import org.apache.log4j.Logger;

public abstract class AbstractDiffParser implements IDiffParser {
	protected Logger logger = Logger.getRootLogger();
	protected String basePath = null;
	protected String splitLine = "===================================================================";

	public AbstractDiffParser(String basePath) {
		this.basePath = basePath;
	}
}
