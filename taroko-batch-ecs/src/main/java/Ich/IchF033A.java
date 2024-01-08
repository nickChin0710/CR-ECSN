/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/04/20  V1.00.01  JeffKung    program initial                          *
 *  112/05/18  V1.00.02  Wilson      mark update_ich_notify_log not found      *
 *  112/05/22  V1.00.03  Wilson      鎖卡要update oppost_date                    *
 ******************************************************************************/

package Ich;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class IchF033A extends AccessDAO {
  private final String progname = "鎖卡資料拋轉(A03B)處理  112/05/22  V1.00.03";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
  String rptId = "";
  String rptName = "";
  int rptSeq = 0;

  String hCallBatchSeqno = "";

  String hTempNotifyDate = "";
  String hNextNotifyDate = "";
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
  String hOrgdTsccDataSeqno = "";
  String hOrgdOrgData = "";
  String hOrgdRptRespCode = "";
  int hCnt = 0;
  int hErrCnt = 0;

  String hIchCardNo = "";
  String hAgencyCd = "";
  String hStroeCd = "";
  String hMerchineType = "";
  String hMerchineNo = "";
  String hSafeNo = "";
  String hLockRsn = "";
  String hLockDate = "";
  String hLockTime = "";
  String hSysDate = "";
  String hSysTime = "";


  String tmpstr1 = "";
  String tmpstr2 = "";
  int forceFlag = 0;
  int totCnt = 0;
  int succCnt = 0;
  int hTnlgRecordCnt = 0;
  int totalCnt = 0;
  String tmpstr = "";
  String hHash = "";


  String out = "";

  Buf1 dtl = new Buf1();

  public int mainProcess(String[] args) {

    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      
      hTempNotifyDate = "";
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
          if (args[1].equals("Y"))
            forceFlag = 1;
      }
      else {
    	  comc.errExit("Usage : ichF033 [[notify_date][fo1yy_flag]] [force_flag]", "");
      }

	  hNextNotifyDate = comm.nextNDate(hTempNotifyDate, 1);
	  
      tmpstr1 = String.format("ARQB_%3.3s_%8.8s_A03B", comc.ICH_BANK_ID3, hTempNotifyDate);
      hTnlgFileName = tmpstr1;
      showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

      deleteIchA03bLock();
      deleteIchOrgdataLog();

      fileOpen();

      updateIchNotifyLogA();

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
    sqlCmd = "select business_date, ";
    sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_system_date,";
    sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time,";
    sqlCmd += "to_char(add_months(sysdate,2),'yyyymmdd') h_add2_system_date ";
    sqlCmd += " from ptr_businday  ";
    sqlCmd += " fetch first 1 rows only ";

    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
      hBusiBusinessDate = getValue("business_date");
      hTempSystemDate = getValue("h_temp_system_date");
      hTempNotifyTime = getValue("h_temp_notify_time");
      hAdd2SystemDate = getValue("h_add2_system_date");
    }

  }

  /***********************************************************************/
  int selectIchOrgdataLog() throws Exception {
    sqlCmd = "select count(*) h_cnt,";
    sqlCmd += "sum(decode(rpt_resp_code,'0',0,1)) h_err_cnt ";
    sqlCmd += " from ich_orgdata_log  ";
    sqlCmd += "where file_name  = ? ";
    setString(1, hTnlgFileName);
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("ichF021 檢核程式未執行或該日無資料需處理...", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
      hCnt = getValueInt("h_cnt");
      hErrCnt = getValueInt("h_err_cnt");
    }

    return (hErrCnt);
  }

  /***********************************************************************/
  void deleteIchA03bLock() throws Exception {
    daoTable = "ich_a03b_lock a";
    whereStr =
        "where a.tscc_data_seqno in (select b.tscc_data_seqno from ich_orgdata_log b where b.file_name  = ?  ";
    whereStr += "and b.tscc_data_seqno = a.tscc_data_seqno) ";
    setString(1, hTnlgFileName);
    deleteTable();
  }
  
  /***********************************************************************/
  void deleteIchOrgdataLog() throws Exception {
      daoTable = "ich_orgdata_log";
      whereStr = "where file_name = ? ";
      setString(1, hTnlgFileName);
      deleteTable();

  }

  /***********************************************************************/
  void updateIchNotifyLogA() throws Exception {
    daoTable = "ich_notify_log";
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
//      comcr.errRtn("update_ich_notify_log not found!", "", hCallBatchSeqno);
//    }

  }

  /***********************************************************************/
  void fileOpen() throws Exception {
    String str600 = "";
    String allData = "";

    /* read ARQB */
    String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
    temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
    int br = openInputText(temstr1, "MS950");
    if (br == -1) {
      comcr.errRtn("檔案不存在：" + temstr1, "", hCallBatchSeqno);
    }


    /* write ARPB */
    tmpstr1 = String.format("ARPB_%3.3s_%8.8s_A03B", comc.ICH_BANK_ID3, hBusiBusinessDate);
    String temstr2 = String.format("%s/media/ich/%s", comc.getECSHOME(), tmpstr1);
    temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
    out = temstr2;

    hHash = "0000000000000000000000000000000000000000";
    tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%-40.40s\r\n", "A03B", "02", "0001",
        comc.ICH_BANK_ID3, "00000000", hHash);

    lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));

    while (true) {
      str600 = readTextFile(br);
      if (endFile[br].equals("Y"))
        break;

      str600 = comc.rtrim(str600);
      if (str600.substring(0, 1).equals("H"))
        continue;

      totalCnt++;

      initIchA03bLock();

      splitBuf1(str600);
      if ((totalCnt % 3000) == 0 || totalCnt == 1)
        showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

      hOrgdTsccDataSeqno = comcr.getTSCCSeq();

      if (!str600.substring(0, 1).equals("D")) {
          hOrgdRptRespCode = "1";
          insertIchOrgdataLog();
          continue;
      }

      tmpstr1 = String.format("%s", comc.rtrim(dtl.lockDate));
      if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
          hOrgdRptRespCode = "1";
          insertIchOrgdataLog();
          continue;
      }
      tmpstr1 = String.format("%s", comc.rtrim(dtl.lockTime));
      if (comc.commTimeCheck(tmpstr1) == false) {
          hOrgdRptRespCode = "1";
          insertIchOrgdataLog();
          continue;
      }
      tmpstr1 = String.format("%s", comc.rtrim(dtl.sysDate));
      if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
          hOrgdRptRespCode = "1";
          insertIchOrgdataLog();
          continue;
      }
      tmpstr1 = String.format("%s", comc.rtrim(dtl.sysTime));
      if (comc.commTimeCheck(tmpstr1) == false) {
          hOrgdRptRespCode = "1";
          insertIchOrgdataLog();
          continue;
      }
      
      hOrgdOrgData = str600;

      hIchCardNo = comc.rtrim(dtl.ichCardNo);
      hAgencyCd = comc.rtrim(dtl.agencyCd);
      hStroeCd = comc.rtrim(dtl.stroeCd);
      hMerchineType = comc.rtrim(dtl.merchineType);
      hMerchineNo = comc.rtrim(dtl.merchineNo);
      hSafeNo = comc.rtrim(dtl.safeNo);
      hLockRsn = comc.rtrim(dtl.lockRsn);
      hLockDate = comc.rtrim(dtl.lockDate);
      hLockTime = comc.rtrim(dtl.lockTime);
      hSysDate = comc.rtrim(dtl.sysDate);
      hSysTime = comc.rtrim(dtl.sysTime);

      if (updateIchCard() != 0) {
          hOrgdRptRespCode = "1";
          insertIchOrgdataLog();
          continue;
      }

      hOrgdRptRespCode = "0";

      insertIchOrgdataLog();

      insertIchA03bLock();

      if (hOrgdRptRespCode.equals("0"))
        succCnt++;

      String buf = String.format("D%-16.16s%-14.14s%-30.30s%1.1s\r\n", hIchCardNo,
          sysDate + sysTime, " ", hOrgdRptRespCode);
      lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

      allData += buf;
    }

    closeInputText(br);
    moveBackup(hTnlgFileName);

    if (totalCnt > 0) {
      hHash = comc.encryptSHA(allData, "SHA-1", "MS950");
      tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%-40.40s\r\n", "A03B", "02", "0001",
          comc.ICH_BANK_ID3, comm.fillZero(Integer.toString(totalCnt), 8), hHash);
      lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
    }

    comc.writeReport(out, lpar, "MS950", false);
  }

  /***********************************************************************/
  int updateIchCard() throws Exception {
    // L5:持卡人icash退卡 L1:愛金卡鎖卡 L4:金融機構鎖卡名單鎖卡 L3:其他原因

    daoTable = "ich_card";
    if (hLockRsn.equals("L5")) {
      updateSQL = "ich_oppost_date   = ? , ";
      updateSQL += "oppost_source     = ? , ";
      updateSQL += "return_date       = ? , ";
      updateSQL += "return_time       = ? , ";
      updateSQL += "return_flag       = 'Y' , ";
      updateSQL += "balance_date      = to_char(sysdate, 'yyyymmdd') , ";
      updateSQL += "current_code      = '1' , ";
      updateSQL += "oppost_date       = ? , ";
      updateSQL += "mod_pgm           = ? , ";
      updateSQL += "mod_time          = sysdate   ";
      whereStr = "where ich_card_no = ? ";
      setString(1, sysDate);
      setString(2, hLockRsn);
      setString(3, hLockDate);
      setString(4, hLockTime);
      setString(5, sysDate);
      setString(6, javaProgram);
      // setString(6, sysDate + sysTime);
      setString(7, hIchCardNo);
    } else {
      updateSQL = "lock_date         = ? , ";
      updateSQL += "lock_time         = ? , ";
      updateSQL += "lock_flag         = 'Y', ";
      updateSQL += "balance_date      = to_char(sysdate, 'yyyymmdd') , ";
      updateSQL += "mod_pgm           = ? , ";
      updateSQL += "mod_time          = sysdate   ";
      whereStr = "where ich_card_no = ? ";
      setString(1, hLockDate);
      setString(2, hLockTime);
      setString(3, javaProgram);
      // setString(4, sysDate + sysTime);
      setString(4, hIchCardNo);
    }
    updateTable();
    if (notFound.equals("Y")) {
      // comcr.err_rtn("update_ich_card not found!", h_ich_card_no
      // , comcr.h_call_batch_seqno);
      return (1);
    }
    return (0);
  }

  /***********************************************************************/
  void initIchA03bLock() throws Exception {
    hIchCardNo = "";
    hAgencyCd = "";
    hStroeCd = "";
    hMerchineType = "";
    hMerchineNo = "";
    hSafeNo = "";
    hLockRsn = "";
    hLockDate = "";
    hLockTime = "";
    hSysDate = "";
    hSysTime = "";
  }

  /***********************************************************************/
  void insertIchOrgdataLog() throws Exception {

    setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
    setValue("file_iden", "A03B");
    setValue("notify_date", hTempNotifyDate);
    setValue("file_name", hTnlgFileName);
    setValue("org_data", hOrgdOrgData);
    setValue("rpt_resp_code", hOrgdRptRespCode);
    setValue("mod_pgm", javaProgram);
    setValue("mod_time", sysDate + sysTime);
    daoTable = "ich_orgdata_log";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_ich_orgdata_log duplicate!", "file_name = " + hTnlgFileName, hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  void insertIchA03bLock() throws Exception {

    setValue("ich_card_no", hIchCardNo);
    setValue("agency_cd", hAgencyCd);
    setValue("stroe_cd", hStroeCd);
    setValue("merchine_type", hMerchineType);
    setValue("merchine_no", hMerchineNo);
    setValue("safe_no", hSafeNo);
    setValue("lock_rsn", hLockRsn);
    setValue("lock_date", hLockDate);
    setValue("lock_time", hLockTime);
    setValue("sys_date", hSysDate);
    setValue("sys_time", hSysTime);
    setValue("file_name", hTnlgFileName);
    setValue("crt_date", sysDate);
    setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", javaProgram);
    daoTable = "ich_a03b_lock";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_ich_a03b_lock duplicate!", "ich_card_no = " + hIchCardNo, hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  void moveBackup(String moveFile) throws Exception {
    String root = String.format("%s/media/ich", comc.getECSHOME());
    String src = String.format("%s/%s", root, moveFile);
    String target = String.format("%s/backup/%s/%s", root, hTempNotifyDate, moveFile);

    comc.fileRename2(src, target);
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    IchF033A proc = new IchF033A();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

  /***********************************************************************/
  class Buf1 {
    String type;
    String ichCardNo;
    String agencyCd;
    String stroeCd;
    String merchineType;
    String merchineNo;
    String safeNo;
    String lockRsn;
    String lockDate;
    String lockTime;
    String sysDate;
    String sysTime;
  }

  void splitBuf1(String str) throws UnsupportedEncodingException {
    byte[] bytes = str.getBytes("MS950");
    dtl.type = comc.subMS950String(bytes, 0, 1);
    dtl.ichCardNo = comc.subMS950String(bytes, 1, 16);
    dtl.agencyCd = comc.subMS950String(bytes, 17, 20);
    dtl.stroeCd = comc.subMS950String(bytes, 37, 20);
    dtl.merchineType = comc.subMS950String(bytes, 57, 1);
    dtl.merchineNo = comc.subMS950String(bytes, 58, 8);
    dtl.safeNo = comc.subMS950String(bytes, 66, 16);
    dtl.lockRsn = comc.subMS950String(bytes, 82, 2);
    dtl.lockDate = comc.subMS950String(bytes, 84, 8);
    dtl.lockTime = comc.subMS950String(bytes, 92, 6);
    dtl.sysDate = comc.subMS950String(bytes, 98, 8);
    dtl.sysTime = comc.subMS950String(bytes, 106, 6);
  }
}
