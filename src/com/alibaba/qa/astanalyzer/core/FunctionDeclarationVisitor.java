package com.alibaba.qa.astanalyzer.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

/**
 * 解析函数申明时的访问器
 * @author wul
 *
 */
public class FunctionDeclarationVisitor extends ASTVisitor {
    private List<CPPASTSimpleDeclaration> functionDeclarations =
        new ArrayList<CPPASTSimpleDeclaration>();

    public FunctionDeclarationVisitor() {
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

        functionDeclarations.clear();
    }

    public int visit(IASTDeclaration astDeclaration) {
        if (astDeclaration instanceof CPPASTSimpleDeclaration) {
            IASTDeclarator[] declarators =
                ((IASTSimpleDeclaration) astDeclaration).getDeclarators();
            for (IASTDeclarator astDeclarator: declarators) {
                if (astDeclarator.getName().resolveBinding() instanceof
                    ICPPFunction) {
                    functionDeclarations.add(
                        (CPPASTSimpleDeclaration) astDeclaration);
                    break;
                }
            }
        }

        return PROCESS_CONTINUE;
    }

    public List<CPPASTSimpleDeclaration> getFunctionDeclarations() {
        return functionDeclarations;
    }

    public void setFunctionDeclarations(
            List<CPPASTSimpleDeclaration> functionDeclarations) {
        this.functionDeclarations = functionDeclarations;
    }
}
