package adoctor.application.proposal;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.Method;
import adoctor.application.bean.proposal.DWProposal;
import adoctor.application.bean.smell.DWSmell;
import adoctor.application.bean.smell.MethodSmell;
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
        Method method = dwSmell.getMethod();
        if (method == null) {
            return null;
        }
        File sourceFile = method.getSourceFile();
        if (sourceFile == null) {
            return null;
        }
        CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(sourceFile);
        if (compilationUnit == null) {
            return null;
        }
        MethodDeclaration methodDeclaration = ASTUtilities.getMethodDeclarationFromContent(compilationUnit, method.getLegacyMethodBean().getTextContent());
        if (methodDeclaration == null) {
            return null;
        }

        // Important part starts here
        Statement acquireStatement = dwSmell.getAcquireStatement();
        if (acquireStatement == null) {
            return null;
        }
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
        Block acquireBlock = ASTUtilities.getBlockFromContent(methodDeclaration, dwSmell.getAcquireBlock().toString());
        if (acquireBlock == null) {
            return null;
        }
        List<Statement> statementList = (List<Statement>) acquireBlock.statements();
        statementList.add(releaseExpressionStatement);

        ArrayList<String> actualHighlights = new ArrayList<>();
        actualHighlights.add(acquireStatement.toString());
        ArrayList<String> proposedHighlights = new ArrayList<>();
        proposedHighlights.add(releaseExpressionStatement.toString());

        DWProposal proposal = new DWProposal();
        proposal.setMethodSmell(dwSmell);
        proposal.setProposedMethodDeclaration(methodDeclaration);
        proposal.setProposedCode(methodDeclaration.toString());
        proposal.setActualHighlights(actualHighlights);
        proposal.setProposedHighlights(proposedHighlights);
        return proposal;
    }
}
