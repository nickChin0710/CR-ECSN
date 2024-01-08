/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/29  V1.00.01    Brian     error correction                          *
 *  107/10/18  V1.00.02    David     add delete_act_jcic_curr                  *
 *  111/10/12  V1.00.03    Suzuwei     sync from mega & updated for project coding standard   * 
 *  112/05/09  V1.00.04    Simon     1.關帳後二日改為關帳後一日執行            *
 *                                   2.comc.errExit() 取代 comcr.errRtn() 顯示"關帳日後一日執行"*
 ******************************************************************************/
package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*報送JCICKK4外幣資料處理程式*/
public class ActN040 extends AccessDAO {

    private String PROGNAME = "報送JCICKK4外幣資料處理程式  112/05/09 V1.00.04";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActN040";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    int recordCnt = 0;

    String hBusiBusinessDate = "";
    String hWdayThisAcctMonth = "";
    int totalCnt = 0;
    private String hWdayStmtCycle = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            exceptExit = 1;
            if (args.length > 2) {
                comc.errExit("Usage : ActN040 business_date seqno", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hBusiBusinessDate = "";
            if (args.length == 1)
                hBusiBusinessDate = args[0];
            selectPtrBusinday();

            if (selectPtrWorkday() != 0) {
                exceptExit = 0;
              //comcr.errRtn(String.format("本程式為關帳日後一日執行, 本日[%s] ! ", hBusiBusinessDate), "", hCallBatchSeqno);
                comc.errExit(String.format("本程式為關帳日後一日執行, 本日[%s] ! ", hBusiBusinessDate), "");
            }
            showLogMessage("I", "", String.format("Business_date[%s]", hBusiBusinessDate));
            showLogMessage("I", "", String.format("acct_month[%s]", hWdayThisAcctMonth));
            
            //showLogMessage("I", "", "====================================");
            //showLogMessage("I", "", "   刪除 act_jcic_curr");
            //delete_act_jcic_curr();
            //showLogMessage("I", "", String.format("     Total delete records[%d]", total_cnt));

            showLogMessage("I", "", String.format("===================================="));
            showLogMessage("I", "", String.format("   新增 act_jcic_curr"));
            insertActJcicCurr();
            showLogMessage("I", "", String.format("     Total process records[%d]", totalCnt));
            showLogMessage("I", "", String.format("===================================="));

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
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
        }

    }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        sqlCmd = "select this_acct_month, stmt_cycle ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where this_close_date = to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd') ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayThisAcctMonth = getValue("this_acct_month");
            hWdayStmtCycle  = getValue("stmt_cycle");
        } else
            return 1;

        return 0;
    }

    /***********************************************************************/
    void insertActJcicCurr() throws Exception {
        /***
        sqlCmd = "insert into act_jcic_curr ";
        sqlCmd += " (p_seqno,";
        sqlCmd += " jcic_curr_type,";
        sqlCmd += " acct_month,";
        sqlCmd += " this_ttl_amt,";
        sqlCmd += " mod_time)";
        sqlCmd += "select ";
        sqlCmd += " b.p_seqno,";
        sqlCmd += " sum(decode(b.bill_sort_seq, '2', 1, '5', 2, '4', 4, 8)),";
        sqlCmd += " ?,";
        sqlCmd += " max(b.this_ttl_amt),";
        sqlCmd += " max(sysdate) ";
        sqlCmd += " from cyc_acmm_curr_" + h_wday_stmt_cycle + " b,cyc_acmm_" + h_wday_stmt_cycle + " c ";
        sqlCmd += " where b.p_seqno = c.p_seqno ";
        sqlCmd += "    and b.curr_code !='901' ";
        sqlCmd += " GROUP BY b.p_seqno ";
        setString(1, h_wday_this_acct_month);
        int actCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.err_rtn("insert_" + daoTable + " duplicate!", "", h_call_batch_seqno);
        }
        ***/

        sqlCmd = "insert into act_jcic_curr ";
        sqlCmd += " (p_seqno,";
        sqlCmd += " jcic_curr_type,";
        sqlCmd += " acct_month,";
        sqlCmd += " this_ttl_amt,";
        sqlCmd += " mod_time)";
        sqlCmd += "select ";
        sqlCmd += " b.p_seqno,";
        sqlCmd += " sum(decode(p.bill_sort_seq, '2', 1, '5', 2, '4', 4, 8)),";
        sqlCmd += " ?,";
        sqlCmd += " max(b.ttl_amt),";
        sqlCmd += " max(sysdate) ";
        sqlCmd += " from act_acct_curr b, act_acno c, ptr_currcode p ";
        sqlCmd += " where b.p_seqno = c.acno_p_seqno ";
        sqlCmd += "    and b.curr_code = p.curr_code ";
        sqlCmd += "    and c.stmt_cycle = ? ";
        sqlCmd += "    and b.curr_code !='901' ";
        sqlCmd += " GROUP BY b.p_seqno ";
        setString(1, hWdayThisAcctMonth);
        setString(2, hWdayStmtCycle);
        int actCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

        totalCnt = actCnt;

    }
    /***************************************************************************/
    private void deleteActJcicCurr() throws Exception {           
       daoTable = "act_jcic_curr";
       whereStr = "WHERE  acct_month = ? ";
     //if delete-used, it should be definite stmt_cycle.
       whereStr += " and   p_seqno in (select acno_p_seqno from act_acno where stmt_cycle =  ? ) ";
       setString(1, hWdayThisAcctMonth);
       setString(2, hWdayStmtCycle);

       totalCnt = deleteTable();

     }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActN040 proc = new ActN040();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
