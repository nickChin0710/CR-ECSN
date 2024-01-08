/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- --------- ------------------------------------------  *
*  106/06/01  V1.00.00  Edson     program initial                             *
*  107/02/05  V1.08.01  Brian     RECS-s1070205-013 數位存款舊資料sql update	  *
*  108/12/11  V1.09.01  Rou       Update dbc_emboss() & dbc_debit()           *
*  109/03/09  V1.09.02  Wilson    新增tsc_autoload_flag                        *
*  109/05/12  V1.09.03  Wilson    set apply_source = "S" or "T"               *
*  109/05/12  V1.09.04  Wilson    新增 vd_bank_no                               *
*  109/05/14  V1.09.05  Wilson    rtn_ibm_date修改                                                                                 *
*  109/11/03  V1.09.06  Wilson    DBC_D020 -> DbcD020                         *
*  109/12/24  V1.09.07  yanghan       修改了變量名稱和方法名稱            *
*  110/06/24  V1.09.08  Wilson    insert dbc_emboss新增mail_branch             *
*  112/02/08  V1.09.09  Wilson    hNewBatchno = hChgBatchno時自動+1             *
*  112/02/09  V1.00.10  Wilson    hChgBatchno = hNewBatchno時自動+1             *
*  112/05/02  V1.00.11  Wilson    insert dbc_meboss add service_code          *
*  112/08/26  V1.00.12  Wilson    修正弱掃問題                                                                                                   *
*  112/11/26  V1.00.13  Wilson    update dbc_emboss_tmp改用card_no             *
*  112/11/30  V1.00.14  Wilson    判斷數位帳戶                                                                                                   *
*  112/12/11  V2.00.15  Wilson    crd_item_unit不判斷卡種                                                                   *
*  112/12/11  V1.00.16  Wilson    團代為2200且帳號5~7碼為988才是數位帳戶                                                 *
*  113/01/04  V1.00.17  Wilson    還原getComboAcctNo                           *
******************************************************************************/

package Dbc;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*非新製卡接收DEBIT*/
public class DbcD020 extends AccessDAO {
    private String progname = "非新製卡接收DEBIT CARD 送製卡資料處理 113/01/04 V1.00.17";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debugD = 0;

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

    String prgmId = "DbcD020";

    String hBirthday = "";
    String hChiName = "";
    String hNation = "";
    String hSex = "";
    String hMarriage = "";
    String hDcesApplyId = "";
    String hDcesApplyIdCode = "";
    String hDcesMailZip = "";
    String hDcesMailAddr1 = "";
    String hDcesMailAddr2 = "";
    String hDcesMailAddr3 = "";
    String hDcesMailAddr4 = "";
    String hDcesMailAddr5 = "";
    String hDcesAcctType = "";
    String hDcesAcctKey = "";
    String hDcesActNo = "";
    String hBatchno = "";
    String hEmbossSource = "";
    String hEmbossReason = "";
    int valMon = 0;
    String hDcesValidFm = "";
    String hGroupAbbrCode = "";
    String hDcesGroupCode = "";
    String hNcccBatchno = "";
    double hNcccRecno = 0;
    double hDcesSeqno = 0;
    double hDcesSourceRecno = 0;
    String hDcesApsRecno = "";
    String hDcesServiceYear = "";
    String hDcesSalary = "";
    String hDcesValue = "";
    String hDcesCreditLmt = "";
    String hDcesStandardFee = "";
    String hDcesAnnualFee = "";
    String hDcesAuthCreditLmt = "";
    String hDcesEmbossSource = "";
    String hDcesEmbossReason = "";
    String hDcesResendNote = "";
    String hDcesSourceBatchno = "";
    String hDcesApsBatchno = "";
    String hDcesToNcccCode = "";
    String hDcesCardType = "";
    String hDcesUnitCode = "";
    String hDcesCardNo = "";
    String hDcesMajorCardNo = "";
    String hDcesMajorValidFm = "";
    String hDcesMajorValidTo = "";
    String hDcesMajorChgFlag = "";
    String hDcesOldCardNo = "";
    String hDcesChangeReason = "";
    String hDcesStatusCode = "";
    String hDcesReasonCode = "";
    String hDcesMemberNote = "";
    String hDcesPmId = "";
    String hDcesPmIdCode = "";
    String hDcesSourceCode = "";
    String hDcesCorpNo = "";
    String hDcesCorpNoCode = "";
    String hDcesCorpActFlag = "";
    String hDcesCorpAssureFlag = "";
    String hDcesRegBankNo = "";
    String hDcesRiskBankNo = "";
    String hDcesChiName = "";
    String hDcesEngName = "";
    String hDcesBirthday = "";
    String hDcesMarriage = "";
    String hDcesRelWithPm = "";
    String hDcesEducation = "";
    String hDcesNation = "";
    String hDcesResidentZip = "";
    String hDcesResidentAddr1 = "";
    String hDcesResidentAddr2 = "";
    String hDcesResidentAddr3 = "";
    String hDcesResidentAddr4 = "";
    String hDcesResidentAddr5 = "";
    String hDcesCompanyName = "";
    String hDcesJobPosition = "";
    String hDcesHomeAreaCode1 = "";
    String hDcesHomeTelNo1 = "";
    String hDcesHomeTelExt1 = "";
    String hDcesHomeAreaCode2 = "";
    String hDcesHomeTelNo2 = "";
    String hDcesHomeTelExt2 = "";
    String hDcesOfficeAreaCode1 = "";
    String hDcesOfficeTelNo1 = "";
    String hDcesOfficeTelExt1 = "";
    String hDcesOfficeAreaCode2 = "";
    String hDcesOfficeTelNo2 = "";
    String hDcesOfficeTelExt2 = "";
    String hDcesEMailAddr = "";
    String hDcesCellarPhone = "";
    String hDcesVip = "";
    String hDcesFeeCode = "";
    String hDcesFinalFeeCode = "";
    String hDcesForceFlag = "";
    String hDcesBusinessCode = "";
    String hDcesIntroduceNo = "";
    String hDcesValidTo = "";
    String hDcesSex = "";
    String hDcesAcceptDm = "";
    String hDcesApplyNo = "";
    String hDcesCardcat = "";
    String hDcesMailType = "";
    String hDcesIntroduceId = "";
    String hDcesIntroduceName = "";
    String hDcesSalaryCode = "";
    String hDcesStudent = "";
    String hDcesPoliceNo1 = "";
    String hDcesPoliceNo2 = "";
    String hDcesPoliceNo3 = "";
    String hDcesPmCash = "";
    String hDcesSupCash = "";
    String hDcesOnlineMark = "";
    String hDcesErrorCode = "";
    String hDcesRejectCode = "";
    String hDcesEmboss4thData = "";
    String hDcesMemberId = "";
    String hDcesPmBirthday = "";
    String hDcesSupBirthday = "";
    String hDcesFeeReasonCode = "";
    String hDcesChgAddrFlag = "";
    String hDcesPvv = "";
    String hDcesCvv = "";
    String hDcesCvv2 = "";
    String hDcesPvki = "";
    String hDcesPinBlock = "";
    String hDcesOldBegDate = "";
    String hDcesOldEndDate = "";
    String hDcesServiceCode = "";
    String hDcesSupFlag = "";
    String hDcesCreditFlag = "";
    String hDcesCommFlag = "";
    String hDcesResidentNo = "";
    int hDcesOtherCntryCode = 0;
    String hDcesPassportNo = "";
    String hDcesDiffCode = "";
    String hDcesStaffFlag = "";
    String hDcesStmtCycle = "";
    String hDcesNcccType = "";
    String hDcesOrgIndivCrdLmt = "";
    String hDcesIndivCrdLmt = "";
    String hDcesOrgCashLmt = "";
    String hDcesCashLmt = "";
    String hDcesIcFlag = "";
    String hDcesElectronicCode = "";
    String hDcesBranch = "";
    String hDcesBinNo = "";
    String hDcesMailAttach1 = "";
    String hDcesMailAttach2 = "";
    String hDcesIcIndicator = "";
    String hDcesIcCvv = "";
    String hDcesIcPin = "";
    String hDcesDerivKey = "";
    String hDcesLOfflnLmt = "";
    String hDcesUOfflnLmt = "";
    String hDcesApplySource = "";
    String hDcesApplyIbmIdCode = "";
    String hDcesPmIbmIdCode = "";
    String hDcesCreateId = "";
    String hDcesThirdRsn = "";
    String hDcesReissueCode = "";
    String hDcesReceiptBranch = "";
    String hDcesReceiptRemark = "";
    String hDcesDigitalFlag   = "";
    String hDecsOldCrtBankNo = "";
    String hDecsOldVdBankNo = "";
    String hDcesCardRefNum = "";
    String hDcesTscAutoloadFlag = "";
    String hDcesMailBranch = "";
    String hDecsOldCardRefNum = "";
    String hDecsOldMailBranch = "";
       
    double tempAmt = 0;
    String hRelation = "";
    String hComboAcctNo = "";
    String hRowid = "";
    String hTransType = "";
    String hSupFlag = "";
    String hDcesVoiceNum = "";
    String hComboIndicator = "";
    String hDcesRowid = "";
    String hRedoFlag = "";
    String hTmpDate = "";
    String hNewBatchno = "";
    long hNewRecno = 0;
    String hChgBatchno = "";
    long hChgRecno = 0;
    String hProgCode = "";
    String hWfValue = "";
    int recCount = 0;
    String hSuperId = "";
    String hCreateDate = "";
    int val = 0;
    int totCnt = 0;
    int recCnt = 0;
    private String hModUser = "";
    String hEngName =  "";
    String hIssueDate =  "";
    String hNameChgDate = "";

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                comcr.hCallBatchSeqno = "no-call";
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
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

            hCreateDate   = sysDate;
//            h_batchno       = args[0];
//            h_emboss_source = args[1];
//            h_emboss_reason = args[2];
//            h_super_id = args[3];
            showLogMessage("I", "", "h_batchno="+hBatchno+", h_emboss_source="+hEmbossSource+", h_emboss_reason="+hEmbossReason+", h_super_id="+hSuperId);
//            val = check_process(1, "DbcD020");

            if (val != 0) {
                hCallErrorDesc = "參數檔='y'   ";
                comcr.errRtn(hCallErrorDesc, "", comcr.hCallBatchSeqno);
            }

            process();

            checkProcess(2, "DbcD020");
            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            showLogMessage("I", "", hCallErrorDesc);

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    int checkProcess(int type, String progCode) throws Exception {
        String hWfValue = "";
        String hProgCode = "";

        if (debug == 1)
            showLogMessage("I", "", "  888 Check_p=[" + type + "]");

        hProgCode = "";
        hProgCode = progCode;
        if (type == 2) {
            daoTable   = " ptr_sys_parm";
            updateSQL  = " wf_value = 'NO',";
            updateSQL += " mod_user = ?,";
            updateSQL += " mod_time = sysdate";
            whereStr   = "where WF_PARM = 'CRD_BATCH'  ";
            whereStr  += "  and WF_KEY  = ? ";
            setString(1, hModUser);
            setString(2, hProgCode);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_ptr_sys_parm not found!", "", comcr.hCallBatchSeqno);
            }
            return (0);
        }
        /**** 檢核是否有程式在執行當中 ***/
        hWfValue = "";
        sqlCmd  = "select WF_VALUE ";
        sqlCmd += "  from ptr_sys_parm  ";
        sqlCmd += " where WF_PARM = 'CRD_BATCH'  ";
        sqlCmd += "   and WF_KEY  = ? ";
        setString(1, hProgCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWfValue = getValue("WF_VALUE");
        } else {
            hCallErrorDesc = "參數檔內無此程式代碼";
            comcr.errRtn(hCallErrorDesc, "", comcr.hCallBatchSeqno);
        }
        if (hWfValue.trim().equals("YES")) {
            return (1);
        } else {
            daoTable   = "ptr_sys_parm";
            updateSQL  = " wf_value     = 'YES',";
            updateSQL += " mod_user     = ?,";
            updateSQL += " mod_time     = sysdate";
            whereStr   = "where WF_PARM = 'CRD_BATCH'  ";
            whereStr  += "  and WF_KEY  = ? ";
            setString(1, hModUser);
            setString(2, hProgCode);
            updateTable();

            commitDataBase();
        }

        return (0);
    }

    /***********************************************************************/
    void process() throws Exception {

        getNcccBatchno();

        getChgBatchno();

        processDbcEmbossTmp(); /* 送製卡暫存檔 */
    }

    /***********************************************************************/
    public int getDbcCardType() throws Exception {
        selectSQL = " unit_code ";
        daoTable  = "dbc_card_type";
        whereStr  = "WHERE group_code    =  ? " 
                  + "  and card_type     =  ? "
                  + "  and digital_flag  =  ? ";
        setString(1, hDcesGroupCode);
        setString(2, hDcesCardType);
        setString(3, hDcesDigitalFlag);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_dbc_card_type  error!!=" + hDcesCardType;
            comcr.errRtn(err1, hDcesGroupCode, comcr.hCallBatchSeqno);
        }

        hDcesUnitCode = getValue("unit_code");

        return (0);
    }

    // ************************************************************************
    public int getCrdItemUnit() throws Exception {
        selectSQL = " ic_flag , electronic_code ,service_code ";
        daoTable  = "crd_item_unit";
        whereStr  = "WHERE unit_code     =  ? ";
        setString(1, hDcesUnitCode);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_crd_item_unit  error!!=" + hDcesCardType;
            comcr.errRtn(err1, hDcesUnitCode, comcr.hCallBatchSeqno);
        }

        hDcesIcFlag = getValue("ic_flag");        
        hDcesElectronicCode = getValue("electronic_code");
        hDcesServiceCode = getValue("service_code");

        return (0);
    }

    // ************************************************************************
    void getNcccBatchno() throws Exception {
        String tmpBatchno = "";
        long hTmpNo = 0;

        hTmpDate    = "";
        sqlCmd = "select to_char(sysdate,'yymmdd') h_tmp_date ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTmpDate = getValue("h_tmp_date");
        }

        sqlCmd = "select distinct(max(batchno)) h_new_batchno ";
        sqlCmd += " from dbc_emboss  ";
        sqlCmd += "where substr(batchno,1,6) = ?   ";
        sqlCmd += "  and nccc_type           = '3' ";
        sqlCmd += "  and to_nccc_date        = ''  ";
        setString(1, hTmpDate);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hNewBatchno = getValue("h_new_batchno");
        }
        if (hNewBatchno.length() > 0) {
            sqlCmd = "select max(recno) h_new_recno ";
            sqlCmd += " from dbc_emboss  ";
            sqlCmd += "where batchno = ? ";
            setString(1, hNewBatchno);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hNewRecno = getValueLong("h_new_recno");
            }
            return;
        } else {
            sqlCmd = "select distinct(max(batchno)) h_new_batchno ";
            sqlCmd += " from dbc_emboss  ";
            sqlCmd += "where substr(batchno,1,6) = ? ";
            setString(1, hTmpDate);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hNewBatchno = getValue("h_new_batchno");
            }
            if (hNewBatchno.length() > 0) {
                hTmpNo    = comcr.str2long(hNewBatchno) + 1;
                tmpBatchno = String.format("%08d", hTmpNo);
            } else {
                hTmpNo = 1;
                tmpBatchno = String.format("%s%02d", hTmpDate, hTmpNo);
            }
            hNewBatchno = tmpBatchno;
        }
if(debug == 1) showLogMessage("I", "", " 888 NCCC Batchno=[" + hNewBatchno + "]");

        return;
    }
    /***********************************************************************/
    void getChgBatchno() throws Exception {
        String tmpBatchno = "";
        int hTmpNo = 0;

        hTmpDate    = "";
        hChgBatchno = "";
        hChgRecno   = 0;
        sqlCmd = "select to_char(sysdate,'yymmdd') h_tmp_date ";
        sqlCmd += "  from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTmpDate = getValue("h_tmp_date");
        }

        sqlCmd = "select distinct(max(batchno)) h_chg_batchno ";
        sqlCmd += " from dbc_emboss  ";
        sqlCmd += "where substr(batchno,1,6) = ?  ";
        sqlCmd += "  and nccc_type           = '2'  ";
        sqlCmd += "  and to_nccc_date        = '' ";
        setString(1, hTmpDate);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hChgBatchno = getValue("h_chg_batchno");
        }
        if (hChgBatchno.length() > 0) {
            sqlCmd = "select max(recno) h_chg_recno ";
            sqlCmd += " from dbc_emboss  ";
            sqlCmd += "where batchno = ? ";
            setString(1, hChgBatchno);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hChgRecno = getValueInt("h_chg_recno");
            }
            return;
        } else {
            sqlCmd =  "select distinct(max(batchno)) h_chg_batchno ";
            sqlCmd += "from dbc_emboss  ";
            sqlCmd += "where substr(batchno,1,6) = ? ";
            setString(1, hTmpDate);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hChgBatchno = getValue("h_chg_batchno");
            }
            if (hChgBatchno.length() > 0) {
                hTmpNo    = comcr.str2int(hChgBatchno) + 1;
                tmpBatchno = String.format("%08d", hTmpNo);
            } else {
                hTmpNo = 1;
                tmpBatchno = String.format("%s%02d", hTmpDate, hTmpNo);
            }
            hChgBatchno = tmpBatchno;
        }
if(debug == 1) showLogMessage("I", "", " 888 chg Batchno=[" + hNewBatchno + "]");

        return;
    }
    /***********************************************************************/
    void processDbcEmbossTmp() throws Exception {
        int rtn;        

if(debug == 1) showLogMessage("I", "", " 888 dbc_emboss_tmp rsn = " + hEmbossReason + " , " + hBatchno);

        sqlCmd = "select ";
        sqlCmd += "a.batchno,";
        sqlCmd += "a.recno,";
        sqlCmd += "a.credit_lmt,";
        sqlCmd += "a.emboss_source,";
        sqlCmd += "a.emboss_reason,";
        sqlCmd += "a.resend_note,";
        sqlCmd += "a.to_nccc_code,";
        sqlCmd += "a.card_type,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.unit_code,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.old_card_no,";
        sqlCmd += "a.change_reason,";
        sqlCmd += "a.status_code,";
        sqlCmd += "a.reason_code,";
        sqlCmd += "a.member_note,";
        sqlCmd += "a.apply_id,";
        sqlCmd += "a.apply_id_code,";
        sqlCmd += "a.pm_id,";
        sqlCmd += "a.pm_id_code,";
        sqlCmd += "a.major_card_no,";
        sqlCmd += "a.major_valid_fm,";
        sqlCmd += "a.major_valid_to,";
        sqlCmd += "a.group_code,";
        sqlCmd += "a.source_code,";
        sqlCmd += "a.corp_no,";
        sqlCmd += "a.corp_no_code,";
        sqlCmd += "a.chi_name,";
        sqlCmd += "a.eng_name,";
        sqlCmd += "a.birthday,";
        sqlCmd += "a.force_flag,";
        sqlCmd += "a.business_code,";
        sqlCmd += "a.valid_fm,";
        sqlCmd += "a.valid_to,";
        sqlCmd += "a.mail_type,";
        sqlCmd += "a.emboss_4th_data,";
        sqlCmd += "a.chg_addr_flag,";
        sqlCmd += "a.vip,";
        sqlCmd += "a.reg_bank_no,";
        sqlCmd += "a.risk_bank_no,";
        sqlCmd += "a.pvv,";
        sqlCmd += "a.cvv,";
        sqlCmd += "a.cvv2,";
        sqlCmd += "a.pvki,";
        sqlCmd += "a.pin_block,";
        sqlCmd += "a.voice_passwd,";
        sqlCmd += "a.old_beg_date,";
        sqlCmd += "a.old_end_date,";
        sqlCmd += "a.fee_code,";
        sqlCmd += "a.standard_fee,";
        sqlCmd += "a.annual_fee,";
        sqlCmd += "a.fee_reason_code,";
        sqlCmd += "a.nccc_type,";
        sqlCmd += "a.ic_flag,";
        sqlCmd += "a.bin_no,";
        sqlCmd += "a.branch,";
        sqlCmd += "a.mail_attach1,";
        sqlCmd += "a.mail_attach2,";
        sqlCmd += "a.apply_source,";
        sqlCmd += "a.service_code,";
        sqlCmd += "a.apply_ibm_id_code,";
        sqlCmd += "a.pm_ibm_id_code,";
        sqlCmd += "decode(a.redo_flag, '', 'N', a.redo_flag) h_redo_flag,";
        sqlCmd += "a.crt_user,";
        sqlCmd += "a.third_rsn,";
        sqlCmd += "a.reissue_code,";
        sqlCmd += "a.receipt_branch,";
        sqlCmd += "a.receipt_remark,";
        sqlCmd += "a.card_ref_num,";
        sqlCmd += "a.tsc_autoload_flag,";
        sqlCmd += "decode(a.digital_flag, '', 'N', a.digital_flag) digital_flag,  ";
        sqlCmd += "a.mail_branch, ";
        sqlCmd += "a.act_no ";
        sqlCmd += "from dbc_emboss_tmp a,dbp_acct_type b ";
        sqlCmd += "where a.batchno        like ? ";
        sqlCmd += "  and a.emboss_source  like ? ";
        sqlCmd += "  and a.emboss_reason  like ? ";
        sqlCmd += "  and a.emboss_date   = '' ";
        sqlCmd += "  and a.nccc_batchno   = '' ";
        sqlCmd += "  and a.confirm_date  <> '' ";
        sqlCmd += "  and a.card_no       <> '' ";
        sqlCmd += "  and a.error_code     = '' ";
        sqlCmd += "  and b.acct_type      = a.acct_type ";
        sqlCmd += "order by a.acct_type, a.card_type, decode(a.unit_code, '', '0000', a.unit_code), a.card_no ";
        setString(1, hBatchno + "%");
        setString(2, hEmbossSource + "%");
        setString(3, hEmbossReason + "%");
        int recordCnt = selectTable();
        if (debug == 1)
            showLogMessage("I", "", "  888 Get tmp = [" + recordCnt + "]" + hEmbossSource);

        for (int i = 0; i < recordCnt; i++) {
            hDcesSourceBatchno    = getValue("batchno", i);
            hDcesSourceRecno      = getValueDouble("recno", i);
            hDcesCreditLmt        = getValue("credit_lmt", i);
            hDcesEmbossSource     = getValue("emboss_source", i);
            hDcesEmbossReason     = getValue("emboss_reason", i);
            hDcesResendNote       = getValue("resend_note", i);
            hDcesToNcccCode      = getValue("to_nccc_code", i);
            hDcesCardType         = getValue("card_type", i);
            hDcesAcctType         = getValue("acct_type", i);
            hDcesAcctKey          = getValue("acct_key", i);
            hDcesUnitCode         = getValue("unit_code", i);
            hDcesCardNo           = getValue("card_no", i);
            hDcesOldCardNo       = getValue("old_card_no", i);
            hDcesChangeReason     = getValue("change_reason", i);
            hDcesStatusCode       = getValue("status_code", i);
            hDcesReasonCode       = getValue("reason_code", i);
            hDcesMemberNote       = getValue("member_note", i);
            hDcesApplyId          = getValue("apply_id", i);
            hDcesApplyIdCode     = getValue("apply_id_code", i);
            hDcesPmId             = getValue("pm_id", i);
            hDcesPmIdCode        = getValue("pm_id_code", i);
            hDcesMajorCardNo     = getValue("major_card_no", i);
            hDcesMajorValidFm    = getValue("major_valid_fm", i);
            hDcesMajorValidTo    = getValue("major_valid_to", i);
            hDcesGroupCode        = getValue("group_code", i);
            hDcesSourceCode       = getValue("source_code", i);
            hDcesCorpNo           = getValue("corp_no", i);
            hDcesCorpNoCode      = getValue("corp_no_code", i);
            hDcesChiName          = getValue("chi_name", i);
            hDcesEngName          = getValue("eng_name", i);
            hDcesBirthday          = getValue("birthday", i);
            hDcesForceFlag        = getValue("force_flag", i);
            hDcesBusinessCode     = getValue("business_code", i);
            hDcesValidFm          = getValue("valid_fm", i);
            hDcesValidTo          = getValue("valid_to", i);
            hDcesMailType         = getValue("mail_type", i);
            hDcesEmboss4thData   = getValue("emboss_4th_data", i);
            hDcesChgAddrFlag     = getValue("chg_addr_flag", i);
            hDcesVip               = getValue("vip", i);
            hDcesRegBankNo       = getValue("reg_bank_no", i);
            hDcesRiskBankNo      = getValue("risk_bank_no", i);
            hDcesPvv               = getValue("pvv", i);
            hDcesCvv               = getValue("cvv", i);
            hDcesCvv2              = getValue("cvv2", i);
            hDcesPvki              = getValue("pvki", i);
            hDcesPinBlock         = getValue("pin_block", i);
            hDcesVoiceNum      = getValue("voice_passwd", i);
            hDcesOldBegDate      = getValue("old_beg_date", i);
            hDcesOldEndDate      = getValue("old_end_date", i);
            hDcesFeeCode          = getValue("fee_code", i);
            hDcesStandardFee      = getValue("standard_fee", i);
            hDcesAnnualFee        = getValue("annual_fee", i);
            hDcesFeeReasonCode   = getValue("fee_reason_code", i);
            hDcesNcccType         = getValue("nccc_type", i);
            hDcesIcFlag           = getValue("ic_flag", i);
            hDcesBinNo            = getValue("bin_no", i);
            hDcesBranch            = getValue("branch", i);
            hDcesMailAttach1      = getValue("mail_attach1", i);
            hDcesMailAttach2      = getValue("mail_attach2", i);
            hComboIndicator        = "Y";
            hDcesApplySource      = getValue("apply_source", i);
            hDcesServiceCode      = getValue("service_code", i);
            hDcesApplyIbmIdCode = getValue("apply_ibm_id_code", i);
            hDcesPmIbmIdCode    = getValue("pm_ibm_id_code", i);
            hRedoFlag              = getValue("h_redo_flag", i);
            hDcesCreateId         = getValue("crt_user", i);
            hDcesThirdRsn         = getValue("third_rsn", i);
            hDcesReissueCode      = getValue("reissue_code", i);
            hDcesReceiptBranch    = getValue("receipt_branch", i);
            hDcesReceiptRemark    = getValue("receipt_remark", i);
            hDcesDigitalFlag      = getValue("digital_flag"  , i);
            hDcesCardRefNum      = getValue("card_ref_num"  , i);
            hDcesTscAutoloadFlag = getValue("tsc_autoload_flag",i);
            hDcesMailBranch      = getValue("mail_branch", i);
            hDcesActNo           = getValue("act_no", i);
        
            /*****************************************************
             * 轉大寫
             ******************************************************/
            hDcesEngName = comc.commAdjEngname(hDcesEngName);
            showLogMessage("I", "", "  888 Card = [" + hDcesCardNo + "]" + hDcesApplyId + " , " + hDcesApplyIdCode);
            totCnt ++;
            totalCnt ++;
            
            String tmpDigitsCode = "";
            tmpDigitsCode = comc.getSubString(hDcesActNo, 4, 7);
            
            if(hDcesGroupCode.equals("2200") && tmpDigitsCode.equals("988")) {
            	hDcesDigitalFlag = "Y";
            }
            else {
            	hDcesDigitalFlag = "N";
            }
            
            getDbcCardType();
            
            getCrdItemUnit();

            if (hDcesNcccType.equals("2")) {
                hChgRecno++;
                if(hChgBatchno.equals(hNewBatchno)) {
                	hChgBatchno = String.format("%08d", Integer.parseInt(hChgBatchno.trim()) + 1);
                }

                hNcccBatchno = hChgBatchno;
                hNcccRecno = hChgRecno;
            } else {
                hNewRecno++;
                if(hNewBatchno.equals(hChgBatchno)) {
                	hNewBatchno = String.format("%08d", Integer.parseInt(hNewBatchno.trim()) + 1);
                }
                
                hNcccBatchno = hNewBatchno;
                hDcesNcccType = "3";
                hNcccRecno = hNewRecno;
            }
           
            getDbcCard();

            /****************************************
             * 正卡combo卡申請第三軌資料
             ****************************************/
            if (hComboIndicator.equals("Y")) {
                getComboAcctNo();
                hDcesDiffCode = "1";
            }
            getIdnoData();
            getMailAddr();
            rtn = 0;
            if (!hDcesEmbossSource.equals("3") && !hDcesEmbossSource.equals("4")) {
                rtn = checkActDuplicate();
            } else {
                rtn = checkActDuplicateCrd();
            }
            /**********************************************
             * 緊急卡/重送件不需申請第三軌
             **********************************************/
            if (rtn == 0) {
                insertDbcEmboss();
                if ((hComboIndicator.equals("Y")) && (hDcesOnlineMark.equals("0"))) {
                    insertDbcDebit();
                }
                updateOldCard();
                updateDbcEmbossTmp();
            } else {
                processErrFlag();
            }
        }
    }

    /***********************************************************************/
    void getDbcCard() throws Exception {
        String hRelation = "";        

        sqlCmd =  "select major_relation, crt_bank_no,vd_bank_no, card_ref_num, mail_branch ";
        sqlCmd += "from dbc_card  ";
        sqlCmd += "where card_no = ? ";
        setString(1, hDcesOldCardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dbc_card not found!", hDcesOldCardNo, comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hRelation = getValue("major_relation");
            hDecsOldCrtBankNo = getValue("crt_bank_no");
            hDecsOldVdBankNo = getValue("vd_bank_no");
            hDecsOldCardRefNum = getValue("card_ref_num");
            hDecsOldMailBranch = getValue("mail_branch");
        }
        hDcesRelWithPm = hRelation;
        return;
    }

    /***********************************************************************/
    void getComboAcctNo() throws Exception {
        hComboAcctNo = "";
        sqlCmd  = "select acct_no ";
        sqlCmd += "  from dbc_card  ";
        sqlCmd += " where card_no = ? ";
        setString(1, hDcesOldCardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("get_combo_acct_no() not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hComboAcctNo = getValue("acct_no");
        }
        hDcesActNo = hComboAcctNo;
        return;
    }

    /***********************************************************************/
    void getIdnoData() throws Exception {
        String hBirthday = "";
        String hChiName = "";
        String hNation = "";
        String hSex = "";
        String hMarriage = "";
        String hEducation = "";
        String hBussinessCode = "";

        hBirthday = "";
        hChiName = "";
        hNation = "";
        hSex = "";
        hMarriage = "";
        hEducation = "";
        hBussinessCode = "";
        hNameChgDate = "";

        sqlCmd = "select birthday,";
        sqlCmd += "chi_name,";
        sqlCmd += "nation,";
        sqlCmd += "sex,";
        sqlCmd += "marriage,";
        sqlCmd += "business_code,";
        sqlCmd += "education, ";
        sqlCmd += "name_chg_date ";
        sqlCmd += " from dbc_idno  ";
        sqlCmd += "where id_no      = ?  ";
        sqlCmd += "  and id_no_code = ? ";
        setString(1, hDcesApplyId);
        setString(2, hDcesApplyIdCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dbc_idno not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBirthday       = getValue("birthday");
            hChiName       = getValue("chi_name");
            hNation         = getValue("nation");
            hSex            = getValue("sex");
            hMarriage       = getValue("marriage");
            hBussinessCode = getValue("business_code");
            hEducation      = getValue("education");
            hNameChgDate  = getValue("name_chg_date");
        }
        hDcesBirthday      = hBirthday;
        hDcesChiName      = hChiName;
        hDcesNation        = hNation;
        hDcesSex           = hSex;
        hDcesMarriage      = hMarriage;
        hDcesEducation     = hEducation;
        hDcesBusinessCode = hBussinessCode;
        return;
    }

    /***********************************************************************/
    void getMailAddr() throws Exception {
        sqlCmd = "select bill_sending_zip,";
        sqlCmd += "bill_sending_addr1,";
        sqlCmd += "bill_sending_addr2,";
        sqlCmd += "bill_sending_addr3,";
        sqlCmd += "bill_sending_addr4,";
        sqlCmd += "bill_sending_addr5 ";
        sqlCmd += " from dba_acno  ";
        sqlCmd += "where acct_type = ?  ";
//        sqlCmd += "  and acct_key  = ?  ";
        sqlCmd += "  and acct_no   = ? ";
        setString(1, hDcesAcctType);
//        setString(2, h_dces_acct_key);
        setString(2, hDcesActNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dba_acno not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDcesMailZip   = getValue("bill_sending_zip");
            hDcesMailAddr1 = getValue("bill_sending_addr1");
            hDcesMailAddr2 = getValue("bill_sending_addr2");
            hDcesMailAddr3 = getValue("bill_sending_addr3");
            hDcesMailAddr4 = getValue("bill_sending_addr4");
            hDcesMailAddr5 = getValue("bill_sending_addr5");
        }

        return;
    }

    /***********************************************************************/
    int checkActDuplicate() throws Exception {
        int recCount = 0;

        sqlCmd = "select count(*) rec_count ";
        sqlCmd += " from dbc_card  ";
        sqlCmd += "where acct_no      = ?  ";
        sqlCmd += "  and current_code = '0' ";
        setString(1, hDcesActNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            recCount = getValueInt("rec_count");
        }

        if (recCount > 0)
            return (1);
        else {
            recCount = 0;
            sqlCmd = "select count(*) rec_count ";
            sqlCmd += " from crd_card  ";
            sqlCmd += "where combo_acct_no = ?  ";
            sqlCmd += "and current_code = '0' ";
            setString(1, hDcesActNo);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                recCount = getValueInt("rec_count");
            }
            if (recCount > 0)
                return (1);
            else
                return (0);
        }
    }

    /***********************************************************************/
    int checkActDuplicateCrd() throws Exception {
        int recCount = 0;

        sqlCmd  = "select count(*) rec_count ";
        sqlCmd += "  from crd_card  ";
        sqlCmd += " where combo_acct_no = ?  ";
        sqlCmd += "   and current_code  = '0' ";
        setString(1, hDcesActNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            recCount = getValueInt("rec_count");
        }

        if (recCount > 0)
            return (1);
        else
            return (0);
    }
    /***********************************************************************/
    void insertDbcEmboss() throws Exception {
        int valMon = 0;
        int tempAmt = 0;
        String hEngChgFlag = "";

        hDcesSupFlag = "";
        hDcesOnlineMark = "0";
        hDcesAuthCreditLmt = hDcesCreditLmt;
        /*****************************************************
         * 重製之緊急補發卡,online_mark='3'(2001/10/07)
         *****************************************************/
        if ((hDcesEmbossSource.equals("5")) && (hDcesReasonCode.equals("3"))) {
            hDcesOnlineMark = "2";
        }
        if (hDcesMailType.length() == 0) {
            hDcesMailType = "4";
            if ((hDcesEmbossSource.equals("3")) || (hDcesEmbossSource.equals("4"))) {
                hDcesMailType = "1";    /* 普掛 */
            }
        }
        
if(debug==1)
   showLogMessage("I", "", " 8888  act="+ hDcesActNo + "," + hDcesGroupCode);
  
        if (hDcesPmId.equals(hDcesApplyId)) {
            hDcesSupFlag = "0";
        } else {
            hDcesSupFlag = "1";
        }

        /**********************************************************
         * 效期起日小於系統月份,以系統月份一日為主,overwrite crd_emboss.valid_fm (2002/02/19)
         **********************************************************/
        valMon = 0;
        sqlCmd = "select 1 val_mon ";
        sqlCmd += " from dual  ";
        sqlCmd += "where to_char(sysdate,'yyyymm') > substr(?,1,6) ";
        setString(1, hDcesValidFm);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            valMon = getValueInt("val_mon");
        }

        if (valMon > 0) {
            hDcesValidFm = String.format("%6.6s01", sysDate);
        }

        recCnt++;
        if (hDcesSourceCode.length() == 0) {
            hGroupAbbrCode = "";
            sqlCmd = "select decode(GROUP_ABBR_CODE,'',' ',GROUP_ABBR_CODE) h_group_abbr_code ";
            sqlCmd += " from ptr_group_code  ";
            sqlCmd += "where group_code = ? ";
            setString(1, hDcesGroupCode.length() == 0 ? "0000" : hDcesGroupCode);
            recordCnt = selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_ptr_group_code not found!", "", comcr.hCallBatchSeqno);
            }
            if (recordCnt > 0) {
                hGroupAbbrCode = getValue("h_group_abbr_code");
            }
            hDcesSourceCode = String.format("%2.2s%4.4s", hGroupAbbrCode, hDcesGroupCode);
        }

        hEngChgFlag = "Y";
        hEngName = "";
        hIssueDate = "";

        sqlCmd  = " select eng_name, ";
        sqlCmd += "        issue_date ";
        sqlCmd += "   from dbc_card ";
        sqlCmd += "  where  card_no = ? ";
        setString(1, hDcesOldCardNo);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dbc_card not found!", hDcesOldCardNo, hCallBatchSeqno);
        }
        hEngName   = getValue("eng_name");
        hIssueDate = getValue("issue_date");

        if (hEngName.equals(hDcesEngName)) {
            hEngChgFlag = "N";
        }

        if (hEngChgFlag.equals("N")) {
            if (hNameChgDate.length() > 0) {
                if (hNameChgDate.equals(hIssueDate)) {
                    hDcesEngName = "";
                }
            }
        }
         
        setValue("batchno"            , hNcccBatchno);
        setValueDouble("recno"        , hNcccRecno);
        setValue("crt_date"           , sysDate);
        setValue("mod_user"           , hModUser);
        setValue("mod_time"           , sysDate + sysTime);
        setValue("mod_pgm"            , prgmId);
        setValueDouble("seqno"        , hDcesSeqno);
        setValueDouble("source_recno" , hDcesSourceRecno);
        setValue("aps_recno"          , hDcesApsRecno);
        setValue("service_year"       , hDcesServiceYear);
        setValue("salary"             , hDcesSalary);
        setValue("value"              , hDcesValue);
        setValue("credit_lmt"         , hDcesCreditLmt);
        setValue("standard_fee"       , hDcesStandardFee);
        setValue("annual_fee"         , hDcesAnnualFee);
        setValue("auth_credit_lmt"    , hDcesAuthCreditLmt);
        setValue("emboss_source"      , hDcesEmbossSource);
        setValue("emboss_reason"      , hDcesEmbossReason);
        setValue("resend_note"        , hDcesResendNote);
        setValue("source_batchno"     , hDcesSourceBatchno);
        setValue("aps_batchno"        , hDcesApsBatchno);
        setValue("to_nccc_code"       , hDcesToNcccCode);
        setValue("card_type"          , hDcesCardType);
        setValue("acct_type"          , hDcesAcctType);
        setValue("acct_key"           , hDcesAcctKey);
        setValue("unit_code"          , hDcesUnitCode);
        setValue("card_no"            , hDcesCardNo);
        setValue("major_card_no"      , hDcesMajorCardNo);
        setValue("major_valid_fm"     , hDcesMajorValidFm);
        setValue("major_valid_to"     , hDcesMajorValidTo);
        setValue("major_chg_flag"     , hDcesMajorChgFlag);
        setValue("old_card_no"        , hDcesOldCardNo);
        setValue("change_reason"      , hDcesChangeReason);
        setValue("status_code"        , hDcesStatusCode);
        setValue("reason_code"        , hDcesReasonCode);
        setValue("member_note"        , hDcesMemberNote);
        setValue("apply_id"           , hDcesApplyId);
        setValue("apply_id_code"      , hDcesApplyIdCode);
        setValue("pm_id"              , hDcesPmId);
        setValue("pm_id_code"         , hDcesPmIdCode);
        setValue("group_code"         , hDcesGroupCode);
        setValue("source_code"        , hDcesSourceCode);
        setValue("corp_no"            , hDcesCorpNo);
        setValue("corp_no_code"       , hDcesCorpNoCode);
        setValue("corp_act_flag"      , hDcesCorpActFlag);
        setValue("corp_assure_flag"   , hDcesCorpAssureFlag);
        setValue("reg_bank_no"        , hDcesRegBankNo);
        setValue("risk_bank_no"       , hDcesRiskBankNo);
        setValue("chi_name"           , hDcesChiName);
        setValue("eng_name"           , hDcesEngName);
        setValue("birthday"           , hDcesBirthday);
        setValue("marriage"           , hDcesMarriage);
        setValue("rel_with_pm"        , hDcesRelWithPm);
        setValue("education"          , hDcesEducation);
        setValue("nation"             , hDcesNation);
        setValue("mail_zip"           , hDcesMailZip);
        setValue("mail_addr1"         , hDcesMailAddr1);
        setValue("mail_addr2"         , hDcesMailAddr2);
        setValue("mail_addr3"         , hDcesMailAddr3);
        setValue("mail_addr4"         , hDcesMailAddr4);
        setValue("mail_addr5"         , hDcesMailAddr5);
        setValue("resident_zip"       , hDcesResidentZip);
        setValue("resident_addr1"     , hDcesResidentAddr1);
        setValue("resident_addr2"     , hDcesResidentAddr2);
        setValue("resident_addr3"     , hDcesResidentAddr3);
        setValue("resident_addr4"     , hDcesResidentAddr4);
        setValue("resident_addr5"     , hDcesResidentAddr5);
        setValue("company_name"       , hDcesCompanyName);
        setValue("job_position"       , hDcesJobPosition);
        setValue("home_area_code1"    , hDcesHomeAreaCode1);
        setValue("home_tel_no1"       , hDcesHomeTelNo1);
        setValue("home_tel_ext1"      , hDcesHomeTelExt1);
        setValue("home_area_code2"    , hDcesHomeAreaCode2);
        setValue("home_tel_no2"       , hDcesHomeTelNo2);
        setValue("home_tel_ext2"      , hDcesHomeTelExt2);
        setValue("office_area_code1"  , hDcesOfficeAreaCode1);
        setValue("office_tel_no1"     , hDcesOfficeTelNo1);
        setValue("office_tel_ext1"    , hDcesOfficeTelExt1);
        setValue("office_area_code2"  , hDcesOfficeAreaCode2);
        setValue("office_tel_no2"     , hDcesOfficeTelNo2);
        setValue("office_tel_ext2"    , hDcesOfficeTelExt2);
        setValue("e_mail_addr"        , hDcesEMailAddr);
        setValue("cellar_phone"       , hDcesCellarPhone);
        setValue("act_no"             , hDcesActNo);
        setValue("vip"                , hDcesVip);
        setValue("fee_code"           , hDcesFeeCode);
        setValue("final_fee_code"     , hDcesFinalFeeCode);
        setValue("force_flag"         , hDcesForceFlag);
        setValue("business_code"      , hDcesBusinessCode);
        setValue("introduce_no"       , hDcesIntroduceNo);
        setValue("valid_fm"           , hDcesValidFm);
        setValue("valid_to"           , hDcesValidTo);
        setValue("sex"                , hDcesSex);
        setValue("accept_dm"          , hDcesAcceptDm);
        setValue("apply_no"           , hDcesApplyNo);
        setValue("cardcat"            , hDcesCardcat);
        setValue("mail_type"          , hDcesMailType);
        setValue("introduce_id"       , hDcesIntroduceId);
        setValue("introduce_name"     , hDcesIntroduceName);
        setValue("salary_code"        , hDcesSalaryCode);
        setValue("student"            , hDcesStudent);
        setValue("police_no1"         , hDcesPoliceNo1);
        setValue("police_no2"         , hDcesPoliceNo2);
        setValue("police_no3"         , hDcesPoliceNo3);
        setValue("pm_cash"            , hDcesPmCash);
        setValue("sup_cash"           , hDcesSupCash);
        setValue("online_mark"        , hDcesOnlineMark);
        setValue("error_code"         , hDcesErrorCode);
        setValue("reject_code"        , hDcesRejectCode);
        setValue("emboss_4th_data"    , hDcesEmboss4thData);
        setValue("member_id"          , hDcesMemberId);
        setValue("pm_birthday"        , hDcesPmBirthday);
        setValue("sup_birthday"       , hDcesSupBirthday);
        setValue("fee_reason_code"    , hDcesFeeReasonCode);
        setValue("chg_addr_flag"      , hDcesChgAddrFlag);
        setValue("pvv"                , hDcesPvv);
        setValue("cvv"                , hDcesCvv);
        setValue("cvv2"               , hDcesCvv2);
        setValue("pvki"               , hDcesPvki);
        setValue("pin_block"          , hDcesPinBlock);
        setValue("old_beg_date"       , hDcesOldBegDate);
        setValue("old_end_date"       , hDcesOldEndDate);
        setValue("service_code"       , hDcesServiceCode);
        setValue("sup_flag"           , hDcesSupFlag);
        setValue("credit_flag"        , hDcesCreditFlag);
        setValue("comm_flag"          , hDcesCommFlag);
        setValue("resident_no"        , hDcesResidentNo);
        setValueInt("other_cntry_code", hDcesOtherCntryCode);
        setValue("passport_no"        , hDcesPassportNo);
        setValue("diff_code"          , hDcesDiffCode);
        setValue("staff_flag"         , hDcesStaffFlag);
        setValue("stmt_cycle"         , hDcesStmtCycle);
        setValue("nccc_type"          , hDcesNcccType);
        setValue("org_indiv_crd_lmt"  , hDcesOrgIndivCrdLmt);
        setValue("indiv_crd_lmt"      , hDcesIndivCrdLmt);
        setValue("org_cash_lmt"       , hDcesOrgCashLmt);
        setValue("cash_lmt"           , hDcesCashLmt);
        setValue("ic_flag"            , hDcesIcFlag);       
        setValue("bin_no"             , hDcesBinNo);
        setValue("branch"             , hDcesBranch);
        setValue("mail_attach1"       , hDcesMailAttach1);
        setValue("mail_attach2"       , hDcesMailAttach2);
        setValue("ic_indicator"       , hDcesIcIndicator);
        setValue("ic_cvv"             , hDcesIcCvv);
        setValue("ic_pin"             , hDcesIcPin);
        setValue("deriv_key"          , hDcesDerivKey);
        setValue("l_offln_lmt"        , hDcesLOfflnLmt);
        setValue("u_offln_lmt"        , hDcesUOfflnLmt);
        
        if(hDcesApplySource.length() == 0) {
        	if(hDcesEmbossSource.equals("5")) {
        		setValue("apply_source"       , "S");
        	}
        	else {
        		setValue("apply_source"       , "T");
        	}
        }
        else {
        	setValue("apply_source"       , hDcesApplySource);
        }
                
        setValue("apply_ibm_id_code"  , hDcesApplyIbmIdCode);
        setValue("pm_ibm_id_code"     , hDcesPmIbmIdCode);
        setValue("create_id"          , hDcesCreateId);
        setValue("third_rsn"          , hDcesThirdRsn);
        setValue("reissue_code"       , hDcesReissueCode);
        setValue("receipt_branch"     , hDcesReceiptBranch);
        setValue("receipt_remark"     , hDcesReceiptRemark);
        setValue("digital_flag"       , hDcesDigitalFlag);
        setValue("crt_bank_no"        , hDecsOldCrtBankNo);
        setValue("vd_bank_no"         , hDecsOldVdBankNo);
        setValue("card_ref_num"       , hDecsOldCardRefNum);
        setValue("tsc_autoload_flag"  , hDcesTscAutoloadFlag);
        setValue("mail_branch"        , hDecsOldMailBranch);
        setValue("electronic_code"    , hDcesElectronicCode);
        daoTable = "dbc_emboss";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_dbc_emboss duplicate!", "", comcr.hCallBatchSeqno);
        }

        /* 毀損重製費 */
        if (hRedoFlag.equals("Y")) {
            tempAmt = 0;
            sqlCmd = "select decode(cast(? as varchar(10)),'0',nvl(NORMAL_MAJOR,0),nvl(NORMAL_SUB,0) ) ";
            sqlCmd += " from dba_lostgrp  ";
            sqlCmd += "where acct_type  = ?  ";
            sqlCmd += "  and group_code = ?  ";
            sqlCmd += "  and lost_code  = 'RT' ";
            setString(1, hDcesSupFlag);
            setString(2, hDcesAcctType);
            setString(3, hDcesGroupCode);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                tempAmt = getValueInt("temp_amt");
            }

            if (tempAmt > 0) {
                setValue("bill_type", "OSSG");
                setValue("transaction_code", "RF");
                setValue("add_item", "RF");
                setValue("real_card_no", hDcesOldCardNo);
                setValueInt("seq_no", 1);
                setValue("acct_holder_id", hDcesApplyId);
                setValueInt("destination_amt", tempAmt);
                setValue("destination_currency", "901");
                setValue("purchase_date", sysDate);
                setValue("chi_desc", "毀損重製費");
                setValue("bill_desc", "毀損重製費");
                setValue("dept_flag", "");
                setValue("confirm_flag", "Y");
                setValue("post_flag", "N");
                setValue("mod_pgm", javaProgram);
                setValue("mod_time", sysDate + sysTime);
                daoTable = "dbb_othexp";
                insertTable();
                if (dupRecord.equals("Y")) {
                    comcr.errRtn("insert_dbb_othexp duplicate!", "", comcr.hCallBatchSeqno);
                }
            }
        }
    }

    /***********************************************************************/
    void insertDbcDebit() throws Exception {
        String hRowid = "";
        String hTransType = "";
        String hSupFlag = "";

        hRowid = "";
        hTransType = "";
        hSupFlag = "";
        hSupFlag = hDcesSupFlag;

        switch (Integer.parseInt(hDcesEmbossSource)) {
        case 1:
            hTransType = "01";
            /* 新製卡 */ break;
        case 2:
            hTransType = "08";
            /* 普昇金 */ break;
        case 3:
        case 4:
            hTransType = "08";
            /* 續卡 */ break;
        case 5:
            if (hDcesEmbossReason.equals("1"))
                hTransType = "02";/* 掛失重製 */
            if (hDcesEmbossReason.equals("2"))
                hTransType = "07";/* 毀損重製 */
            if (hDcesEmbossReason.equals("3"))
                hTransType = "04";/* 偽卡重製 */
            break;

        }
        sqlCmd = "select rowid  as rowid ";
        sqlCmd += "  from dbc_debit  ";
        sqlCmd += " where card_no = ? ";
        setString(1, hDcesCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hRowid = getValue("rowid");

            daoTable = "dbc_debit";
            updateSQL = "apply_date = to_char(sysdate,'yyyymmdd'),";
            updateSQL += " birthday  = ?,";
            updateSQL += " batchno  = ?,";
            updateSQL += " recno   = ?,";
            updateSQL += " trans_type = ?,";
            updateSQL += " old_card_no = ?,";
            updateSQL += " saving_actno = ?,";
            updateSQL += " third_data = '',";
            updateSQL += " ic_pin  = '',";
            updateSQL += " to_ibm_date = '',";
            updateSQL += " rtn_ibm_date = '',";
            updateSQL += " rtn_code  = '',";
            updateSQL += " to_nccc_date = '',";
            updateSQL += " rtn_nccc_date = '',";
            updateSQL += " reject_code = '',";
            updateSQL += " fail_proc_code= '',";
            updateSQL += " fail_proc_date= '',";
            updateSQL += " emboss_code   = '',";
            updateSQL += " emboss_date   = '',";
            updateSQL += " end_ibm_date  = '',";
            updateSQL += " end_rtn_code  = '',";
            updateSQL += " end_rtn_date  = '',";
            updateSQL += " send_prn_date = '',";
            updateSQL += " bank_actno    = '',";
            updateSQL += " mod_user      = ?,";
            updateSQL += " mod_time      = sysdate,";
            updateSQL += " mod_pgm       = ?";
            whereStr = "where rowid    = ? ";
            setString(1, hDcesBirthday);
            setString(2, hNcccBatchno);
            setDouble(3, hNcccRecno);
            setString(4, hTransType);
            setString(5, hDcesOldCardNo);
            setString(6, hComboAcctNo);
             setString(7, hModUser);
            setString(8, prgmId);
            setRowId(9, hRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_dbc_debit not found!", "", comcr.hCallBatchSeqno);
            }
        } else {
            setValue("card_no", hDcesCardNo);
            setValue("apply_id", hDcesApplyId);
            setValue("apply_id_code", hDcesApplyIdCode);
            setValue("birthday", hDcesBirthday);
            setValue("old_card_no", hDcesOldCardNo);
            setValue("apply_date", sysDate);
            setValue("sup_flag", hSupFlag);
            setValue("pm_id", hDcesPmId);
            setValue("pm_id_code", hDcesPmIdCode);
            setValue("batchno", hNcccBatchno);
            setValueDouble("recno", hNcccRecno);
            setValue("trans_type", hTransType);
            setValue("saving_actno", hComboAcctNo);
            setValue("third_data", "");
            setValue("ic_pin", "");
            setValue("to_ibm_date", "");
           	setValue("rtn_ibm_date", "");
            setValue("rtn_code", "");
            setValue("fail_proc_code", "");
            setValue("fail_proc_date", "");
            setValue("to_nccc_date", "");
            setValue("rtn_nccc_date", "");
            setValue("reject_code", "");
            setValue("emboss_code", "");
            setValue("emboss_date", "");
            setValue("end_ibm_date", "");
            setValue("end_rtn_code", "");
            setValue("end_rtn_date", "");
            setValue("send_prn_date", "");
            setValue("bank_actno", "");
            setValue("crt_date", sysDate);
            setValue("mod_user", hModUser);
            setValue("mod_time", sysDate + sysTime);
            setValue("mod_pgm", prgmId);
            daoTable = "dbc_debit";
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_dbc_debit duplicate!", "", comcr.hCallBatchSeqno);
            }
        }
        return;
    }

    /***********************************************************************/
    void updateOldCard() throws Exception {
        if ((hDcesEmbossSource.equals("3")) || (hDcesEmbossSource.equals("4"))) {
            daoTable = "dbc_card";
            updateSQL = "change_status = '2',";
            updateSQL += "change_date   = to_char(sysdate,'yyyymmdd')";
            whereStr = "where card_no = ? ";
            setString(1, hDcesOldCardNo);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_dbc_card not found!", "", comcr.hCallBatchSeqno);
            }
        }
        if ((hDcesEmbossSource.equals("5")) || (hDcesEmbossSource.equals("7"))) {
            daoTable = "dbc_card";
            updateSQL = "reissue_status = '2',";
            updateSQL += " reissue_date = to_char(sysdate,'yyyymmdd')";
            whereStr = "where card_no = ? ";
            setString(1, hDcesOldCardNo);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_dbc_card not found!", "", comcr.hCallBatchSeqno);
            }
        }
    }

    /***********************************************************************/
    void updateDbcEmbossTmp() throws Exception {
        daoTable = "dbc_emboss_tmp";
        whereStr = "where card_no  = ? ";
        setString(1, hDcesCardNo);
//        whereStr = "where rowid  = ? ";
//        setRowId(1, h_dces_rowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_dbc_emboss_tmp not found!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void processErrFlag() throws Exception {

        daoTable = "dbc_emboss_tmp";
        updateSQL = "error_code = '1'";
        whereStr = "where card_no  = ? ";
        setString(1, hDcesCardNo);
//        whereStr = "where rowid  = ? ";
//        setRowId(1, hDcesRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dbc_emboss_tmp not found!", "", comcr.hCallBatchSeqno);
        }

        if (!hDcesEmbossSource.equals("3") && !hDcesEmbossSource.equals("4")) {
            daoTable = "dbc_card";
            updateSQL = "reissue_status = '4'";
            whereStr = "where card_no  = ? ";
            setString(1, hDcesOldCardNo);
            updateTable();
        } else {
            daoTable = "dbc_card";
            updateSQL = "change_status = '4'";
            whereStr = "where card_no  = ? ";
            setString(1, hDcesOldCardNo);
            updateTable();
        }
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbcD020 proc = new DbcD020();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
