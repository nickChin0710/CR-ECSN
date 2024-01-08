package cmsm02;
/* 2020-1022:  JustinWu    change cms_proc_dept to ptr_dept_code
 * 2020-0729:  JustinWu   ++ case_desc
 * 2019-0423:  JH                mod_user<>apr_user
 * 2019-1125:  Alex            usr_cname , proc_reult
 * 2019-1219:  Alex            add error_desc
 * 109-04-27   shiyuqi       updated for project coding standard     
 * 111-11-23   machao        覆核功能bug調整* *  
 * 111-11-24   sunny       配合卡部要求，將「接聽」改為「受理」                  
 * 2022-11-25  Machao      不能覆核自己的案件 代碼調整   *  
 * 2022-11-29  sunny       增加限制只能查詢自己單位，除信用卡部(A401)的使用者除外可看到全部    * 
* */

import ofcapp.BaseAction;

public class Cmsp6030 extends BaseAction {
  Cmsp6030Func func;

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
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // saveFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // saveFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // saveFunc();
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
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_case_date1"), wp.itemStr("ex_case_date2")) == false) {
      alertErr2("受理日期起迄：輸入錯誤");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("處理日期起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where 1=1 and ccd.apr_flag <> 'Y' "
    		+ "and ccd.proc_result ='9' "
        + sqlCol(wp.itemStr("ex_case_date1"), "ccd.case_date", ">=")
        + sqlCol(wp.itemStr("ex_case_date2"), "ccd.case_date", "<=")
        + sqlCol(wp.itemStr("ex_crt_date1"), "ccd.crt_date", ">=")
        + sqlCol(wp.itemStr("ex_crt_date2"), "ccd.crt_date", "<=")
        + sqlCol(wp.itemStr("ex_crt_user"), "ccd.crt_user", "like%")
        + sqlCol(wp.itemStr("ex_seqno"), "ccd.case_seqno", "like%")
        + sqlCol(wp.itemStr("ex_dept_no"), "ccd.proc_deptno")
    // +sql_col(wp.loginUser,"crt_user","<>")
    ;


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(ccd.rowid) as rowid, "
    	+ " ccd.mod_seqno," 
        + " ccd.case_date," 
    	+ " ccd.case_seqno,"
        + " ccd.proc_deptno," 
    	+ " ccd.proc_result," 
        + " ccd.proc_id," 
    	+ " ccd.proc_desc," 
        + " ccd.crt_date,"
        + " ccd.crt_user,"
        + " cct.case_desc, "
        + " (select su.usr_cname from sec_user as su where su.usr_id = ccd.crt_user) as tt_crt_user ";
    wp.daoTable = "CMS_CASEDETAIL as ccd left join CMS_CASETYPE as cct on ccd.PROC_ID = cct.CASE_ID";
    wp.whereOrder = " order by ccd.case_date Asc, ccd.case_seqno Asc, ccd.proc_deptno Asc ";

    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();
    apprDisabled("crt_user", wp.loginUser);
  }

  void queryAfter() {
    String sql1 = " select " + " dept_name " + " from ptr_dept_code " + " where dept_code = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "proc_deptno")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_proc_deptno", sqlStr("dept_name"));
      }

      if (wp.colEq(ii, "proc_result", "9")) {
        wp.colSet(ii, "tt_proc_result", "處理完成");
      } else if (wp.colEq(ii, "proc_result", "5")) {
        wp.colSet(ii, "tt_proc_result", "處理中");
      } else if (wp.colEq(ii, "proc_result", "0")) {
        wp.colSet(ii, "tt_proc_result", "未處理");
      }

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
    func = new Cmsp6030Func();
    func.setConn(wp);
    int ilOk = 0, ilErr = 0, llErrApr = 0;

    String[] lsRowid = wp.itemBuff("rowid");
    String[] liModSeqno = wp.itemBuff("mod_seqno");
    String[] lsCd = wp.itemBuff("case_date");
    String[] lsCs = wp.itemBuff("case_seqno");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsRowid.length;
    String optt = opt[0];
    int a = opt.length;
    if(optt.isEmpty() || a==0) {
    	alertErr2("資料錯誤: 請點選欲覆核資料");
    	return;
    }

    for (int ii = 0; ii < opt.length; ii++) {
      int rr = optToIndex(opt[ii]);
      if (rr < 0) {
        continue;
      }
      optOkflag(rr);
      if (checkAprUser(rr, "crt_user")) {
        llErrApr++;
        wp.colSet(rr, "error_desc", "處理經辦和覆核主管不可為同一人");
        continue;
      }

      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("mod_seqno", liModSeqno[rr]);
      func.varsSet("case_date", lsCd[rr]);
      func.varsSet("case_seqno", lsCs[rr]);

      int liRc = func.dataProc();
      sqlCommit(liRc);
      if (liRc == 1) {
        ilOk++;
      } else {
        ilErr++;
        wp.colSet(rr, "error_desc", func.getMsg());
      }

      optOkflag(rr, liRc);
    }

    // -re-Query-
    // queryRead();
    alertMsg("覆核處理筆數: 成功=" + ilOk + "; 失敗=" + ilErr + "; 不可覆核=" + llErrApr);
  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
//    if (wp.itemEmpty("ex_dept_no")) {
//      wp.itemSet("ex_dept_no", this.userDeptNo());
//    }

	//部門代號add  
	  wp.colSet("ex_dept_no",wp.loginDeptNo);
	   if (empty(wp.loginDeptNo) || eqIgno(wp.loginDeptNo,"A401") || eqIgno(wp.loginDeptNo,"3144")) {
	      wp.colSet("wk_edit_dept","Y");
	   }
	   else wp.colSet("wk_edit_dept","");
  }

  @Override
  public void dddwSelect() {
    try {
//      if (eqIgno(wp.respHtml, "Cmsp6030")) {
//        wp.optionKey = wp.colStr("ex_dept_no");
//        dddwList("dddw_dept_no", "ptr_dept_code", "dept_code", "dept_name", "where 1=1");
//      }

      if (eqIgno(wp.respHtml, "Cmsp6030")) {
    	  
    	  //部門代號add        
          wp.optionKey = wp.colStr("ex_dept_no");
          String lsWhere="where 1=1";
          if (notEmpty(wp.loginDeptNo)&& !eqIgno(wp.loginDeptNo,"A401") && !eqIgno(wp.loginDeptNo,"3144")) {
          	lsWhere += " and dept_code = :dept_no ";
              setString2("dept_no", wp.loginDeptNo);
          }
    	  
          //部門代號add 
          wp.optionKey = wp.colStr("ex_dept_no");
          dddwList("dddw_dept_no", "ptr_dept_code", "dept_code", "dept_name", lsWhere);
    	  
          //部門代號add，受理人員依部門代號條件出現經辦清單。
          lsWhere="where 1=1";
          if (notEmpty(wp.loginDeptNo)&& !eqIgno(wp.loginDeptNo,"A401") && !eqIgno(wp.loginDeptNo,"3144")) {
          	lsWhere += " and usr_deptno = :dept_no ";
              setString2("dept_no", wp.loginDeptNo);
          }        
          wp.optionKey = wp.colStr("ex_crt_user");
          dddwList("dddw_crt_user", "sec_user", "usr_id", "usr_cname", lsWhere);
     //        wp.optionKey = wp.colStr("ex_crt_user");
//        dddwList("dddw_crt_user", "sec_user", "usr_id", "usr_cname", "where 1=1");
      }
      
    } catch (Exception ex) {
    }

  }

}
