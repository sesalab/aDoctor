package adoctorr.application.bean.proposal;


import adoctorr.application.bean.smell.MethodSmell;

import java.util.ArrayList;

public class MethodProposal {
    private MethodSmell methodSmell;
    private ArrayList<String> actualCodeToHighlightList;
    private ArrayList<String> proposedCodeToHighlightList;

    MethodProposal() {
    }

    public ArrayList<String> getActualCodeToHighlightList() {
        return actualCodeToHighlightList;
    }

    public void setActualCodeToHighlightList(ArrayList<String> actualCodeToHighlightList) {
        this.actualCodeToHighlightList = actualCodeToHighlightList;
    }

    public ArrayList<String> getProposedCodeToHighlightList() {
        return proposedCodeToHighlightList;
    }

    public void setProposedCodeToHighlightList(ArrayList<String> proposedCodeToHighlightList) {
        this.proposedCodeToHighlightList = proposedCodeToHighlightList;
    }

    public MethodSmell getMethodSmell() {
        return methodSmell;
    }

    public void setMethodSmell(MethodSmell methodSmell) {
        this.methodSmell = methodSmell;
    }
}
