package com.goehow.example.nfc_helper;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DialogListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private EditText editTextMessage;
    private Button buttonWrite;
    private Button buttonRead;
    private AlertDialog alertDialog;
    private NFCWriteFragment mNfcWriteFragment;
    private NFCReadFragment mNfcReadFragment;

    private boolean isDialogDisplayed = false;
    private boolean isWrite = false;

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent = getIntent();

        if(intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)!=null)
        Toast.makeText(this, intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES).toString(), Toast.LENGTH_SHORT).show();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage ndefMessage = null;
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if ((rawMessages != null) && (rawMessages.length > 0)) {
                ndefMessage = (NdefMessage)rawMessages[0];
            }

        }


        initViews();
        initNFC();
    }

    private void initViews() {

        editTextMessage = (EditText) findViewById(R.id.et_message);
        buttonWrite = (Button) findViewById(R.id.btn_write);
        buttonRead = (Button) findViewById(R.id.btn_read);

        //practice lambda expression
        buttonWrite.setOnClickListener(v -> showWriteFragment());
        buttonRead.setOnClickListener(v -> showReadFragment());

    }


    private void initNFC(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        checkNFCSetting();
    }

    private void checkNFCSetting(){

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("NFC Setting");
        alertBuilder.setMessage("NFC is required to be on");
        alertBuilder.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(intent);
                }

            }
        });
        alertBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        alertDialog = alertBuilder.create();

        if (!nfcAdapter.isEnabled()) {
            alertDialog.show();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        checkNFCSetting();

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        if(nfcAdapter != null){
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);

        if(alertDialog != null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
    }


    private void showWriteFragment() {
        isWrite = true;
        mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);
        if (mNfcWriteFragment == null) {
            mNfcWriteFragment = NFCWriteFragment.newInstance();
        }
        mNfcWriteFragment.show(getFragmentManager(),NFCWriteFragment.TAG);
    }

    private void showReadFragment() {

        mNfcReadFragment = (NFCReadFragment) getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
        if (mNfcReadFragment == null) {
            mNfcReadFragment = NFCReadFragment.newInstance();
        }
        mNfcReadFragment.show(getFragmentManager(),NFCReadFragment.TAG);
    }


    @Override
    public void onDialogDisplayed() {
        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {
        isDialogDisplayed = false;
        isWrite = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "onNewIntent: "+intent.getAction());

        if(tag != null) {
            Toast.makeText(this, "NFC Tag Detected !", Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);

            if (isDialogDisplayed) {

                if (isWrite) {

                    String messageToWrite = editTextMessage.getText().toString();
                    mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);
                    mNfcWriteFragment.onNfcDetected(ndef,messageToWrite);

                } else {

                    mNfcReadFragment = (NFCReadFragment)getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
                    mNfcReadFragment.onNfcDetected(ndef);
                }
            }
        }
    }

    @Override
    public void onBackPressed(){
        return;
    }

}
