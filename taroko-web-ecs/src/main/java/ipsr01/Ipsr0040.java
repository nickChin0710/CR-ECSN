/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-12  V1.00.00  Andy Liu   program initial                            *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package ipsr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Ipsr0040 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "ipsr0040";

  String condWhere = "";
  String reportSubtitle = "";
  String queryTable ="";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
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
      // wp.setExcelMode();
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      // wp.setExcelMode();
      pdfPrint();
    }

    dddwSelect();
    // init_button();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    // String sysdate1="",sysdate0="";
    // sysdate1 = ss_mid(get_sysDate(),0,8);
    // 續卡日期起-迄日
    // wp.col_set("exDateS", "");
    // wp.col_set("exDateE", sysdate1);
  }

  private boolean getWhereStr() throws Exception {
	sqlParm.clear();
    String exCrtDateS = wp.itemStr("exCrtDateS");
    String exCrtDateE = wp.itemStr("exCrtDateE");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exIdno = wp.itemStr("ex_idno");
    String exCardNo = wp.itemStr("ex_card_no");
    String exIpsCard = wp.itemStr("ex_ips_card");
    String exType = wp.itemStr("ex_type");

    String lsWhere = "where 1=1  ";
    // 固定搜尋條件

    // user搜尋條件
    
    lsWhere += sqlStrend(exCrtDateS, exCrtDateE, "a.crt_date");
    lsWhere += sqlStrend(exDateS, exDateE, "a.txn_date");
    lsWhere += sqlCol(exIdno, "c.id_no");
    lsWhere += sqlCol(exCardNo, "a.card_no", "like%");
    lsWhere += sqlCol(exIpsCard, "a.ips_card_no", "like%");
    if ("D".equals(exType)) {
    	queryTable = "ips_i2b005_log";  //日檔
    } else {
    	queryTable = "ips_i2b00a_log";  //10日彙總檔
    }

    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
    // cond_where = wp.whereStr + "";
    // wp.whereStr =cond_where;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  private void setParameter() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "a.card_no, " + "a.ips_card_no, " + "a.txn_date, " + "a.txn_amt, "
        + "a.crt_date, " + "a.txn_bal, " + "c.id_no, " + "c.chi_name, " + "a.txn_type ";
    wp.daoTable += queryTable+ " a" + " left join crd_card b on a.card_no = b.card_no "
        + " left join crd_idno c on b.id_p_seqno = c.id_p_seqno ";
    wp.whereOrder = " order by a.crt_date,a.txn_date,a.card_no,a.ips_card_no ";
    getWhereStr();

    // System.out.println("sqlCmd : "+wp.sqlCmd);
    // wp.pageCount_sql ="select count(*) from (";
    // wp.pageCount_sql += wp.sqlCmd;
    // wp.pageCount_sql += ")";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    // wp.daoTable);

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
    listWkdata();
  }


  void listWkdata() throws Exception {
    int rowCt = 0;
    String lsSql = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      // wp.col_set(ii,"group_cnt","1");
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {
	String exCrtDateS = wp.itemStr("exCrtDateS");
	String exCrtDateE = wp.itemStr("exCrtDateE");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exIdno = wp.itemStr("ex_idno");
    String exCardNo = wp.itemStr("ex_card_no");
    String exIpsCard = wp.itemStr("ex_ips_card");
    String tmpStr = "";
    // 傳送日期
    if (empty(exCrtDateS) == false || empty(exCrtDateE) == false) {
        tmpStr += "處理日期 : ";
        if (empty(exCrtDateS) == false) {
          tmpStr += exCrtDateS + " 起 ";
        }
        if (empty(exCrtDateE) == false) {
          tmpStr += " ~ " + exCrtDateE + " 迄 ";
        }
      }
    if (empty(exDateS) == false || empty(exDateE) == false) {
      tmpStr += "退費日期 : ";
      if (empty(exDateS) == false) {
        tmpStr += exDateS + " 起 ";
      }
      if (empty(exDateE) == false) {
        tmpStr += " ~ " + exDateE + " 迄 ";
      }
    }
    if (empty(exIdno) == false) {
      tmpStr += "  身分證ID : " + exIdno;
    }
    if (empty(exCardNo) == false) {
      tmpStr += "   卡號 : " + exCardNo;
    }
    if (empty(exIpsCard) == false) {
      tmpStr += "   一卡通卡號 : " + exIpsCard;
    }


    reportSubtitle = tmpStr;

  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + ".xlsx";

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

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_group_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_group_code");
      // dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1 group
      // by group_code,group_name order by group_code");

      // dddw_branch
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_err_code");
      // dddw_list("dddw_err_code", "ptr_rskid_desc", "id_code",
      // "id_desc1||'_'||id_code||'_'||id_desc2", "where 1=1 and id_key like 'IPS_ERR%' order by
      // id_desc1,id_code");
    } catch (Exception ex) {
    }
  }

}

