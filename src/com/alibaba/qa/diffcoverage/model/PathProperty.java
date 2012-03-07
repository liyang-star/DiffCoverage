package com.alibaba.qa.diffcoverage.model;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import lombok.Data;

import com.google.common.collect.Lists;


@Data
public class PathProperty {
    private DecimalFormat decimalFormat = new DecimalFormat();
    {
    	decimalFormat.setMaximumIntegerDigits(3);
    	decimalFormat.setMaximumFractionDigits(2);
    }

    private File filename = null;

    private int linesNum = 0;  // 行数
    private int coveragedLinesNum = 0; // 覆盖的行数
    private double linesCoveragePercent = 0.0; // 覆盖率值
    private String linesCoveragePercentString = "0.0";
    private String coveragePercentChart = null;

    private int functionsNum = 0;
    private int coveragedFunctionsNum = 0;
    private double functionsCoveragePercent = 0.0;
    private String functionsCoveragePercentString = "0.0";

    private int branchesNum = 0;
    private int coveragedBranchesNum = 0;
    private double branchesCoveragePercent = 0.0;
    private String branchesCoveragePercentString = "0.0";

    private List<PathProperty> childrenPathProperties = Lists.newArrayList();
    // 因为要对没有覆盖的行进行高亮，因此需要行的信息.如果这个是目录，则不需要这个信息
    private List<LineProperty> lineProperties = Lists.newArrayList();

    private String htmlLink = null;

    public void fillPercentsString() {
    	linesCoveragePercentString = decimalFormat.format(linesCoveragePercent * 100);
    	functionsCoveragePercentString = decimalFormat.format(functionsCoveragePercent * 100);
    	branchesCoveragePercentString = decimalFormat.format(branchesCoveragePercent * 100);
    }

}
