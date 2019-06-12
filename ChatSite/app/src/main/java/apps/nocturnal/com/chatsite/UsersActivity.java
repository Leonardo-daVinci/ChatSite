package apps.nocturnal.com.chatsite;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;

    private FirebaseRecyclerAdapter<Users,UsersActivity.UsersViewholder> mUsersAdapter;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.users_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.keepSynced(true); //new

        //new Use mDatabase instead of UserRef
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Query UserQuery = UserRef.orderByKey();

        FirebaseRecyclerOptions UsersOptions = new FirebaseRecyclerOptions.Builder<Users>().setQuery(UserQuery,Users.class).build();

        mUsersAdapter = new FirebaseRecyclerAdapter<Users, UsersViewholder>(UsersOptions) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewholder holder, int position, @NonNull final  Users model) {
                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setImage(getBaseContext(),model.getImage());

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent (UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });

            }

            @NonNull
            @Override
            public UsersViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_adapter, parent,false);

                return new UsersActivity.UsersViewholder(view);
            }
        };

        mUsersList.setAdapter(mUsersAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUsersAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUsersAdapter.stopListening();
    }

    public static class UsersViewholder extends RecyclerView.ViewHolder{

        View mView;

        UsersViewholder(View itemView) {
            super(itemView);

            mView = itemView;
            }

            public void setName(String name){
                TextView userNameView = mView.findViewById(R.id.users_display_name);
                userNameView.setText(name);
            }

            public void setStatus (String status){
            TextView userStatusView = mView.findViewById(R.id.users_status);
            userStatusView.setText(status);
            }

            public void setImage (Context context, String image){
                CircleImageView userImage = mView.findViewById(R.id.users_image);
                Picasso.get().load(image).into(userImage);             //check this later Picasso.with(context).load(image).into(userImage)
            }
    }
}
