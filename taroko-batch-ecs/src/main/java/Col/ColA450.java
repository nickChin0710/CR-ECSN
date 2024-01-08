/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/17  V1.00.00    phopho     program initial                          *
*  109/01/14  V1.00.01  phopho     fix update_cca_card_acct()                 *
*  109/02/10  V1.00.02  phopho     Mantis 0002572: change logic               *
*  109/03/31  V1.00.03  phopho     Mantis 0002572: change logic               *
*  109/12/10  V1.00.04    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColA450 extends AccessDAO {
    private String progname = "前置協商帳戶狀態設定處理程式 109/12/10  V1.00.04  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";
    String hBusiBusinessMonth = "";
    String hBusiTempProcMonth = "";

    String hWdayThisAcctMonth = "";
    String hClbmStatUnprintFlag = "";
    String hClbmNoTelCollFlag = "";
    String hClbmNoDelinquentFlag = "";
    String hClbmNoCollectionFlag = "";
    String hClbmNoFStopFlag = "";
    String hClbmRevolveRateFlag = "";
    String hClbmNoPenaltyFlag = "";
//    String h_clbm_no_interest_flag = "";
    String hClbmNoSmsFlag = "";
    String hClbmMinPayFlag = "";
    String hClbmAutopayFlag = "";
    String hClbmPayStageFlag = "";
    String hClbmPayStageMark = "";
    String hClbmBlockFlag = "";
    String hClbmBlockMark1 = "";
    String hClbmBlockMark3 = "";
    String hClbmSendCsFlag = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoAcctHolderId = "";
    String hAcnoIdPSeqno = "";
    String hAcnoBlockStatus = "";
    String hAcnoBlockReason = "";
    String hAcnoBlockReason2 = "";
    String hAcnoNoPenaltyFlag = "";
    String hAcnoNoPenaltySMonth = "";
    String hAcnoNoPenaltyEMonth = "";
//    String h_acno_no_interest_flag = "";
//    String h_acno_no_interest_s_month = "";
//    String h_acno_no_interest_e_month = "";
    String hAcnoStatUnprintFlag = "";
    String hAcnoStatUnprintSMonth = "";
    String hAcnoStatUnprintEMonth = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoNoTelCollFlag = "";
    String hAcnoNoTelCollSDate = "";
    String hAcnoNoTelCollEDate = "";
    String hAcnoNoPerCollFlag = "";
    String hAcnoNoPerCollSDate = "";
    String hAcnoNoPerCollEDate = "";
    String hAcnoNoDelinquentFlag = "";
    String hAcnoNoDelinquentSDate = "";
    String hAcnoNoDelinquentEDate = "";
    String hAcnoNoCollectionFlag = "";
    String hAcnoNoCollectionSDate = "";
    String hAcnoNoCollectionEDate = "";
    String hAcnoNoFStopFlag = "";
    String hAcnoNoFStopSDate = "";
    String hAcnoNoFStopEDate = "";
    String hAcnoRevolveIntSign = "";
    double hAcnoRevolveIntRate = 0;
    String hAcnoRevolveRateSMonth = "";
    String hAcnoRevolveRateEMonth = "";
    String hAcnoAutopayAcctSDate = "";
    String hAcnoAutopayAcctEDate = "";
    String hAcnoCardIndicator = "";
    String hAcnoNoSmsFlag = "";
    String hAcnoNoSmsSDate = "";
    String hAcnoNoSmsEDate = "";
    String hAcnoRowid = "";
    String hCardCardNo = "";
    String hCardId = "";
    String hCardIdPSeqno = "";
    String hCardBlockStatus = "";
    String hCardBlockReason = "";
    String hCardBlockReason2 = "";
    String hCardRowid = "";

    String hClrdIdPSeqno = "";
    String hClrdId = "";
    String hClrdEndDate = "";
    String hClrdLiacStatus = "";
    String hClrdLiacSeqno = "";
    double hClrdLiacIntRate = 0;
    String hClrdRmrecolFlag = "";
    String hClrdProcFlag = "";
    String hClrdRowid = "";
    String hClbmDBalFlag = "";
    String hAcnoRevolveReason = "";
    String hAcnoRevolveReason2 = "";
    String hAcnoNoautoBalanceFlag = "";
    String hAcnoNoautoBalanceDate1 = "";
    String hAcnoNoautoBalanceDate2 = "";
    String hWdayNextAcctMonth = "";
    double hAcnoRevolveIntRate2 = 0;
    String hClbmNoautoBalanceFlag = "";
    String hClbmOppostFlag = "";
    String hClbmOppostReason = "";
    String hAcnoRevolveIntSign2 = "";
    String hAcnoRevolveRateSMonth2 = "";
    String hAcnoRevolveRateEMonth2 = "";
    String hCardMajorIdPSeqno = "";
    String hCardComboIndicator = "";
    String hStopProcSeqno = "";
    String hCdjcRowid = "";
    String hAcnoRcUseIndicator = "";
    String hApscRowid = "";
    String hIdnoIdPSeqno = "";
    String hIdnoBirthday = "";
    String hIdnoChiName = "";
    String hApscPmBirthday = "";
    String hApscPmName = "";
    String hApscSupBirthday = "";
    String hApscSupName = "";
    String hClnoInterestBaseDate = "";
    String hClnoContractDate = "";
    String hClnoEndReason = "";
    String hClctRowid = "";
    String hClnoCreditCardFlag = "";
    double hCcdtInEndBalNew = 0;
    double hCcdtOutEndBalNew = 0;
    String hWdayTempAcctMonth = "";

    int totalCnt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
        	dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }

            // 檢查參數
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ColA450", "");
            }
        	
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            selectPtrBusinday();

            selectColLiacRemod();

            showLogMessage("I", "", "總計執行筆數 : [" + totalCnt + "]");

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
        hBusiBusinessMonth = "";
        hBusiTempProcMonth = "";
        sqlCmd = "select business_date, ";
        sqlCmd += "to_char(to_date(business_date,'yyyymmdd'),'yyyymm') business_month, ";
        sqlCmd += "to_char(add_months(to_date(business_date,'yyyymmdd'),2),'yyyymm') temp_proc_month ";
        sqlCmd += "from ptr_businday ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hBusiBusinessMonth = getValue("business_month");
            hBusiTempProcMonth = getValue("temp_proc_month");
        }
    }

    /***********************************************************************/
    void selectColLiacRemod() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_p_seqno, ";  //以 id_p_seqno 為主
        sqlCmd += "id_no, ";
        sqlCmd += "end_date, ";
        sqlCmd += "liac_status, ";
        sqlCmd += "liac_seqno, ";
        sqlCmd += "liac_int_rate, ";
        sqlCmd += "decode(rmrecol_flag,'','N',rmrecol_flag) as rmrecol_flag, ";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_liac_remod ";
        sqlCmd += "where proc_flag = 'N' ";
        sqlCmd += "order by mod_time,liac_status ";

        openCursor();
        while (fetchTable()) {
        	hClrdIdPSeqno = getValue("id_p_seqno");
            hClrdId = getValue("id_no");
            hClrdEndDate = getValue("end_date");
            hClrdLiacStatus = getValue("liac_status");
            hClrdLiacSeqno = getValue("liac_seqno");
            hClrdLiacIntRate = getValueDouble("liac_int_rate");
            hClrdRmrecolFlag = getValue("rmrecol_flag");
            hClrdProcFlag = "";
            hClrdRowid = getValue("rowid");

            selectColLiabParam();
            if ((hClbmDBalFlag.equals("Y")) && (hClrdLiacStatus.equals("3"))
                    && (hClrdRmrecolFlag.equals("N")))
                selectColLiacNego();

            if ((hClbmDBalFlag.equals("Y")) && (hClrdLiacStatus.equals("3")))
                hClrdProcFlag = "1";

//Tcb無合約檔，不檢核
//            if (hClrdLiacStatus.equals("3"))
//                if (selectColLiacContract() != 0)
//                    if (selectColLiacDebt() != 0) {
//                        showLogMessage("I", "", "id[" + hClrdId + "] 無合約資料");
//                        exitProgram(1);
//                    }

            // 因應前置協商新需求，人工結案，結案原因點選【F3】時，自動執行D檔。 2018.3.13
            if (hClrdLiacStatus.equals("6")) {  // TCB 5改6
                selectColLiacNego1();
                if (hClnoEndReason.equals("F3"))
                    hClrdProcFlag = "1";
            }

            if (hClrdLiacStatus.equals("6")) {  // TCB 5改6
                insertColLiacNegoHst();
                deleteColLiacNego();
            }
            selectActAcno();
            updateColLiacRemod();
            commitDataBase();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.id_p_seqno, ";
        sqlCmd += "a.acno_p_seqno, ";
        sqlCmd += "a.acct_status, ";
        sqlCmd += "a.acct_type, ";
        sqlCmd += "a.acct_key, ";
        sqlCmd += "decode(a.no_delinquent_flag,'','N',a.no_delinquent_flag) no_delinquent_flag, ";
        sqlCmd += "decode(a.no_delinquent_s_date,'','00000000',a.no_delinquent_s_date) no_delinquent_s_date, ";
        sqlCmd += "decode(a.no_delinquent_e_date,'','99999999',a.no_delinquent_e_date) no_delinquent_e_date, ";
        sqlCmd += "decode(a.no_collection_flag,'','N',a.no_collection_flag) no_collection_flag, ";
        sqlCmd += "decode(a.no_collection_s_date,'','00000000',a.no_collection_s_date) no_collection_s_date, ";
        sqlCmd += "decode(a.no_collection_e_date,'','99999999',a.no_collection_e_date) no_collection_e_date, ";
        sqlCmd += "decode(a.no_penalty_flag,'','N',a.no_penalty_flag) no_penalty_flag, ";
        sqlCmd += "decode(a.no_penalty_s_month,'','000000',a.no_penalty_s_month) no_penalty_s_month, ";
        sqlCmd += "decode(a.no_penalty_e_month,'','999999',a.no_penalty_e_month) no_penalty_e_month, ";
        //phopho add
//        sqlCmd += "decode(a.no_interest_flag,'','N',a.no_interest_flag) no_interest_flag, ";
//        sqlCmd += "decode(a.no_interest_s_month,'','000000',a.no_interest_s_month) no_interest_s_month, ";
//        sqlCmd += "decode(a.no_interest_e_month,'','999999',a.no_interest_e_month) no_interest_e_month, ";
        //phopho add end
        sqlCmd += "decode(a.stat_unprint_flag,'','N',a.stat_unprint_flag) stat_unprint_flag, ";
        sqlCmd += "decode(a.stat_unprint_s_month,'','000000',a.stat_unprint_s_month) stat_unprint_s_month, ";
        sqlCmd += "decode(a.stat_unprint_e_month,'','999999',a.stat_unprint_e_month) stat_unprint_e_month, ";
        sqlCmd += "decode(a.no_tel_coll_flag,'','N',a.no_tel_coll_flag) no_tel_coll_flag, ";
        sqlCmd += "decode(a.no_tel_coll_s_date,'','00000000',a.no_tel_coll_s_date) no_tel_coll_s_date, ";
        sqlCmd += "decode(a.no_tel_coll_e_date,'','99999999',a.no_tel_coll_e_date) no_tel_coll_e_date, ";
        sqlCmd += "decode(a.no_per_coll_flag,'','N',a.no_per_coll_flag) no_per_coll_flag, ";
        sqlCmd += "decode(a.no_per_coll_s_date,'','00000000',a.no_per_coll_s_date) no_per_coll_s_date, ";
        sqlCmd += "decode(a.no_per_coll_e_date,'','99999999',a.no_per_coll_e_date) no_per_coll_e_date, ";
        sqlCmd += "decode(a.no_sms_flag,'','N',a.no_sms_flag) no_sms_flag, ";
        sqlCmd += "decode(a.no_sms_s_date,'','00000000',a.no_sms_s_date) no_sms_s_date, ";
        sqlCmd += "decode(a.no_sms_e_date,'','99999999',a.no_sms_e_date) no_sms_e_date, ";
        sqlCmd += "decode(a.no_f_stop_flag,'','N',a.no_f_stop_flag) no_f_stop_flag, ";
        sqlCmd += "decode(a.no_f_stop_s_date,'','00000000',a.no_f_stop_s_date) no_f_stop_s_date, ";
        sqlCmd += "decode(a.no_f_stop_e_date,'','99999999',a.no_f_stop_e_date) no_f_stop_e_date, ";
        sqlCmd += "a.revolve_reason, ";
        sqlCmd += "a.revolve_reason_2, ";
        sqlCmd += "decode(a.revolve_rate_s_month,'','000000',a.revolve_rate_s_month) revolve_rate_s_month, ";
        sqlCmd += "decode(a.revolve_rate_e_month,'','999999',a.revolve_rate_e_month) revolve_rate_e_month, ";
        sqlCmd += "decode(a.autopay_acct_s_date,'','00000000',a.autopay_acct_s_date) autopay_acct_s_date, ";
        sqlCmd += "decode(a.autopay_acct_e_date,'','99999999',a.autopay_acct_e_date) autopay_acct_e_date, ";
        sqlCmd += "a.pay_by_stage_flag, ";
        sqlCmd += "a.card_indicator, ";
        sqlCmd += "e.block_reason1 block_reason, ";
        sqlCmd += "e.block_reason2||e.block_reason3||e.block_reason4||e.block_reason5 block_reason2, ";
        sqlCmd += "uf_idno_id(a.id_p_seqno) acct_holder_id, ";
        sqlCmd += "decode(a.noauto_balance_flag,'','N',a.noauto_balance_flag) noauto_balance_flag, ";
        sqlCmd += "decode(a.noauto_balance_date1,'','00000000',a.noauto_balance_date1) noauto_balance_date1, ";
        sqlCmd += "decode(a.noauto_balance_date2,'','99999999',a.noauto_balance_date2) noauto_balance_date2, ";
        sqlCmd += "a.rowid as rowid, ";
        sqlCmd += "b.this_acct_month, ";
        sqlCmd += "b.next_acct_month, ";
        sqlCmd += "round(decode(a.acct_status,'3',c.revolving_interest2,'4',";
        sqlCmd += "c.revolving_interest2,c.revolving_interest1) - ? *100.0/365,3) revolve_int_rate, ";
        sqlCmd += "revolve_int_rate_2, ";
        sqlCmd += "to_char(add_months(to_date(b.this_acct_month,'yyyymm'),2),'yyyymm') temp_acct_month ";
        sqlCmd += "from act_acno a, ptr_workday b, ptr_actgeneral_n c ";
        sqlCmd += "  left join cca_card_acct e on a.acno_p_seqno = e.acno_p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";  //find block_reason in cca_card_acct
        sqlCmd += "where a.acno_flag <> 'Y' ";
        sqlCmd += "and   a.acct_type  = c.acct_type ";
        sqlCmd += "and   a.stmt_cycle = b.stmt_cycle ";
        sqlCmd += "and   a.id_p_seqno = ? ";
        setDouble(1, hClrdLiacIntRate);
        setString(2, hClrdIdPSeqno);
        
        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoIdPSeqno = getValue("act_acno.id_p_seqno", i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno", i);
            hAcnoAcctStatus = getValue("act_acno.acct_status", i);
            hAcnoAcctType = getValue("act_acno.acct_type", i);
            hAcnoAcctKey = getValue("act_acno.acct_key", i);
            hAcnoNoDelinquentFlag = getValue("act_acno.no_delinquent_flag", i);
            hAcnoNoDelinquentSDate = getValue("act_acno.no_delinquent_s_date", i);
            hAcnoNoDelinquentEDate = getValue("act_acno.no_delinquent_e_date", i);
            hAcnoNoCollectionFlag = getValue("act_acno.no_collection_flag", i);
            hAcnoNoCollectionSDate = getValue("act_acno.no_collection_s_date", i);
            hAcnoNoCollectionEDate = getValue("act_acno.no_collection_e_date", i);
            hAcnoNoPenaltyFlag = getValue("act_acno.no_penalty_flag", i);
            hAcnoNoPenaltySMonth = getValue("act_acno.no_penalty_s_month", i);
            hAcnoNoPenaltyEMonth = getValue("act_acno.no_penalty_e_month", i);
//            h_acno_no_interest_flag = getValue("act_acno.no_interest_flag", i);
//            h_acno_no_interest_s_month = getValue("act_acno.no_interest_s_month", i);
//            h_acno_no_interest_e_month = getValue("act_acno.no_interest_e_month", i);
            hAcnoStatUnprintFlag = getValue("act_acno.stat_unprint_flag", i);
            hAcnoStatUnprintSMonth = getValue("act_acno.stat_unprint_s_month", i);
            hAcnoStatUnprintEMonth = getValue("act_acno.stat_unprint_e_month", i);
            hAcnoNoTelCollFlag = getValue("act_acno.no_tel_coll_flag", i);
            hAcnoNoTelCollSDate = getValue("act_acno.no_tel_coll_s_date", i);
            hAcnoNoTelCollEDate = getValue("act_acno.no_tel_coll_e_date", i);
            hAcnoNoPerCollFlag = getValue("act_acno.no_per_coll_flag", i);
            hAcnoNoPerCollSDate = getValue("act_acno.no_per_coll_s_date", i);
            hAcnoNoPerCollEDate = getValue("act_acno.no_per_coll_e_date", i);
            hAcnoNoSmsFlag = getValue("act_acno.no_sms_flag", i);
            hAcnoNoSmsSDate = getValue("act_acno.no_sms_s_date", i);
            hAcnoNoSmsEDate = getValue("act_acno.no_sms_e_date", i);
            hAcnoNoFStopFlag = getValue("act_acno.no_f_stop_flag", i);
            hAcnoNoFStopSDate = getValue("act_acno.no_f_stop_s_date", i);
            hAcnoNoFStopEDate = getValue("act_acno.no_f_stop_e_date", i);
            hAcnoRevolveReason = getValue("act_acno.revolve_reason", i);
            hAcnoRevolveReason2 = getValue("act_acno.revolve_reason_2", i);
            hAcnoRevolveRateSMonth = getValue("act_acno.revolve_rate_s_month", i);
            hAcnoRevolveRateEMonth = getValue("act_acno.revolve_rate_e_month", i);
            hAcnoAutopayAcctSDate = getValue("act_acno.autopay_acct_s_date", i);
            hAcnoAutopayAcctEDate = getValue("act_acno.autopay_acct_e_date", i);
            hAcnoPayByStageFlag = getValue("act_acno.pay_by_stage_flag", i);
            hAcnoBlockReason = getValue("act_acno.block_reason", i);
            hAcnoBlockReason2 = getValue("act_acno.block_reason2", i);
            hAcnoCardIndicator = getValue("act_acno.card_indicator", i);
            hAcnoAcctHolderId = getValue("act_acno.acct_holder_id", i);
            hAcnoNoautoBalanceFlag = getValue("act_acno.noauto_balance_flag", i);
            hAcnoNoautoBalanceDate1 = getValue("act_acno.noauto_balance_date1", i);
            hAcnoNoautoBalanceDate2 = getValue("act_acno.noauto_balance_date2", i);
            hAcnoRowid = getValue("act_acno.rowid", i);
            hWdayThisAcctMonth = getValue("act_acno.this_acct_month", i);
            hWdayNextAcctMonth = getValue("act_acno.next_acct_month", i);
            hAcnoRevolveIntRate = getValueDouble("act_acno.revolve_int_rate", i);
            hAcnoRevolveIntRate2 = getValueDouble("act_acno.revolve_int_rate_2", i);
            hWdayTempAcctMonth = getValue("act_acno.temp_acct_month", i);

            totalCnt++;
            // 0
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

            if (hClbmOppostFlag.equals("Y"))
                selectCrdCard1();
            // 1
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
            // 2
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
            // 3
            // Mantis 0002572 modify 2020.3.31
//            當資料為【2-前置協商】之【5-結案結清】狀態之參數設定時，
//            若設定為【N】，
//            若if (h_acno_no_penalty_flag.arr[0]=='Y'){
//              若【h_wday_this_acct_month.arr  + 2個月】小於【h_acno_no_penalty_s_month.arr】，
//                  str2var(h_acno_no_penalty_flag    , "N");
//                  str2var(h_acno_no_penalty_s_month , ECS_NULLSTR);
//                  str2var(h_acno_no_penalty_e_month , ECS_NULLSTR);
//              其他若【h_wday_this_acct_month.arr  + 2個月】小於【h_acno_no_penalty_e_month.arr】，
//              則【h_acno_no_penalty_e_month】設定為【h_wday_this_acct_month.arr + 2個月】。
//            }
            if (hClbmNoPenaltyFlag.equals("N")) {
                if (hAcnoNoPenaltyFlag.equals("Y")) {
                	if (hClrdLiacStatus.equals("6")) {  //狀態【2-前置協商】之【6-結案結清】  //TCB 5改6
                		if (hWdayTempAcctMonth.compareTo(hAcnoNoPenaltySMonth) < 0) {
                            hAcnoNoPenaltyFlag = "N";
                            hAcnoNoPenaltySMonth = "";
                            hAcnoNoPenaltyEMonth = "";
                        } else {
                            if (hWdayTempAcctMonth.compareTo(hAcnoNoPenaltyEMonth) < 0)
                                hAcnoNoPenaltyEMonth = hWdayTempAcctMonth;
                        }
                    } else {
                    	if (hWdayThisAcctMonth.compareTo(hAcnoNoPenaltySMonth) < 0) {
                            hAcnoNoPenaltyFlag = "N";
                            hAcnoNoPenaltySMonth = "";
                            hAcnoNoPenaltyEMonth = "";
                        } else {
                            if (hWdayThisAcctMonth.compareTo(hAcnoNoPenaltyEMonth) < 0)
                                hAcnoNoPenaltyEMonth = hWdayThisAcctMonth;
                        }
                    }
                }
            } else if (hClbmNoPenaltyFlag.equals("Y")) {
                hAcnoNoPenaltyFlag = "Y";
                hAcnoNoPenaltySMonth = hWdayThisAcctMonth;
                hAcnoNoPenaltyEMonth = "";
            }
            
            //phopho mark 2020.3.31 change logic
//            if (h_clbm_no_penalty_flag.equals("N")) {
//                if (h_acno_no_penalty_flag.equals("Y")) {
//                    if (h_wday_this_acct_month.compareTo(h_acno_no_penalty_s_month) < 0) {
//                        h_acno_no_penalty_flag = "N";
//                        h_acno_no_penalty_s_month = "";
//                        h_acno_no_penalty_e_month = "";
//                    } else {
//                        if (h_wday_this_acct_month.compareTo(h_acno_no_penalty_e_month) < 0)
//                            h_acno_no_penalty_e_month = h_wday_this_acct_month;
//                    }
//                }
//            } else if (h_clbm_no_penalty_flag.equals("Y")) {
//                h_acno_no_penalty_flag = "Y";
//                h_acno_no_penalty_s_month = h_wday_this_acct_month;
////                h_acno_no_penalty_e_month = "";
//                
//                //協商狀態為【5-結案結清】的迄月為【起月】+2個月 Mantis 0002572
//                if (h_clrd_liac_status.equals("5")) {
//                	h_acno_no_penalty_e_month = h_wday_temp_acct_month;
//                } else {
//                	h_acno_no_penalty_e_month = "";
//                }
//                
////                //起月:改用[系統月份]  迄月:改用[系統月份+2個月]  //需求確認20191026 phopho
////                h_acno_no_penalty_s_month = h_busi_business_month;
////                h_acno_no_penalty_e_month = h_busi_temp_proc_month;
//            }
            //Mantis 0002572: Mark no_interest_flag
//            //3.5
//            if (h_clbm_no_interest_flag.equals("N")) {
//                if (h_acno_no_interest_flag.equals("Y")) {
//                    if (h_wday_this_acct_month.compareTo(h_acno_no_interest_s_month) < 0) {
//                    	h_acno_no_interest_flag = "N";
//                    	h_acno_no_interest_s_month = "";
//                    	h_acno_no_interest_e_month = "";
//                    } else {
//                        if (h_wday_this_acct_month.compareTo(h_acno_no_interest_e_month) < 0)
//                        	h_acno_no_interest_e_month = h_wday_this_acct_month;
//                    }
//                }
//            } else if (h_clbm_no_interest_flag.equals("Y")) {
//            	//起月:改用[系統月份]  迄月:改用[系統月份+2個月]  //需求確認20191026 phopho
//            	h_acno_no_interest_flag = "Y";
//            	h_acno_no_interest_s_month = h_busi_business_month;
//            	h_acno_no_interest_e_month = h_busi_temp_proc_month;
//            }
            // 4
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
            // 5
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
            // 6
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
            // 7
            if (hClbmNoFStopFlag.equals("N")) {
                if (hAcnoNoFStopFlag.equals("Y")) {
                    if (hBusiBusinessDate.compareTo(hAcnoNoFStopSDate) < 0) {
                        hAcnoNoFStopFlag = "N";
                        hAcnoNoFStopSDate = "";
                        hAcnoNoFStopEDate = "";
                    } else {
                        if (hBusiBusinessDate.compareTo(hAcnoNoFStopEDate) < 0)
                            hAcnoNoFStopEDate = hBusiBusinessDate;
                    }
                }
            } else if (hClbmNoFStopFlag.equals("Y")) {
                hAcnoNoFStopFlag = "Y";
                hAcnoNoFStopSDate = hBusiBusinessDate;
                hAcnoNoFStopEDate = "";
            }
            // 8
            if (hClbmRevolveRateFlag.equals("N")) {
                hAcnoRevolveIntRate2 = 0;
                hAcnoRevolveReason = "K";
                hAcnoRevolveReason2 = "K";
                hAcnoRevolveIntSign2 = "";
                hAcnoRevolveRateSMonth2 = "";
                hAcnoRevolveRateEMonth2 = "";
                if (hAcnoRevolveRateSMonth.compareTo("000000") != 0) {
                    if (hWdayThisAcctMonth.compareTo(hAcnoRevolveRateSMonth) < 0) {
                        hAcnoRevolveIntRate = 0;
                        hAcnoRevolveReason = "K";
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
                hAcnoRevolveReason = "K";
                hAcnoRevolveReason2 = "K";
                hAcnoRevolveIntRate2 = 0;
                hAcnoRevolveIntSign2 = "";
                hAcnoRevolveRateSMonth2 = "";
                hAcnoRevolveRateEMonth2 = "";
            }
            // 9
            if (hClbmAutopayFlag.equals("Y")) {
                if ((hBusiBusinessDate.compareTo(hAcnoAutopayAcctSDate) < 0)
                        || (hAcnoAutopayAcctSDate.compareTo("000000") == 0)) {
                    hAcnoAutopayAcctSDate = hBusiBusinessDate;
                    hAcnoAutopayAcctEDate = hBusiBusinessDate;
                }
            }
            if (hClbmPayStageFlag.equals("Y")) {
                hAcnoPayByStageFlag = hClbmPayStageMark;
            } else {
                if (hAcnoPayByStageFlag.compareTo("NF") == 0)
                    hAcnoPayByStageFlag = "";
            }
            // 10
            hAcnoBlockStatus = "";
            if (hClbmBlockFlag.equals("Y")) {
                if (comc.getSubString(hAcnoBlockReason2, 2, 4).equals(hClbmBlockMark3) == false) {
                    hAcnoBlockReason2 = String.format("%-8.8s", hAcnoBlockReason2);
                    if (hClbmBlockMark3.length() == 0)
                        hClbmBlockMark3 = "  ";
                    hAcnoBlockReason2 = comc.getSubString(hAcnoBlockReason2, 0, 2) + hClbmBlockMark3
                            + comc.getSubString(hAcnoBlockReason2, 4);
                    hAcnoBlockStatus = "12";
                    insertOnbat(0, 3);
                }

                if (hAcnoBlockReason.equals(hClbmBlockMark1) == false) {
                    hAcnoBlockReason = String.format("%-2.2s", hAcnoBlockReason);
                    if (hClbmBlockMark1.length() == 0)
                        hClbmBlockMark1 = "  ";
                    hAcnoBlockReason = hClbmBlockMark1;
                    hAcnoBlockStatus = "12";
                    insertOnbat(0, 1);
                }
                insertRskAcnolog(0);
                selectCrdCard();
                
                //批次修改需求v3.6【col_liab_param.block_flag、col_liab_param.block_mark1、col_liab_param.block_mark3】
                //不再回寫 【act_anco.block_reason、block_reason2】，因為欄位已經不存在。
                //改回寫【cca_card_acct 授權卡戶基本檔】，且若符合回寫邏輯，則直接回寫入對應的欄位。
                //若符合回寫條件，則【col_liab_param.block_mark1】寫入【cca_card_acct.block_reason1】、
                //【col_liab_param.block_mark3】寫入【cca_card_acct.block_reason3】。欄位值均為兩碼長度。
                updateCcaCardAcct();  //2019.10.18 phopho add
            }
            updateActAcno();
        }
    }

	/***********************************************************************/
	void updateActAcno() throws Exception {
        String lsNoDelinquentFlag = "", lsNoDelinquentSDate = "", lsNoDelinquentEDate = "";
        String lsNoCollectionFlag = "", lsNoCollectionSDate = "", lsNoCollectionEDate = "";
        String lsNoPenaltyFlag = "", lsNoPenaltySMonth = "", lsNoPenaltyEMonth = "";
//        String ls_no_interest_flag = "", ls_no_interest_s_month = "", ls_no_interest_e_month = "";
        String lsStatUnprintFlag = "", lsStatUnprintSMonth = "", lsStatUnprintEMonth = "";
        String lsNoTelCollFlag = "", lsNoTelCollSDate = "", lsNoTelCollEDate = "";
        String lsNoSmsFlag = "", lsNoSmsSDate = "", lsNoSmsEDate = "";
        String lsAutopayAcctSDate = "", lsAutopayAcctEDate = "";
        String lsNoFStopFlag = "", lsNoFStopSDate = "", lsNoFStopEDate = "";
        String lsNoautoBalanceDate1 = "", lsNoautoBalanceDate2 = "";
        String lsRevolveRateSMonth = "", lsRevolveRateEMonth = "";

        lsNoDelinquentFlag = hAcnoNoDelinquentSDate.equals("00000000") ? "N" : "Y";
        lsNoDelinquentSDate = hAcnoNoDelinquentSDate.equals("00000000") ? "" : hAcnoNoDelinquentSDate;
        lsNoDelinquentEDate = hAcnoNoDelinquentEDate.equals("99999999") ? "" : hAcnoNoDelinquentEDate;
        lsNoCollectionFlag = hAcnoNoCollectionSDate.equals("00000000") ? "N" : "Y";
        lsNoCollectionSDate = hAcnoNoCollectionSDate.equals("00000000") ? "" : hAcnoNoCollectionSDate;
        lsNoCollectionEDate = hAcnoNoCollectionEDate.equals("99999999") ? "" : hAcnoNoCollectionEDate;
        lsNoPenaltyFlag = hAcnoNoPenaltySMonth.equals("000000") ? "N" : "Y";
        lsNoPenaltySMonth = hAcnoNoPenaltySMonth.equals("000000") ? "" : hAcnoNoPenaltySMonth;
        lsNoPenaltyEMonth = hAcnoNoPenaltyEMonth.equals("999999") ? "" : hAcnoNoPenaltyEMonth;
        //phopho add
//        ls_no_interest_flag = h_acno_no_interest_s_month.equals("000000") ? "N" : "Y";
//        ls_no_interest_s_month = h_acno_no_interest_s_month.equals("000000") ? "" : h_acno_no_interest_s_month;
//        ls_no_interest_e_month = h_acno_no_interest_e_month.equals("999999") ? "" : h_acno_no_interest_e_month;
        //phopho add end
        lsStatUnprintFlag = hAcnoStatUnprintSMonth.equals("000000") ? "N" : "Y";
        lsStatUnprintSMonth = hAcnoStatUnprintSMonth.equals("000000") ? "" : hAcnoStatUnprintSMonth;
        lsStatUnprintEMonth = hAcnoStatUnprintEMonth.equals("999999") ? "" : hAcnoStatUnprintEMonth;
        lsNoTelCollFlag = hAcnoNoTelCollSDate.equals("00000000") ? "N" : "Y";
        lsNoTelCollSDate = hAcnoNoTelCollSDate.equals("00000000") ? "" : hAcnoNoTelCollSDate;
        lsNoTelCollEDate = hAcnoNoTelCollEDate.equals("99999999") ? "" : hAcnoNoTelCollEDate;
        lsNoSmsFlag = hAcnoNoSmsSDate.equals("00000000") ? "N" : "Y";
        lsNoSmsSDate = hAcnoNoSmsSDate.equals("00000000") ? "" : hAcnoNoSmsSDate;
        lsNoSmsEDate = hAcnoNoSmsEDate.equals("99999999") ? "" : hAcnoNoSmsEDate;
        lsAutopayAcctSDate = hAcnoAutopayAcctSDate.equals("00000000") ? "" : hAcnoAutopayAcctSDate;
        lsAutopayAcctEDate = hAcnoAutopayAcctEDate.equals("00000000") ? "" : hAcnoAutopayAcctEDate;
        lsNoFStopFlag = hAcnoNoFStopSDate.equals("00000000") ? "N" : "Y";
        lsNoFStopSDate = hAcnoNoFStopSDate.equals("00000000") ? "" : hAcnoNoFStopSDate;
        lsNoFStopEDate = hAcnoNoFStopEDate.equals("99999999") ? "" : hAcnoNoFStopEDate;
        lsNoautoBalanceDate1 = hAcnoNoautoBalanceDate1.equals("00000000") ? ""
                : hAcnoNoautoBalanceDate1.length() >= 6 ? hAcnoNoautoBalanceDate1.substring(0, 6)
                        : hAcnoNoautoBalanceDate1;
        lsNoautoBalanceDate2 = hAcnoNoautoBalanceDate2.equals("99999999") ? ""
                : hAcnoNoautoBalanceDate2.length() >= 6 ? hAcnoNoautoBalanceDate2.substring(0, 6)
                        : hAcnoNoautoBalanceDate2;
        lsRevolveRateSMonth = hAcnoRevolveRateSMonth.equals("000000") ? "" : hAcnoRevolveRateSMonth;
        lsRevolveRateEMonth = hAcnoRevolveRateEMonth.equals("999999") ? "" : hAcnoRevolveRateEMonth;

		daoTable = "act_acno";
		updateSQL = "pay_by_stage_flag     = ?, ";
		updateSQL += "no_delinquent_flag   = ?,";
		updateSQL += "no_delinquent_s_date = ?,";
		updateSQL += "no_delinquent_e_date = ?,";
		updateSQL += "no_collection_flag   = ?,";
		updateSQL += "no_collection_s_date = ?,";
		updateSQL += "no_collection_e_date = ?,";
		updateSQL += "no_penalty_flag      = ?,";
		updateSQL += "no_penalty_s_month   = ?,";
		updateSQL += "no_penalty_e_month   = ?,";
		//phopho add
//		updateSQL += "no_interest_flag     = ?,";
//		updateSQL += "no_interest_s_month  = ?,";
//		updateSQL += "no_interest_e_month  = ?,";
        //phopho add end
		updateSQL += "stat_unprint_flag    = ?,";
		updateSQL += "stat_unprint_s_month = ?,";
		updateSQL += "stat_unprint_e_month = ?,";
		updateSQL += "no_tel_coll_flag     = ?,";
		updateSQL += "no_tel_coll_s_date   = ?,";
		updateSQL += "no_tel_coll_e_date   = ?,";
		updateSQL += "no_sms_flag          = ?,";
		updateSQL += "no_sms_s_date        = ?,";
		updateSQL += "no_sms_e_date        = ?,";
		updateSQL += "autopay_acct_s_date  = decode(autopay_acct_bank,'','',cast(? as varchar(8))),";
		updateSQL += "autopay_acct_e_date  = decode(autopay_acct_bank,'','',cast(? as varchar(8))),";
		updateSQL += "no_f_stop_flag       = ?,";
		updateSQL += "no_f_stop_s_date     = ?,";
		updateSQL += "no_f_stop_e_date     = ?,";
		updateSQL += "noauto_balance_flag  = ?,";
		updateSQL += "noauto_balance_date1 = ?,";
		updateSQL += "noauto_balance_date2 = ?,";
		updateSQL += "revolve_reason       = ?,";
		updateSQL += "revolve_reason_2     = ?,";
		updateSQL += "revolve_rate_s_month = ?,";
		updateSQL += "revolve_rate_e_month = ?,";
		updateSQL += "revolve_int_sign     = decode(decode(cast(? as varchar(1)),'Y',cast(? as double),";
		updateSQL += "                       revolve_int_rate),0,'','-'),";
		updateSQL += "revolve_int_rate     = decode(cast(? as varchar(1)),'Y',cast(? as double),";
		updateSQL += "                       decode(cast(? as varchar(6)),'',0,";
		updateSQL += "                       revolve_int_rate)),";
		updateSQL += "revolve_rate_s_month_2 = decode(cast(? as double),0,'',revolve_rate_s_month_2),";
		updateSQL += "revolve_rate_e_month_2 = decode(cast(? as double),0,'',revolve_rate_e_month_2),";
		updateSQL += "revolve_int_sign_2   = decode(cast(? as double),0,'',revolve_int_sign_2),";
		updateSQL += "revolve_int_rate_2   = decode(cast(? as double),0,0,revolve_int_rate_2),";
		updateSQL += "mod_time = sysdate, ";
		updateSQL += "mod_pgm  = ? ";
		whereStr = "where rowid = ? ";
		int cnt = 1;
        setString(cnt++, hAcnoPayByStageFlag);
        setString(cnt++, lsNoDelinquentFlag);
        setString(cnt++, lsNoDelinquentSDate);
        setString(cnt++, lsNoDelinquentEDate);
        setString(cnt++, lsNoCollectionFlag);
        setString(cnt++, lsNoCollectionSDate);
        setString(cnt++, lsNoCollectionEDate);
        setString(cnt++, lsNoPenaltyFlag);
        setString(cnt++, lsNoPenaltySMonth);
        setString(cnt++, lsNoPenaltyEMonth);
//        setString(cnt++, ls_no_interest_flag);
//        setString(cnt++, ls_no_interest_s_month);
//        setString(cnt++, ls_no_interest_e_month);
        setString(cnt++, lsStatUnprintFlag);
        setString(cnt++, lsStatUnprintSMonth);
        setString(cnt++, lsStatUnprintEMonth);
        setString(cnt++, lsNoTelCollFlag);
        setString(cnt++, lsNoTelCollSDate);
        setString(cnt++, lsNoTelCollEDate);
        setString(cnt++, lsNoSmsFlag);
        setString(cnt++, lsNoSmsSDate);
        setString(cnt++, lsNoSmsEDate);
        setString(cnt++, lsAutopayAcctSDate);
        setString(cnt++, lsAutopayAcctEDate);
        setString(cnt++, lsNoFStopFlag);
        setString(cnt++, lsNoFStopSDate);
        setString(cnt++, lsNoFStopEDate);
        setString(cnt++, hAcnoNoautoBalanceFlag);
        setString(cnt++, lsNoautoBalanceDate1);
        setString(cnt++, lsNoautoBalanceDate2);
        setString(cnt++, hAcnoRevolveReason);
        setString(cnt++, hAcnoRevolveReason2);
        setString(cnt++, lsRevolveRateSMonth);
        setString(cnt++, lsRevolveRateEMonth);
        setString(cnt++, hClbmRevolveRateFlag);
        setDouble(cnt++, hAcnoRevolveIntRate);
        setString(cnt++, hClbmRevolveRateFlag);
        setDouble(cnt++, hAcnoRevolveIntRate);
        setString(cnt++, lsRevolveRateSMonth);
        setDouble(cnt++, hAcnoRevolveIntRate2);
        setDouble(cnt++, hAcnoRevolveIntRate2);
        setDouble(cnt++, hAcnoRevolveIntRate2);
        setDouble(cnt++, hAcnoRevolveIntRate2);
        setString(cnt++, javaProgram);
        setRowId(cnt++, hAcnoRowid);
	
//		System.out.println("BreakPointXXX >>> ls_no_delinquent_flag="+ls_no_delinquent_flag+", ls_no_delinquent_s_date="+ls_no_delinquent_s_date
//				+", ls_no_delinquent_e_date="+ls_no_delinquent_e_date+", ls_no_collection_flag="+ls_no_collection_flag
//				+", ls_no_collection_s_date="+ls_no_collection_s_date+", ls_no_collection_e_date="+ls_no_collection_e_date
//				+", ls_no_penalty_flag="+ls_no_penalty_flag+", ls_no_penalty_s_month="+ls_no_penalty_s_month
//				+", ls_no_penalty_e_month="+ls_no_penalty_e_month+", ls_stat_unprint_flag="+ls_stat_unprint_flag
//				+", ls_stat_unprint_s_month="+ls_stat_unprint_s_month+", ls_stat_unprint_e_month="+ls_stat_unprint_e_month
//				+", ls_no_tel_coll_flag="+ls_no_tel_coll_flag+", ls_no_tel_coll_s_date="+ls_no_tel_coll_s_date
//				+", ls_no_tel_coll_e_date="+ls_no_tel_coll_e_date+", ls_no_sms_flag="+ls_no_sms_flag
//				+", ls_no_sms_s_date="+ls_no_sms_s_date+", ls_no_sms_e_date="+ls_no_sms_e_date
//				+", ls_autopay_acct_s_date="+ls_autopay_acct_s_date+", ls_autopay_acct_e_date="+ls_autopay_acct_e_date
//				+", ls_no_f_stop_flag="+ls_no_f_stop_flag+", ls_no_f_stop_s_date="+ls_no_f_stop_s_date+", ls_no_f_stop_e_date="+ls_no_f_stop_e_date
//				+", ls_noauto_balance_date1="+ls_noauto_balance_date1+", ls_noauto_balance_date2="+ls_noauto_balance_date2
//				+", ls_revolve_rate_s_month="+ls_revolve_rate_s_month+", ls_revolve_rate_e_month="+ls_revolve_rate_e_month
//		+", javaProgram="+javaProgram+", h_acno_rowid="+h_acno_rowid);

//        daoTable = "act_acno";
//        updateSQL = "pay_by_stage_flag     = ?, ";
//        updateSQL += "no_delinquent_flag   = decode(cast(? as varchar(8)),'00000000','N','Y'),";
//        updateSQL += "no_delinquent_s_date = decode(cast(? as varchar(8)),'00000000','',?),";
//        updateSQL += "no_delinquent_e_date = decode(cast(? as varchar(8)),'99999999','',?),";
//        updateSQL += "no_collection_flag   = decode(cast(? as varchar(8)),'00000000','N','Y'),";
//        updateSQL += "no_collection_s_date = decode(cast(? as varchar(8)),'00000000','',?),";
//        updateSQL += "no_collection_e_date = decode(cast(? as varchar(8)),'99999999','',?),";
//        updateSQL += "no_penalty_flag      = decode(cast(? as varchar(6)),'000000','N','Y'),";
//        updateSQL += "no_penalty_s_month   = decode(cast(? as varchar(6)),'000000','',?),";
//        updateSQL += "no_penalty_e_month   = decode(cast(? as varchar(6)),'999999','',?),";
//        updateSQL += "stat_unprint_flag    = decode(cast(? as varchar(6)),'000000','N','Y'),";
//        updateSQL += "stat_unprint_s_month = decode(cast(? as varchar(6)),'000000','',?),";
//        updateSQL += "stat_unprint_e_month = decode(cast(? as varchar(6)),'999999','',?),";
//        updateSQL += "no_tel_coll_flag     = decode(cast(? as varchar(8)),'00000000','N','Y'),";
//        updateSQL += "no_tel_coll_s_date   = decode(cast(? as varchar(8)),'00000000','',?),";
//        updateSQL += "no_tel_coll_e_date   = decode(cast(? as varchar(8)),'99999999','',?),";
//        updateSQL += "no_sms_flag          = decode(cast(? as varchar(8)),'00000000','N','Y'),";
//        updateSQL += "no_sms_s_date        = decode(cast(? as varchar(8)),'00000000','',?),";
//        updateSQL += "no_sms_e_date        = decode(cast(? as varchar(8)),'99999999','',?),";
//        updateSQL += "autopay_acct_s_date  = decode(autopay_acct_bank,'','',";
//        updateSQL += "                       decode(cast(? as varchar(8)),'00000000','',?)),";
//        updateSQL += "autopay_acct_e_date  = decode(autopay_acct_bank,'','',";
//        updateSQL += "                       decode(cast(? as varchar(8)),'99999999','',?)),";
//        updateSQL += "no_f_stop_flag       = decode(cast(? as varchar(8)),'00000000','N','Y'),";
//        updateSQL += "no_f_stop_s_date     = decode(cast(? as varchar(8)),'00000000','',?),";
//        updateSQL += "no_f_stop_e_date     = decode(cast(? as varchar(8)),'99999999','',?),";
//        updateSQL += "noauto_balance_flag  = ?,";
//        updateSQL += "noauto_balance_date1 = decode(cast(? as varchar(8)),'00000000','',";
//        updateSQL += "                       substr(?,1,6)),";
//        updateSQL += "noauto_balance_date2 = decode(cast(? as varchar(8)),'99999999','',";
//        updateSQL += "                       substr(?,1,6)),";
//        updateSQL += "revolve_reason       = ?,";
//        updateSQL += "revolve_reason_2     = ?,";
//        updateSQL += "revolve_rate_s_month = decode(cast(? as varchar(6)),'000000','',?),";
//        updateSQL += "revolve_rate_e_month = decode(cast(? as varchar(6)),'999999','',?),";
//        updateSQL += "revolve_int_sign     = decode(decode(cast(? as varchar(1)),'Y',?,";
//        updateSQL += "                       revolve_int_rate),0,'','-'),";
//        updateSQL += "revolve_int_rate     = decode(cast(? as varchar(1)),'Y',?,";
//        updateSQL += "                       decode(cast(? as varchar(6)),'',0,";
//        updateSQL += "                       revolve_int_rate)),";
//        updateSQL += "revolve_rate_s_month_2 = decode(cast(? as double),0,'',revolve_rate_s_month_2),";
//        updateSQL += "revolve_rate_e_month_2 = decode(cast(? as double),0,'',revolve_rate_e_month_2),";
//        updateSQL += "revolve_int_sign_2   = decode(cast(? as double),0,'',revolve_int_sign_2),";
//        updateSQL += "revolve_int_rate_2   = decode(cast(? as double),0,0,revolve_int_rate_2),";
////        updateSQL += "block_reason         = ?,";  //no column
////        updateSQL += "block_reason2        = ?,";  //no column
////        updateSQL += "block_date           = ?,";  //no column
////        updateSQL += "block_status         = nvl(? , block_status),";  //no column
//        updateSQL += "mod_time = sysdate, ";
//        updateSQL += "mod_pgm  = ? ";
//        whereStr = "where rowid = ? ";
////        whereStr += "and  nopro_flag = 'N' ";  //no column
//        setString(1, h_acno_pay_by_stage_flag);
//        setString(2, h_acno_no_delinquent_s_date);
//        setString(3, h_acno_no_delinquent_s_date);
//        setString(4, h_acno_no_delinquent_s_date);
//        setString(5, h_acno_no_delinquent_e_date);
//        setString(6, h_acno_no_delinquent_e_date);
//        setString(7, h_acno_no_collection_s_date);
//        setString(8, h_acno_no_collection_s_date);
//        setString(9, h_acno_no_collection_s_date);
//        setString(10, h_acno_no_collection_e_date);
//        setString(11, h_acno_no_collection_e_date);
//        setString(12, h_acno_no_penalty_s_month);
//        setString(13, h_acno_no_penalty_s_month);
//        setString(14, h_acno_no_penalty_s_month);
//        setString(15, h_acno_no_penalty_e_month);
//        setString(16, h_acno_no_penalty_e_month);
//        setString(17, h_acno_stat_unprint_s_month);
//        setString(18, h_acno_stat_unprint_s_month);
//        setString(19, h_acno_stat_unprint_s_month);
//        setString(20, h_acno_stat_unprint_e_month);
//        setString(21, h_acno_stat_unprint_e_month);
//        setString(22, h_acno_no_tel_coll_s_date);
//        setString(23, h_acno_no_tel_coll_s_date);
//        setString(24, h_acno_no_tel_coll_s_date);
//        setString(25, h_acno_no_tel_coll_e_date);
//        setString(26, h_acno_no_tel_coll_e_date);
//        setString(27, h_acno_no_sms_s_date);
//        setString(28, h_acno_no_sms_s_date);
//        setString(29, h_acno_no_sms_s_date);
//        setString(30, h_acno_no_sms_e_date);
//        setString(31, h_acno_no_sms_e_date);
//        setString(32, h_acno_autopay_acct_s_date);
//        setString(33, h_acno_autopay_acct_s_date);
//        setString(34, h_acno_autopay_acct_e_date);
//        setString(35, h_acno_autopay_acct_e_date);
//        setString(36, h_acno_no_f_stop_s_date);
//        setString(37, h_acno_no_f_stop_s_date);
//        setString(38, h_acno_no_f_stop_s_date);
//        setString(39, h_acno_no_f_stop_e_date);
//        setString(40, h_acno_no_f_stop_e_date);
//        setString(41, h_acno_noauto_balance_flag);
//        setString(42, h_acno_noauto_balance_date1);
//        setString(43, h_acno_noauto_balance_date1);
//        setString(44, h_acno_noauto_balance_date2);
//        setString(45, h_acno_noauto_balance_date2);
//        setString(46, h_acno_revolve_reason);
//        setString(47, h_acno_revolve_reason_2);
//        setString(48, h_acno_revolve_rate_s_month);
//        setString(49, h_acno_revolve_rate_s_month);
//        setString(50, h_acno_revolve_rate_e_month);
//        setString(51, h_acno_revolve_rate_e_month);
//        setString(52, h_clbm_revolve_rate_flag);
//        setDouble(53, h_acno_revolve_int_rate);
//        setString(54, h_clbm_revolve_rate_flag);
//        setDouble(55, h_acno_revolve_int_rate);
//        setString(56, h_acno_revolve_rate_s_month);
//        setDouble(57, h_acno_revolve_int_rate_2);
//        setDouble(58, h_acno_revolve_int_rate_2);
//        setDouble(59, h_acno_revolve_int_rate_2);
//        setDouble(60, h_acno_revolve_int_rate_2);
////        setString(61, h_acno_block_reason);
////        setString(62, h_acno_block_reason2);
////        setString(63, h_busi_business_date);
////        setString(64, h_acno_block_status);
//        setString(61, javaProgram);
//        setRowId(62, h_acno_rowid);

        updateTable();
        if (notFound.equals("Y")) {
        	showLogMessage("I", "", "h_acno_p_seqno="+ hAcnoPSeqno);  //test
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "c.id_p_seqno, ";
        sqlCmd += "c.card_no, ";
        sqlCmd += "d.block_reason2||d.block_reason3||d.block_reason4||d.block_reason5  block_reason2, "; 
        sqlCmd += "d.block_reason1 block_reason, "; 
        sqlCmd += "c.rowid as rowid ";
        sqlCmd += "from crd_card c, cca_card_acct d ";
//        sqlCmd += "where c.p_seqno = ? ";
//        sqlCmd += "and   c.p_seqno = d.p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' "; //find block_reason
        sqlCmd += "where c.acno_p_seqno = ? ";
        sqlCmd += "and   c.acno_p_seqno = d.acno_p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' "; //find block_reason
        sqlCmd += "and   c.current_code = '0' ";
        setString(1, hAcnoPSeqno);
        
        extendField = "crd_card.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardIdPSeqno = getValue("crd_card.id_p_seqno", i);
            hCardCardNo = getValue("crd_card.card_no", i);
            hCardBlockReason2 = getValue("crd_card.block_reason2", i);
            hCardBlockReason = getValue("crd_card.block_reason", i);
            hCardRowid = getValue("crd_card.rowid", i);

            hCardBlockReason2 = String.format("%-8.8s", hCardBlockReason2);
            hCardBlockReason = String.format("%-2.2s", hCardBlockReason);
            hCardBlockStatus = "";
            if ((comc.getSubString(hCardBlockReason2, 2, 4).equals(hClbmBlockMark3) == false)) {
                if (hClbmBlockMark3.length() == 0)
                    hClbmBlockMark3 = "  ";

                hCardBlockReason2 = comc.getSubString(hCardBlockReason2, 0, 2) + hClbmBlockMark3
                        + comc.getSubString(hCardBlockReason2, 4);
                hCardBlockStatus = "12";
                insertOnbat(1, 3);
                updateCrdCard();
            }
            if (hCardBlockReason.compareTo(hClbmBlockMark1) != 0) {
                if (hClbmBlockMark1.length() == 0)
                    hClbmBlockMark1 = "  ";
                hCardBlockReason = hClbmBlockMark1;
                hCardBlockStatus = "12";
                insertOnbat(1, 1);
                updateCrdCard();
            }
        }
    }

    /***********************************************************************/
    void selectCrdCard1() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_p_seqno, ";
        sqlCmd += "major_id_p_seqno, ";
        sqlCmd += "card_no, ";
        sqlCmd += "decode(combo_indicator,'','N',combo_indicator) as combo_indicator, ";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from crd_card ";
//        sqlCmd += "where gp_no = ? ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and   current_code = '0' ";
        setString(1, hAcnoPSeqno);
        
        extendField = "crd_card_1.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardIdPSeqno = getValue("crd_card_1.id_p_seqno", i);
            hCardMajorIdPSeqno = getValue("crd_card_1.major_id_p_seqno", i);
            hCardCardNo = getValue("crd_card_1.card_no", i);
            hCardComboIndicator = getValue("crd_card_1.combo_indicator", i);
            hCardRowid = getValue("crd_card_1.rowid", i);

            if (!hCardComboIndicator.equals("N"))
                insertCrdStopLog();

            if (selectCrdJcic() != 0)
                insertCrdJcic();
            else
                updateCrdJcic();

            if (selectCrdApscard() != 0)
                insertCrdApscard();
            else
                updateCrdApscard();

            insertOnbat1();
            updateCrdCard1();
        }
    }

    /***********************************************************************/
    void updateColLiacRemod() throws Exception {
        daoTable = "col_liac_remod";
        updateSQL = "proc_date = ?, ";
        updateSQL += "proc_flag = decode(cast(? as varchar(1)),'1','1','Y'),";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hClrdProcFlag);
        setString(3, javaProgram);
        setRowId(4, hClrdRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liac_remod not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateCrdCard() throws Exception {
        daoTable = "crd_card";
        updateSQL = "block_reason = ?, ";   
        updateSQL += "block_reason2 = ?,";   
//        updateSQL += "block_status = nvl(?,block_status),";  //no column
        updateSQL += "block_date = ?,";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hCardBlockReason);
        setString(2, hCardBlockReason2);
//        setString(3, h_card_block_status);
        setString(3, hBusiBusinessDate);
        setString(4, javaProgram);
        setRowId(5, hCardRowid);
        

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_card not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateCrdCard1() throws Exception {
        daoTable = "crd_card";
        updateSQL = "oppost_reason = ?, ";
        updateSQL += "oppost_date = ?,";
        updateSQL += "current_code = '1',";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hClbmOppostReason);
        setString(2, hBusiBusinessDate);
        setString(3, javaProgram);
        setRowId(4, hCardRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_card not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertOnbat(int hInt, int hInt2) throws Exception {
        dateTime();
        daoTable = "onbat_2ccas";
        extendField = daoTable + ".";
        setValue(extendField+"trans_type", "2");
        setValueInt(extendField+"to_which", 2);
        setValue(extendField+"dog", sysDate + sysTime);
        setValue(extendField+"proc_mode", "B");
        setValueInt(extendField+"proc_status", 0);
        setValue(extendField+"card_catalog", hAcnoCardIndicator);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"card_hldr_id", hAcnoAcctHolderId);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"card_acct_id", hAcnoAcctKey);
        setValue(extendField+"acno_p_seqno", hAcnoPSeqno);
        setValue(extendField+"card_no", hInt == 0 ? "" : hCardCardNo);
        setValue(extendField+"block_code_1", hInt2 == 1 ? hAcnoBlockReason : "");
        setValue(extendField+"block_code_3", hInt2 == 1 ? "" : comc.getSubString(hAcnoBlockReason2, 2, 2));
        setValue(extendField+"match_flag", hInt2 == 1 ? "1" : "3");
        setValue(extendField+"match_date", sysDate);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_onbat_2ccas duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertOnbat1() throws Exception {
        dateTime();
        daoTable = "onbat_2ccas";
        extendField = daoTable + ".";
        setValue(extendField+"trans_type", "6");
        setValueInt(extendField+"to_which", 2);
        setValue(extendField+"dog", sysDate + sysTime);
        setValue(extendField+"dop", "");
        setValue(extendField+"proc_mode", "B");
        setValueInt(extendField+"proc_status", 0);
        setValue(extendField+"acno_p_seqno", hAcnoPSeqno);
        setValue(extendField+"card_no", hCardCardNo);
        setValue(extendField+"opp_type", "1");
        setValue(extendField+"opp_reason", hClbmOppostReason);
        setValue(extendField+"opp_date", sysDate);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_onbat_2ccas duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertRskAcnolog(int hInt) throws Exception {
        dateTime();
        daoTable = "rsk_acnolog";
        extendField = daoTable + ".";
        setValue(extendField+"kind_flag", hInt == 1 ? "C" : "A");
        setValue(extendField+"card_no", hInt == 1 ? hCardCardNo : "");
        setValue(extendField+"acno_p_seqno", hAcnoPSeqno);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"id_p_seqno", hInt == 1 ? hCardIdPSeqno : hAcnoIdPSeqno);
        setValue(extendField+"log_date", hBusiBusinessDate);
        setValue(extendField+"log_mode", "2"); /* online:1 batch:2 */
        setValue(extendField+"log_type", "3"); /* 凍結:3 解凍:4 */
        setValue(extendField+"log_reason", "");
        setValue(extendField+"log_not_reason", "");
        setValue(extendField+"block_reason", hInt == 1 ? hCardBlockReason : hAcnoBlockReason);
        String block_reason2 = hInt == 1 ? hCardBlockReason2 : hAcnoBlockReason2;
        setValue(extendField+"block_reason2", String.format("%2.2s", comc.getSubString(block_reason2,0)));
        setValue(extendField+"block_reason3", String.format("%2.2s", comc.getSubString(block_reason2,2)));
        setValue(extendField+"block_reason4", String.format("%2.2s", comc.getSubString(block_reason2,4)));
        setValue(extendField+"block_reason5", String.format("%2.2s", comc.getSubString(block_reason2,6)));
        setValue(extendField+"fit_cond", "ECS");
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
//        setValue("mod_ws", "batch");  //no column
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_rsk_acnolog duplicate!", "", hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    void updateCcaCardAcct() throws Exception {
//    	if (h_acno_block_reason.trim().equals(h_clbm_block_mark1.trim())
//        	&& comc.getSubString(h_acno_block_reason2,2,4).trim().equals(h_clbm_block_mark3.trim())) {
//            return;
//        }

    	daoTable = "cca_card_acct";
        updateSQL = "block_reason1 = ?, ";
        updateSQL += "block_reason3 = ?, ";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where acno_p_seqno = ? ";
        whereStr += "and decode(debit_flag,'','N',debit_flag) = 'N' ";
        setString(1, hClbmBlockMark1);
        setString(2, hClbmBlockMark3);
        setString(3, javaProgram);
        setString(4, hAcnoPSeqno);

        updateTable();
//        if (notFound.equals("Y")) {
//            comcr.err_rtn("update_cca_card_acct not found!", "", h_call_batch_seqno);
//        }
    }

    /***********************************************************************/
    void selectColLiabParam() throws Exception {
        hClbmStatUnprintFlag = "";
        hClbmNoTelCollFlag = "";
        hClbmNoDelinquentFlag = "";
        hClbmNoCollectionFlag = "";
        hClbmNoFStopFlag = "";
        hClbmRevolveRateFlag = "";
        hClbmNoPenaltyFlag = "";
//        h_clbm_no_interest_flag = "";
        hClbmNoSmsFlag = "";
        hClbmMinPayFlag = "";
        hClbmAutopayFlag = "";
        hClbmPayStageFlag = "";
        hClbmPayStageMark = "";
        hClbmBlockFlag = "";
        hClbmBlockMark1 = "";
        hClbmBlockMark3 = "";
        hClbmDBalFlag = "";
        hClbmOppostFlag = "";
        hClbmOppostReason = "";
        hClbmNoautoBalanceFlag = "";
        hClbmSendCsFlag = "";
        sqlCmd = "select stat_unprint_flag,";
        sqlCmd += "no_tel_coll_flag, ";
        sqlCmd += "no_delinquent_flag, ";
        sqlCmd += "no_collection_flag, ";
        sqlCmd += "no_f_stop_flag, ";
        sqlCmd += "revolve_rate_flag, ";
        sqlCmd += "no_penalty_flag, ";
        sqlCmd += "no_interest_flag, ";
        sqlCmd += "no_sms_flag, ";
        sqlCmd += "min_pay_flag, ";
        sqlCmd += "autopay_flag, ";
        sqlCmd += "pay_stage_flag, ";
        sqlCmd += "pay_stage_mark, ";
        sqlCmd += "block_flag, ";
        sqlCmd += "block_mark1, ";
        sqlCmd += "block_mark3, ";
        sqlCmd += "d_bal_flag, ";
        sqlCmd += "oppost_flag, ";
        sqlCmd += "oppost_reason, ";
        sqlCmd += "noauto_balance_flag, ";
        sqlCmd += "send_cs_flag ";
        sqlCmd += "from col_liab_param ";
        sqlCmd += "where apr_date <> '' ";
        sqlCmd += "and   liab_status  = ? ";
        sqlCmd += "and   liab_type    = '2' ";
        setString(1, hClrdLiacStatus);

        extendField = "col_liab_param.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClbmStatUnprintFlag = getValue("col_liab_param.stat_unprint_flag");
            hClbmNoTelCollFlag = getValue("col_liab_param.no_tel_coll_flag");
            hClbmNoDelinquentFlag = getValue("col_liab_param.no_delinquent_flag");
            hClbmNoCollectionFlag = getValue("col_liab_param.no_collection_flag");
            hClbmNoFStopFlag = getValue("col_liab_param.no_f_stop_flag");
            hClbmRevolveRateFlag = getValue("col_liab_param.revolve_rate_flag");
            hClbmNoPenaltyFlag = getValue("col_liab_param.no_penalty_flag");
//            h_clbm_no_interest_flag = getValue("col_liab_param.no_interest_flag");
            hClbmNoSmsFlag = getValue("col_liab_param.no_sms_flag");
            hClbmMinPayFlag = getValue("col_liab_param.min_pay_flag");
            hClbmAutopayFlag = getValue("col_liab_param.autopay_flag");
            hClbmPayStageFlag = getValue("col_liab_param.pay_stage_flag");
            hClbmPayStageMark = getValue("col_liab_param.pay_stage_mark");
            hClbmBlockFlag = getValue("col_liab_param.block_flag");
            hClbmBlockMark1 = getValue("col_liab_param.block_mark1");
            hClbmBlockMark3 = getValue("col_liab_param.block_mark3");
            hClbmDBalFlag = getValue("col_liab_param.d_bal_flag");
            hClbmOppostFlag = getValue("col_liab_param.oppost_flag");
            hClbmOppostReason = getValue("col_liab_param.oppost_reason");
            hClbmNoautoBalanceFlag = getValue("col_liab_param.noauto_balance_flag");
            hClbmSendCsFlag = getValue("col_liab_param.send_cs_flag");
        }
    }

    /***********************************************************************/
    void insertCrdStopLog() throws Exception {
        sqlCmd = "select substr(to_char(ecs_stop.nextval,'0000000000'),2,10) ecs_stop ";
        sqlCmd += "from dual ";

        if (selectTable() > 0) {
            hStopProcSeqno = getValue("ecs_stop");
        }

        daoTable = "crd_stop_log";
        extendField = daoTable + ".";
        setValue(extendField+"proc_seqno", hStopProcSeqno);
        setValue(extendField+"create_time", sysDate + sysTime);
        setValue(extendField+"card_no", hCardCardNo);
        setValue(extendField+"current_code", "1");
        setValue(extendField+"oppost_reason", hClbmOppostReason);
        setValue(extendField+"oppost_date", hBusiBusinessDate);
        setValue(extendField+"trans_type", "01");
        setValue(extendField+"send_type", "2"); /* 1:APPC 2:MQ */
        setValue(extendField+"stop_source", "1");
        setValue(extendField+"mod_user", "SYSTEM");
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_stop_log duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    int selectCrdJcic() throws Exception {
        sqlCmd = "select rowid as rowid ";
        sqlCmd += "from crd_jcic ";
        sqlCmd += "where card_no = ? ";
        sqlCmd += "and   trans_type = 'C' ";
        sqlCmd += "and   to_jcic_date = '' ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hCardCardNo);
        
        extendField = "crd_jcic.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCdjcRowid = getValue("crd_jcic.rowid");
        }

        if (recordCnt == 0)
            return 1;
        return 0;
    }

    /***********************************************************************/
    void insertCrdJcic() throws Exception {
        dateTime();
        daoTable = "crd_jcic";
        extendField = daoTable + ".";
        setValue(extendField+"card_no", hCardCardNo);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_user", javaProgram);
        setValue(extendField+"trans_type", "C");
        setValue(extendField+"is_rc", hAcnoRcUseIndicator);
        setValue(extendField+"current_code", "1");
        setValue(extendField+"oppost_reason", hClbmOppostReason);
        setValue(extendField+"oppost_date", hBusiBusinessDate);
        setValue(extendField+"mod_user", javaProgram);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_jcic duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateCrdJcic() throws Exception {
        daoTable = "crd_jcic";
        updateSQL = "current_code = '1', ";
        updateSQL += "oppost_reason = ?,";
        updateSQL += "oppost_date = ?,";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_user = ?,";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hClbmOppostReason);
        setString(2, hBusiBusinessDate);
        setString(3, javaProgram);
        setString(4, javaProgram);
        setRowId(5, hCdjcRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_jcic not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    int selectCrdApscard() throws Exception {
        sqlCmd = "select rowid as rowid ";
        sqlCmd += "from crd_apscard ";
        sqlCmd += "where card_no = ? ";
        sqlCmd += "and   to_aps_date = '' ";
        setString(1, hCardCardNo);
        
        extendField = "crd_apscard.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hApscRowid = getValue("crd_apscard.rowid");
        }

        if (recordCnt == 0)
            return 1;
        return 0;
    }

    /***********************************************************************/
    void updateCrdApscard() throws Exception {
        daoTable = "crd_apscard";
        updateSQL = "stop_reason = '1', ";
        updateSQL += "stop_date = ?,";
        updateSQL += "status_code = '',";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_user = 'batch',";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hApscRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_apscard not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertCrdApscard() throws Exception {
        hIdnoIdPSeqno = hCardMajorIdPSeqno;
        selectCrdIdno();
        hApscPmBirthday = hIdnoBirthday;
        hApscPmName = hIdnoChiName;
        hApscSupBirthday = "";
        hApscSupName = "";
        if (hCardIdPSeqno.compareTo(hCardMajorIdPSeqno) != 0) {
            hIdnoIdPSeqno = hCardIdPSeqno;
            selectCrdIdno();
            hApscSupBirthday = hIdnoBirthday;
            hApscSupName = hIdnoChiName;
        }

        sqlCmd = "insert into crd_apscard (";
        sqlCmd += "crt_datetime,";
        sqlCmd += "card_no,";
        sqlCmd += "valid_date,";
        sqlCmd += "stop_date,";
        sqlCmd += "stop_reason,";
        sqlCmd += "mail_type,";
        sqlCmd += "mail_no,";
        sqlCmd += "mail_branch,";
        sqlCmd += "mail_date,";
        sqlCmd += "pm_id,";
        sqlCmd += "pm_id_code,";
        sqlCmd += "pm_birthday,";
        sqlCmd += "sup_id,";
        sqlCmd += "sup_id_code,";
        sqlCmd += "sup_birthday,";
        sqlCmd += "corp_no,";
        sqlCmd += "corp_no_code,";
        sqlCmd += "card_type,";
        sqlCmd += "pm_name,";
        sqlCmd += "sup_name,";
        sqlCmd += "sup_lost_status,";
        sqlCmd += "status_code,";
        sqlCmd += "group_code,";
        sqlCmd += "mod_user,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss'),";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "'1',";
        sqlCmd += "mail_type,";
        sqlCmd += "mail_no,";
        sqlCmd += "mail_branch,";
        sqlCmd += "mail_proc_date,";
//        sqlCmd += "major_id,";  //no column
//        sqlCmd += "major_id_code,";  //no column
        sqlCmd += "uf_idno_id(major_id_p_seqno) major_id,";
        sqlCmd += "'0' major_id_code,";
        sqlCmd += "?,";
//        sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'',id_no),";  //no column
//        sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'',id_no_code),";  //no column
        sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'', uf_idno_id(id_p_seqno)),";
        sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'','0'),";
        sqlCmd += "?,";
        sqlCmd += "corp_no,";
        sqlCmd += "corp_no_code,";
        sqlCmd += "card_type,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'','0'),";
        sqlCmd += "'',";
        sqlCmd += "group_code,";
        sqlCmd += "?,";
        sqlCmd += "sysdate,";
        sqlCmd += "? ";
        sqlCmd += "from crd_card where rowid = ? ";
        setString(1, hCardCardNo);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hApscPmBirthday);
        setString(5, hApscSupBirthday);
        setString(6, hApscPmName);
        setString(7, hApscSupName);
        setString(8, javaProgram);
        setString(9, javaProgram);
        setRowId(10, hCardRowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        sqlCmd = "select birthday,";
        sqlCmd += "chi_name ";
        sqlCmd += "from crd_idno ";
        sqlCmd += "where id_p_seqno  = ? ";
        setString(1, hIdnoIdPSeqno);
        
        extendField = "crd_idno.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIdnoBirthday = getValue("crd_idno.birthday");
            hIdnoChiName = getValue("crd_idno.chi_name");
        }

        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno error!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActNoautoBalanceLog() throws Exception {
        dateTime();
        daoTable = "act_noauto_balance_log";
        extendField = daoTable + ".";
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"modify_type", "2");
        setValue(extendField+"noauto_balance_flag", hClbmNoautoBalanceFlag);
        setValue(extendField+"noauto_balance_date1", comc.getSubString(hAcnoNoautoBalanceDate1, 0, 6));
        setValue(extendField+"noauto_balance_date2", comc.getSubString(hAcnoNoautoBalanceDate2, 0, 6));
        if (hClbmBlockMark1.length() == 0)
        setValue(extendField+"remark", "前置協商");
        setValue(extendField+"crt_user", javaProgram);
        setValue(extendField+"apr_user", "SYSTEM");
        setValue(extendField+"apr_date", hBusiBusinessDate);
        setValue(extendField+"apr_flag", "Y");
        setValue(extendField+"mod_user", javaProgram);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_noauto_balance_log duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectColLiacNego() throws Exception {
        hClnoInterestBaseDate = "";
        hClnoContractDate = "";
        sqlCmd = "select interest_base_date,";
        sqlCmd += "contract_date ";
        sqlCmd += "from col_liac_nego ";
        sqlCmd += "where liac_seqno  = ? ";
        setString(1, hClrdLiacSeqno);

        extendField = "col_liac_nego.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClnoInterestBaseDate = getValue("col_liac_nego.interest_base_date");
            hClnoContractDate = getValue("col_liac_nego.contract_date");
        }
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_liac_nego not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectColLiacNego1() throws Exception {
        hClnoEndReason = "";
        sqlCmd = "select end_reason ";
        sqlCmd += "from col_liac_nego ";
        sqlCmd += "where liac_seqno  = ? ";
        setString(1, hClrdLiacSeqno);
        
        extendField = "col_liac_nego_1.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClnoEndReason = getValue("col_liac_nego_1.end_reason");
        }

        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_liac_nego_1 error!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void deleteColLiacNego() throws Exception {
        daoTable = "col_liac_nego";
        whereStr = "where liac_seqno  = ? ";
        setString(1, hClrdLiacSeqno);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_col_liac_nego not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertColLiacNegoHst() throws Exception {

        sqlCmd = "insert into col_liac_nego_hst (";
        sqlCmd += "liac_seqno,";
        sqlCmd += "tran_date,";
        sqlCmd += "tran_time,";
        sqlCmd += "file_date,";
        sqlCmd += "liac_status,";
        sqlCmd += "query_date,";
        sqlCmd += "notify_date,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "id_no,";
        sqlCmd += "chi_name,";
        sqlCmd += "bank_code,";
        sqlCmd += "bank_name,";
        sqlCmd += "apply_date,";
        sqlCmd += "nego_s_date,";
        sqlCmd += "stop_notify_date,";
        sqlCmd += "interest_base_date,";
        sqlCmd += "recol_reason,";
        sqlCmd += "credit_flag,";
        sqlCmd += "no_credit_flag,";
        sqlCmd += "cash_card_flag,";
        sqlCmd += "credit_card_flag,";
        sqlCmd += "contract_date,";
        sqlCmd += "liac_remark,";
        sqlCmd += "end_date,";
        sqlCmd += "end_reason,";
        sqlCmd += "liac_txn_code,";
        sqlCmd += "reg_bank_no,";
        sqlCmd += "id_data_date,";
        sqlCmd += "court_agree_date,";
        sqlCmd += "case_status,";
        sqlCmd += "crt_date,";
        sqlCmd += "crt_time,";
        sqlCmd += "proc_flag,";
        sqlCmd += "proc_date,";
        sqlCmd += "end_user,";
        sqlCmd += "end_remark,";
        sqlCmd += "apr_flag,";
        sqlCmd += "apr_date,";
        sqlCmd += "apr_user,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "to_char(sysdate,'yyyymmdd'),";
        sqlCmd += "to_char(sysdate,'hh24miss'),";
        sqlCmd += "file_date,";
        sqlCmd += "liac_status,";
        sqlCmd += "query_date,";
        sqlCmd += "notify_date,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "id_no,";
        sqlCmd += "chi_name,";
        sqlCmd += "bank_code,";
        sqlCmd += "bank_name,";
        sqlCmd += "apply_date,";
        sqlCmd += "nego_s_date,";
        sqlCmd += "stop_notify_date,";
        sqlCmd += "interest_base_date,";
        sqlCmd += "recol_reason,";
        sqlCmd += "credit_flag,";
        sqlCmd += "no_credit_flag,";
        sqlCmd += "cash_card_flag,";
        sqlCmd += "credit_card_flag,";
        sqlCmd += "contract_date,";
        sqlCmd += "liac_remark,";
        sqlCmd += "end_date,";
        sqlCmd += "end_reason,";
        sqlCmd += "'A',";
        sqlCmd += "reg_bank_no,";
        sqlCmd += "id_data_date,";
        sqlCmd += "court_agree_date,";
        sqlCmd += "case_status,";
        sqlCmd += "crt_date,";
        sqlCmd += "crt_time,";
        sqlCmd += "proc_flag,";
        sqlCmd += "proc_date,";
        sqlCmd += "end_user,";
        sqlCmd += "end_remark,";
        sqlCmd += "apr_flag,";
        sqlCmd += "apr_date,";
        sqlCmd += "apr_user,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm ";
        sqlCmd += "from col_liac_nego where liac_seqno = ? ";
        setString(1, hClrdLiacSeqno);
        setString(2, hClrdLiacSeqno);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    int selectColLiacContract() throws Exception {
        hClrdLiacIntRate = 0;
        hClctRowid = "";
        hClnoCreditCardFlag = "";
        sqlCmd = "select liac_int_rate, ";
        sqlCmd += "a.rowid as rowid, ";
        sqlCmd += "b.credit_card_flag ";
        sqlCmd += "from col_liac_nego b ";
        sqlCmd += "left join col_liac_contract a ";
        sqlCmd += "on   a.liac_seqno = b.liac_seqno ";
        sqlCmd += "and  a.file_date  = b.file_date ";
        sqlCmd += "where b.liac_seqno = ? ";
        setString(1, hClrdLiacSeqno);
        
        extendField = "col_liac_contract.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClrdLiacIntRate = getValueDouble("col_liac_contract.liac_int_rate");
            hClctRowid = getValue("col_liac_contract.rowid");
            hClnoCreditCardFlag = getValue("col_liac_contract.credit_card_flag");
        }

        if (hClnoCreditCardFlag.equals("N"))
            return 0;
        if (hClctRowid.length() != 0)
            return 0;
        return 1;
    }

    /***********************************************************************/
    int selectColLiacDebt() throws Exception {
        hCcdtInEndBalNew = 0;
        hCcdtOutEndBalNew = 0;
        sqlCmd = "select in_end_bal_new, ";
        sqlCmd += "out_end_bal_new ";
        sqlCmd += "from col_liac_debt a,col_liac_nego b ";
        sqlCmd += "where a.liac_seqno  = b.liac_seqno ";
        sqlCmd += "and   b.liac_seqno  = ? ";
        sqlCmd += "and   a.report_date = (select max(report_date) ";
        sqlCmd += "                       from   col_liac_debt ";
        sqlCmd += "                       where liac_seqno = ? ) ";
        setString(1, hClrdLiacSeqno);
        setString(2, hClrdLiacSeqno);
        
        extendField = "col_liac_debt.";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
//            comcr.errRtn("select_col_liac_debt not found!", "", hCallBatchSeqno);
        	showLogMessage("I", "", "select_col_liac_debt not found !");
        	
        }
        if (recordCnt > 0) {
            hCcdtInEndBalNew = getValueDouble("col_liac_debt.in_end_bal_new");
            hCcdtOutEndBalNew = getValueDouble("col_liac_debt.out_end_bal_new");
        }

        if ((hCcdtInEndBalNew == 0) && (hCcdtOutEndBalNew == 0))
            return 0;
        return 1;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA450 proc = new ColA450();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
