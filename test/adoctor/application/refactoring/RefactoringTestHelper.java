package adoctor.application.refactoring;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.MethodSmell;
import adoctor.application.proposal.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class RefactoringTestHelper {

    public static ArrayList<MethodProposal> getMethodProposals(String testDirectory, String testPackage, String testClass) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        ArrayList<MethodSmell> methodSmells = ProposalTestHelper.getMethodSmells(testDirectory, testPackage, testClass);

        // Phase 2: ArrayList<MethodProposal>
        ArrayList<MethodSmellProposer> methodSmellProposers = new ArrayList<>();
        methodSmellProposers.add(new DWProposer());
        methodSmellProposers.add(new ERBProposer());
        ProposalDriver proposalDriver = new ProposalDriver(methodSmellProposers);
        ArrayList<MethodProposal> methodProposals = new ArrayList<>();
        for (MethodSmell methodSmell : methodSmells) {
            methodProposals.add(proposalDriver.computeProposal(methodSmell));
        }
        return methodProposals;
    }
}
