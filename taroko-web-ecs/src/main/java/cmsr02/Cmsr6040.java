/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-11-25  V1.00.01  Alex      dddw fix , user cname , proc_dept           *
* 109-04-28  V1.00.02  shiyuqi   updated for project coding standard         *
* 109-07-20  V1.00.03  sunny     fix bug,add cms_casetype.apr_flag ='Y'      *
* 109-07-27  V1.00.04  JustinWu  change cms_proc_dept into ptr_dept_code     *
* 110-01-06  V1.00.05  shiyuqi   修改无意义命名                                                              *
* 111-11-24  V1.00.06  sunny     配合卡部要求，將「接聽」改為「受理」                             *
* 111-11-24  V1.00.07  sunny     增加部門代號欄，限制只能查詢自己單位，除信用卡部(A401)的使用者除外可看到全部    *
* 111-11-29  V1.00.08  sunny     配合卡部要求，增加【受理單位簽核】、【處理單位簽核】兩項資訊 *
* 111-12-22  V1.00.09  sunny     取消ID及卡號資料在報表上的隱碼改為明碼                             *
******************************************************************************/
package cmsr02;

import ofcapp.BaseAction;
import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Cmsr6040 extends BaseAction implements InfacePdf {

  @Override
  public void userAction() throws Exception {

    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) { // -資料讀取-
      strAction = "R";
      // dataRead();
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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

    dddwSelect();
    initButton();

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "Cmsr6040")) {
        wp.optionKey = wp.colStr("ex_case_type");
        dddwList("d_dddw_casetype", "CMS_CASETYPE", "case_id", "case_desc",
            "where 1=1 and case_type ='1' and apr_flag ='Y'");
        
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
        
        wp.optionKey = wp.colStr("ex_proc_deptno");
        dddwList("dddw_proc_dept", "ptr_dept_code", "dept_code", "dept_name", "where 1=1");
        
        //部門代號add，受理人員依部門代號條件出現經辦清單。
        lsWhere="where 1=1";
        if (notEmpty(wp.loginDeptNo)&& !eqIgno(wp.loginDeptNo,"A401") && !eqIgno(wp.loginDeptNo,"3144")) {       
        lsWhere += " and usr_deptno = :dept_no ";
            setString2("dept_no", wp.loginDeptNo);
        }        
        wp.optionKey = wp.colStr("ex_user");
        dddwList("dddw_case_user", "sec_user", "usr_id", "usr_cname", lsWhere);
      }
    } catch (Exception ex) {
    }
  }


  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_cms_date_s"), wp.itemStr("ex_cms_date_e")) == false) {
      alertErr2("受理日期起迄：輸入錯誤");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_ok_date_s"), wp.itemStr("ex_ok_date_e")) == false) {
      alertErr2("處理完成期間起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where ( A.case_date = B.case_date ) and  ( A.case_seqno =B.case_seqno ) "
        + sqlCol(wp.itemStr("ex_cms_date_s"), "A.case_date", ">=")
        + sqlCol(wp.itemStr("ex_cms_date_e"), "A.case_date", "<=")
        + sqlCol(wp.itemStr("ex_ok_date_s"), "B.finish_date", ">=")
        + sqlCol(wp.itemStr("ex_ok_date_e"), "B.finish_date", "<=")
        + sqlCol(wp.itemStr("ex_case_type"), "B.case_type")
        + sqlCol(wp.itemStr("ex_proc_deptno"), "A.proc_deptno")
        + sqlCol(wp.itemStr("ex_procid"), "A.proc_id", "like%")
        + sqlCol(wp.itemStr("ex_user"), "B.case_user", "like%");

    if (wp.itemEq("ex_ugcall_flag", "0")) {
      lsWhere += " and B.ugcall_flag in ('Y','N') ";
    } else if (wp.itemEq("ex_ugcall_flag", "Y")) {
      lsWhere += " and B.ugcall_flag = 'Y' ";
    } else if (wp.itemEq("ex_ugcall_flag", "N")) {
      lsWhere += " and B.ugcall_flag = 'N' ";
    }
    
    //add 案件需簽核
    //case_conf_flag V.需簽核 N.免簽核 Y.已簽核 R.退回
    if (wp.itemEq("ex_case_conf_flag", "0")) {
        lsWhere += " and A.case_conf_flag in ('Y','N','V','R') ";
      } else if (wp.itemEq("ex_case_conf_flag", "Y")) {
        lsWhere += " and A.case_conf_flag = 'Y' ";
      } else if (wp.itemEq("ex_case_conf_flag", "N")) {
        lsWhere += " and A.case_conf_flag = 'N' ";
      } else if (wp.itemEq("ex_case_conf_flag", "R")) {
      lsWhere += " and A.case_conf_flag = 'R' ";
      } else if (wp.itemEq("ex_case_conf_flag", "V")) {
          lsWhere += " and A.case_conf_flag = 'V' ";
          }

    //add 處理單位簽核
    if (wp.itemEq("ex_apr_flag", "0")) {
        lsWhere += " and A.apr_flag in ('Y','N') ";
      } else if (wp.itemEq("ex_apr_flag", "Y")) {
        lsWhere += " and A.apr_flag ='Y' ";
      } else if (wp.itemEq("ex_apr_flag", "N")) {
        lsWhere += " and A.apr_flag ='N' ";
      }
  
    if (empty(wp.itemStr("ex_card_no")) == false) {
      if ((wp.itemStr("ex_card_no").length() == 15) || (wp.itemStr("ex_card_no").length() == 16)) {
        lsWhere += sqlCol(wp.itemStr2("ex_card_no"), "B.card_no");
      } else if (wp.itemStr("ex_card_no").length() == 10) {
        lsWhere += sqlCol(wp.itemStr2("ex_card_no"), "B.case_idno");
      } else {
        errmsg("卡號/身分證字號:輸入錯誤");
        return;
      }
      if (this.logQueryIdno(wp.itemStr("ex_card_no")) == false) {
        return;
      }
    }

  //部門代號add
    if (wp.itemEmpty("ex_dept_no") == false) {
        lsWhere += " and B.case_user IN ( SELECT usr_id FROM sec_user "
        		+ " where 1=1 "
              + sqlCol(wp.itemStr2("ex_dept_no"), "usr_deptno") + ") ";
      }
    
    if (wp.itemEq("ex_case_result", "0")) {
      lsWhere += " and A.proc_result in ('0','5','9') ";
    } else if (wp.itemEq("ex_case_result", "1")) {
      lsWhere += " and A.proc_result = '0' ";
    } else if (wp.itemEq("ex_case_result", "2")) {
      lsWhere += " and A.proc_result = '5' ";
    } else if (wp.itemEq("ex_case_result", "3")) {
      lsWhere += " and A.proc_result = '9' ";
    }
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "A.case_date," + "A.case_seqno," + "A.proc_deptno," + "A.proc_id,"
        + "A.proc_desc," + "A.proc_desc2,"
        + "A.proc_result||'.'||decode(A.proc_result,'0','未處理','5','處理中','9','完成') as tt_proc_result ,"
        + "B.card_no," + "B.case_type," + "B.case_idno," + "B.case_desc," + "B.case_desc2,"
        + "B.case_user ," + "B.finish_date," + "B.ugcall_flag,"
       // + "uf_hi_idno(B.case_idno) as wk_id," + "uf_hi_cardno(B.card_no) as wk_card_no,"
       + "B.case_idno as wk_id," + "B.card_no as wk_card_no,"
        + "1 as db_sum," + "B.case_user,"
        + "(select usr_cname from sec_user where usr_id =B.case_user) as tt_case_user,"
        // +"B.case_user||'_'||decode(B.case_user,'','',(select usr_cname from sec_user where usr_id
        // =B.case_user)) as wk_case_user , "
        + "A.case_conf_flag||'.'||decode(A.case_conf_flag,'R','退回', 'V','未簽核', 'N','免簽核' , 'Y','已簽核', A.case_conf_flag )  as tt_case_conf_flag,"
      //  + "decode(A.case_conf_flag,'Y','Y','R','Y','N','Y','N')||'.'||decode(A.case_conf_flag,'Y','已簽核', '未簽核', A.case_conf_flag )  as tt_case_conf_flag2,"
        + "A.apr_flag||'.'||decode(A.apr_flag,'Y','已簽核','未簽核')  as tt_apr_flag,"
        + "A.proc_deptno||'_'||decode(A.proc_deptno,'','',(select dept_name from ptr_dept_code where dept_code =A.proc_deptno)) as wk_proc_dept  ";
    wp.daoTable = "cms_casedetail A,cms_casemaster B  ";
    wp.whereOrder = " order by proc_deptno ";
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
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "cmsr6040";
    wp.pageRows = 999;
    String cond1;

    cond1 = "統計期間: " + commString.strToYmd(wp.itemStr("ex_cms_date_s")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_cms_date_e"));
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "cmsr6040.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

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
	  
	//部門代號add  
	  wp.colSet("ex_dept_no",wp.loginDeptNo);
	   if (empty(wp.loginDeptNo) || eqIgno(wp.loginDeptNo,"A401") || eqIgno(wp.loginDeptNo,"3144")) {
	      wp.colSet("wk_edit_dept","Y");
	   }
	   else wp.colSet("wk_edit_dept","");
  }

}
