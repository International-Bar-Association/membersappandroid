package com.ibamembers.login;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.DataManager;
import com.ibamembers.app.EventBusFragment;
import com.ibamembers.app.IntentManager;
import com.ibamembers.app.SettingDao;
import com.ibamembers.content.WebViewActivity;
import com.ibamembers.profile.job.GetProfileJob;
import com.ibamembers.profile.job.ProfileModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.SQLException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class LoginFragment extends EventBusFragment {

    private static final String KEY_SKIP_LOGIN = "KEY_SKIP_LOGIN";

    public static Bundle getLoginFragmentArguments(boolean skipLogin) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_SKIP_LOGIN, skipLogin);
        return args;
    }

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.login_progress_bar)
    protected ProgressBar progressBar;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.login_button)
    protected Button loginButton;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.login_username_edit)
    protected EditText usernameEdit;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.login_password_edit)
    protected TextInputEditText passwordEdit;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.login_remember_me_checkbox)
    protected CheckBox rememberMeCheckBox;

    private final String LOGIN_FRAGMENT_TAG = "LOGIN_FRAGMENT_TAG";
    private LoginFragmentListener loginFragmentListener;
    private boolean skipLogin;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        if (args != null) {
            skipLogin = args.getBoolean(KEY_SKIP_LOGIN);
        }

        if (skipLogin) {
            //SkipLogin is called if user is already signed in
            try {
                getUserProfile();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            fillViews();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        passwordEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (validateCredentials()) {
                        login();
                    }
                }
                return false;
            }
        });

        return view;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.login_button)
    public void loginClicked() {
        if (validateCredentials()) {
            login();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.login_remember_me)
    public void rememberMeClicked() {
        boolean isChecked = !rememberMeCheckBox.isChecked();
        rememberMeCheckBox.setChecked(isChecked);

        try {
            setRememberMe(isChecked);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @OnCheckedChanged(R.id.login_remember_me_checkbox)
    public void rememberMeCheckBoxChanged(boolean checked) {
        try {
            setRememberMe(checked);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.login_contact_iba)
    public void contactIbaClicked() {
        App app = getApp();
        Activity activity = getActivity();
        if (app != null && activity != null) {
            IntentManager intentManager = app.getIntentManager();
            intentManager.contactIba(activity);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.login_forgot_password)
    public void forgotPasswordClicked() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.startActivity(WebViewActivity.getWebViewActivityIntent(activity, null, "https://www.ibanet.org/Access/ForgottenDetails.aspx"));
        }
    }

    private void fillViews() throws SQLException {
        App app = getApp();
        if (app != null) {
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            boolean rememberMe = settingDao.isRememberMe();

            if (rememberMe) {
                rememberMeCheckBox.setChecked(true);

                String savedUserName = settingDao.getUsername();
                if (!TextUtils.isEmpty(savedUserName)) {
                    usernameEdit.setText(savedUserName);
                    passwordEdit.requestFocus();
                }
            }
        }
    }

    private void login() {
        App app = getApp();
        Activity activity = getActivity();
        if (app != null && activity != null) {
            showProgressIndication(true);
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            app.getJobManager(App.JobQueueName.Network).addJobInBackground(new LoginJob(usernameEdit.getText().toString(), passwordEdit.getText().toString()));

        }
    }

    private void getUserProfile() throws SQLException  {
        App app = getApp();
        if (app != null) {
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            String sessionToken = settingDao.getSessionToken();
            int profileId = settingDao.getCachedId();

            if (!TextUtils.isEmpty(sessionToken)) {
                app.getJobManager(App.JobQueueName.Network).addJobInBackground(new GetProfileJob(profileId, LOGIN_FRAGMENT_TAG));
                showProgressIndication(true);
            }
        }
    }

    private void showProgressIndication(boolean isVisible) {
        if (isVisible) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateCredentials() {
        Activity activity = getActivity();
        if (activity != null) {
            boolean checkPassed = true;

            Resources resources = activity.getResources();
            String username = usernameEdit.getText().toString();
            String password = passwordEdit.getText().toString();

            if (TextUtils.isEmpty(username)) {
                checkPassed = false;
                usernameEdit.setError(resources.getString(R.string.login_validation_username_empty));
            }

            if (TextUtils.isEmpty(password)) {
                checkPassed = false;
                passwordEdit.setError(resources.getString(R.string.login_validation_password_empty));
            }

            return checkPassed;
        }

        return false;
    }

    private boolean isValidEmailPattern(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    private void setRememberMe(boolean isChecked) throws SQLException {
        App app = getApp();
        if (app != null) {
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            settingDao.setRememberMe(isChecked);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LoginJob.Success loginJobSuccess) {
        //After push token is refresh, getUserProfile and login
        try {
            getUserProfile();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LoginJob.Error loginJobError) {
        showProgressIndication(false);
        String message = loginJobError.getErrorMessage();
        if (message != null) serverErrorReceived(loginJobError.getErrorMessage());
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GetProfileJob.Success success) {
        if (success.getJobTag().equals(LOGIN_FRAGMENT_TAG)) {
            showProgressIndication(false);
            App app = getApp();
            if (app != null) {
                try {
                    if (checkClassPrivileges(success.getProfileModel())) {
                        saveUserProfileData(success.getProfileModel());
                        loginFragmentListener.loginComplete();
                    } else {
                        showErrorDialog(null, getString(R.string.login_privilege_failed), getString(android.R.string.ok));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GetProfileJob.GetProfileJobError error) {
        App app = getApp();
        final Activity activity = getActivity();
        if (app != null && activity != null) {
            Resources resources = activity.getResources();
            if (error.getStatus() == activity.getResources().getInteger(R.integer.session_token_invalid_code)) {
                app.getConnectionManager().sessionExpired(activity, app);
            } else {
                showProgressIndication(false);
                new AlertDialog.Builder(activity)
                        .setMessage(resources.getString(R.string.login_failed_error_message_internet))
                        .setPositiveButton(resources.getString(R.string.login_failed_error_positive_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //loginFragmentListener.loginComplete();
                            }
                        })
                        .show();
            }
        }
    }

    private void serverErrorReceived(String errorMessage) {
        Activity activity = getActivity();
        if (activity != null) {
            Resources resources = activity.getResources();
            String title;
            String message;
            String positiveButton;

            if (TextUtils.isEmpty(errorMessage)) {
                title = resources.getString(R.string.login_failed_error_title_internet);
                message = resources.getString(R.string.login_failed_error_message_internet);
                positiveButton = resources.getString(R.string.login_failed_error_positive_button);
            }
            else {
                title = resources.getString(R.string.login_failed_error_title);
                message = resources.getString(R.string.login_failed_error_message);
                positiveButton = resources.getString(R.string.login_failed_error_positive_button);
            }

            showErrorDialog(title, message, positiveButton);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            loginFragmentListener = (LoginFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement LoginFragmentListener");
        }
    }

    private boolean checkClassPrivileges(ProfileModel profileModel) {
        Resources res = getResources();
        int profileClass = (int) profileModel.getAccess().getProfileClass();
        int[] bits = res.getIntArray(R.array.user_class_login_allowed);
        boolean isContained = false;
        for (Integer profileClassInt : bits) {
            if (profileClassInt == profileClass) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    private void saveUserProfileData(ProfileModel profileModel) throws SQLException {
        App app = getApp();
        if (app != null) {
            String firstName = profileModel.getFirstName();
            String lastName = profileModel.getLastName();
            int userId = profileModel.getUserId();
            float profileClass = profileModel.getAccess().getProfileClass();
            boolean _public = profileModel.is_public();
            String firmName = profileModel.getFirmName();
            String jobPosition = profileModel.getJobPosition();
            String profilePictureUrl = profileModel.getProfilePictureUrl();
            String email = profileModel.getEmail();
            String biography = profileModel.getBiography();
            String phone = profileModel.getPhone();
            float[] areaOfPractice = profileModel.getAreasOfPracticeIds();
            float[] committees = profileModel.getCommitteeIds();
            String[] addressLines = profileModel.getAddress().getAddressLines();
            String city = profileModel.getAddress().getCity();
            String county = profileModel.getAddress().getCounty();
            String state = profileModel.getAddress().getState();
            String country = profileModel.getAddress().getCountry();
            String zip = profileModel.getAddress().getPcZip();

            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();

            if (!TextUtils.isEmpty(firstName)) {
                settingDao.setCachedFirstName(firstName);
            }

            if (!TextUtils.isEmpty(lastName)) {
                settingDao.setCachedLastName(lastName);
            }

            settingDao.setCachedId(userId);
            settingDao.setCachedClass(profileClass);
            settingDao.setPublic(_public);

            if (!TextUtils.isEmpty(firmName)) {
                settingDao.setCachedFirmName(firmName);
            }

            if (!TextUtils.isEmpty(jobPosition)) {
                settingDao.setCachedJobPosition(jobPosition);
            }

            if (!TextUtils.isEmpty(profilePictureUrl)) {
                settingDao.setCachedProfilePictureUrl(profilePictureUrl);
            }

            if (!TextUtils.isEmpty(email)) {
                settingDao.setCachedEmail(email);
            }

            if (!TextUtils.isEmpty(biography)) {
                settingDao.setCachedBiography(biography);
            }

            if (!TextUtils.isEmpty(phone)) {
                settingDao.setCachedPhone(phone);
            }

            DataManager dataManager = app.getDataManager();
            settingDao.setCachedAreaOfPracticeIds(dataManager.formatFloatArray(areaOfPractice));
            settingDao.setCachedCommitteeIds(dataManager.formatFloatArray(committees));
            settingDao.setCachedAddressLines(app.getDataManager().formatAddressLines(addressLines, city, zip, country));

            settingDao.setImageData(profileModel.getImageData());

            if (!TextUtils.isEmpty(city)) {
                settingDao.setCachedCity(city);
            }

            if (!TextUtils.isEmpty(county)) {
                settingDao.setCachedCounty(county);
            }

            if (!TextUtils.isEmpty(state)) {
                settingDao.setCachedState(state);
            }

            if (!TextUtils.isEmpty(country)) {
                settingDao.setCachedCountry(country);
            }

            if (!TextUtils.isEmpty(zip)) {
                settingDao.setCachedZip(zip);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginFragmentListener = null;
    }

    public interface LoginFragmentListener {
        void loginComplete();
    }
}
