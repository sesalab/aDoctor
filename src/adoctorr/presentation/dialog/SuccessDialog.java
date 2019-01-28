package adoctorr.presentation.dialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SuccessDialog extends AbstractDialog {
    public static final String TITLE = "aDoctor - Success";

    private SuccessCallback successCallback;

    private JPanel contentPane;
    private JButton buttonAnalyze;
    private JButton buttonQuit;

    public static void show(SuccessCallback successCallback) {
        SuccessDialog successDialog = new SuccessDialog(successCallback);

        successDialog.showInCenter();
    }

    private SuccessDialog(SuccessCallback successCallback) {
        init(successCallback);
    }

    private void init(SuccessCallback successCallback) {
        super.init(contentPane, TITLE, buttonAnalyze);

        this.successCallback = successCallback;

        buttonAnalyze.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStart();
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

    private void onStart() {
        successCallback.successAnalyze(this);
    }

    private void onQuit() {
        successCallback.successQuit(this);
    }

    interface SuccessCallback {
        void successAnalyze(SuccessDialog successDialog);

        void successQuit(SuccessDialog successDialog);
    }
}
