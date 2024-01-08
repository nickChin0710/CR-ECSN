	/*****************************************************************************
	*                                                                            *
	*                              MODIFICATION LOG                              *
	*                                                                            *
	* DATE       Version   AUTHOR      DESCRIPTION                               *
	* ---------  --------  ---------- ------------------------------------------ *
	* 106-06-22  V1.00.00  Andy       program initial                            *
	* 107-02-06  V1.00.01  Andy       update 刪除成本計算欄位                    *            
	* 109-04-21  V1.00.02  YangFang   updated for project coding standard        * 
	* 109-07-15  V1.00.03  tanwei     新增bug修改                                *
	* 110-01-30  V1.00.04  Justin     default mail_cost as 0                     *
	* 111-11-23  V1.00.05  Simon      新增分行移管作業                           *
	* 112-01-30  V1.00.06  Simon      分行移管作業需同時更新dbc_card.reg_bank_no *
	******************************************************************************/

	package ptrm01;

	import busi.FuncEdit; 
	import taroko.com.TarokoCommon;

	public class Ptrm0050Func extends FuncEdit {
	  String mKkDranch = "";


	  public Ptrm0050Func(TarokoCommon wr) {
	    wp = wr;
	    this.conn = wp.getConn();
	  }

	  @Override
	  public int querySelect() {
	    // TODO Auto-generated method
	    return 0;
	  }

	  @Override
	  public int dataSelect() {
	    // TODO Auto-generated method stub
	    return 0;
	  }

	  @Override
	  public void dataCheck() {
		//String ls_conf_flag=wp.itemStr("conf_flag");
	    // 判斷使用頁面上方代碼或table欄位的代碼
	    if (this.ibAdd) {
	      if (empty(wp.itemStr("kk_branch")) == false)
	        mKkDranch = wp.itemStr("kk_branch");
	      else {
	        errmsg("請輸入分行代碼");
	        return;
	      }
	    } else {
	      mKkDranch = wp.itemStr("branch");
	    }


	    if (this.isAdd()) {
	      // 檢查新增資料是否重複
	      String lsSql = "select count(*) as tot_cnt from gen_brn where branch = ?";
	      Object[] param = new Object[] {mKkDranch};
	      sqlSelect(lsSql, param);
	      if (colNum("tot_cnt") > 0) {
	        errmsg("資料已存在，無法新增");
	      }
	      return;
	    }

	    if (wp.itemStr("mail_fee_flag").equals("Y") && wp.itemNum("mail_cost") < 1) {
	      errmsg("郵寄成本有值!");
	      return;
	    }

	    // -other modify-
	    sqlWhere = " where branch= ? " + " and nvl(mod_seqno,0) = ? ";
	    Object[] param = new Object[] {mKkDranch, wp.modSeqno()};
	    if (this.isOtherModify("gen_brn", sqlWhere, param)) {
	      errmsg("請重新查詢 !");
	      return;
	    }
	  }

	  @Override
	  public int dbInsert() {
	    actionInit("A");
	    dataCheck();
	    if (rc != 1) {
	      return rc;
	    }

	    strSql = "insert into gen_brn (" + " branch " + " , curr_code" + " , full_chi_name"
	        + " , brief_chi_name" + " , full_eng_name" + " , brief_eng_name" 
	        + " , merged_to_brn"
	        + " , corp_no" + " , branch_act_num"
	        + " , comp_addr" + " , comp_name" + " , user_code" + " , user_pass" + " , south_flag"
	        + " , area_flag" + " , chi_addr_1" + " , chi_addr_2" + " , chi_addr_3" + " , chi_addr_4"
	        + " , chi_addr_5" + " , eng_addr_1" + " , eng_addr_2" + " , eng_addr_3" + " , eng_addr_4"
	        + " , brn_test" + " , connect_flag" + " , connect_date" + " , mail_fee_flag"
	        + " , mail_cost" + " , mod_user, mod_time , mod_pgm , mod_seqno ) values ("
	        + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate,?,1 )";
	    Object[] param = new Object[] {mKkDranch, // key
	        wp.itemStr("curr_code"), wp.itemStr("full_chi_name"), wp.itemStr("brief_chi_name"),
	        wp.itemStr("full_eng_name"), wp.itemStr("brief_eng_name"), 
	        wp.itemStr("merged_to_brn"),  
	        wp.itemStr("corp_no"), wp.itemStr("branch_act_num"),
	        wp.itemStr("comp_addr"), wp.itemStr("comp_name"), wp.itemStr("user_code"),
	        wp.itemStr("user_pass"), wp.itemStr("south_flag").equals("Y") ? "Y" : "N",
	        wp.itemStr("area_flag"), wp.itemStr("chi_addr_1"), wp.itemStr("chi_addr_2"),
	        wp.itemStr("chi_addr_3"), wp.itemStr("chi_addr_4"), wp.itemStr("chi_addr_5"),
	        wp.itemStr("eng_addr_1"), wp.itemStr("eng_addr_2"), wp.itemStr("eng_addr_3"),
	        wp.itemStr("eng_addr_4"), wp.itemStr("brn_test").equals("Y") ? "Y" : "N",
	        wp.itemStr("connect_flag").equals("Y") ? "Y" : "N", wp.itemStr("connect_date"),
	        wp.itemStr("mail_fee_flag").equals("Y") ? "Y" : "N", wp.itemNum("mail_cost"), wp.loginUser,
	        wp.itemStr("mod_pgm")};
	    // System.out.println("is_sql:"+is_sql);
	    sqlExec(strSql, param);
	    if (sqlRowNum <= 0) {
	      errmsg(sqlErrtext);
	    }

	    return rc;
	  }

	  @Override
	  public int dbUpdate() {
	    actionInit("U");
	    dataCheck();
	    if (rc != 1) {
	      return rc;
	    }

			if(!wp.itemEmpty("merged_to_brn") 
				 && !eqIgno(wp.itemStr("merged_to_brn"),wp.itemStr("chk_merged_to_brn"))
			  ) 
			{
	  		updateCrdCard();
		  	if(rc!=1)	return rc;
	  		updateDbcCard();
		  	if(rc!=1)	return rc;
			}

	    strSql = "update gen_brn set " + " curr_code=?" + " , full_chi_name=?" + " , brief_chi_name=?"
	        + " , full_eng_name=?" + " , brief_eng_name=?" 
	        + " , merged_to_brn=?" 
	        + " , corp_no=?" + " , branch_act_num=?"
	        + " , comp_addr=?" + " , comp_name=?" + " , user_code=?" + " , user_pass=?"
	        + " , south_flag=?" + " , area_flag=?" + " , chi_addr_1=?" + " , chi_addr_2=?"
	        + " , chi_addr_3=?" + " , chi_addr_4=?" + " , chi_addr_5=?" + " , eng_addr_1=?"
	        + " , eng_addr_2=?" + " , eng_addr_3=?" + " , eng_addr_4=?" + " , brn_test=?"
	        + " , connect_flag=?" + " , connect_date=?" + " , mail_fee_flag=?" + " , mail_cost=?"
	        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
	        + sqlWhere;
	    Object[] param = new Object[] {wp.itemStr("curr_code"), wp.itemStr("full_chi_name"),
	        wp.itemStr("brief_chi_name"), wp.itemStr("full_eng_name"), wp.itemStr("brief_eng_name"),
	        wp.itemStr("merged_to_brn"), 
	        wp.itemStr("corp_no"), wp.itemStr("branch_act_num"),
	        wp.itemStr("comp_addr"), wp.itemStr("comp_name"), wp.itemStr("user_code"),
	        wp.itemStr("user_pass"), wp.itemStr("south_flag").equals("Y") ? "Y" : "N",
	        wp.itemStr("area_flag"), wp.itemStr("chi_addr_1"), wp.itemStr("chi_addr_2"),
	        wp.itemStr("chi_addr_3"), wp.itemStr("chi_addr_4"), wp.itemStr("chi_addr_5"),
	        wp.itemStr("eng_addr_1"), wp.itemStr("eng_addr_2"), wp.itemStr("eng_addr_3"),
	        wp.itemStr("eng_addr_4"), wp.itemStr("brn_test").equals("Y") ? "Y" : "N",
	        wp.itemStr("connect_flag").equals("Y") ? "Y" : "N", wp.itemStr("connect_date"),
	        wp.itemStr("mail_fee_flag").equals("Y") ? "Y" : "N", wp.itemNum("mail_cost"), wp.loginUser,
	        wp.itemStr("mod_pgm"), mKkDranch, wp.modSeqno()};
	    /*
	     * 驗證用 System.out.println("is_sql"+is_sql); System.out.println("value:"+m_kk_branch+":" +
	     * wp.item_ss("ac_full_name")+":" + wp.item_ss("ac_brief_name")+":" +
	     * wp.item_ss("memo3_flag")+":" + wp.item_ss("memo3_kind")+":" + wp.item_ss("dr_flag")+":" +
	     * wp.item_ss("cr_flag")+":" + wp.item_ss("brn_rpt_flag") );
	     */
	    rc = sqlExec(strSql, param);
	    if (sqlRowNum <= 0) {
	      errmsg(this.sqlErrtext);
	    }
	    return rc;

	  }

		public int updateCrdCard() {
			msgOK();
			strSql = " update crd_card set "
					 + " reg_bank_no = ? , "
					 + " mod_user = ? , "
					 + " mod_time = sysdate , "				 
					 + " mod_pgm = ? , "
					 + " mod_seqno = nvl(mod_seqno,0)+1 "
					 + " where reg_bank_no = ? "
					 ;
			
	    Object[] param = new Object[] {wp.itemStr("merged_to_brn"), wp.loginUser,
	        wp.itemStr("mod_pgm"), mKkDranch};
			
	    rc = sqlExec(strSql, param);
	    if (sqlRowNum < 0) {
	      errmsg(this.sqlErrtext);
	    }
			
			return rc;
		}

		public int updateDbcCard() {
			msgOK();
			strSql = " update dbc_card set "
					 + " reg_bank_no = ? , "
					 + " mod_user = ? , "
					 + " mod_time = sysdate , "				 
					 + " mod_pgm = ? , "
					 + " mod_seqno = nvl(mod_seqno,0)+1 "
					 + " where reg_bank_no = ? "
					 ;
			
	    Object[] param = new Object[] {wp.itemStr("merged_to_brn"), wp.loginUser,
	        wp.itemStr("mod_pgm"), mKkDranch};
			
	    rc = sqlExec(strSql, param);
	    if (sqlRowNum < 0) {
	      errmsg(this.sqlErrtext);
	    }
			
			return rc;
		}

	  @Override
	  public int dbDelete() {
	    actionInit("D");
	    dataCheck();
	    if (rc != 1) {
	      return rc;
	    }
	    strSql = "delete gen_brn " + sqlWhere;
	    System.out.println("is_sql:" + strSql);
	    Object[] param = new Object[] {mKkDranch, wp.modSeqno()};
	    System.out.println("param:" + mKkDranch + ":" + wp.modSeqno());
	    rc = sqlExec(strSql, param);
	    if (sqlRowNum <= 0) {
	      errmsg(this.sqlErrtext);
	    }
	    return rc;
	  }

	}
