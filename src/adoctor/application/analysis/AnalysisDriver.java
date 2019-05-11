package adoctor.application.analysis;

import adoctor.application.analysis.analyzers.ClassSmellAnalyzer;
import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.ClassBean;
import adoctor.application.smell.ClassSmell;
import beans.PackageBean;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

@SuppressWarnings("SynchronizeOnNonFinalField")
public class AnalysisDriver {

    private String projectBasePath;
    private String[] pathEntries;
    private ArrayList<ClassSmellAnalyzer> classSmellAnalyzers;
    private String targetPackage;
    private AnalysisHelper analysisHelper;
    private AnalysisThread analysisThread;

    public AnalysisDriver(String projectBasePath, String[] pathEntries, ArrayList<ClassSmellAnalyzer> classSmellAnalyzers, String targetPackage) {
        this.projectBasePath = projectBasePath;
        this.pathEntries = pathEntries;
        this.classSmellAnalyzers = classSmellAnalyzers;
        this.targetPackage = targetPackage;
        this.analysisHelper = new AnalysisHelper();
    }

    public ArrayList<ClassSmell> startAnalysis() throws InterruptedException {
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

    private ArrayList<ClassSmell> analyze(ArrayList<PackageBean> projectPackages, HashMap<String, File> sourceFileMap) throws IOException {
        ArrayList<ClassSmell> classSmells = new ArrayList<>();
        if (classSmellAnalyzers == null || classSmellAnalyzers.size() <= 0
                || projectPackages == null || projectPackages.size() <= 0
                || sourceFileMap == null || sourceFileMap.size() <= 0) {
            return classSmells;
        }
        // Builds the correct list of packages
        ArrayList<PackageBean> packages = new ArrayList<>();
        for (PackageBean packageBean : projectPackages) {
            Pattern pattern = Pattern.compile("^" + targetPackage + "\\..*");
            if (packageBean.getName().equals(targetPackage) || packageBean.getName().matches(pattern.pattern())) {
                packages.add(packageBean);
            }
        }
        if (packages.isEmpty()) {
            packages = projectPackages;
        }

        for (PackageBean packageBean : packages) {
            for (beans.ClassBean legacyClassBean : packageBean.getClasses()) {
                String classFullName = packageBean.getName() + "." + legacyClassBean.getName();
                File sourceFile = sourceFileMap.get(classFullName);
                ClassBean classBean = new ClassBean();
                classBean.setSourceFile(sourceFile);
                CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(pathEntries, sourceFile);
                classBean.setTypeDeclaration(ASTUtilities.getTypeDeclarationByName(compilationUnit, legacyClassBean.getName()));
                classBean.setLegacyClassBean(legacyClassBean); //Not so important
                for (ClassSmellAnalyzer analyzer : classSmellAnalyzers) {
                    ClassSmell classSmell = analyzer.analyze(classBean);
                    if (classSmell != null) {
                        classSmells.add(classSmell);
                    }
                }
            }
        }
        return classSmells;
    }

    private static class AnalysisThread extends Thread {
        private AnalysisDriver analysisDriver;
        private volatile ArrayList<ClassSmell> result;
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

        private ArrayList<ClassSmell> runAnalysis() {
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

    private ArrayList<PackageBean> buildPackageList() throws IOException {
        return analysisHelper.buildPackageList(projectBasePath);
    }

    private ArrayList<File> getAllJavaFiles() {
        return analysisHelper.getAllJavaFiles(projectBasePath);
    }

    private HashMap<String, File> buildSourceFileMap(ArrayList<File> javaFilesList) throws IOException {
        return analysisHelper.buildSourceFileMap(javaFilesList);
    }
}