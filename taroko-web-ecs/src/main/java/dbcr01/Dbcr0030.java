/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-08-11  V1.00.01  ryan                                                  * 
* 111-09-05  V1.00.02  ryan        增加排序                                                                                                     * 
******************************************************************************/
package dbcr01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbcr0030 extends BaseAction implements InfacePdf {
  private String progName = "Dbcr0030";
	
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
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      //pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {  
    try {
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {

    String lsWhere = " where 1=1 " 
        + sqlCol(wp.itemStr("ex_emboss_date1"), "A.emboss_date",">=")
        + sqlCol(wp.itemStr("ex_emboss_date2"), "A.emboss_date","<=")
        + sqlCol(wp.itemStr("ex_act_no"), "A.act_no")
        + sqlCol(wp.itemStr("ex_apply_id"), "A.apply_id") 
        + sqlCol(wp.itemStr("ex_card_no"), "A.card_no")
        ;
    if(wp.itemEq("ex_check_code_flag","Y")) {
    	lsWhere += " and check_code = '000' ";
    }
    if(wp.itemEq("ex_check_code_flag","N")) {
    	lsWhere += " and check_code != '000' ";
    }
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " A.BATCHNO , " + " A.RECNO , " + " A.EMBOSS_DATE , " + " A.ACT_NO , "
        + " A.APPLY_ID , " + " A.CARD_NO , " + " A.CHECK_CODE , " + " B.MSG ";
    wp.daoTable = "DBC_PRE_TMP A LEFT JOIN CRD_MESSAGE B ON A.CHECK_CODE = B.MSG_VALUE AND B.MSG_TYPE = 'NEW_CARD' ";
    wp.whereOrder = " ORDER BY A.BATCHNO,A.RECNO ";
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
    wp.reportId = progName;
    wp.pageRows = 9999;

    StringBuffer  condStr  = new StringBuffer();

	if (!wp.iempty("ex_emboss_date")) {
		condStr.append("  申請日期 : ");
	    condStr.append(wp.itemStr("ex_emboss_date"));
	}
	if (!wp.iempty("ex_act_no")) {
		condStr.append("  金融帳號 : ");
	    condStr.append(wp.itemStr("ex_act_no"));
	}
	if (!wp.iempty("ex_apply_id")) {
		condStr.append("  身分證字號 : ");
	    condStr.append(wp.itemStr("ex_apply_id"));
	}
	if (!wp.iempty("ex_card_no")) {
		condStr.append("  卡號 : ");
		condStr.append(wp.itemStr("ex_card_no"));
	}
	if (wp.itemEq("ex_check_code_flag","Y")) {
		condStr.append("  處理結果 : 成功");
	}
	if (wp.itemEq("ex_check_code_flag","N")) {
		condStr.append("  處理結果 : 失敗");
	}
    wp.colSet("cond1", condStr.toString());

    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "dbcr0030.xlsx";
    pdf.pageCount = 25;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }
  
  void xlsPrint() {
	    try {
	      log("xlsFunction: started--------");
	      wp.reportId = progName;
	      // -cond-
	      StringBuffer  condStr  = new StringBuffer();

	  	if (!wp.iempty("ex_emboss_date1")||!wp.iempty("ex_emboss_date2")) {
	  		condStr.append("  申請日期 : ");
	  	    condStr.append(wp.itemStr("ex_emboss_date1"));
	  	    condStr.append(" -- ");
	  	    condStr.append(wp.itemStr("ex_emboss_date2"));
	  	}
	  	
	  	if (!wp.iempty("ex_act_no")) {
	  		condStr.append("  金融帳號 : ");
	  	    condStr.append(wp.itemStr("ex_act_no"));
	  	}
	  	if (!wp.iempty("ex_apply_id")) {
	  		condStr.append("  身分證字號 : ");
	  	    condStr.append(wp.itemStr("ex_apply_id"));
	  	}
	  	if (!wp.iempty("ex_card_no")) {
	  		condStr.append("  卡號 : ");
	  		condStr.append(wp.itemStr("ex_card_no"));
	  	}
	  	if (wp.itemEq("ex_check_code_flag","Y")) {
	  		condStr.append("  處理結果 : 成功");
	  	}
	  	if (wp.itemEq("ex_check_code_flag","N")) {
	  		condStr.append("  處理結果 : 失敗");
	  	}
	      wp.colSet("cond1", condStr.toString());
	      // ===================================
	      TarokoExcel xlsx = new TarokoExcel();
	      wp.fileMode = "N";
	      xlsx.excelTemplate = progName + ".xlsx";

	      // ====================================
	      // -明細-
	      xlsx.sheetName[0] = "明細";
	      queryFunc();
	      wp.setListCount(1);
	      log("Detl: rowcnt:" + wp.listCount[0]);
	      xlsx.processExcelSheet(wp);
	      /*
	       * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where); wp.listCount[1] =sql_nrow;
	       * ddd("Summ: rowcnt:" + wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
	       */
	      xlsx.outputExcel();
	      xlsx = null;
	      log("xlsFunction: ended-------------");

	    } catch (Exception ex) {
	      wp.expMethod = "xlsPrint";
	      wp.expHandle(ex);
	    }
	  }

}
