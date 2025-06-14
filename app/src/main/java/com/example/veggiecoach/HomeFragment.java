package com.example.veggiecoach;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class HomeFragment extends Fragment {

    private PreviewView previewView;
    private ImageView imageViewPhoto;
    private Button btnTakePhoto;

    private ImageButton btnAddTodo;
    private LinearLayout todoListItems;

    private ImageCapture imageCapture;
    private boolean isCameraRunning = false;

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestReadStoragePermissionLauncher;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    private Uri photoUri; // 사진 저장 위치 Uri

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        previewView = root.findViewById(R.id.previewView);
        imageViewPhoto = root.findViewById(R.id.imageViewPhoto);
        btnTakePhoto = root.findViewById(R.id.btnTakePhoto);

        btnAddTodo = root.findViewById(R.id.btnAddTodo);
        todoListItems = root.findViewById(R.id.todoListItems);

        // 초기엔 카메라와 이미지뷰 숨기기
        previewView.setVisibility(View.GONE);
        imageViewPhoto.setVisibility(View.GONE);

        btnTakePhoto.setText("사진 촬영 / 선택");

        // 사진 촬영 or 갤러리 선택 다이얼로그 표시
        btnTakePhoto.setOnClickListener(v -> showCameraGalleryChoiceDialog());

        // 할 일 추가 버튼 클릭 시 캘린더 액티비티 오픈
        btnAddTodo.setOnClickListener(v -> openCalendarActivity());

        // 카메라 권한 요청 결과 처리 콜백
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(getContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                });

        // 저장소 권한 요청 결과 처리 콜백 (갤러리 접근용)
        requestReadStoragePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openGallery();
                    } else {
                        Toast.makeText(getContext(), "저장소 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSaved -> {
                    if (isSaved) {
                        isCameraRunning = false;
                        previewView.setVisibility(View.GONE);
                        imageViewPhoto.setVisibility(View.GONE);
                        btnTakePhoto.setText("사진 촬영 / 선택");

                        // 사진 Uri를 PhotoPreviewActivity에 넘겨서 띄우기
                        Intent intent = new Intent(getContext(), PhotoPreviewActivity.class);
                        intent.putExtra("imageUri", photoUri.toString());
                        startActivity(intent);

                        Toast.makeText(getContext(),
                                "사진 저장 완료: " + photoUri.getPath(), Toast.LENGTH_SHORT).show();

                        // TODO: 딥러닝 처리 함수 호출 가능
                    } else {
                        Toast.makeText(getContext(), "사진 촬영 실패 또는 취소됨", Toast.LENGTH_SHORT).show();
                    }
                });


        // 갤러리에서 이미지 선택 후 결과 콜백
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        previewView.setVisibility(View.GONE);
                        imageViewPhoto.setVisibility(View.VISIBLE);
                        btnTakePhoto.setText("사진 촬영 / 선택");
                        isCameraRunning = false;

                        imageViewPhoto.setImageURI(uri);
                    }
                });

        return root;
    }

    // 카메라 또는 갤러리 선택 다이얼로그 표시
    private void showCameraGalleryChoiceDialog() {
        String[] options = {"카메라 촬영", "갤러리에서 사진 선택", "취소"};

        new AlertDialog.Builder(requireContext())
                .setTitle("사진 선택")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:  // 카메라 촬영
                            checkCameraPermissionAndStartCamera();
                            break;
                        case 1:  // 갤러리 선택
                            checkReadStoragePermissionAndOpenGallery();
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    // 카메라 권한 확인 후 카메라 시작
    private void checkCameraPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera();
        }
    }

    // 저장소 권한 확인 후 갤러리 열기
    private void checkReadStoragePermissionAndOpenGallery() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            requestReadStoragePermissionLauncher.launch(permission);
        } else {
            openGallery();
        }
    }

    // 갤러리에서 이미지 선택 호출
    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    // 카메라 시작 함수 (Preview + ImageCapture 바인딩)
    private void startCamera() {
        previewView.setVisibility(View.VISIBLE);
        imageViewPhoto.setVisibility(View.GONE);
        btnTakePhoto.setText("촬영");

        isCameraRunning = true;

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                // 촬영 버튼 클릭 시 사진 찍기
                btnTakePhoto.setOnClickListener(v -> takePhoto());

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "카메라 시작 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    // 사진 촬영 실행 (FileProvider 적용)
    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(getContext(), "카메라 준비 중입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 임시 파일 생성
            File photoFile = createImageFile();

            // FileProvider를 이용해 Uri 생성
            photoUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);

            takePictureLauncher.launch(photoUri);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "사진 파일 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 이미지 파일 생성 함수
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // 캘린더 액티비티 열기
    private void openCalendarActivity() {
        Intent intent = new Intent(requireContext(), CalendarActivity.class);
        startActivity(intent);
    }

    // 할 일 리스트에 아이템 추가 (예시)
    private void addTodoItem(String dateStr) {
        TextView todoItem = new TextView(requireContext());
        todoItem.setText("작물 심기 날짜: " + dateStr + " - 할 일: 잡초 뽑기");
        todoItem.setTextSize(16);
        todoItem.setPadding(8, 8, 8, 8);
        todoListItems.addView(todoItem);
    }
}
