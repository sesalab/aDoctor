package adoctor.application.proposal;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.MethodSmell;

import java.io.IOException;
import java.util.ArrayList;

public class ProposalDriver {

    private ArrayList<MethodSmellProposer> methodSmellProposers;

    public ProposalDriver(ArrayList<MethodSmellProposer> methodSmellProposers) {
        this.methodSmellProposers = methodSmellProposers;
    }

    public MethodProposal computeProposal(MethodSmell methodSmell) throws IOException {
        if (methodSmellProposers == null) {
            return null;
        }
        for (MethodSmellProposer proposer : methodSmellProposers) {
            MethodProposal methodProposal = proposer.computeProposal(methodSmell);
            if (methodProposal != null) {
                return methodProposal;
            }
        }
        return null;
    }
}
