/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/10/21  V1.00.12  Allen Ho   New                                        *
* 111-10-11  V1.00.13  jiangyigndong  updated for project coding standard    *
* 111/10/14  V1.00.14  jiangyigndong  updated for project coding standard    *
* 111/10/18  V1.00.15  jiangyigndong  updated for project coding standard    *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktC420 extends AccessDAO {
   private final String PROGNAME = "首刷禮-登錄資格判斷處理處理程式 111/10/18  V1.00.15";
   CommFunction comm = new CommFunction();
   CommRoutine comr = null;
   CommBonus comb = null;
   CommDBonus comd = null;

   String businessDate = "";
   String activeCode = "";

   String[][] recordGroupNo = new String[100][100];
   String[][] activeSeq = new String[100][100];
   String[][] levelSeq = new String[100][100];

   String minDate = "", maxDate = "";
   int parmCnt = 0, recordFlag = 0;;
   int[] pseqCnt = new int[100];
   int totalCnt = 0, insertCnt = 0;

   // ************************************************************************
   public static void main(String[] args) throws Exception {
      MktC420 proc = new MktC420();
      int retCode = proc.mainProcess(args);
      System.exit(retCode);
   }

   // ************************************************************************
   public int mainProcess(String[] args) {
      try {
         dateTime();
         setConsoleMode("N");
         javaProgram = this.getClass().getName();
         showLogMessage("I", "", javaProgram + " " + PROGNAME);

         if (comm.isAppActive(javaProgram)) {
            showLogMessage("I", "", "本程式已有另依程序啟動中, 不執行..");
            return (0);
         }

         if (args.length > 2) {
            showLogMessage("I", "", "請輸入參數:");
            showLogMessage("I", "", "PARM 1 : [business_date]");
            showLogMessage("I", "", "PARM 2 : [active_code]");
            return (1);
         }

         if (args.length >= 1) {
            businessDate = args[0];
         }

         activeCode = "";
         if (args.length == 2) {
            activeCode = args[1];
         }

         if (!connectDataBase()) {
            return (1);
         }

         comr = new CommRoutine(getDBconnect(), getDBalias());
         comb = new CommBonus(getDBconnect(), getDBalias());
         comd = new CommDBonus(getDBconnect(), getDBalias());

         selectPtrBusinday();

         showLogMessage("I", "", "=========================================");
         showLogMessage("I", "", "載入參數資料");
         selectMktFstpParm();
         if (recordFlag == 0) {
            showLogMessage("I", "", "無登錄資料需判斷");
            showLogMessage("I", "", "=========================================");
            return (0);
         }
         showLogMessage("I", "", "=========================================");
         showLogMessage("I", "", "載入暫存資料");
         loadMktFstpCarddtl();
         loadMktBnData();
         loadMktFstpRecord();
         showLogMessage("I", "", "   Record date must >= [" + minDate + "] and [" + comm.nextNDate(businessDate, -3) + "]");
         showLogMessage("I", "", "=========================================");
         showLogMessage("I", "", "載入網路登錄(web_active)資料");
         // selectWebActive();  未定案暫Remark
         showLogMessage("I", "", "=========================================");
         showLogMessage("I", "", "載入語音(mkt_voice)登錄資料");
         selectMktVoice();
         ;
         showLogMessage("I", "", "=========================================");
         showLogMessage("I", "", "載入語音(mkt_vocdata)登錄資料");
         selectMktVocdata();
         ;
         showLogMessage("I", "", "=========================================");

         finalProcess();
         return (0);
      }

      catch (Exception ex) {
         expMethod = "mainProcess";
         expHandle(ex);
         return exceptExit;
      }

   } // End of mainProcess
     // ************************************************************************

   void selectPtrBusinday() throws Exception {
      daoTable = "PTR_BUSINDAY";
      whereStr = "FETCH FIRST 1 ROW ONLY";

      int recordCnt = selectTable();

      if (notFound.equals("Y")) {
         showLogMessage("I", "", "select ptr_businday error!");
         exitProgram(1);
      }

      if (businessDate.length() == 0) {
         businessDate = getValue("BUSINESS_DATE");
      }
      showLogMessage("I", "", "本日營業日 : [" + businessDate + "]");
   }

   // ************************************************************************
   int selectMktFstpParm() throws Exception {
      extendField = "parm.";
      daoTable = "mkt_fstp_parm";
      whereStr = "WHERE apr_flag        = 'Y' "
            + "AND   apr_date       != ''  "
            + "AND   (stop_flag     != 'Y'  "
            + " or    (stop_flag     = 'Y'  "
            + "  and  stop_date      > ? )) "
            + "and   issue_date_s <= ? "
            + "and   to_char(to_date(issue_date_e,'yyyymmdd')+"
            + "             (purchase_days+1) days,'yyyymmdd') >= ?  ";

      if (activeCode.length() > 0) {
         whereStr = whereStr
               + "and active_code = ? ";
      }

      setString(1, businessDate);
      setString(2, businessDate);
      setString(3, businessDate);

      if (activeCode.length() > 0) {
         setString(4, activeCode);
      }

      parmCnt = selectTable();

      minDate = "99999999";
      maxDate = "00000000";
      String tmpMaxDate = "";
      for (int inti = 0; inti < parmCnt; inti++) {
         showLogMessage("I", "",
               "活動代號:[" + getValue("parm.active_code", inti) + "]-" + getValue("parm.active_name", inti));
         setValue("parm.record_flag", "N", inti);
         if (getValue("parm.issue_date_e", inti).length() == 0) {
            setValue("parm.issue_date_e", "30001231", inti);
         }

         if (getValue("parm.issue_date_s", inti).compareTo(minDate) < 0) {
            minDate = getValue("parm.issue_date_s", inti);
         }

         tmpMaxDate = comm.nextNDate(getValue("parm.issue_date_e", inti),
               (getValueInt("parm.purchase_days", inti) + 1));

         if (tmpMaxDate.compareTo(maxDate) > 0) {
            maxDate = tmpMaxDate;
         }

         if (getValue("parm.multi_fb_type", inti).equals("1")) {
            activeSeq[inti][0] = "";
            levelSeq[inti][0] = "";
            recordGroupNo[inti][0] = "";

            setValue("parm.record_flag", "N", inti);
            if (getValue("parm.record_cond", inti).equals("Y")) {
               recordGroupNo[inti][0] = getValue("parm.record_group_no", inti);
               setValue("parm.record_flag", "Y", inti);
               recordFlag = 1;
            }
            pseqCnt[inti] = 1;
         } else
            selectMktFstpParmseq(inti);
      }
      showLogMessage("I", "", "參數檔載入筆數: [" + parmCnt + "]");
      return (0);
   }

   // ************************************************************************
   int selectMktFstpParmseq(int inti) throws Exception {
      extendField = "pseq.";
      daoTable = "mkt_fstp_parmseq";
      whereStr = "WHERE active_code     = ? "
            + "order by level_seq desc,active_seq asc";

      setString(1, getValue("parm.active_code", inti));

      int recCnt = selectTable();

      pseqCnt[inti] = recCnt;

      setValue("parm.record_flag", "N", inti);
      for (int intk = 0; intk < recCnt; intk++) {
         showLogMessage("I", "", "  活動序號:[" + getValue("pseq.active_seq", intk) + "]");
         activeSeq[inti][intk] = getValue("pseq.active_seq", intk);
         levelSeq[inti][intk] = getValue("pseq.level_seq", intk);
         recordGroupNo[inti][intk] = "";
         if (getValue("pseq.record_cond", intk).equals("Y")) {
            setValue("parm.record_flag", "Y", inti);
            recordGroupNo[inti][intk] = getValue("pseq.record_group_no", intk);
            recordFlag = 1;
         }
      }

      return (0);
   }

   // ************************************************************************
   void selectWebActive() throws Exception {
      selectSQL = "active_no as record_no,"
            + "rec_date as record_date,"
            + "rec_time as record_time,"
            + "major_id_p_seqno,"
            + "vd_flag,"
            + "id_no";
      daoTable = "web_active";
      whereStr = "where rec_date between ? and ? "
            + "and   active_no != ''  "
            + "and   id_no     != ''  ";

      setString(1, minDate);
      setString(2, businessDate);

      openCursor();

      totalCnt = 0;
      int outCnt = 0;
      insertCnt = 0;
      setValue("data_from", "1");
      while (fetchTable()) {
         totalCnt++;
         /*
          * showLogMessage("I","","STEP 1 record_date :["+getValue("record_date")+"]");
          * showLogMessage("I","","       id_no       :["+getValue("id_no")+"]");
          * showLogMessage("I","","       id_p_seqno  :["+getValue("major_id_p_seqno")+
          * "]");
          */

         setValue("record_time", "240000");

         if (getValue("major_id_p_seqno").length() != 0) {
            if (!getValue("vd_flag").equals("Y")) {
               if (selectCrdIdno() == 0) {
                  setValue("id_no", getValue("idno.id_no"));
               }
            } else {
               continue;
            }
         }
         setValue("mfcl.id_no", getValue("id_no"));
         int cnt1 = getLoadData("mfcl.id_no");
         if (cnt1 == 0) {
            outCnt++;
            continue;
         }

         for (int inti = 0; inti < parmCnt; inti++) {
            if (getValue("record_date").compareTo(getValue("parm.issue_date_s", inti)) < 0)
               continue;
            if (getValue("record_date").compareTo(
                  comm.nextNDate(getValue("parm.issue_date_e", inti),
                        (getValueInt("parm.purchase_days", inti) + 1))) > 0) {
               continue;
            }

            for (int intk = 0; intk < pseqCnt[inti]; intk++) {
               if (recordGroupNo[inti][intk].length() == 0) {
                  continue;
               }

               setValue("mbda.data_key", recordGroupNo[inti][intk]);
               setValue("mbda.data_type", "2");
               setValue("mbda.data_code", getValue("record_no"));
               int cnt2 = getLoadData("mbda.data_key,mbda.data_type,mbda.data_code");
               if (cnt2 == 0) {
                  continue;
               }

               insertMktFstpRecord(inti, intk);

               if ((getValue("parm.c_record_cond", inti).equals("Y")) &&
                     (recordGroupNo[inti][intk].equals(getValue("parm.c_record_group_no", inti)))) {
                  updateMktFstpCarddtlC(inti);
               } else {
                  setValue("reco.id_p_seqno", getValue("mfcl.id_p_seqno"));
                  setValue("reco.active_code", getValue("parm.active_code", inti));
                  int cnt3 = getLoadData("reco.id_p_seqno,reco.active_code");

                  if (cnt3 == 0) {
                     updateMktFstpCarddtl(inti, intk);
                  } else {
                     for (int intm = 0; intm < cnt3; intm++) {
                        if ((getValue("record_date") + getValue("record_time")).compareTo(
                              (getValue("reco.record_date", intm) + getValue("reco.record_time", intm))) < 0) {
                           continue;
                        }

                        if ((getValue("record_date") + getValue("record_time") + getValue("record_no")).compareTo(
                              (getValue("reco.record_date", intm) +
                                    getValue("reco.record_time", intm) +
                                    getValue("reco.record_no", intm))) == 0) {
                           continue;
                        }

                        updateMktFstpCarddtl(inti, intk);
                     }
                  }
               }
            }
         }

         processDisplay(50000); // every 10000 display message
      }
      closeCursor();
      showLogMessage("I", "", "不符合之ID筆數 :[" + outCnt + "] 新增筆數[" + insertCnt + "]");
   }

   // ************************************************************************
   void selectMktVocdata() throws Exception {
      selectSQL = "docu_code as record_no,"
            + "crt_date as record_date,"
            + "crt_time as record_time,"
            + "p_seqno";
      daoTable = "mkt_vocdata";
      whereStr = "where crt_date between ? and ? "
            + "and   docu_code!='' "
            + "and   p_seqno  !='' ";

      setString(1, minDate);
      setString(2, businessDate);

      openCursor();

      totalCnt = 0;
      int outCnt = 0;
      insertCnt = 0;
      setValue("data_from", "2");
      while (fetchTable()) {
         totalCnt++;

         setValue("mfcl.p_seqno", getValue("p_seqno"));
         int cnt1 = getLoadData("mfcl.p_seqno");
         if (cnt1 == 0) {
            outCnt++;
            continue;
         }

         for (int inti = 0; inti < parmCnt; inti++) {
            if (getValue("record_date").compareTo(getValue("parm.issue_date_s", inti)) < 0) {
               continue;
            }
            if (getValue("record_date").compareTo(
                  comm.nextNDate(getValue("parm.issue_date_e", inti),
                        (getValueInt("parm.purchase_days", inti) + 1))) > 0) {
               continue;
            }

            for (int intk = 0; intk < pseqCnt[inti]; intk++) {
               if (recordGroupNo[inti][intk].length() == 0) {
                  continue;
               }

               setValue("mbda.data_key", recordGroupNo[inti][intk]);
               setValue("mbda.data_type", "1");
               setValue("mbda.data_code", getValue("record_no"));
               int cnt2 = getLoadData("mbda.data_key,mbda.data_type,mbda.data_code");
               if (cnt2 == 0) {
                  continue;
               }

               if (getValue("record_date").compareTo(getValue("mbda.data_code2")) < 0) {
                  continue;
               }
               if (getValue("record_date").compareTo(getValue("mbda.data_code3")) > 0) {
                  continue;
               }

               insertMktFstpRecord(inti, intk);

               if ((getValue("parm.c_record_cond", inti).equals("Y")) &&
                     (recordGroupNo[inti][intk].equals(getValue("parm.c_record_group_no", inti)))) {
                  updateMktFstpCarddtlC(inti);
               } else {
                  setValue("reco.id_p_seqno", getValue("mfcl.id_p_seqno"));
                  setValue("reco.active_code", getValue("parm.active_code", inti));
                  int cnt3 = getLoadData("reco.id_p_seqno,reco.active_code");

                  if (cnt3 == 0) {
                     updateMktFstpCarddtl(inti, intk);
                  } else {
                     for (int intm = 0; intm < cnt3; intm++) {
                        if ((getValue("record_date") + getValue("record_time")).compareTo(
                              (getValue("reco.record_date", intm) + getValue("reco.record_time", intm))) < 0) {
                           continue;
                        }

                        if ((getValue("record_date") + getValue("record_time") + getValue("record_no")).compareTo(
                              (getValue("reco.record_date", intm) +
                                    getValue("reco.record_time", intm) +
                                    getValue("reco.record_no", intm))) == 0) {
                           continue;
                        }

                        updateMktFstpCarddtl(inti, intk);
                     }
                  }
               }
            }
         }

         processDisplay(50000); // every 10000 display message
      }
      closeCursor();
      showLogMessage("I", "", "不符合之ID筆數 :[" + outCnt + "] 新增筆數[" + insertCnt + "]");
   }

   // ************************************************************************
   void selectMktVoice() throws Exception {
      selectSQL = "document as record_no,"
            + "input_date as record_date,"
            + "input_time as record_time,"
            + "p_seqno";
      daoTable = "mkt_voice";
      whereStr = "where function_code != '8050' "
            + "and   input_date between ? and ? "
            + "and   document != '' "
            + "and   p_seqno  !='' ";

      setString(1, minDate);
      setString(2, businessDate);

      openCursor();

      totalCnt = 0;
      int outCnt = 0;
      insertCnt = 0;
      setValue("data_from", "3");
      while (fetchTable()) {
         totalCnt++;

         setValue("mfcl.p_seqno", getValue("p_seqno"));
         int cnt1 = getLoadData("mfcl.p_seqno");
         if (cnt1 == 0) {
            outCnt++;
            continue;
         }

         for (int inti = 0; inti < parmCnt; inti++) {
            if (getValue("record_date").compareTo(getValue("parm.issue_date_s", inti)) < 0) {
               continue;
            }
            if (getValue("record_date").compareTo(
                  comm.nextNDate(getValue("parm.issue_date_e", inti),
                        (getValueInt("parm.purchase_days", inti) + 1))) > 0) {
               continue;
            }

            for (int intk = 0; intk < pseqCnt[inti]; intk++) {
               if (recordGroupNo[inti][intk].length() == 0) {
                  continue;
               }

               setValue("mbda.data_key", recordGroupNo[inti][intk]);
               setValue("mbda.data_type", "1");
               setValue("mbda.data_code", getValue("record_no"));
               int cnt2 = getLoadData("mbda.data_key,mbda.data_type,mbda.data_code");
               if (cnt2 == 0) {
                  continue;
               }

               if (getValue("record_date").compareTo(getValue("mbda.data_code2")) < 0) {
                  continue;
               }
               if (getValue("record_date").compareTo(getValue("mbda.data_code3")) > 0) {
                  continue;
               }

               insertMktFstpRecord(inti, intk);

               if ((getValue("parm.c_record_cond", inti).equals("Y")) &&
                     (recordGroupNo[inti][intk].equals(getValue("parm.c_record_group_no", inti)))) {
                  updateMktFstpCarddtlC(inti);
               } else {
                  setValue("reco.id_p_seqno", getValue("mfcl.id_p_seqno"));
                  setValue("reco.active_code", getValue("parm.active_code", inti));
                  int cnt3 = getLoadData("reco.id_p_seqno,reco.active_code");

                  if (cnt3 == 0) {
                     updateMktFstpCarddtl(inti, intk);
                  } else {
                     for (int intm = 0; intm < cnt3; intm++) {
                        if ((getValue("record_date") + getValue("record_time")).compareTo(
                              (getValue("reco.record_date", intm) + getValue("reco.record_time", intm))) < 0) {
                           continue;
                        }

                        if ((getValue("record_date") + getValue("record_time") + getValue("record_no")).compareTo(
                              (getValue("reco.record_date", intm) +
                                    getValue("reco.record_time", intm) +
                                    getValue("reco.record_no", intm))) == 0) {
                           continue;
                        }

                        updateMktFstpCarddtl(inti, intk);
                     }
                  }
               }
            }
         }

         processDisplay(50000); // every 10000 display message
      }
      closeCursor();
   }

   // ************************************************************************
   int insertMktFstpRecord(int inti, int intk) throws Exception {
      dateTime();
      extendField = "frec.";
      setValue("frec.active_code", getValue("parm.active_code", inti));
      setValue("frec.id_p_seqno", getValue("mfcl.id_p_seqno"));
      setValue("frec.record_group_no", recordGroupNo[inti][intk]);
      setValue("frec.record_no", getValue("record_no"));
      setValue("frec.record_date", getValue("record_date"));
      setValue("frec.record_time", getValue("record_time"));
      setValue("frec.data_from", getValue("data_from"));
      setValue("frec.mod_time", sysDate + sysTime);
      setValue("frec.mod_pgm", javaProgram);

      daoTable = "mkt_fstp_record";

      insertTable();

      return (0);
   }

   // ************************************************************************
   int updateMktFstpCarddtl(int inti, int intk) throws Exception {
      daoTable = "mkt_fstp_carddtl";
      updateSQL = "active_seq        = ?,"
            // + "level_seq = ?,"
            + "record_group_no   = ?,"
            + "record_no         = ?,"
            + "mod_time          = sysdate,"
            + "mod_pgm           = ? ";
      whereStr = "where active_code   = ? "
            + "and   id_p_seqno    = ? "
            + "and   proc_flag     = 'N' "
            + "and   error_code    = '00' ";

      setString(1, activeSeq[inti][intk]);
      // setString(2 , levelSeq[inti][intk]);
      setString(2, recordGroupNo[inti][intk]);
      setString(3, getValue("record_no"));
      setString(4, javaProgram);
      setString(5, getValue("parm.active_code", inti));
      setString(6, getValue("mfcl.id_p_seqno"));

      int n = updateTable();
      return n;
   }

   // ************************************************************************
   int updateMktFstpCarddtlC(int inti) throws Exception {
      daoTable = "mkt_fstp_carddtl";
      updateSQL = "active_seq        = '',"
//            + "level_seq         = '',"
            + "record_group_no   = '',"
            + "record_no         = ?,"
            + "mod_time          = sysdate,"
            + "mod_pgm           = ? ";
      whereStr = "where active_code   = ? "
            + "and   id_p_seqno    = ? "
            + "and   proc_flag     = 'N' "
            + "and   error_code    = '00' ";

      setString(1, getValue("record_no"));
      setString(2, javaProgram);
      setString(3, getValue("parm.active_code", inti));
      setString(4, getValue("mfcl.id_p_seqno"));

      int n = updateTable();
      return n;
   }

   // ************************************************************************
   void loadMktFstpCarddtl() throws Exception {
      extendField = "mfcl.";
      selectSQL = "a.p_seqno,"
            + "b.id_no,"
            + "a.id_p_seqno";
      daoTable = "mkt_fstp_carddtl a,crd_idno b";
      whereStr = "WHERE a.proc_flag  = 'N' "
            + "and   a.error_code = '00' "
            + "and   a.id_p_seqno = b.id_p_seqno "
            + "group by a.p_seqno,a.id_p_seqno,b.id_no";

      int n = loadTable();
      setLoadData("mfcl.p_seqno");
      setLoadData("mfcl.id_no");

      showLogMessage("I", "", "Load mkt_fstp_carddtl Count: [" + n + "]");
   }

   // ************************************************************************
   int loadMktBnData() throws Exception {
      extendField = "mbda.";
      selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2,"
            + "data_code3";
      daoTable = "mkt_bn_data";
      whereStr = "WHERE TABLE_NAME = 'WEB_RECORD_GROUP' ";

      int n = loadTable();
      setLoadData("mbda.data_key,mbda.data_type,mbda.data_code");

      showLogMessage("I", "", "Load mkt_bn_data Count: [" + n + "]");
      return (n);
   }

   // ************************************************************************
   int selectCrdIdno() throws Exception {
      extendField = "idno.";
      selectSQL = "id_no";
      daoTable = "crd_idno";
      whereStr = "where id_p_seqno = ? ";

      setString(1, getValue("major_id_p_seqno"));

      int recordCnt = selectTable();

      if (notFound.equals("Y")) {
         return (1);
      }
      return (0);
   }

   // ************************************************************************
   void loadMktFstpRecord() throws Exception {
      extendField = "reco.";
      selectSQL = "id_p_seqno,"
            + "active_code,"
            + "record_no,"
            + "record_date,"
            + "record_time";
      daoTable = "mkt_fstp_record";
      whereStr = "where active_code in "
            + "      (select active_code "
            + "       from   mkt_fstp_parm "
            + "       WHERE apr_flag        = 'Y' "
            + "       AND   apr_date       != ''  "
            + "       AND   (stop_flag     != 'Y'  "
            + "        or    (stop_flag     = 'Y'  "
            + "         and  stop_date      > ? )) "
            + "       and   issue_date_s <= ? "
            + "       and   to_char(to_date(issue_date_e,'yyyymmdd')+"
            + "                    (purchase_days+n1_days+1)  days,'yyyymmdd') >= ?) ";

      setString(1, businessDate);
      setString(2, businessDate);
      setString(3, businessDate);

      int n = loadTable();
      setLoadData("reco.id_p_seqno,reco.active_code");

      showLogMessage("I", "", "Load mkt_fstp_record Count: [" + n + "]");
   }
   // ************************************************************************

} // End of class FetchSample

