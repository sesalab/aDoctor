package adoctor.application.proposal.proposers;

import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.MIMSmell;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

@SuppressWarnings({"Duplicates", "unchecked"})
public class MIMProposer extends ClassSmellProposer {
    @Override
    public ASTRewrite computeProposal(ClassSmell classSmell) {
        if (classSmell == null) {
            return null;
        }
        if (!(classSmell instanceof MIMSmell)) {
            return null;
        }
        MIMSmell mimSmell = (MIMSmell) classSmell;
        MethodDeclaration smellyMethod = mimSmell.getSmellyMethod();
        AST targetAST = smellyMethod.getAST();

        MethodDeclaration staticMethod = (MethodDeclaration) ASTNode.copySubtree(targetAST, smellyMethod);
        staticMethod.modifiers().add(targetAST.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        ASTRewrite astRewrite = ASTRewrite.create(targetAST);
        astRewrite.replace(smellyMethod, staticMethod, null);
        return astRewrite;
    }
}
