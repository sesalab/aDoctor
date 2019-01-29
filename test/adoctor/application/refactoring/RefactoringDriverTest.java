package adoctor.application.refactoring;

import adoctor.application.bean.proposal.DWProposal;
import adoctor.application.bean.proposal.ERBProposal;
import adoctor.application.bean.proposal.MethodProposal;
import org.eclipse.jface.text.BadLocationException;
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
class RefactoringDriverTest {

    @ParameterizedTest
    @MethodSource("computeProposalProvider")
    void startRefactoring(ArrayList<MethodSmellRefactorer> methodSmellRefactorers, MethodProposal methodProposal, boolean oracle) throws IOException, BadLocationException {
        RefactoringDriver testedRefactoringDriver = new RefactoringDriver(methodProposal, methodSmellRefactorers);
        boolean result = testedRefactoringDriver.startRefactoring();
        assertEquals(result, oracle);
    }

    private static Stream<Arguments> computeProposalProvider() throws IOException, BadLocationException {
        DWProposal dwProposal = new DWProposal();
        ERBProposal erbProposal = new ERBProposal();

        DWRefactorer dwRefactorer = mock(DWRefactorer.class);
        when(dwRefactorer.applyRefactoring(dwProposal)).thenReturn(true);
        ERBRefactorer erbRefactorer = mock(ERBRefactorer.class);
        when(erbRefactorer.applyRefactoring(erbProposal)).thenReturn(true);

        ArrayList<MethodSmellRefactorer> refactorersEmpty = new ArrayList<>();
        ArrayList<MethodSmellRefactorer> refactorersDW = new ArrayList<>();
        refactorersDW.add(dwRefactorer);
        ArrayList<MethodSmellRefactorer> refactorersERB = new ArrayList<>();
        refactorersERB.add(erbRefactorer);
        ArrayList<MethodSmellRefactorer> refactorersDW_ERB = new ArrayList<>();
        refactorersDW_ERB.add(dwRefactorer);
        refactorersDW_ERB.add(erbRefactorer);
        ArrayList<MethodSmellRefactorer> refactorersERB_DW = new ArrayList<>();
        refactorersERB_DW.add(erbRefactorer);
        refactorersERB_DW.add(dwRefactorer);

        return Stream.of(
                arguments(null, null, false),
                arguments(refactorersEmpty, null, false),
                arguments(refactorersDW, null, false),
                arguments(refactorersDW, dwProposal, true),
                arguments(refactorersDW, erbProposal, false),
                arguments(refactorersERB, null, false),
                arguments(refactorersERB, dwProposal, false),
                arguments(refactorersERB, erbProposal, true),
                arguments(refactorersDW_ERB, null, false),
                arguments(refactorersDW_ERB, dwProposal, true),
                arguments(refactorersDW_ERB, erbProposal, true),
                arguments(refactorersERB_DW, null, false),
                arguments(refactorersERB_DW, dwProposal, true),
                arguments(refactorersERB_DW, erbProposal, true)
        );
    }
}