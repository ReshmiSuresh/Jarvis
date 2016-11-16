package com.reshmisuresh.jarvis.Services.Conversation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.reshmisuresh.jarvis.JarvisApplication;
import com.reshmisuresh.jarvis.Services.BaseService;
import com.reshmisuresh.jarvis.audio.FeatureExtractor;
import com.reshmisuresh.jarvis.audio.MicrophoneRecorder;
import com.reshmisuresh.jarvis.audio.WekaClassifier;

public class ConversationClassifier extends BaseService implements MicrophoneRecorder.MicrophoneListener {
    public static String TAG = "ConversationClassifier";
    private JarvisApplication appState;
    private Object[] result = new Object[12];
    private int mSpeech = 0;
    public ConversationClassifier() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appState = (JarvisApplication) getApplicationContext();
        MicrophoneRecorder recorder = MicrophoneRecorder.getInstance();
        if (recorder.isRecording()) {
            Log.d(TAG, "stopping recording");
            recorder.stopRecording();
        }

        Log.d(TAG, "starting recording");
        recorder.registerListener(ConversationClassifier.this);
        recorder.startRecording();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void microphoneBuffer(short[] buffer, int window_size) {
        int voiced = 0;
        //TODO: Fill out appropriate numbers in the following for loop statement
        for(int k=0;k<8000;k+=200){
            //TODO: replace ‘??’ with appropriate numbers below
            double[] features = FeatureExtractor.ComputeFeaturesForFrame(buffer, 200, k);
            try {
                result=getObjectDoubleArray(features,result);
                double value = WekaClassifier.classify(result);
                if ((int)value ==0){
                    mSpeech++;
                }
                //TODO: classify whether the window is voiced or not
                //If output of the classifier is 0.0d, increment ‘voiced’ variable. If output is 1.0d, it is unvoiced. This is assuming that you have the order of classes written in arff file as: “speech{true,false}.”

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (mSpeech>=20) {
            Log.d(TAG,"Speech");
            appState.conversation_queue.add(1);
            mSpeech=0;
        }
        else {
            Log.d(TAG, "No Speech");
            appState.conversation_queue.add(0);
        }

        int convo_sum = sum(appState.conversation_queue.toArray());

        if (convo_sum>0.8*appState.CONVO_BUFFER_SIZE)
            appState.inConversation=true;
        else
            appState.inConversation=false;
        //TODO: After you find the number of ‘voiced’ windows, determine whether a-second-long audio mostly contain human voice or not by thresholding. Test out different thresholds and choose whichever works the best. Keep in mind that threshold should be less than the number of 25-ms-long windows that contain in a second. If ‘voiced’ variable is greater than a certain threshold, call sendSpeechStatusToUI() with speech variable=1.
    }

    public int sum(Object[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += (int)array[i];
        }
        return sum;
    }
    public static Object[] getObjectDoubleArray(double[] in,Object[] result){
        for (int i=0;i<in.length;i++){
            result[i] = new Double(in[i]);
        }
        return result;
    }
}
