package com.alibaba.qa.diffcoverage.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

import org.apache.commons.io.FileUtils;
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
 * 
 * @author garcia.relax@gmail.com
 * 
 */
public class UnifiedDiffFormatParser extends AbstractDiffParser{
    public UnifiedDiffFormatParser(String basePath){
        super(basePath);
    }

    /**
     * 解析Diff字符串,返回各个有差异的函数信息
     */
    public List<ASTFileLocation> parse(String diffStr){
        Map<String, String> splitedFiles = splitFiles(diffStr);
        List<ASTFileLocation> fileLocations = Lists.newArrayList();
        for (Entry<String, String> entry : splitedFiles.entrySet()){
            File file = new File(basePath, entry.getKey());
            if (!file.exists()){
                continue;
            }
            file = FileUtil.normalizeFile(file);
            fileLocations.addAll(getDiffFunctionFileLocations(file,
                entry.getValue()));
        }
        return fileLocations;
    }

    protected List<ASTFileLocation> getDiffFunctionFileLocations(
        File file, String text){
        List<ASTFileLocation> diffFunctionProperties = Lists.newArrayList();
        IASTTranslationUnit translationUnit = 
            ASTTranslationUnitCore.parse(file);
        FunctionDefinitionVisitor functionDefinitionVisitor = 
            new FunctionDefinitionVisitor();
        translationUnit.accept(functionDefinitionVisitor);
        List<IASTFunctionDefinition> functionDefinitions = 
            functionDefinitionVisitor.getFunctionDefinitions();

        Set<Integer> numbers = Sets.newHashSet();
        diff_match_patch differ = new diff_match_patch();
        text = replaceForDiffer(text);
        List<Patch> patchs = differ.patch_fromText(text);
        for (Patch patch : patchs){
            int diffStartingLineNumber = patch.start2 <= 0 ? 1
                : patch.start2 + 1;
            List<Diff> diffs = patch.diffs;
            int counter = 0;
            for (Diff diff : diffs){
                if (diff.operation != Operation.DELETE)
                    ++counter;
                if (diff.operation != Operation.INSERT)
                    continue;
                int index = getFunctionIndex(diffStartingLineNumber + counter,
                    functionDefinitions);
                if (index == -1)
                    continue;
                numbers.add(index);
            }
        }
        for (Integer index : numbers){
            IASTFileLocation astFileLocation = functionDefinitions.get(
                index.intValue()).getFileLocation();
            ASTFileLocation fileLocation = new ASTFileLocation();
            fileLocation.setEndingLineNumber(astFileLocation
                .getEndingLineNumber());
            fileLocation.setFilename(astFileLocation.getFileName());
            fileLocation.setStartingLineNumber(astFileLocation
                .getStartingLineNumber());
            diffFunctionProperties.add(fileLocation);
        }

        return diffFunctionProperties;
    }

    private String replaceForDiffer(String str){
        return str.replaceAll("%", "<percentage>");
    }

    private int getFunctionIndex(int number,
        List<IASTFunctionDefinition> functionDefinitions){
        int counter = 0;
        for (IASTFunctionDefinition functionDefinition : functionDefinitions){
            ++counter;
            if ((number >= functionDefinition.getFileLocation().getStartingLineNumber())
                &&
                (number <= functionDefinition.getFileLocation().getEndingLineNumber()))
                return counter - 1;
        }
        return -1;
    }

    protected Map<String, String> splitFiles(String diffStr){
        Map<String, String> files = Maps.newHashMap();
        String regexPattern =
            String.format(
                "Index:\\s+(.*?)\n%s\n\\-\\-\\-\\s+.*?\\s+\\(.*?\\)\n",
                splitLine);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.MULTILINE);
        String[] fields = pattern.split(diffStr);
        if (fields == null){
            return files;
        }
        for (String field : fields){
            field = field.trim();
            if (field.equals(""))
                continue;
            Pattern pattern2 = Pattern.compile("^\\+\\+\\+\\s+(.*?)\\s+.*?\n",
                Pattern.MULTILINE);
            Matcher matcher = pattern2.matcher(field);
            if (!matcher.find()){
                continue;
            }
            String filename = matcher.group(1);
            field = field.replace(matcher.group(0), "");
            String[] lines = field.split("\n");
            if (lines == null)
                continue;
            
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < lines.length; ++i)
                stringBuilder.append(lines[i] + "\n");
            files.put(filename, stringBuilder.toString());
        }
        return files;
    }

    public static void main(String[] args) throws IOException{
        String basePath = "/home/admin/isearch_4_2_5_yc_iquery_D_20120322";
        String diffFile = "/home/admin/1.diff";
        UnifiedDiffFormatParser parser = new UnifiedDiffFormatParser(basePath);
        List<ASTFileLocation> fileLocations =
            parser.parse(FileUtils.readFileToString(new File(diffFile)));
        for (ASTFileLocation fileLocation : fileLocations){
            System.out.println(fileLocation);
        }
    }
}
