package com.danapps.social_cop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private TextView userLocation;
    private LatLng latLng;
    public static final String TAG = "MainActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Issue> list = new ArrayList<>();
    private RecyclerView listIssue;
    private IssueAdapter adapter;
    private ImageView imageView;
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(android.R.color.transparent));
        window.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.cop_bg));

        mAuth = FirebaseAuth.getInstance();
        userName = findViewById(R.id.user_name);
        imageView = findViewById(R.id.circleImageView);
        userLocation = findViewById(R.id.userLocation);
        listIssue = findViewById(R.id.list_issue);
        listIssue.setLayoutManager(new LinearLayoutManager(this));
        listIssue.setHasFixedSize(true);
        Glide.with(this).load(Objects.requireNonNull(mAuth.getCurrentUser()).getPhotoUrl()).placeholder(getDrawable(R.drawable.ic_account)).into(imageView);
        userName.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());


        Query query = db.collection("users").whereEqualTo("Id", mAuth.getUid());

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(task.getResult()).size() > 0) {
                            startCheck();
                        } else {
                            startActivityForResult(new Intent(MainActivity.this, UploadDetails.class), 199);
                        }
                    }
                });
    }

    private void startCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setLocation();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Location Permission Required")
                    .setMessage("Location Is Needed For Adding An Issue")
                    .setPositiveButton("GRANT", (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 212))
                    .setNegativeButton("Cancel Issue", (dialog, which) -> this.finish())
                    .create()
                    .show();
        } else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 212);
    }


    @SuppressLint("MissingPermission")
    public void setLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert addresses != null;
                userLocation.setText(addresses.get(0).getLocality());
                getIssues();
            } else {
                Toast.makeText(this, "Location Not Found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getIssues() {
        Query query = db.collection("issues").whereEqualTo("city", userLocation.getText());
        FirestoreRecyclerOptions<MinIssue> options = new FirestoreRecyclerOptions.Builder<MinIssue>()
                .setQuery(query, MinIssue.class)
                .build();
        adapter = new IssueAdapter(options);
        listIssue.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.log_out:
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LogUser.class);
                startActivity(intent);
                finish();
                break;

            case R.id.add_issue:
                Intent i = new Intent(MainActivity.this, AddIssue.class);
                startActivity(i);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 212) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setLocation();
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 199 && resultCode == RESULT_OK) {
            startCheck();
            Glide.with(this).load(Objects.requireNonNull(mAuth.getCurrentUser()).getPhotoUrl()).placeholder(getDrawable(R.drawable.ic_account)).into(imageView);
            userName.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }
}