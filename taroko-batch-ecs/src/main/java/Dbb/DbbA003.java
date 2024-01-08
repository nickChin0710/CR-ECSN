/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
 *  109/07/03  V1.00.01    Zuwei     coding standard, rename field method & format                   *
 *  109/07/22  V1.00.02    shiyuqi     coding standard,                   *
 *  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
 *  111/06/01  V1.00.04   JeffKung    解決稅費請款時dest_curr會是空值的問題。                                     *
******************************************************************************/

package Dbb;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*格式查核*/
public class DbbA003 extends AccessDAO {
  private final String progname = "格式查核  111/06/01  V1.00.04";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  String hTempUser = "";
  final  int debug = 1;

  String prgmId = "DbbA003";
  String rptName = "DBB_A003R1";
  int actCnt = 0;
  List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
  int rptSeq = 0;
  String buf = "";
  String szTmp = "";
  String stderr = "";
  long hModSeqno = 0;
  String hModUser = "";
  String hModTime = "";
  String hModPgm = "";
  String hCallBatchSeqno = "";
  String hCurpModPgm = "";
  String hCurpModTime = "";
  String hCurpModUser = "";
  long hCurpModSeqno = 0;

  String hBusinessDate = "";
  String hSystemDate = "";
  String hSystemDateF = "";
  String hDcurReferenceNo = "";
  String hDcurBillType = "";
  String hDcurTxnCode = "";
  String hDcurCardNo = "";
  String hDcurFilmNo = "";
  String hDcurPurchaseDate = "";
  // String h_dcur_dest_amt_char = "";
  double hDcurDestAmt = 0;
  String hDcurDestCurr = "";
  // String h_dcur_source_amt_char = "";
  double hDcurSourceAmt = 0;
  String hDcurSourceCurr = "";
  String hDcurMchtEngName = "";
  String hDcurMchtCity = "";
  String hDcurMchtCountry = "";
  String hDcurMchtCategory = "";
  String hDcurMchtZip = "";
  String hDcurMchtState = "";
  // String h_dcur_usage_code = "";
  // String h_dcur_reason_code = "";
  String hDcurAuthCode = "";
  String hDcurBatchNo = "";
  String hDcurProcessDate = "";
  String hDcurMchtNo = "";
  String hDcurMchtChiName = "";
  String hDcurContractNo = "";
  // String h_dcur_goods_name = "";
  // String h_dcur_original_no = "";
  // String h_dcur_telephone_no = "";
  int hDcurTerm = 0;
  int hDcurTotalTerm = 0;
  // String h_dcur_prod_name = "";
  String hDcurAcctEngShortName = "";
  String hDcurAcctChiShortName = "";
  String hDcurDoubtType = "";
  String hDcurAcctType = "";
  // String h_dcur_acct_key = "";
  // String h_dcur_acct_status = "";
  // String h_dcur_block_status = "";
  // String h_dcur_block_date = "";
  // String h_dcur_pay_by_stage_flag = "";
  // String h_dcur_autopay_acct_no = "";
  String hDcurCurrCode = "";
  // String h_dcur_oppost_date = "";
  String hDcurPromoteDept = "";
  String hDcurProdNo = "";
  String hDcurGroupCode = "";
  String hDcurBinType = "";
  String hDcurPSeqno = "";
  String hDcurIdPSeqno = "";
  String hDcurThisCloseDate = "";
  String hDcurReferenceNoOriginal = "";
  String hDcurRowid = "";
  long hTempdig = 0;
  String hTempstr = "";
  String hPrintName = "";
  String hRptName = "";

  String hDcurModPgm = "";
  String hDcurModTime = "";
  String hDcurModUser = "";
  long hDcurModSeqno = 0;
  int indexCnt = 0;
  int pageCnt = 0;
  double totalPSourceAmt = 0;
  double totalPDestAmt = 0;
  double pagePSourceAmt = 0;
  double pagePDestAmt = 0;
  int lineCnt = 0;
  int pagePositiveCnt = 0;
  int totalPositiveCnt = 0;
  double pageNDestAmt = 0;
  double pageNSourceAmt = 0;
  double totalNDestAmt = 0;
  double totalNSourceAmt = 0;
  int pageNegativeCnt = 0;
  int totalNegativeCnt = 0;
  private long totCnt = 0;

  // ***********************************************************

  public int mainProcess(String[] args) {
    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);
      // =====================================
      if (args.length != 0 && args.length != 1) {
        comc.errExit("Usage : DbbA003 batch_seq", "");
      }

      // 固定要做的

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

      String checkHome = comc.getECSHOME();
      if (comcr.hCallBatchSeqno.length() > 6) {
        if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6)
            .equals(comc.getSubString(checkHome, 0, 6))) {
          comcr.hCallBatchSeqno = "no-call";
        }
      }

      comcr.hCallRProgramCode = javaProgram;
      hTempUser = "";
      if (comcr.hCallBatchSeqno.length() == 20) {
        comcr.callbatch(0, 0, 1);
        selectSQL = " user_id ";
        daoTable = "ptr_callbatch";
        whereStr = "WHERE batch_seqno   = ?  ";

        setString(1, comcr.hCallBatchSeqno);
        int recCnt = selectTable();
        hTempUser = getValue("user_id");
      }
      if (hTempUser.length() == 0) {
        hTempUser = comc.commGetUserID();
      }

      commonRtn();
      hModPgm = javaProgram;
      hDcurModPgm = hModPgm;
      hDcurModTime = hModTime;
      hDcurModUser = hModUser;
      hDcurModSeqno = hModSeqno;

      selectDbbCurpost();

      if (totCnt > 0) {
        String filename =
            String.format("%s/reports/%s_%s", comc.getECSHOME(), rptName, hSystemDateF);
        comc.writeReport(filename, lpar1);
      }

      // ==============================================
      // 固定要做的

      comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
      showLogMessage("I", "", comcr.hCallErrorDesc);
      if (comcr.hCallBatchSeqno.length() == 20)
        comcr.callbatch(1, 0, 1); // 1: 結束
      finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
    }
  }

  /***********************************************************************/
  void commonRtn() throws Exception {
    sqlCmd = "select business_date ";
    sqlCmd += " from ptr_businday ";
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
    }
    if (recordCnt > 0) {
      hBusinessDate = getValue("business_date");
    }

    sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date, ";
    sqlCmd += " to_char(sysdate,'yyyymmddhh24miss') as h_system_date_f ";
    sqlCmd += " from dual ";
    recordCnt = selectTable();
    if (recordCnt > 0) {
      hSystemDate = getValue("h_system_date");
      hSystemDateF = getValue("h_system_date_f");
    }
    hModSeqno = comcr.getModSeq();
    hModUser = comc.commGetUserID();
    hModTime = hSystemDate;
  }

  /***********************************************************************/
  void selectDbbCurpost() throws Exception {
    sqlCmd = "select ";
    sqlCmd += "reference_no,";
    sqlCmd += "bill_type,";
    sqlCmd += "txn_code,";
    sqlCmd += "card_no,";
    sqlCmd += "film_no,";
    sqlCmd += "purchase_date,";
    sqlCmd += "dest_amt,";
    sqlCmd += "dest_curr,";
    sqlCmd += "source_amt,";
    sqlCmd += "source_curr,";
    sqlCmd += "mcht_eng_name,";
    sqlCmd += "mcht_city,";
    sqlCmd += "mcht_country,";
    sqlCmd += "mcht_category,";
    sqlCmd += "mcht_zip,";
    sqlCmd += "mcht_state,";
    // sqlCmd += "usage_code,";
    // sqlCmd += "reason_code,";
    sqlCmd += "auth_code,";
    sqlCmd += "dbb_curpost.batch_no,";
    sqlCmd += "process_date,";
    sqlCmd += "dbb_curpost.mcht_no,";
    sqlCmd += "mcht_chi_name,";
    sqlCmd += "contract_no,";
    // sqlCmd += "goods_name,";
    // sqlCmd += "original_no,";
    // sqlCmd += "telephone_no,";
    sqlCmd += "term,";
    sqlCmd += "total_term,";
    // sqlCmd += "prod_name,";
    sqlCmd += "acct_eng_short_name,";
    sqlCmd += "acct_chi_short_name,";
    sqlCmd += "doubt_type,";
    sqlCmd += "acct_type,";
    // sqlCmd += "acct_key,";
    // sqlCmd += "acct_status,";
    // sqlCmd += "block_status,";
    // sqlCmd += "block_date,";
    // sqlCmd += "pay_by_stage_flag,";
    // sqlCmd += "autopay_acct_no,";
    sqlCmd += "curr_code,";
    // sqlCmd += "oppost_date,";
    sqlCmd += "promote_dept,";
    sqlCmd += "prod_no,";
    sqlCmd += "group_code,";
    sqlCmd += "bin_type,";
    sqlCmd += "p_seqno,";
    sqlCmd += "id_p_seqno,";
    sqlCmd += "dbb_curpost.this_close_date,";
    sqlCmd += "reference_no_original,";
    sqlCmd += "dbb_curpost.rowid as rowid ";
    sqlCmd += " from dbb_curpost , bil_postcntl ";
    sqlCmd += "where decode(format_chk_ok_flag,'','N',format_chk_ok_flag) in ('Y','y') ";
    sqlCmd += "  and decode(confirm_flag_p    ,'','N',confirm_flag_p)     in ('Y','y') ";
    sqlCmd += "  and decode(manual_upd_flag   ,'','N',manual_upd_flag)    != 'Y' ";
    sqlCmd += "  and decode(curr_post_flag    ,'','N',curr_post_flag)     != 'Y' ";
    sqlCmd += "  and doubt_type            = '' ";
    sqlCmd += "  and bil_postcntl.batch_no = dbb_curpost.batch_no ";
    sqlCmd += "order by dbb_curpost.batch_no ";
    openCursor();
    while (fetchTable()) {
      hDcurReferenceNo = getValue("reference_no");
      hDcurBillType = getValue("bill_type");
      hDcurTxnCode = getValue("txn_code");
      hDcurCardNo = getValue("card_no");
      hDcurFilmNo = getValue("film_no");
      hDcurPurchaseDate = getValue("purchase_date");
      hDcurDestAmt = getValueDouble("dest_amt");
      hDcurDestCurr = getValue("dest_curr");
      hDcurSourceAmt = getValueDouble("source_amt");
      hDcurSourceCurr = getValue("source_curr");
      hDcurMchtEngName = getValue("mcht_eng_name");
      hDcurMchtCity = getValue("mcht_city");
      hDcurMchtCountry = getValue("mcht_country");
      hDcurMchtCategory = getValue("mcht_category");
      hDcurMchtZip = getValue("mcht_zip");
      hDcurMchtState = getValue("mcht_state");
      // h_dcur_usage_code = getValue("usage_code");
      // h_dcur_reason_code = getValue("reason_code");
      hDcurAuthCode = getValue("auth_code");
      hDcurBatchNo = getValue("dbb_curpost.batch_no");
      hDcurProcessDate = getValue("process_date");
      hDcurMchtNo = getValue("mcht_no");
      hDcurMchtChiName = getValue("mcht_chi_name");
      hDcurContractNo = getValue("contract_no");
      // h_dcur_goods_name = getValue("goods_name");
      // h_dcur_original_no = getValue("original_no");
      // h_dcur_telephone_no = getValue("telephone_no");
      hDcurTerm = getValueInt("term");
      hDcurTotalTerm = getValueInt("total_term");
      // h_dcur_prod_name = getValue("prod_name");
      hDcurAcctEngShortName = getValue("acct_eng_short_name");
      hDcurAcctChiShortName = getValue("acct_chi_short_name");
      hDcurDoubtType = getValue("doubt_type");
      hDcurAcctType = getValue("acct_type");
      // h_dcur_acct_key = getValue("acct_key");
      // h_dcur_acct_status = getValue("acct_status");
      // h_dcur_block_status = getValue("block_status");
      // h_dcur_block_date = getValue("block_date");
      // h_dcur_pay_by_stage_flag = getValue("pay_by_stage_flag");
      // h_dcur_autopay_acct_no = getValue("autopay_acct_no");
      hDcurCurrCode = getValue("curr_code");
      // h_dcur_oppost_date = getValue("oppost_date");
      hDcurPromoteDept = getValue("promote_dept");
      hDcurProdNo = getValue("prod_no");
      hDcurGroupCode = getValue("group_code");
      hDcurBinType = getValue("bin_type");
      hDcurPSeqno = getValue("p_seqno");
      hDcurIdPSeqno = getValue("id_p_seqno");
      hDcurThisCloseDate = getValue("this_close_date");
      hDcurReferenceNoOriginal = getValue("reference_no_original");
      hDcurRowid = getValue("rowid");

      totCnt++;
      if (totCnt % 5000 == 0 || totCnt == 1) {
        commitDataBase();
        showLogMessage("D", "", "Process record=[" + totCnt + "]");
      }
      chkCurpost();
      if (hDcurDoubtType.length() != 0) {
        if (indexCnt == 0)
          printHeader();
        printDetail();
        if (indexCnt >= 25) {
          printFooter();
          indexCnt = 0;
        }
      }

    }
    closeCursor();
    if (indexCnt != 0)
      printFooter();
  }

  /***********************************************************************/
  void chkCurpost() throws Exception {

    hDcurDoubtType = "";

    hTempdig = (long) hDcurDestAmt;

    if (hTempdig == 0) {
      hDcurDoubtType = "0001";
    }

    sqlCmd = "select to_date(?,'yyyymmdd') h_tempstr";
    sqlCmd += " from dual ";
    setString(1, hDcurPurchaseDate);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hTempstr = getValue("h_tempstr");
    }

    if (recordCnt == 0) {
      hDcurDoubtType = "0002";
    }
    if (comcr.str2long(hBusinessDate) < comcr.str2long(hDcurPurchaseDate)) {
      hDcurDoubtType = "0002";
    }
    
    //20220601: 若是目的地幣別為空值, 判斷特店國別碼為"TW"則將目的地幣別改為'901', 
    //                 或是原始幣別是'901', 也改成'901', 
    //                 非上述原因則報錯, 寫入格式錯誤報表
    if (hDcurDestCurr.length() == 0) {
    	if (hDcurMchtCountry.length()>= 2 && "TW".equals(hDcurMchtCountry.toUpperCase(Locale.TAIWAN).substring(0, 2))) {
    		hDcurDestCurr = "901";
    	} else if ("901".equals(hDcurSourceCurr)) {
    		hDcurDestCurr = "901";
    	} else {
    		 hDcurDoubtType = "0004"; 
    	}
    }

    if (hDcurDoubtType.length() != 0) {
      daoTable = "dbb_curpost";
      updateSQL = "doubt_type           = ?,";
      updateSQL += " mod_time           = sysdate ,";
      updateSQL += " manual_upd_flag    = 'N',";
      updateSQL += " format_chk_ok_flag = 'Y'";
      whereStr = "where rowid   = ? ";
      setString(1, hDcurDoubtType);
      // setString(2, h_business_date);
      setRowId(2, hDcurRowid);
      updateTable();
      if (notFound.equals("Y")) {
        String stderr = "update_dbb_curpost not found!";
        comcr.errRtn(stderr, "", comcr.hCallBatchSeqno);
      }
    } else {
      daoTable = "dbb_curpost";
      updateSQL = " format_chk_ok_flag = 'N',";
      updateSQL += " dest_curr = ? , ";
      updateSQL += " mod_time           = sysdate, ";
      updateSQL += " manual_upd_flag    = 'N'";
      whereStr = "where rowid         = ? ";
      setString(1, hDcurDestCurr);
      setRowId(2, hDcurRowid);
      updateTable();
      if (notFound.equals("Y")) {
        String stderr = "update_dbb_curpost not found!";
        comcr.errRtn(stderr, "", comcr.hCallBatchSeqno);
      }
    }
  }

  /***********************************************************************/
  void printHeader() {
    String reportH1 =
        "卡               號 參 考 號碼 微    縮    影   號  碼 消費日期 代碼 消費金額(台幣) 消費金額(原幣) 幣別 入帳日期 批          號 原因";
    String reportL1 =
        "=================== ========== ======================= ======== ==== ============== ============== ==== ======== ============== ====";
    pageCnt++;
    buf = "";
    buf = comcr.insertStr(buf, "報表名稱: " + rptName, 1);
    buf = comcr.insertStrCenter(buf, "信用卡  格式查核錯誤明細表", 132);
    buf = comcr.insertStr(buf, "頁    次:", 110);
    szTmp = String.format("%4d", pageCnt);
    buf = comcr.insertStr(buf, szTmp, 122);
    lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

    buf = "";
    buf = comcr.insertStr(buf, "印表日期:", 1);
    buf = comcr.insertStr(buf, chinDate, 11);
    lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

    buf = "";
    buf = comcr.insertStr(buf, reportH1, 1);
    lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

    buf = "";
    buf = comcr.insertStr(buf, reportL1, 1);
    lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));
  }

  /***********************************************************************/
  void printDetail() {
    lineCnt++;
    indexCnt++;

    buf = "";
    buf = comcr.insertStr(buf, hDcurCardNo, 1);
    buf = comcr.insertStr(buf, hDcurReferenceNo, 21);
    buf = comcr.insertStr(buf, hDcurFilmNo, 32);

    if (hDcurPurchaseDate.substring(0, 1).equals("2")) {
      szTmp = String.format("%8d", comcr.str2long(hDcurPurchaseDate) - 19110000);
      buf = comcr.insertStr(buf, szTmp, 56);
    } else
      buf = comcr.insertStr(buf, hDcurPurchaseDate, 56);

    buf = comcr.insertStr(buf, hDcurTxnCode, 65);
    buf = comcr.insertStr(buf, String.valueOf(hDcurDestAmt), 70);
    buf = comcr.insertStr(buf, String.valueOf(hDcurSourceAmt), 85);
    buf = comcr.insertStr(buf, hDcurDestCurr, 100);
    szTmp = String.format("%8d", comcr.str2long(hDcurThisCloseDate) - 19110000);
    buf = comcr.insertStr(buf, szTmp, 105);
    buf = comcr.insertStr(buf, hDcurBatchNo, 114);
    buf = comcr.insertStr(buf, hDcurDoubtType, 129);
    lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

    if ((!hDcurTxnCode.equals("06")) && (!hDcurTxnCode.equals("17")) && (!hDcurTxnCode.equals("25"))
        && (!hDcurTxnCode.equals("27")) && (!hDcurTxnCode.equals("28"))
        && (!hDcurTxnCode.equals("29"))) {
      pagePositiveCnt++;
      totalPositiveCnt++;
      pagePDestAmt = pagePDestAmt + hDcurDestAmt;
      pagePSourceAmt = pagePSourceAmt + hDcurSourceAmt;
      totalPDestAmt = totalPDestAmt + hDcurDestAmt;
      totalPSourceAmt = totalPSourceAmt + hDcurSourceAmt;
    } else {
      pageNegativeCnt++;
      totalNegativeCnt++;
      pageNDestAmt = pageNDestAmt + hDcurDestAmt;
      pageNSourceAmt = pageNSourceAmt + hDcurSourceAmt;
      totalNDestAmt = totalNDestAmt + hDcurDestAmt;
      totalNSourceAmt = totalNSourceAmt + hDcurSourceAmt;
    }

  }

  /***********************************************************************/
  void printFooter() {

    buf = "";
    buf = comcr.insertStr(buf, "備註欄 1:金額錯誤 2:日期 3:微縮影號 4:幣別 ", 1);
    lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

    pagePDestAmt = 0;
    pageNDestAmt = 0;
    pageNSourceAmt = 0;
    pageNegativeCnt = 0;
    pagePositiveCnt = 0;
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    DbbA003 proc = new DbbA003();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
}
