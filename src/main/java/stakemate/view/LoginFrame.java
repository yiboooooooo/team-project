package stakemate.view;

import stakemate.interface_adapter.controllers.LoginController;
import stakemate.interface_adapter.view_login.LoginView;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame implements LoginView {

    private LoginController controller; // injected later
    private final MarketsFrame marketsFrame; // we'll show this after login

    private SignupFrame signupFrame;

    // Buttons as FIELDS so we can access them in hookEvents()
    private final JButton signupButton = new JButton("Sign Up");
    private final JButton loginButton = new JButton("Login");

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JLabel errorLabel = new JLabel(" ");

    public LoginFrame(MarketsFrame marketsFrame) {
        super("StakeMate - Login");
        this.marketsFrame = marketsFrame;
        initUi();
    }

    public void setSignupFrame(SignupFrame signupFrame) {
        this.signupFrame = signupFrame;
    }

    public void setController(LoginController controller) {
        this.controller = controller;
        hookEvents();
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 220);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // Add BOTH buttons to the panel
        buttons.add(signupButton);
        buttons.add(loginButton);

        errorLabel.setForeground(Color.RED);

        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        root.add(errorLabel, BorderLayout.NORTH);

        setContentPane(root);

        // We'll attach the ActionListeners in hookEvents() once the controller is set
    }

    private void hookEvents() {
        // Login button
        loginButton.addActionListener(e -> {
            if (controller != null) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                controller.execute(username, password);
            }
        });

        // Sign Up button
        signupButton.addActionListener(e -> {
            if (signupFrame != null) {
                this.setVisible(false); // hide login window
                signupFrame.setVisible(true); // open signup window
            }
        });
    }

    // ---- LoginView implementation ----

    @Override
    public void showError(String message) {
        errorLabel.setText(message != null ? message : " ");
    }

    @Override
    public void onLoginSuccess(String username) {
        // Hide login window and show the markets window
        this.setVisible(false);
        this.dispose();

        marketsFrame.setLoggedInUser(username);
        marketsFrame.setVisible(true);
        // Optional: if you want, you can trigger an initial refresh here
        // (assuming you can get a reference to the ViewMarketController)
    }
}
