package adoctor.application.refactoring;

import adoctor.application.bean.proposal.DWProposal;
import adoctor.application.bean.proposal.MethodProposal;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.UndoEdit;

import java.io.IOException;

public class DWRefactorer extends MethodSmellRefactorer {

    @Override
    public boolean applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException {
        if (methodProposal == null) {
            return false;
        }
        if (!(methodProposal instanceof DWProposal)) {
            return false;
        }
        DWProposal dwProposal = (DWProposal) methodProposal;

        // Required MethodDeclarations
        MethodDeclaration smellyMethodDecl = dwProposal.getMethodSmell().getMethod().getMethodDecl();
        MethodDeclaration proposedMethodDecl = dwProposal.getProposedMethodDecl();
        if (smellyMethodDecl == null || proposedMethodDecl == null) {
            return false;
        }

        // Creation of the rewriter
        CompilationUnit compilationUnit = (CompilationUnit) smellyMethodDecl.getRoot();
        ASTRewrite astRewrite = ASTRewrite.create(compilationUnit.getAST());
        // Accumulate the replacements
        astRewrite.replace(smellyMethodDecl, proposedMethodDecl, null);

        UndoEdit undoEdit = rewriteFile(methodProposal, astRewrite);
        //TODO Eliminare
        if (undoEdit != null) {
            return true;
        } else {
            return false;
        }
    }
}