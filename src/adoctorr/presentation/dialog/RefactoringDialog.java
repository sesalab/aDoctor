package adoctorr.presentation.dialog;

import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.refactoring.RefactoringDriver;
import org.eclipse.jface.text.BadLocationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RefactoringDialog extends JDialog {
    public static final String TITLE = "aDoctor - Refactoring";

    private RefactoringCallback refactoringCallback;
    private RefactoringDriver refactoringDriver;

    private JPanel contentPane;
    private JLabel labelMethodFileName;

    public static void show(RefactoringCallback refactoringCallback, MethodProposal methodProposal) {
        RefactoringDialog refactoringDialog = new RefactoringDialog(refactoringCallback, methodProposal);

        refactoringDialog.startRefactoring();

        refactoringDialog.pack();
        refactoringDialog.setVisible(true);
    }

    private RefactoringDialog(RefactoringCallback refactoringCallback, MethodProposal methodProposal) {
        this.refactoringCallback = refactoringCallback;
        this.refactoringDriver = new RefactoringDriver(methodProposal);

        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 3;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        setTitle(TITLE);

        String fileName = methodProposal.getMethodSmell().getSourceFile().getName();
        String methodName = methodProposal.getMethodSmell().getMethodBean().getName();
        labelMethodFileName.setText("to the method " + methodName + " in file " + fileName);

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
