/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
*  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
*  111-01-19  V1.00.01    Justin    fix Code Correctness: Constructor Invokes Overridable Function
*****************************************************************************/
package com;

import com.*;
import java.util.*;

public class IpassFormat {

  public String byteMap = "", isoString = "", retCode = "", isoHead = "", destId = "",
      sourceId = "", hItalBitmap1 = "";
  public String zeros = "", spaces = "", fiid = "", dpcNum = "", lNet = "", mesgType = "";
  private int offset = 0, k = 0, dataLen = 0, totalLen = 0;

  public String[] isoField = new String[128];
  byte[] isoData = null;

  public IpassFormat(int dataLen, byte[] isoData) {
    this.dataLen = dataLen;
    this.isoData = isoData;
//    initIsoField();
    // fix Code Correctness: Constructor Invokes Overridable Function
	for (int i = 0; i < 128; i++) {
		isoField[i] = "";
	}
  }

  public void initIsoField() {
    for (int i = 0; i < 128; i++) {
      isoField[i] = "";
    }
  }

  /* �N BASE24 BIC �榡�ର�D���榡��� */
  public boolean iso2Host() {
    try {

      String cvtString = "";
      int cnt = 0;

      isoString = new String(isoData, 0, dataLen);

      offset = 0;
      isoString = isoString;

      isoHead = isoString.substring(0, 8);
      destId = isoString.substring(0, 4);
      sourceId = isoString.substring(4, 8);
      offset = 8;

      mesgType = isoString.substring(offset, offset + 4);
      offset += 4;

      cvtString = isoString.substring(offset, offset + 16);
      hItalBitmap1 = cvtString; // add
      byteMap = byte2ByteMap(cvtString, 16);
      offset += 16;

      if (byteMap.charAt(0) == '1') {
        cvtString = isoString.substring(offset, offset + 16);
        byteMap = byteMap + byte2ByteMap(cvtString, 16);
        offset += 16;
        cnt = 128;
      } else {
        cnt = 64;
      }


      for (k = 2; k <= cnt; k++) {
        if (byteMap.charAt(k - 1) == '1') {
          switch (k) {
            case 2:
              isoField[k] = hostVariable(2);
              break;
            case 3:
              isoField[k] = hostFixField(6);
              break;
            case 4:
              isoField[k] = hostFixField(12);
              break;
            case 7:
              isoField[k] = hostFixField(10);
              break;
            case 11:
              isoField[k] = hostFixField(6);
              break;
            case 13:
              isoField[k] = hostFixField(4);
              break;
            case 14:
              isoField[k] = hostFixField(4);
              break;
            case 36:
              isoField[k] = hostVariable(3);
              break;
            case 39:
              isoField[k] = hostFixField(2);
              retCode = isoField[k];
              break;
            case 41:
              isoField[k] = hostFixField(8);
              break;
            case 45:
              isoField[k] = hostVariable(2);
              break;
            case 63:
              isoField[k] = hostVariable(2);
              break;
            case 70:
              isoField[k] = hostFixField(3);
              break;
            case 73:
              isoField[k] = hostFixField(6);
              break;
            case 91:
              isoField[k] = hostFixField(1);
              break;
            default:
              break;
          }
        }
      }

    } // end of try
    catch (Exception ex) {
      expHandle(ex);
      return false;
    }
    return true;

  }

  private String byte2ByteMap(String src, int size) {
    byte[] srcByte = new byte[65];
    String[] cvt = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001",
        "1010", "1011", "1100", "1101", "1110", "1111"};
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

  private String hostVariable(int len) {
    String lenData = "", fieldData = "";
    int fieldLen = 0;

    lenData = isoString.substring(offset, offset + len);
    fieldLen = Integer.parseInt(lenData);
    offset += len;
    fieldData = isoString.substring(offset, offset + fieldLen);
    offset += fieldLen;
    return fieldData;
  }

  private String hostFixField(int len) {
    String fieldData = "";
    fieldData = isoString.substring(offset, offset + len);
    offset += len;
    return fieldData;
  }

  /* �N�D���榡����ର BASE24 BIC �榡 */
  public boolean host2Iso() {

    try {

      String cvtString = "";
      int cnt = 0;

      StringBuffer cvtZeros = new StringBuffer();
      StringBuffer cvtSpace = new StringBuffer();
      for (int i = 0; i < 30; i++) {
        cvtZeros.append("0000000000");
        cvtSpace.append("          ");
      }
      zeros = cvtZeros.toString();
      spaces = cvtSpace.toString();
      cvtZeros = null;
      cvtSpace = null;

      k = 0;
      isoString = "";
      setHeaderMap();
      isoString = isoString + mesgType;
      offset += 4;

      cvtString = byteMap.substring(0, 64);
      isoString = isoString + byteMap2Byte(cvtString, 16);
      offset += 16;

      if (byteMap.charAt(0) == '1') {
        cvtString = byteMap.substring(64, 128);
        isoString = isoString + byteMap2Byte(cvtString, 16);
        offset += 16;
        cnt = 128;
      } else {
        cnt = 64;
      }
      for (k = 2; k <= cnt; k++) {
        if (byteMap.charAt(k - 1) == '1') {
          switch (k) {
            case 2:
              b24Variable(isoField[k], 2);
              break;
            case 3:
              b24FixField(isoField[k], 6);
              break;
            case 4:
              b24FixField(isoField[k], 12);
              break;
            case 7:
              b24FixField(isoField[k], 10);
              break;
            case 11:
              b24FixField(isoField[k], 6);
              break;
            case 13:
              b24FixField(isoField[k], 4);
              break;
            case 14:
              b24FixField(isoField[k], 4);
              break;
            case 36:
              b24Variable(isoField[k], 3);
              break;
            case 39:
              b24FixField(isoField[k], 2);
              break;
            case 41:
              b24FixField(isoField[k], 8);
              break;
            case 45:
              b24Variable(isoField[k], 2);
              break;
            case 63:
              b24Variable(isoField[k], 2);
              break;
            case 70:
              b24FixField(isoField[k], 3);
              break;
            case 73:
              b24FixField(isoField[k], 6);
              break;
            case 91:
              b24FixField(isoField[k], 1);
              break;
            default:
              break;
          }
        }
      }

      // isoData = isoString.getBytes();

      // System.out.println("---IsoStr is=>"+ isoString + "----");
      // totalLen = isoData.length;
      // dataLen = totalLen - 2;
      // isoData[0] = (byte)(dataLen / 256);
      // isoData[1] = (byte)(dataLen % 256);

      isoString = isoString;
    } // end of try
    catch (Exception ex) {
      expHandle(ex);
      return false;
    }
    return true;
  }

  private void setHeaderMap() {
    int i = 0, k = 0;
    char[] map = new char[128];
    for (i = 0; i < 128; i++) {
      map[i] = '0';
    }

    isoHead = sourceId + destId;
    if (isoHead.length() != 8) {
      isoHead = "00000000";
    }

    if (mesgType.length() != 4) {
      mesgType = "XXXX";
    }

    isoString = spaces.substring(0, 2) + isoHead;
    offset = 10;

    for (k = 2; k < 128; k++) {

      if (isoField[k].length() > 0) {
        map[k - 1] = '1';
      }

      if (isoField[k].length() > 0 && k > 64) {
        map[0] = '1';
      }
    }

    byteMap = String.valueOf(map);
  }

  private String byteMap2Byte(String src, int size) {
    char[] destChar = new char[33];
    char[] cvt = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    int i = 0, j = 0, ind = 0;
    String dest = "", tmp = "";

    for (i = 0; i < size; i++) {
      tmp = "";
      tmp = src.substring(j, j + 4);
      ind = Integer.parseInt(tmp, 2);
      destChar[i] = cvt[ind];
      j += 4;
    }

    dest = String.valueOf(destChar);
    dest = dest.substring(0, size);

    return dest;
  }

  private void b24Variable(String fieldData, int len) {
    String zeros = "00000000", tempStr = "";
    int fieldLen = 0;

    fieldLen = fieldData.length();
    tempStr = String.valueOf(fieldLen);
    if (tempStr.length() < len) {
      tempStr = zeros.substring(0, len - tempStr.length()) + tempStr;
    }
    isoString = isoString + tempStr + fieldData;
    offset = offset + len + fieldLen;
  }

  private void b24FixField(String fieldData, int len) {
    if (fieldData.length() < len) {
      fieldData = fieldData + spaces.substring(0, len - fieldData.length());
    }

    isoString = isoString + fieldData.substring(0, len);
    offset += len;
  }


  public void expHandle(Exception ex) {

    return;
  }

} // Class BA24 End
