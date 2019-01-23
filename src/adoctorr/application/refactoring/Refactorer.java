package adoctorr.application.refactoring;

import adoctorr.application.bean.proposal.DurableWakelockProposalMethodBean;
import adoctorr.application.bean.proposal.EarlyResourceBindingProposalMethodBean;
import adoctorr.application.bean.proposal.ProposalMethodBean;
import adoctorr.application.bean.smell.SmellMethodBean;
import org.eclipse.jface.text.BadLocationException;

import java.io.IOException;

public class Refactorer {

    public Refactorer() {

    }

    // Refactoring and overwrite the file
    public boolean applyRefactoring(ProposalMethodBean proposalMethodBean) throws IOException, BadLocationException {
        boolean result = false;
        if (proposalMethodBean != null) {
            int smellType = proposalMethodBean.getSmellMethodBean().getSmellType();
            switch (smellType) {
                case SmellMethodBean.DURABLE_WAKELOCK: {
                    DurableWakelockProposalMethodBean durableWakelockProposalMethodBean = (DurableWakelockProposalMethodBean) proposalMethodBean;
                    DurableWakelockRefactorer durableWakelockRefactorer = new DurableWakelockRefactorer();
                    result = durableWakelockRefactorer.applyRefactor(durableWakelockProposalMethodBean);
                    break;
                }
                case SmellMethodBean.EARLY_RESOURCE_BINDING: {
                    EarlyResourceBindingProposalMethodBean earlyResourceBindingProposalMethodBean = (EarlyResourceBindingProposalMethodBean) proposalMethodBean;
                    EarlyResourceBindingRefactorer earlyResourceBindingRefactorer = new EarlyResourceBindingRefactorer();
                    result = earlyResourceBindingRefactorer.applyRefactor(earlyResourceBindingProposalMethodBean);
                    break;
                }
                default:
                    break;
            }
        }
        return result;
    }
}
