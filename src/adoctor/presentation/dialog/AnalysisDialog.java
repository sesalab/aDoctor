package adoctor.presentation.dialog;

import adoctor.application.analysis.*;
import adoctor.application.bean.smell.MethodSmell;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class AnalysisDialog extends AbstractDialog {
    public static final String TITLE = "aDoctor - Analysis";

    private AnalysisCallback analysisCallback;
    private AnalysisDriver analysisDriver;

    private JPanel contentPane;
    private JButton buttonAbort;

    public static void show(AnalysisCallback analysisCallback, Project project, boolean[] selections, String targetPackage) {
        AnalysisDialog analysisDialog = new AnalysisDialog(analysisCallback, project, selections, targetPackage);
        analysisDialog.startAnalysis();

        analysisDialog.showInCenter();
    }

    private AnalysisDialog(AnalysisCallback analysisCallback, Project project, boolean[] selections, String targetPackage) {
        init(analysisCallback, project, selections, targetPackage);
    }

    private void init(AnalysisCallback analysisCallback, Project project, boolean[] selections, String targetPackage) {
        super.init(contentPane, TITLE, buttonAbort);

        this.analysisCallback = analysisCallback;
        ArrayList<MethodSmellAnalyzer> methodSmellAnalyzers = new ArrayList<>();
        if (selections[0]) {
            methodSmellAnalyzers.add(new DWAnalyzer());
        }
        if (selections[1]) {
            methodSmellAnalyzers.add(new ERBAnalyzer());
        }
        if (selections[2]) {
            methodSmellAnalyzers.add(new IDSAnalyzer());
        }
        this.analysisDriver = new AnalysisDriver(project, methodSmellAnalyzers, targetPackage);

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
