/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/16  V1.01.02  yanghan       修改了變量名稱和方法名稱                                                                                *
*  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
*  110/06/18  V1.00.04   Wilson     where條件新增 -> end_ibm_date <> ''          *
*  110/11/25  V1.00.05   Justin     ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX        *
*  112/01/06  V1.00.06   Wilson     移除end_ibm_date                           *
*  112/08/26  V1.00.07   Wilson     修正弱掃問題                                                                                             *
******************************************************************************/

package Dbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

import bank.authbatch.main.AuthBatch080;
import bank.authbatch.vo.Data004Vo;
import bank.authbatch.vo.Data080Vo;
import bank.authbatch.main.AuthBatch100;
import bank.authbatch.main.AuthBatch004;
import bank.authbatch.vo.Data100Vo;

/*製卡回饋寫入授權系統作業*/
public class DbcD014 extends AccessDAO {
    private String progname = "製卡回饋寫入授權系統作業 112/08/26 V1.00.07";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String prgmId = "DbcD014";
    String hModUser = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";

    String hsysdate = "";
    String hDcesCardNo = "";
    String hDcesEmbossSource = "";
    String hDcesEmbossReason = "";
    String hDcesApplyId = "";
    String hDcesApplyIdCode = "";
    String hDcesCorpNoCode = "";
    String hDcesCorpNo = "";
    String hDcesAcctType = "";
    String hDcesAcctKey = "";
    String hDcesGroupCode = "";
    String hDcesSourceCode = "";
    String hDcesClassCode = "";
    String hDcesOldCardNo = "";
    String hDcesSupFlag = "";
    String hDcesRiskBankNo = "";
    String hDcesValidFm = "";
    String hDcesValidTo = "";
    String hDcesChiName = "";
    String hDcesEngName = "";
    String hDcesBirthday = "";
    String hDcesMailZip = "";
    String hDcesMailAddr1 = "";
    String hDcesMailAddr2 = "";
    String hDcesMailAddr3 = "";
    String hDcesMailAddr4 = "";
    String hDcesMailAddr5 = "";
    String hDcesResidenZip = "";
    String hDcesResidentAddr1 = "";
    String hDcesResidentAddr2 = "";
    String hDcesResidentAddr3 = "";
    String hDceResidentAddr4 = "";
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
    String hDcesCellarPhone = "";
    String hDcesValue = "";
    String hDcesAuthCreditLmt = "";
    String hDcesCorpActFlag = "";
    String hDcesVip = "";
    String hDcesPvv = "";
    String hDcesCvv = "";
    String hDcesCvv2 = "";
    String hDcesOpenNum = "";
    String hDcesVoiceNum = "";
    String hDcesInMainDate = "";
    String hCardIndicator = "";
    String hDcesRowid = "";
    String hDcesActNo = "";
    String hDcesBankActno = "";
    String hDcesPvki = "";
    String hDcesPinBlock = "";
    String hPaymentRule = "";
    String hAcctType = "";
    String hApplyId = "";
    String hCcsAcctKey = "";
    String hCvv2 = "";
    String hSource = "";
    String hEngName = "";
    String hBusCard = "";
    String hIssueDate = "";
    long hSonCreditLmt = 0;
    String pOpenNum = "";
    String pVoiceNum = "";
    String hBankActno = "";
    String hCompanyName = "";
    String hCorpPSeqno = "";
    String hRiskBankNo = "";
    String hCorpAcctKey = "";
    String hClassCode = "";
    String hJobPosition = "";
    String hCardSince = "";
    String hVipCode = "";
    int hLineOfCreditAmt = 0;
    int hBillLowLimit = 0;
    long hComboCashLimit = 0;
    String hAddr = "";
    String hDaaoNewVdchgFlag = "";
    String hActNo = "";
    int recCnt = 0;
    String hChiName = "";
    String hBirthday = "";
    String hHomeAreaCode1 = "";
    String hHomeTelNo1 = "";
    String hHomeTelExt1 = "";
    String hHomeAreaCode2 = "";
    String hHomeTelNo2 = "";
    String hHomeTelExt2 = "";
    String hOfficeAreaCode1 = "";
    String hOfficeTelNo1 = "";
    String hOfficeTelExt1 = "";
    String hOfficeAreaCode2 = "";
    String hOfficeTelNo2 = "";
    String hOfficeTelExt2 = "";
    String hCellarPhone = "";
    double hAssetValue = 0;
    String hVoiceNum = "";
    String hIdnoMsgFlag = "";
    double hIdnoMsgPurchaseAmt = 0;
    int pSonCreditLmt = 0;
    String hAcctKey   = "";
    String hPSeqno    = "";
    String hIdPSeqno = "";
    String hCurrentCode = "";
    String hCorpNo = "";
    String hCorpNoCode = "";
    String hDccdBlockReason = "";
    String hBillAddress = "";
    String hIsRc = "";
    String hCorpActFlag = "";
    String hCreateDate = "";
    String hDaaoBlockReason = "";
    String insChiName = "";
    String hTelHome1 = "";
    String hTelHome2 = "";
    String hTelOffice1 = "";
    String hTelOffice2 = "";
    String hSonCardFlag = "";

    int totCnt = 0;
    int hRecno = 0;
    int tmpInt = 0;
    int rtn = 0;
    

    int nLResult = 0;
    AuthBatch080 l080 = null;
    AuthBatch004 l004 = null;
    AuthBatch100 l100 = null;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0) {
                comc.errExit("Usage : DbcD014 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            l080 = new AuthBatch080(); 
            l080.initProg(getConnection());
            l100 = new AuthBatch100(); 
            l100.initProg(getConnection());
            l004 = new AuthBatch004(); 
            l004.initProg(getConnection());
            
            hModUser = comc.commGetUserID();
            hModPgm = javaProgram;
            hsysdate = "";
            sqlCmd = "select to_char(sysdate,'yyyymmdd') hsysdate ";
            sqlCmd += " from dual ";
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hsysdate = getValue("hsysdate");
            }

            selectDbcEmboss();

            showLogMessage("I", "", String.format("\n執行結束=[%d]", totCnt));

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
    void selectDbcEmboss() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.emboss_source,";
        sqlCmd += "a.emboss_reason,";
        sqlCmd += "a.apply_id,";
        sqlCmd += "decode(a.apply_id_code,'','0',a.apply_id_code) as apply_id_code,";
        sqlCmd += "a.corp_no,";
        sqlCmd += "a.corp_no_code,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.group_code,";
        sqlCmd += "a.source_code,";
        sqlCmd += "a.class_code,";
        sqlCmd += "a.old_card_no,";
        sqlCmd += "a.sup_flag,";
        sqlCmd += "a.risk_bank_no,";
        sqlCmd += "a.valid_fm,";
        sqlCmd += "a.valid_to,";
        sqlCmd += "a.chi_name,";
        sqlCmd += "a.eng_name,";
        sqlCmd += "a.birthday,";
        sqlCmd += "a.mail_zip,";
        sqlCmd += "a.mail_addr1,";
        sqlCmd += "a.mail_addr2,";
        sqlCmd += "a.mail_addr3,";
        sqlCmd += "a.mail_addr4,";
        sqlCmd += "a.mail_addr5,";
        sqlCmd += "a.resident_zip,";
        sqlCmd += "a.resident_addr1,";
        sqlCmd += "a.resident_addr2,";
        sqlCmd += "a.resident_addr3,";
        sqlCmd += "a.resident_addr4,";
        sqlCmd += "a.resident_addr5,";
        sqlCmd += "a.company_name,";
        sqlCmd += "a.job_position,";
        sqlCmd += "a.home_area_code1,";
        sqlCmd += "a.home_tel_no1,";
        sqlCmd += "a.home_tel_ext1,";
        sqlCmd += "a.home_area_code2,";
        sqlCmd += "a.home_tel_no2,";
        sqlCmd += "a.home_tel_ext2,";
        sqlCmd += "a.office_area_code1,";
        sqlCmd += "a.office_tel_no1,";
        sqlCmd += "a.office_tel_ext1,";
        sqlCmd += "a.office_area_code2,";
        sqlCmd += "a.office_tel_no2,";
        sqlCmd += "a.office_tel_ext2,";
        sqlCmd += "a.cellar_phone,";
        sqlCmd += "a.value,";
        sqlCmd += "a.auth_credit_lmt,";
        sqlCmd += "a.corp_act_flag,";
        sqlCmd += "a.vip,";
        sqlCmd += "a.pvv,";
        sqlCmd += "a.cvv,";
        sqlCmd += "a.trans_cvv2,";
        sqlCmd += "a.open_passwd,";
        sqlCmd += "a.voice_passwd,";
        sqlCmd += "a.in_main_date,";
        sqlCmd += "b.card_indicator,";
        sqlCmd += "a.rowid as rowid,";
        sqlCmd += "a.act_no,";
        sqlCmd += "a.bank_actno,";
        sqlCmd += "a.pvki,";
        sqlCmd += "a.pin_block ";
        sqlCmd += " from dbc_emboss a,dbp_acct_type b ";
        sqlCmd += "where a.in_auth_date  ='' ";
        sqlCmd += "  and a.in_main_date <>'' ";
        sqlCmd += "  and a.in_main_error = '0' ";
        sqlCmd += "  and b.acct_type     = a.acct_type ";
        sqlCmd += "  and 1 = 1  ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hDcesCardNo           = getValue("card_no", i);
            hDcesEmbossSource     = getValue("emboss_source", i);
            hDcesEmbossReason     = getValue("emboss_reason", i);
            hDcesApplyId          = getValue("apply_id", i);
            hDcesApplyIdCode     = getValue("apply_id_code", i);
            hDcesCorpNo           = getValue("corp_no", i);
            hDcesCorpNoCode      = getValue("corp_no_code", i);
            hDcesAcctType         = getValue("acct_type", i);
            hDcesAcctKey          = getValue("acct_key", i);
            hDcesGroupCode        = getValue("group_code", i);
            hDcesSourceCode       = getValue("source_code", i);
            hDcesClassCode        = getValue("class_code", i);
            hDcesOldCardNo       = getValue("old_card_no", i);
            hDcesSupFlag          = getValue("sup_flag", i);
            hDcesRiskBankNo      = getValue("risk_bank_no", i);
            hDcesValidFm          = getValue("valid_fm", i);
            hDcesValidTo          = getValue("valid_to", i);
            hDcesChiName          = getValue("chi_name", i);
            hDcesEngName          = getValue("eng_name", i);
            hDcesBirthday          = getValue("birthday", i);
            hDcesMailZip          = getValue("mail_zip", i);
            hDcesMailAddr1        = getValue("mail_addr1", i);
            hDcesMailAddr2        = getValue("mail_addr2", i);
            hDcesMailAddr3        = getValue("mail_addr3", i);
            hDcesMailAddr4        = getValue("mail_addr4", i);
            hDcesMailAddr5        = getValue("mail_addr5", i);
            hDcesResidenZip      = getValue("resident_zip", i);
            hDcesResidentAddr1    = getValue("resident_addr1", i);
            hDcesResidentAddr2    = getValue("resident_addr2", i);
            hDcesResidentAddr3    = getValue("resident_addr3", i);
            hDceResidentAddr4    = getValue("resident_addr4", i);
            hDcesResidentAddr5    = getValue("resident_addr5", i);
            hDcesCompanyName      = getValue("company_name", i);
            hDcesJobPosition      = getValue("job_position", i);
            hDcesHomeAreaCode1   = getValue("home_area_code1", i);
            hDcesHomeTelNo1      = getValue("home_tel_no1", i);
            hDcesHomeTelExt1     = getValue("home_tel_ext1", i);
            hDcesHomeAreaCode2   = getValue("home_area_code2", i);
            hDcesHomeTelNo2      = getValue("home_tel_no2", i);
            hDcesHomeTelExt2     = getValue("home_tel_ext2", i);
            hDcesOfficeAreaCode1 = getValue("office_area_code1", i);
            hDcesOfficeTelNo1    = getValue("office_tel_no1", i);
            hDcesOfficeTelExt1   = getValue("office_tel_ext1", i);
            hDcesOfficeAreaCode2 = getValue("office_area_code2", i);
            hDcesOfficeTelNo2    = getValue("office_tel_no2", i);
            hDcesOfficeTelExt2   = getValue("office_tel_ext2", i);
            hDcesCellarPhone      = getValue("cellar_phone", i);
            hDcesValue             = getValue("value", i);
            hDcesAuthCreditLmt   = getValue("auth_credit_lmt", i);
            hDcesCorpActFlag     = getValue("corp_act_flag", i);
            hDcesVip               = getValue("vip", i);
            hDcesPvv               = getValue("pvv", i);
            hDcesCvv               = getValue("cvv", i);
            hDcesCvv2              = getValue("trans_cvv2", i);
            hDcesOpenNum       = getValue("open_passwd", i);
            hDcesVoiceNum      = getValue("voice_passwd", i);
            hDcesInMainDate      = getValue("in_main_date", i);
            hCardIndicator         = getValue("card_indicator", i);
            hDcesRowid             = getValue("rowid", i);
            hDcesActNo            = getValue("act_no", i);
            hDcesBankActno        = getValue("bank_actno", i);
            hDcesPvki              = getValue("pvki", i);
            hDcesPinBlock         = getValue("pin_block", i);

            totCnt++;

            if (totCnt % 500 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format(" Process cnt=[%d]", totCnt));

/* lai test
h_dces_emboss_source = "1";
*/

if(debug==1) showLogMessage("I","","\n888 Card=["+hDcesCardNo+"]"+hDcesEmbossSource);

            if ((hDcesEmbossSource.equals("1")) || (hDcesEmbossSource.equals("2"))) {
                getOtherData();
                getAddress();
/* lai test
h_dccd_block_reason  = "51";
*/
                if ((hDccdBlockReason.equals("51")) || (hDaaoBlockReason.equals("51")))
                   {
                    rtn = insertOnbat();
                    if(rtn != 0)   continue;
                   }
                /*************************************************************************
                 * 1.emboss_source='1' (2001/12/09)
                 * 2.先檢核是否為新卡戶(act_acno.create_date >=
                 * dbc_emboss.in_main_date入主檔日期) 3.為正卡資料
                 * 以上均成立才可insert_ccs_account()
                 *************************************************************************/
                if (hDcesEmbossSource.equals("1")) {
                    if (hDcesSupFlag.equals("0")) {
                        if (hCreateDate.compareTo(hDcesInMainDate) >= 0) {
                            rtn = insertCcsAccount();
                            if(rtn != 0)   continue;
                            hRecno++;
                        }
                    }
                }
                
                tmpInt = insertCcsBase();
                if(tmpInt != 0)   continue;
                updateDbcEmboss();
            } else {
                getOtherData();
                getAddress();
                tmpInt = insertCcsBase();
                if(tmpInt != 0)   continue;
                updateDbcEmboss();
            }
            commitDataBase();
        }

        /*************************************************
         * insert ccs_account final data以便通知CCAS 可作處理 (2001/12/18)
         *************************************************/
        if (hRecno > 0) {
            insertProcessData();
        }
    }
    /*************************************************************************/
    void insertProcessData() throws Exception {
        setValueInt("to_which" , 2);
        setValue("dog"         , sysDate);
        setValue("dop"         , "");
        setValue("proc_mode"   , "B");
        setValue("proc_status" , "1");
        setValue("card_acct_id", "0000000000");
/* lai
        daoTable = "ccs_account";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.err_rtn("insert_ccs_account duplicate", "", h_call_batch_seqno);
        }
*/
    }

/***********************************************************************/
public int insertOnbat() throws Exception 
{
        String hPaymentRule = "";
        String corpAcctKey;

        hApplyId = "";
        hApplyId = String.format("%s%s", hDcesApplyId, hDcesApplyIdCode);
        hPaymentRule = "1";
        hCcsAcctKey = hAcctKey;
        if (hCardIndicator.equals("2")) {
            if (hCorpActFlag.equals("Y")) {
                hPaymentRule = "2";
                corpAcctKey = String.format("%s000", hCorpNo);
                hCcsAcctKey = corpAcctKey;
            } else {
                hPaymentRule = "1";
            }
        }
        
        extendField = "ccacb.";
        sqlCmd = "select card_acct_idx, ";
        sqlCmd += "      id_p_seqno, ";
        sqlCmd += "      p_seqno, ";
        sqlCmd += "      corp_p_seqno, ";
        sqlCmd += "      bin_type  ";
        sqlCmd += " from cca_card_base ";
        sqlCmd += "where card_no = ? ";
        setString(1, hDcesCardNo);
        selectTable();
        
        Data004Vo lData004Vo = new Data004Vo();
        lData004Vo.setCardNo(hDcesCardNo);
if(debug==1) showLogMessage("I", "", "888 ID["+hCcsAcctKey+"]"+hApplyId          );
        lData004Vo.setCardAcctId(hCcsAcctKey);
        lData004Vo.setCardHldrId(hApplyId);
        lData004Vo.setAccountType(hAcctType);
        lData004Vo.setPaymentType(hPaymentRule);
        lData004Vo.setCardCatalog(hCardIndicator);
        lData004Vo.setMatchFlag("");
        lData004Vo.setBlockCode1("");
        lData004Vo.setBlockCode2("");
        lData004Vo.setBlockCode3("");
        lData004Vo.setBlockCode4("");
        lData004Vo.setBlockCode5("");
        lData004Vo.setAcctNo(hDcesActNo);
        lData004Vo.setOppType("");
        lData004Vo.setOppReason("");
        lData004Vo.setOppDate("");
        lData004Vo.setDebitFlag("N");
        lData004Vo.setTransType("2");
        lData004Vo.setCardAcctIdx(getValue("ccacb.card_acct_idx"));
        lData004Vo.setBinType(getValue("ccacb.bin_type"));
        lData004Vo.setIdPSeqno(getValue("ccacb.id_p_seqno"));
        lData004Vo.setPSeqno(getValue("ccacb.p_seqno"));
        lData004Vo.setCorpPSeqno(getValue("ccacb.corp_p_seqno"));
        
        int result = l004.startProcess(lData004Vo);
        if (result != 0)
           {
            showLogMessage("I", "", lData004Vo.getCardNo() + "call ecs004 error!");
            rollbackDataBase();
            return(1);
           }
        else 
            showLogMessage("I", "", lData004Vo.getCardNo() + "call ecs004 success!");
        
        
//        setValue("trans_type"       , "2");
//        setValueInt("to_which"      , 2);
//        setValue("dog"              , sysDate);
//        setValue("proc_mode"        , "B");
//        setValueInt("proc_status"   , 0);
//        setValue("card_catalog"     , h_card_indicator);
//        setValue("payment_type"     , h_payment_rule);
//        setValue("acct_type"        , h_acct_type);
//        setValue("card_hldr_id"     , h_apply_id);
//        setValue("card_acct_id"     , h_ccs_acct_key);
//        setValue("card_no"          , h_dces_card_no);
//        setValue("acct_no"          , h_dces_act_no);
//        setValue("block_code_1"     , "");
//        setValue("match_flag"       , "");
//        daoTable = "onbat_2ccas";
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.err_rtn("insert_onbat_2ccas duplicate!", "", h_call_batch_seqno);
//        }
        
        
/* lai 
        daoTable   = "dbc_card";
        updateSQL  = " block_reason = '',";
        updateSQL += " block_status = decode(block_reason2,'','22',block_status),";
        updateSQL += " block_date   = block_date,";
        updateSQL += " mod_pgm      = ? ,";
        updateSQL += " mod_time     = sysdate";
        whereStr   = "where card_no      = ?  ";
        whereStr  += "  and current_code = '0' ";
        setString(1, h_mod_pgm);
        setString(2, h_dces_card_no);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.err_rtn("update_dbc_card not found!", "", h_call_batch_seqno);
        }
        daoTable   = "dba_acno";
        updateSQL  = "block_reason = '',";
        updateSQL += " block_status = decode(block_reason2,'','22',block_status),";
        updateSQL += " block_date = block_date,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where acct_no  = ? ";
        setString(1, h_mod_pgm);
        setString(2, h_dces_act_no);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.err_rtn("update_dba_acno not found!", "", h_call_batch_seqno);
        }
*/
   return 0;
}
/***********************************************************************/
public int insertCcsAccount() throws Exception 
{
        String hPaymentRule = "";
        String hCompanyName = "";
        String hCorpAcctKey = "";
        String hActNo = "";
        String hAddr = "";

        hApplyId = "";
        hCompanyName = "";
        hPaymentRule = "1";
        hActNo = "";

        hApplyId = String.format("%s%s", hDcesApplyId, hDcesApplyIdCode);

        hActNo = String.format("%s", hDcesActNo);

        hCompanyName = hDcesCompanyName;
        hCcsAcctKey = hAcctKey;
        if (hCardIndicator.equals("2")) {
            hCompanyName = "";
            sqlCmd = "select chi_name ";
            sqlCmd += " from crd_corp  ";
            sqlCmd += "where corp_p_seqno = ? ";
            setString(1, hCorpPSeqno);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hCompanyName = getValue("chi_name");
            }

            hCorpAcctKey = "";
            hCorpAcctKey = String.format("%s000", hCorpNo);
            sqlCmd = "select risk_bank_no ";
            sqlCmd += " from act_acno  ";
            sqlCmd += "where acct_type = ?  ";
            sqlCmd += "and acct_key = ? ";
            setString(1, hAcctType);
            setString(2, hCorpAcctKey);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hRiskBankNo = getValue("risk_bank_no");
            }
            if (hCorpActFlag.substring(0, 1).equals("Y")) {
                hPaymentRule = "2";
                hCcsAcctKey = hCorpAcctKey;
            } else {
                hPaymentRule = "1";
            }
        }
        hAddr = "";
        hAddr = String.format("%-70.70s", hBillAddress);

        Data100Vo lData100Vo = new Data100Vo();
        lData100Vo.setDebitFlag("Y");
        lData100Vo.setPSeqNo(hPSeqno);
        lData100Vo.setAcnoPSeqNo(hPSeqno); // 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX
        lData100Vo.setAccountType(hAcctType); //ACCT_TYPE
        lData100Vo.setCorpPSeqN0(hCorpPSeqno);
        lData100Vo.setIdPSeqNo(hIdPSeqno);
        lData100Vo.setAcctNo(hActNo);
        lData100Vo.setAcnoFlag("1");
        lData100Vo.setArgueAmt(0);
        lData100Vo.setBillAddress(hAddr);
        lData100Vo.setBillLawPayAmt(0);
        lData100Vo.setBillLowLimit(hBillLowLimit);
        lData100Vo.setCardAcctId(hCcsAcctKey); // CARD_ACCT_IDX='34875'
        lData100Vo.setCardHldrId(hApplyId);
        lData100Vo.setCardIndicator(hCardIndicator);  //rule
        lData100Vo.setCardSince(hCardSince); //card_acct_since
        lData100Vo.setClassCode(hClassCode); //card_acct_level
        lData100Vo.setCloseConsumeFee(0);
        lData100Vo.setCloseInterestFee(0);
        lData100Vo.setCloseLawFee(0);
        lData100Vo.setClosePunishFee(0);
        lData100Vo.setCloseSrvFee(0);
        lData100Vo.setCompanyName(hCompanyName); //corp_name
        lData100Vo.setConsume01(0);
        lData100Vo.setConsume02(0);
        lData100Vo.setConsume03(0);
        lData100Vo.setConsume04(0);
        lData100Vo.setConsume05(0);
        lData100Vo.setConsume06(0);
        lData100Vo.setDog(sysDate + sysTime);
        lData100Vo.setJobPosition(hJobPosition); //position
        lData100Vo.setLineOfCreditAmt(hLineOfCreditAmt); //lmt_tot_consume
        lData100Vo.setLineOfCreditAmtCash(0); //lmt_tot_consume_cash
        lData100Vo.setMaxAtmAmt(0);
        lData100Vo.setMCode("");
        lData100Vo.setNewVdchgFlag(hDaaoNewVdchgFlag);
        
        lData100Vo.setOpenAtm(0);
        lData100Vo.setOpenConsumeFee(0);
        lData100Vo.setOpenInterestFee(0);
        lData100Vo.setOpenLawFee(0);
        lData100Vo.setOpenPunishFee(0);
        lData100Vo.setOpenSrvFee(0);
        lData100Vo.setOpenWritsOff(0);
        
        lData100Vo.setOrganId("");
        lData100Vo.setPayLastestAmt(0);
        lData100Vo.setPaymentRule(hPaymentRule);/*1:個人繳 2:公司繳*/
        
        lData100Vo.setPrepayAmt(0);
        
        lData100Vo.setRiskBankNo("");
        
        lData100Vo.setStatus01("");
        lData100Vo.setStatus02("");
        lData100Vo.setStatus03("");
        lData100Vo.setStatus04("");
        
        lData100Vo.setStatus11("");
        lData100Vo.setStatus12("");
        lData100Vo.setStatus13("");
        lData100Vo.setStatus14("Y");
        
        lData100Vo.setTot1LimitAmt(0);
        lData100Vo.setTot2LimitAmt(0);
        lData100Vo.setVipCode(hVipCode); //status_reason

        nLResult = l100.startProcess(lData100Vo);
        /*
        return 0 => 正常處理完成
        return > 0 => 程式正常處理完成，但有資料面的問題
        return -1 => 有 error (exception)
        */
        if (nLResult==0)
            System.out.println(lData100Vo.getCardAcctId() + ":" + "正常處理完成...");
        else if (nLResult>0)
           {
            System.out.println(lData100Vo.getCardAcctId() + ":" + "程式正常處理完成，但有資料面的問題...");
            rollbackDataBase();
            return(1);
           }
        if (nLResult<0)
           {
            System.out.println(lData100Vo.getCardAcctId() + ":" + "發生 error...");
            rollbackDataBase();
            return(1);
           }
        lData100Vo=null;

        return 0;

}
/***********************************************************************/
int checkCrdIdno() throws Exception {
       int recCnt = 0;

        sqlCmd = "select count(*) rec_cnt ";
        sqlCmd += " from crd_card a, crd_idno b  ";
        sqlCmd += "where b.id_no       = ?  ";
        sqlCmd += "  and b.id_no_code  = ?  ";
        sqlCmd += "  and a.id_p_seqno  = b.id_p_seqno  ";
        sqlCmd += "  and current_code  = '0' ";
        setString(1, hDcesApplyId);
        setString(2, hDcesApplyIdCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("check_crd_idno() not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            recCnt = getValueInt("rec_cnt");
        }

        if (recCnt > 0)
            return (0);
        else
            return (-1);

    }

    /***********************************************************************/
    void getOtherData() throws Exception {
        int pSonCreditLmt = 0;

        hChiName = "";
        hBirthday = "";
        hJobPosition = "";
        hHomeAreaCode1 = "";
        hHomeTelNo1 = "";
        hHomeTelExt1 = "";
        hHomeAreaCode2 = "";
        hHomeTelNo2 = "";
        hHomeTelExt2 = "";
        hOfficeAreaCode1 = "";
        hOfficeTelNo1 = "";
        hOfficeTelExt1 = "";
        hOfficeAreaCode2 = "";
        hOfficeTelNo2 = "";
        hOfficeTelExt2 = "";
        hCellarPhone = "";
        hCardSince = "";
        hVoiceNum = "";
        hIdnoMsgFlag = "";
        hIdnoMsgPurchaseAmt = 0;
        hAssetValue = 0;
        sqlCmd = "select chi_name,";
        sqlCmd += "birthday,";
        sqlCmd += "job_position,";
        sqlCmd += "home_area_code1,";
        sqlCmd += "home_tel_no1,";
        sqlCmd += "home_tel_ext1,";
        sqlCmd += "home_area_code2,";
        sqlCmd += "home_tel_no2,";
        sqlCmd += "home_tel_ext2,";
        sqlCmd += "office_area_code1,";
        sqlCmd += "office_tel_no1,";
        sqlCmd += "office_tel_ext1,";
        sqlCmd += "office_area_code2,";
        sqlCmd += "office_tel_no2,";
        sqlCmd += "office_tel_ext2,";
        sqlCmd += "cellar_phone,";
        sqlCmd += "asset_value,";
        sqlCmd += "card_since,";
        sqlCmd += "voice_passwd,";
        sqlCmd += "decode(msg_flag,'','Y',msg_flag) h_idno_msg_flag,";
        sqlCmd += "msg_purchase_amt ";
        sqlCmd += " from dbc_idno  ";
        sqlCmd += "where id_no      = ?  ";
        sqlCmd += "  and id_no_code = ? ";
        setString(1, hDcesApplyId);
        setString(2, hDcesApplyIdCode);
        int recordCnt = selectTable();
if(debug==1) showLogMessage("I", "", "  888 idno  cnt=["+recordCnt+"]");
        if (recordCnt > 0) {
            hChiName = getValue("chi_name");
            hBirthday = getValue("birthday");
            hJobPosition = getValue("job_position");
            hHomeAreaCode1 = getValue("home_area_code1");
            hHomeTelNo1    = getValue("home_tel_no1");
            hHomeTelExt1   = getValue("home_tel_ext1");
            hHomeAreaCode2 = getValue("home_area_code2");
            hHomeTelNo2    = getValue("home_tel_no2");
            hHomeTelExt2   = getValue("home_tel_ext2");
            hOfficeAreaCode1     = getValue("office_area_code1");
            hOfficeTelNo1        = getValue("office_tel_no1");
            hOfficeTelExt1       = getValue("office_tel_ext1");
            hOfficeAreaCode2     = getValue("office_area_code2");
            hOfficeTelNo2        = getValue("office_tel_no2");
            hOfficeTelExt2       = getValue("office_tel_ext2");
            hCellarPhone          = getValue("cellar_phone");
            hAssetValue           = getValueDouble("asset_value");
            hCardSince            = getValue("card_since");
            hVoiceNum          = getValue("voice_passwd");
            hIdnoMsgFlag         = getValue("h_idno_msg_flag");
            hIdnoMsgPurchaseAmt = getValueDouble("msg_purchase_amt");
        }

        hSonCardFlag = "";
        hAcctType = "";
        hAcctKey = "";
        hPSeqno = "";
        hEngName = "";
        hCorpNo = "";
        hCorpNoCode = "";
        hCorpPSeqno = "";
        hIssueDate = "";
        hCurrentCode = "";
        hDccdBlockReason = "";
        hSonCreditLmt = 0;
        pSonCreditLmt = 0;
        sqlCmd = "select a.INDIV_CRD_LMT,";
        sqlCmd += "a.SON_CARD_FLAG,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "UF_ACNO_KEY2(a.card_no,'') acct_key,";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.eng_name,";
        sqlCmd += "a.trans_cvv2,";
        sqlCmd += "a.issue_date,";
        sqlCmd += "a.current_code,";
        sqlCmd += "a.corp_no,";
        // sqlCmd += "b.corp_no_code,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "b.block_reason1   as block_reason ";
        sqlCmd += " from dbc_card a left join cca_card_acct b ";
        sqlCmd += "                        on a.p_seqno = b.acno_p_seqno ";
		sqlCmd += "                       and b.debit_flag = 'Y'";
        sqlCmd += "where a.card_no = ? ";
        //sqlCmd += "  and a.p_seqno = b.acno_p_seqno and debit_flag = 'Y' ";
        setString(1, hDcesCardNo);
        recordCnt = selectTable();
if(debug==1) showLogMessage("I", "", "  888 dbc_card cnt=["+recordCnt+"]");
        if (recordCnt > 0) {
            pSonCreditLmt = getValueInt("INDIV_CRD_LMT");
            hSonCardFlag  = getValue("SON_CARD_FLAG");
            hAcctType  = getValue("acct_type");
            hAcctKey   = getValue("acct_key");
            hPSeqno    = getValue("p_seqno");
            hIdPSeqno = getValue("id_p_seqno");
if(debug==1) showLogMessage("I", "", "888 NO["+hAcctKey+"]"+hPSeqno+","+hIdPSeqno);
            hEngName   = getValue("eng_name");
            hCvv2       = getValue("trans_cvv2");
            hIssueDate = getValue("issue_date");
            hCurrentCode = getValue("current_code");
            hCorpNo      = getValue("corp_no");
         // h_corp_no_code = getValue("corp_no_code");
            hCorpPSeqno = getValue("corp_p_seqno");
            hDccdBlockReason = getValue("block_reason");
        }
        if (hSonCardFlag.length() > 0) {
            hSonCreditLmt = pSonCreditLmt;
        }
        return;
    }

    /***********************************************************************/
    void getAddress() throws Exception {
        hBillAddress = "";
        hVipCode = "";
        hIsRc = "";
        hClassCode = "";
        hRiskBankNo = "";
        hCorpActFlag = "";
        hCreateDate = "";
        hDaaoBlockReason = "";
        hDaaoNewVdchgFlag = "";
        hLineOfCreditAmt = 0;
        hBillLowLimit = 0;
        hComboCashLimit = 0;
        sqlCmd = "select bill_sending_addr1||" + "bill_sending_addr2||" + "bill_sending_addr3||"
                + "bill_sending_addr4||" + "bill_sending_addr5 bill_address ,";
        sqlCmd += "a.vip_code,";
        sqlCmd += "a.rc_use_indicator,";
        sqlCmd += "a.class_code,";
        sqlCmd += "a.risk_bank_no,";
        sqlCmd += "a.line_of_credit_amt,";
        sqlCmd += "a.corp_act_flag,";
        sqlCmd += "a.month_purchase_lmt,";
        sqlCmd += "decode(a.crt_date,'','19110000',a.crt_date) h_create_date,";
        sqlCmd += "b.block_reason1   as block_reason,";
        sqlCmd += "a.new_vdchg_flag ";
        sqlCmd += " from dba_acno a left join cca_card_acct b ";
        sqlCmd += "                        on a.p_seqno = b.acno_p_seqno ";
		sqlCmd += "                       and b.debit_flag = 'Y'";
        sqlCmd += "where a.p_seqno = ? ";
        //sqlCmd += "  and a.p_seqno = b.acno_p_seqno and b.debit_flag = 'Y' ";
        setString(1, hPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBillAddress = getValue("bill_address");
            hVipCode = getValue("vip_code");
            hIsRc = getValue("rc_use_indicator");
            hClassCode = getValue("class_code");
            hRiskBankNo = getValue("risk_bank_no");
            hLineOfCreditAmt = getValueInt("line_of_credit_amt");
            hCorpActFlag = getValue("corp_act_flag");
            hBillLowLimit = getValueInt("month_purchase_lmt");
            hCreateDate = getValue("h_create_date");
            hDaaoBlockReason = getValue("block_reason");
            hDaaoNewVdchgFlag = getValue("new_vdchg_flag");
        }
        return;
    }
/***********************************************************************/
public int insertCcsBase() throws Exception 
{
        String hPaymentRule = "";
        String pOpenNum = "";
        String pVoiceNum = "";
        String corpAccKey = "";

        hApplyId = "";
        hApplyId = String.format("%s%s", hDcesApplyId, hDcesApplyIdCode);

        hBankActno = String.format("%s", hDcesBankActno);

        hBusCard = "";
        hBusCard = "N";
        if (hCardIndicator.equals("2"))
            hBusCard = "Y";
        hPaymentRule = "1";
        hCcsAcctKey = hAcctKey;
        if (hCardIndicator.equals("2")) {
            if (hCorpActFlag.equals("Y")) {
                hPaymentRule = "2";
                corpAccKey = String.format("%s000", hCorpNo);
                hCcsAcctKey = corpAccKey;
            } else {
                hPaymentRule = "1";
            }
        }
        hSource = "";
        pOpenNum = "";
        pVoiceNum = "";
        pOpenNum = hDcesOpenNum;
        pVoiceNum = hVoiceNum;
        switch (Integer.parseInt(hDcesEmbossSource)) {
        case 1:
            hSource = "1";
            break;
        case 2:
            hSource = "1";
            break;
        case 3:
        case 4:
            hSource = "4";
            break;
        case 5:
            switch (Integer.parseInt(hDcesEmbossReason)) {
            case 1:
                hSource = "4";
                break;
            case 2:
                hSource = "4";
                break;
            case 3:
                hSource = "4";
                break;
            }
            break;
        case 7:
            hSource = "4";
            break;
        }
        
        Data080Vo lData080Vo = new Data080Vo();
        lData080Vo.setAccountType(hAcctType);
        lData080Vo.setAcctNo(hDcesActNo);
        lData080Vo.setAcnoFlag("1");
        lData080Vo.setBankActNo(hBankActno);
        lData080Vo.setBinType("V");
        lData080Vo.setBusinessCard(hBusCard);
        lData080Vo.setCardAcctId(hCcsAcctKey); //=> 應該是 11 bytes
        lData080Vo.setCardHolderId(hApplyId); //CardHolderId+Seq => 應該是 11 bytes
        lData080Vo.setCardNo(hDcesCardNo);
        lData080Vo.setCardType(hDcesSupFlag.equals("0") ? "Y" : "N");
        lData080Vo.setBusinessCard("");
        lData080Vo.setComboIndicator("");
        lData080Vo.setCorpPSeqN0("");
        lData080Vo.setCreditLimit((int)hSonCreditLmt);
        lData080Vo.setCvc2(hCvv2);
        lData080Vo.setDcCurrCode("");
        lData080Vo.setDebitFlag("Y");//N
        lData080Vo.setEngName(hEngName);
        lData080Vo.setPSeqNo(hPSeqno);//
        lData080Vo.setGroupCode(hDcesGroupCode);
        lData080Vo.setIdPSqno(hIdPSeqno);//
        lData080Vo.setMajorIdPSeqNo(hIdPSeqno);//
        lData080Vo.setMemberSince(hIssueDate);
        lData080Vo.setOldCardNo(hDcesOldCardNo);
        lData080Vo.setPaymentRule(hPaymentRule);
        lData080Vo.setPinBlock(hDcesPinBlock);
        lData080Vo.setPinOfActive(pOpenNum);
        lData080Vo.setPinOfVoice(pVoiceNum);
        lData080Vo.setAcnoPSeqNo(hPSeqno);//""
        lData080Vo.setPvki(hDcesPvki);
        lData080Vo.setRule(hCardIndicator);
        lData080Vo.setSource(hSource);
        lData080Vo.setValidFrom(hDcesValidFm.substring(0, 6));
        lData080Vo.setValidTo(hDcesValidTo.substring(0, 6));
        nLResult = l080.startProcess(lData080Vo);

        if (nLResult==0)
            System.out.println(lData080Vo.getCardNo() + ":" + "正常處理完成...");
        else if (nLResult>0)
           {
            System.out.println(lData080Vo.getCardNo() + ":" + "程式正常處理完成，但有資料面的問題...["+nLResult+"]");
            rollbackDataBase();
            return(1);
           }
        if (nLResult<0)
           {
            System.out.println(lData080Vo.getCardNo() + ":" + "發生 error...["+nLResult+"]");
            rollbackDataBase();
            return(1);
           }
        
        lData080Vo = null;
        
//        setValueInt("to_which", 2);
//        setValue("dog", sysDate + sysTime);
//        setValue("dop", null);
//        setValue("proc_mode", "B");
//        setValue("proc_status", "0");
//        setValue("card_no", h_dces_card_no);
//        setValue("card_hldr_id", h_apply_id);
//        setValue("rule", h_card_indicator);
//        setValue("payment_rule", h_payment_rule);
//        setValue("acct_type", h_acct_type);
//        setValue("card_acct_id", h_ccs_acct_key);
//        setValue("valid_from", h_dces_valid_fm.substring(0, 6));
//        setValue("valid_to", h_dces_valid_to.substring(0, 6));
//        setValue("old_card_no", h_dces_old_card_no);
//        setValue("cvc2", h_cvv2);
//        setValue("source", h_source);
//        setValue("eng_name", h_eng_name);
//        setValue("business_card", h_bus_card);
//        setValue("member_since", h_issue_date);
//        setValue("card_type", h_dces_sup_flag.equals("0") ? "Y" : "N");
//        setValueLong("credit_limit", h_son_credit_lmt);
//        setValue("pin_of_active", p_open_passwd);
//        setValue("pin_of_voice", p_voice_passwd);
//        setValue("bank_actno", h_bank_actno);
//        setValue("group_code", h_dces_group_code);
//        setValue("pvki", h_dces_pvki);
//        setValue("pin_block", h_dces_pin_block);
//        setValue("acct_no", h_dces_act_no);
//        daoTable = "cca_base"; // ccs_base
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.err_rtn("insert_cca_base duplicate!", "", h_call_batch_seqno);
//        }
   return 0;
}
/***********************************************************************/
    void updateDbcEmboss() throws Exception {
        daoTable   = "dbc_emboss";
        updateSQL  = " in_auth_date = ?,";
        updateSQL += " mod_pgm      = ?,";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_user     = ?";
        whereStr    = "where rowid  = ? ";
        setString(1, hsysdate);
        setString(2, prgmId);
        setString(3, hModUser);
        setRowId(4, hDcesRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dbc_emboss not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbcD014 proc = new DbcD014();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
