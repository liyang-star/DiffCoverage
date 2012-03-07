package com.alibaba.qa.diffcoverage.parser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

import org.apache.regexp.RE;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.openide.filesystems.FileUtil;

import com.alibaba.qa.astanalyzer.core.ASTTranslationUnitCore;
import com.alibaba.qa.astanalyzer.core.FunctionDefinitionVisitor;
import com.alibaba.qa.diffcoverage.model.ASTFileLocation;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 解析统一差异格式
 * @author garcia.wul@alibaba-inc.com
 *
 */
public class UnifiedDiffFormatParser extends AbstractDiffParser {	
	public UnifiedDiffFormatParser(String basePath) {
	    super(basePath);
    }

	public List<ASTFileLocation> parse(String diffStr) {
		Map<String, String> splitedFiles = splitFiles(diffStr);
		List<ASTFileLocation> fileLocations = Lists.newArrayList();
		for (Entry<String, String> entry: splitedFiles.entrySet()) {
			File file = new File(basePath, entry.getKey());
			if (!file.exists()) {
				logger.error(String.format("%s does not exist", file));
				continue;
			}
			file = FileUtil.normalizeFile(file);
			fileLocations.addAll(getDiffFunctionFileLocations(file, entry.getValue()));
		}
		return fileLocations;
	}

	protected List<ASTFileLocation> getDiffFunctionFileLocations(File file, String text) {
		List<ASTFileLocation> diffFunctionProperties = Lists.newArrayList();
		IASTTranslationUnit translationUnit = ASTTranslationUnitCore.parse(file);
		FunctionDefinitionVisitor functionDefinitionVisitor = new FunctionDefinitionVisitor();
		translationUnit.accept(functionDefinitionVisitor);
		List<IASTFunctionDefinition> functionDefinitions = functionDefinitionVisitor.getFunctionDefinitions();

		Set<Integer> numbers = Sets.newHashSet();
		diff_match_patch differ = new diff_match_patch();
		text = replaceForDiffer(text);
		List<Patch> patchs = differ.patch_fromText(text);
		for (Patch patch: patchs) {
			int diffStartingLineNumber = patch.start2 <= 0 ? 1 : patch.start2 + 1;
			List<Diff> diffs = patch.diffs;
			int counter = 0;
			for (Diff diff: diffs) {
				if (diff.operation != Operation.DELETE)
					++ counter;
				if (diff.operation != Operation.INSERT)
					continue;
				int index = getFunctionIndex(diffStartingLineNumber + counter, functionDefinitions);
				if (index == -1)
					continue;
				numbers.add(index);
			}
		}
		for (Integer index: numbers) {
		    IASTFileLocation astFileLocation = functionDefinitions.get(index.intValue()).getFileLocation();
		    ASTFileLocation fileLocation = new ASTFileLocation();
		    fileLocation.setEndingLineNumber(astFileLocation.getEndingLineNumber());
		    fileLocation.setFilename(astFileLocation.getFileName());
		    fileLocation.setStartingLineNumber(astFileLocation.getStartingLineNumber());
			diffFunctionProperties.add(fileLocation);
		}

		return diffFunctionProperties;
	}

	private String replaceForDiffer(String str) {
		return str.replaceAll("%", "<percentage>");
	}

	private int getFunctionIndex(int number, List<IASTFunctionDefinition> functionDefinitions) {
		int counter = 0;
		for (IASTFunctionDefinition functionDefinition: functionDefinitions) {
			++ counter ;
			if ((number >= functionDefinition.getFileLocation().getStartingLineNumber()) &&
				(number <= functionDefinition.getFileLocation().getEndingLineNumber()))
				return counter - 1;
		}
		return -1;
	}


	protected Map<String, String> splitFiles(String diffStr) {
		Map<String, String> files = Maps.newHashMap();
		String regexPattern = 
			String.format("Index:\\s+(.*?)\n%s\n\\-\\-\\-\\s+.*?\\s+\\(.*?\\)\n", splitLine);
		RE pattern = new RE(regexPattern, RE.MATCH_MULTILINE);
		String[] fields = pattern.split(diffStr);
		if (fields == null) {
			logger.warn("The diff string is not splited by regex pattern");
			return files;
		}
		for (String field: fields) {
			field = field.trim();
			if (field.equals(""))
				continue;
			RE pattern2 = new RE("^\\+\\+\\+\\s+(.*?)\\s+.*?\n", RE.MATCH_MULTILINE);
			boolean isMatched = pattern2.match(field);
			if (!isMatched) {
				logger.warn(String.format("Can not get filename from diff"));
				continue;
			}
			String filename = pattern2.getParen(1);
			field = field.replace(pattern2.getParen(0), "");
			String[] lines = field.split("\n");
			if (lines == null) {
				continue;
			}
			StringBuilder stringBuilder = new StringBuilder();
			for (int i=0; i < lines.length; ++i)
				stringBuilder.append(lines[i] + "\n");
			files.put(filename, stringBuilder.toString());
		}
		return files;
	}
}
