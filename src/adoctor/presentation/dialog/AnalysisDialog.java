package adoctor.presentation.dialog;

import adoctor.application.analysis.AnalysisDriver;
import adoctor.application.analysis.StopAnalysisException;
import adoctor.application.analysis.analyzers.*;
import adoctor.application.smell.ClassSmell;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AnalysisDialog extends AbstractDialog {
    private static final String TITLE = "aDoctor - Analysis";

    private AnalysisCallback analysisCallback;
    private AnalysisDriver analysisDriver;

    private JPanel contentPane;
    private JButton buttonAbort;

    private AnalysisDialog(AnalysisCallback analysisCallback, List<File> projectFiles, String[] pathEntries, List<Boolean> selections, String targetPackage) {
        this.analysisCallback = analysisCallback;
        init(projectFiles, pathEntries, selections, targetPackage);
    }

    public static void show(AnalysisCallback analysisCallback, List<File> projectFiles, String[] pathEntries, List<Boolean> selections, String targetPackage) {
        AnalysisDialog analysisDialog = new AnalysisDialog(analysisCallback, projectFiles, pathEntries, selections, targetPackage);
        analysisDialog.startAnalysis();

        analysisDialog.showInCenter();
    }

    private void init(List<File> projectFiles, String[] pathEntries, List<Boolean> selections, String targetPackage) {
        super.init(contentPane, TITLE, buttonAbort);

        ArrayList<ClassSmellAnalyzer> classSmellAnalyzers = new ArrayList<>();
        if (selections.get(0)) {
            classSmellAnalyzers.add(new DWAnalyzer());
        }
        if (selections.get(1)) {
            classSmellAnalyzers.add(new ERBAnalyzer());
        }
        if (selections.get(2)) {
            classSmellAnalyzers.add(new IDSAnalyzer());
        }
        if (selections.get(3)) {
            classSmellAnalyzers.add(new ISAnalyzer());
        }
        if (selections.get(4)) {
            classSmellAnalyzers.add(new MIMAnalyzer());
        }
        if (selections.get(5)) {
            classSmellAnalyzers.add(new LTAnalyzer());
        }
        analysisDriver = new AnalysisDriver(projectFiles, pathEntries, classSmellAnalyzers, targetPackage);

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
        SwingWorker<List<ClassSmell>, Void> swingWorker = new SwingWorker<List<ClassSmell>, Void>() {
            private StopAnalysisException stopAnalysisException;

            @Override
            protected List<ClassSmell> doInBackground() {
                try {
                    return analysisDriver.startAnalysis();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (StopAnalysisException e) {
                    stopAnalysisException = e;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    if(stopAnalysisException == null) {
                        List<ClassSmell> classSmells = get();
                        analysisCallback.analysisDone(AnalysisDialog.this, classSmells);
                    } else {
                        analysisCallback.analysisAbort(AnalysisDialog.this);
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
    }

    interface AnalysisCallback {
        void analysisAbort(AnalysisDialog analysisDialog);

        void analysisDone(AnalysisDialog analysisDialog, List<ClassSmell> classSmells);
    }
}
