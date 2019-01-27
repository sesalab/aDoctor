package adoctorr.presentation.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StartDialog extends JDialog {
    public static final String TITLE = "aDoctor";

    private StartCallback startCallback;

    private JPanel contentPane;
    private JButton buttonStart;
    private JButton buttonQuit;
    private JButton buttonAbout;

    public static void show(StartCallback startCallback) {
        StartDialog startDialog = new StartDialog(startCallback);

        startDialog.pack();
        startDialog.setVisible(true);
    }

    private StartDialog(StartCallback startCallback) {
        this.startCallback = startCallback;

        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 3;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        setTitle(TITLE);
        getRootPane().setDefaultButton(buttonStart); //Pressing Enter means clicking buttonStart

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

    private void onStart() {
        startCallback.startAnalysis(this);
    }

    private void onQuit() {
        startCallback.startQuit(this);
    }


    private void onAbout() {
        startCallback.startAbout(this);
    }

    interface StartCallback {
        void startAnalysis(StartDialog startDialog);

        void startAbout(StartDialog startDialog);

        void startQuit(StartDialog startDialog);
    }
}
