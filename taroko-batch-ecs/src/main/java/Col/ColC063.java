/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 106/08/10  V1.01.01    phopho     Initial                                  *
* 108/12/02  V1.01.02    phopho     fix err_rtn bug                          *
* 109/12/15  V1.00.03    shiyuqi       updated for project coding standard   *
* 109/12/30  V1.00.04    yanghan       修改了部分无意义的變量名稱            *
* 110/04/06  V1.00.05    Justin     use common value                         *
*****************************************************************************/
package Col;

import java.text.Normalizer;

import com.*;

import hdata.jcic.JcicEnum;

public class ColC063 extends AccessDAO {
    private String progname = "LGD 表一媒體產生處理 110/04/06  V1.00.05 ";
    private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_901;
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommRoutine    comr     = null;

    int debug = 0;
    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";

    String hLgd1IdCorpNo = "";
    String hLgd1IdCorpPSeqno = "";
    String hLgd1IdCorpType = "";
    String hLgd1LgdSeqno = "";
    String hLgd1LgdType = "";
    String hLgd1LgdReason = "";
    String hLgd1LgdEarlyYm = "";
    String hLgd1CloseFlag = "";
    String hLgd1CloseDate = "";
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

    long ilTotCnt = 0;
    long totalCnt = 0;

    /*-program variable-*/
    String hSysDate = "";
    String hSysYymm = "";
    String hModUser = "";
    String hModPgm = "";
    String hParFileName = "";

    /*- temp variable-*/
    String szText = "";
    String cmdStr = "";
    String isSendfile = "";
    String isZipfile = "";
    String isFilename = "";
    long ilrowCnt = 0;
    int fileSeq = 0;
    int iiLgdSeqno = 0;

    private int fptr1 = 0;
    
    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColC063 proc = new ColC063();
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
            if (args.length > 1) {
                comc.errExit("Usage : ColC063 [file_name(12)/callbatch_seqno(20)]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            initFunc();
            hModPgm = javaProgram;
            hParFileName = "";
            if (args.length == 1) {
                hCallBatchSeqno = args[0];
                if (hCallBatchSeqno.length() == 20) {

                    comcr.hCallBatchSeqno = hCallBatchSeqno;
                    comcr.hCallRProgramCode = javaProgram;
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
            if (hParFileName.length() > 0) {
                hLgdbFileName = hParFileName;
                getFilename();
            } else {
                if (checkSendData() == 0) {
                    showLogMessage("I", "", "-OK-->無資料可傳送");
                    showLogMessage("I", "", "程式執行結束");
                    finalProcess();
                    return 0;
                }

                /*-move data to 901-LOG-*/
                getFilename();
                
                showLogMessage("I", "", "     col_lgd_901_x資料更新開始");
                updateColLgd901x();
                showLogMessage("I", "", "     col_lgd_901_x資料更新 [" + totalCnt + "] 筆");
                
                selectColLgd901();
            }

            /*-create txt-file-*/
            checkOpen();
            selectColLgd901log();
            closeOutputText(fptr1);
            
            showLogMessage("I", "", "->資料處理筆數:[" + ilTotCnt + "], 檔案產生筆數:[" + ilrowCnt + "]");

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
        sqlCmd += "from col_lgd_901 ";
        sqlCmd += "where lgd_seqno = '' ";
        /* and ((data_table='acno' and payment_rate='03') or (data_table !='acno'))*/ /*sunny*/
        /*and crt_date>to_char(add_months(to_date(:h_sys_date,'yyyymmdd'),-3),'yyyymmdd')*//*sunny*/
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
        fileSeq = 0;

        if (hLgdbFileName.length() == 0) {
            sqlCmd = "select nvl(to_number(substrb(min(file_name),8,1)),10) - 1 as file_seq ";
            sqlCmd += "from col_lgd_batch ";
            sqlCmd += "where pgm_id = ? and pgm_parm = ? ";
            sqlCmd += "and   ok_flag ='Y' ";
            setString(1, javaProgram);
            setString(2, hSysDate);

            if (selectTable() > 0) {
                fileSeq = getValueInt("file_seq");
            }
            if (fileSeq < 6) {
                comcr.errRtn("檔案名稱編碼(9/8/7/6), 已超過; file-seq=[" + fileSeq + "]", "", hCallBatchSeqno);
            }

            sqlCmd = "select ? ||substrb(?,5,4)||to_char(cast(? as varchar(2)))||'.901' as file_name ";
            sqlCmd += "from dual ";
            setString(1, CommJcic.JCIC_BANK_NO);
            setString(2, hSysDate);
//            setInt(2, fileSeq);
            setString(3, String.valueOf(fileSeq));

            if (selectTable() > 0) {
                hLgdbFileName = getValue("file_name");
            }

        }
        if (debug == 1)
            showLogMessage("I", "", "-JJJ->file-name=[" + hLgdbFileName + "]");
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
    private void selectColLgd901log() throws Exception {
        String hLsData = "";
        selectSQL = "?||'A'||rpad(id_corp_no,10)||to_char(sysdate,'yyyymm')||rpad(lgd_type,1)"
                + "||rpad(substrb(lgd_reason,1,1),1)||rpad(lgd_early_ym,6)||'NNN' as ls_data";
        daoTable = "col_lgd_901_log";
        whereStr = "where file_name = ? and   crt_date like to_char(sysdate,'yyyy')||'%' ";
        setString(1, CommJcic.JCIC_BANK_NO);
        setString(2, hLgdbFileName);

        openCursor();

        writeHeader();

        while (fetchTable()) {
            hLsData = getValue("ls_data");

            ilrowCnt++;

            if (debug == 1) {
                if (ilrowCnt % 100 == 0)
                    showLogMessage("I", "", " 讀取筆數 =[" + ilrowCnt + "]");
            }

            writeTextFile(fptr1, String.format("%s",hLsData));
        }
        closeCursor();

        writeFooter();
    }

    // ************************************************************************
    private void writeHeader() throws Exception {
        String hLsHeader = "";
//        sqlCmd = "select 'JCIC-DAT-901-V01- 017'";
        sqlCmd = "select ?";
        sqlCmd += "||lpad(' ',5)";
        sqlCmd += "||lpad(to_char(to_number(to_char(sysdate,'yyyy')) - 1911),3,'0')||to_char(sysdate,'mmdd')";
        sqlCmd += "||lpad(?,2,'0')||rpad(' ',10)";
        sqlCmd += "||rpad(nvl(wf_value,' '),18)";
        sqlCmd += "||rpad(nvl(wf_value2,' '),80) as ls_header ";
        sqlCmd += "from ptr_sys_parm ";
        sqlCmd += "where wf_parm ='SYSPARM' and wf_key = ? ";  //ColC063 or COL_C063 ?
        setString(1, JCIC_TYPE.getJcicId() + " " + CommJcic.JCIC_BANK_NO);
        setInt(2, fileSeq);
        setString(3, javaProgram);

        if (selectTable() > 0) {
            hLsHeader = getValue("ls_header");
        }

        if (notFound.equals("Y")) {
//            sqlCmd = "select 'JCIC-DAT-901-V01- 017'";
        	sqlCmd = "select ?";
            sqlCmd += "||lpad(' ',5)";
            sqlCmd += "||lpad(to_char(to_number(to_char(sysdate,'yyyy')) - 1911),3,'0')||to_char(sysdate,'mmdd')";
            sqlCmd += "||lpad(?,2,'0')||rpad(' ',10)";
            sqlCmd += "||rpad(' ',18)";
            sqlCmd += "||rpad(' ',80) as ls_header ";
            sqlCmd += "from dual ";
            setString(1, JCIC_TYPE.getJcicId() + " " + CommJcic.JCIC_BANK_NO);
            setInt(2, fileSeq);

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
        sqlCmd = String.format("select '%s'", CommJcic.TAIL_LAST_MARK);
        sqlCmd += "||lpad(to_char(cast(? as varchar(8))),8,'0')";
        sqlCmd += "||rpad(' ',131) as ls_footer ";
        sqlCmd += "from dual ";
        setString(1, String.valueOf(ilrowCnt));
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("set footer error", "", hCallBatchSeqno);
        }
        hLsFooter = getValue("ls_footer");        

        writeTextFile(fptr1, String.format("%s", hLsFooter));
    }

    // ************************************************************************
    private void selectColLgd901() throws Exception {
        String lsSeqno = "";
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

        sqlCmd = "select max(substr(lgd_seqno,7,6)) lgd_seqno ";
        sqlCmd += "from col_lgd_901 ";
        sqlCmd += "where lgd_seqno like to_char(sysdate,'yyyymm')||'%' ";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("can not get LGD_SEQNO 流水號", "", hCallBatchSeqno);
        }
        lsSeqno = getValue("lgd_seqno");
        
        iiLgdSeqno = comcr.str2int(lsSeqno);

        // Cursor start
//        selectSQL = "id_corp_no, min(lgd_early_ym) lgd_early_ym ";
//        daoTable = "col_lgd_901";
//        whereStr = "where lgd_seqno = '' and apr_date <> '' group by id_corp_no ";
        /*   and ((data_table='acno' and payment_rate='03') or (data_table !='acno'))*/ /*sunny*/
        /*  and crt_date>to_char(add_months(to_date(:h_sys_date,'yyyymmdd'),-3),'yyyymmdd')*//*sunny*/

        selectSQL = "id_corp_p_seqno, min(lgd_early_ym) lgd_early_ym ";
        daoTable = "col_lgd_901";
        whereStr = "where lgd_seqno = '' and apr_date <> '' group by id_corp_p_seqno ";
        
        openCursor();
        while (fetchTable()) {
            hLgd1IdCorpNo = "";
            hLgd1IdCorpPSeqno = "";
            hLgd1LgdEarlyYm = "";

//            h_lgd1_id_corp_no = getValue("id_corp_no");
            hLgd1IdCorpPSeqno = getValue("id_corp_p_seqno");
            hLgd1LgdEarlyYm = getValue("lgd_early_ym");

            /*-early_ym<sysdate[YM]-*/
            if (comc.getSubString(hLgd1LgdEarlyYm,0,6).compareTo(hSysYymm) >= 0) {
                continue;
            }

            ilTotCnt++;

            if (debug == 1) {
                if (ilTotCnt % 100 == 0)
                    showLogMessage("I", "", " 讀取筆數 =[" + ilTotCnt + "]");
            }

            selectColLgd9011();
            insertColLgd901Log();
            updateColLgd901();
        }
        closeCursor();
        insertColLgdBatch();
    }

    // ************************************************************************
    private void selectColLgd9011() throws Exception {
        hLgd1LgdSeqno = "";
        hLgd1IdCorpType = "";
        hLgd1LgdType = "";
        hLgd1LgdReason = "";
        hLgd1CloseFlag = "";
        hLgd1CloseDate = "";

        sqlCmd = "select id_corp_type, ";
        sqlCmd += "lgd_type, lgd_reason, ";
        sqlCmd += "close_flag, close_date ";
        sqlCmd += "from col_lgd_901 ";
//        sqlCmd += "where id_corp_no = ? ";
        sqlCmd += "where id_corp_p_seqno = ? ";
        sqlCmd += "and   decode(lgd_early_ym,'','x',lgd_early_ym) =? ";
        /* and ((data_table='acno' and payment_rate='03') or (data_table !='acno')) */      /*sunny add*/
        /* and crt_date>to_char(add_months(to_date(:h_sys_date,'yyyymmdd'),-3),'yyyymmdd')*/
        sqlCmd += "and   lgd_seqno = '' ";
        sqlCmd += "and   apr_date <> '' ";
        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_lgd1_id_corp_no);
        setString(1, hLgd1IdCorpPSeqno);
        setString(2, hLgd1LgdEarlyYm.length() == 0 ? "x" : hLgd1LgdEarlyYm);
        
        extendField = "col_lgd_901_1.";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_lgd_901 error; id_code=[" + hLgd1IdCorpNo + "]", "", hCallBatchSeqno);
        }
        hLgd1IdCorpType = getValue("col_lgd_901_1.id_corp_type");
        hLgd1LgdType = getValue("col_lgd_901_1.lgd_type");
        hLgd1LgdReason = getValue("col_lgd_901_1.lgd_reason");
        hLgd1CloseFlag = getValue("col_lgd_901_1.close_flag");
        hLgd1CloseDate = getValue("col_lgd_901_1.close_date");
    }

    // ************************************************************************
    private void insertColLgd901Log() throws Exception {
        setLgdSeqno();

        dateTime();
        daoTable = "col_lgd_901_log";
        extendField = daoTable + ".";
        setValue(extendField+"crt_date", hSysDate);
        setValue(extendField+"lgd_seqno", hLgd1LgdSeqno);
        setValue(extendField+"id_corp_p_seqno", hLgd1IdCorpPSeqno);
        setValue(extendField+"id_corp_no", hLgd1IdCorpNo);
        setValue(extendField+"id_corp_type", hLgd1IdCorpType);
        setValue(extendField+"lgd_type", hLgd1LgdType);
        setValue(extendField+"lgd_reason", hLgd1LgdReason);
        setValue(extendField+"lgd_early_ym", hLgd1LgdEarlyYm.equals("") ? hSysYymm : hLgd1LgdEarlyYm);
        setValue(extendField+"close_flag", hLgd1CloseFlag);
        setValue(extendField+"close_date", hLgd1CloseDate);
        setValue(extendField+"file_name", hLgdbFileName);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_lgd_901_log error[dupRecord]", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void setLgdSeqno() throws Exception {
        int ii = 0, liCnt = 0;

        if (hLgd1LgdSeqno.length() != 0)
            return;

        while (ii < 10) {
        	ii++;
            iiLgdSeqno++;
            
            sqlCmd = "select to_char(sysdate,'yyyymm')||lpad(to_char(cast(? as varchar(6))),6,'0') as lgd_seqno ";
            sqlCmd += "from dual ";
            setString(1, String.valueOf(iiLgdSeqno));
            
            extendField = "lgd_seqno_1.";

            selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("can not get LGD_SEQNO=[" + iiLgdSeqno + "]", "", hCallBatchSeqno);
            }
            hLgd1LgdSeqno = getValue("lgd_seqno_1.lgd_seqno");

            sqlCmd = "select count(*) as cnt ";
            sqlCmd += "from col_lgd_901 ";
            sqlCmd += "where lgd_seqno = ? ";
            setString(1, hLgd1LgdSeqno);

            extendField = "lgd_seqno_2.";
            
            selectTable();
            if (notFound.equals("Y")) {
                showLogMessage("I", "", "can not get LGD_SEQNO=[" + iiLgdSeqno + "]");
                continue;
            }
            liCnt = getValueInt("lgd_seqno_2.cnt");
            if (liCnt > 0) {
                hLgd1LgdSeqno = "";
                continue;
            }
            break;
        }
        if (hLgd1LgdSeqno.length() != 0)
            return;

        /*-reset ii_lgd_seqno-*/
        iiLgdSeqno = 0;
        sqlCmd = "select nvl(to_number(substr(max(lgd_seqno),7,6)),0) as ii_seqno ";
        sqlCmd += "from col_lgd_901 ";
        sqlCmd += "where lgd_seqno like to_char(sysdate,'yyyymm')||'%' ";

        extendField = "lgd_seqno_3.";
        
        if (selectTable() > 0) {
            iiLgdSeqno = getValueInt("lgd_seqno_3.ii_seqno");
        }

        iiLgdSeqno++;
        sqlCmd = "select to_char(sysdate,'yyyymm')||lpad(to_char(cast(? as varchar(6))),6,'0') as lgd_seqno ";
        sqlCmd += "from dual ";
//        setInt(1, ii_lgd_seqno);
        setString(1, String.valueOf(iiLgdSeqno));

        extendField = "lgd_seqno_4.";
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("can not get LGD_SEQNO=[" + iiLgdSeqno + "]", "", hCallBatchSeqno);
        }
        hLgd1LgdSeqno = getValue("lgd_seqno_4.lgd_seqno");
    }

    // ************************************************************************
    private void updateColLgd901() throws Exception {
        dateTime();
        daoTable = "col_lgd_901";
        updateSQL = "lgd_seqno = ?, crt_901_date = to_char(sysdate,'yyyymmdd'), mod_user = ?, "
                + "mod_time = sysdate, mod_pgm = ?, mod_seqno = nvl(mod_seqno,0)+1 ";
//        whereStr = "where id_corp_no = ? and apr_date <> '' ";
        whereStr = "where id_corp_p_seqno = ? and apr_date <> '' ";
        setString(1, hLgd1LgdSeqno);
        setString(2, hModUser);
        setString(3, hModPgm);
//        setString(4, h_lgd1_id_corp_no);
        setString(4, hLgd1IdCorpPSeqno);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_lgd_901 error!", "id_corp=[" + hLgd1IdCorpNo + "]", hCallBatchSeqno);
        }
    }
    
    // ************************************************************************
    private void updateColLgd901x() throws Exception {
    	/*sunny add，先清掉已經產生在col_lgd_901不符合產生名單的資料*/
    	daoTable = "col_lgd_901";
        updateSQL = "lgd_seqno = 'x', close_flag = 'Y' ";
        whereStr = "where rowid not in ( "
        		+ "  select rowid from col_lgd_901 where lgd_seqno  = '' and apr_date <> '' "
        		+ "   and ((data_table='acno' and payment_rate='03') or (data_table !='acno'))) "
        		+ "and lgd_seqno = '' and apr_date <> '' ";
        totalCnt = updateTable();

//        if (notFound.equals("Y")) {
//            comcr.err_rtn("update_col_lgd_901_x error!", "", h_call_batch_seqno);
//        }
    }
    
    // ************************************************************************
    private void insertColLgdBatch() throws Exception {
    	daoTable = "col_lgd_batch";
    	extendField = daoTable + ".";
        setValue(extendField+"pgm_id", javaProgram);
        setValue(extendField+"pgm_parm", hSysDate);
        setValue(extendField+"exec_time_s", hLgdbExecTimeS);
        setValue(extendField+"exec_time_e", sysDate + sysTime);
        setValueLong(extendField+"tot_cnt", ilTotCnt);
        setValue(extendField+"ok_flag", "Y");
        setValue(extendField+"run2_flag", "N");
        setValue(extendField+"file_name", hLgdbFileName);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_lgd_batch error[dupRecord]", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    /* 0.OK, 1.Error */
    /***********************************************************************/
    // ************************************************************************
    private int selectEcsRefIpAddr(String refIp) throws Exception {
        String nullString = "";

        /*-FTP ref_ID-*/

        hEriaEcsIp = nullString;
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
}