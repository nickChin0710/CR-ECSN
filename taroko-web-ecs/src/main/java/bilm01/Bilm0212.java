/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-12  V1.00.00  yash       program initial                            *
* 108-11-29  V1.00.01  Amber	  Update init_button  Authority 			 *
* 109-01-06  V1.00.04  Ru Chen    Modify AJAX                                *
* 109-04-23  V1.00.05  shiyuqi       updated for project coding standard     * 
* 109-01-04  V1.00.06   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package bilm01;



import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Bilm0212 extends BaseAction {
  String mExKeyNo = "";
  String gsRowid = "";
  String gsBillType = "";
  String gsIdNo = "";
  String gsIdNoCode = "";
  String gsIdPSeqno = "";
  String gsPSeqno = "";
  String gsCardNo = "";
  String gsAcctType = "";
  String gsChiName = "";
  String gsDbStatus = "";
  String gsCorpNo = "";
  String gsCorpPSeqno = "";
  String gsDbEnd = "";
  String gsCompanyName = "";
  String gsPurchaseDate = "";
  String gsAddItem = "";
  String gsBillDesc = "";
  String gsCurrCode = "";
  double gsDcAcctJrnlBal = 0;
  double gsDestAmt = 0;
  double gsDcDestinationAmt = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      // insertFunc();
      strAction = "A";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      // updateFunc();
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      // deleteFunc();
      strAction = "D";
      saveFunc();
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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "D1")) {
      /* 清畫面 */
      strAction = "";
      delFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      // 20200106 modify AJAX
      itemchanged();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    if (wp.respHtml.indexOf("_add") > 0) {
      wp.colSet("purchase_date", getSysDate());
    }
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.sqlCmd = "select rowid, " + "key_no, " + "card_no, " + "id_p_seqno, " + "id_no, "
        + "id_no_code, " + "p_seqno, " + "db_status, " + "chi_name, " + "bill_type, " + "tx_code, "
        + "add_item, " + "acct_type, " + "corp_no, " + "company_name, " + "seq_no, " + "dest_curr, "
        + "purchase_date, " + "chi_desc, " + "bill_desc, " + "dept_flag, " + "post_flag, "
        + "curr_code, " + "decode(curr_code,'901',dest_amt,dc_dest_amt) dc_destination_amt, "
        + "dest_amt,  " + "dc_dest_amt, " + "dc_acct_jrnl_bal, " + "db_modify_flag, " + "apr_user, "
        + "apr_date, " + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno," + "error_code "
        + "from( " + "SELECT hex(a.rowid)as rowid, " + "a.key_no, " + "a.card_no, "
        + "a.id_p_seqno, " + "uf_idno_id(a.id_p_seqno) id_no, "
        + "(select id_no_code from crd_idno b where a.id_p_seqno = b.id_p_seqno) as id_no_code, "
        + " (SELECT d.p_seqno FROM crd_card d WHERE a.card_no = d.card_no) AS p_seqno, "
        + "(select c.acct_status from act_acno c where a.id_p_seqno = c.id_p_seqno and a.acct_type=c.acct_type fetch first 1 rows only) as db_status, "
        + "uf_idno_name(a.id_p_seqno) chi_name, " + "a.bill_type, " + "a.tx_code, " + "a.add_item, "
        + "a.acct_type, " + "a.corp_no, " + "uf_corp_name(a.corp_no) company_name, " + "a.seq_no, "
        + "a.dest_curr, " + "a.purchase_date, " + "a.chi_desc, " + "a.bill_desc, " + "a.dept_flag, "
        + "a.post_flag, " + "decode(a.curr_code,'','901',a.curr_code) curr_code, " + "a.dest_amt, "
        + "a.dc_dest_amt, " + "0.00 dc_acct_jrnl_bal, " + "'' db_modify_flag, " + "a.apr_user, "
        + "a.apr_date, " + "a.mod_user, " + "a.mod_time, " + "a.mod_pgm, " + "a.mod_seqno, "
        + "a.error_code " + "FROM bil_othexp a where 1=1 and post_flag='N' ";
    if (empty(wp.itemStr("ex_key")) == false) {
      wp.sqlCmd += " and  a.key_no like :ex_key ";
      setString("ex_key", wp.itemStr("ex_key") + "%");
    }

    if (empty(wp.itemStr("ex_user")) == false) {
      wp.sqlCmd += " and a.mod_user like :ex_user ";
      setString("ex_user", wp.itemStr("ex_user") + "%");
    }

    if (empty(wp.itemStr("ex_curr_code")) == false) {
      if (wp.itemStr("ex_curr_code").equals("901")) {
        wp.sqlCmd += " and (a.curr_code ='' or a.curr_code =:ex_curr_code ) ";
        setString("ex_curr_code", wp.itemStr("ex_curr_code"));
      } else {
        wp.sqlCmd += " and a.curr_code = :ex_curr_code ";
        setString("ex_curr_code", wp.itemStr("ex_curr_code"));
      }
    }

    if (empty(wp.itemStr("ex_id")) == false) {
      wp.sqlCmd +=
          " and a.id_p_seqno = (select id_p_seqno from crd_idno where 1=1 and id_no =:ex_id) ";
      setString("ex_id", wp.itemStr("ex_id"));
    }

    if (empty(wp.itemStr("ex_card")) == false) {
      wp.sqlCmd += " and a.card_no like :ex_card ";
      setString("ex_card", wp.itemStr("ex_card"));
    }

    if (empty(wp.itemStr("ex_err")) == false) {
      if (wp.itemStr("ex_err").equals("1")) {
        wp.sqlCmd += " and a.error_code ='' ";
      }

      if (wp.itemStr("ex_err").equals("2")) {
        wp.sqlCmd += " and a.error_code !='' ";
      }

    }



    wp.sqlCmd += " order by key_no,acct_type,card_no ) ";

    wp.pageCountSql = "select count(*) from (";
    wp.pageCountSql += wp.sqlCmd;
    wp.pageCountSql += ") ";

    // System.out.println(wp.sqlCmd);
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    String lsSql = "", lsAcctStatus = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // db_status
      lsAcctStatus = wp.colStr(ii, "db_status");
      String[] cde = new String[] {"1", "2", "3", "4"};
      String[] txt = new String[] {"1.非呆帳戶", "2.非呆帳戶", "3.非呆帳戶", "4.呆帳戶"};
      wp.colSet(ii, "db_status", commString.decode(lsAcctStatus, cde, txt));
      // db_add_item
      lsSql = "select exter_desc " + "from ptr_billtype " + "WHERE bill_type = 'OKOL' "
          + "and txn_code =:ls_txn_code ";
      setString("ls_txn_code", wp.colStr(ii, "add_item"));
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "db_add_item", wp.colStr(ii, "add_item") + "_" + sqlStr("exter_desc"));
      } else {
        wp.colSet(ii, "db_add_item", wp.colStr(ii, "add_item"));
      }
      // dc_acct_jrnl_bal
      gsPSeqno = wp.colStr(ii, "p_seqno");
      gsCurrCode = wp.colStr(ii, "curr_code");
      wfGetDcAcctJrnlBal(ii, gsCurrCode, gsPSeqno);
    }
  }

  @Override
  public void querySelect() throws Exception {
    mExKeyNo = wp.itemStr("key_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExKeyNo = wp.itemStr("kk_key_no");
    if (empty(mExKeyNo)) {
      mExKeyNo = itemkk("data_k1");
    }
    gsRowid = itemkk("data_k2");
    String wkModSeqno = itemkk("data_k3");
    wp.sqlCmd = "select rowid, " + "key_no, " + "card_no, " + "id_p_seqno, " + "id_no, "
        + "id_no_code, " + "p_seqno, " + "db_status, " + "chi_name, " + "bill_type, " + "tx_code, "
        + "add_item as txn_code, " + "acct_type, " + "corp_no, " + "company_name, " + "seq_no, "
        + "dest_curr, " + "purchase_date, " + "chi_desc, " + "bill_desc, " + "dept_flag, "
        + "post_flag, " + "curr_code, "
        + "decode(curr_code,'901',dest_amt,dc_dest_amt) dc_destination_amt, " + "dest_amt,  "
        + "dc_dest_amt, " + "dc_acct_jrnl_bal, " + "db_modify_flag, " + "apr_user, " + "apr_date, "
        + "error_code, " + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno " + "from( "
        + "SELECT hex(rowid)as rowid, " + "a.key_no, " + "a.card_no, " + "a.id_p_seqno, "
        + " (SELECT d.p_seqno FROM crd_card d WHERE a.card_no = d.card_no) AS p_seqno, "
        + "uf_idno_id(a.id_p_seqno) id_no, "
        + "(select id_no_code from crd_idno b where a.id_p_seqno = b.id_p_seqno) as id_no_code, "
        + "(select c.acct_status from act_acno c where a.id_p_seqno = c.id_p_seqno and a.acct_type=c.acct_type fetch first 1 rows only) as db_status, "
        + "uf_idno_name(a.id_p_seqno) chi_name, " + "a.bill_type, " + "a.tx_code, "
        + "a.add_item , " + "a.acct_type, " + "a.corp_no, "
        + "uf_corp_name(a.corp_no) company_name, " + "a.seq_no, " + "a.dest_curr, "
        + "a.purchase_date, " + "a.chi_desc, " + "a.bill_desc, " + "a.dept_flag, " + "a.post_flag, "
        + "decode(a.curr_code,'','901',a.curr_code) curr_code, " + "a.dest_amt, "
        + "a.dc_dest_amt, " + "0.00 dc_acct_jrnl_bal, " + "'' db_modify_flag, " + "a.apr_user, "
        + "a.apr_date, " + "a.error_code, " + "a.mod_user, " + "a.mod_time, " + "a.mod_pgm, "
        + "a.mod_seqno " + "FROM bil_othexp a ";
    wp.sqlCmd += "where 1=1 ";
    wp.sqlCmd += sqlCol(gsRowid, "hex(rowid)");
    wp.sqlCmd += sqlCol(wkModSeqno, "mod_seqno");
    wp.sqlCmd += "and post_flag='N' )";
    // System.out.println("sqlCmd1 : "+wp.sqlCmd);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無明細資料");
      return;
    }
    wp.colSet("kk_key", wp.colStr("key_no"));
    listWkdata1();
  }

  void listWkdata1() throws Exception {
    String lsSql = "", lsAcctStatus = "";
    // db_status
    lsAcctStatus = wp.colStr("db_status");
    String[] cde = new String[] {"1", "2", "3", "4"};
    String[] txt = new String[] {"1.非呆帳戶", "2.非呆帳戶", "3.非呆帳戶", "4.呆帳戶"};
    wp.colSet("db_status", commString.decode(lsAcctStatus, cde, txt));
    // db_add_item
    lsSql = "select exter_desc " + "from ptr_billtype " + "WHERE bill_type = 'OKOL' "
        + "and txn_code =:ls_txn_code ";
    setString("ls_txn_code", wp.colStr("add_item"));
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      wp.colSet("db_add_item", wp.colStr("add_item") + "_" + sqlStr("exter_desc"));
    } else {
      wp.colSet("db_add_item", wp.colStr("add_item"));
    }
    // dc_acct_jrnl_bal
    gsPSeqno = wp.colStr("p_seqno");
    gsCurrCode = wp.colStr("curr_code");
    wfGetDcAcctJrnlBal(1, gsCurrCode, gsPSeqno);

  }

  @Override
  public void saveFunc() throws Exception {
    Bilm0212Func func = new Bilm0212Func(wp);
    gsRowid = wp.itemStr("rowid");
    gsChiName = wp.itemStr("chi_name");
    gsDbEnd = wp.itemStr("db_end");
    gsDbStatus = strMid(wp.itemStr("db_status"), 0, 1);
    gsDcAcctJrnlBal = wp.itemNum("dc_acct_jrnl_bal");
    gsIdPSeqno = wp.itemStr("id_p_seqno");
    gsPSeqno = wp.itemStr("p_seqno");
    gsCorpPSeqno = wp.itemStr("corp_p_seqno");
    gsCompanyName = wp.itemStr("company_name");
    gsDestAmt = wp.itemNum("dest_amt");
    gsDcDestinationAmt = wp.itemNum("dc_destination_amt");
    gsAddItem = wp.itemStr("txn_code");
    gsBillDesc = wp.itemStr("bill_desc");
    gsCurrCode = wp.itemStr("curr_code");
    String lsSql = "", lsIdNo = "", lsError = "", lsCorp = "", lsAmt = "", lsCard = "";

    String lsAcctType = "";

    lsIdNo = wp.itemStr("id_no");
    lsCorp = wp.itemStr("corp_no");
    lsCard = wp.itemStr("card_no");
    lsAmt = wp.itemStr("dc_destination_amt");
    if (strAction.equals("D")) {
      if (!empty(wp.itemStr("apr_user")) && !empty(wp.itemStr("apr_date"))) {
        alertErr("此筆為已放行資料不可刪除!! ");
        return;
      } else {
        int rc = func.dbDelete();
        sqlCommit(rc);
        clearFunc();
        return;
      }
    }
    // 匯入資料的檢查--Start
    // check id_no
    lsSql = "select id_p_seqno,chi_name,id_no_code " + "from crd_idno " + "where 1=1 ";
    lsSql += sqlCol(lsIdNo, "id_no");
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      lsError = "1";
    }
    // check corp_no
    if (!empty(lsCorp)) {
      lsSql = "select corp_p_seqno,corp_no,chi_name " + "from crd_corp " + "where 1=1 ";
      lsSql += sqlCol(lsCorp, "corp_no");
      sqlSelect(lsSql);
      if (sqlRowNum <= 0) {
        lsError += "2";
      }
    }

    // check amt
    if (!isNumber(lsAmt)) {
      lsError += "3";
    }

    // check card_no
    lsSql = "select p_seqno,card_no,acct_type " + "from crd_card " + "where 1=1 ";
    lsSql += sqlCol(lsCard, "card_no");
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      lsError += "4";
    } else {
      gsPSeqno = sqlStr("p_seqno");
      lsAcctType = sqlStr("acct_type");
    }
    // check act_acno
    lsSql = "select acct_status, " + "decode(corp_act_flag,'','N',corp_act_flag) as corp_act_flag, "
        + "corp_p_seqno " + "from	act_acno " + "where 1=1 ";
    lsSql += sqlCol(gsPSeqno, "p_seqno");
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      lsError += "5";
    }
    if (!empty(lsError)) {
      alertErr("資料有誤,請依錯誤代碼修正資料!!");
      wp.colSet("error_code", lsError);
      return;
    } else {
      wp.colSet("error_code", "");
    }
    // 匯入資料的檢查--End
    if (ofValidation() != 1) {
      return;
    }
    if (strAction.equals("U")) {
      
      /*acct_type從卡檔取得
      String idtype = wp.itemStr("id_no") + wp.itemStr("id_no_code");

      lsSql = "select acct_type from act_acno where acct_key=:acct_key ";
      setString("acct_key", idtype);
      sqlSelect(lsSql);
      String lsAcctType = sqlStr("acct_type");
	  */

      if (!lsAcctType.equals(wp.itemStr("acct_type"))) {
        alertErr("身分證與帳戶不符!");
        return;
      }


      int rc = func.dbUpdate();
      sqlCommit(rc);
    }
    if (strAction.equals("A")) {
      
      /* acct_type從卡檔取得
      String idtype = wp.itemStr("id_no") + wp.itemStr("id_no_code");

      lsSql = "select acct_type from act_acno where acct_key=:acct_key ";
      setString("acct_key", idtype);
      sqlSelect(lsSql);
      String lsAcctType = sqlStr("acct_type");
      */

      if (!lsAcctType.equals(wp.itemStr("acct_type"))) {
        alertErr("身分證與帳戶不符!");
        return;
      }

      String lsGsCorpNo = wp.itemStr("corp_no");
      if (!empty(lsGsCorpNo)) {
        lsSql = "select chi_name,corp_p_seqno " + "from crd_corp " + "where 1=1 ";
        lsSql += sqlCol(lsGsCorpNo, "corp_no");
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          alertErr("此公司統編不存在!");
          return;
        }
      }

      if (wp.itemStr("acct_type").equals("02")) {
        if (empty(wp.itemStr("corp_no"))) {
          alertErr("法人統編不可空白!");
          return;
        }

        if (empty(wp.itemStr("dc_destination_amt")) || wp.itemNum("dc_destination_amt") <= 0) {
          alertErr("金額不可為0!");
          return;
        }
      }


      int rc = func.dbInsert();
      sqlCommit(rc);
      if (rc > 0) {
        clearFunc();
      }
    }
  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnAddOn(wp.autUpdate());
    btnDeleteOn(wp.autUpdate());
  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_add") > 0) {
        wp.optionKey = empty(wp.colStr("acct_type")) ? "01" : wp.colStr("acct_type");
        this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name",
            "where 1=1  order by acct_type");

        wp.initOption = "--";
        wp.optionKey = empty(wp.colStr("curr_code")) ? "901" : wp.colStr("curr_code");
        this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1  and wf_type = 'DC_CURRENCY' order by wf_id");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("txn_code");;
        this.dddwList("dddw_add_item", "ptr_billtype", "txn_code", "exter_desc",
            "where 1=1  and bill_type = 'OKOL' order by acct_code");
      }
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = empty(wp.colStr("acct_type")) ? "01" : wp.colStr("acct_type");
        this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name",
            "where 1=1  order by acct_type");

        wp.initOption = "--";
        wp.optionKey = empty(wp.colStr("curr_code")) ? "901" : wp.colStr("curr_code");
        this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1  and wf_type = 'DC_CURRENCY' order by wf_id");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("txn_code");;
        this.dddwList("dddw_add_item", "ptr_billtype", "txn_code", "exter_desc",
            "where 1=1  and bill_type = 'OKOL' order by acct_code");
      } else {
        wp.initOption = "--";
        wp.optionKey = empty(wp.itemStr("ex_curr_code")) ? "901" : wp.itemStr("ex_curr_code");
        this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1  and wf_type = 'DC_CURRENCY' order by wf_id");
      }
    } catch (Exception ex) {
    }
  }

  public int wfGetDcAcctJrnlBal(int ii, String lsCurrCode, String lsPSeqno) {
    String lsSql = "";
    gsDcAcctJrnlBal = 0;

    if (!empty(gsPSeqno) & !empty(gsCurrCode)) {
      lsSql =
          "select nvl(dc_acct_jrnl_bal,0) dc_acct_jrnl_bal " + "from act_acct_curr " + "where 1=1 ";
      lsSql += sqlCol(lsCurrCode, "curr_code");
      lsSql += sqlCol(lsPSeqno, "p_seqno");
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        if (wp.respHtml.indexOf("_detl") > 0) {
          wp.colSet("dc_acct_jrnl_bal", sqlStr("dc_acct_jrnl_bal"));
        } else {
          wp.colSet(ii, "dc_acct_jrnl_bal", sqlStr("dc_acct_jrnl_bal"));
        }
        gsDcAcctJrnlBal = sqlNum("dc_acct_jrnl_bal");
      } else {
        // alert_msg("帳戶無結算幣別 !!!");
        return -1;
      }
    }
    // db_end
    if (gsDcAcctJrnlBal != 0) {
      gsDbEnd = "N";
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.colSet("db_end", "N");
      } else {
        wp.colSet(ii, "db_end", "N");
      }
    } else {
      gsDbEnd = "Y";
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.colSet("db_end", "Y");
      } else {
        wp.colSet(ii, "db_end", "Y");
      }
    }
    return 1;
  }

  public int wfCheckId(String lsIdNo, String lsIdNoCode, String lsAcctType, String lsCurrCode,
      String lsPSeqno) {
    String lsSql = "", lsCorpActFlag = "";
    gsIdNo = lsIdNo;
    gsIdNoCode = lsIdNoCode;

    lsSql = "select id_p_seqno,chi_name " + "from crd_idno " + "where 1=1 ";
    lsSql += sqlCol(lsIdNo, "id_no");
    lsSql += sqlCol(lsIdNoCode, "id_no_code");
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      alertMsg("查無卡人資料!! id_no = " + gsIdNo);
      return -1;
    } else {
      gsIdPSeqno = sqlStr("id_p_seqno");
      gsChiName = sqlStr("chi_name");
    }
    //
    lsSql = "select acct_status, " + "decode(corp_act_flag,'','N',corp_act_flag) as corp_act_flag, "
        + "corp_p_seqno " + "from	act_acno " + "where 1=1 ";
    lsSql += sqlCol(gsIdPSeqno, "id_p_seqno");
    lsSql += sqlCol(lsAcctType, "acct_type");
    lsSql += " fetch first 1 rows only ";
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      alertMsg("Select act_acno error !!");
      return -1;
    } else {
      gsDbStatus = sqlStr("acct_status");
      lsCorpActFlag = sqlStr("corp_act_flag");
      gsCorpPSeqno = sqlStr("corp_p_seqno");
    }
    wfGetDcAcctJrnlBal(1, lsCurrCode, lsPSeqno);
    return 1;
  }

  // 20200106 modify AJAX
  public int itemchanged() throws Exception {
    // super.wp = wr;
    String ajaxName = "";
    String option = "";
    String lsSql = "";
    int rc = 0;
    ajaxName = wp.itemStr("ajaxName");

    switch (ajaxName) {
      case "detailCardNo":
        gsCardNo = wp.itemStr("card_no");
        gsCurrCode = wp.itemStr("curr_code");
        gsAcctType = wp.itemStr("acct_type");

        lsSql = "select a.chi_name, " + "a.id_no, " + "a.id_no_code, " + "a.id_p_seqno, "
            + "b.p_seqno " + "from crd_idno a , crd_card b " + "where 1=1 "
            + "and a.id_p_seqno = b.id_p_seqno ";
        lsSql += sqlCol(gsCardNo, "b.card_no");
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          wp.addJSON("card_desp", "卡號無效，查無卡檔資料!!");
          wp.addJSON("id_no", "");
          wp.addJSON("id_no_code", "");
          wp.addJSON("chi_name", "");
          wp.addJSON("jdb_end", "");
          wp.addJSON("db_end", "");
          wp.addJSON("id_p_seqno", "");
          wp.addJSON("corp_p_seqno", "");
          wp.addJSON("jdb_status", "");
          wp.addJSON("db_status", "");
          wp.addJSON("jdc_acct_jrnl_bal", "0");
          wp.addJSON("dc_acct_jrnl_bal", "0");
          break;
        } else {
          gsIdPSeqno = sqlStr("id_p_seqno");
          gsIdNo = sqlStr("id_no");
          gsIdNoCode = sqlStr("id_no_code");
          gsPSeqno = sqlStr("p_seqno");
          gsChiName = sqlStr("chi_name");
          wfCheckId(gsIdNo, gsIdNoCode, gsAcctType, gsCurrCode, gsPSeqno);
          wp.addJSON("card_desp", "");
          wp.addJSON("id_no", gsIdNo);
          wp.addJSON("id_no_code", gsIdNoCode);
          wp.addJSON("chi_name", gsChiName);
          wp.addJSON("id_name", gsChiName);
          wp.addJSON("jdb_end", gsDbEnd);
          wp.addJSON("db_end", gsDbEnd);
          wp.addJSON("id_p_seqno", gsIdPSeqno);
          wp.addJSON("corp_p_seqno", gsCorpPSeqno);
          String[] cde2 = new String[] {"1", "2", "3", "4"};
          String[] txt2 = new String[] {"1.非呆帳戶", "2.非呆帳戶", "3.非呆帳戶", "4.呆帳戶"};
          wp.addJSON("jdb_status", commString.decode(gsDbStatus, cde2, txt2));
          wp.addJSON("db_status", gsDbStatus);
          wp.addJSON("jdc_acct_jrnl_bal", numToStr(gsDcAcctJrnlBal, "#,###"));
          wp.addJSON("dc_acct_jrnl_bal", numToStr(gsDcAcctJrnlBal, "###"));
        }
        break;
      case "CorpDesp":
        gsCorpNo = wp.itemStr("corp_no");
        lsSql = "select chi_name,corp_p_seqno " + "from crd_corp " + "where 1=1 ";
        lsSql += sqlCol(gsCorpNo, "corp_no");
        lsSql += sqlCol(gsCardNo, "b.card_no");
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          wp.addJSON("corp_desp", "此公司統編不存在!");
          wp.addJSON("corp_p_seqno", "");
          wp.addJSON("jcompany_name", "");
          wp.addJSON("company_name", "");
          break;
        } else {
          gsCorpPSeqno = sqlStr("corp_p_seqno");
          gsCompanyName = sqlStr("chi_name");
          wp.addJSON("corp_desp", "");
          wp.addJSON("corp_p_seqno", gsCorpPSeqno);
          wp.addJSON("jcompany_name", gsCompanyName);
          wp.addJSON("company_name", gsCompanyName);
        }
        break;
      case "detailPdate":
        gsPurchaseDate = wp.itemStr("purchase_date");
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMMdd");
        java.util.Date beginDate = format.parse(gsPurchaseDate);
        java.util.Date endDate = format.parse(getSysDate());
        long day = (beginDate.getTime() - endDate.getTime()) / (24 * 60 * 60 * 1000);
        if (day > 20) {
          alertMsg("消費日不可大於現在日期20日以上!!");
        }
        break;
      case "detailBill_desc":
        gsAddItem = wp.itemStr("add_item");
        gsBillDesc = wp.itemStr("bill_desc");
        if (empty(gsBillDesc)
            & (gsAddItem.equals("05") | gsAddItem.equals("OI") | gsAddItem.equals("HC"))) {
          alertMsg("其他費用或餘額代償或其他應收款 ,必需輸入  對帳單文字 !!!");
        }
        if (!empty(gsBillDesc)
            & (!gsAddItem.equals("05") & !gsAddItem.equals("OI") & !gsAddItem.equals("HC"))) {
          alertMsg("非其他費用及餘額代償及其他應收款 ,不能輸入  對帳單文字 !!!");
        }
        break;
      case "detailCurrCode":
        gsCurrCode = wp.itemStr("curr_code");
        gsPSeqno = wp.itemStr("p_seqno");
        rc = wfGetDcAcctJrnlBal(1, gsCurrCode, gsPSeqno);
        break;
      case "detailAmt":
        gsCurrCode = wp.itemStr("curr_code");
        gsDcDestinationAmt = wp.itemNum("dc_destination_amt");
        lsSql = "select exchange_rate " + "from PTR_CURR_RATE " + "where 1=1 ";
        lsSql += sqlCol(gsCurrCode, "curr_code");
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          gsDestAmt = gsDcDestinationAmt * sqlNum("exchange_rate");
        }
        wp.addJSON("jdest_amt", numToStr(gsDestAmt, "###"));
        wp.addJSON("dest_amt", numToStr(gsDestAmt, "###"));
        break;
    }
    return 1;
  }

  public int ofValidation() {
    // bill_type
	
	/*  //TCB沒有代償交易
    if (gsAddItem.equals("05")) {
      wp.colSet("bill_type", "BTAO");
      gsBillType = "BTAO";
    } else {
      wp.colSet("bill_type", "OKOL");
      gsBillType = "OKOL";
    }
    */
    
    wp.colSet("bill_type", "OKOL");
    gsBillType = "OKOL";
    
    // gs_bill_desc
    if (empty(gsBillDesc)
        && (gsAddItem.equals("05") || gsAddItem.equals("OI") || gsAddItem.equals("HC"))) {
      alertErr("其他費用或餘額代償或其他應收款 ,必需輸入  對帳單文字 !!!");
      return -1;
    }
    if (!empty(gsBillDesc)
        && (!gsAddItem.equals("05") && !gsAddItem.equals("OI") && !gsAddItem.equals("HC"))) {
      alertErr("非其他費用及餘額代償及其他應收款 ,不能輸入  對帳單文字 !!!");
      return -1;
    }
    // 呆帳戶法訴費只能為SF不可選LS
    if (gsDbStatus.equals("4") && gsAddItem.equals("LS")) {
      alertErr("加檔呆帳戶法訴費，加檔項目請選擇『SF呆帳戶法訴費』!");
      return -1;
    }
    // 非呆帳戶法訴費只能為LS不可選SF
    if (!gsDbStatus.equals("4") & gsAddItem.equals("SF")) {
      alertErr("加檔非呆帳戶法訴費，加檔項目請選擇『LS法訴費』!");
      return -1;
    }
    // gs_dc_destination_amt & gs_dest_amt
    if (gsDcDestinationAmt < 0 || gsDestAmt < 0) {
      // alert_err("結算金額,台幣金額須>0");
      return -1;
    }
    rc = wfChkAcctCurr();
    if (rc != 1) {
      return -1;
    }

    return 1;
  }

  public int wfChkAcctCurr() {
    String lsSql = "";
    lsSql = "select count(*) ct " + "from act_acct_curr " + "where 1=1";
    lsSql += sqlCol(gsPSeqno, "p_seqno");
    lsSql += sqlCol(gsCurrCode, "curr_code");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      if (sqlNum("ct") == 0) {
        alertErr("帳戶無結算幣別  : " + gsCurrCode);
        return -1;
      }
    }
    return 1;
  }

  public int wfCheckCardno() {
    String lsSql = "";
    lsSql = "select current_code " + "from crd_card " + "where 1=1";
    lsSql += sqlCol(gsCardNo, "card_no");
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      alertErr("查無卡檔資料!!");
      return -1;
    }
    return 1;
  }

  @Override
  public void userAction() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    fileDataImp();

  }

  void fileDataImp() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");
    // int fi = tf.openInputText(inputFile,"UTF-8"); //決定上傳檔內碼
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }

    Bilm0212Func func = new Bilm0212Func(wp);
    func.setConn(wp);

    String lsSql = "";
    int llOk = 0, llErr = 0, llCnt = 0, llErrFormat = 0;
    
    while (true) {
    	
      String fileLength = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (fileLength.length() < 2) {
        continue;
      }
      int file = fileLength.split(",").length;
      
      llCnt++;
      // if(aa < 7){
      // err_alert("檔案格式錯誤!!");
      // return;
      // }
      if (file < 7) {
        llErrFormat++;
        continue;
      }

      String lsError = "";
      String[] splitLine = fileLength.split(",");
      try {
        String lsIdNo = splitLine[0];// A224331327
        String lsCorp = splitLine[1];// 統編
        String lsAmt = splitLine[2];// 20
        String lsCard = splitLine[3];// 3565538600947899
        String lsType = splitLine[4];// HC
        String lsDate = splitLine[5];// 1070430
        String lsMsg = splitLine[6];// 加檔說明
        
        // server debug message
        // wp.alertMesg = "<script language='javascript'>
        // alert('"+ls_id+ls_amt+ls_card+ls_type+ls_date+"')</script>";

        // check id_no
        lsSql = "select id_p_seqno,chi_name,id_no_code " + "from crd_idno " + "where 1=1 ";
        lsSql += sqlCol(lsIdNo, "id_no");
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          lsError = "1";
        } else {
          gsIdPSeqno = sqlStr("id_p_seqno");
          gsChiName = sqlStr("chi_name");
          gsIdNoCode = sqlStr("id_no_code");
        }

        // check corp_no
        if (!empty(lsCorp)) {
          lsSql = "select corp_p_seqno,corp_no,chi_name " + "from crd_corp " + "where 1=1 ";
          lsSql += sqlCol(lsCorp, "corp_no");
          sqlSelect(lsSql);
          if (sqlRowNum <= 0) {
            lsError += "2";
          } else {
            gsCorpPSeqno = sqlStr("corp_p_seqno");
            gsChiName = sqlStr("chi_name");
          }
        }

        // check amt
        if (!isNumber(lsAmt)) {
          lsError += "3";
        }

        // check card_no
        lsSql = "select p_seqno,card_no " + "from crd_card " + "where 1=1 ";
        lsSql += sqlCol(lsCard, "card_no");
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          lsError += "4";
        } else {
          gsPSeqno = sqlStr("p_seqno");
        }

        gsAcctType = "01";
        gsCurrCode = "901";
        gsIdNo = lsIdNo;
        gsDestAmt = toNum(lsAmt);
        gsCardNo = lsCard;
        gsAddItem = lsType;
        gsPurchaseDate = numToStr(toNum(strMid(lsDate, 0, 3)) + 1911, "###") + strMid(lsDate, 3, 4);
        gsBillDesc = lsMsg;

        // check act_acno
        lsSql =
            "select acct_status, " + "decode(corp_act_flag,'','N',corp_act_flag) as corp_act_flag, "
                + "corp_p_seqno " + "from	act_acno " + "where 1=1 ";
        lsSql += sqlCol(gsPSeqno, "p_seqno");
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          lsError += "5";
        } else {
          gsDbStatus = sqlStr("acct_status");
          gsCorpPSeqno = sqlStr("corp_p_seqno");
        }

        // if(wf_check_id(gs_id_no,gs_id_no_code,gs_acct_type,gs_curr_code,gs_p_seqno)!=1){
        // ls_error="1";
        // }
        // PB未檢核這段 取消
        // if(wf_get_dc_acct_jrnl_bal(1,gs_curr_code,gs_p_seqno)!=1){
        // ls_error+="9";
        // }
        // bill_type
        
        /*  //TCB沒有代償
        if (gsAddItem.equals("05")) {
          gsBillType = "BTAO";
        } else {
          gsBillType = "OKOL";
        }
        */
        
        wp.colSet("bill_type", "OKOL");
        gsBillType = "OKOL";
        
        // 匯入資料不檢查validation() 20190422 by Andy
        // if(of_validation()!=1){;
        // ls_error+="V";
        // }

        wp.itemSet("bill_type", gsBillType);
        wp.itemSet("tx_code", gsAddItem);
        wp.itemSet("add_item", gsAddItem);
        wp.itemSet("card_no", gsCardNo);
        wp.itemSet("acct_type", gsAcctType);
        wp.itemSet("id_p_seqno", gsIdPSeqno);
        wp.itemSet("corp_no", lsCorp);// 0418修改檔案匯入新增統編
        // wp.item_set("seq_no","");
        wp.itemSet("dest_amt", numToStr(gsDestAmt, "###"));
        // wp.item_set("dest_curr","");
        wp.itemSet("purchase_date", gsPurchaseDate);
        // wp.item_set("chi_desc","");
        wp.itemSet("bill_desc", gsBillDesc);
        // wp.item_set("dept_flag","");
        // wp.item_set("post_flag","");
        wp.itemSet("key_no", wp.itemStr("ex_key"));
        wp.itemSet("curr_code", gsCurrCode);
        wp.itemSet("dc_dest_amt", numToStr(gsDcDestinationAmt, "###"));
        // wp.item_set("ref_key","");
        wp.itemSet("error_code", lsError);

        if (func.dbInsert() != 1) {
          llErr++;
          continue;
        } else {
          llOk++;
        }

        // 固定長度上傳檔
        // wp.item_set("id_no",commString.mid_big5(ss,0,10));
        // wp.item_set("data_flag1",commString.mid_big5(ss,10,1));
        // wp.item_set("data_flag2",commString.mid_big5(ss,11,1));

      } catch (Exception e) {
        alertMsg("匯入資料異常!!");
        return;
      }

      // ll_cnt++;
      // int rr=ll_cnt-1;
      // this.set_rowNum(rr, ll_cnt);

    }
    // wp.listCount[0]=ll_cnt; //--->開啟上傳檔檢視
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);
    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk + ", 失敗筆數=" + llErr + ", 格式錯誤=" + llErrFormat);
    queryRead();

  }

  void delFunc() throws Exception {

    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaOpt = wp.itemBuff("opt");
    int llOk = 0, llErr = 0;

    wp.listCount[0] = aaRowid.length;


    for (int ll = 0; ll < aaRowid.length; ll++) {

      if (checkBoxOptOn(ll, aaOpt)) {
        String lsSql = "delete from bil_othexp where hex(rowid) = :rowid ";
        setString("rowid", aaRowid[ll]);
        sqlExec(lsSql);
        if (sqlRowNum <= 0) {
          wp.colSet(ll, "ok_flag", "!");
          llErr++;
          sqlCommit(0);
        } else {
          wp.colSet(ll, "ok_flag", "V");
          sqlCommit(1);
          llOk++;
        }
      }

    }
    alertMsg("刪除: 成功筆數=" + llOk + "; 失敗筆數=" + llErr + ";");


  }



}
