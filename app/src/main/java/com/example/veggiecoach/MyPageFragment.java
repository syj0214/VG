package com.example.veggiecoach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MyPageFragment extends Fragment {

    public MyPageFragment() {
        // 기본 생성자 필요
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // fragment_my_page.xml 레이아웃을 인플레이트
        return inflater.inflate(R.layout.fragment_my_page, container, false);
    }
}
