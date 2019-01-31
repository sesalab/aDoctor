package adoctor.application.proposal;

import adoctor.application.analysis.AnalysisDriver;
import adoctor.application.analysis.DWAnalyzer;
import adoctor.application.analysis.ERBAnalyzer;
import adoctor.application.analysis.MethodSmellAnalyzer;
import adoctor.application.bean.smell.MethodSmell;
import beans.ClassBean;
import beans.MethodBean;
import beans.PackageBean;
import process.FolderToJavaProjectConverter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unchecked")
class ProposalTestHelper {

    static ArrayList<MethodSmell> getMethodSmells(String testDirectory, String testPackage, String testClass) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String testFilePath = testDirectory + "/" + testPackage + "/" + testClass + ".java";
        File testFile = new File(testFilePath);
        HashMap<String, File> fileHashMap = new HashMap<>();
        fileHashMap.put(testPackage + "." + testClass, testFile);

        // Phase 1: ArrayList<PackageBean>
        File testDirectoryFile = new File(testDirectory);
        ArrayList<PackageBean> packages = FolderToJavaProjectConverter.convert(testDirectoryFile.getAbsolutePath());
        // belongingClass was not set in aDoctor API: this is just a fix
        for (PackageBean packageBean : packages) {
            for (ClassBean classBean : packageBean.getClasses()) {
                for (MethodBean methodBean : classBean.getMethods()) {
                    methodBean.setBelongingClass(classBean);
                }
            }
        }
        ArrayList<PackageBean> testPackages = new ArrayList<>();
        for (PackageBean packageBean : packages) {
            if (packageBean.getName().equals(testPackage)) {
                testPackages.add(packageBean);
            }
        }
        testPackages.get(0).getClasses().removeIf(classBean -> !classBean.getName().equals(testClass));

        // Phase 2: ArrayList<MethodSmell>
        ArrayList<MethodSmellAnalyzer> methodSmellAnalyzers = new ArrayList<>();
        methodSmellAnalyzers.add(new DWAnalyzer());
        methodSmellAnalyzers.add(new ERBAnalyzer());
        AnalysisDriver analysisDriver = new AnalysisDriver(null, methodSmellAnalyzers, "");
        Method method = AnalysisDriver.class.getDeclaredMethod("analyze", ArrayList.class, HashMap.class);
        method.setAccessible(true);
        return (ArrayList<MethodSmell>) method.invoke(analysisDriver, testPackages, fileHashMap);
    }

}
