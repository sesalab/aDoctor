package adoctor.application.proposal.proposers;

import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.MIMSmell;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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
        ASTRewrite astRewrite = ASTRewrite.create(targetAST);
        ListRewrite listRewrite = astRewrite.getListRewrite(smellyMethod, MethodDeclaration.MODIFIERS2_PROPERTY);
        listRewrite.insertLast(targetAST.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD), null);
        return astRewrite;
    }
}
