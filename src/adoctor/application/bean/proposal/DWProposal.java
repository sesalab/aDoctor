package adoctor.application.bean.proposal;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class DWProposal extends MethodProposal {

    private MethodDeclaration proposedMethodDecl;

    public MethodDeclaration getProposedMethodDecl() {
        return proposedMethodDecl;
    }

    public void setProposedMethodDecl(MethodDeclaration proposedMethodDecl) {
        this.proposedMethodDecl = proposedMethodDecl;
    }
}
