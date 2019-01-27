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
    public static final String TITLE = "aDoctor - Analysis";

    private AnalysisCallback analysisCallback;
    private AnalysisDriver analysisDriver;

    private JPanel contentPane;
    private JButton buttonAbort;

    public static void show(AnalysisCallback analysisCallback, Project project) {
        AnalysisDialog analysisDialog = new AnalysisDialog(analysisCallback, project);
        analysisDialog.startAnalysis();

        analysisDialog.pack();
        analysisDialog.setVisible(true);
    }

    private AnalysisDialog(AnalysisCallback analysisCallback, Project project) {
        this.analysisCallback = analysisCallback;
        this.analysisDriver = new AnalysisDriver(project);

        // Leave them as they are
        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) * 2 / 5;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        setTitle(TITLE);
        getRootPane().setDefaultButton(buttonAbort);

        buttonAbort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onAbort();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onAbort();
            }
        });
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
                    analysisCallback.analysisDone(AnalysisDialog.this, methodSmells);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        swingWorker.execute();
    }

    private void onAbort() {
        analysisDriver.abortAnalysis();
        analysisCallback.analysisAbort(this);
    }

    interface AnalysisCallback {
        void analysisAbort(AnalysisDialog analysisDialog);

        void analysisDone(AnalysisDialog analysisDialog, ArrayList<MethodSmell> methodSmells);
    }
}
