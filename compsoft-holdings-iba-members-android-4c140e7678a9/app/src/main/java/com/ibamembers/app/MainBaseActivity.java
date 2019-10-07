package com.ibamembers.app;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.LinearLayout;

import com.ibamembers.R;
import com.ibamembers.content.SquareImageView;

import butterknife.ButterKnife;

public class MainBaseActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        switch(getLayoutType()) {
            case SCROLLVIEW_NO_TOOLBAR:
            case SCROLLVIEW:
                setContentView(R.layout.base_activity);
                break;
            case NO_SCROLLVIEW_NO_TOOLBAR:
            case NO_SCROLLVIEW:
                setContentView(R.layout.base_activity_no_scroll);
                break;
            case IMAGE_TOOLBAR:
                setContentView(R.layout.base_activity_image_toolbar);
                break;
            default:
                setContentView(R.layout.base_activity);
                break;
        }

        View layoutIsLandscape = findViewById(R.id.base_layout_is_landscape);
        isLandscape = layoutIsLandscape != null;

        baseToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(baseToolbar);
        if (getLayoutType() == LayoutType.SCROLLVIEW_NO_TOOLBAR || getLayoutType() == LayoutType.NO_SCROLLVIEW_NO_TOOLBAR) {
            baseToolbar.setVisibility(View.GONE);
        }

        appBarFrame = findViewById(R.id.app_bar_frame);
        contentFrame = findViewById(R.id.content_frame);

        imageToolbar = findViewById(R.id.image_toolbar);
        contentActionButton = findViewById(R.id.content_fab);
        podcastControllerView = findViewById(R.id.podcast_controller_view);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_WORKER_FRAGMENT);
        if (fragment != null) {
            //noinspection unchecked
            workerFragment = (BaseWorkerFragment)fragment;
        }
        else {
            workerFragment = createWorkerFragment();
            if (workerFragment != null) {
                fragmentManager.beginTransaction().add(workerFragment, TAG_WORKER_FRAGMENT).commit();
            }
        }
    }

    public SquareImageView getImageToolbar() {
        return imageToolbar;
    }

    public void setContentFabVisibility(int visibility) {
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) contentActionButton.getLayoutParams();
        p.setAnchorId(View.NO_ID);
        contentActionButton.setLayoutParams(p);
        contentActionButton.setVisibility(visibility);
    }

    public void setContentFabClickListener(View.OnClickListener listener) {
        contentActionButton.setOnClickListener(listener);
    }

    public void updateContentFabStateImage(int drawable) {
        contentActionButton.setImageResource(drawable);
    }

    public LinearLayout getPodCastControllerView() {
        return podcastControllerView;
    }

    public void setPodCastImageViewTouchListener(View.OnTouchListener listener) {
        imageToolbar.setOnTouchListener(listener);
    }

    protected LayoutType getLayoutType() {
        return LayoutType.SCROLLVIEW;
    }

    protected void loadLayoutIntoAppbar(@LayoutRes int layoutId, Object viewModel) {
        View layout = getLayoutInflater().inflate(layoutId, null);
        ButterKnife.bind(viewModel, layout);
        appBarFrame.addView(layout);
    }

    protected void loadLayoutIntoContentFrame(@LayoutRes int layoutId, Object viewModel) {
        View layout = getLayoutInflater().inflate(layoutId, null);
        ButterKnife.bind(viewModel, layout);
        contentFrame.addView(layout);

    }

    protected void setToolbarVisibility(boolean isVisible) {
        if (baseToolbar != null) {
            if (isVisible) {
                baseToolbar.setVisibility(View.VISIBLE);
            } else {
                baseToolbar.setVisibility(View.GONE);
            }
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
