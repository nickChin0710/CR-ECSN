/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 108/12/25  V1.00.01  phopho     bug fix: 0001649                           *
* 109/01/31  V1.00.02  phopho     bug fix: wp.listCount                      *
* 109-07-27  V1.00.00  Andy       program Re initial                         *
* 109-08-18  V1.00.01  Andy       Update:Mantis3944                          *
* 109-09-30  V1.00.02  Andy       Update:Mantis4292                          *
* 110-01-12  V1.00.03  Andy       Update:Mantis5429                          *
* 110-08-02  V1.00.04  Andy       Update:Mantis8067                          *
* 110-08-03  V1.00.05  Andy       Update:Mantis8067                          *
* 111-10-20  V1.00.06  Machao     sync from mega & updated for project coding standard*
* 112/03/20  V1.00.07  Simon      empty(aaPaymentNo[ii]) corrected to empty(aaPayCardNo[ii]) in ofValidation(int ii)*
* 112-06-28  V1.00.08  Simon      1.取消寫入 "借方科目"、"銷帳鍵值"          *
*                                 2.取消繳款來源 "支票繳款"、"郵局劃撥"，    *
*                                   新增"他行代償"、"債務協商還款"，"前置協商還款"*
*                                 3.codes changes comply with 20200107 modified ajax practice *  
* ****************************************************************************/

package actm01;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actm2020 extends BaseEdit {
	int rr = -1;
	String mAccttype = "";
	String mAcctkey = "";
	String mCurrcode = "";
	String mBatchno = "";
	String gsBatchNo = "";
	String gsCardNo = "";
	String gsPSeqno = "";
	String gsAcctType = "";
	String gsAcctKey = "";
	String gsIdPSeqno = "";
	String gsAcnoPSeqno = "";
	int ilOk = 0;
	int ilErr = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "X1")) {
			/* 查詢功能 */
//			is_action = "new";
//			clearFunc();
			strAction = "Q1";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "Q1")) {
			/* 查詢功能 */
			strAction = "Q1";
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
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			// delFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "S2")) {
			/* 瀏覽頁存檔 */
			strAction = "S2";
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "S3")) {
			/* 新增序號存檔 */
			strAction = "S3";
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "S4")) {
			/* 新增批號存檔 */
			strAction = "S4";
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "set_batchno")) {
			/* 清畫面 */
			strAction = "";
			setBatchno();
		} else if (eqIgno(wp.buttonCode, "AJAX")) {
			/* 20200107 modify AJAX */
			strAction = "AJAX";
			wfChkCol(wr);
		} else if (eqIgno(wp.buttonCode, "re_load")) {
			dddwSelect();
		} 
		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		wp.colSet("sum_ct","0");
		wp.colSet("sum_amt","0");
		
		this.btnModeAud();
		if (wp.respHtml.indexOf("_detl") > 0) {
//			wp.col_set("ex_debit_item", "60000300");
//			wp.col_set("chk_debit_item", "臨時存欠--業務系統");
			
			this.btnModeAud();
			try {
				setBatchno();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

	@Override
	public void queryFunc() throws Exception {
		// 設定queryRead() SQL條件
		if (empty(wp.itemStr2("ex_batch_no"))) {
			if (empty(mBatchno)) {
				alertErr("請選擇批號！");
				return;
			}
		}

		// 判斷摘要代碼是否為空值
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {

		wp.pageControl();
		wp.totalRows = 0;
		wp.sqlCmd = "  SELECT hex(act_pay_detail.rowid) as rowid,           "
				+ "         act_pay_detail.batch_no,                        "
				+ "         act_pay_detail.serial_no,                       "
				+ "         act_pay_detail.pay_card_no,                     "
				+ "         act_pay_detail.acct_type,                       "
				+ "         act_acno.acct_type,                             "
				+ "         act_acno.acct_key,                              "
				+ "         act_pay_detail.pay_amt,                         "
				+ "         act_pay_detail.pay_date,                        "
				+ "         act_pay_detail.payment_type,                    "
				+ "         decode(act_pay_detail.payment_type,'OTHR','其他方式繳款','TIKT','郵局劃撥',act_pay_detail.payment_type) as d_payment_type,  " 
				+ "         act_pay_detail.crt_user,                        "
				+ "         act_pay_detail.crt_date,                        "
				+ "         act_pay_detail.crt_time,                        "
				+ "         act_pay_detail.mod_user,                        "
				+ "         to_char(act_pay_detail.mod_time,'YYYYMMDD') mod_date, "
				+ "         to_char(act_pay_detail.mod_time,'hh24Miss') mod_time, "		
				+ "         '        ' as cpay_date,                        "
				+ "         act_pay_detail.p_seqno,                         "
				+ "         act_pay_detail.acno_p_seqno,                    "
				+ "         act_pay_detail.id_p_seqno,                      "
			//+ "         crd_idno.id_no,                                 "
			//+ "         crd_idno.id_no_code,                            "
				+ "         '0' as db_delcode,                              "
			//+ "         crd_idno.chi_name as cname,                     "
				+ "         decode(act_acno.acno_flag,'2',crd_corp.chi_name,crd_idno.chi_name) as cname, "
			//+ "         act_pay_detail.debit_item,                      "
			//+ "         act_pay_detail.debt_key,                        "
				+ "         '               ' as db_payment_no,             "
				+ "         'U' as p_status,                                "
				+ "         act_pay_detail.payment_no,                      "
				+ "         '' err_msg					                            "
				+ "    FROM act_pay_detail, act_acno                        "
				+ "    left join crd_idno on crd_idno.id_p_seqno = act_acno.id_p_seqno  "
				+ "    left join crd_corp on crd_corp.corp_p_seqno = act_acno.corp_p_seqno  "
				+ "    where act_pay_detail.acno_p_seqno = act_acno.acno_p_seqno "
				+ "    and act_pay_detail.batch_no = :ex_batch_no           ";
		if(strAction.equals("Q")){
			setString("ex_batch_no", wp.itemStr2("ex_batch_no"));
			if (!empty(wp.itemStr2("ex_acct_type")) && !empty(wp.itemStr2("ex_acct_key"))) {
				wp.sqlCmd += " and act_acno.acct_type = :accttype and act_acno.acct_key = :acctkey ";
				setString("accttype", wp.itemStr2("ex_acct_type"));
				setString("acctkey", wp.itemStr2("ex_acct_key"));
			}
			String exSerialNo1 = "",exSerialNo2 = "";
			int iSerialNo1 = 0,iSerialNo2 = 0;		
			if (!empty(wp.itemStr2("ex_serial_no1")) || !empty(wp.itemStr2("ex_serial_no2"))) {
				if(!empty(wp.itemStr2("ex_serial_no1"))){
					iSerialNo1 = Integer.parseInt(wp.itemStr2("ex_serial_no1"));
					exSerialNo1 = String.format("%05d", Integer.valueOf(iSerialNo1));
					wp.colSet("ex_serial_no1", exSerialNo1);
				} 
				if(!empty(wp.itemStr2("ex_serial_no2"))){
					iSerialNo2 = Integer.parseInt(wp.itemStr2("ex_serial_no2"));
					exSerialNo2 = String.format("%05d", Integer.valueOf(iSerialNo2));
					wp.colSet("ex_serial_no2", exSerialNo2);
				}
				wp.sqlCmd += sqlStrend(exSerialNo1, exSerialNo2, "act_pay_detail.serial_no");
			}
//			if(!empty(wp.item_ss("ex_debit_item"))){
//				wp.sqlCmd += sql_col(wp.item_ss("ex_debit_item"),"act_pay_detail.debit_item");
//			}
//			if(!empty(wp.item_ss("ex_debit_key"))){
//				wp.sqlCmd += sql_col(wp.item_ss("ex_debit_key"),"act_pay_detail.debit_key");
//			}
		}		
		if (strAction.equals("Q1")) {
			setString("ex_batch_no", itemKk("data_k1"));
			wp.sqlCmd += "and serial_no = (select max(serial_no) from act_pay_detail where batch_no =:ex_batch_no ) ";
			setString("ex_batch_no", wp.itemStr2("ex_batch_no"));			
		}

		wp.sqlCmd += " order by act_pay_detail.batch_no, act_pay_detail.serial_no ";

		wp.pageCountSql = "select count(*) FROM ( ";
		wp.pageCountSql += wp.sqlCmd + ")";

		if (empty(wp.itemStr2("ex_batch_no"))) {
			if (!empty(mBatchno))
				setString("ex_batch_no", mBatchno);
		} else {
			setString("ex_batch_no", wp.itemStr2("ex_batch_no"));
		}
		wp.colSet("sel_batch_no", wp.itemStr2("ex_batch_no"));
//		if (!empty(wp.item_ss("ex_acct_type")) && !empty(wp.item_ss("ex_acct_key"))) {
//			wp.pageCount_sql += " and act_acno.acct_type = :accttype and act_acno.acct_key = :acctkey ";
//			setString("accttype", wp.item_ss("ex_acct_type"));
//			setString("acctkey", wp.item_ss("ex_acct_key"));
//		}

		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		wp.setPageValue();
		int selCt = wp.selectCnt;
		String lsSerno = wp.colStr(selCt - 1, "serial_no");
		wp.colSet("sel_serial_no", lsSerno);
		if(strAction.equals("Q1")){
			wp.colSet("set_opt", "disabled style='display: none'");
		}
		if(strAction.equals("Q")){
/***
			String lsSql = " select ac_no,ac_full_name "
					+ " from gen_acct_m "
					+ " where ac_no =:ac_no ";
			setString("ac_no", wp.colStr(0,"debit_item"));
			sqlSelect(lsSql);
			wp.colSet("chk_debit_item",sqlStr("ac_full_name"));
			wp.colSet("ex_debit_item",wp.colStr(0,"debit_item"));
			wp.colSet("ex_debt_key",wp.colStr(0,"debt_key"));
***/
			double sumCt=0,sumAmt=0;
			for(int ii=0; ii< selCt;ii++){
				sumCt += 1;
				sumAmt += wp.colNum(ii,"pay_amt");
			}
			wp.colSet("sum_ct", sumCt);
			wp.colSet("sum_amt", sumAmt);
		}
		
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
		Actm2020Func func = new Actm2020Func(wp);
		String lsSql = "";
		int liCnt = 0;
		double ldSum = 0;
		String[] aaRowid = wp.itemBuff("rowid");
		String[] opt = wp.itemBuff("opt");
		String[] dpt = wp.itemBuff("dpt");
		String[] aaBatchNo = wp.itemBuff("batch_no");
		String[] aaSerialNo = wp.itemBuff("serial_no");
		String[] aaPayCardNo = wp.itemBuff("pay_card_no");
		String[] aaPaymentNo = wp.itemBuff("payment_no");
		String[] aaAcctType = wp.itemBuff("acct_type");
		String[] aaAcctKey = wp.itemBuff("acct_key");
		String[] aaPayAmt = wp.itemBuff("pay_amt");
		String[] aaPayDate = wp.itemBuff("pay_date");
		String[] aaPaymentType = wp.itemBuff("payment_type");
		String[] aaPSeqno = wp.itemBuff("p_seqno");
		String[] aaAcnoPSeqno = wp.itemBuff("acno_p_seqno");
		String[] aaIdPSeqno = wp.itemBuff("id_p_seqno");
	//String[] aaDebitItem = wp.itemBuff("debit_item");
	//String[] aaDebtKey = wp.itemBuff("debt_key");

		wp.listCount[0] = aaBatchNo.length;
		String ssBatchNo = "", ssSerialNo = "";
		// 修改/刪除
		int llOk = 0, llErr = 0;
		ssBatchNo = aaBatchNo[aaBatchNo.length - 1];
		ssSerialNo = aaSerialNo[aaSerialNo.length - 1];
		// 未放行資料修改
		if (strAction.equals("S2")) {
			for (rr = 0; rr < aaRowid.length; rr++) {
				if (!checkBoxOptOn(rr, opt)) {
					continue;
				}
				// 修改+刪除
				if (checkBoxOptOn(rr, dpt)) {
					wp.colSet(rr, "ok_flag", "X");
					wp.colSet(rr, "err_msg", "不可同時修改及刪除");
					llErr++;
					continue;
				}
				wp.colSet(rr, "set_opt", "checked"); // 保留頁面勾選狀態
				// 已覆核
				if (wfCheckConfirm(aaBatchNo[rr]) != 1) {
					wp.colSet(rr, "ok_flag", "X");
					wp.colSet(rr, "err_msg", "已放行資料不可修改");
					llErr++;
					continue;
				}
				rc = ofValidation(rr);
				if (rc != 1) {
					llErr++;
					continue;
				}
				func.varsSet("batch_no", aaBatchNo[rr]);
				func.varsSet("serial_no", aaSerialNo[rr]);
				func.varsSet("pay_card_no", aaPayCardNo[rr]);
				func.varsSet("payment_no", aaPaymentNo[rr]);
				func.varsSet("acct_type", aaAcctType[rr]);
				func.varsSet("acct_key", aaAcctKey[rr]);
				func.varsSet("pay_amt", aaPayAmt[rr]);
				func.varsSet("pay_date", aaPayDate[rr]);
			//func.varsSet("debit_item", aaDebitItem[rr]);
			//func.varsSet("debt_key", aaDebtKey[rr]);
				func.varsSet("rowid", aaRowid[rr]);
				func.varsSet("id_p_seqno", aaIdPSeqno[rr]);
				func.varsSet("acno_p_seqno", aaAcnoPSeqno[rr]);
				func.varsSet("p_seqno", aaPSeqno[rr]);
				rc = func.dbUpdate();
				if (rc == 1) {
				//sql_commit(rc);
					wp.colSet(rr, "ok_flag", "V");
					llOk++;
				}
			}
			for (int rr = 0; rr < aaRowid.length; rr++) {

				if (!checkBoxOptOn(rr, dpt)) {
					continue;
				}
				wp.colSet(rr, "set_dpt", "checked"); // 保留頁面勾選狀態
				func.varsSet("rowid", aaRowid[rr]);
				func.varsSet("aa_batch_no", aaBatchNo[rr]);
				func.varsSet("aa_serial_no", aaSerialNo[rr]);
				// return;
				rc = func.dbDelete();
				if (rc == 1) {
				//sql_commit(rc);
					wp.colSet(rr, "ok_flag", "V");
					wp.colSet(rr, "err_msg", "此筆刪除");
					llOk++;
				}
			}
		}
		// 新增批號或新增序號
		if (strAction.equals("S3")) {
			for (int rr = 0; rr < aaBatchNo.length; rr++) {					
				if (rr == 0)	continue; // 第1筆不用處理
				if (checkBoxOptOn(rr, opt)) {
					wp.colSet(rr, "ok_flag", "V");
					wp.colSet(rr, "set_opt", "checked");
					wp.colSet(rr, "err_msg", "此筆刪除");
					llOk++;
					continue;
				}
				if (ofValidation(rr) != 1) {
					llErr++;
					continue;
				}
				//
				func.varsSet("aa_batch_no", aaBatchNo[rr]);
				func.varsSet("aa_serial_no", aaSerialNo[rr]);
				func.varsSet("aa_p_seqno", gsPSeqno);
				func.varsSet("aa_acno_p_seqno", gsAcnoPSeqno);
				func.varsSet("aa_acct_type", aaAcctType[rr]);
				func.varsSet("aa_id_p_seqno", gsIdPSeqno);
				func.varsSet("aa_pay_card_no", gsCardNo);
				func.varsSet("aa_pay_amt", aaPayAmt[rr]);
				func.varsSet("aa_pay_date", aaPayDate[rr]);
				func.varsSet("aa_payment_type", aaPaymentType[rr]);
			//func.varsSet("aa_debit_item", aaDebitItem[rr]);
			//func.varsSet("aa_debt_key", aaDebtKey[rr]);
				func.varsSet("aa_payment_no", aaPaymentNo[rr]);
				rc = func.dbInsert();
				if (rc == 1) {
				//sql_commit(rc);
					wp.colSet(rr, "ok_flag", "V");
					llOk++;
				}
			}
		}
		if (strAction.equals("S4")) {
			for (int rr = 0; rr < aaBatchNo.length; rr++) {
				if (checkBoxOptOn(rr, opt)) {
					wp.colSet(rr, "ok_flag", "V");
					wp.colSet(rr, "set_opt", "checked"); 
					wp.colSet(rr, "err_msg", "此筆刪除");
					llOk++;
					continue;
				}
				if (ofValidation(rr) != 1) {
					llErr++;
					continue;
				}
				//
				func.varsSet("aa_batch_no", aaBatchNo[rr]);
				func.varsSet("aa_serial_no", aaSerialNo[rr]);
				func.varsSet("aa_p_seqno", gsPSeqno);
				func.varsSet("aa_acno_p_seqno", gsAcnoPSeqno);
				func.varsSet("aa_acct_type", aaAcctType[rr]);
				func.varsSet("aa_id_p_seqno", gsIdPSeqno);
				func.varsSet("aa_pay_card_no", gsCardNo);
				func.varsSet("aa_pay_amt", aaPayAmt[rr]);
				func.varsSet("aa_pay_date", aaPayDate[rr]);
				func.varsSet("aa_payment_type", aaPaymentType[rr]);
			//func.varsSet("aa_debit_item", aaDebitItem[rr]);
			//func.varsSet("aa_debt_key", aaDebtKey[rr]);
				func.varsSet("aa_payment_no", aaPaymentNo[rr]);
				rc = func.dbInsert();
				if (rc == 1) {
				//sql_commit(rc);
					wp.colSet(rr, "ok_flag", "V");
					llOk++;
				} else { //batch_no duplicated handle
			    dbRollback();
			    resetBatchNo();
			    wp.dispMesg = "新增批號已被使用，已重新給號，請確認後繼續執行";
			    return;
				}
			}
		}
		// 明細資料處理完畢先判斷成功筆數決定是否繼續處理其他table
		if (llOk == 0) {
			wp.dispMesg = "存檔處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr;
			return;
		}
		// Process act_pay_batch && Process act_batch_cntl
		lsSql = "select count(serial_no) as cnt, sum(pay_amt) as sumamt from act_pay_detail where batch_no = :is_batch";
		setString("is_batch", ssBatchNo);
		sqlSelect(lsSql);
		liCnt = sqlInt("cnt");
		ldSum = sqlNum("sumamt");

		if (strAction.equals("S4") && llOk > 0) { //若insert_act_pay_detail未發生重複，這邊再檢核 act_pay_batch 是否重複 
		  String lsSqlS4 = "select batch_no from act_pay_batch where batch_no = :is_batch";
		  setString("is_batch", ssBatchNo);
		  sqlSelect(lsSqlS4);
		  if (sqlRowNum>0) { //batch_no duplicated handle
			  dbRollback();
			  resetBatchNo();
			  wp.dispMesg = "新增批號已被使用，已重新給號，請確認後繼續執行";
			  return;
		  } 
		}

		if (liCnt <= 0) {
			//當detl 0 筆時,act_pay_batch 金額/筆數 值為0 //20200930
			func.varsSet("aa_batch_no", ssBatchNo);
			func.varsSet("aa_tot_cnt", intToStr(liCnt));
			func.varsSet("aa_tot_amt", numToStr(ldSum,"###"));
			rc = updateActPayBatch(ssBatchNo,liCnt,ldSum);
			sqlCommit(rc);
		} else {
			func.varsSet("aa_batch_no", ssBatchNo);
			func.varsSet("aa_tot_cnt", intToStr(liCnt));
			func.varsSet("aa_tot_amt", numToStr(ldSum,"###"));
			func.varsSet("aa_serial_no", ssSerialNo);
			
			//rc = func.update_Act_pay_batch();
			rc = updateActPayBatch(ssBatchNo,liCnt,ldSum);
			if (rc != 1) {
				func.varsSet("aa_batch_no", ssBatchNo);
				func.varsSet("aa_tot_cnt", intToStr(liCnt));
				func.varsSet("aa_tot_amt", numToStr(ldSum,"###") );
				//rc = func.insert_Act_pay_batch();
				rc = insertActPayBatch(ssBatchNo,liCnt,ldSum);
			}
		//sql_commit(rc);

			func.deleteActBatchCntl();
			//rc = func.insert_Act_batch_cntl();
			rc = insertActBatchCntl(ssBatchNo);
			sqlCommit(rc);
			wp.colSet("btn_set", "disabled style='background: lightgray;'");
		}

		if(strAction.equals("S3")){			
			wp.colSet("btnUpdate_disable", "disabled style='background: lightgray;'");
			wp.colSet("btnSelect_disable", "disabled style='background: lightgray;'");
			for (int ii =0;ii <aaBatchNo.length; ii++){
				wp.colSet(ii,"set_opt", "disabled style='display: none'");
			}			
		}
		if(strAction.equals("S4")){			
			wp.colSet("btnUpdate_disable", "disabled style='background: lightgray;'");
			for (int ii =0;ii <aaBatchNo.length; ii++){
				wp.colSet(ii,"set_opt", "disabled style='display: none'");
			}			
		}
		String msg = "存檔處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr;
		alertMsg(msg);
	}

	int ofValidation(int ii) throws Exception {
		String[] aaPaymentType = wp.itemBuff("payment_type");
	//String[] aaDebitItem = wp.itemBuff("debit_item");
	//String[] aaDebtKey = wp.itemBuff("debt_key");
		String[] aaPayCardNo = wp.itemBuff("pay_card_no");
		String[] aaPaymentNo = wp.itemBuff("payment_no");
		String[] aaAcctType = wp.itemBuff("acct_type");
		String[] aaAcctKey = wp.itemBuff("acct_key");
		String[] aaPayAmt = wp.itemBuff("pay_amt");
		String[] aaPayDate = wp.itemBuff("pay_date");
		String lsSql = "";
		String lsAcctType = "", lsAcctKey = "", lsCardNo = "", lsPSeqno = "", lsIdPSeqno = "", lsAcnoPSeqno = "";
		String msAcctType = "", msAcctKey = "", msCardNo = "", msPSeqno = "", msIdPSeqno = "", msAcnoPSeqno = "";
		String nsAcctType = "", nsAcctKey = "", nsCardNo = "", nsPSeqno = "", nsIdPSeqno = "", nsAcnoPSeqno = "";

		log("aa_acct_key:"+aaAcctKey[ii]);
		// 繳款卡號/繳款編號/帳戶帳號不可均為空白
	//if (empty(aaPaymentNo[ii]) && empty(aaAcctKey[ii]) && empty(aaPaymentNo[ii])) {
		if (empty(aaPayCardNo[ii]) && empty(aaAcctKey[ii]) && empty(aaPaymentNo[ii])) {
			wp.colSet(ii, "ok_flag", "X");
			wp.colSet(ii, "err_msg", "繳款卡號/繳款編號/帳戶帳號不可均為空白!");
			return -1;
		}

/***
		// 繳款方式 OTHR ==> 借方科目不可空白
		if (aaPaymentType[ii].equals("OTHR") && empty(aaDebitItem[ii])) {
			wp.colSet(ii, "ok_flag", "X");
			wp.colSet(ii, "err_msg", "其他方式繳款,借方科目不准為空白!");
			return -1;
		}
		// 借方科目
		if (!empty(aaDebitItem[ii])) {
			lsSql += " select ac_no,ac_full_name "
					+ " from gen_acct_m "
					+ " where ac_no =:ac_no ";
			setString("ac_no", aaDebitItem[ii]);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet(ii, "ok_flag", "X");
				wp.colSet(ii, "err_msg", "借方科目不存在!");
				return -1;
			}
			// 借方科目為2XXXXXX==>銷帳鍵值不可空白
			if(strMid(aaDebitItem[ii],0,1).equals("2")){
				if (empty(aaDebtKey[ii])) {
					wp.colSet(ii, "ok_flag", "X");
					wp.colSet(ii, "err_msg", "借方科目為2XXXXXX時，銷帳鍵值不可為空白！");
					return -1;
				}
			}			
		}
***/		
		// pay_card_no取帳戶資料
		if (!empty(aaPayCardNo[ii])) {
			lsSql = " select a.p_seqno, b.acct_key, b.acct_type, b.corp_p_seqno, b.id_p_seqno, b.acno_p_seqno  "
					+ " from	 crd_card a, act_acno b where a.p_seqno = b.acno_p_seqno "
					+ " and a.card_no = :cardno ";
			setString("cardno", aaPayCardNo[ii]);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet(ii, "ok_flag", "X");
				wp.colSet(ii, "err_msg", "卡號無效或不存在");
				return -1;
			} else {
				lsCardNo = aaPayCardNo[ii];
				lsPSeqno = sqlStr("p_seqno");
				lsAcctType = sqlStr("acct_type");
				lsAcctKey = sqlStr("acct_key");
				lsIdPSeqno = sqlStr("id_p_seqno");
				lsAcnoPSeqno = sqlStr("acno_p_seqno");
			}
		}
		// payment_no取帳戶資料
		if (!empty(aaPaymentNo[ii])) {
			lsSql = " select a.card_no, a.p_seqno, b.acct_key, b.acct_type, b.corp_p_seqno, b.id_p_seqno, b.acno_p_seqno "
					+ " from	 crd_card a, act_acno b where a.p_seqno = b.acno_p_seqno "
					+ " and b.payment_no =:payment_no ";
					//+ " and a.p_seqno = '00' || substr(:payment_no,5,8) ";		//20200714 改直接抓act_acno.payment_no
			setString("payment_no", aaPaymentNo[ii]);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet(ii, "ok_flag", "X");
				wp.colSet(ii, "err_msg", "繳款編號不正確");
				return -1;
			} else {
				msCardNo = sqlStr("card_no");
				msPSeqno = sqlStr("p_seqno");
				msAcctType = sqlStr("acct_type");
				msAcctKey = sqlStr("acct_key");
				msIdPSeqno = sqlStr("id_p_seqno");
				msAcnoPSeqno = sqlStr("acno_p_seqno");
			}
		}
		// acct_key & acct_type 取帳戶資料
		if (!empty(aaAcctType[ii]) && !empty(aaAcctKey[ii])) {
			lsSql = " select a.card_no, a.p_seqno, b.acct_key, b.acct_type, b.corp_p_seqno, b.id_p_seqno, b.acno_p_seqno  "
					+ " from crd_card a, act_acno b where a.p_seqno = b.acno_p_seqno "
					+ " and b.acct_type = :acct_type and b.acct_key =:acct_key ";
			setString("acct_type", aaAcctType[ii]);
			setString("acct_key", aaAcctKey[ii]);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet(ii, "ok_flag", "X");
				wp.colSet(ii, "err_msg", "帳戶帳號無效或不存在");
				return -1;
			} else {
				nsCardNo = sqlStr("card_no");
				nsPSeqno = sqlStr("p_seqno");
				nsAcctType = sqlStr("acct_type");
				nsAcctKey = sqlStr("acct_key");
				nsIdPSeqno = sqlStr("id_p_seqno");
				nsAcnoPSeqno = sqlStr("acno_p_seqno");
			}
		}
		// 三方資料比對
		String chkFlag = "Y";
		if (!empty(aaPayCardNo[ii]) && !empty(aaPaymentNo[ii]) && (!empty(aaAcctType[ii]) && !empty(aaAcctKey[ii]))) {
			gsCardNo = lsCardNo;
			gsPSeqno = lsPSeqno;
			gsAcctType = lsAcctType;
			gsAcctKey = lsAcctKey;
			gsIdPSeqno = lsIdPSeqno;
			gsAcnoPSeqno = lsAcnoPSeqno;

		//if (!ls_card_no.equals(ms_card_no))
		//	chk_flag = "N";
			if (!lsPSeqno.equals(msPSeqno))
				chkFlag = "N";
			if (!lsAcctType.equals(msAcctType))
				chkFlag = "N";
			if (!lsAcctKey.equals(msAcctKey))
				chkFlag = "N";
		//if (!ls_id_p_seqno.equals(ms_id_p_seqno))
		//	chk_flag = "N";
			if (!lsAcnoPSeqno.equals(msAcnoPSeqno))
				chkFlag = "N";		
		}

		if (!empty(aaPayCardNo[ii]) && empty(aaPaymentNo[ii]) && (!empty(aaAcctType[ii]) && !empty(aaAcctKey[ii]))) {
			gsCardNo = lsCardNo;
			gsPSeqno = lsPSeqno;
			gsAcctType = lsAcctType;
			gsAcctKey = lsAcctKey;
			gsIdPSeqno = lsIdPSeqno;
			gsAcnoPSeqno = lsAcnoPSeqno;
			
		}

		if (empty(aaPayCardNo[ii]) && !empty(aaPaymentNo[ii]) && (!empty(aaAcctType[ii]) && !empty(aaAcctKey[ii]))) {
			gsCardNo = "";
			gsPSeqno = msPSeqno;
			gsAcctType = msAcctType;
			gsAcctKey = msAcctKey;
			gsIdPSeqno = msIdPSeqno;
			gsAcnoPSeqno = msAcnoPSeqno;
		}

		if (empty(aaPayCardNo[ii]) && empty(aaPaymentNo[ii]) && (!empty(aaAcctType[ii]) && !empty(aaAcctKey[ii]))) {
			// gs_card_no = ns_card_no;
			gsCardNo = "";
			gsPSeqno = nsPSeqno;
			gsAcctType = nsAcctType;
			gsAcctKey = nsAcctKey;
			gsIdPSeqno = nsIdPSeqno;
			gsAcnoPSeqno = nsAcnoPSeqno;
		}

		if (chkFlag.equals("N")) {
			wp.colSet(ii, "ok_flag", "X");
			wp.colSet(ii, "err_msg", "卡號/繳款編號/帳戶帳號比對不符");
			return -1;
		}
		// 繳款金額
		if (toNum(aaPayAmt[ii]) <= 0) {
			wp.colSet(ii, "ok_flag", "X");
			wp.colSet(ii, "err_msg", "金額需大於０");
			return -1;
		}
		// 繳款日期
		if(empty(aaPayDate[ii])){
			wp.colSet(ii, "ok_flag", "X");
			wp.colSet(ii, "err_msg", "繳款日期不可空白");
			return -1;
		}		
		if (aaPayDate[ii].compareTo(getSysDate()) > 0) {
			wp.colSet(ii, "ok_flag", "X");
			wp.colSet(ii, "err_msg", "繳款日期不可大於系統日:"+getSysDate());
			return -1;
		}
		// 繳款類別
		if(empty(aaPaymentType[ii])){
			wp.colSet(ii, "ok_flag", "X");
			wp.colSet(ii, "err_msg", "繳款類別不可空白");
			return -1;
		}
		lsSql = "select payment_type from ptr_payment "
				+ "where payment_type =:payment_type ";
		setString("payment_type",aaPaymentType[ii]);
		sqlSelect(lsSql);
		if(empty(sqlStr("payment_type"))){
			wp.colSet(ii, "ok_flag", "X");
			wp.colSet(ii, "err_msg", "繳款類別不存在");
			return -1;
		}
		
		wp.colSet(ii, "err_msg", "");
		return 1;
	}

	@Override
	public void initButton() {
		// if (wp.respHtml.indexOf("_detl") > 0) {
		// this.btnMode_aud();
		// }
	}

	@Override
	public void dddwSelect() {
		try {
			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_batch_no");
			this.dddwList("dddw_batch_no", "act_pay_batch", "act_pay_batch.batch_no", "",
					"WHERE ( (substr(act_pay_batch.batch_no,9,1) like '0' OR substr(act_pay_batch.batch_no,9,1) like '1') ) AND ( act_pay_batch.confirm_user ='' ) ORDER BY act_pay_batch.batch_no ASC");
			// "WHERE ( (substr(act_pay_batch.batch_no,9,1) like '0' OR
			// substr(act_pay_batch.batch_no,9,1) like '1') ) AND (
			// act_pay_batch.confirm_date ='' ) ORDER BY act_pay_batch.batch_no
			// ASC");

/***
			//wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_bank_no");
			this.dddwList("dddw_bank_no", "ptr_bankcode", "bc_bankcode", "bc_abname",
					"WHERE bc_bankcode = '000' or bc_bankcode = '700' order by bc_bankcode");
***/
		} catch (Exception ex) {
		}
	}

	// 新增批號之預設批號計算
	void setBatchno() throws Exception {
		String exBankNo = wp.itemStr2("ex_bank_no");
		String wkBatchno = "", wkBusinessDate="";
		int tBatchno = 0;
		String lsSql = "select business_date from ptr_businday fetch first 1 rows only ";
		sqlSelect(lsSql);
		wkBusinessDate=sqlStr("business_date");
		
		lsSql = "select max(batch_no) batch_no from act_pay_batch where 1=1 ";
		wkBatchno = wkBusinessDate + strMid(exBankNo, 0, 4);
		lsSql += sqlCol(wkBatchno, "batch_no", "like%");
		sqlSelect(lsSql);
		if (!empty(sqlStr("batch_no"))) {
			tBatchno = Integer.parseInt(strMid(sqlStr("batch_no"), 12, 4)) + 1;
			wkBatchno = strMid(sqlStr("batch_no"), 0, 12) + (String.format("%04d", Integer.valueOf(tBatchno)));
		} else {
			wkBatchno = wkBatchno + "0001";
		}

/***
		if (exBankNo.equals("1001")) {
		//wkBatchno = getSysDate() + "000";
			wkBatchno = wkBusinessDate + "1001";
			lsSql += sqlCol(wkBatchno, "batch_no", "like%");
			sqlSelect(lsSql);
			if (!empty(sqlStr("batch_no"))) {
				tBatchno = Integer.parseInt(strMid(sqlStr("batch_no"), 12, 4)) + 1;
				wkBatchno = strMid(sqlStr("batch_no"), 0, 12) + (String.format("%04d", Integer.valueOf(tBatchno)));
			} else {
			//wkBatchno = getSysDate() + "00000001";
				wkBatchno = wkBatchno + "0001";
			}
		//wp.colSet("dis", "");
		//wp.colSet("ex_debit_item", "60000300");
		//wp.colSet("chk_debit_item", "臨時存欠--業務系統");
		}
		if (exBankNo.equals("700")) {
			wkBatchno = getSysDate() + "0700";
			lsSql += sqlCol(wkBatchno, "batch_no", "like%");
			sqlSelect(lsSql);
			if (!empty(sqlStr("batch_no"))) {
				tBatchno = Integer.parseInt(strMid(sqlStr("batch_no"), 12, 4)) + 1;
				wkBatchno = strMid(sqlStr("batch_no"), 0, 12) + (String.format("%04d", Integer.valueOf(tBatchno)));
			} else {
				wkBatchno = getSysDate() + "07000001";
			}
			// 700時 借方科目 及 銷帳鍵值設為disabled
			wp.colSet("dis", "disabled style='background: lightgray;'");
			wp.colSet("ex_debit_item", "");
			wp.colSet("ex_debit_key", "");
		}
***/
		wp.colSet("ex_batchno", wkBatchno);
	}

	// 新增批號之預設批號存檔時發生重複，重新要號
	void resetBatchNo() throws Exception {
		String exBankNo = wp.itemStr2("ex_bank_no");
		String wkBatchno = "", wkBusinessDate="";
		int tBatchno = 0;
		String lsSql = "select business_date from ptr_businday fetch first 1 rows only ";
		sqlSelect(lsSql);
		wkBusinessDate=sqlStr("business_date");
		
		lsSql = "select max(batch_no) batch_no from act_pay_batch where 1=1 ";
		wkBatchno = wkBusinessDate + strMid(exBankNo, 0, 4);
		lsSql += sqlCol(wkBatchno, "batch_no", "like%");
		sqlSelect(lsSql);
		if (!empty(sqlStr("batch_no"))) {
			tBatchno = Integer.parseInt(strMid(sqlStr("batch_no"), 12, 4)) + 1;
			wkBatchno = strMid(sqlStr("batch_no"), 0, 12) + (String.format("%04d", Integer.valueOf(tBatchno)));
		} else {
			wkBatchno = wkBatchno + "0001";
		}

/***
		if (exBankNo.equals("000")) {
			wkBatchno = getSysDate() + "000";
			lsSql += sqlCol(wkBatchno, "batch_no", "like%");
			sqlSelect(lsSql);
			if (!empty(sqlStr("batch_no"))) {
				tBatchno = Integer.parseInt(strMid(sqlStr("batch_no"), 12, 4)) + 1;
				wkBatchno = strMid(sqlStr("batch_no"), 0, 12) + (String.format("%04d", Integer.valueOf(tBatchno)));
			} else {
				wkBatchno = getSysDate() + "00000001";
			}
			wp.colSet("dis", "");
			wp.colSet("ex_debit_item", "60000300");
			wp.colSet("chk_debit_item", "臨時存欠--業務系統");
		}
		if (exBankNo.equals("700")) {
			wkBatchno = getSysDate() + "0700";
			lsSql += sqlCol(wkBatchno, "batch_no", "like%");
			sqlSelect(lsSql);
			if (!empty(sqlStr("batch_no"))) {
				tBatchno = Integer.parseInt(strMid(sqlStr("batch_no"), 12, 4)) + 1;
				wkBatchno = strMid(sqlStr("batch_no"), 0, 12) + (String.format("%04d", Integer.valueOf(tBatchno)));
			} else {
				wkBatchno = getSysDate() + "07000001";
			}
			// 700時 借方科目 及 銷帳鍵值設為disabled
			wp.colSet("dis", "disabled style='background: lightgray;'");
			wp.colSet("ex_debit_item", "");
			wp.colSet("ex_debit_key", "");
		}
***/
		wp.colSet("ex_batchno", wkBatchno);
		
		String[] aaBatchNo = wp.itemBuff("batch_no");
		for (int rr = 0; rr < aaBatchNo.length; rr++) {
				wp.colSet(rr, "batch_no", wp.colStr("ex_batchno"));
				wp.colSet(rr, "ok_flag", "");
				continue;
		}

	}

	public int wfCheckConfirm(String batchNo) throws Exception {
		String lsSql = "";
		// 原舊系統抓confirm_user但值多為空白
		lsSql = "select confirm_user from act_pay_batch where batch_no =:batch_no ";
		setString("batch_no", batchNo);
		sqlSelect(lsSql);
		if (!empty(sqlStr("confirm_user"))) {
			return -1;
		}
		return 1;
	}

	public void wfChkCol(TarokoCommon wr) throws Exception {
		super.wp = wr;
		String textData1 = wp.itemStr2("text_data1");
		String textData2 = wp.itemStr2("text_data2");
		String textData3 = wp.itemStr2("text_data3");
		String lsSql = "";
//	System.out.println("1:"+text_data1+" 2:"+text_data2+" 3:"+text_data3+" 4:"+row_index+" 5:"+serno);
		switch (textData1) {
		// 繳款卡號檢核
		case "pay_card_no":
			lsSql += " select a.p_seqno, b.acct_key, b.acct_type, b.corp_p_seqno, b.id_p_seqno, "
					+ " decode(b.acno_flag,'2',d.chi_name,c.chi_name) cname, "
					+ " b.id_p_seqno,"
					+ " b.p_seqno,"
					+ " b.acno_p_seqno "
					+ " from crd_card a, act_acno b "
					+ " left join crd_idno c on c.id_p_seqno = b.id_p_seqno "
					+ " left join crd_corp d on d.corp_p_seqno = b.corp_p_seqno "
					+ " where a.p_seqno = b.acno_p_seqno "
					+ " and a.card_no = :cardno ";
			setString("cardno", textData2);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet("data_msg", "卡號無效或不存在");
				wp.addJSON("data_msg", "卡號無效或不存在");
				wp.addJSON("rcname", "");
				wp.addJSON("rid_p_seqno", "");
				wp.addJSON("racno_p_seqno", "");
				wp.addJSON("rp_seqno", "");
				wp.colSet("chk_flag", "err");
				wp.addJSON("chk_flag", "err");
			} else {
				wp.colSet("chk_flag", "OK");
				wp.addJSON("chk_flag", "OK");				
				wp.addJSON("rcname", sqlStr("cname"));
				wp.addJSON("rid_p_seqno", sqlStr("id_p_seqno"));
				wp.addJSON("racno_p_seqno", sqlStr("acno_p_seqno"));
				wp.addJSON("rp_seqno", sqlStr("p_seqno"));
				wp.colSet("racct_type", sqlStr("acct_type"));
				wp.addJSON("racct_type", sqlStr("acct_type"));
				wp.colSet("racct_key", sqlStr("acct_key"));
				wp.addJSON("racct_key", sqlStr("acct_key"));
			}
			break;
		// 繳款編號檢核
		case "payment_no":
			lsSql += " select b.p_seqno, b.acct_key, b.acct_type, b.corp_p_seqno, b.id_p_seqno, "
					+ " decode(b.acno_flag,'2',d.chi_name,c.chi_name) cname, "
					+ " b.id_p_seqno,"
					+ " b.p_seqno,"
					+ " b.acno_p_seqno "
					+ " from act_acno b "
					+ " left join crd_idno c on c.id_p_seqno = b.id_p_seqno "
					+ " left join crd_corp d on d.corp_p_seqno = b.corp_p_seqno "
					+ " where b.p_seqno = b.acno_p_seqno "
					+ " and b.payment_no =:payment_no ";
			setString("payment_no", textData2);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet("data_msg", "資料錯誤--繳款編號不正確。");
				wp.addJSON("data_msg", "資料錯誤--繳款編號不正確。");
				wp.addJSON("rcname", "");
				wp.addJSON("rid_p_seqno", "");
				wp.addJSON("racno_p_seqno", "");
				wp.addJSON("rp_seqno", "");
				wp.colSet("chk_flag", "err");
				wp.addJSON("chk_flag", "err");
			} else {
				wp.colSet("chk_flag", "OK");
				wp.addJSON("chk_flag", "OK");				
				wp.addJSON("rcname", sqlStr("cname"));
				wp.addJSON("rid_p_seqno", sqlStr("id_p_seqno"));
				wp.addJSON("racno_p_seqno", sqlStr("acno_p_seqno"));
				wp.addJSON("rp_seqno", sqlStr("p_seqno"));
				wp.colSet("racct_type", sqlStr("acct_type"));
				wp.addJSON("racct_type", sqlStr("acct_type"));
				wp.colSet("racct_key", sqlStr("acct_key"));
				wp.addJSON("racct_key", sqlStr("acct_key"));
			}

			break;
		// 帳戶帳號檢核
		case "acct_key":
			log("acct_key:"+textData2);
			lsSql += "select b.acct_type,b.acct_key, "
					+ " decode(b.acno_flag,'2',d.chi_name,c.chi_name) cname, "
					+ " b.id_p_seqno,"
					+ " b.p_seqno,"
					+ " b.acno_p_seqno "
					+ " from act_acno b "
					+ " left join crd_idno c on c.id_p_seqno = b.id_p_seqno "
					+ " left join crd_corp d on d.corp_p_seqno = b.corp_p_seqno "
					+ " where b.p_seqno = b.acno_p_seqno "
					+ "   and b.acct_type =:acct_type "
					+ "   and b.acct_key =:acct_key ";
			setString("acct_type", textData3);
			setString("acct_key", textData2);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet("data_msg", "資料錯誤--帳戶帳號無效或不存在。");
				wp.addJSON("data_msg", "資料錯誤--帳戶帳號無效或不存在。");
				wp.addJSON("rcname", "");
				wp.addJSON("rid_p_seqno", "");
				wp.addJSON("racno_p_seqno", "");
				wp.addJSON("rp_seqno", "");
				wp.colSet("chk_flag", "err");
				wp.addJSON("chk_flag", "err");
			} else {
				log("rcname :"+sqlStr("cname"));
				log("id_p_seqno :"+sqlStr("id_p_seqno"));
				log("acno_p_seqno :"+sqlStr("acno_p_seqno"));
				log("p_seqno :"+sqlStr("p_seqno"));
				wp.colSet("chk_flag", "OK");
				wp.addJSON("chk_flag", "OK");				
				wp.addJSON("rcname", sqlStr("cname"));
				wp.addJSON("rid_p_seqno", sqlStr("id_p_seqno"));
				wp.addJSON("racno_p_seqno", sqlStr("acno_p_seqno"));
				wp.addJSON("rp_seqno", sqlStr("p_seqno"));
				wp.colSet("racct_type", sqlStr("acct_type"));
				wp.addJSON("racct_type", sqlStr("acct_type"));
				wp.colSet("racct_key", sqlStr("acct_key"));
				wp.addJSON("racct_key", sqlStr("acct_key"));
			}
			break;
/***
		case "debit_item":
			if(empty(textData2)){
				wp.colSet("chk_debit_item","");
				wp.addJSON("chk_debit_item", "");
				wp.colSet("chk_flag", "OK");
				wp.addJSON("chk_flag", "OK");
				break;
			}
			lsSql += " select ac_no,ac_full_name "
					+ " from gen_acct_m "
					+ " where ac_no =:ac_no ";
			setString("ac_no", textData2);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet("data_msg", "借方科目不存在");
				wp.addJSON("data_msg", "借方科目不存在");
				wp.colSet("chk_debit_item", "借方科目不存在");
				wp.addJSON("chk_debit_item", "借方科目不存在");
				wp.colSet("chk_flag", "err");
				wp.addJSON("chk_flag", "err");
			} else {
				wp.colSet("chk_debit_item", sqlStr("ac_full_name"));
				wp.addJSON("chk_debit_item", sqlStr("ac_full_name"));
				wp.colSet("chk_flag", "OK");
				wp.addJSON("chk_flag", "OK");
			}
			break;
***/
		case "payment_type":
			lsSql = "select payment_type from ptr_payment "
					+ "where payment_type =:payment_type ";
			setString("payment_type",textData2);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet("data_msg", "繳款類別不存在");
				wp.addJSON("data_msg", "繳款類別不存在");
				wp.colSet("chk_flag", "err");
				wp.addJSON("chk_flag", "err");
			} else {
				wp.colSet("chk_flag", "OK");
				wp.addJSON("chk_flag", "OK");
			}			
		}
		wp.colSet("text_data1", textData1);
		wp.colSet("text_data2", textData2);
		wp.colSet("text_data3", textData3);
		wp.addJSON("text_data1", textData1);
		wp.addJSON("text_data2", textData2);
		wp.addJSON("text_data3", textData3);
		//新增批號第2筆資料的index要+1
//		if(text_data1.equals("acct_key")){
//			if(!serno.equals("01")){
//				row_index = int_2Str(Integer.parseInt(row_index) + 1);
//			} 
//		} 
//		wp.col_set("row_index", row_index);
//		wp.addJSON("row_index", row_index);

	}
	int insertActPayBatch(String aaBatchNo,int aaTotCnt,double aaTotAmt) throws Exception{	
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String sysDate = df.format(new Date());
		df = new SimpleDateFormat("HHmmss");
		String sysTime = df.format(new Date());
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_pay_batch");
		sp.ppstr("batch_no",aaBatchNo);
		sp.ppnum("batch_tot_cnt",aaTotCnt);
		sp.ppnum("batch_tot_amt",aaTotAmt);
		sp.ppstr("curr_code","901");  
		sp.ppstr("crt_date",getSysDate());
		sp.ppstr("crt_time",sysTime);
		sp.ppstr("crt_user",wp.loginUser);
		sp.addsql(", mod_time ",", sysdate ");
		sp.ppstr("mod_user",wp.loginUser);
		sp.ppstr("mod_pgm",wp.modPgm());
		sp.ppstr("mod_seqno","1");
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = 0;
		} else { 
			rc = 1;
		}
		return rc;
	}
	int updateActPayBatch(String aaBatchNo,int aaTotCnt,double aaTotAmt) throws Exception {		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_pay_batch");
		sp.ppnum("batch_tot_cnt", aaTotCnt);
		sp.ppnum("batch_tot_amt", aaTotAmt);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where batch_no=?", aaBatchNo);
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
		}else {
			rc = 1;
		}
		return rc;
	}
	int insertActBatchCntl(String aaBatchNo) throws Exception{
		String lsBatchNo="",lsCtrlDate = "", lsBranch = "", lsBatch = "", lsSerial = "";
		lsBatchNo = aaBatchNo;
		lsCtrlDate = strMid(aaBatchNo,0,8);
        lsBranch = strMid(aaBatchNo,8,4);
        lsBatch = strMid(aaBatchNo,12,4);
        //選取指定批號之最大序號
        String lsSql = "select max(serial_no) max_serial_no from act_pay_detail "
        		+ "where batch_no =:ls_batch_no";
        setString("ls_batch_no",lsBatchNo);
        sqlSelect(lsSql);        
        lsSerial = sqlStr("max_serial_no");

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_batch_cntl");
		sp.ppstr("ctrl_date",lsCtrlDate);
		sp.ppstr("branch",lsBranch);  
		sp.ppstr("batch",lsBatch);
		sp.ppstr("serial",lsSerial);
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
		} else {
			rc = 1;
		}
		
		return rc;
	}
}
