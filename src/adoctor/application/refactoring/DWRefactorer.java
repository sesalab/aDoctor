package adoctor.application.refactoring;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.proposal.DWProposal;
import adoctor.application.bean.proposal.MethodProposal;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.UndoEdit;

import java.io.IOException;

public class DWRefactorer extends MethodSmellRefactorer {

    public DWRefactorer() {

    }

    @Override
    public boolean applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException {
        if (!(methodProposal instanceof DWProposal)) {
            return false;
        }
        DWProposal dwProposal = (DWProposal) methodProposal;
        MethodDeclaration proposedMethod = dwProposal.getProposedMethodDeclaration();

        CompilationUnit compilationUnit = getCompilationUnit(methodProposal);
        if (compilationUnit == null) {
            return false;
        }
        // MethodDeclaration to be replaced
        MethodDeclaration targetMethod = ASTUtilities.getMethodDeclarationFromContent(compilationUnit, methodProposal.getMethodSmell().getMethod().getLegacyMethodBean().getTextContent());
        if (targetMethod == null) {
            return false;
        }

        // Accumulate the replacements
        ASTRewrite astRewrite = ASTRewrite.create(compilationUnit.getAST());
        astRewrite.replace(targetMethod, proposedMethod, null);

        UndoEdit undoEdit = rewriteFile(methodProposal, astRewrite);
        //TODO Eliminare
        if (undoEdit != null) {
            return true;
        } else {
            return false;
        }
    }
}