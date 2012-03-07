package com.alibaba.qa.diffcoverage.model;

import java.util.List;

import lombok.Data;

import com.google.common.collect.Lists;

@Data
public class IgnorePattern {
	private List<String> ingoreFiles = Lists.newArrayList();
	private List<String> ignoreDirs = Lists.newArrayList(".svn");
}
