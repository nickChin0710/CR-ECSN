package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*  109-01-04  V1.00.01   shiyuqi       修改无意义命名             
*  110-01-14  V1.00.02  JustinWu             fix parameterize sql errors   
*  110-01-15  V1.00.03  Justinwu         fix a bug                                                                      *  
*/
import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Ccar0410 extends BaseAction implements InfaceExcel, InfacePdf {

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
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      xlsPrint();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.colStr(0, "ex_dept_no");
      dddwList("dddw_dept_no", "ptr_dept_code", "dept_code", "dept_name", "where 1=1");
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    getWhere();
    wp.setQueryMode();

    queryRead();

  }

  void getWhere() {
	  sqlParm.clear();
    String lsWhere = " where 1=1 and blk_review_flag = '0' and log_not_reason = '' "
        + sqlCol(wp.itemStr("ex_log_date"), "log_date", "like%")
        + sqlCol(wp.itemStr("ex_dept_no"), "user_dept_no")
        + sqlCol(wp.itemStr("ex_proc_user"), "mod_user")
        + sqlCol(wp.itemStr("ex_remark"), "log_remark", "%like%");

    if (wp.itemEq("ex_type", "1")) {
      lsWhere += " and kind_flag = 'C' and log_type ='6' ";
    } else if (wp.itemEq("ex_type", "2")) {
      lsWhere += " and kind_flag = 'A' and log_type ='3' ";
    }

    if (!wp.itemEmpty("ex_block_reason")) {
      lsWhere +=
          " and ? in (spec_status,block_reason,block_reason2,block_reason3,block_reason4,block_reason5) ";
      setString(wp.itemStr("ex_block_reason"));
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    getWhere();

    wp.selectSQL = " user_dept_no , " + " mod_user , " + " card_no , "
        + " uf_idno_id2(card_no,'') as id_no , "
        + " acct_type||'-'||uf_acno_key2(acno_p_seqno,acct_type) as acct_key , " + " log_date , "
        + " spec_status||'/'||block_reason||','||block_reason2||','||block_reason3||','||block_reason4||','||block_reason5 as wk_block_status , "
        + " spec_del_date , " + " log_remark , " + " uf_hi_cardno(card_no) as hh_cardno ,"
        + " uf_hi_idno(uf_idno_id2(card_no,'')) as hh_idno , "
        + " acct_type||'-'||uf_hi_idno(uf_acno_key2(acno_p_seqno,acct_type)) as hh_acctkey ";

    wp.daoTable = " rsk_acnolog ";
    wp.whereOrder = " order by 1 , 2 , 3 , 5 ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
    wp.setListCount(0);

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
    wp.reportId = "ccar0410";
    String cond1 = "查詢日期: " + commString.strToYmd(wp.itemStr("ex_log_date"));

    if (wp.itemEq("ex_type", "1")) {
      cond1 += " 類別:卡片特指";
    } else {
      cond1 += " 類別:戶凍結/特指";
    }

    wp.colSet("cond1", cond1);
    wp.pageRows = 9999;
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ccar0410.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "ccar0410";
      String cond1 = "檢視日期: " + commString.strToYmd(wp.itemStr("ex_log_date"));

      if (wp.itemEq("ex_type", "1")) {
        cond1 += " 類別:卡片特指";
      } else {
        cond1 += " 類別:戶凍結/特指";
      }

      wp.colSet("cond1", cond1);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "ccar0410.xlsx";
      wp.pageRows = 9999;
      queryFunc();
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
