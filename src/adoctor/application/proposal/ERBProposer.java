package adoctor.application.proposal;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.proposal.ERBProposal;
import adoctor.application.bean.smell.ERBSmell;
import adoctor.application.bean.smell.MethodSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ERBProposer extends MethodSmellProposer {

    @Override
    public ERBProposal computeProposal(MethodSmell methodSmell) {
        if (methodSmell == null) {
            return null;
        }
        if (!(methodSmell instanceof ERBSmell)) {
            return null;
        }
        ERBSmell erbSmell = (ERBSmell) methodSmell;
        MethodDeclaration smellyOnCreate = erbSmell.getMethod().getMethodDecl();
        if (smellyOnCreate == null) {
            return null;
        }
        Statement requestStatement = erbSmell.getRequestStatement();
        if (requestStatement == null) {
            return null;
        }
        AST targetAST = smellyOnCreate.getAST();

        // Look for public|protected void onResume()
        boolean foundOnResume = false;
        CompilationUnit compilationUnit = (CompilationUnit) smellyOnCreate.getRoot();
        MethodDeclaration onResume = ASTUtilities.getMethodDeclarationFromName(compilationUnit, ERBSmell.ONRESUME_NAME);
        if (onResume != null) {
            Type returnType = onResume.getReturnType2();
            if (returnType != null && returnType.toString().equals(ERBSmell.ONCREATE_TYPE)) {
                boolean found = false;
                int i = 0;
                List modifierList = onResume.modifiers();
                int n = modifierList.size();
                while (!found && i < n) {
                    IExtendedModifier modifier = (IExtendedModifier) modifierList.get(i);
                    if (modifier.toString().equals(ERBSmell.ONCREATE_SCOPE1) || modifier.toString().equals(ERBSmell.ONCREATE_SCOPE2)) {
                        List parameters = onResume.parameters();
                        if (parameters == null || parameters.size() == 0) {
                            found = true;
                        }
                    }
                    i++;
                }
                foundOnResume = found;
            }
        }

        // Creation of the new onResume
        MethodDeclaration newOnResume;
        if (foundOnResume) {
            newOnResume = (MethodDeclaration) ASTNode.copySubtree(targetAST, onResume);
        } else {
            SimpleName onResumeIdentifier = targetAST.newSimpleName(ERBSmell.ONRESUME_NAME);
            Modifier onResumePublicModifier = targetAST.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
            Block onResumeBody = targetAST.newBlock();

            newOnResume = targetAST.newMethodDeclaration();
            newOnResume.setName(onResumeIdentifier);
            newOnResume.modifiers().add(onResumePublicModifier);
            newOnResume.setBody(onResumeBody);
        }

        // Creation of the new statement for the onResume and removal from the new onCreate
        MethodDeclaration newOnCreate = (MethodDeclaration) ASTNode.copySubtree(targetAST, smellyOnCreate);
        ExpressionStatement requestExpressionStatement = ASTUtilities.getExpressionStatementFromContent(newOnCreate, requestStatement.toString());
        if (requestExpressionStatement == null) {
            return null;
        }
        Statement newRequestStatement = targetAST.newExpressionStatement((Expression) ASTNode.copySubtree(targetAST, requestExpressionStatement.getExpression()));
        Block requestBlock = ASTUtilities.getBlockFromContent(newOnCreate, erbSmell.getRequestBlock().toString());
        if (requestBlock == null) {
            return null;
        }
        List<Statement> statements = (List<Statement>) requestBlock.statements();
        statements.remove(requestExpressionStatement);

        // Addition of the new statement at the bottom of the new onResume
        List<Statement> onResumeStatements = (List<Statement>) newOnResume.getBody().statements();
        onResumeStatements.add(newRequestStatement);

        // Highlights
        ArrayList<String> currentHighlights = new ArrayList<>();
        currentHighlights.add(requestStatement.toString());
        ArrayList<String> proposedHighlights = new ArrayList<>();
        if (foundOnResume) {
            proposedHighlights.add(newRequestStatement.toString());
        } else {
            proposedHighlights.add(newOnResume.toString());
        }

        ERBProposal proposal = new ERBProposal();
        proposal.setMethodSmell(erbSmell);
        proposal.setProposedOnCreate(newOnCreate);
        proposal.setCurrentOnResume(onResume);
        proposal.setProposedOnResume(newOnResume);
        proposal.setProposedCode(newOnCreate.toString() + "\n" + newOnResume.toString());
        proposal.setCurrentHighlights(currentHighlights);
        proposal.setProposedHighlights(proposedHighlights);
        return proposal;
    }
}
