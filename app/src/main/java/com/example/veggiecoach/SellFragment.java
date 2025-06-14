package com.example.veggiecoach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SellFragment extends Fragment {

    public SellFragment() {
        // 기본 생성자 필요
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // fragment_sell.xml 레이아웃을 인플레이트
        return inflater.inflate(R.layout.fragment_sell, container, false);
    }
}
