/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-23  V1.00.00  Andy Liu      program initial                         *
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

public class Crdr0150 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdr0150";

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
    String exSource = wp.itemStr("ex_source");
    String exChangeReason = wp.itemStr("ex_change_reason");
    String exEmbossReason = wp.itemStr("ex_emboss_reason");
    String exKeyin = wp.itemStr("ex_keyin");
    String exBatchno = wp.itemStr("ex_batchno");
    String exType = wp.itemStr("ex_type");

    wp.whereStr = "where 1=1  ";
    wp.whereStr += "and a.group_code = c.group_code   " + "and c.card_type = a.card_type "
        + "and a.group_code = b.group_code ";

    switch (exSource) {
      case "3":
        wp.whereStr += " and a.emboss_source = '3' ";
        break;
      case "4":
        wp.whereStr += " and a.emboss_source = '4' ";
        if (empty(exChangeReason) == false) {
          switch (exChangeReason) {
            case "1":
              wp.whereStr += " and a.change_reason = '1' ";
              break;
            case "2":
              wp.whereStr += " and a.change_reason = '2' ";
              break;
            case "3":
              wp.whereStr += " and a.change_reason = '3' ";
              break;
          }
        }
        break;
      case "5":
        wp.whereStr += " and a.emboss_source = '5' ";
        if (empty(exEmbossReason) == false) {
          switch (exEmbossReason) {
            case "1":
              wp.whereStr += " and a.emboss_reason = '1' ";
              break;
            case "2":
              wp.whereStr += " and a.emboss_reason = '2' ";
              break;
            case "3":
              wp.whereStr += " and a.emboss_reason = '3' ";
              break;
          }
        }
        break;
    }

    if (empty(exKeyin) == false) {
      wp.whereStr += " and a.crt_user = :ex_keyin ";
      setString("ex_keyin", exKeyin);
    }

    if (empty(exBatchno) == false) {
      // wp.whereStr += " and a.batchno lik =:ex_batchno ";
      // setString("ex_batchno", ex_batchno+"%");
      wp.whereStr += sqlCol(exBatchno, "a.batchno", "like%");
    }

    switch (exType) {
      case "1":
        wp.whereStr += " and a.combo_indicator ='N' ";
        break;
      case "2":
        wp.whereStr += " and a.combo_indicator ='Y' ";
        break;
      case "4":
        wp.whereStr += " and c.card_mold_flag = 'M' ";
        break;
    }

    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    if (getWhereStr() == false)
//      return;
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

    wp.selectSQL = "" + "a.batchno, " + "a.recno, " + "a.emboss_source, " + "a.emboss_reason, "
        + "a.card_no, " + "uf_hi_cardno (a.card_no) db_card_no, "// 轉碼:新卡卡號
        + "a.apply_id, " + "a.apply_id_code, " + "a.chi_name, "
        + "uf_hi_cname(a.chi_name) db_chi_name, "// 轉碼:中文姓名
        + "a.group_code, " + "a.valid_to, " + "a.eng_name, " + "a.crt_date, " + "a.crt_user, "
        + "a.birthday, " + "a.emboss_4th_data, " + "a.card_type, " + "a.risk_bank_no, "
        + "a.reissue_code, " + "a.crt_user, " + "a.emboss_reason||a.reissue_code db_reissue_code, "
        + "lpad(' ',8,' ') db_emboss_reason, " + "lpad(' ',2,' ') db_confirm_flag, "
        + "a.apr_date, " + "a.change_reason, " + "a.combo_indicator, " + "a.msisdn," + "a.se_id,"
        + "a.service_type, " + "a.mail_type, " + "a.branch, " + "a.remark_20, " + "a.group_code, "
        + "a.unit_code, " + "b.combo_indicator, " + "c.card_mold_flag ";
    // 放行前/後 table不同
    String exApr = wp.itemStr("ex_apr");
    switch (exApr) {
      case "0":
        wp.daoTable = " crd_emboss_tmp a, ptr_group_code b, ptr_group_card c ";
        break;
      case "1":
        wp.daoTable = " crd_emboss a, ptr_group_code b, ptr_group_card c ";
        break;
    }
    // 排序條件
    String exOrder = wp.itemStr("ex_order");
    if (empty(exOrder) == false) {
      switch (exOrder) {
        case "0":
          wp.whereOrder = " order by a.batchno,a.recno asc ";
          break;
        case "1":
          wp.whereOrder = " order by a.apply_id,a.batchno,a.recno asc ";
          break;
        case "2":
          wp.whereOrder = " order by a.card_no,a.batchno,a.recno asc ";
          break;
        case "3":
          wp.whereOrder = " order by a.reissue_code,a.batchno,a.recno asc ";
          break;
        case "4":
          wp.whereOrder = " order by a.crt_user,a.batchno,a.recno asc ";
          break;
        case "5":
          wp.whereOrder = " order by a.card_type,a.group_code,a.batchno,a.recno asc ";
          break;
      }
    }

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr);
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
    int rowCt = 0;
    String lsSql = "";
    String wpBatchno = "", wpRecno = "", wpEmbossSource, wpEmbossReason = "", wpChangeReason = "",
        wpReissueCode = "";
    String wkBatchno = "", dbConfirmFlag = "", dbEmbossReason = "";
    String wkApplyId = "";
    String dbApplyId = "";
    wp.logSql = false;
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      // 計算欄位
      rowCt += 1;
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

      // db_apply_id身分證字號隱第4~7碼(給報表使用)
      dbApplyId = commString.hideIdno(wkApplyId);
      wp.colSet(ii, "db_apply_id", dbApplyId);

      // emboss_source
      String wpEmbossSource1 = wpEmbossSource;
      String[] cde = new String[] {"1", "2", "3", "4", "5", "7"};
      String[] txt = new String[] {"新製卡", "普昇金卡", "整批續卡", "提前續卡", "重製", "緊急補發"};
      wp.colSet(ii, "emboss_source", commString.decode(wpEmbossSource1, cde, txt));

      // db_emboss_reason
      lsSql = "select wf_type, wf_id, wf_desc from ptr_sys_idtab ";
      lsSql += " where wf_type = 'REISSUE_REASON' ";
      lsSql += " and wf_id = :wp_emboss_reason ";
      setString("wp_emboss_reason", wpEmbossReason);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        dbEmbossReason = sqlStr("wf_desc");
      }
      // if(wp_emboss_source.equals("5")){
      // db_emboss_reason = wp_emboss_reason;
      // }
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
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {
    String exSource = wp.itemStr("ex_source");
    String exChangeReason = wp.itemStr("ex_change_reason");
    String exEmbossReason = wp.itemStr("ex_emboss_reason");
    String exKeyin = wp.itemStr("ex_keyin");
    String exBatchno = wp.itemStr("ex_batchno");
    String exType = wp.itemStr("ex_type");
    String exOrder = wp.itemStr("ex_order");
    String cond1 = "";
    //String ss1 = "";
    String exApr = wp.itemStr("ex_apr");

    // 報表種類
    switch (exSource) {
      case "3":
        exChangeReason += "整批續卡彙整總表";
        break;
      case "4":
        cond1 += "線上續卡彙整總表";
        if (empty(exChangeReason) == false) {
          switch (exChangeReason) {
            case "1":
              exChangeReason = "系統續卡";
              break;
            case "2":
              exChangeReason = "提前續卡";
              break;
            case "3":
              exChangeReason = "人工續卡";
              break;
          }
          exChangeReason += "提前續卡註記: " + exChangeReason;
        }
        break;
      case "5":
        cond1 += "重製卡彙整總表";
        if (empty(exEmbossReason) == false) {
          switch (exEmbossReason) {
            case "1":
              exEmbossReason = "掛失";
              break;
            case "2":
              exEmbossReason = "損毀";
              break;
            case "3":
              exEmbossReason = "偽卡";
              break;
          }
          exChangeReason += "  重製卡原因: " + exEmbossReason;
        }
        break;
    }
    // 表頭放行前後
    if (exApr.equals("0")) {
      cond1 += "放行前";
    } else {
      cond1 += "放行後";
    }
    cond1 += exChangeReason;
    // 列印選項
    if (empty(exKeyin) == false) {
      cond1 += " 登錄人員: " + exKeyin;
    }
    if (empty(exBatchno) == false) {
      cond1 += " 批號: " + exBatchno;
    }
    if (empty(exType) == false) {
      switch (exType) {
        case "0":
          exType = "全部";
          break;
        case "1":
          exType = "其他";
          break;
        case "2":
          exType = "COMBO卡";
          break;
      }
      cond1 += " 列印種類: " + exType;
    }

    if (empty(exOrder) == false) {
      switch (exOrder) {
        case "1":
          exOrder = "ID";
          break;
        case "2":
          exOrder = "卡號";
          break;
        case "3":
          exOrder = "製卡說明";
          break;
        case "4":
          exOrder = "登錄人員";
          break;
        case "5":
          exOrder = "卡種團代";
          break;
      }
      cond1 += " 排序: " + exOrder;
    }
    wp.colSet("cond_1", cond1);
    // wp.col_set("cond_2", ss1);
    reportSubtitle = cond1;
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
      wp.fileMode = "Y";
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
    wp.pageRows = 99999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 30;
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
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_keyin");
      dddwList("dddw_keyin", "sec_user", "usr_id", "usr_cname", "where 1=1 ");

      // dddw_group_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_group_code");
      // dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1 group
      // by group_code,group_name order by group_code");
    } catch (Exception ex) {
    }
  }

}

