package adoctor.application.proposal.proposers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.LTSmell;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.List;

public class LTProposer extends ClassSmellProposer {
    private static final String STOP = "stop";
    private static final String ON_DESTROY = "onDestroy";

    @Override
    public ASTRewrite computeProposal(ClassSmell classSmell) {
        // TODO Low If the thread is declared as local variable, proposed to upgrade it as a instance variable
        if (classSmell == null) {
            return null;
        }
        if (!(classSmell instanceof LTSmell)) {
            return null;
        }
        LTSmell ltSmell = (LTSmell) classSmell;
        VariableDeclarationFragment smellyVar = ltSmell.getSmellyVariableDeclarationFragment();
        if (smellyVar == null) {
            return null;
        }
        AST targetAST = smellyVar.getAST();

        ASTRewrite astRewrite = ASTRewrite.create(targetAST);
        // Look for onDestroy method
        MethodDeclaration onDestroyDecl = getOnDestroy(smellyVar.getRoot());
        // If it does not exist, create it and add it into the class
        if (onDestroyDecl == null) {
            onDestroyDecl = createOnDestroyMethodDeclaration(astRewrite);
            ListRewrite classDeclsListRewrite = astRewrite.getListRewrite(ASTUtilities.getParentTypeDeclaration(smellyVar)
                    , TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
            classDeclsListRewrite.insertLast(onDestroyDecl, null);
        }
        // In any case, create and add the stop() call
        MethodInvocation stopCall = targetAST.newMethodInvocation();
        stopCall.setName(targetAST.newSimpleName(STOP));
        stopCall.setExpression(targetAST.newSimpleName(smellyVar.getName().getIdentifier()));
        ExpressionStatement stopStat = targetAST.newExpressionStatement(stopCall);
        ListRewrite onDestroyStatementsListRewrite = astRewrite.getListRewrite(onDestroyDecl.getBody(), Block.STATEMENTS_PROPERTY);
        onDestroyStatementsListRewrite.insertLast(stopStat, null);
        return astRewrite;
    }

    private static MethodDeclaration getOnDestroy(ASTNode node) {
        List<MethodDeclaration> methodDeclarations = ASTUtilities.getMethodDeclarations(node.getRoot());
        if (methodDeclarations == null) {
            return null;
        }
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            if (methodDeclaration.getName().getIdentifier().equals(ON_DESTROY)) {
                if (Modifier.isPublic(methodDeclaration.getModifiers()) || Modifier.isProtected(methodDeclaration.getModifiers())) {
                    if (methodDeclaration.parameters().size() == 0) {
                        return methodDeclaration;
                    }
                }
            }
        }
        return null;
    }

    private static MethodDeclaration createOnDestroyMethodDeclaration(ASTRewrite astRewrite) {
        AST ast = astRewrite.getAST();
        SuperMethodInvocation superOnDestroy = ast.newSuperMethodInvocation();
        superOnDestroy.setName(ast.newSimpleName(ON_DESTROY));
        ExpressionStatement superOnDestroyStat = ast.newExpressionStatement(superOnDestroy);

        MethodDeclaration onDestroyDecl = ast.newMethodDeclaration();
        onDestroyDecl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        onDestroyDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
        onDestroyDecl.setName(ast.newSimpleName(ON_DESTROY));
        onDestroyDecl.setBody(ast.newBlock());

        ListRewrite onDestroyStatsListRewrite = astRewrite.getListRewrite(onDestroyDecl.getBody(), Block.STATEMENTS_PROPERTY);
        onDestroyStatsListRewrite.insertLast(superOnDestroyStat, null);
        return onDestroyDecl;
    }
}
