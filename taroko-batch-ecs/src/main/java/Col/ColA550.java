/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/20  V1.00.00    phopho     program initial                          *
*  109/01/14  V1.00.01  phopho     fix update_cca_card_acct()                 *
*  109/01/16  V1.00.02  phopho     CR1162 金管會查核逾期手續費須修改程式.       *
*  109/12/10  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;


import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColA550 extends AccessDAO {
    private String progname = "無擔保債務-帳戶狀態設定處理程式  109/12/10  V1.00.02 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";

    String hWdayThisAcctMonth = "";
    String hClbmStatUnprintFlag = "";
    String hClbmNoTelCollFlag = "";
    String hClbmNoDelinquentFlag = "";
    String hClbmNoCollectionFlag = "";
    String hClbmNoFStopFlag = "";
    String hClbmNoPenaltyFlag = "";
    String hClbmNoSmsFlag = "";
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
    String hAcnoStatUnprintFlag = "";
    String hAcnoStatUnprintSMonth = "";
    String hAcnoStatUnprintEMonth = "";
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

    String hAcnoNoInterestFlag = "";
    String hAcnoNoInterestSMonth = "";
    String hAcnoNoInterestEMonth = "";
    String hClbmNoInterestFlag = "";
    String hCludIdPSeqno = "";
    String hCludId = "";
    String hCludLiauStatus = "";
    String hCludExtendEDate = "";
    String hCludRowid = "";
    String hAcnoNoautoBalanceFlag = "";
    String hAcnoNoautoBalanceDate1 = "";
    String hAcnoNoautoBalanceDate2 = "";
    String hWdayNextAcctMonth = "";
    String hClbmNoautoBalanceFlag = "";
    String hClbmOppostFlag = "";
    String hClbmOppostReason = "";
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

    int    totalCnt  = 0;
    long intCmd = 0;
    String hTempId = "";

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }

            // 檢查參數
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ColA550 ", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();

            selectColLiauRemod();

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
        sqlCmd = "select business_date ";
        sqlCmd += "from ptr_businday ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectColLiauRemod() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_p_seqno, ";
        sqlCmd += "id_no, ";
        sqlCmd += "liau_status, ";
        sqlCmd += "extend_e_date, ";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_liau_remod ";
        sqlCmd += "where proc_flag = 'N' ";
        sqlCmd += "order by mod_time,liau_status ";

        openCursor();
        while (fetchTable()) {
        	hCludIdPSeqno = getValue("id_p_seqno");
            hCludId = getValue("id_no");
            hCludLiauStatus = getValue("liau_status");
            hCludExtendEDate = getValue("extend_e_date");
            hCludRowid = getValue("rowid");

            selectColLiabParam();
            selectActAcno();
            updateColLiauRemod();
            commitDataBase();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.id_p_seqno, ";
//        sqlCmd += "a.p_seqno, ";
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
        sqlCmd += "decode(a.no_interest_flag,'','N',a.no_interest_flag) no_interest_flag, ";
        sqlCmd += "decode(a.no_interest_s_month,'','000000',a.no_interest_s_month) no_interest_s_month, ";
        sqlCmd += "decode(a.no_interest_e_month,'','999999',a.no_interest_e_month) no_interest_e_month, ";
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
        sqlCmd += "a.card_indicator, ";
        sqlCmd += "e.block_reason1 block_reason, ";
        sqlCmd += "e.block_reason2||e.block_reason3||e.block_reason4||e.block_reason5 block_reason2, ";
//        sqlCmd += "d.id_no acct_holder_id, "; // a.acct_holder_id
        sqlCmd += "uf_idno_id(a.id_p_seqno) acct_holder_id, ";
        sqlCmd += "decode(a.noauto_balance_flag,'','N',a.noauto_balance_flag) noauto_balance_flag, ";
        sqlCmd += "decode(a.noauto_balance_date1,'','00000000',a.noauto_balance_date1) noauto_balance_date1, ";
        sqlCmd += "decode(a.noauto_balance_date2,'','99999999',a.noauto_balance_date2) noauto_balance_date2, ";
        sqlCmd += "a.rowid as rowid, ";
        sqlCmd += "b.this_acct_month, ";
        sqlCmd += "to_char(add_months(to_date(b.this_acct_month,'yyyymm'),6),'yyyymm') next_acct_month ";
        sqlCmd += "from act_acno a, ptr_workday b, ptr_actgeneral_n c ";
//        sqlCmd += "  left join crd_idno d on a.id_p_seqno = d.id_p_seqno ";
//        sqlCmd += "  left join cca_card_acct e on a.p_seqno = e.p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";  //find block_reason in cca_card_acct
        sqlCmd += "  left join cca_card_acct e on a.acno_p_seqno = e.acno_p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";  //find block_reason in cca_card_acct
        sqlCmd += "where a.acno_flag <> 'Y' ";
        sqlCmd += "and   a.acct_type      = c.acct_type ";
        sqlCmd += "and   a.stmt_cycle     = b.stmt_cycle ";
//        sqlCmd += "and   d.id_no = ? "; // a.acct_holder_id
        sqlCmd += "and   a.id_p_seqno     = ? ";
//        setString(1, h_clud_id);
        setString(1, hCludIdPSeqno);
        
        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoIdPSeqno = getValue("act_acno.id_p_seqno", i);
//            h_acno_p_seqno = getValue("p_seqno", i);
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
            hAcnoNoInterestFlag = getValue("act_acno.no_interest_flag", i);
            hAcnoNoInterestSMonth = getValue("act_acno.no_interest_s_month", i);
            hAcnoNoInterestEMonth = getValue("act_acno.no_interest_e_month", i);
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
                hAcnoNoautoBalanceFlag = "Y";
                hAcnoNoautoBalanceDate1 = hBusiBusinessDate;
                hAcnoNoautoBalanceDate2 = hCludExtendEDate;
                insertActNoautoBalanceLog();
            }
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
                hAcnoNoDelinquentEDate = hCludExtendEDate;
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
                hAcnoNoCollectionEDate = hCludExtendEDate;
            }
            // 3
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
//                h_acno_no_penalty_e_month = h_wday_next_acct_month;
                hAcnoNoPenaltyEMonth = "";  //phopho mod:CR1162
            }
            // 3-2
            if (hClbmNoInterestFlag.equals("N")) {
                if (hAcnoNoInterestFlag.equals("Y")) {
                    if (hWdayThisAcctMonth.compareTo(hAcnoNoInterestSMonth) < 0) {
                        hAcnoNoInterestFlag = "N";
                        hAcnoNoInterestSMonth = "";
                        hAcnoNoInterestEMonth = "";
                    } else {
                        if (hWdayThisAcctMonth.compareTo(hAcnoNoInterestEMonth) < 0)
                            hAcnoNoInterestEMonth = hWdayThisAcctMonth;
                    }
                }
            } else if (hClbmNoInterestFlag.equals("Y")) {
                hAcnoNoInterestFlag = "Y";
                hAcnoNoInterestSMonth = hWdayThisAcctMonth;
                hAcnoNoInterestEMonth = hWdayNextAcctMonth;
            }
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
                hAcnoStatUnprintEMonth = hWdayNextAcctMonth;
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
                hAcnoNoTelCollEDate = hCludExtendEDate;
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
                hAcnoNoSmsEDate = hCludExtendEDate;
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
                hAcnoNoFStopEDate = hCludExtendEDate;
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
            if (hClbmOppostFlag.equals("Y"))
                selectCrdCard1();

            updateActAcno();
        }
    }

    /***********************************************************************/
    void updateActAcno() throws Exception {
        daoTable = "act_acno";
        updateSQL = "no_delinquent_flag   = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += "no_delinquent_s_date = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += "no_delinquent_e_date = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += "no_collection_flag   = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += "no_collection_s_date = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += "no_collection_e_date = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += "no_penalty_flag      = decode(cast(? as varchar(6)),'000000','N','Y'),";
        updateSQL += "no_penalty_s_month   = decode(cast(? as varchar(6)),'000000','',cast(? as varchar(6))),";
        updateSQL += "no_penalty_e_month   = decode(cast(? as varchar(6)),'999999','',cast(? as varchar(6))),";
        updateSQL += "no_interest_flag     = decode(cast(? as varchar(6)),'000000','N','Y'),";
        updateSQL += "no_interest_s_month  = decode(cast(? as varchar(6)),'000000','',cast(? as varchar(6))),";
        updateSQL += "no_interest_e_month  = decode(cast(? as varchar(6)),'999999','',cast(? as varchar(6))),";
        updateSQL += "stat_unprint_flag    = decode(cast(? as varchar(6)),'000000','N','Y'),";
        updateSQL += "stat_unprint_s_month = decode(cast(? as varchar(6)),'000000','',cast(? as varchar(6))),";
        updateSQL += "stat_unprint_e_month = decode(cast(? as varchar(6)),'999999','',cast(? as varchar(6))),";
        updateSQL += "no_tel_coll_flag     = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += "no_tel_coll_s_date   = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += "no_tel_coll_e_date   = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += "no_sms_flag          = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += "no_sms_s_date        = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += "no_sms_e_date        = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += "no_f_stop_flag       = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += "no_f_stop_s_date     = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += "no_f_stop_e_date     = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += "noauto_balance_flag  = ?,";
        updateSQL += "noauto_balance_date1 = decode(cast(? as varchar(8)),'00000000','',";
        updateSQL += "                       substr(?,1,6)),";
        updateSQL += "noauto_balance_date2 = decode(cast(? as varchar(8)),'99999999','',";
        updateSQL += "                       substr(?,1,6)),";
        // updateSQL += "block_reason = ?,";
        // updateSQL += "block_reason2 = ?,";
        // updateSQL += "block_date = ?,";
        // updateSQL += "block_status = nvl(? , block_status),";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hAcnoNoDelinquentSDate);
        setString(2, hAcnoNoDelinquentSDate);
        setString(3, hAcnoNoDelinquentSDate);
        setString(4, hAcnoNoDelinquentEDate);
        setString(5, hAcnoNoDelinquentEDate);
        setString(6, hAcnoNoCollectionSDate);
        setString(7, hAcnoNoCollectionSDate);
        setString(8, hAcnoNoCollectionSDate);
        setString(9, hAcnoNoCollectionEDate);
        setString(10, hAcnoNoCollectionEDate);
        setString(11, hAcnoNoPenaltySMonth);
        setString(12, hAcnoNoPenaltySMonth);
        setString(13, hAcnoNoPenaltySMonth);
        setString(14, hAcnoNoPenaltyEMonth);
        setString(15, hAcnoNoPenaltyEMonth);
        setString(16, hAcnoNoInterestSMonth);
        setString(17, hAcnoNoInterestSMonth);
        setString(18, hAcnoNoInterestSMonth);
        setString(19, hAcnoNoInterestEMonth);
        setString(20, hAcnoNoInterestEMonth);
        setString(21, hAcnoStatUnprintSMonth);
        setString(22, hAcnoStatUnprintSMonth);
        setString(23, hAcnoStatUnprintSMonth);
        setString(24, hAcnoStatUnprintEMonth);
        setString(25, hAcnoStatUnprintEMonth);
        setString(26, hAcnoNoTelCollSDate);
        setString(27, hAcnoNoTelCollSDate);
        setString(28, hAcnoNoTelCollSDate);
        setString(29, hAcnoNoTelCollEDate);
        setString(30, hAcnoNoTelCollEDate);
        setString(31, hAcnoNoSmsSDate);
        setString(32, hAcnoNoSmsSDate);
        setString(33, hAcnoNoSmsSDate);
        setString(34, hAcnoNoSmsEDate);
        setString(35, hAcnoNoSmsEDate);
        setString(36, hAcnoNoFStopSDate);
        setString(37, hAcnoNoFStopSDate);
        setString(38, hAcnoNoFStopSDate);
        setString(39, hAcnoNoFStopEDate);
        setString(40, hAcnoNoFStopEDate);
        setString(41, hAcnoNoautoBalanceFlag);
        setString(42, hAcnoNoautoBalanceDate1);
        setString(43, hAcnoNoautoBalanceDate1);
        setString(44, hAcnoNoautoBalanceDate2);
        setString(45, hAcnoNoautoBalanceDate2);
        // setString(46, h_acno_block_reason);
        // setString(47, h_acno_block_reason2);
        // setString(48, h_busi_business_date);
        // setString(49, h_acno_block_status);
        setString(46, javaProgram);
        setRowId(47, hAcnoRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "c.id_p_seqno, ";
        sqlCmd += "c.card_no, ";
        sqlCmd += "e.block_reason2||e.block_reason3||e.block_reason4||e.block_reason5  block_reason2, "; 
        sqlCmd += "e.block_reason1 block_reason, "; 
        sqlCmd += "c.rowid as rowid ";
        sqlCmd += "from crd_card c ";
//        sqlCmd += "  left join cca_card_acct e on c.p_seqno = e.p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";  //find block_reason in cca_card_acct
//        sqlCmd += "where c.p_seqno = ? ";
        sqlCmd += "  left join cca_card_acct e on c.acno_p_seqno = e.acno_p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";  //find block_reason in cca_card_acct
        sqlCmd += "where c.acno_p_seqno = ? ";
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
//        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "where acno_p_seqno = ? ";
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
    void updateColLiauRemod() throws Exception {
        daoTable = "col_liau_remod";
        updateSQL = "proc_date = ?, ";
        updateSQL += "proc_flag = 'Y',";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hCludRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liau_remod not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateCrdCard() throws Exception {
        daoTable = "crd_card";
        // updateSQL = "block_reason = ?, "; //no column
        // updateSQL += "block_reason2 = ?,";
        // updateSQL += "block_status = nvl(?,block_status),";
        // updateSQL += "block_date = ?,";
        updateSQL = "block_date = ?, ";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        // setString(1, h_card_block_reason);
        // setString(2, h_card_block_reason2);
        // setString(3, h_card_block_status);
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hCardRowid);

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
//        setValue("acct_seqno", h_acno_p_seqno);
        setValue(extendField+"acno_p_seqno", hAcnoPSeqno);
        setValue(extendField+"acct_type", hAcnoAcctType);
        // setValue("acct_key", h_acno_acct_key); //no column
        setValue(extendField+"id_p_seqno", hInt == 1 ? hCardIdPSeqno : hAcnoIdPSeqno);
        setValue(extendField+"log_date", hBusiBusinessDate);
        setValue(extendField+"log_mode", "2"); /* online:1 batch:2 */
        setValue(extendField+"log_type", "3"); /* 凍結:3 解凍:4 */
        setValue(extendField+"log_reason", "");
        setValue(extendField+"log_not_reason", "");
        setValue(extendField+"block_reason", hInt == 1 ? hCardBlockReason : hAcnoBlockReason);
        String blockReason2 = hInt == 1 ? hCardBlockReason2 : hAcnoBlockReason2;
        setValue(extendField+"block_reason2", comc.getSubString(blockReason2, 0, 2));
        setValue(extendField+"block_reason3", comc.getSubString(blockReason2, 2, 2));
        setValue(extendField+"block_reason4", comc.getSubString(blockReason2, 4, 2));
        setValue(extendField+"block_reason5", comc.getSubString(blockReason2 ,6, 2));
        setValue(extendField+"fit_cond", "ECS");
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
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
        hClbmNoPenaltyFlag = "";
        hClbmNoSmsFlag = "";
        hClbmBlockFlag = "";
        hClbmBlockMark1 = "";
        hClbmBlockMark3 = "";
        hClbmOppostFlag = "";
        hClbmOppostReason = "";
        hClbmNoautoBalanceFlag = "";
        hClbmSendCsFlag = "";
        hClbmNoInterestFlag = "";

        sqlCmd = "select stat_unprint_flag,";
        sqlCmd += "no_tel_coll_flag, ";
        sqlCmd += "no_delinquent_flag, ";
        sqlCmd += "no_collection_flag, ";
        sqlCmd += "no_f_stop_flag, ";
        sqlCmd += "no_penalty_flag, ";
        sqlCmd += "no_sms_flag, ";
        sqlCmd += "block_flag, ";
        sqlCmd += "block_mark1, ";
        sqlCmd += "block_mark3, ";
        sqlCmd += "oppost_flag, ";
        sqlCmd += "oppost_reason, ";
        sqlCmd += "noauto_balance_flag, ";
        sqlCmd += "send_cs_flag, ";
        sqlCmd += "no_interest_flag ";
        sqlCmd += "from col_liab_param ";
        sqlCmd += "where apr_date <> '' ";
        sqlCmd += "and   liab_status  = ? ";
        sqlCmd += "and   liab_type    = '6' ";
        setString(1, hCludLiauStatus);
        
        extendField = "col_liab_param.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClbmStatUnprintFlag = getValue("col_liab_param.stat_unprint_flag");
            hClbmNoTelCollFlag = getValue("col_liab_param.no_tel_coll_flag");
            hClbmNoDelinquentFlag = getValue("col_liab_param.no_delinquent_flag");
            hClbmNoCollectionFlag = getValue("col_liab_param.no_collection_flag");
            hClbmNoFStopFlag = getValue("col_liab_param.no_f_stop_flag");
            hClbmNoPenaltyFlag = getValue("col_liab_param.no_penalty_flag");
            hClbmNoSmsFlag = getValue("col_liab_param.no_sms_flag");
            hClbmBlockFlag = getValue("col_liab_param.block_flag");
            hClbmBlockMark1 = getValue("col_liab_param.block_mark1");
            hClbmBlockMark3 = getValue("col_liab_param.block_mark3");
            hClbmOppostFlag = getValue("col_liab_param.oppost_flag");
            hClbmOppostReason = getValue("col_liab_param.oppost_reason");
            hClbmNoautoBalanceFlag = getValue("col_liab_param.noauto_balance_flag");
            hClbmSendCsFlag = getValue("col_liab_param.send_cs_flag");
            hClbmNoInterestFlag = getValue("col_liab_param.no_interest_flag");
        }
    }

    /***********************************************************************/
    void insertCrdStopLog() throws Exception {
        sqlCmd = "select substr(to_char(ecs_stop.nextval,'0000000000'),2,10) ecs_stop ";
        sqlCmd += "from dual ";

        if (selectTable() > 0) {
            hStopProcSeqno = getValue("ecs_stop");
        }

        dateTime();
        daoTable = "crd_stop_log";
        extendField = daoTable + ".";
        setValue(extendField+"proc_seqno", hStopProcSeqno);
        setValue(extendField+"crt_time", sysDate + sysTime);
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
        // sqlCmd += "major_id,";
        // sqlCmd += "major_id_code,";
        sqlCmd += "uf_idno_id(major_id_p_seqno) major_id,";
        sqlCmd += "'0',";
        sqlCmd += "?,";
        // sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'',id),";
        // sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'',id_code),";
        sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'',uf_idno_id(id_p_seqno)),";
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
        // setValue("id", h_acno_acct_holder_id);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"modify_type", "2");
        setValue(extendField+"noauto_balance_flag", hClbmNoautoBalanceFlag);
        setValue(extendField+"noauto_balance_date1", comc.getSubString(hAcnoNoautoBalanceDate1, 0, 6));
        setValue(extendField+"noauto_balance_date2", comc.getSubString(hAcnoNoautoBalanceDate2, 0, 6));
        setValue(extendField+"remark", "無擔保債務展延");
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
    public static void main(String[] args) throws Exception {
        ColA550 proc = new ColA550();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
