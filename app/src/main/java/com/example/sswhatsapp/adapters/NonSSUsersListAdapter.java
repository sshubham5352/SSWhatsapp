package com.example.sswhatsapp.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sswhatsapp.databinding.ItemRvContactNotOnSsBinding;
import com.example.sswhatsapp.listeners.NonSSUsersListListener;
import com.example.sswhatsapp.models.UserDeviceContact;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;

import java.util.List;

public class NonSSUsersListAdapter extends RecyclerView.Adapter<NonSSUsersListAdapter.ViewHolder> {
    //Field Declaration
    Context mContext;
    NonSSUsersListListener mListener;
    LayoutInflater inflater;
    List<UserDeviceContact> contactList;
    int totalViewCreate = 0;
    int viewLoaded = 0;
    int dynamicSize;

    public NonSSUsersListAdapter(Context context, NonSSUsersListListener listener, List<UserDeviceContact> usersList) {
        mContext = context;
        mListener = listener;
        this.contactList = usersList;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dynamicSize = Constants.RV_PAGE_ITEMS_COUNT;
        if (dynamicSize > usersList.size())
            dynamicSize = usersList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        totalViewCreate++;

        ItemRvContactNotOnSsBinding binding = ItemRvContactNotOnSsBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        viewLoaded++;
        UserDeviceContact currentItem = contactList.get(position);
        setFadeInAnimation(holder.itemView);

        Helper.setText(currentItem.getName(), currentItem.getMobileNo(), holder.binding.name, true);
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
    public boolean isListCompleted() {
        return dynamicSize == contactList.size();
    }

    public void showNextPage() {
        int prevSize = dynamicSize;
        dynamicSize += Constants.RV_PAGE_ITEMS_COUNT;
        if (dynamicSize > contactList.size())
            dynamicSize = contactList.size();
        notifyItemRangeInserted(prevSize, dynamicSize - prevSize);
    }

    private void onItemClick(int position) {
        mListener.onNonSSUserClick(contactList.get(position));
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        //Field declaration
        ItemRvContactNotOnSsBinding binding;

        public ViewHolder(@NonNull ItemRvContactNotOnSsBinding binding) {
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