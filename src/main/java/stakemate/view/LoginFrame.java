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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import stakemate.interface_adapter.view_login.LoginController;
import stakemate.interface_adapter.view_login.LoginState;
import stakemate.interface_adapter.view_login.LoginViewModel;

/**
 * Frame for the Login View.
 */
public class LoginFrame extends JFrame implements PropertyChangeListener {

    private final MarketsFrame marketsFrame;
    // Buttons as FIELDS so we can access them in hookEvents()
    private final JButton signupButton = new JButton("Sign Up");
    private final JButton loginButton = new JButton("Login");
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JLabel errorLabel = new JLabel(" ");
    private LoginController controller;
    private SignupFrame signupFrame;
    private LoginViewModel viewModel;

    /**
     * Constructs a LoginFrame.
     * 
     * @param marketsFrame the markets frame to show after login.
     */
    public LoginFrame(final MarketsFrame marketsFrame) {
        super("StakeMate - Login");
        this.marketsFrame = marketsFrame;
        initUi();
    }

    /**
     * Sets the signup frame.
     * 
     * @param signupFrame the signup frame.
     */
    public void setSignupFrame(final SignupFrame signupFrame) {
        this.signupFrame = signupFrame;
    }

    /**
     * Sets the login controller.
     * 
     * @param controller the login controller.
     */
    public void setController(final LoginController controller) {
        this.controller = controller;
        hookEvents();
    }

    /**
     * Sets the login view model.
     * 
     * @param viewModel the login view model.
     */
    public void setViewModel(final LoginViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addPropertyChangeListener(this);
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 220);
        setLocationRelativeTo(null);

        final JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        final JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(signupButton);
        buttons.add(loginButton);

        errorLabel.setForeground(Color.RED);

        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        root.add(errorLabel, BorderLayout.NORTH);

        setContentPane(root);
    }

    private void hookEvents() {
        // Login button
        loginButton.addActionListener(e -> {
            if (controller != null) {
                final String username = usernameField.getText().trim();
                final String password = new String(passwordField.getPassword());
                controller.execute(username, password);
            }
        });
        signupButton.addActionListener(e -> {
            if (signupFrame != null) {
                this.setVisible(false);
                signupFrame.setVisible(true);
            }
        });
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final LoginState state = (LoginState) evt.getNewValue();
        if (state.getError() != null) {
            errorLabel.setText(state.getError());
        } else if (state.getUsername() != null && !state.getUsername().isEmpty()) {
            // Hide login window and show the markets window
            this.setVisible(false);
            this.dispose();

            marketsFrame.setLoggedInUser(state.getUsername());
            marketsFrame.setVisible(true);
        }
    }
}
