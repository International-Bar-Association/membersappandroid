package com.ibamembers.profile.message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.EventBusFragment;
import com.ibamembers.app.gcm.MyFcmMessagingService;
import com.ibamembers.profile.db.ProfileMessage;
import com.ibamembers.profile.db.ProfileMessageDao;
import com.ibamembers.profile.job.GetProfileJob;
import com.ibamembers.profile.job.ProfileModel;
import com.ibamembers.profile.message.job.GetMessageJob;
import com.ibamembers.profile.message.job.HideMessageConferenceJob;
import com.ibamembers.profile.message.job.HideMessageConferenceRequest;
import com.ibamembers.profile.message.job.ProfileMessageModel;
import com.ibamembers.profile.message.job.SendMessageJob;
import com.ibamembers.profile.message.job.SendMessageRequest;
import com.ibamembers.profile.message.job.SetProfileMessageReadJob;
import com.ibamembers.search.SearchProfileActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileMessageFragment extends EventBusFragment implements ProfileMessageListener {

	private static final String TAG = "ProfileMessage";
	private static final int REQUEST_USER_PROFILE_FROM_MESSAGE_THREAD = 100;
	private static final int TAKE_COUNT = 30;
	private final String GET_PROFILE_TAG = "GET_PROFILE_TAG";

	private SimpleDateFormat messageDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
	private SimpleDateFormat messageHeaderDateFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());

	public static Bundle getProfileMessageFragment(int profileId, String userName, boolean showViewProfileButton, boolean isConference) {
		Bundle args = new Bundle();
		args.putInt(ProfileMessageActivity.KEY_SEARCH_PROFILE_ID, profileId);
		args.putString(ProfileMessageActivity.KEY_USER_NAME, userName);
		args.putBoolean(ProfileMessageActivity.KEY_SHOW_PROFILE_BUTTON, showViewProfileButton);
		args.putBoolean(ProfileMessageActivity.KEY_IS_CONFERENCE, isConference);
		return args;
	}

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.profile_picture)
	protected ImageView profileImageView;

//	@SuppressWarnings("WeakerAccess")
//	@BindView(R.id.attending_members_layout)
//	protected RelativeLayout conferenceBadge;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.profile_user_name)
	protected TextView usernameText;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.profile_last_message_date)
	protected TextView lastMessageDate;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.view_profile_button)
	protected TextView viewProfileButton;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.profile_message_recycler_view)
	protected RecyclerView recyclerView;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.profile_no_message_image)
	protected ImageView noMessageIcon;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.profile_no_message)
	protected TextView noMessageText;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.input_text)
	protected EditText inputText;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.send_button)
	protected AppCompatButton sendButton;

	private ProfileMessageAdapter adapter;
	private LinearLayoutManager linearLayoutManager;
	private int profileId;
	private String userName;
	private boolean showViewProfileButton;
	private boolean isConference;

	private int skipIndex;
	private boolean loadMoreMessages = true;
	private boolean isDataLoadedFromApi = true;

	public int getCurrentMessageId() {
		return profileId;
	}

	public enum MessageType {
		UserProfile, UserConference, Recipient
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.profile_message_fragment, container, false);
		ButterKnife.bind(this, view);
		if (!isLayoutLargeAndLandscape()) {
			setHasOptionsMenu(true);
		}

		Bundle args = getArguments();
		if (args != null) {
			profileId = args.getInt(ProfileMessageActivity.KEY_SEARCH_PROFILE_ID);
			userName = args.getString(ProfileMessageActivity.KEY_USER_NAME);
			showViewProfileButton = args.getBoolean(ProfileMessageActivity.KEY_SHOW_PROFILE_BUTTON);
			isConference = args.getBoolean(ProfileMessageActivity.KEY_IS_CONFERENCE);
		}

		setupMessageTheme();
		setupInputTextAsScrollable();
		setRecyclerView();

		setupProfileHeader();
		getProfileJob();

		return view;
	}

	private void setupMessageTheme() {
		if (isConference) {
			//conferenceBadge.setVisibility(View.INVISIBLE);
			profileImageView.setImageResource(R.drawable.profile_image_placeholder_conference);
			usernameText.setTextColor(ContextCompat.getColor(getContext(), R.color.conference_search_font_tint));

			viewProfileButton.setTextColor(ContextCompat.getColor(getContext(), R.color.conference_main_close));
			sendButton.setBackgroundTintList(getResources().getColorStateList(R.color.conference_main_close));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isNetworkAvailable()) {
			getMessageHistory();
		} else {
			getProfileMessageFromDB();
			isDataLoadedFromApi = false;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.message_detail_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_delete) {
			confirmDelete();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(MyFcmMessagingService.NotificationEvent event) {
		Log.i("MessageDetail..", event.getMessage());
		getMessageHistory();
	}

	public void confirmDelete() {
		Activity activity = getActivity();
		if (activity != null) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
			alertDialog.setMessage(getString(R.string.profile_message_delete_message))
					.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							setMessageAsHidden();
						}
					})
					.setNegativeButton(getString(android.R.string.cancel), null);
			alertDialog.show();
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setupInputTextAsScrollable() {
		inputText.setVerticalScrollBarEnabled(true);
		inputText.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (v.getId() == R.id.input_text) {
					v.getParent().requestDisallowInterceptTouchEvent(true);
					switch (event.getAction() & MotionEvent.ACTION_MASK) {
						case MotionEvent.ACTION_UP:
							v.getParent().requestDisallowInterceptTouchEvent(false);
							break;
					}
				}
				return false;
			}
		});
	}

	private void setMessageAsHidden() {
		App app = getApp();
		if (app != null) {
			app.getJobManager(App.JobQueueName.Network).addJobInBackground(new HideMessageConferenceJob(new HideMessageConferenceRequest(profileId)));
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSendMessageSuccess(HideMessageConferenceJob.Success response) {
		Log.i(TAG, "Message thread hidden");
		getApp().sendEventToAnalytics(ANALYTIC_CATEGORY_MESSAGE, "P2P message deleted", null);
		if (!isLayoutLargeAndLandscape()) {
			Activity activity = getActivity();
			if (activity != null && activity instanceof ProfileMessageActivity) {
				activity.finish();
			}
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSendMessageSuccess(HideMessageConferenceJob.Failure response) {
		Log.e(TAG, "Failed to hide message");
	}

	private void setMessageAsRead(int lastMessageId) {
		getApp().getJobManager(App.JobQueueName.Network).addJobInBackground(new SetProfileMessageReadJob(lastMessageId));
	}

	private void getProfileJob() {
		getApp().getJobManager(App.JobQueueName.Network).addJobInBackground(new GetProfileJob(profileId, GET_PROFILE_TAG));
	}

	@SuppressLint("CheckResult")
	private void setupProfileHeader() {
		if (showViewProfileButton) {
			viewProfileButton.setVisibility(View.VISIBLE);
		}

		App app = getApp();
		if (app != null) {
			try {
				ProfileMessageDao profileMessageDao = app.getDatabaseHelper().getProfileMessageDao();
				ProfileMessage profileItem = profileMessageDao.queryForId(profileId);

				if (profileItem != null) {
					String photoPath = profileItem.getImageUrl();
					String profileImagePrefix = getString(R.string.profile_image_prefix); //The image fetched from P2Pconference and profile are inconsistent (profile contains the whole url)

					if (!TextUtils.isEmpty(photoPath)) {
						if (!profileItem.getImageUrl().contains(profileImagePrefix)) {
							photoPath = profileImagePrefix + profileItem.getImageUrl();
						}

						RequestOptions options = new RequestOptions();
						options.centerCrop();
						options.diskCacheStrategy(DiskCacheStrategy.NONE);
						options.skipMemoryCache(true);

						Glide.with(getContext())
								.load(photoPath)
								.apply(options)
								.into(profileImageView);
					}
				}
				else {
					profileImageView.setImageDrawable(ContextCompat.getDrawable(getContext(),
							isConference ? R.drawable.profile_image_placeholder_conference : R.drawable.profile_image_placeholder));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void setRecyclerView() {
		linearLayoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(linearLayoutManager);
		adapter = new ProfileMessageAdapter(new ArrayList<ProfileMessageModel>(), this);
		recyclerView.setAdapter(adapter);

		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0 && loadMoreMessages && isDataLoadedFromApi) {
					Log.i("ProfileMessageFragment", "Top of list reached");
					getMessageHistory(skipIndex, TAKE_COUNT);
				}
			}
		});

		recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right,int bottom, int oldLeft, int oldTop,int oldRight, int oldBottom) {
				recyclerView.scrollToPosition(adapter.getMessageModelList().size() - 1);
			}
		});

	}

	private void getMessageHistory() {
		getMessageHistory(0, TAKE_COUNT);
	}

	private void getMessageHistory(int skip, int paging) {
		try {
			getApp().getJobManager(App.JobQueueName.Network).addJobInBackground(new GetMessageJob(profileId, skip, paging));
			skipIndex += TAKE_COUNT;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageReadSuccess(SetProfileMessageReadJob.Success response) {
		Log.i(TAG, "Message Read");
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSendMessageSuccess(GetProfileJob.Success response) {
		if (response.getJobTag().equals(GET_PROFILE_TAG)) {
			ProfileModel profile = response.getProfileModel();

			String firstName = profile.getFirstName();
			String lastName = profile.getLastName();
			String profilePicUrl = profile.getProfilePictureUrl();

			//conferenceBadge.setVisibility(View.INVISIBLE);
			userName = getApp().getDataManager().getFullName(firstName, lastName);
			if (!TextUtils.isEmpty(userName)) {
				usernameText.setText(userName);
			}

			if (profilePicUrl == null || profilePicUrl.equals("N/A")) {
				setNoPicture();
			} else {
				RequestOptions options = new RequestOptions();
				options.centerCrop();
				options.diskCacheStrategy(DiskCacheStrategy.RESOURCE);
				options.skipMemoryCache(true);

				Glide.with(getActivity())
						.load(profilePicUrl)
						.apply(options)
						.into(profileImageView);
			}

			setupProfileHeader();
		}
	}

	private void setNoPicture() {
		Activity activity = getActivity();
		if (activity != null) {
			profileImageView.setImageDrawable(ContextCompat.getDrawable(activity,
					isConference ? R.drawable.profile_image_placeholder_conference : R.drawable.profile_image_placeholder));
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSendMessageFailed(GetProfileJob.GetProfileJobError response) {
		Log.e("profileMessageFrag", "Failed to get profile details");
	}

	@SuppressWarnings("unused")
	@OnClick(R.id.view_profile_button)
	public void onViewProfileButtonClicked() {
		startActivityForResult(SearchProfileActivity.getSearchProfileActivityIntent(getContext(), profileId, userName, true), REQUEST_USER_PROFILE_FROM_MESSAGE_THREAD);
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onGetMessageSuccess(GetMessageJob.Success response) {
		List<ProfileMessageModel> messageList = response.getResponse().getMessages();
		for (ProfileMessageModel message: messageList) { message.setMessageStatus(ProfileMessageModel.MessageStatus.Sent); }

		if (response.getSkip() > 0) {
			loadMoreMessages = true;
			adapter.appendMessageList(messageList);
		} else {
			adapter.setMessageHistory(messageList);
			recyclerView.scrollToPosition(adapter.getMessageModelList().size() - 1);
			if (adapter.getMessageModelList().size() > 0) {
				setMessageAsRead(adapter.getLastMessageId());
				noMessageText.setVisibility(View.INVISIBLE);
				noMessageIcon.setVisibility(View.INVISIBLE);
			} else {
				noMessageText.setVisibility(View.VISIBLE);
				noMessageIcon.setVisibility(View.VISIBLE);
			}
		}
		//setLatestMessageDate(messageList.get(messageList.size() - 1));
		setLatestMessageDate(response.getResponse().getOtherParticipantLastSeenDateTime());
		if (messageList.size() < TAKE_COUNT) {
			loadMoreMessages = false;
		}

		try {
			String messageHistoryString = new Gson().toJson(adapter.getMessageModelList());
			ProfileMessageDao profileMessageDao = getApp().getDatabaseHelper().getProfileMessageDao();

			List<ProfileMessage> profileList = profileMessageDao.queryForProfileId(profileId);

			if (!profileList.isEmpty()) {
				ProfileMessage profileMessage = profileList.get(0);
				profileMessage.setMessageString(messageHistoryString);
			}

			//profileMessageDao.saveResponseAsProfileMessage(profileId, userName, "", messageHistoryString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void getProfileMessageFromDB() {
		try {
			Log.i(TAG, "Loading profile message from DB");
			ProfileMessageDao profileMessageDao = getApp().getDatabaseHelper().getProfileMessageDao();
			ProfileMessage profileMessage = profileMessageDao.queryForId(profileId);

			if (profileMessage != null) {
				String messageString = profileMessage.getMessageString();
				Type listType = new TypeToken<List<ProfileMessageModel>>() {
				}.getType();
				List<ProfileMessageModel> profileMessageList = new Gson().fromJson(messageString, listType);

				adapter.setMessageHistory(profileMessageList);
				recyclerView.scrollToPosition(adapter.getMessageModelList().size() - 1);
				usernameText.setText(profileMessage.getProfileName());
			}else {
				noMessageText.setVisibility(View.VISIBLE);
				noMessageIcon.setVisibility(View.VISIBLE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSendMessageSuccess(GetMessageJob.GetMessageJobError response) {
		Log.i(TAG, "Failed to get message job error");
		loadMoreMessages = false;
		noMessageText.setVisibility(View.VISIBLE);
		noMessageIcon.setVisibility(View.VISIBLE);
	}

	private void setLatestMessageDate(Date userLastSeenDate) {
		lastMessageDate.setText(getString(R.string.profile_message_last_seen, messageDateFormat.format(userLastSeenDate)));
	}

	@SuppressWarnings("unused")
	@OnClick(R.id.send_button)
	public void sendClicked() {
		String input = inputText.getText().toString().trim();

		if (!TextUtils.isEmpty(input)) {
			noMessageText.setVisibility(View.INVISIBLE);
			noMessageIcon.setVisibility(View.INVISIBLE);

			String uuid = UUID.randomUUID().toString();
			SendMessageRequest messageRequest = new SendMessageRequest(profileId, uuid, input);

			inputText.setText("");
			adapter.addNewMessage(new ProfileMessageModel(uuid, input, true, new Date(), null, null, ProfileMessageModel.MessageStatus.Sending));
			recyclerView.scrollToPosition(adapter.getMessageModelList().size() - 1);

			getApp().getJobManager(App.JobQueueName.Network).addJobInBackground(new SendMessageJob(messageRequest));
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSendMessageSuccess(SendMessageJob.Success response) {
		//setMessageAsRead(response);
		adapter.updateMessageStatus(response.getResponse().getMessage().getUuid(), ProfileMessageModel.MessageStatus.Sent);
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSendMessageSuccess(SendMessageJob.SendMessageJobError response) {
		Log.e("ProfileMessageFragment", "Message failed to send");
		adapter.updateMessageStatus(response.getUuid(), ProfileMessageModel.MessageStatus.Failed);
	}

	@Override
	public void onMessageClicked(ProfileMessageModel message) {
		adapter.updateMessageStatus(message.getUuid(), ProfileMessageModel.MessageStatus.Sending);
		SendMessageRequest messageRequest = new SendMessageRequest(profileId, message.getUuid(), message.getMessage());
		getApp().getJobManager(App.JobQueueName.Network).addJobInBackground(new SendMessageJob(messageRequest));
	}

	public class ProfileMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

		private ProfileMessageListener listener;
		private ArrayList<DateTime> usedDate;
		private SparseBooleanArray headerArray;
		private List<ProfileMessageModel> messageModelList;

		public int getLastMessageId() {
			return getMessageModelList().get(getItemCount() - 1).getMessageId();
		}

		public List<ProfileMessageModel> getMessageModelList() {
			return messageModelList;
		}

		public ProfileMessageAdapter(List<ProfileMessageModel> messageModelList, ProfileMessageListener listener) {
			this.messageModelList = messageModelList;
			this.usedDate = new ArrayList<>();
			this.headerArray = new SparseBooleanArray();
			this.listener = listener;
		}

		public void setMessageHistory(List<ProfileMessageModel> messageModelList) {
			this.messageModelList = messageModelList;
			Collections.sort(this.messageModelList, new ProfileMessageModelComparator());
			calculateHeaderIndices();
			notifyDataSetChanged();
		}

		public void appendMessageList(List<ProfileMessageModel> messageModelList) {
			this.messageModelList.addAll(messageModelList);
			Collections.sort(this.messageModelList, new ProfileMessageModelComparator());
			calculateHeaderIndices();
			notifyDataSetChanged();
		}

		public void addNewMessage(ProfileMessageModel newMessage) {
			this.messageModelList.add(newMessage);
			Collections.sort(this.messageModelList, new ProfileMessageModelComparator());
			calculateHeaderIndices();
			notifyDataSetChanged();
		}

		public void updateMessageStatus(String uuid,  ProfileMessageModel.MessageStatus messageStatus) {
			for (int i = 0; i < messageModelList.size(); i++) {
				String messageUuid = messageModelList.get(i).getUuid();
				if (messageUuid != null && messageUuid.equals(uuid)) {
					messageModelList.get(i).setMessageStatus(messageStatus);
					break;
				}
			}

			notifyDataSetChanged();
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			if (viewType == MessageType.UserProfile.ordinal()) {
				return new ProfileMessageUserViewHolder(inflater, parent);
			}
			else if (viewType == MessageType.UserConference.ordinal()) {
				return new ProfileMessageUserConferenceViewHolder(inflater, parent, listener);
			}
			else if (viewType == MessageType.Recipient.ordinal()) {
				return new ProfileMessageRecipientViewHolder(inflater, parent);
			}
			return null;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

			boolean showHeader;
			if (headerArray.get(position)) {
				showHeader = true;
			} else {
				showHeader = false;
			}

			if (holder instanceof ProfileMessageUserViewHolder) {
				((ProfileMessageUserViewHolder) holder).fillView(messageModelList.get(position), showHeader);
			}
			else if (holder instanceof ProfileMessageUserConferenceViewHolder) {
				((ProfileMessageUserConferenceViewHolder) holder).fillView(messageModelList.get(position), showHeader);
			}
			else if (holder instanceof ProfileMessageRecipientViewHolder) {
				((ProfileMessageRecipientViewHolder) holder).fillView(messageModelList.get(position), showHeader);
			}
		}

		@Override
		public int getItemViewType(int position) {
			ProfileMessageModel message = messageModelList.get(position);
			if (message.isSentMyBe()) {
				if (isConference) {
					return MessageType.UserConference.ordinal();
				} else {
					return MessageType.UserProfile.ordinal();
				}
			} else {
				return MessageType.Recipient.ordinal();
			}
		}

		@Override
		public int getItemCount() {
			return messageModelList.size();
		}

		private void calculateHeaderIndices() {
			usedDate.clear();
			headerArray.clear();

			int index = 0;
			for (ProfileMessageModel profileMessageModel : messageModelList) {

				DateTime dateTime = new DateTime(profileMessageModel.getSentTime());
				dateTime = dateTime.withTimeAtStartOfDay();

				if (!usedDate.contains(dateTime)) {
					usedDate.add(dateTime);
					headerArray.append(index, true);
				}
				index++;
			}
		}
	}

	public class ProfileMessageUserViewHolder extends ProfileMessageBaseViewHolder {
		public ProfileMessageUserViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
			super(inflater.inflate(R.layout.profile_message_user_view_holder, viewGroup, false));
		}

		@Override
		protected int getBackgroundColor() { return R.drawable.profile_message_user_rounded_border; }
	}

	public class ProfileMessageUserConferenceViewHolder extends ProfileMessageBaseViewHolder implements View.OnClickListener {

		private ProfileMessageListener listener;

		public ProfileMessageUserConferenceViewHolder(LayoutInflater inflater, ViewGroup viewGroup, ProfileMessageListener listener) {
			super(inflater.inflate(R.layout.profile_message_user_view_holder, viewGroup, false));
			this.listener = listener;
			itemView.setOnClickListener(this);
		}

		@Override
		protected int getBackgroundColor() { return R.drawable.profile_message_user_rounded_border_conference; }


		@Override
		public void onClick(View view) {
			listener.onMessageClicked(message);
		}
	}

	public class ProfileMessageRecipientViewHolder extends ProfileMessageBaseViewHolder {
		public ProfileMessageRecipientViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
			super(inflater.inflate(R.layout.profile_message_recipient_view_holder, viewGroup, false));
		}

		@Override
		protected int getBackgroundColor() { return R.drawable.profile_message_recipient_rounded_border; }
	}

	public abstract class ProfileMessageBaseViewHolder extends RecyclerView.ViewHolder {

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_layout)
		protected LinearLayout messageLayout;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_day_header)
		protected TextView dayHeader;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_text)
		protected TextView messageText;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_date)
		protected TextView messageDate;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_status)
		protected TextView messageStatus;

		protected ProfileMessageModel message;

		public ProfileMessageBaseViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
			messageLayout.setBackground(ContextCompat.getDrawable(itemView.getContext(), getBackgroundColor()));
		}

		protected abstract int getBackgroundColor();

		protected void fillView(ProfileMessageModel message, boolean showHeader) {
			this.message = message;
			messageText.setText(message.getMessage());
			messageDate.setText(messageDateFormat.format(message.getSentTime()));

			if (showHeader) {
				dayHeader.setVisibility(View.VISIBLE);
				dayHeader.setText(getDateFormat(message.getSentTime()));
			} else {
				dayHeader.setVisibility(View.GONE);
			}

			messageStatus.setVisibility(View.VISIBLE);
			messageStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.message_item_status_sending));
			switch (message.getMessageStatus()) {
				case Sending:
					messageStatus.setText(R.string.profile_message_status_sending);
					break;
				case Failed:
					messageStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.message_item_status_failed));
					messageStatus.setText(R.string.profile_message_status_failed);
					break;
				default:
					messageStatus.setVisibility(View.GONE);
					break;
			}
		}

		private String getDateFormat(Date date) {
			DateTime todayDateTime = new DateTime().withTimeAtStartOfDay();
			DateTime yesterdayDateTime = new DateTime().minusDays(1).withTimeAtStartOfDay();
			DateTime dateTime = new DateTime(date).withTimeAtStartOfDay();

			if (dateTime.equals(todayDateTime)) {
				return getString(R.string.profile_message_date_format_today);
			} else if (dateTime.equals(yesterdayDateTime)) {
				return getString(R.string.profile_message_date_format_yesterday);
			} else {
				return messageHeaderDateFormat.format(date);
			}
		}
	}

	public class ProfileMessageModelComparator implements Comparator<ProfileMessageModel> {
		public int compare(ProfileMessageModel left, ProfileMessageModel right) {
			return left.getSentTime().compareTo(right.getSentTime());
		}
	}
}

interface ProfileMessageListener {
	void onMessageClicked(ProfileMessageModel message);
}


