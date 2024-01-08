package cmsm01;
/** cmsm2130;持卡人帳戶資料維護(一)
 * 2019-1211:  Alex  bug fix
 * 2019-0612:  JH    p_xxx >>acno_pxxx
 * V00.0		2017-0731	JH		initial
 * 109-04-19  V1.00.06  shiyuqi       updated for project coding standard   
 * 109-05-29  V1.00.07  shiyuqi       新增欄位    
 * 109-06-09  V1.00.08  shiyuqi       修改程式名稱                
 * 109-08-21  V1.00.09  JustinWu     修改新增資料修改前後時的mod_date, mod_time
 * 109-09-09  V1.00.10  JustinWu     主檔apr_user=approveUser, mod_user=modifyUser, and update_user=modifyUser
 * 109-12-11   V1.00.11   JustinWu     update bill_apply_flag and add setAcnoDesc
 * 109-12-15   V1.00.12  JustinWu     update e_mail_ebill and e_mail_ebill_date
 * 109-12-16   V1.00.13  JustinWu     add cleanModCol() and check crd_idno
 * 110-01-06   V1.00.14  JustinWu     updated for XSS
 * 110-10-05   V1.00.15  JustinWu     add zip2
 * 111-03-02   V1.00.16  JustinWu     新增異動日期及時間在待覆核資訊
 * 112-04-20   V1.00.17  ZuweiSu      增加檢核條件
 * */

import busi.FuncAction;
import busi.ecs.CommFunction;
import ecsfunc.DeCodeAct;
import taroko.base.CommDate;

public class Cmsm2010Acno extends FuncAction {
    CommFunction commFunc = new CommFunction();
    CommDate commDate = new CommDate();
  String[] aaCol =
      new String[] {"vip_code", "risk_bank_no", "bill_sending_zip", "bill_sending_zip2", "bill_sending_addr1",
          "bill_sending_addr2", "bill_sending_addr3", "bill_sending_addr4", "bill_sending_addr5",
          "accept_dm", "vip_remark", "new_acct_flag", "bill_sending_country_code", "e_mail_ebill","bill_apply_flag"};



	public int acnoOnline() {
		if (empty(wp.colStr("acno_p_seqno"))) {
			errmsg("帳戶流水號(acno_p_seqno): 不可空白");
			return -1;
		}

		strSql = "select * , to_char(to_date(MOD_DATE || MOD_TIME2, 'yyyy/mm/dd hh24:mi:ss') , 'yyyy/mm/dd hh24:mi:ss') as userModDateTime "
				    + " from act_acno_online" 
		            + " where acno_p_seqno =?" 
				    + " and apr_YN ='Y' "
				    + " and apr_flag <>'Y'" 
				    + " and data_image = '2' ";
		this.sqlSelect(strSql, new Object[] { wp.colStr("acno_p_seqno") });
		if (sqlRowNum <= 0) {
			return 0;
		}
		
		// split zipCode into two parts, zip_code1 and zip_code2.  
	    String[] billSendingZipArr = commString.splitZipCode(colStr("bill_sending_zip"));
	    colSet("bill_sending_zip", billSendingZipArr[0]);
	    colSet("bill_sending_zip2", billSendingZipArr[1]);
		wp.colSet("userModDateTime", this.colStr("userModDateTime"));
		return checkModifyAppr();
	}

  private void setAcnoDesc() {
	  
		wp.colSet("bill_apply_flag_desc", ecsfunc.DeCodeCms.billApplyFlagCode(wp.colStr("bill_apply_flag")));

		if (!wp.colEmpty("acct_status")) {
			wp.colSet("tt_acct_status", "." + DeCodeAct.acctStatus(wp.colStr("acct_status")));
		}

		if (wp.colEq("corp_act_flag", "Y")) {
			wp.colSet("tt_corp_act_flag", ".總繳");
		} else if (wp.colEq("corp_act_flag", "N")) {
			wp.colSet("tt_corp_act_flag", ".個繳");
		}
		
		if ( ! wp.colEmpty("vip_code")) {
			setVIPDesc(wp.colStr("vip_code"));
		}

  }

	private boolean setVIPDesc(String vipCode) {
		strSql = " select vip_desc as vip_code_desc "
				+  " from ptr_vip_code "
				+  " where vip_code = ? ";
		this.sqlSelect(strSql, new Object[] { vipCode });
		if (sqlRowNum <= 0) {
			wp.colSet("vip_code_desc", "");
			return false;
		}
		wp.colSet("vip_code_desc", colStr("vip_code_desc"));
		return true;
	}

int checkModifyAppr() {
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
    
    // 如有更動e_mail_ebill，則按下覆核後，需要更新e_mail_ebill_date，
    // 因此需要此欄位判斷e_mail_ebill是否有被更動
    if ( this.colEqIgno("e_mail_ebill", wp.colStr("e_mail_ebill")) == true) {
		wp.colSet("isEMailEbillChg", "Y");
	}
      
	setAcnoDesc();
    wp.colSet("mod_date", colStr("mod_date"));
    wp.colSet("mod_time2", colStr("mod_time2"));
    wp.colSet("mod_user", colStr("mod_user"));
    wp.colSet("acno_p_seqno", colStr("acno_p_seqno"));
    return liMod;
  }

  public void cmsp2010SetModifyData() {
    dateTime();
    wp.colSet("wk_addr_flag", "");
    if (empty(wp.colStr("mod_bill_sending_zip")) == false
    	|| empty(wp.colStr("mod_bill_sending_zip2")) == false
        || empty(wp.colStr("mod_bill_sending_addr1")) == false
        || empty(wp.colStr("mod_bill_sending_addr2")) == false
        || empty(wp.colStr("mod_bill_sending_addr3")) == false
        || empty(wp.colStr("mod_bill_sending_addr4")) == false
        || empty(wp.colStr("mod_bill_sending_addr5")) == false) {
      wp.colSet("wk_addr_flag", "1");
      wp.colSet("chg_addr_date", sysDate);
    }
    wp.colSet("wk_vip_flag", "");
    if (empty(wp.colStr("mod_vip_code")) == false) {
      wp.colSet("wk_vip_flag", "1");
      wp.colSet("no_tel_coll_flag", "Y");
      wp.colSet("no_tel_coll_s_date", sysDate);
      wp.colSet("no_tel_coll_e_date", "");
      wp.colSet("no_per_coll_flag", "Y");
      wp.colSet("no_per_coll_s_date", sysDate);
      wp.colSet("no_per_coll_e_date", "");
    }

  }

  void delOnline() {
    strSql = "delete ACT_ACNO_ONLINE " + " where acno_p_seqno =? " + " and apr_YN='Y' "
        + " and apr_flag<>'Y' ";

    setString2(1, wp.itemStr2("acno_p_seqno"));
    sqlExec(strSql);
  }

  /**
   * 新增異動前資料
   */
  void insertOnlineCol() {
	  
    strSql = "insert into act_acno_online (" 
        + " data_image , " 
    	+ " acno_p_seqno , "
        + " acct_type , " + " acct_key , " + " vip_code , " + " risk_bank_no , "
        + " bill_sending_zip , " + " bill_sending_addr1 , " + " bill_sending_addr2 , "
        + " bill_sending_addr3 , " + " bill_sending_addr4 , " + " bill_sending_addr5 , "
        + " accept_dm , " + " vip_remark , " + " new_acct_flag , " + " bill_sending_country_code ," 
        + " e_mail_ebill ,"+    " bill_apply_flag ,"
        + " mod_date , " + " mod_time2 , " + " mod_user , " + " mod_deptno , " + " apr_yn , "
        + " apr_flag , " + " apr_user , " + " apr_date , " + " apr_time  " + " ) values ("
        + " '1' , " 
        + " :acno_p_seqno , " 
        + " :acct_type , " + " :acct_key , " + " :vip_code , "+ " :risk_bank_no , " 
        + " :bill_sending_zip , " + " :bill_sending_addr1 , "+ " :bill_sending_addr2 , " 
        + " :bill_sending_addr3 , " + " :bill_sending_addr4 , "+ " :bill_sending_addr5 , " 
        + " :accept_dm , " + " :vip_remark , " + " :new_acct_flag , "+ " :bill_sending_country_code ," 
        +" :e_mail_ebill ," +    " :bill_apply_flag ,"
        + ":mod_date , "+ " :mod_time , " + " :mod_user , " + " '' , " + " 'Y' , " 
        + " 'N' , "+ " '' , " + " '' , " + " ''  " + " )";

    setString2("acno_p_seqno", colStr("acno_p_seqno"));
    col2ParmStr("acct_type");
    col2ParmStr("acct_key");
    col2ParmStr("vip_code");
    col2ParmStr("risk_bank_no");
    setString("bill_sending_zip", colStr("bill_sending_zip") + colStr("bill_sending_zip2"));
    col2ParmStr("bill_sending_addr1");
    col2ParmStr("bill_sending_addr2");
    col2ParmStr("bill_sending_addr3");
    col2ParmStr("bill_sending_addr4");
    col2ParmStr("bill_sending_addr5");
    col2ParmStr("vip_remark");
    col2ParmNvl("accept_dm", "N");
    col2ParmNvl("new_acct_flag", "N");
    col2ParmStr("bill_sending_country_code");
    col2ParmStr("e_mail_ebill");
    col2ParmStr("bill_apply_flag");
    setString("mod_date", sysDate);
    setString("mod_time", sysTime);
    setString("mod_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("新增修改前資料至act_acno_online錯誤: " + getMsg());
      return;
    }
  }

  /**
   * 新增異動後資料
   */
  void insertOnlineItem() {
    strSql = "insert into act_acno_online (" 
        + " data_image , " 
    	+ " acno_p_seqno , "
        + " acct_type , " + " acct_key , " + " vip_code , " + " risk_bank_no , "
        + " bill_sending_zip , " + " bill_sending_addr1 , " + " bill_sending_addr2 , "
        + " bill_sending_addr3 , " + " bill_sending_addr4 , " + " bill_sending_addr5 , "
        + " accept_dm , " + " vip_remark , " + " new_acct_flag , " + " bill_sending_country_code , "+" e_mail_ebill , "
        +  "bill_apply_flag , "
        + " mod_date , " + " mod_time2 , " + " mod_user , " + " mod_deptno , " + " apr_yn , "
        + " apr_flag , " + " apr_user , " + " apr_date , " + " apr_time  " + " ) values ("
        + " '2' , " 
        + " :acno_p_seqno , " 
        + " :acct_type , " + " :acct_key , " + " :vip_code , "+ " :risk_bank_no , " 
        + " :bill_sending_zip , " + " :bill_sending_addr1 , "
        + " :bill_sending_addr2 , " 
        + " :bill_sending_addr3 , " + " :bill_sending_addr4 , "
        + " :bill_sending_addr5 , " 
        + " :accept_dm , " + " :vip_remark , " + " :new_acct_flag , "
        + " :bill_sending_country_code , " + ":e_mail_ebill ,"+   ":bill_apply_flag , "+" :mod_date , "
        + " :mod_time , " + " :mod_user , " + " '' , " + " 'Y' , " + " 'N' , "
        + " '' , " + " '' , " + " ''  " + " )";

    item2ParmStr("acno_p_seqno");
    item2ParmStr("acct_type");
    item2ParmStr("acct_key");
    item2ParmStr("vip_code");
    item2ParmStr("risk_bank_no");
    setString("bill_sending_zip", wp.itemStr("bill_sending_zip") + wp.itemStr("bill_sending_zip2"));
    item2ParmStr("bill_sending_addr1");
    item2ParmStr("bill_sending_addr2");
    item2ParmStr("bill_sending_addr3");
    item2ParmStr("bill_sending_addr4");
    item2ParmStr("bill_sending_addr5");
    item2ParmStr("vip_remark");
    col2ParmNvl("accept_dm", "N");
    item2ParmNvl("new_acct_flag", "N");
    item2ParmStr("bill_sending_country_code");
    item2ParmStr("e_mail_ebill");
    item2ParmStr("bill_apply_flag");
    setString("mod_date", sysDate);
    setString("mod_time", sysTime);
    setString("mod_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("新增修改後資料至act_acno_online錯誤: " + getMsg());
      return;
    }
  }

  @Override
  public void dataCheck() {
    if (ibUpdate) {
    	
    	// 如果帳單註記為同公司(個人)
    	if (wp.itemStr("bill_apply_flag").equals("3")) {
    		// 檢查crd_idno_online的company_addr1以及company_addr5是否為空
        	if ( isCompAddrEmptyInCrdIdnoOnline(wp.itemStr("id_p_seqno")) ) {
        		return;
    		}
		}
    	// 前端有電子帳單專用郵件信箱(E_MAIL_EBILL)此欄位動作時
        String statSendSMonth2 = wp.itemEmpty("STAT_SEND_S_MONTH2") ? "000000" : wp.itemStr("STAT_SEND_S_MONTH2");
        String statSendEMonth2 = wp.itemEmpty("STAT_SEND_E_MONTH2") ? "999999" : wp.itemStr("STAT_SEND_E_MONTH2");
        String statSendInternet = wp.itemStr("STAT_SEND_INTERNET");
        String emaiEbill = wp.itemStr("e_mail_ebill");
        String oldEmaiEbill = wp.itemStr("e_mail_ebill_old");
        String sysMonth = commDate.sysDate().substring(0,6);
    	if (!emaiEbill.equalsIgnoreCase(oldEmaiEbill)) {
    	    if (!empty(emaiEbill)) {
    	        if (!commFunc.isValidEmail(emaiEbill)) {
    	            errmsg("電子郵件格式錯誤 ,請重新修正!");
    	            return;
    	        }
    	    } else {
    	        if ("Y".equals(statSendInternet)
    	                && sysMonth.compareTo(statSendSMonth2) >= 0 
    	                && sysMonth.compareTo(statSendEMonth2) <= 0 
    	                && !empty(oldEmaiEbill)) {
    	            errmsg("目前為有效訂閱電子帳單狀態，電子帳單專用郵件信箱不可清除為空值");
    	        }
    	    }
    	}

	}

  }

	@Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "select * from act_acno" + " where acno_p_seqno =?";
    sqlSelect(strSql, new Object[] {wp.itemStr("acno_p_seqno")});
    if (sqlRowNum <= 0) {
      errmsg("act_acno not find, kk=");
      return rc;
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] billSendingZipArr = commString.splitZipCode(colStr("bill_sending_zip"));
    colSet("bill_sending_zip", billSendingZipArr[0]);
    colSet("bill_sending_zip2", billSendingZipArr[1]);

    delOnline();
    if (checkModifyAppr() == 1) {
		getSysDate();
		insertOnlineCol();
		if (rc != 1) {
			return rc;
		}
		insertOnlineItem();
		if (rc != 1) {
			return rc;
		}
    }
    
    updateAcno();
    return rc;
  }

  @Override
  public int dbDelete() {
    dataCheck();
    if (rc != 1) {
      errmsg(getMsg());
      return rc;
    }
    delOnline();

    return rc;
  }

  @Override
  public int dataProc() {
    checkCnt();
    if (rc != 1) {
      return rc;
    }
    strSql = "select *, mod_user as online_mod_user from act_acno_online" + " where acno_p_seqno =?" + " and apr_YN ='Y' "
        + " and apr_flag <>'Y'" + " and data_image = '2' ";
    this.sqlSelect(strSql, new Object[] {wp.colStr("acno_p_seqno")});
    updateAcnoOnlin();
    if (rc != 1)
      return rc;

    if (eqIgno(wp.itemStr("wk_addr_flag"), "1")) {
      updateCrdEmboss();
      if (rc != 1)
        return rc;

      busi.func.SmsMsgDetl sms = new busi.func.SmsMsgDetl();
      sms.setConn(wp);
      sms.cmsP2130Addr(wp.colStr("acno_p_seqno"));
    }
    updateActAcno();
    return rc;
  }

  void checkCnt() {
    msgOK();
    String sql1 = "select count(*) as ll_cnt from act_acno_online " + " where acno_p_seqno = ? "
        + " and mod_date = ? " + " and mod_time2 = ? " + " and mod_user = ? " + " and apr_YN = 'Y' "
        + " and nvl(apr_flag,'N') <> 'Y' ";
    sqlSelect(sql1, new Object[] {wp.itemStr("acno_p_seqno"), wp.itemStr("mod_date"),
        wp.itemStr("mod_time2"), wp.itemStr("mod_user")});
    if (colNum("ll_cnt") != 2) {
      errmsg("此筆資料已有人修改, 請結束再覆核 !");
      rc = -1;
      return;
    }
  }

  void updateAcnoOnlin() {
    msgOK();
    strSql = " update act_acno_online set " + " apr_flag = 'Y' ," + " apr_user = :apr_user ,"
        + " apr_date = to_char(sysdate,'yyyymmdd') ," + " apr_time = to_char(sysdate,'hh24miss')  "
        + " where acno_p_seqno =:acno_p_seqno " + " and mod_date =:mod_date "
        + " and mod_time2 =:mod_time2 " + " and mod_user =:mod_user " + " and apr_yn = 'Y' "
        + " and apr_flag <> 'Y' ";
    setString("apr_user", wp.loginUser);
    item2ParmStr("acno_p_seqno");
    item2ParmStr("mod_date");
    item2ParmStr("mod_time2");
    item2ParmStr("mod_user");
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg(" update act_acno_online error ! ");
      return;
    }
  }

  void updateCrdEmboss() {
    msgOK();
    String sql1 = "select id_no , birthday from crd_idno where id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {wp.itemStr("id_p_seqno")});

    strSql = " update crd_emboss set " + " mail_zip =:mail_zip ," + " mail_addr1 =:mail_addr1 , "
        + " mail_addr2 =:mail_addr2 , " + " mail_addr3 =:mail_addr3 , "
        + " mail_addr4 =:mail_addr4 , " + " mail_addr5 =:mail_addr5   "
        + " where emboss_source = '1' " + " and acct_type = :acct_type "
        + " and apply_id = :apply_id " + " and birthday = :birthday " + " and in_main_date = '' ";

    setString("mail_zip", colStr("bill_sending_zip") + colStr("bill_sending_zip2"));
    setString("mail_addr1", colStr("bill_sending_addr1"));
    setString("mail_addr2", colStr("bill_sending_addr2"));
    setString("mail_addr3", colStr("bill_sending_addr3"));
    setString("mail_addr4", colStr("bill_sending_addr4"));
    setString("mail_addr5", colStr("bill_sending_addr5"));
    setString("acct_type", colStr("acct_type"));
    setString("apply_id", colStr("id_no"));
    setString("birthday", colStr("birthday"));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      rc = -1;
      return;
    } else {
      rc = 1;
    }
  }

  /**
   * update需覆核資料
   */
  void updateActAcno() {
    msgOK();
    strSql = " update act_acno set " 
        + " risk_bank_no =:risk_bank_no ,"
        + " chg_addr_date =:chg_addr_date ," 
    	+ " vip_code =:vip_code ,"
    	+ " bill_apply_flag =:bill_apply_flag , "
        + " bill_sending_zip =:bill_sending_zip ," 
        + " bill_sending_addr1 =:bill_sending_addr1 ,"
        + " bill_sending_addr2 =:bill_sending_addr2 ,"
        + " bill_sending_addr3 =:bill_sending_addr3 ,"
        + " bill_sending_addr4 =:bill_sending_addr4 ,"
        + " bill_sending_addr5 =:bill_sending_addr5 ,"
        + " online_update_date =to_char(sysdate,'yyyymmdd') ,"
        + " no_tel_coll_flag =:no_tel_coll_flag ," 
        + " no_tel_coll_s_date =:no_tel_coll_s_date ,"
        + " no_tel_coll_e_date =:no_tel_coll_e_date ," 
        + " no_per_coll_flag =:no_per_coll_flag ,"
        + " no_per_coll_s_date =:no_per_coll_s_date ,"
        + " no_per_coll_e_date =:no_per_coll_e_date ," 
        + " vip_remark =:vip_remark ,"
        + " new_acct_flag =:new_acct_flag , "
        + " bill_sending_country_code =:bill_sending_country_code ," 
        // 2020-09-08: Justin Wu====
        + " apr_user =:apr_user ,"
        + " apr_date = to_char(sysdate,'yyyyMMdd') ,"
        + " update_user =:update_user ,"
        + " update_date = to_char(sysdate,'yyyyMMdd') ,";
       // 2020-09-08: Justin Wu====
       // 2020-12-15 Justin Wu====
        if (wp.itemStr("isEMailEbillChg").equalsIgnoreCase("Y")) {
            strSql += 
               " e_mail_ebill =:e_mail_ebill ,"
            + " e_mail_ebill_date = to_char(sysdate,'yyyyMMdd') ,";
        }
       // 2020-12-15 Justin Wu====
        strSql+=
           " mod_user =:mod_user ,"
        + " mod_time =sysdate ," 
        + " mod_pgm =:mod_pgm ," 
        + " mod_seqno = nvl(mod_seqno, 0)+1 "
        + " where acno_p_seqno = :acno_p_seqno ";

    setString("risk_bank_no", colStr("risk_bank_no"));
    item2ParmStr("chg_addr_date");
    setString("vip_code", colStr("vip_code"));
    setString("bill_apply_flag", colStr("bill_apply_flag"));
    setString("bill_sending_zip", colStr("bill_sending_zip") + colStr("bill_sending_zip2"));
    setString("bill_sending_addr1", colStr("bill_sending_addr1"));
    setString("bill_sending_addr2", colStr("bill_sending_addr2"));
    setString("bill_sending_addr3", colStr("bill_sending_addr3"));
    setString("bill_sending_addr4", colStr("bill_sending_addr4"));
    setString("bill_sending_addr5", colStr("bill_sending_addr5"));
    item2ParmStr("no_tel_coll_flag");
    item2ParmStr("no_tel_coll_s_date");
    item2ParmStr("no_tel_coll_e_date");
    item2ParmStr("no_per_coll_flag");
    item2ParmStr("no_per_coll_s_date");
    item2ParmStr("no_per_coll_e_date");
    setString("vip_remark", colStr("vip_remark"));
    setString("new_acct_flag", colStr("new_acct_flag"));
    setString("bill_sending_country_code", colStr("bill_sending_country_code"));
    // 2002-09-08: Justin Wu====
    setString("apr_user", wp.loginUser);
    setString("update_user", colStr("online_mod_user"));
    // 2002-09-08: Justin Wu====
    // 2020-12-15 Justin Wu====
    if (wp.itemStr("isEMailEbillChg").equalsIgnoreCase("Y")) {
    	setString("e_mail_ebill", colStr("e_mail_ebill"));
    }
    // 2020-12-15 Justin Wu====
    setString("mod_user", colStr("online_mod_user"));
    setString("mod_pgm", "cmsp2010");
    item2ParmStr("acno_p_seqno");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update act_acno error !");
      rc = -1;
      return;
    }
  }

  /**
   * update 不須覆核資料
   * @return
   */
  public int updateAcno() {
    msgOK();
    strSql = " update act_acno set " ;
    strSql += " special_comment =:special_comment, "
    	       // 2002-09-08: Justin Wu====
    		   + " mod_pgm =:mod_pgm ,"
    	       + " mod_user =:mod_user ,"
    	       + " mod_time = sysdate ,"
    	       + " update_user =:update_user ,"
    	       + " update_date = to_char(sysdate,'yyyyMMdd') , "
    	       + " mod_seqno = nvl(mod_seqno, 0)+1 "
    	      // 2002-09-08: Justin Wu====
    		   + " where acno_p_seqno =:acno_p_seqno ";
    item2ParmStr("special_comment");
    // 2002-09-08: Justin Wu====
    setString("mod_pgm", "cmsm2010");
    setString("mod_user", wp.loginUser);
    setString("update_user", wp.loginUser);
    // 2002-09-08: Justin Wu====
    item2ParmStr("acno_p_seqno");
    
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update act_acno error");
    }
    return rc;
  }

private boolean isCompAddrEmptyInCrdIdnoOnline(String idPSeqno) {
	strSql = " select count(*) as crdIdnoOnlineCnt "
		 + " from crd_idno_online " 
         + " where id_p_seqno =? "
         + " and ( company_addr1='' or company_addr5='' ) "
         + " and upper(apr_flag)='N' ";
   sqlSelect(strSql, new Object[] {idPSeqno});
   if (colNum("crdIdnoOnlineCnt") >= 1) {
     errmsg("無法異動帳單註記，因公司地址異動為空白並待主管放行");
     return true;
   }

   return false;
}

public void cleanModCol() {
	for (int i = 0; i < aaCol.length; i++) {
		wp.colSet("mod_"+aaCol[i], "");
	}

}

}
