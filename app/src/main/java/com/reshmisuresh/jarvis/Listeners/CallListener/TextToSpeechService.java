package com.reshmisuresh.jarvis.Listeners.CallListener;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class TextToSpeechService extends Service implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    public final static String TAG = "TextToSpeechService";

    public static final String TEXT_TO_READ = "text";
    private final String UTTERANCE_ID = "FINISHED_PLAYING";
    private final int MULTI_LINE = 2;

    private TextToSpeech tts;
    private ArrayList<String> texts;
    private boolean isInit;
    Activity act ;

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(getApplicationContext(), this);
        Log.d(TAG, "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        texts = intent.getStringArrayListExtra(TextToSpeechService.TEXT_TO_READ);

        if (isInit) {
            speak();
        }

        return TextToSpeechService.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        Log.d(TAG, "onInit");
        if (status == TextToSpeech.SUCCESS) {
            tts.setOnUtteranceCompletedListener(this);
            int result = tts.setLanguage(Locale.getDefault());
            if (result != TextToSpeech.LANG_MISSING_DATA
                    && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                speak();
                isInit = true;
            }
        }
    }

    private void startSpeechRecog() {
        Intent intentAcc = new Intent(this, SpeechRecogActivity.class);
        intentAcc.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        startActivity(intentAcc);
    }


    private void speak() {
        if (tts != null) {

            // Speak with 3 parameters deprecated but necessary on pre 21 version codes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // This is a single message
                String utteranceId = null;
                if (texts.size() < MULTI_LINE) {
                    // If is a single message this needs to be the last one
                    utteranceId = UTTERANCE_ID;
                }
                tts.speak(texts.get(0), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                if (texts.size() >= MULTI_LINE) {
                    for (int i = 1; i < texts.size(); i++) {
                        if (texts.size() - 1 == i) {
                            // If is the last message add the id
                            utteranceId = UTTERANCE_ID;
                        }
                        tts.speak(texts.get(i), TextToSpeech.QUEUE_ADD, null, utteranceId);
                    }
                }
            } else {
                HashMap<String, String> myHashAlarm = null;
                if (texts.size() < MULTI_LINE) {
                    myHashAlarm = new HashMap<>();
                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
                }
                tts.speak(texts.get(0), TextToSpeech.QUEUE_FLUSH, myHashAlarm);
                if (texts.size() >= MULTI_LINE) {
                    for (int i = 1; i < texts.size(); i++) {
                        if (texts.size() - 1 == i) {
                            // If is the last message add the id
                            myHashAlarm = new HashMap<>();
                            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                                    UTTERANCE_ID);
                        }
                        tts.speak(texts.get(i), TextToSpeech.QUEUE_ADD, myHashAlarm);
                    }
                }
            }
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                startSpeechRecog();
            }
        }, 6000);

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onUtteranceCompleted(String utteranceId) {
        startSpeechRecog();

    }
}
