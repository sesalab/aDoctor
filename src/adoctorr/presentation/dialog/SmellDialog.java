package adoctorr.presentation.dialog;

import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.bean.smell.MethodSmell;
import adoctorr.application.proposal.ProposalDriver;

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

public class SmellDialog extends JDialog {
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

    public static void show(SmellCallback smellCallback, ArrayList<MethodSmell> smellMethodList) {
        SmellDialog smellDialog = new SmellDialog(smellCallback, smellMethodList);

        smellDialog.pack();
        smellDialog.setVisible(true);
    }

    private SmellDialog(SmellCallback smellCallback, ArrayList<MethodSmell> methodSmells) {
        this.smellCallback = smellCallback;
        this.proposalDriver = new ProposalDriver();
        this.methodSmells = methodSmells;
        this.methodProposal = null;

        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 9;
        int y = (screenSize.height - getHeight()) / 16;
        setLocation(x, y);
        getRootPane().setDefaultButton(buttonApply);
        setTitle(TITLE);

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
        String className = selectedSmell.getMethodBean().getBelongingClass().getName();
        String packageName = selectedSmell.getMethodBean().getBelongingClass().getBelongingPackage();
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

        String actualCode = selectedSmell.getMethodBean().getTextContent();
        prepareArea(areaActualCode, actualCode, methodProposal.getActualCodeToHighlightList());
        if (methodProposal != null) {
            String proposalCode = methodProposal.proposalToString();
            prepareArea(areaProposedCode, proposalCode, methodProposal.getProposedCodeToHighlightList());
        }
    }

    private void prepareArea(JTextArea area, String code, ArrayList<String> highlightList) {
        try {
            area.setText(code);
            area.setCaretPosition(0);
            Highlighter actualHighlighter = area.getHighlighter();
            actualHighlighter.removeAllHighlights();
            if (highlightList != null && highlightList.size() > 0) {
                for (String actualCodeToHighlight : highlightList) {
                    int highlightIndex = code.indexOf(actualCodeToHighlight);
                    actualHighlighter.addHighlight(highlightIndex, highlightIndex + actualCodeToHighlight.length(), DefaultHighlighter.DefaultPainter);
                }
            }
        } catch (BadLocationException e) {
            // When the index of the string to highlight is wrong
            e.printStackTrace();
        }
    }

    private String buildElement(MethodSmell methodSmell) {
        String methodName = methodSmell.getMethodBean().getName();
        String smellName = methodSmell.getSmellName();
        return "" +
                "<html>" +
                "<p>" +
                "<b>" + smellName + "</b>" +
                "</p>" +
                "<p>" +
                "" + methodName + "" +
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
