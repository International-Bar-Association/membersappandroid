package com.ibamembers.content;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.EventBusFragment;
import com.ibamembers.content.job.ContentModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;

public class ContentHandlerFragment extends EventBusFragment{

    private static final String TAG_CONTENT = "TAG_SEARCH_JOB";
    private static final String TAG_CONTENT_DETAIL = "TAG_CONTENT_DETAIL";
    public static final String KEY_CONTENT = "KEY_CONTENT";

    private ContentFragment contentFragment;
    private ContentDetailFragment contentDetailFragment;
    private boolean isLandscape;
    public boolean canDownload;
    public boolean isDownloaded;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_multipane_fragment, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        FrameLayout contentFrame = (FrameLayout) view.findViewById(R.id.content_content_fragment);
        isLandscape = contentFrame != null;

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.content_detail_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem downloadItem = menu.findItem(R.id.action_download);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);

        if (!canDownload) {
            downloadItem.setVisible(false);
            deleteItem.setVisible(false);
        }else {
            downloadItem.setVisible(!isDownloaded);
            deleteItem.setVisible(isDownloaded);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (contentDetailFragment != null) {
            if (item.getItemId() == R.id.action_download) {
                contentDetailFragment.downloadAndSaveContent();
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                contentDetailFragment.deleteContent();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            contentFragment = (ContentFragment) getFragmentManager().getFragment(savedInstanceState, TAG_CONTENT);
            Fragment fragment = getFragmentManager().findFragmentById(R.id.content_content_fragment);
            if (fragment instanceof ContentDetailFragment) {
                contentDetailFragment = (ContentDetailFragment) fragment;
            }
        }
        loadFragments(savedInstanceState != null ? savedInstanceState.getInt(KEY_CONTENT) : -1);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTENT, contentFragment.getCurrentSelectedIndex());
        getFragmentManager().putFragment(outState, TAG_CONTENT, contentFragment);
    }

    private void loadFragments(int getCurrentSelectedIndex) {
        contentFragment = new ContentFragment();
        if (getCurrentSelectedIndex != -1) {
            contentFragment.setCurrentSelectedIndex(getCurrentSelectedIndex);
        }
        loadFragmentIntoContainer(contentFragment, R.id.content_main_fragment, false, TAG_CONTENT);
    }

    public boolean handleContentClickedInLandscape(ContentModel content) {
        if (isLandscape) {
            App app = getApp();
            if (app != null) {
                if (shouldLoadSnippet(content)) {
                    contentDetailFragment = new ContentDetailFragment();
                    contentDetailFragment.setHasOptionsMenu(false);
                    contentDetailFragment.setArguments(ContentDetailFragment.getContentDetailFragmentArgs(new Gson().toJson(content), false));
                    loadFragmentIntoContainer(contentDetailFragment, R.id.content_content_fragment, false, TAG_CONTENT_DETAIL);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldLoadSnippet(ContentModel contentModel) {
        if (contentDetailFragment == null || contentDetailFragment.getCurrentContentId() != contentModel.getId() || !contentDetailFragment.isInLayout()) {
            return true;
        }
        return false;
    }

    private void loadFragmentIntoContainer(Fragment fragment, @IdRes int containerId, boolean isSearchFilter, @Nullable String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerId, fragment, tag);
        fragmentTransaction.commit();
    }

    public void setIsAndCanDownloadAndInvalidate(boolean canDownload, boolean isDownloaded) {
        this.canDownload = canDownload;
        this.isDownloaded = isDownloaded;
        getActivity().invalidateOptionsMenu();
    }

    /**
     * Invalidates the options menu. The ContentFragment should also have this even thread to decrement
     * the selected index and reload the content list.
     * @param response DownloadDeleted response
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContentDetailFragment.DownloadDeleted response) {
        setIsAndCanDownloadAndInvalidate(response.isCanDownload(), response.isDownloaded());
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContentBaseFragment.ClearDetailFragment response) {
        clearContentDetailFragment();
    }

    public void clearContentDetailFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_content_fragment);
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }
    }
}
