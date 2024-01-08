/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*  110-12-23  V1.00.02  Justin      log4j1 -> log4j2 
******************************************************************************/
package bank.AuthIntf;

import java.io.UnsupportedEncodingException;
import java.util.*;
import org.apache.logging.log4j.core.Logger;

public class BicFormat extends ConvertMessage implements FormatInterChange {

  public String byteMap = "", isoString = "", retCode = "";
  public String zeros = "", spaces = "", fiid = "", dpcNum = "", lNet = "";
  public String[] iso117 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
  public String[] iso120 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
  public String[] iso121 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
  public String[] iso122 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
  public String[] iso125 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
  public String[] iso126 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
  private int offset = 0, k = 0;

  public BicFormat(Logger logger, AuthGate gate, HashMap cvtHash) {
    super.logger = logger;
    super.gate = gate;
    super.cvtHash = cvtHash;
  }

  /* �N BASE24 BIC �榡�ର�D���榡��� */
  public boolean iso2Host() {
    try {

      String cvtString = "";
      int cnt = 0;

      isoString = new String(gate.isoData, 0, gate.dataLen);

      offset = 0;
      gate.isoString = isoString;

      gate.bicHead = isoString.substring(0, 12);
      offset = 12;

      gate.mesgType = isoString.substring(offset, offset + 4);
      offset += 4;

      cvtString = isoString.substring(offset, offset + 16);
      byteMap = byte2ByteMap(cvtString, 16);
      gate.iso_bitMap = byteMap;
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
              gate.isoField[k] = hostVariable(2);
              break;
            case 3:
              gate.isoField[k] = hostFixField(6);
              break;
            case 4:
              gate.isoField[k] = hostFixField(12);
              break;
            case 5:
              gate.isoField[k] = hostFixField(12);
              break;
            case 6:
              gate.isoField[k] = hostFixField(12);
              break;
            case 7:
              gate.isoField[k] = hostFixField(10);
              break;
            case 8:
              gate.isoField[k] = hostFixField(8);
              break;
            case 9:
              gate.isoField[k] = hostFixField(8);
              break;
            case 10:
              gate.isoField[k] = hostFixField(8);
              break;
            case 11:
              gate.isoField[k] = hostFixField(6);
              break;
            case 12:
              gate.isoField[k] = hostFixField(6);
              break;
            case 13:
              gate.isoField[k] = hostFixField(4);
              break;
            case 14:
              gate.isoField[k] = hostFixField(4);
              break;
            case 15:
              gate.isoField[k] = hostFixField(4);
              break;
            case 16:
              gate.isoField[k] = hostFixField(4);
              break;
            case 17:
              gate.isoField[k] = hostFixField(4);
              break;
            case 18:
              gate.isoField[k] = hostFixField(4);
              break;
            case 19:
              gate.isoField[k] = hostFixField(3);
              break;
            case 20:
              gate.isoField[k] = hostFixField(3);
              break;
            case 21:
              gate.isoField[k] = hostFixField(3);
              break;
            case 22:
              gate.isoField[k] = hostFixField(3);
              break;
            case 23:
              gate.isoField[k] = hostFixField(3);
              break;
            case 24:
              gate.isoField[k] = hostFixField(3);
              break;
            case 25:
              gate.isoField[k] = hostFixField(2);
              break;
            case 26:
              gate.isoField[k] = hostFixField(2);
              break;
            case 27:
              gate.isoField[k] = hostFixField(1);
              break;
            case 28:
              gate.isoField[k] = hostFixField(8);
              break;
            case 29:
              gate.isoField[k] = hostFixField(8);
              break;
            case 30:
              gate.isoField[k] = hostFixField(8);
              break;
            case 31:
              gate.isoField[k] = hostFixField(8);
              break;
            case 32:
              gate.isoField[k] = hostVariable(2);
              break;
            case 33:
              gate.isoField[k] = hostVariable(2);
              break;
            case 34:
              gate.isoField[k] = hostVariable(2);
              break;
            case 35:
              gate.isoField[k] = hostVariable(2);
              break;
            case 36:
              gate.isoField[k] = hostVariable(3);
              break;
            case 37:
              gate.isoField[k] = hostFixField(12);
              break;
            case 38:
              gate.isoField[k] = hostFixField(6);
              break;
            case 39:
              gate.isoField[k] = hostFixField(2);
              retCode = gate.isoField[k];
              break;
            case 40:
              gate.isoField[k] = hostFixField(3);
              break;
            case 41:
              gate.isoField[k] = hostFixField(16);
              break;
            case 42:
              gate.isoField[k] = hostFixField(15);
              break;
            case 43:
              gate.isoField[k] = hostFixField(40);
              break;
            case 44:
              gate.isoField[k] = hostVariable(2);
              break;
            case 45:
              gate.isoField[k] = hostVariable(2);
              break;
            case 46:
              gate.isoField[k] = hostVariable(3);
              break;
            case 47:
              gate.isoField[k] = hostVariable(3);
              break;
            case 48:
              gate.isoField[k] = hostVariable(3);
              break;
            case 49:
              gate.isoField[k] = hostFixField(3);
              break;
            case 50:
              gate.isoField[k] = hostFixField(3);
              break;
            case 51:
              gate.isoField[k] = hostFixField(3);
              break;
            case 52:
              gate.isoField[k] = hostFixField(16);
              break;
            case 53:
              gate.isoField[k] = hostFixField(16);
              break;
            case 54:
              gate.isoField[k] = hostFixField(12);
              break;
            case 55:
              gate.isoField[k] = hostVariable(3);
              break;
            case 56:
              gate.isoField[k] = hostVariable(3);
              break;
            case 57:
              gate.isoField[k] = hostVariable(3);
              break;
            case 58:
              gate.isoField[k] = hostVariable(3);
              break;
            case 59:
              gate.isoField[k] = hostVariable(3);
              break;
            case 60:
              gate.isoField[k] = hostVariable(3);
              break;
            case 61:
              gate.isoField[k] = hostVariable(3);
              break;
            case 62:
              gate.isoField[k] = hostVariable(3);
              break;
            case 63:
              gate.isoField[k] = hostVariable(3);
              break;
            case 64:
              gate.isoField[k] = hostFixField(16);
              break;
            case 65:
              gate.isoField[k] = hostFixField(16);
              break;
            case 66:
              gate.isoField[k] = hostFixField(1);
              break;
            case 67:
              gate.isoField[k] = hostFixField(2);
              break;
            case 68:
              gate.isoField[k] = hostFixField(3);
              break;
            case 69:
              gate.isoField[k] = hostFixField(3);
              break;
            case 70:
              gate.isoField[k] = hostFixField(3);
              break;
            case 71:
              gate.isoField[k] = hostFixField(4);
              break;
            case 72:
              gate.isoField[k] = hostFixField(4);
              break;
            case 73:
              gate.isoField[k] = hostFixField(6);
              break;
            case 74:
              gate.isoField[k] = hostFixField(10);
              break;
            case 75:
              gate.isoField[k] = hostFixField(10);
              break;
            case 76:
              gate.isoField[k] = hostFixField(10);
              break;
            case 77:
              gate.isoField[k] = hostFixField(10);
              break;
            case 78:
              gate.isoField[k] = hostFixField(10);
              break;
            case 79:
              gate.isoField[k] = hostFixField(10);
              break;
            case 80:
              gate.isoField[k] = hostFixField(10);
              break;
            case 81:
              gate.isoField[k] = hostFixField(10);
              break;
            case 82:
              gate.isoField[k] = hostFixField(12);
              break;
            case 83:
              gate.isoField[k] = hostFixField(12);
              break;
            case 84:
              gate.isoField[k] = hostFixField(12);
              break;
            case 85:
              gate.isoField[k] = hostFixField(12);
              break;
            case 86:
              gate.isoField[k] = hostFixField(16);
              break;
            case 87:
              gate.isoField[k] = hostFixField(16);
              break;
            case 88:
              gate.isoField[k] = hostFixField(16);
              break;
            case 89:
              gate.isoField[k] = hostFixField(16);
              break;
            case 90:
              gate.isoField[k] = hostFixField(42);
              break;
            case 91:
              gate.isoField[k] = hostFixField(1);
              break;
            case 92:
              gate.isoField[k] = hostFixField(2);
              break;
            case 93:
              gate.isoField[k] = hostFixField(5);
              break;
            case 94:
              gate.isoField[k] = hostFixField(7);
              break;
            case 95:
              gate.isoField[k] = hostFixField(42);
              break;
            case 96:
              gate.isoField[k] = hostFixField(16);
              break;
            case 97:
              gate.isoField[k] = hostFixField(17);
              break;
            case 98:
              gate.isoField[k] = hostFixField(25);
              break;
            case 99:
              gate.isoField[k] = hostVariable(2);
              break;
            case 100:
              gate.isoField[k] = hostVariable(2);
              break;
            case 101:
              gate.isoField[k] = hostVariable(2);
              break;
            case 102:
              gate.isoField[k] = hostVariable(2);
              break;
            case 103:
              gate.isoField[k] = hostVariable(2);
              break;
            case 104:
              gate.isoField[k] = hostVariable(3);
              break;
            case 105:
              gate.isoField[k] = hostVariable(3);
              break;
            case 106:
              gate.isoField[k] = hostVariable(3);
              break;
            case 107:
              gate.isoField[k] = hostVariable(3);
              break;
            case 108:
              gate.isoField[k] = hostVariable(3);
              break;
            case 109:
              gate.isoField[k] = hostVariable(3);
              break;
            case 110:
              gate.isoField[k] = hostVariable(3);
              break;
            case 111:
              gate.isoField[k] = hostVariable(3);
              break;
            case 113:
              gate.isoField[k] = hostVariable(3);
              break;
            case 114:
              gate.isoField[k] = hostVariable(3);
              break;
            case 115:
              gate.isoField[k] = hostVariable(3);
              break;
            case 116:
              gate.isoField[k] = hostVariable(3);
              break;
            case 117:
              gate.isoField[k] = hostVariable(3);
              break;
            case 118:
              gate.isoField[k] = hostVariable(3);
              break;
            case 119:
              gate.isoField[k] = hostVariable(3);
              break;
            case 120:
              gate.isoField[k] = hostVariable(3);
              break;
            case 121:
              gate.isoField[k] = hostVariable(3);
              break;
            case 122:
              gate.isoField[k] = hostVariable(3);
              break;
            case 123:
              gate.isoField[k] = hostVariable(3);
              break;
            case 124:
              gate.isoField[k] = hostVariable(3);
              break;
            case 125:
              gate.isoField[k] = hostVariable(3);
              break;
            case 126:
              gate.isoField[k] = hostVariable(3);
              break;
            case 127:
              gate.isoField[k] = hostVariable(3);
              break;
            case 128:
              gate.isoField[k] = hostVariable(3);
              break;
            default:
              break;
          }
        }
      }

      if (gate.isoField[101].equals("CF")) {
        formatInCAF();
      } else if (gate.isoField[101].equals("DA")) {
        formatInPBF();
      } else if (gate.isoField[101].equals("NF")) {
        formatInNEG();
      } else if (gate.isoField[101].equals("VP")) {
        formatInVISA();
      }

      /* BIC �榡 �ഫ�� �@�P�榡 */
      /*
       * Jack : �Τ����o... if ( !convertToCommon() ) { return false; }
       */
      convertBicField("C"); // mark by Howard

    } // end of try
    catch (Exception ex) {
      expHandle(ex);
      return false;
    }
    return true;

  }

  private String byte2ByteMap(String src, int size) {
    byte[] srcByte = new byte[65];
    String[] cvt =
        {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010",
            "1011", "1100", "1101", "1110", "1111"};
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

  private void formatOutCAF() {
    if (gate.isoField[91].equals("5"))
      return;
    gate.isoField[117] =
        iso117[1] + iso117[2] + iso117[3] + iso117[4] + iso117[5] + iso117[6] + iso117[7]
            + iso117[8] + iso117[9] + iso117[10] + iso117[11] + iso117[12];

    gate.isoField[120] =
        iso120[1] + iso120[2] + iso120[3] + iso120[4] + iso120[5] + iso120[6] + iso120[7]
            + iso120[8] + iso120[9] + iso120[10];

    gate.isoField[121] = iso121[1] + iso121[2] + iso121[3] + iso121[4] + iso121[5] + iso121[6];

    gate.isoField[122] =
        iso122[1] + iso122[2] + iso122[3] + iso122[4] + iso122[5] + iso122[6] + iso122[7]
            + iso122[8] + iso122[9] + iso122[10];

    gate.isoField[126] = iso126[1] + iso126[2] + iso126[3] + iso126[4] + iso126[5] + iso126[6];

    if (gate.isoField[120].length() < 95)
      retCode = "120";
    else if (gate.isoField[121].length() < 60)
      retCode = "121";
    else if (gate.isoField[122].length() < 101)
      retCode = "122";
    else if (gate.isoField[126].length() < 44)
      retCode = "126";
  }

  private void formatOutPBF() {
    if (gate.isoField[91].equals("5"))
      return;
    gate.isoField[120] =
        iso120[1] + iso120[2] + iso120[3] + iso120[4] + iso120[5] + iso120[6] + iso120[7]
            + iso120[8] + iso120[9] + iso120[10];

    gate.isoField[122] = iso122[1] + iso122[2];

    gate.isoField[125] = iso125[1] + iso125[2] + iso125[3] + iso125[4] + iso125[5];

    if (gate.isoField[120].length() < 111)
      retCode = "120";
    else if (gate.isoField[122].length() < 30)
      retCode = "122";
    else if (gate.isoField[125].length() < 10)
      retCode = "125";
  }

  private void formatOutNEG() {
    if (gate.isoField[91].equals("5"))
      return;

    gate.isoField[120] = iso120[1] + iso120[2] + iso120[3] + iso120[4] + iso120[5];

    if (gate.isoField[120].length() < 15)
      retCode = "120";
  }

  private void formatOutVISA() {
    if (gate.isoField[91].equals("5")) {
      return;
    }

    if (gate.isoField[91].equals("3")) {
      gate.isoField[73] = "      ";
    }

    gate.isoField[120] =
        iso120[1] + iso120[2] + iso120[3] + iso120[4] + iso120[5] + iso120[6] + iso120[7];

    for (k = 0; k < 10; k++) {
      gate.isoField[120] = gate.isoField[120] + "          ";
    }

    if (gate.isoField[120].length() < 150)
      retCode = "120";
  }

  private void formatInCAF() {
    if (gate.isoField[117].length() >= 24) {
      iso117[1] = gate.isoField[117].substring(0, 4);
      iso117[2] = gate.isoField[117].substring(4, 5);
      iso117[3] = gate.isoField[117].substring(5, 6);
      iso117[4] = gate.isoField[117].substring(6, 10);
      iso117[5] = gate.isoField[117].substring(10, 11);
      iso117[6] = gate.isoField[117].substring(11, 12);
      iso117[7] = gate.isoField[117].substring(12, 16);
      iso117[8] = gate.isoField[117].substring(16, 20);
      iso117[9] = gate.isoField[117].substring(20, 21);
      iso117[10] = gate.isoField[117].substring(21, 22);
      iso117[11] = gate.isoField[117].substring(22, 23);
      iso117[12] = gate.isoField[117].substring(23, 24);
    }

    if (gate.isoField[120].length() >= 95) {
      iso120[1] = gate.isoField[120].substring(0, 2);
      iso120[2] = gate.isoField[120].substring(2, 3);
      iso120[3] = gate.isoField[120].substring(3, 19);
      iso120[4] = gate.isoField[120].substring(19, 31);
      iso120[5] = gate.isoField[120].substring(31, 43);
      iso120[6] = gate.isoField[120].substring(43, 55);
      iso120[7] = gate.isoField[120].substring(55, 67);
      iso120[8] = gate.isoField[120].substring(67, 79);
      iso120[9] = gate.isoField[120].substring(79, 91);
      iso120[10] = gate.isoField[120].substring(91, 95);
    }

    if (gate.isoField[121].length() >= 60) {
      iso121[1] = gate.isoField[121].substring(0, 4);
      iso121[2] = gate.isoField[121].substring(4, 16);
      iso121[3] = gate.isoField[121].substring(16, 28);
      iso121[4] = gate.isoField[121].substring(28, 40);
      iso121[5] = gate.isoField[121].substring(40, 52);
      iso121[6] = gate.isoField[121].substring(52, 60);
    }

    if (gate.isoField[122].length() >= 101) {
      iso122[1] = gate.isoField[122].substring(0, 12);
      iso122[2] = gate.isoField[122].substring(12, 24);
      iso122[3] = gate.isoField[122].substring(24, 48);
      iso122[4] = gate.isoField[122].substring(48, 60);
      iso122[5] = gate.isoField[122].substring(60, 72);
      iso122[6] = gate.isoField[122].substring(72, 76);
      iso122[7] = gate.isoField[122].substring(76, 88);
      iso122[8] = gate.isoField[122].substring(88, 100);
      iso122[9] = gate.isoField[122].substring(100, 101);
    }

    if (gate.isoField[126].length() >= 44) {
      iso126[1] = gate.isoField[126].substring(0, 2);
      iso126[2] = gate.isoField[126].substring(2, 4);
      iso126[3] = gate.isoField[126].substring(4, 32);
      iso126[4] = gate.isoField[126].substring(32, 33);
      iso126[5] = gate.isoField[126].substring(33, 43);
      iso126[6] = gate.isoField[126].substring(43, 44);
    }
  }

  private void formatInPBF() {
    if (gate.isoField[120].length() >= 111) {
      iso120[1] = gate.isoField[120].substring(0, 1);
      iso120[2] = gate.isoField[120].substring(1, 20);
      iso120[3] = gate.isoField[120].substring(20, 39);
      iso120[4] = gate.isoField[120].substring(39, 58);
      iso120[5] = gate.isoField[120].substring(58, 69);
      iso120[6] = gate.isoField[120].substring(69, 75);
      iso120[7] = gate.isoField[120].substring(75, 90);
      iso120[8] = gate.isoField[120].substring(90, 96);
      iso120[9] = gate.isoField[120].substring(96, 111);
    }

    if (gate.isoField[122].length() >= 30) {
      iso122[1] = gate.isoField[122].substring(0, 15);
      iso122[2] = gate.isoField[122].substring(15, 30);
    }

    if (gate.isoField[125].length() >= 10) {
      iso125[1] = gate.isoField[122].substring(0, 2);
      iso125[2] = gate.isoField[122].substring(2, 4);
      iso125[3] = gate.isoField[122].substring(4, 6);
      iso125[4] = gate.isoField[122].substring(6, 8);
      iso125[5] = gate.isoField[122].substring(8, 10);
    }
  }

  private void formatInNEG() {
    if (gate.isoField[120].length() >= 15) {
      iso120[1] = gate.isoField[120].substring(0, 2);
      iso120[2] = gate.isoField[120].substring(2, 4);
      iso120[3] = gate.isoField[120].substring(4, 5);
      iso120[4] = gate.isoField[120].substring(5, 11);
      iso120[5] = gate.isoField[120].substring(11, 15);
    }
  }

  private void formatInVISA() {
    if (gate.isoField[120].length() >= 50) {
      iso120[1] = gate.isoField[120].substring(0, 4);
      iso120[2] = gate.isoField[120].substring(4, 6);
      iso120[3] = gate.isoField[120].substring(6, 34);
      iso120[4] = gate.isoField[120].substring(34, 37);
      iso120[5] = gate.isoField[120].substring(37, 39);
      iso120[6] = gate.isoField[120].substring(39, 41);
      iso120[7] = gate.isoField[120].substring(41, 50);
    }
  }

  /* �N�D���榡����ର BASE24 BIC �榡 */
  public boolean host2Iso() {

    try {
      /*
       * Jack : �Τ����o... if ( !convertToInterChange() ) { return false; }
       */
      convertBicField("I");

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
      // if (gate.isoField[101].equals("CF")) formatOutCAF();
      // else if (gate.isoField[101].equals("DA")) formatOutPBF();
      // else if (gate.isoField[101].equals("NF")) formatOutNEG();
      // else if (gate.isoField[101].equals("VP")) formatOutVISA();

      setHeaderMap();
      isoString = isoString + gate.mesgType;
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
              B24Variable(gate.isoField[k], 2);
              break;
            case 3:
              B24FixField(gate.isoField[k], 6);
              break;
            case 4:
              B24FixField(gate.isoField[k], 12);
              break;
            case 5:
              B24FixField(gate.isoField[k], 12);
              break;
            case 6:
              B24FixField(gate.isoField[k], 12);
              break;
            case 7:
              B24FixField(gate.isoField[k], 10);
              break;
            case 8:
              B24FixField(gate.isoField[k], 8);
              break;
            case 9:
              B24FixField(gate.isoField[k], 8);
              break;
            case 10:
              B24FixField(gate.isoField[k], 8);
              break;
            case 11:
              B24FixField(gate.isoField[k], 6);
              break;
            case 12:
              B24FixField(gate.isoField[k], 6);
              break;
            case 13:
              B24FixField(gate.isoField[k], 4);
              break;
            case 14:
              B24FixField(gate.isoField[k], 4);
              break;
            case 15:
              B24FixField(gate.isoField[k], 4);
              break;
            case 16:
              B24FixField(gate.isoField[k], 4);
              break;
            case 17:
              B24FixField(gate.isoField[k], 4);
              break;
            case 18:
              B24FixField(gate.isoField[k], 4);
              break;
            case 19:
              B24FixField(gate.isoField[k], 3);
              break;
            case 20:
              B24FixField(gate.isoField[k], 3);
              break;
            case 21:
              B24FixField(gate.isoField[k], 3);
              break;
            case 22:
              B24FixField(gate.isoField[k], 3);
              break;
            case 23:
              B24FixField(gate.isoField[k], 3);
              break;
            case 24:
              B24FixField(gate.isoField[k], 3);
              break;
            case 25:
              B24FixField(gate.isoField[k], 2);
              break;
            case 26:
              B24FixField(gate.isoField[k], 2);
              break;
            case 27:
              B24FixField(gate.isoField[k], 1);
              break;
            case 28:
              B24FixField(gate.isoField[k], 8);
              break;
            case 29:
              B24FixField(gate.isoField[k], 8);
              break;
            case 30:
              B24FixField(gate.isoField[k], 8);
              break;
            case 31:
              B24FixField(gate.isoField[k], 8);
              break;
            case 32:
              B24Variable(gate.isoField[k], 2);
              break;
            case 33:
              B24Variable(gate.isoField[k], 2);
              break;
            case 34:
              B24Variable(gate.isoField[k], 2);
              break;
            case 35:
              B24Variable(gate.isoField[k], 2);
              break;
            case 36:
              B24Variable(gate.isoField[k], 3);
              break;
            case 37:
              B24FixField(gate.isoField[k], 12);
              break;
            case 38:
              B24FixField(gate.isoField[k], 6);
              break;
            case 39:
              B24FixField(gate.isoField[k], 2);
              break;
            case 40:
              B24FixField(gate.isoField[k], 3);
              break;
            case 41:
              B24FixField(gate.isoField[k], 16);
              break;
            case 42:
              B24FixField(gate.isoField[k], 15);
              break;
            case 43:
              B24FixField(gate.isoField[k], 40);
              break;
            case 44:
              B24Variable(gate.isoField[k], 2);
              break;
            case 45:
              B24Variable(gate.isoField[k], 2);
              break;
            case 46:
              B24FixField(gate.isoField[k], 3);
              break;
            case 47:
              B24FixField(gate.isoField[k], 3);
              break;
            case 48:
              B24Variable(gate.isoField[k], 3);
              break;
            case 49:
              B24FixField(gate.isoField[k], 3);
              break;
            case 50:
              B24FixField(gate.isoField[k], 3);
              break;
            case 51:
              B24FixField(gate.isoField[k], 3);
              break;
            case 52:
              B24FixField(gate.isoField[k], 16);
              break;
            case 53:
              B24FixField(gate.isoField[k], 16);
              break;
            case 54:
              B24FixField(gate.isoField[k], 12);
              break;
            case 55:
              B24Variable(gate.isoField[k], 3);
              break;
            case 56:
              B24Variable(gate.isoField[k], 3);
              break;
            case 57:
              B24Variable(gate.isoField[k], 3);
              break;
            case 58:
              B24Variable(gate.isoField[k], 3);
              break;
            case 59:
              B24Variable(gate.isoField[k], 3);
              break;
            case 60:
              B24Variable(gate.isoField[k], 3);
              break;
            case 61:
              B24Variable(gate.isoField[k], 3);
              break;
            case 62:
              B24Variable(gate.isoField[k], 3);
              break;
            case 63:
              B24Variable(gate.isoField[k], 3);
              break;
            case 64:
              B24FixField(gate.isoField[k], 16);
              break;
            case 65:
              B24FixField(gate.isoField[k], 16);
              break;
            case 66:
              B24FixField(gate.isoField[k], 1);
              break;
            case 67:
              B24FixField(gate.isoField[k], 2);
              break;
            case 68:
              B24FixField(gate.isoField[k], 3);
              break;
            case 69:
              B24FixField(gate.isoField[k], 3);
              break;
            case 70:
              B24FixField(gate.isoField[k], 3);
              break;
            case 71:
              B24FixField(gate.isoField[k], 4);
              break;
            case 72:
              B24FixField(gate.isoField[k], 4);
              break;
            case 73:
              B24FixField(gate.isoField[k], 6);
              break;
            case 74:
              B24FixField(gate.isoField[k], 10);
              break;
            case 75:
              B24FixField(gate.isoField[k], 10);
              break;
            case 76:
              B24FixField(gate.isoField[k], 10);
              break;
            case 77:
              B24FixField(gate.isoField[k], 10);
              break;
            case 78:
              B24FixField(gate.isoField[k], 10);
              break;
            case 79:
              B24FixField(gate.isoField[k], 10);
              break;
            case 80:
              B24FixField(gate.isoField[k], 10);
              break;
            case 81:
              B24FixField(gate.isoField[k], 10);
              break;
            case 82:
              B24FixField(gate.isoField[k], 12);
              break;
            case 83:
              B24FixField(gate.isoField[k], 12);
              break;
            case 84:
              B24FixField(gate.isoField[k], 12);
              break;
            case 85:
              B24FixField(gate.isoField[k], 12);
              break;
            case 86:
              B24FixField(gate.isoField[k], 16);
              break;
            case 87:
              B24FixField(gate.isoField[k], 16);
              break;
            case 88:
              B24FixField(gate.isoField[k], 16);
              break;
            case 89:
              B24FixField(gate.isoField[k], 16);
              break;
            case 90:
              B24FixField(gate.isoField[k], 42);
              break;
            case 91:
              B24FixField(gate.isoField[k], 1);
              break;
            case 92:
              B24FixField(gate.isoField[k], 2);
              break;
            case 93:
              B24FixField(gate.isoField[k], 5);
              break;
            case 94:
              B24FixField(gate.isoField[k], 7);
              break;
            case 95:
              B24FixField(gate.isoField[k], 42);
              break;
            case 96:
              B24FixField(gate.isoField[k], 16);
              break;
            case 97:
              B24FixField(gate.isoField[k], 17);
              break;
            case 98:
              B24FixField(gate.isoField[k], 25);
              break;
            case 99:
              B24Variable(gate.isoField[k], 2);
              break;
            case 100:
              B24Variable(gate.isoField[k], 2);
              break;
            case 101:
              B24Variable(gate.isoField[k], 2);
              break;
            case 102:
              B24Variable(gate.isoField[k], 2);
              break;
            case 103:
              B24Variable(gate.isoField[k], 2);
              break;
            case 104:
              B24Variable(gate.isoField[k], 3);
              break;
            case 105:
              B24Variable(gate.isoField[k], 3);
              break;
            case 106:
              B24Variable(gate.isoField[k], 3);
              break;
            case 107:
              B24Variable(gate.isoField[k], 3);
              break;
            case 108:
              B24Variable(gate.isoField[k], 3);
              break;
            case 109:
              B24Variable(gate.isoField[k], 3);
              break;
            case 110:
              B24Variable(gate.isoField[k], 3);
              break;
            case 111:
              B24Variable(gate.isoField[k], 3);
              break;
            case 113:
              B24Variable(gate.isoField[k], 3);
              break;
            case 114:
              B24Variable(gate.isoField[k], 3);
              break;
            case 115:
              B24Variable(gate.isoField[k], 3);
              break;
            case 116:
              B24Variable(gate.isoField[k], 3);
              break;
            case 117:
              B24Variable(gate.isoField[k], 3);
              break;
            case 118:
              B24Variable(gate.isoField[k], 3);
              break;
            case 119:
              B24Variable(gate.isoField[k], 3);
              break;
            case 120:
              B24Variable(gate.isoField[k], 3);
              break;
            case 121:
              B24Variable(gate.isoField[k], 3);
              break;
            case 122:
              B24Variable(gate.isoField[k], 3);
              break;
            case 123:
              B24Variable(gate.isoField[k], 3);
              break;
            case 124:
              B24Variable(gate.isoField[k], 3);
              break;
            case 125:
              B24Variable(gate.isoField[k], 3);
              break;
            case 126:
              B24Variable(gate.isoField[k], 3);
              break;
            case 127:
              B24Variable(gate.isoField[k], 3);
              break;
            case 128:
              B24Variable(gate.isoField[k], 3);
              break;
            default:
              break;
          }
        }
      }

      setIsoData(isoString);
      /*
       * gate.isoData = isoString.getBytes();
       * 
       * //System.out.println("---IsoStr is=>"+ gate.isoString + "----"); gate.totalLen =
       * gate.isoData.length; gate.dataLen = gate.totalLen - gate.initPnt; gate.isoData[0] =
       * (byte)(gate.dataLen / 256); gate.isoData[1] = (byte)(gate.dataLen % 256);
       * 
       * gate.isoString = isoString;
       */
    } // end of try
    catch (Exception ex) {
      expHandle(ex);
      return false;
    }
    return true;
  }

  public boolean host2Iso(String sP_IsoCommand) {

    try {
      convertBicField("I");



      setIsoData("  " + sP_IsoCommand);
    } // end of try
    catch (Exception ex) {
      expHandle(ex);
      return false;
    }
    return true;
  }

  public void setIsoData(String sP_IsoStr) {
    try {
    	gate.isoData = sP_IsoStr.getBytes("UTF-8");
    } catch(Exception e) {
    	e.printStackTrace();
    }	  
    // System.out.println("---IsoStr is=>"+ gate.isoString + "----");
    gate.totalLen = gate.isoData.length;
    gate.dataLen = gate.totalLen - gate.initPnt;
    gate.isoData[0] = (byte) (gate.dataLen / 256);
    gate.isoData[1] = (byte) (gate.dataLen % 256);

    gate.isoString = isoString;

  }

  private void setHeaderMap() {
    int i = 0, k = 0;
    char[] map = new char[128];
    for (i = 0; i < 128; i++) {
      map[i] = '0';
    }

    if (gate.bicHead.length() != 12) {
      gate.bicHead = "ISO026000000";
    }

    if (gate.mesgType.length() != 4) {
      gate.mesgType = "XXXX";
    }

    isoString = spaces.substring(0, gate.initPnt) + gate.bicHead;
    offset = 14;

    for (k = 2; k < 128; k++) {

      if (gate.isoField[k].length() > 0) {
        map[k - 1] = '1';
      }

      if (gate.isoField[k].length() > 0 && k > 64) {
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

  private void B24Variable(String fieldData, int len) {
    String zeros = "00000000", tempStr = "";
    int fieldLen = 0;

    fieldLen = fieldData.length();
    tempStr = String.valueOf(fieldLen);
    if (tempStr.length() < len) {
      tempStr = zeros.substring(0, len - tempStr.length()) + tempStr;
    }
    isoString = isoString + tempStr + fieldData;
    offset = offset + len + fieldLen;
    tempStr = null;
  }

  private void B24FixField(String fieldData, int len) {
    if (fieldData.length() < len) {
      fieldData = fieldData + spaces.substring(0, len - fieldData.length());
    }

    isoString = isoString + fieldData.substring(0, len);
    offset += len;
  }

  public boolean convertBicField(String cvtCode) {
    if (cvtCode.equals("C")) {
      if ((gate.isoField[63].length() > 0) || (gate.isoField[126].length() > 0)) {
        TokenObject token = new TokenObject();
        token.decodeTokenData(gate);
        if (token.tokenIdB2.equals("B2") || token.tokenIdB5.equals("B5")) {
          gate.emvTrans = true;
        }
      }
    } else {
      if (gate.ProdMode) {
        if (gate.bicHead.length() != 12) {
          gate.bicHead = "ISO026000000";
        }
        TokenObject token = new TokenObject();

        if (gate.isoField[126].length() > 0) {
          gate.isoField[126] = token.createTokenData(gate);
          gate.isoField[63] = "";
        } else {
          gate.isoField[63] = token.createTokenData(gate);
          gate.isoField[126] = "";
        }
      }
    }


    return true;
  }

  public void expHandle(Exception ex) {
    try {
      logger.fatal(" >> ####### BicFormat Exception MESSAGE STARTED ######");
      logger.fatal("BicFormat Exception_Message : ", ex);
      logger.fatal(" >> ####### BicFormat system Exception MESSAGE   ENDED ######");

    } catch (Exception e) {
      // TODO: handle exception
    }
    return;
  }

} // Class BA24 End
