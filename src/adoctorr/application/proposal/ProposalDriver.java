package adoctorr.application.proposal;

import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.bean.smell.MethodSmell;

import java.io.IOException;
import java.util.ArrayList;

public class ProposalDriver {

    public ProposalDriver() {

    }

    public MethodProposal computeProposal(MethodSmell methodSmell) throws IOException {
        //TODO Questo poi cambierà nella CR_RS_1. Sarà preparata La lista completa SmellDialog o si già manda il proposer giusto?
        ArrayList<MethodSmellProposer> methodSmellProposers = new ArrayList<>();
        methodSmellProposers.add(new DWProposer());
        methodSmellProposers.add(new ERBProposer());

        for (MethodSmellProposer proposer : methodSmellProposers) {
            MethodProposal methodProposal = proposer.computeProposal(methodSmell);
            if (methodProposal != null) {
                return methodProposal;
            }
        }
        return null;
    }
}
