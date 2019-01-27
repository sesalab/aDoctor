package adoctorr.application.refactoring;

import adoctorr.application.bean.proposal.MethodProposal;
import org.eclipse.jface.text.BadLocationException;

import java.io.IOException;
import java.util.ArrayList;

public class RefactoringDriver {

    private MethodProposal methodProposal;

    public RefactoringDriver(MethodProposal methodProposal) {
        this.methodProposal = methodProposal;
    }

    public boolean startRefactoring() throws IOException, BadLocationException {
        //TODO Questo poi cambierà nella CR_RS_1. Sarà preparata La lista completa RefactoringDialog o si già manda il refactorer giusto?
        ArrayList<MethodSmellRefactorer> methodSmellRefactorers = new ArrayList<>();
        methodSmellRefactorers.add(new DWRefactorer());
        methodSmellRefactorers.add(new ERBRefactorer());

        for (MethodSmellRefactorer refactorer : methodSmellRefactorers) {
            if (refactorer.applyRefactoring(methodProposal)) {
                return true;
            }
        }
        return false;
    }
}
