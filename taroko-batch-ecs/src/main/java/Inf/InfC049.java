/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/12/20  V1.00.00    castor                       program initial        *
 *  2023-1206 V1.00.01     JH    fileName: date-1天
 ******************************************************************************/

package Inf;

import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.BaseBatch;
import com.CommString;

public class InfC049 extends BaseBatch {
private final String progname = "產生送CRDB 49異動CRRCUR現欠額度資料檔程式  2023-1206 V1.00.01";
CommCrd comc = new CommCrd();
CommFTP commFTP = null;
CommRoutine comr = null;
String isFileName = "";
private int ilFile49;
String isProcDate = "";
String hCardNo = "";
String hIdNo = "";
String hPSeqno = "";
String hSign = "";
int hAcctJrnlBal = 0;

public static void main(String[] args) {
   InfC049 proc = new InfC049();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);
   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : InfC049 [proc_date]");
      okExit(0);
   }
   dbConnect();
   if (liArg == 1) {
      if (commDate.isDate(args[0])) {
         isProcDate = args[0];
      }
   }

   if (empty(isProcDate)) {
      isProcDate =commDate.dateAdd(sysDate,0,0,-1);
   }
   isFileName = "CRU23B1_TYPE_49_"+isProcDate+".txt";
   printf("Process: proc_Date[%s], file_name[%s]"
       ,isProcDate,isFileName);

   checkOpen();
   selectIdNo(isProcDate);
   closeOutputText(ilFile49);

   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   renameFile();

   //--
   sqlCommit();
   endProgram();
}


void selectIdNo(String aProcDate) throws Exception {
//   sqlCmd = "select decode(a.FL_FLAG,'Y',a.card_no,decode(a.corp_p_seqno,'','',a.card_no)) card_no ,uf_idno_id(decode(a.corp_p_seqno,'',a.major_id_p_seqno,a.id_p_seqno)) id_no , b.p_seqno p_seqno ";
//   sqlCmd += " from crd_card a ,act_jrnl b ";
//   sqlCmd += "where a.P_SEQNO = b.P_SEQNO ";
//   sqlCmd += "AND b.ACCT_DATE =? ";
   sqlCmd ="select decode(a.FL_FLAG,'Y',a.card_no,decode(a.corp_p_seqno,'','',a.card_no)) card_no "+
       ", (SELECT id_no FROM crd_idno WHERE id_p_seqno=decode(A.corp_p_seqno,'',A.major_id_p_seqno,A.id_p_seqno)) AS id_no "+
       ", B.p_seqno "+
       " from crd_card A join act_jrnl B on A.P_SEQNO = B.P_SEQNO "+
       "where 1=1 "+
//       "AND A.P_SEQNO = B.P_SEQNO "+
       "AND B.ACCT_DATE =? "
       ;
   setString(1, aProcDate);
   printf("process CURSOR.open.......");
   openCursor();
   while (fetchTable()) {
      totalCnt++;
      dspProcRow(5000);

      hCardNo = colSs( "card_no");
      hIdNo = colSs( "id_no");
      hPSeqno = colSs( "p_seqno");
      getAcctJrnlBal(hPSeqno);
      if (hAcctJrnlBal < 0) {
         hSign = "-";
      } else {
         hSign = "+";
      }
      writeTextFile();
   }
   closeCursor();
   printf("Process Cursor.close rows=[%s]", totalCnt);
}

void checkOpen() throws Exception {
   String lsTemp = "";
   lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
   ilFile49 = openOutputText(lsTemp, "big5");
   if (ilFile49 < 0) {
      printf("CRU23B1-TYPE-49 產檔失敗 ! ");
      okExit(0);
   }
}

void writeTextFile() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   String tempStr = "", newLine = "\r\n";
   tempBuf.append("49"); // --代碼 固定 49
   tempBuf.append(comc.fixLeft(hCardNo, 16)); //--卡號 16 碼
   tempBuf.append(comc.fixLeft(hIdNo, 11)); // --主卡ID , 11 碼
   tempBuf.append(comc.fixLeft(hSign, 1)); // --新現欠額度(SIGN)  , 1 碼
   tempBuf.append(comc.fixLeft(String.format("%09d%n", Math.abs(hAcctJrnlBal)), 9));//--新現欠額度(目前現欠金額)  , 9 碼
   tempBuf.append(comc.fixLeft("", 111));
   tempBuf.append(newLine);

   this.writeTextFile(ilFile49, tempBuf.toString());
}

void procFTP() throws Exception {
   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   // System.setProperty("user.dir",commFTP.h_eria_local_dir);
   showLogMessage("I", "", "mput "+isFileName+" 開始傳送....");
   int errCode = commFTP.ftplogName("NCR2TCB", "mput "+isFileName);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 "+isFileName+" 資料"+" errcode:"+errCode);
      insertEcsNotifyLog(isFileName);
   }
}

public int insertEcsNotifyLog(String fileName) throws Exception {
   setValue("crt_date", sysDate);
   setValue("crt_time", sysTime);
   setValue("unit_code", comr.getObjectOwner("3", javaProgram));
   setValue("obj_type", "3");
   setValue("notify_head", "無法 FTP 傳送 "+fileName+" 資料");
   setValue("notify_name", "媒體檔名:"+fileName);
   setValue("notify_desc1", "程式 "+javaProgram+" 無法 FTP 傳送 "+fileName+" 資料");
   setValue("notify_desc2", "");
   setValue("trans_seqno", commFTP.hEflgTransSeqno);
   setValue("mod_time", sysDate+sysTime);
   setValue("mod_pgm", javaProgram);
   daoTable = "ecs_notify_log";

   insertTable();

   return (0);
}

void renameFile() throws Exception {
   String tmpstr1 = String.format("%s/media/crdb/%s", getEcsHome(), isFileName);
   String tmpstr2 = String.format("%s/media/crdb/backup/%s", getEcsHome(), isFileName);

   if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
      showLogMessage("I", "", "ERROR : 檔案["+isFileName+"]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 ["+isFileName+"] 已移至 ["+tmpstr2+"]");
}

void getAcctJrnlBal(String p_seqno) throws Exception {

   sqlCmd = "SELECT floor(acct_jrnl_bal) acct_jrnl_bal ";
   sqlCmd += "  from act_acct  ";
   sqlCmd += " where p_seqno = ? ";

   setString(1, p_seqno);
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
      hAcctJrnlBal = 0;
   }
   if (recordCnt > 0) {
      hAcctJrnlBal = getValueInt("acct_jrnl_bal");
   }
}

}
