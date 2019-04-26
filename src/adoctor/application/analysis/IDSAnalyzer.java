package adoctor.application.analysis;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.Method;
import adoctor.application.bean.smell.IDSSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

@SuppressWarnings("unchecked")
public class IDSAnalyzer extends MethodSmellAnalyzer {
    private static final String HASHMAP = "HashMap";
    private static final String INTEGER = "Integer";

    @Override
    public MethodSmell analyzeMethod(Method method) {
        if (method == null) {
            return null;
        }
        MethodDeclaration methodDecl = method.getMethodDecl();
        if (methodDecl == null) {
            return null;
        }

        // Local variables
        List<VariableDeclarationStatement> variableDeclarationStatements = ASTUtilities.getVariableDeclarationStatements(methodDecl);
        for (VariableDeclarationStatement varDecl : variableDeclarationStatements) {
            if (isHashMapIntegerObject(varDecl.getType())) {
                IDSSmell smell = new IDSSmell();
                smell.setMethod(method);
                smell.setSmellyVarDecl(varDecl);
                return smell;
            }
        }

        // TODO E' bene che le var di istanza siano gestite in analisi a lv di classe
        //  Fare questo per ciascun metodo è computazionalmente inutile
        /*
        List<FieldDeclaration> fieldDeclarations = ASTUtilities.getFieldDeclarations((CompilationUnit) methodDeclaration.getRoot());
        for (FieldDeclaration fieldDecl : fieldDeclarations) {
            System.out.println(fieldDecl);
            if (isHashMapIntegerObject(fieldDecl.getType())) {
                System.out.println("Ho smell di istanza");

            }
        }
        */
        return null;

    }

    private boolean isHashMapIntegerObject(Type typeNode) {
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
