/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/12/15  V1.00.01    Wilson    program initial                           *
 *  112/05/12  V1.00.02    Wilson    有續卡current_code = '7'                    *
 ******************************************************************************/

package Ich;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*一卡通停卡日處理程式*/
public class IchC001 extends AccessDAO {
    private String progname = "愛金卡停卡日處理程式  112/05/12 V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hIchCardNo = "";
    String hNewIchCardNo = "";
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
                comc.errExit("Usage : IchC001 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.callbatch(0, 0, 0);

            hBusiBusinessDate = "";
            
            selectPtrBusinday();
            
            if (args.length == 1) {
            	if (args[0].length() == 8) {
            		hBusiBusinessDate = args[0];
            	}
            }                      
            
            showLogMessage("I", "", "執行 日期 = [" + hBusiBusinessDate + "]");

            selectIchCard();

            showLogMessage("I", "", String.format("Process records = [%d]\n", totalCnt));

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
    void selectIchCard() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "ich_card_no, ";
        sqlCmd += "new_ich_card_no ";
        sqlCmd += " from ich_card ";
        sqlCmd += "where current_code = '0' ";
        sqlCmd += "  and new_end_date  < ? ";
        setString(1, hBusiBusinessDate);
        openCursor();
        while (fetchTable()) {
            hIchCardNo = getValue("ich_card_no");
            hNewIchCardNo = getValue("new_ich_card_no");

            totalCnt++;
            updateIchCard();
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateIchCard() throws Exception {
        daoTable   = "ich_card";
        updateSQL  = " current_code = ?,";
        updateSQL += " oppost_date = ?,";
        updateSQL += " ich_oppost_date = ? ,";
        updateSQL += " mod_pgm         = ? ,";
        updateSQL += " mod_time        = sysdate";
        whereStr   = "where ich_card_no = ? ";
        if(hNewIchCardNo.length() > 0 && !hIchCardNo.equals(hNewIchCardNo)) {
        	setString(1, "7");
        }
        else {
        	setString(1, "1");
        }
        setString(2, sysDate);
        setString(3, sysDate);
        setString(4, javaProgram);
        setString(5, hIchCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ich_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IchC001 proc = new IchC001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
