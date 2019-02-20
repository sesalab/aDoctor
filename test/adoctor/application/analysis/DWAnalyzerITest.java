package adoctor.application.analysis;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.smell.MethodSmell;
import beans.MethodBean;
import beans.PackageBean;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DWAnalyzerITest {

    private static String testDirectory = "testResources";
    private static String testPackage = "testPackage";
    private static String testClass = "testDW_ERB1";

    @ParameterizedTest
    @MethodSource("analyzerMethodProvider")
    void analyzeMethod(MethodBean methodBean, MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, File sourceFile, boolean shouldBeSmell) {
        DWAnalyzer testedAnalyzer = new DWAnalyzer();
        MethodSmell result = testedAnalyzer.analyzeMethod(methodBean, methodDeclaration, compilationUnit, sourceFile);
        if (shouldBeSmell) {
            assertEquals(result.getMethod(), methodBean);
        } else {
            assertNull(result);
        }
    }

    private static Stream analyzerMethodProvider() throws IOException {
        ArrayList<Arguments> testDatas = new ArrayList<>();
        testDatas.add(arguments(null, null, null, null, false));

        String testFilePath = testDirectory + "/" + testPackage + "/" + testClass + ".java";
        File testFile = new File(testFilePath);
        CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(testFile);

        // Add a test data for each Method in tested class
        ArrayList<PackageBean> packageBeans = AnalysisTestHelper.getPackageBeans(testDirectory, testPackage, testClass);
        ArrayList<MethodBean> methodBeans = AnalysisTestHelper.getMethodBeans(packageBeans);
        MethodBean methodBean1 = methodBeans.get(0);
        MethodBean methodBean2 = methodBeans.get(1);
        MethodBean methodBean3 = methodBeans.get(2);
        MethodBean methodBean4 = methodBeans.get(3);

        MethodDeclaration methodDeclaration1 = ASTUtilities.getMethodDeclarationFromContent(methodBean1.getTextContent(), compilationUnit);
        MethodDeclaration methodDeclaration2 = ASTUtilities.getMethodDeclarationFromContent(methodBean2.getTextContent(), compilationUnit);
        MethodDeclaration methodDeclaration3 = ASTUtilities.getMethodDeclarationFromContent(methodBean3.getTextContent(), compilationUnit);
        MethodDeclaration methodDeclaration4 = ASTUtilities.getMethodDeclarationFromContent(methodBean4.getTextContent(), compilationUnit);

        testDatas.add(arguments(methodBean1, methodDeclaration1, compilationUnit, testFile, false));
        testDatas.add(arguments(methodBean2, methodDeclaration2, compilationUnit, testFile, true));
        testDatas.add(arguments(methodBean3, methodDeclaration3, compilationUnit, testFile, true));
        testDatas.add(arguments(methodBean4, methodDeclaration4, compilationUnit, testFile, false));

        return testDatas.stream();
    }
}