package adoctor.application.proposal;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.smell.DWSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;

@SuppressWarnings("unchecked")
public class DWProposer extends MethodSmellProposer {
    private static final String RELEASE = "release";

    //TODO Modificare Proposer, rendendolo più intelligente
    //TODO Review: it deletes comments maybe because it replaces the whole method instead of adding a new statement at the end of the same
    @Override
    public ASTRewrite computeProposal(MethodSmell methodSmell) {
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
        releaseMethodInvocation.setName(targetAST.newSimpleName(RELEASE));
        ExpressionStatement releaseStat = targetAST.newExpressionStatement(releaseMethodInvocation);

        // Add the new statement at the end of the block but in the new MethodDeclaration
        MethodDeclaration newMethodDecl = (MethodDeclaration) ASTNode.copySubtree(targetAST, smellyMethodDecl);
        //TODO Volendo acquireBlock è ottenibile facendo due volte getParent() del newMethodDecl. Riusare il trucco di scalare i parent
        Block acquireBlock = ASTUtilities.getBlockFromContent(newMethodDecl, dwSmell.getAcquireBlock().toString());
        if (acquireBlock == null) {
            return null;
        }
        List<Statement> statements = (List<Statement>) acquireBlock.statements();
        statements.add(releaseStat);

        // Accumulate the replacements
        ASTRewrite astRewrite = ASTRewrite.create(targetAST);
        astRewrite.replace(smellyMethodDecl, newMethodDecl, null);
        return astRewrite;
    }
}
