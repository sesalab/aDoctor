package adoctor.presentation.dialog;

import adoctor.application.analysis.AnalysisDriver;
import adoctor.application.analysis.analyzers.*;
import adoctor.application.smell.ClassSmell;

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

    private AnalysisDialog(AnalysisCallback analysisCallback, String projectBasePath, String[] pathEntries, boolean[] selections, String targetPackage) {
        init(analysisCallback, projectBasePath, pathEntries, selections, targetPackage);
    }

    public static void show(AnalysisCallback analysisCallback, String projectBasePath, String[] pathEntries, boolean[] selections, String targetPackage) {
        AnalysisDialog analysisDialog = new AnalysisDialog(analysisCallback, projectBasePath, pathEntries, selections, targetPackage);
        analysisDialog.startAnalysis();

        analysisDialog.showInCenter();
    }

    private void init(AnalysisCallback analysisCallback, String projectBasePath, String[] pathEntries, boolean[] selections, String targetPackage) {
        super.init(contentPane, TITLE, buttonAbort);

        this.analysisCallback = analysisCallback;
        ArrayList<ClassSmellAnalyzer> classSmellAnalyzers = new ArrayList<>();
        if (selections[0]) {
            classSmellAnalyzers.add(new DWAnalyzer());
        }
        if (selections[1]) {
            classSmellAnalyzers.add(new ERBAnalyzer());
        }
        if (selections[2]) {
            classSmellAnalyzers.add(new IDSAnalyzer());
        }
        if (selections[3]) {
            classSmellAnalyzers.add(new ISAnalyzer());
        }
        if (selections[4]) {
            classSmellAnalyzers.add(new MIMAnalyzer());
        }
        this.analysisDriver = new AnalysisDriver(projectBasePath, pathEntries, classSmellAnalyzers, targetPackage);

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
        SwingWorker<ArrayList<ClassSmell>, Void> swingWorker = new SwingWorker<ArrayList<ClassSmell>, Void>() {
            @Override
            protected ArrayList<ClassSmell> doInBackground() {
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
                    ArrayList<ClassSmell> classSmells = get();
                    analysisCallback.analysisDone(AnalysisDialog.this, classSmells);
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

        void analysisDone(AnalysisDialog analysisDialog, ArrayList<ClassSmell> classSmells);
    }
}
