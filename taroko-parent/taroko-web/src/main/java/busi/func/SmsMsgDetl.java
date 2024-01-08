/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-12-24   V1.00.02 Justin        parameterize sql
******************************************************************************/
package busi.func;
/**發送簡訊公用程式
 * 2019-0814   JH    ++ is_msg_desc
 * 2019-0610:  JH    p_seqno >>acno_p_seqno
 * 2018-1024:	JH		isSend_acct_type()
 * 2018-0815:	JH		actm0010()
 * 2018-0810:	JH		sms_msg_dtl24
 * 2018-0802:	JH		modify
 * 110-01-07  V1.00.07  tanwei        修改意義不明確變量                                                                          *
 *
 * */

import busi.FuncBase;

public class SmsMsgDetl extends FuncBase {

	private boolean isSend = false;
	private String sqlMsgSeqno = " lpad(to_char(ecs_modseq.nextval),10,'0') ";

	public MsgDtl hsms = new MsgDtl();
	private busi.SqlPrepare sp = new busi.SqlPrepare();
	// ---------------
	public String strMsgDesc = "";

	public String msgSeqno() {
		return hsms.msgSeqno;
	}

	public int actM0010(String aPSeqno, String aType) {
		if (empty(aPSeqno))
			return 0;
		if (commString.strIn2(aType, ",A,B") == false) {
			errmsg("自動扣繳帳號簡訊類別, 須為 A/B");
			return rc;
		}

		hsms.initData();
		hsms.msgPgm = "W_ACTM0010-" + aType.toUpperCase();
		// -A.sms_msg_id-
		selectSmsMsgId(hsms.msgPgm);
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;

		// -B.idno-
		hsms.pseqno = aPSeqno;
		selectCrdIdno();
		if (rc != 1)
			return rc;

		// -acct_type-
		if (isSendAcctType(hsms.acctType) == false) {
			return 0;
		}

		String lsMsgDesc = "";
		lsMsgDesc = colStr("A.msg_userid") + "," + colStr("A.msg_id") + "," + colStr("B.cellar_phone") + ","
				+ colStr("B.chi_name");
		insertMsgDtl(lsMsgDesc);
		if (rc == -1) {
			errmsg("新增簡訊資料失敗, 使用程式代碼:" + hsms.msgPgm);
		}
		return rc;
	}

	public int cmsP2110Telno(String idPSeqno) {
		if (empty(idPSeqno)) {
			errmsg("卡人流水號:不可空白");
			return -1;
		}

		hsms.initData();
		hsms.idPseqno = idPSeqno;
		hsms.msgPgm = "W_CMSP2110";
		selectSmsMsgId(hsms.msgPgm);
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;

		selectCrdIdno();

		String ss = colStr("A.msg_userid") + "," + colStr("A.msg_id") + "," + colStr("B.cellar_phone") + ","
				+ colStr("B.chi_name");
		hsms.msgDesc = ss;
		insertMsgDtl(ss);

		return rc;
	}

	public int ccaM2050Adj(double aAcctIdx, String aMsgDesc) {
		// sprintf(data,"%s,%s,%s,%s,%.f,%s,%s", snd_user,h_m_msg_id[i].arr
		// ,h_m_mobil_phone[i].arr,h_m_chinese_name[i].arr,adj_amt
		// ,tmp_date,tmp_date2);
		// 姓名 //-XX-,調額,起日(xx月xx日),迄日(xx月xx日)
		// -insert sms_msg_dtl24-
		if (aAcctIdx == 0) {
			errmsg("授權帳戶流水號[card_acct_idx]: 不可空白");
			return rc;
		}
		hsms.initData();
		// -A.sms_msg_id-
		selectSmsMsgId("CCAM2050-ADJ");
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;

		// -B.idno-
		selectCcaCardAcct(aAcctIdx);
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 0;

		// -acct_type-
		if (isSendAcctType(hsms.acctType) == false) {
			return 0;
		}

		// insert_msg_dtl24(a_msg_desc);
		String ss = "";
		ss = hsms.msgUserid+","+hsms.msgId+","+hsms.cellarPhone;
		insertMsgDtl(ss);

		return rc;
	}

	void selectSecUser(String aUserId) {
		strSql = "select cellar_phone from sec_user" + " where user_id =?";
		setString2(1, aUserId);
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			errmsg("查無放行人員資料");
			return;
		}

		hsms.cellarPhone = colStr("cellar_phone");
	}

	public int ccaM2030Sms() {		
		hsms.initData();
		// -A.sms_msg_id-
		selectSmsMsgId("CCAM2030");
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;
		
		hsms.idPseqno = wp.itemStr("id_p_seqno");
		hsms.pseqno = wp.itemStr("acno_p_seqno");
		hsms.idNo = wp.itemStr("id_no");
		hsms.acctType = wp.itemStr("acct_type");
		hsms.cellarPhone = wp.itemStr("cellar_phone");
		hsms.chiName = wp.itemStr("chi_name");
		if (isSendAcctType(hsms.acctType) == false) {
			return 0;
		}
		hsms.phoneFlag = "N";
		if (hsms.cellarPhone.length() == 10 && commString.strToNum(hsms.cellarPhone) > 0) {
			hsms.phoneFlag = "Y";
		}
		String msgDesc = hsms.msgUserid+","+hsms.msgId+","+hsms.cellarPhone;
		insertMsgDtl(msgDesc);
		return rc;
	}
	
	public int ccaM2040Sms() {		
		hsms.initData();
		// -A.sms_msg_id-
		selectSmsMsgId("CCAM2040");
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;
		
		hsms.idPseqno = wp.itemStr("id_p_seqno");
		hsms.pseqno = wp.itemStr("acno_p_seqno");
		hsms.idNo = wp.itemStr("id_no");
		hsms.acctType = wp.itemStr("acct_type");
		hsms.cellarPhone = wp.itemStr("cellar_phone");
		hsms.chiName = wp.itemStr("idno_name");
		if (isSendAcctType(hsms.acctType) == false) {
			return 0;
		}
		hsms.phoneFlag = "N";
		if (hsms.cellarPhone.length() == 10 && commString.strToNum(hsms.cellarPhone) > 0) {
			hsms.phoneFlag = "Y";
		}
		
		String msgDesc = hsms.msgUserid+","+hsms.msgId+","+hsms.cellarPhone;
		
		insertMsgDtl(msgDesc);
		return rc;
	}
	
	public int ccaM2050Appr(String aAprUser, String aMsgDesc) {
		// -insert sms_msg_dtl24-
		if (empty(aAprUser)) {
			return 0;
		}

		hsms.initData();
		// -A.sms_msg_id-
		selectSmsMsgId("CCAM2050-APPR");
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;

		// -B.user-
		selectSecUser(aAprUser);
		if (rc != 1)
			return rc;
		if (empty(hsms.cellarPhone)) {
			errmsg("放行人員: 無手機號碼");
			return 0;
		}
		colSet("B.id_p_seqno", wp.itemStr2("id_p_seqno"));
		colSet("B.acno_p_seqno", wp.itemStr2("acno_p_seqno"));
		colSet("B.chi_name", wp.itemStr2("chi_name"));
		String msgDesc = hsms.msgUserid+","+hsms.msgId+","+hsms.cellarPhone;
		insertMsgDtl(msgDesc);
		return rc;
	}

	public int rskP0920LimitUp(String aPseqno, String aCardNo, String aMsgPgm) {
		// -sms_msg_id:766-
		if (empty(aPseqno)) {
			errmsg("帳戶流水號: 不可空白");
			return rc;
		}
		hsms.initData();
		hsms.pseqno = aPseqno;
		hsms.phoneFlag = "N";

		// -A.sms_msg_id-
		selectSmsMsgId(aMsgPgm);
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;

		// -B.idno-
		selectCrdIdno();
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 0;

		// -acct_type-
		if (isSendAcctType(hsms.acctType) == false) {
			return 0;
		}
		// --
		strSql = "select count(*) as db_cnt" + " from sms_msg_dtl" + " where crt_date = to_char(sysdate,'yyyymmdd')"
				+ " and msg_pgm = ? " + " and id_p_seqno = ? " + " and cellar_phone = ?";
		sqlSelect(strSql, new Object[] { hsms.msgPgm, hsms.idPseqno, hsms.cellarPhone });
		if (colNum("db_cnt") > 0) {
			return 0;
		}

		String colName = colStr("A.msg_userid") + "," + colStr("A.msg_id") + "," + colStr("B.cellar_phone") + ","
				+ colStr("B.chi_name");
		if (notEmpty(strMsgDesc))
			colName += "," + strMsgDesc;

		// if (eq_any(ls_acct_type,"02")) {
		// ss +=",商務";
		// }
		// else if (eq_any(ls_acct_type,"03")) {
		// ss +=",採購";
		// }
		hsms.msgDesc = colName;
		insertMsgDtl(colName);
		return rc;
	}

	public int rskP0920LimitDown(String aPseqno, String aCardNo) {
		// -sms_msg_id:1021-
		if (empty(aPseqno)) {
			errmsg("帳戶流水號: 不可空白");
			return rc;
		}
		hsms.initData();
		hsms.pseqno = aPseqno;
		hsms.phoneFlag = "N";
		hsms.msgPgm = "RSKM0920-DOWN";

		// -A.sms_msg_id-
		selectSmsMsgId(hsms.msgPgm);
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;

		// -B.idno-
		selectCrdIdno();
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 0;

		// -acct_type-
		String lsAcctType = colStr("B.acct_type");
		if (isSendAcctType(lsAcctType) == false) {
			return 0;
		}
		// --
		strSql = "select count(*) as db_cnt" + " from sms_msg_dtl" + " where crt_date = to_char(sysdate,'yyyymmdd')"
				+ " and msg_pgm = ? " + " and id_p_seqno = ? " + " and cellar_phone = ?";
		sqlSelect(strSql, new Object[] { hsms.msgPgm, colStr("B.id_p_seqno"), colStr("B.cellar_phone") });
		if (colNum("db_cnt") > 0) {
			return 0;
		}

		String colName = colStr("A.msg_userid") + "," + colStr("A.msg_id") + "," + colStr("B.cellar_phone") + ","
				+ colStr("B.chi_name");
		hsms.msgDesc = colName;

		insertMsgDtl(colName);
		return rc;
	}

	public int rskP0922(String aPseqno, String aMajroIdPSeqno, String msgPgm) {
		// -sms_msg_id:766-
		if (empty(aMajroIdPSeqno)) {
			errmsg("正卡身分證流水號: 不可空白");
			return rc;
		}
		hsms.initData();
		hsms.idPseqno = aMajroIdPSeqno;
		hsms.phoneFlag = "N";
		if (empty(msgPgm) == false) {
			hsms.msgPgm = msgPgm;
		} else {
			hsms.msgPgm = "RSKM0922";
		}

		// -A.sms_msg_id-
		selectSmsMsgId(hsms.msgPgm);
		log("rc :" + rc);
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;

		// -B.idno-
		selectCrdIdno();

		if (rc != 1)
			return rc;
		if (isSend == false)
			return 0;

		// -acct_type-
		if (isSendAcctType(hsms.acctType) == false) {
			return 0;
		}
		// --
		strSql = "select count(*) as db_cnt" + " from sms_msg_dtl" + " where crt_date = to_char(sysdate,'yyyymmdd')"
				+ " and msg_pgm = ? " + " and id_p_seqno = ? " + " and cellar_phone = ? and proc_flag <> 'Y' ";
		sqlSelect(strSql, new Object[] { hsms.msgPgm, hsms.idPseqno, hsms.cellarPhone });
		if (colNum("db_cnt") > 0) {
			return 0;
		}

		String colName = colStr("A.msg_userid") + "," + colStr("A.msg_id") + "," + colStr("B.cellar_phone") + ","
				+ colStr("B.chi_name");

		hsms.msgDesc = colName;
		insertMsgDtl(colName);
		return rc;
	}
	
	public int rskP2400(String aIdPseqno,String msgPgm,String blockCode) {
		if (empty(aIdPseqno)) {
			errmsg("身分證流水號: 不可空白");
			return rc;
		}
		hsms.initData();
		hsms.idPseqno = aIdPseqno;
		hsms.phoneFlag = "N";
		if (empty(msgPgm) == false) {
			hsms.msgPgm = msgPgm;
		} else {
			hsms.msgPgm = "RSKP2400";
		}

		// -A.sms_msg_id-
		selectSmsMsgId(hsms.msgPgm);		
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;

		// -B.idno-
		selectCrdIdno();

		if (rc != 1)
			return rc;
		if (isSend == false)
			return 0;

		// -acct_type-
		if (isSendAcctType(hsms.acctType) == false) {
			return 0;
		}
		// --
		strSql = "select count(*) as db_cnt" + " from sms_msg_dtl" + " where crt_date = to_char(sysdate,'yyyymmdd')"
				+ " and msg_pgm = ? " + " and id_p_seqno = ? " + " and cellar_phone = ? and proc_flag <> 'Y' ";
		sqlSelect(strSql, new Object[] { hsms.msgPgm, hsms.idPseqno, hsms.cellarPhone });
		if (colNum("db_cnt") > 0) {
			return 0;
		}
		
		//--取凍結原因中文
		strSql = "select wf_desc as block_desc from ptr_sys_idtab where wf_type ='REVIEW_BLOCK' and wf_id = ? ";
		sqlSelect(strSql,new Object[] {blockCode});
		if(sqlRowNum <=0) {
			hsms.errorDesc = "凍結原因中文沒有設定";
			return -1;
		}
			
		
		String colName = colStr("A.msg_userid") + "," + colStr("A.msg_id") + "," + colStr("B.cellar_phone")+","+colStr("block_desc");				

		hsms.msgDesc = colName;
		insertMsgDtl(colName);
		return rc;
	}
	
	public int rskP2070Action(String aIdPseqno, String aActionCode) {
		msgOK();
		if (empty(aIdPseqno)) {
			errmsg("卡人流水號: 不可空白");
			return rc;
		}
		hsms.initData();
		hsms.idPseqno = aIdPseqno;
		hsms.phoneFlag = "N";
		hsms.msgPgm = "RSKP2070-" + aActionCode;

		// -A.sms_msg_id-
		selectSmsMsgId(hsms.msgPgm);
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;

		// -check block_reason<>'' 不發送-
		strSql = "select count(*) as db_cnt" + " from cca_card_acct" + " where 1=1 and id_p_seqno = ?" 
				+ " and ( block_reason2 not in ('','61','71','72','73','74','81','82')"
				+ "     or block_reason3 not in ('','61','71','72','73','74','81','82')"
				+ "     or block_reason4 not in ('','61','71','72','73','74','81','82')"
				+ "     or block_reason5 not in ('','61','71','72','73','74','81','82')" + "     )";
		sqlSelect(strSql, new Object[] {aIdPseqno});
		if (colNum("db_cnt") > 0) {
			return 0;
		}
		// -B.idno-
		selectCrdIdno();
		if (rc != 1)
			return rc;

		// -不重覆發送-
		if (!colEmpty("B.cellar_phone")) {
			if (checkDupl(aIdPseqno, colStr("A.msg_id")) > 0)
				return 0;
		}

		String ss = colStr("A.msg_userid") + "," + colStr("A.msg_id") + "," + colStr("B.cellar_phone") + ","
				+ colStr("B.chi_name") + "," + varsNum("delay_day") + "," + (varsNum("delay_day") - 1);
		hsms.msgDesc = ss;
		insertMsgDtl(ss);
		if (sqlRowNum == 0) {
			errmsg("新增簡訊資料失敗 (期中覆審.延遲簡訊)");
		}
		return rc;
	}

	int checkDupl(String aIdPseqno, String aMsgId) {
		// if (col_ss("B.cellar_phone").length()>0) {
		strSql = "select count(*) as db_cnt from sms_msg_dtl" + " where id_p_seqno =?" + " and msg_id =?"
				+ " and 'Y' in (send_flag,resend_flag)" + " and proc_flag <>'Y'";
		setString2(1, aIdPseqno);
		setString(aMsgId);
		sqlSelect(strSql);
		return colInt("db_cnt");
	}

	public int cmsP2130Addr(String aAcctKey, String aAcctType) {
		String lsPSeqno = "";
		if (empty(aAcctType)) {
			aAcctType = "01";
		}

		strSql = "select acno_p_seqno from act_acno" + " where acct_key =? and acct_type =?";
		sqlSelect(strSql, new Object[] { aAcctKey, aAcctType });
		if (sqlRowNum > 0) {
			lsPSeqno = colStr("acno_p_seqno");
		}

		return cmsP2130Addr(lsPSeqno);
	}

	public int cmsP2130Addr(String aPseqno) {
		if (empty(aPseqno)) {
			errmsg("帳戶流水號: 不可空白");
			return rc;
		}
		hsms.initData();
		hsms.pseqno = aPseqno;
		hsms.phoneFlag = "N";
		hsms.msgPgm = "W_CMSP2130";

		// -A.sms_msg_id-
		selectSmsMsgId(hsms.msgPgm);
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 1;

		// -B.idno-
		selectCrdIdno();
		if (rc != 1)
			return rc;
		if (isSend == false)
			return 0;

		// -acct_type-
		if (isSendAcctType(hsms.acctType) == false) {
			return 0;
		}
		// -02/03一律送簡訊, 01/05/06只送一封-
		if (pos("|02|03", hsms.acctType) <= 0 && eqIgno(hsms.phoneFlag, "Y")) {
			strSql = "select count(*) as db_cnt" + " from sms_msg_dtl" + " where crt_date = to_char(sysdate,'yyyymmdd')"
					+ " and msg_pgm = 'W_CMSP2130' " + " and id_p_seqno = ? " + " and cellar_phone = ?";
			sqlSelect(strSql, new Object[] { colStr("B.id_p_seqno"), colStr("B.cellar_phone") });
			if (colNum("db_cnt") > 0) {
				return 0;
			}
		}

		String colName = colStr("A.msg_userid") + "," + colStr("A.msg_id") + "," + colStr("B.cellar_phone") + ","
				+ colStr("B.chi_name");
		if (eqAny(hsms.acctType, "02")) {
			colName += ",商務";
		} else if (eqAny(hsms.acctType, "03")) {
			colName += ",採購";
		}
		hsms.msgDesc = colName;
		insertMsgDtl(colName);
		if (sqlRowNum == 0) {
			errmsg("新增簡訊資料失敗 (帳單地址)");
		}
		return rc;
	}

	boolean isSendAcctType(String strName) {
		// -全部-
		if (colEq("A.acct_type_sel", "0"))
			return true;

		if (empty(strName)) {
			return false;
		}

		strSql = "select count(*) as db_cnt from sms_dtl_data" + " where table_name='SMS_MSG_ID'" + " and data_key =?"
				+ " and data_type ='1' and data_code =?";
		sqlSelect(strSql, new Object[] { hsms.msgPgm, strName });
		// -指定-
		if (colEq("A.acct_type_sel", "1") && colNum("db_cnt") > 0) {
			return true;
		}
		// -排除-
		if (colEq("A.acct_type_sel", "2") && colNum("db_cnt") <= 0) {
			return true;
		}

		errmsg("帳戶類別暫不發簡訊, 帳戶類別=" + strName);
		return false;
	}

	public int insertMsgDtl(String a1MsgDesc) {
		strSql = "select " + sqlMsgSeqno + " as sms_seqno from" + commSqlStr.sqlDual;
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			hsms.msgSeqno = colStr("sms_seqno");
		}

		sp.sql2Insert("sms_msg_dtl");
		if (empty(hsms.msgSeqno)) {
			sp.addsqlParm(" msg_seqno", sqlMsgSeqno);
		} else {
			sp.addsqlParm(" msg_seqno", " ?", hsms.msgSeqno);
		}
		sp.addsqlParm(", msg_dept", ",?", hsms.msgDept);
		sp.addsqlParm(", msg_userid", ",?", hsms.msgUserid);
		sp.addsqlParm(", msg_pgm", ",?", hsms.msgPgm);
		sp.addsqlParm(", id_p_seqno", ",?", hsms.idPseqno);
		sp.addsqlParm(", p_seqno", ",?", hsms.pseqno);
		sp.addsqlParm(", id_no", ",?", hsms.idNo);
		sp.addsqlParm(", acct_type", ",?", hsms.acctType);
		sp.addsqlParm(", msg_id", ",?", hsms.msgId);
		sp.addsqlParm(", msg_desc", ",?", a1MsgDesc);
		sp.addsqlParm(", cellar_phone", ",?", hsms.cellarPhone);
		sp.addsqlParm(", cellphone_check_flag", ",?", hsms.phoneFlag);
		sp.addsqlParm(", chi_name", ",?", hsms.chiName);
		sp.addsqlParm(", add_mode", ",'B'");
		sp.addsqlParm(", send_flag", ",'Y'");
		sp.addsqlParm(", crt_date", "," + commSqlStr.sysYYmd);
		sp.addsqlParm(", crt_user", ",?", modUser);
		sp.addsqlParm(", apr_date", "," + commSqlStr.sysYYmd);
		sp.addsqlParm(", apr_user", ",?", modUser);
		sp.addsqlParm(", apr_flag", ",'Y'");
		sp.modxxx(modUser, modPgm);

		// ddd(sp.sql_stmt(),sp.sql_parm());
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			errmsg("insert sms_msg_dtl error, kk[%s]", hsms.msgPgm);
		}
		return rc;
	}

	// void insert_msg_dtl24(String a_msg_desc) {
	// sp.sql2Insert("sms_msg_dtl24");
	// sp.aaa("msg_dept","?",col_ss("A.msg_dept"));
	// sp.aaa(", msg_userid",",?", col_ss("A.msg_userid"));
	// sp.aaa(", card_no",",?",wp.sss("card_no"));
	// sp.aaa(", id_no",",?",wp.sss("id_no"));
	// sp.aaa(", id_p_seqno",",?",_id_p_seqno);
	// sp.aaa(", p_seqno",",?",kk_pseqno);
	// sp.aaa(", msg_id",",?",col_ss("A.msg_id"));
	// sp.aaa(", cellar_phone",",?",_cellar_phone);
	// sp.aaa(", sms24_flag",", 'Y'");
	// sp.aaa(", crt_date",","+commSqlStr .sys_YYmd);
	// sp.aaa(", crt_time",","+commSqlStr .sys_Time);
	// sp.aaa(", crt_user",",?",mod_user);
	// sp.mod_XXX(mod_user, mod_pgm);
	//
	// sqlExec(sp.sql_stmt(),sp.sql_parm());
	// if (sql_nrow <=0) {
	// errmsg("insert sms_msg_dtl24 error, kk[%s]",h_sms.msg_pgm);
	// }
	// return;
	// }

	void selectCcaCardAcct(double aAcctIdx) {
		strSql = "select id_p_seqno, acno_p_seqno" + " from cca_card_acct" + " where card_acct_idx =?";
		setDouble(aAcctIdx);
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			errmsg("查無授權帳戶資料[cca_card_acct], kk[%s]", aAcctIdx);
			return;
		}

		hsms.idPseqno = colStr("id_p_seqno");
		hsms.pseqno = colStr("acno_p_seqno");
		selectCrdIdno();
	}

	void selectCrdIdno() {
		this.daoTid = "B.";
		if (empty(hsms.idPseqno) == false) {
			strSql = "select A.id_no, A.chi_name " + ", A.cellar_phone, A.id_p_seqno " + ", B.p_seqno, B.acct_type"
					+ " from crd_idno A, act_acno B" + " where A.id_p_seqno = B.id_p_seqno" + " and A.id_p_seqno =?"
					+ commSqlStr.rownum(1);
			setString(1, hsms.idPseqno);
		} else if (empty(hsms.pseqno) == false) {
			strSql = "select A.id_no, A.chi_name " + ", A.cellar_phone, A.id_p_seqno " + ", B.p_seqno, B.acct_type"
					+ " from crd_idno A, act_acno B" + " where A.id_p_seqno = B.id_p_seqno" + " and B.acno_p_seqno =?"
					+ commSqlStr.rownum(1);
			setString(1, hsms.pseqno);
		} else {
			errmsg("id_p_seqno, p_seqno: 不可空白");
			return;
		}
		sqlSelect(strSql);
		if (sqlRowNum != 1) {
			errmsg("查無持卡人基本資料");
			return;
		}

		hsms.phoneFlag = "N";
		if (colStr("B.cellar_phone").length() == 10 && colNum("B.cellar_phone") > 0) {
			hsms.phoneFlag = "Y";
		}

		hsms.cellarPhone = colStr("B.cellar_phone");
		if (empty(hsms.idPseqno))
			hsms.idPseqno = colStr("B.id_p_Seqno");
		hsms.idNo = colStr("B.id_no");
		hsms.acctType = colStr("B.acct_type");
		hsms.chiName = colStr("B.chi_name");
	}

	void selectSmsMsgId(String msgPgm) {
		if (empty(msgPgm)) {
			errmsg("使用程式代碼: 不可空白");
			return;
		}

		isSend = false;

		this.daoTid = "A.";
		strSql = "select * from sms_msg_id" + " where msg_pgm =?"
		// +" and apr_date <>''"
		;
		sqlSelect(strSql, new Object[] { msgPgm });
		if (sqlRowNum <= 0) {
			errmsg("smsm0010: 簡訊參數未設定, 使用程式代碼=" + msgPgm);
			return;
		}

		isSend = eqIgno(colStr("A.msg_send_flag"), "Y");

		hsms.msgDept = colStr("A.msg_dept");
		hsms.msgUserid = colStr("A.msg_userid");
		hsms.msgPgm = msgPgm;
		hsms.msgId = colStr("A.msg_id");
	}
	
	public class MsgDtl {
		public String msgSeqno = "";
		public String msgDept = "";
		public String msgUserid = "";
		public String msgPgm = "";
		public String idPseqno = "";
		public String pseqno = "";
		public String idNo = "";
		public String acctType = "";
		public String cardNo = "";
		public String msgId = "";
		public String cellarPhone = "";
		public String phoneFlag = "";
		public String chiName = "";
		public String exId = "";
		public String msgDesc = "";
		public double minPay = 0;
		public String addMode = "";
		public String resendFlag = "";
		public String sendFlag = "";
		public String priorFlag = "";
		public String createTxtDate = "";
		public String createTxtTime = "";
		public String chiNameFlag = "";
		public String procFlag = "";
		public String sms24Flag = "";
		public String crtDate = "";
		public String crtUser = "";
		public String aprDate = "";
		public String aprUser = "";
		public String aprFlag = "";
		public String errorDesc = "";
		
		public void initData() {
			msgSeqno = "";
			msgDept = "";
			msgUserid = "";
			msgPgm = "";
			idPseqno = "";
			pseqno = "";
			idNo = "";
			acctType = "";
			cardNo = "";
			msgId = "";
			cellarPhone = "";
			phoneFlag = "";
			chiName = "";
			exId = "";
			msgDesc = "";
			minPay = 0;
			addMode = "";
			resendFlag = "";
			sendFlag = "";
			priorFlag = "";
			createTxtDate = "";
			createTxtTime = "";
			chiNameFlag = "";
			procFlag = "";
			sms24Flag = "";
			crtDate = "";
			crtUser = "";
			aprDate = "";
			aprUser = "";
			aprFlag = "";
			errorDesc = "";
		}
	}

}
