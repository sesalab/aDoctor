package adoctorr.application.refactoring;

import adoctorr.application.ast.ASTUtilities;
import adoctorr.application.bean.proposal.DWProposal;
import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.bean.smell.MethodSmell;
import beans.MethodBean;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import process.FileUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class DWRefactorer extends MethodSmellRefactorer {

    public DWRefactorer() {

    }

    @Override
    public boolean applyRefactoring(MethodProposal methodProposal) throws BadLocationException, IOException {
        if (!(methodProposal instanceof DWProposal)) {
            return false;
        }
        DWProposal dwProposal = (DWProposal) methodProposal;
        MethodDeclaration proposedMethodDeclaration = dwProposal.getProposedMethodDeclaration();

        MethodSmell methodSmell = dwProposal.getMethodSmell();
        File sourceFile = methodSmell.getSourceFile();
        MethodBean methodBean = methodSmell.getMethodBean();
        CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(sourceFile);
        // MethodDeclaration to be replaced
        MethodDeclaration targetMethodDeclaration = ASTUtilities.getMethodDeclarationFromContent(methodBean.getTextContent(), compilationUnit);
        if (targetMethodDeclaration == null) {
            return false;
        }

        // Builds the Document object
        String targetSource = FileUtilities.readFile(sourceFile.getAbsolutePath());
        Document document = new Document(targetSource);

        // Refactoring phase
        AST targetAST = compilationUnit.getAST();
        ASTRewrite rewriter = ASTRewrite.create(targetAST);
        rewriter.replace(targetMethodDeclaration, proposedMethodDeclaration, null);

        TextEdit edits = rewriter.rewriteAST(document, JavaCore.getDefaultOptions()); // With JavaCore Options we keep the code format settings, so the \n

        // TODO: Implementare uno stack di Undo
        // The UndoEdit could be used on the same document to reverse the changes
        UndoEdit undoEdit = edits.apply(document, TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS);
        String documentContent = document.get();

        // Overwrite directly the file
        PrintWriter pw = new PrintWriter(new FileOutputStream(sourceFile, false));
        pw.print(documentContent);
        pw.flush(); // Important

        return true;
    }
}