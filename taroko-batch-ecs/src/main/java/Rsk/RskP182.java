package Rsk;
/**
 * 2023-0911  V1.00.01  JH    每月01日執行(產生上月資料)
 */

import com.BaseBatch;
import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;

import java.text.DecimalFormat;

public class RskP182 extends BaseBatch {
private final String progname = "產製個人／法人卡之信用及有效卡額度給卡部 2023-0911  V1.00.01";
CommCrd comc = new CommCrd();
CommFTP commFTP = null;
CommRoutine comr = null;

private int iiFileNum = 0;
String fileName = "";
String lastSixMonth = "";
String twDate = "";
double tlAmt1 = 0;
double tlAmt2 = 0;
double tlAmt3 = 0;
double tlAmt4 = 0;
String isProcDate="";
public static void main(String[] args) {
   RskP182 proc = new RskP182();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP182 [proc_date]");
      okExit(0);
   }

   isProcDate =sysDate;
   if (liArg == 1) {
      if (commDate.isDate(args[0])) {
         isProcDate =args[0];
      }
      else {
         printf(" 日期格式輸入錯誤, args[%s]", args[0]);
         okExit(0);
      }
   }
   dbConnect();
   printf("  procDate=[%s], busiDate=[%s]", isProcDate,hBusiDate);
   String lsDD =commString.right(isProcDate,2);
   if (!eq(lsDD,"01")) {
      printf("  每月01日執行, 產生上月資料");
      okExit(0);
   }
   isProcDate =commDate.dateAdd(isProcDate,0,0,-1);

   //--取民國年
   twDate = commDate.toTwDate(isProcDate);
   //--取得前6個月日期
   lastSixMonth = commDate.dateAdd(isProcDate, 0, -6, 0);

   fileName = "EBDER_CNT_"+isProcDate+".TXT";

   checkOpen();

   procData();

   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   //--BackUp File
   renameFile();

   endProgram();
}

//--FTP
void procFTP() throws Exception {
   commFTP.hEflgTransSeqno = ecsModSeq(10); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/rsk", getEcsHome());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   // System.setProperty("user.dir",commFTP.h_eria_local_dir);
   showLogMessage("I", "", "mput "+fileName+" 開始傳送....");
   int errCode = commFTP.ftplogName("NCR2EMP", "mput "+fileName);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 "+fileName+" 資料"+" errcode:"+errCode);
      insertEcsNotifyLog(fileName);
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
   String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
   String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName+"_"+sysDate);

   if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
      showLogMessage("I", "", "ERROR : 檔案["+fileName+"]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 ["+fileName+"] 已移至 ["+tmpstr4+"]");
}

void procData() throws Exception {
   //--個人流通卡之信用額度 , current_code ='0' and acct_type ='01'
   showLogMessage("I", "", "===開始計算個人流通卡之信用額度===");
   tlAmt1 = selectActAcno(0);
   //--個人有效卡之信用額度 , current_code ='0' and acct_type ='01' and 6個月內有消費
   showLogMessage("I", "", "===開始計算個人有效卡之信用額度===");
   tlAmt2 = selectActAcno(1);
   //--商務卡流通卡之信用額度 , current_code ='0' and acct_type in ('03','06')
   showLogMessage("I", "", "===開始計算商務卡流通卡之信用額度===");
   tlAmt3 = selectActAcno(2);
   //--商務卡有效卡之信用額度 , current_code ='0' and acct_type in ('03','06') and 6個月內有消費
   showLogMessage("I", "", "===開始計算商務卡有效卡之信用額度===");
   tlAmt4 = selectActAcno(3);
   //--寫檔
   writeTextFile();

   closeOutputText(iiFileNum);
}

void writeTextFile() throws Exception {
   String newLine = "\r\n", tmp1 = "", tmp2 = "", tmp3 = "", tmp4 = "";
   DecimalFormat df = new DecimalFormat("0");
   tmp1 = df.format(tlAmt1);
   tmp2 = df.format(tlAmt2);
   tmp3 = df.format(tlAmt3);
   tmp4 = df.format(tlAmt4);
   StringBuilder tempBuf = new StringBuilder();
   tempBuf.append(twDate+" 止個人流通卡總授信承諾額度 ");
   tempBuf.append(commString.lpad(tmp1, 13, "0"));
   tempBuf.append(newLine);
   tempBuf.append(twDate+" 止個人有效卡總授信承諾額度 ");
   tempBuf.append(commString.lpad(tmp2, 13, "0"));
   tempBuf.append(newLine);
   tempBuf.append(twDate+" 止法人流通卡總授信承諾額度 ");
   tempBuf.append(commString.lpad(tmp3, 13, "0"));
   tempBuf.append(newLine);
   tempBuf.append(twDate+" 止法人有效卡總授信承諾額度 ");
   tempBuf.append(commString.lpad(tmp4, 13, "0"));
   tempBuf.append(newLine);
   writeTextFile(iiFileNum, tempBuf.toString());
}

double selectActAcno(int i) throws Exception {

   sqlCmd = " select sum(line_of_credit_amt) as tl_amt from act_acno ";

   if (i == 0) {
      //-有有效卡帳戶-
      sqlCmd += " where acno_p_seqno in "
          +" (select acno_p_seqno from crd_card where current_code ='0' and acct_type ='01') ";
   } else if (i == 1) {
      //-有有效卡帳戶,最近6個月有消費---
      //last_consume_date   	//-x(8)  最後消費日期--
      sqlCmd += " where acno_p_seqno in "
          +" (select acno_p_seqno from crd_card "
          +" where current_code ='0' and acct_type ='01' and last_consume_date > ? ) ";
      setString(1, lastSixMonth);
   } else if (i == 2) {
      //-有效商務卡-
      sqlCmd += " where acno_p_seqno in "
          +" (select acno_p_seqno from crd_card "
          +" where current_code ='0' and acct_type in ('03','06') )";
   } else if (i == 3) {
      //-有效商務卡,最近6個月有消費---
      sqlCmd += " where acno_p_seqno in "
          +" (select acno_p_seqno from crd_card "
          +" where current_code ='0' and acct_type in ('03','06') and last_consume_date > ? )";
      setString(1, lastSixMonth);
   }

   selectTable();

   if (sqlNrow > 0)
      return colNum("tl_amt");

   return 0;
}

void checkOpen() throws Exception {
   //fileName = "EBDER_CNT_"+isProcDate+".TXT";
   String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);

   iiFileNum = openOutputText(lsFile, "MS950");
   if (iiFileNum < 0) {
      errmsg("EBDER_CNT_YYYYMMDD.TXT 產檔失敗 !");
      errExit(1);
   }

   return;
}

}
