/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*  109-09-13  V1.00.01  Zuwei       fix code scan issue      *
*  109-09-28  V1.00.01  Zuwei       fix code scan issue      *
*  111-03-16  V1.00.02  Justin      prevent from infinite loop               *  
*  112-11-03  V1.00.03	Kevin       Auth batch Fix                           *
*  112-12-08  V1.00.04	Kevin       批次授權註記新增                              *
*  113-01-03  V1.00.05  Kevin       批次授權專用註記新增(bG_BatchAuth)             *
******************************************************************************/
package bank.AuthIntf;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;


// public class AuthProcess implements Runnable {
public class AuthProcess extends Thread {

  boolean bG_GetResponse = false, bG_ExceptionOccurred = false;
  String sG_IP, sG_Port, sG_FullIsoCommand;
  Socket G_AuthSocket = null;
  boolean bG_BatchProcess = false;
  boolean bG_BatchAuth = false;

//  String chkFhmNeg = "";
  
  AuthGlobalParm G_GlobalParm = null;
  AuthGate G_Gate = null;

  //FISC
  public AuthProcess(AuthGate P_Gate, AuthGlobalParm P_Gb, String sP_IP, String sP_Port,
	      String sP_FullIsoCommand,String chkFhmNeg) throws Exception {
	    // TODO Auto-generated constructor stub
	    sG_IP = sP_IP;
	    sG_Port = sP_Port;
	    G_Gate = P_Gate;
	    G_GlobalParm = P_Gb;
	    sG_FullIsoCommand = sP_FullIsoCommand;
	    bG_BatchProcess = false;
	    bG_BatchAuth = false;
	    if (!G_Gate.isoField[26].isEmpty()) {
		    if (!"C".equals(G_Gate.isoField[26])) {
		    	bG_BatchAuth = true;
		    }
	    }
	    G_Gate.chkFhmNeg = chkFhmNeg;
	    
        System.out.println("AuthProcess sG_IP:" + sG_IP +",sG_Port:"+sG_Port+",isNeg:"+G_Gate.chkFhmNeg+",sP_FullIsoCommand:"+sP_FullIsoCommand);

  }
  
  public AuthProcess(AuthGate P_Gate, AuthGlobalParm P_Gb, String sP_IP, String sP_Port,
      String sP_FullIsoCommand) throws Exception {
    // TODO Auto-generated constructor stub
    sG_IP = sP_IP;
    sG_Port = sP_Port;
    G_Gate = P_Gate;
    G_GlobalParm = P_Gb;
    sG_FullIsoCommand = sP_FullIsoCommand;
    bG_BatchProcess = false;

  }

  public AuthProcess(AuthGate P_Gate, AuthGlobalParm P_Gb, String sP_FullIsoCommand,
      Socket P_AuthSocket) throws Exception {
    // TODO Auto-generated constructor stub
    G_Gate = P_Gate;
    G_GlobalParm = P_Gb;
    sG_FullIsoCommand = sP_FullIsoCommand;
    G_AuthSocket = P_AuthSocket;
    bG_BatchProcess = true;
  }

  private volatile String sG_ResponseFromAuthAuth;


  public void run() {
	  Socket L_Socket = null;
    try {
      if (bG_BatchProcess)
        L_Socket = G_AuthSocket;
      else
        L_Socket = new Socket(sG_IP, Integer.parseInt(sG_Port));
      
      System.out.println("AuthProcess run L_Socket:" + L_Socket);

      L_Socket.setSoTimeout(6 * 1000);
      try (BufferedOutputStream L_OutputStream = new BufferedOutputStream(L_Socket.getOutputStream());

    		  BufferedInputStream L_InputStream = new BufferedInputStream(L_Socket.getInputStream());) {

      FormatInterChange intr = null;
      if (G_Gate.chkFhmNeg!= null && G_Gate.chkFhmNeg.equals("FHM")) {
          intr = new FhmFormat(G_GlobalParm.logger, G_Gate, G_GlobalParm.cvtHash);
      }
      else if (G_Gate.chkFhmNeg!= null && G_Gate.chkFhmNeg.equals("NEG")) {
          intr = new NegFormat(G_GlobalParm.logger, G_Gate, G_GlobalParm.cvtHash);
      }
      else {
    	  intr = new BicFormat(G_GlobalParm.logger, G_Gate, G_GlobalParm.cvtHash);
          System.out.println("AuthProcess run intr:" + intr);

      }

      if ("".equals(sG_FullIsoCommand)) {
        intr.host2Iso();
      } else {
        intr.host2Iso(sG_FullIsoCommand);
      }
       G_GlobalParm.showLogMessage("D","Send out IsoString is=>" + G_Gate.isoString , "---");


      L_OutputStream.write(G_Gate.isoData, 0, G_Gate.totalLen);
      L_OutputStream.flush();


      int headLen = 0, inputLen = 0, packetLen = 0;
      byte[] authData = new byte[2048];
      byte[] lenData = new byte[3];

      int nL_Tmp = 1;
      String sL_ReceiveData = "";
      while (true) {
        // System.out.println(nL_Tmp);

        headLen = L_InputStream.read(lenData, 0, 2);

        if (headLen != 2) {
          // 這裡會進來....1201,
		  if (headLen == -1) {
			// fail to connect
//			System.out.println("headLen == -1 fail to connect");
	        if (bG_BatchAuth) {
	        	G_Gate.isoData = null;
	            G_Gate.isoData = new byte[2048];
	            G_Gate.isoField[39] = "98";
	            bG_GetResponse = true;
	        }
			break;
		  }
          continue;
        }


        /* 從 SOCKET 讀取交易資料 */
        packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);

        inputLen = L_InputStream.read(authData, 0, packetLen);

        G_Gate.isoData = null;
        G_Gate.isoData = new byte[2048];
        /* 轉換 ISO 格式 為 主機格式 */
        for (int k = 0; k < inputLen; k++) {
          G_Gate.isoData[k] = authData[k];
        }
        G_Gate.dataLen = inputLen;
        String sL_Tmp = new String(authData, 0, inputLen);
        // System.out.println("Data received is =>" + sL_Tmp + "===");


        FormatInterChange intr2 = null;
        if (G_Gate.chkFhmNeg!= null && G_Gate.chkFhmNeg.equals("FHM")) {
            intr2 = new FhmFormat(G_GlobalParm.logger, G_Gate, G_GlobalParm.cvtHash);
        }
        else if (G_Gate.chkFhmNeg!= null && G_Gate.chkFhmNeg.equals("NEG")) {
            intr2 = new NegFormat(G_GlobalParm.logger, G_Gate, G_GlobalParm.cvtHash);
        }
        else {
        	intr2 = new BicFormat(G_GlobalParm.logger, G_Gate, G_GlobalParm.cvtHash);
        }
//    	    intr2 = new BicFormat(G_GlobalParm.logger, G_Gate, G_GlobalParm.cvtHash);
        if (!intr2.iso2Host()) {

          G_Gate.isoField[39] = "30";
          // G_GlobalParm.showLogMessage("E","ISO-8583 CONVERT ERROR ","");

        } else {
          sG_ResponseFromAuthAuth = G_Gate.isoString;
          nL_Tmp++;
          // System.out.println("receive data from socket server:" + sG_ResponseFromAuthAuth +
          // "--");

        }
        bG_GetResponse = true;
        break;


      }
      }

    } catch (Exception e) {
      // TODO: handle exception
        if (bG_BatchAuth) {
        	G_Gate.isoData = null;
            G_Gate.isoData = new byte[2048];
            G_Gate.isoField[39] = "98";
            bG_GetResponse = true;
        }
        else {
        	bG_ExceptionOccurred = true;
        }
      // System.out.println("AuthProcess exception:" + e.getMessage());

    } finally {
    	if (!bG_BatchProcess) {
//          try {
//			L_OutputStream.close();
//		} catch (IOException e) {
//		}
//          try {
//			L_InputStream.close();
//		} catch (IOException e) {
//		}
				if (L_Socket != null) {
					try {
						L_Socket.close();
					} catch (IOException e) {
					}
				}
	    }
    }
    // System.out.println("AuthProcess completed!!");
  }

  public String getAuthResult() {
	  if (G_Gate.chkFhmNeg!= null && G_Gate.chkFhmNeg.equals("NEG")) {
		  // neg return sG_ResponseFromAuthAuth; //回傳完整的 ISO neg String
		  System.out.println("NEG sL_IsoField13 ="+HpeUtil.fillZeroOnLeft(G_Gate.isoField[13], 2));
		  String sL_IsoField39 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[13], 2);
		  String sL_IsoField38 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[38], 6);
		  String sL_IsoField73 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[73], 6);
		  String sL_IsoField92 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[92], 2);
		  String sL_IsoField120 = G_Gate.isoField[120];
		  return sL_IsoField39 + sL_IsoField38 + sL_IsoField73 + sL_IsoField92 + sL_IsoField120; // 回傳 ISO Field 39(2
	  }
	  else {
		  // return sG_ResponseFromAuthAuth; //回傳完整的 ISO String
		  String sL_IsoField39 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[39], 2);
		  String sL_IsoField38 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[38], 6);
		  String sL_IsoField73 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[73], 6);
		  String sL_IsoField92 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[92], 2);
		  String sL_IsoField120 = G_Gate.isoField[120];
		  return sL_IsoField39 + sL_IsoField38 + sL_IsoField73 + sL_IsoField92 + sL_IsoField120; // 回傳 ISO Field 39(2
	  }

                                                                           // bytes), 38(6 bytes),
                                                                           // 73(6 bytes), 120
  }

}
