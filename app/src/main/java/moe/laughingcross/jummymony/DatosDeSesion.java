package moe.laughingcross.jummymony;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.AdTargetingOptions;

import java.net.URL;

public class DatosDeSesion extends AppCompatActivity {

    EditText campoRut;
    EditText campoPin;

    String Token = "";
    String ClientId;
    String ServiceId;
    String LoginParams;
    String SessionCookies;

    String humanName;
    String institution;
    String cardStatus;


    String _Rut;
    String _Pin;

    Boolean validToken = false;
    Boolean connected = false;

    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;

    AdLayout adView;

    private TextWatcher tWatch = new TextWatcher()
    {
        @Override
        public void beforeTextChanged( CharSequence cs, int i1, int i2, int i3 )
        {}

        @Override
        public void onTextChanged( CharSequence cs, int i1, int i2, int i3 )
        {}

        @Override
        public void afterTextChanged( Editable text ){
            sufficentLogin();
        }
    };

    public void sufficentLogin() {
        try {
            if (campoRut.length() > 8 && campoPin.length() >= 4)
                findViewById(R.id.botonIniciarSesion).setEnabled(true);
            else
                findViewById(R.id.botonIniciarSesion).setEnabled(false);
        } catch ( NullPointerException e ) {
            Log.e("JunaCoffeeLogin", "Unexpected NullPointer Exception on field check", e.getCause());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences( getBaseContext() );
        preferencesEditor = preferences.edit();

        //TODO: Check connection
        connected = true;

        //TODO: Check token
        validToken = true;
        try {
            if (preferences.getBoolean("AutoLog", false) && connected && (validToken || preferences.getString("Pin", "").length() > 0)) {
                Token = preferences.getString("Token", "");

                //TODO: Dynamic Asignation of CID, SID, Cookies, etc...
                ClientId = "" + 1;
                ServiceId = "" + 15;
                LoginParams = "";
                SessionCookies = "";

                Empezar();
            }
        } catch ( ClassCastException e )
        {
            //TODO: Show error to user
            Log.e( "ERROR/Settings", "Settings fuckedUp, flyaway", e.getCause() );
        } catch ( Exception e )
        {
            Log.e( "ERROR", e.getLocalizedMessage(), e.getCause() );
        }
            setContentView(R.layout.activity_datos_de_sesion);
            campoRut = (EditText) findViewById(R.id.rut);
            campoPin = (EditText) findViewById(R.id.Pin);

            campoRut.addTextChangedListener(tWatch);
            campoPin.addTextChangedListener(tWatch);

            campoRut.setText( preferences.getString("Rut","") );
            campoPin.setText( preferences.getString("Pin","") );
/////////////////////////////////
        AdRegistration.setAppKey("602b74a12ba04ab4a4ca98f4703267cc");
        AdRegistration.enableTesting(true);
        AdRegistration.enableLogging(true);

        // Programmatically create the AmazonAdLayout
        adView = new AdLayout(this);
        adView.setListener(new MyAdListener());
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.loginLayout);

        // Set the correct width and height of the ad
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.BELOW, R.id.mensajeError);
        lp.setMargins(
                0, //Left
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 20F, getResources().getDisplayMetrics()
                ), //Top
                0, //Right
                0  //Bottom
        );
        layout.addView(adView,lp);

        AdTargetingOptions adOptions = new AdTargetingOptions().enableGeoLocation(true);

        // Optional: Set ad targeting options here.
        adView.loadAd(adOptions); // Retrieves an ad on background thread
/////////////////////////////////
    }


    private void Recordar()
    {
        preferencesEditor.putString("token", Token);
        if ( ( (CheckBox) findViewById( R.id.cb_rememberMe ) ).isChecked() )
        {
            preferencesEditor.putBoolean("Remember", true);
            preferencesEditor.putString("Rut", _Rut);
            preferencesEditor.putString("Pin", _Pin);
            preferencesEditor.commit();
        } else {
            preferencesEditor.putString("Rut", "");
            preferencesEditor.putString("Pin", "");
            campoRut.setText( "" );
            campoPin.setText( "" );

        }
        if ( ( (CheckBox) findViewById( R.id.cb_logMe ) ).isChecked() )
        {
            preferencesEditor.putBoolean("AutoLog", true);
            preferencesEditor.commit();
        }
    }

    public void Empezar()
    {
        Intent i = new Intent(getApplicationContext(), MainPage.class);
        i.putExtra( "Token", Token );
        i.putExtra( "ClientId", ClientId );
        i.putExtra( "ServiceId", ServiceId );
        i.putExtra( "LoginParams", LoginParams );
        i.putExtra( "SesionCookies", SessionCookies );

        startActivityForResult(i, 0);
    }

    public void Autenticar(View view)
    {
        try {
            lock();
            _Rut = campoRut.getText().toString();
            _Pin = campoPin.getText().toString();
            preferencesEditor.putBoolean("Remember", true);
            preferencesEditor.putString("Rut", _Rut);
            preferencesEditor.putString("Pin", _Pin);
            preferencesEditor.commit();
            new autenticacion().execute();
        } catch ( Exception e )
        {
            Log.e( "ERROR", e.getLocalizedMessage(), e.getCause() );
        }
    }
    public class autenticacion extends AsyncTask<URL,Integer,String>
    {
        int Error = 0;
        String ErrorDescription;
        autenticacion()
        {
            try {
                (findViewById(R.id.mensajeError)).setVisibility(View.GONE);
            } catch ( NullPointerException e )
            {
                Log.e("Error", "NullPointerException at auth constructor");
            }
        }
        @Override
        protected String doInBackground(URL... url)
        {
            PARSEC parser = new PARSEC();
            Autenticar autenticador;
            autenticador = new Autenticar(
                    _Rut,
                    _Pin,
                    parser
            );
            String textoRetornado = autenticador.Mensaje;
            Token = textoRetornado;
            for( int i = 0; Token.equals( "" ) && i<10 ; i++ )
            {
                Token = autenticador.getToken( parser );
            }
            try {
                humanName = autenticador.data.getString( "beneficiaryName" );
                institution = autenticador.data.getString( "iesName" );
                cardStatus = autenticador.loginResult.getJSONObject( "return" ).getJSONObject( "card" ).getString( "cardStatusDescription" );
            } catch ( Exception e)
            {
                Log.e( "ERROR",e.getLocalizedMessage(),e.fillInStackTrace() );
            }
            LoginParams = autenticador.LoginParams;
            SessionCookies = parser.cookies;
            Error = autenticador.ErrorCode;
            ErrorDescription = autenticador.ErrorDescription;
            /*if ( autenticador.ErrorCode > 0 )
            {
                textoRetornado = "" + autenticador.ErrorCode + ": " + autenticador.ErrorDescription;
            }*/
//            return textoRetornado;
            return autenticador.ErrorDescription;
        }
        @Override
        protected void onPostExecute(String texto) {
            //TODO: texto = ACTION|MESSAGE
            unlock();
            if ( Error == 0 )
            {
                Recordar();

                preferencesEditor.putString("humanName",humanName);
                preferencesEditor.putString("institution", institution);
                preferencesEditor.putString("cardStatus", cardStatus);
                preferencesEditor.commit();
                preferencesEditor.apply();

                Empezar();
            }
            else try {
                TextView _tView = (TextView) findViewById(R.id.mensajeError);
                _tView.setText(texto);
                _tView.setVisibility( View.VISIBLE );
            } catch ( java.lang.NullPointerException npe )
            {
                Log.e( "ERROR(SystemCompat)", npe.getLocalizedMessage(), npe.fillInStackTrace() );
            }
        }
    }
    public void lock()
    {
        campoRut.setEnabled( false );
        campoPin.setEnabled( false );
        ( findViewById( R.id.botonIniciarSesion ) ).setEnabled( false );
        ( findViewById( R.id.cb_rememberMe ) ).setEnabled( false );
        ( findViewById( R.id.cb_logMe ) ).setEnabled( false );
        ( findViewById( R.id.loadingObject ) ).setVisibility( View.VISIBLE );
    }
    public void unlock()
    {
        campoRut.setEnabled( true );
        campoPin.setEnabled( true );
        ( findViewById( R.id.botonIniciarSesion ) ).setEnabled( true );
        ( findViewById( R.id.cb_rememberMe ) ).setEnabled( true );
        ( findViewById( R.id.cb_logMe ) ).setEnabled( true );
        ( findViewById( R.id.loadingObject ) ).setVisibility( View.GONE );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == 2 ){
        }
    }
}
