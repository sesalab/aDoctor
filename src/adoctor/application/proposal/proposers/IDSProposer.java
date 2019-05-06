package adoctor.application.proposal.proposers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.smell.ClassSmell;
import adoctor.application.bean.smell.IDSSmell;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "Duplicates"})
public class IDSProposer extends ClassSmellProposer {
    private static final String SPARSE_ARRAY = "SparseArray";
    private static final String MAP = "Map";
    private static final String ABSTRACT_MAP = "AbstractMap";
    private static final String INTEGER = "Integer";
    private static final String SET = "Set";
    private static final String TREE_SET = "TreeSet";
    private static final String ENTRY = "Entry";
    private static final String SIMPLE_ENTRY = "SimpleEntry";
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

    //TODO Low Extract some more methods
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
        AST targetAST = smellyVarDecl.getAST();

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

        // Fetch the list of involved method invocations
        List<MethodInvocation> involvedInvocations = getInvolvedInvocations(smellyVarDecl);

        // Creation of list of statements replacements (mainly MethodInvocations, but not only)
        // and some method additions
        List<AbstractMap.SimpleEntry<MethodInvocation, Expression>> replacements = new ArrayList<>();
        List<MethodDeclaration> methodAdditions = new ArrayList<>();
        for (MethodInvocation invocation : involvedInvocations) {
            Expression newExpr = null;
            switch (invocation.getName().getIdentifier()) {
                // remove(int, obj) --> remove(indexOfValue(obj))
                case REMOVE: {
                    newExpr = refactorRemove(targetAST, invocation);
                    break;
                }
                // containsKey(int) --> map.indexOfKey(int) >= 0
                case CONTAINS_KEY: {
                    newExpr = refactorContainsKey(targetAST, invocation);
                    break;
                }
                // containsValue(obj) --> indexOfValue(obj) >= 0
                case CONTAINS_VALUE: {
                    newExpr = refactorContainsValue(targetAST, invocation);
                    break;
                }
                // isEmpty() --> size() == 0
                case IS_EMPTY: {
                    newExpr = refactorIsEmpty(targetAST, invocation);
                    break;
                }
                // entrySet() --> Method call to a private method that does a for each on the array and builds a
                // Set of Entry<Integer,Object>
                case ENTRY_SET: {
                    // Check if new method is absent in additions and then in class
                    boolean found = false;
                    for (int i = 0; i < methodAdditions.size() && !found; i++) {
                        MethodDeclaration methodAddition = methodAdditions.get(i);
                        if (methodAddition.getName().getIdentifier().equals(GET_ENTRY_SET)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        if (!existsGetEntrySet((CompilationUnit) invocation.getRoot())) {
                            MethodDeclaration newMethod = createGetEntrySetMethod(targetAST, newType);
                            methodAdditions.add(newMethod);
                        }
                    }

                    // entrySet() replaced by getEntrySet()
                    newExpr = refactorEntrySet(targetAST, invocation);
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
            replacements.add(new AbstractMap.SimpleEntry<>(invocation, newExpr));
        }

        // Import Proposals
        List<ImportDeclaration> importProposals = new ArrayList<>();
        // Proposal of import android.util.SparseArray
        ImportDeclaration sparseArrayImport = targetAST.newImportDeclaration();
        sparseArrayImport.setName(targetAST.newName(ANDROID_UTIL_SPARSE_ARRAY));
        importProposals.add(sparseArrayImport);
        // Proposal of import java.util
        ImportDeclaration javaUtilImport = targetAST.newImportDeclaration();
        javaUtilImport.setName(targetAST.newName(JAVA_UTIL));
        javaUtilImport.setOnDemand(true);
        importProposals.add(javaUtilImport);
        // Check if import proposal are valid
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

        // Replacements and additions
        // Statement replacements
        ASTRewrite astRewrite = ASTRewrite.create(targetAST);
        astRewrite.replace(smellyVarDecl, newVarDecl, null);
        for (AbstractMap.SimpleEntry<MethodInvocation, Expression> invocationReplacement : replacements) {
            astRewrite.replace(invocationReplacement.getKey(), invocationReplacement.getValue(), null);
        }
        // Addition of all new methods
        for (MethodDeclaration methodAddition : methodAdditions) {
            TypeDeclaration typeDecl = (TypeDeclaration) ((CompilationUnit) smellyVarDecl.getRoot()).types().get(0);
            ListRewrite listRewrite = astRewrite.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
            listRewrite.insertLast(methodAddition, null);
        }
        // Addition of all new imports
        for (ImportDeclaration importAddition : importAdditions) {
            ListRewrite listRewrite = astRewrite.getListRewrite(smellyVarDecl.getRoot(), CompilationUnit.IMPORTS_PROPERTY);
            listRewrite.insertLast(importAddition, null);
        }
        return astRewrite;
    }

    private List<MethodInvocation> getInvolvedInvocations(VariableDeclarationStatement smellyVarDecl) {
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
                        || methodName.equals(IDSSmell.PUT_ALL)*/) {
                            involvedInvocations.add(invocation);
                        }
                    }
                }
            }
        }
        return involvedInvocations;
    }

    private MethodInvocation refactorRemove(AST ast, MethodInvocation invocation) {
        MethodInvocation newInvocation = (MethodInvocation) ASTNode.copySubtree(ast, invocation);
        MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(ast, invocation);
        innerInvocation.setName(ast.newSimpleName(INDEX_OF_VALUE));
        innerInvocation.arguments().remove(0);
        newInvocation.arguments().clear();
        newInvocation.arguments().add(innerInvocation);
        return newInvocation;
    }

    private InfixExpression refactorContainsKey(AST ast, MethodInvocation invocation) {
        MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(ast, invocation);
        innerInvocation.setName(ast.newSimpleName(INDEX_OF_KEY));
        InfixExpression relationalExpr = ast.newInfixExpression();
        relationalExpr.setLeftOperand(innerInvocation);
        relationalExpr.setOperator(InfixExpression.Operator.GREATER_EQUALS);
        relationalExpr.setRightOperand(ast.newNumberLiteral(ZERO));
        return relationalExpr;
    }

    private Expression refactorContainsValue(AST targetAST, MethodInvocation invocation) {
        Expression newExpr;
        MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
        innerInvocation.setName(targetAST.newSimpleName(INDEX_OF_VALUE));
        InfixExpression relationalExpr = targetAST.newInfixExpression();
        relationalExpr.setLeftOperand(innerInvocation);
        relationalExpr.setOperator(InfixExpression.Operator.GREATER_EQUALS);
        relationalExpr.setRightOperand(targetAST.newNumberLiteral(ZERO));
        newExpr = relationalExpr;
        return newExpr;
    }

    private InfixExpression refactorIsEmpty(AST targetAST, MethodInvocation invocation) {
        MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
        innerInvocation.setName(targetAST.newSimpleName(SIZE));
        InfixExpression relationalExpr = targetAST.newInfixExpression();
        relationalExpr.setLeftOperand(innerInvocation);
        relationalExpr.setOperator(InfixExpression.Operator.EQUALS);
        relationalExpr.setRightOperand(targetAST.newNumberLiteral(ZERO));
        return relationalExpr;
    }

    private MethodInvocation refactorEntrySet(AST targetAST, MethodInvocation invocation) {
        MethodInvocation getEntrySetCall = targetAST.newMethodInvocation();
        getEntrySetCall.setName(targetAST.newSimpleName(GET_ENTRY_SET));
        getEntrySetCall.arguments().add(ASTNode.copySubtree(targetAST, invocation.getExpression()));
        return getEntrySetCall;
    }

    private boolean existsGetEntrySet(CompilationUnit compilationUnit) {
        List<MethodDeclaration> methodDeclarations = ASTUtilities.getMethodDeclarations(compilationUnit);
        boolean found = false;
        if (methodDeclarations != null) {
            for (int i = 0; i < methodDeclarations.size() && !found; i++) {
                MethodDeclaration methodDeclaration = methodDeclarations.get(i);
                // Set<Map.Entry<Integer, Object>> getEntrySet(SparseArray<Object> array)
                if (methodDeclaration.getName().getIdentifier().equals(GET_ENTRY_SET)) {
                    found = true;
                }
                //TODO Low Fine graine check: check if there is a method with same signature and return its name rather than boolean
                /*
                if (methodDeclaration.getReturnType2().isParameterizedType()) {
                    ParameterizedType retType = (ParameterizedType) methodDeclaration.getReturnType2();
                    if (retType.getType().isSimpleType()) {
                        SimpleType retTypeType = (SimpleType) retType.getType();
                        if (retTypeType.getName().getFullyQualifiedName().equals(SET)) {
                            if (retType.typeArguments().size() == 2) {
                                Type firstTypeArg = (Type) retType.typeArguments().get(0);
                                Type secondTypeArg = (Type) retType.typeArguments().get(1);
                                if (firstTypeArg.isSimpleType() && secondTypeArg.isSimpleType()) {
                                    if (((SimpleType) firstTypeArg).getName().getFullyQualifiedName().equals(INTEGER)
                                            && ((SimpleType) secondTypeArg).getName().getFullyQualifiedName().equals(OBJECT)) {

                                    }
                                }
                            }
                        }
                    }
                }
                */
            }
        }
        return found;
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
    private MethodDeclaration createGetEntrySetMethod(AST ast, ParameterizedType sparseArrayType) {
        // Return type: Set<Map.Entry<Integer, Object>>
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
}
