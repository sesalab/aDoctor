package adoctor.application.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;

import java.util.ArrayList;

public class FieldDeclarationVisitor extends ASTVisitor {

    private ArrayList<FieldDeclaration> fieldDeclarationList;

    FieldDeclarationVisitor(ArrayList<FieldDeclaration> fieldDeclarationList) {
        this.fieldDeclarationList = fieldDeclarationList;
    }

    @Override
    public boolean visit(FieldDeclaration fieldDeclarationNode) {
        fieldDeclarationList.add(fieldDeclarationNode);
        return true;
    }
}
