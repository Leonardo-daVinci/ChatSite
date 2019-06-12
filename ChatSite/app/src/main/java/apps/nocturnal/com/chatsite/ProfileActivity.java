package apps.nocturnal.com.chatsite;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfilename, mProfileStatus;
    private Button mProfileSendReq, mProfileDeclineReq;
    private String display_name,status,image;
    private String display_name2,image2,status2;

    private FirebaseUser mCurrentUser;

    private DatabaseReference mUsersdatabase, mFriendReqdatabase, mFriendsData, mNotifyData,mMydata;
    private ProgressDialog mProg;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mProfileImage = findViewById(R.id.profile_image);
        mProfilename = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileSendReq = findViewById(R.id.profile_send_req_btn);
        mProfileDeclineReq = findViewById(R.id.profile_decline_btn);
        mProfileDeclineReq.setVisibility(View.INVISIBLE);
        mProfileDeclineReq.setEnabled(false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrent_state = "not_friends";

        mMydata = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mUsersdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqdatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
       mFriendsData = FirebaseDatabase.getInstance().getReference().child("Friends");
       mNotifyData = FirebaseDatabase.getInstance().getReference().child("notifications");

        mProg = new ProgressDialog(this);
        mProg.setTitle("Loading User data");
        mProg.setMessage("Please wait while we load the data required..");
        mProg.setCanceledOnTouchOutside(false);
        mProg.show();

        mMydata.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                display_name2 = dataSnapshot.child("name").getValue().toString();
                image2 = dataSnapshot.child("image").getValue().toString();
                status2 = dataSnapshot.child("status").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mUsersdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                display_name = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();

                mProfilename.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.ic_profile).into(mProfileImage);

                //------------------------------------------------------FRIENDS LIST / REQUEST FEATURE------------------------------------------------//

                mFriendReqdatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)) {
                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(request_type.equals("received")){
                                mCurrent_state = "request_received";
                                mProfileSendReq.setText("Accept Friend Request");
                                mProfileDeclineReq.setVisibility(View.VISIBLE);
                                mProfileDeclineReq.setEnabled(true);
                            }
                            else if(request_type.equals("sent")){
                                mCurrent_state = "request_sent";
                                mProfileSendReq.setText("Cancel Friend Request");
                            }

                            mProg.dismiss();

                        }
                        else {

                            mFriendsData.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        mProfileSendReq.setText("Unfriend the person");
                                    }
                                    mProg.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProg.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mProg.dismiss();
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReq.setEnabled(false);

                //------------------------------------------------------NOT FRIENDS------------------------------------------------//

                if(mCurrent_state.equals("not_friends")){

                    mFriendReqdatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mFriendReqdatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notifydata = new HashMap<>();
                                        notifydata.put("from", mCurrentUser.getUid());
                                        notifydata.put("type", "request");

                                        mNotifyData.child(user_id).push().setValue(notifydata)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mCurrent_state = "request_sent";
                                                        mProfileSendReq.setText("Cancel Friend Request");
                                                        Toast.makeText(ProfileActivity.this, "Request Sent! :) ",Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                        mCurrent_state = "request_sent";
                                        mProfileSendReq.setText("Cancel Friend Request");
                                        Toast.makeText(ProfileActivity.this, "Request Sent! :) ",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            else{
                                Toast.makeText(ProfileActivity.this, "Cannot send friend request. Try again later! ",Toast.LENGTH_LONG).show();
                            }

                            mProfileSendReq.setEnabled(true);
                        }
                    });
                }

                //------------------------------------------------------CANCEL FRIEND REQUEST------------------------------------------------//

                if(mCurrent_state.equals("request_sent")){

                    mFriendReqdatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqdatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        mProfileSendReq.setEnabled(true);
                                        mCurrent_state = "not_friends";
                                        mProfileSendReq.setText("Send Friend Request");
                                         Toast.makeText(ProfileActivity.this, " Request Cancelled!  ",Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    });
                }

                //------------------------------------------------------REQUEST RECEIVED------------------------------------------------//

                if(mCurrent_state.equals("request_received")){

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    final HashMap<String,String> FriendMap = new HashMap<>();
                    FriendMap.put("name",display_name);
                    FriendMap.put("friendship",currentDate);
                    FriendMap.put("image",image);
                    FriendMap.put("status",status);

                    mFriendsData.child(mCurrentUser.getUid()).child(user_id).setValue(FriendMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            HashMap<String,String> FriendMap2 = new HashMap<>();
                            FriendMap2.put("name",display_name2);
                            FriendMap2.put("friendship",currentDate);
                            FriendMap2.put("image",image2);
                            FriendMap2.put("status",status2);


                                 mFriendsData.child(user_id).child(mCurrentUser.getUid()).setValue(FriendMap2)
                                         .addOnSuccessListener(new OnSuccessListener<Void>() {
                                             @Override
                                             public void onSuccess(Void aVoid) {
                                                 mFriendReqdatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                                                         .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                             @Override
                                                             public void onSuccess(Void aVoid) {
                                                                 mFriendReqdatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                                                         .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                             @Override
                                                                             public void onSuccess(Void aVoid) {
                                                                                 mProfileSendReq.setEnabled(true);
                                                                                 mCurrent_state = "friends";
                                                                                 mProfileSendReq.setText("Unfriend the person");
                                                                                 mProfileDeclineReq.setVisibility(View.INVISIBLE);
                                                                                 mProfileDeclineReq.setEnabled(true);
                                                                                 Toast.makeText(ProfileActivity.this, " Request Accepted! :)  ",Toast.LENGTH_LONG).show();
                                                                             }
                                                                         });
                                                             }
                                                         });
                                             }
                                         });
                        }
                    });
                }

                //------------------------------------------------------REMOVE FRIEND------------------------------------------------//

                if(mCurrent_state.equals("friends")){

                    mFriendsData.child(mCurrentUser.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendsData.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mCurrent_state = "not_friends";
                                                    mProfileSendReq.setEnabled(true);
                                                    mProfileSendReq.setText("Send Friend Request");
                                                }
                                            });
                                }
                            });
                }

            }
        });

        //------------------------------------------------------REQUEST RECEIVED------------------------------------------------//

        mProfileDeclineReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendReqdatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFriendReqdatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProfileSendReq.setEnabled(true);
                                                mCurrent_state = "not_friends";
                                                mProfileSendReq.setText("Send Friend Request");
                                                mProfileDeclineReq.setVisibility(View.INVISIBLE);
                                                mProfileDeclineReq.setEnabled(false);
                                            }
                                        });
                            }
                        });
            }
        });
    }

}
