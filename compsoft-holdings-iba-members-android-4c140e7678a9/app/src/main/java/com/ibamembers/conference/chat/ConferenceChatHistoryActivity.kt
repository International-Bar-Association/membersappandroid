package com.ibamembers.conference.chat

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem

import com.ibamembers.R
import com.ibamembers.app.BaseActivity
import com.ibamembers.messages.MessageAdapter
import com.ibamembers.messages.MessagesFragment
import com.ibamembers.messages.job.GeneralMessageModel
import com.ibamembers.profile.message.ProfileMessageActivity
import kotlinx.android.synthetic.main.base_activity_search.*

class ConferenceChatHistoryActivity : BaseActivity(), MessagesFragment.MessageFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_activity_search)
        app?.sendScreenViewAnalytics(this.javaClass.toString())

        setupToolbar()
        getOrAddOnlyFragment(MessagesFragment::class.java, MessagesFragment.getMessageFragmentArgs(MessageAdapter.P2PType.Conference))
    }

    private fun setupToolbar() {
        setConferenceStatusBarColor()
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.conference_theme_primary))
        setSupportActionBar(toolbar)

        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.conference_chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_search -> {
                startActivity(ConferenceSearchContactActivity.getConferenceSearchUsersActivityIntent(this))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun messageClicked(message: GeneralMessageModel) {
        startActivity(ProfileMessageActivity.getProfileMessageActivity(this, message.appUserMessageId, message.title, true, true))
    }

    override fun handleNewMessage(message: GeneralMessageModel) {}

    override fun deleteMessageDetailInLandscape() {}

    override fun getNewMessageIdAndResetId(): Int {
        return 0
    }


}
