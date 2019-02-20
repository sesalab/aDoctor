package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import java.util.ArrayList;

public class ClassInstanceCreationVisitor extends ASTVisitor {

    private ArrayList<ClassInstanceCreation> classInstanceCreations;

    public ClassInstanceCreationVisitor(ArrayList<ClassInstanceCreation> classInstanceCreations) {
        this.classInstanceCreations = classInstanceCreations;
    }

    @Override
    public boolean visit(ClassInstanceCreation classInstanceCreation) {
        classInstanceCreations.add(classInstanceCreation);
        return true;
    }
}
