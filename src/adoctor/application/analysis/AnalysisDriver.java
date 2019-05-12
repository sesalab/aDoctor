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
        this.analysisThread = new AnalysisThread();
    }

    public List<ClassSmell> startAnalysis() throws InterruptedException, StopAnalysisException {
        analysisThread.start();
        System.out.println("Analysis started");
        synchronized (analysisThread) {
            analysisThread.wait();
        }
        if (analysisThread.stopAnalysisException != null) {
            System.out.println("Analysis aborted");
            throw analysisThread.stopAnalysisException;
        } else {
            System.out.println("Analysis terminated successfully");
            return analysisThread.result;
        }
    }

    public void abortAnalysis() {
        analysisThread.stop = true;
    }

    private class AnalysisThread extends Thread {
        private volatile List<ClassSmell> result;
        private volatile boolean stop;
        private final static String STOP_MESSAGE = "Analysis has been stopped";
        private volatile StopAnalysisException stopAnalysisException;

        private AnalysisThread() {
            this.stop = false;
        }

        public void run() {
            synchronized (this) {
                try {
                    result = runAnalysis();
                } catch (StopAnalysisException e) {
                    stopAnalysisException = e;
                    result = null;
                }
                notify();
            }
        }

        private List<ClassSmell> runAnalysis() throws StopAnalysisException {
            checkStop();
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
                checkStop();
                if (projectFile.isFile()) {
                    CompilationUnit compilationUnit;
                    try {
                        compilationUnit = ASTUtilities.getCompilationUnit(projectFile, pathEntries);
                        List<TypeDeclaration> types = (List<TypeDeclaration>) compilationUnit.types();
                        // Package filtering
                        String packageName = compilationUnit.getPackage().getName().toString();
                        if (packageName.matches(pattern.pattern())) {
                            System.out.println("Analyzing file: " + projectFile.getName());
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return classSmells;
        }

        private void checkStop() throws StopAnalysisException {
            if (stop) {
                throw new StopAnalysisException(STOP_MESSAGE);
            }
        }
    }
}