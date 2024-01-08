/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-06-01  V1.00.00  yanghan     Initial
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package dbam01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import java.math.BigDecimal;

public class Dbap0070 extends BaseEdit {
  Dbap0070Func func;
  String kkReferenceNo = "";
  String kkTable = "";
  String kkRowid = "";

  String mAcctKey = "";
  String mChiName = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "U1")) {
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
        strAction = "U";
        procFunc();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("pho_update_disable", "disabled style='background-color: lightgray;'");
    wp.colSet("pho_delete_disable", "disabled style='background-color: lightgray;'");
  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
      } else {
        wp.optionKey = wp.itemStr("exAcctType");
        dddwList("DbpAcctTypeList", "dbp_acct_type", "acct_type", "acct_type||' ['||chin_name||']'",
            "where 1=1 order by acct_type");
      }
    } catch (Exception ex) {
    }
  }

  private boolean getWhereStr() throws Exception {
    if (empty(wp.itemStr("exAcctKey"))) {
      alertErr2("請輸入 帳戶帳號");
      return false;
    }
    String lsDate1 = wp.itemStr("exDateS");
    String lsDate2 = wp.itemStr("exDateE");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[消費日期-起迄]  輸入錯誤");
      return false;
    }

    // todo f_auth_query_vd
    // 2. 執行查詢權限檢查，若未通過，則return -1。
    // if f_auth_query_vd(classname(),ss)=false then return -1
    // 邏輯請參考【col_共用說明/f_auth_query_vd】。

    String acctkey = fillZeroAcctKey(wp.itemStr("exAcctKey"));

    wp.whereStr = "where 1=1 " + " and dba_debt.p_seqno = dba_acno.p_seqno ";
    wp.whereStr += " and dba_acno.acct_type = :acct_type ";
    wp.whereStr += " and dba_acno.acct_key like :acct_key ";
    setString("acct_type", wp.itemStr("exAcctType"));
    setString("acct_key", acctkey + "%");

    if (empty(wp.itemStr("exDateS")) == false) {
      wp.whereStr += " and dba_debt.purchase_date >= :pur_dates ";
      setString("pur_dates", wp.itemStr("exDateS"));
    }
    if (empty(wp.itemStr("exDateE")) == false) {
      wp.whereStr += " and dba_debt.purchase_date <= :pur_datee ";
      setString("pur_datee", wp.itemStr("exDateE"));
    }

    StringBuffer sb = new StringBuffer();
    if (eqIgno(wp.itemStr("exAcitem01"), "Y")) {
      sb.append(",'AF','LF','CF','PF','SF','CC'");
    }
    if (eqIgno(wp.itemStr("exAcitem04"), "Y")) {
      sb.append(",'BL','CB','CA','IT','AO','DB'");
    }
    if (sb.length() > 0) {
      wp.whereStr += " and dba_debt.acct_code in (" + sb.toString().substring(1) + ") ";
    }

    wp.whereOrder = " order by dba_acno.acct_key, dba_debt.purchase_date ";
    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "hex(dba_debt.rowid) as rowid, " + "dba_acno.acct_type, " + "dba_acno.acct_key, "
        + "dba_debt.id_p_seqno, " + "dba_debt.item_post_date, " + "dba_debt.purchase_date, "
        + "dba_debt.reference_no, " + "dba_debt.card_no, " + "dba_debt.beg_bal, "
        + "dba_debt.end_bal, " + "dba_debt.d_avail_bal, " + "dba_debt.acct_code, "
        + "dba_debt.txn_code, " + "dba_debt.p_seqno, " + "dba_debt.acct_month, "
        + "dba_debt.bill_type, " + "'debt' db_table, " + "nvl(dbc_card.acct_no,'') as acct_no, "
        + "nvl(ptr_actcode.chi_long_name,'') as wk_acct_code ";

    wp.daoTable = "dba_debt " + "left join dbc_card on dba_debt.card_no = dbc_card.card_no "
        + "left join ptr_actcode on dba_debt.acct_code = ptr_actcode.acct_code " + ",dba_acno ";
    wp.whereStr+="and dba_debt.end_bal>0";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    ofcQueryafter();
    wp.setPageValue();
  }

  // ofc_queryafter 說明:
  // 1. 迴圈解析mainData[]，逐筆檢查、重整資料。
  // a. 讀取 DBA_ACAJ，若存在資料，則帶入DBA_ACAJ之資料。
  // b. 取得金融帳號 。
  // 2. 若ex_ackey為11碼，取得中文姓名。
  void ofcQueryafter() throws Exception {
    String lsSql = "", lsCname = "";

    // 若ex_ackey為11碼，取得中文姓名
    if (fillZeroAcctKey(wp.itemStr("exAcctKey")).length() == 11) {
      lsSql = "select chi_name from dbc_idno where id_p_seqno = :id_p_seqno ";
      setString("id_p_seqno", wp.colStr("id_p_seqno"));
      sqlSelect(lsSql);
      if (sqlRowNum >= 0) {
        lsCname = sqlStr("chi_name");
      }
      wp.colSet("exCname", lsCname);
    }

    // 讀取 dba_deduct_txn，若存在資料，則帶入dba_deduct_txn之資料。
//    for (int ii = 0; ii < wp.selectCnt; ii++) {
//      lsSql =
//          "select  dba_deduct_txn.acct_code, d_available_bal,"//dba_deduct_txn.beg_bal, dba_deduct_txn.end_bal,
//          + "dba_deduct_txn.purchase_date, "
//              + "hex(dba_deduct_txn.rowid) as rowid, 'ddtxn' as db_table, ptr_actcode.chi_long_name from dba_deduct_txn "
//              + "left join ptr_actcode on dba_deduct_txn.acct_code = ptr_actcode.acct_code "
//              + "where reference_no = :reference_no " // and decode(apr_flag,'','N',apr_flag) <> 'Y'
//              + "order by dba_deduct_txn.crt_date desc" + sqlRownum(1);
//      setString("reference_no", wp.colStr(ii, "reference_no"));
//      sqlSelect(lsSql);
//      if (sqlRowNum > 0) {
//        // System.out.println("UUUUU>> sql_nrow"+sql_nrow+", "+wp.col_ss(ii,"reference_no"));
////        wp.colSet(ii, "beg_bal", sqlStr("beg_bal"));
////        wp.colSet(ii, "end_bal", sqlStr("end_bal"));
//        wp.colSet(ii, "d_avail_bal", sqlStr("d_available_bal"));
//        wp.colSet(ii, "acct_code", sqlStr("acct_code"));
//        wp.colSet(ii, "purchase_date", sqlStr("purchase_date"));
//        wp.colSet(ii, "rowid", sqlStr("rowid"));
////        wp.colSet(ii, "db_table", sqlStr("db_table"));
//        wp.colSet(ii, "wk_acct_code", sqlStr("chi_long_name"));
//      }
//
//      // 取得金融帳號 fr dbc_card << 加到Main query一起做掉
//    }
  }

  @Override
  public void querySelect() throws Exception {
    kkReferenceNo = wp.itemStr("data_k1");
    kkTable = wp.itemStr("data_k2");
    kkRowid = wp.itemStr("data_k3");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(kkReferenceNo))
      kkReferenceNo = wp.itemStr("reference_no");    
    if (empty(kkRowid))
      kkRowid = wp.itemStr("rowid");
    
    dataReadDebt();
//    if (eqIgno(kkTable, "debt")) {
//      dataReadDebt();
//    } else if (eqIgno(kkTable, "ddtxn")) {
//      dataReadDdtxn();
//    }

    detlWkdata();

  }

  void dataReadDebt() throws Exception { 
    wp.selectSQL = " '' as rowid, 'debt' db_table, " + " p_seqno, " + " acct_type, mcht_no,"//merchant_no,
        +"tx_seq, id_p_seqno, bank_actno,d_avail_bal, debt_status"
        +", stmt_cycle, org_reserve_amt, reserve_amt, trans_col_date, trans_bad_date,"
        + " reference_no, " + " item_post_date as post_date, " + " item_post_date, "
        + " beg_bal as hi_beg_bal, " 
        + " end_bal as orginal_amt , 0 as deduct_amt,end_bal, " 
        + " end_bal as bef_amt, " + " end_bal as aft_amt, " + " d_avail_bal as bef_d_amt, "
        + " d_avail_bal as aft_d_amt, " + " acct_code, " + " 'U' as func_code, " + " card_no, "
        + " purchase_date, " + " acct_no, " + " 'N' as ex_dcount, " + " txn_code, "
        + " 'N' as apr_flag, " + " bill_type, " + " mod_seqno, " + " '' as adj_comment, "
        + " '' as c_debt_key, " + " '14817000' as debit_item ";
    wp.daoTable = "dba_debt";
    wp.whereStr = "where reference_no = :reference_no ";
    setString("reference_no", kkReferenceNo);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, TABLE='dba_debt', reference_no= " + kkReferenceNo);
      return;
    }
    wp.colSet("ex_bef_amt", wp.colStr("orginal_amt"));
    wp.colSet("deduct_amt", wp.colStr("orginal_amt"));
    wp.colSet("ex_bef_d_amt", wp.colStr("bef_d_amt"));
    wp.colSet("ex_aft_amt", "0");
//    wp.colSet("ex_aft_amt", wp.colStr("aft_amt"));
    wp.colSet("ex_aft_d_amt", wp.colStr("aft_d_amt"));
    wp.colSet("pho_delete_disable", "disabled style='background-color: lightgray;'");

    // Set Table Order
    String val5 = wp.colStr("acct_code");
    if (val5.equals("AF") || val5.equals("LF") || val5.equals("CF") || val5.equals("PF")
        || val5.equals("RI") || val5.equals("PN") || val5.equals("AI") || val5.equals("SF")
        || val5.equals("CI") || val5.equals("CC")) {
      // wp.col_set("pho_disable", "disabled ");
    }

  }

  void dataReadDdtxn() throws Exception {
    
    wp.selectSQL = " hex(rowid) as rowid, 'ddtxn' db_table, " + " p_seqno, " + " acct_type, item_post_date,id_p_seqno,"
        + " reference_no, " + " beg_bal as bef_amt,deduct_seq, " 
        + " end_bal as aft_amt, end_bal," + " d_available_bal, "+ " 0 as dr_amt,  id_no,  id_no_code, stmt_cycle, "//
        +" debt_status, trans_col_date, trans_bad_date,"
             + " acct_code, " + " card_no, " + " purchase_date, " + " acct_no, " +
        " 'Y' as ex_dcount, " + " apr_flag, " + " mod_seqno ";

    wp.daoTable = "dba_deduct_txn";
    wp.whereStr = "where reference_no = :reference_no ";
    setString("reference_no", kkReferenceNo);
    // wp.whereStr = "where rowid = :rowid " ;
    // setRowid("rowid", kk_rowid);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, TABLE='dba_deduct_txn', reference_no= " + kkReferenceNo);
      return;
    }        
    wp.colSet("ex_bef_amt", wp.colStr("bef_amt"));
    wp.colSet("ex_aft_amt", wp.colStr("aft_amt"));
    wp.colSet("ex_aft_d_amt", wp.colStr("d_available_bal"));
    wp.colSet("ex_bef_d_amt", wp.colStr("d_available_bal"));
  }

  void detlWkdata() throws Exception {
    String pSeqno = "";

    pSeqno = wp.colStr("p_seqno");
    getAcnobyPseqno(pSeqno);
    wp.colSet("acct_key", mAcctKey);
    wp.colSet("ex_cname", mChiName);
    pSeqno = wp.colStr("acct_code");
    wp.colSet("tt_acct_code", wfGetAcctName(pSeqno));
    //--取效期
    String sql1 = "";
    sql1 = "select new_end_date , card_ref_num from dbc_card where card_no = ?";
    sqlSelect(sql1,new Object[] {wp.colStr("card_no")});
    if(sqlRowNum >0) {
    	wp.colSet("new_end_date",sqlStr("new_end_date"));
    	wp.colSet("card_ref_num",sqlStr("card_ref_num"));
    }
    //--取送電文欄位
    String sql2 = "select trace_no , tx_date , tx_time , ref_no , v_card_no , auth_no , card_acct_idx , acno_p_seqno from cca_auth_txlog ";
    sql2 += "where tx_seq = ? "+commSqlStr.rownum(1);
    sqlSelect(sql2,new Object[] {wp.colStr("tx_seq")});
    if(sqlRowNum>0) {
    	wp.colSet("trace_no", sqlStr("trace_no"));
    	wp.colSet("tx_date", sqlStr("tx_date"));
    	wp.colSet("tx_time", sqlStr("tx_time"));
    	wp.colSet("ref_no", sqlStr("ref_no"));
    	wp.colSet("v_card_no", sqlStr("v_card_no"));
    	wp.colSet("auth_no", sqlStr("auth_no"));
    	wp.colSet("card_acct_idx", sqlStr("card_acct_idx"));
    	wp.colSet("acno_p_seqno", sqlStr("acno_p_seqno"));
    	
    	//--取電文紀錄
        String sql3 = "select tx_date , tx_time , ims_seq_no , trans_amt , ims_resp_code , ims_reversal_data from cca_ims_log where card_no = ? and auth_no = ? ";
        sql3 += " and locate('VDAD',ims_reversal_data) >0 and locate('VDIQ',ims_reversal_data) <=0";
        sql3 += " order by tx_date Desc , tx_time Desc " + commSqlStr.rownum(1);
        sqlSelect(sql3,new Object[] {wp.colStr("card_no"),wp.colStr("auth_no")});
        if(sqlRowNum > 0) {
        	wp.colSet("last_ims_seq_no", sqlStr("ims_seq_no"));
        	wp.colSet("last_trans_amt", sqlStr("trans_amt"));
        	wp.colSet("last_ims_resp_code", sqlStr("ims_resp_code"));
        	wp.colSet("last_tx_date", sqlStr("tx_date"));
        	wp.colSet("last_tx_time", sqlStr("tx_time"));
        	if(wp.colEq("last_ims_resp_code", "0000")) {
        		wp.colSet("tt_ims_resp", "成功");
        	} else if(wp.colEmpty("last_ims_resp_code")) {
        		wp.colSet("tt_ims_resp", "主機沒有回應");
        	} else	{
        		wp.colSet("tt_ims_resp", "失敗");
        	}
        	
        	String lastData = "";
        	lastData = sqlStr("ims_reversal_data");
        	if(lastData.length() > 116) {
        		wp.colSet("last_ims_amt", lastData.substring(96, 116));
        	}	else	{
        		wp.colSet("last_ims_amt", "");
        	}
        	
        }
    	
    }
    
    
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Dbap0070Func(wp);
    if (ofValidation() < 0)
      return;

    if (ofcUpdatebefore() < 0)
      return;

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
      return ;
    }    
    
    if(func.isReponse==false) {
    	alertErr2("線上扣款失敗");
    	return ;
    }
    
    if (strAction.equals("U")&& rc == 1) {
//      kkTable = "ddtxn";
      dataRead();
    }
  }
   
  int ofValidation() throws Exception {
    double ldcBefAmt, ldcDrAmt, ldcBefDAmt, ldcAftDAmt;
    String lsDebitItem, lsCDebtKey, lsTimeNow;
    //--上午 9 點 至 下午 3點 才可進行作業
    taroko.base.CommDate commDate = new taroko.base.CommDate();     
    lsTimeNow = commDate.dspTime().replace(":", "");
    
//    if("090000".compareTo(lsTimeNow)>0 || "150000".compareTo(lsTimeNow)<0) {
//    	errmsg("線上扣款只可在 9:00 至 15:00 期間進行 !");
//    	return -1;
//    }
    
    // 已經放行資料，不可調整
    if (eqIgno(wp.itemStr("apr_flag"), "Y")) { //you
      alertErr("此筆已放行不可再調整!!");
      return -1;
    }

    // 若為刪除，則 return 1
    if (strAction.equals("D"))
      return 1;

    // --檢查是否輸入小數點，若有，則顯示警示訊息並 return -1
    // 檢查【dr_amt(D檔金額)】欄位內容，是否有輸入小數點，
    // 若有，則顯示警示訊息並 return -1。
    // 警示訊息內容:【D檔金額:不可輸入小數】。


    // --檢查【扣款金額】--
    ldcDrAmt = wp.itemNum("deduct_amt");
    ldcBefAmt = wp.itemNum("bef_amt");
    if (wp.itemNum("deduct_amt") <= 0) {
      alertErr("扣款金額需大於0");
      return -1;
    }

    // --【adjust_type】欄位內容邏輯--
    // 新增時才做
    if (eqIgno(wp.itemStr("db_table"), "debt")) {
      String adjtype = "";
      String val1 = wp.itemStr("acct_code");
      String val2 = wp.itemStr("bill_type").substring(0, 1);
      if (val1.equals("ID")) {
        if (val2.equals("1")) {
          adjtype = "DE01";
        } else if (val2.equals("2")) {
          adjtype = "DE04";
        } else {
          adjtype = "DE07";
        }
      }

      if (val1.equals("BL") || val1.equals("CB") || val1.equals("CA") || val1.equals("IT")
          || val1.equals("AO") || val1.equals("DB") || val1.equals("OT")) {
        adjtype = "DE08";
      } else if (val1.equals("AF") || val1.equals("LF") || val1.equals("CF") || val1.equals("PF")
          || val1.equals("SF") || val1.equals("CC")) {
        adjtype = "DE09";
      } else if (val1.equals("RI") || val1.equals("AI") || val1.equals("CI")) {
        adjtype = "DE13";
      } else if (val1.equals("PN")) {
        adjtype = "DE14";
      }

      if (empty(adjtype)) {
        alertErr("調整類別比對不到");
        return -1;
      }
      func.varsSet("adjust_type", adjtype);
    }

    return 1;
  }

  int ofcUpdatebefore() throws Exception {
    String lsDeptno, lsGlcode;
    String lsSql = "select a.usr_deptno, b.gl_code from sec_user a, ptr_dept_code b "
        + "where b.dept_code = a.usr_deptno and a.usr_id = :usr_id ";
    setString("usr_id", wp.loginUser);
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      alertErr("無法取得 使用者部門代碼, 起帳部門代碼 !!");
      return -1;
    }
    lsDeptno = sqlStr("usr_deptno");
    lsGlcode = empty(sqlStr("gl_code")) ? "0" : "0" + sqlStr("gl_code").substring(0, 1);
    func.varsSet("job_code", lsDeptno);
    func.varsSet("vouch_job_code", lsGlcode);

    // 若acct_item_ename 為CB或CC或CI或DB，則value_type 為2。
    String lsValueType = "1";
    String val1 = wp.itemStr("acct_code");
    if (val1.equals("CB") || val1.equals("CC") || val1.equals("CI") || val1.equals("DB")) {
      lsValueType = "2";
    }
    func.varsSet("value_type", lsValueType);

    return 1;
  }

  void getAcnobyPseqno(String pseqno) throws Exception {
    mAcctKey = "";
    mChiName = "";
    String lsSql = "select acct_key , dbc_idno.chi_name , dbc_idno.id_no_code, dbc_idno.id_no"
        +", dbc_idno.corp_no "
        + "from dba_acno left join dbc_idno on dba_acno.id_p_seqno = dbc_idno.id_p_seqno "
        + "where p_seqno = :p_seqno ";
    setString("p_seqno", pseqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      mAcctKey = sqlStr("acct_key");
      mChiName = sqlStr("chi_name");
    }
  }

  String wfGetAcctName(String idcode) throws Exception {
    String rtn = "";

    String lsSql = "select chi_long_name from ptr_actcode " + "where acct_code = :acct_code ";
    setString("acct_code", idcode);
    sqlSelect(lsSql);
    if (sqlRowNum > 0)
      rtn = sqlStr("chi_long_name");

    return rtn;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      // this.btnMode_aud();
      this.btnModeAud("XX");
    }
  }

  String fillZeroAcctKey(String acctkey) throws Exception {
    String rtn = acctkey;
    // if (acctkey.trim().length()==8) rtn += "000"; //這支spec 只針對10碼補0
    if (acctkey.trim().length() == 10)
      rtn += "0";

    return rtn;
  }

  // double 相減:
  double sub(double d1, double d2) {
    BigDecimal bd1 = new BigDecimal(Double.toString(d1));
    BigDecimal bd2 = new BigDecimal(Double.toString(d2));
    return bd1.subtract(bd2).doubleValue();
  }
  
  void procFunc() throws Exception {
	  func = new Dbap0070Func(wp);
	  rc = func.procQuery();
	  sqlCommit(rc);
	  
	  if(rc <=0) {
		  if(rc == -2) {
			  alertErr2(func.getMsg());
		  }	else if(rc ==-3) {
			  alertErr2("查詢成功 , 上次交易結果為 = ["+func.queryDesc+"]");
		  }	else
			  alertErr2("查詢失敗 , " + func.queryDesc);
		  return ;
	  }	else	{
		  alertMsg("查詢成功 , 上次交易結果為 = ["+func.queryDesc+"]");
	  }	  
  }
  
}
