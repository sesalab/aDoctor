package adoctorr.presentation.dialog;

import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.bean.smell.MethodSmell;
import adoctorr.application.refactoring.RefactoringDriver;
import com.intellij.ide.SaveAndSyncHandlerImpl;
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

public class RefactoringDialog extends JDialog {
    private JPanel contentPane;
    private JLabel labelMethodFileName;

    private MethodProposal methodProposal;
    private Project project;
    private ArrayList<MethodSmell> smellMethodList;

    private RefactoringThread refactoringThread;
    private boolean result;

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

        // Thread that manage the real refactoring
        refactoringDialog.refactoringThread = new RefactoringThread(refactoringDialog, methodProposal);
        refactoringDialog.refactoringThread.start();

        refactoringDialog.pack();
        // setVisibile(true) is blocking, that's why we use a Thread to start the real refatoring
        refactoringDialog.setVisible(true);

        refactoringDialog.showResults();
    }


    private void onExit() {
        dispose();
    }

    private void showResults() {
        // Refreshes the Editor in order to reflect the changes to the files
        SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);

        if (!result) {
            FailureDialog.show(project, smellMethodList);
        } else {
            methodProposal.getMethodSmell().setResolved(true);

            // Updates the editor with the changes made to the files
            com.intellij.openapi.editor.Document[] documents = FileDocumentManager.getInstance().getUnsavedDocuments();
            for (com.intellij.openapi.editor.Document document1 : documents) {
                FileDocumentManager.getInstance().reloadFromDisk(document1);
            }

            SuccessDialog.show(project);
        }
    }

    private static class RefactoringThread extends Thread {
        private RefactoringDialog refactoringDialog;
        private MethodProposal methodProposal;

        RefactoringThread(RefactoringDialog refactoringDialog, MethodProposal methodProposal) {
            this.refactoringDialog = refactoringDialog;
            this.methodProposal = methodProposal;
        }

        public void run() {
            System.out.println("Refactoring avviato");

            startRefactoring();

            System.out.println("Refactoring terminato con successo");

            // Disposing the analysis window unlocks UI thread blocked at the preceding setVisible(true)
            refactoringDialog.dispose();
        }

        void startRefactoring() {
            RefactoringDriver refactoringDriver = new RefactoringDriver();
            boolean result;
            try {
                result = refactoringDriver.applyRefactoring(methodProposal);
            } catch (BadLocationException e1) {
                result = false;
                e1.printStackTrace();
            } catch (IOException e2) {
                result = false;
                e2.printStackTrace();
            }
            refactoringDialog.result = result;
        }
    }
}
