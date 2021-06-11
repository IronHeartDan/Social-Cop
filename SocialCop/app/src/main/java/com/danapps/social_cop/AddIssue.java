package com.danapps.social_cop;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class AddIssue extends AppCompatActivity implements View.OnClickListener {
    private ViewPager2 viewPager;
    private TextView show_task;
    private Issue issue;
    private StorageReference mStorageRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_issue);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        viewPager = findViewById(R.id.viewPager);
        show_task = findViewById(R.id.show_task);
        viewPager.setUserInputEnabled(false);
        PageStateAdapter adapter = new PageStateAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);
        issue = new Issue();
        issue.setFrom(FirebaseAuth.getInstance().getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_previous:
                if (viewPager.getCurrentItem() == 0) {
                    finish();
                } else if (viewPager.getCurrentItem() == 1) {
                    ImageView btn_next = findViewById(R.id.btn_next);
                    btn_next.setVisibility(View.VISIBLE);
                    show_task.setText("Provide Location");
                    viewPager.setCurrentItem(0);
                } else {
                    ImageView btn_next = findViewById(R.id.btn_next);
                    btn_next.setVisibility(View.VISIBLE);
                    show_task.setText("Provide Proof");
                    viewPager.setCurrentItem(1);
                }
                break;
            case R.id.btn_next:
                if (viewPager.getCurrentItem() == 0) {
                    ProvideProof provideProof = (ProvideProof) getSupportFragmentManager().findFragmentByTag("f1");
                    if (provideProof != null && provideProof.isSet) {
                        v.setVisibility(View.VISIBLE);
                    } else
                        v.setVisibility(View.INVISIBLE);
                    show_task.setText("Provide Proof");
                    viewPager.setCurrentItem(1);
                } else if (viewPager.getCurrentItem() == 1) {
                    show_task.setText("Provide Details");
                    viewPager.setCurrentItem(2);
                } else {
                    ImageView btn_previous = findViewById(R.id.btn_previous);
                    btn_previous.setVisibility(View.GONE);
                    show_task.setVisibility(View.GONE);
                    v.setVisibility(View.GONE);
                    ProvideLocation provideLocation = (ProvideLocation) getSupportFragmentManager().findFragmentByTag("f0");
                    ProvideProof provideProof = (ProvideProof) getSupportFragmentManager().findFragmentByTag("f1");
                    ProvideDetails provideDetails = (ProvideDetails) getSupportFragmentManager().findFragmentByTag("f2");
                    provideDetails.thankYou();

                    issue.setLatLng(provideLocation.latLng);
                    issue.setDesc(provideDetails.desc);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    assert provideProof.bitmap != null;
                    provideProof.bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                    byte[] data = baos.toByteArray();

                    SimpleDateFormat timeStampFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
                    Date myDate = new Date();
                    String filename = timeStampFormat.format(myDate);

                    StorageReference reference = mStorageRef
                            .child("proofs").child(
                                    Objects.requireNonNull(FirebaseAuth.getInstance().getUid())
                            ).child(filename);
                    reference.putBytes(data)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            issue.setProof(task.getResult().toString());
                                            issue.setLocality(provideLocation.locality);
                                            issue.setCity(provideLocation.city);
                                            issue.setStatus(0);
                                            db.collection("issues")
                                                    .add(issue)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Toast.makeText(AddIssue.this, "Issue Submitted", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    });
                                        }
                                    });
                                }
                            });
                    break;
                }
        }
    }

    public void isSet(boolean isSet) {
        if (isSet) {
            findViewById(R.id.btn_next).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.btn_next).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 121) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ProvideProof provideProof = (ProvideProof) getSupportFragmentManager().findFragmentByTag("f1");
                assert provideProof != null;
                provideProof.startCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        if (requestCode == 212) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ProvideLocation provideLocation = (ProvideLocation) getSupportFragmentManager().findFragmentByTag("f0");
                assert provideLocation != null;
                provideLocation.setLocation();
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}