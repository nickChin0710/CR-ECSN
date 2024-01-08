/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-05  V1.00.00  yash       program initial                            *
* 107-04-13  V1.00.01  Andy       Update UI,report format                    *
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *	
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *	
******************************************************************************/
package genr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Genr0030 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "genr0030";
  String reportSubtitle = "";

  String cond_where = "";

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

  private boolean getWhereStr() throws Exception {
    String stdVouchCdF = wp.itemStr("ex_std_vouch_cd_f");
    String stdVouchCdU = wp.itemStr("ex_std_vouch_cd_u");
    String curr = wp.itemStr("ex_curr");

    String lsWhere = "where 1=1";

    if (empty(stdVouchCdF) == false) {
      lsWhere += " and std_vouch_cd >= :std_vouch_cd_f";
      setString("std_vouch_cd_f", stdVouchCdF);
    }

    if (empty(stdVouchCdU) == false) {
      lsWhere += " and std_vouch_cd <= :std_vouch_cd_u";
      setString("std_vouch_cd_u", stdVouchCdU);
    }

    lsWhere += sqlCol(curr, "curr", "like%");


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

    wp.selectSQL = "" + "v.std_vouch_cd , " + "p.curr_chi_name , " + "v.dbcr , " + "v.dbcr_seq , "
        + "v.ac_no , " + "m.ac_full_name , "
        + "decode(v.memo3_kind,'1','卡號','2','身分證號碼','3','YYYYMMDDUBOOOO','') as  memo3_kind, "
        + "v.memo1 , " + "v.memo2 , " + "v.memo3  ";
    wp.daoTable = "gen_std_vouch v " + "left join ptr_currcode p on v.curr=p.curr_code_gl "
        + "left join gen_acct_m m on v.ac_no=m.ac_no ";
    wp.whereOrder = " order by v.std_vouch_cd";

    // setParameter();
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
    String stdVouchCdF = wp.itemStr("ex_std_vouch_cd_f");
    String stdVouchCdU = wp.itemStr("ex_std_vouch_cd_u");
    String curr = wp.itemStr("ex_curr");
    String tmpStr = "";
    // 傳送日期
    if (empty(stdVouchCdF) == false || empty(stdVouchCdU) == false) {
      tmpStr += "標準分錄代碼 : ";
      if (empty(stdVouchCdF) == false) {
        tmpStr += stdVouchCdF + " 起 ";
      }
      if (empty(stdVouchCdU) == false) {
        tmpStr += " ~ " + stdVouchCdU + " 迄 ";
      }
    }
    if (empty(curr) == false) {
      tmpStr += " 幣別 : ";
      tmpStr += curr;
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
      wp.pageRows = 99999;
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
    wp.pageRows = 9999;
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

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_std_vouch_cd_f");
      this.dddwList("dddw_std_vouch_cd_f", "gen_std_vouch", "std_vouch_cd", "",
          "where 1=1 group by std_vouch_cd order by std_vouch_cd");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_std_vouch_cd_u");
      this.dddwList("dddw_std_vouch_cd_u", "gen_std_vouch", "std_vouch_cd", "",
          "where 1=1 group by std_vouch_cd order by std_vouch_cd");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_curr");
      this.dddwList("dddw_curr", "ptr_currcode", "curr_code_gl", "curr_chi_name",
          "where 1=1 group by curr_code_gl,curr_chi_name order by curr_code_gl");

    } catch (Exception ex) {
    }
  }

}
