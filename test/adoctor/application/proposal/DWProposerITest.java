package adoctor.application.proposal;

import adoctor.application.analysis.AnalysisDriver;
import adoctor.application.analysis.DWAnalyzer;
import adoctor.application.analysis.ERBAnalyzer;
import adoctor.application.analysis.MethodSmellAnalyzer;
import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.DWSmell;
import adoctor.application.bean.smell.ERBSmell;
import adoctor.application.bean.smell.MethodSmell;
import beans.ClassBean;
import beans.MethodBean;
import beans.PackageBean;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import process.FolderToJavaProjectConverter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SuppressWarnings("unchecked")
class DWProposerITest {

    @ParameterizedTest
    @MethodSource("computeProposalProvider")
    void computeProposal(MethodSmell methodSmell, boolean assertNull) throws IOException {
        DWProposer testedProposer = new DWProposer();
        MethodProposal result = testedProposer.computeProposal(methodSmell);
        if (assertNull) {
            assertNull(result);
        } else {
            assertNotNull(result);
        }
    }

    private static Stream<Arguments> computeProposalProvider() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ERBSmell erbSmell = new ERBSmell();
        DWSmell dwSmellInvalid = new DWSmell();
        DWSmell dwSmellValid1 = (DWSmell) getFirstSmell("testResources", "testPackage", "testDW1");
        DWSmell dwSmellValid2 = (DWSmell) getFirstSmell("testResources", "testPackage", "testDW2");
        //TODO inserire altri casi di test

        return Stream.of(
                arguments(null, true),
                arguments(erbSmell, true),
                arguments(dwSmellInvalid, true),
                arguments(dwSmellValid1, false),
                arguments(dwSmellValid2, false)
        );
    }

    private static MethodSmell getFirstSmell(String testDirectoryPath, String testPackageName, String testClassName) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String testFilePath = testDirectoryPath + "/" + testPackageName + "/" + testClassName + ".java";
        File testFile = new File(testFilePath);
        HashMap<String, File> fileHashMap = new HashMap<>();
        fileHashMap.put(testPackageName + "." + testClassName, testFile);

        // Phase 1: ArrayList<PackageBean>
        File testDirectory = new File("testResources");
        ArrayList<PackageBean> packages = FolderToJavaProjectConverter.convert(testDirectory.getAbsolutePath());
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
            if (packageBean.getName().equals(testPackageName)) {
                testPackages.add(packageBean);
            }
        }
        testPackages.get(0).getClasses().removeIf(classBean -> !classBean.getName().equals(testClassName));

        // Phase 2: ArrayList<MethodSmell>
        ArrayList<MethodSmellAnalyzer> methodSmellAnalyzers = new ArrayList<>();
        methodSmellAnalyzers.add(new DWAnalyzer());
        methodSmellAnalyzers.add(new ERBAnalyzer());
        AnalysisDriver analysisDriver = new AnalysisDriver(null, methodSmellAnalyzers, "");
        Method method = AnalysisDriver.class.getDeclaredMethod("analyze", ArrayList.class, HashMap.class);
        method.setAccessible(true);
        ArrayList<MethodSmell> methodSmells = (ArrayList<MethodSmell>) method.invoke(analysisDriver, testPackages, fileHashMap);

        return methodSmells.get(0);
    }
}