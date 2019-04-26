package adoctor.application.proposal;

import adoctor.application.bean.smell.MethodSmell;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public abstract class MethodSmellProposer {

    public abstract ASTRewrite computeProposal(MethodSmell methodSmell);
}
