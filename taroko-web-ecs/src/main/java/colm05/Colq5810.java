package colm05;
/** 帳戶凍結歷史資料查詢
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 109-05-06  V1.00.01  Tanwei       updated for project coding standard
 * */
import ofcapp.BaseAction;

public class Colq5810 extends BaseAction {
  String lsAcctKey = "";

  @Override
  public void userAction() throws Exception {
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
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "colq5810")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  boolean queryBefore() {
    String lsBlockReason = "";
    if (!empty(wp.itemStr("ex_card_no"))) {
      String sql1 = " select " + " uf_idno_name(A.major_id_p_seqno) as ex_name ,"
          + " uf_corp_name(A.corp_p_seqno) as ex_name1 ,"
          // + " D.spec_status as ex_card_spec_status , "
          + " A.p_seqno , A.acno_p_seqno, " + " B.special_comment as ex_special_comment , "
          + " B.acct_status as ex_acct_status , " + " C.block_reason1 , " + " C.block_reason2 , "
          + " C.block_reason3 , " + " C.block_reason4 , " + " C.block_reason5 , "
          + " B.acct_type as ex_acct_type1 , " + " B.acct_key as ex_acct_key1 "
          + " from crd_card A, act_acno B, cca_card_acct C  "
          + " where A.acno_p_seqno =B.acno_p_seqno "
          + " and A.acno_p_seqno =C.acno_p_seqno and C.debit_flag='N' " + " and A.card_no = ? ";
      sqlSelect(sql1, new Object[] {wp.itemStr("ex_card_no")});

      String sql2 =
          " select spec_status as ex_card_spec_status from cca_card_base where card_no = ? and debit_flag ='N' ";
      sqlSelect(sql2, new Object[] {wp.itemStr("ex_card_no")});

    } else if (!empty(wp.itemStr("ex_acct_key"))) {
      String sql1 = " select " + " uf_idno_name(A.id_p_seqno) as ex_name , "
          + " uf_corp_name(A.corp_p_seqno) as ex_name1 , " + " A.p_seqno , A.acno_p_seqno, "
          + " A.special_comment as ex_special_comment , " + " A.acct_status as ex_acct_status , "
          + " C.block_reason1 , " + " C.block_reason2 , " + " C.block_reason3 , "
          + " C.block_reason4 , " + " C.block_reason5 , " + " C.spec_status  "
          + " from act_acno A, cca_card_acct C "
          + " where A.acno_p_seqno =C.acno_p_seqno and C.debit_flag='N' " + " and A.acct_key = ? "
          + " and A.acct_type = ? ";
      sqlSelect(sql1, new Object[] {lsAcctKey, wp.itemStr("ex_acct_type")});
    }
    if (sqlRowNum <= 0) {
      return false;
    }
    wp.colSet("ex_name", sqlStr("ex_name"));
    wp.colSet("ex_name1", sqlStr("ex_name1"));
    wp.colSet("ex_card_spec_status", sqlStr("ex_card_spec_status"));
    wp.colSet("ex_special_comment", sqlStr("ex_special_comment"));
    wp.colSet("ex_acct_status", sqlStr("ex_acct_status"));
    wp.colSet("ex_block_reason", sqlStr("block_reason1"));
    wp.colSet("ex_block_reason2", sqlStr("block_reason2"));
    wp.colSet("ex_block_reason3", sqlStr("block_reason3"));
    wp.colSet("ex_block_reason4", sqlStr("block_reason4"));
    wp.colSet("ex_block_reason5", sqlStr("block_reason5"));
    wp.colSet("ex_spec_status", sqlStr("spec_status"));
    wp.colSet("ex_acct_type1", sqlStr("ex_acct_type1"));
    wp.colSet("ex_acct_key1", sqlStr("ex_acct_key1"));
    if (!empty(sqlStr("block_reason1"))) {
      if (lsBlockReason.length() == 0)
        lsBlockReason += sqlStr("block_reason1");
      else
        lsBlockReason += "," + sqlStr("block_reason1");
    }

    if (!empty(sqlStr("block_reason2"))) {
      if (lsBlockReason.length() == 0)
        lsBlockReason += sqlStr("block_reason2");
      else
        lsBlockReason += "," + sqlStr("block_reason2");
    }

    if (!empty(sqlStr("block_reason3"))) {
      if (lsBlockReason.length() == 0)
        lsBlockReason += sqlStr("block_reason3");
      else
        lsBlockReason += "," + sqlStr("block_reason3");
    }

    if (!empty(sqlStr("block_reason4"))) {
      if (lsBlockReason.length() == 0)
        lsBlockReason += sqlStr("block_reason4");
      else
        lsBlockReason += "," + sqlStr("block_reason4");
    }

    if (!empty(sqlStr("block_reason5"))) {
      if (lsBlockReason.length() == 0)
        lsBlockReason += sqlStr("block_reason5");
      else
        lsBlockReason += "," + sqlStr("block_reason5");
    }

    wp.colSet("wk_block_reason", lsBlockReason);

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_acct_key")) && empty(wp.itemStr("ex_card_no"))) {
      alertErr2("帳號 , 卡號 不可同時空白 !");
      return;
    }

    if (!wp.itemEmpty("ex_acct_key")) {
      lsAcctKey = wp.itemStr("ex_acct_key");
      lsAcctKey = commString.acctKey(lsAcctKey);
      if (lsAcctKey.length() != 11) {
        errmsg("帳戶帳號輸入錯誤");
        return;
      }
    }

    if (queryBefore() == false) {
      alertErr2("卡號 OR 帳戶帳號: 輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 ";

    if (wp.itemEq("ex_log_type", "0")) {
      lsWhere += " and log_type in ('3','4','5','6') ";
    } else if (wp.itemEq("ex_log_type", "1")) {
      lsWhere += " and log_type in ('3','4','5') ";
    } else if (wp.itemEq("ex_log_type", "2")) {
      lsWhere += " and log_type ='6' ";
    }

    if (!empty(wp.itemStr("ex_card_no"))) {
//      lsWhere += sqlCol(wp.itemStr("ex_card_no"), "card_no");
    	lsWhere += " and acno_p_seqno in (select acno_p_seqno from crd_card where 1=1 " +sqlCol(wp.itemStr("ex_card_no"),"card_no")+") ";
    } else if (!empty(wp.itemStr("ex_acct_key"))) {
      lsWhere += " and acno_p_seqno in (select acno_p_seqno from act_acno where 1=1 "
    		  + sqlCol(wp.itemStr("ex_acct_key"),"acct_key")
    		  + sqlCol(wp.itemNvl("ex_acct_type", "01"),"acct_type")
    		  +")";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();



  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " log_date ," + " log_mode ," + " log_type ," + " card_no ," + " mod_user ,"
        + " mod_pgm ," + " block_reason ," + " block_reason2 ," + " block_reason3 ,"
        + " block_reason4 ," + " block_reason5 ," + " kind_flag ," + " log_reason ,"
        + " log_not_reason ," + " fit_cond ," + " acct_type," + " spec_status,"
        + " uf_acno_key(acno_p_seqno) as acct_key," + " mod_time , " + " spec_del_date , "
        + " log_remark ";
    wp.daoTable = "rsk_acnolog";
    wp.whereOrder = " order by log_date Desc, mod_time Desc, log_type Desc ";

    pageQuery();

    wp.setListCount(1);
    queryAfter();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();

  }

  void queryAfter() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (eqIgno(wp.colStr(ii, "kind_flag"), "A")) {
        wp.colSet(ii, "tt_kind_flag", ".帳戶");
      } else if (eqIgno(wp.colStr(ii, "kind_flag"), "C")) {
        wp.colSet(ii, "tt_kind_flag", ".卡號");
      }

      if (eqIgno(wp.colStr(ii, "log_mode"), "1")) {
        wp.colSet(ii, "tt_log_mode", ".人工");
      } else if (eqIgno(wp.colStr(ii, "log_mode"), "2")) {
        wp.colSet(ii, "tt_log_mode", ".批次");
      }

    }
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
