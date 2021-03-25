package com.odysee.app.tasks.verification;

import android.content.Context;
import android.os.AsyncTask;

import com.odysee.app.exceptions.AuthTokenInvalidatedException;
import com.odysee.app.model.lbryinc.User;
import com.odysee.app.utils.Lbryio;

public class CheckUserEmailVerifiedTask extends AsyncTask<Void, Void, Boolean> {
    private final Context context;
    private final CheckUserEmailVerifiedHandler handler;

    public CheckUserEmailVerifiedTask(Context context, CheckUserEmailVerifiedHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    protected Boolean doInBackground(Void... params) {
        try {
            User user = Lbryio.fetchCurrentUser(context);
            return user != null && user.isHasVerifiedEmail();
        } catch (AuthTokenInvalidatedException ex) {
            return  false;
        }
    }

    protected void onPostExecute(Boolean result) {
        if (handler != null && result) {
            // we only care if the user has actually verified their email
            handler.onUserEmailVerified();
        }
    }

    public interface CheckUserEmailVerifiedHandler {
        void onUserEmailVerified();
    }
}
