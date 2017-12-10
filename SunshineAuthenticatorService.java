package ip.company.project1.currencyconverter.syncadapter;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * The service which allows the sync adapter framework to access the authenticator.
 */
public class SunshineAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private SunshineAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new SunshineAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}