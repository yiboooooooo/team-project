package stakemate.interface_adapter.view_profile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * ViewModel for the Profile View.
 */
public class ProfileViewModel {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private ProfileState state = new ProfileState();

    /**
     * Gets the current state.
     * 
     * @return the profile state.
     */
    public ProfileState getState() {
        return state;
    }

    /**
     * Sets the current state.
     * 
     * @param state the new profile state.
     */
    public void setState(final ProfileState state) {
        this.state = state;
    }

    /**
     * Fires a property change event for the state.
     */
    public void firePropertyChanged() {
        support.firePropertyChange("state", null, this.state);
    }

    /**
     * Adds a property change listener.
     * 
     * @param listener the listener to add.
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
