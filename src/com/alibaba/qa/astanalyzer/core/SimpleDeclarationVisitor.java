package com.alibaba.qa.astanalyzer.core;

import java.util.List;

import lombok.Getter;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

import com.google.common.collect.Lists;

public class SimpleDeclarationVisitor extends ASTVisitor {
	@Getter
	private List<IASTSimpleDeclaration> simpleDeclarations = Lists.newArrayList();
	
    public SimpleDeclarationVisitor() {
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
    	if (astDeclaration instanceof CPPASTSimpleDeclaration) {
            IASTDeclarator[] declarators =
                ((IASTSimpleDeclaration) astDeclaration).getDeclarators();
            for (IASTDeclarator astDeclarator: declarators) {
                if (astDeclarator.getName().resolveBinding() instanceof
                    ICPPVariable) {
                	simpleDeclarations.add(
                        (CPPASTSimpleDeclaration) astDeclaration);
                    break;
                }
            }
        }
        return PROCESS_CONTINUE;
    }
}
