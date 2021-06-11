package com.danapps.social_cop;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ProvideProof extends Fragment {

    private PreviewView previewView;
    private ImageView capture, preview_capture, cancel_capture;
    private ImageCapture imageCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    public boolean isSet = false;
    private AddIssue addIssue;
    public Bitmap bitmap = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_provide_proof, container, false);

        addIssue = (AddIssue) getActivity();

        previewView = view.findViewById(R.id.camera_preview);
        capture = view.findViewById(R.id.capture);
        preview_capture = view.findViewById(R.id.preview_capture);
        cancel_capture = view.findViewById(R.id.cancel_capture);


        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCapture.takePicture(ContextCompat.getMainExecutor(Objects.requireNonNull(getContext())), new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        v.setVisibility(View.GONE);
                        preview_capture.setVisibility(View.VISIBLE);
                        cancel_capture.setVisibility(View.VISIBLE);
                        preview_capture.setImageBitmap(previewView.getBitmap());
                        bitmap = previewView.getBitmap();
                        assert addIssue != null;
                        addIssue.isSet(true);
                        isSet = true;
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                    }
                });
            }
        });

        cancel_capture.setOnClickListener(v -> {
            v.setVisibility(View.GONE);
            preview_capture.setVisibility(View.GONE);
            capture.setVisibility(View.VISIBLE);
            assert addIssue != null;
            addIssue.isSet(false);
            isSet = false;
        });
        return view;
    }

    public void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(Objects.requireNonNull(getContext()));

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getContext()));

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder()
                .build();

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Camera Permission Required")
                    .setMessage("Camera Permission Is Required For Taking Photos")
                    .setPositiveButton("GRANT", (dialog, which) -> ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.CAMERA}, 121))
                    .setNegativeButton("Cancel Issue", (dialog, which) -> Objects.requireNonNull(getActivity()).finish())
                    .create()
                    .show();
        } else
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 121);
    }
}