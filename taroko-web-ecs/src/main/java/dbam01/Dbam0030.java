/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-08-31  V1.00.00  phopho     program initial                            *
* 2020-03-31 V1.00.01  shiyuqi    Bug Fix                                    *
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*
* 109-12-03  V1.00.03  Justin       remove useless code
* 110-01-04  V1.00.04   shiyuqi       修改无意义命名
* 110-01-06  V1.00.05  Justin       updated for XSS
******************************************************************************/

package dbam01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import java.math.BigDecimal;

public class Dbam0030 extends BaseEdit {
  Dbam0030Func func;
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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("pho_update_disable", "disabled");
    wp.colSet("pho_delete_disable", "disabled");
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

    // 讀取 DBA_ACAJ，若存在資料，則帶入DBA_ACAJ之資料。
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      lsSql =
          "select dba_acaj.orginal_amt, dba_acaj.aft_amt, dba_acaj.aft_d_amt, dba_acaj.acct_code, dba_acaj.purchase_date, "
              + "hex(dba_acaj.rowid) as rowid, 'acaj' as db_table, ptr_actcode.chi_long_name from dba_acaj "
              + "left join ptr_actcode on dba_acaj.acct_code = ptr_actcode.acct_code "
              + "where reference_no = :reference_no " // and decode(apr_flag,'','N',apr_flag) <> 'Y'
              + "order by dba_acaj.crt_date desc, dba_acaj.crt_time desc " + sqlRownum(1);
      setString("reference_no", wp.colStr(ii, "reference_no"));
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        // System.out.println("UUUUU>> sql_nrow"+sql_nrow+", "+wp.col_ss(ii,"reference_no"));
        wp.colSet(ii, "beg_bal", sqlStr("orginal_amt"));
        wp.colSet(ii, "end_bal", sqlStr("aft_amt"));
        wp.colSet(ii, "d_avail_bal", sqlStr("aft_d_amt"));
        wp.colSet(ii, "acct_code", sqlStr("acct_code"));
        wp.colSet(ii, "purchase_date", sqlStr("purchase_date"));
        wp.colSet(ii, "rowid", sqlStr("rowid"));
        wp.colSet(ii, "db_table", sqlStr("db_table"));
        wp.colSet(ii, "wk_acct_code", sqlStr("chi_long_name"));
      }

      // 取得金融帳號 fr dbc_card << 加到Main query一起做掉
    }
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
    if (empty(kkTable))
      kkTable = wp.itemStr("db_table");
    if (empty(kkRowid))
      kkRowid = wp.itemStr("rowid");
    if (eqIgno(kkTable, "debt")) {
      dataReadDebt();
    } else if (eqIgno(kkTable, "acaj")) {
      dataReadAcaj();
    }

    detlWkdata();

  }

  void dataReadDebt() throws Exception {

    wp.selectSQL = " '' as rowid, 'debt' db_table, " + " p_seqno, " + " acct_type, "
        + " reference_no, " + " item_post_date as post_date, " + " item_post_date, "
        + " beg_bal as orginal_amt, " + " 0 as dr_amt, " + " 0 as cr_amt, "
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
    wp.colSet("ex_bef_amt", wp.colStr("bef_amt"));
    wp.colSet("ex_bef_d_amt", wp.colStr("bef_d_amt"));
    wp.colSet("ex_aft_amt", wp.colStr("aft_amt"));
    wp.colSet("ex_aft_d_amt", wp.colStr("aft_d_amt"));
    wp.colSet("pho_delete_disable", "disabled");

    // Set Table Order
    String val5 = wp.colStr("acct_code");
    if (val5.equals("AF") || val5.equals("LF") || val5.equals("CF") || val5.equals("PF")
        || val5.equals("RI") || val5.equals("PN") || val5.equals("AI") || val5.equals("SF")
        || val5.equals("CI") || val5.equals("CC")) {
      // wp.col_set("pho_disable", "disabled ");
    }

  }

  void dataReadAcaj() throws Exception {

    wp.selectSQL = " hex(rowid) as rowid, 'acaj' db_table, " + " p_seqno, " + " acct_type, "
        + " reference_no, " + " post_date, " + " item_post_date, " + " orginal_amt, " + " dr_amt, "
        + " cr_amt, " + " bef_amt, " + " aft_amt, " + " bef_d_amt, " + " aft_d_amt, "
        + " acct_code, " + " func_code, " + " card_no, " + " purchase_date, " + " acct_no, " +
        // " '' as ex_dcount, " + //SPEC中為'': lpad (' ', 2, ' ') AS ex_dcount, 但是問題單要求填'Y' (0000932)
        " 'Y' as ex_dcount, " + " txn_code, " + " apr_flag, " + " mod_seqno, " + " adj_comment, "
        + " c_debt_key, " + " debit_item ";

    wp.daoTable = "dba_acaj";
    wp.whereStr = "where reference_no = :reference_no ";
    setString("reference_no", kkReferenceNo);
    // wp.whereStr = "where rowid = :rowid " ;
    // setRowid("rowid", kk_rowid);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, TABLE='dba_acaj', reference_no= " + kkReferenceNo);
      return;
    }
    wp.colSet("ex_bef_amt", wp.colStr("bef_amt"));
    wp.colSet("ex_bef_d_amt", wp.colStr("bef_d_amt"));
    wp.colSet("ex_aft_amt", wp.colStr("aft_amt"));
    wp.colSet("ex_aft_d_amt", wp.colStr("aft_d_amt"));
  }

  void detlWkdata() throws Exception {
    String param = "";

    param = wp.colStr("p_seqno");
    getAcnobyPseqno(param);
    wp.colSet("acct_key", mAcctKey);
    wp.colSet("ex_cname", mChiName);
    param = wp.colStr("acct_code");
    wp.colSet("tt_acct_code", wfGetAcctName(param));
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Dbam0030Func(wp);

    if (ofValidation() < 0)
      return;

    if (ofcUpdatebefore() < 0)
      return;

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

    if (strAction.equals("U") && rc == 1) {
      kkTable = "acaj";
      dataRead();
    }
  }

  // 1. 資料檢核。
  // 2. 設定dba_acaj.adjust_type欄位值。
  int ofValidation() throws Exception {
    double ldcBefAmt, ldcDrAmt, ldcBefDAmt, ldcAftDAmt;
    String lsDebitItem, lsCDebtKey;

    // 已經放行資料，不可調整
    if (eqIgno(wp.itemStr("apr_flag"), "Y")) {
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

    // --檢查【目前可D數】--
    ldcBefDAmt = wp.itemNum("bef_d_amt");
    if (ldcBefDAmt < 0) {
      alertErr("目前可D數錯誤: 金額<0");
      return -1;
    }

    // --檢查【最新可D數】--
    ldcAftDAmt = wp.itemNum("aft_d_amt");
    if (ldcAftDAmt < 0) {
      alertErr("最新可D數錯誤: 金額<0");
      return -1;
    }

    // --檢查【D檔金額】--
    ldcDrAmt = wp.itemNum("dr_amt");
    ldcBefAmt = wp.itemNum("bef_amt");
    if (wp.itemNum("dr_amt") <= 0) {
      alertErr("D檔金額需大於0");
      return -1;
    }

    // --檢查【借方科目】--
    lsDebitItem = wp.itemStr("debit_item");
    if (empty(lsDebitItem)) {
      alertErr("借方科目 不可空白");
      return -1;
    }

    // -B94262(JH)--
    if (ldcBefAmt == 0 || ldcDrAmt <= ldcBefAmt) {
    } else {
      if (eqIgno(lsDebitItem, "14817000") == false) {
        alertErr("借方科目 需為 1481-7000");
        return -1;
      }
    }

    /*2022/04/01暫時不處理會科, 科目代號及銷帳鍵值第二階段不檢查

    // 檢查【借方科目(debit_item)】，需存在於GEN_ACCT_M table 中。
    String lsSql = "select ac_no from gen_acct_m where ac_no = :ac_no ";
    setString("ac_no", lsDebitItem);
    sqlSelect(lsSql);
    if (sqlRowNum == 0) {
      alertErr("調整錯誤:借方科目");
      return -1;
    }

    // --檢查【銷帳鍵值】--
    lsCDebtKey = wp.itemStr("c_debt_key");
    if (eqIgno(lsDebitItem.substring(0, 4), "1751") && empty(lsCDebtKey)) {
      alertErr("調整錯誤:借方科目1751,一定要有銷帳鍵值");
      return -1;
    }
    if (eqIgno(lsDebitItem.substring(0, 4), "1751") && lsCDebtKey.length() != 20) {
      alertErr("調整錯誤:借方科目1751,銷帳鍵值須為20碼");
      return -1;
    }
    
    */

    // --借貸金額--
    if (ldcDrAmt > ldcBefDAmt) {
      alertErr("D檔金額不可大於目前可D數!");
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
	    String lsDeptno = "", lsGlcode = "";
	    
	    /* 2022/04/01暫時不處理會科
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
	    
	    */
	    
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
    String lsSql = "select acct_key , dbc_idno.chi_name "
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

}
