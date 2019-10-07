package com.ibamembers.search;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.ibamembers.app.BaseFragment;
import com.ibamembers.search.favourites.ProfileSnippet;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class ProfileSnippetFragment extends BaseFragment {

	protected static final int TAKE_COUNT = 1000;

	private ProfileSnippetAdapter profileSnippetAdapter;
	private ProfileSnippetFragmentListener profileSnippetFragmentListener;
	private boolean shouldShowSelected;
	private LinearLayoutManager linearLayoutManager;
	protected int skipIndex;
	protected boolean loadMore = true;
	protected boolean isConference;

	protected abstract RecyclerView getRecyclerView();

	public void setIsConference(boolean conference) {
		isConference = conference;
	}

	protected void setUpRecycler(List<ProfileSnippet> profileSnippets, String jobTag, boolean isContainHeaderRow) {
		Activity activity = getActivity();
		if (activity != null) {
			RecyclerView recyclerView = getRecyclerView();
			if (profileSnippetAdapter == null) {
				profileSnippetAdapter = new ProfileSnippetAdapter(profileSnippets, shouldShowSelected, jobTag, isContainHeaderRow);
			}
			linearLayoutManager = new LinearLayoutManager(activity);
			recyclerView.setLayoutManager(linearLayoutManager);
			recyclerView.setAdapter(profileSnippetAdapter);

			recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					if (profileSnippetAdapter.getItemCount() != 0) {
						if (linearLayoutManager.findLastCompletelyVisibleItemPosition() != 0 &&
								linearLayoutManager.findLastCompletelyVisibleItemPosition() == profileSnippetAdapter.getItemCount() - 1 &&
								loadMore) {
							getProfile();
						}
					}
				}
			});
		}
	}

	public void getProfile() {}

	public void setShouldShowSelected(boolean shouldShowSelected) {
		if (profileSnippetAdapter != null) {
			profileSnippetAdapter.setShouldShowSelected(shouldShowSelected);
		} else {
			this.shouldShowSelected = shouldShowSelected;
		}
	}

	protected void setProfileSnippets(List<ProfileSnippet> profileSnippets) {
		profileSnippetAdapter.setProfileSnippetModels(profileSnippets);
	}

	protected void appendProfileSnippets(List<ProfileSnippet> profileSnippets) {
		profileSnippetAdapter.appendProfileSnippetModels(profileSnippets);
	}

	public ProfileSnippetAdapter getProfileSnippetAdapter() {
		return profileSnippetAdapter;
	}

	protected List<ProfileSnippet> getSnippetsIfPresent() {
		if (profileSnippetAdapter != null) {
			return profileSnippetAdapter.getProfileSnippets();
		} else {
			return new ArrayList<>();
		}
	}

	public ProfileSnippet getSelectedProfileSnippet() {
		ProfileSnippet selectedSnippet = null;

		if (profileSnippetAdapter != null) {
			selectedSnippet = profileSnippetAdapter.getSelectedProfileSnippet();
		}

		return selectedSnippet;
	}

	protected class ProfileSnippetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private static final int VIEW_PROFILE = 0;
		private static final int VIEW_HEADER = 1;

		private List<ProfileSnippet> profileSnippets;
		private int oldSelectedPos;
		private boolean shouldShowSelected;
		private boolean isContainHeaderRow;
		String jobTag;

		public ProfileSnippetAdapter(List<ProfileSnippet> profileSnippets, boolean shouldShowSelected, String jobTag, boolean isContainHeaderRow) {
			this.profileSnippets = profileSnippets;
			this.shouldShowSelected = shouldShowSelected;
			this.jobTag = jobTag;
			this.oldSelectedPos = -1;
			this.isContainHeaderRow = isContainHeaderRow;
		}

		public void setProfileSnippetModels(List<ProfileSnippet> profileSnippets) {
			this.profileSnippets = profileSnippets;
			notifyDataSetChanged();
		}

		public void appendProfileSnippetModels(List<ProfileSnippet> profileSnippets) {
			this.profileSnippets.addAll(profileSnippets);
			notifyDataSetChanged();
		}

		public void setNewSelected(int newSelectedPos) {
			if (oldSelectedPos != -1 && oldSelectedPos < getItemCount()) {
				profileSnippets.get(oldSelectedPos).setIsSelected(false);
				notifyItemChanged(oldSelectedPos);
				oldSelectedPos = -1;
			}

			if (newSelectedPos != -1) {
				profileSnippets.get(newSelectedPos).setIsSelected(true);
				notifyItemChanged(newSelectedPos);
				oldSelectedPos = newSelectedPos;
			}
		}

		public ProfileSnippet getSelectedProfileSnippet() {
			if (oldSelectedPos != -1) {
				return profileSnippets.get(oldSelectedPos);
			}
			return null;
		}

		public void setShouldShowSelected(boolean shouldShowSelected) {
			this.shouldShowSelected = shouldShowSelected;
			notifyDataSetChanged();
		}

		public boolean isShouldShowSelected() {
			return shouldShowSelected;
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			if (viewType == VIEW_HEADER) {
				return new ProfileSnippetHeaderViewHolder(inflater, parent);
			} else {
				return new ProfileSnippetViewHolder(inflater, parent);
			}
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			if (!isContainHeaderRow && position >= 0) {
				((ProfileSnippetViewHolder) holder).fillView(profileSnippets.get(position));
			} else if (isContainHeaderRow && position > 0) {
				((ProfileSnippetViewHolder) holder).fillView(profileSnippets.get(position - 1));
			}
		}

		@Override
		public int getItemViewType(int position) {
			if (isContainHeaderRow && position == 0) {
				return VIEW_HEADER;
			} else {
				return VIEW_PROFILE;
			}
		}

		@Override
		public int getItemCount() {
			return isContainHeaderRow ? profileSnippets.size() + 1 : profileSnippets.size();
		}

		public void snippetClicked(int position) {
			newSnippetClicked(position);
		}

		public void selectFirstItemIfNoneSelected() {
			boolean noneSelected = true;

			for (ProfileSnippet profileSnippet : profileSnippets) {
				if (profileSnippet.isSelected()) {
					noneSelected = false;
				}
			}

			if (noneSelected && profileSnippets.size() > 0) {
				newSnippetClicked(0);
			}
		}

		public void newSnippetClicked(int position) {
			int newPosition = position;

			if (isContainHeaderRow) {
				newPosition--;
			}

			if (!(newPosition < 0)) {
				if (newPosition < profileSnippets.size()) {
					profileSnippetFragmentListener.profileSnippetClicked(profileSnippets.get(newPosition), jobTag);
					setNewSelected(newPosition);
				}
			} else {
				profileSnippetFragmentListener.profileSnippetClicked(null, jobTag);
			}
		}

		public List<ProfileSnippet> getProfileSnippets() {
			return profileSnippets;
		}

		protected class ProfileSnippetViewHolder extends RecyclerView.ViewHolder {

			@SuppressWarnings("WeakerAccess")
			@BindView(R.id.profile_snippet_full_view)
			ConstraintLayout fullView;

//			@SuppressWarnings("WeakerAccess")
//			@BindView(R.id.attending_members_layout)
//			RelativeLayout attendingMembersBadge;

			@SuppressWarnings("WeakerAccess")
			@BindView(R.id.profile_snippet_user_name)
			TextView userName;

			@SuppressWarnings("WeakerAccess")
			@BindView(R.id.profile_snippet_firm_name)
			TextView firmName;

			@SuppressWarnings("WeakerAccess")
			@BindView(R.id.profile_snippet_job_role)
			TextView jobRole;

			@SuppressWarnings("WeakerAccess")
			@BindView(R.id.profile_snippet_picture)
			ImageView profilePicture;

			@SuppressWarnings("WeakerAccess")
			@BindView(R.id.profile_snippet_picture_progress_bar)
			ProgressBar profilePictureProgressBar;

			@SuppressWarnings("WeakerAccess")
			@BindView(R.id.view_profile_button)
			TextView viewProfileButton;

			public ProfileSnippetViewHolder(LayoutInflater inflater, ViewGroup parent) {
				super(inflater.inflate(R.layout.profile_snippet, parent, false));
				ButterKnife.bind(this, itemView);
				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						newSnippetClicked(getAdapterPosition());
					}
				});
			}

			public void fillView(final ProfileSnippet profileSnippet){
				App app = getApp();
				Activity activity = getActivity();
				if (app != null && activity != null) {
					Resources resources = getResources();
					final String fullName = app.getDataManager().getFullName(profileSnippet.getFirstName(), profileSnippet.getLastName());
					String firmName = profileSnippet.getFirmName();
					String jobRole = profileSnippet.getJobPosition();
					byte[] imageDataBytes = profileSnippet.getImageData();
					String profilePictureUrl = profileSnippet.getProfilePicture();

					if (profileSnippet.isSelected() && isShouldShowSelected()) {
						fullView.setBackgroundColor(ContextCompat.getColor(activity, R.color.searchResultProfileSelected));
					} else {
						fullView.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.white));
					}

					if (imageDataBytes != null) {
						loadImage(imageDataBytes, profilePicture);
					}
					else if (profilePictureUrl != null) {
						loadImage(profilePictureUrl, profilePicture);
					}
					else {
						setNoPicture();
					}

					if (isConference) userName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.content_detail_description_font));

					if (!TextUtils.isEmpty(fullName)) {
						this.userName.setText(fullName);
					} else {
						this.userName.setText(resources.getString(R.string.profile_no_name));
					}

					this.firmName.setVisibility(isConference ? View.GONE : View.VISIBLE);
					if (!TextUtils.isEmpty(firmName)) {
						this.firmName.setText(firmName);
					} else {
						this.firmName.setText("");
					}

					if (!TextUtils.isEmpty(firmName)) {
						this.jobRole.setText(firmName);
					} else {
						this.jobRole.setText("");
					}

					viewProfileButton.setTextColor(ContextCompat.getColor(itemView.getContext(), isConference ? R.color.conference_main_close: R.color.colorAccent));
					viewProfileButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							startActivity(SearchProfileActivity.getSearchProfileActivityIntent(getContext(), profileSnippet.getId(), fullName, true));
						}
					});
				}
			}

			private void setNoPicture() {
				Activity activity = getActivity();
				if (activity != null) {
					profilePictureProgressBar.setVisibility(View.INVISIBLE);
					if (isConference) {
						profilePicture.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.profile_image_placeholder_conference));
					} else {
						profilePicture.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.profile_image_placeholder));
					}

				}
			}

			private void loadImage(byte[] imageData, ImageView imageView) {
				Activity activity = getActivity();
				if (activity != null) {
					Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
					profilePictureProgressBar.setVisibility(View.INVISIBLE);
					imageView.setImageBitmap(bitmap);
				}
			}

			private void loadImage(String imageUrl, ImageView imageView) {
				final Activity activity = getActivity();

				if (imageUrl.equals("N/A")) {
					setNoPicture();
					return;
				}

				if (activity != null) {
					profilePictureProgressBar.setVisibility(View.VISIBLE);

					RequestOptions options = new RequestOptions();
					options.centerCrop();
					options.diskCacheStrategy(DiskCacheStrategy.NONE);

					RequestListener requestListener = new RequestListener() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
							setNoPicture();
							return false;
						}

						@Override
						public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
							profilePictureProgressBar.setVisibility(View.INVISIBLE);
							return false;
						}
					};

					Glide.with(activity)
							.load(imageUrl)
							.apply(options)
							.listener(requestListener)
							.into(imageView);
				}
			}
		}

		protected class ProfileSnippetHeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

			@SuppressWarnings("WeakerAccess")
			@BindView(R.id.header_text)
			protected TextView favouritesTab;

			public ProfileSnippetHeaderViewHolder(LayoutInflater inflater, ViewGroup parent) {
				super(inflater.inflate(R.layout.tab_header_row, parent, false));
				ButterKnife.bind(this, itemView);
				favouritesTab.setText(getString(R.string.toolbar_title_favourites));
				itemView.setClickable(true);
				itemView.setOnClickListener(this);
			}

			@Override
			public void onClick(View v) {
				profileSnippetFragmentListener.profileHeaderSnippetClicked();
			}
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			profileSnippetFragmentListener = (ProfileSnippetFragmentListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement ProfileSnippetFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		profileSnippetFragmentListener = null;
	}

	public interface ProfileSnippetFragmentListener {
		void profileSnippetClicked(ProfileSnippet profileSnippet, String jobTag);

		void profileHeaderSnippetClicked();
	}
}
