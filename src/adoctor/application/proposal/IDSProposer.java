package adoctor.application.proposal;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.proposal.IDSProposal;
import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.IDSSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class IDSProposer extends MethodSmellProposer {

    @Override
    public MethodProposal computeProposal(MethodSmell methodSmell) {
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
        ParameterizedType newType = targetAST.newParameterizedType(targetAST.newSimpleType(targetAST.newSimpleName(IDSSmell.SPARSE_ARRAY)));
        ParameterizedType parType = (ParameterizedType) smellyVarDecl.getType();
        SimpleType secondType = (SimpleType) parType.typeArguments().get(1);
        SimpleType newSimpleType = (SimpleType) ASTNode.copySubtree(targetAST, secondType);
        List<Type> typeParameters = newType.typeArguments();
        typeParameters.add(newSimpleType);
        VariableDeclarationStatement newVarDecl = (VariableDeclarationStatement) ASTNode.copySubtree(targetAST, smellyVarDecl);
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
        List<MethodInvocation> allInvocations = ASTUtilities.getMethodInvocations(smellyVarDecl.getParent());
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

        // TODO OPZIONALE: Aggiungere import di SparseArray se assente: serve il campo proposedImport in IDSProposal
        String actualCode = methodSmell.getMethod().getLegacyMethodBean().getTextContent();
        String proposedCode = actualCode.replace(smellyVarDecl.toString(), newVarDecl.toString());
        for (AbstractMap.SimpleEntry<MethodInvocation, Expression> entry : replacements) {
            proposedCode = proposedCode.replace(entry.getKey().toString(), entry.getValue().toString());
        }

        ArrayList<String> currentHighlights = new ArrayList<>();
        currentHighlights.add(smellyVarDecl.toString());
        ArrayList<String> proposedHighlights = new ArrayList<>();
        proposedHighlights.add(newVarDecl.toString());
        for (AbstractMap.SimpleEntry<MethodInvocation, Expression> entry : replacements) {
            currentHighlights.add(entry.getKey().toString());
            proposedHighlights.add(entry.getValue().toString());
        }

        IDSProposal proposal = new IDSProposal();
        proposal.setMethodSmell(methodSmell);
        proposal.setProposedVarDecl(newVarDecl);
        proposal.setInvocationReplacements(replacements);
        proposal.setProposedCode(proposedCode);
        proposal.setCurrentHighlights(currentHighlights);
        proposal.setProposedHighlights(proposedHighlights);
        return proposal;
    }
}
