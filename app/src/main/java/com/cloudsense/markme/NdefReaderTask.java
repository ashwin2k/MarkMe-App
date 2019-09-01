package com.cloudsense.markme;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NdefReaderTask extends AsyncTask<Tag,Void,String> {
    Context con;
    ArrayList<String> total_name=new ArrayList<>();
    int students;
    Dialog d;
    String currentPhotoPath;
    Dialog load;
    String name;
    boolean stat;
    public NdefReaderTask(Context context,boolean status)
    {
        this.con=context;
        this.stat=status;
    }
    @Override
    protected String doInBackground(Tag... params) {
        Tag tag = params[0];

        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            return null;
        }

        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    return readText(ndefRecord);
                } catch (UnsupportedEncodingException e) {
                    Log.e("NFCTASK", "Unsupported Encoding", e);
                }
            }
        }

        return null;    }
    private String readText(NdefRecord record) throws UnsupportedEncodingException {

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;


        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.d("NFCTASK","Read content: " + result);
            d=new Dialog(con);
            if(stat) {
                writeToFirebase(result);
                load=new Dialog(con);
                load.setContentView(R.layout.load_dia);
                load.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                load.show();


            }
            else {
                Log.d("NFCERR","DENIED");
                d.setContentView(R.layout.dialog_denied);
                d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                d.show();
            }

        }
    }
    protected void writeToFirebase(String res)
    {


        FirebaseAuth mAuth =FirebaseAuth.getInstance();
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
        Date c = Calendar.getInstance().getTime();
        Calendar c1 = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(c1.getTime());
        Log.d("Date","DATE : " + strDate);
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);

        FirebaseFirestore fb = FirebaseFirestore.getInstance();
        fb.collection("Students").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                students=0;
                if(task.isSuccessful())
                {

                    for(DocumentSnapshot d:task.getResult())
                    {
                        students++;
                        total_name.add(d.getData().get("ROLL").toString());
                        Log.d("TOTAL",total_name+"");
                    }

                }
                else
                    Log.d("TOTAL STUDENTS","Error");

                Log.d("TOTAL STUDENTS",students+"");


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TOTAL STUDENTS","Fail");

            }
        });
        //WRITE DATA
        Map<String, Object> user = new HashMap<>();
        user.put("UID",  strDate);
        user.put("ROLL", res);
        user.put("BUS",(int)(Math.random()*29)+2);

        fb.collection("Attendance").document("CSE A").collection(formattedDate).document(res).set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("WRITEDB", "DocumentSnapshot successfully written!");
                load.dismiss();
                int present=0;
                Log.d("STATS",present+"");

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("WRITEDB", "Error writing document", e);
                    }
                });
        fb.collection("Students").document(res).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
               name= task.getResult().getData().get("NAME").toString();
               Log.d("NAME",name);
                if(stat) {
                    d.setContentView(R.layout.dialog_sucess);
                    d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    Log.d("Return",name);
                    TextView t=d.findViewById(R.id.welcome);
                    d.show();
                    t.setText("Welcome, "+name);

                }

                d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        load.dismiss();
                        d.dismiss();
                    }
                },15000);

            }
        });
    }

}
