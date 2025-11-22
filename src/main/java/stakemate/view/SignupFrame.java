package stakemate.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import stakemate.interface_adapter.controllers.SignupController;
import stakemate.interface_adapter.view_signup.SignupView;

public class SignupFrame extends JFrame implements SignupView {

    private final JFrame loginFrame;
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JPasswordField confirmField = new JPasswordField(15);
    private final JLabel errorLabel = new JLabel(" ");
    private final JButton signupButton = new JButton("Sign Up");
    private final JButton cancelButton = new JButton("Cancel");
    private SignupController controller;

    public SignupFrame(final JFrame loginFrame) {
        super("StakeMate - Sign Up");
        this.loginFrame = loginFrame;
        initUi();
        hookCancel();
    }

    public void setController(final SignupController controller) {
        this.controller = controller;
        hookSignup();
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        final JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        final JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        form.add(new JLabel("Confirm Password:"));
        form.add(confirmField);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(signupButton);

        errorLabel.setForeground(Color.RED);

        root.add(errorLabel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void hookSignup() {
        signupButton.addActionListener(e -> {
            if (controller == null) {
                return;
            }

            final String username = usernameField.getText().trim();
            final String password = new String(passwordField.getPassword());
            final String confirm = new String(confirmField.getPassword());

            if (!password.equals(confirm)) {
                showError("Passwords do not match.");
                return;
            }

            controller.execute(username, password);
        });
    }

    private void hookCancel() {
        cancelButton.addActionListener(e -> {
            // Just close signup and show login again
            this.setVisible(false);
            this.dispose();

            if (loginFrame != null) {
                loginFrame.setVisible(true);
            }
        });
    }

    // ===== SignupView implementation =====

    @Override
    public void showError(final String message) {
        errorLabel.setText(message != null ? message : " ");
    }

    @Override
    public void onSignupSuccess(final String username) {
        JOptionPane.showMessageDialog(
            this,
            "Account created for " + username + ". Please log in.",
            "Signup Successful",
            JOptionPane.INFORMATION_MESSAGE
        );
        this.setVisible(false);
        this.dispose();

        if (loginFrame != null) {
            loginFrame.setVisible(true);
        }
    }
}
