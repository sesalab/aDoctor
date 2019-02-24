package adoctor.application.refactoring;

import adoctor.application.bean.proposal.MethodProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.UndoEdit;

import java.io.IOException;
import java.util.ArrayList;

public class RefactoringDriver {

    private MethodProposal methodProposal;
    private ArrayList<MethodSmellRefactorer> methodSmellRefactorers;

    public RefactoringDriver(MethodProposal methodProposal, ArrayList<MethodSmellRefactorer> methodSmellRefactorers) {
        this.methodProposal = methodProposal;
        this.methodSmellRefactorers = methodSmellRefactorers;
    }

    public UndoEdit startRefactoring() throws IOException, BadLocationException {
        if (methodSmellRefactorers == null) {
            return null;
        }
        for (MethodSmellRefactorer refactorer : methodSmellRefactorers) {
            UndoEdit undoEdit = refactorer.applyRefactoring(methodProposal);
            if (undoEdit != null) {
                return undoEdit;
            }
        }
        return null;
    }
}
