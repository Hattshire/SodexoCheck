package moe.laughingcross.jummymony;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONObject;

import java.net.URL;

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


    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;

    //TODO: constant balance checking
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        preferences = PreferenceManager.getDefaultSharedPreferences( getBaseContext() );
        preferencesEditor = preferences.edit();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Intent i = getIntent();
        Token = i.getStringExtra("Token");
        ClientId = i.getStringExtra("ClientId");
        ServiceId = i.getStringExtra("ServiceId");
        LoginParams = i.getStringExtra("LoginParams");
        SessionCookies = i.getStringExtra("SessionCookies");

        name = (TextView) findViewById( R.id.humanName );
        institution = (TextView) findViewById( R.id.school );
        cardStatus = (TextView) findViewById( R.id.cardStatus );
        balance = (TextView) findViewById( R.id.balance );

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
                String.format(
                        getString(R.string.balanceFormat), preferences.getInt( "latestBalance", 0 )
                )
        );

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
            default:
                break;
        }
        return true;
    }

    void endSession()
    {
        preferencesEditor.putBoolean( "AutoLog", false);
        preferencesEditor.putString( "token", "");
        preferencesEditor.commit();
        if ( preferences.getBoolean( "Remember", false ) )
        {
            preferencesEditor.putString( "humanName", "");
            preferencesEditor.putString( "cardStatus", "");
            preferencesEditor.putString( "institution", "");
            preferencesEditor.commit();
        }
        finish();
    }

    void refreshBalanceData()
    {
        new comeBotas().execute();
    }

    @Override
    public void onBackPressed() {

    }

    class comeBotas extends AsyncTask<URL, Integer, String>
    {
        int _balance = 0;
        @Override
        protected String doInBackground( URL... urls )
        {
            PARSEC parser = new PARSEC();
            String response = parser.GET( "/wp-content/plugins/beneficiarios/api.php?action=balance&token=" + Token + "&from=19/12/2016&to=19/01/2017&clientid=1&serviceid=15" );

            try {
                //TODO: check prblems here
                _balance = (new JSONObject(response)).getJSONObject("result").getJSONObject("return").getInt("amountBalance");
            } catch ( Exception e) {
                Log.e("ERROR", e.getLocalizedMessage() , e.fillInStackTrace() );
            }
            return "";
        }

        @Override
        protected void onPostExecute( String texto )
        {
            preferencesEditor.putInt( "latestBalance", _balance );
            balance.setText(
                    String.format(
                            getString( R.string.balanceFormat ),
                            _balance
                    )
            );
        }
    }
}
