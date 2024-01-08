/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/08/15  V1.00.00    phopho     program initial                          *
*  108/12/02  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/14  V1.00.02     shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC026 extends AccessDAO {
    private String progname = "報送JCIC(Z13)檔案處理程式  109/12/14  V1.00.02 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hCcieIdPSeqno = "";
    String hCcieId = "";
    String hCcieInstFlag = "";
    String hCcieInstSMcode = "";
    String hCcieInstSDate = "";
    String hCcieInstEDate = "";
    String hCcieCreateDate = "";
    String hCcieRepudiateDate = "";
    String hCcieCloseDate = "";
    String hCcieRowid = "";
    int hCnt = 0;
    String hCcicInstFlag = "";
    String hCcicInstSMcode = "";
    String hCcicRowid = "";
    String hCcicTranType = "";
    String hCcicChiName = "";
    String hCcicBirthday = "";
    String hCcicResidentAddr = "";
    String hCcicInstDate = "";

    int totalCnt = 0;
    int updFlag = 0;
    int jcicFlag = 0;

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
                comc.errExit("Usage : ColC026 business_date", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();

            if (args.length == 1)
                hBusiBusinessDate = args[0];

            if (selectColCsInstbase0() != 0) {
            	exceptExit = 0;
                comcr.errRtn("產生媒體程式(ColC027) 尚未執行, 本程式不可執行! error", "", hCallBatchSeqno);
            }
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
    int selectColCsInstbase0() throws Exception {

        sqlCmd = "select 1 ";
        sqlCmd += " from col_cs_instbase ";
        sqlCmd += "where proc_flag = 'X' ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();

        return recordCnt > 0 ? 1 : 0;
    }

    /**********************************************************************/
    void selectColCsInstbase() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "id_no,";
        sqlCmd += "inst_flag,";
        sqlCmd += "inst_s_mcode,";
        sqlCmd += "inst_s_date,";
        sqlCmd += "inst_e_date,";
        sqlCmd += "decode(crt_send_date,'',crt_date,'') h_ccie_create_date,";
        sqlCmd += "decode(repudiate_date,'','',decode(repudiate_send_date,'',repudiate_date,'')) h_ccie_repudiate_date,";
        sqlCmd += "decode(close_date,'','',decode(close_send_date,'',close_date,'')) h_ccie_close_date,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_cs_instbase ";
        sqlCmd += "where proc_flag not in ('X','Y') ";
        sqlCmd += "and send_flag = 'Y' ";
        sqlCmd += "and ((crt_date <> '' and crt_send_date = '') ";
        sqlCmd += "OR (repudiate_date <> '' and repudiate_send_date = '') ";
        sqlCmd += "OR (close_date <> '' and close_send_date = '')) ";
        sqlCmd += " order by inst_seqno ";

        openCursor();
        while (fetchTable()) {
            hCcieIdPSeqno = getValue("id_p_seqno");
            hCcieId = getValue("id_no");
            hCcieInstFlag = getValue("inst_flag");
            hCcieInstSMcode = getValue("inst_s_mcode");
            hCcieInstSDate = getValue("inst_s_date");
            hCcieInstEDate = getValue("inst_e_date");
            hCcieCreateDate = getValue("h_ccie_create_date");
            hCcieRepudiateDate = getValue("h_ccie_repudiate_date");
            hCcieCloseDate = getValue("h_ccie_close_date");
            hCcieRowid = getValue("rowid");

            totalCnt++;
            if (totalCnt % 5000 == 0) {
                showLogMessage("I", "", String.format("Process record[%d]\n", totalCnt));
            }

            updFlag = 0;
            selectCrdIdno();
            jcicFlag = selectColCsInstjcic();

            if ((hCcieCreateDate.length() != 0) && (hCcieCloseDate.length() != 0))
                continue;

            if (hCcieCreateDate.length() != 0) {
                if (jcicFlag == 3) {
                    hCcicInstFlag = "3";
                    hCcicTranType = "D";
                    insertColCsInstjcic1();
                }
                hCcicInstFlag = "1";
                if (jcicFlag == 1) {
                    hCcicTranType = "C";
                } else {
                    hCcicTranType = "A";
                }
                hCcicInstDate = hCcieInstEDate;
                insertColCsInstjcic();
                jcicFlag = 1;
            }

            if ((hCcieRepudiateDate.length() != 0) && (jcicFlag != 0)) {
                hCcicInstFlag = "3";
                hCcicTranType = "C";
                hCcicInstDate = hCcieRepudiateDate;
                insertColCsInstjcic();
            }

            if ((hCcieCloseDate.length() != 0) && (jcicFlag != 0)) {
                hCcicInstFlag = hCcieInstFlag;
                hCcicTranType = "C";
                hCcicInstDate = hCcieCloseDate;
                insertColCsInstjcic();
            }

            if (updFlag == 0)
                continue;
            updateColCsInstbase();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        sqlCmd = "select chi_name,";
        sqlCmd += "birthday,";
        sqlCmd += "resident_addr1||resident_addr2||resident_addr3||resident_addr4||resident_addr5 resident_addr";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hCcieIdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCcicChiName = getValue("chi_name");
            hCcicBirthday = getValue("birthday");
            hCcicResidentAddr = getValue("resident_addr");
        }

    }

    /***********************************************************************/
    int selectColCsInstjcic() throws Exception {
        int colCnt;
        sqlCmd = "select inst_flag,";
        sqlCmd += "inst_s_mcode,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from col_cs_instjcic a  ";
        sqlCmd += "where id_no = ? ORDER by proc_date desc ";
        setString(1, hCcieId);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCcicInstFlag = getValue("inst_flag");
            hCcicInstSMcode = getValue("inst_s_mcode");
            hCcicRowid = getValue("rowid");
        }

        colCnt = recordCnt;

        if (colCnt == 0)
            return 0;
        if (hCcicInstFlag.equals("2") || hCcicInstFlag.equals("4"))
            return 0;
        return comc.str2int(hCcicInstFlag);
    }

    /***********************************************************************/
    void insertColCsInstjcic1() throws Exception {
        updFlag = 1;
        sqlCmd = "insert into col_cs_instjcic ";
        sqlCmd += "(crt_date,";
        sqlCmd += "crt_time,";
        sqlCmd += "inst_seqno,";
        sqlCmd += "tran_type,";
        sqlCmd += "p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "id_no,";
        sqlCmd += "id_code,";
        sqlCmd += "chi_name,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "birthday,";
        sqlCmd += "resident_addr,";
        sqlCmd += "update_date,";
        sqlCmd += "inst_flag,";
        sqlCmd += "inst_date,";
        sqlCmd += "inst_s_date,";
        sqlCmd += "inst_e_date,";
        sqlCmd += "inst_s_mcode,";
        sqlCmd += "send_acct_no,";
        sqlCmd += "proc_flag,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "mod_time)";
        sqlCmd += " select ";
        sqlCmd += "to_char(sysdate,'yyyymmdd'),";
        sqlCmd += "to_char(sysdate,'hh24miss'),";
        sqlCmd += "inst_seqno,";
        sqlCmd += "?,";
        sqlCmd += "p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "id_no,";
        sqlCmd += "id_code,";
        sqlCmd += "?,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "inst_date,";
        sqlCmd += "inst_s_date,";
        sqlCmd += "inst_e_date,";
        sqlCmd += "?,";
        sqlCmd += "send_acct_no,";
        sqlCmd += "'N',";
        sqlCmd += "?,";
        sqlCmd += "sysdate ";
        sqlCmd += "from col_cs_instjcic where rowid = ? ";
        setString(1, hCcicTranType);
        setString(2, hCcicChiName);
        setString(3, hCcicBirthday);
        setString(4, hCcicResidentAddr);
        setString(5, hBusiBusinessDate);
        setString(6, hCcicInstFlag);
        setString(7, hCcicInstSMcode);
        setString(8, javaProgram + "_1");
        setRowId(9, hCcicRowid);
        insertTable();
        if (dupRecord.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("insert_col_cs_instjcic_1 duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertColCsInstjcic() throws Exception {
        updFlag = 1;
        sqlCmd = "insert into col_cs_instjcic ";
        sqlCmd += "(crt_date,";
        sqlCmd += "crt_time,";
        sqlCmd += "inst_seqno,";
        sqlCmd += "tran_type,";
        sqlCmd += "p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "id_no,";
        sqlCmd += "id_code,";
        sqlCmd += "chi_name,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "birthday,";
        sqlCmd += "resident_addr,";
        sqlCmd += "update_date,";
        sqlCmd += "inst_flag,";
        sqlCmd += "inst_date,";
        sqlCmd += "inst_s_date,";
        sqlCmd += "inst_e_date,";
        sqlCmd += "inst_s_mcode,";
        sqlCmd += "send_acct_no,";
        sqlCmd += "proc_flag,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "mod_time)";
        sqlCmd += " select ";
        sqlCmd += "to_char(sysdate,'yyyymmdd'),";
        sqlCmd += "to_char(sysdate,'hh24miss'),";
        sqlCmd += "inst_seqno,";
        sqlCmd += "?,";
        sqlCmd += "p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "id_no,";
        sqlCmd += "id_code,";
        sqlCmd += "?,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "inst_s_date,";
        sqlCmd += "inst_e_date,";
        sqlCmd += "?,";
        sqlCmd += "send_acct_no,";
        sqlCmd += "'N',";
        sqlCmd += "?,";
        sqlCmd += "sysdate ";
        sqlCmd += "from col_cs_instbase where rowid = ? ";
        setString(1, hCcicTranType);
        setString(2, hCcicChiName);
        setString(3, hCcicBirthday);
        setString(4, hCcicResidentAddr);
        setString(5, hBusiBusinessDate);
        setString(6, hCcicInstFlag);
        setString(7, hCcicInstDate);
        setString(8, hCcieInstSMcode);
        setString(9, javaProgram);
        setRowId(10, hCcieRowid);
        
        insertTable();
        if (dupRecord.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("insert_col_cs_instjcic duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateColCsInstbase() throws Exception {
        daoTable = "col_cs_instbase";
        updateSQL = "proc_date = ?,";
        updateSQL += " proc_flag = 'X',";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hCcieRowid);
        
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_cs_instbase not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC026 proc = new ColC026();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
