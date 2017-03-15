package moe.laughingcross.jummymony;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PARSEC {
    private URL direccionBase;
    private HttpURLConnection urlConnection;
    private InputStream entrada;
    private OutputStream salida;
    protected String cookies = "";

    void conectar( final String url ) throws IOException
    {
        String _url = "http://www.becajunaebsodexo.cl";
        if(url.charAt(0)=='/')
            _url += url;

        direccionBase = new URL( _url );
        urlConnection = ( HttpURLConnection ) direccionBase.openConnection();

        if ( !cookies.isEmpty() )
            urlConnection.setRequestProperty( "Cookie", cookies );
    }

    private String readStream( final InputStream is ) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    public String GET()
    {
        return GET( "/" );
    }

    public String GET( String url )
    {
        String datos;
        Boolean e_connectionFailed = false;
        try {
            conectar( url );
            urlConnection.setDoOutput( true );
            urlConnection.setRequestMethod( "GET" );
            entrada = new BufferedInputStream( urlConnection.getInputStream() );
        } catch (Exception e) {
            Log.e( "PARSEC.GET", "No se puede realizar la conexión::"+e.getLocalizedMessage() );
            e_connectionFailed = true;
        }
        if ( !e_connectionFailed ) {
            cookies += urlConnection.getHeaderField( "Set-Cookie" ) + ";";
            datos = readStream(entrada);
            urlConnection.disconnect();
        }
        else
            datos = "ERROR|connectionFailed";
        return datos;
    }

    private void writeStream(OutputStream salida, String datos)
     {
        try {
            BufferedWriter escritor = new BufferedWriter(
                    new OutputStreamWriter(salida, "UTF-8")
            );
            escritor.write(datos);
            escritor.flush();
            escritor.close();
            salida.close();
        } catch (Exception e) {
            Log.e( "writeStream", "Error al escribir::"+e.getLocalizedMessage() );
        }
    }

    public String POST( String url, String datosAEnviar)
    {
        String Salida = "";
        try {
            Log.i("PARSEC.POST","Conectando...");
            conectar( url );
            Log.i("PARSEC.POST","Preparando Solicitud...");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(20000);
            urlConnection.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36" );
            urlConnection.setRequestProperty( "Host","www.becajunaebsodexo.cl" );
            urlConnection.setRequestProperty( "Connection", "keep-alive" );
            urlConnection.setChunkedStreamingMode(1);

        } catch (Exception e) {
            Log.e("PARSEC.POST","No se puede realizar la conexión::"+e.getLocalizedMessage());
        }
        try {
            Log.i("PARSEC.POST","Enviando Solicitud...");
            salida = urlConnection.getOutputStream();
            writeStream(salida,datosAEnviar);

        } catch (IOException e) {
            Log.e("PARSEC.POST","No se puede realizar la conexión::"+e.getLocalizedMessage());
        }
        try {
            Log.i("PARSEC.POST","Obteniendo respuesta...");
            urlConnection.connect();
            entrada = new BufferedInputStream(urlConnection.getInputStream());
            Salida = readStream(entrada);

            Log.i("PARSEC.POST","Cerrando conexion...");
            urlConnection.disconnect();

        } catch (Exception e) {
            Log.e("PARSEC.POST","No se puede realizar la conexión::"+e.getLocalizedMessage(),e);
        }
        if ( Salida.contentEquals( "<META HTTP-EQUIV=\"Refresh\" Content=\"0;\">" ) )
        {
            Salida = GET( url );
        }

        Log.i("PARSEC.POST","Retornando...");
        return Salida;
    }

}
