package adoctorr.application.proposal;

import adoctorr.application.ast.ASTUtilities;
import adoctorr.application.bean.proposal.DurableWakelockProposalMethodBean;
import adoctorr.application.bean.smell.DurableWakelockSmellMethodBean;
import beans.MethodBean;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DurableWakelockProposer {

    DurableWakelockProposer() {

    }

    public DurableWakelockProposalMethodBean computeProposal(DurableWakelockSmellMethodBean smellMethodBean) throws IOException {
        if (smellMethodBean != null) {
            File sourceFile = smellMethodBean.getSourceFile();
            MethodBean methodBean = smellMethodBean.getMethodBean();

            CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(sourceFile);
            MethodDeclaration methodDeclaration = ASTUtilities.getMethodDeclarationFromContent(methodBean.getTextContent(), compilationUnit);
            if (methodDeclaration != null) {
                Statement acquireStatement = smellMethodBean.getAcquireStatement();
                if (acquireStatement != null) {
                    AST targetAST = compilationUnit.getAST();
                    MethodInvocation releaseMethodInvocation = targetAST.newMethodInvocation();

                    // This is done in order to get the wakelock identifier
                    ExpressionStatement acquireExpressionStatement = (ExpressionStatement) acquireStatement;
                    Expression acquireExpression = acquireExpressionStatement.getExpression();
                    MethodInvocation acquireMethodInvocation = (MethodInvocation) acquireExpression;
                    releaseMethodInvocation.setExpression((Expression) ASTNode.copySubtree(targetAST, acquireMethodInvocation.getExpression()));

                    SimpleName releaseSimpleName = targetAST.newSimpleName("release");
                    releaseMethodInvocation.setName(releaseSimpleName);

                    // Wrap the MethodInvocation in an ExpressionStatement
                    ExpressionStatement releaseExpressionStatement = targetAST.newExpressionStatement(releaseMethodInvocation);

                    // If the scope is the method, then add it to the end of the method
                    Block acquireBlock = ASTUtilities.getBlockFromContent(smellMethodBean.getAcquireBlock().toString(), methodDeclaration);
                    List<Statement> statementList = (List<Statement>) acquireBlock.statements();
                    statementList.add(releaseExpressionStatement);

                    ArrayList<String> proposedCodeToHighlightList = new ArrayList<>();
                    proposedCodeToHighlightList.add(releaseExpressionStatement.toString());

                    DurableWakelockProposalMethodBean proposalMethodBean = new DurableWakelockProposalMethodBean();
                    proposalMethodBean.setSmellMethodBean(smellMethodBean);
                    proposalMethodBean.setProposedMethodDeclaration(methodDeclaration);
                    proposalMethodBean.setProposedCodeToHighlightList(proposedCodeToHighlightList);
                    return proposalMethodBean;
                }
            }
        }
        return null;
    }
}
