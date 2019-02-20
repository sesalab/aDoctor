package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;

public class MethodInvocationVisitor extends ASTVisitor {

    private ArrayList<MethodInvocation> methodInvocations;

    public MethodInvocationVisitor(ArrayList<MethodInvocation> methodInvocations) {
        this.methodInvocations = methodInvocations;
    }

    @Override
    public boolean visit(MethodInvocation methodInvocation) {
        methodInvocations.add(methodInvocation);
        return true;
    }
}
