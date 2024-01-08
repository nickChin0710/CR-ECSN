/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package busi;

public class CommBusi {

  public final String TSCC_bill_type = "TSCC";
  public final String BK_ID_NCCC = "BK02";
  public final String ccas_LOG0300 = "1";
  // "BK_ICA"="3383"
  public final String ccas_BK_ICA = "3768";
  public final String gs_BK_name = "TCB";
  public final String gs_BK_city = "Taipei";

  // -rsk.新舊控制流水號-
  public String ctrlSeqno(String aCtrlSeqno, String aBinType) {
    if (aCtrlSeqno.length() > 6) {
      return aCtrlSeqno;
    }

    return aCtrlSeqno + aBinType;
  }

  // -rsk.帳務控制流號-
  public String rskCtrlSeqnoPrbl(String aCtrlSeqno, String aBinType, String rskStatus) {
    String lsCtrlSeqno = aCtrlSeqno + aBinType;
    if (aCtrlSeqno.length() > 6) {
      lsCtrlSeqno = aCtrlSeqno;
    }
    // --問交
    return lsCtrlSeqno + "-PR" + rskStatus;
  }

  public String rskCtrlSeqnoRept(String aCtrlSeqno, String aBinType, String rskStatus) {
    String lsCtrlSeqno = aCtrlSeqno + aBinType;
    if (aCtrlSeqno.length() > 6) {
      lsCtrlSeqno = aCtrlSeqno;
    }
    // --調單
    return lsCtrlSeqno + "-RE" + rskStatus;
  }

  public String rskCtrlSeqnoChgb(String aCtrlSeqno, String aBinType, String aStage,
      String aStatus) {
    String lsCtrlSeqno = aCtrlSeqno + aBinType;
    if (aCtrlSeqno.length() > 6) {
      lsCtrlSeqno = aCtrlSeqno;
    }
    // -扣款-
    return lsCtrlSeqno + "-CB" + aStage + aStatus;
  }

  // --
  public int txSign(String aTxn) {
    if ("|06,25,27,28,29".indexOf(aTxn) > 0) {
      return -1;
    }
    return 1;
  }

  public boolean isDebit(String debitFlag) {
    return debitFlag.equalsIgnoreCase("Y");
  }
}
