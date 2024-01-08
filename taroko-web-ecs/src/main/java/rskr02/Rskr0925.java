package rskr02;
/**
 * 2019-0628:  JH    bugfix
 * 2019-0621:  JH    p_xxx >>acno_p_xxx
 *  V.2018-0611.JH
 *  109-04-28  V1.00.00  Tanwei       updated for project coding standard
 * 110-01-05  V1.00.01  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         * 
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr0925 extends BaseAction implements InfacePdf {
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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "rskr0925")) {
        wp.optionKey = wp.colStr(0, "ex_log_reason");
        dddwList("dddw_log_reason", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='ADJ_REASON_UP'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {

    if (itemallEmpty("ex_apr_date1,ex_apr_date2".split(","))) {
      alertErr2("請輸入 覆核期間");
      return;
    }
    if (icondStrend("ex_apr_date1", "ex_apr_date2") == false) {
      alertErr2("覆核日期起迄：輸入錯誤");
      return;
    }

    String isLog = "", isApr = "";

    String lsWhere = " where B.acno_p_seqno = A.acno_p_seqno ";
    lsWhere += " and A.log_mode='1' and A.log_type='1' and A.kind_flag ='A' ";
    lsWhere += " and A.aft_loc_amt > A.bef_loc_amt ";
    lsWhere += " and B.current_code='0' and B.sup_flag='1' ";
    lsWhere += sqlCol(wp.itemStr("ex_apr_date1"), "A.apr_date", ">=")
        + sqlCol(wp.itemStr("ex_apr_date2"), "A.apr_date", "<=")
        + sqlCol(wp.itemStr("ex_log_reason"), "A.log_reason");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " " + " uf_nvl(A.apr_date,A.log_date) as db_sort_date ," + " B.card_no ,"
        + " B.id_p_seqno ," + " B.sup_flag ," + " B.current_code ," + " A.log_date ,"
        + " A.log_reason ," + " A.bef_loc_amt ," + " A.aft_loc_amt ," + " A.apr_user ,"
        + " A.apr_date ," + " A.acno_p_seqno ,"
        + " uf_idno_name(B.major_id_p_seqno) as db_major_name ," + " '' as db_bir_date ,"
        + " '' as db_idno_name ," + " '' as db_sex ," + " 'checked' as check_on ";
    wp.daoTable = "crd_card B, rsk_acnolog A ";
    wp.whereOrder = " order by 1 ";
    pageQuery();

    if (sqlRowNum <= 0) {
      wp.colSet("ll_rows", "0");
      wp.colSet("ex_print_rows", "0");
      alertErr2("此條件查無資料");
      return;
    }
    wp.colSet("ll_rows", wp.selectCnt);
    wp.colSet("ex_print_rows", wp.selectCnt);
    queryAfter();

    wp.setListCount(1);
    wp.setPageValue();
  }

  void queryAfter() {
    String sql1 = "select chi_name as db_idno_name ," + " birthday as db_bir_date ,"
        + " sex as db_sex " + " from crd_idno " + " where id_p_seqno =?";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.logSql = false;
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "id_p_seqno")});
      if (sqlRowNum <= 0)
        continue;
      wp.colSet(ii, "db_birdate", sqlStr("db_bir_date"));
      wp.colSet(ii, "db_idno_name", sqlStr("db_idno_name"));
      wp.colSet(ii, "db_sex", sqlStr("db_sex"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {

    wp.reportId = "rskr0925";
    wp.pageRows = 9999;
    if (printData() != 1)
      return;

    TarokoPDF pdf = new TarokoPDF();
    pdf.pageVert = true;

    wp.fileMode = "Y";
    pdf.excelTemplate = "rskr0925.xlsx";
    pdf.pageCount = 1;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

    return;

  }

  int printData() {
    int llBlank1 = 0, llBlank2 = 0;
    String ls14 = "", ls15 = "", lsBlank1 = "", lsBlank2 = "", lsEx = "";
    lsEx = "　　依主管機關規定，本行調高客戶信用卡額度時，須書面通知附卡持卡人，且附卡";
    // ll_ex = ls_ex.length();
    String lsSysdate = "", lsPrintDate = "";
    String lsZip = "", lsAddr = "", lsAddr5 = "";
    String[] lsPSeqno = wp.itemBuff("acno_p_seqno");
    String[] lsCname = wp.itemBuff("db_idno_name");
    String[] lsSex = wp.itemBuff("db_sex");
    String[] lsCnameMajor = wp.itemBuff("db_major_name");
    String[] liBefAmt = wp.itemBuff("bef_loc_amt");
    String[] liAftAmt = wp.itemBuff("aft_loc_amt");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = wp.itemRows("card_no");
    if (optToIndex(aaOpt[0]) < 0) {
      alertPdfErr("請點選列印資料");
      return 0;
    }

    lsSysdate = getSysDate();
    lsSysdate = commDate.toTwDate(lsSysdate);
    lsPrintDate = "中　　　華　　　民　　　國　　" + lsSysdate.substring(0, 3) + "　 年 　"
        + lsSysdate.substring(3, 5) + "　 月　" + lsSysdate.substring(5, 7) + "　日";

    byte[] llEx = lsEx.getBytes();

    int ii = 0;
    for (int rr = 0; rr < aaOpt.length; rr++) {
      // -option-ON-
      if (optToIndex(aaOpt[rr]) < 0) {
        continue;
      }

      llBlank1 = 0;
      llBlank2 = 0;
      lsZip = "";
      lsAddr = "";
      lsAddr5 = "";
      ls14 = "";
      ls15 = "";
      lsBlank1 = "";
      lsBlank2 = "";
      String sql1 = "select bill_sending_zip as ls_zip ,"
          + " bill_sending_addr1||bill_sending_addr2||bill_sending_addr3 as ls_addr ,"
          + " bill_sending_addr4||bill_sending_addr5  as ls_addr5 " + " from act_acno "
          + " where acno_p_seqno =?";
      sqlSelect(sql1, new Object[] {lsPSeqno[rr]});
      lsZip = sqlStr("ls_zip");
      lsAddr = sqlStr("ls_addr");
      lsAddr5 = sqlStr("ls_addr5");
      wp.colSet(ii, "ls_cname", lsCname[rr]);
      if (this.eqIgno(lsSex[rr], "1")) {
        wp.colSet(ii, "ls_sex", "先生");
      } else {
        wp.colSet(ii, "ls_sex", "小姐");
      }
      if (!eqIgno(lsCname[rr], lsCnameMajor[rr])) {
        wp.colSet(ii, "ls_cname_major1", "請  " + lsCnameMajor[rr] + " 代轉");
      } else {
        wp.colSet(ii, "ls_cname_major1", " ");
      }
      // 持卡人有終止信用卡附卡契約之權利。因本行已核准正卡持卡人 {ls_cname_major} 君
      wp.colSet(ii, "ls_cname_major", lsCnameMajor[rr]);
      wp.colSet(ii, "ex_ll_11", lsCname[rr] + " " + wp.colStr(ii, "ls_sex") + " 您好:");
      wp.colSet(ii, "ls_zip", lsZip);
      wp.colSet(ii, "ls_addr", lsAddr);
      wp.colSet(ii, "ls_addr5", lsAddr5 + " (請  " + lsCnameMajor[rr] + " 代轉)");
      wp.colSet(ii, "print_date", lsPrintDate);

      /*
       * 依主管機關規定，本行調高客戶信用卡額度時，須書面通知附卡持卡人，且附卡 持卡人有終止信用卡附卡契約之權利。因本行已核准正卡持卡人＿＿ＸＸＸ＿＿＿君
       * 之申請，將信用卡額度由新台幣xx999,999,999x元調高至新台幣xx999,999,999x元，
       */
      String lsMaName = lsCnameMajor[rr];
      if (lsMaName.length() >= 5) {
        lsMaName = commString.mid(lsMaName, 0, 1) + "ＸＸ";
      }
      if (lsMaName.length() == 2) {
        lsMaName = "　　　" + lsMaName;
      } else if (lsMaName.length() == 3) {
        lsMaName = "　　" + lsMaName;
      } else if (lsMaName.length() == 4) {
        lsMaName = "　　" + lsMaName;
      } else if (lsMaName.length() >= 5) {
        lsMaName = "　" + lsMaName;
      }
      lsMaName = commString.rpad(lsMaName, 8, "　");

      String lsAmt1 = String.format("%,.0f", commString.strToNum(liBefAmt[rr]));
      lsAmt1 = commString.lpad(lsAmt1, 13, " ");
      String lsAmt2 = String.format("%,.0f", commString.strToNum(liAftAmt[rr]));
      lsAmt2 = commString.lpad(lsAmt2, 13, " ");

      wp.colSet(ii, "ex_ll_14", "持卡人有終止信用卡附卡契約之權利。因本行已核准正卡持卡人" + lsMaName + "君");
      wp.colSet(ii, "ex_ll_15", "之申請，將信用卡額度由新台幣 " + lsAmt1 + " 元調高至新台幣 " + lsAmt2 + " 元，");
      ii++;
    }
    wp.listCount[0] = ii;

    return 1;
  }

}
