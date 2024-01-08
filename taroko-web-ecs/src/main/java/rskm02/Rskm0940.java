/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-24  V1.00.01   Justin        parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package rskm02;
/** DebitCard 臨時額度異動簽單
 * 2019-0619:  JH    p_xxx >>acno_pxxx
 * */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskm0940 extends BaseAction implements InfacePdf {
  String seqNo = "";
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  @Override
  public void userAction() throws Exception {
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
      readInitData();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) {
      // -資料處理-
      strAction = "PDF";
      pdfPrint();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "rskm0940_detl")) {
        wp.optionKey = wp.colStr(0, "charge_user");
        dddwList("dddw_charge_user", "ptr_sys_idtab", "wf_id", "wf_id",
            "where wf_type ='RSKM0930_CHARGE' ");
      }
    } catch (Exception ex) {
    }

    String lsSql = "";
    try {
      if (wp.colEmpty("id_no") == false) {
        if (wp.colEq("chg", "Y"))
          wp.colSet("card_no", "");
        lsSql = " select A.card_no as db_code , " + " A.card_no as db_desc "
            + " from cca_card_base A join dbc_card B on A.card_no = B.card_no "
            + " where A.id_p_seqno in (select id_p_seqno from dbc_idno where 1=1 "
            + sqlCol(wp.colStr("id_no"), "id_no")
            + " ) and A.debit_flag ='Y' and B.current_code ='0'  " + " order by 2 Asc";
        wp.optionKey = wp.colStr("card_no");
        dddwList("dddw_card_no", lsSql);
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 " 
        + sqlCol(wp.itemStr("ex_seq_no"), "seq_no", "like%")
        + sqlCol(wp.itemStr("ex_idno"), "id_no", "like%")
        + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%")
        + sqlCol(wp.itemStr("ex_tel_user"), "tel_user", "like%")
        + sqlCol(wp.itemStr("ex_fast_flag"), "uf_nvl(fast_flag,'N')")
        + sqlCol(wp.itemStr("ex_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "crt_date", "<=");

    if (!wp.itemEq("ex_close_flag", "0"))
      lsWhere += sqlCol(wp.itemStr("ex_close_flag"), "uf_nvl(close_flag,'N')", "like%");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " seq_no ," + " tel_date ," + " tel_time ," + " id_no ," + " id_code ,"
        + " card_no ," + " chi_name ," + " print_flag ," + " close_flag ," + " crt_date ,"
        + " fast_flag ," + " tel_user ";
    wp.daoTable = "rsk_credits_vd";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(0);
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    seqNo = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(seqNo))
      seqNo = itemkk("seq_no");
    wp.selectSQL = "" + " seq_no ," + " tel_date ," + " tel_time ," + " tel_user ," + " apr_flag ,"
        + " apr_user ," + " id_no ," + " id_code ," + " print_flag ," + " comp_name ,"
        + " comp_title ," + " audi_other ," + " audi_oth_note ," + " fast_flag ," + " self_flag ,"
        + " area_code ," + " consum_item ," + " consum_amt ," + " over_time_flag ,"
        + " over_dd_flag ," + " over_mm_flag ," + " abroad_flag ," + " abroad_date1 ,"
        + " abroad_date2 ," + " oth_flag ," + " oth_reason ," + " sms_flag ," + " tel_flag ,"
        + " tel_no ," + " email_flag ," + " email_addr ," + " email_addr as h_mail ,"
        + " reply_date ," + " reply_time ," + " adj_date1 ," + " adj_date2 ," + " adj_item ,"
        + " adj_rate ," + " adj_amt ," + " adj_remark ," + " adj_remark2 ," + " sign_flag ,"
        + " sign_date ," + " audit_remark ," + " audit_result ," + " charge_user ,"
        + " appr_passwd ," + " user_id ," + " decs_user ," + " crt_user ," + " crt_date ,"
        + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno ," + " card_no ,"
        + " chi_name ," + " card_cond ," + " print_cnt ,"
        + " substrb(card_cond,1,1) db_card_cond01 ," + " substrb(card_cond,2,1) db_card_cond02 ,"
        + " substrb(card_cond,3,1) db_card_cond03 ," + " substrb(card_cond,4,1) db_card_cond04 ,"
        + " audi_item ," + " substrb(audi_item,1,1) db_audi_item01 ,"
        + " substrb(audi_item,2,1) db_audi_item02 ," + " substrb(audi_item,3,1) db_audi_item03 ,"
        + " substrb(audi_item,4,1) db_audi_item04 ," + " substrb(audi_item,5,1) db_audi_item05 ,"
        + " substrb(audi_item,6,1) db_audi_item06 ," + " substrb(audi_item,7,1) db_audi_item07 ,"
        + " substrb(audi_item,8,1) db_audi_item08 ," + " substrb(audi_item,9,1) db_audi_item09 ,"
        + " substrb(audi_item,10,1) db_audi_item10 ," + " consum_amt_note ," + " adj_bef_amt ,"
        + " purch_flag ," + " cntry_code ," + " over_lmt_parm_flag ," + " dd_cnt_flag ,"
        + " mm_cnt_flag ," + " risk_type_flag ,"
        + " rpad(nvl(risk_type_val,'N'),15,'N') as risk_type_val ," + " time_lmt ," + " dd_lmt ,"
        + " mm_lmt ," + " limit_flag ," + " time_pcnt ," + " dd_pcnt ," + " mm_pcnt ,"
        + " dd_cnt_pcnt ," + " mm_cnt_pcnt ," + " purch_item_flag ,"
        + " substrb(risk_type_val,1,1) as db_risk_p ,"
        + " substrb(risk_type_val,2,1) as db_risk_d1 ,"
        + " substrb(risk_type_val,3,1) as db_risk_t ,"
        + " substrb(risk_type_val,4,1) as db_risk_H ,"
        + " substrb(risk_type_val,5,1) as db_risk_E ,"
        + " substrb(risk_type_val,6,1) as db_risk_M ,"
        + " substrb(risk_type_val,7,1) as db_risk_J ,"
        + " substrb(risk_type_val,8,1) as db_risk_R ,"
        + " substrb(risk_type_val,9,1) as db_risk_L ,"
        + " substrb(risk_type_val,10,1) as db_risk_I ,"
        + " substrb(risk_type_val,11,1) as db_risk_X ,"
        + " substrb(risk_type_val,12,1) as db_risk_G ,"
        + " substrb(risk_type_val,13,1) as db_risk_P1 ,"
        + " substrb(risk_type_val,14,1) as db_risk_P2 ,"
        + " substrb(risk_type_val,15,1) as db_risk_P3 ,"
        + " substrb(risk_type_val,16,1) as db_risk_P0 ,"
        + " substrb(risk_type_val,17,1) as db_risk_M1 ,"
        + " substrb(risk_type_val,18,1) as db_risk_M2 ,"
        + " substrb(risk_type_val,19,1) as db_risk_C ," + " dd_cnt ," + " mm_cnt ,"
        + " 'N' as db_user_id_flag ," + " hex(rowid) as rowid ," + " terror_money ,"
        + " purchase_item ," + " purchase_item2 ," + " purchase_item3 ,"
        + " nvl(close_flag,'N') as close_flag , " + " time_lmt * time_pcnt / 100 as wk_time_amt , "
        + " dd_lmt * dd_pcnt / 100 as wk_dd_amt , " + " mm_lmt * mm_pcnt / 100 as wk_mm_amt , "
        + " dd_cnt * dd_cnt_pcnt / 100 as wk_dd_cnt , "
        + " mm_cnt * mm_cnt_pcnt / 100 as wk_mm_cnt , "
        + " to_char(mod_time,'yyyymmdd') as mod_date , " + " no_callback_flag , " + " risk_type2 ";
    wp.daoTable = "rsk_credits_vd";
    wp.whereStr = "where 1=1" + sqlCol(seqNo, "seq_no");
    pageSelect();
    if (sqlRowNum <= 0) {
      alertErr2("查無資料");
      return;
    }
    wp.colSet("adj_remark0", commString.mid(wp.colStr("adj_remark"), 0, 100));
    wp.colSet("adj_remark1", commString.mid(wp.colStr("adj_remark"), 100, 100));

    wp.colSet("user_no", wp.loginUser);
    userName();
    dataReadAfter();
  }

  void dataReadAfter() {
    String sql1 = "select " + " B.e_mail_addr " + " from dbc_card A , dbc_idno B "
        + " where A.id_p_seqno = B.id_p_seqno " + " and A.current_code = '0' "
        + " and B.id_no = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("id_no")});

    if (sqlRowNum > 0) {
      wp.colSet("h_mail", sqlStr("e_mail_addr"));
    }

    wp.colSet("wk_dd_cnt", (int) Math.floor(wp.colNum("wk_dd_cnt")));
    wp.colSet("wk_mm_cnt", (int) Math.floor(wp.colNum("wk_mm_cnt")));
  }

  void userName() {
    String sql1 = " select " + " usr_cname " + " from sec_user " + " where usr_id = ? ";

    sqlSelect(sql1, new Object[] {wp.loginUser});

    if (sqlRowNum > 0) {
      wp.colSet("user_name", sqlStr("usr_cname"));
    }

  }

  @Override
  public void saveFunc() throws Exception {
    rskm02.Rskm0940Func func = new rskm02.Rskm0940Func();
    func.setConn(wp);
    if (this.isDelete() && checkApproveZz() == false) {
      return;
    }
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    } else
      this.saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "rskm0940_detl")) {
      this.btnModeAud();
    }

  }

  @Override
  public void initPage() {
    int liTimeAmt = 0, liDdAmt = 0, liMmAmt = 0, liDdCnt = 0, liMmCnt = 0;
    wp.colSet("time_lmt", "40000");
    wp.colSet("dd_lmt", "60000");
    wp.colSet("mm_lmt", "500000");
    wp.colSet("time_pcnt", "100");
    wp.colSet("dd_pcnt", "100");
    wp.colSet("mm_pcnt", "100");
    wp.colSet("dd_cnt_pcnt", "100");
    wp.colSet("mm_cnt_pcnt", "100");

    liTimeAmt = 40000 * 100 / 100;
    liDdAmt = 60000 * 100 / 100;
    liMmAmt = 500000 * 100 / 100;
    liDdCnt = 99 * 100 / 100;
    liMmCnt = 999 * 100 / 100;

    wp.colSet("wk_time_amt", "" + liTimeAmt);
    wp.colSet("wk_dd_amt", "" + liDdAmt);
    wp.colSet("wk_mm_amt", "" + liMmAmt);
    wp.colSet("wk_dd_cnt", "" + liDdCnt);
    wp.colSet("wk_mm_cnt", "" + liMmCnt);

    if (eqIgno(wp.respHtml, "rskm0940_detl") && eqIgno(strAction, "new") || empty(strAction)) {
      String sql1 = " select " + " usr_cname " + " from sec_user " + " where usr_id = ? ";

      sqlSelect(sql1, new Object[] {wp.loginUser});

      wp.colSet("tel_user", sqlStr("usr_cname"));
      wp.colSet("tel_date", getSysDate());
      wp.colSet("tel_time", commDate.sysTime());
      wp.colSet("area_code", "國內外");
      wp.colSet("adj_date1", getSysDate());
    }

    if (eqIgno(wp.respHtml, "rskm0940_detl")) {
      wp.colSet("user_no", wp.loginUser);
      userName();
    }
  }

  void readInitData() {

    if (wp.itemEmpty("id_no"))
      return;

    if (wp.itemStr("id_no").length() != 10) {
      alertErr2("持卡人ID: 輸入錯誤 !");
      wp.colSet("card_no", "");
      wp.colSet("dddw_card_no", "");
      wp.colSet("chi_name", "");
      wp.colSet("h_mail", "");
      wp.colSet("db_card_cond03", "");
      return;
    }

    String sql1 = "select " + " B.chi_name , " + " B.e_mail_addr "
        + " from dbc_card A , dbc_idno B " + " where A.id_p_seqno = B.id_p_seqno "
        + " and A.current_code = '0' " + " and B.id_no = ? ";

    sqlSelect(sql1, new Object[] {wp.itemStr("id_no")});

    if (sqlRowNum <= 0) {
      alertErr2("持卡人ID: 輸入錯誤 !");
      wp.colSet("card_no", "");
      wp.colSet("dddw_card_no", "");
      wp.colSet("chi_name", "");
      wp.colSet("h_mail", "");
      wp.colSet("db_card_cond03", "");
      return;
    }

    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("h_mail", sqlStr("e_mail_addr"));

    String sql2 = " select " + " count(*) as db_cnt " + " from crd_card " + " where id_p_seqno in "
        + " (select id_p_seqno from crd_idno where id_no = ? )" + " and current_code ='0' ";

    sqlSelect(sql2, new Object[] {wp.itemStr("id_no")});

    if (sqlNum("db_cnt") > 0) {
      wp.colSet("db_card_cond03", "Y");
    } else {
      wp.colSet("db_card_cond03", "");
    }

    wp.colSet("chg", "Y");

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "rskm0940";

    rskm02.Rskm0940Func func = new rskm02.Rskm0940Func();
    func.setConn(wp);

    if (func.printUpdate() != 1) {
      sqlCommit(-1);
      wp.respHtml = "TarokoErrorPDF";
      alertErr2("列印失敗");
      return;
    }

    dataRead();
    setData();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.pageVert = true;
    pdf.excelTemplate = "rskm0940.xlsx";
    pdf.pageCount = 35;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  void setData() {
    if (wp.colEq("apr_flag", "Y")) {
      wp.colSet("ex5E", "■ 檢查人員:" + wp.colStr("apr_user"));
    } else {
      wp.colSet("ex5E", "□ 檢查人員:" + wp.colStr("apr_user"));
    }

    wp.colSet("ex7B", commString.hideIdno(wp.colStr("id_no")));
    wp.colSet("ex7F", commString.hideCardNo(wp.colStr("card_no")));
    wp.colSet("ex7J", commString.hideIdnoName(wp.colStr("chi_name")));

    if (wp.colEq("print_flag", "Y")) {
      wp.colSet("ex7K", "■ 已列印 " + wp.colStr("print_cnt"));
    } else {
      wp.colSet("ex7K", "□ 已列印 " + wp.colStr("print_cnt"));
    }

    if (wp.colEq("db_card_cond01", "Y")) {
      wp.colSet("ex9B", "■ 1.持卡本人來電申請");
    } else {
      wp.colSet("ex9B", "□ 1.持卡本人來電申請");
    }

    if (wp.colEq("db_card_cond03", "Y")) {
      wp.colSet("ex9H", "■ 2.持有本行信用卡");
    } else {
      wp.colSet("ex9H", "□ 2.持有本行信用卡");
    }

    if (wp.colEq("db_card_cond04", "Y")) {
      wp.colSet("ex10B", "■ 3.消費無異常情形");
    } else {
      wp.colSet("ex10B", "□ 3.消費無異常情形");
    }

    if (wp.colEq("terror_money", "Y")) {
      wp.colSet("ex10H", "■ 4.須申報洗錢或資恐交易");
    } else {
      wp.colSet("ex10H", "□ 4.須申報洗錢或資恐交易");
    }
    // --
    if (wp.colEq("db_audi_item01", "Y")) {
      wp.colSet("ex14B", "■ 1.Debit Card 開戶分行");
    } else {
      wp.colSet("ex14B", "□ 1.Debit Card 開戶分行");
    }

    if (wp.colEq("db_audi_item06", "Y")) {
      wp.colSet("ex14H", "■ 6.電話號碼(手機、家電、公司)");
    } else {
      wp.colSet("ex14H", "□ 6.電話號碼(手機、家電、公司)");
    }

    if (wp.colEq("db_audi_item02", "Y")) {
      wp.colSet("ex15B", "■ 2.生日");
    } else {
      wp.colSet("ex15B", "□ 2.生日");
    }

    if (wp.colEq("db_audi_item07", "Y")) {
      wp.colSet("ex15H", "■ 7.最近一筆刷卡交易(特店、金額)");
    } else {
      wp.colSet("ex15H", "□ 7.最近一筆刷卡交易(特店、金額)");
    }

    if (wp.colEq("db_audi_item03", "Y")) {
      wp.colSet("ex16B", "■ 3.地址");
    } else {
      wp.colSet("ex16B", "□ 3.地址");
    }

    if (wp.colEq("db_audi_item08", "Y")) {
      wp.colSet("ex16H", "■ 8.最早往來或約持本行卡幾年");
    } else {
      wp.colSet("ex16H", "□ 8.最早往來或約持本行卡幾年");
    }

    if (wp.colEq("db_audi_item04", "Y")) {
      wp.colSet("ex17B", "■ 4.生肖");
    } else {
      wp.colSet("ex17B", "□ 4.生肖");
    }

    if (wp.colEq("db_audi_item09", "Y")) {
      wp.colSet("ex17H", "■ 9.持有本行卡片張數(含信用卡及Debit Card，不含一般金融卡)");
    } else {
      wp.colSet("ex17H", "□ 9.持有本行卡片張數(含信用卡及Debit Card，不含一般金融卡)");
    }

    if (wp.colEq("db_audi_item05", "Y")) {
      wp.colSet("ex18B", "■ 5.出生地");
    } else {
      wp.colSet("ex18B", "□ 5.出生地");
    }

    if (wp.colEq("db_audi_item10", "Y")) {
      wp.colSet("ex18H", "■ 10.其他:" + wp.colStr("audi_oth_note"));
    } else {
      wp.colSet("ex18H", "□ 10.其他:" + wp.colStr("audi_oth_note"));
    }

    if (wp.colEq("self_flag", "Y")) {
      wp.colSet("ex20E", "■ 主動服務");
    } else {
      wp.colSet("ex20E", "□ 主動服務");
    }

    if (wp.colEq("purch_flag", "Y")) {
      wp.colSet("ex21B", "■ 1.預約消費");
    } else {
      wp.colSet("ex21B", "□ 1.預約消費");
    }

    if (wp.colEq("purch_item_flag", "Y")) {
      wp.colSet("ex22B", "■ 消費項目　" + wp.colStr("consum_item"));
    } else {
      wp.colSet("ex22B", "□ 消費項目　" + wp.colStr("consum_item"));
    }

    if (wp.colEq("abroad_flag", "Y")) {
      wp.colSet("ex23B", "■ 出國消費　　國別:" + wp.colStr("cntry_code"));
    } else {
      wp.colSet("ex23B", "□ 出國消費　　國別:" + wp.colStr("cntry_code"));
    }

    wp.colSet("ex23H", "　　出國期間:" + commString.strToYmd(wp.colStr("abroad_date1")) + "　－　"
        + commString.strToYmd(wp.colStr("abroad_date2")));

    if (wp.colEq("over_lmt_parm_flag", "Y")) {
      wp.colSet("ex24B", "■ 2.超過系統參數");
    } else {
      wp.colSet("ex24B", "□ 2.超過系統參數");
    }

    String ex24C = "";

    if (wp.colEq("over_time_flag", "Y")) {
      ex24C += " ■ 次限額(7D)　";
    } else {
      ex24C += " □ 次限額(7D)　";
    }

    if (wp.colEq("over_dd_flag", "Y")) {
      ex24C += " ■ 日限額(6D)　";
    } else {
      ex24C += " □ 日限額(6D)　";
    }

    if (wp.colEq("over_mm_flag", "Y")) {
      ex24C += " ■ 月限額(5D)　";
    } else {
      ex24C += " □ 月限額(5D)　";
    }
    if (wp.colEq("dd_cnt_flag", "Y")) {
      ex24C += " ■ 日限次(8D)　";
    } else {
      ex24C += " □ 日限次(8D)　";
    }
    if (wp.colEq("mm_cnt_flag", "Y")) {
      ex24C += " ■ 月限次(9D)　";
    } else {
      ex24C += " □ 月限次(9D)　";
    }

    wp.colSet("ex24C", ex24C);

    wp.colSet("ex25B", "消費項目:" + wp.colStr("purchase_item"));

    if (wp.colEq("oth_flag", "Y")) {
      wp.colSet("ex26B", "■ 3.其他:" + wp.colStr("oth_reason"));
    } else {
      wp.colSet("ex26B", "□ 3.其他:" + wp.colStr("oth_reason"));
    }

    String ex29B = "";

    if (wp.colEq("db_risk_p", "Y")) {
      ex29B += " ■ P　";
    } else {
      ex29B += " □ P　";
    }

    if (wp.colEq("db_risk_d1", "Y")) {
      ex29B += " ■ D1　";
    } else {
      ex29B += " □ D1　";
    }

    if (wp.colEq("db_risk_t", "Y")) {
      ex29B += " ■ T　";
    } else {
      ex29B += " □ T　";
    }
    if (wp.colEq("db_risk_h", "Y")) {
      ex29B += " ■ H　";
    } else {
      ex29B += " □ H　";
    }
    if (wp.colEq("db_risk_e", "Y")) {
      ex29B += " ■ E　";
    } else {
      ex29B += " □ E　";
    }
    if (wp.colEq("db_risk_m", "Y")) {
      ex29B += " ■ M　";
    } else {
      ex29B += " □ M　";
    }
    if (wp.colEq("db_risk_j", "Y")) {
      ex29B += " ■ J　";
    } else {
      ex29B += " □ J　";
    }
    if (wp.colEq("db_risk_r", "Y")) {
      ex29B += " ■ R　";
    } else {
      ex29B += " □ R　";
    }
    if (wp.colEq("db_risk_l", "Y")) {
      ex29B += " ■ L　";
    } else {
      ex29B += " □ L　";
    }
    if (wp.colEq("db_risk_i", "Y")) {
      ex29B += " ■ I　";
    } else {
      ex29B += " □ I　";
    }
    if (wp.colEq("db_risk_x", "Y")) {
      ex29B += " ■ X　";
    } else {
      ex29B += " □ X　";
    }
    if (wp.colEq("db_risk_g", "Y")) {
      ex29B += " ■ G　";
    } else {
      ex29B += " □ G　";
    }
    if (wp.colEq("db_risk_p1", "Y")) {
      ex29B += " ■ P1　";
    } else {
      ex29B += " □ P1　";
    }
    if (wp.colEq("db_risk_c", "Y")) {
      ex29B += " ■ C　";
    } else {
      ex29B += " □ C　";
    }
    if (wp.colEq("db_risk_p3", "Y")) {
      ex29B += " ■ P3　";
    } else {
      ex29B += " □ P3　";
    }

    wp.colSet("ex29B", ex29B);

    String ex30B = "";

    if (wp.colEq("db_risk_p0", "Y")) {
      ex30B += " ■ P0　";
    } else {
      ex30B += " □ P0　";
    }


    if (wp.colEq("db_risk_m1", "Y")) {
      ex30B += "■ M1　";
    } else {
      ex30B += "□ M1　";
    }

    if (wp.colEq("db_risk_m2", "Y")) {
      ex30B += " ■ M2　";
    } else {
      ex30B += " □ M2　";
    }

    ex30B += "　　　" + wp.itemStr("risk_type2");

    wp.colSet("ex30B", ex30B);

    String ex33B = "";
    ex33B = wp.colStr("time_pcnt") + "/" + String.format("%,10.0f", wp.colNum("wk_time_amt"));
    wp.colSet("ex33B", ex33B);

    String ex33D = "";
    ex33D = wp.colStr("dd_pcnt") + "/" + String.format("%,10.0f", wp.colNum("wk_dd_amt"));
    wp.colSet("ex33D", ex33D);

    String ex33F = "";
    ex33F = wp.colStr("mm_pcnt") + "/" + String.format("%,10.0f", wp.colNum("wk_mm_amt"));
    wp.colSet("ex33F", ex33F);

    String ex33H = "";
    ex33H = wp.colStr("dd_cnt_pcnt") + "/" + String.format("%,10.0f", wp.colNum("wk_dd_cnt"));
    wp.colSet("ex33H", ex33H);

    String ex33J = "";
    ex33J = wp.colStr("mm_cnt_pcnt") + "/" + String.format("%,10.0f", wp.colNum("wk_mm_cnt"));
    wp.colSet("ex33J", ex33J);

    String ex35B = "";
    ex35B = "　　異動期間:" + commString.strToYmd(wp.colStr("adj_date1")) + "　－　"
        + commString.strToYmd(wp.colStr("adj_date2"));
    wp.colSet("ex35B", ex35B);

    if (wp.colEq("fast_flag", "Y")) {
      wp.colSet("ex39F", "■ 急件");
    } else {
      wp.colSet("ex39F", "□ 急件");
    }

    if (wp.colEq("no_callback_flag", "Y")) {
      wp.colSet("ex39H", "■ 不須回電");
    } else {
      wp.colSet("ex39H", "□ 不須回電");
    }

    if (wp.colEq("close_flag", "Y")) {
      wp.colSet("ex39K", "■ 結案");
    } else {
      wp.colSet("ex39K", "□ 結案");
    }

    if (wp.colEq("sms_flag", "Y")) {
      wp.colSet("ex40B", "■ 簡訊");
    } else {
      wp.colSet("ex40B", "□ 簡訊");
    }

    if (wp.colEq("tel_flag", "Y")) {
      wp.colSet("ex40D", "■ 回電");
    } else {
      wp.colSet("ex40D", "□ 回電");
    }

    wp.colSet("ex40F", "電話:" + commString.hideTelno(wp.colStr("tel_no")));

    if (wp.colEq("email_flag", "Y")) {
      wp.colSet("ex41B", "■ Mail");
    } else {
      wp.colSet("ex41B", "□ Mail");
    }

    wp.colSet("ex41D", "電子信箱:" + commString.hideEmail(wp.colStr("email_addr")));
    wp.listCount[0] = 1;
  }

}
