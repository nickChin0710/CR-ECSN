/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  109-07-03  V1.00.01   Alex       initial                                  *
 *  2023-0905 V1.00.02  JH       modify
 *  2023-1002 V1.00.03  JH    not-find insert
 *  2023-1116 V1.00.04  JH    commit=5000
 *****************************************************************************/
package Rsk;

import com.BaseBatch;
import com.CommCrd;

public class RskP170 extends BaseBatch {
private final String progname = "收舊戶信評等級檔案 2023-1116 V1.00.04";
CommCrd comc = new CommCrd();

final String fileName = "PCIC_OLD.TXT";
private int iiFileNum = 0;

//--檔案欄位
String idNo = "";
String pdRatingOld = "";
String pdRatingDate = "";

//--程式處理欄位
String idPSeqno = "";
String dbPdRatingNew = "";
String dbPdRatingOld = "";
String branch = "";
String status = "";
boolean lbContinue = false;

public static void main(String[] args) {
   RskP170 proc = new RskP170();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP170 [business_date]");
      okExit(0);
   }

   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }
   dbConnect();

   //--吃檔
   checkOpen();
   //--逐筆處理
   processData();
   //--移至備份檔案
   renameFile();

   endProgram();
}

void checkOpen() throws Exception {
   //String fileName = "PCIC_OLD.TXT";
   String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);

   iiFileNum = openInputText(lsFile, "UTF-8");
   if (iiFileNum < 0) {
      printf("PCIC_OLD.txt 吃檔失敗 !, file[%s]", fileName);
      okExit(0);
   }

   return;
}

void processData() throws Exception {
   while (true) {
      String fileData = readTextFile(iiFileNum);
      if (endFile[iiFileNum].equals("Y")) {
         break;
      }

      if (empty(fileData))
         break;

      totalCnt++;
      if ((totalCnt % 5000)==0) {
         printf(" Process Cnt=[%s]", totalCnt);
         sqlCommit();
      }
      initData();
      fileData = removeBom(fileData);
      splitData(fileData);

      int liRC=0;
      //--先取卡人流水號
      //selectCrdIdnoSeqno();
      selectIdnOPcic();
      //--查無卡人資訊無法繼續跳過此筆
      if (empty(idPSeqno)) {
//         printf(" --crd_idno N-file [%s]", idNo);
         continue;
      }

      //--記錄目前資料庫內的 rsk_pcic
      //selectRskPcic();
      //--判斷信評等級上升或下降
      comparePdRating();
      //--更新rsk_pcic
      liRC =updateRskPcic();
      if (liRC !=0) {
//         sqlRollback();
         continue;
      }
      //--更新信用卡卡人檔
      updateCrdIdNo();
      //--更新VD卡人檔
      updateDbcIdNo();
      //--判斷記錄是否存在決定寫入異動記錄檔還是更新 , true:更新 , false:新增
      liRC =insertRskPcicLog();
//      if (checkRskPcicLog()) {
//         liRC =updateRskPcicLog();
//      } else {
//         liRC =insertRskPcicLog();
//      }
      if (liRC !=0) {
         //sqlRollback();
         continue;
      }

      //sqlCommit(1);
   }
   sqlCommit();
   closeInputText(iiFileNum);
}
//------------------
int tiPcic=-1;
void selectIdnOPcic() throws Exception {
   if (tiPcic <=0) {
      sqlCmd = "select A.id_p_seqno, B.pd_rating_new, B.pd_rating_old "
          +" from crd_idno A left join rsk_pcic B "
          +"  on B.id_p_seqno=A.id_p_seqno "
          +" where A.id_no = ? ";
      tiPcic =ppStmtCrt("tiPcic","");
   }
   ppp(1, idNo);
   sqlSelect(tiPcic);

   if (sqlNrow > 0) {
      idPSeqno =colSs("id_p_seqno");
      dbPdRatingNew = colSs("pd_rating_new");
      dbPdRatingOld = colSs("pd_rating_old");
   }

}
//-----------------------
void selectCrdIdnoSeqno() throws Exception {

   sqlCmd = "select id_p_seqno from crd_idno_seqno where id_no = ? ";
   setString(1, idNo);

   sqlSelect();

   if (sqlNrow > 0) {
      idPSeqno = colSs("id_p_seqno");
   }

}
//----------------------
void selectRskPcic() throws Exception {

   sqlCmd = "select pd_rating_new , pd_rating_old from rsk_pcic where id_p_seqno = ? ";
   setString(1, idPSeqno);

   sqlSelect();

   if (sqlNrow > 0) {
      dbPdRatingNew = colSs("pd_rating_new");
      dbPdRatingOld = colSs("pd_rating_old");
   }
}

void comparePdRating() {
   //-- U:升高 S:不變 D:降低
   if (pdRatingOld.compareTo(dbPdRatingOld) < 0) {
      status = "D";
   } else if (pdRatingOld.compareTo(dbPdRatingOld) == 0) {
      status = "S";
   } else if (pdRatingOld.compareTo(dbPdRatingOld) > 0) {
      status = "U";
   }
}

int updateRskPcic() throws Exception {
   int updateCnt = 0;
   daoTable = "rsk_pcic";
   updateSQL = "pd_rating_old =? , ";
   updateSQL += "crt_date_old =? , ";
   updateSQL += "mod_user =? ,";
   updateSQL += "mod_time =sysdate ,";
   updateSQL += "mod_pgm =? ";
   whereStr = "where id_p_seqno =? ";

   setString(1, pdRatingOld);
   setString(2, pdRatingDate);
   setString(3, hModUser);
   setString(4, hModPgm);
   setString(5, idPSeqno);

   updateCnt = updateTable();
   if (updateCnt == 0) {
      return insertRskPcoc();
   }
   return 0;
}
//-------
com.Parm2sql ttApcic=null;
int insertRskPcoc() throws Exception {
   if (ttApcic ==null) {
      ttApcic =new com.Parm2sql();
      ttApcic.insert("rsk_pcic");
   }
   ttApcic.aaa("id_p_seqno"                , idPSeqno);    //-卡人流水號碼--
   //ttApcic.aaa("pd_rating_new"             , hh.pd_rating_new);    //-新戶信評等級--
   ttApcic.aaa("pd_rating_old"             , pdRatingOld);    //-舊戶信評等級--
//   ttApcic.aaa("crt_date_new"              , hh.crt_date_new);    //-新戶建檔日期--
   ttApcic.aaa("crt_date_old"              , pdRatingDate);    //-舊戶覆審日期--
//   ttApcic.aaa("otb"                       , hh.otb);    //-新戶信用額度--
//   ttApcic.aaa("rev_rate"                  , hh.rev_rate);    //-新戶循環利率--
//   ttApcic.aaa("memo"                      , hh.memo);    //-保留欄位--
   ttApcic.aaa("mod_user"                  , hModUser);    //-異動使用者--
   ttApcic.aaaDtime("mod_time");    //-異動時間--
   ttApcic.aaa("mod_pgm"                   , hModPgm);    //-異動程式--

   if (ttApcic.ti <=0) {
      ttApcic.ti =ppStmtCrt("ttApcic", ttApcic.getSql());
   }

   sqlExec(ttApcic.ti, ttApcic.getParms());
   if (sqlNrow <=0) {
      printf(" insert rsk_pcic error, kk[%s]", idNo);
      //lbContinue =true;
      return 1;
   }

   return 0;
}

boolean checkRskPcicLog() throws Exception {

   sqlCmd = "select count(*) as log_cnt from rsk_pcic_log where log_date = ? and id_p_seqno = ? ";
   setString(1, hBusiDate);
   setString(2, idPSeqno);

   sqlSelect();

   if (colNum("log_cnt") > 0)
      return true;

   return false;
}

int insertRskPcicLog() throws Exception {
   daoTable = "rsk_pcic_log";
   setValue("log_date", hBusiDate);
   setValue("id_p_seqno", idPSeqno);
   setValue("last_pd_rating_new", dbPdRatingNew);
   setValue("pd_rating_new", dbPdRatingNew);
   setValue("last_pd_rating_old", dbPdRatingOld);
   setValue("pd_rating_old", pdRatingOld);
   setValue("status", status);
   setValue("crt_date", hBusiDate);
   setValue("crt_time", sysTime);
   setValue("mod_user", hModUser);
   setValue("mod_time", sysDate+sysTime);
   setValue("mod_pgm", hModPgm);
   setValueInt("mod_seqno", 1);
   int llRow=insertTable();
   if (eq(dupRecord,"Y") || llRow <=0) {
      return updateRskPcicLog();
   }
   return 0;
}

int updateRskPcicLog() throws Exception {
   daoTable = "rsk_pcic_log";
   updateSQL = "last_pd_rating_new =? , ";
   updateSQL += "pd_rating_new =? , ";
   updateSQL += "last_pd_rating_old =? , ";
   updateSQL += "pd_rating_old =? , ";
   updateSQL += "status =? , ";
   updateSQL += "mod_user =? ,";
   updateSQL += "mod_time =sysdate ,";
   updateSQL += "mod_pgm =? ";
   whereStr = "where log_date = ? and  id_p_seqno =? ";

   setString(1, dbPdRatingNew);
   setString(2, dbPdRatingNew);
   setString(3, dbPdRatingOld);
   setString(4, pdRatingOld);
   setString(5, status);
   setString(6, hModUser);
   setString(7, hModPgm);
   setString(8, hBusiDate);
   setString(9, idPSeqno);

   int nn=updateTable();
   if (nn == 0) {
      printf("update_rsk_pcic_log error, kk=[%s]", idNo);
      //lbContinue = true;
      return 1;
   }
   return 0;
}

void updateCrdIdNo() throws Exception {
   daoTable = "crd_idno";
   updateSQL = "credit_level_old = ? , ";
   updateSQL += "mod_time = sysdate , ";
   updateSQL += "mod_user = ? , ";
   updateSQL += "mod_pgm = ? , ";
   updateSQL += "mod_seqno = nvl(mod_seqno,0)+1 ";
   whereStr = " where id_no = ? ";

   setString(1, pdRatingOld);
   setString(2, hModUser);
   setString(3, hModPgm);
   setString(4, idNo);

   updateTable();
}

void updateDbcIdNo() throws Exception {
   int updateCnt = 0;
   daoTable = "dbc_idno";
   updateSQL = "credit_level_old = ? , ";
   updateSQL += "mod_time = sysdate , ";
   updateSQL += "mod_user = ? , ";
   updateSQL += "mod_pgm = ? , ";
   updateSQL += "mod_seqno = nvl(mod_seqno,0)+1 ";
   whereStr = " where id_no = ? ";

   setString(1, pdRatingOld);
   setString(2, hModUser);
   setString(3, hModPgm);
   setString(4, idNo);

   updateCnt = updateTable();
}

String removeBom(String oriData) {
   String proData = "", bomString = "\uFEFF";
   if (oriData.startsWith(bomString)) {
      proData = oriData.replace(bomString, "");
   } else {
      proData = oriData;
   }

   return proData;
}

void initData() {
   idNo = "";
   pdRatingOld = "";
   pdRatingDate = "";
   idPSeqno = "";
   dbPdRatingNew = "";
   dbPdRatingOld = "";
   branch = "";
   status = "";
   lbContinue = false;
}

void splitData(String fileData) throws Exception {
   byte[] bytes = fileData.getBytes("MS950");
   idNo = comc.subMS950String(bytes, 0, 10).trim();
   pdRatingOld = comc.subMS950String(bytes, 10, 2).trim();
   if (pdRatingOld.length() < 2)
      pdRatingOld = "0"+pdRatingOld;
   pdRatingDate = comc.subMS950String(bytes, 12, 8);
}

void renameFile() throws Exception {
   String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
   String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName+"_"+hBusiDate);

   if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
      showLogMessage("I", "", "ERROR : 檔案["+fileName+"]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 ["+fileName+"] 已移至 ["+tmpstr2+"]");
}

}
