package com.example.veggiecoach;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class CropInfoFragment extends Fragment {

    private static final String ARG_CROP_NAME = "cropName";
    private static final String TAG = "CropInfoFragment";

    private String cropName;
    private TextView textViewCropInfo;

    private FirebaseFirestore db;

    public static CropInfoFragment newInstance(String cropName) {
        CropInfoFragment fragment = new CropInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CROP_NAME, cropName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_crop_info, container, false);
        textViewCropInfo = root.findViewById(R.id.textViewCropInfo);

        if (getArguments() != null) {
            cropName = getArguments().getString(ARG_CROP_NAME);
        }

        Log.d(TAG, "Received cropName: " + cropName);

        db = FirebaseFirestore.getInstance();

        fetchCropInfo();

        return root;
    }

    private void fetchCropInfo() {
        if (cropName == null || cropName.trim().isEmpty()) {
            textViewCropInfo.setText("작물 이름이 전달되지 않았습니다.");
            return;
        }

        CollectionReference collectionRef = db.collection("crops_info");

        collectionRef.whereEqualTo("name", cropName)
                .get()
                .addOnSuccessListener(this::handleQuerySuccess)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore fetch error", e);
                    textViewCropInfo.setText("정보를 불러오는 중 오류가 발생했습니다.");
                });
    }

    private void handleQuerySuccess(QuerySnapshot querySnapshot) {
        if (!isAdded()) return;  // Fragment가 Activity에 붙어있지 않으면 종료

        if (querySnapshot.isEmpty()) {
            textViewCropInfo.setText("해당 작물 정보를 찾을 수 없습니다.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (DocumentSnapshot doc : querySnapshot) {
            String name = doc.getString("name");

            // growthTime은 Number 타입이므로 Object로 받고 toString 처리
            Object growthTimeObj = doc.get("growthTime");
            String growthTimeStr = (growthTimeObj != null) ? growthTimeObj.toString() : "정보 없음";

            sb.append("작물명: ").append(name != null ? name : "알 수 없음").append("\n")
                    .append("성장 기간: ").append(growthTimeStr).append("\n");
        }

        textViewCropInfo.setText(sb.toString());
    }
}
