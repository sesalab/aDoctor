package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;

public class MethodDeclarationVisitor extends ASTVisitor {

    private ArrayList<MethodDeclaration> methodDeclarations;

    public MethodDeclarationVisitor(ArrayList<MethodDeclaration> methodDeclarations) {
        this.methodDeclarations = methodDeclarations;
    }

    @Override
    public boolean visit(MethodDeclaration methodDeclaration) {
        methodDeclarations.add(methodDeclaration);
        return true;
    }
}
