/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
 *  109/07/22  V1.00.01    Zuwei     coding standard, rename field method & format                   *
******************************************************************************/
package dbTools;

import java.util.Base64;

public class Utils {

    public Utils() {
        // TODO Auto-generated constructor stub
    }

    public static String encodedString(String sPSrc) {

        String sLResult = "";
        try {

            final Base64.Encoder encoder = Base64.getEncoder();

            final byte[] textByte = sPSrc.getBytes("UTF-8");
            // 編碼
            sLResult = encoder.encodeToString(textByte);

        } catch (Exception e) {
            // TODO: handle exception
        }

        return sLResult;

    }

    public static String decodedString(String sPEncodedStr) {

        String sLResult = "";
        try {

            final Base64.Decoder decoder = Base64.getDecoder();


            // 解碼
            sLResult = new String(decoder.decode(sPEncodedStr), "UTF-8");


        } catch (Exception e) {
            // TODO: handle exception
        }

        return sLResult;

    }

}
