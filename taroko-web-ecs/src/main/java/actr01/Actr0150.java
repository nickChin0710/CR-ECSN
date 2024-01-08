/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR     DESCRIPTION                                *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-11-29  V1.00.00  Max Lin    program initial                            *
 * 107-03-06  V1.00.01  Andy       Update SQL Error Debug                     *
 * 107-07-19  V1.00.02  Alex       bug fixed                                  *
 * 109-04-15	 V1.00.03  Alex      add auth_query						                  *
 * 109-06-17	 V1.00.04  Andy      update : Mantis3639					              *
 * 109-07-21  V1.00.08  Andy       update:Mantis3795                          *
 * 111/10/24  V1.00.09  jiangyigndong  updated for project coding standard    *
 * 112/07/12  V1.00.10  Simon      1.control getWhereStr():sqlCol(x,x,x) parms resetting*
 *                                 2.remove ColFunc()                         *
 *                                 3.取消卡友選項                             *
 ******************************************************************************/
package actr01;

import busi.func.ColFunc;
import ofcapp.BaseAction;

public class Actr0150 extends BaseAction {

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

    if(chkStrend(lsSDate, lsEDate)==false){
      alertErr2("提出/扣款日期 : 起迄錯誤");
      return -1;
    }

/***
    ColFunc func =new ColFunc();
    func.setConn(wp);
    if (func.fAuthQuery(wp.modPgm(), wp.itemStr2("ex_id"))!=1) {
      alertErr2(func.getMsg());
      return -1;
    }
***/

    String lsWhere = " where 1=1 "
            + " and A.p_seqno = B.acno_p_seqno "
            + " and A.err_type = '2' "
            +sqlCol(lsSDate,"A.enter_acct_date",">=")
            +sqlCol(lsEDate,"A.enter_acct_date","<=")
            ;
    //if(!wp.item_empty("ex_id")){
    //	ls_where += " and A.id_p_seqno in (select C.id_p_seqno from crd_idno C where 1=1"+zzsql.col(wp.item_ss("ex_id"),"C.id_no")+" )";
    //}

    if(!wp.itemEmpty("ex_id")){
      String lsAcctKey = "";
      lsAcctKey = commString.acctKey(wp.itemStr2("ex_id"));
      if(lsAcctKey.length()!=11){
        alertErr2("帳戶ID輸入錯誤");
        return -1;
      }
      lsWhere += sqlCol(lsAcctKey,"B.acct_key");
    }

    if(wp.itemEmpty("ex_autopay_acct_no") == false){
      lsWhere += sqlCol(wp.itemStr2("ex_autopay_acct_no"),"a.autopay_acct_no","%like%");
    }
    if(!empty(lsBankNo)){
      if(eqIgno(commString.mid(lsBankNo, 0, 3),"700")){
        lsWhere += " and A.acct_bank like '700%' ";
      }	else	{
        lsWhere += sqlCol(lsBankNo,"A.acct_bank");
      }
    }


    if(wp.itemEq("ex_prt", "0")){
      lsWhere += " and A.status_code > '01' ";
    }	else	{
      lsWhere += " and A.status_code > '00' ";
    }

/***
    if(wp.itemEq("ex_vip", "N")){
      lsWhere += " and B.vip_code  ='' ";
    }	else if (wp.itemEq("ex_vip", "Y")){
      lsWhere += " and B.vip_code  <>'' ";
    }	else if (wp.itemEq("ex_vip", "T")){
      lsWhere += " and exists ( select 1 from crd_card where p_seqno = B.acno_p_seqno and card_note='I' and current_code ='0') ";
    }
***/

  //sum(lsWhere);
  //sumAmt(lsWhere);
    wp.whereStr = lsWhere;
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {

    getWhereStr();
    sumAmt(wp.whereStr);

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();

  }

/***
  void sum(String lsWhere) throws Exception{
    String sql1 = " select "
            + " count(*) as db_cnt "
            + " from act_b002r1 A , act_acno B "
            +lsWhere
            ;

    sqlSelect(sql1);

    wp.colSet("error_cnt",""+sqlNum("db_cnt"));
  }
***/

  void sumAmt(String lsWhere) throws Exception {
    String sql1 = " select "
            + " count(*) as db_cnt, "
            + " SUM(A.transaction_amt) as sum_transaction_amt "
            + " from act_b002r1 A , act_acno B "
            +lsWhere
            ;

    sqlSelect(sql1);
    wp.colSet("error_cnt",""+sqlNum("db_cnt"));
    wp.colSet("sum_transaction_amt",""+sqlNum("sum_transaction_amt"));
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    if(getWhereStr()!=1)return;
    wp.selectSQL = ""
            + " A.acct_bank ,"
            + " B.acct_type ,"
            + " B.acct_key ,"
            + " A.print_date ,"
            + " A.enter_acct_date ,"
            + " A.autopay_acct_no ,"
            + " A.from_mark ,"
            + " B.acct_type||'-'||B.acct_key as wk_acct_key ,"
            + " A.transaction_amt ,"
            + " A.autopay_id ,"
            + " A.autopay_id_code ,"
            + " A.status_code ,"
            + " A.err_type ,"
            + " A.create_date ,"
            + " (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = A.id_p_seqno) as id_no, "
            + " A.chi_name ,"
            + " A.acct_type ,"
            + " A.stmt_cycle ,"
            + " B.vip_code ,"
            + " A.cellphone_no , "
            //+ " (select bank_name from act_ach_bank where bank_no = A.acct_bank) as tt_acct_bank , "
            //+ " (select bank_name from act_ach_bank where bank_no like A.acct_bank || '%' ) as tt_acct_bank , "
            //A.acct_bank同時有排序(order by),在此select like 執行會變慢，改在list_wkdata()讀取act_ach_bank
            + " decode(B.vip_code,'','','*') as vip "
    ;
    wp.daoTable = " act_b002r1 A , act_acno B ";
    wp.whereOrder = " order by A.acct_bank , A.enter_acct_date , A.transaction_amt ";
    pageQuery();

    if(sqlNotFind()){
      alertErr2("此條件查無資料");
      wp.colSet("error_cnt", ""+0);
      return ;
    }
    wp.setListCount(0);
    wp.setPageValue();
    listWkdata();

  }
  void listWkdata() throws Exception {
    int selCt = wp.selectCnt;
    String lsSql="",wkStatusCode = "",s1="",s2="", lsSqlA="",wkBankNo="";

    for (int ii = 0; ii < selCt; ii++) {

      wp.colSet(ii, "wk_send_flag", "N");
      s1 =wp.colStr(ii, "cellphone_no");
      if (s1.length()==10)  {
        s2 =s1.trim().replaceAll(",","");
        if ( s2.length()==10 && commString.isNumber(s2) == true ) {
          wp.colSet(ii, "wk_send_flag", "Y");
        }
      }

      // status_code退件原因中文
      wkStatusCode = wp.colStr(ii, "status_code");
      //20200617 改用ptr_sys_idtab參數對照
//			String[] cde = new String[] { "01", "02", "03", "04", "05","06","07","22",
//										"23","24","25","99"};
//			String[] txt = new String[] { "01:存款不足",
//											"02:非委託用戶",
//											"03:已終止委託用戶",
//											"04:無此帳號",
//											"05:收受者統編錯誤",
//											"06:無此用戶號碼",
//											"07:用戶號碼不符",
//											"22:帳戶已結清",
//											"23:靜止戶",
//											"24:凍結戶",
//											"25:帳戶存款遭法院強制執行",
//											"99:其他"};
//			wp.col_set(ii, "status_code", zzstr.decode(ss, cde, txt));
      lsSql = "select wf_desc "
              + "from ptr_sys_idtab "
              + "where wf_type = 'ACT_STATUS' "
              + "and wf_id =:parm_wf_id ";
      setString("parm_wf_id",wkStatusCode);
      sqlSelect(lsSql);
      if(sqlRowNum > 0){
        wp.colSet(ii, "status_code",sqlStr("wf_desc"));
      }

      wkBankNo = wp.colStr(ii, "acct_bank");
      lsSqlA = "select bank_name "
              + "from act_ach_bank "
              + "where bank_no like :parm_bank_no || '%' ";
      setString("parm_bank_no",wkBankNo);
      sqlSelect(lsSqlA);
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
