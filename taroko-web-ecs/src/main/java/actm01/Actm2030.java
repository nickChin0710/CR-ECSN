/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 109-04-15  V1.00.01  Alex       add auth_query                             *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-12-05  V1.00.04  Simon      免違約金、循環息合併維護                   *
* 112-12-06  V1.00.05  Simon      1.update wp.pageCountSql                   *
*                                 2.調整刪除後 show no_flag checkbox         *
******************************************************************************/

package actm01;

import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon; 

public class Actm2030 extends BaseEdit {
	CommString commString = new CommString();
	Actm2030Func func;
//String hNextAcctMonth="";	
  String mAccttype="";	
  String mAcctkey="";	
  String mCurrcode = "";
     int mLlOk = 0;
  
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
      super.wp = wr;
      rc=1;

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
          insertFunc();
      } else if (eqIgno(wp.buttonCode, "U")) {
          /* 更新功能 */
          updateFunc();
      } else if (eqIgno(wp.buttonCode, "D")) {
          /* 刪除功能 */
          deleteFunc();
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
      }

      dddwSelect();
      initButton();
  }
  
  @Override
  public void queryFunc() throws Exception {
 	 if(wp.itemEmpty("ex_acct_key")){
 		 alertErr2("帳戶帳號:不可空白");
 		 return ;
 	 }
 	 
 	 String lsAcctKey = "";
 	 lsAcctKey = commString.acctKey(wp.itemStr2("ex_acct_key"));
 	 if(lsAcctKey.length()!=11){
 	 	 alertErr2("帳戶帳號: 輸入錯誤");
 	 	 return ;
 	 }

 	 ColFunc func =new ColFunc();
 	 func.setConn(wp);   	    
 	    
 	 if (func.fAuthQuery(wp.modPgm(), commString.mid(lsAcctKey, 0,10))!=1) {   		 
 	  	 alertErr2(func.getMsg()); 
 	  	 return ; 
 	 }
 	 
     wp.setQueryMode();
     queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
      
    String lsPSeqno = getInitParm();
    if (lsPSeqno.equals("")) {
    	alertErr2("帳戶帳號不正確");
    	return;
    }
          
		wp.sqlCmd = " SELECT nvl(hex(b.rowid),'30687') as rowid " +
  			" ,'901' as curr_code " +
  			" ,decode(a.no_penalty_flag,'','N',a.no_penalty_flag) as no_flag " +
  			" ,a.no_penalty_s_month as no_s_month " +
  			" ,a.no_penalty_e_month as no_e_month " +
  			" ,b.log_data " +
  			" ,b.chg_date " +
  			" ,b.chg_user " +
  			" ,'1' as ex_save_flag " +
  			" from act_acno a " +
				" left join act_dual b on b.dual_key = a.acct_type||a.acct_key " +
				" and b.func_code ='0701' " +
			  " where a.acno_p_seqno = a.p_seqno and a.p_seqno = :0701_p_seqno " +
        " union " +
		    " SELECT nvl(hex(d.rowid),'30687') as rowid " +
				" ,e.curr_code " +
				" ,decode(e.no_interest_flag,'','N',e.no_interest_flag) as no_flag " +
				" ,e.no_interest_s_month as no_s_month " +
				" ,e.no_interest_e_month as no_e_month " +
				" ,d.log_data " +
				" ,d.chg_date " +
				" ,d.chg_user " +
				" ,'2' as ex_save_flag " +
			  " from act_acct_curr e, act_acno c " +
			//" left join act_dual d on d.dual_key = c.acct_type||c.acct_key||e.curr_code " +
				" left join act_dual d on d.dual_key = c.acct_type||c.acct_key||'901' " +
				" and d.func_code ='0710' " +
			  " where e.p_seqno = :0710_p_seqno and e.p_seqno = c.acno_p_seqno " +
			  " and e.curr_code = '901' ";
			  //循環息(理論上有多筆 by curr_code，但 tcb 改成只有台幣 
		setString("0701_p_seqno", lsPSeqno);
		setString("0710_p_seqno", lsPSeqno);

		wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
        alertErr(appMsg.errCondNodata);
        return;
    }

    listWkdata();
    wp.setPageValue();

  }
    
  private String getInitParm() throws Exception {
		String lsSql = "";
		
		mAccttype = wp.itemStr2("ex_acct_type");
    mAcctkey  = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
		
		lsSql  = " select a.acct_type, a.acct_key, a.p_seqno," 
		       + " uf_acno_name(a.p_seqno) as acno_cname, a.acct_status,"
		       + " p.next_acct_month ";
		lsSql += " from act_acno a, ptr_workday p";
		lsSql += " where 1=1 and a.stmt_cycle=p.stmt_cycle ";
		lsSql += " and a.acno_flag <> 'Y' ";
		lsSql += " and a.acct_type = :acct_type and a.acct_key = :acct_key ";
		setString("acct_type", mAccttype);
		setString("acct_key",  mAcctkey);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
		//hNextAcctMonth = sqlStr("next_acct_month");
			wp.colSet("next_acct_month", sqlStr("next_acct_month"));
			wp.colSet("acct_type", sqlStr("acct_type"));
			wp.colSet("acct_key", sqlStr("acct_key"));
			wp.colSet("p_seqno", sqlStr("p_seqno"));
			wp.colSet("ex_cname", sqlStr("acno_cname"));
			wp.colSet("ex_acct_status", sqlStr("acct_status"));
			wp.colSet("tt_acct_status", commString.decode(sqlStr("acct_status"), ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));
			return sqlStr("p_seqno");
		}
		return "";
	}
    
  void listWkdata() throws Exception {
		String ss = "";

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			ss = wp.colStr(ii, "curr_code");
			wp.colSet(ii, "tt_curr_code", getCurrName(ss));
			
			ss = wp.colStr(ii, "ex_save_flag");
  		if (ss.equals("1")) {
	  		wp.colSet(ii, "tt_no_flag_type", "違約金");
		  } else {
	  		wp.colSet(ii, "tt_no_flag_type", "循環息");
		  }
			
			ss = wp.colStr(ii, "rowid");
			wp.colSet(ii, "wk_dual", eqIgno(ss,"30687")? "N" : "Y");
			wp.colSet(ii, "del_disable", eqIgno(ss,"30687")? "disabled" : "");

	    ss = wp.colStr(ii, "no_flag");
		  wp.colSet(ii, "no_flag-Y", ss.equals("Y")?"checked":"");//value="${ser_num}"
/***
  		if (!ss.equals("Y")) {
	  		wp.colSet(ii, "no_s_month", hNextAcctMonth);
	  		wp.colSet(ii, "no_e_month", hNextAcctMonth);
		  } 
***/		
			wp.colSet(ii, "chk_no_flag", ss);

			ss = wp.colStr(ii, "no_s_month");
			wp.colSet(ii, "chk_no_s_month", ss);
			ss = wp.colStr(ii, "no_e_month");
			wp.colSet(ii, "chk_no_e_month", ss);
		//wp.col_set(ii, "no_flag-Y", "checked");
		//wp.col_set(ii, "aopt-Y", "checked");

			ss = wp.colStr(ii, "log_data");
			if (empty(ss)) continue;
			String lsPenFlag, lsSMonth, lsEMonth;
			lsPenFlag = commString.mid(ss, 0, 1);
			lsSMonth = commString.mid(ss, 1, 6);
			lsEMonth = commString.mid(ss, 7, 6);
		  if (eqIgno(lsEMonth,"999912")) lsEMonth = "";
			
		  wp.colSet(ii, "no_flag", lsPenFlag);
		  wp.colSet(ii, "no_flag-Y", lsPenFlag.equals("Y")?"checked":"");//value="${ser_num}"
		//wp.col_set(ii, "aopt-Y", "");
			wp.colSet(ii, "no_s_month", lsSMonth);
			wp.colSet(ii, "no_e_month", lsEMonth);

	    wp.colSet(ii, "chk_no_flag", lsPenFlag);
	    wp.colSet(ii, "chk_no_s_month", lsSMonth);
	    wp.colSet(ii, "chk_no_e_month", lsEMonth);
		}
	}
    
    String getCurrName(String currcode) throws Exception {
		String rtn = currcode;
		String lsSql = "select wf_desc from ptr_sys_idtab where wf_type = 'DC_CURRENCY' and wf_id = :curr_code ";
		setString("curr_code", currcode);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			rtn += "_" + sqlStr("wf_desc");
		}
		return rtn;
	}

    @Override
    public void querySelect() throws Exception {
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
    }

    @Override
    public void saveFunc() throws Exception {
    	func =new Actm2030Func(wp);
    	
    	ofcUpdate();
      
//        rc = func.dbSave(is_action);
//        ddd(func.getMsg());
//        if (rc!=1) {
//            err_alert(func.getMsg());
//        }
//        this.sql_commit(rc);
    }

    void ofcUpdate() throws Exception {
    	int llOk = 0, llErr = 0, llMod = 0, llNoUpd = 0;
		String lsPSeqno, lsExNoFlag, lsExSaveFlag;
		String lsAcctType, lsAcctKey, lsExAcctStatus;
		String lsFuncCode="", lsDualNo="", lsLog="", ssNoFlag="", ssAopt="", ssDopt="";
		String lsSql="", lsNextAcctMonth="";
		
		String[] aaRowid = wp.itemBuff("rowid");
		String[] aaCurrCode = wp.itemBuff("curr_code");
		String[] aaExSaveFlag = wp.itemBuff("ex_save_flag");
		String[] aaNoFlag = wp.itemBuff("no_flag");
		String[] aaNoSMonth = wp.itemBuff("no_s_month");
		String[] aaNoEMonth = wp.itemBuff("no_e_month");
		String[] aaChkNoFlag = wp.itemBuff("chk_no_flag");
		String[] aaChkNoSMonth = wp.itemBuff("chk_no_s_month");
		String[] aaChkNoEMonth = wp.itemBuff("chk_no_e_month");
	//String[] aaOpt = wp.itemBuff("aopt");
		String[] ddOpt = wp.itemBuff("dopt");
		
		//detail_control
		int rowcntaa = 0;
		if (!(aaRowid == null) && !empty(aaRowid[0])) rowcntaa = aaRowid.length;
		wp.listCount[0] = rowcntaa;
		
		//保留原選項
		for (int ll = 0; ll < aaRowid.length; ll++) {
			ssNoFlag= checkBoxOptOn(ll, aaNoFlag)==true? "Y" : "N";
			wp.colSet(ll, "no_flag-Y", ssNoFlag.equals("Y")?"checked":"");
		//ssAopt= checkBoxOptOn(ll, aaOpt)==true? "Y" : "N";
		//wp.colSet(ll, "aopt-Y", ssAopt.equals("Y")?"checked":"");
			ssDopt= checkBoxOptOn(ll, ddOpt)==true? "Y" : "N";
			wp.colSet(ll, "dopt-Y", ssDopt.equals("Y")?"checked":"");
		}

		lsPSeqno = wp.itemStr2("p_seqno");
		if (empty(lsPSeqno)) {
		//alert_err("未讀取資料, 不可存檔/刪除");
			alertErr("請先查詢, 再存檔");
			return;
		}

		lsSql = "select p.next_acct_month from act_acno a, ptr_workday p "
		    	 + "where a.stmt_cycle = p.stmt_cycle "
		    	 + "and a.acno_p_seqno = :ps_p_seqno ";
		setString("ps_p_seqno", lsPSeqno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
		  lsNextAcctMonth = sqlStr("next_acct_month");
		} 
    //起迄年月需大於或等於下次關帳年月

		for (int ll = 0; ll < aaRowid.length; ll++) {

/***
			if ((checkBoxOptOn(ll, aaOpt) == true) && (checkBoxOptOn(ll, ddOpt) == true)) {
					alertErr("不可同時勾選[修改]與[刪除] ");
					return;
			}

			if ((checkBoxOptOn(ll, aaOpt) == false) && (checkBoxOptOn(ll, ddOpt) == false)) {
				continue;
			}
***/
			
			if (!checkBoxOptOn(ll, ddOpt)) {
				llMod++;
				//checkbox須另外處理
				ssNoFlag= checkBoxOptOn(ll, aaNoFlag)==true? "Y" : "N";
				if (eqIgno(ssNoFlag,"Y")) {
					if (empty(aaNoSMonth[ll]) ) {
					//alert_err("[免收旗標] 為 [Y] 時, [起/迄月份] 必須要有值");
				  	wp.colSet(ll, "a-ok_flag","X");
						alertErr("資料有誤，請輸入起始生效年月");
            llErr++;
					//return;
					}
/***
		  		if (chkStrend(lsNextAcctMonth,aaNoSMonth[ll])==false || 
		  		    chkStrend(lsNextAcctMonth,aaNoEMonth[ll])==false) {
            alertErr("起迄年月需大於或等於下次關帳年月");
				  	return;
	    		}
***/
		  		if (chkStrend(lsNextAcctMonth,aaNoSMonth[ll])==false) { 
			  	  wp.colSet(ll, "a-ok_flag","X");
            alertErr("起始年月需大於或等於下次關帳年月");
            llErr++;
					//return;
	    		}
		  		if (!empty(aaNoEMonth[ll]) && 
		    		  chkStrend(lsNextAcctMonth,aaNoEMonth[ll])==false) {
			  	  wp.colSet(ll, "a-ok_flag","X");
            alertErr("迄止年月需大於或等於下次關帳年月");
            llErr++;
					//return;
	    		}
				  if (!empty(aaNoEMonth[ll]) && 
				      chkStrend(aaNoSMonth[ll],aaNoEMonth[ll])==false) {
			  	  wp.colSet(ll, "a-ok_flag","X");
            alertErr("起迄月份  輸入錯誤");
            llErr++;
					//return;
	    		}
				}
				
//	System.out.println("checkPoint: AAA ["+ss_no_flag+"]:["+aa_chk_no_flag[ll]+"]"
//					+"["+aa_no_s_month[ll]+"]:["+aa_chk_no_s_month[ll]+"]"
//					+"["+aa_no_e_month[ll]+"]:["+aa_chk_no_e_month[ll]+"]");
				//alert_err("資料未異動, 不可修改");
				if (eqIgno(ssNoFlag,aaChkNoFlag[ll]) 
					&& eqIgno(aaNoSMonth[ll],aaChkNoSMonth[ll])
					&& eqIgno(aaNoEMonth[ll],aaChkNoEMonth[ll])) {
					llNoUpd++;
				}
			}
		}
	
	  if (llErr > 0 )  {
		  return;
	  }
		
	  if (llNoUpd == 2 )  {
		  alertErr("資料未異動, 存檔無效");
		  return;
	  }
		
		lsAcctType = wp.itemStr2("acct_type");
		lsAcctKey  = wp.itemStr2("acct_key");
		lsExAcctStatus = wp.itemStr2("ex_acct_status");
		for (int ll = 0; ll < aaRowid.length; ll++) {
			// -option-ON-
/***
			if ((checkBoxOptOn(ll, aaOpt) == false) && (checkBoxOptOn(ll, ddOpt) == false)) {
				continue;
			}
***/
			if (!checkBoxOptOn(ll, ddOpt)) {
		  	ssNoFlag= checkBoxOptOn(ll, aaNoFlag)==true? "Y" : "N";
				if (eqIgno(ssNoFlag,aaChkNoFlag[ll]) 
					&& eqIgno(aaNoSMonth[ll],aaChkNoSMonth[ll])
					&& eqIgno(aaNoEMonth[ll],aaChkNoEMonth[ll])) {
			  	continue;
				}
			}

			// -delete detail-
			if (eqIgno(aaRowid[ll],"30687")==false) {
				func.varsSet("rowid", aaRowid[ll]);
				if (func.dbDelete() < 0) {
					alertErr(func.getMsg());
					sqlCommit(0);
			    if (checkBoxOptOn(ll, ddOpt)) {
			      wp.colSet(ll, "d-ok_flag","X");
				  } else {
				  	wp.colSet(ll, "a-ok_flag","X");
				  }
			  //if (checkBoxOptOn(ll, aaOpt) ) {
			  //  wp.colSet(ll, "a-ok_flag","X");
				//}
					return;
				} else {
					llOk++;
			    if (checkBoxOptOn(ll, ddOpt)) {
			    //wp.col_set(ll, "no_flag-Y", "");
			      wp.colSet(ll, "d-ok_flag","V");
				  }
			  //if (checkBoxOptOn(ll, aaOpt) ) {
			  //wp.col_set(ll, "aopt-Y", "");
			  //wp.col_set(ll, "dopt-Y", "checked");
			  //wp.colSet(ll, "a-ok_flag","V");
				//}
			  }
			}

			if (checkBoxOptOn(ll, ddOpt)) continue;
		//if (!checkBox_opt_on(ll, aa_no_flag)) continue;
			
			ssNoFlag= checkBoxOptOn(ll, aaNoFlag)==true? "Y" : "N";
			// -insert-
  		lsExSaveFlag = aaExSaveFlag[ll];
			if (eqIgno(lsExSaveFlag,"1")) {  //違約金 (理論上單筆)
				lsFuncCode = "0701";
				lsDualNo = lsAcctType+lsAcctKey;
				lsLog  = fixLength(ssNoFlag,1);
				lsLog += fixLength(aaNoSMonth[ll],6);
				lsLog += fixLength(empty(aaNoEMonth[ll])?"999912":aaNoEMonth[ll],6);
				lsLog += fixLength(lsExAcctStatus,1);
//		    System.out.println("of_update 0701: ["+ls_log+"]");
			} else {  //循環息(理論上有多筆 by curr_code，但 tcb 改成只有台幣 )
				lsFuncCode = "0710";
				lsDualNo = lsAcctType+lsAcctKey+aaCurrCode[ll];
				lsLog  = fixLength(ssNoFlag,1);
				lsLog += fixLength(aaNoSMonth[ll],6);
				lsLog += fixLength(empty(aaNoEMonth[ll])?"999912":aaNoEMonth[ll],6);
				lsLog += fixLength(lsExAcctStatus,1);
//		    System.out.println("of_update 0710: ["+ls_log+"]");
			}
	    	
			func.varsSet("func_code", lsFuncCode);
			func.varsSet("dual_key", lsDualNo);
			func.varsSet("log_data", lsLog);
			if (func.dbUpdate() == 1) {
			  wp.colSet(ll, "a-ok_flag","V");
				if (eqIgno(aaRowid[ll],"30687")) {
					llOk++;
				}
			} else {
				llErr++;
			  wp.colSet(ll, "a-ok_flag","X");
			}
		}
		alertMsg("存檔處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr);
		sqlCommit(llErr > 0 ? 0 : 1);
		mLlOk =	llOk;
	  if (llErr == 0) {
	    queryFunc();
    //wp.colSet("p_seqno", "");
	  }

	}

    @Override
    public void initButton() {
    //if (wp.respHtml.indexOf("_detl") > 0) {
    //    this.btnMode_aud();
    //}
    	String sKey = "1st-page";
      if (wp.respHtml.equals("actm2030"))  {
         wp.colSet("btnUpdate_disable","");
         this.btnModeAud(sKey);
			  if  (wp.colStr("btnUpdate_disable").equals(""))   {
            wp.colSet("btnUpdate", "");
        }
			  else   {
            wp.colSet("btnUpdate", "disabled");
			  }
      }

    }

    @Override
    public void dddwSelect() {
        try {	
//			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_curr_code");
			this.dddwList("dddw_curr_code", "PTR_CURRCODE", "curr_code", "curr_chi_name", "where 1=1 order by curr_code");
        }
        catch(Exception ex) {}
    }

    String fixLength(String laText, int laWidth) throws Exception {
		String rtn = laText;
		if (empty(rtn)) rtn="";
		int fnum;
		if (laText.length() < laWidth) {
			fnum = laWidth - laText.length();
			rtn += commString.fill(' ',fnum);
		}
		return rtn;
	}
    
    String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
	//if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}

}

