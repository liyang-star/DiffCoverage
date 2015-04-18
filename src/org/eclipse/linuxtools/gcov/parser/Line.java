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

import lombok.Data;

@SuppressWarnings("serial")
@Data
public class Line implements Serializable{

    private boolean exists = false;  // 改行是否存在
    private long count = 0;  // 每行的执行次数
    // 2011-12-05 Wu Liang 增加行号
    private int lineNumber = -1;  // 行号

    public Line(int lineNumber) {
        setLineNumber(lineNumber);
    }
}
