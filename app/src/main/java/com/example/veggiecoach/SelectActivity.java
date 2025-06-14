package com.example.veggiecoach;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.View;

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
    private List<String> docIdList;

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

        cropAdapter = new CropAdapter(cropList, this::showDeleteConfirmation, cropName -> {
            Intent intent = new Intent(SelectActivity.this, CropMainActivity.class);
            intent.putExtra("cropName", cropName);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.rvCrops);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(cropAdapter);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels));

        ImageButton btnAddCrop = findViewById(R.id.btnAddCrop);
        btnAddCrop.setOnClickListener(v -> showFilterOptions());

        loadCropsFromFirebase();
    }

    private void showFilterOptions() {
        String[] options = {"딸기", "오이", "감자", "상추"};

        new AlertDialog.Builder(this)
                .setTitle("추가할 작물 종류 선택")
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
                    loadCropsFromFirebase();
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

    private void showDeleteConfirmation(int position) {
        String cropName = cropList.get(position);
        new AlertDialog.Builder(this)
                .setTitle("작물 삭제")
                .setMessage(cropName + " 을(를) 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteCropFromFirebase(position))
                .setNegativeButton("취소", null)
                .show();
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

    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private final int spanCount;
        private final int spacing;

        public GridSpacingItemDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            // 좌우 여백 동일하게 spacing / 2
            outRect.left = spacing / 2;
            outRect.right = spacing / 2;

            // 첫 번째 줄이면 위쪽 여백 추가
            if (position < spanCount) {
                outRect.top = spacing;
            } else {
                outRect.top = 0;
            }

            // 아래쪽 여백 항상 spacing
            outRect.bottom = spacing;
        }
    }
}
