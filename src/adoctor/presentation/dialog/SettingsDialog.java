package adoctor.presentation.dialog;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SettingsDialog extends AbstractDialog {
    private static final String TITLE = "aDoctor - Settings";

    private SettingsCallback settingsCallback;
    private JPanel contentPane;
    private JCheckBox checkStats;
    private JButton buttonSave;
    private JButton buttonBack;
    private JLabel labelAbout;

    private SettingsDialog(SettingsCallback settingsCallback, boolean savedStats) {
        this.settingsCallback = settingsCallback;
        init(savedStats);
    }

    public static void show(SettingsCallback settingsCallback, boolean savedStats) {
        SettingsDialog settingsDialog = new SettingsDialog(settingsCallback, savedStats);
        settingsDialog.showInCenter();
    }

    private void init(boolean savedStats) {
        super.init(contentPane, TITLE, buttonSave);

        buttonBack.addActionListener(e -> onBack());
        buttonSave.addActionListener(e -> onSave());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onBack();
            }
        });
        checkStats.setSelected(savedStats);
    }

    private void onBack() {
        settingsCallback.settingsBack(this);
    }

    private void onSave() {
        settingsCallback.settingsSave(this, checkStats.isSelected());
    }

    public interface SettingsCallback {
        void settingsBack(SettingsDialog settingsDialog);

        void settingsSave(SettingsDialog settingsDialog, boolean statsChecked);
    }
}
