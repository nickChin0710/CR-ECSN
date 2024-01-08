/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 107-03-14  V1.00.01  ryan       program initial                            *
 * 109-04-06  V2.00.01  ryan       add f_auth_query()                         *
 * 111/10/24  V3.00.01  jiangyigndong  updated for project coding standard    *
 * 112/02/14  V3.00.02  Simon      add acct_code_type display                 *
 * 112/02/22  V3.00.03  Simon      set wp.pageRows = 9999                     *
 ******************************************************************************/
package actq01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actq0090 extends BaseEdit {
  String exPSeqno ="", exIdCname ="", exCycle ="";
  String mProgName = "actq0090", exAcnoPSeqno ="";
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
    wp.colSet("tol_dc_end_bal", "0");
    wp.colSet("tol_end_bal", "0");
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageRows = 9999;

    //查詢權限檢查，參考【f_auth_query】
    String lsId = "";
    lsId = wp.itemStr("ex_acct_key");
    if(empty(lsId)){
      lsId = wp.itemStr("ex_card_no");
    }
    busi.func.ColFunc func =new busi.func.ColFunc();
    func.setConn(wp);
    if (func.fAuthQuery(mProgName, lsId)!=1)
    { alertErr(func.getMsg()); return ;}

    if(wp.itemStr("ex_history").equals("N")){
      getwherestrA();
    }else{
      getWhereStrB();
    }
    queryreadAb();
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
  void listWkdata(){
    long tolDcEndBal = 0,tolEndBal=0;
    String lAcctCode = "", lAcctCodeType = "";
    for (int ll = 0; ll < wp.selectCnt; ll++) {
      tolDcEndBal += wp.colNum(ll,"dc_end_bal");
      tolEndBal   += wp.colNum(ll,"end_bal");
      lAcctCode      = wp.colStr(ll,"acct_code");
      lAcctCodeType  = wp.colStr(ll,"acct_code_type");
      if (lAcctCode.equals("DB")) {
        wp.colSet(ll,"acct_code",lAcctCode+"-"+lAcctCodeType);
      }
    }
    wp.colSet("tol_dc_end_bal", tolDcEndBal+"");
    wp.colSet("tol_end_bal", tolEndBal+"");
  }

  void queryreadAb() throws Exception{

    wp.pageControl();

    wp.selectSQL = " a.*,ptr_actcode.chi_long_name "
            + " ,uf_dc_amt (a.curr_code, a.beg_bal, a.dc_beg_bal) dc_beg_bal "
            + " ,uf_dc_amt (a.curr_code, a.end_bal, a.dc_end_bal) dc_end_bal "
            + " ,uf_dc_amt (a.curr_code, a.d_avail_bal, a.dc_d_avail_bal) dc_aval_bal "
            + " from ( select"
            + " post_date "
            + " ,acct_code "
            + " ,acct_code_type "
            + " ,acct_month "
            + " ,beg_bal "
            + " ,end_bal "
            + " ,d_avail_bal "
            + " ,interest_date "
            + " ,purchase_date "
            + " ,card_no "
            + " ,reference_no "
            //+ " ,acno_p_seqno "
            + " ,p_seqno "
            + " ,item_order_normal "
            + " ,item_class_normal "
            + " ,stmt_cycle "
            + " ,interest_rs_date "
            + " ,decode(curr_code,' ','901',curr_code) curr_code "
            + " ,dc_beg_bal "
            + " ,dc_end_bal "
            + " ,dc_d_avail_bal "
    ;


    wp.daoTable = " act_debt union select "
            + " post_date "
            + " ,acct_code "
            + " ,acct_code_type "
            + " ,acct_month "
            + " ,beg_bal "
            + " ,end_bal "
            + " ,d_avail_bal "
            + " ,interest_date "
            + " ,purchase_date "
            + " ,card_no "
            + " ,reference_no "
            //+ " ,acno_p_seqno "
            + " ,p_seqno "
            + " ,item_order_normal "
            + " ,item_class_normal "
            + " ,stmt_cycle "
            + " ,interest_rs_date "
            + " ,decode(curr_code,' ','901',curr_code) curr_code "
            + " ,dc_beg_bal "
            + " ,dc_end_bal "
            + " ,dc_d_avail_bal "
            + " from act_debt_hst "
            + " ) as a,ptr_actcode ";
    //wp.whereOrder = " ";
    wp.whereOrder = " order by acct_month, post_date ";
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      wp.colSet("tol_dc_end_bal", "0");
      wp.colSet("tol_end_bal", "0");
      return;
    }

    listWkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }


  //點選當期
//	void queryread_a() throws Exception{
//		wp.pageControl();
//
//		wp.selectSQL = " act_debt.post_date "
//					+ " ,act_debt.acct_code "
//					+ " ,act_debt.acct_month "
//					+ " ,act_debt.beg_bal "
//					+ " ,act_debt.end_bal "
//					+ " ,act_debt.d_avail_bal "
//					+ " ,act_debt.interest_date "
//					+ " ,act_debt.purchase_date "
//					+ " ,act_debt.card_no "
//					//+ " ,act_debt.acquire_date "
//					+ " ,act_debt.reference_no "
//					+ " ,act_debt.acno_p_seqno "
//					+ " ,act_debt.item_order_normal "
//					+ " ,act_debt.item_class_normal "
//					+ " ,act_debt.stmt_cycle "
//					+ " ,ptr_actcode.query_type "
//					+ " ,act_debt.interest_rs_date "
//					//+ " ,act_debt.acct_item_cname "
//					+ " ,ptr_actcode.chi_long_name "
//					+ " ,decode(act_debt.curr_code,' ','901',curr_code) curr_code "
//					+ " ,uf_dc_amt(curr_code,beg_bal,dc_beg_bal) dc_beg_bal "
//					+ " ,uf_dc_amt(curr_code,end_bal,dc_end_bal) dc_end_bal "
//					+ " ,uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) dc_aval_bal "
//					;
//
//
//		wp.daoTable = " act_debt,ptr_actcode ";
//		wp.whereOrder = " ";
//		getWhereStr_a();
//		pageQuery();
//		wp.setListCount(1);
//		if (sql_notFind()) {
//			alertErr(appMsg.err_condNodata);
//			wp.colSet("tol_dc_end_bal", "0");
//			wp.colSet("tol_end_bal", "0");
//			return;
//		}
//
//	    list_wkdata();
//		wp.totalRows = wp.dataCnt;
//		wp.listCount[1] = wp.dataCnt;
//		wp.setPageValue();
//	}
  //點選歷史
//	void queryread_b() throws Exception{
//		wp.pageControl();
//
//		wp.selectSQL = " act_debt_hst.reference_no "
//					+ " ,act_debt_hst.acno_p_seqno "
//					+ " ,act_debt_hst.acct_type "
//					//+ " ,act_debt_hst.acct_key "
//					+ " ,act_debt_hst.post_date "
//					+ " ,act_debt_hst.item_order_normal "
//					//+ " ,act_debt_hst.item_order_back_date "
//					//+ " ,act_debt_hst.item_order_refund "
//					+ " ,act_debt_hst.item_class_normal "
//					//+ " ,act_debt_hst.item_class_back_date "
//					//+ " ,act_debt_hst.item_class_refund "
//					+ " ,act_debt_hst.acct_month "
//					+ " ,act_debt_hst.stmt_cycle "
//					//+ " ,act_debt_hst.bill_type "
//					//+ " ,act_debt_hst.transaction_code "
//					+ " ,act_debt_hst.beg_bal "
//					+ " ,act_debt_hst.end_bal "
//					+ " ,act_debt_hst.d_avail_bal "
//					//+ " ,act_debt_hst.acct_item_cname "
//					+ " ,act_debt_hst.interest_date "
//					+ " ,act_debt_hst.purchase_date "
//					//+ " ,act_debt_hst.acquire_date"
//					//+ " ,act_debt_hst.film_no "
//					//+ " ,act_debt_hst.mcht_no "
//					//+ " ,act_debt_hst.prod_no "
//					+ " ,act_debt_hst.interest_rs_date "
//					+ " ,act_debt_hst.card_no "
//					+ " ,act_debt_hst.acct_code "
//					+ " ,ptr_actcode.chi_long_name "
//					+ " ,decode(curr_code,'','901',curr_code) curr_code "
//					+ " ,uf_dc_amt(curr_code,beg_bal,dc_beg_bal) dc_beg_bal "
//					+ " ,uf_dc_amt(curr_code,end_bal,dc_end_bal) dc_end_bal "
//					+ " ,uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) dc_aval_bal "
//					;
//
//
//		wp.daoTable = " act_debt_hst,ptr_actcode ";
//		wp.whereOrder = " ";
//		getWhereStr_b();
//		pageQuery();
//		wp.setListCount(1);
//		if (sql_notFind()) {
//			alertErr(appMsg.err_condNodata);
//			wp.colSet("tol_dc_end_bal", "0");
//			wp.colSet("tol_end_bal", "0");
//			return;
//		}
//
//	    list_wkdata();
//		wp.totalRows = wp.dataCnt;
//		wp.listCount[1] = wp.dataCnt;
//		wp.setPageValue();
//	}

  void getwherestrA() throws Exception{
    if(empty(exCycle)){
      exCycle = wp.itemStr("ex_cycle");
    }
    if(empty(exPSeqno)){
      exPSeqno = wp.itemStr("ex_p_seqno");
    }
    String sqlSelect="select next_acct_month from ptr_workday where stmt_cycle = :ex_cycle ";
    setString("ex_cycle", exCycle);
    sqlSelect(sqlSelect);
    String exMonth = sqlStr("next_acct_month");
    wp.whereStr = " where 1=1 and a.acct_code = ptr_actcode.acct_code ";

    if (empty(exPSeqno) == false) {
      //wp.whereStr += " and  a.acno_p_seqno = :ex_p_seqno ";
      wp.whereStr += " and  a.p_seqno = :ex_p_seqno ";
      setString("ex_p_seqno", exPSeqno);
    }
    if (empty(exMonth) == false) {
      wp.whereStr += " and  a.acct_month = :ex_month ";
      setString("ex_month", exMonth);
    }
    if (wp.itemStr("ex_dc").equals("1")) {
      wp.whereStr += " and  (a.acct_code  = 'BL' "
              + "or a.acct_code = 'CA' "
              + "or a.acct_code = 'IT' "
              + "or a.acct_code = 'ID' "
              + "or a.acct_code = 'AO' "
              + "or a.acct_code = 'DP' "
              + "or a.acct_code = 'CB' "
              + "or a.acct_code = 'DB' "
              + "or a.acct_code = 'OT') ";
    }
    if (wp.itemStr("ex_dc").equals("2")) {
      wp.whereStr += " and  (a.acct_code = 'AF' "
              + "or a.acct_code = 'LF' "
              + "or a.acct_code = 'CF' "
              + "or a.acct_code = 'PF' "
              + "or a.acct_code = 'RI' "
              + "or a.acct_code = 'PN' "
              + "or a.acct_code = 'AI' "
              + "or a.acct_code = 'SF' "
              + "or a.acct_code = 'CI' "
              + "or a.acct_code = 'CC') ";
    }
    if (empty(wp.itemStr("ex_curr_code")) == false) {
      wp.whereStr += " and  a.curr_code = :ex_curr_code ";
      setString("ex_curr_code", wp.itemStr("ex_curr_code"));
    }
  }

  void getWhereStrB(){

    if(empty(exPSeqno)){
      exPSeqno = wp.itemStr("ex_p_seqno");
    }
    wp.whereStr = " where 1=1 and a.acct_code = ptr_actcode.acct_code ";

    if (empty(exPSeqno) == false) {
      //wp.whereStr += " and  a.acno_p_seqno = :ex_p_seqno ";
      wp.whereStr += " and  a.p_seqno = :ex_p_seqno ";
      setString("ex_p_seqno", exPSeqno);
    }

    if (empty(wp.itemStr("ex_month")) == false) {
      wp.whereStr += " and  a.acct_month = :ex_month ";
      setString("ex_month", wp.itemStr("ex_month"));
    }

    if (wp.itemStr("ex_dc").equals("1")) {
      wp.whereStr += " and  (a.acct_code  = 'BL' "
              + "or a.acct_code = 'CA' "
              + "or a.acct_code = 'IT' "
              + "or a.acct_code = 'ID' "
              + "or a.acct_code = 'AO' "
              + "or a.acct_code = 'DP' "
              + "or a.acct_code = 'CB' "
              + "or a.acct_code = 'DB' "
              + "or a.acct_code = 'OT') ";
    }
    if (wp.itemStr("ex_dc").equals("2")) {
      wp.whereStr += " and  (a.acct_code = 'AF' "
              + "or a.acct_code = 'LF' "
              + "or a.acct_code = 'CF' "
              + "or a.acct_code = 'PF' "
              + "or a.acct_code = 'RI' "
              + "or a.acct_code = 'PN' "
              + "or a.acct_code = 'AI' "
              + "or a.acct_code = 'SF' "
              + "or a.acct_code = 'CI' "
              + "or a.acct_code = 'CC') ";
    }

    if (empty(wp.itemStr("ex_curr_code")) == false) {
      wp.whereStr += " and  a.curr_code = :ex_curr_code ";
      setString("ex_curr_code", wp.itemStr("ex_curr_code"));
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
			/*
			if(wp.itemStr("ex_history").equals("Y")){
				if(empty(wp.itemStr("ex_month"))){
					alertErr("關帳年月不可為空");
					return;
				}
			}
		  */
    }

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
    if(strAction.equals("Q2")){
      queryFunc();
    }
  }
}
