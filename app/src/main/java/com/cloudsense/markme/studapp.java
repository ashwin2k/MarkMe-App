package com.cloudsense.markme;

import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class studapp extends AppCompatActivity {

    int progress = 0;
    CountDownTimer timer;
    String r;
    Boolean st=false;

    boolean stat;
    CustomGauge time;
    NfcAdapter adapter;
    Context con;
    int tick = 1;
    RelativeLayout alertbg;
    ArrayList<String> total_name=new ArrayList<>();

    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;

    private KeyStore keyStore;
    Dialog finger,d,load;
    TextView current;

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

        setContentView(R.layout.studappview);
        TextView today = findViewById(R.id.dt);
        finger=new Dialog(this);
        finger.setContentView(R.layout.fingerprint);
        today.setText(Utility.styleDate());
        time = findViewById(R.id.gauge2);
        time.setValue(progress);
        con = this;
        current = findViewById(R.id.time);
        alertbg = findViewById(R.id.back_bg);
        load=new Dialog(con);

        load.setContentView(R.layout.load_dia);
        load.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView summ = findViewById(R.id.summary);
        summ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(con, PersonalSummary.class));
            }
        });

        timer = new CountDownTimer(30000, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("TIMER", "Tick of Progress" + (30000 - millisUntilFinished) / 300 + "  " + millisUntilFinished);
                current.setText("" + millisUntilFinished / (1000));
                stat = true;
                time.setValue((int) (30000 - millisUntilFinished) / 300);

            }

            @Override
            public void onFinish() {
                time.setValue(100);
                stat = false;
            }
        };

        timer.start();


        adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null)
            Log.d("NFCTAG", adapter.toString());
        Button b=findViewById(R.id.qqq);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launchcam();


            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        finger = new Dialog(con);
        finger.setContentView(R.layout.fingerprint);
        finger.show();
        finger.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        handleIntent(intent);
        Log.d("NFCTASK", "DETECTED");
    }
    private void launchcam(){
        IntentIntegrator c=new IntentIntegrator(this); // `this` is the current Activity
      //  c.initiateScan(IntentIntegrator.ONE_D_CODE_TYPES);
        c.initiateScan();

    }
    private void handleIntent(Intent intent) {
        String action = intent.getAction();


        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if ("text/plain".equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTaskStuddApp(this, stat).execute(tag);

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
                    new NdefReaderTaskStuddApp(this, stat).execute(tag);
                    break;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupForegroundDispatch(this, adapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, adapter);

        super.onPause();
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
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

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                finger.show();
                FingerprintManager fpm=(FingerprintManager)con.getSystemService(FINGERPRINT_SERVICE);
                try{
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }
                if (initCipher()) {
                    //If the cipher is initialized successfully, then create a CryptoObject instance//
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                    // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                    FingerprintHandler helper = new FingerprintHandler(con, finger, new FingerprintHandler.results() {
                        @Override
                        public void getresults() {
                            if (r != null) {

                                Log.d("NFCTASK","Read content: " + r);
                                d=new Dialog(con);
                                if(true) {
                                    writeToFire(r);
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
                    });
                    st=helper.startAuth(fpm, cryptoObject);
                    Log.d("staa",st+"");
                    d=new Dialog(con);
                    if(st) {
                        writeToFire(r);

                        load.show();
                        d.show();
                    }
                    else {
                        Log.d("NFCERR","DENIED");
                        d.setContentView(R.layout.dialog_denied);
                        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        d.show();
                    }

                }


            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    protected void writeToFire(String res) {

        d.show();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("ashwinkumar14102000@gmail.com", "ohn4n4n4")
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d("FIREAUTH", "SUCCESS");


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FIREAUTH", "FAIL");

            }
        });
        Date c = Calendar.getInstance().getTime();
        Calendar c1 = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(c1.getTime());
        Log.d("Date", "DATE : " + strDate);
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);

        FirebaseFirestore fb = FirebaseFirestore.getInstance();

        //WRITE DATA
        Map<String, Object> user = new HashMap<>();
        user.put("UID", strDate);
        user.put("ROLL", res);
        user.put("BUS", (int) (Math.random() * 29) + 2);

        fb.collection("Attendance").document("x").collection(formattedDate).document("312318104027").set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("WRITEDB", "DocumentSnapshot successfully written!");
                int present = 0;
                Log.d("STATS", present + "");
                load.dismiss();



            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("WRITEDB", "Error writing document", e);
                    }
                });

    }


    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }
    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }
    public boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

}
