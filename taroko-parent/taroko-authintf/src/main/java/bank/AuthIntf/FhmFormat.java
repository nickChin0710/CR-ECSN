package bank.AuthIntf;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;


import org.apache.logging.log4j.core.Logger;




public class FhmFormat extends ConvertMessage implements FormatInterChange {

    public  String   byteMap="",isoString="",retCode="";
    public  String   zeros="",spaces="",fiid="",dpcNum="",lNet="";
    private int offset = 0, k = 0;

    public FhmFormat(Logger logger,AuthGate gate,HashMap cvtHash) {
        super.logger  = logger;
        super.gate    = gate;
        super.cvtHash = cvtHash;
    }

    /* �N FISC FHM �榡�ର�D���榡���  */
    public boolean iso2Host(){
      try {

        String cvtString = "";
        int cnt = 0;

        isoString    = new String(gate.isoData,0,gate.dataLen);

        offset  = 0;
        gate.isoString = isoString;

//        gate.fhmHead = isoString.substring(0, 12);
//        offset  = 12;

        gate.mesgType = isoString.substring(offset, offset + 4);
        offset += 4;

        cvtString = isoString.substring(offset, offset + 16);
        byteMap = byte2ByteMap(cvtString, 16);
        gate.iso_bitMap =byteMap;
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
                    gate.isoField[k] = hostFixField(9);
                    break;
                case 29:
                    gate.isoField[k] = hostFixField(9);
                    break;
                case 30:
                    gate.isoField[k] = hostFixField(9);
                    break;
                case 31:
                    gate.isoField[k] = hostFixField(9);
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
                    gate.isoField[k] = hostFixField(8);
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
                    gate.isoField[k] = hostFixField(8);
                    break;
                case 54:
                    gate.isoField[k] = hostVariable(3);
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
                    gate.isoField[k] = hostFixField(16);
                    break;
                case 126:
                    gate.isoField[k] = hostVariable(3);
                    break;
                case 127:
                    gate.isoField[k] = hostVariable(3);
                    break;
                case 128:
                    gate.isoField[k] = hostFixField(16);
                    break;
                default:
                    break;
                }
            }
        }


        /* FHM �榡 �ഫ�� �@�P�榡  */
        /* Jack : �Τ����o...
          if ( !convertToCommon() )
             { return false; }
         */
//          convertFhmField("C"); //mark by Howard

       } // end of try
       catch ( Exception ex )
            { expHandle(ex); return false; }
       return true;

    }

    private String byte2ByteMap(String src, int size) {
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



    /* �N�D���榡����ର FISC FHM �榡 */
    public boolean host2Iso()  {

     try {
    	 /* Jack : �Τ����o...
        if ( !convertToInterChange() )
           { return false; }
           */
//        convertFhmField("I");

        String cvtString = "";
        int    cnt = 0;

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

        k = 0;
        isoString = "  ";
        offset = 2;
//        if (gate.isoField[101].equals("CF"))      formatOutCAF();
//        else if (gate.isoField[101].equals("DA")) formatOutPBF();
//        else if (gate.isoField[101].equals("NF")) formatOutNEG();
//        else if (gate.isoField[101].equals("VP")) formatOutVISA();

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
                    B24FixField(gate.isoField[k], 8);
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
                    B24FixField(gate.isoField[k], 8);
                    break;
                case 54:
                    B24Variable(gate.isoField[k], 3);
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
                    B24FixField(gate.isoField[k], 16);
                    break;
                case 126:
                    B24Variable(gate.isoField[k], 3);
                    break;
                case 127:
                    B24Variable(gate.isoField[k], 3);
                    break;
                case 128:
                    B24FixField(gate.isoField[k], 16);
                    break;
                default:
                    break;
                }
            }
        }

        gate.isoData     = isoString.getBytes(); //Howard: isoString 的前兩碼是空白
        
        //System.out.println("---IsoStr is=>"+ gate.isoString + "----");
        gate.totalLen    = gate.isoData.length;
        gate.dataLen     = gate.totalLen - gate.initPnt;
        gate.isoData[0]  = (byte)(gate.dataLen / 256);
        gate.isoData[1]  = (byte)(gate.dataLen % 256);

        gate.isoString = isoString;
       } // end of try
       catch ( Exception ex )
            { expHandle(ex); return false; }
       return true;
    }

    private void setHeaderMap() {
        int i = 0, k = 0;
        char[] map = new char[128];
        for (i = 0; i < 128; i++) {
            map[i] = '0';
        }

//        if ( gate.fhmHead.length() != 12 )
//           { gate.fhmHead = "ISO026000000"; }

        if ( gate.mesgType.length() != 4 )
           { gate.mesgType = "XXXX"; }

        isoString = spaces.substring(0,gate.initPnt);
        offset    = 2;

        for (k = 2; k <128; k++) {
        	
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
        tempStr  = String.valueOf(fieldLen);
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


	public void expHandle (Exception ex)
	{
	    logger.fatal(" >> ####### FhmFormat Exception MESSAGE STARTED ######");
	    logger.fatal("FhmFormat Exception_Message : ", ex);
	    logger.fatal(" >> ####### FhmFormat system Exception MESSAGE   ENDED ######");
	    return;
	}

	public boolean host2Iso(String sP_IsoCommand) {
	    try {
	        setIsoData("  " + sP_IsoCommand);
	      } // end of try
	      catch (Exception ex) {
	        expHandle(ex);
	        return false;
	      }
		return false;
	}
	
	public void setIsoData(String sP_IsoStr) {
	    gate.isoData = sP_IsoStr.getBytes();
	
	    // System.out.println("---IsoStr is=>"+ gate.isoString + "----");
	    gate.totalLen = gate.isoData.length;
	    gate.dataLen = gate.totalLen - gate.initPnt;
	    gate.isoData[0] = (byte) (gate.dataLen / 256);
	    gate.isoData[1] = (byte) (gate.dataLen % 256);
	
	    gate.isoString = isoString;
	
	  }

} // Class FISC End
