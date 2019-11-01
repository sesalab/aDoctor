package adoctor.presentation.dialog;

import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class StartDialog extends AbstractDialog {
    private static final String TITLE = "aDoctor";

    private StartCallback startCallback;
    private Project project;

    private JPanel contentPane;
    private JCheckBox checkAll;
    private JCheckBox checkDW;
    private JCheckBox checkERB;
    private JCheckBox checkIDS;
    private JCheckBox checkIS;
    private JCheckBox checkMIM;
    private JCheckBox checkLT;

    private JButton buttonSelect;
    private JButton buttonStart;
    private JButton buttonQuit;
    private JButton buttonAbout;
    private JLabel labelPackage;

    private List<JCheckBox> checkBoxes;

    private StartDialog(StartCallback startCallback, Project project, List<Boolean> selections) {
        this.startCallback = startCallback;
        this.project = project;

        init();

        // List of all check boxes
        checkBoxes = new ArrayList<>();
        checkBoxes.add(checkDW);
        checkBoxes.add(checkERB);
        checkBoxes.add(checkIDS);
        checkBoxes.add(checkIS);
        checkBoxes.add(checkMIM);
        checkBoxes.add(checkLT);
        // There is a pre-saved selections list: initialize check boxes
        if (selections != null) {
            Iterator<Boolean> selectionIter = selections.iterator();
            Iterator<JCheckBox> checkBoxIter = checkBoxes.iterator();
            while (selectionIter.hasNext() && checkBoxIter.hasNext()) {
                checkBoxIter.next().setSelected(selectionIter.next());
            }
            // adjust the select all check box
            boolean andResult = selections.stream().reduce((a, b) -> a && b).orElse(false);
            checkAll.setSelected(andResult);
        } else { // pre-saved selections list does not exists: check all as default
            checkAll.setSelected(false);
            checkAll.doClick();
        }
    }

    public static void show(StartCallback startCallback, Project project, List<Boolean> selections) {
        StartDialog startDialog = new StartDialog(startCallback, project, selections);
        startDialog.showInCenter();
    }

    private void init() {
        super.init(contentPane, TITLE, buttonStart);

        labelPackage.setText("");

        checkAll.addActionListener(e -> onSelectAll());
        checkDW.addActionListener(e -> onCheck());
        checkERB.addActionListener(e -> onCheck());
        checkIDS.addActionListener(e -> onCheck());
        checkIS.addActionListener(e -> onCheck());
        checkMIM.addActionListener(e -> onCheck());
        checkLT.addActionListener(e -> onCheck());
        buttonSelect.addActionListener(e -> onSelectModule());
        buttonStart.addActionListener(e -> onStart());
        buttonQuit.addActionListener(e -> onQuit());
        buttonAbout.addActionListener(e -> onAbout());

        // call onQuit() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });

        /*
        // call onQuit() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQuit();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        */
    }

    private void onSelectAll() {
        // Uncheck -> Check
        if (checkAll.isSelected()) {
            checkBoxes.forEach(jCheckBox -> jCheckBox.setSelected(true));
        } else { // Check -> Uncheck
            checkBoxes.forEach(jCheckBox -> jCheckBox.setSelected(false));
        }
    }

    private void onCheck() {
        // All are selected: check checkAll. Not all are selected: uncheck checkAll
        List<Boolean> selections = checkBoxes.stream().map(AbstractButton::isSelected).collect(Collectors.toList());
        boolean andResult = selections.stream().reduce((a, b) -> a && b).orElse(false);
        if (andResult) {
            checkAll.setSelected(true);
        } else {
            checkAll.setSelected(false);
        }
    }

    // TODO High Change some graphics of the StartDialog: boost package selection
    private void onSelectModule() {
        PackageChooserDialog pcd = new PackageChooserDialog("Select target package", project);
        pcd.show();
        if (pcd.getSelectedPackage() != null) {
            labelPackage.setText(pcd.getSelectedPackage().getQualifiedName());
        }
    }

    private void onStart() {
        // Nothing is selected: "select at least one" pop-up
        List<Boolean> selections = checkBoxes.stream().map(AbstractButton::isSelected).collect(Collectors.toList());
        boolean orResult = selections.stream().reduce((a, b) -> a || b).orElse(true);
        if (orResult) {
            startCallback.startAnalysis(this, selections, labelPackage.getText());
        } else {
            JOptionPane.showMessageDialog(this, "Select at least one smell!", "Error", JOptionPane.ERROR_MESSAGE, null);
        }
    }

    private void onAbout() {
        startCallback.startAbout(this);
    }

    private void onQuit() {
        startCallback.startQuit(this);
    }

    interface StartCallback {
        void startAnalysis(StartDialog startDialog, List<Boolean> selections, String targetPackage);

        void startAbout(StartDialog startDialog);

        void startQuit(StartDialog startDialog);
    }
}
