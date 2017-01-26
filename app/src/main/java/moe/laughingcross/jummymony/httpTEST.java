package moe.laughingcross.jummymony;

import android.Manifest;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

public class httpTEST extends AppCompatActivity {

    PARSEC parser = new PARSEC();
    public static String texto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_test);
    }
    public  void obtenerNonce(View view) throws NullPointerException, IOException
    {
        new obtenerPagina().execute();
    }

    public class obtenerPagina extends AsyncTask<URL,Integer,String> {
        @Override
        protected String doInBackground(URL... params) {
            String textoRetornado;
            try {
                Log.e("doInBackground","Tratando de hacer GET");
                textoRetornado = parser.GET().replaceFirst("([\\s\\S]*nonce = \")","").replaceFirst("(\"[\\s\\S]*)","");
                Log.e("doInBackground","parser.GET()->textoRetrnado::"+textoRetornado);
            } catch (Exception e) {
                Log.e("GET: ", "IOEXEPTION - PARSEC.java::doInBackgrund(URL... params)::GET()::"+e.getLocalizedMessage());
                textoRetornado="error!!!1!";
            }
            return textoRetornado;
        }

        @Override
        protected void onPostExecute(String resultado)
        {
            httpTEST.texto = resultado;
            TextView cuadro = (TextView) findViewById(R.id.textView);
            cuadro.setText(texto);
        }
    }
    public class obtenerX extends AsyncTask<URL,Integer,String>
    {
        @Override
        protected String doInBackground(URL... url)
        {
            return "";
        }
    }
}
