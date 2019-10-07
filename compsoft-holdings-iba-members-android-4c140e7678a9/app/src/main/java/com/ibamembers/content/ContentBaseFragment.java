package com.ibamembers.content;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.EventBusFragment;
import com.ibamembers.app.IBAUtils;
import com.ibamembers.content.db.ContentDownload;
import com.ibamembers.content.db.ContentDownloadDao;
import com.ibamembers.content.job.ContentModel;
import com.ibamembers.content.job.GetContentLibraryJob;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class ContentBaseFragment extends EventBusFragment {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.content_recycler)
    protected RecyclerView contentRecycler;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.content_no_content)
    protected TextView noContentText;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.content_swipe_refresh)
    protected SwipeRefreshLayout swipeRefresh;

    private ContentFragmentListener contentFragmentListener;
    protected ContentAdapter contentAdapter;
    private SimpleDateFormat apiDateFormat;
    private LinearLayoutManager linearLayoutManager;
    private int contentIdSelected;
    protected int currentSelectedIndex;
    protected boolean loadContentOnResume;
    private boolean isLandscape;
    protected ContentTabsState contentTabsState;

    public int getCurrentSelectedIndex() {
        return currentSelectedIndex;
    }

    public void setCurrentSelectedIndex(int currentSelectedIndex) {
        this.currentSelectedIndex = currentSelectedIndex;
    }

    public void setLoadContentOnResume(boolean loadContentOnResume) {
        this.loadContentOnResume = loadContentOnResume;
    }

    public enum ContentLibraryType {
        Article, Film, Podcast;

        public static ContentLibraryType forInt(int x) {
            switch (x) {
                case 0:
                    return Article;
                case 1:
                    return Film;
                case 2:
                    return Podcast;
                default:
                    return null;
            }
        }
    }

    public enum ContentTabsState {
        Contents,
        MyDownloads
    }

    abstract ContentAdapter getContentAdapter(List<ContentModel> contentModelList);
    abstract void loadContent();
    abstract void setupSwipeRefreshLayout();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.content_fragment, container, false);
        ButterKnife.bind(this, view);

        View layoutIsLandscape = view.findViewById(R.id.content_is_landscape);
        isLandscape = layoutIsLandscape != null;
        contentTabsState = ContentTabsState.Contents;

        apiDateFormat = new SimpleDateFormat(getString(R.string.default_api_date_format), Locale.getDefault());
        setUpRecycler(null);
        setupSwipeRefreshLayout();
        loadContent();
        return view;
    }

    protected void setUpRecycler(List<ContentModel> contentModelList) {
        Activity activity = getActivity();
        if (activity != null) {
            if (contentAdapter == null) {
                if (contentModelList == null) {
                    contentModelList = new ArrayList<>();
                }
                contentAdapter = getContentAdapter(contentModelList);
            }
            contentRecycler.setHasFixedSize(true);

            if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && isTablet(getActivity())){
                contentRecycler.setLayoutManager(new StaggeredGridLayoutManager(2, Configuration.ORIENTATION_PORTRAIT));
            } else {
                linearLayoutManager = new LinearLayoutManager(activity);
                contentRecycler.setLayoutManager(linearLayoutManager);
            }
            contentRecycler.setAdapter(contentAdapter);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GetContentLibraryJob.Success response) {
        if (swipeRefresh.isRefreshing()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefresh.setRefreshing(false);
                }
            }, 1000);
        }

        List<ContentModel> messageModelList = response.getContentlibaryModel().getContentList();
        List<ContentModel> downloadsListFromDb = loadDownloadsFromDB();

        //For every item in response iterate against local db and set the fileDir if that item exists
        for (ContentModel downloads : downloadsListFromDb) {
            for (ContentModel content : messageModelList) {
                if (downloads.getId() == content.getId()) {
                    String fileDir = downloads.getFileDir();
                    if (!TextUtils.isEmpty(fileDir)) {
                        content.setFileDir(fileDir);
                    }
                    break;
                }
            }
        }

        loadContentModelToAdapter(messageModelList);
    }

    protected void loadContentModelToAdapter(List<ContentModel> contentModelList) {
        if (contentModelList.size() > 0) {
            noContentText.setVisibility(View.INVISIBLE);
            contentAdapter.setContentList(contentModelList);

            if (isLandscape) {
                ContentModel newSelectedContent = contentAdapter.getContentModelList().get(currentSelectedIndex);
                setCurrentContent(newSelectedContent);
                linearLayoutManager.scrollToPosition(contentAdapter.isContainHeaderRow() ? currentSelectedIndex + 1 : currentSelectedIndex);
            }
        } else {
            noContentText.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GetContentLibraryJob.Failed response) {
        if (swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
        Log.e("ContentBaseFragment" , "Failed to get content library");
    }

    protected void loadDownloadsAndDisplayContent() {
        List<ContentModel> contentModelList = loadDownloadsFromDB();

        contentAdapter.setContentList(contentModelList);
        noContentText.setVisibility(contentModelList.size() > 0 ? View.INVISIBLE : View.VISIBLE);

        int downloadListSize = contentAdapter.getContentModelList().size();
        if (isLandscape) {
            if (downloadListSize > 0) {
                ContentModel newSelectedContent = contentAdapter.getContentModelList().get(currentSelectedIndex);
                setCurrentContent(newSelectedContent);
                linearLayoutManager.scrollToPosition(contentAdapter.isContainHeaderRow() ? currentSelectedIndex + 1 : currentSelectedIndex);
            } else {
                App app = getApp();
                if (app != null) {
                    app.getEventBus().post(new ClearDetailFragment());
                }
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(false);
            }
        }, 800);
    }

    public List<ContentModel> loadDownloadsFromDB() {
        App app = getApp();
        if (app != null) {
            try {
                ContentDownloadDao contentDownloadDao = app.getDatabaseHelper().getContentDownloadDao();
                List<ContentDownload> contentDownloadList = contentDownloadDao.queryForAll();
                List<ContentModel> contentModelList = new ArrayList<>(contentDownloadList.size());

                for (ContentDownload contentDownload : contentDownloadList) {
                    contentModelList.add(ContentDownloadDao.convertContentDownloadToContentModel(contentDownload));
                }

                return contentModelList;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    public class ContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_MY_DOWNLOADS = 0;
        private static final int VIEW_FEATURED = 1;
        private static final int VIEW_NON_FEATURED = 2;

        private List<ContentModel> contentModelList;
        private boolean isContainHeaderRow;

        public List<ContentModel> getContentModelList() {
            return contentModelList;
        }

        public boolean isContainHeaderRow() {
            return isContainHeaderRow;
        }

        public ContentAdapter(List<ContentModel> contentModelList, boolean isContainHeaderRow) {
            this.contentModelList = contentModelList;
            this.isContainHeaderRow = isContainHeaderRow;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_MY_DOWNLOADS) {
                return new DownloadViewHolder(inflater, parent);
            } else if (viewType == VIEW_FEATURED) {
                return new FeaturedViewHolder(inflater, parent);
            } else if (viewType == VIEW_NON_FEATURED) {
                return new NonFeaturedViewHolder(inflater, parent);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (isContainHeaderRow && position == 0 && !isLandscape && isTablet(getActivity())) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
            } else if (isContainHeaderRow && position != 0) {
                if (isArticleFeatured(position)) {
                    ((FeaturedViewHolder) holder).fillView(contentModelList.get(position - 1));
                } else {
                    ((NonFeaturedViewHolder) holder).fillView(contentModelList.get(position - 1));
                }
            } else if (!isContainHeaderRow) {
                if (isArticleFeatured(position)) {
                    ((FeaturedViewHolder) holder).fillView(contentModelList.get(position));
                } else {
                    ((NonFeaturedViewHolder) holder).fillView(contentModelList.get(position));
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isContainHeaderRow && position == 0) {
                return VIEW_MY_DOWNLOADS;
            } else if (isArticleFeatured(position)) {
                return VIEW_FEATURED;
            } else {
                return VIEW_NON_FEATURED;
            }
        }

        @Override
        public int getItemCount() {
            return isContainHeaderRow ? contentModelList.size() + 1 : contentModelList.size();
        }

        public void setContentList(List<ContentModel> contentModelList) {
            this.contentModelList = contentModelList;
            notifyDataSetChanged();
        }

        public boolean isArticleFeatured(int position) {
            ContentModel contentModel = contentModelList.get(isContainHeaderRow ? position - 1 : position);
            return contentModel.isFeatured();
        }

        protected class DownloadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            @SuppressWarnings("WeakerAccess")
            @BindView(R.id.header_text)
            protected TextView header;

            public DownloadViewHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.tab_header_row, parent, false));
                ButterKnife.bind(this, itemView);
                itemView.setClickable(true);
                itemView.setOnClickListener(this);
                setupTitleAndIcon();
            }

            private void setupTitleAndIcon() {
                header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.my_downloads_untoggled, 0);
                header.setText(R.string.content_filter_tab);
            }

            @Override
            public void onClick(View v) {
                swipeRefresh.setRefreshing(true);
                currentSelectedIndex = 0;
                if (contentTabsState == ContentTabsState.Contents) {
                    loadDownloadsAndDisplayContent();
                    contentTabsState = ContentTabsState.MyDownloads;
                    header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.my_downloads_toggled, 0);
                    header.setText(R.string.content_download_header);
                } else {
                    loadContent();
                    contentTabsState = ContentTabsState.Contents;
                    header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.my_downloads_untoggled, 0);
                    header.setText(R.string.content_filter_tab);
                }
            }
        }

        public class FeaturedViewHolder extends ContentBaseViewHolder {
            public FeaturedViewHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.content_featured_view_holder, parent, false));
                ButterKnife.bind(this, itemView);
            }

        }

        public class NonFeaturedViewHolder extends ContentBaseViewHolder  {
            public NonFeaturedViewHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.content_non_featured_view_holder, parent, false));
                ButterKnife.bind(this, itemView);
            }
        }

        public class ContentBaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            @SuppressWarnings("WeakerAccess")
            @BindView(R.id.card_layout)
            protected LinearLayout cardLayout;

            @SuppressWarnings("WeakerAccess")
            @BindView(R.id.content_title)
            protected TextView contentTitle;

            @SuppressWarnings("WeakerAccess")
            @BindView(R.id.content_type)
            protected TextView contentTypeText;

            @SuppressWarnings("WeakerAccess")
            @BindView(R.id.content_date_sent)
            protected TextView contentDateSent;

            @SuppressWarnings("WeakerAccess")
            @BindView(R.id.content_image)
            protected ImageView contentImage;

            private ContentModel contentModel;

            public ContentBaseViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
            }

            protected void fillView(ContentModel content) {
                this.contentModel = content;
                String title = content.getTitle();
                String dateString = content.getCreated();
                String imageFilename = content.getThumbnailUrl();
                ContentLibraryType contentType = content.getContentType();

                if (!TextUtils.isEmpty(imageFilename)) {
                    contentImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    String imageUrl = getString(R.string.image_prefix_url) + imageFilename;

                    RequestOptions options = new RequestOptions();
                    options.centerCrop();
                    options.diskCacheStrategy(DiskCacheStrategy.RESOURCE);

                    RequestListener requestListener = new RequestListener() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                            new android.os.Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    setContentPlaceholder();
                                }
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    };

                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .apply(options)
                            .listener(requestListener)
                            .into(contentImage);
                } else {
                    setContentPlaceholder();
                }

                if (!TextUtils.isEmpty(title)) {
                    contentTitle.setText(title);
                }

                if (contentType != null) {
                    contentTypeText.setText(contentType.toString());
                    switch (contentType) {
                        case Article:
                            contentTypeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.content_type_article ,0 ,0 ,0);
                            break;
                        case Film:
                            contentTypeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.content_type_film ,0 ,0 ,0);
                            break;
                        case Podcast:
                            contentTypeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.content_type_podcast  ,0 ,0 ,0);
                            break;
                    }
                }

                try {
                    Date date = apiDateFormat.parse(dateString);
                    contentDateSent.setText(IBAUtils.getFormattedElapsedTimeFromDate(getActivity(), date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (isLandscape && contentIdSelected == contentModel.getId()) {
                    cardLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.content_item_clicked_background));
                } else {
                    cardLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.content_item_normal_background));
                }
            }

            private void setContentPlaceholder() {
                contentImage.setScaleType(ImageView.ScaleType.CENTER);
                contentImage.setImageResource(R.drawable.iba_image_placeholder);
            }

            @Override
            public void onClick(View v) {
                ContentModel contentModel = contentModelList.get(getCorrectAdapterPosition());
                currentSelectedIndex = getCorrectAdapterPosition();
                setCurrentContent(contentModel);
            }

            private int getCorrectAdapterPosition() {
                return isContainHeaderRow ? getAdapterPosition() - 1 : getAdapterPosition();
            }
        }
    }

    private void setCurrentContent(ContentModel contentModel) {
        contentIdSelected = contentModel.getId();
        contentFragmentListener.contentClicked(contentModel, contentTabsState);
        contentAdapter.notifyDataSetChanged();
    }

    public class ClearDetailFragment{}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            contentFragmentListener = (ContentFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ContentFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        contentFragmentListener = null;
    }

    public interface ContentFragmentListener {
        void contentClicked(ContentModel content, ContentTabsState contentTabState);
    }
}
