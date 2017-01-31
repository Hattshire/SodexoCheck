package moe.laughingcross.jummymony;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class latestTransactions extends AppCompatActivity {

    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;

    LinearLayout baseLayout;
    String token;

    String rangeBegin = "", rangeEnd = "";

    final String dateFormatString = "dd/MM/yyyy";
    final String parserDateFormatString = "yy/MM/dd hh:mm:ss";

    DateFormat dateFormat = new SimpleDateFormat( dateFormatString, Locale.getDefault() );
    DateFormat parserDateFormat = new SimpleDateFormat( parserDateFormatString, Locale.getDefault() );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest_transactions);

        Intent i = getIntent();

        preferences = PreferenceManager.getDefaultSharedPreferences( getBaseContext() );
        preferencesEditor = preferences.edit();

        Toolbar myToolbar = ( Toolbar ) findViewById( R.id.transactions_toolbar );
        setSupportActionBar( myToolbar );

        token = i.getStringExtra("Token");

        baseLayout = (LinearLayout) findViewById(R.id.TransactionList);

        int month = 5;

        getTransactions(
                dateFormat.format( System.currentTimeMillis() - 2629746000L*month ),
                dateFormat.format( System.currentTimeMillis() )
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch ( item.getItemId() )
        {
            case R.id.action_end:
                endSession();
                break;
            case R.id.action_refresh:
                //refreshBalanceData();
                break;
            case R.id.action_about:
                changeToAbout();
            default:
                break;
        }
        return true;
    }

    public void changeToAbout()
    {
        Intent i = new Intent( this, About.class);
        startActivityForResult( i, 0 );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==2){
            setResult(2);
            finish();
        }
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

    RelativeLayout formatLayout( String amount, String location, String date, Integer color )
    {
        //Set a custom date output
        DateFormat finalDateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());

        //Set a proper date for layout id
        DateFormat idDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        Integer usedIdDateFormat;
        try {
            usedIdDateFormat = Integer.valueOf(idDateFormat.format(dateFormat.parse(date))) * 10;
        } catch ( ParseException e )
        {
            Log.w( "Warning", "cant set id format" );
            usedIdDateFormat = Integer.valueOf( idDateFormat.format( System.currentTimeMillis() ) );
        }
        //Declare new tile
        RelativeLayout Clayout = new RelativeLayout(this);
        RelativeLayout layout = new RelativeLayout(this);

        //Create new textViews
        TextView dateView = new TextView(this);
        TextView amountView = new TextView(this);
        TextView locationView = new TextView(this);

        try {

            //Set textViews contents
            try {
                dateView.setText( capitalize( finalDateFormat.format( parserDateFormat.parse( date ) ) ) );
            } catch (ParseException e) {
                dateView.setText(date);
            }
            amountView.setText( String.format(
                    getResources().getString( R.string.balanceFormat ),
                    Integer.valueOf( amount )
            ) );
            locationView.setText(location);

            //Try to set layout id
            try {
                layout.setId( usedIdDateFormat );
            } catch (Exception e) {
                Log.w("Warning", "Transaction tile without Id");

                //Append TextViews to layout
                layout.addView(dateView);
                layout.addView(amountView);
                layout.addView(locationView);

                //Set layout background color
                layout.setBackgroundColor( getResources().getColor( R.color.error_tile ) );
                return layout;
            }

            //Set textViews color
            amountView.setTextColor(getResources().getColor(R.color.colorPrimary));
            dateView.setTextColor(getResources().getColor(R.color.colorPrimary));
            locationView.setTextColor(getResources().getColor(R.color.colorPrimary));

            //Set TextViews positions
            try
            {
                //DateView
                dateView.setId( usedIdDateFormat + 1 );
                RelativeLayout.LayoutParams dateViewLayoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                dateViewLayoutParams.addRule( RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE );
                dateViewLayoutParams.setMargins(
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 10F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 10F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 10F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 10F, getResources().getDisplayMetrics() )
                );
                dateView.setLayoutParams( dateViewLayoutParams );
                dateView.setTextSize( TypedValue.COMPLEX_UNIT_SP, 20f );

                //AmountView
                amountView.setId( usedIdDateFormat + 2 );
                RelativeLayout.LayoutParams amountViewLayoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                amountViewLayoutParams.addRule( RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE );
                amountViewLayoutParams.setMargins(
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 20F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 20F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 20F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 20F, getResources().getDisplayMetrics() )
                );
                amountView.setLayoutParams( amountViewLayoutParams );
                amountView.setTextSize( TypedValue.COMPLEX_UNIT_SP, 50f );

                //LocationView
                locationView.setId( usedIdDateFormat + 3 );
                RelativeLayout.LayoutParams locationViewLayoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                locationViewLayoutParams.addRule( RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE );
                locationViewLayoutParams.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE );
                locationViewLayoutParams.setMargins(
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 10F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 10F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 10F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 10F, getResources().getDisplayMetrics() )
                );
                locationView.setLayoutParams( locationViewLayoutParams );
                locationView.setTextSize( TypedValue.COMPLEX_UNIT_SP, 17f );

                //Append TextViews to layout
                layout.addView(dateView);
                layout.addView(amountView);
                layout.addView(locationView);
            }
            catch( Exception e )
            {
                Log.w( "Warning", e.getLocalizedMessage(), e.getCause() );
            }

            //Set layout height to 180dp
            RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT );
            RelativeLayout.LayoutParams CParams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT );
            try {
                Log.i( "INFO", "Size: " + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180F, getResources().getDisplayMetrics()) );
                int heightPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180F, getResources().getDisplayMetrics());
                lParams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, heightPixels );
                CParams.addRule( RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE );
                lParams.setMargins(
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 0F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 9F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 0F, getResources().getDisplayMetrics() ),
                        (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 9F, getResources().getDisplayMetrics() )
                );
             } catch ( Exception e )
            {
                Log.w( "Warning", "Can't set Layout height", e.getCause() );
            }
            layout.setLayoutParams(lParams);
            Clayout.setLayoutParams(CParams);
            //Set layout background color
            layout.setBackgroundColor( getResources().getColor( color ) );

        } catch ( Exception e )
        {
            Log.w( "Warning", "Can't create tile: " + e.getLocalizedMessage(), e.getCause() );
        }
        Clayout.addView( layout );
        return Clayout;
    }

    void getTransactions(String from, String to) {
        (new GetTransactionsInBackground()).execute(from, to);
    }

    void getTransactions( Long from, Long to)
    {
        getTransactions( dateFormat.format( from ), dateFormat.format( to ) );
    }

    String capitalize( final String string )
    {
        if ( string == null || string.length() < 2 )
        {
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    class GetTransactionsInBackground extends AsyncTask<String, Integer, Boolean> {

         JSONArray transactionList;
         String _from, _to;

         @Override
         protected Boolean doInBackground(String... params) {

            String _from = params[0];
            String _to = params[1];

            PARSEC parser = new PARSEC();
            // TODO: Make a parser for the API
            String response = parser.GET("/wp-content/plugins/beneficiarios/api.php?action=balance&token=" + token + "&from=" + _from + "&to=" + _to + "&clientid=1&serviceid=15");

            try {
                //TODO: check problems here
                JSONObject transactions = new JSONObject(response);
                if (transactions.getJSONObject("result")
                        .getInt("O_ERROR_CODE") == 0
                        ) {
                    transactions = transactions.getJSONObject("result").getJSONObject("return");
                    try {
                        transactionList = transactions.getJSONArray("recordTx");
                    } catch ( JSONException e )
                    {
                        if ( e.getMessage().equals( "No value for recordTx" ) )
                            transactionList = new JSONArray();
                        else {
                            Log.w("Warning", e.getLocalizedMessage(), e.getCause());
                            transactionList = new JSONArray( transactions.getJSONObject("recordTx")
                                    .toString().
                                            replace( "}", "}]" ).
                                            replace( "{", "[{" )
                            );
                        }
                    }
                } else {
                    Log.w("Warning", "Can't retrieve transactions");
                    cancel(true);
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getLocalizedMessage(), e.getCause());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute( final Boolean status ) {

            //There was an error, can't continue
            if ( !status )
                return;

            String txType, location = "", date, amount;
            Integer color;

            for (Integer i = 0; i < transactionList.length(); i++) {
                JSONObject _tempOBJ;
                color = R.color.consume_tile;
                try {
                    _tempOBJ = transactionList.getJSONObject( i );
                    txType = _tempOBJ.getString("typeTransaction");
                    switch (txType) {
                        case "Consumo":
                            location = _tempOBJ.getString("consumePointName");
                        case "Liberación de saldo":
                            amount = _tempOBJ.getString("amountTx");
                            date = _tempOBJ.getString("dateTx");
                            switch ( _tempOBJ.getString("declined") )
                            {
                                case "false":
                                    if ( txType.equals("Liberación de saldo" )) {
                                        color = R.color.deposit_tile;
                                        location = txType;
                                    }
                                    break;
                                default:
                                    color = R.color.error_tile;
                                    break;
                            }
                            break;
                        default:
                            Log.w("Warning", "Corrupted transaction information, output truncated");
                            throw new org.json.JSONException( "Bad Data" );
                    }
                    baseLayout.addView( formatLayout( amount, location, date, color ) );
                } catch (JSONException e) {
                    Log.e("ERROR", e.getLocalizedMessage(), e.getCause());
                    return;
                }
            }
            setDateInRange( _from, _to ,rangeBegin, rangeEnd );
        }

         void setDateInRange( String from, String to, String gFrom, String gTo )
         {
             gFrom = from;
             gTo = to;
         }
    }
}
