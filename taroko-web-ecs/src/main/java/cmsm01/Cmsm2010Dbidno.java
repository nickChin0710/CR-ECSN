package cmsm01;
/* 
 *2019-0419:  JH    modify
 *109-04-19  V1.00.06  shiyuqi         updated for project coding standard  
 *109-08-20  V1.00.07  JustinWu     add checkbox和radio的比較
 *109-08-21  V1.00.08  JustinWu     dbc_idno: update mod_user, mod_pgm, mod_time, and mod_seqno 
 *109-09-09  V1.00.10  JustinWu     主檔apr_user=approveUser, mod_user=modifyUser, and update_user=modifyUser
 *109-09-14  V1.00.11   JustinWu    update column apr_time -> apr_date
 *109-09-15   V1.00.12  JustinWu    修正覆核錯誤原因
 *109-12-16   V1.00.13  JustinWu    add cleanModCol()
 *109-12-31   V1.00.14   shiyuqi       修改无意义命名 
 *109-01-06   V1.00.15  JustinWu    updated for XSS
 *110-03-01   V1.00.16  JustinWu    ignore to check some columns
 *110-10-05   V1.00.17  JustinWu    add zip2
 *111-02-24   V1.00.18  JustinWu    update C_PHONE_CHG_DATE and C_PHONE_CHG_TIME
 *111-03-02   V1.00.19  JustinWu    新增異動日期及時間在待覆核資訊
 *111-03-03   V1.00.20  JustinWu    trim() compared columns
 * */

import busi.FuncAction;

public class Cmsm2010Dbidno extends FuncAction {
  String idPSeqno = "";
  boolean dbIdnoExt = false;
  String lsHomeNo = "";
  String lsOfficeNo = "";
  String lsCellPhone = "";
  String pgm = "cmsm2010";
  
  // all types of input except checkbox and radio
  String[] aaCol = new String[] {" office_area_code1 ", " office_tel_no1 ", " office_tel_ext1 ",
      " office_area_code2 ", " office_tel_no2 ", " office_tel_ext2 ", " home_area_code1 ",
      " home_tel_no1 ", " home_tel_ext1 ", " home_area_code2 ", " home_tel_no2 ",
      " home_tel_ext2 ", " cellar_phone "
      // " promote_emp_no "
  };
  
  // checkbox and radio button
  String[] checkCol = {"market_agree_base", "acs_email_flag", "e_news" ,"accept_mbullet" , 
  			                           "accept_dm" , "accept_sms" , "accept_call_sell" , "tsc_market_flag"
//  			                           ,"staff_flag","credit_flag","salary_code", "comm_flag"
  			                           };

  // **2017/11/21 Alex************************************************************
  public int dbIdnoOnline() {
    if (empty(wp.colStr("id_p_seqno"))) {
      errmsg("身分證流水號: 不可空白");
      return -1;
    }

    strSql = "select " 
        + " office_area_code1 , " 
    	+ " office_tel_no1 , " 
        + " office_tel_ext1 ,"
        + " office_area_code2 ," 
        + " office_tel_no2 ," 
        + " office_tel_ext2 ," 
        + " home_area_code1 ,"
        + " home_tel_no1 ," 
        + " home_tel_ext1 ," 
        + " home_area_code2 ," 
        + " home_tel_no2 ,"
        + " home_tel_ext2 ," 
        + " cellar_phone ," 
        + " e_news ," 
        + " accept_mbullet ,"
        + " accept_dm ," 
        + " accept_sms ," 
        + " accept_call_sell ," 
        + " promote_emp_no, "
        + " market_agree_base , " 
        + " tsc_market_flag, " 
        + " tsc_market_date, "
        + " mod_user, "
        + " mod_date, "
        + " mod_time2, "
        + " to_char(to_date(MOD_DATE || MOD_TIME2, 'yyyy/mm/dd hh24:mi:ss') , 'yyyy/mm/dd hh24:mi:ss') as userModDateTime "
        + " from dbs_modlog_main " 
        + " where id_p_seqno =?"
        + " and nvl(apr_flag,'N')<>'Y' " 
        + " and mod_table = 'DBC_IDNO' ";
    this.sqlSelect(strSql, new Object[] {wp.colStr("id_p_seqno")});
    if (sqlRowNum <= 0) {
      return 0;
    }

    wp.colSet("mod_user", colStr("mod_user"));
    wp.colSet("mod_date", colStr("mod_date"));
    wp.colSet("mod_time2", colStr("mod_time2"));
    wp.colSet("userModDateTime", this.colStr("userModDateTime"));

    return checkModifyDbAppr();
  }

  int checkModifyDbAppr() {
    int liMod = 0;

    for (int ii = 0; ii < aaCol.length; ii++) {
			String col = aaCol[ii].trim();
			if (this.colEqIgno(col, wp.colStr(col)) == true) {
				wp.colSet("mod_" + col, "");
				continue;
			}
			
      wp.colSet("mod_" + col, "background-color: rgb(255,191,128)");
      wp.colSet(col, colStr(col));
      liMod = 1;
    }
    
    for (int ii = 0; ii < checkCol.length; ii++) {
			String col = checkCol[ii].trim();
			if (this.colNvl(col, "N").equals(wp.colNvl(col, "N"))) {
				wp.colSet("mod_" + col, "");
				continue;
			}
		
      wp.colSet("mod_" + col, "background-color: rgb(255,191,128)");
      wp.colSet(col, colStr(col));
      liMod = 1;
    }

    return liMod;
  }

  void delDbonline() {
    strSql = "delete dbs_modlog_main " + " where id_p_seqno =:id_p_seqno "
        + " and mod_table='DBC_IDNO' " + " and apr_flag<>'Y' ";

    item2ParmStr("id_p_seqno");
    sqlExec(strSql);
  }

  public void cmsp2010SetModifyData() {
    dateTime();
    if (wp.itemEmpty("mod_e_mail_addr") == false) {
      wp.colSet("e_mail_from_mark", "M");
      wp.colSet("e_mail_chg_date", this.sysDate);
    }
    wp.colSet("wk_dm_flag", "");
    if (wp.itemEmpty("mod_e_news") == false) {
      wp.colSet("wk_dm_flag", "1");
      wp.colSet("e_new_from_mark", "M");
      wp.colSet("e_news_chg_date", sysDate);
    }
    if (wp.itemEmpty("mod_accept_mbullet") == false) {
      wp.colSet("wk_dm_flag", "1");
      wp.colSet("mbullet_from_mark", "M");
      wp.colSet("mbullet_chg_date", sysDate);
    }
    if (wp.itemEmpty("mod_accept_dm") == false) {
      wp.colSet("wk_dm_flag", "1");
      wp.colSet("dm_from_mark", "M");
      wp.colSet("dm_chg_date", sysDate);
    }
    if (wp.itemEmpty("mod_accept_sms") == false) {
      wp.colSet("wk_dm_flag", "1");
      wp.colSet("sms_from_mark", "M");
      wp.colSet("sms_chg_date", sysDate);
    }
    if (wp.itemEmpty("mod_accept_call_sell") == false) {
      wp.colSet("wk_dm_flag", "1");
      wp.colSet("call_sell_from_mark", "M");
      wp.colSet("call_sell_chg_date", sysDate);
    }
    if (wp.itemEmpty("mod_tsc_market_flag") == false) {
      wp.colSet("tsc_market_date", sysDate);
    }
    /*
    * */
  }

  int checkModifyDbIdext() {
    dbIdnoExt = true;
    // -returnVal: 1.有異動, 0.資料不存在, -1.未異動-
    strSql = "select * from dbc_idno_ext" + " where id_p_seqno =? ";
    sqlSelect(strSql, new Object[] {idPSeqno});
    if (sqlRowNum <= 0) {
      dbIdnoExt = false;
    }

    String[] aaCol = new String[] {"asig_inq1_cname", "asig_inq1_idno", "asig_inq1_telno",
        // "asig_inq1_acct",
        // "asig_inq1_card",
        // "asig_inq1_limit",
        "asig_inq1_chk_data", "asig_inq2_cname", "asig_inq2_idno", "asig_inq2_telno",
        // "asig_inq2_acct",
        // "asig_inq2_card",
        // "asig_inq2_limit",
        "asig_inq2_chk_data", "asig_inq3_cname", "asig_inq3_idno", "asig_inq3_telno",
        // "asig_inq3_acct",
        // "asig_inq3_card",
        // "asig_inq3_limit",
        "asig_inq3_chk_data"};

    String col = "";
    for (int ii = 0; ii < aaCol.length; ii++) {
      col = aaCol[ii].trim();
      if (this.colEqIgno(col, wp.itemStr(col)) == true) {
        continue;
      }
      return 1;
    }

    col = "asig_inq1_acct";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq1_card";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq1_limit";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq2_acct";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq2_card";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq2_limit";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq3_acct";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq3_card";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq3_limit";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;

    return 0;
  }

  void insertDbonline() {
    strSql = "insert into dbs_modlog_main (" + " mod_table , " + " mod_user , " + " mod_pgm , "
        + " mod_date ," + " mod_time2 ," + " mod_audcode , " + " id_p_seqno , "
        + " office_area_code1 , " + " office_tel_no1 , " + " office_tel_ext1 , "
        + " office_area_code2 , " + " office_tel_no2 , " + " office_tel_ext2 , "
        + " home_area_code1 , " + " home_tel_no1 , " + " home_tel_ext1 , " + " home_area_code2 , "
        + " home_tel_no2 , " + " home_tel_ext2 , " + " cellar_phone , " + " e_news , "
        + " accept_mbullet , " + " accept_dm ," + " accept_sms ," + " accept_call_sell ,  "
//        + " market_agree_base , " 
        + " tsc_market_flag , " +  " tsc_market_date "
        + " ) values (" + " 'DBC_IDNO' , " + " :mod_user , " + " :mod_pgm ,"
        + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') , " + " 'U' , "
        + " :id_p_seqno , " + " :office_area_code1 , " + " :office_tel_no1 , "
        + " :office_tel_ext1 , " + " :office_area_code2 , " + " :office_tel_no2 , "
        + " :office_tel_ext2 , " + " :home_area_code1 , " + " :home_tel_no1 , "
        + " :home_tel_ext1 , " + " :home_area_code2 , " + " :home_tel_no2 , " + " :home_tel_ext2 , "
        + " :cellar_phone , " + " :e_news , " + " :accept_mbullet , " + " :accept_dm ,"
        + " :accept_sms ," + " :accept_call_sell,  " 
//        + " :market_agree_base , " 
        + " :tsc_market_flag , " +  " :tsc_market_date "
        + " )";
    

    setString("mod_user", wp.loginUser);
    setString("mod_pgm", pgm);
    item2ParmStr("id_p_seqno");
    item2ParmStr("office_area_code1");
    item2ParmStr("office_tel_no1");
    item2ParmStr("office_tel_ext1");
    item2ParmStr("office_area_code2");
    item2ParmStr("office_tel_no2");
    item2ParmStr("office_tel_ext2");
    item2ParmStr("home_area_code1");
    item2ParmStr("home_tel_no1");
    item2ParmStr("home_tel_ext1");
    item2ParmStr("home_area_code2");
    item2ParmStr("home_tel_no2");
    item2ParmStr("home_tel_ext2");
    item2ParmStr("cellar_phone");
    item2ParmNvl("e_news", "N");
    item2ParmNvl("accept_mbullet", "N");
    item2ParmNvl("accept_dm", "N");
    item2ParmNvl("accept_sms", "N");
    item2ParmNvl("accept_call_sell", "N");
//    item2ParmNvl("market_agree_base", "N");
    item2ParmNvl("tsc_market_flag", "N");
    item2ParmStr("tsc_market_date");


    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insertDbonline=" + sqlErrtext);
    }

  }

  int checkModifyDbIdext1() {
    dbIdnoExt = true;
    // -returnVal: 1.有異動, 0.資料不存在, -1.未異動-
    strSql = "select * from dbc_idno_ext" + " where id_p_seqno =?";
    sqlSelect(strSql, new Object[] {idPSeqno});
    if (sqlRowNum <= 0) {
      dbIdnoExt = false;
      return 1;
    }

    String[] aaCol = new String[] {"asig_inq1_cname", "asig_inq1_idno", "asig_inq1_telno",
        // "asig_inq1_acct",
        // "asig_inq1_card",
        // "asig_inq1_limit",
        "asig_inq1_chk_data", "asig_inq2_cname", "asig_inq2_idno", "asig_inq2_telno",
        // "asig_inq2_acct",
        // "asig_inq2_card",
        // "asig_inq2_limit",
        "asig_inq2_chk_data", "asig_inq3_cname", "asig_inq3_idno", "asig_inq3_telno",
        // "asig_inq3_acct",
        // "asig_inq3_card",
        // "asig_inq3_limit",
        "asig_inq3_chk_data"};

    String col = "";
    for (int ii = 0; ii < aaCol.length; ii++) {
      col = aaCol[ii].trim();
      if (this.colEqIgno(col, wp.itemStr(col)) == true) {
        continue;
      }
      return 1;
    }

    col = "asig_inq1_acct";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq1_card";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq1_limit";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq2_acct";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq2_card";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq2_limit";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq3_acct";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq3_card";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    /*
     * col ="asig_inq3_limit"; if (eq_igno(col_nvl(col,"N"),wp.item_nvl(col,"N"))==false) return 1;
     */
    return 0;
  }

  void updateDbIdnoExt() {
    strSql = "update dbc_idno_ext set" + " asig_inq1_cname =:asig_inq1_cname , "
        + " asig_inq1_idno =:asig_inq1_idno , " + " asig_inq1_telno =:asig_inq1_telno , "
        + " asig_inq1_acct =:asig_inq1_acct , " + " asig_inq1_card =:asig_inq1_card , "
        + " asig_inq1_limit =:asig_inq1_limit , " + " asig_inq1_chk_data =:asig_inq1_chk_data , "
        + " asig_inq2_cname =:asig_inq2_cname , " + " asig_inq2_idno =:asig_inq2_idno , "
        + " asig_inq2_telno =:asig_inq2_telno , " + " asig_inq2_acct =:asig_inq2_acct , "
        + " asig_inq2_card =:asig_inq2_card , " + " asig_inq2_limit =:asig_inq2_limit , "
        + " asig_inq2_chk_data =:asig_inq2_chk_data , " + " asig_inq3_cname =:asig_inq3_cname , "
        + " asig_inq3_idno =:asig_inq3_idno , " + " asig_inq3_telno =:asig_inq3_telno , "
        + " asig_inq3_acct =:asig_inq3_acct , " + " asig_inq3_card =:asig_inq3_card , "
        + " asig_inq3_limit =:asig_inq3_limit , " + " asig_inq3_chk_data =:asig_inq3_chk_data , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + " apr_user =:apr_user , "
        + " mod_user =:mod_user , " + " mod_time =sysdate , " + " mod_pgm  =:mod_pgm , "
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where id_p_seqno =:id_p_seqno";

    item2ParmStr("id_p_seqno");
    item2ParmStr("asig_inq1_cname");
    item2ParmStr("asig_inq1_idno");
    item2ParmStr("asig_inq1_telno");
    item2ParmNvl("asig_inq1_acct", "N");
    item2ParmNvl("asig_inq1_card", "N");
    item2ParmNvl("asig_inq1_limit", "N");
    item2ParmStr("asig_inq1_chk_data");
    item2ParmStr("asig_inq2_cname");
    item2ParmStr("asig_inq2_idno");
    item2ParmStr("asig_inq2_telno");
    item2ParmNvl("asig_inq2_acct", "N");
    item2ParmNvl("asig_inq2_card", "N");
    item2ParmNvl("asig_inq2_limit", "N");
    item2ParmStr("asig_inq2_chk_data");
    item2ParmStr("asig_inq3_cname");
    item2ParmStr("asig_inq3_idno");
    item2ParmStr("asig_inq3_telno");
    item2ParmNvl("asig_inq3_acct", "N");
    item2ParmNvl("asig_inq3_card", "N");
    item2ParmNvl("asig_inq3_limit", "N");
    item2ParmStr("asig_inq3_chk_data");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", pgm);


    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
  }

  void insertDbIdnoExt() {
    strSql = "insert into dbc_idno_ext (" + " id_p_seqno , " + " asig_inq1_cname , "
        + " asig_inq1_idno , " + " asig_inq1_telno , " + " asig_inq1_acct , " + " asig_inq1_card , "
        + " asig_inq1_limit , " + " asig_inq1_chk_data , " + " asig_inq2_cname , "
        + " asig_inq2_idno , " + " asig_inq2_telno , " + " asig_inq2_acct , " + " asig_inq2_card , "
        + " asig_inq2_limit , " + " asig_inq2_chk_data , " + " asig_inq3_cname , "
        + " asig_inq3_idno , " + " asig_inq3_telno , " + " asig_inq3_acct , " + " asig_inq3_card , "
        + " asig_inq3_limit , " + " asig_inq3_chk_data , " + " apr_date , " + " apr_user , "
        + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values ("
        + " :id_p_seqno , " + " :asig_inq1_cname , " + " :asig_inq1_idno , "
        + " :asig_inq1_telno , " + " :asig_inq1_acct , " + " :asig_inq1_card , "
        + " :asig_inq1_limit , " + " :asig_inq1_chk_data , " + " :asig_inq2_cname , "
        + " :asig_inq2_idno , " + " :asig_inq2_telno , " + " :asig_inq2_acct , "
        + " :asig_inq2_card , " + " :asig_inq2_limit , " + " :asig_inq2_chk_data , "
        + " :asig_inq3_cname , " + " :asig_inq3_idno , " + " :asig_inq3_telno , "
        + " :asig_inq3_acct , " + " :asig_inq3_card , " + " :asig_inq3_limit , "
        + " :asig_inq3_chk_data , " + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , "
        + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 " + " )";

    item2ParmStr("id_p_seqno");
    item2ParmStr("asig_inq1_cname");
    item2ParmStr("asig_inq1_idno");
    item2ParmStr("asig_inq1_telno");
    item2ParmNvl("asig_inq1_acct", "N");
    item2ParmNvl("asig_inq1_card", "N");
    item2ParmNvl("asig_inq1_limit", "N");
    item2ParmStr("asig_inq1_chk_data");
    item2ParmStr("asig_inq2_cname");
    item2ParmStr("asig_inq2_idno");
    item2ParmStr("asig_inq2_telno");
    item2ParmNvl("asig_inq2_acct", "N");
    item2ParmNvl("asig_inq2_card", "N");
    item2ParmNvl("asig_inq2_limit", "N");
    item2ParmStr("asig_inq2_chk_data");
    item2ParmStr("asig_inq3_cname");
    item2ParmStr("asig_inq3_idno");
    item2ParmStr("asig_inq3_telno");
    item2ParmNvl("asig_inq3_acct", "N");
    item2ParmNvl("asig_inq3_card", "N");
    item2ParmNvl("asig_inq3_limit", "N");
    item2ParmStr("asig_inq3_chk_data");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", pgm);

    wp.log("idno_ext=" + strSql);

    sqlExec(strSql);
    log("A:" + sqlRowNum);
    if (sqlRowNum <= 0) {
      errmsg("dbc_idno_ext=" + sqlErrtext);
    }
  }

  @Override
  public void dataCheck() {
	  String sql1 = "select count(*) as ll_cnt "
	    		+ " from dbs_modlog_main " 
	    		+ " where id_p_seqno = ? "
	            + " and mod_date = ? " 
	    		+ " and mod_time2 = ? " 
	            + " and mod_user = ? "
	            + " and mod_table = 'DBC_IDNO' "
	            + " and nvl(apr_flag,'N') <> 'Y' "
	            ;
	    sqlSelect(sql1, new Object[] {wp.itemStr("id_p_seqno"), wp.itemStr("mod_date"),
	        wp.itemStr("mod_time2"), wp.itemStr("mod_user")});
	    log("1. id_p_seqno:" + wp.itemStr("id_p_seqno"));
	    log("2. mod_date:" + wp.itemStr("mod_date"));
	    log("3. mod_time2:" + wp.itemStr("mod_time2"));
	    log("4. mod_user:" + wp.itemStr("mod_user"));
	    if (colNum("ll_cnt") != 1) {
	      errmsg("此筆資料已有人修改, 請結束再覆核 !");
	      rc = -1;
	      return;
	    }

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    idPSeqno = wp.itemStr("id_p_seqno");
    strSql = "select * from dbc_idno" + " where id_p_seqno =?";
    sqlSelect(strSql, new Object[] {idPSeqno});
    if (sqlRowNum <= 0) {
      errmsg("DBC_IDNO not find, kk=" + idPSeqno);
      return rc;
    }
    
    // if this value is equal to 1, it means this record is modified.
    int isModify = 0; 

    isModify = checkModifyDbAppr();
    if ( isModify == 1) {
      delDbonline();
      insertDbonline();
    }
    
    if (isModify == 0) {
		wp.respMesg = "需覆核資料無異動。";
	}
    
    // 目前隱藏
    if (rc == 1 && checkModifyDbIdext1() == 1) {
      if (dbIdnoExt) {
        updateDbIdnoExt();
      } else{
    	insertDbIdnoExt();
      }
    }

    if (rc == 1) {
    	if (updateDbcidnoM() == 1 ) {
    		if (rc == 1) {
    			wp.respMesg += "不需覆核資料更新成功。";
			}else {
				wp.respMesg += "不需覆核資料更新失敗。";
			}
    	}else {
    		wp.respMesg += "不需覆核資料無異動。";
    	}
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    delDbonline();
    return rc;
  }

  @Override
  public int dataProc() {
		dataCheck();

		if (rc != 1)
			return rc;

    strSql = "select " + " office_area_code1 , " + " office_tel_no1 , " + " office_tel_ext1 ,"
        + " office_area_code2 ," + " office_tel_no2 ," + " office_tel_ext2 ," + " home_area_code1 ,"
        + " home_tel_no1 ," + " home_tel_ext1 ," + " home_area_code2 ," + " home_tel_no2 ,"
        + " home_tel_ext2 ," + " cellar_phone ," + " e_news ," + " accept_mbullet ,"
        + " accept_dm ," + " accept_sms ," + " accept_call_sell ," + " promote_emp_no, "
        + " market_agree_base , " + " tsc_market_flag, " + " tsc_market_date, "
        + " mod_user as online_mod_user "
        + " from dbs_modlog_main " 
        + " where id_p_seqno =?" 
        + " and nvl(apr_flag,'N')<>'Y' "
        + " and mod_table = 'DBC_IDNO' ";
    this.sqlSelect(strSql, new Object[] {wp.itemStr("id_p_seqno")});
    
    strSql =  " select cellar_phone dbc_idno_cellar_phone "
            + " from dbc_idno " 
            + " where id_p_seqno =?";
    this.sqlSelect(strSql, new Object[] {wp.itemStr("id_p_seqno")});

    updateDbcIdno();
    if (rc != 1)
      return rc;
    updateModlog();
    if (rc != 1)
      return rc;
    insertIBM();
    if (rc != 1)
      return rc;
    if (eqIgno(wp.itemStr("wk_dm_flag"), "1")) {
      updateCrdIdno();
    }
    return rc;
  }

  void updateDbcIdno() {

    msgOK();

    strSql = " update dbc_idno set " 
        + " office_area_code1 =:office_area_code1 ,"
        + " office_tel_no1 =:office_tel_no1 ," 
        + " office_tel_ext1 =:office_tel_ext1 ,"
        + " office_area_code2 =:office_area_code2 ," 
        + " office_tel_no2 =:office_tel_no2 ,"
        + " office_tel_ext2 =:office_tel_ext2 ," 
        + " home_area_code1 =:home_area_code1 ,"
        + " home_tel_no1 =:home_tel_no1 ," 
        + " home_tel_ext1 =:home_tel_ext1 ,"
        + " home_area_code2 =:home_area_code2 ," 
        + " home_tel_no2 =:home_tel_no2 ,"
        + " home_tel_ext2 =:home_tel_ext2 ," 
        + " cellar_phone =:cellar_phone ,";
        if (colStr("dbc_idno_cellar_phone").trim().equals(colStr("cellar_phone").trim()) == false) {
        	strSql += " c_phone_chg_date = to_char(sysdate,'yyyymmdd'), "
        		    + " c_phone_chg_time = sysdate, ";
		}
        strSql += " e_news =:e_news ," 
        + " accept_mbullet =:accept_mbullet ," 
        + " accept_dm =:accept_dm ,"
        + " accept_sms =:accept_sms ," 
        + " accept_call_sell =:accept_call_sell, "
//        + " market_agree_base = :market_agree_base  , " 
        + " tsc_market_flag = :tsc_market_flag , " 
        // 2020-09-09 JustinWu
        + " apr_user = :apr_user , "
        + " apr_date = to_char(sysdate, 'yyyyMMdd') ,"
        // 2020-09-09 JustinWu        
        + " mod_time = sysdate , "
        + " mod_pgm = :mod_pgm , "
        + " mod_user = :mod_user, "
        + " mod_seqno = nvl(mod_seqno, 0) + 1 "
        // + " promote_emp_no =:promote_emp_no "
        + " where id_p_seqno =:id_p_seqno ";

    setString("office_area_code1", colStr("office_area_code1"));
    setString("office_tel_no1", colStr("office_tel_no1"));
    setString("office_tel_ext1", colStr("office_tel_ext1"));
    setString("office_area_code2", colStr("office_area_code2"));
    setString("office_tel_no2", colStr("office_tel_no2"));
    setString("office_tel_ext2", colStr("office_tel_ext2"));
    setString("home_area_code1", colStr("home_area_code1"));
    setString("home_tel_no1", colStr("home_tel_no1"));
    setString("home_tel_ext1", colStr("home_tel_ext1"));
    setString("home_area_code2", colStr("home_area_code2"));
    setString("home_tel_no2", colStr("home_tel_no2"));
    setString("home_tel_ext2", colStr("home_tel_ext2"));
    setString("cellar_phone", colStr("cellar_phone"));
    setString("e_news", colStr("e_news"));
    setString("accept_mbullet", colStr("accept_mbullet"));
    setString("accept_dm", colStr("accept_dm"));
    setString("accept_sms", colStr("accept_sms"));
    setString("accept_call_sell", colStr("accept_call_sell"));
    // setString("promote_emp_no",col_ss("promote_emp_no"));
//    setString("market_agree_base",colStr("market_agree_base"));
    setString("tsc_market_flag", colStr("tsc_market_flag"));
    setString("accept_call_sell", colStr("accept_call_sell"));
    setString("apr_user", wp.loginUser); // 2020-09-09 JustinWu
    setString("mod_pgm", "cmsp2010");
    setString("mod_user", colStr("online_mod_user"));
    item2ParmStr("id_p_seqno");
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update dbc_idno error !");
      rc = -1;
      return;
    }


  }

  void updateModlog() {
    strSql = " update dbs_modlog_main set " + " apr_user =:apr_user ,"
        + " apr_date =to_char(sysdate,'yyyymmdd') ," + " apr_time =to_char(sysdate,'hh24miss') ,"
        + " apr_flag ='Y' ," + " mod_date =to_char(sysdate,'yyyymmdd') ,"
        + " mod_time2 =to_char(sysdate,'hh24miss') ," + " mod_user =:mod_user ,"
        + " mod_pgm =:mod_pgm " + " where mod_table ='DBC_IDNO' " + " and id_p_seqno =:id_p_seqno "
        + " and apr_flag <> 'Y'";
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    item2ParmStr("id_p_seqno");
    setString("mod_pgm", pgm);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Update 異動記錄檔 error ! (dbs_modlog_main)");
      rc = -1;
      return;
    }
  }

  void insertIBM() {
    msgOK();
    lsHomeNo = "";
    lsOfficeNo = "";

    lsHomeNo =
        wp.itemStr("home_area_code1") + wp.itemStr("home_tel_no1") + wp.itemStr("home_tel_ext1");
    lsOfficeNo = wp.itemStr("office_area_code1") + wp.itemStr("office_tel_no1")
        + wp.itemStr("office_tel_ext1");
    lsCellPhone = wp.itemStr2("cellar_phone");
    strSql = "insert into dbs_main2ibm (" + " crd_date , " + " id_p_seqno , " + " card_id , "
        + " card_id_code , " + " home_area , " + " home_telno , " + " home_telext , "
        + " office_area , " + " office_telno , " + " office_telext , " + " cellar_phone , "
        + " mod_user , " + " apr_user " + " ) values (" + " sysdate , " + " :id_p_seqno , "
        + " :card_id , " + " '0' , " + " :home_area , " + " :home_telno , " + " :home_telext , "
        + " :office_area , " + " :office_telno , " + " :office_telext , " + " :cellar_phone , "
        + " :mod_user , " + " :apr_user " + " )";

    item2ParmStr("id_p_seqno");
    item2ParmStr("card_id", "id_no");
    item2ParmStr("home_area", "home_area_code1");
    if (lsHomeNo.length() == 0) {
      setString("home_telno", "XXXXXXXXXX");
    } else {
      item2ParmStr("home_telno", "home_tel_no1");
    }
    item2ParmStr("home_telext", "home_tel_ext1");
    item2ParmStr("office_area", "office_area_code1");
    if (lsOfficeNo.length() == 0) {
      setString("office_telno", "XXXXXXXXXX");
    } else {
      item2ParmStr("office_telno", "office_tel_no1");
    }
    item2ParmStr("office_telext", "office_tel_ext1");
    if (lsCellPhone.length() == 0) {
      setString("cellar_phone", "XXXXXXXXXXXXXX");
    } else {
      item2ParmStr("cellar_phone");
    }

    setString("mod_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("Insert 傳送 IBM 資料檔失敗  !");
      rc = -1;
      return;
    }
  }

  /**
   * update 需覆核的crd_idno
   */
  void updateCrdIdno() {
    // msgOK();
    //
    // is_sql ="select "
    // + " e_news ,"
    // + " accept_mbullet ,"
    // + " accept_dm ,"
    // + " accept_sms ,"
    // + " accept_call_sell ,"
    // + " promote_emp_no "
    // + " from dbs_modlog_main "
    // +" where id_p_seqno =?"
    // +" and nvl(apr_flag,'N')<>'Y' "
    // +" and mod_table = 'DBC_IDNO' "
    // ;
    // this.sqlSelect(is_sql,new Object[]{
    // wp.item_ss("id_p_seqno")
    // });

    strSql = " update crd_idno set " 
        + " e_news =:e_news , "
        + " e_news_from_mark =:e_news_from_mark , " 
        + " e_news_chg_date =:e_news_chg_date , "
        + " accept_mbullet =:accept_mbullet , " 
        + " mbullet_from_mark =:mbullet_from_mark , "
        + " mbullet_chg_date =:mbullet_chg_date , " 
        + " accept_dm =:accept_dm , "
        + " dm_from_mark =:dm_from_mark , " 
        + " dm_chg_date =:dm_chg_date , "
        + " accept_sms =:accept_sms , " 
        + " sms_from_mark =:sms_from_mark , "
        + " sms_chg_date =:sms_chg_date , " 
        + " accept_call_sell =:accept_call_sell , "
        + " call_sell_from_mark =:call_sell_from_mark , "
        + " call_sell_chg_date =:call_sell_chg_date , " 
//        + " market_agree_base = :market_agree_base , " 
        + " tsc_market_flag = :tsc_market_flag, " 
        + " tsc_market_date = :tsc_market_date, "
        // 2020-09-09 JustinWu
        + " apr_user =:apr_user , "
        + " apr_date = to_char(sysdate, 'yyyyMMdd') , "
        // 2020-09-09 JustinWu
        + " mod_time = sysdate , "
        + " mod_pgm =:mod_pgm , "
        + " mod_user = :mod_user , "
        + " mod_seqno = nvl(mod_seqno, 0) + 1 " 
        + " where id_no =:id_no";

    setString("e_news", colStr("e_news"));
    item2ParmStr("e_news_from_mark");
    item2ParmStr("e_news_chg_date");
    setString("accept_mbullet", colStr("accept_mbullet"));
    item2ParmStr("mbullet_from_mark");
    item2ParmStr("mubllet_chg_date");
    setString("accept_dm", colStr("accept_dm"));
    item2ParmStr("dm_from_mark");
    item2ParmStr("dm_chg_date");
    setString("accept_sms", colStr("accept_sms"));
    item2ParmStr("sms_from_mark");
    item2ParmStr("sms_chg_date");
    setString("accept_call_sell", colStr("accept_call_sell"));
    item2ParmStr("call_sell_from_mark");
    item2ParmStr("call_sell_chg_date");
//    item2ParmNvl("market_agree_base", "N"); //改為0,1,2
    item2ParmNvl("tsc_market_flag", "N");
    item2ParmStr("tsc_market_date");
    // 2020-09-09 JustinWu
    setString("apr_user", wp.loginUser);
    // 2020-09-09 JustinWu
    setString("mod_pgm", "cmsp2010");
    setString("mod_user",colStr("online_mod_user"));
    item2ParmStr("id_no");
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update crd_idno error !");
      rc = -1;
      return;
    }

  }

  /**
   * update 不用覆核的資料
   * @return
   */
  int updateDbcidnoM() {
		if (wp.itemNvl("msg_flag", "N").equals(colStr("msg_flag")) && 
				wp.itemNum("msg_purchase_amt") == (colNum("msg_purchase_amt")) ) {
			return 0;
		}
	  
    msgOK();

    strSql = " update dbc_idno set " 
                + " msg_flag =:msg_flag , "
                + " msg_purchase_amt =:msg_purchase_amt, "
                + " mod_time = sysdate , "
                + " mod_pgm =:mod_pgm , "
                + " mod_user = :mod_user , "
                + " mod_seqno = nvl(mod_seqno, 0) + 1 " 
                + " where id_p_seqno =:id_p_seqno ";

    item2ParmNvl("msg_flag", "N");
    item2ParmNum("msg_purchase_amt");
    setString("mod_pgm", pgm);
    setString("mod_user",wp.loginUser);
    item2ParmStr("id_p_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update dbc_idno error ");
    }

    return 1;
  }

	public void cleanModCol() {
		for (int i = 0; i < aaCol.length; i++) {
			wp.colSet("mod_"+aaCol[i].trim(), "");
		}
		
		for (int i = 0; i < checkCol.length; i++) {
			wp.colSet("mod_"+checkCol[i].trim(), "");
		}

	}
  
  

}
