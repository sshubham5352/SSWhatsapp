package com.example.sswhatsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.databinding.ActivityHomeBinding;
import com.example.sswhatsapp.firebase.FirestoreManager;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    //fields
    private ActivityHomeBinding binding;
    private FirestoreManager firestoreManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        //setting onClickListener
        binding.fabAllChats.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab_all_chats) {
            startAllContactsActivity();
        }
    }

    public void startAllContactsActivity() {
        Intent intent = new Intent(this, AllContactsActivity.class);
        startActivity(intent);
    }
}
