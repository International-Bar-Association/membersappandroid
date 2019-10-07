package com.ibamembers.conference.chat

import android.content.SearchRecentSuggestionsProvider

class MySuggestionProvider : SearchRecentSuggestionsProvider() {

    companion object {
        const val AUTHORITY = "com.ibamembers.conference.chat.MySuggestionProvider"
        const val MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }

    init {
        //String AUTHORITY = getContext().getString(R.string.file_provider_authorities);
        setupSuggestions(AUTHORITY, MODE)
    }


}
