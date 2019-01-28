package adoctorr.application.proposal;

import adoctorr.application.ast.ASTUtilities;
import adoctorr.application.bean.proposal.ERBProposal;
import adoctorr.application.bean.smell.ERBSmell;
import adoctorr.application.bean.smell.MethodSmell;
import beans.MethodBean;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ERBProposer extends MethodSmellProposer {

    public ERBProposer() {

    }

    @Override
    public ERBProposal computeProposal(MethodSmell methodSmell) throws IOException {
        if (!(methodSmell instanceof ERBSmell)) {
            return null;
        }
        ERBSmell erbSmell = (ERBSmell) methodSmell;
        File sourceFile = erbSmell.getSourceFile();
        MethodBean methodBean = erbSmell.getMethodBean();
        CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(sourceFile);
        MethodDeclaration onCreateMethodDeclaration = ASTUtilities.getMethodDeclarationFromContent(methodBean.getTextContent(), compilationUnit);
        if (onCreateMethodDeclaration != null) {
            Statement requestStatement = erbSmell.getRequestStatement();
            if (requestStatement != null) {
                AST targetAST = compilationUnit.getAST();

                ArrayList<String> actualCodeToHighlightList = new ArrayList<>();
                actualCodeToHighlightList.add(requestStatement.toString());
                ArrayList<String> proposedCodeToHighlightList = new ArrayList<>();
                // Only for public|protected void onResume()
                boolean foundOnResume = false;
                MethodDeclaration proposedOnResumeMethodDeclaration = ASTUtilities.getMethodDeclarationFromName(ERBSmell.ONRESUME_NAME, compilationUnit);
                if (proposedOnResumeMethodDeclaration != null) {
                    Type returnType = proposedOnResumeMethodDeclaration.getReturnType2();
                    if (returnType != null && returnType.toString().equals(ERBSmell.ONCREATE_TYPE)) {
                        boolean found = false;
                        int i = 0;
                        List modifierList = proposedOnResumeMethodDeclaration.modifiers();
                        int n = modifierList.size();
                        while (!found && i < n) {
                            IExtendedModifier modifier = (IExtendedModifier) modifierList.get(i);
                            if (modifier.toString().equals(ERBSmell.ONCREATE_SCOPE1) || modifier.toString().equals(ERBSmell.ONCREATE_SCOPE2)) {
                                List parameters = proposedOnResumeMethodDeclaration.parameters();
                                if (parameters == null || parameters.size() == 0) {
                                    found = true;
                                }
                            }
                            i++;
                        }
                        foundOnResume = found;
                    }
                }

                // Create the new statement for onResume
                ExpressionStatement requestExpressionStatementTEMP = (ExpressionStatement) requestStatement;
                ExpressionStatement requestExpressionStatement = ASTUtilities.getExpressionStatementFromContent(requestExpressionStatementTEMP.toString(), onCreateMethodDeclaration);
                if (requestExpressionStatement != null) {
                    Expression requestExpression = requestExpressionStatement.getExpression();
                    Statement newRequestStatement = targetAST.newExpressionStatement((Expression) ASTNode.copySubtree(targetAST, requestExpression));

                    // Remove from onCreate
                    Block requestBlock = ASTUtilities.getBlockFromContent(erbSmell.getRequestBlock().toString(), onCreateMethodDeclaration);
                    if (requestBlock == null) {
                        return null;
                    }
                    List<Statement> statementList = (List<Statement>) requestBlock.statements();
                    statementList.remove(requestExpressionStatement);

                    MethodDeclaration actualOnResumeMethodDeclaration = null;
                    if (!foundOnResume) {
                        SimpleName onResumeIdentifier = targetAST.newSimpleName(ERBSmell.ONRESUME_NAME);
                        Modifier onResumePublicModifier = targetAST.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
                        Block onResumeBody = targetAST.newBlock();

                        proposedOnResumeMethodDeclaration = targetAST.newMethodDeclaration();
                        proposedOnResumeMethodDeclaration.setName(onResumeIdentifier);
                        proposedOnResumeMethodDeclaration.modifiers().add(onResumePublicModifier);
                        proposedOnResumeMethodDeclaration.setBody(onResumeBody);
                    } else {
                        actualOnResumeMethodDeclaration = (MethodDeclaration) ASTNode.copySubtree(targetAST, proposedOnResumeMethodDeclaration);
                    }

                    // Add at the bottom of the onResume
                    List<Statement> onResumeStatementList = (List<Statement>) proposedOnResumeMethodDeclaration.getBody().statements();
                    onResumeStatementList.add(newRequestStatement);

                    if (!foundOnResume) {
                        String onResumeMethodDeclarationString = proposedOnResumeMethodDeclaration.toString();
                        proposedCodeToHighlightList.add(onResumeMethodDeclarationString);
                    } else {
                        proposedCodeToHighlightList.add(newRequestStatement.toString());
                    }

                    ERBProposal proposalMethodBean = new ERBProposal();
                    proposalMethodBean.setMethodSmell(erbSmell);
                    proposalMethodBean.setProposedOnCreate(onCreateMethodDeclaration);
                    proposalMethodBean.setActualOnResume(actualOnResumeMethodDeclaration);
                    proposalMethodBean.setProposedOnResume(proposedOnResumeMethodDeclaration);
                    proposalMethodBean.setActualCodeToHighlightList(actualCodeToHighlightList);
                    proposalMethodBean.setProposedCodeToHighlightList(proposedCodeToHighlightList);
                    return proposalMethodBean;
                }
            }
        }
        return null;
    }
}
