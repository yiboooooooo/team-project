package stakemate.interface_adapter.view_signup;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * ViewModel for the Signup View.
 */
public class SignupViewModel {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private SignupState state = new SignupState();

    /**
     * Gets the current state.
     * 
     * @return the signup state.
     */
    public SignupState getState() {
        return state;
    }

    /**
     * Sets the current state and fires a property change.
     * 
     * @param state the new signup state.
     */
    public void setState(SignupState state) {
        this.state = state;
        support.firePropertyChange("state", null, this.state);
    }

    /**
     * Adds a property change listener.
     * 
     * @param listener the listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
