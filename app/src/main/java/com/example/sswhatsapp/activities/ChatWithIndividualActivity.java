package com.example.sswhatsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.sswhatsapp.R;
import com.example.sswhatsapp.adapters.ChatWithIndividualAdapter;
import com.example.sswhatsapp.daos.ChatWithIndividualDao;
import com.example.sswhatsapp.databinding.ActivityChatWithIndividualBinding;
import com.example.sswhatsapp.listeners.ChatWIthIndividualDaoListener;
import com.example.sswhatsapp.listeners.ChatWithIndividualAdapterListener;
import com.example.sswhatsapp.models.responses.ChatItemResponse;
import com.example.sswhatsapp.models.responses.InterConnectionResponse;
import com.example.sswhatsapp.models.responses.UserDetailsResponse;
import com.example.sswhatsapp.services.FCMService;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;
import com.example.sswhatsapp.utils.SessionManager;
import com.example.sswhatsapp.utils.SoundManager;

public class ChatWithIndividualActivity extends AppCompatActivity implements View.OnClickListener, ChatWithIndividualAdapterListener, ChatWIthIndividualDaoListener {
    //fields
    private ActivityChatWithIndividualBinding binding;
    ChatWithIndividualDao chatDao;
    private ChatWithIndividualAdapter chatsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_with_individual);
        initDao();
        initToolbar();
        initChatAdapter();

        if (!chatDao.isEradicated()) {
            //make call to fetch previous chats
            getPreviousChats();
        } else {
            //as there are no previous chats so the Rv should stack items fromTop
            changeRvStackingOrder(false);
        }

        //setting onClickListeners
        binding.btnSendMessage.setOnClickListener(this);
        binding.btnMic.setOnClickListener(this);

        // setting onEditorAction
        setMessageTextChangeListener();

        //onBackPressed
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onActivityBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatDao.updateMyLiveOnChatStatus(true);
        chatDao.attachReceiverDocListener();
        chatDao.attachChatsCollectionListener();
        chatDao.attachChatParticipantListener();
        FCMService.clearNotification(chatDao.getReceiverUser().getUserId());
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatDao.updateMyLiveOnChatStatus(false);
        chatDao.detachChatParticipantListener();
        chatDao.detachChatsCollectionListener();
    }

    void onActivityBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.INTENT_MY_INTERCONNECTION_EXTRA, chatDao.getMyInterconnection());
        setResult(RESULT_OK, intent);

        if (!chatDao.getChatsList().isEmpty()) {
            intent.putExtra(Constants.INTENT_LAST_CHAT_ITEM_EXTRA, chatDao.getChatsList().get(chatDao.getLastChatItemIndex()));
        }

        if (isTaskRoot()) {
            intent.setClass(this, HomeActivity.class);
            intent.putExtra(Constants.INTENT_COMING_FROM_NOTIFICATION_EXTRA, true);
            startActivity(intent);
        }
        finish();
    }

    private void initDao() {
        Intent intent = getIntent();
        UserDetailsResponse receiverUserDetails = (UserDetailsResponse) intent.getSerializableExtra(Constants.INTENT_USER_DETAILS_EXTRA);
        InterConnectionResponse myInterconnection = (InterConnectionResponse) intent.getSerializableExtra(Constants.INTENT_MY_INTERCONNECTION_EXTRA);
        InterConnectionResponse receiversInterconnection = (InterConnectionResponse) intent.getSerializableExtra(Constants.INTENT_RECEIVERS_INTERCONNECTION_EXTRA);

        if (!SessionManager.isInitiated()) {
            SessionManager.initSessionManager(getApplicationContext());
        }

        if (!SoundManager.isInitiated()) {
            SoundManager.initSoundManager(getApplicationContext());
        }
        UserDetailsResponse senderUserDetails = SessionManager.getUser();
        chatDao = new ChatWithIndividualDao(this, this, senderUserDetails, receiverUserDetails, myInterconnection, receiversInterconnection);
    }

    private void initToolbar() {
        UserDetailsResponse receiverUser = chatDao.getReceiverUser();
        binding.toolbar.setNavigationOnClickListener(view -> onActivityBackPressed());
        if (receiverUser.getProfileImgUrl() != null) {
            Glide.with(this)
                    .load(receiverUser.getProfileImgUrl())
                    .placeholder(Helper.getProfilePlaceholderImg(this, receiverUser.gender))
                    .error(Helper.getProfilePlaceholderImg(this, receiverUser.gender))
                    .into(binding.imgUserProfile);
        } else {
            binding.imgUserProfile.setImageResource(Helper.getProfilePlaceholderImg(this, receiverUser.getGender()));
        }
        binding.userName.setText(receiverUser.getLocalPhoneName());
    }

    private void initChatAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        binding.rvChats.setLayoutManager(layoutManager);
        chatsAdapter = new ChatWithIndividualAdapter(this, this, chatDao.getChatsList(), chatDao.getMyUserId());
        binding.rvChats.setAdapter(chatsAdapter);

        binding.rvChats.addItemDecoration(new ChatWithIndividualAdapter.HeaderItemDecoration(this, binding.rvChats, new ChatWithIndividualAdapter.HeaderItemDecoration.StickyHeaderInterface() {
            @Override
            public boolean isHeader(int itemPosition) {
                return chatDao.getChatsList().get(itemPosition).getChatCategory() == Constants.LAYOUT_TYPE_BANNER_DATE;
            }

            @Override
            public int getHeaderPositionForChatItem(int chatItemPosition) {
                for (int i = chatItemPosition; i >= 0; i--) {
                    if (chatDao.getChatsList().get(i).getChatCategory() == Constants.LAYOUT_TYPE_BANNER_DATE) {
                        return i;
                    }
                }
                return chatItemPosition;
            }

            @Override
            public void bindHeaderData(ChatWithIndividualAdapter.BannerDateHolder bannerDateHolder, int headerPosition) {
                bannerDateHolder.binding.bannerDate.setText(chatDao.getChatsList().get(headerPosition).getDateBannerTitle());
            }
        }));

        binding.rvChats.getRecycledViewPool().setMaxRecycledViews(Constants.LAYOUT_TYPE_CHAT_MSG_SENT, 0);
        binding.rvChats.getRecycledViewPool().setMaxRecycledViews(Constants.LAYOUT_TYPE_CHAT_MSG_RECEIVED, 0);
    }

    private void setMessageTextChangeListener() {
        TextWatcher listener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    // field got empty
                    chatDao.updateMyTypingStatus(false);
                    startAnimOnStopTyping();
                } else {
                    // user is writing something
                    chatDao.updateMyTypingStatus(true);
                    startAnimOnStartTyping();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        binding.message.addTextChangedListener(listener);
    }

    //ANIM
    private void startAnimOnStartTyping() {
        if (binding.btnSendMessage.getVisibility() == View.GONE) {
            Animation micBtnAnim = AnimationUtils.loadAnimation(this, R.anim.anim_shrink_gone);
            binding.btnMic.setVisibility(View.GONE);
            binding.btnMic.startAnimation(micBtnAnim);

            Animation sendBtnAnim = AnimationUtils.loadAnimation(this, R.anim.anim_expand_visible);
            binding.btnSendMessage.setVisibility(View.VISIBLE);
            binding.btnSendMessage.startAnimation(sendBtnAnim);
            binding.btnSendMessage.setClickable(true);
        }

        if (binding.btnCamera.getVisibility() == View.VISIBLE) {
            Animation cameraBtnAnim = AnimationUtils.loadAnimation(this, R.anim.anim_slide_right_gone);
            binding.btnCamera.setVisibility(View.GONE);
            binding.btnCamera.startAnimation(cameraBtnAnim);
        }
    }

    //ANIM
    private void startAnimOnStopTyping() {
        Animation sendBtnAnim = AnimationUtils.loadAnimation(this, R.anim.anim_shrink_gone);
        binding.btnSendMessage.startAnimation(sendBtnAnim);
        binding.btnSendMessage.setVisibility(View.GONE);
        binding.btnSendMessage.setClickable(false);

        Animation micBtnAnim = AnimationUtils.loadAnimation(this, R.anim.anim_expand_visible);
        binding.btnMic.setVisibility(View.VISIBLE);
        binding.btnMic.startAnimation(micBtnAnim);

        Animation cameraBtnAnim = AnimationUtils.loadAnimation(this, R.anim.anim_slide_bottom_visible);
        binding.btnCamera.setVisibility(View.VISIBLE);
        binding.btnCamera.startAnimation(cameraBtnAnim);
    }

    private void scrollChatRvToBottom() {
        binding.rvChats.scrollToPosition(chatDao.getChatsList().size() - 1);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_send_message) {
            String message = binding.message.getText().toString().trim();
            binding.message.setText(null);

            if (message.length() != 0) {
                ChatItemResponse chatItem = chatDao.addMessageChat(message);
                chatDao.sendMessageChat(chatItem);
                chatDao.sendNotificationV1(chatItem);
            }
        } else if (view.getId() == R.id.btn_mic) {
            return;
        }
    }

    //CALL FROM DAO
    @Override
    public void dateBannerAdded(int position) {
        chatsAdapter.notifyItemInserted(position);
    }

    //CALL FROM DAO
    @Override
    public void chatItemsAdded(int position) {
        chatsAdapter.notifyItemInserted(position);
        scrollChatRvToBottom();
    }

    //CALL FROM DAO
    @Override
    public void chatItemsAdded(int startPosition, int chatCount) {
        chatsAdapter.notifyItemRangeInserted(startPosition, chatCount);
    }

    //CALL FROM DAO
    @Override
    public void chatSentSuccess(int position) {
        chatsAdapter.notifyItemChanged(position);
        SoundManager.playChatSentSound();
    }

    //CALL FROM DAO
    @Override
    public void chatSentFailure(int position) {
        chatsAdapter.notifyItemChanged(position);
    }

    //CALL FROM DAO
    @Override
    public void chatReceivedSuccess(int position) {
        chatsAdapter.notifyItemInserted(position);
        SoundManager.playChatReceivedSound();
        scrollChatRvToBottom();
    }

    //CALL FROM DAO
    @Override
    public void chatItemUpdated(int position) {
        chatsAdapter.notifyItemChanged(position);
    }

    //CALL FROM DAO
    @Override
    public void receiverTypingStatusUpdated(boolean isTyping) {
        if (isTyping) {
            binding.userAvailabilityStatus.setText(getString(R.string.status_typing));
        } else {
            binding.userAvailabilityStatus.setText(getString(R.string.status_online));
        }
    }

    //CALL FROM DAO
    @Override
    public void receiverOnlineStatusUpdated(boolean isOnline, String lastOnline) {
        if (isOnline) {
            if (!binding.userAvailabilityStatus.getText().toString().matches(getString(R.string.status_typing))) {
                binding.userAvailabilityStatus.setText(getString(R.string.status_online));
            }
            binding.userAvailabilityStatus.setVisibility(View.VISIBLE);
        } else {
            if (lastOnline != null) {
                binding.userAvailabilityStatus.setText(lastOnline);
                binding.userAvailabilityStatus.setVisibility(View.VISIBLE);
            } else {
                binding.userAvailabilityStatus.setVisibility(View.GONE);
            }
        }
    }


    //CALL FROM DAO
    @Override
    public void hideLoadingAnimation() {
        binding.animChatLoading.setVisibility(View.GONE);
        binding.animChatLoading.pauseAnimation();
    }

    //CALL FROM DAO
    @Override
    public void changeRvStackingOrder(boolean stackFromEnd) {
        ((LinearLayoutManager) binding.rvChats.getLayoutManager()).setStackFromEnd(stackFromEnd);
    }

    //CALL FROM ADAPTER
    @Override
    public void getPreviousChats() {
        if (!chatDao.areAllPreviousChatsFetched()) {
            binding.animChatLoading.playAnimation();
            binding.animChatLoading.setVisibility(View.VISIBLE);
            chatDao.fetchPreviousChats();
        }
    }

    @Override
    protected void onDestroy() {
        chatDao.onDestroy();
        chatDao = null;
        super.onDestroy();
    }
}
