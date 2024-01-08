package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu  updated for project coding standard      *
*  109-08-26  V1.00.02  tanwei      modify                                   *
*  109-01-04  V1.00.03   shiyuqi       修改无意义命名
*  110-01-05  V1.00.04  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *                                                                                       *  
*/
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Ccar0300 extends BaseAction implements InfacePdf {
  String hhIdPseqno = "", hhIdPseqno2 = "";
  int liAcctIdx = 0;
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
    try {
      if (eqIgno(wp.respHtml, "ccar0300")) {
        wp.optionKey = wp.colStr(0, "ex_adj_reason");
        dddwList("dddw_adj_reason", "cca_sys_parm3", "sys_key", "sys_data1",
            "where SYS_ID='ADJREASON'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("臨調日期起迄：輸入錯誤");
      return;
    }
    String lsWhere =
        " where 1=1 and A.apr_user <> 'ccam2120' and A.adj_reason <> '02' and A.mod_type <>'0'  "
            + sqlCol(wp.itemStr("ex_date1"), "A.adj_date", ">=")
            + sqlCol(wp.itemStr("ex_date2"), "A.adj_date", "<=")
            + sqlCol(wp.itemStr("ex_user_id"), "A.adj_user", "like%")
            + sqlCol(wp.itemStr("ex_adj_reason"), "A.adj_reason");

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

    wp.selectSQL = "" + " A.apr_user , " + " uf_acno_key_idx(A.card_acct_idx) as wk_acct_key , "
        + " A.adj_date , " + " A.adj_time , " + " uf_acno_name_idx(A.card_acct_idx) as acno_cname ,"
        + " A.rela_flag , " + " A.lmt_tot_consume , "
        + " (A.lmt_tot_consume * A.tot_amt_month / 100) as wk_adj_limit , " + " A.tot_amt_month , "
        + " (A.lmt_tot_consume * A.adj_inst_pct / 100) as wk_inst_tot , " + " A.adj_inst_pct , "
        + " A.adj_eff_date1 , " + " A.adj_eff_date2 , " + " A.adj_reason , " + " A.adj_remark , "
        + " substr(A.adj_remark,1,14) as adj_remark_15 , " + " A.adj_area , "
        + " decode(A.adj_user,'ECS050','固額調整','ECS100','固額調整',A.adj_user) as adj_user , "
        + " A.card_acct_idx , "
        // + " p_seqno , "
        + " A.debit_flag , "
        + " (select sys_data1 from cca_sys_parm3 where sys_id = 'ADJREASON' and sys_key = A.adj_reason ) as tt_adj_reason , "
        + " B.acno_p_seqno , B.acct_type "

    ;
    wp.daoTable =
        "cca_limit_adj_log A left join cca_card_acct B on A.card_acct_idx = B.card_acct_idx ";
    wp.whereOrder = "  order by 1 , 2 ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    logSql();
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
    int liMonthCnt = 0;
    String sql1 = "select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      liMonthCnt = 0;
      liMonthCnt =
          commDate.monthsBetween(wp.colStr(ii, "adj_eff_date2"), wp.colStr(ii, "adj_eff_date1"));      
      if (liMonthCnt >= 3) {
        wp.colSet(ii, "wk_adj_eff_date", commString.strToYmd(wp.colStr(ii, "adj_eff_date1")) + "-"
            + commString.strToYmd(wp.colStr(ii, "adj_eff_date2")) + " *");
      } else {
        wp.colSet(ii, "wk_adj_eff_date", commString.strToYmd(wp.colStr(ii, "adj_eff_date1")) + "-"
            + commString.strToYmd(wp.colStr(ii, "adj_eff_date2")));
      }

      wp.colSet(ii, "wk_adj_reason",
          wp.colStr(ii, "adj_reason") + "_" + wp.colStr(ii, "tt_adj_reason"));
      wp.colSet(ii, "hh_name", commString.hideIdnoName(wp.colStr(ii, "acno_cname")));
      if (wp.colStr(ii, "wk_acct_key").length() < 2)
        continue;
      wp.colSet(ii, "hh_acct_key", wp.colStr(ii, "wk_acct_key").substring(0, 2) + "-"
          + commString.hideIdno(wp.colStr(ii, "wk_acct_key").substring(3)));
      
      if(wp.colEq(ii,"acct_type", "03") || wp.colEq(ii,"acct_type", "06")) {
    	  sqlSelect(sql1,new Object[] {wp.colStr(ii,"acno_p_seqno")});
    	  if (sqlRowNum > 0) {    		  
    		  wp.colSet(ii, "card_no", sqlStr("card_no"));
    	  }
      }
      
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
    wp.reportId = "Ccar0300";
    String cond1 = "臨調日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2"));
    wp.colSet("cond1", cond1);
    wp.pageRows = 9999;
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ccar0300.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.fullWidth = 100;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

}