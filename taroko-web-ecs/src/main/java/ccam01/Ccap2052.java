/*臨時調整額度覆核（依卡戶）V.2018-0928
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 109-10-16 V1.00.02  tanwei           updated for project coding standard
 * 109-12-31  V1.00.03   shiyuqi       修改无意义命名    
 * 110-01-15  V1.00.04   Justin          fix  a query bug                                                                                  *    
 * */
package ccam01;

import ofcapp.BaseAction;

public class Ccap2052 extends BaseAction {
  String cardAcctIdx = "";
  busi.func.CcasFunc ooCcas = null;
  ofcapp.EcsApprove ooAppr = null;
  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
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
      /* 更新功能 */
      procFunc();
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = "", lsIdNo = "";

    lsWhere = " where B.mod_type='ADJ-LIMIT' " + sqlCol(wp.itemStr("ex_adj_user"), "B.adj_user");

    lsIdNo = wp.itemStr("ex_idno");
    if (!empty(lsIdNo)) {
      if (lsIdNo.length() == 8) {
        lsWhere += " and A.corp_p_seqno in (select corp_p_seqno from crd_corp where 1=1 "+sqlCol(lsIdNo,"corp_no")+")";
      } else if (lsIdNo.length() == 10) {
        lsWhere += " and A.id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "+sqlCol(lsIdNo,"id_no")+")";
      } else {
        errmsg("身分證ID 須為 8 碼 或 10 碼");
        return;
      }
    }

    if (!wp.itemEmpty("ex_card_no")) {
      lsWhere += " and A.acno_p_seqno in (select acno_p_seqno from crd_card where 1=1 "+sqlCol(wp.itemStr("ex_card_no"),"card_no")+")";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " A.acct_type , A.acno_p_seqno, A.card_acct_idx," + " B.adj_eff_start_date , "
        + " B.adj_eff_end_date , " + " B.adj_area , " + " B.tot_amt_month , " + " B.adj_inst_pct , "
        + " B.adj_remark , " + " B.adj_reason , " + " B.adj_sms_flag , " + " B.adj_user , "
        + " B.adj_date , " + " B.adj_quota , " + " B.mod_seqno,"
        + " uf_corp_no(A.corp_p_seqno) as corp_no , "
        + " uf_acno_name2(A.acno_p_seqno,A.acct_type) as acno_name , " + " A.spec_status , "
        + " A.block_reason1||','||A.block_reason2||','||A.block_reason3||','||"
        + "A.block_reason4||','||A.block_reason5 as wk_block_reason , "
        + " uf_tt_ccas_parm3('ADJREASON',b.adj_reason) as tt_adj_reason," + " A.debit_flag, "
        + " C.acct_key , " + " C.line_of_credit_amt , " + " A.acno_flag ";
    wp.daoTable = " cca_card_acct A join cca_card_acct_t B on A.card_acct_idx=B.card_acct_idx"
        + " join act_acno C on C.acno_p_seqno=A.acno_p_seqno";
    wp.whereOrder = "order by C.acct_key, A.acct_type";
    pageQuery();
    wp.listCount[0] = sqlRowNum;
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();

    queryAfter();
  }
  
  void queryAfter() {
	  String sql1 = "select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
	  for(int ii=0;ii<wp.selectCnt;ii++) {
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
    cardAcctIdx = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.selectSQL = "" + " card_acct_idx , " + " adj_area , " + " adj_reason , "
        + " tot_amt_month , " + " adj_eff_start_date , " + " adj_eff_end_date ,"
        + " adj_inst_pct , " + " adj_remark , " + " spec_status , " + " adj_risk_flag ,"
        + " mod_user," + " crt_user," + " crt_date," + " adj_date," + " debit_flag,"
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " 0 as comp_amt , "
        + " 0 as comp_inst_amt , " + " 0 as line_credit_amt , " + " block_reason1 , "
        + " block_reason2 , " + " block_reason3 , " + " block_reason4 , " + " block_reason5 , "
        + " block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as wk_block_reason ,"
        + " spec_status ," + " hex(rowid) as rowid, mod_seqno , "
        + " uf_tt_ccas_parm3('ADJREASON',adj_reason) as tt_adj_reason , " + " adj_sms_flag , "
        + " notice_flag";
    wp.daoTable = " cca_card_acct_t";
    wp.whereStr = " where card_acct_idx =? and mod_type='ADJ-LIMIT'";

    setDouble(1, commString.strToNum(cardAcctIdx));

    pageSelect();
    if (sqlRowNum <= 0) {
      alertErr("查無資料, key=" + cardAcctIdx);
      return;
    }

    dataReadAfter();
    selectCcaAdjParm();

  }

  void selectCcaAdjParm() throws Exception {
    wp.pageRows = 999;

    wp.sqlCmd = " select " + " card_acct_idx , " + " risk_type ,"
        + " uf_tt_risk_type(risk_type) as tt_risk_type , " + " adj_month_amt , " + " adj_day_amt , "
        + " adj_day_cnt , " + " adj_month_cnt , "
        + " adj_eff_start_date as adj_date1 , "
        + " adj_eff_end_date as adj_date2 , "
        + " spec_flag "
        + " from cca_adj_parm_t " + " where 1=1 "
        + col(wp.colNum("card_acct_idx"), "card_acct_idx",true);

    pageQuery();
    wp.setListCount(0);
    if (sqlRowNum == 0) {
      this.selectOK();
    }
  }

  void dataReadAfter() {
    String sql1 =
        "select B.acct_type , " + " B.acct_key ," + " B.line_of_credit_amt as line_credit_amt , "
            + " uf_idno_name(B.id_p_seqno) as chi_name, " + " A.tot_amt_month,"
            + " A.adj_eff_start_date, A.adj_eff_end_date , " + " A.id_p_seqno" + ", B.acno_p_seqno"
            + " from cca_card_acct A join act_acno B on B.acno_p_seqno=A.acno_p_seqno "
            + " where A.debit_flag <>'Y'" + " and A.card_acct_idx =?";

    setDouble(1, wp.colNum("card_acct_idx"));
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      alertErr2("查無帳戶資料(act_acno), kk=" + wp.colStr("card_acct_idx"));
      return;
    }
    wp.colSet("acct_type", sqlStr("acct_type"));
    wp.colSet("acct_key", sqlStr("acct_key"));
    wp.colSet("line_credit_amt", sqlStr("line_credit_amt"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
    wp.colSet("acno_p_seqno", sqlStr("acno_p_seqno"));
    String lsTodate = this.getSysDate();
    if (commString.strComp(lsTodate, sqlStr("add_eff_start_date")) >= 0
        && commString.strComp(lsTodate, sqlStr("add_eff_end_date")) <= 0) {
      wp.colSet("line_credit_amt_t", sqlStr("tot_amt_month"));
    } else {
      wp.colSet("line_credit_amt_t", sqlStr("line_credit_amt"));
    }

    // --
    String sql2 = "select A.fh_flag as rela_flag , " + " A.non_asset_balance as asset_balance , "
        + " B.asset_value as bond_amt " + " from crd_idno B left join crd_correlate A "
        + " on A.correlate_id =B.id_no " + " where B.id_p_seqno =? " + " order by A.crt_date desc "
        + commSqlStr.rownum(1);
    sqlSelect(sql2, new Object[] {wp.colStr("id_p_seqno")});

    if (sqlRowNum <= 0) {
      wp.colSet("rela_flag", "");
      wp.colSet("asset_balance", "");
      wp.colSet("bond_amt", "");
      return;
    }	else	{
    	wp.colSet("rela_flag", sqlStr("rela_flag"));
        wp.colSet("asset_balance", sqlStr("asset_balance"));
        wp.colSet("bond_amt", sqlStr("bond_amt"));
    }
    
    //--附卡張數
    String sql3 = " select count(*) as db_cnt from crd_card where acno_p_seqno = ? and current_code='0' and sup_flag='1'  ";
    sqlSelect(sql3, new Object[] { wp.colStr("acno_p_seqno") });
	if (sqlRowNum > 0) {
		wp.colSet("card_sup_cnt", sqlStr("db_cnt"));
	}
    
	if(wp.colEq("acct_type", "03") || wp.colEq("acct_type", "06")) {
		String sql4 = "";
		sql4 = " select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
		sqlSelect(sql4,new Object[] {wp.colStr("acno_p_seqno")});
		
		if(sqlRowNum > 0 ) {
			wp.colSet("card_no", sqlStr("card_no"));
		}
	}		
	
  }

  @Override
  public void saveFunc() throws Exception {
    ccam01.Ccap2052Func func = new ccam01.Ccap2052Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    } else
      this.saveAfter(false);


  }

  @Override
  public void procFunc() throws Exception {
    ccam01.Ccap2052Func func = new ccam01.Ccap2052Func();
    func.setConn(wp);
    ooAppr = new ofcapp.EcsApprove(wp);
    wp.listCount[0] = wp.itemRows("risk_type");
    
    if (!apprBankUnit(wp.itemStr("mod_user"),"")) {
		alertErr("銀行單位不同");
		return;
	}
    
    ooCcas = new busi.func.CcasFunc();
    ooCcas.setConn(wp);    
    if (ooCcas.specApprove(wp.loginUser, wp.itemStr("spec_status")) != 1) {      
      alertErr2(ooCcas.getMsg());
      return;
    }
    
    //--額度層級
    double lmCheckAmt =0;
	if(wp.itemNum("tot_amt_month")>=wp.itemNum("adj_inst_pct"))
		lmCheckAmt = wp.itemNum("tot_amt_month");
	else	lmCheckAmt = wp.itemNum("adj_inst_pct");
	// -主管覆核-
	if (ooAppr.adjLimitApprove(wp.loginUser,lmCheckAmt) != 1) {
		alertErr(ooAppr.getMesg());
		return;
	}
    
    rc = func.dataProc();
    if (rc != 1) {
      alertErr2(func.getMsg());
      return;
    }

    alertMsg("覆核完成");

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
