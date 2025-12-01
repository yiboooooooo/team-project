package stakemate.use_case.view_profile;

/**
 * Output Boundary for the View Profile Use Case.
 */
public interface ViewProfileOutputBoundary {
    /**
     * Presents the profile data.
     * 
     * @param outputData the output data.
     */
    void presentProfile(ViewProfileOutputData outputData);

    /**
     * Presents an error message.
     * 
     * @param error the error message.
     */
    void presentError(String error);
}
