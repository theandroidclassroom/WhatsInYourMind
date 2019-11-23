package com.theandroidclassroom.whatsinyourmindtac.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theandroidclassroom.whatsinyourmindtac.R;
import com.theandroidclassroom.whatsinyourmindtac.storage.MySharedPreferences;
import com.theandroidclassroom.whatsinyourmindtac.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SecondStepRegisterActivity extends AppCompatActivity {

    @BindView(R.id.profileImage)
    CircleImageView mProfile;
    @BindView(R.id.about)
    EditText mAboutEt;
    @BindView(R.id.btn)
    Button mSaveBtn;
    @BindView(R.id.relative)
    RelativeLayout mParent;
    private int REQ_CODE = 100;
    private int PERMISSION_REQ_CODE = 200;
    private Uri mUri;
    private FirebaseAuth mAuth;
    private CollectionReference mUserRef;
    private String mUserID;
    private StorageReference storageReference;
    private AlertDialog dialog;
    private MySharedPreferences sp;


    private String permissions[] = {Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = MySharedPreferences.getInstance(this);
        setContentView(R.layout.activity_second_step_register);
        ButterKnife.bind(this);
        dialog = Utils.getAlertDialog(this,"saving Info..");

        requestFunctions();
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseFirestore.getInstance().collection("users");
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_pics");
        mUserID = mAuth.getUid();
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQ_CODE);
            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDataAndSave();
            }
        });

    }

    private void checkDataAndSave() {
        if (Utils.isNetworkAvailable()) {
            String about = mAboutEt.getText().toString().trim();
            if (mUri == null) {
              Utils.showSnackbar(mParent,"Please select an image");
                return;

            }
            if (about.length() < 40) {

                Utils.showSnackbar(mParent,"Bio too short");
            } else {

                uploadImage(about);
            }
        } else {
            Utils.showSnackbar(mParent,"No Internet Available!");
        }

    }

    private void uploadImage(final String about) {
        dialog.show();
        UploadTask task = storageReference.child(mUserID).putFile(mUri);
        task.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            saveData(about, imageUrl);
                        }
                    });

                } else {
                  dialog.dismiss();
                  Utils.showSnackbar(mParent,task.getException().getLocalizedMessage());
                }
            }
        });
    }

    private void saveData(String about, String imageUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put("profile_pic", imageUrl);
        map.put("about", about);


        mUserRef.document(mUserID).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    sp.setLogin("2");
                    Toast.makeText(getApplicationContext(), "Profile Info Saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SecondStepRegisterActivity.this,HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), task.getException()
                            .getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {

            CropImage.activity(data.getData())
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setOutputCompressQuality(50)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null) {
                mUri = result.getUri();
                mProfile.setImageURI(mUri);

            }
        }
    }


    private void requestFunctions() {
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SecondStepRegisterActivity.this, permissions, PERMISSION_REQ_CODE);
        }
    }


}
