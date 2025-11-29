package stakemate.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import stakemate.interface_adapter.view_signup.SignupController;
import stakemate.interface_adapter.view_signup.SignupState;
import stakemate.interface_adapter.view_signup.SignupViewModel;

/**
 * Frame for the Signup View.
 */
public class SignupFrame extends JFrame implements PropertyChangeListener {

    private final JFrame loginFrame;
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JPasswordField confirmField = new JPasswordField(15);
    private final JLabel errorLabel = new JLabel(" ");
    private final JButton signupButton = new JButton("Sign Up");
    private final JButton cancelButton = new JButton("Cancel");
    private SignupController controller;
    private SignupViewModel viewModel;

    /**
     * Constructs a SignupFrame.
     * 
     * @param loginFrame the login frame to return to.
     */
    public SignupFrame(final JFrame loginFrame) {
        super("StakeMate - Sign Up");
        this.loginFrame = loginFrame;
        initUi();
        hookCancel();
    }

    /**
     * Sets the signup controller.
     * 
     * @param controller the signup controller.
     */
    public void setController(final SignupController controller) {
        this.controller = controller;
        hookSignup();
    }

    /**
     * Sets the signup view model.
     * 
     * @param viewModel the signup view model.
     */
    public void setViewModel(final SignupViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addPropertyChangeListener(this);
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
                errorLabel.setText("Passwords do not match.");
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

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final SignupState state = (SignupState) evt.getNewValue();
        if (state.getError() != null) {
            errorLabel.setText(state.getError());
        } else if (state.getUsername() != null && !state.getUsername().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Account created for " + state.getUsername() + ". Please log in.",
                    "Signup Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            this.setVisible(false);
            this.dispose();

            if (loginFrame != null) {
                loginFrame.setVisible(true);
            }
        }
    }
}
