package com.ibamembers.messages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.EventBusFragment;
import com.ibamembers.app.IBAUtils;
import com.ibamembers.messages.job.GeneralMessageModel;
import com.ibamembers.messages.job.MessageStatusDeleted;
import com.ibamembers.messages.job.MessageStatusRead;
import com.ibamembers.messages.job.SetMessagesStatusDeletedJob;
import com.ibamembers.messages.job.SetMessagesStatusReadJob;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessagesDetailFragment extends EventBusFragment {

    public static Bundle getMessagesDetailFragmentArgs(String messageString) {
        Bundle bundle = new Bundle();
        bundle.putString(MessagesDetailActivity.KEY_MESSAGE_STRING, messageString);
        return bundle;
    }

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.message_detail_title)
    protected TextView messageDetailTitle;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.message_detail_date)
    protected TextView messageDetailDate;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.message_detail_description)
    protected TextView messageDetailDescription;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.message_detail_webview)
    protected WebView messageDetailWebview;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.progressBar)
    protected ProgressBar progressBar;

    private MenuItem deleteMenuItem;

    private GeneralMessageModel currentMessage;
    private SimpleDateFormat apiDateFormat;
    private boolean isLandscape;

    public int getCurrentMessageId() {
        if (currentMessage != null) {
            return currentMessage.getAppUserMessageId();
        }
        return -1;
    }

    public enum MessageStatus {
        Unread,
        Read,
        Deleted;

        public static MessageStatus forInt(int id) {
            switch (id) {
                case 0:
                    return Unread;
                case 1:
                    return Read;
                case 2:
                    return Deleted;
            }
            return null;
        }
    }

    public enum MessageType {
        Standard,
        Renewal,
        Upgrade,
        EventPass,
        Profile,
        Other;

        public static MessageType forInt(int id) {
            switch (id) {
                case 0:
                    return Standard;
                case 1:
                    return Renewal;
                case 2:
                    return Upgrade;
                case 3:
                    return EventPass;
                case 10:
                    return Profile;
                default:
                    return Other;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_detail_fragment, container, false);
        View layoutIsLandscape = view.findViewById(R.id.message_detail_is_landscape);
        isLandscape = layoutIsLandscape != null;

        ButterKnife.bind(this, view);

        apiDateFormat = new SimpleDateFormat(getString(R.string.default_api_date_format), Locale.getDefault());
        apiDateFormat.setTimeZone(TimeZone.getDefault());

        Bundle args = getArguments();
        if (args != null) {
            String messageString = args.getString(MessagesDetailActivity.KEY_MESSAGE_STRING);
            currentMessage = new Gson().fromJson(messageString, GeneralMessageModel.class);
            fillView();
            setMessageAsRead();
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.message_detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void confirmDelete() {
        Activity activity = getActivity();
        if (activity != null) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            alertDialog.setMessage(getString(R.string.messages_delete_message))
                    .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setMessageAsDeleted();
                        }
                    })
                    .setNegativeButton(getString(android.R.string.cancel), null);
            alertDialog.show();
        }
    }

    private void setMessageAsRead() {
        if (currentMessage.getStatus() == MessagesDetailFragment.MessageStatus.Unread) {
            App app = getApp();
            if (app != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.message_status_date_format), Locale.getDefault());
                String dateString = dateFormat.format(new Date());
                try {
                    app.getJobManager(App.JobQueueName.Network).addJobInBackground(new SetMessagesStatusReadJob(new MessageStatusRead(currentMessage.getAppUserMessageId(), dateString)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setMessageAsDeleted() {
        App app = getApp();
        if (app != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.message_status_date_format), Locale.getDefault());
            String dateString = dateFormat.format(new Date());
            try {
                Log.i("MessagesFragment", "Calling delete API...");
                app.getJobManager(App.JobQueueName.Network).addJobInBackground(new SetMessagesStatusDeletedJob(currentMessage, new MessageStatusDeleted(currentMessage.getAppUserMessageId(), dateString)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SetMessagesStatusDeletedJob.MessageDeletedSuccess response) {
        getApp().sendEventToAnalytics(ANALYTIC_CATEGORY_MESSAGE, "Normal message deleted", null);
        if (!isLandscape) {
            Activity activity = getActivity();
            if (activity != null && activity instanceof MessagesDetailActivity) {
                activity.finish();
            }
        }
    }

    private void fillView() {
        if (currentMessage != null) {
            String title = currentMessage.getTitle();
            Date date = currentMessage.getDate();
            String description = currentMessage.getText();
            String url = currentMessage.getUrl();

            String finalDateString = IBAUtils.formatMessageTime(getActivity(), date, true);
            if (!TextUtils.isEmpty(finalDateString)) {
                messageDetailDate.setText(finalDateString);
            }

            if (!TextUtils.isEmpty(title)) {
                messageDetailTitle.setText(title);
            }

            if (!TextUtils.isEmpty(url)) {
                messageDetailWebview.setVisibility(View.VISIBLE);
                setupWebViewAndLoadUrl(url);
            } else if (!TextUtils.isEmpty(description)) {
                messageDetailDescription.setText(description);
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebViewAndLoadUrl(String url) {
        progressBar.setVisibility(View.VISIBLE);
        WebSettings webSettings = messageDetailWebview.getSettings();
        webSettings.setAppCachePath( getApp().getCacheDir().getAbsolutePath() );
        webSettings.setAllowFileAccess( true );
        webSettings.setAppCacheEnabled( true );
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode( WebSettings.LOAD_DEFAULT );
        messageDetailWebview.setWebViewClient(new IBAWebViewClient());
        messageDetailWebview.loadUrl(url);
        if (url.contains("aspx")) {
            messageDetailWebview.setDownloadListener(new DownloadListener() {
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });
        }
    }

    private class IBAWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
