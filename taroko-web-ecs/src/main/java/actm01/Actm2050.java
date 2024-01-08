/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 109-04-15  V1.00.01  Alex       add auth_query									           *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 111-11-21  V1.00.04  Simon      cancel showing act_acno.batch_int_rate data*
******************************************************************************/

package actm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;

import java.math.BigDecimal;
import java.util.ArrayList;

import busi.func.ColFunc;

public class Actm2050 extends BaseEdit {
	CommString commString = new CommString();
	CommDate commDate = new CommDate();
	Actm2050Func func;
    String mAccttype="";
    String mAcctkey="";
    
    String isSysYM = "";
    String isEndMm = "";
    
    int breakpoint = 0;
    
    ArrayList<String> ListExYymm = new ArrayList<String>();
	ArrayList<String> ListExOrate = new ArrayList<String>();
	ArrayList<String> ListExRate = new ArrayList<String>();
	ArrayList<String> ListExAdjRate = new ArrayList<String>();
    
    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc=1;
        isSysYM = commString.left(wp.sysDate,6);

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
        } else if (eqIgno(wp.buttonCode, "Rt")) {
			/* 利率調整 */
        	wfSetIntRate();
			wp.colSet("popflag", "Y");
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        }

        dddwSelect();
        initButton();
    }
    
    @Override
	public void initPage() {
    	wp.colSet("pho_update_disable", "disabled style='background-color: lightgray;'");
    	wp.colSet("pho_delete_disable", "disabled style='background-color: lightgray;'");
	}

    @Override
    public void queryFunc() throws Exception {
        wp.setQueryMode();
        queryRead();
    }

	@Override
	public void queryRead() throws Exception {
		
		
		mAccttype = wp.itemStr2("ex_acct_type");
    	mAcctkey  = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
    	
    	if(mAcctkey.length()!=11){
    		alertErr2("帳戶帳號 : 輸入錯誤");
    		return ;
    	}
    	
    	ColFunc func =new ColFunc();
   	func.setConn(wp);      
       
   	if (func.fAuthQuery(wp.modPgm(), commString.mid(mAcctkey, 0,10))!=1) { 
        	alertErr2(func.getMsg()); 
        	return ; 
      }
   	wp.pageControl();
		wp.selectSQL = " hex(a.rowid) as rowid " +
						" ,b.revolving_interest1 " +
						" ,b.revolving_interest2 " +
						" ,b.revolving_interest3 " +
						" ,b.revolving_interest4 " +
						" ,b.revolving_interest5 " +
						" ,b.revolving_interest6 " +
						" ,a.revolve_int_sign " +
						" ,a.revolve_int_rate " +
						" ,a.revolve_rate_s_month " +
						" ,a.revolve_rate_e_month " +
						" ,a.revolve_reason " +
						" ,a.revolve_int_sign_2 " +
						" ,a.revolve_int_rate_2 " +
						" ,a.revolve_rate_s_month_2 " +
						" ,a.revolve_rate_e_month_2 " +
						" ,a.revolve_reason_2 " +
						" ,a.acct_status " +
						" ,'N' as ex_dual_flag " +
						" ,c.this_acct_month " +
						" ,a.pay_by_stage_flag " +
						" ,c.next_acct_month " +
					//" ,a.batch_int_sign " +
					//" ,a.batch_int_rate " +
					//" ,a.batch_rate_s_month " +
					//" ,a.batch_rate_e_month " +
						" ,a.id_p_seqno " +
						" ,a1.chi_name as id_cname " +
						" ,a.corp_p_seqno " +
						" ,a2.chi_name as corp_cname " +
						" ,a.p_seqno " +
						" ,a.acct_type " +
						" ,a.acct_key " +
						" ,a1.id_no " ;
		
		wp.daoTable = " act_acno a "
					+ "left join crd_idno a1 on a1.id_p_seqno = a.id_p_seqno "
					+ "left join crd_corp a2 on a2.corp_p_seqno = a.corp_p_seqno "
					+ ", ptr_actgeneral_n b, ptr_workday c ";
		
		wp.whereStr = "where a.acct_type = :acct_type " +
						" and a.acct_key   = :acct_key " +
						" and a.acct_type  = b.acct_type " +
						" and a.acno_p_seqno = a.p_seqno " +
						" and a.stmt_cycle = c.stmt_cycle " ;
		setString("acct_type", mAccttype);
		setString("acct_key",  mAcctkey);

        pageQuery();
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            wp.colSet("p_seqno", "");
            return;
        }
        
        detlWkdata();
		wp.colSet("pho_update_disable", "");
		
		if (readActDual()) {
    		wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
    	}

    }
	
	void detlWkdata() {
    	
    	wp.colSet("ex_cname", wp.colStr("id_cname"));
    	wp.colSet("ex_corp_cname", wp.colStr("corp_cname"));
    	
    	wp.colSet("ex_revolving_interest1", String.format("%.3f",wp.colNum("revolving_interest1"))+" /10000");
    	wp.colSet("ex_revolving_interest2", String.format("%.3f",wp.colNum("revolving_interest2"))+" /10000");
    	wp.colSet("ex_revolving_interest3", String.format("%.3f",wp.colNum("revolving_interest3"))+" /10000");
    	wp.colSet("ex_revolving_interest4", String.format("%.3f",wp.colNum("revolving_interest4"))+" /10000");
    	wp.colSet("ex_revolving_interest5", String.format("%.3f",wp.colNum("revolving_interest5"))+" /10000");
    	wp.colSet("ex_revolving_interest6", String.format("%.3f",wp.colNum("revolving_interest6"))+" /10000");
    	
    	wp.colSet("ex_revolve_int_sign", wp.colStr("revolve_int_sign"));
    	wp.colSet("ex_revolve_int_rate", wp.colStr("revolve_int_rate"));
    	wp.colSet("ex_revolve_rate_s_month", wp.colStr("revolve_rate_s_month"));
    	wp.colSet("ex_revolve_rate_e_month", wp.colStr("revolve_rate_e_month"));
    	wp.colSet("revolve_reason", wp.colStr("revolve_reason"));
    	wp.colSet("tt_revolve_reason", commString.decode(wp.colStr("revolve_reason"), ",I,J,2,K,L", ",I.頂級卡,J.系統調整,2.人工調整,K.前協,L.個別協商"));
    	wp.colSet("ex_revolve_int_sign2", wp.colStr("revolve_int_sign_2"));
    	wp.colSet("ex_revolve_int_rate2", wp.colStr("revolve_int_rate_2"));
    	wp.colSet("ex_revolve_rate_s_month2", wp.colStr("revolve_rate_s_month_2"));
    	wp.colSet("ex_revolve_rate_e_month2", wp.colStr("revolve_rate_e_month_2"));
    	wp.colSet("revolve_reason_2", wp.colStr("revolve_reason_2"));
    	wp.colSet("tt_revolve_reason2", commString.decode(wp.colStr("revolve_reason_2"), ",I,J,2,K,L", ",I.頂級卡,J.系統調整,2.人工調整,K.前協,L.個別協商"));
    	
    	wp.colSet("ex_acct_status", wp.colStr("acct_status"));
    	wp.colSet("tt_acct_status", commString.decode(wp.colStr("acct_status"), ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));
    	wp.colSet("ex_this_acct_month", wp.colStr("this_acct_month"));
    	wp.colSet("ex_pay_by_stage", wp.colStr("pay_by_stage_flag"));
    	wp.colSet("ex_next_acct_month", wp.colStr("next_acct_month"));
    //wp.colSet("ex_batch_int_sign", wp.colStr("batch_int_sign"));
    //wp.colSet("ex_batch_int_rate", wp.colStr("batch_int_rate"));
    //wp.colSet("ex_batch_rate_s_month", wp.colStr("batch_rate_s_month"));
    //wp.colSet("ex_batch_rate_e_month", wp.colStr("batch_rate_e_month"));
	}
	
	boolean readActDual() throws Exception {
    	String lsDualNo = "";
    	String lsDualData = "";
    	
    	lsDualNo = mAccttype + mAcctkey;
    	String lsSql = "select log_data from act_dual "
					  + "where dual_key = :dual_key "
					  + "  and func_code = '0703' ";
		setString("dual_key", lsDualNo);
		sqlSelect(lsSql);
		if (sqlRowNum == 0) {
			return false;
		}
		
		lsDualData=sqlStr("log_data");
    	setActDual(lsDualData);
    	wp.colSet("pho_delete_disable", "");
    	
    	return true;
    }
    
    void setActDual(String asData) {
    	double ldStd1 = 0, ldStd2 = 0, ldStd3 = 0, ldStd4 = 0, ldStd5 = 0, ldStd6 = 0;
		ldStd1 = commString.strToNum(commString.mid(asData, 0, 7));
    	ldStd2 = commString.strToNum(commString.mid(asData, 7, 7));
    	ldStd3 = commString.strToNum(commString.mid(asData, 14, 7));
    	ldStd4 = commString.strToNum(commString.mid(asData, 21, 7));
    	ldStd5 = commString.strToNum(commString.mid(asData, 28, 7));
    	ldStd6 = commString.strToNum(commString.mid(asData, 35, 7));
    	wp.colSet("ex_revolving_interest1", String.format("%.3f",ldStd1)+" /10000");
    	wp.colSet("ex_revolving_interest2", String.format("%.3f",ldStd2)+" /10000");
    	wp.colSet("ex_revolving_interest3", String.format("%.3f",ldStd3)+" /10000");
    	wp.colSet("ex_revolving_interest4", String.format("%.3f",ldStd4)+" /10000");
    	wp.colSet("ex_revolving_interest5", String.format("%.3f",ldStd5)+" /10000");
    	wp.colSet("ex_revolving_interest6", String.format("%.3f",ldStd6)+" /10000");

    	String lsSign = "", lsSMonth = "", lsEMonth = "", lsIntReason = "";
    	String lsSign2 = "", lsSMonth2 = "", lsEMonth2 = "", lsIntReason2 = "", lsAcctStatus = "";
    	double ldcIntRate = 0, ldcIntRate2 = 0;
    	//-利率調整[一]
    	lsSign       = commString.mid(asData, 42, 1);
    	ldcIntRate  = commString.strToNum(commString.mid(asData, 43, 6));
    	lsSMonth    = commString.mid(asData, 49, 6);
    	lsEMonth    = commString.mid(asData, 55, 6);
    	lsIntReason = commString.mid(asData, 61, 1);
    	//-利率調整[二]
    	lsSign2       = commString.mid(asData, 62, 1);
    	ldcIntRate2  = commString.strToNum(commString.mid(asData, 63, 6));
    	lsSMonth2    = commString.mid(asData, 69, 6);
    	lsEMonth2    = commString.mid(asData, 75, 6);
    	lsIntReason2 = commString.mid(asData, 81, 1);
    	lsAcctStatus = commString.mid(asData, 82, 1);

    	wp.colSet("revolving_interest1", ldStd1);
    	wp.colSet("revolving_interest2", ldStd2);
    	wp.colSet("revolving_interest3", ldStd3);
    	wp.colSet("revolving_interest4", ldStd4);
    	wp.colSet("revolving_interest5", ldStd5);
    	wp.colSet("revolving_interest6", ldStd6);
    	
    	wp.colSet("revolve_int_sign", lsSign);
    	wp.colSet("revolve_int_rate", ldcIntRate);
    	wp.colSet("revolve_rate_s_month", lsSMonth);
    	wp.colSet("revolve_rate_e_month", lsEMonth);
    	wp.colSet("revolve_reason", lsIntReason);
    	wp.colSet("ex_revolve_int_sign", lsSign);
    	wp.colSet("ex_revolve_int_rate", ldcIntRate);
    	wp.colSet("ex_revolve_rate_s_month", lsSMonth);
    	wp.colSet("ex_revolve_rate_e_month", lsEMonth);
    	wp.colSet("tt_revolve_reason", commString.decode(lsIntReason, ",I,J,2,K,L", ",I.頂級卡,J.系統調整,2.人工調整,K.前協,L.個別協商"));
    	
    	wp.colSet("revolve_int_sign_2", lsSign2);
    	wp.colSet("revolve_int_rate_2", ldcIntRate2);
    	wp.colSet("revolve_rate_s_month_2", lsSMonth2);
    	wp.colSet("revolve_rate_e_month_2", lsEMonth2);
    	wp.colSet("revolve_reason_2", lsIntReason2);
    	wp.colSet("ex_revolve_int_sign2", lsSign2);
    	wp.colSet("ex_revolve_int_rate2", ldcIntRate2);
    	wp.colSet("ex_revolve_rate_s_month2", lsSMonth2);
    	wp.colSet("ex_revolve_rate_e_month2", lsEMonth2);
    	wp.colSet("tt_revolve_reason2", commString.decode(lsIntReason2, ",I,J,2,K,L", ",I.頂級卡,J.系統調整,2.人工調整,K.前協,L.個別協商"));
    	
    	wp.colSet("ex_acct_status", lsAcctStatus);
		wp.colSet("tt_acct_status", commString.decode(lsAcctStatus, ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));
    	wp.colSet("ex_dual_flag", "Y");
	}
	
    @Override
    public void querySelect() throws Exception {
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {

    }

    //每次進來都重算
    void wfSetIntRate() throws Exception {
    	double ldcRate, ldcAdj, lmRate;
    	String lsYm, lsYm1, lsYm2, lsSign;
    	double fixRate = 19.71;
    	
    	//-set initail value-
    	int j=0;
    	lsYm = isSysYM;
    	wp.listCount[0] = 12;
    	for (int ii = 0; ii < 12; ii++) {
    		ListExYymm.add(lsYm);
    		ListExOrate.add("19.71");
    		ListExRate.add("19.71");
    		ListExAdjRate.add("0");
    		
    		j = ii+1;
    		lsYm = commString.left(commDate.dateAdd(isSysYM+"01",0,j,0),6);
    		wp.serNum = String.format("%02d",j);
    		wp.setValue("SER_NUM", String.format("%02d",j), ii);
		}
    	
    	//-set OLD-int-rate-
    	//-int-rate1-
		lmRate = wp.colNum("revolve_int_rate");
		if (lmRate != 0) {
			lsSign = wp.colStr("revolve_int_sign");
			lsYm1  = wp.colStr("revolve_rate_s_month");
			lsYm2  = empty(wp.colStr("revolve_rate_e_month"))? "999912" : wp.colStr("revolve_rate_e_month");
			ldcRate = 0;
			if (eqIgno(lsSign,"+")) ldcRate=sum(fixRate,lmRate);  //ldc_rate=19.71 + lm_rate;
			if (eqIgno(lsSign,"-")) ldcRate=sub(fixRate,lmRate);  //ldc_rate=19.71 - lm_rate;
			for (int ii = 0; ii < ListExYymm.size(); ii++) {
				lsYm = ListExYymm.get(ii);
				if (chkStrend(lsYm1,lsYm) && chkStrend(lsYm,lsYm2)) {
					ListExOrate.set(ii, String.valueOf(ldcRate));
					ldcAdj = sub(toNum(ListExRate.get(ii)),ldcRate);
					ListExAdjRate.set(ii, String.valueOf(ldcAdj));
				}
			}
		}
		//-int-rate2-
		lmRate = wp.colNum("revolve_int_rate_2");
		if (lmRate != 0) {
			lsSign = wp.colStr("revolve_int_sign_2");
			lsYm1  = wp.colStr("revolve_rate_s_month_2");
			lsYm2  = empty(wp.colStr("revolve_rate_e_month_2"))? "999912" : wp.colStr("revolve_rate_e_month_2");
			ldcRate = 0;
			if (eqIgno(lsSign,"+")) ldcRate=sum(fixRate,lmRate);
			if (eqIgno(lsSign,"-")) ldcRate=sub(fixRate,lmRate);
			for (int ii = 0; ii < ListExYymm.size(); ii++) {
				lsYm = ListExYymm.get(ii);
				if (chkStrend(lsYm1,lsYm) && chkStrend(lsYm,lsYm2)) {
					ListExOrate.set(ii, String.valueOf(ldcRate));
					ldcAdj = sub(toNum(ListExRate.get(ii)),ldcRate);
					ListExAdjRate.set(ii, String.valueOf(ldcAdj));
				}
			}
		}

		//-set NEW-int-rate-
		//-int-rate1-
		lmRate = wp.colNum("ex_revolve_int_rate");
		lsSign = wp.colStr("ex_revolve_int_sign");
		if ((lmRate != 0) && (empty(lsSign)==false)) {
			lsYm1  = wp.colStr("ex_revolve_rate_s_month");
			lsYm2  = empty(wp.colStr("ex_revolve_rate_e_month"))? "999912" : wp.colStr("ex_revolve_rate_e_month");
			ldcRate = 0;
			if (eqIgno(lsSign,"+")) ldcRate=sum(fixRate,lmRate);
			if (eqIgno(lsSign,"-")) ldcRate=sub(fixRate,lmRate);
			for (int ii = 0; ii < ListExYymm.size(); ii++) {
				lsYm = ListExYymm.get(ii);
				if (chkStrend(lsYm1,lsYm) && chkStrend(lsYm,lsYm2)) {
					ListExRate.set(ii, String.valueOf(ldcRate));
					ldcAdj = sub(ldcRate,toNum(ListExOrate.get(ii)));
					ListExAdjRate.set(ii, String.valueOf(ldcAdj));
				}
			}
		}
		//-int-rate2-
		lmRate = wp.colNum("ex_revolve_int_rate2");
		lsSign = wp.colStr("ex_revolve_int_sign2");
		if ((lmRate != 0) && (empty(lsSign)==false)) {
			lsYm1  = wp.colStr("ex_revolve_rate_s_month2");
			lsYm2  = empty(wp.colStr("ex_revolve_rate_e_month2"))? "999912" : wp.colStr("ex_revolve_rate_e_month2");
			ldcRate = 0;
			if (eqIgno(lsSign,"+")) ldcRate=sum(fixRate,lmRate);
			if (eqIgno(lsSign,"-")) ldcRate=sub(fixRate,lmRate);
			for (int ii = 0; ii < ListExYymm.size(); ii++) {
				lsYm = ListExYymm.get(ii);
				if (chkStrend(lsYm1,lsYm) && chkStrend(lsYm,lsYm2)) {
					ListExRate.set(ii, String.valueOf(ldcRate));
					ldcAdj = sub(ldcRate,toNum(ListExOrate.get(ii)));
					ListExAdjRate.set(ii, String.valueOf(ldcAdj));
				}
			}
		}
    	
    	//final: set column
    	for (int ii = 0; ii < ListExYymm.size(); ii++) {
    		wp.colSet(ii,"p-ex_yymm", ListExYymm.get(ii));
    		wp.colSet(ii,"p-ex_orate", ListExOrate.get(ii));
    		wp.colSet(ii,"p-ex_rate", ListExRate.get(ii));
    		wp.colSet(ii,"p-ex_adj_rate", ListExAdjRate.get(ii));
    	}
    }
    

	@Override
	public void saveFunc() throws Exception {
		func = new Actm2050Func(wp);
		
//		if (of_validation()<0) return;
		if (ofValidation()<0) {
			if (breakpoint==0) { //中斷做confirm時,不清breakpoint_x ; 其他狀況(跳出or完成)清除breakpoint_x
				wp.colSet("breakpoint_1","");
		    	wp.colSet("breakpoint_2","");
			}
			return;
		}
		wp.colSet("breakpoint_1","");
    	wp.colSet("breakpoint_2","");

//		if (is_action.equals("D")){
//			if of_excmsg("是否取消資料異動")<>1 then Return -1
//		}
    	
		if (empty(wp.itemStr2("rowid"))) {
			alertErr("未讀取持卡人之 [帳戶資料]; 請重新讀取");
			return;
		}
    	
    	ofUpdate();  //set log_data
    
        rc = func.dbSave(strAction);
        if (rc!=1) {
            alertErr2(func.getMsg());
        }
        this.sqlCommit(rc);
        
        if (rc == 1 && eqIgno(strAction,"U")) {
        	queryRead();
    	}
        
    }
    
    int ofValidation() throws Exception {
		String ss;
		String lsSMonth, lsEMonth, lsIsTmp="N";
		int liOk1, liOk2;
		String lsSMonth2, lsEMonth2, lsNextAcctMm;

		mAccttype = wp.itemStr2("acct_type");
		mAcctkey = wp.itemStr2("acct_key");
		func.varsSet("dual_no", mAccttype + mAcctkey);
		
		lsIsTmp = wp.itemStr2("ex_dual_flag");
		if (empty(mAccttype) || empty(mAcctkey)) {
			alertErr("帳戶類別, 帳戶帳號 不可空白");
			return -1;
		}
		//-刪除暫存檔------------------------------
		if (eqIgno(strAction,"D") && eqIgno(lsIsTmp,"Y")) {
			return 1;
		}
		
//		is_sysYM = zzStr.left(ls_sysdate,6);
//		is_sysYM12 =Left(iuo_date.of_relativeymd(ls_sysdate,1,0,0),6)

//		//最近一年調整利率
		wfSetIntRate();	//請參考4.7

		//-1-
		liOk1 = wfChkIntData(1);
		if (liOk1 == -1) return -1;
		//-2-
		liOk2 = wfChkIntData(2);
		if (liOk2 == -1) return -1;
		if (liOk1==0 && liOk2==0){
			alertErr("資料未異動, 不可存檔");
			return -1;
		}

		lsSMonth = wp.itemStr2("ex_revolve_rate_s_month");
		lsEMonth = wp.itemStr2("ex_revolve_rate_e_month");
		lsSMonth2 = wp.itemStr2("ex_revolve_rate_s_month2");
		lsEMonth2 = wp.itemStr2("ex_revolve_rate_e_month2");
		if (empty(lsSMonth2)==false || empty(lsEMonth2)==false) {
			if (empty(lsEMonth)==false && chkStrend(lsEMonth,lsSMonth2)==false) {
				alertErr("利率調整 [有效期間] 重疊");
				return -1;
			}
			if (empty(lsEMonth)) {
				alertErr("利率調整 [有效期間] 重疊");
				return -1;
			}
		}
		//-JH(B98158)-
		wfSetEndMm();
		if (empty(wp.itemStr2("ex_pay_by_stage"))) {
			if (empty(lsEMonth)==false && chkStrend(isSysYM,lsEMonth)) {
				ss = commString.right(lsEMonth, 2);
				if (commString.pos(isEndMm, ss)<0) {
					alertErr("有效迄月 只可為 "+isEndMm.substring(1));
					return -1;
				}
			}
			if (empty(lsEMonth2)==false && chkStrend(isSysYM,lsEMonth2)) {
				ss = commString.right(lsEMonth2, 2);
				if (commString.pos(isEndMm, ss)<0) {
					alertErr("有效迄月 只可為 "+isEndMm.substring(1));
					return -1;
				}
			}
		}
		//-JH(B98015)-
		lsNextAcctMm = wp.itemStr2("next_acct_month");
		double lmAdjRate;
		for (int ii = 0; ii < ListExYymm.size(); ii++) {
			lmAdjRate = toNum(ListExAdjRate.get(ii));
			if (lmAdjRate == 0) continue;
			if (chkStrend(ListExYymm.get(ii),lsNextAcctMm)) {
				alertErr("利率異動起始月份 須大於 下次帳務月份");
				return -1;
			}
		}

  //若act_acno原先已有利率異動值時，前項判斷會有遺漏，因此增加以下判斷以防錯誤
		if (liOk1 !=  0) { //有更改 revolve_1 參數
			//wf_chk_int_data()已有判斷起、迄日期大小比對，所以此處只判斷起始日即可
			//另外，wf_chk_int_data()只有act_acno原利率異動值是空值時，才有判斷須大於系統年月
			if (empty(lsSMonth)==false && chkStrend(lsSMonth,lsNextAcctMm)==true) {
				alertErr("利率異動起始月份 須大於 下次關帳月份");
				return -1;
			}
		}

		if (liOk2 !=  0) { //有更改 revolve_2 參數
			//wf_chk_int_data()已有判斷起、迄日期大小比對，所以此處只判斷起始日即可
			//另外，wf_chk_int_data()只有act_acno原利率異動值是空值時，才有判斷須大於系統年月
			if (empty(lsSMonth2)==false && chkStrend(lsSMonth2,lsNextAcctMm)==true) {
				alertErr("利率異動起始月份 須大於 下次關帳月份");
				return -1;
			}
		}

		//-JH:R98011-check 例外檔-
		long llCnt=0;
		String lsSql = "select count(*) as ll_cnt from act_idno_rate "
				//+ "where id_no = :id_no "
				  + "where id_p_seqno = :id_p_seqno "
				  + "  and  ( revolve_rate_s_month >= to_char(sysdate,'yyyymm') "
				  + "      or revolve_rate_e_month >= to_char(sysdate,'yyyymm') ) ";
	//setString("id_no", wp.item_ss("id_no"));
		setString("id_p_seqno", wp.itemStr2("id_p_seqno"));
		sqlSelect(lsSql);
		llCnt = (long) sqlNum("ll_cnt");
		if (llCnt>0) {
//			if of_excmsg("持卡人於[優惠利率例外檔]有資料, 是否存檔")<>1 then Return -1
			if (eqIgno(wp.itemStr2("breakpoint_1"),"Y")==false) {
				breakpoint = 1;
	    		wp.colSet("breakpoint", 1);
	    		wp.dispMesg = "檢核中...";
	    		return -1;
	    	}
		}
		//-利率調高:月份-
		if (wfCheckAdjRateHigh() == -1) return -1;
		//-R100-014-
		if (wfCheckTop() != 1) return -1;
		
		return 1;
	}
    
    int wfChkIntData(int aiType) throws Exception {
    	String lsSign, lsYm1, lsYm2, lsOsign, lsOym1, lsOym2;
    	double ldcRate, ldcOrate;
    	double ldcInt1, ldcInt2, ldcInt3, ldcInt4, ldcInt5, ldcInt6, ldcMaxRc;
    	
    	switch (aiType) {
			case 1:
				lsSign = wp.itemStr2("ex_revolve_int_sign");
				ldcRate = wp.itemNum("ex_revolve_int_rate");
				lsYm1 = wp.itemStr2("ex_revolve_rate_s_month");
			    lsYm2 = wp.itemStr2("ex_revolve_rate_e_month");
			    lsOsign = wp.itemStr2("revolve_int_sign");
			    ldcOrate = wp.itemNum("revolve_int_rate");
				lsOym1 = wp.itemStr2("revolve_rate_s_month");
			    lsOym2 = wp.itemStr2("revolve_rate_e_month");
				break;
			case 2:
				lsSign = wp.itemStr2("ex_revolve_int_sign2");
				ldcRate = wp.itemNum("ex_revolve_int_rate2");
				lsYm1 = wp.itemStr2("ex_revolve_rate_s_month2");
			    lsYm2 = wp.itemStr2("ex_revolve_rate_e_month2");
			    lsOsign = wp.itemStr2("revolve_int_sign_2");
			    ldcOrate = wp.itemNum("revolve_int_rate_2");
				lsOym1 = wp.itemStr2("revolve_rate_s_month_2");
			    lsOym2 = wp.itemStr2("revolve_rate_e_month_2");
				break;
			default:
				alertErr("檢核 [類別參數] 錯誤, ai_adj_type="+aiType);
				return -1;
    	}
    	if (!empty(lsYm1) && empty(lsYm2)) lsYm2 = "999912";
    	if (!empty(lsOym1) && empty(lsOym2)) lsOym2 = "999912";
    	if (ldcRate==0 && empty(lsYm1) && empty(lsYm2)) lsSign = "";
    	if (ldcOrate==0 && empty(lsOym1) && empty(lsOym2)) lsOsign = "";
    	//-資料無異動-
    	if (eqIgno(lsSign,lsOsign) && ldcRate==ldcOrate 
    			&& eqIgno(lsYm1,lsOym1) && eqIgno(lsYm2,lsOym2)) {
    		return 0;
    	}
    	if (ldcRate !=0 || empty(lsYm1)==false || empty(lsYm1)==false) {
    		if (empty(lsSign)) {
    			alertErr("利率加減碼之[加減] 不可空白");
				return -1;
    		}
    		if (ldcRate <= 0) {
    			alertErr("利率加減碼之[利率] 須大於 0");
				return -1;
    		}
    		if (empty(lsYm1) && empty(lsYm2)) {
    			alertErr("利率加減碼之[生效年月] 不可空白");
				return -1;
    		}
    		if (empty(lsYm2)==false && chkStrend(lsYm1,lsYm2)==false) {
    			alertErr("利率加減碼 之[生效年月] 起迄輸入錯誤");
				return -1;
    		}
    	}
    	if (ldcRate==0) return 1;
    	
    //-新增-
    //if (ldcOrate==0 && ldcRate != 0) {
    //	if (chkStrend(lsYm1,isSysYM)) {
    //		alertErr("新增 利率調整之[生效年月起] 須大於系統年月: "+isSysYM);
		//	return -1;
    //	}
    //}
    	
    	//-標準利率-
    	ldcInt1 = wp.itemNum("revolving_interest1");
    	ldcInt2 = wp.itemNum("revolving_interest2");
    	ldcInt3 = wp.itemNum("revolving_interest3");
    	ldcInt4 = wp.itemNum("revolving_interest4");
    	ldcInt5 = wp.itemNum("revolving_interest5");
    	ldcInt6 = wp.itemNum("revolving_interest6");
    	
    	//-加碼-
    	if (eqIgno(lsSign,"+")) {
    		String lsSql = "select rc_max_rate from ptr_actgeneral "
						  + sqlRownum(1);
			  sqlSelect(lsSql);
			  if (sqlRowNum == 0) {
				  ldcMaxRc = 0;
			  } else {
				  ldcMaxRc = sqlNum("rc_max_rate");
			  }
			  if ((ldcRate+ldcInt1) > ldcMaxRc || (ldcRate+ldcInt2) > ldcMaxRc 
				  || (ldcRate+ldcInt3) > ldcMaxRc || (ldcRate+ldcInt4) > ldcMaxRc
				  || (ldcRate+ldcInt5) > ldcMaxRc || (ldcRate+ldcInt6) > ldcMaxRc) {
				  alertErr("利率調整後大於 MAX RC (" + ldcMaxRc + ")");
				  return -1;
			  }
    	}
    	//-加碼:減max[標準利率] must be >0-
    	if (eqIgno(lsSign,"-")) {
    		ldcMaxRc = 0;
    		if (ldcInt1 > ldcMaxRc) ldcMaxRc = ldcInt1;
    		if (ldcInt2 > ldcMaxRc) ldcMaxRc = ldcInt2;
    		if (ldcInt3 > ldcMaxRc) ldcMaxRc = ldcInt3;
    		if (ldcInt4 > ldcMaxRc) ldcMaxRc = ldcInt4;
    		if (ldcInt5 > ldcMaxRc) ldcMaxRc = ldcInt5;
    		if (ldcInt6 > ldcMaxRc) ldcMaxRc = ldcInt6;
    		
    		if (ldcRate > ldcMaxRc) {
    			alertErr("調整後小於0, 不允許調整!");
				  return -1;
    		}
    	}
    	
    	return 1;
    }
    
    void wfSetEndMm() throws Exception {
    	String lsMm;
    	int liMm;
    	isEndMm ="";
    	
    	String lsSql = "select run_month from cyc_jcic_grade "
				  + sqlRownum(1);
    	sqlSelect(lsSql);
    	if (sqlRowNum > 0) {
    		lsMm = sqlStr("run_month");
    	} else {
    		lsMm = "YYYYYYYYYYYY";
    	}

    	for (int ii = 0; ii < lsMm.length(); ii++) {
    		if (!eqIgno(commString.mid(lsMm, ii,1),"Y")) continue;
    			
    		liMm = ii+3;
    		if (liMm > 12) liMm = liMm - 12;
    		isEndMm +=","+String.format("%02d",liMm);
    	}
    	
    	if (empty(isEndMm)) isEndMm = ",01,02,03,04,05,06,07,08,09,10,11,12";
	}

	int wfCheckAdjRateHigh() throws Exception {
		String lsAcctMonth, lsHighMm="";
		double ldcAdj;

		//-排除分期還款戶-
		if (!empty(wp.itemStr2("ex_pay_by_stage"))) return 1;

		for (int ii = 0; ii < ListExYymm.size(); ii++) {
			ldcAdj = toNum(ListExAdjRate.get(ii));
			if (ldcAdj <= 0) continue;
			lsHighMm = ListExYymm.get(ii);
			break;
		}
    	if (empty(lsHighMm)) return 1;
		
    	lsAcctMonth = wp.itemStr2("ex_this_acct_month")+"01";
    	lsAcctMonth = commString.left(commDate.dateAdd(lsAcctMonth,0,2,0),6);
    	if (chkStrend(lsAcctMonth,lsHighMm)==false) {
    		alertErr("調升利率  須前二個月公告[起始月份須大於關帳月份+2]");
			return -1;
    	}
    	
    	return 1;
    }
    
    int wfCheckTop() throws Exception {
    	String lsPSeqno;
        long llCnt;
        String lsSign, lsYm1, lsYm2, lsOsign, lsOym1, lsOym2;
        double ldcRate, ldcOrate;

    	if (eqIgno(wp.itemStr2("revolve_reason"),"I")==false) return 1;
    	if (empty(wp.itemStr2("rowid"))) {
    		alertErr("未讀取 卡人帳戶資料");
			return -1;
    	}

    	lsSign = wp.itemStr2("ex_revolve_int_sign");
		ldcRate = wp.itemNum("ex_revolve_int_rate");
		lsYm1 = wp.itemStr2("ex_revolve_rate_s_month");
	    lsYm2 = wp.itemStr2("ex_revolve_rate_e_month");
	    lsOsign = wp.itemStr2("revolve_int_sign");
	    ldcOrate = wp.itemNum("revolve_int_rate");
		lsOym1 = wp.itemStr2("revolve_rate_s_month");
	    lsOym2 = wp.itemStr2("revolve_rate_e_month");
	    if (eqIgno(lsSign,lsOsign) && ldcRate==ldcOrate 
    		&& eqIgno(lsYm1,lsOym1) && eqIgno(lsYm2,lsOym2)) {
    		return 1;
    	}
	    
	    llCnt = 0;
	    lsPSeqno = wp.itemStr2("p_seqno");
	    String lsSql = "select count(*) as ll_cnt from act_acno "
	    			  + "where acno_p_seqno = :p_seqno "
	    			  + "and   revolve_reason = 'I' "
	    			  + "and   to_char(sysdate,'yyyymm') between decode(revolve_rate_s_month,'','19000101',revolve_rate_s_month) "
	    			  + "      and decode(revolve_rate_e_month,'','99991231',revolve_rate_e_month) ";
	    setString("p_seqno", lsPSeqno);
	    sqlSelect(lsSql);
	    llCnt = (long) sqlNum("ll_cnt");
	    if (llCnt>0) {
//	    	if of_excmsg("頂級卡客戶利率，尚於優惠利率適用期間~n是否修改")<>1 then return -1  //todo
	    	if (eqIgno(wp.itemStr2("breakpoint_2"),"Y")==false) {
	    		breakpoint = 2;
	    		wp.colSet("breakpoint", 2);
	    		wp.dispMesg = "檢核中...";
	    		return -1;
	    	}
	    }
    	
    	return 1;
    }
    
    void ofUpdate() throws Exception {
    	String lsLog="";
    	// make log_data string

    	lsLog  = String.format("%07.3f",wp.itemNum("revolving_interest1"));
    	lsLog += String.format("%07.3f",wp.itemNum("revolving_interest2"));
    	lsLog += String.format("%07.3f",wp.itemNum("revolving_interest3"));
    	lsLog += String.format("%07.3f",wp.itemNum("revolving_interest4"));
    	lsLog += String.format("%07.3f",wp.itemNum("revolving_interest5"));
    	lsLog += String.format("%07.3f",wp.itemNum("revolving_interest6"));
    	//-利率一-
    	lsLog += fixLength(wp.itemStr2("ex_revolve_int_sign"),1);
    	lsLog += String.format("%06.3f",wp.itemNum("ex_revolve_int_rate"));
    	lsLog += fixLength(wp.itemStr2("ex_revolve_rate_s_month"),6);
    	lsLog += fixLength(wp.itemStr2("ex_revolve_rate_e_month"),6);
    	lsLog += fixLength(wp.itemStr2("revolve_reason"),1);
    	//-利率二-
    	lsLog += fixLength(wp.itemStr2("ex_revolve_int_sign2"),1);
    	lsLog += String.format("%06.3f",wp.itemNum("ex_revolve_int_rate2"));
    	lsLog += fixLength(wp.itemStr2("ex_revolve_rate_s_month2"),6);
    	lsLog += fixLength(wp.itemStr2("ex_revolve_rate_e_month2"),6);
    	lsLog += fixLength(wp.itemStr2("revolve_reason_2"),1);
    	lsLog += fixLength(wp.itemStr2("ex_acct_status"),1);
    	//-ACT_ACNO.old-
    	lsLog += fixLength(wp.itemStr2("revolve_int_sign"),1);
    	lsLog += String.format("%06.3f",wp.itemNum("revolve_int_rate"));
    	lsLog += fixLength(wp.itemStr2("revolve_rate_s_month"),6);
    	lsLog += fixLength(wp.itemStr2("revolve_rate_e_month"),6);
    	lsLog += fixLength(wp.itemStr2("revolve_int_sign_2"),1);
    	lsLog += String.format("%06.3f",wp.itemNum("revolve_int_rate_2"));
    	lsLog += fixLength(wp.itemStr2("revolve_rate_s_month_2"),6);
    	lsLog += fixLength(wp.itemStr2("revolve_rate_e_month_2"),6);
    	//--因若old沒有值空白會被trim掉最後加1個/讓值不會被trim掉
    	lsLog += "/";
    	//if ib_dflag then ls_dual_flag  = 'D'
    log("of_update: ["+lsLog+"]");
    	func.varsSet("log_data", lsLog);
    }

    @Override
    public void initButton() {
    //if (wp.respHtml.indexOf("_detl") > 0) {
    //    this.btnMode_aud();
    //}

	    String sKey = "1st-page";
      if (wp.respHtml.equals("actm2050"))  {
         wp.colSet("btnUpdate_disable","");
         wp.colSet("btnDelete_disable","");
         this.btnModeAud(sKey);
      }

    }

    @Override
    public void dddwSelect() {
        try {	
			wp.optionKey = wp.itemStr2("ex_acct_type");
			dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_curr_code");
			dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
        }
        catch(Exception ex) {}
    }
    
    //from Colr0230
    String fixLength(String laText, int laWidth) throws Exception {
		String rtn = laText;
		if (empty(rtn)) rtn="";
		int fnum;
//		if (txtLen(la_text) < la_width) {
//			fnum = la_width - txtLen(la_text);
//			rtn += zzStr.fill(' ',fnum);
//		}
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
    
	//double 相加:
	double sum(double d1,double d2) {
		BigDecimal bd1 = new BigDecimal(Double.toString(d1));
		BigDecimal bd2 = new BigDecimal(Double.toString(d2));
		return bd1.add(bd2).doubleValue();
	}
	//double 相減:
	double sub(double d1,double d2) {
		BigDecimal bd1 = new BigDecimal(Double.toString(d1));
		BigDecimal bd2 = new BigDecimal(Double.toString(d2));
		return bd1.subtract(bd2).doubleValue();
	}

//	public static int txtLen(String str) {
//		int len = 0;
//		for(int i=0;i<str.length();i++){
//			int acsii = str.charAt(i); 
//			len+=(acsii<0 || acsii > 128)?2:1;
//		}
//		return len;
//	}

}

