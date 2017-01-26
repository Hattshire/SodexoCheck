package moe.laughingcross.jummymony;

/**
 * Created by DOSmasSOD on 29/07/2016.
 */
public class base64 {
    protected static String keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    protected static String encode(String datos){
        String output = "";
        char chr1, chr2, chr3;
        int enc1, enc2, enc3, enc4;
        int i = 0;

        do {
            chr1 = datos.charAt(i);
            i++;
            chr2 = datos.length() > i ? datos.charAt(i) : 0;
            i++;
            chr3 = datos.length() > i ? datos.charAt(i) : 0;
            i++;

            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;

            if (chr2 == 0) {
                enc3 = enc4 = 64;
            } else if (chr3 == 0) {
                enc4 = 64;
            }

            output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2) +
                    keyStr.charAt(enc3) + keyStr.charAt(enc4);
        } while (i < datos.length());
        return output;
    }

    protected static String decode(String datos) {
        String output = "";
        char chr1, chr2, chr3;
        int enc1, enc2, enc3, enc4;
        int i = 0;

        // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
        datos = datos.replaceAll("[^A-Za-z0-9+/=]", "");

        do {
            enc1 = keyStr.indexOf(datos.charAt(i++));
            enc2 = keyStr.indexOf(datos.charAt(i++));
            enc3 = keyStr.indexOf(datos.charAt(i++));
            enc4 = keyStr.indexOf(datos.charAt(i++));

            chr1 = (char)((enc1 << 2) | (enc2 >> 4));
            chr2 = (char)(((enc2 & 15) << 4) | (enc3 >> 2));
            chr3 = (char)(((enc3 & 3) << 6) | enc4);

            output = output + chr1;

            if (enc3 != 64) {
                output = output + chr2;
            }
            if (enc4 != 64) {
                output = output + chr3;
            }
        } while (i < datos.length());

        return output;
    }
}
