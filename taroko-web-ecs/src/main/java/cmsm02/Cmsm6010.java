/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-16  V1.00.03  Alex       fix queryWhere                             *
* 108-12-04  V1.00.02  Alex       dddw fix                                   *
* 108-11-12  V1.00.01  Alex       add mod_date,mod_time                      *
* 109-04-27  V1.00.04  shiyuqi       updated for project coding standard     * 
* 109-07-21  V1.00.05  JustinWu   remove setConfirmMesg and dataRead if update is successful*
* 109-07-27  V1.00.08  JustinWu   change CMS_PROC_DEPT into PTR_DEPT_CODE
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package cmsm02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Cmsm6010 extends BaseEdit {
  Cmsm6010Func func;
  String caseType = "", caseId = "";

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
      updateRetrieve = true; // dataRead if update is successful
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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
        ajaxFunc();
        return;
    }

    dddwSelect();
    initButton();
  }

	private void ajaxFunc() {
		String caseType = wp.itemStr("caseType");

		try {
			wp.initOption = "--";
			wp.optionKey = "";
			dddwList("dddw_case_id", "CMS_CASETYPE", "CASE_ID", "CASE_DESC", 
					String.format("WHERE CASE_TYPE='%s' AND apr_flag = 'Y' ORDER BY CASE_ID", caseType));
			
			wp.addJSON("dddw_case_id", wp.colStr("dddw_case_id"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

@Override
  public void dddwSelect() {
	  
    try {
      if (eqIgno(wp.respHtml, "Cmsm6010")) {
        wp.optionKey = wp.colStr("ex_dept_no");
        dddwList("dddw_dept_no", "PTR_DEPT_CODE", "dept_code", "dept_name", "where 1=1");
        
        String caseType = wp.colEmpty("ex_case_type") ? "1" :wp.colStr("ex_case_type");
        
		wp.optionKey = wp.colStr("ex_case_id");
		dddwList("dddw_case_id", "CMS_CASETYPE", "CASE_ID", "CASE_DESC", 
				String.format("WHERE CASE_TYPE='%s' AND apr_flag = 'Y' ORDER BY CASE_ID", caseType));
      }

    } catch (Exception ex) {
    }

    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
				wp.optionKey = wp.colStr("dept_no");
				dddwList("dddw_dept_no", "PTR_DEPT_CODE", "dept_code", "dept_name", "where 1=1");

				wp.optionKey = wp.colStr("dept_no2");
				dddwList("dddw_dept_no2", "PTR_DEPT_CODE", "dept_code", "dept_name", "where 1=1");

				wp.optionKey = wp.colStr("dept_no3");
				dddwList("dddw_dept_no3", "PTR_DEPT_CODE", "dept_code", "dept_name", "where 1=1");
				
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {


    String lsWhere = " where 1=1 "
        + sqlCol(wp.itemStr("ex_case_type"), "case_type")
        + sqlCol(wp.itemStr("ex_dept_no"), "dept_no")
        + sqlCol(wp.itemStr("ex_case_id"), "case_id");



    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " case_type , " + " case_id," + " case_desc," + " send_code," + " dept_no,"
        + " dept_no2," + " dept_no3," + " crt_date," + " crt_user," + " apr_flag," + " apr_date,"
        + " apr_user," + " conf_mark , " + " to_char(mod_time,'yyyymmdd') as mod_date , "
        + " to_char(mod_time,'hh24miss') as mod_time  "

    ;
    wp.daoTable = "CMS_CASETYPE";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by case_id Asc, apr_flag ";
    logSql();
    pageQuery();


    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    caseType = wp.itemStr("data_k1");
    caseId = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(caseType)) {
      caseType = itemKk("case_type");
    }
    if (empty(caseId)) {
      caseId = itemKk("case_id");
    }

    if (empty(caseType) || empty(caseId)) {
      alertErr2("代碼類別 , 分類代碼 不可空白");
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno," + "case_type," + "case_id," + "case_desc,"
        + "send_code," + "dept_no," + "dept_no2," + "dept_no3," + "crt_date," + "crt_user,"
        + "apr_flag," + "apr_date," + "apr_user," + "conf_mark,"
        + "to_char(mod_time,'yyyymmdd') as mod_date," + "mod_user";
    wp.daoTable = "CMS_CASETYPE";
    wp.whereStr =    " where 1=1"
                                 + sqlCol(caseType, "case_type") 
                                 + sqlCol(caseId, "case_id");
    wp.whereOrder = " order by apr_flag" + " fetch first 1 rows only";
    // this.sql_ddd();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + caseType);
      return;
    }
    if (eqIgno(wp.colStr("case_type"), "1")) {
      wp.colSet("tt_case_type", ".問題案件");
    } else if (eqIgno(wp.colStr("case_type"), "2")) {
      wp.colSet("tt_case_type", ".處理部門");
    }
  }

  @Override
  public void saveFunc() throws Exception {

    func = new cmsm02.Cmsm6010Func(wp);
    if (isDelete() && wp.itemEq("apr_flag", "Y")) {
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;
      }
    }
    rc = func.dbSave(strAction);

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

}
