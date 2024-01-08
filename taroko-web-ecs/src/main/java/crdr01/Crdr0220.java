/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-13  V1.00.00  yash       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      * 
 * 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package crdr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdr0220 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdr0220";

  String condWhere = "";
  String reportSubtitle = "";
  String reportSubtitle1 = "";

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
    String exBatchno = wp.itemStr("ex_batchno");
    String exFailProcCode = wp.itemStr("ex_fail_proc_code");
    String exProcDate1 = wp.itemStr("exDateS");
    String exProcDate2 = wp.itemStr("exDateE");
    String exCardNo1 = wp.itemStr("ex_card_no1");
    String exCardNo2 = wp.itemStr("ex_card_no2");
    String exType = wp.itemStr("ex_type");


    if (this.chkStrend(exProcDate1, exProcDate2) == false) {
      alertErr("重送件/退件處理日期(起迄) 輸入錯誤");
      return false;
    }

    if (this.chkStrend(exCardNo1, exCardNo2) == false) {
      alertErr("卡號(起迄) 輸入錯誤");
      return false;
    }

    String lsWhere = "where 1=1 ";

    if (empty(exBatchno) == false) {
      lsWhere += " and c.batchno = :ex_batchno";
      setString("ex_batchno", exBatchno);
    }

    if (empty(exFailProcCode) == false) {
      lsWhere += " and c.fail_proc_code = :ex_fail_proc_code";
      setString("ex_fail_proc_code", exFailProcCode);
    }

    if (empty(exProcDate1) == false) {
      lsWhere += " and c.proc_date >= :ex_proc_date1";
      setString("ex_proc_date1", exProcDate1);
    }

    if (empty(exProcDate2) == false) {
      lsWhere += " and c.proc_date <= :ex_proc_date2";
      setString("ex_proc_date2", exProcDate2);
    }

    if (empty(exCardNo1) == false) {
      lsWhere += " and c.card_no >= :ex_card_no1";
      setString("ex_card_no1", exCardNo1);
    }

    if (empty(exCardNo2) == false) {
      lsWhere += " and c.card_no <= :ex_card_no2";
      setString("ex_card_no2", exCardNo2);
    }

    if (empty(exType) == false) {
      if (exType.equals("1")) {

        lsWhere += " and decode(d.combo_indicator,'','N',d.combo_indicator) = 'N' ";

      } else if (exType.equals("2")) {

        lsWhere += " and decode(d.combo_indicator,'','N',d.combo_indicator) = 'Y' ";

      }
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
    // String ex_date1 = wp.item_ss("ex_date1");
    // setString("ex_date1", ex_date1);

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "" + "c.batchno||'-'||c.recno  as batchno , "
        + "decode(c.emboss_source,'1','新製卡','2','普昇金卡','3','換卡','4','提前續卡','5','毀損重製','6','掛失補發','7','緊急補發卡','8','星座卡毀損重製','9','重送件','') as emboss_source , "
        + "decode(c.emboss_reason,'0','','1','掛失','2','毀損','3','偽卡','4','星座卡重製','') as emboss_reason , "
        + "c.card_no , " + "uf_hi_cardno (c.card_no) db_card_no, "// 轉碼:附卡卡號
        + "c.card_type , " + "c.group_code," + "c.apply_id||'-'||c.apply_id_code as apply_id ,"
        + "(uf_hi_idno (c.apply_id) ||'-'||c.apply_id_code) db_apply_id, "// 轉碼:持卡人身分證號碼
        + "c.birthday," + "c.chi_name," + "c.pm_id||'-'||c.pm_id_code as pm_id,"
        + "(uf_hi_idno (c.pm_id) ||'-'||c.pm_id_code) db_pm_id, "// 轉碼:正卡人身分證號碼
        + "decode(c.reject_code,'F','IBM取三軌失敗',m.msg) as msg ," + "c.proc_date,"
        + "d.combo_indicator";

    wp.daoTable = " crd_proc_tmp c inner join ptr_group_code d on c.group_code = d.group_code ";
    wp.daoTable +=
        " left join crd_message m on c.reject_code = m.msg_value and m.msg_type = 'REJECT_CODE' ";
    wp.whereOrder = " order by c.batchno";
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    // setParameter();

    if (strAction.equals("XLS") || strAction.equals("PDF")) {
      selectNoLimit();
    }
    pageQuery();
    // list_wkdata();
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
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("user_id", wp.loginUser);
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-

      String co1 = "批號：" + wp.itemStr("ex_batchno") + "   "
          + (wp.itemStr("ex_fail_proc_code").equals("1") ? "退件" : "重送件") + "   " + "重送件/退件處理日："
          + commString.strToYmd(
              wp.itemStr("ex_proc_date1") + "~" + commString.strToYmd(wp.itemStr("ex_proc_date2")));
      String co2 =
          "卡號：" + wp.itemStr("ex_card_no1") + "~" + wp.itemStr("ex_card_no2") + "     " + "列印種類: "
              + (wp.itemStr("ex_type").equals("") ? "全部"
                  : wp.itemStr("ex_type").equals("1") ? "其他卡"
                      : wp.itemStr("ex_type").equals("2") ? "combo卡" : "");
      wp.colSet("cond_1", co1);
      wp.colSet("cond_2", co2);
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      // xlsx.report_id ="rskr0020";
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
    wp.colSet("cond_2", reportSubtitle1);
    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  void subTitle() {
    String exBatchno = wp.itemStr("ex_batchno");
    String exFailProcCode = wp.itemStr("ex_fail_proc_code");
    String exProcDate1 = wp.itemStr("exDateS");
    String exProcDate2 = wp.itemStr("exDateE");
    String exCardNo1 = wp.itemStr("ex_card_no1");
    String exCardNo2 = wp.itemStr("ex_card_no2");
    String exType = wp.itemStr("ex_type");
    String title = "";
    String title1 = "";

    if (empty(exBatchno) == false) {
      title += " 批號: " + exBatchno;
    }

    if (exFailProcCode.equals("1")) {
      title += " 類別:退件";
    } else {
      title += " 類別:重送";
    }

    if (!empty(exProcDate1) || !empty(exProcDate2)) {
      title += "  處理日期:";
      if (!empty(exProcDate1)) {
        title += exProcDate1;
      }
      title += " -- ";
      if (!empty(exProcDate2)) {
        title += exProcDate2;
      }
    }

    if (!empty(exCardNo1) || !empty(exCardNo2)) {
      title1 += "  卡號:";
      if (!empty(exCardNo1)) {
        title1 += exCardNo1;
      }
      title += " -- ";
      if (!empty(exCardNo2)) {
        title1 += exCardNo2;
      }
    }

    if (empty(exType) == true) {
      title1 += " 列印種類:全部";
    } else {
      if (exType.equals("1")) {
        title1 += " 列印種類:其他卡";
      }
      if (exType.equals("2")) {
        title1 += " 列印種類:combo卡";
      }
    }
    reportSubtitle = title;
    reportSubtitle1 = title1;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("ex_bin_no");
      this.dddwList("dddw_bin_no", "ptr_bintable", "bin_no", "card_desc", "where 1=1");

      wp.optionKey = wp.itemStr("ex_group_code");
      this.dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 order by group_code");
    } catch (Exception ex) {
    }
  }

}
