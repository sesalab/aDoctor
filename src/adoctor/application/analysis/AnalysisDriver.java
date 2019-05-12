package adoctor.application.analysis;

import adoctor.application.analysis.analyzers.ClassSmellAnalyzer;
import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.ClassBean;
import adoctor.application.smell.ClassSmell;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings({"SynchronizeOnNonFinalField", "unchecked"})
public class AnalysisDriver {

    private List<File> projectFiles;
    private String[] pathEntries;
    private ArrayList<ClassSmellAnalyzer> classSmellAnalyzers;
    private String targetPackage;
    private AnalysisThread analysisThread;

    public AnalysisDriver(List<File> projectFiles, String[] pathEntries, ArrayList<ClassSmellAnalyzer> classSmellAnalyzers, String targetPackage) {
        this.projectFiles = projectFiles;
        this.pathEntries = pathEntries;
        this.classSmellAnalyzers = classSmellAnalyzers;
        this.targetPackage = targetPackage;
        this.analysisThread = new AnalysisThread(this);
    }

    public List<ClassSmell> startAnalysis() throws InterruptedException {
        analysisThread.start();
        synchronized (analysisThread) {
            analysisThread.wait();
        }
        return analysisThread.result;
    }

    public void abortAnalysis() {
        analysisThread.stop = true;
    }

    // TODO High fix the interruptable part of the analysis.
    private List<ClassSmell> analyze() throws IOException {
        List<ClassSmell> classSmells = new ArrayList<>();
        if (classSmellAnalyzers == null || classSmellAnalyzers.size() <= 0
                || projectFiles == null || projectFiles.size() <= 0) {
            return classSmells;
        }

        // Analysis phase
        Pattern pattern;
        if (targetPackage == null || targetPackage.equals("")) {
            pattern = Pattern.compile(".*");
        } else {
            pattern = Pattern.compile("^" + targetPackage + "(\\..*)?$");
        }
        for (File projectFile : projectFiles) {
            if (projectFile.isFile()) {
                CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(projectFile, pathEntries);
                List<TypeDeclaration> types = (List<TypeDeclaration>) compilationUnit.types();
                // Package filtering
                String packageName = compilationUnit.getPackage().getName().toString();
                if (packageName.matches(pattern.pattern())) {
                    System.out.println("Analizzo il file: " + projectFile.getName());
                    for (TypeDeclaration type : types) {
                        ClassBean classBean = new ClassBean();
                        classBean.setTypeDeclaration(type);
                        classBean.setSourceFile(projectFile);
                        // Analysis launch
                        for (ClassSmellAnalyzer analyzer : classSmellAnalyzers) {
                            ClassSmell classSmell = analyzer.analyze(classBean);
                            if (classSmell != null) {
                                classSmells.add(classSmell);
                            }
                        }
                    }
                }
            }
        }
        return classSmells;
    }

    private static class AnalysisThread extends Thread {
        private AnalysisDriver analysisDriver;
        private volatile List<ClassSmell> result;
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

        private List<ClassSmell> runAnalysis() {
            try {
                if (stop) {
                    return null;
                }
                return analysisDriver.analyze();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}