/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-15  V1.00.00  yash       program initial                            *
* 107-03-13  V1.00.01  Andy       Update dddw_list merchant UI               *
* 109-04-23  V1.00.02  shiyuqi       updated for project coding standard     *  
* 111-05-31  V1.00.03  Ryan       增加異動人員與登入人員相同時不能覆核                                                    * 
******************************************************************************/

package bilp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilp0330 extends BaseEdit {
  String mExSeqNo = "";
  String mExMchtNo = "";
  String errmsg = "";

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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1  and c.confirm_flag <> 'Y' ";

    if (empty(wp.itemStr("ex_merchant")) == false) {
      wp.whereStr += " and  c.mcht_no like :ex_merchant ";
      setString("ex_merchant", wp.itemStr("ex_merchant") + "%");
    }

    if (empty(wp.itemStr("ex_user")) == false) {
      wp.whereStr += " and  c.mod_user like :ex_user ";
      setString("ex_user", wp.itemStr("ex_user") + "%");
    }

    if (empty(wp.itemStr("exDateS")) == false) {
      wp.whereStr += " and  uf_2ymd(c.mod_time) >= :exDateS ";
      setString("exDateS", wp.itemStr("exDateS"));
    }

    if (empty(wp.itemStr("exDateE")) == false) {
      wp.whereStr += " and  uf_2ymd(c.mod_time) <= :exDateE ";
      setString("exDateE", wp.itemStr("exDateE"));
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

    wp.selectSQL = " c.mcht_no" + ", c.seq_no" + ", c.confirm_user" + ", c.confirm_flag"
        + ", c.confirm_date" + ", c.copy_flag" + ", c.mod_user" + ", c.mod_time" + ", c.mod_pgm"
        + ", m.mcht_chi_name" + ", hex(c.rowid) as rowid, c.mod_seqno";

    wp.daoTable = "bil_prod_copy_mas c left join bil_merchant m on c.mcht_no=m.mcht_no";
    wp.whereOrder = " order by c.mcht_no";
//    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    apprDisabled("mod_user");
  }

  @Override
  public void querySelect() throws Exception {
    mExSeqNo = wp.itemStr("seq_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.pageRows = 999;
    mExMchtNo = itemKk("data_k1");
    mExSeqNo = itemKk("data_k2");
    wp.pageControl();
    wp.selectSQL =
        "hex(a.rowid) as rowid " + " , a.seq_no" + " , a.mcht_no_cpy " + " , b.mcht_chi_name ";
    wp.daoTable =
        "bil_prod_copy_dtl as a left join bil_merchant as b on a.mcht_no_cpy = b.mcht_no ";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  a.mcht_no = :mcht_no ";
    wp.whereStr += " and  a.seq_no = :seq_no ";
    setString("mcht_no", mExMchtNo);
    setString("seq_no", mExSeqNo);
    wp.whereOrder = " order by a.mcht_no_cpy ";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr("mcht_no_cpy資料檔查無資料!!");
      // alert_err(AppMsg.err_condNodata);
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    wp.sqlCmd = "select hex(a.rowid) as rowid, a.mod_seqno " + " , a.mcht_no" + " , b.mcht_chi_name"
        + " , b.mcht_type" + " , a.seq_no" + " , a.copy_flag" + " , a.confirm_flag"
        + " , a.confirm_date" + " , uf_2ymd(a.mod_time) as mod_date" + " , a.mod_user "
        + "from bil_prod_copy_mas a left JOIN bil_merchant b on a.mcht_no = b.mcht_no "
        + "where 1=1 " + "and  a.mcht_no = :mcht_no " + "and  a.seq_no = :seq_no ";
    setString("mcht_no", mExMchtNo);
    setString("seq_no", mExSeqNo);
    sqlSelect(wp.sqlCmd);
    wp.colSet("mcht_no1", sqlStr("mcht_no"));
    wp.colSet("mcht_chi_name1", sqlStr("mcht_chi_name"));
    wp.colSet("mcht_type", sqlStr("mcht_type"));
    wp.colSet("seq_no1", sqlStr("seq_no"));
    wp.colSet("copy_flag", sqlStr("copy_flag"));
    wp.colSet("confirm_flag", sqlStr("confirm_flag"));
    wp.colSet("confirm_date", sqlStr("confirm_date"));
  }

  @Override
  public void saveFunc() throws Exception {

    int llOk = 0, llErr = 0;
    if (strAction.equals("S2")) {

      String[] aaOpt = wp.itemBuff("opt");
      String[] aaMchtNo = wp.itemBuff("mcht_no");
      String[] aaSeqNo = wp.itemBuff("seq_no");
      wp.listCount[0] = aaMchtNo.length;
      int rr = -1;
      int rcn = 0;
      for (int ll = 0; ll < aaOpt.length; ll++) {

        rr = optToIndex(aaOpt[ll]);
        if (rr < 0) {
          continue;
        }

        rcn = wfUpdFile(aaMchtNo[rr], aaSeqNo[rr]);
        if (rcn == 1) {
          llOk++;
          wp.colSet(rr, "ok_flag", "V");
          sqlCommit(1);
        } else {
          llErr++;
          wp.colSet(rr, "ok_flag", "X");
          sqlCommit(0);
        }

      }

    }
    alertMsg("放行資料處理完成; OK = " + llOk + ", ERR = " + llErr);

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
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_merchant");
      this.dddwList("dddw_merchant", "bil_merchant", "mcht_no", "mcht_chi_name",
          "where 1=1 order by mcht_no");
    } catch (Exception ex) {
    }
  }

  public int wfUpdFile(String mchtNo, String seqNo) throws Exception {

    String lsSqld = " select * from ptr_businday ";
    sqlSelect(lsSqld);
    String sdaye = sqlStr("business_date");

    String lsSql = " update bil_prod_copy_mas set ";
    lsSql += " confirm_date=:confirm_date,confirm_flag=:confirm_flag,copy_flag=:copy_flag,  ";
    lsSql +=
        " mod_user =:mod_user,mod_time=sysdate, mod_pgm =:mod_pgm , mod_seqno =nvl(mod_seqno,0)+1   ";
    lsSql += "  where mcht_no=:mcht_no and seq_no=:seq_no  ";
    setString("confirm_date", sdaye);
    setString("confirm_flag", "Y");
    setString("copy_flag", "N");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.itemStr("mod_pgm"));
    setString("mcht_no", mchtNo);
    setString("seq_no", seqNo);

    sqlExec(lsSql);

    return sqlRowNum;

  }

  public void processAjaxOption() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    if (wp.respHtml.indexOf("_detl") > 0) {
      setString("mcht_no", wp.getValue("mcht_no", 0) + "%");
    } else {
      setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }
    return;
  }
  
//	void apprDisabled(String col) throws Exception {
//		for (int ll = 0; ll < wp.listCount[0]; ll++) {
//			if (!wp.colStr(ll, col).equals(wp.loginUser)) {
//				wp.colSet(ll, "opt_disabled", "");
//			} else
//				wp.colSet(ll, "opt_disabled", "disabled");
//		}
//	}
}
