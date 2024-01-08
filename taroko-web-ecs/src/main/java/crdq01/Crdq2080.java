/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-02  V1.00.00  Andy Liu      program initial                         *
* 106-12-14            Andy		  update : program name : Crdi2080==>Crdq2080*
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      * 
 * 109-12-30  V1.00.02  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package crdq01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdq2080 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdq2080";

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
    String exApr = wp.itemStr("ex_apr");
    String exDate1 = wp.itemStr("ex_date1");
    String exDate2 = wp.itemStr("ex_date2");
    String exKind = wp.itemStr("ex_kind");
    String exExpireFlag = wp.itemStr("ex_expire_flag");
    String exChangeFlag = wp.itemStr("ex_change_flag");
    String exIdNo = wp.itemStr("ex_id_no");
    String exCardNo = wp.itemStr("ex_card_no");

    String lsWhere = "where 1=1  ";
    // 固定搜尋條件

    // user搜尋條件
    // ex_apr放行前後==>移至select tabel選項判斷

    // 放行日期
    if (exApr.equals("1")) {
      lsWhere += sqlStrend(exDate1, exDate2, "b.apr_date");
    }
    // 類別==>續卡或不續卡處理
    if (exKind.equals("1")) {
      lsWhere += "and b.kind_type = '080' ";
      if (empty(exExpireFlag) == false) {
        lsWhere += sqlCol(exExpireFlag, "b.process_kind");
      }
    }

    if (exKind.equals("2")) {
      lsWhere += "and b.kind_type = '120' ";
      if (empty(exChangeFlag) == false) {
        lsWhere += sqlCol(exChangeFlag, "b.process_kind");
      }
    }

    // 身分證號
    if (empty(exIdNo) == false) {
      lsWhere += sqlCol(exIdNo, "c.id_no");
    }

    // 卡號
    if (empty(exCardNo) == false) {
      lsWhere += sqlCol(exCardNo, "b.pp_card_no");
    }

    wp.whereStr = lsWhere;
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
    String exApr = wp.itemStr("ex_apr");
    wp.selectSQL = "" + "b.pp_card_no, " + "b.change_reason, " + "b.change_status, "
        + "b.change_date, " + "b.expire_reason, " + "b.expire_chg_flag, " + "b.expire_chg_date, "
        + "b.change_reason_old, " + "b.change_status_old, " + "b.change_date_old, "
        + "b.expire_reason_old, " + "b.expire_chg_flag_old, " + "b.expire_chg_date_old, "
        + "b.old_end_date, " + "b.cur_end_date, " + "b.cur_beg_date, " + "b.crt_user, "
        + "b.crt_date, " + "b.apr_user, " + "b.apr_date, " + "b.id_p_seqno, " + "a.group_code, "
        + "a.valid_to, " + "a.valid_fm, " + "c.id_no, " + "c.id_no_code, "
        + "(c.id_no ||'_'||c.id_no_code) db_id_no, " + "c.chi_name, "
        + "lpad(' ',20,' ') db_expire_chg_date, " + "lpad(' ',20,' ') db_expire_chg_flag, "
        + "lpad(' ',20,' ') db_expire_reason, " + "lpad(' ',20,' ') db_expire_reason_old, "
        + "b.mod_time, " + "b.kind_type, " + "b.process_kind, "
        + "lpad(' ',20,' ') db_process_kind ";
    // 放行前
    if (exApr.equals("0")) {
      wp.daoTable = " crd_card_pp a inner join crd_card_pp_tmp b on  a.pp_card_no = b.pp_card_no ";
    }
    // 放行後
    if (exApr.equals("1")) {
      wp.daoTable =
          " crd_card_pp a inner join crd_card_pp_tmp_h b on  a.pp_card_no = b.pp_card_no ";
    }
    wp.daoTable += " left join crd_idno c on  b.id_p_seqno = c.id_p_seqno ";
    wp.whereOrder = " order by b.apr_date, b.pp_card_no ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
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
    String wkKindType = "", wkExpireReason = "", wkExpireReasonOld = "", wkExpireChgFlag = "",
        wkExpireChgFlagOld = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_ct", "1");

      // db_process_kind處理狀態
      wkKindType = wp.colStr(ii, "kind_type");
      wkExpireChgFlagOld = wp.colStr(ii, "expire_chg_flag_old");
      String[] cde0 = new String[] {"1", "4", "5"};
      String[] txt0 = new String[] {"不續卡", "人工不續卡", "系統不續卡"};
      wp.colSet(ii, "expire_chg_flag_old", commString.decode(wkExpireChgFlagOld, cde0, txt0));
      if (wkKindType.equals("080")) {
        String processKind = wp.colStr(ii, "process_kind");
        String[] cde = new String[] {"0", "1", "4", "2", "3"};
        String[] txt = new String[] {"取消不續卡(放行前)", "預約不續卡", "人工不續卡", "取消不續卡(放行後)", "系統不續卡改續卡"};
        wp.colSet(ii, "db_process_kind", commString.decode(processKind, cde, txt));
        // db_expire_chg_date處理日期
        wp.colSet(ii, "db_expire_chg_date", wp.colStr(ii, "change_date"));

        // db_expire_reason不續卡原因
        wkExpireChgFlag = wp.colStr(ii, "expire_chg_flag");
        wkExpireReason = wp.colStr(ii, "expire_reason");
        if (wkExpireChgFlag.equals("1") & empty(wkExpireReason) == false) {
          lsSql = "select wf_desc from ptr_sys_idtab where 1=1 and wf_type = 'NOTCHG_KIND_O' ";
          lsSql += sqlCol(wkExpireReason, "wf_id");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            wkExpireReason = wkExpireReason + "_" + sqlStr("wf_desc");
          }
        }
        if (wkExpireChgFlag.equals("4") & empty(wkExpireReason) == false) {
          lsSql = "select wf_desc from ptr_sys_idtab where 1=1 and wf_type = 'NOTCHG_KIND_M' ";
          lsSql += sqlCol(wkExpireReason, "wf_id");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            wkExpireReason = wkExpireReason + "_" + sqlStr("wf_desc");
          }
        }
        wp.colSet(ii, "db_expire_reason", wkExpireReason);

        // db_expire_reason_old原不續卡原因

        wkExpireReasonOld = wp.colStr(ii, "expire_reason_old");
        if (wkExpireChgFlagOld.equals("1") & empty(wkExpireReasonOld) == false) {
          lsSql = "select wf_desc from ptr_sys_idtab where 1=1 and wf_type = 'NOTCHG_KIND_O' ";
          lsSql += sqlCol(wkExpireReasonOld, "wf_id");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            wkExpireReasonOld = wkExpireReasonOld + "_" + sqlStr("wf_desc");
          }
        }

        if (wkExpireChgFlagOld.equals("4") & empty(wkExpireReasonOld) == false) {
          lsSql = "select wf_desc from ptr_sys_idtab where 1=1 and wf_type = 'NOTCHG_KIND_M' ";
          lsSql += sqlCol(wkExpireReasonOld, "wf_id");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            wkExpireReasonOld = wkExpireReasonOld + "_" + sqlStr("wf_desc");
          }
        }
        wp.colSet(ii, "db_expire_reason_old", wkExpireReasonOld);

      }

      if (wkKindType.equals("120")) {
        // db_process_kind處理狀態
        String processKind = wp.colStr(ii, "process_kind");
        String[] cde = new String[] {"0", "1", "2", "3",};
        String[] txt = new String[] {"取消線上續卡(放行前)", "線上續卡", "取消線上續卡(放行後)", "系統續卡改不續卡"};
        wp.colSet(ii, "db_process_kind", commString.decode(processKind, cde, txt));

        // db_expire_chg_flag續卡註記
        String changeReason = wp.colStr(ii, "change_reason");
        String[] cde1 = new String[] {"1", "2", "3",};
        String[] txt1 = new String[] {"系統續卡", "提前續卡", "人工續卡"};
        wp.colSet(ii, "db_expire_chg_flag", commString.decode(changeReason, cde1, txt1));
        // db_expire_chg_date處理日期
        wp.colSet(ii, "db_expire_chg_date", wp.colStr(ii, "db_chg_date"));
      }


    }
    wp.colSet("row_ct", intToStr(rowCt));
  }

  void subTitle() {
    String exApr = wp.itemStr("ex_apr");
    String exDate1 = wp.itemStr("ex_date1");
    String exDate2 = wp.itemStr("ex_date2");
    String exKind = wp.itemStr("ex_kind");
    String exExpireFlag = wp.itemStr("ex_expire_flag");
    String exChangeFlag = wp.itemStr("ex_change_flag");
    String exIdNo = wp.itemStr("ex_id_no");
    String exCardNo = wp.itemStr("ex_card_no");

    String title = "列表條件 : ";
    // ex_apr放行前後
    title += " 放行前後 : ";
    if (exApr.equals("0")) {
      title += " 放行前 ";
    } else {
      title += " 放行後 ";
      // ex_date放行日期
      if (empty(exDate1) == false || empty(exDate2) == false) {
        title += "  放行日期 : ";
        if (empty(exDate1) == false) {
          title += exDate1 + " 起 ";
        }
        if (empty(exDate2) == false) {
          title += " ~ " + exDate2 + " 迄 ";
        }
      }
    }

    // ex_kind類別
    title += " 類別 : ";
    if (exKind.equals("1")) {
      title += " 不續卡 ";
      // ex_expire_flag不續卡處理項目
      if (empty(exExpireFlag) == false) {
        String expireFlag = exExpireFlag;
        String[] cde1 = new String[] {"1", "4", "2", "3"};
        String[] txt1 = new String[] {"預約不續卡", "人工不續卡", "取消不續卡(放行後)", "系統不續卡改續卡"};
        title += " 不續卡處理項目 : " + commString.decode(expireFlag, cde1, txt1);
      }
    } else {
      title += " 提前續卡 ";
      // ex_change_flag提前續卡處理項目
      if (empty(exChangeFlag) == false) {
        String exChangeFlag2 = exChangeFlag;
        String[] cde2 = new String[] {"1", "2", "3",};
        String[] txt2 = new String[] {"線上續卡", "取消線上續卡(放行後)", "系統續卡改不續卡"};
        title += " 提前續卡處理項目 : " + commString.decode(exChangeFlag2, cde2, txt2);
      }
    }

    // ex_id_no身分證號
    if (empty(exIdNo) == false) {
      title += " 身分證號 : " + exIdNo;
    }

    // ex_card_no卡號
    if (empty(exCardNo) == false) {
      title += " 卡號 : " + exCardNo;
    }

    reportSubtitle = title;
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
      // dddw_trans_type
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_trans_type");
      // dddw_list("dddw_trans_type", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and wf_type =
      // 'IBM_STOPTYPE' order by wf_id");

      // dddw_rtn_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_rtn_code");
      // dddw_list("dddw_rtn_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and wf_type =
      // 'IBM_RTNCODE' and wf_id <> '00' and wf_id <>'99' order by wf_id");
    } catch (Exception ex) {
    }
  }

}

