package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

import java.util.ArrayList;

public class SuperFieldAccessVisitor extends ASTVisitor {

    private ArrayList<SuperFieldAccess> superFieldAccesses;

    public SuperFieldAccessVisitor(ArrayList<SuperFieldAccess> superFieldAccesses) {
        this.superFieldAccesses = superFieldAccesses;
    }

    @Override
    public boolean visit(SuperFieldAccess superFieldAccess) {
        superFieldAccesses.add(superFieldAccess);
        return true;
    }
}
