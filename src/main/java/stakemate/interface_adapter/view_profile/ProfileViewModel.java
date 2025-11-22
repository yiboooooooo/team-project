package stakemate.interface_adapter.view_profile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ProfileViewModel {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private ProfileState state = new ProfileState();

    public ProfileState getState() {
        return state;
    }

    public void setState(final ProfileState state) {
        this.state = state;
    }

    public void firePropertyChanged() {
        support.firePropertyChange("state", null, this.state);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
