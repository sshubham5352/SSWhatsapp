package com.example.sswhatsapp.adapters;

import android.content.Context;
import android.graphics.Canvas;
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

        switch (getItemViewType(position)) {
            case Constants.LAYOUT_TYPE_BANNER_DATE: {
                BannerDateHolder bannerDateHolder = (BannerDateHolder) holder;
                bannerDateHolder.binding.bannerDate.setText(chatItem.getDateBannerTitle());
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

        //CALL FOR CONTROLLER
        if (position == 0) {
            mListener.fetchPreviousChats();
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

    @Override
    public long getItemId(int position) {
        return 0;
    }

    //VIEW HOLDER CLASSES
    public static class BannerDateHolder extends RecyclerView.ViewHolder {
        //Field declaration
        public ItemRvChatDateBannerBinding binding;

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

    //RV ITEM DECORATION CLASSES
    public static class HeaderItemDecoration extends RecyclerView.ItemDecoration {
        //FIELDS
        private final Context mContext;
        private final LayoutInflater layoutInflater;
        private final BannerDateHolder currentHeader;
        private final StickyHeaderInterface mListener;
        private final Runnable dateHeaderHideoutRunnable;
        private final int DATE_HEADER_HIDEOUT_MILLIS = 1000;
        int lastItemIndex;

        //CONSTRUCTOR
        public HeaderItemDecoration(Context context, RecyclerView rv, @NonNull StickyHeaderInterface listener) {
            mContext = context;
            mListener = listener;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            currentHeader = getHeaderViewForItem(rv);
            lastItemIndex = -1;


            dateHeaderHideoutRunnable = () -> {
                currentHeader.binding.bannerDate.setVisibility(View.GONE);
                rv.invalidate();
            };

            addRvOnScrollListener(rv);
        }

        private void addRvOnScrollListener(RecyclerView rv) {
            rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        rv.postDelayed(dateHeaderHideoutRunnable, DATE_HEADER_HIDEOUT_MILLIS);
                    } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        rv.removeCallbacks(dateHeaderHideoutRunnable);
                        currentHeader.binding.bannerDate.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }

        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.onDrawOver(c, parent, state);
            View topChild = parent.getChildAt(0);
            if (topChild == null) {
                return;
            }

            int topChildPosition = parent.getChildAdapterPosition(topChild);
            if (topChildPosition == RecyclerView.NO_POSITION) {
                return;
            }

            if (mListener.isHeader(topChildPosition)) {
                mListener.bindHeaderData(currentHeader, topChildPosition);
            } else {
                mListener.bindHeaderData(currentHeader, mListener.getHeaderPositionForChatItem(topChildPosition));
            }

            fixLayoutSize(parent, currentHeader.binding.getRoot());
            int contactPoint = currentHeader.binding.getRoot().getBottom();
            View childInContact = getChildInContact(parent, contactPoint);
            if (childInContact == null) {
                return;
            }

            if (mListener.isHeader(parent.getChildAdapterPosition(childInContact))) {
                pushCurrentWithNewHeader(c, currentHeader.binding.getRoot(), childInContact);
                return;
            }
            drawHeader(c, currentHeader.binding.getRoot());
        }

        private BannerDateHolder getHeaderViewForItem(RecyclerView parent) {
            ItemRvChatDateBannerBinding binding = ItemRvChatDateBannerBinding.inflate(layoutInflater, parent, false);
            return new BannerDateHolder(binding);
        }

        private void drawHeader(Canvas c, View header) {
            c.save();
            c.translate(0, 0);
            header.draw(c);
            c.restore();
        }

        private void pushCurrentWithNewHeader(Canvas c, View currentHeader, View nextHeader) {
            c.save();
            c.translate(0, nextHeader.getTop() - currentHeader.getHeight());
            currentHeader.draw(c);
            c.restore();
        }

        private View getChildInContact(RecyclerView parent, int contactPoint) {
            View childInContact = null;
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child.getBottom() > contactPoint) {
                    if (child.getTop() <= contactPoint) {
                        // This child overlaps the contactPoint
                        childInContact = child;
                        break;
                    }
                }
            }
            return childInContact;
        }

        /**
         * Properly measures and layouts the top sticky header.
         *
         * @param parent ViewGroup: RecyclerView in this case.
         */
        private void fixLayoutSize(ViewGroup parent, View view) {
            // Specs for parent (RecyclerView)
            int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

            // Specs for children (headers)
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, 0, view.getLayoutParams().width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, 0, view.getLayoutParams().height);

            view.measure(childWidthSpec, childHeightSpec);

            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        public interface StickyHeaderInterface {
            boolean isHeader(int itemPosition);

            /**
             * This method gets called by {@link HeaderItemDecoration} to fetch the position of the header item in the adapter
             * that is used for (represents) item at specified position.
             *
             * @param position int. Adapter's position of the item for which to do the search of the position of the header item.
             * @return int. Position of the header item in the adapter.
             */
            int getHeaderPositionForChatItem(int position);

            void bindHeaderData(ChatWithIndividualAdapter.BannerDateHolder bannerDateHolder, int headerPosition);
        }
    }
}
