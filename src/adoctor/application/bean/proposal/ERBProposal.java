package adoctor.application.bean.proposal;


import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ERBProposal extends MethodProposal {

    private MethodDeclaration proposedOnCreate;
    private MethodDeclaration currentOnResume;
    private MethodDeclaration proposedOnResume;

    public MethodDeclaration getCurrentOnResume() {
        return currentOnResume;
    }

    public void setCurrentOnResume(MethodDeclaration currentOnResume) {
        this.currentOnResume = currentOnResume;
    }

    public MethodDeclaration getProposedOnCreate() {
        return proposedOnCreate;
    }

    public void setProposedOnCreate(MethodDeclaration proposedOnCreate) {
        this.proposedOnCreate = proposedOnCreate;
    }

    public MethodDeclaration getProposedOnResume() {
        return proposedOnResume;
    }

    public void setProposedOnResume(MethodDeclaration proposedOnResume) {
        this.proposedOnResume = proposedOnResume;
    }
}
