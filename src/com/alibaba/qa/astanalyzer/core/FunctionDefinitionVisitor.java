package com.alibaba.qa.astanalyzer.core;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import com.google.common.collect.Lists;

/**
 * 用于寻找C/C++中所有函数定义的访问器
 * @author wul
 *
 */
public class FunctionDefinitionVisitor extends ASTVisitor{
    private List<IASTFunctionDefinition> functionDefinitions = Lists.newArrayList();

    public FunctionDefinitionVisitor() {
        super.shouldVisitAmbiguousNodes = true;
        super.shouldVisitArrayModifiers = true;
        super.shouldVisitBaseSpecifiers = true;
        super.shouldVisitDeclarations = true;
        super.shouldVisitDeclarators = true;
        super.shouldVisitDeclSpecifiers = true;
        super.shouldVisitDesignators = true;
        super.shouldVisitEnumerators = true;
        super.shouldVisitExpressions = true;
        super.shouldVisitImplicitNameAlternates = true;
        super.shouldVisitImplicitNames = true;
        super.shouldVisitInitializers = true;
        super.shouldVisitNames = true;
        super.shouldVisitNamespaces = true;
        super.shouldVisitParameterDeclarations = true;
        super.shouldVisitPointerOperators = true;
        super.shouldVisitProblems = true;
        super.shouldVisitStatements = true;
        super.shouldVisitTemplateParameters = true;
        super.shouldVisitTranslationUnit = true;
        super.shouldVisitTypeIds = true;
    }

    public int visit(IASTDeclaration astDeclaration) {
        if (astDeclaration instanceof IASTFunctionDefinition) {
            functionDefinitions.add((IASTFunctionDefinition) astDeclaration);
        }
        return PROCESS_CONTINUE;
    }

    public List<IASTFunctionDefinition> getFunctionDefinitions() {
        return functionDefinitions;
    }
}
