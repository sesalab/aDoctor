package adoctorr.application.bean.proposal;


import adoctorr.application.bean.smell.SmellMethodBean;

import java.util.ArrayList;

public class ProposalMethodBean {
    private SmellMethodBean smellMethodBean;
    private ArrayList<String> actualCodeToHighlightList;
    private ArrayList<String> proposedCodeToHighlightList;

    ProposalMethodBean() {
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

    public SmellMethodBean getSmellMethodBean() {
        return smellMethodBean;
    }

    public void setSmellMethodBean(SmellMethodBean smellMethodBean) {
        this.smellMethodBean = smellMethodBean;
    }
}
