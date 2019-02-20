package adoctor.application.analysis;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.smell.IDSSmell;
import adoctor.application.bean.smell.MethodSmell;
import beans.MethodBean;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class IDSAnalyzer extends MethodSmellAnalyzer {

    @Override
    public MethodSmell analyzeMethod(MethodBean methodBean, MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, File sourceFile) {
        if (methodBean == null || methodDeclaration == null || compilationUnit == null || sourceFile == null) {
            return null;
        }
        List<SimpleName> names = new ArrayList<>();
        // Local variables
        List<VariableDeclarationStatement> variableDeclarationStatements = ASTUtilities.getVariableDeclarationStatements(methodDeclaration);
        for (VariableDeclarationStatement varDecl : variableDeclarationStatements) {
            if (isHashMapIntegerObject(varDecl.getType())) {
                List<VariableDeclarationFragment> varFragments = varDecl.fragments();
                for (VariableDeclarationFragment f : varFragments) {
                    names.add(f.getName());
                }
            }
        }

        //TODO E' bene che le var di istanza siano gestite diversamente. Fare questo per ciascun metodo è computazionalmente inutile
        /*
        List<FieldDeclaration> fieldDeclarations = ASTUtilities.getFieldDeclarations((CompilationUnit) methodDeclaration.getRoot());
        for (FieldDeclaration fieldDecl : fieldDeclarations) {
            System.out.println(fieldDecl);
            if (isHashMapIntegerObject(fieldDecl.getType())) {
                System.out.println("Ho smell di istanza");
                List<VariableDeclarationFragment> varFragments = fieldDecl.fragments();
                for (VariableDeclarationFragment f : varFragments) {
                    names.add(f.getName());
                }
            }
        }
        */

        if (names.isEmpty()) {
            return null;
        }
        System.out.println(names);
        IDSSmell smell = new IDSSmell();
        smell.setMethodBean(methodBean);
        smell.setSourceFile(sourceFile);
        smell.setNames(names);
        return smell;

        //TODO: Fare delle classi nuove MethodBean che contengono il MethodBean legacy con MethodDeclaration e File associati
        //TODO Rimuovere il parametro CompilationUnit perché ricavabile con getRoot di MethodDeclaration

        // TODO Data una di esse (es sempre la prima) si propone di cambiarle il tipo di dichiarazione in SparseArray<Object> (aggiungere import!)
        // TODO Si propone anche di cambiare automaticamente alcune invocazioni di metodi della stessa variabile, se presenti
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
        if (!type.getName().toString().equals("HashMap")) {
            return false;
        }
        List<Type> typeParameters = parameterizedType.typeArguments();
        if (!typeParameters.get(0).isSimpleType()) {
            return false;
        }
        SimpleType FirstParType = (SimpleType) typeParameters.get(0);
        return FirstParType.getName().toString().equals("Integer") && typeParameters.get(1).isSimpleType();
    }
}
