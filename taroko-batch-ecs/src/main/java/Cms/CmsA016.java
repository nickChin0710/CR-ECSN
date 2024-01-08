package Cms;
/**
 * 2023-0504  V1.00.00  JH    initial
 * 2023-0615  V1.00.01  JH    modify
 * 2023-0621  V1.00.02  JH    cms_roadlist.mod_type
 * 2023-0706  V1.00.03  JH    產生月檔 from cms_roadlist
 * 2023-0713  V1.00.04  JH    未登錄卡片
 * 2023-0717  V1.00.05  JH    ftp+backup
 * 2023-0825  V1.00.06  JH    modify
 * 2023-0925  V1.00.07  JH    getRdsPcard()
 * 2023-1214  V1.00.08  JH    fileName_yyyyMMdd
 * */

import com.CommFTP;
import com.CommRoutine;
import com.Parm2sql;

import java.text.Normalizer;

@SuppressWarnings({"unchecked", "deprecation"})
public class CmsA016 extends com.BaseBatch {
private final String PROGNAME = "道路救援產生名單檔-月檔(TMSROAD8)  2023-1214  V1.00.08";
CommFTP commFTP = null;
CommRoutine comr = null;

HH hh = new HH();

//-----------
class HH {
   String rowid = "";
   //   String mod_type="";
   String rds_pcard = "";
   String id_no = "";
   String card_no = "";
   String car_no = "";
   String old_car_no = "";
   String idno_name = "";
   String id_pseqno = "";
   String mod_type = "";
   //-------
   String rd_moddate = "";
   double rd_seqno = 0;
   String rd_status;
   String rd_sendsts = "";
   String rd_sendYn = "";
   //--
   String maj_id_pseqno = "";
   String maj_card_no = "";
   String acno_pseqno = "";
   String group_code = "";
   //--
   String give_flag = "";
   String proj_no = "";
   double purch_amt = 0;
   int purch_cnt = 0;
   double purch_amt_lyy = 0;

   void initData() {
      rowid = "";
//      mod_type="";
      rds_pcard = "";
      id_no = "";
      card_no = "";
      car_no = "";
      old_car_no = "";
      idno_name = "";
      id_pseqno = "";
      mod_type = "";
      //-------
      rd_moddate = "";
      rd_seqno = 0;
      rd_status = "";
      rd_sendsts = "";
      rd_sendYn = "";
      //--
      maj_id_pseqno = "";
      maj_card_no = "";
      acno_pseqno = "";
      group_code = "";
      //--
      give_flag = "";
      proj_no = "";
      purch_amt = 0;
      purch_cnt = 0;
      purch_amt_lyy = 0;
   }
}

//--
String isFileName = "TMSROAD8";
int iiFileNum = -1;

//=*****************************************************************************
public static void main(String[] args) {
   CmsA016 proc = new CmsA016();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 1) {
      printf("Usage : CmsA016 [busi_date(08)]");
      okExit(0);
   }

   if (args.length >= 1) {
      setBusiDate(args[0]);
   }
   //--
   dbConnect();

   //--------
   isFileName = "TMSROAD8_"+hBusiDate;
   fileOpen();
   //---------
   selectCmsRoadList();
   sqlCommit();
   //-FTP+backup--
   procFTP();
//   fileBackup(isFileName,"media/cms","media/cms/backup");
   String lsFile1 = getEcsHome()+"/media/cms"+"/"+isFileName;
   String lsFile2 = getEcsHome()+"/media/cms/backup/"+isFileName;
   com.CommFile commfile = new com.CommFile();
   if (commfile.fileCopy(lsFile1, lsFile2)) {
      commfile.fileDelete(lsFile1);
      printf("file[%s] backup is OK", isFileName);
   }
   else printf("file[%s] backup is ERROR", isFileName);

   endProgram();
}

//======================
void selectCmsRoadList() throws Exception {
   int liOutCnt = 0;

   sqlCmd = " select hex(rowid) as rowid"
             + ", card_no, id_p_seqno"
             + ", rm_carno "
             + ", rm_carmanid ,chi_name rm_carmanname "
             + ", mod_type, rds_pcard, give_flag "
             + " from cms_roadlist"
             + " where 1=1"
             + " and send_date =''"
             + " AND proc_flag ='M' "
//             +" AND proc_flag ='M' and rm_carno=''"
             + " ORDER BY card_no "
   ;

   this.openCursor();
   while (fetchTable()) {
      totalCnt++;
      hh.initData();

      hh.rowid = colSs("rowid");
      hh.card_no = colSs("card_no");
      hh.car_no = colSs("rm_carno").toUpperCase();
      hh.id_no = colSs("rm_carmanid");
      hh.idno_name = colSs("rm_carmanname");
      hh.id_pseqno = colSs("id_p_seqno");
      hh.rds_pcard = colSs("rds_pcard");
      hh.give_flag = colNvl("give_flag", "N");
      hh.mod_type = colSs("mod_type");

      hh.rd_sendYn = "Y";
      hh.rd_sendsts = "N";
      String lsRdsPcard = getRdsPcard();
      String lsTxType = "N";  //新增
      if (eq(hh.mod_type, "D")) lsTxType = "D";

      String lsIdno = commString.left(hh.id_no, 3) + "***" + commString.right(hh.id_no, 4);
      String lsCardNo = commString.right(hh.card_no, 9);
      String lsChiName = getIdnoName();
      liOutCnt++;
      String lsTxt = "2"
                      + commString.rpad(lsTxType, 1)   //交易別:N,D
                      + commString.rpad(lsRdsPcard, 1)   //優惠別
                      + commString.bbFixlen(lsIdno, 10)   //持卡人ID
                      + commString.bbFixlen(lsCardNo, 9)   //卡號
                      + commString.bbFixlen(hh.car_no, 8)   //車號
                      + commString.bbFixlen(sysDate, 8)   //批次系統日
                      + commString.bbFixlen(lsChiName, 12)   //持卡人姓名
                      + newLine;
      writeTextFile(iiFileNum, lsTxt);

      updateCmsRoadList();
   }
   closeCursor();

   //-Footer------
   String lsTxt = "3" +
                   String.format("%09d", liOutCnt) +
                   commString.bbFixlen(sysDate, 8) +
                   commString.space(32) +
                   newLine;
   writeTextFile(iiFileNum, lsTxt);

   closeOutputText(iiFileNum);
   printf(" 產生筆數[%s]", liOutCnt);
}

//==========================================================
int tiCtype = -1;

String getRdsPcard() throws Exception {
   String lsCarNo=hh.car_no.replaceAll("-","");
   //-L.租賃車-
   if (lsCarNo.length() == 6) {
      byte[] cc = lsCarNo.getBytes();
      if (cc[0] == cc[1] || cc[4] == cc[5]) {
         return "L";
      }
   }
   if (hh.car_no.length() == 7) {
      byte[] cc = hh.car_no.getBytes();
      if (cc[0] == 'R') {
         return "L";
      }
   }

   if (!empty(hh.rds_pcard))
      return hh.rds_pcard;

   if (tiCtype <= 0) {
      sqlCmd = "select A.rds_pcard" +
                " from ptr_card_type A join crd_card B on A.card_type=B.card_type" +
                " where B.card_no =?" +
                commSqlStr.rownum(1);
      tiCtype = ppStmtCrt("ti-ctype-S", "");
   }

   ppp(1, hh.card_no);
   sqlSelect(tiCtype);
   if (sqlNrow <= 0) {
      return "";
   }
   return colSs("rds_pcard");
}

//-------------
int tiIdno = -1;

String getIdnoName() throws Exception {
   String lsName = hh.idno_name.replaceAll("　", "");
   if (!empty(lsName)) {
      return lsName;
   }

   if (tiIdno <= 0) {
      sqlCmd = "select chi_name" +
                " from crd_idno" +
                " where id_p_seqno =?";
      tiIdno = ppStmtCrt("ti-idno-S", "");
   }

   ppp(1, hh.id_pseqno);
   sqlSelect(tiIdno);
   if (sqlNrow > 0) {
      lsName = colSs("chi_name").replaceAll("　", "");
   }
   return lsName;
}

//====================================================
Parm2sql ttListU = null;

void updateCmsRoadList() throws Exception {
   if (ttListU == null) {
      ttListU = new Parm2sql();
      ttListU.update("cms_roadlist");
   }

   ttListU.aaa("send_date", sysDate);
   ttListU.aaa("proc_flag", "Y");
   ttListU.aaa("proc_date", sysDate);
   ttListU.aaaDtime("mod_time");
   ttListU.aaa("mod_pgm", hModPgm);
   ttListU.aaaWhere("where rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)", hh.rowid);

   if (ttListU.ti <= 0) {
      ttListU.ti = ppStmtCrt("tt-detl-U", ttListU.getSql());
   }

   sqlExec(ttListU.ti, ttListU.getParms());
   if (sqlNrow <= 0) {
      sqlerr("update cms_roadlist error");
      errExit(1);
   }
}

//===================================
void fileOpen() throws Exception {
   //-isFileName = "TMSROAD8_"+sysDate;-
   String lsPath = getEcsHome() + "/media/cms/" + isFileName;
   printf("open out-file [%s]", lsPath);
   iiFileNum = openOutputText(lsPath);
   if (iiFileNum < 0) {
      errmsg("在程式執行目錄下沒有權限讀寫資料, file[%s]", lsPath);
      okExit(0);
   }
}

//================
private void procFTP() throws Exception {
   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());

   commFTP.hEflgTransSeqno = ecsModSeq(10);
   //comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = getEcsHome() + "/media/cms";//fileName;
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
   int errCode = commFTP.ftplogName("NCR2TCB", "mput " + isFileName);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
      commFTP.insertEcsNotifyLog(isFileName, "3", javaProgram, sysDate, sysTime);
   }
}

}