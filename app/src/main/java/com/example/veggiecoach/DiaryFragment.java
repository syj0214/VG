package com.example.veggiecoach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DiaryFragment extends Fragment {

    public static DiaryFragment newInstance() {
        return new DiaryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_diary, container, false);

        // 예시 텍스트 - 나중에 실제 영농일지 내용으로 바꾸면 됨
        TextView tvDiaryInfo = root.findViewById(R.id.tvDiaryInfo);
        tvDiaryInfo.setText("여기에 영농일지 내용을 표시하세요.");

        return root;
    }
}
