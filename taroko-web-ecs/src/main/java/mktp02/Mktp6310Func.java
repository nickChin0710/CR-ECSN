/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/23  V1.00.00   machao      Initial                              *
* 112/04/19  V1.00.01   Zuwei Su      覆核後rate變為整數                              *
* 112-05-09  V1.00.03   Ryan    新增國內外消費欄位、ATM手續費回饋加碼欄位，特店中文名稱、特店英文名稱參數維護                            
* 112-07-28  V1.00.04   Ryan    新增只計算加碼回饋欄位維護                                                                        *
***************************************************************************/
package mktp02;

import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6310Func extends busi.FuncProc {
  private final String PROGNAME = "COMBO現金回饋參數檔覆核112/03/23  V1.00.00";
  String approveTabName = "PTR_COMBO_FUNDP";
  String controlTabName = "PTR_COMBO_FUNDP_T";

  public Mktp6310Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {}

  // ************************************************************************
  @Override
  public int dataProc() {
    return rc;
  }

  // ************************************************************************
  public int dbInsertA4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = " insert into  " + approveTabName+ " ("  //" aud_type, "
    		+ " fund_code, " + " fund_name, " + " fund_crt_date_s, " + " fund_crt_date_e, " 
    		+ " stop_flag, " + " stop_date, " + " stop_desc, " + " effect_months, " 
    		+ " acct_type_sel, " + " merchant_sel, " + " mcht_group_sel, "  + " platform_kind_sel, " 
    		+ " group_card_sel, " + " group_code_sel, " + " mcc_code_sel, " 
    		+ " bl_cond, " + " ca_cond, " + " id_cond, " + " ao_cond, " + " it_cond, " + " ot_cond, " 
    		+ " fund_feed_flag, "  + " threshold_sel, " + " purchase_type_sel, " 
    		+ " purchse_s_amt_1, " + " purchse_e_amt_1, " + " purchse_rate_1, " 
    		+ " purchse_s_amt_2, " + " purchse_e_amt_2, " + " purchse_rate_2, " 
    		+ " purchse_s_amt_3, " + " purchse_e_amt_3, "  + " purchse_rate_3, " 
    		+ " purchse_s_amt_4, " + " purchse_e_amt_4, " + " purchse_rate_4, " 
    		+ " purchse_s_amt_5, " + " purchse_e_amt_5, " + " purchse_rate_5, " 
    		+ " save_s_amt_1, " + " save_e_amt_1, " + " save_rate_1, "  
    		+ " save_s_amt_2, " + " save_e_amt_2, " + " save_rate_2, " 
    		+ " save_s_amt_3, " + " save_e_amt_3, " + " save_rate_3, " 
    		+ " save_s_amt_4, " + " save_e_amt_4, " + " save_rate_4, " 
    		+ " save_s_amt_5, "  + " save_e_amt_5, " + " save_rate_5, " 
    		+ " feedback_lmt, " + " feedback_type, " + " card_feed_run_day, " 
    		+ " cancel_period, " + " cancel_s_month, " + " cancel_unbill_type, " 
    		+ " cancel_unbill_rate, " + " cancel_event, "  + " apr_date, " + " apr_flag, " 
    		+ " apr_user, " + " crt_date, " + " crt_user, " + " mod_seqno, " + " mod_user, " 
    		+ " mod_time, " + " mod_pgm, "
    	    + " foreign_code, "
            + " mcht_cname_sel, "
            + " mcht_ename_sel,"
            + " atmibwf_cond,"
            + " onlyaddon_calcond "
    		+ " ) values (" 
//    		+ "?,"
    		+ "?,?,?,?,?,?,?,?,?,?,?," 
    		+ "?,?,?,?,?,?,?,?,?,?,?," 
    		+ "'2','1',?,?,?,?,?,?,?,?," 
    		+ "?,?,?,?,?,?,?,?,?,?," 
    		+ "?,?,?,?,?,?,?,?,?,?," 
    		+ "?,?,?,?,?,?,?,?,?,?," 
    		+ "to_char(sysdate,'yyyymmdd'),'Y',?,?,?,?,?," + "sysdate,?,?,?,?,?,?)";

	  Object[] param =new Object[]
	       {
//	    	   colStr("aud_type"),	   
		        colStr("fund_code"),
		        colStr("fund_name"),
		        colStr("fund_crt_date_s"),
		        colStr("fund_crt_date_e"),
		        colStr("stop_flag"),
		        colStr("stop_date"),
		        colStr("stop_desc"),
		        colStr("effect_months"),
		        colStr("acct_type_sel"),
		        colStr("merchant_sel"),
		        colStr("mcht_group_sel"),
		        colStr("platform_kind_sel"),
		        colStr("group_card_sel"),
		        colStr("group_code_sel"),
		        colStr("mcc_code_sel"),
		        colStr("bl_cond"),
		        colStr("ca_cond"),
		        colStr("id_cond"),
		        colStr("ao_cond"),
		        colStr("it_cond"),
		        colStr("ot_cond"),
		        colStr("fund_feed_flag"),
		        colInt("purchase_s_amt_1"),
		        colInt("purchase_e_amt_1"),
		        colNum("purchase_rate_1"),
		        colInt("purchase_s_amt_2"),
		        colInt("purchase_e_amt_2"),
		        colNum("purchase_rate_2"),
		        colInt("purchase_s_amt_3"),
		        colInt("purchase_e_amt_3"),
		        colNum("purchase_rate_3"), 
		        colInt("purchase_s_amt_4"),
		        colInt("purchase_e_amt_4"),
		        colNum("purchase_rate_4"), 
		        colInt("purchase_s_amt_5"),
		        colInt("purchase_e_amt_5"),
		        colNum("purchase_rate_5"), 
		        
		        colNum("save_s_amt_1"),
		        colNum("save_e_amt_1"),
		        colNum("save_rate_1"),
		        colNum("save_s_amt_2"),
		        colNum("save_e_amt_2"),
		        colNum("save_rate_2"),
		        colNum("save_s_amt_3"),
		        colNum("save_e_amt_3"),
		        colNum("save_rate_3"),
		        colNum("save_s_amt_4"),
		        colNum("save_e_amt_4"),
		        colNum("save_rate_4"),
		        colNum("save_s_amt_5"),
		        colNum("save_e_amt_5"),
		        colNum("save_rate_5"),
		        
		        colStr("feedback_lmt"),
		        colStr("feedback_type"),
		        colStr("card_feed_run_day"),
		        colStr("cancel_period"),
		        colStr("cancel_s_month"),
		        colStr("cancel_unbill_type"),
		        colInt("cancel_unbill_rate"),
		        colStr("cancel_event"),
		        wp.loginUser,
		        colStr("crt_date"), 
		        colStr("crt_user"),
		        colStr("mod_seqno"),
		        wp.loginUser,
		        wp.modPgm(),
		        colStr("foreign_code"),
		        colStr("mcht_cname_sel"),
		        colStr("mcht_ename_sel"),
		        colStr("atmibwf_cond"),
		        colStr("onlyaddon_calcond")
	       };
    
    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " fund_code, " + " fund_name, " + " fund_crt_date_s, " + " fund_crt_date_e, " 
    		+ " stop_date, " + " stop_desc, " + "stop_flag," + " effect_months, " + " acct_type_sel, " + " merchant_sel, " 
    		+ " mcht_group_sel, "  + " platform_kind_sel, " + " group_card_sel, " + " group_code_sel, " 
    		+ " bl_cond, " + " ca_cond, " + " id_cond, " + " ao_cond, " + " it_cond, " + " ot_cond, " 
    		+ " fund_feed_flag, "  + " threshold_sel, " + " purchase_type_sel, " 
    		+ " purchse_s_amt_1, " + " purchse_e_amt_1, " + " purchse_rate_1, " 
    		+ " purchse_s_amt_2, " + " purchse_e_amt_2, " + " purchse_rate_2, " 
    		+ " purchse_s_amt_3, " + " purchse_e_amt_3, "  + " purchse_rate_3, " 
    		+ " purchse_s_amt_4, " + " purchse_e_amt_4, " + " purchse_rate_4, " 
    		+ " purchse_s_amt_5, " + " purchse_e_amt_5, " + " purchse_rate_5, " 
    		+ " save_s_amt_1, " + " save_e_amt_1, " + " save_rate_1, "  
    		+ " save_s_amt_2, " + " save_e_amt_2, " + " save_rate_2, " 
    		+ " save_s_amt_3, " + " save_e_amt_3, " + " save_rate_3, " 
    		+ " save_s_amt_4, " + " save_e_amt_4, " + " save_rate_4, " 
    		+ " save_s_amt_5, "  + " save_e_amt_5, " + " save_rate_5, " 
    		+ " feedback_lmt, " + " feedback_type, " + " card_feed_run_day, " + " cancel_period, " + " cancel_s_month, " 
    		+ " cancel_unbill_type, " + " cancel_unbill_rate, " + " cancel_event, "  + " apr_date, " 
    		+ " apr_flag, " + " apr_user, " + " crt_date, " + " crt_user, " 
    		+ " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno,"
    		+ " foreign_code, "
    	    + " mcht_cname_sel,"
    	    + " mcht_ename_sel,"
    	    + " atmibwf_cond,"
    	    + " onlyaddon_calcond "
    		+ " from " + procTabName  + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String apr_flag = "Y";
    strSql = "update " + approveTabName + " set " 
//    + "aud_type = ?, " 
    		+ "fund_name = ?, " + "fund_crt_date_s = ?, "
            + "fund_crt_date_e = ?, " + "stop_flag = ?, " + "stop_date = ?, " + "stop_desc = ?, " + "effect_months = ?, " 
            + "acct_type_sel = ?, " + "merchant_sel = ?, " + "mcht_group_sel = ?, " + "platform_kind_sel = ?, "
            + "group_card_sel = ?, " + "group_code_sel = ?, " + "mcc_code_sel = ?, "
            + "bl_cond = ?, " + "ca_cond = ?, " + "id_cond = ?, " + "ao_cond = ?, " + "it_cond = ?, " + "ot_cond = ?, "
            + "fund_feed_flag = ?, " + "threshold_sel = ?, " + "purchase_type_sel = ?, "
            + "purchse_s_amt_1 = ?, " + "purchse_e_amt_1 = ?, " + "purchse_rate_1 = ?, " 
            + "purchse_s_amt_2 = ?, " + "purchse_e_amt_2 = ?, " + "purchse_rate_2 = ?, " 
            + "purchse_s_amt_3 = ?, " + "purchse_e_amt_3 = ?, " + "purchse_rate_3 = ?, "
            + "purchse_s_amt_4 = ?, " + "purchse_e_amt_4 = ?, " + "purchse_rate_4 = ?, " 
            + "purchse_s_amt_5 = ?, " + "purchse_e_amt_5 = ?, " + "purchse_rate_5 = ?, " 
            + "save_s_amt_1 = ?, " + "save_e_amt_1 = ?, " + "save_rate_1 = ?, "
            + "save_s_amt_2 = ?, " + "save_e_amt_2 = ?, " + "save_rate_2 = ?, "
            + "save_s_amt_3 = ?, " + "save_e_amt_3 = ?, " + "save_rate_3 = ?, "
            + "save_s_amt_4 = ?, " + "save_e_amt_4 = ?, " + "save_rate_4 = ?, "
            + "save_s_amt_5 = ?, " + "save_e_amt_5 = ?, " + "save_rate_5 = ?, "
            + "feedback_lmt = ?, " + "feedback_type = ?, " + "card_feed_run_day = ?, " + "cancel_period = ?, " 
            + "cancel_s_month = ?, " + "cancel_unbill_type = ?, " + "cancel_unbill_rate = ?, " + "cancel_event = ?, " 
            + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, "
            + "crt_user  = ?, "+ "crt_date  = ?, " + "mod_user  = ?, "
            + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ?,"
            + "foreign_code = ?, "
            + "mcht_cname_sel = ?, "
            + "mcht_ename_sel = ?,"
            + "atmibwf_cond = ?,"
            + "onlyaddon_calcond = ? "
            + "where fund_code = ? ";
    
      Object[] param = new Object[] {  //  colStr("aud_type"),
    		colStr("fund_name"), colStr("fund_crt_date_s"),
            colStr("fund_crt_date_e"), colStr("stop_flag"), colStr("stop_date"),
            colStr("stop_desc"), colStr("effect_months"), 
            colStr("acct_type_sel"), colStr("merchant_sel"),colStr("mcht_group_sel"),
            colStr("platform_kind_sel"), colStr("group_card_sel"), colStr("group_code_sel"),colStr("mcc_code_sel"),
            colStr("bl_cond"),colStr("ca_cond"), colStr("id_cond"),
            colStr("ao_cond"), colStr("it_cond"), colStr("ot_cond"),
            colStr("fund_feed_flag"), colStr("threshold_sel"), colStr("purchase_type_sel"),
            colInt("purchase_s_amt_1"), colInt("purchase_e_amt_1"), colNum("purchase_rate_1"),
            colInt("purchase_s_amt_2"), colInt("purchase_e_amt_2"), colNum("purchase_rate_2"),
            colInt("purchase_s_amt_3"), colInt("purchase_e_amt_3"), colNum("purchase_rate_3"),
            colInt("purchase_s_amt_4"), colInt("purchase_e_amt_4"), colNum("purchase_rate_4"),
            colInt("purchase_s_amt_5"), colInt("purchase_e_amt_5"), colNum("purchase_rate_5"),
            colNum("save_s_amt_1"), colNum("save_e_amt_1"), colNum("save_rate_1"),
            colNum("save_s_amt_2"), colNum("save_e_amt_2"), colNum("save_rate_2"),
            colNum("save_s_amt_3"), colNum("save_e_amt_3"), colNum("save_rate_3"),
            colNum("save_s_amt_4"), colNum("save_e_amt_4"), colNum("save_rate_4"),
            colNum("save_s_amt_5"), colNum("save_e_amt_5"), colNum("save_rate_5"),
            colInt("feedback_lmt"), colStr("feedback_type"), colStr("card_feed_run_day"),
            colStr("cancel_period"), colStr("cancel_s_month"), colStr("cancel_unbill_type"),
            colInt("cancel_unbill_rate"), colStr("cancel_event"), wp.loginUser, apr_flag,
            colStr("crt_user"), colStr("crt_date"),wp.loginUser,
            colStr("mod_pgm"), 
            colStr("foreign_code"),
            colStr("mcht_cname_sel"),
            colStr("mcht_ename_sel"),
            colStr("atmibwf_cond"),
            colStr("onlyaddon_calcond"),
            colStr("fund_code")};
        
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and fund_code = ? ";

    Object[] param = new Object[] {colStr("fund_code")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + approveTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_parm_data " + "where 1 = 1 " + "and table_name  =  'PTR_COMBO_FUNDP' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("fund_code"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_parm_data 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_parm_data_t " + "where 1 = 1 " + "and table_name  =  'PTR_COMBO_FUNDP' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("fund_code"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_parm_data_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into mkt_parm_data " + "select * " + "from  mkt_parm_data_t " + "where 1 = 1 "
        + "and table_name  =  'PTR_COMBO_FUNDP' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("fund_code"),};

    sqlExec(strSql, param);

    return 1;
  }

//************************************************************************
public int dbDeleteD4TBnCdata() throws Exception {
  rc = dbSelectS4();
  if (rc != 1)
    return rc;
strSql = "delete from mkt_parm_cdata_t "
        + "where 1 = 1 "
        + "and table_name  =  'PTR_COMBO_FUNDP' "
        + "and data_key  = ?  ";

  Object[] param = new Object[] 
  		{
  		colStr("fund_code")
  		};
  
  wp.dupRecord = "Y";
  sqlExec(strSql, param);
  if (rc != 1)
    errmsg("刪除 mkt_parm_cdata_t 錯誤");

  return 1;
}

// ************************************************************************
public int dbInsertA4BnCdata() throws Exception {
  rc = dbSelectS4();
  if (rc != 1)
    return rc;
strSql = "insert into mkt_parm_cdata "
        + "select * "
        + "from  mkt_parm_cdata_t "
        + "where 1 = 1 "
        + "and table_name  =  'PTR_COMBO_FUNDP' "
        + "and data_key  = ?  ";

  Object[] param = new Object[] 
  		{
  		   colStr("fund_code"),
  		};

  sqlExec(strSql, param);

  return 1;
}
// ************************************************************************
public int dbDeleteD4BnCdata() throws Exception {
  rc = dbSelectS4();
  if (rc != 1)
      return rc;
  strSql = "delete from mkt_parm_cdata "
          + "where 1 = 1 "
          + "and table_name  =  'PTR_COMBO_FUNDP' "
          + "and data_key  = ?  ";

  Object[] param = new Object[] 
  		{
  		colStr("fund_code"),
  		};
  
  wp.dupRecord = "Y";
  sqlExec(strSql, param);
  if (rc != 1)
    errmsg("刪除 mkt_parm_cdata 錯誤");

  return 1;
}
  // ************************************************************************
  public int dbDelete() {
    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

}  // End of class
