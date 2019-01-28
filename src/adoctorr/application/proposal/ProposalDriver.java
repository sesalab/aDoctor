package adoctorr.application.proposal;

import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.bean.smell.MethodSmell;

import java.io.IOException;
import java.util.ArrayList;

public class ProposalDriver {

    private ArrayList<MethodSmellProposer> methodSmellProposers;

    public ProposalDriver(ArrayList<MethodSmellProposer> methodSmellProposers) {
        this.methodSmellProposers = methodSmellProposers;
    }

    public MethodProposal computeProposal(MethodSmell methodSmell) throws IOException {
        for (MethodSmellProposer proposer : methodSmellProposers) {
            MethodProposal methodProposal = proposer.computeProposal(methodSmell);
            if (methodProposal != null) {
                return methodProposal;
            }
        }
        return null;
    }
}
