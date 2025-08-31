package com.example.sswhatsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.adapters.NonSSUsersAdapter;
import com.example.sswhatsapp.adapters.SSUsersAdapter;
import com.example.sswhatsapp.databinding.ActivityAllContactsBinding;
import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.listeners.NonSSUsersAdapterListener;
import com.example.sswhatsapp.listeners.SSUsersAdapterListener;
import com.example.sswhatsapp.models.UserDeviceContact;
import com.example.sswhatsapp.models.responses.InterConnectionResponse;
import com.example.sswhatsapp.models.responses.UserDetailsResponse;
import com.example.sswhatsapp.providers.ContactsProvider;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;
import com.example.sswhatsapp.utils.Permissions;
import com.example.sswhatsapp.utils.SessionManager;
import com.example.sswhatsapp.utils.TimeHandler;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllContactsActivity extends AppCompatActivity implements View.OnClickListener, FirestoreNetworkCallListener,
        SSUsersAdapterListener, NonSSUsersAdapterListener, NestedScrollView.OnScrollChangeListener {
    //fields
    private ActivityAllContactsBinding binding;
    private FirestoreManager firestoreManager;
    private List<UserDeviceContact> allContactsList;
    private List<UserDeviceContact> nonSSWhatsappContactList;
    private List<UserDetailsResponse> SSWhatsappUsersList;     //these are the people on SSWhatsapp whose mobile no. is saved in user's phone
    SSUsersAdapter ssUsersAdapter;
    NonSSUsersAdapter nonSSUsersAdapter;
    InterConnectionResponse myInterconnection, receiversInterconnection;
    String myUserId, myInterconnectionsDocId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_contacts);
        myUserId = SessionManager.getUserId();
        myInterconnectionsDocId = SessionManager.getMyInterconnectionsDocId();
        firestoreManager = new FirestoreManager(this, this);

        requestUserContactsPermission();
        initToolbar();

        //setting Listeners
        binding.scrollView.setOnScrollChangeListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Permissions.PERMISSION_READ_CONTACTS_CODE) {
            if (grantResults[0] == Permissions.PERMISSION_GRANTED) {
                getUserDeviceContacts();
            } else {
                Toast.makeText(this, "Permission not granted :(", Toast.LENGTH_LONG).show();
                binding.layoutSsUsers.setVisibility(View.GONE);
                binding.layoutNonSsUsers.setVisibility(View.GONE);
                binding.layoutPermissionNotGranted.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initToolbar() {
        binding.toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void requestUserContactsPermission() {
        binding.layoutSsUsers.setVisibility(View.GONE);
        binding.layoutNonSsUsers.setVisibility(View.GONE);
        if (Permissions.isReadContactsGranted(this, true))
            getUserDeviceContacts();
    }

    private void getUserDeviceContacts() {
        allContactsList = ContactsProvider.getUserDeviceContacts(this);
        UserDeviceContact.removeInvalidNumbers(allContactsList);
        UserDeviceContact.removeNumber(allContactsList, SessionManager.getUserMobileNo());
        Collections.sort(allContactsList);

        if (allContactsList.size() != 0) {
            getAllUsersFromFirebase();
        } else
            Toast.makeText(this, "no contacts found :(", Toast.LENGTH_LONG).show();
    }

    private void initContactLists(List<UserDetailsResponse> allUsers) {
        SSWhatsappUsersList = new ArrayList<>();
        nonSSWhatsappContactList = new ArrayList<>();

        boolean isContactOnSSWhatsapp;
        for (UserDeviceContact contact : allContactsList) {
            isContactOnSSWhatsapp = false;
            for (UserDetailsResponse user : allUsers) {
                if (user.getMobileNo().matches(contact.getMobileNo())) {
                    user.setLocalPhoneName(contact.getName());
                    SSWhatsappUsersList.add(user);
                    isContactOnSSWhatsapp = true;
                    break;
                }
            }
            if (!isContactOnSSWhatsapp)
                nonSSWhatsappContactList.add(contact);
        }
    }

    private void setSSUserListAdapter() {
        //setting rv carbon emission
        binding.rvSsContacts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.rvSsContacts.addItemDecoration(new SSUsersAdapter.SpacingItemDecoration(getResources().getDimensionPixelSize(R.dimen.space_between_rv_user_items)));
        ssUsersAdapter = new SSUsersAdapter(this, this, SSWhatsappUsersList);
        binding.rvSsContacts.setAdapter(ssUsersAdapter);
    }

    private void setNonSSUserListAdapter() {
        //setting rv carbon emission
        binding.rvNonSsContacts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.rvNonSsContacts.addItemDecoration(new NonSSUsersAdapter.SpacingItemDecoration(getResources().getDimensionPixelSize(R.dimen.space_between_rv_user_items)));
        nonSSUsersAdapter = new NonSSUsersAdapter(this, this, nonSSWhatsappContactList);
        binding.rvNonSsContacts.setAdapter(nonSSUsersAdapter);
    }

    private void setToolbarNoOfContacts() {
        binding.noOfContacts.setText(getResources().getString(R.string.no_of_contacts, SSWhatsappUsersList.size()));
    }


    @Override
    public void onClick(View view) {
        //Layout onClickListener
        if (view.getId() == R.id.search_contact) {

        }
    }

    @Override
    public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (!binding.scrollView.canScrollVertically(1)) {
            //onBottomHit
            if (ssUsersAdapter.isListCompletelyShown()) {
                if (nonSSUsersAdapter.isListCompleted())
                    binding.scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) null);
                else
                    nonSSUsersAdapter.showNextPage();
            } else {
                ssUsersAdapter.showNextPage();
            }
        }
    }

    //Adapter Listener
    @Override
    public void onSSUserClick(UserDetailsResponse user) {
        getMyConnection(user.getUserId());
    }

    @Override
    public void onSSUsersListCompletelyShown() {
        binding.layoutNonSsUsers.setVisibility(View.VISIBLE);
        setNonSSUserListAdapter();
    }

    //Adapter Listener
    @Override
    public void onNonSSUserClick(UserDeviceContact user) {
    }

    //ACTIVITY LAUNCH
    void startChatWithIndividualActivity() {
        Intent intent = new Intent(this, ChatWithIndividualActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        intent.putExtra(Constants.INTENT_USER_DETAILS_EXTRA, ssUsersAdapter.selectedUser);
        intent.putExtra(Constants.INTENT_MY_INTERCONNECTION_EXTRA, myInterconnection);
        intent.putExtra(Constants.INTENT_RECEIVERS_INTERCONNECTION_EXTRA, receiversInterconnection);
        startActivity(intent);
        finish();
    }

    //NETWORK CALL
    private void getAllUsersFromFirebase() {
        firestoreManager.getAllUsersExceptMe();
    }

    //NETWORK CALL
    private void getMyConnection(String userId) {
        firestoreManager.getMyInterconnection(myInterconnectionsDocId, userId);
    }

    //NETWORK CALL
    private void createNewConnection(String userId) {
        firestoreManager.createNewConnection(myUserId, userId, TimeHandler.getCurrentTimeStamp());
    }

    //NETWORK CALL
    private void createMyInterconnection(InterConnectionResponse interconnectionResponse) {
        firestoreManager.createMyInterconnection(myInterconnectionsDocId, interconnectionResponse);
    }

    //NETWORK CALL
    private void createReceiversInterconnection(String connectionId) {
        InterConnectionResponse receiverInterconnectionItem = new InterConnectionResponse(connectionId, myUserId, true, TimeHandler.getCurrentTimeStamp());
        firestoreManager.createReceiversInterconnection(ssUsersAdapter.selectedUser.myInterconnectionsDocId, receiverInterconnectionItem);
    }

    //NETWORK CALL
    private void deleteConnection(String connectionId) {
        firestoreManager.deleteConnection(connectionId);
    }


    //NETWORK CALL
    private void deleteConnectionsReferenceFromList(String connectionListRef, String connectionWith) {
        firestoreManager.deleteConnectionsReferenceFromList(connectionListRef, connectionWith);
    }

    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.GET_ALL_USERS_EXCEPT_ME_CALL: {
                QuerySnapshot snapshot = (QuerySnapshot) response;
                List<UserDetailsResponse> allUsers = new ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    if (doc.exists())
                        allUsers.add(doc.toObject(UserDetailsResponse.class));
                }

                initContactLists(allUsers);
                setToolbarNoOfContacts();
                setSSUserListAdapter();
                binding.layoutSsUsers.setVisibility(View.VISIBLE);
                break;
            }

            case FirebaseConstants.GET_MY_INTERCONNECTION_CALL: {
                QuerySnapshot querySnapshot = (QuerySnapshot) response;

                if (querySnapshot.size() == 0) {
                    //Connection does not exist in the collection
                    //So we need to create a new connection and 2 interconnections for each user
                    createNewConnection(ssUsersAdapter.selectedUser.getUserId());
                } else {
                    //Connection exists
                    myInterconnection = querySnapshot.getDocuments().get(0).toObject(InterConnectionResponse.class);
                    //a time utilization (saving one network call)
                    receiversInterconnection = new InterConnectionResponse(myInterconnection.getConnectionId(), myUserId, true, myInterconnection.getModifiedAt());    // <- a time utilization (saving one network call)
                    startChatWithIndividualActivity();
                }
                break;
            }

            case FirebaseConstants.CREATE_NEW_CONNECTION_CALL: {
                InterConnectionResponse connectionRef = (InterConnectionResponse) response;
                if (connectionRef != null) {
                    /*
                     * now creating an interconnection for the user*/
                    createMyInterconnection(connectionRef);
                }
                break;
            }

            case FirebaseConstants.CREATE_MY_INTERCONNECTION_CALL: {
                myInterconnection = ((InterConnectionResponse) response);
                if (myInterconnection != null) {
                    createReceiversInterconnection(myInterconnection.getConnectionId());
                } else {
                    onFirestoreNetworkCallFailure(FirebaseConstants.GENERAL_ERROR);
                }
                break;
            }

            case FirebaseConstants.CREATE_RECEIVERS_INTERCONNECTION_CALL: {
                receiversInterconnection = (InterConnectionResponse) response;
                startChatWithIndividualActivity();
                break;
            }
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.CREATE_NEW_CONNECTION_CALL: {
                if (response != null) {
                    String connectionId = (String) response;
                    if (!Helper.isNill(connectionId)) {
                        deleteConnection(connectionId);
                    }
                }
                onFirestoreNetworkCallFailure(FirebaseConstants.GENERAL_ERROR);
                break;
            }

            case FirebaseConstants.CREATE_MY_INTERCONNECTION_CALL: {
                if (response != null) {
                    String connectionId = (String) response;
                    if (!Helper.isNill(connectionId)) {
                        deleteConnection(connectionId);
                        deleteConnectionsReferenceFromList(myInterconnectionsDocId, ssUsersAdapter.selectedUser.getUserId());
                    }
                }
                onFirestoreNetworkCallFailure(FirebaseConstants.GENERAL_ERROR);
                break;
            }

            case FirebaseConstants.CREATE_RECEIVERS_INTERCONNECTION_CALL: {
                if (response != null) {
                    String connectionId = (String) response;
                    if (!Helper.isNill(connectionId)) {
                        deleteConnection(connectionId);
                        deleteConnectionsReferenceFromList(myInterconnectionsDocId, ssUsersAdapter.selectedUser.getUserId());
                        deleteConnectionsReferenceFromList(ssUsersAdapter.selectedUser.getMyInterconnectionsDocId(), myUserId);
                    }
                }
                onFirestoreNetworkCallFailure(FirebaseConstants.GENERAL_ERROR);
                break;
            }
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(String errorMessage) {
        Log.d(FirebaseConstants.NETWORK_CALL, errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}
