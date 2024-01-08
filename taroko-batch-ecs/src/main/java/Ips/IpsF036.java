/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/06/30  V1.00.01    Wilson    mark update ich_notify_log                *
*  109/07/01  v1.00.02    Wilson    新增update_ips_card                         *
 *  109/07/04  V1.00.03    Zuwei     coding standard, rename field method & format                   *
 *  109/07/23  V1.00.04    shiyuqi     coding standard,                   *
 * 109/08/24  V1.00.05    Wilson    測試修改                                                                                                     *
*  109-10-19  V1.00.06    shiyuqi       updated for project coding standard     *
*  109/10/30  V1.00.07    Wilson    檔名日期改營業日                                                                                      *
******************************************************************************/

package Ips;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*銀行聯名卡黑名單清除暨回覆檔(I2B006)處理*/
public class IpsF036 extends AccessDAO {
  private final String progname = "銀行聯名卡黑名單清除暨回覆檔(I2B006)處理  109/10/30 V1.00.07";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  String hCallBatchSeqno = "";

  String hTempNotifyDate = "";
  String hBusiBusinessDate = "";
  String hTempSystemDate = "";
  String hTempNotifyTime = "";
  String hAdd2SystemDate = "";
  String hTnlgPerformFlag = "";
  String hTnlgNotifyDate = "";
  String hTnlgCheckCode = "";
  String hTnlgProcFlag = "";
  String hTnlgRowid = "";
  String hTnlgFileName = "";
  String hI2b6CardNo = "";
  String hI2b6IpsCardNo = "";
  String hOrgdTsccDataSeqno = "";
  String hOrgdOrgData = "";
  String hOrgdRptRespCode = "";
  String hI2b6TxnType = "";
  String hI2b6TxnDate = "";
  String hI2b6TxnTime = "";
  String hI2b6PostFlag = "";
  String hI2b6TsccDataSeqno = "";
  int hCnt = 0;
  int hErrCnt = 0;

  String tmpstr1 = "";
  String tmpstr2 = "";
  String fileSeq = "";
  int forceFlag = 0;
  int totCnt = 0;
  int succCnt = 0;
  int hTnlgRecordCnt = 0;
  int totalCnt = 0;
  String nUserpid = "";
  String tmpstr = "";
  int nRetcode = 0;

  Buf1 dtl = new Buf1();

  public int mainProcess(String[] args) {

    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);
      // =====================================
      if (comm.isAppActive(javaProgram)) {
        comc.errExit("Error!! Someone is running this program now!!!",
            "Please wait a moment to run again!!");
      }
//      if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 3) {
//        comc.errExit("Usage : IpsF036 [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
//      }

      // 固定要做的

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      hTempNotifyDate = "";
      fileSeq = "01";
      forceFlag = 0;
      
      selectPtrBusinday();
      
      if (args.length == 0) {
    	  hTempNotifyDate = hBusiBusinessDate;
      }
      else if (args.length == 1) {
          if ((args[0].length() == 1) && (args[0].equals("Y")))
            forceFlag = 1;
          if (args[0].length() == 8)
            hTempNotifyDate = args[0];
          if (args[0].length() == 2) {
            showLogMessage("I", "", "參數(一) 不可兩碼");
          }
      }
      else if (args.length == 2) {
          hTempNotifyDate = args[0];
          if ((args[1].length() == 1) && (args[1].equals("Y")))
            forceFlag = 1;
          if (args[1].length() == 2)
            fileSeq = args[1];
          if (args[1].length() != 1 && args[1].length() != 2) {
            showLogMessage("I", "", "參數(二) 為[force_flag] or [seq(nn)] ");
          }
      }
      else if (args.length == 3) {
          hTempNotifyDate = args[0];
          if (args[1].equals("Y"))
            forceFlag = 1;
          if (args[2].length() != 2) {
            showLogMessage("I", "", "file seq 必須兩碼");
          }
          fileSeq = args[2];
      }
      else {
    	  comc.errExit("Usage : IpsF036 [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
      }

      tmpstr1 = String.format("I2B006_%4.4s%8.8s%2.2s.dat", comc.IPS_BANK_ID4, hTempNotifyDate, fileSeq);
      hTnlgFileName = tmpstr1;
      showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

      int rtn = selectIpsOrgdataLog();
      if (rtn != 0) {
        String stderr = String.format("%s 檢核有錯本程式不執行..[%d]", javaProgram, rtn);
        backupRtn();
        comcr.errRtn(stderr, "", hCallBatchSeqno);
      }

      deleteIpsI2b006Log();

      fileOpen();

      // 第二階段須解除mark
      // updateIpsNotifyLogA();

      backupRtn();

      showLogMessage("I", "",
          String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));
      // ==============================================
      // 固定要做的
      comcr.callbatch(1, 0, 0);
      showLogMessage("I", "", "執行結束");
      finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
    }
  }

  /***********************************************************************/
  void selectPtrBusinday() throws Exception {
//    hTempNotifyDate = hTempNotifyDate.length() == 0 ? sysDate : hTempNotifyDate;
    hBusiBusinessDate = "";
    hAdd2SystemDate = "";
    sqlCmd = "select business_date ";
//    sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_system_date,";
//    sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time,";
//    sqlCmd += "to_char(add_months(sysdate,2),'yyyymmdd') h_add2_system_date ";
    sqlCmd += " from ptr_businday  ";
    sqlCmd += "fetch first 1 rows only ";
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
      hBusiBusinessDate = getValue("business_date");
//      hTempSystemDate = getValue("h_temp_system_date");
//      hTempNotifyTime = getValue("h_temp_notify_time");
//      hAdd2SystemDate = getValue("h_add2_system_date");
    }

  }

  /***********************************************************************/
  void backupRtn() throws Exception {

    String root = String.format("%s/media/ips", comc.getECSHOME());

    root = Normalizer.normalize(root, java.text.Normalizer.Form.NFKD);

    tmpstr2 = String.format("%s/media/ips/backup/%s/%s", comc.getECSHOME(), hTempNotifyDate,
        hTnlgFileName);
    comc.fileRename2(String.format("%s/%s", root, hTnlgFileName), tmpstr2);

    tmpstr1 =
        String.format("I2B006_%4.4s%8.8s%2.2s.zip", comc.IPS_BANK_ID4, hTempNotifyDate, fileSeq);
    tmpstr2 =
        String.format("%s/media/ips/backup/%s/%s", comc.getECSHOME(), hTempNotifyDate, tmpstr1);
    comc.fileRename2(String.format("%s/%s", root, tmpstr1), tmpstr2);
  }

  /***********************************************************************/
  int selectIpsOrgdataLog() throws Exception {
    sqlCmd = "select count(*) h_cnt,";
    sqlCmd += "sum(decode(rpt_resp_code,'0000',0,1)) h_err_cnt ";
    sqlCmd += " from ips_orgdata_log  ";
    sqlCmd += "where file_name  = ? ";
    setString(1, hTnlgFileName);
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("IpsF021 檢核程式未執行或該日無資料需處理...", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
      hCnt = getValueInt("h_cnt");
      hErrCnt = getValueInt("h_err_cnt");
    }

    return (hErrCnt);
  }

  /***********************************************************************/
  void deleteIpsI2b006Log() throws Exception {
    daoTable = "ips_i2b006_log a";
    whereStr =
        "where a.tscc_data_seqno in (select b.tscc_data_seqno from ips_orgdata_log b where b.file_name  = ?  ";
    whereStr += "and b.tscc_data_seqno = a.tscc_data_seqno) ";
    setString(1, hTnlgFileName);
    deleteTable();
  }

  /***********************************************************************/
  void updateIpsNotifyLogA() throws Exception {
    daoTable = "ips_notify_log";
    updateSQL = "proc_flag  = '2',";
    updateSQL += " proc_date  = to_char(sysdate,'yyyymmdd'),";
    updateSQL += " proc_time  = to_char(sysdate,'hh24miss'),";
    updateSQL += " mod_pgm   = ?,";
    updateSQL += " mod_time  = sysdate";
    whereStr = "where file_name  = ? ";
    setString(1, javaProgram);
    setString(2, hTnlgFileName);
    updateTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("update_ips_notify_log not found!", "", hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  void fileOpen() throws Exception {
    String str600 = "";

    String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), hTnlgFileName);
    temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
    int f = openInputText(temstr1);
    if (f == -1) {
      comcr.errRtn("檔案不存在：" + temstr1, "", hCallBatchSeqno);
    }
    int br = openInputText(temstr1, "MS950");
    while (true) {
      str600 = readTextFile(br);
      if (endFile[br].equals("Y"))
        break;
      str600 = comc.rtrim(str600);
      if ((str600.substring(0, 1).equals("H")) || (str600.substring(0, 1).equals("T")))
        continue;

      totalCnt++;

      initIpsI2b006Log();

      splitBuf1(str600);
      if ((totalCnt % 3000) == 0 || totalCnt == 1)
        showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

      hOrgdTsccDataSeqno = comcr.getTSCCSeq();

      // tmpstr1 = String.format("%010.0f", h_orgd_tscc_data_seqno);
      tmpstr1 = String.format("%010.0f", comcr.str2double(hOrgdTsccDataSeqno)); // phopho mod
      hI2b6TsccDataSeqno = tmpstr1;

      hOrgdOrgData = str600;
      hI2b6IpsCardNo = comc.rtrim(dtl.ipsCardNo);
      selectIpsCard();
      hI2b6TxnType = comc.rtrim(dtl.txnType);
      hI2b6TxnDate = comc.rtrim(dtl.txnDate);
      hI2b6TxnTime = comc.rtrim(dtl.txnTime);
      hOrgdRptRespCode = "0000";

      insertIpsOrgdataLog();

      // 第二階段須解除mark
      // insertIpsI2b006Log();
      updateIpsCard();

      if (hOrgdRptRespCode.equals("0000"))
        succCnt++;
    }

    closeInputText(br);
  }

  /***********************************************************************/
  void initIpsI2b006Log() throws Exception {
    hI2b6IpsCardNo = "";
    hI2b6CardNo = "";
    hI2b6TxnType = "";
    hI2b6TxnDate = "";
    hI2b6TxnTime = "";
    hI2b6PostFlag = "N";
    hI2b6TsccDataSeqno = "";
  }

  /***********************************************************************/
  int selectIpsCard() throws Exception {
    hI2b6CardNo = "";

    sqlCmd = "select card_no ";
    sqlCmd += " from ips_card  ";
    sqlCmd += "where ips_card_no = ? ";
    setString(1, hI2b6IpsCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hI2b6CardNo = getValue("card_no");
    } else {
      return (1);
    }
    return (0);
  }

  /***********************************************************************/
  void insertIpsOrgdataLog() throws Exception {

    setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
    setValue("file_iden", "I2B006");
    setValue("notify_date", hTempNotifyDate);
    setValue("file_name", hTnlgFileName);
    setValue("org_data", hOrgdOrgData);
    setValue("rpt_resp_code", hOrgdRptRespCode);
    setValue("mod_pgm", javaProgram);
    setValue("mod_time", sysDate + sysTime);
    daoTable = "ips_orgdata_log";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_ips_orgdata_log duplicate!", "", hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  void insertIpsI2b006Log() throws Exception {

    setValue("ips_card_no", hI2b6IpsCardNo);
    setValue("card_no", hI2b6CardNo);
    setValue("txn_type", hI2b6TxnType);
    setValue("txn_date", hI2b6TxnDate);
    setValue("txn_time", hI2b6TxnTime);
    setValue("post_flag", hI2b6PostFlag);
    setValue("file_name", hTnlgFileName);
    setValue("crt_date", sysDate);
    setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", javaProgram);
    daoTable = "ips_i2b006_log";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_ips_i2b006_log duplicate!", "", hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  void updateIpsCard() throws Exception {
    daoTable = "ips_card";
    updateSQL = " blacklt_flag   = 'N',";
    updateSQL += " blacklt_date   = '',";
    updateSQL += " mod_pgm        = ?,";
    updateSQL += " mod_time       = sysdate";
    whereStr = "where ips_card_no  = ? ";
    setString(1, javaProgram);
    setString(2, hI2b6IpsCardNo);
    updateTable();
    if (notFound.equals("Y")) {
      //comcr.errRtn("update_ips_card not found!", "", hCallBatchSeqno);
    }
    
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    IpsF036 proc = new IpsF036();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

  /***********************************************************************/
  class Buf1 {
    String type;
    String ipsCardNo;
    String txnType;
    String txnDate;
    String txnTime;
    String filler0;
    String filler1;

    String allText() throws UnsupportedEncodingException {
      String rtn = "";
      rtn += comc.fixLeft(type, 1);
      rtn += comc.fixLeft(ipsCardNo, 11);
      rtn += comc.fixLeft(txnType, 2);
      rtn += comc.fixLeft(txnDate, 8);
      rtn += comc.fixLeft(txnTime, 6);
      rtn += comc.fixLeft(filler0, 28);
      rtn += comc.fixLeft(filler1, 2);
      return rtn;
    }
  }

  void splitBuf1(String str) throws UnsupportedEncodingException {
    byte[] bytes = str.getBytes("MS950");
    dtl.type = comc.subMS950String(bytes, 0, 1);
    dtl.ipsCardNo = comc.subMS950String(bytes, 1, 11);
    dtl.txnType = comc.subMS950String(bytes, 12, 2);
    dtl.txnDate = comc.subMS950String(bytes, 14, 8);
    dtl.txnTime = comc.subMS950String(bytes, 22, 6);
    dtl.filler0 = comc.subMS950String(bytes, 28, 28);
    dtl.filler1 = comc.subMS950String(bytes, 56, 2);
  }

}
