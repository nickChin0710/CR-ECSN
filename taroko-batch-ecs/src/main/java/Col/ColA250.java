/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/17  V1.00.00    phopho     program initial                          *
*  109/01/14  V1.00.01  phopho     fix update_cca_card_acct()                 *
*  109/12/07  V1.00.05    shiyuqi       updated for project coding standard   *
*  110/08/30  V1.00.02  Ryan       update mantis 8519                         *
*  110/10/19  V1.00.03   Ryan       update_act_acno  revolve_int_rate 負號問題     *
*  110/10/21  V1.00.04   Ryan       update_act_acno  revolve_int_rate<0 就帶0      *
*  111/07/25  V1.00.05  Sunny       同步220520版本,revolve_int_rate負號問題                    * 
******************************************************************************/

package Col;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColA250 extends AccessDAO {
    private String progname = "債協資料帳戶狀態設定處理程式  111/07/25  V1.00.05 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf                  = "";
    String szTmp                = "";
    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";

    String hWdayThisAcctMonth = "";
    String hClbmStatUnprintFlag = "";
    String hClbmNoTelCollFlag = "";
    String hClbmNoDelinquentFlag = "";
    String hClbmNoCollectionFlag = "";
    String hClbmNoFStopFlag = "";
    String hClbmRevolveRateFlag = "";
    String hClbmNoPenaltyFlag = "";
    String hClbmNoSmsFlag = "";
    String hClbmMinPayFlag = "";
    String hClbmAutopayFlag = "";
    String hClbmPayStageFlag = "";
    String hClbmPayStageMark       = "";
    String hClbmBlockFlag = "";
    String hClbmBlockMark1 = "";
    String hClbmBlockMark3 = "";
    String hClbmSendCsFlag = "";
    String hClboId = "";
    String hClboIdPSeqno = "";
    String hClboEndDate = "";
    String hClboLiabStatus = "";
    String hClboOriLiabStatus = "";
    String hClboForceFlag = "";
    String hClboModTime = "";
    String hClboRowid = "";
    double hClbnSuccYearIntRate = 0;
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
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
    String hAcnoNoCollectionFlag   = "";
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

    int    totalCnt  = 0;
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
            if (args.length > 2) {
                comc.errExit("Usage : ColA250 [id] [batch_seqno]", "");
            }
            
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;
            
            comcr.callbatch(0, 0, 0);

            hTempId = "";
            if (args.length > 0 && args[0].length() != 20) {
                hTempId = args[0];
            }
            selectPtrBusinday();

            selectColLiabParam();
            selectColLiabRemod();

            showLogMessage("I", "", "總計執行筆數 : [" + totalCnt + "]");

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
//        sqlCmd = "select business_date ";
//        sqlCmd += "from ptr_businday ";
        sqlCmd = "select to_char(sysdate,'yyyymmdd') as business_date ";
        sqlCmd += "from dual ";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectColLiabRemod() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_p_seqno, ";  //以 id_p_seqno 為主
        sqlCmd += "id_no, ";
        sqlCmd += "ori_liab_status, ";
        sqlCmd += "liab_status, ";
        sqlCmd += "force_flag, ";
        sqlCmd += "end_date, ";
        sqlCmd += "mod_time ";
        sqlCmd += "from col_liab_remod ";
        sqlCmd += "where batch_proc_date <> '' ";
        sqlCmd += "and   block_proc_date = '' ";
        sqlCmd += "and   nopro_flag = 'N' ";
        sqlCmd += "group by id_no,id_p_seqno,ori_liab_status,liab_status,force_flag,end_date,mod_time ";
        sqlCmd += "order by mod_time ";

        openCursor();
        while (fetchTable()) {
            hClboIdPSeqno = getValue("id_p_seqno");
            hClboId = getValue("id_no");
            hClboOriLiabStatus = getValue("ori_liab_status");
            hClboLiabStatus = getValue("liab_status");
            hClboForceFlag = getValue("force_flag");
            hClboEndDate = getValue("end_date");
            hClboModTime = getValue("mod_time");
            showLogMessage("I", "", totalCnt+": h_clbo_id= " + hClboId +", h_clbo_id_p_seqno= " + hClboIdPSeqno);

            totalCnt++;
            if (totalCnt % 3000 == 0) {
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");
                commitDataBase();
            }

            if ((comc.getSubString(hClboLiabStatus,0,1).equals("A")) || (comc.getSubString(hClboLiabStatus,0,1).equals("B"))) {
                updateColLiabRemod();
                commitDataBase();
                continue;
            }

            if (!comc.getSubString(hClboForceFlag,0,1).equals("Y")) {
                if (hClboEndDate.length() == 0) {
                    if (selectColLiabNego() != 0) {
                        updateColLiabRemod();
                        commitDataBase();
                        continue;
                    }
                }
            }

            selectColLiabParam();
            selectActAcno();
            updateColLiabRemod();
            commitDataBase();
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateColLiabRemod() throws Exception {
        daoTable = "col_liab_remod";
        updateSQL = "block_proc_date = ?, ";
        updateSQL += "nopro_date = ?,";
        updateSQL += "nopro_flag = 'Y',";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
//        whereStr = "where id_no = ? ";
        whereStr = "where id_p_seqno = ? ";
        whereStr += "and  nopro_flag = 'N' ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, javaProgram);
//        setString(4, h_clbo_id);
        setString(4, hClboIdPSeqno);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liab_remod not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.id_p_seqno, ";
//        sqlCmd += "a.p_seqno, ";
        sqlCmd += "a.acno_p_seqno, ";
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
        sqlCmd += "decode(a.revolve_rate_s_month,'','000000',a.revolve_rate_s_month) revolve_rate_s_month, ";
        sqlCmd += "decode(a.revolve_rate_e_month,'','999999',a.revolve_rate_e_month) revolve_rate_e_month, ";
        sqlCmd += "decode(a.autopay_acct_s_date,'','00000000',a.autopay_acct_s_date) autopay_acct_s_date, ";
        sqlCmd += "decode(a.autopay_acct_e_date,'','99999999',a.autopay_acct_e_date) autopay_acct_e_date, ";
        sqlCmd += "a.pay_by_stage_flag, ";
        sqlCmd += "e.block_reason1, ";
        sqlCmd += "e.block_reason2 ||e.block_reason3 ||e.block_reason4 ||e.block_reason5 block_reason2, ";
        sqlCmd += "a.card_indicator, ";
//        sqlCmd += "d.id_no acct_holder_id, ";
        sqlCmd += "uf_idno_id(a.id_p_seqno) acct_holder_id, ";
        sqlCmd += "a.rowid as rowid, ";
        sqlCmd += "b.this_acct_month, ";
        sqlCmd += "round(decode(a.acct_status,'3',c.revolving_interest2,'4',";
        sqlCmd += "c.revolving_interest2,c.revolving_interest1) - ? *100.0/365,3) revolve_int_rate ";
        sqlCmd += "from act_acno a, ptr_workday b, ptr_actgeneral_n c ";
//        sqlCmd += "  left join crd_idno d on a.id_p_seqno = d.id_p_seqno ";  //find id_no in crd_idno
//        sqlCmd += "  left join cca_card_acct e on a.p_seqno = e.p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";  //find block_reason in cca_card_acct
        sqlCmd += "  left join cca_card_acct e on a.acno_p_seqno = e.acno_p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";  //find block_reason in cca_card_acct
        sqlCmd += "where a.acno_flag      <> 'Y' ";
        sqlCmd += "and   a.acct_type      = c.acct_type ";
        sqlCmd += "and   a.stmt_cycle     = b.stmt_cycle ";
//        sqlCmd += "and d.id_no = ? "; //a.acct_holder_id
        sqlCmd += "and   a.id_p_seqno     = ? ";
        setDouble(1, hClbnSuccYearIntRate);
//        setString(2, h_clbo_id);
        setString(2, hClboIdPSeqno);
        
        extendField = "act_acno.";
        
        noTrim = "Y";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoIdPSeqno = getValue("act_acno.id_p_seqno", i);
//            h_acno_p_seqno = getValue("p_seqno", i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno", i);
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
            hAcnoRevolveRateSMonth = getValue("act_acno.revolve_rate_s_month", i);
            hAcnoRevolveRateEMonth = getValue("act_acno.revolve_rate_e_month", i);
            hAcnoAutopayAcctSDate = getValue("act_acno.autopay_acct_s_date", i);
            hAcnoAutopayAcctEDate = getValue("act_acno.autopay_acct_e_date", i);
            hAcnoPayByStageFlag = getValue("act_acno.pay_by_stage_flag", i);
            hAcnoBlockReason = getValue("act_acno.block_reason1", i);
            hAcnoBlockReason2 = getValue("act_acno.block_reason2", i);
            hAcnoCardIndicator = getValue("act_acno.card_indicator", i);
            hAcnoAcctHolderId = getValue("act_acno.acct_holder_id", i);
            hAcnoRowid = getValue("act_acno.rowid", i);
            hWdayThisAcctMonth = getValue("act_acno.this_acct_month", i);
            hAcnoRevolveIntRate = getValueDouble("act_acno.revolve_int_rate", i);
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
                if (hAcnoRevolveRateSMonth.compareTo("000000") != 0) {
                    if (hWdayThisAcctMonth.compareTo(hAcnoRevolveRateSMonth) < 0) {
                        hAcnoRevolveIntRate = 0;
                        hAcnoRevolveIntSign = "";
                        hAcnoRevolveRateSMonth = "";
                        hAcnoRevolveRateEMonth = "";
                    } else {
                        if (hWdayThisAcctMonth.compareTo(hAcnoRevolveRateEMonth) < 0)
                            hAcnoRevolveRateEMonth = hWdayThisAcctMonth;
                    }
                }
            } else if (hClbmRevolveRateFlag.equals("Y")) {
                hAcnoRevolveRateSMonth = hWdayThisAcctMonth;
                hAcnoRevolveRateEMonth = "";
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
                if (hAcnoPayByStageFlag.compareTo("NE") == 0)
                    hAcnoPayByStageFlag = "";
            }
            // 10
            hAcnoBlockStatus = "";
            if (hClbmBlockFlag.equals("Y")) {
                if ((comc.getSubString(hAcnoBlockReason2,2,4).compareTo(comc.getSubString(hClbmBlockMark3,0,2)) != 0)) {
                    hAcnoBlockReason2 = String.format("%-8.8s", hAcnoBlockReason2);
                    if (hClbmBlockMark3.length() == 0)
                        hClbmBlockMark3 = "  ";
                    hAcnoBlockReason2 = comc.getSubString(hAcnoBlockReason2,0,2) + comc.getSubString(hClbmBlockMark3,0,2) + comc.getSubString(hAcnoBlockReason2,4);
                    hAcnoBlockStatus = "Y";
                    //insertOnbat(0, 3);
                }

                if (hAcnoBlockReason.compareTo(hClbmBlockMark1) != 0) {
                    hAcnoBlockReason = String.format("%-2.2s", hAcnoBlockReason);
                    if (hClbmBlockMark1.length() == 0)
                        hClbmBlockMark1 = "  ";
                    hAcnoBlockReason = comc.getSubString(hClbmBlockMark1,0,2);
                    hAcnoBlockStatus = "Y";
                    //insertOnbat(0, 1);
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

        noTrim = "N";
    }

    /***********************************************************************/
    void updateActAcno() throws Exception {
        daoTable = "act_acno";
        updateSQL = "pay_by_stage_flag     = ?, ";
        updateSQL += "no_delinquent_flag   = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += "no_delinquent_s_date = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += "no_delinquent_e_date = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += "no_collection_flag   = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += "no_collection_s_date = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += "no_collection_e_date = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += "no_penalty_flag      = decode(cast(? as varchar(6)),'000000','N','Y'),";
        updateSQL += "no_penalty_s_month   = decode(cast(? as varchar(6)),'000000','',cast(? as varchar(6))),";
        updateSQL += "no_penalty_e_month   = decode(cast(? as varchar(6)),'999999','',cast(? as varchar(6))),";
        updateSQL += "stat_unprint_flag    = decode(cast(? as varchar(6)),'000000','N','Y'),";
        updateSQL += "stat_unprint_s_month = decode(cast(? as varchar(6)),'000000','',cast(? as varchar(6))),";
        updateSQL += "stat_unprint_e_month = decode(cast(? as varchar(6)),'999999','',cast(? as varchar(6))),";
        updateSQL += "no_tel_coll_flag     = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += "no_tel_coll_s_date   = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += "no_tel_coll_e_date   = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += "no_sms_flag          = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += "no_sms_s_date        = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += "no_sms_e_date        = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += "autopay_acct_s_date  = decode(autopay_acct_bank,'','',";
        updateSQL += "                       decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8)))),";
        updateSQL += "autopay_acct_e_date  = decode(autopay_acct_bank,'','',";
        updateSQL += "                       decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8)))),";
        updateSQL += "no_f_stop_flag       = decode(cast(? as varchar(8)),'00000000','N','Y'),";
        updateSQL += "no_f_stop_s_date     = decode(cast(? as varchar(8)),'00000000','',cast(? as varchar(8))),";
        updateSQL += "no_f_stop_e_date     = decode(cast(? as varchar(8)),'99999999','',cast(? as varchar(8))),";
        updateSQL += "revolve_rate_s_month = decode(cast(? as varchar(6)),'000000','',cast(? as varchar(6))),";
        updateSQL += "revolve_rate_e_month = decode(cast(? as varchar(6)),'999999','',cast(? as varchar(6))),";
        updateSQL += "revolve_int_sign     = decode(cast(? as double),0,'','-'),";
        updateSQL += "revolve_int_rate     = ?,";
        updateSQL += "liab_status          = decode(cast(? as varchar(1)),'4',";
        updateSQL += "                       decode(cast(? as varchar(1)),'3','3',";
        updateSQL += "                              liab_status),liab_status),";
        updateSQL += "liab_end_date        = decode(cast(? as varchar(1)),'4',";
        updateSQL += "                       decode(cast(? as varchar(1)),'3',";
        updateSQL += "                              cast(? as varchar(8)),liab_end_date),liab_end_date),";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hAcnoPayByStageFlag);
        setString(2, hAcnoNoDelinquentSDate);
        setString(3, hAcnoNoDelinquentSDate);
        setString(4, hAcnoNoDelinquentSDate);
        setString(5, hAcnoNoDelinquentEDate);
        setString(6, hAcnoNoDelinquentEDate);
        setString(7, hAcnoNoCollectionSDate);
        setString(8, hAcnoNoCollectionSDate);
        setString(9, hAcnoNoCollectionSDate);
        setString(10, hAcnoNoCollectionEDate);
        setString(11, hAcnoNoCollectionEDate);
        setString(12, hAcnoNoPenaltySMonth);
        setString(13, hAcnoNoPenaltySMonth);
        setString(14, hAcnoNoPenaltySMonth);
        setString(15, hAcnoNoPenaltyEMonth);
        setString(16, hAcnoNoPenaltyEMonth);
        setString(17, hAcnoStatUnprintSMonth);
        setString(18, hAcnoStatUnprintSMonth);
        setString(19, hAcnoStatUnprintSMonth);
        setString(20, hAcnoStatUnprintEMonth);
        setString(21, hAcnoStatUnprintEMonth);
        setString(22, hAcnoNoTelCollSDate);
        setString(23, hAcnoNoTelCollSDate);
        setString(24, hAcnoNoTelCollSDate);
        setString(25, hAcnoNoTelCollEDate);
        setString(26, hAcnoNoTelCollEDate);
        setString(27, hAcnoNoSmsSDate);
        setString(28, hAcnoNoSmsSDate);
        setString(29, hAcnoNoSmsSDate);
        setString(30, hAcnoNoSmsEDate);
        setString(31, hAcnoNoSmsEDate);
        setString(32, hAcnoAutopayAcctSDate);
        setString(33, hAcnoAutopayAcctSDate);
        setString(34, hAcnoAutopayAcctEDate);
        setString(35, hAcnoAutopayAcctEDate);
        setString(36, hAcnoNoFStopSDate);
        setString(37, hAcnoNoFStopSDate);
        setString(38, hAcnoNoFStopSDate);
        setString(39, hAcnoNoFStopEDate);
        setString(40, hAcnoNoFStopEDate);
        setString(41, hAcnoRevolveRateSMonth);
        setString(42, hAcnoRevolveRateSMonth);
        setString(43, hAcnoRevolveRateEMonth);
        setString(44, hAcnoRevolveRateEMonth);
//        setDouble(45, hAcnoRevolveIntRate);
//        setDouble(46, hAcnoRevolveIntRate);
        setDouble(45, hAcnoRevolveIntRate<0?0:hAcnoRevolveIntRate);
        setDouble(46, hAcnoRevolveIntRate<0?0:hAcnoRevolveIntRate);
        setString(47, hClboLiabStatus);
        setString(48, hClboOriLiabStatus);
        setString(49, hClboLiabStatus);
        setString(50, hClboOriLiabStatus);
        setString(51, hClboEndDate);
        setString(52, javaProgram);
        setRowId(53, hAcnoRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.id_p_seqno, ";
        sqlCmd += "a.card_no, ";
        sqlCmd += "b.block_reason2||b.block_reason3||b.block_reason4||b.block_reason5 block_reason2, "; 
        sqlCmd += "b.block_reason1, ";  
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from crd_card a, cca_card_acct b ";
//        sqlCmd += "where a.p_seqno = ? ";
//        sqlCmd += "and   a.p_seqno = b.p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";
        sqlCmd += "where a.acno_p_seqno = ? ";
        sqlCmd += "and   a.acno_p_seqno = b.acno_p_seqno and decode(debit_flag,'','N',debit_flag) = 'N' ";
        sqlCmd += "and   a.current_code = '0' ";
        setString(1, hAcnoPSeqno);
        
        extendField = "crd_card.";
        
        noTrim = "Y";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardIdPSeqno = getValue("crd_card.id_p_seqno", i);
            hCardCardNo = getValue("crd_card.card_no", i);
            hCardBlockReason2 = getValue("crd_card.block_reason2", i);
            hCardBlockReason = getValue("crd_card.block_reason1", i);
            hCardRowid = getValue("crd_card.rowid", i);

            hCardBlockReason2 = String.format("%-8.8s", hCardBlockReason2);
            hCardBlockReason = String.format("%-2.2s", hCardBlockReason);
            hCardBlockStatus = "";
            if ((comc.getSubString(hCardBlockReason2,0,2).compareTo(comc.getSubString(hClbmBlockMark3,0,2)) != 0)) {
                if (hClbmBlockMark3.length() == 0)
                    hClbmBlockMark3 = "  ";
                hCardBlockReason2 = comc.getSubString(hCardBlockReason2,0,2) + comc.getSubString(hClbmBlockMark3,0,2)+ comc.getSubString(hCardBlockReason2,4);
                hCardBlockStatus = "Y";
                insertOnbat(1, 3);
                updateCrdCard();
            }
            if (hCardBlockReason.compareTo(hClbmBlockMark1) != 0) {
                if (hClbmBlockMark1.length() == 0)
                    hClbmBlockMark1 = "  ";
                hCardBlockReason = comc.getSubString(hClbmBlockMark1,0,2);
                hCardBlockStatus = "Y";
                insertOnbat(1, 1);
                updateCrdCard();
            }
        }
        noTrim = "N";
    }

    /***********************************************************************/
    void updateCrdCard() throws Exception {
//        daoTable = "crd_card";
//        updateSQL = "block_reason = ?, ";
//        updateSQL += "block_reason2 = ?,";
//        updateSQL += "block_status = nvl(?,block_status),";
//        updateSQL += "block_date = ?,";
//        updateSQL += "mod_time = sysdate, ";
//        updateSQL += "mod_pgm  = ? ";
//        whereStr = "where rowid = ? ";
//        whereStr += "and  nopro_flag = 'N' ";
//        setString(1, h_card_block_reason);
//        setString(2, h_card_block_reason2);
//        setString(3, h_card_block_status);
//        setString(4, h_busi_business_date);
//        setString(5, javaProgram);
//        setRowId(6, h_card_rowid);
        
        daoTable = "crd_card";
        updateSQL = "block_date = ?,";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hCardRowid);

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
        String blockReason2 = hInt == 1 ? hCardBlockReason2 : hAcnoBlockReason2;
        setValue(extendField+"block_reason2", comc.getSubString(blockReason2, 0, 2));
        setValue(extendField+"block_reason3", comc.getSubString(blockReason2, 2, 2));
        setValue(extendField+"block_reason4", comc.getSubString(blockReason2, 4, 2));
        setValue(extendField+"block_reason5", comc.getSubString(blockReason2, 6, 2));
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
//    		&& comc.getSubString(h_acno_block_reason2,2,4).trim().equals(h_clbm_block_mark3.trim())) {
//    	    return;
//    	}

//    	daoTable = "cca_card_acct";
//        updateSQL = "block_reason1 = ?, ";
//        updateSQL += "block_reason3 = ?, ";
//        updateSQL += "mod_time = sysdate, ";
//        updateSQL += "mod_pgm  = ? ";
//        whereStr = "where acno_p_seqno = ? ";
//        whereStr += "and decode(debit_flag,'','N',debit_flag) = 'N' ";
//        setString(1, hClbmBlockMark1);
//        setString(2, hClbmBlockMark3);
//        setString(3, javaProgram);
//        setString(4, hAcnoPSeqno);
    	
    	daoTable = "cca_card_acct";
        updateSQL = "block_reason1 = ?, ";
        updateSQL += "block_reason3 = ?, ";
        updateSQL += "block_status = decode(trim(?||block_reason2||?||block_reason4||block_reason5),'','N','Y'), ";
        updateSQL += "block_date = decode(trim(?||block_reason2||?||block_reason4||block_reason5),'',block_date,decode(block_date,'',?,block_date)), ";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where acno_p_seqno = ? ";
        whereStr += "and decode(debit_flag,'','N',debit_flag) = 'N' ";
        setString(1, hClbmBlockMark1);
        setString(2, hClbmBlockMark3);
        setString(3, hClbmBlockMark1);
        setString(4, hClbmBlockMark3);
        setString(5, hClbmBlockMark1);
        setString(6, hClbmBlockMark3);
        setString(7, hBusiBusinessDate);
        setString(8, javaProgram);
        setString(9, hAcnoPSeqno);
        
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
        hClbmNoSmsFlag = "";
        hClbmMinPayFlag = "";
        hClbmAutopayFlag = "";
        hClbmPayStageFlag = "";
        hClbmPayStageMark = "";
        hClbmBlockFlag = "";
        hClbmBlockMark1 = "";
        hClbmBlockMark3 = "";
        hClbmSendCsFlag = "";
        sqlCmd = "select stat_unprint_flag,";
        sqlCmd += "no_tel_coll_flag, ";
        sqlCmd += "no_delinquent_flag, ";
        sqlCmd += "no_collection_flag, ";
        sqlCmd += "no_f_stop_flag, ";
        sqlCmd += "revolve_rate_flag, ";
        sqlCmd += "no_penalty_flag, ";
        sqlCmd += "no_sms_flag, ";
        sqlCmd += "min_pay_flag, ";
        sqlCmd += "autopay_flag, ";
        sqlCmd += "pay_stage_flag, ";
        sqlCmd += "pay_stage_mark, ";
        sqlCmd += "block_flag, ";
        sqlCmd += "block_mark1, ";
        sqlCmd += "block_mark3, ";
        sqlCmd += "send_cs_flag ";
        sqlCmd += "from col_liab_param ";
        sqlCmd += "where apr_date <> '' ";
        sqlCmd += "and   liab_status  = ? ";
        sqlCmd += "and   liab_type    = '1' ";
        setString(1, hClboLiabStatus);
        
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
            hClbmNoSmsFlag = getValue("col_liab_param.no_sms_flag");
            hClbmMinPayFlag = getValue("col_liab_param.min_pay_flag");
            hClbmAutopayFlag = getValue("col_liab_param.autopay_flag");
            hClbmPayStageFlag = getValue("col_liab_param.pay_stage_flag");
            hClbmPayStageMark = getValue("col_liab_param.pay_stage_mark");
            hClbmBlockFlag = getValue("col_liab_param.block_flag");
            hClbmBlockMark1 = getValue("col_liab_param.block_mark1");
            hClbmBlockMark3 = getValue("col_liab_param.block_mark3");
            hClbmSendCsFlag = getValue("col_liab_param.send_cs_flag");
        }
    }

    /***********************************************************************/
    int selectColLiabNego() throws Exception {
        hClbnSuccYearIntRate = 0;
        sqlCmd = "select succ_year_int_rate ";
        sqlCmd += "from col_liab_nego ";
//        sqlCmd += "where id_no = ? ";
        sqlCmd += "where id_p_seqno = ? ";
//        setString(1, h_clbo_id);
        setString(1, hClboIdPSeqno);
        
        extendField = "col_liab_nego.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClbnSuccYearIntRate = getValueDouble("col_liab_nego.succ_year_int_rate");
        } else
            return 1;
        return 0;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA250 proc = new ColA250();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
