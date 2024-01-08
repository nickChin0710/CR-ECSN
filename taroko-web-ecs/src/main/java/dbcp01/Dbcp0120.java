/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-08  V1.00.01  ryan            program initial                            *
* 108-12-26  V1.00.02  JustinWu   change the value of card_no used to update or insert into DBC_EMBOSS_TMP from "" to card_no													                     *
* 109-04-23  V1.00.02  yanghan    修改了變量名稱和方法名稱                                                                         *    
* 109-05-27  V1.00.03  Wilson     apply_source -> "T"                        *
* 109-12-25  V1.00.04  Justin       parameterize sql
* 111-12-27  V1.00.05  Wilson     insert dbc_emboss_tmp apply_source -> "T"  *
******************************************************************************/
package dbcp01;

import busi.SqlPrepare;
//import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;


public class Dbcp0120 extends BaseProc {

	int row = -1;
	String msg = "",msgok="";
  /*
   * String kk1 = "",kk2=""; int il_ok = 0; int il_err = 0;
   */
	CommString commString = new CommString();
	CommDate commDate = new CommDate();
	@Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        dataProcess();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      // case "A":
      // /* 新增功能 */
      // saveFunc();
      // break;
      // case "U":
      // /* 更新功能 */
      // saveFunc();
      // break;
      // case "D":
      // /* 刪除功能 */
      // saveFunc();
      // break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("ex_date1", getSysDate());
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_aprid");
      dddwList("dddw_ex_aprid", "sec_user", "usr_id", "usr_cname", "where 1=1 order by usr_id ");
    } catch (Exception e) {
    }
  }

  // for query use only
  private int getWhereStr() throws Exception {

    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");
    if (this.chkStrend(date1, date2) == false) {
      alertErr2("[登錄日期-起迄]  輸入錯誤");
      return -1;
    }
    wp.whereStr = " where 1=1 and ( dbc_card_tmp.card_no = dbc_card.card_no ) "
        + " and ( dbc_card.id_p_seqno = c.id_p_seqno ) " + " and dbc_card_tmp.kind_type = '120' "
        + " and dbc_card_tmp.apr_user ='' ";

    if (notEmpty(wp.itemStr("ex_date1"))) {
      wp.whereStr += " and dbc_card_tmp.crt_date >= :ex_date1 ";
      setString("ex_date1", wp.itemStr("ex_date1"));
    }
    if (notEmpty(wp.itemStr("ex_date2"))) {
      wp.whereStr += " and dbc_card_tmp.crt_date <= :ex_date2 ";
      setString("ex_date2", wp.itemStr("ex_date2"));
    }
    if (notEmpty(wp.itemStr("ex_aprid"))) {
      wp.whereStr += " and dbc_card_tmp.crt_user = :ex_aprid ";
      setString("ex_aprid", wp.itemStr("ex_aprid"));
    }
    switch (wp.itemStr("ex_crdtype")) {
      case "1":
        wp.whereStr += " and dbc_card_tmp.process_kind = '1' ";
        break;
      case "2":
        wp.whereStr += " and dbc_card_tmp.process_kind = '2' ";
        break;
      case "3":
        wp.whereStr += " and dbc_card_tmp.process_kind = '3' ";
        break;
    }
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "dbc_card_tmp.card_no, " + "c.id_no||'-'||c.id_no_code as wk_id, " + "c.id_no, "
        + "c.id_no_code, " + "dbc_card_tmp.corp_no as tmp_corp_no, " + "dbc_card_tmp.process_kind, "
        + "dbc_card_tmp.change_reason, " + "dbc_card_tmp.change_status as tmp_change_status, "
        + "dbc_card_tmp.change_date, " + "dbc_card_tmp.change_reason_old, "
        + "dbc_card_tmp.change_status_old, " + "dbc_card_tmp.change_date_old, "
        + "dbc_card_tmp.old_end_date, " + "dbc_card_tmp.cur_end_date, " + "dbc_card_tmp.crt_user, "
        + "dbc_card_tmp.crt_date, " + "dbc_card.group_code, " + "c.chi_name, "
        + "dbc_card_tmp.apr_user, " + "dbc_card_tmp.apr_date, " + "d.id_no as major_id_no, "
        + "d.id_no_code as major_id_code_no, " + "dbc_card.major_card_no, "
        + "dbc_card.major_id_p_seqno, " + "dbc_card.sup_flag, " + "dbc_card.card_type, "
        + "dbc_card.unit_code, " + "dbc_card.reg_bank_no, " + "dbc_card.ic_flag, "
        + "dbc_card.source_code, " + "dbc_card.emboss_data, " + "dbc_card.corp_no, "
        + "dbc_card.acct_type, " + "UF_ACNO_KEY2(dbc_card.p_seqno,'Y') as acct_key, "
        + "dbc_card.eng_name, " + "dbc_card.force_flag, " + "dbc_card.new_beg_date, "
        + "dbc_card.new_end_date, " + "dbc_card_tmp.cur_beg_date, " + "dbc_card_tmp.kind_type, "
        + "dbc_card_tmp.expire_chg_flag, " + "dbc_card_tmp.expire_reason, "
        + "lpad(' ',20,' ') db_expire_reason, " + "dbc_card_tmp.rowid, "
        + "dbc_card.change_status, " + "dbc_card_tmp.mod_time, " + "dbc_card.ibm_id_code ";
    wp.daoTable = " dbc_card_tmp ,dbc_card ,dbc_idno c " + " left join dbc_idno d on "
        + " dbc_card.major_id_p_seqno = d.id_p_seqno ";
    wp.whereOrder = "  ";
    if (getWhereStr() != 1)
      return;
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
    ofcRetrieve();
  }


  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {
    // -check approve-
    /*
     * if (!check_approve(wp.item_ss("approval_user"), wp.item_ss("approval_passwd"))) { return; }
     */
    busi.SqlPrepare sp = new SqlPrepare();
    String[] opt = wp.itemBuff("opt");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] aaCardNo = wp.itemBuff("card_no");
    String[] aaKindType = wp.itemBuff("kind_type");
    String[] aaProcessKind = wp.itemBuff("process_kind");
    wp.listCount[0] = aaModSeqno.length;
    // -update-
    row = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      row = (int) this.toNum(opt[ii]) - 1;
      if (row < 0) {
        continue;
      }
      // update dbc_card_tmp
      sp.sql2Update("dbc_card_tmp");
      sp.ppstr("apr_user", wp.loginUser);
      sp.addsql(", apr_date = to_char(sysdate,'YYYYMMDD')");
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.ppstr("mod_user", wp.loginUser);
      sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
      sp.sql2Where(" where card_no=?", aaCardNo[row]);
      sp.sql2Where(" and kind_type=?", aaKindType[row]);
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum <= 0) {
        alertErr("update dbc_card_tmp err");
        wp.colSet(row, "ok_flag", "!");
        sqlCommit(0);
        return;
      }
      switch (aaProcessKind[row]) {
        // 1. 線上續卡
        case "1":
          if (wfMoveEmbossTmp(row) != 1) {
            wp.colSet(row, "ok_flag", "!");
            sqlCommit(0);
            return;
          }
          break;
        // 2. 取消線上續卡
        case "2":
          if (wfCancelChg(row) != 1) {
            wp.colSet(row, "ok_flag", "!");
            sqlCommit(0);
            return;
          }
          break;
        // 3. 系統續卡改不續卡
        case "3":
          if (wfProcessChg(row) != 1) {
            wp.colSet(row, "ok_flag", "!");
            sqlCommit(0);
            return;
          }
          break;
      }
    }

    // insert dbc_card_tmp_h
    String sqlInsert =
        "insert into dbc_card_tmp_h select * from dbc_card_tmp where kind_type = '120' and apr_date <>''";
    sqlExec(sqlInsert);
    if (sqlRowNum <= 0) {
      alertErr("insert dbc_card_tmp_h err");
      // wp.col_set(rr, "ok_flag", "!");
      sqlCommit(0);
      return;
    }

    // delete_tmp dbc_card_tmp
    String sqlDelete = "delete dbc_card_tmp where kind_type = '120' and apr_date <>''";
    sqlExec(sqlDelete);
    if (sqlRowNum <= 0) {
      alertErr("delete dbc_card_tmp err");
      // wp.col_set(rr, "ok_flag", "!");
      sqlCommit(0);
      return;
    }

    sqlCommit(1);
    queryFunc();
    errmsg("放行完成");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void listWkdata() {
    String all = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      all = wp.colStr(ii, "process_kind");
      wp.colSet(ii, "tt_process_kind", commString.decode(all, ",1,2,3", ",線上續卡,取消線上續卡,系統續卡改不續卡"));

      all = wp.colStr(ii, "change_reason");
      wp.colSet(ii, "tt_change_reason", commString.decode(all, ",1,2,3", ",系統續卡,提前續卡,人工續卡"));

      all = wp.colStr(ii, "tmp_change_status");
      wp.colSet(ii, "tt_change_status", commString.decode(all, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));

      all = wp.colStr(ii, "change_reason_old");
      wp.colSet(ii, "tt_change_reason_old", commString.decode(all, ",0,1,2,3", ",,系統續卡,提前續卡,人工續卡"));

      all = wp.colStr(ii, "expire_chg_flag");
      wp.colSet(ii, "tt_expire_chg_flag", commString.decode(all, ",1,4,5", ",預約不續卡,人工不續卡,系統不續卡"));

    }
  }

  void ofcRetrieve() {
    String lsExpireChgFlag = "", lsWfDesc = "", lsExpireReason = "", sqlSelect = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      lsExpireChgFlag = wp.colStr(ii, "expire_chg_flag");
      lsExpireReason = wp.colStr(ii, "expire_reason");
      if (lsExpireChgFlag.equals("1")) {
        sqlSelect =
            "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_VD_O' and wf_id = :ls_expire_reason ";
        setString("ls_expire_reason", lsExpireReason);
        sqlSelect(sqlSelect);
        lsWfDesc = sqlStr("wf_desc");
        wp.colSet(ii, "db_expire_reason", lsExpireReason + lsWfDesc);
      }
      if (lsExpireChgFlag.equals("4")) {
        sqlSelect =
            "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_VD_M' and wf_id = :ls_expire_reason ";
        setString("ls_expire_reason", lsExpireReason);
        sqlSelect(sqlSelect);
        lsWfDesc = sqlStr("wf_desc");
        wp.colSet(ii, "db_expire_reason", lsExpireReason + lsWfDesc);
      }
    }
  }

  int wfCancelChg(int as_row) {
    String lsChangeStatus = "", lsCardNo = "";
    String[] aaCardNo = wp.itemBuff("card_no");
    String[] aaChangeStatus = wp.itemBuff("change_status");

    lsCardNo = aaCardNo[as_row];
    lsChangeStatus = aaChangeStatus[as_row];

    if (lsChangeStatus.equals("1")) {
      String sqlDelete =
          "delete from dbc_emboss_tmp where old_card_no = :ls_card_no and nccc_batchno =''";
      setString("ls_card_no", lsCardNo);
      sqlExec(sqlDelete);
      if (sqlRowNum <= 0) {
        alertErr("此卡片已送製卡,不可取消提前續卡");
        return -1;
      }
    }

    String sqlUpdate = "update dbc_card set " + " expire_chg_flag = '' "
        + " ,expire_reason   = ''  " + " ,expire_chg_date = '' " + " ,change_status = '' "
        + " ,change_reason   = '' " + " ,mod_pgm = :mod_pgm " + " ,mod_time = sysdate "
        + " ,change_date = '' " + " where card_no = :ls_card_no ";
    setString("mod_pgm", wp.modPgm());
    setString("ls_card_no", lsCardNo);
    sqlExec(sqlUpdate);
    if (sqlRowNum <= 0) {
      alertErr("寫入不續卡註記失敗");
      return -1;
    }

    return 1;
  }

  int wfProcessChg(int as_row) {
    String[] aaCardNo = wp.itemBuff("card_no");
    // String[] aa_expire_chg_flag = wp.item_buff("expire_chg_flag");
    String[] aaChangeStatus = wp.itemBuff("change_status");

    String lsCardNo = aaCardNo[as_row];
    // String ls_expire_chg_flag=aa_expire_chg_flag[as_row];

    // -- 刪除資料
    String sqlDelete = "delete from dbc_emboss_tmp where old_card_no = :ls_card_no ";
    sqlExec(sqlDelete);
    if (sqlRowNum <= 0) {
      alertErr("已製卡完成不在暫存檔內");
      return -1;
    }

    // -- update卡片主檔改為系統不續卡
    String sqlUpdate = "update dbc_card set " + " expire_chg_flag  = '1' "
        + " ,expire_reason    = 'Z1'  " + " ,expire_chg_date  = to_char(sysdate,'yyyymmdd') "
        + " ,change_status = '' " + " ,change_reason   = '' " + " ,mod_pgm = : mod_pgm "
        + " ,mod_time = sysdate " + " ,change_date = '' " + " where card_no = :ls_card_no ";
    setString("ls_card_no", lsCardNo);
    setString("mod_pgm", wp.modPgm());
    sqlExec(sqlUpdate);
    if (sqlRowNum <= 0) {
      alertErr("寫入不續卡註記失敗");
      return -1;
    }

    return 1;
  }

  int wfMoveEmbossTmp(int asRow) {
    String lsChangeStatus = "", lsIdpSeqno = "", lspSeqno = "", lsCardno = "",
        lsMajorCardno = "", lRecno = "" ,lsAcctNo = "";
    // String[] aa_change_status = wp.item_buff("change_status");
    // String[] aa_tmp_change_status = wp.item_buff("tmp_change_status");
    // String[] aa_id_p_seqno = wp.item_buff("id_p_seqno");
    // String[] aa_p_seqno = wp.item_buff("p_seqno");
    String[] aaCardNo = wp.itemBuff("card_no");
    String[] aaSupFlag = wp.itemBuff("sup_flag");
    String[] aaMajorCardNo = wp.itemBuff("major_card_no");
    String[] aaNewEndDate = wp.itemBuff("new_end_date");
    String[] aaCardType = wp.itemBuff("card_type");
    String[] aaUnitCode = wp.itemBuff("unit_code");
    String[] aaRegBankNo = wp.itemBuff("reg_bank_no");
    String[] aaIdNo = wp.itemBuff("id_no");
    String[] aaIdNoCode = wp.itemBuff("id_no_code");
    String[] aaIcFlag = wp.itemBuff("ic_flag");
    String[] aaMajorIdNo = wp.itemBuff("major_id_no");
    String[] aaMajorIdNoCode = wp.itemBuff("major_id_no_code");
    String[] aaEmbossData = wp.itemBuff("emboss_data");
    String[] aaGroupCode = wp.itemBuff("group_code");
    String[] aaSourceCode = wp.itemBuff("source_code");
    String[] aaCorpNo = wp.itemBuff("corp_no");
    String[] aaAcctType = wp.itemBuff("acct_type");
    String[] aaAcctKey = wp.itemBuff("acct_key");
    String[] aaEngName = wp.itemBuff("eng_name");
    String[] aaNewBegDate = wp.itemBuff("new_beg_date");
    String[] aaForceFlag = wp.itemBuff("force_flag");
    String[] aaIbmIdCode = wp.itemBuff("ibm_id_code");
    String[] aaChangeReason = wp.itemBuff("change_reason");
    String[] aaCurBegDate = wp.itemBuff("cur_beg_date");
    String[] aaCurEndDate = wp.itemBuff("cur_end_date");

    String sqlSelect =
        "select id_p_seqno,p_seqno,change_status,acct_no " + " from dbc_card where card_no = :ls_cardno";
    setString("ls_cardno", aaCardNo[asRow]);
    sqlSelect(sqlSelect);
    lsChangeStatus = sqlStr("change_status");
    lsIdpSeqno = sqlStr("id_p_seqno");
    lspSeqno = sqlStr("p_seqno");
    lspSeqno = sqlStr("p_seqno");
    lsAcctNo = sqlStr("acct_no");
    if (sqlRowNum <= 0) {
      alertErr("找取不到卡檔資料");
      return -1;
    }

    if (lsChangeStatus.equals("1")) {
      alertErr("續卡製卡中，不可再做續卡");
      return -1;
    }
    if (lsChangeStatus.equals("2")) {
      alertErr("已送製卡中,不可再做續卡");
      return -1;
    }
    lsCardno = aaCardNo[asRow];
    sqlSelect =
        "select count(*) as li_cnt " + " from  dbc_emboss_tmp " + " where old_card_no = :ls_cardno";
    setString("ls_cardno", lsCardno);
    sqlSelect(sqlSelect);
    double li_cnt = sqlNum("li_cnt");
    if (li_cnt > 0) {
      alertErr("續卡製卡中，不可再做續卡");
      return -1;
    }

    sqlSelect =
        "select risk_bank_no,line_of_credit_amt,chg_addr_date,stat_send_paper,stat_send_internet "
            + " from dba_acno " + " where p_seqno = :ls_p_seqno";
    setString("ls_p_seqno", lspSeqno);
    sqlSelect(sqlSelect);
    String lsRiskBankNo = sqlStr("risk_bank_no");
    String li_act_credit_amt = sqlStr("line_of_credit_amt");
    // String ls_chg_addr_date = sql_ss("chg_addr_date");
    // String ls_send_paper = sql_ss("stat_send_paper");
    // String stat_send_internet = sql_ss("stat_send_internet");
    if (sqlRowNum <= 0) {
      alertErr("無法抓取到此卡號帳戶資料");
      return -1;
    }

    // -- 改為抓自己之效期最展期 2002/01/24 > sysdate+6
    String lsMajorCurrentCode = "", lsMajorValidFm = "", lsMajorValidTo = "";
    if (aaSupFlag[asRow].equals("1")) {
      lsMajorCardno = aaMajorCardNo[asRow];
      sqlSelect = "select new_beg_date,new_end_date,current_code "
          + " from dbc_card where card_no = :ls_major_cardno ";
      setString("ls_major_cardno", lsMajorCardno);
      sqlSelect(sqlSelect);
      lsMajorCurrentCode = sqlStr("current_code");
      lsMajorValidFm = sqlStr("new_beg_date");
      lsMajorValidTo = sqlStr("new_end_date");
      if (sqlRowNum <= 0) {
        alertErr("找取不到正卡資料");
        return -1;
      }
      if (!lsMajorCurrentCode.equals("0")) {
        alertErr("正卡不為正常卡,不可做線上續卡");
        return -1;
      }
    }

    String lsCreateDate = getSysDate();
    String liSystemDd = strMid(lsCreateDate, 6, 2);

    // --Get Batchno--
    String batchno1 = strMid(lsCreateDate, 2, 6);
    sqlSelect = "select max(batchno) as ls_batchno "
        + " from dbc_emboss_tmp where substr(batchno,1,6) = :ls_batchno1 and substr(batchno,7,2) < '85' ";
    setString("ls_batchno1", batchno1);
    sqlSelect(sqlSelect);
    String lsBatchno = sqlStr("ls_batchno");
    if (empty(lsBatchno)) {
      lsBatchno = batchno1 + "01";
    } else {
      sqlSelect =
          "select (max(recno)+1) as li_recno " + " from dbc_emboss_tmp where batchno = :ls_batchno";
      setString("ls_batchno", lsBatchno);
      sqlSelect(sqlSelect);
      lRecno = sqlStr("li_recno");
    }
    String ls_mail_type = "";
    if (empty(lRecno) || lRecno.equals("0")) {
      lRecno = "1";
    }

    String lIsidnoValue1 = "", lsIdnoValue2 = "";
    sqlSelect =
        " select chi_name, birthday " + " from dbc_idno where id_p_seqno = :ls_id_p_seqno ";
    setString("ls_id_p_seqno", lsIdpSeqno);
    sqlSelect(sqlSelect);
    lIsidnoValue1 = sqlStr("chi_name");
    lsIdnoValue2 = sqlStr("birthday");
    if (sqlRowNum <= 0) {
      alertErr("抓取卡人檔失敗");
      return -1;
    }
    String h_age_indicator = "";
    /*
     * sql_select="select AGE_INDICATOR from ptr_bintable where bin_no = substr(:ls_cardno,1,6) ";
     * setString("ls_cardno",ls_cardno); sqlSelect(sql_select);
     */// "AGE_INDICATOR" is not valid
    // ls_group_code = aa_group_code[as_row];
    String lsDateFm = aaCurBegDate[asRow];
    String lsDateTo = aaCurEndDate[asRow];
    if (this.toNum(liSystemDd) >= 25) {
      sqlSelect =
          "select to_char(add_months(to_date(:ls_date_fm,'yyyymmdd'),1),'yyyymm')||'01' as ls_date_fm from dual";
      setString("ls_date_fm", lsDateFm);
      sqlSelect(sqlSelect);
      lsDateFm = sqlStr("ls_date_fm");
      if (sqlRowNum <= 0) {
        alertErr("日期資料轉換錯誤 !!");
        return -1;
      }
    }

    // -- 寫入dbc_emboss_tmp
    busi.SqlPrepare sp = new SqlPrepare();
    sqlSelect = "select  count(*) as cnt " + " from dbc_emboss_tmp "
        + " where batchno = :ls_batchno and recno = :li_recno";
    setString("ls_batchno", lsBatchno);
    setString("li_recno", lRecno);
    sqlSelect(sqlSelect);
    double cnt = sqlNum("cnt");
    if (cnt > 0) {
      sp.sql2Update("dbc_emboss_tmp");
      // sp.addsql(", crt_date = to_char(sysdate,'YYYYMMDD')");
      sp.ppstr("act_no", lsAcctNo);
      sp.ppstr("emboss_source", "4");
      sp.ppstr("apply_source", "T");
      sp.ppstr("to_nccc_code", "Y");
      sp.ppstr("mail_type", ls_mail_type);
      sp.ppstr("card_type", aaCardType[asRow]);
      sp.ppstr("unit_code", aaUnitCode[asRow]);
      sp.ppstr("reg_bank_no", aaRegBankNo[asRow]);
      sp.ppstr("risk_bank_no", lsRiskBankNo);
      sp.ppstr("status_code", "1");
      sp.ppstr("card_no", lsCardno);
      sp.ppstr("old_card_no", lsCardno);
      sp.ppstr("change_reason", aaChangeReason[asRow]);
      sp.ppstr("nccc_type", "2");
      sp.ppstr("reason_code", "");
      sp.ppstr("apply_id", aaIdNo[asRow]);
      sp.ppstr("apply_id_code", aaIdNoCode[asRow]);
      sp.ppstr("ic_flag", aaIcFlag[asRow]);
      // --- 附卡寫入正卡資料
      if (aaSupFlag[asRow].equals("1")) {
        sp.ppstr("major_card_no", aaMajorCardNo[asRow]);
        sp.ppstr("pm_id", aaMajorIdNo[asRow]);
        sp.ppstr("pm_id_code", aaMajorIdNoCode[asRow]);
        sp.ppstr("major_valid_fm", lsMajorValidFm);
        sp.ppstr("major_valid_to", lsMajorValidTo);
      } else {
        sp.ppstr("pm_id", aaMajorIdNo[asRow]);
        sp.ppstr("pm_id_code", aaMajorIdNoCode[asRow]);
      }
      sp.ppstr("emboss_4th_data", aaEmbossData[asRow]);
      sp.ppstr("group_code", aaGroupCode[asRow]);
      sp.ppstr("source_code", aaSourceCode[asRow]);
      sp.ppstr("corp_no", aaCorpNo[asRow]);
      sp.ppstr("acct_type", aaAcctType[asRow]);
      sp.ppstr("acct_key", aaAcctKey[asRow]);
      sp.ppstr("chi_name", lIsidnoValue1);
      sp.ppstr("eng_name", aaEngName[asRow]);
      sp.ppstr("birthday", lsIdnoValue2);
      sp.ppnum("credit_lmt", this.toNum(li_act_credit_amt));
      sp.ppstr("force_flag", aaForceFlag[asRow]);
      sp.ppstr("old_beg_date", aaNewBegDate[asRow]);
      sp.ppstr("old_end_date", aaNewEndDate[asRow]);
      sp.ppstr("third_rsn", "A");
      sp.ppstr("apply_ibm_id_code", aaIbmIdCode[asRow]);
      sp.ppstr("age_indicator", h_age_indicator);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("valid_fm", lsDateFm);
      sp.ppstr("valid_to", lsDateTo);
      sp.ppstr("sup_flag", aaSupFlag[asRow]);
      sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
      sp.addsql(", mod_time = sysdate");
      sp.sql2Where(" where batchno=?", lsBatchno);
      sp.sql2Where(" and recno=?", lRecno);
    } else {
      sp.sql2Insert("dbc_emboss_tmp");
      sp.ppstr("act_no", lsAcctNo);
      sp.ppstr("batchno", lsBatchno);
      sp.ppstr("recno", lRecno);
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("emboss_source", "4");
      sp.ppstr("apply_source", "T");
      sp.ppstr("to_nccc_code", "Y");
      sp.ppstr("mail_type", ls_mail_type);
      sp.ppstr("card_type", aaCardType[asRow]);
      sp.ppstr("unit_code", aaUnitCode[asRow]);
      sp.ppstr("reg_bank_no", aaRegBankNo[asRow]);
      sp.ppstr("risk_bank_no", lsRiskBankNo);
      sp.ppstr("status_code", "1");
      sp.ppstr("card_no", lsCardno);
      sp.ppstr("old_card_no", lsCardno);
      sp.ppstr("change_reason", aaChangeReason[asRow]);
      sp.ppstr("nccc_type", "2");
      sp.ppstr("reason_code", "");
      sp.ppstr("apply_id", aaIdNo[asRow]);
      sp.ppstr("apply_id_code", aaIdNoCode[asRow]);
      sp.ppstr("ic_flag", aaIcFlag[asRow]);
      // --- 附卡寫入正卡資料
      if (aaSupFlag[asRow].equals("1")) {
        sp.ppstr("major_card_no", aaMajorCardNo[asRow]);
        sp.ppstr("pm_id", aaMajorIdNo[asRow]);
        sp.ppstr("pm_id_code", aaMajorIdNoCode[asRow]);
        sp.ppstr("major_valid_fm", lsMajorValidFm);
        sp.ppstr("major_valid_to", lsMajorValidTo);
      } else {
        sp.ppstr("pm_id", aaMajorIdNo[asRow]);
        sp.ppstr("pm_id_code", aaMajorIdNoCode[asRow]);
      }
      sp.ppstr("emboss_4th_data", aaEmbossData[asRow]);
      sp.ppstr("group_code", aaGroupCode[asRow]);
      sp.ppstr("source_code", aaSourceCode[asRow]);
      sp.ppstr("corp_no", aaCorpNo[asRow]);
      sp.ppstr("acct_type", aaAcctType[asRow]);
      sp.ppstr("acct_key", aaAcctKey[asRow]);
      sp.ppstr("chi_name", lIsidnoValue1);
      sp.ppstr("eng_name", aaEngName[asRow]);
      sp.ppstr("birthday", lsIdnoValue2);
      sp.ppnum("credit_lmt", this.toNum(li_act_credit_amt));
      sp.ppstr("force_flag", aaForceFlag[asRow]);
      sp.ppstr("old_beg_date", aaNewBegDate[asRow]);
      sp.ppstr("old_end_date", aaNewEndDate[asRow]);
      sp.ppstr("third_rsn", "A");
      sp.ppstr("apply_ibm_id_code", aaIbmIdCode[asRow]);
      sp.ppstr("age_indicator", h_age_indicator);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("valid_fm", lsDateFm);
      sp.ppstr("valid_to", lsDateTo);
      sp.ppstr("sup_flag", aaSupFlag[asRow]);
      sp.addsql(", mod_seqno ", ", 1");
      sp.addsql(", mod_time ", ", sysdate");
      sp.addsql(", crt_date ", ", to_char(sysdate,'YYYYMMDD')");
    }
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      alertErr("此資料無法搬到續卡檔內");
      return -1;
    }

    String lsChangeReason = aaChangeReason[asRow];
    String sqlUpdate = " update dbc_card set " + "expire_chg_flag = '' " + ",expire_chg_date = '' "
        + ",change_reason = :ls_change_reason " + ",change_date = to_char(sysdate,'yyyymmdd') "
        + ",change_status = '1' " + ",mod_pgm = :mod_pgm " + ",mod_time = sysdate "
        + "where card_no = :ls_cardno ";
    setString("ls_cardno", lsCardno);
    setString("ls_change_reason", lsChangeReason);
    setString("mod_pgm", wp.modPgm());
    sqlExec(sqlUpdate);
    if (sqlRowNum <= 0) {
      alertErr("寫入卡片檔錯誤~");
      return -1;
    }

    return 1;
  }

}
