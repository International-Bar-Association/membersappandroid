package com.ibamembers.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ibamembers.R;
import com.ibamembers.app.MainBaseActivity;

public class DataPickerActivity extends MainBaseActivity implements DataPickerFragment.DataPickerFragmentListener {

    private static final String KEY_DATA_TYPE = "KEY_DATA_TYPE";
    private static final String KEY_IS_CONFERENCE = "KEY_IS_CONFERENCE";

    public static Intent getDataPickerActivityIntent(Context context, DataPickerFragment.DataType dataType, boolean isConference) {
        Intent intent = new Intent(context, DataPickerActivity.class);
        intent.putExtra(KEY_DATA_TYPE, dataType.ordinal());
        intent.putExtra(KEY_IS_CONFERENCE, isConference);
        return intent;
    }

    private DataPickerFragment dataPickerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDisplayHomeAsUpEnabled(true);

        boolean isConference = getIntent().getBooleanExtra(KEY_IS_CONFERENCE, false);
        if (isConference) {
            baseToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.conference_theme_primary));
            setConferenceStatusBarColor();
        }

        DataPickerFragment.DataType dataType = DataPickerFragment.DataType.forInt(getIntent().getIntExtra(KEY_DATA_TYPE, -1));
        dataPickerFragment = getOrAddOnlyFragment(DataPickerFragment.class, DataPickerFragment.getDataPickerFragmentArguments(dataType));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.data_picker_menu, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        if (searchView != null) {
            searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (dataPickerFragment != null) {
                        dataPickerFragment.setSearchTerm(newText);
                    }
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public void newDataItemSelected() {
        setResult(RESULT_OK);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}
