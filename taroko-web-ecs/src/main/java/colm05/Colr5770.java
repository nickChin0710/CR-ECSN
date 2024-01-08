/** 連動強停/凍結明細表
 * 19-1202:   Alex  fix order by
 * 19-1129:   Alex  連動日期起 不可空白
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 109-05-06  V1.00.02  Tanwei       updated for project coding standard
 ** 109-01-04  V1.00.03   shiyuqi       修改无意义命名 
* 110-01-05  V1.00.04  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *  *
* 110-10-25  V1.00.04  machao     SQL Injection  
 * */
package colm05;

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Colr5770 extends BaseQuery implements InfacePdf {

  taroko.base.CommDate commDate = new taroko.base.CommDate();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) { // -資料讀取-
      strAction = "R";
      // dataRead();
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
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }
    dddwSelect();
    initButton();

  }


  @Override
  public void queryFunc() throws Exception {
    if (wp.itemEmpty("ex_date1")) {
      alertErr("連動日期(起)不可空白");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("連動日期起迄：輸入錯誤");
      return;
    }


    wp.whereStr = " where A.kind_flag ='A' and A.log_mode='2' "
        + sqlCol(wp.itemStr("ex_date1"), "A.log_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "A.log_date", "<=");

    if (wp.itemEq("ex_relate_code", "S")) {
      wp.whereStr +=
          " and A.log_type='2' and A.log_reason='S' and A.log_not_reason='' and A.relate_code='S' ";
    } else if (wp.itemEq("ex_relate_code", "B")) {
      wp.whereStr += " and A.log_type='3' and A.block_reason<>'' and A.relate_code='B' ";
    }

    if (!wp.itemEmpty("ex_idno")) {
//      wp.whereStr += " and A.id_p_seqno in (select id_p_seqno from crd_idno where id_no ='"
//          + wp.itemStr("ex_idno") + "')";
    	wp.whereStr += "and A.id_p_seqno in (select id_p_seqno from crd_idno where id_no =:idNo)";
    	setString ("idNo",wp.itemStr("ex_idno"));
    }

    wp.whereStr += " and A.acno_p_seqno<>A.rela_p_seqno and A.rela_p_seqno<>'' ";



    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " A.acno_p_seqno ,    " + " A.acct_type , "
        + "uf_acno_key(A.acno_p_seqno) as acct_key ,  " + " A.id_p_seqno ,  " + " A.log_date ,  "
        + " A.log_reason ,  " + " A.log_not_reason ,  " + " A.relate_code ,  " + " A.rela_p_seqno,"
        + " B.acct_type as db_source_acct_type," + " B.acct_key as db_source_acct_key,"
        + " uf_idno_name(A.id_p_seqno) as chi_name   ";
    wp.daoTable = "rsk_acnolog A join act_acno B on A.rela_p_seqno=B.acno_p_seqno ";
    wp.whereOrder = " order by A.log_date, A.acct_type ";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    // idRead();
    // balRead();
    listWkdata();
    wp.setListCount(1);
    // wp.totalRows = wp.dataCnt;
    wp.setPageValue();
  }

  void listWkdata() {
    String sql1 = " select " + " acct_jrnl_bal " + " from act_acct " + " where p_seqno = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_source_acct",
          wp.colStr(ii, "db_source_acct_type") + "-" + wp.colStr(ii, "db_source_acct_key"));
      wp.colSet(ii, "wk_acct", wp.colStr(ii, "acct_type") + "-" + wp.colStr(ii, "acct_key"));

      wp.logSql = false;
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "acno_p_seqno")});
      if (sqlRowNum <= 0)
        continue;
      wp.colSet(ii, "acct_jrnl_bal", sqlStr("acct_jrnl_bal"));
    }
  }

  // void idRead(){
  // String sql1 = " select "
  // + " chi_name "
  // + " from crd_idno "
  // + " where id_p_seqno = ? "
  // ;
  // for(int ii=0 ;ii<wp.selectCnt;ii++){
  // sqlSelect(sql1,new Object[]{wp.col_ss(ii,"id_p_seqno")});
  // if(sql_nrow<=0) continue;
  // wp.col_set(ii,"chi_name", sql_ss("chi_name"));
  // }
  // }

  // void balRead(){
  // String sql1 = " select "
  // + " acct_jrnl_bal "
  // + " from act_acct "
  // + " where p_seqno = ? "
  // ;
  // for(int ii=0 ;ii<wp.selectCnt ; ii++){
  // sqlSelect(sql1,new Object[]{wp.col_ss(ii,"acno_p_seqno")});
  // if(sql_nrow<=0) continue;
  // wp.col_set(ii,"acct_jrnl_bal", sql_ss("acct_jrnl_bal"));
  // }
  // }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }


  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "colr5770";
    String cond1 = "連動強停成功/連動凍結成功  連動日期: " + commDate.dspDate(wp.itemStr("ex_date1")) + " -- "
        + commDate.dspDate(wp.itemStr("ex_date2"));
    wp.colSet("cond_1", cond1);
    wp.colSet("user_id", wp.loginUser);
    wp.pageRows = 9999;
    queryFunc();

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "colr5770.xlsx";
    pdf.pageCount = 35;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

}
