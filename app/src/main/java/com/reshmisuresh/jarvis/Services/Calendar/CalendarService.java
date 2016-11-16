package com.reshmisuresh.jarvis.Services.Calendar;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.reshmisuresh.jarvis.JarvisApplication;
import com.reshmisuresh.jarvis.Services.BaseService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class CalendarService extends BaseService {
    public static String TAG = "CalendarService";
    JarvisApplication appState;
    public CalendarService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appState = (JarvisApplication) getApplicationContext();
        new StartCalendarCollectionTask().execute();
        mCalendarHandler.postDelayed(mCalendarTask, mCollectionInterval);


    }

    private class StartCalendarCollectionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            startDataCollection();
            return null;
        }
    }

    private void startDataCollection() {
        Uri content = Uri.parse("content://com.android.calendar/events");
        String[] vec = new String[] { "calendar_id", "title", "description", "dtstart", "dtend", "eventLocation" };
        String selectionClause = "(dtstart >= ? AND dtend >= ?)";
        long dtstart = System.currentTimeMillis()-mCollectionInterval;
        long dtend = System.currentTimeMillis();
        String[] selectionsArgs = new String[]{"" + dtstart, "" + dtend};

        Cursor cursor = appState.getContentResolver()
                .query(
                        content, vec, selectionClause,selectionsArgs,null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {

            Log.d(TAG,cursor.getString(1));
            Log.d(TAG,getDate(Long.parseLong(cursor.getString(3))));
            Log.d(TAG,getDate(Long.parseLong(cursor.getString(4))));
            String loc = cursor.getString(5);
            if (loc.length()>5) {
                Location cal_loc = getLocationFromAddress(loc);
                Log.d(TAG, "Location---" + cal_loc);

                if (cal_loc.distanceTo(appState.lastLocation)<100)
                    appState.inMeeting = true;

            }

            cursor.moveToNext();

        }
    }
    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final long mCollectionInterval = 15 * 60 * 1000;
    private Handler mCalendarHandler = new Handler();
    private Runnable mCalendarTask = new Runnable() {
        @Override
        public void run() {
            mCalendarHandler.removeCallbacks(mCalendarTask);
            new StartCalendarCollectionTask().execute();
            mCalendarHandler.postDelayed(mCalendarTask, mCollectionInterval);
            Log.e(TAG, "calendar");
        }
    };

    public Location getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        Location loc = new Location("location");
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLongitude();
            loc.setLatitude(location.getLatitude());
            loc.setLongitude(location.getLongitude());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return loc;
    }
}
