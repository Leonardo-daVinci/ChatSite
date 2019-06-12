package apps.nocturnal.com.chatsite;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private FirebaseUser mCurrentUser;
    private FirebaseRecyclerAdapter<Friends,FriendsFragment.FriendsViewHolder> mFriendsAdapter;
    private DatabaseReference mDatabase;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View view = inflater.inflate(R.layout.fragment_friends, container, false);

       mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mFriendsList = view.findViewById(R.id.friends_list);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUser.getUid());
        mDatabase.keepSynced(true); //new

        DatabaseReference FriendRef = mDatabase;
        Query FriendQuery = FriendRef.orderByKey();

        FirebaseRecyclerOptions FriendOptions = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(FriendQuery,Friends.class).build();

        mFriendsAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(FriendOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull final Friends model) {
                holder.setName(model.getName());
                holder.setFriendship(model.getFriendship());
                holder.setImage(getContext(),model.getImage());

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chatsIntent = new Intent (getActivity(),ProfileActivity.class);
                        chatsIntent.putExtra("user_id",user_id);
                        startActivity(chatsIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_adapter,parent,false);
                return  new FriendsFragment.FriendsViewHolder(view1);
            }
        };

        mFriendsList.setAdapter(mFriendsAdapter);

        // Inflate the layout for this fragment
       return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mFriendsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mFriendsAdapter.stopListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView FriendNameview = mView.findViewById(R.id.friends_display_name);
            FriendNameview.setText(name);
        }

        public void setFriendship(String friendship){
            TextView FriendshipView = mView.findViewById(R.id.friends_status);
            FriendshipView.setText("Friends Since: "+friendship);
        }

        public void setImage (Context context, String image){
            CircleImageView FriendImage = mView.findViewById(R.id.friend_image);
            Picasso.get().load(image).into(FriendImage);
        }
    }
}
