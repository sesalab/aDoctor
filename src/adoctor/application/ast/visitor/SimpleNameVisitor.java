package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.ArrayList;

public class SimpleNameVisitor extends ASTVisitor {

    private ArrayList<SimpleName> simpleNames;

    public SimpleNameVisitor(ArrayList<SimpleName> simpleNames) {
        this.simpleNames = simpleNames;
    }

    @Override
    public boolean visit(SimpleName simpleName) {
        simpleNames.add(simpleName);
        return true;
    }
}
