package cmsr02;
/** 
 * 112-08-21 Ryan        欄位調整
 * 109-07-20 JustinWu      add default_chk
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 109-04-28   shiyuqi       updated for project coding standard     * 
 * */
import ofcapp.BaseAction;

public class Cmsr4510 extends BaseAction {
  String lsWhere = "";
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
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  private  boolean getWhereStr() {
	    if (this.chkStrend(wp.itemStr("ex_oppo_date1"), wp.itemStr("ex_oppo_date2")) == false) {
	        alertErr2("掛失日期起迄：輸入錯誤");
	        return false;
	      }

	       lsWhere = " where A.current_code = '2' "
	          + sqlCol(wp.itemStr("ex_oppo_date1"), "A.oppost_date", ">=")
	          + sqlCol(wp.itemStr("ex_oppo_date2"), "A.oppost_date", "<=")
	          + sqlCol(wp.itemStr("ex_card_no"), "A.card_no") + sqlCol(wp.itemStr("ex_idno"), "C.id_no");

	      if (!empty(wp.itemStr("ex_lost_fee"))) {
	        lsWhere += " and uf_nvl(lost_fee_code,'N') = '" + wp.itemStr("ex_lost_fee") + "'";
	      }
	      return true;
  }
  
  @Override
  public void queryFunc() throws Exception {
	if(getWhereStr()==false) {
		return;
	}
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " " + " A.card_no ," + " A.id_p_seqno ," + " A.sup_flag ,"
        + " A.major_id_p_seqno ," + " A.major_card_no ," + " A.current_code ," + " A.issue_date ,"
        + " A.oppost_reason ," + " A.oppost_date ," + " A.p_seqno ,"
        + " uf_nvl(A.lost_fee_code,'N') as lost_fee_code ," + " B.line_of_credit_amt ,"
        + " C.id_no ," + " C.job_position ," + " C.company_name ";
    if (wp.itemEq("ex_debit_flag", "N")) {
      wp.daoTable = "crd_card A join act_acno B on B.acno_p_seqno=A.acno_p_seqno"
          + " join crd_idno C on C.id_p_seqno=A.id_p_seqno";
    } else if (wp.itemEq("ex_debit_flag", "Y")) {
      wp.daoTable = "dbc_card A join dba_acno B on B.p_seqno=A.p_seqno"
          + " join dbc_idno C on C.id_p_seqno=A.id_p_seqno";
    }
    wp.whereOrder = " order by card_no Asc ";

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

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (this.eqIgno(wp.colStr(ii, "sup_flag"), "0")) {
        wp.colSet(ii, "sup_flag", "0.正卡");
      } else if (this.eqIgno(wp.colStr(ii, "sup_flag"), "1")) {
        wp.colSet(ii, "sup_flag", "1.附卡");
      }
      

      /*
       * selectData(wp.col_ss(ii,"id_p_seqno")); wp.col_set(ii,"line_of_credit_amt",
       * sql_ss("line_of_credit_amt")); wp.col_set(ii,"job_position", sql_ss("job_position"));
       * wp.col_set(ii,"company_name", sql_ss("company_name"));
       */
    }
  }

  void selectData(String lsIdPSeqno) {
    String sql1 = "select B.line_of_credit_amt , " + " C.job_position , " + " C.company_name "
        + " from dba_acno B join dbc_idno C on B.id_p_senqo=C.id_p_seqno "
        + " where C.id_p_seqno =?";
    sqlSelect(sql1, new Object[] {lsIdPSeqno});
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
    wp.colSet("default_chk", "checked");

  }

}
