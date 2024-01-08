/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-19  V1.00.01  andy       program initial                            *
* 109-04-27  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package mktp01;


import ofcapp.BaseEdit;
import taroko.com.TarokoExcel;
import taroko.com.TarokoCommon;

public class Mktp0020 extends BaseEdit {
  int i = 0, iiUnit = 0;
  String kk1Batchno = "";
  String mProgName = "mktp0020";
  String usSql = "";
  int selectCt = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "R1")) {
      // -資料讀取-
      strAction = "R1";
      dataRead1();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
    }

    dddwSelect();
    initButton();
  }

  void getWhereStr() {

    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_batchno")) == false) {
      wp.whereStr += " and  batch_no = :ex_batchno ";
      setString("ex_batchno", wp.itemStr("ex_batchno"));
    }
    if (empty(wp.itemStr("ex_crtdate1")) == false) {
      wp.whereStr += " and  file_date >= :ex_crtdate1 ";
      setString("ex_crtdate1", wp.itemStr("ex_crtdate1"));
    }
    if (empty(wp.itemStr("ex_crtdate2")) == false) {
      wp.whereStr += " and  file_date <= :ex_crtdate2 ";
      setString("ex_crtdate2", wp.itemStr("ex_crtdate2"));
    }
  }

  @Override
  public void queryFunc() throws Exception {

    String lsDate1 = wp.itemStr("ex_crtdate1");
    String lsDate2 = wp.itemStr("ex_crtdate2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[建檔日期-起迄]  輸入錯誤");
      return;
    }
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " hex(rowid) as rowid " + " ,batch_no " + " ,description " + " ,confirm_parm "
        + " ,file_date " + " ,employee_no " + " ,confirm_date ";

    wp.daoTable = " mkt_month_par ";
    wp.whereOrder = " ";
    getWhereStr();

    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    // dddw_select();

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    kk1Batchno = itemKk("data_k1");
    if (empty(kk1Batchno)) {
      kk1Batchno = wp.itemStr("kk_batchno");
    }

    String lsSql = "select count(*) ct from mkt_list " + "where batch_no =:batch_no ";
    setString("batch_no", kk1Batchno);
    sqlSelect(lsSql);
    if (sqlNum("ct") == 0) {
      alertErr("mkt_list資料檔查無資料!!");
      return;
    } else {
      wp.colSet("row_ct", sqlStr("ct"));
    }
    wp.colSet("ex_sex", "0");
    wp.colSet("ex_age_limit", "0");
    wp.colSet("ex_zip_code", "0");
    wp.colSet("ex_card_rank", "0");
    wp.colSet("kk_batchno", kk1Batchno);

  }

  public void dataRead1() throws Exception {
    kk1Batchno = wp.itemStr("kk_batchno");
    String excludeList = "";
    String exIdIndex = wp.itemStr("ex_id_index");
    StringBuilder exSendNumber = new StringBuilder();
    if (exIdIndex.equals("Y")) {
      wp.sqlCmd += "select y.* " + "from mkt_list y, " + "(select distinct a.id_no, "
          + "a.batch_no, " + "a.group_type, " + "a.type_code, " + "a.seq_no " + "from mkt_list a "
          + "where a.card_rank = (select min (b.card_rank) " + "from mkt_list b "
          + "where a.id_no = b.id_no ";
      wp.sqlCmd += sqlCol(kk1Batchno, "b.batch_no");
      wp.sqlCmd += ") ";
      wp.sqlCmd += "and a.seq_no = (select max (b.seq_no) " + "from mkt_list b "
          + "where a.id_no = b.id_no ";
      wp.sqlCmd += sqlCol(kk1Batchno, "b.batch_no");
      wp.sqlCmd += ")";
      wp.sqlCmd += sqlCol(kk1Batchno, "a.batch_no");
      wp.sqlCmd += ") z " + "where 1 = 1 " + "and y.batch_no = z.batch_no "
          + "and y.group_type = z.group_type " + "and y.type_code = z.type_code "
          + "and y.seq_no = z.seq_no";
    } else {
      wp.sqlCmd += "select y.* " + "from mkt_list y " + "where 1=1 ";
      wp.sqlCmd += sqlCol(kk1Batchno, "y.batch_no");
    }

    // 性別
    if (!wp.itemStr("ex_sex").equals("0")) {
      wp.sqlCmd += sqlCol(wp.itemStr("ex_sex"), "y.sex");
    }

    // 年齡限制
    if (!wp.itemStr("ex_age_limit").equals("0")) {
      if (empty(wp.itemStr("ex_age1")) & empty(wp.itemStr("ex_age2"))) {
        alertErr("請輸入制年齡!!");
        return;
      } else {
        wp.sqlCmd += sqlStrend(wp.itemStr("ex_age1"), wp.itemStr("ex_age2"), "y.age");
      }
    }

    // 金普卡註記
    if (!wp.itemStr("ex_card_rank").equals("0")) {
      // wp.sqlCmd +=" and card_rank = :card_rank ";
      // setString("card_rank",wp.item_ss("ex_card_rank"));
      wp.sqlCmd += sqlCol(wp.itemStr("ex_card_rank"), "y.card_rank");
    }

    // 郵遞區號指定/排除
    if (!wp.itemStr("ex_zip_code").equals("0")) {
      if (empty(wp.itemStr("exclude_list_desc"))) {
        alertErr("指定/限制郵遞區為空白!!");
        return;
      }
      if (wp.itemStr("ex_zip_code").equals("1")) {
        excludeList = addCommaByEachTwoBytes3(wp.colStr("exclude_list"));
        wp.sqlCmd += excludeList;
      }
      if (wp.itemStr("ex_zip_code").equals("2")) {
        excludeList = addCommaByEachTwoBytes3(wp.colStr("exclude_list"));
        wp.sqlCmd += excludeList;
      }
    }
    // 信用額度
    if (!empty(wp.itemStr("ex_credit_amt"))) {
      wp.sqlCmd += sqlStrend(wp.itemStr("ex_credit_amt"), "", "line_of_credit_amt");
    }
    // wp.sqlCmd += " order by seq_no ";
    // 寄送人(篩選筆數)==>在SQL條件最後
    if (!empty(wp.itemStr("ex_send_number"))) {
      exSendNumber.append(" fetch first ");
      exSendNumber.append(wp.itemStr("ex_send_number"));
      exSendNumber.append(" rows only ");
      wp.sqlCmd += exSendNumber.toString();
    }
    usSql = wp.sqlCmd;
    wp.colSet("us_sql", usSql);
    // System.out.println(wp.sqlCmd);
    wp.sqlCmd = "select count(*) ct from (" + wp.sqlCmd;
    wp.sqlCmd += " ) ";
    // System.out.println(wp.sqlCmd);
    sqlSelect(wp.sqlCmd);
    wp.colSet("select_ct", sqlStr("ct"));
    String lsSql = "select count(*) ct from mkt_list " + "where batch_no =:batch_no ";
    setString("batch_no", kk1Batchno);
    sqlSelect(lsSql);
    wp.colSet("row_ct", sqlStr("ct"));
  }

  void list_wkdata() {
    // int row_ct = 0;
    // for (int ii = 0; ii < wp.selectCnt; ii++) {
    // row_ct += 1;
    // wp.col_set(ii,"no",int_2Str(row_ct));
    // }
    // wp.col_set("row_ct", int_2Str(row_ct));
  }

  @Override
  public void saveFunc() throws Exception {
    kk1Batchno = wp.itemStr("kk_batchno");
    usSql = wp.itemStr("us_sql");
    if (empty(usSql)) {
      usSql = "select * from mkt_list " + "where 1=1 ";
      usSql += sqlCol(kk1Batchno, "batch_no");
    }
    wp.sqlCmd = "update ( ";
    wp.sqlCmd += usSql;
    wp.sqlCmd += ") a set a.file_date = to_char(sysdate, 'yyyymmdd')";
    // System.out.println(wp.sqlCmd);
    sqlExec(wp.sqlCmd);
    if (sqlRowNum > 0) {
      sqlCommit(1);
    } else {
      sqlCommit(0);
    }

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {

    try {
      // wp.optionKey = wp.col_ss("acct_type_1");
      // this.dddw_list("dddw_acct_type_1", "ptr_acct_type", "acct_type", "chin_name", "where 1=1
      // order by acct_type");

    } catch (Exception ex) {
    }
  }

  private String addCommaByEachTwoBytes3(String data) {
    StringBuilder zipCode = new StringBuilder();
    String buf = "";
    if (wp.itemStr("ex_zip_code").equals("1")) {
      zipCode.append("and zip_code in ");
    }
    if (wp.itemStr("ex_zip_code").equals("2")) {
      zipCode.append("and zip_code not in ");
    }
    for (int i = 0; i < data.length(); i++) {
      buf = data.substring(i, i + 1);
      if (i == 0) {
        zipCode.append("('");
        zipCode.append(buf);
      }
      if (i == data.length() - 1) {
        zipCode.append(buf);
        zipCode.append("')");
      }
      if (i > 0 & i != data.length() - 1) {
        if (!buf.equals(",")) {
          zipCode.append(buf);
        }
        if (buf.equals(",")) {
          zipCode.append("','");
        }
      }
    }

    // System.out.println(zip_code.toString());
    return zipCode.toString();
  }

  void xlsPrint() {
    try {
      saveFunc();
      wp.pageRows = 99999;
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      // subTitle();
      // wp.col_set("cond_1", report_subtitle);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      // queryFunc();
      // dataRead1();
      usSql = wp.itemStr("us_sql");
      if (empty(usSql)) {
        usSql = "select * from mkt_list " + "where 1=1 ";
        usSql += sqlCol(kk1Batchno, "batch_no");
      }
      wp.sqlCmd = usSql;
      // System.out.println("excel :"+wp.sqlCmd);
      pageQuery();
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
}
