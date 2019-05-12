package adoctor.application.proposal;

import adoctor.application.proposal.proposers.ClassSmellProposer;
import adoctor.application.proposal.undo.Undo;
import adoctor.application.smell.ClassSmell;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
                String javaFileContent = new String(Files.readAllBytes(Paths.get(sourceFile.getAbsolutePath())), StandardCharsets.UTF_8);
                org.eclipse.jface.text.Document document = new org.eclipse.jface.text.Document(javaFileContent);
                TextEdit edits = astRewrite.rewriteAST(document, JavaCore.getDefaultOptions()); // With JavaCore Options we keep the code format settings, so the \n
                UndoEdit undoEdit = edits.apply(document, TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS);
                return new Undo(undoEdit, document);
            }
        }
        return null;
    }
}
