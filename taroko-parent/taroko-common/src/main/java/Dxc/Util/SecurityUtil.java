/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-08-14  V1.00.00  Zuwei      common method for verify sql、path、輸出瀏覽器咨詢      *
*                                                                            *  
******************************************************************************/
package Dxc.Util;

import java.io.File;
import java.text.Normalizer;

public class SecurityUtil {
    public static String verifyPath(String path) {
        String tempStr = Normalizer.normalize(path, Normalizer.Form.NFD);
        while (tempStr.indexOf("..\\") >= 0 || tempStr.indexOf("../") >= 0) {
//            tempStr = tempStr.replaceAll("\\.\\./", "./");
//            tempStr = tempStr.replaceAll("\\.\\.\\\\", ".\\\\");
        	tempStr = tempStr.replace("..\\", ".\\");
        	tempStr = tempStr.replace("../", "./");
        }
        char[] originalChars = tempStr.toCharArray();
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < originalChars.length; i++) {
        	char c = originalChars[i];
            if (c == '\\' || c == '/') {
                c = File.separatorChar;
            }
            sb.append(c);
        }
        tempStr = sb.toString();
        return tempStr;
    }
    
//    public static String verifySql(String sql) {
//        String tempStr = Normalizer.normalize(sql, Normalizer.Form.NFD);
//        tempStr.replaceAll("--", "");
//        char[] originalChars = tempStr.toCharArray();
//        StringBuilder sb = new StringBuilder(32);
//        for (int i = 0; i < originalChars.length; i++) {
//        	char c = originalChars[i];
//            if (c == ';') {
//                c = ' ';
//            }
//            if (c == '\n') {
//                c = ' ';
//            }
//            sb.append(c);
//        }
//        tempStr = sb.toString();
//        return tempStr;
//    }
    
//    public static String verifyOutdata(String outdata) {
//        String tempStr = Normalizer.normalize(outdata, Normalizer.Form.NFD);
//        return tempStr;
//    }

}
