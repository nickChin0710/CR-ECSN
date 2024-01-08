/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/03/06  V1.00.01  Simon       program initial derive from ActB002       *
 *                                   only process post office                  *
 *  112/05/23  V1.00.02  Simon       clone from R6 dir:/crdataupload/          *
 *  112/06/05  V1.00.03  Simon       copy from R6 dir:/crdataupload/ & delete it*
 *  112/06/24  V1.00.04  mHung3      簡訊修改                                  *
 *  112/07/12  V1.00.05  Simon       1.郵局代碼 "700" 及 "7000000" 調整        *
 *                                   2.簡訊修改                                *
 *  112/07/13  V1.00.06  Simon       act_b002r1.autopay_acct_no controlled to 16 Bytes*
 *  112/10/24  V1.00.07  Simon       批次自動覆核                              *
 ******************************************************************************/

package Act;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*它行自動扣繳媒體回饋處理*/
public class ActB102 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private final String PROGNAME = "郵局自動扣繳媒體回饋處理  112/10/24  V1.00.07";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActB102";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRProgramCode = "";
    String buf = "";
    String szTmp = "";

    String hBusiBusinessDate = "";
    String hAoayAcctType = "";
    String hAoayAcctKey = "";
    String hAoayFromMark = "";
    String hAoayStmtCycle = "";
    double hAoayTransactionAmt = 0;
    String hAoayAutopayId = "";
    String orignStatusCode = "";
    String hAoayIdPSeqno = "";
    String hAoayId = "";
    String hAoayIdCode = "";
    String hAoayChiName = "";
    String hAoayRowid = "";
    String hPtclAcctBank = "";
    String hAoayEnterAcctDate = "";
    String hAoayPSeqno = "";
    String hAoayStatusCode = "";
    String hApbtBatchNo = "";
    String hAoayAutopayAcctNo = "";
    double hApdlPayAmt = 0;
    String hAoayAutopayIdCode = "";
    String hB021ErrType = "";
    String hIdnoCellarPhone = "";
    String hApdlSerialNo = "";
    String hApdlPayDate = "";
    String hAperErrorReason = "";
    String hAperErrorRemark = "";
    String hApbtModUser = "";
    String hApbtModPgm = "";
    long hTempBatchNoSeq = 0;
    String hTempBatchNo = "";
    int hApbtBatchTotCnt = 0;
    double hApbtBatchTotAmt = 0;
    String hApbtModSeqno = "";
    String hWdayThisLastpayDate = "";
    String hPtclExternalName = "";
    String hPtclAcctMonth = "";
    String hPtclSStmtCycle = "";
    String hPtclEStmtCycle = "";
    String hPtclValueDate = "";
    String hPtclRowid = "";
    String hPtclMediaName = "";
    String hPtclBusinessDate = "";
    String wsInFileFlag = "";
    long rightNo = 0;
    long errorNoR1 = 0;
    String hOutMediaName = "";
    String hOutBusinessDate = "";
    String hIdnoChiName = "";
    String hAchBankNo = "";
    String hAchBcAbname = "";
    String hIdnoBirthday = "";
    String hIdnoHomeAreaCode1 = "";
    String hIdnoHomeTelNo1 = "";
    String hIdnoHomeTelExt1 = "";
    String hSmidMsgId = "";
    String hSmidMsgDept = "";
    String hSmidMsgSendFlag = "";
    String hSmidMsgSelAcctType = "";
    String hSmidMsgUserid = "";
    String hSmdlCellphoneCheckFlag = "";
    String hSmdlVoiceFlag = "";
    String hWdayThisCloseDate = "";
    String hTarokoRemoteDir = "";
    int hPtclSeqNo = 0;
    int hOutSeqNo = 0;

    String fileName = "";
    String temstr0 = "";
    String temstr1 = "";
    String str600 = "";
    int nSerialNo = 0;
    int errorNo = 0;
  //String flowControl = "";
    String wsNotFound = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hBusiBusinessDate = "";
            if (args.length > 0 && args[0].length() == 8)
                hBusiBusinessDate = args[0];

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            showLogMessage("I", "", String.format("程式開始執行"));

            hModUser = comc.commGetUserID();
            hApbtModUser = hModUser;
            hApbtModPgm = javaProgram;

            selectPtrBusinday();

            hPtclAcctBank = "700"; 
            /*-- read 簡訊參數檔 --*/ 
            selectSmsMsgId();

            sqlCmd = "select bank_no,bank_name ";
            sqlCmd += " from act_ach_bank  ";
            sqlCmd += "where bank_no like ? || '%'  ";
            sqlCmd += "fetch first 1 rows only ";
            setString(1, hPtclAcctBank);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hAchBankNo = getValue("bank_no");
                hAchBcAbname = getValue("bank_name");
            }

            checkFopen();

            readFile();

          //String fs = String.format("%s/media/act/%s", comc.getECSHOME(), h_ptcl_external_name);
            String fs = String.format("%s/media/act/%s", 
            comc.getECSHOME(), fileName);
            String ft = String.format("%s/media/act/backup/%s.%s", 
            comc.getECSHOME(), fileName, sysDate+sysTime);
            if (comc.fileRename(fs, ft) == false) {
                comcr.errRtn(String.format("無法搬移檔案[%s]", fs), "", hCallBatchSeqno);
            }
/***
            if (comc.fileMove(fs, ft) == false) {
                comcr.errRtn(String.format("無法複製檔案[%s]", fs), "", hCallBatchSeqno);
            }
***/

            ft = Normalizer.normalize(ft, java.text.Normalizer.Form.NFKD);
            comc.chmod777(ft);

      	    showLogMessage("I", "", "檔案 [" + temstr0 + "] 已備份至 [" + ft + "]");

      	    if (comc.fileDelete(temstr0) == false) {
      		    showLogMessage("I", "", "來源檔案 [" + temstr0 + "] 刪除失敗!");
    	      }

          //showLogMessage("I", "", String.format("批 號 =[%s]", hApbtBatchNo));

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
    void selectPtrBusinday() throws Exception {

        sqlCmd = "select decode( cast(? as varchar(8)) ,'',business_date, ? ) h_busi_business_date ";
        sqlCmd += "  from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
        }

    }

    /***********************************************************************/
    void selectSmsMsgId() throws Exception {
        hSmidMsgId = "";
        hSmidMsgDept = "";
        hSmidMsgSendFlag = "";
        hSmidMsgSelAcctType = "";
        hSmidMsgUserid = "";
        hSmidMsgSendFlag = "N";

        sqlCmd = "select msg_id,";
        sqlCmd += " msg_dept,";
        sqlCmd += " decode(msg_send_flag , '', 'N', msg_send_flag ) h_smid_msg_send_flag,";
        sqlCmd += " decode(ACCT_TYPE_SEL, '', '0', ACCT_TYPE_SEL) h_smid_msg_sel_acct_type,"; // msg_sel_acct_type
        sqlCmd += " msg_userid ";
        sqlCmd += "  from sms_msg_id  ";
      //sqlCmd += " where msg_pgm ='ActA205' ";
        sqlCmd += " where msg_pgm ='ActB102' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSmidMsgId = getValue("msg_id");
            hSmidMsgDept = getValue("msg_dept");
            hSmidMsgSendFlag = getValue("h_smid_msg_send_flag");
            hSmidMsgSelAcctType = getValue("h_smid_msg_sel_acct_type");
            hSmidMsgUserid = getValue("msg_userid");
        }

    }

  /***********************************************************************/
  void checkFopen() throws Exception {
    fileName = String.format("POST006R_%8.8s.TXT", hBusiBusinessDate);
    temstr0 = String.format("/crdataupload/%s", fileName);
    temstr0 = Normalizer.normalize(temstr0, java.text.Normalizer.Form.NFKD);
    int br = openInputText(temstr0, "MS950");
    if (br == -1) {
      exceptExit = 0;
      comc.errExit(String.format("本日[%s]無檔案[/crdataupload/%s]",hBusiBusinessDate,fileName),
      hCallBatchSeqno);
    } else {
      closeInputText(br);
    }

    temstr1 = String.format("%s/media/act/%s", comc.getECSHOME(), fileName);
    temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
    if (comc.fileCopy(temstr0, temstr1) == false) {
      comcr.errRtn(String.format("複製檔案錯誤[%s] to [%s]",temstr0,temstr1),
      "", hCallBatchSeqno);
    }

    showLogMessage("I", "", String.format("input file name:[%s]", temstr1));

  }

    /***********************************************************************/
    void readFile() throws Exception {
        nSerialNo = 0;

        selectActPayBatch();
        hApbtBatchTotCnt = 0;
        hApbtBatchTotAmt = 0;

        int br = openInputText(temstr1, "MS950");
        if (br == -1) {
            comcr.errRtn(temstr1, "檔案開啓失敗！", hCallBatchSeqno);
        }
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if (str600.length() < 10)
                continue;

            if (DEBUG_MODE)
                showLogMessage("I", "", String.format("acct_bank[%s]", hPtclAcctBank));

            if (str600.substring(0, 1).equals("1")) {
                getField700();
            } else {
                break;
            }

            wsNotFound = "N";
            selectActOtherApay();
            hIdnoCellarPhone = "";

            if (wsNotFound.equals("Y"))
                continue;

            if (!hAoayStatusCode.equals("00")) {
                hB021ErrType = "2";
                /* --AAA--簡訊--AAA-- */
              //if (hAoayAcctType.equals("02")) {
                if (hAoayIdPSeqno.length() == 0) {
                    insertActCorpAutopay();
                } else if (hSmidMsgSendFlag.equals("Y")) {
                    selectCrdIdno();
                  //if (check_acct_type() == 0) {
                    szTmp = "";
                    szTmp = String.format("%s,%s,%s,%s,%s,代扣失敗",
                    hSmidMsgUserid, hSmidMsgId,
                    hIdnoCellarPhone, hIdnoChiName, hAchBcAbname);
                    insertSmsMsgDtl();
                  //} 
                }
                /* --VVV--mike For Message--VVV-- */

                insertActB002r1();
            } else {
                setPayDateRtn();
                insertActPayDetail();
                hApbtBatchTotCnt++;
                hApbtBatchTotAmt = hApbtBatchTotAmt + hApdlPayAmt;
            }
        }

        closeInputText(br);
        
        if ( (hApbtBatchTotCnt != 0) || (errorNo != 0) )
            insertActPayBatch();
    }

    /***********************************************************************/
    void selectActPayBatch() throws Exception {
        String tempstr = "";

        tempstr = String.format("%s1002%3.3s%c", hBusiBusinessDate, hPtclAcctBank, '%');
        hTempBatchNo = tempstr;

        sqlCmd = "select to_number(substr(max(batch_no),16,1))+1 h_temp_batch_no_seq ";
        sqlCmd += "  from act_pay_batch  ";
        sqlCmd += " where batch_no like ? ";
        setString(1, hTempBatchNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempBatchNoSeq = getValueLong("h_temp_batch_no_seq");
        }
        // else {
        // ws_in_file_flag = "N";
        // update_ptr_media_cntl();
        // comcr.err_rtn("Usage : ActB102 callbatch_seqno", "",
        // h_call_batch_seqno);
        // }

        if (hTempBatchNoSeq != 0) {
            tempstr = String.format("%15.15s%01d", hTempBatchNo, hTempBatchNoSeq);
        } else {
            tempstr = String.format("%15.15s1", hTempBatchNo);
        }
        hApbtBatchNo = tempstr;

    }

    /***********************************************************************/
    void getField700() throws Exception {
        String stra = "";
        String strb = "";
        int int1;

        if (nSerialNo == 0)
            nSerialNo++;

        if ((!str600.substring(0, 1).equals("1"))
                || ((!str600.substring(1, 2).equals("P")) && (!str600.substring(1, 2).equals("G")))
                || (!str600.substring(2, 5).equals("J03"))) {
            showLogMessage("I", "", String.format("媒體格式錯誤 !"));
            rollbackDataBase();
            comcr.errRtn("", "", hCallBatchSeqno);
        }

        stra = str600.substring(9, 9 + 7).trim();
        strb = String.format("%08d", comcr.str2long(stra) + 19110000);
        // if (val_date(strb) != 0)
        if (comcr.str2Date(strb) == null) {
            showLogMessage("I", "", String.format("入扣帳日期錯誤[%s] !", stra));
            rollbackDataBase();
            comcr.errRtn("", "", hCallBatchSeqno);
        }
        hAoayEnterAcctDate = strb;

        hAoayAutopayAcctNo = str600.substring(19, 19 + 14).trim();
        hAoayAutopayId = str600.substring(33, 33 + 10).trim();

        stra = str600.substring(43, 43 + 11).trim();
        hApdlPayAmt = comcr.str2double(stra);
        hApdlPayAmt = hApdlPayAmt / 100;

        /* Arthur 90/02/28 */
        hAoayPSeqno = str600.substring(64, 64 + 10).trim();
        hAoayStatusCode = str600.substring(78, 78 + 2).trim(); // 78+2

        if ((comc.getSubString(hAoayStatusCode, 0, 2).equals("  ")) || (hAoayStatusCode.length() == 0)) {
            hAoayStatusCode = "00";
        }

    }

    /***********************************************************************/
    void selectActOtherApay() throws Exception {
        hAoayAcctType = "";
        hAoayAcctKey = "";
        hAoayFromMark = "";
        hAoayStmtCycle = "";
        hAoayTransactionAmt = 0;
        hAoayAutopayId = "";
        orignStatusCode = "";
        hAoayIdPSeqno = "";
        hAoayId = "";
        hAoayIdCode = "";
        hAoayChiName = "";
        hAoayRowid = "";

        sqlCmd = "select a.acct_type,";
        sqlCmd += " UF_ACNO_KEY(a.p_seqno) acct_key,";
        sqlCmd += " a.from_mark,";
        sqlCmd += " a.stmt_cycle,";
        sqlCmd += " a.transaction_amt,";
        sqlCmd += " a.autopay_id,";
        sqlCmd += " a.status_code,";
        sqlCmd += " a.id_p_seqno,";
        sqlCmd += " b.id_no,";
        sqlCmd += " b.id_no_code,";
        sqlCmd += " a.chi_name,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += "  from act_other_apay a, crd_idno b ";
      //sqlCmd += " where a.acct_bank       = ? ";
        sqlCmd += " where a.acct_bank like ? || '%'  ";
        sqlCmd += "   and a.id_p_seqno      = b. id_p_seqno ";
        sqlCmd += "   and a.enter_acct_date = ? ";
        sqlCmd += "   and a.p_seqno         = ? ";
        sqlCmd += "   and a.from_mark       = '01' ";
        setString(1, hPtclAcctBank);
        setString(2, hAoayEnterAcctDate);
        setString(3, hAoayPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAoayAcctType = getValue("acct_type");
            hAoayAcctKey = getValue("acct_key");
            hAoayFromMark = getValue("from_mark");
            hAoayStmtCycle = getValue("stmt_cycle");
            hAoayTransactionAmt = getValueDouble("transaction_amt");
            hAoayAutopayId = getValue("autopay_id");
            orignStatusCode = getValue("status_code");
            hAoayIdPSeqno = getValue("id_p_seqno");
            hAoayId = getValue("id_no");
            hAoayIdCode = getValue("id_no_code");
            hAoayChiName = getValue("chi_name");
            hAoayRowid = getValue("rowid");
        }
        /*
         * ECSprintf(stderr,"bank no:[%s]\n",h_ptcl_acct_bank.arr);
         * ECSprintf(stderr,"acct date:[%s]\n",h_aoay_enter_acct_date.arr);
         * ECSprintf(stderr,"p seqno:[%s]\n",h_aoay_p_seqno.arr);
         * ECSprintf(stderr,"autopay no:[%s]\n",h_aoay_autopay_acct_no.arr);
         * ECSprintf(stderr,"autopay id:[%s]\n",h_aoay_autopay_id.arr);
         * ECSprintf(stderr,"orign_status_code:[%s]\n",orign_status_code.arr);
         * ECSprintf(stderr,"select code:[%d]\n",sqlca.sqlcode);
         */
        if (DEBUG_MODE) {
            showLogMessage("I", "", String.format(" stmt_cycle:[%s]", hAoayStmtCycle));
            showLogMessage("I", "", String.format("acct_bank[%s] enter_acct_date[%s] autopay_p_seqno[%s]",
                    hPtclAcctBank, hAoayEnterAcctDate, hAoayPSeqno));
        }
        if (recordCnt == 0) {
            wsNotFound = "Y";
            hB021ErrType = "0";
            insertActB002r1();
            if (hAoayStatusCode.equals("00")) {
                hAperErrorReason = "302";
                hAperErrorRemark = "AUT2 act_other_apay not found";
                insertActPayError();
            }
            return;
        } else if (!orignStatusCode.substring(0, 2).equals("99")) {
            wsNotFound = "Y";
            hB021ErrType = "1";
            insertActB002r1();
            if (hAoayStatusCode.equals("00")) {
                hAperErrorReason = "301";
                hAperErrorRemark = "AUT2 duplicated update act_other_apay";
                setPayDateRtn();
                insertActPayError();
            }
            updateActOtherApay();
            return;
        }

        if (hAoayTransactionAmt != hApdlPayAmt) { /* 91/07/15 Arthur */
            wsNotFound = "Y";
            hB021ErrType = "0";
            insertActB002r1();
            if (hAoayStatusCode.equals("00")) {
                if (DEBUG_MODE) {
                    showLogMessage("I", "", String.format("acct_bank[%s]", hPtclAcctBank));
                    showLogMessage("I", "", String.format("enter_acct_date[%s]", hAoayEnterAcctDate));
                    showLogMessage("I", "", String.format("p_seqno[%s]", hAoayPSeqno));
                }
                hAperErrorReason = "303";
                hAperErrorRemark = "AUT2 transaction amt not match";
                setPayDateRtn();
                insertActPayError();
            }
        } else {
            updateActOtherApay();
        }
    }

    /***********************************************************************/
    void insertActPayError() throws Exception {

        hApdlSerialNo = String.format("%05d", nSerialNo);
        nSerialNo++;
        errorNo++;

        daoTable = "act_pay_error";
        extendField = daoTable + ".";
        setValue(extendField + "batch_no", hApbtBatchNo);
        setValue(extendField + "serial_no", hApdlSerialNo);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "acno_p_seqno", hAoayPSeqno);
        setValue(extendField + "acct_type", hAoayAcctType);
        // setValue ( extendField+ "acct_key" , h_aoay_acct_key);
        setValueDouble(extendField + "pay_amt", hApdlPayAmt);
        setValue(extendField + "pay_date", hApdlPayDate);
        setValue(extendField + "payment_type", "AUT2");
        setValue(extendField + "error_reason", hAperErrorReason);
        setValue(extendField + "error_remark", hAperErrorRemark);
        setValue(extendField + "crt_user", hApbtModUser);
        setValue(extendField + "crt_date", sysDate);
        setValue(extendField + "crt_time", sysTime);
        setValue(extendField + "mod_user", hApbtModUser);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", hApbtModPgm);
        insertTable();
        if (dupRecord.equals("Y")) {
            rollbackDataBase();
            comcr.errRtn("insert_act_pay_error error", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActOtherApay() throws Exception {
        daoTable = "act_other_apay";
        updateSQL = "status_code = ?,";
        updateSQL += "batch_no    = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hAoayStatusCode);
        setString(2, hApbtBatchNo);
        setRowId(3, hAoayRowid);
        updateTable();
        if (notFound.equals("Y")) {
            rollbackDataBase();
            comcr.errRtn("update_act_other_apay error", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActCorpAutopay() throws Exception {
        daoTable = "act_corp_autopay";
        extendField = daoTable + ".";
        setValue(extendField + "business_date", hBusiBusinessDate);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "acct_type", hAoayAcctType);
        // setValue (extendField+"acct_key" , h_aoay_acct_key);
        setValueDouble(extendField + "autopay_amt", hAoayTransactionAmt);
        setValueDouble(extendField + "autopay_amt_bal", hAoayTransactionAmt - hApdlPayAmt);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", "ActB102");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_corp_autopay duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoChiName = "";
        hIdnoBirthday = "";
        hIdnoCellarPhone = "";
        hIdnoHomeAreaCode1 = "";
        hIdnoHomeTelNo1 = "";
        hIdnoHomeTelExt1 = "";

        sqlCmd = "select chi_name,";
        sqlCmd += " birthday,";
        sqlCmd += " cellar_phone,";
        sqlCmd += " decode(c.home_tel_no1,'',c.home_area_code2,c.home_area_code1) h_idno_home_area_code1,";
        sqlCmd += " decode(c.home_tel_no1,'',c.home_tel_no2,c.home_tel_no1      ) h_idno_home_tel_no1,";
        sqlCmd += " decode(c.home_tel_no1,'',c.home_tel_ext2,c.home_tel_ext1    ) h_idno_home_tel_ext1 ";
        sqlCmd += "  from crd_idno c  ";
        sqlCmd += " where id_p_seqno = ? ";
        setString(1, hAoayIdPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIdnoChiName = getValue("chi_name");
            hIdnoBirthday = getValue("birthday");
            hIdnoCellarPhone = getValue("cellar_phone");
            hIdnoHomeAreaCode1 = getValue("h_idno_home_area_code1");
            hIdnoHomeTelNo1 = getValue("h_idno_home_tel_no1)");
            hIdnoHomeTelExt1 = getValue("h_idno_home_tel_ext1");
        }
    }

    /***********************************************************************/
    int check_acct_type() throws Exception {

        if (hSmidMsgSelAcctType.equals("0"))
            return (0);

        if (hSmidMsgSelAcctType.equals("1")) {
            sqlCmd = "select data_code " + "from sms_dtl_data " + "where table_name='SMS_MSG_ID' " + "and data_key = ? "
                    + "and data_type='1'";
            setString(1, "ACT_B002");
            int recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                String data_code = getValue("data_code", i);
                if (data_code.equals(hAoayAcctType)) {
                    return 0;
                }
            }
        }

        return (1);
    }

    /***********************************************************************/
    void insertSmsMsgDtl() throws Exception {

        /*--cellphone format check--*/
        hSmdlCellphoneCheckFlag = "Y";
        hSmdlVoiceFlag = "";
        if (checkPhoneNo(hIdnoCellarPhone) != 0) {
            hSmdlCellphoneCheckFlag = "N";
            /*
             * if (h_idno_home_tel_no1.len!=0) { remark
             */
            hSmdlVoiceFlag = "Y";
            insertActAutopayVoice();
            /* } */
        }

        hModSeqno = comcr.getModSeq();

        daoTable = "sms_msg_dtl";
        extendField = daoTable + ".";
      //setValue(extendField + "msg_seqno", h_apbt_mod_seqno);
        setValue(extendField + "msg_seqno", hModSeqno + "");
      //setValue(extendField + "msg_pgm", "ACT_B002");
        setValue(extendField + "msg_pgm", "ActB102");
        setValue(extendField + "msg_dept", hSmidMsgDept);
        setValue(extendField + "msg_userid", hSmidMsgUserid);
        setValue(extendField + "msg_id", hSmidMsgId);
        setValue(extendField + "CELLAR_PHONE", hIdnoCellarPhone); // cellphone_no
        setValue(extendField + "cellphone_check_flag", hSmdlCellphoneCheckFlag);
        setValue(extendField + "CHI_NAME", hIdnoChiName); // holder_name
        setValue(extendField + "msg_desc", szTmp);
        setValue(extendField + "p_seqno", hAoayPSeqno); // acct_p_seqno
        setValue(extendField + "acct_type", hAoayAcctType);
        setValue(extendField + "id_p_seqno", hAoayIdPSeqno);
        setValue(extendField + "ID_NO", hAoayId); 
        setValue(extendField + "add_mode", "B");
        setValue(extendField + "crt_date", sysDate); // add_date
        setValue(extendField + "crt_time", sysTime); // add_time
        setValue(extendField + "CRT_USER", hApbtModUser); // add_user_id
        setValue(extendField + "apr_date", sysDate); // conf_date
        setValue(extendField + "APR_USER", hApbtModUser); // conf_user_id
        setValue(extendField + "APR_FLAG", "Y"); // approve_flag
        setValue(extendField + "SEND_FLAG", "N"); // msg_status
        setValue(extendField + "proc_flag", "N"); // 
        setValue(extendField + "mod_user", hApbtModUser);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", hApbtModPgm);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_sms_msg_dtl duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int checkPhoneNo(String pno) throws Exception {
        int i;
        String str10 = "";

        if (pno.length() != 10)
            return (1);

        str10 = pno;

        for (i = 0; i < 10; i++) {
            if ((str10.toCharArray()[i] < '0') || (str10.toCharArray()[i] > '9'))
                return (1); /* FALSE */
        }

        return (0);
    }

    /***********************************************************************/
    void insertActAutopayVoice() throws Exception {
        daoTable = "act_autopay_voice";
        extendField = daoTable + ".";
        setValue(extendField + "business_date", hBusiBusinessDate);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "acct_type", hAoayAcctType);
        // setValue(extendField+"acct_key" , h_aoay_acct_key);
        setValue(extendField + "chi_name", hIdnoChiName);
        setValue(extendField + "home_area_code", hIdnoHomeAreaCode1);
        setValue(extendField + "home_tel_no", hIdnoHomeTelNo1);
        setValue(extendField + "home_tel_ext", hIdnoHomeTelExt1);
        setValue(extendField + "ftp_date", "");
        setValue(extendField + "ftp_flag", "N");
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", "ActB102");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_autopay_voice duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActB002r1() throws Exception {
        errorNoR1++;
        daoTable = "act_b002r1";
        extendField = daoTable + ".";
        setValue(extendField + "batch_no", hApbtBatchNo);
        setValue(extendField + "print_date", hBusiBusinessDate);
        setValue(extendField + "enter_acct_date", hAoayEnterAcctDate);
      //setValue(extendField + "acct_bank", hPtclAcctBank);
        setValue(extendField + "acct_bank", hAchBankNo);
      //setValue(extendField + "autopay_acct_no", hAoayAutopayAcctNo);
        String  tmpstr16 = String.format("%016d", comcr.str2long(hAoayAutopayAcctNo));
        setValue(extendField + "autopay_acct_no", tmpstr16);
        setValue(extendField + "from_mark", "01");
        setValue(extendField + "stmt_cycle", hAoayStmtCycle);
        setValueDouble(extendField + "transaction_amt", hApdlPayAmt);
        setValue(extendField + "autopay_id", hAoayAutopayId);
        setValue(extendField + "autopay_id_code", hAoayAutopayIdCode);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "acct_type", hAoayAcctType);
      //setValue(extendField + "acct_key", h_aoay_acct_key);
        setValue(extendField + "id_p_seqno", hAoayIdPSeqno);
      //setValue(extendField + "id_no", h_aoay_id); // id
      //setValue(extendField + "id_code", h_aoay_id_code);
        setValue(extendField + "chi_name", hAoayChiName);
        setValue(extendField + "status_code", hAoayStatusCode);
        setValue(extendField + "err_type", hB021ErrType);
        setValue(extendField + "cellphone_no", hIdnoCellarPhone);
        insertTable();
        if (dupRecord.equals("Y")) {
            rollbackDataBase();
            comcr.errRtn("insert_act_b002r1 error", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void setPayDateRtn() throws Exception {
        hWdayThisCloseDate = "";

        if (hPtclValueDate.equals("2")) {
            hApdlPayDate = hAoayEnterAcctDate;
            return;
        }

        sqlCmd = "select this_lastpay_date ";
        sqlCmd += "  from ptr_workday  ";
        sqlCmd += " where stmt_cycle = ? ";
        setString(1, hAoayStmtCycle);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayThisLastpayDate = getValue("this_lastpay_date");
        } else {
            rollbackDataBase();
            comcr.errRtn(String.format("select ptr_workday error.stmt_cycle[%s]", hAoayStmtCycle), "", hCallBatchSeqno);
        }

        hApdlPayDate = hWdayThisLastpayDate;

    }

    /***********************************************************************/
    void insertActPayDetail() throws Exception {

        hApdlSerialNo = String.format("%05d", nSerialNo);
        nSerialNo++;
        rightNo++;

        daoTable = "act_pay_detail";
        extendField = daoTable + ".";
        setValue(extendField + "batch_no", hApbtBatchNo);
        setValue(extendField + "serial_no", hApdlSerialNo);
        setValue(extendField + "p_seqno", hAoayPSeqno);
        setValue(extendField + "acno_p_seqno", hAoayPSeqno);
        setValue(extendField + "acct_type", hAoayAcctType);
        // setValue ( extendField+ "acct_key" , h_aoay_acct_key);
        setValue(extendField + "id_p_seqno", hAoayIdPSeqno);
        // setValue ( extendField+ "id" , h_aoay_id);
        // setValue ( extendField+ "id_code" , h_aoay_id_code);
        setValueDouble(extendField + "pay_amt", hApdlPayAmt);
        setValue(extendField + "pay_date", hApdlPayDate);
        setValue(extendField + "payment_type", "AUT2");
        setValue(extendField + "crt_user", hApbtModUser); // update_user
        setValue(extendField + "crt_date", sysDate); // update_date
        setValue(extendField + "crt_time", sysTime); // update_time
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_user", hApbtModUser);
        setValue(extendField + "mod_pgm", hApbtModPgm);
        insertTable();
        if (dupRecord.equals("Y")) {
            rollbackDataBase();
            comcr.errRtn("insert_act_pay_detail error", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActPayBatch() throws Exception {

        daoTable = "act_pay_batch";
        extendField = daoTable + ".";
        setValue(extendField + "batch_no", hApbtBatchNo);
        setValueInt(extendField + "batch_tot_cnt", hApbtBatchTotCnt);
        setValueDouble(extendField + "batch_tot_amt", hApbtBatchTotAmt);
        setValue(extendField + "crt_user", hApbtModUser);
      //setValue(extendField + "crt_date", sysDate);
        setValue(extendField + "crt_date", hBusiBusinessDate);
        setValue(extendField + "crt_time", sysTime);
        setValue(extendField + "trial_user", hApbtModUser);
      //setValue(extendField + "trial_date", sysDate);
        setValue(extendField + "trial_date", hBusiBusinessDate);
        setValue(extendField + "trial_time", sysTime);
        setValue(extendField + "confirm_user", hApbtModUser);
        setValue(extendField + "confirm_date", hBusiBusinessDate);
        setValue(extendField + "confirm_time", sysTime);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "curr_code", "901");
        setValue(extendField + "mod_user", hApbtModUser);
        setValue(extendField + "mod_pgm", hApbtModPgm);
        insertTable();
        if (!dupRecord.equals("Y"))
            return;

        hModSeqno = comcr.getModSeq();

        daoTable = "act_pay_batch";
        updateSQL = "batch_tot_cnt  = batch_tot_cnt + ? ,";
        updateSQL += " batch_tot_amt = batch_tot_amt + ? ,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_user      = ?,";
        updateSQL += " mod_pgm       = ?,";
        updateSQL += " mod_seqno     = mod_seqno + 1 ";
        whereStr = "where batch_no = ? ";
        setInt(1, hApbtBatchTotCnt);
        setDouble(2, hApbtBatchTotAmt);
        setString(3, hApbtModUser);
        setString(4, hApbtModPgm);
      //setString(5, h_apbt_mod_seqno);
        setString(5, hApbtBatchNo);
        updateTable();

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActB102 proc = new ActB102();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
