package adoctor.application.proposal.proposers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.ClassBean;
import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.ERBSmell;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.List;

public class ERBProposer extends ClassSmellProposer {
    private static final String ONCREATE_TYPE = "void";
    private static final String ONRESUME_NAME = "onResume";

    private static MethodDeclaration getOnResume(ClassBean classBean) {
        MethodDeclaration[] methods = classBean.getTypeDeclaration().getMethods();
        for (MethodDeclaration methodDecl : methods) {
            if (methodDecl.getName().toString().equals(ONRESUME_NAME)) {
                Type returnType = methodDecl.getReturnType2();
                if (returnType != null && returnType.toString().equals(ONCREATE_TYPE)) {
                    if (Modifier.isPublic(methodDecl.getModifiers()) || Modifier.isProtected(methodDecl.getModifiers())) {
                        List parameters = methodDecl.parameters();
                        if (parameters != null && parameters.size() == 0) {
                            return methodDecl;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ASTRewrite computeProposal(ClassSmell classSmell) {
        if (classSmell == null) {
            return null;
        }
        if (!(classSmell instanceof ERBSmell)) {
            return null;
        }
        ERBSmell erbSmell = (ERBSmell) classSmell;
        MethodDeclaration onCreateDecl = erbSmell.getOnCreate();
        AST targetAST = onCreateDecl.getAST();
        ASTRewrite astRewrite = ASTRewrite.create(targetAST);

        // Creates the new request statement
        Statement requestStatement = erbSmell.getRequestStatement();
        Statement newRequestStatement = (Statement) ASTNode.copySubtree(targetAST, requestStatement);

        // Creation of the new onResume
        MethodDeclaration onResumeDecl = getOnResume(erbSmell.getClassBean());
        if (onResumeDecl == null) {
            MethodDeclaration newOnResumeDecl = targetAST.newMethodDeclaration();
            newOnResumeDecl.setName(targetAST.newSimpleName(ONRESUME_NAME));
            newOnResumeDecl.modifiers().add(targetAST.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
            newOnResumeDecl.setBody(targetAST.newBlock());
            newOnResumeDecl.getBody().statements().add(newRequestStatement);
            ListRewrite listRewrite = astRewrite.getListRewrite(classSmell.getClassBean().getTypeDeclaration(), TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
            listRewrite.insertAfter(newOnResumeDecl, onCreateDecl, null);
        } else {
            ListRewrite listRewrite = astRewrite.getListRewrite(onResumeDecl.getBody(), Block.STATEMENTS_PROPERTY);
            listRewrite.insertLast(newRequestStatement, null);
        }

        // Remove the release at from the onCreate block
        Block requestBlock = ASTUtilities.getParentBlock(requestStatement);
        ListRewrite listRewrite = astRewrite.getListRewrite(requestBlock, Block.STATEMENTS_PROPERTY);
        listRewrite.remove(requestStatement, null);
        return astRewrite;
    }
}
