package adoctor.application.proposal;

import adoctor.application.bean.smell.MethodSmell;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
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

    private ArrayList<MethodSmellProposer> methodSmellProposers;

    public ProposalDriver(ArrayList<MethodSmellProposer> methodSmellProposers) {
        this.methodSmellProposers = methodSmellProposers;
    }

    public Document computeProposal(MethodSmell methodSmell) throws IOException, BadLocationException {
        if (methodSmellProposers == null) {
            return null;
        }
        for (MethodSmellProposer proposer : methodSmellProposers) {
            ASTRewrite astRewrite = proposer.computeProposal(methodSmell);
            if (astRewrite != null) {
                // Overwrite the document
                File sourceFile = methodSmell.getMethod().getSourceFile();
                org.eclipse.jface.text.Document document = new org.eclipse.jface.text.Document(FileUtilities.readFile(sourceFile.getAbsolutePath()));
                // TODO Mantenere i comment nel rewrite
                TextEdit edits = astRewrite.rewriteAST(document, JavaCore.getDefaultOptions()); // With JavaCore Options we keep the code format settings, so the \n
                // TODO UndoEdit da ritornare assieme al proposedDocument oppure ritornare solo TextEdit?
                UndoEdit undoEdit = edits.apply(document, TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS);
                String documentContent = document.get().replaceAll("(\r\n|\r)", "\n");
                Document proposedDocument = EditorFactory.getInstance().createDocument(documentContent);
                return proposedDocument;
            }
        }
        return null;
    }
}
