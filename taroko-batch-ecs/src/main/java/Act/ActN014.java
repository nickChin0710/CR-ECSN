/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/26  V1.00.01    Brian     error correction                          *
 *  109/11/19  V1.00.02    shiyuqi   updated for project coding standard       * 
 *  112/04/05  V1.00.03    Simon     1.關帳後二日改為關帳後一日執行            *
 *                                   2.TCB 商務卡個繳戶合併為一筆以公司戶流水帳號*
 *                                   更新 act_jcic_end                         *
 *  112/04/06  V1.00.04    Simon     商務卡合計修改為 by corp_p_seqno、acct_type*
 *  112/04/25  V1.00.05    Simon     comc.errExit() 取代 comcr.errRtn() 顯示"關帳日後一日執行"*
 *  112/05/01  V1.00.06    Simon     1.add fetch's daoTable                    *
 *                                   2.selectActAcct() error fixed             *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*帳戶報送JCIC結案應收帳款異動處理程式*/
public class ActN014 extends AccessDAO {

    private String progname = "帳戶報送JCIC結案應收帳款異動處理程式   112/05/01  V1.00.06 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActN014";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hWdayStmtCycle = "";
    String hWdayThisAcctMonth = "";
    String hWdayLastAcctMonth = "";
    String hWdayLastCloseDate = "";
    String hWdayThisCloseDate = "";
    String hWdayLastDelaypayDate = "";
    String hAjedRowid = "";
    String hAjedCorpPSeqno = "";
    String hAjedAcctType = "";
    double hAcctJrnlBal = 0;
    double hCorpJrnlBal = 0;
    int totalCnt = 0;

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
                comc.errExit("Usage : ActN014 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            if (args.length == 1)
                hBusiBusinessDate = args[0];
            selectPtrBusinday();

            if (selectPtrWorkday() != 0) {
                exceptExit = 0;
              //comcr.errRtn(String.format("本程式為關帳日後一日執行, 本日[%s] ! ", hBusiBusinessDate), "", hCallBatchSeqno);
                comc.errExit(String.format("本程式為關帳日後一日執行, 本日[%s] ! ", 
                hBusiBusinessDate), hCallBatchSeqno);
            }

            showLogMessage("I","","=========================================");
            showLogMessage("I","","處理 act_acno.acno_flag='1' with act_jcic_end...");
            selectActJcicEnd();
            showLogMessage("I","","=========================================");
            showLogMessage("I","","處理 act_acno.acno_flag='2' with act_jcic_end...");
            selectActJcicEnd2();//TCB 商務卡個繳戶合併為一筆以公司戶流水帳號更新 act_jcic_end
            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));
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
        sqlCmd = "select decode( cast(? as varchar(8)) ,'',business_date, ? ) h_busi_business_date ";
        sqlCmd += " from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
        }

    }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        hWdayStmtCycle = "";
        hWdayThisAcctMonth = "";
        hWdayLastAcctMonth = "";
        hWdayThisCloseDate = "";
        hWdayLastCloseDate = "";
        hWdayLastDelaypayDate = "";

        sqlCmd = "select stmt_cycle,";
        sqlCmd += " this_acct_month,";
        sqlCmd += " last_acct_month,";
        sqlCmd += " last_close_date,";
        sqlCmd += " this_close_date,";
        sqlCmd += " last_delaypay_date ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where this_close_date = to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd') ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayThisAcctMonth = getValue("this_acct_month");
            hWdayLastAcctMonth = getValue("last_acct_month");
            hWdayLastCloseDate = getValue("last_close_date");
            hWdayThisCloseDate = getValue("this_close_date");
            hWdayLastDelaypayDate = getValue("last_delaypay_date");
        } else
            return (1);
        return (0);
    }

  /***********************************************************************/
  void selectActJcicEnd() throws Exception {

    fetchExtend = "ajed1.";
    daoTable = "act_acct a,act_jcic_end b, act_acno c";

    sqlCmd = "select ";
    sqlCmd += " b.rowid rowid ";
    sqlCmd += " from act_acct a,act_jcic_end b, act_acno c ";
    sqlCmd += "where a.p_seqno       = b.p_seqno ";
    sqlCmd += "  and c.p_seqno       = b.p_seqno ";
    sqlCmd += "  and c.acno_flag     = '1' ";
    sqlCmd += "  and a.acct_jrnl_bal > 0 ";
    sqlCmd += "  and a.stmt_cycle    = ? ";
    sqlCmd += "  and b.send_flag in ('Y','U') ";
    sqlCmd += "UNION ";
    sqlCmd += "select distinct b.rowid ";
    sqlCmd += " from crd_card a,act_jcic_end b ";
    sqlCmd += "where a.acno_p_seqno = b.p_seqno ";
    sqlCmd += "  and a.id_p_seqno   = a.major_id_p_seqno "; // and a.id = a.major_id
    sqlCmd += "  and a.current_code = '0' ";
    sqlCmd += "  and a.acno_flag    = '1' ";
    sqlCmd += "  and a.stmt_cycle   = ? ";
    sqlCmd += "  and b.send_flag in ('Y','U') ";
    setString(1, hWdayStmtCycle);
    setString(2, hWdayStmtCycle);
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
        hAjedRowid = getValue("ajed1.rowid");

        if (((totalCnt % 5000) == 0) && (totalCnt > 0)) {
            showLogMessage("I", "", String.format("Process record[%d] ", totalCnt));
            commitDataBase();
        }
        totalCnt++;
        updateActJcicEnd();
    }
    closeCursor(cursorIndex);

  }

  /***********************************************************************/
  void selectActJcicEnd2() throws Exception {

    fetchExtend = "ajed2.";
    daoTable = "act_acno a,act_jcic_end b";

    sqlCmd = "select ";
    sqlCmd += " b.rowid rowid, ";
    sqlCmd += " a.corp_p_seqno, ";
    sqlCmd += " a.acct_type ";
    sqlCmd += " from act_acno a,act_jcic_end b ";
    sqlCmd += "where a.p_seqno    = b.p_seqno ";
    sqlCmd += "  and a.acno_flag = '2' ";
    sqlCmd += "  and a.stmt_cycle    = ? ";
    sqlCmd += "  and b.send_flag in ('Y','U') ";
    setString(1, hWdayStmtCycle);
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
      hAjedRowid  = getValue("ajed2.rowid");
      hAjedCorpPSeqno = getValue("ajed2.corp_p_seqno");
      hAjedAcctType = getValue("ajed2.acct_type");

      if (((totalCnt % 5000) == 0) && (totalCnt > 0)) {
          showLogMessage("I", "", String.format("Process record[%d] ", totalCnt));
          commitDataBase();
      }
      totalCnt++;

      hCorpJrnlBal = 0;
      selectActAcct();
      if (hCorpJrnlBal > 0) {
        updateActJcicEnd();
        continue;
      }

      int retcode = selectCrdCard();
      if (retcode > 0) {
        updateActJcicEnd();
      }

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

    setString(1, hAjedCorpPSeqno);
    setString(2, hAjedAcctType);
    int recordCnt = selectTable();
    for (int ii = 0; ii < recordCnt; ii++) {
      hAcctJrnlBal = getValueDouble("acct.acct_jrnl_bal",ii);
      hCorpJrnlBal += hAcctJrnlBal;
		}
		return;
	}
	
  /***********************************************************************/
  int selectCrdCard() throws Exception {

    sqlCmd  = " select ";
    sqlCmd += " card_no,";
    sqlCmd += " current_code ";
    sqlCmd += " from crd_card  ";
    sqlCmd += "where 1=1  ";
    sqlCmd += "  and corp_p_seqno = ?  ";
    sqlCmd += "  and acct_type = ?  ";
    sqlCmd += "  and current_code = '0' ";
    sqlCmd += "  and card_no      = major_card_no ";
    setString(1, hAjedCorpPSeqno);
    setString(2, hAjedAcctType);
    int recordCnt = selectTable();
    return recordCnt;

  }

    /***********************************************************************/
    void updateActJcicEnd() throws Exception {
        daoTable = "act_jcic_end";
        updateSQL = " send_flag = 'N',";
        updateSQL += " proc_date = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm   = 'ActN014'";
        whereStr = "where rowid  = ? ";
        setString(1, hBusiBusinessDate);
        setRowId(2, hAjedRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_jcic_end not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActN014 proc = new ActN014();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
