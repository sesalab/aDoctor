package adoctor.presentation.dialog;

import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JCheckBox checkLT;
    private JCheckBox checkMIM;

    private JButton buttonRun;
    private JButton buttonAbout;
    private JTextField fieldPackage;

    private List<JCheckBox> checkBoxes;

    private StartDialog(StartCallback startCallback, Project project, List<Boolean> selectedSmells) {
        this.startCallback = startCallback;
        this.project = project;
        init();

        // List of all check boxes
        checkBoxes = new ArrayList<>();
        checkBoxes.add(checkDW);
        checkBoxes.add(checkERB);
        checkBoxes.add(checkIDS);
        checkBoxes.add(checkIS);
        checkBoxes.add(checkLT);
        checkBoxes.add(checkMIM);
        // There is a pre-saved selections list: initialize check boxes
        if (selectedSmells != null) {
            Iterator<Boolean> selectionIter = selectedSmells.iterator();
            Iterator<JCheckBox> checkBoxIter = checkBoxes.iterator();
            while (selectionIter.hasNext() && checkBoxIter.hasNext()) {
                checkBoxIter.next().setSelected(selectionIter.next());
            }
            // adjust the select all check box
            boolean andResult = selectedSmells.stream().reduce((a, b) -> a && b).orElse(false);
            checkAll.setSelected(andResult);
        } else { // pre-saved selections list does not exists: check all as default
            checkAll.setSelected(false);
            checkAll.doClick();
        }
    }

    public static void show(StartCallback startCallback, Project project, List<Boolean> selectedSmells) {
        StartDialog startDialog = new StartDialog(startCallback, project, selectedSmells);
        startDialog.showInCenter();
    }

    private void init() {
        super.init(contentPane, TITLE, buttonRun);

        checkAll.addActionListener(e -> onSelectAll());
        checkDW.addActionListener(e -> onCheck());
        checkERB.addActionListener(e -> onCheck());
        checkIDS.addActionListener(e -> onCheck());
        checkIS.addActionListener(e -> onCheck());
        checkLT.addActionListener(e -> onCheck());
        checkMIM.addActionListener(e -> onCheck());
        buttonRun.addActionListener(e -> onRun());
        buttonAbout.addActionListener(e -> onAbout());
        fieldPackage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                onSelectModule();
            }
        });

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
        checkAll.setSelected(andResult);
    }

    private void onSelectModule() {
        PackageChooserDialog pcd = new PackageChooserDialog("Select target package", project);
        pcd.show();
        if (pcd.getSelectedPackage() != null) {
            fieldPackage.setText(pcd.getSelectedPackage().getQualifiedName());
        }
    }

    private void onRun() {
        // Nothing is selected: "select at least one" pop-up
        List<Boolean> selections = checkBoxes.stream().map(AbstractButton::isSelected).collect(Collectors.toList());
        boolean orResult = selections.stream().reduce((a, b) -> a || b).orElse(true);
        if (orResult) {
            startCallback.runAnalysis(this, selections, fieldPackage.getText());
        } else {
            JOptionPane.showMessageDialog(this, "Select at least one smell!", "Error", JOptionPane.ERROR_MESSAGE, null);
        }
    }

    private void onAbout() {
        startCallback.startSettings(this);
    }

    private void onQuit() {
        startCallback.startQuit(this);
    }

    public interface StartCallback {
        void runAnalysis(StartDialog startDialog, List<Boolean> selectedSmells, String targetPackage);

        void startSettings(StartDialog startDialog);

        void startQuit(StartDialog startDialog);
    }
}
