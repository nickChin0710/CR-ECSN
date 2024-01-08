/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  110-01-07    yanghan       修改了无意义的变量名称           *
*****************************************************************************/
package bank.authbatch.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


public class HpeUtil {
	public static java.sql.Date getCurDate4Sql() {
		java.sql.Date L_CurDate =  new java.sql.Date(new java.util.Date().getTime());
		return L_CurDate;
	}

	/*
	public static String getNextSeqValOfDb2(Connection P_Connection, String sP_SequenceName)  throws Exception {
		//get sequence value

		String sL_SeqVal = "0";
		try {


			//String sL_Sql = " select VALUES NEXTVAL FOR  "+ sP_SequenceName  ;
			String sL_Sql = "  SELECT NEXT VALUE FOR " + sP_SequenceName + " FROM sysibm.sysdummy1 "  ;

			//System.out.println("getNextSeqVal sql:" + sL_Sql + "==");



			PreparedStatement Db2Stmt = P_Connection.prepareStatement(sL_Sql);	


			ResultSet L_ResultSet = Db2Stmt.executeQuery();


			if (L_ResultSet.next()) {
				sL_SeqVal = L_ResultSet.getString(1);

			}


			L_ResultSet.close();

			Db2Stmt.close();


		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("getNextSeqVal exception:" + e.getMessage());
			sL_SeqVal = "0";
		}
		return sL_SeqVal;

	}
	 */
	public static Timestamp getCurTimestamp() {
		java.sql.Timestamp  L_CurTimeStamp = new java.sql.Timestamp(new java.util.Date().getTime());

		//L_CurTimeStamp.toString() => 2018-01-04 09:27:43.245
		return L_CurTimeStamp;
	}
	public static String getNextMonthDate(String sP_SrcDate) {
		String sL_Result = "";
		try {
			String sL_YYYY = sP_SrcDate.substring(0,4);
			String sL_MM = sP_SrcDate.substring(4,6);
			String sL_DD = "01";

			if ((Integer.parseInt(sL_MM)+1)>12) {
				sL_MM = "01";

				sL_YYYY = Integer.toString(Integer.parseInt(sL_YYYY) + 1);

			}
			else {
				sL_MM = Integer.toString(Integer.parseInt(sL_MM)+1) ;
			}
			sL_Result = sL_YYYY + sL_MM + sL_DD;  
		} catch (Exception e) {
			// TODO: handle exception
			sL_Result = "";
		}

		return sL_Result;
	}
	public static String  transPasswd(int typeNum,String tranPWD) throws Exception
	{
		int[] addNum={7,34,295,4326,76325,875392,2468135,12357924,123456789};
		int  transInt,int1,int2,dataLen,dataInt=1;
		String[] fDig={"08122730435961748596","04112633405865798792","03162439425768718095",
				"04152236415768798390","09182035435266718497","01152930475463788296",
				"07192132465068748593","02172931455660788394"};
		String  tmpStr="",tmpStr1="",retStr="",fDigstr="";

		dataLen=tranPWD.length();
		if (typeNum==0)
		{
			for (int1=0;int1<dataLen;int1++) 
				tmpStr=tmpStr+fDigstr.substring(((int)tranPWD.charAt(int1)-48)*2+1,1);
			for (int1=0;int1<dataLen;int1++) dataInt = dataInt*10;
			tmpStr1=String.valueOf(dataInt + Integer.valueOf(tmpStr) - addNum[dataLen-1]);
			retStr =  tmpStr1.substring(tmpStr1.length()-1,dataLen);
			return retStr;
		}
		else
		{
			tmpStr1=String.valueOf(Integer.valueOf(tranPWD) + addNum[dataLen-1]);
			tmpStr = tmpStr1.substring(tmpStr1.length()-1,dataLen);
			for (int1=0;int1<dataLen;int1++)
			{
				fDigstr=fDig[int1];
				for (int2=0;int2<10;int2++)
					if (tmpStr.substring(int1-1,1)==fDigstr.substring(int2*2,1))
					{
						retStr=retStr+fDigstr.substring(int2*2-1,1);
						break;
					}
			}
			return retStr;
		}
	}

	public static String getLocalIpAddress() throws Exception{
		String sL_Ip = "";
		InetAddress addr = InetAddress.getLocalHost();
		sL_Ip = addr.getHostAddress();
		return sL_Ip;
	}

	public static String byte2ByteMap(String src, int size) {
		byte[] srcByte = new byte[65];
		String[] cvt = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
		String dest = "";
		int i = 0, ind = 0;
		srcByte = src.getBytes();

		for (i = 0; i < size; i++) {
			if (srcByte[i] >= '0' && srcByte[i] <= '9') {
				ind = (int) (srcByte[i] & 0x0F);
			} else
				if (srcByte[i] >= 'A' && srcByte[i] <= 'F') {
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
	public static String transNullValue(String sP_Src) {
		String sL_Result=sP_Src;
		if (null == sP_Src)
			sL_Result = "";

		return sL_Result;
	}

	public static String zero_pad_bin_char(String bin_char){
		int len = bin_char.length();
		if(len == 8) return bin_char;
		String zero_pad = "0";
		for(int i=1;i<8-len;i++) zero_pad = zero_pad + "0"; 
		return zero_pad + bin_char;
	}
	public static String convertBinaryStrToHexStr(String binary) {
		String hex = "";
		String hex_char;
		int len = binary.length()/8;
		for(int i=0;i<len;i++){
			String bin_char = binary.substring(8*i,8*i+8);
			int conv_int = Integer.parseInt(bin_char,2);
			hex_char = Integer.toHexString(conv_int);
			hex_char = fillZeroOnLeft(hex_char, 2);
			if(i==0) 
				hex = hex_char;
			else 
				hex = hex+hex_char;
		}
		return hex;
	}
	public static String convertHexStrToBinaryStr(String sP_SrcHexStr) {
		String hex_char,bin_char,binary;
		binary = "";
		int len = sP_SrcHexStr.length()/2;
		for(int i=0;i<len;i++){
			hex_char = sP_SrcHexStr.substring(2*i,2*i+2);
			int conv_int = Integer.parseInt(hex_char,16);
			bin_char = Integer.toBinaryString(conv_int);
			bin_char = zero_pad_bin_char(bin_char);
			if(i==0) binary = bin_char; 
			else binary = binary+bin_char;
			//out.printf("%s %s\n", hex_char,bin_char);
		}
		return binary;
	}
	public static String fillZeroOnLeft(String sP_Src, int nP_TargetLen) {
		String sL_Result=sP_Src;
		for(int i=sL_Result.length(); i<nP_TargetLen; i++) {
			sL_Result = "0" + sL_Result;
		}

		return  sL_Result;      



	}

	public static String fillZeroOnLeft(Double dP_Src, int nP_TargetLen) {
		//0 => ��ܭn��0
		//nP_TargetLen => ��̲ܳת���
		//d => ��ܬ������

		int nL_Src = Integer.valueOf(dP_Src.intValue());

		return  String.format("%0" + nP_TargetLen + "d", nL_Src);      



	}

	public static String fillCharOnRight(String sP_Src, int nP_TargetLen, String sL_TarcharChar) {
		//�b�r��k��ɤW�Y�r��    	
		int strLen = sP_Src.length();
		if (strLen < nP_TargetLen) {
			while (strLen < nP_TargetLen) {
				StringBuffer sb = new StringBuffer();
				//    	    	sb.append(sL_TarcharChar).append(sP_Src);//����" "
				sb.append(sP_Src).append(sL_TarcharChar);//�k��" "
				sP_Src = sb.toString();
				strLen = sP_Src.length();
			}
		}

		return sP_Src;       


	}

	public static String getTransKeyValue() throws Exception{

		return fillCharOnLeft(getCurDateTimeStr(true), 20, "0");
	}
	public static String fillCharOnLeft(String sP_Src, int nP_TargetLen, String sL_TarcharChar) {
		//�b�r�ꥪ��ɤW�Y�r��
		int strLen = sP_Src.length();
		if (strLen < nP_TargetLen) {
			while (strLen < nP_TargetLen) {
				StringBuffer sb = new StringBuffer();
				sb.append(sL_TarcharChar).append(sP_Src);//����" "
				//    			sb.append(sP_Src).append(sL_TarcharChar);//�k��" "
				sP_Src = sb.toString();
				strLen = sP_Src.length();
			}
		}

		return sP_Src;       


	}

	public static String bcd2Str(byte[] bytes) {  
		StringBuffer temp = new StringBuffer(bytes.length * 2);  
		for (int i = 0; i < bytes.length; i++) {  
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));  
			temp.append((byte) (bytes[i] & 0x0f));  
		}  
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp  
				.toString().substring(1) : temp.toString();  
	}  

	//public static byte[] str2Bcd(String asc) {  
	public static String str2Bcd(String asc) {
		int len = asc.length();  
		int mod = len % 2;  
		if (mod != 0) {  
			asc = "0" + asc;  
			len = asc.length();  
		}  
		byte abt[] = new byte[len];  
		if (len >= 2) {  
			len = len / 2;  
		}  
		byte bbt[] = new byte[len];  
		abt = asc.getBytes();  
		int j, k;  
		for (int p = 0; p < asc.length() / 2; p++) {  
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {  
				j = abt[2 * p] - '0';  
			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {  
				j = abt[2 * p] - 'a' + 0x0a;  
			} else {  
				j = abt[2 * p] - 'A' + 0x0a;  
			}  
			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {  
				k = abt[2 * p + 1] - '0';  
			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {  
				k = abt[2 * p + 1] - 'a' + 0x0a;  
			} else {  
				k = abt[2 * p + 1] - 'A' + 0x0a;  
			}  
			int a = (j << 4) + k;  
			byte b = (byte) a;  
			bbt[p] = b;  
		}  
		String sL_Result= "";
		try {
			//sL_Result = new String(bbt, "UTF-8");  // Best way to decode using "UTF-8"
			sL_Result = new String(bbt, Charset.forName("UTF-8"));  // Best way to decode using "UTF-8"
		}
		catch (Exception e) {
			sL_Result="";
		}
		return sL_Result;
		//return bbt;  
	}  
	public static boolean isAmount(String strTmp)
	{
		if (strTmp==null || strTmp.trim().length()==0)
			return false;

		//double lm_val=0;
		try {
			Double.parseDouble(strTmp);
		}
		catch(Exception ex)
		{
			return false;
		}

		return true;
	}
	public static boolean isNumberString(String strTmp) {
		for (char c : strTmp.toCharArray())
		{
			if (!Character.isDigit(c)) return false;
		}
		return true;
	}

	public static XMLGregorianCalendar getNow() throws DatatypeConfigurationException{
		XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
		Calendar date = Calendar.getInstance();
		cal.setYear(date.get(Calendar.YEAR));
		cal.setMonth(date.get(Calendar.MONTH)+1);
		cal.setDay(date.get(Calendar.DATE));
		cal.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), date.get(Calendar.SECOND));
		return cal;
	}

	public static int binaryToInt(String str){
		double j=0;
		for(int i=0;i<str.length();i++){
			if(str.charAt(i)== '1'){
				j=j+ Math.pow(2,str.length()-1-i);
			}

		}
		return (int) j;
	}
	public static String convertToBinary(String sP_Src){
		byte[] L_Ary = sP_Src.getBytes();

		String sL_Tmp ="", sL_Result="";
		int nL_Result1=0, nL_Result2=0;
		for(int i=0; i<L_Ary.length;i++){
			sL_Tmp = Integer.toBinaryString(0x100 + L_Ary[i]).substring(1);
			//System.out.println(sL_Tmp);


			nL_Result1 = binaryToInt(sL_Tmp.substring(0,4));

			nL_Result2 = binaryToInt(sL_Tmp.substring(4,8));


			sL_Result = sL_Result + Integer.toString(nL_Result1) + Integer.toString(nL_Result2);


			//System.out.println(Integer.toBinaryString(L_Ary[i]).substring(1));
		}
		return sL_Result;
	} 

	public static XMLGregorianCalendar getCurDate() throws DatatypeConfigurationException{
		XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
		Calendar date = Calendar.getInstance();
		cal.setYear(date.get(Calendar.YEAR));
		cal.setMonth(date.get(Calendar.MONTH)+1);
		cal.setDay(date.get(Calendar.DATE));

		//cal.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), date.get(Calendar.SECOND));
		return cal;
	}

	public static String getPriorNDayString(String sP_Sep, int nP_NDay) {
		//取得 N 天後的日期字串。N>0表示未來， N<0表示過去
		//假設今天是 2018/01/12, 則  getPriorNDayString("",3)，會傳回 "20180115"
		//假設今天是 2018/01/12, 則  getPriorNDayString("",-3)，會傳回 "20180109"

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, nP_NDay);

		Date L_Date = cal.getTime();


		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy" + sP_Sep + "MM" + sP_Sep + "dd");

		String sL_Result = sdFormat.format(L_Date);



		return sL_Result;
	}

	public static String getCurDateStr(String sP_Sep) throws DatatypeConfigurationException{
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy" + sP_Sep + "MM" + sP_Sep + "dd");
		Date date = new Date();
		String strDate = sdFormat.format(date);
		return strDate;

		/*
		Calendar date = Calendar.getInstance();
		String sL_Result = date.get(Calendar.YEAR) + sP_Sep + fillCharOnLeft(""+date.get(Calendar.MONTH)+1, 2, "0") + sP_Sep + fillCharOnLeft(""+date.get(Calendar.DATE), 2, "0"); 
		return sL_Result;
		 */
	}

	public static String getTaiwanDateStr(String sP_SrcString) throws DatatypeConfigurationException{
		//�� 20110909 �ഫ�� 1000909
		String sL_Result = sP_SrcString;
		if (sP_SrcString.length()>=8)
			sL_Result = Integer.parseInt(sP_SrcString.substring(0,4))-1911 + sP_SrcString.substring(4,8);

		return sL_Result;
	}

	public static boolean isFirstCharLatter(String sP_Source) {

		boolean bL_Result = false;
		char ch=sP_Source.charAt(0);
		if(ch<='z'&&ch>='a'||ch<='Z'&&ch>='A'){
			bL_Result=true;
		}
		return bL_Result;


	}

	public static int compareDateString(String sL_Date1, String sL_Date2) {
		int nL_Result = 0;
		try {
			Date dL_Date1 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(sL_Date1);
			Date dL_Date2 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(sL_Date2);

			//System.out.println(dL_Date1);
			//System.out.println(dL_Date2);

			if (dL_Date1.compareTo(dL_Date2) > 0) {
				nL_Result=1;
			} else if (dL_Date1.compareTo(dL_Date2) < 0) {
				nL_Result=-1;
			} else if (dL_Date1.compareTo(dL_Date2) == 0) {
				nL_Result=0;
			} else {
				nL_Result=-1;
				//System.out.println("Something weird happened...");
			}

		} catch (Exception e) {
			//e.printStackTrace();
			nL_Result=-1;
		}

		return nL_Result;
	}

	public static int compareDateDiffOfDay(String sL_Date1, String sL_Date2) {
		//�p���Ӥ���t���X�� => sL_Date2 - sL_Date1  �ݬݬ۹j�X��  
		int nL_Result = 0;
		try {
			Date dL_Date1 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(sL_Date1);
			Date dL_Date2 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(sL_Date2);

			long L_BetweenDate = (dL_Date2.getTime() - dL_Date1.getTime())/(1000*60*60*24);

			nL_Result = Integer.parseInt(Long.toString(L_BetweenDate));



		} catch (Exception e) {
			//e.printStackTrace();
			nL_Result=-1;
		}

		return nL_Result;
	}

	public static int getRandomNumber(int nL_MaxNum) 
	{
		int nL_Result = 0;

		SecureRandom L_RandomGen = new SecureRandom();

		nL_Result = L_RandomGen.nextInt(nL_MaxNum);
		L_RandomGen = null;

		return nL_Result;
	}

	public static String getCurHourAndMinStr(String sP_Sep) throws DatatypeConfigurationException{

		Calendar date = Calendar.getInstance();

		String sL_Hour = String.format("%1$tH", date);
		String sL_Min = String.format("%1$tM", date);
		String sL_Result = sL_Hour + sP_Sep + sL_Min ; 
		return sL_Result;
	}

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
		//return data.getBytes("Cp500");

		/*
		byte[] b = new byte[str.length() / 8];
		int count = 0;

		for (int i = 0; i < b.length; i++) {
			b[i] = Byte.parseByte(str.substring(count, count + 8), 2);
			count += 8;
		}
		// Integer.parseInt(c, 2)

		return b;
		 */

	}





	public static byte[] strToByteAry(String sP_Src) throws UnsupportedEncodingException{


		byte[] b = new byte[sP_Src.length() / 8];
		int count = 0;

		for (int i = 0; i < b.length; i++) {
			b[i] = Byte.parseByte(sP_Src.substring(count, count + 8), 2);
			count += 8;
		}
		// Integer.parseInt(c, 2)

		return b;





	}
	public static String readDataFromEasyCard(BufferedInputStream P_EasyCardBufferedInputStream, BufferedReader P_BufferReader) throws Exception{
		int headLen=0,packetLen=0,inputLen=0;
		byte[]  authData = new byte[2048];
		byte[]  lenData  = new byte[3];
		String sL_IsoStrFromEasyCard ="";

		try {
			BufferedReader L_EasyCardBufferedReader = new BufferedReader(new InputStreamReader(P_EasyCardBufferedInputStream));    	
			if (1==1) {
				//BufferedInputStream G_SocketReader = new BufferedInputStream(P_Socket.getInputStream());


				//while (true) {
				headLen =  P_EasyCardBufferedInputStream.read(lenData, 0, 2);
				if ( headLen != 2 ) {
					return "";
				}  
				// �q SOCKET Ū�������� 
				packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);

				inputLen  = P_EasyCardBufferedInputStream.read(authData, 0, packetLen);

				sL_IsoStrFromEasyCard    = new String(authData,0,inputLen);

				//System.out.print("readDataFromEasyCard =>" + sL_IsoStrFromEasyCard + "===");


				//break;
				//}
			}
			else {

				//BufferedReader in = new BufferedReader(new InputStreamReader(P_Socket.getInputStream()));
				//BufferedReader in = P_BufferReader;

				while ((sL_IsoStrFromEasyCard = L_EasyCardBufferedReader.readLine()) != null) {

					break;
				}

				//System.out.print("readDataFromEasyCard :" + sL_IsoStrFromEasyCard + "===");

			}
		}//end try
		catch (Exception e) {
			//System.out.println("readDataFromEasyCard() error=> " + e.getMessage());
		}
		return sL_IsoStrFromEasyCard;
	}

	public static String readDataFromEcs(BufferedInputStream P_EcsBufferedInputStream) throws Exception{
		int headLen=0,packetLen=0,inputLen=0;
		byte[]  authData = new byte[2048];
		byte[]  lenData  = new byte[3];
		String sL_IsoStrFromEcs ="";

		try {

			if (1==1) {
				//BufferedInputStream G_SocketReader = new BufferedInputStream(P_Socket.getInputStream());


				//while (true) {
				headLen =  P_EcsBufferedInputStream.read(lenData, 0, 2);
				if ( headLen != 2 ) {
					return "";
				}  
				// �q SOCKET Ū�������� 
				packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);

				inputLen  = P_EcsBufferedInputStream.read(authData, 0, packetLen);

				sL_IsoStrFromEcs    = new String(authData,0,inputLen);

				//System.out.print("readDataFromEcs :" + sL_IsoStrFromEcs + "===");


				//break;
				//}
			}
			else {

			}
		}//end try
		catch (Exception e) {
			//System.out.println("readDataFromEcs() error=> " + e.getMessage());
		}
		return sL_IsoStrFromEcs;
	}

	/*
    public static void writeData2EasyCard(Socket P_Socket, String sP_Data) throws Exception{


    	BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());
    	BufferedOutputStream L_SocketWriter = new BufferedOutputStream(P_Socket.getOutputStream());



    	if (1==1) {
        	PrintWriter L_PrintWriter = new PrintWriter(P_Socket.getOutputStream());
    		L_PrintWriter.println("****" + sP_Data + "================");
    		L_PrintWriter.flush();
    	}
    	else {
    		L_SocketWriter.write(sP_Data.getBytes(), 0, sP_Data.getBytes().length);

    		L_SocketWriter.flush();
    	}

    }
	 */
	public static String exchangeDataWithEasyCard(BufferedOutputStream  P_EasyCardBufferedOutputStream,BufferedInputStream  P_EasyCardBufferedInputStream,  String sP_Data) throws Exception{
		String sL_EasyCardResponseData="";

		//BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());




		if (1==2) { 
			/*
        	PrintWriter L_PrintWriter = new PrintWriter(P_EasyCardBufferedOutputStream);
    		L_PrintWriter.println("****" + sP_Data + "================");
    		L_PrintWriter.flush();
			*/
		}
		else {

			writeData2EasyCard(P_EasyCardBufferedOutputStream, sP_Data);
		}
		sL_EasyCardResponseData = readDataFromEasyCard(P_EasyCardBufferedInputStream, null);

		return sL_EasyCardResponseData;
	}
	public static String getMonthEndDate(String sP_Year, String sP_Month) {
		//傳入 (2016,12) => 回傳 20161231
		//傳入 (2016,2) => 回傳 20160229
		String sL_Result = "";
		try {
			YearMonth yearMonth = YearMonth.of( Integer.parseInt(sP_Year), Integer.parseInt(sP_Month));
			LocalDate endOfMonth = yearMonth.atEndOfMonth();
			//DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate( FormatStyle.SHORT );
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			formatter = formatter.withLocale( Locale.US );  // Re-assign JVM’s current default Locale that was implicitly applied to the formatter.
			sL_Result = endOfMonth.format( formatter );

			//System.out.println(sL_Result+"--");

		} catch (Exception e) {
			// TODO: handle exception
			sL_Result = "";
		}
		return sL_Result;
	}

	private static String interpretHostResponseData(byte[] response) throws UnsupportedEncodingException, IOException {

		String EBSS; // X(1)
		String TSQ_Name; // X(16)
		String Filler; // X(20
		String HttpReturnCode = ""; // X(2)
		String result = "";
		int iPos = 0;
		StringBuilder receiveLog = new StringBuilder("");

		try {

			EBSS = getHostReturnFiled(response, iPos, 1); // 0~1
			iPos = iPos + 1;
			receiveLog.append("clsCWSServerPtl::EBSS::" + EBSS + "\r\n");

			TSQ_Name = getHostReturnFiled(response, iPos, 16); // 1~16
			iPos = iPos + 16;
			receiveLog.append("clsCWSServerPtl::TSQ_Name::" + TSQ_Name + "\r\n");

			Filler = getHostReturnFiled(response, iPos, 20); // 16~36
			iPos = iPos + 20;
			receiveLog.append("clsCWSServerPtl::Filler::" + Filler + "\r\n");

			HttpReturnCode = getHostReturnFiled(response, iPos, 2); // 36~38
			iPos = iPos + 2;
			receiveLog.append("clsCWSServerPtl::HttpReturnCode::" + HttpReturnCode + "\r\n");

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
			//logger.error(this.getClass().getName() + "_interpretHostResponseData() " + e.getClass().getName() + " : " + e.getMessage());
			//notifyMonitor.sendMonitor(e.getClass().getName() + " : " + e.getMessage(), this.getClass().getName() + "_interpretHostResponseData()");
			//System.out.println("interpretHostResponseData error 1=>" + e.getMessage());
			throw e;
		} catch (IOException e) {
			//logger.error(this.getClass().getName() + "_interpretHostResponseData() " + e.getClass().getName() + " : " + e.getMessage());
			//notifyMonitor.sendMonitor(e.getClass().getName() + " : " + e.getMessage(), this.getClass().getName() + "_interpretHostResponseData()");
			//System.out.println("interpretHostResponseData error 2=>" + e.getMessage());
			throw e;
		}

		return HttpReturnCode + result;
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

	private static String getHostReturnFiled(byte[] hostReturnData, int start, int dataLen) throws UnsupportedEncodingException, IOException {

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

	public static String exchangeDataWithEcs(BufferedOutputStream  P_EcsBufferedOutputStream,BufferedInputStream  P_EasyCardBufferedInputStream,  String sP_Data) throws Exception{
		String sL_EcsResponseData="";

		//BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());


		try {

			//P_EasyCardBufferedOutputStream.write(sP_Data.getBytes(), 0, sP_Data.getBytes().length);

			byte[] L_Ary = HpeUtil.genIsoByteAry(sP_Data);
			P_EcsBufferedOutputStream.write(L_Ary, 0, L_Ary.length);

			P_EcsBufferedOutputStream.flush();


			sL_EcsResponseData = readDataFromEcs(P_EasyCardBufferedInputStream);
		}
		catch(Exception e) {
			//System.out.println("exception on exchangeDataWithEcs() =>" + e.getMessage());
		}
		return sL_EcsResponseData;
	}

	public static byte[] genIsoByteAry(String sP_IsoData) {
		byte[] L_EntireAry = null;
		try {
			L_EntireAry     = ("00" + sP_IsoData).getBytes();

			//System.out.println("---IsoStr is=>"+ gate.isoString + "----");
			int nL_TotalLen    = L_EntireAry.length;
			int nL_DataLen     = nL_TotalLen - 2;
			L_EntireAry[0]  = (byte)(nL_DataLen / 256);
			L_EntireAry[1]  = (byte)(nL_DataLen % 256);

			/*
    		byte[] L_LenAry = new byte[2];
    		L_LenAry[0]  = (byte)(sP_IsoData.length() / 256);
    		L_LenAry[1]  = (byte)(sP_IsoData.length() % 256);


    		List<byte[]> ByteAryList = new ArrayList<byte[]>();
    		ByteAryList.add(L_LenAry);

    		byte[] L_DataAry = strToByteAry(sP_IsoData);

    		ByteAryList.add(L_DataAry);
    		L_EntireAry = convertByteAryArrayList2ByteAry(ByteAryList);
			 */

		}
		catch (Exception e) {

		}
		return L_EntireAry;
	}
	public static void writeData2EasyCard(BufferedOutputStream  P_EasyCardBufferedOutputStream, String sP_Data) throws Exception{
		if ("".equals(sP_Data))
			return;

		byte[] L_Ary = HpeUtil.genIsoByteAry(sP_Data);
		P_EasyCardBufferedOutputStream.write(L_Ary, 0, L_Ary.length);

		P_EasyCardBufferedOutputStream.flush();
		/*
    	//BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());
    	BufferedOutputStream L_SocketWriter = P_EasyCardBufferedOutputStream;



    	if (1==1) { //���ծɡA�Ȧ�post�L�Ӫ���ơA�n�o�ˤ~�i�H�e��easycard
        	PrintWriter L_PrintWriter = new PrintWriter(P_EasyCardBufferedOutputStream);
    		L_PrintWriter.println("****" + sP_Data + "================");
    		L_PrintWriter.flush();
    	}
    	else {
    		L_SocketWriter.write(sP_Data.getBytes(), 0, sP_Data.getBytes().length);

    		L_SocketWriter.flush();
    	}
		 */
	}

	public static String genBitMap(List P_DataFieldList) {
		String sL_Bitmap="", sL_FirstByteValue="0";
		for(int i=2; i<=128; i++) {
			if (P_DataFieldList.indexOf(i) >=0) {
				sL_Bitmap +="1";
				if (i>64)
					sL_FirstByteValue="1";
			}
			else
				sL_Bitmap +="0";
		}

		sL_Bitmap = sL_FirstByteValue + sL_Bitmap;

		String sL_Bitmap1 = sL_Bitmap.substring(0, 64);
		String sL_Bitmap2 = sL_Bitmap.substring(64, 128);

		String sL_HexBitmap = convertBinaryStrToHexStr(sL_Bitmap1) + convertBinaryStrToHexStr(sL_Bitmap2);

		return sL_HexBitmap;

	}


	public static void writeData2ECS(BufferedOutputStream  P_BufferedOutputStream, String sP_Data) throws Exception{


		//BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());




		if (1==2) {
			/*
        	PrintWriter L_PrintWriter = new PrintWriter(P_BufferedOutputStream);
    		L_PrintWriter.println("****" + sP_Data + "================");
    		L_PrintWriter.flush();
			 */
		}
		else {
			byte[] L_Ary = genIsoByteAry(sP_Data);
			byte[]  lenData  = new byte[3];
			byte[]  authData = new byte[2048];

			P_BufferedOutputStream.write(L_Ary, 0, L_Ary.length);
			P_BufferedOutputStream.flush();


		}


	}

	public static String getCurTimeStr() throws DatatypeConfigurationException{

		//SimpleDateFormat L_SDF = new SimpleDateFormat("hhmmssSSS");
		SimpleDateFormat L_SDF = new SimpleDateFormat("HHmmss");

		String sL_Result = L_SDF.format(new Date());

		return sL_Result;
	}

	public static String getByteHex(byte[] inputByte){
		StringBuilder str=new StringBuilder();
		for(byte byte1:inputByte){
			str.append(toHex(byte1));
		}
		return str.toString();
	} 

	public static String toHex(byte b){
		return (""+"0123456789ABCDEF".charAt(0xf&b>>4)+"0123456789ABCDEF".charAt(b&0xf));
	}
	public static byte[] transToEBCDIC(String sP_Src) throws UnsupportedEncodingException {
		/*
		String data;

		if (this._data.length() > _length)
			data = this._data.substring(0, _length);
		else
			data = pad(this._data, _length, ' ');


		clsTools.recordSendLog(data);
		 */
		return sP_Src.getBytes("Cp500");
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
		//System.out.println("result of stringToBytes()=>" + b.toString() + "---" );
		return b;
	}


	public static String getCurDateTimeStr(boolean bP_IncludeMSec) throws DatatypeConfigurationException{

		SimpleDateFormat L_SDF = null;
		if (bP_IncludeMSec)
			L_SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		else
			L_SDF = new SimpleDateFormat("yyyyMMddHHmmss");

		String sL_Result = L_SDF.format(new Date());

		return sL_Result;
	}

}
