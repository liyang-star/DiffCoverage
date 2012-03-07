package com.alibaba.qa.astanalyzer.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

/**
 * 类申明的解析访问器
 * @author wul
 *
 */
public class ClassDeclarationsVisitor extends ASTVisitor {
    private List<ICPPASTCompositeTypeSpecifier> classDeclarations =
        new ArrayList<ICPPASTCompositeTypeSpecifier>();

    public ClassDeclarationsVisitor() {
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

        classDeclarations.clear();
    }

    public int visit(IASTDeclaration astDeclaration) {
        if (astDeclaration instanceof IASTSimpleDeclaration) {
            ICPPASTCompositeTypeSpecifier compositeTypeSpecifier = null;
            IASTDeclSpecifier astDeclarationSpecifier =
                ((IASTSimpleDeclaration) astDeclaration).getDeclSpecifier();
            if (astDeclarationSpecifier instanceof
                ICPPASTCompositeTypeSpecifier) {
                compositeTypeSpecifier =
                    (ICPPASTCompositeTypeSpecifier) astDeclarationSpecifier;
                classDeclarations.add(compositeTypeSpecifier);
            }
        }
        return PROCESS_CONTINUE;
    }

    public List<ICPPASTCompositeTypeSpecifier> getClassDeclarations() {
        return classDeclarations;
    }

}
