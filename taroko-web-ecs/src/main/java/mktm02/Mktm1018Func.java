package mktm02;
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 110-07-23  V1.00.01  Machao       新增功能: mktm1018_市區停車請款匯入作業     *
*                                                                                 *  
******************************************************************************/
import busi.FuncAction;
import busi.ecs.CommRoutine;

public class Mktm1018Func extends FuncAction{
	
	CommRoutine comr = new CommRoutine();
	  @Override
	  public void dataCheck() {
	    // TODO Auto-generated method stub

	  }

	  @Override
	  public int dbInsert() {
	    // TODO Auto-generated method stu
	    return 0;
	  }

	  @Override
	  public int dbUpdate() {
	    // TODO Auto-generated method stub
	    return 0;
	  }

	  @Override
	  public int dbDelete() {
	    // TODO Auto-generated method stub
	    return 0;
	  }

	  @Override
	  public int dataProc() {
	    // TODO Auto-generated method stub
	    return 0;
	  }


	  public int insertRighList() {
		  comr.setConn(this.wp);
		    msgOK();
		    //執行代碼
		    strSql = " insert into mkt_dodo_resp_t (" + " tran_date ," + " tran_time ,"
		  	          + " tran_seqno ," + " park_vendor ," + " park_date_s ," + " park_time_s ," + " park_date_e ,"
		  	          + " park_time_e ," + " card_no ," + " acct_type ," + " p_seqno ," + " id_p_seqno ,"
		  	          + " station_id ," + " park_hr ," + " free_hr ," + " charge_amt ,"
		  	          + " use_bonus_hr ," + " use_point ," + " manual_reason ," + " pass_type ," + " err_code ,"
		  	          + " act_use_point ," + " act_charge_amt ," + " imp_date ," + " verify_flag ,"
		  	          + " verify_remark ," + " file_name ," + " data_date ," + " proc_flag ,"
		  	          + " proc_date ," + " aud_type ," + " chg_remark ," + " crt_date ,"
		  	          + " crt_user ," + " apr_date ," + " apr_flag ," + " mod_user ," + " mod_time ,"
		  	          + " mod_pgm ," + " mod_seqno ," + " id_no ," + " action_cd ," + " data_time ,"
		  	          + " filed_t ," + " station_name " + " )"
		  	          + " values ( " + " to_char(sysdate,'yyyymmdd'), " + " to_char(sysdate,'hh24miss') ," 
		  	          + " :tran_seqno ," + " :park_vendor ," + " :park_date_s ," + " :park_time_s ," + " :park_date_e ,"
		  	          + " :park_time_e ," + " :card_no ," + " :acct_type ," + " :p_seqno ," + " :id_p_seqno ,"
		  	          + " :station_id ," + " :park_hr ," + " :free_hr ," + " 0 ,"
		  	          + " 0 ," + " 0 ," + " :manual_reason ," + " 3 ," + " :err_code ," 
		  	          + " 0 ," + " 0 ," + " to_char(sysdate,'yyyymmdd'), " + " 0 ," 
		  	          + " :verify_remark ," + " :file_name ," + " :data_date ," + " 'N '," 
		  	          + " to_char(sysdate,'yyyymmdd'), " + " 'A '," + " :chg_remark ," + " to_char(sysdate,'yyyymmdd'), " 
		  	          + " :crt_user ,"+ " :apr_date ," + " 0 ," + " :mod_user ," + " sysdate, "
		  	          + " :mod_pgm ," + " 1 ," + " :id_no ," + " :action_cd ," + " :data_time ," 
		  	          + " :filed_t ," + " :station_name " + " ) ";
		    
		    setString("tran_seqno", comr.getSeqno("ECS_DBMSEQ"));	    
		    item2ParmStr("park_vendor");
		    item2ParmStr("park_date_s");
		    item2ParmStr("park_time_s");
		    item2ParmStr("park_date_e");
		    item2ParmStr("park_time_e");
		    item2ParmStr("card_no");
		    item2ParmStr("acct_type");
		    item2ParmStr("p_seqno");
		    item2ParmStr("id_p_seqno");
		    item2ParmStr("station_id");
		    item2ParmNum("park_hr");
		    item2ParmNum("free_hr");
		    item2ParmStr("manual_reason");
		    item2ParmStr("err_code");		    
		    item2ParmStr("verify_remark");
		    item2ParmStr("file_name");
		    item2ParmStr("data_date");		    
		    item2ParmStr("chg_remark");		    
		    setString("crt_user", wp.loginUser);		    
		    item2ParmStr("apr_date");		    
//		    item2ParmStr("apr_flag");
		    setString("mod_user", wp.loginUser);
		    setString("mod_pgm", wp.modPgm());
		    item2ParmStr("id_no");		    
		    item2ParmStr("action_cd");
		    item2ParmStr("data_time");
		    item2ParmStr("filed_t");
		    item2ParmStr("station_name");	
		    
		    
		    sqlExec(strSql);
		    
		    if (sqlRowNum <= 0) {
		    	errmsg("insert mkt_dodo_resp_t error ");
		    }

		    return rc;
		  }
	  
	  
	  
	  
}
