/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-10  V1.00.00  Andy Liu    program initial                           *
* 107-05-02  V1.00.01  Andy        Update                                    *
* 107-05-17  V1.00.02  Andy        Update UI                                 *
* 108-11-26  V1.00.03  Amber       check mcht_no                             *
* 108/12/04  V1.00.04  Amber       Update                                    *
* 109-01-02  V1.00.05  Ru Chen     modify AJAX                               *
* 109-04-23  V1.00.06  shiyuqi       updated for project coding standard     * 
* 110-01-06  V1.00.07  Justin          updated for XSS
* 110-01-15  V1.00.08  Justin          fix  a query bug   
* 110-01-30  V1.00.09  Justin          fix a bug
******************************************************************************/

package bilm01;


import ecsfunc.DEncryptForDB;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilm0070 extends BaseEdit {
  String kkMchtNo = "";
  String bilMerchantName = "";

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
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      // updateFunc();
      strAction = "S2";
      saveFunc();
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
      wp.colSet("loan_flag", "Y");
      wp.colSet("advance_flag", "N");
      wp.colSet("pos_flag", "N");
      wp.colSet("video_flag", "N");
      wp.colSet("mp_rate", "100");
      wp.colSet("mcht_capital", "0");
      wp.colSet("mcht_country", "TW");
      wp.colSet("mcht_state", "TW");
      wp.colSet("tx_type", "2");
    } else if (eqIgno(wp.buttonCode, "S2")) {
      strAction = "S2";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "ddl")) {
      strAction = "ddl";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      // 20200102 modify AJAX
      if ("0".equals(wp.getValue("ID_CODE"))) {
        strAction = "AJAX";
        processAjaxOption();
      } else if ("1".equals(wp.getValue("ID_CODE"))) {
        itemchanged();
      }

    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    if (wp.respHtml.indexOf("_detl") > 0) {
      if (strAction.equals("new")) {
        wp.colSet("loan_flag", "Y");
        wp.colSet("advance_flag", "N");
        wp.colSet("pos_flag", "N");
        wp.colSet("video_flag", "N");
        wp.colSet("mp_rate", "100");
        wp.colSet("mcht_capital", "0");
        wp.colSet("mcht_country", "TW");
        wp.colSet("mcht_state", "TW");
        wp.colSet("tx_type", "2");
      }
    }
    wp.sqlCmd = "SELECT max(mp_1_rate) as mp_1_rate FROM ptr_actgeneral_n ";
    sqlSelect(wp.sqlCmd);
    if (sqlRowNum > 0) {
      wp.colSet("db_mp_rate", sqlStr("mp_1_rate"));
    }
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
	  sqlParm.clear();
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_merchant")) && empty(wp.itemStr("ex_uniform_no"))) {
      alertErr("請至少輸入一項查詢條件!!");
      return false;
    }
    if (empty(wp.itemStr("ex_merchant")) == false) {
       wp.whereStr += " and mcht_no like :ex_merchant ";
       setString("ex_merchant", wp.itemStr("ex_merchant")+"%");
//      wp.whereStr += sqlCol(wp.itemStr("ex_merchant"), "mcht_no", "like%");
    }

    if (empty(wp.itemStr("ex_uniform_no")) == false) {
      wp.whereStr += " and  uniform_no = :ex_uniform_no ";
      setString("ex_uniform_no", wp.itemStr("ex_uniform_no"));
    }

    if (wp.itemStr("ex_apr_flag").equals("Y")) {
      bilMerchantName = "bil_merchant";
    } else {
      bilMerchantName = "bil_merchant_t";
    }
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
    if (getWhereStr() == false)
      return;
    // select columns
    wp.selectSQL = " mcht_no" + " , uniform_no" + " , contract_head" + " , contract_curr_no"
        + " , mcht_chi_name" + " , mcht_zip" + " , mcht_address" + " , owner_name"
        + " , stmt_inst_flag" + " , uf_2ymd(mod_time) as mod_date" + " , mod_user ";
    getWhereStr();
    wp.daoTable = bilMerchantName;
    wp.whereOrder = " order by mcht_no";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {



    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    kkMchtNo = itemKk("data_k1");
    if (empty(kkMchtNo)) {
      kkMchtNo = wp.itemStr("kk_mcht_no");
    }
    if (empty(kkMchtNo)) {
      kkMchtNo = wp.itemStr("mcht_no");
    }

    String lsSql = "select count(*) as tot_cnt from bil_merchant_t where mcht_no = ?  ";
    Object[] param = new Object[] {kkMchtNo};
    sqlSelect(lsSql, param);
    if (sqlNum("tot_cnt") > 0) {
      alertMsg("已有未覆核資料");
      bilMerchantName = "bil_merchant_t";
    } else {
      bilMerchantName = "bil_merchant";
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", mcht_no " + " , uniform_no"
        + " , forced_flag" + " , mcht_eng_name" + " , sign_date" + " , mcht_chi_name"
        + " , broken_date" + " , mcht_type" + " , loan_flag" + " , trans_flag" + " , mcht_zip"
        + " , mcht_address" + " , owner_name" + " , owner_id" + " , contract_name" + " , mcht_tel1"
        + " , mcht_tel1_1" + " , mcht_tel1_2" + " , mcht_tel2" + " , mcht_tel2_1" + " , mcht_tel2_2"
        + " , mcht_fax1" + " , mcht_fax1_1" + " , mcht_fax2" + " , mcht_fax2_1" + " , e_mail"
        + " , mcc_code" + " , bank_name" + " , assign_acct" + " , oth_bank_name" + " , oth_bank_id"
        + " , oth_bank_acct" + " , mp_rate" + " , clr_bank_id" + " , mcht_acct_name"
        + " , confirm_flag" + " , mcht_city" + " , mcht_country" + " , mcht_state"
        + " , mcht_status" + " , mcht_board_name" + " , mcht_open_addr" + " , mcht_capital"
        + " , mcht_setup_date" + " , advance_flag" + " , tx_type" + " , chain_type"
        + " , mcht_property" + " , pos_flag" + " , card_type_name" + " , video_flag"
        + " , rsecind_kind" + " , rsecind_flag" + " , gift_file_name" + " , gift_file_passwd"
        + " , gift_file_passwd as gift_file_passwd1" + " , gift_file_dir" + " , borrow_flag"
        + " , crt_user" + " , crt_date" + " , apr_user" + " , apr_date" + " , contract_head"
        + ", uf_2ymd(mod_time) as mod_date" + ", mod_user " + ", stmt_inst_flag "
        + ", installment_delay" + ", chk_online "; // 20180927 add Andy

    if (bilMerchantName.equals("bil_merchant_t")) {
      wp.selectSQL += ", 'U.更新待覆核' as apr_flag";
    } else if (bilMerchantName.equals("bil_merchant")) {
      wp.selectSQL += ", 'Y.未異動' as apr_flag";
    }

    wp.daoTable = bilMerchantName;
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  mcht_no = :mcht_no ";
    setString("mcht_no", kkMchtNo);

    pageSelect();
    if (bilMerchantName.equals("bil_merchant")) {
      wp.colSet("d_disable", "disabled");
    }
    if (sqlNotFind()) {
      alertErr("查無資料, mcht_no= " + kkMchtNo);
      return;
    }

    wp.sqlCmd = "SELECT max(mp_1_rate) as mp_1_rate FROM ptr_actgeneral_n ";
    sqlSelect(wp.sqlCmd);
    if (sqlRowNum > 0) {
      wp.colSet("db_mp_rate", sqlStr("mp_1_rate"));
    }
    wp.colSet("kk_mcht_no", kkMchtNo);

    // 20191204add 解密
    DEncryptForDB ec = new DEncryptForDB();
    // System.out.println("ec_password :"+wp.col_ss("zip_password"));
    String dcPassword = ec.decryptForDb(wp.colStr("gift_file_passwd"));
    // System.out.println("dc_password : "+dc_password);
    wp.colSet("gift_file_passwd", dcPassword);
    wp.colSet("gift_file_passwd1", dcPassword);

  }

  @Override
  public void saveFunc() throws Exception {

    Bilm0070Func func = new Bilm0070Func(wp);

    if (strAction.equals("A")) {

      // check mcht_no
      String lsSql =
          "select count(*) as ct " + "from bil_merchant " + "where mcht_no  = :mcht_no ";
      setString("mcht_no", wp.itemStr("kk_mcht_no"));

      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        if (sqlNum("ct") > 0) {
          alertErr("此特店代號資料已存在, 不可新增 !!");
          return;
        }
      }

    }

    if (strAction.equals("S2")) {
      if (wp.itemStr("apr_flag").equals("Y.未異動")) {
        strAction = "A";
      } else if (wp.itemStr("apr_flag").equals("U.更新待覆核")) {
        strAction = "U";
      }
    }

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    } else {
      alertMsg("資料處理成功!!");
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("rsecind_kind");
        this.dddwList("dddw_rsecind_kind", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1 and wf_type like 'RSECIND_KIND' order by wf_id");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("rsecind_flag");
        this.dddwList("dddw_rsecind_flag", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1 and wf_type like 'RSECIND_REASON%' order by wf_id");

      }
    } catch (Exception ex) {
    }
  }

  void validMchtno(String val) throws Exception {

    String lsSql2 = "select mcht_no " + " from bil_merchant " + " where mcht_no  = :mcht_no ";
    setString("mcht_no", val);
    sqlSelect(lsSql2);
    if (sqlRowNum > 0) {
      wp.addJSON("valid_mchtno", "此特店代號已有資料，無法再新增");
    } else {
      wp.addJSON("valid_mchtno", "");
    }

  }

  // 20200102 modify AJAX
  public int itemchanged() throws Exception {
    // super.wp = wr;
    String ajaxName = "";
    String lsRsecindKind = "";
    String lsSql = "";
    String option = "";
    String val = wp.itemStr("val");

    ajaxName = wp.itemStr("ajaxName");

    switch (ajaxName) {
      case "rsecind_flag_select":
        lsRsecindKind = "RSECIND_REASON_" + wp.itemStr("rsecind_kind");
        lsSql = "select wf_id, wf_desc " + "from ptr_sys_idtab where 1=1 ";
        lsSql += sqlCol(lsRsecindKind, "wf_type", "like%");
        lsSql += "order by wf_id ";
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          break;
        }
        option += "<option value=\"\">--</option>";
        for (int ii = 0; ii < sqlRowNum; ii++) {
          option += "<option value=\"" + sqlStr(ii, "wf_id") + "\">" + sqlStr(ii, "wf_id") + "_"
              + sqlStr(ii, "wf_desc") + "</option>";
        }
        wp.addJSON("dddw_rsecind_flag", option);
        break;
      case "kk_mcht_no":
        validMchtno(val);
        break;
    }
    return 1;
  }

  public void processAjaxOption() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    if (wp.respHtml.indexOf("_detl") > 0) {
      // System.out.println("mcht_no :"+wp.getValue("kk_mcht_no", 0) + "%");
      setString("mcht_no", wp.getValue("kk_mcht_no", 0) + "%");
    } else {
      setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }
    wp.addJSON("ex", "j_mcht_no");
    return;
  }
}
