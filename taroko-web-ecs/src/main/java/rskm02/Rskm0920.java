/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei        updated for project coding standard      *
* 109-09-23  V1.00.02  Zuwei         修改SQL Injection Issue
* 109-12-23  V1.00.03  Justin        parameterize sql
* 109-12-31  V1.00.03  shiyuqi       修改无意义命名                                                                                      *    
* 110-02-01  V1.00.04  Justin        fix a bug 
******************************************************************************/
package rskm02;
/** 線上持卡人信用額度調整
 * 2019-1212:  Alex  dataRead fix
 * 2019-0814   JH    xx萬元簡訊
 * 2019-0627:  JH    modify
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 *  V.2018-0625.JH
* 109-12-30  V1.00.01  shiyuqi       修改无意义命名
 * 2024-0103   JH    corp_no不補0
 * */
import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Rskm0920 extends BaseAction {
  String acctType = "", acctKey = "";
  String isPSeqno = "", isCardNo = "", isAcctKey = "", isEmendType = "", icAcctType = "",
      lsWhere = "";

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      keepData();
      clearFunc();
      newData();
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
	sqlParm.clear();
    try {
      if (eqIgno(wp.respHtml, "rskm0920")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");

        wp.optionKey = wp.colStr(0, "ex_mod_user");
        dddwList("dddw_sec_user", "sec_user", "usr_id", "usr_id||'_'||usr_cname", "where 1=1");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "rskm0920_detl")) {
        if (empty(isEmendType))
          isEmendType = wp.itemStr2("emend_type");
        wp.optionKey = wp.colStr(0, "log_reason_up");
        if (eqIgno(isEmendType, "2") || eqIgno(isEmendType, "3")) {
          dddwList("dddw_reason_up", "ptr_sys_idtab", "wf_id", "wf_desc",
              "where wf_type='ADJ_REASON_UP' and id_code='2'");
        } else {
          setString(isEmendType);
          dddwList("dddw_reason_up", "ptr_sys_idtab", "wf_id", "wf_desc",
              "where wf_type='ADJ_REASON_UP' and id_code= ? ");
        }

      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "rskm0920_detl")) {
        if (empty(isEmendType))
          isEmendType = wp.itemStr2("emend_type");
        wp.optionKey = wp.colStr(0, "log_reason_down");
        if (eqIgno(isEmendType, "2") || eqIgno(isEmendType, "3")) {
          dddwList("dddw_reason_down", "ptr_sys_idtab", "wf_id", "wf_desc",
              "where wf_type='ADJ_REASON_DOWN' and id_code='2'");
        } else {
        	setString(isEmendType);
          dddwList("dddw_reason_down", "ptr_sys_idtab", "wf_id", "wf_desc",
              "where wf_type='ADJ_REASON_DOWN' and id_code= ? ");
        }

        wp.optionKey = wp.colStr(0, "kk_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");

        // --簡訊
        wp.optionKey = wp.colStr(0, "sms_flag");
        String[] aaCde = null, aaDesc = null;
        if (eqIgno(isEmendType, "5")) {
          aaCde = new String[] {"5"};
          aaDesc = new String[] {"預借現金額度調整"};
        } else {
          aaCde = new String[] {"1", "6"};
          aaDesc = new String[] {"調整額度", "覆審降額"};
        }
        wp.colSet("dddw_sms_flag", this.ddlbOption(aaCde, aaDesc));

      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (!empty(wp.itemStr("ex_acct_key"))) {
      if (selectPseqnoByAcctKey(wp.itemStr("ex_acct_key"), wp.itemStr("ex_acct_type")) == false) {
        alertErr2("帳戶帳號 輸入錯誤 !");
        return;
      }
    } else if (!empty(wp.itemStr("ex_card_no"))) {
      if (selectPseqnoByCardNo(wp.itemStr("ex_card_no")) == false) {
        alertErr2("卡號 輸入錯誤 !");
        return;
      }
    }

    if (this.chkStrend(wp.itemStr("ex_log_date1"), wp.itemStr("ex_log_date2")) == false) {
      alertErr2("登錄日期: 起迄錯誤!");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_apr_date1"), wp.itemStr("ex_apr_date2")) == false) {
      alertErr2("覆核日期: 起迄錯誤!");
      return;
    }

    lsWhere = " where log_type='1' and log_mode='1'  "
        + " and emend_type not in ('','4') and kind_flag ='A' "
        + sqlCol(wp.itemStr("ex_log_date1"), "log_date", ">=")
        + sqlCol(wp.itemStr("ex_log_date2"), "log_date", "<=") 
        + sqlCol(isPSeqno, "acno_p_seqno")
        + sqlCol(wp.itemStr("ex_mod_user"), "mod_user")
        + sqlCol(wp.itemStr("ex_emend_type"), "emend_type");

    if (wp.itemEq("ex_apr_flag", "Y")) {
      if (itemallEmpty("ex_apr_date1,ex_apr_date2,ex_log_date1,ex_log_date2".split(","))) {
        alertErr2("覆核日期、登錄日期: 不可皆為空白");
        return;
      }
      lsWhere += " and apr_flag ='Y' " 
          + sqlCol(wp.itemStr("ex_apr_date1"), "apr_date", ">=")
          + sqlCol(wp.itemStr("ex_apr_date2"), "apr_date", "<=");
    } else if (wp.itemEq("ex_apr_flag", "N")) {
      if (itemallEmpty("ex_apr_date1,ex_apr_date22".split(",")) == false) {
        alertErr2("覆核日期: 不可有值");
        return;
      }
      lsWhere += " and apr_flag ='N' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    sqlParm.setSqlParmNoClear(true);
    querySum(lsWhere);

    queryRead();
  }

  boolean selectPseqnoByCardNo(String lsCardNo) {
    String sql1 =
        "select acno_p_seqno as is_p_seqno from crd_card " + " where card_no =? and sup_flag ='0' ";
    sqlSelect(sql1, new Object[] {lsCardNo});
    if (sqlRowNum > 0) {
      isPSeqno = sqlStr("is_p_seqno");
      return true;
    }
    return false;
  }

  boolean selectPseqnoByAcctKey(String lsAcctKey, String lsAcctType) {
    String wkAcctKey =lsAcctKey;
    if (lsAcctKey.length() !=8) {
       wkAcctKey = commString.acctKey(lsAcctKey);
    }
    if (wkAcctKey.length() != 11 && wkAcctKey.length() != 8)    return false;
    if (empty(lsAcctType))
      lsAcctType = "01";

    String sql1 = "select acno_p_seqno as is_p_seqno from act_acno " + " where acct_key =?"
        + " and acct_type= ?";
    sqlSelect(sql1, new Object[] {wkAcctKey, lsAcctType});
    if (sqlRowNum > 0) {
      isPSeqno = sqlStr("is_p_seqno");
      return true;
    }
    return false;
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(A.rowid) as rowid ," + " A.emend_type , " + " A.acct_type , "
        + " uf_acno_key(A.acno_p_seqno) as acct_key , " + " A.card_no , " + " A.bef_loc_amt , "
        + " A.aft_loc_amt , " + " A.log_reason , " + " A.mod_user , " + " A.log_date , "
        + " A.apr_flag , " + " A.kind_flag , " + " A.acno_p_seqno , " + " A.id_p_seqno , "
        + " A.corp_p_seqno , " + " A.log_mode , " + " A.log_type , " + " A.adj_loc_flag , "
        + " decode(A.adj_loc_flag,'1','高','2','低') as tt_adj_loc_flag , " + " A.apr_user , "
        + " A.apr_date , " + " A.fh_flag , " + " A.bef_loc_cash , " + " A.aft_loc_cash , "
        + " decode(A.emend_type,'5', A.bef_loc_cash, A.bef_loc_amt) as db_bef_amt , "
        + " decode(A.emend_type,'5', A.aft_loc_cash, A.aft_loc_amt) as db_aft_amt , "
        + " uf_acno_name(A.acno_p_seqno) as db_idno_name," + " '' as db_son_card_flag , "
        + " uf_idno_id(A.id_p_seqno) as db_idno , " + " 0 as db_bef_cash , "
        + " 0 as db_bef_card , " + " 0 as db_upper , " + " 0 as db_low ,"
        + " decode(A.adj_loc_flag,'1',(select wf_desc from ptr_sys_idtab where wf_type='ADJ_REASON_UP' and wf_id = A.log_reason),'2',(select wf_desc from ptr_sys_idtab where wf_type='ADJ_REASON_DOWN' and wf_id = A.log_reason)) as tt_log_reason , "
        + " A.sms_flag ";
    wp.daoTable = "rsk_acnolog A";
    wp.whereOrder = " order by A.log_date, A.mod_seqno ";
    logSql();
    pageQuery();

    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    listWkData();
    // queryAfter();
    wp.setPageValue();

  }

  void listWkData() {
    String[] aaCde = "1,5,6,Y,N".split(",");
    String[] aaTxt = new String[] {"調整額度", "預借現金額度調整", "覆審降額", "", ""};
    String sql1 = " select id_no from crd_idno where id_p_seqno = ? ";
    String sql2 =
        " select count(*) as db_line from mkt_line_cust where id_no = ? and status_code = '0' ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (eqIgno(wp.colStr(ii, "emend_type"), "4")) {
        selectLocAmt(wp.colStr(ii, "acno_p_seqno"), ii);
      }
      wp.colSet(ii, "db_bef_cash", wp.colStr(ii, "bef_loc_cash"));
      if (wp.colNum(ii, "db_aft_amt") >= wp.colNum(ii, "db_bef_amt")) {
        wp.colSet(ii, "db_upper", wp.colNum(ii, "db_aft_amt") - wp.colNum(ii, "db_bef_amt"));
        wp.colSet(ii, "db_lower", "0");
      } else {
        wp.colSet(ii, "db_upper", "0");
        wp.colSet(ii, "db_lower", wp.colNum(ii, "db_bef_amt") - wp.colNum(ii, "db_aft_amt"));
      }
      // --
      String smsFlag = commString.decode(wp.colStr(ii, "sms_flag"), aaCde, aaTxt);
      wp.colSet(ii, "tt_sms_flag", smsFlag);

      // --check line 推播
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "id_p_seqno")});
      if (sqlRowNum > 0) {
        sqlSelect(sql2, new Object[] {sqlStr("id_no")});
        if (sqlNum("db_line") > 0)
          wp.colSet(ii, "wk_line", "Y");
        else
          wp.colSet(ii, "wk_line", "N");
      } else {
        wp.colSet(ii, "wk_line", "N");
      }
    }
  }

  void querySum(String aWhere) throws Exception {

    String sql1 = " select " + " count(*) as db_cnt , "
        + " sum(decode(adj_loc_flag,'1',aft_loc_amt-bef_loc_amt,0)) as tl_upper , "
        + " sum(decode(adj_loc_flag,'2',bef_loc_amt-aft_loc_amt,0)) as tl_lower "
        + " from rsk_acnolog  " + aWhere;
    sqlSelect(sql1);

    wp.colSet("tl_upper", "" + sqlNum("tl_upper"));
    wp.colSet("tl_lower", "" + sqlNum("tl_lower"));
    wp.colSet("db_cnt", "" + sqlNum("db_cnt"));
  }

  void selectLocAmt(String lsPSeqno, int rr) {
    String sql1 =
        "select line_of_credit_amt as db_bef_amt from act_acno " + " where acno_p_seqno =?";
    sqlSelect(sql1, new Object[] {lsPSeqno});
    if (sqlRowNum > 0) {
      wp.colSet(rr, "db_bef_amt", sqlStr("db_bef_amt"));
      return;
    }
    return;
  }

  @Override
  public void querySelect() throws Exception {
    acctType = wp.itemStr("data_k1");
    acctKey = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.logSql = true;
    isEmendType = wp.itemStr2("emend_type");
    if (wp.itemEmpty("kk_acct_key") == false || wp.itemEmpty("kk_card_no") == false) {

      if (wp.itemEmpty("kk_acct_key") == false) {
        acctType = wp.itemNvl("kk_acct_type", "01");
        acctKey = wp.itemStr2("kk_acct_key");
        if (acctKey.length() !=8) {
           acctKey =commString.acctKey(acctKey);
        }
        if (acctKey.length() != 11 && acctKey.length() !=8) {
          clearFunc();
          wp.colSet("emend_type", isEmendType);
          alertErr2("帳戶帳號輸入錯誤");
          return;
        }

        if (selectPseqnoByAcctKey(acctKey, acctType) == false) {
          clearFunc();
          wp.colSet("emend_type", isEmendType);
          wp.colSet("kk_acct_type", acctType);
          wp.colSet("kk_acct_key", acctKey);
          alertErr2("帳戶帳號 輸入錯誤 !");
          return;
        }
      } else if (wp.itemEmpty("kk_card_no") == false) {
        if (selectPseqnoByCardNo(wp.itemStr2("kk_card_no")) == false) {
          String kk_card_no = wp.itemStr2("kk_card_no");
          clearFunc();
          wp.colSet("emend_type", isEmendType);
          wp.colSet("kk_card_no", kk_card_no);
          alertErr2("卡號 輸入錯誤 !");
          return;
        }
      }

      clearFunc();

      wp.selectSQL = "" + " hex(rowid) as rowid , mod_seqno , " + " emend_type ," + " acct_type ,"
          + " uf_acno_key(acno_p_seqno) as acct_key ," + " card_no ," + " bef_loc_amt ,"
          + " aft_loc_amt ," + " log_reason ," + " mod_user ," + " log_date ," + " apr_flag ,"
          + " to_char(mod_time,'yyyymmdd') as mod_date ," + " kind_flag ," + " acno_p_seqno ,"
          + " id_p_seqno ," + " corp_p_seqno ," + " log_mode ," + " log_type ," + " adj_loc_flag ,"
          + " fit_cond ," + " security_amt ," + " apr_user ," + " apr_date ," + " fh_flag ,"
          + " bef_loc_cash ," + " aft_loc_cash ," + " sms_flag ,"
          + " uf_acno_name(acno_p_seqno) as db_chi_name," + " '' as db_son_card_flag,"
          + " uf_idno_id(id_p_seqno) as db_idno,"
          + " decode(emend_type,'5', aft_loc_cash, aft_loc_amt) as db_aft_amt , "
          + " 0 as db_bef_amt," + " 0 as db_bef_cash," + " 0 as db_bef_card," + " 0 as db_upper,"
          + " 0 as db_low ";
      wp.daoTable = " rsk_acnolog ";
      wp.whereStr =
          " where log_type='1' and log_mode='1' and emend_type not in ('','4') and kind_flag ='A' and apr_flag ='N' "
              + " and emend_type = ? and acno_p_seqno = ?";
      pageSelect(new Object[] {isEmendType, isPSeqno});

      if (sqlRowNum <= 0) {
        selectOK();
        if (eqIgno(isEmendType, "1") || eqIgno(isEmendType, "2")
            || eqIgno(isEmendType, "3")) {
          log("A:" + isPSeqno);
          String sql1 =
              "select line_of_credit_amt as wk_bef_amt from act_acno " + " where acno_p_seqno =?";
          sqlSelect(sql1, new Object[] {isPSeqno});
          if (sqlRowNum > 0) {
            log("amt:" + sqlStr("wk_bef_amt"));
            wp.colSet("wk_bef_amt", sqlStr("wk_bef_amt"));
          } else {
            wp.colSet("wk_bef_amt", "0");
          }
        } else if (eqIgno(isEmendType, "5")) {
          String sql2 = "select line_of_credit_amt_cash as wk_bef_amt from act_acno "
              + " where acno_p_seqno =?";
          sqlSelect(sql2, new Object[] {isPSeqno});
          if (sqlRowNum > 0) {
            wp.colSet("wk_bef_amt", sqlStr("wk_bef_amt"));
          } else {
            wp.colSet("wk_bef_amt", "0");
          }
        } else if (eqIgno(isEmendType, "4")) {
          String sql3 = "select indiv_crd_lmt as wk_bef_amt from crd_card "
              + " where acno_p_seqno =?" + commSqlStr.rownum(1);
          sqlSelect(sql3, new Object[] {isPSeqno});
          if (sqlRowNum > 0) {
            wp.colSet("wk_bef_amt", sqlStr("wk_bef_amt"));
          } else {
            wp.colSet("wk_bef_amt", "0");
          }
        }
        // **--chi_name , acct_type , acct_key , id_p_seqno , corp_p_seqno
        String sql4 = "select " + " acct_type ," + " acct_key ," + " id_p_seqno ,"
            + " corp_p_seqno ," + " uf_acno_name(p_seqno) as chi_name " + " from act_acno "
            + " where acno_p_seqno =?";
        sqlSelect(sql4, new Object[] {isPSeqno});
        if (sqlRowNum > 0) {
          wp.colSet("acct_key", sqlStr("acct_key"));
          wp.colSet("acct_type", sqlStr("acct_type"));
          wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
          wp.colSet("corp_p_seqno", sqlStr("corp_p_seqno"));
          wp.colSet("db_chi_name", sqlStr("chi_name"));
        }

        String sql5 =
            "select card_no " + " from crd_card " + " where acno_p_seqno =?" + commSqlStr.rownum(1);
        sqlSelect(sql5, new Object[] {isPSeqno});
        if (sqlRowNum > 0) {
          wp.colSet("card_no", sqlStr("card_no"));
        }
        wp.colSet("emend_type", isEmendType);
        wp.colSet("acno_p_seqno", isPSeqno);

        // **--db_bef

        String sql6 = "select " + " line_of_credit_amt as bef_loc_amt ,"
            + " line_of_credit_amt_cash as bef_loc_cash " + " from act_acno "
            + " where acno_p_seqno =?";

        sqlSelect(sql6, new Object[] {isPSeqno});

        if (sqlRowNum > 0) {
          wp.colSet("db_bef_amt", sqlStr("bef_loc_amt"));
          wp.colSet("db_bef_card", sqlStr("bef_loc_amt"));
          wp.colSet("db_bef_cash", sqlStr("bef_loc_cash"));
        }

        if (eqIgno(isEmendType, "1") || eqIgno(isEmendType, "5")) {
          wp.colSet("card_no", "");
        }
      } else {
        alertMsg("此筆資料待覆核....");
        isEmendType = wp.colStr("emend_type");
        dataAfter();
      }
    } else {
      if (empty(acctType))
        acctType = wp.itemStr2("rowid");
      wp.selectSQL = "" + " hex(rowid) as rowid , mod_seqno , " + " emend_type ," + " acct_type ,"
          + " uf_acno_key(acno_p_seqno) as acct_key ," + " card_no ," + " bef_loc_amt ,"
          + " aft_loc_amt ," + " log_reason ," + " mod_user ," + " log_date ," + " apr_flag ,"
          + " to_char(mod_time,'yyyymmdd') as mod_date ," + " kind_flag ," + " acno_p_seqno ,"
          + " id_p_seqno ," + " corp_p_seqno ," + " log_mode ," + " log_type ," + " adj_loc_flag ,"
          + " fit_cond ," + " security_amt ," + " apr_user ," + " apr_date ," + " fh_flag ,"
          + " bef_loc_cash ," + " aft_loc_cash ," + " sms_flag ,"
          + " uf_acno_name(acno_p_seqno) as db_chi_name," + " '' as db_son_card_flag,"
          + " uf_idno_id(id_p_seqno) as db_idno,"
          + " decode(emend_type,'5', aft_loc_cash, aft_loc_amt) as db_aft_amt , "
          + " 0 as db_bef_amt," + " 0 as db_bef_cash," + " 0 as db_bef_card," + " 0 as db_upper,"
          + " 0 as db_low ";
      wp.daoTable = " rsk_acnolog ";
      wp.whereStr = " where 1=1 and rowid = ? ";
      setRowid(1,acctType);

      pageSelect();
      if (sqlNotFind()) {
        alertErr("查無資料, key=" + acctType);
        return;
      }
      isEmendType = wp.colStr("emend_type");
      if (wp.colEq("apr_flag", "N"))
        alertMsg("此筆資料待覆核....");
      dataAfter();
    }

    selectLine(wp.colStr("id_p_seqno"));

  }

  void selectLine(String aIdPSeqno) {

    String sql1 = " select uf_idno_id(?) as id_no from dual ";
    sqlSelect(sql1, new Object[] {aIdPSeqno});
    if (sqlRowNum <= 0) {
      return;
    }

    String lsIdNo = sqlStr("id_no");

    String sql2 =
        "select count(*) as db_cnt from mkt_line_cust where id_no = ? and status_code = '0' ";
    sqlSelect(sql2, new Object[] {lsIdNo});
    if (sqlNum("db_cnt") > 0) {
      wp.colSet("wk_line", "Y");
      return;
    }

    wp.colSet("wk_line", "N");
  }

  void dataAfter() {
    if (eqIgno(isEmendType, "1") || eqIgno(isEmendType, "2") || eqIgno(isEmendType, "3")) {
      wp.colSet("wk_bef_amt", wp.colStr("bef_loc_amt"));
    } else if (eqIgno(isEmendType, "5")) {
      wp.colSet("wk_bef_amt", wp.colStr("bef_loc_cash"));
    } else if (eqIgno(isEmendType, "4")) {
      wp.colSet("wk_bef_amt", wp.colStr("bef_loc_amt"));
    }

    wp.colSet("db_bef_amt", wp.colStr("bef_loc_amt"));
    wp.colSet("db_bef_card", "0");
    if (eqIgno(isEmendType, "1")) {
      String sql1 =
          "select line_of_credit_amt as ldc_credit_amt from act_acno " + " where acno_p_seqno =?";
      sqlSelect(sql1, new Object[] {wp.colStr("acno_p_seqno")});
      if (sqlRowNum > 0)
        wp.colSet("db_bef_amt", sqlStr("ldc_credit_amt"));
    }
    wp.colSet("db_bef_cash", wp.colStr("bef_loc_cash"));
    if (wp.colNum(0, "db_aft_amt") > wp.colNum(0, "wk_bef_amt")) {
      wp.colSet("db_upper", "" + (wp.colNum(0, "db_aft_amt") - wp.colNum(0, "wk_bef_amt")));
      wp.colSet("db_low", "0");
      wp.colSet("log_reason_up", wp.colStr("log_reason"));
    } else {
      wp.colSet("db_low", "" + (wp.colNum(0, "wk_bef_amt") - wp.colNum(0, "db_aft_amt")));
      wp.colSet("db_upper", "0");
      wp.colSet("log_reason_down", wp.colStr("log_reason"));
    }

    wp.colSet("kk_card_no", "");
    wp.colSet("kk_acct_key", "");
    wp.colSet("kk_acct_type", "01");

  }

  @Override
  public void saveFunc() throws Exception {
    isEmendType = wp.itemStr("emend_type");
    rskm02.Rskm0920Func func = new rskm02.Rskm0920Func();
    func.setConn(wp);

    if (wp.itemEq("apr_flag", "Y")) {
      alertErr2("此筆資料已覆核 , 不可異動");
      return;
    }

    if (isAdd() || isUpdate()) {
      // if(wp.item_eq("sms_flag", "Y") && wp.item_num("db_aft_amt")%10000!=0){
      // err_alert("額度仟元以下不發送簡訊，請人工發送");
      // return ;
      // }

      String sql1 = "";
      if (wp.itemEq("emend_type", "4")) {
        sql1 = " select count(*) as db_cnt from rsk_acnolog "
            + " where card_no = ? and log_type='1' and log_mode='1' and kind_flag='C' "
            + " and emend_type <> '' " + " and apr_flag in ('','N') ";
        sqlSelect(sql1, new Object[] {wp.itemStr2("card_no")});
      } else {
        sql1 = " select count(*) as db_cnt from rsk_acnolog " + " where acno_p_seqno = ? "
            + " and log_type='1' and log_mode='1' and kind_flag='A' " + " and emend_type <> '' "
            + " and apr_flag in ('','N') ";
        sqlSelect(sql1, new Object[] {wp.itemStr2("acno_p_seqno")});
      }

      if (sqlNum("db_cnt") != 0) {
        if (sqlNum("db_cnt") == 1 && wp.itemEmpty("rowid")) {
          alertErr2("此帳戶/卡片 已有登錄，不可重覆登錄");
          return;
        }
        if (wp.itemEmpty("rowid") == false && sqlNum("db_cnt") > 1) {
          alertErr2("此帳戶/卡片 已有登錄，不可重覆登錄");
          return;
        }
      }

    }
    wp.log("A:"+wp.itemStr("acno_p_seqno"));
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    } else
      this.saveAfter(false);

    if (isUpdate() && rc ==1)
      clearFunc();

    if (isAdd() || isUpdate()) {
      wp.colSet("emend_type", isEmendType);
    }

  }



  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    btnModeAud();

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  void newData() throws Exception {
    if (!empty(isAcctKey)) {
      if (selectPseqnoByAcctKey(isAcctKey, icAcctType) == false) {
        alertErr2("帳戶帳號 輸入錯誤 !");
        return;
      }
    } else if (!empty(isCardNo)) {
      if (selectPseqnoByCardNo(isCardNo) == false) {
        alertErr2("卡號 輸入錯誤 !");
        return;
      }
    } else if (empty(isAcctKey) && empty(isCardNo)) {
      alertErr2("帳戶帳號,卡號 不可同時空白 !");
      return;
    }

    if (empty(isEmendType)) {
      alertErr2("修改方式: 不可空白");
      return;
    }

    // --若已有待覆核資料直接讀取待覆核資料
    String sqlN =
        "select hex(rowid) as rowid from rsk_acnolog where log_type='1' and log_mode='1' and emend_type not in ('','4') and kind_flag ='A' and apr_flag ='N' "
            + " and emend_type = ? and acno_p_seqno = ?";

    sqlSelect(sqlN, new Object[] {isEmendType, isPSeqno});

    if (sqlRowNum > 0 && empty(sqlStr("rowid")) == false) {
      acctType = sqlStr("rowid");
      dataRead();
      alertMsg("此筆資料待覆核....");
    } else {
      // **--wk_bef_amt
      if (eqIgno(isEmendType, "1") || eqIgno(isEmendType, "2") || eqIgno(isEmendType, "3")) {
        String sql1 =
            "select line_of_credit_amt as wk_bef_amt from act_acno " + " where acno_p_seqno =?";
        sqlSelect(sql1, new Object[] {isPSeqno});
        if (sqlRowNum > 0) {
          wp.colSet("wk_bef_amt", sqlStr("wk_bef_amt"));
        } else {
          wp.colSet("wk_bef_amt", "0");
        }
      } else if (eqIgno(isEmendType, "5")) {
        String sql2 = "select line_of_credit_amt_cash as wk_bef_amt from act_acno "
            + " where acno_p_seqno =?";
        sqlSelect(sql2, new Object[] {isPSeqno});
        if (sqlRowNum > 0) {
          wp.colSet("wk_bef_amt", sqlStr("wk_bef_amt"));
        } else {
          wp.colSet("wk_bef_amt", "0");
        }
      } else if (eqIgno(isEmendType, "4")) {
        String sql3 = "select indiv_crd_lmt as wk_bef_amt from crd_card " + " where acno_p_seqno =?"
            + commSqlStr.rownum(1);
        sqlSelect(sql3, new Object[] {isPSeqno});
        if (sqlRowNum > 0) {
          wp.colSet("wk_bef_amt", sqlStr("wk_bef_amt"));
        } else {
          wp.colSet("wk_bef_amt", "0");
        }
      }
      // **--chi_name , acct_type , acct_key , id_p_seqno , corp_p_seqno
      String sql4 = "select " + " acct_type ," + " acct_key ," + " id_p_seqno ," + " corp_p_seqno ,"
          + " uf_acno_name(acno_p_seqno) as chi_name " + " from act_acno "
          + " where acno_p_seqno =?";
      sqlSelect(sql4, new Object[] {isPSeqno});
      if (sqlRowNum > 0) {
        wp.colSet("acct_key", sqlStr("acct_key"));
        wp.colSet("acct_type", sqlStr("acct_type"));
        wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
        wp.colSet("corp_p_seqno", sqlStr("corp_p_seqno"));
        wp.colSet("db_chi_name", sqlStr("chi_name"));
      }

      String sql5 = "select " + " card_no " + " from crd_card " + " where acno_p_seqno =?"
          + " order by issue_date desc" + commSqlStr.rownum(1);
      sqlSelect(sql5, new Object[] {isPSeqno});
      if (sqlRowNum > 0) {
        wp.colSet("card_no", sqlStr("card_no"));
      }
      wp.colSet("emend_type", isEmendType);
      wp.colSet("acno_p_seqno", isPSeqno);

      // **--db_bef

      String sql6 = "select " + " line_of_credit_amt as bef_loc_amt ,"
          + " line_of_credit_amt_cash as bef_loc_cash " + " from act_acno "
          + " where acno_p_seqno =?";
      sqlSelect(sql6, new Object[] {isPSeqno});

      if (sqlRowNum > 0) {
        wp.colSet("db_bef_amt", sqlStr("bef_loc_amt"));
        wp.colSet("db_bef_card", sqlStr("bef_loc_amt"));
        wp.colSet("db_bef_cash", sqlStr("bef_loc_cash"));
      }

      if (eqIgno(isEmendType, "1") || eqIgno(isEmendType, "5")) {
        wp.colSet("card_no", "");
      }
    }

    // --Line推播
    String sql7 = " select id_no from crd_idno where id_p_seqno = ? ";
    String sql8 =
        " select count(*) as db_line from mkt_line_cust where id_no = ? and status_code = '0' ";

    sqlSelect(sql7, new Object[] {wp.colStr("id_p_seqno")});
    if (sqlRowNum > 0) {
      sqlSelect(sql8, new Object[] {sqlStr("id_no")});
      if (sqlNum("db_line") > 0)
        wp.colSet("wk_line", "Y");
      else
        wp.colSet("wk_line", "N");
    }

  }

  void keepData() {
    isCardNo = wp.itemStr("ex_card_no");
    isAcctKey = wp.itemStr("ex_acct_key");
    isEmendType = wp.itemStr("ex_emend_type");
    icAcctType = wp.itemStr("ex_acct_type");

    if (eqIgno(isEmendType, "1")) {
      if (empty(isCardNo) && empty(isAcctKey)) {
        alertErr2("卡號,帳務帳號  不可同時空白 !");
        return;
      }
    } else if (eqIgno(isEmendType, "2")) {
      if (!empty(isCardNo) || empty(isAcctKey)) {
        alertErr2("只可輸入帳戶帳號且帳戶帳號不可空白");
        return;
      }
    } else if (eqIgno(isEmendType, "3")) {
      if (!empty(isAcctKey)) {
        alertErr2("只可輸入卡號");
        return;
      }
    } else if (eqIgno(isEmendType, "5")) {
      if (empty(isCardNo) && empty(isAcctKey)) {
        alertErr2("卡號,帳務帳號  不可同時空白 !");
        return;
      }
    }

  }

  // ----------------------------------------------------------------------------------
  public void wfAjaxKey(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =
    seleceInitDataByAcctKey(wp.itemStr("ax_type"), wp.itemStr("ax_key"));
    if (rc != 1) {
      wp.addJSON("acct_type", "");
      wp.addJSON("acct_key", "");
      wp.addJSON("card_no", "");
      wp.addJSON("db_chi_name", "");
      wp.addJSON("wk_bef_amt", "");
      wp.addJSON("acno_p_seqno", "");
      wp.addJSON("id_p_seqno", "");
      wp.addJSON("corp_p_seqno", "");
      wp.addJSON("db_bef_amt", "");
      wp.addJSON("db_bef_card", "");
      wp.addJSON("db_bef_cash", "");
      return;
    }
    wp.addJSON("acct_type", sqlStr("acct_type"));
    wp.addJSON("acct_key", sqlStr("acct_key"));
    wp.addJSON("card_no", sqlStr("card_no"));
    wp.addJSON("db_chi_name", sqlStr("db_chi_name"));
    wp.addJSON("wk_bef_amt", sqlStr("wk_bef_amt"));
    wp.addJSON("acno_p_seqno", sqlStr("acno_p_seqno"));
    wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));
    wp.addJSON("corp_p_seqno", sqlStr("corp_p_seqno"));
    wp.addJSON("db_bef_amt", sqlStr("bef_loc_amt"));
    wp.addJSON("db_bef_card", sqlStr("bef_loc_amt"));
    wp.addJSON("db_bef_cash", sqlStr("bef_loc_cash"));

  }

  public void wfAjaxCard(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =
    seleceInitDataByCardno(wp.itemStr("ax_card"));
    if (rc != 1) {
      wp.addJSON("acct_type", "");
      wp.addJSON("acct_key", "");
      wp.addJSON("card_no", "");
      wp.addJSON("db_chi_name", "");
      wp.addJSON("wk_bef_amt", "");
      wp.addJSON("acno_p_seqno", "");
      wp.addJSON("id_p_seqno", "");
      wp.addJSON("corp_p_seqno", "");
      wp.addJSON("db_bef_amt", "");
      wp.addJSON("db_bef_card", "");
      wp.addJSON("db_bef_cash", "");
      return;
    }
    wp.addJSON("acct_type", sqlStr("acct_type"));
    wp.addJSON("acct_key", sqlStr("acct_key"));
    wp.addJSON("card_no", sqlStr("card_no"));
    wp.addJSON("db_chi_name", sqlStr("db_chi_name"));
    wp.addJSON("wk_bef_amt", sqlStr("wk_bef_amt"));
    wp.addJSON("acno_p_seqno", sqlStr("acno_p_seqno"));
    wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));
    wp.addJSON("corp_p_seqno", sqlStr("corp_p_seqno"));
    wp.addJSON("db_bef_amt", sqlStr("bef_loc_amt"));
    wp.addJSON("db_bef_card", sqlStr("bef_loc_amt"));
    wp.addJSON("db_bef_cash", sqlStr("bef_loc_cash"));
  }

  void seleceInitDataByAcctKey(String acctType, String acctKey) {
    String wkAcctKey = "", isEmendType = "";
    wkAcctKey = acctKey;
    if (wkAcctKey.length() !=8) {
       wkAcctKey =commString.acctKey(acctKey);
    }
    if (wkAcctKey.length() != 11 && wkAcctKey.length() !=8) {
      alertErr2("帳戶帳號至少輸入 8 碼 !");
      return;
    }
    if (empty(acctType))
      acctType = "01";

    String sql1 = "select acno_p_seqno from act_acno " + " where acct_key =?" + " and acct_type= ?";
    sqlSelect(sql1, new Object[] {wkAcctKey, acctType});
    if (sqlRowNum <= 0) {
      log("A");
      alertErr2("查無資料: 帳戶類別:" + acctType + " 帳戶帳號:" + wkAcctKey);
      return;
    }

    isEmendType = wp.itemStr("ax_emend_type");

    if (eqIgno(isEmendType, "1") || eqIgno(isEmendType, "2") || eqIgno(isEmendType, "3")) {
      String sql2 =
          "select line_of_credit_amt as wk_bef_amt from act_acno " + " where acno_p_seqno =?";
      sqlSelect(sql2, new Object[] {sqlStr("acno_p_seqno")});
      if (sqlRowNum <= 0) {
        log("B");
        alertErr2("查無資料: 帳戶類別:" + acctType + " 帳戶帳號:" + wkAcctKey);
        return;
      }
    } else if (eqIgno(isEmendType, "5")) {
      String sql2 =
          "select line_of_credit_amt_cash as wk_bef_amt from act_acno " + " where acno_p_seqno =?";
      sqlSelect(sql2, new Object[] {sqlStr("acno_p_seqno")});
      if (sqlRowNum <= 0) {
        alertErr2("查無資料: 帳戶類別:" + acctType + " 帳戶帳號:" + wkAcctKey);
        return;
      }
    }
    log("line_of_credit_amt:" + sqlStr("wk_bef_amt"));
    // **--chi_name , acct_type , acct_key , id_p_seqno , corp_p_seqno
    String sql4 = "select " + " acct_type ," + " acct_key ," + " id_p_seqno ," + " corp_p_seqno ,"
        + " uf_acno_name(acno_p_seqno) as db_chi_name " + " from act_acno "
        + " where acno_p_seqno =?";
    sqlSelect(sql4, new Object[] {sqlStr("acno_p_seqno")});
    if (sqlRowNum <= 0) {
      log("C");
      alertErr2("查無資料: 帳戶類別:" + acctType + " 帳戶帳號:" + wkAcctKey);
      return;
    }

    String sql5 = "select card_no " + " from crd_card " + " where acno_p_seqno =?"
        + " order by issue_date desc" + commSqlStr.rownum(1);
    sqlSelect(sql5, new Object[] {sqlStr("acno_p_seqno")});
    if (sqlRowNum <= 0) {
      log("D");
      alertErr2("查無資料: 帳戶類別:" + acctType + " 帳戶帳號:" + wkAcctKey);
      return;
    }
    // **--db_bef

    String sql6 = "select " + " line_of_credit_amt as bef_loc_amt ,"
        + " line_of_credit_amt_cash as bef_loc_cash " + " from act_acno "
        + " where acno_p_seqno =?";
    sqlSelect(sql6, new Object[] {sqlStr("acno_p_seqno")});

    if (sqlRowNum <= 0) {
      log("E");
      alertErr2("查無資料: 帳戶類別:" + acctType + " 帳戶帳號:" + wkAcctKey);
      return;
    }

  }

  void seleceInitDataByCardno(String cardNo) {
    String isEmendType = "";
    String sql1 = "select acno_p_seqno from crd_card " + " where card_no =?";
    sqlSelect(sql1, new Object[] {cardNo});
    if (sqlRowNum <= 0) {
      alertErr2("查無資料: 卡號:" + cardNo);
      return;
    }

    isEmendType = wp.itemStr("ax_emend_type");
    log("type:" + isEmendType);
    if (eqIgno(isEmendType, "1") || eqIgno(isEmendType, "2") || eqIgno(isEmendType, "3")) {
      String sql2 =
          "select line_of_credit_amt as wk_bef_amt from act_acno " + " where acno_p_seqno =?";
      sqlSelect(sql2, new Object[] {sqlStr("acno_p_seqno")});
      if (sqlRowNum <= 0) {
        alertErr2("查無資料: 卡號:" + cardNo);
        return;
      }
    } else if (eqIgno(isEmendType, "5")) {
      String sql2 =
          "select line_of_credit_amt_cash as wk_bef_amt from act_acno " + " where acno_p_seqno =?";
      sqlSelect(sql2, new Object[] {sqlStr("acno_p_seqno")});
      if (sqlRowNum <= 0) {
        alertErr2("查無資料: 卡號:" + cardNo);
        return;
      }
    }
    log("line_of_credit_amt:" + sqlStr("wk_bef_amt"));
    // **--chi_name , acct_type , acct_key , id_p_seqno , corp_p_seqno
    String sql4 = "select " + " acct_type ," + " acct_key ," + " id_p_seqno ," + " corp_p_seqno ,"
        + " uf_acno_name(acno_p_seqno) as db_chi_name " + " from act_acno "
        + " where acno_p_seqno =?";
    sqlSelect(sql4, new Object[] {sqlStr("acno_p_seqno")});
    if (sqlRowNum <= 0) {
      alertErr2("查無資料: 卡號:" + cardNo);
      return;
    }

    String sql5 = "select card_no " + " from crd_card " + " where acno_p_seqno =?"
        + " order by issue_date desc" + commSqlStr.rownum(1);
    sqlSelect(sql5, new Object[] {sqlStr("acno_p_seqno")});
    if (sqlRowNum <= 0) {
      alertErr2("查無資料: 卡號:" + cardNo);
      return;
    }
    // **--db_bef

    String sql6 = "select " + " line_of_credit_amt as bef_loc_amt ,"
        + " line_of_credit_amt_cash as bef_loc_cash " + " from act_acno "
        + " where acno_p_seqno =?";
    sqlSelect(sql6, new Object[] {sqlStr("acno_p_seqno")});

    if (sqlRowNum <= 0) {
      alertErr2("查無資料: 卡號:" + cardNo);
      return;
    }

  }

}
