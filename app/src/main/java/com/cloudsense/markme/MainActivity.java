package com.cloudsense.markme;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import pl.pawelkleczkowski.customgauge.CustomGauge;
import android.nfc.NfcAdapter;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    int progress = 0;
    CountDownTimer timer;
    CountDownTimer hourly;
    boolean stat;
    CustomGauge time;
    int period=0;
    String currentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    ContentValues values;
    UploadTask uploadTask;
    NfcAdapter adapter;
    Context con;
    int tick=1;
    Uri imageUri;
    RelativeLayout alertbg;
    TextView current;
    TransitionDrawable trans;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_main);
        TextView today=findViewById(R.id.dt);
        today.setText(Utility.styleDate());
        time = findViewById(R.id.gauge2);
        time.setValue(progress);
        con=this;
        current=findViewById(R.id.time);
        alertbg=findViewById(R.id.back_bg);
        Animation anim=AnimationUtils.loadAnimation(this,R.anim.trans);
        MaterialCardView cd=findViewById(R.id.sneak);
        cd.setAnimation(anim);
        cd.startAnimation(anim);
        TextView summ=findViewById(R.id.summary);
        summ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(con,AttendanceSummary.class));
            }
        });

        timer = new CountDownTimer(30000, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("TIMER", "Tick of Progress" + (30000 - millisUntilFinished) / 300 + "  " + millisUntilFinished);
                current.setText(""+millisUntilFinished/ (1000));
                stat=true;
                time.setValue((int) (30000 - millisUntilFinished) / 300);

            }

            @Override
            public void onFinish() {
                time.setValue(100);
                stat=false;
            }
        };
        hourly=new CountDownTimer(36000000,3600000) {
            @Override
            public void onTick(long millisUntilFinished) {
                period++;
                int rem=(int)((36000000-millisUntilFinished)/360000)*60;
                Log.d("TO NEXT ATTENDANCE",rem+" ");
                NotificationCompat.Builder builder=new NotificationCompat.Builder(con);
                builder.setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(period+"th Attendance check")
                        .setContentText("Next attendance in "+rem+" minutes")
                        .setOngoing(true)
                        .setContentInfo(period+"/10")
                        .setSubText("Out of 10 periods")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(0, builder.build());
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();
        hourly.start();


        adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null)
            Log.d("NFCTAG", adapter.toString());
        LottieAnimationView stud= findViewById(R.id.student_ed);
        stud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nx= new Intent(con,studapp.class);
                startActivity(nx);
            }
        });


    }














    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
        Log.d("NFCTASK","DETECTED");
    }
    private void handleIntent(Intent intent) {

        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cam.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cam.putExtra("android.intent.extras.CAMERA_FACING", 1);

            startActivityForResult(cam, 111);

            String type = intent.getType();
            if ("text/plain".equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask(this,stat).execute(tag);

            } else {
                Log.d("NFCTASK", "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask(this,stat).execute(tag);
                    break;
                }
            }
        }
    }
        @Override
        protected void onResume () {
            super.onResume();

            setupForegroundDispatch(this, adapter);
        }
        @Override
        protected void onPause () {
            /**
             * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
             */
                 stopForegroundDispatch(this, adapter);

            super.onPause();
        }
        public static void setupForegroundDispatch ( final Activity activity, NfcAdapter adapter){
            final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

            IntentFilter[] filters = new IntentFilter[1];
            String[][] techList = new String[][]{};

            // Notice that this is the same filter as in our manifest.
            filters[0] = new IntentFilter();
            filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
            filters[0].addCategory(Intent.CATEGORY_DEFAULT);
            try {
                filters[0].addDataType("text/plain");
            } catch (IntentFilter.MalformedMimeTypeException e) {
                throw new RuntimeException("Check your mime type.");
            }

            adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
        }
        public static void stopForegroundDispatch ( final Activity activity, NfcAdapter adapter){
            adapter.disableForegroundDispatch(activity);
        }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case 111:
                if (requestCode == 111)
                    if (resultCode == Activity.RESULT_OK) {
                        try {

                            String imageurl = getRealPathFromURI(imageUri);

                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            Log.d("PICx","fs"+storage);
                            StorageReference storageRef = storage.getReference();
                            Log.d("PICx","sr"+storageRef);

                            StorageReference mountainsRef = storageRef.child("01.jpg");

                            Uri stream = Uri.fromFile(new File(imageurl));
                            uploadTask = mountainsRef.putFile(stream);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Log.d("PICx","NOPE");
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.d("PICx","uploaded");
                                }
                            });
                            Log.d("BIT",imageurl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
        }
    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


}
