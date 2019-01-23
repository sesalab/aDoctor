package adoctorr.application.proposal;

import adoctorr.application.bean.proposal.ProposalMethodBean;
import adoctorr.application.bean.smell.DurableWakelockSmellMethodBean;
import adoctorr.application.bean.smell.EarlyResourceBindingSmellMethodBean;
import adoctorr.application.bean.smell.SmellMethodBean;

import java.io.IOException;

public class Proposer {

    public Proposer() {

    }

    public ProposalMethodBean computeProposal(SmellMethodBean smellMethodBean) throws IOException {
        if (smellMethodBean != null) {
            int smellType = smellMethodBean.getSmellType();
            ProposalMethodBean proposedMethodBean;
            switch (smellType) {
                case SmellMethodBean.DURABLE_WAKELOCK: {
                    DurableWakelockProposer durableWakelockProposer = new DurableWakelockProposer();
                    DurableWakelockSmellMethodBean durableWakelockSmellMethodBean = (DurableWakelockSmellMethodBean) smellMethodBean;
                    proposedMethodBean = durableWakelockProposer.computeProposal(durableWakelockSmellMethodBean);
                    break;
                }
                case SmellMethodBean.EARLY_RESOURCE_BINDING: {
                    EarlyResourceBindingProposer earlyResourceBindingProposer = new EarlyResourceBindingProposer();
                    EarlyResourceBindingSmellMethodBean earlyResourceBindingSmellMethodBean = (EarlyResourceBindingSmellMethodBean) smellMethodBean;
                    proposedMethodBean = earlyResourceBindingProposer.computeProposal(earlyResourceBindingSmellMethodBean);
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
