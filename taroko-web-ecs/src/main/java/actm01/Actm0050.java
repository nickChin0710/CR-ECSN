/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-05  V1.00.00  OrisChang  program initial                            *
* 107-08-23  V1.00.01  Alex       deleteFunc , ok_flag                       *
* 107-08-24  V1.00.02  Alex       amt fixed                                  *
* 108-02-22  V1.00.03  Alex       輸入卡號帶出帳戶帳號								       *
* 109-04-15  V1.00.04  Alex       add auth_query									           *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 111-11-17  V1.00.04  Simon      codes changes comply with 20200107 modified ajax practice *
******************************************************************************/

package actm01;

import java.util.ArrayList;

import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.base.CommDate;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import java.util.*;

public class Actm0050 extends BaseEdit {
	CommString commString = new CommString();
	CommDate commDate = new CommDate();
	String pPSeqno = "", hChangeFlag = "";
  HashMap<String,Double>  acagHash = new  HashMap<String,Double>();
	int hRowidCnt = 0;
	String kpAcctType = "", kpAcctKey = "", kpCardNo = "";

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

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
			// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
//		updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "AJAX")) {
			/* 20200107 modify AJAX */
			strAction = "AJAX";
			processAjaxOption(wr);
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

		dddwSelect();
		dddwCurrCodeM();
		initButton();
	}
	
	@Override
	public void initPage() {
		wp.colSet("btnUpdate_disable", "disabled");
		wp.colSet("btnDelete_disable", "disabled");
	}
	
	@Override
	public void queryFunc() throws Exception {
		String lsAcctKey = "";
		if(empty(wp.itemStr2("ex_acct_key")) && empty(wp.itemStr2("ex_card_no"))) {
			alertErr2("帳號, 卡號不可均為空白");
			return;
		}

		ColFunc func =new ColFunc();
		func.setConn(wp);
		
		if(wp.itemEmpty("ex_acct_key")==false){
			lsAcctKey = "";
			lsAcctKey = commString.acctKey(wp.itemStr2("ex_acct_key"));
			if(lsAcctKey.length()!=11){
				alertErr2("帳戶帳號:輸入錯誤");
				return ;
			}
			
			if (func.fAuthQuery(wp.modPgm(), commString.mid(lsAcctKey, 0,10))!=1) { 
	      	alertErr2(func.getMsg()); 
	      	return ; 
	      }
			
		}	else if(wp.itemEmpty("ex_card_no")==false)	{
			if (func.fAuthQuery(wp.modPgm(), wp.itemStr2("ex_card_no"))!=1) { 
	      	alertErr2(func.getMsg()); 
	      	return ; 
	      }
		}
		
		// 設定queryRead() SQL條件
		String lsPSeqno = getInitParm();

		if (lsPSeqno.equals("")) {
			alertErr2("無此帳號/卡號");
			return;
		}

		if (!lsPSeqno.equals("")) {
			getDtlData(lsPSeqno);
		}

		pPSeqno = lsPSeqno;

		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
    wp.pageRows = 9999;
		Object[] param = null;
		String lsSql = "";
		String qryTbl = "";
		
		lsSql += " select hex(rowid) as rowid, acct_data ";
//	ls_sql += " select rownum as row_id, acct_data ";
		lsSql += " from ACT_MODDATA_TMP ";
		lsSql += " where p_seqno = ? and act_modtype='01'";
		param = new Object[] { pPSeqno };

		sqlSelect(lsSql, param);
		if (empty(sqlStr("acct_data"))) {
			qryTbl = "ACT_ACAG_CURR";
			wp.pageControl();

			wp.selectSQL =  "hex(rowid) as rowid,ACCT_MONTH,CURR_CODE as ex_curr_code,DC_PAY_AMT,PAY_AMT, ";
			wp.selectSQL += "DC_PAY_AMT as DC_PAY_AMT2,PAY_AMT as PAY_AMT2,'N' as appr_yn, 'U' as status_ua  ";
			wp.daoTable = "ACT_ACAG_CURR";
			wp.whereStr = " where p_seqno = :p_seqno";
			this.setString("p_seqno", pPSeqno);
			wp.whereOrder = "order by ACCT_MONTH";

			pageQuery();
			if (sqlNotFind()) {
				alertErr(appMsg.errCondNodata);
				return;
			}
		} else {
			qryTbl = "ACT_MODDATA_TMP";
			wp.pageControl();

			wp.selectSQL  = " hex(rowid) as rowid, acct_data, ";
			wp.selectSQL += " substr(acct_data,4,6) as data_acct_month ";
			wp.daoTable = "ACT_MODDATA_TMP";
			wp.whereStr = " where p_seqno = :p_seqno and act_modtype='01'";
			this.setString("p_seqno", pPSeqno);
			wp.whereOrder = "order by data_acct_month";

			pageQuery();

		}

    wp.colSet("qry_table", qryTbl);
		wp.setListCount(1);

	//ddd("A1:"+wp.selectCnt);
	//ddd("A2:"+wp.listCount[0]);
	//wp.totalRows = wp.dataCnt; //pageRows設定999時，wp.datacnt=0
	//wp.listCount[1] = wp.dataCnt;
    wp.colSet("readrow", wp.listCount[0]);
	//ddd("A3:"+wp.col_ss("readrow"));
  //wp.col_set("readrow", Integer.toString(wp.listCount[0]));
	//ddd("A4:"+wp.col_ss("readrow"));
	//wp.col_set("ft_cnt", Integer.toString(wp.dataCnt));沒有 ft_cnt 這個欄位
		wp.setPageValue();
		if (qryTbl == "ACT_MODDATA_TMP")
			ProcModDataTmp();
		
		if (qryTbl == "ACT_ACAG_CURR")
			ProcSumMP();
		
	//wp.col_set("btnUpdate_disable", "");
    queryReadAfter();
		wfAcagHashSet();
		wfGetMcode();

	}

  void queryReadAfter() throws Exception {
   		Double lsDouble = 0D;
   	
    	for (int ii = 0; ii < wp.selectCnt; ii++) {
    		  //小數兩位
		 	    wp.colSet(ii, "sv_dc_pay_amt", wp.colStr(ii,"dc_pay_amt"));
	    	  lsDouble = commString.strToNum(wp.colStr(ii,"dc_pay_amt"));
    		  wp.colSet(ii,"dc_pay_amt", commString.numFormat(lsDouble,"##0.00"));
	    	  lsDouble = commString.strToNum(wp.colStr(ii,"pay_amt"));
        //wp.col_set(ii,"pay_amt", zzStr.num_format(ls_double,"#,##0.00"));
        	wp.colSet(ii,"pay_amt", commString.numFormat(lsDouble,"#,##0"));
	    	  lsDouble = commString.strToNum(wp.colStr(ii,"pay_amt2"));
        //wp.col_set(ii,"pay_amt2", zzStr.num_format(ls_double,"#,##0.00"));
        	wp.colSet(ii,"pay_amt2", commString.numFormat(lsDouble,"#,##0"));
        	wp.colSet(ii,"opt_disabled", "disabled");
		 	    wp.colSet(ii, "sv_curr_code", wp.colStr(ii,"ex_curr_code"));
        	wp.colSet(ii,"curr_code_disabled", "disabled");
		 	    wp.colSet(ii, "sv_acct_month", wp.colStr(ii,"acct_month"));
        	wp.colSet(ii,"acct_month_disabled", "disabled");
    		}
    		
    }

  void wfAcagHashSet() throws Exception {
   	//double ls_pay_amt = 0, tw_amt_sub = 0;
   	  double lsPayAmt = 0;
      String lsKey  = "";
   	
    	for (int ii = 0; ii < wp.selectCnt; ii++) {
         lsKey  = wp.colStr(ii,"acct_month");
	    	 lsPayAmt = commString.strToNum(wp.colStr(ii,"pay_amt"));
         Double twAmtSub = (Double)acagHash.get(lsKey);
         if ( twAmtSub == null )
            { acagHash.put(lsKey,lsPayAmt); } 
         else
            { twAmtSub +=  lsPayAmt;
            	acagHash.put(lsKey,twAmtSub); } 
       }
  }

	void wfGetMcode() throws Exception {
	//String ls_cycl_ym = "" , ls_acct_ym="" , ldt_val1 ="" , ldt_val2 ="" , tt_mcode="" ;
		String lsCyclYm = "" , ldtVal1 ="" , ldtVal2 ="" , ttMcode="" ;
		double ldcMpamt = 0 , lmAmt = 0 ;
		int liMcode = 0 ;
		lsCyclYm = wp.colStr("this_acct_month");
		if(empty(lsCyclYm)){
			alertErr2("未取得關帳週期年月");
			return ;
		}
		
		String sql1 = " select "
						+ " mix_mp_balance "
						+ " from ptr_actgeneral "
						+ " where 1=1 "
						+commSqlStr.rownum(1)
						;
		
		sqlSelect(sql1);
		
		if(sqlRowNum<=0){
			ldcMpamt = 0 ;
		}	else	{
			ldcMpamt = sqlNum("mix_mp_balance");
		}
		
		String lsMinAcctYymm = "" ;

    for ( Map.Entry m : acagHash.entrySet() )
    {
			String lsAcctYm = (String)m.getKey();
			if(lsAcctYm.compareTo(lsCyclYm)>=0)	continue;
        	
	 		lmAmt = (Double)acagHash.get(lsAcctYm);
			if(lmAmt <= ldcMpamt)	continue;
        	
			if(lmAmt > ldcMpamt) {
	  	  if(empty(lsMinAcctYymm)) {
	    	  lsMinAcctYymm = lsAcctYm;
	  		}	
	  		else if(commString.strToNum(lsAcctYm) < commString.strToNum(lsMinAcctYymm)) {
	    	  lsMinAcctYymm = lsAcctYm;
	  		}	
			}	
    }

		if(empty(lsMinAcctYymm))	liMcode=0;
		else	{
			ldtVal1 = lsCyclYm+"01";
			ldtVal2 = lsMinAcctYymm+"01";
			liMcode = commDate.monthsBetween(ldtVal1,ldtVal2);
		}
		
	//tt_mcode = String.format("%02d", li_mcode);
		ttMcode = String.format("%3d", liMcode);
		wp.colSet("q_mcode", ttMcode);
		
	}
	
	private String getInitParm() throws Exception {
//		Object[] param = null;
		String lsSql = "";
		
		//from phopho
		lsSql  = " select acno_flag,acct_type, acct_key, p_seqno, uf_acno_name(p_seqno) as acno_cname ";
		lsSql += " from act_acno ";
		lsSql += " where 1=1 ";
		lsSql += " and acno_p_seqno = p_seqno ";
		
		if (empty(wp.itemStr2("ex_acct_key")) == false) {
			lsSql += "and acct_type = :acct_type and acct_key = :acct_key ";
			setString("acct_type", wp.itemStr2("ex_acct_type"));
			String acctkey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
			setString("acct_key",  acctkey);
		} else {
			lsSql += "and p_seqno in (select p_seqno from crd_card where card_no = :card_no) ";
			setString("card_no", wp.itemStr2("ex_card_no"));
		}
		
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("ex_acct_type", sqlStr("acct_type"));
			wp.colSet("ex_acct_key", sqlStr("acct_key"));
			wp.colSet("h_acct_type", sqlStr("acct_type"));
			wp.colSet("h_acct_key", sqlStr("acct_key"));
			wp.colSet("q_p_seqno", sqlStr("p_seqno"));
		//wp.col_set("q_id_cname", sql_ss("acno_cname"));
		//wp.col_set("q_corp_cname", sql_ss("acno_cname"));
			if ( sqlStr("acno_flag").equals("2"))  {
			   wp.colSet("q_corp_cname", sqlStr("acno_cname"));	
		     wp.colSet("q_id_cname", "");
			} else  {
		     wp.colSet("q_id_cname", sqlStr("acno_cname"));
			   wp.colSet("q_corp_cname", "");	
			}
			return sqlStr("p_seqno");
		}
		return "";

	}

	private void getDtlData(String pSeqno) throws Exception {
		String sYyymm = "";
		Object[] param = null;
		String lsSql = "";
		lsSql += " SELECT a.acct_status,   a.stmt_cycle, b.this_acct_month " + " FROM act_acno a, ptr_workday b "
				+ " WHERE b.stmt_cycle = a.stmt_cycle " + " and a.acno_p_seqno = ? " + "";
		param = new Object[] { pSeqno };
		sqlSelect(lsSql, param);

		if (empty(sqlStr("acct_status"))) {
			wp.colSet("q_acct_status", "");
			wp.colSet("q_stmt_cycle", "");
			wp.colSet("q_this_acct_month", "");
			wp.colSet("q_p_seqno", pSeqno);
		} else {
			String sStatus = "";
			if (Integer.parseInt(sqlStr("acct_status")) == 1)
				sStatus = "1-正常";
			if (Integer.parseInt(sqlStr("acct_status")) == 2)
				sStatus = "2-逾放";
			if (Integer.parseInt(sqlStr("acct_status")) == 3)
				sStatus = "3-催收";
			if (Integer.parseInt(sqlStr("acct_status")) == 4)
				sStatus = "4-呆帳";
			wp.colSet("q_acct_status", sStatus);
			wp.colSet("q_stmt_cycle", sqlStr("stmt_cycle"));
			wp.colSet("this_acct_month", sYyymm);
			sYyymm = sqlStr("this_acct_month");
			wp.colSet("this_acct_month", sYyymm);
      /***
			if(!s_yyymm.trim().equals("")) {
				s_yyymm = String.valueOf(Integer.parseInt(s_yyymm) - 191100);
				s_yyymm = s_yyymm.substring(0, 3) + "/" + s_yyymm.substring(3);
			}
      ***/
			if(!sYyymm.trim().equals("")) {
				sYyymm = sYyymm.substring(0, 4) + "/" + sYyymm.substring(4);
			}
			wp.colSet("q_this_acct_month", sYyymm);
			wp.colSet("q_p_seqno", pSeqno);
		}
		
	//wp.col_set("q_mcode", getMcode(p_seqno,sql_ss("stmt_cycle")));

	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		// ProcModDataTmp();
	}



	@Override
	public void saveFunc() throws Exception {

		kpAcctType = wp.colStr("ex_acct_type");
		kpAcctKey  = wp.colStr("ex_acct_key");
		kpCardNo   = wp.colStr("ex_card_no");
	//h_rowid_cnt = wp.item_rows("rowid");
		//ddd("B:"+wp.col_ss("readrow"));
		//ddd("C:"+wp.col_num("readrow"));
	//h_rowid_cnt = (int)wp.item_num("readrow");
	  hRowidCnt = (int)wp.colNum("readrow");
		//ddd("D:"+h_rowid_cnt);
		//ddd("E:"+wp.item_rows("rowid"));
	
    for (int ii = 0; ii <hRowidCnt; ii++) {
       	wp.colSet(ii,"opt_disabled", "disabled");
		    wp.colSet(ii, "ex_curr_code", wp.colStr(ii,"sv_curr_code"));
       	wp.colSet(ii,"curr_code_disabled", "disabled");
		    wp.colSet(ii, "acct_month", wp.colStr(ii,"sv_acct_month"));
       	wp.colSet(ii,"acct_month_disabled", "disabled");
    }

		int rowcntaa = 0;
		rowcntaa = wp.itemRows("ser_num");
		wp.listCount[0] = rowcntaa;

    String[] aaAcctMonth = {""};
		aaAcctMonth = itemBuffMerge(rowcntaa, "sv_acct_month", "acct_month");
		
    String[] aaCurrCode = {""};
		aaCurrCode = itemBuffMerge(rowcntaa, "sv_curr_code", "ex_curr_code");
		
		if (empty(wp.itemStr2("q_p_seqno"))) {
		   alertErr2("請先查詢 再異動存檔!");
			 return;
		}

		if( this.isDelete() && !wp.itemStr2("qry_table").equals("ACT_MODDATA_TMP") ) {
		   alertErr2("無異動資料可刪除!");
			 return;
		}
			
    hChangeFlag = "N";
		if(wfValidation(aaAcctMonth,aaCurrCode)!=1)	{
		  return ;
    }

		if( this.isUpdate() && !hChangeFlag.equals("Y") ) {
		   alertErr2("未異動資料不可存檔!");
			 return;
		}
			
		Actm0050Func func = new Actm0050Func(wp);
		int llOk = 0, llErr = 0;
		String acctData = "";
		
	  String[] aaRowid = wp.itemBuff("rowid");
		String[] opt = wp.itemBuff("opt");
	  String[] aaSerNum = wp.itemBuff("ser_num");  
	//String[] aa_acct_month = wp.item_buff("acct_month");  
	//String[] aa_curr_code = wp.item_buff("ex_curr_code");  
		String[] aaDcPayAmt = wp.itemBuff("dc_pay_amt");  
		String[] aaSvDcPayAmt = wp.itemBuff("sv_dc_pay_amt");  
		String[] aaPayAmt = wp.itemBuff("pay_amt");
		String[] aaDcPayAmt2 = wp.itemBuff("dc_pay_amt2");  
		String[] aaPayAmt2 = wp.itemBuff("pay_amt2");  
		String[] aaStatusUa = wp.itemBuff("status_ua");  

		String colActModtype = "01";
		String colPSeqno = wp.itemStr2("q_p_seqno");
		String colAcctType = wp.itemStr2("h_acct_type");
		String colAcctKey = wp.itemStr2("h_acct_key");


		if(this.isDelete()){
			//-delete detail-
			if (func.dbDelete() < 0) {
				alertErr(func.getMsg());
	      sqlCommit(0);
				return;
			}
      clearFunc();
		  wp.itemSet("ex_acct_type",kpAcctType);
		  wp.itemSet("ex_acct_key",kpAcctKey);
		  wp.itemSet("ex_card_no",kpCardNo);
      deleteRetrieve();
      wp.colSet("q_p_seqno","");
			return;
		}
			
		if(this.isUpdate()){
			//-delete detail-
			if (func.dbDelete() < 0) {
				  alertErr(func.getMsg());
	        sqlCommit(0);
				  return;
			}
			
   	//double ls_pay_amt = 0, tw_amt_sub = 0;
   		double lsPayAmt = 0, lsDouble = 0, sumMp = 0, lsDouble1 = 0, lsDouble2 = 0, lsDouble3 = 0;
      String lsKey  = "", lsSql = "";
      int liInt  = 0;
		  double liExchangeRate = 0;
      long liLong  = 0;
   	
			log("Listcnt:"+wp.listCount[0]);
			
			for (int ll = 0; ll < wp.listCount[0]; ll++) {
				//-option-ON-
				if (checkBoxOptOn(ll, opt)) {
					continue;
				}
				
				aaAcctMonth[ll] = empty(aaAcctMonth[ll])? "199901":aaAcctMonth[ll];
				aaCurrCode[ll] = empty(aaCurrCode[ll])? "901":aaCurrCode[ll];
				aaDcPayAmt[ll] = empty(aaDcPayAmt[ll])? "0":aaDcPayAmt[ll];
			  aaPayAmt[ll]  = empty(aaPayAmt[ll])? "0":aaPayAmt[ll];
				aaDcPayAmt2[ll] = empty(aaDcPayAmt2[ll])? "0":aaDcPayAmt2[ll];
				aaPayAmt2[ll] = empty(aaPayAmt2[ll])? "0":aaPayAmt2[ll];


        lsDouble1 = commString.strToNum(aaDcPayAmt[ll]);
      //ls_double2 = zzstr.ss_2Num(aa_sv_dc_pay_amt[ll]);
	      if ( ll  < hRowidCnt ) { 
          lsDouble2 = commString.strToNum(aaSvDcPayAmt[ll]);
		    }

        lsDouble3 = commString.strToNum(aaPayAmt[ll]);
		    //ddd("A:"+ls_double1);
		    //ddd("B:"+ls_double2);
	      if ( ( ll  < hRowidCnt && lsDouble1 != lsDouble2 ) || 
	           ( ll >= hRowidCnt && lsDouble3 == 0 ) ) { 
	        lsSql = "select exchange_rate from ptr_curr_rate where curr_code = :curr_code";
	        setString("curr_code", aaCurrCode[ll]);
		      sqlSelect(lsSql);
		      if (sqlRowNum > 0) {
		         try {
			         liExchangeRate = Double.parseDouble(sqlStr("exchange_rate"));
		         } catch (Exception ex) {
		           liExchangeRate = 1.0;
		         }
		      }
          liLong   = (long) Math.round(lsDouble1 * liExchangeRate);

		      //ddd("C:"+li_long);
          aaPayAmt[ll] = "" + liLong;
		      //ddd("D:"+aa_pay_amt[ll]);
    		  wp.colSet(ll,"pay_amt", commString.numFormat(liLong,"#,##0"));

		    }

	    	lsDouble = commString.strToNum(aaDcPayAmt[ll]);
    		wp.colSet(ll,"dc_pay_amt", commString.numFormat(lsDouble,"##0.00"));

		    sumMp += commString.strToNum(aaPayAmt[ll]);

        lsKey  = aaAcctMonth[ll];
	    	lsPayAmt = commString.strToNum(aaPayAmt[ll]);
        Double twAmtSub = (Double)acagHash.get(lsKey);
        if ( twAmtSub == null )
           { acagHash.put(lsKey,lsPayAmt); } 
        else
           { twAmtSub +=  lsPayAmt;
           	 acagHash.put(lsKey,twAmtSub); } 

				acctData = "";
			//acct_data += aa_ser_num[ll] + "@";改成以下只存放兩碼
			  liInt = (int) commString.strToNum(aaSerNum[ll]);
			  acctData += commString.numFormat(liInt, "00") + "@";

				acctData += aaAcctMonth[ll] + "@";
				acctData += aaCurrCode[ll] + "@";
				acctData += aaDcPayAmt[ll] + "@";
				acctData += aaPayAmt[ll] + "@";
				acctData += aaDcPayAmt2[ll] + "@";
				acctData += aaPayAmt2[ll] + "@"; 
				acctData += aaStatusUa[ll] ;
				
				func.varsSet("act_modtype", colActModtype);
				func.varsSet("p_seqno", colPSeqno);
				func.varsSet("curr_code", aaCurrCode[ll]);
				func.varsSet("acct_type", colAcctType);
			//func.vars_set("acct_key", col_acct_key);
				func.varsSet("acct_data", acctData);
				if (func.dbInsert() == 1) {
					llOk++;
					wp.colSet(ll,"ok_flag", "V");
					sqlCommit(1);
					continue;
				} else {
					llErr++;
					wp.colSet(ll,"ok_flag", "X");
					dbRollback();
					continue;
				}
			}
			
		  wp.colSet("q_mp", numToStr(sumMp, "###,###,###,##0"));
		  wfGetMcode();
      wp.colSet("q_p_seqno","");
			
			alertMsg("存檔處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr);			
		}	
		
	}


  public void deleteRetrieve() throws Exception {

		String lsPSeqno = getInitParm();

		if (!lsPSeqno.equals("")) {
			getDtlData(lsPSeqno);
		}

		pPSeqno = lsPSeqno;

		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
  }

  public String[] itemBuffMerge(int serCnt, String fieldName1, String fieldName2) throws Exception {
	  String[] inputFieldData1 = {""};
	  String[] inputFieldData2 = {""};
	//String[] inputFieldData_a = {""};
   	int dataCnt = 0;

    String[] inputFieldDataA = new String[serCnt];//借用 "ser_num" 欄位個數設定陣列個數

    //ddd("actm0050_inputFieldData_a: " + inputFieldData_a);看不到實際內容，要用以下方式顯示每個陣列元素才看得到
    /*** 
 		for (int i = 0; i < inputFieldData_a.length; i++) {
      ddd("actm0050_inputFieldData_a-i: " + i);
      ddd("actm0050_inputFieldData_a[i]: " + inputFieldData_a[i]);
		}
    ***/ 
     
		inputFieldData1 = (String[]) wp.inputHash.get(fieldName1.trim().toUpperCase());
     
    /*** 
		for (int i = 0; i < inputFieldData_1.length; i++) {
      ddd("actm0050_inputFieldData_1-i: " + i);
      ddd("actm0050_inputFieldData_1[i]: " + inputFieldData_1[i]);
		}
    ***/ 
     
	  if (inputFieldData1 != null) {
		  for (int i = 0; i < inputFieldData1.length; i++) {
			  if (inputFieldData1[i] == null) { 
			  //inputFieldData[i] = "";
				  continue;
			  }
			  if (i >= hRowidCnt) { //只取讀取出來的record 的 sv_data 
				  continue;
			  }
			  inputFieldDataA[dataCnt] = inputFieldData1[i].trim();
        dataCnt++;
		  }
		}

		inputFieldData2 = (String[]) wp.inputHash.get(fieldName2.trim().toUpperCase());
    /*** 
		for (int i = 0; i < inputFieldData_2.length; i++) {
      ddd("actm0050_inputFieldData_2-i: " + i);
      ddd("actm0050_inputFieldData_2[i]: " + inputFieldData_2[i]);
		}
    ***/ 

	  if (inputFieldData2 != null) {
		  for (int i = 0; i < inputFieldData2.length; i++) {
			  if (inputFieldData2[i] == null) {
			  //inputFieldData[i] = "";
				  continue;
			  }
			  inputFieldDataA[dataCnt] = inputFieldData2[i].trim();
		  //wp.col_set(data_cnt, "ex_curr_code", inputFieldData_a[data_cnt]);
		    wp.colSet(dataCnt, fieldName2, inputFieldDataA[dataCnt]);
        dataCnt++;
		  }
		}

    /*** 
		for (int i = 0; i < inputFieldData_a.length; i++) {
      ddd("actm0050_inputFieldData_a-i: " + i);
      ddd("actm0050_inputFieldData_a[i]: " + inputFieldData_a[i]);
		}
    ***/ 

	  return inputFieldDataA;
  }

	public int wfValidation(String[] aaAcctMonth,String[] aaCurrCode) throws Exception { 

		int rowidCnt = 0, sernumCnt = 0, llErr = 0;
		double lsDouble = 0, lsDouble1 = 0, lsDouble2 = 0;
    String lsSql = "", lsActcurrFlag = "";
		
		String[] opt = wp.itemBuff("opt");
		String[] aaSerNum = wp.itemBuff("ser_num");  
		String[] aaDcPayAmt = wp.itemBuff("dc_pay_amt");  
		String[] aaSvDcPayAmt = wp.itemBuff("sv_dc_pay_amt");  
	//String[] aa_acct_month = wp.item_buff("acct_month");
	//String[] ls_acct_month = aa_acct_month;
	//String[] ls_curr_code = aa_curr_code;
		sernumCnt = wp.itemRows("ser_num");
	//rowid_cnt = wp.item_rows("rowid");
		rowidCnt = (int)wp.colNum("readrow");
		
		//ddd("actm0050_A,String[] opt: "+ opt);
		//ddd("actm0050_B,opt[0]: "+ opt[0]);
		//ddd("actm0050_C,opt[1]: "+ opt[1]); checkbox disabled 時，opt[i] 會是 null,會發生ArrayIndexOutOfBoundsException 
		//ddd("actm0050_D,opt[2]: "+ opt[2]); 

		for (int ll = 0; ll < sernumCnt; ll++) {
			if (ll < rowidCnt) {
				 lsDouble1 = commString.strToNum(aaDcPayAmt[ll]);
				 lsDouble2 = commString.strToNum(aaSvDcPayAmt[ll]);
			  if (lsDouble1 != lsDouble2) {
				   hChangeFlag = "Y";
			  }
			}
				
			if (ll < rowidCnt) {
				 continue;
			}
				
			//-option-ON-此畫面勾選表示不處理
			if (checkBoxOptOn(ll, opt)) {
				 continue;
			}
				
			if (empty(aaAcctMonth[ll])) {
				 wp.colSet(ll,"ok_flag", "X");
				 llErr++;
		     alertErr2("新增帳務月份 不可空白!");
				 continue;
			}

	    lsSql = "select curr_code from act_acct_curr where p_seqno = :p_seqno and curr_code = :curr_code";
	    setString("p_seqno", wp.colStr("q_p_seqno"));
	    setString("curr_code", aaCurrCode[ll]);
		  sqlSelect(lsSql);
		  if (sqlRowNum > 0) {
		     lsActcurrFlag = "Y";
		  } else {
		     lsActcurrFlag = "N";
		  }

			if (!lsActcurrFlag.equals("Y")) {
				 wp.colSet(ll,"ok_flag", "X");
				 llErr++;
		     alertErr2("雙幣幣別輸入錯誤！");
				 continue;
			}

			if (checkDup(ll, aaAcctMonth, aaCurrCode)==true) {
				 wp.colSet(ll,"ok_flag", "X");
				 llErr++;
		     alertErr2("新增帳務月份+雙幣幣別 不可重複！");
				 continue;
			}

	    lsDouble = commString.strToNum(aaDcPayAmt[ll]);
			if (lsDouble <= 0) {
				 wp.colSet(ll,"ok_flag", "X");
				 llErr++;
		     alertErr2("新增當期MP餘額 須大於零 !");
				 continue;
			}

			hChangeFlag = "Y";

		}
    
		if (llErr > 0) 
		{		return -1; }
		else 
		{   return 1;  }
		
	}
	
	public boolean checkDup(int txIi, String[] aaAcctMonth, String[] aaCurrCode) throws Exception { 

		String[] opt = wp.itemBuff("opt");
		String chkAcctMonth = aaAcctMonth[txIi];
		String chkCurrCode = aaCurrCode[txIi];
		
		for (int ll = 0; ll < txIi; ll++) {
			//-option-ON-此畫面勾選表示不處理
			if (checkBoxOptOn(ll, opt)) {
				continue;
			}
				
			if ( chkAcctMonth.equals(aaAcctMonth[ll]) && chkCurrCode.equals(aaCurrCode[ll]) ) {
				return true;
			}
			
		}

		return false;
	}
	
	@Override
	public void initButton() {
	//if (wp.respHtml.indexOf("_detl") > 0) { //沒有 _detl
	//	this.btnMode_aud();
	//}
		String sKey = "1st-page";

    if (wp.respHtml.equals("actm0050"))  {
        wp.colSet("btnUpdate_disable","");
        wp.colSet("btnDelete_disable","");
        this.btnModeAud(sKey);
    }

	}

	@Override
	public void dddwSelect() {
		try {
			//wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

		} catch (Exception ex) {
		}
	}

	public void dddwCurrCodeM() {
		try {
    	for (int ii = 0; ii < wp.listCount[0]; ii++) {
          //if (ii < h_rowid_cnt)  {
          //    continue;
          //}
			    wp.initOption = "--";
			    wp.optionKey = wp.colStr(ii,"ex_curr_code");
		      this.dddwList(ii,"dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id desc");
    	}
		} catch (Exception ex) {
		}
	}

	void ProcModDataTmp() throws Exception {
		int rowCt = wp.selectCnt;
		String strTmpData = "";
		ArrayList<String> serNumList = new ArrayList<String>();
		// ArrayList<String> acct_data_List = new ArrayList<String>();
		ArrayList<String> acctMonthList = new ArrayList<String>();
		ArrayList<String> currCodeList = new ArrayList<String>();
		ArrayList<String> dcPayAmtList = new ArrayList<String>();
		ArrayList<String> payAmtList = new ArrayList<String>();
		ArrayList<String> dcPayAmt2List = new ArrayList<String>();
		ArrayList<String> payAmt2List = new ArrayList<String>();
		ArrayList<String> apprYnList = new ArrayList<String>();
		ArrayList<String> statusUaList = new ArrayList<String>();

		Double sumMp = 0D;

		// 將頁面資料用ArrayList先存起來
		for (int ii = 0; ii < rowCt; ii++) {
			strTmpData = wp.colStr(ii, "acct_data");
			String[] tmpCols = strTmpData.equals("") || strTmpData == null ? null : strTmpData.split("@");

			if (tmpCols.length >= 8) {
				serNumList.add(commString.numFormat(ii + 1, "00")); 
				acctMonthList.add(tmpCols[1]);
				currCodeList.add(tmpCols[2]);
				dcPayAmtList.add(tmpCols[3]);
				payAmtList.add(tmpCols[4]);
				dcPayAmt2List.add(tmpCols[5]);
				payAmt2List.add(tmpCols[6]);
				apprYnList.add("Y");
				statusUaList.add(tmpCols[7]);

			//sumMp += Double.parseDouble(tmpCols[4]);
				sumMp += commString.strToNum(tmpCols[4]);

			  wp.colSet(ii, "ser_num", serNumList.get(ii));
			  wp.colSet(ii, "acct_month", acctMonthList.get(ii));
			  wp.colSet(ii, "ex_curr_code", currCodeList.get(ii));
			  wp.colSet(ii, "dc_pay_amt", dcPayAmtList.get(ii));
			  wp.colSet(ii, "pay_amt", payAmtList.get(ii));
			  wp.colSet(ii, "dc_pay_amt2", dcPayAmt2List.get(ii));
			  wp.colSet(ii, "pay_amt2", payAmt2List.get(ii));
			  wp.colSet(ii, "appr_yn", apprYnList.get(ii));
			  wp.colSet(ii, "status_ua", statusUaList.get(ii));

			}

		}
    /*
		for (int ii = 0; ii < row_ct; ii++) {
			wp.col_set(ii, "ser_num", ser_num_List.get(ii));
			wp.col_set(ii, "acct_month", acct_month_List.get(ii));
			wp.col_set(ii, "curr_code", curr_code_List.get(ii));
			wp.col_set(ii, "dc_pay_amt", dc_pay_amt_List.get(ii));
			wp.col_set(ii, "pay_amt", pay_amt_List.get(ii));
			wp.col_set(ii, "pay_amt2", pay_amt2_List.get(ii));
			wp.col_set(ii, "appr_yn", appr_yn_List.get(ii));
			wp.col_set(ii, "status_ua", status_ua_List.get(ii));
		}
    */
//	wp.col_set("q_mp", num_2str(sumMp, "###,###,###,##0.00"));
		wp.colSet("q_mp", numToStr(sumMp, "###,###,###,##0"));

	}
	void ProcSumMP() throws Exception {
		int rowCt = wp.selectCnt;
		String strTmpData = "";

		Double sumMp = 0D;

		// 將頁面資料用ArrayList先存起來
		for (int ii = 0; ii < rowCt; ii++) {
			strTmpData = wp.colStr(ii, "pay_amt");
		//sumMp += Double.parseDouble(strTmpData);
		  sumMp += commString.strToNum(strTmpData);
		}
//		wp.col_set("q_mp", num_2str(sumMp, "###,###,###,##0.00"));
		wp.colSet("q_mp", numToStr(sumMp, "###,###,###,##0"));

	}
	
  /***
	private String getMcode(String p_seqno, String stmt_cycle) throws Exception {
		String ls_sql = "";
		float lf_mp = 0;
		float lf_pay = 0;
		String ls_acc_ym = "";
		String ls_cyc_ym = "";
		int diffMonth = 0;
		
		ls_sql = "select this_acct_month from PTR_WORKDAY where STMT_CYCLE = :stmt_cycle";
		setString("stmt_cycle", stmt_cycle);
		
		sqlSelect(ls_sql);
		if (sql_nrow > 0) {
			ls_cyc_ym = sql_ss("this_acct_month");
		}

		try {
		//get mp
		//select mp from (SELECT ptr_actgeneral.mix_mp_balance as mp, row_number() over() rownum FROM ptr_actgeneral) a WHERE a.rownum < 2
		ls_sql =" select mp from (SELECT ptr_actgeneral.mix_mp_balance as mp, row_number() over() rownum FROM ptr_actgeneral) a WHERE a.rownum < 2";
		
		sqlSelect(ls_sql);
		if (sql_nrow > 0) {
			lf_mp = Float.parseFloat(sql_ss("mp"));
		}
		}catch(Exception ex) {lf_mp = 0;}
		
		//get acag_curr
		//select acct_month, pay_amt from act_acag_curr where P_SEQNO = '0001781236' order by acct_month
		ls_sql = "select acct_month, pay_amt from act_acag_curr where P_SEQNO = :p_seqno order by acct_month";
		setString("p_seqno", p_seqno);
		
		sqlSelect(ls_sql);
		if (sql_nrow > 0) {
			for(int i=0; i<sql_nrow; i++) {
				ls_acc_ym = sql_ss(i,"acct_month");	
				lf_pay = Float.parseFloat(sql_ss(i,"pay_amt"));
				if(Integer.parseInt(ls_acc_ym) > Integer.parseInt(ls_cyc_ym)) break;
				if(lf_pay > 0) {
					lf_mp = lf_mp - lf_pay;
					if(lf_mp < 0) break;
				}else {
					continue;
				}
			}
			
			if(lf_mp>0) return "0";
			
			diffMonth = (Integer.parseInt(ls_cyc_ym.substring(0, 4)) - Integer.parseInt(ls_acc_ym.substring(0, 4))) * 12;
			diffMonth = diffMonth + (Integer.parseInt(ls_cyc_ym.substring(4, 6)) - Integer.parseInt(ls_acc_ym.substring(4, 6)));

		}else {
			return "";
		}
		
		return String.valueOf(diffMonth);
	}
  ***/
	
  public void processAjaxOption(TarokoCommon wr) throws Exception {
    super.wp = wr;
		String lsIdCode = wp.itemStr2("aj_idCode");
  	if (eqIgno(lsIdCode, "1")) {
  		String jsAddSerNum = wp.itemStr2("add_ser_num");
  
  		String lsSql = "Select wf_id, wf_desc "
  				+ "from ptr_sys_idtab "
  				+ "where 1=1 "
  		  	+ "and wf_type = 'DC_CURRENCY' "
  				+ "order by wf_id desc ";
  		sqlSelect(lsSql);
  		String option = "";
  		if (sqlRowNum <= 0) {
  			option += "<option value=''>--</option>";
  		} else {
  		  option += "<option value=''>--</option>";
  			for (int ii = 0; ii < sqlRowNum; ii++) {
  				option += "<option value='" + sqlStr(ii, "wf_id") + " " +"' ${ex_curr_code-" + sqlStr(ii, "wf_id") + "} > " + sqlStr(ii, "wf_id") 
  				        + "_" + sqlStr(ii, "wf_desc") + "</option> ";
  			}
  		}
  		wp.addJSON("add_ser_num2", jsAddSerNum);
  		wp.addJSON("dddw_curr_code2", option);
  	} else if (eqIgno(lsIdCode, "2")) {
  		String jsSerNum = wp.itemStr2("aj_ser_num");
  		String jsCurrCode = wp.itemStr2("aj_curr_code");
  		String jsDcAmt = wp.itemStr2("aj_dc_amt");
  		String jsExchangeRate = "";
  
  	  String lsSql = "select exchange_rate from ptr_curr_rate where curr_code = :curr_code";
  	  setString("curr_code", jsCurrCode);
  		sqlSelect(lsSql);
  		if (sqlRowNum > 0) {
  		   try {
  			 //js_exchange_rate = Double.parseDouble(sql_ss("exchange_rate"));
  			   jsExchangeRate = sqlStr("exchange_rate");
  		   } catch (Exception ex) {
  		     jsExchangeRate = "1.0";
  		   }
  		}
  
  		wp.addJSON("ax_ser_num", jsSerNum);
  		wp.addJSON("ax_exchange_rate", jsExchangeRate);
  		wp.addJSON("ax_dc_amt", jsDcAmt);
  	} else if (eqIgno(lsIdCode, "3")) {
  		String jsSerNum = wp.itemStr2("aj_ser_num");
  		String jsPSeqno = wp.itemStr2("aj_p_seqno");
  		String jsCurrCode = wp.itemStr2("aj_curr_code");
  		String jsActcurrFlag = "N";
  		String jsDcAmt = wp.itemStr2("aj_dc_amt");
  		String jsExchangeRate = "";
  
  	  String lsSql = "select curr_code from act_acct_curr where p_seqno = :p_seqno and curr_code = :curr_code";
  	  setString("p_seqno", jsPSeqno);
  	  setString("curr_code", jsCurrCode);
  		sqlSelect(lsSql);
  		if (sqlRowNum > 0) {
  		    jsActcurrFlag = "Y";
  		}
  
  	  String lsSql2 = "select exchange_rate from ptr_curr_rate where curr_code = :curr_code";
  	  setString("curr_code", jsCurrCode);
  		sqlSelect(lsSql2);
  		if (sqlRowNum > 0) {
  		   try {
  			   jsExchangeRate = sqlStr("exchange_rate");
  		   } catch (Exception ex) {
  		     jsExchangeRate = "1.0";
  		   }
  		}
  
  		wp.addJSON("ax_ser_num", jsSerNum);
  		wp.addJSON("ax_actcurr_flag", jsActcurrFlag);
  		wp.addJSON("ax_exchange_rate", jsExchangeRate);
  		wp.addJSON("ax_dc_amt", jsDcAmt);
		} 

     return;
  }
  
/***
  	public void ajaxSetCurrddw(TarokoCommon wr) throws Exception {
		super.wp = wr;

		String jsAddSerNum = wp.itemStr2("add_ser_num");

		String lsSql = "Select wf_id, wf_desc "
				+ "from ptr_sys_idtab "
				+ "where 1=1 "
		  	+ "and wf_type = 'DC_CURRENCY' "
				+ "order by wf_id desc ";
		sqlSelect(lsSql);
		String option = "";
		if (sqlRowNum <= 0) {
			option += "<option value=''>--</option>";
		} else {
		  option += "<option value=''>--</option>";
			for (int ii = 0; ii < sqlRowNum; ii++) {
				option += "<option value='" + sqlStr(ii, "wf_id") + " " +"' ${ex_curr_code-" + sqlStr(ii, "wf_id") + "} > " + sqlStr(ii, "wf_id") 
				        + "_" + sqlStr(ii, "wf_desc") + "</option> ";
			}
		}
		wp.addJSON("add_ser_num2", jsAddSerNum);
		wp.addJSON("dddw_curr_code2", option);

	}

	public void ajaxAmtExChange(TarokoCommon wr) throws Exception {
		super.wp = wr;

		String jsSerNum = wp.itemStr2("aj_ser_num");
		String jsCurrCode = wp.itemStr2("aj_curr_code");
		String jsDcAmt = wp.itemStr2("aj_dc_amt");
		String jsExchangeRate = "";

	  String lsSql = "select exchange_rate from ptr_curr_rate where curr_code = :curr_code";
	  setString("curr_code", jsCurrCode);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
		   try {
			 //js_exchange_rate = Double.parseDouble(sql_ss("exchange_rate"));
			   jsExchangeRate = sqlStr("exchange_rate");
		   } catch (Exception ex) {
		     jsExchangeRate = "1.0";
		   }
		}

		wp.addJSON("ax_ser_num", jsSerNum);
		wp.addJSON("ax_exchange_rate", jsExchangeRate);
		wp.addJSON("ax_dc_amt", jsDcAmt);
		return;

	}

	public void ajaxChkActcurr(TarokoCommon wr) throws Exception {
		super.wp = wr;

		String jsSerNum = wp.itemStr2("aj_ser_num");
		String jsPSeqno = wp.itemStr2("aj_p_seqno");
		String jsCurrCode = wp.itemStr2("aj_curr_code");
		String jsActcurrFlag = "N";
		String jsDcAmt = wp.itemStr2("aj_dc_amt");
		String jsExchangeRate = "";

	  String lsSql = "select curr_code from act_acct_curr where p_seqno = :p_seqno and curr_code = :curr_code";
	  setString("p_seqno", jsPSeqno);
	  setString("curr_code", jsCurrCode);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
		    jsActcurrFlag = "Y";
		}

	  String lsSql2 = "select exchange_rate from ptr_curr_rate where curr_code = :curr_code";
	  setString("curr_code", jsCurrCode);
		sqlSelect(lsSql2);
		if (sqlRowNum > 0) {
		   try {
			   jsExchangeRate = sqlStr("exchange_rate");
		   } catch (Exception ex) {
		     jsExchangeRate = "1.0";
		   }
		}

		wp.addJSON("ax_ser_num", jsSerNum);
		wp.addJSON("ax_actcurr_flag", jsActcurrFlag);
		wp.addJSON("ax_exchange_rate", jsExchangeRate);
		wp.addJSON("ax_dc_amt", jsDcAmt);
		return;

	}
***/

	String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}
	
}
