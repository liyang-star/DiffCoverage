package com.alibaba.qa.diffcoverage.model;

import java.util.List;

import lombok.Data;

@Data
public class FunctionProperty {
    private int startingLineNumber = -1;
    private int endingLineNumber = -1;
    private List<LineProperty> lines = null;
    private String filename = null;
    private boolean isCoveraged = false;
}
