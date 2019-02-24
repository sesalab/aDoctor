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
    public UndoEdit applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException {
        if (methodProposal == null) {
            return null;
        }
        if (!(methodProposal instanceof DWProposal)) {
            return null;
        }
        DWProposal dwProposal = (DWProposal) methodProposal;

        // Required MethodDeclarations
        MethodDeclaration smellyMethodDecl = dwProposal.getMethodSmell().getMethod().getMethodDecl();
        MethodDeclaration proposedMethodDecl = dwProposal.getProposedMethodDecl();
        if (smellyMethodDecl == null || proposedMethodDecl == null) {
            return null;
        }

        // Creation of the rewriter
        CompilationUnit compilationUnit = (CompilationUnit) smellyMethodDecl.getRoot();
        ASTRewrite astRewrite = ASTRewrite.create(compilationUnit.getAST());
        // Accumulate the replacements
        astRewrite.replace(smellyMethodDecl, proposedMethodDecl, null);

        return rewriteFile(methodProposal, astRewrite);
    }
}