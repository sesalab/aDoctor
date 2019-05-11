package adoctor.presentation.dialog;

import adoctor.application.proposal.ProposalDriver;
import adoctor.application.proposal.proposers.*;
import adoctor.application.proposal.undo.Undo;
import adoctor.application.smell.ClassSmell;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.jface.text.BadLocationException;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings({"GtkPreferredJComboBoxRenderer", "unchecked"})
public class SmellDialog extends AbstractDialog {
    private static final String TITLE = "aDoctor - Smell List";
    private static final String baseHTML = "" +
            "<html>" +
            "<body>" +
            "<div style=\"margin:4px;\">" +
            "<div style=\"font-size:14px;\"><b>" + "%s" + "</b></div>" +
            "<div style=\"margin-left:8px;\">" + "%s" + "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    private static final String extendedHTML = "" +
            "<html>" +
            "<body>" +
            "<div style=\"margin-left:4px; font-size:14px\">" +
            "<div style=\"font-size:16px\"><b>Smell</b>: " + "%s" + "</div>" +
            "<div style=\"margin-left:8px;\"><b>Description</b>: " + "%s" + "</div>" +
            "<div style=\"margin-left:8px;\"><b>Class</b>: " + "%s" + "</div>" +
            "<div style=\"margin-left:8px;\"><b>Method</b>: " + "%s" + "</div>" +
            "</div>" +
            "</body>" +
            "</html>";

    private SmellCallback smellCallback;
    private Project project;
    private ArrayList<ClassSmell> classSmells;
    private ProposalDriver proposalDriver;
    private ClassSmell selectedSmell;
    private Undo undo;

    private JPanel contentPane;
    private JPanel panelList;
    private JComboBox<ClassSmell> boxSmell;
    private JTextPane paneDetails;
    private JPanel panelMain;
    private JLabel labelError;
    private JButton buttonApply;
    private JButton buttonBack;
    private JButton buttonUndo;

    private SmellDialog(SmellCallback smellCallback, Project project, ArrayList<ClassSmell> classSmells, boolean[] selections, boolean undoExists) {
        init(smellCallback, project, classSmells, selections, undoExists);
    }

    public static void show(SmellCallback smellCallback, Project project, ArrayList<ClassSmell> smellMethodList, boolean[] selections, boolean undoExists) {
        SmellDialog smellDialog = new SmellDialog(smellCallback, project, smellMethodList, selections, undoExists);
        smellDialog.showInCenter();
    }

    private void init(SmellCallback smellCallback, Project project, ArrayList<ClassSmell> classSmells, boolean[] selections, boolean undoExists) {
        super.init(contentPane, TITLE, buttonApply);

        this.smellCallback = smellCallback;
        this.project = project;
        this.classSmells = classSmells;
        ArrayList<ClassSmellProposer> classSmellProposers = new ArrayList<>();
        if (selections[0]) {
            classSmellProposers.add(new DWProposer());
        }
        if (selections[1]) {
            classSmellProposers.add(new ERBProposer());
        }
        if (selections[2]) {
            classSmellProposers.add(new IDSProposer());
        }
        if (selections[3]) {
            classSmellProposers.add(new ISProposer());
        }
        if (selections[4]) {
            classSmellProposers.add(new MIMProposer());
        }
        proposalDriver = new ProposalDriver(classSmellProposers);
        selectedSmell = null;
        undo = null;

        // The smell list
        boxSmell.setRenderer(new SmellRenderer());
        boxSmell.removeAllItems();
        for (ClassSmell classSmell : classSmells) {
            boxSmell.addItem(classSmell);
        }
        boxSmell.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSelectItem();
            }
        });
        boxSmell.setSelectedIndex(0); // Select the first smell of the list

        buttonApply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onApply();
            }
        });

        buttonBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBack();
            }
        });

        // The undo button
        buttonUndo.setVisible(undoExists);
        buttonUndo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onUndo();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });
    }

    private void updateDetails() {
        selectedSmell = classSmells.get(boxSmell.getSelectedIndex());

        // Updates the extended description
        String smellName = selectedSmell.getName();
        String smellClass = selectedSmell.getClassBean().getLegacyClassBean().getName();
        String smellDescription = selectedSmell.getDescription();
        String smellMethod = selectedSmell.getClassBean().getLegacyClassBean().getName();
        String text = String.format(extendedHTML, smellName, smellDescription, smellClass, smellMethod);
        paneDetails.setText(text);

        // Clear the main panel
        panelMain.removeAll();
        try {
            // Compute the proposal of the selected smell and then show the diff if no errors
            undo = proposalDriver.computeProposal(selectedSmell);
            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(selectedSmell.getClassBean().getSourceFile());
            if (undo == null || vf == null) {
                panelMain.add(labelError, BorderLayout.CENTER);
            } else {
                DocumentContent currentDocumentContent = DiffContentFactory.getInstance().createDocument(project, vf);
                if (currentDocumentContent == null) {
                    panelMain.add(labelError, BorderLayout.CENTER);
                } else {
                    String validDocumentContent = undo.getDocument().get().replaceAll("(\r\n|\r)", "\n");
                    Document proposedDocument = EditorFactory.getInstance().createDocument(validDocumentContent);
                    DocumentContent proposedDocumentContent = DiffContentFactory.getInstance().create(proposedDocument.getText(),
                            JavaClassFileType.INSTANCE);
                    SimpleDiffRequest request = new SimpleDiffRequest("Diff Panel", currentDocumentContent,
                            proposedDocumentContent, "Current Code", "Proposed Code");

                    // DiffPanel preparation
                    DiffRequestPanel diffRequestPanel = DiffManager.getInstance().createRequestPanel(project, new Disposable() {
                        @Override
                        public void dispose() {

                        }
                    }, null);
                    diffRequestPanel.putContextHints(DiffUserDataKeys.FORCE_READ_ONLY, true);
                    diffRequestPanel.setRequest(request);
                    JComponent panelDiffComponent = diffRequestPanel.getComponent();
                    int preferredWidth = panelList.getPreferredSize().width * 4;
                    int preferredHeight = panelList.getPreferredSize().height * 2;
                    panelDiffComponent.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
                    panelMain.add(panelDiffComponent, BorderLayout.CENTER);
                }
            }
        } catch (IOException | BadLocationException e) {
            panelMain.add(labelError, BorderLayout.CENTER);
            e.printStackTrace();
        }
        pack();
        setLocationRelativeTo(null);
    }

    private void onSelectItem() {
        updateDetails();
    }

    private void onApply() {
        smellCallback.smellApply(this, selectedSmell, undo);
    }

    private void onBack() {
        smellCallback.smellBack(this);
    }

    private void onQuit() {
        smellCallback.smellQuit(this);
    }

    private void onUndo() {
        int dialogResult = JOptionPane.showConfirmDialog(this, "Are you sure to undo the last refactoring?", "Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult == 0) {
            smellCallback.smellUndo(this);
        }
    }

    interface SmellCallback {
        void smellApply(SmellDialog smellDialog, ClassSmell targetSmell, Undo undo);

        void smellBack(SmellDialog smellDialog);

        void smellQuit(SmellDialog smellDialog);

        void smellUndo(SmellDialog smellDialog);
    }

    private class SmellRenderer extends BasicComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = new JLabel();
            ClassSmell classSmell = (ClassSmell) value;
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            String smellName = classSmell.getName();
            String smellClass = classSmell.getClassBean().getLegacyClassBean().getName();
            String text = String.format(baseHTML, smellName, smellClass);
            label.setText(text);
            return label;
        }
    }
}
