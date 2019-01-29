package adoctor.presentation.dialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FailureDialog extends AbstractDialog {
    public static final String TITLE = "aDoctor - Failure";

    private FailureCallback failureCallback;

    private JPanel contentPane;
    private JButton buttonBack;
    private JButton buttonQuit;

    public static void show(FailureCallback failureCallback) {
        FailureDialog failureDialog = new FailureDialog(failureCallback);

        failureDialog.showInCenter();
    }

    private FailureDialog(FailureCallback failureCallback) {
        init(failureCallback);
    }

    private void init(FailureCallback failureCallback) {
        super.init(contentPane, TITLE, buttonBack);

        this.failureCallback = failureCallback;

        buttonBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBack();
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

    private void onBack() {
        failureCallback.failureBack(this);
    }

    private void onQuit() {
        failureCallback.failureQuit(this);
    }

    interface FailureCallback {
        void failureBack(FailureDialog failureDialog);

        void failureQuit(FailureDialog failureDialog);
    }
}
