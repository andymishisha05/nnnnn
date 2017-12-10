package ip.company.project1.currencyconverter.syncadapter;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ip.company.project1.currencyconverter.NavActivity;
import ip.company.project1.currencyconverter.R;
import ip.company.project1.currencyconverter.helper.NotificationUtils;

/**
 * Created by EsSaM on 5/10/2017.
 */

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 6;
    public static final int SYNC_FLEXTIME = 2;


    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
       // String locationQuery = Utility.getPreferredLocation(getContext());

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;
        try {
            final String FORECAST_BASE_URL =
                    "http://ip2web.com/ip2web-android/news-all.php?";


            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            forecastJsonStr = buffer.toString();
            getWeatherDataFromJson(forecastJsonStr,null);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String forecastJsonStr,
                                        String locationSetting)
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OWM_ID = "id";
        final String OWM_NEWS_NAME = "name";
        final String OWM_WSF = "wsf";



        try {

            JSONArray jsonArray =new JSONArray(forecastJsonStr);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String id = jsonObject.getString(OWM_ID);
            String name = jsonObject.getString(OWM_NEWS_NAME);
            String wsf = jsonObject.getString(OWM_WSF);

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();
            NotificationUtils.setIDOnPrefernces(getContext(), id);

            if (!NotificationUtils.getID(getContext()).equals(NotificationUtils.getNewID(getContext()))) {
                NotificationUtils.setNewID(getContext(), id);
                notifyWeather();
            }
            int inserted = 0;
            // add to database

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void notifyWeather() {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

      //  Bitmap image = BitmapFactory.decodeStream(ImgURL.openConnection().getInputStream());
        Intent i = new Intent (getContext(), NavActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NotificationCompat.Builder builder = new NotificationCompat.Builder (getContext())
                .setContentTitle("fffffff")
                .setContentText("gggggggggg")
                .setSmallIcon(R.drawable.log2)
                //.setLargeIcon(image)
                .setSound(alarmSound)
                .setAutoCancel(true);


        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(getContext());
        taskStackBuilder.addNextIntentWithParentStack(i);
        PendingIntent resultPendingIntent = taskStackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager manager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(4444,builder.build());
        }


    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) throws Exception {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest.Builder b = (new SyncRequest.Builder()).syncPeriodic(syncInterval, flexTime);
            b.setSyncAdapter(account, authority);
            b.setExtras(new Bundle());
            ContentResolver.requestSync(b.build());
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) throws Exception {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);


    }


    public static Account getSyncAccount(Context context) throws Exception {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));


        if ( null == accountManager.getPassword(newAccount) ) {

            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }


            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) throws Exception {
        /*
         * Since we've created an account
         */

      //  SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
         SunshineSyncAdapter.syncAllAccountsPeriodically(context,3);
      //  SunshineSyncAdapter.configurePeriodicSync(context,SYNC_INTERVAL,SYNC_FLEXTIME);


        ContentResolver.setIsSyncable(newAccount, context.getString(R.string.content_authority), 1);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) throws Exception {
        getSyncAccount(context);
    }
    public static void syncAllAccountsPeriodically(Context contextAct, long seconds) throws Exception {
        AccountManager manager = AccountManager.get(contextAct);

        Account[] accounts = manager.getAccountsByType(contextAct.getString(R.string.sync_account_type));
        String accountName = "";
        String accountType = "";
        for (Account account : accounts) {
            accountName = account.name;
            accountType = account.type;
            break;
        }
        Account a = new Account(accountName, accountType);
        ContentResolver.addPeriodicSync(a, contextAct.getString(R.string.content_authority), new Bundle(), seconds*1000);
    }

}