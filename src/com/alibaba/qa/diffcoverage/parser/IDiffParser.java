package com.alibaba.qa.diffcoverage.parser;

import java.util.List;

import com.alibaba.qa.diffcoverage.model.ASTFileLocation;

public interface IDiffParser {
	public List<ASTFileLocation> parse(String diffStr);
}
