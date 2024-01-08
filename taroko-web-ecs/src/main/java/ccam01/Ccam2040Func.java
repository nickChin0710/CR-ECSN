package ccam01;

/** 解凍、凍結(卡戶)
 * 2019-1230   JH    acno_Block: busi.func >>ecsfunc
 * 2019-0606:  JH    p_seqno >> acno_p_seqno
   2018-1213:  JH    rsk_acnolog
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
  * 109-12-30  V1.00.01  shiyuqi       修改无意义命名
* 110-01-05  V1.00.02  Tanwei     zzDate,zzStr,commBusi,zzCurr變量更改         *   *  
* 111-11-14:   Alex  解除凍結時 outgoing送法修正 , callAutoAuth變數設定為false 不從畫面直接call授權
 * */
import busi.FuncAction;
import busi.func.OutgoingBlock;
import busi.func.AcnoBlockReason;

public class Ccam2040Func extends FuncAction {
	busi.CommBusi commBusi = new busi.CommBusi();
	taroko.base.CommDate commDate = new taroko.base.CommDate();

	double cardAcctIdx = 0;
	String isPSeqno = "", acnoPSeqno = "", debitFlag = "";
	boolean ibDebit = false;
	String isBlock15 = "";
	busi.func.OutgoingBlock ooOutgo = null;

	@Override
	public void dataCheck() {
		cardAcctIdx = wp.itemNum("card_acct_idx");
		acnoPSeqno = wp.itemStr("acno_p_seqno");
		debitFlag = wp.itemStr("debit_flag");
		ibDebit = commBusi.isDebit(debitFlag);
		isPSeqno = acnoPSeqno;

		isBlock15 = wp.itemStr("block_reason1") + wp.itemStr("block_reason2") + wp.itemStr("block_reason3")
				+ wp.itemStr("block_reason4") + wp.itemStr("block_reason5");
		String lsSpec = wp.itemStr("spec_status");

		if (!ibDelete && empty(isBlock15) && empty(lsSpec)) {
			errmsg("凍結碼, 戶特指: 不可空白");
			return;
		}
		if (!ibDelete) {
			if (!wp.itemEmpty("spec_status")) {
				if (wp.itemEmpty("spec_del_date")) {
					errmsg("戶特指刪除日期:不可空白");
					return;
				}
			}

			if (!wp.itemEmpty("spec_del_date")) {
				if (this.getSysDate().compareTo(wp.itemStr("spec_del_date")) > 0) {
					errmsg("戶特指刪除日期需大於系統日");
					return;
				}

				if (wp.colEmpty("spec_status")) {
					errmsg("戶特指:不可空白");
					return;
				}

			}
		}

		selectCcaCardAcct();
		if (rc != 1)
			return;

		// -add,Update-
		/*
		 * if (ib_debit && !ib_delete) { if (wp.item_empty("acct_no")) {
		 * errmsg("DEBIT卡之金融帳號不可為空白"); return; } if ( !empty(is_block15)) {
		 * errmsg("VD金融卡不可指定凍結碼"); return; } if (wp.item_eq("nocancel_credit_flag", "Y")
		 * && wp.item_eq("spec_code", "51")) { errmsg("VISA金融卡 不可取消簽帳消費功能"); return; }
		 * if (wp.item_eq("spec_code", "51") == false) { errmsg("VISA金融卡: 戶特指須=51");
		 * return; } }
		 */

		if (!ibDebit) {

			if (selectActAcno() != 1) {
				errmsg("select act_acno err; kk[%s]", acnoPSeqno);
				return;
			}

			if (ibDelete) {
				if (colEq("acno.no_unblock_flag", "Y") && !empty(isBlock15)) {
					String noUnblockSdate = colStr("acno.no_unblock_s_date");
					String noUnblockSdate1 = colNvl("acno.no_unblock_e_date", "99991231");
					if (commDate.sysComp(noUnblockSdate) >= 0 && commDate.sysComp(noUnblockSdate1) <= 0) {
						errmsg("此卡戶為永不解凍戶, 不可[解凍]!");
						return;
					}
				}
				return;
			}

			if (colEq("acno.no_block_flag", "Y") && !empty(isBlock15)) {
				String noBlockSDate = colStr("acno.no_block_s_date");
				String noBlockEDate = colNvl("acno.no_block_e_date", "99991231");
				if (commDate.sysComp(noBlockSDate) >= 0 && commDate.sysComp(noBlockEDate) <= 0) {
					errmsg("此卡戶為永不凍結戶, 不可凍結!");
					return;
				}
			}

		}

		String blockReason1 = wp.itemStr2("block_reason1");
		if (notEmpty(blockReason1) && blockReason1.substring(1, 2).equals("1") == false) {
			errmsg("凍結原因1: 之個位數字必需為'1'");
			return;
		}

	}

	void selectCcaCardAcct() {
		strSql = "select mod_seqno" + ", block_reason1, block_reason2, block_reason3, block_reason4, block_reason5"
				+ ", spec_status" + " from cca_card_acct" + " where acno_p_seqno =? ";

		if (!ibDebit) {
			strSql += " and debit_flag <>'Y' ";
		} else {
			strSql += " and debit_flag ='Y' ";
		}

		setString2(1, acnoPSeqno);
		// ppp(2,debitFlag);

		daoTid = "A.";
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			sqlErr("cca_card_acct.select");
			return;
		}

		if (colNum("A.mod_seqno") != wp.itemNum("mod_seqno")) {
			errmsg(errOtherModify);
			return;
		}

		return;
	}

	int selectActAcno() {
		if (empty(isPSeqno)) {
			return -1;
		}

		daoTid = "acno.";
		strSql = "select no_block_flag ," + " no_block_s_date ," + " no_block_e_date, " + " no_unblock_flag ,"
				+ " no_unblock_s_date ," + " no_unblock_e_date " + " from act_acno" + " where acno_p_seqno =?";
		sqlSelect(strSql, new Object[] { isPSeqno });

		return sqlRowNum;
	}

	@Override
	public int dbInsert() {
		// dataCheck();
		// if (rc != 1) {
		// return rc;
		// }
		// is_sql = "update CCA_CARD_ACCT set "
		// + " block_status =:block_status,"
		// + " block_reason1 =:block_reason1,"
		// + " block_reason2 =:block_reason2,"
		// + " block_reason3 =:block_reason3,"
		// + " block_reason4 =:block_reason4,"
		// + " block_reason5 =:block_reason5,"
		// +" spec_status =:spec_status,"
		// + " spec_remark = '卡戶凍結(禁超)',"
		// + " spec_user =:spec_user,"
		// + " spec_del_date ='',"
		// + " spec_date = to_char(sysdate,'yyyymmdd'),"
		// +commSqlStr.setMod_xxx(mod_user, mod_pgm)
		// + " where p_seqno =:acnoPSeqno "
		// + " and debit_flag=:debitFlag "
		// + " and nvl(mod_seqno,0) =:mod_seqno ";
		// ;
		// if (wp.item_empty("block_reason1")
		// && wp.item_empty("block_reason2")
		// && wp.item_empty("block_reason3")
		// && wp.item_empty("block_reason4")
		// && wp.item_empty("block_reason5")) {
		// ppp("block_status", "N");
		// }
		// else {
		// ppp("block_status", "Y");
		// }
		//
		// ppp("block_reason1",wp.item_ss("block_reason1"));
		// ppp("block_reason2",wp.item_ss("block_reason2"));
		// ppp("block_reason3",wp.item_ss("block_reason3"));
		// ppp("block_reason4",wp.item_ss("block_reason4"));
		// ppp("block_reason5",wp.item_ss("block_reason5"));
		// ppp("spec_status",wp.item_ss("spec_status"));
		// ppp("spec_user",wp.item_ss("spec_user"));
		// ppp("acnoPSeqno", acnoPSeqno);
		// ppp("debitFlag", debitFlag);
		// ppp("mod_seqno",wp.item_num("mod_seqno"));
		//
		// sqlExec(is_sql);
		// if (sql_nrow <= 0) {
		// errmsg("update cca_card_acct error; kk[%s]",acnoPSeqno);
		// return rc;
		// }
		//
		// oo_outgo=new busi.func.OutgoingBlock();
		// oo_outgo.setConn(wp);
		// if (oo_outgo.block_Update(is_p_seqno,"")==-1) {
		// errmsg(oo_outgo.getMsg());
		// }
		//
		return rc;
	}

	@Override
	public int dbUpdate() {
		dataCheck();
		if (rc != 1) {
			return rc;
		}

		String[] aaBlock = new String[] { wp.itemStr2("block_reason1"), wp.itemStr2("block_reason2"),
				wp.itemStr2("block_reason3"), wp.itemStr2("block_reason4"), wp.itemStr2("block_reason5") };

		strSql = "update CCA_CARD_ACCT set " + " block_status =:block_status," + " block_reason1 =:block_reason1,"
				+ " block_reason2 =:block_reason2," + " block_reason3 =:block_reason3,"
				+ " block_reason4 =:block_reason4," + " block_reason5 =:block_reason5,"
				+ " block_date =:block_date ," + "unblock_date =:unblock_date ,"
				+ " block_sms_flag =:block_sms_flag ," + " spec_status =:spec_status," + " spec_remark =:spec_remark,"
				+ " spec_del_date =:spec_del_date," + " spec_user =:spec_user," + " spec_date =:spec_date,"
				+ commSqlStr.setModxxx(modUser, modPgm) + " where acno_p_seqno =:acnoPSeqno";

		if (!ibDebit) {
			strSql += " and debit_flag <>'Y' ";
		} else {
			strSql += " and debit_flag ='Y' ";
		}

		if (empty(isBlock15)) {
			setString2("block_status", "N");
			setString2("block_date","");
			setString2("unblock_date",getSysDate());
		} else {
			setString2("block_status", "Y");
			setString2("block_date",getSysDate());
			setString2("unblock_date","");
		}

		setString2("block_reason1", aaBlock[0]);
		setString2("block_reason2", aaBlock[1]);
		setString2("block_reason3", aaBlock[2]);
		setString2("block_reason4", aaBlock[3]);
		setString2("block_reason5", aaBlock[4]);
		setString2("block_sms_flag", wp.itemNvl("block_sms_flag", "N"));
		setString2("spec_status", wp.itemStr2("spec_status"));
		setString2("spec_remark", wp.itemStr2("spec_remark"));
		setString2("spec_del_date", wp.itemStr2("spec_del_date"));
		setString2("acnoPSeqno", acnoPSeqno);
		// ppp("debitFlag", debitFlag);
		if (!wp.itemEmpty("spec_user")) {
			setString2("spec_user", wp.itemStr2("spec_user"));
			setString2("spec_date", wp.itemStr2("spec_date"));
		} else {
			if(wp.itemEmpty("spec_status")) {
				setString2("spec_user", "");
				setString2("spec_date", "");
			}	else	{
				setString2("spec_user", modUser);
				setString2("spec_date", this.getSysDate());	
			}			
		}

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("凍結戶資料失敗[CCA_CARD_ACCT]; kk[%s]", acnoPSeqno);
			return rc;
		}

		// -insert Rsk_acnolog-
		AcnoBlockReason ooAclg = new AcnoBlockReason();
		ooAclg.setConn(wp);
		if (ooAclg.ccaM2040Update(cardAcctIdx) != 1) {
			errmsg(ooAclg.getMsg());
			return rc;
		}

		if (!ibDebit) {
			Onbat2ecs onbat = new Onbat2ecs();
			onbat.setConn(wp);
			onbat.ccam2040CardBlock("N", isPSeqno, aaBlock);
		}

		cardOutgoing();
		updateCrdCard();

		return rc;
	}

	private void updateCrdCard() {
		if (!ibDebit) {
			strSql = "update crd_card set" + " block_code =?, block_date =?" + "," + commSqlStr.setModxxx(modUser, modPgm)
					+ " where acno_p_seqno =?";
		} else {
			strSql = "update dbc_card set" + " block_code =?, block_date =?" + "," + commSqlStr.setModxxx(modUser, modPgm)
					+ " where p_seqno =?";
		}

		setString2(1, isBlock15);
		if (empty(isBlock15))
			setString2(2, "");
		else
			setString2(2, wp.sysDate);
		setString2(3, isPSeqno);
		sqlExec(strSql);
		if (sqlRowNum < 0) {
			sqlErr("crd[dbc]_card.update[block]");
		}
		return;
	}

	void cardOutgoing() {
		ooOutgo = new OutgoingBlock();
		ooOutgo.setConn(wp);
		ooOutgo.isCallAutoAuth = false;
		// -VD有可能凍結,特指-
		// 2018-1107 if(ib_debit) return ;
		String lsVdFlag = (ibDebit ? "Y" : "N");
		if (ibDelete) {
			if (notEmpty(isBlock15)) {
				wp.itemSet("spec_remark", "卡戶解凍(解超)");
			}

			ooOutgo.cardOutgoingDelete(lsVdFlag, isPSeqno);
			return;
		}

		String lsSpecCode = wp.itemStr2("spec_status");
		if (!wp.itemEmpty("block_reason5"))
			lsSpecCode = wp.itemStr2("block_reason5");
		if (!wp.itemEmpty("block_reason4"))
			lsSpecCode = wp.itemStr2("block_reason4");
		if (!wp.itemEmpty("block_reason3"))
			lsSpecCode = wp.itemStr2("block_reason3");
		if (!wp.itemEmpty("block_reason2"))
			lsSpecCode = wp.itemStr2("block_reason2");
		if (!wp.itemEmpty("block_reason1"))
			lsSpecCode = wp.itemStr2("block_reason1");
		if (empty(lsSpecCode))
			return;

		if (notEmpty(isBlock15)) {
			wp.itemSet("spec_del_date", "");
			wp.itemSet("spec_mst_vip_amt", "0");
			wp.itemSet("spec_remark", "凍結(禁超)");
		}
		ooOutgo.cardOutgoingUpdate(lsVdFlag, isPSeqno, lsSpecCode);
	}

	@Override
	public int dbDelete() {
		dataCheck();
		if (rc != 1) {
			return rc;
		}

		strSql = "update CCA_CARD_ACCT set " + " block_status ='N'," + " block_reason1 =''," + " block_reason2 ='',"
				+ " block_reason3 =''," + " block_reason4 =''," + " block_reason5 =''," + " spec_status ='',"
				+ " spec_remark = '卡戶解凍(解超)'," + " spec_user =''," + " spec_del_date = ''," + " spec_date = '', "
				+ " block_sms_flag = 'N' , unblock_date = to_char(sysdate,'yyyymmdd') , block_date = '' ," 
				+ commSqlStr.setModxxx(modUser, modPgm) + " where acno_p_seqno =:acnoPSeqno ";

		if (!ibDebit) {
			strSql += " and debit_flag <>'Y' ";
		} else {
			strSql += " and debit_flag ='Y' ";
		}

		setString2("acnoPSeqno", acnoPSeqno);
		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}

		// -outgoing-
		cardOutgoing();

		// -update.crd_card-
		if (!ibDebit) {
			strSql = "update crd_card set" + " block_code =''" + ", block_date =''" + ","
					+ commSqlStr.setModxxx(modUser, modPgm) + " where acno_p_seqno =?";
		} else {
			strSql = "update dbc_card set" + " block_code =''" + ", block_date =''" + ","
					+ commSqlStr.setModxxx(modUser, modPgm) + " where p_seqno =?";
		}

		setString2(1, isPSeqno);
		sqlExec(strSql);
		if (sqlRowNum < 0) {
			sqlErr("crd_card.update[unBlock]");
			return rc;
		}

		if (!ibDebit) {
			// -onbat_2ecs-
			Onbat2ecs onbat = new Onbat2ecs();
			onbat.setConn(wp);
			onbat.ccam2040CardUnBlock("N", isPSeqno);
		}

		// -insert Rsk_acnolog-
		AcnoBlockReason ooAclg = new AcnoBlockReason();
		ooAclg.setConn(wp);
		if (ooAclg.ccaM2040Delete(cardAcctIdx) != 1) {
			errmsg(ooAclg.getMsg());
			return rc;
		}

		return rc;
	}

	@Override
	public int dataProc() {
		busi.func.SmsMsgDetl sms = new busi.func.SmsMsgDetl();
		sms.setConn(wp);

		rc = sms.ccaM2040Sms();
		if (rc != 1) {
			errmsg("發送簡訊失敗 , " + sms.getMsg());
			return rc;
		}

		strSql = " update cca_card_acct set " + " block_sms_flag ='Y' " + " where acno_p_seqno =? ";
		if (!ibDebit) {
			strSql += " and debit_flag <>'Y' ";
		} else {
			strSql += " and debit_flag ='Y' ";
		}

		setString2(1, wp.itemStr("acno_p_seqno"));
		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg(sqlErrtext);
			return rc;
		}

		return rc;
	}

}
