/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 110-10-25  V1.00.08  Justin     if change_code == 3, current_code = 2      *
* 110-10-07  V1.00.07  Justin     INSERT CCA_OUTGOING values CURRENT_CODE=2, 
*                                 OPPOST_DATE=SysDate, OPPOST_REASON=C1, resp_code=''*
* 110-01-04  V1.00.06  Ryan       key_table ==> LOST change to OPPOSITION    * 
* 109-07-30  V1.00.05  Ryan       取消覆核直接update hce ,add cca_outgoing   * 
* 109-04-20  V1.00.04  shiyuqi       updated for project coding standard     *
* 108-11-19  V1.00.01  Alex       bug fixed                                  *
* 108-11-25  V1.00.02  Alex       dataCheck Fixed                            *
* 108-12-23  V1.00.03  Alex       dataCheck fixed                            * 
******************************************************************************/
package cmsm03;

import busi.FuncAction;
import busi.func.OutgoingOppo;

public class Cmsm2310Func extends FuncAction {
  String rowID = "", vCardNo = "", cardNo = "";
  String isNegReason = "41";
  @Override
  public void dataCheck() {
    rowID = wp.itemStr("rowid");
    cardNo = wp.itemStr("card_no");
    vCardNo = wp.itemStr("v_card_no");

    if (empty(rowID) && empty(vCardNo)) {
      errmsg("頁面空白時不可修改/刪除,請重新讀取資料");
      return;
    }

//    if (!empty(wp.itemStr("apr_date"))) {
//      errmsg("主管已覆核  不可修改/刪除");
//      return;
//    }
//
//    if (!empty(wp.itemStr("proc_date"))) {
//      errmsg("資料已傳送處理, 不可異動");
//      return;
//    }
//
//    if (this.ibDelete) {
//      return;
//    }

    if (wp.itemEmpty("change_code")) {
      errmsg("資料未異動 不可存檔");
      return;
    }

    if (pos("|2|3|4|5", wp.itemStr("status_code")) > 0) {
      errmsg("TPAN 狀態已停用, 不可再異動");
      return;
    }


    if (pos("|1|3", wp.itemStr("change_code")) > 0 && !eqIgno(wp.itemStr("status_code"), "0")) {
      errmsg("TPAN狀態 不是 [正常], 不可暫停 or 終止");
      return;
    }

    if (eqIgno(wp.itemStr("change_code"), "2") && !eqIgno(wp.itemStr("status_code"), "1")) {
      errmsg("TPAN狀態 不是 [暫停], 不可恢復");
      return;
    }

//    if (this.ibAdd) {
//      if (checkData() == false) {
//        errmsg("TPAN 有異動未傳送處理, 不可再新增異動");
//        return;
//      }
//    }

  }

//  boolean checkData() {
//    String sql1 = "select count(*) as db_cnt " + " from hce_status_entry " + " where v_card_no =?"
//        + " and proc_date =''";
//    sqlSelect(sql1, new Object[] {vCardNo});
//
//    if (colNum("db_cnt") > 0)
//      return false;
//    return true;
//  }

  @Override
  public int dbInsert() {
//    actionInit("A");
//    dataCheck();
//    if (rc != 1) {
//      return rc;
//    }
//    strSql = "insert into hce_status_entry (" + " crt_date ," + " crt_time ," + " apr_date ," +  " v_card_no ,"
//        + " card_no ," + " new_end_date ," + " status_code ," + " sir_status ," + " id_p_seqno ,"
//        + " change_code ," + " user_note ," + " proc_flag ," + " crt_user ," + " apr_user ,"+ " mod_user ,"
//        + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ("
//        + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ," + " to_char(sysdate,'yyyymmdd') ," + " :v_card_no ,"
//        + " :card_no ," + " :new_end_date ," + " :status_code ," + " :sir_status ,"
//        + " :id_p_seqno ," + " :change_code ," + " :user_note ," + " 'N' ," + " :crt_user ,"
//        + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " '1' " + " )";
//    item2ParmStr("v_card_no");
//    item2ParmStr("card_no");
//    item2ParmStr("new_end_date");
//    item2ParmStr("status_code");
//    item2ParmStr("sir_status");
//    item2ParmStr("id_p_seqno");
//    item2ParmStr("change_code");
//    item2ParmStr("user_note");
//    setString("crt_user", wp.loginUser);
//    setString("apr_user", wp.loginUser);
//    setString("mod_user", wp.loginUser);
//    setString("mod_pgm", "cmsm2310");
//    this.sqlExec(strSql);
//    if (sqlRowNum <= 0) {
//      errmsg("Insert hce_status_entry error, " + getMsg());
//    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
//    log("rowid:" + wp.itemStr("rowid"));
//    strSql = " update hce_status_entry set " + " change_code =:change_code ,"
//        + " user_note =:user_note ," + " proc_flag ='N' ," + " mod_user =:mod_user ,"
//        + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 "
//        + " where hex(rowid) =:rowid ";
//    item2ParmStr("change_code");
//    item2ParmStr("user_note");
//    setString("mod_user", wp.loginUser);
//    setString("mod_pgm", "cmsm2310");
//    item2ParmStr("rowid");
//    this.sqlExec(strSql);
//
//    if (sqlRowNum <= 0) {
//      errmsg("Update hce_status_entry error, " + getMsg());
//    }
    updateHce();
    getNegReason();
    insertCcaOutGoing("1");
    return rc;
  }

  @Override
  public int dbDelete() {
//    actionInit("D");
//    dataCheck();
//    if (rc != 1) {
//      return rc;
//    }
//
//    strSql = "Delete hce_status_entry " + " where hex(rowid) =:rowid ";
//    item2ParmStr("rowid");
//    sqlExec(strSql);
//    if (sqlRowNum <= 0) {
//      errmsg("delete hce_status_entry err=" + getMsg());
//      rc = -1;
//    } else
//      rc = 1;
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  void updateHce(){
	  String changeCode = wp.itemStr("change_code");
	  String statusCode = "";
	  if(changeCode.equals("1")){
		  statusCode = "1";
	  }
	  if(changeCode.equals("2")){
		  statusCode = "0";
	  }
	  if(changeCode.equals("3")){
		  statusCode = "2";
	  }
	  strSql = " update hce_card set status_code = :status_code , ";
	  strSql += " change_date = to_char(sysdate,'yyyymmdd') , ";
	  strSql += " mod_time = sysdate ," + " mod_pgm = :mod_pgm ";
	  strSql += " where v_card_no = :v_card_no ";
	  setString("status_code", statusCode);
	  setString("mod_pgm", "cmsm2310");
	  setString("v_card_no", vCardNo);
	  this.sqlExec(strSql);
	  if (sqlRowNum <= 0) {
	      errmsg("update hce_card error, " + getMsg());
	  }

  }
  
	void insertCcaOutGoing(String actCode) {
		String currentCode = "";
		if(wp.itemStr("change_code").equals("2")){
		    currentCode = "0";
		}
	    if(wp.itemStr("change_code").equals("3")){
//	    	currentCode = "1"; // 20221/10/25 Justin [phase2]
	    	currentCode = "2"; // 20221/10/25 Justin [phase2]
	    }
	    
	    OutgoingOppo ooOutgo = new OutgoingOppo();
	    ooOutgo.setConn(wp);
		ooOutgo.oppoTwmpReq(vCardNo,actCode,isNegReason,currentCode);
		String bitmap = ooOutgo.getBitmap();
		String respCode = ooOutgo.respCode;
		
		strSql =  "INSERT INTO cca_outgoing( ";
		strSql += "crt_date , ";
		strSql += "crt_time , ";
		strSql += "card_no , ";
		strSql += "v_card_no , ";
		strSql += "key_value , ";
		strSql += "key_table , ";
		strSql += "act_code , ";
		strSql += "crt_user , ";
		strSql += "proc_flag , ";
		strSql += "send_times , ";
		strSql += "data_from , ";
		strSql += "data_type , ";
		strSql += "bin_type , ";
		strSql += "vip_amt , ";
		strSql += "mod_time , ";
		strSql += "mod_pgm , ";
		strSql += "electronic_card_no , ";
		strSql += "current_code , ";
		strSql += "new_end_date , ";
		strSql += "oppost_date , ";
		strSql += "oppost_reason, ";
		strSql += "reason_code, ";
		strSql += "bitmap, ";
		strSql += "resp_code ";
		strSql += "	) ";
		strSql += " VALUES ( ";
		strSql += "	to_char(sysdate,'yyyymmdd'), ";
		strSql += " to_char(sysdate,'hh24miss'), ";
		strSql += "	:card_no , ";
		strSql += "	:v_card_no , ";
		strSql += "	'TWMP' , ";
		strSql += "	'OPPOSITION' , ";
		strSql += "	:act_code , ";
		strSql += "	:crt_user , ";
		strSql += "	'1' , ";
		strSql += "	'1' , ";
		strSql += "	'1' , ";
		strSql += "	'OPPO' , ";
		strSql += "	:bin_type , ";
		strSql += "	'0' , ";
		strSql += "	to_char(sysdate,'yyyymmddhh24miss'), ";
		strSql += " 'cmsm2310' , ";
		strSql += "	'' , ";
		strSql += "	:current_code , ";
		strSql += "	:new_end_date , ";
		strSql += "	:oppost_date , ";
		strSql += "	:oppost_reason , ";
		strSql += " :reason_code, ";
		strSql += " :bitmap, ";
		strSql += " :respCode ";
		strSql += ")";
		setString("card_no", cardNo);
		setString("v_card_no", vCardNo);
		setString("act_code", actCode);
		setString("crt_user", wp.loginUser);
		setString("bin_type", wp.itemStr("bin_type"));
		// 2021/10/07 Justin
		// [phase2] INSERT CCA_OUTGOING的CURRENT_CODE固定放“2”、 OPPOST_DATE放系統日、OPPOST_REASON 固定放“C1, resp_code固定放空白
//		setString("current_code", currentCode);                  // [phase 3] 2021/10/07 Justin
		setString("current_code", "2");                          // [phase 2] 2021/10/07 Justin
		setString("new_end_date", wp.itemStr("new_end_date"));
//		setString("oppost_date", wp.itemStr("oppost_date"));     // [phase 3] 2021/10/07 Justin
		setString("oppost_date", sysDate);                       // [phase 2] 2021/10/07 Justin
//		setString("oppost_reason", wp.itemStr("oppost_reason")); // [phase 3] 2021/10/07 Justin
		setString("oppost_reason", "C1");                        // [phase 2] 2021/10/07 Justin
		setString("reason_code", isNegReason);
		setString("bitmap", bitmap);
//		setString("respCode", respCode);                         // [phase 3] 2021/10/07 Justin
		setString("respCode", "");                               // [phase 2] 2021/10/07 Justin
		
		this.sqlExec(strSql);
		if (sqlRowNum <= 0) {
		    errmsg("insert cca_outgoing error, " + getMsg());
		}
	}
	
	void getNegReason(){
		// -get outgo-reason-
		
		strSql = " select  neg_opp_reason as neg_reason ";
		strSql += " from cca_opp_type_reason";
		strSql += " where opp_status = :opp_status ";
		setString("opp_status", wp.itemStr("oppost_reason"));
		sqlSelect(strSql);

		if (sqlRowNum > 0) {
			isNegReason = colStr("neg_reason");
		}

	}
}
