package cmsm01;
/** VIP 卡戶異動記錄查詢
 * 19-1230:    Alex  code->chinese , usercname
 * 19-0613:    JH    p_xxx >>acno_pxxx
 * 2018-0821:	JH		modify
 * v01.00 Alex 107-05-14
 * 109-04-27  V1.00.03  shiyuqi       updated for project coding standard     * 
 * 109-12-25   V1.00.04  Justin         parameterize sql 
 * * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      * 
 * */

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;

import taroko.com.TarokoExcel;

public class Cmsq0040 extends BaseAction implements InfaceExcel {

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
    }
    // else if (eq_igno(wp.buttonCode, "A")) {
    // /* 新增功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "U")) {
    // /* 更新功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "D")) {
    // /* 刪除功能 */
    // saveFunc();
    // }
    else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      xlsPrint();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsq0040")) {
        wp.optionKey = wp.colStr("ex_acct_type");
        dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
      }

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_log_date1"), wp.itemStr("ex_log_date2")) == false) {
      alertErr2("異動日期起迄：輸入錯誤");
      return;
    }

    if (wp.iempty("ex_idno") == false) {
      if (wp.itemStr("ex_idno").length() != 10) {
        alertErr2("身分證字號  輸入錯誤");
        return;
      }
    }

    if (wp.iempty("ex_log_date1") && wp.iempty("ex_log_date2") && wp.iempty("ex_idno")) {
      alertErr2("異動日期及身分證字號: 不可全部空白");
      return;
    }

    String lsWhere = " where 1=1  " + sqlCol(wp.itemStr("ex_log_date1"), "A.log_date", ">=")
        + sqlCol(wp.itemStr("ex_log_date2"), "A.log_date", "<=")
        + sqlCol(wp.itemStr("ex_acct_type"), "A.acct_type")
        + sqlCol(wp.itemStr("ex_vip_code"), "A.vip_code")
        + sqlCol(wp.itemStr("ex_mod_user"), "A.mod_user");

    if (wp.iempty("ex_idno") == false) {
      lsWhere += inIdnoCrd("A.id_p_seqno", wp.itemStr2("ex_idno"), "");
    }

    if (wp.itemEq("ex_log_type", "1")) {
      lsWhere += " and A.log_type = '1' ";
    } else if (wp.itemEq("ex_log_type", "2")) {
      lsWhere += " and A.log_type = '2' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " A.log_date ," + " A.log_time ," + " A.log_type ,"
        + " uf_acno_name(A.p_seqno) as chi_name ," + " A.acct_type ," + " B.acct_key ,"
        + " A.vip_code ," + " A.vip_remark ," + " A.mod_user," + " A.apr_user," + " A.p_seqno,"
        + " decode(A.log_type,'1','1.人工','2','2.批次') as wk_log_type , "
        + " (select usr_cname from sec_user where usr_id = A.mod_user) as tt_mod_user , "
        + " (select usr_cname from sec_user where usr_id = A.apr_user) as tt_apr_user ";
    wp.daoTable = "act_vip_code_log A join act_acno B on A.p_seqno = B.acno_p_seqno";
    wp.whereOrder = " order by acct_key , acct_type, log_date, log_time ";

    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter(wp.selectCnt);
    wp.setPageValue();
  }

  void queryAfter(int listNrow) {

    for (int ii = 0; ii < listNrow; ii++) {
      wp.colSet(ii, "wk_mod_user", wp.colStr(ii, "mod_user") + "_" + wp.colStr(ii, "tt_mod_user"));
      wp.colSet(ii, "wk_apr_user", wp.colStr(ii, "apr_user") + "_" + wp.colStr(ii, "tt_apr_user"));
      wp.colSet(ii, "hh_name", commString.hideIdnoName(wp.colStr(ii, "chi_name")));
      wp.colSet(ii, "hh_acctno", commString.hideAcctNo(wp.colStr(ii, "acct_key")));
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

  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "cmsq0040";
      String cond1 = "異動日期: " + commString.strToYmd(wp.itemStr("ex_log_date1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_log_date2"));
      wp.colSet("cond1", cond1);
      wp.colSet("user_id", wp.loginUser);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "cmsq0040.xlsx";
      wp.pageRows = 9999;
      queryFunc();
      wp.setListCount(1);
      queryFunc();
      wp.listCount[1] = sqlRowNum;
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub

  }

}
