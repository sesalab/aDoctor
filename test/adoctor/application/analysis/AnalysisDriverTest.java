package adoctor.application.analysis;

import adoctor.application.bean.smell.DWSmell;
import adoctor.application.bean.smell.ERBSmell;
import adoctor.application.bean.smell.MethodSmell;
import beans.MethodBean;
import beans.PackageBean;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class AnalysisDriverTest {

    private static String testDirectory = "testResources";
    private static String testPackage = "testPackage";
    private static String testClass = "testDW_ERB1";

    @ParameterizedTest
    @MethodSource("analyzeProvider")
    void analyze(ArrayList<MethodSmellAnalyzer> methodSmellAnalyzers, ArrayList<PackageBean> projectPackages, HashMap<String, File> sourceFileMap, int expectedSize) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AnalysisDriver analysisDriver = new AnalysisDriver(null, methodSmellAnalyzers, "");
        // Reflection
        Method method = AnalysisDriver.class.getDeclaredMethod("analyze", ArrayList.class, HashMap.class);
        method.setAccessible(true);
        ArrayList<MethodSmell> result = (ArrayList<MethodSmell>) method.invoke(analysisDriver, projectPackages, sourceFileMap);
        assertEquals(expectedSize, result.size());
    }

    private static Stream analyzeProvider() throws IOException {
        DWAnalyzer dwAnalyzer = mock(DWAnalyzer.class);
        when(dwAnalyzer.analyzeMethod(any(MethodBean.class), any(MethodDeclaration.class), any(CompilationUnit.class), any(File.class)))
                .thenReturn(new DWSmell());
        ERBAnalyzer erbAnalyzer = mock(ERBAnalyzer.class);
        when(erbAnalyzer.analyzeMethod(any(MethodBean.class), any(MethodDeclaration.class), any(CompilationUnit.class), any(File.class)))
                .thenReturn(new ERBSmell());

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

        return Stream.of(
                arguments(null, null, null, 0),
                arguments(analyzersEmpty, packagesEmpty, fileHashMapEmpty, 0),
                arguments(analyzersEmpty, packagesEmpty, fileHashMap, 0),
                arguments(analyzersEmpty, packages, fileHashMapEmpty, 0),
                arguments(analyzersEmpty, packages, fileHashMap, 0),
                arguments(analyzersDW, packages, fileHashMap, 4),
                arguments(analyzersERB, packages, fileHashMap, 4),
                arguments(analyzersDW_ERB, packages, fileHashMap, 8)
        );
    }
}