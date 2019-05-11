package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import java.util.ArrayList;

public class SuperMethodInvocationVisitor extends ASTVisitor {

    private ArrayList<SuperMethodInvocation> superMethodInvocations;

    public SuperMethodInvocationVisitor(ArrayList<SuperMethodInvocation> superMethodInvocations) {
        this.superMethodInvocations = superMethodInvocations;
    }

    @Override
    public boolean visit(SuperMethodInvocation superMethodInvocation) {
        superMethodInvocations.add(superMethodInvocation);
        return true;
    }
}
