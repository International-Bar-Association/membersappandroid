package com.ibamembers.profile;

import android.os.Bundle;
import android.view.MenuItem;

import com.ibamembers.app.MainBaseActivity;

public class EditBioActivity extends MainBaseActivity implements EditBioFragment.EditBioListener{

    private EditBioFragment editBioFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDisplayHomeAsUpEnabled(true);
        editBioFragment = getOrAddOnlyFragment(EditBioFragment.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            hideKeyboard();
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (editBioFragment != null) {
            if (!editBioFragment.getIsUpdatingData()) {
                editBioFragment.saveProfileData();
            }
        }
    }

    @Override
    public void userFinishedEditing() {
        setResult(RESULT_OK);
        finish();
    }
}
