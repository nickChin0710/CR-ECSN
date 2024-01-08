/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-08-07  V1.00.00  yash       program initial                            *
* 107-0927	JH		modify                                                   *
* 107-1003	 V1.00.02  yash       修改扣款次數                                                                 *
* 107-1101	 V1.00.03  yash       修改報表顯示條件                                                          *
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*
* 109-12-23  V1.00.03  Justin       parameterize sql
******************************************************************************/
package dbar01;

import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Dbar0020 extends ofcapp.BaseAction implements InfacePdf {
  String idno = "";
  private String idpSeqno = "";

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
    if (wp.itemEmpty("ex_acctno") && wp.itemEmpty("ex_id")) {
      alertErr2("請輸入 金融卡卡號  或 身分證ID ");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("日期起迄：輸入錯誤");
      return;
    }

    idno = wp.itemStr2("ex_id");
    // 1101補入
    if (!empty(idno)) {
      String sql2 = " select id_p_seqno from dbc_idno where id_no=? " + commSqlStr.rownum(1);
      setString2(1, wp.itemStr2("ex_id"));
      sqlSelect(sql2);
      idpSeqno = sqlStr("id_p_seqno");
    }

    if (empty(idno)) {
      String sql1 = "select id_p_seqno, uf_idno_id2(id_p_seqno,'Y') as id_no"
          + " from dbc_card where acct_no=?" + commSqlStr.rownum(1);
      setString2(1, wp.itemStr2("ex_acctno"));
      sqlSelect(sql1);
      if (sqlRowNum <= 0) {
        alertErr2("金融卡卡號: 未申請卡片");
        return;
      }
      idno = sqlStr("id_no");

      idpSeqno = sqlStr("id_p_seqno");
    }
    if (empty(idno)) {
      alertErr2("無法取得: 卡人身分證ID");
      return;
    }
    if (this.logQueryIdno("Y", idno) == false) {
      return;
    }

    String lsWhere = "where 1=1" + sqlCol(idno, "B.id_no")
        + sqlStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2"), "A.deduct_date");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
    queryAfter();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "A.id_no as id," + " A.acct_no ," + " A.deduct_date ," + " A.deduct_seq ,"
        + " A.card_no ," + " A.merchant_no ," + " A.reference_no ," + " A.deduct_amt ,"
        + " A.beg_bal ," + " A.id_p_seqno ," + " uf_nvl(B.chi_name,B.eng_name) as chi_name ,"
        + " A.purchase_date ," + " A.p_seqno ," + " A.acct_type ,"
        + " uf_nvl(replace(C.mcht_chi_name,'　',''),C.mcht_eng_name) as db_mcht_cname,"
        + " (ROW_NUMBER() OVER(PARTITION BY a.reference_no )) as db_deduct_cnt";
    wp.daoTable = "dba_deduct_txn A left join dbc_idno B on B.id_p_seqno=A.id_p_seqno"
        + " left join dbb_bill C on C.reference_no=A.reference_no ";
    wp.whereOrder = "order by A.reference_no, A.deduct_date, B.id_no, A.card_no";

    pageQuery();
    wp.listCount[0] = sqlRowNum;
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
  }

  void queryAfter() {
    wp.sqlCmd = "select sum(nvl(end_bal,0)) as db_end_bal" + " FROM dba_debt"
        + " WHERE card_no in (select card_no from dbc_card where id_p_seqno =?)";
    setString2(1, idpSeqno);
    sqlSelect();
    wp.colSet("db_end_bal", sqlNum("db_end_bal"));
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
    String cond1 = "";
    wp.reportId = "dbar0020";
    wp.pageRows = 9999;
    if (!empty(wp.itemStr("ex_acctno"))) {
      cond1 += " 金融卡帳號： " + wp.itemStr("ex_acctno") + " ";
    }
    if (!empty(wp.itemStr("ex_id"))) {
      cond1 += "  身分證ID：" + wp.itemStr("ex_id") + " ";
    }
    if (!empty(wp.itemStr("ex_date1")) || !empty(wp.itemStr("ex_date2"))) {
      cond1 += "  扣款日期：" + wp.itemStr("ex_date1") + " -- " + wp.itemStr("ex_date2");
    }
    wp.colSet("cond_1", cond1);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "dbar0020.xlsx";
    pdf.pageCount = 28;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
