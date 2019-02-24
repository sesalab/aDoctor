package adoctor.application.refactoring;

import adoctor.application.bean.proposal.IDSProposal;
import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.IDSSmell;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.UndoEdit;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;

public class IDSRefactorer extends MethodSmellRefactorer {
    @Override
    public UndoEdit applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException {
        if (methodProposal == null) {
            return null;
        }
        if (!(methodProposal instanceof IDSProposal)) {
            return null;
        }
        IDSProposal idsProposal = (IDSProposal) methodProposal;

        VariableDeclarationStatement smellyVarDecl = ((IDSSmell) idsProposal.getMethodSmell()).getSmellyVarDecl();
        VariableDeclarationStatement proposedVarDecl = idsProposal.getProposedVarDecl();
        if (smellyVarDecl == null || proposedVarDecl == null) {
            return null;
        }

        // Creation of the rewriter
        CompilationUnit compilationUnit = (CompilationUnit) smellyVarDecl.getRoot();
        ASTRewrite astRewrite = ASTRewrite.create(compilationUnit.getAST());

        // Accumulate the replacements
        astRewrite.replace(smellyVarDecl, proposedVarDecl, null);
        List<AbstractMap.SimpleEntry<MethodInvocation, Expression>> invocationReplacements = idsProposal.getInvocationReplacements();
        for (AbstractMap.SimpleEntry<MethodInvocation, Expression> invocationReplacement : invocationReplacements) {
            astRewrite.replace(invocationReplacement.getKey(), invocationReplacement.getValue(), null);
        }
        ImportDeclaration newImportDecl = idsProposal.getNewImportDecl();
        if (newImportDecl != null) {
            ListRewrite listRewrite = astRewrite.getListRewrite(compilationUnit, CompilationUnit.IMPORTS_PROPERTY);
            listRewrite.insertLast(newImportDecl, null);
        }

        return rewriteFile(methodProposal, astRewrite);
    }
}
