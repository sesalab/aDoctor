package adoctorr.application.analysis;

import adoctorr.application.ast.ASTUtilities;
import adoctorr.application.bean.smell.MethodSmell;
import beans.ClassBean;
import beans.MethodBean;
import beans.PackageBean;
import com.intellij.openapi.project.Project;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("SynchronizeOnNonFinalField")
public class AnalysisDriver {

    private Project project;
    private AnalysisHelper analysisHelper;
    private AnalysisThread analysisThread;

    public AnalysisDriver(Project project) {
        this.project = project;
        this.analysisHelper = new AnalysisHelper();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ArrayList<MethodSmell> startAnalysis() throws InterruptedException {
        this.analysisThread = new AnalysisThread(this);
        analysisThread.start();
        synchronized (analysisThread) {
            analysisThread.wait();
        }
        return analysisThread.result;
    }

    public void abortAnalysis() {
        analysisThread.stop = true;
    }

    private static class AnalysisThread extends Thread {
        private AnalysisDriver analysisDriver;
        private volatile ArrayList<MethodSmell> result;
        private volatile boolean stop;

        private AnalysisThread(AnalysisDriver analysisDriver) {
            this.analysisDriver = analysisDriver;
            result = null;
            stop = false;
        }

        public void run() {
            synchronized (this) {
                System.out.println("Analisi avviata");
                result = runAnalysis();
                System.out.println("Analisi terminata");
                notify();
            }
        }

        private ArrayList<MethodSmell> runAnalysis() {
            try {
                if (stop) {
                    return null;
                }
                // Very very slow!
                ArrayList<PackageBean> projectPackageList = analysisDriver.buildPackageList();
                if (projectPackageList == null) {
                    return null;
                }
                System.out.println("\tprojectPackageList costruita");
                if (stop) {
                    return null;
                }
                ArrayList<File> javaFilesList = analysisDriver.getAllJavaFiles();
                if (javaFilesList == null) {
                    return null;
                }
                System.out.println("\tjavaFilesList costruita");
                if (stop) {
                    return null;
                }
                try {
                    HashMap<String, File> sourceFileMap = analysisDriver.buildSourceFileMap(javaFilesList);
                    if (sourceFileMap == null) {
                        return null;
                    }
                    System.out.println("\tsourceFileMap costruita");
                    if (stop) {
                        return null;
                    }
                    try {
                        return analysisDriver.analyze(projectPackageList, sourceFileMap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Builds an ArrayList with all code bean found in the whole project
     * returns null iff there are no smells
     *
     * @param packageList
     * @param sourceFileMap
     * @return
     */
    private ArrayList<MethodSmell> analyze(ArrayList<PackageBean> packageList, HashMap<String, File> sourceFileMap) throws IOException {
        if (packageList == null || sourceFileMap == null) {
            return null;
        }
        ArrayList<MethodSmell> methodSmells = new ArrayList<>();

        //TODO Questo poi cambierà nella CR_RS_1. La lista già corretta sarà ricevuta da AnalysisDialog
        ArrayList<MethodSmellAnalyzer> methodSmellAnalyzers = new ArrayList<>();
        methodSmellAnalyzers.add(new DWAnalyzer());
        methodSmellAnalyzers.add(new ERBAnalyzer());

        for (PackageBean packageBean : packageList) {
            for (ClassBean classBean : packageBean.getClasses()) {
                String classFullName = packageBean.getName() + "." + classBean.getName();
                File sourceFile = sourceFileMap.get(classFullName);
                CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(sourceFile);
                for (MethodBean methodBean : classBean.getMethods()) {
                    MethodDeclaration methodDeclaration = ASTUtilities.getMethodDeclarationFromContent(methodBean.getTextContent(), compilationUnit);

                    for (MethodSmellAnalyzer analyzer : methodSmellAnalyzers) {
                        MethodSmell methodSmell = analyzer.analyzeMethod(methodBean, methodDeclaration, compilationUnit, sourceFile);
                        if (methodSmell != null) {
                            methodSmells.add(methodSmell);
                        }
                    }
                }
            }
        }
        return methodSmells;
    }

    private ArrayList<PackageBean> buildPackageList() throws IOException {
        return analysisHelper.buildPackageList(project);
    }

    private ArrayList<File> getAllJavaFiles() {
        return analysisHelper.getAllJavaFiles(project);
    }

    private HashMap<String, File> buildSourceFileMap(ArrayList<File> javaFilesList) throws IOException {
        return analysisHelper.buildSourceFileMap(javaFilesList);
    }
}