package com.ibamembers.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ibamembers.R;
import com.ibamembers.app.BaseFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserProfileNoAccessFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.profile_no_access_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.upgrade_button)
    public void upgradeButtonClicked() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.iba_contact_email), null));
        startActivity(Intent.createChooser(intent, "Send Email"));
    }
}