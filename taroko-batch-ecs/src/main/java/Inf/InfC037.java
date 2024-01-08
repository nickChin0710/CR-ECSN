/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  109/04/22  V1.00.00    Alex      program initial							 *
 *  109-07-03  V1.00.01   shiyuqi       updated for project coding standard   *
 *  109-07-22  V1.00.02   yanghan       修改了字段名称                                                                              *
 *  109-10-19  V1.00.03   shiyuqi       updated for project coding standard   *
 *  109-11-11  V1.00.04    tanwei        updated for project coding standard  *
 *  110-01-30  V1.00.05    Alex          改用營業日								 *
 *  111/02/14  V1.00.06    Ryan      big5 to MS950
 *  2023-1206 V1.00.07     JH       fileName=date -1天
 *****************************************************************************/

package Inf;

import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;
import com.BaseBatch;

public class InfC037 extends BaseBatch {
private final String progname = "產生送CRDB 37 ORG306額度異動  2023-1206 V1.00.07";
CommCrd comc = new CommCrd();
CommFTP commFTP = null;
CommRoutine comr = null;
String isFileName = "";
private int ilFile37;
String isProcDate = "";
String hIdPSeqno = "";
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
boolean hContinue = false;

public static void main(String[] args) {
   InfC037 proc = new InfC037();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);
   dateTime();
   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : InfC037 [proc_date]");
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
   isFileName = "CRU23B1_TYPE_37_"+isProcDate+".txt";
   printf("Process: proc_date[%s], file_name[%s]", isProcDate,isFileName);

   checkOpen();
   selectDataType37();
   closeOutputText(ilFile37);

   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   renameFile();

   sqlCommit();
   endProgram();
}

void selectDataType37() throws Exception {
   fetchExtend = "type370.";
   sqlCmd =" select distinct card_no , acno_p_seqno "
       +" from rsk_acnolog "
       +" where emend_type = '3' "
       +" and log_date = ? "
       +" and acct_type ='03' "
//       +" group by card_no , acno_p_seqno "
   ;
   setString(1, isProcDate);
   openCursor();
   while (fetchTable()) {
      initData();
      hCardNo = colSs("type370.card_no");
      hAcnoPSeqno = colSs("type370.acno_p_seqno");
      checkOnlyOneCnt(hCardNo);
      checkAdjStart(hAcnoPSeqno);
      if (hContinue)
         continue;
      // h_aft_amt = col_int("business.aft_loc_amt");
      hIdNo = getMajorId(hCardNo);
      writeTextFile37();
   }
   closeCursor();

   // --臨調開始
   fetchExtend = "type371.";
   sqlCmd =" select uf_idno_id2(id_p_seqno,debit_flag) as id_no "
       +", tot_amt_month , acno_p_seqno "
       +" from cca_card_acct "
       +" where adj_eff_start_date = ? "
   ;

   setString(1, isProcDate);
   openCursor();
   while (fetchTable()) {
      initData();
      hIdNo = colSs("type371.id_no");
      hAftAmt = colInt("type371.tot_amt_month");
      hAcnoPSeqno = colSs("type371.acno_p_seqno");
      checkAcctType(hAcnoPSeqno);
      if (hContinue)
         continue;
      selectAnotherCard(hAcnoPSeqno);
   }
   closeCursor();

   // --臨調結束
   String hTempDate = "";
   hTempDate = commDate.dateAdd(isProcDate, 0, 0, -1);
   fetchExtend = "type372.";
   sqlCmd =" select id_p_seqno "
       +", uf_idno_id2(id_p_seqno,debit_flag) as id_no "
       +", tot_amt_month , acno_p_seqno "
       +" from cca_card_acct "
       +" where adj_eff_end_date = ? "
   ;

   setString(1, hTempDate);
   openCursor();

   while (fetchTable()) {
      initData();
      hIdPSeqno = colSs("type372.id_p_seqno");
      hAcnoPSeqno = colSs("type372.acno_p_seqno");
      checkAcctType(hAcnoPSeqno);
      if (hContinue) continue;
      checkAcnolog(hIdPSeqno);
      if (hContinue) continue;
      hIdNo = colSs("type372.id_no");
      hAftAmt = getAcnoAmt(hAcnoPSeqno);
      selectAnotherCard(hAcnoPSeqno);
   }
   closeCursor();

   // --臨調刪除
   fetchExtend = "type373.";
   sqlCmd =" select distinct card_acct_idx "
       +" from cca_limit_adj_log "
       +" where log_date = ? "
       +" and adj_eff_date1 = '' "
       +" and adj_eff_date2 = '' "
   ;
//   sqlCmd += " group by card_acct_idx ";

   setString(1, isProcDate);
   openCursor();
   while (fetchTable()) {
      initData();
      hCardAcctIdx = colInt("type373.card_acct_idx");
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
      selectAnotherCard(hAcnoPSeqno);
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

String getAcnoPseqnoCardAcct(int cardAcctIdx) throws Exception {
   sqlCmd = "select acno_p_seqno from cca_card_acct where card_acct_idx = ?  ";
   setInt(1, cardAcctIdx);
   sqlSelect();
   if (sqlNrow > 0)
      return colSs("acno_p_seqno");
   return "";
}

void selectAnotherCard(String lsAcnoPSeqno) throws Exception {
   String sql1 = "";
   daoTid = "card1.";
   sql1 = " select card_no from crd_card where acno_p_seqno = ? ";
   setString(1, lsAcnoPSeqno);
   sqlSelect(sql1);
   for (int ii = 0; ii < sqlNrow; ii++) {
      hCardNo = colSs(ii, "card1.card_no");
      writeTextFile37();
   }
}

void checkAcctType(String lsAcnoPSeqno) throws Exception {
   // --確認臨調帳戶是否為03戶
   String sql1 = "";
   sql1 = " select acct_type from act_acno where acno_p_seqno = ? ";
   setString(1, lsAcnoPSeqno);
   sqlSelect(sql1);
   if (sqlNrow > 0) {
      if (colEq("acct_type", "03") == false)
         hContinue = true;
   }
}

void writeTextFile37() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   String tmpStr = "", newLine = "\r\n";
   tempBuf.append("37"); // --代碼 固定 37
   tempBuf.append(comc.fixLeft(hCardNo, 16)); // --卡號
   tempBuf.append(comc.fixLeft(hIdNo, 11)); // --主身分證 11 碼
   tmpStr = commString.int2Str(hAftAmt); // --額度
   tempBuf.append(comc.fixLeft(commString.lpad(tmpStr, 9, "0"), 9));
   tempBuf.append(comc.fixLeft("", 112)); // --保留 112
   tempBuf.append(newLine);

   this.writeTextFile(ilFile37, tempBuf.toString());

   totalCnt++;
   processDisplay(2000);
}

String getMajorId(String cardNo) throws Exception {
   sqlCmd = " select uf_idno_id2(major_id_p_seqno,debit_flag) as id_no from cca_card_base where card_no = ? ";
   setString(1, cardNo);
   sqlSelect();
   if (sqlNrow > 0)
      return colSs("id_no");
   return "";
}

void checkOnlyOneCnt(String lsCardNo) throws Exception {
   String sql1 = "";
   sql1 += " select aft_loc_amt from rsk_acnolog where emend_type = '3' and log_date = ? ";
   sql1 += " and acct_type ='03' and card_no = ? ";
   setString(1, isProcDate);
   setString(2, lsCardNo);
   sqlSelect(sql1);
   if (sqlNrow > 0) {
      hAftAmt = colInt("aft_loc_amt");
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

void checkOpen() throws Exception {
   String lsTemp = "";
   lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
   ilFile37 = openOutputText(lsTemp, "MS950");
   if (ilFile37 < 0) {
      printf("CRU23B1-TYPE-37 產檔失敗 ! ; file=[%s]", lsTemp);
      okExit(0);
   }
}

int getAcnoAmt(String acnoPSeqno) throws Exception {
   sqlCmd = " select line_of_credit_amt from act_acno where acno_p_seqno = ? ";
   setString(1, acnoPSeqno);
   sqlSelect();
   if (sqlNrow > 0)
      return colInt("line_of_credit_amt");
   return -1;
}

void checkAcnolog(String lsIdPSeqno) throws Exception {
   // --臨調結束後確認當天有無永調
   String sql1 = "";
   sql1 = "select uf_idno_id2(id_p_seqno,acct_type) as id_no , bef_loc_amt , aft_loc_amt ";
   sql1 += " from rsk_acnolog ";
   sql1 += " where emend_type ='3' and acct_type ='03' and log_date = ? and id_p_seqno = ? ";
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
