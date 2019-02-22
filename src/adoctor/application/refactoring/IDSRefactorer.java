package adoctor.application.refactoring;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.proposal.IDSProposal;
import adoctor.application.bean.proposal.MethodProposal;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.UndoEdit;

import java.io.IOException;

public class IDSRefactorer extends MethodSmellRefactorer {
    @Override
    public boolean applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException {
        // Preconditions check
        if (!(methodProposal instanceof IDSProposal)) {
            return false;
        }
        IDSProposal idsProposal = (IDSProposal) methodProposal;
        VariableDeclarationStatement proposedVarDecl = idsProposal.getProposedVarDecl();
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
        //astRewrite.replace(targetMethod, proposedMethod, null);

        UndoEdit undoEdit = rewriteFile(methodProposal, astRewrite);
        //TODO Eliminare
        if (undoEdit != null) {
            return true;
        } else {
            return false;
        }
    }
}
