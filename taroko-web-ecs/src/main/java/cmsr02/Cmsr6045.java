package cmsr02;
/** 
 * 19-1206:   Alex  fix case_desc , add tt_mail_type
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 ** 109-04-28   shiyuqi       updated for project coding standard     * 
 * */
import ofcapp.BaseAction;

public class Cmsr6045 extends BaseAction {

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
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_cms_date_s"), wp.itemStr("ex_cms_date_e")) == false) {
      alertErr2("統計期間起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where ( A.case_date = B.case_date ) and ( A.case_seqno = B.case_seqno ) "
        + sqlBetween("ex_cms_date_s", "ex_cms_date_e", "A.case_date")
        + sqlCol(wp.itemStr("ex_casetype"), "B.proc_deptno");

    if (empty(wp.itemStr("ex_card_no")) == false) {
      if ((wp.itemStr("ex_card_no").length() == 15) || (wp.itemStr("ex_card_no").length() == 16)) {
        lsWhere += sqlCol(wp.itemStr2("ex_card_no"), "A.card_no");
      } else if (wp.itemStr("ex_card_no").length() == 10) {
        lsWhere += sqlCol(wp.itemStr2("ex_card_no"), "A.case_idno");
      } else {
        errmsg("卡號/身分證字號:輸入錯誤");
        return;
      }
    }

    if (wp.itemEq("ex_send_type", "0") == false) {
      lsWhere += sqlCol(wp.itemStr2("ex_send_type"), "B.mail_send_type");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "A.card_no ," + "A.case_type ," + "A.case_idno ," + "A.case_desc ,"
        + "A.case_user ," + "A.case_date ," + "B.case_seqno ," + "B.proc_deptno ,"
        + "B.print_date ," + "B.bill_sending_zip,"
        + "B.bill_sending_addr1 || B.bill_sending_addr2 || B.bill_sending_addr3 || B.bill_sending_addr4 || B.bill_sending_addr5 as addr,"
        + "B.proc_desc ," + "B.result_flag ,"
        // +"decode(A.case_idno,'',uf_idno_name(A.card_no),uf_idno_name(A.case_idno)) as chi_name,"
        + "A.debit_flag ," + "B.recv_cname ," + "B.case_sale_id ," + "B.mail_send_type , "
        + "(select case_desc from cms_casetype where case_type||case_id = B.proc_deptno) as tt_case_desc , "
        + "decode(B.mail_send_type,'1','掛號','2','限專','3','平信') as tt_mail_type ";
    wp.daoTable = "cms_casemaster A,cms_casepost B  ";
    wp.whereOrder = " order by A.card_no ";
    logSql();
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wkAddr();
    wp.setListCount(1);
    wp.setPageValue();
  }

  void wkAddr() {
    String sql1 =
        " select B.chi_name from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where A.card_no = ? ";
    String sql2 = " select chi_name from crd_idno where id_no = ? ";
    for (int i = 0; i < wp.selectCnt; i++) {
      if (empty(wp.colStr(i, "bill_sending_zip"))) {
        wp.colSet(i, "wk_addr", wp.colStr("addr"));
      } else {
        wp.colSet(i, "wk_addr", wp.colStr(i, "bill_sending_zip") + " " + wp.colStr(i, "addr"));
      }

      if (wp.colEmpty(i, "card_no") == false) {
        sqlSelect(sql1, new Object[] {wp.colStr(i, "card_no")});
      } else if (wp.colEmpty(i, "case_idno") == false) {
        sqlSelect(sql2, new Object[] {wp.colStr(i, "case_idno")});
      }

      if (sqlRowNum > 0) {
        wp.colSet(i, "chi_name", sqlStr("chi_name"));
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
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "Cmsr6045")) {
        wp.optionKey = wp.colStr("ex_casetype");
        dddwList("d_dddw_casetype", "CMS_CASETYPE", "case_type||case_id", "case_desc",
            "where 1=1 and apr_flag ='Y' and case_type in ('A','B','C','D') ");
      }

    } catch (Exception ex) {
    }

  }

}
