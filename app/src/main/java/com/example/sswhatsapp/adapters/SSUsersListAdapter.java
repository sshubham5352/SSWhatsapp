package com.example.sswhatsapp.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sswhatsapp.databinding.ItemRvContactOnSsBinding;
import com.example.sswhatsapp.listeners.SSUsersListListener;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;
import com.example.sswhatsapp.utils.PicassoCache;

import java.util.List;

public class SSUsersListAdapter extends RecyclerView.Adapter<SSUsersListAdapter.ViewHolder> {
    //Field Declaration
    Context mContext;
    SSUsersListListener mListener;
    LayoutInflater inflater;
    List<UserDetailsResponse> usersList;

    public UserDetailsResponse selectedUser;
    int dynamicSize;

    public SSUsersListAdapter(Context context, SSUsersListListener listener, List<UserDetailsResponse> usersList) {
        mContext = context;
        mListener = listener;
        this.usersList = usersList;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        dynamicSize = Constants.RV_PAGE_ITEMS_COUNT;
        if (dynamicSize > usersList.size())
            dynamicSize = usersList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRvContactOnSsBinding binding = ItemRvContactOnSsBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDetailsResponse currentItem = usersList.get(position);
        setFadeInAnimation(holder.itemView);

        PicassoCache.getPicassoInstance(mContext).load(currentItem.getProfileImgUrl()).
                placeholder(Helper.getProfilePlaceholderImg(mContext, usersList.get(position).gender))
                .into(holder.binding.imgUserProfile);
        Helper.setText(currentItem.getName(), holder.binding.name, true);
        Helper.setText(currentItem.getTagline(), holder.binding.tagline, true);

        if (position == usersList.size() - 1)
            mListener.onSSUsersListCompletelyShown();
    }

    private void setFadeInAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        view.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return dynamicSize;
    }

    //returns true if all the items in the list are shown, otherwise false
    public boolean isListCompletelyShown() {
        return dynamicSize == usersList.size();
    }

    public void showNextPage() {
        int prevSize = dynamicSize;
        dynamicSize += Constants.RV_PAGE_ITEMS_COUNT;
        if (dynamicSize > usersList.size())
            dynamicSize = usersList.size();
        notifyItemRangeInserted(prevSize, dynamicSize - prevSize);
    }

    //on SS contact click listener
    private void onItemClick(int position) {
        selectedUser = usersList.get(position);
        mListener.onSSUserClick(usersList.get(position));
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        //Field declaration
        ItemRvContactOnSsBinding binding;

        public ViewHolder(@NonNull ItemRvContactOnSsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setOnClickListeners();
        }

        private void setOnClickListeners() {
            binding.rootLayout.setOnClickListener(view -> {
                if (mListener != null && getAdapterPosition() != RecyclerView.NO_POSITION)
                    onItemClick(getAdapterPosition());
            });
        }
    }

    public static class SpacingItemDecoration extends RecyclerView.ItemDecoration {
        int topSpace;

        public SpacingItemDecoration(int topSpace) {
            this.topSpace = topSpace;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            if (parent.getChildAdapterPosition(view) != 0) {
                outRect.top = topSpace;
            }
        }
    }
}