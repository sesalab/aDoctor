package adoctor.application.proposal;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.MethodSmell;

import java.io.IOException;

public abstract class MethodSmellProposer {

    public abstract MethodProposal computeProposal(MethodSmell methodSmell) throws IOException;

}
