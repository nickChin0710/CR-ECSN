/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-26  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
 * 109-12-30  V1.00.02  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package crdr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdr0080 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdr0080";
  String reportSubtitle = "";
  String reportSubtitle1 = "";
  String condWhere = "";

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
    String exKind = wp.itemStr("ex_kind");
    String exExpireFlag = wp.itemStr("ex_expire_flag");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exChangeFlag = wp.itemStr("ex_change_flag");
    String exIdNo = wp.itemStr("ex_id_no");
    String exCardNo = wp.itemStr("ex_card_no");
    String exApr = wp.itemStr("ex_apr");

    if (exApr.equals("2") && empty(exDateS)) {
      alertErr("請輸入放行日期起日!!");
      return false;
    }

    wp.whereStr = "where 1=1  ";

    switch (exKind) {
      case "1":
        wp.whereStr += " and b.kind_type = '080' ";
        switch (exExpireFlag) {
          case "1":
            wp.whereStr += " and b.process_kind = '1' ";
            break;
          case "2":
            wp.whereStr += " and b.process_kind = '2' ";
            break;
          case "3":
            wp.whereStr += " and b.process_kind = '3' ";
            break;
          case "4":
            wp.whereStr += " and b.process_kind = '4' ";
            break;
        }
        break;
      case "2":
        wp.whereStr += " and  b.kind_type = '120' ";
        switch (exChangeFlag) {
          case "1":
            wp.whereStr += " and b.process_kind = '1' ";
            break;
          case "2":
            wp.whereStr += " and b.process_kind = '2' ";
            break;
          case "3":
            wp.whereStr += " and b.process_kind = '3' ";
            break;
        }
        break;
    }

    if (empty(exDateS) == false) {
      wp.whereStr += " and b.apr_date >= :exDateS ";
      setString("exDateS", exDateS);
    }

    if (empty(exDateE) == false) {
      wp.whereStr += " and b.apr_date <= :exDateE ";
      setString("exDateE", exDateE);
    }
    if (empty(exIdNo) == false) {
      wp.whereStr +=
          " and a.id_p_seqno = (select id_p_seqno from crd_idno where 1=1 and id_no =:ex_id) ";
      setString("ex_id", exIdNo);
    }
    if (empty(exCardNo) == false) {
      wp.whereStr += " and a.card_no = :ex_card_no ";
      setString("ex_card_no", exCardNo);
    }


    // setParameter();
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

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "" + "a.sup_flag, " + "b.card_no, " + "uf_hi_cardno (b.card_no) db_card_no, "// 轉碼:卡號
        + "uf_idno_id(b.id_p_seqno) id_no, " + "uf_hi_idno (uf_idno_id(b.id_p_seqno)) db_id_no, "// 轉碼:身分證號碼
        + "uf_idno_name(b.id_p_seqno) chi_name, "
        + "uf_hi_cname(uf_idno_name(b.id_p_seqno)) db_hi_cname, "// 轉碼:姓名
        + "b.corp_no, " + "b.change_reason, " + "b.change_status, " + "b.change_date, "
        + "b.expire_reason, " + "b.expire_chg_flag, " + "b.expire_chg_date, "
        + "decode(b.change_reason_old,'1','系統續卡','2','提前續卡','3','人工續卡') as change_reason_old, "
        + "b.change_status_old, " + "b.change_date_old, " + "b.expire_reason_old, "
        + "decode(b.expire_chg_flag_old,'1','預約不續卡','4','人工不續卡','5','系統不續卡') as expire_chg_flag_old, "
        + "b.expire_chg_date_old, " + "b.old_end_date, " + "b.cur_end_date, " + "b.cur_beg_date, "
        + "b.crt_user, " + "b.crt_date, " + "b.apr_user, " + "b.apr_date, " + "a.group_code, "
        + "a.new_end_date, " + "a.new_beg_date, " + "lpad(' ',20,' ') db_expire_chg_date, "
        + "lpad(' ',20,' ') db_expire_chg_flag, " + "lpad(' ',20,' ') db_expire_reason, "
        + "lpad(' ',20,' ') db_expire_reason_old, " + "'    ' db_sup_flag, " + "b.mod_time, "
        + "b.process_kind, " + "lpad(' ',20,' ') db_process_kind ";
    String exApr = wp.itemStr("ex_apr");
    switch (exApr) {
      case "1":
        wp.daoTable = " crd_card a left join crd_card_tmp b on a.card_no = b.card_no ";
        break;
      case "2":
        wp.daoTable = " crd_card a left join crd_card_tmp_h b on a.card_no = b.card_no ";
        break;
    }
    wp.whereOrder = " order by b.apr_user,b.apr_date,b.card_no ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr);
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
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    String lsCol = "", dbProcessKind = "", dbSupFlag = "附卡";
    String lsSql = "", dbExpireChgDate = "", lsExpireReason = "", dbExpireReason = "",
        lsExpireReasonOld = "", dbExpireReasonOld = "", lsChangeReason = "", lsExpireChgFlag = "",
        dbExpireChgFlag = "";
    String lsExKind = wp.itemStr("ex_kind");
    wp.logSql = false;
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      // 計算欄位
      rowCt += 1;
      lsCol = wp.colStr(ii, "process_kind");
      // System.out.println("process_kind:"+ls_col);
      if (lsExKind.equals("1")) {
        // db_process_kind
        switch (lsCol) {
          case "0":
            dbProcessKind = "取消不續卡(放行前)";
            break;
          case "1":
            dbProcessKind = "預約不續卡";
            break;
          case "2":
            dbProcessKind = "取消不續卡(放行後)";
            break;
          case "3":
            dbProcessKind = "系統不續卡改續卡";
            break;
          case "4":
            dbProcessKind = "人工不續卡";
            break;
        }
        wp.colSet(ii, "db_process_kind", dbProcessKind);
        wp.colSet(ii, "db_expire_chg_flag", "");
        wp.colSet(ii, "db_expire_chg_date", wp.colStr(ii, "expire_chg_date"));
        // db_expire_reason
        lsExpireReason = wp.colStr(ii, "expire_reason");
        if (wp.colStr(ii, "expire_chg_flag").equals("1")) {
          lsSql = "select wf_desc from ptr_sys_idtab ";
          lsSql += " where wf_type = 'NOTCHG_KIND_O' and ";
          lsSql += "wf_id = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(lsSql);
          dbExpireReason = lsExpireReason + "-" + sqlStr("wf_desc");
          wp.colSet(ii, "db_expire_reason", dbExpireReason);
        }
        if (wp.colStr(ii, "expire_chg_flag").equals("4")) {
          lsSql = "select wf_desc from ptr_sys_idtab ";
          lsSql += " where wf_type = 'NOTCHG_KIND_M' and ";
          lsSql += "wf_id = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(lsSql);
          dbExpireReason = lsExpireReason + "-" + sqlStr("wf_desc");
          wp.colSet(ii, "db_expire_reason", dbExpireReason);
        }

        // db_expire_reason_old
        lsExpireReasonOld = wp.colStr(ii, "expire_reason_old");
        if (wp.colStr(ii, "expire_chg_flag_old").equals("1")) {
          lsSql = "select wf_desc from ptr_sys_idtab ";
          lsSql += " where wf_type = 'NOTCHG_KIND_O' and ";
          lsSql += "wf_id = :ls_expire_reason_old ";
          setString("ls_expire_reason_old", lsExpireReasonOld);
          sqlSelect(lsSql);
          dbExpireReasonOld = lsExpireReasonOld + "-" + sqlStr("wf_desc");
          wp.colSet(ii, "db_expire_reason_old", dbExpireReasonOld);
        }
        if (wp.colStr(ii, "expire_chg_flag_old").equals("4")) {
          lsSql = "select wf_desc from ptr_sys_idtab ";
          lsSql += " where wf_type = 'NOTCHG_KIND_M' and ";
          lsSql += "wf_id = :ls_expire_reason_old ";
          setString("ls_expire_reason_old", lsExpireReasonOld);
          sqlSelect(lsSql);
          dbExpireReasonOld = lsExpireReasonOld + "-" + sqlStr("wf_desc");
          wp.colSet(ii, "db_expire_reason_old", dbExpireReasonOld);
        }
      } else {
        // db_process_kind
        switch (lsCol) {
          case "0":
            dbProcessKind = "取消線上續卡(放行前)";
            break;
          case "1":
            dbProcessKind = "線上續卡";
            break;
          case "2":
            dbProcessKind = "取消線上續卡(放行後)";
            break;
          case "3":
            dbProcessKind = "系統續卡改不續卡";
            break;
        }
        wp.colSet(ii, "db_process_kind", dbProcessKind);

        // db_expire_change_flag
        lsChangeReason = wp.colStr(ii, "change_reason");
        // System.out.println("change_reason:"+ls_change_reason);
        switch (lsChangeReason) {
          case "1":
            dbExpireChgFlag = "系統續卡";
            break;
          case "2":
            dbExpireChgFlag = "提前續卡";
            break;
          case "3":
            dbExpireChgFlag = "人工續卡";
            break;
        }
        wp.colSet(ii, "db_expire_chg_flag", dbExpireChgFlag);
        // db_expire_chg_date
        wp.colSet(ii, "db_expire_chg_date", wp.colStr(ii, "change_date"));
      }
      if (wp.colStr(ii, "sup_flag").equals("0")) {
        dbSupFlag = "正卡";
      } else {
        dbSupFlag = "附卡";
      }
      wp.colSet(ii, "db_sup_flag", dbSupFlag);
    }
    wp.colSet("row_ct", intToStr(rowCt));

  }

  void subTitle() {
    String exKind = wp.itemStr("ex_kind");
    String exExpireFlag = wp.itemStr("ex_expire_flag");
    String exApr = wp.itemStr("ex_apr");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exChangeFlag = wp.itemStr("ex_change_flag");
    String exIdNo = wp.itemStr("ex_id_no");
    String exCardNo = wp.itemStr("ex_card_no");
    String title = "";
    title += "類別: ";
    if (exKind.equals("1")) {
      title += "不續卡";
      title += "  不續卡處理項目: ";
      switch (exExpireFlag) {
        case "0":
          title += "全部";
          break;
        case "1":
          title += "預約不續卡";
          break;
        case "2":
          title += "人工不續卡";
          break;
        case "3":
          title += "取消不續卡";
          break;
        case "4":
          title += "系統不續卡改續卡";
          break;
      }
    } else {
      title += "提前續卡";
      title += "  提前續卡處理項目: ";
      switch (exChangeFlag) {
        case "0":
          title += "全部";
          break;
        case "1":
          title += "線上續卡";
          break;
        case "2":
          title += "取消線上續卡";
          break;
        case "3":
          title += "系統續卡改不續卡";
          break;
      }
    }
    if (exApr.equals("1")) {
      title += "  放行前";
    } else {
      title += "  放行後";
      if (!empty(exDateS) || !empty(exDateE)) {
        title += "  放行日期: ";
        if (!empty(exDateS)) {
          title += exDateS + "起";
        }
        title += " -- ";
        if (!empty(exDateE)) {
          title += exDateE + "迄";
        }
      }
    }
    if (!empty(exIdNo)) {
      title += "  身分證字號: " + exIdNo;
    }
    if (!empty(exCardNo)) {
      title += "  卡號: " + exCardNo;
    }
    reportSubtitle = title;
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      String exKind = wp.itemStr("ex_kind");
      String exExpireFlag = wp.itemStr("ex_expire_flag");
      String exDateS = wp.itemStr("exDateS");
      String exDateE = wp.itemStr("exDateE");
      String exChangeFlag = wp.itemStr("ex_change_flag");
      String exApr = wp.itemStr("ex_apr");
      String exIdPSeqno = wp.itemStr("ex_id_p_seqno");
      String exCardNo = wp.itemStr("ex_card_no");
      String cond = "";
      switch (exKind) {
        case "1":
          cond = " 類別：不續卡  不續卡處理項目:" + exExpireFlag;
          switch (exApr) {
            case "1":
              cond += "放行前";
              break;
            case "2":
              cond += "放行後  放行日期:" + exDateS + " ~ " + exDateE;
              break;
          }
          break;
        case "2":
          cond = " 類別：提前續卡  提前續卡處理項目:" + exChangeFlag;
          switch (exApr) {
            case "1":
              cond += "放行前";
              break;
            case "2":
              cond += "放行後  放行日期:" + exDateS + " ~ " + exDateE;
              break;
          }
          break;
      }
      String cond1 = "卡人流水號 :" + exIdPSeqno + " 卡號: " + exCardNo;
      wp.colSet("cond_1", cond);
      wp.colSet("cond_2", cond1);
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
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
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
    } catch (Exception ex) {
    }
  }

}

