package adoctor.application.refactoring;

import adoctor.application.bean.proposal.IDSProposal;
import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.IDSSmell;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.UndoEdit;

import java.io.IOException;

public class IDSRefactorer extends MethodSmellRefactorer {
    @Override
    public boolean applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException {
        if (methodProposal == null) {
            return false;
        }
        if (!(methodProposal instanceof IDSProposal)) {
            return false;
        }
        IDSProposal idsProposal = (IDSProposal) methodProposal;

        VariableDeclarationStatement smellyVarDecl = ((IDSSmell) idsProposal.getMethodSmell()).getSmellyVarDecl();
        VariableDeclarationStatement proposedVarDecl = idsProposal.getProposedVarDecl();
        if (smellyVarDecl == null || proposedVarDecl == null) {
            return false;
        }

        // Creation of the rewriter
        CompilationUnit compilationUnit = (CompilationUnit) smellyVarDecl.getRoot();
        ASTRewrite astRewrite = ASTRewrite.create(compilationUnit.getAST());
        // Accumulate the replacements
        astRewrite.replace(smellyVarDecl, proposedVarDecl, null);
        //TODO Gli altri replace

        UndoEdit undoEdit = rewriteFile(methodProposal, astRewrite);
        //TODO Eliminare
        if (undoEdit != null) {
            return true;
        } else {
            return false;
        }
    }
}
