package adoctor.application.refactoring;

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

    @Override
    public UndoEdit applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException {
        if (methodProposal == null) {
            return null;
        }
        if (!(methodProposal instanceof ERBProposal)) {
            return null;
        }
        ERBProposal erbProposal = (ERBProposal) methodProposal;

        MethodDeclaration currentOnCreate = erbProposal.getMethodSmell().getMethod().getMethodDecl();
        MethodDeclaration proposedOnCreate = erbProposal.getProposedOnCreate();
        MethodDeclaration currentOnResume = erbProposal.getCurrentOnResume();
        MethodDeclaration proposedOnResume = erbProposal.getProposedOnResume();
        if (currentOnCreate == null || proposedOnCreate == null || proposedOnResume == null) {
            return null;
        }

        // Creation of the rewriter
        CompilationUnit compilationUnit = (CompilationUnit) currentOnCreate.getRoot();
        ASTRewrite astRewrite = ASTRewrite.create(compilationUnit.getAST());

        // Accumulate the replacements
        astRewrite.replace(currentOnCreate, proposedOnCreate, null);
        if (currentOnResume == null) {
            // Insert the onResume() after the onCreate(Bundle)
            ListRewrite listRewrite = astRewrite.getListRewrite((TypeDeclaration) compilationUnit.types().get(0), TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
            listRewrite.insertAfter(proposedOnResume, proposedOnCreate, null);
        } else {
            astRewrite.replace(currentOnResume, proposedOnResume, null);
        }

        return rewriteFile(methodProposal, astRewrite);
    }
}
