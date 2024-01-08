/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  107/02/23  V1.00.01    Brian     error correction                          *
 *  108/05/21  V1.00.02    David     新系統不用寫onbat                         *
 *  109/11/16  V1.00.03    shiyuqi   updated for project coding standard       *
 *  111/10/25  V1.00.04    Simon     sync codes with mega                      *
 *  112/09/21  V1.00.05    Simon     新增寫入門市店號、門市會計日              *
 *  112/09/22  V1.00.06    Simon     新增寫入繳款編號(payment_no)              *
 *  112/09/29  V1.00.07    Simon     新增寫入代收單位別(def_branch=act_b003r1.bank_no)*
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*批次繳款篩選已放行資料程式*/
public class ActE002 extends AccessDAO {

    public boolean debugMode = false;

    private String progname = "批次繳款篩選已放行資料程式   112/09/29 "
                            + "V1.00.07 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActE002";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String ecsServer = "";

    String hApbtBatchNo = "";
    String hApbtRowid = "";
    String hApdlPSeqno = "";
    String hApdlAcnoPSeqno = "";
    String hApdlAcctType = "";
    String hApdlAcctKey = "";
    String hApdlIdPSeqno = "";
    String hApdlId = "";
    String hApdlIdCode = "";
    String hApdlBatchNo = "";
    String hApdlSerialNo = "";
    String hApdlPayCardNo = "";
    String hApdlPaymentNo = "";
    String hApdlCurrCode = "";
    double hApdlPayAmt = 0;
    double hApdlDcPayAmt = 0;
    String hApdlPayDate = "";
    String hApdlPaymentType = "";
    String hApdlDebitItem = "";
    String hApdlDebtKey = "";
    String hApdlUpdateUser = "";
    String hApdlUpdateDate = "";
    String hApdlUpdateTime = "";
    String hApdlAcctBankNo = "";
    String hApdlCollStoreNo = "";
    String hApdlRowid = "";
    String hApdlModUser = "";
    // String h_apdl_mod_ws = "";
    String hUserUsrDeptno = "";
    String hAdclVouchJobCode = "";
    int hCnt = 0;
    String hPccdGlCode = "";
    int onbatCnt = 0;
    int totalCnt = 0;
    int classcodeCnt = 0;

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
                comc.errExit("Usage : ActE002", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hModUser = comc.commGetUserID();
            hApdlModUser = hModUser;

            selectActPayBatch();

            //if (onbat_cnt > 0)
                //insert_onbat_0();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void selectActPayBatch() throws Exception {
        sqlCmd = "select ";
        sqlCmd += " batch_no,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_pay_batch ";
        sqlCmd += " where 1=1 ";
        sqlCmd += "   and confirm_user != '' ";
        sqlCmd += "   and confirm_date != '' ";
        sqlCmd += "   and confirm_time != '' ";
        sqlCmd += "   and batch_tot_cnt !=0 ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hApbtBatchNo = getValue("batch_no");
            hApbtRowid = getValue("rowid");

            showLogMessage("I", "", String.format("Process batch no[%s]", hApbtBatchNo));
            if (checkActPayDetail() != 0) {
                showLogMessage("I", "", String.format("   Duplicate data error, Skip..."));
                continue;
            }

            totalCnt = classcodeCnt = 0;
            selectActPayDetail();
            showLogMessage("I", "", String.format("Total process record[%d] onbat累計[%d] classcode[%d]", totalCnt,
                    onbatCnt, classcodeCnt));

            deleteActPayBatch();

        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    int checkActPayDetail() throws Exception {
        sqlCmd = "select 1 cnt";
        sqlCmd += "  from act_pay_detail  ";
        sqlCmd += " where batch_no   = ?  ";
        sqlCmd += "   and proc_mark  = ''  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hApbtBatchNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCnt = getValueInt("cnt");
        } else
            return (0);
        return (1);
    }

    /***********************************************************************/
    void selectActPayDetail() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " p_seqno,";
        sqlCmd += " acno_p_seqno,";
        sqlCmd += " acct_type,";
        sqlCmd += " UF_ACNO_KEY(p_seqno) acct_key,";
        sqlCmd += " id_p_seqno,";
        sqlCmd += " UF_IDNO_ID(id_p_seqno) id,";
        // sqlCmd += " id_code,"; //notfound
        sqlCmd += " batch_no,";
        sqlCmd += " serial_no,";
        sqlCmd += " pay_card_no,";
        sqlCmd += " payment_no,";
        sqlCmd += " curr_code,";
        sqlCmd += " pay_amt,";
        sqlCmd += " dc_pay_amt,";
        sqlCmd += " pay_date,";
        sqlCmd += " payment_type,";
        sqlCmd += " debit_item,";
        sqlCmd += " debt_key,";
        sqlCmd += " crt_user,";
        sqlCmd += " crt_date,";
        sqlCmd += " crt_time,";
        sqlCmd += " def_branch,";
        sqlCmd += " collection_store_no,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_pay_detail ";
        sqlCmd += " where batch_no = ? ";
        setString(1, hApbtBatchNo);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hApdlPSeqno = getValue("p_seqno", i);
            hApdlAcnoPSeqno = getValue("acno_p_seqno", i);
            hApdlAcctType = getValue("acct_type", i);
            hApdlAcctKey = getValue("acct_key", i);
            hApdlIdPSeqno = getValue("id_p_seqno", i);
            hApdlId = getValue("id", i);
            // h_apdl_id_code = getValue ("id_code" , i); //notfound
            hApdlBatchNo = getValue("batch_no", i);
            hApdlSerialNo = getValue("serial_no", i);
            hApdlPayCardNo = getValue("pay_card_no", i);
            hApdlPaymentNo = getValue("payment_no", i);
            hApdlCurrCode = getValue("curr_code", i);
            hApdlPayAmt = getValueDouble("pay_amt", i);
            hApdlDcPayAmt = getValueDouble("dc_pay_amt", i);
            hApdlPayDate = getValue("pay_date", i);
            hApdlPaymentType = getValue("payment_type", i);
            hApdlDebitItem = getValue("debit_item", i);
            hApdlDebtKey = getValue("debt_key", i);
            hApdlUpdateUser = getValue("crt_user", i);
            hApdlUpdateDate = getValue("crt_date", i);
            hApdlUpdateTime = getValue("crt_time", i);
            hApdlAcctBankNo = getValue("def_branch", i);
            hApdlCollStoreNo = getValue("collection_store_no", i);
            hApdlRowid = getValue("rowid", i);

            totalCnt++;
            insertActPayHst();

            hUserUsrDeptno = "";
            hPccdGlCode = "";
            hAdclVouchJobCode = "";

            if ((hApdlBatchNo.substring(8, 12).equals("0000")) || (hApdlBatchNo.substring(8, 12).equals("0700")))
                selectPtrDeptCode();

            insertActDebtCancel();

//            if ((!h_apdl_batch_no.substring(8, 12).equals("9001"))
//                    && (!h_apdl_batch_no.substring(8, 12).equals("9008")))
//                insert_onbat();

            deleteActPayDetail();
        }
    }

    /***********************************************************************/
    void insertActPayHst() throws Exception {
        setValue("batch_no", hApdlBatchNo);
        setValue("serial_no", hApdlSerialNo);
        setValue("p_seqno", hApdlPSeqno);
        setValue("acno_p_seqno", hApdlAcnoPSeqno);
        setValue("acct_type", hApdlAcctType);
        setValue("id_p_seqno", hApdlIdPSeqno);
        setValue("pay_card_no", hApdlPayCardNo);
        setValue("payment_no", hApdlPaymentNo);
        setValue("curr_code", hApdlCurrCode);
        setValueDouble("pay_amt", hApdlPayAmt);
        setValueDouble("dc_pay_amt", hApdlDcPayAmt);
        setValue("pay_date", hApdlPayDate);
        setValue("payment_type", hApdlPaymentType);
        setValue("debit_item", hApdlDebitItem);
        setValue("debt_key", hApdlDebtKey);
        setValue("update_user", hApdlUpdateUser);
        setValue("update_date", hApdlUpdateDate);
        setValue("update_time", hApdlUpdateTime);
        setValue("def_branch", hApdlAcctBankNo);
        setValue("collection_store_no", hApdlCollStoreNo);
        setValue("mod_user", hApdlModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", "ActE002");
        daoTable = "act_pay_hst";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_hst duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectPtrDeptCode() throws Exception {
        String tmpstr = "";
        sqlCmd = "select a.gl_code,";
        sqlCmd += " b.usr_deptno ";
        sqlCmd += "  from PTR_DEPT_CODE a,sec_user b  ";
        sqlCmd += " where a.dept_code = b.usr_deptno  ";
        sqlCmd += "   and b.usr_id  = ? ";
        setString(1, hApdlUpdateUser);
        selectTable();
        if (notFound.equals("Y")) {
          //comcr.err_rtn("select_ptr_dept_code not found!", "", h_call_batch_seqno); 
          /*modified on 2019/07/22, if notfound set default value */ 
            showLogMessage("I", "", String.format("select sec_user & ptr_dept_code not found! act_pay_detail.crt_user[%s]", 
                    hApdlUpdateUser));
            hPccdGlCode    = "1";
            hUserUsrDeptno = "OP";
            showLogMessage("I", "", String.format("Let ptr_dept_code.gl_code be [%s], ptr_dept_code.dept_code be [%s]", 
                    hPccdGlCode,hUserUsrDeptno));
        }
        else {
            hPccdGlCode = getValue("gl_code");
            hUserUsrDeptno = getValue("usr_deptno");
        }  
        
      //h_pccd_gl_code = getValue("gl_code");
      //h_user_usr_deptno = getValue("usr_deptno");

        tmpstr = String.format("0%1.1s", hPccdGlCode);
        hAdclVouchJobCode = tmpstr;
        classcodeCnt++;
    }

    /***********************************************************************/
    void insertActDebtCancel() throws Exception {
        setValue("batch_no", hApdlBatchNo);
        setValue("serial_no", hApdlSerialNo);
        setValue("p_seqno", hApdlPSeqno);
        setValue("acno_p_seqno", hApdlAcnoPSeqno);
        setValue("acct_type", hApdlAcctType);
        setValue("id_p_seqno", hApdlIdPSeqno);
      //setValue("id", h_apdl_id);
      //setValue("id_code", h_apdl_id_code);
        setValue("pay_card_no", hApdlPayCardNo);
        setValue("curr_code", hApdlCurrCode);
        setValueDouble("pay_amt", hApdlPayAmt);
        setValueDouble("dc_pay_amt", hApdlDcPayAmt);
        setValue("pay_date", hApdlPayDate);
        setValue("payment_type", hApdlPaymentType);
        setValue("debit_item", hApdlDebitItem);
        setValue("debt_key", hApdlDebtKey);
        setValue("job_code", comc.getSubString(hUserUsrDeptno,0,2));
        setValue("vouch_job_code", hAdclVouchJobCode);
        setValue("update_user", hApdlUpdateUser);
        setValue("update_date", hApdlUpdateDate);
        setValue("update_time", hApdlUpdateTime);
        setValue("branch", hApdlAcctBankNo);
        setValue("collection_store_no", hApdlCollStoreNo);
        setValue("mod_user", hApdlModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", "ActE002");
        // setValue ("mod_ws" , h_apdl_mod_ws);
        daoTable = "act_debt_cancel";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_debt_cancel duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
//    void insert_onbat() throws Exception {
//        extendField = "onbat.";
//        setValue(extendField+"trans_type", "16");
//        setValueInt(extendField+"to_which", 2);
//        setValue(extendField+"dog", sysDate + sysTime);
//        setValue(extendField+"proc_mode", "B");
//        setValueInt(extendField+"proc_status", 0);
//        setValue(extendField+"card_catalog", h_apdl_acct_type.equals("01") ? "1" : "2");
//        setValue(extendField+"payment_type", h_apdl_id_p_seqno == "" ? "2" : "1");
//        setValue(extendField+"acct_type", h_apdl_acct_type);
//        setValue(extendField+"card_hldr_id",
//                h_apdl_id_p_seqno == "" ? h_apdl_acct_key.substring(1, 1 + 8) : h_apdl_id + h_apdl_id_code);
//        setValue(extendField+"card_acct_id", h_apdl_acct_key);
//        setValue(extendField+"trans_date", h_apdl_pay_date);
//        setValueDouble(extendField+"trans_amt", h_apdl_pay_amt);
//        daoTable = "onbat_2ccas"; // onbat
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.err_rtn("insert_onbat_2ccas duplicate!", "", h_call_batch_seqno);
//        }
//
//        onbat_cnt++;
//    }

    /***********************************************************************/
    void deleteActPayDetail() throws Exception {
        daoTable = "act_pay_detail";
        whereStr = "where rowid = ? ";
        setRowId(1, hApdlRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_act_pay_detail not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteActPayBatch() throws Exception {
        daoTable = "act_pay_batch";
        whereStr = "where rowid = ? ";
        setRowId(1, hApbtRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_act_pay_batch not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
//    void insert_onbat_0() throws Exception {
//        extendField = "onbat0.";
//        setValue(extendField+"trans_type", "16");
//        setValueInt(extendField+"to_which", 2);
//        setValue(extendField+"dog", sysDate + sysTime);
//        setValue(extendField+"proc_mode", "B");
//        setValueInt(extendField+"proc_status", 1);
//        setValue(extendField+"card_acct_id", "0000000000");
//        daoTable = "onbat_2ccas"; // onbat
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.err_rtn("insert_onbat_2ccas duplicate!", "", h_call_batch_seqno);
//        }
//
//    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActE002 proc = new ActE002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
