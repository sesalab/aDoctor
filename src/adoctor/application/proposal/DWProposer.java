package adoctor.application.proposal;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.proposal.DWProposal;
import adoctor.application.bean.smell.DWSmell;
import adoctor.application.bean.smell.MethodSmell;
import beans.MethodBean;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class DWProposer extends MethodSmellProposer {

    public DWProposer() {

    }

    @Override
    public DWProposal computeProposal(MethodSmell methodSmell) throws IOException {
        if (!(methodSmell instanceof DWSmell)) {
            return null;
        }
        DWSmell dwSmell = (DWSmell) methodSmell;
        File sourceFile = dwSmell.getSourceFile();
        MethodBean methodBean = dwSmell.getMethodBean();
        CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(sourceFile);
        MethodDeclaration methodDeclaration = ASTUtilities.getMethodDeclarationFromContent(methodBean.getTextContent(), compilationUnit);
        if (methodDeclaration != null) {
            Statement acquireStatement = dwSmell.getAcquireStatement();
            if (acquireStatement != null) {
                AST targetAST = compilationUnit.getAST();

                MethodInvocation releaseMethodInvocation = targetAST.newMethodInvocation();
                // This is done in order to get the wakelock identifier
                ExpressionStatement acquireExpressionStatement = (ExpressionStatement) acquireStatement;
                Expression acquireExpression = acquireExpressionStatement.getExpression();
                MethodInvocation acquireMethodInvocation = (MethodInvocation) acquireExpression;
                releaseMethodInvocation.setExpression((Expression) ASTNode.copySubtree(targetAST, acquireMethodInvocation.getExpression()));

                SimpleName releaseSimpleName = targetAST.newSimpleName(DWSmell.RELEASE_NAME);
                releaseMethodInvocation.setName(releaseSimpleName);

                // Wrap the MethodInvocation in an ExpressionStatement
                ExpressionStatement releaseExpressionStatement = targetAST.newExpressionStatement(releaseMethodInvocation);

                // If the scope is the method, then add it to the end of the method
                Block acquireBlock = ASTUtilities.getBlockFromContent(dwSmell.getAcquireBlock().toString(), methodDeclaration);
                if (acquireBlock == null) {
                    return null;
                }
                List<Statement> statementList = (List<Statement>) acquireBlock.statements();
                statementList.add(releaseExpressionStatement);

                ArrayList<String> proposedCodeToHighlightList = new ArrayList<>();
                proposedCodeToHighlightList.add(releaseExpressionStatement.toString());

                DWProposal proposalMethodBean = new DWProposal();
                proposalMethodBean.setMethodSmell(dwSmell);
                proposalMethodBean.setProposedMethodDeclaration(methodDeclaration);
                proposalMethodBean.setProposedCodeToHighlightList(proposedCodeToHighlightList);
                return proposalMethodBean;
            }
        }
        return null;
    }
}
