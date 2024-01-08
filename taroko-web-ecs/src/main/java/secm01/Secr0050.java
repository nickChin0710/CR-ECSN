/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-06-17  V1.00.01  ryan                                                  * 
******************************************************************************/
package secm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Secr0050 extends BaseAction implements InfacePdf {

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
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
	  
	  
    try {
        wp.optionKey = wp.colStr("ex_group_id");
        dddwList("dddw_group_id", "sec_workgroup", "group_id", "group_name", "where 1=1");

        wp.optionKey = wp.colStr("ex_user_level");
        dddwList("dddw_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC_USRLVL'");
        
        wp.optionKey = wp.colStr("ex_branch");
        dddwList("dddw_branch", "gen_brn", "branch", "full_chi_name",
                "where 1=1 group by branch,full_chi_name order by branch");


    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
	if (chkStrend(wp.itemStr("ex_user_id1"), wp.itemStr("ex_user_id2")) == false) {
		 alertErr2("使用者代號起迄：輸入錯誤");
		 return;
	}

    String lsWhere = " where 1=1 " 
        + sqlCol(wp.itemStr("ex_branch"), "bank_unitno")
        + sqlCol(wp.itemStr("ex_group_id"), "usr_group","%like%")
        + sqlCol(wp.itemStr("ex_user_level"), "usr_level") 
        + sqlCol(wp.itemStr("ex_user_id1"), "usr_id",">=")
        + sqlCol(wp.itemStr("ex_user_id2"), "usr_id","<=")
        ;
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " " + " usr_id , " + " usr_cname , " + " usr_empno , " + " usr_deptno , "
        + " usr_type , "
        + " usr_level , " + " usr_group , " + " usr_indate , "
        + " usr_intime , " + " bank_unitno  " ;
    wp.daoTable = "SEC_USER ";
    wp.whereOrder = " order by bank_unitno,usr_level,usr_id ";
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
  public void pdfPrint() throws Exception {
    wp.reportId = "Secr0050";
    wp.pageRows = 9999;
    String cond1;

    StringBuffer  condStr  = new StringBuffer();

	if (!wp.iempty("ex_branch")) {
		condStr.append("  分行代號:");
	    condStr.append(wp.itemStr("ex_branch"));
	}
	if (!wp.iempty("ex_group_id")) {
		condStr.append("  歸屬群組(子系統):");
	    condStr.append(wp.itemStr("ex_group_id"));
	}
	if (!wp.iempty("ex_user_level")) {
		condStr.append("  使用者層級:");
	    condStr.append(wp.itemStr("ex_user_level"));
	}
	if (!wp.iempty("ex_user_id1") || !wp.iempty("ex_user_id2")) {
		condStr.append("  使用者代號:");
		condStr.append(wp.itemStr("ex_user_id1"));
		condStr.append("--");
		condStr.append(wp.itemStr("ex_user_id2"));
	}
    wp.colSet("cond1", condStr.toString());

    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "secr0050.xlsx";
    pdf.pageCount = 25;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
