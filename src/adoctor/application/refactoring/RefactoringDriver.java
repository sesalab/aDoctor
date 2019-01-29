package adoctor.application.refactoring;

import adoctor.application.bean.proposal.MethodProposal;
import org.eclipse.jface.text.BadLocationException;

import java.io.IOException;
import java.util.ArrayList;

public class RefactoringDriver {

    private MethodProposal methodProposal;
    private ArrayList<MethodSmellRefactorer> methodSmellRefactorers;

    public RefactoringDriver(MethodProposal methodProposal, ArrayList<MethodSmellRefactorer> methodSmellRefactorers) {
        this.methodProposal = methodProposal;
        this.methodSmellRefactorers = methodSmellRefactorers;
    }

    public boolean startRefactoring() throws IOException, BadLocationException {
        for (MethodSmellRefactorer refactorer : methodSmellRefactorers) {
            if (refactorer.applyRefactoring(methodProposal)) {
                return true;
            }
        }
        return false;
    }
}
