/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-06-01  V1.00.00  Alex       Initial									 *
* 109-06-12  V1.00.01  Alex       add callToIBM								 *
* 109-10-16  V1.00.02  tanwei     updated for project coding standard        *
* 109-11-13  V1.00.03  Alex       解圈後改為不佔額度								 *
* 109-12-23  V1.00.04  Justin      parameterize sql
* 111-03-22  V1.00.05  Justin     多一個篩選條件 , 交易成功 , 交易失敗       *
******************************************************************************/
package dbam01;

import ofcapp.BaseAction;
import taroko.com.TarokoParm;

import java.io.FileInputStream;
import java.text.Normalizer;
import java.util.Properties;

import bank.Auth.HpeUtil;
import net.sf.json.JSONObject;
import com.tcb.ap4.tool.*;

import Dxc.Util.SecurityUtil;

public class Dbap0065 extends BaseAction {
	String isIdPSeqno2 = "", isUrlToken = "", isUrlTxn = "";
	String isUserName1 = "", isUserName2 = "", isUserPd1 = "", isUserPd2 = "";
	String isApplJson1 = "", isApplJson2 = "", isToken = "";
	taroko.base.CommDate commDate = new taroko.base.CommDate();
	taroko.base.CommString commString = new taroko.base.CommString();
	JSONObject jasonToString = new JSONObject();
	boolean ibDebug = false;

	@Override
	public void userAction() throws Exception {
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
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			saveFunc();
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
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			procFunc();
		} else if (eqIgno(wp.buttonCode, "C1")) {
			// -資料處理-
			procFunc2();
		}
	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {				
		
		if(wp.itemEmpty("ex_auth_seqno")) {
			alertErr("授權序號: 不可空白");
			return ;
		}
		
		String lsWhere = " where 1=1 and A.vdcard_flag = 'D' "
				+sqlCol(wp.itemStr("ex_auth_seqno"),"auth_seqno")
				;		

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = "hex(A.rowid) as rowid ," + " A.auth_seqno ," + " A.card_no ," + " A.tx_date ," + " A.tx_time ,"
				+ " A.eff_date_end ," + " A.mcht_no ," + " A.mcht_name ," + " A.mcc_code ," + " A.pos_mode ,"
				+ " substr(A.pos_mode,1,2) as pos_mode_1_2 ," + " substr(A.pos_mode,3,1) as pos_mode_3 ,"
				+ " A.nt_amt ," + " A.consume_country ," + " A.tx_currency ," + " A.iso_resp_code ,"
				+ " A.auth_status_code ," + " A.iso_adj_code ," + " A.auth_no ," + " A.auth_user ," + " A.vip_code ,"
				+ " A.stand_in ," + " A.class_code ," + " A.auth_unit ," + " A.logic_del ," + " A.auth_remark ,"
				+ " A.trans_type ," + " uf_idno_id2(A.card_no,'') as id_no ,"
				+ " uf_idno_name(A.id_p_seqno) as db_idno_name ," + " A.curr_otb_amt ," + " A.curr_tot_lmt_amt ,"
				+ " A.curr_tot_std_amt ," + " A.curr_tot_tx_amt ," + " A.curr_tot_cash_amt ," + " A.curr_tot_unpaid ,"
				+ " A.fallback ," + " A.roc ," + " A.ibm_bit39_code ," + " A.ibm_bit33_code ," + " A.ec_ind ,"
				+ " A.ucaf ," + " A.mtch_flag, A.cacu_amount," + " A.ec_flag ,"
				+ " uf_tt_ccas_parm3('LOGICDEL',A.logic_del) as tt_logic_del ," + " A.v_card_no ,"
				+ " A.online_redeem ," + " uf_tt_ccas_parm3('AUTHUNIT',A.auth_unit) as tt_auth_unit ,"
				+ " decode(A.online_redeem,'','','A','分期 (A)','I','分期 (I)','E','分期 (E)','Z','分期 (Z)','0','紅利 (0)','1','紅利 (1)','2','紅利 (2)',"
				+ " '3','紅利 (3)','4','紅利 (4)','5','紅利 (5)','6','紅利 (6)','7','紅利 (7)','') as tt_online_redeem ,"
				+ " decode(curr_tot_std_amt,0,0,((curr_tot_unpaid+decode(cacu_amount,'Y',nt_amt,0)) / curr_tot_std_amt)) * 100 as cond_curr_rate , "
				+ " A.id_p_seqno , " + " iso_resp_code||'-'||auth_status_code||'-'||iso_adj_code as wk_resp , "
				+ " ibm_bit39_code||'-'||ibm_bit33_code as wk_IBM , "
				+ " A.ori_amt , A.trace_no , A.ref_no , A.tx_seq , uf_corp_no(A.corp_p_seqno) as corp_no , "
				+ " A.vd_lock_nt_amt , A.card_acct_idx , A.acno_p_seqno , A.acct_type , uf_acno_key2(acno_p_seqno,'Y') as acct_key ";

		wp.daoTable = " cca_auth_txlog A ";
		wp.whereOrder = " order by A.tx_date desc, A.tx_time Desc ";

		pageQuery();

		if (sqlNotFind()) {
			alertErr2("此條件查無資料");
			return;
		}
		queryAfter();
		wp.setListCount(0);
		wp.setPageValue();

	}

	void queryAfter() {
		String sql1 = " select acct_no , new_end_date , card_ref_num from dbc_card where card_no = ? ";
		int ilCnt = wp.selectCnt;
		for (int ii = 0; ii < ilCnt; ii++) {
			sqlSelect(sql1, new Object[] { wp.colStr(ii, "card_no") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "acct_no", sqlStr("acct_no"));
				wp.colSet(ii, "new_end_date", sqlStr("new_end_date"));
				wp.colSet(ii, "card_ref_num", sqlStr("card_ref_num"));
			}
		}
	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void procFunc() throws Exception {
		int llOk = 0, llErr = 0;
		String[] aaOpt = wp.itemBuff("opt");
		String[] aaRowid = wp.itemBuff("rowid");
		String[] aaNewEndDate = wp.itemBuff("new_end_date");
		String[] aaAcctNo = wp.itemBuff("acct_no");
		String[] aaTraceNo = wp.itemBuff("trace_no");
		String[] aaRefNo = wp.itemBuff("ref_no");
		String[] aaCardNo = wp.itemBuff("card_no");
		String[] aaTxDate = wp.itemBuff("tx_date");
		String[] aaTxTime = wp.itemBuff("tx_time");
		String[] aaVCardNo = wp.itemBuff("v_card_no");		
		String[] aaVdLockNtAmt = wp.itemBuff("vd_lock_nt_amt");
		String[] aaCardRefNum = wp.itemBuff("card_ref_num");
		String[] aaTxSeq = wp.itemBuff("tx_seq");
		wp.listCount[0] = wp.itemRows("rowid");

		optNumKeep(wp.itemRows("rowid"));
//		ibDebug = true;
		getImsParm();
		HpeUtil hpeUtil = new HpeUtil();
		// --取編號
		String seqNo = "", backSeqNo = "";
		String sql1 = "select seq_send_ibmseqno.nextval as seqno from dual ";
		int rr = -1;
		String lsSentData = "", lsTempReceiveData = "", lsReceiveData = "", lsReponseCode = "", lsOriSentData = "";
		String oldDataDate = "20220321";
		JSONObject jsonObjectSent = new JSONObject();
		for (int ii = 0; ii < aaOpt.length; ii++) {
			rr = this.optToIndex(aaOpt[ii]);
			if (rr < 0)
				continue;
			optOkflag(rr);
			lsReceiveData = "";
			lsReponseCode = "";
			lsTempReceiveData = "";
			seqNo = "";
			backSeqNo = "";
			lsOriSentData = "";
			sqlSelect(sql1);
			seqNo = commString.lpad(sqlStr("seqno"), 6, "0");			
			// --上行電文 Head 70
			lsSentData = "";
//			lsSentData += hpeUtil.hexStr2Str("00D50000"); 長度改由 IMS計算 IBPBMCRD 由 IMS程式塞入從0200開始
//			lsSentData += "IBPBMCRD 0200VDRTATM ";
			lsSentData += " 0200VDRTATM ";
			lsSentData += "CRD0" + seqNo; // --CRD0+送JSON Seqno
			lsSentData += seqNo; // --送JSON Seqno
			lsSentData += getSysDate();
			lsSentData += commDate.sysTime();
			lsSentData += "BCLIENTID1     ";

			// --上行電文 Body 143
			lsSentData += "VR";
			lsSentData += commString.rpad(aaCardNo[rr] + "=" + commString.mid(aaNewEndDate[rr], 2, 4), 37); // --卡號=new_end_date(MMYY)
			lsSentData += commString.rpad("0", 10, "0"); // --VD 實際圈存金額 TX_AMT
			lsSentData += commString.lpad(aaVdLockNtAmt[rr], 10, "0"); //--  charge_amt
			lsSentData += commString.rpad("0", 10, "0"); //--  強圈時才放金額
			lsSentData += commString.rpad(aaAcctNo[rr], 13, " "); // --acct_no
			lsSentData += commString.rpad(aaCardRefNum[rr], 2, " "); // --金融卡序號
			// --圈存序號
			lsSentData += commString.rpad("CRD0"+commString.mid(aaTxSeq[rr], 4), 10);
//			lsSentData += commString.rpad("CRD0"+commString.mid(aaTxSeq[rr], 4), 10); // --圈存序號
//			lsSentData += commString.rpad(aaTxSeq[rr], 10); // --圈存序號
			lsSentData += commString.rpad(aaTraceNo[rr], 6); // --trace_no
			lsSentData += aaTxTime[rr]; // --交易時間 HHMMSS
			lsSentData += commString.mid(aaTxDate[rr], 4, 4); // --交易日期 MMDD
			lsSentData += commString.rpad(aaRefNo[rr], 12); // --ref_no
			lsSentData += commString.rpad(aaVCardNo[rr], 20); // --v_card_no
			lsSentData += " "; // --保留
			lsOriSentData = lsSentData.trim();
			//--傳送電文前先寫log
			if(insertImsLog(rr, lsOriSentData, "", "CRD0" + seqNo)==1) {
				sqlCommit(1);
			}	else	{
				wp.colSet(rr, "ok_flag", "X");
				dbRollback();
				continue;
			}			
			lsSentData = hpeUtil.encoded2Base64(hpeUtil.transByCode(lsSentData, "Cp1047"));
			jsonObjectSent.put("message", lsSentData);
			jsonObjectSent.put("seqNo", "0200VDRTATM CRD0" + seqNo);
			lsSentData = jsonObjectSent.toString();
			log(lsSentData);
			if (ibDebug)
				break;
			lsTempReceiveData = hpeUtil.curlToken(isUrlTxn, isApplJson2, isToken, lsSentData);
			if (empty(lsTempReceiveData)) {
				//--電文沒有回應不需 update log
				wp.colSet(rr, "ok_flag", "X");
				llErr++;
				continue;
			}
			
			lsTempReceiveData = parsingJson(lsTempReceiveData, 2);
			lsReceiveData = hpeUtil.ebcdic2Str(hpeUtil.decodedString2(lsTempReceiveData));
			
			backSeqNo = commString.mid(lsReceiveData, 21, 10);
			if (backSeqNo.equals("CRD0" + seqNo) == false) {
				//--電文回覆序號錯誤不需 update log
				llErr++;
				continue;
			}
			
			// --66~69 reponse code 0000 表示成功
			lsReponseCode = commString.mid(lsReceiveData, 61, 4);
			if (lsReponseCode.equals("0000")) {
//				rc = updateCcaAuthTxlog(aaRowid[rr]);
//				if (rc == 1) {
//					llOk++;
//					wp.colSet(rr, "ok_flag", "V");
//					sqlCommit(1);
//				} else {
//					wp.colSet(rr, "ok_flag", "X");
//					llErr++;
//					dbRollback();
//				}
				llOk++;
				wp.colSet(rr, "ok_flag", "V");
				if (updateCcaImsLog("CRD0" + seqNo, lsReponseCode,"") == 1) {					
					sqlCommit(1);
					continue;
				} else {
					dbRollback();
					continue;
				}
			} else {
				llErr++;
				wp.colSet(rr, "ok_flag", "X");
				if (updateCcaImsLog("CRD0" + seqNo, lsReponseCode,"") == 1) {
					sqlCommit(1);
					continue;
				} else {
					dbRollback();
					continue;
				}
			}
		}

		alertMsg("解圈處理完成 , 成功 : " + llOk + " 筆 , 失敗 : " + llErr + " 筆");

	}
	
	public void procFunc2() throws Exception {
		int llOk = 0, llErr = 0 , llSame = 0;
		String[] aaOpt = wp.itemBuff("opt");			
		wp.listCount[0] = wp.itemRows("rowid");

		int rr = -1;
		for (int ii = 0; ii < aaOpt.length; ii++) {
			rr = this.optToIndex(aaOpt[ii]);
			if (rr < 0)
				continue;
			optOkflag(rr);			
			
			if(checkAcaj(rr) == false) {
				llSame++;
				wp.colSet(rr, "ok_flag", "!");
				continue;
			}
			
			rc = insertDbaAcaj(rr);
			if (rc == 1) {
				llOk++;
				wp.colSet(rr, "ok_flag", "V");
				sqlCommit(1);
			} else {
				wp.colSet(rr, "ok_flag", "X");
				llErr++;
				dbRollback();
			}
		}

		alertMsg("處理完成 , 成功 : " + llOk + " 筆 , 失敗 : " + llErr + " 筆 , 當日重複執行: "+llSame );
	}
	
	public int updateCcaImsLog(String lsSeqNo , String lsRespCode , String lsProcCode)	{
		
		String sql1 = "";
		sql1 = " update cca_ims_log set ims_resp_code = ? , proc_code = ? where ims_seq_no = ? and tx_date = to_char(sysdate,'yyyymmdd') ";
		setString(1,lsRespCode);
		setString(2,lsProcCode);
		setString(3,lsSeqNo);				
		sqlExec(sql1);
		
		return rc;
	}
	
	public int updateCcaAuthTxlog(String lsRowid) {

		String sql1 = "";

		sql1 = " update cca_auth_txlog set mod_pgm = ? , mod_time = sysdate , mod_user = ? ,";
		sql1 += " mod_seqno = nvl(mod_seqno,0)+1 , unlock_flag = 'M' , cacu_amount ='N' , ";
		sql1 += " chg_date = to_char(sysdate,'yyyymmdd') , chg_time = to_char(sysdate,'hh24miss') where 1=1 and rowid = ? ";

		setString(1, wp.modPgm());
		setString(2, wp.loginUser);
		setRowid(3,lsRowid);
		sqlExec(sql1);

		return rc;
	}

	@Override
	public void initButton() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initPage() {
		wp.initFlag = "Y";

	}

	void getImsParm() throws Exception {
		String sql1 = "", sql2 = "", sql3 = "", lsTokenDate = "", updateSql = "";
		sql1 = " select wf_value , wf_value2 , wf_value3 , wf_value4 from ptr_sys_parm "
				+ " where wf_parm = 'IMS_TOKEN' and wf_key = 'GET_TOKEN' ";

		sql2 = " select wf_value , wf_value2 , wf_value3 , wf_value4 from ptr_sys_parm "
				+ " where wf_parm = 'IMS_TXN' and wf_key = 'TXN' ";

		sql3 = " select wf_value , wf_value2||wf_value3||wf_value4 as token from ptr_sys_parm where wf_parm = 'IMS_TOKEN' and wf_key = 'TOKEN' ";

		sqlSelect(sql1);
		if (sqlRowNum > 0) {
			isUrlToken = sqlStr("wf_value");
			isApplJson1 = sqlStr("wf_value2");
			isUserName1 = sqlStr("wf_value3");
			//--dr_flag => 3:crap1R , 6:crap1T 轉打crap1P,crap1T
			if(commString.strIn(TarokoParm.getInstance().getDbSwitch2Dr(), "3,6")) {
				isUrlToken = sqlStr("wf_value4");
			}			
		}
		
		
		
		sqlSelect(sql2);
		if (sqlRowNum > 0) {
			isUrlTxn = sqlStr("wf_value");
			isApplJson2 = sqlStr("wf_value2");
			isUserName2 = sqlStr("wf_value3");
			//--dr_flag => 3:crap1R , 6:crap1T 轉打crap1P,crap1T
			if(commString.strIn(TarokoParm.getInstance().getDbSwitch2Dr(), "3,6")) {
				isUrlTxn = sqlStr("wf_value4");
			}
		}
				
		
		sqlSelect(sql3);
		if (sqlRowNum > 0) {
			lsTokenDate = sqlStr("wf_value");
			isToken = sqlStr("token");
		}						
		
		if (eqIgno(lsTokenDate, getSysDate()) == false) {
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
			// --取 Token
			String lsTemp = "";
			lsTemp = hpeUtil.curlToken(isUrlToken, isApplJson1, "", userPass);
			isToken = parsingJson(lsTemp, 1);
			lsTokenDate = this.getSysDate();
			updateSql = "update ptr_sys_parm set wf_value = ? , wf_value2 = ? , wf_value3 = ? , wf_value4 = ? where wf_parm = 'IMS_TOKEN' and wf_key = 'TOKEN'";

			setString(1, lsTokenDate);
			setString(2, commString.mid(isToken, 0, 100));
			setString(3, commString.mid(isToken, 100, 100));
			setString(4, commString.mid(isToken, 200, isToken.length()));
			sqlExec(updateSql);
			sqlCommit(1);
		}

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

	void selectIdPseqno() {
		String sql1 = " select uf_vd_idno_pseqno(?) as id_p_seqno2 from dual ";
		sqlSelect(sql1, new Object[] { wp.itemStr("ex_idno")});

		isIdPSeqno2 = sqlStr("id_p_seqno2");
	}

	int insertImsLog(int i, String sendData, String respCode, String imsSeqNo) {
		String[] aaOriAmt = wp.itemBuff("ori_amt");
		String[] aaCardNo = wp.itemBuff("card_no");
		String[] aaCardAcctIdx = wp.itemBuff("card_acct_idx");
		String[] aaAcnoPSeqno = wp.itemBuff("acno_p_seqno");
		String[] aaAuthNo = wp.itemBuff("auth_no");

		String sql1 = "";
		sql1 = "insert into cca_ims_log (" + " tx_date ," + " tx_time ," + " card_no ," + " auth_no ," + " trans_type ,"
				+ " card_acct_idx ," + " acno_p_seqno ," + " ims_seq_no ," + " ims_reversal_data ," + " trans_amt ,"
				+ " ims_resp_code ," + " iso_resp_code ," + " crt_date ," + " crt_user ," + " send_date ,"
				+ " proc_code ," + " mod_time ," + " mod_pgm " + ") values ( " + " to_char(sysdate,'yyyymmdd') ,"
				+ " to_char(sysdate,'hh24miss') ," + " :card_no ," + " :auth_no ," + " '' ," + " :card_acct_idx ,"
				+ " :acno_p_seqno ," + " :ims_seq_no ," + " :ims_reversal_data ," + " :trans_amt ,"
				+ " :ims_resp_code ," + " :iso_resp_code ," + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ,"
				+ " to_char(sysdate,'yyyymmdd') ," + " '' ," + " sysdate ," + " 'Dbap0065' " + " )";

		setString("card_no", aaCardNo[i]);
		setString("auth_no", aaAuthNo[i]);
		setDouble("card_acct_idx", commString.strToNum(aaCardAcctIdx[i]));
		setString("acno_p_seqno", aaAcnoPSeqno[i]);
		setString("ims_seq_no", imsSeqNo);
		setString("ims_reversal_data", sendData);
		setString("trans_amt", aaOriAmt[i]);
		setString("ims_resp_code", respCode);		
		setString("iso_resp_code", "00");
		setString("crt_user", wp.loginUser);
		sqlExec(sql1);
		return rc;
	}
	
	int insertDbaAcaj(int i) {
		
		String[] acnoPSeqno = wp.itemBuff("acno_p_seqno");
		String[] acctType = wp.itemBuff("acct_type");
		String[] acctNo = wp.itemBuff("acct_no");
		String[] referenceNo = wp.itemBuff("ref_no");
		String[] cardNo = wp.itemBuff("card_no");
		String[] purchaseDate = wp.itemBuff("tx_date");
		String[] txSeq = wp.itemBuff("tx_seq");
		String[] mchtNo = wp.itemBuff("mcht_no");
		String[] vdLockNtAmt = wp.itemBuff("vd_lock_nt_amt");
		
		String sql1 = "";
		sql1 = " insert into dba_acaj (crt_date , crt_time , p_seqno , acct_type , acct_no , adjust_type , "
				+ " reference_no , orginal_amt , func_code , card_no , purchase_date ,  proc_flag , "
				+ " txn_code , tx_seq , mcht_no , apr_flag ,  apr_date , apr_user , mod_user , "
				+ " mod_time , mod_pgm , mod_seqno ) values (to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),:p_seqno, "
				+ " :acct_type , :acct_no , 'RE10', :reference_no , :orginal_amt , 'U' , :card_no , :purchase_date , "
				+ " 'N' , '' , :tx_seq , :mcht_no , 'Y' , to_char(sysdate,'yyyymmdd') , 'ecs', :mod_user , sysdate  , :mod_pgm , 1) "
				;
		
		setString("p_seqno",acnoPSeqno[i]);
		setString("acct_type",acctType[i]);
		setString("acct_no",acctNo[i]);
		setString("reference_no",referenceNo[i]);
		setString("orginal_amt",vdLockNtAmt[i]);
		setString("card_no",cardNo[i]);
		setString("purchase_date",purchaseDate[i]);
		setString("tx_seq",txSeq[i]);
		setString("mcht_no",mchtNo[i]);
		setString("mod_user",wp.loginUser);
		setString("mod_pgm","Dbap0065");
		
		sqlExec(sql1);		
		
		return rc;
	}
	
	boolean checkAcaj(int i) {
		String[] txSeq = wp.itemBuff("tx_seq");
		String sql1 = "select count(*) as db_cnt from dba_acaj where tx_seq = ? and adjust_type = 'RE10' and proc_flag = 'N' ";
		sqlSelect(sql1,new Object[] {txSeq[i]});
		
		if(sqlNum("db_cnt") > 0)
			return false ;
		
		return true;
	}
	
}
