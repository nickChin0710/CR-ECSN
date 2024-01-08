package bank.AuthIntf;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.logging.log4j.core.Logger;

import bank.Auth.HpeUtil;

public class NegFormat extends ConvertMessage implements FormatInterChange {

    public  String   byteMap="",isoString="",retCode="";
    public  String   zeros="",spaces="",fiid="",dpcNum="",lNet="";
    private int offset = 0, k = 0;

    public NegFormat(Logger logger,AuthGate gate,HashMap cvtHash) {
        super.logger  = logger;
        super.gate    = gate;
        super.cvtHash = cvtHash;
    }

    /* NEG change to ECS format*/
	public boolean iso2Host() {
		try {

			String cvtString = "";
			int cnt = 0;

			isoString = new String(gate.isoData, 0, gate.dataLen);

			offset = 0;
			gate.isoString = isoString;

			gate.mesgType = isoString.substring(offset, offset + 4);
			offset += 4;

			for (k = 2; k <= 15; k++) {
					switch (k) {
					case 2:
						gate.isoField[k] = getIsoFixLenStrToHost(1, false);
						break;
					case 3:
						gate.isoField[k] = getIsoFixLenStrToHost(1, false);
						break;
					case 4:
						gate.isoField[k] = getIsoFixLenStrToHost(16, false);
						break;
					case 5:
						gate.isoField[k] = getIsoFixLenStrToHost(3, false);
						break;
					case 6:
						gate.isoField[k] = getIsoFixLenStrToHost(2, false);
						break;
					case 7:
						gate.isoField[k] = getIsoFixLenStrToHost(1, false);
						break;
					case 8:
						gate.isoField[k] = getIsoFixLenStrToHost(13, false);
						break;
					case 9:
						gate.isoField[k] = getIsoFixLenStrToHost(12, false);
						break;
					case 10:
						gate.isoField[k] = getIsoFixLenStrToHost(4, false);
						break;
					case 11:
						gate.isoField[k] = getIsoFixLenStrToHost(8, false);
						break;
					case 12:
						gate.isoField[k] = getIsoFixLenStrToHost(8, false);
						break;
					case 13:
						gate.isoField[k] = getIsoFixLenStrToHost(2, false);
						break;
					case 14:
						gate.isoField[k] = getIsoFixLenStrToHost(6, false);
						break;
					case 15:
						gate.isoField[k] = getIsoFixLenStrToHost(54, false);
						break;
					default:
						break;
					}
					//System.out.println("gate.isoField["+k+"] : "+gate.isoField[k]);
			}

		} // end of try
		catch (Exception ex) {
			expHandle(ex);
			return false;
		}
		return true;

	}
  //kevin:取iso固定長欄位並轉碼
    private String getIsoFixLenStrToHost(int len, boolean bP_IsEbcdic) throws Exception{
    	String fieldData = "";

    	byte[] L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, len);
    	if (bP_IsEbcdic)
    		fieldData = HpeUtil.ebcdic2Str(L_TmpAry);
    	else
    		fieldData = new String(L_TmpAry, 0, L_TmpAry.length);

    	System.out.println("fieldData = [" + fieldData + "]==========================================");
    	
    	offset += len;
    	return fieldData;
    }


    /*ECS change to NEG format  */
    public boolean host2Iso()  {

     try {

//        convertBicField("I");

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
        offset    = 2;

        isoString = isoString + gate.mesgType;
        offset += 4;
        
        for (k = 2; k <= 15; k++) {
                switch (k) {
                case 2:
                	B24FixField(gate.isoField[k], 1);
                    break;
                case 3:
                    B24FixField(gate.isoField[k], 1);
                    break;
                case 4:
                	B24Variable(gate.isoField[k], 16);
                    break;
                case 5:
                    B24FixField(gate.isoField[k], 3);
                    break;
                case 6:
                    B24FixField(gate.isoField[k], 2);
                    break;
                case 7:
                    B24FixField(gate.isoField[k], 1);
                    break;
                case 8:
                    B24FixField(gate.isoField[k], 13);
                    break;
                case 9:
                	B24Variable(gate.isoField[k], 12);
                    break;
                case 10:
                	B24Variable(gate.isoField[k], 4);
                    break;
                case 11:
                	B24FixField(gate.isoField[k], 8);
                    break;
                case 12:
                	B24FixField(gate.isoField[k], 8);
                    break;
                case 13:
                    B24FixField(gate.isoField[k], 2);
                    break;
                case 14:
                    B24FixField(gate.isoField[k], 6);
                    break;
                case 15:
                    B24FixField(gate.isoField[k], 54);
                    break;
                default:
                    break;
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

    private void B24Variable(String fieldData, int len) {
        if (fieldData.length() < len) {
            fieldData = fieldData + zeros.substring(0, len - fieldData.length());
        }

        isoString = isoString + fieldData.substring(0, len);
        offset += len;
    }
    
    private void B24FixField(String fieldData, int len) {
        if (fieldData.length() < len) {
            fieldData = fieldData + spaces.substring(0, len - fieldData.length());
        }

        isoString = isoString + fieldData.substring(0, len);
        offset += len;
    }

    private String hostFixField(int len) {
        String fieldData = "";
        fieldData = isoString.substring(offset, offset + len);
        offset += len;
        return fieldData;
    }

	private String byteMap2Byte(String src, int size) {
		char[] destChar = new char[33];
		char[] cvt = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
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

	private String byte2ByteMap(String src, int size) {
		byte[] srcByte = new byte[65];
		String[] cvt = { "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
				"1100", "1101", "1110", "1111" };
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

	public void expHandle(Exception ex) {
		logger.fatal(" >> ####### BicFormat Exception MESSAGE STARTED ######");
		logger.fatal("BicFormat Exception_Message : ", ex);
		logger.fatal(" >> ####### BicFormat system Exception MESSAGE   ENDED ######");
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
//	    try {
			gate.isoData = sP_IsoStr.getBytes();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	    // System.out.println("---IsoStr is=>"+ gate.isoString + "----");
	    gate.totalLen = gate.isoData.length;
	    gate.dataLen = gate.totalLen - gate.initPnt;
	    gate.isoData[0] = (byte) (gate.dataLen / 256);
	    gate.isoData[1] = (byte) (gate.dataLen % 256);
	
	    gate.isoString = isoString;
	
	  }


} // Class FISC NEG End
