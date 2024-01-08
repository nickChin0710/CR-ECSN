/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-11  V1.00.00  Andy Liu   program initial                            *
* 107-06-08  V1.00.01  Andy       Update lable report                        *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *	
* 109-05-27  V1.00.03  JustinWu    prevent from dropping a not existing table
* 110-12-09  V1.00.04  Ryan       fix a sql injection bug                    *
******************************************************************************/
package mktr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Mktr4110 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "mktr4110";
  String gsSql = "";
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
    String exTypeCode = wp.itemStr("ex_type_code");
    String exDbMontype = wp.itemStr("ex_db_montype");
    String exDbPrize1 = wp.itemStr("ex_db_prize1");
    String exDbPrize2 = wp.itemStr("ex_db_prize2");
    String exDbPrizeDate = wp.itemStr("ex_db_prize_date");
    String exBatchNo = wp.itemStr("ex_batch_no");
    String exDbDesc = wp.itemStr("ex_db_desc");

    String lsWhere = "where 1=1  " + "and file_date != '' ";
    // 固定搜尋條件

    // user搜尋條件
   // lsWhere += sqlCol(exTypeCode, "type_code");
    lsWhere += " and type_code = :ex_type_code";
    
    //lsWhere += sqlCol(exBatchNo, "batch_no");
    lsWhere += " and batch_no = :ex_batch_no";
    
    setString("ex_type_code",exTypeCode);
    setString("ex_batch_no",exBatchNo);
    
    switch (exTypeCode) {
      case "2":
        if (!exDbMontype.equals("0")) {
          lsWhere += sqlCol(exDbMontype, "group_type");
        }
        break;
      case "3":
        lsWhere += sqlStrend(exDbPrize1, exDbPrize2, "group_type");
        break;
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

    wp.selectSQL = "" + "group_type, " + "seq_no, " + "card_no, " + "name, " + "sex, "
        + "zip_code, " + "address_1, " + "address_2, " + "address_3, " + "address_4, "
        + "(address_1 || address_2 || address_3 || address_4) wk_addr1, " + "address_5, "
        + "id_no, " + "batch_no, " + "type_code, " + "' ' db_description ";
    wp.daoTable = " mkt_list ";
    wp.whereOrder += "ORDER BY batch_no";

    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable + wp.whereStr +
    // wp.whereOrder);

    // 頁面重新計算筆數
    // wp.pageCount_sql ="select count(*) from (select " + wp.selectSQL + "
    // from " +wp.daoTable+wp.whereStr+wp.whereOrder+")";

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
    String wkTypeCode = "", wkBatchno = "", wkGroupType = "", wkId = "";
    String isSql = "", lsSql = "";
    String crTempTable = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      // wk_batchno
      wkTypeCode = wp.colStr(ii, "type_code");
      wkBatchno = wp.colStr(ii, "batch_no");
      wkGroupType = wp.colStr(ii, "group_type");
      if (wkTypeCode.equals("1") | empty(wkGroupType)) {
      } else {
        wkBatchno = wkBatchno + " - " + wkGroupType;
      }
      wp.colSet(ii, "wk_batchno", wkBatchno);
      // wk_id
      wkId = strMid(wp.colStr(ii, "id_no"), 0, 7);
      wp.colSet(ii, "wk_id", wkId);

      // sex
      if (wp.colStr(ii, "sex").equals("1")) {
        wp.colSet(ii, "sex", "先生");
      } else {
        wp.colSet(ii, "sex", "小姐");
      }
    }
    wp.colSet("row_ct", intToStr(rowCt));
    // 刪除暫存報表table
    crTempTable = "drop table if exists session.mkt_list_temp_test";
    sqlExec(crTempTable);
    // 定義暫存報表table
    crTempTable = "declare global temporary table mkt_list_temp_test " + "("
        + "group_type1 varchar(2), card_no1 varchar(19),name1 varchar(50),db_sex1 varchar(18),zip_code1 varchar(5),wk_addr1 varchar(44),address_51 varchar(60),db_id_no1 varchar(7),db_batch_no1 varchar(12),type_code1 varchar(1), "
        + "group_type2 varchar(2), card_no2 varchar(19),name2 varchar(50),db_sex2 varchar(18),zip_code2 varchar(5),wk_addr2 varchar(44),address_52 varchar(60),db_id_no2 varchar(7),db_batch_no2 varchar(12),type_code2 varchar(1), "
        + "group_type3 varchar(2), card_no3 varchar(19),name3 varchar(50),db_sex3 varchar(18),zip_code3 varchar(5),wk_addr3 varchar(44),address_53 varchar(60),db_id_no3 varchar(7),db_batch_no3 varchar(12),type_code3 varchar(1), "
        + "group_type4 varchar(2), card_no4 varchar(19),name4 varchar(50),db_sex4 varchar(18),zip_code4 varchar(5),wk_addr4 varchar(44),address_54 varchar(60),db_id_no4 varchar(7),db_batch_no4 varchar(12),type_code4 varchar(1) "
        + " ) on commit preserve rows not logged ";
    sqlExec(crTempTable);
    // 重新讀取資料
    String exTypeCode = wp.itemStr("ex_type_code");
    String exDbMontype = wp.itemStr("ex_db_montype");
    String exDbPrize1 = wp.itemStr("ex_db_prize1");
    String exDbPrize2 = wp.itemStr("ex_db_prize2");
    String exBatchNo = wp.itemStr("ex_batch_no");
    wp.sqlCmd = "select group_type, " + "batch_no, " + "seq_no, " + "card_no, " + "name, "
        + "decode(sex,'1','先生      收','2','小姐      收') as db_sex, " + "zip_code, "
        + "(address_1 || address_2 || address_3 || address_4) wk_addr, " + "address_5, "
        + "SUBSTR(id_no,1,7) as db_id_no, " + "batch_no, " + "type_code, "
        + "decode(type_code,'1',batch_no,group_type,'',batch_no,batch_no ||'-'|| group_type) db_batch_no "
        + "from mkt_list " + "where 1=1 ";
    wp.sqlCmd += sqlCol(exTypeCode, "type_code");
    wp.sqlCmd += sqlCol(exBatchNo, "batch_no");
    switch (exTypeCode) {
      case "2":
        if (!exDbMontype.equals("0")) {
          wp.sqlCmd += sqlCol(exDbMontype, "group_type");
        }
        break;
      case "3":
        wp.sqlCmd += sqlStrend(exDbPrize1, exDbPrize2, "group_type");
        break;
    }
    sqlSelect(wp.sqlCmd);
    // 依資料筆數重新新增於暫存表檔
    int rct = 0, rc = 0, rc1 = 0, rtn = 0;
    rct = sqlRowNum;
    rc = rct % 4;
    if (rc == 0) {
      for (int ii = 0; ii < rct; ii++) {
        rc1 = (ii + 1) % 4;
        if (rc1 == 1) {
          sqlInsert1(ii);
        }
      }
    }
    if (rc == 1) {
      for (int ii = 0; ii < rct - 1; ii++) {
        rc1 = (ii + 1) % 4;
        if (rc1 == 1) {
          rtn = sqlInsert1(ii);
        }
      }
      rtn = sqlInsert2(rct - 1);
    }
    if (rc == 2) {
      for (int ii = 0; ii < rct - 2; ii++) {
        rc1 = (ii + 1) % 4;
        if (rc1 == 1) {
          System.out.println("GG...");
          sqlInsert1(ii);
        }
      }
      sqlInsert3(rct - 2);
    }
    if (rc == 3) {
      for (int ii = 0; ii < rct - 3; ii++) {
        rc1 = (ii + 1) % 4;
        if (rc1 == 1) {
          sqlInsert1(ii);
        }
      }
      sqlInsert4(rct - 3);
    }
    // wp.sqlCmd = "select count(*) ct from session.mkt_list_temp_test ";
    // sqlSelect(wp.sqlCmd);
    // System.out.println("mkt_list_temp_test ct :"+sql_ss("ct"));
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      // subTitle();
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
    // subTitle();
    wp.colSet("cond_1", reportSubtitle);
    // ===========================
    wp.pageRows = 99999;
    // queryFunc();

    wp.sqlCmd = "select * from session.mkt_list_temp_test ";
    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 30;
    // pdf.pageVert= true; //直印
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  void getSql() {
    gsSql = "insert into session.mkt_list_temp_test ("
        + "group_type1, card_no1, name1, db_sex1, zip_code1, wk_addr1, address_51, db_id_no1, db_batch_no1, type_code1, "
        + "group_type2, card_no2, name2, db_sex2, zip_code2, wk_addr2, address_52, db_id_no2, db_batch_no2, type_code2, "
        + "group_type3, card_no3, name3, db_sex3, zip_code3, wk_addr3, address_53, db_id_no3, db_batch_no3, type_code3, "
        + "group_type4, card_no4, name4, db_sex4, zip_code4, wk_addr4, address_54, db_id_no4, db_batch_no4, type_code4 "
        + ") values ( "
        + ":group_type1, :card_no1, :name1, :db_sex1, :zip_code1, :wk_addr1, :address_51, :db_id_no1, :db_batch_no1, :type_code1, "
        + ":group_type2, :card_no2, :name2, :db_sex2, :zip_code2, :wk_addr2, :address_52, :db_id_no2, :db_batch_no2, :type_code2, "
        + ":group_type3, :card_no3, :name3, :db_sex3, :zip_code3, :wk_addr3, :address_53, :db_id_no3, :db_batch_no3, :type_code3, "
        + ":group_type4, :card_no4, :name4, :db_sex4, :zip_code4, :wk_addr4, :address_54, :db_id_no4, :db_batch_no4, :type_code4 "
        + ") ";
  }

  public int sqlInsert1(int ii) {
    String isSql = "";
    getSql();
    int rc = 0;
    isSql += gsSql;
    setString("group_type1", sqlStr(ii, "group_type"));
    setString("card_no1", sqlStr(ii, "card_no"));
    setString("name1", sqlStr(ii, "name"));
    setString("db_sex1", sqlStr(ii, "db_sex"));
    setString("zip_code1", sqlStr(ii, "zip_code"));
    setString("wk_addr1", sqlStr(ii, "wk_addr"));
    setString("address_51", sqlStr(ii, "address_5"));
    setString("db_id_no1", sqlStr(ii, "db_id_no"));
    setString("db_batch_no1", sqlStr(ii, "db_batch_no"));
    setString("type_code1", sqlStr(ii, "type_code"));
    //
    setString("group_type2", sqlStr(ii + 1, "group_type"));
    setString("card_no2", sqlStr(ii + 1, "card_no"));
    setString("name2", sqlStr(ii + 1, "name"));
    setString("db_sex2", sqlStr(ii + 1, "db_sex"));
    setString("zip_code2", sqlStr(ii + 1, "zip_code"));
    setString("wk_addr2", sqlStr(ii + 1, "wk_addr"));
    setString("address_52", sqlStr(ii + 1, "address_5"));
    setString("db_id_no2", sqlStr(ii + 1, "db_id_no"));
    setString("db_batch_no2", sqlStr(ii + 1, "db_batch_no"));
    setString("type_code2", sqlStr(ii + 1, "type_code"));
    //
    setString("group_type3", sqlStr(ii + 2, "group_type"));
    setString("card_no3", sqlStr(ii + 2, "card_no"));
    setString("name3", sqlStr(ii + 2, "name"));
    setString("db_sex3", sqlStr(ii + 2, "db_sex"));
    setString("zip_code3", sqlStr(ii + 2, "zip_code"));
    setString("wk_addr3", sqlStr(ii + 2, "wk_addr"));
    setString("address_53", sqlStr(ii + 2, "address_5"));
    setString("db_id_no3", sqlStr(ii + 2, "db_id_no"));
    setString("db_batch_no3", sqlStr(ii + 2, "db_batch_no"));
    setString("type_code3", sqlStr(ii + 2, "type_code"));
    //
    setString("group_type4", sqlStr(ii + 3, "group_type"));
    setString("card_no4", sqlStr(ii + 3, "card_no"));
    setString("name4", sqlStr(ii + 3, "name"));
    setString("db_sex4", sqlStr(ii + 3, "db_sex"));
    setString("zip_code4", sqlStr(ii + 3, "zip_code"));
    setString("wk_addr4", sqlStr(ii + 3, "wk_addr"));
    setString("address_54", sqlStr(ii + 3, "address_5"));
    setString("db_id_no4", sqlStr(ii + 3, "db_id_no"));
    setString("db_batch_no4", sqlStr(ii + 3, "db_batch_no"));
    setString("type_code4", sqlStr(ii + 3, "type_code"));
    sqlExec(isSql);
    if (sqlRowNum > 0) {
      rc = 1;
    }
    return rc;
  }

  public int sqlInsert2(int ii) {
    String isSql = "";
    getSql();
    int rc = 0;
    isSql = gsSql;
    setString("group_type1", sqlStr(ii, "group_type"));
    setString("card_no1", sqlStr(ii, "card_no"));
    setString("name1", sqlStr(ii, "name"));
    setString("db_sex1", sqlStr(ii, "db_sex"));
    setString("zip_code1", sqlStr(ii, "zip_code"));
    setString("wk_addr1", sqlStr(ii, "wk_addr"));
    setString("address_51", sqlStr(ii, "address_5"));
    setString("db_id_no1", sqlStr(ii, "db_id_no"));
    setString("db_batch_no1", sqlStr(ii, "db_batch_no"));
    setString("type_code1", sqlStr(ii, "type_code"));
    //
    setString("group_type2", "");
    setString("card_no2", "");
    setString("name2", "");
    setString("db_sex2", "");
    setString("zip_code2", "");
    setString("wk_addr2", "");
    setString("address_52", "");
    setString("db_id_no2", "");
    setString("db_batch_no2", "");
    setString("type_code2", "");
    //
    setString("group_type3", "");
    setString("card_no3", "");
    setString("name3", "");
    setString("db_sex3", "");
    setString("zip_code3", "");
    setString("wk_addr3", "");
    setString("address_53", "");
    setString("db_id_no3", "");
    setString("db_batch_no3", "");
    setString("type_code3", "");
    //
    setString("group_type4", "");
    setString("card_no4", "");
    setString("name4", "");
    setString("db_sex4", "");
    setString("zip_code4", "");
    setString("wk_addr4", "");
    setString("address_54", "");
    setString("db_id_no4", "");
    setString("db_batch_no4", "");
    setString("type_code4", "");
    sqlExec(isSql);
    if (sqlRowNum > 0) {
      rc = 1;
    }
    return rc;
  }

  public int sqlInsert3(int ii) {
    String isSql = "";
    getSql();
    int rc = 0;
    isSql += gsSql;
    setString("group_type1", sqlStr(ii, "group_type"));
    setString("card_no1", sqlStr(ii, "card_no"));
    setString("name1", sqlStr(ii, "name"));
    setString("db_sex1", sqlStr(ii, "db_sex"));
    setString("zip_code1", sqlStr(ii, "zip_code"));
    setString("wk_addr1", sqlStr(ii, "wk_addr"));
    setString("address_51", sqlStr(ii, "address_5"));
    setString("db_id_no1", sqlStr(ii, "db_id_no"));
    setString("db_batch_no1", sqlStr(ii, "db_batch_no"));
    setString("type_code1", sqlStr(ii, "type_code"));
    //
    setString("group_type2", sqlStr(ii + 1, "group_type"));
    setString("card_no2", sqlStr(ii + 1, "card_no"));
    setString("name2", sqlStr(ii + 1, "name"));
    setString("db_sex2", sqlStr(ii + 1, "db_sex"));
    setString("zip_code2", sqlStr(ii + 1, "zip_code"));
    setString("wk_addr2", sqlStr(ii + 1, "wk_addr"));
    setString("address_52", sqlStr(ii + 1, "address_5"));
    setString("db_id_no2", sqlStr(ii + 1, "db_id_no"));
    setString("db_batch_no2", sqlStr(ii + 1, "db_batch_no"));
    setString("type_code2", sqlStr(ii + 1, "type_code"));
    //
    setString("group_type3", "");
    setString("card_no3", "");
    setString("name3", "");
    setString("db_sex3", "");
    setString("zip_code3", "");
    setString("wk_addr3", "");
    setString("address_53", "");
    setString("db_id_no3", "");
    setString("db_batch_no3", "");
    setString("type_code3", "");
    //
    setString("group_type4", "");
    setString("card_no4", "");
    setString("name4", "");
    setString("db_sex4", "");
    setString("zip_code4", "");
    setString("wk_addr4", "");
    setString("address_54", "");
    setString("db_id_no4", "");
    setString("db_batch_no4", "");
    setString("type_code4", "");
    sqlExec(isSql);
    if (sqlRowNum > 0) {
      rc = 1;
    }
    return rc;
  }

  public int sqlInsert4(int ii) {
    String isSql = "";
    getSql();
    int rc = 0;
    isSql += gsSql;
    setString("group_type1", sqlStr(ii, "group_type"));
    setString("card_no1", sqlStr(ii, "card_no"));
    setString("name1", sqlStr(ii, "name"));
    setString("db_sex1", sqlStr(ii, "db_sex"));
    setString("zip_code1", sqlStr(ii, "zip_code"));
    setString("wk_addr1", sqlStr(ii, "wk_addr"));
    setString("address_51", sqlStr(ii, "address_5"));
    setString("db_id_no1", sqlStr(ii, "db_id_no"));
    setString("db_batch_no1", sqlStr(ii, "db_batch_no"));
    setString("type_code1", sqlStr(ii, "type_code"));
    //
    setString("group_type2", sqlStr(ii + 1, "group_type"));
    setString("card_no2", sqlStr(ii + 1, "card_no"));
    setString("name2", sqlStr(ii + 1, "name"));
    setString("db_sex2", sqlStr(ii + 1, "db_sex"));
    setString("zip_code2", sqlStr(ii + 1, "zip_code"));
    setString("wk_addr2", sqlStr(ii + 1, "wk_addr"));
    setString("address_52", sqlStr(ii + 1, "address_5"));
    setString("db_id_no2", sqlStr(ii + 1, "db_id_no"));
    setString("db_batch_no2", sqlStr(ii + 1, "db_batch_no"));
    setString("type_code2", sqlStr(ii + 1, "type_code"));
    //
    setString("group_type3", sqlStr(ii + 2, "group_type"));
    setString("card_no3", sqlStr(ii + 2, "card_no"));
    setString("name3", sqlStr(ii + 2, "name"));
    setString("db_sex3", sqlStr(ii + 2, "db_sex"));
    setString("zip_code3", sqlStr(ii + 2, "zip_code"));
    setString("wk_addr3", sqlStr(ii + 2, "wk_addr"));
    setString("address_53", sqlStr(ii + 2, "address_5"));
    setString("db_id_no3", sqlStr(ii + 2, "db_id_no"));
    setString("db_batch_no3", sqlStr(ii + 2, "db_batch_no"));
    setString("type_code3", sqlStr(ii + 2, "type_code"));
    //
    setString("group_type4", "");
    setString("card_no4", "");
    setString("name4", "");
    setString("db_sex4", "");
    setString("zip_code4", "");
    setString("wk_addr4", "");
    setString("address_54", "");
    setString("db_id_no4", "");
    setString("batch_no4", "");
    setString("type_code4", "");
    sqlExec(isSql);
    if (sqlRowNum > 0) {
      rc = 1;
    }
    return rc;
  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_acct_type
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_acct_type");
      // dddw_list("dddw_acct_type", "ptr_acct_type", "acct_type",
      // "chin_name", "where 1=1 and card_indicator='2' order by acct_type
      // ");
    } catch (Exception ex) {

    }
  }

}
