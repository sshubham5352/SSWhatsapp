package com.example.sswhatsapp.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.databinding.ItemRvChatDateBannerBinding;
import com.example.sswhatsapp.databinding.ItemRvChatMsgReceivedBinding;
import com.example.sswhatsapp.databinding.ItemRvChatMsgSentBinding;
import com.example.sswhatsapp.listeners.ChatWithIndividualAdapterListener;
import com.example.sswhatsapp.models.ChatItemResponse;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.TimeHandler;

import java.util.List;

public class ChatWithIndividualAdapter extends RecyclerView.Adapter {
    //Field Declaration
    static final int NULL_LAYOUT = -1;
    static int SPACE_BETWEEN_CHATS = 30;
    Context mContext;
    ChatWithIndividualAdapterListener mListener;
    LayoutInflater inflater;
    List<ChatItemResponse> mChatsList;
    String mUserId;
    int loadedViewCount;


    public ChatWithIndividualAdapter(Context context, ChatWithIndividualAdapterListener listener, List<ChatItemResponse> chatList, String userId) {
        mContext = context;
        mListener = listener;
        this.mChatsList = chatList;
        mUserId = userId;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        loadedViewCount = -1;
    }


    @Override
    public int getItemViewType(int position) {
        if (position < 0)
            return NULL_LAYOUT;

        ChatItemResponse chatItemResponse = mChatsList.get(position);

        switch (chatItemResponse.getChatCategory()) {
            case Constants.CHAT_CATEGORY_MSG: {
                if (chatItemResponse.getSenderId().matches(mUserId)) {
                    return Constants.LAYOUT_TYPE_CHAT_MSG_SENT;
                } else {
                    return Constants.LAYOUT_TYPE_CHAT_MSG_RECEIVED;
                }
            }

            case Constants.LAYOUT_TYPE_BANNER_DATE: {
                return Constants.LAYOUT_TYPE_BANNER_DATE;
            }
        }
        return NULL_LAYOUT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case Constants.LAYOUT_TYPE_BANNER_DATE: {
                ItemRvChatDateBannerBinding binding = ItemRvChatDateBannerBinding.inflate(layoutInflater, parent, false);
                return new BannerDateHolder(binding);
            }
            case Constants.LAYOUT_TYPE_CHAT_MSG_SENT: {
                ItemRvChatMsgSentBinding binding = ItemRvChatMsgSentBinding.inflate(layoutInflater, parent, false);
                return new ChatMsgSentHolder(binding);
            }
            case Constants.LAYOUT_TYPE_CHAT_MSG_RECEIVED: {
                ItemRvChatMsgReceivedBinding binding = ItemRvChatMsgReceivedBinding.inflate(layoutInflater, parent, false);
                return new ChatMsgReceivedHolder(binding);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItemResponse chatItem = mChatsList.get(position);
        if (position > loadedViewCount) {
            setFadeInAnimation(holder.itemView);
            loadedViewCount++;
        }

        switch (getItemViewType(position)) {
            case Constants.LAYOUT_TYPE_BANNER_DATE: {
                BannerDateHolder bannerDateHolder = (BannerDateHolder) holder;
                bannerDateHolder.binding.bannerDate.setText(chatItem.getTimeStamp());
                break;
            }
            case Constants.LAYOUT_TYPE_CHAT_MSG_SENT: {
                ChatMsgSentHolder msgSentHolder = (ChatMsgSentHolder) holder;
                msgSentHolder.setBackground(getItemViewType(position - 1));
                msgSentHolder.binding.msgReadStatus.setBackgroundResource(getChatStatusDrawable(chatItem.getChatStatus()));
                msgSentHolder.binding.chatTimeStamp.setText(TimeHandler.getChatTimeStamp(chatItem.getTimeStamp()));
                msgSentHolder.binding.message.setText(chatItem.getMessage());
                break;
            }
            case Constants.LAYOUT_TYPE_CHAT_MSG_RECEIVED: {
                ChatMsgReceivedHolder msgReceivedHolder = (ChatMsgReceivedHolder) holder;
                msgReceivedHolder.setBackground(getItemViewType(position - 1));
                msgReceivedHolder.binding.chatTimeStamp.setText(TimeHandler.getChatTimeStamp(chatItem.getTimeStamp()));
                msgReceivedHolder.binding.message.setText(chatItem.getMessage());
                break;
            }
        }
    }

    private int getChatStatusDrawable(int chatStatus) {
        switch (chatStatus) {
            case Constants.CHAT_STATUS_PENDING:
                return R.drawable.img_msg_loading_icon;
            case Constants.CHAT_STATUS_SENT:
                return R.drawable.img_single_tick_white;
            case Constants.CHAT_STATUS_RECEIVED:
                return R.drawable.img_double_tick_white;
            case Constants.CHAT_STATUS_READ:
                return R.drawable.img_double_tick_green;
            case Constants.CHAT_STATUS_HALTED:
                return R.drawable.img_red_cross;
        }
        return R.drawable.img_msg_loading_icon;
    }

    private void setFadeInAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(100);
        view.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return mChatsList.size();
    }


    //VIEW HOLDERS
    static class BannerDateHolder extends RecyclerView.ViewHolder {
        //Field declaration
        ItemRvChatDateBannerBinding binding;

        public BannerDateHolder(@NonNull ItemRvChatDateBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class ChatMsgSentHolder extends RecyclerView.ViewHolder {
        //Field declaration
        ItemRvChatMsgSentBinding binding;

        public ChatMsgSentHolder(@NonNull ItemRvChatMsgSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setOnLongPressListener();
        }

        private void setOnLongPressListener() {

        }

        public void setBackground(int previousItemViewType) {
            if (previousItemViewType != Constants.LAYOUT_TYPE_CHAT_MSG_SENT) {
                if (previousItemViewType != Constants.LAYOUT_TYPE_BANNER_DATE) {
                    /*
                     * Adding margin_top to the chat which are first in thread and not immediately
                     * after the date banner because date banner has it's own margins.
                     */
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.msgRootLayout.getLayoutParams();
                    params.topMargin = SPACE_BETWEEN_CHATS;
                    binding.msgRootLayout.setLayoutParams(params);
                }
                binding.msgRootLayout.setBackgroundResource(R.drawable.bg_chat_msg_sent_with_tail);
            }
        }
    }

    static class ChatMsgReceivedHolder extends RecyclerView.ViewHolder {
        //Field declaration
        ItemRvChatMsgReceivedBinding binding;

        public ChatMsgReceivedHolder(@NonNull ItemRvChatMsgReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            setOnLongPressListener();
        }

        private void setOnLongPressListener() {
        }

        public void setBackground(int previousItemViewType) {
            if (previousItemViewType != Constants.LAYOUT_TYPE_CHAT_MSG_RECEIVED) {
                if (previousItemViewType != Constants.LAYOUT_TYPE_BANNER_DATE) {
                    /*
                     * Adding margin_top to the chat which are first in thread and not immediately
                     * after the date banner because date banner has it's own margins.
                     */
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.msgRootLayout.getLayoutParams();
                    params.topMargin = SPACE_BETWEEN_CHATS;
                    binding.msgRootLayout.setLayoutParams(params);
                }

                binding.msgRootLayout.setBackgroundResource(R.drawable.bg_chat_msg_received_with_tail);

            }
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