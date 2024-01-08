/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------  *
*  106/06/01  V1.00.00    SUP       program initial                         *
*  109/12/23  V1.00.01   shiyuqi       updated for project coding standard   *
*  112/10/16  V1.00.02   Wilson     不為該月一日不執行                                                                            *
****************************************************************************/

package Crd;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*PP卡效期到期處理*/
public class CrdM017 extends AccessDAO {
    public static final boolean debugMode = false;

    private String progname = "PP卡效期到期處理  112/10/16  V1.00.02 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    String hTempUser = "";

    String prgmId = "CrdM017";
    String hModUser = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";

    String hFirstDay = "";
    String hProcDate = "";
    String hSysdate = "";
    String hCrdpPpCardNo = "";
    String hCrdpRowid = "";

    int totCnt = 0;

    // ********************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : CrdM017 [yyyymm]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hFirstDay = "";
            sqlCmd = "select to_char(sysdate,'yyyymm')||'01' h_first_date,";
            sqlCmd += "to_char(sysdate,'yyyymmdd') h_proc_date ";
            sqlCmd += " from dual ";
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hFirstDay = getValue("h_first_date");
                hProcDate = getValue("h_proc_date");
                hSysdate = hProcDate;
            }
            if (args.length == 0) {
                if (!hFirstDay.equals(hProcDate)) {          		  
                	showLogMessage("I", "", String.format("今天[%s]不為本月一號,不需跑程式，程式執行結束", hProcDate));                  
                	finalProcess();                  
                	return 0;
                }
            }
            
            if (args.length == 1) {
                if (args[0].length() != 6) {
                    comcr.errRtn("Usage : CrdM017 [yyyymm]", "", hCallBatchSeqno);
                }
                hProcDate = String.format("%6.6s01", args[0]);
            }

            showLogMessage("I", "", String.format("執行日期=[%s] ", hProcDate));
            
            hModUser = comc.commGetUserID();

            sqlCmd = "select ";
            sqlCmd += "pp_card_no,";
            sqlCmd += "rowid  rowid ";
            sqlCmd += " from crd_card_pp ";
            sqlCmd += "where current_code = '0' ";
            sqlCmd += "  and valid_to     < ? ";
            setString(1, hProcDate);
            recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                hCrdpPpCardNo = getValue("pp_card_no", i);
                hCrdpRowid = getValue("rowid", i);

                totCnt++;

                updateCrdCardPp();

            }

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void updateCrdCardPp() throws Exception {
        daoTable   = "crd_card_pp ";
        updateSQL  = "current_code  = '1', "; /* 一般申停 */
        updateSQL += "oppost_reason = 'B1', ";
        updateSQL += "oppost_date   = ?, ";
        updateSQL += "mod_user      = ?, ";
        updateSQL += "mod_time      = sysdate, ";
        updateSQL += "mod_pgm       = ? ";
        whereStr   = "where rowid   = ? ";
        setString(1, hSysdate);
        setString(2, hModUser);
        setString(3, prgmId);
        setRowId(4, hCrdpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_card_pp  not found!", "", comcr.hCallBatchSeqno);
        }

        return;
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdM017 proc = new CrdM017();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
