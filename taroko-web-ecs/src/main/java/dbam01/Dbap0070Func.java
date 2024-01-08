/*****************************************************************************
 * * MODIFICATION LOG * * DATE Version AUTHOR DESCRIPTION * --------- -------- ----------
 *109-06-01 V1.00.00 yanghan Initial
 *109-06-11  V1.00.01  Zuwei  update dba_debt MOD_TIME,MOD_PGM,MOD_USER 
 ******************************************************************************/

package dbam01;

import java.io.FileInputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import com.tcb.ap4.tool.Decryptor;

import Dxc.Util.SecurityUtil;
import busi.FuncEdit;
import busi.SqlPrepare;
import bank.Auth.HpeUtil;
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;
import net.sf.json.JSONObject;

public class Dbap0070Func extends FuncEdit {
	String rowid = "";
	String table = "";
	String deductDate = "";
	String deductSeq = "";
	String isUrlToken = "" , isUrlTxn = "";
	String isUserName1 = "" , isUserName2 = "" , isUserPd1 = "" , isUserPd2 = "";
	String isApplJson1 = "" , isApplJson2 = "" , isToken = "";
	byte[] rowidBite = null;
	public boolean isReponse = false;
	public String queryDesc = "";
	taroko.base.CommDate commDate = new taroko.base.CommDate();
	JSONObject jasonToString = new JSONObject();
	boolean ibDebug = false ;
	public Dbap0070Func(TarokoCommon wr) {
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
		rowidBite = wp.itemRowId("rowid");

		// -other modify-
		sqlWhere = "where rowid = ? " + "and nvl(mod_seqno,0) = ? ";
		Object[] param = new Object[] { rowidBite, wp.modSeqno() };
		isOtherModify("dba_deduct_txn", sqlWhere, param);
	}

	@Override
	public int dbInsert() {
		return 0;
	}

	// 查询 ptr_businday 的 business_date
	public String selectdate() {
		String lsSql = "select business_date from ptr_businday";
		Object[] param = new Object[] {};
		sqlSelect(lsSql, param);
		String businessDate = colStr("business_date");
		if (businessDate != null) {
			return businessDate;
		} else {
			return "1";
		}
	}

	// 查询 最新的 DeductSeq
	public String selectDeductSeq() {
		String lsSql = "select dba_txnseq.nextval as dba_txnseq  from dual";
		Object[] param = new Object[] {};
		sqlSelect(lsSql, param);
		String deductSeq = colStr("dba_txnseq");
		if (deductSeq == null) {// 判空
			return "1";
		} else {
			String deductSeq1 = "";
			for (int i = 0; i < 10 - deductSeq.length(); i++) {
				deductSeq1 = deductSeq1 + 0;
			}
			deductSeq = deductSeq1 + deductSeq;
			return deductSeq;
		}
	}

	// 扣款金額 >0 且 :扣款金額 <= 目前期末餘額 才可新增
	public int checkBeforeInsert() {
		if (Integer.parseInt(wp.itemStr("deduct_amt")) > 0
				&& Integer.parseInt(wp.itemStr("deduct_amt")) <= Integer.parseInt(wp.itemStr("ex_bef_amt"))) {
			return 1;
		} else {
			return 0;
		}
	}

	public void toDo() throws Exception {
//		ibDebug = true;
		// 本方法功能為：通過scokt 發送並獲取電文		
		getImsParm();				        		
		HpeUtil hpeUtil = new HpeUtil();
		JSONObject jsonObjectSent = new JSONObject();
		// --取 Token		
		String seqNo = "";
		String lsSendData = "" , lsOriSendData = "" , backSeqNo = "";
		String lsReceiveData = "";
		String lsReponseCode = "";
		String lsTempReceiveData = "";
		String sql1 = "select seq_send_ibmseqno.nextval as seqno from dual ";
		sqlSelect(sql1);
		seqNo = commString.lpad(colStr("seqno"), 6, "0");
		lsSendData += " 0200VDADATM ";
//		lsSendData += "8B00IBPBMCRD 0200VDADATM ";
		lsSendData += "CRD0" + seqNo; // --0000+送JSON Seqno
		lsSendData += seqNo; // --送JSON Seqno
		lsSendData += getSysDate();
		lsSendData += commDate.sysTime();
		lsSendData += "9CLIENTID1     ";

		// --上行電文 Body 143
		lsSendData += "VA";
		lsSendData += commString.rpad(wp.itemStr("card_no") + "=" + commString.mid(wp.itemStr("new_end_date"), 2, 4), 37); // --卡號=new_end_date(MMYY)
		if(eqIgno(wp.buttonCode,"U")) {
			lsSendData += commString.lpad(wp.itemStr("deduct_amt"), 10,"0");
			lsSendData += commString.lpad(wp.itemStr("deduct_amt"), 10,"0");
		} else if(eqIgno(wp.buttonCode,"U1")) {
			lsSendData += commString.lpad("0", 10,"0");
			lsSendData += commString.lpad(wp.itemStr("deduct_amt"), 10,"0");
		}		
		lsSendData += commString.rpad("0", 10,"0");
		lsSendData += commString.rpad(wp.itemStr("acct_no"), 13, " "); // --acct_no 目前為 11 碼 , 但格式為13碼 後補2空白
																		// 未來可能擴充欄位至13碼
		lsSendData += commString.rpad(wp.itemStr("card_ref_num"), 2, " ");
//		lsSendData += "02"; // --目前沒有先固定放 02
		lsSendData += commString.rpad("CRD0"+commString.mid(wp.itemStr("tx_seq"), 4), 10); // --圈存序號
//		lsSendData += commString.rpad(wp.itemStr("tx_seq"), 10, " "); // --圈存序號
		lsSendData += commString.rpad(wp.itemStr("trace_no"), 6); // --trace_no
		lsSendData += wp.itemStr("tx_time"); // --交易時間 HHMMSS
		lsSendData += commString.mid(wp.itemStr("tx_date"), 4, 4); // --交易日期 MMDD
		lsSendData += commString.rpad(wp.itemStr("ref_no"), 12); // --ref_no
		lsSendData += commString.rpad(wp.itemStr("v_card_no"), 20); // --v_card_no
		lsSendData += " "; // --保留
		lsOriSendData = lsSendData.trim() ;
		if(ibDebug)	{
			isReponse = false ; 
			return ;
		}
		
		insertImsLog(lsOriSendData,"","CRD0"+seqNo);
		if(rc!=1) {
			isReponse = false ; 
			return ;
		}
		
		lsSendData = hpeUtil.encoded2Base64(hpeUtil.transByCode(lsSendData, "Cp1047"));
	    jsonObjectSent.put("message", lsSendData);
	    jsonObjectSent.put("seqNo", "0200VDADATM CRD0"+seqNo);
	    lsSendData = jsonObjectSent.toString();
	    log(lsSendData);
	    lsTempReceiveData = hpeUtil.curlToken(isUrlTxn, isApplJson2, isToken, lsSendData);
	    if(empty(lsTempReceiveData)) {
	    	isReponse = false ;	 
	    	updateCcaImsLog("CRD0"+seqNo,"","Y");
	    }
	    
	    lsTempReceiveData = parsingJson(lsTempReceiveData,2);
	    lsReceiveData = hpeUtil.ebcdic2Str(hpeUtil.decodedString2(lsTempReceiveData));
	    backSeqNo = commString.mid(lsReceiveData, 21, 10);
	    if (backSeqNo.equals("CRD0" + seqNo) == false) {
	    	isReponse = false ;	   
	    	updateCcaImsLog("CRD0"+seqNo,"","Y");
		}
		lsReponseCode = commString.mid(lsReceiveData, 61, 4);

		if (lsReponseCode.equals("0000")) {
			isReponse = true;
			updateCcaImsLog("CRD0"+seqNo,lsReponseCode,"");
		}	else {
			isReponse = false;
			updateCcaImsLog("CRD0"+seqNo,lsReponseCode,"");
		}			
		return;
	}
	
	int updateCcaImsLog(String lsSeqNo , String lsRespCode , String lsProcCode)	{
		
		String sql1 = "";
		sql1 = " update cca_ims_log set ims_resp_code = ? , proc_code = ? where ims_seq_no = ? and tx_date = to_char(sysdate,'yyyymmdd') ";
		setString(1,lsRespCode);
		setString(2,lsProcCode);
		setString(3,lsSeqNo);				
		sqlExec(sql1);
		
		return rc;
	}
	
	int insertImsLog(String sendData, String respCode , String imsSeqNo) {
		String sql1 = "";
		sql1 = "insert into cca_ims_log (" + " tx_date ," + " tx_time ," + " card_no ," + " auth_no ," + " trans_type ,"
				+ " card_acct_idx ," + " acno_p_seqno ," + " ims_seq_no ," + " ims_reversal_data ," + " trans_amt ,"
				+ " ims_resp_code ," + " iso_resp_code ," + " crt_date ," + " crt_user ," + " send_date ,"
				+ " proc_code ," + " mod_time ," + " mod_pgm " + ") values ( " + " to_char(sysdate,'yyyymmdd') ,"
				+ " to_char(sysdate,'hh24miss') ," + " :card_no ," + " :auth_no ," + " '' ,"
				+ " :card_acct_idx ," + " :acno_p_seqno ," + " :ims_seq_no ," + " :ims_reversal_data ,"
				+ " :trans_amt ," + " :ims_resp_code ," + " :iso_resp_code ," + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ,"
				+ " to_char(sysdate,'yyyymmdd') ," + " '' ," + " sysdate ," + " 'Dbap0070' " + " )";
		
		setString("card_no",wp.itemStr("card_no"));
		setString("auth_no",wp.itemStr("auth_no"));
		setDouble("card_acct_idx",wp.itemNum("card_acct_idx"));
		setString("acno_p_seqno",wp.itemStr("acno_p_seqno"));
		setString("ims_seq_no",imsSeqNo);
		setString("ims_reversal_data",sendData);
		setDouble("trans_amt",wp.itemNum("deduct_amt"));
		setString("ims_resp_code",respCode);
		setString("iso_resp_code","00");		
		setString("crt_user",wp.loginUser);
		
		sqlExec(sql1);
		if(rc!=1) {
			errmsg("insert IMS log 錯誤");
			return rc;
		}
		
		return rc;
	}
	
	@Override
	public int dbUpdate() {
		dateTime();
		deductDate = selectdate();
		deductSeq = selectDeductSeq();
		if (deductSeq.equals(1)) {// 若返回1則說明為獲取到 dba_txnseq
			return rc;
		}
		actionInit("U");
		if (rc != 1) {
			return rc;
		}

		try {
			toDo();
		} catch (Exception e) {
		}
		
		if(rc!=1)	return rc;
		
		if (isReponse) {// 根據回復碼判斷本操作是否成功 若成功新增记录并修改相应数据，若不成功则只新增 dba_deduct_txn 留作记录 其他不修改
			if (checkBeforeInsert() == 1) {// 新增之前需要检验 rc =
				insertDeductTxn("00", "01");
				if (rc != 1) {
					return rc;
				}
			}

			rc = insertDbaJrnl();// 新增 DbaJrnl
			if (rc != 1) {
				errmsg("線上扣款失敗");
				return rc;
			}

			rc = updateDbaDebt(); // 修改 DbaDebt
			if (rc != 1) {
				errmsg("線上扣款失敗");
				return rc;
			}
			wp.respMesg = "線上扣款成功";
		} else {// 電文失敗 只新增 dba_deduct_txn
			if (checkBeforeInsert() == 1) {// 新增之前需要检验
				rc = insertDeductTxn("01", "10");
				if (rc != 1) {
					errmsg("線上扣款失敗");
					return rc;
				}
			}
		}
		return rc;
	}

	@Override
	public int dbDelete() {
		return 0;
	}

	// 新增DeductTxn
	public int insertDeductTxn(String lsProcCode, String lsProcType) {
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HHmmss");
		LocalDateTime dt = LocalDateTime.now();
		actionInit("A");
		busi.SqlPrepare sp = new SqlPrepare();
		String idNo = ((wp.itemStr("corp_no") == null) ? wp.itemStr("id_no") : wp.itemStr("corp_no"));
		sp.sql2Insert("dba_deduct_txn");
		sp.ppstr("tx_seq", wp.itemStr("tx_seq"));
		sp.ppstr("deduct_seq", deductSeq);
		sp.ppstr("crt_date", deductDate);
		// sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("abstract_code", "VDPY");
		sp.ppstr("deduct_date", deductDate);
		sp.ppstr("reference_no", wp.itemStr("reference_no"));
		sp.ppstr("acct_no", wp.itemStr("acct_no"));
		sp.ppstr("p_seqno", wp.itemStr("p_seqno"));
		sp.ppstr("acct_type", wp.itemStr("acct_type"));
		sp.ppstr("id_p_seqno", wp.itemStr("id_p_seqno"));
		sp.ppstr("id_no", idNo);
		// sp.addsql(", ", ", decode(+" + wp.itemStr("corp_no") + ",null," +
		// wp.itemStr("id_no") + ","+
		// wp.itemStr("corp_no") + ") ");
		sp.ppstr("id_no_code", wp.itemStr("id_no_code"));
		sp.ppstr("card_no", wp.itemStr("card_no"));
		sp.ppstr("bank_actno", wp.itemStr("bank_actno"));
		sp.ppstr("acct_code", wp.itemStr("acct_code"));
		sp.ppstr("acct_item_cname", "");
		sp.ppstr("merchant_no", wp.itemStr("mcht_no"));
		sp.ppstr("transaction_code", wp.itemStr("txn_code"));
		sp.ppstr("item_post_date", wp.itemStr("item_post_date"));
		sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
		sp.ppstr("beg_bal", wp.itemStr("hi_beg_bal"));
		sp.ppstr("end_bal", wp.itemStr("aft_amt"));
		sp.ppstr("d_available_bal", wp.itemStr("d_avail_bal"));
		sp.ppstr("debt_status", wp.itemStr("debt_status"));
		sp.ppstr("from_code", "3");
		sp.ppstr("stmt_cycle", wp.itemStr("stmt_cycle"));
		sp.ppstr("org_reserve_amt", wp.itemStr("org_reserve_amt"));
		sp.ppstr("reserve_amt", wp.itemStr("reserve_amt"));
		sp.ppstr("org_deduct_amt", wp.itemStr("deduct_amt"));
		sp.ppstr("deduct_amt", wp.itemStr("deduct_amt"));
		sp.ppstr("trans_col_date", wp.itemStr("trans_col_date"));
		sp.ppstr("trans_bad_date", wp.itemStr("trans_bad_date")); // deduct_proc_date
		sp.ppstr("deduct_proc_date", date.format(formatter));
		sp.ppstr("deduct_proc_time", time.format(formatter1));
		sp.ppstr("deduct_proc_type", lsProcType);
		sp.ppstr("deduct_proc_code", lsProcCode);
		sp.addsql(", mod_time ", ", sysdate ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", "dbap0070");
		// System.out.println("sql語句"+sp.sqlStmt());
		// System.out.println("sql變量"+sp.sqlParm());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
			rc = -1;
			return rc;
		} else {
			rc = 1;
		}
		return rc;
	}

	// 新增DbaJrnl
	public int insertDbaJrnl() {
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HHmmss");
		LocalDateTime dt = LocalDateTime.now();
		/*
		 * sp.ppstr("crt_date",date.format(formatter));
		 * sp.ppstr("crt_time",time.format(formatter1));
		 */
		/*
		 * 程序说明书上有这两列但是 表中并无此两列 sp.ppstr("id", wp.itemStr("id_no"));
		 * sp.ppstr("id_no_code", wp.itemStr("id_no_code"));
		 */
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("dba_jrnl");
		sp.ppstr("p_seqno", wp.itemStr("p_seqno"));
		sp.ppstr("crt_date", deductDate);
		// sp.addsql(", crt_date ", ",select business_date from ptr_businday");
		sp.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
		/* sp.ppstr("crt_time", time.format(formatter1)); */
		sp.ppstr("acct_type", wp.itemStr("acct_type"));
		sp.ppstr("acct_no", wp.itemStr("acct_no"));
		sp.ppstr("id_p_seqno", wp.itemStr("id_p_seqno"));
		sp.ppstr("deduct_seq", deductSeq);
		sp.ppstr("stmt_cycle", wp.itemStr("stmt_cycle"));
		sp.ppstr("debt_status", wp.itemStr("debt_status"));
		sp.ppstr("trans_col_date", wp.itemStr("trans_col_date"));
		sp.ppstr("trans_bad_date", wp.itemStr("trans_bad_date"));
		sp.ppstr("acct_code", wp.itemStr("acct_code"));
		sp.ppstr("acct_date", deductDate);
		// sp.addsql(", acct_date ", ", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("tran_class", "D");
		sp.ppstr("tran_type", "VD");
		sp.ppstr("dr_cr", "D");
		sp.ppstr("transaction_amt", wp.itemStr("deduct_amt"));
		sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
		sp.ppstr("item_post_date", wp.itemStr("item_post_date"));
		sp.ppstr("item_date", deductDate);
		// sp.addsql(", item_date ", ", to_char(sysdate,'yyyymmdd') ");
		// sp.ppstr("item_date", date.format(formatter));
		sp.ppstr("reference_no", wp.itemStr("reference_no"));
		sp.ppstr("card_no", wp.itemStr("card_no"));
		sp.ppstr("pay_id", wp.itemStr("card_no"));
		int bal = Integer.parseInt(wp.itemStr("bef_amt")) - Integer.parseInt(wp.itemStr("deduct_amt"));
		sp.ppstr("jrnl_bal", String.valueOf(bal));
		sp.ppstr("item_bal", String.valueOf(bal));
		sp.ppstr("item_d_bal", wp.itemStr("d_avail_bal"));
		sp.addsql(", mod_time ", ", sysdate ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", "dbap0070");
		// sp.ppstr("mod_time", String.valueOf(dt));

		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
			errmsg(this.sqlErrtext);
		} else {
			rc = 1;
		}
		return rc;
	}

	String parsingJson(String fromObject, int infoType) {
		String lsReponseData = "";
		jasonToString = JSONObject.fromObject(fromObject);
		// --1:Token , 2:Txn
		if (infoType == 1) {
			lsReponseData = jasonToString.getString("token");
		} else if (infoType == 2) {
			lsReponseData = jasonToString.getString("message");
		} else if (infoType == 3) {
			lsReponseData = jasonToString.getString("seqNo");
		}

		return lsReponseData;
	}

	void getImsParm() throws Exception {
		String sql1 = "" , sql2 = "" , sql3 = "" , lsTokenDate = "" , updateSql = "";
		sql1 = " select wf_value , wf_value2 , wf_value3 , wf_value4 from ptr_sys_parm "
				+ " where wf_parm = 'IMS_TOKEN' and wf_key = 'GET_TOKEN' ";

		sql2 = " select wf_value , wf_value2 , wf_value3 , wf_value4 from ptr_sys_parm "
				+ " where wf_parm = 'IMS_TXN' and wf_key = 'TXN' ";
		
		sql3 = " select wf_value , wf_value2||wf_value3||wf_value4 as token from ptr_sys_parm where wf_parm = 'IMS_TOKEN' and wf_key = 'TOKEN' ";
		
		sqlSelect(sql1);
		if (sqlRowNum > 0) {
			isUrlToken = colStr("wf_value");
			isApplJson1 = colStr("wf_value2");
			isUserName1 = colStr("wf_value3");
//			isUserPd1 = colStr("wf_value4"); 改從檔案中讀取
		}

		sqlSelect(sql2);
		if (sqlRowNum > 0) {
			isUrlTxn = colStr("wf_value");
			isApplJson2 = colStr("wf_value2");
			isUserName2 = colStr("wf_value3");			
		}
		
		sqlSelect(sql3);
		if(sqlRowNum>0) {
			lsTokenDate = colStr("wf_value");
			isToken = colStr("token");
		}
		
		if(eqIgno(lsTokenDate,getSysDate())==false) {
			//--從檔案中取密碼			
			String confFile = wp.getEcsAcdpPath();
			confFile = Normalizer.normalize(confFile, Normalizer.Form.NFKC);
			Properties props = new Properties();
			try (FileInputStream fis = new FileInputStream(confFile);) {
				props.load(fis);
				fis.close();
			}
			isUserPd1 = props.getProperty("cr.ims").trim();
			//--解密
			if(commString.strIn(TarokoParm.getInstance().getDbSwitch2Dr(), "1,2,3,4,5,6,Y,D")) {
				Decryptor decrptor = new Decryptor();
				isUserPd1 = decrptor.doDecrypt(isUserPd1);
			}
			//--取Token
			JSONObject jsonObjectUserPw = new JSONObject();
			jsonObjectUserPw.put("password", isUserPd1);
	        jsonObjectUserPw.put("username", isUserName1);
	        String userPass = jsonObjectUserPw.toString();		
			HpeUtil hpeUtil = new HpeUtil();			
			//--取 Token			
			String lsTemp = "";
			lsTemp = hpeUtil.curlToken(isUrlToken, isApplJson1, "", userPass);
			isToken = parsingJson(lsTemp,1);
			lsTokenDate = this.getSysDate();
			updateSql = "update ptr_sys_parm set wf_value = ? , wf_value2 = ? , wf_value3 = ? , wf_value4 = ? where wf_parm = 'IMS_TOKEN' and wf_key = 'TOKEN'";
			
			setString(1,lsTokenDate);
			setString(2,commString.mid(isToken, 0,100));
			setString(3, commString.mid(isToken, 100, 100));
			setString(4, commString.mid(isToken, 200, isToken.length()));
			sqlExec(updateSql);
			sqlCommit(1);
		}
		
	}

	// 更新Dba_Debt
	public int updateDbaDebt() {
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("dba_debt");
		sp.ppnum("end_bal", Integer.parseInt(wp.itemStr("bef_amt")) - Integer.parseInt(wp.itemStr("deduct_amt")));
		sp.ppdate("mod_time");
		sp.ppstr("mod_pgm", "dbap0070");
		sp.ppstr("mod_user", wp.loginUser);
		sp.sql2Where(" WHERE REFERENCE_NO =?", wp.itemStr("reference_no"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
			errmsg(this.sqlErrtext);
			return rc;
		} else {
			rc = 1;
		}
		return rc;
	}
	
	public int procQuery() throws Exception {
		//--基本檢核
		queryCheck();
		if(rc !=1)
			return rc;
		//--送查詢電文
		getImsParm();				        		
		HpeUtil hpeUtil = new HpeUtil();
		JSONObject jsonObjectSent = new JSONObject();
		// --取 Token		
		String seqNo = "";
		String lsSendData = "" , lsOriSendData = "" , backSeqNo = "";
		String lsReceiveData = "";
		String lsReponseCode = "" , lastTxnRespCode = "";
		String lsTempReceiveData = "";
		String sql1 = "select seq_send_ibmseqno.nextval as seqno from dual ";
		sqlSelect(sql1);
		seqNo = commString.lpad(colStr("seqno"), 6, "0");
		lsSendData += " 0200VDIQATM ";
		lsSendData += "CRD0" + seqNo;
		lsSendData += seqNo;
		lsSendData += getSysDate();
		lsSendData += commDate.sysTime();
		lsSendData += "QCLIENTID1     ";		
		lsSendData += "VQ";
		lsSendData += commString.rpad(wp.itemStr("card_no") + "=" + commString.mid(wp.itemStr("new_end_date"), 2, 4), 37); // --卡號=new_end_date(MMYY)
//		lsSendData += commString.lpad("0", 10,"0");
//		lsSendData += commString.lpad("0", 10,"0");
		
		if(wp.itemEmpty("last_ims_amt") == false) {			
			lsSendData += commString.lpad(wp.itemStr("last_ims_amt"), 20,"0");
		}	else	{
			lsSendData += commString.lpad(wp.itemStr("last_trans_amt"), 10,"0");
			lsSendData += commString.lpad(wp.itemStr("last_trans_amt"), 10,"0");
		}		
		
		lsSendData += commString.rpad("0", 10,"0");
		lsSendData += commString.rpad(wp.itemStr("acct_no"), 13, " ");																		
		lsSendData += commString.rpad(wp.itemStr("card_ref_num"), 2, " ");
		lsSendData += commString.rpad(wp.itemStr("last_ims_seq_no"), 10); // --要查詢的原交易序號
		lsSendData += commString.rpad("VDAD 9", 6); 
		lsSendData += wp.itemStr("tx_time"); // --交易時間 HHMMSS
		lsSendData += commString.mid(wp.itemStr("tx_date"), 4, 4); // --交易日期 MMDD
		lsSendData += commString.rpad(wp.itemStr("ref_no"), 12); // --ref_no
		lsSendData += commString.rpad(wp.itemStr("v_card_no"), 20); // --v_card_no
		lsSendData += " "; // --保留
		lsOriSendData = lsSendData.trim() ;
		insertImsLog(lsOriSendData,"","CRD0"+seqNo);
		sqlCommit(1);
		try {
			lsSendData = hpeUtil.encoded2Base64(hpeUtil.transByCode(lsSendData, "Cp1047"));
		    jsonObjectSent.put("message", lsSendData);
		    jsonObjectSent.put("seqNo", "0200VDIQATM CRD0"+seqNo);
		    lsSendData = jsonObjectSent.toString();
		    log(lsSendData);
		    lsTempReceiveData = hpeUtil.curlToken(isUrlTxn, isApplJson2, isToken, lsSendData);
		    lsTempReceiveData = parsingJson(lsTempReceiveData,2);
		    lsReceiveData = hpeUtil.ebcdic2Str(hpeUtil.decodedString2(lsTempReceiveData));
		}	catch(Exception e) {
			return -1;
		}
		log(lsReceiveData);
	    backSeqNo = commString.mid(lsReceiveData, 21, 10);
	    if (backSeqNo.equals("CRD0" + seqNo)) {
	    	lsReponseCode = commString.mid(lsReceiveData, 61, 4);
	    }
	    
	    if(lsReponseCode.isEmpty())	{
	    	queryDesc = "主機未回應或逾時" ;
	    	return -1;
	    }	else if("0000".equals(lsReponseCode)) {
	    	//--本次查詢結果成功 , 再判斷上次交易結果	    	
	    	lastTxnRespCode = commString.mid(lsReceiveData, 162, 4);
	    	if("0000".equals(lastTxnRespCode)) {
	    		//--上次交易結果成功
	            //--ECS 端原為失敗但主機端是成功故要將ECS進行成功處理
	    		deductDate = selectdate();
	    		deductSeq = selectDeductSeq();
		    	if (checkBeforeInsertQuery() == 1) {// 新增之前需要检验 rc =
		    		insertDeductTxnQuery("00", "01");
					if (rc != 1) {
						queryDesc = "成功 , 但是新增資料[deduct]時失敗請聯繫負責人員";
						rc = -3 ;
						return rc;
					}
				}

				rc = insertDbaJrnlQuery();// 新增 DbaJrnl
				if (rc != 1) {
					queryDesc = "成功 , 但是新增資料[jrnl]時失敗請聯繫負責人員";
					rc = -3 ;
					return rc;
				}

				rc = updateDbaDebtQuery(); // 修改 DbaDebt
				if (rc != 1) {
					queryDesc = "成功 , 但是新增資料[debt]時失敗請聯繫負責人員";
					rc = -3 ;
					return rc;
				}
				
				//--將log update 為 成功
				rc = updateImsLogQuery();
				if (rc != 1) {
					queryDesc = "成功 , 但是資料處理[imslog]時失敗請聯繫負責人員";
					rc = -3 ;
					return rc;
				}
				queryDesc = "成功";				
	    	}	else	{
	    		//--上次交易結果失敗
	    		queryDesc = "失敗 , 主機回覆代碼為 = [ "+lastTxnRespCode+"]";
	    		rc =1;
	    	}
	    	updateCcaImsLog("CRD0"+seqNo,lsReponseCode,"");
	    }	else	{
	    	queryDesc = "主機回覆代碼為 = [ "+lsReponseCode+"]";
	    	rc = -1; 
	    }
	    
		return rc;
	}
	
	int updateImsLogQuery() throws Exception {
		
		String sql1 = "";
		sql1 = " update cca_ims_log set ims_resp_code = '0000' , proc_code = '' where ims_seq_no = ? and tx_date = ? and tx_time = ? ";
		setString(1,wp.itemStr("last_ims_seq_no"));
		setString(2,wp.itemStr("last_tx_date"));
		setString(3,wp.itemStr("last_tx_time"));				
		sqlExec(sql1);
		
		return rc;
	}
	
	void queryCheck() throws Exception {
		String lastSeqNo = "" , lastResp = "";
		lastSeqNo = wp.itemStr("last_ims_seq_no");
		lastResp = wp.itemStr("last_ims_resp_code");
		if(lastSeqNo.isEmpty())	{
			errmsg("無上次交易電文序號 , 不可查詢 !");
			rc =-2;
			return ;
		}
		if("0000".equals(lastResp)) {
			errmsg("上次交易結果為成功 , 不須查詢 !");
			rc =-2;
			return ;
		}
	}
	
	// 扣款金額 >0 且 :扣款金額 <= 目前期末餘額 才可新增
	public int checkBeforeInsertQuery() {
		if (Integer.parseInt(wp.itemStr("last_trans_amt")) > 0
				&& Integer.parseInt(wp.itemStr("last_trans_amt")) <= Integer.parseInt(wp.itemStr("ex_bef_amt"))) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public int insertDbaJrnlQuery() {
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HHmmss");
		LocalDateTime dt = LocalDateTime.now();
		/*
		 * sp.ppstr("crt_date",date.format(formatter));
		 * sp.ppstr("crt_time",time.format(formatter1));
		 */
		/*
		 * 程序说明书上有这两列但是 表中并无此两列 sp.ppstr("id", wp.itemStr("id_no"));
		 * sp.ppstr("id_no_code", wp.itemStr("id_no_code"));
		 */
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("dba_jrnl");
		sp.ppstr("p_seqno", wp.itemStr("p_seqno"));
		sp.ppstr("crt_date", deductDate);
		// sp.addsql(", crt_date ", ",select business_date from ptr_businday");
		sp.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
		/* sp.ppstr("crt_time", time.format(formatter1)); */
		sp.ppstr("acct_type", wp.itemStr("acct_type"));
		sp.ppstr("acct_no", wp.itemStr("acct_no"));
		sp.ppstr("id_p_seqno", wp.itemStr("id_p_seqno"));
		sp.ppstr("deduct_seq", deductSeq);
		sp.ppstr("stmt_cycle", wp.itemStr("stmt_cycle"));
		sp.ppstr("debt_status", wp.itemStr("debt_status"));
		sp.ppstr("trans_col_date", wp.itemStr("trans_col_date"));
		sp.ppstr("trans_bad_date", wp.itemStr("trans_bad_date"));
		sp.ppstr("acct_code", wp.itemStr("acct_code"));
		sp.ppstr("acct_date", deductDate);
		// sp.addsql(", acct_date ", ", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("tran_class", "D");
		sp.ppstr("tran_type", "VD");
		sp.ppstr("dr_cr", "D");
		sp.ppstr("transaction_amt", wp.itemStr("last_trans_amt"));
		sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
		sp.ppstr("item_post_date", wp.itemStr("item_post_date"));
		sp.ppstr("item_date", deductDate);
		// sp.addsql(", item_date ", ", to_char(sysdate,'yyyymmdd') ");
		// sp.ppstr("item_date", date.format(formatter));
		sp.ppstr("reference_no", wp.itemStr("reference_no"));
		sp.ppstr("card_no", wp.itemStr("card_no"));
		sp.ppstr("pay_id", wp.itemStr("card_no"));
		int bal = Integer.parseInt(wp.itemStr("bef_amt")) - Integer.parseInt(wp.itemStr("last_trans_amt"));
		sp.ppstr("jrnl_bal", String.valueOf(bal));
		sp.ppstr("item_bal", String.valueOf(bal));
		sp.ppstr("item_d_bal", wp.itemStr("d_avail_bal"));
		sp.addsql(", mod_time ", ", sysdate ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", "dbap0070");
		// sp.ppstr("mod_time", String.valueOf(dt));

		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
			errmsg(this.sqlErrtext);
		} else {
			rc = 1;
		}
		return rc;
	}
	
	// 更新Dba_Debt
	public int updateDbaDebtQuery() {
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("dba_debt");
		sp.ppnum("end_bal", Integer.parseInt(wp.itemStr("bef_amt")) - Integer.parseInt(wp.itemStr("last_trans_amt")));
		sp.ppdate("mod_time");
		sp.ppstr("mod_pgm", "dbap0070");
		sp.ppstr("mod_user", wp.loginUser);
		sp.sql2Where(" WHERE REFERENCE_NO =?", wp.itemStr("reference_no"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
			errmsg(this.sqlErrtext);
			return rc;
		} else {
			rc = 1;
		}
		return rc;
	}
	
	public int insertDeductTxnQuery(String lsProcCode, String lsProcType) {
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HHmmss");
		LocalDateTime dt = LocalDateTime.now();
		int bal = Integer.parseInt(wp.itemStr("bef_amt")) - Integer.parseInt(wp.itemStr("last_trans_amt"));
		actionInit("A");
		busi.SqlPrepare sp = new SqlPrepare();
		String idNo = ((wp.itemStr("corp_no") == null) ? wp.itemStr("id_no") : wp.itemStr("corp_no"));
		sp.sql2Insert("dba_deduct_txn");
		sp.ppstr("tx_seq", wp.itemStr("tx_seq"));
		sp.ppstr("deduct_seq", deductSeq);
		sp.ppstr("crt_date", deductDate);
		// sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("abstract_code", "VDPY");
		sp.ppstr("deduct_date", deductDate);
		sp.ppstr("reference_no", wp.itemStr("reference_no"));
		sp.ppstr("acct_no", wp.itemStr("acct_no"));
		sp.ppstr("p_seqno", wp.itemStr("p_seqno"));
		sp.ppstr("acct_type", wp.itemStr("acct_type"));
		sp.ppstr("id_p_seqno", wp.itemStr("id_p_seqno"));
		sp.ppstr("id_no", idNo);
		// sp.addsql(", ", ", decode(+" + wp.itemStr("corp_no") + ",null," +
		// wp.itemStr("id_no") + ","+
		// wp.itemStr("corp_no") + ") ");
		sp.ppstr("id_no_code", wp.itemStr("id_no_code"));
		sp.ppstr("card_no", wp.itemStr("card_no"));
		sp.ppstr("bank_actno", wp.itemStr("bank_actno"));
		sp.ppstr("acct_code", wp.itemStr("acct_code"));
		sp.ppstr("acct_item_cname", "");
		sp.ppstr("merchant_no", wp.itemStr("mcht_no"));
		sp.ppstr("transaction_code", wp.itemStr("txn_code"));
		sp.ppstr("item_post_date", wp.itemStr("item_post_date"));
		sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
		sp.ppstr("beg_bal", wp.itemStr("hi_beg_bal"));
		sp.ppstr("end_bal", wp.itemStr("aft_amt"));
		sp.ppstr("d_available_bal", wp.itemStr("d_avail_bal"));
		sp.ppstr("debt_status", wp.itemStr("debt_status"));
		sp.ppstr("from_code", "3");
		sp.ppstr("stmt_cycle", wp.itemStr("stmt_cycle"));
		sp.ppstr("org_reserve_amt", wp.itemStr("org_reserve_amt"));
		sp.ppstr("reserve_amt", wp.itemStr("reserve_amt"));
		sp.ppstr("org_deduct_amt", wp.itemStr("last_trans_amt"));
		sp.ppstr("deduct_amt", wp.itemStr("last_trans_amt"));
		sp.ppstr("trans_col_date", wp.itemStr("trans_col_date"));
		sp.ppstr("trans_bad_date", wp.itemStr("trans_bad_date")); // deduct_proc_date
		sp.ppstr("deduct_proc_date", date.format(formatter));
		sp.ppstr("deduct_proc_time", time.format(formatter1));
		sp.ppstr("deduct_proc_type", lsProcType);
		sp.ppstr("deduct_proc_code", lsProcCode);
		sp.addsql(", mod_time ", ", sysdate ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", "dbap0070");
		// System.out.println("sql語句"+sp.sqlStmt());
		// System.out.println("sql變量"+sp.sqlParm());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
			rc = -1;
			return rc;
		} else {
			rc = 1;
		}
		return rc;
	}
	
}
