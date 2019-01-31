package adoctor.application.analysis;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.smell.MethodSmell;
import beans.ClassBean;
import beans.MethodBean;
import beans.PackageBean;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import process.FolderToJavaProjectConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ERBAnalyzerITest {

    @ParameterizedTest
    @MethodSource("analyzerMethodProvider")
    void analyzeMethod(MethodBean methodBean, MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, File sourceFile, boolean shouldBeSmell) {
        ERBAnalyzer testedAnalyzer = new ERBAnalyzer();
        MethodSmell result = testedAnalyzer.analyzeMethod(methodBean, methodDeclaration, compilationUnit, sourceFile);
        if (shouldBeSmell) {
            assertEquals(result.getMethodBean(), methodBean);
        } else {
            assertNull(result);
        }
    }

    private static Stream analyzerMethodProvider() throws IOException {
        ArrayList<Arguments> testDatas = new ArrayList<>();
        testDatas.add(arguments(null, null, null, null, false));

        String testDirectory = "testResources";
        String testPackage = "testPackage";
        String testClass = "testAnalyzers";

        String testFilePath = testDirectory + "/" + testPackage + "/" + testClass + ".java";
        File testFile = new File(testFilePath);
        CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(testFile);

        // Add a test data for each Method in tested class
        ArrayList<MethodBean> methodBeans = getMethodBeans(testDirectory, testPackage, testClass);
        MethodBean methodBean1 = methodBeans.get(0);
        MethodBean methodBean2 = methodBeans.get(1);
        MethodBean methodBean3 = methodBeans.get(2);
        MethodBean methodBean4 = methodBeans.get(3);

        MethodDeclaration methodDeclaration1 = ASTUtilities.getMethodDeclarationFromContent(methodBean1.getTextContent(), compilationUnit);
        MethodDeclaration methodDeclaration2 = ASTUtilities.getMethodDeclarationFromContent(methodBean2.getTextContent(), compilationUnit);
        MethodDeclaration methodDeclaration3 = ASTUtilities.getMethodDeclarationFromContent(methodBean3.getTextContent(), compilationUnit);
        MethodDeclaration methodDeclaration4 = ASTUtilities.getMethodDeclarationFromContent(methodBean4.getTextContent(), compilationUnit);

        testDatas.add(arguments(methodBean1, methodDeclaration1, compilationUnit, testFile, true));
        testDatas.add(arguments(methodBean2, methodDeclaration2, compilationUnit, testFile, false));
        testDatas.add(arguments(methodBean3, methodDeclaration3, compilationUnit, testFile, false));
        testDatas.add(arguments(methodBean4, methodDeclaration4, compilationUnit, testFile, false));

        return testDatas.stream();
    }

    private static ArrayList<MethodBean> getMethodBeans(String testDirectoryPath, String testPackageName, String testClassName) throws IOException {
        // Phase 1: ArrayList<PackageBean>
        File testDirectory = new File(testDirectoryPath);
        ArrayList<PackageBean> totalPackages = FolderToJavaProjectConverter.convert(testDirectory.getAbsolutePath());
        // belongingClass was not set in aDoctor API: this is just a fix
        for (PackageBean packageBean : totalPackages) {
            for (ClassBean classBean : packageBean.getClasses()) {
                for (MethodBean methodBean : classBean.getMethods()) {
                    methodBean.setBelongingClass(classBean);
                }
            }
        }

        // Phase 2: Removal of useless packages and classes
        ArrayList<PackageBean> testPackages = new ArrayList<>();
        for (PackageBean packageBean : totalPackages) {
            if (packageBean.getName().equals(testPackageName)) {
                testPackages.add(packageBean);
            }
        }
        testPackages.get(0).getClasses().removeIf(classBean -> !classBean.getName().equals(testClassName));

        // Phase 3: Building of a single list of MethodBeans
        ArrayList<MethodBean> methodBeans = new ArrayList<>();
        for (PackageBean packageBean : testPackages) {
            for (ClassBean classBean : packageBean.getClasses()) {
                methodBeans.addAll(classBean.getMethods());
            }
        }
        return methodBeans;
    }
}