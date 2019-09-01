package com.cloudsense.markme;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class AttendanceSummary extends AppCompatActivity {
    int students;
   ArrayList<String> pres_name=new ArrayList<>();
    int present=0;
    int today=0,yester=0;
    Context con;
    CustomGauge gauge;
    DatePicker fp;
    ListView preslist;
    Dialog dp;
    BarChart chart;
    TextView pres;
    View v;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.content_main);
        chart=findViewById(R.id.max);
        dp=new Dialog(this);
        dp.setContentView(R.layout.picker_dia);
        pres=findViewById(R.id.pres_no);

        preslist=findViewById(R.id.pres_list);

        TextView cont_date=findViewById(R.id.dt_cont);
        cont_date.setText(Utility.styleDate().substring(0,Utility.styleDate().length()-2));
        cont_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(con,chart,gauge,preslist,pres);
                newFragment.show(getSupportFragmentManager(), "datePicker");

            }
        });

        students=0;
        RelativeLayout sc=findViewById(R.id.scroll);
        v=sc.getRootView();
        gauge=findViewById(R.id.present_count);
        gauge.setEndValue(10);

        FirebaseAuth mAuth =FirebaseAuth.getInstance();
        con=this;
        mAuth.signInWithEmailAndPassword("ashwinkumar14102000@gmail.com", "ohn4n4n4")
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d("FIREAUTH","SUCCESS");


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FIREAUTH","FAIL");

            }
        });
        FirebaseFirestore fb= FirebaseFirestore.getInstance();
        fb.collection("Students").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                students=0;
                if(task.isSuccessful())
                {

                        for(DocumentSnapshot d:task.getResult())
                            students++;
                    TextView total_students=findViewById(R.id.pres_tot);

                    total_students.setText("out of "+students);

                }
                else
                    Log.d("TOTAL STUDENTS","Error");


            }
        });

        fb.collection("Attendance").document("CSE A").collection(Utility.dateGet()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                present=0;
                if(task.isSuccessful()) {
                    for (DocumentSnapshot d : task.getResult()) {
                        pres_name.add(d.getData().get("ROLL").toString());
                        Log.d("PRESENT",pres_name+"");
                        present++;
                    }


                }
                else
                    Log.d("TOTAL STUDENTS","Error");



            }
        });
        fb.collection("Attendance").document("CSE A").collection(Utility.prevDate()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                yester=0;
                if(task.isSuccessful()) {
                    for (DocumentSnapshot d : task.getResult()) {
                        Log.d("PRESENTYEST  ",yester+"");
                        yester++;
                    }
                    barSet(yester,present);

                    ArrayAdapter adapter=new ArrayAdapter(con,android.R.layout.simple_list_item_1,pres_name);
                    preslist.setAdapter(adapter);
                    gauge.setValue(present);
                    pres.setText(present+"");
                    Log.d("TOTAL STUDENTS",pres_name.size()+"");

                    Log.d("TOTALA",students+" "+pres_name.size() );
                }
                else
                    Log.d("TOTAL STUDENTS","Error");

            }
        });




    }
    public void barSet(int v1,int v2)
    {
        Log.d("BARSET","SETTING"+v1+v2);
        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setAxisMinValue(0f);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setAxisMinValue(0f);
        ArrayList<String> labels=new ArrayList<>();
        labels.add("Yesterday");
        labels.add("Today");
        ArrayList<BarEntry> data=new ArrayList<>();
        BarEntry tod=new BarEntry(v1,0);
        BarEntry yest=new BarEntry(v2,1);
        data.add(yest);
        data.add(tod);

        BarDataSet dataSet=new BarDataSet(data,"Attendance Turnover");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData dat=new BarData(labels,dataSet);
        dataSet.setBarSpacePercent(50f);
        chart.setData(dat);
        chart.setDescription("COMPARISON");
        chart.animateXY(2000, 2000);
        chart.invalidate();
    }


}

 class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    int present;
    ArrayList<String> pres_name=new ArrayList<>();
    Context con;
    BarChart chart;
    FirebaseFirestore fb;
    int count;
    int today;
    int y,m,d;
    FirebaseAuth mAuth;
    TextView pres;
    CustomGauge gauge;
     String set;
    ListView preslist;

     public DatePickerFragment(Context con,BarChart ch,CustomGauge gg,ListView pl,TextView tw)
    {
        this.con=con;
        chart=ch;
        gauge=gg;
        preslist=pl;
        pres=tw;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        fb=FirebaseFirestore.getInstance();
        fb.collection("Attendance").document("CSE A").collection(Utility.dateGet()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                today=0;
                if(task.isSuccessful())
                {
                    for(DocumentSnapshot d:task.getResult()){
                        today++;
                    }
                }

            }
        });

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        return  dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
          set=Utility.convDate(month + 1, year, day);
        Log.d("SET TIME", set);

        fb.collection("Attendance").document("CSE A").collection(Utility.convDate(month+1,year,day)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                present=0;
                if(task.isSuccessful()) {
                    for (DocumentSnapshot d : task.getResult()) {
                        pres_name.add(d.getData().get("ROLL").toString());
                        Log.d("PRESENT",pres_name+"");
                        present++;
                    }
                }
                else
                    Log.d("TOTAL STUDENTS","Error");

                ArrayAdapter adapter=new ArrayAdapter(con,android.R.layout.simple_list_item_1,pres_name);
                preslist.setAdapter(adapter);
                Log.d("TOTAL STUDENTS",present+"");
                gauge.setValue(present);
                pres.setText(present+"");
                Log.d("PREV",Utility.prevDate());
                barSet(today,present,set);


            }
        });
        fb.collection("Attendance").document("CSE A").collection(Utility.dateGet()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                today=0;
                if(task.isSuccessful())
                {
                    for(DocumentSnapshot d:task.getResult()){
                        today++;
                    }
                }barSet(today,present,set);

            }
        });




    }
     public void barSet(int v1,int v2,String date)
     {
         YAxis yAxisLeft = chart.getAxisLeft();
         yAxisLeft.setAxisMinValue(0f);

         YAxis yAxisRight = chart.getAxisRight();
         yAxisRight.setAxisMinValue(0f);
         ArrayList<String> labels=new ArrayList<>();
         labels.add(date);
         labels.add("Today");
         ArrayList<BarEntry> data=new ArrayList<>();
         BarEntry tod=new BarEntry(v2,0);
         BarEntry yest=new BarEntry(v1,1);
         data.add(yest);
         data.add(tod);

         BarDataSet dataSet=new BarDataSet(data,"Attendance Turnover");
         dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
         BarData dat=new BarData(labels,dataSet);
         dataSet.setBarSpacePercent(50f);
         chart.setData(dat);
         chart.setDescription("COMPARISON");
         chart.animateXY(2000, 2000);
         chart.invalidate();
     }
}