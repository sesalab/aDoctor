package adoctor.application.proposal;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.smell.IDSSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "Duplicates"})
public class IDSProposer extends MethodSmellProposer {
    private static final String HASHMAP = "HashMap";
    private static final String MAP = "Map";
    private static final String INTEGER = "Integer";
    private static final String SPARSE_ARRAY = "SparseArray";
    private static final String IMPORT_SPARSE_ARRAY = "android.util.SparseArray";
    private static final String REMOVE = "remove";
    private static final String CONTAINS_KEY = "containsKey";
    private static final String CONTAINS_VALUE = "containsValue";
    private static final String IS_EMPTY = "isEmpty";
    private static final String INDEX_OF_VALUE = "indexOfValue";
    private static final String INDEX_OF_KEY = "indexOfKey";
    private static final String SIZE = "size";
    private static final String ENTRY_SET = "entrySet";
    private static final String KEY_SET = "keySet";
    private static final String VALUES = "values";
    private static final String PUT_ALL = "putAll";

    @Override
    public ASTRewrite computeProposal(MethodSmell methodSmell) {
        if (methodSmell == null) {
            return null;
        }
        if (!(methodSmell instanceof IDSSmell)) {
            return null;
        }
        IDSSmell idsSmell = (IDSSmell) methodSmell;
        MethodDeclaration smellyMethodDecl = idsSmell.getMethod().getMethodDecl();
        if (smellyMethodDecl == null) {
            return null;
        }
        VariableDeclarationStatement smellyVarDecl = idsSmell.getSmellyVarDecl();
        if (smellyVarDecl == null) {
            return null;
        }
        AST targetAST = smellyMethodDecl.getAST();

        // Changes of Declaration of SparseArray<SecondType> to HashMap<Integer, SecondType>
        ParameterizedType newType = targetAST.newParameterizedType(targetAST.newSimpleType(targetAST.newSimpleName(
                SPARSE_ARRAY)));
        ParameterizedType parType = (ParameterizedType) smellyVarDecl.getType();
        SimpleType secondType = (SimpleType) parType.typeArguments().get(1);
        SimpleType newSimpleType = (SimpleType) ASTNode.copySubtree(targetAST, secondType);
        newType.typeArguments().add(newSimpleType);
        VariableDeclarationStatement newVarDecl = (VariableDeclarationStatement) ASTNode.copySubtree(targetAST
                , smellyVarDecl);
        newVarDecl.setType(newType);

        // Changes of HashMap<> constructor to SparseArray<>
        List<VariableDeclarationFragment> fragments = newVarDecl.fragments();
        for (VariableDeclarationFragment fragment : fragments) {
            List<ClassInstanceCreation> creations = ASTUtilities.getClassInstanceCreations(fragment);
            if (creations != null && !creations.isEmpty()) {
                ClassInstanceCreation creation = creations.get(0);
                ParameterizedType newConstructor = targetAST.newParameterizedType(targetAST.newSimpleType(targetAST
                        .newSimpleName(SPARSE_ARRAY)));
                creation.setType(newConstructor);
            }
        }
        // List of involved reference variables
        List<SimpleName> identifiers = ASTUtilities.getSimpleNames(newVarDecl);
        List<String> variables = new ArrayList<>();
        if (identifiers != null) {
            for (SimpleName identifier : identifiers) {
                if (identifier.isDeclaration()) {
                    variables.add(identifier.getIdentifier());
                }
            }
        }
        // List of involved method invocations
        List<MethodInvocation> allInvocations = ASTUtilities.getMethodInvocations(smellyVarDecl.getParent());
        List<MethodInvocation> involvedInvocations = new ArrayList<>();
        if (allInvocations != null) {
            for (MethodInvocation invocation : allInvocations) {
                if (invocation.getExpression() != null) {
                    if (variables.contains(invocation.getExpression().toString())) {
                        String methodName = invocation.getName().getIdentifier();
                        if (methodName.equals(REMOVE) && invocation.arguments().size() == 2
                                || methodName.equals(CONTAINS_KEY)
                                || methodName.equals(CONTAINS_VALUE)
                                || methodName.equals(IS_EMPTY)
                                || methodName.equals(ENTRY_SET)
                                /*
                        || methodName.equals(IDSSmell.KEY_SET)
                        || methodName.equals(IDSSmell.VALUES)
                        || methodName.equals(IDSSmell.PUT_ALL)*/) {
                            involvedInvocations.add(invocation);
                        }
                    }
                }
            }
        }
        // Creation of list of the new Expressions (mainly MethodInvocations, but not only)
        List<AbstractMap.SimpleEntry<MethodInvocation, Expression>> replacements = new ArrayList<>();
        List<MethodDeclaration> additions = new ArrayList<>();
        for (MethodInvocation invocation : involvedInvocations) {
            String methodName = invocation.getName().getIdentifier();
            Expression newExpr = null;
            switch (methodName) {
                // remove(int, obj) --> remove(indexOfValue(obj))
                case REMOVE: {
                    MethodInvocation newInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
                    MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
                    innerInvocation.setName(targetAST.newSimpleName(INDEX_OF_VALUE));
                    innerInvocation.arguments().remove(0);
                    newInvocation.arguments().clear();
                    newInvocation.arguments().add(innerInvocation);
                    newExpr = newInvocation;
                    break;
                }
                // containsKey(int) --> map.indexOfKey(int) >= 0
                case CONTAINS_KEY: {
                    MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
                    innerInvocation.setName(targetAST.newSimpleName(INDEX_OF_KEY));
                    InfixExpression relationalExpr = targetAST.newInfixExpression();
                    relationalExpr.setLeftOperand(innerInvocation);
                    relationalExpr.setOperator(InfixExpression.Operator.GREATER_EQUALS);
                    relationalExpr.setRightOperand(targetAST.newNumberLiteral("0"));
                    newExpr = relationalExpr;
                    break;
                }
                // containsKey(obj) --> indexOfValue(obj) >= 0
                case CONTAINS_VALUE: {
                    MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
                    innerInvocation.setName(targetAST.newSimpleName(INDEX_OF_VALUE));
                    InfixExpression relationalExpr = targetAST.newInfixExpression();
                    relationalExpr.setLeftOperand(innerInvocation);
                    relationalExpr.setOperator(InfixExpression.Operator.GREATER_EQUALS);
                    relationalExpr.setRightOperand(targetAST.newNumberLiteral("0"));
                    newExpr = relationalExpr;
                    break;
                }
                // isEmpty() --> size() == 0
                case IS_EMPTY: {
                    MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
                    innerInvocation.setName(targetAST.newSimpleName(SIZE));
                    InfixExpression relationalExpr = targetAST.newInfixExpression();
                    relationalExpr.setLeftOperand(innerInvocation);
                    relationalExpr.setOperator(InfixExpression.Operator.EQUALS);
                    relationalExpr.setRightOperand(targetAST.newNumberLiteral("0"));
                    newExpr = relationalExpr;
                    break;
                }
                // entrySet() --> Method call to a private method that does a for each on the array and builds a
                // Set of Entry<Integer,Object>
                case ENTRY_SET: {
                    // TODO Check se il metodo getEntrySet valido (possono essercene altri non validi) esiste già

                    // TODO entrySet() va sostituita con una call al metodo getEntrySet() passandogli l'array.

                    MethodDeclaration newMethod = createGetEntrySet(targetAST, newType);
                    System.out.println(newMethod);
                    additions.add(newMethod);
                    break;
                }
                /*
                // keySet() -->
                case IDSSmell.KEY_SET: {
                    break;
                }
                // values() -->
                case IDSSmell.VALUES: {
                    break;
                }
                case IDSSmell.PUT_ALL: {
                    break;
                }
                */
            }
            replacements.add(new AbstractMap.SimpleEntry<>(invocation, newExpr));
        }

        // Proposal of import android.util.SparseArray if not present
        // TODO Same for java.util.Set
        ImportDeclaration newImportDecl = targetAST.newImportDeclaration();
        newImportDecl.setName(targetAST.newName((IMPORT_SPARSE_ARRAY)));
        CompilationUnit compilationUnit = (CompilationUnit) smellyMethodDecl.getRoot();
        List<ImportDeclaration> imports = compilationUnit.imports();
        for (ImportDeclaration anImport : imports) {
            if (anImport.getName().toString().equals(IMPORT_SPARSE_ARRAY)) {
                newImportDecl = null;
            }
        }

        // TODO Aggiungere tutti i metodi in additions

        // Accumulate the replacements
        ASTRewrite astRewrite = ASTRewrite.create(targetAST);
        astRewrite.replace(smellyVarDecl, newVarDecl, null);
        for (AbstractMap.SimpleEntry<MethodInvocation, Expression> invocationReplacement : replacements) {
            astRewrite.replace(invocationReplacement.getKey(), invocationReplacement.getValue(), null);
        }
        if (newImportDecl != null) {
            ListRewrite listRewrite = astRewrite.getListRewrite(compilationUnit, CompilationUnit.IMPORTS_PROPERTY);
            listRewrite.insertLast(newImportDecl, null);
        }
        return astRewrite;
    }

    /*
        private Set<Map.Entry<Integer, Object>> getEntrySet(SparseArray<Object> array) {
            Set<Map.Entry<Integer, Object>> entrySet = new TreeSet<>();
            for (int i = 0; i < array.size(); i++) {
                int key = array.keyAt(i);
                Object obj = array.get(key);
                AbstractMap.SimpleEntry<Integer, Object> entry = new AbstractMap.SimpleEntry<>(key, obj);
                entrySet.add(entry);
            }
            return entrySet;
        }
    */
    private MethodDeclaration createGetEntrySet(AST targetAST, ParameterizedType sparseArrayType) {
        // Return type
        SimpleType mapType = targetAST.newSimpleType(targetAST.newSimpleName(MAP));
        QualifiedType mapEntryType = targetAST.newQualifiedType(mapType, targetAST
                .newSimpleName("Entry"));
        ParameterizedType mapEntryParType = targetAST.newParameterizedType(mapEntryType);
        mapEntryParType.typeArguments().add(targetAST.newSimpleType(targetAST.newSimpleName(INTEGER)));
        mapEntryParType.typeArguments().add(ASTNode.copySubtree(targetAST, (ASTNode) sparseArrayType.typeArguments().get(0)));
        ParameterizedType returnType = targetAST.newParameterizedType(targetAST.newSimpleType(targetAST
                .newSimpleName("Set")));
        returnType.typeArguments().add(mapEntryParType);

        // Formal parameter
        SingleVariableDeclaration formalPar = targetAST.newSingleVariableDeclaration();
        formalPar.setType((Type) ASTNode.copySubtree(targetAST, sparseArrayType));
        formalPar.setName(targetAST.newSimpleName("array"));

        // First statement of the method body
        Block methodBlock = targetAST.newBlock();
        VariableDeclarationFragment varDeclFrag = targetAST.newVariableDeclarationFragment();
        varDeclFrag.setName(targetAST.newSimpleName("entrySet"));
        ClassInstanceCreation constExpr = targetAST.newClassInstanceCreation();
        constExpr.setType(targetAST.newParameterizedType(targetAST.newSimpleType(targetAST
                .newSimpleName("TreeSet"))));
        varDeclFrag.setInitializer(constExpr);
        VariableDeclarationStatement constrStat = targetAST.newVariableDeclarationStatement(varDeclFrag);
        constrStat.setType((Type) ASTNode.copySubtree(targetAST, returnType));

        // For: Init
        VariableDeclarationFragment initFrag = targetAST.newVariableDeclarationFragment();
        initFrag.setName(targetAST.newSimpleName("i"));
        initFrag.setInitializer(targetAST.newNumberLiteral("0"));
        VariableDeclarationExpression initExpr = targetAST.newVariableDeclarationExpression(initFrag);
        initExpr.setType(targetAST.newPrimitiveType(PrimitiveType.INT));

        //For: Condition
        InfixExpression conditionExpr = targetAST.newInfixExpression();
        conditionExpr.setLeftOperand(targetAST.newSimpleName("i"));
        conditionExpr.setOperator(InfixExpression.Operator.LESS);
        MethodInvocation sizeCall = targetAST.newMethodInvocation();
        sizeCall.setExpression(targetAST.newSimpleName("array"));
        sizeCall.setName(targetAST.newSimpleName("size"));
        conditionExpr.setRightOperand(sizeCall);

        // For: Update
        PostfixExpression updateExpr = targetAST.newPostfixExpression();
        updateExpr.setOperand(targetAST.newSimpleName("i"));
        updateExpr.setOperator(PostfixExpression.Operator.INCREMENT);

        // First statement of the for body
        VariableDeclarationFragment keyAtFrag = targetAST.newVariableDeclarationFragment();
        keyAtFrag.setName(targetAST.newSimpleName("key"));
        MethodInvocation keyAtCall = targetAST.newMethodInvocation();
        keyAtCall.setExpression(targetAST.newSimpleName("array"));
        keyAtCall.setName(targetAST.newSimpleName("keyAt"));
        keyAtCall.arguments().add(targetAST.newSimpleName("i"));
        keyAtFrag.setInitializer(keyAtCall);
        VariableDeclarationStatement keyAtStat = targetAST.newVariableDeclarationStatement(keyAtFrag);
        keyAtStat.setType(targetAST.newPrimitiveType(PrimitiveType.INT));

        // TODO The other three statements

        // For: Body
        Block forBlock = targetAST.newBlock();
        forBlock.statements().add(keyAtStat);

        // Building the for
        ForStatement forStat = targetAST.newForStatement();
        forStat.initializers().add(initExpr);
        forStat.setExpression(conditionExpr);
        forStat.updaters().add(updateExpr);
        forStat.setBody(forBlock);

        // Return statement
        ReturnStatement returnStat = targetAST.newReturnStatement();
        returnStat.setExpression(targetAST.newSimpleName("entrySet"));

        // Building the method body
        methodBlock.statements().add(constrStat);
        methodBlock.statements().add(forStat);
        methodBlock.statements().add(returnStat);

        // Building the whole method
        MethodDeclaration newMethod = targetAST.newMethodDeclaration();
        newMethod.modifiers().add(targetAST.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        newMethod.setReturnType2(returnType);
        newMethod.setName(targetAST.newSimpleName("getEntrySet"));
        newMethod.parameters().add(formalPar);
        newMethod.setBody(methodBlock);

        return newMethod;
    }
}
