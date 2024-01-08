/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/08/09  V1.01.01  Lai        Initial          old: crd_d008             *
* 107/10/29  V1.08.01  Lai        BECS-1071029-079 附卡check cyc_fee skip     *
* 107/12/10  V1.09.01  詹曜維                   RECS-s1071204-117 insert nccc_type         *
* 108/02/12  V1.10.01  詹曜維                   BECS-1080212-012 h_m_mbos_valid_fm         *
* 109/03/25  V1.11.01  Wilson     mbos_aps_batchno空白可substring              *
* 109/12/19  V1.00.02   shiyuqi       updated for project coding standard   *
* 112/02/14  V1.00.03  Wilson     fee_type用getSubString                      *
* 112/11/14  V1.00.04  JeffKung   不處理年費
*****************************************************************************/
package Crd;

import com.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class CrdD013 extends AccessDAO {
    private String progname = "轉入APS或其他介面作業 112/11/14  V1.00.04  ";
    private Map<String, Object> resultMap;

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    int debugD = 1;

    String checkHome = "";
    String hCallErrorDesc = "";
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hBusiChiDate = "";
    int totalCnt = 0;
    String tmpChar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;

    String mbosBatchno = "";
    double mbosRecno = 0;
    String mbosApplyId = "";
    String mbosCardNo = "";
    String mbosEmbossSource = "";
    String mbosEmbossReason = "";
    String mbosComboIndicator = "";
    String mbosOnlineMark = "";
    String mbosApsBatchno = "";
    String mbosMailCode = "";
    String mbosCardcat = "";
    String mbosValidTo = "";
    String mbosReasonCode = "";
    String mbosFeeCode = "";
    String mbosNcccType = "";
    int mbosStandardFee = 0;
    int mbosAnnualFee = 0;

    String hCorppSeqno = "";
    String hPSeqno = "";
    String hAcctType = "";
    String hCardType = "";
    String hGroupCode = "";
    String hSupFlag = "";
    String hMajorIdPSeqno = "";
    String hIdPSeqno = "";
    String hCorpPSeqno = "";
    String hIssueDate = "";
    String hGpNo = "";
    String hCurrFeeCode = "";
    String hStmtCycle = "";
    String hNewBegDate = "";
    String hNewEndDate = "";
    String hIdnoChiName = "";
    String hIdnoBirthday = "";
    String hMajorChiName = "";
    String hMajorBirthday = "";
    String hCardIndicator = "";
    String hApsFlag = "";
    String hApscStatusCode = "";
    String hApscReissueDate = "";
    String hPromoteEmpNo = "";
    String hDataType = "";
    int hFirstFee = 0;
    int hOtherFee = 0;
    double hSupRate = 0;
    int hSupEndMonth = 0;
    double hSupEndRate = 0;
    long pOtherFeeAmt = 0;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdD013 proc = new CrdD013();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            checkHome = comc.getECSHOME();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (args.length > 2) {
                String err1 = "CrdD013  [seq_no]\n";
                String err2 = "CrdD013  [seq_no]";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }
            comcr.hCallRProgramCode = this.getClass().getName();
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.hCallParameterData = javaProgram;
                for (int i = 0; i < args.length; i++) {
                    comcr.hCallParameterData = comcr.hCallParameterData + " " + args[i];
                }
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            dateTime();
            selectPtrBusinday();

            totalCnt = 0;

            selecCcrdEmboss();

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        }

        catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }

    } // End of mainProcess
      // ************************************************************************

    public void selectPtrBusinday() throws Exception {
        selectSQL = "business_date   , " + "to_char(sysdate,'yyyymmdd')    as SYSTEM_DATE ";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hBusiBusinessDate = getValue("BUSINESS_DATE");
        long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
        hBusiChiDate = Long.toString(hLongChiDate);

        showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");
    }

    // ************************************************************************
    public void selecCcrdEmboss() throws Exception {

        daoTable = "crd_emboss ";
        whereStr = "where in_other_date  = ''  " 
                 + "  and in_main_error  = '0' " + "  and in_main_date  <> ''  "
                 + "  and card_no       <> ''  " + "  and rtn_nccc_date <> ''  ";

        openCursor();

        while (fetchTable()) {
            initRtn();

            mbosBatchno = getValue("batchno");
            mbosRecno = getValueDouble("recno");
            mbosApplyId = getValue("apply_id");
            mbosCardNo = getValue("card_no");
            mbosEmbossSource = getValue("emboss_source");
            mbosEmbossReason = getValue("emboss_reason");
            mbosComboIndicator = getValue("combo_indicator");
            if (mbosComboIndicator.length() < 1)
                mbosComboIndicator = "N";
            mbosOnlineMark = getValue("online_mark");
            mbosApsBatchno = getValue("aps_batchno");
            mbosCardcat = getValue("cardcat");
            mbosValidTo = getValue("valid_to");
            mbosReasonCode = getValue("reason_code");
            mbosFeeCode = getValue("fee_code");
            mbosNcccType = getValue("nccc_type");
            mbosStandardFee = getValueInt("standard_fee");
            mbosAnnualFee = getValueInt("annual_fee");

            totalCnt++;
            processDisplay(5000); // every nnnnn display message
            if (debug == 1) {
                showLogMessage("I", "", "  888 Card=[" + mbosCardNo + "]");
                showLogMessage("I", "", "  888  src=[" + mbosEmbossSource + "]");
            }

            switch (mbosEmbossSource.trim()) {
            case "1":
                procNewCard();
                ;
                break; // * 新製卡
            case "3":
            case "4":
                procChgCard();
                ;
                break; // * 續卡(換卡)
            case "5":
                procReissueCard();
                ;
                break; // * 重製卡(補發卡)
            default:
                break;
            }
            /* lai curr */
            /* 20150413 三合一卡改不等於N */
            if ((mbosComboIndicator.compareTo("N") != 0) &&
                (mbosOnlineMark.compareTo("0")     == 0)) {
                updateCrdCombo();
            }

            updateCrdEmboss();
        }

    }

    // ************************************************************************
    public int procNewCard() throws Exception {
        /* 製卡成功與不成功 */
        if (getValue("reject_code").length() == 0) {
            /* 已入主檔成功 */
            if (getValue("in_main_date").length() > 0 && getValue("in_main_error").equals("0")) {
                getCardData();
                getPtrAcctType();
                hApsFlag = "Y";
                insertApscdsuc();

                selectSQL = "count(*) as move_cnt ";
                daoTable = "crd_move_list ";
                whereStr = "where new_card_no   = ? ";

                setString(1, mbosCardNo);
                tmpInt = selectTable();
                
                if ((getValueInt("move_cnt") > 0) || 
                	(mbosApsBatchno.trim().length() != 0 &&
                    mbosApsBatchno.substring(0, 5).equals("ECSUP"))) {
                    hApscStatusCode = "7";
                    insertApscard();
                }

                /*** change curr_fee_code *****/
                //20231114不處理年費
                //if ((tmpInt = insertCycAfee()) == 0) {
                //    updateCrdCard();
                //}
                
                
                mbosMailCode = "Y";
                if (mbosCardcat.length() > 2) {
                    updateStarCard();
                }
            }
        }

        return (0);
    }

    // ************************************************************************
    public int procChgCard() throws Exception {

        if (getValue("reject_code").length() == 0) {
            getCardData();
            getPtrAcctType();

            processApscard();
        }

        return (0);
    }

    // ************************************************************************
    public int procReissueCard() throws Exception {

        if (getValue("reject_code").length() == 0) {
            getCardData();
            getPtrAcctType();

            processApscard();
            
            //20231114不處理年費
            //if (mbosEmbossReason.equals("1") || mbosEmbossReason.equals("3")) {
            //    /*********************************************
            //     * 未抓取到特定年之年費
            //     *********************************************/
            //    tmpInt = chkCycAfee();
            //    if (tmpInt != 0) {
            //        tmpInt = insertCycAfee();
            //        if (tmpInt == 0) {
            //            updateCrdCard();
            //        }
            //    }
            //}
        }

        return (0);
    }

    // ************************************************************************
    public int processApscard() throws Exception {

        switch (mbosEmbossSource.trim()) {
        case "1":
        case "2":
            hApscStatusCode = "";
            break; // * 新製卡
        case "3":
        case "4":
            hApscStatusCode = "7";
            break;
        case "5":
            switch (mbosEmbossSource.trim()) {
            case "1":
                hApscStatusCode = "2";
                break;
            case "2":
                hApscStatusCode = "6";
                break;
            case "3":
                hApscStatusCode = "1";
                break;
            }
            break;
        case "7":
            hApscStatusCode = "2";
            break;
        default:
            hApscStatusCode = "";
            break;
        }

        hApscReissueDate = getValue("in_main_date");

        extendField = "apsc.";
        selectSQL = "rowid   as rowid  ";
        daoTable = "crd_apscard   ";
        whereStr = "where card_no       =  ? " + "  and to_aps_date   = '' ";

        setString(1, mbosCardNo);

        tmpInt = selectTable();

        String hRowid = getValue("apsc.rowid");

        if (tmpInt > 0) {
            updateSQL = "status_code       =  ? , " + "reissue_date      =  ? , " 
                      + "mod_pgm           =  ? , "
                      + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable = "crd_apscard ";
            whereStr = "where rowid   = ? ";

            setString(1, hApscStatusCode);
            setString(2, hApscReissueDate);
            setString(3, javaProgram);
            setString(4, sysDate + sysTime);
            setRowId(5, hRowid);

            updateTable();

            if (notFound.equals("Y")) {
                String err1 = "update_crd_emboss    error[notFind]";
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }
        } else {
            insertApscard();
        }

        return (0);
    }

    // ************************************************************************
    public int chkOrgAnnualFee(String chkCd) throws Exception {
        double pAnnualFee = 0, hAnnualFee = 0;

        /* 正卡 */
        if (hSupFlag.equals("0"))
            pAnnualFee = (double) hOtherFee;
        else
            pAnnualFee = (double) (hOtherFee * hSupRate) / 100;

        switch (chkCd) {
        case " ":
        case "0": /* 當有source code時,抓取參數年費設定之標準年費 */
            hAnnualFee = pAnnualFee;
            break;
        case "1":
        case "2":
        case "3":
        case "4":
        case "5":
        case "6":
        case "7":
        case "8":
        case "9":
        case "Z":
            hAnnualFee = 0;
            break;
        default:
            hAnnualFee = pAnnualFee;
            break;
        }
        mbosReasonCode = "";
        mbosFeeCode = hCurrFeeCode;
        mbosStandardFee = (int) pAnnualFee;
        mbosAnnualFee = (int) hAnnualFee;

        return (0);
    }

    // ************************************************************************
    public int chkBusAnnualFee(String chkCd) throws Exception {
        long hAnnualFee = 0;

        switch (chkCd) {
        case " ":
        case "0": /* 當有source code時,抓取參數年費設定之標準年費 */
            hAnnualFee = pOtherFeeAmt;
            break;
        case "1":
        case "2":
        case "3":
        case "4":
        case "5":
        case "6":
        case "7":
        case "8":
        case "9":
        case "Z":
            hAnnualFee = 0;
            break;
        default:
            hAnnualFee = pOtherFeeAmt;
            break;
        }
        mbosReasonCode = "";
        mbosFeeCode = hCurrFeeCode;
        mbosStandardFee = (int) pOtherFeeAmt;
        mbosAnnualFee = (int) hAnnualFee;

        return (0);
    }

    // ************************************************************************
    public int selectCrdIdno(String chkIdPSeqno) throws Exception {
        extendField = "idno.";
        selectSQL = "chi_name   ,birthday ";
        daoTable = "crd_idno      ";
        whereStr = "where id_p_seqno    = ? " + "fetch first 1 row only";

        setString(1, chkIdPSeqno);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_crd_idno  error[notFind]=[" + chkIdPSeqno + "]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int getPtrAcctType() throws Exception {
        selectSQL = "card_indicator       ";
        daoTable = "ptr_acct_type ";
        whereStr = "where acct_type    = ? ";

        setString(1, hAcctType);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_acct_type error[notFind]" + mbosCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hCardIndicator = getValue("card_indicator");

        return (0);
    }

    /***************************************************************************
     * 檢核重製卡(掛失及偽卡)是否已產生年費 ? 1.若系統月份小於效期月,檢核前一年(system year -1)年費是否有收 -- check
     * cyc_afee之 substr(fee_date,'yyyy') = (sys year -1)
     * 2.若系統月份大於效期月份,檢核本年年費是否已收 -- check cyc_afee之 substr(fee_date,'yyyy') =
     * system year (modified date 2001/08/24)
     ***************************************************************************/
    // ************************************************************************
    public int chkCycAfee() throws Exception {

        tmpChar = sysDate.substring(0, 4);
        int tYear = Integer.parseInt(tmpChar);
        tmpChar = sysDate.substring(4, 6);
        tmpChar1 = mbosValidTo.substring(4, 6);
        if (tmpChar.compareTo(tmpChar1) < 0)
            tYear = tYear - 1;

        tmpChar = Integer.toString(tYear);

        String year = comc.getSubString(getValue("valid_fm"), 0, 4);
        if (getValue("sup_flag").equals("1") && tYear < comc.str2int(year)) {
            return (0);
        }
        
        selectSQL = "count(*)   as afe1_cnt ";
        daoTable = "cyc_afee   ";
        whereStr = "where id_p_seqno   = ? " 
                 + "  and acct_type    = ? " 
                 + "  and group_code   = ? "
                 + "  and card_type    = ? "
                 + "  and substr(fee_date,1,4) = ? ";

        setString(1, hIdPSeqno);
        setString(2, hAcctType);
        setString(3, hGroupCode);
        setString(4, hCardType);
        setString(5, tmpChar);

        tmpInt = selectTable();

        /**************************************
         * 此年之年費未產生
         **************************************/
        if (getValueInt("afe1_cnt") < 1) {
            processAnnual();
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public int processAnnual() throws Exception {
        int flag = 0;

        flag = getGroupCard();
        if (hCardIndicator.equals("1")) {
            flag = chkOrgAnnualFee(hCurrFeeCode);
        }

        flag = 0;
        if (hCardIndicator.equals("2")) {
            flag = getCorpFee();
            flag = chkBusAnnualFee(hCurrFeeCode);
        }

        return (flag);
    }

    // ******* 抓取年費參數檔 ******************************************************
    public int getGroupCard() throws Exception {
        selectSQL = "first_fee     ,other_fee        , " + "sup_rate      ,sup_end_month    , sup_end_rate ";
        daoTable = "ptr_group_card ";
        whereStr = "where card_type    = ? " + "  and group_code   = ? ";

        setString(1, hCardType);
        setString(2, hGroupCode);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_group_card error[notFind]" + hCardType;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hFirstFee = getValueInt("first_fee");
        hOtherFee = getValueInt("other_fee");
        hSupRate = getValueDouble("sup_rate");
        hSupEndMonth = getValueInt("sup_end_month");
        hSupEndRate = getValueDouble("sup_end_rate");

        return (0);
    }

    // ************************************************************************
    public int getCorpFee() throws Exception {
        selectSQL = "other_fee_amt    ";
        daoTable = "ptr_corp_fee   ";
        whereStr = "where card_type    = ? " + "  and corp_p_seqno = ? ";

        setString(1, hCardType);
        setString(2, hCorpPSeqno);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            tmpInt = getGroupCard();
            if (tmpInt != 0)
                return (1);
            pOtherFeeAmt = hOtherFee;
        }

        pOtherFeeAmt = getValueInt("other_fee");

        return (0);
    }

    // ************************************************************************
    public int insertApscard() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " insert apscard=[" + getValue("apply_id") + "]");

        /* 附卡 */
        if (hSupFlag.equals("1")) {
            setValue("sup_id"      , getValue("apply_id"));
            setValue("sup_id_code" , getValue("apply_id_code"));
            setValue("sup_birthday", getValue("birthday"));
            setValue("sup_chi_name", hIdnoChiName);
            setValue("pm_name"     , hMajorChiName);
            setValue("pm_birthday" , hMajorBirthday);
        } else {
            setValue("sup_id"      , "");
            setValue("sup_id_code" , "");
            setValue("sup_birthday", "");
            setValue("sup_chi_name", "");
            setValue("pm_name"     , hIdnoChiName);
            setValue("pm_birthday" , getValue("birthday"));
        }
        setValue("valid_date"     , getValue("valid_to"));
        setValue("status_code"    , hApscStatusCode);
        setValue("reissue_date"   , hApscReissueDate);
        setValue("mail_no"        , "");
        setValue("mail_branch"    , "");
        setValue("mail_date"      , "");
        setValue("sup_lost_status", "");
        setValue("crt_datetime"   , sysDate + sysTime);
        setValue("mod_time"       , sysDate + sysTime);
        setValue("mod_pgm"        , javaProgram);

        daoTable = "crd_apscard  ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_apscard       error[dupRecord]=" + mbosCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int insertApscdsuc() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " insert apscdsuc=[" + getValue("birthday") + "]");

        if (hSupFlag.equals("1")) {
            setValue("sup_id"      , getValue("apply_id"));
            setValue("sup_id_code" , getValue("apply_id_code"));
            setValue("sup_birthday", getValue("birthday"));
            setValue("sup_chi_name", hIdnoChiName);
            setValue("pm_birthday" , hMajorBirthday);
        } else {
            setValue("sup_id"      , "");
            setValue("sup_id_code" , "");
            setValue("sup_birthday", "");
            setValue("sup_chi_name", "");
            setValue("pm_birthday" , getValue("birthday"));
        }
        setValue("nccc_type"   , getValue("nccc_type"));
        setValueInt("aps_seqno", getValueInt("seqno"));
        setValue("chi_name"    , hIdnoChiName);
        setValue("valid_date"  , getValue("valid_to"));
        setValue("aps_flag"    , hApsFlag);
        setValue("crt_date"    , sysDate);
        setValue("mod_time"    , sysDate + sysTime);
        setValue("mod_pgm"     , javaProgram);

        daoTable = "crd_apscdsuc  ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_apscdsuc      error[dupRecord]=" + mbosCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int insertCycAfee() throws Exception {

        selectSQL = "count(*) as afee_cnt ";
        daoTable = "cyc_afee      ";
        whereStr = "where card_no   = ? ";

        setString(1, mbosCardNo);
        tmpInt = selectTable();

        if (debug == 1)
            showLogMessage("I", "", " insert afee=[" + getValueInt("afee_cnt") + "]");

        if (getValueInt("afee_cnt") > 0)
            return (1);

        switch (mbosEmbossSource.trim()) {
        case "1":
        case "2":
            hDataType = "1";
            break; // * 新製卡
        case "3":
        case "4":
        case "5":
        case "6":
        case "7":
            hDataType = "2";
            break; // * 重製卡(補發卡)
        default:
            hDataType = "3";
            break;
        }
        /**********************************************
         * introduce_no size 6 = promote_emp_no size 4 = introduce_emp_no
         * (2001/3/22)
         **********************************************/
        String hIntroduceEmpNo = "";

        tmpInt = getValue("introduce_no").length();
        if (tmpInt > 0)
            hPromoteEmpNo = getValue("introduce_no").substring(0, tmpInt);
        if (tmpInt == 4)
            hIntroduceEmpNo = getValue("introduce_no").substring(0, 4);

//      setValue("fee_date"            , getValue("valid_fm"));
        setValue("fee_date"            , hIssueDate);
        setValue("expire_date"         , getValue("valid_to"));
        setValue("p_seqno"             , hPSeqno);
        setValue("id_p_seqno"          , hIdPSeqno);
        setValue("corp_p_seqno"        , hCorpPSeqno);
        setValue("data_type"           , hDataType);
        setValue("indicator"           , "");
        setValue("reason_code"         , mbosReasonCode);
        setValue("fee_type"            , comc.getSubString(mbosFeeCode,0,1));
        setValue("stmt_cycle"          , hStmtCycle);
        setValue("issue_date"          , hIssueDate);
        setValue("introduce_emp_no"    , hIntroduceEmpNo);
        setValue("promote_emp_no"      , hPromoteEmpNo);
        setValueDouble("org_annual_fee", (double) mbosStandardFee);
        setValueDouble("rcv_annual_fee", (double) mbosAnnualFee);
        setValue("maintain_code"       , "");
        setValue("crt_date"            , sysDate);
        setValue("apr_user"            , javaProgram);
        setValue("apr_date"            , sysDate);
        setValue("mod_time"            , sysDate + sysTime);
        setValue("mod_pgm"             , javaProgram);

        daoTable = "cyc_afee    ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_cyc_afee          error[dupRecord]=" + mbosCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int getCardData() throws Exception {

        extendField = "card.";
        selectSQL = "acno_p_seqno       ,acct_type       ,corp_p_seqno, " 
                  + "card_type     ,group_code      ,sup_flag, "
                  + "id_p_seqno    ,major_id_p_seqno, " + "issue_date    ,p_seqno           , "
                  + "curr_fee_code ,stmt_cycle      , " + "new_beg_date  ,new_end_date    ";
        daoTable = "crd_card            ";
        whereStr = "WHERE card_no      = ?  ";

        setString(1, mbosCardNo);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_crd_card      error[notFind]" + mbosCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hPSeqno = getValue("card.acno_p_seqno");
        hAcctType = getValue("card.acct_type");
        hCorpPSeqno = getValue("card.corp_p_seqno");
        hCardType = getValue("card.card_type");
        hGroupCode = getValue("card.group_code");
        hSupFlag = getValue("card.sup_flag");
        hMajorIdPSeqno = getValue("card.major_id_p_seqno");
        hIdPSeqno = getValue("card.id_p_seqno");
        hIssueDate = getValue("card.issue_date");
        hGpNo = getValue("card.p_seqno");
        hCurrFeeCode = getValue("card.curr_fee_code");
        hStmtCycle = getValue("card.stmt_cycle");
        hNewBegDate = getValue("card.new_beg_date");
        hNewEndDate = getValue("card.new_end_date");

        if (debug == 1)
            showLogMessage("I", "", " get card  =[" + hIdPSeqno + "]");
        selectCrdIdno(hIdPSeqno);
        hIdnoChiName = getValue("idno.chi_name");
        hIdnoBirthday = getValue("idno.birthday");

        if (hSupFlag.equals("1")) {
            selectCrdIdno(hMajorIdPSeqno);
            hMajorChiName = getValue("idno.chi_name");
            hMajorBirthday = getValue("idno.birthday");
        }

        return (0);
    }

    // ************************************************************************
    public int updateCrdCard() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " upd card  =[" + hCurrFeeCode + "]");
        if (hCurrFeeCode.length() < 1)
            return (1);

        if (comm.isNumber(hCurrFeeCode)) {
            int tmpVal = Integer.parseInt(hCurrFeeCode);

            tmpVal = tmpVal - 1;
            tmpChar1 = String.format("%-2d", tmpVal);
            tmpChar = tmpChar1.substring(0, 1);

            updateSQL = "curr_fee_code     =  ? , " + "mod_pgm           =  ? , "
                    + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable = "crd_card ";
            whereStr = "where card_no       = ? ";

            setString(1, tmpChar);
            setString(2, javaProgram);
            setString(3, sysDate + sysTime);
            setString(4, mbosCardNo);

            updateTable();

            if (notFound.equals("Y")) {
                String err1 = "insert_crd_card          error[dupRecord]=" + mbosCardNo;
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }
        }

        return (0);
    }

    // ************************************************************************
    public int updateStarCard() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " upd star  =[" + mbosMailCode + "]");

        updateSQL = "mail_code         =  ? , " + "in_other_date     =  ? , " 
                  + "mod_pgm           =  ? , "
                  + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        daoTable = "crd_emboss";
        whereStr = "where batchno = ? " + "  and recno   = ? ";

        setString(1, mbosMailCode);
        setString(2, sysDate);
        setString(3, javaProgram);
        setString(4, sysDate + sysTime);
        setString(5, mbosBatchno);
        setDouble(6, mbosRecno);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss    error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        /* check是否還有其他星座卡需要做,若有則不需做寄件動作,否則要做 */

        selectSQL = "count(*) as star_cnt ";
        daoTable = "crd_star_card ";
        whereStr = "where apply_id      = ?  " + "  and apply_id_code = ? " 
                 + "  and send_date     = '' " + "  and emboss_date   = '' ";

        setString(1, getValue("apply_id"));
        setString(2, getValue("apply_id_code"));
        tmpInt = selectTable();

        if (getValueInt("star_cnt") > 0)
            mbosMailCode = "N";

        return (0);
    }

    // ************************************************************************
    public int updateCrdCombo() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " upd combo =[" + getValue("apply_id_code") + "]");

        updateSQL = "apply_id_code     =  ? , " + "emboss_code       = '0', " 
                  + "emboss_date       =  ? , " + "mod_pgm           =  ? , " 
                  + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        daoTable  = "crd_combo ";
        whereStr  = "where card_no = ? ";

        setString(1, getValue("apply_id_code"));
        setString(2, sysDate);
        setString(3, javaProgram);
        setString(4, sysDate + sysTime);
        setString(5, mbosCardNo);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_combo     error[notFind]" + mbosCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int updateCrdEmboss() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " upd emboss=[" + mbosMailCode + "]");

        updateSQL = "mail_code         =  ? , " + "in_other_date     =  ? , " 
                  + "mod_pgm           =  ? , "
                  + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        daoTable  = "crd_emboss";
        whereStr  = "where batchno = ? " + "  and recno   = ? ";

        setString(1, mbosMailCode);
        setString(2, sysDate);
        setString(3, javaProgram);
        setString(4, sysDate + sysTime);
        setString(5, mbosBatchno);
        setDouble(6, mbosRecno);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss    error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public void initRtn() throws Exception {

        mbosBatchno = "";
        mbosRecno = 0;
        mbosApplyId = "";
        mbosCardNo = "";
        mbosEmbossSource = "";
        mbosEmbossReason = "";
        mbosComboIndicator = "";
        mbosOnlineMark = "";
        mbosApsBatchno = "";
        mbosMailCode = "";
        mbosCardcat = "";
        mbosValidTo = "";

        mbosReasonCode = "";
        mbosFeeCode = "";
        mbosStandardFee = 0;
        mbosAnnualFee = 0;

        hPSeqno = "";
        hAcctType = "";
        hCorpPSeqno = "";
        hCardType = "";
        hGroupCode = "";
        hSupFlag = "";
        hMajorIdPSeqno = "";
        hIdPSeqno = "";
        hIssueDate = "";
        hGpNo = "";
        hCurrFeeCode = "";
        hStmtCycle = "";
        hNewBegDate = "";
        hNewEndDate = "";
        hIdnoChiName = "";
        hIdnoBirthday = "";
        hMajorChiName = "";
        hMajorBirthday = "";
        hCardIndicator = "";
        hApscStatusCode = "";
        hApscReissueDate = "";
        hPromoteEmpNo = "";
        hDataType = "";
        hFirstFee = 0;
        hOtherFee = 0;
        hSupRate = 0;
        hSupEndMonth = 0;
        hSupEndRate = 0;
        pOtherFeeAmt = 0;
    }
    // ************************************************************************

} // End of class FetchSample
