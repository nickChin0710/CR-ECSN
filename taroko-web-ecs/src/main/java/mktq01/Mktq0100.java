/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-26  V1.00.00  Andy Liu   program initial                            *
* 107-06-29  V1.00.01  Andy       Update UI
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *                                         *
* 110-07-22  V1.00.05  YangBo       修改查詢變量和方法                
* 110-11-24  V1.00.05   machao      bug修改          *                                         *
* 112-01-12  V1.00.07   Zuwei Su   db_current_code值設定        *                                         *
******************************************************************************/
package mktq01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Mktq0100 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "mktq0100";

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
    String exSaleno = wp.itemStr("ex_saleno");
    String exIntrid = wp.itemStr("ex_intrid");
    String exIsudate1 = wp.itemStr("ex_isudate1");
    String exIsudate2 = wp.itemStr("ex_isudate2");
    String exRewardFlag = wp.itemStr("ex_reward_flag");
    String exCurrentCode = wp.itemStr("ex_current_code");
    String exChannelCode = wp.itemStr("ex_channel_code");
    String exRegBankNo = wp.itemStr("ex_reg_bank_no");
    String exCardType = wp.itemStr("ex_card_type");
    String exGroupCode = wp.itemStr("ex_group_code");
//    String lsSql = "", exSaleno1 = "", exSaleno2 = "";
//    //
//    if (!empty(exSaleno)) {
//      lsSql = "select employ_no,"
//          + "lpad(decode(old_employ,'','X',old_employ) ,6,'000000' ) as old_employ "
//          + "from crd_employee " + "where employ_no in (:ls_val) " + "or old_employ in (:ls_val) ";
//      setString("ls_val", exSaleno);
//      sqlSelect(lsSql);
//      if (sqlRowNum > 0) {
//        exSaleno1 = sqlStr("employ_no");
//        exSaleno2 = sqlStr("old_employ");
//      }
//    }
    String lsShere = "where 1=1 ";
    // 固定搜尋條件

    // user搜尋條件
    if (empty(exSaleno) && empty(exIntrid) && empty(exChannelCode)) {
      alertErr2("介紹人代號、ID或通路代號請至少擇一輸入 !!");
      return false;
    }
    if (!empty(exSaleno) && !empty(exIntrid)) {
      alertErr2("介紹人代號或ID不可全部輸入 !!");
      return false;
    }
    lsShere += sqlStrend(exIsudate1 + "01", exIsudate2 + "31", "a.issue_date");
//    if (!empty(exSaleno)) {
//      lsShere += "and (promote_emp_no =:ex_saleno1 or ";
//      lsShere += "promote_emp_no =:ex_saleno or ";
//      lsShere += "promote_emp_no =:ex_saleno2 ) ";
//      setString("ex_saleno1", exSaleno1);
//      setString("ex_saleno", exSaleno);
//      setString("ex_saleno2", exSaleno2);
//    }
    lsShere += sqlCol(exSaleno, "b.employ_no");
    lsShere += sqlCol(exIntrid, "a.introduce_id");
    lsShere += sqlCol(wp.itemStr("ex_card_type"), "a.card_type", "like%");
    lsShere += sqlCol(wp.itemStr("ex_group_code"), "a.group_code", "like%");
    lsShere += sqlCol(wp.itemStr("ex_reg_bank_no"), "a.reg_bank_no", "like%");

    switch (exRewardFlag) {
      case "Y":
        lsShere += "and reward_amt > 0";
        break;
      case "N":
        lsShere += "and reward_amt = 0";
        break;
      default:
        break;
    }

    lsShere += sqlCol(exCurrentCode, "a.current_code");
    lsShere += sqlCol(exChannelCode, "a.channel_code");

    wp.whereStr = lsShere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    // if (getWhereStr() == false)
    //  return;
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

        wp.selectSQL = ""
                + "a.card_no, "
                + "(a.card_type||'-'||c.name) as card_type, "
                + "(a.group_code||'-'||d.group_name) as group_code, "
                + "uf_idno_name(a.id_p_seqno) as chi_name, "
                + "decode(a.sup_flag,'0','0:正卡','1:附卡') as sup_flag, "
                + "a.major_card_no, "
                + "a.current_code, "
                + "a.new_end_date, "
                + "a.issue_date, "
                + "e.staff_flag, "
                + "b.id as employ_id, "
                + "a.reg_bank_no, "
                + "f.brief_chi_name, "
                + "(SELECT min(last_purchase_date) FROM mkt_post_consume m WHERE m.CARD_NO = a.CARD_NO) as min_purchase_date, "
                + "(SELECT max(last_purchase_date) FROM mkt_post_consume m WHERE m.CARD_NO = a.CARD_NO) as max_purchase_date, "
                + "((g.consume_ao_amt+g.consume_bl_amt+g.consume_ca_amt+g.consume_id_amt+g.consume_it_amt+g.consume_ot_amt) - "
                + "(g.refund_it_amt+g.sub_ao_amt+g.sub_bl_amt+g.sub_ca_amt+g.sub_id_amt+g.sub_ot_amt+g.sub_it_amt)) as debit_amount, "
                + "decode (a.reward_amt,0,'N','Y') reward_flag, "
                + "b.employ_no, "
                + "b.old_employ, "
                + "a.channel_code";
        wp.daoTable = " crd_card" + " a ";
    wp.daoTable += "inner join crd_employee" + " b " + "on a.introduce_id = b.id ";
    wp.daoTable += "inner join ptr_card_type" + " c " + "on a.card_type = c.card_type ";
    wp.daoTable += "inner join ptr_group_code" + " d " + "on a.group_code = d.group_code ";
    wp.daoTable += "left join crd_idno" + " e " + "on a.id_p_seqno  = e.id_p_seqno  ";
    wp.daoTable += "left join gen_brn" + " f " + "on a.reg_bank_no  = f.branch  ";
    wp.daoTable += "left join mkt_post_consume" + " g " + "on a.card_no  = g.card_no  ";
    wp.whereOrder = "order by a.new_end_date, a.card_no ";

//   String iii = "select " + wp.selectSQL + " from " + wp.daoTable+wp.whereStr+wp.whereOrder ;

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
    String exSaleno = wp.itemStr("ex_saleno");
    String exIntrid = wp.itemStr("ex_intrid");
    String lsSql = "", dbChiName = "", dbUnitNo = "", dbUnitName = "";
    // 根據輸入介紹人代號或介紹人ID做不同處理
    if (!empty(exIntrid)) {
      lsSql = "select chi_name, unit_no, unit_name from crd_employee where 1=1 ";
      lsSql += sqlCol(exIntrid, "id");
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        dbChiName = sqlStr("chi_name");
        dbUnitNo = sqlStr("unit_no");
        dbUnitName = sqlStr("unit_name");
      }
      if (empty(dbChiName)) {
        lsSql = "select introduce_name from crd_card where 1=1 ";
        lsSql += sqlCol(exIntrid, "introduce_id");
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          dbChiName = sqlStr("introduce_name");
        }
      }
      if (empty(dbChiName)) {
        lsSql = "select chi_name from crd_idno where 1=1 ";
        lsSql += sqlCol(exIntrid, "id_no");
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          dbChiName = sqlStr("chi_name");
        }
      }
      if (empty(dbChiName)) {
        lsSql = "select name from mkt_sale where 1=1 ";
        lsSql += sqlCol(exIntrid, "sale_id");
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          dbChiName = sqlStr("chi_name");
        }
      }
    } else {
      if (!empty(exSaleno)) {
        lsSql = "select chi_name, unit_no, unit_name from crd_employee where 1=1 ";
        lsSql += sqlCol(exSaleno, "employ_no");
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          dbChiName = sqlStr("chi_name");
          dbUnitNo = sqlStr("unit_no");
          dbUnitName = sqlStr("unit_name");
        }
        if (empty(dbChiName)) {
          lsSql = "select name from mkt_sale where 1=1 ";
          lsSql += sqlCol(exSaleno, "sale_no");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            dbChiName = sqlStr("chi_name");
          }
        }
      }
    }
    wp.colSet("ex_intrname", dbChiName);
    wp.colSet("ex_unitno", dbUnitNo);
    wp.colSet("ex_unitname", dbUnitName);

    //
//    String wkEmpNo = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String currentCode = wp.colStr(ii, "current_code");
      String[] cde = new String[] {"0", "1", "2", "3", "4", "5"};
      String[] txt = new String[] {"0:正常", "1:申停", "2:掛失", "3:強停", "4:其他", "5:偽卡"};
      wp.colSet(ii, "db_current_code", commString.decode(currentCode, cde, txt));
//      // 計算欄位
//      rowCt += 1;
//      wp.colSet(ii, "group_ct", "1");
//      //
//      wkEmpNo = wp.colStr(ii, "promote_emp_no");
//      lsSql = "SELECT decode(old_employ,'','X',old_employ) as old_employ " + "FROM crd_employee "
//          + "WHERE employ_no  = :ls_val  " + "or old_employ = :ls_val ";
//      setString("ls_val", wkEmpNo);
//      sqlSelect(lsSql);
//      if (sqlStr("old_employ").equals(wkEmpNo)) {
//        wp.colSet(ii, "db_cno", "Y");
//      }
    }
    wp.colSet("row_ct", intToStr(rowCt));
  }

  void subTitle() {}

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
      // dddw_bank_no
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_bank_no");
      // dddw_list("dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
      // "where 1=1 ");

      // dddw_apply_source
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_apply_source");
//       dddw_list("dddw_apply_source", "dbc_apply_source",
      // "apply_source", "apply_source_name", "where 1=1 ");
      if ((wp.respHtml.equals("mktq0100"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        // 受理分行
        if (wp.colStr("ex_reg_bank_no").length() > 0) {
          wp.optionKey = wp.colStr("ex_reg_bank_no");
        }
        this.dddwList("dddw_reg_bank_no", "gen_brn", "trim(branch)", "trim(brief_chi_name)",
                " where 1 = 1 ");
        // 卡種
        if (wp.colStr("ex_card_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_card_type");
        }
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
                " where 1 = 1 ");
        // 團體代號
        if (wp.colStr("ex_group_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_group_code");
        }
        this.dddwList("dddw_group_code", "ptr_group_code", "trim(group_code)", "trim(group_name)",
                " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

}
