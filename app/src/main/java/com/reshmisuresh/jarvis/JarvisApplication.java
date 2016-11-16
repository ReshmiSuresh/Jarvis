package com.reshmisuresh.jarvis;

import android.app.Application;
import android.content.Context;
import android.location.Location;

import com.android.internal.telephony.ITelephony;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.Queue;


public class JarvisApplication extends Application {
    public static Context context;
    public int CONVO_BUFFER_SIZE = 300;
    public Queue<Integer> conversation_queue = new CircularFifoQueue<Integer>(CONVO_BUFFER_SIZE);
    public boolean inConversation = false;
    public boolean incomingCall = false;
    public int detectedActivity = -1;
    public Location lastLocation = new Location("");
    public boolean inMeeting = false;
    public boolean isDriving = false;
    public ITelephony telephonyService;
    public JarvisApplication(){

        context = this;
        lastLocation.setLatitude(0.0d);
        lastLocation.setLongitude(0.0d);

    }
}
