/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- * 
*  112-03-10  V1.00.00  JH          program initial                          *
*  112-03-30  V1.00.01  JH          re-coding                                *
*  112-04-24  V1.00.02  Alex        新增將檔案進行FTP至 /crdatacrea/ELOAN 並backup *    
*  112-05-10  V1.00.03  Alex        取消 HDR 檔案                                                                                * 
*  112-06-06  V1.00.04  Alex        副檔名修正為.txt , 取消檔名日期
 *  2023-1120 V1.00.05  JH       isRun BusiDate
*****************************************************************************/
package Rsk;

import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;
import com.DataSet;
@SuppressWarnings({"unchecked", "deprecation"})
public class RskP192 extends com.BaseBatch {
private final String PROGNAME = "傳送給ELOAN使用附卡資料檔處理  2023-1120 V1.00.05";
com.DataSet dsRcrate=new DataSet();

HH hh=new HH();
//-----------
class HH {
	String id_p_seqno="";
	String acno_p_seqno="";
	String acct_type="";
	double revol_int_rate=0;
	//----
	String id_no="";  //crridn="";  //crd_idno.id_no
	//String rela_type="";  //crrgam=N
	double credit_limit=0;  //crrnam
	//String crrdate="";   //crrdate
	String cpbdue_type="";  //CRRDUE	債協註記(值為Y/N/空白)
	String rc_rate="";  //CRRPOT	循環信用利率(百分之99v99)
	String asset_value="";   //CRRGUA	提供擔保註記
	String autopay_acct_no="";  //CRRACT	委扣帳號
	String autopay_flag="";  //CRRACF	自動扣繳註記
	String autopay_indr="";  //CRRALM	"扣繳額度
	String credit_cash_rate="";  //CRRADV	預借現金成數註記
	String eng_name="";  //CRRENM	英文姓名
	double credit_limit306=0;  //CRRCL306	ORG 306 額度
	double credit_limit506=0;  //CRRCL506	ORG 506 額度
	String crr506m="";
	String liac_status="";  //CRRCRM	前置協商註記 (空白、X1~X6)
	String stat_send_internet="";  //EMAIL1	申請電子帳單註記(Y/N)
	String e_mail_ebill="";  //EMAIL2	EMAIL-ADDR (來源為申請電子帳單檔)

	void init_data() {
		id_p_seqno="";
		acno_p_seqno="";
		acct_type="";
		revol_int_rate=0;
		//----
		id_no="";
		//rela_type="";
		credit_limit=0;
		cpbdue_type="";
		cpbdue_type="";
		rc_rate="";
		asset_value="";
		autopay_acct_no="";
		autopay_flag="";
		autopay_indr="";
		credit_cash_rate="";
		eng_name="";
		credit_limit306=0;
		credit_limit506=0;
		crr506m="";
		liac_status="";
		stat_send_internet="";
		e_mail_ebill="";
	}
}
//---------
String isFileName="CRRSEG";
//int iiFileNumHH=-1,
int iiFileNumDD=-1;
int iiOutCnt=0;
CommFTP commFTP = null;
CommRoutine comr = null;
CommCrd comc = new CommCrd();
//=*****************************************************************************
public static void main(String[] args) {
	RskP192 proc = new RskP192();

//	proc.debug = true;
	proc.runCheck = true;
	proc.mainProcess(args);
	proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(PROGNAME);

	if (args.length > 1) {
		printf("Usage : RskP192 [busi_date]");
		okExit(0);
	}

	dbConnect();

   String ls_runDate="";
   if (args.length >= 1) {
      setBusiDate(args[0]);
      ls_runDate =hBusiDate;
   }
   else ls_runDate =sysDate;

   if (checkWorkDate(ls_runDate)) {
      printf("-- [%s]非營業日, 不執行", ls_runDate);
      okExit(0);
   }

	fileOpen();

	selectPtrRcrate();
	selectCrdIdno();

	printf("處理筆數:[%s]",totalCnt);
	sqlCommit();

	//--傳檔
	commFTP = new CommFTP(getDBconnect(), getDBalias());
	comr = new CommRoutine(getDBconnect(), getDBalias());
	procFTP();
	renameFile();
		
	endProgram();
}

//=====================================
void procFTP() throws Exception {
//	//--HDR
//	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
//	commFTP.hEflgSystemId = "ELOAN_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
//	commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
//	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
//	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
//	commFTP.hEflgModPgm = javaProgram;
//
//	// System.setProperty("user.dir",commFTP.h_eria_local_dir);
//	showLogMessage("I", "", "mput " + isFileName+"_"+commString.right(sysDate,6)+".HDR" + " 開始傳送....");
//	int errCode = commFTP.ftplogName("ELOAN_FTP_PUT", "mput " + isFileName+"_"+commString.right(sysDate,6)+".HDR");
//
//	if (errCode != 0) {
//		showLogMessage("I", "", "ERROR:無法傳送 " + isFileName+"_"+commString.right(sysDate,6)+".HDR" + " 資料" + " errcode:" + errCode);
//		insertEcsNotifyLog(isFileName+"_"+commString.right(sysDate,6)+".HDR");
//	}
	
	//--DAT
	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	commFTP.hEflgSystemId = "ELOAN_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	commFTP.hEflgModPgm = javaProgram;

	// System.setProperty("user.dir",commFTP.h_eria_local_dir);
//	showLogMessage("I", "", "mput " + isFileName+"_"+commString.right(sysDate,6)+".DAT" + " 開始傳送....");
//	int errCode = commFTP.ftplogName("ELOAN_FTP_PUT", "mput " + isFileName+"_"+commString.right(sysDate,6)+".DAT");
	showLogMessage("I", "", "mput " + isFileName+".txt" + " 開始傳送....");
	int errCode = commFTP.ftplogName("ELOAN_FTP_PUT", "mput " + isFileName+".txt");

	if (errCode != 0) {
//		showLogMessage("I", "", "ERROR:無法傳送 " + isFileName+"_"+commString.right(sysDate,6)+".DAT" + " 資料" + " errcode:" + errCode);
//		insertEcsNotifyLog(isFileName+"_"+commString.right(sysDate,6)+".DAT");
		showLogMessage("I", "", "ERROR:無法傳送 " + isFileName+".txt" + " 資料" + " errcode:" + errCode);
		insertEcsNotifyLog(isFileName+".txt");
	}
}

//=====================
public int insertEcsNotifyLog(String fileName) throws Exception {
	setValue("crt_date", sysDate);
	setValue("crt_time", sysTime);
	setValue("unit_code", comr.getObjectOwner("3", javaProgram));
	setValue("obj_type", "3");
	setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
	setValue("notify_name", "媒體檔名:" + fileName);
	setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
	setValue("notify_desc2", "");
	setValue("trans_seqno", commFTP.hEflgTransSeqno);
	setValue("mod_time", sysDate + sysTime);
	setValue("mod_pgm", javaProgram);
	daoTable = "ecs_notify_log";

	insertTable();

	return (0);
}

//=====================
void renameFile() throws Exception {
//	String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), isFileName+"_"+commString.right(sysDate,6)+".HDR");
//	String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), isFileName+"_"+commString.right(sysDate,6)+".HDR");
//
//	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
//		showLogMessage("I", "", "ERROR : 檔案[" + isFileName+"_"+commString.right(sysDate,6)+".HDR" + "]更名失敗!");
//		return;
//	}
//	showLogMessage("I", "", "檔案 [" + isFileName+"_"+commString.right(sysDate,6)+".HDR" + "] 已移至 [" + tmpstr2 + "]");
	
//	String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), isFileName+"_"+commString.right(sysDate,6)+".DAT");
//	String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), isFileName+"_"+commString.right(sysDate,6)+".DAT");
	String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), isFileName+".txt");
	String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), isFileName+".txt"+"_"+sysDate);

	if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
//		showLogMessage("I", "", "ERROR : 檔案[" + isFileName+"_"+commString.right(sysDate,6)+".DAT" + "]更名失敗!");
		showLogMessage("I", "", "ERROR : 檔案[" + isFileName+".txt" + "]更名失敗!");
		return;
	}
//	showLogMessage("I", "", "檔案 [" + isFileName+"_"+commString.right(sysDate,6)+".DAT" + "] 已移至 [" + tmpstr4 + "]");
	showLogMessage("I", "", "檔案 [" + isFileName+".txt" + "] 已移至 [" + tmpstr4 + "]");
	
}
//==============

void selectCrdIdno() throws Exception {
	String cc="" , newLine = "\r\n"; //""$$";
//	sqlCmd = "select id_no, id_p_seqno"
//			+ ", card_since, eng_name"
//			+ " from crd_idno "
//			+ " where 1=1"
////			+ " order by id_no"
////			+ commSqlStr.rownum(10000)
//	;
	sqlCmd ="SELECT DISTINCT A.id_no, A.id_p_seqno" +
			" FROM crd_idno A JOIN act_acno B ON B.id_p_seqno=A.id_p_seqno" +
			" WHERE 1=1"
//			commSqlStr.rownum(1000)
			;
	//ppp(1,isProcYymm);

	this.openCursor();
	while (fetchTable()) {
		hh.init_data();

		hh.id_p_seqno =colSs("id_p_seqno");
		if (empty(hh.id_p_seqno)) continue;

		hh.id_no =colSs("id_no");
		//--
		selectActAcno();
		if (empty(hh.acno_p_seqno)) continue;
		totalCnt++;
		dspProcRow(10000);

		selectCrdCard();
		//selectCrdRela();
		selectColCpbdue();
		//selectActAcctCurr();
		getRcRate(hh.revol_int_rate);
		selectActAcno_2();
		selectColLiacNego();

		//???7	擔供擔保註記	CRRGUA	CHARACTER(2)
		hh.asset_value ="00";
		//9	自動轉帳註記	CRRACF	CHARACTER(1)	0:不轉帳 2:自動轉帳
		//Check #10 #08
		hh.autopay_flag ="0";
		if (!empty(hh.autopay_acct_no) && !eq(hh.autopay_indr,"00")) {
			hh.autopay_flag ="2";
		}

		//--
		iiOutCnt++;
		String lsTxt = commString.rpad(hh.id_no, 10) + cc
				+ "N" + cc  //2.CRRGAM 保證人註記	1
				+ String.format("%09.0f", hh.credit_limit) + cc  //3.CRRNAM.信用額度	9
				+ commString.bbFixlen(hh.cpbdue_type, 1) + cc  //4.CRRDUE 債協註記(值為Y/N/空白)	1
				+ commString.bbFixlen(hh.rc_rate, 4) + cc  //5.CRRPOT 循環信用利率(百分之99v99)
				+ "00" + cc  //6.CRRGUA	提供擔保註記	2
				+ commString.bbFixlen(hh.autopay_acct_no, 13) + cc  //7.CRRACT 委扣帳號 13
				+ commString.bbFixlen(hh.autopay_flag, 1) + cc  //8.CRRACF 自動扣繳註記	1
				+ commString.bbFixlen(hh.autopay_indr, 2) + cc 	//9.CRRALM 扣繳額度
				+ commString.bbFixlen(hh.credit_cash_rate, 2) + cc  //10.CRRADV 預借現金成數註記	2
				+ commString.bbFixlen(hh.eng_name, 26) + cc  //11.CRRENM	英文姓名 26
				+ String.format("%09.0f", hh.credit_limit306) + cc  //12.CRRCL306:ORG 306 額度	9
				+ String.format("%09.0f", hh.credit_limit506) + cc  //13.CRRCL506:ORG 506 額度
				+ " " + cc  //14.CRR506M: 持有個人採購融資卡註記 X1
				+ commString.bbFixlen(hh.liac_status, 2) + cc  //15.CRRCRM:前置協商註記-X2
				+ commString.bbFixlen(hh.stat_send_internet, 1) + cc  //16.EMAIL1: 申請電子帳單註記(Y/N)-X1
				+ commString.bbFixlen(hh.e_mail_ebill, 30) + cc  //17.EMAIL2:EMAIL-ADDR (來源為申請電子帳單檔)-X30
				+ newLine;
		writeTextFile(iiFileNumDD,lsTxt);
	}
	closeCursor();

	closeOutputText(iiFileNumDD);
	printf(" 產生筆數[%s]", iiOutCnt);
	//--
	//ENVELT1CUST_XLC_YYMMDD.HDR
	//1-32 為檔名(左靠)
	//33-40 為處理日
	//41-42 塞 ‘00’
	//43-56 為檔案產生年月日時分杪
	//57-64為筆數(右靠)

//	String ss=isFileName+"_"+commString.right(sysDate,6)+".DAT"
//	+sysDate+"00"+sysDate+sysTime
//	+commString.lpad(""+iiOutCnt,8,"0");
//	writeTextFile(iiFileNumHH,ss);
//	closeOutputText(iiFileNumHH);
}
//==========
//int tiAcurr=-1;
//void selectActAcctCurr() throws Exception {
//	if (empty(hh.acno_p_seqno)) return;
//
//	if (tiAcurr <=0) {
//		sqlCmd ="select autopay_acct_no"
//		+" from act_acct_curr"
//		+" where curr_code ='901'"
//		+" and p_seqno =?"
//		;
//		tiAcurr =ppStmtCrt("ti-Acurr","");
//	}
//	ppp(1, hh.acno_p_seqno);
//
//	sqlSelect(tiAcurr);
//	if (sqlNrow <=0) return;
//
//	//8	委託扣款帳號	CRRACT	CHARACTER(13)	act_acct_curr.autopay_acct_no
//	//curr = 901
//	hh.autopay_acct_no =colSs("autopay_acct_no");
//
//}
//----
int tiRcrate=-1;
void selectPtrRcrate() throws Exception {
//	if (tiRcrate <=0) {
//		sqlCmd = " select rcrate_year from ptr_rcrate where 1=1 and rcrate_day = ? ";
//		tiRcrate =ppStmtCrt("ti-rcrate","");
//	}
//	ppp(1, hh.revol_int_rate);
//	sqlSelect(tiRcrate);
//	if (sqlNrow <=0) return;
//	double lmRateYY =colNum("rcrate_year") * 100;
//	//循環利率	CRRPOT	CHARACTER(4)
//	hh.rc_rate =String.format("%04.0f",lmRateYY);
	dsRcrate.dataClear();
	sqlCmd ="select rcrate_day, rcrate_year from ptr_rcrate"+
			" order by 1,2";
	sqlQuery(dsRcrate,sqlCmd,null);
	printf(" dsRcrate.rowcount="+dsRcrate.rowCount());
}
void getRcRate(double am_intRate) throws Exception {
	hh.rc_rate="0000";
	for (int ll = 0; ll <dsRcrate.rowCount(); ll++) {
		double lmRateDD =dsRcrate.colNum(ll,"rcrate_day");
		if (lmRateDD == am_intRate) {
			double lmRateYY =dsRcrate.colNum(ll,"rcrate_year") * 100;
			//循環利率	CRRPOT	CHARACTER(4)
			hh.rc_rate =String.format("%04.0f",lmRateYY);
			break;
		}
	}
}
//-------
//int tiRela=-1;
//void selectCrdRela() throws Exception {
//	if (empty(hh.id_p_seqno)) return;
//	if (tiRela <=0) {
//		sqlCmd ="select rela_type"+
//		" from crd_rela"+
//		" where id_p_seqno =?";
//		tiRela =ppStmtCrt("ti-S-rela","");
//	}
//
//	ppp(1, hh.id_p_seqno);
//	sqlSelect(tiRela);
//	if (sqlNrow <=0) return;
//	//保證人註記	rela_type	CHARACTER(1)	ID去串CRD_RELA?
//	hh.rela_type =colSs("rela_type");
//
//}
//--
int tiAcno=-1;
void selectActAcno() throws Exception {
	if (empty(hh.id_p_seqno)) return;
	if (tiAcno <=0) {
		sqlCmd = "select A.acno_p_seqno, A.p_seqno, A.acct_type" +
				", A.line_of_credit_amt, A.line_of_credit_amt_cash" +
				", A.revolve_int_rate" +
				", A.autopay_indicator" +
				", A.stat_send_internet, A.e_mail_ebill" +
				", B.autopay_acct_no" +
				" from act_acno A left join act_acct_curr B" +
				" on B.p_seqno=A.acno_p_seqno and B.curr_code='901'" +
				" where A.id_p_seqno =?" +
				" and A.acct_type in ('01','03','06')" +
				" order by A.acct_type"+
				commSqlStr.rownum(1)
		;
		tiAcno =ppStmtCrt("ti-S-acno","");
	}

	ppp(1, hh.id_p_seqno);
	sqlSelect(tiAcno);
	if (sqlNrow <=0) return;
	hh.acno_p_seqno =colSs("acno_p_seqno");
	hh.acct_type =colSs("acct_type");
	//3	國際卡信用額度 (個人卡額度)	credit_limit	DECIMAL(9,0)	acct_type = 01
	hh.credit_limit =colNum("line_of_credit_amt");
	//6	循環利率	CRRPOT	CHARACTER(4)
	hh.revol_int_rate =colNum("revolve_int_rate");
	//10	扣繳額度(方式)	CRRALM	CHARACTER(2) 00:不扣繳,10:扣最低,20:扣全額
	//帳戶自動扣繳指示碼      1.扣 TTL, 2.扣MP 3.其它
	String lsAutoPayIndi =colSs("autopay_indicator");
	if (eq(lsAutoPayIndi,"1")) hh.autopay_indr ="20";
	else if (eq(lsAutoPayIndi,"2")) hh.autopay_indr ="10";
	else hh.autopay_indr ="00";

	//11	預借現金成數	CRRADV	CHARACTER(2)	空白,00,10,…..
	//act_acno.line_of_credit_amt_cash/act_acno_line_of_credit_amt
	double lmCash =(colNum("line_of_credit_amt_cash") / hh.credit_limit) * 100;
	hh.credit_cash_rate =String.format("%02.0f",lmCash);
	hh.credit_cash_rate =commString.right(hh.credit_cash_rate,2);

	//17	申請電子帳單註記(Y/N)	EMAIL1	CHAR (1)	2016.09.08add
	//act_acno.stat_send_internet
	hh.stat_send_internet =colNvl("stat_send_internet","N");
	//18	電子郵件地址	EMAIL2	CHAR (30) act_acno.e_mail_ebill
	hh.e_mail_ebill =colSs("e_mail_ebill");
	//7	CRRACT	委扣帳號	13-
	hh.autopay_acct_no =colSs("autopay_acct_no");
}
//--
int tiAcno2=-1;
void selectActAcno_2() throws Exception {
	if (empty(hh.id_p_seqno)) return;
	if (tiAcno2 <=0) {
		sqlCmd = "select " +
//		", decode(acct_type,'03',line_of_credit_amt,0) as credit_limit_03"+
//		", decode(acct_type,'06',line_of_credit_amt,0) as credit_limit_06"+
				" sum(line_of_credit_amt) as credit_limit_306" +
				" from act_acno" +
				" where id_p_seqno =?" +
				" and acct_type in ('03','06')"
		;
		tiAcno2 = ppStmtCrt("ti-S-acno2", "");
	}

	ppp(1, hh.id_p_seqno);
	sqlSelect(tiAcno2);
	if (sqlNrow <=0) return;
	//13	ORG 306 法人卡額度	CRRCL306	DECIMAL(9,0)	同一ID，其acct_type = “03”的act_acno.line_of_credit_amt
	hh.credit_limit306 =colNum("credit_limit_306");  //+colNum("credit_limit_06");
	//14	ORG 506 採購融資卡額度	CRRCL506	DECIMAL(9,0)	同一ID，其acct_type = “06”的act_acno.line_of_credit_amt
	hh.credit_limit506 =0;
	//15	持有個人採購融資卡註記	CRR506M	CHARACTER(1)	Check #14
//	hh.crr506m ="";
//	if (hh.credit_limit506 >0) hh.crr506m="Y";
}
//---
int tiCpbdu=-1;
void selectColCpbdue() throws Exception {
	//5	債協註記	CRRDUE	CHARACTER(1)	(Y:表示有 N:表示無)
	//col_cpbdue.cpbdue_tcb_type
	hh.cpbdue_type ="N";
	if (tiCpbdu <=0) {
		sqlCmd ="select cpbdue_tcb_type"+
		" from col_cpbdue"+
		" where cpbdue_acct_type =?" +
		" and cpbdue_id_p_seqno =?"
		;
		tiCpbdu =ppStmtCrt("ti-cpdbu","");
	}

	ppp(1, hh.acct_type);
	ppp(hh.id_p_seqno);
	sqlSelect(tiCpbdu);
	if (sqlNrow <=0) return;
	String lsType=colSs("cpbdue_tcb_type");
	hh.cpbdue_type =lsType;
//	if (!empty(lsType)) {
//		hh.cpbdue_type ="Y";
//	}
}
//----
int tiCard=-1;
void selectCrdCard() throws Exception {
	if (tiCard <=0) {
		sqlCmd ="select eng_name"+
		" from crd_card"+
		" where id_p_seqno =?"+
		" and eng_name <>''"+
		" order by issue_date desc "+commSqlStr.rownum(1)
		;
		tiCard =ppStmtCrt("ti-card","");
	}

	ppp(1, hh.id_p_seqno);
	sqlSelect(tiCard);
	if (sqlNrow <=0) return;

	//12	英文戶名	CRRENM	CHARACTER(26)	crd_idno.eng_name
	hh.eng_name =colSs("eng_name");
}
//--------
int tiLiac=-1;
void selectColLiacNego() throws Exception {
	if (tiLiac <=0) {
		sqlCmd ="select liac_status"
		+" from col_liac_nego"
		+" where id_p_seqno =?"
		+" order by file_date desc"+commSqlStr.rownum(1)
		;
		tiLiac =ppStmtCrt("ti-Liac","");
	}
	ppp(1, hh.id_p_seqno);
	sqlSelect(tiLiac);
	if (sqlNrow <=0) return;

	//16	前置協商註記	CRRCRM	CHARACTER(2)	col_liac_nego.liac_status
	hh.liac_status ="X"+colSs("liac_status");
}
//================
void fileOpen() throws Exception {
	//--
//	String lsFileHH= getEcsHome() + "/media/rsk/" + isFileName+"_"+commString.right(sysDate,6)+".HDR";
//	printf("open out-file [%s]", lsFileHH);
//	iiFileNumHH =openOutputText(lsFileHH);
//	if (iiFileNumHH <0) {
//		errmsg("在程式執行目錄下沒有權限讀寫資料, file[%s]",lsFileHH);
//		errExit(1);
//	}
	//--
//	String lsFileDD= getEcsHome() + "/media/rsk/" + isFileName+"_"+commString.right(sysDate,6)+".DAT";
	String lsFileDD= getEcsHome() + "/media/rsk/" + isFileName+".txt";
	printf("open out-file [%s]", lsFileDD);
	iiFileNumDD =openOutputText(lsFileDD);
	if (iiFileNumDD <0) {
		errmsg("在程式執行目錄下沒有權限讀寫資料, file[%s]",lsFileDD);
		errExit(1);
	}
}

}
