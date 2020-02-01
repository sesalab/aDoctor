package adoctor.presentation.dialog;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AbortedDialog extends AbstractDialog {
    private static final String TITLE = "aDoctor - Aborted";

    private AbortedCallback abortedCallback;

    private JPanel contentPane;
    private JButton buttonAnalyze;
    private JButton buttonBack;

    public static void show(AbortedCallback abortedCallback) {
        AbortedDialog abortedDialog = new AbortedDialog(abortedCallback);
        abortedDialog.showInCenter();
    }

    private AbortedDialog(AbortedCallback abortedCallback) {
        this.abortedCallback = abortedCallback;
        init();
    }

    private void init() {
        super.init(contentPane, TITLE, buttonAnalyze);

        buttonAnalyze.addActionListener(e -> onRestart());
        buttonBack.addActionListener(e -> onBack());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });
    }

    private void onRestart() {
        abortedCallback.abortedRestart(this);
    }

    private void onBack() {
        abortedCallback.abortedBack(this);
    }

    private void onQuit() {
        abortedCallback.abortedQuit(this);
    }

    public interface AbortedCallback {
        void abortedRestart(AbortedDialog abortedDialog);

        void abortedBack(AbortedDialog abortedDialog);

        void abortedQuit(AbortedDialog abortedDialog);
    }
}
