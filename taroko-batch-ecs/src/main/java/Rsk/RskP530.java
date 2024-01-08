/******************************************************************************
* VD卡授權交易超過 n 天未請款處理程式 V.2018-0508-JH
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
 * 2017-1228-00:  V1.00.00    JH      initial                                 *
 * 109/07/04      V1.00.01    Zuwei     coding standard, rename field method & format  *
*  109/07/23      V1.00.02    shiyuqi     coding standard, rename field method & format*     
*  109/09/22      V1.00.03    JeffKung  select ref_no from cca_auth_txlog        *        
*  109/11/18      V1.00.04    Alex      稅改抓Mcc_code 9399                        *
*  109-11-23      V1.00.05   tanwei  updated for project coding standard         *
*  111-03-10      V1.00.06    Alex     mcc_code add 9311                         *
*  111-04-15      V1.00.07    Alex     vd_lock_nt_amt > 0 才撈出                                                *
*  111-05-06       V1.00.08    Alex     排除VD悠遊自動加值交易							*
*  111-10-10      V1.00.09    Alex     增加寫入dba_acaj調整原因欄位 , 01->到期未請款自動解圈 *
******************************************************************************/
package Rsk;

import com.CommFunction;
import com.CommRoutine;
import com.SqlParm;
import com.BaseBatch;

public class RskP530 extends BaseBatch {
  private final String progname = "VD卡授權交易超過 n 天未請款處理程式 V.2022-1010  111/10/10 V1.00.09 ";
  CommFunction comm = new CommFunction();
  CommRoutine comr = null;
  private int commit = 1; // commit

  private String hDcasTransactionCode = "";
  private String hDcasRealCardNo = "";
  // private String hDcas_film_no = "";
  private String hDcasPurchaseDate = "";
  private double hDcasDestAmt = 0;
  private String hDcasPSeqno = "";
  private String hDcasAcctType = "";
  // private String hDcas_acct_key = "";
  private String hDcasDebitAcctNo = "";
  private String hDcasReferenceNo = "";
  // private String hDcas_rsk_type = "";
  private String hDcasMchtNo = "";
  private String hDcasRowid = "";
  private String hTxSeq = "";
  private int shortTempDays = 0, longTempDays = 0, taxTempDays = 0;
  // private int il_cnt = 0;
  private String country = "";
  private String changeDate = "";
  // =======================================================================
  private SqlParm tAcaj = new com.SqlParm();

  public static void main(String[] args) {
    RskP530 proc = new RskP530();
    proc.debug = true;
    proc.mainProcess(args);
    proc.systemExit(0);
  }

  @Override
  protected void dataProcess(String[] args) throws Exception {
    dspProgram(progname);
    // ddd("mod_pgm=" + javaProgram);

    if (args.length != 0) {
      printf("Usage : RskP530");
      errExit(1);
    }

    if (!connectDataBase())
      errExit(1);
    // comr = new CommRoutine(getDBconnect(), getDBalias());

    selectPtrBusinday();
    setModXxx();
    
    //--判斷假日和有扣款資料未回覆時不執行 
    //--(因這2條件符合其一時 DbaA001 不啟動接 RskP530 產生的 acaj 資料 若仍執行RskP530造成重覆資料一直寫入acaj)
    if(checkHoliday()) {
    	showLogMessage("I", "", "非營業日不執行 !");
    	okExit(0);
    }
    
    if (selectDbaDeductCtl() != 0) {    	
    	showLogMessage("I","","扣款資料尚未送回, 不產生批次解圈資料");
    	okExit(0);
    }
    
    
    selectPtrSysparm();
    selectDbbCcasDtl();

    sqlCommit(1);
  }
  
  //--判斷假日
  boolean checkHoliday() throws Exception {	  
	  sqlCmd =   "select holiday ";
	  sqlCmd += " from ptr_holiday  ";
	  sqlCmd += "where holiday = ? ";
	  setString(1, hBusiDate);
	  int recordCnt = selectTable();
	  if (recordCnt > 0) {		  
		  return true;
	  }
	  
	  return false;
  }
  
  //--判斷扣款資料
  
  int selectDbaDeductCtl() throws Exception {	 
	  sqlCmd = "select deduct_date ";
	  sqlCmd += " from dba_deduct_ctl  ";
	  sqlCmd += "where receive_date =''  ";
	  sqlCmd += "and proc_type = 'A'  ";
	  sqlCmd += " fetch first 1 rows only ";
	  int recordCnt = selectTable();
	  if (recordCnt > 0) {	      
	  } else
	      return (0);
	  return (1);
  }
  
  // =****************************************************************************
  void selectPtrSysparm() throws Exception {
    // 請款日超過交易日 n 天
    country = "";
    changeDate = "";
    shortTempDays = 0;
    longTempDays = 0;
    taxTempDays = 0;

    daoTable = "";
    sqlCmd = " SELECT wf_value6 as short_temp_days ,"
        + " wf_value7 as long_temp_days ,"
        + " wf_value8 as tax_temp_days ,"
        + " wf_value2 as country ,"
        + " wf_value as change_date  "
        + " from ptr_sys_parm "
        + " where wf_parm='SYSPARM' and wf_key ='RSK_BILL_OVER_DAYS' ";
    sqlSelect();

    if (sqlNrow <= 0) {
      printf("select ptr_sys_parm error; kk=RSK_BILL_OVER_DAYS");
      shortTempDays = 14;
      longTempDays = 29;
      taxTempDays = 45;
      return;
    }
    // find
    shortTempDays = colInt("short_temp_days");
    longTempDays = colInt("long_temp_days");
    taxTempDays = colInt("tax_temp_days");
    country = colSs("country");
    changeDate = colSs("change_date");
  }

  // =********************************************************************************
  void selectDbbCcasDtl() throws Exception {
    daoTable = "select_cca_auth_txlog";
    sqlCmd = "select "
        + " ref_no as reference_no , "
        + " acno_p_seqno , "
        + " acct_type , "
        + " uf_acno_key2(acno_p_seqno,'Y') as acct_key,"
        // + " acct_no ," //--debit_acct_no
        + " card_no ,"
        // + " txn_code ," //--先不放
        + " tx_date ," // --purchase_date
        + " vd_lock_nt_amt ," // -- dest_amt
        // + " rsk_type ," //--風管疑義碼
        + " mcht_no ,"
        + " consume_country ,"
        + " hex(rowid) as rowid , "
        + " tx_seq "
        + " from cca_auth_txlog "
        + " where 1=1 "
        + " and unlock_flag in ('','N') "
        + " and mtch_flag <> 'Y' "
        + " and vdcard_flag = 'D' "
        + " and trans_code <> 'VA' "	//--VD悠遊卡自動加值交易
        // + " and rsk_type in ('N') "
        + " and length(auth_no) = 6 " // --auth_code
        + " and vd_lock_nt_amt > 0 "
        + " and case when mcc_code in ('9399','9311') "
        + " then uf_date_add(tx_date,0,0,cast(? as int)) "
        + " when consume_country <> ? and tx_date > ?" // country,change_day
        + " then uf_date_add(tx_date,0,0,cast(? as int))" // short-days
        + " else uf_date_add(tx_date,0,0,cast(? as int)) " // long-days
        + " end < ?" // busi-date
    ;

    ppp(1, taxTempDays);
    ppp(2, country);
    ppp(3, changeDate);
    ppp(4, shortTempDays);
    ppp(5, longTempDays);
    ppp(6, hBusiDate);

    // ddd(sqlCmd,get_sqlParm());
    this.fetchExtend = "aa.";
    this.openCursor();

    while (fetchTable()) {
      totalCnt++;
//      ddd(">>" + totalCnt + ": card_no[%s]", colSs("aa.card_no"));
      initHData();

      hDcasReferenceNo = colSs("aa.reference_no");
      hDcasPSeqno = colSs("aa.acno_p_seqno");
      hDcasAcctType = colSs("aa.acct_type");
      // hDcas_acct_key = col_ss("aa.acct_key");
      hDcasRealCardNo = colSs("aa.card_no");
      hDcasDebitAcctNo = getBankAcctNo(); // --debit_acct_no
      // hDcas_transaction_code = col_ss("aa.txn_code");
      // hDcas_film_no = col_ss("aa.film_no");
      hDcasPurchaseDate = colSs("aa.tx_date"); // --purchase_date
      hDcasDestAmt = colInt("aa.vd_lock_nt_amt"); // -- dest_amt
      // hDcas_rsk_type = col_ss("aa.rsk_type");
      hDcasMchtNo = colSs("aa.mcht_no");
      hDcasRowid = colSs("aa.rowid");
      hTxSeq = colSs("aa.tx_seq");
      
      // update_dbb_ccas_dtl();
      if (hDcasDestAmt > 0) {
        insertDbaAcaj();
      }

      // il_cnt++;
      sqlCommit(commit);
    }

    closeCursor();
  }

  String getBankAcctNo() throws Exception {

    String sql1 = "";
    sql1 = " select acct_no from dbc_card where card_no = ? ";

    setString(1, hDcasRealCardNo);
    sqlSelect(sql1);

    if (sqlNrow > 0) {
      return colSs("acct_no");  
    }

    return "";
  }

  // ********************************************************************************
  void initHData() {
    hDcasReferenceNo = "";
    hDcasPSeqno = "";
    hDcasAcctType = "";
    // hDcas_acct_key = "";
    hDcasDebitAcctNo = "";
    hDcasRealCardNo = "";
    hDcasTransactionCode = "";
    // hDcas_film_no = "";
    hDcasPurchaseDate = "";
    hDcasDestAmt = 0;
    hDcasMchtNo = "";
    // hDcas_rsk_type = "";
    hDcasRowid = "";
    hTxSeq = "";
  }

  // ********************************************************************************
  void updateDbbCcasDtl() throws Exception {
    daoTable = "update_dbb_ccas_dtl-U1";
    int tid = getTableId();
    if (tid <= 0) {
      sqlCmd = "update dbb_ccas_dtl set"
          + " rsk_type = 'C',"
          + " lift_add_date =? ,"
          + " lift_add_id = ?,"
          + " lift_conf_date = ?,"
          + " lift_conf_id = ?,"
          + commSqlStr.setModXxx(hModUser, hModPgm)
          + " where rowid = ? ";
      // sql1 =sqlCmd;
      tid = ppStmtCrt();
    }
    Object[] pp =
        new Object[] {hBusiDate, hModUser, hBusiDate, hModUser, commSqlStr.ss2rowid(hDcasRowid)};

    sqlExec(tid, pp);
    if (sqlNrow <= 0) {
      printf("update dbb_ccas_dtl error");
      errExit(1);
    }
  }

  // ********************************************************************************
  void insertDbaAcaj() throws Exception {
    if (tAcaj.pfidx <= 0) {
      tAcaj.sqlFrom = "insert into dba_acaj ("
          + " crt_date ,"
          + " crt_time ,"
          + " p_seqno ,"
          + " acct_type ,"
          + " acct_no ,"
          + " adjust_type ,"
          + " reference_no ,"
          + " orginal_amt ,"
          + " func_code ,"
          + " card_no ,"
          + " purchase_date ,"
          + " adj_reason_code ," //--2022/10/09 增加調整原因
          + " proc_flag ,"
          + " txn_code ,"
          + " tx_seq ,"
          + " mcht_no ,"
          + " apr_flag ,"
          + " apr_date ,"
          + " apr_user ,"
          + " mod_user ,"
          + " mod_time ,"
          + " mod_pgm ,"
          + " mod_seqno"
          + " ) values ("
          + commSqlStr.sysYYmd
          + ","
          + commSqlStr.sysTime
          + ","
          + tAcaj.pmkk(0, ":p_seqno ,")
          + tAcaj.pmkk(":acct_type ,")
          + tAcaj.pmkk(":acct_no ,")
          + " 'RE10' ," // adjust_type 解圈不扣款
          + tAcaj.pmkk(":reference_no ,")
          + tAcaj.pmkk(":orginal_amt ,")
          + " 'U' ,"
          + tAcaj.pmkk(":card_no ,")
          + tAcaj.pmkk(":purchase_date ,")
          + " '01' ," //--調整原因固定放 01 到期未請款自動解圈
          + " 'N' ,"
          + " '' ," // --txn_code 先放空值
          // + t_acaj.pmkk(":txn_code ,")
          + tAcaj.pmkk(":tx_seq ,")
          + tAcaj.pmkk(":mcht_no ,")
          + " 'Y' ,"
          + commSqlStr.sysYYmd
          + ","
          + tAcaj.pmkk(":apr_user ,")
          + tAcaj.pmkk(":mod_user ,")
          + " sysdate ,"
          + tAcaj.pmkk(":mod_pgm ,")
          + " 1 "
          + " )";
      tAcaj.pfidx = ppStmtCrt("insert_dba_acaj-A1", tAcaj.sqlFrom);
    }
    tAcaj.ppp("p_seqno", hDcasPSeqno);
    tAcaj.ppp("acct_type", hDcasAcctType);
    tAcaj.ppp("acct_no", hDcasDebitAcctNo);
    tAcaj.ppp("reference_no", hDcasReferenceNo);
    tAcaj.ppp("orginal_amt", hDcasDestAmt);
    tAcaj.ppp("card_no", hDcasRealCardNo);
    tAcaj.ppp("purchase_date", hDcasPurchaseDate);
    // t_acaj.ppp("txn_code", hDcas_transaction_code);
    tAcaj.ppp("tx_seq", hTxSeq);
    tAcaj.ppp("mcht_no", hDcasMchtNo);
    tAcaj.ppp("apr_user", hModUser);
    tAcaj.ppp("mod_user", hModUser);
    tAcaj.ppp("mod_pgm", hModPgm);
    Object[] pps = tAcaj.getConvParm();
    // ddd(t_acaj.sql_from, pps);
    sqlExec(tAcaj.pfidx, pps);
    if (sqlNrow <= 0) {
      errmsg("insert dba_acaj error");
      this.errExit(0);
    }
  }

}
