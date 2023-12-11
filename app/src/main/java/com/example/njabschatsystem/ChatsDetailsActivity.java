package com.example.njabschatsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.njabschatsystem.Adapter.ChatAdapter;
import com.example.njabschatsystem.databinding.ActivityChatsDetailsBinding;
import com.example.njabschatsystem.models.MessageModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatsDetailsActivity extends AppCompatActivity {

    ActivityChatsDetailsBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        final String senderId = auth.getUid();
        String recieveId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.vuyo).into(binding.profileImage);

        binding.backArrow.setOnClickListener(view -> {
            Intent intent = new Intent(ChatsDetailsActivity.this,MainActivity.class);
            startActivity(intent);

        });

        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels,this,recieveId);

        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        final String senderRoom = senderId + recieveId;
        final String receiverRoom = recieveId + senderId;
        Toast.makeText(this, senderRoom, Toast.LENGTH_SHORT).show();

        database.getReference().child("chats")
                        .child(senderRoom)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        messageModels.clear();
                                        for (DataSnapshot snapshot1 : snapshot.getChildren())
                                        {
                                            MessageModel model = snapshot1.getValue(MessageModel.class);
                                            model.setMessageID(snapshot1.getKey());
                                            messageModels.add(model);
                                        }
                                        chatAdapter.notifyDataSetChanged();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


        binding.send.setOnClickListener(view -> {
            String message = binding.enterMessage.getText().toString();
            final MessageModel model = new MessageModel(senderId,message);
            model.setTimestamp(new Date().getTime());
            binding.enterMessage.setText("");

            database.getReference().child("chats")
                    .child(senderRoom)
                    .push()
                    .setValue(model).addOnSuccessListener(unused -> database.getReference().child("chats")
                            .child(receiverRoom)
                            .push()
                            .setValue(model).addOnSuccessListener(unused1 -> {

                            }));

        });





    }
}