package com.odysee.app.ui.firstrun;

import static com.odysee.app.utils.Lbryio.TAG;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.odysee.app.FirstRunActivity;
import com.odysee.app.MainActivity;
import com.odysee.app.OdyseeApp;
import com.odysee.app.R;
import com.odysee.app.SignInActivity;
import com.odysee.app.callable.UserExistsWithPassword;
import com.odysee.app.exceptions.LbryioRequestException;
import com.odysee.app.exceptions.LbryioResponseException;
import com.odysee.app.model.WalletSync;
import com.odysee.app.model.lbryinc.User;
import com.odysee.app.tasks.verification.CheckUserEmailVerifiedTask;
import com.odysee.app.tasks.wallet.DefaultSyncTaskHandler;
import com.odysee.app.tasks.wallet.SyncApplyTask;
import com.odysee.app.tasks.wallet.SyncGetTask;
import com.odysee.app.tasks.wallet.SyncSetTask;
import com.odysee.app.utils.FirstRunStepHandler;
import com.odysee.app.utils.Helper;
import com.odysee.app.utils.Lbry;
import com.odysee.app.utils.LbryAnalytics;
import com.odysee.app.utils.Lbryio;
import com.odysee.app.utils.Utils;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.Setter;

public class SignInFragment extends Fragment {
    public final static String ARG_ACCOUNT_TYPE = "com.odysee";
    public final static String ARG_AUTH_TYPE = "auth_token_type";

    private boolean walletSyncStarted;
    private WalletSync currentWalletSync;
    private ScheduledFuture<?> emailVerifyFuture = null;

    private View layoutWalletSyncContainer;
    private View layoutWalletSyncInputArea;
    private ProgressBar walletSyncProgress;
    private MaterialButton walletSyncDoneButton;
    private TextView textWalletSyncLoading;
    private TextInputEditText inputWalletSyncPassword;

    private TextInputLayout layoutPassword;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private ProgressBar activityProgress;
    private View layoutCollect;
    private View layoutVerify;
    private TextView textTitle;
    private TextView textAgreeToTerms;
    private TextView textUseMagicLink;

    private String currentEmail;
    private ScheduledExecutorService emailVerifyCheckScheduler;
    private ExecutorService executor;

    private boolean requestInProgress;
    private MaterialButton buttonPrimary;
    private MaterialButton buttonSecondary;
    private boolean signInMode;
    private boolean emailSignInChecked;

    @Setter
    private FirstRunStepHandler firstRunStepHandler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_in, container, false);

        executor = ((OdyseeApp) getActivity().getApplication()).getExecutor();

        layoutCollect = root.findViewById(R.id.signin_form);
        textTitle = root.findViewById(R.id.signin_title);
        layoutVerify = root.findViewById(R.id.verification_email_verify_container);
        layoutPassword = root.findViewById(R.id.layout_signin_password);
        inputEmail = root.findViewById(R.id.verification_email_input);
        inputPassword = root.findViewById(R.id.signin_password);
        activityProgress = root.findViewById(R.id.signin_activity_progress);

        layoutWalletSyncContainer = root.findViewById(R.id.verification_wallet_sync_container);
        layoutWalletSyncInputArea = root.findViewById(R.id.verification_wallet_input_area);
        walletSyncProgress = root.findViewById(R.id.verification_wallet_loading_progress);
        walletSyncDoneButton = root.findViewById(R.id.verification_wallet_done_button);
        textWalletSyncLoading = root.findViewById(R.id.verification_wallet_loading_text);
        inputWalletSyncPassword = root.findViewById(R.id.verification_wallet_password_input);
        textAgreeToTerms = root.findViewById(R.id.agree_to_terms_note);
        textUseMagicLink = root.findViewById(R.id.use_magic_link_text);

        buttonPrimary = root.findViewById(R.id.button_primary);
        buttonSecondary = root.findViewById(R.id.button_secondary);

        buttonSecondary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInMode = !signInMode;
                textTitle.setText(signInMode ? R.string.log_in_odysee : R.string.join_odysee);
                textAgreeToTerms.setVisibility(signInMode ? View.GONE : View.VISIBLE);
                buttonPrimary.setText(signInMode ? R.string.continue_text : R.string.sign_up);
                buttonSecondary.setText(signInMode ? R.string.sign_up : R.string.sign_in);
                layoutPassword.setVisibility(signInMode ? View.GONE : View.VISIBLE);
                inputPassword.setText("");

                if (firstRunStepHandler != null) {
                    firstRunStepHandler.onSignInModeChanged(signInMode);
                }
            }
        });

        textUseMagicLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentEmail == null) {
                    showError(getString(R.string.no_current_email));
                    return;
                }

                beforeSignInTransition();
                handleUserSignInWithoutPassword(currentEmail);
            }
        });

        walletSyncDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = Helper.getValue(inputWalletSyncPassword.getText());
                if (Helper.isNullOrEmpty(password)) {
                    showError(getString(R.string.please_enter_your_password));
                    return;
                }

                Context context = getContext();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inputWalletSyncPassword.getWindowToken(), 0);

                Helper.setViewVisibility(layoutWalletSyncInputArea, View.GONE);
                runWalletSync(password);
            }
        });

        textAgreeToTerms.setMovementMethod(LinkMovementMethod.getInstance());

        buttonPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpOrSignIn();
            }
        });

        root.findViewById(R.id.verification_email_resend_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEmailVerificationFlow(currentEmail);
            }
        });

        View buttonEdit = root.findViewById(R.id.verification_email_edit_button);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editEmail();
            }
        });

        return root;
    }

    private boolean checkUserExistsWithPassword(String email) {
        Callable<Boolean> callable = new UserExistsWithPassword(getContext(), email);
        Future<Boolean> future = executor.submit(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "checkUserExistsWithPassword: ".concat(e.getLocalizedMessage()));
        }
        return false;
    }

    private void beforeSignInTransition() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //TransitionManager.beginDelayedTransition(findViewById(R.id.signin_buttons));
                View view = getView();
                if (view != null) {
                    view.findViewById(R.id.signin_buttons).setVisibility(View.INVISIBLE);
                    activityProgress.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void performSignIn(final String email, final String password) {
        if (requestInProgress) {
            return;
        }

        if (!emailSignInChecked) {
            requestInProgress = true;
            if (firstRunStepHandler != null) {
                firstRunStepHandler.onRequestInProgress(false);
            }
            beforeSignInTransition();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> options = new HashMap<>();
                    options.put("email", email);
                    try {
                        Object response = Lbryio.parseResponse(Lbryio.call(
                                "user", "exists", options, Helper.METHOD_POST, getContext()));

                        requestInProgress = false;
                        if (response instanceof JSONObject) {
                            JSONObject json = (JSONObject) response;
                            boolean hasPassword = Helper.getJSONBoolean("has_password", false, json);
                            if (!hasPassword) {
                                setCurrentEmail(email);
                                handleUserSignInWithoutPassword(email);
                                return;
                            }

                            // email exists, we can use sign in flow. Show password field
                            setCurrentEmail(email);
                            emailSignInChecked = true;
                            displaySignInControls();
                            displayMagicLink();
                        }
                    } catch (LbryioRequestException | LbryioResponseException ex) {
                        if (ex instanceof LbryioResponseException) {
                            LbryioResponseException rex = (LbryioResponseException) ex;
                            if (rex.getStatusCode() == 412) {
                                // old email verification flow
                                setCurrentEmail(email);
                                handleUserSignInWithoutPassword(email);

                                Bundle bundle = new Bundle();
                                bundle.putString("email", currentEmail);
                                LbryAnalytics.logEvent(LbryAnalytics.EVENT_EMAIL_ADDED, bundle);
                                LbryAnalytics.logEvent("");

                                requestInProgress = false;
                                return;
                            }
                        }

                        requestInProgress = false;
                        restoreControls(true);
                        if (firstRunStepHandler != null) {
                            firstRunStepHandler.onRequestCompleted(FirstRunActivity.FIRST_RUN_STEP_ACCOUNT);
                        }
                        showError(ex.getMessage());
                    }
                }
            });

            return;
        }

        if (Helper.isNullOrEmpty(password)) {
            showError(getString(R.string.please_enter_signin_password));
            return;
        }

        beforeSignInTransition();
        requestInProgress = true;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> options = new HashMap<>();
                options.put("email", email);
                options.put("password", password);
                try {
                    Object response = Lbryio.parseResponse(Lbryio.call("user", "signin", options, Helper.METHOD_POST, getContext()));
                    requestInProgress = false;
                    if (response instanceof JSONObject) {
                        Type type = new TypeToken<User>(){}.getType();
                        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                        User user = gson.fromJson(response.toString(), type);
                        Lbryio.currentUser = user;

                        addOdyseeAccountExplicitly(user.getPrimaryEmail());

                        finishWithWalletSync();
                        return;
                    }
                } catch (LbryioRequestException | LbryioResponseException ex) {
                    if (ex instanceof LbryioResponseException) {
                        if (((LbryioResponseException) ex).getStatusCode() == 409) {
                            handleEmailVerificationFlow(email);
                            return;
                        }
                    }

                    showError(ex.getMessage());
                    requestInProgress = false;
                    restoreControls(true);
                    if (firstRunStepHandler != null) {
                        firstRunStepHandler.onRequestCompleted(FirstRunActivity.FIRST_RUN_STEP_ACCOUNT);
                    }
                    return;
                }

                requestInProgress = false;
                restoreControls(true);
                if (firstRunStepHandler != null) {
                    firstRunStepHandler.onRequestCompleted(FirstRunActivity.FIRST_RUN_STEP_ACCOUNT);
                }
                showError(getString(R.string.unknown_error_occurred));
            }
        });
    }

    private void disableVerificationControls() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                View view = getView();
                if (view != null) {
                    view.findViewById(R.id.verification_email_resend_button).setEnabled(false);
                    view.findViewById(R.id.verification_email_edit_button).setEnabled(false);
                }
            }
        });
    }

    private void enableVerificationControls() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                View view = getView();
                if (view != null) {
                    view.findViewById(R.id.verification_email_resend_button).setEnabled(true);
                    view.findViewById(R.id.verification_email_edit_button).setEnabled(true);
                }
            }
        });
    }

    private void handleEmailVerificationFlow(final String email) {
        requestInProgress = true;
        disableVerificationControls();
        if (firstRunStepHandler != null) {
            firstRunStepHandler.onRequestInProgress(false);
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> options = new HashMap<>();
                options.put("email", email);
                options.put("only_if_expired", "true");
                try {
                    Object response = Lbryio.parseResponse(Lbryio.call("user_email", "resend_token", options, Helper.METHOD_POST, getContext()));
                    requestInProgress = false;
                    enableVerificationControls();
                    waitForVerification();
                } catch (LbryioRequestException | LbryioResponseException ex) {
                    requestInProgress = false;
                    enableVerificationControls();
                    showError(ex.getMessage());
                }
            }
        });
    }

    private void displaySignInControls() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                restoreControls(true);
                layoutPassword.setVisibility(View.VISIBLE);
                buttonPrimary.setText(R.string.sign_in);
            }
        });
    }

    private void displayMagicLink() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                textUseMagicLink.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleUserSignInWithoutPassword(String email) {
        requestInProgress = true;
        if (firstRunStepHandler != null) {
            firstRunStepHandler.onRequestInProgress(true);
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> options = new HashMap<>();
                options.put("email", email);
                options.put("send_verification_email", "true");
                try {
                    Object response = Lbryio.parseResponse(Lbryio.call("user_email", "new", options, Helper.METHOD_POST,getContext()));
                    requestInProgress = false;
                    waitForVerification();
                } catch (LbryioRequestException | LbryioResponseException ex) {
                    requestInProgress = false;
                    if (ex instanceof LbryioResponseException) {
                        if (((LbryioResponseException) ex).getStatusCode() == 409) {
                            handleEmailVerificationFlow(email);
                            return;
                        }
                    }
                    restoreControls(true);
                    if (firstRunStepHandler != null) {
                        firstRunStepHandler.onRequestCompleted(FirstRunActivity.FIRST_RUN_STEP_ACCOUNT);
                    }
                    showError(ex.getMessage());
                }
            }
        });
    }

    private void setCurrentEmail(String email) {
        this.currentEmail = email;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                View view = getView();
                ((TextView) view.findViewById(R.id.verification_email_added_address)).setText(currentEmail);
            }
        });
    }

    private void signUpOrSignIn() {
        currentEmail = Helper.getValue(inputEmail.getText());
        String password = Helper.getValue(inputPassword.getText());
        if (Helper.isNullOrEmpty(currentEmail) || !currentEmail.contains("@")) {
            View view = getView(); //root.findViewById(R.id.verification_email_collect_container);
            if (view != null) {
                Snackbar.make(view, R.string.provide_valid_email, Snackbar.LENGTH_LONG).
                        setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
            }
            return;
        }

        Context context = getContext();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputEmail.getWindowToken(), 0);

        if (!signInMode) {
            // sign up
            handleUserSignUp(currentEmail, password);
            return;
        }

        performSignIn(currentEmail, password);
    }

    private void updateControlsBeforeRequest() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                inputEmail.setEnabled(false);
                inputPassword.setEnabled(false);
                buttonPrimary.setEnabled(false);
                buttonSecondary.setEnabled(false);
                activityProgress.setVisibility(View.VISIBLE);

                View view = getView();
                if (view != null) {
                    view.findViewById(R.id.signin_buttons).setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void restoreControls(final boolean showClose) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                inputEmail.setEnabled(true);
                inputPassword.setEnabled(true);
                buttonPrimary.setEnabled(true);
                buttonSecondary.setEnabled(true);
                activityProgress.setVisibility(View.INVISIBLE);

                View view = getView();
                if (view != null) {
                    view.findViewById(R.id.signin_buttons).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void handleUserSignUp(final String email, final String password) {
        if (requestInProgress) {
            return;
        }
        if (Helper.isNullOrEmpty(email) || Helper.isNullOrEmpty(password)) {
            showError(getString(R.string.provide_valid_email_password));
            return;
        }

        updateControlsBeforeRequest();
        requestInProgress = true;

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> options = new HashMap<>();
                    options.put("email", email);
                    options.put("password", password);
                    Object response = Lbryio.parseResponse(Lbryio.call("user", "signup", options, Helper.METHOD_POST, getContext()));
                    if (response != null && "ok".equalsIgnoreCase(response.toString())) {
                        // log email added
                        Bundle bundle = new Bundle();
                        bundle.putString("email", currentEmail);
                        LbryAnalytics.logEvent(LbryAnalytics.EVENT_EMAIL_ADDED, bundle);

                        // wait for verification
                        waitForVerification();
                        restoreControls(false);
                        return;
                    }

                    requestInProgress = false;
                    restoreControls(false);
                    if (firstRunStepHandler != null) {
                        firstRunStepHandler.onRequestCompleted(FirstRunActivity.FIRST_RUN_STEP_ACCOUNT);
                    }
                    showError(getString(R.string.signup_error_occurred));
                } catch (LbryioRequestException | LbryioResponseException ex) {
                    requestInProgress = false;
                    restoreControls(true);
                    if (firstRunStepHandler != null) {
                        firstRunStepHandler.onRequestCompleted(FirstRunActivity.FIRST_RUN_STEP_ACCOUNT);
                    }
                    showError(getString(R.string.signup_failed, ex.getMessage()));
                }
            }
        });
    }

    private void waitForVerification() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                layoutCollect.setVisibility(View.GONE);
                layoutVerify.setVisibility(View.VISIBLE);

                scheduleEmailVerify();
            }
        });
    }

    private void showError(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                View view = getView();
                if (view != null) {
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG).
                            setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                }
            }
        });
    }

    private void scheduleEmailVerify() {
        Activity activity = getActivity();
        if (activity != null && emailVerifyCheckScheduler == null) {
            OdyseeApp app = (OdyseeApp) activity.getApplication();
            emailVerifyCheckScheduler = app.getScheduledExecutor();
        }
        emailVerifyFuture = emailVerifyCheckScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkEmailVerified();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void checkEmailVerified() {
        CheckUserEmailVerifiedTask task = new CheckUserEmailVerifiedTask(getContext(), new CheckUserEmailVerifiedTask.CheckUserEmailVerifiedHandler() {
            @Override
            public void onUserEmailVerified() {
                layoutCollect.setVisibility(View.GONE);
                layoutVerify.setVisibility(View.GONE);
                if (emailVerifyFuture != null) {
                    emailVerifyFuture.cancel(true);
                    emailVerifyFuture = null;
                }

                addOdyseeAccountExplicitly(currentEmail);

                // send broadcast to indicate the user finished sign in
                Intent intent = new Intent(SignInActivity.ACTION_USER_FINISHED_SIGN_IN);
                //intent.putExtra("sourceTabId", sourceTabId);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                // perform wallet sync first before finish the activity
                finishWithWalletSync();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void runWalletSync(String password) {
        if (walletSyncStarted) {
            return;
        }

        walletSyncStarted = true;
        Helper.setViewVisibility(layoutCollect, View.GONE);
        Helper.setViewVisibility(layoutVerify, View.GONE);
        Helper.setViewVisibility(layoutWalletSyncContainer, View.VISIBLE);
        Helper.setViewVisibility(walletSyncProgress, View.VISIBLE);
        Helper.setViewVisibility(textWalletSyncLoading, View.VISIBLE);

        password = Utils.getSecureValue(MainActivity.SECURE_VALUE_KEY_SAVED_PASSWORD, getContext(), Lbry.KEYSTORE);
        if (Helper.isNullOrEmpty(password)) {
            password = Helper.getValue(inputWalletSyncPassword.getText());
        }

        final String actual = password;
        SyncGetTask task = new SyncGetTask(password,false,null, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncGetSuccess(WalletSync walletSync) {
                currentWalletSync = walletSync;
                Lbryio.lastRemoteHash = walletSync.getHash();
                if (Helper.isNullOrEmpty(actual)) {
                    processExistingWallet(walletSync);
                } else {
                    processExistingWalletWithPassword(actual);
                }
            }

            @Override
            public void onSyncGetWalletNotFound() {
                // no wallet found, get sync apply data and run the process
                processNewWallet();
            }
            @Override
            public void onSyncGetError(Exception error) {
                // try again
                Helper.setViewVisibility(walletSyncProgress, View.GONE);
                //Helper.setViewText(textWalletSyncLoading, error.getMessage());
                Helper.setViewVisibility(textWalletSyncLoading, View.GONE);
                Helper.setViewVisibility(layoutWalletSyncInputArea, View.VISIBLE);
                walletSyncStarted = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void finishWithWalletSync() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                runWalletSync("");
            }
        });
    }

    private void addOdyseeAccountExplicitly(String currentEmail) {
        // Add account explicitly
        Account account = new Account(currentEmail, ARG_ACCOUNT_TYPE);
        Context context = getContext();
        if (context == null) {
            context = getActivity();
        }

        AccountManager accountManager = AccountManager.get(context);
        try {
            Bundle bundle = new Bundle();
            bundle.putString("email", currentEmail);
            accountManager.addAccountExplicitly(account, "", bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Account act = accountManager.getAccounts()[0];
        accountManager.setAuthToken(act, ARG_AUTH_TYPE, Lbryio.AUTH_TOKEN);
    }

    private void finishSignIn() {
        if (firstRunStepHandler != null) {
            firstRunStepHandler.onCompleted(FirstRunActivity.FIRST_RUN_STEP_ACCOUNT);
        }
    }

    private void editEmail() {
        if (emailVerifyFuture != null) {
            emailVerifyFuture.cancel(true);
            emailVerifyCheckScheduler = null;
        }

        inputPassword.setText("");
        restoreControls(true);

        View view = getView();
        TransitionManager.beginDelayedTransition(view.findViewById(R.id.verification_activity));
        layoutVerify.setVisibility(View.GONE);
        layoutCollect.setVisibility(View.VISIBLE);
    }

    public void processExistingWallet(WalletSync walletSync) {
        // Try first sync apply
        SyncApplyTask applyTask = new SyncApplyTask("", walletSync.getData(), null, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncApplySuccess(String hash, String data) {
                // check if local and remote hash are different, and then run sync set
                Utils.setSecureValue(MainActivity.SECURE_VALUE_KEY_SAVED_PASSWORD, "", getContext(), Lbry.KEYSTORE);
                if (!hash.equalsIgnoreCase(Lbryio.lastRemoteHash) && !Helper.isNullOrEmpty(Lbryio.lastRemoteHash)) {
                    new SyncSetTask(Lbryio.lastRemoteHash, hash, data, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                finishSignIn();
            }

            @Override
            public void onSyncApplyError(Exception error) {
                // failed, request the user to enter a password
                Helper.setViewVisibility(walletSyncProgress, View.GONE);
                Helper.setViewVisibility(textWalletSyncLoading, View.GONE);
                Helper.setViewVisibility(inputWalletSyncPassword, View.VISIBLE);

                Helper.setViewVisibility(layoutWalletSyncInputArea, View.VISIBLE);
                walletSyncStarted = false;
            }
        });
        applyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void processExistingWalletWithPassword(String password) {
        Helper.setViewVisibility(textWalletSyncLoading, View.VISIBLE);
        Helper.setViewVisibility(inputWalletSyncPassword, View.GONE);

        if (currentWalletSync == null) {
            showError(getString(R.string.wallet_sync_op_failed));
            Helper.setViewText(textWalletSyncLoading, R.string.wallet_sync_op_failed);
            return;
        }

        Helper.setViewVisibility(walletSyncProgress, View.VISIBLE);
        Helper.setViewText(textWalletSyncLoading, R.string.apply_wallet_data);
        SyncApplyTask applyTask = new SyncApplyTask(password, currentWalletSync.getData(), null, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncApplySuccess(String hash, String data) {
                Utils.setSecureValue(MainActivity.SECURE_VALUE_KEY_SAVED_PASSWORD, password, getContext(), Lbry.KEYSTORE);
                // check if local and remote hash are different, and then run sync set
                if (!hash.equalsIgnoreCase(Lbryio.lastRemoteHash) && !Helper.isNullOrEmpty(Lbryio.lastRemoteHash)) {
                    new SyncSetTask(Lbryio.lastRemoteHash, hash, data, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                finishSignIn();
            }

            @Override
            public void onSyncApplyError(Exception error) {
                // failed, request the user to enter a password
                showError(error.getMessage());
                Helper.setViewVisibility(walletSyncProgress, View.GONE);
                Helper.setViewVisibility(textWalletSyncLoading, View.GONE);
                Helper.setViewVisibility(inputWalletSyncPassword, View.VISIBLE);
                Helper.setViewVisibility(layoutWalletSyncInputArea, View.VISIBLE);
                walletSyncStarted = false;
            }
        });
        applyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void processNewWallet() {
        SyncApplyTask fetchTask = new SyncApplyTask(true, null, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncApplySuccess(String hash, String data) { createNewRemoteSync(hash, data); }
            @Override
            public void onSyncApplyError(Exception error) {
                showError(error.getMessage());
                Helper.setViewVisibility(walletSyncProgress, View.GONE);
                Helper.setViewText(textWalletSyncLoading, R.string.wallet_sync_op_failed);

                walletSyncStarted = false;
            }
        });
        fetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void createNewRemoteSync(String hash, String data) {
        SyncSetTask setTask = new SyncSetTask("", hash, data, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncSetSuccess(String hash) {
                Lbryio.lastRemoteHash = hash;

                finishSignIn();
            }

            @Override
            public void onSyncSetError(Exception error) {
                showError(error.getMessage());
                Helper.setViewVisibility(walletSyncProgress, View.GONE);
                Helper.setViewText(textWalletSyncLoading, R.string.wallet_sync_op_failed);

                walletSyncStarted = false;
            }
        });
        setTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
