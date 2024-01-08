/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 106/08/16  V1.01.01    phopho     Initial                                  *
* 108/12/02  V1.01.02    phopho     fix err_rtn bug                          *
* 109/12/15  V1.00.03    shiyuqi       updated for project coding standard   *
* 109/12/30  V1.00.04    yanghan       修改了部分无意义的變量名稱            *
* 110/04/06  V1.00.05    Justin     use common value                         *
*****************************************************************************/
package Col;

import com.*;

import hdata.jcic.JcicEnum;

import java.text.Normalizer;

public class ColC065 extends AccessDAO {
    private String progname = "LGD 表二媒體產生處理  110/04/06  V1.00.05 ";
    private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_902;
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    String hBusiBusinessDate = "";

    String hLgd2IdCorpNo = "";
    String hLgd2IdCorpPSeqno = "";
    String hLgd2CloseDate = "";
    String hLgd2LgdSeqno = "";
    String hLgd2AudCode = "";
    String hLgd2LgdType = "";
    String hLgd2EarlyYm = "";
    double hLgd2RiskAmt = 0;
    String hLgd2OverdueYm = "";
    double hLgd2OverdueAmt = 0;
    String hLgd2CollYm = "";
    double hLgd2CollAmt = 0;
    double hLgd2RecvSelfAmt = 0;
    double hLgd2RecvRelaAmt = 0;
    double hLgd2RecvOthAmt = 0;
    double hLgd2CostsAmt = 0;
    String hLgd2CostsYm = "";
    double hLgd2RevolRate = 0;
    String hLgd2CrdtCharact = "";
    String hLgd2AssureType = "";
    String hLgd2CrdtUseType = "";
    String hLgd2SynLoanYn = "";
    String hLgd2SynLoanDate = "";
    String hLgd2FinaCommitYn = "";
    String hLgd2EcicCaseType = "";
    double hLgd2FinaCommitPrct = 0;
    String hLgd2CardRelaType = "";
    double hLgd2RecvTradeAmt = 0;
    double hLgd2RecvCollatAmt = 0;
    double hLgd2RecvFinaAmt = 0;
    String hLgd2CloseReason = "";
    String hLgd2CollatYn = "";
    String hLgd2FromType = "";
    String hLgd2Rowid = "";
    String hLgdbExecTimeS = "";
    String hLgdbFileName = "";
    String hEriaEcsIp = "";
    String hEriaRefIp = "";
    String hEriaRefName = "";
    String hEriaUserId = "";
    String hEriaUserPasswd = "";
    String hEriaTransType = "";
    String hEriaRemoteDir = "";
    String hEriaLocalDir = "";
    String hEriaPortNo = "";
    String hCallBatchSeqno = "";

    long ilTotCnt = 0;
    long totalCnt = 0;

    /*-program variable-*/
    String hSysDate = "";
    String hSysYymm = "";
    String hModUser = "";
    String hModPgm = "";
    String hParFileName = "";

    /*- temp variable-*/
    int fptr1;
    String szText = "";
    String cmdStr = "";
    String isSendfile = "";
    String isZipfile = "";
    String isFilename = "";
    long ilrowCnt = 0;
    int iiFileSeq = 0;
    int lgdSeqno = 0;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColC065 proc = new ColC065();
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

            // 檢查參數
            if (args.length > 2) {
                comc.errExit("Usage : ColC065 [file_name/callbatch_seqno]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            initFunc();
            hModPgm = javaProgram;
            hParFileName = "";
            if (args.length >= 1) {
                if (hCallBatchSeqno.length() == 20) {
                    comcr.callbatch(0, 0, 0);

                    sqlCmd = "select user_id ";
                    sqlCmd += "from ptr_callbatch where batch_seqno = ? ";
                    setString(1, hCallBatchSeqno);
                    if (selectTable() > 0) {
                        hModUser = getValue("user_id");
                    }
                }
                if (args[0].length() == 12) {
                    hParFileName = args[0];
                }
            }

            if (hModUser.length() == 0) {
                hModUser = comc.commGetUserID();
            }

            if (debug == 1)
                showLogMessage("I", "", "-->par-File-name=[" + hParFileName + "]");

            hLgdbFileName = "";
            if (hParFileName.length() == 0) {
                if (checkSendData() == 0) {
                    showLogMessage("I", "", "無資料可傳送");
                    showLogMessage("I", "", "程式執行結束");
                    finalProcess();
                    return 0;
                }
                getFilename();
                updateColLgd902();
            } else {
                hLgdbFileName = hParFileName;
                getFilename();
                /*--
                  EXEC SQL select count(*) into :il_tot_cnt:di
                           from col_lgd_902
                           where file_name =:h_lgdb_file_name;
                  if (sqlca.sqlcode!=0 && sqlca.sqlcode!=1403)
                     {
                        sprintf(msgerr,"select col_lgd_902 error; file_name=[%s]",h_lgdb_file_name.arr);
                        err_exit(0);
                     }
                  if (il_tot_cnt==0)
                     {
                        ECSprintf(stderr,"no data to create; file_name=[%s]\n"
                                       , h_lgdb_file_name.arr);
                        goto MAIN_9000;               
                     }
                --*/
            }

            /*-create txt-file-*/
            checkOpen();
            
            showLogMessage("I", "", "     col_lgd_902_x資料更新開始");
            updateColGd902X();
            showLogMessage("I", "", "     col_lgd_902_x資料更新 [" + totalCnt + "] 筆");
            
            selectColLgd902();
            closeOutputText(fptr1);
            showLogMessage("I", "", "->檔案產生筆數:[" + ilTotCnt + "]");

            /*--
               ftp_script();
               
               str2var(h_oold_ref_ip              , h_eria_ref_ip.arr); 
               str2var(h_oold_ref_name            , h_eria_ref_name.arr); 
               str2var(h_oold_user_id             , h_eria_user_id.arr); 
               str2var(h_oold_user_passwd         , h_eria_user_passwd.arr); 
               str2var(h_oold_trans_type          , h_eria_trans_type.arr); 
               str2var(h_oold_remote_dir          , h_eria_remote_dir.arr); 
               str2var(h_oold_local_dir           , h_eria_local_dir.arr); 
               str2var(h_oold_port_no             , h_eria_port_no.arr);
            
               ftp_out();
            --*/
            if (hCallBatchSeqno.length() == 20) {
                comcr.hCallErrorDesc = "process is OK";
                comcr.callbatch(1, 0, 0);
            }

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

    private void initFunc() throws Exception {
        hBusiBusinessDate = "";
        hSysDate = "";
        hSysYymm = "";

        /*--business_date的前一天--*/
        selectSQL = "business_date, to_char(sysdate,'yyyymmdd') sys_date, "
                + "to_char(sysdate,'yyyymm') sys_yymm ";
        daoTable = "ptr_businday";
        whereStr = "fetch first 1 row only";

        selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_ptr_businday error!", "", hCallBatchSeqno);
        }

        hBusiBusinessDate = getValue("business_date");
        hSysDate = getValue("sys_date");
        hSysYymm = getValue("sys_yymm");

        showLogMessage("I", "", "business_date=[" + hBusiBusinessDate + "], sys-date=[" + hSysDate + "]");
    }

    // ************************************************************************
    private int checkSendData() throws Exception {
        int llCnt = 0;
        sqlCmd = "select count(*) cnt ";
        sqlCmd += "from col_lgd_902 ";
        sqlCmd += "where decode(send_flag,'','n',send_flag)<>'Y' ";
        sqlCmd += "and   apr_date <> '' ";
        sqlCmd += "fetch first 10 row only ";

        if (selectTable() > 0) {
            llCnt = getValueInt("cnt");
        }

        if (llCnt == 0)
            return 0;

        return 1;
    }

    // ************************************************************************
    private void getFilename() throws Exception {
        /*-017MMDDx.902:[x=9,8,7,6]-*/
        iiFileSeq = 0;

        if (hLgdbFileName.length() == 0) {
            sqlCmd = "select nvl(to_number(substrb(min(file_name),8,1)),10) - 1 as file_seq ";
            sqlCmd += "from col_lgd_batch ";
            sqlCmd += "where pgm_id ='ColC065' and pgm_parm = ? ";
            sqlCmd += "and   ok_flag ='Y' ";
            setString(1, hSysDate);

            if (selectTable() > 0) {
                iiFileSeq = getValueInt("file_seq");
            }
            if (iiFileSeq < 6) {
                comcr.errRtn("檔案名稱編碼(9/8/7/6), 已超過; file-seq=[" + iiFileSeq + "]", "", hCallBatchSeqno);
            }

            sqlCmd = "select ?||substrb(?,5,4)||to_char(cast(? as varchar(2)))||'.902' as file_name ";
            sqlCmd += "from dual ";
            setString(1, CommJcic.JCIC_BANK_NO);
            setString(2, hSysDate);
            setString(3, String.valueOf(iiFileSeq));

            if (selectTable() > 0) {
                hLgdbFileName = getValue("file_name");
            }

        }
        if (debug == 1)
            showLogMessage("I", "", "-JJJ->file-name=[" + hLgdbFileName + "]");

        /*--
           EXEC SQL select 10 - to_number(substrb(:h_lgdb_file_name,8,1))
                    into :ii_file_seq:ss
                    from  dual;
           if (ii_file_seq>4)
           {
              sprintf(msgerr,"檔案傳送數=[%d], 已超過; file-name=[%s]"
                          , ii_file_seq, h_lgdb_file_name.arr);
              err_exit(1);
           }
           --*/
    }

    // ************************************************************************
    private void checkOpen() throws Exception {
//        is_sendfile = comc.GetECSHOME() + "/media/col/" + h_lgdb_file_name;
        isSendfile = String.format("%s/media/col/%s", comc.getECSHOME(), hLgdbFileName);
        isSendfile = Normalizer.normalize(isSendfile, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", "->Data FILE[" + isSendfile + "]");

        fptr1 = openOutputText(isSendfile, "MS950");
        if (fptr1 == -1) {
            comcr.errRtn(String.format("error: [%s]在程式執行目錄下沒有權限讀寫", isSendfile), "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void writeHeader() throws Exception {
        String hLsHeader = "";
//        sqlCmd = "select 'JCIC-DAT-902-V01- 017'";
        sqlCmd = "select ?";
        sqlCmd += "||lpad(' ',5)";
        sqlCmd += "||lpad(to_char(to_number(to_char(sysdate,'yyyy')) - 1911),3,'0')||to_char(sysdate,'mmdd')";
        sqlCmd += "||lpad(?,2,'0')||rpad(' ',10)";
        sqlCmd += "||rpad(nvl(wf_value,' '),18)";
        sqlCmd += "||rpad(nvl(wf_value2,' '),80) as ls_header ";
        sqlCmd += "from ptr_sys_parm ";
        sqlCmd += "where wf_parm ='SYSPARM' and wf_key = ? ";  //ColC065 or COL_C065 ?
        setString(1, JCIC_TYPE.getJcicId() + " " + CommJcic.JCIC_BANK_NO);
        setInt(2, iiFileSeq);
        setString(3, javaProgram);

        if (selectTable() > 0) {
            hLsHeader = getValue("ls_header");
        }

        if (notFound.equals("Y")) {
//            sqlCmd = "select 'JCIC-DAT-902-V01- 017'";
        	sqlCmd = "select ?";
            sqlCmd += "||lpad(' ',5)";
            sqlCmd += "||lpad(to_char(to_number(to_char(sysdate,'yyyy')) - 1911),3,'0')||to_char(sysdate,'mmdd')";
            sqlCmd += "||lpad(?,2,'0')||rpad(' ',10)";
            sqlCmd += "||rpad(' ',18)";
            sqlCmd += "||rpad(' ',80) as ls_header ";
            sqlCmd += "from dual ";
            setString(1, JCIC_TYPE.getJcicId() + " " + CommJcic.JCIC_BANK_NO);
            setInt(2, iiFileSeq);

            selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("set header error", "", hCallBatchSeqno);
            }
            hLsHeader = getValue("ls_header");
        }

        writeTextFile(fptr1, String.format("%s", hLsHeader));
    }

    // ************************************************************************
    private void writeFooter() throws Exception {
        String hLsFooter = "";
//        sqlCmd = "select 'TRLR'";
//        sqlCmd += "||to_char(?,'FM00000000')";
//        sqlCmd += "||rpad(' ',131) as ls_footer ";
//        sqlCmd += "from dual ";
//        setLong(1, ilrowCnt);
        
        //DB2 貌似不支援FM00000000格式?
//        sqlCmd = "select 'TRLR'";
        sqlCmd = "select ?";
        sqlCmd += "||lpad(to_char(cast(? as varchar(8))),8,'0')";
        sqlCmd += "||rpad(' ',131) as ls_footer ";
        sqlCmd += "from dual ";
        setString(1, CommJcic.TAIL_LAST_MARK);
        setString(2, String.valueOf(ilrowCnt));

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("set footer error", "", hCallBatchSeqno);
        }
        hLsFooter = getValue("ls_footer");
        writeTextFile(fptr1, String.format("%s", hLsFooter));
    }

    // ************************************************************************
    private void updateColLgd902() throws Exception {
        hLgd2IdCorpPSeqno = "";
        hLgd2IdCorpNo = "";
        hLgd2LgdSeqno = "";
        hLgd2CloseDate = "";
        hLgd2EarlyYm = "";
        hLgd2FromType = "";
        hLgd2Rowid = "";

        selectSQL = "id_corp_p_seqno, id_corp_no, lgd_seqno, close_date, from_type, rowid as rowid ";
        daoTable = "col_lgd_902";
        whereStr = "where decode(send_flag,'','n',send_flag)<>'Y' and apr_date <> '' ";

        openCursor();
        while (fetchTable()) {
        	hLgd2IdCorpPSeqno = getValue("id_corp_p_seqno");
            hLgd2IdCorpNo = getValue("id_corp_no");
            hLgd2LgdSeqno = getValue("lgd_seqno");
            hLgd2CloseDate = getValue("close_date");
            hLgd2FromType = getValue("from_type");
            hLgd2Rowid = getValue("rowid");

            ilTotCnt++;

            if (debug == 1) {
                if (ilTotCnt % 100 == 0)
                    showLogMessage("I", "", " 讀取筆數 =[" + ilTotCnt + "]");
            }

            if (hLgd2LgdSeqno.length() == 0) {
                sqlCmd = "select lgd_seqno ";
                sqlCmd += "from col_lgd_901 ";
//                sqlCmd += "where id_corp_no = ? ";
                sqlCmd += "where id_corp_p_seqno = ? ";
                sqlCmd += "and   close_date = ? ";
                sqlCmd += "and   lgd_seqno != 'x' ";
                sqlCmd += "fetch first 1 row only ";
//                setString(1, h_lgd2_id_corp_no);
                setString(1, hLgd2IdCorpPSeqno);
                setString(2, hLgd2CloseDate);
                
                extendField = "col_lgd_901.";

                if (selectTable() > 0) {
                    hLgd2LgdSeqno = getValue("col_lgd_901.lgd_seqno");
                }

                if (hLgd2LgdSeqno.length() == 0)
                    continue;
            }

            /*-get early_ym-*/
            if (hLgd2FromType.equals("1") == false) {
                sqlCmd = "select lgd_early_ym ";
                sqlCmd += "from col_lgd_901_log ";
                sqlCmd += "where lgd_seqno = ? ";
                setString(1, hLgd2LgdSeqno);

                extendField = "col_lgd_901_log.";
                
                if (selectTable() > 0) {
                    hLgd2EarlyYm = getValue("col_lgd_901_log.lgd_early_ym");
                }

            }

            dateTime();
            daoTable = "col_lgd_902";
            updateSQL = "lgd_seqno = ?, early_ym = ?, file_name = ?, send_ym = ?, "
                    + "send_date = ?, send_flag = 'Y', mod_time = sysdate, mod_pgm = ? ";
            whereStr = "where rowid = ? ";
            setString(1, hLgd2LgdSeqno);
            setString(2, hLgd2EarlyYm.length() == 0 ? hSysYymm : hLgd2EarlyYm);
            setString(3, hLgdbFileName);
            setString(4, hSysYymm);
            setString(5, hSysDate);
            setString(6, hModPgm);
            setRowId(7, hLgd2Rowid);

            updateTable();

            if (notFound.equals("Y")) {
                comcr.errRtn("update_col_lgd_902 error!", "id_corp=[" + hLgd2IdCorpNo + "]", hCallBatchSeqno);
            }

            /*-update col_lgd_901-*/
            if (hLgd2FromType.equals("1") == false) {
                dateTime();
                daoTable = "col_lgd_901";
                updateSQL = "crt_902_date = ?, mod_time = sysdate, mod_pgm = ? ";
                whereStr = "where lgd_seqno = ? and crt_902_date = '' ";
                setString(1, hSysDate);
                setString(2, hModPgm);
                setString(3, hLgd2LgdSeqno);

                updateTable();
            }
        }
        closeCursor();
    }

    // ************************************************************************
    private void selectColLgd902() throws Exception {
        String allStr = "";
        ilTotCnt = 0;
        hLgdbExecTimeS = "";

        sqlCmd = "select to_char(sysdate,'yyyy/mm/dd hh24:mi:ss') exec_time ";
        sqlCmd += "from dual ";

        if (selectTable() > 0) {
            hLgdbExecTimeS = getValue("exec_time");
        }
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "get sysdate error");
        }

        // Cursor start
        selectSQL = "lgd_seqno, " 
        		+ "id_corp_p_seqno, "
        		+ "id_corp_no, "
                + "nvl(recv_self_amt,0)+nvl(recv_rela_amt,0)+nvl(recv_oth_amt,0) recv_self_amt, " 
                + "rowid as rowid, "
                + "decode(aud_code,'',' ',aud_code) aud_code, " 
                + "decode(lgd_type,'','F',lgd_type) lgd_type, " 
                + "early_ym, "
                + "round(nvl(risk_amt,0) / 1000,0) risk_amt, " 
                + "overdue_ym, "
                + "round(nvl(overdue_amt,0) / 1000,0) overdue_amt, " 
                + "coll_ym, "
                + "round(nvl(coll_amt,0) / 1000,0) coll_amt, " 
                + "crdt_charact, " 
                + "assure_type, " 
                + "crdt_use_type, "
                + "decode(syn_loan_yn,'','N',syn_loan_yn) syn_loan_yn, " 
                + "syn_loan_date, " 
                + "decode(fina_commit_yn,'','N',fina_commit_yn) fina_commit_yn, "
                + "ecic_case_type, " 
                + "nvl(fina_commit_prct,0) fina_commit_prct, "
                + "decode(card_rela_type,'','N',card_rela_type) card_rela_type, " 
                + "round(nvl(recv_trade_amt,0) / 1000,0) recv_trade_amt, "
                + "round(nvl(recv_collat_amt,0) / 1000,0) recv_collat_amt, "
                + "round(nvl(recv_fina_amt,0) / 1000,0) recv_fina_amt, "
                + "round(nvl(recv_self_amt,0) / 1000,0) recv_self_amt, "
                + "round(nvl(recv_rela_amt,0) / 1000,0) recv_rela_amt, "
                + "round(nvl(recv_oth_amt,0) / 1000,0) recv_oth_amt, " 
                + "round(nvl(costs_amt,0) / 1000,0) costs_amt, "
                + "costs_ym, " 
                + "nvl(revol_rate,0) revol_rate, " 
                + "close_reason, " 
                + "collat_yn ";
        daoTable = "col_lgd_902";
        whereStr = "where file_name = ? "
        		+ "  and (close_reason='B2' "
        		+ "  and early_ym >= '201607' "
        		+ "  and substr(sysdate,1,6) >= to_char(add_months(to_date(early_ym,'yyyymm'),24),'yyyymm') "
        		+ "   or close_reason!='B2') ";
        setString(1, hLgdbFileName);

        openCursor();

        ilTotCnt =0;
        writeHeader();

        while (fetchTable()) {
            hLgd2LgdSeqno = "";
            hLgd2IdCorpPSeqno = "";
            hLgd2IdCorpNo = "";
            hLgd2RecvSelfAmt = 0;
            hLgd2Rowid = "";
            hLgd2AudCode = "";
            hLgd2LgdType = "";
            hLgd2EarlyYm = "";
            hLgd2RiskAmt = 0;
            hLgd2OverdueYm = "";
            hLgd2OverdueAmt = 0;
            hLgd2CollYm = "";
            hLgd2CollAmt = 0;
            hLgd2CrdtCharact = "";
            hLgd2AssureType = "";
            hLgd2CrdtUseType = "";
            hLgd2SynLoanYn = "";
            hLgd2SynLoanDate = "";
            hLgd2FinaCommitYn = "";
            hLgd2EcicCaseType = "";
            hLgd2FinaCommitPrct = 0;
            hLgd2CardRelaType = "";
            hLgd2RecvTradeAmt = 0;
            hLgd2RecvCollatAmt = 0;
            hLgd2RecvFinaAmt = 0;
            hLgd2RecvSelfAmt = 0;
            hLgd2RecvRelaAmt = 0;
            hLgd2RecvOthAmt = 0;
            hLgd2CostsAmt = 0;
            hLgd2CostsYm = "";
            hLgd2RevolRate = 0;
            hLgd2CloseReason = "";
            hLgd2CollatYn = "";

            hLgd2LgdSeqno = getValue("lgd_seqno");
            hLgd2IdCorpPSeqno = getValue("id_corp_p_seqno");
            hLgd2IdCorpNo = getValue("id_corp_no");
            hLgd2RecvSelfAmt = getValueDouble("recv_self_amt");
            hLgd2Rowid = getValue("rowid");
            hLgd2AudCode = getValue("aud_code");
            hLgd2LgdType = getValue("lgd_type");
            hLgd2EarlyYm = getValue("early_ym");
            hLgd2RiskAmt = getValueDouble("risk_amt");
            hLgd2OverdueYm = getValue("overdue_ym");
            hLgd2OverdueAmt = getValueDouble("overdue_amt");
            hLgd2CollYm = getValue("coll_ym");
            hLgd2CollAmt = getValueDouble("coll_amt");
            hLgd2CrdtCharact = getValue("crdt_charact");
            hLgd2AssureType = getValue("assure_type");
            hLgd2CrdtUseType = getValue("crdt_use_type");
            hLgd2SynLoanYn = getValue("syn_loan_yn");
            hLgd2SynLoanDate = getValue("syn_loan_date");
            hLgd2FinaCommitYn = getValue("fina_commit_yn");
            hLgd2EcicCaseType = getValue("ecic_case_type");
            hLgd2FinaCommitPrct = getValueDouble("fina_commit_prct");
            hLgd2CardRelaType = getValue("card_rela_type");
            hLgd2RecvTradeAmt = getValueDouble("recv_trade_amt");
            hLgd2RecvCollatAmt = getValueDouble("recv_collat_amt");
            hLgd2RecvFinaAmt = getValueDouble("recv_fina_amt");
            hLgd2RecvSelfAmt = getValueDouble("recv_self_amt");
            hLgd2RecvRelaAmt = getValueDouble("recv_rela_amt");
            hLgd2RecvOthAmt = getValueDouble("recv_oth_amt");
            hLgd2CostsAmt = getValueDouble("costs_amt");
            hLgd2CostsYm = getValue("costs_ym");
            hLgd2RevolRate = getValueDouble("revol_rate");
            hLgd2CloseReason = getValue("close_reason");
            hLgd2CollatYn = getValue("collat_yn");

            ilTotCnt++;

            if (debug == 1) {
                if (ilTotCnt % 100 == 0)
                    showLogMessage("I", "", " 讀取筆數 =[" + ilTotCnt + "]");
            }

            /*-<0, set=0-*/
            if (hLgd2RiskAmt < 0) {
                hLgd2RiskAmt = 0;
            }
            if (hLgd2OverdueAmt < 0) {
                hLgd2OverdueAmt = 0;
            }
            if (hLgd2CollAmt < 0) {
                hLgd2CollAmt = 0;
            }
            if (hLgd2FinaCommitPrct < 0) {
                hLgd2FinaCommitPrct = 0;
            }
            if (hLgd2RecvTradeAmt < 0) {
                hLgd2RecvTradeAmt = 0;
            }
            if (hLgd2RecvCollatAmt < 0) {
                hLgd2RecvCollatAmt = 0;
            }
            if (hLgd2RecvFinaAmt < 0) {
                hLgd2RecvFinaAmt = 0;
            }
            if (hLgd2RecvSelfAmt < 0) {
                hLgd2RecvSelfAmt = 0;
            }
            if (hLgd2RecvRelaAmt < 0) {
                hLgd2RecvRelaAmt = 0;
            }
            if (hLgd2RecvOthAmt < 0) {
                hLgd2RecvOthAmt = 0;
            }
            if (hLgd2CostsAmt < 0) {
                hLgd2CostsAmt = 0;
            }
            if (hLgd2RevolRate < 0) {
                hLgd2RevolRate = 0;
            }

            allStr = CommJcic.JCIC_BANK_NO;
            allStr += String.format("%1$1s", hLgd2AudCode);
            allStr += String.format("%-10.10s", hLgd2IdCorpNo);
            allStr += String.format("%-50.50s", hLgd2LgdSeqno);
            allStr += String.format("%-6.6s", hSysYymm);
            allStr += String.format("%1$1s", hLgd2LgdType);
            allStr += String.format("%-6.6s", hLgd2EarlyYm);
            allStr += String.format("%010.0f", hLgd2RiskAmt);
            allStr += String.format("%-6.6s", hLgd2OverdueYm);
            allStr += String.format("%010.0f", hLgd2OverdueAmt);
            allStr += String.format("%-6.6s", hLgd2CollYm);
            allStr += String.format("%010.0f", hLgd2CollAmt);
            allStr += String.format("%1.1s", hLgd2CrdtCharact);
            allStr += String.format("%1.1s", hLgd2AssureType);
            allStr += String.format("%1.1s", hLgd2CrdtUseType);
            allStr += String.format("%1.1s", hLgd2SynLoanYn);
            allStr += String.format("%-8.8s", hLgd2SynLoanDate);
            allStr += String.format("%1.1s", hLgd2FinaCommitYn);
            allStr += String.format("%1.1s", hLgd2EcicCaseType);
            allStr += String.format("%04.3f", hLgd2FinaCommitPrct);
            allStr += String.format("%1.1s", hLgd2CardRelaType);
            allStr += String.format("%010.0f", hLgd2RecvTradeAmt);
            allStr += String.format("%010.0f", hLgd2RecvCollatAmt);
            allStr += String.format("%010.0f", hLgd2RecvFinaAmt);
            allStr += String.format("%010.0f", hLgd2RecvSelfAmt);
            allStr += String.format("%010.0f", hLgd2RecvRelaAmt);
            allStr += String.format("%010.0f", hLgd2RecvOthAmt);
            allStr += String.format("%010.0f", hLgd2CostsAmt);
            allStr += String.format("%-6.6s", hLgd2CostsYm);
            allStr += String.format("%08.5f", hLgd2RevolRate);
            allStr += String.format("%-2.2s", hLgd2CloseReason);
            allStr += String.format("%1.1s", hLgd2CollatYn);

            writeTextFile(fptr1, String.format("%s", allStr));
            updateColLgdJrnl();
        }
        closeCursor();
        writeFooter();
        insertColLgdBatch();
    }

    // ************************************************************************
    private void updateColLgdJrnl() throws Exception {
        if (hLgd2RecvSelfAmt == 0) {
            return;
        }

        dateTime();
        daoTable = "col_lgd_jrnl";
        updateSQL = "lgd_seqno = ?, send_date = ?, mod_time = sysdate, mod_pgm = ? ";
//        whereStr = "where id_corp_no = ? and   close_date = ? and   apr_date <> '' ";
        whereStr = "where id_corp_p_seqno = ? and close_date = ? and apr_date <> '' ";
        setString(1, hLgd2LgdSeqno);
        setString(2, hSysDate);
        setString(3, javaProgram);
//        setString(4, h_lgd2_id_corp_no);
        setString(4, hLgd2IdCorpPSeqno);
        setString(5, hLgd2CloseDate);

        updateTable();
    }

    // ************************************************************************
    private void insertColLgdBatch() throws Exception {
        dateTime();
        daoTable = "col_lgd_batch";
        extendField = daoTable + ".";
        setValue(extendField+"pgm_id", javaProgram);
        setValue(extendField+"pgm_parm", hSysDate);
        setValue(extendField+"exec_time_s", hLgdbExecTimeS.length() == 0 ? null : hLgdbExecTimeS);
        setValue(extendField+"exec_time_e", sysDate + sysTime);
        setValueLong(extendField+"tot_cnt", ilTotCnt);
        setValue(extendField+"ok_flag", "Y");
        setValue(extendField+"run2_flag", "N");
        setValue(extendField+"file_name", hLgdbFileName);

        insertTable();
    }

    /***********************************************************************/
    /* 0.OK, 1.Error */
    /***********************************************************************/
    // ************************************************************************
    private int selectEcsRefIpAddr(String refIp) throws Exception {
        String allStr = "";

        /*-FTP ref_ID-*/
 
        hEriaEcsIp = allStr;
        hEriaRefIp = "";
        hEriaRefName = "";
        hEriaUserId = "";
        hEriaUserPasswd = "";
        hEriaTransType = "";
        hEriaRemoteDir = "";
        hEriaLocalDir = "";
        hEriaPortNo = "";

        sqlCmd = "select ref_ip, ";
        sqlCmd += "ref_name, ";
        sqlCmd += "user_id, ";
        sqlCmd += "user_passwd, ";
        sqlCmd += "trans_type, ";
        sqlCmd += "remote_dir, ";
        sqlCmd += "local_dir, ";
        sqlCmd += "port_no ";
        sqlCmd += "from ecs_ref_ip_addr ";
        sqlCmd += "where ecs_ip = ? ";
        sqlCmd += "and   ref_ip_code = ? ";
        setString(1, hEriaEcsIp);
        setString(2, refIp);

        selectTable();
        if (notFound.equals("Y")) {
            return 1;
        }
        hEriaRefIp = getValue("ref_ip");
        hEriaRefName = getValue("ref_name");
        hEriaUserId = getValue("user_id");
        hEriaUserPasswd = getValue("user_passwd");
        hEriaTransType = getValue("trans_type");
        hEriaRemoteDir = getValue("remote_dir");
        hEriaLocalDir = getValue("local_dir");
        hEriaPortNo = getValue("port_no");
        
        return 0;
    }

    // ************************************************************************
    private void ftpScript() throws Exception {
        int liRC = 0;
        String lsWfValue2 = "";

        /*-ZIP-*/
        lsWfValue2 = "";
        sqlCmd = "select wf_value2 ";
        sqlCmd += "from ptr_sys_parm ";
        sqlCmd += "where wf_parm = 'SYSPARM' ";
        sqlCmd += "and   wf_key  = 'ROADCAR_ZIP_PWD' ";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_sys_parm error[ROADCAR_ZIP_PWD]", "", hCallBatchSeqno);
        }
        lsWfValue2 = getValue("wf_value2");

        int tmpInt = comm.zipFile(isSendfile, isZipfile, lsWfValue2);
        if (tmpInt != 0) {
            comcr.errRtn(String.format("無法壓縮檔案[%s]", isSendfile), "", hCallBatchSeqno);
        }
        
        comc.chmod777(isZipfile);

        liRC = selectEcsRefIpAddr("ROAD_MEGAFTPSERVER");
        if (liRC != 0)
            return;
        
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = javaProgram; /* 區分不同類的 FTP 檔案-大類 (必要) */
        // commFTP.h_eflg_group_id =h_efl1_group_id; /* 區分不同類的 FTP 檔案-次分類 (非必要)*/
        commFTP.hEflgSourceFrom = "0000"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = this.getClass().getName();
        commFTP.hEriaLocalDir = "";
        String hEflgRefIpCode = "ROAD_MEGAFTPSERVER";
        //System.setProperty("user.dir", h_eria_local_dir);
        
        /** put 不能寫全路徑 , 需在 參數檔設路徑 **/
        String procCode = String.format("put %s.zip", isFilename);

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        
        //phopho add 2019.5.27 無檔案的return message須自己判斷
        if (errCode == 0 && commFTP.fileList.size() == 0) {
        	showLogMessage("I", "", String.format("[%-20.20s] => [無資料可傳送]", procCode));
        }
        
        if (errCode != 0) {
            showLogMessage("I", "", String.format("FTP 1=[%s]傳檔錯誤 err_code[%d]", procCode, errCode));
            return;
        }

    }

    // ************************************************************************
    private void ftpOut() throws Exception {
        int liRC = 0;
        int rtn = 0;
        String lsFilename = "";
        String tmpstr = "";

        liRC = selectEcsRefIpAddr("ROAD_FTPCOMMON");
        if (liRC != 0)
            return;

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = javaProgram; /* 區分不同類的 FTP 檔案-大類 (必要) */
        // commFTP.h_eflg_group_id =h_efl1_group_id; /* 區分不同類的 FTP 檔案-次分類 (非必要)*/
        commFTP.hEflgSourceFrom = "0000"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = this.getClass().getName();
        commFTP.hEriaLocalDir = "";
//        if (h_eria_trans_type.compareTo("A") == 0) {
//            commFTP.h_eria_trans_type = "A"; //ASCII
//        } else {
//            commFTP.h_eria_trans_type = "B"; //BINARY
//        }
        String hEflgRefIpCode = "ROAD_FTPCOMMON";
        //System.setProperty("user.dir", h_eria_local_dir);
        
        /** put 不能寫全路徑 , 需在 參數檔設路徑 **/
        String procCode = String.format("put %s.zip", isFilename);

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        
        //phopho add 2019.5.27 無檔案的return message須自己判斷
        if (errCode == 0 && commFTP.fileList.size() == 0) {
        	showLogMessage("I", "", String.format("[%-20.20s] => [無資料可傳送]", procCode));
        }
        
        if (errCode != 0) {
            showLogMessage("I", "", String.format("FTP 1=[%s]傳檔錯誤 err_code[%d]", procCode, errCode));
            return;
        }
    }

    // ************************************************************************
    private void updateColGd902X() throws Exception {
    	/*sunny add，先清掉已經產生在col_lgd_902不符合產生名單的資料*/
    	daoTable = "col_lgd_902";
        updateSQL = "send_flag = 'Y' ";
        whereStr = "where rowid not in ( "
        		+ "  select rowid from col_lgd_902 where decode(send_flag,'','n',send_flag) <> 'Y' and apr_date <> '' "
        		+ "   and (close_reason='B2' and early_ym >= '201607' "
        		+ "   and substr(sysdate,1,6) >= to_char(add_months(to_date(early_ym,'yyyymm'),24),'yyyymm') "
        		+ "    or close_reason!='B2')) "
        		+ "and decode(send_flag,'','n',send_flag) <> 'Y' and apr_date <> '' ";
        totalCnt = updateTable();
    }
    
}