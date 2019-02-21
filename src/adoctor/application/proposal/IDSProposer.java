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
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class IDSProposer extends MethodSmellProposer {

    @Override
    public MethodProposal computeProposal(MethodSmell methodSmell) throws IOException {
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
        // Declaration of SparseArray<SecondType> instead of HashMap<Integer, SecondType>
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

        // TODO Proporre anche di cambiare automaticamente alcune invocazioni di metodi della stessa variabile di rif, se presenti
        //  Farsi una lista di statement proposti (quelli da modificare) e metterla in un nuovo campo di IDSProposal
        //  Attenzione che le MethodInvocation da cambiare possono essere immerse in ogni posto: fare una buona visita dell'intero metodo
        //  Con questa lista, settare il nuovo codice proposto, gli highlight attuali e proposti
        //  1: ottenre la lista delle variabili nei frammenti
        //  2: visitare intero methodDeclaration per ottenere ogni MethodInvocation
        //  3: scartare le invocazioni che non riguardano le variabili nei frammenti
        //  4: scartare le invocazioni dei metodi che non richiedono alcuna modifica (put, get ecc)
        //  5: Risalire allo statement di appartenenza delle methodinvocation rimaste
        //  6: Fare gli algoritmi per proporre la modifica per ogni statement da cambiare
        //  7: Mettere ogni statement proposto nella lista delle proposte
        //  8: Lavorare sul proposedcode e sugli highlights
        


        // TODO OPZIONALE: Aggiungere import di SparseArray se assente: serve il campo proposedImport in IDSProposal

        String actualCode = methodSmell.getMethod().getLegacyMethodBean().getTextContent();
        String proposedCode = actualCode.replace(varDecl.toString(), newVarDecl.toString());
        ArrayList<String> actualHighlights = new ArrayList<>();
        actualHighlights.add(varDecl.toString());
        ArrayList<String> proposedHighlights = new ArrayList<>();
        proposedHighlights.add(newVarDecl.toString());

        IDSProposal proposal = new IDSProposal();
        proposal.setMethodSmell(methodSmell);
        proposal.setProposedVarDecl(newVarDecl);
        proposal.setProposedCode(proposedCode);
        proposal.setActualHighlights(actualHighlights);
        proposal.setProposedHighlights(proposedHighlights);
        return proposal;
    }
}
