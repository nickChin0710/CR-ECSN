/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 106/08/09  V1.01.01  phopho     Initial                                    *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
*****************************************************************************/
package Col;

import com.*;

public class ColC061 extends AccessDAO {
    private String progname = "LGD 表一檔案資料產生處理一 109/12/15  V1.00.01 ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    String hBusiBusinessDate = "";

    String hAcnoAcctStatus = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoLastPayDate = "";
    String hLgd1IdCorpPSeqno = "";  //phopho add  //取代id_no用
    String hLgd1IdCorpNo = "";
    String hLgd1IdCorpType = "";
    String hLgd1LgdSeqno = "";
    String hLgd1LgdEarlyYm = "";
    String hLgd1AcctPSeqno = "";
    String hLgd1PaymentRate = "";
    double hLgd1RevolRate = 0;
    double hLgd1RiskAmt = 0;
    double hLgd1AcctJrnlBal = 0;
    String hLgd1OverdueYm = "";
    double hLgd1OverdueAmt = 0;
    String hLgd1CollYm = "";
    double hLgd1CollAmt = 0;
    String hCallBatchSeqno = "";
    String hAcnoChiName = "";

    long ilTotCnt = 0;
    String hModUser = "";
    String isStmtCycle = "";
    String isCloseDate = "";
    String isAcctMonth = "";
    String is03PayDate = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColC061 proc = new ColC061();
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

            if (comm.isAppActive(javaProgram)) {
            	exceptExit = 0;
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }

            // 檢查參數
            if (args.length > 2) {
                comc.errExit("Usage : ColC061 [stmt_cycle]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            //online call batch 時須記錄
//            comcr.h_call_batch_seqno = h_call_batch_seqno;
//            comcr.h_call_r_program_code = javaProgram;
//            comcr.callbatch(0, 0, 0);

            getModUser();

            hBusiBusinessDate = "";
            isStmtCycle = "";
            if (args.length >= 1) {
                if (args[0].length() == 2) {
                    isStmtCycle = args[0];
                }
            }
            selectPtrBusinday();
            selectPtrWorkday();
            if (checkLgdBatch() == 1) {
                selectActAcno();
                updateColLgdBatch();
            }

            showLogMessage("I", "", "處理帳戶筆數 : [" + ilTotCnt + "]");

            // ==============================================
            // 固定要做的
//            comcr.callbatch(1, 0, 0);
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
        is03PayDate = "";
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date, ";
        sqlCmd += "to_char(add_months(to_date(decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))),'yyyymmdd'),-3),'yyyymmdd') pay_date ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);

        selectTable();
        if ( notFound.equals("Y") ) {
            comcr.hCallErrorDesc = "系統錯誤, 請通知資訊室";
            rollbackDataBase();
            comcr.errRtn("select_ptr_businday error!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
        is03PayDate = getValue("pay_date");
         
        showLogMessage("I", "",
                "-->busi_date=[" + hBusiBusinessDate + "], last_pay_date-03=[" + is03PayDate + "]");
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
    private void selectPtrWorkday() throws Exception {
        isCloseDate = "";
        isAcctMonth = "";
        if (isStmtCycle.length() > 0) {
            selectSQL = "last_close_date, last_acct_month ";
            daoTable = "ptr_workday";
            whereStr = "where stmt_cycle = ? ";
            setString(1, isStmtCycle);

            selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_ptr_workday error!", "", hCallBatchSeqno);
            }
            isCloseDate = getValue("last_close_date");
            isAcctMonth = getValue("last_acct_month");
        } else {
            selectSQL = "stmt_cycle, last_close_date, last_acct_month ";
            daoTable = "ptr_workday";
            whereStr = "where last_close_date in ( select max(last_close_date) from ptr_workday "
                    + "where last_close_date <= ? ) ";
            setString(1, hBusiBusinessDate);

            selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_ptr_workday error!", "", hCallBatchSeqno);
            }
            isStmtCycle = getValue("stmt_cycle");
            isCloseDate = getValue("last_close_date");
            isAcctMonth = getValue("last_acct_month");
        }
        
        showLogMessage("I", "", "-->處理: Cycle=[" + isStmtCycle + "], close_date=[" + isCloseDate + "], acct_month=["
                + isAcctMonth + "]");
    }

    // ************************************************************************
    private int checkLgdBatch() throws Exception {
        int llCnt = 0;
        sqlCmd = "select count(*) cnt ";
        sqlCmd += "from col_lgd_batch ";
        sqlCmd += "where pgm_id = ? ";
        sqlCmd += "and pgm_parm =rpad(?,6)||rpad(?,2) ";
        sqlCmd += "and ok_flag ='Y' and run2_flag!='Y' ";
        setString(1, javaProgram);
        setString(2, isAcctMonth);
        setString(3, isStmtCycle);

        if (selectTable() > 0) {
            llCnt = getValueInt("cnt");
        }

        if (llCnt > 0) {
            showLogMessage("I", "", "資料已處理, 不須重複執行");
            return 0;
        }

        // do insert
        daoTable = "col_lgd_batch";
        extendField = daoTable + ".";
        setValue(extendField+"pgm_id", javaProgram);
        setValue(extendField+"pgm_parm", String.format("%1$-6s", isAcctMonth) + String.format("%1$-2s", isStmtCycle));
        setValue(extendField+"exec_time_s", sysDate + sysTime);
        setValue(extendField+"exec_time_e", "");
        setValueInt(extendField+"tot_cnt", 0);
        setValue(extendField+"ok_flag", "N");
        setValue(extendField+"run2_flag", "N");

        insertTable();

        return 1;
    }

    // ************************************************************************
    private void updateColLgdBatch() throws Exception {
        dateTime();
        daoTable = "col_lgd_batch";
        updateSQL = "exec_time_e = sysdate, tot_cnt = ?, ok_flag = 'Y' ";
        whereStr = "where pgm_id = ? and pgm_parm = rpad(?,6)||rpad(?,2) "
                + "and   ok_flag = 'N' and run2_flag = 'N' ";
        setLong(1, ilTotCnt);
        setString(2, javaProgram);
        setString(3, isAcctMonth);
        setString(4, isStmtCycle);

        updateTable();

        if (notFound.equals("Y")) {
            rollbackDataBase();
            comcr.errRtn("update_col_lgd_batch error!", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void selectActAcno() throws Exception {
        ilTotCnt = 0;

        /*-get acct_type-*/
        selectSQL = "decode(c.id_no,'',d.corp_no,c.id_no) db_key_no, " 
        		+ "decode(c.id_no,'',d.corp_p_seqno,c.id_p_seqno) db_key_p_seqno, "
//                + "a.gp_no, "
                + "a.p_seqno, "
                + "decode(c.id_no,'','2','1') db_type, " 
                + "b.acct_jrnl_bal, " 
                + "b.stmt_revol_rate, "
                + "a.payment_rate1, " 
                + "a.pay_by_stage_flag, " 
                + "a.last_pay_date, " 
                + "a.acct_status, "
                + "substrb(a.org_delinquent_date,1,6) overdue_ym ";
        daoTable = "act_acct_hst b, act_acno a, crd_idno c "
        		+ " left join crd_corp d on d.corp_p_seqno = a.corp_p_seqno "; //find corp_no in crd_corp
//        whereStr = "where b.p_seqno = a.gp_no " 
        whereStr = "where b.p_seqno = a.p_seqno "
                + "and   b.acct_month = ? "
                + "and   a.stmt_cycle = ? "
                + "and   c.id_p_seqno = a.id_p_seqno "
//                + "and   a.p_seqno = a.gp_no "
                + "and   a.acno_flag <> 'Y' " 
//                + "and   a.payment_rate1 >='03' "
                + "and   a.payment_rate1 ='03' "
                + "and   a.payment_rate1 not between '0A' and '0Z' " 
                + "and   decode(a.pay_by_stage_flag,'','x',a.pay_by_stage_flag)!='00' "
//                + "and   b.acct_jrnl_bal >=1000 and a.gp_no not in  "
                + "and   b.acct_jrnl_bal >=1000 and a.p_seqno not in  "
                + "( select acct_p_seqno from col_lgd_901 where data_table ='acno' and decode(close_flag,'','N',close_flag)!='Y') ";
        /* and lgd_seqno is not null */
        setString(1, isAcctMonth);
        setString(2, isStmtCycle);

        openCursor();
        while (fetchTable()) {
            hLgd1IdCorpNo = getValue("db_key_no");
            hLgd1IdCorpPSeqno = getValue("db_key_p_seqno");
//            h_lgd1_acct_p_seqno = getValue("gp_no");
            hLgd1AcctPSeqno = getValue("p_seqno");
            hLgd1IdCorpType = getValue("db_type");
            hLgd1AcctJrnlBal = getValueDouble("acct_jrnl_bal");
            hLgd1RevolRate = getValueDouble("stmt_revol_rate");
            hLgd1PaymentRate = getValue("payment_rate1");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hAcnoLastPayDate = getValue("last_pay_date");
            hAcnoAcctStatus = getValue("acct_status");
            hLgd1OverdueYm = getValue("overdue_ym");

            ilTotCnt++;

            /*-'NE','NF','NK','NO':三個月有還款--*/
            /*-jh:1050608:有值&<>'99'-*/
            /* if (strstr("|NE|NF|NK|NO",h_acno_pay_by_stage_flag.arr)>0 && */
            if (hAcnoPayByStageFlag.length() > 0 && comc.getSubString(hAcnoLastPayDate, 0, 2).compareTo("00") != 0) {
                if (hAcnoLastPayDate.compareTo(is03PayDate) >= 0) {
                    continue;
                }
            }
            insertColLgd901();
        }
        closeCursor();
    }

    // ************************************************************************
    private void getEarlyYmAmt() throws Exception {
        int liMm = 0;
        liMm = 3 - Integer.valueOf(hLgd1PaymentRate);

        /*-最早違約年月-*/
        sqlCmd = "select to_char(add_months(to_date(?,'yyyymm'), ?),'yyyymm') early_ym ";
        sqlCmd += "from dual";
        setString(1, isAcctMonth);
        setInt(2, liMm);
        
        extendField = "early_ym_amt_1.";

        selectTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "-->can not get LGD_EARLY_YM [" + hLgd1IdCorpNo + "]");
            return;
        }
        hLgd1LgdEarlyYm = getValue("early_ym_amt_1.early_ym");

        if (hLgd1LgdEarlyYm.length() == 0) {
            return;
        }

        if (hLgd1IdCorpType.equals("1")) {
//            sqlCmd = "select sum(nvl(a.acct_jrnl_bal,0)) acct_jrnl_bal ";
//            sqlCmd += "from act_acct_hst a ";
//            sqlCmd += "where a.acct_month = ? ";
////            sqlCmd += "and a.p_seqno in ( select c.gp_no from act_acno c, crd_idno b ";
////            sqlCmd += "where b.id_p_seqno = c.id_p_seqno and b.id_no = ? and c.p_seqno = c.gp_no) ";
//            sqlCmd += "and a.p_seqno in ( select c.p_seqno from act_acno c, crd_idno b ";
//            sqlCmd += "where b.id_p_seqno = c.id_p_seqno and b.id_no = ? and c.acno_flag <> 'Y') ";
//            setString(1, h_lgd1_lgd_early_ym);
//            setString(2, h_lgd1_id_corp_no);
            
            sqlCmd = "select sum(nvl(a.acct_jrnl_bal,0)) acct_jrnl_bal ";
            sqlCmd += "from act_acct_hst a ";
            sqlCmd += "where a.acct_month = ? ";
            sqlCmd += "and a.p_seqno in ( select c.p_seqno from act_acno c ";
            sqlCmd += "where c.id_p_seqno = ? and c.acno_flag <> 'Y') ";
            setString(1, hLgd1LgdEarlyYm);
            setString(2, hLgd1IdCorpPSeqno);
            
            extendField = "early_ym_amt_2.";

            if (selectTable() > 0) {
                hLgd1RiskAmt = getValueDouble("early_ym_amt_2.acct_jrnl_bal");
            }
        } else {
//            sqlCmd = "select sum(nvl(acct_jrnl_bal,0)) acct_jrnl_bal ";
//            sqlCmd += "from act_acct_hst ";
//            sqlCmd += "where acct_month = ? ";
////            sqlCmd += "and p_seqno in ( select gp_no from act_acno ";
//            sqlCmd += "and p_seqno in ( select p_seqno from act_acno ";
//            sqlCmd += "where acct_key = ?||'000') ";
//            setString(1, h_lgd1_lgd_early_ym);
//            setString(2, h_lgd1_id_corp_no);
            
            sqlCmd = "select sum(nvl(acct_jrnl_bal,0)) acct_jrnl_bal ";
            sqlCmd += "from act_acct_hst ";
            sqlCmd += "where acct_month = ? ";
            sqlCmd += "and p_seqno in ( select p_seqno from act_acno ";
            sqlCmd += "where corp_p_seqno = ? ) ";
            setString(1, hLgd1LgdEarlyYm);
            setString(2, hLgd1IdCorpPSeqno);
            
            extendField = "early_ym_amt_3.";

            if (selectTable() > 0) {
                hLgd1RiskAmt = getValueDouble("early_ym_amt_3.acct_jrnl_bal");
            }
        }
    }

    // ************************************************************************
    private void getOverdueYmAmt() throws Exception {
        /* get 帳戶逾放年月, 逾放日暴險額 */
        if (hLgd1OverdueYm.length() == 0) {
            return;
        }

        if (hLgd1IdCorpType.equals("1")) {
//            sqlCmd = "select sum(nvl(a.acct_jrnl_bal,0)) acct_jrnl_bal ";
//            sqlCmd += "from act_acct_hst a ";
//            sqlCmd += "where a.acct_month = ? ";
////            sqlCmd += "and a.p_seqno in ( select c.gp_no from act_acno c, crd_idno b ";
////            sqlCmd += "where b.id_p_seqno = c.id_p_seqno and b.id_no = ? and c.p_seqno = c.gp_no) ";
//            sqlCmd += "and a.p_seqno in ( select c.p_seqno from act_acno c, crd_idno b ";
//            sqlCmd += "where b.id_p_seqno = c.id_p_seqno and b.id_no = ? and c.acno_flag <> 'Y') ";
//            setString(1, h_lgd1_overdue_ym);
//            setString(2, h_lgd1_id_corp_no);
            
            sqlCmd = "select sum(nvl(a.acct_jrnl_bal,0)) acct_jrnl_bal ";
            sqlCmd += "from act_acct_hst a ";
            sqlCmd += "where a.acct_month = ? ";
            sqlCmd += "and a.p_seqno in ( select c.p_seqno from act_acno c ";
            sqlCmd += "where c.id_p_seqno = ? and c.acno_flag <> 'Y') ";
            setString(1, hLgd1OverdueYm);
            setString(2, hLgd1IdCorpPSeqno);
            
            extendField = "overdue_ym_amt_1.";

            if (selectTable() > 0) {
                hLgd1OverdueAmt = getValueDouble("overdue_ym_amt_1.acct_jrnl_bal");
            }
        } else {
//            sqlCmd = "select sum(nvl(acct_jrnl_bal,0)) acct_jrnl_bal ";
//            sqlCmd += "from act_acct_hst ";
//            sqlCmd += "where acct_month = ? ";
////            sqlCmd += "and p_seqno in ( select gp_no from act_acno ";
//            sqlCmd += "and p_seqno in ( select p_seqno from act_acno ";
//            sqlCmd += "where acct_key = ?||'000') ";
//            setString(1, h_lgd1_overdue_ym);
//            setString(2, h_lgd1_id_corp_no);
            
            sqlCmd = "select sum(nvl(acct_jrnl_bal,0)) acct_jrnl_bal ";
            sqlCmd += "from act_acct_hst ";
            sqlCmd += "where acct_month = ? ";
            sqlCmd += "and p_seqno in ( select p_seqno from act_acno ";
            sqlCmd += "where corp_p_seqno = ? ) ";
            setString(1, hLgd1OverdueYm);
            setString(2, hLgd1IdCorpPSeqno);
            
            extendField = "overdue_ym_amt_2.";

            if (selectTable() > 0) {
                hLgd1OverdueAmt = getValueDouble("overdue_ym_amt_2.acct_jrnl_bal");
            }
        }
    }

    // ************************************************************************
    private void getCollYmAmt() throws Exception {
        /* get 轉催收年月, 轉催日暴險額 */

        /*->>-105-0428-
        	EXEC SQL
        		SELECT sum(nvl(end_bal,0)), 
        				 substrb(max(trans_date),1,6)
        		 INTO :h_lgd1_coll_amt:di,
        				:h_lgd1_coll_ym:di  
        		 FROM col_bad_detail
        		WHERE p_seqno = :h_lgd1_acct_p_seqno
        		  and trans_type = '3'
        		  and new_item_ename in ('CB','CI','CC')
        		  and trans_date = (select min(trans_date)
        								  from   col_bad_detail
        								  where  p_seqno=:h_lgd1_acct_p_seqno
        								  and    trans_type = '3');	
        -<<-*/
        if (hLgd1IdCorpType.equals("1")) {
//            sqlCmd = "select sum(nvl(A.end_bal,0)) end_bal, ";
//            sqlCmd += "substrb(max(A.trans_date),1,6) trans_date ";
//            sqlCmd += "from col_bad_detail A, act_acno B, crd_idno C ";
////            sqlCmd += "where A.p_seqno = B.gp_no ";
////            sqlCmd += "and   B.p_seqno = B.gp_no ";
//            sqlCmd += "where A.p_seqno = B.p_seqno ";
//            sqlCmd += "and   B.acno_flag <> 'Y' ";
//            sqlCmd += "and   C.id_p_seqno = B.id_p_seqno ";
//            sqlCmd += "and C.id_no = ? ";
//            sqlCmd += "and trans_type = '3' and new_acct_code in ('CB','CI','CC') ";
//            sqlCmd += "and trans_date = (select min(trans_date) ";
////            sqlCmd += "from col_bad_detail where p_seqno=B.gp_no and trans_type = '3') ";
//            sqlCmd += "from col_bad_detail where p_seqno = B.p_seqno and trans_type = '3') ";
//            setString(1, h_lgd1_id_corp_no);
            
            sqlCmd = "select sum(nvl(A.end_bal,0)) end_bal, ";
            sqlCmd += "substrb(max(A.trans_date),1,6) trans_date ";
            sqlCmd += "from col_bad_detail A, act_acno B ";
            sqlCmd += "where A.p_seqno = B.p_seqno ";
            sqlCmd += "and   B.acno_flag <> 'Y' ";
            sqlCmd += "and   B.id_p_seqno = ? ";
            sqlCmd += "and trans_type = '3' and new_acct_code in ('CB','CI','CC') ";
            sqlCmd += "and trans_date = (select min(trans_date) ";
            sqlCmd += "from col_bad_detail where p_seqno = B.p_seqno and trans_type = '3') ";
            setString(1, hLgd1IdCorpPSeqno);

            extendField = "coll_ym_amt_1.";
            
            if (selectTable() > 0) {
                hLgd1CollAmt = getValueDouble("coll_ym_amt_1.end_bal");
                hLgd1CollYm = getValue("coll_ym_amt_1.trans_date");
            }
        } else {
//            sqlCmd = "select sum(nvl(A.end_bal,0)) end_bal, ";
//            sqlCmd += "substrb(max(A.trans_date),1,6) trans_date ";
//            sqlCmd += "from col_bad_detail A, act_acno B ";
////            sqlCmd += "where A.p_seqno = B.gp_no ";
////            sqlCmd += "and   B.p_seqno = B.gp_no ";
//            sqlCmd += "where A.p_seqno = B.p_seqno ";
//            sqlCmd += "and   B.acno_flag <> 'Y' ";
//            sqlCmd += "and B.acct_key = ?||'000' ";
//            sqlCmd += "and trans_type = '3' and new_item_ename in ('CB','CI','CC') ";
//            sqlCmd += "and trans_date = (select min(trans_date) ";
////            sqlCmd += "from col_bad_detail where p_seqno=B.gp_no and trans_type = '3') ";
//            sqlCmd += "from col_bad_detail where p_seqno = B.p_seqno and trans_type = '3') ";
//            setString(1, h_lgd1_id_corp_no);
            
            sqlCmd = "select sum(nvl(A.end_bal,0)) end_bal, ";
            sqlCmd += "substrb(max(A.trans_date),1,6) trans_date ";
            sqlCmd += "from col_bad_detail A, act_acno B ";
            sqlCmd += "where A.p_seqno = B.p_seqno ";
            sqlCmd += "and   B.acno_flag <> 'Y' ";
            sqlCmd += "and B.corp_p_seqno = ? ";
            sqlCmd += "and trans_type = '3' and new_item_ename in ('CB','CI','CC') ";
            sqlCmd += "and trans_date = (select min(trans_date) ";
            sqlCmd += "from col_bad_detail where p_seqno = B.p_seqno and trans_type = '3') ";
            setString(1, hLgd1IdCorpPSeqno);
            
            extendField = "coll_ym_amt_2.";

            if (selectTable() > 0) {
                hLgd1CollAmt = getValueDouble("coll_ym_amt_2.end_bal");
                hLgd1CollYm = getValue("coll_ym_amt_2.trans_date");
            }
        }
    }

    // ************************************************************************
    private void resetAcctJrnlBal() throws Exception {
        double lmBal = 0;
        if (hLgd1RiskAmt > 0 && hLgd1OverdueAmt > 0) {
            return;
        }

        if (hLgd1IdCorpType.equals("1")) {
//            sqlCmd = "select sum(nvl(a.acct_jrnl_bal,0)) acct_jrnl_bal ";
//            sqlCmd += "from act_acct a ";
//            sqlCmd += "where 1=1 ";
////            sqlCmd += "and a.p_seqno in ( select c.gp_no from act_acno c, crd_idno b ";
////            sqlCmd += "where b.id_p_seqno = c.id_p_seqno and b.id_no = ? and c.p_seqno = c.gp_no) ";
//            sqlCmd += "and a.p_seqno in ( select c.p_seqno from act_acno c, crd_idno b ";
//            sqlCmd += "where b.id_p_seqno = c.id_p_seqno and b.id_no = ? and c.acno_flag <> 'Y') ";
//            setString(1, h_lgd1_id_corp_no);
            
            sqlCmd = "select sum(nvl(a.acct_jrnl_bal,0)) acct_jrnl_bal ";
            sqlCmd += "from act_acct a ";
            sqlCmd += "where a.p_seqno in ( select c.p_seqno from act_acno c ";
            sqlCmd += "where c.id_p_seqno = ? and c.acno_flag <> 'Y') ";
            setString(1, hLgd1IdCorpPSeqno);
            
            extendField = "acct_jrnl_bal_1.";

            if (selectTable() > 0) {
                lmBal = getValueDouble("acct_jrnl_bal_1.acct_jrnl_bal");
            }
        } else {
//            sqlCmd = "select sum(nvl(acct_jrnl_bal,0)) acct_jrnl_bal ";
//            sqlCmd += "from act_acct ";
//            sqlCmd += "where 1=1 ";
////            sqlCmd += "and p_seqno in ( select gp_no from act_acno ";
//            sqlCmd += "and p_seqno in ( select p_seqno from act_acno ";
//            sqlCmd += "where acct_key = ?||'000') ";
//            setString(1, h_lgd1_id_corp_no);
            
            sqlCmd = "select sum(nvl(acct_jrnl_bal,0)) acct_jrnl_bal ";
            sqlCmd += "from act_acct ";
            sqlCmd += "where p_seqno in ( select p_seqno from act_acno ";
            sqlCmd += "where corp_p_seqno = ? ) ";
            setString(1, hLgd1IdCorpPSeqno);

            extendField = "acct_jrnl_bal_2.";
            
            if (selectTable() > 0) {
                lmBal = getValueDouble("acct_jrnl_bal_2.acct_jrnl_bal");
            }
        }


        if (hLgd1RiskAmt == 0 && hLgd1LgdEarlyYm.length() > 0) {
            hLgd1RiskAmt = lmBal;
        }
        if (hLgd1OverdueAmt == 0 && hLgd1OverdueYm.length() > 0) {
            hLgd1OverdueAmt = lmBal;
        }
    }

    // ************************************************************************
    private void insertColLgd901() throws Exception {
        getEarlyYmAmt();
        if (hLgd1LgdEarlyYm.length() == 0) {
            return;
        }

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
        setLgdSeqno();
        getChiName();  //add by phopho 2019.4.2

        dateTime();
        daoTable = "col_lgd_901";
        extendField = daoTable + ".";
        setValue(extendField+"id_corp_p_seqno", hLgd1IdCorpPSeqno);
        setValue(extendField+"id_corp_no", hLgd1IdCorpNo);
        setValue(extendField+"id_corp_type", hLgd1IdCorpType);
        setValue(extendField+"lgd_seqno", hLgd1LgdSeqno);
        setValue(extendField+"lgd_type", "F");
        setValue(extendField+"lgd_reason", "A");
        setValue(extendField+"lgd_early_ym", hLgd1LgdEarlyYm);
        setValue(extendField+"from_type", "2");
        setValue(extendField+"data_table", "acno");
        setValue(extendField+"acct_p_seqno", hLgd1AcctPSeqno);
        setValue(extendField+"payment_rate", hLgd1PaymentRate);
        setValue(extendField+"chi_name", hAcnoChiName);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_user", hModUser);
        setValue(extendField+"close_flag", "N");
        setValueDouble(extendField+"revol_rate", hLgd1RevolRate);
        setValueDouble(extendField+"risk_amt", hLgd1RiskAmt);
        setValue(extendField+"acct_month", isAcctMonth);
        setValueDouble(extendField+"acct_jrnl_bal", hLgd1AcctJrnlBal);
        setValue(extendField+"overdue_ym", hLgd1OverdueYm);
        setValueDouble(extendField+"overdue_amt", hLgd1OverdueAmt);
        setValue(extendField+"coll_ym", hLgd1CollYm);
        setValueDouble(extendField+"coll_amt", hLgd1CollAmt);
        setValue(extendField+"apr_date", sysDate);
        setValue(extendField+"apr_user", hModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();

        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_lgd_901 error[dupRecord]", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void setLgdSeqno() throws Exception {
        hLgd1LgdSeqno = "";
        sqlCmd = "select max(lgd_seqno) lgd_seqno ";
        sqlCmd += "from col_lgd_901 ";
//        sqlCmd += "where id_corp_no = ? ";
        sqlCmd += "where id_corp_p_seqno = ? ";
//        sqlCmd += "and lgd_seqno <> '' ";
        sqlCmd += "and decode(lgd_seqno,'','x',lgd_seqno) <> 'x' ";
        sqlCmd += "and decode(close_flag,'','x',close_flag)<>'Y' ";
//        setString(1, h_lgd1_id_corp_no);
        setString(1, hLgd1IdCorpPSeqno);

        extendField = "lgd_seqno.";
        
        if (selectTable() > 0) {
            hLgd1LgdSeqno = getValue("lgd_seqno.lgd_seqno");
        }
    }
    
 // ************************************************************************
    private void getChiName() throws Exception {
    	hAcnoChiName = "";
        sqlCmd = "select uf_acno_name(cast(? as varchar(10))) chi_name from dual ";
        setString(1, hLgd1AcctPSeqno);
        
        extendField = "getChiName.";

        if (selectTable() > 0) {
        	hAcnoChiName = getValue("getChiName.chi_name");
        }
    }

    // ************************************************************************
}