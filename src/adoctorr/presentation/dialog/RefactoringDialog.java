package adoctorr.presentation.dialog;

import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.bean.smell.MethodSmell;
import adoctorr.application.refactoring.RefactoringDriver;
import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.eclipse.jface.text.BadLocationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class RefactoringDialog extends JDialog {
    private JPanel contentPane;
    private JLabel labelMethodFileName;

    private MethodProposal methodProposal;
    private Project project;
    private ArrayList<MethodSmell> smellMethodList;
    private RefactoringDriver refactoringDriver;

    private RefactoringDialog(MethodProposal methodProposal, Project project, ArrayList<MethodSmell> smellMethodList) {
        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 3;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        setTitle("aDoctor - Refactoring");

        this.methodProposal = methodProposal;
        this.project = project;
        this.smellMethodList = smellMethodList;
        this.refactoringDriver = new RefactoringDriver(methodProposal);

        String fileName = methodProposal.getMethodSmell().getSourceFile().getName();
        String methodName = methodProposal.getMethodSmell().getMethodBean().getName();

        labelMethodFileName.setText("to the method " + methodName + " in file " + fileName);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
    }

    public static void show(MethodProposal methodProposal, Project project, ArrayList<MethodSmell> smellMethodList) {
        RefactoringDialog refactoringDialog = new RefactoringDialog(methodProposal, project, smellMethodList);

        refactoringDialog.startRefactoring();

        refactoringDialog.pack();
        refactoringDialog.setVisible(true);
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
                showResults(result);
            }
        };
        swingWorker.execute();
    }

    private void onExit() {
        dispose();
    }

    private void showResults(boolean result) {
        dispose();
        // Refreshes the Editor in order to reflect the changes to the files
        SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);

        if (!result) {
            FailureDialog.show(project, smellMethodList);
        } else {
            methodProposal.getMethodSmell().setResolved(true);

            // Updates the editor with the changes made to the files
            Document[] documents = FileDocumentManager.getInstance().getUnsavedDocuments();
            for (Document document1 : documents) {
                FileDocumentManager.getInstance().reloadFromDisk(document1);
            }
            SuccessDialog.show(project);
        }
    }
}
