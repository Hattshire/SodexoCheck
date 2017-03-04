package moe.laughingcross.jummymony;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.AdTargetingOptions;

import org.json.JSONObject;

import java.net.URL;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class MainPage extends AppCompatActivity {

    String Token;
    String ClientId;
    String ServiceId;
    String LoginParams;
    String SessionCookies;

    TextView name;
    TextView institution;
    TextView cardStatus;
    TextView balance;

    TextView latestBalance;
    TextView latestBalanceValue;

    AdLayout adView;

    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;

    //TODO: constant balance checking
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        preferences = PreferenceManager.getDefaultSharedPreferences( getBaseContext() );
        preferencesEditor = preferences.edit();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(myToolbar);

        Intent i = getIntent();
        ClientId = i.getStringExtra("ClientId");
        ServiceId = i.getStringExtra("ServiceId");
        LoginParams = i.getStringExtra("LoginParams");
        SessionCookies = i.getStringExtra("SessionCookies");
        Token = preferences.getString(
                "token",
                i.getStringExtra( "Token" )
        );
        ( (ImageView) findViewById(R.id.head) ).setColorFilter( getResources().getColor( R.color.colorAccent ), PorterDuff.Mode.SRC_IN );

        name = (TextView) findViewById( R.id.humanName );
        institution = (TextView) findViewById( R.id.school );
        cardStatus = (TextView) findViewById( R.id.cardStatus );
        balance = (TextView) findViewById( R.id.balance );

        latestBalance = (TextView) findViewById( R.id.lastBalance );
        latestBalanceValue = (TextView) findViewById( R.id.lastBalanceValue );

        name.setText( preferences.getString( "humanName", "" ) );
        institution.setText( preferences.getString( "institution", "" ) );
        cardStatus.setText(
                String.format(
                        getString(R.string.cardStatus),
                        preferences.getString( "cardStatus", "" )
                )
        );

        (new comeBotas()).execute();

        balance.setText(
                getString( R.string.balanceNull )
        );
        if  ( preferences.getBoolean( "screenTester", false ) ) {
            name.setText( getString( R.string.exampleName ) );
            institution.setText( getString( R.string.default_college ) );
        }
        else
        {
//////////////////////////////////////////////////////////////
            adView = (AdLayout) findViewById(R.id.mainPageAdView);
            adView.setListener(new MyAdListener());
            AdTargetingOptions adOptions = new AdTargetingOptions().enableGeoLocation(true);

            // Optional: Set ad targeting options here.
            adView.loadAd(adOptions); // Retrieves an ad on background thread
//////////////////////////////////////////////////////////////
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_toolbar, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch ( item.getItemId() )
        {
            case R.id.action_end:
                endSession();
                break;
            case R.id.action_refresh:
                refreshBalanceData();
                break;
            case R.id.action_about:
                changeToAbout();
            default:
                break;
        }
        return true;
    }

    void endSession()
    {
        if  ( preferences.getBoolean( "screenTester", false ) ) {
            preferencesEditor.remove( "screenTester" );
        }
        // Clear preferences and data
        preferencesEditor.putBoolean( "AutoLog", false);
        preferencesEditor.putString( "token", "");
        preferencesEditor.putString( "humanName", "");
        preferencesEditor.putString( "cardStatus", "");
        preferencesEditor.putString( "institution", "");
        preferencesEditor.putInt( "latestBalance", -1);
        preferencesEditor.commit();

        // Give SIG_LOGOUT message
        setResult(2);
        finish();
    }

    void refreshBalanceData()
    {
        new comeBotas().execute();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == 2 ){
            setResult( 2 );
            finish();
        }
    }


    public void changeToLatestTransactions( View view )
    {
        Intent i = new Intent( this, latestTransactions.class);
        i.putExtra( "Token", Token );
        startActivityForResult( i, 0 );
    }

    public void changeToAbout()
    {
        Intent i = new Intent( this, About.class);
        startActivityForResult( i, 0 );

    }

    class comeBotas extends AsyncTask<URL, Integer, String>
    {
        int _balance;

        @Override
        protected void onPreExecute(  )
        {
            _balance = preferences.getInt( "latestBalance", -1 );
            balance.setText(
                    getString( R.string.balanceNull )
            );
            if ( _balance >= 0 ) {
                latestBalanceValue.setText(
                        String.format(
                                getString(R.string.balanceFormat),
                                _balance
                        )
                );
            }
            else {
                latestBalanceValue.setText(
                                getString(R.string.balanceNull)
                );
            }
            latestBalance.setVisibility( View.VISIBLE );
            latestBalanceValue.setVisibility( View.VISIBLE );
        }

        @Override
        protected String doInBackground( URL... urls )
        {
            PARSEC parser = new PARSEC();
            String response = parser.GET( "/wp-content/plugins/beneficiarios/api.php?action=balance&token=" + Token + "&from=19/12/2016&to=19/01/2017&clientid=1&serviceid=15" );

            if ( response.equals( "ERROR|connectionFailed" ) )
            {
                return response;
            }
            else if ( response.startsWith( "ERROR" ) )
            {
                Log.d( "JunaCoffee", String.format( "Catch this! %s", response ) );
                return response;
            }
            try {
                //TODO: check problems here
                JSONObject transactions = new JSONObject( response );
                if ( transactions.getJSONObject( "result" )
                        .getInt( "O_ERROR_CODE" ) == 0
                        ) {
                    _balance = transactions.getJSONObject("result").getJSONObject("return").getInt("amountBalance");
                } else {
                    Log.w( "ERROR", "Can't retrieve balance" );
                    cancel(true);
                }
            } catch ( Exception e) {
                Log.e("ERROR", e.getLocalizedMessage() , e.getCause() );
            }
            return "";
        }

        @Override
        protected void onPostExecute( String texto )
        {
            //TODO: Error handling
            if ( texto.length() > 0 )
                return;

            preferencesEditor.putInt( "latestBalance", _balance );
            preferencesEditor.commit();

            balance.setText(
                    String.format(
                            getString( R.string.balanceFormat ),
                            _balance
                    )
            );
            latestBalance.setVisibility( View.GONE );
            latestBalanceValue.setVisibility( View.GONE );
        }
    }
}
