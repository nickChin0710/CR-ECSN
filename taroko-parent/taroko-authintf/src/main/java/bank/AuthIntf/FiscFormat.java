package bank.AuthIntf;


import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.logging.log4j.core.Logger;

import bank.Auth.HpeUtil;

public class FiscFormat extends ConvertMessage implements FormatInterChange {

public  String   byteMap="",retCode="",rejectMesg="",errFlag="";
public  String   zeros="",spaces="";

public  int      offset=0,k=0;


public  FiscFormat(Logger logger,AuthGate gate,HashMap cvtHash)
{
super.logger  = logger;
super.gate    = gate;
super.cvtHash = cvtHash;
}

/* �N FISC ISO8583 �榡�ର�D���榡��� */
public boolean iso2Host()
{
String  cvtStr="";
int     cnt =0;

try
{

StringBuffer cvtZeros = new StringBuffer();
StringBuffer cvtSpace = new StringBuffer();
for( int i=0; i<30; i++ )
   {
     cvtZeros.append("0000000000");
     cvtSpace.append("          ");
   }
zeros    = cvtZeros.toString();
spaces   = cvtSpace.toString();
cvtZeros = null;
cvtSpace = null;

offset   = 0;

//gate.mesgType = hostFixAns(4);
gate.mesgType = getIsoFixLenStrToHost(4, true);
byteMap       = bitMapToByteMap(8);

if ( byteMap.charAt(1-1)  == '1' )
   {
     byteMap = byteMap + bitMapToByteMap(8);
     cnt = 128;
   }
 else
   { cnt = 64; }

 for( k=2; k<=cnt; k++ )
    {
      if ( byteMap.charAt(k-1) == '1' )
         {
           switch ( k )
            {
            case  2 : gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case  3 : gate.isoField[k] = getIsoFixLenStrToHost(6, true);      break;
		    case  4 : gate.isoField[k] = getIsoFixLenStrToHost(12, true);     break;
		    case  5 : gate.isoField[k] = getIsoFixLenStrToHost(12, true);     break;
		    case  6 : gate.isoField[k] = getIsoFixLenStrToHost(12, true);     break;
		    case  7 : gate.isoField[k] = getIsoFixLenStrToHost(10, true);     break;
		    case  8 : gate.isoField[k] = getIsoFixLenStrToHost(8, true);      break;
		    case  9 : gate.isoField[k] = getIsoFixLenStrToHost(8, true);      break;
		    case  10: gate.isoField[k] = getIsoFixLenStrToHost(8, true);      break;
		    case  11: gate.isoField[k] = getIsoFixLenStrToHost(6, true);      break;
		    case  12: gate.isoField[k] = getIsoFixLenStrToHost(6, true);      break;
		    case  13: gate.isoField[k] = getIsoFixLenStrToHost(4, true);      break;
		    case  14: gate.isoField[k] = getIsoFixLenStrToHost(4, true);      break;
		    case  15: gate.isoField[k] = getIsoFixLenStrToHost(4, true);      break;
		    case  16: gate.isoField[k] = getIsoFixLenStrToHost(4, true);      break;
		    case  17: gate.isoField[k] = getIsoFixLenStrToHost(4, true);      break;
		    case  18: gate.isoField[k] = getIsoFixLenStrToHost(4, true);      break;
		    case  19: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  20: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  21: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  22: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  23: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  24: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  25: gate.isoField[k] = getIsoFixLenStrToHost(2, true);      break;
		    case  26: gate.isoField[k] = getIsoFixLenStrToHost(2, true);      break;
		    case  27: gate.isoField[k] = getIsoFixLenStrToHost(1, true);      break;
		    case  28: gate.isoField[k] = getIsoFixLenStrToHost(9, true);      break;
		    case  29: gate.isoField[k] = getIsoFixLenStrToHost(9, true);      break;
		    case  30: gate.isoField[k] = getIsoFixLenStrToHost(9, true);      break;
		    case  31: gate.isoField[k] = getIsoFixLenStrToHost(9, true);      break;
		    case  32: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case  33: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case  34: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case  35: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case  36: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  37: gate.isoField[k] = getIsoFixLenStrToHost(12, true);     break;
		    case  38: gate.isoField[k] = getIsoFixLenStrToHost(6, true);      break;
		    case  39: gate.isoField[k] = getIsoFixLenStrToHost(2, true);      break;
		    case  40: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  41: gate.isoField[k] = getIsoFixLenStrToHost(8, true);      break;
		    case  42: gate.isoField[k] = getIsoFixLenStrToHost(15, true);     break;
		    case  43: gate.isoField[k] = getIsoFixLenStrToHost(40, true);     break;
		    case  44: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case  45: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case  46: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  47: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  48: hostVarF48(3, true);                   break;
		    case  49: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  50: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  51: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  52: gate.isoField[k] = hostFixBcd(8);      break;
		    case  53: gate.isoField[k] = getIsoFixLenStrToHost(8, true);      break;
		    case  54: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  55: hostVarF55(3, true);                   break;
		    case  56: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  57: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  58: hostVarF58(3, true);                   break;
		    case  59: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  60: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  61: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  62: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  63: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case  64: gate.isoField[k] = hostFixBcd(8);      break;
		    case  65: gate.isoField[k] = hostFixBcd(8);      break;
		    case  66: gate.isoField[k] = getIsoFixLenStrToHost(1, true);      break;
		    case  67: gate.isoField[k] = getIsoFixLenStrToHost(2, true);      break;
		    case  68: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  69: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  70: gate.isoField[k] = getIsoFixLenStrToHost(3, true);      break;
		    case  71: gate.isoField[k] = getIsoFixLenStrToHost(4, true);      break;
		    case  72: gate.isoField[k] = getIsoFixLenStrToHost(4, true);      break;
		    case  73: gate.isoField[k] = getIsoFixLenStrToHost(6, true);      break;
		    case  74: gate.isoField[k] = getIsoFixLenStrToHost(10, true);     break;
		    case  75: gate.isoField[k] = getIsoFixLenStrToHost(10, true);     break;
		    case  76: gate.isoField[k] = getIsoFixLenStrToHost(10, true);     break;
		    case  77: gate.isoField[k] = getIsoFixLenStrToHost(10, true);     break;
		    case  78: gate.isoField[k] = getIsoFixLenStrToHost(10, true);     break;
		    case  79: gate.isoField[k] = getIsoFixLenStrToHost(10, true);     break;
		    case  80: gate.isoField[k] = getIsoFixLenStrToHost(10, true);     break;
		    case  81: gate.isoField[k] = getIsoFixLenStrToHost(10, true);     break;
		    case  82: gate.isoField[k] = getIsoFixLenStrToHost(12, true);     break;
		    case  83: gate.isoField[k] = getIsoFixLenStrToHost(12, true);     break;
		    case  84: gate.isoField[k] = getIsoFixLenStrToHost(12, true);     break;
		    case  85: gate.isoField[k] = getIsoFixLenStrToHost(12, true);     break;
		    case  86: gate.isoField[k] = getIsoFixLenStrToHost(16, true);     break;
		    case  87: gate.isoField[k] = getIsoFixLenStrToHost(16, true);     break;
		    case  88: gate.isoField[k] = getIsoFixLenStrToHost(16, true);     break;
		    case  89: gate.isoField[k] = getIsoFixLenStrToHost(16, true);     break;
		    case  90: gate.isoField[k] = getIsoFixLenStrToHost(42, true);     break;
		    case  91: gate.isoField[k] = getIsoFixLenStrToHost(1, true);      break;
		    case  92: gate.isoField[k] = getIsoFixLenStrToHost(2, true);      break;
		    case  93: gate.isoField[k] = getIsoFixLenStrToHost(5, true);      break;
		    case  94: gate.isoField[k] = getIsoFixLenStrToHost(7, true);      break;
		    case  95: gate.isoField[k] = getIsoFixLenStrToHost(42, true);     break;
		    case  96: gate.isoField[k] = hostFixBcd(8);      break;
		    case  97: gate.isoField[k] = getIsoFixLenStrToHost(17, true);     break;
		    case  98: gate.isoField[k] = getIsoFixLenStrToHost(25, true);     break;
		    case  99: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case 100: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case 101: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case 102: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case 103: gate.isoField[k] = getIsoVarLenStrToHost(2, true);      break;
		    case 104: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 105: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 112: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 113: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 114: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 115: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 116: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 117: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 118: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 119: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 120: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 121: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 122: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 123: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 124: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 125: gate.isoField[k] = hostFixBcd(8);      break;
		    case 126: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 127: gate.isoField[k] = getIsoVarLenStrToHost(3, true);      break;
		    case 128: gate.isoField[k] = hostFixBcd(8);      break;
		    default : break;
           }
        }
    }

 /* FISC �榡 �ഫ�� �@�P�榡  */
//kevin:remove by fail
// if ( !convertToCommon() )
//    { return false; }

} // end of try

catch ( Exception ex )
     { expHandle(ex); return false; }
return true;

} // end of iso2Host

private String bitMapToByteMap(int size)
{
String cvtMap="",tmp="",zeros="00000000";
int    i=0,cvt=0;
for ( i=0;i<size;i++ )
    {
      cvt = ( gate.isoData[offset] & 0xFF);
      tmp=Integer.toBinaryString(cvt);
      if ( tmp.length() < 8 )
         { tmp = zeros.substring(0,8-tmp.length()) + tmp; }
      cvtMap  = cvtMap + tmp;
      offset++;
    }
return cvtMap;
}

//kevin:針對fisc客製化取欄位
private String hostVarAns(int size, int subOffset, String fieldData)
{
String subFieldData="";
int    fieldLen=0;
System.out.println("hostVarAns-size="+ size);
System.out.println("hostVarAns-subOffset="+ subOffset);
System.out.println("hostVarAns-fieldData="+ fieldData);
fieldLen  = Integer.parseInt(fieldData.substring(subOffset,subOffset+size));
System.out.println("hostVarAns-fieldLen="+ fieldLen);
subOffset   += size;
subFieldData = fieldData.substring(subOffset,subOffset+fieldLen);

subOffset += fieldLen;
return subFieldData;
}
//private String hostVarAns(int size)
//{
//String fieldData="";
//int    fieldLen=0;
//
//fieldLen  = Integer.parseInt(new String(gate.isoData,offset,size));
//offset   += size;
//fieldData = new String(gate.isoData,offset,fieldLen);
//
//offset += fieldLen;
//return fieldData;
//}

private String hostFixAns(int Len)
{
String fieldData="";
fieldData  = new String(gate.isoData,offset,Len);
offset    += Len;
return fieldData;
}

private String hostFixBcd(int size)
{
String dest="",lByte="",rByte="";
int    i=0,cvt=0,left=0,right=0;

for ( i=0;i<size;i++ )
    {
      cvt = ( gate.isoData[offset] & 0xFF );
      lByte = Integer.toHexString(cvt/16);
      lByte = lByte.toUpperCase();
      rByte = Integer.toHexString(cvt%16);
      rByte = rByte.toUpperCase();
      dest = dest + lByte + rByte;
      offset++;
    }

return dest;
}
//kevin:取iso變動長欄位並轉碼
private String getIsoVarLenStrToHost(int len, boolean bP_IsEbcdic) throws Exception{
	String lenData = "", fieldData = "";
	int fieldLen = 0;

	byte[] L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, len);

	if (bP_IsEbcdic)
		lenData = HpeUtil.ebcdic2Str(L_TmpAry);
	else
		lenData = new String(L_TmpAry, 0, L_TmpAry.length);
	//lenData = isoStringOfFisc.substring(offset, offset + len);
	fieldLen = Integer.parseInt(lenData);
	offset += len;

	L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, fieldLen);

	if (bP_IsEbcdic)
		fieldData = HpeUtil.ebcdic2Str(L_TmpAry);
	else
		fieldData = new String(L_TmpAry, 0, L_TmpAry.length);

	//fieldData = isoStringOfFisc.substring(offset, offset + fieldLen);
	offset += fieldLen;
	return fieldData;
}
//kevin:取iso固定長欄位並轉碼
private String getIsoFixLenStrToHost(int len, boolean bP_IsEbcdic) throws Exception{
	String fieldData = "";

	byte[] L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, len);
	if (bP_IsEbcdic)
		fieldData = HpeUtil.ebcdic2Str(L_TmpAry);
	else
		fieldData = new String(L_TmpAry, 0, L_TmpAry.length);


	//fieldData = isoStringOfFisc.substring(offset, offset + len);
	offset += len;
	return fieldData;
}

private void hostVarF48(int size, boolean bP_IsEbcdic) throws UnsupportedEncodingException
{
String fieldData="",checkCode1="",checkCode2="",unUseData="",lenData="";
int    fieldLen=0 ,subOffset=0;

byte[] L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, size);

if (bP_IsEbcdic)
	lenData = HpeUtil.ebcdic2Str(L_TmpAry);
else
	lenData = new String(L_TmpAry, 0, L_TmpAry.length);
//lenData = isoStringOfFisc.substring(offset, offset + len);
fieldLen = Integer.parseInt(lenData);
offset += size;

L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, fieldLen);

if (bP_IsEbcdic)
	fieldData = HpeUtil.ebcdic2Str(L_TmpAry);
else
	fieldData = new String(L_TmpAry, 0, L_TmpAry.length);

int checkPnt = offset+fieldLen;
checkCode1   = fieldData.substring(subOffset,subOffset+1);

if (
	     checkCode1.equals("A") ||
	     checkCode1.equals("C") ||
	     checkCode1.equals("F") ||
	     checkCode1.equals("H") ||
	     checkCode1.equals("O") ||
	     checkCode1.equals("P") ||
	     checkCode1.equals("R") ||
	     checkCode1.equals("T") ||
	     checkCode1.equals("U") ||
	     checkCode1.equals("X") ||
	     checkCode1.equals("Z")
	   )
	   { offset++; gate.tccCode = checkCode1; subOffset++; }

while ( offset < checkPnt )
{
  checkCode2 = fieldData.substring(subOffset,subOffset+2);
  System.out.println("hostVarF48-checkCode2=" + checkCode2);
  offset +=2; subOffset +=2;
  if ( checkCode2.equals("11") )
     { gate.keyExchangeBlock = hostVarAns(2, subOffset, fieldData);  
       subOffset += gate.keyExchangeBlock.length()+2;
       offset += gate.keyExchangeBlock.length()+2;}
  else
  if ( checkCode2.equals("42") )
//     { gate.ucafInd = hostVarAns(2, subOffset, fieldData).substring(6);\\kevin:整個tag都收進來再處理，避免欄位算錯
  	 { gate.ucafInd = hostVarAns(2, subOffset, fieldData);
       subOffset += gate.ucafInd.length()+2; 
       offset += gate.ucafInd.length()+2;}
  else
  if ( checkCode2.equals("43") )
     { gate.ucaf = hostVarAns(2, subOffset, fieldData); 
       subOffset += gate.ucaf.length()+2;
       offset += gate.ucaf.length()+2;}
  else
  if ( checkCode2.equals("44") )
     { gate.xid  = hostVarAns(2, subOffset, fieldData); 
       subOffset += gate.xid.length()+2; 
       offset += gate.xid.length()+2;}
  else
  if ( checkCode2.equals("61") )
     { gate.posConditionCode  = hostVarAns(2, subOffset, fieldData); 
       subOffset += gate.posConditionCode.length()+2; 
       offset += gate.posConditionCode.length()+2;}
  else	  
  if ( checkCode2.equals("92") )
     { gate.cvv2 = hostVarAns(2, subOffset, fieldData);
       subOffset += gate.cvv2.length()+2; 
       offset += gate.cvv2.length()+2;}
  else
     { unUseData = hostVarAns(2, subOffset, fieldData); 
       System.out.println("hostVarF48-unUseData="+unUseData+"len="+subOffset);
       subOffset += unUseData.length()+2; 
       offset += unUseData.length()+2;
       System.out.println("hostVarF48-len="+subOffset);}
}
offset = checkPnt;
return;
}

//fieldLen     = Integer.parseInt(new String(gate.isoData,offset,size));
//offset      += size;
//fieldData    = new String(gate.isoData,offset,fieldLen);
//int checkPnt = offset+fieldLen;
//checkCode1   = new String(gate.isoData,offset,1);

//if (
//     checkCode1.equals("A") ||
//     checkCode1.equals("C") ||
//     checkCode1.equals("F") ||
//     checkCode1.equals("H") ||
//     checkCode1.equals("O") ||
//     checkCode1.equals("P") ||
//     checkCode1.equals("R") ||
//     checkCode1.equals("T") ||
//     checkCode1.equals("U") ||
//     checkCode1.equals("X") ||
//     checkCode1.equals("Z")
//   )
//   { offset++; gate.tccCode = checkCode1; }

//while ( offset < checkPnt )
// {
//   checkCode2 = new String(gate.isoData,offset,2);
//   offset +=2;
//   if ( checkCode2.equals("11") )
//      { gate.keyExchangeBlock = hostVarAns(2);     }
//   else
//   if ( checkCode2.equals("42") )
//      { gate.ucafInd = hostVarAns(2).substring(6); }
//   else
//   if ( checkCode2.equals("43") )
//      { gate.ucaf = hostVarAns(2); }
//   else
//   if ( checkCode2.equals("44") )
//      { gate.xid  = hostVarBcd(2); }
//   else
//   if ( checkCode2.equals("92") )
//      { gate.cvv2 = hostVarAns(2); }
//   else
//      { unUseData = hostVarAns(2); }
// }
//
//offset = checkPnt;
//return;
//}

private String hostVarBcd(int size)
{
String fieldData="";
int    fieldLen=0;

fieldLen  = Integer.parseInt(new String(gate.isoData,offset,size));
offset   += size;

fieldData = hostFixBcd(fieldLen);
return fieldData;
}

private void hostVarF55(int size, boolean bP_IsEbcdic) throws Exception {

	String lenData = "", fieldData = "";
	int fieldLen = 0;

	byte[] L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, size);

	if (bP_IsEbcdic)
		lenData = HpeUtil.ebcdic2Str(L_TmpAry);
	else
		lenData = new String(L_TmpAry, 0, L_TmpAry.length);
	fieldLen = Integer.parseInt(lenData);
	offset += size;	

	fieldData = new String(gate.isoData,offset,fieldLen);

	gate.emvTrans = true;
	int checkPnt  = offset+fieldLen;
	while ( offset < checkPnt )
	{
		if ( gate.isoData[offset] == (byte)0x57 )
			{ offset++; gate.emv57 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x5A )
			{ offset++; gate.emv5A = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x5F && gate.isoData[offset+1] == (byte)0x24)
			{ offset+=2; gate.emv5F24 = hostVarBinBcd(1);  }
		else
		if ( gate.isoData[offset] == (byte)0x5F && gate.isoData[offset+1] == (byte)0x2A)
			{ offset+=2;  gate.emv5F2A = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x5F && gate.isoData[offset+1] == (byte)0x34)
			{ offset+=2; gate.emv5F34 = hostVarBinBcd(1);  }
		else
		if ( gate.isoData[offset] == (byte)0x71 )
			{ offset++; gate.emv71 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x72 )
			{ offset++; gate.emv72 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x82 )
			{ offset++; gate.emv82 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x84 )
			{ offset++; gate.emv84 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x8A )
			{ offset++; gate.emv8A = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x91 )
			{ offset++; gate.emv91 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x95 )
			{ offset++; gate.emv95 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9A )
			{ offset++; gate.emv9A = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9B )
			{ offset++; gate.emv9B = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9C )
			{ offset++; gate.emv9C = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x02)
			{ offset+=2; gate.emv9F02 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x03)
			{ offset+=2; gate.emv9F03 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x09)
			{ offset+=2; gate.emv9F09 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x10)
			{ offset+=2; gate.emv9F10 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x1A)
			{ offset+=2; gate.emv9F1A = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x1E)
			{ offset+=2; gate.emv9F1E = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x26)
			{ offset+=2; gate.emv9F26 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x27)
			{ offset+=2; gate.emv9F27 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x33)
			{ offset+=2; gate.emv9F33 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x34)
			{ offset+=2; gate.emv9F34 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x35)
			{ offset+=2; gate.emv9F35 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x36)
			{ offset+=2; gate.emv9F36 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x37)
			{ offset+=2; gate.emv9F37 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x41)
			{ offset+=2; gate.emv9F41 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x53)
			{ offset+=2; gate.emv9F53 = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0x9F && gate.isoData[offset+1] == (byte)0x5B)
			{ offset+=2; gate.emv9F5B = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0xDF && gate.isoData[offset+1] == (byte)0xED)
			{ offset+=2; gate.emvDFED = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0xDF && gate.isoData[offset+1] == (byte)0xEE)
			{ offset+=2; gate.emvDFEE = hostVarBinBcd(1); }
		else
		if ( gate.isoData[offset] == (byte)0xDF && gate.isoData[offset+1] == (byte)0xEF)
			{ offset+=2; gate.emvDFEF = hostVarBinBcd(1); }
		else
			{ offset+=2; hostVarBinBcd(1); }
	}

		return;
}
//FISC MESSAGE DATA ELEMENT #58 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200304 , ##START##
private void hostVarF58(int size, boolean bP_IsEbcdic) throws Exception
{
String fieldData="", checkCode="", unUseData="", lenData="";
int    fieldLen=0, subOffset=0;

byte[] L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, size);

if (bP_IsEbcdic)
	lenData = HpeUtil.ebcdic2Str(L_TmpAry);
else
	lenData = new String(L_TmpAry, 0, L_TmpAry.length);
//lenData = isoStringOfFisc.substring(offset, offset + len);
fieldLen = Integer.parseInt(lenData);
offset += size;

L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, fieldLen);

if (bP_IsEbcdic)
	fieldData = HpeUtil.ebcdic2Str(L_TmpAry);
else
	fieldData = new String(L_TmpAry, 0, L_TmpAry.length);

//gate.emvTrans = true;
int checkPnt  = offset+fieldLen;
while ( offset < checkPnt )
 {
	checkCode = fieldData.substring(subOffset,subOffset+2);
	   offset +=2; subOffset +=2;
	   //Tag21- 紅利扣抵資訊 (0100/0110;0420/0430)
	   if ( checkCode.equals("21") ) {
		   gate.f58t21 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t21.length()+2;
		   offset += gate.f58t21.length()+2;	}
	   else	
	   //Tag28- 電子化繳費稅處理平台發卡參加機構代碼   (0100/0110;0420/0430)
	   if ( checkCode.equals("28") ) {
		   gate.f58t28 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t28.length()+2;
		   offset += gate.f58t28.length()+2;
	   }
	   else	
	   //Tag30- 大賣場收單處理單位代號    (0100/0110;0420/0430)
	   if ( checkCode.equals("30") ) {
		   gate.f58t30 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t30.length()+2;
		   offset += gate.f58t30.length()+2;	
	   }
	   else	
	   //Tag31- 雙幣卡匯率轉換資訊  (0100/0110;0120/0130;0420/0430)
	   if ( checkCode.equals("31") ) {
		   gate.f58t31 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t31.length()+2;
		   offset += gate.f58t31.length()+2;	
	   }
	   else
	   //Tag32- TSP Transaction Data (0100/0110;0120/0130;0420/0430)
	   if ( checkCode.equals("32") ) {
		   gate.f58t32 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t32.length()+2;
		   offset += gate.f58t32.length()+2;	
	   }
	   else	
	   //Tag33- 電子化繳費稅處理平台代收行   (0100;0420)	 
	   if ( checkCode.equals("33") ) {
		   gate.f58t33 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t33.length()+2;
		   offset += gate.f58t33.length()+2;	
	   }
	   else	
	   //Tag49- 信用卡載具資訊  	     (0100/0110)
	   if ( checkCode.equals("49") ) {
		   gate.f58t49 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t49.length()+2;
		   offset += gate.f58t49.length()+2;	
	   }
	   else
	   //Tag50- 銀聯優計劃 Coupon資訊   	  (0100/0110;0420/0430)  
	   if ( checkCode.equals("50") ) {
		   gate.f58t50 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t50.length()+2;
		   offset += gate.f58t50.length()+2;	
	   }
	   else	
	   //Tag51- 銀 聯 QR Code Voucher Number   (0100/0110;0420/0430)
	   if ( checkCode.equals("51") ) {
		   gate.f58t51 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t51.length()+2;
		   offset += gate.f58t51.length()+2;	
	   }
	   else	
	   //Tag53- 銀聯實時立減折扣資訊  (0100/0110) 
	   if ( checkCode.equals("53") ) {
		   gate.f58t53 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t53.length()+2;
		   offset += gate.f58t53.length()+2;	
	   }
	   else	
	   //Tag56-Payment Account Reference (PAR)  (0100/0110;0120;0420/0430) 
	   if ( checkCode.equals("56") ) {
		   gate.f58t56 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t56.length()+2;
		   offset += gate.f58t56.length()+2;	
	   }
	   else	
	   //Tag60- 交易識別碼             (0100/0110;0120;0420)
	   if ( checkCode.equals("60") ) {
		   gate.f58t60 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t60.length()+2;
		   offset += gate.f58t60.length()+2;	
	   }
	   else	
	   //Tag61-授權欄位驗證碼    (0110)
	   if ( checkCode.equals("61") ) {
		   gate.f58t61 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t61.length()+2;
		   offset += gate.f58t61.length()+2;	
	   }
	   else	
	   //Tag62-卡片級別識別碼     (0100/0110;0120/0130;0420/0430)
	   if ( checkCode.equals("62") ) {
		   gate.f58t62 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t62.length()+2;
		   offset += gate.f58t62.length()+2;	
	   }
	   else	
	   //Tag63-授權通知來源         (0120)
	   if ( checkCode.equals("63") ) {
		   gate.f58t63 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t63.length()+2;
		   offset += gate.f58t63.length()+2;	
	   }
	   else	
	   //Tag64-銀聯交易傳輸日期時間  (0110)
	   if ( checkCode.equals("64") ) {
		   gate.f58t64 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t64.length()+2;
		   offset += gate.f58t64.length()+2;	
	   }
	   else	
	   //Tag65-銀聯交易序號           (0110)
	   if ( checkCode.equals("65") ) {
		   gate.f58t65 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t65.length()+2;
		   offset += gate.f58t65.length()+2;	
	   }
	   else	
	   //Tag66-代收通知理由碼       (0120;0620)
	   if ( checkCode.equals("66") ) {
		   gate.f58t66 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t66.length()+2;
		   offset += gate.f58t66.length()+2;	
	   }
	   else	
	   //Tag67-卡號比對資訊           (0100;0420)
	   if ( checkCode.equals("67") ) {
		   gate.f58t67 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t67.length()+2;
		   offset += gate.f58t67.length()+2;	
	   }
	   else	
	   //Tag68-卡號比對資訊/繳費稅交易傳送機構    (0100;0420)
	   if ( checkCode.equals("68") ) {
		   gate.f58t68 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t68.length()+2;
		   offset += gate.f58t68.length()+2;	
	   }
	   else	
	   //Tag69-信用卡特殊平台交易識別碼    (0100/0110;0120/0130;0420/0430)
	   if ( checkCode.equals("69") ) {
		   gate.f58t69 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t69.length()+2;
		   offset += gate.f58t69.length()+2;	
	   }
	   else	
       //Tag70-VISA國 際組織訊息理 由碼    (0100;0120;0302)
	   if ( checkCode.equals("70") ) {
		   gate.f58t70 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t70.length()+2;
		   offset += gate.f58t70.length()+2;	
	   }
	   else	
	   //Tag71-VISA國際組織檔案維護訊息錯誤 碼   (0312)
	   if ( checkCode.equals("71") ) {
		   gate.f58t71 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t71.length()+2;
		   offset += gate.f58t71.length()+2;	
	   }
	   else	
	   //Tag72-FiscerCard國際組織通知細部理由碼    (0120;0620)
	   if ( checkCode.equals("72") ) {
		   gate.f58t72 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t72.length()+2;
		   offset += gate.f58t72.length()+2;	
	   }
	   else	
	   //Tag73-Token交易類型   (0100/0110;0120/0130;0420/0430;0620/0630)
	   if ( checkCode.equals("73") ) {
		   gate.f58t73 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t73.length()+2;
		   offset += gate.f58t73.length()+2;	
	   }
	   else	
	   //Tag80-悠遊卡端末設備交易日期時間   (0100;0120)
	   if ( checkCode.equals("80") ) {
		   gate.f58t80 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t80.length()+2;
		   offset += gate.f58t80.length()+2;	
	   }
	   else	
	   //Tag81-悠遊卡端末設備交易序號           (0100;0120)
	   if ( checkCode.equals("81") ) {
		   gate.f58t81 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t81.length()+2;
		   offset += gate.f58t81.length()+2;	
	   }
	   else	
	   //Tag82-一 卡 通NTID序號      (0100/0110;0120/0130;0420/0430)
	   if ( checkCode.equals("82") ) {
		   gate.f58t82 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t82.length()+2;
		   offset += gate.f58t82.length()+2;	
	   }
	   else	
	   //Tag83-悠遊卡主機端交易日期時間  0312
	   if ( checkCode.equals("83") ) {
		   gate.f58t83 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t83.length()+2;
		   offset += gate.f58t83.length()+2;	
	   }
	   else	
	   //Tag84-愛金卡交易 資訊     (0100/0110;0120/0130;0312;0420/0430
	   if ( checkCode.equals("84") ) {
		   gate.f58t84 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t84.length()+2;
		   offset += gate.f58t84.length()+2;	
	   }
	   else	
	   //Tag85-愛金卡掛卡/ 取消掛卡回覆碼  (0312)
	   if ( checkCode.equals("85") ) {
		   gate.f58t85 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t85.length()+2;
		   offset += gate.f58t85.length()+2;	
	   }
	   else	
	   //Tag86-信用卡載具中獎入戶同意註記資訊   (0110)
	   if ( checkCode.equals("86") ) {
		   gate.f58t86 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t86.length()+2;
		   offset += gate.f58t86.length()+2;	
	   }
	   else	
	   //Tag90-Merchant PAN  (0100;0120;0420)
	   if ( checkCode.equals("90") ) {
		   gate.f58t90 = hostVarAns(2, subOffset, fieldData);  
		   subOffset += gate.f58t90.length()+2;
		   offset += gate.f58t90.length()+2;	
	   }
	   else	{
	   		unUseData = hostVarAns(2, subOffset, fieldData);  
	   		subOffset += unUseData.length()+2;
	   		offset += unUseData.length()+2;	
	   }
 }

return;
}
//FISC MESSAGE DATA ELEMENT #58 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200304 , ##END##

private String hostVarBinBcd(int size)
{
String fieldData="";
int    fieldLen=0;

if ( size == 1 )
   { fieldLen  =   gate.isoData[offset] & 0xFF; offset++; }
else
   { fieldLen  = ( gate.isoData[offset] & 0xFF ) * 256 + ( gate.isoData[offset+1] & 0xFF ); offset +=2; }

fieldData = hostFixBcd(fieldLen);
return fieldData;
}


public boolean host2Iso()
{
try
{

//kevin:remove by fail
//if ( !convertToInterChange() )
//   { return false; }

String  cvtStr="";
int     cnt =0;

StringBuffer cvtZeros = new StringBuffer();
StringBuffer cvtSpace = new StringBuffer();
for( int i=0; i<30; i++ )
   {
     cvtZeros.append("0000000000");
     cvtSpace.append("          ");
   }
zeros    = cvtZeros.toString();
spaces   = cvtSpace.toString();
cvtZeros = null;
cvtSpace = null;

offset = gate.initPnt;
fiscFixAns(gate.mesgType,4);
setByteMap();
cvtStr=byteMap.substring(0,64);
byteMapToBitMap(cvtStr,8);

if ( byteMap.charAt(1-1)  == '0' )
   { cnt = 64; }
else
if ( byteMap.charAt(1-1)  == '1' )
   {
     cvtStr=byteMap.substring(64,128);
     byteMapToBitMap(cvtStr,8);
     cnt = 128;
   }

for ( k=2; k<=cnt; k++ )
    {
      if ( byteMap.charAt(k-1) == '1' )
         {
           switch ( k )
            {
             case  2 : fiscVarAns(gate.isoField[k],2);      break;
             case  3 : fiscFixAns(gate.isoField[k],6);      break;
             case  4 : fiscFixAns(gate.isoField[k],12);     break;
             case  5 : fiscFixAns(gate.isoField[k],12);     break;
             case  6 : fiscFixAns(gate.isoField[k],12);     break;
             case  7 : fiscFixAns(gate.isoField[k],10);     break;
             case  8 : fiscFixAns(gate.isoField[k],8);      break;
             case  9 : fiscFixAns(gate.isoField[k],8);      break;
             case  10: fiscFixAns(gate.isoField[k],8);      break;
             case  11: fiscFixAns(gate.isoField[k],6);      break;
             case  12: fiscFixAns(gate.isoField[k],6);      break;
             case  13: fiscFixAns(gate.isoField[k],4);      break;
             case  14: fiscFixAns(gate.isoField[k],4);      break;
             case  15: fiscFixAns(gate.isoField[k],4);      break;
             case  16: fiscFixAns(gate.isoField[k],4);      break;
             case  17: fiscFixAns(gate.isoField[k],4);      break;
             case  18: fiscFixAns(gate.isoField[k],4);      break;
             case  19: fiscFixAns(gate.isoField[k],3);      break;
             case  20: fiscFixAns(gate.isoField[k],3);      break;
             case  21: fiscFixAns(gate.isoField[k],3);      break;
             case  22: fiscFixAns(gate.isoField[k],3);      break;
             case  23: fiscFixAns(gate.isoField[k],3);      break;
             case  24: fiscFixAns(gate.isoField[k],3);      break;
             case  25: fiscFixAns(gate.isoField[k],2);      break;
             case  26: fiscFixAns(gate.isoField[k],2);      break;
             case  27: fiscFixAns(gate.isoField[k],1);      break;
             case  28: fiscFixAns(gate.isoField[k],9);      break;
             case  29: fiscFixAns(gate.isoField[k],9);      break;
             case  30: fiscFixAns(gate.isoField[k],9);      break;
             case  31: fiscFixAns(gate.isoField[k],9);      break;
             case  32: fiscVarAns(gate.isoField[k],2);      break;
             case  33: fiscVarAns(gate.isoField[k],2);      break;
             case  34: fiscVarAns(gate.isoField[k],2);      break;
             case  35: fiscVarAns(gate.isoField[k],2);      break;
             case  36: fiscVarAns(gate.isoField[k],3);      break;
             case  37: fiscFixAns(gate.isoField[k],12);     break;
             case  38: fiscFixAns(gate.isoField[k],6);      break;
             case  39: fiscFixAns(gate.isoField[k],2);      break;
             case  40: fiscFixAns(gate.isoField[k],3);      break;
             case  41: fiscFixAns(gate.isoField[k],8);      break;
             case  42: fiscFixAns(gate.isoField[k],15);     break;
             case  43: fiscFixAns(gate.isoField[k],40);     break;
             case  44: fiscVarAns(gate.isoField[k],2);      break;
             case  45: fiscVarAns(gate.isoField[k],2);      break;
             case  46: fiscVarAns(gate.isoField[k],3);      break;
             case  47: fiscVarAns(gate.isoField[k],3);      break;
             case  48: fiscVarF48();                        break;
             case  49: fiscFixAns(gate.isoField[k],3);      break;
             case  50: fiscFixAns(gate.isoField[k],3);      break;
             case  51: fiscFixAns(gate.isoField[k],3);      break;
             case  52: fiscFixBcd(gate.isoField[k],8);      break;
             case  53: fiscFixAns(gate.isoField[k],8);      break;
             case  54: fiscVarAns(gate.isoField[k],3);      break;
             case  55: fiscVarF55();                        break;
             case  56: fiscVarAns(gate.isoField[k],3);      break;
             case  57: fiscVarAns(gate.isoField[k],3);      break;
             case  58: fiscVarAns(gate.isoField[k],3);      break;
             case  59: fiscVarAns(gate.isoField[k],3);      break;
             case  60: fiscVarAns(gate.isoField[k],3);      break;
             case  61: fiscVarAns(gate.isoField[k],3);      break;
             case  62: fiscVarAns(gate.isoField[k],3);      break;
             case  63: fiscVarAns(gate.isoField[k],3);      break;
             case  64: fiscFixBcd(gate.isoField[k],8);      break;
             case  65: fiscFixBcd(gate.isoField[k],8);      break;
             case  66: fiscFixAns(gate.isoField[k],1);      break;
             case  67: fiscFixAns(gate.isoField[k],2);      break;
             case  68: fiscFixAns(gate.isoField[k],3);      break;
             case  69: fiscFixAns(gate.isoField[k],3);      break;
             case  70: fiscFixAns(gate.isoField[k],3);      break;
             case  71: fiscFixAns(gate.isoField[k],4);      break;
             case  72: fiscFixAns(gate.isoField[k],4);      break;
             case  73: fiscFixAns(gate.isoField[k],6);      break;
             case  74: fiscFixAns(gate.isoField[k],10);     break;
             case  75: fiscFixAns(gate.isoField[k],10);     break;
             case  76: fiscFixAns(gate.isoField[k],10);     break;
             case  77: fiscFixAns(gate.isoField[k],10);     break;
             case  78: fiscFixAns(gate.isoField[k],10);     break;
             case  79: fiscFixAns(gate.isoField[k],10);     break;
             case  80: fiscFixAns(gate.isoField[k],10);     break;
             case  81: fiscFixAns(gate.isoField[k],10);     break;
             case  82: fiscFixAns(gate.isoField[k],12);     break;
             case  83: fiscFixAns(gate.isoField[k],12);     break;
             case  84: fiscFixAns(gate.isoField[k],12);     break;
             case  85: fiscFixAns(gate.isoField[k],12);     break;
             case  86: fiscFixAns(gate.isoField[k],16);     break;
             case  87: fiscFixAns(gate.isoField[k],16);     break;
             case  88: fiscFixAns(gate.isoField[k],16);     break;
             case  89: fiscFixAns(gate.isoField[k],16);     break;
             case  90: fiscFixAns(gate.isoField[k],42);     break;
             case  91: fiscFixAns(gate.isoField[k],1);      break;
             case  92: fiscFixAns(gate.isoField[k],2);      break;
             case  93: fiscFixAns(gate.isoField[k],5);      break;
             case  94: fiscFixAns(gate.isoField[k],7);      break;
             case  95: fiscFixAns(gate.isoField[k],42);     break;
             case  96: fiscFixBcd(gate.isoField[k],8);      break;
             case  97: fiscFixAns(gate.isoField[k],17);     break;
             case  98: fiscFixAns(gate.isoField[k],25);     break;
             case  99: fiscVarAns(gate.isoField[k],2);      break;
             case 100: fiscVarAns(gate.isoField[k],2);      break;
             case 101: fiscVarAns(gate.isoField[k],2);      break;
             case 102: fiscVarAns(gate.isoField[k],2);      break;
             case 103: fiscVarAns(gate.isoField[k],2);      break;
             case 104: fiscVarAns(gate.isoField[k],3);      break;
             case 105: fiscVarAns(gate.isoField[k],3);      break;
             case 112: fiscVarAns(gate.isoField[k],3);      break;
             case 113: fiscVarAns(gate.isoField[k],3);      break;
             case 114: fiscVarAns(gate.isoField[k],3);      break;
             case 115: fiscVarAns(gate.isoField[k],3);      break;
             case 116: fiscVarAns(gate.isoField[k],3);      break;
             case 117: fiscVarAns(gate.isoField[k],3);      break;
             case 118: fiscVarAns(gate.isoField[k],3);      break;
             case 119: fiscVarAns(gate.isoField[k],3);      break;
             case 120: fiscVarAns(gate.isoField[k],3);      break;
             case 121: fiscVarAns(gate.isoField[k],3);      break;
             case 122: fiscVarAns(gate.isoField[k],3);      break;
             case 123: fiscVarAns(gate.isoField[k],3);      break;
             case 124: fiscVarAns(gate.isoField[k],3);      break;
             case 125: fiscFixBcd(gate.isoField[k],8);      break;
             case 126: fiscVarAns(gate.isoField[k],3);      break;
             case 127: fiscVarAns(gate.isoField[k],3);      break;
             case 128: fiscFixBcd(gate.isoField[k],8);      break;
             default : break;
           }
        }
    }

gate.totalLen = offset;
gate.dataLen  = offset - gate.initPnt;

gate.isoData[0]   = (byte)(gate.dataLen / 256);
gate.isoData[1]   = (byte)(gate.dataLen % 256);

} // end of try

catch ( Exception ex )
     { expHandle(ex); return false; }
return true;

} // end of host2Iso



private void setByteMap()
{
int  i=0,k=0;
char map[] = new char[129];
String tmpStr="";

for ( i=0; i<=128; i++ )
    { map[i] = '0'; }

if  ( gate.tccCode.length() > 0 || gate.keyExchangeBlock.length() > 0 ||
      gate.ucafInd.length() > 0 || gate.ucaf.length() > 0 || gate.xid.length() > 0 || gate.cvv2.length() > 0 )
    { gate.isoField[48] = "A"; }

if  ( gate.emvTrans )
    { gate.isoField[55] = "A"; }

for( k=2; k<128; k++ )
   {
     if ( gate.isoField[k].length() > 0 )
        { map[k-1]  = '1';  }

     if ( map[k-1]  == '1'  &&  k > 64 )
        { map[1-1]  =  '1'; }
   }

byteMap = String.valueOf(map);
return;
}

private void byteMapToBitMap(String src, int size)
{
String tmp ="";
int    i=0,pnt =0;

for ( i=0; i<size; i++ )
   {
     tmp = src.substring(pnt,pnt+8);
     gate.isoData[offset] = (byte)(Integer.parseInt(tmp, 2));
     pnt += 8;
     offset++;
   }

return;
}

private void fiscFixAns(String fieldData,int len) throws UnsupportedEncodingException
{
int i=0;
if ( fieldData.length() < len )
  { fieldData =  fieldData + spaces.substring(0,len-fieldData.length()); }
else
if ( fieldData.length() > len )
  { fieldData = fieldData.substring(0,len);  }

byte[] tmp = fieldData.getBytes("Cp1047");
for ( i=0; i<len; i++ )
    { gate.isoData[offset] = tmp[i];   offset++;  }

return;
}

private void fiscVarAns(String fieldData,int size) throws UnsupportedEncodingException
{
String tmpStr="";
byte[] tmpByte;
int    i=0;

tmpStr= String.valueOf(fieldData.length());
if ( ( size - tmpStr.length()) == 1  )
  { tmpStr = "0" + tmpStr;  }
else
if ( ( size - tmpStr.length()) == 2  )
  { tmpStr = "00" + tmpStr; }

tmpByte = tmpStr.getBytes("Cp1047");
for ( i=0; i<size; i++ )
   { gate.isoData[offset] = tmpByte[i];   offset++;   }

tmpByte = fieldData.getBytes("Cp1047");
for ( i=0; i<fieldData.length(); i++ )
   { gate.isoData[offset] = tmpByte[i];   offset++;   }

return;
}

private void fiscFixBcd(String fieldData,int size)
{
int mod = fieldData.length() % 2;
if ( mod != 0 )
  { fieldData = "0" + fieldData;  }

int len = fieldData.length() / 2;

if ( len != size )
  { retCode = "F"+k; return; }

convertBcd(fieldData,len);
return;
}

private void convertBcd(String src,int size)
{
int    i=0,left=0,right=0,pnt=0;
byte[] tmp = src.getBytes();
for ( i=0; i<size; i++ )
   {
     left  = tmp[pnt] - 48;   pnt++;
     if ( left > 40 )
        { left = left - 39;   }
     else
     if ( left > 10 )
        { left = left - 7;    }
     right = tmp[pnt] - 48;   pnt++;
     if ( right > 40 )
        { right = right - 39; }
     else
     if ( right > 10 )
        { right = right - 7;  }
     gate.isoData[offset] = (byte)(left * 16 + right);
     offset++;
   }
return;
}

private void fiscVarF48() throws UnsupportedEncodingException
{
int len =0,mod=0;
String tmpStr="";
byte[] tmpByte;
int    checkPnt=0;

checkPnt=offset;
offset +=3;

if ( gate.tccCode.length() == 1 )
   { fiscFixAns(gate.tccCode,1); }

if ( gate.keyExchangeBlock.length() > 0 )
   {
     fiscFixAns("11",2);
     fiscVarAns(gate.keyExchangeBlock,2);
   }

if ( gate.ucafInd.length() == 1 )
   {
     fiscFixAns("42",2);
     fiscVarAns("010300"+gate.ucafInd,2);
   }

if ( gate.ucaf.length() > 0 )
   {
     fiscFixAns("43",2);
     fiscVarAns(gate.ucaf,2);
   }

if ( gate.xid.length() > 0 )
   {
     fiscFixAns("44",2);
     fiscFixBcd(gate.xid,10);
   }

if ( gate.ucafInd.length() == 1 )
   {
     fiscFixAns("92",2);
     fiscVarAns(gate.cvv2,2);
   }

tmpStr= (offset - checkPnt -3)+"";
if ( tmpStr.length()  == 1  )
   { tmpStr = "00" + tmpStr;   }
else
if ( tmpStr.length()  == 2  )
   { tmpStr = "0" + tmpStr;   }
tmpByte = tmpStr.getBytes("Cp1047");
for( int i=0; i<3; i++ )
   { gate.isoData[checkPnt+i] = tmpByte[i]; }

return;
}

private void fiscVarF55()
{
int len =0,mod=0;
String tmpStr="";
byte[] tmpByte;
int    checkPnt=0;

checkPnt=offset;
offset +=3;

if ( !gate.requestTrans )
   { fiscTag55("91",gate.emv91); }

if ( gate.emv57.length() > 0 )
   { fiscTag55("57",gate.emv57); }

if ( gate.emv5A.length() > 0 )
   { fiscTag55("5A",gate.emv5A); }

if ( gate.emv5F24.length() > 0 )
   { fiscTag55("5F24",gate.emv5F24); }

if ( gate.emv5F2A.length() > 0 )
   { fiscTag55("5F2A",gate.emv5F2A); }

if ( gate.emv5F34.length() > 0 )
   { fiscTag55("5F34",gate.emv5F34); }

if ( gate.emv71.length() > 0 )
   { fiscTag55("71",gate.emv71); }

if ( gate.emv72.length() > 0 )
   { fiscTag55("72",gate.emv72); }

if ( gate.emv82.length() > 0 )
   { fiscTag55("82",gate.emv82); }

if ( gate.emv84.length() > 0 )
   { fiscTag55("84",gate.emv84); }

if ( gate.emv8A.length() > 0 )
   { fiscTag55("8A",gate.emv8A); }

if ( gate.emv91.length() > 0 )
   { fiscTag55("91",gate.emv91); }

if ( gate.emv95.length() > 0 )
   { fiscTag55("95",gate.emv95); }

if ( gate.emv9A.length() > 0 )
   { fiscTag55("9A",gate.emv9A); }

if ( gate.emv9B.length() > 0 )
   { fiscTag55("9B",gate.emv9B); }

if ( gate.emv9C.length() > 0 )
   { fiscTag55("9C",gate.emv9C); }

if ( gate.emv9F02.length() > 0 )
   { fiscTag55("9F02",gate.emv9F02); }

if ( gate.emv9F03.length() > 0 )
   { fiscTag55("9F03",gate.emv9F03); }

if ( gate.emv9F09.length() > 0 )
   { fiscTag55("9F09",gate.emv9F09); }

if ( gate.emv9F10.length() > 0 )
   { fiscTag55("9F10",gate.emv9F10); }

if ( gate.emv9F1A.length() > 0 )
   { fiscTag55("9F1A",gate.emv9F1A); }

if ( gate.emv9F1E.length() > 0 )
   { fiscTag55("9F1E",gate.emv9F1E); }

if ( gate.emv9F26.length() > 0 )
   { fiscTag55("9F26",gate.emv9F26); }

if ( gate.emv9F27.length() > 0 )
   { fiscTag55("9F27",gate.emv9F27); }

if ( gate.emv9F33.length() > 0 )
   { fiscTag55("9F33",gate.emv9F33); }

if ( gate.emv9F34.length() > 0 )
   { fiscTag55("9F34",gate.emv9F34); }

if ( gate.emv9F35.length() > 0 )
   { fiscTag55("9F35",gate.emv9F35); }

if ( gate.emv9F36.length() > 0 )
   { fiscTag55("9F36",gate.emv9F36); }

if ( gate.emv9F37.length() > 0 )
   { fiscTag55("9F37",gate.emv9F37); }

if ( gate.emv9F41.length() > 0 )
   { fiscTag55("9F41",gate.emv9F41); }

if ( gate.emv9F53.length() > 0 )
   { fiscTag55("9F53",gate.emv9F53); }

if ( gate.emv9F5B.length() > 0 )
   { fiscTag55("9F5B",gate.emv9F5B); }

if ( gate.emvDFED.length() > 0 )
   { fiscTag55("DFED",gate.emvDFED); }

if ( gate.emvDFEE.length() > 0 )
   { fiscTag55("DFEE",gate.emvDFEE); }

if ( gate.emvDFEF.length() > 0 )
   { fiscTag55("DFEF",gate.emvDFEF); }

tmpStr= (offset - checkPnt -3)+"";
if ( tmpStr.length()  == 1  )
   { tmpStr = "00" + tmpStr;   }
else
if ( tmpStr.length()  == 2  )
   { tmpStr = "0" + tmpStr; }
tmpByte = tmpStr.getBytes();
for( int i=0; i<3; i++ )
   { gate.isoData[checkPnt+i] = tmpByte[i]; }

return;
}

private void fiscTag55(String tagData,String emvData)
{
int len = tagData.length() / 2;
fiscFixBcd(tagData,len);

if ( (emvData.length() % 2) != 0 )
   { emvData = "0" + emvData;  }
len = emvData.length() / 2;
gate.isoData[offset] = (byte)len;
offset++;
convertBcd(emvData,len);
return;
}

public void expHandle (Exception ex)
{
logger.fatal(" >> ####### MastFormat Exception MESSAGE STARTED ######");
logger.fatal("MastFormat Exception_Message : ", ex);
logger.fatal(" >> ####### Mast system Exception MESSAGE   ENDED ######");
return;
}

@Override
public boolean host2Iso(String sP_IsoCommand) {
	// TODO Auto-generated method stub
	return false;
}

}  // Class MastFormat End
