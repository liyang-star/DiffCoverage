package com.alibaba.qa.diffcoverage.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.javatuples.Triplet;
import org.openide.filesystems.FileUtil;

import com.alibaba.qa.astanalyzer.core.ASTTranslationUnitCore;
import com.alibaba.qa.astanalyzer.core.FunctionDefinitionVisitor;
import com.alibaba.qa.diffcoverage.model.ASTFileLocation;
import com.google.common.collect.Lists;

public class NormalDiffFormatParser extends AbstractDiffParser {

	public NormalDiffFormatParser(String basePath) {
	    super(basePath);
    }

	@Override
	public List<ASTFileLocation> parse(String diffStr) {
		List<ASTFileLocation> diffFunctionProperties = Lists.newArrayList();
		List<Triplet<String, Integer, Integer>> files = splitFiles(diffStr);
		for (Triplet<String, Integer, Integer> info: files) {
			File file = new File(basePath, info.getValue0());
			if (!file.exists()) {
			    continue;
			}
			file = FileUtil.normalizeFile(file);
			List<Integer> rows = Lists.newArrayList();
			for (int i=info.getValue1().intValue(); i <= info.getValue2().intValue(); ++i)
				rows.add(i);
			IASTTranslationUnit translationUnit = ASTTranslationUnitCore.parse(file);
			FunctionDefinitionVisitor functionDefinitionVisitor = 
			    new FunctionDefinitionVisitor();
			translationUnit.accept(functionDefinitionVisitor);
			for (IASTFunctionDefinition functionDefinition: 
			    functionDefinitionVisitor.getFunctionDefinitions()) {
				List<Integer> functionRows = Lists.newArrayList();
				for (int i=functionDefinition.getFileLocation().getStartingLineNumber();
					i<=functionDefinition.getFileLocation().getEndingLineNumber();
					++i)
					functionRows.add(i);
				functionRows.retainAll(rows);
				if (functionRows.size() <= 0)
					continue;
				boolean flag = false;
				for (ASTFileLocation diffFileLocation: diffFunctionProperties) {
					if ((diffFileLocation.getStartingLineNumber() == 
					    functionDefinition.getFileLocation().getStartingLineNumber()) &&
						(diffFileLocation.getEndingLineNumber() == 
						functionDefinition.getFileLocation().getEndingLineNumber()))
						flag = true;
				}
				if (flag)
					continue;
				ASTFileLocation fileLocation = new ASTFileLocation();
				fileLocation.setFilename(
				    functionDefinition.getFileLocation().getFileName());
				fileLocation.setStartingLineNumber(
				    functionDefinition.getFileLocation().getStartingLineNumber());
				fileLocation.setEndingLineNumber(
				    functionDefinition.getFileLocation().getEndingLineNumber());
				diffFunctionProperties.add(fileLocation);
			}
		}
		
		return diffFunctionProperties;
	}

	private List<Triplet<String, Integer, Integer>> splitFiles(String diffStr) {
		List<Triplet<String, Integer, Integer>> files = Lists.newArrayList();
		diffStr = diffStr.replaceAll(splitLine, "");
		String[] lines = diffStr.split("\n");
		if (lines == null)
			return files;
		String filename = null;
		for (String line: lines) {
			Pattern pattern = Pattern.compile("^Index:\\s+(.*?)");
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				filename = line.replaceFirst("^Index:\\s+", "");
				continue;
			}
			pattern = Pattern.compile("^\\d+\\,{0,1}\\d{0,1}[a|d|c](\\d+),(\\d+)");
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				files.add(
					new Triplet<String, Integer, Integer>(new String(filename), 
					Integer.valueOf(matcher.group(1)), 
					Integer.valueOf(matcher.group(2))));
				continue;
			}
			pattern = Pattern.compile("^\\d+\\,{0,1}\\d{0,1}[a|d|c](\\d+)");
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				files.add(
					new Triplet<String, Integer, Integer>(new String(filename), 
	                    Integer.valueOf(matcher.group(1)), 
	                    Integer.valueOf(matcher.group(1))));
				continue;
			}
		}
		
		return files;
	}
	
    public static void main(String[] args) throws IOException{
        String basePath = "/home/admin/20120502_113645_opt-en2_2";
        String diffFile = "/home/admin/diff.txt";
        NormalDiffFormatParser parser = new NormalDiffFormatParser(basePath);
        List<ASTFileLocation> fileLocations =
            parser.parse(FileUtils.readFileToString(new File(diffFile)));
        for (ASTFileLocation fileLocation : fileLocations){
            System.out.println(fileLocation);
        }
    }
	
}
