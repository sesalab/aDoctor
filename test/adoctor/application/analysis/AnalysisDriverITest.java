package adoctor.application.analysis;

import adoctor.application.bean.smell.MethodSmell;
import beans.ClassBean;
import beans.MethodBean;
import beans.PackageBean;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SuppressWarnings("unchecked")
class AnalysisDriverITest {

    private static String testDirectory = "testResources";
    private static String testPackage = "testPackage";
    private static String testClass = "testDW_ERB1";

    @ParameterizedTest
    @MethodSource("analyzeProvider")
    void analyze(ArrayList<MethodSmellAnalyzer> methodSmellAnalyzers, ArrayList<PackageBean> projectPackages, HashMap<String, File> sourceFileMap, ArrayList<MethodBean> oracle) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AnalysisDriver analysisDriver = new AnalysisDriver(null, methodSmellAnalyzers, "");
        // Reflection
        Method method = AnalysisDriver.class.getDeclaredMethod("analyze", ArrayList.class, HashMap.class);
        method.setAccessible(true);
        ArrayList<MethodSmell> result = (ArrayList<MethodSmell>) method.invoke(analysisDriver, projectPackages, sourceFileMap);

        assertEquals(oracle.size(), result.size());
        for (int i = 0; i < result.size(); i++) {
            assertEquals(oracle.get(0), result.get(0).getMethod());
        }
    }

    private static Stream analyzeProvider() throws IOException {
        // Inputs
        DWAnalyzer dwAnalyzer = new DWAnalyzer();
        ERBAnalyzer erbAnalyzer = new ERBAnalyzer();

        ArrayList<MethodSmellAnalyzer> analyzersEmpty = new ArrayList<>();
        ArrayList<MethodSmellAnalyzer> analyzersDW = new ArrayList<>();
        analyzersDW.add(dwAnalyzer);
        ArrayList<MethodSmellAnalyzer> analyzersERB = new ArrayList<>();
        analyzersERB.add(erbAnalyzer);
        ArrayList<MethodSmellAnalyzer> analyzersDW_ERB = new ArrayList<>();
        analyzersDW_ERB.add(dwAnalyzer);
        analyzersDW_ERB.add(erbAnalyzer);

        ArrayList<PackageBean> packagesEmpty = new ArrayList<>();
        ArrayList<PackageBean> packages = AnalysisTestHelper.getPackageBeans(testDirectory, testPackage, testClass);
        File testFile = new File(testDirectory + "/" + testPackage + "/" + testClass + ".java");
        HashMap<String, File> fileHashMapEmpty = new HashMap<>();
        HashMap<String, File> fileHashMap = new HashMap<>();
        fileHashMap.put(testPackage + "." + testClass, testFile);

        // Oracles
        ArrayList<ClassBean> classes = (ArrayList<ClassBean>) packages.get(0).getClasses();
        ArrayList<MethodBean> methods = (ArrayList<MethodBean>) classes.get(0).getMethods();
        MethodBean smellyBeanERB0 = methods.get(0);
        MethodBean smellyBeanDW1 = methods.get(1);
        MethodBean smellyBeanDW2 = methods.get(2);
        ArrayList<MethodBean> smellyBeansEmpty = new ArrayList<>();
        ArrayList<MethodBean> smellyBeansDW = new ArrayList<>();
        smellyBeansDW.add(smellyBeanDW1);
        smellyBeansDW.add(smellyBeanDW2);
        ArrayList<MethodBean> smellyBeansERB = new ArrayList<>();
        smellyBeansERB.add(smellyBeanERB0);
        ArrayList<MethodBean> smellyBeansDW_ERB = new ArrayList<>();
        smellyBeansDW_ERB.add(smellyBeanERB0);
        smellyBeansDW_ERB.add(smellyBeanDW1);
        smellyBeansDW_ERB.add(smellyBeanDW2);

        return Stream.of(
                arguments(null, null, null, smellyBeansEmpty),
                arguments(analyzersEmpty, packagesEmpty, fileHashMapEmpty, smellyBeansEmpty),
                arguments(analyzersEmpty, packagesEmpty, fileHashMap, smellyBeansEmpty),
                arguments(analyzersEmpty, packages, fileHashMapEmpty, smellyBeansEmpty),
                arguments(analyzersEmpty, packages, fileHashMap, smellyBeansEmpty),
                arguments(analyzersDW, packages, fileHashMap, smellyBeansDW),
                arguments(analyzersERB, packages, fileHashMap, smellyBeansERB),
                arguments(analyzersDW_ERB, packages, fileHashMap, smellyBeansDW_ERB)
        );
    }
}