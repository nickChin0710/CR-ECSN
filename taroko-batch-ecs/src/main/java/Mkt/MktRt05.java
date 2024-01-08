/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/09/12 V1.00.00  Zuwei Su    program initial                            *
*  112/09/25 V1.00.01  Zuwei Su    調整欄位寬度                               *
*  112/09/25 V1.00.02  Holmes      前日剩餘總點數取當日營業日                 *
*  112/09/26 V1.00.03  Zuwei Su    本日優惠活動折抵點數第二個sql table錯誤，本日優惠活動折抵點數sql not in 改 in                 *
*  112/10/20 V1.00.04  Holmes      調整移除點數tran_code '6'                  *
*  112/11/25 V1.00.05  Zuwei Su    增加log輸出                  *
******************************************************************************/
package Mkt;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommRoutine;

public class MktRt05 extends AccessDAO {
  private final String PROGNAME = "信用卡紅利點數給點及兌換情形統計表程式 112/10/20 V1.00.04";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommDate commDate = new CommDate();
  CommCrdRoutine comcr = null;
  CommRoutine comr = null;

  int DEBUG = 1;
  String hTempUser = "";

  int reportPageLine = 45;
  String prgmId = "MktRt05";

  String rptIdR1 = "CRD121";
  String rptName1 = "信用卡紅利點數給點及兌換情形統計表";
  int pageCnt1 = 0, lineCnt1 = 0;
  int rptSeq1 = 0;
  List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

  String buf = "";
  long ydayBalbp = 0;
  long newBp = 0;
  long exchgBp = 0;
  long removeBp = 0;
  long newBp2 = 0;
  long redeemBp = 0;
  long todayBalbp = 0;
  int totCnt = 0;

  String hBusiBusinessDate = "";
  String hFirstDay = "";
  String hCallBatchSeqno = "";
  String hChiYymmdd = "";
  String tmp = "";

  /***********************************************************************/
  public int mainProcess(String[] args) {
    try {
      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + PROGNAME);
      showLogMessage("I", "", "Usage MktRt05 [business_date]");

      if (!connectDataBase()) {
          comc.errExit("connect DataBase error", "");
      }
      // =====================================
      comr = new CommRoutine(getDBconnect(), getDBalias());
      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      // get searchDate
      if (args.length >= 1) {
        hBusiBusinessDate = args[0];
          showLogMessage("I", "", String.format("程式參數1: [%s]", hBusiBusinessDate));
      } else {
        hBusiBusinessDate = selectPtrBusinday();
      }

      if (!commDate.isDate(hBusiBusinessDate)) {
          showLogMessage("I", "", "請傳入參數合格值yyyymmdd");
          return -1;
      }

      showLogMessage("I", "", String.format("執行日期[%s]", hBusiBusinessDate));
      hChiYymmdd = commDate.toTwDate(hBusiBusinessDate);

      selectDetail();

      headFile();
      writeFile();
      tailFile();

      // 改為線上報表
      // String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), prgmId);
      // filename = Normalizer.normalize(filename, Normalizer.Form.NFKD);
      // comc.writeReport(filename, lpar1);
      showLogMessage("I", "", String.format("寫入兌換入帳明細表結束, 總計筆數[%s]", totCnt));
      // 寫入ptr_batch_rpt
      comcr.deletePtrBatchRpt(rptIdR1, sysDate);
      comcr.insertPtrBatchRpt(lpar1);

      // ==============================================
      // 固定要做的
      comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
      showLogMessage("I", "", comcr.hCallErrorDesc);
      if (comcr.hCallBatchSeqno.length() == 20) {
        comcr.callbatch(1, 0, 1); // 1: 結束
      }

      finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
    }
  }

  private String selectPtrBusinday() throws Exception {
      sqlCmd = "select BUSINESS_DATE from PTR_BUSINDAY ";
      selectTable();

      if (notFound.equals("Y")) {
          comc.errExit("執行結束, 營業日為空!!", "");
      }

      return getValue("BUSINESS_DATE");
  }

  /***********************************************************************/
  void selectDetail() throws Exception {
    String lastDate = commDate.dateAdd(hBusiBusinessDate, 0, 0, -1);
    // 前日剩餘總點數 - ptr_businday.business_date  - 1天
    sqlCmd = " SELECT                                 "
        +"  ( bonus_notax + bonus_tax + vd_bonus_notax + vd_bonus_tax ) AS yday_balbp "
        +" FROM                                   "
        +"  act_master_bal a                     "
        +" WHERE                                  "
        +"   a.check_date = ?                     "
        +"   AND a.CURR_CODE = '901'              ";
//  setString(1, lastDate);
    setString(1, hBusiBusinessDate);    
    selectTable();
    if (!"Y".equals(notFound)) {
      ydayBalbp = getValueInt("yday_balbp");
    }
    
    // 本日新增點數
    sqlCmd = " SELECT                                                               "
        + "   sum(a.beg_tran_bp) AS new_bp                                       "
        + " FROM                                                                 "
        + "   mkt_bonus_dtl a                                                    "
        + " WHERE                                                                "
//      + "   a.tran_code IN ('0', '1', '2', '3', '4', '5', '6')                 "
        + "   a.tran_code IN ('0', '1', '2', '3', '4', '5', '7')                 "
        + "   AND a.tran_pgm NOT IN (                                            "
        + "               'BilO101', 'BilO102', 'BilO105',                       "
        + "               'TCSRDA40', 'ECSCDA40', 'mktp0270' ,                   "
        + "               'MktT110', 'MktT120', 'MktT130' , 'MktT135', 'MktT140' "   
        + "               )                                                      "
        + "   AND a.acct_date = ?                                                ";
    setString(1, hBusiBusinessDate);
    selectTable();
    if (!"Y".equals(notFound)) {
      newBp = getValueInt("new_bp");
    }
    sqlCmd = " SELECT                                                               "
        + "   sum(a.beg_tran_bp) AS new_bp                                       "
        + " FROM                                                                 "
        + "   dbm_bonus_dtl a                                                    "
        + " WHERE                                                                "
//      + "   a.tran_code IN ('0', '1', '2', '3', '4', '5', '6')                 "
        + "   a.tran_code IN ('0', '1', '2', '3', '4', '5', '7')                 "        
        + "   AND a.tran_pgm NOT IN (                                            "
        + "               'BilO101', 'BilO102', 'BilO105',                       "
        + "               'TCSRDA40', 'ECSCDA40', 'mktp0270' ,                   "
        + "               'MktT110', 'MktT120', 'MktT130' , 'MktT135', 'MktT140' "   
        + "               )                                                      "
        + "   AND a.acct_date = ?                                                ";
    setString(1, hBusiBusinessDate);
    selectTable();
    if (!"Y".equals(notFound)) {
      newBp += getValueInt("new_bp");
    }
    
    // 本日兌換點數
    sqlCmd = " SELECT                                               "
        + "   sum(a.beg_tran_bp) AS exchg_bp                     "
        + " FROM                                                 "
        + "   mkt_bonus_dtl a                                    "
        + " WHERE                                                "
        + "   a.tran_pgm IN ('TCSRDA40', 'ECSCDA40', 'mktp0270') "
        + "   AND a.acct_date = ?                                ";
    setString(1, hBusiBusinessDate);
    selectTable();
    if (!"Y".equals(notFound)) {
      exchgBp = getValueInt("exchg_bp");
    }
    sqlCmd = " SELECT                                               "
        + "   sum(a.beg_tran_bp) AS exchg_bp                     "
        + " FROM                                                 "
        + "   dbm_bonus_dtl a                                    "
        + " WHERE                                                "
        + "   a.tran_pgm IN ('TCSRDA40', 'ECSCDA40', 'mktp0270') "
        + "   AND a.acct_date = ?                                ";
    setString(1, hBusiBusinessDate);
    selectTable();
    if (!"Y".equals(notFound)) {
      exchgBp += getValueInt("exchg_bp");
    }
    
    // 本日移除點數
    sqlCmd = " SELECT                            "
        + "   sum(a.beg_tran_bp) AS remove_bp "
        + " FROM                              "
        + "   mkt_bonus_dtl a                 "
        + " WHERE                             "
//      + "   a.tran_code IN ('7')            "
        + "   a.tran_code IN ('6')            "        
        + "   AND a.acct_date = ?             ";
    setString(1, hBusiBusinessDate);
    selectTable();
    if (!"Y".equals(notFound)) {
      removeBp = getValueInt("remove_bp");
    }
    sqlCmd = " SELECT                            "
        + "   sum(a.beg_tran_bp) AS remove_bp "
        + " FROM                              "
        + "   dbm_bonus_dtl a                 "
        + " WHERE                             "
//      + "   a.tran_code IN ('7')            "
        + "   a.tran_code IN ('6')            "        
        + "   AND a.acct_date = ?             ";
    setString(1, hBusiBusinessDate);
    selectTable();
    if (!"Y".equals(notFound)) {
      removeBp += getValueInt("remove_bp");
    }
    
    // 本日優惠活動折抵點數
    sqlCmd = " SELECT                                                               "
        + "   sum(a.beg_tran_bp) AS new_bp                                       "
        + " FROM                                                                 "
        + "   mkt_bonus_dtl a                                                    "
        + " WHERE                                                                "
        + "   a.tran_pgm IN (                                                    "
        + "               'MktT110', 'MktT120', 'MktT130' , 'MktT135', 'MktT140' "    
        + "               )                                                      "
        + "   AND a.acct_date = ?                                                ";
    setString(1, hBusiBusinessDate);
    selectTable();
    if (!"Y".equals(notFound)) {
      newBp2 = getValueInt("new_bp");
    }
    sqlCmd = " SELECT                                                               "
        + "   sum(a.beg_tran_bp) AS new_bp                                       "
        + " FROM                                                                 "
        + "   dbm_bonus_dtl a                                                    "
        + " WHERE                                                                "
        + "   a.tran_pgm IN (                                                "
        + "               'MktT110', 'MktT120', 'MktT130' , 'MktT135', 'MktT140' "    
        + "               )                                                      "
        + "   AND a.acct_date = ?                                                ";
    setString(1, hBusiBusinessDate);
    selectTable();
    if (!"Y".equals(notFound)) {
      newBp2 += getValueInt("new_bp");
    }
    
    // 本日線上折抵點數
    sqlCmd = " SELECT                                            "
        + "   sum(a.beg_tran_bp) AS redeem_bp                 "
        + " FROM                                              "
        + "   mkt_bonus_dtl a                                 "
        + " WHERE                                             "
        + "   a.tran_pgm IN ('BilO101', 'BilO102', 'BilO105') "
        + "   AND a.acct_date = ?                             ";
    setString(1, hBusiBusinessDate);
    selectTable();
    if (!"Y".equals(notFound)) {
      redeemBp = getValueInt("redeem_bp");
    }
    
    // 本日剩餘總點數
    sqlCmd = " SELECT                                               "
        + "   sum( a.end_tran_bp + a.res_tran_bp) AS today_balbp "
        + " FROM                                                 "
        + "   mkt_bonus_dtl a                                    "
        + " WHERE                                                "
        + "   a.acct_date <= ?                                   ";
    setString(1, hBusiBusinessDate);
    showLogMessage("I", "", String.format("本日剩餘總點數1: %s %s", sqlCmd, hBusiBusinessDate));
    selectTable();
    if (!"Y".equals(notFound)) {
      todayBalbp = getValueInt("today_balbp");
      showLogMessage("I", "", String.format("本日剩餘總點數1: %d", getValueInt("today_balbp")));
    }
    sqlCmd = " SELECT                              "
        + "   sum(a.end_tran_bp) AS today_balbp "
        + " FROM                                "
        + "   dbm_bonus_dtl a                   "
        + " WHERE                               "
        + "   a.acct_date <= ?                  ";
    setString(1, hBusiBusinessDate);
    showLogMessage("I", "", String.format("本日剩餘總點數2: %s %s", sqlCmd, hBusiBusinessDate));
    selectTable();
    if (!"Y".equals(notFound)) {
      todayBalbp += getValueInt("today_balbp");
      showLogMessage("I", "", String.format("本日剩餘總點數2: %d", getValueInt("today_balbp")));
    }
    showLogMessage("I", "", String.format("本日剩餘總點數: %d", todayBalbp));

    totCnt = 1;
    if (ydayBalbp == 0 && newBp == 0 && exchgBp == 0 && removeBp == 0 && newBp2 == 0 && redeemBp == 0 && todayBalbp == 0) {
      totCnt = 0;
    }
  }


  /***********************************************************************/
  void headFile() throws Exception {
    String temp = "";

    pageCnt1++;
//    if (pageCnt1 > 1) {
//      lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));
//    }

    buf = "";
    buf = comcr.insertStr(buf, "合作金庫商業銀行", 60);
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));
    buf = comcr.insertStr(buf, "分行代號: " + "3144 信用卡部", 1);
    buf = comcr.insertStr(buf, "" + rptName1, 50);
    buf = comcr.insertStr(buf, "保存年限: 五年", 100);
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));

    buf = "";
    tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
        hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
    buf = comcr.insertStr(buf, "報表代號: CRD121     科目代號:", 1);
    buf = comcr.insertStr(buf, "中華民國 " + tmp, 50);
    temp = String.format("%04d", pageCnt1);
    buf = comcr.insertStr(buf, "第 " + temp + "頁", 100);
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));

//    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));
    
    buf = "------------------------------------------------------------------------------------------------------------------------------------";
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));
    buf = "         前日              本日             本日             本日               本日                 本日               本日 ";
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));
    buf = "      剩餘總點數    +    新增點數    -    兌換點數    -    移除點數   -   優惠活動折抵點數   -   線上折抵點數    =    剩餘點數";
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));
    buf = "------------------------------------------------------------------------------------------------------------------------------------";
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));

    lineCnt1 = 7;
  }

  /***********************************************************************/
  void tailFile() throws UnsupportedEncodingException {
    buf = "";
    for (int i = 0; i < 10; i++) {
      lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));
    }
    buf = "------------------------------------------------------------------------------------------------------------------------------------";
    lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));
     buf = " 說明 : 1.移除點數 = 指定無效卡失效期限後移除點數 + 未兌換到期後移除點數。";
     lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
     buf = " 製表單位:資訊部                      經辦:                           核章:                                   Form:MRGS31";
     lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
  }

  /***********************************************************************/
  void writeFile() throws Exception {
    if (totCnt > 0) {
//      buf = "      剩餘總點數    +    新增點數    -    兌換點數    -    移除點數    -    優惠活動折抵點數    -    線上折抵點數    =    剩餘點數";
//      buf = "總計：  %,d             %,d              %,d              %,d              %,d                       %,d                %,d";
      buf = "總計：" + comc.fixRight(String.format("%,d", ydayBalbp), 17)
              + comc.fixRight(String.format("%,d", newBp), 12)
              + comc.fixRight(String.format("%,d", exchgBp), 15)
              + comc.fixRight(String.format("%,d", removeBp), 15)
              + comc.fixRight(String.format("%,d", newBp2), 20)
              + comc.fixRight(String.format("%,d", redeemBp), 18)
              + comc.fixRight(String.format("%,d", todayBalbp), 20);
//      buf = String.format(buf, ydayBalbp, newBp, exchgBp, removeBp, newBp2, redeemBp, todayBalbp);
      lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
    } else {
      buf = "*** 查無資料 ***";
      lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    return;
  }

  /************************************************************************/
  public static void main(String[] args) throws Exception {
    MktRt05 proc = new MktRt05();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
}
