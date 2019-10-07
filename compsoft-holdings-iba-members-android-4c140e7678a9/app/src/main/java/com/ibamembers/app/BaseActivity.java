package com.ibamembers.app;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ibamembers.R;
import com.ibamembers.content.SquareImageView;

import java.util.List;

public class BaseActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    protected static final String TAG_WORKER_FRAGMENT = "WorkerFragment";
    protected static final String TAG_ONLY_FRAGMENT = "Fragment";
    protected static final int REQUEST_CODE_PERMISSIONS = 100;

    protected Toolbar baseToolbar;
    protected SquareImageView imageToolbar;
    protected LinearLayout podcastControllerView;
    protected FloatingActionButton contentActionButton;
    protected BaseWorkerFragment workerFragment;
    protected FrameLayout appBarFrame;
    protected FrameLayout contentFrame;
    protected boolean isLandscape;

    private ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback;

    protected enum LayoutType {
        SCROLLVIEW,
        SCROLLVIEW_NO_TOOLBAR,
        NO_SCROLLVIEW,
        NO_SCROLLVIEW_NO_TOOLBAR,
        IMAGE_TOOLBAR
    }

    protected void setDisplayHomeAsUpEnabled(boolean enabled) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enabled);
            actionBar.setHomeButtonEnabled(enabled);
        }
    }

    protected BaseWorkerFragment createWorkerFragment() {
        return null;
    }

    protected App getApp() {
        return (App) getApplication();
    }

    protected <F extends Fragment> F getOrAddOnlyFragment(Class<F> fragmentClass) {
        return getOrAddFragment(fragmentClass, TAG_ONLY_FRAGMENT, R.id.content_frame);
    }

    protected <F extends Fragment> F getOrAddOnlyFragment(Class<F> fragmentClass, Bundle args) {
        return getOrAddFragment(fragmentClass, TAG_ONLY_FRAGMENT, R.id.content_frame, args);
    }

    protected <F extends Fragment> F getOrAddOnlyFragment(Class<F> fragmentClass, String tag) {
        return getOrAddFragment(fragmentClass, tag, R.id.content_frame);
    }

    protected <F extends Fragment> F getOrAddOnlyFragment(Class<F> fragmentClass, String tag, Bundle args) {
        return getOrAddFragment(fragmentClass, tag, R.id.content_frame, args);
    }

    protected <F extends Fragment> F getOrAddFragment(Class<F> fragmentClass, String tag, @IdRes int containerViewId) {
        return getOrAddFragment(fragmentClass, tag, containerViewId, null);
    }

    protected <F extends Fragment> F getOrAddFragment(Class<F> fragmentClass, String tag, @IdRes int containerViewId, Bundle args) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        F typedFragment = null;
        if (fragment != null) {
            if (fragmentClass.isAssignableFrom(fragment.getClass())) {
                typedFragment = fragmentClass.cast(fragment);
            }
        }
        if (typedFragment == null) {
            try {
                typedFragment = fragmentClass.getConstructor().newInstance();
                if (args != null) {
                    typedFragment.setArguments(args);
                }
                fragmentManager.beginTransaction().add(containerViewId, typedFragment, tag).commit();
            } catch (Exception ex) {
                throw new RuntimeException("Worker fragment must contain a public no-arg constructor", ex);
            }
        }
        return typedFragment;
    }

    public void requestPermissions(List<String> permissions, ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback) {
        String[] params = permissions.toArray(new String[permissions.size()]);
        requestPermissions(params, onRequestPermissionsResultCallback);
    }

    public void requestPermissions(String[] permissions, ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback) {
        this.onRequestPermissionsResultCallback = onRequestPermissionsResultCallback;
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS);
    }

    protected void setConferenceStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.conference_theme_primary_dark));
        }
    }

    /**
     * Hides the keyboard.
     */
    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (imm != null && view != null) { imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS && onRequestPermissionsResultCallback != null) {
            this.onRequestPermissionsResultCallback.onRequestPermissionsResult(requestCode, permissions, grantResults);
            this.onRequestPermissionsResultCallback = null;
        }
    }
}
