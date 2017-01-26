package moe.laughingcross.jummymony;

/**
 * Created by DOSmasSOD on 27/07/2016.
 */
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.lang.Math;
import java.net.URLEncoder;

class Autenticar {
    protected String Rut;
    protected String Contraseña;
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
            Log.e("AAAAAAAAAAAAAAAAAAAA","");
        }

        return result;
    }

    private void codificar()  {
        //TODO: SALSA 4DWin
        Log.v("Autenticar","codificar::Contraseña.set()");
        Contraseña = "" + Now + Pin;
        try {
            Log.v("Autenticar","Contraseña=LaCrypto.codificarContraseña(Contraseña)::"+Contraseña);
            Contraseña = LaCrypto.codificarContraseña(Contraseña);
        } catch (Exception e) {
            Log.v("codificar()","LaCrypto.codificarContraseña(Contraseña)::"+e.getLocalizedMessage());
        }
        Info = encodeURIComponent( "" + Now );
    }

    private void error(int fallo, TextView contenedorMensaje)
    {
        switch (fallo) {
            case 626:
                contenedorMensaje.setText(R.string.error_invalid_rut);
                break;
            default:
                contenedorMensaje.setText(R.string.error_unknown);
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
        Log.i("Autenticar","codificar");
        codificar();
        try {
            Log.i("Autenticar","parser.POST");
            LoginParams =
                    "rut="+Rut+
                    "&password="+Contraseña+
                    "&passwordEnc="+Contraseña+
                    "&info="+Info;
            Mensaje = parser.GET( APIUrl + "?action=login&" + LoginParams );
            JSONObject loginResponse = new JSONObject( Mensaje );
            if (loginResponse.getString( "status" ).equals("OK"))
            {
                loginResult = loginResponse.getJSONObject("result");
                ErrorCode = loginResult.getInt( "O_ERROR_CODE" );
                ErrorDescription = loginResult.getString( "O_ERROR_DESCRIPTION" );
                if ( ErrorCode == 0 )
                {
                    data = loginResult.getJSONObject("return").getJSONObject("beneficiary");
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
        } catch (Exception e) {
            Log.e("Autenticar", "Error al enviar los datos al servidor::",e);
        }
    }
}
