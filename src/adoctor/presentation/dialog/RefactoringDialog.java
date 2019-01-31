package adoctor.presentation.dialog;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.refactoring.DWRefactorer;
import adoctor.application.refactoring.ERBRefactorer;
import adoctor.application.refactoring.MethodSmellRefactorer;
import adoctor.application.refactoring.RefactoringDriver;
import org.eclipse.jface.text.BadLocationException;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class RefactoringDialog extends AbstractDialog {
    public static final String TITLE = "aDoctor - Refactoring";

    private RefactoringCallback refactoringCallback;
    private RefactoringDriver refactoringDriver;

    private JPanel contentPane;
    private JLabel labelMethodFileName;

    public static void show(RefactoringCallback refactoringCallback, MethodProposal methodProposal, boolean[] selections) {
        RefactoringDialog refactoringDialog = new RefactoringDialog(refactoringCallback, methodProposal, selections);

        refactoringDialog.startRefactoring();

        refactoringDialog.showInCenter();
    }

    private RefactoringDialog(RefactoringCallback refactoringCallback, MethodProposal methodProposal, boolean[] selections) {
        init(refactoringCallback, methodProposal, selections);
    }

    private void init(RefactoringCallback refactoringCallback, MethodProposal methodProposal, boolean[] selections) {
        super.init(contentPane, TITLE, null);

        this.refactoringCallback = refactoringCallback;
        ArrayList<MethodSmellRefactorer> methodSmellRefactorers = new ArrayList<>();
        if (selections[0]) {
            methodSmellRefactorers.add(new DWRefactorer());
        }
        if (selections[1]) {
            methodSmellRefactorers.add(new ERBRefactorer());
        }
        this.refactoringDriver = new RefactoringDriver(methodProposal, methodSmellRefactorers);

        String fileName = methodProposal.getMethodSmell().getSourceFile().getName();
        String methodName = methodProposal.getMethodSmell().getMethodBean().getName();
        labelMethodFileName.setText("the method " + methodName + " in file " + fileName);

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
                } catch (IOException | BadLocationException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void done() {
                boolean result;
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
        void refactoringDone(RefactoringDialog refactoringDialog, boolean result);

        void refactoringQuit(RefactoringDialog refactoringDialog);
    }
}
