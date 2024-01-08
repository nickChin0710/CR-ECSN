/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-13  V1.00.00  Andy Liu   program initial                            *
* 106-12-14            Andy		       update : ucStr==>zzstr                     *	
* 109-04-23  V1.00.03  yanghan  修改了變量名稱和方法名稱*
* 109-12-25   V1.00.04  Justin      parameterize sql 
******************************************************************************/
package dbcr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Dbcr0085 extends BaseReport {

  InputStream inExcelFile = null;
  String progName = "dbcr0085";

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
    String apr = wp.itemStr("ex_apr");
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");
    String kind = wp.itemStr("ex_kind");
    String expireFlag = wp.itemStr("ex_expire_flag");
    String changeFlag = wp.itemStr("ex_change_flag");
    String exIdNo = wp.itemStr("ex_id_no");
    String exCardNo = wp.itemStr("ex_card_no");

    String lswhere = "where 1=1  ";

    // 放行前後決定資料table寫在queryRead()

    // 放行前後==>放行日期
    if (apr.equals("1")) {
      lswhere += sqlStrend(date1, date2, "a.apr_date");
    }

    // ex_kind類別: 1==>ex_expire_flag不續卡處理項目 2==>ex_change_flag續卡處理項目
    switch (kind) {
      case "1":
        lswhere += " and a.kind_type = '080' ";
        lswhere += sqlCol(expireFlag, "a.process_kind");
        break;
      case "2":
        lswhere += " and a.kind_type = '120' ";
        lswhere += sqlCol(changeFlag, "a.process_kind");
        break;
    }

    // ex_id_no身分證字號
    if (empty(exIdNo) == false) {
      lswhere += sqlCol(exIdNo, "c.id_no");
    }

    // ex_card_no卡號
    if (empty(exCardNo) == false) {
      lswhere += sqlCol(exCardNo, "a.card_no");
    }


    wp.whereStr = lswhere;
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

    wp.selectSQL = "" + "a.apr_date, " + "(c.id_no ||'-'||c.id_no_code) wk_apply_id, "
        + "c.chi_name, " + "decode(b.sup_flag,'0','正卡','附卡') db_sup_flag, " + "a.card_no, "
        + "a.corp_no, " + "a.id_p_seqno, " + "b.group_code, " + "b.new_end_date, "
        + "lpad(' ',20,' ') db_process_kind, " + "lpad(' ',20,' ') db_expire_chg_flag, "
        + "lpad(' ',20,' ') db_expire_reason, " + "a.change_date_old, " + "a.change_reason_old, "
        + "a.expire_chg_date_old, " + "a.expire_chg_flag_old, "
        + "lpad(' ',20,' ') db_expire_reason_old, " + "a.crt_user, "
        + "lpad(' ',20,' ') db_expire_chg_date, " + "a.change_reason, " + "a.change_status, "
        + "a.change_date, " + "a.expire_reason, " + "a.expire_chg_flag, " + "a.expire_chg_date, "
        + "a.change_status_old, " + "a.expire_reason_old, " + "a.old_end_date, "
        + "a.cur_end_date, " + "a.cur_beg_date, " + "a.crt_date, " + "a.apr_user, "
        + "b.group_code, " + "b.new_beg_date, " + "a.mod_time, " + "a.process_kind   ";

    // 放行前/後 table不同
    String ex_apr = wp.itemStr("ex_apr");
    switch (ex_apr) {
      case "0":
        wp.daoTable = " dbc_card_tmp a left join dbc_card b on a.card_no = b.card_no "
            + "                left join dbc_idno c on a.id_p_seqno = c.id_p_seqno ";
        break;
      case "1":
        wp.daoTable = " dbc_card_tmp_h a left join dbc_card b on a.card_no = b.card_no "
            + "                  left join dbc_idno c on a.id_p_seqno = c.id_p_seqno ";
        break;
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
    String lsCol = "", dbProcessKind = "";
    String lsSql = "", dbExpireChgDate = "", lsExpireReason = "", dbExpireReason = "",
        lsExpireReasonOld = "", dbExpireReasonOld = "", lsChangeReason = "",
        lsExpireChgFlag = "", dbExpireChgFlag = "";
    String lsExKind = wp.itemStr("ex_kind");
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      row += 1;
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

        // db_expire_reason
        lsExpireReason = wp.colStr(ii, "expire_reason");
        if (wp.colStr(ii, "expire_chg_flag").equals("1")) {
          lsSql = "select wf_desc from ptr_sys_idtab ";
          lsSql += " where wf_type = 'NOTCHG_VD_O' and ";
          lsSql += "wf_id = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(lsSql);
          dbExpireReason = lsExpireReason + "-" + sqlStr("wf_desc");
        }
        if (wp.colStr(ii, "expire_chg_flag").equals("4")) {
          lsSql = "select wf_desc from ptr_sys_idtab ";
          lsSql += " where wf_type = 'NOTCHG_VD_M' and ";
          lsSql += "wf_id = :ls_expire_reason ";
          setString("ls_expire_reason", lsExpireReason);
          sqlSelect(lsSql);
          dbExpireReason = lsExpireReason + "-" + sqlStr("wf_desc");
        }

        // db_expire_reason_old
        lsExpireReasonOld = wp.colStr(ii, "expire_reason_old");
        if (wp.colStr(ii, "expire_chg_flag_old").equals("1")) {
          lsSql = "select wf_desc from ptr_sys_idtab ";
          lsSql += " where wf_type = 'NOTCHG_VD_O' and ";
          lsSql += "wf_id = :ls_expire_reason_old ";
          setString("ls_expire_reason_old", lsExpireReasonOld);
          sqlSelect(lsSql);
          dbExpireReasonOld = lsExpireReasonOld + "-" + sqlStr("wf_desc");
        }
        if (wp.colStr(ii, "expire_chg_flag_old").equals("4")) {
          lsSql = "select wf_desc from ptr_sys_idtab ";
          lsSql += " where wf_type = 'NOTCHG_VD_M' and ";
          lsSql += "wf_id = :ls_expire_reason_old ";
          setString("ls_expire_reason_old", lsExpireReasonOld);
          sqlSelect(lsSql);
          dbExpireReasonOld = lsExpireReasonOld + "-" + sqlStr("wf_desc");
        }

        // db_expire_change_flag
        lsExpireChgFlag = wp.colStr(ii, "expire_chg_flag");
        switch (lsExpireChgFlag) {
          case "1":
            dbExpireChgFlag = "預約不續卡";
            break;
          case "4":
            dbExpireChgFlag = "人工不續卡";
            break;
          case "2":
            dbExpireChgFlag = "取消不續卡";
            break;
          case "3":
            dbExpireChgFlag = "不續卡改續卡";
            break;
        }

        // db_expire_chg_date
        dbExpireChgDate = wp.colStr(ii, "expire_chg_date");

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

        // db_expire_chg_date
        dbExpireChgDate = wp.colStr(ii, "chg_date");
      }
      // System.out.println("db_process_kind:"+db_process_kind);
      wp.colSet(ii, "db_process_kind", dbProcessKind);
      wp.colSet(ii, "db_expire_chg_date", dbExpireChgDate);
      wp.colSet(ii, "db_expire_reason", dbExpireReason);
      wp.colSet(ii, "db_expire_chg_flag", dbExpireChgFlag);
      wp.colSet(ii, "db_expire_reason_old", dbExpireReasonOld);
    }
    wp.colSet("row_ct", intToStr(row));
  }

  void subTitle() {
    String apr = wp.itemStr("ex_apr");
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");
    String kind = wp.itemStr("ex_kind");
    String exExpireFlag = wp.itemStr("ex_expire_flag");
    String exChangeFlag = wp.itemStr("ex_change_flag");
    String exIdNo = wp.itemStr("ex_id_no");
    String exCardNo = wp.itemStr("ex_card_no");

    String all = "";
    // ex_apr放行前後
    String[] cde = new String[] {"0", "1"};
    String[] txt = new String[] {"放行前", "放行後"};
    all += " 放行前後 : " + commString.decode(apr, cde, txt);

    // ex_date放行日期
    if (empty(date1) == false || empty(date2) == false) {
      all += " 放行日期 : ";
      if (empty(date1) == false) {
        all += date1 + " 起 ";
      }
      if (empty(date2) == false) {
        all += " ~ " + date2 + " 迄 ";
      }
    }

    // ex_kind類別
    String[] cde1 = new String[] {"1", "2"};
    String[] txt1 = new String[] {"不續卡", "續卡"};
    all += " 類別 : " + commString.decode(kind, cde1, txt1);

    // ex_expire_flag不續卡處理項目
    if (empty(exExpireFlag) == false) {
      String[] cde2 = new String[] {"1", "4", "2", "3"};
      String[] txt2 = new String[] {"預約不續卡", "人工不續卡", "取消不續卡", "系統不續卡改續卡"};
      all += " 不續卡處理項目 : " + commString.decode(exExpireFlag, cde2, txt2);
    }

    // ex_change_flag提前續卡處理項目
    if (empty(exChangeFlag) == false) {
      String[] cde3 = new String[] {"1", "2", "3"};
      String[] txt3 = new String[] {"線上續卡", "取消線上續卡", "系統續卡改不續卡"};
      all += " 提前續卡處理項目 : " + commString.decode(exChangeFlag, cde3, txt3);
    }

    // ex_id_no身分證字號
    if (empty(exIdNo) == false) {
      all += " 身分證字號 : " + exIdNo;
    }

    // ex_card_no卡號
    if (empty(exCardNo) == false) {
      all += " 卡號 : " + exCardNo;
    }

    reportSubtitle = all;
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

    } catch (Exception ex) {
    }
  }

}

