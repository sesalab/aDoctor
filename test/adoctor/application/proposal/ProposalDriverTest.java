package adoctor.application.proposal;

import adoctor.application.bean.smell.MethodSmell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ProposalDriverTest {

    private ProposalDriver testedProposalDriver;

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

    @ParameterizedTest
    @MethodSource("computeProposalProvider")
    void computeProposal(MethodSmell methodSmell) {
        ArrayList<MethodSmellProposer> methodSmellProposers = new ArrayList<>();
        methodSmellProposers.add(Mockito.mock(DWProposer.class));
        methodSmellProposers.add(Mockito.mock(ERBProposer.class));

        testedProposalDriver = new ProposalDriver(methodSmellProposers);
    }

    private static Stream<Arguments> computeProposalProvider() {
        MethodSmell methodSmell1 = Mockito.mock(MethodSmell.class);
        Mockito.when(methodSmell1.getSmellName()).thenReturn("Ciao");
        //methodSmell1.setSmellName("Ciao");
        System.out.println(methodSmell1.getSmellName());
        return Stream.of(
                arguments(methodSmell1)
        );
    }
}