package com.example.messagingapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure this layout exists
        Map<String,String> data = new HashMap<>();
        FirebaseFirestore.getInstance().collection("test").add(data);
    }
}
