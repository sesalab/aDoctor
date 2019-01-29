package adoctor.application.proposal;

import adoctor.application.bean.proposal.DWProposal;
import adoctor.application.bean.proposal.ERBProposal;
import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.DWSmell;
import adoctor.application.bean.smell.ERBSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ProposalDriverTest {

    /*
    @BeforeAll
    static void init() {

    }

    @AfterAll
    static void destroy() {

    }

    @BeforeEach
    void setUp() {
        //MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void tearDown() {

    }
    */

    /**
     * Test if:
     * null is returned when no correct proposers for the input are available;
     * not null is returned when at least one correct proposer for the input is available.
     *
     * @param methodSmellProposers test data #1, the ArrayList of proposers
     * @param methodSmell          test data #2, the input smell
     * @param oracle               oracle
     */
    @ParameterizedTest
    @MethodSource("computeProposalProvider")
    void computeProposal(ArrayList<MethodSmellProposer> methodSmellProposers, MethodSmell methodSmell, MethodProposal oracle) throws IOException {
        ProposalDriver testedProposalDriver = new ProposalDriver(methodSmellProposers);
        MethodProposal result = testedProposalDriver.computeProposal(methodSmell);
        assertEquals(result, oracle);
    }

    private static Stream<Arguments> computeProposalProvider() throws IOException {
        DWProposal dwOracle = new DWProposal();
        ERBProposal erbOracle = new ERBProposal();

        MethodSmell dwSmell = new DWSmell();
        MethodSmell erbSmell = new ERBSmell();

        DWProposer dwProposer = mock(DWProposer.class);
        when(dwProposer.computeProposal(dwSmell)).thenReturn(dwOracle);
        ERBProposer erbProposer = mock(ERBProposer.class);
        when(erbProposer.computeProposal(erbSmell)).thenReturn(erbOracle);

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
                arguments(null, null, null),
                arguments(proposersEmpty, null, null),
                arguments(proposersDW, null, null),
                arguments(proposersDW, dwSmell, dwOracle),
                arguments(proposersDW, erbSmell, null),
                arguments(proposersERB, null, null),
                arguments(proposersERB, dwSmell, null),
                arguments(proposersERB, erbSmell, erbOracle),
                arguments(proposersDW_ERB, null, null),
                arguments(proposersDW_ERB, dwSmell, dwOracle),
                arguments(proposersDW_ERB, erbSmell, erbOracle),
                arguments(proposersERB_DW, null, null),
                arguments(proposersERB_DW, dwSmell, dwOracle),
                arguments(proposersERB_DW, erbSmell, erbOracle)
        );
    }
}