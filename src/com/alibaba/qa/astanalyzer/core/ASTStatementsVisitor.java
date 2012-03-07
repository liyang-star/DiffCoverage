package com.alibaba.qa.astanalyzer.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class ASTStatementsVisitor extends ASTVisitor {
    private List<IASTStatement> statements =
        new ArrayList<IASTStatement>();

    public ASTStatementsVisitor() {
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

        statements.clear();
    }

    public int visit(IASTStatement statement) {
        statements.add(statement);
        return PROCESS_CONTINUE;
    }

    public List<IASTStatement> getStatements() {
        return statements;
    }
}
