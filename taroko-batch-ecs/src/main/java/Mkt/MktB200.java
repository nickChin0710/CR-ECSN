/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/01/01  V1.00.00    Edson     program initial                           *
 *  107/01/22  V1.00.01    Brian     error correction                          *
 *  109-12-04  V1.00.02  tanwei      updated for project coding standard       *
 ******************************************************************************/

package Mkt;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*員工VD卡招攬獎勵紀錄處理程式*/
public class MktB200 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private String progname = "員工VD卡招攬獎勵紀錄處理程式  109/12/04 V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTempThisAcctMonth = "";
    String hMifdProgramCode = "";
    String hMifdExclObjFlag = "";
    String hMifdExclPurFlag = "";
    String hMifdItemEnameBl = "";
    String hMifdItemEnameCa = "";
    String hMifdItemEnameIt = "";
    String hMifdItemEnameId = "";
    String hMifdItemEnameOt = "";
    String hMifdItemEnameAo = "";
    String hMifdApplyDateS = "";
    String hMifdApplyDateE = "";
    String hDbilReferenceNo = "";
    double hDbilDestinationAmt = 0;
    String hDccdCardNo = "";
    String hDccdIssueDate = "";
    String hEmplId = "";
    String hEmplEmployNo = "";

    int totalCnt = 0;
    String tmpstr = "";

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
                comc.errExit("Usage : MktB200", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            // comcr.h_call_batch_seqno = h_call_batch_seqno;
            // comcr.h_call_r_program_code = javaProgram;
            //
            // comcr.callbatch(0, 0, 0);

            if (args.length == 1)
                hBusiBusinessDate = args[0];

            selectPtrBusinday();

            if (!hBusiBusinessDate.substring(6).equals("01")) {
                exceptExit = 0;
                comcr.errRtn(String.format("本程式只在每月一日執行!"), "", hCallBatchSeqno);
            }
            showLogMessage("I", "", String.format("Processing acct_month[%s]", hTempThisAcctMonth));

            selectMktIntrFund();

            // ==============================================
            // 固定要做的
            // comcr.callbatch(1, 0, 0);
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
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,?) h_busi_business_date,";
        sqlCmd += "  to_char(add_months(to_date(decode(cast(? as varchar(8)),'',business_date,?),'yyyymmdd'),-1),'yyyymm') h_temp_this_acct_month ";
        sqlCmd += "  from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hTempThisAcctMonth = getValue("h_temp_this_acct_month");
        }

    }

    /***********************************************************************/
    void selectMktIntrFund() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " program_code,";
        sqlCmd += " excl_obj_flag,";
        sqlCmd += " excl_pur_flag,";
        sqlCmd += " item_ename_bl,";
        sqlCmd += " item_ename_ca,";
        sqlCmd += " item_ename_it,";
        sqlCmd += " item_ename_id,";
        sqlCmd += " item_ename_ot,";
        sqlCmd += " item_ename_ao,";
        sqlCmd += " apply_date_s,";
        sqlCmd += " decode(apply_date_e,'','30001231',apply_date_e) h_mifd_apply_date_e ";
        sqlCmd += "from mkt_intr_fund ";
        sqlCmd += "where apr_flag = 'Y' and ? <= to_char(add_months(to_date(decode(apply_date_e,'','30001231',apply_date_e),'yyyymmdd'),12),'yyyymmdd') ";
        setString(1, hBusiBusinessDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hMifdProgramCode = getValue("program_code");
            hMifdExclObjFlag = getValue("excl_obj_flag");
            hMifdExclPurFlag = getValue("excl_pur_flag");
            hMifdItemEnameBl = getValue("item_ename_bl");
            hMifdItemEnameCa = getValue("item_ename_ca");
            hMifdItemEnameIt = getValue("item_ename_it");
            hMifdItemEnameId = getValue("item_ename_id");
            hMifdItemEnameOt = getValue("item_ename_ot");
            hMifdItemEnameAo = getValue("item_ename_ao");
            hMifdApplyDateS = getValue("apply_date_s");
            hMifdApplyDateE = getValue("h_mifd_apply_date_e");

            showLogMessage("I", "", String.format("Fund_code=[%s] Processing....", hMifdProgramCode));
            totalCnt = 0;
            selectDbcCard();
            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void selectDbcCard() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " c.reference_no,";
        sqlCmd += " decode(c.txn_code,'06',-1,'25',-1,'27',-1,'28',-1,'29',-1,1)*c.dest_amt h_dbil_destination_amt,";
        sqlCmd += " a.card_no,";
        sqlCmd += " a.issue_date,";
        sqlCmd += " b.id,";
        sqlCmd += " b.employ_no ";
        sqlCmd += "from dbc_card a,crd_employee b,dbb_bill c ,crd_idno i";
        sqlCmd += "where a.promote_emp_no = b.employ_no ";
        sqlCmd += "  and a.id_p_seqno = i.id_p_seqno "; // find a.id in crd_idno
        sqlCmd += "  and b.status_id in ('1','7') ";
        sqlCmd += "  and (    ? = 'N' ";
        sqlCmd += "       or (     ? = 'Y' ";
        sqlCmd += "           and (a.promote_emp_no,decode(a.source_code,'','y',a.source_code),i.id_no) not in "; // a.id
        sqlCmd += "              (select decode(data_code1,'','x',data_code1), ";
        sqlCmd += "                      decode(data_code2,'','x',data_code2), ";
        sqlCmd += "                      decode(data_code3,'','x',data_code3) ";
        sqlCmd += "                      from mkt_intr_data ";
        sqlCmd += "                     where program_code = ? ";
        sqlCmd += "                       and data_type = '1'))) ";
        sqlCmd += "  and (    ? = 'N' ";
        sqlCmd += "       or (     ? = 'Y' ";
        sqlCmd += "           and (decode(a.group_code, '', 'y', a.group_code),decode(c.mcht_no, '', 'y', c.mcht_no),c.bill_type) not in ";
        sqlCmd += "              (select decode(data_code1,'','x',data_code1), ";
        sqlCmd += "                      decode(data_code2,'','x',data_code2), ";
        sqlCmd += "                      decode(data_code3,'','x',data_code3) ";
        sqlCmd += "                      from mkt_intr_data ";
        sqlCmd += "                     where program_code = ? ";
        sqlCmd += "                       and data_type = '2'))) ";
        sqlCmd += "  and a.card_no = c.card_no ";
        sqlCmd += "  and c.acct_month between substr(?,1,6) and substr(?,1,6) ";
        sqlCmd += "  and decode(c.acct_code, '', 'x', c.acct_code) in (decode(?,'Y','BL','xx'), ";
        sqlCmd += "                                               decode(?,'Y','CA','xx'), ";
        sqlCmd += "                                               decode(?,'Y','IT','xx'), ";
        sqlCmd += "                                               decode(?,'Y','ID','xx'), ";
        sqlCmd += "                                               decode(?,'Y','OT','xx'), ";
        sqlCmd += "                                               decode(?,'Y','AO','xx')) ";
        sqlCmd += "  and c.acct_month = ? ";
        setString(1, hMifdExclObjFlag);
        setString(2, hMifdExclObjFlag);
        setString(3, hMifdProgramCode);
        setString(4, hMifdExclPurFlag);
        setString(5, hMifdExclPurFlag);
        setString(6, hMifdProgramCode);
        setString(7, hMifdApplyDateS);
        setString(8, hMifdApplyDateE);
        setString(9, hMifdItemEnameBl);
        setString(10, hMifdItemEnameCa);
        setString(11, hMifdItemEnameIt);
        setString(12, hMifdItemEnameId);
        setString(13, hMifdItemEnameOt);
        setString(14, hMifdItemEnameAo);
        setString(15, hTempThisAcctMonth);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hDbilReferenceNo = getValue("reference_no", i);
            hDbilDestinationAmt = getValueDouble("h_dbil_destination_amt", i);
            hDccdCardNo = getValue("card_no", i);
            hDccdIssueDate = getValue("issue_date", i);
            hEmplId = getValue("id", i);
            hEmplEmployNo = getValue("employ_no", i);

            totalCnt++;
            if ((totalCnt % 10000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
                commitDataBase();
            }
            insertMktIntrLog();
        }
    }

    /***********************************************************************/
    void insertMktIntrLog() throws Exception {
        setValue("program_code", hMifdProgramCode);
        setValue("acct_month", hTempThisAcctMonth);
        setValue("vd_flag", "Y");
        setValue("ref_no", hDbilReferenceNo);
        setValueDouble("dest_amt", hDbilDestinationAmt);
        setValue("card_no", hDccdCardNo);
        setValue("issue_date", hDccdIssueDate);
        setValue("employ_id", hEmplId);
        setValue("employ_no", hEmplEmployNo);
        setValue("crt_date", hBusiBusinessDate);
        setValue("crt_time", sysTime);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "mkt_intr_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_mkt_intr_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktB200 proc = new MktB200();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
