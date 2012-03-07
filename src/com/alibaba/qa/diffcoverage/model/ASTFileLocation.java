package com.alibaba.qa.diffcoverage.model;

import lombok.Data;

@Data
public class ASTFileLocation{
    private String filename = null;
    private int startingLineNumber = 0;
    private int endingLineNumber = 0;
}
