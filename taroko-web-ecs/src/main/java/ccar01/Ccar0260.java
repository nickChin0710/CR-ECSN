package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*  109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
*/

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Ccar0260 extends BaseAction implements InfacePdf {
  String hhIdPseqno = "", hhIdPseqno2 = "", lsPSeqno = "", lsCardAcctIdx = "",
      hhCorpPSeqno = "";

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
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("異動日期起迄：輸入錯誤");
      return;
    }

    if (wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_date2") && wp.itemEmpty("ex_idno") && wp.itemEmpty("ex_card_no")) {
      alertErr2("異動日期  , 身分證號 , 卡號 不可皆為空白 ");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "A.tx_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "A.tx_date", "<=");

    if (!empty(wp.itemStr("ex_idno"))) {
      lsWhere += " and B.id_p_seqno in " + " (select id_p_seqno from crd_idno where 1=1 "
          + sqlCol(wp.itemStr2("ex_idno"), "id_no") + " union "
          + " select id_p_seqno from dbc_idno where 1=1 " + sqlCol(wp.itemStr2("ex_idno"), "id_no")
          + " ) ";
    }
    
    if (wp.itemEmpty("ex_card_no") == false ) {
    	lsWhere += " and B.acno_p_seqno in (select acno_p_seqno from crd_card where 1=1 "
    			+ sqlCol(wp.itemStr("ex_card_no"),"card_no")
    			+ " union all "
    			+ " select p_seqno as acno_p_seqno from dbc_card where 1=1 "
    			+ sqlCol(wp.itemStr("ex_card_no"),"card_no")
    			+ " ) ";
    }
    
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " A.tx_date , " + " A.tx_time , " + " A.card_acct_idx , " + " A.acct_type , "
        + " A.org_credit_amt , " + " A.line_credit_amt , " + " 0 as org_adj_amt , "
        + " 0 as acno_credit_amt , " + " '' as acno_name , " + " A.adj_quota , "
        + " A.adj_eff_start_date , " + " A.adj_eff_end_date , " + " A.adj_reason , "
        + " A.org_amt_month," + " A.card_acct_idx , " + " A.tot_amt_month ";
    wp.daoTable =
        "cca_credit_log A left join cca_card_acct B on A.card_acct_idx = B.card_acct_idx ";
    wp.whereOrder = "  order by A.tx_date, A.tx_time ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    
    pageQuery();
    
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
    wp.setListCount(1);
    queryAfter();    
  }

  void queryAfter() {
	  String sql1 = "select debit_flag, acno_flag, acno_p_seqno " + " from cca_card_acct "+ " where 1=1 " + " and card_acct_idx = ? ";
	  String sql2 = "select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      
      sqlSelect(sql1, new Object[] {wp.colNum(ii, "card_acct_idx")});
      lsPSeqno = sqlStr("acno_p_seqno");
      if (this.eqAny(sqlStr("debit_flag"), "Y")) {
        selectDbaAcno(lsPSeqno, ii);
      } else if (this.eqAny("acno_flag", "2")) {
        selectActCorpGp(lsPSeqno, ii);
      } else {
        selectActAcno(lsPSeqno, ii);
      }

      wp.colSet(ii, "wk_acct", wp.colStr(ii, "acct_type") + "-" + wp.colStr(ii, "acct_key"));
      wp.colSet(ii, "hh_acct",
          wp.colStr(ii, "acct_type") + "-" + commString.hideIdno(wp.colStr(ii, "acct_key")));
      wp.colSet(ii, "wk_adj_eff_date", commString.strToYmd(wp.colStr(ii, "adj_eff_start_date")) + " -- "
          + commString.strToYmd(wp.colStr(ii, "adj_eff_end_date")));
      
      if(wp.colEq(ii,"acct_type", "03") || wp.colEq(ii,"acct_type", "06")) {
    	  sqlSelect(sql2,new Object[] {lsPSeqno});
    	  if (sqlRowNum > 0) {    		  
    		  wp.colSet(ii, "card_no", sqlStr("card_no"));
    	  }
      }
      
    }
  }

  void selectDbaAcno(String pSeqno, int ii) {
    String sql1 = "select line_of_credit_amt as acno_credit_amt ,"
        + " uf_vd_acno_name(p_seqno) as acno_name ," + " acct_key ,"
        + " uf_vd_corp_no(corp_p_seqno) as corp_no " + " from dba_acno " + " where 1=1 "
        + " and p_seqno = ? ";
    sqlSelect(sql1, new Object[] {pSeqno});

    if (sqlRowNum <= 0)
      return;

    wp.colSet(ii, "corp_no", sqlStr("corp_no"));
    wp.colSet(ii, "acct_key", sqlStr("acct_key"));
    wp.colSet(ii, "acno_name", sqlStr("acno_name"));
    wp.colSet(ii, "hh_name", commString.hideIdnoName(sqlStr("acno_name")));
    wp.colSet(ii, "acno_credit_amt", sqlStr("acno_credit_amt"));

  }

  void selectActCorpGp(String pSeqno, int ii) {
    String sql1 =
        "select lmt_tot_consume as acno_credit_amt ," + " uf_idno_id(id_p_seqno) as acct_key ,"
            + " uf_acno_name(p_seqno) as acno_name ," + " uf_corp_no(corp_p_seqno) as corp_no "
            + " from act_corp_gp " + " where 1=1 " + " and gp_no = ? ";
    sqlSelect(sql1, new Object[] {pSeqno});

    if (sqlRowNum <= 0)
      return;

    wp.colSet(ii, "corp_no", sqlStr("corp_no"));
    wp.colSet(ii, "acct_key", sqlStr("acct_key"));
    wp.colSet(ii, "acno_name", sqlStr("acno_name"));
    wp.colSet(ii, "hh_name", commString.hideIdnoName(sqlStr("acno_name")));
    wp.colSet(ii, "acno_credit_amt", sqlStr("acno_credit_amt"));

  }

  void selectActAcno(String pSeqno, int ii) {
    String sql1 = "select line_of_credit_amt as acno_credit_amt ," + " acct_key ,"
        + " uf_acno_name(acno_p_seqno) as acno_name ," + " uf_corp_no(corp_p_seqno) as corp_no "
        + " from act_acno " + " where 1=1 " + " and acno_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {pSeqno});

    if (sqlRowNum <= 0)
      return;

    wp.colSet(ii, "corp_no", sqlStr("corp_no"));
    wp.colSet(ii, "acct_key", sqlStr("acct_key"));
    wp.colSet(ii, "acno_name", sqlStr("acno_name"));
    wp.colSet(ii, "hh_name", commString.hideIdnoName(sqlStr("acno_name")));
    wp.colSet(ii, "acno_credit_amt", sqlStr("acno_credit_amt"));

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
    wp.reportId = "Ccar0260";

    String cond1 = "異動日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2"));
    wp.colSet("cond1", cond1);
    wp.pageRows = 9999;
    queryFunc();

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ccar0260.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

}
