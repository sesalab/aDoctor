package adoctorr.presentation.dialog;

import adoctorr.application.bean.proposal.MethodProposal;
import adoctorr.application.bean.smell.MethodSmell;
import adoctorr.application.proposal.ProposalDriver;
import com.intellij.openapi.project.Project;

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

    private static final String DURABLE_WAKELOCK_DESCRIPTION = "Durable Wakelock is present when there is a " +
            "PowerManager.WakeLock instance that calls an acquire() without setting a timeout or without calling the " +
            "corresponding release()";
    private static final String EARLY_RESOURCE_BINDING_DESCRIPTION = "Early Resource Binding is present when an " +
            "Android system service is used in the onCreate(Bundle) method of an Activity subclass.";

    private JPanel contentPane;

    private JList<String> listSmell;

    private JLabel labelSmellName;
    private JLabel labelIcon;

    private JTextArea areaActualCode;
    private JTextArea areaProposedCode;
    private JLabel labelClassName;
    private JButton buttonApply;
    private JButton buttonQuit;

    private Project project;
    private ProposalDriver proposalDriver;
    private ArrayList<MethodSmell> smellMethodList;
    private ArrayList<MethodSmell> unresolvedSmellMethodList;
    private MethodProposal methodProposal;

    private SmellDialog(Project project, ArrayList<MethodSmell> smellMethodList) {
        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 9;
        int y = (screenSize.height - getHeight()) / 16;
        setLocation(x, y);
        getRootPane().setDefaultButton(buttonApply);
        setTitle("aDoctor - Smells' list");

        this.project = project;
        this.proposalDriver = new ProposalDriver();
        this.smellMethodList = smellMethodList;
        this.unresolvedSmellMethodList = new ArrayList<>();
        for (MethodSmell methodSmell : smellMethodList) {
            if (!methodSmell.isResolved()) {
                unresolvedSmellMethodList.add(methodSmell);
            }
        }
        this.methodProposal = null;

        // The smells' list
        DefaultListModel<String> listSmellModel = (DefaultListModel<String>) listSmell.getModel();
        for (MethodSmell methodSmell : unresolvedSmellMethodList) {
            listSmellModel.addElement(buildElement(methodSmell));
        }

        listSmell.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //This if statement prevents multiple fires
                if (!e.getValueIsAdjusting()) {
                    onSelectItem();
                }
            }
        });

        // Select the first smell of the list
        listSmell.setSelectedIndex(0);

        areaActualCode.setPreferredSize(null);
        areaProposedCode.setPreferredSize(null);

        buttonApply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onApplyRefactoring();
            }
        });

        buttonQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQuit();
            }
        });

        // call onQuit() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });
    }

    public static void show(Project project, ArrayList<MethodSmell> smellMethodList) {
        SmellDialog smellDialog = new SmellDialog(project, smellMethodList);

        smellDialog.pack();
        smellDialog.setVisible(true);
    }

    private void updateDetails() {
        MethodSmell selectedSmell = unresolvedSmellMethodList.get(listSmell.getSelectedIndex());

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

    private void onApplyRefactoring() {
        dispose();
        RefactoringDialog.show(methodProposal, project, smellMethodList);
    }

    private void onQuit() {
        dispose();
    }
}
