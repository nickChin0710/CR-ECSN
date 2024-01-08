/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 106/08/10  V1.01.01  phopho     Initial                                    *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
*****************************************************************************/
package Col;

import com.*;

public class ColC062 extends AccessDAO {
    private String progname = "LGD 表一檔案資料產生處理(二)  109/12/15  V1.00.01 ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debugD = 0;
    String hBusiBusinessDate = "";

    String hLgd1IdCorpNo = "";
    String hLgd1IdCorpType = "";
    String hLgd1IdCorpPSeqno = "";  //phopho add  //取代id_no用
    String hLgd1LgdSeqno = "";
    String hLgd1LgdType = "";
    String hLgd1LgdReason = "";
    String hLgd1LgdEarlyYm = "";
    String hLgd1FromType = "";
    String hLgd1DataTable = "";
    String hLgd1AcctPSeqno = "";
    String hLgd1PaymentRate = "";
    String hLgd1ChiName = "";
    String hLgd1CrtDate = "";
    String hLgd1CrtUser = "";
    String hLgd1CloseFlag = "";
    double hLgd1RevolRate = 0;
    double hLgd1RiskAmt = 0;
    String hLgd1AcctMonth = "";
    double hLgd1AcctJrnlBal = 0;
    String hLgd1NotifyDate = "";
    String hLgd1OverdueYm = "";
    double hLgd1OverdueAmt = 0;
    String hLgd1CollYm = "";
    double hLgd1CollAmt = 0;
    String hLgdbPgmParm = "";
    String hLgdbExecTimeS = "";
    String hCallBatchSeqno = "";

    long ilTotCnt = 0;
    long ilLgdCnt = 0;
    int iiRtnCode = 1;
    String hModUser = "";
    String isLgdProcFlag = "";
    String hExecTimeS = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColC062 proc = new ColC062();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

//            if (comm.isAppActive(javaProgram)) {
//                String err1 = "comm.isAppActive    error";
//                String err2 = "";
//                comc.err_exit(err1, err2);
//            }

            // 檢查參數
            if (args.length > 2) {
                comc.errExit("Usage : ColC062  [busi_date/call_seqno]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hBusiBusinessDate = "";
            if (args.length >= 2) {
                if (args[0].length() == 8) {
                    hBusiBusinessDate = args[0];
                }
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            if (hCallBatchSeqno.length() == 20)
                comcr.callbatch(0, 0, 0);

            getModUser();

            ilTotCnt = 0;
            selectPtrBusinday();

            selectColLiacNego();
            selectColLiadRenew();
            selectColLiadLiquidate();

            showLogMessage("I", "", "處理帳戶筆數 : [" + ilTotCnt + "]");

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
            showLogMessage("I", "", "程式執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    } // End of mainProcess
      // ************************************************************************

    private void selectPtrBusinday() throws Exception {
        
        selectSQL = "decode(cast(? as varchar(8)), '',business_date, ?) business_date ";
        daoTable = "ptr_businday";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.hCallErrorDesc = "系統錯誤, 請通知資訊室";
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno );
        }
        hBusiBusinessDate = getValue("business_date");
    }

    // ************************************************************************
    private void getModUser() throws Exception {
        hModUser = "";
        /* -online callbatch------------------------------------- */
        if (hCallBatchSeqno.length() == 20) {
            selectSQL = "user_id ";
            daoTable = "ptr_callbatch";
            whereStr = "where batch_seqno = ? ";
            setString(1, hCallBatchSeqno);

            if (selectTable() > 0) {
                hModUser = getValue("user_id");
            }
        }
        if (hModUser.length() == 0) {
            hModUser = comc.commGetUserID();
        }
    }

    // ************************************************************************
    private void insertColLgdBatch() throws Exception {
        dateTime();
        daoTable = "col_lgd_batch";
        extendField = daoTable + ".";
        setValue(extendField+"pgm_id", javaProgram);
        setValue(extendField+"pgm_parm", hLgdbPgmParm);
        setValue(extendField+"exec_time_s", hLgdbExecTimeS.length() == 0 ? null : hLgdbExecTimeS);
        setValue(extendField+"exec_time_e", sysDate + sysTime);
        setValueLong(extendField+"tot_cnt", ilLgdCnt);
        setValue(extendField+"ok_flag", "Y");
        setValue(extendField+"run2_flag", "N");

        insertTable();
    }

    // ************************************************************************
    private void selectColLiacNego() throws Exception {

        /*-init-*/
        hLgdbPgmParm = "nego";
        ilLgdCnt = 0;
        hLgdbExecTimeS = "";
        sqlCmd = "select to_char(sysdate,'yyyy/mm/dd hh24:mi:ss') exec_time ";
        sqlCmd += "from dual ";

        selectTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "get sysdate error");
        }else {
            hLgdbExecTimeS = getValue("exec_time");
        }

        /*-get acct_type-*/
        selectSQL = "id_no, id_p_seqno, min(decode(notify_date,'',file_date, notify_date)) notify_date ";
        daoTable = "col_liac_nego";
        whereStr = "where liac_status in ('1','2') and  decode(lgd_proc_flag, '','N', lgd_proc_flag)='N' group by id_no, id_p_seqno ";

        openCursor();

        hLgd1DataTable = "nego";
        hLgd1IdCorpType = "1";
        while (fetchTable()) {

            initColLgd901();
            hLgd1IdCorpNo = getValue("id_no");
            hLgd1IdCorpPSeqno = getValue("id_p_seqno");
            hLgd1NotifyDate = getValue("notify_date");

            ilTotCnt++;
            ilLgdCnt++;

            /*-排除瘦身-*/
            selectEcsActAcno();
            if (iiRtnCode != 0) {
                isLgdProcFlag = "3";
                updateColLiacNego();
                continue;
            }

            selectColLgd901();
            if (iiRtnCode < 0)
                continue;

            if (iiRtnCode == 0) {
                selectLiacCname();
                insertColLgd901();
            }

            updateColLiacNego();
        }
        closeCursor();
        insertColLgdBatch();
    }

    // ************************************************************************
    private void selectColLiadRenew() throws Exception {

        /*-init-*/
        hLgdbPgmParm = "renew";
        ilLgdCnt = 0;
        hExecTimeS = "";
        sqlCmd = "select to_char(sysdate,'yyyy/mm/dd hh24:mi:ss') exec_time ";
        sqlCmd += "from dual ";

        selectTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "get sysdate error");
        } else {
            hExecTimeS = getValue("exec_time");
        }

        /*-get acct_type-*/
        selectSQL = "id_no, id_p_seqno, min(decode(notify_date,'',recv_date,notify_date)) notify_date ";
        daoTable = "col_liad_renew";
        whereStr = "where renew_status in ('1','6') and decode(lgd_proc_flag,'','N',lgd_proc_flag)='N' group by id_no, id_p_seqno ";

        openCursor();

        hLgd1DataTable = "renew";
        hLgd1IdCorpType = "1";
        while (fetchTable()) {

            initColLgd901();
            hLgd1IdCorpNo = getValue("id_no");
            hLgd1IdCorpPSeqno = getValue("id_p_seqno");
            hLgd1NotifyDate = getValue("notify_date");

            ilTotCnt++;
            ilLgdCnt++;

            /*-排除瘦身-*/
            selectEcsActAcno();
            if (iiRtnCode != 0) {
                isLgdProcFlag = "3";
                updateColLiadRenew();
                continue;
            }

            selectColLgd901();
            if (iiRtnCode < 0)
                continue;

            if (iiRtnCode == 0) {
                sqlCmd = "select chi_name ";
                sqlCmd += "from col_liad_renew ";
//                sqlCmd += "where id_no = ? ";
                sqlCmd += "where id_p_seqno = ? ";
                sqlCmd += "and renew_status in ('1','6') ";
                sqlCmd += "and decode(lgd_proc_flag,'','N',lgd_proc_flag) ='N' ";
                sqlCmd += "and decode(notify_date,'',recv_date,notify_date) = ? ";
                sqlCmd += "fetch first 1 row only";
//                setString(1, h_lgd1_id_corp_no);//phopho mod
                setString(1, hLgd1IdCorpPSeqno);
                setString(2, hLgd1NotifyDate);

                 if (selectTable() > 0) {
                    hLgd1ChiName = getValue("chi_name");
                }
                if (notFound.equals("Y")) {
//                    showLogMessage("I", "", "select col_liad_renew.chi_name error; id_no=[" + h_lgd1_id_corp_no + "]");
                    showLogMessage("I", "", "select col_liad_renew.chi_name error; id_p_seqno=[" + hLgd1IdCorpPSeqno + "]");
                }
                
                insertColLgd901();
            }

            updateColLiadRenew();
        }
        closeCursor();
        insertColLgdBatch();
    }

    // ************************************************************************
    private void selectColLiadLiquidate() throws Exception {

        /*-init-*/
        hLgdbPgmParm = "liqu";
        ilLgdCnt = 0;
        hLgdbExecTimeS = "";
        sqlCmd = "select to_char(sysdate,'yyyy/mm/dd hh24:mi:ss') exec_time ";
        sqlCmd += "from dual ";

        if (selectTable() > 0) {
            hLgdbExecTimeS = getValue("exec_time");
        }
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "get sysdate error");
        }

        /*-get acct_type-*/
        selectSQL = "id_no, id_p_seqno, min(decode(notify_date,'',recv_date,notify_date)) notify_date ";
        daoTable = "col_liad_liquidate";
        whereStr = "where liqu_status in ('1','5') and decode(lgd_proc_flag,'','N',lgd_proc_flag)='N' group by id_no, id_p_seqno ";

        openCursor();

        hLgd1DataTable = "liqu";
        hLgd1IdCorpType = "1";
        while (fetchTable()) {
            initColLgd901();
            hLgd1IdCorpNo = getValue("id_no");
            hLgd1IdCorpPSeqno = getValue("id_p_seqno");
            hLgd1NotifyDate = getValue("notify_date");

            ilTotCnt++;
            ilLgdCnt++;

            /*-排除瘦身-*/
            selectEcsActAcno();
            if (iiRtnCode != 0) {
                isLgdProcFlag = "3";
                updateColLiadLiquidate();
                continue;
            }

            selectColLgd901();
            if (iiRtnCode < 0)
                continue;

            /*-closed or notFind-*/
            if (iiRtnCode == 0) {
                sqlCmd = "select chi_name ";
                sqlCmd += "from col_liad_liquidate ";
//                sqlCmd += "where id_no = ? ";
                sqlCmd += "where id_p_seqno = ? ";
                sqlCmd += "and liqu_status in ('1','5') ";
                sqlCmd += "and decode(lgd_proc_flag,'','N',lgd_proc_flag) ='N' ";
                sqlCmd += "and decode(notify_date,'',recv_date,notify_date) = ? ";
                sqlCmd += "fetch first 1 row only";
//                setString(1, h_lgd1_id_corp_no);
                setString(1, hLgd1IdCorpPSeqno);
                setString(2, hLgd1NotifyDate);

                if (selectTable() > 0) {
                    hLgd1ChiName = getValue("chi_name");
                }
                if (notFound.equals("Y")) {
//                    showLogMessage("I", "","select col_liad_liquidate.chi_name error; id_no=[" + h_lgd1_id_corp_no + "]");
                    showLogMessage("I", "","select col_liad_liquidate.chi_name error; id_p_seqno=[" + hLgd1IdCorpPSeqno + "]");
                }
                insertColLgd901();
            }
            updateColLiadLiquidate();
        }
        closeCursor();
        insertColLgdBatch();
    }

    // ************************************************************************
    private void selectLiacCname() throws Exception {
        sqlCmd = "select chi_name ";
        sqlCmd += "from col_liac_nego ";
//        sqlCmd += "where id_no = ? ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and liac_status in ('1','2') ";
        sqlCmd += "and decode(lgd_proc_flag,'','N',lgd_proc_flag) ='N' ";
        sqlCmd += "and decode(notify_date,'',file_date,notify_date) = ? ";
        sqlCmd += "fetch first 1 row only";
//        setString(1, h_lgd1_id_corp_no);
        setString(1, hLgd1IdCorpPSeqno);
        setString(2, hLgd1NotifyDate);
        
        extendField = "liac_cname.";

        if (selectTable() > 0) {
            hLgd1ChiName = getValue("liac_cname.chi_name");
        }
        if (notFound.equals("Y")) {
//            showLogMessage("I", "", "select col_liac_nego.chi_name error; id_no=[" + h_lgd1_id_corp_no + "]");
            showLogMessage("I", "", "select col_liac_nego.chi_name error; id_p_seqno=[" + hLgd1IdCorpPSeqno + "]");
        }
    }

    // ************************************************************************
    private void updateColLiacNego() throws Exception {
        iiRtnCode = 0;
        dateTime();
        daoTable = "col_liac_nego";
        updateSQL = "lgd_proc_flag = ?, lgd_proc_date = to_char(sysdate,'yyyymmdd') ";
//        whereStr = "where id_no = ? ";
        whereStr = "where id_p_seqno = ? ";
        whereStr+= "and liac_status in ('1','2') and decode(lgd_proc_flag,'','N',lgd_proc_flag)='N' ";
        setString(1, isLgdProcFlag);
//        setString(2, h_lgd1_id_corp_no);
        setString(2, hLgd1IdCorpPSeqno);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_col_liac_nego error!";
//            String err2 = "id_no=[" + h_lgd1_id_corp_no + "]";
            String err2 = "id_p_seqno=[" + hLgd1IdCorpPSeqno + "]";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void updateColLiadRenew() throws Exception {
        iiRtnCode = 0;
        dateTime();
        daoTable = "col_liad_renew";
        updateSQL = "lgd_proc_flag = ?, lgd_proc_date = to_char(sysdate,'yyyymmdd') ";
//        whereStr = "where id_no = ? ";
        whereStr = "where id_p_seqno = ? ";
        whereStr+= "and renew_status in ('1','6') and decode(lgd_proc_flag,'','N',lgd_proc_flag)='N' ";
        setString(1, isLgdProcFlag);
//        setString(2, h_lgd1_id_corp_no);
        setString(2, hLgd1IdCorpPSeqno);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_col_liad_renew error!";
//            String err2 = "id_no=[" + h_lgd1_id_corp_no + "]";
            String err2 = "id_p_seqno=[" + hLgd1IdCorpPSeqno + "]";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void updateColLiadLiquidate() throws Exception {
        iiRtnCode = 0;
        dateTime();
        daoTable = "col_liad_liquidate";
        updateSQL = "lgd_proc_flag = ?, lgd_proc_date = to_char(sysdate,'yyyymmdd') ";
//        whereStr = "where id_no = ? "
        whereStr = "where id_p_seqno = ? ";
        whereStr+= "and liqu_status in ('1','5') and  decode(lgd_proc_flag,'','N',lgd_proc_flag)='N' ";
        setString(1, isLgdProcFlag);
//        setString(2, h_lgd1_id_corp_no);
        setString(2, hLgd1IdCorpPSeqno);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_col_liad_liquidate error!";
//            String err2 = "id_no=[" + h_lgd1_id_corp_no + "]";
            String err2 = "id_p_seqno=[" + hLgd1IdCorpPSeqno + "]";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void selectEcsActAcno() throws Exception {
        int liCnt = 0;
        iiRtnCode = 0;
        sqlCmd = "select count(*) cnt ";
        sqlCmd += "from ecs_act_acno ";
//        sqlCmd += "where acct_holder_id = ? ";
        sqlCmd += "where id_p_seqno = ? ";
//        setString(1, h_lgd1_id_corp_no);
        setString(1, hLgd1IdCorpPSeqno);

        extendField = "ecs_act_acno.";
        
        if (selectTable() > 0) {
            liCnt = getValueInt("ecs_act_acno.cnt");
        }

        if (liCnt > 0) {
            iiRtnCode = -1;
        }
    }

    // ************************************************************************
    private void selectColLgd901() throws Exception {
        int liCnt = 0;
        iiRtnCode = 0;
        isLgdProcFlag = "1";
        sqlCmd = "select count(*) cnt, ";
        sqlCmd += "max(lgd_seqno) lgd_seqno ";
        sqlCmd += "from col_lgd_901 ";
//        sqlCmd += "where id_corp_no = ? ";
        sqlCmd += "where id_corp_p_seqno = ? ";
        sqlCmd += "and   lgd_reason = 'B1' ";
        sqlCmd += "and   decode(close_flag,'','N',close_flag) <>'Y' ";
//        setString(1, h_lgd1_id_corp_no);
        setString(1, hLgd1IdCorpPSeqno);
        
        extendField = "col_lgd_901.";

        selectTable();
        if (notFound.equals("Y")) {
//            showLogMessage("I", "", "select col_lgd_901.count() error; id=[" + h_lgd1_id_corp_no + "]");
            showLogMessage("I", "", "select col_lgd_901.count() error; id_corp_p_seqno=[" + hLgd1IdCorpPSeqno + "]");
            iiRtnCode = -1;
            return;
        }
        liCnt = getValueInt("col_lgd_901.cnt");
        hLgd1LgdSeqno = getValue("col_lgd_901.lgd_seqno");
        
        if (liCnt > 0) {
            iiRtnCode = 1;
            isLgdProcFlag = "2";
        }
        if (liCnt == 0) {
            hLgd1LgdSeqno = "";
        }
    }

    // ************************************************************************
    private void insertColLgd901() throws Exception {
        selectCrdCard();

        /*- 無最早年月 no-insert -*/
        if (hLgd1LgdEarlyYm.length() == 0) {
            return;
        }

        getEarlyYmAmt();
        getOverdueYmAmt();
        getCollYmAmt();
        resetAcctJrnlBal();
        if (hLgd1RiskAmt < 0) {
            hLgd1RiskAmt = 0;
        }
        if (hLgd1OverdueAmt < 0) {
            hLgd1OverdueAmt = 0;
        }
        if (hLgd1CollAmt < 0) {
            hLgd1CollAmt = 0;
        }

        daoTable = "col_lgd_901";
        extendField = daoTable + ".";
        setValue(extendField+"id_corp_p_seqno", hLgd1IdCorpPSeqno);
        setValue(extendField+"id_corp_no", hLgd1IdCorpNo);
        setValue(extendField+"id_corp_type", hLgd1IdCorpNo.length() == 8 ? "2" : "1");
        setValue(extendField+"lgd_seqno", hLgd1LgdSeqno);
        setValue(extendField+"lgd_type", "F");
        setValue(extendField+"lgd_reason", "B1");
        setValue(extendField+"lgd_early_ym", hLgd1LgdEarlyYm);
        setValue(extendField+"from_type", "2");
        setValue(extendField+"data_table", hLgd1DataTable);
        setValue(extendField+"acct_p_seqno", "");
        setValue(extendField+"payment_rate", "");
        setValue(extendField+"chi_name", hLgd1ChiName);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_user", hModUser);
        setValue(extendField+"close_flag", "N");
        setValueDouble(extendField+"revol_rate", hLgd1RevolRate);
        setValueDouble(extendField+"risk_amt", hLgd1RiskAmt);
        setValue(extendField+"acct_month", hLgd1AcctMonth);
        setValueDouble(extendField+"acct_jrnl_bal", hLgd1AcctJrnlBal);
        setValue(extendField+"overdue_ym", hLgd1OverdueYm);
        setValueDouble(extendField+"overdue_amt", hLgd1OverdueAmt);
        setValue(extendField+"coll_ym", hLgd1CollYm);
        setValueDouble(extendField+"coll_amt", hLgd1CollAmt);
        setValue(extendField+"notify_date", hLgd1NotifyDate);
        setValue(extendField+"apr_date", sysDate);
        setValue(extendField+"apr_user", hModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();

        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_lgd_901 error[dupRecord]", "", comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void selectCrdCard() throws Exception {
        hLgd1LgdEarlyYm = "";
        /*-強停日-*/
        sqlCmd = "select substrb(min(oppost_date),1,6) early_ym ";
        sqlCmd += "from crd_card ";
//        sqlCmd += "where id_no = ? ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and   current_code ='3' ";
//        setString(1, h_lgd1_id_corp_no);
        setString(1, hLgd1IdCorpPSeqno);
        
        extendField = "crd_card_1.";

        if (selectTable() > 0) {
            hLgd1LgdEarlyYm = getValue("crd_card_1.early_ym");
        }

        if (hLgd1LgdEarlyYm.length() > 0) {
            return;
        }

        /*--申停日--*/
        sqlCmd = "select substrb(min(c.oppost_date),1,6) early_ym ";
        sqlCmd += "from crd_card c ";
//        sqlCmd += "where c.p_seqno in ( ";
//        sqlCmd += "select a.p_seqno from act_acno a, crd_idno b ";
        sqlCmd += "where  c.acno_p_seqno in ( ";
        sqlCmd += "select a.acno_p_seqno from act_acno a, crd_idno b ";
//        sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_no = ? ";
        sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_p_seqno = ? ";
        sqlCmd += "and   a.acct_status <> '1') ";
        sqlCmd += "and   c.current_code = '1' ";
//        setString(1, h_lgd1_id_corp_no);
        setString(1, hLgd1IdCorpPSeqno);
        
        extendField = "crd_card_2.";

        if (selectTable() > 0) {
            hLgd1LgdEarlyYm = getValue("crd_card_2.early_ym");
        }
    }

    // ************************************************************************
    private void getEarlyYmAmt() throws Exception {
        if (hLgd1LgdEarlyYm.length() == 0) {
            return;
        }

        if (hLgd1IdCorpType.equals("1")) {
//            sqlCmd = "select sum(nvl(acct_jrnl_bal,0)) acct_jrnl_bal, ";
//            sqlCmd += "min(stmt_revol_rate) stmt_revol_rate ";
//            sqlCmd += "from act_acct_hst ";
//            sqlCmd += "where acct_month = ? ";
//            sqlCmd += "and p_seqno in ( select a.gp_no from act_acno a, crd_idno b ";
////            sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_no = ? and a.p_seqno = a.gp_no) ";
//            sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_p_seqno = ? and a.p_seqno = a.gp_no) ";
//            setString(1, h_lgd1_lgd_early_ym);
////            setString(2, h_lgd1_id_corp_no);
//            setString(2, h_lgd1_id_p_seqno);
            sqlCmd = "select sum(nvl(acct_jrnl_bal,0)) acct_jrnl_bal, ";
            sqlCmd += "min(stmt_revol_rate) stmt_revol_rate ";
            sqlCmd += "from act_acct_hst ";
            sqlCmd += "where acct_month = ? ";
            sqlCmd += "and p_seqno in ( select a.p_seqno from act_acno a, crd_idno b ";
            sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_p_seqno = ? and a.acno_flag <> 'Y') ";
            setString(1, hLgd1LgdEarlyYm);
            setString(2, hLgd1IdCorpPSeqno);
            
            extendField = "early_ym_amt_1.";

            if (selectTable() > 0) {
                hLgd1RiskAmt = getValueDouble("early_ym_amt_1.acct_jrnl_bal");
                hLgd1RevolRate = getValueDouble("early_ym_amt_1.stmt_revol_rate");
            }
        } else {
            sqlCmd = "select sum(nvl(acct_jrnl_bal,0)) acct_jrnl_bal, ";
            sqlCmd += "min(stmt_revol_rate) stmt_revol_rate ";
            sqlCmd += "from act_acct_hst ";
            sqlCmd += "where acct_month = ? ";
//            sqlCmd += "and p_seqno in ( select gp_no from act_acno ";
            sqlCmd += "and p_seqno in ( select p_seqno from act_acno ";
            sqlCmd += "where acct_key = ?||'000') ";
            setString(1, hLgd1LgdEarlyYm);
            setString(2, hLgd1IdCorpNo);
            
            extendField = "early_ym_amt_2.";

            if (selectTable() > 0) {
                hLgd1RiskAmt = getValueDouble("early_ym_amt_2.acct_jrnl_bal");
                hLgd1RevolRate = getValueDouble("early_ym_amt_2.stmt_revol_rate");
            }
        }
    }

    // ************************************************************************
    private void getOverdueYmAmt() throws Exception {

        /* get 帳戶逾放年月, 逾放日暴險額 */
        if (hLgd1IdCorpType.equals("1")) {
            sqlCmd = "select min(substrb(a.org_delinquent_date,1,6)) overdue_ym ";
            sqlCmd += "from act_acno a, crd_idno b ";
//            sqlCmd += "where b.id_no = ? ";
            sqlCmd += "where b.id_p_seqno = ? ";
//            sqlCmd += "and   a.p_seqno = a.gp_no ";
            sqlCmd += "and   a.acno_flag <> 'Y' ";
            sqlCmd += "and   b.id_p_seqno = a.id_p_seqno ";
//            setString(1, h_lgd1_id_corp_no);
            setString(1, hLgd1IdCorpPSeqno);
            
            extendField = "overdue_ym_amt_1.";

            if (selectTable() > 0) {
                hLgd1OverdueYm = getValue("overdue_ym_amt_1.overdue_ym");
            }

            if (hLgd1OverdueYm.length() == 0) {
                return;
            }

//            sqlCmd = "select nvl(sum(acct_jrnl_bal),0) acct_jrnl_bal ";
//            sqlCmd += "from act_acct_hst ";
//            sqlCmd += "where acct_month = ? ";
//            sqlCmd += "and p_seqno in ( select a.gp_no from act_acno a, crd_idno b ";
////            sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_no = ? and a.p_seqno = a.gp_no) ";
//            sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_p_seqno = ? and a.p_seqno = a.gp_no) ";
//            setString(1, h_lgd1_overdue_ym);
////            setString(2, h_lgd1_id_corp_no);
//            setString(2, h_lgd1_id_p_seqno);
            sqlCmd = "select nvl(sum(acct_jrnl_bal),0) acct_jrnl_bal ";
            sqlCmd += "from act_acct_hst ";
            sqlCmd += "where acct_month = ? ";
            sqlCmd += "and p_seqno in ( select a.p_seqno from act_acno a, crd_idno b ";
            sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_p_seqno = ? and a.acno_flag <> 'Y') ";
            setString(1, hLgd1OverdueYm);
            setString(2, hLgd1IdCorpPSeqno);
            
            extendField = "overdue_ym_amt_2.";

            if (selectTable() > 0) {
                hLgd1OverdueAmt = getValueDouble("overdue_ym_amt_2.acct_jrnl_bal");
            }
        } else {
            sqlCmd = "select min(substrb(org_delinquent_date,1,6)) overdue_ym ";
            sqlCmd += "from act_acno ";
            sqlCmd += "where acct_key =?||'000' ";
//            sqlCmd += "and   p_seqno = gp_no ";
            sqlCmd += "and acno_flag <> 'Y' ";
            setString(1, hLgd1IdCorpNo);
            
            extendField = "overdue_ym_amt_3.";

            if (selectTable() > 0) {
                hLgd1OverdueYm = getValue("overdue_ym_amt_3.overdue_ym");
            }

            if (hLgd1OverdueYm.length() == 0) {
                return;
            }

            sqlCmd = "select nvl(sum(acct_jrnl_bal),0) acct_jrnl_bal ";
            sqlCmd += "from act_acct_hst ";
            sqlCmd += "where acct_month = ? ";
//            sqlCmd += "and p_seqno in ( select gp_no from act_acno ";
            sqlCmd += "and p_seqno in ( select p_seqno from act_acno ";
            sqlCmd += "where acct_key = ?||'000') ";
            setString(1, hLgd1OverdueYm);
            setString(2, hLgd1IdCorpNo);
            
            extendField = "overdue_ym_amt_4.";

            if (selectTable() > 0) {
                hLgd1OverdueAmt = getValueDouble("overdue_ym_amt_4.acct_jrnl_bal");
            }
        }
    }

    // ************************************************************************
    private void getCollYmAmt() throws Exception {
        /* get 轉催收年月, 轉催日暴險額 */

        if (hLgd1IdCorpType.equals("1")) {
            sqlCmd = "select sum(A.end_bal) end_bal, ";
            sqlCmd += "substrb(max(A.trans_date),1,6) trans_date ";
            sqlCmd += "from col_bad_detail A, act_acno B, crd_idno C ";
//            sqlCmd += "where A.p_seqno = B.gp_no ";
//            sqlCmd += "and   B.p_seqno = B.gp_no ";
            sqlCmd += "where A.p_seqno = B.p_seqno ";
            sqlCmd += "and   B.acno_flag <> 'Y' ";
            sqlCmd += "and   C.id_p_seqno = B.id_p_seqno ";
//            sqlCmd += "and C.id_no = ? ";
            sqlCmd += "and B.id_p_seqno = ? ";
            sqlCmd += "and trans_type = '3' and acct_code in ('CB','CI','CC') ";
            sqlCmd += "and trans_date = (select min(trans_date) ";
//            sqlCmd += "from col_bad_detail where p_seqno=B.gp_no and trans_type = '3') ";
            sqlCmd += "from col_bad_detail where p_seqno = B.p_seqno and trans_type = '3') ";
//            setString(1, h_lgd1_id_corp_no);
            setString(1, hLgd1IdCorpPSeqno);
            
            extendField = "coll_ym_amt_1.";

            if (selectTable() > 0) {
                hLgd1CollAmt = getValueDouble("coll_ym_amt_1.end_bal");
                hLgd1CollYm = getValue("coll_ym_amt_1.trans_date");
            }
        } else {
            sqlCmd = "select sum(A.end_bal) end_bal, ";
            sqlCmd += "substrb(max(A.trans_date),1,6) trans_date ";
            sqlCmd += "from col_bad_detail A, act_acno B ";
//            sqlCmd += "where A.p_seqno = B.gp_no ";
//            sqlCmd += "and   B.p_seqno = B.gp_no ";
            sqlCmd += "where A.p_seqno = B.p_seqno ";
            sqlCmd += "and   B.acno_flag <> 'Y' ";
            sqlCmd += "and B.acct_key = ?||'000' ";
            sqlCmd += "and trans_type = '3' and acct_code in ('CB','CI','CC') ";
            sqlCmd += "and trans_date = (select min(trans_date) ";
//            sqlCmd += "from col_bad_detail where p_seqno=B.gp_no and trans_type = '3') ";
            sqlCmd += "from col_bad_detail where p_seqno = B.p_seqno and trans_type = '3') ";
            setString(1, hLgd1IdCorpNo);
            
            extendField = "coll_ym_amt_2.";

            if (selectTable() > 0) {
                hLgd1CollAmt = getValueDouble("coll_ym_amt_2.end_bal");
                hLgd1CollYm = getValue("coll_ym_amt_2.trans_date");
            }
        }
    }

    // ************************************************************************
    private void resetAcctJrnlBal() throws Exception {
        double lm_bal = 0;
        if (hLgd1RiskAmt > 0 && hLgd1OverdueAmt > 0) {
            return;
        }

        if (hLgd1IdCorpType.equals("1")) {
//            sqlCmd = "select sum(acct_jrnl_bal) acct_jrnl_bal ";
//            sqlCmd += "from act_acct ";
//            sqlCmd += "where 1=1 ";
//            sqlCmd += "and p_seqno in ( select a.gp_no from act_acno a, crd_idno b ";
////            sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_no = ? and a.p_seqno = a.gp_no) ";
//            sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_p_seqno = ? and a.p_seqno = a.gp_no) ";
////            setString(1, h_lgd1_id_corp_no);
//            setString(1, h_lgd1_id_p_seqno);
            sqlCmd = "select sum(acct_jrnl_bal) acct_jrnl_bal ";
            sqlCmd += "from act_acct ";
            sqlCmd += "where 1=1 ";
            sqlCmd += "and p_seqno in ( select a.p_seqno from act_acno a, crd_idno b ";
            sqlCmd += "where b.id_p_seqno = a.id_p_seqno and b.id_p_seqno = ? and a.acno_flag <> 'Y') ";
            setString(1, hLgd1IdCorpPSeqno);
            
            extendField = "acct_jrnl_bal_1.";

            if (selectTable() > 0) {
                lm_bal = getValueDouble("acct_jrnl_bal_1.acct_jrnl_bal");
            }
        } else {
            sqlCmd = "select sum(acct_jrnl_bal) acct_jrnl_bal ";
            sqlCmd += "from act_acct ";
            sqlCmd += "where 1=1 ";
//            sqlCmd += "and p_seqno in ( select gp_no from act_acno ";
            sqlCmd += "and p_seqno in ( select p_seqno from act_acno ";
            sqlCmd += "where acct_key = ?||'000') ";
            setString(1, hLgd1IdCorpNo);
            
            extendField = "acct_jrnl_bal_2.";

            if (selectTable() > 0) {
                lm_bal = getValueDouble("acct_jrnl_bal_2.acct_jrnl_bal");
            }
        }

        if (hLgd1RiskAmt == 0 && hLgd1LgdEarlyYm.length() > 0) {
            hLgd1RiskAmt = lm_bal;
        }
        if (hLgd1OverdueAmt == 0 && hLgd1OverdueYm.length() > 0) {
            hLgd1OverdueAmt = lm_bal;
        }
    }

    // ************************************************************************
    private void initColLgd901() throws Exception {
        /*--
        str2var(h_lgd1_id_corp_type            , "");
        str2var(h_lgd1_data_table              , "");
        --*/
        hLgd1IdCorpNo = "";
        hLgd1IdCorpPSeqno = "";
        hLgd1LgdSeqno = "";
        hLgd1LgdType = "";
        hLgd1LgdReason = "";
        hLgd1LgdEarlyYm = "";
        hLgd1FromType = "";
        hLgd1AcctPSeqno = "";
        hLgd1PaymentRate = "";
        hLgd1ChiName = "";
        hLgd1CrtDate = "";
        hLgd1CrtUser = "";
        hLgd1CloseFlag = "";
        hLgd1NotifyDate = "";
        hLgd1AcctMonth = "";
        hLgd1OverdueYm = "";
        hLgd1CollYm = "";
        hLgd1RevolRate = 0;
        hLgd1RiskAmt = 0;
        hLgd1AcctJrnlBal = 0;
        hLgd1OverdueAmt = 0;
        hLgd1CollAmt = 0;
    }

    // ************************************************************************
}