package ccam01;
/**人工授權沖正處理
 * 2019-1210:  Alex  add initButton
 * 2019-0520:  JH    modify
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 */

import ofcapp.BaseAction;

public class Ccam1020 extends BaseAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  String txDate = "" , txTime = "" , cardNo = "" , authNo = "" , traceNo = "";

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
    }


  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("日期 : 起迄錯誤");
      return;
    }

    if (empty(wp.itemStr("ex_card_no")) && empty(wp.itemStr("ex_idno"))) {
      alertErr2("卡號 , 身分證字號 不可同時空白 !");
      return;
    }

    if (!empty(wp.itemStr("ex_card_no"))) {
      if (checkCardNo() == false)
        return;
    }

    String lsWhere = " where 1=1 " + " and iso_resp_code in ('00' ,'000','001') "
    	+ " and cacu_amount='Y' and mtch_flag <> 'Y' "
//-- 開放轉檔前交易可以沖正    		
//        + " and cacu_amount='Y' and mtch_flag = 'N' "
        + " and online_redeem in ('','N','A','Z','0','I','E','1','2') "
        + " and not (trans_type='0220' and proc_code='200030') "
        + " and reversal_flag <> 'Y' "
        + sqlCol(wp.itemStr("ex_date1"), "tx_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "tx_date", "<=");

    if (!empty(wp.itemStr("ex_card_no"))) {
      lsWhere += sqlCol(wp.itemStr("ex_card_no"), "card_no");
    } else if (!wp.itemEmpty("ex_idno")) {
      String lsIdno = wp.itemStr2("ex_idno");
      lsWhere += "and card_no in ("
          + " select card_no from crd_card C, crd_idno D where C.id_p_seqno=D.id_p_seqno"
          + sqlCol(lsIdno, "D.id_no")
          + " union select card_no from dbc_card E, dbc_idno F where E.id_p_seqno=F.id_p_seqno"
          + sqlCol(lsIdno, "F.id_no") + " )";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  boolean checkCardNo() {
    String sql1 = " select " + " debit_flag " + " from cca_card_base " + " where card_no = ? ";
    sqlSelect(sql1, new Object[] {wp.itemStr("ex_card_no")});

    if (sqlRowNum <= 0) {
      alertErr2("輸入卡號錯誤 !");
      return false;
    }

    if (eqIgno(sqlStr("debit_flag"), "Y")) {
      alertErr2("DEBIT卡不可做人工沖正!");
      return false;
    }

    return true;
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " tx_date ," + " tx_time ," + " card_no ," + " auth_no ," + " mcht_no ,"
        + " mcc_code ," + " nt_amt ," + " card_acct_idx , " + " auth_seqno,"
        + " hex(rowid) as rowid , trace_no ";
    wp.daoTable = "cca_auth_txlog";
    wp.whereOrder = " order by tx_date desc, tx_time desc ";

    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    // queryAfter();
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
	txDate = wp.itemStr("data_k1");
	txTime = wp.itemStr("data_k2");
	cardNo = wp.itemStr("data_k3");
	authNo = wp.itemStr("data_k4");
	traceNo = wp.itemStr("data_k5");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.selectSQL = " A.*, " + " to_char(A.mod_time,'yyyymmdd') as mod_date , "
        + " uf_idno_id(A.id_p_seqno) as id_no " + ", hex(A.rowid) as rowid";
    wp.daoTable = "cca_auth_txlog A ";
    wp.whereStr = "where 1=1" 
    			+sqlCol(txDate,"tx_date")
    			+sqlCol(txTime,"tx_time")
    			+sqlCol(cardNo,"card_no")
    			+sqlCol(authNo,"auth_no")
    			+sqlCol(traceNo,"trace_no")
    			;

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料");
      return;
    }
    
    dataReadAfter();
    
  }
  
  void dataReadAfter() throws Exception {
	  String sql1 = "select curr_code as card_curr_code from crd_card where card_no = ? ";
	  sqlSelect(sql1,new Object[] {wp.colStr("card_no")});
	  
	  if(sqlRowNum >0) {
		  wp.colSet("card_curr_code", sqlNvl("card_curr_code","901"));
	  }	else	{
		  wp.colSet("card_curr_code", "901");
	  }
  }
  
  @Override
  public void saveFunc() throws Exception {
    Ccam1020Func func = new Ccam1020Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    // sql_commit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
      return;
    }

    this.alertMsg(wp.colStr("rc_mesg") + "  " + wp.colStr("rc_mesg2"), true);
    btnDeleteOn(false);
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "ccam1020_detl")) {
    	btnModeAud("XX");
    	if(wp.colEq("reversal_flag", "Y")) {
    		btnDeleteOn(false);
    	}
    }
      

  }

  @Override
  public void initPage() {
    if (wp.iempty("ex_date1")) {
      String lsDate = commDate.dateAdd(wp.sysDate, 0, 0, -30);
      wp.colSet("ex_date1", lsDate);
    }
  }

}
