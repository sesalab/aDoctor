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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ERBProposerITest {

    private static String testDirectory = "testResources";
    private static String testPackage = "testPackage";
    private static String testClass = "testDW_ERB1";

    @ParameterizedTest
    @MethodSource("computeProposalProvider")
    void computeProposal(MethodSmell methodSmell, boolean shouldBeNull) throws IOException {
        ERBProposer testedProposer = new ERBProposer();
        MethodProposal result = testedProposer.computeProposal(methodSmell);
        if (shouldBeNull) {
            assertNull(result);
        } else {
            assertEquals(methodSmell, result.getMethodSmell());
        }
    }

    private static Stream<Arguments> computeProposalProvider() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ArrayList<MethodSmell> methodSmells = ProposalTestHelper.getMethodSmells(testDirectory, testPackage, testClass);
        DWSmell dwSmellInvalid = new DWSmell();
        DWSmell dwSmellValid1 = (DWSmell) methodSmells.get(1);
        DWSmell dwSmellValid2 = (DWSmell) methodSmells.get(2);
        ERBSmell erbSmellInvalid = new ERBSmell();
        ERBSmell erbSmellValid0 = (ERBSmell) methodSmells.get(0);

        return Stream.of(
                arguments(null, true),
                arguments(dwSmellInvalid, true),
                arguments(dwSmellValid1, true),
                arguments(dwSmellValid2, true),
                arguments(erbSmellInvalid, true),
                arguments(erbSmellValid0, false)
        );
    }
}