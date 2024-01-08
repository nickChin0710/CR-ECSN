/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00  Edson       program initial                           *
 *  106/12/26  V1.00.01  Brian       error correction                          *
 *  109/11/19  V1.00.02  shiyuqi     updated for project coding standard       * 
 *  109/12/30  V1.00.03  Zuwei       “icbcecs”改為”system”                     *
 *  112/05/02  V1.00.04  Simon       1.TCB 商務卡個繳戶合併為一筆以公司戶報送  *
 *                                   2.add fetch's daoTable                    *
********************************************************************************/

package Act;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*帳戶報送JCIC結案應收帳款異動處理程式*/
public class ActN016 extends AccessDAO {

    private String progname = "帳戶報送JCIC結案應收帳款異動處理程式  112/05/02  V1.00.04 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActN016";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hTempPSeqno = "";
    String hAjegRowid = "";
    String hAcctStmtCycle = "";
    String hAcctPSeqno = "";
    String hAcctAcctType = "";
    String hAcctAcctKey = "";
    String hAjtnTxnType = "";
    String hAjlgAcctMonth = "";
    String hAjtnPaymentRate = "";
    String hWdayThisAcctMonth = "";
    String hAjlgJcicAcctStatus = "";
    String hAjlgJcicAcctStatusFlag = "";
    String hAcctOriSaleDate = "";
    String hAcctOriAcctStatus = "";
    String hAcctAcnoFlag = "";
    String hAcctCorpPSeqno = "";
    double hAcctJrnlBal = 0.0;
    double hCorpJrnlBal = 0.0;

    int hTempCnt = 0;
    int totalCnt = 0;
    int insertCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            exceptExit = 1;
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ActN016 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            if ((args.length == 1) && args[0].length() == 8) {
                hBusiBusinessDate = args[0];
                hCallBatchSeqno = "";
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            selectPtrBusinday();

            selectActJcicEndLog();

            selectActJcicEndLog2();

            showLogMessage("I", "", String.format("Total process record[%d] insert[%d]", totalCnt, insertCnt));

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select decode( cast(? as varchar(8)) ,'',business_date, ? ) h_busi_business_date";
        sqlCmd += " from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
        }

    }

  /***********************************************************************/
  void selectActJcicEndLog() throws Exception {

    fetchExtend = "ajel.";
    daoTable = "act_acct a,act_jcic_end_log b,act_acno c";

    sqlCmd  = "select ";
    sqlCmd += " b.rowid rowid,";
    sqlCmd += " a.stmt_cycle,";
    sqlCmd += " a.p_seqno,";
    sqlCmd += " a.acct_type,";
    sqlCmd += " c.acct_key,";
    sqlCmd += " decode(ori_sale_date,'',decode(sign(a.acct_jrnl_bal),1,"
            + " decode(b.ori_acct_status,'','NC','NA'),decode(b.ori_acct_status,'','UC','UA')),"
            + " 'TB') h_ajtn_txn_type ";
    sqlCmd += "from act_acct a,act_jcic_end_log b,act_acno c ";
    sqlCmd += "where a.p_seqno = b.p_seqno ";
    sqlCmd += "  and a.p_seqno = c.acno_p_seqno ";
    sqlCmd += "  and c.acno_flag = '1' ";
    sqlCmd += "  and b.proc_mark = 'N' ";
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
      hAjegRowid = getValue("ajel.rowid");
      hAcctStmtCycle = getValue("ajel.stmt_cycle");
      hAcctPSeqno = getValue("ajel.p_seqno");
      hAcctAcctType = getValue("ajel.acct_type");
      hAcctAcctKey = getValue("ajel.acct_key");
      hAjtnTxnType = getValue("ajel.h_ajtn_txn_type");

      if (((totalCnt % 5000) == 0) && (totalCnt > 0)) {
          showLogMessage("I", "", String.format("Process record[%d] ",totalCnt));
          commitDataBase();
      }
      totalCnt++;
      selectPtrWorkday();
      if (selectActJcicLog() != 0) {
          hAjtnTxnType = "X" + hAjtnTxnType.substring(1);
      } else {
          hAjtnPaymentRate = "00";
          if (selectCrdCard() != 0) {
              hAjtnPaymentRate = "01";
              if ((hAjtnTxnType.equals("UC")) || (hAjlgJcicAcctStatusFlag.equals("U")))
                  hAjtnTxnType = "0" + hAjtnTxnType.substring(1);
          }
          if (hAjtnTxnType.toCharArray()[1] == 'A') {
              if ((!hAjlgJcicAcctStatusFlag.equals("U")) && (hAjlgJcicAcctStatus.length() == 0))
                  hAjtnTxnType = "1" + hAjtnTxnType.substring(1);
          } else if (hAjtnTxnType.toCharArray()[1] == 'B') {
              if (!hAjlgJcicAcctStatusFlag.equals("T"))
                  hAjtnTxnType = "2" + hAjtnTxnType.substring(1);
          } else {
              if ((!hAjlgAcctMonth.equals(hWdayThisAcctMonth))
                      && (hAjtnTxnType.toCharArray()[0] == 'U'))
                  hAjtnTxnType = "3" + hAjtnTxnType.substring(1);
          }

      }

      if ((hAjtnTxnType.equals("UC")) || (hAjtnTxnType.equals("UA")) || (hAjtnTxnType.equals("TB"))
              || (hAjtnTxnType.equals("NA")))
          insertActJcicTxn();
      updateActJcicEndLog();
    }
    closeCursor(cursorIndex);

  }

    /***********************************************************************/
    void selectPtrWorkday() throws Exception {
        hWdayThisAcctMonth = "";

        sqlCmd = "select this_acct_month ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where stmt_cycle = ? ";
        setString(1, hAcctStmtCycle);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_workday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hWdayThisAcctMonth = getValue("this_acct_month");
        }

    }

    /***********************************************************************/
    int selectActJcicLog() throws Exception {
        hAjlgAcctMonth = "";
        hAjlgJcicAcctStatus = "";
        hAjlgJcicAcctStatusFlag = "";

        sqlCmd = "select acct_month,";
        sqlCmd += " jcic_acct_status,";
        sqlCmd += " jcic_acct_status_flag ";
        sqlCmd += " from act_jcic_log  ";
        sqlCmd += "where p_seqno    = ?  ";
        sqlCmd += "  and log_type   = 'A'  ";
        sqlCmd += "  and acct_month = (select max(acct_month) " + "from act_jcic_log " + "where p_seqno    = ?  ";
        sqlCmd += "                       and log_type   = 'A'  ";
        sqlCmd += "                       and acct_month > to_char(add_months( to_date(?,'yyyymm'),-12), 'yyyymm')) ";
        setString(1, hAcctPSeqno);
        setString(2, hAcctPSeqno);
        setString(3, hWdayThisAcctMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAjlgAcctMonth = getValue("acct_month");
            hAjlgJcicAcctStatus = getValue("jcic_acct_status");
            hAjlgJcicAcctStatusFlag = getValue("jcic_acct_status_flag");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    int selectCrdCard() throws Exception {

        sqlCmd = "select max(decode(current_code,'3',1,0)) h_temp_cnt ";
        sqlCmd += " from crd_card a  ";
        sqlCmd += "where acno_p_seqno = ?  ";
        sqlCmd += "  and id_p_seqno  = major_id_p_seqno  ";
        sqlCmd += "  and oppost_date = (select max(oppost_date) from crd_card where acno_p_seqno = a.acno_p_seqno  ";
        sqlCmd += "                                                             and id_p_seqno = major_id_p_seqno) ";
        setString(1, hAcctPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
        } else
            return (0);
        return (hTempCnt);
    }

    /***********************************************************************/
    void insertActJcicTxn() throws Exception {
        insertCnt++;
        setValue("this_acct_month", hAjlgAcctMonth);
        setValue("p_seqno", hAcctPSeqno);
        setValue("business_date", hBusiBusinessDate);
        setValueInt("payment_num", 99);
        setValue("acct_type", hAcctAcctType);
        setValue("acct_key", hAcctAcctKey);
        setValue("payment_rate", hAjtnPaymentRate);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        setValue("apr_date", hBusiBusinessDate);
        setValue("apr_user", "system");
        setValue("txn_type", hAjtnTxnType);
        daoTable = "act_jcic_txn";
        insertTable();
        if (dupRecord.equals("Y")) {
          showLogMessage("I", "", String.format("insert_" + daoTable + " duplicate!" + 
          " p_seqno[%s], business_date[%s], payment_num[%d]", 
          hAcctPSeqno, hBusiBusinessDate,99));
        }

    }

    /***********************************************************************/
    void updateActJcicEndLog() throws Exception {
        daoTable = "act_jcic_end_log";
        updateSQL = " proc_mark = substr(?, 1, 1),";
        updateSQL += " proc_date = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm   = 'ActN016'";
        whereStr = "where rowid  = ? ";
        setString(1, hAjtnTxnType);
        setString(2, hBusiBusinessDate);
        setRowId(3, hAjegRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_jcic_end_log not found!", "", hCallBatchSeqno);
        }

    }

  /***********************************************************************/
  void selectActJcicEndLog2() throws Exception {

    fetchExtend = "ajel2.";
    daoTable = "act_jcic_end_log b,act_acno c";

    sqlCmd = "select ";
    sqlCmd += " b.rowid rowid,";
    sqlCmd += " b.p_seqno,";
    sqlCmd += " b.ori_sale_date,";
    sqlCmd += " b.ori_acct_status,";
    sqlCmd += " c.acno_flag,";
    sqlCmd += " c.stmt_cycle,";
    sqlCmd += " c.acct_type,";
    sqlCmd += " c.corp_p_seqno ";
    sqlCmd += "from act_jcic_end_log b,act_acno c ";
    sqlCmd += "where c.acno_p_seqno = b.p_seqno ";
    sqlCmd += "  and c.acno_flag in ('2','3') ";
    sqlCmd += "  and b.proc_mark = 'N' ";
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
      hAjegRowid = getValue("ajel2.rowid");
      hAcctPSeqno = getValue("ajel2.p_seqno");
      hAcctOriSaleDate = getValue("ajel2.ori_sale_date");
      hAcctOriAcctStatus = getValue("ajel2.ori_acct_status");
      hAcctAcnoFlag = getValue("ajel2.acno_flag");
      hAcctStmtCycle = getValue("ajel2.stmt_cycle");
      hAcctAcctType = getValue("ajel2.acct_type");
      hAcctCorpPSeqno = getValue("ajel2.corp_p_seqno");
    //hAjtnTxnType = getValue("h_ajtn_txn_type");
      if (((totalCnt % 5000) == 0) && (totalCnt > 0)) {
          showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
          commitDataBase();
      }
      totalCnt++;

      if (hAcctAcnoFlag.equals("3")) {
        hAcctPSeqno = selectActAcno();
      } 

      if (hAcctOriSaleDate.length() != 0) {
        hAjtnTxnType = "TB";
      } else {
        hCorpJrnlBal = 0;
        selectActAcct();
        if (hCorpJrnlBal > 0) {
          if (hAcctOriAcctStatus.equals(""))
            hAjtnTxnType = "NC";
          else
            hAjtnTxnType = "NA";
        } else {
          if (hAcctOriAcctStatus.equals(""))
            hAjtnTxnType = "UC";
          else
            hAjtnTxnType = "UA";
        }
      } 

      selectPtrWorkday();
      if (selectActJcicLog() != 0) {
          hAjtnTxnType = "X" + hAjtnTxnType.substring(1);
      } else {
          hAjtnPaymentRate = "00";
          if (selectCrdCard2() != 0) {
              hAjtnPaymentRate = "01";
              if ((hAjtnTxnType.equals("UC")) || (hAjlgJcicAcctStatusFlag.equals("U")))
                  hAjtnTxnType = "0" + hAjtnTxnType.substring(1);
          }
          if (hAjtnTxnType.toCharArray()[1] == 'A') {
              if ((!hAjlgJcicAcctStatusFlag.equals("U")) && (hAjlgJcicAcctStatus.length() == 0))
                  hAjtnTxnType = "1" + hAjtnTxnType.substring(1);
          } else if (hAjtnTxnType.toCharArray()[1] == 'B') {
              if (!hAjlgJcicAcctStatusFlag.equals("T"))
                  hAjtnTxnType = "2" + hAjtnTxnType.substring(1);
          } else {
              if ((!hAjlgAcctMonth.equals(hWdayThisAcctMonth))
                      && (hAjtnTxnType.toCharArray()[0] == 'U'))
                  hAjtnTxnType = "3" + hAjtnTxnType.substring(1);
          }

      }

      if ((hAjtnTxnType.equals("UC")) || (hAjtnTxnType.equals("UA")) || (hAjtnTxnType.equals("TB"))
              || (hAjtnTxnType.equals("NA")))
          insertActJcicTxn();
      updateActJcicEndLog();
    }
    closeCursor(cursorIndex);

  }

  /***********************************************************************/
	void selectActAcct() throws Exception {

    extendField = "acct.";
		sqlCmd  = " select ";
 		sqlCmd += " a.p_seqno, ";
 		sqlCmd += " b.acct_jrnl_bal ";
    sqlCmd += "  from act_acct b,act_acno a ";
		sqlCmd += " where 1=1 ";
		sqlCmd += "   and a.acno_flag in ('3') ";
		sqlCmd += "   and a.p_seqno = b.p_seqno ";
		sqlCmd += "   and a.corp_p_seqno = ?  ";
		sqlCmd += "   and a.acct_type = ?  ";

    setString(1, hAcctCorpPSeqno);
    setString(2, hAcctAcctType);
    int recordCnt = selectTable();
    for (int ii = 0; ii < recordCnt; ii++) {
      hAcctJrnlBal = getValueDouble("acct.acct_jrnl_bal",ii);
      hCorpJrnlBal += hAcctJrnlBal;
		}
		return;
	}
	
  /***********************************************************************/
  int selectCrdCard2() throws Exception {

    sqlCmd = "select max(decode(current_code,'3',1,0)) h_temp_cnt ";
    sqlCmd += " from crd_card a  ";
    sqlCmd += "where corp_p_seqno = ?  ";
    sqlCmd += "  and acct_type   = ?  ";
    sqlCmd += "  and id_p_seqno  = major_id_p_seqno  ";
    sqlCmd += "  and oppost_date = (select max(oppost_date) from crd_card "
            + "where corp_p_seqno = ? and acct_type   = ? ";
    sqlCmd += "  and id_p_seqno = major_id_p_seqno) ";
    setString(1, hAcctCorpPSeqno);
    setString(2, hAcctAcctType);
    setString(3, hAcctCorpPSeqno);
    setString(4, hAcctAcctType);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
        hTempCnt = getValueInt("h_temp_cnt");
    } else
        return (0);
    return (hTempCnt);
  }

  /***********************************************************************/
	String selectActAcno() throws Exception {

    String lAcctPSeqno = "";

    extendField = "acno.";
		sqlCmd  = " select ";
 		sqlCmd += " a.p_seqno ";
    sqlCmd += "  from act_acno a ";
		sqlCmd += " where 1=1 ";
		sqlCmd += "   and a.acno_flag in ('2') ";
		sqlCmd += "   and a.corp_p_seqno = ?  ";
		sqlCmd += "   and a.acct_type = ?  ";

    setString(1, hAcctCorpPSeqno);
    setString(2, hAcctAcctType);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      lAcctPSeqno = getValue("acno.p_seqno");
		}
		return lAcctPSeqno;
	}
	
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActN016 proc = new ActN016();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
