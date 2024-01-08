// java -cp MegaAuthIntf.jar mega.AuthIntf.TestProg 127.0.0.1 6050
// java -cp MegaAuthIntf.jar mega.AuthIntf.TestProg 127.0.0.1 18001
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*  111-01-19  V1.00.02  Justin      fix J2EE Bad Practices: Leftover Debug Code 
******************************************************************************/
package bank.AuthIntf;



public class TestProg {

  public TestProg() {
    // TODO Auto-generated constructor stub
  }

// //fix J2EE Bad Practices: Leftover Debug Code
//  public static void main(String[] args) {
//    // TODO Auto-generated method stub
//    AuthData L_AuthData = new AuthData();
//    // down, assign auth data ...
//    // L_AuthData.setFullIsoCommand("ISO02600000002003238C48128E18008000000000000012300031916161505698516161503190319998800 0006493817335542123456780987=1812        9911000000000000                1234567890     NATIONAL CREDIT CARD  TAIPEI       TW TW027027010800431         000100901019BK77PRO200000000000");
//
//    L_AuthData.setCardNo("4312349000000048");
//    L_AuthData.setCvv2("999");
//    L_AuthData.setExpireDate("20230129"); // YYYYMMDD
//    L_AuthData.setLocalTime("20171109171020"); // yyyymmdzdhhmmss when trans_type=3
//                                                // //Howard:不知道此欄位要放到 ISO 的哪個欄位中.....
//    L_AuthData.setMccCode("7011"); // bit18 mcc code
//    L_AuthData.setMchtNo("0108000333     ");// bit42 acceptor_id=mcht_no
//    L_AuthData.setOrgAuthNo("");
//    L_AuthData.setOrgRefNo("");
//    L_AuthData.setTransAmt("102");
//    L_AuthData.setTransType("1");// 1: regular 2:refund 3:reversal 4:代行
//    L_AuthData.setTypeFlag("I");// A: install B: mail => 改為 I: install M: mail C:公共事業
//
//    // up, assign auth data ...
//
//    String sL_IP = args[0];
//    String sL_Port = args[1];
//
//    // System.out.println("V1:" + sL_IP);
//    // System.out.println("V2:" + sL_Port);
//    String sL_TranxResult = "";
//    try {
//      // down, 單筆授權
//
//      AuthGateway AuthGatewayTest = new AuthGateway();
//
//      sL_TranxResult = AuthGatewayTest.startProcess(L_AuthData, sL_IP, sL_Port);
//      // 回傳 8 碼= > 2碼response code + 6碼授權碼 + Field[120]
//
//      AuthGatewayTest = null;
//
//
//      System.out.println("Process result [ IsoField39+38+73+120 ] =>" + sL_TranxResult + "--");
//
//      // up, 單筆授權
//
//    } catch (Exception e) {
//      // TODO: handle exception
//      System.out.println("Exception:" + e.getMessage());
//    }
//
//    /*
//     * String sL_PropertyFilePath="C:/temp"; // 參數檔存放路徑 sL_TranxResult =
//     * AuthGatewayTest.startProcess(L_AuthData, sL_PropertyFilePath);
//     */
//
//    /*
//     * //down, 批次授權 try { AuthGateway AuthGateway4Batch = new AuthGateway();
//     * 
//     * AuthGateway4Batch.initConnection("127.0.0.1", "15001");
//     * 
//     * sL_TranxResult = AuthGateway4Batch.sendAuthData(L_AuthData);
//     * System.out.println("交易結果IsoField39:" + sL_TranxResult + "--");
//     * 
//     * 
//     * sL_TranxResult = AuthGateway4Batch.sendAuthData(L_AuthData);
//     * System.out.println("交易結果IsoField39:" + sL_TranxResult + "--");
//     * 
//     * sL_TranxResult = AuthGateway4Batch.sendAuthData(L_AuthData);
//     * System.out.println("交易結果IsoField39:" + sL_TranxResult + "--");
//     * 
//     * sL_TranxResult = AuthGateway4Batch.sendAuthData(L_AuthData);
//     * System.out.println("交易結果IsoField39:" + sL_TranxResult + "--");
//     * 
//     * sL_TranxResult = AuthGateway4Batch.sendAuthData(L_AuthData);
//     * System.out.println("交易結果IsoField39:" + sL_TranxResult + "--");
//     * 
//     * sL_TranxResult = AuthGateway4Batch.sendAuthData(L_AuthData);
//     * System.out.println("交易結果IsoField39:" + sL_TranxResult + "--");
//     * 
//     * sL_TranxResult = AuthGateway4Batch.sendAuthData(L_AuthData);
//     * System.out.println("交易結果IsoField39:" + sL_TranxResult + "--");
//     * 
//     * sL_TranxResult = AuthGateway4Batch.sendAuthData(L_AuthData);
//     * System.out.println("交易結果IsoField39:" + sL_TranxResult + "--");
//     * 
//     * 
//     * AuthGateway4Batch.releaseConnection(); AuthGateway4Batch=null;
//     * 
//     * } catch (Exception e) { // TODO: handle exception
//     * System.out.println("Batch auth failed! Exception:" + e.getMessage()); }
//     */
//
//
//    // up, 批次授權
//
//  }

}
