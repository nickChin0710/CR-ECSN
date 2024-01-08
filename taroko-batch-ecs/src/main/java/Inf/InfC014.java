/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  109/04/22  V1.00.00    Alex      program initial							 *
 *  109-07-03  V1.00.01    shiyuqi       updated for project coding standard  *
 *  109-07-22  V1.00.01    yanghan                           修改了字段名称                    *
 *  109-10-19  V1.00.03    shiyuqi       updated for project coding standard  *
 *  109-11-11  V1.00.04    tanwei        updated for project coding standard  *
 *  110-01-30  V1.00.05    Alex          改用營業日								 *
 *  111/02/14  V1.00.06    Ryan      big5 to MS950
 *  2023-1206 V1.00.07     JH       file: date -1天
 *****************************************************************************/

package Inf;

import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;
import com.BaseBatch;

public class InfC014 extends BaseBatch {
private final String progname = "產生送CRDB 14 卡片信用成數變動  2023-1206 V1.00.07";
CommCrd comc = new CommCrd();
CommFTP commFTP = null;
CommRoutine comr = null;

String isFileName = "";
private int ilFile14;
String hIdPSeqno = "";
boolean hContinue = false;
String isProcDate = "";
String hIdNo = "";
String hOppoStatus = "";
String hCardNo = "";
String hAcnoPSeqno = "";
String hSonCardFlag = "";
String hAcctType = "";
String hAdjEffDate1 = "";
String hAdjEffDate2 = "";
int hBefAmt = 0;
int hAftAmt = 0;
int hAftCash = 0;
int hCardAcctIdx = 0;
int hCardAdjLimit = 0;
double hCalBefAmt = 0.0;
double hCalAftAmt = 0.0;
double hCalBefCash = 0.0;
double hCalAftCash = 0.0;
String hCardAdjDate1 = "";
String hType = "";

public static void main(String[] args) {
   InfC014 proc = new InfC014();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);
   dateTime();
   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : InfC014 [proc_date]");
      okExit(0);
   }

   dbConnect();
   if (liArg == 1) {
      if (commDate.isDate(args[0])) {
         isProcDate = args[0];
      }
   }
   if (empty(isProcDate)) {
      isProcDate =commDate.dateAdd(hBusiDate,0,0,-1);
   }
   isFileName = "CRU23B1_TYPE_14_"+isProcDate+".txt";
   printf("Process: proc_date[%s], file_name[%s]", isProcDate,isFileName);

   checkOpen();
   selectDataType14();
   closeOutputText(ilFile14);

   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   renameFile();
   //---
   sqlCommit();
   endProgram();
}

void selectDataType14() throws Exception {
   fetchExtend = "type14.";
   sqlCmd =" select id_p_seqno , acno_p_seqno "
       +" from rsk_acnolog "
       +" where emend_type ='5' "
       +" and log_date = ? "
       +" group by id_p_seqno , acno_p_seqno ";
   setString(1, isProcDate);
   openCursor();
   while (fetchTable()) {
      initData();
      hIdPSeqno = colSs("type14.id_p_seqno");
      checkOnlyOneType5(hIdPSeqno);
      if (hContinue)
         continue;
      // h_id_no = col_ss("type14.id_no");
      hAcnoPSeqno = colSs("type14.acno_p_seqno");
      hAftAmt = getAcnoAmt(hAcnoPSeqno);
      // h_aft_cash = col_int("type14.aft_loc_cash");
      selectAnotherCard14(hAcnoPSeqno);
   }
   closeCursor();

   // --永調額度調整 連帶變動預借現金額度
   fetchExtend = "type141.";
   sqlCmd =" select acno_p_seqno , id_p_seqno "
       +" from rsk_acnolog "
       +" where log_date = ? "
       +" and emend_type ='1' "
       +" and son_card_flag <> 'Y' "
       +" group by id_p_seqno , acno_p_seqno ";
   setString(1, isProcDate);
   openCursor();
   while (fetchTable()) {
      initData();
      hIdPSeqno = colSs("type141.id_p_seqno");
      hAcnoPSeqno = colSs("type141.acno_p_seqno");
      checkOnlyOneType1(hIdPSeqno);
      if (hContinue)
         continue;
      // h_cal_bef_amt = col_num("type141.bef_loc_amt");
      // h_cal_bef_cash = col_num("type141.bef_loc_cash");
      hAftAmt = getAcnoAmt(hAcnoPSeqno);
      hAftCash = getAcnoCash(hAcnoPSeqno);
      hCalAftAmt = hAftAmt;
      hCalAftCash = hAftCash;
      if (checkCashMod() == false)
         continue;
      selectAnotherCard14(hAcnoPSeqno);
   }
   closeCursor();
}

int getAcnoCash(String acnoPSeqno) throws Exception {
   sqlCmd = " select line_of_credit_amt_cash from act_acno where acno_p_seqno = ? ";
   setString(1, acnoPSeqno);
   sqlSelect();
   if (sqlNrow > 0)
      return colInt("line_of_credit_amt_cash");
   return -1;

}

int getAcnoAmt(String acnoPSeqno) throws Exception {
   sqlCmd = " select line_of_credit_amt from act_acno where acno_p_seqno = ? ";
   setString(1, acnoPSeqno);
   sqlSelect();
   if (sqlNrow > 0)
      return colInt("line_of_credit_amt");
   return -1;
}

void checkOpen() throws Exception {
   String lsTemp = "";
   lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
   ilFile14 = openOutputText(lsTemp, "MS950");
   if (ilFile14 < 0) {
      printf("CRU23B1-TYPE-14 產檔失敗 ! ; file[%s]", lsTemp);
      okExit(0);
   }
}

void selectAnotherCard14(String acnoPSeqno) throws Exception {
   int llCnt = 0;
   daoTid = "card1.";
   sqlCmd = "";
   sqlCmd += " select card_no from crd_card where acno_p_seqno = ? ";
   setString(1, acnoPSeqno);
   sqlSelect();
   llCnt = sqlNrow;
   if (sqlNrow <= 0)
      return;
   for (int ii = 0; ii < llCnt; ii++) {
      hCardNo = "";
      hCardNo = colSs(ii, "card1.card_no");
      writeTextFile14();
   }
}

void writeTextFile14() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   String tempStr = "", newLine = "\r\n";
   int tempInt = 0;
   tempBuf.append("14"); // --代碼 固定 14
   tempBuf.append(comc.fixLeft(hCardNo, 16)); // --卡號 16
   tempBuf.append(comc.fixLeft(hIdNo, 11)); // --主身分證 11 碼
   tempInt = hAftCash * 100 / hAftAmt;
   tempStr = commString.int2Str(tempInt);
   tempBuf.append(comc.fixLeft(tempStr, 2)); // --成數 2 碼
   tempBuf.append(comc.fixLeft("", 119)); // --保留 119

   tempBuf.append(newLine);
   writeTextFile(ilFile14, tempBuf.toString());

   totalCnt++;
   processDisplay(2000);
}

boolean checkCashMod() {
   double ldBefRate = 0.0, ldAftRate = 0.0;
   ldBefRate = hCalBefCash / hCalBefAmt * 100;
   ldAftRate = hCalAftCash / hCalAftAmt * 100;
   if (ldBefRate == ldAftRate)
      return false;
   return true;
}

void checkOnlyOneType5(String lsIdPSeqno) throws Exception {
   String sql1 = "";
   sql1 = " select emend_type , uf_idno_id2(id_p_seqno,acct_type) as id_no , aft_loc_cash , aft_loc_amt ";
   sql1 += " from rsk_acnolog where 1=1 and emend_type in ('1','5') and log_date = ? ";
   sql1 += " and id_p_seqno = ? order by mod_time Desc fetch first 1 rows only ";

   setString(1, isProcDate);
   setString(2, lsIdPSeqno);

   sqlSelect(sql1);

   if (sqlNrow > 0) {
      hAftCash = colInt("aft_loc_cash");
      hIdNo = colSs("id_no");
      hType = colSs("emend_type");
      if (hType.equals("5") == false)
         hContinue = true;
   }

}

void checkOnlyOneType1(String lsIdPSeqno) throws Exception {
   String sql1 = "";
   sql1 = " select emend_type , uf_idno_id2(id_p_seqno,acct_type) as id_no , bef_loc_cash , bef_loc_amt ";
   sql1 += " from rsk_acnolog where log_date = ? and emend_type in ('1','5') and son_card_flag <> 'Y' ";
   sql1 += " and id_p_seqno = ? order by mod_time Desc fetch first 1 rows only ";

   setString(1, isProcDate);
   setString(2, lsIdPSeqno);

   sqlSelect(sql1);
   if (sqlNrow > 0) {
      hIdNo = colSs("id_no");
      hCalBefCash = colInt("bef_loc_cash");
      hCalBefAmt = colInt("bef_loc_amt");
      hType = colSs("emend_type");
      if (hType.equals("1") == false)
         hContinue = true;
   }
}

void initData() {
   hIdNo = "";
   hOppoStatus = "";
   hCardNo = "";
   hSonCardFlag = "";
   hAcctType = "";
   hAcnoPSeqno = "";
   hCardAcctIdx = 0;
   hBefAmt = 0;
   hAftAmt = 0;
   hAftCash = 0;
   hCalBefAmt = 0.0;
   hCalAftAmt = 0.0;
   hCalBefCash = 0.0;
   hCalAftCash = 0.0;
   hCardAdjDate1 = "";
   hCardAdjLimit = 0;
   hIdPSeqno = "";
   hContinue = false;
   hType = "";
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

}
