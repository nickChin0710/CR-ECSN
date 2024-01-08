/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ * 
* 111-04-19  V1.00.01  machao     TSC畫面整合    *
******************************************************************************/
package tscm01;
/* 2019-0624:  JH    p_xxx >>acno_p_xxx
 * 109-04-28  V1.00.01  Tanwei       updated for project coding standard
* */
import ofcapp.BaseAction;
import taroko.base.CommString;

public class Tscq0060 extends BaseAction {
  String lsWhere = "", lsWhere2 = "";

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
    if (this.chkStrend(wp.itemStr("ex_rm_date1"), wp.itemStr("ex_rm_date2")) == false) {
      alertErr2("指定日期起迄：輸入錯誤");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_send_date1"), wp.itemStr("ex_send_date2")) == false) {
      alertErr2("傳送日期起迄：輸入錯誤");
      return;
    }
    if (itemallEmpty(new String[] {"ex_send_date1", "ex_send_date2", "ex_rm_date1", "ex_rm_date2",
        "ex_card_no", "ex_idno"})) {
      alertErr2("[指定日期, 傳送日期, 身分證ID, 卡號] 不可全部空白");
      return;
    }
    
    lsWhere = " where 1=1 "
    		+ sqlCol(wp.itemStr("ex_send_date1"),"rm_send_date",">=")
    		+ sqlCol(wp.itemStr("ex_send_date2"),"rm_send_date","<=")
    		+ sqlCol(wp.itemStr("ex_card_no"),"card_no","like%")
    		+ sqlCol(wp.itemStr("ex_crt_user"),"mod_user")
    		;
    
    if(wp.itemEmpty("ex_idno") == false) {
    	lsWhere += " and acno_p_seqno in (select acno_p_seqno from act_acno where 1=1 " +sqlCol(wp.itemStr("ex_idno"),"acct_key","like%")+")";
    }    

    if (wp.itemEq("ex_risk_class", "1")) {
      lsWhere += " and risk_class ='57' ";
    } else if (wp.itemEq("ex_risk_class", "2")) {
      lsWhere += " and risk_class ='04' ";
    } else if (wp.itemEq("ex_risk_class", "3")) {
      lsWhere += " and restore_date <>'' ";
    }

    if (wp.itemEq("ex_resp_code", "1")) {
      lsWhere += " and rpt_resp_code ='00' ";
    } else if (wp.itemEq("ex_resp_code", "2")) {
      lsWhere += " and rpt_resp_code <>'00' ";
    }

    if (!wp.itemEq("ex_send_reason", "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_send_reason"), "send_reason");
    }
    
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }
  
  void getWhereStr2() {	  
	  lsWhere2 = " where 1=1 " 
			   + sqlCol(wp.itemStr("ex_send_date1"), "crt_date", ">=")
			   + sqlCol(wp.itemStr("ex_send_date2"), "crt_date", "<=")
			   + sqlCol(wp.itemStr("ex_rm_date1"), "remove_date", ">=")
			   + sqlCol(wp.itemStr("ex_rm_date2"), "remove_date", "<=")
			   + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%");
			   ;
	  if(wp.itemEmpty("ex_idno") == false) {		  
		  lsWhere2 += " and acno_p_seqno in (select acno_p_seqno from act_acno where 1=1 " +sqlCol(wp.itemStr("ex_idno"),"acct_key","like%")+")";
	  }  			
			   
	  if (wp.itemEq("ex_risk_class", "1")) {
		  lsWhere2 += " and risk_class ='57' ";
	  } else if (wp.itemEq("ex_risk_class", "2")) {
		  lsWhere2 += " and risk_class ='04' ";
	  } else if (wp.itemEq("ex_risk_class", "3")) {
		  lsWhere2 += " and txn_code = 'D' ";
	  }

	  if (wp.itemEq("ex_resp_code", "1")) {
		  lsWhere2 += " and rpt_resp_code ='00' ";
	  } else if (wp.itemEq("ex_resp_code", "2")) {
		  lsWhere2 += " and rpt_resp_code <>'00' ";
	  }
  }
  
  @Override
  public void queryRead() throws Exception {

    wp.pageControl();
    daoTid = "A.";
    wp.selectSQL = "" + " card_no  , " + " tsc_card_no , " + " risk_class , " + " send_reason , "
        + " remove_date , " + " rm_send_date , " + " rm_send_time , " + " acct_type , "
        + " uf_acno_key(acno_p_seqno) as acct_key , " + " restore_date , " + " mod_user , "
        + " rpt_resp_code, "
        + " decode(risk_class,'57','拒絕代行','04','鎖卡','00','取消') as memo_risk_class, "
        + " decode(send_reason,'10','黑名單','20','凍結','30','維護特指','40','人工指定') as memo_send_reason ";
    // 取消代行授權資料轉檔
    wp.daoTable = " tsc_rm_actauth ";
    wp.whereOrder = " order by remove_date Desc, rm_send_date desc , rm_send_time desc, card_no "
        + commSqlStr.rownum(200);

    pageQuery();
    sqlParm.clear();
    wp.setListCount(1);
    // list1_wkdata();
    queryRead2();
  }

  void queryRead2() throws Exception {	  
	  getWhereStr2();
	  wp.whereStr = lsWhere2;
	  daoTid = "B.";
	  wp.selectSQL = "" + " crt_date , " + " crt_time , " + " card_no , " + " tsc_card_no , "
			  	   + " txn_code , " + " remove_date , " + " rpt_resp_code , " + " rpt_resp_date , "
			  	   + " acct_type , " + " uf_acno_key(acno_p_seqno) as acct_key , "
			  	   + " uf_nvl(risk_class,'xx') as risk_class ";
	  wp.daoTable = " tsc_bkti_hst ";
	  wp.whereOrder = " order by crt_date Desc, crt_time Desc, card_no Asc " + commSqlStr.rownum(200);

	  pageQuery();
	  wp.setListCount(2);
    // list2_wkdata();
  }

  // void list1_wkdata(){
  // for(int ii=0 ;ii<wp.selectCnt;ii++){
  // wp.col_set(ii,"A.wk_acct_key", wp.col_ss(ii,"A.acct_type")+"-"+wp.col_ss(ii,"A.acct_key"));
  // }
  // }

  // void list2_wkdata(){
  // for(int ii=0 ;ii<wp.selectCnt;ii++){
  // wp.col_set(ii,"B.wk_acct_key", wp.col_ss(ii,"B.acct_type")+"-"+wp.col_ss(ii,"B.acct_key"));
  // }
  // }

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
