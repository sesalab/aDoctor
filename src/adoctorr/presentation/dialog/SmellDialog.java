package adoctorr.presentation.dialog;

import adoctorr.application.bean.proposal.DurableWakelockProposalMethodBean;
import adoctorr.application.bean.proposal.EarlyResourceBindingProposalMethodBean;
import adoctorr.application.bean.proposal.ProposalMethodBean;
import adoctorr.application.bean.smell.SmellMethodBean;
import adoctorr.application.proposal.Proposer;
import com.intellij.openapi.project.Project;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.*;
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
    private ArrayList<SmellMethodBean> smellMethodList;
    private ProposalMethodBean proposalMethodBean;

    private ArrayList<SmellMethodBean> unresolvedSmellMethodList;

    private SmellDialog(Project project, ArrayList<SmellMethodBean> smellMethodList) {
        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 9;
        int y = (screenSize.height - getHeight()) / 16;
        setLocation(x, y);
        getRootPane().setDefaultButton(buttonApply);
        setTitle("aDoctor - Smells' list");

        areaActualCode.setPreferredSize(null);
        areaProposedCode.setPreferredSize(null);

        this.project = project;
        this.smellMethodList = smellMethodList;
        proposalMethodBean = null;

        unresolvedSmellMethodList = new ArrayList<>();
        for (SmellMethodBean smellMethodBean : smellMethodList) {
            if (!smellMethodBean.isResolved()) {
                unresolvedSmellMethodList.add(smellMethodBean);
            }
        }

        // The smells' list
        DefaultListModel<String> listSmellModel = (DefaultListModel<String>) listSmell.getModel();
        for (SmellMethodBean smellMethodBean : unresolvedSmellMethodList) {
            String methodName = smellMethodBean.getMethodBean().getName();
            int smellType = smellMethodBean.getSmellType();
            String smellName = SmellMethodBean.getSmellName(smellType);
            String htmlContent = "" +
                    "<html>" +
                    "<p style=\"font-size:10px\">" +
                    "<b>" + smellName + "</b>" +
                    "</p>" +
                    "<p style=\"font-size:9px\">" +
                    "" + methodName + "" +
                    "</p>" +
                    "</html>";
            listSmellModel.addElement(htmlContent);
        }

        listSmell.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //This if statement prevents multiple fires
                if (!e.getValueIsAdjusting()) {
                    onUpdateSmellMethodDetails();
                }
            }
        });

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

        // Select the first smell of the list
        listSmell.setSelectedIndex(0);
    }

    public static void show(Project project, ArrayList<SmellMethodBean> smellMethodList) {
        SmellDialog dialog = new SmellDialog(project, smellMethodList);

        dialog.pack();
        dialog.setVisible(true);
    }

    private void onUpdateSmellMethodDetails() {
        int selectedIndex = listSmell.getSelectedIndex();
        SmellMethodBean smellMethodBean = unresolvedSmellMethodList.get(selectedIndex);

        // Compute the proposal of the selected smell
        Proposer proposer = new Proposer();
        try {
            proposalMethodBean = proposer.computeProposal(smellMethodBean);

            String className = smellMethodBean.getMethodBean().getBelongingClass().getName();
            String packageName = smellMethodBean.getMethodBean().getBelongingClass().getBelongingPackage();
            String classFullName = packageName + "." + className;
            labelSmellName.setText(SmellMethodBean.getSmellName(smellMethodBean.getSmellType()));
            labelClassName.setText(classFullName);

            // Smell Description
            int smellType = smellMethodBean.getSmellType();
            switch (smellType) {
                case SmellMethodBean.DURABLE_WAKELOCK: {
                    labelIcon.setToolTipText(DURABLE_WAKELOCK_DESCRIPTION);
                    break;
                }
                case SmellMethodBean.EARLY_RESOURCE_BINDING: {
                    labelIcon.setToolTipText(EARLY_RESOURCE_BINDING_DESCRIPTION);
                    break;
                }
                default:
                    break;
            }

            // Actual Code area
            String actualCode = smellMethodBean.getMethodBean().getTextContent();
            areaActualCode.setText(actualCode);
            areaActualCode.setCaretPosition(0);
            Highlighter actualHighlighter = areaActualCode.getHighlighter();
            actualHighlighter.removeAllHighlights();
            ArrayList<String> actualCodeToHighlightList = proposalMethodBean.getActualCodeToHighlightList();
            if (actualCodeToHighlightList != null && actualCodeToHighlightList.size() > 0) {
                for (String actualCodeToHighlight : actualCodeToHighlightList) {
                    int highlightIndex = actualCode.indexOf(actualCodeToHighlight);
                    actualHighlighter.addHighlight(highlightIndex, highlightIndex + actualCodeToHighlight.length(), DefaultHighlighter.DefaultPainter);
                }
            }

            // Proposed Code Area
            String proposedCode = "";
            switch (smellType) {
                case SmellMethodBean.DURABLE_WAKELOCK: {
                    DurableWakelockProposalMethodBean durableWakelockProposalMethodBean = (DurableWakelockProposalMethodBean) proposalMethodBean;
                    MethodDeclaration proposedMethodDeclaration = durableWakelockProposalMethodBean.getProposedMethodDeclaration();
                    proposedCode = proposedMethodDeclaration.toString();
                    break;
                }
                case SmellMethodBean.EARLY_RESOURCE_BINDING: {
                    EarlyResourceBindingProposalMethodBean earlyResourceBindingProposalMethodBean = (EarlyResourceBindingProposalMethodBean) proposalMethodBean;
                    MethodDeclaration proposedOnCreate = earlyResourceBindingProposalMethodBean.getProposedOnCreate();
                    MethodDeclaration proposedOnResume = earlyResourceBindingProposalMethodBean.getProposedOnResume();
                    proposedCode = proposedOnCreate.toString() + "\n" + proposedOnResume.toString();
                    break;
                }
                default:
                    break;
            }
            areaProposedCode.setText(proposedCode);
            areaProposedCode.setCaretPosition(0);
            Highlighter proposedHighlighter = areaProposedCode.getHighlighter();
            proposedHighlighter.removeAllHighlights();
            ArrayList<String> proposedCodeToHighlightList = proposalMethodBean.getProposedCodeToHighlightList();
            if (proposedCodeToHighlightList != null && proposedCodeToHighlightList.size() > 0) {
                for (String proposedCodeToHighlight : proposedCodeToHighlightList) {
                    int highlightIndex = proposedCode.indexOf(proposedCodeToHighlight);
                    proposedHighlighter.addHighlight(highlightIndex, highlightIndex + proposedCodeToHighlight.length(), DefaultHighlighter.DefaultPainter);
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (BadLocationException e2) {
            // When the index of the string to highlight is wrong
            e2.printStackTrace();
        }
    }

    private void onApplyRefactoring() {
        dispose();
        RefactoringDialog.show(proposalMethodBean, project, smellMethodList);
    }

    private void onQuit() {
        dispose();
    }
}
