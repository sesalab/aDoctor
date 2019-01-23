package adoctorr.application.bean.proposal;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class DurableWakelockProposalMethodBean extends ProposalMethodBean {

    private MethodDeclaration proposedMethodDeclaration;

    public DurableWakelockProposalMethodBean() {
    }

    public MethodDeclaration getProposedMethodDeclaration() {
        return proposedMethodDeclaration;
    }

    public void setProposedMethodDeclaration(MethodDeclaration proposedMethodDeclaration) {
        this.proposedMethodDeclaration = proposedMethodDeclaration;
    }

}
