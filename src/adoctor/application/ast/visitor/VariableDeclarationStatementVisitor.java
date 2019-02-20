package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.ArrayList;

public class VariableDeclarationStatementVisitor extends ASTVisitor {

    private ArrayList<VariableDeclarationStatement> variableDeclarationFragmentList;

    public VariableDeclarationStatementVisitor(ArrayList<VariableDeclarationStatement> variableDeclarationFragmentList) {
        this.variableDeclarationFragmentList = variableDeclarationFragmentList;
    }

    @Override
    public boolean visit(VariableDeclarationStatement variableDeclarationFragmentNode) {
        variableDeclarationFragmentList.add(variableDeclarationFragmentNode);
        return true;
    }
}
