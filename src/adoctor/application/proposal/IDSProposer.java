package adoctor.application.proposal;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.Method;
import adoctor.application.bean.proposal.IDSProposal;
import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.IDSSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class IDSProposer extends MethodSmellProposer {

    @Override
    public MethodProposal computeProposal(MethodSmell methodSmell) throws IOException {
        // Preconditions check
        if (!(methodSmell instanceof IDSSmell)) {
            return null;
        }
        IDSSmell idsSmell = (IDSSmell) methodSmell;
        Method method = idsSmell.getMethod();
        if (method == null) {
            return null;
        }
        File sourceFile = method.getSourceFile();
        if (sourceFile == null) {
            return null;
        }
        CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(sourceFile);
        if (compilationUnit == null) {
            return null;
        }
        MethodDeclaration methodDeclaration = ASTUtilities.getMethodDeclarationFromContent(compilationUnit, method.getLegacyMethodBean().getTextContent());
        if (methodDeclaration == null) {
            return null;
        }
        VariableDeclarationStatement varDecl = idsSmell.getVariableDeclarationStatement();
        if (varDecl == null) {
            return null;
        }

        AST targetAST = compilationUnit.getAST();
        // Changes of Declaration of SparseArray<SecondType> to HashMap<Integer, SecondType>
        ParameterizedType newType = targetAST.newParameterizedType(targetAST.newSimpleType(targetAST.newSimpleName(IDSSmell.SPARSE_ARRAY)));
        ParameterizedType parType = (ParameterizedType) varDecl.getType();
        SimpleType secondType = (SimpleType) parType.typeArguments().get(1);
        SimpleType newSimpleType = (SimpleType) ASTNode.copySubtree(targetAST, secondType);
        List<Type> typeParameters = newType.typeArguments();
        typeParameters.add(newSimpleType);
        VariableDeclarationStatement newVarDecl = (VariableDeclarationStatement) ASTNode.copySubtree(targetAST, varDecl);
        newVarDecl.setType(newType);
        // Changes of HashMap<> constructor to SparseArray<> one
        List<VariableDeclarationFragment> fragments = newVarDecl.fragments();
        for (VariableDeclarationFragment fragment : fragments) {
            List<ClassInstanceCreation> creations = ASTUtilities.getClassInstanceCreations(fragment);
            if (!creations.isEmpty()) {
                ClassInstanceCreation creation = creations.get(0);
                ParameterizedType newConstructor = targetAST.newParameterizedType(targetAST.newSimpleType(targetAST.newSimpleName(IDSSmell.SPARSE_ARRAY)));
                creation.setType(newConstructor);
            }
        }
        // List of involved reference variables
        List<SimpleName> identifiers = ASTUtilities.getSimpleNames(newVarDecl);
        List<String> variables = new ArrayList<>();
        for (SimpleName identifier : identifiers) {
            if (identifier.isDeclaration()) {
                variables.add(identifier.getIdentifier());
            }
        }
        // List of involved method invocations
        List<MethodInvocation> allInvocations = ASTUtilities.getMethodInvocations(varDecl.getParent());
        List<MethodInvocation> invocations = new ArrayList<>();
        for (MethodInvocation invocation : allInvocations) {
            if (variables.contains(invocation.getExpression().toString())) {
                String methodName = invocation.getName().getIdentifier();
                if (methodName.equals(IDSProposal.REMOVE) && invocation.arguments().size() == 2
                        || methodName.equals(IDSProposal.CONTAINS_KEY)
                        || methodName.equals(IDSProposal.CONTAINS_VALUE)
                        || methodName.equals(IDSProposal.IS_EMPTY)
                        /*
                        || methodName.equals(IDSProposal.ENTRY_SET)
                        || methodName.equals(IDSProposal.KEY_SET)
                        || methodName.equals(IDSProposal.VALUES)
                        || methodName.equals(IDSProposal.PUT_ALL)*/) {
                    invocations.add(invocation);
                }
            }
        }
        System.out.println(invocations);
        // Creation of list of the new Expressions (mainly MethodInvocations, but not only)
        List<AbstractMap.SimpleEntry<MethodInvocation, Expression>> replacements = new ArrayList<>();
        for (MethodInvocation invocation : invocations) {
            String methodName = invocation.getName().getIdentifier();
            Expression newExpr = null;
            switch (methodName) {
                // remove(int, obj) --> remove(indexOfValue(obj))
                case IDSProposal.REMOVE: {
                    MethodInvocation newInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
                    MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
                    innerInvocation.setName(targetAST.newSimpleName(IDSProposal.INDEX_OF_VALUE));
                    innerInvocation.arguments().remove(0);
                    newInvocation.arguments().clear();
                    newInvocation.arguments().add(innerInvocation);
                    newExpr = newInvocation;
                    break;
                }
                // containsKey(int) --> map.indexOfKey(int) >= 0
                case IDSProposal.CONTAINS_KEY: {
                    MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
                    innerInvocation.setName(targetAST.newSimpleName(IDSProposal.INDEX_OF_KEY));
                    InfixExpression relationalExpr = targetAST.newInfixExpression();
                    relationalExpr.setLeftOperand(innerInvocation);
                    relationalExpr.setOperator(InfixExpression.Operator.GREATER_EQUALS);
                    relationalExpr.setRightOperand(targetAST.newNumberLiteral("0"));
                    newExpr = relationalExpr;
                    break;
                }
                // containsKey(obj) --> indexOfValue(obj) >= 0
                case IDSProposal.CONTAINS_VALUE: {
                    MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
                    innerInvocation.setName(targetAST.newSimpleName(IDSProposal.INDEX_OF_VALUE));
                    InfixExpression relationalExpr = targetAST.newInfixExpression();
                    relationalExpr.setLeftOperand(innerInvocation);
                    relationalExpr.setOperator(InfixExpression.Operator.GREATER_EQUALS);
                    relationalExpr.setRightOperand(targetAST.newNumberLiteral("0"));
                    newExpr = relationalExpr;
                    break;
                }
                // isEmpty() --> size() == 0
                case IDSProposal.IS_EMPTY: {
                    MethodInvocation innerInvocation = (MethodInvocation) ASTNode.copySubtree(targetAST, invocation);
                    innerInvocation.setName(targetAST.newSimpleName(IDSProposal.SIZE));
                    InfixExpression relationalExpr = targetAST.newInfixExpression();
                    relationalExpr.setLeftOperand(innerInvocation);
                    relationalExpr.setOperator(InfixExpression.Operator.EQUALS);
                    relationalExpr.setRightOperand(targetAST.newNumberLiteral("0"));
                    newExpr = relationalExpr;
                    break;
                }
                /*
                // entrySet() -->
                case IDSProposal.ENTRY_SET: {
                    break;
                }
                // keySet() -->
                case IDSProposal.KEY_SET: {
                    break;
                }
                // values() -->
                case IDSProposal.VALUES: {
                    break;
                }
                case IDSProposal.PUT_ALL: {
                    break;
                }
                */
            }
            replacements.add(new AbstractMap.SimpleEntry<>(invocation, newExpr));
        }
        System.out.println(replacements);
        // TODO Bisognerebbe costruire una lista di coppie (SimpleEntry va bene) e darla a proposal

        // TODO OPZIONALE: Aggiungere import di SparseArray se assente: serve il campo proposedImport in IDSProposal
        String actualCode = methodSmell.getMethod().getLegacyMethodBean().getTextContent();
        String proposedCode = actualCode.replace(varDecl.toString(), newVarDecl.toString());
        for (AbstractMap.SimpleEntry<MethodInvocation, Expression> entry : replacements) {
            proposedCode = proposedCode.replace(entry.getKey().toString(), entry.getValue().toString());
        }

        ArrayList<String> actualHighlights = new ArrayList<>();
        actualHighlights.add(varDecl.toString());
        ArrayList<String> proposedHighlights = new ArrayList<>();
        proposedHighlights.add(newVarDecl.toString());
        for (AbstractMap.SimpleEntry<MethodInvocation, Expression> entry : replacements) {
            actualHighlights.add(entry.getKey().toString());
            proposedHighlights.add(entry.getValue().toString());
        }

        IDSProposal proposal = new IDSProposal();
        proposal.setMethodSmell(methodSmell);
        proposal.setProposedVarDecl(newVarDecl);
        proposal.setInvocationReplacements(replacements);
        proposal.setProposedCode(proposedCode);
        proposal.setActualHighlights(actualHighlights);
        proposal.setProposedHighlights(proposedHighlights);
        return proposal;
    }
}
