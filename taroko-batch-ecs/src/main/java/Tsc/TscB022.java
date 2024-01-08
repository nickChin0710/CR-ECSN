/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/06/03  V1.00.01    Brian     cca_block_log撈取條件ecs_proc_flag增加空字串          *
*  109-11-13  V1.00.02    tanwei    updated for project coding standard       *
*                                                                             *
******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*CCAS凍結,特指轉取消代行授權明細檔處理程式*/
public class TscB022 extends AccessDAO {
    private final String progname = "CCAS凍結,特指轉取消代行授權明細檔處理程式   109/11/13 V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hElbsLogDatetime = "";
    String hElbsDataKey = "";
    String hElbsDataType = "";
    String hElbsCardNo = "";
    String hElbsAcctType = "";
    String hElbsBlockReason = "";
    String hElbsLogRowid = "";
    String hTempDataType = "";
    String hCardCardNo = "";
    String hCardAcctType = "";
    String hCardAcnoPSeqno = "";
    String hTempBlockReason = "";
    String hCardRowid = "";
    String sqlSt01 = "";
    String hTrahTscCardNo = "";
    String hTempSpecStatus = "";
    String hTempSpecDelDate = "";
    int inti = 0;
    String hTrahSendReason = "";
    String hRcpmBkecBlockCond = "";
    String hRcpmBkecBlockReason = "";
    String hRcpmAutoBlockCond = "";
    String hRcpmAutoBlockReason = "";
    String hTempEcsProcFlag = "";
    String hElbxLogDatetime = "";
    String hElbxLogRowid = "";
    String hElbxRowid = "";

    String str600 = "";
    String tmpstr = "";
    String tmpstr1 = "";
    int totalCnt = 0;
    int nUserpid = 0;
    int nRetcode = 0;
    int procCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            if (args.length != 0) {
                comc.errExit("Usage : TscB022 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            selectPtrBusinday();

            selectRskCommParm();

            deleteEcsLogBlockSpec();
            commitDataBase();
            totalCnt = 0;
            selectCcasLogBlockSpec0();
            showLogMessage("I", "", String.format("累計轉檔 [%d] 筆\n", totalCnt));

            totalCnt = 0;
            selectCcasLogBlockSpec1();
            showLogMessage("I", "", String.format("累計戶凍結/戶特指 [%d] 筆\n", totalCnt));
//            showLogMessage("I", "", String.format("累計凍結 [%d] 筆\n", total_cnt));

            totalCnt = 0;
            selectCcasLogBlockSpec2();
            showLogMessage("I", "", String.format("累計卡特指 [%d] 筆\n", totalCnt));
//            showLogMessage("I", "", String.format("累計特指 [%d] 筆\n", total_cnt));

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
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                : hBusiBusinessDate;

    }

    /***********************************************************************/
    void selectRskCommParm() throws Exception {
        hRcpmBkecBlockCond = "";
        hRcpmBkecBlockReason = "";
        hRcpmAutoBlockCond = "";
        hRcpmAutoBlockReason = "";
        sqlCmd = "select bkec_block_cond,";
        sqlCmd += "bkec_block_reason,";
        sqlCmd += "auto_block_cond,";
        sqlCmd += "auto_block_reason ";
        sqlCmd += " from rsk_comm_parm  ";
        sqlCmd += "where parm_type = 'W_RSKM2250'  ";
        sqlCmd += "  and seq_no    = 10 ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_rsk_comm_parm not found!", "", hCallBatchSeqno);
        }
        hRcpmBkecBlockCond   = getValue("bkec_block_cond");
        hRcpmBkecBlockReason = getValue("bkec_block_reason").replace(",", ""); //table column格式修改故將,移除
        hRcpmAutoBlockCond   = getValue("auto_block_cond");
        hRcpmAutoBlockReason = getValue("auto_block_reason").replace(",", ""); //table column格式修改故將,移除

    }

    /***********************************************************************/
    void deleteEcsLogBlockSpec() throws Exception {
        daoTable = "ecs_log_block_spec";
        deleteTable();
    }

    /***********************************************************************/
    void selectCcasLogBlockSpec0() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.log_date||a.log_time datetime,";
        sqlCmd += "a.data_type||decode(a.data_type,'C',a.card_no,c.acct_type||c.acct_key) h_elbs_data_key,";
        sqlCmd += "a.data_type,";
        sqlCmd += "b.card_no,";
        sqlCmd += "c.acct_type,";
        sqlCmd += "rtrim(a.block_reason1||a.block_reason2||a.block_reason3||a.block_reason4||a.block_reason5) h_elbs_block_reason,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += " from cca_block_log a, crd_card b, act_acno c ";
        sqlCmd += "where a.block_spec_flag ='B' ";
        sqlCmd += "  and a.ecs_proc_flag in ('', 'N') ";
//        sqlCmd += "  and b.card_no       = a.card_no ";
        sqlCmd += "  and b.card_no in (select crd.card_no from crd_card crd , tsc_card t where crd.acno_p_seqno = a.acno_p_seqno and crd.card_no = t.card_no and t.current_code = '0') "; //a.card_no為空, 需要用acno_p_seqno去串, 且必須為悠遊卡正常使用卡
        sqlCmd += "  and c.acno_p_seqno  = b.acno_p_seqno ";
        openCursor();
        while(fetchTable()) {
            hElbsLogDatetime  = getValue("datetime");
            hElbsDataKey      = getValue("h_elbs_data_key");
            hElbsDataType     = getValue("data_type");
            hElbsCardNo       = getValue("card_no");
            hElbsAcctType     = getValue("acct_type");
            hElbsBlockReason  = getValue("h_elbs_block_reason");
            hElbsLogRowid     = getValue("rowid");

            if (selectEcsLogBlockSpec() == 0) {
                insertEcsLogBlockSpec();
                totalCnt++;
            } else {
                if (hElbsLogDatetime.compareTo(hElbxLogDatetime) > 0) {
                    updateEcsLogBlockSpec();
                } else {
                    hElbxLogRowid = hElbsLogRowid;
                }
                updateCcasLogBlockSpec1();
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    int selectEcsLogBlockSpec() throws Exception {
        sqlCmd = "select log_datetime,";
        sqlCmd += "log_rowid,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from ecs_log_block_spec  ";
        sqlCmd += "where data_key = ? ";
        setString(1, hElbsDataKey);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hElbxLogDatetime = getValue("log_datetime");
            hElbxLogRowid    = getValue("log_rowid");
            hElbxRowid        = getValue("rowid");
        } else
            return (0);
        return (1);
    }

    /***********************************************************************/
    void insertEcsLogBlockSpec() throws Exception {
        setValue("log_datetime", hElbsLogDatetime);
        setValue("data_key"    , hElbsDataKey);
        setValue("data_type"   , hElbsDataType);
        setValue("card_no"     , hElbsCardNo);
        setValue("acct_type"   , hElbsAcctType);
        setValue("block_reason", hElbsBlockReason);
        setValue("log_rowid"   , hElbsLogRowid);
        daoTable = "ecs_log_block_spec";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ecs_log_block_spec duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateEcsLogBlockSpec() throws Exception {
        daoTable   = "ecs_log_block_spec";
        updateSQL  = " log_datetime  = ?,";
        updateSQL += " block_reason  = ?,";
        updateSQL += " log_rowid     = ?";
        whereStr   = "where data_key = ? ";
        setString(1, hElbsLogDatetime);
        setString(2, hElbsBlockReason);
        setString(3, hElbsLogRowid);
        setString(4, hElbsDataKey);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ecs_log_block_spec not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateCcasLogBlockSpec1() throws Exception {
        daoTable = "cca_block_log h";
        updateSQL = "ecs_proc_flag = 'W',";
        updateSQL += " ecs_proc_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " check_date = sysdate";
        whereStr = "where rowid   = ? ";
        setRowId(1, hElbxLogRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_cca_block_log h not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectCcasLogBlockSpec1() throws Exception {
        int int1 = 0;

        sqlCmd = "select ";
        sqlCmd += "a.data_type,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "c.acno_p_seqno,";
        sqlCmd += "a.block_reason,";       
        sqlCmd += "rtrim(d.spec_status) h_temp_spec_status,";
        sqlCmd += "decode(rtrim(d.spec_del_date),'','',to_char(to_date(d.spec_del_date,'yyyymmdd')+1 days,'yyyymmdd')) h_temp_spec_del_date,";
        sqlCmd += "a.log_rowid ";
        sqlCmd += "from ecs_log_block_spec a, crd_card b, act_acno c, cca_block_log d ";
        sqlCmd += " where b.card_no = a.card_no ";
        sqlCmd += "   and b.acno_p_seqno = d.acno_p_seqno ";
        sqlCmd += "   and c.acno_p_seqno = b.acno_p_seqno ";
        openCursor();
        while(fetchTable()) {
            hTempDataType    = getValue("data_type");
            hCardCardNo      = getValue("card_no");
            hCardAcctType    = getValue("acct_type");
            hCardAcnoPSeqno = getValue("acno_p_seqno");
            hTempBlockReason = getValue("block_reason");
            hCardRowid        = getValue("log_rowid");

            hTempSpecStatus   = getValue("h_temp_spec_status");
            hTempSpecDelDate = "";
            
            hTrahSendReason = "";
            tmpstr = "";

            /* 戶凍結 */
            if ((hRcpmBkecBlockCond.equals("Y")) && (hTempBlockReason.length() != 0)) {
                hTrahSendReason = "20";
                tmpstr = "";
                for (int int2 = 0; int2 < hTempBlockReason.length() / 2; int2++) {
                    for (int1 = 0; int1 < hRcpmBkecBlockReason.length() / 2; int1++) {
                        if (hRcpmBkecBlockReason.substring(int1 * 2, int1 * 2 + 2)
                                .equals(hTempBlockReason.substring(int2 * 2, int2 * 2 + 2)))
                            break;
                    }
                    if (int1 < hRcpmBkecBlockReason.length() / 2)
                        continue;
                    tmpstr1 = String.format("%2.2s", hTempBlockReason.substring(int2 * 2));
                    tmpstr += tmpstr1;
                }
            }
            /* 戶特指 */
            if (tmpstr.length() == 0) {
                hTempSpecDelDate = getValue("h_temp_spec_del_date");
                hTrahSendReason = "30";
                tmpstr = hTempSpecStatus;
                if ((hRcpmAutoBlockCond.equals("Y")) && (tmpstr.length() != 0))
                    for (int1 = 0; int1 < hRcpmAutoBlockReason.length() / 2; int1++)
                        if (hRcpmAutoBlockReason.substring(int1 * 2, int1 * 2 + 2).equals(hTempSpecStatus)) {
                            tmpstr = "";
                            break;
                        }
            }
            
            procCnt = 0;
            selectTscCard1(tmpstr.length());
            hTempEcsProcFlag = "X";
            if (procCnt == 1)
                hTempEcsProcFlag = "Y";
            if (procCnt == 2)
                hTempEcsProcFlag = "Z";

            updateCcasLogBlockSpec();
            
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectTscCard1(int authFlag) throws Exception {
        int nseq = 1;
        sqlCmd = "SELECT a.tsc_card_no ";
        sqlCmd += " FROM crd_card b,tsc_card a ";
        sqlCmd += "WHERE a.card_no       = b.card_no ";
        sqlCmd += "  AND sysdate-to_date(a.crt_date,'yyyymmdd')>=2 ";
        sqlCmd += "  AND a.current_code != '6'";

        if (hTempDataType.equals("C")) {
            sqlCmd += "AND b.card_no = ?";
            setString(nseq++, hCardCardNo);
        } else {
            sqlCmd += "AND b.acct_type = ? AND b.acno_p_seqno = ?";
            setString(nseq++, hCardAcctType);
            setString(nseq++, hCardAcnoPSeqno);
        }
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTrahTscCardNo = getValue("tsc_card_no", i);

            totalCnt++;
            procCnt = 1;

            if (authFlag == 0)
                updateTscRmActauth();
            else {
                if (hTrahSendReason.equals("20"))
                    insertTscRmActauth();
                else if (hTrahSendReason.equals("30")) {
                    if (insertTscRmActauth() != 0)
                        updateTscRmActauth(1);
                }
            }
        }

    }

    /***********************************************************************/
    void updateTscRmActauth() throws Exception {
        daoTable   = "tsc_rm_actauth";
        updateSQL  = " restore_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ? ";
        whereStr   = "where tsc_card_no = ?  ";
        whereStr  += "  and risk_class  = '57'  ";
        whereStr  += "  and send_reason in ('20', '30') ";
        setString(1, javaProgram);
        setString(2, hTrahTscCardNo);
        // setString(3, h_trah_send_reason);
        updateTable();
        if (notFound.equals("Y")) {
            procCnt = 2;
        }
    }
    
    /***********************************************************************/
    void updateTscRmActauth(int inti) throws Exception {
        daoTable   = "tsc_rm_actauth";
        updateSQL  = " restore_date = decode(cast(? as int), 0, to_char(sysdate,'yyyymmdd'), ?),";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ?";
        whereStr   = "where tsc_card_no = ?  ";
        whereStr  += "  and risk_class  = '57'  ";
        whereStr  += "  and send_reason = ? ";
        setInt(1, inti);
        setString(2, hTempSpecDelDate);
        setString(3, javaProgram);
        setString(4, hTrahTscCardNo);
        setString(5, hTrahSendReason);
        updateTable();
        if (notFound.equals("Y")) {
            procCnt = 2;
        }
    }

    /***********************************************************************/
    int insertTscRmActauth() throws Exception {
        sqlCmd = "insert into tsc_rm_actauth ";
        sqlCmd += "(send_reason,";
        sqlCmd += "risk_class,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "remove_date,";
        sqlCmd += "restore_date,";
        sqlCmd += "card_no,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "new_end_date,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "'57',";
        sqlCmd += "b.tsc_card_no,";
        sqlCmd += "to_char(sysdate,'yyyymmdd'),";
        sqlCmd += "?,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "b.new_end_date,";
        sqlCmd += "sysdate,";
        sqlCmd += "? ";
        sqlCmd += "from crd_card a,tsc_card b where a.card_no  = b.card_no and b.tsc_card_no = ? ";
        setString(1, hTrahSendReason);
        setString(2, hTempSpecDelDate);
        setString(3, javaProgram);
        setString(4, hTrahTscCardNo);
        insertTable();
        sqlCmd = ""; /* initail */
        if (dupRecord.equals("Y")) {
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateCcasLogBlockSpec() throws Exception {
        daoTable   = "cca_block_log";
        updateSQL  = "ecs_proc_flag = decode(cast(? as varchar(10)), 'X',";
        updateSQL += "decode(check_date, null, 'N',";
        updateSQL += " decode(sign(sysdate-check_date-(2*24*60*60)), -1, 'N', 'X')), ?),";
        updateSQL += " ecs_proc_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " check_date    = sysdate";
        whereStr   = "where rowid  = ? ";
        setString(1, hTempEcsProcFlag);
        setString(2, hTempEcsProcFlag);
        setRowId(3, hCardRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_cca_block_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectCcasLogBlockSpec2() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "data_type,";
        sqlCmd += "card_no,";
        sqlCmd += "rtrim(spec_status) h_temp_spec_status,";
        sqlCmd += "decode(rtrim(spec_del_date),'','',to_char(to_date(spec_del_date,'yyyymmdd')+1 days,'yyyymmdd')) h_temp_spec_del_date,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from cca_block_log a ";
        sqlCmd += "where block_spec_flag ='S' ";
        sqlCmd += "and ecs_proc_flag in ('', 'N') ";
        openCursor();
        while(fetchTable()) {
            hTempDataType = getValue("data_type");
            hCardCardNo = getValue("card_no");
            hTempSpecStatus = getValue("h_temp_spec_status");
            hTempSpecDelDate = getValue("h_temp_spec_del_date");
            hCardRowid = getValue("rowid");

            hTrahSendReason = "30";
            tmpstr = hTempSpecStatus;
            if ((hRcpmAutoBlockCond.equals("Y")) && (tmpstr.length() != 0))
                for (int int1 = 0; int1 < hRcpmAutoBlockReason.length() / 2; int1++)
                    if (hRcpmAutoBlockReason.substring(int1 * 2, int1 * 2 + 2).equals(hTempSpecStatus)) {
                        tmpstr = "";
                        break;
                    }
            procCnt = 0;
            selectTscCard2(tmpstr.length());
            hTempEcsProcFlag = "X";
            if (procCnt == 1)
                hTempEcsProcFlag = "Y";
            updateCcasLogBlockSpec();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectTscCard2(int authFlag) throws Exception {

        sqlCmd = "select ";
        sqlCmd += "a.tsc_card_no ";
        sqlCmd += " from crd_card b,tsc_card a ";
        sqlCmd += "where a.card_no       = b.card_no ";
        sqlCmd += "  and sysdate-to_date(a.crt_date,'yyyymmdd')>= 2 ";
        sqlCmd += "  and a.current_code != '6' ";
        sqlCmd += "  and b.card_no       = ? ";
        setString(1, hCardCardNo);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTrahTscCardNo = getValue("tsc_card_no", i);

            totalCnt++;
            procCnt = 1;

            if (authFlag == 0)
                updateTscRmActauth(0);
            else {
                if (insertTscRmActauth() != 0)
                    updateTscRmActauth(1);
            }
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscB022 proc = new TscB022();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
