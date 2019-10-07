package com.ibamembers.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.ibamembers.app.App;
import com.ibamembers.app.MainBaseActivity;
import com.ibamembers.app.SettingDao;
import com.ibamembers.main.MainActivity;

import java.sql.SQLException;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends MainBaseActivity implements LoginFragment.LoginFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setToolbarVisibility(false);

        App app = getApp();
        if (app != null) {
            try {
                if (isUserAlreadyRegistered(app)) {
                    skipLogin();
                } else {
                    getOrAddOnlyFragment(LoginFragment.class);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (getIntent() != null) {
            String dataString = getIntent().getDataString();
            if (dataString != null && dataString.contains("viewevent")) {
                Log.i("LoginActivity", "We have a eventLink");

                String eventIdString = dataString.substring(dataString.indexOf("?id=") + 4, dataString.length());
                int eventId = Integer.parseInt(eventIdString);

                App app = getApp();
                if (app != null) {
                    app.saveLoadedEventId(eventId);
                }
            }
        }
    }

    private boolean isUserAlreadyRegistered(App app) throws SQLException {
        SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
        return settingDao.isUserRegistered();
    }

    private void logUserOut(App app) throws SQLException {
        SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
        settingDao.setUserRegistered(false);
        settingDao.setLoginDate(null);
    }

    private void skipLogin() {
        getOrAddOnlyFragment(LoginFragment.class, LoginFragment.getLoginFragmentArguments(true));
    }

    @Override
    public void loginComplete() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
