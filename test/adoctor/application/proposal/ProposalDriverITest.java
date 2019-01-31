package adoctor.application.proposal;

import adoctor.application.bean.proposal.MethodProposal;
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

class ProposalDriverITest {

    private static String testDirectory = "testResources";
    private static String testPackage = "testPackage";
    private static String testClass = "testDW_ERB1";

    @ParameterizedTest
    @MethodSource("computeProposalProvider")
    void computeProposal(ArrayList<MethodSmellProposer> methodSmellProposers, MethodSmell methodSmell, boolean shouldBeNull) throws IOException {
        ProposalDriver testedProposalDriver = new ProposalDriver(methodSmellProposers);
        MethodProposal result = testedProposalDriver.computeProposal(methodSmell);
        if (shouldBeNull) {
            assertNull(result);
        } else {
            assertEquals(methodSmell, result.getMethodSmell());
        }
    }

    private static Stream<Arguments> computeProposalProvider() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ArrayList<MethodSmell> methodSmells = ProposalTestHelper.getMethodSmells(testDirectory, testPackage, testClass);
        MethodSmell dwSmell = methodSmells.get(1);
        MethodSmell erbSmell = methodSmells.get(0);

        DWProposer dwProposer = new DWProposer();
        ERBProposer erbProposer = new ERBProposer();
        ArrayList<MethodSmellProposer> proposersEmpty = new ArrayList<>();
        ArrayList<MethodSmellProposer> proposersDW = new ArrayList<>();
        proposersDW.add(dwProposer);
        ArrayList<MethodSmellProposer> proposersERB = new ArrayList<>();
        proposersERB.add(erbProposer);
        ArrayList<MethodSmellProposer> proposersDW_ERB = new ArrayList<>();
        proposersDW_ERB.add(dwProposer);
        proposersDW_ERB.add(erbProposer);
        ArrayList<MethodSmellProposer> proposersERB_DW = new ArrayList<>();
        proposersERB_DW.add(erbProposer);
        proposersERB_DW.add(dwProposer);

        return Stream.of(
                arguments(null, null, true),
                arguments(proposersEmpty, null, true),
                arguments(proposersDW, null, true),
                arguments(proposersDW, dwSmell, false),
                arguments(proposersDW, erbSmell, true),
                arguments(proposersERB, null, true),
                arguments(proposersERB, dwSmell, true),
                arguments(proposersERB, erbSmell, false),
                arguments(proposersDW_ERB, null, true),
                arguments(proposersDW_ERB, dwSmell, false),
                arguments(proposersDW_ERB, erbSmell, false),
                arguments(proposersERB_DW, null, true),
                arguments(proposersERB_DW, dwSmell, false),
                arguments(proposersERB_DW, erbSmell, false)
        );
    }

}