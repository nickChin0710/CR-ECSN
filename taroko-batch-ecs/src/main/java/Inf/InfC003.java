/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  109-07-03  V1.00.00   shiyuqi       updated for project coding standard   *
 *  109-07-22  V1.00.01   yanghan       修改了字段名称                                                                              *
 *  109-10-19  V1.00.02   shiyuqi       updated for project coding standard   *
 *  109-11-11  V1.00.03   tanwei        updated for project coding standard   *
 *  110-01-30  V1.00.04   Alex          改用營業日								 *
 *  111/02/14  V1.00.05    Ryan      big5 to MS950
 *  2023-1206 V1.00.06     JH    fileName: date-1天
 *****************************************************************************/
package Inf;

import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;
import com.BaseBatch;

public class InfC003 extends BaseBatch {
private final String progname = "產生送CRDB 03 信用額度異動 2023-1206 V1.00.06";
CommCrd comc = new CommCrd();
CommFTP commFTP = null;
CommRoutine comr = null;
//------
private int ilFile03;
boolean hContinue = false;
String isFileName = "";
String isProcDate = "";
String hIdNo = "";
String hIdPSeqno = "";
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

public static void main(String[] args) {
   InfC003 proc = new InfC003();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);
   dateTime();
   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : InfC003 [business_date]");
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
   isFileName = "CRU23B1_TYPE_03_"+isProcDate+".txt";
   printf("Process: proc_date[%s], file_name[%s]", isProcDate,isFileName);

   checkOpen();
   selectDataType03();
   closeOutputText(ilFile03);

   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   renameFile();

   //--------------
   sqlCommit();
   endProgram();
}

void selectDataType03() throws Exception {

   // --永調
   fetchExtend = "type03.";
   sqlCmd = "";
   sqlCmd += " select id_p_seqno , acno_p_seqno ";
   sqlCmd += " from rsk_acnolog "
       +" where emend_type ='1' "
       +" and son_card_flag <> 'Y' "
       +" and log_date = ? "
       +" and kind_flag ='A' "
       +" group by id_p_seqno , acno_p_seqno ";

   setString(1, isProcDate);

   openCursor();
   while (fetchTable()) {
      initData();
      hIdPSeqno = colSs("type03.id_p_seqno");
      hAcnoPSeqno = colSs("type03.acno_p_seqno");
      checkOnlyOneCnt(hIdPSeqno);
      checkAdjStart(hAcnoPSeqno);
      if (hContinue == true)
         continue;

      writeTextFile03();
   }
   closeCursor();

   // --臨調開始-Table:cca_card_acct , Where adj_eff_start_date = sys_date , 調整前:
   // act_acno.line_of_credit_amt 調整後: cca_card_acct.tot_amt_month
   fetchExtend = "type031.";
   sqlCmd =" select uf_idno_id2(id_p_seqno,debit_flag) as id_no "
       +", tot_amt_month , acno_p_seqno "
       +" from cca_card_acct "
       +" where adj_eff_start_date = ? ";

   setString(1, isProcDate);
   openCursor();

   while (fetchTable()) {
      initData();
      hIdNo = colSs("type031.id_no");
      hAftAmt = colInt("type031.tot_amt_month");
      hAcnoPSeqno = colSs("type031.acno_p_seqno");
      checkAcctType(hAcnoPSeqno);
      if (hContinue)
         continue;
      hBefAmt = getAcnoAmt(hAcnoPSeqno);
      writeTextFile03();
   }
   closeCursor();

   // --臨調結束:Table:cca_card_acct , Where adj_eff_end_date = sys_date-1 ,
   // 調整前:cca_card_acct.tot_amt_month 調整後: act_acno.line_of_credit_amt
   String hTempDate = "";
   hTempDate = commDate.dateAdd(isProcDate, 0, 0, -1);
   fetchExtend = "type032.";
   sqlCmd =" select id_p_seqno "
       +", uf_idno_id2(id_p_seqno,debit_flag) as id_no "
       +", tot_amt_month , acno_p_seqno "
       +" from cca_card_acct "
       +" where adj_eff_end_date = ? ";

   setString(1, hTempDate);
   openCursor();

   while (fetchTable()) {
      initData();
      hIdPSeqno = colSs("type32.id_p_seqno");
      hAcnoPSeqno = colSs("type032.acno_p_seqno");
      checkAcctType(hAcnoPSeqno);
      if (hContinue)
         continue;
      checkAcnolog(hIdPSeqno);
      if (hContinue)
         continue;
      hIdNo = colSs("type032.id_no");
      hAftAmt = getAcnoAmt(hAcnoPSeqno);
      hBefAmt = colInt("type032.tot_amt_month");
      writeTextFile03();
   }

   closeCursor();

   // --臨調刪除:Table:cca_limit_adj_log , where log_date = sys_date and adj_eff_date
   // ='' and adj_eff_date2 = '' , 調整前:cca_limit_adj_log.tot_amt_month_b ,
   // act_acno.line_of_credit_amt

   fetchExtend = "type033.";
   sqlCmd =" select distinct card_acct_idx "
       +" from cca_limit_adj_log "
       +" where log_date = ? "
       +" and adj_eff_date1 = '' "
       +" and adj_eff_date2 = '' "
//       +" group by card_acct_idx "
   ;

   setString(1, isProcDate);
   openCursor();
   while (fetchTable()) {
      initData();
      hCardAcctIdx = colInt("type033.card_acct_idx");
      // h_bef_amt = col_int("type033.tot_amt_month_b");
      checkOnlyOneAdj(hCardAcctIdx);
      hAcnoPSeqno = getAcnoPseqnoCardAcct(hCardAcctIdx);
      checkAcctType(hAcnoPSeqno);
      if (hContinue)
         continue;
      checkAcnolog(hIdPSeqno);
      if (hContinue)
         continue;
      checkCcaLimitAdjLog(hCardAcctIdx);
      if (hContinue)
         continue;
      hIdNo = getIdnoCardAcct(hCardAcctIdx);
      hAftAmt = getAcnoAmt(hAcnoPSeqno);
      writeTextFile03();
   }

   closeCursor();
}

String getIdnoCardAcct(int cardAcctIdx) throws Exception {
   sqlCmd = "select uf_idno_id2(id_p_seqno,debit_flag) as id_no from cca_card_acct where card_acct_idx = ? ";
   setInt(1, cardAcctIdx);
   sqlSelect();
   if (sqlNrow > 0)
      return colSs("id_no");
   return "";
}

void checkOpen() throws Exception {
   String lsTemp = "";
   lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
   ilFile03 = openOutputText(lsTemp, "MS950");
   if (ilFile03 < 0) {
      printf("CRU23B1-TYPE-03 產檔失敗 !, file[%s] ", lsTemp);
      okExit(0);
   }
}

void writeTextFile03() throws Exception {
   String nextLine = System.lineSeparator();
   StringBuilder tempBuf = new StringBuilder();
   String tempStr = "", newLine = "\r\n";

   tempBuf.append("03"); // --代碼 固定 03
   tempBuf.append(comc.fixLeft("", 16)); // --卡號 固定空白 16 碼
   tempBuf.append(comc.fixLeft(hIdNo, 11)); // --主身分證 11 碼
   tempStr = commString.int2Str(hBefAmt);
   tempBuf.append(comc.fixLeft(commString.lpad(tempStr, 9, "0"), 9)); // --調整前額度 未滿9位數時左補0
   tempStr = "";
   tempStr = commString.int2Str(hAftAmt);
   tempBuf.append(comc.fixLeft(commString.lpad(tempStr, 9, "0"), 9)); // --調整後額度 未滿9位數時左補0
   tempStr = "";
   tempBuf.append(comc.fixLeft("", 103)); // --保留 103
   tempBuf.append(newLine);

   this.writeTextFile(ilFile03, tempBuf.toString());

   totalCnt++;
   processDisplay(2000);
}

//  void writeTextFile03() throws Exception {
//    StringBuffer tempBuf = new StringBuffer();    
//    String tempStr = "";
//    tempBuf.append("03"); // --代碼 固定 03
//    tempBuf.append(commString.bbFixlen("", 16)); // --卡號 固定空白 16 碼
//    tempBuf.append(commString.bbFixlen(hIdNo, 11)); // --主身分證 11 碼
//    tempStr = commString.int2Str(hBefAmt);
//    tempBuf.append(commString.lpad(tempStr, 9,"0")); // --調整前額度 未滿9位數時左補0
//    tempStr = "";
//    tempStr = commString.int2Str(hAftAmt);
//    tempBuf.append(commString.lpad(tempStr, 9,"0")); // --調整後額度 未滿9位數時左補0
//    tempStr = "";
//    tempBuf.append(commString.bbFixlen("", 103)); // --保留 103
//    tempBuf.append(newLine);
//    totalCnt++;
//    this.writeTextFile(ilFile03, tempBuf.toString());  	  
//  }

int getAcnoAmt(String acnoPSeqno) throws Exception {
   sqlCmd = " select line_of_credit_amt from act_acno where acno_p_seqno = ? ";
   setString(1, acnoPSeqno);
   sqlSelect();
   if (sqlNrow > 0)
      return colInt("line_of_credit_amt");
   return -1;
}

String getAcnoPseqnoCardAcct(int cardAcctIdx) throws Exception {
   sqlCmd = "select acno_p_seqno from cca_card_acct where card_acct_idx = ?  ";
   setInt(1, cardAcctIdx);
   sqlSelect();
   if (sqlNrow > 0)
      return colSs("acno_p_seqno");
   return "";
}

void checkOnlyOneCnt(String lsIdPSeqno) throws Exception {
   // --確認當日只永調一筆
   String sql1 = "";
   sql1 = "select uf_idno_id2(id_p_seqno,acct_type) as id_no , bef_loc_amt , aft_loc_amt ";
   sql1 += " from rsk_acnolog ";
   sql1 += " where emend_type ='1' and son_card_flag <> 'Y' and log_date = ? and id_p_seqno = ? ";
   sql1 += " order by mod_time Desc fetch first 1 rows only ";
   setString(1, isProcDate);
   setString(2, lsIdPSeqno);
   sqlSelect(sql1);

   if (sqlNrow > 0) {
      hBefAmt = colInt("bef_loc_amt");
      hAftAmt = colInt("aft_loc_amt");
      hIdNo = colSs("id_no");
   }

}

void checkAcnolog(String lsIdPSeqno) throws Exception {
   // --臨調結束後確認當天有無永調
   String sql1 = "";
   sql1 = "select uf_idno_id2(id_p_seqno,acct_type) as id_no , bef_loc_amt , aft_loc_amt ";
   sql1 += " from rsk_acnolog ";
   sql1 += " where emend_type ='1' and son_card_flag <> 'Y' and log_date = ? and id_p_seqno = ? ";
   sql1 += " order by mod_time Desc fetch first 1 rows only ";
   setString(1, isProcDate);
   setString(2, lsIdPSeqno);
   sqlSelect(sql1);

   if (sqlNrow > 0) {
      hContinue = true;
   }
}

void checkOnlyOneAdj(int liCardAcctIdx) throws Exception {
   // --確認當日是否只刪除臨調一次
   String sql1 = "";
   sql1 = " select tot_amt_month_b ";
   sql1 += " from cca_limit_adj_log where log_date = ? and adj_eff_date1 = '' and adj_eff_date2 = '' ";
   sql1 += " and card_acct_idx = ? order by log_time Desc fetch first 1 rows only ";

   setString(1, isProcDate);
   setInt(2, liCardAcctIdx);

   sqlSelect(sql1);
   if (sqlNrow > 0) {
      hBefAmt = colInt("tot_amt_month_b");
   }

}

void checkAdjStart(String lsAcnoPSeqno) throws Exception {
   // --確認永調當日是否仍有臨調
   String sql1 = "";
   sql1 = " select hex(rowid) as rowid from cca_card_acct where adj_eff_start_date = ? ";
   sql1 += " and acno_p_seqno = ? and debit_flag <> 'Y' ";
   setString(1, isProcDate);
   setString(2, hAcnoPSeqno);
   sqlSelect(sql1);
   if (sqlNrow > 0) {
      hContinue = true;
   }
}

void checkAcctType(String lsAcnoPSeqno) throws Exception {
   // --確認臨調帳戶是否為個人戶
   String sql1 = "";
   sql1 = " select acct_type , id_p_seqno from act_acno where acno_p_seqno = ? ";
   setString(1, lsAcnoPSeqno);
   sqlSelect(sql1);
   if (sqlNrow > 0) {
      hIdPSeqno = colSs("id_p_seqno");
      if (colEq("acct_type", "01") == false) {
         hContinue = true;
      }
   }
}

void checkCcaLimitAdjLog(int liCardAcctIdx) throws Exception {
   // --確認是否為當日臨調當日刪除
   String sql1 = "select log_time from cca_limit_adj_log where log_date = ? and aud_code = 'A' and card_acct_idx = ? ";
   setString(1, isProcDate);
   setInt(2, liCardAcctIdx);
   sqlSelect(sql1);
   if (sqlNrow > 0) {
      hContinue = true;
   }
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
