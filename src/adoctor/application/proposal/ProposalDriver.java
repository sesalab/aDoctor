package adoctor.application.proposal;

import adoctor.application.bean.smell.ClassSmell;
import adoctor.application.proposal.proposers.ClassSmellProposer;
import adoctor.application.proposal.undo.Undo;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import process.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ProposalDriver {

    private ArrayList<ClassSmellProposer> classSmellProposers;

    public ProposalDriver(ArrayList<ClassSmellProposer> classSmellProposers) {
        this.classSmellProposers = classSmellProposers;
    }

    public Undo computeProposal(ClassSmell classSmell) throws IOException, BadLocationException {
        if (classSmellProposers == null) {
            return null;
        }
        for (ClassSmellProposer proposer : classSmellProposers) {
            ASTRewrite astRewrite = proposer.computeProposal(classSmell);
            if (astRewrite != null) {
                // Overwrite the document
                File sourceFile = classSmell.getClassBean().getSourceFile();
                org.eclipse.jface.text.Document document = new org.eclipse.jface.text.Document(FileUtilities.readFile(sourceFile.getAbsolutePath()));
                TextEdit edits = astRewrite.rewriteAST(document, JavaCore.getDefaultOptions()); // With JavaCore Options we keep the code format settings, so the \n
                UndoEdit undoEdit = edits.apply(document, TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS);
                return new Undo(undoEdit, document);
            }
        }
        return null;
    }
}
