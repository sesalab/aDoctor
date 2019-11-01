package adoctor.application.proposal.proposers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.IDSSmell;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.List;

public class IDSProposer extends ClassSmellProposer {
    private static final String ABSTRACT_MAP = "AbstractMap";
    private static final String SPARSE_ARRAY = "SparseArray";
    private static final String ENTRY = "Entry";
    private static final String INTEGER = "Integer";
    private static final String MAP = "Map";
    private static final String MAP_ENTRY = "Map.Entry";
    private static final String SET = "Set";
    private static final String SIMPLE_ENTRY = "SimpleEntry";
    private static final String TREE_SET = "TreeSet";

    private static final String ANDROID_UTIL_SPARSE_ARRAY = "android.util.SparseArray";
    private static final String JAVA_UTIL = "java.util";

    private static final String ADD = "add";
    private static final String GET = "get";
    private static final String GET_ENTRY_SET = "getEntrySet";
    private static final String CONTAINS_KEY = "containsKey";
    private static final String CONTAINS_VALUE = "containsValue";
    private static final String ENTRY_SET = "entrySet";
    private static final String IS_EMPTY = "isEmpty";
    private static final String INDEX_OF_VALUE = "indexOfValue";
    private static final String INDEX_OF_KEY = "indexOfKey";
    private static final String KEY_SET = "keySet";
    private static final String KEY_AT = "keyAt";
    private static final String PUT_ALL = "putAll";
    private static final String REMOVE = "remove";
    private static final String SIZE = "size";
    private static final String VALUES = "values";
    private static final String ARRAY_VAR = "array";
    private static final String ENTRY_VAR = "entry";
    private static final String KEY_VAR = "key";
    private static final String OBJ_VAR = "obj";
    private static final String ZERO = "0";
    private static final String I = "i";

    //TODO Medium extract some more methods.
    @Override
    public ASTRewrite computeProposal(ClassSmell classSmell) {
        if (classSmell == null) {
            return null;
        }
        if (!(classSmell instanceof IDSSmell)) {
            return null;
        }
        IDSSmell idsSmell = (IDSSmell) classSmell;
        VariableDeclarationStatement smellyVarDecl = idsSmell.getSmellyVarDecl();
        if (smellyVarDecl == null) {
            return null;
        }

        // Build the new variable declaration statement
        VariableDeclarationStatement newVarDecl = buildNewVarDecl(smellyVarDecl);

        // Build import additions
        List<ImportDeclaration> importAdditions = buildImportAdditions(smellyVarDecl);

        // Fetch the list of involved method invocations
        List<MethodInvocation> involvedInvocations = getInvolvedInvocations(smellyVarDecl);
        // Creation of list of replacements and additions
        List<Pair<MethodInvocation, Expression>> replacements = new ArrayList<>();
        List<MethodDeclaration> methodAdditions = new ArrayList<>();
        for (MethodInvocation invocation : involvedInvocations) {
            Expression newExpr = null;
            switch (invocation.getName().getIdentifier()) {
                // remove(int, obj) --> remove(indexOfValue(obj))
                case REMOVE: {
                    newExpr = refactorRemove(invocation);
                    break;
                }
                // containsKey(int) --> map.indexOfKey(int) >= 0
                case CONTAINS_KEY: {
                    newExpr = refactorContainsKey(invocation);
                    break;
                }
                // containsValue(obj) --> indexOfValue(obj) >= 0
                case CONTAINS_VALUE: {
                    newExpr = refactorContainsValue(invocation);
                    break;
                }
                // isEmpty() --> size() == 0
                case IS_EMPTY: {
                    newExpr = refactorIsEmpty(invocation);
                    break;
                }
                // entrySet() --> Method call to a private method that does a for each on the array and builds a
                // Set of Entry<Integer,Object>
                case ENTRY_SET: {
                    // Check if new method is absent in additions and then in class
                    System.out.println("Trovata invocazione a entrySet()");
                    if (!existsGetEntrySet(methodAdditions)) {
                        System.out.println("Prima ricerca fallita");
                        List<MethodDeclaration> methodDeclarations = ASTUtilities.getMethodDeclarations(invocation.getRoot());
                        if (!existsGetEntrySet(methodDeclarations)) {
                            System.out.println("Seconda ricerca fallita: creo metodo");
                            MethodDeclaration newMethod = createGetEntrySetMethod((ParameterizedType) newVarDecl.getType());
                            methodAdditions.add(newMethod);
                        }
                    }
                    /*
                    boolean found = false;
                    for (int i = 0; i < methodAdditions.size() && !found; i++) {
                        MethodDeclaration methodAddition = methodAdditions.get(i);
                        if (methodAddition.getName().getIdentifier().equals(GET_ENTRY_SET)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        if (!existsGetEntrySet((CompilationUnit) invocation.getRoot())) {
                            MethodDeclaration newMethod = createGetEntrySetMethod((ParameterizedType) newVarDecl.getType());
                            methodAdditions.add(newMethod);
                        }
                    }
                     */

                    // entrySet() replaced by getEntrySet()
                    newExpr = refactorEntrySet(invocation);
                    break;
                }
                //TODO Low Should these be implemented? Is it necessary?
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
            replacements.add(new MutablePair<>(invocation, newExpr));
        }

        // Replacements and additions
        ASTRewrite astRewrite = ASTRewrite.create(smellyVarDecl.getAST());
        // Addition of all new imports
        for (ImportDeclaration importAddition : importAdditions) {
            ListRewrite listRewrite = astRewrite.getListRewrite(smellyVarDecl.getRoot(), CompilationUnit.IMPORTS_PROPERTY);
            listRewrite.insertLast(importAddition, null);
        }
        // Statement replacements
        astRewrite.replace(smellyVarDecl, newVarDecl, null);
        for (Pair<MethodInvocation, Expression> invocationReplacement : replacements) {
            astRewrite.replace(invocationReplacement.getKey(), invocationReplacement.getValue(), null);
        }
        // Addition of all new methods
        for (MethodDeclaration methodAddition : methodAdditions) {
            TypeDeclaration typeDecl = (TypeDeclaration) ((CompilationUnit) smellyVarDecl.getRoot()).types().get(0);
            ListRewrite listRewrite = astRewrite.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
            listRewrite.insertLast(methodAddition, null);
        }
        return astRewrite;
    }

    private static VariableDeclarationStatement buildNewVarDecl(VariableDeclarationStatement smellyVarDecl) {
        AST ast = smellyVarDecl.getAST();
        // Changes of Declaration of SparseArray<SecondType> to HashMap<Integer, SecondType>
        ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName(
                SPARSE_ARRAY)));
        ParameterizedType parType = (ParameterizedType) smellyVarDecl.getType();
        SimpleType secondType = (SimpleType) parType.typeArguments().get(1);
        SimpleType newSimpleType = (SimpleType) ASTNode.copySubtree(ast, secondType);
        newType.typeArguments().add(newSimpleType);
        VariableDeclarationStatement newVarDecl = (VariableDeclarationStatement) ASTNode.copySubtree(ast
                , smellyVarDecl);
        newVarDecl.setType(newType);

        // Changes of HashMap<> constructor to SparseArray<>
        List<VariableDeclarationFragment> fragments = newVarDecl.fragments();
        for (VariableDeclarationFragment fragment : fragments) {
            List<ClassInstanceCreation> creations = ASTUtilities.getClassInstanceCreations(fragment);
            if (creations != null && !creations.isEmpty()) {
                ClassInstanceCreation creation = creations.get(0);
                ParameterizedType newConstructor = ast.newParameterizedType(ast.newSimpleType(ast
                        .newSimpleName(SPARSE_ARRAY)));
                creation.setType(newConstructor);
            }
        }

        return newVarDecl;
    }

    private static List<MethodInvocation> getInvolvedInvocations(VariableDeclarationStatement smellyVarDecl) {
        // List of involved reference variables
        List<SimpleName> identifiers = ASTUtilities.getSimpleNames(smellyVarDecl);
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
                                || methodName.equals(IDSSmell.PUT_ALL)
                                */) {
                            involvedInvocations.add(invocation);
                        }
                    }
                }
            }
        }
        return involvedInvocations;
    }

    private static MethodInvocation refactorRemove(MethodInvocation invocation) {
        AST ast = invocation.getAST();
        MethodInvocation newInvocation = (MethodInvocation) ASTNode.copySubtree(ast, invocation);
        MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(ast, invocation);
        innerInvocation.setName(ast.newSimpleName(INDEX_OF_VALUE));
        innerInvocation.arguments().remove(0);
        newInvocation.arguments().clear();
        newInvocation.arguments().add(innerInvocation);
        return newInvocation;
    }

    private static InfixExpression refactorContainsKey(MethodInvocation invocation) {
        AST ast = invocation.getAST();
        MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(ast, invocation);
        innerInvocation.setName(ast.newSimpleName(INDEX_OF_KEY));
        InfixExpression relationalExpr = ast.newInfixExpression();
        relationalExpr.setLeftOperand(innerInvocation);
        relationalExpr.setOperator(InfixExpression.Operator.GREATER_EQUALS);
        relationalExpr.setRightOperand(ast.newNumberLiteral(ZERO));
        return relationalExpr;
    }

    private static Expression refactorContainsValue(MethodInvocation invocation) {
        AST ast = invocation.getAST();
        MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(ast, invocation);
        innerInvocation.setName(ast.newSimpleName(INDEX_OF_VALUE));
        InfixExpression relationalExpr = ast.newInfixExpression();
        relationalExpr.setLeftOperand(innerInvocation);
        relationalExpr.setOperator(InfixExpression.Operator.GREATER_EQUALS);
        relationalExpr.setRightOperand(ast.newNumberLiteral(ZERO));
        return relationalExpr;
    }

    private static InfixExpression refactorIsEmpty(MethodInvocation invocation) {
        AST ast = invocation.getAST();
        MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(ast, invocation);
        innerInvocation.setName(ast.newSimpleName(SIZE));
        InfixExpression relationalExpr = ast.newInfixExpression();
        relationalExpr.setLeftOperand(innerInvocation);
        relationalExpr.setOperator(InfixExpression.Operator.EQUALS);
        relationalExpr.setRightOperand(ast.newNumberLiteral(ZERO));
        return relationalExpr;
    }

    private static MethodInvocation refactorEntrySet(MethodInvocation invocation) {
        AST ast = invocation.getAST();
        MethodInvocation getEntrySetCall = ast.newMethodInvocation();
        getEntrySetCall.setName(ast.newSimpleName(GET_ENTRY_SET));
        getEntrySetCall.arguments().add(ASTNode.copySubtree(ast, invocation.getExpression()));
        return getEntrySetCall;
    }

    // Set<Map.Entry<Integer, Object>> getEntrySet(SparseArray<Object> array)
    private static boolean existsGetEntrySet(List<MethodDeclaration> methodDeclarations) {
        if (methodDeclarations != null) {
            for (MethodDeclaration methodDeclaration : methodDeclarations) {
                // First look for the name: getEntrySet
                if (methodDeclaration.getName().getIdentifier().equals(GET_ENTRY_SET)) {
                    // Then check the return type: Set<Map.Entry<Integer, Object>>
                    try {
                        ParameterizedType retType = (ParameterizedType) methodDeclaration.getReturnType2();
                        SingleVariableDeclaration methodArg = (SingleVariableDeclaration) methodDeclaration.parameters().get(0);
                        // Expected Set
                        Type retType2 = retType.getType();
                        System.out.println("retType2 " + retType2);
                        // Expected Map.Entry
                        Type retType3 = ((ParameterizedType) retType.typeArguments().get(0)).getType();
                        System.out.println("retType3 " + retType3);
                        // Expected Integer
                        Type retType3Arg1 = (Type) ((ParameterizedType) retType.typeArguments().get(0)).typeArguments().get(0);
                        System.out.println("retType3Arg1 " + retType3Arg1);
                        // Expected SparseArray
                        Type methodArgType = ((ParameterizedType) methodArg.getType()).getType();
                        System.out.println("methodArgType " + methodArgType);
                        // Expected Anything, it only matters their existence and their match
                        Type retType3Arg2 = (Type) ((ParameterizedType) retType.typeArguments().get(0)).typeArguments().get(1);
                        Type methodArgTypeArg = (Type) ((ParameterizedType) methodArg.getType()).typeArguments().get(0);
                        System.out.println("retType3Arg2 " + retType3Arg2);
                        System.out.println("methodArgTypeArg " + methodArgTypeArg);

                        return retType2.toString().equals(SET)
                                && retType3.toString().equals(MAP_ENTRY)
                                && retType3Arg1.toString().equals(INTEGER)
                                && methodArgType.toString().equals(SPARSE_ARRAY)
                                && retType3Arg2.toString().equals(methodArgTypeArg.toString());
                    } catch (ClassCastException | NullPointerException e) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /*
    This is the structure of the method that this method adds
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
    //TODO Low Extract some methods
    private static MethodDeclaration createGetEntrySetMethod(ParameterizedType sparseArrayType) {
        // Return type: Set<Map.Entry<Integer, Object>>
        AST ast = sparseArrayType.getAST();
        SimpleType mapType = ast.newSimpleType(ast.newSimpleName(MAP));
        QualifiedType mapEntryType = ast.newQualifiedType(mapType, ast
                .newSimpleName(ENTRY));
        ParameterizedType mapEntryParType = ast.newParameterizedType(mapEntryType);
        mapEntryParType.typeArguments().add(ast.newSimpleType(ast.newSimpleName(INTEGER)));
        mapEntryParType.typeArguments().add(ASTNode.copySubtree(ast, (ASTNode) sparseArrayType.typeArguments().get(0)));
        ParameterizedType returnType = ast.newParameterizedType(ast.newSimpleType(ast
                .newSimpleName(SET)));
        returnType.typeArguments().add(mapEntryParType);

        // Formal parameter: SparseArray<Object> array
        SingleVariableDeclaration formalPar = ast.newSingleVariableDeclaration();
        formalPar.setType((Type) ASTNode.copySubtree(ast, sparseArrayType));
        formalPar.setName(ast.newSimpleName(ARRAY_VAR));

        // First statement of the method body: Set<Map.Entry<Integer, Object>> entrySet = new TreeSet<>();
        Block methodBlock = ast.newBlock();
        VariableDeclarationFragment varDeclFrag = ast.newVariableDeclarationFragment();
        varDeclFrag.setName(ast.newSimpleName(ENTRY_SET));
        ClassInstanceCreation constExpr = ast.newClassInstanceCreation();
        constExpr.setType(ast.newParameterizedType(ast.newSimpleType(ast
                .newSimpleName(TREE_SET))));
        varDeclFrag.setInitializer(constExpr);
        VariableDeclarationStatement constrStat = ast.newVariableDeclarationStatement(varDeclFrag);
        constrStat.setType((Type) ASTNode.copySubtree(ast, returnType));

        // For Init: int i = 0
        VariableDeclarationFragment initFrag = ast.newVariableDeclarationFragment();
        initFrag.setName(ast.newSimpleName(I));
        initFrag.setInitializer(ast.newNumberLiteral(ZERO));
        VariableDeclarationExpression initExpr = ast.newVariableDeclarationExpression(initFrag);
        initExpr.setType(ast.newPrimitiveType(PrimitiveType.INT));

        //For Condition: i < array.size()
        InfixExpression conditionExpr = ast.newInfixExpression();
        conditionExpr.setLeftOperand(ast.newSimpleName(I));
        conditionExpr.setOperator(InfixExpression.Operator.LESS);
        MethodInvocation sizeCall = ast.newMethodInvocation();
        sizeCall.setExpression(ast.newSimpleName(ARRAY_VAR));
        sizeCall.setName(ast.newSimpleName(SIZE));
        conditionExpr.setRightOperand(sizeCall);

        // For Update: i++
        PostfixExpression updateExpr = ast.newPostfixExpression();
        updateExpr.setOperand(ast.newSimpleName(I));
        updateExpr.setOperator(PostfixExpression.Operator.INCREMENT);

        // First statement of the for body: int key = array.keyAt(i);
        VariableDeclarationFragment keyAtFrag = ast.newVariableDeclarationFragment();
        keyAtFrag.setName(ast.newSimpleName(KEY_VAR));
        MethodInvocation keyAtCall = ast.newMethodInvocation();
        keyAtCall.setExpression(ast.newSimpleName(ARRAY_VAR));
        keyAtCall.setName(ast.newSimpleName(KEY_AT));
        keyAtCall.arguments().add(ast.newSimpleName(I));
        keyAtFrag.setInitializer(keyAtCall);
        VariableDeclarationStatement keyAtStat = ast.newVariableDeclarationStatement(keyAtFrag);
        keyAtStat.setType(ast.newPrimitiveType(PrimitiveType.INT));

        // Second statement of the for body: Object obj = array.get(key);
        VariableDeclarationFragment getFrag = ast.newVariableDeclarationFragment();
        getFrag.setName(ast.newSimpleName(OBJ_VAR));
        MethodInvocation getCall = ast.newMethodInvocation();
        getCall.setExpression(ast.newSimpleName(ARRAY_VAR));
        getCall.setName(ast.newSimpleName(GET));
        getCall.arguments().add(ast.newSimpleName(KEY_VAR));
        getFrag.setInitializer(getCall);
        VariableDeclarationStatement getStat = ast.newVariableDeclarationStatement(getFrag);
        getStat.setType((Type) ASTNode.copySubtree(ast, (ASTNode) sparseArrayType.typeArguments().get(0)));

        // Third statement of the for body: AbstractMap.SimpleEntry<Integer, Object> entry = new AbstractMap.SimpleEntry<>(key, obj);
        VariableDeclarationFragment newFrag = ast.newVariableDeclarationFragment();
        newFrag.setName(ast.newSimpleName(ENTRY_VAR));
        ClassInstanceCreation newExpr = ast.newClassInstanceCreation();
        ParameterizedType abstractMapSimpleEntryDiamond = ast.newParameterizedType(ast.newQualifiedType(
                ast.newSimpleType(ast.newSimpleName(ABSTRACT_MAP))
                , ast.newSimpleName(SIMPLE_ENTRY)));
        newExpr.setType(abstractMapSimpleEntryDiamond);
        newExpr.arguments().add(ast.newSimpleName(KEY_VAR));
        newExpr.arguments().add(ast.newSimpleName(OBJ_VAR));
        newFrag.setInitializer(newExpr);
        VariableDeclarationStatement newStat = ast.newVariableDeclarationStatement(newFrag);
        ParameterizedType abstractMapSimpleEntry = (ParameterizedType) ASTNode.copySubtree(ast, abstractMapSimpleEntryDiamond);
        abstractMapSimpleEntry.typeArguments().add(ast.newSimpleType(ast.newSimpleName(INTEGER)));
        abstractMapSimpleEntry.typeArguments().add(ASTNode.copySubtree(ast, (ASTNode) sparseArrayType.typeArguments().get(0)));
        newStat.setType(abstractMapSimpleEntry);

        // Fourth statement of the for body: entrySet.add(entry)
        MethodInvocation addCall = ast.newMethodInvocation();
        addCall.setExpression(ast.newSimpleName(ENTRY_SET));
        addCall.setName(ast.newSimpleName(ADD));
        addCall.arguments().add(ast.newSimpleName(ENTRY_VAR));
        ExpressionStatement addStat = ast.newExpressionStatement(addCall);

        // Building the for body 
        Block forBlock = ast.newBlock();
        forBlock.statements().add(keyAtStat);
        forBlock.statements().add(getStat);
        forBlock.statements().add(newStat);
        forBlock.statements().add(addStat);

        // Building the whole for
        ForStatement forStat = ast.newForStatement();
        forStat.initializers().add(initExpr);
        forStat.setExpression(conditionExpr);
        forStat.updaters().add(updateExpr);
        forStat.setBody(forBlock);

        // Return statement: return entrySet
        ReturnStatement returnStat = ast.newReturnStatement();
        returnStat.setExpression(ast.newSimpleName(ENTRY_SET));

        // Building the method body
        methodBlock.statements().add(constrStat);
        methodBlock.statements().add(forStat);
        methodBlock.statements().add(returnStat);

        // Building the whole method
        MethodDeclaration newMethod = ast.newMethodDeclaration();
        newMethod.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        newMethod.setReturnType2(returnType);
        newMethod.setName(ast.newSimpleName(GET_ENTRY_SET));
        newMethod.parameters().add(formalPar);
        newMethod.setBody(methodBlock);

        return newMethod;
    }

    private static List<ImportDeclaration> buildImportAdditions(VariableDeclarationStatement smellyVarDecl) {
        AST ast = smellyVarDecl.getAST();
        List<ImportDeclaration> importProposals = new ArrayList<>();
        // Proposal of import android.util.SparseArray
        ImportDeclaration sparseArrayImport = ast.newImportDeclaration();
        sparseArrayImport.setName(ast.newName(ANDROID_UTIL_SPARSE_ARRAY));
        importProposals.add(sparseArrayImport);
        // Proposal of import java.util
        ImportDeclaration javaUtilImport = ast.newImportDeclaration();
        javaUtilImport.setName(ast.newName(JAVA_UTIL));
        javaUtilImport.setOnDemand(true);
        importProposals.add(javaUtilImport);

        // Check if these proposals are valid
        List<ImportDeclaration> importAdditions = new ArrayList<>();
        List<ImportDeclaration> currentImports = ((CompilationUnit) smellyVarDecl.getRoot()).imports();
        for (ImportDeclaration importProposal : importProposals) {
            String importName = importProposal.getName().getFullyQualifiedName();
            boolean found = false;
            for (int i = 0; i < currentImports.size() && !found; i++) {
                ImportDeclaration currentImport = currentImports.get(i);
                if (currentImport.getName().getFullyQualifiedName().equals(importName)) {
                    found = true;
                }
            }
            if (!found) {
                importAdditions.add(importProposal);
            }
        }
        return importAdditions;
    }
}
