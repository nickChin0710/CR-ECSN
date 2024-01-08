/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-13  V1.00.01    tanwei    updated for project coding standard       *
*                                                                             *
******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*黑名單轉取消代行授權明細檔處理程式*/
public class TscB020 extends AccessDAO {
    private final String progname = "黑名單轉取消代行授權明細檔處理程式   109/11/13 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTrahTscCardNo = "";
    int totalCnt = 0;
    int totalCnt2 = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0) {
                comc.errExit("Usage : TscB020 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            selectPtrBusinday();

            selectTscRmActauth1();
            showLogMessage("I", "", String.format("累計恢復 [%d] 筆", totalCnt));

            selectTscRmActauth2();
            showLogMessage("I", "", String.format("累計取消 [%d] 筆", totalCnt2));

            // ==============================================
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

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                    : hBusiBusinessDate;
        }

    }

    /***********************************************************************/
    void selectTscRmActauth1() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "tsc_card_no ";
        sqlCmd += "from tsc_rm_actauth ";
        sqlCmd += "where send_reason = '10' ";
        sqlCmd += "MINUS ";
        sqlCmd += "select a.tsc_card_no ";
        sqlCmd += "from tsc_bkec_log a ";
        sqlCmd += "where crt_date = to_char(sysdate,'yyyymmdd') ";
        sqlCmd += "and not exists (select tsc_card_no ";
        sqlCmd += "from tsc_bkec_expt c ";
        sqlCmd += "where c.tsc_card_no = a.tsc_card_no ";
        sqlCmd += "  and (c.black_flag = '3' ";
        sqlCmd += "  and to_char(sysdate,'yyyymmdd') ";
        sqlCmd +=     "  between decode(c.send_date_s,'','19000101',c.send_date_s) ";
        sqlCmd += "  and decode(c.send_date_e,'','29991231',c.send_date_e))) ";
        openCursor();
        while (fetchTable()) {
            hTrahTscCardNo = getValue("tsc_card_no");

            totalCnt++;

            updateTscRmActauth();
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateTscRmActauth() throws Exception {
        daoTable   = "tsc_rm_actauth";
        updateSQL  = " restore_date = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ?";
        whereStr   = "where tsc_card_no = ?  ";
        whereStr  += "  and send_reason = '10' ";
        setString(1, javaProgram);
        setString(2, hTrahTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_rm_actauth not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectTscRmActauth2() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "a.tsc_card_no ";
        sqlCmd += "from tsc_bkec_log a ";
        sqlCmd += "where crt_date = to_char(sysdate,'yyyymmdd') ";
        sqlCmd += "and not exists (select tsc_card_no ";
        sqlCmd += "from tsc_bkec_expt c ";
        sqlCmd += "where c.tsc_card_no = a.tsc_card_no ";
        sqlCmd += "  and (c.black_flag = '3' ";
        sqlCmd += "  and to_char(sysdate,'yyyymmdd') ";
        sqlCmd +=       " between decode(c.send_date_s,'','19000101',c.send_date_s) ";
        sqlCmd +=       "     and decode(c.send_date_e,'','29991231',c.send_date_e))) ";
        sqlCmd += "MINUS ";
        sqlCmd += "select tsc_card_no ";
        sqlCmd += "from tsc_rm_actauth ";
        sqlCmd += "where send_reason = '10' ";
        openCursor();
        while (fetchTable()) {
            hTrahTscCardNo = getValue("tsc_card_no");

            totalCnt2++;

            insertTscRmActauth();
        }
        closeCursor();
    }

    /***********************************************************************/
    void insertTscRmActauth() throws Exception {
        sqlCmd = "insert into tsc_rm_actauth ";
        sqlCmd += "(send_reason,";
        sqlCmd += "risk_class,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "remove_date,";
        sqlCmd += "card_no,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "new_end_date,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += "'10',";
        sqlCmd += "'57',";
        sqlCmd += "b.tsc_card_no,";
        sqlCmd += "to_char(sysdate,'yyyymmdd'),";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "b.new_end_date,";
        sqlCmd += "sysdate,";
        sqlCmd += "? ";
        sqlCmd += "from crd_card a,tsc_card b where a.card_no  = b.card_no and b.tsc_card_no = ? ";
        setString(1, javaProgram);
        setString(2, hTrahTscCardNo);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscB020 proc = new TscB020();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
