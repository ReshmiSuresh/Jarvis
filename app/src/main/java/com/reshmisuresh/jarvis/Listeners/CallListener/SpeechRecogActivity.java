package com.reshmisuresh.jarvis.Listeners.CallListener;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.reshmisuresh.jarvis.JarvisApplication;
import com.reshmisuresh.jarvis.R;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechRecogActivity extends Activity {

    private static final int REQ_CODE_SPEECH_INPUT = 110 ;
    JarvisApplication appState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appState = (JarvisApplication) getApplicationContext();
        listen();
    }

    public void listen() {


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);
                    Log.d("Speech",text);
                    if(text.toLowerCase().contains("no")){
                        try {
                            appState.telephonyService.endCall();
                            sendMessage();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    if (text.toLowerCase().contains("yes")){
                        acceptCall(this);
                    }
                }
                break;
            }

        }
        finish();
    }

    private void sendMessage() {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("+16036676881", null, "I am driving at the moment. I will call you back.", null, null);
    }

    public void acceptCall(Context context){
        Intent intentAcc = new Intent(context, CallAcceptActivity.class);
        intentAcc.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        context.startActivity(intentAcc);
    }
}
