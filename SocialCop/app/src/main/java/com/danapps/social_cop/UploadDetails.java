package com.danapps.social_cop;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UploadDetails extends AppCompatActivity implements View.OnClickListener {

    private ImageView userImage;
    private Uri uri, uploaded;
    private StorageReference mStorageRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_details);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(android.R.color.transparent));
        window.setBackgroundDrawable(getDrawable(R.drawable.cop_bg));
        userImage = findViewById(R.id.userImage);
        editText = findViewById(R.id.input_name);
        mStorageRef = FirebaseStorage.getInstance().getReference();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addImage:

            case R.id.userImage:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, 121);
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Grant Permission")
                            .setMessage("Storage Permission Required To Select Image")
                            .setPositiveButton("GRANT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    ActivityCompat.requestPermissions(UploadDetails.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 121);
                                }
                            })
                            .setNegativeButton("DENY", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Toast.makeText(UploadDetails.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create()
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 121);
                }
                break;

            case R.id.submit:
                v.setEnabled(false);
                ProgressBar progressBar = findViewById(R.id.progress_circular);
                progressBar.setVisibility(View.VISIBLE);
                String name = editText.getText().toString().trim();
                if (uri != null) {
                    if (!TextUtils.isEmpty(name)) {
                        upload(uri, name);
                    } else
                        Toast.makeText(this, "Name Required", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Image Required", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void upload(Uri uri, String name) {
        mStorageRef
                .child("profile")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .putFile(uri)
                .addOnSuccessListener(taskSnapshot -> mStorageRef.child("profile")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .getDownloadUrl().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                uploaded = task.getResult();

                                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                        .setPhotoUri(uploaded)
                                        .setDisplayName(name)
                                        .build();
                                FirebaseAuth.getInstance().getCurrentUser().updateProfile(request)
                                        .addOnCompleteListener(task1 -> {
                                            Map<String, String> user = new HashMap<>();
                                            user.put("Id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                            user.put("Name", name);
                                            user.put("Profile", uploaded.toString());
                                            db.collection("users")
                                                    .add(user)
                                                    .addOnCompleteListener(task11 -> {
                                                        setResult(RESULT_OK);
                                                        finish();
                                                    });
                                        });

                            }
                        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 121 && resultCode == RESULT_OK) {
            assert data != null;
            uri = data.getData();
            Glide.with(this).load(uri).into(userImage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 121 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 121);
        } else {
            Toast.makeText(UploadDetails.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please Provide Input", Toast.LENGTH_SHORT).show();
    }
}