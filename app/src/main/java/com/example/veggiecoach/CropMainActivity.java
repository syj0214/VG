package com.example.veggiecoach;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class CropMainActivity extends AppCompatActivity {

    private Button btnCropManage, btnCropInfo, btnHome, btnSell, btnMyPage;
    private TextView tvCropTitle;

    private String cropName = "작물 없음";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_main);

        btnCropInfo = findViewById(R.id.btnCropInfo);         // 작물 정보
        btnCropManage = findViewById(R.id.btnCropManage);     // 작물 관리
        btnHome = findViewById(R.id.btnHome);
        btnSell = findViewById(R.id.btnSell);
        btnMyPage = findViewById(R.id.btnMyPage);

        tvCropTitle = findViewById(R.id.tvCropTitle);

        // 전달받은 작물명
        Intent intent = getIntent();
        String passedName = intent.getStringExtra("cropName");
        if (passedName != null) {
            cropName = passedName;
        }

        // 초기 화면 프래그먼트로 홈 띄우기
        tvCropTitle.setText("홈");
        replaceFragment(HomeFragment.newInstance());

        btnCropInfo.setOnClickListener(v -> {
            tvCropTitle.setText("작물 정보");
            replaceFragment(CropInfoFragment.newInstance(cropName));
        });

        btnCropManage.setOnClickListener(v -> {
            tvCropTitle.setText("작물 관리");
            replaceFragment(new CropManageFragment());
        });

        btnHome.setOnClickListener(v -> {
            tvCropTitle.setText("홈");
            replaceFragment(HomeFragment.newInstance());
        });

        btnSell.setOnClickListener(v -> {
            tvCropTitle.setText("판매");
            replaceFragment(new SellFragment());
        });

        btnMyPage.setOnClickListener(v -> {
            tvCropTitle.setText("마이페이지");
            replaceFragment(new MyPageFragment());
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
