package com.cloudsense.markme;

import android.graphics.Color;
import android.os.Bundle;
import android.util.EventLogTags;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PersonalSummary extends AppCompatActivity {
    long attended=0;
    PieChart pieChart;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pers_summary_layout);
         pieChart = findViewById(R.id.piechart);

        FirebaseFirestore fb = FirebaseFirestore.getInstance();
        fb.collection("Students").document("312318104072").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                    attended=(Long)documentSnapshot.get("ATTENDED");
                    Log.d("ATTENDED",attended+"");
                ArrayList NoOfEmp = new ArrayList();
                pieChart.setCenterText("Out of 80 days");
                pieChart.setDescription("Personalized attendance summary");

                NoOfEmp.add(new Entry((float)attended, 0));
                NoOfEmp.add(new Entry(18f, 1));
                NoOfEmp.add(new Entry(2f, 2));

                PieDataSet dataSet = new PieDataSet(NoOfEmp, " ");

                ArrayList year = new ArrayList();

                year.add("Present");
                year.add("Absent");
                year.add("OD");

                PieData data = new PieData(year, dataSet);
                pieChart.setData(data);
                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                pieChart.animateXY(5000, 5000);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
}
