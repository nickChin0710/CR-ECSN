/* 自行自動扣繳成功明細表 V.2019-1218.Alex
 *
 * 2019-1218  V1.00.01  Alex  fix  A.p_seqno = B.acno_p_seqno
 * 2020-0618  V1.00.02  Andy  Update:Mantis3642
 * 2020-0721  V1.00.08  Andy  update:Mantis3795
 * 2022-1024  V1.00.09  jiangyigndong  updated for project coding standard
 * 112/05/24  V1.00.10  Simon      queryRead()排除queryFunc()重複執行 getWhereStr()*
 * 112/07/04  V1.00.11  Ryan      增加自行、花農選項，修改全部、扣足金額、扣不足金額選項
 * 112/09/01  V1.00.12  Ryan      調整報表where 條件
 * */
package actr01;

import ofcapp.BaseAction;


public class Actr0082 extends BaseAction {

  String hsWhere = "";
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
      // 雙幣幣別
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_curr_code");
      dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
              "where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id desc ");
    } catch (Exception ex) {
    }

  }

  private boolean getWhereStr() throws Exception {
//		if(wp.itemEmpty("ex_date_S") && wp.itemEmpty("ex_date_E")){
//			errmsg("IBM 扣帳日期 : 不可全部空白 !");
//			return ;
//		}
    String exAcctKey = wp.itemStr("ex_acct_key");

    if(chkStrend(wp.itemStr("ex_date_S"), wp.itemStr("ex_date_E"))==false){
      errmsg("IBM 扣帳日期 : 起迄錯誤 !");
      return false;
    }

    String lsWhere = " where A.status_code not in ('99') "
            + " and A.p_seqno = B.acno_p_seqno "
            + sqlCol(wp.itemStr("ex_date_S"),"A.enter_acct_date",">=")
            + sqlCol(wp.itemStr("ex_date_E"),"A.enter_acct_date","<=")
            + sqlCol(wp.itemStr("ex_curr_code"),"uf_nvl(A.curr_code,'901')")
            + sqlCol(wp.itemStr("ex_from_mark"),"A.from_mark")
            ;



    if(wp.itemEq("ex_ok_amt", "1")){
      lsWhere += " and A.TRANSACTION_AMT > 0 and A.ORI_TRANSACTION_AMT = A.TRANSACTION_AMT ";
    }	else if(wp.itemEq("ex_ok_amt", "2")){
      lsWhere += " and A.TRANSACTION_AMT > 0 and A.ORI_TRANSACTION_AMT > A.TRANSACTION_AMT ";
    } else {
      lsWhere += " and A.TRANSACTION_AMT > 0 ";
    }

    //if(!wp.itemEmpty("ex_id")){
    //	ls_where += " and A.id_p_seqno in (select C.id_p_seqno from crd_idno C where 1=1"+zzsql.col(wp.itemStr("ex_id"),"C.id_no")+" )";
    //}

    if (empty(exAcctKey) == false) {
      String lsAcctKey = fillZeroAcctKey(exAcctKey);
      lsWhere += " and B.acct_key =:ex_acct_key ";
      setString("ex_acct_key", lsAcctKey);
    }

    if(wp.itemEmpty("ex_autopay_acct_no") == false) {
      //ls_where += sqlCol(wp.itemStr("ex_autopay_acct_no"),"a.autopay_acct_no","%like%");
      lsWhere += " and (a.autopay_acct_no like :autopay_acct_no or a.dc_autopay_acct_no like :dc_autopay_acct_no) ";
      setString("autopay_acct_no"   , '%'+wp.itemStr("ex_autopay_acct_no")+'%');
      setString("dc_autopay_acct_no", '%'+wp.itemStr("ex_autopay_acct_no")+'%');
    }

    hsWhere = lsWhere;
    return true;

  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;

    listSum(hsWhere);//執行 pageQuery()或sqlSelect(sql) 後，setString 設定的參數會清掉，之後要用需再執行 setString
    getWhereStr();

    wp.whereStr = hsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    wp.totalRows =wp.colInt("tl_rows");
    queryRead();
  }

  void listSum(String lsWhere) throws Exception{
    String sql1 = " select "
            + " count(*) as tl_rows , "
            + " sum(A.ori_transaction_amt) as tl_ori_amt , "
            + " sum(uf_dc_amt(A.curr_code,A.transaction_amt,A.dc_transaction_amt)) as tl_amt "
            + " from act_chkautopay A, act_acno B "
            + lsWhere
            ;

    sqlSelect(sql1);

    wp.colSet("tl_rows", sqlStr("tl_rows"));
    wp.colSet("tl_ori_amt", sqlStr("tl_ori_amt"));
    wp.colSet("tl_amt", sqlStr("tl_amt"));
  }

  @Override
  public void queryRead() throws Exception {

    if (!strAction.equals("Q")) {
      if (getWhereStr() == false)
        return;
    }

    wp.pageControl();
    wp.selectSQL = ""
            + " A.enter_acct_date ,"
            + " A.p_seqno ,"
            + " A.acct_type ,"
            + " B.acct_key ,"
            //+ " (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = A.id_p_seqno) as id_no, "
            + "(case when b.acno_flag = '2' then substr(b.acct_key,1,8) "
            + " when b.acno_flag = '3' then (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = a.id_p_seqno) "
            + " else substr(b.acct_key,1,10)||'-'||substr(b.acct_key,11,1) end) as id_no, "
            //+ " A.chi_name ,"
            + "(case when b.acno_flag = '2' then uf_corp_name(b.corp_p_seqno) "
            + " else uf_idno_name(a.id_p_seqno) end) as chi_name, "
            + " A.from_mark ,"
            + " A.ori_transaction_amt ,"
            + " A.sms_add_date ,"
            + " uf_nvl(A.curr_code,'901') curr_code ,"
            + " A.autopay_id ,"
            //+ " A.autopay_acct_no ,"
            //+ " A.dc_autopay_acct_no ,"
            + "(case when A.curr_code in ('','901') then A.autopay_acct_no "
            + " else A.dc_autopay_acct_no end) as autopay_acct_no, "
            + " A.dc_autopay_id ,"
            + " uf_dc_amt(A.curr_code,A.transaction_amt,A.dc_transaction_amt) transaction_amt ,"
            + " B.vip_code ,"
            + " decode(B.vip_code,'','','*') as vip ,"
            + " A.act_no_l ,"
            + " A.amt_1 ,"
            + " A.curr_rate ,"
            + " A.deduct_amt_1 ,"
            + " A.deduct_amt_2 ,"
            + " A.mod_pgm "
    ;
    //wp.daoTable = " act_chkautopay A join act_acno B on A.p_seqno = B.acno_p_seqno ";
    wp.daoTable = " act_chkautopay A, act_acno B ";

    pageQuery();
    if(sqlNotFind()){
      alertErr("此條件查無資料");
      return ;
    }

    wp.setListCount(0);
    wp.setPageValue();
    listWkdata();

  }

  void listWkdata() throws Exception {
    double lsDoubleAmt = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      //if(wp.colStr(ii,"mod_pgm").equals("act_a105")) {
      if ( (wp.colStr(ii,"curr_code").equals("840") && wp.colNum(ii,"curr_rate") > 1000)  ||
              (wp.colStr(ii,"curr_code").equals("392") && wp.colNum(ii,"curr_rate") > 10)       )
      {
        lsDoubleAmt = wp.colNum(ii,"amt_1") * 100.0;
        lsDoubleAmt = convAmt(lsDoubleAmt);
        wp.colSet(ii, "amt_1", lsDoubleAmt);

        lsDoubleAmt = wp.colNum(ii,"curr_rate") / 100;
        lsDoubleAmt = convAmt(lsDoubleAmt);
        wp.colSet(ii, "curr_rate", lsDoubleAmt);
      }
    }
  }

  String fillZeroAcctKey(String acctkey) throws Exception {
    String rtn = acctkey;
    if (acctkey.trim().length() == 8)
      rtn += "000";
    if (acctkey.trim().length() == 10)
      rtn += "0";

    return rtn;
  }

  /***********************************************************************/
  public double convAmt(double cvtAmt) throws Exception
  {
    long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.000001);
    double cvtDouble =  ((double) cvtLong) / 100;
    return cvtDouble;
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
