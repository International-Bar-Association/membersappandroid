package com.ibamembers.messages;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.EventBusFragment;
import com.ibamembers.app.gcm.MyFcmMessagingService;
import com.ibamembers.messages.db.NewMessageBufferDao;
import com.ibamembers.messages.job.AllMessagesModel;
import com.ibamembers.messages.job.GeneralMessageModel;
import com.ibamembers.messages.job.GetNormalMessagesJob;
import com.ibamembers.messages.job.GetSingleMessagesJob;
import com.ibamembers.messages.job.SetMessagesStatusDeletedJob;
import com.ibamembers.profile.db.ProfileMessageDao;
import com.ibamembers.profile.message.job.GetMessageConferenceJob;
import com.ibamembers.profile.message.job.GetMessageConferenceResponse;
import com.ibamembers.profile.message.job.ProfileMessageModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ibamembers.profile.message.job.GetMessageConferenceJob.GET_CONFERENCE_MESSAGE_JOB_TAG;
import static com.ibamembers.profile.message.job.GetMessageConferenceJob.GET_NORMAL_MESSAGE_JOB_TAG;

public class MessagesFragment extends EventBusFragment {

	private static final String TAG = "MessagesFragment";
	private static final String GET_MESSAGES_TAG = "GET_MESSAGES_TAG";

	public static final String KEY_MESSAGE_TYPE = "KEY_MESSAGE_TYPE";

	public static Bundle getMessageFragmentArgs(MessageAdapter.P2PType messageType){
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_MESSAGE_TYPE, messageType);
		return bundle;
	}

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.messages_recycler)
	protected RecyclerView messageRecycler;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.messages_no_messages)
	protected TextView noMessagesText;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.message_swipe_refresh)
	protected SwipeRefreshLayout swipeRefresh;

	private MessageFragmentListener messageFragmentListener;
	private MessageAdapter messageAdapter;
	private boolean isLandscape;
	private List<GeneralMessageModel> fetchedMessages;
	private MessageAdapter.P2PType messageType = MessageAdapter.P2PType.Normal;
	private boolean isFetchingMessages;

	public int getCurrentSelectedIndex() {
		return messageAdapter.getCurrentSelectedIndex();
	}

	public void setCurrentSelectedIndex(int currentSelectedIndex) {
		if (messageAdapter != null) {
			messageAdapter.setCurrentSelectedIndex(currentSelectedIndex);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.messages_fragment, container, false);
		ButterKnife.bind(this, view);

		View layoutIsLandscape = view.findViewById(R.id.message_fragment_is_landscape);
		isLandscape = layoutIsLandscape != null;
		fetchedMessages = new ArrayList<>();

		Bundle args = getArguments();
		if (args != null) messageType = (MessageAdapter.P2PType) args.getSerializable(KEY_MESSAGE_TYPE);

		setupRecycler(null);
		setupSwipeRefreshLayout();

		if (messageType == MessageAdapter.P2PType.Conference) clearNotificationCount();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		App app = getApp();
		if (app != null) {
			if (!app.getEventBus().isRegistered(this)) {
				app.getEventBus().register(this);
			}
		}
		loadMessages();
	}

	private void setupSwipeRefreshLayout() {
		swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadMessages();
			}
		});
	}

	protected void setupRecycler(List<GeneralMessageModel> messageList) {
		Activity activity = getActivity();
		if (activity != null) {
			if (messageAdapter == null) {
				if (messageList == null) {
					messageList = new ArrayList<>();
				}
				messageAdapter = new MessageAdapter(messageList, messageFragmentListener, isLandscape, messageType);
			}
			messageRecycler.setLayoutManager(new LinearLayoutManager(activity));
			messageRecycler.setAdapter(messageAdapter);
		}
	}

	public void loadMessages() {
		if (messageType == MessageAdapter.P2PType.Normal) {
			fetchedMessages = new ArrayList<>();
			loadNormalMessages();
		} else { //Conference
			loadConferenceMessages();
		}
	}

	public void loadNormalMessages() {
		App app = getApp();
		if (app != null && !isFetchingMessages) {
			isFetchingMessages = true;
			app.getJobManager(App.JobQueueName.Network).addJobInBackground(new GetNormalMessagesJob());
		}
	}

	public void loadConferenceMessages() {
		App app = getApp();
		if (app != null) {
			swipeRefresh.setRefreshing(true);
			app.getJobManager(App.JobQueueName.Network).addJobInBackground(new GetMessageConferenceJob(
					messageType == MessageAdapter.P2PType.Normal ? GET_NORMAL_MESSAGE_JOB_TAG : GET_CONFERENCE_MESSAGE_JOB_TAG));
		}
	}


	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(GetMessageConferenceJob.Success response) {
		if (messageType == MessageAdapter.P2PType.Conference && response.getTAG().equals(GET_CONFERENCE_MESSAGE_JOB_TAG)) {
			swipeRefresh.setRefreshing(true);
			List<GetMessageConferenceResponse> profileMessageList = response.getResponse();
			fetchedMessages = new ArrayList<>();

			if (profileMessageList.size() > 0) {

				//loop through all the profiles and fetch their jobs
				for (GetMessageConferenceResponse profileMessage : profileMessageList) {
					ProfileMessageModel model = profileMessage.getLastMessage();
					int readStatus;
					if (model.getReadTime() != null || model.isSentMyBe()) {
						readStatus = MessagesDetailFragment.MessageStatus.Read.ordinal();
					} else {
						readStatus = MessagesDetailFragment.MessageStatus.Unread.ordinal();
					}

					fetchedMessages.add(new GeneralMessageModel(profileMessage.getUserId(),
							model.getSentTime(),
							10,
							profileMessage.getName(),
							model.getMessage(),
							profileMessage.getUserProfileImageUrl(),
							readStatus));

					//Save profile message
					try {
						ProfileMessageDao profileMessageDao = getApp().getDatabaseHelper().getProfileMessageDao();
						//List<ProfileMessage> profileMessagesList = profileMessageDao.queryForProfileId(profileMessage.getUserId());

						List<ProfileMessageModel> singleHistoryList = new ArrayList<>();
						singleHistoryList.add(profileMessage.getLastMessage());
						String messageHistoryString = new Gson().toJson(singleHistoryList);

						profileMessageDao.saveResponseAsProfileMessage(profileMessage.getUserId(), profileMessage.getName(), profileMessage.getUserProfileImageUrl(), messageHistoryString);

					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				Log.i("MessageFragment", "GetMessageConferenceJob, fetchedMessages size: " + String.valueOf(fetchedMessages.size()));
			} else {
				if (swipeRefresh.isRefreshing()) {
					swipeRefresh.setRefreshing(false);
				}
			}

			loadMessagesAndRefreshAdapter(null);
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(GetMessageConferenceJob.GetMessageConferenceJobError response) {
		if (swipeRefresh.isRefreshing()) {
			swipeRefresh.setRefreshing(false);
		}

		Log.e("MessageFragment", "Failed to get a profile messages..loading normal messages");
		fetchedMessages = new ArrayList<>();
		loadNormalMessages();
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(GetNormalMessagesJob.GetAllMessagesJobSuccess response) {
		loadMessagesAndRefreshAdapter(response);
	}

	public void loadMessagesAndRefreshAdapter(GetNormalMessagesJob.GetAllMessagesJobSuccess response) {
		swipeRefresh.setRefreshing(false);

		List<GeneralMessageModel> messageModelList = fetchedMessages;
		if (messageType == MessageAdapter.P2PType.Normal && response != null) {
			messageModelList.addAll(response.getAllMessagesModel().getMessages());
		}

		if (messageModelList.size() > 0) {
			noMessagesText.setVisibility(View.INVISIBLE);
			Log.i("MessageFragment", "BEFORE Setting message to list: " + messageModelList.toString());
			messageAdapter.setMessageList(getApp(), messageModelList); //Saves in DB here
			Log.i("MessageFragment", "AFTER Setting message to list: " + messageModelList.toString());

			//Set the current selected message
			if (isLandscape) {
				GeneralMessageModel newSelectedMessage = messageAdapter.getMessageList().get(getCurrentSelectedIndex());
				messageAdapter.setCurrentMessage(newSelectedMessage);
			} else {
				int newMessageId = messageFragmentListener.getNewMessageIdAndResetId();
				if (newMessageId != -1) {
					List<GeneralMessageModel> messageList = messageAdapter.getMessageList();
					for (int i = 0; i < messageList.size(); i++) {
						GeneralMessageModel currentMessage = messageList.get(i);
						if (currentMessage.getAppUserMessageId() == newMessageId) {
							setCurrentSelectedIndex(i);
							messageAdapter.setCurrentMessage(currentMessage);
							break;
						}
					}
				}
			}
			messageAdapter.notifyDataSetChanged();
		} else {
			messageAdapter.resetMessageList();
			noMessagesText.setVisibility(View.VISIBLE);
		}
		isFetchingMessages = false;
		loadMessageFromNotification();
	}

	public void loadMessageFromNotification() {
		App app = getApp();
		if (app != null) {
			int messageId = app.getP2PMessageId();
			if (messageId != 0) {
				List<GeneralMessageModel> messageList = messageAdapter.getMessageList();
				if (messageList != null) {
					for (int i = 0; i < messageList.size(); i++) {
						if(messageId == messageList.get(i).getAppUserMessageId()) {
							messageFragmentListener.messageClicked(messageList.get(i));
							break;
						}
					}
				}

				app.saveP2PMessageId(0);
			}
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(GetNormalMessagesJob.GetAllMessagesJobError response) {
		swipeRefresh.setRefreshing(false);
		Log.e("MessageFragment", "Failed to get all messages");
		isFetchingMessages = false;
	}

	public void updateMessageState(GeneralMessageModel updatedMessage) {
		messageAdapter.setMessageInListAsRead(updatedMessage);
		messageAdapter.notifyDataSetChanged();
	}

	public void refreshAdapter() {
		messageAdapter.notifyDataSetChanged();
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SetMessagesStatusDeletedJob.MessageDeletedSuccess response) {
		deleteMessage(response.getDeleteMessage());
		App app = getApp();
		if (app != null) {
			app.getEventBus().post(new ShowSnackBar(getString(R.string.messages_delete_deleted)));
		}
	}

	public void deleteMessage(GeneralMessageModel updatedMessage) {
		removeBufferedMessage(updatedMessage);
		messageAdapter.deleteMessage(updatedMessage);
		messageAdapter.notifyDataSetChanged();
		List<GeneralMessageModel> messageList = messageAdapter.getMessageList();

		if (isLandscape) {
			if (messageList.size() < getCurrentSelectedIndex() + 1) {
				if (messageList.size() == 0) {
					if (isLandscape) {
						messageFragmentListener.deleteMessageDetailInLandscape();
					}
					return;
				} else {
					noMessagesText.setVisibility(View.INVISIBLE);
					setCurrentSelectedIndex(messageList.size() - 1);
				}
			}

			GeneralMessageModel newSelectedMessage = messageList.get(getCurrentSelectedIndex());
			messageAdapter.setCurrentMessage(newSelectedMessage);
			messageAdapter.notifyDataSetChanged();
		}

		if (messageList.size() == 0) {
			noMessagesText.setVisibility(View.VISIBLE);
		}
	}

	public void loadNewMessage(int appUserMessageId) {
		App app = getApp();
		if (app != null) {
			try {
				swipeRefresh.setRefreshing(true);
				app.getJobManager(App.JobQueueName.Network).addJobInBackground(new GetSingleMessagesJob(appUserMessageId));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(GetSingleMessagesJob.Success response) {
		swipeRefresh.setRefreshing(false);
		AllMessagesModel messagesModel = response.getAllMessagesModel();
		GeneralMessageModel message;
		if (messagesModel.getMessages().size() != 0) {
			message = messagesModel.getMessages().get(0);
			messageFragmentListener.handleNewMessage(message);
			messageFragmentListener.getNewMessageIdAndResetId(); //resets the id so that we don't load the message again
			messageAdapter.addNewMessage(message);

			if (isLandscape) {
				messageAdapter.setCurrentMessage(message);
				messageAdapter.notifyDataSetChanged();
			}
			messageAdapter.setMessageInListAsRead(message);

			saveBufferedMessageToDB(message);
		} else {
			noMessagesText.setVisibility(View.INVISIBLE);
		}
	}

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(GetSingleMessagesJob.Failed response) {
		swipeRefresh.setRefreshing(false);
	}

	/**
	 * Saves all messages that has been fetched from GetSingleMessage
	 * @param message
	 */
	public void saveBufferedMessageToDB(GeneralMessageModel message) {
		App app = getApp();
		if (app != null) {
			try {
				NewMessageBufferDao newMessageBufferDao = app.getDatabaseHelper().getNewMessageBufferDao();
				newMessageBufferDao.saveMessageModelAsNewMessage(getActivity(), message);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void removeBufferedMessage(GeneralMessageModel message) {
		App app = getApp();
		if (app != null) {
			try {
				NewMessageBufferDao newMessageBufferDao = app.getDatabaseHelper().getNewMessageBufferDao();
				newMessageBufferDao.removeNewMessageFromDownloads(message.getAppUserMessageId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void clearNotificationCount(){
		App app = getApp();
		if (app != null) {
			SharedPreferences sharedPrefs = app.getSharedPreferences();
			int notificationCount = sharedPrefs.getInt(MyFcmMessagingService.KEY_NOTIFICATION_COUNT, 0);

			if (notificationCount > 0) {
				sharedPrefs.edit().putInt(MyFcmMessagingService.KEY_NOTIFICATION_COUNT, 0).apply();
			}
		}
	}

	public static class MessageModelComparator implements Comparator<GeneralMessageModel> {
		public int compare(GeneralMessageModel left, GeneralMessageModel right) {
			return right.getDate().compareTo(left.getDate());
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			messageFragmentListener = (MessageFragmentListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement MessageFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		messageFragmentListener = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		App app = getApp();
		if (app != null && app.getEventBus().isRegistered(this)) {
			app.getEventBus().unregister(this);
		}
	}

	public interface MessageFragmentListener {
		void messageClicked(GeneralMessageModel message);

		void handleNewMessage(GeneralMessageModel message);

		void deleteMessageDetailInLandscape();

		int getNewMessageIdAndResetId();
	}
}
