package adoctor.application.analysis;

import beans.ClassBean;
import beans.MethodBean;
import beans.PackageBean;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import parser.CodeParser;
import process.FileUtilities;
import process.FolderToJavaProjectConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class AnalysisHelper {

    ArrayList<PackageBean> buildPackageList(String projectBasePath) throws IOException {
        ArrayList<PackageBean> packageList = null;
        if (projectBasePath != null) {
            File projectDirectory = new File(projectBasePath);

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
        return packageList;
    }

    ArrayList<File> getAllJavaFiles(String projectBasePath) {
        ArrayList<File> javaFilesList = null;
        if (projectBasePath != null) {
            File projectDirectory = new File(projectBasePath);

            // Invokes the recursive function to get all .java files
            javaFilesList = getJavaFilesInDirectory(projectDirectory);
        }
        return javaFilesList;
    }

    HashMap<String, File> buildSourceFileMap(ArrayList<File> javaFilesList) throws IOException {
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
