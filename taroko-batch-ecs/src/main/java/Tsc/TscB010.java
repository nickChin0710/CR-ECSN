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

/*取消代行授權資料轉歷史檔處理程式*/
public class TscB010 extends AccessDAO {
    private final String progname = "取消代行授權資料轉歷史檔處理程式   109/11/13 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hMTrahRowid = "";
    String hTrahRowid = "";
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
            if (args.length != 0) {
                comc.errExit("Usage : TscB010 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            selectPtrBusinday();

            selectTscRmActauth();

            showLogMessage("I", "", String.format("累計新增 [%d] 筆", totalCnt));
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
    void selectTscRmActauth() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += " from tsc_card b,tsc_rm_actauth a ";
        sqlCmd += "where a.tsc_card_no = b.tsc_card_no ";
        sqlCmd += "  and rpt_resp_code = '00' ";
        sqlCmd += "  and (a.rt_send_date  <> '' or substr(b.new_end_date,1,6) < substr(?,1,6)) ";
        setString(1, hBusiBusinessDate);
        openCursor();
        while (fetchTable()) {
            hTrahRowid = getValue("rowid");

            totalCnt++;

            updateTscRmActauth();
            insertTscRmActauthHst();
            deleteTscRmActauth();
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateTscRmActauth() throws Exception {
        daoTable = "tsc_rm_actauth";
        updateSQL = "mod_time  = sysdate,";
        updateSQL += " mod_pgm  = ?";
        whereStr = "where rowid = ? ";
        setString(1, javaProgram);
        setRowId(2, hTrahRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_rm_actauth not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertTscRmActauthHst() throws Exception {
        sqlCmd = "insert into tsc_rm_actauth_hst ";
        sqlCmd += "select * ";
        sqlCmd += "  from tsc_rm_actauth ";
        sqlCmd += " where rowid  = ? ";
        setRowId(1, hTrahRowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteTscRmActauth() throws Exception {
        daoTable = "tsc_rm_actauth";
        whereStr = "where rowid = ? ";
        setRowId(1, hTrahRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_tsc_rm_actauth not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscB010 proc = new TscB010();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
