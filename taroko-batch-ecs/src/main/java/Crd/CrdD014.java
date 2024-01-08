/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/08/16  V1.01.01  Lai        Initial          old: crd_d008             *
* 109/12/19  V1.00.02   shiyuqi       updated for project coding standard   *
* 112/03/27  V1.00.03  Wilson     add order by sup_flag                      *
* 112/04/12  V1.00.04  Wilson     調整setPinOfActive(mbosOpenNum)             *
* 112/08/26  V1.00.05  Wilson     修正弱掃問題                                                                                                *
*****************************************************************************/
package Crd;

import com.*;

import bank.authbatch.main.AuthBatch080;
import bank.authbatch.vo.Data080Vo;
import bank.authbatch.main.AuthBatch100;
import bank.authbatch.vo.Data100Vo;

import java.util.*;

@SuppressWarnings("unchecked")
public class CrdD014 extends AccessDAO {
    private String progname = "製卡回饋資料寫入授權 112/08/26  V1.00.05 ";
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
    String mbosApplyIdCode = "";
    String mbosCardNo = "";
    String mbosEmbossSource = "";
    String mbosEmbossReason = "";
    String mbosInMainDate = "";
    String mbosGroupCode = "";
    String mbosCardType = "";
    String mbosAcctType = "";
    String mbosOpenNum = "";

    String mbosPvki = "";
    String mbosPinBlock = "";

    String hCardIndicator = "";
    String hChiName = "";
    String hBirthday = "";
    String hJobPosition = "";
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
    String hVoiceNum = "";
    String hCardSince = "";
    String hCardAcnoFlag = "";
    String hCardBinType = "";
    String hMsgFlag = "";
    String hCompanyName = "";
    double hAssetValue = 0;
    double hMsgPurchaseAmt = 0;
    String hBillAddress = "";
    String hVipCode = "";
    String hIsRc = "";
    String hClassCode = "";
    String hRiskBankNo = "";
    String hCorpActFlag = "";
    int hLineOfCreditAmt = 0;
    int hLineOfCreditAmtCash = 0;
    int hBillLowLimit = 0;
    int hComboCashLimit = 0;
    String hAcctKey = "";
    String hCreateDate = "";
    int hSonCreditLmt = 0;
    String hSonCardFlag = "";
    String hAcctType = "";
    String hEngName = "";
    String hCvv2 = "";
    String hPvki = "";
    String hPinBlock = "";
    String hIssueDate = "";
    String hCurrentCode = "";
    String hCorpNo = "";
    String hCorpNoCode = "";
    String hCorpPSeqno = "";
    String hIdPSeqno = "";
    String hMajorIdPSeqno = "";
    String hAcnoPSeqno = "";
    String hPSeqno = "";

    int hRecno = 0;
    

    int nLResult = 0;
    AuthBatch080 l080 = null;
    AuthBatch100 l100 = null;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdD014 proc = new CrdD014();
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
                String err1 = "CrdD014  [seq_no]\n";
                String err2 = "CrdD014  [seq_no]";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            l080 = new AuthBatch080();
            l080.initProg(getConnection());
            l100 = new AuthBatch100();
            l100.initProg(getConnection());
            
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

            selectCrdEmboss();

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

        showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] ["
                                                 + hBusiChiDate + "]");
    }

    // ************************************************************************
    public void selectCrdEmboss() throws Exception {

        daoTable = "crd_emboss ";
        whereStr = "where in_auth_date   = ''  " + "  and in_main_date  <> ''  " 
                 + "  and in_main_error  = '0' "
                 + "  and card_no       <> ''  " + "  and rtn_nccc_date <> ''  "
                 + "  order by sup_flag  ";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            initRtn();

            mbosBatchno = getValue("batchno",i);
            mbosRecno = getValueDouble("recno",i);
            mbosApplyId = getValue("apply_id",i);
            mbosApplyIdCode = getValue("apply_id_code",i);
            mbosCardNo = getValue("card_no",i);
            mbosEmbossSource = getValue("emboss_source",i);
            mbosEmbossReason = getValue("emboss_reason",i);
            mbosInMainDate = getValue("in_main_date",i);
            mbosGroupCode = getValue("group_code",i);
            mbosCardType = getValue("card_type",i);
            mbosAcctType = getValue("acct_type",i);
            mbosOpenNum = getValue("open_passwd",i);

            totalCnt++;
            processDisplay(5000); // every nnnnn display message
            getPtrAcctType();

            if (debug == 1) {
                showLogMessage("I","","888 Batch=[" + mbosBatchno + "]" + mbosCardNo);
                showLogMessage("I","","888  src=[" + mbosEmbossSource + "]"+ mbosApplyId +"]");
            }

            tmpInt = getOtherData();
            tmpInt = getAddress();
            if (mbosEmbossSource.equals("1") || mbosEmbossSource.equals("2")) {
                tmpInt = getPinBlock();
                /***************************************************************
                 * 1.emboss_source='1' (2001/12/09)
                 * 2.先檢核是否為新卡戶(act_acno.create_date >=
                 * crd_emboss.in_main_date入主檔日期) 3.為正卡資料
                 * 以上均成立才可insert_cca_account()
                 ***************************************************************/
                if (mbosEmbossSource.equals("1")) {
                    if (getValue("sup_flag").equals("0")) {
                        if(debug == 1)
                           showLogMessage("I","", " Date=["+ hCreateDate +"]"+ mbosInMainDate);
/* lai test
   h_create_date = sysDate;
*/
                        if(hCreateDate.compareTo(mbosInMainDate) >= 0) {
                           tmpInt = insertCcaAccount();
                           if(tmpInt != 0)   continue;
                           hRecno++;
                        }
                    }
                }

//              insert_cca_holder();
                tmpInt = insertCcaBase();
                if(tmpInt != 0)   continue;
                insertCrdJcic();
                updateCrdEmboss();
            } else {
                tmpInt = insertCcaBase();
                if(tmpInt != 0)   continue;
                insertCrdJcic();
                updateCrdEmboss();
            }
            commitDataBase();
            
        }
        /*************************************************
         * insert cca_account final data以便通知CCAS 可作處理 (2001/12/18)
         *************************************************/
        if (hRecno > 0) {
            insertProcessData();
        }

    }

    // ************************************************************************
    public int selectCrdIdno(String chkIdPSeqno) throws Exception {
        extendField = "idno.";
        selectSQL = "chi_name         ,birthday      ,job_position    , "
                + "home_area_code1  ,home_tel_no1  ,home_tel_ext1   , "
                + "home_area_code2  ,home_tel_no2  ,home_tel_ext2   , "
                + "office_area_code1,office_tel_no1,office_tel_ext1 , "
                + "office_area_code2,office_tel_no2,office_tel_ext2 , "
                + "cellar_phone     ,asset_value   ,card_since      , "
                + "voice_passwd     ,msg_flag      ,msg_purchase_amt, " + "company_name     ";
        daoTable = "crd_idno      ";
        whereStr = "where id_no         = ? " + "  and id_no_code    = ? ";

        setString(1, mbosApplyId);
        setString(2, mbosApplyIdCode);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_crd_idno            error[notFind]=[" + mbosApplyId + "]" + mbosApplyIdCode;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hChiName = getValue("idno.chi_name");
        hBirthday = getValue("idno.birthday");
        hJobPosition = getValue("idno.job_position");
        hHomeAreaCode1 = getValue("idno.home_area_code1");
        hHomeTelNo1 = getValue("idno.home_tel_no1");
        hHomeTelExt1 = getValue("idno.home_tel_ext1");
        hHomeAreaCode2 = getValue("idno.home_area_code2");
        hHomeTelNo2 = getValue("idno.home_tel_no2");
        hHomeTelExt2 = getValue("idno.home_tel_ext2");
        hOfficeAreaCode1 = getValue("idno.office_area_code1");
        hOfficeTelNo1 = getValue("idno.office_tel_no1");
        hOfficeTelExt1 = getValue("idno.office_tel_ext1");
        hOfficeAreaCode2 = getValue("idno.office_area_code2");
        hOfficeTelNo2 = getValue("idno.office_tel_no2");
        hOfficeTelExt2 = getValue("idno.office_tel_ext2");
        hCellarPhone = getValue("idno.cellar_phone");
        hVoiceNum = getValue("idno.voice_passwd");
        hCardSince = getValue("idno.card_since");
        hMsgFlag = getValue("idno.msg_flag");
        hCompanyName = getValue("idno.company_name");
        hAssetValue = getValueDouble("idno.asset_value");
        hMsgPurchaseAmt = getValueDouble("idno.msg_purchase_amt");

        return (0);
    }

    // ************************************************************************
    public int getPtrAcctType() throws Exception {
        selectSQL = "card_indicator       ";
        daoTable = "ptr_acct_type ";
        whereStr = "where acct_type    = ? ";

        setString(1, mbosAcctType);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_acct_type error[notFind]" + mbosAcctType;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hCardIndicator = getValue("card_indicator");

        return (0);
    }

    // ************************************************************************
    public int insertCcaAccount() throws Exception {
        String hApplyId = "";
        String hCorpAcctKey = "";
        if (debug == 1)
            showLogMessage("I", "", " insert account=[" + getValue("apply_id") + "]");

        hApplyId = mbosApplyId + mbosApplyIdCode;
        
        String tmpPaymentRule = "1";
        String tmpCardAcctId = hApplyId;
        if (hCardIndicator.equals("2")) {
            hCompanyName = "";

            extendField = "corp.";
            selectSQL = "chi_name ";
            daoTable = "crd_corp   ";
            whereStr = "where corp_p_seqno = ? ";

            setString(1, hCorpPSeqno);

            tmpInt = selectTable();

            hCompanyName = getValue("corp.chi_name");

            hCorpAcctKey = hCorpNo + "000";
            extendField = "acno1.";
            selectSQL = "risk_bank_no ";
            daoTable = "act_acno   ";
            whereStr = "where acct_type    = ? " + "  and acct_key     = ? ";

            setString(1, hAcctType);
            setString(2, hCorpAcctKey);

            tmpInt = selectTable();

            hRiskBankNo = getValue("acno1.risk_bank_no");
            if (hCorpActFlag.equals("Y")) {
                tmpPaymentRule = "2";
                tmpCardAcctId = hCorpAcctKey;
            } else {
                tmpPaymentRule = "1";
                tmpCardAcctId = hApplyId;
            }
        }
if(debug ==1)
   showLogMessage("I","","  888  ind=[" + hCardIndicator + "]"+tmpCardAcctId+","+ hAcnoPSeqno);

        Data100Vo lData100Vo = new Data100Vo();
        lData100Vo.setDebitFlag("N");
        lData100Vo.setPSeqNo(hPSeqno);
        lData100Vo.setAcnoPSeqNo(hAcnoPSeqno);
        lData100Vo.setCorpPSeqN0(hCorpPSeqno);
        lData100Vo.setIdPSeqNo(hIdPSeqno);
        lData100Vo.setAccountType(hAcctType); //ACCT_TYPE
        lData100Vo.setAcctNo("");
        lData100Vo.setAcnoFlag(hCardAcnoFlag);
        lData100Vo.setArgueAmt(0);
        lData100Vo.setBillAddress(hBillAddress);
        lData100Vo.setBillLawPayAmt(0);
        lData100Vo.setBillLowLimit(hBillLowLimit);
        lData100Vo.setCardAcctId(tmpCardAcctId); // CARD_ACCT_IDX='34875'
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
        lData100Vo.setLineOfCreditAmtCash(hLineOfCreditAmtCash); //lmt_tot_consume_cash
        lData100Vo.setMaxAtmAmt(0);
        lData100Vo.setMCode("");
        lData100Vo.setNewVdchgFlag("");
        
        lData100Vo.setOpenAtm(0);
        lData100Vo.setOpenConsumeFee(0);
        lData100Vo.setOpenInterestFee(0);
        lData100Vo.setOpenLawFee(0);
        lData100Vo.setOpenPunishFee(0);
        lData100Vo.setOpenSrvFee(0);
        lData100Vo.setOpenWritsOff(0);
        
        lData100Vo.setOrganId("");
        lData100Vo.setPayLastestAmt(0);
        lData100Vo.setPaymentRule(tmpPaymentRule);/*1:個人繳 2:公司繳*/
        
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
        lData100Vo.setVipCode(hVipCode); //status_code

        nLResult = l100.startProcess(lData100Vo);
        /*
        return 0 => 正常處理完成
        return > 0 => 程式正常處理完成，但有資料面的問題
        return -1 => 有 error (exception)
        */
        if (nLResult ==0)
            System.out.println(lData100Vo.getCardAcctId() + ":" + "正常處理完成...");
        else if(nLResult >0)
               {
                System.out.println(lData100Vo.getCardAcctId() + ":" 
                                  + "程式正常處理完成，但有資料面的問題...["+ nLResult +"]");
                rollbackDataBase();
                return(1);
               }
        if (nLResult <0)
           {
            System.out.println(lData100Vo.getCardAcctId() + ":" + "發生 error...["+ nLResult +"]");
            rollbackDataBase();
            return(1);
           }
        lData100Vo=null;
        
        
        
        setValueInt("to_which", 2);
        setValue("dop"        , "");
        setValue("proc_mode"  , "B");
        setValue("proc_status", "0");
        setValueDouble("total_unpaid_amt", hComboCashLimit);
        setValue("branch", hRiskBankNo);
//        setValue("dog", sysDate + sysTime);
//        setValue("rule", h_card_indicator);
//        setValue("acct_type", h_acct_type);
//        setValue("card_hldr_id", h_apply_id);
//        setValue("card_acct_level", h_class_code);
//        setValue("position", h_job_position);
//        setValue("card_acct_since", h_card_since);
//        setValue("acno_flag", h_card_acno_flag);
//        setValue("status_14", "Y");
//        setValue("status_reason", h_vip_code);
//        setValueDouble("max_precash_amt", 0);
//        setValueDouble("lmt_tot_consume", h_line_of_credit_amt);
//        setValueDouble("lmt_tot_consume_cash", h_line_of_credit_amt_cash);
//        setValueDouble("bill_low_limit", h_bill_low_limit);
//        setValue("corp_name", h_company_name);
//        setValue("bill_address", h_bill_address);

//        daoTable = "cca_account  ";
//
//        insertTable();
//
//        if (dupRecord.equals("Y")) {
//            String err1 = "insert_cca_account       error[dupRecord]=" + mbos_card_no;
//            String err2 = "";
//            comcr.err_rtn(err1, err2, comcr.h_call_batch_seqno);
//        }

        return (0);
    }

    // ************************************************************************
    public int insertCcaHolder() throws Exception {
        String hApplyId = "";
        String hTelHome1 = "";
        String hTelHome2 = "";
        String hTelOffice1 = "";
        String hTelOffice2 = "";
        String insChiName = "";

        hApplyId = mbosApplyId + mbosApplyIdCode;
        if (hHomeTelNo1.length() > 0)
            hTelHome1 = String.format("%-4s", hHomeAreaCode1) + String.format("%-10s", hHomeTelNo1)
                    + String.format("%-6s", hHomeTelExt1);
        if (hHomeTelNo2.length() > 0)
            hTelHome2 = String.format("%-4s", hHomeAreaCode2) + String.format("%-10s", hHomeTelNo2)
                    + String.format("%-6s", hHomeTelExt2);
        if (hOfficeTelNo1.length() > 0)
            hTelOffice1 = String.format("%-4s", hOfficeAreaCode1) + String.format("%-10s", hOfficeTelNo1)
                    + String.format("%-6s", hOfficeTelExt1);
        if (hOfficeTelNo2.length() > 0)
            hTelOffice2 = String.format("%-4s", hOfficeAreaCode2) + String.format("%-10s", hOfficeTelNo2)
                    + String.format("%-6s", hOfficeTelExt2);
        tmpInt = hChiName.length();
        insChiName = hChiName.substring(0, tmpInt);
        if (tmpInt > 12)
            insChiName = hChiName.substring(0, 12);

        setValueInt("to_which", 2);
        setValue("dog"           , sysDate + sysTime);
        setValue("dop"           , "");
        setValue("proc_mode"     , "B");
        setValue("proc_status"   , "0");
        setValue("card_hldr_id"  , hApplyId);
        setValue("name"          , insChiName);
        setValue("dob"           , hBirthday);
        setValue("tel_home_1"    , hTelHome1);
        setValue("tel_home_2"    , hTelHome2);
        setValue("tel_office_1"  , hTelOffice1);
        setValue("tel_office_2"  , hTelOffice2);
        setValue("mobile"        , hCellarPhone);
        setValueDouble("guaranty", hAssetValue);
        setValue("job_position"  , hJobPosition);
        setValue("card_since"    , hCardSince);
        setValue("msg_flag"      , hMsgFlag);
        setValue("p_seqno"       , hAcnoPSeqno);
        setValueDouble("msg_purchase_amt", hMsgPurchaseAmt);

        daoTable = "cca_holder   ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_cca_holder      error[dupRecord]=" + mbosCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int insertCcaBase() throws Exception {
        String hSource       = "";
        String hPaymentRule = "";
        String hApplyId     = "";
        String pVoiceNum = "";
        String corpAcctKey  = "";
        String hCcsAcctKey = "";

        hApplyId = mbosApplyId + mbosApplyIdCode;
        hCcsAcctKey = hAcctKey;
        String hBusCard = "N";
        hPaymentRule = "1";
        if (hCardIndicator.equals("2")) {
            hBusCard = "Y";
            if (hCorpActFlag.equals("Y")) {
                hPaymentRule = "2";
                corpAcctKey = hCorpNo + "000";
                hCcsAcctKey = corpAcctKey;
            } else {
                hPaymentRule = "1";
            }
        }
        pVoiceNum = hVoiceNum;

        switch (mbosEmbossSource.trim()) {
        case "1":
        case "2":
            hSource = "1";
            break;
        case "3":
        case "4":
            hSource = "2";
            break;
        case "5":
            switch (mbosEmbossReason.trim()) {
            case "1":
                hSource = "4";
                break;
            case "2":
                hSource = "3";
                break;
            case "3":
                hSource = "4";
                break;
            }
        case "7":
            hSource = "4";
            break;
        }

if(debug ==1)
   showLogMessage("I","","  888  src=[" + hSource+ "]");
       
        Data080Vo lData080Vo = new Data080Vo();
        lData080Vo.setAccountType(hAcctType);
        lData080Vo.setAcctNo(getValue("act_no"));
        lData080Vo.setAcnoFlag(hCardAcnoFlag);
        lData080Vo.setBankActNo("");
        lData080Vo.setBinType(hCardBinType);
        lData080Vo.setBusinessCard(hBusCard);
        lData080Vo.setCardAcctId(hCcsAcctKey); //=> 應該是 11 bytes
        lData080Vo.setCardHolderId(hApplyId); //CardHolderId+Seq => 應該是 11 bytes
        lData080Vo.setCardNo(mbosCardNo);
        lData080Vo.setCardType("Y");
        if (getValue("sup_flag").equals("1"))
            lData080Vo.setCardType("N");
        lData080Vo.setBusinessCard("");
        lData080Vo.setComboIndicator(getValue("combo_indicator"));
        lData080Vo.setCreditLimit(hSonCreditLmt);
        lData080Vo.setCvc2(hCvv2);
        lData080Vo.setDcCurrCode("");
        lData080Vo.setDebitFlag("N");
        lData080Vo.setEngName(hEngName);
        lData080Vo.setCorpPSeqN0(hCorpPSeqno);
        lData080Vo.setPSeqNo(hPSeqno);
        lData080Vo.setIdPSqno(hIdPSeqno);
        lData080Vo.setGroupCode(mbosGroupCode);
        lData080Vo.setMajorIdPSeqNo(hMajorIdPSeqno);
        lData080Vo.setMemberSince(hIssueDate);
        lData080Vo.setOldCardNo(getValue("old_card_no"));
        lData080Vo.setPaymentRule(hPaymentRule);
        lData080Vo.setPinBlock(mbosPinBlock);
        lData080Vo.setPinOfActive(mbosOpenNum);
        lData080Vo.setPinOfVoice(pVoiceNum);
        lData080Vo.setAcnoPSeqNo(hAcnoPSeqno);
        lData080Vo.setPvki(mbosPvki);
        lData080Vo.setRule(hCardIndicator);
        lData080Vo.setSource(hSource);
        lData080Vo.setValidFrom(getValue("valid_fm").substring(0, 6));
        lData080Vo.setValidTo(getValue("valid_to").substring(0, 6));
        nLResult = l080.startProcess(lData080Vo);

        if(nLResult == 0)
           System.out.println(lData080Vo.getCardNo() + "程式正常處理完成..." + nLResult);
        else if(nLResult >0)
               {
                System.out.println(lData080Vo.getCardNo() + ":" 
                                  + "程式正常處理完成，但有資料面的問題...["+ nLResult +"]");
                rollbackDataBase();
                return(1);
               }
        if (nLResult <0)
           {
            System.out.println(lData080Vo.getCardNo() + ":" + "發生 error...["+ nLResult +"]");
            rollbackDataBase();
            return(1);
           }
        lData080Vo = null;
        
        
//        setValueInt("to_which", 2);
//        setValue("dog", sysDate + sysTime);
//        setValue("dop", "");
//        setValue("proc_mode", "B");
//        setValue("proc_status", "0");
//        setValue("card_no", mbos_card_no);
//        setValue("card_hldr_id", h_apply_id);
//        setValue("rule", h_card_indicator);
//        setValue("payment_rule", h_payment_rule);

//        setValue("acct_type", h_acct_type);
//        setValue("card_acct_id", h_ccs_acct_key);
//        setValue("valid_from", getValue("valid_fm").substring(0, 6));
//        setValue("valid_to", getValue("valid_to").substring(0, 6));
//        setValue("old_card_no", getValue("old_card_no"));
//        setValue("cvc2", h_cvv2);
//        setValue("source", h_source);
//        setValue("eng_name", h_eng_name);
//        setValue("business_card", h_bus_card);
//        setValue("member_since", h_issue_date);
//        setValue("card_type", "Y");
//        if (getValue("sup_flag").equals("1"))
//            setValue("card_type", "N");
//        setValueInt("credit_limit", h_son_credit_lmt);
//        setValue("pin_of_active", p_open_passwd);
//        setValue("pin_of_voice", p_voice_passwd);
//        setValue("pvki", mbos_pvki);
//        setValue("pin_block", mbos_pin_block);
//        setValue("group_code", mbos_group_code);
//        setValue("acct_no", getValue("act_no"));
//        setValue("combo_indicator"  , getValue("combo_indicator"));
//        setValue("acno_flag"        , h_card_acno_flag);
//        setValue("p_seqno"          , h_acno_p_seqno);
//        setValue("gp_no"            , h_p_seqno);
//        setValue("id_p_seqno"       , h_id_p_seqno);
//        setValue("major_id_p_seqno" , h_major_id_p_seqno);
//        setValue("corp_p_seqno"     , h_corp_p_seqno);

//        daoTable = "cca_base     ";
//
//        insertTable();
//
//        if (dupRecord.equals("Y")) {
//            String err1 = "insert_cca_base        error[dupRecord]=" + mbos_card_no;
//            String err2 = "";
//            comcr.err_rtn(err1, err2, comcr.h_call_batch_seqno);
//        }

        return (0);
    }

    // ************************************************************************
    public int insertCrdJcic() throws Exception {

        setValue("trans_type"  , "A");
        setValue("card_no"     , mbosCardNo);
        setValue("crt_date"    , sysDate);
        setValue("crt_user"    , javaProgram);
        setValue("apr_date"    , sysDate);
        setValue("apr_user"    , javaProgram);
        setValue("current_code", hCurrentCode);
        setValue("is_rc"       , hIsRc);
        setValue("mod_time"    , sysDate + sysTime);
        setValue("mod_pgm"     , javaProgram);

        daoTable = "crd_jcic   ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_jcic      error[dupRecord]=";
            String err2 = mbosCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int insertProcessData() throws Exception {
        String hApplyId = "";
        String hTelHome1 = "";
        String hTelHome2 = "";
        String hTelOffice1 = "";
        String hTelOffice2 = "";
        String insChiName = "";

        setValueInt("to_which", 2);
        setValue("dog", sysDate + sysTime);
        setValue("dop", "");
        setValue("proc_mode", "B");
        setValue("proc_status", "1");
        setValue("card_acct_id", "0000000000");
/*
        daoTable = "cca_account  ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_cca_account     error[dupRecord]=" + mbos_card_no;
            String err2 = "";
            comcr.err_rtn(err1, err2, comcr.h_call_batch_seqno);
        }
*/

        return (0);
    }

    // ************************************************************************
    public int getOtherData() throws Exception {

        selectCrdIdno("");

        extendField = "card.";
        selectSQL = "indiv_crd_lmt ,son_card_flag,issue_date  ,current_code, "
                + "acct_type     ,acno_p_seqno      ,eng_name        , " 
                + "trans_cvv2    ,pvki         ,pin_block       , "
                + "corp_no       ,corp_no_code ,corp_p_seqno    , " 
                + "p_seqno       ,id_p_seqno   ,major_id_p_seqno, "
                + "acno_flag     ,bin_type     ";
        daoTable = "crd_card            ";
        whereStr = "WHERE card_no      = ?  ";

        setString(1, mbosCardNo);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_crd_card      error[notFind]" + mbosCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hSonCreditLmt = getValueInt("card.indiv_crd_lmt");
        hSonCardFlag = getValue("card.son_card_flag");
        hAcctType = getValue("card.acct_type");
        hEngName = getValue("card.eng_name");
        hCvv2 = getValue("card.trans_cvv2");
        hPvki = getValue("card.pvki");
        hPinBlock = getValue("card.pin_block");
        hIssueDate = getValue("card.issue_date");
        hCurrentCode = getValue("card.current_code");
        hCorpNo = getValue("card.corp_no");
        hCorpNoCode = getValue("card.corp_no_code");
        hCorpPSeqno = getValue("card.corp_p_seqno");
        hAcnoPSeqno = getValue("card.acno_p_seqno");
        hPSeqno = getValue("card.p_seqno");
        hIdPSeqno = getValue("card.id_p_seqno");
        hMajorIdPSeqno = getValue("card.major_id_p_seqno");
        hCardAcnoFlag = getValue("card.acno_flag");
        hCardBinType = getValue("card.bin_type");

        if (debug == 1)
            showLogMessage("I", "", " get card  =[" + hAcnoPSeqno + "]");

        if (hSonCardFlag.length() == 0)
            hSonCreditLmt = 0;

        return (0);
    }

    // ************************************************************************
    public int getAddress() throws Exception {

        extendField = "acno.";
        selectSQL = "bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4||bill_sending_addr5  as bill_sending_addr   , "
                + "vip_code     ,rc_use_indicator  ,class_code     , "
                + "risk_bank_no ,line_of_credit_amt,line_of_credit_amt_cash, "
                + "corp_act_flag,month_purchase_lmt,combo_cash_limit , "
                + "acct_key     ,decode(crt_date , '', '19110000',crt_date) as crt_date ";
        daoTable = "act_acno            ";
        whereStr = "WHERE acno_p_seqno      = ?  ";

        setString(1, hAcnoPSeqno);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_crd_acno      error[notFind]" + mbosCardNo;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hBillAddress = getValue("acno.bill_sending_addr");
        hVipCode = getValue("acno.vip_code");
        hIsRc = getValue("acno.rc_use_indicator");
        hClassCode = getValue("acno.class_code");
        hRiskBankNo = getValue("acno.risk_bank_no");
        hCorpActFlag = getValue("acno.corp_act_flag");
        hLineOfCreditAmt = getValueInt("acno.line_of_credit_amt");
        hLineOfCreditAmtCash = getValueInt("acno.line_of_credit_amt_cash");
        hBillLowLimit = getValueInt("acno.month_purchase_lmt");
        hComboCashLimit = getValueInt("acno.combo_cash_limit");
        hAcctKey = getValue("acno.acct_key");
        hCreateDate = getValue("acno.crt_date");

        if (debug == 1)
            showLogMessage("I", "", " get acno  =[" + hAcnoPSeqno + "]");

        return (0);
    }

    // ************************************************************************
    public int getPinBlock() throws Exception {

        if (getValue("online_mark").compareTo("0") != 0) {
            mbosPvki = hPvki;
            mbosPinBlock = hPinBlock;
        } else {
            hPvki = "";
            hPinBlock = "";
        }

        return (0);
    }

    // ************************************************************************
    public int updateCrdEmboss() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " upd emboss=[" + mbosRecno + "]");

        updateSQL = "in_auth_date      =  ? , " + "mod_pgm           =  ? , "
                  + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        daoTable  = "crd_emboss";
        whereStr  = "where batchno = ? " + "  and recno   = ? ";

        setString(1, sysDate);
        setString(2, javaProgram);
        setString(3, sysDate + sysTime);
        setString(4, mbosBatchno);
        setDouble(5, mbosRecno);

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
        mbosApplyIdCode = "";
        mbosCardNo = "";
        mbosEmbossSource = "";
        mbosEmbossReason = "";
        mbosInMainDate = "";
        mbosGroupCode = "";
        mbosCardType = "";
        mbosAcctType = "";
        mbosOpenNum = "";

        mbosPvki = "";
        mbosPinBlock = "";

        hCardIndicator = "";
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
        hVoiceNum = "";
        hCardSince = "";
        hMsgFlag = "";
        hCompanyName = "";
        hAssetValue = 0;
        hMsgPurchaseAmt = 0;
        hBillAddress = "";
        hVipCode = "";
        hIsRc = "";
        hClassCode = "";
        hRiskBankNo = "";
        hCorpActFlag = "";
        hLineOfCreditAmt = 0;
        hLineOfCreditAmtCash = 0;
        hBillLowLimit = 0;
        hComboCashLimit = 0;
        hAcctKey = "";
        hCreateDate = "";

    }
    // ************************************************************************

} // End of class FetchSample
