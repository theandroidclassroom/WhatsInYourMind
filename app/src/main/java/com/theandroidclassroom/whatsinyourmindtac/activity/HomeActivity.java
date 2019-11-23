package com.theandroidclassroom.whatsinyourmindtac.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.theandroidclassroom.whatsinyourmindtac.R;
import com.theandroidclassroom.whatsinyourmindtac.pojo.QuotesPojo;
import com.theandroidclassroom.whatsinyourmindtac.storage.Constants;
import com.theandroidclassroom.whatsinyourmindtac.storage.MySharedPreferences;
import com.theandroidclassroom.whatsinyourmindtac.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.relative)
    RelativeLayout mParent;
    @BindView(R.id.addBtn)
    FloatingActionButton mAddBtn;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    MySharedPreferences sp;
    private CollectionReference mPostRef;
    private FirestoreRecyclerOptions options;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hone);
        ButterKnife.bind(this);


        sp = MySharedPreferences.getInstance(this);

        saveUserData();
        mPostRef = FirebaseFirestore.getInstance().collection(Constants.POSTS);

        options = new FirestoreRecyclerOptions.Builder<QuotesPojo>()
                .setQuery(mPostRef.orderBy("timestamp", Query.Direction.DESCENDING),QuotesPojo.class)
                .build();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPostDialog();
            }
        });
    }

    private void showPostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Post A Quote");
        View view = LayoutInflater.from(this).inflate(R.layout.add_quote,null,false);
        final EditText editText = view.findViewById(R.id.edittext);
        builder.setView(view);
        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String quote = editText.getText().toString().trim();
                if (quote.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Empty Quote!",Toast.LENGTH_SHORT).show();
                }
                else {
                    postQuote(dialog,quote);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void postQuote(final DialogInterface dialog, String quote) {
        Map<String,Object> map = new HashMap<>();
        map.put(Constants.QUOTE,quote);
        map.put(Constants.TIMESTAMP,(System.currentTimeMillis()/1000));
        map.put(Constants.ID,sp.getUserID());
        map.put(Constants.NAME,sp.getUserData(Constants.NAME));
        mPostRef.document().set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()){
                    mRecyclerView.scrollToPosition(0);
                    Toast.makeText(getApplicationContext(),"Quote Added",Toast.LENGTH_SHORT).show();
                }
                else {
                    Utils.showSnackbar(mParent,task.getException().getMessage());
                }
            }
        });


    }

    private void saveUserData(){
        CollectionReference ref = FirebaseFirestore.getInstance().collection("users");
        final DocumentReference documentReference = ref.document(sp.getUserID());

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String name = documentSnapshot.getString(Constants.NAME);
                String profilePic = documentSnapshot.getString(Constants.PROFILE_PIC);
                String about = documentSnapshot.getString(Constants.ABOUT);

                sp.setUserData(Constants.NAME,name);
                sp.setUserData(Constants.PROFILE_PIC,profilePic);
                sp.setUserData(Constants.ABOUT,about);
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        FirestoreRecyclerAdapter<QuotesPojo,MyViewHolder> adapter =  new FirestoreRecyclerAdapter<QuotesPojo, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position,
                                            @NonNull final QuotesPojo model) {
                holder.quote.setText(model.getQuote());
                holder.name.setText(model.getName());
                holder.time.setText(getTime(model.getTimestamp()));

                holder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String sharableText = model.getQuote() +"\n \n-"+model.getName();
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(Intent.EXTRA_TEXT,sharableText);
                        startActivity(Intent.createChooser(sharingIntent,"Share using: "));
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.post_list_layout,viewGroup,false);

                return new MyViewHolder(view);
            }


        };
        adapter.startListening();
        mRecyclerView.setAdapter(adapter);



    }
    private String getTime (long timestamp){
        long ts = timestamp*1000;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String time = sdf.format(new Date(ts));
        return time;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.quote)
        TextView quote;
        @BindView(R.id.name)TextView name;
        @BindView(R.id.time)TextView time;
        @BindView(R.id.share)
        LinearLayout share;
        @BindView(R.id.info)LinearLayout info;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
