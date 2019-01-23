package adoctorr.presentation.dialog;

import adoctorr.application.bean.proposal.ProposalMethodBean;
import adoctorr.application.bean.smell.SmellMethodBean;
import adoctorr.application.refactoring.Refactorer;
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

    private ProposalMethodBean proposalMethodBean;
    private Project project;
    private ArrayList<SmellMethodBean> smellMethodList;

    private RefactoringThread refactoringThread;
    private boolean result;

    private RefactoringDialog(ProposalMethodBean proposalMethodBean, Project project, ArrayList<SmellMethodBean> smellMethodList) {
        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 3;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        setTitle("aDoctor - Refactoring");

        this.proposalMethodBean = proposalMethodBean;
        this.project = project;
        this.smellMethodList = smellMethodList;

        String fileName = proposalMethodBean.getSmellMethodBean().getSourceFile().getName();
        String methodName = proposalMethodBean.getSmellMethodBean().getMethodBean().getName();

        labelMethodFileName.setText("to the method " + methodName + " in file " + fileName);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
    }

    public static void show(ProposalMethodBean proposalMethodBean, Project project, ArrayList<SmellMethodBean> smellMethodList) {
        RefactoringDialog refactoringDialog = new RefactoringDialog(proposalMethodBean, project, smellMethodList);

        // Thread that manage the real refactoring
        refactoringDialog.refactoringThread = new RefactoringThread(refactoringDialog, proposalMethodBean);
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
            proposalMethodBean.getSmellMethodBean().setResolved(true);

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
        private ProposalMethodBean proposalMethodBean;

        RefactoringThread(RefactoringDialog refactoringDialog, ProposalMethodBean proposalMethodBean) {
            this.refactoringDialog = refactoringDialog;
            this.proposalMethodBean = proposalMethodBean;
        }

        public void run() {
            System.out.println("Refactoring avviato");

            startRefactoring();

            System.out.println("Refactoring terminato con successo");

            // Disposing the analysis window unlocks UI thread blocked at the preceding setVisible(true)
            refactoringDialog.dispose();
        }

        void startRefactoring() {
            Refactorer refactorer = new Refactorer();
            boolean result;
            try {
                result = refactorer.applyRefactoring(proposalMethodBean);
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
