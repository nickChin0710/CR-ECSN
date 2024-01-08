/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-16  V1.00.00  ryan       program initial                            *
* 109-04-21  V1.00.01  shiyuqi       updated for project coding standard     * 																			 *
******************************************************************************/
package actp01;

import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Actp1130 extends BaseProc {
  int rr = -1;
  String msg = "";
  String isFileName = "", idCname = "";
  long talSumEndBal = 0, talSumAdiEndBal = 0;
  int ilOk = 0;
  int ilErr = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
  public void initPage() {}

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("ex_actype");
      dddwList("dddw_ex_actype", "ptr_acct_type", "acct_type", "chin_name",
          "where 1=1 order by acct_type");
      wp.optionKey = wp.itemStr("ex_file_name");
      dddwList("dddw_file_name", "act_npl_corp", "file_name",
          "file_name||'_'||sale_date||'_'||corp_no||'_'||corp_cname",
          "where 1=1 order by file_name");

    } catch (Exception ex) {
    }
  }

  private int getWhereStr() throws Exception {
    String lsAckey = "";
    lsAckey = wp.itemStr("ex_acct_key");

    switch (lsAckey.length()) {
      case 11:
        break;
      case 10:
        lsAckey += "0";
        break;
      case 8:
        lsAckey += "000";
        break;
      default:
        alertErr("帳戶帳號輸入錯誤~");
        return -1;
    }
    if (wfGetIdName(lsAckey) != 1) {
      idCname = "";
    }
    // -join table---
    wp.whereStr = " where 1=1 and  ( act_debt_npl.p_seqno = act_npl_log.p_seqno ) "
        + " and  ( act_debt_npl.file_name = act_npl_log.file_name ) "
        + " and ( act_debt_npl.corp_no = act_npl_log.corp_no ) ";

    if (empty(wp.itemStr("ex_actype").trim()) == false) {
      wp.whereStr += " and act_debt_npl.acct_type = :ex_actype ";
      setString("ex_actype", wp.itemStr("ex_actype"));
    }
    if (empty(lsAckey) == false) {
      wp.whereStr += " and act_debt_npl.acct_key = :ex_acct_key ";
      setString("ex_acct_key", lsAckey);
    }
    /*
     * if (empty(wp.item_ss("corp_no")) == false) { wp.whereStr +=
     * " and act_debt_npl.corp_no = :corp_no "; setString("corp_no", wp.item_ss("corp_no"));
     * 
     * }
     */
    if (empty(wp.itemStr("ex_file_name").trim()) == false) {
      wp.whereStr += " and act_debt_npl.file_name = :ex_file_name ";
      setString("ex_file_name", wp.itemStr("ex_file_name"));
    }
    isFileName = wp.itemStr("ex_file_name");
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
    wp.colSet("tal_sum_end_bal", talSumEndBal + "");
    wp.colSet("tal_sum_adi_end_bal", talSumAdiEndBal + "");
    wp.colSet("id_cname", idCname);
    wp.colSet("rowcount", wp.selectCnt + "");
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " act_debt_npl.p_seqno, " + " act_debt_npl.acct_code, "
        + " act_debt_npl.file_name, " + " act_debt_npl.corp_no, "
        + " sum(act_debt_npl.end_bal) sum_end_bal, " + " act_debt_npl.acct_type, "
        + " act_debt_npl.acct_key, " + " rpad(' ',8,' ') db_sale_date, "
        + " rpad(' ',20,' ') db_id_cname, " + " 'N' db_cancel_flag, "
        + " rpad(' ',8,' ') db_cancel_date, " + " sum(act_npl_log.adi_end_bal) sum_adi_end_bal, "
        + " act_npl_log.cancel_flag, " + " act_npl_log.cancel_date ";
    wp.daoTable = " act_debt_npl,act_npl_log ";
    wp.whereOrder = " group by " + " act_debt_npl.p_seqno, " + " act_debt_npl.acct_code, "
        + " act_debt_npl.file_name, " + " act_debt_npl.corp_no, " + " act_debt_npl.acct_type, "
        + " act_debt_npl.acct_key, " + " act_npl_log.cancel_flag, " + " act_npl_log.cancel_date ";
    if (getWhereStr() != 1)
      return;

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      wp.colSet("sale_date", "");
      wp.colSet("corp_no", "");
      wp.colSet("corp_cname", "");
      wp.colSet("wk_corp_cname", "");
      wp.colSet("rowid", "");
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();

  }

  void listWkdata() throws Exception {
    String lsFileName1 = "", lsFileName2 = "", lsCorpNo1 = "", lsCorpNo2 = "", lsSaleDate = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      lsFileName1 = wp.colStr(ii, "file_name");
      lsCorpNo2 = wp.colStr(ii, "corp_no");
      if (lsFileName1.equals(lsFileName2) == false || lsCorpNo1.equals(lsCorpNo2) == false) {
        lsFileName1 = lsFileName2;
        lsCorpNo1 = lsCorpNo2;
      }
      String sqlSelect = " SELECT sale_date " + " FROM act_npl_corp "
          + " WHERE ( file_name = :ls_file_name1 ) " + " AND  ( corp_no = :ls_corp_no1 ) ";
      setString("ls_file_name1", lsFileName1);
      setString("ls_corp_no1", lsCorpNo1);
      sqlSelect(sqlSelect);
      lsSaleDate = sqlStr("sale_date");
      if (sqlRowNum <= 0) {
        lsSaleDate = "";
      }
      wp.colSet(ii, "db_id_cname", idCname);
      wp.colSet(ii, "db_sale_date", lsSaleDate);
      talSumEndBal += wp.colNum(ii, "sum_end_bal");
      talSumAdiEndBal += wp.colNum(ii, "sum_adi_end_bal");

    }
    wfReadNplCorp(wp.colStr(0, "file_name"));
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
     * if (!check_approve(wp.item_ss("approval_user"),wp.item_ss("approval_passwd"))) { return; }
     */
    String[] serNum = wp.itemBuff("ser_num");
    wp.listCount[0] = serNum.length;
    String lsFileName = "", lsCorpNo = "", lsApprFlag = "";
    String lsActype = "", lsAckey = "", lsUserid = "";
    if (wp.itemStr("rowcount").equals("0") || empty(wp.itemStr("rowcount"))) {
      alertErr("持卡人未出售債權, 不可執行退回");
      return;
    }
    if (wp.itemStr("cancel_flag").equals("Y")) {
      alertErr("此帳戶已退回, 不可再執行退回作業~");
      return;
    }
    String sqlSelect =
        "select file_name, corp_no ,apr_flag from act_npl_corp where hex(rowid) = :is_rowid ";
    setString("is_rowid", wp.itemStr("rowid"));
    sqlSelect(sqlSelect);
    if (sqlRowNum <= 0) {
      alertErr("查無債權管理公司資料");
      return;
    }

    lsFileName = sqlStr("file_name");
    lsCorpNo = sqlStr("corp_no");
    lsApprFlag = sqlStr("appr_flag");

    if (empty(lsApprFlag)) {
      lsApprFlag = "N";
    }
    lsActype = wp.itemStr("acct_type");
    lsAckey = wp.itemStr("acct_key");
    lsUserid = wp.loginUser;

    sqlSelect = "select count(*) as cnt from ecs_act_acno where acno_p_seqno = :p_seqno ";
    setString("p_seqno", wp.itemStr("p_seqno"));
    sqlSelect(sqlSelect);
    if (sqlNum("cnt") > 0) {
      alertErr("此人已瘦身，請先解瘦身後再處理");
      return;
    }

    // --Update act_debt_npl---------------------
    if (lsApprFlag.equals("N")) {
      String sqlDelete =
          " Delete act_debt_npl " + " where acct_type= :ls_actype " + " and acct_key= :ls_ackey "
              + " and	corp_no= :ls_corp_no " + " and file_name= :ls_file_name ";
      setString("ls_actype", lsActype);
      setString("ls_ackey", lsAckey);
      setString("ls_corp_no", lsCorpNo);
      setString("ls_file_name", lsFileName);
      sqlExec(sqlDelete);
    } else {
      String sqlUpdate = " UPDATE act_debt_npl SET " + " cancel_flag = 'Y', "
          + " mod_user = :ls_userid, " + " mod_time = sysdate, " + " mod_pgm = 'actp1130', "
          + " mod_seqno = mod_seqno + 1 " + " WHERE ( act_debt_npl.acct_type = :ls_actype ) "
          + " AND ( act_debt_npl.acct_key = :ls_ackey ) "
          + " AND ( act_debt_npl.file_name = :ls_file_name ) "
          + " AND ( act_debt_npl.corp_no = :ls_corp_no ) ";
      setString("ls_userid", lsUserid);
      setString("ls_actype", lsActype);
      setString("ls_ackey", lsAckey);
      setString("ls_corp_no", lsCorpNo);
      setString("ls_file_name", lsFileName);
      sqlExec(sqlUpdate);
    }
    if (sqlRowNum <= 0) {
      alertErr("債權出售退回處理失敗,Update act_debt_npl error");
      sqlCommit(0);
      return;
    }
    // --Update act_npl_log---------------------
    if (lsApprFlag.equals("N")) {
      String sqlDelete = " delete act_npl_log " + " where acct_type= :ls_actype "
          + " and corp_no= :ls_corp_no " + " and file_name= :ls_file_name ";
      setString("ls_actype", lsActype);
      setString("ls_corp_no", lsCorpNo);
      setString("ls_file_name", lsFileName);
      sqlExec(sqlDelete);

    } else {
      String sqlUpdate = " UPDATE act_npl_log SET " + " cancel_flag = 'Y', "
          + " cancel_date = to_char(sysdate,'yyyymmdd'), " + " cancel_id = :ls_userid, "
          + " mod_user = :ls_userid, " + " mod_time = sysdate, " + " mod_pgm = 'actp1130', "
          + " mod_seqno = mod_seqno + 1 " + " WHERE acct_type = :ls_actype "
          + " AND corp_no= :ls_corp_no " + " and file_name= :ls_file_name ";
      setString("ls_userid", lsUserid);
      setString("ls_userid", lsUserid);
      setString("ls_actype", lsActype);
      setString("ls_corp_no", lsCorpNo);
      setString("ls_file_name", lsFileName);
      sqlExec(sqlUpdate);

    }
    if (sqlRowNum <= 0) {
      alertErr("債權出售退回處理失敗,Update act_npl_log error");
      sqlCommit(0);
      return;
    }
    // --Update act_acno------------------
    if (lsApprFlag.equals("Y")) {
      String sqlUpdate = " UPDATE act_acno SET " + " sale_flag = '', " + " sale_date = '', "
          + " mod_user = :ls_userid, " + " mod_time = sysdate, " + " mod_pgm = 'actp1130', "
          + " mod_seqno = mod_seqno + 1 " + " WHERE acct_type = :ls_actype "
          + " and acct_key = :ls_ackey ";

      setString("ls_userid", lsUserid);
      setString("ls_actype", lsActype);
      setString("ls_ackey", lsAckey);
      sqlExec(sqlUpdate);

    }
    if (sqlRowNum < 0) {
      alertErr("債權出售退回處理失敗,Update act_acno error");
      sqlCommit(0);
      return;
    }
    sqlCommit(1);
    alertMsg("債權出售退回處理成功");
  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }

    String sKey = "1st-page";
    if (wp.respHtml.equals("actp1130")) {
      wp.colSet("btnUpdate_disable", "");
      this.btnModeAud(sKey);
    }

  }

  int wfGetIdName(String asId) {
    String lsId = "", lsIdcode = "", lsCname = "";
    switch (asId.trim().length()) {
      case 8:
        lsId = asId.trim();
        String sqlSelect = "select chi_name from	crd_corp where	corp_no = :ls_id";
        setString("ls_id", lsId);
        sqlSelect(sqlSelect);
        lsCname = sqlStr("chi_name");
        break;
      case 10:
        lsId = (asId.trim() + "0").substring(0, 10);
        lsIdcode = (asId.trim() + "0").substring(10, 11);
        String sqlSelect2 =
            "select chi_name from crd_idno where	id_no = :ls_id and id_no_code = :ls_idcode ";
        setString("ls_id", lsId);
        setString("ls_idcode", lsIdcode);
        sqlSelect(sqlSelect2);
        lsCname = sqlStr("chi_name");
        break;
      case 11:
        lsId = (asId.trim() + "0").substring(0, 10);
        lsIdcode = (asId.trim() + "0").substring(10, 11);
        String sqlSelect3 =
            "select chi_name from crd_idno where	id_no = :ls_id and id_no_code = :ls_idcode ";
        setString("ls_id", lsId);
        setString("ls_idcode", lsIdcode);
        sqlSelect(sqlSelect3);
        lsCname = sqlStr("chi_name");
        break;
      default:
        wp.alertMesg = "<script language='javascript'> alert('身分證號 輸失錯誤~')</script>";
        return -1;
    }
    idCname = lsCname;

    return 1;
  }

  public void wfReadNplCorp(String lsFile) throws Exception {
    if (empty(lsFile)) {
      wp.colSet("sale_date", "");
      wp.colSet("corp_no", "");
      wp.colSet("corp_cname", "");
      wp.colSet("wk_corp_cname", "");
      wp.colSet("rowid", "");
    }
    String sqlSelect =
        "select hex(rowid) as rowid, sale_date, corp_no, corp_cname from act_npl_corp where file_name =:ls_file";
    setString("ls_file", lsFile);
    sqlSelect(sqlSelect);
    if (sqlRowNum <= 0) {
      wp.alertMesg = "<script language='javascript'> alert('查無出售批次')</script>";
      wp.colSet("sale_date", "");
      wp.colSet("corp_no", "");
      wp.colSet("corp_cname", "");
      wp.colSet("wk_corp_cname", "");
      wp.colSet("rowid", "");
      return;
    }
    wp.colSet("rowid", sqlStr("rowid"));
    wp.colSet("sale_date", sqlStr("sale_date"));
    wp.colSet("corp_no", sqlStr("corp_no"));
    wp.colSet("corp_cname", sqlStr("corp_cname"));
    wp.colSet("wk_corp_cname", sqlStr("corp_no") + "[" + sqlStr("corp_cname") + "]");
  }

}
