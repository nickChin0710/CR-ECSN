/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2020-03-31 V1.00.01  shiyuqi    Bug Fix                                    *
* 2020-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*
* 2020-12-23  V1.00.03  Justin       amend comments
** 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
* 111-03-29   V1.00.04   Ryan      覆核人員與異動人員相同不能覆核                      *  
******************************************************************************/
package dbam01;

import busi.SqlPrepare;
import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

//import javax.swing.JOptionPane;

public class Dbap0030 extends BaseProc {
  CommString commString = new CommString();

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
  public void dddwSelect() {
    try {
      wp.initOption = "";
      wp.optionKey = wp.itemStr("exActype");
      dddwList("DbpAcctTypeList", "dbp_acct_type", "acct_type", "acct_type||' ['||chin_name||']'",
          "where 1=1 order by acct_type");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("exCrtUser");
      dddwList("SecUserIDNameList", "sec_user", "usr_id", "usr_id||' ['||usr_cname||']'",
          "where 1=1 order by usr_id"); // "where usr_type = '4' order by usr_id");
    } catch (Exception ex) {
    }
  }

  private boolean getWhereStr() throws Exception {

    wp.whereStr = "where 1=1 " + "and dba_acaj.p_seqno = dba_acno.p_seqno "
        + "and (substr (dba_acaj.adjust_type, 1, 2) <> 'OP') "
        + "and (dba_acaj.adjust_type <> 'AI01') " + "and (dba_acaj.adjust_type > 'DE00'  "
        + "and dba_acaj.adjust_type < 'DE15') " + "and (dba_acaj.adjust_type <> 'DE02' "
        + "and dba_acaj.adjust_type <> 'DE05' " + "and dba_acaj.adjust_type <> 'DE06' "
        + "and dba_acaj.adjust_type <> 'DE10' " + "and dba_acaj.adjust_type <> 'DE11' "
        + "and dba_acaj.adjust_type <> 'DE12') " + "and dba_acaj.proc_flag = 'N' ";

    if (empty(wp.itemStr("exAcctType")) == false) {
      wp.whereStr += " and dba_acaj.acct_type = :acct_type ";
      setString("acct_type", wp.itemStr("exAcctType"));
    }
    if (empty(wp.itemStr("exAcctKey")) == false) {
      wp.whereStr += " and dba_acno.acct_key = :acct_key ";
      setString("acct_key", fillZeroAcctKey(wp.itemStr("exAcctKey")));
    }
    if (eqIgno(wp.itemStr("exAprFlag"), "1")) {
      wp.whereStr += " and dba_acaj.apr_flag <>'Y' ";
    } else {
      wp.whereStr += " and dba_acaj.apr_flag = 'Y' ";
    }
    if (empty(wp.itemStr("exCrtUser")) == false) {
      wp.whereStr += " and dba_acaj.chg_user = :chg_user ";
      setString("chg_user", wp.itemStr("exCrtUser"));
    }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.whereOrder = "order by dba_acaj.crt_date, dba_acaj.crt_time ";

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    queryRead();
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
  }

  @Override
  public void queryRead() throws Exception {
    wp.setQueryMode();
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "hex(dba_acaj.rowid) as rowid, " + "dba_acaj.crt_date, " + "dba_acaj.crt_time, "
        + "dba_acaj.p_seqno, " + "dba_acaj.acct_type, " + "dba_acno.acct_key, "
        + "dba_acaj.adjust_type, " + "dba_acaj.reference_no, " + "dba_acaj.post_date, "
        + "dba_acaj.orginal_amt, " + "dba_acaj.dr_amt, " + "dba_acaj.cr_amt, "
        + "dba_acaj.bef_amt, " + "dba_acaj.aft_amt, " + "dba_acaj.bef_d_amt, "
        + "dba_acaj.aft_d_amt, " + "dba_acaj.acct_code, " + "dba_acaj.func_code, "
        + "dba_acaj.card_no, " + "dba_acaj.cash_type, " + "dba_acaj.value_type, "
        + "dba_acaj.trans_acct_type, " + "dba_acaj.trans_acct_key, " + "dba_acaj.interest_date, "
        + "dba_acaj.adj_reason_code, " + "dba_acaj.adj_comment, " + "dba_acaj.c_debt_key, "
        + "dba_acaj.debit_item, " + "dba_acaj.chg_date, " + "dba_acaj.chg_user, "
        + "dba_acaj.mod_user, " + "dba_acaj.mod_time, " + "dba_acaj.mod_pgm, "
        + "dba_acaj.mod_seqno, " + "dba_acno.acct_holder_id as id_no, "
        + "dba_acno.acct_holder_id_code as id_code, " + "nvl(dbc_idno.chi_name,'') as idno_name, "
        + "nvl(crd_corp.chi_name,'') as corp_name, " + "dba_acaj.purchase_date, "
        + "dba_acaj.apr_flag, " + "dba_acaj.apr_user, " + "dba_acaj.apr_date ";

    wp.daoTable =
        "dba_acaj, dba_acno " + "left join dbc_idno on dbc_idno.id_p_seqno = dba_acno.id_p_seqno "
            + "left join crd_corp on crd_corp.corp_p_seqno = dba_acno.corp_p_seqno ";

    pageQuery();
    wp.setListCount(1);
    ofcRetrieve();
    wp.setPageValue();
    apprDisabled("mod_user");
  }

  // ofc_retrieve說明:
  // 1. 取得帳務科目中文簡稱。
  // 2. 取得中文姓名(或公司名稱)、身分證號。
  void ofcRetrieve() throws Exception {
    String param = "";
    // String ss2 = "", ss3 = "";
    // String[] sArr = new String[]{"","",""};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      param = wp.colStr(ii, "apr_flag");
      wp.colSet(ii, "tt_apr_flag", commString.decode(param, ",Y,N", ",解放行,待放行"));

      param = wp.colStr(ii, "value_type");
      wp.colSet(ii, "tt_value_type", commString.decode(param, ",1,2", ",原起息日,覆核日"));

      // 取得帳務科目中文簡稱
      param = wp.colStr(ii, "acct_code");
      wp.colSet(ii, "debt_chi", wfGetDebtChiName(param));

      // 取得中文姓名(或公司名稱)、身分證號等 //太慢了, 改成left join
      // ss =wp.col_ss(ii,"acct_type");
      // ss2=wp.col_ss(ii,"acct_key");
      // ss3=wp.col_ss(ii,"p_seqno");
      // sArr = wf_get_extr_data(ss,ss2,ss3);
      // wp.col_set(ii,"id_no", sArr[0]);
      // wp.col_set(ii,"id_code", sArr[1]);
      // wp.col_set(ii,"chi_name", sArr[2]);
      param = wp.colStr(ii, "idno_name");
      if (!empty(param)) {
        wp.colSet(ii, "chi_name", param);
      } else {
        wp.colSet(ii, "chi_name", wp.colStr(ii, "corp_name"));
      }
    }
  }

  String wfGetDebtChiName(String idcode) throws Exception {
    String rtn = "";
    String lsSql = "select chi_short_name from ptr_actcode " + "where acct_code = :acct_code ";
    setString("acct_code", idcode);
    sqlSelect(lsSql);
    if (sqlRowNum > 0)
      rtn = sqlStr("chi_short_name");

    return rtn;
  }

  // 說明: 取得中文姓名(或公司名稱)、身分證號等。
  String[] wfGetExtrData(String asAcctType, String asAcctKey, String asPSeqno) throws Exception {
    String lsIdPSeqno = "", lsCorpPSeqno = "";
    String[] rtnArr = new String[] {"", "", ""};

    // Get data form dba_acno
    String lsSql =
        "select id_p_seqno, acct_holder_id, acct_holder_id_code, corp_p_seqno from dba_acno "
            + "where p_seqno = :p_seqno and acct_type = :acct_type and acct_key = :acct_key ";
    setString("p_seqno", asPSeqno);
    setString("acct_type", asAcctType);
    setString("acct_key", asAcctKey);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      rtnArr[0] = sqlStr("acct_holder_id");
      rtnArr[1] = sqlStr("acct_holder_id_code");
      lsIdPSeqno = sqlStr("id_p_seqno");
      lsCorpPSeqno = sqlStr("corp_p_seqno");
    }

    // Get id chinese name
    if (!empty(lsIdPSeqno)) {
      lsSql = "select chi_name from dbc_idno where id_p_seqno = :id_p_seqno ";
      setString("id_p_seqno", lsIdPSeqno);
      sqlSelect(lsSql);
      rtnArr[2] = sqlStr("chi_name");
    }

    // Get corp chinese name
    if (!empty(lsCorpPSeqno)) {
      lsSql = "select chi_name from crd_corp where corp_p_seqno = :corp_p_seqno ";
      setString("corp_p_seqno", lsCorpPSeqno);
      sqlSelect(lsSql);
      rtnArr[2] = sqlStr("chi_name");
    }

    return rtnArr;
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
    String[] lsRowid = wp.itemBuff("rowid");
    int rowcntaa = 0;
    if (!(lsRowid == null) && !empty(lsRowid[0]))
      rowcntaa = lsRowid.length;
    wp.listCount[0] = rowcntaa;

    if (wfValidation() < 0)
      return;

    // 先移到前端顯示訊息, 待有解法再處理
    // javax.swing.JOptionPane.getRootFrame().setAlwaysOnTop(true);
    // if (JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
    // "是否執行科目明細調整放行/解放行 !", "信息",JOptionPane.YES_NO_OPTION)!=0) {
    // return -1;
    // }

    if (ofcProcess() != 1) {
      sqlCommit(0);
      return;
    }
    sqlCommit(1);
    // 預測出現資料錯誤原因：在更新數據之後 又用原先的條件進行查詢所以報錯 資料錯誤: 此條件查無資料
    queryRead();
    wp.dispMesg = "資料處理成功!";
  }

  int wfValidation() throws Exception {
    int liCnt = 0, tt = 0;
    String[] lsReferenceNo = wp.itemBuff("reference_no");
    String[] opt = wp.itemBuff("opt");

    // for (int ll = 0; ll < ls_reference_no.length; ll++) {
    // if ((checkBox_opt_on(ll, opt) == false)) {
    // continue;
    // }

    int rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      tt = rr;
      liCnt = 0;
      // 2018.5.8 有換頁的opt處理, 須扣掉(行數 x 頁數)//
      rr = rr - (wp.pageRows * (wp.currPage - 1));
      // 2018.5.8 end//
      if (rr < 0)
        continue;

      String lsSql =
          "select count(*) as li_cnt from dba_debt " + "where reference_no = :reference_no ";
      setString("reference_no", lsReferenceNo[rr]);
      sqlSelect(lsSql);
      liCnt = (int) sqlNum("li_cnt");
      if (liCnt == 0) {
        wp.colSet(tt, "ok_flag", "!");
        alertErr("此筆資料在帳務檔找不到，無法放行！");
        return -1;
      }
    }
    return 1;
  }

  int ofcProcess() throws Exception {
    busi.SqlPrepare sp = new SqlPrepare();
    String[] lsRowid = wp.itemBuff("rowid");
    String[] lsAprFlag = wp.itemBuff("apr_flag");
    String[] lsModSeqno = wp.itemBuff("mod_seqno");
    String[] opt = wp.itemBuff("opt");

    int rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      // 2018.5.8 有換頁的opt處理, 須扣掉(行數 x 頁數)//
      rr = rr - (wp.pageRows * (wp.currPage - 1));
      // 2018.5.8 end//
      if (rr < 0)
        continue;

      sp.sql2Update("dba_acaj");
      sp.ppstr("apr_flag", lsAprFlag[rr].equals("Y") ? "N" : "Y");
      sp.ppstr("apr_user", wp.loginUser);
      sp.ppstr("apr_date", wp.sysDate);
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
      sp.sql2Where(" where rowid = ?", wp.hexStrToByteArr(lsRowid[rr]));
      sp.sql2Where(" and nvl(mod_seqno,0) = ?", lsModSeqno[rr]);
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum < 0) {
        alertErr("update dba_acaj error");
        return -1;
      }
    }
    return 1;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  String fillZeroAcctKey(String acctkey) throws Exception {
    String rtn = acctkey;
    if (acctkey.trim().length() == 8)
      rtn += "000";
    if (acctkey.trim().length() == 10)
      rtn += "0";

    return rtn;
  }
  
	public void apprDisabled(String col) throws Exception {
		for (int ll = 0; ll < wp.listCount[0]; ll++) {
			if (!wp.colStr(ll, col).equals(wp.loginUser)) {
				wp.colSet(ll, "opt_disabled", "");
			} else
				wp.colSet(ll, "opt_disabled", "disabled");
		}
	}

}
