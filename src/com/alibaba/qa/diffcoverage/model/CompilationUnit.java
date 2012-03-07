package com.alibaba.qa.diffcoverage.model;

import lombok.Data;

@Data
public class CompilationUnit {
	private String sourceFile = null;
	private String gcdaFile = null;
	private String objectFile = null;
}
