/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/12/20  V1.00.00    castor                       program initial
 *  2023-1206 V1.00.01     JH    fileName:date-1days
 ******************************************************************************/

package Inf;

import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;
import com.BaseBatch;

public class InfC043 extends BaseBatch {
private final String progname = "產生送CRDB 43異動 CRRGAM 保證人註記資料檔程式  2023-1206 V1.00.01";
CommCrd comc = new CommCrd();
CommFTP commFTP = null;
CommRoutine comr = null;
String isFileName = "";
private int ilFile43;
String is_procDate = "";

String hCardNo = "";
String hIdNo = "";
String hIdPSeqno = "";
String hModAudcode = "";
String hRelaType = "";


public static void main(String[] args) {
   InfC043 proc = new InfC043();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : InfCrb43 [business_date]");
      okExit(0);
   }
   dbConnect();
   if (liArg == 1) {
      if (commDate.isDate(args[0])) {
         is_procDate = args[0];
      }
   }
   if (empty(is_procDate)) {
      is_procDate =commDate.dateAdd(sysDate,0,0,-1);
   }
   isFileName = "CRU23B1_TYPE_43_"+is_procDate+".txt";
   printf("process: busi_date[%s], proc_date[%s], fileName[%s]"
       ,hBusiDate,is_procDate,isFileName);

   checkOpen();
   selectIdNo();
   closeOutputText(ilFile43);
   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   renameFile();

   //--
   sqlCommit();
   endProgram();
}


void selectIdNo() throws Exception {
//   sqlCmd = "SELECT b.card_no card_no, uf_idno_id(b.major_id_p_seqno) id_no,a.ID_P_SEQNO id_p_seqno,a.MOD_AUDCODE mod_audcode,a.K_P_SEQNO k_p_seqno,max(a.MOD_TIME) MOD_TIME  ";
//   sqlCmd += "FROM LOG_CRD_RELA a ,crd_card b ";
//   sqlCmd += "WHERE a.MOD_AUDCODE IN ('A','D') ";
//   sqlCmd += "  AND a.K_RELA_TYPE ='1' ";
//   sqlCmd += "  AND SUBSTR(a.MOD_TIME,1,8) = ? ";
//   sqlCmd += "AND a.K_P_SEQNO = b.ACNO_P_SEQNO ";
//   sqlCmd += "GROUP BY b.CARD_NO,uf_idno_id(b.major_id_p_seqno),a.ID_P_SEQNO,a.MOD_AUDCODE,a.K_P_SEQNO ";
   sqlCmd ="SELECT b.card_no card_no "+
       ", (SELECT id_no FROM crd_idno WHERE id_p_seqno=B.major_id_p_seqno) id_no "+
       ", a.ID_P_SEQNO id_p_seqno "+
       ", a.MOD_AUDCODE mod_audcode "+
       ", a.K_P_SEQNO k_p_seqno "+
       ", max(a.MOD_TIME) MOD_TIME "+
       " FROM LOG_CRD_RELA a, crd_card b "+
       " WHERE a.MOD_AUDCODE IN ('A','D') "+
       "  AND a.K_RELA_TYPE ='1' "+
       "  AND a.K_P_SEQNO = b.ACNO_P_SEQNO "+
       "  AND SUBSTR(a.MOD_TIME,1,8) = ? "+
       " GROUP BY b.CARD_NO,b.major_id_p_seqno,a.ID_P_SEQNO,a.MOD_AUDCODE,a.K_P_SEQNO "
       ;

   setString(1, is_procDate);
   openCursor();
   while (fetchTable()) {
      totalCnt++;
      dspProcRow(500);

      hCardNo = colSs("card_no");
      hIdNo = colSs("id_no");
      hIdPSeqno = colSs("ID_P_SEQNO");
      hModAudcode = colSs("MOD_AUDCODE");

      if (eq(hModAudcode,"A")) {
         hRelaType = "Y";
      } else {
         if (checkRelaType(hIdPSeqno) > 0) {
            hRelaType = "Y";
         } else {
            hRelaType = "N";
         }
      }

      writeTextFile();
   }
   closeCursor();
}


void checkOpen() throws Exception {
   String lsTemp = "";
   lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
   ilFile43 = openOutputText(lsTemp, "big5");
   if (ilFile43 < 0) {
      printf("CRU23B1-TYPE-43 產檔失敗 ! ");
      okExit(0);
   }
}

void writeTextFile() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   String tempStr = "", newLine = "\r\n";
   tempBuf.append("43"); // --代碼 固定 43
   tempBuf.append(comc.fixLeft(hCardNo, 16)); //--卡號 16 碼
   tempBuf.append(comc.fixLeft(hIdNo, 11)); // --主卡ID 11 碼
   tempBuf.append(comc.fixLeft(hRelaType, 1)); //--保證人註記
   tempBuf.append(comc.fixLeft("", 120));
   tempBuf.append(newLine);
   totalCnt++;
   this.writeTextFile(ilFile43, tempBuf.toString());
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

int checkRelaType(String major_id_p_seqno) throws Exception {

   sqlCmd = "SELECT count(*) cnt ";
   sqlCmd += "  from crd_rela  ";
   sqlCmd += " where rela_type ='1' ";
   sqlCmd += "   and id_p_seqno = ? ";

   setString(1, major_id_p_seqno);
   selectTable();

   return getValueInt("cnt");
}


}
