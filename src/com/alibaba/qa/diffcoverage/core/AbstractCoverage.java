package com.alibaba.qa.diffcoverage.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openide.filesystems.FileUtil;

import cambridge.FileTemplateLoader;
import cambridge.Template;
import cambridge.TemplateEvaluationException;
import cambridge.TemplateFactory;

import com.alibaba.qa.diffcoverage.model.ConfigProperty;
import com.alibaba.qa.diffcoverage.model.LineProperty;
import com.alibaba.qa.diffcoverage.model.PathProperty;
import com.alibaba.qa.diffcoverage.model.templates.ResultTemplate;
import com.alibaba.qa.diffcoverage.parser.CoverageFileParser;
import com.alibaba.qa.diffcoverage.parser.GccObjectFileParser;
import com.alibaba.qa.diffcoverage.parser.ICoverageFileParser;
import com.alibaba.qa.diffcoverage.parser.IObjectFileParser;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * ICoverage的抽象类
 * @author garcia.wul@alibaba-inc.com
 *
 */
public abstract class AbstractCoverage implements ICoverage {
    protected Logger logger = Logger.getRootLogger();
    // TODO 20110-12-07 garcia.wul 这个属性使用Guice的注入来实现多态可能更好
    protected IObjectFileParser objectFileParser = new GccObjectFileParser();
    @Getter
    protected ICoverageFileParser coverageFileParser = new CoverageFileParser();
    protected FileTemplateLoader templateLoader = new FileTemplateLoader();

    @Getter @Setter
    protected File tmpPath = new File(System.getProperty("user.home"));
    // 这里需要对这两个临时文件加上get方法，这样可以在外围去delete这两个文件
    @Getter @Setter
    protected File directoryResultTemplateFile = 
        new File(tmpPath, ".directoryResultTemplateFile");
    @Getter @Setter
    protected File fileResultTemplateFile = 
        new File(tmpPath, ".fileResultTemplateFile");
    @Setter
    protected ConfigProperty configProperty = null;
    
    // 2012-03-05 garcia.wul 这些文件是没有产生GCDA文件的
    @Getter @Setter
    protected Set<String> zeroFiles = Sets.newHashSet();

    public AbstractCoverage(ConfigProperty configProperty) {
        setConfigProperty(configProperty);
        getFile(ResultTemplate.dirTemplate, directoryResultTemplateFile);
        getFile(ResultTemplate.fileTemplate, fileResultTemplateFile);
    }

    @Override
    public void dumpHtml(String basePath, String output, 
        List<PathProperty> pathProperties) {
        for (PathProperty pathProperty: pathProperties) {
            if (pathProperty.getFilename().isDirectory())
                writeDirPathProperty(basePath, output, pathProperty);
            else
                writeFilePathProperty(basePath, output, pathProperty);
        }
    }

    @Override
    public List<String> findObjectFiles(String path) {
        List<String> files = Lists.newArrayList();
        File[] subFiles = new File(path).listFiles();
        if (subFiles == null)
            return files;
        for (File file: subFiles) {
            if (file.isDirectory())
                files.addAll(findObjectFiles(FileUtil.normalizePath(file.getAbsolutePath())));
            else if ((getFileMimeType(file).contains("application/octet-stream")) &&
                ((file.toString().endsWith(".o") || file.toString().endsWith(".obj"))))
                files.add(FileUtil.normalizePath(file.getAbsolutePath()));
        }
        return files;
    }

    /**
     * 分析文件的mime type
     * @param file
     * @return
     */
    protected String getFileMimeType(File file) {
        return new MimetypesFileTypeMap().getContentType(file);
    }

    /**
     * 将内容写入指定的输出文件中
     * @param url
     * @param file
     */
    protected void getFile(String content, File file) {
    	if (file.exists())
    		file.delete();
    	try {
	        FileUtils.write(file, content);
        } catch (IOException e) {
	        logger.error(e.getMessage());
        }
    }

    protected void writeDirPathProperty(String basePath, String output, 
        PathProperty pathProperty) {
        String name = pathProperty.getFilename().getAbsolutePath().replaceFirst(
            basePath.toString(), "");
        name = StringUtils.stripStart(name, "/");
        File indexFile = new File(new File(output, name), "index.html");
        logger.debug(String.format("Output %s 's coverage infomation to %s",
            pathProperty.getFilename(), indexFile));
        if (!indexFile.getParentFile().exists())
            indexFile.getParentFile().mkdirs();

        TemplateFactory templateFactory = templateLoader.newTemplateFactory(
            directoryResultTemplateFile);
        Template template = templateFactory.createTemplate();
        template.setProperty("pathProperty", pathProperty);
        try {
            FileUtils.write(indexFile, template.asString());
        } catch (TemplateEvaluationException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    protected void writeFilePathProperty(String basePath, String output, 
        PathProperty pathProperty) {
        String name = pathProperty.getFilename().getAbsolutePath().replaceFirst(
            basePath.toString(), "");
        name = StringUtils.stripStart(name, "/");
        File indexFile = new File(output, name + ".html");
        logger.debug(String.format("Output %s 's coverage infomation to %s",
            pathProperty.getFilename(), indexFile));
        if (!indexFile.getParentFile().exists())
            indexFile.getParentFile().mkdirs();

        // 没有覆盖到的行
        List<Integer> uncoveragedLinesNumbers = Lists.newArrayList();
        for (LineProperty lineProperty: pathProperty.getLineProperties()) {
            if ((lineProperty.getCoveragedNum() <= 0) && (!lineProperty.isShouldIgnore()))
                uncoveragedLinesNumbers.add(lineProperty.getLineNumber());
        }

        TemplateFactory templateFactory = templateLoader.newTemplateFactory(
            fileResultTemplateFile);
        Template template = templateFactory.createTemplate();
        template.setProperty("pathProperty", pathProperty);
        template.setProperty("uncoveragedLinesNumbers", uncoveragedLinesNumbers.toString());
    	CharsetDetector charsetDetector = new CharsetDetector();
        try {
        	charsetDetector.setText(FileUtils.readFileToByteArray(
        	    pathProperty.getFilename()));
        	CharsetMatch charsetMatch = charsetDetector.detect();
            template.setProperty("fileContent",
                FileUtils.readFileToString(
                pathProperty.getFilename(), 
                charsetMatch.getName()).replace("<", "&lt;").replace(">", "&gt;"));
            FileUtils.write(indexFile, template.asString());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
    
    protected boolean isIgnoreByPattern(File file) {
    	if (configProperty.getIgnorePattern() == null) 
    		return false;
    	String filename = file.getName();
    	String parent = FileUtil.normalizePath(file.getParent());
    	for (String pattern: configProperty.getIgnorePattern().getIngoreFiles()) {
    		Pattern re = Pattern.compile(pattern);
    		Matcher matcher = re.matcher(filename);
    		if (matcher.find())
    			return true;
    	}
    	for (String pattern: configProperty.getIgnorePattern().getIgnoreDirs()) {
            Pattern re = Pattern.compile(pattern);
            Matcher matcher = re.matcher(filename);
            if (matcher.find())
                return true;
    	}
    	return false;
    }
}
