package tscm01;
/** 2019-0625:  JH    p_xxx >>acno_p_xxx
 * 109-04-28  V1.00.01  Tanwei       updated for project coding standard
 * * 110-01-06  V1.00.03  shiyuqi       修改无意义命名                                                                                     *    
 * 111-04-14  V1.00.01   yangqinkai     TSC畫面整合
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Tscr0060 extends BaseAction implements InfacePdf {
  String lsMaxDate = "";
  int ilListCnt = 0;

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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_date1")) || empty(wp.itemStr("ex_date2"))) {
      alertErr2("請款日期:不可空白");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("請款日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where A.online_mark ='0' "
        + sqlCol(wp.itemStr("ex_date1"), "A.batch_no", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "substr(A.batch_no,1,8)", "<=")
        + " and B.block_reason1||B.block_reason2||B.block_reason3||B.block_reason4||B.block_reason5 >''";

    wp.pageCountSql = "select count(*) from tsc_cgec_all A join Vcard_acno C on C.card_no=A.card_no"
        + " left join cca_card_acct B on B.acno_p_seqno=C.acno_p_seqno and B.debit_flag<>'Y'"
        + lsWhere;

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " " + " A.card_no , " + " A.tsc_card_no , " + " A.purchase_date , "
        + " A.crt_date , " + " uf_idno_id(A.card_no) as db_idno , "
        + " B.block_reason1, B.block_reason2, B.block_reason3, B.block_reason4, B.block_reason5 "
        + ", C.acno_p_seqno "
//        + ", C.line_of_credit_amt as db_limit_amt " 
        + ", uf_acno_endbal(C.p_seqno) as db_end_bal "
        + ", '' as db_proc_flag" + ", '' as del_flag ";
    wp.daoTable = "tsc_cgec_all A join Vcard_acno C on C.card_no=A.card_no "
        + " left join cca_card_acct B on B.acno_p_seqno=C.acno_p_seqno and B.debit_flag<>'Y'";

    wp.whereOrder = " order by A.purchase_date Asc , A.tsc_card_no Asc ";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);

    wp.setPageValue();

  }

  void queryAfter() {
    wp.logSql = false;
    String sql1 = "select auto_block_reason as ls_block_cond " + " from rsk_comm_parm "
        + " where parm_type='W_RSKM2230' " + commSqlStr.rownum(1);
    sqlSelect(sql1);
    String lsBlockCond = sqlStr("ls_block_cond");
    lsBlockCond = commString.spilt(lsBlockCond, 2, ",");

    sql1 = "select max(crt_date) as ls_max_date " + " from tsc_bkec_log ";
    sqlSelect(sql1);
    lsMaxDate = sqlStr("ls_max_date");

    String sql3 = "select proc_flag as db_proc_flag " + " from tsc_bkec_log "
        + " where crt_date =? " + " and tsc_card_no =? " + commSqlStr.rownum(1);
    
    String sql4 = "select line_of_credit_amt as db_limit_amt from act_acno where acno_p_seqno = ? ";
    
    String[] aaBlock = new String[5];
    String lsBlock = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql4,new Object[] {wp.colNum(ii,"acno_p_seqno")});
      if(sqlRowNum >0) {
    	  wp.colSet(ii, "db_limit_amt",sqlNum("db_limit_amt"));
      }    	
   
      aaBlock[0] = wp.colStr(ii, "block_reason1");
      aaBlock[1] = wp.colStr(ii, "block_reason2");
      aaBlock[2] = wp.colStr(ii, "block_reason3");
      aaBlock[3] = wp.colStr(ii, "block_reason4");
      aaBlock[4] = wp.colStr(ii, "block_reason5");
      lsBlock = "";
      for (int jj = 0; jj < 5; jj++) {
        if (empty(aaBlock[jj]))
          continue;
        lsBlock += aaBlock[jj] + ",";
        if (commString.strIn2(aaBlock[jj], lsBlockCond)) {
          wp.colSet(ii, "del_flag", "Y");
        }
      }
      wp.colSet(ii, "db_block_reason", lsBlock);
      if (eqIgno(wp.colStr(ii, "del_flag"), "Y"))
        continue;

      if (!empty(lsMaxDate)) {
        sqlSelect(sql3, new Object[] {lsMaxDate, wp.colStr(ii, "tsc_card_no")});
        if (sqlRowNum > 0) {
          wp.colSet(ii, "db_proc_flag", sqlStr("db_proc_flag"));
        }
      }
    }
  }

  void selectProcFlag(String tscCardNo, String maxDate) {
    String sql1 = "select proc_flag as db_proc_flag " + " from tsc_bkec_log "
        + " where crt_date =? " + " and tsc_card_no =? " + commSqlStr.rownum(1);
    // + " and A.p_seqno in (select p_seqno from crd_card where card_no = ?)";
    sqlSelect(sql1, new Object[] {maxDate, tscCardNo});

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
    wp.reportId = "Tscr0060";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "匯入日期:  " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2"));
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    
    if(sqlNotFind()) {
    	wp.respHtml = "TarokoErrorPDF";
    	return;
    }
    
    TarokoPDF pdf = new TarokoPDF();
    pdf.pageVert = false;

    wp.fileMode = "Y";
    pdf.excelTemplate = "tscr0060.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

    // wp.listCount[0] =il_list_cnt;
    return;
  }

  void dataPrint() {
    int ii = 0;

    String[] lsDelFlag = wp.itemBuff("del_flag");
    String[] lsPurchaseDate = wp.itemBuff("purchase_date");
    String[] lsDbIdno = wp.itemBuff("wk_id");
    String[] lsCardNo = wp.itemBuff("card_no");
    String[] lsTscCardNo = wp.itemBuff("tsc_card_no");
    String[] lsDbLimitAmt = wp.itemBuff("db_limit_amt");
    String[] lsDbEndBal = wp.itemBuff("db_end_bal");
    String[] lsDbBlockReason = wp.itemBuff("db_block_reason");
    String[] lsDbProcFlag = wp.itemBuff("db_proc_flag");

    ilListCnt = lsDelFlag.length;
    for (int rr = 0; rr < lsPurchaseDate.length; rr++) {
      if (eqIgno(lsDelFlag[rr], "Y")) {
        continue;
      }
      wp.colSet(ii, "tt_del_flag", lsDelFlag[rr]);
      wp.colSet(ii, "tt_purchase_date", lsPurchaseDate[rr]);
      wp.colSet(ii, "tt_idno", lsDbIdno[rr]);
      wp.colSet(ii, "tt_card_no", lsCardNo[rr]);
      wp.colSet(ii, "tt_tsc_card_no", lsTscCardNo[rr]);
      wp.colSet(ii, "tt_limit_amt", lsDbLimitAmt[rr]);
      wp.colSet(ii, "tt_end_bal", lsDbEndBal[rr]);
      wp.colSet(ii, "tt_block_reason", lsDbBlockReason[rr]);
      wp.colSet(ii, "tt_proc_flag", lsDbProcFlag[rr]);
      ii++;
    }
    wp.listCount[0] = ii;
  }

}
