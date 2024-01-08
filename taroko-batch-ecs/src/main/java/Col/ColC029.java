/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/08/30  V1.00.00   PhoPho     program initial                           *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC029 extends AccessDAO {
    private String progname = "個別協商分期環款帳戶狀態設定處理程式 109/12/15  V1.00.01 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hCcrdPSeqno = "";
    String hCcrdInstFlag = "";
    String hCcrdInstRate = "";
    String hCcrdRowid = "";
    String hAcnoIdPSeqno = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctStatus = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoStatUnprintFlag = "";
    String hAcnoStatUnprintSMonth = "";
    String hAcnoStatUnprintEMonth = "";
    String hAcnoNoDelinquentFlag = "";
    String hAcnoNoDelinquentSDate = "";
    String hAcnoNoDelinquentEDate = "";
    String hAcnoNoCollectionFlag = "";
    String hAcnoNoCollectionSDate = "";
    String hAcnoNoCollectionEDate = "";
    String hAcnoNoPenaltyFlag = "";
    String hAcnoNoPenaltySMonth = "";
    String hAcnoNoPenaltyEMonth = "";
    String hAcnoNoTelCollFlag = "";
    String hAcnoNoTelCollSDate = "";
    String hAcnoNoTelCollEDate = "";
    String hAcnoNoSmsFlag = "";
    String hAcnoNoSmsSDate = "";
    String hAcnoNoSmsEDate = "";
    String hAcnoRevolveReason = "";
    String hAcnoRevolveReason2 = "";
    String hAcnoRevolveRateSMonth = "";
    String hAcnoRevolveRateEMonth = "";
    String hAcnoNoautoBalanceFlag = "";
    String hAcnoNoautoBalanceDate1 = "";
    String hAcnoNoautoBalanceDate2 = "";
    String hAcnoRowid = "";
    String hWdayThisAcctMonth = "";
    String hWdayNextAcctMonth = "";
    double hAcnoRevolveIntRate = 0;
    String hClbmRevolveRateFlag = "";
    String hClbmStatUnprintFlag = "";
    String hClbmNoTelCollFlag = "";
    String hClbmNoDelinquentFlag = "";
    String hClbmNoCollectionFlag = "";
    String hClbmNoPenaltyFlag = "";
    String hClbmNoSmsFlag = "";
    String hClbmNoautoBalanceFlag = "";
    String hAcnoRevolveIntSign = "";
    double hAcnoRevolveIntRate2 = 0;
    String hAcnoRevolveIntSign2 = "";
    String hAcnoRevolveRateSMonth2 = "";
    String hAcnoRevolveRateEMonth2 = "";
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
            if (args.length > 2) {
                comc.errExit("Usage : ColC029 [business_date] [callbatch_seqno]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);
            

            selectPtrBusinday();

            if (args.length >= 1 && args[0].length() == 8)
                hBusiBusinessDate = args[0];

            selectColCsRemod();

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
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
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        hBusiBusinessDate = getValue("business_date");

    }

    /***********************************************************************/
    void selectColCsRemod() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "p_seqno,";
        sqlCmd += "inst_flag,";
        sqlCmd += "inst_rate,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_cs_remod ";
        sqlCmd += "where proc_flag = '0' ";
        sqlCmd += " order by mod_time,inst_flag ";

        openCursor();
        while (fetchTable()) {
            hCcrdPSeqno = getValue("p_seqno");
            hCcrdInstFlag = getValue("inst_flag");
            hCcrdInstRate = getValue("inst_rate");
            hCcrdRowid = getValue("rowid");

            selectColLiabParam();
            selectActAcno();

            updateColCsRemod();
            commitDataBase();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectColLiabParam() throws Exception {
        hClbmStatUnprintFlag = "";
        hClbmNoTelCollFlag = "";
        hClbmNoDelinquentFlag = "";
        hClbmNoCollectionFlag = "";
        hClbmRevolveRateFlag = "";
        hClbmNoPenaltyFlag = "";
        hClbmNoSmsFlag = "";
        hClbmNoautoBalanceFlag = "";

        sqlCmd = "select stat_unprint_flag,";
        sqlCmd += "no_tel_coll_flag,";
        sqlCmd += "no_delinquent_flag,";
        sqlCmd += "no_collection_flag,";
        sqlCmd += "revolve_rate_flag,";
        sqlCmd += "no_penalty_flag,";
        sqlCmd += "no_sms_flag,";
        sqlCmd += "noauto_balance_flag ";
        sqlCmd += " from col_liab_param ";
        sqlCmd += "where apr_date <> '' ";
        sqlCmd += "and liab_status = ?  ";
        sqlCmd += "and liab_type = '5' ";
        setString(1, hCcrdInstFlag);
        if (selectTable() > 0) {
            hClbmStatUnprintFlag = getValue("stat_unprint_flag");
            hClbmNoTelCollFlag = getValue("no_tel_coll_flag");
            hClbmNoDelinquentFlag = getValue("no_delinquent_flag");
            hClbmNoCollectionFlag = getValue("no_collection_flag");
            hClbmRevolveRateFlag = getValue("revolve_rate_flag");
            hClbmNoPenaltyFlag = getValue("no_penalty_flag");
            hClbmNoSmsFlag = getValue("no_sms_flag");
            hClbmNoautoBalanceFlag = getValue("noauto_balance_flag");
        }

    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.id_p_seqno,";
//        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "decode(a.stat_unprint_flag,'','N',a.stat_unprint_flag) stat_unprint_flag,";
        sqlCmd += "decode(a.stat_unprint_s_month,'','000000',a.stat_unprint_s_month) stat_unprint_s_month,";
        sqlCmd += "decode(a.stat_unprint_e_month,'','999999',a.stat_unprint_e_month) stat_unprint_e_month,";
        sqlCmd += "decode(a.no_delinquent_flag,'','N',a.no_delinquent_flag) no_delinquent_flag,";
        sqlCmd += "decode(a.no_delinquent_s_date,'','00000000',a.no_delinquent_s_date) no_delinquent_s_date,";
        sqlCmd += "decode(a.no_delinquent_e_date,'','99999999',a.no_delinquent_e_date) no_delinquent_e_date,";
        sqlCmd += "decode(a.no_collection_flag,'','N',a.no_collection_flag) no_collection_flag,";
        sqlCmd += "decode(a.no_collection_s_date,'','00000000',a.no_collection_s_date) no_collection_s_date,";
        sqlCmd += "decode(a.no_collection_e_date,'','99999999',a.no_collection_e_date) no_collection_e_date,";
        sqlCmd += "decode(a.no_penalty_flag,'','N',a.no_penalty_flag) no_penalty_flag,";
        sqlCmd += "decode(a.no_penalty_s_month,'','000000',a.no_penalty_s_month) no_penalty_s_month,";
        sqlCmd += "decode(a.no_penalty_e_month,'','999999',a.no_penalty_e_month) no_penalty_e_month,";
        sqlCmd += "decode(a.no_tel_coll_flag,'','N',a.no_tel_coll_flag) no_tel_coll_flag,";
        sqlCmd += "decode(a.no_tel_coll_s_date,'','00000000',a.no_tel_coll_s_date) no_tel_coll_s_date,";
        sqlCmd += "decode(a.no_tel_coll_e_date,'','99999999',a.no_tel_coll_e_date) no_tel_coll_e_date,";
        sqlCmd += "decode(a.no_sms_flag,'','N',a.no_sms_flag) no_sms_flag,";
        sqlCmd += "decode(a.no_sms_s_date,'','00000000',a.no_sms_s_date) no_sms_s_date,";
        sqlCmd += "decode(a.no_sms_e_date,'','99999999',a.no_sms_e_date) no_sms_e_date,";
        sqlCmd += "a.revolve_reason,";
        sqlCmd += "a.revolve_reason_2,";
        sqlCmd += "decode(a.revolve_rate_s_month,'','000000',a.revolve_rate_s_month) revolve_rate_s_month,";
        sqlCmd += "decode(a.revolve_rate_e_month,'','999999',a.revolve_rate_e_month) revolve_rate_e_month,";
        sqlCmd += "decode(a.noauto_balance_flag,'','N',a.noauto_balance_flag) noauto_balance_flag,";
        sqlCmd += "decode(a.noauto_balance_date1,'','00000000',a.noauto_balance_date1) noauto_balance_date1,";
        sqlCmd += "decode(a.noauto_balance_date2,'','99999999',a.noauto_balance_date2) noauto_balance_date2,";
        sqlCmd += "a.rowid as rowid,";
        sqlCmd += "b.this_acct_month,";
        sqlCmd += "b.next_acct_month,";
        sqlCmd += "round(decode(a.acct_status,'3',c.revolving_interest2,'4',c.revolving_interest2,c.revolving_interest1)";
        sqlCmd += " - ?*100.0/365,3) revolve_int_rate,";
        sqlCmd += "revolve_int_rate_2 ";
        sqlCmd += "from act_acno a,ptr_workday b,ptr_actgeneral_n c ";
//        sqlCmd += "where a.p_seqno = a.gp_no ";
        sqlCmd += "where a.acno_flag <> 'Y' ";
        sqlCmd += "and a.acct_type = c.acct_type ";
        sqlCmd += "and a.stmt_cycle = b.stmt_cycle ";
//        sqlCmd += "and a.p_seqno = ? ";
        sqlCmd += "and a.acno_p_seqno = ? ";
        setString(1, hCcrdInstRate);
        setString(2, hCcrdPSeqno);

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoIdPSeqno = getValue("id_p_seqno", i);
//            h_acno_p_seqno = getValue("p_seqno", i);
            hAcnoPSeqno = getValue("acno_p_seqno", i);
            hAcnoAcctStatus = getValue("acct_status", i);
            hAcnoAcctType = getValue("acct_type", i);
            hAcnoAcctKey = getValue("acct_key", i);
            hAcnoStatUnprintFlag = getValue("stat_unprint_flag", i);
            hAcnoStatUnprintSMonth = getValue("stat_unprint_s_month", i);
            hAcnoStatUnprintEMonth = getValue("stat_unprint_e_month", i);
            hAcnoNoDelinquentFlag = getValue("no_delinquent_flag", i);
            hAcnoNoDelinquentSDate = getValue("no_delinquent_s_date", i);
            hAcnoNoDelinquentEDate = getValue("no_delinquent_e_date", i);
            hAcnoNoCollectionFlag = getValue("no_collection_flag", i);
            hAcnoNoCollectionSDate = getValue("no_collection_s_date", i);
            hAcnoNoCollectionEDate = getValue("no_collection_e_date", i);
            hAcnoNoPenaltyFlag = getValue("no_penalty_flag", i);
            hAcnoNoPenaltySMonth = getValue("no_penalty_s_month", i);
            hAcnoNoPenaltyEMonth = getValue("no_penalty_e_month", i);
            hAcnoNoTelCollFlag = getValue("no_tel_coll_flag", i);
            hAcnoNoTelCollSDate = getValue("no_tel_coll_s_date", i);
            hAcnoNoTelCollEDate = getValue("no_tel_coll_e_date", i);
            hAcnoNoSmsFlag = getValue("no_sms_flag", i);
            hAcnoNoSmsSDate = getValue("no_sms_s_date", i);
            hAcnoNoSmsEDate = getValue("no_sms_e_date", i);
            hAcnoRevolveReason = getValue("revolve_reason", i);
            hAcnoRevolveReason2 = getValue("revolve_reason_2", i);
            hAcnoRevolveRateSMonth = getValue("revolve_rate_s_month", i);
            hAcnoRevolveRateEMonth = getValue("revolve_rate_e_month", i);
            hAcnoNoautoBalanceFlag = getValue("noauto_balance_flag", i);
            hAcnoNoautoBalanceDate1 = getValue("noauto_balance_date1", i);
            hAcnoNoautoBalanceDate2 = getValue("noauto_balance_date2", i);
            hAcnoRowid = getValue("rowid", i);
            hWdayThisAcctMonth = getValue("this_acct_month", i);
            hWdayNextAcctMonth = getValue("next_acct_month", i);
            hAcnoRevolveIntRate = getValueDouble("revolve_int_rate", i);
            hAcnoRevolveIntRate2 = getValueDouble("revolve_int_rate_2", i);

            totalCnt++;

            if (hClbmStatUnprintFlag.equals("N")) {
                if (hAcnoStatUnprintFlag.equals("Y")) {
                    if (hWdayThisAcctMonth.compareTo(hAcnoStatUnprintSMonth) < 0) {
                        hAcnoStatUnprintFlag = "N";
                        hAcnoStatUnprintSMonth = "";
                        hAcnoStatUnprintEMonth = "";
                    } else {
                        if (hWdayThisAcctMonth.compareTo(hAcnoStatUnprintEMonth) < 0)
                            hAcnoStatUnprintEMonth = hWdayThisAcctMonth;
                    }
                }
            } else if (hClbmStatUnprintFlag.equals("Y")) {
                hAcnoStatUnprintFlag = "Y";
                hAcnoStatUnprintSMonth = hWdayThisAcctMonth;
                hAcnoStatUnprintEMonth = "";
            }

            if (hClbmNoautoBalanceFlag.equals("N")) {
                if (hAcnoNoautoBalanceFlag.equals("Y")) {
                    if ((hBusiBusinessDate.compareTo(hAcnoNoautoBalanceDate1) < 0)
                            || (hBusiBusinessDate.compareTo(hAcnoNoautoBalanceDate2) > 0)) {
                    } else {
                        hAcnoNoautoBalanceFlag = "N";
                        hAcnoNoautoBalanceDate2 = hBusiBusinessDate;
                        insertActNoautoBalanceLog();
                    }
                }
            } else if (hClbmNoautoBalanceFlag.equals("Y")) {
                if (hAcnoNoautoBalanceFlag.equals("N")) {
                    hAcnoNoautoBalanceFlag = "Y";
                    hAcnoNoautoBalanceDate1 = hBusiBusinessDate;
                    hAcnoNoautoBalanceDate2 = "";
                    insertActNoautoBalanceLog();
                }
            }

            if (hClbmNoDelinquentFlag.equals("N")) {
                if (hAcnoNoDelinquentFlag.equals("Y")) {
                    if (hBusiBusinessDate.compareTo(hAcnoNoDelinquentSDate) < 0) {
                        hAcnoNoDelinquentFlag = "N";
                        hAcnoNoDelinquentSDate = "";
                        hAcnoNoDelinquentEDate = "";
                    } else {
                        if (hBusiBusinessDate.compareTo(hAcnoNoDelinquentEDate) < 0)
                            hAcnoNoDelinquentEDate = hBusiBusinessDate;
                    }
                }
            } else if (hClbmNoDelinquentFlag.equals("Y")) {
                hAcnoNoDelinquentFlag = "Y";
                hAcnoNoDelinquentSDate = hBusiBusinessDate;
                hAcnoNoDelinquentEDate = "";
            }

            if (hClbmNoCollectionFlag.equals("N")) {
                if (hAcnoNoCollectionFlag.equals("Y")) {
                    if (hBusiBusinessDate.compareTo(hAcnoNoCollectionSDate) < 0) {
                        hAcnoNoCollectionFlag = "N";
                        hAcnoNoCollectionSDate = "";
                        hAcnoNoCollectionEDate = "";
                    } else {
                        if (hBusiBusinessDate.compareTo(hAcnoNoCollectionEDate) < 0)
                            hAcnoNoCollectionEDate = hBusiBusinessDate;
                    }
                }
            } else if (hClbmNoCollectionFlag.equals("Y")) {
                hAcnoNoCollectionFlag = "Y";
                hAcnoNoCollectionSDate = hBusiBusinessDate;
                hAcnoNoCollectionEDate = "";
            }

            if (hClbmNoPenaltyFlag.equals("N")) {
                if (hAcnoNoPenaltyFlag.equals("Y")) {
                    if (hWdayThisAcctMonth.compareTo(hAcnoNoPenaltySMonth) < 0) {
                        hAcnoNoPenaltyFlag = "N";
                        hAcnoNoPenaltySMonth = "";
                        hAcnoNoPenaltyEMonth = "";
                    } else {
                        if (hWdayThisAcctMonth.compareTo(hAcnoNoPenaltyEMonth) < 0)
                            hAcnoNoPenaltyEMonth = hWdayThisAcctMonth;
                    }
                }
            } else if (hClbmNoPenaltyFlag.equals("Y")) {
                hAcnoNoPenaltyFlag = "Y";
                hAcnoNoPenaltySMonth = hWdayThisAcctMonth;
                hAcnoNoPenaltyEMonth = "";
            }

            if (hClbmNoTelCollFlag.equals("N")) {
                if (hAcnoNoTelCollFlag.equals("Y")) {
                    if (hBusiBusinessDate.compareTo(hAcnoNoTelCollSDate) < 0) {
                        hAcnoNoTelCollFlag = "N";
                        hAcnoNoTelCollSDate = "";
                        hAcnoNoTelCollEDate = "";
                    } else {
                        if (hBusiBusinessDate.compareTo(hAcnoNoTelCollEDate) < 0)
                            hAcnoNoTelCollEDate = hBusiBusinessDate;
                    }
                }
            } else if (hClbmNoTelCollFlag.equals("Y")) {
                hAcnoNoTelCollFlag = "Y";
                hAcnoNoTelCollSDate = hBusiBusinessDate;
                hAcnoNoTelCollEDate = "";
            }

            if (hClbmNoSmsFlag.equals("N")) {
                if (hAcnoNoSmsFlag.equals("Y")) {
                    if (hBusiBusinessDate.compareTo(hAcnoNoSmsSDate) < 0) {
                        hAcnoNoSmsFlag = "N";
                        hAcnoNoSmsSDate = "";
                        hAcnoNoSmsEDate = "";
                    } else {
                        if (hBusiBusinessDate.compareTo(hAcnoNoSmsEDate) < 0)
                            hAcnoNoSmsEDate = hBusiBusinessDate;
                    }
                }
            } else if (hClbmNoSmsFlag.equals("Y")) {
                hAcnoNoSmsFlag = "Y";
                hAcnoNoSmsSDate = hBusiBusinessDate;
                hAcnoNoSmsEDate = "";
            }

            if (hClbmRevolveRateFlag.equals("N")) {
                hAcnoRevolveIntRate2 = 0;
                hAcnoRevolveReason2 = "L";
                hAcnoRevolveIntSign2 = "";
                hAcnoRevolveRateSMonth2 = "";
                hAcnoRevolveRateEMonth2 = "";
                if (!hAcnoRevolveRateSMonth.equals("000000")) {
                    if (hWdayThisAcctMonth.compareTo(hAcnoRevolveRateSMonth) < 0) {
                        hAcnoRevolveIntRate = 0;
                        hAcnoRevolveReason = "L";
                        hAcnoRevolveIntSign = "";
                        hAcnoRevolveRateSMonth = "";
                        hAcnoRevolveRateEMonth = "";
                    } else {
                        if (hWdayThisAcctMonth.compareTo(hAcnoRevolveRateEMonth) < 0)
                            hAcnoRevolveRateEMonth = hWdayThisAcctMonth;
                    }
                }
            } else if (hClbmRevolveRateFlag.equals("Y")) {
                hAcnoRevolveRateSMonth = hWdayNextAcctMonth;
                hAcnoRevolveRateEMonth = "";
                hAcnoRevolveReason = "L";
                hAcnoRevolveReason2 = "L";
                hAcnoRevolveIntRate2 = 0;
                hAcnoRevolveIntSign2 = "";
                hAcnoRevolveRateSMonth2 = "";
                hAcnoRevolveRateEMonth2 = "";
            }
            updateActAcno();
        }
    }

    /***********************************************************************/
    void insertActNoautoBalanceLog() throws Exception {
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("acct_type", hAcnoAcctType);
        setValue("p_seqno", hAcnoPSeqno);
        setValue("modify_type", "2");
        setValue("noauto_balance_flag", hClbmNoautoBalanceFlag);
        setValue("noauto_balance_date1", hAcnoNoautoBalanceDate1);
        setValue("noauto_balance_date2", hAcnoNoautoBalanceDate2);
        setValue("remark", "個別協商");
        setValue("crt_user", javaProgram);
        setValue("apr_user", "SYSTEM");
        setValue("apr_date", hBusiBusinessDate);
        setValue("apr_flag", "Y");
        setValue("mod_user", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "act_noauto_balance_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_noauto_balance_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcno() throws Exception {
    	daoTable = "act_acno";
        updateSQL = "no_delinquent_flag     = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += " no_delinquent_s_date = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += " no_delinquent_e_date = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += " stat_unprint_flag    = decode(cast(? as varchar(6)),'000000','N','Y'),";
        updateSQL += " stat_unprint_s_month = decode(cast(? as varchar(6)),'000000','',cast(? as varchar(6))),";
        updateSQL += " stat_unprint_e_month = decode(cast(? as varchar(6)),'999999','',cast(? as varchar(6))),";
        updateSQL += " no_collection_flag   = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += " no_collection_s_date = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += " no_collection_e_date = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += " no_penalty_flag      = decode(cast(? as varchar(6)),'000000','N','Y'),";
        updateSQL += " no_penalty_s_month   = decode(cast(? as varchar(6)),'000000','',cast(? as varchar(6))),";
        updateSQL += " no_penalty_e_month   = decode(cast(? as varchar(6)),'999999','',cast(? as varchar(6))),";
        updateSQL += " no_tel_coll_flag     = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += " no_tel_coll_s_date   = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += " no_tel_coll_e_date   = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += " no_sms_flag          = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += " no_sms_s_date        = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += " no_sms_e_date        = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += " noauto_balance_flag  = ?,";
        updateSQL += " noauto_balance_date1 = decode(cast(? as varchar(8)),'00000000','',substr(?,1,6)),";
        updateSQL += " noauto_balance_date2 = decode(cast(? as varchar(8)),'99999999','',substr(?,1,6)),";
        updateSQL += " revolve_reason       = ?,";
        updateSQL += " revolve_reason_2     = ?,";
        updateSQL += " revolve_rate_s_month = decode(cast(? as varchar(6)),'000000','',cast(? as varchar(6))),";
        updateSQL += " revolve_rate_e_month = decode(cast(? as varchar(6)),'999999','',cast(? as varchar(6))),";
        updateSQL += " revolve_int_sign     = decode(decode(cast(? as varchar(1)),'Y',cast(? as double),revolve_int_rate),0,'','-'),";
        updateSQL += " revolve_int_rate     = decode(cast(? as varchar(1)),'Y',cast(? as double),decode(cast(? as varchar(8)),'',0,revolve_int_rate)),";
        updateSQL += " revolve_rate_s_month_2 = decode(cast(? as double),0,'',revolve_rate_s_month_2),";
        updateSQL += " revolve_rate_e_month_2 = decode(cast(? as double),0,'',revolve_rate_e_month_2),";
        updateSQL += " revolve_int_sign_2   = decode(cast(? as double),0,'',revolve_int_sign_2),";
        updateSQL += " revolve_int_rate_2   = decode(cast(? as double),0,0,revolve_int_rate_2),";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where rowid = ? ";
        setString(1, hAcnoNoDelinquentSDate);
        setString(2, hAcnoNoDelinquentSDate);
        setString(3, hAcnoNoDelinquentSDate);
        setString(4, hAcnoNoDelinquentEDate);
        setString(5, hAcnoNoDelinquentEDate);
        setString(6, hAcnoStatUnprintSMonth);
        setString(7, hAcnoStatUnprintSMonth);
        setString(8, hAcnoStatUnprintSMonth);
        setString(9, hAcnoStatUnprintEMonth);
        setString(10, hAcnoStatUnprintEMonth);
        setString(11, hAcnoNoCollectionSDate);
        setString(12, hAcnoNoCollectionSDate);
        setString(13, hAcnoNoCollectionSDate);
        setString(14, hAcnoNoCollectionEDate);
        setString(15, hAcnoNoCollectionEDate);
        setString(16, hAcnoNoPenaltySMonth);
        setString(17, hAcnoNoPenaltySMonth);
        setString(18, hAcnoNoPenaltySMonth);
        setString(19, hAcnoNoPenaltyEMonth);
        setString(20, hAcnoNoPenaltyEMonth);
        setString(21, hAcnoNoTelCollSDate);
        setString(22, hAcnoNoTelCollSDate);
        setString(23, hAcnoNoTelCollSDate);
        setString(24, hAcnoNoTelCollEDate);
        setString(25, hAcnoNoTelCollEDate);
        setString(26, hAcnoNoSmsSDate);
        setString(27, hAcnoNoSmsSDate);
        setString(28, hAcnoNoSmsSDate);
        setString(29, hAcnoNoSmsEDate);
        setString(30, hAcnoNoSmsEDate);
        setString(31, hAcnoNoautoBalanceFlag);
        setString(32, hAcnoNoautoBalanceDate1);
        setString(33, hAcnoNoautoBalanceDate1);
        setString(34, hAcnoNoautoBalanceDate2);
        setString(35, hAcnoNoautoBalanceDate2);
        setString(36, hAcnoRevolveReason);
        setString(37, hAcnoRevolveReason2);
        setString(38, hAcnoRevolveRateSMonth);
        setString(39, hAcnoRevolveRateSMonth);
        setString(40, hAcnoRevolveRateEMonth);
        setString(41, hAcnoRevolveRateEMonth);
        setString(42, hClbmRevolveRateFlag);
        setDouble(43, hAcnoRevolveIntRate);
        setString(44, hClbmRevolveRateFlag);
        setDouble(45, hAcnoRevolveIntRate);
        setString(46, hAcnoRevolveRateSMonth);
        setDouble(47, hAcnoRevolveIntRate2);
        setDouble(48, hAcnoRevolveIntRate2);
        setDouble(49, hAcnoRevolveIntRate2);
        setDouble(50, hAcnoRevolveIntRate2);
        setString(51, javaProgram);
        setRowId(52, hAcnoRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateColCsRemod() throws Exception {
        daoTable = "col_cs_remod";
        updateSQL = "proc_date  = ?,";
        updateSQL += " proc_flag  = 'Y',";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where rowid   = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hCcrdRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_cs_remod not found!", "", comcr.hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC029 proc = new ColC029();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
