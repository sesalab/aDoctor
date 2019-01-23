package adoctorr.application.proposal;

import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.bean.smell.DWSmell;
import adoctorr.application.bean.smell.ERBSmell;
import adoctorr.application.bean.smell.MethodSmell;

import java.io.IOException;

public class ProposalDriver {

    public ProposalDriver() {

    }

    public MethodProposal computeProposal(MethodSmell methodSmell) throws IOException {
        if (methodSmell != null) {
            int smellType = methodSmell.getSmellType();
            MethodProposal proposedMethodBean;
            switch (smellType) {
                case MethodSmell.DURABLE_WAKELOCK: {
                    DWProposer DWProposer = new DWProposer();
                    DWSmell DWSmell = (DWSmell) methodSmell;
                    proposedMethodBean = DWProposer.computeProposal(DWSmell);
                    break;
                }
                case MethodSmell.EARLY_RESOURCE_BINDING: {
                    ERBProposer ERBProposer = new ERBProposer();
                    ERBSmell ERBSmell = (ERBSmell) methodSmell;
                    proposedMethodBean = ERBProposer.computeProposal(ERBSmell);
                    break;
                }
                default:
                    return null;
            }
            return proposedMethodBean;
        }
        return null;
    }
}
