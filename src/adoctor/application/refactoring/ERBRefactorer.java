package adoctor.application.refactoring;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.proposal.ERBProposal;
import adoctor.application.bean.proposal.MethodProposal;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.UndoEdit;

import java.io.IOException;

public class ERBRefactorer extends MethodSmellRefactorer {

    public ERBRefactorer() {

    }

    @Override
    public boolean applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException {
        if (!(methodProposal instanceof ERBProposal)) {
            return false;
        }
        ERBProposal erbProposal = (ERBProposal) methodProposal;
        MethodDeclaration proposedOnCreate = erbProposal.getProposedOnCreate();
        MethodDeclaration actualOnResume = erbProposal.getActualOnResume();
        MethodDeclaration proposedOnResume = erbProposal.getProposedOnResume();

        CompilationUnit compilationUnit = getCompilationUnit(methodProposal);
        // MethodDeclaration to be replaced
        MethodDeclaration targetOnCreate = ASTUtilities.getMethodDeclarationFromContent(methodProposal.getMethodSmell().getMethodBean().getTextContent(), compilationUnit);
        if (targetOnCreate == null) {
            return false;
        }

        // Accumulate the replacements
        ASTRewrite rewriter = ASTRewrite.create(compilationUnit.getAST());
        rewriter.replace(targetOnCreate, proposedOnCreate, null); // Replaces the onCreate(Bundle)

        if (actualOnResume == null) {
            // Insert the onResume() after the onCreate(Bundle)
            ListRewrite listRewrite = rewriter.getListRewrite((TypeDeclaration) compilationUnit.types().get(0), TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
            listRewrite.insertAfter(proposedOnResume, proposedOnCreate, null);
        } else {
            // Replaces the current onResume() with the new one
            MethodDeclaration targetOnResume = ASTUtilities.getMethodDeclarationFromContent(actualOnResume.toString(), compilationUnit);
            if (targetOnResume == null) {
                return false;
            } else {
                rewriter.replace(targetOnResume, proposedOnResume, null);
            }
        }

        UndoEdit undoEdit = rewriteFile(methodProposal, rewriter);
        //TODO Eliminare
        if (undoEdit != null) {
            return true;
        } else {
            return false;
        }
    }
}
