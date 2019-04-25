package adoctor.presentation.dialog;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.MethodSmell;
import adoctor.application.proposal.*;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

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
            "<div><b>Smell</b>: " + "%s" + "</div>" +
            "<div style=\"margin-left:8px;\"><b>Description</b>: " + "%s" + "</div>" +
            "<div style=\"margin-left:8px;\"><b>Class</b>: " + "%s" + "</div>" +
            "<div style=\"margin-left:8px;\"><b>Method</b>: " + "%s" + "</div>" +
            "</div>" +
            "</body>" +
            "</html>";

    private SmellCallback smellCallback;
    private Project project;
    private ArrayList<MethodSmell> methodSmells;
    private ProposalDriver proposalDriver;
    private MethodProposal methodProposal;

    private JPanel contentPane;
    private JPanel panelList;
    private JComboBox<MethodSmell> boxSmell;
    private JTextPane paneDetails;
    private JPanel panelDiff;
    private JButton buttonApply;
    private JButton buttonBack;
    private JButton buttonUndo;

    public static void show(SmellCallback smellCallback, Project project, ArrayList<MethodSmell> smellMethodList, boolean[] selections, boolean undoExists) {
        SmellDialog smellDialog = new SmellDialog(smellCallback, project, smellMethodList, selections, undoExists);
        smellDialog.showInCenter();
    }

    private SmellDialog(SmellCallback smellCallback, Project project, ArrayList<MethodSmell> methodSmells, boolean[] selections, boolean undoExists) {
        init(smellCallback, project, methodSmells, selections, undoExists);
    }

    private void init(SmellCallback smellCallback, Project project, ArrayList<MethodSmell> methodSmells, boolean[] selections, boolean undoExists) {
        super.init(contentPane, TITLE, buttonApply);

        this.smellCallback = smellCallback;
        this.project = project;
        ArrayList<MethodSmellProposer> methodSmellProposers = new ArrayList<>();
        if (selections[0]) {
            methodSmellProposers.add(new DWProposer());
        }
        if (selections[1]) {
            methodSmellProposers.add(new ERBProposer());
        }
        if (selections[2]) {
            methodSmellProposers.add(new IDSProposer());
        }
        this.proposalDriver = new ProposalDriver(methodSmellProposers);
        this.methodSmells = methodSmells;
        this.methodProposal = null;

        buttonUndo.setVisible(undoExists);

        // The smell list
        this.boxSmell.setRenderer(new SmellRenderer());
        boxSmell.removeAllItems();
        for (MethodSmell methodSmell : methodSmells) {
            boxSmell.addItem(methodSmell);
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
        MethodSmell selectedSmell = methodSmells.get(boxSmell.getSelectedIndex());

        // Updates the extended description
        String smellName = selectedSmell.getSmellName();
        String smellClass = selectedSmell.getMethod().getLegacyMethodBean().getBelongingClass().getName();
        String smellDescription = selectedSmell.getSmellDescription();
        String smellMethod = selectedSmell.getMethod().getLegacyMethodBean().getName();
        String text = String.format(extendedHTML, smellName, smellDescription, smellClass, smellMethod);
        paneDetails.setText(text);

        // Compute the proposal of the selected smell
        try {
            methodProposal = proposalDriver.computeProposal(selectedSmell);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Diff
        String currentCode = selectedSmell.getMethod().getLegacyMethodBean().getTextContent();
        String proposalCode = methodProposal.getProposedCode();
        DocumentContent currentDocument = DiffContentFactory.getInstance().create(currentCode);
        DocumentContent proposedDocument = DiffContentFactory.getInstance().create(proposalCode);
        SimpleDiffRequest request = new SimpleDiffRequest("Diff Panel", currentDocument, proposedDocument, "Current Code", "Proposed Code");
        DiffRequestPanel diffRequestPanel = DiffManager.getInstance().createRequestPanel(project, new Disposable() {
            @Override
            public void dispose() {

            }
        }, null);
        diffRequestPanel.setRequest(request);
        JComponent diffPanelComponent = diffRequestPanel.getComponent();
        // Resize
        int preferredWidth = panelList.getPreferredSize().width * 4;
        int preferredHeight = panelList.getPreferredSize().height * 2;
        diffPanelComponent.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        panelDiff.removeAll();
        panelDiff.add(diffPanelComponent, BorderLayout.CENTER);
    }

    private void onSelectItem() {
        updateDetails();
    }

    private void onApply() {
        smellCallback.smellApply(this, methodProposal);
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
        void smellApply(SmellDialog smellDialog, MethodProposal methodProposal);

        void smellBack(SmellDialog smellDialog);

        void smellQuit(SmellDialog smellDialog);

        void smellUndo(SmellDialog smellDialog);
    }

    private class SmellRenderer extends BasicComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = new JLabel();
            MethodSmell methodSmell = (MethodSmell) value;
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            String smellName = methodSmell.getSmellName();
            String smellClass = methodSmell.getMethod().getLegacyMethodBean().getBelongingClass().getName();
            String text = String.format(baseHTML, smellName, smellClass);
            label.setText(text);
            return label;
        }
    }
}
