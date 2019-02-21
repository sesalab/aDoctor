package adoctor.application.bean.proposal;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class DWProposal extends MethodProposal {

    private MethodDeclaration proposedMethodDeclaration;

    public DWProposal() {
    }

    public MethodDeclaration getProposedMethodDeclaration() {
        return proposedMethodDeclaration;
    }

    public void setProposedMethodDeclaration(MethodDeclaration proposedMethodDeclaration) {
        this.proposedMethodDeclaration = proposedMethodDeclaration;
    }
}
