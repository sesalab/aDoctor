package adoctorr.presentation.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AboutDialog extends JDialog {
    public static final String TITLE = "aDoctor - About";
    public static final String ABOUT = "" +
            "<html> " +
            "<div style=\"text-align:center\">" +
            "This version of aDoctor is a code smell identification and refactoring plugin developed<br>" +
            "by Emanuele Iannone at Università degli Studi di Salerno.<br>" +
            "Please, feel free to report any bugs or suggestions at emaiannone@hotmail.it<br>" +
            "All icons used in this plugin are made by " +
            "<a href=\"https://www.flaticon.com/authors/good-ware\" title=\"Good Ware\">Good Ware</a> " +
            "from <a href=\"https://www.flaticon.com/\" title=\"Flaticon\">www.flaticon.com</a> is " +
            "licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" " +
            "title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a>" +
            "</div>" +
            "</html>";

    private AboutCallback aboutCallback;

    private JPanel contentPane;
    private JButton buttonBack;
    private JLabel labelAbout;

    public static void show(AboutCallback aboutCallback) {
        AboutDialog aboutDialog = new AboutDialog(aboutCallback);

        aboutDialog.pack();
        aboutDialog.setVisible(true);
    }

    private AboutDialog(AboutCallback aboutCallback) {
        this.aboutCallback = aboutCallback;

        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 4;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        setTitle(TITLE);
        getRootPane().setDefaultButton(buttonBack);

        labelAbout.setText(ABOUT);

        buttonBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBack();
            }
        });

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
