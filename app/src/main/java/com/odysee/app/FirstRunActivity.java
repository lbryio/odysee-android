package com.odysee.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.odysee.app.callable.WalletBalanceFetch;
import com.odysee.app.exceptions.ApiCallException;
import com.odysee.app.exceptions.LbryioRequestException;
import com.odysee.app.exceptions.LbryioResponseException;
import com.odysee.app.listener.VerificationListener;
import com.odysee.app.model.Claim;
import com.odysee.app.model.WalletBalance;
import com.odysee.app.model.lbryinc.Reward;
import com.odysee.app.model.lbryinc.RewardVerified;
import com.odysee.app.tasks.RewardVerifiedHandler;
import com.odysee.app.tasks.claim.ClaimListResultHandler;
import com.odysee.app.tasks.claim.ClaimListTask;
import com.odysee.app.ui.findcontent.FollowingFragment;
import com.odysee.app.ui.firstrun.CreateChannelFragment;
import com.odysee.app.ui.rewards.RewardVerificationFragment;
import com.odysee.app.ui.firstrun.SignInFragment;
import com.odysee.app.utils.FirstRunStepHandler;
import com.odysee.app.utils.Helper;
import com.odysee.app.utils.Lbry;
import com.odysee.app.utils.LbryAnalytics;
import com.odysee.app.utils.Lbryio;
import com.odysee.app.utils.VerificationSkipQueue;

import org.json.JSONObject;

import lombok.SneakyThrows;

public class FirstRunActivity extends AppCompatActivity implements FirstRunStepHandler, VerificationListener {

    private BroadcastReceiver authReceiver;

    public static final int FIRST_RUN_STEP_ACCOUNT = 1;
    public static final int FIRST_RUN_STEP_CHANNEL = 2;
    public static final int FIRST_RUN_STEP_FOLLOW = 3;
    public static final int FIRST_RUN_STEP_REWARDS = 4;

    private static final String PREFERENCE_KEY_INTERNAL_CURRENT_FIRST_RUN_STEP = "com.odysee.app.CurrentFirstRunStep";

    private int currentStep;
    private boolean currentSignInMode;
    private String currentChannelName;
    private boolean ytSyncOptInChecked;
    private ViewPager2 viewPager;
    private ImageView pagerIndicator1;
    private ImageView pagerIndicator2;
    private ImageView pagerIndicator3;
    private ImageView pagerIndicator4;

    private MaterialButton buttonSkip;
    private MaterialButton buttonContinue;
    private ProgressBar progressIndicator;

    private VerificationSkipQueue verificationSkipQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change status bar text color depending on Night mode when app is running
        String darkModeAppSetting = ((OdyseeApp) getApplication()).getDarkModeAppSetting();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!darkModeAppSetting.equals(MainActivity.APP_SETTING_DARK_MODE_NIGHT) && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                //noinspection deprecation
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else {
            int defaultNight = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (darkModeAppSetting.equals(MainActivity.APP_SETTING_DARK_MODE_NOTNIGHT) || (darkModeAppSetting.equals(MainActivity.APP_SETTING_DARK_MODE_SYSTEM) && defaultNight == Configuration.UI_MODE_NIGHT_NO)) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    getWindow().getDecorView().getWindowInsetsController().setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                } else {
                    //noinspection deprecation
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    getWindow().getDecorView().getWindowInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            }
        }

        setContentView(R.layout.activity_first_run);

        verificationSkipQueue = new VerificationSkipQueue(this, new VerificationSkipQueue.ShowInProgressListener() {
            @Override
            public void maybeShowRequestInProgress() {
                if (currentStep == FIRST_RUN_STEP_REWARDS) {
                    onRequestInProgress(true);
                }
            }
        }, new RewardVerifiedHandler() {
            @Override
            public void onSuccess(RewardVerified rewardVerified) {
                if (Lbryio.currentUser != null) {
                    Lbryio.currentUser.setRewardApproved(rewardVerified.isRewardApproved());
                }

                if (!rewardVerified.isRewardApproved()) {
                    // show pending purchase message (possible slow card tx)
                    Snackbar.make(findViewById(R.id.first_run_pager), R.string.purchase_request_pending, Snackbar.LENGTH_LONG).show();
                } else  {
                    Snackbar.make(findViewById(R.id.first_run_pager), R.string.reward_verification_successful, Snackbar.LENGTH_LONG).show();
                }

                if (currentStep == FIRST_RUN_STEP_REWARDS) {
                    onRequestCompleted(FIRST_RUN_STEP_REWARDS);
                }
            }

            @Override
            public void onError(Exception error) {
                showFetchUserError(getString(R.string.purchase_request_failed_error));
                if (currentStep == FIRST_RUN_STEP_REWARDS) {
                    onRequestCompleted(FIRST_RUN_STEP_REWARDS);
                }
            }
        });
        verificationSkipQueue.createBillingClientAndEstablishConnection();

        viewPager = findViewById(R.id.first_run_pager);
        viewPager.setUserInputEnabled(false);
        viewPager.setSaveEnabled(false);
        viewPager.setAdapter(new FirstRunPagerAdapter(this));

        pagerIndicator1 = findViewById(R.id.pager_indicator_1);
        pagerIndicator2 = findViewById(R.id.pager_indicator_2);
        pagerIndicator3 = findViewById(R.id.pager_indicator_3);
        pagerIndicator4 = findViewById(R.id.pager_indicator_4);

        buttonContinue = findViewById(R.id.first_run_continue_button);
        buttonSkip = findViewById(R.id.first_run_skip_button);
        progressIndicator = findViewById(R.id.first_run_progress);

        buttonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCompleted(currentStep);
            }
        });

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentStep == FIRST_RUN_STEP_CHANNEL) {
                    if (!ytSyncOptInChecked) {
                        handleCreateChannel();
                    } else {
                        proceedToFollowStep();
                    }
                } else if (currentStep == FIRST_RUN_STEP_REWARDS) {
                    // final step (Use Odysee)
                    finishFirstRun();
                }
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                onBackPressed();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void checkEmailVerifiedRewardForChannelStep() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean emailRewardClaimed = sp.getBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_EMAIL_REWARD_CLAIMED, false);

        if (!emailRewardClaimed) {
            viewPager.setVisibility(View.INVISIBLE);
            onRequestInProgress(true);

            ((OdyseeApp) getApplication()).getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    String authToken = Lbryio.AUTH_TOKEN;
                    try {
                        String walletAddress = (String) Lbry.authenticatedGenericApiCall(Lbry.METHOD_ADDRESS_UNUSED, null, authToken);

                        Map<String, String> options = new HashMap<>();
                        options.put("reward_type", Reward.TYPE_CONFIRM_EMAIL);
                        options.put("wallet_address", walletAddress);
                        if (authToken != null) {
                            options.put("auth_token", authToken);
                        }

                        JSONObject reward = (JSONObject) Lbryio.parseResponse(
                                Lbryio.call("reward", "claim", options, Helper.METHOD_POST, null));
                        if (reward != null) {
                            // successfully claimed
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FirstRunActivity.this);
                                    prefs.edit().putBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_EMAIL_REWARD_CLAIMED, true).apply();
                                }
                            });
                        }

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                checkWalletBalanceForChannelStep();
                            }
                        });
                    } catch (ApiCallException | LbryioRequestException | LbryioResponseException ex) {
                        // failed, but we can still continue
                        checkWalletBalanceForChannelStep();
                    }
                }
            });
            return;
        }

        checkWalletBalanceForChannelStep();
    }

    private void emailRewardChecked() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                viewPager.setVisibility(View.VISIBLE);
                progressIndicator.setVisibility(View.GONE);
                proceedToChannelStep();
                displayControlsForStep(currentStep);
            }
        });
    }

    private void checkWalletBalanceForChannelStep() {
        Activity a = this;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Callable<WalletBalance> c = new WalletBalanceFetch(Lbryio.AUTH_TOKEN);
                Future<WalletBalance> f = ((OdyseeApp) a.getApplication()).getExecutor().submit(c);

                try {
                    WalletBalance wb = f.get();
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (wb.getAvailable().doubleValue() < Helper.MIN_SPEND) {
                                // proceed to rewards step
                                viewPager.setVisibility(View.VISIBLE);
                                progressIndicator.setVisibility(View.GONE);
                                // TODO: Figure this out
                                proceedToFollowStep();
                            } else {
                                emailRewardChecked();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    emailRewardChecked();
                }
            }
        });
        t.start();
    }

    private void handleCreateChannel() {
        if (Helper.isNullOrEmpty(currentChannelName)) {
            showError(getString(R.string.no_channel_name));
            return;
        }

        onRequestInProgress(true);
        ((OdyseeApp) getApplication()).getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> options = new HashMap<>();
                options.put("name", String.format("@%s", currentChannelName));
                options.put("bid", new DecimalFormat(Helper.SDK_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US)).format(0.001));
                options.put("title", currentChannelName);
                options.put("blocking", true);

                try {
                    JSONObject result = (JSONObject) Lbry.authenticatedGenericApiCall(Lbry.METHOD_CHANNEL_CREATE, options, Lbryio.AUTH_TOKEN);
                    if (result.has("outputs")) {
                        // success
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                proceedToFollowStep();
                                onRequestCompleted(FIRST_RUN_STEP_REWARDS);
                            }
                        });
                    }
                } catch (ApiCallException | ClassCastException ex) {
                    // failed
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showError(ex.getMessage());
                        }
                    });
                    onRequestCompleted(FIRST_RUN_STEP_CHANNEL);
                }
            }
        });
    }

    private void checkCurrentFirstRunStep() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int currentStep = sp.getInt(PREFERENCE_KEY_INTERNAL_CURRENT_FIRST_RUN_STEP, FIRST_RUN_STEP_ACCOUNT);

        if (currentStep != FIRST_RUN_STEP_ACCOUNT) {
            viewPager.setVisibility(View.INVISIBLE);
        }

        if (currentStep == FIRST_RUN_STEP_CHANNEL) {
            checkEmailVerifiedRewardForChannelStep();
            return;
        }

        setActiveStep(currentStep);
        viewPager.setCurrentItem(currentStep - 1);
        displayControlsForStep(currentStep);
        if (currentStep != FIRST_RUN_STEP_ACCOUNT) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewPager.setVisibility(View.VISIBLE);
                }
            }, 1000);
        }
    }

    private void displayControlsForStep(int step) {
        switch (step) {
            case FIRST_RUN_STEP_ACCOUNT:
            case FIRST_RUN_STEP_FOLLOW:
            default:
                buttonSkip.setVisibility(View.VISIBLE);
                buttonContinue.setVisibility(View.INVISIBLE);
                break;

            case FIRST_RUN_STEP_CHANNEL:
                buttonSkip.setVisibility(View.VISIBLE);
                buttonContinue.setVisibility(View.INVISIBLE);
                buttonContinue.setText(R.string.create);
                break;

            case FIRST_RUN_STEP_REWARDS:
                buttonSkip.setVisibility(View.INVISIBLE);
                buttonContinue.setVisibility(View.VISIBLE);
                buttonContinue.setText(R.string.use_odysee);
                break;
        }
    }

    private void setActiveStep(int step) {
        currentStep = step;
        ImageView[] indicators = { pagerIndicator1, pagerIndicator2, pagerIndicator3, pagerIndicator4 };
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setImageDrawable(AppCompatResources.getDrawable(this, (step == i + 1) ? R.drawable.selected_page_dot : R.drawable.page_dot));
        }
    }

    public void onResume() {
        super.onResume();
        checkCurrentFirstRunStep();
        LbryAnalytics.setCurrentScreen(this, "First Run", "FirstRun");
    }

    private void finishFirstRun() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_FIRST_RUN_COMPLETED, true).apply();

        // first_run_completed event
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_FIRST_RUN_COMPLETED);

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCompleted(int completedStep) {
        if (completedStep == FIRST_RUN_STEP_ACCOUNT) {
            // move to channel creation step
            onRequestInProgress(true);
            checkChannelStep();
        } else if (completedStep == FIRST_RUN_STEP_CHANNEL) {
            proceedToFollowStep();
        } else if (completedStep == FIRST_RUN_STEP_FOLLOW) {
            proceedToRewardsStep();
        } else if (completedStep == FIRST_RUN_STEP_REWARDS) {
            finishFirstRun();
        }
    }

    private void checkChannelStep() {
        Map<String, Object> options = Lbry.buildClaimListOptions(Claim.TYPE_CHANNEL, 1, 999, true);
        ClaimListTask task = new ClaimListTask(options, Lbryio.AUTH_TOKEN, null, new ClaimListResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                onRequestInProgress(false);
                if (claims.size() == 0) {
                    // no channels, move to first run step: channel
                    checkEmailVerifiedRewardForChannelStep();
                } else {
                    // this user already has a channel, move to the final step: rewards verification
                    proceedToFollowStep();
                }
            }

            @Override
            public void onError(Exception error) {
                checkEmailVerifiedRewardForChannelStep();
                //onRequestCompleted(FIRST_RUN_STEP_CHANNEL);
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void proceedToChannelStep() {
        setActiveStep(FIRST_RUN_STEP_CHANNEL);
        viewPager.setCurrentItem(FIRST_RUN_STEP_CHANNEL - 1);
        displayControlsForStep(FIRST_RUN_STEP_CHANNEL);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt(PREFERENCE_KEY_INTERNAL_CURRENT_FIRST_RUN_STEP, FIRST_RUN_STEP_CHANNEL).apply();
    }

    private void proceedToFollowStep() {
        if (currentSignInMode /* Sign In */) {
            proceedToRewardsStep();
            return;
        }

        setActiveStep(FIRST_RUN_STEP_FOLLOW);
        viewPager.setCurrentItem(FIRST_RUN_STEP_FOLLOW - 1);
        displayControlsForStep(FIRST_RUN_STEP_FOLLOW);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt(PREFERENCE_KEY_INTERNAL_CURRENT_FIRST_RUN_STEP, FIRST_RUN_STEP_FOLLOW).apply();
    }

    private void proceedToRewardsStep() {
        setActiveStep(FIRST_RUN_STEP_REWARDS);
        viewPager.setCurrentItem(FIRST_RUN_STEP_REWARDS - 1);
        displayControlsForStep(FIRST_RUN_STEP_REWARDS);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt(PREFERENCE_KEY_INTERNAL_CURRENT_FIRST_RUN_STEP, FIRST_RUN_STEP_REWARDS).apply();
    }

    @Override
    public void onRequestInProgress(boolean showProgress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonSkip.setVisibility(View.INVISIBLE);
                buttonContinue.setVisibility(View.INVISIBLE);
                progressIndicator.setVisibility(showProgress ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onRequestCompleted(int step) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressIndicator.setVisibility(View.GONE);
                displayControlsForStep(step);

                if (step == FIRST_RUN_STEP_CHANNEL) {
                    onChannelNameUpdated(currentChannelName);
                }
            }
        });
    }

    @Override
    public void onSignInModeChanged(boolean signInMode) {
        currentSignInMode = signInMode;
    }

    @Override
    public void onChannelNameUpdated(String channelName) {
        currentChannelName = channelName;
        buttonContinue.setVisibility(Helper.isNullOrEmpty(channelName) ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onYouTubeSyncOptInCheckChanged(boolean checked) {
        ytSyncOptInChecked = checked;
        buttonContinue.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
        buttonContinue.setText(checked ? R.string.continue_text : R.string.create);
    }

    public void showError(String message) {
        Snackbar.make(findViewById(R.id.first_run_main), message, Snackbar.LENGTH_LONG).
                setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onSkipped() {

    }

    private static class CheckInstallIdTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private final InstallIdHandler handler;
        public CheckInstallIdTask(Context context, InstallIdHandler handler) {
            this.context = context;
            this.handler = handler;
        }
        protected Boolean doInBackground(Void... params) {
            // Load the installation id from the file system
            File[] dirs = context.getExternalFilesDirs(null);
            String lbrynetDir = dirs[0].getAbsolutePath().concat("/lbrynet");

            File dir = new File(lbrynetDir);
            boolean dirExists = dir.isDirectory();
            if (!dirExists) {
                dirExists = dir.mkdirs();
            }

            if (!dirExists) {
                return false;
            }

            String installIdPath = String.format("%s/install_id", lbrynetDir);
            File file = new File(installIdPath);
            String installId  = null;
            if (!file.exists()) {
                // generate the install_id
                installId = Lbry.generateId();
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(file));
                    writer.write(installId);
                } catch (IOException ex) {
                    return false;
                } finally {
                    Helper.closeCloseable(writer);
                }
            } else {
                // read the installation id from the file
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(installIdPath)));
                    installId = reader.readLine();
                } catch (IOException ex) {
                    return false;
                } finally {
                    Helper.closeCloseable(reader);
                }
            }

            if (!Helper.isNullOrEmpty(installId)) {
                Lbry.INSTALLATION_ID = installId;
            }
            return !Helper.isNullOrEmpty(installId);
        }
        protected void onPostExecute(Boolean result) {
            if (handler != null) {
                handler.onInstallIdChecked(result);
            }
        }

        public interface InstallIdHandler {
            void onInstallIdChecked(boolean result);
        }
    }

    private static class FirstRunPagerAdapter extends FragmentStateAdapter {
        private final FragmentActivity activity;

        public FirstRunPagerAdapter(FragmentActivity activity) {
            super(activity);
            this.activity = activity;
        }

        @NonNull
        @Override
        @SneakyThrows
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                default:
                    SignInFragment siFragment = SignInFragment.class.newInstance();
                    if (activity instanceof FirstRunStepHandler) {
                        siFragment.setFirstRunStepHandler((FirstRunStepHandler) activity);
                    }
                    return siFragment;
                case 1:
                    CreateChannelFragment ccFragment = CreateChannelFragment.class.newInstance();
                    if (activity instanceof FirstRunStepHandler) {
                        ccFragment.setFirstRunStepHandler((FirstRunStepHandler) activity);
                    }
                    return ccFragment;
                case 2:
                    FollowingFragment fFragment = FollowingFragment.class.newInstance();
                    if (activity instanceof FirstRunStepHandler) {
                        fFragment.setFirstRunStepHandler((FirstRunStepHandler) activity);
                    }
                    return fFragment;
                case 3:
                    RewardVerificationFragment rvFragment = RewardVerificationFragment.class.newInstance();
                    if (activity instanceof FirstRunStepHandler) {
                        rvFragment.setFirstRunStepHandler((FirstRunStepHandler) activity);
                    }
                    return rvFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    @Override
    public void onBackPressed() {
        // do nothing
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }


    @Override
    public void onEmailAdded(String email) {

    }

    @Override
    public void onEmailEdit() {

    }

    @Override
    public void onEmailVerified() {

    }

    @Override
    public void onPhoneAdded(String countryCode, String phoneNumber) {

    }

    @Override
    public void onPhoneVerified() {

    }

    @Override
    public void onManualVerifyContinue() {

    }

    @Override
    public void onSkipQueueAction() {
        verificationSkipQueue.onSkipQueueAction(this);
    }

    @Override
    public void onTwitterVerified() {

    }

    @Override
    public void onManualProgress(boolean progress) {

    }

    public void showFetchUserError(String message) {
        Snackbar.make(viewPager, message, Snackbar.LENGTH_LONG).
                setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
    }
}
