package adoctor.application.refactoring;

import org.eclipse.jface.text.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class RefactoringDriver {

    private File targetFile;
    private Document proposedDocument;

    public RefactoringDriver(File targetFile, Document proposedDocument) {
        this.targetFile = targetFile;
        this.proposedDocument = proposedDocument;
    }

    public boolean startRefactoring() throws IOException {
        if (targetFile == null || proposedDocument == null) {
            return false;
        }
        String text = proposedDocument.get();
        PrintWriter pw = new PrintWriter(new FileOutputStream(targetFile, false));
        pw.print(text);
        pw.flush(); // Important
        //ApplicationManager.getApplication().invokeAndWait(thread);
        //TransactionGuardImpl.getInstance().submitTransactionAndWait(thread);
        return true;
    }
}
