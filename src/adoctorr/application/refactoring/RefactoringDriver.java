package adoctorr.application.refactoring;

import adoctorr.application.bean.proposal.DWProposal;
import adoctorr.application.bean.proposal.ERBProposal;
import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.bean.smell.MethodSmell;
import org.eclipse.jface.text.BadLocationException;

import java.io.IOException;

public class RefactoringDriver {

    public RefactoringDriver() {

    }

    // Refactoring and overwrite the file
    public boolean applyRefactoring(MethodProposal methodProposal) throws IOException, BadLocationException {
        boolean result = false;
        if (methodProposal != null) {
            int smellType = methodProposal.getMethodSmell().getSmellType();
            switch (smellType) {
                case MethodSmell.DURABLE_WAKELOCK: {
                    DWProposal DWProposal = (DWProposal) methodProposal;
                    DWRefactorer DWRefactorer = new DWRefactorer();
                    result = DWRefactorer.applyRefactor(DWProposal);
                    break;
                }
                case MethodSmell.EARLY_RESOURCE_BINDING: {
                    ERBProposal ERBProposal = (ERBProposal) methodProposal;
                    ERBRefactorer ERBRefactorer = new ERBRefactorer();
                    result = ERBRefactorer.applyRefactor(ERBProposal);
                    break;
                }
                default:
                    break;
            }
        }
        return result;
    }
}
