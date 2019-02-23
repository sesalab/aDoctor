package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import java.util.ArrayList;

public class ImportDeclarationVisitor extends ASTVisitor {

    private ArrayList<ImportDeclaration> importDeclarations;

    public ImportDeclarationVisitor(ArrayList<ImportDeclaration> importDeclarations) {
        this.importDeclarations = importDeclarations;
    }

    @Override
    public boolean visit(ImportDeclaration importDeclaration) {
        importDeclarations.add(importDeclaration);
        return true;
    }
}