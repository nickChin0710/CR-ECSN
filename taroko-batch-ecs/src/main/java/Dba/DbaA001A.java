/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/11/20  V1.00.01    JeffKung   VD手續費補扣款D04產出
 *  111/12/09  V1.00.02    JeffKung   授權參數生效與請款日間隙補扣款 
 *  
 ******************************************************************************/

package Dba;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.Locale;

import org.codehaus.plexus.util.StringUtils;

import com.AccessDAO;
import com.CommCpi;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class DbaA001A extends AccessDAO {
  private final String progname = "產生VD手續費補扣款檔處理程式  111/12/09 V1.00.02";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCpi comcpi = new CommCpi();
  CommFTP commFTP = null;
  CommCrdRoutine comcr = null;
  CommRoutine comr = null;

  String szTmp = "";
  String stderr = "";
  long hModSeqno = 0;
  String ecsServer = "";
  String hModUser = "";
  String hModTime = "";
  String hModPgm = "";
  String hModWs = "";
  String hModLog = "";
  String hBusiBusinessDate = "";
  String hTempBusinessDate = "";

  String hDbblReferenceNo = "";
  String hDbblPSeqno = "";
  String hDbblAcctNo = "";
  String hDbblMchtNo = "";
  String hDbblMccCode = "";
  String hDbblBillType = "";
  String hDbblTxnCode = "";
  String hDbblPostDate = "";
  String hDbblPurchaseDate = "";
  String hDbblCardNo = "";
  String hDbblTxSeq = "";
  double hDbblDestAmt = 0;
  String hDbblMchtChiName = "";
  String hDbblMchtEngName = "";
  String hDbblAuthCode = "";
  String hDbblMchtCountry = "";
  String hDcioId = "";
  String hDcioIdCode = "";
  String hDbblRowid = "";

  String seqno = "";
  
  int hDetlSendCnt1 = 0;
  double hDetlSendAmt1 = 0;
  
  String parmStartDate = "";
  String parmEndDate = "";
  String tempDeductSeq = "";
  
  VDD vdd = new VDD();
  String chiDate = "";
  String filename1 = "";
  
  String parmMappingIdFlag = "";  //V1.00.20 參數存放是否要執行mapping ID

  public int mainProcess(String[] args) {

    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);
      // =====================================
      if (args.length != 2) {
    	  showLogMessage("I", "", " ==DbaA001A 參數必須輸入== \n");
    	  showLogMessage("I", "", " ==參數1: 入帳日期>=起始日期  \n");
    	  showLogMessage("I", "", " ==參數2: 入帳日期< 截止日期  \n");
    	  return 0;
      }
    	  
      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      commFTP = new CommFTP(getDBconnect(), getDBalias());
      comr = new CommRoutine(getDBconnect(), getDBalias());

      selectPtrBusinday();

      if (args.length == 2) {
    	  parmStartDate = args[0];
    	  parmEndDate = args[1];
      }

      //V1.00.20  暫時解決外國人ID錯誤的問題
      parmMappingIdFlag = selectPtrSysParm();
      
      hDetlSendCnt1 = 0;
      hDetlSendAmt1 = 0;

      checkOpen();
      selectDbbBill();
      checkClose();

      showLogMessage("I", "", " =============================================== \n");
      showLogMessage("I", "", "  DEBIT 扣款檔案:" + filename1 + "\n");
      showLogMessage("I", "", "      首筆 1筆, 尾筆 1筆\n");
      showLogMessage("I", "", String.format("      本日總筆數 [%d]\n", hDetlSendCnt1));
      showLogMessage("I", "", String.format("      本日總金額 [%f]\n", hDetlSendAmt1));
      showLogMessage("I", "", " =============================================== \n");
      
      procFTP(String.format("VDD04_REQ.%8.8s", hBusiBusinessDate));
      
      // ==============================================
      // 固定要做的
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
    hBusiBusinessDate = "";
    hTempBusinessDate = "";

    sqlCmd = "select business_date,";
    sqlCmd += "to_char(to_date(business_date,'yyyymmdd')+1 days,'yyyymmdd') h_temp_business_date ";
    sqlCmd += " from ptr_businday  ";
    sqlCmd += " fetch first 1 rows only ";
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_businday not found!", "", "");
    }
    if (recordCnt > 0) {
      hBusiBusinessDate = getValue("business_date");
      hTempBusinessDate = getValue("h_temp_business_date");
    }

  }

  /***********************************************************************
   * V1.00.20 讀參數檔取得是否要執行ID比對
   * 
   */
  String selectPtrSysParm() throws Exception {
	String parmMappingIdFlag = "";
    sqlCmd =    "select wf_value3 from ptr_sys_parm  where WF_PARM = 'IDTAB' AND WF_KEY = 'DBAA001'  ";
    sqlCmd += " fetch first 1 rows only ";
    int recordCnt = selectTable();
    if (recordCnt > 0) {
    	parmMappingIdFlag = getValue("wf_value3");
    } 
    
    showLogMessage("I", "", "  外國人ID 是否執行ID比對參數:  [ " + parmMappingIdFlag + "]");
    
    return (parmMappingIdFlag);
    
  }
  /** *************************************************************************/
  void checkClose() throws Exception {

    String tmpstr = String.format("3006%8.8s%010d%014.0f%164.164s", hBusiBusinessDate,
        hDetlSendCnt1, hDetlSendAmt1, " ");
    tmpstr = comc.fixLeft(tmpstr, 200) + "\r\n";
    writeBinFile2(0, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);

    closeBinaryOutput2(0);
  }

  /***********************************************************************/
  void checkOpen() throws Exception {
    filename1 = String.format("%s/media/dba/VDD04_REQ.%8.8s", comc.getECSHOME(), hBusiBusinessDate);
    filename1 = Normalizer.normalize(filename1, java.text.Normalizer.Form.NFKD);
    if (openBinaryOutput(filename1) == false) {
      comcr.errRtn(filename1, "檔案開啓失敗！", "");
    }
    String tmpstr = String.format("1006%8.8s%188.188s", hBusiBusinessDate, " ");

    tmpstr = comc.fixLeft(tmpstr, 200) + "\r\n";
    writeBinFile2(0, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);

  }

  /***********************************************************************/
  void selectDbbBill() throws Exception {

	sqlCmd = "select ";
    sqlCmd += "a.reference_no,";
    sqlCmd += "a.card_no,";
    sqlCmd += "a.purchase_date,";
    sqlCmd += "a.p_seqno,";
    sqlCmd += "a.post_date,";
    sqlCmd += "c.acct_no,"; 
    sqlCmd += "a.bill_type,";
    sqlCmd += "a.txn_code,";
    sqlCmd += "a.mcht_no,"; 
    sqlCmd += "a.source_curr,";
    sqlCmd += "decode(a.txn_code,'06',a.source_amt*-1,a.source_amt) as source_amt,"; 
    sqlCmd += "a.dest_curr ,";
    sqlCmd += "decode(a.txn_code,'06',a.dest_amt*-1,a.dest_amt) as dest_amt,";
    sqlCmd += "a.auth_code,";
    sqlCmd += "a.mcht_chi_name,";
    sqlCmd += "a.mcht_eng_name,"; 
    sqlCmd += "a.mcht_country,";
    sqlCmd += "a.mcht_category,";
    sqlCmd += "a.tx_seq, ";
    sqlCmd += "b.acct_holder_id,"; 
    sqlCmd += "a.rowid as rowid "; 
    sqlCmd += "from dbb_bill a,dba_acno b,dbc_card c ";
    sqlCmd += "where a.p_seqno = b.p_seqno ";
    sqlCmd += "and a.card_no = c.card_no ";
    sqlCmd += "and a.rsk_type <> '1' ";
    sqlCmd += "and a.bill_type = 'FISC' ";
    sqlCmd += "and a.txn_code in ('05','06','07') ";
    sqlCmd += "and a.dest_amt > 0 ";
    sqlCmd += "and a.fees_reference_no = '' ";  //V1.00.02 沒有收手續費的交易
    sqlCmd += "and a.mcht_category = '5999' ";
    sqlCmd += "and a.mcht_no like '9500%' ";
    sqlCmd += "and a.post_date >= ? ";  //20221201 (第一次補扣到20221130)
    sqlCmd += "and a.post_date < ? ";   //20221210
    sqlCmd += "order by a.post_date ";
    
    setString(1, parmStartDate);
    setString(2, parmEndDate);

    openCursor();
    while (fetchTable()) {
    	
      hDbblReferenceNo = getValue("reference_no");
      hDbblPSeqno = getValue("p_seqno");
      hDbblAcctNo = getValue("acct_no");
      hDbblMchtNo = getValue("mcht_no");
      hDbblAuthCode = getValue("auth_code"); 
      hDbblMchtCountry = getValue("mcht_country"); 
      hDbblMccCode = getValue("mcht_category");
      hDbblMchtEngName = getValue("mcht_eng_name");
      hDbblMchtChiName = getValue("mcht_chi_name");
      hDbblBillType = getValue("bill_type");
      hDbblTxnCode = getValue("txn_code");
      hDbblPostDate = getValue("post_date");
      hDbblPurchaseDate = getValue("purchase_date");
      hDbblCardNo = getValue("card_no");
      hDbblTxSeq = getValue("tx_seq");
      hDbblDestAmt = getValueDouble("dest_amt");
      hDcioId = getValue("acct_holder_id");
      hDbblRowid = getValue("rowid");
  	
      //負向交易不處理
      if("06".equals(hDbblTxnCode)) {
    	  showLogMessage("I", "", "  負向交易不處理,[card_no],[reference_no] ---  [ " + hDbblCardNo + "],[" + hDbblReferenceNo + "]");
    	  continue;
      }
      
      //V1.00.20 國外ID錯誤暫時以sysparm鍵入值mapping
      if ("Y".equals(parmMappingIdFlag)) {
      	if(hDcioId.length()==10 && hDcioId.substring(1,2).toUpperCase(Locale.TAIWAN).equals(hDcioId.substring(1,2).toLowerCase(Locale.TAIWAN))==false) {
      		String mappiedId = selectPtrSysIdtab(hDcioId);
      		if (mappiedId.length()>0) {
      			//showLogMessage("I", "", "  外國人ID mapping結果: [原ID],[新ID] ---  [ " + hDcioId + "],[" + mappiedId + "]");
      			hDcioId = mappiedId;
      		}
      	}
      }
      
      writeMediaFile();

    }
    
    closeCursor();

  }
  
  /***********************************************************************/
  @SuppressWarnings("unused")
  void writeMediaFile() throws Exception {

    String tmpstr = "";
    int ints = 0;
    int intd = 0;

    tempDeductSeq = String.format("%010.0f", getDebitSeq());
    
    vdd.type = "2"; // 固定值: 2
    vdd.bank = "006"; // 固定值: 006
    vdd.fromCode = "D04"; //D04:帳戶扣款
    
    if("".equals(hDbblTxSeq)) { 
    	vdd.txSeq = tempDeductSeq;
    } else {
    	vdd.txSeq = hDbblTxSeq;
    }
    
    vdd.purchaseDate = hDbblPurchaseDate;
    if ("95002001".equals(hDbblMchtNo)) {
    	vdd.amt=10;  //中華電信資費手續費10元
    } else {
    	vdd.amt=20;  //其餘手續費20元
    }
    vdd.acctNo = hDbblAcctNo;
    vdd.vdCardNo = hDbblCardNo;
    vdd.deductSeq = tempDeductSeq;
    vdd.acctHolderId = hDcioId;
    vdd.respondCode = "";
    vdd.electronicCardNo = ""; //空白
    vdd.mccCode = hDbblMccCode;
    vdd.merchantNo = hDbblMchtNo;
    vdd.authCode = hDbblAuthCode;  
    vdd.summaryCode = "VDFF";  //VD補扣款
    vdd.engDesc = hDbblMchtEngName;
    vdd.chiDesc = comcpi.commTransChinese(hDbblMchtChiName); //半型轉全型
    
    hDetlSendAmt1 = hDetlSendAmt1 + vdd.amt;
    hDetlSendCnt1++;
    tmpstr = vdd.allText() + "\r\n";
    writeBinFile2(0, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);

  }

  /***********************************************************************
   * V1.00.20 讀參數檔取得對應的ID
   * 
   */
  String selectPtrSysIdtab(String mappingId) throws Exception {
	String mappiedId = "";
    sqlCmd =    "select WF_DESC FROM PTR_SYS_IDTAB WHERE WF_TYPE ='DBAA001' and WF_ID = ?  ";
    sqlCmd += " fetch first 1 rows only ";
    
    setString(1, mappingId);
    
    int recordCnt = selectTable();
    if (recordCnt > 0) {
    	mappiedId = getValue("WF_DESC");
    } 
    
    return (mappiedId);
    
  }
  
  /*************************************************************************/
  double getDebitSeq() throws Exception {
    double seqno = 0;

    sqlCmd = "select dba_txnseq.nextval as nextval from dual";
    int nRecordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("GetDebitSeq() not found!", "", "");
    }

    seqno = getValueDouble("nextval");

    return (seqno);
  }
  
  void procFTP(String isFileName) throws Exception {
	  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
      commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
      commFTP.hEriaLocalDir = String.format("%s/media/dba", comc.getECSHOME());
      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
      commFTP.hEflgModPgm = javaProgram;
      

      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
      showLogMessage("I", "", "put " + isFileName + " 開始傳送....");
      int errCode = commFTP.ftplogName("NCR2TCB", "put " + isFileName);
      
      if (errCode != 0) {
          showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料"+" errcode:"+errCode);
          insertEcsNotifyLog(isFileName);          
      }  else {
    	  comc.fileRename2(String.format("%s/media/dba/", comc.getECSHOME())+isFileName,String.format("%s/media/dba/backup/", comc.getECSHOME())+isFileName);
      }
  }
  
  public int insertEcsNotifyLog(String fileName) throws Exception {
      setValue("crt_date", sysDate);
      setValue("crt_time", sysTime);
      setValue("unit_code", comr.getObjectOwner("3", javaProgram));
      setValue("obj_type", "3");
      setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
      setValue("notify_name", "媒體檔名:" + fileName);
      setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
      setValue("notify_desc2", "");
      setValue("trans_seqno", commFTP.hEflgTransSeqno);
      setValue("mod_time", sysDate + sysTime);
      setValue("mod_pgm", javaProgram);
      daoTable = "ecs_notify_log";

      insertTable();

      return (0);
  }
  
  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    DbaA001A proc = new DbaA001A();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

  class VDD {
    String type; // 明細資料
    String bank; // 銀行代碼
    String fromCode; // 交易代碼 D01:解圈 D02:解圈扣款 D03:補圈(強圈) D04:帳戶扣款
    String txSeq; // 圈存序號
    String purchaseDate; // 交易日期
    double amt; // 金額
    String acctNo; // 金融帳號
    String vdCardNo; // VD卡號
    String deductSeq; // 解圈扣款流水號
    String acctHolderId; // 法人戶統一編號 / 帳戶歸屬的ID
    String respondCode; // 處理回覆碼
    String electronicCardNo; // 票證外顯卡號
    String space; // 保留
    String summaryCode; // 摘要代號
    String engDesc; // 交易英文說明
    String chiDesc; // 交易中文說明
    String mccCode; // 特店類別碼
    String merchantNo; // 特店代號
    String authCode; //授權碼   //V1.00.18

    String allText() throws UnsupportedEncodingException {
      String rtn = "";
      rtn += comc.fixLeft(type, 1);
      rtn += comc.fixLeft(bank, 3);
      rtn += comc.fixLeft(fromCode, 3);
      rtn += comc.fixLeft(txSeq, 10);
      rtn += comc.fixLeft(purchaseDate, 8);
      rtn += String.format("%012.0f", amt);
      rtn += comc.fixLeft(acctNo, 13);
      rtn += comc.fixLeft(vdCardNo, 16);
      rtn += comc.fixLeft(deductSeq, 15);
      rtn += comc.fixLeft(acctHolderId, 10);
      rtn += comc.fixLeft(respondCode, 2);
      rtn += comc.fixLeft(electronicCardNo, 16);
      rtn += comc.fixLeft(mccCode, 4);
      rtn += comc.fixLeft(merchantNo, 15);
      rtn += comc.fixLeft(authCode, 6);  //V1.00.18
      rtn += comc.fixLeft(space, 30);
      rtn += comc.fixLeft(summaryCode, 4);
      rtn += comc.fixLeft(engDesc, 16);
      rtn += fixLeftAll(chiDesc, 16);

      return rtn;
    }
  }

  String fixLeftAll(String str, int len) throws UnsupportedEncodingException {
    int size = (Math.floorDiv(len, 100) + 1) * 100;
    String spc = "";
    for (int i = 0; i < size; i++)
      spc += "　";
    if (str == null)
      str = "";
    str = str + spc;
    byte[] bytes = str.getBytes("MS950");
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, 0, vResult, 0, len);

    return new String(vResult, "MS950");
  }
     
}
