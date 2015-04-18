package com.alibaba.qa.diffcoverage.unittests;

import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 对CoverageFileParser的单元测试
 * @author garcia.relax@gmail.com
 *
 */
public class CoverageFileParserTester {
    private File tmpPath = null;
    private File sourceFile = null;
    private File gcdaFile = null;
    private File executeFile = null;
    private String sourceCode = "";

    @Before
    public void setUp() throws Exception {
        tmpPath = new File(System.getProperty("java.io.tmpdir"));
        sourceFile = new File(tmpPath, "CoverageFileParserTester.cc");
        gcdaFile = new File(tmpPath, "CoverageFileParserTester.gcda");
        executeFile = new File(tmpPath, "CoverageFileParserTester");
    }

    @After
    public void tearDown() throws Exception {
        if (sourceFile.exists())
            sourceFile.delete();
        if (gcdaFile.exists())
            gcdaFile.delete();

        tmpPath = null;
        sourceFile = null;
        sourceCode = null;
    }

    protected void init() throws Exception {
        FileUtils.write(sourceFile, sourceCode);
        String command =
            String.format("g++ -ftest-coverage -fprofile-arcs -o %s %s -lgcov && %s",
            executeFile, sourceFile, executeFile);
        Runtime.getRuntime().exec(command);
    }

    @Test
    public void test() {
        fail("Not yet implemented");
    }

}
