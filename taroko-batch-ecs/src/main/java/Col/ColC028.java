/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/08/21  V1.00.00    phopho     program initial                          *
 *  108/12/02  V1.00.01    phopho     fix err_rtn bug                          *
 *  109/12/14  V1.00.02    shiyuqi       updated for project coding standard   *
 ******************************************************************************/

package Col;

import java.util.ArrayList;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC028 extends AccessDAO {
    private String progname = "個別協商轉歷史檔案處理程式  109/12/14  V1.00.02 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hMCciePSeqno = "";
    String hMCcieInstSeqno = "";
    String hMCcieCloseSendDate = "";
    String hCciePSeqno = "";
    String hCcieInstSeqno = "";
    String hCcieCloseSendDate = "";
    List<String> aCcieCloseSendDate = new ArrayList<String>();
    List<String> aCcieInstSeqno = new ArrayList<String>();
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
            if (args.length > 1) {
                comc.errExit("Usage : ColC028 [business_date]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();

            if (args.length == 1 && args[0].length() == 8)
                hBusiBusinessDate = args[0];

            selectColCsInstbase();

            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));

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
        hBusiBusinessDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectColCsInstbase() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "p_seqno ";
        sqlCmd += "from col_cs_instbase ";
        sqlCmd += "group by p_seqno ";
        sqlCmd += "HAVING count(*) >1 ";

        openCursor();
        while (fetchTable()) {
            hCciePSeqno = getValue("p_seqno");

            totalCnt++;
            if (totalCnt % 1000 == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }

            selectColCsInstbase1();
        }
        closeCursor();
    }

    /**********************************************************************/
    void selectColCsInstbase1() throws Exception {
        sqlCmd = "select inst_seqno,";
        sqlCmd += "close_send_date ";
        sqlCmd += " from col_cs_instbase  ";
        sqlCmd += "where p_seqno = ? ORDER BY inst_seqno ";
        setString(1, hCciePSeqno);
        
        extendField = "col_cs_instbase_1.";
        
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aCcieInstSeqno.add(getValue("col_cs_instbase_1.inst_seqno", i));
            aCcieCloseSendDate.add(getValue("col_cs_instbase_1.close_send_date", i));
        }

        for (int int1 = recordCnt - 2; int1 >= 0; int1--)
            if (aCcieCloseSendDate.get(int1).length() != 0) {
                hCcieInstSeqno = aCcieInstSeqno.get(int1);
                hCcieCloseSendDate = aCcieCloseSendDate.get(int1);
                insertColCsInsthst();
                deleteColCsInstbase();
            }

        commitDataBase();
    }

    /***********************************************************************/
    void insertColCsInsthst() throws Exception {
        sqlCmd = "insert into col_cs_insthst ";
        sqlCmd += "select to_char(sysdate,'yyyymmdd'),";
        sqlCmd += "col_cs_instbase.* ";
        sqlCmd += "from col_cs_instbase ";
        sqlCmd += "where inst_seqno = ?";
        setString(1, hCcieInstSeqno);
        
        insertTable();
        if (dupRecord.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("insert_col_cs_insthst duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void deleteColCsInstbase() throws Exception {
        daoTable = "col_cs_instbase";
        whereStr = "where inst_seqno = ? ";
        setString(1, hCcieInstSeqno);
        
        deleteTable();
//        if (notFound.equals("Y")) {  //2019.12.02 Fix error?
//            comcr.err_rtn("delete_col_cs_instbase not found!", "", h_call_batch_seqno);
//        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC028 proc = new ColC028();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
