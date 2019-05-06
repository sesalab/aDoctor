package adoctor.presentation.dialog;

import adoctor.application.bean.smell.ClassSmell;
import adoctor.application.refactoring.RefactoringDriver;
import org.eclipse.jface.text.Document;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RefactoringDialog extends AbstractDialog {
    private static final String TITLE = "aDoctor - Refactoring";

    private RefactoringCallback refactoringCallback;
    private RefactoringDriver refactoringDriver;

    private JPanel contentPane;
    private JLabel labelMethodFileName;

    private RefactoringDialog(RefactoringCallback refactoringCallback, ClassSmell targetSmell, Document proposedDocument) {
        init(refactoringCallback, targetSmell, proposedDocument);
    }

    public static void show(RefactoringCallback refactoringCallback, ClassSmell targetSmell, Document proposedDocument) {
        RefactoringDialog refactoringDialog = new RefactoringDialog(refactoringCallback, targetSmell, proposedDocument);

        refactoringDialog.startRefactoring();

        refactoringDialog.showInCenter();
    }

    private void init(RefactoringCallback refactoringCallback, ClassSmell targetSmell, Document proposedDocument) {
        super.init(contentPane, TITLE, null);

        this.refactoringCallback = refactoringCallback;
        File targetFile = targetSmell.getClassBean().getSourceFile();
        this.refactoringDriver = new RefactoringDriver(targetFile, proposedDocument);
        labelMethodFileName.setText("in file " + targetFile.getName());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });
    }

    private void startRefactoring() {
        SwingWorker<Boolean, Void> swingWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    return refactoringDriver.startRefactoring();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void done() {
                Boolean result;
                try {
                    result = get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    result = false;
                }
                refactoringCallback.refactoringDone(RefactoringDialog.this, result);
            }
        };
        swingWorker.execute();
    }

    private void onQuit() {
        refactoringCallback.refactoringQuit(this);
    }

    interface RefactoringCallback {
        void refactoringDone(RefactoringDialog refactoringDialog, Boolean result);

        void refactoringQuit(RefactoringDialog refactoringDialog);
    }
}
