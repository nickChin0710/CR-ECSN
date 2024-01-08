/*
 * 2019-12-19  V1.00.01  Alex            fix dddw , queryWhere
 * 2020-04-27  V1.00.02  shiyuqi       updated for project coding standard     *   
 * 2020-07-27  V1.00.03  JustinWu   change cms_proc_dept into ptr_dept_code
 * 2020-07-28  V1.00.04  JustinWu   check opt and change errAlert into wp.dispMesg and ++根據分配人員代號以AJAX更換案件移送部門
 * 111-11-23   machao        覆核功能bug調整* 
 */

package cmsm02;

import ofcapp.BaseAction;

public class Cmsm6050 extends BaseAction {
  Cmsm6050Func func;

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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      getCrtUserDeptNo();
    }

  }

	private void getCrtUserDeptNo() throws Exception {
		String sql1 = "select pdc.DEPT_CODE , pdc.DEPT_NAME " 
	            + " from SEC_USER as su LEFT JOIN PTR_DEPT_CODE as pdc ON su.USR_DEPTNO = pdc.DEPT_CODE "
				+ " where 1=1 "
				+ " and  dept_code IS NOT NULL "
	            +sqlCol(wp.itemStr("crtUserAJAX"), "su.USR_ID");
		sqlSelect(sql1);
		if (sqlRowNum > 0) {
			wp.addJSON("deptCodeAJAX", sqlStr("DEPT_CODE"));
			wp.addJSON("deptNameAJAX", sqlStr("DEPT_NAME"));
			wp.addJSON("rowIndexAJAX", wp.itemStr("rowIndex"));
		}else {
			wp.addJSON("deptCodeAJAX", "");
			wp.addJSON("deptNameAJAX", "");
			wp.addJSON("crtUserAJAX", wp.itemStr("crtUserAJAX"));
			wp.addJSON("rowIndexAJAX", wp.itemStr("rowIndex"));
		}

	}

@Override
  public void queryFunc() throws Exception {
    if (wp.itemEmpty("ex_dept_no")) {
      alertErr2("部門代號:不可空白");
      return;
    }
    if (wp.itemEmpty("ex_id_card") == false) {
      if ((wp.itemStr("ex_id_card").length() == 10) == false
          && (wp.itemStr("ex_id_card").length() == 15) == false
          && (wp.itemStr("ex_id_card").length() == 16) == false) {
        alertErr2("身分證ID/卡號 輸入錯誤");
        return;
      }
    }
    if (itemIsempty("ex_id_card") == false) {
      if (logQueryIdno(wp.itemStr("ex_id_card")) == false) {
        return;
      }
      this.zzVipColor(wp.itemStr("ex_id_card"));
    }

    if (this.chkStrend(wp.itemStr("ex_case_date1"), wp.itemStr("ex_case_date2")) == false) {
      alertErr2("統計日期起迄：輸入錯誤");
      return;
    }
    String lsWhere =
        " where B.case_date = A.case_date and B.case_seqno = A.case_seqno and B.proc_deptno in (select dept_code from ptr_dept_code where 1=1 "
            + sqlCol(wp.itemStr("ex_dept_no"), "dept_code") + ") "
            + " and B.proc_result ='0' and B.case_conf_flag in ('N','Y') "
            + sqlCol(wp.itemStr("ex_case_date1"), "B.case_date", ">=")
            + sqlCol(wp.itemStr("ex_case_date2"), "B.case_date", "<=")
            + sqlCol(wp.itemStr("ex_id_card"),
                "decode(length('" + wp.itemStr("ex_id_card") + "'),10,A.case_idno,A.card_no)")
            + sqlCol(wp.itemStr("ex_case_type"), "A.case_type");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }


  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(B.rowid) as rowid,B.mod_seqno," + " B.case_date , " + " B.case_seqno,"
        + " B.proc_deptno," + " B.proc_result," + " B.crt_user," + " A.case_idno," + " A.card_no,"
        + " A.case_type," + " A.case_desc," + " A.case_user"

    ;
    wp.daoTable = "cms_casedetail B, cms_casemaster A ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by B.proc_deptno Asc, B.case_date Asc, B.case_seqno Asc ";
    logSql();
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();
  }

  void queryAfter() throws Exception {
    if (eqIgno(wp.respHtml, "Cmsm6050")) {
      dddwList("dddw_crt_user", "sec_user", "usr_id", "usr_cname", "where 1=1");
    }
    String sql1 = " select " + " substr(case_desc,1,6) as tt_case_desc " + " from cms_casetype "
        + " where 1=1 " + " and case_type ='1' " + " and case_id = ? ";

    String sql2 = " select " + " dept_name " + " from ptr_dept_code " + " where dept_code = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "db_id_card", wp.colStr(ii, "case_idno") + "/" + wp.colStr(ii, "card_no"));
      wp.colSet(ii, "dddw_crt_user", wp.colStr("dddw_crt_user"));
      if (wp.colEq(ii, "proc_result", "9")) {
        wp.colSet(ii, "tt_proc_result", "完成");
      } else if (wp.colEq(ii, "proc_result", "5")) {
        wp.colSet(ii, "tt_proc_result", "處理中");
      } else if (wp.colEq(ii, "proc_result", "0")) {
        wp.colSet(ii, "tt_proc_result", "未處理");
      }

      sqlSelect(sql1, new Object[] {wp.colStr(ii, "case_type")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_case_type", sqlStr("tt_case_desc"));
      }

      sqlSelect(sql2, new Object[] {wp.colStr(ii, "proc_deptno")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_proc_deptno", sqlStr("dept_name"));
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
    func = new Cmsm6050Func();
    func.setConn(wp);

    int ilOk = 0;
    int ilErr = 0;

    String[] lsRowid = wp.itemBuff("rowid");
    String[] lsModSeqno = wp.itemBuff("mod_seqno");
    String[] lsCu = wp.itemBuff("crt_user");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsRowid.length;
    String optt = opt[0];
    int a = opt.length;
    if(optt.isEmpty() || a==0) {
    	alertErr2("資料錯誤: 請點選欲分配資料");
    	return;
    }

    int rr = -1;
    
    if (  ! "".equals(opt[0])  )
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      wp.log("" + ii + "-ON." + lsRowid[rr]);

      if (isEmpty(lsCu[rr])) {
    	  ilErr++;
    	  wp.colSet(rr, "ok_flag", "X");
    	  continue;
      }
      
      
      if (rr < 0) {
    	ilErr++;
    	wp.colSet(rr, "ok_flag", "X");
        continue;
      }
      
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("crt_user", lsCu[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("mod_seqno", lsModSeqno[rr]);

      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        continue;
      }else {
		ilErr++;
		wp.colSet(rr, "ok_flag", "X");
      }   
    }

    wp.dispMesg = "分配處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr;
  }



  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    if (wp.itemEmpty("ex_dept_no")) {
      wp.itemSet("ex_dept_no", this.userDeptNo());
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "Cmsm6050")) {
        wp.optionKey = wp.colStr("ex_case_type");
        dddwList("d_dddw_casetype",
            "select case_id as db_code , case_id||' '||case_desc as db_desc from cms_casetype where 1=1 and case_type ='1' order by case_id");
      }

      if (eqIgno(wp.respHtml, "Cmsm6050")) {
        wp.optionKey = wp.colStr("ex_dept_no");
        dddwList("d_dddw_dept_no", "ptr_dept_code", "dept_code", "dept_name", "where 1=1");
      }

    } catch (Exception ex) {
    }
  }

}
