package adoctor.application.bean.proposal;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class IDSProposal extends MethodProposal {

    private MethodDeclaration proposedMethod;

    public MethodDeclaration getProposedMethodDeclaration() {
        return proposedMethod;
    }

    public void setProposedMethodDeclaration(MethodDeclaration proposedMethod) {
        this.proposedMethod = proposedMethod;
    }

    @Override
    public String proposalToString() {
        return proposedMethod.toString();
    }
}
