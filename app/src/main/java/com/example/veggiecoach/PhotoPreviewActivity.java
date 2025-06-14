package com.example.veggiecoach;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class PhotoPreviewActivity extends AppCompatActivity {

    private ImageView imageViewPreview;
    private TextView textViewDescription;  // 텍스트뷰 변수 추가

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);

        imageViewPreview = findViewById(R.id.imageViewPreview);
        textViewDescription = findViewById(R.id.textViewDescription);  // 텍스트뷰 연결

        String uriStr = getIntent().getStringExtra("imageUri");
        if (uriStr != null) {
            Uri imageUri = Uri.parse(uriStr);

            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                imageViewPreview.setImageBitmap(bitmap);

                // 텍스트뷰에 이미지 경로 또는 원하는 텍스트 표시
                textViewDescription.setText("사진 경로: " + imageUri.getPath());

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "이미지 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "이미지 경로가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
