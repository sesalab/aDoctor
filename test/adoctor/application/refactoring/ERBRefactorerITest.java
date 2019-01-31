package adoctor.application.refactoring;

import adoctor.application.bean.proposal.DWProposal;
import adoctor.application.bean.proposal.ERBProposal;
import adoctor.application.bean.proposal.MethodProposal;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ERBRefactorerITest {

    @ParameterizedTest
    @MethodSource("applyRefactoringProvider")
    void applyRefactoring(MethodProposal methodProposal, boolean oracle) throws IOException, BadLocationException {
        ERBRefactorer testedRefactorer = new ERBRefactorer();
        boolean result = testedRefactorer.applyRefactoring(methodProposal);
        assertEquals(result, oracle);
    }

    private static Stream<Arguments> applyRefactoringProvider() {
        DWProposal dwProposal = new DWProposal();
        ERBProposal erbProposalInvalid = new ERBProposal();

        return Stream.of(
                arguments(null, false),
                arguments(dwProposal, false),
                arguments(erbProposalInvalid, false)
        );
    }
}