/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- * 
*  112-04-07  V1.00.00  JH          program initial                          *
*  112-04-24  V1.00.01  Alex        新增將檔案進行FTP至 /crdatacrea/ELOAN 並backup *
*  112-05-10  V1.00.02  Alex        取消 HDR 檔                                                                                    * 
*  112-06-06  V1.00.03  Alex        副檔名修正為.txt , 取消檔名日期
 *  2023-1120 V1.00.03  JH    is busi-date run
*****************************************************************************/
package Rsk;

import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;

@SuppressWarnings({"unchecked", "deprecation"})
public class RskP190 extends com.BaseBatch {
private final String PROGNAME = "傳送給ELOAN使用卡人資料檔(CAASEG)  2023-1120 V1.00.03";
HH hh=new HH();
//-----------
class HH {
   String id_pseqno="";
   String majid_pseqno="";
   int int_rate_mcode=0;
   String son_card_flag="";
   //-------
   String card_no="";   //-cracrd
   String current_code="";  //-crasta
//   String spec_status="";   //-crares
   String oppost_reason="";
   String majid_no="";  //-crridn
   String id_no="";  //-craidn
   String new_end_date="";  //-craaep
   String issue_bank_no="";  //-crabnk
   String bank_actno="";  //-craacn
   double card_limit=0;  //-cralamt
   String group_code="";  //-cracno
   String issue_date="";  //-cradate
   String reg_bank_no="";  //-crabrh
   String ori_issue_date="";  //-craode
   int delay_day=0;  //-delayday
   String card_open="";  //-craops

   void init_data() {
      id_pseqno="";
      majid_pseqno="";
      card_no="";   //-cracrd
      current_code="";  //-crasta
//      spec_status="";   //-crares
      oppost_reason="";
      int_rate_mcode=0;
      son_card_flag="";
      //-------
      majid_no="";  //-crridn
      id_no="";  //-craidn
      new_end_date="";  //-craaep
      issue_bank_no="";  //-crabnk
      bank_actno="";  //-craacn
      card_limit=0;  //-cralamt
      group_code="";  //-cracno
      issue_date="";  //-cradate
      reg_bank_no="";  //-crabrh
      ori_issue_date="";  //-craode
      delay_day=0;  //-delayday
      card_open="N";  //-craops

   }
}
//---------
String isFileName="CAASEG";
int iiFileNumDD=-1;
int iiOutCnt=0;
CommFTP commFTP = null;
CommRoutine comr = null;
CommCrd comc = new CommCrd();
//=*****************************************************************************
public static void main(String[] args) {
   RskP190 proc = new RskP190();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 1) {
      printf("Usage : RskP190 [busi_date]");
      okExit(0);
   }

   dbConnect();

   String ls_runDate="";
   if (args.length >= 1) {
      setBusiDate(args[0]);
      ls_runDate =hBusiDate;
   }
   else ls_runDate=sysDate;
   if (checkWorkDate(ls_runDate)) {
      printf("-- 非營業日[%s], 不執行", ls_runDate);
      okExit(0);
   }

   fileOpen();
   selectCrdCard();

   printf("處理筆數:[%s]",totalCnt);
   sqlCommit();
   
   //--傳檔
   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   renameFile();		   
   
   endProgram();
}
//=====================
void procFTP() throws Exception {
	//--HDR
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
	showLogMessage("I", "", "mput " + isFileName+".txt" + " 開始傳送....");
//	int errCode = commFTP.ftplogName("ELOAN_FTP_PUT", "mput " + isFileName+"_"+commString.right(sysDate,6)+".DAT");
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

//=====================
void selectCrdCard() throws Exception {
   String cc="" , newLine = "\r\n";  //""$$";
   sqlCmd = "select card_no, current_code, group_code"
             +", id_p_seqno, major_id_p_seqno, acno_p_seqno"
             +", new_end_date, reg_bank_no, bank_actno, combo_acct_no"
             +", son_card_flag, indiv_crd_lmt"
             +", issue_date, ori_issue_date, activate_flag"
           +", oppost_reason"
             + " from crd_card "
             + " where 1=1"
//             + " order by card_no"
//	+ commSqlStr.rownum(200)
   ;

   //ppp(1,isProcYymm);

   this.openCursor();
   while (fetchTable()) {
      totalCnt++;
      hh.init_data();

      dspProcRow(10000);

      hh.id_pseqno =colSs("id_p_seqno");
      hh.majid_pseqno =colSs("major_id_p_seqno");
      if (empty(hh.id_pseqno)) continue;

      //身分證號碼--正卡人	CRRIDN	CHARACTER(10)	crd_card.major_id_p_seqno 去串crd_idno
      hh.majid_no =selectCrdIdno(hh.majid_pseqno);
      //2	信用卡卡號	CRACRD	CHARACTER(16)
      hh.card_no =colSs("card_no");
      //3	身分證號碼--持卡人	CRAIDN	CHARACTER(10)
      if (eq(hh.majid_pseqno,hh.id_pseqno)) {
         hh.id_no =hh.majid_no;
      }
      else hh.id_no =selectCrdIdno(hh.id_pseqno);
      //4	卡片到期日	CRAAEP	CHARACTER(4) YYMM
      hh.new_end_date =colSs("new_end_date");
      hh.new_end_date =commString.mid(hh.new_end_date,2,4);
      //5	卡別狀況	CRASTA	CHARACTER(1)
      hh.current_code =colSs("current_code");
      //6	發卡/業績單位	CRABNK	CHARACTER(4)	crd_card.reg_bank_no
      hh.issue_bank_no =colSs("reg_bank_no");
      //7	CRAACN	存款帳號(VD/COMBO的帳號)	PIC X(13)
      //hh.bank_actno =colSs("bank_actno");
      hh.bank_actno =colSs("combo_acct_no");
      //8	卡片額度	CRALAMT	DECIMAL(9,0)
      // If crd_card.son_card_flag = “Y”=> crd_card.indiv_crd_lmt；否則，放act_acno.line_of_credit_amt
      hh.card_limit =colNum("indiv_crd_lmt");
      hh.son_card_flag =colSs("son_card_flag");
      selectActAcno(colSs("acno_p_seqno"));

      hh.group_code =colSs("group_code");
      //hh.group_code3 =commString.right(hh.group_code3,3);
      hh.issue_date =colSs("issue_date");
      hh.reg_bank_no =colSs("reg_bank_no");
      //11	CRARES	控管碼原因	PIC X(2)	2	74	75	crd_card.oppost_reason
      hh.oppost_reason =colSs("oppost_reason");
      if (hh.oppost_reason.length()>2) {
         hh.oppost_reason =commString.left(hh.oppost_reason,2);
      }
      //selectCcaCardBase();

      hh.ori_issue_date =colSs("ori_issue_date");
      if (empty(hh.ori_issue_date)) hh.ori_issue_date=colSs("issue_date");
      hh.ori_issue_date =commDate.toTwDate(hh.ori_issue_date);
      hh.ori_issue_date =commString.lpad(hh.ori_issue_date,7,"0");
      //開卡註記	CRAOPS	CHAR(01)	(Y已開卡/N未開卡)
      //判斷crd_card.activate_flag “2”表已開卡
      if (colEq("activate_flag","2"))
         hh.card_open ="Y";
      else hh.card_open ="N";

      //--
      iiOutCnt++;
      String lsTxt=commString.rpad(hh.majid_no,10)+cc
                    +commString.rpad(hh.card_no,16)+cc	 //CHARACTER(16)
                    +commString.rpad(hh.id_no,10)+cc
                    +commString.rpad(hh.new_end_date,4)+cc  //卡片到期日	craaep	CHARACTER(4)
                    +commString.rpad(hh.current_code,1)+cc  //卡別狀況	crasta	CHARACTER(1)
                    +commString.rpad(hh.issue_bank_no,4)+cc  //6.CRABNK 發卡/業績單位 X(4)
              +commString.rpad(hh.bank_actno,13)+cc  //7.CRAACN 存款帳號(VD/COMBO的帳號) X(13)
              +String.format("%09.0f",hh.card_limit)+cc  //8.CRALAMT 卡片額度 9(09)
              +"  "+cc  //9.CRACNO 卡別代號 X(2)
              +commString.rpad(hh.reg_bank_no,4)+cc  //10.CRABRH 原申辦分行 X(4)
              +commString.rpad(hh.oppost_reason,2)+cc  //11.CRARES 控管碼原因 X(2)
              +commString.rpad(hh.ori_issue_date,7)+cc  //12.CRAODE 開戶日(卡片建檔日) X(7)
              +commString.rpad(hh.group_code,4)+cc  //15.CRACNO 卡別代號 X(4)
              +String.format("%07d",hh.delay_day)+cc  //14.逾期天數 X(7)
              +commString.rpad(hh.card_open,1)+cc  //13.開卡註記 X(1)
              +newLine;
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

//   String ss=isFileName+"_"+commString.right(sysDate,6)+".DAT"
//              +sysDate+"00"+sysDate+sysTime
//              +commString.lpad(""+iiOutCnt,8,"0");
//   writeTextFile(iiFileNumHH,ss);
//   closeOutputText(iiFileNumHH);
}
//------
int tiAcno=-1;
void selectActAcno(String aPseqno) throws Exception {
   if (empty(aPseqno)) return;
   if (tiAcno <=0) {
      sqlCmd ="select A.line_of_credit_amt, A.int_rate_mcode"+
               ", A.p_seqno, B.min_pay_bal "+
               " from act_acno A left join act_acct B on B.p_seqno=A.p_seqno"+
               " where acno_p_seqno =?"
       ;
      tiAcno =ppStmtCrt("ti-acno","");
   }

   ppp(1, aPseqno);
   daoTid ="acno.";
   sqlSelect(tiAcno);
   if (sqlNrow <=0) return;

   //-不是子卡-
   if (!eq(hh.son_card_flag,"Y")) {
      hh.card_limit =colNum("acno.line_of_credit_amt");
   }

   double lm_minPayBal=colNum("acno.min_pay_bal");
   hh.int_rate_mcode =colInt("acno.int_rate_mcode");
   int liDay=0;
   if (hh.int_rate_mcode ==0 && lm_minPayBal>0) {
      liDay =15;
//      String lsPseqno=colSs("acno.p_seqno");
//      liDay=selectActAcct(lsPseqno);
   }
   else if (hh.int_rate_mcode ==1) liDay=30;
   else if (hh.int_rate_mcode ==2) liDay=60;
   else if (hh.int_rate_mcode ==3) liDay=90;
   else if (hh.int_rate_mcode ==4) liDay=120;
   else if (hh.int_rate_mcode ==5) liDay=150;
   else if (hh.int_rate_mcode ==6) liDay=180;
   else if (hh.int_rate_mcode >=7) liDay=210;

   //14	逾期天數	DELAYDAY	CHAR(07)
   hh.delay_day =liDay;
}
//--
int tiAcct=-1;
int selectActAcct(String aPseqno) throws Exception {
   if (empty(aPseqno)) {
      return 0;
   }

   if (tiAcct <=0) {
      sqlCmd ="select min_pay_bal"+
               " from act_acct"+
               " where p_seqno =?"
       ;
      tiAcct =ppStmtCrt("ti-acct","");
   }
   ppp(1, aPseqno);
   sqlSelect(tiAcct);
   if (sqlNrow <=0) return 0;
   if (colNum("min_pay_bal")>0) return 15;
   return 0;
}
//-------
int tiCardbase=-1;
void selectCcaCardBase() throws Exception {
   if (tiCardbase <=0) {
      sqlCmd ="select spec_status, spec_del_date"+
               " from cca_card_base"+
               " where card_no =?"
       ;
      tiCardbase =ppStmtCrt("ti-card_base","");
   }
   ppp(1, hh.card_no);
   sqlSelect(tiCardbase);
   if (sqlNrow <=0) return;

//   hh.spec_status =colSs("spec_status");
//   String lsDelDate =colSs("spec_del_date");
//   if (!empty(lsDelDate) && lsDelDate.compareTo(sysDate)<=0) {
//      hh.spec_status ="";
//   }

}
//---------
int tiIdno=-1;
String selectCrdIdno(String a_idPseqno) throws Exception {
   if (empty(a_idPseqno)) return "";

   if (tiIdno <=0) {
      sqlCmd ="select id_no, id_no_code"
               +" from crd_idno"
       +" where id_p_seqno =? and id_p_seqno<>''"
       ;
      tiIdno =ppStmtCrt("ti-idno","");
   }

   ppp(1, a_idPseqno);

   daoTid="idno.";
   sqlSelect(tiIdno);
   if (sqlNrow <=0) return "";

   return colSs("idno.id_no");
}

//================
void fileOpen() throws Exception {
   //--
//   String lsFileHH= getEcsHome() + "/media/rsk/" + isFileName+"_"+commString.right(sysDate,6)+".HDR";
//   printf("open out-file [%s]", lsFileHH);
//   iiFileNumHH =openOutputText(lsFileHH);
//   if (iiFileNumHH <0) {
//      errmsg("在程式執行目錄下沒有權限讀寫資料, file[%s]",lsFileHH);
//      errExit(1);
//   }
   //--
//   String lsFileDD= getEcsHome() + "/media/rsk/" + isFileName+"_"+commString.right(sysDate,6)+".DAT";
   String lsFileDD= getEcsHome() + "/media/rsk/" + isFileName+".txt";
   printf("open out-file [%s]", lsFileDD);
   iiFileNumDD =openOutputText(lsFileDD);
   if (iiFileNumDD <0) {
      errmsg("在程式執行目錄下沒有權限讀寫資料, file[%s]",lsFileDD);
      okExit(0);
   }
}

}
