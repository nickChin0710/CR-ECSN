package cmsr02;
/**
 * 109-04-28   shiyuqi       updated for project coding standard     * 
 * 19-1205:      Alex             add initButton
 * 19-0614:      JH                 p_xxx >>acno_p_xxx
 * 20-0727:      JustinWu    fix the bug of the display of sex   
 * 20-0728:      JustinWu    sendnote: B.case_sale_id->A.case_seqno
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Cmsr0040 extends BaseAction implements InfacePdf {
  String lsIdno = "", lsCardNo = "";
  boolean ibPrint1 = false, ibPrint2 = false;

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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    } else if (eqIgno(wp.buttonCode, "PDF2")) { // -PDF-
      strAction = "PDF";
      pdfPrint2();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsr0040")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "ex_case_type");
        dddwList("d_dddw_casetype2",
            "select case_type||case_id as db_code , case_desc as db_desc from cms_casetype where 1=1 and case_type in ('A','B','C','D') ");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date_1"), wp.itemStr("ex_date_2")) == false) {
      alertErr2("補寄期間起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 "
    		+ " and A.case_date = B.case_date "
    		+ " and A.case_seqno = B.case_seqno "
            + sqlCol(wp.itemStr("ex_date1"), "A.case_date", ">=")
            + sqlCol(wp.itemStr("ex_date2"), "A.case_date", "<=")
            + sqlCol(wp.itemStr("ex_case_type"), "A.case_type")
            + sqlCol(wp.itemStr("ex_card_no"), "A.card_no")
            + sqlCol(wp.itemStr("ex_idno"), "A.case_idno")    ;
    if (!eqIgno(wp.itemStr("ex_send_type"), "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_send_type"), "B.mail_send_type");
    }
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" 
        + " A.card_no ," 
        + " A.case_idno ," 
        + " A.case_date ," 
        + " A.debit_flag ,"
        + " A.case_type ,"
        + " A.case_seqno ," 
        + " B.proc_deptno ," 
        + " B.case_prt_date ,"
        + " B.bill_sending_zip as bill_zip ," 
        + " B.bill_sending_addr1 as bill_addr1 ,"
        + " B.bill_sending_addr2 as bill_addr2 ," 
        + " B.bill_sending_addr3 as bill_addr3 ,"
        + " B.bill_sending_addr4 as bill_addr4 ," 
        + " B.bill_sending_addr5 as bill_addr5 ,"
        + " B.print_user ," 
        + " B.print_seqno ," 
        + " B.print_date ," 
        + " B.recv_cname ,"
        + " B.case_sale_id ," 
        + " B.mail_send_type ," 
        + " hex(B.rowid) as rowid , "
        + " B.proc_deptno||'-'||B.mail_send_type||'-'||A.case_seqno as sendnote ";
    wp.daoTable = "cms_casemaster A, cms_casepost B";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by A.case_date, A.card_no ";
    logSql();
    pageQuery();

    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr2("查無資料");
      return;
    }
    queryAfter();
    wp.setPageValue();
    printDataSet();
    printDataSet2();
  }

  void printDataSet2() {
    if (ibPrint2 == false)
      return;
    int ilSelectCnt = 0;
    ilSelectCnt = wp.selectCnt;
    String lsDesc1 = "", lsDesc2 = "";
    if (wp.itemEq("ex_send_text", "1")) {
      lsDesc1 = "普掛";
    } else if (wp.itemEq("ex_send_text", "2")) {
      lsDesc1 = "限掛";
    }
    for (int ii = 0; ii < ilSelectCnt; ii++) {
      lsDesc2 = wp.colStr(ii, "recv_cname") + "　　" + wp.colStr(ii, "bill_addr1")
          + wp.colStr(ii, "bill_addr2") + wp.colStr(ii, "bill_addr3") + wp.colStr(ii, "bill_addr4")
          + wp.colStr(ii, "bill_addr5");
      wp.colSet(ii, "ex_desc1", lsDesc1);
      wp.colSet(ii, "ex_desc2", lsDesc2);
    }
  }

  void queryAfter() {
    wp.logSql = false;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_addr",
          wp.colStr(ii, "bill_zip") + "-" + wp.colStr(ii, "bill_addr1")
              + wp.colStr(ii, "bill_addr2") + wp.colStr(ii, "bill_addr3")
              + wp.colStr(ii, "bill_addr4") + wp.colStr(ii, "bill_addr5"));
      
      wp.colSet(ii, "wk_sendnote", wp.colStr(ii, "proc_deptno") + "-"
          + wp.colStr(ii, "mail_send_type") + "-" + wp.colStr(ii, "case_sale_id"));
      
      wp.colSet(ii, "addr1", wp.colStr(ii, "bill_addr1") + wp.colStr(ii, "bill_addr2")
          + wp.colStr(ii, "bill_addr3") + wp.colStr(ii, "bill_addr4"));
      
      wp.colSet(ii, "addr2", wp.colStr(ii, "bill_addr5"));

      lsIdno = wp.colStr(ii, "case_idno");
      lsCardNo = wp.colStr(ii, "card_no");
      
      if (empty(lsIdno) && empty(lsCardNo)){
    	  wp.colSet(ii, "db_sex", "");
    	  continue;
      }
      
      sqlRowNum = 0;
      if (eqIgno(wp.colStr(ii, "debit_flag"), "N")) {
        if (!empty(lsCardNo)) {
        	
          String sql1 = " select A.chi_name , A.sex " 
              + " from crd_idno A , crd_card B "
              + " where 1=1 " 
              + " and A.id_p_seqno = B.id_p_seqno " 
              + " and B.card_no = ? ";
          
          sqlSelect(sql1, new Object[] {lsCardNo});
        } else if (!empty(lsIdno)) {
          
        String sql2 =
              " select chi_name , sex " 
          + " from crd_idno " 
          + " where id_no = ? " 
          + commSqlStr.rownum(1);
          
          sqlSelect(sql2, new Object[] {lsIdno});
        }
      } else if (eqIgno(wp.colStr(ii, "debit_flag"), "Y")) {
        if (!empty(lsCardNo)) {
        	
          String sql3 = " select A.chi_name , A.sex " 
              + " from dbc_idno A , dbc_card B "
              + " where B.id_p_seqno = A.id_p_seqno " 
              + " and B.card_no = ? ";
          
          sqlSelect(sql3, new Object[] {lsCardNo});
        } else if (!empty(lsIdno)) {
        	
          String sql4 =
              " select chi_name , sex " 
          + " from dbc_idno " 
          + " where id_no = ? " 
          + commSqlStr.rownum(1);
          
          sqlSelect(sql4, new Object[] {lsIdno});
        }
      }    
      
      if (sqlRowNum <= 0) {
    	  wp.colSet(ii, "db_sex", "");
    	  continue;
      }
      
      if (empty(wp.colStr(ii, "recv_cname")))
        wp.colSet(ii, "recv_cname", sqlStr("chi_name"));
      
      wp.colSet(ii, "db_sex", sqlStr("sex"));
    }

    for (int rr = 0; rr < wp.selectCnt; rr++) {
      wp.colSet(rr, "wk_cname_addr",
    		  
      wp.colStr(rr, "recv_cname") + "     " + wp.colStr(rr, "bill_zip") + "-"
              + wp.colStr(rr, "bill_addr1") + wp.colStr(rr, "bill_addr2")
              + wp.colStr(rr, "bill_addr3") + wp.colStr(rr, "bill_addr4")
              + wp.colStr(rr, "bill_addr5"));
    }

  }

  void printDataSet() {
    if (ibPrint1 == false)
      return;
    int ilSelectCnt = 0, ilForCnt = 0;
    ilSelectCnt = wp.selectCnt;
    if (ilSelectCnt % 18 == 0) {
      ilForCnt = ilSelectCnt / 18;
    } else {
      ilForCnt = (ilSelectCnt / 18) + 1;
    }
    int ll = -1, kk = 0;
    for (int ii = 0; ii < ilForCnt; ii++) {
      kk++;
      for (int aa = 1; aa <= 18; aa++) {
        ll++;
        wp.colSet(ii, "ex_zip" + aa, wp.colStr(ll, "bill_zip"));
        wp.colSet(ii, "ex_addr1" + aa, wp.colStr(ll, "bill_addr1") + wp.colStr(ll, "bill_addr2")
            + wp.colStr(ll, "bill_addr3"));
        wp.colSet(ii, "ex_addr2" + aa, wp.colStr(ll, "bill_addr4") + wp.colStr(ll, "bill_addr5"));
        wp.colSet(ii, "ex_name" + aa, wp.colStr(ll, "recv_cname"));
        wp.colSet(ii, "ex_sendnote" + aa, wp.colStr(ll, "sendnote"));
        if (eqIgno(wp.colStr(ll, "db_sex"), "1")) {
          wp.colSet(ii, "ex_sex" + aa, "先生　　收");
        } else if (eqIgno(wp.colStr(ll, "db_sex"), "2")) {
          wp.colSet(ii, "ex_sex" + aa, "小姐　　收");
        }
        if (ll == ilSelectCnt)
          continue;
      }
      if (ll == ilSelectCnt)
        continue;
    }
    wp.listCount[0] = kk;
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
    int llOk = 0;
    int llErr = 0;
    cmsr02.Cmsr0040Func func = new cmsr02.Cmsr0040Func();
    func.setConn(wp);

    String[] lsRowid = wp.itemBuff("rowid");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = lsRowid.length;

    for (int rr = 0; rr < lsRowid.length; rr++) {
      if (!this.checkBoxOptOn(rr, aaOpt))
        continue;
      func.varsSet("rowid", lsRowid[rr]);
      if (func.deleteData() == 1) {
        llOk++;
        wp.colSet(rr, "ok_flag", "V");
      } else {
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
      }
    }
    if (llOk > 0)
      sqlCommit(1);
    alertMsg("刪除成功 ; OK = " + llOk + " ERR = " + llErr);

  }



  @Override
  public void initButton() {
    btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {
    ibPrint1 = true;
    wp.reportId = "cmsr0040";
    wp.pageRows = 9999;
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.pageVert = true;
    pdf.excelTemplate = "cmsr0040-1.xlsx";
    pdf.pageCount = 1;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

  public void pdfPrint2() throws Exception {
    ibPrint2 = true;
    wp.reportId = "cmsr0040";
    wp.pageRows = 9999;
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    // pdf.pageVert = true;
    pdf.excelTemplate = "cmsr0040-2.xlsx";
    pdf.pageCount = 20;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
