package adoctor.presentation.dialog;

import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StartDialog extends AbstractDialog {
    public static final String TITLE = "aDoctor";

    private StartCallback startCallback;
    private Project project;

    private JPanel contentPane;
    private JCheckBox checkBoxDW;
    private JCheckBox checkBoxERB;
    private JButton buttonSelect;
    private JButton buttonStart;
    private JButton buttonQuit;
    private JButton buttonAbout;
    private JLabel labelPackage;

    public static void show(StartCallback startCallback, Project project) {
        StartDialog startDialog = new StartDialog(startCallback, project);

        startDialog.showInCenter();
    }

    private StartDialog(StartCallback startCallback, Project project) {
        init(startCallback, project);
    }

    private void init(StartCallback startCallback, Project project) {
        super.init(contentPane, TITLE, buttonStart);

        this.startCallback = startCallback;
        this.project = project;

        labelPackage.setText("");
        checkBoxDW.setSelected(true);
        checkBoxERB.setSelected(true);

        buttonSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSelectModule();
            }
        });

        buttonStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStart();
            }
        });

        buttonQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQuit();
            }
        });

        buttonAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAbout();
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

    private void onSelectModule() {
        PackageChooserDialog pcd = new PackageChooserDialog("Select target package", project);
        pcd.show();
        labelPackage.setText(pcd.getSelectedPackage().getQualifiedName());
    }

    private void onStart() {
        boolean[] selections = new boolean[2];
        selections[0] = checkBoxDW.isSelected();
        selections[1] = checkBoxERB.isSelected();
        if (!selections[0] && !selections[1]) {
            JOptionPane.showMessageDialog(this, "Select at least one smell!", "Error", JOptionPane.ERROR_MESSAGE, null);
        } else {
            startCallback.startAnalysis(this, selections, labelPackage.getText());
        }
    }

    private void onQuit() {
        startCallback.startQuit(this);
    }


    private void onAbout() {
        startCallback.startAbout(this);
    }

    interface StartCallback {
        void startAnalysis(StartDialog startDialog, boolean[] selections, String targetPackage);

        void startAbout(StartDialog startDialog);

        void startQuit(StartDialog startDialog);
    }
}
