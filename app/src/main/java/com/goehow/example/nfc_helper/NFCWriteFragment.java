package com.goehow.example.nfc_helper;

import android.app.DialogFragment;
import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Kling on 2/14/17.
 */

public class NFCWriteFragment extends DialogFragment{

    public static final String TAG = NFCWriteFragment.class.getSimpleName();

    public static NFCWriteFragment newInstance() {
        return new NFCWriteFragment();
    }

    private TextView textViewMessage;
    private ProgressBar progressBar;
    private DialogListener dialogListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write,container,false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        textViewMessage = (TextView) view.findViewById(R.id.tv_message);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dialogListener = (MainActivity)context;
        dialogListener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dialogListener.onDialogDismissed();
    }


    public void onNfcDetected(Ndef ndef, String messageToWrite){

        progressBar.setVisibility(View.VISIBLE);
        writeToNfc(ndef,messageToWrite);
    }

    private void writeToNfc(Ndef ndef, String message){

        textViewMessage.setText("Writing to Tag...");
        if (ndef != null) {

            try {
                ndef.connect();

                NdefRecord appRecord = NdefRecord.createApplicationRecord( getActivity().getApplicationContext().getPackageName() );
                NdefRecord textRecord = NdefRecord.createTextRecord("en", message);
                NdefMessage ndefMessage = new NdefMessage(textRecord,appRecord);

                ndef.writeNdefMessage(ndefMessage);

                ndef.close();

                Vibrator vibrator = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(500);

                //Write Successful
                textViewMessage.setText("Successfully Written to Tag!");

            } catch (IOException | FormatException e) {
                e.printStackTrace();

                Vibrator vibrator = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(500);

                textViewMessage.setText("Error Writing Message !");

            } finally {
                progressBar.setVisibility(View.GONE);
            }

        }
    }

}
