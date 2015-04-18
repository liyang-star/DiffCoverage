package com.alibaba.qa.diffcoverage.model;

import lombok.Data;

/**
 * 代码行所具有的属性,它是覆盖率信息中的最小单位
 * @author garcia.relax@gmail.com
 *
 */
@Data
public class LineProperty {
    private int lineNumber = -1;
    private int coveragedNum = -1; // 覆盖次数
    private boolean shouldIgnore = false;  // 对于 { 或者 },注释, 空白行这些的可以忽略
    private String filename = null;
}
