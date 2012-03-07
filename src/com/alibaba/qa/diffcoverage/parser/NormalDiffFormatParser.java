package com.alibaba.qa.diffcoverage.parser;

import java.io.File;
import java.util.List;

import org.apache.regexp.RE;
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
			file = FileUtil.normalizeFile(file);
			if (!file.exists()) {
				logger.warn(String.format("%s does not exist", file));
				continue;
			}
			List<Integer> rows = Lists.newArrayList();
			for (int i=info.getValue1().intValue(); i <= info.getValue2().intValue(); ++i)
				rows.add(i);
			IASTTranslationUnit translationUnit = ASTTranslationUnitCore.parse(file);
			FunctionDefinitionVisitor functionDefinitionVisitor = new FunctionDefinitionVisitor();
			translationUnit.accept(functionDefinitionVisitor);
			for (IASTFunctionDefinition functionDefinition: functionDefinitionVisitor.getFunctionDefinitions()) {
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
					if ((diffFileLocation.getStartingLineNumber() == functionDefinition.getFileLocation().getStartingLineNumber()) &&
						(diffFileLocation.getEndingLineNumber() == functionDefinition.getFileLocation().getEndingLineNumber()))
						flag = true;
				}
				if (flag)
					continue;
				ASTFileLocation fileLocation = new ASTFileLocation();
				fileLocation.setFilename(functionDefinition.getFileLocation().getFileName());
				fileLocation.setStartingLineNumber(functionDefinition.getFileLocation().getStartingLineNumber());
				fileLocation.setEndingLineNumber(functionDefinition.getFileLocation().getEndingLineNumber());
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
			RE pattern = new RE("^Index:\\s+(.*?)");
			boolean isMatched = pattern.match(line);
			if (isMatched) {
				filename = line.replaceFirst("^Index:\\s+", "");
				continue;
			}
			RE pattern1 = new RE("^.*[a|d|c](\\d+),(\\d+)");
			isMatched = pattern1.match(line);
			if (isMatched) {
				files.add(
					new Triplet<String, Integer, Integer>(new String(filename), 
					Integer.valueOf(pattern1.getParen(1)), 
					Integer.valueOf(pattern1.getParen(2))));
				continue;
			}
			RE pattern2 = new RE("^.*[a|d|c](\\d+)");
			isMatched = pattern2.match(line);
			if (isMatched) {
				files.add(
					new Triplet<String, Integer, Integer>(new String(filename), 
					Integer.valueOf(pattern2.getParen(1)), 
					Integer.valueOf(pattern2.getParen(1))));
				continue;
			}
		}
		
		return files;
	}
	
}
