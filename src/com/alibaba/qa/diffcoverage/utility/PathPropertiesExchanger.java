package com.alibaba.qa.diffcoverage.utility;

import static com.google.common.collect.Collections2.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Setter;

import org.openide.filesystems.FileUtil;

import com.alibaba.qa.diffcoverage.model.BranchProperty;
import com.alibaba.qa.diffcoverage.model.FileProperty;
import com.alibaba.qa.diffcoverage.model.FunctionProperty;
import com.alibaba.qa.diffcoverage.model.LineProperty;
import com.alibaba.qa.diffcoverage.model.PathProperty;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PathPropertiesExchanger{
    private PercentChart percentChart = new PercentChart();
    private List<PathProperty> pathProperties = Lists.newArrayList();
    @Setter
    private String basePath = null;
    private List<String> dealedPathes = Lists.newArrayList();

    public List<PathProperty> fromFileProperties(
        List<FileProperty> fileProperties){
        List<String> files = findAllFiles(fileProperties);
        files.add(basePath.toString());
        files = new ArrayList<String>(Sets.newHashSet(files));
        for (String file : files){
            file = FileUtil.normalizePath(file);
            dealedPathes.add(file);
            if (new File(file).isDirectory())
                pathProperties.add(createDirPathProperty(file, fileProperties));
            else
                pathProperties.add(createFilePathProperty(file, fileProperties));
        }
        return pathProperties;
    }

    private PathProperty createFilePathProperty(final String file,
        List<FileProperty> fileProperties){
        PathProperty pathProperty = new PathProperty();
        pathProperty.setFilename(new File(file));
        for (FileProperty fileProperty : fileProperties){
            if (fileProperty == null)
                continue;
            if (!fileProperty.getFilename().equals(file))
                continue;
            pathProperty.setLinesNum(pathProperty.getLinesNum() +
                filterValidLines(fileProperty.getLines()).size());
            pathProperty.setCoveragedLinesNum(pathProperty
                .getCoveragedLinesNum() +
                filterCoveragedLines(fileProperty.getLines()).size());

            pathProperty.setBranchesNum(pathProperty.getBranchesNum() +
                fileProperty.getBranchProperty().size());
            pathProperty.setCoveragedBranchesNum(
                filterCoveragedBranches(fileProperty.getBranchProperty())
                    .size());

            pathProperty.setFunctionsNum(pathProperty.getFunctionsNum() +
                fileProperty.getFunctionProperties().size());
            pathProperty.setCoveragedFunctionsNum(
                filterCoveragedFunctions(fileProperty.getFunctionProperties())
                    .size());
            pathProperty.setLineProperties(fileProperty.getLines());
            pathProperty.setHtmlLink(fileProperty.getHtmlLink());
        }
        if (pathProperty.getLinesNum() != 0)
            pathProperty.setLinesCoveragePercent(
                ((double) pathProperty.getCoveragedLinesNum())
                    / (double) pathProperty.getLinesNum());
        if (pathProperty.getBranchesNum() != 0){
            pathProperty.setBranchesCoveragePercent(
                ((double) pathProperty.getCoveragedBranchesNum())
                    / (double) pathProperty.getBranchesNum());
        }
        if (pathProperty.getFunctionsNum() != 0)
            pathProperty.setFunctionsCoveragePercent(
                ((double) pathProperty.getCoveragedFunctionsNum())
                    / (double) pathProperty.getFunctionsNum());
        pathProperty.fillPercentsString();
        pathProperty
            .setCoveragePercentChart(
            percentChart.getChartUrl(pathProperty.getLinesCoveragePercent() * 100));
        return pathProperty;
    }

    private PathProperty createDirPathProperty(final String file,
        List<FileProperty> fileProperties){
        PathProperty pathProperty = new PathProperty();
        pathProperty.setFilename(new File(file));
        List<FileProperty> childrenFileProperties =
            Lists.newArrayList(filter(fileProperties,
                new Predicate<FileProperty>(){
                    @Override
                    public boolean apply(FileProperty fileProperty){
                        return fileProperty.getFilename().toString()
                            .startsWith(file);
                    }
                }));
        Collections.sort(childrenFileProperties,
            new Comparator<FileProperty>(){
                @Override
                public int compare(FileProperty o1, FileProperty o2){
                    return o1.getFilename().toString()
                        .compareTo(o2.getFilename().toString());
                }
            });
        for (FileProperty fileProperty : childrenFileProperties){
            pathProperty.setLinesNum(pathProperty.getLinesNum() +
                filterValidLines(fileProperty.getLines()).size());
            pathProperty.setCoveragedLinesNum(pathProperty
                .getCoveragedLinesNum() +
                filterCoveragedLines(fileProperty.getLines()).size());

            pathProperty.setBranchesNum(pathProperty.getBranchesNum() +
                fileProperty.getBranchProperty().size());
            pathProperty.setCoveragedBranchesNum(pathProperty
                .getCoveragedBranchesNum()
                +
                filterCoveragedBranches(fileProperty.getBranchProperty())
                    .size());

            pathProperty.setFunctionsNum(pathProperty.getFunctionsNum() +
                fileProperty.getFunctionProperties().size());
            pathProperty.setCoveragedFunctionsNum(pathProperty
                .getCoveragedFunctionsNum()
                +
                filterCoveragedFunctions(fileProperty.getFunctionProperties())
                    .size());
        }
        if (pathProperty.getLinesNum() != 0)
            pathProperty.setLinesCoveragePercent(
                ((double) pathProperty.getCoveragedLinesNum())
                    / (double) pathProperty.getLinesNum());
        if (pathProperty.getBranchesNum() != 0)
            pathProperty.setBranchesCoveragePercent(
                ((double) pathProperty.getCoveragedBranchesNum())
                    / (double) pathProperty.getBranchesNum());
        if (pathProperty.getFunctionsNum() != 0)
            pathProperty.setFunctionsCoveragePercent(
                ((double) pathProperty.getCoveragedFunctionsNum())
                    / (double) pathProperty.getFunctionsNum());

        pathProperty.setHtmlLink(pathProperty.getFilename().getName()
            + "/index.html");

        childrenFileProperties = Lists.newArrayList(filter(fileProperties,
            new Predicate<FileProperty>(){
                @Override
                public boolean apply(FileProperty fileProperty){
                    return new File(fileProperty.getFilename()).getParent()
                        .equals(new File(file).toString());
                }
            }));
        for (FileProperty fileProperty : childrenFileProperties){
            if (new File(fileProperty.getFilename()).isDirectory())
                pathProperty.getChildrenPathProperties().add(
                    createDirPathProperty(
                        fileProperty.getFilename().toString(), fileProperties));
            else
                pathProperty.getChildrenPathProperties().add(
                    createFilePathProperty(fileProperty.getFilename()
                        .toString(), fileProperties));
        }
        for (File childFile : new File(file).listFiles()){
            if (!childFile.isDirectory())
                continue;
            boolean flag = false;
            for (FileProperty fileProperty : fileProperties){
                if (FileUtil.normalizePath(fileProperty.getFilename())
                    .startsWith(
                        FileUtil.normalizeFile(childFile).toString() + "/")){
                    flag = true;
                    break;
                }
            }
            if (!flag)
                continue;
            pathProperty.getChildrenPathProperties().add(
                createDirPathProperty(childFile.toString(), fileProperties));
        }

        pathProperty.fillPercentsString();
        pathProperty
            .setCoveragePercentChart(
            percentChart.getChartUrl(pathProperty.getLinesCoveragePercent() * 100));
        // 看看它的目录有没有处理过
        File parentFile = pathProperty.getFilename().getParentFile()
            .getAbsoluteFile();
        boolean flag = false;
        for (String path : dealedPathes){
            if (path.equals(parentFile.toString()))
                flag = true;
        }
        if ((!flag) && (!parentFile.toString().equals(basePath.toString())) &&
            (parentFile.toString().startsWith(basePath.toString()))){
            dealedPathes.add(parentFile.toString());
            PathProperty pathProperty2 = createDirPathProperty(
                parentFile.toString(), fileProperties);
            pathProperties.add(pathProperty2);
        }
        return pathProperty;
    }

    private List<BranchProperty> filterCoveragedBranches(
        List<BranchProperty> branchProperties){
        return Lists.newArrayList(filter(branchProperties,
            new Predicate<BranchProperty>(){

                @Override
                public boolean apply(BranchProperty branchProperty){
                    return branchProperty.isCoveraged();
                }
            }));
    }

    private List<FunctionProperty> filterCoveragedFunctions(
        List<FunctionProperty> functionProperties){
        return Lists.newArrayList(filter(functionProperties,
            new Predicate<FunctionProperty>(){
                @Override
                public boolean apply(FunctionProperty functionProperty){
                    return functionProperty.isCoveraged();
                }
            }));
    }

    private List<LineProperty> filterCoveragedLines(List<LineProperty> lines){
        return Lists.newArrayList(filter(lines, new Predicate<LineProperty>(){
            @Override
            public boolean apply(LineProperty lineProperty){
                return (lineProperty.getCoveragedNum() > 0) &&
                    (!lineProperty.isShouldIgnore());
            }
        }));
    }

    private List<LineProperty> filterValidLines(List<LineProperty> lines){
        return Lists.newArrayList(filter(lines, new Predicate<LineProperty>(){

            @Override
            public boolean apply(LineProperty lineProperty){
                return !lineProperty.isShouldIgnore();
            }
        }));
    }

    /**
     * 找出所有的文件和目录
     * 
     * @param fileProperties
     * @return
     */
    private List<String> findAllFiles(List<FileProperty> fileProperties){
        List<String> files = Lists.newArrayList();
        for (FileProperty fileProperty : fileProperties){
            if (fileProperty == null)
                continue;
            if (files.contains(fileProperty.getFilename()))
                continue;
            files.add(fileProperty.getFilename().toString());
            String parentPath = new File(fileProperty.getFilename())
                .getParent();
            if (files.contains(parentPath))
                continue;
            files.add(parentPath);
        }
        return files;
    }
}
