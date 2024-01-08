package rskm02;
/**
 * 2019-1210:  Alex  son_card_flag = N all clear
 * 2019-1202:  Alex  dataCheck fix 
 * 2019-0912   JH    approve.passwd_list
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
 * 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *
 * 2023-1017   JH    dbDelete()
 *
 * */
import busi.FuncAction;

public class Rskm0922Func extends FuncAction {
  String cardNo = "", lsAdjReason = "", lsAdjLocFlag = "";
  boolean ibApprove = false;

  @Override
  public void dataCheck() {
    cardNo = wp.itemStr("card_no");

    if (empty(cardNo)) {
      errmsg("卡號:  不可空白");
      return;
    }

    if (ibDelete) {
      if (checkLog() == false) {
        errmsg("非待覆核資料不可刪除");
      }
      return;
    }
    
    if(wp.itemEq("son_card_flag", "Y") && checkCurrent()==false) {
    	errmsg("此卡非有效卡 , 不可設定為子卡");
    	return ;
    }
    
    if (wp.itemEq("son_card_flag", "N") && wp.itemNum("new_indiv_crd_lmt") != 0) {
      errmsg("調整子卡額度需為子卡");
      return;
    }

    if (wp.itemEq("son_card_flag", "N")) {
      wp.itemSet("new_indiv_crd_lmt", "0");
      wp.itemSet("indiv_inst_lmt", "0");
      wp.itemSet("card_adj_limit", "0");
      wp.itemSet("card_adj_date1", "");
      wp.itemSet("card_adj_date2", "");
    }

    if (wp.itemEq("son_card_flag", "Y")) {
      if (wp.itemNum("new_indiv_crd_lmt") <= 0) {
        errmsg("子卡額度 不可 <=0");
        return;
      }
      
      if(wp.itemNum("indiv_inst_lmt") > 100) {
    	  errmsg("子卡分期付款限額% 不可大於 100");
    	  return ;
      }
      
    } else if (wp.itemEq("son_card_flag", "N")) {
      wp.itemSet("new_indiv_crd_lmt", "0");
      wp.itemSet("indiv_inst_lmt", "0");
      wp.itemSet("card_adj_limit", "0");
      wp.itemSet("card_adj_date1", "");
      wp.itemSet("card_adj_date2", "");
      if (wp.itemNum("new_indiv_crd_lmt") != 0) {
        errmsg("非子卡 子卡額度須 =0  ");
        return;
      }
    } else {
      errmsg("子卡旗標不可空白");
      return;
    }

    if (wp.itemEmpty("adj_eff_start_date") == false && wp.itemEmpty("adj_eff_end_date") == false) {
      if (wp.itemStr2("adj_eff_start_date").compareTo(this.getSysDate()) <= 0
          || wp.itemStr2("adj_eff_end_date").compareTo(this.getSysDate()) >= 0) {
        if (wp.itemNum("card_adj_limit") > (wp.itemNum("tot_amt_month")+wp.itemNum("over_pay"))) {
          errmsg("臨調額度不可超過正卡臨調額度+溢繳款額度");
          return;
        }
      }
    } else {
      if (wp.itemNum("card_adj_limit") > wp.itemNum("line_of_credit_amt")) {
        errmsg("臨調額度不可超過永調額度");
        return;
      }
    }

    if (wp.itemNum("new_indiv_crd_lmt") > wp.itemNum("line_of_credit_amt")) {
      errmsg("子卡額度不得超過帳戶額度");
      return;
    }



    if (wp.itemNum("card_adj_limit") > 0) {
      if (wp.itemEmpty("card_adj_date1") || wp.itemEmpty("card_adj_date2")) {
        errmsg("指定日期: 不可空白");
        return;
      }

      if (chkStrend(wp.itemStr("card_adj_date1"), wp.itemStr("card_adj_date2")) == -1) {
        errmsg("指定日期: 起迄錯誤");
        return;
      }
    }

    if (!wp.itemEmpty("card_adj_date1") || !wp.itemEmpty("card_adj_date2")) {
      if (wp.itemNum("card_adj_limit") <= 0) {
        errmsg("指定日期調整額度不可空白");
        return;
      }
    }

    if (wp.itemNum("new_indiv_crd_lmt") > wp.itemNum("indiv_crd_lmt")) {
      lsAdjReason = "I";
      lsAdjLocFlag = "1";
    } else if (wp.itemNum("new_indiv_crd_lmt") < wp.itemNum("indiv_crd_lmt")) {
      lsAdjReason = "J";
      lsAdjLocFlag = "2";
    } else {
      lsAdjLocFlag = "3";
    }

    ofcapp.EcsApprove ooappr = new ofcapp.EcsApprove(wp);
    if (wp.itemEmpty("approval_user") == false && wp.itemEmpty("approval_passwd") == false) {
      try {
        if (ooappr.cardLimitApprove(modPgm, wp.itemStr2("approval_user"),
            wp.itemStr2("approval_passwd")) != 1) {
          errmsg(ooappr.getMesg());
          return;
        }
        ibApprove = true;
      } catch (Exception ex) {
      }
    }
  }
  
  boolean checkCurrent() {
	  
	  String sql1 = "select current_code from crd_card where card_no = ? ";
	  sqlSelect(sql1,new Object[] {cardNo});
	  
	  if(sqlRowNum<=0)	return false;
	  
	  if(colEq("current_code","0")==false)	return false ;
	  
	  return true ;
  }
  
  boolean checkLog() {

    String sql1 = " select count(*) as log_cnt from rsk_acnolog "
        + " where log_mode ='1' and log_type ='1' and kind_flag ='C' "
        + " and emend_type ='4' and card_no = ? and apr_flag<>'Y' ";

    sqlSelect(sql1, new Object[] {cardNo});

    if (colNum("log_cnt") > 0)
      return true;

    return false;
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    if (ibApprove) {
      if (wp.itemEq("apr_flag", "Y"))
        deleteLog();
      if ((wp.itemNum("new_indiv_crd_lmt") != wp.itemNum("indiv_crd_lmt"))
          || (wp.itemNum("ori_indiv_inst_lmt") != wp.itemNum("indiv_inst_lmt"))) {
        updateCrdCard();
        if (rc != 1)
          return rc;
      }

      updateCcaCardBase();
      if (rc != 1)
        return rc;
      insertLog();
      if (rc != 1)
        return rc;
      if (wp.itemEq("sms_flag", "Y"))
        insertSmsMsgDtl();
    } else {
      deleteLog();
      if (rc != 1)
        return rc;
      insertLog();
    }

    return rc;
  }

  void updateCrdCard() {
    // --永調
    msgOK();
    strSql = " update crd_card set " + " indiv_crd_lmt =:indiv_crd_lmt , "
        + " indiv_inst_lmt =:indiv_inst_lmt , " + " son_card_flag =:son_card_flag "
        + commSqlStr.modxxxSet(modUser, modPgm) + " where card_no =:kk1 ";

    item2ParmNum("indiv_crd_lmt", "new_indiv_crd_lmt");
    item2ParmNum("indiv_inst_lmt");
    item2ParmNvl("son_card_flag", "N");
    setString("kk1", cardNo);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update crd_card error ");
    }
  }

  void updateCcaCardBase() {
    msgOK();
    strSql = " update cca_card_base set " + " card_adj_limit =:card_adj_limit , "
        + " card_adj_date1 =:card_adj_date1 , " + " card_adj_date2 =:card_adj_date2 , "
        + " adj_chg_user =:adj_chg_user " + " where card_no =:kk1 ";

    item2ParmNum("card_adj_limit");
    item2ParmStr("card_adj_date1");
    item2ParmStr("card_adj_date2");
    if (wp.itemNum("card_adj_limit") == 0) {
      setString("adj_chg_user", "");
    } else {
      setString("adj_chg_user", wp.loginUser);
    }

    setString("kk1", cardNo);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update cca_card_base error ");
    }
  }

  void insertLog() {
    msgOK();
    strSql = " insert into rsk_acnolog (" + " kind_flag ," + " card_no ," + " acno_p_seqno ,"
        + " acct_type ," + " id_p_seqno ," + " corp_p_seqno ," + " log_date ," + " log_mode ,"
        + " log_type ," + " log_reason ," + " bef_loc_amt ," + " aft_loc_amt ," + " bef_loc_cash ,"
        + " aft_loc_cash ," + " adj_loc_flag ," + " emend_type ," + " sms_flag ,"
        + " card_adj_limit ," + " card_adj_date1 ," + " card_adj_date2 ," + " user_dept_no ,"
        + " son_card_flag ," + " apr_flag ," + " apr_user ," + " apr_date ," + " mod_user ,"
        + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ( " + " 'C' ," + " :card_no ,"
        + " :acno_p_seqno ," + " :acct_type ," + " :id_p_seqno ," + " :corp_p_seqno ,"
        + " to_char(sysdate,'yyyymmdd') ," + " '1' ," + " '1' ," + " :log_reason ,"
        + " :bef_loc_amt ," + " :aft_loc_amt ," + " :bef_loc_cash ," + " :aft_loc_cash ,"
        + " :adj_loc_flag ," + " '4' ," + " :sms_flag ," + " :card_adj_limit ,"
        + " :card_adj_date1 ," + " :card_adj_date2 ," + " :user_dept_no ," + " :son_card_flag ,"
        + " :apr_flag ," + " :apr_user ," + " :apr_date ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " 1 " + " ) ";

    item2ParmStr("card_no");
    item2ParmStr("acno_p_seqno");
    item2ParmStr("acct_type");
    item2ParmStr("id_p_seqno");
    item2ParmStr("corp_p_seqno");
    setString("log_reason", lsAdjReason);
    item2ParmNum("bef_loc_amt", "indiv_crd_lmt");
    item2ParmNum("aft_loc_amt", "new_indiv_crd_lmt");
    item2ParmNum("bef_loc_cash", "ori_indiv_inst_lmt");
    item2ParmNum("aft_loc_cash", "indiv_inst_lmt");
    setString("adj_loc_flag", lsAdjLocFlag);
    item2ParmNvl("sms_flag", "N");
    item2ParmNum("card_adj_limit");
    item2ParmStr("card_adj_date1");
    item2ParmStr("card_adj_date2");
    setString("user_dept_no", wp.loginDeptNo);
    item2ParmNvl("son_card_flag", "N");

    if (ibApprove) {
      setString("apr_flag", "Y");
      setString("apr_user", wp.itemStr2("approval_user"));
      setString("apr_date", getSysDate());
    } else {
      setString("apr_flag", "N");
      setString("apr_user", "");
      setString("apr_date", "");
    }

    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert rsk_acnolog error ");
    }
  }

  void deleteLog() {
    msgOK();

    strSql = " delete rsk_acnolog where log_mode ='1' and log_type ='1' and kind_flag ='C' "
        + " and emend_type ='4' and card_no = :card_no and apr_flag <> 'Y' ";

    item2ParmStr("card_no");

    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("delete rsk_acnolog error ");
    } else
      rc = 1;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " delete rsk_acnolog where log_mode ='1' and log_type ='1' and kind_flag ='C' "
        + " and emend_type ='4' and card_no = :card_no "
        +" and apr_flag <>'Y' ";

    setString("card_no", cardNo);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete rsk_acnolog error !");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  void insertSmsMsgDtl() {
    busi.func.SmsMsgDetl ooSms = new busi.func.SmsMsgDetl();
    ooSms.setConn(wp);

//    if (wp.itemEmpty("sms_parm")) {
//      errmsg("請指定簡訊參數");
//      return;
//    }

    rc = ooSms.rskP0922(varsStr("acno_p_seqno"), wp.itemStr("major_id_p_seqno"), "RSKM0922-1");

  }

}
