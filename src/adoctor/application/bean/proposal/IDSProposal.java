package adoctor.application.bean.proposal;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class IDSProposal extends MethodProposal {

    private VariableDeclarationStatement proposedVarDecl;

    public VariableDeclarationStatement getProposedVarDecl() {
        return proposedVarDecl;
    }

    public void setProposedVarDecl(VariableDeclarationStatement proposedVarDecl) {
        this.proposedVarDecl = proposedVarDecl;
    }
}
