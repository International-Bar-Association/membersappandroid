package com.ibamembers.conference

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar

import com.ibamembers.R
import com.ibamembers.app.MainBaseActivity

open class ConferenceBaseActivity : MainBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDisplayHomeAsUpEnabled(true)
        setConferenceStatusBarColor()
    }


    protected fun setupToolbar(toolbar: Toolbar) {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.conference_theme_primary))
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
}