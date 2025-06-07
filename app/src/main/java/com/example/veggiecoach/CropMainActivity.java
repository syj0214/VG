package com.example.veggiecoach;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CropMainActivity extends AppCompatActivity {

    private Button btnInitWork, btnManage, btnTakePhoto;
    private TextView textViewCropInfo;
    private PreviewView previewView;
    private ImageView imageViewPhoto;

    private ImageCapture imageCapture;
    private boolean isCameraRunning = false;
    private String cropName = "작물 없음";

    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_main);

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // 뷰 연결
        btnInitWork = findViewById(R.id.btnInitWork);
        btnManage = findViewById(R.id.btnManage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        textViewCropInfo = findViewById(R.id.textViewCropInfo);
        previewView = findViewById(R.id.previewView);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);

        // 작물명 전달 받기
        Intent intent = getIntent();
        String passedName = intent.getStringExtra("cropName");
        if (passedName != null) {
            cropName = passedName;
        }

        // 초기 작업 버튼 클릭
        btnInitWork.setOnClickListener(v -> {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            // Firestore에서 작물 정보 조회 후 UI 업데이트
            fetchCropInfoAndDisplay(cropName, timestamp);

            // 카메라 관련 뷰는 숨김
            previewView.setVisibility(android.view.View.GONE);
            imageViewPhoto.setVisibility(android.view.View.GONE);
            btnTakePhoto.setVisibility(android.view.View.GONE);
            isCameraRunning = false;
        });

        // 작물 관리 버튼 클릭 (카메라 시작)
        btnManage.setOnClickListener(v -> {
            startCameraWithPermissionCheck();
        });

        // 사진 촬영 버튼
        btnTakePhoto.setOnClickListener(v -> {
            if (!isCameraRunning) {
                startCameraWithPermissionCheck();
            } else {
                takePhoto();
            }
        });

        // 초기 UI 상태
        previewView.setVisibility(android.view.View.GONE);
        imageViewPhoto.setVisibility(android.view.View.GONE);
        btnTakePhoto.setVisibility(android.view.View.GONE);
        textViewCropInfo.setText("작물 정보가 여기에 표시됩니다.");
    }

    // Firestore에서 한글명 기준 작물 정보 조회
    private void fetchCropInfoAndDisplay(String cropName, String timestamp) {
        CollectionReference cropsRef = db.collection("crops_info");
        // 한글명 필드가 'cropName'인 문서 찾기 (예: "딸기")
        cropsRef.whereEqualTo("name", cropName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        if (snapshots != null && !snapshots.isEmpty()) {
                            DocumentSnapshot doc = snapshots.getDocuments().get(0);

                            String name = doc.getString("name");
                            Long growthTime = doc.getLong("growthTime");

                            String info = "작물명: " + (name != null ? name : cropName) + "\n"
                                    + "성장 기간: " + (growthTime != null ? growthTime + "일" : "정보 없음") + "\n"
                                    + "시간: " + timestamp;

                            textViewCropInfo.setText(info);
                        } else {
                            textViewCropInfo.setText("작물 정보가 없습니다: " + cropName);
                        }
                    } else {
                        textViewCropInfo.setText("작물 정보 조회 실패: " + task.getException());
                    }
                });
    }

    private void startCameraWithPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        previewView.setVisibility(android.view.View.VISIBLE);
        imageViewPhoto.setVisibility(android.view.View.GONE);
        btnTakePhoto.setVisibility(android.view.View.VISIBLE);
        btnTakePhoto.setText("사진 촬영");
        isCameraRunning = true;

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "카메라 시작 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "카메라 준비 중입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(getExternalFilesDir(null),
                "crop_photo_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(() -> {
                            previewView.setVisibility(android.view.View.GONE);
                            imageViewPhoto.setVisibility(android.view.View.VISIBLE);
                            btnTakePhoto.setText("카메라 실행");
                            isCameraRunning = false;

                            imageViewPhoto.setImageBitmap(BitmapFactory.decodeFile(photoFile.getAbsolutePath()));
                            Toast.makeText(CropMainActivity.this,
                                    "사진 저장 완료: " + photoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                            // TODO: 딥러닝 처리 함수 호출 등 추가 가능
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        runOnUiThread(() -> {
                            Toast.makeText(CropMainActivity.this,
                                    "사진 촬영 실패: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
