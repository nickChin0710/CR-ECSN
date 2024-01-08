/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                         *
*  109/04/07  V1.00.01    JustinWu  modify several methods such as insertions or updates
*  109/05/22  V1.00.02    Wilson    新增market_agree_base                     * 
*  109/06/13  V1.00.03    Wilson    insert_crd_idno_seqno                   *  
*  109/06/22  V1.00.04    Wilson    續卡update dbc_card刪除欄位                                               *
*  109/06/22  V1.00.05    Wilson    VD預設為開卡                                                                                       *
*  109/06/22  V1.00.06    Wilson    續卡update值更                                                                              *
*  109/09/09  V1.00.07    Wilson    開卡相關欄位調整                                                                                *
*  109/09/21  V1.00.08    Wilson    insert crd_idno_seqno 新增debit_idno_flag*
*  109/11/23  V1.00.09    Wilson    拒絕行銷相關欄位邏輯調整                                                                 *
*  109/11/25  V1.00.10    Wilson    新增updCnt                               *
*  109/11/30  V1.00.11    Wilson    測試修改                                                                                                *
*  109/12/10  V1.00.12    Wilson    debit_idno_flag = "Y"                   *
*  109/12/22  V1.01.00  yanghan       修改了變量名稱和方法名稱            *
*  110/06/15  V1.01.01    Wilson    insert dbc_card、dba_acno新增digital_flag *
*  110/06/24  V1.01.02    Wilson    區分branch跟mail_branch                   *
*  112/02/01  V1.01.03    Wilson    dbc_idno、dbc_card增加msg_flag、msg_purchase_amt*
*  112/03/09  V1.01.04    Wilson    update dbc_emboss add check_code        *
*  112/03/15  V1.01.05    Wilson    insert&update dbc_idno add eng_name     *
*  112/03/20  V1.01.06    Wilson    id_code -> id_no_code                   *
*  112/03/31  V1.01.07    Wilson    update dba_acno add chg_addr_date       *
*  112/04/07  V1.01.08    Wilson    調整電子帳單專用郵件信箱處理邏輯                                                   *
*  112/05/11  V1.01.09    Wilson    毀損補發取消檢核舊卡片不為有效卡                                                  *
*  112/08/26  V1.01.10    Wilson    修正弱掃問題                                                                                       *
*  112/11/21  V1.01.11    Wilson    tmpCheckCode = ""                       *
****************************************************************************/

package Dbc;

import com.AccessDAO;
import com.CommCpi;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*VD卡寫入主檔處理程式*/
public class DbcD011 extends AccessDAO {
    private String proganme = "VD卡寫入主檔處理程式  112/11/21 V1.01.11";
    CommFunction   comm   = new CommFunction();
    CommCrd        comc   = new CommCrd();
    CommCpi        comcpi = new CommCpi();
    CommCrdRoutine comcr  = null;

    int debug = 1;
    int debugD = 1;

    String hTempUser = "";
    int totalCnt = 0;
    int sucCnt = 0;
    int failCnt = 0;

    String prgmId = "DbcD011";
    String stderr = "";
    String hModUser = "";
    String hCallBatchSeqno = "";

    String dbcD011 = "";
    String hBusinessDate = "";
    String hDcesBatchno = "";
    double hDcesRecno = 0;
    String hDcesEmbossSource = "";
    String hDcesEmbossReason = "";
    String hDcesSourceBatchno = "";
    String hDcesSourceRecno = "";
    String hDcesApsBatchno = "";
    String hDcesApsRecno = "";
    String hDcesSeqno = "";
    String hDcesToNcccCode = "";
    String hDcesCardType = "";
    String hDceAcctType = "";
    String hDcesAccKey = "";
    String hDceToNcccDate = "";
    String hDcesClassCode = "";
    String hDcesSupFlag = "";
    String hDcesUnitCode = "";
    String hDcesPinBlock = "";
    String hDcesCarNo = "";
    String hDcesMajorCardNo = "";
    String hDcesMajorValidFm = "";
    String hDcesMajorValidTo = "";
    String hDcesMajorChgFlag = "";
    String hDcesOldCardNo = "";
    String hDcesChangeReason = "";
    String hDcesStatusCode = "";
    String hDcesReasonCode = "";
    String hDceMemberNote = "";
    String hDcesApplyId = "";
    String hDcesApplIdCode = "";
    String hDcesPmId = "";
    String hDcesPmIdCode = "";
    String hDcesGroupCode = "";
    String hDcesSourceCode = "";
    String hDcesCorpNo = "";
    String hDcesCorpNoCode = "";
    String hDcesCorpActFlag = "";
    String hDcesCorpAssureFlag = "";
    String hDcesRegBankNo = "";
    String hDcesRiskBankNo = "";
    String hDcesChiName = "";
    String hDcesEngName = "";
    String hDceBirthday = "";
    String hDcesMarriage = "";
    String hDcesRelWithPm = "";
    int hDcesServiceYear = 0;
    String hDcesEducation = "";
    String hDcesNation = "";
    double hDcesSalary = 0;
    String hDcesMailZip = "";
    String hDcesResidentNo = "";
    String hDcesPassportNo = "";
    String hDcesOtherCntryCode = "";
    String hDcesStaffFlag = "";
    String hDcesCreditFlag = "";
    String hDcesCommFlag = "";
    String hDcesMailAddr1 = "";
    String hDcesMailAddr2 = "";
    String hDcesMailAddr3 = "";
    String hDcesMailAddr4 = "";
    String hDcesMailAddr5 = "";
    String hDcesResidenZip = "";
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
    String hDcesActNo = "";
    String hDcesVip = "";
    String hDcesFeeCode = "";
    String hDcesForceFlag = "";
    String hDcesBusinessCode = "";
    String hDcesIntroduceNo = "";
    String hDcesValidFm = "";
    String hDcesValidTo = "";
    String hDcesSex = "";
    double hDcesValue = 0;
    String hDcesAcceptDm = "";
    String hDcesApplyNo = "";
    String hDcesCardcat = "";
    String hDcesMailType = "";
    String hDcesMailNo = "";
    String hDcesIntroduceId = "";
    String hDcesIntroduceName = "";
    String hDcesSalaryCode = "";
    String hDcesStudent = "";
    String hDcesCreditLmt = "";
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
    String hDcesStandardFee = "";
    String hDcesFinalFeeCode = "";
    String hDcesFeeReasonCode = "";
    String hDcesAnnualFee = "";
    String hDcesChgAddrFlag = "";
    String hDcesPvv = "";
    String hDcesCvv = "";
    String hDcesCvv2 = "";
    String hDcesPvki = "";
    String hDcesStmtCycle = "";
    String hDcesRtnNcccDate = "";
    String hDcesOpenNum = "";
    String hDcesVoiceNum = "";
    String hDcesServiceCode = "";
    int hDcesCntlAreaCode = 0;
    String hDcesStockNo = "";
    String hDcesOldBegDate = "";
    String hDcesOldEndDate = "";
    String hDcesEmbossResult = "";
    String hDcesDiffCode = "";
    String hDcesCreditError = "";
    double hDcesAuthCreditLmt = 0;
    String hDcesFailProcCode = "";
    String hDcesCompleteCode = "";
    String hDcesMailCode = "";
    String hDcesAprNote = "";
    String hDcesAprUser = "";
    String hDcesAprDate = "";
    String hDcesChtNum = "";
    String hDcesChtDate = "";
    String hDcesSonCardFlag = "";
    double hDcesIndivCrdLmt = 0;
    String hDcesIcFlag = "";
    String hDcesBranch = "";
    String hDcesMailAttach1 = "";
    String hDcesMailAttach2 = "";
    String hDcesBankActno = "";
    String hDcesCrtDate = "";
    String hDcesModUser = "";
    String hDcesModTime = "";
    String hDcesModPgm = "";
    String hDcesFhFlag = "";
    String hDcesRowid = "";
    String hDcesApplyIbmIdCode = "";
    String hDcesPmIbmIdCode = "";
    String hDcesThirdRsn = "";
    String hDcesThirdRsnIbm = "";
    String hDcesVdcoPcFlag = "";
    String hDcesBinNo = "";
    String hDcesBinType = "";
    String hDcesMailBranch = "";
    
    int cCnt = 0;
    String cCardNo = "";
    String hDccdIndicator = "";
    String hComboIndicator = "";
    String hAcctType = "";
    long hInstCrdtamt = 0;
    double hInstCrdtrate = 0;
    String hUCycleFlag = "";
    String hStmtCycle = "";
    double hPseqIdSeqno = 0;
    String hDccdId = "";
    String hDccdIdCode = "";
    String hDccdIdPSeqno = "";
    String hDccdCorpPSeqno = "";
    String hDccdCorpNo = "";
    String hDccdCorpNoCode = "";
    String hDccdCardType = "";
    String hDccdGroupCode = "";
    String hDccdSourceCode = "";
    String hDccdSupFlag = "";
    String ppCurrentCode = "";
    String hDccdSonCardFlag = "";
    String hDccdMajorRelation = "";
    String hDccdMajorId = "";
    String hDccdMajorIdCode = "";
    String hDccdMajorIdPSeqno = "";
    String hDccdMajorCardNo = "";
    String hDccdMemberNote = "";
    String hDccdMemberId = "";
    String hDccdForceFlag = "";
    String hDccdEngName = "";
    String hDccdRegBankNo = "";
    String hDccdUnitCode = "";
    String hDccdNewBegDate = "";
    String hDccdNewEndDate = "";
    String hDccdEmbossData = "";
    String hDccdBlockStatus = "";
    String hDccdBlockReason = "";
    String hDccdBlockReason2 = "";
    String hDccdBlockDate = "";
    String hDccdAcctType = "";
    String hDccdAcctKey = "";
    String hDccdPSeqno = "";
    String hDccdGpNo = "";
    String hDccdFeeCode = "";
    String hDccdCurrFeeCode = "";
    String hDccdLostFeeCode = "";
    double hDccdIndivCrdLmt = 0;
    double hDccdIndivInstLmt = 0;
    String hDccdExpireReason = "";
    String hDccdExpireChgFlag = "";
    String hDccdExpireChgDate = "";
    String hDccdCorpActFlag = "";
    String hDccdActivateType = "";
    String hDccdActivateFlag = "";
    String hDccdActivateDate = "";
    String hDccdApplyAtmFlag = "";
    String hDccdApplyChtFlag = "";
    String hDccdStmtCycle = "";
    String hDccdAcctNo = "";
    String hDccdComboIndicator = "";
    String hDccdIcFlag = "";
    String hDccdOldBankActno = "";
    String hDccdPromoteDept = "";
    String hDccdPromoteEmpNo = "";
    String hDccdIntroduceId = "";
    String hDccdIntroduceName = "";
    String hDccdIntroduceEmpNo = "";
    String hDccdMsgFlag = "";
    int hDccdMsgPurchaseAmt = 0;
    String hDccdRowid = "";
    String hOldCardNo = "";
    String hPmIdCode = "";
    String hAcctKey = "";
    String hMajorIdPSeqno = "";
    String hMajorCardNo = "";
    String hCorpPSeqno = "";
    String hCorpAcnoPSeqno = "";
    String hCorpCreditActNo = "";
    String hCorpStmtCycle = "";
    String hCorpAcctType = "";
    String hCorpAcctKey = "";
    String hDcioIdPSeqno = "";
    String hIdCode = "";
    String hDcioChiName = "";
    String hFstStmtCycle = "";
    String hDcioCellarPhone = "";
    String hDcioOfficeAreaCode1 = "";
    String hDcioOfficeTelNo1 = "";
    String hDcioOfficeTelExt1 = "";
    String hDcioHomeAreaCode1 = "";
    String hDcioHomeTelNo1 = "";
    String hDcioHomeTelExt1 = "";
    String pPSeqno = "";
    String pGpNo = "";
    String pOrgRiskBankNo = "";
    String pAcnoRegBankNo = "";
    String pAcnoClassCode = "";
    String pAcnoVipCode = "";
    double pAcnoCreditAmt = 0;
    String pAcnoComboAcctNo = "";
    String pAcnoStmtCycle = "";
    String pAcnoCreditActNo = "";
    String pAcnoStopStatus = "";
    String pAcnoNewVdchgFlag = "";
    String pAcnoRowid = "";
    String pAcctType = "";
    String pCorpNo = "";
    String pComboAcctNo = "";
    String hTtttStatSendPaper = "";
    String pAcnoAcctKey = "";
    String pAcnoPSeqno = "";
    String pAcnoGpNo = "";
    String pAcnoCorpActFlag = "";
    String pIdPSeqno = "";
    String pCorpPSeqno = "";
    String maxAcctKey = "";
    double pPseqAcnoSeqno = 0;
    double pPseqActSeqno = 0;
    String pAcctKey = "";
    String pRegBankNo = "";
    String pRiskBankNo = "";
    String pAcctStatus = "";
    String pAcctSubStatus = "";
    String pStmtCycle = "";
    String pAcctHolderId = "";
    String pAcctHolderIdCode = "";
    String pCorpNoCode = "";
    String pCreditActNo = "";
    String pCardIndicator = "";
    String pFCurrencyFlag = "";
    double pCreditAmt = 0;
    double pMonthPurchaseLmt = 0;
    String pRcBAdj = "";
    String pRcUseIndicator = "";
    String pVipCode = "";
    String pClassCode = "";
    String pAcceptDm = "";
    String pCorpAssureFlag = "";
    String pCorpActFlag = "";
    String pAutopayIndicator = "";
    String pWorseMcode = "";
    String pLegalDelayCode = "";
    String pMailZip = "";
    String pMailAddr1 = "";
    String pMailAddr2 = "";
    String pMailAddr3 = "";
    String pMailAddr4 = "";
    String pMailAddr5 = "";
    String pNoTelCollFlag = "";
    double pInstAuthLocAmt = 0;
    String hStatSendPaper = "";
    String hStatSendInternet = "";
    String hNewVdchgFlag = "";
    long pComboSeqno = 0;
    String pComboRowid = "";
    double cashAmt = 0;
    String pCardNo = "";
    String pIdno = "";
    double pDrAmt = 0;
    double pCrAmt = 0;
    double jCashAmt = 0;
    String sqlSt = "";
    String pAdjLoFlag = "";
    double pAcnoCreditAmtCash = 0;
    double hTempLineOfCreditAmtCash = 0;
    long pPercentc = 0;
    long pLfixC = 0;
    String tDay = "";
    String tStmtCycle = "";
    String hTempOldBankActno = "";
    String hTempOldBegDate = "";
    String hTempOldEndDate = "";
    String hDccdOldCardNo = "";
    String hDccdCardNo = "";
    String hDccdUrgentFlag = "";
    String hDccdCurrentCode = "";
    String hDccdPinBlock = "";
    String hDccdIssueDate = "";
    String hDccdReissueDate = "";
    String hDccdReissueReason = "";
    String hDccdChangeDate = "";
    String hDccdUpgradeDate = "";
    String hDccdApplyNo = "";
    String hDccdProdNo = "";
    String hDccdPvv = "";
    String hDccdCvv = "";
    String hDccdCvv2 = "";
    String hDccdPvki = "";
    String hDccdBatchno = "";
    double hDccdRecno = 0;
    String hDccdOppostReason = "";
    String hDccdOppostDate = "";
    String hDccdNewCardNo = "";
    String hDccdOldActivateType = "";
    String hDccdOldActivateFlag = "";
    String hDccdOldActivateDate = "";
    String hDccdEmergentFlag = "";
    String hDccdSetCode = "";
    String hDccdMailType = "";
    String hDccdMailNo = "";
    String hDccdStockNo = "";
    String hDccdBranch = "";
    String hDccdBankActno = "";
    String hDccdMailAttach1 = "";
    String hDccdMailAttach2 = "";
    String hDccdCrtDate = "";
    String hDccdCrtUser = "";
    String hDccdAprDate = "";
    String hDccdAprUser = "";
    double hDccdModSeqno = 0;
    String hDccdIbmIdCode = "";
    String hPvki = "";
    String hCvv2 = "";
    String hPinBlock = "";
    String hServiceCode = "";
    String hDcioId = "";
    String hDcioIdCode = "";
    String hDcioIbmIdCode = "";
    String hDcioStaffFlag = "";
    String hDcioStaffBrNo = "";
    String hDcioCreditFlag = "";
    String hDcioCommFlag = "";
    String hDcioSalaryCode = "";
    String hDcioSex = "";
    String hDcioBirthday = "";
    String hDcioMarriage = "";
    String hDcioEducation = "";
    String hDcioStudent = "";
    String hDcioNation = "";
    int hDcioServiceYear = 0;
    double hDcioAssetValue = 0;
    String hTempCorpNo = "";
    double hDcioAnnualIncome = 0;
    String hDcioResidentNo = "";
    String hDcioPassportNo = "";
    String hDcioOtherCntryCode = "";
    String hDcioOfficeAreaCode2 = "";
    String hDcioOfficeTelNo2 = "";
    String hDcioOfficeTelExt2 = "";
    String hDcioHomeAreaCode2 = "";
    String hDcioHomeTelNo2 = "";
    String hDcioHomeTelExt2 = "";
    String hDcioResidentZip = "";
    String hDcioResidentAddr1 = "";
    String hDcioResidentAddr2 = "";
    String hDcioResidentAddr3 = "";
    String hDcioResidentAddr4 = "";
    String hDcioResidentAddr5 = "";
    String hDcioJobPosition = "";
    String hDcioCompanyName = "";
    String hDcioBusinessCode = "";
    String hDcioBbCall = "";
    String hDcioFaxNo = "";
    String hDcioEMailAddr = "";
    String hDcioCardSince = "";
    String hDcioVoiceNum = "";
    String hDcioFstStmtCycle = "";
    String hDcioCrtDate = "";
    String hDcioCrtUser = "";
    String hDcioAprDate = "";
    String hDcioAprUser = "";
    String hDcioENews = "";
    String hDcioAcceptMbullet = "";
    String hDcioAcceptCallSell = "";
    String hDcioAcceptDm = "";
    String hDcioDmFromMark = "";
    String hDcioDmChgDate = "";
    String hDcioAcceptSms = "";
    String hDcioModUser = "";
    String hDcioModPgm = "";
    String hIdnoENews = "";
    String hIdnoAcceptMbullet = "";
    String hIdnoAcceptCallSell = "";
    String hIdnoAcceptDm = "";
    String hIdnoDmFromMark = "";
    String hIdnoDmChgDate = "";
    String hIdnoAcceptSms = "";
    String majorCardno = "";
    String hDccdReissueStatus = "";
    String tempSource = "";
    String hDccdChangeStatus = "";
    String hDccdUpgradeStatus = "";
    String chkMajorCardno = "";
    String chkOldMajorCardno = "";
    String chkSupCardno = "";
    String chkNewMajorCardno = "";
    String hDccdOldBegDate = "";
    String hDccdOldEndDate = "";
    String hDccdMailBranch = "";
    String pCorpAcnoPSeqno = "";
    String idnoMarketAgreeBase = "";
    String idnoAcceptCallSell = "";
    String idnoCallSellFromMark = "";
    String idnoCallSellChgDate = "";
    String idnoENews = "";
    String idnoENewsFromMark = "";
    String idnoENewsChgDate = "";
    String idnoAcceptDm = "";
    String idnoDmFromMark = "";
    String idnoDmChgDate = "";
    String idnoAcceptSms = "";
    String idnoSmsFromMark = "";
    String idnoSmsChgDate = "";
    
    
    int dCount = 0;
    int addrLen = 0;
    String hIdRowid = "";
    int recCount = 0;
    String gClassCode = "";
    int tempInt = 0;
    String hIdnoId = "";
    String hIdnoIdCode = "";
    String hIdnoBirthday = "";
    String hIdnoChiName = "";
    String hIdnoHomeAreaCode1 = "";
    String hIdnoOfficeAreaCode1 = "";
    String hIdnoHomeTelNo1 = "";
    String hIdnoOfficeTelNo1 = "";
    String hIdnoCellarPhone = "";
    String hDcesInMainError = "";
    String hDcesInMainMsg = "";
    int hDcesCashLmt = 0;
    String hDcesInMainDate = "";
    String hOldMajorCardNo = "";
    String hDccdMailProcDate = "";
    int pComboCashLimit = 0;
    String pComboIndicator = "";
    String hRcUseFlag = "";
    int pAcnoComboCashLimit = 0;
    String hIntIdnoIdPSeqno ="";

    String pSupFlag = "";
    String pActNo = "";
    String begDate = "";
    String procType = "";
    String errorMsg = "";
    String hDccdModUser = "";
    String hDccdModPgm = "";
    int hCorpCreditLimit = 0;
    int rtnCode = 0;
    int delPinFlag = 0;
    int totCnt = 0;
    int totcnt = 0;
    int code = 0;
    int total1 = 0;
    int total2 = 0;
    int total3 = 0;
    int total4 = 0;
    int total5 = 0;
    int total6 = 0;
    int total7 = 0;
    int validFlag = 0;
    int dup1 = 0;
    int chk = 0;
    int crdCnt = 0;
    int idnoCnt = 0;
    long actSeqno = 0;
    String tmpCheckCode = "";
    
    private String hIdnoIdPSeqno = "";
    private String dbcEmbossSpouseName= "";
    private String dbcEmbossSpouseIdNo= "";
    private String dbcEmbossSpouseBirthday= "";
    private String dbcEmbossResidentNoExpireDate= "";
    private String dbcEmbossGraduationElementarty= "";
    private String dbcEmbossUrFlag= "";
    private String dbcEmbossCompanyZip= "";
    private String dbcEmbossCompanyAddr1= "";
    private String dbcEmbossCompanyAddr2= "";
    private String dbcEmbossCompanyAddr3= "";
    private String dbcEmbossCompanyAddr4= "";
    private String dbcEmbossCompanyAddr5= "";
	private String dbcEmbossCrtBankNo= "";
	private String dbcEmbossVdBankNo= "";
	private String dbcEmbossCardRefNum= "";
	private String dbcEmbossElectronicCode= "";
	private String dbcEmbossStatSendInternet = "";
	private String dbcEmbossBilApplyFlag = "";
	private String dbcEmbossMarketAgreeBase = "";
	private String dbcEmbossENews = "";
	private String dbcEmbossAcceptCallSell = "";
	private String dbcEmbossAcceptDm = "";
	private String dbcEmbossAcceptSms = "";
	private String dbcEmbossDigitalFlag = "";
	

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + proganme);
            // =====================================
            if (args.length >  1) {
                comc.errExit("Usage : DbcD011 flag", "flag==> 0: 一般 1: online緊急製卡 ");
            }
            procType = args[0];
            if ((procType.equals("0") == false) && (procType.equals("1") == false)) {
                comc.errExit("Usage : dbc_d011 資料錯誤 ex:dbc_d011 0", "");
            }
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

            dataPrepare();

            process();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,總處理筆數=[" + totalCnt + "],成功筆數=[" + sucCnt + "],失敗筆數=[" + failCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void dataPrepare() throws Exception {
        hModUser = comc.commGetUserID();
    }

    /***********************************************************************/
    void process() throws Exception {
        begDate = sysDate;
        begDate = String.format("%6.6s01", begDate);

        getBusinessDay();

        fetchDetail();
        return;
    }

    /***********************************************************************/
    void getBusinessDay() throws Exception {
        hBusinessDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += "  from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void fetchDetail() throws Exception {
        /* 取得申請書暫存檔資料(一次多筆) */
        /****************************************************************/
        /* 1:新製卡 2:普昇金卡 4:換卡 5:毀損重製 6:掛失補發 */
        /* 7:緊急補發卡 8:星座卡毀損重製9:重送件 */
        /****************************************************************/
        /* check emboss_source */
        String code = "";

        sqlCmd  = "select ";
        sqlCmd += "a.batchno,";
        sqlCmd += "a.recno,";
        sqlCmd += "a.emboss_source,";
        sqlCmd += "a.emboss_reason,";
        sqlCmd += "a.source_batchno,";
        sqlCmd += "a.source_recno,";
        sqlCmd += "a.aps_batchno,";
        sqlCmd += "a.aps_recno,";
        sqlCmd += "a.seqno,";
        sqlCmd += "a.to_nccc_code,";
        sqlCmd += "a.card_type,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.to_nccc_date,";
        sqlCmd += "a.class_code,";
        sqlCmd += "a.sup_flag,";
        sqlCmd += "a.unit_code,";
        sqlCmd += "a.pin_block,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.major_card_no,";
        sqlCmd += "a.major_valid_fm,";
        sqlCmd += "a.major_valid_to,";
        sqlCmd += "a.major_chg_flag,";
        sqlCmd += "a.old_card_no,";
        sqlCmd += "a.change_reason,";
        sqlCmd += "a.status_code,";
        sqlCmd += "a.reason_code,";
        sqlCmd += "a.member_note,";
        sqlCmd += "a.apply_id,";
        sqlCmd += "decode(a.apply_id_code,'','0',a.apply_id_code) h_dces_apply_id_code,";
        sqlCmd += "a.pm_id,";
        sqlCmd += "decode(a.pm_id_code,'','0',a.pm_id_code) h_dces_pm_id_code,";
        sqlCmd += "a.group_code,";
        sqlCmd += "a.source_code,";
        sqlCmd += "a.corp_no,";
        sqlCmd += "decode(a.corp_no_code,'','0',a.corp_no_code) h_dces_corp_no_code,";
        sqlCmd += "decode(a.corp_act_flag,'','N',a.corp_act_flag) h_dces_corp_act_flag,";
        sqlCmd += "a.corp_assure_flag,";
        sqlCmd += "a.reg_bank_no,";
        sqlCmd += "a.risk_bank_no,";
        sqlCmd += "a.chi_name,";
        sqlCmd += "a.eng_name,";
        sqlCmd += "a.birthday,";
        sqlCmd += "a.marriage,";
        sqlCmd += "a.rel_with_pm,";
        sqlCmd += "a.service_year,";
        sqlCmd += "a.education,";
        sqlCmd += "a.nation,";
        sqlCmd += "a.salary,";
        sqlCmd += "a.mail_zip,";
        sqlCmd += "a.resident_no,";
        sqlCmd += "a.passport_no,";
        sqlCmd += "a.other_cntry_code,";
        sqlCmd += "a.staff_flag,";
        sqlCmd += "a.credit_flag,";
        sqlCmd += "a.comm_flag,";
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
        sqlCmd += "a.e_mail_addr,";
        sqlCmd += "a.cellar_phone,";
        sqlCmd += "a.act_no,";
        sqlCmd += "a.vip,";
        sqlCmd += "a.fee_code,";
        sqlCmd += "a.force_flag,";
        sqlCmd += "a.business_code,";
        sqlCmd += "a.introduce_no,";
        sqlCmd += "a.valid_fm,";
        sqlCmd += "a.valid_to,";
        sqlCmd += "a.sex,";
        sqlCmd += "a.value,";
        sqlCmd += "a.accept_dm,";
        sqlCmd += "a.apply_no,";
        sqlCmd += "a.cardcat,";
        sqlCmd += "a.mail_type,";
        sqlCmd += "a.mail_no,";
        sqlCmd += "a.introduce_id,";
        sqlCmd += "a.introduce_name,";
        sqlCmd += "a.salary_code,";
        sqlCmd += "a.student,";
        sqlCmd += "a.credit_lmt,";
        sqlCmd += "a.police_no1,";
        sqlCmd += "a.police_no2,";
        sqlCmd += "a.police_no3,";
        sqlCmd += "a.pm_cash,";
        sqlCmd += "a.sup_cash,";
        sqlCmd += "decode(a.online_mark,'','0',a.online_mark) h_dces_online_mark,";
        sqlCmd += "a.error_code,";
        sqlCmd += "a.reject_code,";
        sqlCmd += "a.emboss_4th_data,";
        sqlCmd += "a.member_id,";
        sqlCmd += "a.pm_birthday,";
        sqlCmd += "a.sup_birthday,";
        sqlCmd += "a.standard_fee,";
        sqlCmd += "a.final_fee_code,";
        sqlCmd += "a.fee_reason_code,";
        sqlCmd += "a.annual_fee,";
        sqlCmd += "a.chg_addr_flag,";
        sqlCmd += "a.pvv,";
        sqlCmd += "a.cvv,";
        sqlCmd += "a.trans_cvv2,";
        sqlCmd += "a.pvki,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "a.rtn_nccc_date,";
        sqlCmd += "a.open_passwd,";
        sqlCmd += "a.voice_passwd,";
        sqlCmd += "a.service_code,";
        sqlCmd += "a.cntl_area_code,";
        sqlCmd += "a.stock_no,";
        sqlCmd += "a.old_beg_date,";
        sqlCmd += "a.old_end_date,";
        sqlCmd += "a.emboss_result,";
        sqlCmd += "a.diff_code,";
        sqlCmd += "a.credit_error,";
        sqlCmd += "a.auth_credit_lmt,";
        sqlCmd += "a.fail_proc_code,";
        sqlCmd += "a.complete_code,";
        sqlCmd += "a.mail_code,";
        sqlCmd += "a.apr_note,";
        sqlCmd += "a.apr_user,";
        sqlCmd += "a.apr_date,";
        sqlCmd += "a.cht_passwd,";
        sqlCmd += "a.cht_date,";
        sqlCmd += "a.son_card_flag,";
        sqlCmd += "a.indiv_crd_lmt,";
        sqlCmd += "a.ic_flag,";
        sqlCmd += "a.branch,";
        sqlCmd += "a.mail_attach1,";
        sqlCmd += "a.mail_attach2,";
        sqlCmd += "a.bank_actno,";
        sqlCmd += "a.crt_date,";
        sqlCmd += "a.mod_user,";
        sqlCmd += "a.mod_time,";
        sqlCmd += "a.mod_pgm,";
        sqlCmd += "a.fh_flag,";
        sqlCmd += "a.rowid  as rowid,";
        sqlCmd += "a.apply_ibm_id_code,";
        sqlCmd += "a.pm_ibm_id_code,";
        sqlCmd += "a.third_rsn,";
        sqlCmd += "a.third_rsn_ibm,";
        sqlCmd += "a.vdco_pc_flag, ";
        // =JustinWu: add in 2020/04/08=
        sqlCmd += "a.spouse_name, ";
        sqlCmd += "a.spouse_id_no, ";
        sqlCmd += "a.spouse_birthday, ";
        sqlCmd += "a.resident_no_expire_date, ";
        sqlCmd += "a.graduation_elementarty, ";
        sqlCmd += "a.ur_flag, ";
        sqlCmd += "a.company_zip, ";
        sqlCmd += "a.company_addr1, ";
        sqlCmd += "a.company_addr2, ";
        sqlCmd += "a.company_addr3, ";
        sqlCmd += "a.company_addr4, ";
        sqlCmd += "a.company_addr5, ";
        sqlCmd += "a.crt_bank_no, ";
        sqlCmd += "a.vd_bank_no, ";
        sqlCmd += "a.card_ref_num, ";
        sqlCmd += "a.electronic_code, ";
        sqlCmd += "a.stat_send_internet, ";
        sqlCmd += "a.bill_apply_flag, ";
        sqlCmd += "a.market_agree_base, ";
        sqlCmd += "a.e_news, ";
        sqlCmd += "a.mail_branch, ";
        //====================
        sqlCmd += "decode(a.digital_flag ,'','N',a.digital_flag)  as digital_flag  , ";
        sqlCmd += "d.bin_no      ,";
        sqlCmd += "d.bin_type     ";
        sqlCmd += " from ptr_bintable d, dbc_emboss a, dbp_acct_type b ";
        sqlCmd += "where a.card_no      <> '' ";
        sqlCmd += "  and a.reject_code   = '' ";
        sqlCmd += "  and a.in_main_date  = '' ";
        sqlCmd += "  and b.acct_type     = a.acct_type ";
     // sqlCmd += "  and c.acct_type     = a.acct_type ";
        sqlCmd += "  and d.bin_no || d.bin_no_2_fm || '0000' <= a.card_no ";
        sqlCmd += "  and d.bin_no || d.bin_no_2_to || '9999' >= a.card_no ";
        sqlCmd += "  and decode(a.unmark_code,'','N',a.unmark_code) <> 'Y' ";
        sqlCmd += "  and a.apply_id     <> '' ";
        sqlCmd += "  and a.birthday     <> '' ";
        sqlCmd += "order by a.card_type,a.group_code,a.sup_flag,a.batchno,a.recno ";
        int recordCnt = selectTable();
        if(debug == 1) 
        	showLogMessage("I", "", "888 Main cnt=["+recordCnt + "]");
        
        
        for (int i = 0; i < recordCnt; i++) {
            hDcesBatchno           = getValue("batchno", i);
            hDcesRecno             = getValueDouble("recno", i);
            hDcesEmbossSource     = getValue("emboss_source", i);
            hDcesEmbossReason     = getValue("emboss_reason", i);
            hDcesSourceBatchno    = getValue("source_batchno", i);
            hDcesSourceRecno      = getValue("source_recno", i);
            hDcesApsBatchno       = getValue("aps_batchno", i);
            hDcesApsRecno         = getValue("aps_recno", i);
            hDcesSeqno             = getValue("seqno", i);
            hDcesToNcccCode      = getValue("to_nccc_code", i);
            hDcesCardType         = getValue("card_type", i);
            hDceAcctType         = getValue("acct_type", i);
            hDcesAccKey          = getValue("acct_key", i);
            hDceToNcccDate      = getValue("to_nccc_date", i);
            hDcesClassCode        = getValue("class_code", i);
            hDcesSupFlag          = getValue("sup_flag", i);
            hDcesUnitCode         = getValue("unit_code", i);
            hDcesPinBlock         = getValue("pin_block", i);
            hDcesCarNo           = getValue("card_no", i);
            hDcesMajorCardNo     = getValue("major_card_no", i);
            hDcesMajorValidFm    = getValue("major_valid_fm", i);
            hDcesMajorValidTo    = getValue("major_valid_to", i);
            hDcesMajorChgFlag    = getValue("major_chg_flag", i);
            hDcesOldCardNo       = getValue("old_card_no", i);
            hDcesChangeReason     = getValue("change_reason", i);
            hDcesStatusCode       = getValue("status_code", i);
            hDcesReasonCode       = getValue("reason_code", i);
            hDceMemberNote       = getValue("member_note", i);
            hDcesApplyId          = getValue("apply_id", i);
            hDcesApplIdCode     = getValue("h_dces_apply_id_code", i);
            hDcesPmId             = getValue("pm_id", i);
            hDcesPmIdCode        = getValue("h_dces_pm_id_code", i);
            hDcesGroupCode        = getValue("group_code", i);
            hDcesSourceCode       = getValue("source_code", i);
            hDcesCorpNo           = getValue("corp_no", i);
            hDcesCorpNoCode      = getValue("h_dces_corp_no_code", i);
            hDcesCorpActFlag     = getValue("h_dces_corp_act_flag", i);
            hDcesCorpAssureFlag  = getValue("corp_assure_flag", i);
            hDcesRegBankNo       = getValue("reg_bank_no", i);
            hDcesRiskBankNo      = getValue("risk_bank_no", i);
            hDcesChiName          = getValue("chi_name", i);
            hDcesEngName          = getValue("eng_name", i);
            hDceBirthday          = getValue("birthday", i);
            hDcesMarriage          = getValue("marriage", i);
            hDcesRelWithPm       = getValue("rel_with_pm", i);
            hDcesServiceYear      = getValueInt("service_year", i);
            hDcesEducation         = getValue("education", i);
            hDcesNation            = getValue("nation", i);
            hDcesSalary            = getValueDouble("salary", i);
            hDcesMailZip          = getValue("mail_zip", i);
            hDcesResidentNo       = getValue("resident_no", i);
            hDcesPassportNo       = getValue("passport_no", i);
            hDcesOtherCntryCode  = getValue("a.other_cntry_code", i);
            hDcesStaffFlag        = getValue("staff_flag", i);
            hDcesCreditFlag       = getValue("credit_flag", i);
            hDcesCommFlag         = getValue("comm_flag", i);
            hDcesMailAddr1        = getValue("mail_addr1", i);
            hDcesMailAddr2        = getValue("mail_addr2", i);
            hDcesMailAddr3        = getValue("mail_addr3", i);
            hDcesMailAddr4        = getValue("mail_addr4", i);
            hDcesMailAddr5        = getValue("mail_addr5", i);
            hDcesResidenZip      = getValue("resident_zip", i);
            hDcesResidentAddr1    = getValue("resident_addr1", i);
            hDcesResidentAddr2    = getValue("resident_addr2", i);
            hDcesResidentAddr3    = getValue("resident_addr3", i);
            hDcesResidentAddr4    = getValue("resident_addr4", i);
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
            hDcesEMailAddr       = getValue("e_mail_addr", i);
            hDcesCellarPhone      = getValue("cellar_phone", i);
            hDcesActNo            = getValue("act_no", i);
            hDcesVip               = getValue("vip", i);
            hDcesFeeCode          = getValue("fee_code", i);
            hDcesForceFlag        = getValue("force_flag", i);
            hDcesBusinessCode     = getValue("business_code", i);
            hDcesIntroduceNo      = getValue("introduce_no", i);
            hDcesValidFm          = getValue("valid_fm", i);
            hDcesValidTo          = getValue("valid_to", i);
            hDcesSex               = getValue("sex", i);
            hDcesValue             = getValueDouble("value", i);
            hDcesAcceptDm         = getValue("accept_dm", i);
            hDcesApplyNo          = getValue("apply_no", i);
            hDcesCardcat           = getValue("cardcat", i);
            hDcesMailType         = getValue("mail_type", i);
            hDcesMailNo           = getValue("mail_no", i);
            hDcesIntroduceId      = getValue("introduce_id", i);
            hDcesIntroduceName    = getValue("introduce_name", i);
            hDcesSalaryCode       = getValue("salary_code", i);
            hDcesStudent           = getValue("student", i);
            hDcesCreditLmt        = getValue("credit_lmt", i);
            hDcesPoliceNo1        = getValue("police_no1", i);
            hDcesPoliceNo2        = getValue("police_no2", i);
            hDcesPoliceNo3        = getValue("police_no3", i);
            hDcesPmCash           = getValue("pm_cash", i);
            hDcesSupCash          = getValue("sup_cash", i);
            hDcesOnlineMark       = getValue("h_dces_online_mark", i);
            hDcesErrorCode        = getValue("error_code", i);
            hDcesRejectCode       = getValue("reject_code", i);
            hDcesEmboss4thData   = getValue("emboss_4th_data", i);
            hDcesMemberId         = getValue("member_id", i);
            hDcesPmBirthday       = getValue("pm_birthday", i);
            hDcesSupBirthday      = getValue("sup_birthday", i);
            hDcesStandardFee      = getValue("standard_fee", i);
            hDcesFinalFeeCode    = getValue("final_fee_code", i);
            hDcesFeeReasonCode   = getValue("fee_reason_code", i);
            hDcesAnnualFee        = getValue("annual_fee", i);
            hDcesChgAddrFlag     = getValue("chg_addr_flag", i);
            hDcesPvv               = getValue("pvv", i);
            hDcesCvv               = getValue("cvv", i);
            hDcesCvv2              = getValue("trans_cvv2", i);
            hDcesPvki              = getValue("pvki", i);
            hDcesStmtCycle        = getValue("stmt_cycle", i);
            hDcesRtnNcccDate     = getValue("rtn_nccc_date", i);
            hDcesOpenNum       = getValue("open_passwd", i);
            hDcesVoiceNum      = getValue("voice_passwd", i);
            hDcesServiceCode      = getValue("service_code", i);
            hDcesCntlAreaCode    = getValueInt("cntl_area_code", i);
            hDcesStockNo          = getValue("stock_no", i);
            hDcesOldBegDate      = getValue("old_beg_date", i);
            hDcesOldEndDate      = getValue("old_end_date", i);
            hDcesEmbossResult     = getValue("emboss_result", i);
            hDcesDiffCode         = getValue("diff_code", i);
            hDcesCreditError      = getValue("credit_error", i);
            hDcesAuthCreditLmt   = getValueDouble("auth_credit_lmt", i);
            hDcesFailProcCode    = getValue("fail_proc_code", i);
            hDcesCompleteCode     = getValue("complete_code", i);
            hDcesMailCode         = getValue("mail_code", i);
            hDcesAprNote          = getValue("apr_note", i);
            hDcesAprUser          = getValue("apr_user", i);
            hDcesAprDate          = getValue("apr_date", i);
            hDcesChtNum        = getValue("cht_passwd", i);
            hDcesChtDate          = getValue("cht_date", i);
            hDcesSonCardFlag     = getValue("son_card_flag", i);
            hDcesIndivCrdLmt     = getValueDouble("indiv_crd_lmt", i);
            hDcesIcFlag           = getValue("ic_flag", i);
            hDcesBranch            = getValue("branch", i);
            hDcesMailAttach1      = getValue("mail_attach1", i);
            hDcesMailAttach2      = getValue("mail_attach2", i);
            hDcesBankActno        = getValue("bank_actno", i);
            hDcesCrtDate          = getValue("crt_date", i);
            hDcesModUser          = getValue("mod_user", i);
            hDcesModTime          = getValue("mod_time", i);
            hDcesModPgm           = getValue("mod_pgm", i);
            hDcesFhFlag           = getValue("fh_flag", i);
            hDcesRowid             = getValue("rowid", i);
            hDcesApplyIbmIdCode = getValue("apply_ibm_id_code", i);
            hDcesPmIbmIdCode    = getValue("pm_ibm_id_code", i);
            hDcesThirdRsn         = getValue("third_rsn", i);
            hDcesThirdRsnIbm     = getValue("third_rsn_ibm", i);
            hDcesVdcoPcFlag      = getValue("vdco_pc_flag", i);
            hDcesBinNo            = getValue("bin_no", i);
            hDcesBinType          = getValue("bin_type", i);
            // =JustinWu: add in 2020/04/08=
            dbcEmbossSpouseName = getValue("spouse_name", i);
            dbcEmbossSpouseIdNo = getValue("spouse_id_no", i);
            dbcEmbossSpouseBirthday = getValue("spouse_birthday", i);
            dbcEmbossResidentNoExpireDate = getValue("resident_no_expire_date", i);
            dbcEmbossGraduationElementarty = getValue("graduation_elementarty", i);
            dbcEmbossUrFlag = getValue("ur_flag", i);
            dbcEmbossCompanyZip = getValue("company_zip", i);
            dbcEmbossCompanyAddr1 = getValue("company_addr1", i);
            dbcEmbossCompanyAddr2 = getValue("company_addr2", i);
            dbcEmbossCompanyAddr3 = getValue("company_addr3", i);
            dbcEmbossCompanyAddr4 = getValue("company_addr4", i);
            dbcEmbossCompanyAddr5 = getValue("company_addr5", i);
            dbcEmbossCrtBankNo = getValue("crt_bank_no", i);
            dbcEmbossVdBankNo = getValue("vd_bank_no", i);
            dbcEmbossCardRefNum = getValue("card_ref_num", i);
            dbcEmbossElectronicCode = getValue("electronic_code", i);
            dbcEmbossStatSendInternet = getValue("stat_send_internet", i);
            dbcEmbossBilApplyFlag = getValue("bill_apply_flag", i);
            dbcEmbossMarketAgreeBase = getValue("market_agree_base", i);
            dbcEmbossENews = getValue("e_news", i); 
            //====================
            dbcEmbossDigitalFlag = getValue("digital_flag", i);
            hDcesMailBranch      = getValue("mail_branch", i);
            
            System.out.println("dbc_emboss_market_agree_base1 = " + dbcEmbossMarketAgreeBase);
            
            tmpCheckCode = "";

            totCnt++;
            totalCnt++;
            if (totCnt % 500 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format(" Process cnt=[%d]", totCnt));

            if (debug == 1) {
            	showLogMessage("I", "", "");
                showLogMessage("I", "", "888 Read bat=["+hDcesBatchno + "]" + hDcesAprNote);
                showLogMessage("I", "", "     rtn_ncc=["+hDcesRtnNcccDate+"]"+hDcesCarNo);
            }

            /*****************************************************
             * 轉大寫
             ******************************************************/
            hDcesEngName = comc.commAdjEngname(hDcesEngName);
            /*****************************************************
             * 若線上proc_type='1'(緊急新製卡),且online_mark='2', 則不需有 rtn_nccc_date(回饋日期),
             * 但一般情形,需有rtn_nccc_date(回饋日期)
             ******************************************************/
            delPinFlag = 0;
            if (procType.equals("1")) {
                if (!hDcesOnlineMark.equals("2")) {
                    continue;
                }
            } else /* 需有回饋日期 */
            {
                if (hDcesRtnNcccDate.length() <= 0) {
                    continue;
                }
                /* 不做緊急新製卡 */
                if (hDcesOnlineMark.equals("2")) {
                    continue;
                }
            }
            
            if (hDcesPinBlock.length() == 0) {
            	rtnCode = getPinData();
                if (rtnCode != 0) {
                    rtnCode = 1;
                    tmpCheckCode = "E01";
                    errorMsg = String.format("請先產生PVV");
                    updateErrorEmboss();
                    continue;
                }
            }
            if (hDcesAprNote.equals("Y")) {
                rtnCode = 1;
                tmpCheckCode = "E02";
                errorMsg = String.format("需主管放行");
                updateErrorEmboss();
                continue;
            }
            totcnt++;
            errorMsg = "";
            code = hDcesEmbossSource;
            rtnCode = 0;

            if (debug == 1)
                showLogMessage("I", "", 
                		" 888 card_no: " + hDcesCarNo + ", emboss_src: " + code + ", emboss_reason: " +hDcesEmbossReason);

            hStatSendPaper = "N";
            hStatSendInternet = "N";
            if (hDcesVdcoPcFlag.equals("Q")) {
                hStatSendPaper = "N";
                hStatSendInternet = "N";
            } else if (hDcesVdcoPcFlag.equals("G")) {
                hStatSendPaper = "Y";
                hStatSendInternet = "Y";
            } else if (hDcesVdcoPcFlag.equals("E")) {
                hStatSendPaper = "N";
                hStatSendInternet = "Y";
            } else if (hDcesVdcoPcFlag.equals("P")) {
                hStatSendPaper = "Y";
                hStatSendInternet = "N";
            }
            switch (Integer.parseInt(code)) {
            case 1: /* 新製卡 */
                total1++;
                if (checkCardNo(hDcesCarNo) == 0) {
                    rtnCode = procNewCard();
                }
                break;
            case 2: /* 普升金卡 */
                total2++;
                if (checkCardNo(hDcesCarNo) == 0) {
                    rtnCode = procUpgradeCard();
                }
                break;
            case 3:
            case 4: /* 換卡 */
                total4++;
                /* 信用卡 不換卡號  */
                rtnCode=procChgCard();
                /* JustinWu 20200408
                if ((rtn_code = check_card_no(h_dces_card_no)) == 0) {
                    rtn_code = proc_new_card();
                    h_dccd_new_card_no   = h_dces_card_no;
                    h_dccd_change_date   = sysDate;
                    h_dccd_change_status = "3";
                    update_old_card(1);
                }
                */
                break;
            case 5: /* 5:重製 */
                total5++;
                if (! hDcesCarNo.equals(hDcesOldCardNo)) {
                    rtnCode = checkCardNo(hDcesCarNo);
                }
                if (rtnCode == 0) {
                    rtnCode = procReissueCard();
                }
                break;
            case 7: /* 緊急補發卡 */
                total7++;
                rtnCode = checkCardNo(hDcesCarNo);
                if (rtnCode == 0) {
                    rtnCode = procUrgentCard();
                }
                break;
            default: /* 星座卡毀損重製 */
                break;
            }
            if (rtnCode == 0) {
                if (hDcesEmbossSource.compareTo("2") >= 0) {
                }
                if (delPinFlag == 1) {
                    rtnCode = delPinBlock();
                }
            }
            
            if(tmpCheckCode.length() == 0) {
            	sucCnt++;
            }
            else {
            	failCnt++;
            }

            if (hDcesFhFlag.equals("Y")) {
                updateEmboss2();
                updateDbaAcno2();
            } else {
                updateEmboss();
            }
            
            commitDataBase();
        }
    }

    private int procChgCard() throws Exception {
    	int rtn = 0 ;
    	hOldCardNo =  hDcesOldCardNo;
        // init_crd_card();
    	rtn = getCardData();
    	
        if (rtn != 0)
            return (rtn);

        hDccdOldActivateType = hDccdActivateType;
        hDccdOldActivateFlag = hDccdActivateFlag;
        hDccdOldActivateDate = hDccdActivateDate;
        hDccdActivateType = "";
        hDccdActivateFlag = "1";
        hDccdActivateDate = "";
        hDccdOldBegDate = hDccdNewBegDate;
        hDccdOldEndDate = hDccdNewEndDate;
        hDccdNewBegDate = getValue("valid_fm");
        hDccdNewEndDate = getValue("valid_to");
        hDccdChangeDate = sysDate;
        hDccdChangeStatus = "3";
        hDccdBatchno = hDcesBatchno;
        hDccdRecno = hDcesRecno;
        hDccdMailType = getValue("mail_type");
        hDccdBankActno = getValue("bank_actno");
        hDccdMailBranch = getValue("mail_branch");
        hDccdBranch = getValue("branch");

        rtn = updateChgCard(2);
        if (rtn != 0)
            return (rtn);

		return 0;
	}

	private int updateChgCard(int type) throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "  888 update dbc_card " + hDcesOldCardNo + "]");
	        hDccdCvv2 = hDcesCvv2;
	        hDccdPvki = hDcesPvki;
	        hDccdPinBlock = hDcesPinBlock;
	        hDccdIcFlag = getValue("ic_flag");
	        if (hDccdPinBlock.length() == 0) {
	        	hDccdPinBlock = hPinBlock;
	        	hDccdCvv2 = hCvv2;
	        	hDccdPvki = hPvki;
	        }
	        hDccdBatchno = hDcesBatchno;
	        hDccdRecno = hDcesRecno;

	        if (type == 1) {
	            updateSQL = "old_beg_date      = ? , " + "old_end_date      = ? , " 
	                      + "old_activate_type = ? , " + "old_activate_flag = ? , " 
	                      + "old_activate_date = ? , " + "eng_name          = ? , "
	                      + "emboss_data       = ? , " + "unit_code         = ? , " 
	                      + "trans_cvv2        = ? , " + "pvki              = ? , " 
	                      + "pin_block         = ? , " + "activate_type     = ? , "
	                      + "activate_flag     = ? , " + "activate_date     = ? , " 
	                      + "urgent_flag       = ? , " + "new_beg_date      = ? , " 
	                      + "new_end_date      = ? , " + "reissue_date      = ? , "
	                      + "reissue_status    = ? , " + "batchno           = ? , " 
	                      + "recno             = ? , " + "branch            = ? , " 
	                      + "mail_branch       = ? , " + "mail_no           = ? , "
	                      + "mail_proc_date    = ? , " + "mail_type         = ? , " 
	                      + "ic_flag           = ? , " + "old_bank_actno    = ? , " 
	                      + "bank_actno        = ? , " + "mod_pgm           = ? , "
	                      // JustinWu: 2020/04/10
	                      + "current_code           = ? , " + "oppost_date    = ? , " 
	                      + "oppost_reason           = ? , " 
	                      // =============
	                      + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
	            daoTable = "dbc_card ";
	            whereStr = "where card_no        =  ? ";

	            setString(1, hDccdOldBegDate);
	            setString(2, hDccdOldEndDate);
	            setString(3, hDccdOldActivateType);
	            setString(4, hDccdOldActivateFlag);
	            setString(5, hDccdOldActivateDate);
	            setString(6, hDccdEngName);
	            setString(7, hDccdEmbossData);
	            setString(8, hDccdUnitCode);
	            setString(9, hDccdCvv2);
	            setString(10, hDccdPvki);
	            setString(11, hDccdPinBlock);
	            setString(12, hDccdActivateType);
	            setString(13, hDccdActivateFlag);
	            setString(14, hDccdActivateDate);
	            setString(15, hDccdUrgentFlag);
	            setString(16, hDccdNewBegDate);
	            setString(17, hDccdNewEndDate);
	            setString(18, hDccdReissueDate);
	            setString(19, hDccdReissueStatus);
	            setString(20, hDccdBatchno);
	            setDouble(21, hDccdRecno);
	            setString(22, hDccdBranch);
	            setString(23, hDccdMailBranch);
	            setString(24, hDccdMailNo);
	            setString(25, "");
	            setString(26, hDccdMailType);
	            setString(27, hDccdIcFlag);
	            setString(28, hDccdOldBankActno);
	            setString(29, hDccdBankActno);
	            setString(30, javaProgram);
	            // JustinWu: 2020/04/10
	            setString(31, hDccdCurrentCode);
	            setString(32, hDccdOppostDate);
	            setString(33, hDccdOppostReason);
	            // ==============
	            setString(34, sysDate + sysTime);
	            setString(35, hDcesOldCardNo);
	        } else /************ 續卡 **********/
	        {
	            updateSQL = "old_beg_date      = ? , " + "old_end_date      = ? , " 
	                      + "old_activate_type = ? , " + "old_activate_flag = ? , " 
	                      + "old_activate_date = ? , " + "emboss_data       = ? , "
	                      + "trans_cvv2        = ? , " + "pvki              = ? , " 
	                      + "pin_block         = ? , " + "activate_type     = ? , " 
	                      + "activate_flag     = ? , " + "activate_date     = ? , "
	                      + "new_beg_date      = ? , " + "new_end_date      = ? , " 
	                      + "change_date       = ? , " + "change_status     = ? , " 
	                      + "batchno           = ? , " + "recno             = ? , "
	                      + "branch            = ? , " + "mail_branch       = ? , " 
	                      + "mail_no           = ? , " + "mail_proc_date    = ? , " 
	                      + "mail_type         = ? , " + "ic_flag           = ? , "
	                      + "old_bank_actno    = ? , " + "bank_actno        = ? , " 
	                      // =============
	                      + "mod_pgm           = ? , "
	                      + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
	            daoTable = "dbc_card ";
	            whereStr = "where card_no        =  ? ";

	            setString(1, hDccdOldBegDate);
	            setString(2, hDccdOldEndDate);
	            setString(3, hDccdOldActivateType);
	            setString(4, hDccdOldActivateFlag);
	            setString(5, hDccdOldActivateDate);
	            setString(6, hDccdEmbossData);
	            setString(7, hDccdCvv2);
	            setString(8, hDccdPvki);
	            setString(9, hDccdPinBlock);
	            setString(10, hDccdActivateType);
	            setString(11, hDccdActivateFlag);
	            setString(12, hDccdActivateDate);
	            setString(13, hDccdNewBegDate);
	            setString(14, hDccdNewEndDate);
	            setString(15, hDccdChangeDate);
	            setString(16, hDccdChangeStatus);
	            setString(17, hDccdBatchno);
	            setDouble(18, hDccdRecno);
	            setString(19, hDccdBranch);
	            setString(20, hDccdMailBranch);
	            setString(21, hDccdMailNo);
	            setString(22, "");
	            setString(23, hDccdMailType);
	            setString(24, hDccdIcFlag);
	            setString(25, hDccdOldBankActno);
	            setString(26, hDccdBankActno);
	            // ==============
	            setString(27, javaProgram);
	            setString(28, sysDate + sysTime);
	            setString(29, hDcesOldCardNo);
	        }

	        updateTable();

	        if (notFound.equals("Y")) {
	            String err1 = "update_old dbc_card        error[notFind]";
	            String err2 = hDcesCarNo;
	            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
	        }
	        if (debug == 1)
	            showLogMessage("I", "", "  888 update dbc_card OK " + hDcesOldCardNo + "]");
	        return 0;
	    }

	/***********************************************************************/
    int procUpgradeCard() throws Exception {
        int rtn = 0;

        initDbcCard();
        hOldCardNo = "";
        hOldCardNo = hDcesOldCardNo;

        if ((rtn = getCardData()) != 0)
            return (rtn);

        hDccdUpgradeDate   = sysDate;
        hDccdUpgradeStatus = "3";
        hDccdNewCardNo    = hDcesCarNo;
        hDccdBankActno     = hDcesBankActno;
        rtn = updateOldCard(2);
        moveOldCardVar();
        hDccdCardType      = hDcesCardType;
        hDccdNewBegDate   = hDcesValidFm;
        hDccdNewEndDate   = hDcesValidTo;
        hDccdOldCardNo    = hDcesOldCardNo;
        hDccdSourceCode    = "";
        hDcesStmtCycle     = hDccdStmtCycle;
        hDccdFeeCode       = hDcesFeeCode;
        hDccdActivateFlag  = "1";
        /*********************************************
         * 抓取附卡之正卡是否製卡成功 20010/08/29
         *********************************************/
        if (hDccdSupFlag.equals("1")) {
            if ((rtn = chkUpgradeCardno()) != 0)
                return (rtn);
        }
        if ((rtn = insertDbcCard(1)) != 0) {
            return (rtn);
        }

        return (0);
    }

    /***********************************************************************/
    int chkUpgradeCardno() throws Exception {
        String majorCardno = "";
        majorCardno = "";
        /********** 確定正卡是否製卡完成 2001/08/29 **************/
        sqlCmd = "select card_no ";
        sqlCmd += " from dbc_card  ";
        sqlCmd += "where id_p_seqno = ?  ";
        sqlCmd += "and card_type = ?  ";
        sqlCmd += "and decode(group_code,'','0000', group_code) = ?  ";
        sqlCmd += "and current_code = '0' ";
        setString(1, hDccdMajorIdPSeqno);
        setString(2, hDccdCardType);
        setString(3, hDccdGroupCode.length() == 0 ? "0000" : hDccdGroupCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            majorCardno = getValue("card_no");
        } else {
            errorMsg = String.format("普昇金抓取不到正卡");
            return (1);
        }
        if (!hDccdMajorCardNo.equals(majorCardno)) {
            hDccdMajorCardNo = majorCardno;
        }
        return (0);
    }

    /***********************************************************************/
    int procNewCard() throws Exception {
        int rtn = 0;

        /* 檢核附卡 */
        hAcctType = "";
        hAcctKey = "";
        hDccdIndicator = "";
        rtn = getAcctType();

        if (debug == 1)
            showLogMessage("I", "", "  888 new_card=[" + hDccdIndicator + "]");

        rtn = 0;
        switch (Integer.parseInt(hDccdIndicator)) {
        case 1:
            rtn = procOrgNewCard();
            break;
        case 2:
            rtn = procBusNewCard();
            break;
        }
        if (rtn != 0)
            return (1);

        return (0);
    }

    /***********************************************************************/
    int getAcctType() throws Exception {

        hAcctType = "";
        String hGroupCode = "";
        String hDccdType = "";
        hDccdIndicator = "";
        hComboIndicator = "";
        hDccdType = hDcesCardType;
        hGroupCode = hDcesGroupCode;
        hInstCrdtamt = 0;
        hInstCrdtrate = 0;

        if ((hGroupCode.length() > 0) && (!hGroupCode.equals("0000"))) {
            sqlCmd = "select a.card_indicator,";
            sqlCmd += "b.acct_type,";
            sqlCmd += "a.inst_crdtamt,";
            sqlCmd += "a.inst_crdtrate,";
            sqlCmd += "a.u_cycle_flag,";
            sqlCmd += "a.stmt_cycle ";
            sqlCmd += " from dbp_acct_type a,dbp_prod_type b  ";
            sqlCmd += "where nvl(b.group_code,'0000') = ?  ";
            sqlCmd += "  and b.card_type =''  ";
            sqlCmd += "  and a.acct_type = b.acct_type ";
            setString(1, hGroupCode.length() == 0 ? "0000" : hGroupCode);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hDccdIndicator  = getValue("card_indicator");
                hComboIndicator = "Y";
                hAcctType       = getValue("acct_type");
                hInstCrdtamt    = getValueLong("inst_crdtamt");
                hInstCrdtrate   = getValueDouble("inst_crdtrate");
                hUCycleFlag    = getValue("u_cycle_flag");
                hStmtCycle      = getValue("stmt_cycle");
                return (0);
            }
        }
        sqlCmd = "select a.card_indicator,";
        sqlCmd += "b.acct_type,";
        sqlCmd += "a.inst_crdtamt,";
        sqlCmd += "a.inst_crdtrate,";
        sqlCmd += "a.u_cycle_flag,";
        sqlCmd += "a.stmt_cycle ";
        sqlCmd += " from dbp_acct_type a,dbp_prod_type b  ";
        sqlCmd += "where b.card_type = ?  ";
        sqlCmd += "  and a.acct_type = b.acct_type ";
        setString(1, hDccdType);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", String.format("GET ACCT_TYPE error type[%s]", hDccdType));
            tmpCheckCode = "E05";
            errorMsg = "找不到帳戶資料";
            return (1);
        }
        if (recordCnt > 0) {
            hDccdIndicator  = getValue("card_indicator");
            hComboIndicator = "Y";
            hAcctType       = getValue("acct_type");
            hInstCrdtamt    = getValueLong("inst_crdtamt");
            hInstCrdtrate   = getValueDouble("inst_crdtrate");
            hUCycleFlag    = getValue("u_cycle_flag");
            hStmtCycle      = getValue("stmt_cycle");
        }

        hDceAcctType = hAcctType;

        return (0);
    }

    /***********************************************************************/
    int procBusNewCard() throws Exception {
        int suc = 0;
        String hIdCd = "";
        int ccode = 0;
        int rtn = 0;
        int busExitFlag = 0;

        busExitFlag = getCorpData();
        if (busExitFlag != 0) {
            return (1);
        }
        if (hDcesApplIdCode.trim().length() == 0) {
            hDcesApplIdCode = "0";
        }

        initDbcIdno();
        busExitFlag = checkIdno();
        if (busExitFlag != 0) {
        	hDcioIdPSeqno = getIdPSeqno();
        }
        moveIdnoVar();
        if (busExitFlag != 0) {
            // h_id_cd = h_dces_apply_id_code;
            ccode = 0;
            // ccode = comcr.str2int(h_id_cd);
            while (suc == 0) {
                rtn = existedIIdno();
                if (rtn != 0) {
                    dup1++;
                    ccode++;
                    hIdCd = String.format("%1d", ccode);
                    hDcesApplIdCode = hIdCd;
                    hDcioIdCode = hIdCd;
                } else {
                    suc = 1;
                }
            }
        }
        rtn = 0;
        /* 需產生dba_acno,個繳時AC_ACCT */
        initProcssData();
        moveToData2();
        if ((rtn = subBusProcess()) != 0) {
            return (1);
        }

        hDcesCashLmt      = pComboCashLimit;
        hDceAcctType     = pAcctType;
        hDcesAccKey      = pAcctKey;
        hDcesStmtCycle    = hCorpStmtCycle;
        hDcesCorpActFlag = pCorpActFlag;
        if (busExitFlag == 0) {
            if ((rtn = updateDbcIdno()) != 0) {
                return (rtn);
            }
        } else {
            if ((rtn = insertDbcIdno()) != 0)
                return (rtn);
        }

        initDbcCard();
        rtn = 0;
        hDccdPSeqno    = pPSeqno;
        hDccdStmtCycle = hCorpStmtCycle;
        hDccdGpNo      = pGpNo;
        hDccdIdPSeqno = hDcioIdPSeqno;
        if (hDcesCorpActFlag.equals("Y")) {
            hDccdAcctKey = pAcctKey;
            hDccdGpNo    = hCorpAcnoPSeqno;
        } else {
            hDccdAcctKey = pAcctKey;
            hDccdPSeqno  = pPSeqno;
            hDccdGpNo    = pGpNo;
        }
        moveCardVar();
        hDccdActivateFlag = "1";
        if (hDcesOnlineMark.equals("2")) {
            hDccdActivateFlag = "2";
        }
        if ((rtn = insertDbcCard(2)) != 0) {
            return (rtn);
        }

        return (0);
    }

    /***********************************************************************/
    int getCorpData() throws Exception {
        hCorpPSeqno = "";
        hCorpAcnoPSeqno = "";
        hCorpAcctType = "";
        hCorpAcctKey = "";
        hCorpCreditActNo = "";
        hCorpStmtCycle = "";
        hCorpCreditLimit = 0;
        hCorpAcctType = hAcctType;
        hCorpAcctKey = String.format("%8s000", hDcesCorpNo);

        sqlCmd = "select ";
        // sqlCmd += "a.corp_no,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "b.p_seqno,";
        sqlCmd += "b.credit_act_no,";
        sqlCmd += "b.stmt_cycle ";
        sqlCmd += " from crd_corp a,dba_acno b  ";
        sqlCmd += "where a.corp_no   = ?  ";
        sqlCmd += "  and decode(b.corp_no_code,'','0', b.corp_no_code) = ? ";
        sqlCmd += "  and b.acct_type = ?  ";
        sqlCmd += "  and b.acct_key  = ? ";
        setString(1, hDcesCorpNo);
        setString(2, hDcesCorpNoCode);
        setString(3, hCorpAcctType);
        setString(4, hCorpAcctKey);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "get_corp_data");
            tmpCheckCode = "E07";
            errorMsg = "無法人資料/帳戶";
            return (1);
        }
        if (recordCnt > 0) {
         // h_corp_no            = getValue("corp_no");
            hCorpPSeqno       = getValue("corp_p_seqno");
            hCorpAcnoPSeqno  = getValue("p_seqno");
            hCorpCreditActNo = getValue("credit_act_no");
            hCorpStmtCycle    = getValue("stmt_cycle");
        }

        if (hCorpStmtCycle.length() == 0) {
        	tmpCheckCode = "E08";
            errorMsg = "無法人關帳期";
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    int getIndivAcctKey() throws Exception {
        String maxAcctKey = "";
        String maxValue = "";
        int pCorpCrdSeqno;

        pAcnoAcctKey = "";
        pAcnoClassCode = "";
        pAcnoVipCode = "";
        pAcnoRegBankNo = "";
        pAcnoPSeqno = "";
        pAcnoGpNo = "";
        pAcnoCorpActFlag = "";
        pAcnoComboAcctNo = "";
        pAcnoRowid = "";
        pAcnoCreditAmt = 0;
        pAcnoCreditAmtCash = 0;
        pAcnoComboCashLimit = 0;

        sqlCmd = "select acct_key,";
        sqlCmd += "p_seqno,";
        sqlCmd += "reg_bank_no,";
        sqlCmd += "vip_code,";
        sqlCmd += "line_of_credit_amt,";
        sqlCmd += "corp_act_flag,";
        sqlCmd += "acct_no,";
        sqlCmd += "class_code,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from dba_acno  ";
        sqlCmd += "where acct_type    = ?  ";
        sqlCmd += "  and id_p_seqno   = ?  ";
        sqlCmd += "  and corp_p_seqno = ? ";
        setString(1, pAcctType);
        setString(2, pIdPSeqno);
        setString(3, pCorpPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            pAcnoAcctKey      = getValue("acct_key");
            pAcnoPSeqno       = getValue("p_seqno");
            pAcnoGpNo         = getValue("p_seqno");
            pAcnoRegBankNo   = getValue("reg_bank_no");
            pAcnoVipCode      = getValue("vip_code");
            pAcnoCreditAmt    = getValueDouble("line_of_credit_amt");
            pAcnoCorpActFlag = getValue("corp_act_flag");
            pAcnoComboAcctNo = getValue("acct_no");
            pAcnoClassCode    = getValue("class_code");
            pAcnoRowid         = getValue("rowid");
            return (0);
        } else {
            sqlCmd  = "select max(acct_key) max_acct_key ";
            sqlCmd += "  from dba_acno  ";
            sqlCmd += " where acct_type            = ?  ";
            sqlCmd += "   and substr(acct_key,1,8) = ? ";
            setString(1, pAcctType);
            setString(2, pCorpNo);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                maxAcctKey = getValue("max_acct_key");
            }
            maxValue = maxAcctKey.substring(8, 8 + 3);
        }

        pCorpCrdSeqno = comcr.str2int(maxValue) + 1;
        pAcnoAcctKey = String.format("%8s%03d", pCorpNo, pCorpCrdSeqno);

        return (1);
    }

    /***********************************************************************/
    int subBusProcess() throws Exception {
        int rtn = 0;
        /* 第一次分期付款額度 = 一般額度 */
        getClassCode();
        /**********************************
         * 抓取個人序號 ,!= 0 -- 不存在
         **********************************/
        rtn = getIndivAcctKey();
        pAcctKey = pAcnoAcctKey;
        pPSeqno  = pAcnoPSeqno;
        pGpNo    = pAcnoGpNo;
        if(debug == 1) 
        	showLogMessage("I", "", String.format("***** p_p_seqno[%s]", pPSeqno));
        validFlag = 0;
        validFlag = checkDbcCard();
        /********* 個繳帳戶已存在 *************/
        if (rtn == 0) {
            /******************************************
             * 舊帳戶以原corp_act_flag為主(2001/10/07)
             ******************************************/
            if (!pCorpActFlag.equals(pAcnoCorpActFlag)) {
                pCorpActFlag = pAcnoCorpActFlag;
            }

            if (pSupFlag.equals("0")) {
                rtn = updateDbaAcno(1);
            }
            return (rtn);
        }
        getPSeqno();
        pCreditActNo = "";
        pGpNo         = pPSeqno;
        pCreditActNo = pAcnoCreditActNo;
        if (pCorpActFlag.equals("Y")) {
            pGpNo = pCorpAcnoPSeqno;
        }
        if (pCorpActFlag.equals("N")) {
            pGpNo = pPSeqno;
        }
        /********************************************************
         * 商務卡個人defualt reg_bank,reg_bank_no (2001/07/15)
         *******************************************************/
        getCreditActNo(2);
        pRegBankNo = "";
        pRiskBankNo = "";
        if ((rtn = insertDbaAcno()) != 0)
            return (rtn);

        return (0);
    }

    /***********************************************************************/
    void moveToData2() throws Exception {
        pCardNo             = hDcesCarNo;
        pRiskBankNo        = hDcesRiskBankNo;
        pActNo              = hDcesActNo;
        pCorpNo             = hDcesCorpNo;
        pCorpNoCode        = hDcesCorpNoCode;
        pCorpPSeqno        = hCorpPSeqno;
        pSupFlag            = hDcesSupFlag;
        pAcctType           = hDceAcctType;
        pClassCode          = hDcesClassCode;
        pVipCode            = hDcesVip;
        pAcceptDm           = hDcesAcceptDm;
        pCorpAssureFlag    = hDcesCorpAssureFlag;
        pCorpActFlag       = hDcesCorpActFlag;
        pCreditActNo       = hCorpCreditActNo;
        pCreditAmt          = hDcesAuthCreditLmt;
        pAcctHolderId      = hDcesApplyId;
        pAcctHolderIdCode = hDcesApplIdCode;
        pIdPSeqno          = hDcioIdPSeqno;
        pStmtCycle          = hCorpStmtCycle;
        pCorpAcnoPSeqno   = hCorpAcnoPSeqno;
        pMailZip            = hDcesMailZip;
        pMailAddr1          = hDcesMailAddr1;
        pMailAddr2          = hDcesMailAddr2;
        pMailAddr3          = hDcesMailAddr3;
        pMailAddr4          = hDcesMailAddr4;
        pMailAddr5          = hDcesMailAddr5;

        return;
    }

    /***********************************************************************/
    int checkPmCard() throws Exception {
        String hPmIdCode = "";
        sqlCmd  = "select id_no_code ";
        sqlCmd += "  from dbc_idno  ";
        sqlCmd += " where id_no    = ?  ";
        sqlCmd += "   and birthday = ? ";
        setString(1, hDcesPmId);
        setString(2, hDcesPmBirthday);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hPmIdCode = getValue("id_no_code");
        }
        if (hPmIdCode.length() == 0)
            hPmIdCode = "0";

        hDcesPmIdCode = hPmIdCode;
        hMajorIdPSeqno = "";
        hMajorCardNo = "";
        sqlCmd = "select a.acct_type,";
        sqlCmd += "b.acct_key,";
        sqlCmd += "a.major_id_p_seqno,";
        sqlCmd += "a.card_no ";
        sqlCmd += " from dbc_card a,dba_acno b, dbc_idno c  ";
        sqlCmd += "where c.id_no        = ?  ";
        sqlCmd += "  and c.id_no_code   = ?  ";
        sqlCmd += "  and b.p_seqno      = a.p_seqno ";
        sqlCmd += "  and c.id_p_seqno   = a.id_p_seqno ";
        sqlCmd += "  and a.card_type    = ?  ";
        sqlCmd += "  and decode(a.group_code,'','0000',a.group_code) = ?  ";
        sqlCmd += "  and a.current_code = '0'  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hDcesPmId);
        setString(2, hDcesPmIdCode);
        setString(3, hDcesCardType);
        setString(4, hDcesGroupCode.length() == 0 ? "0000" : hDcesGroupCode);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcctType        = getValue("acct_type");
            hAcctKey         = getValue("acct_key");
            hMajorIdPSeqno = getValue("major_id_p_seqno");
            hMajorCardNo    = getValue("card_no");
        }
        if (hMajorCardNo.length() == 0) {
        	tmpCheckCode = "E09";
            errorMsg = String.format("無正卡資料");
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    int procOrgNewCard() throws Exception {
        int suc = 0;
        String hIdCd = "";
        int ccode = 0;
        String pidno = "";
        int rtn = 0;
        int idnoExitFlag = 0;

        /****************************************************
         * 檢核附卡所屬正卡之acct_type,acct_key
         ****************************************************/
        if (hDcesSupFlag.equals("1")) {
            if ((rtn = checkPmCard()) != 0) {
                return (rtn);
            }
        }
        if (hDcesApplIdCode.equals(" ")) {
            hDcesApplIdCode = "0";
        }
        initDbcIdno();
        idnoExitFlag = checkIdno();
        /**************************************************************
         * new dbc_idno to get id_p_seqno before insert into dba_acno
         **************************************************************/
        if (idnoExitFlag != 0) {
        	hDcioIdPSeqno = getIdPSeqno();
        }
        moveIdnoVar();
        /*********************************************************
         * 檢核ID_CODE是否存在
         *********************************************************/
        if (idnoExitFlag != 0) {
            // h_id_cd = h_dces_apply_id_code;
            // h_id_cd = "";
            ccode = 0;
            // ccode = comcr.str2int(h_id_cd);
            while (suc == 0) {
                rtn = existedIIdno();
                /***********************************************
                 * id_code已存在,id_code+1
                 ***********************************************/
                if (rtn != 0) {
                    dup1++;
                    ccode++;
                    hIdCd = String.format("%1d", ccode);
                    hDcesApplIdCode = hIdCd;
                    hDcioIdCode = hIdCd;
                } else {
                    suc = 1;
                }
            }
        }
        /*******************************************************
         * 正卡之acct_type,acct_key(apply_id_code)可能會變更
         *******************************************************/
        if (hDcesSupFlag.equals("0")) {
            pidno = String.format("%10s%1s", hDcesApplyId, hDcesApplIdCode);
            hAcctKey = pidno;
        }

        rtn = 0;
        initProcssData();
        moveToData1();
        rtn = subMainProcess();
        if ( rtn != 0) {
            return (1);
        }
        hDceAcctType  = pAcctType;
        hDcesAccKey   = pAcctKey;
        hDcesStmtCycle = pStmtCycle;
        if (debug == 1)
            showLogMessage("I", "", "  888 1685=[" + idnoExitFlag + "]");

        if (idnoExitFlag == 0) {
            if ((rtn = updateDbcIdno()) != 0)
                return (rtn);
        } else {
            hDcioFstStmtCycle = pStmtCycle;
            if ((rtn = insertDbcIdno()) != 0)
                return (rtn);
        }
        if (debug == 1)
            showLogMessage("I", "", "  888 1698=[" + idnoExitFlag + "]");
        initDbcCard();
        rtn = 0;
        hDcesCashLmt   = pComboCashLimit;
        hDccdAcctKey   = pAcctKey;
        hDccdPSeqno    = pPSeqno;
        hDccdStmtCycle = pStmtCycle;
        hDccdGpNo      = pGpNo;
        hDccdIdPSeqno = hDcioIdPSeqno;
        hDccdAcctNo    = "";
        if (hComboIndicator.equals("Y")) {
            hDccdAcctNo = hDcesActNo;
        }
        moveCardVar();
        hDccdActivateFlag = "1";
        if (hDcesOnlineMark.equals("2")) {
            hDccdActivateFlag = "2";
        }
        rtn = insertDbcCard(3);
        if ( rtn != 0) {
            return (rtn);
        }

        return (0);
    }

    /***********************************************************************/
    /**
     * get id_p_seqno. 
     * First, select it from crd_idno_seqno.
     * If we cannot find id_p_seqno from this table, 
     * then get it from ECS_ACNO
     * @throws Exception
     */
    String getIdPSeqno() throws Exception {
        String seqno = "";
        int idPSeqno = 0;
        
        sqlCmd = "select id_p_seqno "
                        + " from crd_idno_seqno"
                        + " where id_no = ? "
                        + " fetch first 1 rows only ";
        setString(1, hDcesApplyId );
        selectTable();
        if (notFound.equals("Y")) {
        	idPSeqno = getIdSeqnoFromEcsAcno();
        	hIntIdnoIdPSeqno = String.format("%010d", idPSeqno);
        	insertCrdIdnoSeqno();
        }else {
        	idPSeqno = getValueInt("id_p_seqno");
        }
        
        // convert id_p_seqno into a string whose length is 10 
        seqno = String.format("%010d", idPSeqno);

        return seqno;
    }

    private int getIdSeqnoFromEcsAcno() throws Exception {
    	int idSeqno = 0;
    	sqlCmd = "select ECS_ACNO.NEXTVAL id_seqno ";
        sqlCmd += " from dual";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("ERROR GET ID_P_SEQNO", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
        	idSeqno = getValueInt("id_seqno");
        }
		return idSeqno;
	}

	/***********************************************************************/
    void moveIdnoVar() throws Exception {
        hDcioId            = hDcesApplyId;
        hDcioIdCode       = hDcesApplIdCode;
        hDcioIbmIdCode   = hDcesApplyIbmIdCode;
        hDcioChiName      = hDcesChiName;
        hDcioStaffFlag    = hDcesStaffFlag;
        hDcioSex           = hDcesSex;
        hDcioBirthday      = hDceBirthday;
        hDcioMarriage      = hDcesMarriage;
        hDcioEducation     = hDcesEducation;
        hDcioStudent       = hDcesStudent;
        hDcioNation        = hDcesNation;
        hDcioServiceYear  = hDcesServiceYear;
        hDcioAnnualIncome = hDcesSalary;
        hDcioAssetValue   = hDcesValue;
        hDcioCreditFlag   = hDcesCreditFlag;
        hDcioCommFlag     = hDcesCommFlag;
        hDcioSalaryCode   = hDcesSalaryCode;
        hDcioVoiceNum  = hDcesVoiceNum;
        if (hDceToNcccDate.length() > 0) {
            hDcioCardSince = hDceToNcccDate;
        } else {
            hDcioCardSince = sysDate;
        }

        hDcioOfficeAreaCode1 = hDcesOfficeAreaCode1;
        hDcioOfficeTelNo1    = hDcesOfficeTelNo1;
        hDcioOfficeTelExt1   = hDcesOfficeTelExt1;
        hDcioOfficeAreaCode2 = hDcesOfficeAreaCode2;
        hDcioOfficeTelNo2    = hDcesOfficeTelNo2;
        hDcioOfficeTelExt2   = hDcesOfficeTelExt2;
        hDcioHomeAreaCode1   = hDcesHomeAreaCode1;
        hDcioHomeTelNo1      = hDcesHomeTelNo1;
        hDcioHomeTelExt1     = hDcesHomeTelExt1;
        hDcioHomeAreaCode2   = hDcesHomeAreaCode2;
        hDcioHomeTelNo2      = hDcesHomeTelNo2;
        hDcioHomeTelExt2     = hDcesHomeTelExt2;
        hDcioResidentZip      = hDcesResidenZip;
        hDcioResidentAddr1    = hDcesResidentAddr1;
        hDcioResidentAddr2    = hDcesResidentAddr2;
        hDcioResidentAddr3    = hDcesResidentAddr3;
        hDcioResidentAddr4    = hDcesResidentAddr4;
        hDcioResidentAddr5    = hDcesResidentAddr5;
        hDcioJobPosition      = hDcesJobPosition;
        hDcioCompanyName      = hDcesCompanyName;
        hDcioBusinessCode     = hDcesBusinessCode;
        hDcioCellarPhone      = hDcesCellarPhone;
        hDcioEMailAddr       = hDcesEMailAddr;
        hDcioCrtDate          = sysDate;
        hDcioModUser          = hModUser;
        hDcioModPgm           = prgmId;
    }

    /***********************************************************************/
    int existedIIdno() throws Exception {

        sqlCmd = "select id_p_seqno,";
        sqlCmd += "id_no_code,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from dbc_idno  ";
        sqlCmd += "where id_no      = ?  ";
        sqlCmd += "  and id_no_code = ? ";
        setString(1, hDcesApplyId);
        setString(2, hDcesApplIdCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void moveToData1() throws Exception {
        pCardNo          = hDcesCarNo;
        pRegBankNo      = hDcesRegBankNo;
        pRiskBankNo     = hDcesRiskBankNo;
        pActNo           = hDcesActNo;
        pCorpNo          = hDcesCorpNo;
        pCorpNoCode     = hDcesCorpNoCode;
        pSupFlag         = hDcesSupFlag;
        pAcctType        = hDceAcctType;
        pAcctKey         = hAcctKey;
        pClassCode       = hDcesClassCode;
        pVipCode         = hDcesVip;
        pAcceptDm        = hDcesAcceptDm;
        pCorpAssureFlag = hDcesCorpAssureFlag;
        pCorpActFlag    = hDcesCorpActFlag;
        pCreditAmt       = hDcesAuthCreditLmt;
        pComboAcctNo    = "";
        if (hComboIndicator.equals("Y")) {
            pComboAcctNo = hDcesActNo;
        }
        if (!hDcesApplyId.equals(hDcesPmId)) {
            pAcctHolderId      = hDcesPmId;
            pAcctHolderIdCode = hDcesPmIdCode;
            pIdPSeqno          = hMajorIdPSeqno;
        } else {
            pAcctHolderId      = hDcesApplyId;
            pAcctHolderIdCode = hDcesApplIdCode;
            pIdPSeqno          = hDcioIdPSeqno;
            if (hDcesEmbossSource.equals("1")) {
                pMailZip   = hDcesMailZip;
                pMailAddr1 = hDcesMailAddr1;
                pMailAddr2 = hDcesMailAddr2;
                pMailAddr3 = hDcesMailAddr3;
                pMailAddr4 = hDcesMailAddr4;
                pMailAddr5 = hDcesMailAddr5;
            }
        }
        pStmtCycle = hDcesStmtCycle;

        return;
    }

    /***********************************************************************/
    void getClassCode() throws Exception {
        String gClassCode = "";
        sqlCmd = "select class_code ";
        sqlCmd += " from ptr_class  ";
        sqlCmd += "where cast(? as double)  between beg_credit_lmt  ";
        sqlCmd += "                          and end_credit_lmt ";
        setDouble(1, pCreditAmt);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            gClassCode = getValue("class_code");
        }
        pClassCode = gClassCode;

        return;
    }

    /***********************************************************************/
    int subMainProcess() throws Exception {
        int rtn = 0;
        int exitFlag = 0;

        validFlag = 0;
        getClassCode();
        exitFlag = checkAcno();
        validFlag = checkDbcCard();

        if (debug == 1)
            showLogMessage("I", "", "  888 sub_main valid=[" + exitFlag + "]" + validFlag);

        /* 存在(舊卡戶) */
        if (exitFlag == 0) {
            if (pAcctKey.length() == 0) {
                pAcctKey = pAcnoAcctKey;
            }
            rtn = updateDbaAcno(2);
            return (rtn);
        } else {
            getPSeqno();
        }

        pGpNo = pPSeqno;
        getCreditActNo(1);
        pRegBankNo = "";
        if ((rtn = insertDbaAcno()) != 0)
            return (rtn);

        return (0);
    }

    /***********************************************************************/
    int checkAcno() throws Exception {

        pAcnoComboAcctNo = "";
        pAcnoAcctKey = "";
        pGpNo = "";
        pAcnoStmtCycle = "";
        pAcnoClassCode = "";
        pAcnoVipCode = "";
        pAcnoRegBankNo = "";
        pAcnoCreditActNo = "";
        pAcnoStopStatus = "";
        pAcnoRowid = "";
        pAcnoNewVdchgFlag = "";

        pAcnoCreditAmt = 0;
        pAcnoCreditAmtCash = 0;
        pAcnoComboCashLimit = 0;
        hTtttStatSendPaper = "";
        sqlCmd = "select p_seqno,";
        sqlCmd += "acct_key,";
        sqlCmd += "risk_bank_no,";
        sqlCmd += "reg_bank_no,";
        sqlCmd += "class_code,";
        sqlCmd += "vip_code,";
        sqlCmd += "line_of_credit_amt,";
        sqlCmd += "acct_no,";
        sqlCmd += "stat_send_paper,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "credit_act_no,";
        sqlCmd += "stop_status,";
        sqlCmd += "new_vdchg_flag,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from dba_acno  ";
        sqlCmd += "where acct_no = ? ";
        setString(1, pComboAcctNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            pPSeqno              = getValue("p_seqno");
            pGpNo                = getValue("p_seqno");
            pAcnoAcctKey        = getValue("acct_key");
            pOrgRiskBankNo     = getValue("risk_bank_no");
            pAcnoRegBankNo     = getValue("reg_bank_no");
            pAcnoClassCode      = getValue("class_code");
            pAcnoVipCode        = getValue("vip_code");
            pAcnoCreditAmt      = getValueDouble("line_of_credit_amt");
            pAcnoComboAcctNo   = getValue("acct_no");
            hTtttStatSendPaper = getValue("stat_send_paper");
            pAcnoStmtCycle      = getValue("stmt_cycle");
            pAcnoCreditActNo   = getValue("credit_act_no");
            pAcnoStopStatus     = getValue("stop_status");
            pAcnoNewVdchgFlag  = getValue("new_vdchg_flag");
            pAcnoRowid           = getValue("rowid");
        } else {
            return (1);
        }
        if (pStmtCycle.length() == 0)
            pStmtCycle = pAcnoStmtCycle;

        return (0);
    }

    /***********************************************************************/
    /***
     * 檢查卡號是否有效卡
     * 
     * @return
     * @throws Exception
     */
    int checkDbcCard() throws Exception {
        int recCount = 0;

        sqlCmd = "select count(*) rec_count ";
        sqlCmd += "  from dbc_card  ";
        sqlCmd += " where p_seqno      = ?  ";
        sqlCmd += "   and id_p_seqno   = major_id_p_seqno  ";
        sqlCmd += "   and current_code = '0'  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, pPSeqno);
        int recordCnt = selectTable();
        
        if (recordCnt > 0) {
            recCount = getValueInt("rec_count");
        }

        if (recCount >= 1)
            return (1);
        else
            return (0);
    }

    /***********************************************************************/
    int updateDbaAcno(int idx) throws Exception {
        String tmpData = "";
        int flag = 0;
        int commVal = 0;
        int seq = 1;

        if (debug == 1)
            showLogMessage("I", "", "  888 update_dba_acno " + idx + "]");

        daoTable = "dba_acno";

        if (pAcnoStopStatus.equals("Y")) {
            updateSQL = "stop_status = ''";
            commVal = 1;
        }

        if (hDcesVdcoPcFlag.length() > 0) {
            tmpData = "stat_send_internet = ?";
            setString(seq++, hStatSendInternet);
            if (commVal == 1)
                updateSQL += ",";
            commVal = 1;
            updateSQL += tmpData;

            tmpData = "stat_send_paper    = ?";
            setString(seq++, hStatSendPaper);
            if (commVal == 1)
                updateSQL += ",";
            commVal = 1;
            updateSQL += tmpData;
        }
        /*
         * if((h_combo_indicator.equals("Y")) &&
         * (!p_combo_acct_no.equals(p_acno_combo_acct_no))) { tmp_data =
         * "combo_acct_no = ?"; setString(seq++, p_combo_acct_no); if(comm_val
         * == 1) updateSQL += ","; comm_val = 1; updateSQL += tmp_data; }
         */
        if (pMailAddr1.length() > 0)
            flag = 1;
        if (pMailAddr2.length() > 0)
            flag = 1;
        if (pMailAddr3.length() > 0)
            flag = 1;
        if (pMailAddr4.length() > 0)
            flag = 1;
        if (pMailAddr5.length() > 0)
            flag = 1;
        if (flag == 1) {
            tmpData = "bill_sending_zip   = ?,"+"bill_sending_addr1 = ?,"+"bill_sending_addr2 = ?,"
                     + "bill_sending_addr3 = ?,"+"bill_sending_addr4 = ?,"+"bill_sending_addr5 = ?,"
                     + "chg_addr_date = ?";
            setString(seq++, pMailZip);
            setString(seq++, pMailAddr1);
            setString(seq++, pMailAddr2);
            setString(seq++, pMailAddr3);
            setString(seq++, pMailAddr4);
            setString(seq++, pMailAddr5);
            setString(seq++, sysDate);
            if (commVal == 1)
                updateSQL += ",";
            commVal = 1;
            updateSQL += tmpData;
        }
        if (!pClassCode.equals(pAcnoClassCode)) {
            tmpData = "class_code = ?";
            setString(seq++, pClassCode);
            if (commVal == 1)
                updateSQL += ",";
            commVal = 1;
            updateSQL += tmpData;
        }
        if (!pVipCode.equals(pAcnoVipCode)) {
            tmpData = "vip_code = ?";
            setString(seq++, pVipCode);
            if (commVal == 1)
                updateSQL += ",";
            commVal = 1;
            updateSQL += tmpData;
        }
        if ((hDccdIndicator.equals("1")) && (pRiskBankNo.length() > 0)) {
            if (!pRiskBankNo.equals(pOrgRiskBankNo)) {
                tmpData = "risk_bank_no = ?";
                setString(seq++, pRiskBankNo);
                if (commVal == 1)
                    updateSQL += ",";
                commVal = 1;
                updateSQL += tmpData;
            }
        }
                
        tmpData = " BILL_APPLY_FLAG = decode(cast(? as varchar(1)),'',BILL_APPLY_FLAG ,?)";
    	setString(seq++, dbcEmbossBilApplyFlag);
    	setString(seq++, dbcEmbossBilApplyFlag);
        if (commVal == 1)
            updateSQL += ",";
        commVal = 1;
        updateSQL += tmpData;
        
        // ==============================
        if (commVal == 1)
            updateSQL += ",";

        if (hDcesEmbossSource.equals("1")) {
            tmpData = "new_vdchg_flag='Y',mod_pgm= ?,mod_user = ?,mod_time=sysdate";
            setString(seq++, prgmId);
            setString(seq++, hModUser);
        } else {
            tmpData = "mod_pgm=?,mod_user = ?,mod_time=sysdate";
            setString(seq++, prgmId);
            setString(seq++, hModUser);
        }

        updateSQL += tmpData;
        whereStr = " WHERE rowid  = ?";
        setRowId(seq++, pAcnoRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "", comcr.hCallBatchSeqno);
        }
        if (debug == 1)
        	showLogMessage("I", "", "  888 update dba_acno ok =[" + "]");

        if(dbcEmbossStatSendInternet.equalsIgnoreCase("Y") && !hDcesEMailAddr.equals("")) {
        	updateEMailEbill();
        }
        
        return (0);
    }

    /***********************************************************************/
    void updateEMailEbill() throws Exception {
		daoTable = "dba_acno";
		updateSQL = " e_mail_ebill = ? ,";
		updateSQL += " e_mail_ebill_date = ? ,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " where id_p_seqno = ? ";  
        setString(1, hDcesEMailAddr);
        setString(2, sysDate);
        setString(3, prgmId);
        setString(4, sysDate + sysTime);
        setString(5, hDcioIdPSeqno);

		updateTable();
		
		return;
    }
    
    /***********************************************************************/
    void getPSeqno() throws Exception {
        double pPseqAcnoSeqno;

        pPSeqno = "";
        pPseqAcnoSeqno = 0;
        // sqlCmd = "select ECS_DEBT_ACNO.NEXTVAL acno_seqno ";
        sqlCmd = "select ECS_ACNO.NEXTVAL acno_seqno ";
        sqlCmd += " from dual";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("抓取不到卡人流水號", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            pPseqAcnoSeqno = getValueInt("acno_seqno");
        }

        pPSeqno = String.format("%010.0f", pPseqAcnoSeqno);
        return;
    }

    /***********************************************************************/
    /***
     * 授信號碼 credit_act_no
     * 
     * @throws Exception
     */
    void getCreditActNo(int idx) throws Exception {
        String actBuf = "";
        String creditAct = "";
        long pPseqActSeqno = 0;
        String pPseqAct2code = "";

        if (debug == 1)
            showLogMessage("I", "", "  888 get_credit_act_no=[" + idx + "]");

        pCreditActNo = "";
        sqlCmd = "select ECS_DEBT_CRET_ACTNO.NEXTVAL act_seqno";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("抓取不到授信帳號", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            pPseqActSeqno = getValueInt("act_seqno");
        }

        actSeqno = pPseqActSeqno;
        pPseqAct2code = "CA";
        if (debug == 1)
            showLogMessage("I", "", "  888 step 1=[" + actSeqno + "]");
        /*
         * lai mark rtn_dec_cvrt rtn = comcpi.dec_cvrt(36, 4, act_seqno);
         * act_buf = rtn.output; act_seqno = rtn.num;
         */
        creditAct = String.format("%2s%4s", pPseqAct2code, actBuf);
        pCreditActNo = creditAct;

        if (debug == 1)
            showLogMessage("I", "", "  888 get_credit end=[" + creditAct + "]" + actSeqno);

        return;
    }

    /***********************************************************************/
    /***
     * 一般卡寫入主檔
     * 
     * @return
     * @throws Exception
     */
    int insertDbaAcno() throws Exception {
        String pAcctStatus = "";
        String pAcctSubStatus = "";
        String pFCurrencyFlag = "";
        String pNoTelCollFlag = "";
        String pRcBAdj = "";
        String pRcUseIndicator = "";
        String pLegalDelayCode = "";
        /* 抓取acno's p_seqno */
        /* 抓取cycle number */
        /* 抓取授信帳號 */
        /* 之前insert or update 不成功,不可做insert or update */
        /***** initial *****/
        pCardIndicator = "";
        pComboIndicator = "";
        pAutopayIndicator = "";
        pWorseMcode = "";
        pLegalDelayCode = "";
        pAcctStatus = "";
        pAcctSubStatus = "";
        pFCurrencyFlag = "";
        pNoTelCollFlag = "";
        pRcBAdj = "";
        pRcUseIndicator = "";
        pComboCashLimit = 0;

        pCardIndicator = hDccdIndicator;
        pComboIndicator = hComboIndicator;
        pAcctStatus = "1";
        pAcctSubStatus = "1";
        pAutopayIndicator = "1";
        pRcBAdj = "1";
        pRcUseIndicator = hRcUseFlag;
        pAutopayIndicator = "1";
        pWorseMcode = "0";
        pLegalDelayCode = "9";
        hNewVdchgFlag = "Y";

        if (debug == 1)
            showLogMessage("I", "", "  888  insert dba_acno=[" + pPSeqno + "]");

        setValue("p_seqno"                 , pPSeqno);
        setValue("acct_type"               , pAcctType);
        setValue("acct_key"                , pAcctKey);
        setValue("reg_bank_no"             , pRegBankNo);
        setValue("risk_bank_no"            , pRiskBankNo);
        setValue("acct_status"             , pAcctStatus);
        setValue("acct_sub_status"         , pAcctSubStatus);
        setValue("stmt_cycle"              , pStmtCycle);
        setValue("id_p_seqno"              , pIdPSeqno);
        setValue("acct_holder_id"          , pAcctHolderId);
        setValue("acct_holder_id_code"     , pAcctHolderIdCode);
        setValue("corp_no"                 , pCorpNo);
        setValue("corp_no_code"            , pCorpNoCode);
        setValue("corp_p_seqno"            , pCorpPSeqno);
        setValue("credit_act_no"           , pCreditActNo);
        setValue("card_indicator"          , pCardIndicator);
        setValue("f_currency_flag"         , pFCurrencyFlag);
        setValueDouble("line_of_credit_amt", pCreditAmt);
        setValueDouble("month_purchase_lmt", pMonthPurchaseLmt);
        setValue("rc_use_b_adj"            , pRcBAdj);
        setValue("rc_use_indicator"        , pRcUseIndicator);
        setValue("vip_code"                , pVipCode);
        setValue("class_code"              , pClassCode);
        setValue("accept_dm"               , pAcceptDm);
        setValue("corp_assure_flag"        , pCorpAssureFlag);
        setValue("corp_act_flag"           , pCorpActFlag);
        setValue("autopay_indicator"       , pAutopayIndicator);
        setValue("worse_mcode"             , pWorseMcode);
        setValue("legal_delay_code"        , pLegalDelayCode);
        setValue("bill_sending_zip"        , pMailZip);
        setValue("bill_sending_addr1"      , pMailAddr1);
        setValue("bill_sending_addr2"      , pMailAddr2);
        setValue("bill_sending_addr3"      , pMailAddr3);
        setValue("bill_sending_addr4"      , pMailAddr4);
        setValue("bill_sending_addr5"      , pMailAddr5);
        setValue("no_tel_coll_flag"        , pNoTelCollFlag);
        setValueDouble("inst_auth_loc_amt" , pInstAuthLocAmt);
        setValue("stat_send_paper"         , hStatSendPaper);
        setValue("stat_send_internet"      , hStatSendInternet);
        setValue("special_stat_code"       , "5");
        setValue("acct_no"                 , pComboAcctNo);
        setValue("new_vdchg_flag"          , hNewVdchgFlag);
        setValue("digital_flag  "          , dbcEmbossDigitalFlag);
        
        // JustinWu: add in 2020/04/08=
        
        if(dbcEmbossStatSendInternet.equalsIgnoreCase("Y")) {
        	setValue("E_MAIL_EBILL", hDcesEMailAddr);
        	setValue("E_MAIL_EBILL_DATE", sysDate);
        }else {
        	setValue("E_MAIL_EBILL", "");
        	setValue("E_MAIL_EBILL_DATE", "");
        }
        
        setValue("BILL_APPLY_FLAG", dbcEmbossBilApplyFlag);
        
        // ===================
        
        setValue("crt_date"                , sysDate);
        setValue("mod_user"                , hModUser);
        setValue("mod_time"                , sysDate + sysTime);
        setValue("mod_pgm"                 , prgmId);
        setValueInt("mod_seqno"            , 0);
        daoTable = "dba_acno";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_dba_acno duplicate!", "", comcr.hCallBatchSeqno);
        }
        if (debug == 1)
        	showLogMessage("I", "", "  888  insert dba_acno ok =[" + "]");
        return (0);
    }

    /***********************************************************************/
    int updateDbcIdno() throws Exception {
        int addrLen = 0;

        if (debug == 1)
            showLogMessage("I", "", "  888  update dbc_idno=[" + hDcesChiName + "]");

        selectCrdIdnoRtn();
        
        selectSQL = "accept_call_sell,call_sell_from_mark,call_sell_chg_date,e_news,e_news_from_mark,e_news_chg_date, "
                + "accept_dm,dm_from_mark,dm_chg_date,accept_sms,sms_from_mark,sms_chg_date ";
        
      daoTable  = " DBC_IDNO ";
      whereStr  = " where id_no    = ?";
      setString( 1, hDcioId);
      
      if (selectTable() > 0) {
    	  idnoAcceptCallSell   = getValue("accept_call_sell");
    	  idnoCallSellFromMark  = getValue("call_sell_from_mark");
    	  idnoCallSellChgDate   = getValue("call_sell_chg_date");
    	  idnoENews               = getValue("e_news");
    	  idnoENewsFromMark     = getValue("e_news_from_mark");
    	  idnoENewsChgDate      = getValue("e_news_chg_date");
    	  idnoAcceptDm      = getValue("accept_dm");
    	  idnoDmFromMark   = getValue("dm_from_mark");
    	  idnoDmChgDate    = getValue("dm_chg_date");
    	  idnoAcceptSms     = getValue("accept_sms");
    	  idnoSmsFromMark  = getValue("sms_from_mark");
    	  idnoSmsChgDate   = getValue("sms_chg_date");
      }

        addrLen = hDcesResidentAddr1.length() + hDcesResidentAddr2.length() + hDcesResidentAddr3.length()
                + hDcesResidentAddr4.length() + hDcesResidentAddr5.length();
        daoTable = "dbc_idno";
        updateSQL = " chi_name          = decode(cast(? as varchar(50)),'',chi_name ,?),";
        updateSQL += " resident_zip      = decode(cast(? as int)        ,0 ,resident_zip ,?),";
        updateSQL += " resident_addr1    = decode(cast(? as int)        ,0 ,resident_addr1,?),";
        updateSQL += " resident_addr2    = decode(cast(? as int)        ,0 ,resident_addr2,?),";
        updateSQL += " resident_addr3    = decode(cast(? as int)        ,0 ,resident_addr3,?),";
        updateSQL += " resident_addr4    = decode(cast(? as int)        ,0 ,resident_addr4,?),";
        updateSQL += " resident_addr5    = decode(cast(? as int)        ,0 ,resident_addr5,?),";
        updateSQL += " business_code     = decode(cast(? as varchar(10)),'',business_code,?),";
        updateSQL += " home_area_code1   = decode(cast(? as varchar(10)),'',home_area_code1,?),";
        updateSQL += " home_tel_no1      = decode(cast(? as varchar(12)),'',home_tel_no1, ?),";
        updateSQL += " home_tel_ext1     = decode(cast(? as varchar(10)),'',home_tel_ext1,?),";
        updateSQL += " home_area_code2   = decode(cast(? as varchar(10)),'',home_area_code2,?),";
        updateSQL += " home_tel_no2      = decode(cast(? as varchar(12)),'',home_tel_no2,?),";
        updateSQL += " home_tel_ext2     = decode(cast(? as varchar(10)),'',home_tel_ext2,?),";
        updateSQL += " office_area_code1 = decode(cast(? as varchar(10)),'',office_area_code1,?),";
        updateSQL += " office_tel_no1    = decode(cast(? as varchar(12)),'',office_tel_no1, ?),";
        updateSQL += " office_tel_ext1   = decode(cast(? as varchar(10)),'',office_tel_ext1,?),";
        updateSQL += " office_area_code2 = decode(cast(? as varchar(10)),'',office_area_code2,?),";
        updateSQL += " office_tel_no2    = decode(cast(? as varchar(12)),'',office_tel_no2, ?),";
        updateSQL += " office_tel_ext2   = decode(cast(? as varchar(10)),'',office_tel_ext2,?),";
        updateSQL += " e_mail_addr       = decode(cast(? as varchar(50)),'',e_mail_addr,?),";
        updateSQL += " job_position      = decode(cast(? as varchar(24)),'',job_position,?),";
        updateSQL += " cellar_phone      = decode(cast(? as varchar(15)),'',cellar_phone ,?),";
        updateSQL += " company_name      = decode(cast(? as varchar(24)),'',company_name ,?),";
        updateSQL += " salary_code       = decode(cast(? as varchar(10)),'',salary_code ,?),";
        updateSQL += " credit_flag       = decode(cast(? as varchar(10)),'',credit_flag ,?),";
        updateSQL += " resident_no       = decode(cast(? as varchar(20)),'',resident_no ,?),";
        updateSQL += " passport_no       = decode(cast(? as varchar(20)),'',passport_no ,?),";
        updateSQL += " other_cntry_code  = decode(cast(? as varchar(10)),'',other_cntry_code,?),";
        updateSQL += " staff_flag        = decode(cast(? as varchar(10)),'',staff_flag ,?),";
        updateSQL += " comm_flag         = decode(cast(? as varchar(10)),'',comm_flag ,?),";
        updateSQL += " fst_stmt_cycle    = decode(cast(? as varchar(10)),'1',";
        updateSQL += "     decode(cast(? as varchar(10)),'' ,?,fst_stmt_cycle),fst_stmt_cycle),";
        updateSQL += " mod_pgm     = ?,";
        updateSQL += " mod_time    = sysdate,";
        updateSQL += " mod_user    = ? , ";
        //JustinWu: add in 2020/04/08
        updateSQL += " MAIL_ZIP = decode(cast(? as varchar(6)),'',MAIL_ZIP ,?),";
        updateSQL += " MAIL_ADDR1 = decode(cast(? as varchar(10)),'',MAIL_ADDR1 ,?),";
        updateSQL += " MAIL_ADDR2 = decode(cast(? as varchar(10)),'',MAIL_ADDR2 ,?),";
        updateSQL += " MAIL_ADDR3 = decode(cast(? as varchar(12)),'',MAIL_ADDR3 ,?),";
        updateSQL += " MAIL_ADDR4 = decode(cast(? as varchar(12)),'',MAIL_ADDR4 ,?),";
        updateSQL += " MAIL_ADDR5 = decode(cast(? as varchar(56)),'',MAIL_ADDR5 ,?),";
        updateSQL += " SPOUSE_NAME = decode(cast(? as varchar(50)),'',SPOUSE_NAME ,?),";
        updateSQL += " SPOUSE_ID_NO = decode(cast(? as varchar(20)),'',SPOUSE_ID_NO ,?),";
        updateSQL += " SPOUSE_BIRTHDAY = decode(cast(? as varchar(8)),'',SPOUSE_BIRTHDAY ,?),";
        updateSQL += " RESIDENT_NO_EXPIRE_DATE = decode(cast(? as varchar(8)),'',RESIDENT_NO_EXPIRE_DATE ,?),";
        updateSQL += " GRADUATION_ELEMENTARTY = decode(cast(? as varchar(20)),'',GRADUATION_ELEMENTARTY ,?),";
        updateSQL += " UR_FLAG = decode(cast(? as varchar(1)),'',UR_FLAG ,?),";
        updateSQL += " COMPANY_ZIP = decode(cast(? as varchar(6)),'',COMPANY_ZIP ,?),";
        updateSQL += " COMPANY_ADDR1 = decode(cast(? as varchar(10)),'',COMPANY_ADDR1 ,?),";
        updateSQL += " COMPANY_ADDR2 = decode(cast(? as varchar(10)),'',COMPANY_ADDR2 ,?),";
        updateSQL += " COMPANY_ADDR3 = decode(cast(? as varchar(12)),'',COMPANY_ADDR3 ,?),";
        updateSQL += " COMPANY_ADDR4 = decode(cast(? as varchar(12)),'',COMPANY_ADDR4 ,?),";
        updateSQL += " COMPANY_ADDR5 = decode(cast(? as varchar(56)),'',COMPANY_ADDR5 ,?),";
        updateSQL += " market_agree_base = decode(cast(? as varchar(1)),'',market_agree_base ,?),";
        updateSQL += " accept_call_sell    = decode( cast(? as varchar(60)),'',accept_call_sell  ,?),";
        updateSQL += " call_sell_from_mark = ? , ";
        updateSQL += " call_sell_chg_date  = ? , ";
        updateSQL += " e_news              = decode( cast(? as varchar(60)),'',e_news  ,?), ";
        updateSQL += " e_news_from_mark    = ? , ";
        updateSQL += " e_news_chg_date     = ? , ";
        updateSQL += " accept_dm           = decode( cast(? as varchar(60)),'',accept_dm  ,?), ";
        updateSQL += " dm_from_mark        = ? , ";
        updateSQL += " dm_chg_date         = ? , ";
        updateSQL += " accept_sms          = decode( cast(? as varchar(60)),'',accept_sms  ,?), ";
        updateSQL += " sms_from_mark       = ? , ";
        updateSQL += " sms_chg_date        = ? , ";
        updateSQL += " eng_name            = decode(cast(? as vargraphic(25)),'',eng_name ,?) ";
        //=======================
        whereStr   = " where rowid  = ? ";
        setString(1, hDcesChiName);
        setString(2, hDcesChiName);
        setInt(3, addrLen);
        setString(4, hDcesResidenZip);
        setInt(5, addrLen);
        setString(6, hDcesResidentAddr1);
        setInt(7, addrLen);
        setString(8, hDcesResidentAddr2);
        setInt(9, addrLen);
        setString(10, hDcesResidentAddr3);
        setInt(11, addrLen);
        setString(12, hDcesResidentAddr4);
        setInt(13, addrLen);
        setString(14, hDcesResidentAddr5);
        setString(15, hDcesBusinessCode);
        setString(16, hDcesBusinessCode);
        setString(17, hDcesHomeTelNo1);
        setString(18, hDcesHomeAreaCode1);
        setString(19, hDcesHomeTelNo1);
        setString(20, hDcesHomeTelNo1);
        setString(21, hDcesHomeTelNo1);
        setString(22, hDcesHomeTelExt1);
        setString(23, hDcesHomeTelNo2);
        setString(24, hDcesHomeAreaCode2);
        setString(25, hDcesHomeTelNo2);
        setString(26, hDcesHomeTelNo2);
        setString(27, hDcesHomeTelNo2);
        setString(28, hDcesHomeTelExt2);
        setString(29, hDcesOfficeTelNo1);
        setString(30, hDcesOfficeAreaCode1);
        setString(31, hDcesOfficeTelNo1);
        setString(32, hDcesOfficeTelNo1);
        setString(33, hDcesOfficeTelNo1);
        setString(34, hDcesOfficeTelExt1);
        setString(35, hDcesOfficeTelNo2);
        setString(36, hDcesOfficeAreaCode2);
        setString(37, hDcesOfficeTelNo2);
        setString(38, hDcesOfficeTelNo2);
        setString(39, hDcesOfficeTelNo2);
        setString(40, hDcesOfficeTelExt2);
        setString(41, hDcesEMailAddr);
        setString(42, hDcesEMailAddr);
        setString(43, hDcesJobPosition);
        setString(44, hDcesJobPosition);
        setString(45, hDcesCellarPhone);
        setString(46, hDcesCellarPhone);
        setString(47, hDcesCompanyName);
        setString(48, hDcesCompanyName);
        setString(49, hDcesSalaryCode);
        setString(50, hDcesSalaryCode);
        setString(51, hDcesCreditFlag);
        setString(52, hDcesCreditFlag);
        setString(53, hDcesResidentNo);
        setString(54, hDcesResidentNo);
        setString(55, hDcesPassportNo);
        setString(56, hDcesPassportNo);
        setString(57, hDcesOtherCntryCode);
        setString(58, hDcesOtherCntryCode);
        setString(59, hDcesStaffFlag);
        setString(60, hDcesStaffFlag);
        setString(61, hDcesCommFlag);
        setString(62, hDcesCommFlag);
        setString(63, hDccdIndicator);
        setString(64, hFstStmtCycle);
        setString(65, pStmtCycle);
        setString(66, hModUser);
        setString(67, hModUser);
        //JustinWu: add in 2020/04/08
        setString(68, hDcesMailZip);
        setString(69, hDcesMailZip);
        setString(70, hDcesMailAddr1);
        setString(71, hDcesMailAddr1);
        setString(72, hDcesMailAddr2);
        setString(73, hDcesMailAddr2);
        setString(74, hDcesMailAddr3);
        setString(75, hDcesMailAddr3);
        setString(76, hDcesMailAddr4);
        setString(77, hDcesMailAddr4);
        setString(78, hDcesMailAddr5);
        setString(79, hDcesMailAddr5);
        setString(80, dbcEmbossSpouseName);
        setString(81, dbcEmbossSpouseName);
        setString(82, dbcEmbossSpouseIdNo);
        setString(83, dbcEmbossSpouseIdNo);
        setString(84, dbcEmbossSpouseBirthday);
        setString(85, dbcEmbossSpouseIdNo);
        setString(86, dbcEmbossResidentNoExpireDate);
        setString(87, dbcEmbossResidentNoExpireDate);
        setString(88, dbcEmbossGraduationElementarty);
        setString(89, dbcEmbossGraduationElementarty);
        setString(90, dbcEmbossUrFlag);
        setString(91, dbcEmbossUrFlag);
        setString(92, dbcEmbossCompanyZip);
        setString(93, dbcEmbossCompanyZip);
        setString(94, dbcEmbossCompanyAddr1);
        setString(95, dbcEmbossCompanyAddr1);
        setString(96, dbcEmbossCompanyAddr2);
        setString(97, dbcEmbossCompanyAddr2);
        setString(98, dbcEmbossCompanyAddr3);
        setString(99, dbcEmbossCompanyAddr3);
        setString(100, dbcEmbossCompanyAddr4);
        setString(101, dbcEmbossCompanyAddr4);
        setString(102, dbcEmbossCompanyAddr5);
        setString(103, dbcEmbossCompanyAddr5);
        setString(104, dbcEmbossMarketAgreeBase);
        setString(105, dbcEmbossMarketAgreeBase);
        
        if(dbcEmbossMarketAgreeBase.equals("Y")) {
        	dbcEmbossAcceptCallSell = "N";
        }else{
        	dbcEmbossAcceptCallSell = "Y";
        }

        setString(106, dbcEmbossAcceptCallSell);
        setString(107, dbcEmbossAcceptCallSell);  
          
        if (dbcEmbossAcceptCallSell.equals("idno_accept_call_sell")) {
      	    setString(108, idnoCallSellFromMark);
            setString(109, idnoCallSellChgDate);
        }
        else {
      	    setString(108, "A");
      	    setString(109, sysDate);
        }

        setString(110, getValue("e_news"));
        setString(111, getValue("e_news"));
          
        if (dbcEmbossENews.equals("idno_e_news")) {
      	    setString(112, idnoENewsFromMark);
            setString(113, idnoENewsChgDate);
        }
        else {
      	    setString(112, "A");
      	    setString(113, sysDate);
        }
        
        if(dbcEmbossENews.equals("Y")) {
        	dbcEmbossAcceptDm = "Y";
        	dbcEmbossAcceptSms = "Y";
        }else{
        	dbcEmbossAcceptDm = "N";
        	dbcEmbossAcceptSms = "N";
        }
        
        setString(114, dbcEmbossAcceptDm);
        setString(115, dbcEmbossAcceptDm);
          
        if (dbcEmbossAcceptDm.equals("idno_accept_dm")) {
      	    setString(116, idnoDmFromMark);
            setString(117, idnoDmChgDate);
        }
        else {
      	    setString(116, "A");
      	    setString(117, sysDate);
        }
        
        setString(118, dbcEmbossAcceptSms);
        setString(119, dbcEmbossAcceptSms);
          
        if (dbcEmbossAcceptSms.equals("idno_accept_sms")) {
      	  setString(120, idnoSmsFromMark);
            setString(121, idnoSmsChgDate);
        }
        else {
      	  setString(120, "A");
      	  setString(121, sysDate);
        } 
        
        setString(122, hDcesEngName);
        setString(123, hDcesEngName);
        
        //=======================       

        setRowId(124, hIdRowid);
        updateTable();
        if (debug == 1)
            showLogMessage("I", "", "  888  update dbc_idno ok =[" + "]");
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dbc_idno not found!", "", comcr.hCallBatchSeqno);
        }

        return (0);
    }

    /***********************************************************************/
    void selectCrdIdnoRtn() throws Exception {
        int tempInt = 0;

        sqlCmd = "select count(*) temp_int ";
        sqlCmd += "  from crd_card  ";
        sqlCmd += " where id_p_seqno   = ?  ";
        sqlCmd += "   and current_code = '0' ";
        sqlCmd += "   and sup_flag     = '0' ";
        setString(1, hDcioIdPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }

        if (tempInt < 1) {
            return;
        }

        hIdnoId = "";
        hIdnoIdCode = "";
        hIdnoBirthday = "";
        hIdnoChiName = "";
        hIdnoOfficeTelNo1 = "";
        hIdnoHomeTelNo1 = "";
        hIdnoHomeAreaCode1 = "";
        hIdnoOfficeAreaCode1 = "";
        hIdnoCellarPhone = "";

        sqlCmd = "select 1 temp_int,";
        sqlCmd += "id_no,";
        sqlCmd += "id_no_code,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "birthday,";
        sqlCmd += "chi_name,";
        sqlCmd += "home_area_code1,";
        sqlCmd += "office_area_code1,";
        sqlCmd += "home_tel_no1,";
        sqlCmd += "office_tel_no1,";
        sqlCmd += "cellar_phone ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_no      = ?  ";
        sqlCmd += "  and id_no_code = ? ";
        setString(1, hDcioId);
        setString(2, hDcioIdCode);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt                 = getValueInt("temp_int");
            hIdnoId                = getValue("id_no");
            hIdnoIdCode           = getValue("id_no_code");
            hIdnoIdPSeqno        = getValue("id_p_seqno");
            hIdnoBirthday          = getValue("birthday");
            hIdnoChiName          = getValue("chi_name");
            hIdnoHomeAreaCode1   = getValue("home_area_code1");
            hIdnoOfficeAreaCode1 = getValue("office_area_code1");
            hIdnoHomeTelNo1      = getValue("home_tel_no1");
            hIdnoOfficeTelNo1    = getValue("office_tel_no1");
            hIdnoCellarPhone      = getValue("cellar_phone");

            if (((!hIdnoBirthday.equals(hDcioBirthday)) || 
                 (!hIdnoChiName.equals(hDcioChiName)) || 
                 (!hIdnoOfficeTelNo1.equals(hDcioOfficeTelNo1)) || 
                 (!hIdnoHomeTelNo1.equals(hDcioHomeTelNo1)) || 
                 (!hIdnoCellarPhone.equals(hDcioCellarPhone)))) {
                daoTable  = "dbc_idno_diff";
                whereStr  = "where process_date = ?  ";
                whereStr += "  and id_p_seqno   = ?  ";
                whereStr += "  and acct_no      = ? ";
                setString(1, hBusinessDate);
                setString(2, hIdnoIdPSeqno);
                setString(3, hDcesActNo);
                deleteTable();

                setValue("process_date"       , hBusinessDate);
                setValue("branch_code"        , hDcesBranch);
                setValue("id_p_seqno"         , hIdnoIdPSeqno);
                setValue("chi_name"           , hDcioChiName);
                setValue("birthday"           , hDcioBirthday);
                setValue("home_area_code1"    , hDcioHomeAreaCode1);
                setValue("home_tel_no1"       , hDcioHomeTelNo1);
                setValue("office_area_code1"  , hDcioOfficeAreaCode1);
                setValue("office_tel_no1"     , hDcioOfficeTelNo1);
                setValue("cellar_phone"       , hDcioCellarPhone);
                setValue("acct_no"            , hDcesActNo);
                setValue("chi_name_1"         , hIdnoChiName);
                setValue("birthday_1"         , hIdnoBirthday);
                setValue("home_area_code1_1"  , hIdnoHomeAreaCode1);
                setValue("home_tel_no1_1"     , hIdnoHomeTelNo1);
                setValue("office_area_code1_1", hIdnoOfficeAreaCode1);
                setValue("office_tel_no1_1"   , hIdnoOfficeTelNo1);
                setValue("cellar_phone_1"     , hIdnoCellarPhone);
                setValue("mod_user"           , "batch");
                setValue("mod_time"           , sysDate + sysTime);
                setValue("mod_pgm"            , prgmId);
                daoTable = "dbc_idno_diff";
                insertTable();
                if (dupRecord.equals("Y")) {
                    comcr.errRtn("insert_dbc_idno_diff duplicate!", "", comcr.hCallBatchSeqno);
                }
            }
        }
    }

    /***********************************************************************/
    int insertDbcIdno() throws Exception {

        hTempCorpNo = pCorpNo;
        selectCrdIdnoRtn();
        
        if (debug == 1)
            showLogMessage("I", "", "  888  insert dbc_idno=[" + hDcesChiName + "]");

        hDcioENews = "N";
        hDcioAcceptMbullet = "N";
//        h_dcio_accept_call_sell = "N";
        
        System.out.println("dbc_emboss_market_agree_base2 = " + dbcEmbossMarketAgreeBase);

        if(dbcEmbossMarketAgreeBase.equals("Y")) {
        	hDcioAcceptCallSell =  "N";
        }else{
        	hDcioAcceptCallSell =  "Y";
        }

        hDcioAcceptDm = "N";
        hDcioDmFromMark = "A";
        hDcioDmChgDate = hBusinessDate;
        hDcioAcceptSms = "N";
                
        if (selectCrdIdno() == 0) {
            hDcioENews           = hIdnoENews;
            hDcioAcceptMbullet   = hIdnoAcceptMbullet;
            hDcioAcceptCallSell = hIdnoAcceptCallSell;
            hDcioAcceptDm        = hIdnoAcceptDm;
            hDcioDmFromMark     = hIdnoDmFromMark;
            hDcioDmChgDate      = hIdnoDmChgDate;
            hDcioAcceptSms       = hIdnoAcceptSms;
        }
        
        /* 之前insert or update 不成功,不可做insert or update */
        setValue("id_p_seqno"         , hDcioIdPSeqno);
        setValue("id_no"              , hDcioId);
        setValue("id_no_code"         , hDcioIdCode);
        setValue("chi_name"           , hDcioChiName);
        setValue("ibm_id_code"        , hDcioIbmIdCode);
        setValue("staff_flag"         , hDcioStaffFlag);
        setValue("staff_br_no"        , hDcioStaffBrNo);
        setValue("credit_flag"        , hDcioCreditFlag);
        setValue("comm_flag"          , hDcioCommFlag);
        setValue("salary_code"        , hDcioSalaryCode);
        setValue("sex"                , hDcioSex);
        setValue("birthday"           , hDcioBirthday);
        setValue("marriage"           , hDcioMarriage);
        setValue("education"          , hDcioEducation);
        setValue("student"            , hDcioStudent);
        setValue("nation"             , hDcioNation);
        setValueInt("service_year"    , hDcioServiceYear);
        setValueDouble("asset_value"  , hDcioAssetValue);
        setValue("corp_no"            , hTempCorpNo);
        setValueDouble("annual_income", hDcioAnnualIncome);
        setValue("resident_no"        , hDcioResidentNo);
        setValue("passport_no"        , hDcioPassportNo);
        setValue("other_cntry_code"   , hDcioOtherCntryCode);
        setValue("office_area_code1"  , hDcioOfficeAreaCode1);
        setValue("office_tel_no1"     , hDcioOfficeTelNo1);
        setValue("office_tel_ext1"    , hDcioOfficeTelExt1);
        setValue("office_area_code2"  , hDcioOfficeAreaCode2);
        setValue("office_tel_no2"     , hDcioOfficeTelNo2);
        setValue("office_tel_ext2"    , hDcioOfficeTelExt2);
        setValue("home_area_code1"    , hDcioHomeAreaCode1);
        setValue("home_tel_no1"       , hDcioHomeTelNo1);
        setValue("home_tel_ext1"      , hDcioHomeTelExt1);
        setValue("home_area_code2"    , hDcioHomeAreaCode2);
        setValue("home_tel_no2"       , hDcioHomeTelNo2);
        setValue("home_tel_ext2"      , hDcioHomeTelExt2);
        setValue("resident_zip"       , hDcioResidentZip);
        setValue("resident_addr1"     , hDcioResidentAddr1);
        setValue("resident_addr2"     , hDcioResidentAddr2);
        setValue("resident_addr3"     , hDcioResidentAddr3);
        setValue("resident_addr4"     , hDcioResidentAddr4);
        setValue("resident_addr5"     , hDcioResidentAddr5);
        setValue("job_position"       , hDcioJobPosition);
        setValue("company_name"       , hDcioCompanyName);
        setValue("business_code"      , hDcioBusinessCode);
        setValue("cellar_phone"       , hDcioCellarPhone);
        setValue("fax_no"             , hDcioFaxNo);
        setValue("e_mail_addr"        , hDcioEMailAddr);
        setValue("card_since"         , hDcioCardSince);
        setValue("voice_passwd"       , hDcioVoiceNum);
        setValue("fst_stmt_cycle"     , hDcioFstStmtCycle);
        setValue("crt_date"           , hDcioCrtDate);
        setValue("crt_user"           , hDcioCrtUser);
        setValue("apr_date"           , hDcioAprDate);
        setValue("apr_user"           , hDcioAprUser);
        setValue("e_news"             , hDcioENews);
        setValue("e_news_from_mark"   , "A");
        setValue("e_news_chg_date"    , sysDate);
        setValue("accept_mbullet"     , hDcioAcceptMbullet);
        setValue("accept_call_sell"   , hDcioAcceptCallSell);
        setValue("call_sell_from_mark", "A");
        setValue("call_sell_chg_date" , sysDate);        
        setValue("accept_dm"          , hDcioAcceptDm);
        setValue("dm_from_mark"       , hDcioDmFromMark);
        setValue("dm_chg_date"        , hDcioDmChgDate);
        setValue("accept_sms"         , hDcioAcceptSms);
        setValue("sms_from_mark", "A");
        setValue("sms_chg_date" , sysDate);
        // =JustinWu: add in 2020/04/08=
        setValue("SPOUSE_NAME",dbcEmbossSpouseName);
        setValue("SPOUSE_ID_NO",dbcEmbossSpouseIdNo);
        setValue("SPOUSE_BIRTHDAY",dbcEmbossSpouseBirthday);
        setValue("RESIDENT_NO_EXPIRE_DATE",dbcEmbossResidentNoExpireDate);
        setValue("GRADUATION_ELEMENTARTY",dbcEmbossGraduationElementarty);
        setValue("UR_FLAG",dbcEmbossUrFlag);
        setValue("BUSINESS_CODE",hDcesBusinessCode);
        setValue("MAIL_ZIP",hDcesMailZip);
        setValue("MAIL_ADDR1",hDcesMailAddr1);
        setValue("MAIL_ADDR2",hDcesMailAddr2);
        setValue("MAIL_ADDR3",hDcesMailAddr3);
        setValue("MAIL_ADDR4",hDcesMailAddr4);
        setValue("MAIL_ADDR5",hDcesMailAddr5);
        setValue("COMPANY_ZIP",dbcEmbossCompanyZip);
        setValue("COMPANY_ADDR1",dbcEmbossCompanyAddr1);
        setValue("COMPANY_ADDR2",dbcEmbossCompanyAddr2);
        setValue("COMPANY_ADDR3",dbcEmbossCompanyAddr3);
        setValue("COMPANY_ADDR4",dbcEmbossCompanyAddr4);
        setValue("COMPANY_ADDR5",dbcEmbossCompanyAddr5);
        setValue("market_agree_base",dbcEmbossMarketAgreeBase);
        setValue("msg_flag", "Y");
        setValueInt("msg_purchase_amt", 0);
        setValue("eng_name", hDcesEngName);

        // ====================  
        setValue("mod_user"           , hDcioModUser);
        setValue("mod_time"           , sysDate + sysTime);
        setValue("mod_pgm"            , hDcioModPgm);
        daoTable = "dbc_idno";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_dbc_idno duplicate!", "", comcr.hCallBatchSeqno);
        }
        if (debug == 1)
        	showLogMessage("I", "", "  888  insert dbc_idno ok =[" + "]");
        idnoCnt++;

        return (0);
    }

    /***********************************************************************/
    int selectCrdIdno() throws Exception {
        hIdnoENews = "";
        hIdnoAcceptMbullet = "";
        hIdnoAcceptCallSell = "";
        hIdnoAcceptDm = "";
        hIdnoDmFromMark = "";
        hIdnoDmChgDate = "";
        hIdnoAcceptSms = "";
        sqlCmd = "select e_news,";
        sqlCmd += "accept_mbullet,";
        sqlCmd += "accept_call_sell,";
        sqlCmd += "accept_dm,";
        sqlCmd += "dm_from_mark,";
        sqlCmd += "dm_chg_date,";
        sqlCmd += "accept_sms ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_no  = ?  ";
        sqlCmd += "  and id_no_code = ? ";
        setString(1, hDcioId);
        setString(2, hDcioIdCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIdnoENews           = getValue("e_news");
            hIdnoAcceptMbullet   = getValue("accept_mbullet");
            hIdnoAcceptCallSell = getValue("accept_call_sell");
            hIdnoAcceptDm        = getValue("accept_dm");
            hIdnoDmFromMark     = getValue("dm_from_mark");
            hIdnoDmChgDate      = getValue("dm_chg_date");
            hIdnoAcceptSms       = getValue("accept_sms");
        } else {
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void moveCardVar() throws Exception {
        hDccdIbmIdCode = hDcesApplyIbmIdCode;
        hDccdCardNo     = hDcesCarNo;
        hDccdId          = hDcesApplyId;
        hDccdIdCode     = hDcesApplIdCode;
        if (hDccdIndicator.equals("2")) {
            hDccdCorpNo       = hDcesCorpNo;
            hDccdCorpNoCode  = hDcesCorpNoCode;
            hDccdCorpPSeqno  = hCorpPSeqno;
            hDccdCorpActFlag = hDcesCorpActFlag;
        }
        hDccdCardType    = hDcesCardType;
        hDccdGroupCode   = hDcesGroupCode;
        hDccdSourceCode  = hDcesSourceCode;
        hDccdSupFlag     = hDcesSupFlag;
        hDccdMemberId    = hDcesMemberId;
        hDccdCurrentCode = "0";
        hDccdEngName     = hDcesEngName;
        hDccdRegBankNo  = hDcesRegBankNo;
        hDccdUnitCode    = hDcesUnitCode;
        hDccdPinBlock    = hDcesPinBlock;
        hDccdNewBegDate = hDcesValidFm;
        hDccdNewEndDate = hDcesValidTo;
        hDccdMemberNote  = hDceMemberNote;
        hDccdModUser     = hModUser;
        hDccdModPgm      = prgmId;
        if (!hDcesApplyId.equals(hDcesPmId)) {
            hDccdMajorId         = hDcesPmId;
            hDccdMajorIdCode    = hDcesPmIdCode;
            hDccdMajorIdPSeqno = hMajorIdPSeqno;
            hDccdMajorCardNo    = hMajorCardNo;
        } else {
            hDccdMajorId         = hDcesApplyId;
            hDccdMajorIdCode    = hDcesApplIdCode;
            hDccdMajorIdPSeqno = hDcioIdPSeqno;
            hDccdMajorCardNo    = hDcesCarNo;
        }
        if (hDceToNcccDate.length() > 0) {
            hDccdIssueDate = hDceToNcccDate;
        } else {
            hDccdIssueDate = sysDate;
        }
        /* 電子錢包 */
        if (hDcesApplyId.equals(hDcesPmId)) {
            hDccdSetCode = hDcesPmCash;
        } else {
            hDccdSetCode = hDcesSupCash;
        }

        hDccdApplyNo       = hDcesApplyNo;
        hDccdCvv2           = hDcesCvv2;
        hDccdPvki           = hDcesPvki;
        hDccdPinBlock      = hDcesPinBlock;
        hDccdEmbossData    = hDcesEmboss4thData;
        hDccdMajorRelation = hDcesRelWithPm;
        hDccdAcctType      = hDceAcctType;
        hDccdIntroduceId   = hDcesIntroduceId;
        hDccdIntroduceName = hDcesIntroduceName;
        if (hDcesIntroduceNo.length() > 0) {
            hDccdPromoteEmpNo = hDcesIntroduceNo;
        }
        /*******************************/
        /* final really fee code value */
        /*******************************/
        hDccdFeeCode        = hDcesFinalFeeCode;
        hDccdCurrFeeCode   = hDcesFinalFeeCode;
        hDccdBatchno         = hDcesBatchno;
        hDccdRecno           = hDcesRecno;
        hDccdMailType       = hDcesMailType;
        hDccdMailNo         = hDcesMailNo;
        hDccdComboIndicator = hComboIndicator;
        hDccdSonCardFlag   = hDcesSonCardFlag;
        hDccdIndivCrdLmt   = hDcesIndivCrdLmt;
        hDccdCrtDate        = sysDate;

        hDccdIcFlag         = hDcesIcFlag;
        hDccdBranch          = hDcesBranch;
        hDccdMailAttach1    = hDcesMailAttach1;
        hDccdMailAttach2    = hDcesMailAttach2;
        hDccdBankActno      = hDcesBankActno;
        hDccdMsgFlag        = "Y";
        hDccdMsgPurchaseAmt = 0;
    }

    /***********************************************************************/
    int checkIdno() throws Exception {
        String hIdCode = "";
        hIdRowid = "";
        hFstStmtCycle = "";
        sqlCmd = "select id_p_seqno,";
        sqlCmd += "id_no_code,";
        sqlCmd += "chi_name,";
        sqlCmd += "fst_stmt_cycle,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from dbc_idno  ";
        sqlCmd += "where id_no       = ?  ";
        sqlCmd += "  and ibm_id_code = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hDcesApplyId);
        setString(2, hDcesApplyIbmIdCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDcioIdPSeqno = getValue("id_p_seqno");
            hIdCode         = getValue("id_no_code");
            hDcioChiName   = getValue("chi_name");
            hFstStmtCycle  = getValue("fst_stmt_cycle");
            hIdRowid        = getValue("rowid");
        } else {
            return (1);
        }
        hDcesApplIdCode = hIdCode;
        return (0);
    }
    /***********************************************************************/
    int procReissueCard() throws Exception {
    	hOldCardNo = hDcesOldCardNo;
        // init_crd_card();
    	int rtn = getCardData();
        if (rtn != 0)
            return (rtn);

        /* 重製中 -- 不為毀損重製時,需寫舊卡及新卡號,並增加一筆新資料到卡片主檔 */
        hDccdMailBranch = getValue("mail_branch");

        if ( ! hDcesEmbossReason.equals("2") ) {
            pPSeqno = hDccdPSeqno;
            hDccdReissueDate = sysDate;
            hDccdReissueStatus = "3"; /* 重製已完成 */
            hDccdNewCardNo = hDcesCarNo;
            /*** adding bank_actno ***/
            hDccdBankActno = getValue("bank_actno");
            rtn = updateOldCard(1);
            if (rtn != 0)
                return (rtn);

            moveOldCardVar();
            hDccdReissueDate = "";
            hDccdReissueStatus = "";
            hDccdNewBegDate = getValue("valid_fm");
            hDccdNewEndDate = getValue("valid_to");
            hDccdOldCardNo = hDcesOldCardNo;
            hDccdEngName = getValue("eng_name");
            hDccdUnitCode = hDcesUnitCode;
            hDccdEmbossData = getValue("emboss_4th_data");
            hDccdMailType = getValue("mail_type");
            hDccdMailBranch = getValue("mail_branch");
            hDccdBranch = getValue("branch");
            hDccdMailNo = "";
            hDccdStmtCycle = pStmtCycle;
            hDccdActivateFlag = "1";
            
            rtn = insertDbcCard(3);
            if (rtn != 0)
                return (rtn);
            
        } else {
            /* 毀損重製 卡號不變 */
            hDccdReissueDate      = sysDate;
            hDccdReissueStatus    = "3"; /* 重製已完成 */
            hDccdOldBegDate      = hDccdNewBegDate;
            hDccdOldEndDate      = hDccdNewEndDate;
            hDccdNewBegDate      = getValue("valid_fm");
            hDccdNewEndDate      = getValue("valid_to");
            hDccdMailType         = getValue("mail_type");
            hDccdEngName          = getValue("eng_name");
            hDccdUnitCode         = hDcesUnitCode;
            hDccdEmbossData       = getValue("emboss_4th_data");
            hDccdOldActivateType = hDccdActivateType;
            hDccdOldActivateFlag = hDccdActivateFlag;
            hDccdOldActivateDate = hDccdActivateDate;
            hDccdActivateFlag     = "1";
            hDccdActivateType     = "";
            hDccdActivateDate     = "";
            hDccdBatchno           = hDcesBatchno;
            hDccdRecno             = hDcesRecno;
            hDccdMailBranch       = getValue("mail_branch");
            hDccdBranch            = getValue("branch");
            hDccdMailNo           = "";
            hDccdBankActno        = getValue("bank_actno");
            hDccdCurrentCode      = "0";  
            hDccdOppostDate       = "";
            hDccdOppostReason     = "";
            if (hDcesOnlineMark.equals("1") || hDcesOnlineMark.equals("2"))
                hDccdUrgentFlag = "Y";
            hDccdChangeDate = sysDate;
            hDccdChangeStatus = "3";

            rtn = updateChgCard(1);
            if (rtn != 0)
                return (rtn);
        }

        return (0);
    }

	/***********************************************************************/
    int procUrgentCard() throws Exception {
        int rtn = 0;
        hOldCardNo = "";
        hOldCardNo = hDcesOldCardNo;
        rtn = getCardData();
        if (rtn != 0)
            return (rtn);
        hDccdReissueStatus = "3";
        hDccdReissueDate   = sysDate;
        hDccdNewCardNo    = hDcesCarNo;
        hDccdBankActno     = hDcesBankActno;

        rtn = updateOldCard(1);
        if (rtn != 0)
            return (rtn);
        moveOldCardVar();
        hDccdReissueDate   = "";
        hDccdReissueStatus = "";
        hDccdNewEndDate   = hDcesValidFm;
        hDccdNewEndDate   = hDcesValidTo;
        hDccdEmergentFlag  = "Y";
        hDccdActivateFlag  = "2";
        hDccdOldCardNo    = hDcesOldCardNo;
        hDcesStmtCycle     = hDccdStmtCycle;
        rtn = insertDbcCard(5);
        if (rtn != 0) {
            return (rtn);
        }
        return (0);
    }

    /***********************************************************************/
    int updateOldCard(int type) throws Exception {
        String pNewMajorCardNo = "";
        String tempSource = "";

        tempSource = hDcesEmbossSource;
        /* 正卡換新卡號,要更新附卡major_card_no (不包括普申金卡) */
        if ((hDccdSupFlag.equals("0")) && (!hDcesEmbossSource.equals("2"))) {
            pNewMajorCardNo = "";
            pNewMajorCardNo = hDcesCarNo;
            updateSupCard(hOldCardNo, pNewMajorCardNo);
        }
        /********************************************
         * type = 1 為重製,type=2 為普申金
         ********************************************/
        if (type == 1) {
            daoTable   = "dbc_card";
            updateSQL  = " old_activate_type = ?,";
            updateSQL += " old_activate_flag = ?,";
            updateSQL += " old_activate_date = ?,";
            updateSQL += " activate_type  = ?,";
            updateSQL += " activate_flag  = ?,";
            updateSQL += " activate_date  = ?,";
            updateSQL += " new_card_no    = ?,";
            updateSQL += " reissue_date   = ?,";
            updateSQL += " reissue_status = ?,";
            updateSQL += " change_status  = decode(cast(? as varchar(10)),'3',? ,'4',? ,change_status),";
            updateSQL += " change_date    = decode(cast(? as varchar(10)),'3',? ,'4',? ,change_date),";
            updateSQL += " mod_pgm        = ?,";
            updateSQL += " mod_user       = ?,";
            updateSQL += " mod_time       = sysdate";
            whereStr   = "where card_no   = ? ";
            setString(1, hDccdOldActivateType);
            setString(2, hDccdOldActivateFlag);
            setString(3, hDccdOldActivateDate);
            setString(4, hDccdActivateType);
            setString(5, hDccdActivateFlag);
            setString(6, hDccdActivateDate);
            setString(7, hDccdNewCardNo);
            setString(8, hDccdReissueDate);
            setString(9, hDccdReissueStatus);
            setString(10, tempSource);
            setString(11, hDccdChangeStatus);
            setString(12, hDccdChangeStatus);
            setString(13, tempSource);
            setString(14, hDccdChangeDate);
            setString(15, hDccdChangeDate);
            setString(16, prgmId);
            setString(17, hModUser);
            setString(18, hDcesOldCardNo);
            updateTable();
            if (notFound.equals("Y")) {
                errorMsg = "舊卡號update錯誤";
                comcr.errRtn("update_dbc_card not found!", "", comcr.hCallBatchSeqno);
            }
        } else {
            daoTable   = "dbc_card";
            updateSQL  = " upgrade_status = ?,";
            updateSQL += " upgrade_date   = ?,";
            updateSQL += " new_card_no    = ?,";
            updateSQL += " mod_pgm        = ?,";
            updateSQL += " mod_user       = ?,";
            updateSQL += " mod_time       = sysdate";
            whereStr   = "where card_no   = ? ";
            setString(1, hDccdUpgradeStatus);
            setString(2, hDccdUpgradeDate);
            setString(3, hDccdNewCardNo);
            setString(4, prgmId);
            setString(5, hModUser);
            setString(6, hDcesOldCardNo);
            updateTable();
            if (notFound.equals("Y")) {
                errorMsg = "舊卡號update錯誤";
                comcr.errRtn("update_dbc_card not found!", "", comcr.hCallBatchSeqno);
            }
        }
        return (0);
    }

    /***********************************************************************/
    /***
     * 將正卡新卡號,update到附卡之正卡卡號
     * 
     * @param majorCardno
     * @param newMajorCardno
     * @return
     * @throws Exception
     */
    int updateSupCard(String majorCardno, String newMajorCardno) throws Exception {
        String chkMajorCardno = "";
        String chOldMajorCardno = "";
        String chkNewMajorCardno = "";
        String chkSupCardno = "";
        chkMajorCardno      = "";
        chkSupCardno        = "";
        chOldMajorCardno  = "";
        chkNewMajorCardno  = "";
        chkMajorCardno      = majorCardno;
        chkNewMajorCardno  = newMajorCardno;
        sqlCmd  = "select ";
        sqlCmd += "major_card_no,";
        sqlCmd += "card_no ";
        sqlCmd += "from dbc_card ";
        sqlCmd += "where major_card_no = ? ";
        sqlCmd += "  and sup_flag      = '1' ";
        sqlCmd += "  and current_code  = '0' ";
        setString(1, chkMajorCardno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            chOldMajorCardno = getValue("major_card_no", i);
            chkSupCardno = getValue("card_no", i);

            if (!chOldMajorCardno.equals(chkNewMajorCardno)) {
                if (chkNewMajorCardno.length() > 0) {
                    daoTable   = "dbc_card";
                    updateSQL  = " major_card_no = ?,";
                    updateSQL += " mod_time      = sysdate,";
                    updateSQL += " mod_user      = ?,";
                    updateSQL += " mod_pgm       = ?";
                    whereStr   = "where card_nox     = ?  ";
                    whereStr  += "  and current_code = '0' ";
                    setString(1, chkNewMajorCardno);
                    setString(2, hModUser);
                    setString(3, prgmId);
                    setString(4, chkSupCardno);
                    updateTable();
                    if (notFound.equals("Y")) {
                        comcr.errRtn("update_dbc_card not found!", "", comcr.hCallBatchSeqno);
                    }
                }
            }
        }

        return (0);
    }

    /***********************************************************************/
    void moveOldCardVar() throws Exception {
        hDccdIbmIdCode   = hDcesApplyIbmIdCode;
        hDccdNewCardNo   = "";
        hDccdCardNo       = hDcesCarNo;
        hDccdCurrentCode  = "0";
        hDccdEmergentFlag = "";
        hDccdBatchno       = hDcesBatchno;
        hDccdRecno         = hDcesRecno;
        if (hDccdSupFlag.equals("0")) {
            hDccdMajorCardNo = hDcesCarNo;
        }
        hDccdPvki           = hDcesPvki;
        hDccdCvv2           = hDcesCvv2;
        hDccdPinBlock      = hDcesPinBlock;
        hDccdEmbossData    = hDcesEmboss4thData;
        hDccdCrtDate       = sysDate;
        hDccdUpgradeStatus = "";
        hDccdUpgradeDate   = "";
        if (hDceToNcccDate.length() > 0) {
            hDccdIssueDate = hDceToNcccDate;
        } else {
            hDccdIssueDate = sysDate;
        }
        hDccdActivateType  = "";
        hDccdActivateFlag  = "1";
        hDccdActivateDate  = "";
        hDccdMailType      = hDcesMailType;
        hDccdMailNo        = "";
        hDccdMailBranch    = "";
        hDccdMailProcDate = "";

        hDccdBranch = hDcesBranch;

        return;
    }

    /***********************************************************************/
    int insertDbcCard(int idx) throws Exception {
        String hTempOldBankActno = "";
        String hTempOldBegDate = "";
        String hTempOldEndDate = "";
        String tempX02   = "";
        String tempX02b = "";

        if (debug == 1)
            showLogMessage("I", "", "  888 insert dbc_card=[" + idx + "]");

        /* 之前insert or update 不成功,不可做insert or update */
        if (hDccdMailType.length() == 0)
            hDccdMailType = "1";
        /*******************************************
         * 中華電信密碼
         *******************************************/
        if (hDcesChtNum.length() > 0) {
            hDccdApplyChtFlag = "Y";
        } else {
            hDccdApplyChtFlag = "N";
        }
        if (hDccdPinBlock.length() == 0) {
            hDccdPinBlock = hPinBlock;
            hDccdCvv2 = hCvv2;
            hDccdPvki = hPvki;
        }
        if ((hDcesOnlineMark.equals("1")) || (hDcesOnlineMark.equals("2"))) {
            hDccdEmergentFlag = "Y";
        }
        if (hDccdSonCardFlag.equals("1")) {
            hDccdSonCardFlag = "Y";
        }
        if (hDccdSonCardFlag.equals("0")) {
            hDccdSonCardFlag = "";
        }
        hTempOldBankActno = "";
        hTempOldBegDate = "";
        hTempOldEndDate = "";

        if (hDcesOldCardNo.length() > 0) {
            hDccdOldCardNo = hDcesOldCardNo;
            sqlCmd = "select bank_actno,";
            sqlCmd += "new_beg_date,";
            sqlCmd += "new_end_date ";
            sqlCmd += " from dbc_card  ";
            sqlCmd += "where card_no = ? ";
            setString(1, hDccdOldCardNo);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hTempOldBankActno = getValue("bank_actno");
                hTempOldBegDate   = getValue("new_beg_date");
                hTempOldEndDate   = getValue("new_end_date");
            } else {
                showLogMessage("I", "", "無卡片資料 2");
                return (1);
            }
        }

        setValue("card_no"             , hDccdCardNo);
        setValue("id_p_seqno"          , hDccdIdPSeqno);
        setValue("corp_p_seqno"        , hDccdCorpPSeqno);
        setValue("corp_no"             , hDccdCorpNo);
        setValue("card_type"           , hDccdCardType);
        setValue("urgent_flag"         , hDccdUrgentFlag);
        setValue("group_code"          , hDccdGroupCode);
        setValue("source_code"         , hDccdSourceCode);
        setValue("unit_code"           , hDccdUnitCode);
        setValue("bin_no"              , hDcesBinNo);
        setValue("bin_type"            , hDcesBinType);
        setValue("sup_flag"            , hDccdSupFlag);
        setValue("acno_flag"           , "1");
        setValue("major_relation"      , hDccdMajorRelation);
        setValue("major_id_p_seqno"    , hDccdMajorIdPSeqno);
        setValue("major_card_no"       , hDccdMajorCardNo);
        setValue("member_id"           , hDccdMemberId);
        setValue("current_code"        , hDccdCurrentCode);
        setValue("eng_name"            , hDccdEngName);
        setValue("reg_bank_no"         , hDccdRegBankNo);
        setValue("pin_block"           , hDccdPinBlock);
        setValue("old_beg_date"        , hTempOldBegDate);
        setValue("old_end_date"        , hTempOldEndDate);
        setValue("new_beg_date"        , hDccdNewBegDate);
        setValue("new_end_date"        , hDccdNewEndDate);
        setValue("issue_date"          , hDccdIssueDate);
        setValue("reissue_date"        , hDccdReissueDate);
        setValue("reissue_reason"      , hDccdReissueReason);
        setValue("change_date"         , hDccdChangeDate);
        setValue("upgrade_date"        , hDccdUpgradeDate);
        setValue("apply_no"            , hDccdApplyNo);
        setValue("promote_dept"        , hDccdPromoteDept);
        setValue("promote_emp_no"      , hDccdPromoteEmpNo);
        setValue("introduce_id"        , hDccdIntroduceId);
        setValue("introduce_emp_no"    , hDccdIntroduceEmpNo);
        setValue("introduce_name"      , hDccdIntroduceName);
        setValue("prod_no"             , hDccdProdNo);
        setValueDouble("indiv_crd_lmt" , hDccdIndivCrdLmt);
        setValueDouble("indiv_inst_lmt", hDccdIndivInstLmt);
        setValue("pvv"                 , hDccdPvv);
        setValue("cvv"                 , hDccdCvv);
        setValue("trans_cvv2"          , hDccdCvv2);
        setValue("pvki"                , hDccdPvki);
        setValue("emboss_data"         , hDccdEmbossData);
        setValue("batchno"             , hDccdBatchno);
        setValueDouble("recno"         , hDccdRecno);
        setValue("oppost_reason"       , hDccdOppostReason);
        setValue("oppost_date"         , hDccdOppostDate);
        setValue("new_card_no"         , hDccdNewCardNo);
        setValue("old_card_no"         , hDccdOldCardNo);
        setValue("acct_type"           , hDccdAcctType);
        setValue("p_seqno"             , hDccdPSeqno);
        setValue("stmt_cycle"          , hDccdStmtCycle);
        setValue("apply_cht_flag"      , hDccdApplyChtFlag);
        setValue("fee_code"            , hDccdFeeCode);
        setValue("curr_fee_code"       , hDccdCurrFeeCode);
        setValue("expire_reason"       , hDccdExpireReason);
        setValue("apply_atm_flag"      , hDccdApplyAtmFlag);
        setValue("expire_chg_flag"     , hDccdExpireChgFlag);
        setValue("expire_chg_date"     , hDccdExpireChgDate);
        setValue("corp_act_flag"       , hDccdCorpActFlag);
        setValue("old_activate_type"   , hDccdOldActivateType);
        setValue("old_activate_flag"   , hDccdOldActivateFlag);
        setValue("old_activate_date"   , hDccdOldActivateDate);
        setValue("activate_type"       , hDccdActivateType);
        setValue("activate_flag"       , hDccdActivateFlag);
        setValue("activate_date"       , hDccdActivateDate);
        setValue("son_card_flag"       , hDccdSonCardFlag);
        setValue("emergent_flag"       , hDccdEmergentFlag);
        setValue("set_code"            , hDccdSetCode);
        setValue("mail_type"           , hDccdMailType);
        setValue("acct_no"             , hDccdAcctNo);
        setValueInt("beg_bal"          , 0);
        setValueInt("end_bal"          , 0);
        setValue("mail_no"             , hDccdMailNo);
        setValue("stock_no"            , hDccdStockNo);
        setValue("ic_flag"             , hDccdIcFlag);
        setValue("branch"              , hDccdBranch);
        setValue("old_bank_actno"      , hTempOldBankActno);
        setValue("bank_actno"          , hDccdBankActno);
        setValue("mail_attach1"        , hDccdMailAttach1);
        setValue("mail_attach2"        , hDccdMailAttach2);     
        // JustinWu: add in 2020/04/08===
        setValue("CRT_BANK_NO",dbcEmbossCrtBankNo);
        setValue("VD_BANK_NO",dbcEmbossVdBankNo);
        setValue("CARD_REF_NUM",dbcEmbossCardRefNum);
        setValue("ELECTRONIC_CODE",dbcEmbossElectronicCode);
        //=====================   
        setValue("crt_date"            , hDccdCrtDate);
        setValue("crt_user"            , hDccdCrtUser);
        setValue("apr_date"            , hDccdAprDate);
        setValue("apr_user"            , hDccdAprUser);
        setValue("mod_user"            , hModUser);
        setValue("mod_time"            , sysDate + sysTime);
        setValue("mod_pgm"             , prgmId);
        setValueDouble("mod_seqno"     , hDccdModSeqno);
        setValue("ibm_id_code"         , hDccdIbmIdCode);
        setValue("digital_flag"        , dbcEmbossDigitalFlag);
        setValue("msg_flag"            , hDccdMsgFlag);
        setValueInt("msg_purchase_amt" , hDccdMsgPurchaseAmt);

        daoTable = "dbc_card";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_dbc_card duplicate!", "", comcr.hCallBatchSeqno);
        }
        if (debug == 1)
        	showLogMessage("I", "", "  888  insert dbc_card ok =[" + "]");
        tempX02b = String.format("%2.2s", hDcesSourceBatchno.substring(6));

        tempX02 = String.format("%1.1s%1.1s", hDcesThirdRsn, hDcesThirdRsnIbm);

        if (hDcesEmbossSource.equals("3")) {
            if ((tempX02.equals("A0") || tempX02.equals("B1") || tempX02.equals("C1")) && 
                (comcr.str2int(tempX02b) >= 85)) {
                checkIdnoC();
                insertDbcCardNosend();
                insertDbcReturn();
            }
        }
        crdCnt++;

        return (0);
    }

    /***********************************************************************/
    int checkIdnoC() throws Exception {

        hDcioChiName = "";
        hDcioCellarPhone = "";
        hDcioOfficeTelNo1 = "";
        hDcioOfficeAreaCode1 = "";
        hDcioOfficeTelExt1 = "";
        hDcioHomeTelNo1 = "";
        hDcioHomeAreaCode1 = "";
        hDcioHomeTelExt1 = "";

        sqlCmd = "select chi_name,";
        sqlCmd += "cellar_phone,";
        sqlCmd += "office_area_code1,";
        sqlCmd += "office_tel_no1,";
        sqlCmd += "office_tel_ext1,";
        sqlCmd += "home_area_code1,";
        sqlCmd += "home_tel_no1,";
        sqlCmd += "home_tel_ext1 ";
        sqlCmd += " from dbc_idno  ";
        sqlCmd += "where id_no       = ?  ";
        sqlCmd += "  and ibm_id_code = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hDcesApplyId);
        setString(2, hDcesApplyIbmIdCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDcioChiName          = getValue("chi_name");
            hDcioCellarPhone      = getValue("cellar_phone");
            hDcioOfficeAreaCode1 = getValue("office_area_code1");
            hDcioOfficeTelNo1    = getValue("office_tel_no1");
            hDcioOfficeTelExt1   = getValue("office_tel_ext1");
            hDcioHomeAreaCode1   = getValue("home_area_code1");
            hDcioHomeTelNo1      = getValue("home_tel_no1");
            hDcioHomeTelExt1     = getValue("home_tel_ext1");
        } else {
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void insertDbcCardNosend() throws Exception {
        setValue("batchno"          , hDccdBatchno);
        setValueDouble("recno"      , hDccdRecno);
        setValue("card_no"          , hDccdCardNo);
        setValue("id_p_seqno"       , hDccdIdPSeqno);
        setValue("corp_no"          , hDccdCorpNo);
        setValue("chi_name"         , hDcioChiName);
        setValue("cellar_phone"     , hDcioCellarPhone);
        setValue("office_area_code1", hDcioOfficeAreaCode1);
        setValue("office_tel_no1"   , hDcioOfficeTelNo1);
        setValue("office_tel_ext1"  , hDcioOfficeTelExt1);
        setValue("in_main_date"     , sysDate);
        setValue("home_area_code1"  , hDcioHomeAreaCode1);
        setValue("home_tel_no1"     , hDcioHomeTelNo1);
        setValue("home_tel_ext1"    , hDcioHomeTelExt1);
        setValue("call_flag"        , "N");
        setValue("dept_flag"        , "");
        setValue("mod_user"         , hModUser);
        setValue("mod_time"         , sysDate + sysTime);
        setValue("mod_pgm"          , prgmId);
        daoTable = "dbc_card_nosend";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_dbc_card_nosend duplicate!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertDbcReturn() throws Exception {
        setValue("card_no"     , hDccdCardNo);
        setValue("acct_no"     , hDcesActNo);
        setValue("return_date" , sysDate);
        setValue("group_code"  , hDccdGroupCode);
        setValue("id_p_seqno"  , hDccdIdPSeqno);
        setValue("ic_flag"     , hDccdIcFlag);
        setValue("reason_code" , "09");
        setValue("return_type" , "2");
        setValue("mail_type"   , "5");
        setValue("package_flag", "N");
        setValue("beg_date"    , hDccdNewBegDate);
        setValue("end_date"    , hDccdNewEndDate);
        setValue("mod_user"    , "batch");
        setValue("proc_status" , "7");
        setValue("mod_time"    , sysDate + sysTime);
        setValue("mod_pgm"     , prgmId);
        daoTable = "dbc_return";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_dbc_return duplicate!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    int getCardData() throws Exception {
        ppCurrentCode = "";
        sqlCmd = "select b.id_no,";
        sqlCmd += "b.id_no_code,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "a.corp_no,";
        sqlCmd += "a.card_type,";
        sqlCmd += "a.group_code,";
        sqlCmd += "a.source_code,";
        sqlCmd += "a.sup_flag,";
        sqlCmd += "a.current_code,";
        sqlCmd += "a.son_card_flag,";
        sqlCmd += "a.major_relation,";
        sqlCmd += "c.id_no as major_id,";
        sqlCmd += "c.id_no_code as major_id_code,";
        sqlCmd += "a.major_id_p_seqno,";
        sqlCmd += "a.major_card_no,";
        sqlCmd += "a.member_id,";
        sqlCmd += "a.force_flag,";
        sqlCmd += "a.eng_name,";
        sqlCmd += "a.reg_bank_no,";
        sqlCmd += "a.unit_code,";
        sqlCmd += "a.new_beg_date,";
        sqlCmd += "a.new_end_date,";
        sqlCmd += "a.emboss_data,";
//      sqlCmd += "a.block_status,";
//      sqlCmd += "a.block_reason,";
//      sqlCmd += "a.block_reason2,";
//      sqlCmd += "a.block_date,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "d.acct_key,";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.fee_code,";
        sqlCmd += "a.curr_fee_code,";
        sqlCmd += "a.lost_fee_code,";
        sqlCmd += "a.indiv_crd_lmt,";
        sqlCmd += "a.indiv_inst_lmt,";
        sqlCmd += "a.expire_reason,";
        sqlCmd += "a.expire_chg_flag,";
        sqlCmd += "a.expire_chg_date,";
        sqlCmd += "a.corp_act_flag,";
        sqlCmd += "a.activate_type,";
        sqlCmd += "a.activate_flag,";
        sqlCmd += "a.activate_date,";
        sqlCmd += "a.apply_atm_flag,";
        sqlCmd += "a.apply_cht_flag,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "a.acct_no,";
        sqlCmd += "a.ic_flag,";
        sqlCmd += "a.bank_actno,";
        sqlCmd += "a.promote_dept,";
        sqlCmd += "a.promote_emp_no,";
        sqlCmd += "a.introduce_id,";
        sqlCmd += "a.introduce_name,";
        sqlCmd += "a.introduce_emp_no,";
        sqlCmd += "a.msg_flag,";
        sqlCmd += "a.msg_purchase_amt,";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += " from dbc_card a,dbc_idno b, dbc_idno c, dba_acno d  ";
        sqlCmd += "where card_no      = ? ";
        sqlCmd += "  and b.id_p_seqno = a.id_p_seqno ";
        sqlCmd += "  and c.id_p_seqno = a.major_id_p_seqno ";
        sqlCmd += "  and d.p_seqno    = a.p_seqno ";
        setString(1, hOldCardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "get_dbc_card");
            tmpCheckCode = "E11";
            errorMsg = "無卡片資料";
            return (1);
        }
        if (recordCnt > 0) {
            hDccdId               = getValue("id_no");
            hDccdIdCode          = getValue("id_no_code");
            hDccdIdPSeqno       = getValue("id_p_seqno");
            hDccdCorpPSeqno     = getValue("corp_p_seqno");
            hDccdCorpNo          = getValue("corp_no");
            hDccdCardType        = getValue("card_type");
            hDccdGroupCode       = getValue("group_code");
            hDccdSourceCode      = getValue("source_code");
            hDccdSupFlag         = getValue("sup_flag");
            ppCurrentCode         = getValue("current_code");
            hDccdSonCardFlag    = getValue("son_card_flag");
            hDccdMajorRelation   = getValue("major_relation");
            hDccdMajorId         = getValue("major_id");
            hDccdMajorIdCode    = getValue("major_id_code");
            hDccdMajorIdPSeqno = getValue("major_id_p_seqno");
            hDccdMajorCardNo    = getValue("major_card_no");
            hDccdMemberId        = getValue("member_id");
            hDccdForceFlag       = getValue("force_flag");
            hDccdEngName         = getValue("eng_name");
            hDccdRegBankNo      = getValue("reg_bank_no");
            hDccdUnitCode        = getValue("unit_code");
            hDccdNewBegDate     = getValue("new_beg_date");
            hDccdNewEndDate     = getValue("new_end_date");
            hDccdEmbossData      = getValue("emboss_data");
//          h_dccd_block_status     = getValue("block_status");
//          h_dccd_block_reason     = getValue("block_reason");
//          h_dccd_block_reason2    = getValue("block_reason2");
//          h_dccd_block_date       = getValue("block_date");
            hDccdAcctType        = getValue("acct_type");
            hDccdAcctKey         = getValue("acct_key");
            hDccdPSeqno          = getValue("p_seqno");
            hDccdGpNo            = getValue("p_seqno");
            hDccdFeeCode         = getValue("fee_code");
            hDccdCurrFeeCode    = getValue("curr_fee_code");
            hDccdLostFeeCode    = getValue("lost_fee_code");
            hDccdIndivCrdLmt    = getValueDouble("indiv_crd_lmt");
            hDccdIndivInstLmt   = getValueDouble("indiv_inst_lmt");
            hDccdExpireReason    = getValue("expire_reason");
            hDccdExpireChgFlag  = getValue("expire_chg_flag");
            hDccdExpireChgDate  = getValue("expire_chg_date");
            hDccdCorpActFlag    = getValue("corp_act_flag");
            hDccdActivateType    = getValue("activate_type");
            hDccdActivateFlag    = getValue("activate_flag");
            hDccdActivateDate    = getValue("activate_date");
            hDccdApplyAtmFlag   = getValue("apply_atm_flag");
            hDccdApplyChtFlag   = getValue("apply_cht_flag");
            hDccdStmtCycle       = getValue("stmt_cycle");
            hDccdAcctNo          = getValue("acct_no");
            hDccdComboIndicator  = "Y";
            hDccdIcFlag          = getValue("ic_flag");
            hDccdOldBankActno   = getValue("bank_actno");
            hDccdPromoteDept     = getValue("promote_dept");
            hDccdPromoteEmpNo   = getValue("promote_emp_no");
            hDccdIntroduceId     = getValue("introduce_id");
            hDccdIntroduceName   = getValue("introduce_name");
            hDccdIntroduceEmpNo = getValue("introduce_emp_no");
            hDccdMsgFlag        = getValue("msg_flag");
            hDccdMsgPurchaseAmt = getValueInt("msg_purchase_amt");
            hDccdRowid            = getValue("rowid");
        }
        chk = 0;
        /******* 此舊卡片(毀損重製,續卡)已停掛,不可入主檔(2002/02/19) **********/
        if ( (hDcesEmbossSource.equals("3") ) || (hDcesEmbossSource.equals("4")) ) {
            chk = 1;
            if (!ppCurrentCode.equals("0")) {
            	tmpCheckCode = "E12";
                errorMsg = "舊卡片不為有效卡";
                return (1);
            }
        }
        
        return (0);
    }

    /***********************************************************************/
    int delPinBlock() throws Exception {
        daoTable = "crd_cardno_data";
        whereStr = "where card_no = ? ";
        setString(1, hDcesCarNo);
        deleteTable();
        if (notFound.equals("Y")) {
        	tmpCheckCode = "E15";
            errorMsg = "線上製卡PIN_BLOCK值抓取不到";
            return 1;
        }

        return (0);
    }

    /***********************************************************************/
    int updateEmboss2() throws Exception {
        int changeCreditLimit;
        if (debug == 1)
            showLogMessage("I", "", "  888 update dbc_emboss2=[" + hDcesCarNo + "]");

        /* 緊急新製卡需視為資料已回饋 */
        changeCreditLimit = 0;
        if (pAcnoCreditAmt != pCreditAmt) {
            if (validFlag == 1) {
                if (pCreditAmt < pAcnoCreditAmt)
                    changeCreditLimit = 1;
            }
        }

        if (hDcesRtnNcccDate.length() <= 0) {
            hDcesRtnNcccDate = sysDate;
        }
        if (hDcesApplyId.substring(0, 10).equals(hDcesPmId)) {
            hDcesPmIdCode = hDcesApplIdCode;
        }
        hDcesInMainError = String.format("%1d", rtnCode);
        if (!hDcesInMainError.equals("0")) {
            hDcesInMainDate = "";
            hDcesInMainMsg = errorMsg;
        } else {
            hDcesInMainDate = sysDate;
        }

        if (changeCreditLimit == 1) {
            daoTable   = "dbc_emboss";
            updateSQL  = " class_code      ='M',";
            updateSQL += " apply_id_code   = ?,";
            updateSQL += " pm_id_code      = ?,";
            updateSQL += " acct_type       = ?,";
            updateSQL += " acct_key        = ?,";
            updateSQL += " cash_lmt        = ?,";
            updateSQL += " in_main_error   = ?,";
            updateSQL += " in_main_date    = ?,";
            updateSQL += " in_main_msg     = ?,";
            updateSQL += " rtn_nccc_date   = ?,";
            updateSQL += " auth_credit_lmt = ?,";
            updateSQL += " check_code      = ?,";
            updateSQL += " mod_pgm         = ?,";
            updateSQL += " mod_user        = ?,";
            updateSQL += " mod_time        = sysdate";
            whereStr   = "where rowid      = ? ";
            setString(1, hDcesApplIdCode);
            setString(2, hDcesPmIdCode);
            setString(3, hDceAcctType);
            setString(4, hDcesAccKey);
            setInt(5, hDcesCashLmt);
            setString(6, hDcesInMainError);
            setString(7, hDcesInMainDate);
            setString(8, hDcesInMainMsg);
            setString(9, hDcesRtnNcccDate);
            setDouble(10, pAcnoCreditAmt);
            setString(11, tmpCheckCode);
            setString(12, prgmId);
            setString(13, hModUser);
            setRowId(14, hDcesRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_dbc_emboss not found!", "", comcr.hCallBatchSeqno);
            }
        } else {
            daoTable   = "dbc_emboss";
            updateSQL  = " class_code    = 'M',";
            updateSQL += " apply_id_code = ?,";
            updateSQL += " pm_id_code    = ?,";
            updateSQL += " acct_type     = ?,";
            updateSQL += " acct_key      = ?,";
            updateSQL += " cash_lmt      = ?,";
            updateSQL += " in_main_error = ?,";
            updateSQL += " in_main_date  = ?,";
            updateSQL += " in_main_msg   = ?,";
            updateSQL += " rtn_nccc_date = ?,";
            updateSQL += " check_code    = ?,";
            updateSQL += " mod_pgm       = ?,";
            updateSQL += " mod_user      = ?,";
            updateSQL += " mod_time      = sysdate";
            whereStr   = "where rowid    = ? ";
            setString(1, hDcesApplIdCode);
            setString(2, hDcesPmIdCode);
            setString(3, hDceAcctType);
            setString(4, hDcesAccKey);
            setInt(5, hDcesCashLmt);
            setString(6, hDcesInMainError);
            setString(7, hDcesInMainDate);
            setString(8, hDcesInMainMsg);
            setString(9, hDcesRtnNcccDate);
            setString(10, tmpCheckCode);
            setString(11, hModUser);
            setString(12, hModUser);
            setRowId(13, hDcesRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_dbc_emboss not found!", "", comcr.hCallBatchSeqno);
            }
        }
        if (debug == 1)
        	showLogMessage("I", "", "  888 update dbc_emboss2 ok =[" + "]");
        return (0);
    }

    /***********************************************************************/
    int updateDbaAcno2() throws Exception {
        daoTable   = "dba_acno";
        updateSQL  = " class_code ='M',";
        updateSQL += " mod_pgm    = ?,";
        updateSQL += " mod_user   = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr   = "where acct_type = ?  ";
        whereStr  += "  and acct_key  = ? ";
        setString(1, prgmId);
        setString(2, hModUser);
        setString(3, hDceAcctType);
        setString(4, hDcesAccKey);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dba_acno not found!", "", comcr.hCallBatchSeqno);
        }

        return (0);
    }

    /***********************************************************************/
    int updateEmboss() throws Exception {
        int changeCreditLimit;

        if (debug == 1)
            showLogMessage("I", "", "  888 update dbc_emboss=[" + rtnCode + "]" + errorMsg);

        changeCreditLimit = 0;
        /* 緊急新製卡需視為資料已回饋 */
        if (hDcesRtnNcccDate.length() <= 0) {
            hDcesRtnNcccDate = sysDate;
        }
        if (hDcesApplyId.equals(hDcesPmId)) {
            hDcesPmIdCode = hDcesApplIdCode;
        }
        hDcesInMainError = String.format("%1d", rtnCode);
        if (!hDcesInMainError.equals("0")) {
            hDcesInMainDate = "";
            hDcesInMainMsg = errorMsg;
        } else {
            hDcesInMainDate = sysDate;
        }

        if (pAcnoCreditAmt != pCreditAmt) {
            if (validFlag == 1) {
                if (pCreditAmt < pAcnoCreditAmt)
                    changeCreditLimit = 1;
            }
        }

        if (changeCreditLimit == 1) {
            daoTable   = "dbc_emboss";
            updateSQL  = " apply_id_code = ?,";
            updateSQL += " pm_id_code    = ?,";
            updateSQL += " acct_type     = ?,";
            updateSQL += " acct_key      = ?,";
            updateSQL += " cash_lmt      = ?,";
            updateSQL += " in_main_error = ?,";
            updateSQL += " in_main_date  = ?,";
            updateSQL += " in_main_msg   = ?,";
            updateSQL += " rtn_nccc_date = ?,";
            updateSQL += " auth_credit_lmt = ?,";
            updateSQL += " check_code    = ?,";
            updateSQL += " mod_pgm       = ?,";
            updateSQL += " mod_user      = ?,";
            updateSQL += " mod_time      = sysdate";
            whereStr   = "where rowid    = ? ";
            setString(1, hDcesApplIdCode);
            setString(2, hDcesPmIdCode);
            setString(3, hDceAcctType);
            setString(4, hDcesAccKey);
            setInt(5, hDcesCashLmt);
            setString(6, hDcesInMainError);
            setString(7, hDcesInMainDate);
            setString(8, hDcesInMainMsg);
            setString(9, hDcesRtnNcccDate);
            setDouble(10, pAcnoCreditAmt);
            setString(11, tmpCheckCode);
            setString(12, prgmId);
            setString(13, hModUser);
            setRowId(14, hDcesRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_dbc_emboss not found!", "", comcr.hCallBatchSeqno);
            }
        } else {
            daoTable   = "dbc_emboss";
            updateSQL  = " apply_id_code = ?,";
            updateSQL += " pm_id_code    = ?,";
            updateSQL += " acct_type     = ?,";
            updateSQL += " acct_key      = ?,";
            updateSQL += " cash_lmt      = ?,";
            updateSQL += " in_main_error = ?,";
            updateSQL += " in_main_date  = ?,";
            updateSQL += " in_main_msg   = ?,";
            updateSQL += " rtn_nccc_date = ?,";
            updateSQL += " check_code    = ?,";
            updateSQL += " mod_pgm       = ?,";
            updateSQL += " mod_user      = ?,";
            updateSQL += " mod_time      = sysdate";
            whereStr   = "where rowid    = ? ";
            setString(1, hDcesApplIdCode);
            setString(2, hDcesPmIdCode);
            setString(3, hDceAcctType);
            setString(4, hDcesAccKey);
            setInt(5, hDcesCashLmt);
            setString(6, hDcesInMainError);
            setString(7, hDcesInMainDate);
            setString(8, hDcesInMainMsg);
            setString(9, hDcesRtnNcccDate);
            setString(10, tmpCheckCode);
            setString(11, prgmId);
            setString(12, hModUser);
            setRowId(13, hDcesRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_dbc_emboss not found!", "", comcr.hCallBatchSeqno);
            }
        }
        if (debug == 1)
        	showLogMessage("I", "", "  888 update dbc_emboss ok =[" + "]");
        return (0);
    }

    /***********************************************************************/
    int checkCardNo(String cardno) throws Exception {
        String cCardNo = "";
        int cCnt = 0;

        cCardNo = cardno;
        sqlCmd = "select count(*) c_cnt ";
        sqlCmd += "  from dbc_card  ";
        sqlCmd += " where card_no = ? ";
        setString(1, cCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            cCnt = getValueInt("c_cnt");
        }
        if (cCnt > 0) {
        	tmpCheckCode = "E10";
            errorMsg = "此卡號已存在卡檔";
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    int updateErrorEmboss() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "  888 update error_emboss=[" + rtnCode + "]" + errorMsg);

        hDcesInMainError = String.format("%1d", rtnCode);
        hDcesInMainMsg = errorMsg;
        daoTable   = "dbc_emboss";
        updateSQL  = " in_main_error = ?,";
        updateSQL += " in_main_msg   = ?,";
        updateSQL += " check_code    = ?,";
        updateSQL += " mod_pgm       = ?,";
        updateSQL += " mod_user      = ?,";
        updateSQL += " mod_time      = sysdate";
        whereStr   = "where rowid    = ? ";
        setString(1, hDcesInMainError);
        setString(2, hDcesInMainMsg);
        setString(3, tmpCheckCode);
        setString(4, hModUser);
        setString(5, hModUser);
        setRowId(6, hDcesRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dbc_emboss duplicate", "", comcr.hCallBatchSeqno);
        }
        if (debug == 1)
        	showLogMessage("I", "", "  888 update error_emboss ok =[" + "]");

        return (0);
    }

    /***********************************************************************/
    int getPinData() throws Exception {
        if ((!hDcesOnlineMark.equals("1")) && (!hDcesOnlineMark.equals("2"))) {
        	tmpCheckCode = "E04";
            errorMsg = String.format("一般卡無PIN_BLOCK值");
            return (1);
        }

        hPinBlock = "";
        hCvv2 = "";
        hServiceCode = "";
        hPvki = "";
        sqlCmd = "select pvki,";
        sqlCmd += "cvv2,";
        sqlCmd += "pin_block,";
        sqlCmd += "service_code,";
        sqlCmd += "pvki ";
        sqlCmd += " from crd_cardno_data  ";
        sqlCmd += "where card_no = ? ";
        setString(1, hDcesCarNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hPvki         = getValue("pvki");
            hCvv2         = getValue("cvv2");
            hPinBlock    = getValue("pin_block");
            hServiceCode = getValue("service_code");
            hPvki         = getValue("pvki");
        } else {
            return 1;
        }
        if (hPinBlock.length() == 0) {
            return 1;
        }
        delPinFlag = 1;

        return (0);
    }

    /***************************************************************************/
    void initDbcIdno() {
        hDcioIdPSeqno = "";
        hDcioId = "";
        hDcioIdCode = "";
        hDcioChiName = "";
        hDcioVoiceNum = "";
        hDcioSex = "";
        hDcioMarriage = "";
        hDcioBirthday = "";
        hDcioEducation = "";
        hDcioStudent = "";
        hDcioNation = "";
        hDcioAssetValue = 0;
        hDcioServiceYear = 0;
        hDcioAnnualIncome = 0;
        hDcioStaffFlag = "";
        hDcioCreditFlag = "";
        hDcioCommFlag = "";
        hDcioPassportNo = "";
        hDcioOtherCntryCode = "";
        hDcioStaffBrNo = "";
        hDcioCommFlag = "";
        hDcioSalaryCode = "";
        hDcioResidentNo = "";
        hDcioOfficeAreaCode1 = "";
        hDcioOfficeTelNo1 = "";
        hDcioOfficeTelExt1 = "";
        hDcioOfficeAreaCode2 = "";
        hDcioOfficeTelNo2 = "";
        hDcioOfficeTelExt2 = "";
        hDcioHomeAreaCode1 = "";
        hDcioHomeTelNo1 = "";
        hDcioHomeTelExt1 = "";
        hDcioHomeAreaCode2 = "";
        hDcioHomeTelNo2 = "";
        hDcioHomeTelExt2 = "";
        hDcioResidentZip = "";
        hDcioResidentAddr1 = "";
        hDcioResidentAddr2 = "";
        hDcioResidentAddr3 = "";
        hDcioResidentAddr4 = "";
        hDcioResidentAddr5 = "";
        hDcioJobPosition = "";
        hDcioCompanyName = "";
        hDcioBusinessCode = "";
        hDcioCellarPhone = "";
        hDcioBbCall = "";
        hDcioFaxNo = "";
        hDcioCardSince = "";
        hDcioVoiceNum = "";
        hDcioCrtDate = "";
        hDcioCrtUser = "";
        hDcioAprDate = "";
        hDcioAprUser = "";
        hDcioModUser = "";
        hDcioModPgm = "";
        hDcioFstStmtCycle = "";
    }

    /***************************************************************************/
    void initDbcCard() {
        hDccdIbmIdCode = "";
        hDccdCardNo = "";
        hDccdId = "";
        hDccdIdCode = "";
        hDccdIdPSeqno = "";
        hDccdCorpPSeqno = "";
        hDccdCorpNo = "";
        hDccdCorpNoCode = "";
        hDccdCardType = "";
        hDccdUrgentFlag = "";
        hDccdGroupCode = "";
        hDccdPinBlock = "";
        hDccdIndivCrdLmt = 0;
        hDccdIndivInstLmt = 0;
        hDccdSourceCode = "";
        hDccdSupFlag = "";
        hDccdMajorRelation = "";
        hDccdMajorId = "";
        hDccdMajorIdCode = "";
        hDccdMajorIdPSeqno = "";
        hDccdMajorCardNo = "";
        hDccdMemberNote = "";
        hDccdMemberId = "";
        hDccdCurrentCode = "";
        hDccdEngName = "";
        hDccdRegBankNo = "";
        hDccdUnitCode = "";
        hDccdOldBegDate = "";
        hDccdOldEndDate = "";
        hDccdNewBegDate = "";
        hDccdNewEndDate = "";
        hDccdIssueDate = "";
        hDccdReissueDate = "";
        hDccdReissueReason = "";
        hDccdUpgradeStatus = "";
        hDccdUpgradeDate = "";
        hDccdChangeDate = "";
        hDccdApplyNo = "";
        hDccdPromoteDept = "";
        hDccdPromoteEmpNo = "";
        hDccdIntroduceId = "";
        hDccdIntroduceEmpNo = "";
        hDccdIntroduceName = "";
        hDccdProdNo = "";
        hDccdPvv = "";
        hDccdCvv = "";
        hDccdCvv2 = "";
        hDccdPinBlock = "";
        hDccdApplyChtFlag = "";
        hDccdApplyAtmFlag = "";
        hDccdEmbossData = "";
        hDccdPvki = "";
        hDccdBatchno = "";
        hDccdRecno = 0;
        hDccdOppostReason = "";
        hDccdOppostDate = "";
        hDccdNewCardNo = "";
        hDccdOldCardNo = "";
        hDccdAcctType = "";
        hDccdAcctKey = "";
        hDccdPSeqno = "";
        hDccdGpNo = "";
        hDccdFeeCode = "";
        hDccdCurrFeeCode = "";
        hDccdLostFeeCode = "";
        hDccdExpireChgDate = "";
        hDccdExpireChgFlag = "";
        hDccdExpireReason = "";
        hDccdCorpActFlag = "";
        hDccdOldActivateType = "";
        hDccdOldActivateFlag = "";
        hDccdOldActivateDate = "";
        hDccdActivateType = "";
        hDccdActivateFlag = "";
        hDccdActivateDate = "";
        hDccdSonCardFlag = "";
        hDccdSetCode = "";
        hDccdMailType = "";
        hDccdMailNo = "";
        hDccdStockNo = "";
        hDccdComboIndicator = "";
        hDccdAcctNo = "";
        hDccdIcFlag = "";
        hDccdBranch = "";
        hDccdMailAttach1 = "";
        hDccdMailAttach2 = "";
        hDccdOldBankActno = "";
        hDccdBankActno = "";
        hDccdCrtDate = "";
        hDccdCrtUser = "";
        hDccdAprDate = "";
        hDccdAprUser = "";
        hDccdModUser = "";
        hDccdModPgm = "";
        hDccdRowid = "";
        return;
    }

    /***************************************************************************/
    void initProcssData() {
        pCardNo = "";
        pRegBankNo = "";
        pRiskBankNo = "";
        pAcctType = "";
        pAcctKey = "";
        pAcctHolderId = "";
        pAcctHolderIdCode = "";
        pSupFlag = "";
        pClassCode = "";
        pActNo = "";
        pVipCode = "";
        pAcceptDm = "";
        pCorpAssureFlag = "";
        pCorpActFlag = "";
        pCreditAmt = 0;
        pInstAuthLocAmt = 0;
        pCorpNo = "";
        pCorpNoCode = "";
        pCreditActNo = "";
        pIdPSeqno = "";
        pCorpPSeqno = "";
        pCorpAcnoPSeqno = "";
        pPSeqno = "";
        pGpNo = "";
        pMailZip = "";
        pMailAddr1 = "";
        pMailAddr2 = "";
        pMailAddr3 = "";
        pMailAddr4 = "";
        pMailAddr5 = "";
        pComboIndicator = "";
        pComboAcctNo = "";
        return;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbcD011 proc = new DbcD011();
        int retCode = proc.mainProcess(args);     
        proc.programEnd(retCode);
    }
    
    /***********************************************************************/  
    public int insertCrdIdnoSeqno() throws Exception {
    	if (debug == 1)
            showLogMessage("I", "", "888  insert crd_idno_seqno=[" + hIntIdnoIdPSeqno + "]" + hDcesApplyId);
        
        setValue("id_no"             , hDcesApplyId);
        setValue("id_p_seqno"        , hIntIdnoIdPSeqno);
        setValue("id_flag"           , "");		
    	setValue("bill_apply_flag"   , "");
    	setValue("debit_idno_flag"   , "Y");

        daoTable = "crd_idno_seqno   ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_idno_seqno    error[dupRecord]= ";
            String err2 = hDcesApplyId;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************


}


