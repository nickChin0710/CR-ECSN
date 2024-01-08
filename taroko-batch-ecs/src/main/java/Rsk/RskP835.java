package Rsk;
/**
 * 2020-0319   V1.00.00	  JH	 acno.select
 * 2020-0121   V1.00.01	  JH     acnolog.fit_cond
 * 2019-0723   V1.00.02   JH     SIT: modify
 * 2019-0625:  V1.00.03   JH     p_xxx >>acno_p_xxx
 * 2018-0919:  V1.00.04	  JH     insert_rsk_blockexec
 * 2018-0309:  V1.00.05   Alex   report
 * 2017-11xx:  V1.00.06	  JH	 initial
 * 109-11-19   V1.00.07  tanwei  updated for project coding standard
 * 111-12-13   V1.00.08   Alex   凍結後寫入cca_outgoing 
 * */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.*;

import Cca.CcaOutGoing;

public class RskP835 extends BaseBatch {
private String progname = "整批凍結連動處理    111/12/13 V1.00.08";
//-============================================================================
hdata.ActAcno hAcno=new hdata.ActAcno();
hdata.ActDebt hDebt=new hdata.ActDebt();
hdata.CcaCardAcct hCcat=new hdata.CcaCardAcct();
hdata.CrdCard hCard=new hdata.CrdCard();
hdata.PtrBlockparam hBkpm=new hdata.PtrBlockparam();
hdata.RskBlockexec hBkec=new hdata.RskBlockexec();
hdata.RskAcnoLog hAclg=new hdata.RskAcnoLog();
CcaOutGoing ccaOutGoing = null;
//-----------------------------------------------------------------------------
String   hCorpChiName="";
private String hBlockWhy="";
private String hNotblockWhy="";
private int prsCount=0;
private String hIdcorpName="";
private int printCnt=0;
private String hSystemDate="";
//=----------------------------------------------------------------------------
private int pageCnt = 0 ;
private String buf = "";
List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
private String rptId = "";
private String rptName1 = "";
private int rptSeq1 = 0;
private double totalAmt = 0;
private double pageAmt = 0;
private int pageLine = 0;
private String swPrint = "";
//=----------------------------------------------------------------------------
//private String temstr = "";
//private int i_file_num = 0;
//----------------------------------------------------------------------------
//com.SqlParm tt_aclg=null;
//com.SqlParm tt_stop=null;
//------------------------------------------------
private int tiAcno=-1;
private int tiCcasAcctU=-1;
private int tiCardU=-1;
private int tiDfile=-1;
private int tiBlockparam=-1;
private int tiCard0=-1;
private int tiDebt=-1;
private int tiAcaj=-1;
private int tiCard=-1;
private int tiMcode=-1;

int iiCommit=1;

//=*************************************************************************
public static void main(String[] args) {
	RskP835 proc = new RskP835();
//	proc.debug =true;
	proc.mainProcess(args);
	proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	if (args.length>1) {
		printf("Usage : RskP835 [busi_date]");
		errExit(1);
	}

	dbConnect();

	hSystemDate =sysDate;
	if (args.length==1) {
		setBusiDate(args[0]);
   }
	
   ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
   hBkpm.validDate =sysDate;

   selectRskAcnolog();    // Main--

   sqlCommit(iiCommit);
   ccaOutGoing.finalCnt2();
   endProgram();
}

//===========================================
void selectRskAcnolog() throws Exception {
	int liRc=0;

	hBkec.initData();
   
	sqlCmd="SELECT a.p_seqno,a.acno_p_seqno,"
			+" a.id_p_seqno,"
			+" a.corp_p_seqno,"
			+" a.acct_type,"
			+" uf_card_indicator(A.acct_type) as card_indicator,"
			+" a.block_reason1,"
			+" a.block_reason2,"
			+" a.block_reason3,"
			+" a.block_reason4,"
			+" a.block_reason5,"
			+" a.spec_status,"
			+" a.card_acct_idx,"
			+" b.acno_p_seqno as rela_p_seqno,"
			+" b.log_reason,"
			+" b.block_reason"
         +" FROM   cca_card_acct A join rsk_acnolog B"
			+" on A.id_p_seqno =B.id_p_seqno and A.debit_flag <>'Y'"
			+" where  b.kind_flag ='A'"
			+" and    b.log_date =?"	//:h_busi_business_date   
         +" and    b.log_mode ='2' and b.log_type ='3'"
         +" and A.acct_type <>B.acct_type"
         +" and    nvl(b.log_reason,'') in ('Ma','Mb','Mc')"
			+" and    b.log_not_reason =''"
			+" and    nvl(b.block_reason,'') <>''"
         +" and    A.block_reason1 ='' "
         +" and    nvl(A.acno_flag,'N') not in ('2','Y')" //--非公司戶總繳--
         +" and    a.id_p_seqno <>''"
         +" and    B.rela_p_seqno =''"
         ;
	ppp(1,hBusiDate);
	
//	ddd(sqlCmd,get_sqlParm());
	if (this.openCursor() <0) {
		sqlerr("cur_acno open 失敗");
      errExit(1);
	}
	
	this.fetchExtend ="AA.";
	while (this.fetchTable()) {
		totalCnt++;

      hAcno.acnoPSeqno =colSs("AA.acno_p_seqno");
		hAcno.pSeqno =colSs("AA.p_seqno");
		hAcno.idPSeqno =colSs("AA.id_p_seqno");
		hAcno.corpPSeqno =colSs("AA.corp_p_seqno");
		hAcno.acctType =colSs("AA.acct_type");
		hAcno.cardIndicator =colSs("AA.card_indicator");
		hCcat.blockReason1 =colSs("AA.block_reason1");
		hCcat.blockReason2 =colSs("AA.block_reason2");
		hCcat.blockReason3 =colSs("AA.block_reason3");
		hCcat.blockReason4 =colSs("AA.block_reason4");
		hCcat.blockReason5 =colSs("AA.block_reason5");
		hCcat.specStatus =colSs("AA.spec_status");
		hCcat.cardAcctIdx =colNum("AA.card_acct_idx");
		hAclg.relaPSeqno =colSs("AA.rela_p_seqno");
		hAclg.logReason =colSs("AA.log_reason");
		hAclg.blockReason =colSs("AA.block_reason");

      //-R97035:凍結碼[1]=empty不連動-
      if (empty(hAclg.blockReason)) {
         continue;
      }

      liRc =selectActAcno(hAcno.acnoPSeqno);
      if (liRc <=0) continue;

      //--總繳戶--
      if (colIn("acno.acno_flag",",2,Y")) continue;
//      String ls_acno_flag =col_ss("acno.acno_flag");
//      if (commString.ss_in(ls_acno_flag,",2,Y")) continue;

		//-停用戶-
		if (colEq("acno.stop_status","Y")) {
			continue;
		}
      //--採購卡--
      if (eq(hAcno.acctType,"06")) continue;
      
      if (totalCnt%1000==0) {
      	printf("-->讀取筆數 =["+totalCnt+"]");
      }
      
      //-新系統取消-無有效卡 連動 D 檔--
      if (getAvailableCard(hAcno.pSeqno)==0) {
//      	if (check_is_dfile(h_acno.p_seqno)==0) continue;
//         select_act_debt(h_acno.p_seqno);
//         h_bkec.t_block_cnt6++;
         continue;
      }

      hBlockWhy =hAclg.logReason;
      hNotblockWhy ="";
      if (eqIgno("Mc",hAclg.logReason)) {
      	hBkec.tBlockCnt3++;
      }
      else if (eq("Mb",hAclg.logReason)) {
      	hBkec.tBlockCnt2++;
      }
      else if (eq("Ma",hAclg.logReason)) {
      	hBkec.tBlockCnt++;
      }

      //-- 凍結帳戶 --
      hBkec.tAcctCnt++;	//應凍結筆數 -
      updateCcaCardAcct();
      insertRskAcnolog(4);
      selectCrdCard(hAcno.pSeqno);

      prsCount++;
      if (prsCount%3000==0) {
      	printf("凍結筆數 =["+prsCount+"]");
      }
   }  /* end while */
   
	this.closeCursor();
	
   insertRskBlockexec();
}

//=****************************************************************************
int selectActAcno(String aPSeqno) throws Exception {
	if (tiAcno <=0) {
		sqlCmd ="select "
				+" stop_status,"
				+" no_block_flag,"
				+" no_block_s_date,"
				+" no_block_e_date,"
				+" pay_by_stage_flag,"
				+" payment_rate1,"
				+" card_indicator,"
				+" acno_flag "
				+" from act_acno"
				+" where acno_p_seqno =?"
				;
		tiAcno =ppStmtCrt("ti_acno","");
	}
	ppp(1,aPSeqno);
	
	daoTid ="acno.";
	sqlSelect(tiAcno);
	if (sqlNrow<0) {
		printf("select act_acno error; kk="+aPSeqno);
		errExit(0);
	}
	return sqlNrow;
}

//=*************************************************************************
void insertRskBlockexec() throws Exception {
	sqlCmd ="select hex(rowid) as rowid"
			+" from rsk_blockexec"
			+" where param_type ='1'"
			+" and acct_type ='00'"
			+" and valid_date =?"
			+" and exec_date ="+commSqlStr.sysYYmd
			+" and exec_mode ='2'"+commSqlStr.rownum(1);
	ppp(1,hBusiDate);
	sqlSelect();
	if (sqlNrow>0) {
		sqlCmd ="update rsk_blockexec set"
				+" exec_date_e ="+commSqlStr.sysDTime+","
				+" exec_times =exec_times+1,"
				+" t_acct_cnt =t_acct_cnt+?,  "   
				+" t_block_cnt =t_block_cnt+?, "   
				+" t_block_cnt2 =t_block_cnt2+?,"   
				+" t_block_cnt3 =t_block_cnt3+?,"   
				+" t_block_cnt6 =t_block_cnt6+?,"
				+commSqlStr.modxxxSet(hModUser, hModPgm)
				+" where rowid =?";
		ppp(1,hBkec.tAcctCnt);
		ppp(hBkec.tBlockCnt);
		ppp(hBkec.tBlockCnt2);
		ppp(hBkec.tBlockCnt3);
		ppp(hBkec.tBlockCnt6);
		this.ppRowId(colSs("rowid"));
		sqlExec("");
		if (sqlNrow <=0) {
			sqlerr("update rsk_blockexec1 error; kk[%s]",hBusiDate);
			errExit(0);
		}
		return;
	}
	
	sqlCmd ="insert into rsk_blockexec ("
				+" param_type,  "
				+" acct_type,   "
				+" valid_date,  "
				+" exec_date,   "
				+" exec_msg,    "
				+" exec_mode,   "
				+" exec_date_e, "
				+" exec_times,  "
				+" t_acct_cnt,  "   
				+" t_block_cnt, "   
				+" t_block_cnt2,"   
				+" t_block_cnt3,"   
				+" t_block_cnt6,"                         
				+commSqlStr.sqlModxxx
				+" ) values ( "
				+"  '1'"  //'1',
				+", '00'"  //'00',
				+", ?"  //valid_date
				+", "+commSqlStr.sysYYmd
				+", '連動凍結'"
				+", '2'"		//exec_mode
				+","+commSqlStr.sysDTime
				+", 1"		//exec_times
				+", ?"  //:h_bkec.t_acct_cnt,
				+", ?"  //:h_bkec.t_block_cnt,
				+", ?"  //:h_bkec.t_block_cnt2,
				+", ?"  //:h_bkec.t_block_cnt3,
				+", ?,"  //:h_bkec.t_block_cnt6,
				+modxxxInsert()
				+" )";
	ppp(1,hBusiDate);
	ppp(hBkec.tAcctCnt);
	ppp(hBkec.tBlockCnt);
	ppp(hBkec.tBlockCnt2);
	ppp(hBkec.tBlockCnt3);
	ppp(hBkec.tBlockCnt6);

	dddSql();

	sqlExec("");
	if (sqlNrow<=0) {
		sqlerr("insert rsk_blockexec1 error");
		errExit(0);
	}
}

//=***********************************************************************
void updateCcaCardAcct() throws Exception {
	if (tiCcasAcctU <=0) {
		sqlCmd ="update cca_card_acct set"
				+" block_status ='Y', "
				+" block_reason1 =?, "
				+" block_date =?, "		//最近凍結日期
				+modxxxSet()
				+" where card_acct_idx =?"
				;
		tiCcasAcctU =ppStmtCrt("ti_ccas_acct_U","");
	}
	ppp(1,hAclg.blockReason);
	ppp(hBusiDate);
	ppp(hCcat.cardAcctIdx);
	
	sqlExec(tiCcasAcctU);
   if (sqlNrow<=0) {
      sqlerr("update cca_card_acct error, kk[%s]",hCcat.cardAcctIdx);
      errExit(0);
   }
}
//=*************************************************************************
void updateCrdCard() throws Exception {
	if (tiCardU <=0) {
		sqlCmd ="update crd_card set"
				+" block_code =?,"
				+" block_date =?,"
				+modxxxSet()
				+" where card_no =?"
				;
		tiCardU =ppStmtCrt("ti_card_U","");
	}
	
	String lsBlock15=hAclg.blockReason+hCcat.blockReason2
			+hCcat.blockReason3+hCcat.blockReason4+hCcat.blockReason5;
	ppp(1,lsBlock15);
	ppp(hBusiDate);
	//kk-
	ppp(hCard.cardNo);
	
	sqlExec(tiCardU);
   if (sqlNrow <=0) {
      errmsg("update crd_card error, kk[%s]",hCard.cardNo);
      errExit(0);
   }
}

private com.Parm2sql ttAclg=null;
//=====================================================
void insertRskAcnolog(int aInt) throws Exception {
	String lsKind ="A";
	String lsCardNo ="";
	if (aInt==5) {
		lsKind ="C";
		lsCardNo =hCard.cardNo;
	}
	
	if (ttAclg ==null) {
		ttAclg = new Parm2sql();
		ttAclg.insert("rsk_acnolog");
	}
	ttAclg.aaa("kind_flag",lsKind);
	ttAclg.aaa("card_no",lsCardNo);
	ttAclg.aaa("acct_type",hAcno.acctType);
	ttAclg.aaa("acno_p_seqno", hAcno.acnoPSeqno);
	ttAclg.aaa("id_p_seqno", hAcno.idPSeqno);
	ttAclg.aaa("corp_p_seqno", hAcno.corpPSeqno);
	ttAclg.aaa("log_date",hBusiDate);
	ttAclg.aaa("log_mode","2");
	ttAclg.aaa("log_type","3");
	ttAclg.aaa("log_reason",hBlockWhy);
	ttAclg.aaa("log_not_reason",hNotblockWhy);
	if (empty(hNotblockWhy))
		ttAclg.aaa("fit_cond", "N");
	else ttAclg.aaa("fit_cond","Y");
	ttAclg.aaa("log_remark","連動凍結");
	ttAclg.aaa("block_reason", hAclg.blockReason);
	ttAclg.aaa("block_reason2", hAclg.blockReason2);
	ttAclg.aaa("block_reason3", hAclg.blockReason3);
	ttAclg.aaa("block_reason4", hAclg.blockReason4);
	ttAclg.aaa("block_reason5", hAclg.blockReason5);
	ttAclg.aaa("spec_status", hCcat.specStatus);
	ttAclg.aaa("spec_del_date", hCcat.specDelDate);
	ttAclg.aaa("emend_type","1");
	ttAclg.aaa("relate_code","B");
	ttAclg.aaa("rela_p_seqno", hAclg.relaPSeqno);
	ttAclg.aaa("apr_flag","Y");
	ttAclg.aaa("apr_user",hModUser);
	ttAclg.aaaYmd("apr_date");
	ttAclg.aaaModxxx(hModUser,hModPgm);

	if (ttAclg.ti <=0) {
		ttAclg.ti =ppStmtCrt("tt_aclg",ttAclg.getConvSQL());
	}

//	ddd(tt_aclg.sql_from,tt_aclg.get_convParm(false));
	sqlExec(ttAclg.ti,ttAclg.getConvParm());
   if (sqlNrow<=0) {
      sqlerr("insert rsk_acnolog error, kk[%s,%s]",lsKind,hAcno.acnoPSeqno);
      errExit(0);
   }
}

//=***********************************************************************
void selectPtrBlockparam() throws Exception {
	hBkpm.mcodeValue4      = 0;
   hBkpm.debtAmt4         = 0;

	if (tiBlockparam <=0) {
		sqlCmd ="select mcode_value4,"
				+" debt_amt4"
				+" from   ptr_blockparam"
				+" where  valid_date <=?"	//:h_busi_business_date
	         +" and    apr_flag = 'Y'"
	         +" and    pause_flag != 'Y'"
	         +" and    acct_type = ?" //:h_acno.acct_type
	         +" and    param_type = '1'"
	         +" and    exec_mode  ='2'"
	         +" order by valid_date desc"
	         +commSqlStr.rownum(1)
	         ;
		tiBlockparam =ppStmtCrt("ti_blockparam","");
	}
	
	daoTid ="bkpm.";
	setString(1,hBusiDate);
	setString(2,hAcno.acctType);
	sqlSelect(tiBlockparam);
	if (sqlNrow>0) {
		hBkpm.mcodeValue4      =colInt("bkpm.mcode_value4");
	   hBkpm.debtAmt4         =colNum("blpm.debt_amt4");
	}
}

//=***********************************************************************   
int getAvailableCard(String aPSeqno) throws Exception {
	
	if (tiCard0 <=0) {
		sqlCmd ="select count(*) as db_cnt"
				+" from crd_card"
				+" where p_seqno =?"
				+" and current_code ='0'";
		tiCard0 =ppStmtCrt("ti_card_0","");
	}
	ppp(1,aPSeqno);
	sqlSelect(tiCard0);
   if (sqlNrow<=0) {
      sqlerr("get_available_card error, kk[%s]",aPSeqno);
      errExit(0);
   }

   return colInt("db_cnt");
}

//=***********************************************************************
void selectCrdCard(String aPSeqno) throws Exception {
	if (tiCard <=0) {
		sqlCmd ="select card_no,"
				+" current_code,"
				+" oppost_reason,"
				+" oppost_date,"
				+" block_code,"
				+" sup_flag,"
				+" combo_indicator"
				+" from crd_card"
				+" where p_seqno =?"
				+" and current_code ='0'"
				;
		tiCard =ppStmtCrt("ti_card","");
	}
	daoTid="card.";
	ppp(1,aPSeqno);
	sqlSelect(tiCard);
	if (sqlNrow<=0)
		return;
	
	int rrSelect =sqlNrow;
   for (int ll=0; ll<rrSelect; ll++) {
      hCard.cardNo  =colSs(ll,"card.card_no");
      hCard.currentCode =colSs(ll,"card.current_code");
      hCard.oppostReason =colSs(ll,"card.oppost_reason");
      hCard.oppostDate =colSs(ll,"card.oppost_date");
      hCard.blockCode =colSs(ll,"card.block_code");
      hCard.supFlag =colSs(ll,"card.sup_flag");
      hCard.comboIndicator =colNvl(ll,"card.combo_indicator","N");
      hAclg.cardNo         =colSs(ll,"card.card_no");

//      if (No_empty(h_card.oppost_date)) {
//      	continue;
//      }
      
      //crd_card:取消凍結碼
      updateCrdCard();
      
      //--送Outgoing
      ccaOutGoing.InsertCcaOutGoingBlock(colSs(ll,"card.card_no"), colSs(ll,"card.current_code"), sysDate, hAclg.blockReason);
      
      if (eq(hCard.supFlag,"0") && !eq(hCard.comboIndicator,"N") ) {    	  
    	  insertCrdStopLog();    	  
      }
   } /*-for-*/
}

private com.Parm2sql ttStop=null;
//==============================================
void insertCrdStopLog() throws Exception {
	if (ttStop ==null) {
		ttStop = new Parm2sql();
		ttStop.insert("crd_stop_log");
	}
	ttStop.aaaFunc("proc_seqno",commSqlStr.seqEcsStop,"");
	ttStop.aaaFunc("crt_time","to_char(sysdate,'yyyymmddhh24miss')","");
	ttStop.aaa("card_no",hCard.cardNo);
	ttStop.aaa("current_code", "0");
	ttStop.aaa("trans_type","09");
	ttStop.aaa("send_type","2");  //MQUEUE-
	ttStop.aaaModxxx(hModUser,hModPgm);
   if (ttStop.ti <=0) {
      ttStop.ti =ppStmtCrt("tt-slog",ttStop.getConvSQL());
	}

   sqlExec(ttStop.ti,ttStop.getConvParm());
   if (sqlNrow<=0) {
      sqlerr("insert crd_stop_log error, kk[%s]",hCard.cardNo);
      errExit(0);
   }
}

}
