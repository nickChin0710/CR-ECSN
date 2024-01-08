/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 * 2020-0409    V1.00.00  JH      set_block_reason()                           *
 * 2020-0121    V1.00.01  JH      onbat_2xxx.mod_pgm                           *
 * 2020-0108    V1.00.02  JH      Parm2sql()                                   *
 * 2019-0606:   V1.00.03  JH      acno_p_seqno                                 *
 *  109/11/16   V1.00.04  Zuwei   rename from   Rsk_block                      *
 *  109-11-24   V1.00.05  tanwei  updated for project coding standard          *
 * 2022-0719    V1.00.06  Alex    卡凍結取消									   *
 ******************************************************************************/
package Rsk;
/** 凍結碼公用程式
 
 *
 * */

import com.BaseBatch;
import java.sql.Connection;
import java.util.Arrays;

public class RskBlock extends BaseBatch {

public String procBlock="", firstBlock="Y", blockFlag="N";

public String[] blockReason=new String[5];
public String[] oriBlock=new String[6];
public String specStatus="";
public String specRemark="";

private int matchFlag=0;
private double iiCardAcctIdx=0;

//Table-ID-------------------------------------------------------
private int tidActAcnoS1=-1;
private int tidCardAcctU=-1;
private int tidCrdCard=-1;
private int tidCrdCardU=-1;

public RskBlock(Connection conn[],String[] dbAlias) throws Exception {
  super.conn  = conn;
  setDBalias(dbAlias);
  setSubParm(dbAlias);

  return;
}

public void setModXxx(String aBusiDate, String aModUser, String aModPgm) {
	try {
		hBusiDate =aBusiDate;
		hModUser =aModUser;
		hModPgm =aModPgm;
		if (empty(hBusiDate)) {
				this.selectPtrBusinday();
		}
	}
	catch (Exception ex) {}
}

int selectActAcno(String aPSeqno) throws Exception {
//	tid_act_acno_s1 = gettid("select_act_acno");
	if (tidActAcnoS1 <= 0) {
		sqlCmd = "select A.acno_p_seqno ,"
			+ " A.id_p_seqno ,"
			+ " A.acct_type ,"
			+ " A.acct_key ,"
			+ " A.card_indicator ,"
			+ " B.block_reason1 , "
			+ " B.block_reason2 , "
			+ " B.block_reason3 , "
			+ " B.block_reason4 , "
			+ " B.block_reason5 , "
			+ " uf_spec_status(B.spec_status,B.spec_del_date) as spec_status , "
         +" B.card_acct_idx, "
			+ " hex(B.rowid) as cca_rowid "
			+ " from act_acno A join cca_card_acct B on A.acno_p_seqno =B.acno_p_seqno and B.debit_flag<>'Y'"
			+ " where 1=1"
			+ " and A.acno_p_seqno = ?"
			+ " and nvl(A.stop_status,'') <> 'Y' "
			+ " and A.acno_flag <>'Y'"
			+ " order by A.acct_type";
		tidActAcnoS1 = ppStmtCrt("tid_act_acno_s1","");
	}
	ppp(1, aPSeqno);

	daoTid = "acno.";
	sqlSelect(tidActAcnoS1);
	if (sqlNrow <0) {
		errmsg("select_act_acno error, kk[%s]",aPSeqno);
		return rc;
	}
	if (sqlNrow==0) {
		return 0;
	}
	
	blockReason[0] =colSs("acno.block_reason1");
	blockReason[1] =colSs("acno.block_reason2");
	blockReason[2] =colSs("acno.block_reason3");
	blockReason[3] =colSs("acno.block_reason4");
	blockReason[4] =colSs("acno.block_reason5");
	specStatus =colSs("acno.spec_status");

	iiCardAcctIdx =colNum("acno.card_acct_idx");

	//--
	for(int ii=0; ii<5; ii++)
		oriBlock[ii] =blockReason[ii];
	oriBlock[5] =specStatus;
	
	return 1;
}

public int badAnnou(String aPSeqno,String aBlock4) throws Exception {
	matchFlag =4;
	procBlock ="";
	blockFlag ="N";

	if (empty(aPSeqno)) {
		errmsg("[acno_p_seqno] 不可空白");
		return rc;
	}
	rc =selectActAcno(aPSeqno);
	if (rc !=1)
		return rc;

	int liBlock=setBlockReason(aBlock4);
	//-不須凍結-
	if (liBlock==0)
		return 0;

	blockFlag ="Y";
	blockReason[3] =procBlock;
	updateCcaCardAcct();
	if (rc==-1)
		return rc;
	
	insertRskAcnologA();
	if (rc==-1)
		return rc;
	
//	selectCrdCard(aPSeqno);
	return rc;
}
public int badAnnouSpec(String aPSeqno,String aSpec) throws Exception {
	matchFlag =6;
	if (empty(aPSeqno)) {
		errmsg("[acno_p_seqno] 不可空白");
		return rc;
	}
	if (empty(aSpec)) {
		errmsg("[spec_status] 不可空白");
		return rc;
	}
	rc =selectActAcno(aPSeqno);
	if (rc !=1)
		return rc;


	specStatus =aSpec;
	specRemark ="流通卡不良記錄通報";
	updateCcaCardAcctSpec(iiCardAcctIdx);
	if (rc==-1)
		return rc;

	insertRskAcnologA();
	if (rc==-1)
		return rc;

//	selectCrdCard(aPSeqno);

	procBlock =aSpec;
	firstBlock ="Y";

	return rc;
}

void updateCcaCardAcct() throws Exception {
	if (tidCardAcctU <= 0) {
		sqlCmd = "update cca_card_acct set "
			+ " block_reason2 = ? ,"
			+ " block_reason3 = ? ,"
			+ " block_reason4 = ? ,"
			+ " block_reason5 = ? ,"
			+ " block_status = 'Y' ,"
			+ " block_date = ? "
         + " where card_acct_idx =?"  //rowid = ? "
      ;
		tidCardAcctU = ppStmtCrt("tid_card_acct_U","");
	}
	ppp(1,blockReason[1]);
	ppp(blockReason[2]);
	ppp(blockReason[3]);
	ppp(blockReason[4]);
	ppp(hBusiDate);
	//--
	//this.ppRowId(colSs("acno.cca_rowid"));
   ppp(colNum("acno.card_acct_idx"));
   dddSql(tidCardAcctU);
	sqlExec(tidCardAcctU);
	if (sqlNrow <= 0) {
		errmsg("update cca_card_acct error idx[%s]"
         ,colSs("acno.card_acct_idx"));
	}
}

private int tiCardAcctU2=-1;
void updateCcaCardAcctSpec(double aAcctIndx) throws Exception {
/*SPEC_STATUS
SPEC_DATE
SPEC_USER
SPEC_DEL_DATE
SPEC_MST_VIP_AMT
SPEC_REMARK*/
	if (tiCardAcctU2 <= 0) {
		sqlCmd = "update cca_card_acct set "
				+ " spec_status = ? ,"
				+ " spec_date = ? ,"
				+ " spec_user = ? ,"
				+ " spec_del_date ='' ,"
				+ " spec_remark = ?"
				+modxxxSet(",")
				+ " where card_acct_idx =?"  //rowid = ? "
		;
		tiCardAcctU2 = ppStmtCrt("tid_card_acct_U","");
	}
	ppp(1,specStatus);
	ppp(hBusiDate);
	ppp(hModUser);
	ppp("");
	//--
	ppp(aAcctIndx);

	sqlExec(tiCardAcctU2);
	if (sqlNrow <= 0) {
		errmsg("update cca_card_acct error idx[%s]"
				,colSs("acno.card_acct_idx"));
	}
}

void selectCrdCard(String aPSeqno) throws Exception {
	if (tidCrdCard<=0) {
		sqlCmd ="select card_no, id_p_seqno, block_code, block_date"
				+" from crd_card"
				+" where acno_p_seqno =? and current_code ='0'";
		tidCrdCard =ppStmtCrt("tid_crd_card","");
	}
	ppp(1,aPSeqno);
	
	daoTid ="card.";
	sqlSelect(tidCrdCard);
	if (sqlNrow <=0)
		return;
	
	int llRow =sqlNrow;
	for(int ll=0; ll<llRow; ll++) {
		updateCrdCard(colSs("card.card_no"));
		insertOnbat2ccas();
	}
}

void updateCrdCard(String aCardNo) throws Exception {
	if (tidCrdCardU<=0) {
		sqlCmd ="update crd_card set"
				+" block_code =?"
				+", block_date =?"
				+","+commSqlStr.setModXxx(hModUser, hModPgm)
				+" where card_no =?";
		daoTable ="tid_crd_card_U";
		tidCrdCardU =ppStmtCrt();
	}
	ppp(1,blockReason[0]+blockReason[1]+blockReason[2]+blockReason[3]+blockReason[4]);
	ppp(hBusiDate);
	ppp(aCardNo);
	sqlExec(tidCrdCardU);
	if (sqlNrow <=0) {
		printf("update crd_card error, kk[%s]",aCardNo);
	}
}
//===============================================================
private com.Parm2sql ttOnbat=null;
void insertOnbat2ccas() throws Exception {
	// - ai_flag: 1.acno, 5.card: 只有卡片送CCAS
	String lsCardIndi = "";
	lsCardIndi = colSs("acno.card_indicator");	//-ACNO-
	String lsCardNo = colSs("card.card_no");		//-card-

	if (ttOnbat ==null) {
      ttOnbat = new com.Parm2sql();
      ttOnbat.insert("onbat_2ccas");
   }
	ttOnbat.aaa("trans_type","2");
	ttOnbat.aaa("to_which",2);
	ttOnbat.aaaFunc("dog",commSqlStr.sysDTime,"");
	ttOnbat.aaa("proc_mode","B");
	ttOnbat.aaa("proc_status", 0);
	ttOnbat.aaa("card_catalog",lsCardIndi);
	ttOnbat.aaa("acct_type",colSs("acno.acct_type"));
	ttOnbat.aaa("id_p_seqno",colSs("card.id_p_seqno"));
	ttOnbat.aaa("acno_p_seqno",colSs("acno.acno_p_seqno"));
	ttOnbat.aaa("card_no",lsCardNo);
	ttOnbat.aaa("block_code_2",blockReason[1]);
	ttOnbat.aaa("block_code_3",blockReason[2]);
	ttOnbat.aaa("block_code_4",blockReason[3]);
	ttOnbat.aaa("block_code_5",blockReason[4]);
	ttOnbat.aaa("match_flag",matchFlag);
	ttOnbat.aaaFunc("match_date",commSqlStr.sysYYmd,"");
	ttOnbat.aaa("debit_flag","N");
	ttOnbat.aaa("mod_pgm",hModPgm);

	if (ttOnbat.ti <=0) {
		ttOnbat.ti = ppStmtCrt("tt-onbat",ttOnbat.getSql());
	}

	sqlExec(ttOnbat.ti,ttOnbat.getConvParm());
	if (sqlNrow <= 0) {
		errmsg("insert onbat_2ccas error, kk[%s]",lsCardNo);
	}
}

public boolean isBadLoan() {
	for(int ii=0; ii<5; ii++) {
		if (commString.pos("|0N,61,62,63,64", blockReason[ii]) > 0) {
//			h_badl_bad_loan_flag = "Y";
			return true;
		}
	}
	return false;
}
int setBlockReason(String aInBlock) {
	int ii = 0, sw05 = 0, sw06 = 0, sw0F = 0;

	//proc_block =a_in_block;
	
	// -首次凍結-
	firstBlock ="Y";
	for (ii = 0; ii <= 4; ii++) {
		if (empty(blockReason[ii]))
			continue;
		firstBlock ="N";
		break;
	}
	if (eq(firstBlock,"Y")) {
		procBlock = aInBlock;
		return 1;
	}

	// -卡戶有61,62,63,64,0N-
	if (isBadLoan()) {
		return 0;
	}

	if (commString.arrayFind(blockReason,"0F"))
		sw0F =1;

	// -05,06,0G,0K; 0F.不可變更-
	if (sw0F == 1)
		return 0;
	
	for (ii = 1; ii <= 4; ii++) {
		if (eqIgno(blockReason[ii], "05")) {
			sw05 = 1;
			blockReason[ii] = "";
		}
		if (eqIgno(blockReason[ii], "06")) {
			sw06 = 1;
			blockReason[ii] = "";
		}
//		if (eq_igno(block_reason[ii], "0Q")) {
//			sw_0Q = 1;
//			block_reason[ii] = "";
//		}
//		if (eq_igno(block_reason[ii], "0K")) {
//			sw_0K = 1;
//			block_reason[ii] = "";
//		}
	}

	if (sw05 == 1 && sw06 == 1) {
		procBlock ="0F";
		return 1;
	}

	// -拒往名單:05-
	if (eq(aInBlock, "05")) {
		if (sw05 == 1 && sw06 == 0) {
			blockReason[3] ="05";
			return 0;
		}
		if (sw05 == 0 && sw06 == 0) {
			procBlock = "05";
			return 1;
		}
		if (sw05 == 0 && sw06 == 1) {
			procBlock = "0F";
			return 1;
		}
		return 0;
	}

	// -他行強停:06-
	if (eqIgno(aInBlock, "06")) {
		if (sw06 == 1 && sw05 == 0)
			return 0;
		if (sw06 == 0 && sw05 == 0) {
			procBlock = "06";
			return 1;
		}
		if (sw06 == 0 && sw05 == 1) {
			procBlock = "0F";
			return 1;
		}
		return 0;
	}
	if (sw05 == 1 || sw06 == 1)
		return 0;

	if (eqIgno(aInBlock, "0Q")) {
		if (Arrays.asList(blockReason).contains("0Q"))
			return 0;
		procBlock = "0Q";
		return 1;
	}

	if (eqIgno(aInBlock, "0K")) {
		if (Arrays.asList(blockReason).contains("0K"))
			return 0;
		procBlock = "0K";
		return 1;
	}

	return 1;
}

//==============================================
private com.Parm2sql ttAclg=null;
void insertRskAcnologA() throws Exception {
	if (ttAclg ==null) {
      ttAclg = new com.Parm2sql();
      ttAclg.insert("rsk_acnolog");
   }
   ttAclg.aaa("kind_flag", "A");
   ttAclg.aaa("acno_p_seqno", colSs("acno.acno_p_seqno"));
   ttAclg.aaa("acct_type", colSs("acno.acct_type"));
   ttAclg.aaa("id_p_seqno", colSs("acno.id_p_seqno"));
   ttAclg.aaa("corp_p_seqno", colSs("acno.corp_p_seqno"));
   ttAclg.aaa("log_date", hBusiDate);
   ttAclg.aaa("log_mode", "1");
   ttAclg.aaa("log_type", "3");
   ttAclg.aaa("log_reason","");
   ttAclg.aaa("log_not_reason","");
   ttAclg.aaa("block_reason", blockReason[0]);
   ttAclg.aaa("block_reason2", blockReason[1]);
   ttAclg.aaa("block_reason3", blockReason[2]);
   ttAclg.aaa("block_reason4", blockReason[3]);
   ttAclg.aaa("block_reason5", blockReason[4]);
   ttAclg.aaa("spec_status",colSs("acno.spec_status"));
   ttAclg.aaa("fit_cond","ECS");
   ttAclg.aaaModxxx(hModUser,hModUser);
   if (ttAclg.ti <= 0) {
      ttAclg.ti = ppStmtCrt("tt-aclg", ttAclg.getSql());
   }

	Object[] pps = ttAclg.getConvParm();
	//ddd(tt_aclg.sql_from, pps);
	sqlExec(ttAclg.ti, pps);
	if (sqlNrow <= 0) {
		errmsg("insert rsk_acnolog error");
		return;
	}

}
@Override
protected void dataProcess(String[] args) throws Exception {

}

}
