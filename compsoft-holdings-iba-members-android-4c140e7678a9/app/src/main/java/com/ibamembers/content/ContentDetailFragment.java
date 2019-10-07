package com.ibamembers.content;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.EventBusFragment;
import com.ibamembers.app.IBAUtils;
import com.ibamembers.content.db.ContentDownload;
import com.ibamembers.content.db.ContentDownloadDao;
import com.ibamembers.content.job.ContentModel;
import com.ibamembers.content.job.DownloadMediaJob;
import com.ibamembers.main.MainActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class ContentDetailFragment extends EventBusFragment implements MediaController.MediaPlayerControl, View.OnTouchListener, MediaPlayer.OnPreparedListener {

	public static final String KEY_CONTENT_ID = "KEY_CONTENT_ID";
	public static final String KEY_IS_DOWNLOADS = "KEY_IS_DOWNLOADS";

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.content_fab)
	protected FloatingActionButton landscapeContentFab;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.content_detail_title)
	protected TextView contentDetailTitle;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.content_detail_type)
	protected TextView contentDetailTypeText;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.content_detail_date_sent)
	protected TextView contentDetailDateSent;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.content_detail_image)
	protected ImageView contentDetailImage;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.content_detail_description)
	protected TextView contentDetailDescription;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.article_button)
	protected Button articleButton;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.content_detail_progress)
	protected ProgressBar progressBar;

	public static Bundle getContentDetailFragmentArgs(String contentString, boolean isMyDownloads) {
		Bundle bundle = new Bundle();
		bundle.putString(KEY_CONTENT_ID, contentString);
		bundle.putBoolean(KEY_IS_DOWNLOADS, isMyDownloads);
		return bundle;
	}

	private ContentDetailFragmentListener contentDetailFragmentListener;
	private ContentModel currentContent;
	private SimpleDateFormat apiDateFormat;

	private MediaPlayer mediaplayer;
	private MediaController mediaController;
	private PlayerState playerState;
	private int calculatedDuration;
	private String mediaUrl;
	private boolean canDownload;  //for articles like video where it cannot be downloaded
	private boolean isDownloaded; //check whether article has been downloaded
	private boolean isIntentFromMyDownloads;
	private boolean isLandscape;
	private MenuItem downloadItem;

	public int getCurrentContentId() {
		if (currentContent != null) {
			return currentContent.getId();
		}
		return -1;
	}

	public enum PlayerState {
		Idle,
		Stop,
		Play,
		Pause
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.content_detail_fragment, container, false);
		View layoutIsLandscape = view.findViewById(R.id.content_detail_is_landscape);
		isLandscape = layoutIsLandscape != null;
		ButterKnife.bind(this, view);
		apiDateFormat = new SimpleDateFormat(getString(R.string.default_api_date_format), Locale.getDefault());

		Bundle args = getArguments();
		if (args != null) {
			String contentString = args.getString(KEY_CONTENT_ID);
			isIntentFromMyDownloads = args.getBoolean(KEY_IS_DOWNLOADS);
			currentContent = new Gson().fromJson(contentString, ContentModel.class);
			fillView();
		}

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.content_detail_menu, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		downloadItem = menu.findItem(R.id.action_download);
		MenuItem deleteItem = menu.findItem(R.id.action_delete);

		if (!canDownload) {
			downloadItem.setVisible(false);
			deleteItem.setVisible(false);
		} else {
			downloadItem.setVisible(!isDownloaded);
			deleteItem.setVisible(isDownloaded);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_download) {
			downloadAndSaveContent();
			return true;
		} else if (item.getItemId() == R.id.action_delete) {
			deleteContent();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void downloadAndSaveContent() {
		if (!isContentDownloaded()) {
			downloadMedia();
		} else {
			Toast.makeText(getActivity(), getString(R.string.content_detail_download_action_already_downloaded), Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(DownloadMediaJob.DownloadComplete response) {
		App app = getApp();
		if (app != null) {
			try {
				downloadItem.setEnabled(true);
				progressBar.setVisibility(View.GONE);
				File downloadedFile = response.getDownloadFile();
				ContentDownloadDao contentDownloadDao = app.getDatabaseHelper().getContentDownloadDao();
				currentContent.setFileDir(downloadedFile != null ? response.getDownloadFile().getAbsolutePath() : null);
				contentDownloadDao.saveContentToDownloads(currentContent);
				contentDetailFragmentListener.invalidateMenus(canDownload, isDownloaded = true);
				Toast.makeText(getActivity(), getString(R.string.content_detail_download_action_complete), Toast.LENGTH_SHORT).show();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(DownloadMediaJob.DownloadAndPlayComplete response) {
		progressBar.setVisibility(View.GONE);
		mediaUrl = response.getDownloadFile().getAbsolutePath();
		isDownloaded = true;
		downloadItem.setEnabled(true);
		prepareAudioPlayer(mediaUrl);
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(DownloadMediaJob.DownloadFailed response) {
		progressBar.setVisibility(View.GONE);
		downloadItem.setEnabled(true);
		showErrorDialog(null, getString(R.string.content_detail_download_action_failed), getString(android.R.string.ok));
	}

	private void downloadMedia() {
		if (currentContent != null) {
			String mediaUrl = currentContent.getUrl();
			ContentBaseFragment.ContentLibraryType contentType = currentContent.getContentType();
			switch (contentType) {
				case Article:
					downloadURl(mediaUrl, false);
					break;
				case Podcast:
					downloadURl(mediaUrl, false);
					break;
			}
		}
	}

	private void downloadURl(String stringUrl, boolean isInstantPlay) {
		App app = getApp();
		if (app != null) {
			progressBar.setVisibility(View.VISIBLE);
			downloadItem.setEnabled(false);
			app.getJobManager(App.JobQueueName.Network).addJobInBackground(new DownloadMediaJob(getActivity(), stringUrl, isInstantPlay));
		}
	}

	public void deleteContent() {
		showAlertDialog(getString(R.string.content_detail_delete_action_message), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				App app = getApp();
				if (app != null) {
					try {
						ContentDownloadDao contentDownloadDao = app.getDatabaseHelper().getContentDownloadDao();
						contentDownloadDao.deleteById(currentContent.getId());
						Toast.makeText(getActivity(), getString(R.string.content_detail_delete_action_confirmed), Toast.LENGTH_SHORT).show();

						//Post to ContentHandlerFragment to invalidate menus and ContentFragment to decrement index and reload content
						app.getEventBus().post(new DownloadDeleted(canDownload, isDownloaded = false));

						//This is for ContentDetailActivity in portraitMode
						contentDetailFragmentListener.invalidateMenus(canDownload, isDownloaded);
						if (isIntentFromMyDownloads) {
							getActivity().setResult(Activity.RESULT_OK);
							getActivity().finish();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private boolean isContentDownloaded() {
		App app = getApp();
		if (app != null) {
			try {
				ContentDownloadDao contentDownloadDao = app.getDatabaseHelper().getContentDownloadDao();
				List<ContentDownload> contentDownloadList = contentDownloadDao.queryForAll();
				for (ContentDownload download : contentDownloadList) {
					if (download.getId() == currentContent.getId()) {
						return true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private void fillView() {
		String title = currentContent.getTitle();
		String dateString = currentContent.getCreated();
		String imageFilename = currentContent.getThumbnailUrl();
		ContentFragment.ContentLibraryType contentType = currentContent.getContentType();
		String description = currentContent.getPrecis();

		if (isIntentFromMyDownloads) {
			String fileDir = currentContent.getFileDir();
			//Media file was not saved so load the url instead
			if (!TextUtils.isEmpty(fileDir)) {
				mediaUrl = currentContent.getFileDir();
			} else {
				mediaUrl = currentContent.getUrl();
			}
		} else {
			mediaUrl = currentContent.getUrl();
		}

		String imageUrl = getString(R.string.image_prefix_url) + imageFilename;
		Activity activity = getActivity();
		if (activity != null && !activity.isDestroyed()) {
			if (activity instanceof ContentDetailActivity) {
				SquareImageView imageToolbar = ((ContentDetailActivity) getActivity()).getImageToolbar();
				if (!TextUtils.isEmpty(imageFilename)) {
					loadImage(imageUrl, imageToolbar);
				}
			} else if (activity instanceof MainActivity) { //Tablet landscape mode
				contentDetailImage.setVisibility(View.VISIBLE);
				loadImage(imageUrl, contentDetailImage);
			}
		}

		if (!TextUtils.isEmpty(title)) {
			contentDetailTitle.setText(title);
		}


		isDownloaded = isContentDownloaded();
		canDownload = true;
		if (contentType != null) {
			contentDetailTypeText.setText(contentType.toString());
			switch (contentType) {
				case Article:
					setupArticleViews();
					break;
				case Film:
					canDownload = false;
					setupFilmViews(mediaUrl);
					break;
				case Podcast:
					try {
						setupPodcastViews(mediaUrl);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					break;
			}
		}
		contentDetailFragmentListener.invalidateMenus(canDownload, isDownloaded);

		try {
			Date date = apiDateFormat.parse(dateString);
			contentDetailDateSent.setText(IBAUtils.getFormattedElapsedTimeFromDate(getActivity(), date));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (!TextUtils.isEmpty(description)) {
			contentDetailDescription.setText(description);
		}
	}

	private void loadImage(String imageUrl, ImageView imageView){
		RequestOptions options = new RequestOptions();
		options.diskCacheStrategy(DiskCacheStrategy.RESOURCE);

		Glide.with(getActivity())
				.load(imageUrl)
				.apply(options)
				.into(imageView);
	}

	@SuppressWarnings("unused")
	@OnClick(R.id.article_button)
	protected void articleButtonClicked() {
		if (!TextUtils.isEmpty(mediaUrl)) {
			contentDetailFragmentListener.loadWebView(currentContent.getFileDir(), currentContent.getUrl());
		}
	}

	private void setupArticleViews() {
		contentDetailTypeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.content_type_article, 0, 0, 0);
		if (getActivity() instanceof ContentDetailActivity) {
			((ContentDetailActivity) getActivity()).setContentFabVisibility(View.GONE);
		}
	}

	private void setupFilmViews(final String videoUrl) {
		contentDetailTypeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.content_type_film, 0, 0, 0);
		articleButton.setVisibility(View.GONE);
		if (getActivity() instanceof ContentDetailActivity) {
			((ContentDetailActivity) getActivity()).setContentFabClickListener(getFilmClickListener(videoUrl));
		} else if (getActivity() instanceof MainActivity) {
			landscapeContentFab.setVisibility(View.VISIBLE);
			landscapeContentFab.setOnClickListener(getFilmClickListener(videoUrl));
		}
	}

	private View.OnClickListener getFilmClickListener(final String videoUrl) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isWifiConnected()) {
					contentDetailFragmentListener.loadVideoPlayer(videoUrl);
				} else if (isNetworkAvailable()) {
					showDataWarningDialog(getString(R.string.content_detail_data_warning_message_video), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							contentDetailFragmentListener.loadVideoPlayer(videoUrl);
						}
					});
				} else {
					showErrorDialog(null, getString(R.string.content_detail_no_internet_connection), getString(android.R.string.ok));
				}
			}
		};
	}

	private void setupPodcastViews(final String audioUrl) throws IllegalArgumentException {
		contentDetailTypeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.content_type_podcast, 0, 0, 0);
		articleButton.setVisibility(View.GONE);
		playerState = PlayerState.Idle;

		if (!TextUtils.isEmpty(audioUrl)) {
			if (getActivity() instanceof ContentDetailActivity) {
				((ContentDetailActivity) getActivity()).setContentFabClickListener(getPodcastClickListener(audioUrl));
			} else if (getActivity() instanceof MainActivity) {
				landscapeContentFab.setVisibility(View.VISIBLE);
				landscapeContentFab.setOnClickListener(getPodcastClickListener(audioUrl));
			}
		}
	}

	private View.OnClickListener getPodcastClickListener(final String audioUrl) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (playerState == PlayerState.Idle) {
					if (!isDownloaded) {
						downloadURl(mediaUrl, true);
						return;
					} else {
						String fileDir = getDownloadedContentFile(currentContent.getId());
						if (fileDir != null) {
							mediaUrl = fileDir;
							prepareAudioPlayer(mediaUrl);
							return;
						}
					}

					//Audio is not played before so check wifi
					if (isNetworkAvailable()) {
						showDataWarningDialog(getString(R.string.content_detail_data_warning_message), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								prepareAudioPlayer(audioUrl);
							}
						});
					} else {
						showErrorDialog(null, getString(R.string.content_detail_no_internet_connection), getString(android.R.string.ok));
					}
				} else {
					playPodcast();
				}
			}
		};
	}

	private String getDownloadedContentFile(int id) {
		try {
			ContentDownloadDao contentDownloadDao = getApp().getDatabaseHelper().getContentDownloadDao();
			ContentDownload contentDownload = contentDownloadDao.queryForId(id);
			return contentDownload.getFileDir();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void showDataWarningDialog(String message, DialogInterface.OnClickListener positiveListener) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setTitle(getString(R.string.content_detail_data_warning_title))
				.setMessage(message)
				.setPositiveButton(getString(R.string.content_detail_data_warning_continue), positiveListener)
				.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		alertDialog.show();
	}

	private void playPodcast() {
		switch (playerState) {
			case Pause:
			case Stop:
				new Handler().post(new Runnable() {
					public void run() {
						mediaController.setEnabled(true);
						mediaController.show();
						mediaplayer.start();
						playerState = PlayerState.Play;
						updatePlayerFab(R.drawable.content_detail_pause_icon);
					}
				});
				break;
			case Play:
				mediaplayer.pause();
				playerState = PlayerState.Pause;
				updatePlayerFab(R.drawable.content_detail_play_icon);
				break;
		}
	}

	private void updatePlayerFab(int drawable) {
		if (getActivity() instanceof ContentDetailActivity) {
			((ContentDetailActivity) getActivity()).updateContentFabStateImage(drawable);
		} else if (getActivity() instanceof MainActivity) {
			landscapeContentFab.setImageResource(drawable);
		}
	}

	private void prepareAudioPlayer(String audioUrl) throws IllegalArgumentException {
		//Fail to grab duration from audio url so calculate separately
		try {
			FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
			retriever.setDataSource(audioUrl);
			String duration = retriever.getMetadata().getAll().get(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
			calculatedDuration = Integer.parseInt(duration);

			mediaplayer = new MediaPlayer();
			mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			playerState = PlayerState.Stop;

			try {
				mediaplayer.setDataSource(audioUrl);
				mediaplayer.prepare();
				mediaplayer.setOnPreparedListener(this);
			} catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
				e.printStackTrace();
			}

			mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					playerState = PlayerState.Stop;
					updatePlayerFab(R.drawable.content_detail_play_icon);
				}
			});
		} catch (IllegalArgumentException e) {
			Log.e("ContentDetailFrag", e.getMessage());
			showOKErrorDialog(getString(R.string.content_detail_fail_to_load_media), null);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mediaController != null) {
			mediaController.show();
			return false;
		}
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mediaController = new MediaController(getActivity(), false);
		mediaController.setMediaPlayer(this);
		if (getActivity() instanceof ContentDetailActivity) {
			mediaController.setAnchorView(((ContentDetailActivity) getActivity()).getPodCastControllerView());
			((ContentDetailActivity) getActivity()).setPodCastImageViewTouchListener(this);
		} else if (getActivity() instanceof MainActivity) {
			mediaController.setAnchorView(getView());
			contentDetailImage.setOnTouchListener(this);
		}
		if (playerState == PlayerState.Stop) {
			playPodcast();
		}
	}

	@Override
	public void start() {
		mediaplayer.start();
		playerState = PlayerState.Play;
		updatePlayerFab(R.drawable.content_detail_pause_icon);
	}

	@Override
	public void pause() {
		mediaplayer.pause();
		playerState = PlayerState.Pause;
		updatePlayerFab(R.drawable.content_detail_play_icon);
	}

	@Override
	public int getDuration() {
		int duration = mediaplayer.getDuration();
		if (duration <= 0) {
			return calculatedDuration;
		} else {
			return duration;
		}
	}

	@Override
	public int getCurrentPosition() {
		return mediaplayer.getCurrentPosition();
	}

	@Override
	public void seekTo(int pos) {
		mediaplayer.seekTo(pos);
	}

	@Override
	public boolean isPlaying() {
		return mediaplayer.isPlaying();
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return false;
	}

	@Override
	public boolean canSeekForward() {
		return false;
	}

	@Override
	public int getAudioSessionId() {
		return 0;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mediaplayer != null) {
			mediaplayer.stop();
		}
	}

	public boolean isWifiConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
	}

	protected boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public class DownloadDeleted {
		private boolean canDownload;
		private boolean isDownloaded;

		public DownloadDeleted(boolean canDownload, boolean isDownloaded) {
			this.canDownload = canDownload;
			this.isDownloaded = isDownloaded;
		}

		public boolean isCanDownload() {
			return canDownload;
		}

		public boolean isDownloaded() {
			return isDownloaded;
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			contentDetailFragmentListener = (ContentDetailFragmentListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement ContentDetailFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		contentDetailFragmentListener = null;
	}

	public interface ContentDetailFragmentListener {
		void loadVideoPlayer(String videoUrl);

		void loadWebView(String offlineUrl, String url);

		void invalidateMenus(boolean canDownload, boolean isDownloaded);
	}
}
