/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.gcov.parser;

import java.io.Serializable;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;

import com.google.common.collect.Lists;

public class SourceFile implements Serializable {

    private static final long serialVersionUID = -9182882194956475711L;
    private final String name;
    private final int index;
    @Getter @Setter
    private List<Line> lines = Lists.newArrayList();
    private final TreeSet<GcnoFunction> fnctns = new TreeSet<GcnoFunction>();
    private int numLines = 1;
    private final CoverageInfo cvrge = new CoverageInfo();
    private long maxCount = -1;

    /**
     * Constructor
     */
    public SourceFile(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public void accumulateLineCounts() {
        for (Line line : lines) {
            if (line.isExists()) {
                cvrge.incLinesInstrumented();
                if (line.getCount() != 0)
                    cvrge.incLinesExecuted();
            }
        }
    }

    public long getmaxLineCount() {
        if (maxCount < 0) {
            for (Line line : lines) {
                if (line.getCount() > maxCount)
                    maxCount = line.getCount();
            }
        }
        return maxCount;
    }

    /* getters & setters */



    public int getLinesExecuted() {
        return cvrge.getLinesExecuted();
    }

    public int getLinesInstrumented() {
        return cvrge.getLinesInstrumented();
    }

    public String getName() {
        return name;
    }

    public SortedSet<GcnoFunction> getFnctns() {
        return fnctns;
    }

    public void addFnctn(GcnoFunction fnctn) {
        this.fnctns.add(fnctn);
    }

    public int getNumLines() {
        return numLines;
    }

    public void setNumLines(int numLines) {
        this.numLines = numLines;
    }

    public int getIndex() {
        return index;
    }

    public void createLines() {
        int n = getNumLines();
//        ((ArrayList<Line>)lines).ensureCapacity(n);
        for (int j = 0; j < n ; ++j) {
            // 2011-12-05 garcia.wul 增加行号
            lines.add(new Line(j));
        }
    }


}
