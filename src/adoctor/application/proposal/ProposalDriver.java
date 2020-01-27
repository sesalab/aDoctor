package adoctor.application.proposal;

import adoctor.application.proposal.proposers.ClassSmellProposer;
import adoctor.application.proposal.undo.Undo;
import adoctor.application.smell.ClassSmell;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.*;

public class ProposalDriver {

    private ArrayList<ClassSmellProposer> classSmellProposers;

    public ProposalDriver(ArrayList<ClassSmellProposer> classSmellProposers) {
        this.classSmellProposers = classSmellProposers;
    }

    public Undo computeProposal(ClassSmell classSmell) throws IOException, BadLocationException {
        if (classSmellProposers == null) {
            return null;
        }
        for (ClassSmellProposer proposer : classSmellProposers) {
            ASTRewrite astRewrite = proposer.computeProposal(classSmell);
            if (astRewrite != null) {
                File sourceFile = classSmell.getClassBean().getSourceFile();
                String javaFileContent = new String(Files.readAllBytes(Paths.get(sourceFile.getAbsolutePath())), StandardCharsets.UTF_8);
                Document document = new Document(javaFileContent);
                Hashtable<String, String> options = JavaCore.getOptions();
                // options.put(FORMATTER_TAB_CHAR, JavaCore.SPACE);
                options.put(FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT, FALSE);
                options.put(FORMATTER_COMMENT_FORMAT_JAVADOC_COMMENT, FALSE);
                options.put(FORMATTER_COMMENT_FORMAT_LINE_COMMENT, FALSE);

                // Proposal phase
                TextEdit rewriteEdit = astRewrite.rewriteAST(document, options);
                UndoEdit undoEdit = rewriteEdit.apply(document);
                /*
                CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options, ToolFactory.M_FORMAT_EXISTING);
                TextEdit formatEdit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, javaFileContent, 0, javaFileContent.length(), 0, null);
                UndoEdit undoEdit = formatEdit.apply(document);
                 */
                return new Undo(undoEdit, document);
            }
        }
        return null;
    }
}
