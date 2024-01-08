/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-12  V1.00.00   Andy Liu     program initial                            *
* 109-01-03  V1.00.01   Justin Wu    updated for archit.  change                
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *   
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *                                                       *
******************************************************************************/
package ipsr01;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Ipsr0030 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "ipsr0030";

  String condWhere = "";
  String reportSubtitle = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        // is_action="new";
        // clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      // case "R":
      // // -資料讀取-
      // is_action = "R";
      // dataRead();
      // break;
      // case "A":
      // /* 新增功能 */
      // saveFunc();
      // break;
      // case "U":
      // /* 更新功能 */
      // saveFunc();
      // break;
      // case "D":
      // /* 刪除功能 */
      // saveFunc();
      // break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "XLS":
        // -Excel-
        strAction = "XLS";
        // wp.setExcelMode();
        xlsPrint();
        break;
      case "PDF":
        // -PDF-
        strAction = "PDF";
        // wp.setExcelMode();
        pdfPrint();
        break;
      case "AJAX":
        // AJAX 20200102 updated for archit. change
        itemchanged();
        break;
      default:
        break;
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
    String sysdate1 = "", sysdate0 = "";

    SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_MONTH, -7);
    dateformat.format(c.getTime());
    sysdate0 = dateformat.format(c.getTime());
    Calendar d = Calendar.getInstance();
    d.add(Calendar.DAY_OF_MONTH, -1);
    dateformat.format(d.getTime());
    sysdate1 = dateformat.format(d.getTime());

    wp.colSet("exDateS", sysdate0);
    wp.colSet("exDateE", sysdate1);
  }

  private boolean getWhereStr() throws Exception {
	sqlParm.clear();
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exFileCode = wp.itemStr("ex_file_code");
    String exErrCode = wp.itemStr("ex_err_code");

    String lsWhere = "where 1=1  ";
    // 固定搜尋條件

    // user搜尋條件
    lsWhere += sqlStrend(exDateS, exDateE, "a.send_date");
    lsWhere += sqlCol(exFileCode, "a.file_code");
    lsWhere += sqlCol(exErrCode, "a.err_code");

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

    wp.selectSQL = "a.send_date, " + "a.file_code, " + "a.ips_card_no, " + "a.err_code, "
        + "a.card_ic_no, " + "b.card_no, " + "a.file_code||'_'||a.err_code as db_file_errcode ";
    wp.daoTable += "ips_file_errdata a " + "left join ips_card b on b.ips_card_no = a.ips_card_no ";
    wp.whereOrder = " order by a.send_date,a.file_code,a.err_code ";
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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exFileCode = wp.itemStr("ex_file_code");
    String exErrCode = wp.itemStr("ex_err_code");
    String tmpStr = "";
    // 傳送日期
    if (empty(exDateS) == false || empty(exDateE) == false) {
      tmpStr += "報送日期 : ";
      if (empty(exDateS) == false) {
        tmpStr += exDateS + " 起 ";
      }
      if (empty(exDateE) == false) {
        tmpStr += " ~ " + exDateE + " 迄 ";
      }
    }
    if (empty(exFileCode) == false) {
      tmpStr += "  檔案名稱 : " + exFileCode;
    }
    if (empty(exErrCode) == false) {
      tmpStr += "   錯誤代碼 : " + exErrCode;
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
      // dddw_list("dddw_group_code", "ptr_group_code", "group_code",
      // "group_name", "where 1=1 group by group_code,group_name order by
      // group_code");

      // dddw_branch
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_err_code");
      // dddw_list("dddw_err_code", "ptr_rskid_desc", "id_code",
      // "id_desc1||'_'||id_code||'_'||id_desc2", "where 1=1 and id_key
      // like 'IPS_ERR%' order by id_desc1,id_code");
    } catch (Exception ex) {
    }
  }

  public int itemchanged() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change

    String lsFileCode = "";
    String option = "";

    lsFileCode = wp.itemStr("file_code");
    String lsSql = "select wf_desc " + " ,wf_id " + " from ptr_sys_idtab " + " where 1=1 "
        + " and id_code =:id_code" + " order by wf_id ";
    setString("id_code", lsFileCode);
    sqlSelect(lsSql);

    if (sqlRowNum <= 0) {
      option += "<option value=''>--</option>";
    } else {
      option += "<option value=''>--</option>";
      for (int ii = 0; ii < sqlRowNum; ii++) {
        option += "<option value='" + sqlStr(ii, "wf_id") + "' ${ex_err_code-" + sqlStr(ii, "wf_id")
            + "} > " + sqlStr(ii, "wf_id") + "_" + sqlStr(ii, "wf_desc") + "</option>";
        // option += "<option value='" + sql_ss(ii, "batch_no_new") + "' ${ex_batch_no-" +
        // sql_ss(ii, "batch_no_new") + "} > " + sql_ss(ii, "batch_no_new") + "</option>";
      }
    }
    wp.addJSON("dddw_err_code", option);

    return 1;
  }
}
