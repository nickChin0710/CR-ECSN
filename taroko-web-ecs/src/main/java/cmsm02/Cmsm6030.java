/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-11-25  V1.00.01   Alex          dddw add , code ->chinese                 *
* 108-12-16  V1.00.02   Alex          dataRead fix                              *
* 108-12-19  V1.00.03   Alex          add dept_name                             *
* 109-04-27  V1.00.04   shiyuqi     updated for project coding standard     *  
* 109-07-27  V1.00.05   JustinWu change cms_proc_dept into ptr_dept_code
* 109-12-31  V1.00.06   shiyuqi       修改无意义命名                                                        * 
* 111-11-22  V1.00.07   sunny         調整部門代號，如為A401及3144，表信用卡部可查看所有部門，如其他只能看自己部門*
* 111-11-24  V1.00.08   sunny       配合卡部要求，將「接聽」改為「受理」                     *  
******************************************************************************/
package cmsm02;

import ofcapp.BaseAction;

public class Cmsm6030 extends BaseAction {
  Cmsm6030Func func;
  String rowid = "", procDeptno = "";

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
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      strAction = "D";
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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* AJAX */
      strAction = "AJAX";
      processAjaxOption();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "Cmsm6030")) {
    	
    	wp.optionKey = wp.colStr("ex_crt_user");
    	dddwList("dddw_crt_user", "sec_user"
                 , "usr_id", "usr_cname", "where 1=1");
        wp.optionKey = wp.colStr("ex_case_user");
        dddwList("dddw_case_user", "sec_user"
                 , "usr_id", "usr_cname", "where 1=1");  
    	
        wp.optionKey = wp.colStr("ex_dept_no");
        String lsWhere="where 1=1";
        if (notEmpty(wp.loginDeptNo)&& !eqIgno(wp.loginDeptNo,"A401") && !eqIgno(wp.loginDeptNo,"3144")) {
        	lsWhere += " and dept_code = :dept_no ";
            setString2("dept_no", wp.loginDeptNo);
        }
        
        wp.optionKey = wp.colStr("ex_dept_no");
        dddwList("d_dddw_deptno", "ptr_dept_code", "dept_code", "dept_name", lsWhere);
        wp.optionKey = wp.colStr("ex_crt_user");
        dddwList("dddw_crt_user", "sec_user", "usr_id", "usr_cname", "where 1=1");
        wp.optionKey = wp.colStr("ex_case_user");
        dddwList("dddw_case_user", "sec_user", "usr_id", "usr_cname", "where 1=1");
      }

    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "Cmsm6030")) {
        wp.optionKey = wp.colStr("ex_case_type");
        dddwList("d_dddw_casetype", "CMS_CASETYPE", "case_id", "case_desc", "where 1=1");
      }

    } catch (Exception ex) {
    }

    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("proc_id");
        dddwList("dddw_proc_id", "cms_casetype", "case_id", "case_desc",
            "where 1=1 and apr_flag ='Y' and case_type ='2' "
                + sqlCol(wp.colStr("proc_deptno"), "dept_no"));
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (itemIsempty("ex_id_card") == false) {
      if (logQueryIdno(wp.itemStr2("ex_id_card")) == false) {
        alertErr2("無查詢權限");
        return;
      }
      this.zzVipColor(wp.itemStr("ex_id_card"));
    }

    if (this.chkStrend(wp.itemStr("ex_case_date1"), wp.itemStr("ex_case_date2")) == false) {
      alertErr2("受理日期起迄：輸入錯誤");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_recall_date1"), wp.itemStr("ex_recall_date2")) == false) {
      alertErr2("預計回電日期起迄：輸入錯誤");
      return;
    }
    String lsWhere =
        " where B.case_date = A.case_date and B.case_seqno = A.case_seqno and uf_nvl(B.case_conf_flag,'N') in ('Y','N') "
            + sqlCol(wp.itemStr("ex_case_date1"), "B.case_date", ">=")
            + sqlCol(wp.itemStr("ex_case_date2"), "B.case_date", "<=")
            + sqlCol(wp.itemStr("ex_recall_date1"), "A.eta_date", ">=")
            + sqlCol(wp.itemStr("ex_recall_date2"), "A.eta_date", "<=")
            + sqlCol(wp.itemStr("ex_apr_flag"), "uf_nvl(B.apr_flag,'N')")
            + sqlCol(wp.itemStr("ex_id_card"),
                "decode(length('" + wp.itemStr("ex_id_card") + "'),10,A.case_idno,A.card_no)")
            + sqlCol(wp.itemStr("ex_case_type"), "A.case_type")
            + sqlCol(wp.itemStr("ex_crt_user"), "B.crt_user", "like%")
            + sqlCol(wp.itemStr("ex_case_user"), "A.case_user", "like%")
            + sqlCol(wp.itemStr("ex_seq_no"), "A.case_seqno");

    if (wp.itemEmpty("ex_dept_no") == false) {
      lsWhere += " and B.proc_deptno in (select dept_code from ptr_dept_code "
      		+ " where 1=1 "
            + sqlCol(wp.itemStr2("ex_dept_no"), "dept_code") + ") ";
    }

    if (wp.itemEq("ex_proc_result", "0")) {
      lsWhere += " and B.proc_result <> '9' ";
    } else {
      lsWhere += sqlCol(wp.itemStr("ex_proc_result"), "B.proc_result");
    }

    if (wp.itemEq("ex_ugcall", "Y")) {
      lsWhere += " and A.ugcall_flag = 'Y' ";
    } else if (wp.itemEq("ex_ugcall", "N")) {
      lsWhere += " and A.ugcall_flag <> 'Y' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(B.rowid) as rowid," + " B.case_date , " + " B.case_seqno,"
        + " B.proc_deptno,"
        + " (select dept_name from ptr_dept_code where dept_code = B.proc_deptno) as tt_dept_code ,"
        + " B.proc_id," + " B.proc_desc," + " substr(B.proc_desc,1,20) as tt_proc_desc20 ,"
        + " B.proc_result," + " B.crt_date," + " B.crt_user," + " B.apr_flag," + " B.apr_date,"
        + " B.apr_user," + " A.card_no," + " A.case_type," + " A.case_desc,"
        + " substr(A.case_desc,1,20) as tt_case_desc20 ," + " A.case_user,"
        + " (select usr_cname from sec_user where usr_id = A.case_user) as tt_case_user , "
        + " (select usr_cname from sec_user where usr_id = B.crt_user) as tt_crt_user , "
        + " A.case_idno," + " B.recall_date," + " A.eta_date," + " A.reply_phone,"
        + " A.ugcall_flag , "
        + " decode(B.proc_result,'0','未處理','5','處理中','9','處理完成') as tt_proc_result , "
        + " decode(B.proc_id,'1','問題案件','2','處理部門') as tt_proc_id "

    ;
    wp.daoTable = "cms_casedetail B, cms_casemaster A ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by B.case_date Asc, B.case_seqno Asc ";
    logSql();
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
    rowid = wp.itemStr("data_k1");
    procDeptno = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.selectSQL = "hex(B.rowid) as rowid, B.mod_seqno," + "A.case_date," + "A.case_seqno,"
        + "B.proc_deptno," + "B.apr_flag," + "A.ugcall_flag," + "A.card_no," + "A.case_idno,"
        + "A.case_user," + "'' as crt_user," + "B.recall_date," + "A.eta_date," + "A.reply_phone,"
        + "A.case_desc," + "B.proc_id," + "B.proc_result," + "B.proc_result as old_proc_result ,"
        + "B.proc_desc," + "B.proc_desc2," + "B.apr_date," + "B.apr_user,"
        + " (select usr_cname from sec_user where usr_id = A.case_user) as tt_case_user , "
        // + " (select usr_cname from sec_user where usr_id = B.crt_user) as tt_crt_user , "
        + " (select usr_cname from sec_user where usr_id = B.apr_user) as tt_apr_user , "
        + " (select dept_name from ptr_dept_code where dept_code = B.proc_deptno) as tt_proc_deptno ";
    wp.daoTable = "cms_casedetail B, cms_casemaster A ";
    wp.whereStr = " where A.case_date = B.case_date and A.case_seqno =B.case_seqno "
        + sqlRowId(rowid, "B.rowid") + sqlCol(procDeptno, "B.proc_deptno");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + rowid);
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new cmsm02.Cmsm6030Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
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
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub
	  wp.colSet("ex_dept_no",wp.loginDeptNo);
	   if (empty(wp.loginDeptNo) || eqIgno(wp.loginDeptNo,"A401") || eqIgno(wp.loginDeptNo,"3144")) {
	      wp.colSet("wk_edit_dept","Y");
	   }
	   else wp.colSet("wk_edit_dept","");

  }

  public void processAjaxOption() throws Exception {
    String lsUser = "";
    lsUser = wp.loginUser;
    String sql1 = "select usr_cname from sec_user where usr_id = ? ";
    sqlSelect(sql1, new Object[] {lsUser});

    if (sqlRowNum <= 0) {
      wp.addJSON("usr_id", lsUser);
      wp.addJSON("usr_cname", "");
      return;
    }

    wp.addJSON("usr_id", lsUser);
    wp.addJSON("usr_cname", sqlStr("usr_cname"));
  }

}

