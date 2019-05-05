package adoctor.application.proposal.proposers;

import adoctor.application.bean.smell.ISSmell;
import adoctor.application.bean.smell.MethodSmell;
import javafx.util.Pair;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

@SuppressWarnings("Duplicates")
public class ISProposer extends MethodSmellProposer {
    @Override
    public ASTRewrite computeProposal(MethodSmell methodSmell) {
        if (methodSmell == null) {
            return null;
        }
        if (!(methodSmell instanceof ISSmell)) {
            return null;
        }
        ISSmell isSmell = (ISSmell) methodSmell;
        MethodDeclaration smellyMethodDecl = isSmell.getMethod().getMethodDecl();
        if (smellyMethodDecl == null) {
            return null;
        }
        MethodInvocation smellyCall = isSmell.getSmellyCall();
        Pair<MethodDeclaration, String> smellySetter = isSmell.getSmellySetter();
        if (smellyCall == null || smellySetter == null) {
            return null;
        }
        AST targetAST = smellyMethodDecl.getAST();

        ASTRewrite astRewrite = ASTRewrite.create(targetAST);
        Assignment newAssignment = targetAST.newAssignment();
        FieldAccess fieldAccess = targetAST.newFieldAccess();
        fieldAccess.setExpression(targetAST.newThisExpression());
        fieldAccess.setName(targetAST.newSimpleName(smellySetter.getValue()));
        newAssignment.setLeftHandSide(fieldAccess);
        newAssignment.setOperator(Assignment.Operator.ASSIGN);
        newAssignment.setRightHandSide((Expression) ASTNode.copySubtree(targetAST, (ASTNode) smellyCall.arguments().get(0)));
        astRewrite.replace(smellyCall, newAssignment, null);
        return astRewrite;
    }
}
