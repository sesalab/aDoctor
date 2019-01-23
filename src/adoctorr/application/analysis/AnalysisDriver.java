package adoctorr.application.analysis;

import adoctorr.application.ast.ASTUtilities;
import adoctorr.application.bean.smell.DWSmell;
import adoctorr.application.bean.smell.ERBSmell;
import adoctorr.application.bean.smell.MethodSmell;
import beans.ClassBean;
import beans.MethodBean;
import beans.PackageBean;
import com.intellij.openapi.project.Project;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import parser.CodeParser;
import process.FileUtilities;
import process.FolderToJavaProjectConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AnalysisDriver {

    public AnalysisDriver() {

    }

    /**
     * Builds an ArrayList of PackageBean, a legacy structure from aDoctor.
     * This will be used during the building of the list of code smells.
     *
     * @param project
     * @return
     */
    public ArrayList<PackageBean> buildPackageList(Project project) throws IOException {
        ArrayList<PackageBean> packageList = null;
        if (project != null) {
            String projectBasePath = project.getBasePath();
            if (projectBasePath != null) {
                File projectDirectory = new File(project.getBasePath());

                // Calling the aDoctor legacy method to build this list
                packageList = FolderToJavaProjectConverter.convert(projectDirectory.getAbsolutePath());

                // belongingClass was not set in aDoctor API: this is just a fix
                for (PackageBean packageBean : packageList) {
                    for (ClassBean classBean : packageBean.getClasses()) {
                        for (MethodBean methodBean : classBean.getMethods()) {
                            methodBean.setBelongingClass(classBean);
                        }
                    }
                }
            }
        }
        return packageList;
    }

    /**
     * Obtains all project Java files
     *
     * @param project
     * @return
     */
    public ArrayList<File> getAllJavaFiles(Project project) {
        ArrayList<File> javaFilesList = null;
        if (project != null) {
            String projectBasePath = project.getBasePath();
            if (projectBasePath != null) {
                File projectDirectory = new File(project.getBasePath());

                // Invokes the recursive function to get all .java files
                javaFilesList = getJavaFilesInDirectory(projectDirectory);
            }
        }
        return javaFilesList;
    }

    /**
     * Builds a HashMap that given a class FQN it is possible to get the related java File.
     *
     * @param javaFilesList
     * @return
     */
    public HashMap<String, File> buildSourceFileMap(ArrayList<File> javaFilesList) throws IOException {
        HashMap<String, File> sourceFileMap = null;
        if (javaFilesList != null && javaFilesList.size() > 0) {
            sourceFileMap = new HashMap<>();
            for (File javaFile : javaFilesList) {
                // Creates the CompilationUnit of every Java file in order to get its FQN easily. This is done through CodeParser of aDoctor
                CodeParser codeParser = new CodeParser();
                String javaFileContent = FileUtilities.readFile(javaFile.getAbsolutePath());
                CompilationUnit compilationUnit = codeParser.createParser(javaFileContent);

                // Builds the FQN String
                String packageName = compilationUnit.getPackage().getName().getFullyQualifiedName();
                TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
                String className = typeDeclaration.getName().toString();
                String classFullName = packageName + "." + className;

                sourceFileMap.put(classFullName, javaFile);
            }
        }
        return sourceFileMap;
    }

    /**
     * Builds an ArrayList with all code bean found in the whole project
     * returns null iff there are no smells
     *
     * @param packageList
     * @param sourceFileMap
     * @return
     */
    public ArrayList<MethodSmell> analyze(ArrayList<PackageBean> packageList, HashMap<String, File> sourceFileMap) throws IOException {
        ArrayList<MethodSmell> smellMethodList = null;
        if (packageList != null && sourceFileMap != null) {
            ArrayList<DWSmell> durableWakelockList = new ArrayList<>();
            ArrayList<ERBSmell> earlyResourceBindingList = new ArrayList<>();

            DWAnalyzer DWAnalyzer = new DWAnalyzer();
            ERBAnalyzer ERBAnalyzer = new ERBAnalyzer();

            for (PackageBean packageBean : packageList) {
                for (ClassBean classBean : packageBean.getClasses()) {
                    String className = classBean.getName();
                    String packageName = packageBean.getName();
                    String classFullName = packageName + "." + className;
                    File sourceFile = sourceFileMap.get(classFullName);

                    CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(sourceFile);
                    for (MethodBean methodBean : classBean.getMethods()) {
                        MethodDeclaration methodDeclaration = ASTUtilities.getMethodDeclarationFromContent(methodBean.getTextContent(), compilationUnit);

                        DWSmell DWSmell = DWAnalyzer.analyzeMethod(methodBean, methodDeclaration, compilationUnit, sourceFile);
                        if (DWSmell != null) {
                            durableWakelockList.add(DWSmell);
                        }
                        ERBSmell ERBSmell = ERBAnalyzer.analyzeMethod(methodBean, methodDeclaration, compilationUnit, sourceFile);
                        if (ERBSmell != null) {
                            earlyResourceBindingList.add(ERBSmell);
                        }
                    }
                }
            }
            smellMethodList = new ArrayList<>();
            smellMethodList.addAll(durableWakelockList);
            smellMethodList.addAll(earlyResourceBindingList);
        }
        return smellMethodList;
    }

    /**
     * Get all Java files in the directory and then recusively does the same in all subdirectories
     *
     * @param directory
     * @return
     */
    private ArrayList<File> getJavaFilesInDirectory(File directory) {
        ArrayList<File> javaFilesList = new ArrayList<>();
        // File list of the current directory
        File[] fList = directory.listFiles();

        // If the directory is non empty
        if (fList != null) {
            for (File file : fList) {
                if (file.isFile()) {
                    if (file.getName().contains(".java")) {
                        javaFilesList.add(file);
                    }
                } else if (file.isDirectory()) {
                    File subDirectory = new File(file.getAbsolutePath());
                    javaFilesList.addAll(getJavaFilesInDirectory(subDirectory));
                }
            }
        }
        return javaFilesList;
    }
}