/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  每月1日
 *  112-07-07  V1.00.00  Alex        initial                                  *
 *  112-07-17  V1.00.01  Alex        add FTP                                  *
 *  2023-0831  V1.00.01    JH    check run date
 *****************************************************************************/
package Rsk;

import java.text.DecimalFormat;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class RskR149 extends BaseBatch {
private final String progname = "產生分行商務卡期中覆審清單報表 on-demand 2023-0831 V1.00.01";
CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
CommString commString = new CommString();
CommDate commDate = new CommDate();
CommFTP commFTP = null;
CommRoutine comr = null;

String fileName = "";
private int iiFileNum = 0;
String busiTwDate = "";
String newLine = "\r\n";

String lastBranch = "";
String branch = "";
String branchFullName = "";
String corpNo = "";
String corpName = "";
String cardNo = "";
String chiName = "";
String reviewMonth = "";
String cardSince = "";
double idLimit = 0.0;
double cardLimit = 0.0;
String corpTel = "";
double lastYearConsume = 0.0;
double thisYearConsume = 0.0;
String cardLastSix = "";
int pageNo = 0;
int detailCnt = 0;

String searchMonth1 = "";
String searchMonth2 = "";

public static void main(String[] args) {
   RskR149 proc = new RskR149();
   proc.mainProcess(args);
   proc.systemExit();
}


@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskR149 [business_date]");
      errExit(1);
   }

   dbConnect();
   if (liArg == 1) {
      this.setBusiDate(args[0]);
   } else {
      hBusiDate = sysDate;
   }
   //-check 每月01日執行---
   String lsDD =commString.right(hBusiDate,2);
   if (!eq(lsDD,"01")) {
      printf("--每月01日產生檔案, busiDate[%s]-", hBusiDate);
      okExit(0);
   }

   busiTwDate = commDate.toTwDate(hBusiDate);
   searchMonth1 = commString.mid(hBusiDate,4,2);  //.substring(4, 6);
   searchMonth2 = commString.mid(commDate.dateAdd(hBusiDate, 0, 6, 0),4,2);  //.substring(4, 6);

   printf(" --處理日期: busiDate[%s], busiTwDate[%s], searchMonth1[%s], searchMonth2[%s]--"
    , hBusiDate, busiTwDate, searchMonth1,searchMonth2);

   fileName = "RCRM149.1.TXT";
   checkOpen();

   procData();

   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   renameFile();

   endProgram();
}

void procFTP() throws Exception {
   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "BREPORT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;
   showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
   int errCode = commFTP.ftplogName("BREPORT", "mput " + fileName);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 " + fileName + ".txt" + " 資料" + " errcode:" + errCode);
      insertEcsNotifyLog(fileName);
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
   String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
   String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName + "_" + sysDate);

   if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
      showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr4 + "]");

}

void procData() throws Exception {

   sqlCmd = " select reg_bank_no , uf_corp_no(corp_p_seqno) as corp_no , uf_corp_name(corp_p_seqno) as corp_name , card_no , "
             + " uf_chi_name(card_no) as chi_name , review_month , card_since , id_limit , card_limit , "
             + " corp_tel , last_year_consume , this_year_consume , crt_date , crt_time "
             + " from rsk_review_corp_list "
             +" where 1=1 "
             + " and (substr(review_month,5,2) = ? "
             + " or substr(card_since,5,2) in (?,?)) "
             + " order by reg_bank_no Asc , corp_p_seqno Asc  "
   ;

   setString(1, searchMonth1);
   setString(2, searchMonth1);
   setString(3, searchMonth2);

   openCursor();

   while (fetchTable()) {
      totalCnt++;
      initData();
      branch = colSs("reg_bank_no");
      corpNo = colSs("corp_no");
      corpName = colSs("corp_name");
      cardNo = colSs("card_no");
      chiName = colSs("chi_name");
      reviewMonth = colSs("review_month");
      cardSince = colSs("card_since");
      idLimit = colNum("id_limit");
      cardLimit = colNum("card_limit");
      corpTel = colSs("corp_tel");
      lastYearConsume = colNum("last_year_consume");
      thisYearConsume = colNum("this_year_consume");
      //cardLastSix = cardNo.substring(cardNo.length() - 6, cardNo.length());
      cardLastSix =commString.right(cardNo,6);
      //--分行名稱
      selectGenBrn();

      if (lastBranch.isEmpty() || lastBranch.equals(branch) == false) {
         if (lastBranch.isEmpty() == false) {
            printFooter();
         }
         pageNo = 1;
         printHeader(pageNo);
         lastBranch = branch;
      }

      printDetail();

   }
   printFooter();
   closeCursor();
   closeOutputText(iiFileNum);
}

void initData() {
   branch = "";
   corpNo = "";
   corpName = "";
   cardNo = "";
   chiName = "";
   reviewMonth = "";
   cardSince = "";
   idLimit = 0;
   cardLimit = 0;
   corpTel = "";
   lastYearConsume = 0;
   thisYearConsume = 0;
}

void checkOpen() throws Exception {
   //		fileName = "RCRM149.1.TXT";
   String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);

   iiFileNum = openOutputText(lsFile, "MS950");
   if (iiFileNum < 0) {
      showLogMessage("I", "", "無權限產擋");
      errExit(1);
   }

   return;
}

void printDetail() throws Exception {
   StringBuffer tempBuf = new StringBuffer();

   tempBuf.append(comc.fixLeft(corpName, 26));
   tempBuf.append(commString.space(1));
   tempBuf.append(comc.fixLeft(corpNo, 9));
   tempBuf.append(comc.fixRight(cardLastSix, 6));
   tempBuf.append(commString.space(3));
   tempBuf.append(comc.fixLeft(chiName, 12));
   String ss =commString.mid(reviewMonth,4);
   tempBuf.append(comc.fixRight(ss, 3));
   tempBuf.append(commString.space(1));
   tempBuf.append(comc.fixLeft(cardSince, 8));
   tempBuf.append(commString.space(2));
   tempBuf.append(comc.fixRight(getNumbericValue(idLimit), 11));
   tempBuf.append(commString.space(2));
   tempBuf.append(comc.fixRight(getNumbericValue(cardLimit), 11));
   tempBuf.append(commString.space(2));
   tempBuf.append(comc.fixLeft(corpTel, 12));
   tempBuf.append(comc.fixRight(getNumbericValue(lastYearConsume), 10));
   tempBuf.append(commString.space(2));
   tempBuf.append(comc.fixRight(getNumbericValue(thisYearConsume), 11));
   tempBuf.append(newLine);
   writeTextFile(iiFileNum, tempBuf.toString());
   detailCnt++;
}

void printFooter() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   tempBuf.append("合計" + newLine + newLine);
   writeTextFile(iiFileNum, tempBuf.toString());
}

void printHeader(int page) throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   String reportBank = "合作金庫商業銀行", reportName = "法人當月需覆審之客戶明細月報", reportId = "CRM149";
   String twYear = "", twMonth = "", twDate = "", reportTwDate = "", title = "";

   //--test

   twYear = commString.left(busiTwDate, busiTwDate.length() - 4);
   twMonth = busiTwDate.substring(busiTwDate.length() - 4).substring(0, 2);
   twDate = commString.right(busiTwDate,2);  //.substring(busiTwDate.length() - 2);

   reportTwDate = "中華民國 " + twYear + " 年 " + twMonth + " 月 " + twDate + " 日";

   //--控制行
   tempBuf.append(commString.rpad(branch, 10));
   tempBuf.append(commString.rpad(reportId, 16));
   tempBuf.append(commString.rpad(busiTwDate + reportName, 88) + "N");
   tempBuf.append(newLine);

   //--銀行
   tempBuf.append(commString.space(57));
   tempBuf.append(reportBank);
   tempBuf.append(newLine);

   //--分行、名稱、保存期限

   tempBuf.append(commString.rpad(" 分行代號:" + branch + " " + branchFullName, 43));
   tempBuf.append(commString.rpad(reportName, 53));
   tempBuf.append("保存期限:一年");
   tempBuf.append(newLine);

   //--報表代號、科目代號、日期、頁次
   tempBuf.append(" 報表代號:");
   tempBuf.append(commString.rpad(reportId, 10));
   tempBuf.append(commString.rpad(" 科目代號: ", 28));
   tempBuf.append(commString.rpad(reportTwDate, 64));
   tempBuf.append("第");
   tempBuf.append(commString.lpad(commString.int2Str(page), 4, "0"));
   tempBuf.append("頁");
   tempBuf.append(newLine);

   //--分隔
   tempBuf.append("====================================================================================================================================").append(newLine);

   //--欄位一
   title = " 公司名稱                  客戶編號 卡號     持卡人   覆審月  開戶日          公司    卡片額度     聯絡電話      上年度       本年度";
   tempBuf.append(commString.rpad(title, 132));
   tempBuf.append(newLine);

   title = "";
   //--欄位二
   title = "                                    末六碼   姓名                           總額度                                 消費         消費";
   tempBuf.append(commString.rpad(title, 132));
   tempBuf.append(newLine);

   //--分隔
   tempBuf.append("====================================================================================================================================").append(newLine);

   writeTextFile(iiFileNum, tempBuf.toString());
}

int tiGenBrn = -1;

void selectGenBrn() throws Exception {
   if (tiGenBrn <= 0) {
      sqlCmd = "select full_chi_name from gen_brn where branch = ? ";
      tiGenBrn = ppStmtCrt("ti-S-genBrn", "");
   }

   setString(1, branch);

   sqlSelect(tiGenBrn);
   if (sqlNrow <= 0) {
      branchFullName = "";
      return;
   }

   branchFullName = colSs("full_chi_name");
}

String getNumbericValue(double a) {
   String number = "";
   number = DecimalFormat.getNumberInstance().format(a);
   return number;
}

}
