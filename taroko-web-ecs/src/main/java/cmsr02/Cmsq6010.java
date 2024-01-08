/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-11-25  V1.00.01   Alex              order by , code -> chinese , add dddw     *
* 109-04-28  V1.00.02   shiyuqi       updated for project coding standard     * 
* 109-07-27  V1.00.03   JustinWu   change cms_proc_dept into ptr_dept_code
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package cmsr02;
/*
 * cmsq6010 案件移送處理狀況查詢
 * V00.0		Alex		2017-0815: inital
 * 111-11-24  V1.00.01   sunny       配合卡部要求，將「接聽」改為「受理」                     *  
 * */
import ofcapp.BaseAction;

public class Cmsq6010 extends BaseAction {
  String caseDate = "", caseSeqno = "";

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
    }
  }

  @Override
  public void queryFunc() throws Exception {
    if (itemIsempty("ex_idcard_no") == false) {
      if ((wp.itemStr("ex_idcard_no").length() == 10) == false
          && (wp.itemStr("ex_idcard_no").length() == 15) == false
          && (wp.itemStr("ex_idcard_no").length() == 16) == false) {
        alertErr2("身分證ID/卡號 輸入錯誤");
        return;
      }
      if (this.logQueryIdno(wp.itemStr("ex_idcard_no")) == false) {
        return;
      }
      zzVipColor(wp.itemStr("ex_idcard_no"));
    }
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("受理日期起迄：輸入錯誤");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_eta_date1"), wp.itemStr("ex_eta_date2")) == false) {
      alertErr2("預計回電日期起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where 1=1  " + sqlCol(wp.itemStr("ex_date1"), "case_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "case_date", "<=")
        + sqlCol(wp.itemStr("ex_eta_date1"), "eta_date", ">=")
        + sqlCol(wp.itemStr("ex_eta_date2"), "eta_date", "<=")
        + sqlCol(wp.itemStr("ex_seqno"), "case_seqno", "like%")
        + sqlCol(wp.itemStr("ex_case_user"), "case_user", "like%");
    if (wp.itemEmpty("ex_idcard_no") == false) {
      lsWhere += " and decode(length('" + wp.itemStr("ex_idcard_no") + "'),10,case_idno,card_no) ='"
          + wp.itemStr("ex_idcard_no") + "' ";
    }
    if (wp.itemEq("ex_result", "1")) {
      lsWhere += " and case_result='0' ";
    } else if (wp.itemEq("ex_result", "2")) {
      lsWhere += " and case_result='5' ";
    } else if (wp.itemEq("ex_result", "3")) {
      lsWhere += " and case_result='9' ";
    } else if (wp.itemEq("ex_result", "4")) {
      lsWhere += " and case_trace_flag='Y' ";
    } else if (wp.itemEq("ex_result", "5")) {
      lsWhere += " and case_trace_flag='F' ";
    }
    if (wp.itemEq("ex_send_code", "1")) {
      lsWhere += " and send_code <> 'Y' ";
    } else if (wp.itemEq("ex_send_code", "2")) {
      lsWhere += " and send_code = 'Y' ";
    }
    if (wp.itemEq("ex_ug_call", "N")) {
      lsWhere += " and ugcall_flag <> 'Y' ";
    } else if (wp.itemEq("ex_ug_call", "Y")) {
      lsWhere += " and ugcall_flag='Y' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "case_date," + " case_seqno , " + " case_idno," + " card_no," + " case_type,"
        + " case_desc," + " case_result," + " finish_date," + " send_code," + " case_user,"
        + " case_trace_date," + " ugcall_flag," + " reply_flag," + " case_time,"
        + " decode(case_result,'0','未處理','5','處理中','9','處理完成') as tt_case_result , "
        + " (select usr_cname from sec_user where usr_id = case_user) as tt_case_user ";
    wp.daoTable = "cms_casemaster ";
    wp.whereOrder = " order by case_date Asc , case_time Asc ";
    log("aaa :::" + wp.whereStr);
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {

      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    caseDate = wp.itemStr("data_k1");
    caseSeqno = wp.itemStr("data_k2");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    // readDetail();
    wp.selectSQL = "case_date," + " case_seqno , " + " case_user," + " ugcall_flag," + " case_idno,"
        + " card_no," + " case_result,"
        + " decode(case_result,'0','未處理','5','處理中','9','處理完成') as tt_case_result ,"
        + " finish_date," + " send_code," + " case_type," + " case_trace_date," + " reply_flag,"
        + " case_desc," + " case_desc2";
    wp.daoTable = "cms_casemaster";
    wp.whereStr = " where 1=1" + sqlCol(caseDate, "case_date") + sqlCol(caseSeqno, "case_seqno");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + caseDate);
      return;
    }
    readDetail();
  }

  void readDetail() throws Exception {
    wp.selectSQL = " proc_deptno , "
        + " (select dept_name from ptr_dept_code where dept_code = proc_deptno) as tt_dept_name , "
        + " proc_result , "
        + " decode(proc_result,'0','未處理','5','處理中','9','處理完成') as tt_proc_result ,"
        + " finish_date as D_finish_date," + " crt_user," + " recall_date," + " proc_id,"
        + " (select case_desc from cms_casetype where case_type = '2' and case_id = proc_id) as tt_proc_id ,"
        + " proc_desc," + " proc_desc2,"
        + " (select usr_cname from sec_user where usr_id = cms_casedetail.crt_user) as tt_crt_user";
    wp.daoTable = "cms_casedetail";
    wp.whereStr = " where 1=1" + sqlCol(caseDate, "case_date") + sqlCol(caseSeqno, "case_seqno");
    this.logSql();
    pageQuery();
    if (sqlRowNum <= 0) {
      wp.notFound = "N";
      return;
    }
    wp.setListCount(1);
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
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsq6010")) {
        wp.optionKey = wp.colStr("ex_case_user");
        dddwList("dddw_case_user", "sec_user", "usr_id", "usr_cname", "where 1=1 ");
      }
    } catch (Exception ex) {
    }

  }

}
