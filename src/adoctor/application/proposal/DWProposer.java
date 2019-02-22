package adoctor.application.proposal;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.proposal.DWProposal;
import adoctor.application.bean.smell.DWSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class DWProposer extends MethodSmellProposer {

    @Override
    public DWProposal computeProposal(MethodSmell methodSmell) {
        if (methodSmell == null) {
            return null;
        }
        if (!(methodSmell instanceof DWSmell)) {
            return null;
        }
        DWSmell dwSmell = (DWSmell) methodSmell;
        MethodDeclaration smellyMethodDecl = dwSmell.getMethod().getMethodDecl();
        if (smellyMethodDecl == null) {
            return null;
        }
        Statement acquireStatement = dwSmell.getAcquireStatement();
        if (acquireStatement == null) {
            return null;
        }
        AST targetAST = smellyMethodDecl.getAST();

        // Creates the release() statement
        MethodInvocation releaseMethodInvocation = targetAST.newMethodInvocation();
        ExpressionStatement acquireExpressionStatement = (ExpressionStatement) acquireStatement;
        Expression acquireExpression = acquireExpressionStatement.getExpression();
        MethodInvocation acquireMethodInvocation = (MethodInvocation) acquireExpression;
        releaseMethodInvocation.setExpression((Expression) ASTNode.copySubtree(targetAST, acquireMethodInvocation.getExpression()));
        releaseMethodInvocation.setName(targetAST.newSimpleName(DWSmell.RELEASE_NAME));
        ExpressionStatement releaseStat = targetAST.newExpressionStatement(releaseMethodInvocation);

        // Add the new statement at the end of the block but in the new MethodDeclaration
        MethodDeclaration newMethodDecl = (MethodDeclaration) ASTNode.copySubtree(targetAST, smellyMethodDecl);
        //TODO Volendo acquireBlock è ottenibile facendo due volte getParent() del newMethodDecl
        Block acquireBlock = ASTUtilities.getBlockFromContent(newMethodDecl, dwSmell.getAcquireBlock().toString());
        if (acquireBlock == null) {
            return null;
        }
        List<Statement> statements = (List<Statement>) acquireBlock.statements();
        statements.add(releaseStat);

        ArrayList<String> currentHighlights = new ArrayList<>();
        currentHighlights.add(acquireStatement.toString());
        ArrayList<String> proposedHighlights = new ArrayList<>();
        proposedHighlights.add(releaseStat.toString());

        DWProposal proposal = new DWProposal();
        proposal.setMethodSmell(dwSmell);
        proposal.setProposedMethodDecl(newMethodDecl);
        proposal.setProposedCode(newMethodDecl.toString());
        proposal.setCurrentHighlights(currentHighlights);
        proposal.setProposedHighlights(proposedHighlights);
        return proposal;
    }
}
