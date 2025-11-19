package stakemate.use_case.view_profile;

public interface ViewProfileOutputBoundary {
    void presentProfile(ViewProfileOutputData outputData);

    void presentError(String error);
}
