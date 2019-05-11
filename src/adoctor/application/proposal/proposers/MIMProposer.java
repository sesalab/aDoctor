package adoctor.application.proposal.proposers;

import adoctor.application.smell.ClassSmell;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class MIMProposer extends ClassSmellProposer {
    @Override
    public ASTRewrite computeProposal(ClassSmell classSmell) {
        return null;
    }
}
