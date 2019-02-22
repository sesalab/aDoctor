package adoctor.application.bean.proposal;

import adoctor.application.bean.smell.MethodSmell;

import java.util.ArrayList;

public abstract class MethodProposal extends Proposal {
    private MethodSmell methodSmell;
    private String proposedCode;
    private ArrayList<String> currentHighlights;
    private ArrayList<String> proposedHighlights;

    MethodProposal() {
    }

    public MethodSmell getMethodSmell() {
        return methodSmell;
    }

    public void setMethodSmell(MethodSmell methodSmell) {
        this.methodSmell = methodSmell;
    }

    public String getProposedCode() {
        return proposedCode;
    }

    public void setProposedCode(String proposedCode) {
        this.proposedCode = proposedCode;
    }

    public ArrayList<String> getCurrentHighlights() {
        return currentHighlights;
    }

    public void setCurrentHighlights(ArrayList<String> currentHighlights) {
        this.currentHighlights = currentHighlights;
    }

    public ArrayList<String> getProposedHighlights() {
        return proposedHighlights;
    }

    public void setProposedHighlights(ArrayList<String> proposedHighlights) {
        this.proposedHighlights = proposedHighlights;
    }
}
