package com.reshmisuresh.jarvis.Listeners.CallListener;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.reshmisuresh.jarvis.JarvisApplication;
import com.android.internal.telephony.*;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class CallReceiver extends BroadcastReceiver {
    JarvisApplication appState;
    TextToSpeech t1;


    @Override
    public void onReceive(final Context context, Intent intent) {

        appState = (JarvisApplication) context.getApplicationContext();
        if (!appState.incomingCall) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                Class c = Class.forName(tm.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                appState.telephonyService = (ITelephony) m.invoke(tm);
                Bundle bundle = intent.getExtras();
                String phoneNumber = bundle.getString("incoming_number");
                Log.e("INCOMING", phoneNumber);
                if (phoneNumber != null) {
                    appState.incomingCall = true;
//                telephonyService.silenceRinger();
//                acceptCall(context);
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamMute(AudioManager.STREAM_RING, true);
//                appState.telephonyService.endCall();

                    askVoiceQuestion(context,lookupContact(context, phoneNumber));
                    Log.e("HANG UP", phoneNumber);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }else {
            appState.incomingCall = false;
        }
    }

    private String lookupContact(Context context, String phoneNumber) {
        String contact = "";
        ContentResolver localContentResolver = context.getContentResolver();
        Cursor contactLookupCursor =
                localContentResolver.query(
                        Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                                Uri.encode(phoneNumber)),
                        new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                        null,
                        null,
                        null);
        try {
            while(contactLookupCursor.moveToNext()){
                String contactName = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                String contactId = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                contact = contactName;
            }
        } finally {
            contactLookupCursor.close();
        }
        if (contact.equals(""))
            contact = "Unknwon";
        return contact;
    }


    private void askVoiceQuestion(Context context, String contact) {
        Intent speechIntent = new Intent(context, TextToSpeechService.class);
        ArrayList<String> allMessagesToRead = new ArrayList<>();
        allMessagesToRead.add(contact+" Calling, Say Yes to Answer, or No to reject, after the beep. ");
        speechIntent.putStringArrayListExtra(TextToSpeechService.TEXT_TO_READ, allMessagesToRead);
        context.startService(speechIntent);

    }

    public void acceptCall(Context context){
        Intent intentAcc = new Intent(context, CallAcceptActivity.class);
        intentAcc.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        context.startActivity(intentAcc);
    }
}