package adoctor.application.proposal;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.DWSmell;
import adoctor.application.bean.smell.ERBSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DWProposerITest {

    private static String testDirectory = "testResources";
    private static String testPackage = "testPackage";
    private static String testClass = "testDW_ERB1";

    @ParameterizedTest
    @MethodSource("computeProposalProvider")
    void computeProposal(MethodSmell methodSmell, boolean shouldBeNull) throws IOException {
        DWProposer testedProposer = new DWProposer();
        MethodProposal result = testedProposer.computeProposal(methodSmell);
        if (shouldBeNull) {
            assertNull(result);
        } else {
            assertEquals(methodSmell, result.getMethodSmell());
        }
    }

    private static Stream<Arguments> computeProposalProvider() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ArrayList<MethodSmell> methodSmells = ProposalTestHelper.getMethodSmells(testDirectory, testPackage, testClass);
        ERBSmell erbSmellInvalid = new ERBSmell();
        ERBSmell erbSmellValid0 = (ERBSmell) methodSmells.get(0);
        DWSmell dwSmellInvalid = new DWSmell();
        DWSmell dwSmellValid1 = (DWSmell) methodSmells.get(1);
        DWSmell dwSmellValid2 = (DWSmell) methodSmells.get(2);

        return Stream.of(
                arguments(null, true),
                arguments(erbSmellInvalid, true),
                arguments(erbSmellValid0, true),
                arguments(dwSmellInvalid, true),
                arguments(dwSmellValid1, false),
                arguments(dwSmellValid2, false)
        );
    }
}