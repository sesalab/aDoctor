package adoctorr.application.refactoring;

import adoctorr.application.bean.proposal.MethodProposal;
import org.eclipse.jface.text.BadLocationException;

import java.io.IOException;

public abstract class MethodSmellRefactorer {

    public abstract boolean applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException;
}
