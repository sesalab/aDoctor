package adoctor.application.proposal;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.DWSmell;
import adoctor.application.bean.smell.ERBSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ERBProposerITest {

    @ParameterizedTest
    @MethodSource("computeProposalProvider")
    void computeProposal(MethodSmell methodSmell, MethodProposal oracle) throws IOException {
        ERBProposer testedProposer = new ERBProposer();
        MethodProposal result = testedProposer.computeProposal(methodSmell);
        assertEquals(result, oracle);
    }

    private static Stream<Arguments> computeProposalProvider() {
        DWSmell dwSmell = new DWSmell();
        ERBSmell erbSmellInvalid = new ERBSmell();

        return Stream.of(
                arguments(null, null),
                arguments(dwSmell, null),
                arguments(erbSmellInvalid, null)
        );
    }
}