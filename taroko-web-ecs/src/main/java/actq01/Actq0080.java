/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 107-03-16  V1.00.01  ryan       program initial                            *
 * 109-04-06  V2.00.01  ryan       add f_auth_query()                         *
 * 111/10/24  V3.00.01  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/
package actq01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actq0080 extends BaseEdit {
  String exPSeqno ="", exIdCname ="", exCycle ="";
  String mProgName = "actq0080", exAcnoPSeqno ="";
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + strAction + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "PSEQNO1")) {
      /* ex_acct_key */
      strAction = "PSEQNO1";
      pSeqno();
    } else if (eqIgno(wp.buttonCode, "PSEQNO2")) {
      /* ex_card_no */
      strAction = "PSEQNO2";
      pSeqno();
    } else if (eqIgno(wp.buttonCode, "Q2")) {
      /* ex_card_no */
      strAction = "Q2";
      pSeqno();
    }

    dddwSelect();
    initButton();
  }


  @Override
  public void queryFunc() throws Exception {

    // -page control-
    //wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void initPage(){
    wp.colSet("tol_interest_amt1", "0");
    wp.colSet("tol_interest_amt2", "0");
    wp.colSet("tol_interest_amt", "0");
  }

  int getWhereStr() throws Exception{
    String lsDate1 = wp.itemStr("ex_date1");
    String lsDate2 = wp.itemStr("ex_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr("[關帳年月-起迄]  輸入錯誤");
      return -1;
    }

    if(empty(exPSeqno)){
      exPSeqno = wp.itemStr("ex_p_seqno");
    }

    wp.whereStr = " where 1=1 and reason_code in ('DB0A','DB02','DB03','DB04','DB05') ";

    if (empty(exPSeqno) == false) {
      wp.whereStr += " and  p_seqno = :ex_p_seqno ";
      setString("ex_p_seqno", exPSeqno);
    }
    if (empty(wp.itemStr("ex_date1")) == false) {
      wp.whereStr += " and  acct_month >= :ex_date1 ";
      setString("ex_date1", wp.itemStr("ex_date1"));
    }
    if (empty(wp.itemStr("ex_date2")) == false) {
      wp.whereStr += " and  acct_month <= :ex_date2 ";
      setString("ex_date2", wp.itemStr("ex_date2"));
    }

    if (empty(wp.itemStr("ex_curr_code")) == false) {
      wp.whereStr += " and  curr_code = :ex_curr_code ";
      setString("ex_curr_code", wp.itemStr("ex_curr_code"));
    }
    return 1;
  }

  @Override
  public void queryRead() throws Exception {

    //查詢權限檢查，參考【f_auth_query】
    String lsId = "";
    lsId = wp.itemStr("ex_acct_key");
    if(empty(lsId)){
      lsId = wp.itemStr("ex_card_no");
    }

    busi.func.ColFunc func =new busi.func.ColFunc();
    func.setConn(wp);
    if (func.fAuthQuery(mProgName, lsId)!=1) {
      alertErr(func.getMsg()); return; }

    wp.pageControl();

    wp.selectSQL = " p_seqno "
            + " ,acct_type "
            //+ " ,acct_key "
            + " ,post_date "
            + " ,acct_month "
            + " ,intr_org_captial  "
            + " ,intr_s_date "
            + " ,intr_e_date "
            + " ,interest_sign "
            + " ,reason_code "
            + " ,interest_amt  tw_interest_amt "
            + " ,inte_d_amt  tw_inte_d_amt "
            + " ,reference_no "
            + " ,interest_rate "
            + " ,crt_date "
            + " ,crt_time "
            + " ,decode(curr_code,'','901',curr_code) curr_code "
            + " ,decode(curr_code,'',inte_d_amt,'901',inte_d_amt,dc_inte_d_amt) inte_d_amt "
            + " ,decode(curr_code,'',intr_org_captial,'901',intr_org_captial,dc_intr_org_captial) intr_org_captial "
            + " ,decode(curr_code,'',interest_amt,'901',interest_amt,dc_interest_amt) interest_amt "
            + " ,decode(curr_code,'901',1,'TWD',1,'840',2,'USD',2,'392',3,'978',4,9)  curr_sort "
    ;

    wp.daoTable = " act_intr ";
    wp.whereOrder = " ORDER BY curr_sort, crt_date, reference_no, intr_s_date ";

    if(getWhereStr()!=1){
      return;
    }
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      wp.colSet("tol_interest_amt1", "0");
      wp.colSet("tol_interest_amt2", "0");
      wp.colSet("tol_interest_amt", "0");
      return;
    }

    listWkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {

    try {
      wp.optionKey = wp.itemStr("ex_acct_type");
      this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

      wp.optionKey = wp.itemStr("ex_curr_code");
      this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");

    } catch (Exception ex) {
    }
  }
  void listWkdata() throws Exception{
    String ss1 = "",ss2="",interestSign="";
    int ii = 1;
    double interestAmt1=0,interestAmt2=0,sumInterestAmt=0,tolInterestAmt1=0,tolInterestAmt2=0;

    getWhereStr();
    String whereStr = wp.whereStr;
    String sqlSelect = "select sum(interest_amt) as tol_interest_amt1 from act_intr "+ whereStr + " and interest_sign = '+'";
    sqlSelect(sqlSelect);
    tolInterestAmt1 = sqlNum("tol_interest_amt1");

    getWhereStr();
    whereStr = wp.whereStr;
    sqlSelect = "select sum(interest_amt) as tol_interest_amt2 from act_intr "+ whereStr + " and interest_sign = '-'";
    sqlSelect(sqlSelect);
    tolInterestAmt2 = sqlNum("tol_interest_amt2");

    sumInterestAmt = tolInterestAmt1-tolInterestAmt2;
    wp.colSet("tol_interest_amt1",formatDouble1(tolInterestAmt1)+"");
    wp.colSet("tol_interest_amt2",formatDouble1(tolInterestAmt2)+"");
    wp.colSet("tol_interest_amt",formatDouble1(sumInterestAmt)+"");
    interestAmt1 = 0;
    interestAmt2 = 0;
    sumInterestAmt = 0;

    for (int ll = 0; ll < wp.selectCnt; ll++) {
      ss1 = wp.colStr(ll,"curr_code");
      ss2 = wp.colStr(ii,"curr_code");
      interestSign = wp.colStr(ll,"interest_sign");
      if(interestSign.equals("+")){
        interestAmt1 += wp.colNum(ll, "interest_amt");
      }
      if(interestSign.equals("-")){
        interestAmt2 += wp.colNum(ll, "interest_amt");
      }
      ii++;
      if(ss1.equals(ss2)){
        continue;
      }

      sumInterestAmt = interestAmt1-interestAmt2;
      wp.colSet(ll,"sum_interest_amt","<tr>"
              +"<td colspan=2></td>"
              +"<td nowrap colspan=2 class='dsp_text'>&nbsp;結算幣別: "
              + ss1
              + "</td>"
              + "<td nowrap colspan=9 class='dsp_text'>&nbsp;利息小計: "
              +formatDouble1(interestAmt1)+" - "+formatDouble1(interestAmt2)+" = "+formatDouble1(sumInterestAmt)
              +"</td>"
              + "</tr>");
      interestAmt1 = 0;
      interestAmt2 = 0;

    }
  }

  void pSeqno() throws Exception{
    String exAcctKey = wp.itemStr("ex_acct_key");
    if(exAcctKey.length()==8){
      exAcctKey = exAcctKey+"000";
    }
    if(exAcctKey.length()==10){
      exAcctKey = exAcctKey+"0";
    }
    if(strAction.equals("Q2")){
      if(empty(exAcctKey)&&empty(wp.itemStr("ex_card_no"))){
        alertErr("帳號及卡號不可全部為空");
        return;
      }
    }

    //wp.alertMesg += "<script language='javascript'> alert('f_auth_query1111')</script>";

    /*** 同 actq0090.java，查詢權限檢查放在 queryRead裡
     //查詢權限檢查，參考【f_auth_query】
     String ls_id = "";
     ls_id = ex_acct_key;
     if(empty(ls_id)){
     ls_id = wp.sss("ex_card_no");
     }

     busi.func.colFunc func =new busi.func.colFunc();
     func.setConn(wp);
     if (func.f_auth_query(m_progName, ls_id)!=1) {
     alertErr(func.getMsg()); return; }
     ***/

    String sqlSelect="",corpPSeqno = "";
    if(strAction.equals("PSEQNO1")||strAction.equals("Q2")){
      if(!empty(exAcctKey)){
        sqlSelect = "select acct_type "
                + ", acct_key "
                + ", acno_p_seqno "
                + ", id_p_seqno "
                + ", corp_p_seqno "
                + ", uf_acno_name(acno_p_seqno) as acno_cname "
                + " from act_acno "
                + " where acct_type = :ex_acct_type "
                + " and acct_key = :ex_acct_key ";
        setString("ex_acct_type",wp.itemStr("ex_acct_type"));
        setString("ex_acct_key",exAcctKey);
        sqlSelect(sqlSelect);
        if(sqlRowNum<=0){
          alertErr("查無資料");
          return;
        }
        exAcnoPSeqno = sqlStr("acno_p_seqno");
        exIdCname = sqlStr("acno_cname");
        wp.colSet("ex_acno_p_seqno", exAcnoPSeqno);
        wp.colSet("ex_id_cname", exIdCname);
        corpPSeqno = sqlStr("corp_p_seqno");
      }
    }
    if(strAction.equals("PSEQNO2")||strAction.equals("Q2")){
      if(!empty(wp.itemStr("ex_card_no"))){
        sqlSelect = "select acct_type "
                + ", acct_key "
                + ", acno_p_seqno "
                + ", corp_p_seqno "
                + ", uf_acno_name(acno_p_seqno) as acno_cname "
                + " from act_acno "
                + " where acno_p_seqno in (select acno_p_seqno from crd_card where card_no = :ex_card_no) ";
        setString("ex_card_no",wp.itemStr("ex_card_no"));
        sqlSelect(sqlSelect);
        if(sqlRowNum<=0){
          alertErr("查無資料");
          return;
        }
        exAcnoPSeqno = sqlStr("acno_p_seqno");
        exIdCname = sqlStr("acno_cname");
        wp.colSet("ex_acno_p_seqno", exAcnoPSeqno);
        wp.colSet("ex_id_cname", exIdCname);
        corpPSeqno = sqlStr("corp_p_seqno");
      }
    }


    if(empty(exAcnoPSeqno))return;
    sqlSelect = "select a.acct_status, "
            + " a.p_seqno, "
            + " a.stmt_cycle, "
            + " b.this_acct_month "
            + " from act_acno a, ptr_workday b "
            + " where b.stmt_cycle = a.stmt_cycle "
            + " and  a.acno_p_seqno = :acno_p_seqno ";
    setString("acno_p_seqno", exAcnoPSeqno);
    sqlSelect(sqlSelect);
    exPSeqno = sqlStr("p_seqno");
    wp.colSet("ex_p_seqno", exPSeqno);
    exCycle = sqlStr("stmt_cycle");
    String this_acct_month = sqlStr("this_acct_month");

    if(sqlRowNum>0){
      wp.colSet("ex_cycle", exCycle);
    }

    if(!empty(corpPSeqno)){
      sqlSelect = "select chi_name from crd_corp where corp_p_seqno = :corp_p_seqno";
      setString("corp_p_seqno",corpPSeqno);
      sqlSelect(sqlSelect);
      if(sqlRowNum>0){
        wp.colSet("ex_corp_cname", sqlStr("chi_name"));
      }

    }

    String lsYymm1 = wp.itemStr("ex_date1");
    String lsYymm2 = wp.itemStr("ex_date2");
    if(empty(lsYymm2)) {
      lsYymm2 = "999999";
    }

    sqlSelect = " select sum(beg_bal) as tot_beg_bal "
            + " from act_debt "
            + " where p_seqno = :ex_p_seqno "
            + " and  acct_month >= :ex_yymm1 "
            + " and  acct_month <= :ex_yymm2 "
            + " and  acct_code = 'RI' ";
    setString("ex_p_seqno", exPSeqno);
    setString("ex_yymm1",lsYymm1);
    setString("ex_yymm2",lsYymm2);
    sqlSelect(sqlSelect);
    wp.colSet("ex_interest", sqlStr("tot_beg_bal"));

    if(strAction.equals("Q2")){
      queryFunc();
    }
  }

  public static double formatDouble1(double d) {
    return (double)Math.round(d*100)/100;
  }
}
