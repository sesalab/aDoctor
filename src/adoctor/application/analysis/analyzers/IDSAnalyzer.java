package adoctor.application.analysis.analyzers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.ClassBean;
import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.IDSSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class IDSAnalyzer extends ClassSmellAnalyzer {
    private static final String HASHMAP = "HashMap";
    private static final String INTEGER = "Integer";

    // TODO Low There should be the instance variable check. How to manage it?
    @Override
    public ClassSmell analyze(ClassBean classBean) {
        if (classBean == null) {
            return null;
        }
        MethodDeclaration[] methods = classBean.getTypeDeclaration().getMethods();
        for (MethodDeclaration methodDecl : methods) {
            // Local variables
            List<VariableDeclarationStatement> variableDeclarationStatements = ASTUtilities.getVariableDeclarationStatements(methodDecl);
            if (variableDeclarationStatements == null) {
                return null;
            }
            for (VariableDeclarationStatement varDecl : variableDeclarationStatements) {
                if (hasHashMapIntegerObjectType(varDecl.getType())) {
                    IDSSmell smell = new IDSSmell();
                    smell.setClassBean(classBean);
                    smell.setSmellyVarDecl(varDecl);
                    return smell;
                }
            }

            /*
            List<FieldDeclaration> fieldDeclarations = ASTUtilities.getFieldDeclarations((CompilationUnit) methodDeclaration.getRoot());
            for (FieldDeclaration fieldDecl : fieldDeclarations) {
                System.out.println(fieldDecl);
                if (hasHashMapIntegerObjectType(fieldDecl.getType())) {
                    System.out.println("Ho smell di istanza");

                }
            }
            */
        }
        return null;
    }

    private static boolean hasHashMapIntegerObjectType(Type typeNode) {
        if (!typeNode.isParameterizedType()) {
            return false;
        }
        ParameterizedType parameterizedType = (ParameterizedType) typeNode;
        if (!parameterizedType.getType().isSimpleType()) {
            return false;
        }
        SimpleType type = (SimpleType) parameterizedType.getType();
        if (!type.getName().toString().equals(HASHMAP)) {
            return false;
        }
        List<Type> typeParameters = parameterizedType.typeArguments();
        if (!typeParameters.get(0).isSimpleType()) {
            return false;
        }
        SimpleType FirstParType = (SimpleType) typeParameters.get(0);
        return FirstParType.getName().toString().equals(INTEGER) && typeParameters.get(1).isSimpleType();
    }
}
