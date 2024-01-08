/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-02-12  V1.00.00  yash       program initial                            *
* 111-10-24  V1.00.01  Yang Bo    sync code from mega                        *
* 112-07-11  V1.00.02  Simon      parms reuse control in getWhereStr()       *
* 112-07-22  V1.00.03  Simon      1.取消全部覆核                             *
*                                 2.取消銷帳鍵值、借方科目                   *
*                                 3.取消利息指示碼                           *
* 112-12-18  V1.00.04  Simon      1.exclude act_acaj.process_flag='Y'        *
*                                 2.取消 update tsc_cgec_pre                 *
*                                 3.act_acaj.apr_date 以                     *
*                                   business_date 取代 sysDate               *
******************************************************************************/

package actp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;


public class Actp0010 extends BaseProc {

	String hRefresh = "";
  String hAcctKey = "";

    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;

        strAction = wp.buttonCode;
        if (eqIgno(wp.buttonCode, "X")) {
            /* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "R")) { 
            //-資料讀取- 
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "A")) {
            /* 新增功能 */
           // insertFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {
            /* 更新功能 */
            //updateFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {
            /* 刪除功能 */
           // deleteFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {
            /* 瀏覽功能 :skip-page*/
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        }else if (eqIgno(wp.buttonCode, "S2")) {
            /* 存檔*/
            strAction = "S2";
            dataProcess();
      //}else if (eqIgno(wp.buttonCode, "C3")) {
      //    /*全部覆核*/
      //    strAction = "C3";
      //    doApproveAll();
        }
        dddwSelect();
        initButton();
    }

    //for query use only
    private boolean getWhereStr() throws Exception {
   //String lsKey = "";
   	 if(wp.itemEmpty("ex_ackey")==false){
   		 hAcctKey = commString.acctKey(wp.itemStr2("ex_ackey"));
      	 if(hAcctKey.length()!=11){
      		 alertErr2("帳戶帳號輸入錯誤");
      		 return false;
      	 } 
   	 }
   	 
   	 
        wp.whereStr =" where 1=1 "
                    +" and ( act_acno.acno_p_seqno = act_acaj.p_seqno )  "
                    +" and ( substr(act_acaj.adjust_type, 1,2)<>'OP' )  "
                    +" and ( act_acaj.adjust_type <> 'AI01' )  "
                    +" and ( act_acaj.adjust_type <> 'CN01' )"
                    +" and ( act_acaj.process_flag != 'Y' ) "
                  //+sql_col(wp.item_nvl("ex_actype", "01"),"act_acaj.acct_type")
                    +sqlCol(wp.itemStr("ex_actype"),"act_acaj.acct_type")
                    +sqlCol(hAcctKey,"act_acno.acct_key")
                    +sqlCol(wp.itemStr("ex_crtuser"),"act_acaj.update_user")
                    +sqlCol(wp.itemStr("ex_curr_code"),"uf_nvl(act_acaj.curr_code,'901')")
                    ;
        
//        if(empty(wp.item_ss("ex_actype")) == false){
//            wp.whereStr += " and  act_acaj.acct_type = :ex_actype ";
//            setString("ex_actype", wp.item_ss("ex_actype"));
//        }
        
//        if(empty(wp.item_ss("ex_ackey")) == false){
//            wp.whereStr += " and  act_acno.acct_key = :ex_ackey ";
//            setString("ex_ackey", wp.item_ss("ex_ackey"));
//        }
        
        if(wp.itemStr("ex_apr").equals("1")){
            wp.whereStr += " and  act_acaj.apr_flag <> 'Y' ";
        }else{
        	 wp.whereStr += " and  act_acaj.apr_flag = 'Y' ";
        }
        
//        if(empty(wp.item_ss("ex_crtuser")) == false){
//            wp.whereStr += " and  act_acaj.update_user = :ex_crtuser ";
//            setString("ex_crtuser", wp.item_ss("ex_crtuser"));
//        }
        
//        if(empty(wp.item_ss("ex_curr_code")) == false){
//            wp.whereStr += " and  decode(act_acaj.curr_code,'',901, act_acaj.curr_code)  = :ex_curr_code ";
//            setString("ex_curr_code", wp.item_ss("ex_curr_code"));
//        }
        

        return true;
    }

    @Override
    public void queryFunc() throws Exception {
   	 
      getWhereStr();
      setSummary(wp.whereStr);
      wp.queryWhere = wp.whereStr;
      wp.setQueryMode();
      wp.itemSet("save_ex_apr",wp.itemStr("ex_apr"));
      wp.colSet("save_ex_apr",wp.itemStr("ex_apr"));
      queryRead();
    }
    
    public void setSummary(String lsWhere) throws Exception {
   	  int ilSqlNrow = 0;
   	  //--ex_acct_code
   	  if(wp.itemEmpty("ex_curr_code")==false) {
   		  wp.colSet("tl_curr", wp.itemStr("ex_curr_code"));
   	  } else {
   		  wp.colSet("tl_curr", "901");
   	  }

      wp.colSet("t1_cnt", 0);
      wp.colSet("tl_dr_amt", 0);
      wp.colSet("tl_cr_amt", 0);
      /***
   	  String sql1 = " select "
   		 	 	  	 + " act_acaj.acct_code , "
   			  		 + " sum(dr_amt) as wk_dr_amt , "
   			  		 + " sum(cr_amt) as wk_cr_amt  "
//   			 		 + " sum(uf_dc_amt(curr_code,dr_amt,dc_dr_amt)) as wk_dr_amt , "
//   			 		 + " sum(uf_dc_amt(curr_code,cr_amt,dc_cr_amt)) as wk_cr_amt "
   			 		   + " from act_acaj left join act_acno on act_acno.acno_p_seqno=act_acaj.p_seqno "
   			 		   + ls_where
   			 		   + " group by act_acaj.acct_code order by act_acaj.acct_code "
   			 		   ;
      ***/
      String sqlTot = "select count(*) as t1_cnt " 
                     + "from act_acaj left join act_acno on act_acno.acno_p_seqno=act_acaj.p_seqno " 
                     + lsWhere
                     ;
      sqlSelect(sqlTot);
      if (sqlRowNum > 0) {
         wp.colSet("t1_cnt", sqlInt("t1_cnt"));
      }

      getWhereStr();
      lsWhere = wp.whereStr;
      String sql1 = "", sql2 = "";
   	  if(wp.colStr("tl_curr").equals("901")) {
     	    sql1 = " select "
     			 		 + " act_acaj.acct_code , "
   	  		 		 + " decode(sum(dr_amt),null,0,sum(dr_amt)) as wk_dr_amt , "
   		  	 		 + " decode(sum(cr_amt),null,0,sum(cr_amt)) as wk_cr_amt  "
   			   		 + " from act_acaj left join act_acno on act_acno.acno_p_seqno=act_acaj.p_seqno "
   			 	  	 + lsWhere
   			 	  	 + " group by act_acaj.acct_code order by act_acaj.acct_code "
   			 		   ;
   	  } else {
     	    sql1 = " select "
     			 		 + " act_acaj.acct_code , "
   	  		 		 + " decode(sum(dc_dr_amt),null,0,sum(dc_dr_amt)) as wk_dr_amt , "
   		  	 		 + " decode(sum(dc_cr_amt),null,0,sum(dc_cr_amt)) as wk_cr_amt  "
   			   		 + " from act_acaj left join act_acno on act_acno.acno_p_seqno=act_acaj.p_seqno "
   			 	  	 + lsWhere
   			 	  	 + " group by act_acaj.acct_code order by act_acaj.acct_code "
   			 		   ;
   	  }
   	 
   	  sqlSelect(sql1);
   	 
   	  ilSqlNrow = sqlRowNum;

   	  String lsAcctCode1 = "" , lsAcctCode2 = "";
   	  for(int ii=0;ii<ilSqlNrow;ii++){
   		  if(ii<5) {
   			  lsAcctCode1 += " "+sqlStr(ii,"acct_code")+": 借方:"+sqlStr(ii,"wk_dr_amt")+" 貸方:"+sqlStr(ii,"wk_cr_amt"); 
   		  }	else	 {
   			  lsAcctCode2 += " "+sqlStr(ii,"acct_code")+": 借方:"+sqlStr(ii,"wk_dr_amt")+" 貸方:"+sqlStr(ii,"wk_cr_amt");
   		  }   		    		 
   	  }
   	  wp.colSet("ex_acct_code1", lsAcctCode1);
   	  wp.colSet("ex_acct_code2", lsAcctCode2);
   	 
      /***
   	  String sql2 = " select "
   			 	   + " sum(dr_amt) as tl_dr_amt , "
   			 	   + " sum(cr_amt) as tl_cr_amt  "
		 		 	 //+ " sum(uf_dc_amt(curr_code,dr_amt,dc_dr_amt)) as tl_dr_amt , "
		 		 	 //+ " sum(uf_dc_amt(curr_code,cr_amt,dc_cr_amt)) as tl_cr_amt "
		 		 		 + " from act_acaj left join act_acno on act_acno.acno_p_seqno=act_acaj.p_seqno "
		 		 		 + ls_where
		 		 		 ;
      ***/
   	 
      getWhereStr();
      lsWhere = wp.whereStr;
   	  if(wp.colStr("tl_curr").equals("901")) {
    	    sql2 = " select "
    			 	   + " decode(sum(dr_amt),null,0,sum(dr_amt)) as tl_dr_amt , "
   	  		 	   + " decode(sum(cr_amt),null,0,sum(cr_amt)) as tl_cr_amt  "
		 	  	 		 + " from act_acaj left join act_acno on act_acno.acno_p_seqno=act_acaj.p_seqno "
		 		  		 + lsWhere
		 		  		 ;
   	  } else {
    	    sql2 = " select "
    			 	   + " decode(sum(dc_dr_amt),null,0,sum(dc_dr_amt)) as tl_dr_amt , "
   	  		 	   + " decode(sum(dc_cr_amt),null,0,sum(dc_cr_amt)) as tl_cr_amt  "
		 	  	 		 + " from act_acaj left join act_acno on act_acno.acno_p_seqno=act_acaj.p_seqno "
		 		  		 + lsWhere
		 		  		 ;
   	  }
   	 
   	  sqlSelect(sql2);
   	 
    //wp.col_set("tl_curr", "901");
		  wp.colSet("tl_dr_amt", sqlStr("tl_dr_amt"));
		  wp.colSet("tl_cr_amt", sqlStr("tl_cr_amt"));
   	 
    }
    
    @Override
    public void queryRead() throws Exception {

       //ddd("actp0010-dispC：wp.item_ss(ex_apr)="+wp.item_ss("ex_apr"));
       //ddd("actp0010-dispD：wp.item_ss(save_ex_apr)="+wp.item_ss("save_ex_apr"));
       if(!wp.itemStr("ex_apr").equals(wp.itemStr("save_ex_apr"))) {
            wp.itemSet("ex_apr",wp.itemStr("save_ex_apr"));
            wp.colSet("ex_apr",wp.itemStr("save_ex_apr"));
       };
       //ddd("actp0010-dispE：wp.item_ss(ex_apr)="+wp.item_ss("ex_apr"));
       //ddd("actp0010-dispF：wp.item_ss(save_ex_apr)="+wp.item_ss("save_ex_apr"));

        wp.pageControl();

        wp.selectSQL = " act_acaj.crt_date  "
        		  			+ ",act_acaj.apr_flag"
        		  			+ ",decode(act_acaj.apr_flag,'','待放行','N','待放行','Y','解放行',act_acaj.apr_flag) as apr_name "
        		  			+ ",act_acaj.acct_type||'-'|| act_acno.acct_key as wk_acct_key"
        		  			+ ",act_acaj.crt_time "
        		  			+ ",act_acaj.p_seqno "
        		  			+ ",act_acaj.acct_type "
        		  			+ ",act_acno.acct_key "
        		  			+ ",act_acaj.adjust_type "
        		  			+ ",act_acaj.reference_no "
        		  			+ ",act_acaj.post_date "
        		  			+ ",uf_dc_amt(curr_code,orginal_amt,dc_orginal_amt) orginal_amt"
        		  			+ ",uf_dc_amt(curr_code,dr_amt,dc_dr_amt) dr_amt"
        		  			+ ",uf_dc_amt(curr_code,cr_amt,dc_cr_amt) cr_amt"
        		  			+ ",uf_dc_amt(curr_code,bef_amt,dc_bef_amt) bef_amt"
        		  			+ ",uf_dc_amt(curr_code,aft_amt,dc_aft_amt) aft_amt"
        		  			+ ",uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt) bef_d_amt"
        		  			+ ",uf_dc_amt(curr_code,aft_d_amt,dc_aft_d_amt) aft_d_amt"
        		  			+ ",act_acaj.acct_code"
        		  			+ ",act_acaj.function_code"
        		  			+ ",act_acaj.card_no"
        		  			+ ",act_acaj.cash_type"
        		  		//+ ",act_acaj.value_type"
        		  		//+ ",decode(act_acaj.value_type,'1','原起息日','2','覆核日',act_acaj.value_type) as tt_value_type "
        		  			+ ",act_acaj.trans_acct_type"
        		  			+ ",act_acaj.trans_acct_key"
        		  			+ ",act_acaj.interest_date"
        		  			+ ",act_acaj.adj_reason_code"
        		  			+ ",act_acaj.adj_comment"
        		  		//+ ",act_acaj.c_debt_key"
        		  		//+ ",act_acaj.debit_item"
        		  			+ ",act_acaj.apr_flag"
        		  			+ ",act_acaj.update_date"
        		  			+ ",act_acaj.update_user"
        		  			+ ",uf_acno_name(act_acno.acno_p_seqno) as chi_name"
        		  			+ ",uf_idno_id(act_acno.id_p_seqno) as id_no"        		  			
        		  			+ ",(select chi_short_name from ptr_actcode where acct_code = act_acaj.acct_code) as debt_chi "
        		  			+ ",decode(act_acaj.curr_code,'','901',act_acaj.curr_code) curr_code"
        		  			+ ",act_acaj.dr_amt tw_dr_amt"
        		  			+ ",act_acaj.cr_amt tw_cr_amt"
        		  			+ ",act_acaj.mod_user"
        		  			+ ",act_acaj.mod_time"
        		  		//+ ",act_acaj.mod_pgm"
        		  			+ ",act_acaj.mod_seqno"
        		  			+ ",hex(act_acaj.rowid) as rowid"
        		  			;
        
        wp.daoTable = "act_acaj, act_acno ";
        wp.whereOrder=" order by act_acaj.crt_date";
        getWhereStr();//因setSummary() in queryFunc() 當select 執行完後 parms 已清除
		  //if (!strAction.equals("Q")) { 
      //    getWhereStr();
		  //}

        pageQuery();

        wp.setListCount(1);
		    if (sqlNotFind()) {
		      if (!hRefresh.equals("Y")) {
			      alertErr(appMsg.errCondNodata);
		      } else {
            wp.notFound = "N";
		      }
			    return;
		    }

        wp.listCount[1] = wp.dataCnt;
        wp.setPageValue();
        apprDisabled("update_user");

    }

    @Override
    public void querySelect() throws Exception {
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {

    }

   
    @Override
	public void dataProcess() throws Exception {
    	 //-check approve-
//		 if (!check_approve(wp.item_ss("zz_apr_user"),
//		 wp.item_ss("zz_apr_passwd")))
//		 {
//		 return;
//		 }

		if (!wp.itemStr("ex_apr").equals(wp.itemStr("save_ex_apr"))) {
			alertErr("放行、解放行作業類別已變更, 請重新查詢再繼續");
			return;
		}

		String[] aaRowid = wp.itemBuff("rowid");
		String[] aaModSeqno = wp.itemBuff("mod_seqno");
		String[] opt = wp.itemBuff("opt");
		String[] aaReferenceNo = wp.itemBuff("reference_no");
		String[] aaAprFlag = wp.itemBuff("apr_flag");
		
		wp.listCount[0] = aaRowid.length;
		
		// check
		int rr = -1;
		int llOk = 0, llErr = 0;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = optToIndex(opt[ii]);
			if (rr < 0) {
				continue;
			}

		}
        //save
		if (llErr == 0) {
			// -update-
			rr = -1;
			for (int ii = 0; ii < opt.length; ii++) {
				rr = optToIndex(opt[ii]);
				if (rr < 0) {
					continue;
				}
				
				String lsSql = "select count(*) as tt  from dual"
						+" where exists (select 1 from act_debt      where reference_no =:reference_no1 )"
					 	+"	  or exists (select 1 from act_debt_hst  where reference_no =:reference_no2 ) ";
				setString("reference_no1", aaReferenceNo[rr]);
				setString("reference_no2", aaReferenceNo[rr]);
				sqlSelect(lsSql);
				int tot = (int) sqlNum("tt");
				if(tot == 0){
					alertErr("此筆資料在帳務檔及帳務歷史檔找不到，無法放行！");
					wp.colSet(rr, "ok_flag", "X");
		            llErr++;
		            continue;
				}
				
				
				String lsConf ="Y";
				String lsAprUser = wp.loginUser;
				String lsAprDate = getDBsysDate();
				if(aaAprFlag[rr].equals("Y")){
					 lsConf ="N";
				   lsAprUser = "";
				   lsAprDate = "";
				}
				
			
					//update act_acaj
					String usSq =" update act_acaj set "
						       +"   apr_flag=:apr_flag "
						       +",  apr_user=:apr_user "
						       +",  apr_date=:apr_date "
						       +",  mod_user=:mod_user "
						       +",  mod_time=sysdate "
						       +",  mod_pgm=:mod_pgm "
						       +",  mod_seqno =mod_seqno+1 "
						       +"where  hex(rowid) = :rowid  and mod_seqno = :mod_seqno ";
					setString("apr_flag",lsConf);
					setString("apr_user",lsAprUser);
				  setString("apr_date",lsAprDate);
					setString("mod_user",wp.loginUser);
					setString("mod_pgm",wp.itemStr("mod_pgm"));
					setString("rowid",aaRowid[rr]);
					setString("mod_seqno",aaModSeqno[rr]);
					sqlExec(usSq);
					if (sqlRowNum <= 0) {
  					alertErr("更新帳務調整檔(act_acaj)失敗！");
						wp.colSet(rr, "ok_flag", "X");
			            llErr++;
			            sqlCommit(0);
			            continue;    
					}else{
						wp.colSet(rr, "ok_flag", "V");
			            llOk++;
			            sqlCommit(1);
					}
					
					//update tsc_cgec_pre
/***
					String usSq2 =" update tsc_cgec_pre set "
						       +"   del_flag  = 'Y' "
						     //+",  del_date = :del_date' "
						       +",  del_date = :del_date "
						       +",  pay_flag  = 'N' "
						       +",  mod_user=:mod_user "
						       +",  mod_time=sysdate "
						       +",  mod_pgm=:mod_pgm "
						       +",  mod_seqno =mod_seqno+1 "
						       +"where  reference_no = :reference_no  ";
					setString("del_date",getSysDate());
					setString("mod_user",wp.loginUser);
					setString("mod_pgm",wp.itemStr("mod_pgm"));
					setString("reference_no",aaReferenceNo[rr]);
					sqlExec(usSq2);
***/			
		  }
		//alert_msg("處理: 成功筆數=" + ll_ok + "; 失敗筆數=" + ll_err + ";" );
		  String lsAlertMsg = "處理: 成功筆數= " + llOk + "; 失敗筆數= " + llErr + ";";
	    wp.respMesg =lsAlertMsg;
	  //wp.alertMesg(ls_alert_msg);//若前面有執行過 err_alert("xxx")，執行此method可覆蓋"資料錯誤:"的字樣
	    wp.errMesg =lsAlertMsg;//若前面有執行過 err_alert("xxx")，執行此method可覆蓋"資料錯誤:"的字樣，
	                             //此行程式碼不執行 prompt window
		}

	}

  private String getDBsysDate() throws Exception {
		String lsDbSysDate = "";
		String lsSql = "";
/***		
		lsSql  = " select to_char(sysdate,'yyyymmdd') as hcol_db_sysDate ";
		lsSql += " from dual ";
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			lsDbSysDate = sqlStr("hcol_db_sysDate");
			return lsDbSysDate;
		} else {
		  return "";
		}
***/
		lsSql = "Select business_date from ptr_businday fetch first 1 rows only ";
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
      lsDbSysDate = sqlStr("business_date");
			return lsDbSysDate;
		}else {
			return "";
		}


	}
    
    @Override
    public void initButton() {
        if (wp.respHtml.indexOf("_detl") > 0) {
            this.btnModeAud();
        }
      //if (wp.col_num("t1_cnt") <=wp.pageRows
      //      || wp.item_eq("ex_apr","2") ) {
        if (wp.colNum("t1_cnt") <= 0
            || wp.itemEq("ex_apr","2") ) {
            buttonOff("btnproc3_off");
        }
    }

/***
    void doApproveAll() throws Exception {
  		if (!wp.itemStr("ex_apr").equals(wp.itemStr("save_ex_apr"))) {
	  		alertErr("放行、解放行作業類別已變更, 請重新查詢再繼續");
		  	return;
		  }

       wp.listCount[0] =wp.itemRows("rowid");
       wp.pageControl();
       log("where="+wp.queryWhere);
    
       String sql1 = "select hex(act_acaj.rowid) as rowid, act_acaj.mod_seqno, act_acaj.apr_flag,"
                    + " reference_no, act_acaj.update_user as can_user"
                    + " from act_acaj left join act_acno on act_acno.acno_p_seqno=act_acaj.p_seqno "
                    + wp.queryWhere //待覆核--
                    + " order by act_acaj.crt_date";

       sqlSelect(sql1);
       if (sqlRowNum <=0) {
          alertErr("無資料可覆核");
          return;
       }
    
       int llOk=0, llErr=0;
       int llNrow =sqlRowNum;
       for (int ii = 0; ii < llNrow; ii++) {
          if (!apprBankUnit(sqlStr(ii,"can_user"),wp.loginUser)) {
             llErr++;
             continue;
          }

				  String sql2 = "select count(*) as tt  from dual"
					  	+" where exists (select 1 from act_debt      where reference_no =:reference_no1 )"
					 	  +"	  or exists (select 1 from act_debt_hst  where reference_no =:reference_no2 ) ";
				  setString("reference_no1", sqlStr(ii,"reference_no"));
				  setString("reference_no2", sqlStr(ii,"reference_no"));
				  sqlSelect(sql2);
				  int tot = (int) sqlNum("tt");
				  if(tot == 0){
					 //alert_msg("此筆資料在帳務檔及帳務歷史檔找不到，無法放行！");
		         llErr++;
		         continue;
				  }

				  String lsConf ="Y";
			
				  String lsAprUser = wp.loginUser;
				  String lsAprDate = getDBsysDate();
			
					//update act_acaj
					String usSq =" update act_acaj set "
						       +"   apr_flag=:apr_flag "
						       +",  apr_user=:apr_user "
						       +",  apr_date=:apr_date "
						       +",  mod_user=:mod_user "
						       +",  mod_time=sysdate "
						       +",  mod_pgm=:mod_pgm "
						       +",  mod_seqno =mod_seqno+1 "
						       +"where  hex(rowid) = :rowid  and mod_seqno = :mod_seqno ";
					setString("apr_flag",lsConf);
					setString("apr_user",lsAprUser);
					setString("apr_date",lsAprDate);
					setString("mod_user",wp.loginUser);
					setString("mod_pgm",wp.itemStr("mod_pgm"));
					setString("rowid",sqlStr(ii,"rowid"));
					setString("mod_seqno",sqlStr(ii,"mod_seqno"));
					sqlExec(usSq);
					if (sqlRowNum <= 0) {
					//wp.col_set(rr, "ok_flag", "X");
			       llErr++;
			       sqlCommit(0);
			       continue;    
					}else{
					//wp.col_set(rr, "ok_flag", "V");
			       llOk++;
			       sqlCommit(1);
					}
					
					//update tsc_cgec_pre
					String usSq2 =" update tsc_cgec_pre set "
						       +"   del_flag  = 'Y' "
						       +",  del_date = :del_date "
						       +",  pay_flag  = 'N' "
						       +",  mod_user=:mod_user "
						       +",  mod_time=sysdate "
						       +",  mod_pgm=:mod_pgm "
						       +",  mod_seqno =mod_seqno+1 "
						       +"where  reference_no = :reference_no  ";
					setString("del_date",getSysDate());
					setString("mod_user",wp.loginUser);
					setString("mod_pgm",wp.itemStr("mod_pgm"));
					setString("reference_no",sqlStr(ii,"reference_no"));
					sqlExec(usSq2);
				
       }
        //--
        wp.listCount[0] =0;
	    	hRefresh = "Y";
        queryFunc();
      //ok_alert("覆核處理: 成功筆數=" + ll_ok + "; 失敗筆數=" + ll_err);
			  String lsAlertMsg = "處理: 成功筆數= " + llOk + "; 失敗筆數= " + llErr + ";";
	      wp.respMesg =lsAlertMsg;
	      wp.alertMesg(lsAlertMsg);
    }
***/    
    @Override
    public void dddwSelect() {
        try {
        	

			wp.optionKey = wp.itemStr("ex_actype");
			this.dddwList("dddw_actype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");
			
			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_crtuser");
			this.dddwList("dddw_user", "sec_user", "usr_id", "usr_cname", "where 1=1  order by usr_id");
			
			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_curr_code");
			this.dddwList("dddw_curr", "ptr_sys_idtab ", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY'  order by wf_id");

			
        }
        catch(Exception ex) {}
    }

    
}
