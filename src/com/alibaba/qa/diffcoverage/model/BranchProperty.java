package com.alibaba.qa.diffcoverage.model;

import java.util.List;

import lombok.Data;


/**
 * 代码块所具有的属性
 * @author garcia.wul@alibaba-inc.com
 *
 */
@Data
public class BranchProperty {
    private int startingLineNumber = -1;
    private int endingLineNumber = -1;
    private List<LineProperty> lines = null;
    private String filename = null;
    private boolean isCoveraged = false;
}
