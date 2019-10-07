package com.ibamembers.conference.website

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView

import com.ibamembers.R
import com.ibamembers.app.App
import com.ibamembers.app.BaseActivity
import com.ibamembers.app.BaseFragment
import com.ibamembers.app.SettingDao
import com.ibamembers.conference.ConferenceBaseActivity

import java.sql.SQLException
import java.util.Locale

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import kotlinx.android.synthetic.main.conference_web_fragment.*


class ConferenceWebActivity : ConferenceBaseActivity() {

    companion object {
        const val KEY_SAVED_STATE = "KEY_SAVED_STATE"

        fun getConferenceActivityIntent(context: Context, savedState: Bundle): Intent {
            val intent = Intent(context, ConferenceWebActivity::class.java)
            intent.putExtra(KEY_SAVED_STATE, savedState)
            return intent
        }
    }

    private var conferenceWebFragment: ConferenceWebFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDisplayHomeAsUpEnabled(true)
        intent?.let {
            val savedState = it.getBundleExtra(KEY_SAVED_STATE)
            conferenceWebFragment = getOrAddOnlyFragment(ConferenceWebFragment::class.java, ConferenceWebFragment.getConferenceFragmentArgs(savedState))
        }
    }

    override fun getLayoutType(): LayoutType {
        return LayoutType.NO_SCROLLVIEW_NO_TOOLBAR
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        conferenceWebFragment?.saveWebViewState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        conferenceWebFragment?.restoreWebViewState(savedInstanceState)
    }

    override fun onBackPressed() {
        if (conferenceWebFragment?.handleBackPressed() != true) super.onBackPressed()
    }

}

class ConferenceWebFragment : BaseFragment() {

    companion object {
        fun getConferenceFragmentArgs(savedState: Bundle?): Bundle {
            val bundle = Bundle()
            bundle.putBundle(ConferenceWebActivity.KEY_SAVED_STATE, savedState)
            return bundle
        }
    }

    fun saveWebViewState(outState: Bundle){
        conference_webview?.saveState(outState)
    }

    fun restoreWebViewState(savedInstanceState: Bundle){
        conference_webview?.restoreState(savedInstanceState)
    }

    fun handleBackPressed(): Boolean{
        if (conference_webview?.canGoBack() == true) {
            conference_webview?.goBack()
            return true
        }
        return false
    }

    private var currentUrl: String? = null
    private var savedState: Bundle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.conference_web_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            savedState = it.getBundle(ConferenceWebActivity.KEY_SAVED_STATE)
            setupWebView()
        }

        getConferenceUrl()

        if (savedInstanceState != null) {
            savedState = savedInstanceState
        }

        if (savedState != null)
            conference_webview!!.restoreState(savedState)
        else {
            conference_webview!!.loadUrl(currentUrl)
        }
    }

    private fun getConferenceUrl() {
        try {
            val settingDao = app!!.databaseHelper.settingDao
            currentUrl = settingDao.conferenceUrl
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webSettings = conference_webview!!.settings
        webSettings.setAppCachePath(app!!.cacheDir.absolutePath)
        webSettings.allowFileAccess = true
        webSettings.setAppCacheEnabled(true)
        webSettings.javaScriptEnabled = true

        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        conference_webview!!.webViewClient = IBAWebViewClient()
        conference_webview!!.isVerticalScrollBarEnabled = true

        front_webview.setOnClickListener {
            if (conference_webview.canGoForward()) {
                conference_webview.goForward()
            }
        }

        refresh_webview.setOnClickListener {
            conference_webview.reload()
        }
    }

    private inner class IBAWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            view.loadUrl(request.url.toString())
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            currentUrl = url
            front_webview!!.setImageResource(if (conference_webview.canGoForward()) R.drawable.webview_front_arrow else R.drawable.webview_front_arrow_disable)
            loadJavascriptInSignUp(url)
        }
    }

    private fun loadJavascriptInSignUp(url: String) {
        var userName: String? = null
        var password: String? = null
        app?.let {
            try {
                val settingDao = app.databaseHelper.settingDao
                userName = settingDao.username
                password = settingDao.password
            } catch (e: SQLException) {
                e.printStackTrace()
            }

        }
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            val javascript = "javascript: document.getElementsByName('userName')[0].value = '" + userName + "';\n" +
                    "document.getElementsByName('password')[0].value = '" + password + "';\n" +
                    "document.getElementsByName('submit')[0].click();\n"

            if (url.toLowerCase(Locale.getDefault()).contains("signin.cfm")) {
                conference_webview.loadUrl(javascript)
            }
        }
    }

}
