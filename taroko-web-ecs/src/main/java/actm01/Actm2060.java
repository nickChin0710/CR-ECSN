/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 109-04-15  V1.00.01  Alex       add auth_query									  *
* 111-10-20  V1.00.03  Machao      sync from mega & updated for project coding standard                                                                           *
******************************************************************************/

package actm01;

import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon; 

public class Actm2060 extends BaseEdit {
	CommString commString = new CommString();
	Actm2060Func func;
    String mAccttype="";	
    String mAcctkey="";	
    String mPSeqno = "";
    
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
    public void dddwSelect() {
        try {	
			wp.optionKey = wp.itemStr2("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");
        }
        catch(Exception ex) {}
    }
    
    @Override
    public void queryFunc() throws Exception {
    	//設定queryRead() SQL條件
   	 
   	 if(wp.itemEmpty("ex_acct_key")){
   		 alertErr2("帳戶帳號: 不可空白");
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
			alertErr2("無此帳號");
			return;
		}
        
		mPSeqno = lsPSeqno;
		String lsDualNo="", lsDualData="";
		lsDualNo = mAccttype + mAcctkey;
		String lsSql = "select log_data from act_dual "
						  + "where dual_key = :dual_key "
						  + "  and func_code = '0704' ";
		setString("dual_key", lsDualNo);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			lsDualData=sqlStr("log_data");
			setActDual(lsDualData);
		} else {
			lsSql = "select acct_status, "
					+ "rc_use_indicator, "
					+ "mp_flag, "
					+ "min_pay_rate, "
					+ "min_pay_rate_s_month, "
					+ "min_pay_rate_e_month, "
					+ "mp_1_amt, "
					+ "mp_1_s_month, "
					+ "mp_1_e_month, "
					+ "'N' as ex_dual_flag "
					+ "from act_acno "
					+ "where acno_p_seqno = :p_seqno ";
			setString("p_seqno", lsPSeqno);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				String lsMpFlag="",lsMp0Yms="", lsMp0Yme="";
				lsMpFlag = sqlStr("mp_flag");
				lsMp0Yms = sqlStr("min_pay_rate_s_month");
				lsMp0Yme = sqlStr("min_pay_rate_e_month");
				if (eqIgno(lsMp0Yms,"99991231")) lsMp0Yms = "";
				if (eqIgno(lsMp0Yme,"99991231")) lsMp0Yme = "";
//				if isNull(ls_mp_flag) or pos("0|1",ls_mp_flag)=0 then ls_mp_flag='0'
				if (empty(lsMpFlag) || commString.pos("0|1",lsMpFlag)==0) lsMpFlag = "0";
				
				wp.colSet("ex_rc_use_indicator", sqlStr("rc_use_indicator"));
				wp.colSet("ex_mpflag", lsMpFlag);
//				wp.col_set("ex_mp0_rate", (int) sql_num("min_pay_rate"));
				wp.colSet("ex_mp0_rate", sqlStr("min_pay_rate"));
				wp.colSet("ex_mp0_s_month", lsMp0Yms);
				wp.colSet("ex_mp0_e_month", lsMp0Yme);
				wp.colSet("ex_mp1_rate", sqlStr("mp_1_amt"));
				wp.colSet("ex_mp1_s_month", sqlStr("mp_1_s_month"));
				wp.colSet("ex_mp1_e_month", sqlStr("mp_1_e_month"));
				wp.colSet("ex_acct_status", sqlStr("acct_status"));
				wp.colSet("tt_acct_status", commString.decode(sqlStr("acct_status"), ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));
				wp.colSet("ex_dual_flag", sqlStr("ex_dual_flag"));
				
				wp.colSet("pho_update_disable", "");
				wp.colSet("pho_delete_disable", "disabled");
			} else {
				alertErr("查無帳戶資料, acct_type="+mAccttype+", acct_key="+mAcctkey);
			}
		}
		
		//MP最低應繳$$-
		if (wfGetActgeneralN() < 1) {
			alertErr("讀取 標準MP百分比 資料error");
		}

		wp.colSet("queryReadCnt", "0");
		dw2Query();
	}

	int wfGetActgeneralN() throws Exception {
		double mp1Rate=0;
		String mp3Rate="0", mpMcode="0";
		String mp1Bl, mp1Ao, mp1Ca, mp1Id, mp1Ot;  
		String lsType;

		mp1Bl = "N"; mp1Ao = "N"; mp1Ca = "N"; 
		mp1Id = "N"; mp1Ot = "N"; 
		lsType = mAccttype;
		if (empty(lsType)) return -1;

		String lsSql = "select mp_1_rate, mp_3_rate, mp_mcode, "
					+ "decode(mp_1_bl_flag,'','N',mp_1_bl_flag) as mp1_bl, "
					+ "decode(mp_1_ao_flag,'','N',mp_1_ao_flag) as mp1_ao, "
					+ "decode(mp_1_ca_flag,'','N',mp_1_ca_flag) as mp1_ca, "
					+ "decode(mp_1_id_flag,'','N',mp_1_id_flag) as mp1_id, "
					+ "decode(mp_1_ot_flag,'','N',mp_1_ot_flag) as mp1_ot "
					+ "from ptr_actgeneral_n "
					+ "where acct_type = :acct_type "+sqlRownum(1);
		setString("acct_type", lsType);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			mp1Rate = sqlNum("mp_1_rate");
			mp3Rate = sqlStr("mp_3_rate");
			mpMcode = sqlStr("mp_mcode");
			mp1Bl = sqlStr("mp1_bl");
			mp1Ao = sqlStr("mp1_ao");
			mp1Ca = sqlStr("mp1_ca");
			mp1Id = sqlStr("mp1_id");
			mp1Ot = sqlStr("mp1_ot");
		}
    	
		wp.colSet("ex_min_percent_payment", mp1Rate);
		wp.colSet("ex_min_percent_payment2", mp3Rate);
		wp.colSet("mp_1data", mp1Bl+" "+mp1Ao+" "+mp1Ca+" "+mp1Id+" "+mp1Ot);
		wp.colSet("mp1_bl", mp1Bl);
		wp.colSet("mp1_ao", mp1Ao);
		wp.colSet("mp1_ca", mp1Ca);
		wp.colSet("mp1_id", mp1Id);
		wp.colSet("mp1_ot", mp1Ot);
		wp.colSet("ex_mp_mcode", mpMcode);

    	return 1;
    }
    
    private String getInitParm() throws Exception {
		String lsSql = "";
		
		mAccttype = wp.itemStr2("ex_acct_type");
    	mAcctkey  = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
		
		lsSql  = " select acct_type, acct_key, p_seqno, uf_acno_name(p_seqno) as acno_cname ";
		lsSql += ", uf_idno_name(id_p_seqno) as id_name, uf_corp_name(corp_p_seqno) as corp_name ";
		lsSql += " from act_acno ";
		lsSql += " where 1=1 ";
		lsSql += "and acno_p_seqno = p_seqno ";
		lsSql += "and acct_type = :acct_type and acct_key = :acct_key ";
		setString("acct_type", mAccttype);
		setString("acct_key",  mAcctkey);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("acct_type", sqlStr("acct_type"));
			wp.colSet("acct_key", sqlStr("acct_key"));
			wp.colSet("p_seqno", sqlStr("p_seqno"));
//			wp.col_set("ex_cname", sql_ss("acno_cname"));
			wp.colSet("ex_cname", sqlStr("id_name"));
			wp.colSet("ex_corp_cname", sqlStr("corp_name"));
			return sqlStr("p_seqno");
		}
		return "";
	}
    
    void setActDual(String asData) {
    	double mp1Rate=0;
		String mp3Rate="0", lsRcUse="", lsAcctSts="";
		String lsMp0Yms="", lsMp0Yme="", lsMpFlag="", lsMp1Yms="", lsMp1Yme="";
		int lmMp0Rate=0, lmMp1Amt=0;
   	System.out.println("setActDual: as_data="+asData);

//		mp1_rate	= commString.ss_2Num(zzStr.mid(as_data, 0, 6));	// min_percent_payment
		lsRcUse	  = commString.mid(asData, 6, 1);		// rc_use_indicator
		lmMp0Rate	= commString.strToInt(commString.mid(asData, 7, 3));	// min_pay_rate
		lsMp0Yms	= commString.mid(asData, 10, 6);	// min_pay_rate_s_month
		lsMp0Yme	= commString.mid(asData, 16, 6);	// min_pay_rate_e_month
		lsAcctSts	= commString.mid(asData, 22, 1);	// acct_status
		lsMpFlag	= commString.mid(asData, 23, 1);	// ex_mpflag
		lmMp1Amt	= (int) commString.strToNum(commString.mid(asData, 24, 14));	// ex_mp_1amt
		lsMp1Yms	= commString.mid(asData, 38, 6);	// ex_mp1_yms
		lsMp1Yme	= commString.mid(asData, 44, 6);	// ex_mp1_yme
//		mp3_rate	= zzStr.mid(as_data, 50, 6);	// ex_min_percent_payment2

		wp.colSet("ex_mpflag", lsMpFlag);
		wp.colSet("ex_mp0_rate", lmMp0Rate);
		wp.colSet("ex_mp0_s_month", lsMp0Yms);
		wp.colSet("ex_mp0_e_month", lsMp0Yme);
		wp.colSet("ex_mp1_rate", lmMp1Amt);
		wp.colSet("ex_mp1_s_month", lsMp1Yms);
		wp.colSet("ex_mp1_e_month", lsMp1Yme);
		wp.colSet("ex_rc_use_indicator", lsRcUse);
		wp.colSet("ex_acct_status", lsAcctSts);
		wp.colSet("tt_acct_status", commString.decode(lsAcctSts, ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));
		wp.colSet("ex_dual_flag", "Y");

		wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
		wp.colSet("pho_update_disable", "");
		wp.colSet("pho_delete_disable", "");
    }
    
    //dw_2.ofc_retrieve()
    void dw2Query() throws Exception {
    	//雙幣卡--特殊固定MP畫面
    	wp.selectSQL = "p_seqno, "
			     + "acct_type, "
			     + "curr_code, "
			     + "mp_1_amt, "
			     + "mp_1_s_month, "
			     + "mp_1_e_month "
			     ;
		wp.daoTable = "act_acct_curr ";
		wp.whereStr = "where p_seqno = :p_seqno "
				+ "and curr_code <> '901' ";
		setString("p_seqno", mPSeqno);

		wp.whereOrder =" order by curr_code";
	
		pageQuery();
		wp.notFound = "N";
		wp.setListCount(1);
    	
		listWkdata();
		wp.colSet("queryReadCnt", wp.selectCnt);
    }
    
    void listWkdata() throws Exception {
    	String ss="",lsDualNo="", lsDualData="";
    	double dd=0;
		lsDualNo = mAccttype + mAcctkey;
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			ss = wp.colStr(ii,"curr_code");
			lsDualData = getDualData(lsDualNo+ss);
			if (empty(lsDualData)) continue;
			
		//dd = zzStr.ss_2Num(zzStr.mid(ls_dual_data,  0, 15));	// mp_1_amt
			dd = commString.strToNum(commString.mid(lsDualData, 24, 14));	// mp_1_amt
			wp.colSet(ii,"mp_1_amt", dd);
		//ss = zzStr.mid(ls_dual_data, 15, 6);	// mp_1_s_month
			ss = commString.mid(lsDualData, 38, 6);	// mp_1_s_month
			wp.colSet(ii,"mp_1_s_month", ss);
		//ss = zzStr.mid(ls_dual_data, 21, 6);	// mp_1_e_month
			ss = commString.mid(lsDualData, 44, 6);	// mp_1_e_month
			wp.colSet(ii,"mp_1_e_month", ss);

		}
	}

	String getDualData(String asKey) throws Exception {
		String rtn = "";
		String lsSql = "select log_data from act_dual "
				  + "where dual_key = :dual_key "
				  + "  and func_code = '0704' ";
		setString("dual_key", asKey);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) rtn=sqlStr("log_data");

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
    	func = new Actm2060Func(wp);
    	
    	System.out.println("saveFunc: init");
		  if (ofValidation()<0) return;
		
		  ofUpdate();  //set log_data

      rc = func.dbSave(strAction);
      if (rc!=1) {
          alertErr2(func.getMsg());
      }
      this.sqlCommit(rc);
      if (strAction.equals("U")){
			    saveDetail();
		  }
      if (rc==1) {
		      wp.colSet("ex_dual_flag", "Y");
      }
    }

	int ofValidation() throws Exception {
		String lsActype, lsAckey, lsRcUse, lsMpFlag;
		String lsDate1, lsDate2, lsMp0Date, lsMp1Date;
		double lmRate, lmAmt;
		
		//Detail Control
		int rowcntaa = 0;
		String[] aaCurrCode = wp.itemBuff("curr_code");
		if (!(aaCurrCode==null) && !empty(aaCurrCode[0])) rowcntaa = aaCurrCode.length;
		wp.listCount[0] = rowcntaa;

		lsActype = wp.itemStr2("acct_type");
    	lsAckey = wp.itemStr2("acct_key");
    	
		func.varsSet("dual_no", lsActype + lsAckey);
		if (empty(lsActype) || empty(lsAckey)) {
			alertErr("帳戶帳號  不可空白");
			return -1;
		}
		if (strAction.equals("D")){
//			if of_excmsg("是否取消 異動資料?")<>1 then Return -1
			return 1;
		}

		//--是否異動--
//		if dw_data.ib_datamodify=false and dw_2.ib_datamodify=false then
		if ((eqIgno(wp.itemStr2("ex_mpflag"),wp.itemStr2("chk_ex_mpflag")) 
				&& wp.itemNum("ex_mp0_rate")==wp.itemNum("chk_ex_mp0_rate")
				&& eqIgno(wp.itemStr2("ex_mp0_s_month"),wp.itemStr2("chk_ex_mp0_s_month"))
				&& eqIgno(wp.itemStr2("ex_mp0_e_month"),wp.itemStr2("chk_ex_mp0_e_month"))
				&& wp.itemNum("ex_mp1_rate")==wp.itemNum("chk_ex_mp1_rate")
				&& eqIgno(wp.itemStr2("ex_mp1_s_month"),wp.itemStr2("chk_ex_mp1_s_month"))
				&& eqIgno(wp.itemStr2("ex_mp1_e_month"),wp.itemStr2("chk_ex_mp1_e_month"))) ) {
			alertErr("資料未異動, 不須存檔");
			return -1;
		}
		
		lsRcUse = wp.itemStr2("ex_rc_use_indicator");
		if (eqIgno(lsRcUse,"3") && wp.itemNum("ex_mp0_rate") != 0) {
			alertErr("不可允用RC, 不准調整");
			return -1;
		}

		//特殊MP百分比調整
		lsMpFlag = wp.itemStr2("ex_mpflag");
		if (eqIgno(lsMpFlag,"1")) {	//-特殊固定MP 1
      /***
			if (wp.item_num("ex_mp1_rate") <= 0) {
				alert_err("特殊固定MP: MP必須大於 0");
				return -1;
			}
      ***/
			lsDate1 = wp.itemStr2("ex_mp1_s_month");
			if (empty(lsDate1)) {
				alertErr("特殊固定MP: 有效期間[起] 不可空白");
				return -1;
			}
			lsDate2 = wp.itemStr2("ex_mp1_e_month");
			if (chkStrend(lsDate1, lsDate2) == false) {
				alertErr("特殊固定MP: 有效期間輸入錯誤");
				return -1;
			}
		} else {	//-帳戶特殊MP百分比調整
			lmRate = wp.itemNum("ex_mp0_rate");
			if (lmRate <= 0) {
				alertErr("帳戶特殊MP: MP必須 > 0%");
				return -1;
			} else if (lmRate > 100) {
				alertErr("帳戶特殊MP: MP必須 <= 100 %");
				return -1;
			}
		/*	if (wp.item_num("ex_min_percent_payment") >= lm_rate
					|| wp.item_num("ex_min_percent_payment2") >= lm_rate) {
//				if messagebox("提示","帳戶特殊MP: 標準MP百分比>=累計金額百分比??",Question!,YesNo!, 2)<>1 then
				alert_err("帳戶特殊MP: 標準MP百分比>=累計金額百分比??");  //todo JAVA確認
				return -1;
			}*/
			lsDate1 = wp.itemStr2("ex_mp0_s_month");
			if (empty(lsDate1)) {
				alertErr("帳戶特殊MP: 有效期間[起] 不可空白");
				return -1;
			}
			lsDate2 = wp.itemStr2("ex_mp0_e_month");
			if (chkStrend(lsDate1, lsDate2) == false) {
				alertErr("帳戶特殊MP: 生效期間輸入錯誤");
				return -1;
			}
		}
		
		//檢核起迄日期--
		//讀取 act_acno 原始資料,以判斷此筆是新增或修改
		String lsSql = "select min_pay_rate_s_month, mp_1_s_month from act_acno "
				+ "where acct_type = :acct_type "
				+ "  and acct_key  = :acct_key ";
		setString("acct_type", lsActype);
		setString("acct_key", lsAckey);
		sqlSelect(lsSql);
		lsMp0Date = sqlStr("min_pay_rate_s_month");
		lsMp1Date = sqlStr("mp_1_s_month");

		//-chk 帳戶特殊MP百分比調整/特殊固定 起迄日期檢核(修改)----------
		if (eqIgno(lsMpFlag,"0")) {
//			if(get_sysDate().substring(0,6).compareTo(ls_date1)<0){
			if (chkStrend(getDBsysDate().substring(0,6), lsDate1) == false) {
//			if (chk_strend(ls_mp0_date, ls_date1) == false) {
				alertErr("帳戶特殊MP[起始年月]不可小於[系統年月]");
				return -1;
			}
			wp.colSet("ex_mp1_rate", 0);
			wp.colSet("ex_mp1_s_month", "");
			wp.colSet("ex_mp1_e_month", "");
		} else {
			if (chkStrend(getDBsysDate().substring(0,6), lsDate1) == false) {
//			if (chk_strend(ls_mp1_date, ls_date1) == false) {
				alertErr("特殊固定MP: [起始年月]不可小於[系統年月]");
				return -1;
			}
			wp.colSet("ex_mp0_rate", 0);
			wp.colSet("ex_mp0_s_month", "");
			wp.colSet("ex_mp0_e_month", "");
		}
		
		if (eqIgno(lsMpFlag,"1")) {
			for (int ii = 0; ii < rowcntaa; ii++) {
				if (empty(aaCurrCode[ii])) continue;
				lsDate1 = wp.itemStr(ii,"mp_1_s_month");
				lsDate2 = wp.itemStr(ii,"mp_1_e_month");
				if (chkStrend(lsDate1, lsDate2) == false) {
					alertErr("幣別: "+aaCurrCode[ii]+" 特殊固定MP [生效期間]輸入錯誤");
					return -1;
				}
				lmAmt = wp.itemNum(ii,"mp_1_amt");
				if (lmAmt < 0) {
					alertErr("幣別: "+aaCurrCode[ii]+" 特殊固定MP 金額 不可小於 0");
					return -1;
				}
			}
		}
		
		return 1;
	}
	
  private String getDBsysDate() throws Exception {
		String lsDbSysDate = "";
		String lsSql = "";
		
		lsSql  = " select to_char(sysdate,'yyyymmdd') as hcol_db_sysDate ";
		lsSql += " from dual ";
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			lsDbSysDate = sqlStr("hcol_db_sysDate");
			return lsDbSysDate;
		} else {
		  return "";
		}
	}
    
	void ofUpdate() throws Exception {
    	String lsLog="";
    System.out.println("of_update: init");
    	// make log_data string
	    /*-act_dual.log_data layout-
		1,    6     min_percent_payment
		7,    1     rc_use_indicator
		8,    3     min_pay_rate
		11,   6     min_pay_rate_s_month
		17,   6     min_pay_rate_e_month
		23,   1     acct_status
		24,   1     ex_mpflag
		25,   14    ex_mp1_amt
		39,   6     ex_mp1_ym_s
		45,   6     ex_mp1_ym_e
		51,   6     ex_min_percent_payment2
		*/

		lsLog  = String.format("%06.2f",wp.itemNum("ex_min_percent_payment"));
		lsLog += fixLength(wp.itemStr2("ex_rc_use_indicator"),1);
		lsLog += String.format("%03d",(long)wp.itemNum("ex_mp0_rate"));
		lsLog += fixLength(wp.itemStr2("ex_mp0_s_month"),6);
		lsLog += fixLength(wp.itemStr2("ex_mp0_e_month"),6);
		lsLog += fixLength(wp.itemStr2("ex_acct_status"),1);
		lsLog += fixLength(wp.itemStr2("ex_mpflag"),1);
	//ls_log += String.format("%014.3f",wp.item_num("ex_mp1_rate"));
		lsLog += String.format("%014.2f",wp.itemNum("ex_mp1_rate"));
		lsLog += fixLength(wp.itemStr2("ex_mp1_s_month"),6);
		lsLog += fixLength(wp.itemStr2("ex_mp1_e_month"),6);
		lsLog += String.format("%06.2f",wp.itemNum("ex_min_percent_payment2"));
		
    System.out.println("of_update: ["+lsLog+"]");
    func.varsSet("log_data", lsLog);
    }
	
	void saveDetail() throws Exception {
		int llOk = 0, llErr = 0;
		String lsKey="", lsLog="";
		
		if (eqIgno(wp.itemStr2("ex_mpflag"),"1")==false) return;
		
		//Detail Control
		int rowcntaa = 0;
		String[] aaCurrCode = wp.itemBuff("curr_code");
		if (!(aaCurrCode==null) && !empty(aaCurrCode[0])) rowcntaa = aaCurrCode.length;
		
		//-act_acct_curr-
		//1,	15		mp1_amt
		//16,	6		mp1_s_month
		//22,	6		mp1_e_month
		for (int ii = 0; ii < rowcntaa; ii++) {
			lsKey  = wp.itemStr2("acct_type")+wp.itemStr2("acct_key")+aaCurrCode[ii];
 		  lsLog  = String.format("%06.2f",wp.itemNum("ex_min_percent_payment"));
		  lsLog += fixLength(wp.itemStr2("ex_rc_use_indicator"),1);
		  lsLog += String.format("%03d",(long)wp.itemNum("ex_mp0_rate"));
		  lsLog += fixLength(wp.itemStr2("ex_mp0_s_month"),6);
	  	lsLog += fixLength(wp.itemStr2("ex_mp0_e_month"),6);
		  lsLog += fixLength(wp.itemStr2("ex_acct_status"),1);
	 	  lsLog += fixLength(wp.itemStr2("ex_mpflag"),1);
		//ls_log  = String.format("%015.2f",wp.item_num(ii,"mp_1_amt"));
			lsLog += String.format("%014.2f",wp.itemNum(ii,"mp_1_amt"));
			lsLog += fixLength(wp.itemStr(ii,"mp_1_s_month"),6);
			lsLog += fixLength(wp.itemStr(ii,"mp_1_e_month"),6);
		  lsLog += String.format("%06.2f",wp.itemNum("ex_min_percent_payment2"));
			func.varsSet("dual_no", lsKey);
			func.varsSet("log_data", lsLog);

			if (func.insertDetailFunc() == 1) {
				llOk++;
			}
			else {
				llErr++;
				rc = -1;
			}
		}
		sqlCommit(llErr > 0 ? 0 : 1);
	}

    @Override
    public void initButton() {
    //if (wp.respHtml.indexOf("_detl") > 0) {
    //    this.btnMode_aud();
    //}
    	String sKey = "1st-page";
      if (wp.respHtml.equals("actm2060"))  {
         wp.colSet("btnUpdate_disable","");
         wp.colSet("btnDelete_disable","");
         this.btnModeAud(sKey);
      }
   
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
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}


}

