package com.ibamembers.conference.chat

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SimpleCursorAdapter
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView

import com.ibamembers.R
import com.ibamembers.app.App
import com.ibamembers.app.BaseActivity
import com.ibamembers.main.MainActivity
import com.ibamembers.profile.UserProfileFragment
import com.ibamembers.profile.message.ProfileMessageActivity
import com.ibamembers.search.DataPickerActivity
import com.ibamembers.search.DataPickerFragment
import com.ibamembers.search.ProfileSnippetFragment
import com.ibamembers.search.SearchFilterFragment
import com.ibamembers.search.SearchHandlerFragment
import com.ibamembers.search.SearchProfileFragment
import com.ibamembers.search.SearchResultFragment
import com.ibamembers.search.favourites.ProfileSnippet

import java.sql.SQLException

import kotlinx.android.synthetic.main.base_activity_search.*
import kotlinx.android.synthetic.main.search_toolbar.*

class ConferenceSearchContactActivity : BaseActivity(),
        SearchView.OnSuggestionListener,
        SearchFilterFragment.SearchFilterFragmentListener,
        ProfileSnippetFragment.ProfileSnippetFragmentListener,
        SearchResultFragment.SearchResultFragmentListener,
        UserProfileFragment.UserProfileFragmentListener,
        SearchProfileFragment.SearchProfileFragmentListener {


    private var searchHandlerFragment: SearchHandlerFragment? = null

    private var shouldRefreshCommittee: Boolean = false
    private var shouldRefreshAreaOfPractices: Boolean = false
    private var shouldRefreshCountries: Boolean = false
    private val shouldRefreshConference: Boolean = false

    private val toolbarWidth: Int = 0
    private val toolbarHeight: Int = 0
    private val searchMenu: Menu? = null
    private val searchMenuItem: MenuItem? = null
    private val mSearchViewAdapter: SearchFeedResultsAdaptor? = null
    private val txtSearch: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_activity_search)
        app.sendScreenViewAnalytics(this.javaClass.toString())

        setupToolbar()
        setupViews()

        searchHandlerFragment = getOrAddOnlyFragment(SearchHandlerFragment::class.java, SearchHandlerFragment.getSearchHandlerFragmentArguments(true))

        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (searchHandlerFragment != null) {
            searchHandlerFragment!!.setShouldRefreshBooleans(shouldRefreshCommittee, shouldRefreshAreaOfPractices, shouldRefreshCountries, shouldRefreshConference)
        }
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            Log.i("SearchView", "Query: $query")
        }
    }

    protected fun setupToolbar() {
        setConferenceStatusBarColor()
        toolbar!!.setBackgroundColor(ContextCompat.getColor(this, R.color.conference_theme_primary))
        setSupportActionBar(toolbar)

        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }

        search_fab!!.visibility = View.VISIBLE
        search_filter_fab?.visibility = View.VISIBLE
        search_filter_fab?.hide()
    }

    private fun setupViews() {
        search_fab.setOnClickListener {
            val app = application as App
            if (app != null) {
                val connectionManager = app.connectionManager
                if (connectionManager.canConnectToInternet(this)) {

                    try {
                        val settingDao = app.databaseHelper.settingDao
                        settingDao.searchConference = true
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    }

                    search_fab?.hide()
                    search_filter_fab?.show()
                    dismissKeyboardIfUp()
                    searchHandlerFragment!!.clearContentFragment()
                    if (searchHandlerFragment != null) {
                        searchHandlerFragment!!.switchToSearchResults(true)
                    }
                } else {
                    //TODO
                    //showCantConnectToInternetDialog(DialogManager.UserInteraction.SEARCHING);
                }
            }
        }

        search_filter_fab.setOnClickListener {
            search_filter_fab?.hide()
            search_fab?.show()

            if (searchHandlerFragment != null) {
                searchHandlerFragment!!.cancelSearchJob()
                searchHandlerFragment!!.switchToSearchFilter(true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        } else if (item.itemId == R.id.action_search) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            } else
                searchtoolbar?.visibility = View.VISIBLE//circleReveal(1, true, true);

            searchMenuItem!!.expandActionView()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSuggestionSelect(position: Int): Boolean {
        return false
    }

    override fun onSuggestionClick(position: Int): Boolean {
        return false
    }

    override fun loadDataPickerView(dataType: DataPickerFragment.DataType) {
        if (searchHandlerFragment != null) {
            if (!searchHandlerFragment!!.getDataPickerValues(dataType)) {
                when (dataType) {
                    DataPickerFragment.DataType.COMMITTEES -> {
                        startActivityForResult(DataPickerActivity.getDataPickerActivityIntent(this, DataPickerFragment.DataType.COMMITTEES, true), MainActivity.COMMITTEE_REQUEST_CODE)
                    }
                    DataPickerFragment.DataType.AREA_OF_PRACTICES -> {
                        startActivityForResult(DataPickerActivity.getDataPickerActivityIntent(this, DataPickerFragment.DataType.AREA_OF_PRACTICES, true), MainActivity.AREA_OF_PRACTICE_REQUEST_CODE)
                    }
                    DataPickerFragment.DataType.COUNTRIES -> {
                        startActivityForResult(DataPickerActivity.getDataPickerActivityIntent(this, DataPickerFragment.DataType.COUNTRIES, true), MainActivity.COUNTRY_REQUEST_CODE)
                    }
                }
            } else if (searchMenuItem != null) {
                //				searchMenuItem.setVisible(true);
                //				resetSearchView();
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == MainActivity.COMMITTEE_REQUEST_CODE) {
                shouldRefreshCommittee = true
            } else if (requestCode == MainActivity.AREA_OF_PRACTICE_REQUEST_CODE) {
                shouldRefreshAreaOfPractices = true
            } else if (requestCode == MainActivity.COUNTRY_REQUEST_CODE) {
                shouldRefreshCountries = true
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun setSearchButtonVisibility(isVisible: Boolean) {

    }

    override fun dismissKeyboardIfUp() {

    }

    override fun favouritesClicked() {

    }


    /**
     * ProfileSnippetFragmentListener
     */
    override fun profileSnippetClicked(profileSnippet: ProfileSnippet?, jobTag: String) {
        if (searchHandlerFragment != null) {
            if (profileSnippet != null) { // if null favourites fragment should clear content view
                var handled = false
                if (jobTag == SearchHandlerFragment.TAG_SEARCH_JOB) {
                    handled = searchHandlerFragment!!.profileSnippetClicked(profileSnippet)
                }

                if (!handled) {
                    val app = application as App
                    if (app != null) {
                        startActivity(ProfileMessageActivity.getProfileMessageActivity(this, profileSnippet.id, profileSnippet.jobPosition, true, true))
                    }
                }
            }

            //storedProfileSnippet = null;
            //storedJobTag = null;
            //profileSnippetWaitingToBeClicked = false;

            if (searchMenuItem != null) {
                searchMenuItem.isVisible = false
            }

        } else {
            //profileSnippetWaitingToBeClicked = true;
            //storedProfileSnippet = profileSnippet;
            //storedJobTag = jobTag;
        }
    }

    override fun profileHeaderSnippetClicked() {

    }

    /**
     * SearchResultFragmentListener
     */
    override fun finishedSearching() {

    }

    override fun editBio() {

    }

    override fun updateConferenceButton() {

    }

    /**
     * SearchProfileFragmentListener
     */

    override fun leaveProfileFragment() {

    }

    override fun favouriteStatusChanged(userId: Float, wasRemoved: Boolean) {

    }

    override fun searchProfileFinishedLoading(tag: String) {

    }

    override fun changeToolbarTitle(title: String, jobTag: String) {

    }

    class SearchFeedResultsAdaptor(private val context: Context, layout: Int, c: Cursor, from: Array<String>, to: IntArray, flags: Int) : SimpleCursorAdapter(context, layout, c, from, to, flags) {

        override fun bindView(view: View, context: Context?, cursor: Cursor) {
            val nameText = view.findViewById<TextView>(R.id.conference_chat_item_name)
            val postText = view.findViewById<TextView>(R.id.conference_chat_item_rank)

            nameText.text = cursor.getString(1)
            postText.text = cursor.getString(2)
        }

        companion object {
            private val tag = SearchFeedResultsAdaptor::class.java.name
        }
    }

    companion object {

        val KEY_URL = "KEY_URL"

        fun getConferenceSearchUsersActivityIntent(context: Context): Intent {
            return Intent(context, ConferenceSearchContactActivity::class.java)
        }

        var columns = arrayOf("_id", "NAME", "POS")
    }

    // You must implements your logic to get data using OrmLite
    //	private void populateAdapter(String query) {
    //		final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, "cityName" });
    //		for (int i=0; i<SUGGESTIONS.length; i++) {
    //			if (SUGGESTIONS[i].toLowerCase().startsWith(query.toLowerCase()))
    //				c.addRow(new Object[] {i, SUGGESTIONS[i]});
    //		}
    //		mSearchViewAdapter.changeCursor(c);
    //	}


}
