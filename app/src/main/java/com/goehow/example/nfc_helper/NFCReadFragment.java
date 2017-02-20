package com.goehow.example.nfc_helper;

import android.app.DialogFragment;
import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Kling on 2/14/17.
 */

public class NFCReadFragment extends DialogFragment {

    public static final String TAG = NFCWriteFragment.class.getSimpleName();

    public static NFCReadFragment newInstance() {
        return new NFCReadFragment();
    }

    private TextView textViewMessage;
    private DialogListener dialogListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_read, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        textViewMessage = (TextView) view.findViewById(R.id.tv_message);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dialogListener = (MainActivity) context;
        dialogListener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dialogListener.onDialogDismissed();
    }

    public void onNfcDetected(Ndef ndef) {
        //call from MainActivity
        readFromNFC(ndef);
    }


    private void readFromNFC(Ndef ndef) {

        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            ndefMessage.getRecords()[0].getTnf();
            NdefRecord[] records = ndefMessage.getRecords();
            String message = "";
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        message = message + " " + readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }



            Log.d(TAG, "readFromNFC: " + message);
            textViewMessage.setText(message);
            ndef.close();
        } catch (IOException | FormatException e) {
            e.printStackTrace();

        }
    }


    private String readText(NdefRecord record) throws UnsupportedEncodingException {

        byte[] payload = record.getPayload();


        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";


        int languageCodeLength = payload[0] & 0063;


        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

}
