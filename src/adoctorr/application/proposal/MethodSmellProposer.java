package adoctorr.application.proposal;

import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.bean.smell.MethodSmell;

import java.io.IOException;

public abstract class MethodSmellProposer {

    public abstract MethodProposal computeProposal(MethodSmell methodSmell) throws IOException;

}
