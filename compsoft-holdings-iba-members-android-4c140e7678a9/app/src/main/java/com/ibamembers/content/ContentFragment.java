package com.ibamembers.content;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ibamembers.app.App;
import com.ibamembers.content.job.ContentModel;
import com.ibamembers.content.job.GetContentLibraryJob;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class ContentFragment extends ContentBaseFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApp().sendScreenViewAnalytics(this.getClass().toString());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    void setupSwipeRefreshLayout() {
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDownloadsOrContent();
            }
        });
    }

    public void loadDownloadsOrContent() {
        if (contentTabsState == ContentTabsState.Contents) {
            loadContent();
        } else {
            loadDownloadsAndDisplayContent();
        }
    }

    @Override
    ContentAdapter getContentAdapter(List<ContentModel> contentModelList) {
        return new ContentAdapter(contentModelList, true);
    }

    @Override
    void loadContent() {
        App app = getApp();
        if (app != null) {
            app.getJobManager(App.JobQueueName.Network).addJobInBackground(new GetContentLibraryJob());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContentDetailFragment.DownloadDeleted response) {
        currentSelectedIndex--;
        loadDownloadsOrContent();
    }
}
