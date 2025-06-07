package com.example.veggiecoach;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CropAdapter cropAdapter;
    private List<String> cropList;
    private List<String> docIdList; // 문서 ID 저장용

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        cropList = new ArrayList<>();
        docIdList = new ArrayList<>();

        // CropAdapter 생성 시 삭제 리스너 + 클릭 리스너 모두 전달
        cropAdapter = new CropAdapter(cropList, this::deleteCropFromFirebase, cropName -> {
            // 작물 클릭 시 작물 관리 화면으로 이동
            Intent intent = new Intent(SelectActivity.this, CropMainActivity.class);
            intent.putExtra("cropName", cropName);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.rvCrops);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(cropAdapter);

        ImageButton btnAddCrop = findViewById(R.id.btnAddCrop);
        btnAddCrop.setOnClickListener(v -> showFilterOptions());

        loadCropsFromFirebase();
    }

    private void showFilterOptions() {
        String[] options = {"딸기", "오이", "감자", "상추"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("추가할 작물 종류 선택")
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];

                    if (!cropList.contains(selected)) {
                        saveCropToFirebase(selected);
                    } else {
                        Toast.makeText(this, selected + " 이미 추가됨", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void saveCropToFirebase(String cropName) {
        Map<String, Object> cropData = new HashMap<>();
        cropData.put("cropName", cropName);
        cropData.put("timestamp", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .collection("myCrops")
                .add(cropData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, cropName + " 추가됨", Toast.LENGTH_SHORT).show();
                    loadCropsFromFirebase(); // 새로고침
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "파이어베이스 저장 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCropsFromFirebase() {
        db.collection("users")
                .document(userId)
                .collection("myCrops")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cropList.clear();
                    docIdList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String cropName = doc.getString("cropName");
                        if (cropName != null) {
                            cropList.add(cropName);
                            docIdList.add(doc.getId());
                        }
                    }

                    cropAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "작물 불러오기 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteCropFromFirebase(int position) {
        if (position < 0 || position >= docIdList.size()) return;

        String docId = docIdList.get(position);
        String cropName = cropList.get(position);

        db.collection("users")
                .document(userId)
                .collection("myCrops")
                .document(docId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, cropName + " 삭제됨", Toast.LENGTH_SHORT).show();
                    cropList.remove(position);
                    docIdList.remove(position);
                    cropAdapter.notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show();
                });
    }
}
