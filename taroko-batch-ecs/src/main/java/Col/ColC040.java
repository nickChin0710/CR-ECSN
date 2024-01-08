/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 106/08/08  V1.01.01  phopho     Initial                                    *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
*****************************************************************************/
package Col;

import com.*;

public class ColC040 extends AccessDAO {
    private String progname = "更生清算帳戶狀態設定處理程式  109/12/15  V1.00.01 ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";

    String hWdayThisAcctMonth = "";
    String hWdayNextAcctMonth = "";
    String hClbmStatUnprintFlag = "";
    String hClbmPayStageFlag = "";
    String hClbmPayStageMark = "";
    String hClbmSendCsFlag = "";
    String hClbmOppostFlag = "";
    String hClbmOppostReason = "";
    String hClbmNoautoBalanceFlag = "";
    String hCdrdId = "";
    String hCdrdIdPSeqno = "";
    String hCdrdLiadType = "";
    String hCdrdLiadStatus = "";
    String hCdrdProcFlag = "";
    String hCdrdRowid = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoIdPSeqno = "";
    String hAcnoRcUseIndicator = "";
    String hAcnoStatUnprintFlag = "";
    String hAcnoStatUnprintSMonth = "";
    String hAcnoStatUnprintEMonth = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoCardIndicator = "";
    String hAcnoNoautoBalanceFlag = "";
    String hAcnoNoautoBalanceDate1 = "";
    String hAcnoNoautoBalanceDate2 = "";
    String hAcnoRowid = "";
    String hCardCardNo = "";
    String hCardId = "";
    String hCardIdPSeqno = "";
    String hCardMajorId = "";
    String hCardMajorIdPSeqno = "";
    String hCardComboIndicator = "";
    String hCardRowid = "";
    String hCdjcRowid = "";
    String hIdnoIdPSeqno = "";
    String hIdnoIdNo = "";
    String hIdnoIdNoCode = "";
    String hIdnoChiName = "";
    String hIdnoBirthday = "";
    String hApscPmIdNo = "";
    String hApscPmIdNoCode = "";
    String hApscPmBirthday = "";
    String hApscPmName = "";
    String hApscSupIdNo = "";
    String hApscSupIdNoCode = "";
    String hApscSupBirthday = "";
    String hApscSupName = "";
    String hApscRowid = "";
    String hStopProcSeqno = "";

    long totalCnt = 0;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColC040 proc = new ColC040();
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
            if (args.length != 0) {
                comc.errExit("Usage : ColC040", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            selectColLiadRemod();

            showLogMessage("I", "", "處理帳戶筆數 : [" + totalCnt + "]");

            // ==============================================
            // 固定要做的
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
        hBusiBusinessDate = "";
        selectSQL = "business_date ";
        daoTable = "ptr_businday";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno );
        }
        hBusiBusinessDate = getValue("business_date");
    }

    // ************************************************************************
    private void selectColLiadRemod() throws Exception {
        selectSQL = "id_p_seqno, id_no, liad_type, liad_status, rowid as rowid";
        daoTable = "col_liad_remod";
        whereStr = "where decode(proc_flag,'','N',proc_flag) = 'N' order by mod_time,liad_status";

        openCursor();

        while (fetchTable()) {
        	hCdrdIdPSeqno = getValue("id_p_seqno");
        	hCdrdId = getValue("id_no");
            hCdrdLiadType = getValue("liad_type");
            hCdrdLiadStatus = getValue("liad_status");
            hCdrdProcFlag = "";
            hCdrdRowid = getValue("rowid");

            selectColLiabParam();
            selectActAcno();
            updateColLiadRemod();
            commitDataBase();
        }
        closeCursor();
    }

    // ************************************************************************
    private void selectActAcno() throws Exception {
////        selectSQL = "a.id_p_seqno, a.p_seqno, a.acct_status, a.acct_type, a.acct_key, "
//        selectSQL = "a.id_p_seqno, a.acno_p_seqno, a.acct_status, a.acct_type, a.acct_key, "
//                + "decode(a.stat_unprint_flag,'','N',a.stat_unprint_flag) stat_unprint_flag, "
//                + "decode(a.stat_unprint_s_month,'','000000',a.stat_unprint_s_month) stat_unprint_s_month, "
//                + "decode(a.stat_unprint_e_month,'','999999',a.stat_unprint_e_month) stat_unprint_e_month, "
//                + "a.pay_by_stage_flag, a.card_indicator, a.rc_use_indicator, "
//                + "decode(a.noauto_balance_flag,'','N',a.noauto_balance_flag) noauto_balance_flag, "
//                + "decode(a.noauto_balance_date1,'','00000000',a.noauto_balance_date1) noauto_balance_date1, "
//                + "decode(a.noauto_balance_date2,'','99999999',a.noauto_balance_date2) noauto_balance_date2, "
//                + "a.rowid as rowid, b.this_acct_month,b.next_acct_month ";
//        daoTable = "act_acno a,ptr_workday b,ptr_actgeneral_n c, crd_idno d";
////        whereStr = "where a.p_seqno = a.gp_no "
//        whereStr = "where a.acno_flag <> 'Y' " 
//                + "and    a.acct_type = c.acct_type "
//                + "and    a.stmt_cycle = b.stmt_cycle "
//                + "and    d.id_p_seqno = a.id_p_seqno "
//                + "and    d.id_no = ? ";
//        setString(1, h_cdrd_id);
        
        selectSQL = "a.id_p_seqno, a.acno_p_seqno, a.acct_status, a.acct_type, a.acct_key, "
                + "decode(a.stat_unprint_flag,'','N',a.stat_unprint_flag) stat_unprint_flag, "
                + "decode(a.stat_unprint_s_month,'','000000',a.stat_unprint_s_month) stat_unprint_s_month, "
                + "decode(a.stat_unprint_e_month,'','999999',a.stat_unprint_e_month) stat_unprint_e_month, "
                + "a.pay_by_stage_flag, a.card_indicator, a.rc_use_indicator, "
                + "decode(a.noauto_balance_flag,'','N',a.noauto_balance_flag) noauto_balance_flag, "
                + "decode(a.noauto_balance_date1,'','00000000',a.noauto_balance_date1) noauto_balance_date1, "
                + "decode(a.noauto_balance_date2,'','99999999',a.noauto_balance_date2) noauto_balance_date2, "
                + "a.rowid as rowid, b.this_acct_month,b.next_acct_month ";
        daoTable = "act_acno a,ptr_workday b,ptr_actgeneral_n c ";
        whereStr = "where a.acno_flag <> 'Y' " 
                + "and    a.acct_type = c.acct_type "
                + "and    a.stmt_cycle = b.stmt_cycle "
                + "and    a.id_p_seqno = ? ";
        setString(1, hCdrdIdPSeqno);
        
        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoIdPSeqno = getValue("act_acno.id_p_seqno", i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno", i);
            hAcnoAcctStatus = getValue("act_acno.acct_status", i);
            hAcnoAcctType = getValue("act_acno.acct_type", i);
            hAcnoAcctKey = getValue("act_acno.acct_key", i);
            hAcnoStatUnprintFlag = getValue("act_acno.stat_unprint_flag", i);
            hAcnoStatUnprintSMonth = getValue("act_acno.stat_unprint_s_month", i);
            hAcnoStatUnprintEMonth = getValue("act_acno.stat_unprint_e_month", i);
            hAcnoPayByStageFlag = getValue("act_acno.pay_by_stage_flag", i);
            hAcnoCardIndicator = getValue("act_acno.card_indicator", i);
            hAcnoRcUseIndicator = getValue("act_acno.rc_use_indicator", i);
            hAcnoNoautoBalanceFlag = getValue("act_acno.noauto_balance_flag", i);
            hAcnoNoautoBalanceDate1 = getValue("act_acno.noauto_balance_date1", i);
            hAcnoNoautoBalanceDate2 = getValue("act_acno.noauto_balance_date2", i);
            hAcnoRowid = getValue("act_acno.rowid", i);
            hWdayThisAcctMonth = getValue("act_acno.this_acct_month", i);
            hWdayNextAcctMonth = getValue("act_acno.next_acct_month", i);

            totalCnt++;

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
                if ((hAcnoNoautoBalanceFlag.equals("N"))
                        || ((hAcnoNoautoBalanceFlag.equals("Y"))
                                && ((hBusiBusinessDate.compareTo(hAcnoNoautoBalanceDate1) < 0)
                                        || (hBusiBusinessDate.compareTo(hAcnoNoautoBalanceDate2) > 0)))) {
                    hAcnoNoautoBalanceFlag = "N";
                    hAcnoNoautoBalanceDate1 = hBusiBusinessDate;
                    hAcnoNoautoBalanceDate2 = "";
                    insertActNoautoBalanceLog();
                }
            }

            if (hClbmOppostFlag.equals("Y"))
                selectCrdCard1();

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

            if (hClbmPayStageFlag.equals("Y")) {
                hAcnoPayByStageFlag = hClbmPayStageMark;
            } else {
                if (hAcnoPayByStageFlag.compareTo(hClbmPayStageMark) == 0)
                    hAcnoPayByStageFlag = "";
            }

            updateActAcno();
        }

    }

    // ************************************************************************
    private void updateActAcno() throws Exception {
        updateSQL = "pay_by_stage_flag = ?, stat_unprint_flag = decode(cast(? as varchar(6)),'000000','N','Y'), "
                + "stat_unprint_s_month = decode(cast(? as varchar(6)),'000000','',cast(? as varchar(6))), "
                + "stat_unprint_e_month = decode(cast(? as varchar(6)),'999999','',cast(? as varchar(6))), noauto_balance_flag = ?, "
                + "noauto_balance_date1 = decode(cast(? as varchar(8)),'00000000','',substr(?,1,6)), "
                + "noauto_balance_date2 = decode(cast(? as varchar(8)),'99999999','',substr(?,1,6)), "
                + "mod_time = sysdate, mod_pgm = ? ";
        daoTable = "act_acno";
        whereStr = "WHERE rowid = ? ";
        setString(1, hAcnoPayByStageFlag);
        setString(2, hAcnoStatUnprintSMonth);
        setString(3, hAcnoStatUnprintSMonth);
        setString(4, hAcnoStatUnprintSMonth);
        setString(5, hAcnoStatUnprintEMonth);
        setString(6, hAcnoStatUnprintEMonth);
        setString(7, hAcnoNoautoBalanceFlag);
        setString(8, hAcnoNoautoBalanceDate1);
        setString(9, hAcnoNoautoBalanceDate1);
        setString(10, hAcnoNoautoBalanceDate2);
        setString(11, hAcnoNoautoBalanceDate2);
        setString(12, javaProgram);
        setRowId(13, hAcnoRowid);

        updateTable();
        if (notFound.equals("Y")) {
            String err1 = "update_act_acno error!";
            String err2 = "rowid=[" + hAcnoRowid + "]";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void selectCrdCard1() throws Exception {
        selectSQL = "id_p_seqno, major_id_p_seqno, card_no, decode(combo_indicator,'','N',combo_indicator) combo_indicator, "
                + "rowid as rowid ";
        daoTable = "crd_card";
//        whereStr = "where gp_no = ? and   current_code = '0' ";
        whereStr = "where p_seqno = ? and current_code = '0' ";
        setString(1, hAcnoPSeqno);
        
        extendField = "crd_card_1.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardIdPSeqno = getValue("crd_card_1.id_p_seqno", i);
            hCardMajorIdPSeqno = getValue("crd_card_1.major_id_p_seqno", i);
            hCardCardNo = getValue("crd_card_1.card_no", i);
            hCardComboIndicator = getValue("crd_card_1.combo_indicator", i);
            hCardRowid = getValue("crd_card_1.rowid", i);

            if (hCardComboIndicator.equals("N") == false)
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

    // ************************************************************************
    private void updateColLiadRemod() throws Exception {
        daoTable = "col_liad_remod";
        updateSQL = "proc_date = ?, proc_flag = decode(cast(? as varchar(1)),'1','1','Y'), "
                + "mod_time = sysdate, mod_pgm = ? ";
        whereStr = "WHERE rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hCdrdProcFlag);
        setString(3, javaProgram);
        setRowId(4, hCdrdRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_col_liad_remod error!";
            String err2 = "rowid=[" + hCdrdRowid + "]";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void updateCrdCard1() throws Exception {
        daoTable = "crd_card";
        updateSQL = "oppost_reason = ?, oppost_date = ?, current_code = '1', mod_time = sysdate, "
                + "mod_pgm = ? ";
        whereStr = "WHERE rowid = ? ";
        setString(1, hClbmOppostReason);
        setString(2, hBusiBusinessDate);
        setString(3, javaProgram);
        setRowId(4, hCardRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_card_1 error!";
            String err2 = "rowid=[" + hCardRowid + "]";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void insertOnbat1() throws Exception {
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
            comcr.errRtn("insert_onbat_2ccas duplicate!", "", comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void selectColLiabParam() throws Exception {
        hClbmStatUnprintFlag = "";
        hClbmPayStageFlag = "";
        hClbmPayStageMark = "";
        hClbmOppostFlag = "";
        hClbmOppostReason = "";
        hClbmNoautoBalanceFlag = "";
        hClbmSendCsFlag = "";
        sqlCmd = "select stat_unprint_flag, ";
        sqlCmd += "pay_stage_flag, ";
        sqlCmd += "pay_stage_mark, ";
        sqlCmd += "oppost_flag, ";
        sqlCmd += "oppost_reason, ";
        sqlCmd += "noauto_balance_flag, ";
        sqlCmd += "send_cs_flag ";
        sqlCmd += "from col_liab_param where apr_date <> '' ";
        sqlCmd += "and liab_status = ? and liab_type = ? ";
        setString(1, hCdrdLiadStatus);
        setString(2, hCdrdLiadType);
        
        extendField = "col_liab_param.";

        if (selectTable() > 0) {
            hClbmStatUnprintFlag = getValue("col_liab_param.stat_unprint_flag");
            hClbmPayStageFlag = getValue("col_liab_param.pay_stage_flag");
            hClbmPayStageMark = getValue("col_liab_param.pay_stage_mark");
            hClbmOppostFlag = getValue("col_liab_param.oppost_flag");
            hClbmOppostReason = getValue("col_liab_param.oppost_reason");
            hClbmNoautoBalanceFlag = getValue("col_liab_param.noauto_balance_flag");
            hClbmSendCsFlag = getValue("col_liab_param.send_cs_flag");
        }
    }

    // ************************************************************************
    private void insertCrdStopLog() throws Exception {
        // first select
        sqlCmd = "select substr(to_char(ecs_stop.nextval,'0000000000'),2,10) ecs_stop ";
        sqlCmd += "from dual";

        if (selectTable() > 0) {
            hStopProcSeqno = getValue("ecs_stop");
        }

        // then insert
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
        setValue(extendField+"mod_user", comc.commGetUserID());
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();

        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_stop_log error[dupRecord]", "", comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private int selectCrdJcic() throws Exception {
        sqlCmd = "select rowid rowid ";
        sqlCmd += "from crd_jcic where card_no = ? ";
        sqlCmd += "and trans_type = 'C' and to_jcic_date = '' ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hCardCardNo);
        
        extendField = "crd_jcic.";

        selectTable();
        if (notFound.equals("Y"))
            return 1;
        hCdjcRowid = getValue("crd_jcic.rowid");
        return 0;
    }

    // ************************************************************************
    private void insertCrdJcic() throws Exception {
        dateTime();
        daoTable = "crd_jcic";
        extendField = daoTable + ".";
        setValue(extendField+"card_no", hCardCardNo);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_user", comc.commGetUserID());
        setValue(extendField+"trans_type", "C");
        setValue(extendField+"is_rc", hAcnoRcUseIndicator);
        setValue(extendField+"current_code", "1");
        setValue(extendField+"oppost_reason", hClbmOppostReason);
        setValue(extendField+"oppost_date", hBusiBusinessDate);
        setValue(extendField+"mod_user", comc.commGetUserID());
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();

        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_jcic error[dupRecord]", "", comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void updateCrdJcic() throws Exception {
        daoTable = "crd_jcic";
        updateSQL = "current_code = '1', oppost_reason = ?, oppost_date = ?, mod_time = sysdate, "
                + "mod_user = ?, mod_pgm = ? ";
        whereStr = "WHERE rowid = ? ";
        setString(1, hClbmOppostReason);
        setString(2, hBusiBusinessDate);
        setString(3, comc.commGetUserID());
        setString(4, javaProgram);
        setRowId(5, hCdjcRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_jcic error!";
            String err2 = "rowid=[" + hCdjcRowid + "]";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private int selectCrdApscard() throws Exception {
        sqlCmd = "select rowid rowid ";
        sqlCmd += "from crd_apscard where card_no = ? ";
        sqlCmd += "and to_aps_date = '' ";
        setString(1, hCardCardNo);
        
        extendField = "crd_apscard.";

        selectTable();
        if (notFound.equals("Y"))
            return 1;
        
        hApscRowid = getValue("crd_apscard.rowid");
        return 0;
    }

    // ************************************************************************
    private void updateCrdApscard() throws Exception {
        daoTable = "crd_apscard";
        updateSQL = "stop_reason = '1', stop_date = ?, status_code = '', mod_time = sysdate, "
                + "mod_user = 'batch', mod_pgm = ? ";
        whereStr = "WHERE rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hApscRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_apscard error!";
            String err2 = "rowid=[" + hApscRowid + "]";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void insertCrdApscard() throws Exception {
        hIdnoIdPSeqno = hCardMajorIdPSeqno;
        selectCrdIdno();
        hApscPmIdNo = hIdnoIdNo;
        hApscPmIdNoCode = hIdnoIdNoCode;
        hApscPmBirthday = hIdnoBirthday;
        hApscPmName = hIdnoChiName;
        hApscSupIdNo = "";
        hApscSupIdNoCode = "";
        hApscSupBirthday = "";
        hApscSupName = "";
        if (hCardIdPSeqno.compareTo(hCardMajorIdPSeqno) != 0) {
            hIdnoIdPSeqno = hCardIdPSeqno;
            selectCrdIdno();
            hApscSupIdNo = hIdnoIdNo;
            hApscSupIdNoCode = hIdnoIdNoCode;
            hApscSupBirthday = hIdnoBirthday;
            hApscSupName = hIdnoChiName;
        }
        
        sqlCmd = "insert into crd_apscard(";
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
        sqlCmd += " SELECT ";
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
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
//        sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'',id),";  //no column
//        sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'',id_code),";  //no column
        sqlCmd += "?,";
        sqlCmd += "?,";
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
        sqlCmd += "FROM  crd_card ";
        sqlCmd += "WHERE rowid = ? ";
        setString(1, hCardCardNo);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hApscPmIdNo);
        setString(5, hApscPmIdNoCode);
        setString(6, hApscPmBirthday);
        setString(7, hApscSupIdNo);
        setString(8, hApscSupIdNoCode);
        setString(9, hApscSupBirthday);
        setString(10, hApscPmName);
        setString(11, hApscSupName);
        setString(12, comc.commGetUserID());
        setString(13, javaProgram);
        setRowId(14, hCardRowid);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_apscard duplicate!", "", hCallBatchSeqno);
        }

//        // first select ?
//        String temp_mail_type = "";
//        String temp_mail_no = "";
//        String temp_mail_branch = "";
//        String temp_mail_proc_date = "";
//        String temp_major_id = "";
//        String temp_major_id_code = "";
//        String temp_sup_id = "";
//        String temp_sup_id_code = "";
//        String temp_corp_no = "";
//        String temp_corp_no_code = "";
//        String temp_card_type = "";
//        String temp_sup_lost_status = "";
//        String temp_group_code = "";
//        sqlCmd = "select mail_type,mail_no,mail_branch,mail_proc_date,major_id,";
//        sqlCmd += "major_id_code,decode(id_p_seqno,major_id_p_seqno,'',id) sup_id,";
//        sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'',id_code) sup_id_code,";
//        sqlCmd += "corp_no,corp_no_code,card_type,";
//        sqlCmd += "decode(id_p_seqno,major_id_p_seqno,'','0') sup_lost_status,group_code ";
//        sqlCmd += "from crd_card where rowid = ? ";
//        setRowId(1, h_card_rowid);
//
//        selectTable();
//        
//        if (notFound.equals("Y")) {
//            return;
//        }
//        temp_mail_type = getValue("mail_type");
//        temp_mail_no = getValue("mail_no");
//        temp_mail_branch = getValue("mail_branch");
//        temp_mail_proc_date = getValue("mail_proc_date");
//        temp_major_id = getValue("major_id");
//        temp_major_id_code = getValue("major_id_code");
//        temp_sup_id = getValue("sup_id");
//        temp_sup_id_code = getValue("sup_id_code");
//        temp_corp_no = getValue("corp_no");
//        temp_corp_no_code = getValue("corp_no_code");
//        temp_card_type = getValue("card_type");
//        temp_sup_lost_status = getValue("sup_lost_status");
//        temp_group_code = getValue("group_code");
//
//        // then insert ?
//        setValue("CRT_DATETIME", sysDate + sysTime);
//        setValue("card_no", h_card_card_no);
//        setValue("valid_date", h_busi_business_date);
//        setValue("stop_date", h_busi_business_date);
//        setValue("stop_reason", "1");
//        setValue("mail_type", temp_mail_type);
//        setValue("mail_no", temp_mail_no);
//        setValue("mail_branch", temp_mail_branch);
//        setValue("mail_date", temp_mail_proc_date);
//        setValue("pm_id", temp_major_id);
//        setValue("pm_id_code", temp_major_id_code);
//        setValue("pm_birthday", h_apsc_pm_birthday);
//        setValue("sup_id", temp_sup_id);
//        setValue("sup_id_code", temp_sup_id_code);
//        setValue("sup_birthday", h_apsc_sup_birthday);
//        setValue("corp_no", temp_corp_no);
//        setValue("corp_no_code", temp_corp_no_code);
//        setValue("card_type", temp_card_type);
//        setValue("pm_name", h_apsc_pm_name);
//        setValue("sup_name", h_apsc_sup_name);
//        setValue("sup_lost_status", temp_sup_lost_status);
//        setValue("status_code", "");
//        setValue("group_code", temp_group_code);
//        setValue("mod_user", comc.comm_GetUserID());
//        setValue("mod_time", sysDate + sysTime);
//        setValue("mod_pgm", javaProgram);
//
//        daoTable = "crd_apscard";
//
//        insertTable();
//
//        if (dupRecord.equals("Y")) {
//            comcr.err_rtn("insert_crd_apscard error[dupRecord]", "", comcr.h_call_batch_seqno);
//        }
    }

    // ************************************************************************
    private void selectCrdIdno() throws Exception {
        sqlCmd = "select id_no, id_no_code, ";  //phopho add
        sqlCmd += "birthday, chi_name ";
        sqlCmd += "from crd_idno ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hIdnoIdPSeqno);
        
        extendField = "crd_idno.";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found", "", comcr.hCallBatchSeqno);
        }
        hIdnoIdNo = getValue("crd_idno.id_no");
        hIdnoIdNoCode = getValue("crd_idno.id_no_code");
        hIdnoBirthday = getValue("crd_idno.birthday");
        hIdnoChiName = getValue("crd_idno.chi_name");
    }

    // ************************************************************************
    private void insertActNoautoBalanceLog() throws Exception {
    	daoTable = "act_noauto_balance_log";
    	extendField = daoTable + ".";
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"acct_key", hAcnoAcctKey);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"modify_type", "2");
        setValue(extendField+"noauto_balance_flag", hClbmNoautoBalanceFlag);
        setValue(extendField+"noauto_balance_date1", comc.getSubString(hAcnoNoautoBalanceDate1, 0, 6));
        setValue(extendField+"noauto_balance_date2", comc.getSubString(hAcnoNoautoBalanceDate2, 0, 6));
        setValue(extendField+"remark", hCdrdLiadType.equals("3") ? "更生" : "清算");
        setValue(extendField+"crt_user", javaProgram);
        setValue(extendField+"apr_user", "SYSTEM");
        setValue(extendField+"apr_date", hBusiBusinessDate);
        setValue(extendField+"apr_flag", "Y");
        setValue(extendField+"mod_user", javaProgram);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();

        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_noauto_balance_log error[dupRecord]", "", comcr.hCallBatchSeqno);
        }
    }
    // ************************************************************************
}