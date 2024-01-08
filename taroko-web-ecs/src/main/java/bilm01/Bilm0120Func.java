/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-10-03  V1.00.01  Ryan       Initial                              *
***************************************************************************/
package bilm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Bilm0120Func extends FuncEdit {
	
	String kk1 = "";
	String kk2 = "";

  public Bilm0120Func(TarokoCommon wr) {
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
  public void dataCheck() {

	 	if(wp.itemEmpty("p_seqno")) {
	  		errmsg("p_seqno已不存在，請從新讀取");
	  		return;
	  	}

	 	//取卡號或取身分證號
	 	if (this.isAdd()) {
	 		if (wp.itemEmpty("id_no") && wp.itemEmpty("card_no")) {
		  		errmsg("帳戶身分證號及卡號，不能同時為空值");
		  		return;
		  	}

	 		//輸入卡號時取身分證號
	 		if (!wp.itemEmpty("card_no")) {
	 			String lsSql = "select 1 as tot_cnt, (select id_no from crd_idno "
	 					     + " where id_p_seqno = x.id_p_seqno) as id_no "
	 					     + " from crd_card x where card_no = ? ";
				Object[] param = new Object[] { wp.itemStr("card_no") };
				sqlSelect(lsSql, param);
				if (colNum("tot_cnt") <= 0) {
					errmsg("資料庫無此卡號，無法新增");
					return;
				}
				
				wp.itemSet("id_no", colStr("id_no"));
	 		} else {
	 			String lsSql = "select 1 as tot_cnt, card_no "
					     + " from crd_card where id_p_seqno in (select id_p_seqno from crd_idno "
					     + "                        where id_no = ? ) "
					     + " order by current_code,new_end_date "
					     + " fetch first 1 rows only ";
	 			Object[] param = new Object[] { wp.itemStr("id_no") };
	 			sqlSelect(lsSql, param);
	 			if (colNum("tot_cnt") <= 0) {
	 				errmsg("無卡號資料，無法新增");
	 				return;
	 			}
			
	 			wp.itemSet("card_no", colStr("card_no"));
	 		}
	 			
	 	}

	 	if(!this.ibDelete) {
	 		if(wp.itemEmpty("id_no") && wp.itemEmpty("card_no")) {
		  		errmsg("帳戶身分證號及卡號，不能同時為空值");
		  		return;
		  	}
		  	
			if(wp.itemEmpty("install_type")) {
		  		errmsg("分期種類，不能為空值");
		  		return;
		  	}
			
			if(wp.itemNum("install_tot_amt") < 3000) {
		  		errmsg("分期金額不可小於3000");
		  		return;
		  	}
			
			if(wp.itemEmpty("mcht_no")) {
		  		errmsg("分期特店代號，不能為空值");
		  		return;
		  	}
			
			if(wp.itemEmpty("product_no")) {
		  		errmsg("分期期數，不能為空值");
		  		return;
		  	}

			if(wp.itemNum("min_pay_bal")>0) {
	 			errmsg("最低應繳金額(餘額) 不可大於0");
	 			return;
		 	}
			
			if(wp.itemNum("ttl_amt_bal") > wp.itemNum("line_of_credit_amt")) {
	 			errmsg("可分期金額 不可大於 總額度");
	 			return;
		 	}
			
			if(wp.itemNum("install_tot_amt") > wp.itemNum("ttl_amt_bal")) {
	 			errmsg("分期金額 不可大於 可分期金額");
	 			return;
		 	}
	 	}

		if (this.isAdd()) {
			// 檢查新增資料是否重複
			String lsSql = "select count(*) as tot_cnt from bil_contract "
					     + "where p_seqno = ? and post_cycle_dd = 0 "
					     + "and mcht_no = ? ";
			Object[] param = new Object[] { wp.itemStr("p_seqno"), wp.itemStr("mcht_no") };
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("分期資料今日已新增，請檢查!!");
				return;
			}

			kk1 = varsStr("aa_contract_no");
			kk2 = varsStr("aa_contract_seq_no");

			// 檢查新增資料是否重複
			lsSql = "select count(*) as tot_cnt from bil_contract " 
				  + "where contract_no = ? and contract_seq_no = ?";
			param = new Object[] { kk1,kk2 };
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("資料已存在，無法新增");
				return;
			}
		} else {
			
			kk1 = wp.itemStr("contract_no");
		    kk2 = wp.itemStr("contract_seq_no");
			
			// -other modify-
			sqlWhere = " where contract_no = ? and contract_seq_no = ?  ";
			Object[] param = new Object[] {kk1,kk2 };
			if (isOtherModify("bil_contract", sqlWhere, param)) {
				errmsg("請重新查詢");
				return;
			}
		}
  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;
    
    String lsSql = "";
    Object[] param = null;
    busi.SqlPrepare sp = new SqlPrepare();
    
    sp.sql2Insert("bil_contract");
    sp.ppstr("contract_no", kk1);
    sp.ppstr("contract_seq_no", kk2);
    sp.ppstr("contract_kind", "1");
    sp.ppstr("mcht_no", wp.itemStr("mcht_no"));
    
    //讀mcht_chi_name
	lsSql = "select mcht_chi_name from bil_merchant where mcht_no = ? ";
	param = new Object[] { wp.itemStr("mcht_no") };
	sqlSelect(lsSql, param);
    sp.ppstr("mcht_chi_name", colStr("mcht_chi_name"));
    
    sp.ppstr("product_no", wp.itemStr("product_no"));
    //讀product_name
    lsSql = "select product_name from bil_prod where mcht_no = ? and product_no = ? ";
	param = new Object[] { wp.itemStr("mcht_no"),wp.itemStr("product_no")};
	sqlSelect(lsSql, param);
    sp.ppstr("product_name", colStr("product_name")); 
	
    sp.ppstr("card_no", wp.itemStr("card_no"));
    //讀crd_card
    lsSql = "select acct_type,stmt_cycle,id_p_seqno,acno_p_seqno,p_seqno,new_end_date from crd_card where card_no = ? ";
	param = new Object[] { wp.itemStr("card_no")};
	sqlSelect(lsSql, param);
	
    sp.ppstr("acct_type", colStr("acct_type"));
    sp.ppstr("limit_end_date", colStr("new_end_date"));
    sp.ppstr("first_post_date", "");
    sp.ppnum("post_cycle_dd", 0);
    sp.ppstr("purchase_date", wp.sysDate); //get system date
    sp.ppstr("stmt_cycle", colStr("stmt_cycle"));
    sp.ppnum("install_curr_term", 0);
    sp.ppstr("all_post_flag", "N");
    sp.ppstr("forced_post_flag", "N");
    sp.ppstr("fee_flag", wp.itemStr("fee_flag"));
    sp.ppstr("cps_flag", "N");
    sp.ppnum("tot_amt", wp.itemNum("install_tot_amt"));
    sp.ppnum("qty", 1);
    sp.ppnum("install_tot_term", wp.itemNum("install_tot_term"));
    sp.ppnum("unit_price", wp.itemNum("unit_price"));
    if ("F".equals(wp.itemStr("fee_flag"))) {
    	sp.ppnum("first_remd_amt", wp.itemNum("first_install_amt") - wp.itemNum("unit_price"));
    } else {
    	sp.ppnum("remd_amt", wp.itemNum("first_install_amt") - wp.itemNum("unit_price"));
    }
    sp.ppnum("extra_fees", wp.itemNum("extra_fees"));
    sp.ppnum("trans_rate", wp.itemNum("trans_rate"));
    sp.ppnum("year_fees_rate", wp.itemNum("year_fees_rate"));
    sp.ppstr("year_fees_date", wp.sysDate);
    sp.ppstr("auto_delv_flag", "Y");
    sp.ppstr("clt_forced_post_flag", "N");
    sp.ppstr("bill_prod_type", "S"); //特店歸屬類別
    sp.ppstr("apr_flag", "N");
    sp.ppstr("apr_date", "");
    sp.ppstr("installment_kind", "N");
    
    //分期入帳時間 (畫面Y:當期;N:次期) (DB--0:次期;1:當期)
    if ("Y".equals(wp.itemStr("installment_delay"))) {
    	sp.ppstr("first_post_kind", "1");
    } else {
    	sp.ppstr("first_post_kind", "0");
    }

    sp.ppstr("new_it_flag", "Y");
    sp.ppstr("id_p_seqno", colStr("id_p_seqno"));  //select from crd_card
    sp.ppstr("acno_p_seqno", colStr("acno_p_seqno")); //select from crd_card
    sp.ppstr("p_seqno", colStr("p_seqno")); //select from crd_card 
    sp.ppstr("ccas_resp_code", "00");
    sp.ppstr("nccc_resp_code", "00");
    sp.ppstr("spec_flag", wp.itemStr("spec_flag"));  //專款專用註記
    
    sp.addsql(", mod_time ", ", sysdate");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);

    sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0) {
        errmsg(sqlErrtext);
    }
    
    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update bil_contract set tot_amt = ?, install_tot_term = ?, "
           + "unit_price = ?, extra_fees = ?, trans_rate = ?, year_fees_rate = ? , ";
    
    if ("F".equals(wp.itemStr("fee_flag"))) {
    	strSql += "first_remd_amt = ? , "; 
    } else {
    	strSql += "remd_amt = ? , ";
    }
    
    strSql += "spec_flag = ? , apr_flag='N', apr_date='', ";
    strSql += "mod_user = ?, mod_time  = sysdate, mod_pgm   = ?, mod_seqno = mod_seqno+1 "; 
           
    strSql += sqlWhere ;

    Object[] param = new Object[] {wp.itemNum("install_tot_amt"), wp.itemNum("install_tot_term"),
    		wp.itemNum("unit_price"), wp.itemNum("extra_fees"),  wp.itemNum("trans_rate"), 
    		wp.itemNum("year_fees_rate"), (wp.itemNum("first_install_amt") - wp.itemNum("unit_price")),
    		wp.itemStr("spec_flag"),wp.loginUser,wp.modPgm(), kk1, kk2};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete bil_contract "
    		+ sqlWhere;

    Object[] param = new Object[] {kk1, kk2};

    sqlExec(strSql, param);
    
    return rc;
  }
  // ************************************************************************

} // End of class
