package com.ibamembers.content;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.github.barteksc.pdfviewer.PDFView;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.BaseFragment;
import com.ibamembers.content.job.DownloadPDFJob;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewFragment extends BaseFragment {

    public static Bundle getWebViewFragmentArgs(String offlineUrl, String url) {
        Bundle bundle = new Bundle();
        bundle.putString(WebViewActivity.KEY_OFFLINE_URL, offlineUrl);
        bundle.putString(WebViewActivity.KEY_WEB_URL, url);
        return bundle;
    }

    @BindView(R.id.webView)
    protected WebView webView;

    @BindView(R.id.pdfView)
    protected PDFView pdfView;


    public WebView getWebView() {
        return webView;
    }

    private String offlineUrl;
    private String url;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.web_view_fragment, container, false);
        ButterKnife.bind(this, view);
        Bundle args = getArguments();
        if (args != null) {
            offlineUrl = args.getString(WebViewActivity.KEY_OFFLINE_URL);
            url = args.getString(WebViewActivity.KEY_WEB_URL);
            getUrlOrHtmlAndLoad();
        }
        return view;
    }

    private void getUrlOrHtmlAndLoad() {
        String finalUrl = url;
        String extension = finalUrl.substring(finalUrl.lastIndexOf("."), finalUrl.length());
        if (extension.toLowerCase(Locale.getDefault()).equals(".pdf")) {
            if ( !isNetworkAvailable() && offlineUrl != null) {
                finalUrl =  offlineUrl;
            }
            setupPDFReaderAndLoadUrl(finalUrl);
        } else {
            if ( !isNetworkAvailable() && offlineUrl != null) {
                webView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK );
                finalUrl = "file://" + offlineUrl;
            }
            setupWebViewAndLoadUrl(finalUrl);
        }
    }

    private void setupPDFReaderAndLoadUrl(String url) {
        pdfView.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);

        getApp().getJobManager(App.JobQueueName.Loading).addJobInBackground(new DownloadPDFJob(url));

//        pdfReaderView = SimpleReaderFactory.createSimpleViewer(getActivity(), null);
//        if (isNetworkAvailable()) {
//            pdfReaderView.openUrl(url, "");
//        } else {
//            pdfReaderView.openFile(url, "");
//        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DownloadPDFJob.DownloadFileComplete response) {
        pdfView.fromFile(response.getDownloadedFile()).load();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebViewAndLoadUrl(String url) {
        pdfView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);

        WebSettings webSettings = webView.getSettings();
        webSettings.setAppCachePath( getApp().getCacheDir().getAbsolutePath() );
        webSettings.setAllowFileAccess( true );
        webSettings.setAppCacheEnabled( true );
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode( WebSettings.LOAD_DEFAULT );
        webView.setWebViewClient(new IBAWebViewClient());
        webView.loadUrl(url);
        if (url.contains("aspx")) {
            webView.setDownloadListener(new DownloadListener() {
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
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
    }
}
