package adoctor.presentation.dialog;

import javax.swing.*;

abstract class AbstractDialog extends JDialog {

    void init(JPanel contentPane, String title, JButton defaultButton) {
        setModal(true);
        setContentPane(contentPane);
        setTitle(title);
        getRootPane().setDefaultButton(defaultButton);
    }

    void showInCenter() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

}
