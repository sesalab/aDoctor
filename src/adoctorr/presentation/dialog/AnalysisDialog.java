package adoctorr.presentation.dialog;

import adoctorr.application.analysis.AnalysisDriver;
import adoctorr.application.bean.smell.MethodSmell;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class AnalysisDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonAbort;

    private Project project;
    private AnalysisDriver analysisDriver;

    /**
     * Default constructor and initializator of the dialog
     *
     * @param project
     */
    private AnalysisDialog(Project project) {
        // Leave them as they are
        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) * 2 / 5;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        setTitle("aDoctor - Analysis");
        //setModalityType(ModalityType.MODELESS);

        this.project = project;
        this.analysisDriver = new AnalysisDriver(project);

        buttonAbort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onAbort();
            }
        });

        // call onAbort() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onAbort();
            }
        });
    }

    /**
     * First and only method from the outside to be called in order to show this dialog
     *
     * @param project
     */
    public static void show(Project project) {
        // Save all files in the current project before starting the analysis
        FileDocumentManager.getInstance().saveAllDocuments();
        project.save();

        AnalysisDialog analysisDialog = new AnalysisDialog(project);
        analysisDialog.startAnalysis();

        analysisDialog.pack();
        analysisDialog.setVisible(true);
    }

    // Control logic managed by a worker thread
    private void startAnalysis() {
        SwingWorker<ArrayList<MethodSmell>, Void> swingWorker = new SwingWorker<ArrayList<MethodSmell>, Void>() {
            @Override
            protected ArrayList<MethodSmell> doInBackground() {
                try {
                    return analysisDriver.startAnalysis();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    ArrayList<MethodSmell> methodSmells = get();
                    dispose();
                    if (methodSmells == null) {
                        AbortDialog.show(project);
                    } else if (methodSmells.size() == 0) {
                        NoSmellDialog.show(project);
                    } else {
                        SmellDialog.show(project, methodSmells);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        swingWorker.execute();
    }

    private void onAbort() {
        analysisDriver.abortAnalysis();
        System.out.println("Analisi abortita");
    }
}
