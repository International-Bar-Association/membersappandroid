package com.ibamembers.search.favourites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.search.ProfileSnippetFragment;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouritesFragment extends ProfileSnippetFragment {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.favourites_recycler)
    protected RecyclerView favouritesRecycler;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.favourites_no_profiles)
    protected TextView noProfiles;

    private boolean isLandscape;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.favourites_fragment, container, false);
        ButterKnife.bind(this, view);
        loadMore = false;

        setRetainInstance(true);

        View layoutIsLandscape = view.findViewById(R.id.favourites_layout_is_landscape);
        isLandscape = layoutIsLandscape != null;


        List<ProfileSnippet> profileSnippets = getSnippetsIfPresent();
        setUpRecycler(profileSnippets, FavouriteHandlerFragment.TAG_FAVOURITES_JOB, false);

        if (profileSnippets.size() <= 0) {
            updateProfileSnippets();
        }

        return view;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (isLandscape) {
            getProfileSnippetAdapter().selectFirstItemIfNoneSelected();
        }
    }

    private void updateProfileSnippets() {
        App app = getApp();
        if (app != null) {
            try {
                List<ProfileSnippet> profileSnippets = app.getDatabaseHelper().getProfileSnippetDao().queryForAll();

                Collections.sort(profileSnippets, new Comparator<ProfileSnippet>() {
                    @Override
                    public int compare(ProfileSnippet lhs, ProfileSnippet rhs) {
                        String lhstring = getApp().getDataManager().getFullName(lhs.getFirstName(), lhs.getLastName());
                        String rhstring = getApp().getDataManager().getFullName(rhs.getFirstName(), rhs.getLastName());

                        return lhstring.compareToIgnoreCase(rhstring);
                    }
                });
                setProfileSnippets(profileSnippets);
                updateNoFavouritesText(profileSnippets);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void shouldRefreshFavourites(float userId, boolean wasRemoved) throws SQLException {
        if (userId == -1) {
            updateProfileSnippets();
        } else {
            ProfileSnippetAdapter profileSnippetAdapter = getProfileSnippetAdapter();
            List<ProfileSnippet> profileSnippets = getSnippetsIfPresent();

            if (wasRemoved) {
                int selectedSnippetIndex = -1;

                for (int i = 0; i < profileSnippets.size(); i++) {
                    if (profileSnippets.get(i).getId() == userId) {
                        selectedSnippetIndex = i;
                    }
                }

                if (selectedSnippetIndex != -1) {
                    profileSnippets.remove(selectedSnippetIndex);
                    profileSnippetAdapter.notifyItemRemoved(selectedSnippetIndex);

                    int newSnippetIndex = -1;

                    if (profileSnippetAdapter.getProfileSnippets().size() > 0) {
                        if (selectedSnippetIndex > 0) {
                            newSnippetIndex = selectedSnippetIndex - 1;
                        } else {
                            newSnippetIndex = selectedSnippetIndex;
                        }
                    }

                    profileSnippetAdapter.newSnippetClicked(newSnippetIndex);
                }
            } else {
                updateProfileSnippets();

                if (profileSnippets.size() == 1) {
                    profileSnippetAdapter.newSnippetClicked(0);
                }
            }

            updateNoFavouritesText(profileSnippets);
        }
    }

    private void updateNoFavouritesText(List<ProfileSnippet> profileSnippets) {
        if (profileSnippets.size() > 0) {
            noProfiles.setVisibility(View.INVISIBLE);
        } else {
            noProfiles.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return favouritesRecycler;
    }
}
