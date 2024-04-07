package com.example.sswhatsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.adapters.ChatWithIndividualAdapter;
import com.example.sswhatsapp.daos.ChatWithIndividualDao;
import com.example.sswhatsapp.databinding.ActivityChatWithIndividualBinding;
import com.example.sswhatsapp.listeners.ChatWIthIndividualDaoListener;
import com.example.sswhatsapp.listeners.ChatWithIndividualAdapterListener;
import com.example.sswhatsapp.models.ChatItemResponse;
import com.example.sswhatsapp.models.InterConnection;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;
import com.example.sswhatsapp.utils.PicassoCache;
import com.example.sswhatsapp.utils.SessionManager;

import java.util.List;

public class ChatWithIndividualActivity extends AppCompatActivity implements View.OnClickListener, ChatWithIndividualAdapterListener, ChatWIthIndividualDaoListener {
    //fields
    private ActivityChatWithIndividualBinding binding;
    ChatWithIndividualDao chatDao;
    private ChatWithIndividualAdapter chatsAdapter;
    private List<ChatItemResponse> chatsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_with_individual);
        initDao();
        initToolbar();
        chatsList = chatDao.getChatsList();
        initChatAdapter();

        if (!chatDao.isEradicated()) {
            //make call to fetch previous chats
            chatDao.fetchAllChats();
        }

        //setting onClickListeners
        binding.btnSendMessage.setOnClickListener(this);
        binding.btnMic.setOnClickListener(this);

        // setting onEditorAction
        setMessageTextChangeListener();
        setRvLayoutChangeListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatDao.attachChatReceiverListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatDao.detachChatReceiverListener();
    }

    private void initDao() {
        Intent intent = getIntent();
        UserDetailsResponse receiverUserDetails = (UserDetailsResponse) intent.getSerializableExtra(Constants.INTENT_USER_DETAILS_EXTRA);
        InterConnection myInterconnection = (InterConnection) intent.getSerializableExtra(Constants.INTENT_MY_INTERCONNECTION_EXTRA);
        InterConnection receiversInterconnection = (InterConnection) intent.getSerializableExtra(Constants.INTENT_RECEIVERS_INTERCONNECTION_EXTRA);

        UserDetailsResponse senderUserDetails = SessionManager.getUser();

        chatDao = new ChatWithIndividualDao(this, this,
                senderUserDetails, receiverUserDetails, myInterconnection, receiversInterconnection);
    }

    private void initToolbar() {
        UserDetailsResponse connectionWithUser = chatDao.getReceiverUser();
        binding.toolbar.setNavigationOnClickListener(view -> finish());
        PicassoCache.getPicassoInstance(this).load(connectionWithUser.getProfileImgUrl()).
                placeholder(Helper.getProfilePlaceholderImg(this, connectionWithUser.getGender()))
                .into(binding.imgUserProfile);
        binding.userName.setText(connectionWithUser.getName());
    }

    private void initChatAdapter() {
//        binding.rvSsContacts.addItemDecoration(new SSUsersListAdapter.SpacingItemDecoration(getResources().getDimensionPixelSize(R.dimen.space_between_rv_user_items)));
        binding.rvChats.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        chatsAdapter = new ChatWithIndividualAdapter(this, this, chatsList, chatDao.getMyUserId());
        binding.rvChats.setAdapter(chatsAdapter);
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
                    startAnimOnStopTyping();
                } else {
                    // user is writing something
                    startAnimOnStartTyping();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        binding.message.addTextChangedListener(listener);
    }

    private void setRvLayoutChangeListener() {
        binding.rvChats.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> scrollChatRvToBottom());
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
        if (chatsList.size() != 0)
            binding.rvChats.smoothScrollToPosition(chatsList.size() - 1);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_send_message) {
            String message = binding.message.getText().toString().trim();
            binding.message.setText(null);

            if (message.length() != 0) {
                ChatItemResponse chatItem = chatDao.addMessageChat(message);
                chatDao.sendMessageChat(chatItem);
                chatDao.sendNotification(chatItem);
            }
        } else if (view.getId() == R.id.btn_mic) {
            return;
        }
    }

    @Override
    public void dateBannerAdded(int position) {
        chatsAdapter.notifyItemInserted(position);
    }

    @Override
    public void chatAdded(int position) {
        chatsAdapter.notifyItemInserted(position);
    }

    @Override
    public void allChatsAdded(int startPosition, int chatCount) {
        chatsAdapter.notifyItemRangeInserted(startPosition, chatCount);
    }


    @Override
    public void chatSentSuccess(int position) {
        chatsAdapter.notifyItemChanged(position);
    }

    @Override
    public void chatSentFailure(int position) {
        chatsAdapter.notifyItemChanged(position);
    }

    @Override
    public void chatReceivedSuccess(int position) {
        chatsAdapter.notifyItemInserted(position);
    }
}
