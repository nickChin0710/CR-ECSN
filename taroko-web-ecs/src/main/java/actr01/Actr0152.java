/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR     DESCRIPTION                                *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-11-29  V1.00.00  Max Lin    program initial                            *
 * 109-06-17	 V1.00.01  Andy       update : Mantis3643					     *
 * 109-07-21  V1.00.08  Andy       update:Mantis3795                          *
 * 111/10/24  V1.00.09  jiangyigndong  updated for project coding standard    *
 * 112/07/12  V1.00.10  Simon      1.control getWhereStr():sqlCol(x,x,x) parms resetting*
 *                                 2.取消卡友選項                             *
 ******************************************************************************/
package actr01;

import ofcapp.BaseAction;

public class Actr0152 extends BaseAction {

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    }
    else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    }
    else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    }
    else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    }
    else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    }
    else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    }
    else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    }
    else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    }
    else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    }
    else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }
    else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      // 銀行代號
      wp.optionKey = wp.colStr("ex_bank_id");
      dddwList("dddw_bank_id", "act_ach_bank", "bank_no", "bank_name", "where 1=1 order by bank_no");
    } catch (Exception ex) {
    }

  }

  private int getWhereStr() throws Exception {
    String lsSDate = wp.itemStr2("ex_date_S");
    String lsEDate = wp.itemStr2("ex_date_E");
    String lsBankNo = wp.itemStr2("ex_bank_id");
//		if(empty(ls_s_date) || empty(ls_e_date)){
//			err_alert("提出/扣款日期  不可空白");
//			return -1;
//		}

    if(chkStrend(lsSDate, lsEDate)==false){
      alertErr2("提出/扣款日期 : 起迄錯誤");
      return -1;
    }
    StringBuffer  lsWhere  = new StringBuffer();
    lsWhere.append(" where 1=1 ");
    lsWhere.append(" and A.p_seqno = B.acno_p_seqno ");
    lsWhere.append(" and A.status_code = '00' ");
    lsWhere.append(sqlCol(lsSDate,"A.enter_acct_date",">="));
    lsWhere.append(sqlCol(lsEDate,"A.enter_acct_date","<="));


    //if(!wp.item_empty("ex_id")){
    //	ls_where.append(" and A.id_p_seqno in (select id_p_seqno from crd_idno where id_no ='");
    //	ls_where.append(wp.item_ss("ex_id"));
    //	ls_where.append("') ");
    //}

    if(!wp.itemEmpty("ex_id")){
      String lsAcctKey = "";
      lsAcctKey = commString.acctKey(wp.itemStr2("ex_id"));
      if(lsAcctKey.length()!=11){
        alertErr2("帳戶ID輸入錯誤");
        return -1;
      }
      lsWhere.append(" and B.acct_key ='");
      lsWhere.append(lsAcctKey);
      lsWhere.append("' ");
    }

    if(wp.itemEmpty("ex_autopay_acct_no") == false){
      lsWhere.append(sqlCol(wp.itemStr2("ex_autopay_acct_no"),"a.autopay_acct_no","%like%"));
    }
    if(!empty(lsBankNo)){
      if(eqIgno(commString.mid(lsBankNo, 0, 3),"700")){
        lsWhere.append(" and A.acct_bank like '700%' ");
      }	else	{
        lsWhere.append(sqlCol(lsBankNo,"A.acct_bank"));
      }
    }


/***
    if(wp.itemEq("ex_vip", "N")){
      lsWhere.append(" and B.vip_code  ='' ");
    }	else if (wp.itemEq("ex_vip", "Y")){
      lsWhere.append(" and B.vip_code  <>'' ");
    }	else if (wp.itemEq("ex_vip", "T")){
      lsWhere.append(" and exists ( select 1 from crd_card where p_seqno = B.acno_p_seqno and card_note='I' and current_code ='0') ");
    }
***/

    //sum(lsWhere.toString());
    //sumAmt(lsWhere.toString());
    // -page control-
    wp.whereStr = lsWhere.toString() ;

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {

    if(getWhereStr()!=1)return;
    //sum(wp.whereStr);
    sumAmt(wp.whereStr);
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();

  }

/***
  void sum(String lsWhere) throws Exception {
    String sql1 = " select "
            + " count(*) as db_cnt "
            + " from act_other_apay A , act_acno B "
            + lsWhere
            ;

    sqlSelect(sql1);

    wp.colSet("ok_cnt",""+sqlNum("db_cnt"));
  }
***/

  void sumAmt(String lsWhere) throws Exception {
    String sql1 = " select "
            + " count(*) as db_cnt, "
            + " SUM(A.transaction_amt) as sum_transaction_amt "
            + " from act_other_apay A , act_acno B "
            +lsWhere
            ;

    sqlSelect(sql1);
    wp.colSet("ok_cnt",""+sqlNum("db_cnt"));
    wp.colSet("sum_transaction_amt",""+sqlNum("sum_transaction_amt"));
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if(getWhereStr()!=1)return;

    wp.selectSQL = ""
            + " A.acct_bank ,"
            + " A.acct_type ,"
            + " B.acct_key ,"
            + " A.enter_acct_date ,"
            + " A.autopay_acct_no ,"
            + " A.from_mark ,"
            + " A.stmt_cycle ,"
            + " A.p_seqno ,"
            + " A.acct_type||'-'||B.acct_key as wk_acct_key ,"
            + " A.transaction_amt ,"
            + " A.autopay_id ,"
            + " A.autopay_id_code ,"
            + " A.status_code ,"
            + " A.crt_date ,"
            + " A.mod_user ,"
            + " A.mod_time ,"
            + " A.mod_pgm ,"
            + " A.mod_seqno ,"
//			 + " uf_idno_id(A.id_p_seqno) as id_no ,"
            + " (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = A.id_p_seqno) as id_no, "
            + " A.chi_name ,"
            + " B.vip_code ,"
            //+ " (select bank_name from act_ach_bank where bank_no = A.acct_bank) as tt_acct_bank , "
            //+ " (select bank_name from act_ach_bank where bank_no like A.acct_bank || '%' ) as tt_acct_bank , "
            //A.acct_bank同時有排序(order by),在此select like 執行會變很慢，改在list_wkdata()讀取act_ach_bank
            + " decode(B.vip_code,'','','*') as vip "
    ;
    wp.daoTable = " act_other_apay A , act_acno B ";
    wp.whereOrder = " order by A.acct_bank , A.enter_acct_date , A.transaction_amt ";
    //if(sql_num("db_cnt") <= 10000) {
    //	 wp.whereOrder += ", A.transaction_amt  ";
    //}

    pageQuery();

    if(sqlNotFind()){
      alertErr2("此條件查無資料");
      wp.colSet("ok_cnt", ""+0);
      return ;
    }
    wp.setListCount(0);
    wp.setPageValue();
    listWkdata();

  }

  void listWkdata() throws Exception {
    String lsSql="", wkBankNo="";
    lsSql = "select bank_name "
            + "from act_ach_bank "
            + "where bank_no like :parm_bank_no || '%' ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {

      wkBankNo = wp.colStr(ii, "acct_bank");
      setString("parm_bank_no",wkBankNo);
      sqlSelect(lsSql);
      if(sqlRowNum > 0){
        wp.colSet(ii, "tt_acct_bank",sqlStr("bank_name"));
      } else {
        wp.colSet(ii, "tt_acct_bank","");
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

}
