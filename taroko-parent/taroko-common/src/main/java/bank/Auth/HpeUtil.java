/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-07-22  V1.00.01  Zuwei       updated for project coding standard      *
* 109-08-03  V1.00.01  Zuwei       fix code scan issue                       *
* 109-09-03  V1.00.02  yanghan      新增安全關閉輸入流的方法                                                                             *  
* 109-09-04  V1.00.03  Zuwei      fix code scan issue                                                                            *  
*  109-09-28  V1.00.01  Zuwei       fix code scan issue      *
*  110-01-07  V1.00.02    shiyuqi       修改无意义命名                        
* 111-01-19  V1.00.07  Justin       fix J2EE Bad Practices: Leftover Debug Code
******************************************************************************/
package bank.Auth;
 
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
// import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
// import javax.xml.datatype.DatatypeFactory;
// import javax.xml.datatype.XMLGregorianCalendar;

// import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;


public class HpeUtil {


    public static String removeInvalidChar(String sPSrc) {
        // Howard: 將非英數字的字元 replace 為 空白
        String sLResult = "", sLTmp = "";
        for (int n = 0; n < sPSrc.length(); n++) {
            sLTmp = sPSrc.substring(n, n + 1);
            if (!sLTmp.matches("[0-9a-zA-Z]*")) {
                sLTmp = " ";
            }
            sLResult = sLResult + sLTmp;
        }
        return sLResult;
    }

    public static String getCurDateStr(boolean bPIncludeSep) throws DatatypeConfigurationException {

        SimpleDateFormat lsdf = null;

        if (bPIncludeSep)
            lsdf = new SimpleDateFormat("yyyy/MM/dd");
        else
            lsdf = new SimpleDateFormat("yyyyMMdd");


        String sLResult = lsdf.format(new Date());

        return sLResult;
    }

    public static void writeData2Socket(byte[] pDataAry, BufferedOutputStream pTargetOutputStream) {

        try {
            pTargetOutputStream.write(pDataAry);
        } catch (Exception e) {
            // TODO: handle exception
            // System.out.println("writeData2Socket exception=>" + e.getMessage() + "---");

        }
    }

    public static String genIsoField07() {
        String sLResult = "";

        try {
            // return MMDDHHmmss， e.g. 0803150000
            SimpleDateFormat sdFormat = new SimpleDateFormat("MMddHHmmss");
            Date date = new Date();
            String strDate = sdFormat.format(date);
            return strDate;


        } catch (Exception e) {
            // TODO: handle exception
        }
        return sLResult;
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
    
    public static byte[] decodedString2(String sPEncodedStr) {

        String sLResult = "";
        try {

            final Base64.Decoder decoder = Base64.getDecoder();


            // 解碼            
            return decoder.decode(sPEncodedStr);

        } catch (Exception e) {
            // TODO: handle exception
        }

        return "".getBytes();

    }

    public static Timestamp convertStringToTimestamp(String sPDateTime) {
        try {
            DateFormat formatter;
            formatter = new SimpleDateFormat("yyyyMMddHHmmss");

            Date date = formatter.parse(sPDateTime);
            java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());

            return timeStampDate;
        } catch (ParseException e) {
            // System.out.println("Exception :" + e);
            return null;
        }
    }

    public static Timestamp getCurTimestamp() {
        java.sql.Timestamp lCurTimeStamp = new java.sql.Timestamp(new java.util.Date().getTime());

        // System.out.println(L_CurTimeStamp.toString() ); //L_CurTimeStamp.toString() => 2018-01-04 09:27:43.245
        return lCurTimeStamp;
    }

    public static String getNextSeqValOfDb2(Connection pConnection, String sPSequenceName)
            throws Exception {
        // get sequence value

        String sLSeqVal = "0";
        try {


            String sLSql = "VALUES NEXTVAL FOR  " + sPSequenceName;

            // System.out.println("getNextSeqVal sql:" + sL_Sql + "==");

            /*
             * worked java.sql.Statement Db2Stmt = P_Connection.createStatement(); ResultSet
             * L_ResultSet = Db2Stmt.executeQuery(sL_Sql); System.out.println("a2");
             * 
             * if (L_ResultSet.next()) { sL_SeqVal = L_ResultSet.getString(1);
             * System.out.println("a3"); }
             */



            PreparedStatement db2Stmt = pConnection.prepareStatement(sLSql);


            ResultSet lResultSet = db2Stmt.executeQuery();


            if (lResultSet.next()) {
                sLSeqVal = lResultSet.getString(1);

            }


            lResultSet.close();

            db2Stmt.close();


        } catch (Exception e) {
            // TODO: handle exception
            // System.out.println("getNextSeqVal exception:" + e.getMessage());
            sLSeqVal = "0";
        }
        return sLSeqVal;

    }

    public static String getMaskData(String sPSrcData, int nPKeepLength, String sPMaskStr) {
        // call getMaskData("09730253334",4,"#") => return "#######3334"
        int nLMaskLength = sPSrcData.length() - nPKeepLength;
        String sLKeepData = sPSrcData.substring(nLMaskLength, sPSrcData.length());

        return HpeUtil.fillCharOnLeft(sLKeepData, sPSrcData.length(), sPMaskStr);

    }

    // ************************************************************************
    // 0:加密 1:解密
    // return password
    //
    //
    // ************************************************************************
    public static String transPasswd(int type, String fromPawd) throws Exception {
        long addNum[] = {7, 34, 295, 4326, 76325, 875392, 2468135, 12357924, 123456789};
        int transInt, int1, int2, datalen;
        long dataint = 1;
        String fdig[] = {"08122730435961748596", "04112633405865798792", "03162439425768718095",
                "04152236415768798390", "09182035435266718497", "01152930475463788296",
                "07192132465068748593", "02172931455660788394"};
        String tmpstr = "";
        String tmpstr1 = "";
        String toPawd = "";

        if (fromPawd.length() < 1)
            return "";

        if (type == 0) {
            // 加密
            datalen = fromPawd.length();
            for (int1 = 0; int1 < datalen; int1++) {
                int sbn = Integer.parseInt(fromPawd.substring(int1, int1 + 1)) * 2 + 1;
                tmpstr += fdig[int1].substring(sbn, sbn + 1);
            }

            for (int1 = 0; int1 < datalen; int1++) {
                dataint = dataint * 10;
            }
            tmpstr1 = String.valueOf(dataint + Long.parseLong(tmpstr) - addNum[datalen - 1]);
            toPawd = tmpstr1.substring(tmpstr1.length() - datalen);

        } else {
            // 解密
            datalen = fromPawd.length();

            tmpstr1 = String.format("%d", Long.parseLong(fromPawd) + addNum[datalen - 1]);
            tmpstr = tmpstr1.substring(tmpstr1.length() - datalen);
            for (int1 = 0; int1 < datalen; int1++) {
                for (int2 = 0; int2 < 10; int2++) {
                    int po = int2 * 2 + 1;
                    if (tmpstr.substring(int1, int1 + 1).equals(fdig[int1].substring(po, po + 1))) {
                        po = int2 * 2;
                        toPawd += fdig[int1].substring(po, po + 1);
                        break;
                    }
                }
            }
        }

        return toPawd;
    }

    // kevun:弱掃問題 (Often Misused: Authentication)
    // public static String getLocalIpAddress() throws Exception{
    // String sL_Ip = "";
    // InetAddress addr = InetAddress.getLocalHost();
    // sL_Ip = addr.getHostAddress();
    // return sL_Ip;
    // }
    public static java.sql.Date getCurDate4Sql() {
        java.sql.Date lCurDate = new java.sql.Date(new java.util.Date().getTime());
        return lCurDate;
    }

    public static String byte2ByteMap(String src, int size) {
        byte[] srcByte = new byte[65];
        String[] cvt = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000",
                "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
        String dest = "";
        int i = 0, ind = 0;
        srcByte = src.getBytes();

        for (i = 0; i < size; i++) {
            if (srcByte[i] >= '0' && srcByte[i] <= '9') {
                ind = (int) (srcByte[i] & 0x0F);
            } else if (srcByte[i] >= 'A' && srcByte[i] <= 'F') {
                ind = (int) (srcByte[i] & 0x0F);
                ind += 9;
            }

            dest = dest + cvt[ind];
        }
        return dest;
    }

    // combine all bytes array in List collection
    public static byte[] convertByteAryArrayList2ByteAry(List<byte[]> ByteAryList)
            throws NullPointerException, IOException {

        if (ByteAryList == null)
            throw new NullPointerException("The array has no data");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte[] item : ByteAryList) {
            outputStream.write(item);
        }

        return outputStream.toByteArray();
    }

    public static String transNullValue(String sPSrc) {
        String sLResult = sPSrc;
        if (null == sPSrc)
            sLResult = "";

        return sLResult;
    }

    public static String zeroPadBinChar(String binChar) {
        int len = binChar.length();
        if (len == 8)
            return binChar;
        String zeroPad = "0";
        for (int i = 1; i < 8 - len; i++)
            zeroPad = zeroPad + "0";
        return zeroPad + binChar;
    }

    public static String convertBinaryStrToHexStr(String binary) {
        String hex = "";
        String hexChar;
        int len = binary.length() / 8;
        for (int i = 0; i < len; i++) {
            String binChar = binary.substring(8 * i, 8 * i + 8);
            int convInt = Integer.parseInt(binChar, 2);
            hexChar = Integer.toHexString(convInt);
            hexChar = fillZeroOnLeft(hexChar, 2);
            if (i == 0)
                hex = hexChar;
            else
                hex = hex + hexChar;
        }
        return hex;
    }

    public static String convertHexStrToBinaryStr(String sPSrcHexStr) {
        String hexChar, binChar, binary;
        binary = "";
        int len = sPSrcHexStr.length() / 2;
        for (int i = 0; i < len; i++) {
            hexChar = sPSrcHexStr.substring(2 * i, 2 * i + 2);
            int convInt = Integer.parseInt(hexChar, 16);
            binChar = Integer.toBinaryString(convInt);
            binChar = zeroPadBinChar(binChar);
            if (i == 0)
                binary = binChar;
            else
                binary = binary + binChar;
            // out.printf("%s %s\n", hex_char,bin_char);
        }
        return binary;
    }

    public static String fillZeroOnLeft(String sPSrc, int nPTargetLen) {
        String sLResult = sPSrc;
        for (int i = sLResult.length(); i < nPTargetLen; i++) {
            sLResult = "0" + sLResult;
        }

        return sLResult;



    }

    public static String fillZeroOnLeft(double dPSrc, int nPTargetLen) {
        // 0 => ��ܭn��0
        // nP_TargetLen => ��̲ܳת���
        // d => ��ܬ������
        int nL_Src = (int) dPSrc;
        // int nL_Src = Integer.valueOf(dP_Src.intValue());

        return String.format("%0" + nPTargetLen + "d", nL_Src);



    }
    
//    // J2EE Bad Practices: Leftover Debug Code
//    public static void main(String[] args) {
//        try {
//            String sLHmacKeyHex =
//                    "C1C51B1535778FF511929BDFC5EBC82018C142DFB48FDE1E506AF45831D9F9CC0E8BA5D8FADF60EB";
//
//
//
//            String sLTmpString = hextoStr(sLHmacKeyHex);
//            // System.out.println("--" +sL_TmpString.length() + "--");
//            /*
//             * byte[] bytes = Hex.decodeHex(sL_HmacKeyHex.toCharArray()); System.out.println(new
//             * String(bytes, "UTF-8") + "---");
//             */
//            /*
//             * String sL_Date1 = "20190101"; String sL_Date2 = getCurDateStr(false); int nL_Result =
//             * compareDateString(sL_Date1, sL_Date2);
//             * 
//             * sL_Date1 = "20190901"; nL_Result = compareDateString(sL_Date1, sL_Date2);
//             */
//            // System.out.println("" +nL_Result);
//            /*
//             * String sL_Tmp = transPasswd(1,"377");
//             * 
//             * System.out.println(sL_Tmp);
//             */
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//    }

    public static String fillCharOnRight(String sPSrc, int nPTargetLen, String sLTarcharChar) {
        // �b�r��k��ɤW�Y�r��
        int strLen = sPSrc.length();
        if (strLen < nPTargetLen) {
            while (strLen < nPTargetLen) {
                StringBuffer sb = new StringBuffer();
                // sb.append(sL_TarcharChar).append(sP_Src);//����" "
                sb.append(sPSrc).append(sLTarcharChar);// �k��" "
                sPSrc = sb.toString();
                strLen = sPSrc.length();
            }
        }

        return sPSrc;


    }

    public static String getTransKeyValue() throws Exception {

        return fillCharOnLeft(getCurDateTimeStr(true, false), 20, "0");
    }

    public static String fillCharOnLeft(String sPSrc, int nPTargetLen, String sLTarcharChar) {
        // �b�r�ꥪ��ɤW�Y�r��
        int strLen = sPSrc.length();
        if (strLen < nPTargetLen) {
            while (strLen < nPTargetLen) {
                StringBuffer sb = new StringBuffer();
                sb.append(sLTarcharChar).append(sPSrc);// ����" "
                // sb.append(sP_Src).append(sL_TarcharChar);//�k��" "
                sPSrc = sb.toString();
                strLen = sPSrc.length();
            }
        }

        return sPSrc;


    }

    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1)
                : temp.toString();
    }

    // public static byte[] str2Bcd(String asc) {
    public static String str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte byte1[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }
        byte byte2[] = new byte[len];
        byte1 = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((byte1[2 * p] >= '0') && (byte1[2 * p] <= '9')) {
                j = byte1[2 * p] - '0';
            } else if ((byte1[2 * p] >= 'a') && (byte1[2 * p] <= 'z')) {
                j = byte1[2 * p] - 'a' + 0x0a;
            } else {
                j = byte1[2 * p] - 'A' + 0x0a;
            }
            if ((byte1[2 * p + 1] >= '0') && (byte1[2 * p + 1] <= '9')) {
                k = byte1[2 * p + 1] - '0';
            } else if ((byte1[2 * p + 1] >= 'a') && (byte1[2 * p + 1] <= 'z')) {
                k = byte1[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = byte1[2 * p + 1] - 'A' + 0x0a;
            }
            int byteA = (j << 4) + k;
            byte bytes = (byte) byteA;
            byte2[p] = bytes;
        }
        String sLResult = "";
        try {
            // sL_Result = new String(bbt, "UTF-8"); // Best way to decode using "UTF-8"
            sLResult = new String(byte2, Charset.forName("UTF-8")); // Best way to decode using
                                                                  // "UTF-8"
        } catch (Exception e) {
            sLResult = "";
        }
        return sLResult;
        // return bbt;
    }

    public static boolean isAmount(String param) {
        if (param == null || param.trim().length() == 0)
            return false;

        // double lm_val=0;
        try {
            Double.parseDouble(param);
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    public static boolean isNumberString(String sPSource) {



        for (char chars : sPSource.toCharArray()) {
            if (!Character.isDigit(chars))
                return false;
        }
        return true;

    }

    /*
     * Howard:marked on 0225 public static XMLGregorianCalendar getNow() throws
     * DatatypeConfigurationException{ XMLGregorianCalendar cal =
     * DatatypeFactory.newInstance().newXMLGregorianCalendar(); Calendar date =
     * Calendar.getInstance(); cal.setYear(date.get(Calendar.YEAR));
     * cal.setMonth(date.get(Calendar.MONTH)+1); cal.setDay(date.get(Calendar.DATE));
     * cal.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE),
     * date.get(Calendar.SECOND)); return cal; }
     */
    public static int binaryToInt(String str) {
        double j = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '1') {
                j = j + Math.pow(2, str.length() - 1 - i);
            }

        }
        return (int) j;
    }

    public static String convertToBinary(String sPSrc) {
        byte[] lAry = sPSrc.getBytes();

        String sLTmp = "", sLResult = "";
        int nLResult1 = 0, nLResult2 = 0;
        for (int i = 0; i < lAry.length; i++) {
            sLTmp = Integer.toBinaryString(0x100 + lAry[i]).substring(1);
            // System.out.println(sL_Tmp);


            nLResult1 = binaryToInt(sLTmp.substring(0, 4));

            nLResult2 = binaryToInt(sLTmp.substring(4, 8));


            sLResult = sLResult + Integer.toString(nLResult1) + Integer.toString(nLResult2);


            // System.out.println(Integer.toBinaryString(L_Ary[i]).substring(1));
        }
        return sLResult;
    }

    /*
     * public static XMLGregorianCalendar getCurDate() throws DatatypeConfigurationException{
     * XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(); Calendar
     * date = Calendar.getInstance(); cal.setYear(date.get(Calendar.YEAR));
     * cal.setMonth(date.get(Calendar.MONTH)+1); cal.setDay(date.get(Calendar.DATE));
     * 
     * //cal.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE),
     * date.get(Calendar.SECOND)); return cal; }
     */

    /*
     * public static String getCurDateStr(String sP_Sep) throws DatatypeConfigurationException{
     * 
     * Calendar date = Calendar.getInstance(); String sL_Year =
     * Integer.toString(date.get(Calendar.YEAR));
     * 
     * String sL_Month = Integer.toString(date.get(Calendar.MONTH)+1); sL_Month =
     * fillCharOnLeft(sL_Month, 2, "0");
     * 
     * String sL_Date = Integer.toString(date.get(Calendar.DATE)); sL_Date = fillCharOnLeft(sL_Date,
     * 2, "0");
     * 
     * String sL_Result = sL_Year + sP_Sep + sL_Month+ sP_Sep + sL_Date; return sL_Result; }
     */

    /*
     * public static String getCurMonthAndDate(String sP_Sep) throws DatatypeConfigurationException{
     * 
     * Calendar date = Calendar.getInstance();
     * 
     * 
     * int nL_Month = date.get(Calendar.MONTH) +1; String sL_Result = fillCharOnLeft(
     * Integer.toString(nL_Month), 2, "0") + sP_Sep + fillCharOnLeft(
     * Integer.toString(date.get(Calendar.DATE)), 2, "0"); return sL_Result; }
     */
    public static String getTaiwanDateStr(String sPSrcString)
            throws DatatypeConfigurationException {
        // �� 20110909 �ഫ�� 1000909
        String sLResult = sPSrcString;
        if (sPSrcString.length() >= 8)
            sLResult = Integer.parseInt(sPSrcString.substring(0, 4)) - 1911
                    + sPSrcString.substring(4, 8);

        return sLResult;
    }

    public static boolean isFirstCharLatter(String sPSource) {

        boolean bLResult = false;
        char ch = sPSource.charAt(0);
        if (ch <= 'z' && ch >= 'a' || ch <= 'Z' && ch >= 'A') {
            bLResult = true;
        }
        return bLResult;


    }

    public static int compareDateString(String sLDate1, String sLDate2) {
        int nLResult = 0;
        try {
            Date dLDate1 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(sLDate1);
            Date dLDate2 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(sLDate2);

            // System.out.println(dL_Date1);
            // System.out.println(dL_Date2);

            if (dLDate1.compareTo(dLDate2) > 0) {
                nLResult = 1; // dL_Date1 > dL_Date2
            } else if (dLDate1.compareTo(dLDate2) < 0) {
                nLResult = -1; // dL_Date1 < dL_Date2
            } else if (dLDate1.compareTo(dLDate2) == 0) {
                nLResult = 0; // dL_Date1 = dL_Date2
            } else {
                nLResult = -1;
                // System.out.println("Something weird happened...");
            }

        } catch (Exception e) {
            // e.printStackTrace();
            nLResult = -1;
        }

        return nLResult;
    }


    public static Date addDays(Date dPSrcDate, int nPAddDayCount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dPSrcDate);
        cal.add(Calendar.DATE, nPAddDayCount);
        Date dLResult = cal.getTime();

        return dLResult;
    }


    /**
     * 判斷系統日期是否介於 sP_BeginDate 與 sP_EndDate 之間
     * 
     * @param sPBeginDate
     * @param sPEndDate
     * @return
     */
    public static boolean isCurDateBetweenTwoDays(String sPBeginDate, String sPEndDate) {
        boolean bLResult = false;

        if ((sPBeginDate.trim().equals("")) || (sPEndDate.trim().equals(""))) {

            return false;
        }
        try {
            String sLCurDate = getCurDateStr(false);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date dLBeginDate = sdf.parse(sPBeginDate);
            Date dLEndDate = sdf.parse(sPEndDate);

            dLBeginDate = addDays(dLBeginDate, -1);
            dLEndDate = addDays(dLEndDate, 1);

            Date dLCurDate = sdf.parse(sLCurDate);

            if ((dLBeginDate.before(dLCurDate)) && (dLCurDate.before(dLEndDate))) {
                bLResult = true;
            } else {
                bLResult = false;
            }
        } catch (Exception e) {
            // TODO: handle exception
            bLResult = false;
        }

        return bLResult;
    }

    public static int compareDateDiffOfDay(String sLDate1, String sLDate2) {
        // 計算兩個日期差異幾天 => sL_Date2 - sL_Date1 看看相隔幾天
        int nLResult = 0;
        try {
            Date dLDate1 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(sLDate1);
            Date dLDate2 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(sLDate2);

            long lBetweenDate = (dLDate2.getTime() - dLDate1.getTime()) / (1000 * 60 * 60 * 24);

            nLResult = Integer.parseInt(Long.toString(lBetweenDate));



        } catch (Exception e) {
            // e.printStackTrace();
            nLResult = -1;
        }

        return nLResult;
    }

    public static int getRandomNumber(int nLMaxNum) {
        int nLResult = 0;

        SecureRandom lRandomGen = new SecureRandom();

        nLResult = lRandomGen.nextInt(nLMaxNum);
        lRandomGen = null;

        return nLResult;
    }

    /*
     * public static String getCurHourAndMinStr(String sP_Sep) throws
     * DatatypeConfigurationException{
     * 
     * Calendar date = Calendar.getInstance();
     * 
     * String sL_Hour = String.format("%1$tH", date); sL_Hour = fillCharOnLeft(sL_Hour, 2, "0");
     * 
     * String sL_Min = String.format("%1$tM", date); sL_Min = fillCharOnLeft(sL_Min, 2, "0");
     * 
     * String sL_Result = sL_Hour + sP_Sep + sL_Min ; return sL_Result; }
     */

    /*
     * public static String getCurHMS(String sP_Sep) throws DatatypeConfigurationException{
     * //取得現在的HHMMSS Calendar date = Calendar.getInstance();
     * 
     * String sL_Hour = String.format("%1$tH", date); sL_Hour = fillCharOnLeft(sL_Hour, 2, "0");
     * 
     * String sL_Min = String.format("%1$tM", date); sL_Min = fillCharOnLeft(sL_Min, 2, "0");
     * 
     * String sL_Sec = String.format("%1$tS", date); sL_Sec = fillCharOnLeft(sL_Sec, 2, "0");
     * 
     * 
     * String sL_Result = sL_Hour + sP_Sep + sL_Min + sP_Sep + sL_Sec; return sL_Result; }
     */
    private static String pad(String str, int size, char padChar) {
        StringBuffer padded = new StringBuffer(str);
        while (padded.length() < size) {
            padded.append(padChar);
        }
        return padded.toString();
    }

    public static byte[] stringToBytes22(String str) {

        String data;

        data = pad(str, str.length(), ' ');

        return data.getBytes(Charset.forName("UTF-8"));
        // return data.getBytes("Cp500");

        /*
         * byte[] b = new byte[str.length() / 8]; int count = 0;
         * 
         * for (int i = 0; i < b.length; i++) { b[i] = Byte.parseByte(str.substring(count, count +
         * 8), 2); count += 8; } // Integer.parseInt(c, 2)
         * 
         * return b;
         */

    }


    public static String hextoStr(String sPHexString) {
        byte[] bytes;
        String sLResult = "";
        try {

            /*
             * bytes = DatatypeConverter.parseHexBinary(sP_HexString); sL_Result= new String(bytes);
             */


            bytes = Hex.decodeHex(sPHexString.toCharArray());
            // sL_Result = new String(bytes, "UTF-8") ;
            sLResult = new String(bytes);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return sLResult;
    }

    public static byte[] transHexString2ByteAry(String src) {
        byte[] biBytes = new BigInteger("10" + src.replaceAll("\\s", ""), 16).toByteArray();
        return Arrays.copyOfRange(biBytes, 1, biBytes.length);
    }

    public static String strToHex(String sPAsciiString) {
        char[] chars = sPAsciiString.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }
        return hex.toString();
    }


    private static String asciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }
        return hex.toString();
    }

    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static byte[] hexStrToByteArr(String sPSrc) {

        int len = sPSrc.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(sPSrc.charAt(i), 16) << 4)
                    + Character.digit(sPSrc.charAt(i + 1), 16));
        return data;
    }


    public static byte[] strToByteAry(String sPSrc) throws UnsupportedEncodingException {


        byte[] b = new byte[sPSrc.length() / 8];
        int count = 0;

        for (int i = 0; i < b.length; i++) {
            b[i] = Byte.parseByte(sPSrc.substring(count, count + 8), 2);
            count += 8;
        }
        // Integer.parseInt(c, 2)

        return b;



    }

    public static String readDataFromEasyCard(BufferedInputStream pEasyCardBufferedInputStream,
            BufferedReader pBufferReader) throws Exception {
        int headLen = 0, packetLen = 0, inputLen = 0;
        byte[] authData = new byte[2048];
        byte[] lenData = new byte[3];
        String sLIsoStrFromEasyCard = "";

        try {
            BufferedReader lEasyCardBufferedReader =
                    new BufferedReader(new InputStreamReader(pEasyCardBufferedInputStream));
            if (1 == 1) {
                // BufferedInputStream G_SocketReader = new
                // BufferedInputStream(P_Socket.getInputStream());


                // while (true) {
                headLen = pEasyCardBufferedInputStream.read(lenData, 0, 2);
                if (headLen != 2) {
                    return "";
                }
                // �q SOCKET Ū��������
                packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);

                inputLen = pEasyCardBufferedInputStream.read(authData, 0, packetLen);

                sLIsoStrFromEasyCard = new String(authData, 0, inputLen);


                // System.out.print("readDataFromEasyCard =>" + sL_IsoStrFromEasyCard + "===");


                // break;
                // }
            } else {

                // BufferedReader in = new BufferedReader(new
                // InputStreamReader(P_Socket.getInputStream()));
                // BufferedReader in = P_BufferReader;

                while ((sLIsoStrFromEasyCard = lEasyCardBufferedReader.readLine()) != null) {

                    break;
                }

                // System.out.print("readDataFromEasyCard :" + sL_IsoStrFromEasyCard + "===");

            }
        } // end try
        catch (Exception e) {
            // System.out.println("readDataFromEasyCard() error=> " + e.getMessage());
            throw e;
        }
        return sLIsoStrFromEasyCard;
    }

    public static String readDataFromEcs(BufferedInputStream pEcsBufferedInputStream)
            throws Exception {
        int headLen = 0, packetLen = 0, inputLen = 0;
        byte[] authData = new byte[2048];
        byte[] lenData = new byte[3];
        String sLIsoStrFromEcs = "";

        try {

            if (1 == 1) {
                // BufferedInputStream G_SocketReader = new
                // BufferedInputStream(P_Socket.getInputStream());


                // while (true) {
                headLen = pEcsBufferedInputStream.read(lenData, 0, 2);
                if (headLen != 2) {
                    return "";
                }
                // �q SOCKET Ū��������
                packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);

                inputLen = pEcsBufferedInputStream.read(authData, 0, packetLen);

                sLIsoStrFromEcs = new String(authData, 0, inputLen);

                // System.out.print("readDataFromEcs :" + sL_IsoStrFromEcs + "===");


                // break;
                // }
            } else {

            }
        } // end try
        catch (Exception e) {
            System.out.println("readDataFromEcs() error=> " + e.getMessage());
        }
        return sLIsoStrFromEcs;
    }

    /*
     * public static void writeData2EasyCard(Socket P_Socket, String sP_Data) throws Exception{
     * 
     * 
     * BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());
     * BufferedOutputStream L_SocketWriter = new BufferedOutputStream(P_Socket.getOutputStream());
     * 
     * 
     * 
     * if (1==1) { PrintWriter L_PrintWriter = new PrintWriter(P_Socket.getOutputStream());
     * L_PrintWriter.println("****" + sP_Data + "================"); L_PrintWriter.flush(); } else {
     * L_SocketWriter.write(sP_Data.getBytes(), 0, sP_Data.getBytes().length);
     * 
     * L_SocketWriter.flush(); }
     * 
     * }
     */
    public static String exchangeDataWithEasyCard(
            BufferedOutputStream pEasyCardBufferedOutputStream,
            BufferedInputStream pEasyCardBufferedInputStream, String sPData) throws Exception {
        String sLEasyCardResponseData = "";

        // BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());



        if (1 == 2) {
            /*
             * PrintWriter L_PrintWriter = new PrintWriter(P_EasyCardBufferedOutputStream);
             * L_PrintWriter.println("****" + sP_Data + "================"); L_PrintWriter.flush();
             */
        } else {

            writeData2EasyCard(pEasyCardBufferedOutputStream, sPData);
        }
        sLEasyCardResponseData = readDataFromEasyCard(pEasyCardBufferedInputStream, null);

        return sLEasyCardResponseData;
    }

    public static String getMonthEndDate(String sPYear, String sPMonth) {
        // 傳入 (2016,12) => 回傳 20161231
        // 傳入 (2016,2) => 回傳 20160229
        String sLResult = "";
        try {
            YearMonth yearMonth = YearMonth.of(Integer.parseInt(sPYear), Integer.parseInt(sPMonth));
            LocalDate endOfMonth = yearMonth.atEndOfMonth();
            // DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate( FormatStyle.SHORT );
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            formatter = formatter.withLocale(Locale.US); // Re-assign JVM’s current default Locale
                                                         // that was implicitly applied to the
                                                         // formatter.
            sLResult = endOfMonth.format(formatter);

            // System.out.println(sL_Result+"--");

        } catch (Exception e) {
            // TODO: handle exception
            sLResult = "";
        }
        return sLResult;
    }

    private static String interpretHostResponseData(byte[] response)
            throws UnsupportedEncodingException, IOException {

        String ebss; // X(1)
        String tsqName; // X(16)
        String filler; // X(20
        String httpReturnCode = ""; // X(2)
        String result = "";
        int iPos = 0;
        StringBuilder receiveLog = new StringBuilder("");

        try {

            ebss = getHostReturnFiled(response, iPos, 1); // 0~1
            iPos = iPos + 1;
            receiveLog.append("clsCWSServerPtl::EBSS::" + ebss + "\r\n");

            tsqName = getHostReturnFiled(response, iPos, 16); // 1~16
            iPos = iPos + 16;
            receiveLog.append("clsCWSServerPtl::TSQ_Name::" + tsqName + "\r\n");

            filler = getHostReturnFiled(response, iPos, 20); // 16~36
            iPos = iPos + 20;
            receiveLog.append("clsCWSServerPtl::Filler::" + filler + "\r\n");

            httpReturnCode = getHostReturnFiled(response, iPos, 2); // 36~38
            iPos = iPos + 2;
            receiveLog.append("clsCWSServerPtl::HttpReturnCode::" + httpReturnCode + "\r\n");

            if (iPos < response.length) {
                byte[] byteHostFieldData = new byte[4];
                System.arraycopy(response, iPos, byteHostFieldData, 0, 4);
                int PacketLen = byteToInteger(byteHostFieldData);
                iPos = iPos + 4;
                receiveLog.append("clsCWSServerPtl::PacketLen::" + PacketLen + "\r\n");

                result = getHostReturnFiled(response, iPos, PacketLen);
                receiveLog.append("clsCWSServerPtl::result::" + result + "\r\n");
            }
        } catch (UnsupportedEncodingException e) {
            // logger.error(this.getClass().getName() + "_interpretHostResponseData() " +
            // e.getClass().getName() + " : " + e.getMessage());
            // notifyMonitor.sendMonitor(e.getClass().getName() + " : " + e.getMessage(),
            // this.getClass().getName() + "_interpretHostResponseData()");
            // System.out.println("interpretHostResponseData error 1=>" + e.getMessage());
            throw e;
        } catch (IOException e) {
            // logger.error(this.getClass().getName() + "_interpretHostResponseData() " +
            // e.getClass().getName() + " : " + e.getMessage());
            // notifyMonitor.sendMonitor(e.getClass().getName() + " : " + e.getMessage(),
            // this.getClass().getName() + "_interpretHostResponseData()");
            // System.out.println("interpretHostResponseData error 2=>" + e.getMessage());
            throw e;
        }

        return httpReturnCode + result;
    }

    private static int byteToInteger(byte[] bytes) {

        StringBuilder sb = new StringBuilder();

        if (bytes != null && bytes.length > 0) {
            // Bytes array to binary string
            for (int i = 0; i < bytes.length; i++) {
                sb.append(padLeft(Integer.toBinaryString(bytes[i] & 0xFF), 8, '0'));
            }
        }

        return Integer.parseInt(sb.toString(), 2);
    }

    private static String getHostReturnFiled(byte[] hostReturnData, int start, int dataLen)
            throws UnsupportedEncodingException, IOException {

        StringBuilder sb = new StringBuilder();

        try {

            if (dataLen > 0) {
                byte[] byteHostFiled = new byte[dataLen];
                System.arraycopy(hostReturnData, start, byteHostFiled, 0, dataLen);
                InputStream is = new ByteArrayInputStream(byteHostFiled);
                Reader in = new InputStreamReader(is, "Cp500");
                int buf = -1;

                while ((buf = in.read()) > -1) {
                    sb.append((char) buf);
                }
                in.close();
            }
        } catch (UnsupportedEncodingException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }

        return sb.toString();
    }

    public static String exchangeDataWithEcs(BufferedOutputStream pEcsBufferedOutputStream,
            BufferedInputStream pEasyCardBufferedInputStream, String sPData) throws Exception {
        String sLEcsResponseData = "";

        // BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());


        try {

            // P_EasyCardBufferedOutputStream.write(sP_Data.getBytes(), 0,
            // sP_Data.getBytes().length);

            byte[] lAry = HpeUtil.genIsoByteAry(sPData);
            pEcsBufferedOutputStream.write(lAry, 0, lAry.length);

            pEcsBufferedOutputStream.flush();


            sLEcsResponseData = readDataFromEcs(pEasyCardBufferedInputStream);
        } catch (Exception e) {
            // System.out.println("exception on exchangeDataWithEcs() =>" + e.getMessage());
        }
        return sLEcsResponseData;
    }

    public static byte[] genIsoByteAry(String sPIsoData) {
        byte[] lEntireAry = null;
        try {
            lEntireAry = ("00" + sPIsoData).getBytes("IBM-1047");

            // System.out.println("---IsoStr is=>"+ gate.isoString + "----");
            int nLTotalLen = lEntireAry.length;
            int nLDataLen = nLTotalLen - 2;
            lEntireAry[0] = (byte) (nLDataLen / 256);
            lEntireAry[1] = (byte) (nLDataLen % 256);

            /*
             * byte[] L_LenAry = new byte[2]; L_LenAry[0] = (byte)(sP_IsoData.length() / 256);
             * L_LenAry[1] = (byte)(sP_IsoData.length() % 256);
             * 
             * 
             * List<byte[]> ByteAryList = new ArrayList<byte[]>(); ByteAryList.add(L_LenAry);
             * 
             * byte[] L_DataAry = strToByteAry(sP_IsoData);
             * 
             * ByteAryList.add(L_DataAry); L_EntireAry =
             * convertByteAryArrayList2ByteAry(ByteAryList);
             */

        } catch (Exception e) {

        }
        return lEntireAry;
    }

    public static byte[] addLength2HeadOfByteAry(byte[] pSrcByteAry) {
        byte[] iEntireAry = null;
        try {
            int nLDataLen = pSrcByteAry.length;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write("00".getBytes());
            outputStream.write(pSrcByteAry);
            iEntireAry = outputStream.toByteArray();
            iEntireAry[0] = (byte) (nLDataLen / 256);
            iEntireAry[1] = (byte) (nLDataLen % 256);



        } catch (Exception e) {

        }
        return iEntireAry;
    }

    public static void writeData2EasyCard(BufferedOutputStream pEasyCardBufferedOutputStream,
            String sPData) throws Exception {
        if ("".equals(sPData))
            return;

        byte[] lAry = HpeUtil.genIsoByteAry(sPData);
        pEasyCardBufferedOutputStream.write(lAry, 0, lAry.length);

        pEasyCardBufferedOutputStream.flush();
        /*
         * //BufferedInputStream L_SocketReader = new
         * BufferedInputStream(P_Socket.getInputStream()); BufferedOutputStream L_SocketWriter =
         * P_EasyCardBufferedOutputStream;
         * 
         * 
         * 
         * if (1==1) { //���ծɡA�Ȧ�post�L�Ӫ���ơA�n�o�ˤ~�i�H�e��easycard PrintWriter L_PrintWriter =
         * new PrintWriter(P_EasyCardBufferedOutputStream); L_PrintWriter.println("****" + sP_Data +
         * "================"); L_PrintWriter.flush(); } else {
         * L_SocketWriter.write(sP_Data.getBytes(), 0, sP_Data.getBytes().length);
         * 
         * L_SocketWriter.flush(); }
         */
    }

    public static String genBitMap(List pDataFieldList) {
        String sLBitmap = "", sLFirstByteValue = "0";
        for (int i = 2; i <= 128; i++) {
            if (pDataFieldList.indexOf(i) >= 0) {
                sLBitmap += "1";
                if (i > 64)
                    sLFirstByteValue = "1";
            } else
                sLBitmap += "0";
        }

        sLBitmap = sLFirstByteValue + sLBitmap;

        String sLBitmap1 = sLBitmap.substring(0, 64);
        String sLBitmap2 = sLBitmap.substring(64, 128);

        String sLHexBitmap =
                convertBinaryStrToHexStr(sLBitmap1) + convertBinaryStrToHexStr(sLBitmap2);

        return sLHexBitmap;

    }

    public static void writeData2Socket(BufferedOutputStream pOutputStream, byte[] pData)
            throws Exception {
        if (pData == null)
            return;

        try {
            // sP_Data = "CCC" +sP_Data;//for test
            // System.out.println("begin writeData2Auth()");
            // System.out.println("data string is =>" + sP_Data + "===");
            byte[] lAry = pData;
            // byte[] L_Ary = HpeUtil.genIsoByteAry(sP_Data);
            // System.out.println("data from byte array is =>" + new String(L_Ary) + "===");
            pOutputStream.write(lAry, 0, lAry.length);

            pOutputStream.flush();
            // System.out.println("end writeData()");

        } catch (Exception ex) {

            // TODO: handle exception
            throw ex;

        }
    }

    public static String readDataFromSocket(BufferedInputStream pInputStream) throws Exception {
        String sLResult = "";
        try {
            byte[] lDataByteAry = new byte[2048];
            byte[] lenData = new byte[3];
            int headLen = pInputStream.read(lenData, 0, 2);

            if (headLen == 2) {
                int packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);

                int inputLen = pInputStream.read(lDataByteAry, 0, packetLen);

                sLResult = new String(lDataByteAry, 0, inputLen);
            }

        } catch (Exception ex) {

            // TODO: handle exception
            throw ex;

        }

        return sLResult;
    }


    public static void writeData2ECS(BufferedOutputStream pBufferedOutputStream, String sPData)
            throws Exception {


        // BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());



        if (1 == 2) {
            /*
             * PrintWriter L_PrintWriter = new PrintWriter(P_BufferedOutputStream);
             * L_PrintWriter.println("****" + sP_Data + "================"); L_PrintWriter.flush();
             */
        } else {
            byte[] lAry = genIsoByteAry(sPData);
            byte[] lenData = new byte[3];
            byte[] authData = new byte[2048];

            pBufferedOutputStream.write(lAry, 0, lAry.length);
            pBufferedOutputStream.flush();


        }


    }

    public static String getCurTimeStr() throws DatatypeConfigurationException {

        // SimpleDateFormat L_SDF = new SimpleDateFormat("hhmmssSSS");
        SimpleDateFormat lSDF = new SimpleDateFormat("HHmmss");

        String sLResult = lSDF.format(new Date());

        return sLResult;
    }

    public static String getByteHex(byte[] inputByte) {
        StringBuilder str = new StringBuilder();
        for (byte byte1 : inputByte) {
            str.append(toHex(byte1));
        }
        return str.toString();
    }

    public static String toHex(byte b) {
        return ("" + "0123456789ABCDEF".charAt(0xf & b >> 4) + "0123456789ABCDEF".charAt(b & 0xf));
    }

    public static byte[] transToEBCDIC(String sPSrc) throws UnsupportedEncodingException {
        /*
         * String data;
         * 
         * if (this._data.length() > _length) data = this._data.substring(0, _length); else data =
         * pad(this._data, _length, ' ');
         * 
         * 
         * clsTools.recordSendLog(data);
         */
        // return sP_Src.getBytes("Cp500");
        System.out.println("ebcdic=Cp1047");
        return sPSrc.getBytes("Cp1047");

    }

    public static String padLeft(String str, int size, char padChar) {
        StringBuilder padded = new StringBuilder(str);
        while (padded.length() < size) {
            padded.insert(0, padChar);
        }
        return padded.toString();
    }

    public static byte[] stringToBytes(String str) {
        byte[] b = new byte[str.length() / 8];
        int count = 0;

        for (int i = 0; i < b.length; i++) {
            b[i] = Byte.parseByte(str.substring(count, count + 8), 2);
            count += 8;
        }
        // Integer.parseInt(c, 2)
        // System.out.println("result of stringToBytes()=>" + b.toString() + "---" );
        return b;
    }

    public static String getCurDateTimeStr(boolean bPIncludeMSec, boolean bLIncludeSep)
            throws DatatypeConfigurationException {

        SimpleDateFormat lSDF = null;
        if (bPIncludeMSec) {
            if (bLIncludeSep)
                lSDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
            else
                lSDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        } else {
            if (bLIncludeSep)
                lSDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            else
                lSDF = new SimpleDateFormat("yyyyMMddHHmmss");
        }
        String sLResult = lSDF.format(new Date());

        return sLResult;
    }

    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0;) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static List<String> getAllFileName(String sPFullPathFile) {
        List<String> lFileNames = new ArrayList<String>();

        File[] files = new File(sPFullPathFile).listFiles();
        // If this pathname does not denote a directory, then listFiles() returns null.

        for (File file : files) {
            if (file.isFile()) {
                lFileNames.add(file.getName());
            }
        }

        return lFileNames;
    }

    public static List<String> readFileAllLines(String sPFullPathFileName) {
        List<String> lResult = null;
        Path file = Paths.get(sPFullPathFileName);

        try {
            if (Files.exists(file) && Files.isReadable(file)) {
                lResult = Files.readAllLines(file, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            lResult = null;
            // e.printStackTrace();
        }
        return lResult;
    }

    public static boolean deleteFile(String sPFullPathFileName) {
        boolean bLResult = true;

        try {
            Path file = Paths.get(sPFullPathFileName);
            Files.delete(file);
        } catch (IOException e) {
            bLResult = false;
            // e.printStackTrace();
        }
        return bLResult;

    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static boolean writeToFile(String sPFullPathFileName, String sPContent) {
        boolean bLResult = true;

        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(sPFullPathFileName), "utf-8"))) {
            writer.write(sPContent);
        } catch (IOException e) {
            bLResult = false;
            // e.printStackTrace();
        }
        return bLResult;

    }

    // kevin:這邊總共有四個方法，分別是將 string/hex 互轉以及 byte array/hex 互轉
    public static String byte2Hex(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++)
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        return result;
    }

    public static String string2Hex(String plainText, String charset)
            throws UnsupportedEncodingException {
        return String.format("%040x", new BigInteger(1, plainText.getBytes(charset)));
    }

    public static byte[] hex2Byte(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
        return bytes;
    }

    public static String hex2String(String hexString) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2)
            str.append((char) Integer.parseInt(hexString.substring(i, i + 2), 16));
        return str.toString();
    }

    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     * 
     * @param String str Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     * 
     * @param byte[] b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            // sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     * 
     * @param String src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        int m = 0, n = 0;
        int strLength = src.length() / 2;
        System.out.println(strLength);
        byte[] ret = new byte[strLength];
        for (int i = 0; i < strLength; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     * 
     * @param String strText 全角字符串
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText) throws Exception {
        char chars;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++) {
            chars = strText.charAt(i);
            intAsc = (int) chars;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else
                // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     * 
     * @param String hex 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex) {
        int hexLength = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < hexLength; i++) {
            String hexSub = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String param = hexSub.substring(2, 4) + "00";
            // 低位直接转
            String param1 = hexSub.substring(4);
            // 将16进制的string转为int
            int cnt = Integer.valueOf(param, 16) + Integer.valueOf(param1, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(cnt);
            str.append(new String(chars));
        }
        return str.toString();
    }

    public static byte[] asBytes(String bytes) {
        String tmp;
        byte[] byteLength = new byte[bytes.length() / 2];
        int i;
        for (i = 0; i < bytes.length() / 2; i++) {
            tmp = bytes.substring(i * 2, i * 2 + 2);
            byteLength[i] = (byte) (Integer.parseInt(tmp, 16) & 0xff);
        }
        return byteLength; // return bytes
    }

    public static byte[] genFiscIsoByteAry(String sPIsoData, String sPBitMap) {
        byte[] iEntireAry = null;
        try {
            iEntireAry = ("00" + sPIsoData).getBytes("IBM-1047");

            // System.out.println("---IsoStr is=>"+ gate.isoString + "----");
            int nLTotalLen = iEntireAry.length;
            int nLDataLen = nLTotalLen - 2;
            iEntireAry[0] = (byte) (nLDataLen / 256);
            iEntireAry[1] = (byte) (nLDataLen % 256);
            byte[] sLBitMap = hex2Byte(sPBitMap);
            int x = 6;
            int y = 7;
            for (int i = 0; i < sLBitMap.length; i++) {
                System.out.println("BITMAP=" + sLBitMap[i]);
                Arrays.fill(iEntireAry, x, y, sLBitMap[i]);
                x++;
                y++;
            }


        } catch (Exception e) {

        }
        return iEntireAry;
    }

    // kevin:轉換byte再決定是否轉換ebcdic
    public static byte[] getSubByteAry(byte[] pSrcByteAry, int nPBeginPos, int nPLength) {
        byte[] lResult = new byte[nPLength];

        System.arraycopy(pSrcByteAry, nPBeginPos, lResult, 0, nPLength);

        return lResult;

    }

    // kevin:轉換ebcdic to ascii
    public static String ebcdic2Str(byte[] conStr) throws UnsupportedEncodingException {

        byte[] asc =
                "                                                                          [.<(+]&         !$*);^-/        |,%_>?         `:#@'=#0abcdefghi       jklmnopqr       ~stuvwxyz                      {ABCDEFGHI      }JKLMNOPQR      #0STUVWXYZ      0123456789      "
                        .getBytes("ASCII");

        byte[] rtn = new byte[conStr.length];

        for (int i = 0; i < conStr.length; i++) {
            int j = byteToUnsignedInt(conStr[i]);
            rtn[i] = asc[j];
        }
        return new String(rtn, "ASCII");

    }

    private static int byteToUnsignedInt(byte bytes) {
        return 0x00 << 24 | bytes & 0xff;
    }

    // kevin: post message to RS for request token
    public static String curlToken(String url, String accepts, String token, String minusD)
            throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        //kevin:https連線
        SSLSocketFactory oldSocketFactory = null;
        HostnameVerifier oldHostnameVerifier = null;
        boolean useHttps = url.startsWith("https");
        if (useHttps) {
            HttpsURLConnection https = (HttpsURLConnection) con;
            oldSocketFactory = trustAllHosts(https);
            oldHostnameVerifier = https.getHostnameVerifier();
            https.setHostnameVerifier(DO_NOT_VERIFY);
        }
        
        con.setDoOutput(true);
        con.setReadTimeout(20 * 1000);
        con.setConnectTimeout(20 * 1000);
        con.setRequestMethod("POST");
        con.setRequestProperty("accept", "*/*");
        if (token.length() > 0) {
        	token = token.replaceAll("\r", "");
        	token = token.replaceAll("%0d", "");
        	token = token.replaceAll("\n", "");
        	token = token.replaceAll("%0a", "");
        	token = token.replaceAll(":", "");
        	token = token.replaceAll("=", "");
            con.setRequestProperty("Authorization", "Bearer " + token);

        }
        accepts = accepts.replaceAll("\r", "");
        accepts = accepts.replaceAll("%0d", "");
        accepts = accepts.replaceAll("\n", "");
        accepts = accepts.replaceAll("%0a", "");
        accepts = accepts.replaceAll(":", "");
        accepts = accepts.replaceAll("=", "");
        con.setRequestProperty("Content-Type", accepts);
        try (OutputStream reqStream = con.getOutputStream()) {
        	reqStream.write(minusD.getBytes());
        }
        
//        int respCode = con.getResponseCode();
        
        try (ByteArrayOutputStream rspBuff= new ByteArrayOutputStream();        
        	InputStream rspStream = con.getInputStream();) {
	        int c;
	        while ((c = rspStream.read()) > 0) {
	            rspBuff.write(c);
	        }	        	        
	        return new String(rspBuff.toByteArray());
        }
    }

    // kevin:base64 encoding
    public static String encoded2Base64(byte[] bPSrc) {

        String sLResult = "";
        try {

            final Base64.Encoder encoder = Base64.getEncoder();
            sLResult = encoder.encodeToString(bPSrc);

        } catch (Exception e) {
            // TODO: handle exception
        }

        return sLResult;
    }

    // kevin:decimalRemove去除小數位
    public static String decimalRemove(double bLAmt) {

        String sLResult = "";
        DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
        sLResult = decimalFormat.format(bLAmt);

        return sLResult;
    }

    // kevin:編碼UTF-8 、Cp1047
    public static byte[] transByCode(String sPSrc, String sPCode)
            throws UnsupportedEncodingException {
        System.out.println("Code =" + sPCode);
        return sPSrc.getBytes(sPCode);
    }
    
  //kevin:設定信任
    private static final TrustManager[] trustAllCerts = new TrustManager[]{ new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
    }};
    
    /**
     * 設置不驗證主機
     */
    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    
    private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
        SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
        try {
//            SSLContext sc = SSLContext.getInstance("TLS");
        	SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();
            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oldFactory;
    }
    
}
