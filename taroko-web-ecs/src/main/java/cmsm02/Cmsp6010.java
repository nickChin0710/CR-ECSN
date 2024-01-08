/*
 * 2019-1219   V1.00.01  Alex        fix dddw , fix queryRead
*  2020-0427   V1.00.02  shiyuqi     updated for project coding standard     * 
*  2020-1022   V1.00.03  JustinWu    change cms_proc_dept to ptr_dept_code 
*  2022-11-23  V1.00.04  Machao      程序頁面bug調整，覆核功能bug調整
*  2022-11-24  V1.00.08  sunny       配合卡部要求，將「接聽」改為「受理」 & 不能覆核自己的案件   *  
*  2022-11-24  V1.00.09  sunny       增加部門代號欄，限制只能查詢自己單位，除信用卡部(A401)的使用者除外可看到全部    
*  2022-11-25  V1.00.10  Machao      不能覆核自己的案件 代碼調整             *
 */
package cmsm02;

import ofcapp.BaseAction;

public class Cmsp6010 extends BaseAction {
  Cmsp6010Func func;

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
    if (this.chkStrend(wp.itemStr("ex_recall_date1"), wp.itemStr("ex_recall_date2")) == false) {
      alertErr2("預計回電日期起迄：輸入錯誤");
      return;
    }
    String lsWhere =
                " where A.case_date = B.case_date "
            + " and A.case_seqno = B.case_seqno "
            + " and B.case_conf_flag ='V' "
            + " and B.apr_flag<>'Y' "
            + sqlCol(wp.itemStr("ex_case_date1"), "A.case_date", ">=")
            + sqlCol(wp.itemStr("ex_case_date2"), "A.case_date", "<=")
            + sqlCol(wp.itemStr("ex_recall_date1"), "A.eta_date", ">=")
            + sqlCol(wp.itemStr("ex_recall_date2"), "A.eta_date", "<=")
            + sqlCol(wp.itemStr("ex_case_type"), "A.case_type")
            + sqlCol(wp.itemStr("ex_id_card"),
                "decode(length('" + wp.itemStr("ex_id_card") + "'),10,A.case_idno,A.card_no)")
            + sqlCol(wp.itemStr("ex_case_user"), "A.case_user", "like%");
    if (wp.itemEq("ex_ug_call", "0") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_ug_call"), "A.ugcall_flag");
    }
    
    //部門代號add
    if (wp.itemEmpty("ex_dept_no") == false) {
        lsWhere += " and A.case_user IN ( SELECT usr_id FROM sec_user "
        		+ " where 1=1 "
              + sqlCol(wp.itemStr2("ex_dept_no"), "usr_deptno") + ") ";
      }


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(B.rowid) as rowid, B.mod_seqno," + " A.case_idno," + " A.reply_flag,"
        + " A.eta_date," + " A.case_type," + " A.case_desc," + " A.ugcall_flag, " + " A.card_no,"
        + " A.case_date , " + " A.case_seqno," + " A.case_result," + " A.case_user,"
        + " (select usr_cname from sec_user where usr_id = A.case_user ) as tt_case_user ,"
        + " B.case_conf_flag," + " B.proc_deptno , " + " '' as wk_proc ";
    wp.daoTable = "cms_casedetail B, cms_casemaster A ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by B.case_date Asc, B.case_seqno Asc ";
    logSql();
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();
//    apprDisabled("case_user");  //apr_disable 
    apprDisabled("case_user", wp.loginUser);
  }

  void queryAfter() {
    String sql1 = " select " + " dept_name " + " from ptr_dept_code " + " where dept_code = ? ";

    String sql2 = " select " + " substr(case_desc,1,6) as tt_case_type " + " from cms_casetype "
        + " where case_id = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "proc_deptno")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_proc_deptno", sqlStr("dept_name"));
      }

      sqlSelect(sql2, new Object[] {wp.colStr(ii, "case_type")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_case_type", sqlStr("tt_case_type"));
      }
    }
  }

  // dddw_list("d_dddw_casetype","CMS_CASETYPE"
  // ,"case_id","case_desc","where 1=1");

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
    func = new Cmsp6010Func();
    func.setConn(wp);
    int ilOk = 0;
    int ilErr = 0;
    String[] lsRowid = wp.itemBuff("rowid");
    String[] caseSeqnoArr = wp.itemBuff("case_seqno");
    String[] caseDateArr = wp.itemBuff("case_date");
    String[] liModSeqno = wp.itemBuff("mod_seqno");
    String[] lsWkProc = wp.itemBuff("wk_proc");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsRowid.length;
    String optt = opt[0];
    int a = opt.length;
    if(optt.isEmpty() || a==0) {
    	alertErr2("資料錯誤: 請點選欲覆核資料");
    	return;
    }

    int rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = optToIndex(opt[ii]);

      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("caseSeqno", caseSeqnoArr[rr]);
      func.varsSet("caseDate", caseDateArr[rr]);
      func.varsSet("mod_seqno", liModSeqno[rr]);
      func.varsSet("wk_proc", lsWkProc[rr]);

      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
    }


    if (ilOk > 0) {
      sqlCommit(1);
    }
    // -re-Query-
    // queryRead();
    alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub
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
      if (eqIgno(wp.respHtml, "Cmsp6010")) {

          //部門代號add        
          wp.optionKey = wp.colStr("ex_dept_no");
          String lsWhere="where 1=1";
          if (notEmpty(wp.loginDeptNo)&& !eqIgno(wp.loginDeptNo,"A401") && !eqIgno(wp.loginDeptNo,"3144")) {
          	lsWhere += " and dept_code = :dept_no ";
              setString2("dept_no", wp.loginDeptNo);
          }
         
          //部門代號add 
          wp.optionKey = wp.colStr("ex_dept_no");
          dddwList("d_dddw_deptno", "ptr_dept_code", "dept_code", "dept_name", lsWhere); 
    	  
        wp.optionKey = wp.colStr("ex_case_type");
        dddwList("d_dddw_casetype", "CMS_CASETYPE", "case_id", "case_desc",
            "where 1=1 and conf_mark = 'Y' ");

      //部門代號add，受理人員依部門代號條件出現經辦清單。
        lsWhere="where 1=1";
        if (notEmpty(wp.loginDeptNo)&& !eqIgno(wp.loginDeptNo,"A401") && !eqIgno(wp.loginDeptNo,"3144")) {
        	lsWhere += " and usr_deptno = :dept_no ";
            setString2("dept_no", wp.loginDeptNo);
        }        
        wp.optionKey = wp.colStr("ex_case_user");
        dddwList("d_dddw_user", "sec_user", "usr_id", "usr_cname", lsWhere);
        
//        wp.optionKey = wp.colStr("ex_case_user");
//        dddwList("d_dddw_user", "sec_user", "usr_id", "usr_cname", "where 1=1 ");
      }

    } catch (Exception ex) {
    }

  }
  
//  public void apprDisabled(String col) throws Exception {
//		for (int ll = 0; ll < wp.listCount[0]; ll++) {
//			if (!wp.colStr(ll, col).equals(wp.loginUser)) {
//				wp.colSet(ll, "opt_disabled", "");
//			} else
//				wp.colSet(ll, "opt_disabled", "disabled");
//		}
//	}

}
