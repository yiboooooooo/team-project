package stakemate.interface_adapter.view_login;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * ViewModel for the Login View.
 */
public class LoginViewModel {
    public static final String VIEW_NAME = "login";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private LoginState state = new LoginState();

    /**
     * Gets the current state.
     * 
     * @return the login state.
     */
    public LoginState getState() {
        return state;
    }

    /**
     * Sets the current state and fires a property change.
     * 
     * @param state the new login state.
     */
    public void setState(final LoginState state) {
        this.state = state;
        support.firePropertyChange("state", null, this.state);
    }

    /**
     * Adds a property change listener.
     * 
     * @param l the listener to add.
     */
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }
}
