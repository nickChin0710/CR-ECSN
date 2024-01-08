/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 110-01-06  V2.00.03  Ryan        送oempay時 actCode = 2                      * 
* 110-01-04  V2.00.02  Ryan       key_table ==> LOST change to OPPOSITION    * 
* 109-09-07  V2.00.01  Ryan       program initial                            * 
* 110-10-26  V2.00.02  Justin        中止->終止
* 110-12-01  V2.00.03  ryan        update oempay iso          
* 110-12-29  V2.00.04  ryan        actCode 固定為2          
******************************************************************************/
package cmsm03;

import busi.FuncAction;
import busi.func.OutgoingOppo;

public class Cmsm2311Func extends FuncAction {
  String rowID = "", vCardNo = "", cardNo = "";
  @Override
  public void dataCheck() {
    rowID = wp.itemStr("rowid");
    cardNo = wp.itemStr("card_no");
    vCardNo = wp.itemStr("v_card_no");

    if (empty(rowID) && empty(vCardNo)) {
      errmsg("頁面空白時不可修改/刪除,請重新讀取資料");
      return;
    }


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


  @Override
  public int dbInsert() {

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    updateOempay();
//    getNegReason();
    insertCcaOutGoing();
    return rc;
  }

  @Override
  public int dbDelete() {
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  void updateOempay(){
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
	  strSql = " update oempay_card set status_code = :status_code , ";
	  strSql += " change_date = to_char(sysdate,'yyyymmdd') , ";
	  strSql += " mod_time = sysdate ," + " mod_pgm = :mod_pgm ";
	  strSql += " where v_card_no = :v_card_no ";
	  setString("status_code", statusCode);
	  setString("mod_pgm", "cmsm2311");
	  setString("v_card_no", vCardNo);
	  this.sqlExec(strSql);
	  if (sqlRowNum <= 0) {
	      errmsg("update oempay_card error, " + getMsg());
	  }
  }
  
	void insertCcaOutGoing() {
		String currentCode = "";
		String binType = wp.itemStr("bin_type"); 
		String newEndDate = wp.itemStr("new_end_date");
		String aReason = "3702";
//	    String actCode = "1";
		String actCode = "2";

		if(wp.itemStr("change_code").equals("2")){
		    currentCode = "0";
//		    actCode = "3";
		    aReason = "3703";
		}
	    if(wp.itemStr("change_code").equals("3")){
	    	currentCode = "1";
	    	aReason = "3701";
	    }
	    
	    OutgoingOppo ooOutgo = new OutgoingOppo();
	    ooOutgo.setConn(wp);
		ooOutgo.oppoOempayReq(cardNo,actCode,aReason,binType,newEndDate,vCardNo,currentCode);
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
		strSql += "	'OEMPAY' , ";
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
		strSql += " 'cmsm2311' , ";
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
		setString("current_code", currentCode);
		setString("new_end_date", wp.itemStr("new_end_date"));
		setString("oppost_date", wp.itemStr("oppost_date"));
		setString("oppost_reason", wp.itemStr("oppost_reason"));
		setString("reason_code", aReason);
		setString("bitmap", bitmap);
		setString("respCode", respCode);
		
		this.sqlExec(strSql);
		if (sqlRowNum <= 0) {
		    errmsg("update cca_outgoing error, " + getMsg());
		}
	}
	
//	void getNegReason(){
//		// -get outgo-reason-
//		
//		strSql = " select  neg_opp_reason as neg_reason ";
//		strSql += " from cca_opp_type_reason";
//		strSql += " where opp_status = :opp_status ";
//		setString("opp_status", wp.itemStr("oppost_reason"));
//		sqlSelect(strSql);
//
//		if (sqlRowNum > 0) {
//			isNegReason = colStr("neg_reason");
//		}
//	}

}
