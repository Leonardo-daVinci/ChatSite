package apps.nocturnal.com.chatsite;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsActivity extends AppCompatActivity {

    private CircleImageView mProfileImage;
    private TextView mProfilename,mOnline;
    private DatabaseReference mUsersdatabase,mChatsData,mUserRef;
    private FloatingActionButton mSendMsgbtn;
    private EditText mInputText;
    private FirebaseUser mCurrentUser;
    private RecyclerView mChatList;

    private FirebaseRecyclerAdapter<ChatMessages,ChatsActivity.ChatsViewholder> mChatsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        final String user_id = getIntent().getStringExtra("user_id");

        mProfileImage = findViewById(R.id.chats_profileimg);
        mProfilename = findViewById(R.id.chats_profileName);
        mSendMsgbtn = findViewById(R.id.chats_floatingActionButton);
        mInputText = findViewById(R.id.chats_edittext);
        mOnline = findViewById(R.id.chats_online);

        mChatList = findViewById(R.id.chats_list_of_msg);
        //mChatList.setLayoutManager(new LinearLayoutManager(this));


        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mChatList.setLayoutManager(linearLayoutManager);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUsersdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mChatsData = FirebaseDatabase.getInstance().getReference().child("Chats");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mUsersdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String online = dataSnapshot.child("online").getValue().toString();

                mProfilename.setText(display_name);
                if(online.equals("true")){
                    mOnline.setText("Online");
                }
                Picasso.get().load(image).placeholder(R.drawable.ic_profile).into(mProfileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        DatabaseReference ChatsRef = mChatsData.child(mCurrentUser.getUid()).child(user_id);
        Query ChatsQuery = ChatsRef.orderByKey();

        FirebaseRecyclerOptions ChatOptions = new FirebaseRecyclerOptions.Builder<ChatMessages>().setQuery(ChatsQuery,ChatMessages.class).build();

        mChatsAdapter = new FirebaseRecyclerAdapter<ChatMessages, ChatsViewholder>(ChatOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewholder holder, int position, @NonNull final ChatMessages model) {
                holder.setMessage(model.getMessageText());
                holder.setTime(model.getMessageTime());
                String sender = model.getMessageUser();

                if(sender.equals(user_id)){
                    holder.setBG(1);
                }

                if(sender.equals(mCurrentUser.getUid())){
                    holder.setBG(0);
                }
            }


            @NonNull
            @Override
            public ChatsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages, parent,false);
                    return new ChatsViewholder(view);
            }
        };
        mChatList.setAdapter(mChatsAdapter);
        mChatList.scrollToPosition(mChatsAdapter.getItemCount()-1);

        mSendMsgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int items = mChatsAdapter.getItemCount();

                String message = mInputText.getText().toString();

                final ChatMessages chat = new ChatMessages(message, mCurrentUser.getUid());
                final long DTStamp = chat.getMessageTime();

                mChatsData.child(user_id).child(mCurrentUser.getUid())
                        .child(String.valueOf(DTStamp)).setValue(chat);

                mChatsData.child(mCurrentUser.getUid()).child(user_id)
                        .child(String.valueOf(DTStamp)).setValue(chat);

                mInputText.setText("");

                mChatList.scrollToPosition(items-1);
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mChatsAdapter.startListening();
        mUserRef.child("online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mChatsAdapter.stopListening();
    }

    public class ChatsViewholder extends RecyclerView.ViewHolder {

        View mView;

        public ChatsViewholder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setMessage (String message){
            TextView messageView = mView.findViewById(R.id.message_text_data);
            messageView.setText(message);
        }

        public void setTime (long time){
            TextView timeView = mView.findViewById(R.id.message_time);
            timeView.setText(android.text.format.DateFormat.format("HH:mm",time));
        }

        public void setBG (int sender) {
            LinearLayout mMessage = mView.findViewById(R.id.message_adapter);
            LinearLayout MessageBG = mView.findViewById(R.id.message_text);

            if(sender == 1) {
                MessageBG.setBackgroundResource(R.drawable.sendmessage);
            }
            if(sender ==0){
                mMessage.setGravity(Gravity.END);
                MessageBG.setBackgroundResource(R.drawable.receivemessage);
            }
        }
    }

}
