/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  105/07/11  V1.01.00  Lai         RECS-1050113-002 一卡通 intial            *
*  105/12/01  V1.02.00  Lai         alter oppost_date                         *
*  106/06/01  V1.03.00  Edson       Transfer to JAVA                          *
*  107/01/29  V1.03.01  Alice       BECS-1070125-009 退鎖卡資料不報送黑名單   *
*  107/09/26  V1.04.01  David FU    ECS-1070125-009(JAVA)                     *
*  109-12-14  V1.04.02    tanwei      updated for project coding standard     *
*  112-05-18  V1.04.03  Alex        出黑名單規則變更                                                                               *
*  112-05-30  V1.04.04  Alex        欄位修正 ressiue_date > reissue_date         *
*  112-10-03  V1.04.05  Wilson      讀取資料邏輯調整                                                                                     *
*  112-10-04  V1.04.06  Wilson      增加讀取凍結碼38                                                                                     *
******************************************************************************/

package Ips;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*黑名單資料處理程式*/
public class IpsB003 extends AccessDAO {
    private String progname = "黑名單資料處理程式  112/10/04 V1.04.06";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hB2i3CrtDate = "";
    String hB2i3CrtTime = "";
    String hIardIpsCardNo = "";
    String hIardCardNo = "";
    String hIardCurrentCode = "";
    String hCardCurrentCode = "";
    String hCardOppostDate = "";
    String hCardOppostReason = "";
    String hCardReissueDate = "";
    String hCardReissueReason = "";
    String hCardNewEndDate = "";
    String hIardNewEndDate = "";
    String hIardOppostDate = "";
    String hIardRowid = "";
    String hB2i3FromMark = "";
    String hB2i3FromRsn = "";
    String hIpscStop1Cond = "";
    int hIpscStop1Days = 0;
    String hIpscStop2Cond = "";
    int hIpscStop2Days = 0;
    String hIpscStop3Cond = "";
    int hIpscStop3Days = 0;
    String dateCurr = "";
    String addDays = "";
    String hTempDate = "";
    String newStopDate = "";
    int totCnt = 0;
    int insertCnt = 0;

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
                comc.errExit("Usage : IpsB003 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            if (args.length == 1)
                if (args[0].length() == 8)
                    hBusiBusinessDate = args[0];

            selectPtrBusinday();
//            int rtn = selectIpsCommParm();
//            if (rtn == 0)
//                selectIpsCard();
            selectIpsCard();

            showLogMessage("I", "", String.format("Process records = [%d][%d]\n", totCnt, insertCnt));

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
        hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? sysDate : hBusiBusinessDate;
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_b2i3_crt_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_b2i3_crt_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hB2i3CrtDate = getValue("h_b2i3_crt_date");
            hB2i3CrtTime = getValue("h_b2i3_crt_time");
        }

    }

    /***********************************************************************/
    int selectIpsCommParm() throws Exception {
        hIpscStop1Cond = "";
        hIpscStop1Days = 0;
        hIpscStop2Cond = "";
        hIpscStop2Days = 0;
        hIpscStop3Cond = "";
        hIpscStop3Days = 0;
        sqlCmd = "select stop1_cond,";
        sqlCmd += "stop1_days,";
        sqlCmd += "stop2_cond,";
        sqlCmd += "stop2_days,";
        sqlCmd += "stop3_cond,";
        sqlCmd += "stop3_days ";
        sqlCmd += " from ips_comm_parm  ";
        sqlCmd += "where parm_type ='BLACK_LIST' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIpscStop1Cond = getValue("stop1_cond");
            hIpscStop1Days = getValueInt("stop1_days");
            hIpscStop2Cond = getValue("stop2_cond");
            hIpscStop2Days = getValueInt("stop2_days");
            hIpscStop3Cond = getValue("stop3_cond");
            hIpscStop3Days = getValueInt("stop3_days");
        } else {
            return 1403;
        }
        return 0;
    }

    /***********************************************************************/
    void selectIpsCard() throws Exception {
    	//停卡&凍結碼38
        sqlCmd = "select ";
        sqlCmd += "a.ips_card_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.current_code as h_iard_current_code,";
        sqlCmd += "b.current_code as h_card_current_code,";
        sqlCmd += "b.oppost_date,";
        sqlCmd += "b.oppost_reason,";
        sqlCmd += "b.reissue_date,";
        sqlCmd += "b.reissue_reason,";
        sqlCmd += "b.new_end_date,";
        sqlCmd += "a.new_end_date,";
        sqlCmd += "a.ips_oppost_date,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from crd_card b join ips_card a on b.card_no = a.card_no ";
        sqlCmd += "     join cca_card_acct c on b.acno_p_seqno = c.acno_p_seqno ";
        sqlCmd += "where decode(a.blacklt_flag,'','N',a.blacklt_flag) = 'N' ";
        sqlCmd += "and a.blacklt_date  = '' ";        
        sqlCmd += "and a.lock_date  = '' ";
        sqlCmd += "and a.return_date  = '' ";                
        sqlCmd += "and ( (b.current_code not in ('0','2')) ";
        sqlCmd += "    or (c.block_reason1 = '38') or (c.block_reason2 = '38') or (c.block_reason3 = '38') or (c.block_reason4 = '38') or (c.block_reason5 = '38') ) ";
        sqlCmd += "union ";
        //覆審凍結碼
        sqlCmd += "select ";
        sqlCmd += "a.ips_card_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.current_code as h_iard_current_code,";
        sqlCmd += "b.current_code as h_card_current_code,";
        sqlCmd += "b.oppost_date,";
        sqlCmd += "b.oppost_reason,";
        sqlCmd += "b.reissue_date,";
        sqlCmd += "b.reissue_reason,";
        sqlCmd += "b.new_end_date,";
        sqlCmd += "a.new_end_date,";
        sqlCmd += "a.ips_oppost_date,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from crd_card b join ips_card a on b.card_no = a.card_no join cca_card_acct c on b.acno_p_seqno = c.acno_p_seqno ";
        sqlCmd += "where decode(a.blacklt_flag,'','N',a.blacklt_flag) = 'N' ";
        sqlCmd += "and a.blacklt_date = '' ";        
        sqlCmd += "and a.lock_date = '' ";
        sqlCmd += "and a.return_date = '' "; 
        sqlCmd += "and ( (c.block_reason1 in (select wf_id from ptr_sys_idtab where wf_type = 'REVIEW_BLOCK')) ";
        sqlCmd += "   or (c.block_reason2 in (select wf_id from ptr_sys_idtab where wf_type = 'REVIEW_BLOCK')) ";
        sqlCmd += "   or (c.block_reason3 in (select wf_id from ptr_sys_idtab where wf_type = 'REVIEW_BLOCK')) ";
        sqlCmd += "   or (c.block_reason4 in (select wf_id from ptr_sys_idtab where wf_type = 'REVIEW_BLOCK')) ";
        sqlCmd += "   or (c.block_reason5 in (select wf_id from ptr_sys_idtab where wf_type = 'REVIEW_BLOCK')) ) ";
        sqlCmd += "union ";
        //毀損補發、續卡
        sqlCmd += "select ";
        sqlCmd += "a.ips_card_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.current_code as h_iard_current_code,";
        sqlCmd += "b.current_code as h_card_current_code,";
        sqlCmd += "b.oppost_date,";
        sqlCmd += "b.oppost_reason,";
        sqlCmd += "b.reissue_date,";
        sqlCmd += "b.reissue_reason,";
        sqlCmd += "b.new_end_date,";
        sqlCmd += "a.new_end_date,";
        sqlCmd += "a.ips_oppost_date,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from crd_card b join ips_card a on b.card_no = a.card_no ";
        sqlCmd += "where decode(a.blacklt_flag,'','N',a.blacklt_flag) = 'N' ";
        sqlCmd += "and a.blacklt_date  = '' ";        
        sqlCmd += "and a.lock_date  = '' ";
        sqlCmd += "and a.return_date  = '' ";                
        sqlCmd += "and b.current_code ='0' ";
        sqlCmd += "and ((b.reissue_date <> '' and b.reissue_reason = '2' and a.current_code = '6')  ";	//--毀補
        sqlCmd += " or ";
        sqlCmd += " (b.change_date <> '' and a.current_code = '7')) ";	//--續卡
        sqlCmd += "union ";
        //逾期&超額
        sqlCmd += "select ";        
        sqlCmd += "a.ips_card_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.current_code as h_iard_current_code,";
        sqlCmd += "b.current_code as h_card_current_code,";
        sqlCmd += "b.oppost_date,";
        sqlCmd += "b.oppost_reason,";
        sqlCmd += "b.reissue_date,";
        sqlCmd += "b.reissue_reason,";
        sqlCmd += "b.new_end_date,";
        sqlCmd += "a.new_end_date,";
        sqlCmd += "a.ips_oppost_date,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from crd_card b join ips_card a on b.card_no = a.card_no join act_acno c on b.acno_p_seqno = c.acno_p_seqno ";
        sqlCmd += "     join act_acct d on b.p_seqno = d.p_seqno ";
        sqlCmd += "where decode(a.blacklt_flag,'','N',a.blacklt_flag) = 'N' ";
        sqlCmd += "and a.blacklt_date  = '' ";        
        sqlCmd += "and a.lock_date  = '' ";
        sqlCmd += "and a.return_date  = '' "; 
        sqlCmd += "and ((c.int_rate_mcode > 1) or (c.int_rate_mcode = 1 and d.acct_jrnl_bal > c.line_of_credit_amt)) "; 
        
        openCursor();
        while (fetchTable()) {
            hIardIpsCardNo = getValue("ips_card_no");
            hIardCardNo = getValue("card_no");
            hIardCurrentCode = getValue("h_iard_current_code");
            hCardCurrentCode = getValue("h_card_current_code");
            hCardOppostDate = getValue("oppost_date");
            hCardOppostReason = getValue("oppost_reason");
            hCardReissueDate = getValue("reissue_date");
            hCardReissueReason = getValue("reissue_reason");
            hCardNewEndDate = getValue("new_end_date");
            hIardNewEndDate = getValue("new_end_date");
            hIardOppostDate = getValue("ips_oppost_date");
            hIardRowid = getValue("rowid");

            hB2i3FromMark = "2";
            hB2i3FromRsn = "";

            totCnt++;
            insertIpsB2i003Log();
            updateIpsCard();
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateIpsCard() throws Exception {
        daoTable   = "ips_card";
        updateSQL  = " blacklt_flag = 'Y' ,";
        updateSQL += " blacklt_date = ? ,";
        updateSQL += " blacklt_from = '3' ,";
        updateSQL += " mod_pgm      = ? ,";
        updateSQL += " mod_time     = sysdate";
        whereStr   = "where rowid   = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hIardRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int insertIpsB2i003Log() throws Exception {

        insertCnt++;

        setValue("crt_date"   , hB2i3CrtDate);
        setValue("crt_time"   , hB2i3CrtTime);
        setValue("ips_card_no", hIardIpsCardNo);
        setValue("card_no"    , hIardCardNo);
        setValue("from_mark"  , hB2i3FromMark);
        setValue("from_rsn"   , hB2i3FromRsn);
        setValue("proc_flag"  , "N");
        setValue("mod_pgm"    , javaProgram);
        setValue("mod_time"   , sysDate + sysTime);
        daoTable = "ips_b2i003_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    String addDays(String dateCurr, int addDays) throws Exception {
        String hTempDate = "";

        hTempDate = "";
        sqlCmd = "select to_char((to_date(? , 'yyyymmdd') + ? days),'yyyymmdd') h_temp_date ";
        sqlCmd += " from dual ";
        setString(1, dateCurr);
        setInt(2, addDays);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempDate = getValue("h_temp_date");
        }
        return hTempDate;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsB003 proc = new IpsB003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
