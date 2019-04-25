package adoctor.presentation.dialog;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.MethodSmell;
import adoctor.application.proposal.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings({"UseJBColor", "GtkPreferredJComboBoxRenderer"})
public class SmellDialog extends AbstractDialog {
    public static final String TITLE = "aDoctor - Smell List";
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
    private ArrayList<MethodSmell> methodSmells;
    private ProposalDriver proposalDriver;
    private MethodProposal methodProposal;

    private JPanel contentPane;
    private JComboBox<MethodSmell> boxSmell;
    private JTextPane paneDetails;
    private JTextArea areaActualCode;
    private JTextArea areaProposedCode;
    private JButton buttonApply;
    private JButton buttonBack;
    private JButton buttonUndo;

    public static void show(SmellCallback smellCallback, ArrayList<MethodSmell> smellMethodList, boolean[] selections, boolean undoExists) {
        SmellDialog smellDialog = new SmellDialog(smellCallback, smellMethodList, selections, undoExists);
        smellDialog.showInCenter();
    }

    private SmellDialog(SmellCallback smellCallback, ArrayList<MethodSmell> methodSmells, boolean[] selections, boolean undoExists) {
        init(smellCallback, methodSmells, selections, undoExists);
    }

    private void init(SmellCallback smellCallback, ArrayList<MethodSmell> methodSmells, boolean[] selections, boolean undoExists) {
        super.init(contentPane, TITLE, buttonApply);

        this.smellCallback = smellCallback;
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
        areaActualCode.setPreferredSize(null);
        areaProposedCode.setPreferredSize(null);

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

        //TODO Introdurre il DIFF


        String actualCode = selectedSmell.getMethod().getLegacyMethodBean().getTextContent();
        if (methodProposal != null) {
            prepareArea(areaActualCode, actualCode, methodProposal.getCurrentHighlights());
            String proposalCode = methodProposal.getProposedCode();
            prepareArea(areaProposedCode, proposalCode, methodProposal.getProposedHighlights());
        } else {
            prepareArea(areaActualCode, actualCode, null);
            prepareArea(areaProposedCode, "No proposal available", null);
        }
    }

    //TODO Introdurre il DIFF
    private void prepareArea(JTextArea area, String code, ArrayList<String> strings) {
        try {
            area.setText(code);
            area.setCaretPosition(0);
            Highlighter highlighter = area.getHighlighter();
            highlighter.removeAllHighlights();
            if (strings != null && strings.size() > 0) {
                Random r = new Random();
                int highlightEnd = 0;
                for (String string : strings) {
                    int highlightIndex = code.indexOf(string, highlightEnd);
                    highlightEnd = highlightIndex + string.length();
                    Color randomColor = new Color(0, 40 + r.nextInt(11) * 5, 200);
                    highlighter.addHighlight(highlightIndex, highlightEnd, new DefaultHighlighter.DefaultHighlightPainter(randomColor));
                }
            }
        } catch (BadLocationException e) {
            // When the index of the string to highlight is wrong
            e.printStackTrace();
        }
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

        void smellBack(SmellDialog analysisDialog);

        void smellQuit(SmellDialog analysisDialog);

        void smellUndo(SmellDialog analysisDialog);
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
