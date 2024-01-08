/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/04/17  V1.01.01  Lai         program initial                           *
 *  112/09/12  V1.01.02  Lai         modify  ftp
 *  2023-1120  V1.01.03 JH    isRun busi_date
 *  2023-1202  V1.01.04  JH   非營業no run return(0)
 *                                                                             *
 ******************************************************************************/
package Rsk;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*JCIC*/
public class RskP195 extends AccessDAO {
private String PROGNAME = "轉收基金系統資料，再下傳給卡部信評   2023-1202  V1.01.04";
CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
CommCrdRoutine comcr = null;
CommRoutine comr = null;
CommFTP commFTP = null;
int DEBUG = 0;

String prgmId = "RskP195";
String rptId_r1 = "CARDID_YYMMDD.HDR";
String rptName1 = "";
int rptSeq1 = 0;
int page_cnt1 = 0, line_cnt1 = 0;
List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

String buf = "";
String stderr = "";
String h_call_batch_seqno = "";

String h_busi_business_date = "";
int h_rec_count = 0;
String h_check_code = "0";

String h_eflg_system_id = "";
String h_eflg_group_id = "";
String h_eflg_source_from = "";
String h_eflg_trans_seqno = "";
String h_eflg_mod_pgm = "";
String h_eria_local_dir = "";
String tmp = "";
String temstr1 = "";
String temstr2 = "";
String filename_in = "";
String filename_out = "";
String filename = "";
String h_file_namek2 = "";
int err_code = 0;
long tot_cnt = 0;
String tempEflgTransSeqno = "";

buf2 data = new buf2();
private long error_cnt = 0;

public int mainProcess(String[] args) {
   try {
      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram+" "+PROGNAME);

      // 固定要做的
      if (!connectDataBase()) {
         comc.errExit("connect DataBase error", "");
      }
      // =====================================
      if (args.length > 2) {
         comc.errExit("Usage : RskP195 [yyyymmdd]", "");
      }

// if (comm.isAppActive(javaProgram))
//     comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      String ls_runDate = "";
      if (args.length > 0) {
         h_busi_business_date = "";
         if (args[0].length() == 8) {
            h_busi_business_date = args[0];
            ls_runDate = h_busi_business_date;
         } else {
            String ErrMsg = String.format("指定營業日[%s]", args[0]);
         }
      }
      select_ptr_businday();
      if (ls_runDate.length() == 0) {
         ls_runDate = sysDate;
      }
      if (checkWorkDate(ls_runDate)) {
         exceptExit =0;
         comc.errExit(" ["+ls_runDate+"]非營業日, 不執行]", "");
      }

      h_rec_count = 0;

      filename_in = "NFundBenefit.txt";
      temstr1 = String.format("%s/media/rsk/%s", comc.getECSHOME(), filename_in);
      temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);

      filename_out = String.format("ELOAN_FU_%s.TXT", h_busi_business_date);
      String rptId_r1 = filename_out;
      filename = String.format("%s/media/rsk/%s", comc.getECSHOME(), filename_out);
      filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);

      read_open();

      if (h_rec_count > 0) {
         comc.writeReport(filename, lpar1);

         ftp_proc();
         insert_file_ctl(filename_out, "2");
      } else comc.fileDelete(filename_out);

      showLogMessage("I", "", String.format("讀取資料筆數: [%d]", tot_cnt));
      showLogMessage("I", "", String.format("    成功\\筆數: [%d]", h_rec_count));
      // ==============================================

      commFTP = new CommFTP(getDBconnect(), getDBalias());
      comr = new CommRoutine(getDBconnect(), getDBalias());

      renameFile();

      // 固定要做的
      showLogMessage("I", "", "執行結束");
      finalProcess();
      return 0;
   } catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
   }
}

// ************************************************************************
void procFTP() throws Exception {
   //--HDR
   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   // System.setProperty("user.dir",commFTP.h_eria_local_dir);
   showLogMessage("I", "", "mput "+filename_out+" 開始傳送....");
   int errCode = commFTP.ftplogName("CREDITCARD", "mput "+filename_out);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 "+filename_out+" 資料"+" errcode:"+errCode);
      insertEcsNotifyLog(filename_out);
   }

}

//=====================
public int insertEcsNotifyLog(String fileName) throws Exception {
   showLogMessage("I", "", "    INSERT ecs_notify_log=["+tempEflgTransSeqno+"]");

   setValue("crt_date", sysDate);
   setValue("crt_time", sysTime);
//	setValue("unit_code", comr.getObjectOwner("3", javaProgram));
   setValue("unit_code", "RskP");
   setValue("obj_type", "3");
   setValue("notify_head", "無法 FTP 傳送 "+fileName+" 資料");
   setValue("notify_name", "媒體檔名:"+fileName);
   setValue("notify_desc1", "程式 "+javaProgram+" 無法 FTP 傳送 "+fileName+" 資料");
   setValue("notify_desc2", "");
   setValue("trans_seqno", tempEflgTransSeqno);
   setValue("mod_time", sysDate+sysTime);
   setValue("mod_pgm", javaProgram);
   daoTable = "ecs_notify_log";

   insertTable();

   return (0);
}

//=====================
void renameFile() throws Exception {
   String tmpstr1 = String.format("%s/media/rsk/%s", comc.getECSHOME(), filename_out);
   String tmpstr2 = String.format("%s/media/rsk/backup/%s", comc.getECSHOME(), filename_out);

   if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
      showLogMessage("I", "", "ERROR : 檔案["+filename_out+"]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 ["+filename_out+"] 已移至 ["+tmpstr2+"]");

}

// ************************************************************************
public int select_ptr_businday() throws Exception {

   sqlCmd = "select to_char(sysdate,'yyyymmdd') as business_date";
   sqlCmd += "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
      comcr.errRtn("select ptr_businday not found!", "", h_call_batch_seqno);
   }
   if (recordCnt > 0) {
      h_busi_business_date = h_busi_business_date.length() == 0 ? getValue("business_date") : h_busi_business_date;
   }

   showLogMessage("I", "", String.format("營業日=[%s]", h_busi_business_date));

   return 0;
}

/***********************************************************************/
void insert_file_ctl(String filename, String type) throws Exception {
   setValue("file_name", filename_out);
   setValue("crt_date", sysDate);
   setValueInt("head_cnt", 1);
   setValueInt("record_cnt", h_rec_count);
   setValue("check_code", h_check_code);
   setValue("send_nccc_date", sysDate);
   daoTable = "crd_file_ctl";
   insertTable();
   if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_crd_file_ctl duplicate!", "", h_call_batch_seqno);
   }
}

/***********************************************************************/
void read_open() throws Exception {
   String str600 = "";

   int f = openInputText(temstr1, "MS950");
   if (f == -1) {
      comcr.errRtn("檔案不存在："+temstr1, "", h_call_batch_seqno);
   }
   closeInputText(f);
   int br = openInputText(temstr1, "MS950");
   while (true) {
      str600 = readTextFile(br);
      if (endFile[br].equals("Y")) break;
      str600 = str600.trim();
      if (DEBUG == 1) showLogMessage("I", "", "** Read buf=["+str600+"]");
      byte[] bytes = str600.getBytes("MS950");
      for (int int1 = bytes.length-1; int1 >= 0; int1--) {
         if ((bytes[int1] < 20) || (bytes[int1] > 120))
            bytes[int1] = 0x0;
         else break;

      }
      if (bytes.length == 0) continue;

      tot_cnt++;
      if (tot_cnt % 30000 == 0 || tot_cnt == 1)
         showLogMessage("I", "", String.format("crd Process 1 record=[%d]", tot_cnt));

      process(str600);
   }
   closeInputText(br);
}

/***********************************************************************/
void process(String str600) throws Exception {

//第1-byte取10 bytes，來源檔的第57-byte取3 bytes，’00’ (放置於 14th  /15th byte的位置)，來源檔的第11-byte取13 bytes (從第 16th byte開始置放)。其餘長度空白。
   buf = "";
   tmp = String.format("%-10.10s", str600);
   buf = comcr.insertStr(buf, tmp, 1);

   tmp = String.format("%-3.3s", str600.substring(56));
   buf = comcr.insertStr(buf, tmp, 11);
   if (DEBUG == 1) showLogMessage("I", "", "  888 curr=["+tmp+"]");

   buf = comcr.insertStr(buf, "00", 14);

   tmp = String.format("%-13.13s", str600.substring(10));
   buf = comcr.insertStr(buf, tmp, 16);
   if (DEBUG == 1) showLogMessage("I", "", "  888 field 3=["+tmp+"]");

   tmp = String.format("%52.52s", " ");
   buf = comcr.insertStr(buf, tmp, 29);
   lpar1.add(comcr.putReport(rptId_r1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

   h_rec_count++;
}

/***********************************************************************/
void ftp_proc() throws Exception {
   String tojcicmsg = "";

   CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
   CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

   String h_eflg_ref_ip_code = "CREDITCARD";
   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = h_eflg_ref_ip_code;  /* 區分不同類的 FTP 檔案-大類     (必要) */
   commFTP.hEflgGroupId = "ELOAN";    /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "ELOAN";    /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
   commFTP.hEflgModPgm = this.getClass().getName();

   tempEflgTransSeqno = commFTP.hEflgTransSeqno;

   System.setProperty("user.dir", commFTP.hEriaLocalDir);

   String proc_code = String.format("put %s", filename_out);
   showLogMessage("I", "", proc_code+" "+h_eflg_ref_ip_code+" 開始上傳....["+commFTP.hEflgTransSeqno+"]");

   int err_code = commFTP.ftplogName(h_eflg_ref_ip_code, proc_code);
   if (err_code != 0) {
      showLogMessage("I", "", String.format("[%s] => error_code[%d] error\n", h_eflg_ref_ip_code, err_code));
      showLogMessage("I", "", String.format("[%s]檔案傳送ELOAN_FU有誤(error), 請通知相關人員處理\n", proc_code));
      insertEcsNotifyLog(filename_out);
      tojcicmsg = String.format("SENDMSG.sh 1 \"%s執行完成 傳送ELOAN失敗\"", prgmId);
      showLogMessage("I", "", tojcicmsg);
   } else {
      tojcicmsg = String.format("SENDMSG.sh 1 \"%s執行完成 傳送ELOAN無誤\"", prgmId);
      showLogMessage("I", "", tojcicmsg);
   }
}

/***********************************************************************/
public static void main(String[] args) throws Exception {
   RskP195 proc = new RskP195();
   int retCode = proc.mainProcess(args);
   proc.programEnd(retCode);
}

/***********************************************************************/
class buf2 {
   String id;
   String instore;
   String refervalue;
   String unreach;
   String modifydate;
   String investcrny;
   String name;
   String len;
/*
授信戶統編
庫存金額
參考價值
在途金額
更新日期(YYYMMDD)
投資幣別
客戶姓名
*/

   String allText() throws UnsupportedEncodingException {
      String rtn = "";
      rtn += comc.fixLeft(id, 10);
      rtn += comc.fixLeft(instore, 13);
      rtn += comc.fixLeft(refervalue, 13);
      rtn += comc.fixLeft(unreach, 13);
      rtn += comc.fixLeft(modifydate, 7);
      rtn += comc.fixLeft(investcrny, 3);
      rtn += comc.fixLeft(name, 40);
      //        rtn += comc.fixLeft(len        ,  1);
      return rtn;
   }
}

}
