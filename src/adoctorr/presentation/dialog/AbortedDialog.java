package adoctorr.presentation.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AbortedDialog extends JDialog {
    public static final String TITLE = "aDoctor - Aborted";

    private AbortedCallback abortedCallback;

    private JPanel contentPane;
    private JButton buttonAnalyze;
    private JButton buttonQuit;

    public static void show(AbortedCallback abortedCallback) {
        AbortedDialog abortedDialog = new AbortedDialog(abortedCallback);

        abortedDialog.pack();
        abortedDialog.setVisible(true);
    }

    private AbortedDialog(AbortedCallback abortedCallback) {
        this.abortedCallback = abortedCallback;

        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) * 2 / 5;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        getRootPane().setDefaultButton(buttonAnalyze);
        setTitle(TITLE);

        buttonAnalyze.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRestart();
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

    private void onRestart() {
        abortedCallback.abortedRestart(this);
    }

    private void onQuit() {
        abortedCallback.abortedQuit(this);
    }

    interface AbortedCallback {
        void abortedQuit(AbortedDialog abortedDialog);

        void abortedRestart(AbortedDialog abortedDialog);
    }
}
