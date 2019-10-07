package com.ibamembers.messages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.ibamembers.app.IBAUtils;
import com.ibamembers.messages.db.NewMessage;
import com.ibamembers.messages.db.NewMessageBufferDao;
import com.ibamembers.messages.job.GeneralMessageModel;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_GENERAL_MESSAGE = 100;
	private static final int TYPE_PROFILE_MESSAGE = 101;

	private ViewGroup parent;
	private MessagesFragment.MessageFragmentListener listener;
	private boolean isLandscape;
	private int currentSelectedIndex;
	private P2PType p2pType;

	private List<GeneralMessageModel> messageList;
	private MessagesDetailFragment.MessageStatus previouslySelectedStatus;
	private MessagesDetailFragment.MessageStatus currentlySelectedStatus;
	private GeneralMessageModel currentlySelectedMessage;
	private GeneralMessageModel previouslySelectedMessage;

	public enum P2PType {
		Normal,
		Conference
	}

	public void setCurrentSelectedIndex(int currentSelectedIndex) {
		this.currentSelectedIndex = currentSelectedIndex;
	}

	public int getCurrentSelectedIndex() {
		return currentSelectedIndex;
	}

	public MessageAdapter(List<GeneralMessageModel> messageList, MessagesFragment.MessageFragmentListener listener, boolean isLandscape, P2PType messageType) {
		this.messageList = messageList;
		this.listener = listener;
		this.isLandscape = isLandscape;
		this.p2pType = messageType;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		this.parent = parent;
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		if (p2pType == P2PType.Normal) {
			return new MessageViewHolder(inflater, parent, parent.getContext());
		} else {
			return new ConferenceMessageViewHolder(inflater, parent, parent.getContext());
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		((BaseMessageViewHolder) holder).fillView(messageList.get(position));

	}

	@Override
	public int getItemCount() {
		return messageList.size();
	}

	@Override
	public int getItemViewType(int position) {
		if (messageList.get(position) instanceof GeneralMessageModel) {
			return TYPE_GENERAL_MESSAGE;
		} else {
			return TYPE_PROFILE_MESSAGE;
		}
	}

	public void resetMessageList() {
		messageList = new ArrayList<>();
		notifyDataSetChanged();
	}

	public void loadMessageList(App app) {
		boolean isMessageInNewList;

		if (app != null) {
			try {
				NewMessageBufferDao newMessageBufferDao = app.getDatabaseHelper().getNewMessageBufferDao();

				List<NewMessage> newMessageListDB = newMessageBufferDao.queryForAll();
				List<GeneralMessageModel> messageModelList = new ArrayList<>(newMessageListDB.size());
				for (NewMessage newMessage : newMessageListDB) {
					messageModelList.add(NewMessageBufferDao.convertNewMessageToMessageModel(app, newMessage));
				}

				Collections.sort(messageModelList, new MessagesFragment.MessageModelComparator());
				this.messageList = messageModelList;
				notifyDataSetChanged();

			} catch (SQLException | ParseException e) {
				e.printStackTrace();
			}


		}
	}

	public void setMessageList(App app, List<GeneralMessageModel> newMessageList) {
		boolean isMessageInNewList;

		if (app != null) {
			try {
				app.getDatabaseHelper().clearNewMessageBufferDao();
				NewMessageBufferDao newMessageBufferDao = app.getDatabaseHelper().getNewMessageBufferDao();

				for (GeneralMessageModel currentMessages : newMessageList) {
					newMessageBufferDao.saveMessageModelAsNewMessage(app, currentMessages);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

//		if (app != null) {
//			try {
//				//Fetch all saved single messages from DB
//				NewMessageBufferDao newMessageBufferDao = app.getDatabaseHelper().getNewMessageBufferDao();
//				List<NewMessage> newMessageListDB = newMessageBufferDao.queryForAll();
//				List<GeneralMessageModel> messageModelList = new ArrayList<>(newMessageListDB.size());
//				for (NewMessage newMessage : newMessageListDB) {
//					messageModelList.add(NewMessageBufferDao.convertNewMessageToMessageModel(parent.getContext(), newMessage));
//				}
//
//				//check if that saved message exists in the GetAllList
//				for (GeneralMessageModel bufferedMessage : messageModelList) {
//					isMessageInNewList = false;
//					for (GeneralMessageModel currentMessages : newMessageList) {
//						//Remove saved message if it exists
//						if (currentMessages.getAppUserMessageId() == bufferedMessage.getAppUserMessageId()) {
//							removeBufferedMessage(app, currentMessages);
//							isMessageInNewList = true;
//							break;
//						}
//					}
//
//					//if not then we append that message to the new list
//					if (!isMessageInNewList) {
//						newMessageList.add(0, bufferedMessage);
//					}
//				}
//			} catch (SQLException | ParseException e) {
//				e.printStackTrace();
//			}
//		}
//
//		//refresh the new message list

		Collections.sort(newMessageList, new MessagesFragment.MessageModelComparator());

		this.messageList = newMessageList;
	}

	public void removeBufferedMessage(App app, GeneralMessageModel message) {
		if (app != null) {
			try {
				NewMessageBufferDao newMessageBufferDao = app.getDatabaseHelper().getNewMessageBufferDao();
				newMessageBufferDao.removeNewMessageFromDownloads(message.getAppUserMessageId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}


	public void addNewMessage(GeneralMessageModel newMessage) {
		if (messageList.size() > 0) {
			boolean isMessageExit = false;
			for (GeneralMessageModel currentMessages : messageList) {
				if (currentMessages.getAppUserMessageId() == newMessage.getAppUserMessageId()) {
					isMessageExit = true;
					break;
				}
			}
			if (!isMessageExit) {
				messageList.add(0, newMessage);
				notifyItemInserted(0);
			}
		} else {
			messageList.add(newMessage);
			notifyItemInserted(0);
		}
	}

	public List<GeneralMessageModel> getMessageList() {
		return messageList;
	}

	public void deleteMessage(GeneralMessageModel messageModel) {
		for (int i = 0; i < messageList.size(); i++) {
			GeneralMessageModel message = messageList.get(i);
			if (message.getAppUserMessageId() == messageModel.getAppUserMessageId()) {
				messageList.remove(i);
				break;
			}
		}
	}

	public void setMessageInListAsRead(GeneralMessageModel messageModel) {
		for (int i = 0; i < messageList.size(); i++) {
			GeneralMessageModel message = messageList.get(i);
			if (message.getAppUserMessageId() == messageModel.getAppUserMessageId()) {
				messageList.get(i).setStatus(MessagesDetailFragment.MessageStatus.Read.ordinal());
				break;
			}
		}
	}

	/**
	 * Selects the current view with the given MessageModel.
	 * Note: The messageStatus is not affected
	 *
	 * @param currentMessage Message to view
	 */
	public void setCurrentMessage(GeneralMessageModel currentMessage) {
		listener.messageClicked(currentMessage);

		if (currentlySelectedMessage != null) {
			previouslySelectedMessage = currentlySelectedMessage;
			previouslySelectedStatus = previouslySelectedMessage.getStatus();
		}
		currentlySelectedMessage = currentMessage;
		currentlySelectedStatus = currentlySelectedMessage.getStatus();
	}

	public class MessageViewHolder extends BaseMessageViewHolder {

		public MessageViewHolder(LayoutInflater inflater, ViewGroup parent, Context context) {
			super(inflater, parent, context);
		}

		@Override
		int getFontColor() { return R.color.colorPrimary;}

		@Override
		int getImagePlaceholder() {
			return R.drawable.profile_image_placeholder;
		}

	}

	public class ConferenceMessageViewHolder extends BaseMessageViewHolder {

		public ConferenceMessageViewHolder(LayoutInflater inflater, ViewGroup parent, Context context) {
			super(inflater, parent, context);
		}

		@Override
		int getFontColor() { return R.color.content_detail_description_font;}

		@Override
		int getImagePlaceholder() {
			return R.drawable.profile_image_placeholder_conference;
		}
	}

	abstract public class BaseMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_layout)
		protected RelativeLayout messageLayout;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_profile_image_layout)
		protected RelativeLayout profileImageLayout;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_profile_image)
		protected ImageView profileImage;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_date)
		protected TextView messageDateTextView;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_title)
		protected TextView messageTitle;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_description)
		protected TextView messageDescription;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.message_state_bar)
		protected ImageView messageStatusBar;

//		@SuppressWarnings("WeakerAccess")
//		@BindView(R.id.attending_members_layout)
//		protected RelativeLayout attendingMessageLayout;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.separator)
		protected View layoutSeparator;

		@SuppressWarnings("WeakerAccess")
		@BindView(R.id.separator_no_image)
		protected View layoutSeparatorForNoImage;


		private Context context;
		private GeneralMessageModel currentMessage;
		private SimpleDateFormat apiDateFormat;

		abstract int getFontColor();
		abstract int getImagePlaceholder();

		public BaseMessageViewHolder(LayoutInflater inflater, ViewGroup parent, Context context) {
			super(inflater.inflate(R.layout.message_view_holder, parent, false));
			this.context = context;
			ButterKnife.bind(this, itemView);
			itemView.setOnClickListener(this);
			apiDateFormat = new SimpleDateFormat(context.getString(R.string.default_api_date_format), Locale.getDefault());
			apiDateFormat.setTimeZone(TimeZone.getDefault());
		}

		public void fillView(GeneralMessageModel message) {
			this.currentMessage = message;
			MessagesDetailFragment.MessageStatus messageStatus = message.getStatus();
			String title = message.getTitle();
			String description = message.getText();
			Date messageDate = message.getDate();
			MessagesDetailFragment.MessageType messageType = message.getMessageType();
			String profileUrl = message.getUrl();

			String finalDateString = IBAUtils.formatMessageTime(context, messageDate, false);
			if (!TextUtils.isEmpty(finalDateString)) {
				messageDateTextView.setText(finalDateString);
			}

			if (!TextUtils.isEmpty(title)) {
				messageTitle.setText(title);
			}

			messageTitle.setTextColor(ContextCompat.getColor(context, getFontColor()));

			if (!TextUtils.isEmpty(description)) {
				messageDescription.setText(description);
			} else {
				messageDescription.setText("");
			}

			if (messageStatus == MessagesDetailFragment.MessageStatus.Unread) {
				messageStatusBar.setVisibility(View.VISIBLE);

			} else {
				messageStatusBar.setVisibility(View.INVISIBLE);
			}

			//TODO need to clarify this. Might have been message status for push messages
//                if (previouslySelectedMessage != null) {
//                    if (previouslySelectedMessage.getAppUserMessageId() == currentMessage.getAppUserMessageId() &&
//                            previouslySelectedStatus == MessagesDetailFragment.MessageStatus.Unread &&
//                            messageList.get(getAdapterPosition()).getStatus() == MessagesDetailFragment.MessageStatus.Read) {
//                        messageStatusBar.setVisibility(View.INVISIBLE);
//                    }
//                }
//
//                if (currentlySelectedMessage != null) {
//                    if (currentlySelectedMessage.getAppUserMessageId() == currentMessage.getAppUserMessageId() && currentlySelectedStatus == MessagesDetailFragment.MessageStatus.Unread) {
//                        messageStatusBar.setVisibility(View.VISIBLE);
//                    }
//                }

			if (isLandscape && currentlySelectedMessage != null && currentlySelectedMessage.getAppUserMessageId() == message.getAppUserMessageId()) {
				messageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.content_item_clicked_background));
			} else {
				messageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.content_item_normal_background));
			}

			if (messageType == MessagesDetailFragment.MessageType.Profile) {
				profileImageLayout.setVisibility(View.VISIBLE);
				//attendingMessageLayout.setVisibility(View.GONE);
				layoutSeparator.setVisibility(View.VISIBLE);
				layoutSeparatorForNoImage.setVisibility(View.GONE);

				RelativeLayout.LayoutParams params =  (RelativeLayout.LayoutParams) messageStatusBar.getLayoutParams();
				params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.separator);
			} else {
				profileImageLayout.setVisibility(View.GONE);
				layoutSeparator.setVisibility(View.GONE);
				layoutSeparatorForNoImage.setVisibility(View.VISIBLE);

				RelativeLayout.LayoutParams params =  (RelativeLayout.LayoutParams) messageStatusBar.getLayoutParams();
				params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.separator_no_image);
			}

			loadImage(profileUrl, messageType);
		}

		@Override
		public void onClick(View v) {
			GeneralMessageModel message = messageList.get(getAdapterPosition());
			currentSelectedIndex = getAdapterPosition();

			setCurrentMessage(message);
			setMessageInListAsRead(message);
			notifyDataSetChanged();
		}

		@SuppressLint("CheckResult")
		private void loadImage(String imageUrl, MessagesDetailFragment.MessageType messageType) {
			profileImage.setImageResource(getImagePlaceholder());

			if (!TextUtils.isEmpty(imageUrl) && messageType == MessagesDetailFragment.MessageType.Profile) {
				imageUrl = context.getString(R.string.profile_image_prefix) + imageUrl;

				RequestOptions options = new RequestOptions();
				options.centerCrop();
				options.diskCacheStrategy(DiskCacheStrategy.RESOURCE);
				options.skipMemoryCache(true);

				RequestListener requestListener = new RequestListener() {
					@Override
					public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
						new android.os.Handler().post(new Runnable() {
							@Override
							public void run() {
								profileImage.setImageResource(getImagePlaceholder());
							}
						});
						return false;
					}

					@Override
					public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
						return false;
					}
				};

				Glide.with(context)
						.load(imageUrl)
						.apply(options)
						.listener(requestListener)
						.into(profileImage);
			}
		}
	}
}
