package moe.laughingcross.jummymony;

/**
 * Created by DOSmasSOD on 27/07/2016.
 */
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.*;
import org.apache.commons.codec.digest.DigestUtils;

public class LaCrypto {
/*
private static String encode64(String llave)
{
    Base64 encodificador = new Base64(false);
    Log.e("encode64", "datos|byte[]::llave.getBytes(utf-8)");
    String datas = "";
    try {
        byte[] datos = llave.getBytes("UTF-8");
        Log.e("encode64","Base64.encodeBase64String(datos)");
        datas = org.apache.commons.codec.binary.StringUtils.newStringUtf8(encodificador.encodeBase64(datos,false,false,Integer.MAX_VALUE));
    } catch (Exception e) {
        Log.e("encode64","No se pudo decodificar llaves::"+e.getLocalizedMessage());
    }
    return datas;
}
*/

    private static String sha1(String llave)
    {
        String vuelto = "";
        try {
            //byte[] billete = llave.getBytes("UTF-8");
            //billete = DigestUtils.getSha1Digest().digest(billete);
            //char[] billetes = Hex.encodeHex(billete);
            vuelto = new String(Hex.encodeHex(DigestUtils.getSha1Digest().digest(llave.getBytes("UTF-8"))));
        } catch (Exception e) {
            Log.e ("sha1(llave)","No se pudo hacer Sha1::"+e.getLocalizedMessage());
        }
        Log.e("sha1","return "+vuelto);
        return vuelto;
    }

    public static String codificarContraseña(String contraseña)
    {
        Log.e("LaCrypto","return encode64(sha1(contraseña))::<<|"+contraseña);
        //try {
        String test = sha1(contraseña);
            contraseña = base64.encode(test);
        //} catch (Exception e) {
        //    Log.e("codificarContraseña", "Error al codificar::"+e.getLocalizedMessage());
        //}
        return contraseña;
    }
}
