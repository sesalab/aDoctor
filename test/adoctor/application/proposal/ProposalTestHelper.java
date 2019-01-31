package adoctor.application.proposal;

import adoctor.application.analysis.*;
import adoctor.application.bean.smell.MethodSmell;
import beans.PackageBean;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class ProposalTestHelper {

    public static ArrayList<MethodSmell> getMethodSmells(String testDirectory, String testPackage, String testClass) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ArrayList<PackageBean> testPackages = AnalysisTestHelper.getPackageBeans(testDirectory, testPackage, testClass);

        // Phase 2: ArrayList<MethodSmell>
        String testFilePath = testDirectory + "/" + testPackage + "/" + testClass + ".java";
        File testFile = new File(testFilePath);
        HashMap<String, File> fileHashMap = new HashMap<>();
        fileHashMap.put(testPackage + "." + testClass, testFile);

        ArrayList<MethodSmellAnalyzer> methodSmellAnalyzers = new ArrayList<>();
        methodSmellAnalyzers.add(new DWAnalyzer());
        methodSmellAnalyzers.add(new ERBAnalyzer());
        AnalysisDriver analysisDriver = new AnalysisDriver(null, methodSmellAnalyzers, "");
        Method method = AnalysisDriver.class.getDeclaredMethod("analyze", ArrayList.class, HashMap.class);
        method.setAccessible(true);
        return (ArrayList<MethodSmell>) method.invoke(analysisDriver, testPackages, fileHashMap);
    }

}
