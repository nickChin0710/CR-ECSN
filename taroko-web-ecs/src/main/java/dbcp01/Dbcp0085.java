/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-01  V1.00.01  ryan       program initial                            *
* 108-12-17  V1.00.02  ryan		  update : ptr_group_card==>crd_item_unit    *
* 108-12-30  V1.00.03  JustinWu change the value of card_no used to update or insert into DBC_EMBOSS_TMP  from "" to card_no
* 109-04-23  V1.00.02  yanghan  修改了變量名稱和方法名稱*   
* 110/1/4    V1.00.03  yanghan       修改了變量名稱和方法名稱            *
* 110-01-05  V1.00.04  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         * 
* 112-03-27  V1.00.05  Wilson     update dbc_card add cancel_expire_chg_date *
******************************************************************************/
package dbcp01;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import busi.SqlPrepare;
import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;


public class Dbcp0085 extends BaseProc {

  int rr = -1;
  String msg = "", msgok = "";
  // String kk1 = "", kk2 = "";
  // int il_ok = 0;
  // int il_err = 0;
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
      // insertFunc();
      // break;
      // case "U":
      // /* 更新功能 */
      // updateFunc();
      // break;
      // case "D":
      // /* 刪除功能 */
      // deleteFunc();
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
        + " and ( dbc_card.p_seqno = dba_acno.p_seqno ) "
        + " and ( dbc_card.id_p_seqno = c.id_p_seqno ) "
        + " and ( dbc_card.major_id_p_seqno = d.id_p_seqno ) "
        + " and dbc_card_tmp.kind_type = '080' " + " and dbc_card_tmp.apr_user ='' ";

    if (empty(wp.itemStr("ex_date1")) == false) {
      wp.whereStr += " and dbc_card_tmp.crt_date >= :ex_date1 ";
      setString("ex_date1", wp.itemStr("ex_date1"));
    }
    if (empty(wp.itemStr("ex_date2")) == false) {
      wp.whereStr += " and dbc_card_tmp.crt_date <= :ex_date2 ";
      setString("ex_date2", wp.itemStr("ex_date2"));
    }
    if (empty(wp.itemStr("ex_aprid")) == false) {
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
      case "4":
        wp.whereStr += " and dbc_card_tmp.process_kind = '4' ";
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
        + "dbc_card_tmp.old_end_date, " + "dbc_card_tmp.cur_end_date, " + "dbc_card_tmp.crt_user, "
        + "dbc_card_tmp.crt_date, " + "dbc_card.group_code, " + "c.chi_name, "
        + "dbc_card_tmp.apr_user, " + "dbc_card_tmp.apr_date, " + "d.id_no as major_id_no, "
        + "d.id_no_code as major_id_no_code, " + "dbc_card.major_card_no, "
        + "dbc_card.major_id_p_seqno, " + "dbc_card.sup_flag, " + "dbc_card.card_type, "
        + "dbc_card.unit_code, " + "dbc_card.reg_bank_no, " + "dbc_card.ic_flag, "
        + "dbc_card.source_code, " + "dbc_card.emboss_data, " + "dbc_card.corp_no, "
        + "dbc_card.acct_type, " + "dba_acno.acct_key, " + "dbc_card.eng_name, "
        + "dbc_card.force_flag, " + "dbc_card.new_beg_date, " + "dbc_card.new_end_date, "
        + "dbc_card_tmp.cur_beg_date, " + "dbc_card_tmp.expire_reason, "
        + "dbc_card_tmp.expire_chg_flag, " + "dbc_card_tmp.expire_chg_date, "
        + "lpad(' ',20,' ') db_expire_reason, "
        + "dbc_card_tmp.change_status as tmp_change_status, " + "dbc_card.id_p_seqno, "
        + "dbc_card_tmp.kind_type, " + "dbc_card.p_seqno, " + "dbc_card_tmp.expire_chg_flag_old, "
        + "dbc_card_tmp.expire_reason_old, " + "dbc_card_tmp.change_reason, "
        + "lpad(' ',20,' ') db_expire_reason_old, " + "dbc_card_tmp.rowid, "
        + "dbc_card.change_reason, " + "dbc_card.change_status, " + "dbc_card_tmp.mod_time, "
        + "dbc_card.ibm_id_code  ";
    wp.daoTable = " dbc_card_tmp ,dbc_card ,dba_acno ,dbc_idno c ,dbc_idno d ";
    wp.whereOrder = "  ";
    if (getWhereStr() != 1)
      return;
    // System.out.println("select "+wp.selectSQL+" from "+wp.daoTable+" "+wp.whereStr+"
    // "+wp.whereOrder);
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
    rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      // update dbc_card_tmp
      sp.sql2Update("dbc_card_tmp");
      sp.ppstr("apr_user", wp.loginUser);
      sp.addsql(", apr_date = to_char(sysdate,'YYYYMMDD')");
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.ppstr("mod_user", wp.loginUser);
      sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
      sp.sql2Where(" where card_no=?", aaCardNo[rr]);
      sp.sql2Where(" and kind_type=?", aaKindType[rr]);
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum <= 0) {
        alertErr("update dbc_card_tmp err");
        wp.colSet(rr, "ok_flag", "!");
        sqlCommit(0);
        return;
      }

      switch (aaProcessKind[rr]) {
        // 預約不續卡
        case "1":
          if (wfUpdDbcCard(rr) != 1) {
            wp.colSet(rr, "ok_flag", "!");
            sqlCommit(0);
            return;
          }
          break;
        // 取消不續卡(放行後)
        case "2":
          if (wfCancelExpire(rr) != 1) {
            wp.colSet(rr, "ok_flag", "!");
            sqlCommit(0);
            return;
          }
          break;
        // 系統不續卡改續卡
        case "3":
          if (wfMoveEmbossTmp(rr) != 1) {
            wp.colSet(rr, "ok_flag", "!");
            sqlCommit(0);
            return;
          }
          break;
        // 人工不續卡
        case "4":
          if (wfUpdDbcCard(rr) != 1) {
            wp.colSet(rr, "ok_flag", "!");
            sqlCommit(0);
            return;
          }
          break;
      }

    }

    // insert dbc_card_tmp_h
    String sqlInsert =
        "insert into dbc_card_tmp_h select * from dbc_card_tmp where kind_type = '080' and apr_date <>''";
    sqlExec(sqlInsert);
    if (sqlRowNum <= 0) {
      alertErr("insert dbc_card_tmp_h err");
      wp.colSet(rr, "ok_flag", "!");
      sqlCommit(0);
      return;
    }

    // delete dbc_card_tmp
    String sqlDelete = "delete dbc_card_tmp where kind_type = '080' and apr_date <>''";
    sqlExec(sqlDelete);
    if (sqlRowNum <= 0) {
      alertErr("delete dbc_card_tmp err");
      wp.colSet(rr, "ok_flag", "!");
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
    String tmpString1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      tmpString1 = wp.colStr(ii, "process_kind");
      wp.colSet(ii, "tt_process_kind",
          commString.decode(tmpString1, ",0,1,2,3,4", ",取消不續卡(放行前),預約不續卡,取消不續卡(放行後),系統不續卡改續卡,人工不續卡"));

      tmpString1 = wp.colStr(ii, "expire_chg_flag");
      wp.colSet(ii, "tt_expire_chg_flag",
          commString.decode(tmpString1, ",1,2,3,4", ",預約不續卡,取消不續卡,系統不續卡改續卡,人工不續卡"));

      tmpString1 = wp.colStr(ii, "change_reason");
      wp.colSet(ii, "tt_change_reason", commString.decode(tmpString1, ",1,2,3,4", ",續卡待製卡中,續卡製卡中,續卡完成,製卡失敗"));
    }
  }

  void ofcRetrieve() {
    String lsExpireChgFlag = "", lsWfDesc = "", lsExpireReason = "", lsExpireReasonOld = "",
        lsExpireChgFlagOld = "", sqlSelect = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      lsExpireChgFlag = wp.colStr(ii, "expire_chg_flag");
      lsExpireChgFlagOld = wp.colStr(ii, "expire_chg_flag_old");
      lsExpireReason = wp.colStr(ii, "expire_reason");
      lsExpireReasonOld = wp.colStr(ii, "expire_reason_old");
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
      if (lsExpireChgFlagOld.equals("1")) {
        sqlSelect =
            "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_VD_O' and wf_id = :ls_expire_reason_old ";
        setString("ls_expire_reason_old", lsExpireReasonOld);
        sqlSelect(sqlSelect);
        lsWfDesc = sqlStr("wf_desc");
        wp.colSet(ii, "db_expire_reason", lsExpireReasonOld + lsWfDesc);
      }
      if (lsExpireChgFlagOld.equals("4")) {
        sqlSelect =
            "select wf_desc from ptr_sys_idtab where wf_type = 'NOTCHG_VD_M' and wf_id = :ls_expire_reason_old ";
        setString("ls_expire_reason_old", lsExpireReasonOld);
        sqlSelect(sqlSelect);
        lsWfDesc = sqlStr("wf_desc");
        wp.colSet(ii, "db_expire_reason", lsExpireReasonOld + lsWfDesc);
      }
    }
  }

  int wfUpdDbcCard(int as_row) {
    String lsChangeStatus = "", tempExpireChgFlag = "";
    String lsCardNo, lsExpireChgFlag, lsExpireReason;
    String[] aaCardNo = wp.itemBuff("card_no");
    String[] aaExpireChgFlag = wp.itemBuff("expire_chg_flag");
    String[] aaTmpChangeStatus = wp.itemBuff("tmp_change_status");
    String[] aaExpireReason = wp.itemBuff("expire_reason");
    lsCardNo = aaCardNo[as_row];
    lsExpireChgFlag = aaExpireChgFlag[as_row];
    lsChangeStatus = aaTmpChangeStatus[as_row];
    lsExpireReason = aaExpireReason[as_row];

    if (lsChangeStatus.equals("2")) {
      alertErr("此卡片已送製卡,不可作任何異動");
      return -1;
    }
    if (lsChangeStatus.equals("1")) {
      String sql_delete =
          "delete from dbc_emboss_tmp where old_card_no = :ls_card_no and card_no = :ls_card_no2 and nccc_batchno =''";
      setString("ls_card_no", lsCardNo);
      setString("ls_card_no2", lsCardNo);
      sqlExec(sql_delete);
      if (sqlRowNum <= 0) {
        alertErr("續卡製卡中，不可改為不續卡");
        return -1;
      }
    }
    if (lsExpireChgFlag.equals("1")) {
      tempExpireChgFlag = "2";
    }
    if (lsExpireChgFlag.equals("4")) {
      tempExpireChgFlag = "3";
    }
    String sqlUpdate = "update dbc_card set " + " expire_chg_flag = :temp_expire_chg_flag "
        + " ,expire_reason   = :ls_expire_reason  "
        + " ,expire_chg_date = to_char(sysdate,'yyyymmdd') " + " ,change_status = '' "
        + " ,change_date     = '' " + " ,change_reason   = '' " + " where card_no = :ls_card_no ";
    setString("temp_expire_chg_flag", tempExpireChgFlag);
    setString("ls_expire_reason", lsExpireReason);
    setString("ls_card_no", lsCardNo);
    sqlExec(sqlUpdate);
    if (sqlRowNum <= 0) {
      alertErr("寫入不續卡註記失敗");
      return -1;
    }

    return 1;
  }

  int wfCancelExpire(int as_row) {
    String[] aaCardNo = wp.itemBuff("card_no");
    String lsCardNo = aaCardNo[as_row];
    String lsExpireChgFlag = "", lsChangeStatus = "";

    String sqlSelect =
        "select expire_chg_flag,change_status from dbc_card where card_no = :ls_card_no";
    setString("ls_card_no", lsCardNo);
    sqlSelect(sqlSelect);
    lsExpireChgFlag = sqlStr("expire_chg_flag");
    lsChangeStatus = sqlStr("change_status");

    if (lsChangeStatus.equals("2")) {
      alertErr("此卡片已送製卡,不可作任何異動");
      return -1;
    }
    if (empty(lsExpireChgFlag)) {
      alertErr("此筆資料本身並無不續卡註記");
      return -1;
    }
    if (lsChangeStatus.equals("1")) {
      String sqlDelete =
          "delete from dbc_emboss_tmp where card_no = :ls_card_no and emboss_source  = '4' and nccc_batchno=''";
      sqlExec(sqlDelete);
      if (sqlRowNum <= 0) {
        alertErr("此卡片已送製卡,不可做預約不續卡");
        return -1;
      }
    }

    String sqlUpdate = "update dbc_card set " + " expire_chg_flag  = '' "
        + " ,expire_reason    = ''  " + " ,expire_chg_date  = '' " + " ,change_status = '' "
        + " ,change_reason   = '' " + " ,cancel_expire_chg_date = to_char(sysdate,'yyyymmdd') " + " where card_no = :ls_card_no ";
    setString("ls_card_no", lsCardNo);
    sqlExec(sqlUpdate);
    if (sqlRowNum <= 0) {
      alertErr("取消不續卡註記失敗");
      return -1;
    }

    return 1;
  }

  int wfMoveEmbossTmp(int as_row) {
    String lsChangeStatus = "", lsIdpSeqno = "", lspSeqno = "", lsCardno = "", lsMajorCardno = "",
        liRecno = "", lsGroupCode = "";
    String[] aaChangeStatus = wp.itemBuff("change_status");
    String[] aaTmpChangeStatus = wp.itemBuff("tmp_change_status");
    String[] aaIdpSeqno = wp.itemBuff("id_p_seqno");
    String[] aapSeqno = wp.itemBuff("p_seqno");
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

    // -- 產生新效期起為'舊有效期之迄日年月一 號' 2001/09/11 (mod)
    lsChangeStatus = aaTmpChangeStatus[as_row];
    lsIdpSeqno = aaIdpSeqno[as_row];
    lspSeqno = aapSeqno[as_row];
    if (lsChangeStatus.equals("1")) {
      alertErr("續卡製卡中，不可改為不續卡");
      return -1;
    }
    if (lsChangeStatus.equals("2")) {
      alertErr("此卡片送製卡中,不可再做提前續卡");
      return -1;
    }
    lsCardno = aaCardNo[as_row];
    String sqlSelect =
        "select count(*) as li_cnt from  dbc_emboss_tmp where old_card_no = :ls_cardno";
    setString("ls_cardno", lsCardno);
    sqlSelect(sqlSelect);
    double li_cnt = sqlNum("li_cnt");
    if (li_cnt > 0) {
      alertErr("續卡製卡中，不可改為不續卡");
      return -1;
    }
    String ls_major_current_code = "", ls_major_valid_fm = "", ls_major_valid_to = "";
    if (aaSupFlag[as_row].equals("1")) {
      lsMajorCardno = aaMajorCardNo[as_row];
      sqlSelect =
          "select new_beg_date,new_end_date,current_code from dbc_card where card_no = :ls_major_cardno ";
      setString("ls_major_cardno", lsMajorCardno);
      sqlSelect(sqlSelect);
      ls_major_current_code = sqlStr("current_code");
      ls_major_valid_fm = sqlStr("new_beg_date");
      ls_major_valid_to = sqlStr("new_end_date");
      if (sqlRowNum <= 0) {
        alertErr("找取不到正卡資料");
        return -1;
      }
      if (!ls_major_current_code.equals("0")) {
        alertErr("正卡不為正常卡,不可做線上續卡");
        return -1;
      }
    }
    // -- 改為抓自己之效期最展期 2002/01/24
    String lsValidDate = aaNewEndDate[as_row];
    String lsCreateDate = getSysDate();
    String lsChk = commDate.dateAdd(lsValidDate, 0, -6, 0);
    if (lsChk.compareTo(lsCreateDate) > 0) {
      alertErr("效期需在系統日六個月內(" + lsChk + ")");
      return -1;
    }
    sqlSelect =
        " select line_of_credit_amt,chg_addr_date ,stat_send_paper,stat_send_internet  from dba_acno where p_seqno = :ls_p_seqno ";
    setString("ls_p_seqno", lspSeqno);
    sqlSelect(sqlSelect);
    double liActCreditAmt = sqlNum("line_of_credit_amt");
    String lsChgAddrDate = sqlStr("chg_addr_date");
    String lsSendPaper = sqlStr("stat_send_paper");
    String lsSendInternet = sqlStr("stat_send_internet");
    if (sqlRowNum <= 0) {
      return -1;
    }
    // --Get Batchno--
    String lsBatchno1 = strMid(lsCreateDate, 2, 6);
    sqlSelect =
        "select max(batchno) as ls_batchno from dbc_emboss_tmp where substr(batchno,1,6) = :ls_batchno1 and substr(batchno,7,2) < '85' ";
    setString("ls_batchno1", lsBatchno1);
    sqlSelect(sqlSelect);
    String lsBatchno = sqlStr("ls_batchno");
    if (empty(lsBatchno)) {
      lsBatchno = lsBatchno + "01";
    } else {
      sqlSelect = "select max(recno)+1 from dbc_emboss_tmp where batchno = :ls_batchno";
      setString("ls_batchno", lsBatchno);
      sqlSelect(sqlSelect);
      liRecno = sqlStr("li_recno");
    }
    String lsMailType = "";
    if (empty(liRecno) || liRecno.equals("0")) {
      liRecno = "1";
    }
    String lsIdnoValue1 = "", lsIdnoValue2 = "";
    sqlSelect = " select chi_name, birthday from dbc_idno where id_p_seqno = :ls_id_p_seqno ";
    setString("ls_id_p_seqno", lsIdpSeqno);
    sqlSelect(sqlSelect);
    lsIdnoValue1 = sqlStr("chi_name");
    lsIdnoValue2 = sqlStr("birthday");
    if (sqlRowNum <= 0) {
      alertErr("抓取卡人檔失敗");
      return -1;
    }

    // -- 抓取展期年
    int liExtn = wfGetExtnYear(aaUnitCode[as_row], aaCardType[as_row]);

    /*
     * sql_select="select AGE_INDICATOR from ptr_bintable where bin_no = substr(:ls_cardno,1,6) ";
     * setString("ls_cardno",ls_cardno); sqlSelect(sql_select);
     */// "AGE_INDICATOR" is not valid

    // -- 寫入dbc_emboss_tmp
    busi.SqlPrepare sp = new SqlPrepare();
    sqlSelect =
        "select  count(*) as cnt from dbc_emboss_tmp where batchno = :ls_batchno and recno = :li_recno";
    setString("ls_batchno", lsBatchno);
    setString("li_recno", liRecno);
    sqlSelect(sqlSelect);
    double cnt = sqlNum("cnt");
    if (cnt > 0) {
      sp.sql2Update("dbc_emboss_tmp");
      sp.ppstr("emboss_source", "4");
      sp.ppstr("apply_source", "C");
      sp.ppstr("to_nccc_code", "Y");
      sp.ppstr("mail_type", lsMailType);
      sp.ppstr("card_type", aaCardType[as_row]);
      sp.ppstr("unit_code", aaUnitCode[as_row]);
      sp.ppstr("status_code", "1");
      sp.ppstr("nccc_type", "2");
      sp.ppstr("change_reason", "1");
      sp.ppstr("card_no", lsCardno);
      sp.ppstr("old_card_no", aaCardNo[as_row]);
      sp.ppstr("reg_bank_no", aaRegBankNo[as_row]);
      sp.ppstr("risk_bank_no", "");
      // sp.ppss("status_code", "1");
      sp.ppstr("reason_code", "");
      sp.ppstr("apply_id", aaIdNo[as_row]);
      sp.ppstr("apply_id_code", aaIdNoCode[as_row]);
      sp.ppstr("ic_flag", aaIcFlag[as_row]);
      // --- 附卡寫入正卡資料
      if (aaSupFlag[as_row].equals("1")) {
        sp.ppstr("major_card_no", aaMajorCardNo[as_row]);
        sp.ppstr("pm_id", aaMajorIdNo[as_row]);
        sp.ppstr("pm_id_code", aaMajorIdNoCode[as_row]);
        sp.ppstr("major_valid_fm", ls_major_valid_fm);
        sp.ppstr("major_valid_to", ls_major_valid_to);
      } else {
        sp.ppstr("pm_id", aaMajorIdNo[as_row]);
        sp.ppstr("pm_id_code", aaMajorIdNoCode[as_row]);
      }
      sp.ppstr("emboss_4th_data", aaEmbossData[as_row]);
      sp.ppstr("group_code", aaGroupCode[as_row]);
      lsGroupCode = aaGroupCode[as_row];
      sp.ppstr("source_code", aaSourceCode[as_row]);
      sp.ppstr("corp_no", aaCorpNo[as_row]);
      sp.ppstr("acct_type", aaAcctType[as_row]);
      sp.ppstr("acct_key", aaAcctKey[as_row]);
      sp.ppstr("chi_name", lsIdnoValue1);
      sp.ppstr("eng_name", aaEngName[as_row]);
      sp.ppstr("birthday", lsIdnoValue2);
      // -- 產生新效期起為'舊有效期之迄日年月一 號' 2001/09/11 (mod)
      String lsEndVal = "", lsBegVal = "";
      if (aaSupFlag[as_row].equals("1")) {
        lsEndVal = ls_major_valid_to;
      } else {
        lsEndVal = aaNewEndDate[as_row];
      }
      lsBegVal = strMid(getSysDate(), 0, 6);
      lsEndVal = commDate.dateAdd(lsEndVal, liExtn, 0, 0);
      sp.ppstr("valid_fm", lsBegVal);
      sp.ppstr("valid_to", lsEndVal);
      sp.ppstr("sup_flag", aaSupFlag[as_row]);
      sp.ppnum("credit_lmt", liActCreditAmt);
      sp.ppstr("old_beg_date", aaNewBegDate[as_row]);
      sp.ppstr("old_end_date", aaNewEndDate[as_row]);
      sp.ppstr("force_flag", aaForceFlag[as_row]);
      sp.ppstr("third_rsn", "A");
      sp.ppstr("apply_ibm_id_code", aaIbmIdCode[as_row]);
      sp.ppstr("age_indicator", "");
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.ppstr("mod_user", wp.loginUser);
      sp.addsql(", crt_date = to_char(sysdate,'YYYYMMDD')");
      sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
      sp.addsql(", mod_time = sysdate");
      sp.sql2Where(" where batchno=?", lsBatchno);
      sp.sql2Where(" and recno=?", liRecno);
    } else {
      sp.sql2Insert("dbc_emboss_tmp");
      sp.ppstr("batchno", lsBatchno);
      sp.ppstr("recno", liRecno);

      sp.ppstr("emboss_source", "4");
      sp.ppstr("apply_source", "C");
      sp.ppstr("to_nccc_code", "Y");
      sp.ppstr("mail_type", lsMailType);
      sp.ppstr("card_type", aaCardType[as_row]);
      sp.ppstr("unit_code", aaUnitCode[as_row]);
      sp.ppstr("status_code", "1");
      sp.ppstr("nccc_type", "2");
      sp.ppstr("change_reason", "1");
      sp.ppstr("card_no", lsCardno);
      sp.ppstr("old_card_no", aaCardNo[as_row]);
      sp.ppstr("reg_bank_no", aaRegBankNo[as_row]);
      sp.ppstr("risk_bank_no", "");
      // sp.ppss("status_code", "1");
      sp.ppstr("reason_code", "");
      sp.ppstr("apply_id", aaIdNo[as_row]);
      sp.ppstr("apply_id_code", aaIdNoCode[as_row]);
      sp.ppstr("ic_flag", aaIcFlag[as_row]);
      // --- 附卡寫入正卡資料
      if (aaSupFlag[as_row].equals("1")) {
        sp.ppstr("major_card_no", aaMajorCardNo[as_row]);
        sp.ppstr("pm_id", aaMajorIdNo[as_row]);
        sp.ppstr("pm_id_code", aaMajorIdNoCode[as_row]);
        sp.ppstr("major_valid_fm", ls_major_valid_fm);
        sp.ppstr("major_valid_to", ls_major_valid_to);
      } else {
        sp.ppstr("pm_id", aaMajorIdNo[as_row]);
        sp.ppstr("pm_id_code", aaMajorIdNoCode[as_row]);
      }
      sp.ppstr("emboss_4th_data", aaEmbossData[as_row]);
      sp.ppstr("group_code", aaGroupCode[as_row]);
      lsGroupCode = aaGroupCode[as_row];
      sp.ppstr("source_code", aaSourceCode[as_row]);
      sp.ppstr("corp_no", aaCorpNo[as_row]);
      sp.ppstr("acct_type", aaAcctType[as_row]);
      sp.ppstr("acct_key", aaAcctKey[as_row]);
      sp.ppstr("chi_name", lsIdnoValue1);
      sp.ppstr("eng_name", aaEngName[as_row]);
      sp.ppstr("birthday", lsIdnoValue2);
      // -- 產生新效期起為'舊有效期之迄日年月一 號' 2001/09/11 (mod)
      String lsEndVal = "", lsBegVal = "";
      if (aaSupFlag[as_row].equals("1")) {
        lsEndVal = ls_major_valid_to;
      } else {
        lsEndVal = aaNewEndDate[as_row];
      }
      lsBegVal = strMid(getSysDate(), 0, 6);
      lsEndVal = commDate.dateAdd(lsEndVal, liExtn, 0, 0);
      sp.ppstr("valid_fm", lsBegVal);
      sp.ppstr("valid_to", lsEndVal);
      sp.ppstr("sup_flag", aaSupFlag[as_row]);
      sp.ppnum("credit_lmt", liActCreditAmt);
      sp.ppstr("old_beg_date", aaNewBegDate[as_row]);
      sp.ppstr("old_end_date", aaNewEndDate[as_row]);
      sp.ppstr("force_flag", aaForceFlag[as_row]);
      sp.ppstr("third_rsn", "A");
      sp.ppstr("apply_ibm_id_code", aaIbmIdCode[as_row]);
      sp.ppstr("age_indicator", "");
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.ppstr("mod_user", wp.loginUser);
      sp.addsql(", crt_date ", ", to_char(sysdate,'YYYYMMDD')");
      sp.addsql(", mod_seqno ", ", 1");
      sp.addsql(", mod_time ", ", sysdate");
    }

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      alertErr("此資料無法搬到續卡檔內");
      return -1;
    }

    String sqlUpdate = " update dbc_card set " + "expire_chg_flag = '' " + ",expire_chg_date = '' "
        + ",expire_reason = '' " + ",change_reason = '1' "
        + ",change_date = to_char(sysdate,'yyyymmdd') " + ",change_status = '1' "
        + ",cancel_expire_chg_date = to_char(sysdate,'yyyymmdd') "
        + "where card_no = :ls_cardno ";
    setString("ls_cardno", lsCardno);
    sqlExec(sqlUpdate);
    if (sqlRowNum <= 0) {
      alertErr("寫入卡片檔錯誤~");
      return -1;
    }

    return 1;
  }

  int wfGetExtnYear(String asUnitCode, String as_card_type) {
    if (empty(asUnitCode)) {
      asUnitCode = "0000";
    }
    String sqlSelect = "select extn_year " + "from crd_item_unit "
        + "where unit_code = :as_unit_code " + "and card_type = :as_card_type ";
    setString("as_unit_code", asUnitCode);
    setString("as_card_type", as_card_type);
    sqlSelect(sqlSelect);
    int li_year = this.toInt(sqlStr("extn_year"));
    if (sqlRowNum <= 0) {
      alertErr("抓取不到展期年~");
      return -1;
    }
    return li_year;
  }

}
