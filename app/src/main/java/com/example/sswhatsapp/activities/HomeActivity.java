package com.example.sswhatsapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.adapters.MyInterconnectionsAdapter;
import com.example.sswhatsapp.adapters.SSUsersAdapter;
import com.example.sswhatsapp.daos.HomeDao;
import com.example.sswhatsapp.databinding.ActivityHomeBinding;
import com.example.sswhatsapp.listeners.HomeDaoListener;
import com.example.sswhatsapp.listeners.MyInterconnectionsAdapterListener;
import com.example.sswhatsapp.models.MyInterconnectionRvItem;
import com.example.sswhatsapp.models.responses.ChatItemResponse;
import com.example.sswhatsapp.models.responses.InterConnectionResponse;
import com.example.sswhatsapp.services.FCMService;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Permissions;
import com.example.sswhatsapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, MyInterconnectionsAdapterListener, HomeDaoListener {
    //fields
    private ActivityHomeBinding binding;
    private HomeDao homeDao;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private MyInterconnectionsAdapter myInterconnectionsAdapter;
    private boolean isComingFromNotification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initDao();
        getIntentData();

        if (Permissions.isReadContactsGranted(this, true)) {
            onRequestedPermissionsGranted();
        }

        //setting activityLauncher
        initActivityLauncher();

        //setting onClickListener
        binding.fabAllChats.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeDao.attachChatCollectionListeners();
        homeDao.attachMyInterconnectionsListener();
        FCMService.clearAllNotifications();
    }

    @Override
    protected void onPause() {
        homeDao.detachChatCollectionListeners();
        homeDao.detachMyInterconnectionsListener();
        super.onPause();
    }

    private void initDao() {
        homeDao = new HomeDao(this, SessionManager.getUser(), this);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        isComingFromNotification = intent.getBooleanExtra(Constants.INTENT_COMING_FROM_NOTIFICATION_EXTRA, false);
        ArrayList<InterConnectionResponse> interConnectionResponseList = (ArrayList<InterConnectionResponse>) intent.getSerializableExtra(Constants.INTENT_MY_INTERCONNECTIONS_LIST_EXTRA);

        if (interConnectionResponseList != null) {
            homeDao.addInterConnections(interConnectionResponseList);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Permissions.PERMISSION_READ_CONTACTS_CODE) {
            if (grantResults[0] == Permissions.PERMISSION_GRANTED) {
                onRequestedPermissionsGranted();
            } else {
                Toast.makeText(this, "Permission not granted :(", Toast.LENGTH_LONG).show();
                binding.layoutNoChats.setVisibility(View.VISIBLE);
            }
        }
    }

    private void onRequestedPermissionsGranted() {
        /*
         * FLOW OF CODE IN HOME SCREEN
         * ->Firstly we check if ContactsPermission is granted or not
         * ->If granted then we check if coming on HomeActivity from Notifications
         * ->If coming from notifications then we make a call to FetchInterconnectionList
         * ->If "not" coming from notifications that means coming from SplashActivity and we already have InterconnectionList so we call UserDetails for the interconnections
         * ->If permission for Contacts is "not" granted then we ask for permission and then flow starts from onRequestPermissionsResult method
         * */

        if (isComingFromNotification) {
            //we don't have the interconnectionsList so we making a call for that first
            homeDao.fetchMyInterconnectionsList();
        } else {
            //we already have the interconnectionsList from Intent passed by SplashActivity
            if (homeDao.getInterconnectionListSize() == 0) {
                binding.layoutNoChats.setVisibility(View.VISIBLE);
                // TODO: 18-06-2024 set listener for the MyInterconnections
            } else {
                homeDao.getInterconnectedUsersDetails();
            }
            initMyInterconnectionsAdapter();
        }
    }

    private void initActivityLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        onActivityResult(result.getData());
                    }
                });
    }

    private void onActivityResult(Intent intent) {
        //coming back from ChatWithIndividualActivity
        InterConnectionResponse interConnectionResponse = (InterConnectionResponse) intent.getSerializableExtra(Constants.INTENT_MY_INTERCONNECTION_EXTRA);
        ChatItemResponse lastChatItem = (ChatItemResponse) intent.getSerializableExtra(Constants.INTENT_LAST_CHAT_ITEM_EXTRA);
        if (interConnectionResponse == null) {
            return;
        }

        for (int i = 0; i < homeDao.getInterconnectionListSize(); i++) {
            MyInterconnectionRvItem myInterconnection = homeDao.getMyInterconnectionsList().get(i);
            if (myInterconnection.getConnectionId().matches(interConnectionResponse.connectionId)) {
                myInterconnection.setUnseenChatCount(MyInterconnectionRvItem.NULL_CHAT_COUNT);
                if (lastChatItem != null) {
                    myInterconnection.setLastChatItem(lastChatItem);
                }
                sortInterconnectionsList();
                return;
            }
        }
    }

    private void initMyInterconnectionsAdapter() {
        if (myInterconnectionsAdapter != null) {
            return;
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvMyInterconnections.setLayoutManager(layoutManager);
        binding.rvMyInterconnections.addItemDecoration(new SSUsersAdapter.SpacingItemDecoration(getResources().getDimensionPixelSize(R.dimen.space_between_rv_interconnection_items)));
        myInterconnectionsAdapter = new MyInterconnectionsAdapter(this, this, homeDao.getMyInterconnectionsList(), homeDao.getMyUserDetails().getUserId());
        binding.rvMyInterconnections.getRecycledViewPool().setMaxRecycledViews(Constants.LAYOUT_TYPE_MY_INTERCONNECTION, 0);
        binding.rvMyInterconnections.setAdapter(myInterconnectionsAdapter);
    }


    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.fab_all_chats) {
            startAllContactsActivity();
        }
    }

    //ACTIVITY LAUNCH
    private void startChatWithIndividualActivity(MyInterconnectionRvItem myInterconnectionItem) {
        Intent intent = new Intent(this, ChatWithIndividualActivity.class);
        intent.putExtra(Constants.INTENT_USER_DETAILS_EXTRA, myInterconnectionItem.getReceiverUserDetails());
        intent.putExtra(Constants.INTENT_MY_INTERCONNECTION_EXTRA, new InterConnectionResponse(myInterconnectionItem.getConnectionId(), myInterconnectionItem.getConnectionWith(), false, myInterconnectionItem.getModifiedAt()));
        intent.putExtra(Constants.INTENT_RECEIVERS_INTERCONNECTION_EXTRA, new InterConnectionResponse(myInterconnectionItem.getConnectionId(), homeDao.getMyUserDetails().getUserId(), false, myInterconnectionItem.getModifiedAt()));
        activityResultLauncher.launch(intent);
    }

    //ACTIVITY LAUNCH
    private void startAllContactsActivity() {
        Intent intent = new Intent(this, AllContactsActivity.class);
        activityResultLauncher.launch(intent);
    }

    //CALL FROM DAO
    @Override
    public void myInterconnectionsListFetched() {
        initMyInterconnectionsAdapter();

        if (homeDao.getInterconnectionListSize() == 0) {
            binding.layoutNoChats.setVisibility(View.VISIBLE);
        } else {
            homeDao.getInterconnectedUsersDetails();
        }
    }

    //CALL FROM DAO
    @Override
    public void allInterconnectedUsersDetailsAdded() {
        // TODO: 19-02-2025 uncomment later
        homeDao.getLastChatItemOfAllInterconnections();
        homeDao.getUnseenChatCountOfAllInterconnections();
    }

    //CALL FROM DAO
    @Override
    public void interconnectedUserDetailsAdded(int position) {
        homeDao.getLastChatItemOfInterconnection(homeDao.getMyInterconnectionsList().get(position).getConnectionId());
        homeDao.getUnseenChatCountOfInterconnection(homeDao.getMyInterconnectionsList().get(position).getConnectionId());
    }

    //CALL FROM DAO
    @Override
    public void newInterconnectionAdded(int position) {
        myInterconnectionsAdapter.notifyItemInserted(position);
    }

    //CALL FROM DAO
    @Override
    public void interconnectionRemoved(int position) {
        myInterconnectionsAdapter.notifyItemRemoved(position);
    }

    //CALL FROM DAO
    @Override
    public void myInterconnectionItemUpdated(int position) {
        myInterconnectionsAdapter.notifyItemChanged(position);
    }

    //CALL FROM DAO
    @Override
    public void sortInterconnectionsList() {
        Collections.sort(homeDao.getMyInterconnectionsList());
        myInterconnectionsAdapter.notifyItemRangeChanged(0, homeDao.getInterconnectionListSize());
    }

    //CALL FROM DAO
    @Override
    public void moveInterconnectionItem(int fromPosition, int toPosition) {
        myInterconnectionsAdapter.notifyItemMoved(fromPosition, toPosition);
    }


    //CALL FROM MY_INTERCONNECTION_ADAPTER
    @Override
    public void onInterconnectionRvItemClick(int position) {
        startChatWithIndividualActivity(homeDao.getMyInterconnectionsList().get(position));
    }
}
