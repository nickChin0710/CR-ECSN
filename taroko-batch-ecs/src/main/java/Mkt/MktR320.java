package Mkt;
/**
 * 2023-0810 V1.00.01   JH    YYYYMM02執行
 * 2023-1006 V1.00.02   JH    oFile=FEETCB_0+is_busiDateTW+.TXT;
 * 2023-1018 V1.00.03   JH    1xxxx....\r\n
 * */

import com.CommFTP;
import com.Parm2sql;

import java.text.Normalizer;

@SuppressWarnings({"unchecked", "deprecation"})
public class MktR320 extends com.BaseBatch {
private final String PROGNAME = "聯名機構推卡獎勵-高雄三信  2023-1018 V1.00.03";
//-HH----------
String hh_proj_code="MKTR320";
String hh_acct_month="";
String hh_card_no="";
//--
String is_busiDateTW="";
long il_dataCnt =0;
//=*****************************************************************************
public static void main(String[] args) {
   MktR320 proc = new MktR320();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : MktR320 [busi_date, callbatch_seqno]");
      okExit(0);
   }

   if (args.length >= 1) {
      if (args[0].length()==8) {
         String sG_Args0 = args[0];
         hBusiDate= Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD);
      }
   }
   if (args.length >0) {
      callBatchSeqno(args[args.length -1]);
   }

   dbConnect();
   callBatch(0, 0, 0);

   //-每月2日執行---
   String lsDD=commString.right(hBusiDate,2);
   if (!eq(lsDD,"02")) {
      printf("每月2日執行, 本日非執行日!!");
      okExit(0);
   }

   hh_acct_month =commDate.monthAdd(hBusiDate,0);
   is_busiDateTW =commDate.toTwDate(hBusiDate);
   printf(" process TW-date[%s]", is_busiDateTW);

   fileOpen();
   //-header-
   //FEETCB_0YYYMMDD.TXT              首筆 : 放民國年 (CARDLINK放系統日，新系統改放營業日)
   //BOF0YYYMMDD                              header : 第二筆 (日期同上處理)
   //1999999999999999900200         明細 (固定1 + 16 位卡號 + 5位數字(固定放200元))
   //EOF00000000                                  尾筆：計算明細筆數共8位整數
   String ss="BOF0"+is_busiDateTW+commString.space(11)+newLine;
   writeTextFile(iiFileNum,ss);

   //-data--
   select_Ips_cgec_all();

   //-footer---
   ss ="EOF"+commString.fixlenNum(""+ il_dataCnt,8)+commString.space(11)+newLine;
   writeTextFile(iiFileNum,ss);
   closeOutputText(iiFileNum);

   //---
   ftpMput(isFileName);
//   sqlCommit();
   endProgram();
}
//==================
void select_Ips_cgec_all() throws Exception {
   String ls_newLine = "\r\n";
   sqlCmd ="SELECT DISTINCT A.card_no, A.ips_card_no"
           +" FROM ips_card A JOIN crd_card C ON A.card_no=C.card_no "
           +"   JOIN ips_cgec_all B ON B.ips_card_no=A.ips_card_no "
           +" WHERE 1=1 "
           +" AND C.group_code ='1683' "
           +" ORDER BY A.card_no"
           ;
   openCursor();
   while (fetchTable()) {
      totalCnt++;
      hh_card_no =colSs("card_no");
      //check是否回饋--
      int liRC=select_mkt_member_log(hh_card_no);
      if (liRC >0) continue;

      String tt="1"+commString.bbFixlen(hh_card_no,16)+"00200"+ls_newLine;
      writeTextFile(iiFileNum,tt);
      il_dataCnt++;
      //---
      insertMkt_Member_Log();
   }
   closeCursor();
}
//===================
int tiMemlg=-1;
int select_mkt_member_log(String a_cardNo) throws Exception {
   if (tiMemlg <=0) {
      sqlCmd ="select count(*) as mem_cnt"+
              " from mkt_member_log"+
              " where 1=1"+
              " and proj_code =?"+
              " and card_no =?";
      tiMemlg =ppStmtCrt("tiMemlg","");
   }
   ppp(1, hh_proj_code);
   ppp(hh_card_no);
   sqlSelect(tiMemlg);
   if (sqlNrow >0 && colInt("mem_cnt")>0) {
      return 1;
   }
   return 0;
}
//---------
com.Parm2sql ttAmemlg=null;
void insertMkt_Member_Log() throws Exception {
   if (ttAmemlg ==null) {
      ttAmemlg =new Parm2sql();
      ttAmemlg.insert("mkt_member_log");
   }

   ttAmemlg.aaa("acct_month", hh_acct_month);          	//-x(6)
   ttAmemlg.aaa("proj_code", hh_proj_code);           	//-x(10)
   ttAmemlg.aaa("card_no", hh_card_no);             	//-x(19)
   //003	issue_date          	//-x(8)
   //004	acct_type           	//-x(2)
   ttAmemlg.aaa("group_code", "1683");          	//-x(4)
   //006	id_p_seqno          	//-x(10)
   //007	promote_dept        	//-x(11)
   //008	promote_emp_no      	//-x(10)
   //009	staff_branch_no     	//-x(11)
   //010	member_id           	//-x(20)
   //011	reg_bank_no         	//-x(4)
   //012	clerk_id            	//-x(10)
   //013	introduce_emp_no    	//-x(10)
   //014	introduce_id        	//-x(11)
   //015	prod_no             	//-x(8)
   //016	electronic_code     	//-x(2)
   //017	electronic_card_no  	//-x(20)
   //018-k	ref_no              	//-x(10)
   ttAmemlg.aaa("dest_amt", 200);            	//-dec(14,3)
   //020	platform_kind_amt   	//-dec(12,0)
   //021	platform_kind_cnt   	//-int(4)
   //022	autoload_cnt        	//-int(4)
   //023	vd_flag             	//-x(1)
   //024	proc_date           	//-x(8)
   //025	ftp_time            	//-x(6)
   //026	static_month        	//-x(6)
   ttAmemlg.aaa("proc_flag", "Y");           	//-x(1)
   ttAmemlg.aaa("crt_date", hBusiDate);            	//-x(8)
   ttAmemlg.aaa("crt_time", sysTime);            	//-x(6)
   ttAmemlg.aaaDtime("mod_time");            	//-time()
   ttAmemlg.aaa("mod_pgm", hModPgm);             	//-x(20)

   if (ttAmemlg.ti <=0) {
      ttAmemlg.ti =ppStmtCrt("ttAmemlg",ttAmemlg.getSql());
   }

   sqlExec(ttAmemlg.ti, ttAmemlg.getParms());
   if (sqlNrow <= 0) {
      printf("Insert table mkt_member_log failed; kk[%s]", hh_card_no);
      //errExit(0);
   }
}
//===================
int iiFileNum=-1;
String isFileName="";
void fileOpen() throws Exception {
   isFileName ="FEETCB_0"+is_busiDateTW+".TXT";
   //--------
   //-FEETCB_0YYYMMDD.TXT-
   String lsPath= getEcsHome() + "/media/mkt/"+isFileName;
   printf("open out-file [%s]", lsPath);
   iiFileNum = openOutputText(lsPath);
   if (iiFileNum <0) {
      printf("此路徑或檔案不存在, file[%s]",lsPath);
      okExit(0);
   }
}
//------------
int ftpMput(String filename) throws Exception {
   String procCode = "";

   CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
   //CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

   //commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgTransSeqno = ecsModSeq(10); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = javaProgram; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "NCR2TCB"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/mkt/", getEcsHome());
   commFTP.hEflgModPgm = javaProgram;
   String hEflgRefIpCode = "NCR2TCB";

   System.setProperty("user.dir", commFTP.hEriaLocalDir);

   procCode = "mput " + filename;

   showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

   int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
   if (errCode != 0) {
      printf("%s FTP =[%s]無法連線 error", javaProgram, procCode);
      //errmsg("%s FTP =[%s]無法連線 error", javaProgram);
      //errExit(0);
   }
   return 0;
}

}
