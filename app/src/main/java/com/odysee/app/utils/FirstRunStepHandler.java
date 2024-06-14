package com.odysee.app.utils;

public interface FirstRunStepHandler {
    void onCompleted(int completedStep);
    void onRequestInProgress(boolean showProgress);
    void onRequestCompleted(int step);
    void onSignInModeChanged(boolean signInMode);
    void onChannelNameUpdated(String channelName);
    void onYouTubeSyncOptInCheckChanged(boolean checked);
    void onStarted();
    void onSkipped();
}
