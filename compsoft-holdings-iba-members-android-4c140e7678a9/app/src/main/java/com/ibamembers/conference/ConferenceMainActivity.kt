package com.ibamembers.conference

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ibamembers.R
import com.ibamembers.app.EventBusFragment
import com.ibamembers.app.SettingDao
import com.ibamembers.app.gcm.MyFcmMessagingService
import com.ibamembers.conference.chat.ConferenceChatHistoryActivity
import com.ibamembers.conference.event.ConferenceEventActivity
import com.ibamembers.conference.website.ConferenceWebActivity
import kotlinx.android.synthetic.main.conference_main_button_layout.*
import kotlinx.android.synthetic.main.conference_main_fragment.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ConferenceMainActivity : ConferenceBaseActivity() {

    companion object {
        private const val KEY_URL = "KEY_URL"

        fun getConferenceMainActivityIntent(context: Context, url: String): Intent {
            val intent = Intent(context, ConferenceMainActivity::class.java)
            intent.putExtra(KEY_URL, url)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app.sendScreenViewAnalytics(this.javaClass.toString())
        setDisplayHomeAsUpEnabled(true)
        getOrAddOnlyFragment(ConferenceMainFragment::class.java)
    }

    override fun getLayoutType(): LayoutType {
        return LayoutType.NO_SCROLLVIEW_NO_TOOLBAR
    }
}

class ConferenceMainFragment : EventBusFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        loadConferenceEventFromLink()
        return  inflater.inflate(R.layout.conference_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNotificationBadge()
        setupViews()
    }

    private fun setupViews() {
        schedule_layout.setOnClickListener { onScheduleClicked() }
        fab_chat.setOnClickListener { onChatClicked() }
        website_layout.setOnClickListener { onWebsiteClicked() }
        fab_close.setOnClickListener { onCloseClicked() }
    }

    private fun loadConferenceEventFromLink() {
        app?.let {
            val eventId = app.loadedEventId
            val messageId = app.p2PMessageId
            if (eventId != 0) {
                onScheduleClicked()
            } else if (messageId != 0) {
                onChatClicked()
            }
        }
    }

    private fun setNotificationBadge() {
        app?.let {
            val sharedPrefs = app.sharedPreferences
            val notificationCount = sharedPrefs.getInt(MyFcmMessagingService.KEY_NOTIFICATION_COUNT, 0)

            if (notificationCount > 0) {
                conference_badge!!.visibility = View.VISIBLE
                conference_badge!!.text = notificationCount.toString()
            } else {
                conference_badge!!.visibility = View.INVISIBLE
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: MyFcmMessagingService.NotificationEvent) {
        app?.let { setNotificationBadge() }
    }

    fun onScheduleClicked() {
        startActivity(Intent(activity, ConferenceEventActivity::class.java))
    }

    fun onChatClicked() {
        val canChat = SettingDao.isClassAllowedToSearch(app)
        if (canChat) {
            startActivity(Intent(activity, ConferenceChatHistoryActivity::class.java))
        } else {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(getString(R.string.profile_upgrade_membership_title))
                    .setMessage(getString(R.string.profile_upgrade_membership_message))
                    .setPositiveButton(getString(R.string.profile_upgrade_membership_button)) { dialog, id ->
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.iba_contact_email), null))
                        startActivity(Intent.createChooser(intent, "Send Email"))
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()
        }

    }

    fun onWebsiteClicked() {
        startActivity(Intent(activity, ConferenceWebActivity::class.java))
    }

    fun onCloseClicked() {
        activity!!.finish()
    }
}
