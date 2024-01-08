/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-12  V1.00.00  Andy Liu   program initial                            *
* 106-12-14            Andy		  update : ucStr==>zzstr                     *
* 109-04-23  V1.00.03  yanghan  修改了變量名稱和方法名稱*
* 109-12-25   V1.00.04  Justin      parameterize sql
* 110/1/4    V1.00.05  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package dbcr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbcr0080 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbcr0080";

  String condWhere = "";
  String reportSubtitle = "";

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
	  sqlParm.clear();
    String exEmbossSource = wp.itemStr("ex_emboss_source");
    String exChangeReason = wp.itemStr("ex_change_reason");
    String exEmbossReason = wp.itemStr("ex_emboss_reason");
    String exBatchno = wp.itemStr("ex_batchno");
    String exDigitalFlag = wp.itemStr("ex_digital_flag");

    String lsWhere = "where 1=1  ";

    // ex_emboss_source送製卡來源==> 3 ,4==>ex_change_reason提前續卡註記 ,5==>ex_emboss_reason重製卡原因
    switch (exEmbossSource) {
      case "3":
        lsWhere += " and emboss_source = '3' ";
        break;
      case "4":
        lsWhere += " and emboss_source = '4' ";
        if (empty(exChangeReason) == false) {
          lsWhere += sqlCol(exChangeReason, "change_reason");
        }
        break;
      case "5":
        lsWhere += " and emboss_source = '5' ";
        if (empty(exEmbossReason) == false) {
          lsWhere += sqlCol(exEmbossReason, "emboss_reason");
        }
        break;
    }

    // ex_batchno批號
    if (empty(exBatchno) == false) {
      lsWhere += sqlCol(exBatchno, "batchno");
    }

    // ex_digital_flag存戶別
    if (exDigitalFlag.equals("0") == false) {
      if (exDigitalFlag.equals("Y")) {
        lsWhere += " and digital_flag ='Y' ";
      } else {
        lsWhere += " and decode(digital_flag,'','N',digital_flag) ='N' ";
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

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "" + "batchno, " + "recno, " + "(batchno||'-'||recno) wk_batchno, "
        + "emboss_source, " + "emboss_reason, " + "card_no, " + "apply_id, " + "apply_id_code, "
        + "(apply_id||'-'|| apply_id_code) wk_apply_id, " + "chi_name, " + "group_code, "
        + "valid_to, " + "eng_name, " + "crt_date, " + "birthday, " + "emboss_4th_data, "
        + "card_type, " + "risk_bank_no, " + "reissue_code, " + "crt_user, "
        + "(emboss_reason||reissue_code) db_reissue_code, " + "lpad(' ',8,' ') db_emboss_reason, "
        + "lpad(' ',2,' ') db_confirm_flag, " + "change_reason, " + "receipt_branch, "
        + "receipt_remark,  " + "digital_flag, "
        + "decode(digital_flag,'Y','數位存款帳戶','一般存款帳戶') db_digital_flag ";

    // 放行前/後 table不同
    String exApr = wp.itemStr("ex_apr");
    switch (exApr) {
      case "0":
        wp.daoTable = " dbc_emboss_tmp ";
        break;
      case "1":
        wp.daoTable = " dbc_emboss ";
        break;
    }

    // 排序條件
    String exOrder = wp.itemStr("ex_order");
    if (empty(exOrder) == false) {
      switch (exOrder) {
        case "1":
          wp.whereOrder = " order by apply_id asc ";
          break;
        case "2":
          wp.whereOrder = " order by card_no asc ";
          break;
        case "3":
          wp.whereOrder = " order by reissue_code asc ";
          break;
        case "4":
          wp.whereOrder = " order by crt_user asc ";
          break;
      }
    }

    // setParameter();
    // System.out.println(" select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr);
    // wp.daoTable);

    if (strAction.equals("XLS")) {
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
    int row = 0;
    String lsSql = "";
    String wpBatchno = "", wpRecno = "", wpEmbossSource, wpEmbossReason = "",
        wpChangeReason = "", wpReissueCode = "";
    String wkBatchno = "", dbConfirmFlag = "", dbEmbossReason = "";
    String wkApplyId = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      row += 1;

      wpBatchno = wp.colStr(ii, "batchno");
      wpRecno = wp.colStr(ii, "recno");
      wpEmbossSource = wp.colStr(ii, "emboss_source");
      wpEmbossReason = wp.colStr(ii, "emboss_reason");
      wpChangeReason = wp.colStr(ii, "change_reason");
      wpReissueCode = wp.colStr(ii, "reissue_code");
      // wk_batchno批號
      wkBatchno = wpBatchno + "-" + wpRecno;
      wp.colSet(ii, "wk_batchno", wkBatchno);
      // db_confirm_flag 覆核標籤
      if (empty(wp.colStr(ii, "confirm_date"))) {
        dbConfirmFlag = "Y";
      } else {
        dbConfirmFlag = "N";
      }
      wp.colSet(ii, "db_confirm_flag", dbConfirmFlag);

      // wk_apply_id身分證號
      wkApplyId = wp.colStr(ii, "apply_id") + "-" + wp.colStr(ii, "apply_id_code");
      wp.colSet(ii, "wk_apply_id", wkApplyId);

      // emboss_source中文
      String wpEmbossSourceTmp1 = wpEmbossSource;
      String[] cde = new String[] {"1", "2", "3", "4", "5", "7"};
      String[] txt = new String[] {"新製卡", "普昇金卡", "整批續卡", "提前續卡", "重製", "緊急補發"};
      wp.colSet(ii, "emboss_source", commString.decode(wpEmbossSourceTmp1, cde, txt));

      // db_emboss_reason
      lsSql = "select wf_type, wf_id, wf_desc from ptr_sys_idtab ";
      lsSql += " where wf_type = 'REISSUE_REASON' ";
      lsSql += " and wf_id = :wp_emboss_reason ";
      setString("wp_emboss_reason", wpEmbossReason);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        dbEmbossReason = sqlStr("wf_desc");
      }

      // db_emboss_reason
      if (wpEmbossSource.equals("4")) {
        switch (wpChangeReason) {
          case "1":
            dbEmbossReason = "系統續卡";
            break;
          case "2":
            dbEmbossReason = "提前續卡";
            break;
          case "3":
            dbEmbossReason = "人工續卡";
            break;
        }
      }
      wp.colSet(ii, "db_emboss_reason", dbEmbossReason);

      // db_reissue_code 中文
      lsSql =
          "select reissue_reason, reissue_code, content ,reissue_reason||reissue_code db_code from ptr_reissue_code ";
      lsSql += " where reissue_reason = :wp_emboss_reason ";
      setString("wp_emboss_reason", wpEmbossReason);
      lsSql += " and reissue_code = :wp_reissue_code";
      setString("wp_reissue_code", wpReissueCode);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wpReissueCode = sqlStr("db_code") + "-" + sqlStr("content");
      }
      wp.colSet(ii, "db_reissue_code", wpReissueCode);
    }
    wp.colSet("row_ct", intToStr(row));
  }

  void subTitle() {
    String exApr = wp.itemStr("ex_apr");
    String exEmbossSource = wp.itemStr("ex_emboss_source");
    String exChangeReason = wp.itemStr("ex_change_reason");
    String exEmbossReason = wp.itemStr("ex_emboss_reason");
    String exBatchno = wp.itemStr("ex_batchno");
    String exOrder = wp.itemStr("ex_order");
    String exDigitalFlag = wp.itemStr("ex_digital_flag");

    String tmpStr = "";
    // ex_apr放行前後
    String[] cde = new String[] {"0", "1"};
    String[] txt = new String[] {"放行前", "放行後"};
    tmpStr += " 放行前後 : " + commString.decode(exApr, cde, txt);

    // ex_emboss_source送製卡來源
    String[] cde1 = new String[] {"3", "4", "5"};
    String[] txt1 = new String[] {"整批續卡", "線上續卡", "重製卡"};
    tmpStr += " 送製卡來源 : " + commString.decode(exEmbossSource, cde1, txt1);

    // ex_change_reason提前續卡註記
    if (exEmbossSource.equals("4") && empty(exChangeReason) == false) {
      String[] cde2 = new String[] {"1", "2", "3"};
      String[] txt2 = new String[] {"系統續卡", "提前續卡", "人工續卡"};
      tmpStr += " 提前續卡註記 : " + commString.decode(exChangeReason, cde2, txt2);
    }

    // ex_emboss_reason重製卡原因
    if (exEmbossSource.equals("5") && empty(exEmbossReason) == false) {
      String[] cde3 = new String[] {"1", "2", "3"};
      String[] txt3 = new String[] {"掛失", "損毀", "偽卡"};
      tmpStr += " 重製卡原因 : " + commString.decode(exEmbossReason, cde3, txt3);
    }

    // ex_batchno批號
    if (empty(exBatchno) == false) {
      tmpStr += " 批號 : " + exBatchno;
    }

    // ex_order排序
    if (empty(exOrder) == false) {
      String[] cde4 = new String[] {"1", "2", "3", "4"};
      String[] txt4 = new String[] {"ID", "卡號", "重製說明", "登錄人員"};
      tmpStr += " 排序 : " + commString.decode(exOrder, cde4, txt4);
    }

    // ex_digital_flag存戶別
    String[] cde5 = new String[] {"0", "N", "Y"};
    String[] txt5 = new String[] {"全部", "一般存款帳戶", "數位存款帳戶"};
    tmpStr += " 存戶別 : " + commString.decode(exDigitalFlag, cde5, txt5);

    reportSubtitle = tmpStr;
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = progName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);

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

  void pdfPrint() throws Exception {
    wp.reportId = progName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);

    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = progName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    // pdf.pageVert= true; //直印
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
      // dddw_keyin
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_keyin");
      // dddw_list("dddw_keyin", "sec_user", "usr_id", "usr_cname", "where 1=1 ");

      // dddw_group_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_group_code");
      // dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1 group
      // by group_code,group_name order by group_code");
    } catch (Exception ex) {
    }
  }

}

