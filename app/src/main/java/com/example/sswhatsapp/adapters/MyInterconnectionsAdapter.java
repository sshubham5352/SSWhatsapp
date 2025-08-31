package com.example.sswhatsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sswhatsapp.R;
import com.example.sswhatsapp.databinding.ItemRvMyInterconnectionsBinding;
import com.example.sswhatsapp.listeners.MyInterconnectionsAdapterListener;
import com.example.sswhatsapp.models.MyInterconnectionRvItem;
import com.example.sswhatsapp.models.responses.ChatItemResponse;
import com.example.sswhatsapp.models.responses.UserDetailsResponse;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;
import com.example.sswhatsapp.utils.TimeHandler;

import java.util.List;

public class MyInterconnectionsAdapter extends RecyclerView.Adapter<MyInterconnectionsAdapter.ViewHolder> {
    //Field Declaration
    Context mContext;
    MyInterconnectionsAdapterListener mListener;
    List<MyInterconnectionRvItem> interconnectionsList;
    String myUserId;


    public MyInterconnectionsAdapter(Context context, MyInterconnectionsAdapterListener listener, List<MyInterconnectionRvItem> interconnectionsList, String myUserId) {
        mContext = context;
        mListener = listener;
        this.interconnectionsList = interconnectionsList;
        this.myUserId = myUserId;
    }

    @Override
    public int getItemViewType(int position) {
        return Constants.LAYOUT_TYPE_MY_INTERCONNECTION;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRvMyInterconnectionsBinding binding = ItemRvMyInterconnectionsBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new MyInterconnectionsAdapter.ViewHolder(binding, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyInterconnectionRvItem currentItem = interconnectionsList.get(position);

        if (currentItem.getReceiverUserDetails() != null) {
            setReceiverUserDetails(currentItem.getReceiverUserDetails(), holder);
        }

        if (currentItem.getLastChatItem() != null) {
            setLastChatItemDetails(currentItem.getLastChatItem(), holder);
            holder.binding.lastChat.setVisibility(View.VISIBLE);
            holder.binding.lastMsgTimeStamp.setVisibility(View.VISIBLE);
        } else {
            holder.binding.lastChat.setVisibility(View.INVISIBLE);
            holder.binding.lastMsgTimeStamp.setVisibility(View.INVISIBLE);
            holder.binding.lastMsgReadStatus.setVisibility(View.GONE);
        }

        if (currentItem.getUnseenChatCount() > 0) {
            setUnseenChatCount(currentItem.getUnseenChatCount(), holder);
            holder.binding.lastMsgTimeStamp.setTextColor(mContext.getColor(R.color.colorBlue));
            holder.binding.numberOfUnreadMessages.setVisibility(View.VISIBLE);
        } else {
            holder.binding.lastMsgTimeStamp.setTextColor(mContext.getColor(R.color.colorDarkGray));
            holder.binding.numberOfUnreadMessages.setVisibility(View.INVISIBLE);
        }
    }

    private void setReceiverUserDetails(UserDetailsResponse userDetails, ViewHolder holder) {
        if (Helper.isNill(userDetails.getProfileImgUrl())) {
            holder.binding.imgUserProfile.setImageResource(Helper.getProfilePlaceholderImg(mContext, userDetails.gender));
        } else {
            Glide.with(mContext)
                    .load(userDetails.getProfileImgUrl())
                    .placeholder(Helper.getProfilePlaceholderImg(mContext, userDetails.gender))
                    .error(Helper.getProfilePlaceholderImg(mContext, userDetails.gender))
                    .into(holder.binding.imgUserProfile);
        }
        holder.binding.userName.setText(userDetails.getLocalPhoneName());
    }


    private void setLastChatItemDetails(ChatItemResponse chatItem, ViewHolder holder) {
        if (chatItem.getChatCategory() == Constants.CHAT_CATEGORY_MSG) {
            holder.binding.lastChat.setText(chatItem.getMessage());
        }

        if (chatItem.senderId.matches(myUserId)) {
            holder.binding.lastMsgReadStatus.setBackgroundResource(Helper.getChatStatusDarkDrawable(chatItem.getChatStatus()));
            holder.binding.lastMsgReadStatus.setVisibility(View.VISIBLE);
        } else {
            holder.binding.lastMsgReadStatus.setVisibility(View.GONE);
        }
        holder.binding.lastMsgTimeStamp.setText(TimeHandler.getInterconnectionItemTimeStamp(chatItem.getTimeStamp()));
    }

    private void setUnseenChatCount(long unseenChatCount, ViewHolder holder) {
        if (unseenChatCount < 100) {
            holder.binding.numberOfUnreadMessages.setText(String.valueOf(unseenChatCount));
        } else {
            holder.binding.numberOfUnreadMessages.setText(mContext.getString(R.string.ninety_nine_plus));
        }
    }

    @Override
    public int getItemCount() {
        return interconnectionsList.size();
    }

    //INNER CLASS
    static class ViewHolder extends RecyclerView.ViewHolder {
        //Field declaration
        ItemRvMyInterconnectionsBinding binding;

        public ViewHolder(@NonNull ItemRvMyInterconnectionsBinding binding, MyInterconnectionsAdapterListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            setOnClickListeners(listener);
        }

        private void setOnClickListeners(MyInterconnectionsAdapterListener listener) {
            binding.rootLayout.setOnClickListener(view -> {
                if (listener == null || getAdapterPosition() == RecyclerView.NO_POSITION)
                    return;
                listener.onInterconnectionRvItemClick(getAdapterPosition());
            });
        }
    }
}
