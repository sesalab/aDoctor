package adoctor.application.refactoring;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.rewriting.FileRewriter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import process.FileUtilities;

import java.io.File;
import java.io.IOException;

public abstract class MethodSmellRefactorer {

    private FileRewriter fileRewriter;

    MethodSmellRefactorer() {
        this.fileRewriter = new FileRewriter();
    }

    public FileRewriter getFileRewriter() {
        return fileRewriter;
    }

    public void setFileRewriter(FileRewriter fileRewriter) {
        this.fileRewriter = fileRewriter;
    }

    UndoEdit rewriteFile(MethodProposal methodProposal, ASTRewrite astRewrite) throws IOException, BadLocationException {
        File sourceFile = methodProposal.getMethodSmell().getMethod().getSourceFile();
        Document document = new Document(FileUtilities.readFile(sourceFile.getAbsolutePath()));
        TextEdit edits = astRewrite.rewriteAST(document, JavaCore.getDefaultOptions()); // With JavaCore Options we keep the code format settings, so the \n
        UndoEdit undoEdit = edits.apply(document, TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS);
        String documentContent = document.get();
        boolean result = fileRewriter.writeText(sourceFile, documentContent);
        if (result) {
            return undoEdit;
        } else {
            return null;
        }
    }

    public abstract UndoEdit applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException;
}
