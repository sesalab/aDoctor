package adoctor.presentation.dialog;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.MethodSmell;
import adoctor.application.proposal.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

@SuppressWarnings("UseJBColor")
public class SmellDialog extends AbstractDialog {
    public static final String TITLE = "aDoctor - Smell list";

    private SmellCallback smellCallback;
    private ArrayList<MethodSmell> methodSmells;
    private ProposalDriver proposalDriver;
    private MethodProposal methodProposal;

    private JPanel contentPane;
    private JList<String> listSmell;
    private JLabel labelSmellName;
    private JLabel labelIcon;
    private JLabel labelClassName;
    private JTextArea areaActualCode;
    private JTextArea areaProposedCode;
    private JButton buttonApply;
    private JButton buttonBack;

    public static void show(SmellCallback smellCallback, ArrayList<MethodSmell> smellMethodList, boolean[] selections) {
        SmellDialog smellDialog = new SmellDialog(smellCallback, smellMethodList, selections);

        smellDialog.showInCenter();
    }

    private SmellDialog(SmellCallback smellCallback, ArrayList<MethodSmell> methodSmells, boolean[] selections) {
        init(smellCallback, methodSmells, selections);
    }

    private void init(SmellCallback smellCallback, ArrayList<MethodSmell> methodSmells, boolean[] selections) {
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

        areaActualCode.setPreferredSize(null);
        areaProposedCode.setPreferredSize(null);
        // The smell list
        DefaultListModel<String> listSmellModel = (DefaultListModel<String>) listSmell.getModel();
        for (MethodSmell methodSmell : methodSmells) {
            listSmellModel.addElement(buildElement(methodSmell));
        }
        listSmell.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // This if statement prevents multiple fires
                if (!e.getValueIsAdjusting()) {
                    onSelectItem();
                }
            }
        });
        listSmell.setSelectedIndex(0); // Select the first smell of the list


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

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });
    }

    private void updateDetails() {
        MethodSmell selectedSmell = methodSmells.get(listSmell.getSelectedIndex());

        // Selected Smell Description
        String className = selectedSmell.getMethod().getLegacyMethodBean().getBelongingClass().getName();
        String packageName = selectedSmell.getMethod().getLegacyMethodBean().getBelongingClass().getBelongingPackage();
        String classFullName = packageName + "." + className;
        labelClassName.setText(classFullName);
        labelSmellName.setText(selectedSmell.getSmellName());
        labelIcon.setToolTipText(selectedSmell.getSmellDescription());

        // Compute the proposal of the selected smell
        try {
            methodProposal = proposalDriver.computeProposal(selectedSmell);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String actualCode = selectedSmell.getMethod().getLegacyMethodBean().getTextContent();
        if (methodProposal != null) {
            prepareArea(areaActualCode, actualCode, methodProposal.getActualHighlights());
            String proposalCode = methodProposal.getProposedCode();
            prepareArea(areaProposedCode, proposalCode, methodProposal.getProposedHighlights());
        } else {
            prepareArea(areaActualCode, actualCode, null);
            prepareArea(areaProposedCode, "No proposal available", null);
        }
    }

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

    private String buildElement(MethodSmell methodSmell) {
        String methodName = methodSmell.getMethod().getLegacyMethodBean().getName();
        String smellName = methodSmell.getSmellName();
        return "" +
                "<html>" +
                "<p style=\"margin:4px;\">" +
                "<b>" + smellName + "</b>" +
                "</p>" +
                "<p style=\"margin-left:4px; margin-right:4px;\">" +
                methodName +
                "</p>" +
                "</html>";
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

    interface SmellCallback {
        void smellApply(SmellDialog smellDialog, MethodProposal methodProposal);

        void smellBack(SmellDialog analysisDialog);

        void smellQuit(SmellDialog analysisDialog);
    }
}
