package adoctor.application.refactoring;

import adoctor.application.bean.proposal.DWProposal;
import adoctor.application.bean.proposal.ERBProposal;
import adoctor.application.bean.proposal.MethodProposal;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DWRefactorerITest {

    private static String testDirectory = "testResources";
    private static String testPackage = "testPackage";
    private static String testClass = "testDW_ERB1";

    @ParameterizedTest
    @MethodSource("applyRefactoringProvider")
    void applyRefactoring(MethodProposal methodProposal, boolean oracle) throws IOException, BadLocationException {
        DWRefactorer testedRefactorer = new DWRefactorer();
        boolean result = testedRefactorer.applyRefactoring(methodProposal);
        assertEquals(result, oracle);
    }

    private static Stream<Arguments> applyRefactoringProvider() throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        ArrayList<MethodProposal> methodProposals = RefactoringTestHelper.getMethodProposals(testDirectory, testPackage, testClass);

        ERBProposal erbProposalInvalid = new ERBProposal();
        ERBProposal erbProposalValid0 = (ERBProposal) methodProposals.get(0);
        DWProposal dwProposalInvalid = new DWProposal();
        DWProposal dwProposalValid1 = (DWProposal) methodProposals.get(1);
        DWProposal dwProposalValid2 = (DWProposal) methodProposals.get(2);

        return Stream.of(
                arguments(null, false),
                arguments(erbProposalInvalid, false),
                arguments(erbProposalValid0, false),
                arguments(dwProposalInvalid, false),
                arguments(dwProposalValid1, true),
                arguments(dwProposalValid2, true)
        );
    }
}