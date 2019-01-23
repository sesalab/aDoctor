package adoctorr.presentation.dialog;

import adoctorr.application.analysis.Analyzer;
import adoctorr.application.bean.smell.SmellMethodBean;
import beans.PackageBean;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AnalysisDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonAbort;

    private AnalysisThread analysisThread;

    private Project project;
    private ArrayList<SmellMethodBean> smellMethodList;

    private volatile boolean analysisAborted;
    private volatile boolean smellFound;

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

        this.project = project;
        smellMethodList = null;
        analysisAborted = false;
        smellFound = false;

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
        AnalysisDialog analysisDialog = new AnalysisDialog(project);

        // Save all files in the current project before starting the analysis
        FileDocumentManager.getInstance().saveAllDocuments();
        project.save();

        // Thread that manage the real analysis
        analysisDialog.analysisThread = new AnalysisThread(project, analysisDialog);
        analysisDialog.analysisThread.start();

        analysisDialog.pack();
        // setVisible(true) is blocking, that's why we use a Thread to start the real analysis
        analysisDialog.setVisible(true);

        // Invoked at the end of the analysis thread or when the dialog is closed because a dispose() is executed
        analysisDialog.checkResults();
    }

    private void onAbort() {
        // sets this flag to false, in order to stop the analysis thread, as soon as it can
        analysisAborted = true;
        System.out.println("Analisi abortita");

        // Disposing the analysis window unlocks UI thread blocked at the preceding setVisible(true)
        dispose();
    }

    private void checkResults() {
        if (analysisAborted) {
            AbortDialog.show(project); // It was aborted
        } else if (smellFound) {
            SmellDialog.show(project, smellMethodList); // If there is at least one smell, show the SmellDialog
        } else {
            NoSmellDialog.show(project); // There are no smell
        }
    }

    private static class AnalysisThread extends Thread {
        private Project project;
        private AnalysisDialog analysisDialog;

        AnalysisThread(Project project, AnalysisDialog analysisDialog) {
            this.project = project;
            this.analysisDialog = analysisDialog;
        }

        public void run() {
            System.out.println("Analisi avviata");

            startAnalysis();

            // Disposing the analysis window unlocks UI thread blocked at the preceding setVisible(true)
            analysisDialog.dispose();
        }

        void startAnalysis() {
            Analyzer analyzer = new Analyzer();
            // The final results
            ArrayList<SmellMethodBean> smellMethodList = null;
            ArrayList<PackageBean> projectPackageList;
            try {
                // runThread flag is periodically checked to see if the analysis can go on
                if (!analysisDialog.analysisAborted) {
                    projectPackageList = analyzer.buildPackageList(project);     // Very very slow!
                    if (projectPackageList != null && !analysisDialog.analysisAborted) {
                        System.out.println("\tprojectPackageList costruita");
                        ArrayList<File> javaFilesList = analyzer.getAllJavaFiles(project);
                        if (javaFilesList != null && !analysisDialog.analysisAborted) {
                            try {
                                System.out.println("\tjavaFilesList costruita");
                                HashMap<String, File> sourceFileMap = analyzer.buildSourceFileMap(javaFilesList);
                                if (sourceFileMap != null && !analysisDialog.analysisAborted) {
                                    try {
                                        System.out.println("\tsourceFileMap costruita");
                                        smellMethodList = analyzer.analyze(projectPackageList, sourceFileMap);
                                        if (smellMethodList != null && smellMethodList.size() > 0 && !analysisDialog.analysisAborted) {
                                            analysisDialog.smellFound = true;
                                            System.out.println("Analisi terminata con successo");
                                        }
                                    } catch (IOException e3) {
                                        smellMethodList = null;
                                        e3.printStackTrace();
                                    }
                                }
                            } catch (IOException e2) {
                                smellMethodList = null;
                                e2.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException e1) {
                smellMethodList = null;
                e1.printStackTrace();
            }
            // Set the results to the analysisDialog in a callback-like style
            analysisDialog.smellMethodList = smellMethodList;
        }
    }
}
