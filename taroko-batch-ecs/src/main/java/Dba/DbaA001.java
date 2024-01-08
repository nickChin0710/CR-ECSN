/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109/05/22  V1.00.01    Pino      for TCB layout                            *
 *  109/06/24  V1.00.02    Jeff Kung 補圈存序號                   *
 *  109/07/03  V1.00.03    Zuwei     coding standard, rename field method & format       *
 *  109/07/20  V1.00.04    Jeff Kung for bill_type = 'OSSG' 將交易代碼設為'05'           *
 *  109/07/22  V1.00.05    shiyuqi     coding standard,                   *
 *  109/08/27  V1.00.06    JeffKung   fix for acaj缺少欄位                   *
 *  109/09/09  V1.00.07	   JeffKung   1.營業日才執行 ; 2.無圈存序號要多寫補圈檔                                                            *
 *  109/09/14  V1.00.08	   JeffKung   無圈存序號要多寫補圈檔                                                                                        *
 *  109/09/21  V1.00.09    JeffKung   add mcc&mchtNo                                                                *
 *  109/09/28  V1.00.10    JeffKung   txn_code對應先一律改為VDPY的對應                                                              *
 *  109/10/19  V1.00.11    JeffKung   ref_ip_code set to NCR2TCB                                                 *
 *  109-10-19  V1.00.12    shiyuqi       updated for project coding standard     *
 *  109-11-17  V1.00.13    Justin        isnull -> nvl 
 *  109/12/15  V1.00.14    JeffKung   corp_no from left join need func nvl()                                  *
 *  110-02-19  V1.00.15    JustinWu  item_ename -> acct_code
 *  110/12/17  V1.00.16    JeffKung   補圈序號回寫debt                                                                    *
 *  111/03/11  V1.00.17    JeffKung   批次解圈增加"RE10"條件                                                                           *
 *  111/03/17  V1.00.18    JeffKung   增加授權碼欄位及手續費拆分VDEF及VDFE                                *
 *  111/03/24  V1.00.19    JeffKung   CcaAuthTxlog的ref_no有重覆的問題                                                    *
 *  111/03/30  V1.00.20    JeffKung   暫時解決外國人ID錯誤的問題                                   
 *  111/04/06  V1.00.21    Justin     fix Portability Flaw: Locale Dependent Comparison*
 *  111/04/11  V1.00.22    JeffKung   VDEF增加判斷MCC='9***'                                                    *
 *  111/05/11  V1.00.23    JeffKung   無法更改ID時會將帳號對應ID放入新ID                                     *
 *  111/10/09  V1.00.24    Alex       增加處理dba_acaj.adj_reason_code , 將此欄位放入 dba_deduct_txn.abstract_code , 以利後續區別批次解圈原因 *
 *  111/11/18  V1.00.25    JeffKung   區分國外易手續費、E-GOV手續費
*                                     及VDFC:選牌費、繳交交通罰款、中華電信資費、換發行照、核定稅款等手續費
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

/*產生並送IBMDEBIT扣款檔處理程式*/
public class DbaA001 extends AccessDAO {
  private final String progname = "產生VD解圈扣款檔處理程式  111/11/18 V1.00.25";
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
  String hCallBatchSeqno = "";
  String iFileName = "";
  String iPostDate = "";

  String hBusiBusinessDate = "";
  String hTempBusinessDate = "";
  String hDatnFromCode = "";
  String hDadtReferenceNo = "";
  String hDadtPSeqno = "";
  String hDadtAcctType = "";
  // String h_dadt_acct_key = "";
  String hDadtAcctNo = "";
  String hDadtBankActno = "";
  String hDadtMchtNo = "";
  String hDadtMccCode = "";
  String hDadtBillType = "";
  String hDadtTxnCode = "";
  String hDadtItemPostDate = "";
  String hDadtPurchaseDate = "";
  String hDadtAcctItemEname = "";
  String hDadtAcctItemCname = "";
  String hDadtStmtCycle = "";
  String hDadtCardNo = "";
  String hDadtDebtStatus = "";
  String hDadtTransColDate = "";
  String hDadtTransBadDate = "";
  String hDadtIdPSeqno = "";
  String hDadtTxSeq = "";
  double hDadtBegBal = 0;
  double hDadtEndBal = 0;
  double hDadtDAvailBal = 0;
  double hDadtOrgReserveAmt = 0;
  double hDadtReserveAmt = 0;
  double hDatnOrgDeductAmt = 0;
  String hDcioId = "";
  String hDcioIdCode = "";
  String hDadmDeductType = "";
  String hMchtEngName = "";
  int hDadmDeductNdays = 0;
  String hMerchantCategorl = "";
  String hDadtRowid = "";
  int hTempCnt = 0;
  String hDetlDeductDate = "";
  String hDpteSummaryCode = "";
  String hDpteCommentCode = "";
  String hDpteTxnComment = "";
  int hDetlSendCnt = 0;
  int hDetlSendCnt1 = 0;
  int hDetlSendCnt2 = 0;
  int hDetlSendCnt3 = 0;
  int hDetlSendCnt4 = 0;
  double hDetlSendAmt = 0;
  double hDetlSendAmt1 = 0;
  double hDetlSendAmt2 = 0;
  double hDetlSendAmt3 = 0;
  double hDetlSendAmt4 = 0;
  String hDatnDeductSeq = "";
  String seqno = "";
  String hDbilMchtChiName = "";
  String dbilMchtChiName = "";
  String dbilMchtEngName = "";
  String hDbilAuthCode = "";
  String hDbilMchtCountry = "";
  String hDbcElectronicCode = "";
  String hElectronicCardNo = "";
  String tempDate = "";
  //--V1.00.24 新增欄位調整原因
  String hAdjReasonCode = "";
  Buf0 data = new Buf0();
  Buf1 data1 = new Buf1();
  VDD vdd = new VDD();
  String chiDate = "";
  String filename1 = "";
  String filename2 = "";
  String filename3 = "";
  String filename4 = "";
  
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
      if (args.length != 0) {
        comc.errExit("Usage : DbaA001 ", "");
      }

      // 固定要做的

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      commFTP = new CommFTP(getDBconnect(), getDBalias());
      comr = new CommRoutine(getDBconnect(), getDBalias());

      selectPtrBusinday();

      if(checkHoliday()) {
          exceptExit = 0;
          comcr.errRtn("非營業日,本程式不處理\n", "", hBusiBusinessDate);
      }
      
      /*
      tempDate = comcr.increaseDays(hBusiBusinessDate, 1);
      if (tempDate.equals(hTempBusinessDate) == false) {
        exceptExit = 0;
        comcr.errRtn("明日非營業日,本程式不處理\n", "", hCallBatchSeqno);
      }
      */
      
      if (selectDbaDeductCtl() != 0) {
        stderr = String.format("[%s] 扣款資料尚未送回, 不可再送扣款資料\n", hDetlDeductDate);
        exceptExit = 0;
        comcr.errRtn(stderr, "", hCallBatchSeqno);
      }

      //V1.00.20  暫時解決外國人ID錯誤的問題
      parmMappingIdFlag = selectPtrSysParm();
      
      hDetlSendCnt = 0;
      hDetlSendCnt1 = 0;
      hDetlSendCnt2 = 0;
      hDetlSendCnt3 = 0;
      hDetlSendCnt4 = 0;
      hDetlSendAmt = 0;
      hDetlSendAmt1 = 0;
      hDetlSendAmt2 = 0;
      hDetlSendAmt3 = 0;
      hDetlSendAmt4 = 0;

      checkOpen();
      selectDbaDebt();
      checkClose();

      insertDbaDeductCtl();

      showLogMessage("I", "", " =============================================== \n");
      showLogMessage("I", "", "  DEBIT 扣款檔案:" + filename1 + "\n");
      showLogMessage("I", "", "      首筆 1筆, 尾筆 1筆\n");
      showLogMessage("I", "", String.format("      本日總筆數 [%d]\n", hDetlSendCnt1));
      showLogMessage("I", "", String.format("      本日總金額 [%f]\n", hDetlSendAmt1));
      showLogMessage("I", "", " =============================================== \n");
      showLogMessage("I", "", " =============================================== \n");
      showLogMessage("I", "", "  DEBIT 扣款檔案:" + filename2 + "\n");
      showLogMessage("I", "", "      首筆 1筆, 尾筆 1筆\n");
      showLogMessage("I", "", String.format("      本日總筆數 [%d]\n", hDetlSendCnt2));
      showLogMessage("I", "", String.format("      本日總金額 [%f]\n", hDetlSendAmt2));
      showLogMessage("I", "", " =============================================== \n");
      showLogMessage("I", "", " =============================================== \n");
      showLogMessage("I", "", "  DEBIT 扣款檔案:" + filename3 + "\n");
      showLogMessage("I", "", "      首筆 1筆, 尾筆 1筆\n");
      showLogMessage("I", "", String.format("      本日總筆數 [%d]\n", hDetlSendCnt3));
      showLogMessage("I", "", String.format("      本日總金額 [%f]\n", hDetlSendAmt3));
      showLogMessage("I", "", " =============================================== \n");
      showLogMessage("I", "", " =============================================== \n");
      showLogMessage("I", "", "  DEBIT 扣款檔案:" + filename4 + "\n");
      showLogMessage("I", "", "      首筆 1筆, 尾筆 1筆\n");
      showLogMessage("I", "", String.format("      本日總筆數 [%d]\n", hDetlSendCnt4));
      showLogMessage("I", "", String.format("      本日總金額 [%f]\n", hDetlSendAmt4));
      showLogMessage("I", "", " =============================================== \n");
      
      procFTP(String.format("VDD01_REQ.%8.8s", hBusiBusinessDate));
      procFTP(String.format("VDD02_REQ.%8.8s", hBusiBusinessDate));
      procFTP(String.format("VDD03_REQ.%8.8s", hBusiBusinessDate));
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
      comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
      hBusiBusinessDate = getValue("business_date");
      hTempBusinessDate = getValue("h_temp_business_date");
    }

  }

  /***********************************************************************/
  boolean checkHoliday() throws Exception {

    sqlCmd =   "select holiday ";
    sqlCmd += " from ptr_holiday  ";
    sqlCmd += "where holiday = ? ";
    setString(1, hBusiBusinessDate);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      return true;
    }
    return false;
  }

  /***********************************************************************/
  int selectDbaDeductCtl() throws Exception {
    hDetlDeductDate = "";
    sqlCmd = "select deduct_date ";
    sqlCmd += " from dba_deduct_ctl  ";
    sqlCmd += "where receive_date =''  ";
    sqlCmd += "and proc_type = 'A'  ";
    sqlCmd += " fetch first 1 rows only ";
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDetlDeductDate = getValue("deduct_date");
    } else
      return (0);
    return (1);
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

    tmpstr = String.format("3006%8.8s%010d%014.0f%164.164s", hBusiBusinessDate, hDetlSendCnt2,
        hDetlSendAmt2, " ");
    tmpstr = comc.fixLeft(tmpstr, 200) + "\r\n";
    writeBinFile2(1, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);

    tmpstr = String.format("3006%8.8s%010d%014.0f%164.164s", hBusiBusinessDate, hDetlSendCnt3,
        hDetlSendAmt3, " ");
    tmpstr = comc.fixLeft(tmpstr, 200) + "\r\n";
    writeBinFile2(2, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);

    tmpstr = String.format("3006%8.8s%010d%014.0f%164.164s", hBusiBusinessDate, hDetlSendCnt4,
        hDetlSendAmt4, " ");
    tmpstr = comc.fixLeft(tmpstr, 200) + "\r\n";
    writeBinFile2(3, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);

    closeBinaryOutput2(0);
    closeBinaryOutput2(1);
    closeBinaryOutput2(2);
    closeBinaryOutput2(3);
  }

  /***********************************************************************/
  void checkOpen() throws Exception {
    filename1 = String.format("%s/media/dba/VDD01_REQ.%8.8s", comc.getECSHOME(), hBusiBusinessDate);
    filename1 = Normalizer.normalize(filename1, java.text.Normalizer.Form.NFKD);
    if (openBinaryOutput(filename1) == false) {
      comcr.errRtn(filename1, "檔案開啓失敗！", hCallBatchSeqno);
    }

    filename2 = String.format("%s/media/dba/VDD02_REQ.%8.8s", comc.getECSHOME(), hBusiBusinessDate);
    filename2 = Normalizer.normalize(filename2, java.text.Normalizer.Form.NFKD);
    if (openBinaryOutput(filename2) == false) {
      comcr.errRtn(filename2, "檔案開啓失敗！", hCallBatchSeqno);
    }

    filename3 = String.format("%s/media/dba/VDD03_REQ.%8.8s", comc.getECSHOME(), hBusiBusinessDate);
    filename3 = Normalizer.normalize(filename3, java.text.Normalizer.Form.NFKD);
    if (openBinaryOutput(filename3) == false) {
      comcr.errRtn(filename3, "檔案開啓失敗！", hCallBatchSeqno);
    }

    filename4 = String.format("%s/media/dba/VDD04_REQ.%8.8s", comc.getECSHOME(), hBusiBusinessDate);
    filename4 = Normalizer.normalize(filename4, java.text.Normalizer.Form.NFKD);
    if (openBinaryOutput(filename4) == false) {
      comcr.errRtn(filename4, "檔案開啓失敗！", hCallBatchSeqno);
    }
    
    String tmpstr = String.format("1006%8.8s%188.188s", hBusiBusinessDate, " ");

    tmpstr = comc.fixLeft(tmpstr, 200) + "\r\n";
    writeBinFile2(0, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
    writeBinFile2(1, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
    writeBinFile2(2, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
    writeBinFile2(3, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);

  }

  /***********************************************************************/
  void selectDbaDebt() throws Exception {
	  
	String newID = "";

    sqlCmd = "select ";
    sqlCmd += "'1' cod,";
    sqlCmd += "a.reference_no,";
    sqlCmd += "a.p_seqno,";
    sqlCmd += "a.acct_type,";
    sqlCmd += "a.acct_no,"; // -5
    sqlCmd += "a.bank_actno,";
    sqlCmd += "a.mcht_no,";
    sqlCmd += "a.bill_type,";
    sqlCmd += "a.txn_code,";
    sqlCmd += "a.item_post_date,";
    sqlCmd += "a.purchase_date,"; // -10
    sqlCmd += "a.acct_code ,";
    sqlCmd += "a.stmt_cycle,";
    sqlCmd += "a.card_no,";
    sqlCmd += "a.debt_status,";
    sqlCmd += "a.trans_col_date,"; // -15
    sqlCmd += "a.trans_bad_date,";
    sqlCmd += "a.id_p_seqno,";
    sqlCmd += "a.beg_bal,";
    sqlCmd += "a.end_bal,";
    sqlCmd += "a.d_avail_bal,"; // -20
    sqlCmd += "a.org_reserve_amt,";
    sqlCmd += "a.reserve_amt,";
    sqlCmd += "a.end_bal,";
    sqlCmd += "decode(c.corp_no,'',c.id_no,c.corp_no) h_dcio_id,";
    sqlCmd += "decode(c.corp_no,'',c.id_no_code,'') id_no_code,"; // -25
    sqlCmd += "b.deduct_type,";
    sqlCmd += "b.deduct_n_days,";
    sqlCmd += "a.tx_seq, ";
    sqlCmd += "d.mcht_eng_name as hMchtEngName ,";         //mchtEngName
    sqlCmd += "a.mcc_code,";
    sqlCmd += "d.auth_code,";  //V1.00.18
    sqlCmd += "d.mcht_country,";  //V1.00.18
    sqlCmd += "e.acct_holder_id,"; //V1.00.23
    sqlCmd += "a.rowid as rowid, "; // --29
    sqlCmd += "'' as adj_reason_code "; //--V1.00.24 調整原因只在 dba_acaj有 所以放空白
    sqlCmd += "from dbc_idno c,dba_debt a,dba_decol_parm b ";
    sqlCmd += " left join dbb_bill d on d.reference_no = a.reference_no ";
    sqlCmd += " left join dba_acno e on e.p_seqno = a.p_seqno ";
    sqlCmd += "where a.end_bal > 0 ";
    sqlCmd += "and c.id_p_seqno = a.id_p_seqno ";
    sqlCmd += "and a.acct_type = b.acct_type ";
    sqlCmd += "UNION ";
    sqlCmd += "select '2' cod, "; // --1
    sqlCmd += "a.reference_no, ";
    sqlCmd += "a.p_seqno, ";
    sqlCmd += "a.acct_type, ";
    sqlCmd += "a.acct_no, "; // --5
    sqlCmd += "'', ";
    sqlCmd += "a.mcht_no, ";
    sqlCmd += "'',  ";
    sqlCmd += "'05', ";
    sqlCmd += "a.post_date, ";
    sqlCmd += "a.purchase_date, "; // --10
    sqlCmd += "a.acct_code, ";
    sqlCmd += "'', ";
    sqlCmd += "a.card_no, ";
    sqlCmd += "'', ";
    sqlCmd += "'', "; // --15
    sqlCmd += "'', ";
    sqlCmd += "'', ";
    sqlCmd += "0, ";
    sqlCmd += "a.orginal_amt, ";
    sqlCmd += "0, "; // --20
    sqlCmd += "a.orginal_amt, ";
    sqlCmd += "a.orginal_amt, ";
    sqlCmd += "a.orginal_amt, ";
    sqlCmd += "decode(nvl(c.corp_no,''),'',substr(b.acct_key,1,10),c.corp_no) h_dcio_id , ";
    sqlCmd += "decode(nvl(c.corp_no,''),'',substr(b.acct_key,11,1),'') id_no_code , "; // --25
    sqlCmd += "'1', ";
    sqlCmd += "0, ";
    sqlCmd += "a.tx_seq, ";
    sqlCmd += " ( select d1.mcht_name  from cca_auth_txlog d1 where d1.card_no = a.card_no and d1.tx_seq = a.tx_seq and d1.ref_no = a.reference_no fetch first 1 rows only ) as  hMchtEngName , ";    
    sqlCmd += " ( select d2.mcc_code  from cca_auth_txlog d2 where d2.card_no = a.card_no and d2.tx_seq = a.tx_seq and d2.ref_no = a.reference_no fetch first 1 rows only ) as  mcc_code , ";
    sqlCmd += " ( select d3.auth_no  from cca_auth_txlog d3 where d3.card_no = a.card_no and d3.tx_seq = a.tx_seq and d3.ref_no = a.reference_no fetch first 1 rows only ) as  auth_code , ";
    //sqlCmd += "d.mcht_name  as hMchtEngName ,";    //mchtEngName
    //sqlCmd += "d.mcc_code, ";   //mcc_code
    //sqlCmd += "d.auth_no  as auth_code, "; //auth_code   V1.00.18
    sqlCmd += " '' as mcht_country,";  //V1.00.18
    sqlCmd += " '' as acct_holder_id,"; //V1.00.23
    sqlCmd += "a.rowid as rowid, "; // --29
    sqlCmd += "a.adj_reason_code "; //--V1.00.24 調整原因
    sqlCmd += "from dba_acno b,dba_acaj a  ";
    sqlCmd += "  left join crd_corp c on c.corp_p_seqno = b.corp_p_seqno "; // find
                                                                            // corp_no
                                                                            // in
                                                                            // crd_corp
    sqlCmd += "where a.orginal_amt > 0 ";
    sqlCmd += "and a.p_seqno = b.p_seqno ";
    //sqlCmd += "and a.reference_no = d.ref_no ";
    //sqlCmd += "and a.tx_seq = d.tx_seq ";   //V1.00.19 加比tx_seq
    //sqlCmd += "and d.unlock_flag in ('','N') ";
    //sqlCmd += "and a.card_no = d.card_no ";
    sqlCmd += "and a.proc_flag = 'N' ";
    sqlCmd += "and a.adjust_type = 'RE10' ";
    sqlCmd += "and a.acct_no <> '' ";
    sqlCmd += "and b.acct_key <> '' ";
    sqlCmd += "and a.apr_flag = 'Y' ";
    sqlCmd += "order by 1 desc";

    //int recordCnt = selectTable();
    
    openCursor();
    while (fetchTable()) {
      hDatnFromCode = getValue("cod");
      hDadtReferenceNo = getValue("reference_no");
      hDadtPSeqno = getValue("p_seqno");
      hDadtAcctType = getValue("acct_type");
      hDadtAcctNo = getValue("acct_no");
      hDadtBankActno = getValue("bank_actno");
      hDadtMchtNo = getValue("mcht_no");
      hDbilAuthCode = getValue("auth_code"); //V1.00.18
      hDbilMchtCountry = getValue("mcht_country"); //V1.00.18
      hDadtMccCode = getValue("mcc_code");
      hMchtEngName = getValue("hMchtEngName");
      hDadtBillType = getValue("bill_type");
      hDadtTxnCode = getValue("txn_code");
      hAdjReasonCode = getValue("adj_reason_code");
      //若不為系統產生的費用類交易, 將txnCode轉為"05" 
      //系統產生的費用類交易OSSG,保留原始txnCode
      if (hDadtBillType.equals("OSSG")==false) {
    	  hDadtTxnCode = "05";
      }
      
      /*remark
      if (!hDadtTxnCode.equals("05")) {
    	  hDadtTxnCode = "05";
      }
      */
            
      hDadtItemPostDate = getValue("item_post_date");
      hDadtPurchaseDate = getValue("purchase_date");
      hDadtAcctItemEname = getValue("acct_code");
      hDadtStmtCycle = getValue("stmt_cycle");
      hDadtCardNo = getValue("card_no");
      hDadtDebtStatus = getValue("debt_status");
      hDadtTransColDate = getValue("trans_col_date");
      hDadtTransBadDate = getValue("trans_bad_date");
      hDadtIdPSeqno = getValue("id_p_seqno");
      hDadtTxSeq = getValue("tx_seq");
      hDadtBegBal = getValueDouble("beg_bal");
      hDadtEndBal = getValueDouble("end_bal");
      hDadtDAvailBal = getValueDouble("d_avail_bal");
      hDadtOrgReserveAmt = getValueDouble("org_reserve_amt");
      hDadtReserveAmt = getValueDouble("reserve_amt");
      hDatnOrgDeductAmt = getValueDouble("end_bal");
      hDcioId = getValue("h_dcio_id");
      hDcioIdCode = getValue("id_no_code");
      hDadmDeductType = getValue("deduct_type");
      hDadmDeductNdays = getValueInt("deduct_n_days");
      // h_merchant_categorl = getValue("order_fg", i);
      hDadtRowid = getValue("rowid");

      if (hDadmDeductType.equals("2")) {
        tempDate = comcr.increaseDays(hDadtItemPostDate, hDadmDeductNdays);
        if (hBusiBusinessDate.compareTo(tempDate) > 0)
          continue;
      }
      
    //V1.00.23 該帳號已被change id, 且無法更改id成功, 將新ID保留在acct_holder_id
      newID = getValue("acct_holder_id");
      if ("".equals(newID)==false && newID.equals(hDcioId) ==false ) {
    	  showLogMessage("I", "", "  帳號歸屬ID mapping結果: [原ID],[新ID] ---  [ " + hDcioId + "],[" + newID + "]");
    	  hDcioId = newID;
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
      
      insertDbaDeductTxn();
      
      //如果是VD悠遊的交易借位放在mchtEngName的前兩碼
      hDpteSummaryCode = "";
      if ("TSCC".equals(hDadtBillType)) {
    	  hDpteSummaryCode = comc.getSubString(hMchtEngName,0,4);
      } else {
    	  if (selectDbpTxnCode() != 0) {
    		  updateDbaDeductTxn();
    		  continue;
    	  }
      }
      writeMediaFile();
      
      if (hDatnFromCode.equals("2"))
        updateDbaAcaj();
      else 
    	updateDbaDebt();
      
    }
    
    closeCursor();

  }

  /***********************************************************************/
  void insertDbaDeductTxn() throws Exception {
    tempDate = String.format("%010.0f", getDebitSeq());
    hDatnDeductSeq = tempDate;

    setValue("crt_date", hBusiBusinessDate);
    setValue("crt_time", sysTime);
    setValue("deduct_date", hBusiBusinessDate);  //TCB同營業日期
    setValue("deduct_seq", hDatnDeductSeq);
    setValue("reference_no", hDadtReferenceNo);
    setValue("acct_no", hDadtAcctNo);
    setValue("p_seqno", hDadtPSeqno);
    setValue("acct_type", hDadtAcctType);
    // setValue("acct_key", h_dadt_acct_key);
    setValue("id_p_seqno", hDadtIdPSeqno);
    setValue("id_no", hDcioId);
    setValue("id_code", hDcioIdCode);
    setValue("card_no", hDadtCardNo);
    setValue("bank_actno", hDadtBankActno);
    setValue("merchant_no", hDadtMchtNo);
    setValue("txn_code", hDadtTxnCode);
    setValue("item_post_date", hDadtItemPostDate);
    setValue("purchase_date", hDadtPurchaseDate);
    setValueDouble("beg_bal", hDadtBegBal);
    setValueDouble("end_bal", hDadtEndBal);
    setValueDouble("d_avail_bal", hDadtDAvailBal);
    setValue("acct_code", hDadtAcctItemEname);
    setValue("acct_item_cname", hDadtAcctItemCname);
    setValueDouble("org_reserve_amt", hDadtOrgReserveAmt);
    setValueDouble("reserve_amt", hDadtReserveAmt);
    setValueDouble("org_deduct_amt", hDatnOrgDeductAmt);
    setValue("tx_seq", hDadtTxSeq);
    setValue("from_code", hDatnFromCode);
    setValue("debt_status", hDadtDebtStatus);
    setValue("trans_col_date", hDadtTransColDate);
    setValue("trans_bad_date", hDadtTransBadDate);
    setValue("deduct_proc_code", "99");
    setValue("stmt_cycle", hDadtStmtCycle);
    //--2022/10/09 : 新增處理調整原因欄位
    setValue("abstract_code",hAdjReasonCode);
    setValue("mod_user", "system");
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", javaProgram);
    daoTable = "dba_deduct_txn";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_dba_deduct_txn duplicate!", "", hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  int selectDbpTxnCode() throws Exception {
    hDpteSummaryCode = "";
    hDpteCommentCode = "";
    hDpteTxnComment = "";

    sqlCmd = "select summary_code,";
    sqlCmd += "comment_code,";
    sqlCmd += "txn_comment ";
    sqlCmd += " from dbp_txn_code  ";
    sqlCmd += "where txn_code = ? ";
    setString(1, hDadtTxnCode);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDpteSummaryCode = getValue("summary_code");
      hDpteCommentCode = getValue("comment_code");
      hDpteTxnComment = getValue("txn_comment");
    } else
      return (1);
    return (0);
  }

  /***********************************************************************/
  void updateDbaDeductTxn() throws Exception {
    daoTable = "dba_deduct_txn";
    updateSQL = "deduct_proc_code = 'TX'";
    whereStr = "where deduct_seq = ? ";
    setString(1, hDatnDeductSeq);
    updateTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("update_dba_deduct_txn not found!", "", hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  @SuppressWarnings("unused")
  void writeMediaFile() throws Exception {

    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String tmpstr3 = "";
    int ints = 0;
    int intd = 0;

    hDetlSendCnt++;
    // h_detl_send_amt = h_detl_send_amt + h_dadt_end_bal;

    selectDbbBill();
    selectElectronicCardNo();

    vdd.type = "2"; // 固定值: 2
    vdd.bank = "006"; // 固定值: 006
    // D01:解圈 D02:解圈扣款 D03:補圈(強圈) D04:帳戶扣款
    if (hDatnFromCode.equals("1")) {
      vdd.fromCode = "D02";
    } else if (hDatnFromCode.equals("2")) {
      vdd.fromCode = "D01";
    }
    vdd.txSeq = hDadtTxSeq;
    vdd.purchaseDate = hDadtPurchaseDate;
    vdd.amt = hDadtEndBal;
    vdd.acctNo = hDadtAcctNo;
    vdd.vdCardNo = hDadtCardNo;
    vdd.deductSeq = hDatnDeductSeq;
    vdd.acctHolderId = hDcioId;
    vdd.respondCode = "";
    vdd.electronicCardNo = hElectronicCardNo;
    vdd.mccCode = hDadtMccCode;
    vdd.merchantNo = hDadtMchtNo;
    vdd.authCode = hDbilAuthCode;  //V1.00.18

    vdd.summaryCode = hDpteSummaryCode;
    
    //V1.00.18若為交易費用則改為VDFE : VD清算  ; VDEF : EGOV費 
    //若特店國別有值且不為TW, 就歸類為VDFE: VD清算, 否則為VDEF:EGOV費
    if ("FIFC".equals(hDadtBillType)) {
    	if (hDbilMchtCountry.length() >= 2 && hDbilMchtCountry.substring(0,2).equalsIgnoreCase("TW") == false) {
    		vdd.summaryCode = "VDFE";
    	} else {
    		//V1.00.22 VDEF增加判斷MCC='9***'
    		if (hDadtMccCode.length() >= 1 && "9".equals(hDadtMccCode.substring(0,1)) ) {
    			vdd.summaryCode = "VDEF";
    		} else {
    			vdd.summaryCode = "VDFE";
    		}
    	}
    }
    
    vdd.engDesc = dbilMchtEngName;
    vdd.chiDesc = comcpi.commTransChinese(dbilMchtChiName);
    
    if (vdd.fromCode.equals("D01")) {
      hDetlSendAmt1 = hDetlSendAmt1 + hDadtEndBal;
      hDetlSendCnt1++;
      vdd.summaryCode = "";
      vdd.engDesc = hMchtEngName;  //英文特店另外從授權資料填入
      tmpstr = vdd.allText() + "\r\n";
      writeBinFile2(0, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
    } else if (vdd.fromCode.equals("D02")) {
    	
    	//若是無圈存序號的交易(線上加檔/問交結案), 需送一筆補圈交易
    	if (hDadtTxSeq.length()==0) {
    		 vdd.txSeq = String.format("%010.0f",getCCAVDTXNSEQNO());  //補0至10碼
    		 vdd.fromCode = "D03";
    		 hDetlSendAmt3 = hDetlSendAmt3 + hDadtEndBal;
    	     hDetlSendCnt3++;
    	     vdd.summaryCode = "";
    	     tmpstr = vdd.allText() + "\r\n";
    	     writeBinFile2(2, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
    	}
    	
      vdd.fromCode = "D02";
      vdd.summaryCode = hDpteSummaryCode;
      
      //V1.00.18若為交易費用則改為VDFE
      if ("FIFC".equals(hDadtBillType)) {
      	vdd.summaryCode = "VDFE";
      }
      
      //V1.00.18若為交易費用則改為VDFE : VD清算  ; VDEF : EGOV費 
      //若特店國別有值且不為TW, 就歸類為VDFE: VD清算, 否則為VDEF:EGOV費
      if ("FIFC".equals(hDadtBillType)) {
    	  if (hDbilMchtCountry.length() >= 2 && hDbilMchtCountry.substring(0,2).equalsIgnoreCase("TW") == false) {
    		  vdd.summaryCode = "VDFE";
    	  } else {
    		  //V1.00.22 VDEF增加判斷MCC='9***'
    		  if (hDadtMccCode.length() >= 1 && "9".equals(hDadtMccCode.substring(0,1)) ) {
    			  vdd.summaryCode = "VDEF";
      		  } else {
      			  vdd.summaryCode = "VDFE";
      		  }
    	  }
      }
      
      hDetlSendAmt2 = hDetlSendAmt2 + hDadtEndBal;
      hDetlSendCnt2++;
      tmpstr = vdd.allText() + "\r\n";
      writeBinFile2(1, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);

    } else if (vdd.fromCode.equals("D03")) {
      hDetlSendAmt3 = hDetlSendAmt3 + hDadtEndBal;
      hDetlSendCnt3++;
      vdd.summaryCode = "";
      tmpstr = vdd.allText() + "\r\n";
      writeBinFile2(2, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
    } else if (vdd.fromCode.equals("D04")) {
      hDetlSendAmt4 = hDetlSendAmt4 + hDadtEndBal;
      hDetlSendCnt4++;
      tmpstr = vdd.allText() + "\r\n";
      writeBinFile2(3, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
    }

  }

  /***********************************************************************/
  void selectElectronicCardNo() throws Exception {
    hDbcElectronicCode = "";
    hElectronicCardNo = "";

    sqlCmd = "select electronic_code ";
    sqlCmd += " from dbc_card ";
    sqlCmd += " where card_no = ? ";
    setString(1, hDadtCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDbcElectronicCode = getValue("electronic_code");
    }

    if (hDbcElectronicCode.equals("01")) {
      sqlCmd = "select nvl(tsc.tsc_card_no, '') as electronic_card_no ";
      sqlCmd += " from dbc_card as c INNER JOIN tsc_vd_card as tsc ";
      sqlCmd += " on c.electronic_code='01' and  c.card_no = tsc.vd_card_no ";
      sqlCmd += " where c.card_no = ? ";
      setString(1, hDadtCardNo);
      recordCnt = selectTable();
      if (recordCnt > 0) {
        hElectronicCardNo = getValue("electronic_card_no");
      }
    }

  }

  /***********************************************************************/
  void selectDbbBill() throws Exception {
    hDbilMchtChiName = "";
    dbilMchtChiName = "";
    dbilMchtEngName = "";
    if (hDadtReferenceNo.isEmpty())
      return;
    sqlCmd =
        "select decode(dest_curr,source_curr,mcht_chi_name,mcht_eng_name) h_dbil_mcht_chi_name,";
    sqlCmd += " mcht_chi_name, ";
    sqlCmd += " mcht_eng_name ";
    sqlCmd += " from dbb_bill  ";
    sqlCmd += "where reference_no = ? ";
    setString(1, hDadtReferenceNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDbilMchtChiName = getValue("h_dbil_mcht_chi_name");
      dbilMchtChiName = getValue("mcht_chi_name");
      dbilMchtEngName = getValue("mcht_eng_name");
    }

  }

  /***********************************************************************/
  void updateDbaAcaj() throws Exception {
    daoTable = "dba_acaj";
    updateSQL = "proc_flag = 'M',";
    updateSQL += " mod_time = sysdate,";
    updateSQL += " deduct_seq = ?,";
    updateSQL += " mod_pgm = ?";
    whereStr = "where rowid = ? ";
    setString(1, hDatnDeductSeq);
    setString(2, javaProgram);
    setRowId(3, hDadtRowid);
    updateTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("update_dba_acaj not found!", "", hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  void updateDbaDebt() throws Exception {
	  
	  //原本沒有圈存序號的才要回寫
	  if (StringUtils.isNotBlank(hDadtTxSeq)) {
		  return;
	  }

    daoTable = "dba_debt";
    updateSQL = "tx_seq = ? ,";
    updateSQL += " mod_time = sysdate,";
    updateSQL += " mod_pgm = ?";
    whereStr = "where rowid = ? ";
    setString(1, vdd.txSeq);
    setString(2, javaProgram);
    setRowId(3, hDadtRowid);
    updateTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("update_dba_debt not found!", "", hDadtReferenceNo);
    }

  }
  
  /***********************************************************************/
  void insertDbaDeductCtl() throws Exception {
    setValue("proc_type", "A");
    setValue("deduct_date", hBusiBusinessDate);  //TCB同營業日期
    setValue("crt_date", hBusiBusinessDate);
    setValue("crt_time", sysTime);
    setValueInt("send_cnt", hDetlSendCnt2);  //D02解圈扣款筆數
    setValueDouble("send_amt", hDetlSendAmt2);  //D02解圈扣款金額
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", javaProgram);
    daoTable = "dba_deduct_ctl";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_dba_deduct_ctl duplicate!", "", hCallBatchSeqno);
    }

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
      comcr.errRtn("GetDebitSeq() not found!", "", hCallBatchSeqno);
    }

    seqno = getValueDouble("nextval");

    return (seqno);
  }
  
  /*************************************************************************/
  double getCCAVDTXNSEQNO() throws Exception {
    double seqno = 0;

    sqlCmd = "select CCA_VDTXN_SEQNO.nextval as nextval from dual";
    int nRecordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("GetCCAVDTXNSEQNO() not found!", "", hCallBatchSeqno);
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
    DbaA001 proc = new DbaA001();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

  /***********************************************************************/
  class Buf0 {
    String data1;
    String data2;
    String data3;
    String len;

    String allText() throws UnsupportedEncodingException {
      String rtn = "";
      rtn += fixLeft(data1, 94);
      rtn += fixLeft(data2, 14);
      rtn += fixLeft(data3, 12);
      rtn += fixLeft(len, 1);
      return rtn;
    }

  }

  void splitBuf0(String str) throws UnsupportedEncodingException {
    byte[] bytes = str.getBytes("Cp1047");
    data.data1 = comc.subString(bytes, 0, 94, "Cp1047");
    data.data2 = comc.subString(bytes, 94, 14, "Cp1047");
    data.data3 = comc.subString(bytes, 108, 12, "Cp1047");
    data.len = comc.subString(bytes, 120, 1, "Cp1047");
  }

  /***********************************************************************/
  class Buf1 {
    String data1;
    String data4;
    String data2;
    String data3;
    String len;

    String allText() throws UnsupportedEncodingException {
      String rtn = "";
      rtn += fixLeft(data1, 94);
      rtn += fixLeft(data4, 4);
      rtn += fixLeft(data2, 10);
      rtn += fixLeft(data3, 12);
      rtn += fixLeft(len, 1);
      return rtn;
    }

  }

  void splitBuf1(String str) throws UnsupportedEncodingException {
    byte[] bytes = str.getBytes("Cp1047");
    data1.data1 = comc.subString(bytes, 0, 94, "Cp1047");
    data1.data4 = comc.subString(bytes, 94, 4, "Cp1047");
    data1.data2 = comc.subString(bytes, 98, 10, "Cp1047");
    data1.data3 = comc.subString(bytes, 108, 12, "Cp1047");
    data1.len = comc.subString(bytes, 120, 1, "Cp1047");
  }

  /*******************************************************************************/
  String fixLeft(String str, int len) throws UnsupportedEncodingException {
    int size = (Math.floorDiv(len, 100) + 1) * 100;
    String spc = "";
    for (int i = 0; i < size; i++)
      spc += " ";
    if (str == null)
      str = "";
    str = str + spc;
    byte[] bytes = str.getBytes("MS950");
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, 0, vResult, 0, len);

    return new String(vResult, "MS950");
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
