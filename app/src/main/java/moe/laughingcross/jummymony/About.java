package moe.laughingcross.jummymony;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class About extends AppCompatActivity {

    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;

    Integer count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        preferences = PreferenceManager.getDefaultSharedPreferences( getBaseContext() );
        preferencesEditor = preferences.edit();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(myToolbar);
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
                testMode();
                break;
            case R.id.action_about:
                //changeToAbout();
            default:
                break;
        }
        return true;
    }

    void endSession()
    {
        preferencesEditor.putBoolean( "AutoLog", false);
        preferencesEditor.putString( "token", "");
        preferencesEditor.putString( "humanName", "");
        preferencesEditor.putString( "cardStatus", "");
        preferencesEditor.putString( "institution", "");
        preferencesEditor.commit();
        setResult(2);
        finish();
    }

    void testMode()
    {
        count++;
        if ( count >= 62 )
        {
            preferencesEditor.putBoolean( "screenTester", true );
            preferencesEditor.commit();
            count = 0;
            Log.i( "JunaCoffee", "Screentester mode" );
        }
    }
}
