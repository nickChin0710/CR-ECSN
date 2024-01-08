/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/06/30  V1.00.01    Wilson    mark update ich_notify_log                *
 *  109/07/04  V1.00.02    Zuwei     coding standard, rename field method & format                   *
 *  109/07/23  V1.00.03    shiyuqi     coding standard,                   *
 * 109/08/24  V1.00.05    Wilson    測試修改                                                                                                     *
*  109-10-19  V1.00.06    shiyuqi       updated for project coding standard     *
*  109/10/30  V1.00.07    Wilson    檔名日期改營業日                                                                                      *
*  112/05/12  V1.00.08    Wilson    mark update_ips_notify_log                *
*  112/09/06  V1.00.09    Wilson    update ips_card add autoload_from         *
******************************************************************************/

package Ips;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*鎖退卡暨關閉自動加值檔(I2B003)處理*/
public class IpsF033A extends AccessDAO {
  private final String progname = "鎖退卡暨關閉自動加值檔(I2B003)處理  112/09/06 V1.00.09";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  String hCallBatchSeqno = "";

  String hTempNotifyDate = "";
  String hBusiusinessDate = "";
  String hTempSystemDate = "";
  String hTempNotifyTime = "";
  String hAdd2SystemDate = "";
  String hTnlgPerformFlag = "";
  String hTnlgNotifyDate = "";
  String hTnlgCheckCode = "";
  String hTnlgProcFlag = "";
  String hTnlgRowid = "";
  String hTnlgFileName = "";
  String hI2b3TxnType = "";
  String hI2b3TrafficDate = "";
  String hI2b3TxnDate = "";
  String hI2b3IpsCardNo = "";
  String hI2b3CardNo = "";
  String hI2b3TsccDataSeqno = "";
  String hOrgdTsccDataSeqno = "";
  String hOrgdOrgData = "";
  String hOrgdRptRespCode = "";
  String hI2b3TxnTime = "";
  String hI2b3SystemCd = "";
  String hI2b3TxnDateR = "";
  String hI2b3TxnTimeR = "";
  String hI2b3TrafficCd = "";
  String hI2b3TrafficCdSub = "";
  String hI2b3TrafficEqup = "";
  String hI2b3TrafficAbbr = "";
  String hI2b3AddrCd = "";
  String hI2b3PostFlag = "";
  String hI2b3FileName = "";
  String hI2b3CreateDate = "";
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
//        comc.errExit("Usage : IpsF033A [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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
    	  hTempNotifyDate = hBusiusinessDate;
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
    	  comc.errExit("Usage : IpsF033A [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
      }

      tmpstr1 = String.format("I2B003_%4.4s%8.8s%2.2s.dat", comc.IPS_BANK_ID4, hTempNotifyDate, fileSeq);
      hTnlgFileName = tmpstr1;
      showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

      /*
      int rtn = selectIpsOrgdataLog();
      if (rtn != 0) {
        String stderr = String.format("%s檢核有錯本程式不執行..[%d]", javaProgram, rtn);
        backupRtn();
        comcr.errRtn(stderr, "", hCallBatchSeqno);
      }
      */

      deleteIpsI2b003Log();
      deleteIpsOrgdataLog();

      fileOpen();

      updateIpsNotifyLogA();

      backupRtn();

      showLogMessage("I", "",
          String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));

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
  void selectPtrBusinday() throws Exception {
//    hTempNotifyDate = hTempNotifyDate.length() == 0 ? sysDate : hTempNotifyDate;
    hBusiusinessDate = "";
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
      hBusiusinessDate = getValue("business_date");
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

    //tmpstr1 =
    //    String.format("I2B003_%4.4s%8.8s%2.2s.zip", comc.IPS_BANK_ID4, hTempNotifyDate, fileSeq);
    //tmpstr2 =
    //    String.format("%s/media/ips/backup/%s/%s", comc.getECSHOME(), hTempNotifyDate, tmpstr1);
    //comc.fileRename2(String.format("%s/%s", root, tmpstr1), tmpstr2);
  }

  /***********************************************************************/
  void deleteIpsOrgdataLog() throws Exception {
      daoTable = "ips_orgdata_log";
      whereStr = "where file_name = ? ";
      setString(1, hTnlgFileName);
      deleteTable();

  }
  
  /***********************************************************************/
  void deleteIpsI2b003Log() throws Exception {
    daoTable = "ips_i2b003_log a";
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
//    if (notFound.equals("Y")) {
//      comcr.errRtn("update_ips_notify_log not found!", "", hCallBatchSeqno);
//    }

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

      initIpsI2b003Log();

      splitBuf1(str600);
      if ((totalCnt % 3000) == 0 || totalCnt == 1)
        showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

      hOrgdTsccDataSeqno = comcr.getTSCCSeq();

      // tmpstr1 = String.format("%010.0f", h_orgd_tscc_data_seqno);
      tmpstr1 = String.format("%010.0f", comcr.str2double(hOrgdTsccDataSeqno)); // phopho mod
      hI2b3TsccDataSeqno = tmpstr1;

      hOrgdOrgData = str600;
      hI2b3IpsCardNo = comc.rtrim(dtl.ipsCardNo);
      selectIpsCard();
      hI2b3TxnType = comc.rtrim(dtl.txnType);
      hI2b3TxnDate = comc.rtrim(dtl.txnDate);
      hI2b3TxnTime = comc.rtrim(dtl.txnTime);
      hI2b3SystemCd = comc.rtrim(dtl.systemCd);
      hI2b3TxnDateR = comc.rtrim(dtl.txnDateR);
      hI2b3TxnTimeR = comc.rtrim(dtl.txnTimeR);
      hI2b3TrafficDate = comc.rtrim(dtl.trafficDate);
      if (hI2b3TxnType.equals("51")) {
        hI2b3TrafficDate = hI2b3TxnDate;
      }
      hI2b3TrafficCd = comc.rtrim(dtl.trafficCd);
      hI2b3TrafficCdSub = comc.rtrim(dtl.trafficCdSub);
      hI2b3TrafficEqup = comc.rtrim(dtl.trafficEqup);
      hI2b3TrafficAbbr = comc.rtrim(dtl.trafficAbbr);
      hI2b3AddrCd = comc.rtrim(dtl.trafficAddr);
      hOrgdRptRespCode = "0000";

      insertIpsOrgdataLog();

      insertIpsI2b003Log();

      updateIpsCard();

      if (hOrgdRptRespCode.equals("0000"))
        succCnt++;
    }

    closeInputText(br);
  }

  /***********************************************************************/
  void initIpsI2b003Log() throws Exception {
    hI2b3IpsCardNo = "";
    hI2b3CardNo = "";
    hI2b3TxnType = "";
    hI2b3TxnDate = "";
    hI2b3TxnTime = "";
    hI2b3SystemCd = "";
    hI2b3TxnDateR = "";
    hI2b3TxnTimeR = "";
    hI2b3TrafficDate = "";
    hI2b3TrafficCd = "";
    hI2b3TrafficCdSub = "";
    hI2b3TrafficEqup = "";
    hI2b3TrafficAbbr = "";
    hI2b3AddrCd = "";
    hI2b3PostFlag = "N";
    hI2b3FileName = "";
    hI2b3CreateDate = "";
    hI2b3TsccDataSeqno = "";
  }

  /***********************************************************************/
  int selectIpsCard() throws Exception {
    hI2b3CardNo = "";

    sqlCmd = "select card_no ";
    sqlCmd += " from ips_card  ";
    sqlCmd += "where ips_card_no = ? ";
    setString(1, hI2b3IpsCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hI2b3CardNo = getValue("card_no");
    } else
      return 1;

    return (0);
  }

  /***********************************************************************/
  void insertIpsOrgdataLog() throws Exception {

    setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
    setValue("file_iden", "I2B003");
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
  void insertIpsI2b003Log() throws Exception {

    setValue("ips_card_no", hI2b3IpsCardNo);
    setValue("card_no", hI2b3CardNo);
    setValue("txn_type", hI2b3TxnType);
    setValue("txn_date", hI2b3TxnDate);
    setValue("txn_time", hI2b3TxnTime);
    setValue("system_cd", hI2b3SystemCd);
    setValue("txn_date_r", hI2b3TxnDateR);
    setValue("txn_time_r", hI2b3TxnTimeR);
    setValue("traffic_date", hI2b3TrafficDate);
    setValue("traffic_cd", hI2b3TrafficCd);
    setValue("traffic_cd_sub", hI2b3TrafficCdSub);
    setValue("traffic_equp", hI2b3TrafficEqup);
    setValue("traffic_abbr", hI2b3TrafficAbbr);
    setValue("addr_cd", hI2b3AddrCd);
    setValue("post_flag", hI2b3PostFlag);
    setValue("file_name", hTnlgFileName);
    setValue("crt_date", sysDate);
    setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", javaProgram);
    daoTable = "ips_i2b003_log";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_ips_i2b003_log duplicate!", "", hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  /***
   * 4F：關閉自動加值 51：退卡 91：鎖卡
   * 
   * @return
   * @throws Exception
   */
  int updateIpsCard() throws Exception {

    daoTable = "ips_card";
    updateSQL = " autoload_flag  = decode(cast(? as varchar(10)) , '4F', 'N', autoload_flag) ,";
    updateSQL += " autoload_clo_date = decode(cast(? as varchar(10)) , '4F', cast(? as varchar(10)) , autoload_clo_date) ,";
    updateSQL += " autoload_from = decode(cast(? as varchar(10)) , '4F', '2' , autoload_from) ,";
    updateSQL += " return_flag  = decode(cast(? as varchar(10)) , '51', 'Y', return_flag) ,";
    updateSQL += " return_date  = decode(cast(? as varchar(10)) , '51', cast(? as varchar(10)) , return_date) ,";
    updateSQL += " lock_flag   = decode(cast(? as varchar(10)) , '91', 'Y', lock_flag) ,";
    updateSQL += " lock_date   = decode(cast(? as varchar(10)) , '91', cast(? as varchar(10)) , lock_date)";
    whereStr = "where ips_card_no = ? ";
    setString(1, hI2b3TxnType);
    setString(2, hI2b3TxnType);
    setString(3, hI2b3TrafficDate);
    setString(4, hI2b3TxnType);
    setString(5, hI2b3TxnType);
    setString(6, hI2b3TxnType);
    setString(7, hI2b3TxnDate);
    setString(8, hI2b3TxnType);
    setString(9, hI2b3TxnType);
    setString(10, hI2b3TxnDate);
    setString(11, hI2b3IpsCardNo);
    updateTable();
    if (notFound.equals("Y")) {
      return 1;
    }

    return (0);
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    IpsF033A proc = new IpsF033A();
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
    String systemCd;
    String txnDateR;
    String txnTimeR;
    String trafficDate;
    String trafficCd;
    String trafficCdSub;
    String trafficEqup;
    String trafficAbbr;
    String trafficAddr;
    String filler0;
    String filler1;

    String allText() throws UnsupportedEncodingException {
      String rtn = "";
      rtn += comc.fixLeft(type, 1);
      rtn += comc.fixLeft(ipsCardNo, 11);
      rtn += comc.fixLeft(txnType, 2);
      rtn += comc.fixLeft(txnDate, 8);
      rtn += comc.fixLeft(txnTime, 6);
      rtn += comc.fixLeft(systemCd, 2);
      rtn += comc.fixLeft(txnDateR, 8);
      rtn += comc.fixLeft(txnTimeR, 6);
      rtn += comc.fixLeft(trafficDate, 8);
      rtn += comc.fixLeft(trafficCd, 2);
      rtn += comc.fixLeft(trafficCdSub, 2);
      rtn += comc.fixLeft(trafficEqup, 30);
      rtn += comc.fixLeft(trafficAbbr, 20);
      rtn += comc.fixLeft(trafficAddr, 50);
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
    dtl.systemCd = comc.subMS950String(bytes, 28, 2);
    dtl.txnDateR = comc.subMS950String(bytes, 30, 8);
    dtl.txnTimeR = comc.subMS950String(bytes, 38, 6);
    dtl.trafficDate = comc.subMS950String(bytes, 44, 8);
    dtl.trafficCd = comc.subMS950String(bytes, 52, 2);
    dtl.trafficCdSub = comc.subMS950String(bytes, 54, 2);
    dtl.trafficEqup = comc.subMS950String(bytes, 56, 30);
    dtl.trafficAbbr = comc.subMS950String(bytes, 86, 20);
    dtl.trafficAddr = comc.subMS950String(bytes, 106, 50);
    dtl.filler0 = comc.subMS950String(bytes, 156, 28);
    dtl.filler1 = comc.subMS950String(bytes, 184, 2);
  }

}
