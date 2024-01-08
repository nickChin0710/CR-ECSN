/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-05  V1.00.00  OrisChang  program initial                            *
* 107-08-21  V1.00.01  Alex       confirm , tempYN , querry errmsg fixed     *
* 111-10-20  V1.00.03  Machao      sync from mega & updated for project coding standard                                                                            *
******************************************************************************/

package actm01;

import java.text.SimpleDateFormat;
import java.util.Date;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actm0070 extends BaseEdit {
	String mExAcctType = "";
	String mExAcctKey = "";
	String pCurrCode = "";
	String pPSeqno = "";
	String mAcctMonth = "";
	String acctDataHead = "";

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
			// updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
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
	public void initPage() {
		if (empty(strAction)) {
			wp.colSet("temp_yn", "N");
			wp.itemSet("ex_curr_code", "901"); // 下拉選單要用這個
		//wp.col_set("btnUpdate_disable", "disabled");
		//wp.col_set("btnDelete_disable", "disabled");
		}
	}

	@Override
	public void queryFunc() throws Exception {
		// 設定queryRead() SQL條件
		String lsPSeqno = getInitParm();

		if (lsPSeqno.equals("")) {
			alertErr2("無此帳號");
			return;
		}

		if (!lsPSeqno.equals("")) {
			getDtlData(lsPSeqno);
		}

		pPSeqno = lsPSeqno;

		pCurrCode = wp.itemStr2("ex_curr_code");
		wp.colSet("q_curr_code", wp.itemStr2("ex_curr_code"));

		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		Object[] param = null;
		String lsSql = "";

		lsSql += " select hex(rowid) as rowid, curr_code, acct_data ";
		lsSql += " from ACT_MODDATA_TMP ";
		lsSql += " where p_seqno = ? and curr_code = ? and act_modtype='03' ";
		param = new Object[] { pPSeqno, pCurrCode };

		sqlSelect(lsSql, param);
		if (sqlRowNum<=0) {

			// select columns
			lsSql = "SELECT hex(rowid) as rowid , p_seqno, curr_code, dc_autopay_beg_amt, " + // --自動扣繳期初金額
					"       dc_autopay_bal, " + // --自動扣繳餘額(右)
					"       dc_min_pay_bal, " + // --最低應繳款餘額
					"       dc_acct_jrnl_bal " + // --目前欠款總餘額
					"    FROM act_acct_curr " + "   WHERE p_seqno = ? " + "     and curr_code = ? ";

			param = new Object[] { pPSeqno, wp.itemStr2("ex_curr_code") };

			sqlSelect(lsSql, param);

			if (sqlRowNum<=0) {				
				alertErr2("查無資料 , 幣別:"+wp.itemStr2("ex_curr_code"));
				return;
			} else {
				wp.colSet("dc_autopay_beg_amt", sqlStr("dc_autopay_beg_amt"));
				wp.colSet("dc_autopay_bal", sqlStr("dc_autopay_bal"));
				wp.colSet("txt_autopay_bal", sqlStr("dc_autopay_bal"));
				wp.colSet("dc_min_pay_bal", sqlStr("dc_min_pay_bal"));
				wp.colSet("dc_acct_jrnl_bal", sqlStr("dc_acct_jrnl_bal"));
				wp.colSet("curr_code", sqlStr("curr_code"));
				wp.colSet("rowid", sqlStr("rowid"));
				wp.colSet("temp_yn", "N");
			}

		} else {
			// cycle/month/status/beg/min/jrnl/txt/user/date/auto
			// String[] itm = sql_ss("acct_data").split("|"); //split("|")會有問題,改成split("@")
			String[] itm = sqlStr("acct_data").split("@");
			wp.colSet("dc_autopay_beg_amt", itm[3]);
			wp.colSet("dc_autopay_bal", itm[9]);
			wp.colSet("txt_autopay_bal", itm[6]);
			wp.colSet("dc_min_pay_bal", itm[4]);
			wp.colSet("dc_acct_jrnl_bal", itm[5]);
			wp.colSet("curr_code", sqlStr("curr_code"));
			wp.colSet("temp_yn", "Y");
			wp.colSet("rowid", sqlStr("rowid"));
		}
		wp.setListCount(1);

	//wp.col_set("q_dc_autopay_bal", wp.col_ss("dc_autopay_bal"));
		wp.colSet("q_dc_autopay_bal", wp.colStr("txt_autopay_bal"));

    /***
		wp.col_set("btnUpdate_disable", "");
		if(wp.col_eq("temp_yn", "N")){
			wp.col_set("btnUpdate_disable", "disabled");
		}
    ***/

	}

	private String getInitParm() throws Exception {
		String lsSql = "";

		lsSql = " select acct_type, acct_key, p_seqno, uf_acno_name(p_seqno) as acno_cname ";
		lsSql += " from act_acno ";
		lsSql += " where 1=1 ";
		lsSql += " and acno_p_seqno = p_seqno ";
		lsSql += " and acct_type = :acct_type and acct_key = :acct_key ";
		String acctkey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
		wp.colSet("ex_acct_key", acctkey);
		wp.itemSet("ex_acct_key", acctkey);
		setString("acct_type", wp.itemStr2("ex_acct_type"));
//		setString("acct_key", wp.item_ss("ex_acct_key"));
		setString("acct_key",  acctkey);

		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("q_acct_type", sqlStr("acct_type"));
			wp.colSet("q_acct_key", sqlStr("acct_key"));
			wp.colSet("q_p_seqno", sqlStr("p_seqno"));
			wp.colSet("q_id_cname", sqlStr("acno_cname"));
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
		mAcctMonth = "";

		// acctDataHead = "";
		if (empty(sqlStr("acct_status"))) {
			wp.colSet("q_acct_status", "");
			wp.colSet("q_stmt_cycle", "");
			wp.colSet("q_this_acct_month", "");
			wp.colSet("h_this_acct_month", "");
			wp.colSet("q_p_seqno", pSeqno);
		} else {
			// acctDataHead += sql_ss("stmt_cycle") + "|";
			// acctDataHead += sql_ss("this_acct_month") + "|";
			// acctDataHead += sql_ss("acct_status") + "|";

			String sStatus = "";
			wp.colSet("h_acct_status", sqlStr("acct_status"));
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
			wp.colSet("h_this_acct_month", sqlStr("this_acct_month"));
			sYyymm = sqlStr("this_acct_month");
			if (!sYyymm.trim().equals("")) {
				sYyymm = String.valueOf(Integer.parseInt(sYyymm) - 191100);
				mAcctMonth = sYyymm;
				sYyymm = sYyymm.substring(0, 3) + "/" + sYyymm.substring(3);
			}
			wp.colSet("q_this_acct_month", sYyymm);
			wp.colSet("q_p_seqno", pSeqno);
		}

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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String sysdate = sdf.format(new Date());

		if (   !eqIgno(wp.itemStr2("ex_acct_type"),wp.itemStr2("q_acct_type")) 
				|| !eqIgno(wp.itemStr2("ex_acct_key"),wp.itemStr2("q_acct_key"))
				|| !eqIgno(wp.itemStr2("ex_curr_code"),wp.itemStr2("q_curr_code"))
			 )
		{
			alertErr("KEY值已更改, 請重新查詢再進行存檔或刪除");
			return;
		}
		
		if (strAction.equals("U")) {
				if ( wp.itemStr2("txt_autopay_bal").equals(wp.itemStr2("q_dc_autopay_bal")) ) {
	    		alertErr("未更改 自動扣繳餘額, 不可存檔");
			    return;
				}
			}


		String acctData = "";
		Actm0070Func func = new Actm0070Func(wp);

		try {
			// beg/min/jrnl/txt/user/date/auto
			acctData += wp.itemStr2("q_stmt_cycle") + "@";
			acctData += wp.itemStr2("h_this_acct_month") + "@";
			acctData += wp.itemStr2("h_acct_status") + "@";

			acctData += wp.itemStr2("dc_autopay_beg_amt") + "@";
			acctData += wp.itemStr2("dc_min_pay_bal") + "@";
			acctData += wp.itemStr2("dc_acct_jrnl_bal") + "@";
			acctData += wp.itemStr2("txt_autopay_bal") + "@";
			acctData += wp.loginUser + "@";
			acctData += sysdate + "@";
			acctData += wp.itemStr2("dc_autopay_bal") + "@";

			String colActModtype = "03";
			String colPSeqno = wp.itemStr2("q_p_seqno");
			String colAcctType = wp.itemStr2("q_acct_type");
			String colAcctKey = wp.itemStr2("q_acct_key");
			String colCurrCode = wp.itemStr2("curr_code");

			wp.listCount[0] = 1;

      /***
			// -delete detail-
			if (func.dbDelete() < 0) {
				alert_err(func.getMsg());
				sql_commit(0);
				return;
			}
			if (is_action.equals("U")) {
				func.vars_set("act_modtype", col_act_modtype);
				func.vars_set("p_seqno", col_p_seqno);
				func.vars_set("curr_code", col_curr_code);
				func.vars_set("acct_type", col_acct_type);
				func.vars_set("acct_key", col_acct_key);
				func.vars_set("acct_data", acct_data);
				func.dbInsert();
				if (rc != 1) {
					err_alert(func.getMsg());
				}
			}
      ***/
			if (strAction.equals("U")) {  //更新的做法：刪除再新增
  			if (func.dbDelete() < 0) {
	  			alertErr(func.getMsg());
		  		sqlCommit(0);
			  	return;
			  }
				func.varsSet("act_modtype", colActModtype);
				func.varsSet("p_seqno", colPSeqno);
				func.varsSet("curr_code", colCurrCode);
				func.varsSet("acct_type", colAcctType);
			//func.vars_set("acct_key", col_acct_key);
				func.varsSet("acct_data", acctData);
				func.dbInsert();
				if (rc != 1) {
					alertErr2(func.getMsg());
				}
		  }	else	if (this.isDelete()) {
			  rc = func.dbDelete();
			  sqlCommit(rc);
			  if(rc!=1){
				  errmsg(func.getMsg());
			  }		
			}	else  {	
			  wp.listCount[0] = 0;
			}

			sqlCommit(rc);
		} catch (Exception ex) {}

	}

	@Override
	public void initButton() {		
	//this.btnMode_aud();
		String sKey = "1-page";
    this.btnModeAud(sKey);
  
    /***
		if(wp.col_eq("temp_yn", "N")){
			button_off("btnDelete_disable");
		}
    ***/

	}

	@Override
	public void dddwSelect() {
		try {
			// wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.optionKey = wp.itemStr2("ex_curr_code");
			// this.dddw_list("dddw_curr_code", "PTR_CURRCODE", "curr_code",
			// "curr_chi_name", "where 1=1 order by curr_code");
			this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
					"where wf_type = 'DC_CURRENCY' order by wf_id");
		} catch (Exception ex) {
		}
	}

	public void forinitinfo(TarokoCommon wr) throws Exception {

		super.wp = wr;
		String accttyp = "", acctkey = "", cardno = "";
		accttyp = wp.itemStr2("accttyp");
		acctkey = wp.itemStr2("acctkey");
		cardno = wp.itemStr2("cardno");

		if (empty(acctkey) && empty(cardno)) {
			return;
		}

		if (empty(cardno)) {
			if (!(empty(accttyp) && empty(acctkey))) {
				return;
			}
		}

		Object[] param = null;
		String lsSql = "";
		lsSql += " select acct_type, acct_key, p_seqno, "
				// + " uf_acno_name(p_seqno) as acno_cname "
				+ "       'TESTCNAME' as acno_cname                "
				+ " from act_acno                                  "
				+ " where 1=1                                      " + "and acno_p_seqno = p_seqno ";
		if (!empty(accttyp)) {
			lsSql += "and acct_type =?  and acct_key =?";
			param = new Object[] { accttyp, acctkey };
		} else {
			lsSql += "and   p_seqno in (select p_seqno from crd_card where card_no =?)";
			param = new Object[] { cardno };
		}
		sqlSelect(lsSql, param);

		if (empty(sqlStr("acct_status"))) {
			return;
		} else {
			wp.addJSON("q_id_cname", sqlStr("acno_cname"));
			wp.addJSON("q_corp_cname", sqlStr("acno_cname"));

			wp.colSet("q_id_cname", sqlStr("acno_cname"));
			wp.colSet("q_corp_cname", sqlStr("acno_cname"));
		}

		return;
	}

	String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}
}
