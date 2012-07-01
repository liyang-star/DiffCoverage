package com.alibaba.qa.diffcoverage.unittests;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.qa.diffcoverage.parser.GccObjectFileParser;
import com.alibaba.qa.diffcoverage.parser.IObjectFileParser;

public class GccObjectFileParserTester {
    private File tmpPath = null;
    private File sourceFile = null;
    private File objectFile = null;
    private String sourceCode = null;
    private IObjectFileParser objectFileParser = new GccObjectFileParser();

    @Before
    public void setUp() throws Exception {
        tmpPath = new File(System.getProperty("java.io.tmpdir"));
        sourceFile = new File(tmpPath, "GccObjectFileParserTester.cc");
        objectFile = new File(tmpPath, "GccObjectFileParserTester.o");
    }

    @After
    public void tearDown() throws Exception {
        if (sourceFile.exists())
            sourceFile.delete();
        if (objectFile.exists())
            objectFile.delete();
        tmpPath = null;
        sourceFile = null;
        sourceCode = null;
        objectFile = null;
    }

    protected void init() throws Exception {
        FileUtils.write(sourceFile, sourceCode);
        String command =
            String.format("g++ -c -ftest-coverage -fprofile-arcs -o %s %s", objectFile, sourceFile);
        Runtime.getRuntime().exec(command);
    }


    /**
     * 对lookForGcdaPath()方法的测试
     */
    @Test
    public void testLookForGcdaPathFromObject() throws Exception {
        sourceCode = "int main() {return 0;}";
        init();
//        String expectedResult = new File(tmpPath, "GccObjectFileParserTester.gcda").toString();
//        assertEquals(expectedResult, objectFileParser.lookForGcdaPath(
//            objectFile.getAbsolutePath(), ));
    }

}
