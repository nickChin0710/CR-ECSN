/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-13  V1.00.01    tanwei    updated for project coding standard       *
*  110/01/14  V1.00.02    Wilson    新增 update tsc_vd_card                     *
*  111/12/15  V1.00.03    Wilson    只處理效期到期的資料                                                                              *
*  112/05/09  V1.00.04    Wilson    有續卡current_code = '7'                    *
******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*悠遊卡停卡日處理程式*/
public class TscC001 extends AccessDAO {
    private final String progname = "悠遊卡停卡日處理程式   112/05/09 V1.00.04";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTscCardNo = "";
    String hNewTscCardNo = "";
    String hTmpType = "";
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
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : TscC001 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            
            selectPtrBusinday();
            
            if (args.length == 1) {
            	if (args[0].length() == 8) {
            		hBusiBusinessDate = args[0];
            	}
            }                      
            
            showLogMessage("I", "", "執行 日期 = [" + hBusiBusinessDate + "]");

            selectTscCard();

            showLogMessage("I", "", String.format("Process records = [%d]", totalCnt));

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
        sqlCmd = "select to_char(sysdate,'yyyymmdd') as sysdate ";
        sqlCmd += " from ptr_businday  ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
        	hBusiBusinessDate = getValue("sysdate");
        }
        
    }

    /***********************************************************************/
    void selectTscCard() throws Exception {
        sqlCmd = "select '1' as tmp_type, ";
        sqlCmd += "tsc_card_no, ";
        sqlCmd += "new_tsc_card_no ";
        sqlCmd += " from tsc_card ";
        sqlCmd += "where current_code = '0' ";
        sqlCmd += "  and new_end_date  <  ? ";
        sqlCmd += "UNION ";
        sqlCmd += "select '2' as tmp_type, ";
        sqlCmd += "tsc_card_no, ";
        sqlCmd += "new_tsc_card_no ";
        sqlCmd += " from tsc_vd_card ";
        sqlCmd += "where current_code = '0' ";
        sqlCmd += "  and new_end_date  <  ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        openCursor();
        while (fetchTable()) {
        	hTmpType = getValue("tmp_type");
            hTscCardNo = getValue("tsc_card_no");
            hNewTscCardNo = getValue("new_tsc_card_no");

            totalCnt++;
            if (totalCnt % 10000 == 0 || totalCnt == 1) {
                showLogMessage("I", "", String.format("Process record=[%d]", totalCnt));
            }
            
            switch (hTmpType) {
            case "1":
            	updateTscCard();
                break;
            case "2":
            	updateTscVdCard();
                break;
            }           
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateTscCard() throws Exception {
        daoTable   = "tsc_card";
        updateSQL  = " current_code = ?,";
        updateSQL += " oppost_date = ?,";
        updateSQL += " tsc_oppost_date = ?,";
        updateSQL += " mod_pgm         = ?,";
        updateSQL += " mod_time        = sysdate";
        whereStr   = "where tsc_card_no = ? ";
        if(hNewTscCardNo.length() > 0 && !hTscCardNo.equals(hNewTscCardNo)) {
        	setString(1, "7");
        }
        else {
        	setString(1, "1");
        }
        setString(2, sysDate);
        setString(3, sysDate);
        setString(4, javaProgram);
        setString(5, hTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateTscVdCard() throws Exception {
        daoTable   = "tsc_vd_card";
        updateSQL  = " current_code = ?,";
        updateSQL += " oppost_date = ?,";
        updateSQL += " tsc_oppost_date = ?,";
        updateSQL += " mod_pgm         = ?,";
        updateSQL += " mod_time        = sysdate";
        whereStr   = "where tsc_card_no = ? ";
        if(hNewTscCardNo.length() > 0 && !hTscCardNo.equals(hNewTscCardNo)) {
        	setString(1, "7");
        }
        else {
        	setString(1, "1");
        }
        setString(2, sysDate);
        setString(3, sysDate);
        setString(4, javaProgram);
        setString(5, hTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_vd_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscC001 proc = new TscC001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
