package com.ibamembers.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

public abstract class EventBusFragment extends BaseFragment {

    @Override
    public void onStart() {
        super.onStart();
        App app = getApp();
        if (app != null) {
            if (!app.getEventBus().isRegistered(this)) {
                app.getEventBus().register(this);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App app = getApp();
        if (app != null) {
            app.getEventBus().unregister(this);
        }
    }

    protected void showErrorDialog(String title, String message, String positiveButton) {
        showErrorDialog(title, message, positiveButton, true, null);
    }

    protected void showErrorDialog(String title, String message, String positiveButton, boolean isCancellable, DialogInterface.OnClickListener onClickListener) {
        Activity activity = getActivity();
        if (activity != null) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            alertDialog.setTitle(title)
                    .setMessage(message)
                    .setCancelable(isCancellable)
                    .setPositiveButton(positiveButton, onClickListener);
            alertDialog.show();
        }
    }
}
