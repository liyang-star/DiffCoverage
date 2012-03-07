package com.alibaba.qa.diffcoverage.model;

import java.util.List;

import lombok.Data;

@Data
public class FileProperty {
    private String filename = null;
    private List<LineProperty> lines = null;
    private List<FunctionProperty> functionProperties = null;
    private List<BranchProperty> branchProperty = null;

    private String htmlLink = null;
}
