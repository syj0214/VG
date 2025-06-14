package com.example.veggiecoach;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etPasswordConfirm;
    private Button btnSignUp, btnCheckDuplicate;
    private FirebaseAuth mAuth;
    private boolean isEmailChecked = false; // 중복확인 여부 저장 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etSignUpEmail);
        etPassword = findViewById(R.id.etSignUpPassword);
        etPasswordConfirm = findViewById(R.id.etSignUpPasswordConfirm);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnCheckDuplicate = findViewById(R.id.btnCheckDuplicate);

        btnCheckDuplicate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEmailDuplicate();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void checkEmailDuplicate() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("이메일을 입력해주세요.");
            return;
        }

        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                    if (isNewUser) {
                        Toast.makeText(SignupActivity.this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                        isEmailChecked = true;
                    } else {
                        Toast.makeText(SignupActivity.this, "이미 사용 중인 이메일입니다.", Toast.LENGTH_SHORT).show();
                        isEmailChecked = false;
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "중복 확인 실패: " +
                            (task.getException() != null ? task.getException().getMessage() : "알 수 없는 오류"), Toast.LENGTH_SHORT).show();
                    isEmailChecked = false;
                }
            }
        });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("이메일을 입력해주세요.");
            return;
        }
        if (!isEmailChecked) {
            Toast.makeText(this, "이메일 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("비밀번호를 입력해주세요.");
            return;
        }
        if (!password.equals(passwordConfirm)) {
            etPasswordConfirm.setError("비밀번호가 일치하지 않습니다.");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("비밀번호는 최소 6자리 이상이어야 합니다.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignupActivity.this, "회원가입 성공: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                            // 회원가입 성공 시 로그인 화면으로 이동
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(SignupActivity.this, "이미 사용 중인 이메일입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignupActivity.this, "회원가입 실패: " +
                                        (task.getException() != null ? task.getException().getMessage() : "알 수 없는 오류"), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
