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
    public static final String DURABLE_WAKELOCK_DESCRIPTION = "Durable Wakelock is present when there is a " +
            "PowerManager.WakeLock instance that calls an acquire() without setting a timeout or without calling the " +
            "corresponding release()";
    public static final String EARLY_RESOURCE_BINDING_DESCRIPTION = "Early Resource Binding is present when an " +
            "Android system service is used in the onCreate(Bundle) method of an Activity subclass.";

    private SmellCallback smellCallback;
    private ArrayList<MethodSmell> unresolvedMethodSmells;
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
    private JButton buttonQuit;

    public static void show(SmellCallback smellCallback, ArrayList<MethodSmell> smellMethodList) {
        SmellDialog smellDialog = new SmellDialog(smellCallback, smellMethodList);

        smellDialog.pack();
        smellDialog.setVisible(true);
    }

    private SmellDialog(SmellCallback smellCallback, ArrayList<MethodSmell> methodSmells) {
        this.smellCallback = smellCallback;
        this.proposalDriver = new ProposalDriver();
        this.unresolvedMethodSmells = new ArrayList<>();
        for (MethodSmell methodSmell : methodSmells) {
            if (!methodSmell.isResolved()) {
                unresolvedMethodSmells.add(methodSmell);
            }
        }
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
        for (MethodSmell methodSmell : unresolvedMethodSmells) {
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

        buttonQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQuit();
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
        MethodSmell selectedSmell = unresolvedMethodSmells.get(listSmell.getSelectedIndex());

        // Selected Smell Description
        String className = selectedSmell.getMethodBean().getBelongingClass().getName();
        String packageName = selectedSmell.getMethodBean().getBelongingClass().getBelongingPackage();
        String classFullName = packageName + "." + className;
        labelSmellName.setText(MethodSmell.getSmellName(selectedSmell.getSmellType()));
        labelClassName.setText(classFullName);
        int smellType = selectedSmell.getSmellType();
        switch (smellType) {
            case MethodSmell.DURABLE_WAKELOCK: {
                labelIcon.setToolTipText(DURABLE_WAKELOCK_DESCRIPTION);
                break;
            }
            case MethodSmell.EARLY_RESOURCE_BINDING: {
                labelIcon.setToolTipText(EARLY_RESOURCE_BINDING_DESCRIPTION);
                break;
            }
        }

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
        String smellName = MethodSmell.getSmellName(methodSmell.getSmellType());
        return "" +
                "<html>" +
                "<p style=\"font-size:10px\">" +
                "<b>" + smellName + "</b>" +
                "</p>" +
                "<p style=\"font-size:9px\">" +
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

    private void onQuit() {
        smellCallback.smellQuit(this);
    }

    interface SmellCallback {
        void smellApply(SmellDialog smellDialog, MethodProposal methodProposal);

        void smellQuit(SmellDialog analysisDialog);
    }
}
