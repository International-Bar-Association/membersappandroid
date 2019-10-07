package com.ibamembers.messages;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
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
import com.ibamembers.app.BaseFragment;
import com.ibamembers.messages.job.GeneralMessageModel;
import com.ibamembers.profile.message.ProfileMessageFragment;

import butterknife.ButterKnife;

public class MessagesHandlerFragment extends BaseFragment {

    private static final String TAG_MESSAGES = "TAG_MESSAGES";
    private static final String TAG_MESSAGE_DETAIL = "TAG_MESSAGE_DETAIL";
    private static final String TAG_PROFILE_MESSAGE_DETAIL = "TAG_PROFILE_MESSAGE_DETAIL";
    public static final String KEY_MESSAGE = "KEY_MESSAGE";

    private MessagesFragment messagesFragment;
    private MessagesDetailFragment messagesDetailFragment;
    private ProfileMessageFragment profileMessageFragment;
    private boolean isLandscape;
    private GeneralMessageModel currentMessage;
    private MenuItem deleteMenuItem;

    int currentMessageId = 0; //keeps track of both normal message and profile message

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_multipane_fragment, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        FrameLayout contentFrame = view.findViewById(R.id.message_content_fragment);
        isLandscape = contentFrame != null;

        return view;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isVisible() && isLandscape) {
            inflater.inflate(R.menu.message_detail_menu, menu);
            deleteMenuItem = menu.findItem(R.id.action_delete);
            if (messagesDetailFragment == null) {
                deleteMenuItem.setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            if (messagesDetailFragment != null) {
                messagesDetailFragment.confirmDelete();
            } else if (profileMessageFragment != null) {
                profileMessageFragment.confirmDelete();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            messagesFragment = (MessagesFragment) getFragmentManager().getFragment(savedInstanceState, TAG_MESSAGES);
            Fragment fragment = getFragmentManager().findFragmentById(R.id.message_content_fragment);

            if (fragment instanceof MessagesDetailFragment) {
                messagesDetailFragment = (MessagesDetailFragment) fragment;
            }
        }

        loadFragments(savedInstanceState != null ? savedInstanceState.getInt(KEY_MESSAGE) : -1);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_MESSAGE, messagesFragment.getCurrentSelectedIndex());
        getFragmentManager().putFragment(outState, TAG_MESSAGES, messagesFragment);
    }

    private void loadFragments(int currentlySelectedIndex) {
        messagesFragment = new MessagesFragment();
        if (currentlySelectedIndex != -1) {
            messagesFragment.setCurrentSelectedIndex(currentlySelectedIndex);
        }
        loadFragmentIntoContainer(messagesFragment, R.id.message_main_fragment, false, TAG_MESSAGES);

        //if (isLandscape && messagesFragment != null) {}
    }

    public boolean handleMessageClickedInLandscape(GeneralMessageModel message) {
        this.currentMessage = message;
        if (isLandscape) {
            App app = getApp();
            if (app != null) {
                if (shouldLoadSnippet(message)) {
                    if (message.getMessageType() != MessagesDetailFragment.MessageType.Profile) {
                        messagesDetailFragment = new MessagesDetailFragment();
                        messagesDetailFragment.setHasOptionsMenu(false);
                        messagesDetailFragment.setArguments(MessagesDetailFragment.getMessagesDetailFragmentArgs(new Gson().toJson(message)));
                        loadFragmentIntoContainer(messagesDetailFragment, R.id.message_content_fragment, false, TAG_MESSAGE_DETAIL);
                    } else {
                        profileMessageFragment = new ProfileMessageFragment();
                        profileMessageFragment.setHasOptionsMenu(false);
                        profileMessageFragment.setArguments(ProfileMessageFragment.getProfileMessageFragment(message.getAppUserMessageId(), message.getTitle(), true, false));
                        loadFragmentIntoContainer(profileMessageFragment, R.id.message_content_fragment, false, TAG_PROFILE_MESSAGE_DETAIL);
                    }

                    if (deleteMenuItem != null) {
                        deleteMenuItem.setVisible(true);
                    }
                }
            }
            return true;
        }else {
            return false;
        }
    }

    private boolean shouldLoadSnippet(GeneralMessageModel messageModel) {
        Fragment fragment1 = getFragmentManager().findFragmentByTag(TAG_MESSAGE_DETAIL);
        Fragment fragment2 = getFragmentManager().findFragmentByTag(TAG_PROFILE_MESSAGE_DETAIL);

        if (fragment1 != null) {
            currentMessageId = ((MessagesDetailFragment) fragment1).getCurrentMessageId();
        } else if (fragment2 != null){
            currentMessageId = ((ProfileMessageFragment) fragment2).getCurrentMessageId();
        }

        return currentMessageId != messageModel.getAppUserMessageId();
    }

    public void setMessageAsRead() {
        if (messagesFragment != null && currentMessage != null) {
            if (currentMessage.getStatus() == MessagesDetailFragment.MessageStatus.Unread) {
                currentMessage.setStatus(MessagesDetailFragment.MessageStatus.Read.ordinal());
                messagesFragment.updateMessageState(currentMessage);
            }
        }
    }

    public void loadNewMessage(int id) {
        if (messagesFragment != null) {
            messagesFragment.loadNewMessage(id);
        }
    }

    public void loadMessages() {
        if (messagesFragment != null) {
            messagesFragment.loadMessages();
        }
    }

    private void loadFragmentIntoContainer(Fragment fragment, @IdRes int containerId, boolean isSearchFilter, @Nullable String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerId, fragment, tag);
        fragmentTransaction.commit();
    }

    public void clearMessageDetailFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.message_content_fragment);
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }
    }
}
