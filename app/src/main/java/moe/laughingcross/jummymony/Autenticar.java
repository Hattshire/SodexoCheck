package moe.laughingcross.jummymony;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.lang.Math;
import java.net.URLEncoder;

class Autenticar {
    protected String Rut;
    protected String Password;
    protected String Info;
    protected String Mensaje;
    protected String LoginParams;
    protected int ErrorCode;
    protected String ErrorDescription;

    final private String APIUrl = "/wp-content/plugins/beneficiarios/api.php";

    private long Now;
    private String Pin;
    private char digitoVerificador;
    protected JSONObject data;
    protected JSONObject loginResult;

    private boolean verificarRut(String rut)
    {
        //S=digito verificador ; M=Contador
        int S=1;
        int M=0;
        //char[] rutProcesado = new char[rut.length()];
        digitoVerificador = rut.charAt(rut.length()-1);
        rut = rut.substring(0,rut.length()-1);
        Rut = rut;
        int rutProcesado = 0;
        for (int i=0;i<rut.length();i++)
        {
            rutProcesado*=10;
            rutProcesado+=(int) rut.charAt(i);
        }
//        for(int i=0;i<rut.length();i++)
//        {
//            rutProcesado[i] = (char) ( ( ( (int) rut.charAt(-i) - 48) * ( ( (i+5) % 5) + 2) ) + 48);
//
//        }

        for (; rutProcesado!=0; rutProcesado = (int) Math.floor(rutProcesado / 10))
            S = ( S + rutProcesado % 10 * ( 9 - M++ % 6 ) ) % 11;
        if ( (digitoVerificador=='k' && S==10) || (digitoVerificador=='0' && S==11) || digitoVerificador == (char) S+48 )
        {
            Rut=Rut+digitoVerificador;
            return true;
        }
        return false;
    }

    private static String encodeURIComponent(String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
            Log.e("ERROR",e.getLocalizedMessage(), e.getCause() );
        }

        return result;
    }

    private void encode()  {
        //TODO: SALSA 4DWin
        Log.v("Autenticar","codificar::Password.set()");
        Password = "" + Now + Pin;
        try {
            Log.v( "Autenticar","Password = LaCrypto.encodePassword(Password)::" + Password );
            Password = LaCrypto.encodePassword(Password);
        } catch (Exception e) {
            Log.e( "Error","encode(): " + e.getLocalizedMessage(), e.getCause() );
        }
        Info = encodeURIComponent( "" + Now );
    }

    private void error(int fallo, TextView contenedorMensaje)
    {
        switch (fallo) {
            case 626:
                contenedorMensaje.setText( "Rut Invalido" );
                break;
            default:
                contenedorMensaje.setText( "Error desconocido" );
                break;
        }
    }

    public String getToken( PARSEC parser )
    {
        String _tken = parser.POST( "/beneficiarios/", LoginParams + "&password=&ingresar=Ingresar" );
        String tken = "";
        if ( _tken.indexOf("var token") > 0 )
            tken = _tken.replaceFirst("([\\s\\S]*var token=')", "").replaceFirst("('[\\s\\S]*)", "");
        else
        {
            ErrorCode = 3584;
            ErrorDescription = "Problemas de conexión.";
        }
        return tken;
    }

    Autenticar(String rut, String pin, PARSEC parser)
    {
        Pin = pin;
        Rut = rut;//if (verificarRut(rut)) {//
        Now = new Date().getTime();
        Now = Now/1000;
        Now = Math.round( Now )*1000;
        Log.v("Autenticar","encode()");
        encode();
        try {
            Log.v( "Autenticar", "parser.POST" );
            LoginParams =
                    "rut="+Rut+
                    "&password="+Password+
                    "&passwordEnc="+Password+
                    "&info="+Info;
            Mensaje = parser.GET( APIUrl + "?action=login&" + LoginParams );
            JSONObject loginResponse = new JSONObject( Mensaje );
            if (loginResponse.getString( "status" ).equals( "OK" ))
            {
                loginResult = loginResponse.getJSONObject( "result" );
                ErrorCode = loginResult.getInt( "O_ERROR_CODE" );
                ErrorDescription = loginResult.getString( "O_ERROR_DESCRIPTION" );
                if ( ErrorCode == 0 )
                {
                    data = loginResult.getJSONObject( "return" ).getJSONObject( "beneficiary" );
                    Mensaje = getToken( parser );
                }
            }
/*            if ( Mensaje.lastIndexOf( "\n" +
                    "\t  Clave inválida" ) > 0 )
            {
                ErrorCode = 3007301;
            }
            if ( Mensaje.lastIndexOf( "Usuario no válido" ) > 0 )
            {
                ErrorCode = 3007302;
            }
            if ( Mensaje.lastIndexOf( "(340)Problemas validando Rut" ) > 0 )
            {
                //TODO: Validar rut de forma local
                ErrorCode = 3007340;
            }*/
        } catch ( Exception e ) {
            Log.e( "Autenticar", e.getLocalizedMessage(), e.getCause() );
        }
    }
}
