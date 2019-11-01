package adoctor.presentation.dialog;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AboutDialog extends AbstractDialog {
    private static final String TITLE = "aDoctor - About";

    private AboutCallback aboutCallback;
    private JPanel contentPane;
    private JButton buttonBack;
    private JLabel labelAbout;

    public static void show(AboutCallback aboutCallback) {
        AboutDialog aboutDialog = new AboutDialog(aboutCallback);
        aboutDialog.showInCenter();
    }

    private AboutDialog(AboutCallback aboutCallback) {
        this.aboutCallback = aboutCallback;
        init();
    }

    private void init() {
        super.init(contentPane, TITLE, buttonBack);

        buttonBack.addActionListener(e -> onBack());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onBack();
            }
        });
    }

    private void onBack() {
        aboutCallback.aboutBack(this);
    }

    interface AboutCallback {
        void aboutBack(AboutDialog aboutDialog);
    }
}
