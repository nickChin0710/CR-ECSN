/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/07/07  V1.01.01  Lai        Initial                                    *
* 106/10/19  V1.39.01  詹曜維     BECS-1061018-087 附卡自動分期註記更正      *
* 106/10/31  V1.40.01  詹曜維     BECS-1061031-089 自動扣款抓act_no_l_ind    *
* 106/12/01  V1.41.01  林志鴻     BECS-1061201-097 並行月份同actm2080作法    *
* 108/01/07  V1.42.01  詹曜維     BECS-1080104-002 修正國籍誤帶問題          *
* 108/02/27  V1.43.01  詹曜維     RECS-1071108-003 修正授信戶年費問題        *
* 108/04/12  V1.44.01  詹曜維     BECS-1080411-022 修正電子帳單邏輯          *
* 108/12/19  V1.45.01  Rou     Update Table:ACT_ACNO、CRD_CARD、CRD_IDNO       *
* 109/02/10  V1.46.01  Wilson  帳單註記(同通訊(法人)) contact -> comm                 *
* 109/02/13  V1.47.01  Wilson  還原UPDATE crd_nccc_stat                        *
* 109/02/20  V1.48.01  Wilson  調整update crd_nccc_stat欄位規則                                                   *
* 109/02/26  V1.49.01  Wilson  修正corp_no_code                                *
* 109/03/27  V1.49.02  Wilson  e.group_code    = a.group_code                *
* 109/04/13  V1.49.03  Wilson  update crd_card新增欄位 、branch一律放mail_branch   *
* 109/06/13  V1.49.04  Wilson  insert_crd_idno_seqno                         *
* 109/06/22  V1.49.05  Wilson  續卡update值更正                                                                                           *
* 109/07/07  V1.49.06  Wilson  autopay_dc_flag預設為 'Y'                       *
* 109/08/17  V1.49.07  Wilson  p_acno_acct_key判斷修改                                                                      *
* 109/09/21  V1.49.08  Wilson  insert crd_idno_seqno 新增debit_idno_flag      *
* 109/11/02  V1.49.09  Wilson  拒絕行銷相關欄位邏輯調整                                                                                *
* 109/11/11  V1.49.10  Wilson  修正bug                                        *
* 109/11/12  V1.49.11  Wilson  調整update_crd_idno、update_act_acno位置                        *
* 109/11/18  V1.49.12  Wilson  修正bug                                        *
* 109/11/20  v1.49.13  Wilson  insert改成extendField                          *
* 109/12/01  v1.49.14  Wilson  insert_crd_card新增PAYMENT_NO_II               *
* 109/12/18  V1.00.15   shiyuqi       updated for project coding standard   *
* 110/06/24  V1.00.16  Wilson  區分branch跟mail_branch                         *
* 110/09/10  V1.00.17  Wilson  procNewCard的新製卡檢核新增認同集團碼條件                                       *
* 111/12/12  V1.00.18  Wilson  APPLY_NO改為取12碼、商務卡(個人)的ACCT_KEY改為P_SEQNO + 0 * 
* 111/12/30  V1.00.19  Wilson  取CARD_FEE_DATE前6碼調整                                                                      *    
* 112/01/16  V1.00.20  Wilson  insert crd_card增加clerk_id                    *
* 112/01/30  V1.00.21  Wilson  risk_bank_no不更新                                                                                  *
* 112/02/01  V1.00.22  Wilson  crd_idno、crd_card增加msg_flag、msg_purchase_amt*
* 112/02/09  V1.00.23  Wilson  移除risk_bank_no取3碼邏輯                                                                    *
* 112/02/16  V1.00.24  Wilson  insert&update act_acno增加e_mail_ebill_date    *
* 112/02/26  V1.00.25  Wilson  修正update act_acno欄位順序                                                              *
* 112/02/27  V1.00.26  Wilson  bill_apply_flag刪除4跟5                         *
* 112/03/01  V1.00.27  Wilson  自動扣款相關欄位調整                                                                                         *
* 112/03/02  V1.00.28  Wilson  mark month_purchase_lmt相關處理                                                *
* 112/03/09  V1.00.29  Wilson  update crd_emboss add check_code              *
* 112/03/10  V1.00.30  Wilson  update act_acno revolve_int_rate_year -> rcrate_year*
* 112/03/15  V1.00.31  Wilson  insert&update crd_idno add eng_name           *
* 112/03/18  V1.00.32  Wilson  修正pGpNo   = pAcnoGpNo                        *
* 112/03/20  V1.00.33  Wilson  商務卡一卡一戶                                                                                                    *
* 112/03/24  V1.00.34  Wilson  修正卡檔acno_flag問題                                                                               *
* 112/03/31  V1.00.35  Wilson  update act_acno add chg_addr_date             *
* 112/04/07  V1.00.36  Wilson  調整電子帳單專用郵件信箱處理邏輯                                                                   *
* 112/04/12  V1.00.37  Wilson  insert crd_card add electronic_code           *
* 112/04/13  V1.00.38  Wilson  帳號欄位補0                                      *
* 112/05/11  V1.00.39  Wilson  毀損補發取消檢核舊卡片不為有效卡                                                                  *
* 112/06/26  V1.00.40  Wilson  payment_no、payment_no_ii改由CrdD082處理                         *
* 112/07/14  V1.00.41  Wilson  insert crd_card add card_ref_num              *
* 112/07/18  V1.00.42  Wilson  掛失&偽冒補卡增加檢核條件                                                                               *
* 112/08/18  V1.00.43  Wilson  掛失&偽冒補卡異動新卡卡號於貴賓卡主檔                                                         *
* 112/08/26  V1.00.44  Wilson  修正弱掃問題                                                                                                         *
* 112/09/01  V1.00.45  Wilson  修正補發卡stmt_cycle空值問題                                                                 *
* 112/11/17  V1.00.46  Wilson  update crd_idno add credit_level_old          *
* 112/11/21  V1.00.47  Wilson  tmpCheckCode = ""                             *
* 112/11/26  V1.00.48  Wilson  商務卡檢核相同統編不可存在相同的活卡                                                            *
* 112/12/03  V1.00.49  Wilson  crd_item_unit不判斷卡種                                                                        *
* 112/12/14  V1.00.50  Wilson  是否收製卡費註記為Y且無申請電子帳單要收製卡費                                          *
* 113/01/04  V1.00.51  Wilson  risk_bank_no改放空白                                                                               *
*****************************************************************************/
package Crd;

import com.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CrdD011 extends AccessDAO {
    private String progname = "一般卡寫入主檔處理程式  113/01/04  V1.00.51 ";

    CommFunction   comm  = new CommFunction();
    CommRoutine    comr  = null;
    CommCrd        comc  = new CommCrd();
    CommString   commStr = new CommString();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();

    int debug = 1;

    String checkhome = "";
    String hCallErrorDesc = "";
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hBusiChiDate = "";
    int totalCnt = 0;
    int sucCnt = 0;
    int failCnt = 0;
    int writeCnt1 = 0;
    int writeCnt2 = 0;
    String tmpChar1 = "";
    String tmpChar2 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;
    int tempInt = 0;

    String pathName1 = "", pathName2 = "";
    int fi, fo;
    String endflag = "\r";
    int rptSeq = 0;
    int[] prefixN = { 5, 1, 1, 5, 1, 2, 5, 7, 7, 5, 4, 9, 5 };

    String mbosBatchno = "";
    double mbosRecno = 0;
    String mbosEmbossSource = "";
    String mbosEmbossReason = "";
    String mbosVirtualFlag = "";
    String mbosOnlineMark = "";
    String mbosrtnncccdate = "";
    String mbosCardNo = "";
    String mbosOldCardNo = "";
    String mbosPinBlock = "";
    String mbosCvv2 = "";
    String mbosPvki = "";
    String mbosGroupCode = "";
    String mbosSourceCode = "";
    String mbosCardType = "";
    String mbosBinNo = "";
    String mbosPmId = "";
    String mbosPmIdCode = "";
    String mbosPmBirthday = "";
    String mbosApplyId = "";
    String mbosApplyIdCode = "";
    String mbosBirthday = "";
    String mbosSupFlag = "";
    String mbosMajorCardNo = "";
    String mbosUnitCode = "";
    String mbosToNcccDate = "";
    String mbosVoiceNum = "";
    String mbosStmtCycle = "";
    String mbosAcctType = "";
    String mbosInMainError = "";
    String mbosInMainDate = "";
    String mbosInMainMsg = "";
    String mbosStatSendInternet = "";
    String mbosActNo = "";
    String mbosActNoL = "";
    String mbosActNoF = "";
    String mbosCurrCode = "";
    String mbosAgreeLInd = "";
    String mbosActNoFInd = "";
    String mbosChiName = "";
    String mbosElectronicCode = "";
    String mbosCorpNo = "";
    String mbosCorpNoCode = "";
    String mbosCorpActFlag = "";
    String mbosCardIndicator = "";
    int mbosCashLmt = 0;
    String mbosIntroduceNo = ""; // promote_emp_no
    String mbosIntroduceId = "";
    String mbosIntroduceName = "";
    String mbosAcceptDm = "";
    String mbosSuperNote = "";
    String mbosRowid = "";
    String mbosActNoLInd = "";
    String mbosStudent = "";
    String mbosEstGraduateMonth = "";
    String mbosResidentZip = "";
    String mbosResidentAddr1 = "";
    String mbosResidentAddr2 = "";
    String mbosResidentAddr3 = "";
    String mbosResidentAddr4 = "";
    String mbosResidentAddr5 = "";
    String mbosSpecialCardRate = "";
    String mbosCardRefNum = "";
    String mbosCrtBankNo = "";
    String mbosVdBankNo = "";
    String mbosSpouseName = "";
    String mbosSpouseIdNo = "";
    String mbosResidentNoExpireDate = "";
    String mbosGraduationElementarty = "";
    String mbosUrFlag = "";
    String mbosBusinessCode = "";
    String mbosInstFlag = "";
    String mbosCreditLevelNew = "";
    String mbosMailZip = "";
    String mbosMailAddr1 = "";
    String mbosMailAddr2 = "";
    String mbosMailAddr3 = "";
    String mbosMailAddr4 = "";
    String mbosMailAddr5 = "";
    String mbosCompanyZip = "";
    String mbosCompanyAddr1 = "";
    String mbosCompanyAddr2 = "";
    String mbosCompanyAddr3 = "";
    String mbosCompanyAddr4 = "";
    String mbosCompanyAddr5 = "";
    String mbosFeeCodeI = "";
    Double mbosRevolveIntRate;
    String mbosEMailAddr = "";
    String mbosFlFlag = "";
    String mbosAutopayAcctBank = "";
    String mbosBillApplyFlag = "";
    String mbosCurrChangeAccout = "";
    String mbosAcctKey = "";
    String mbosMarketAgreeBase = "";
    String mbosENews = "";
    String mbosAcceptCallSell = "";
    String mbosAcceptSms = "";
    String mbosClerkId = "";
    int mbosSmsAmt = 0;
    String tmpSpecialCardRateFlag = "";
    String tmpMsgFlag = "";
    int tmpMsgPurchaseAmt = 0;
    Double mbosRevolveIntRateYear;
    String mbosEngName = "";
    String mbosSonCardFlag = "";

    String hCardAcnoFlag = "";

    String pgcCrdMakecardFeeFlag = "";
    String pgcCrdMakecardFee = "";
    String hTempThisAcctMonth = "";
    String hTempNextAcctMonth = "";
    String procType = "";
    int psctPaper2EmailMm = 0;
    int psctEmail2PaperMm = 0;

    String hComboIndicator = "";
    String hFCurrencyFlag = "";
    int delPinFlag = 0;
    int rtnCode = 0;
    int rtn = 0;
    int rptCnt = 0, rptCnt1 = 0;
    int pageCnt = 0, pageCnt1 = 0;
    String errorMsg = "";
    String hPinBlock = "";
    String hCvv2 = "";
    String hPvki = "";
    String swNew = "";
    String swMove = "";
    String moveChiName = "";
    String moveCardNo = "";
    String moveRowid = "";
    String cardAutoInstallment = "";
    String pgcdGroupName = "";
    String hStmtCycle = "";
    String hRcUseFlag = "";
    String hCardIndicator = "";
    String hMajorIdPSeqno = "";
    String hMajorCardNo = "";
    String hCardNote = "";
    String hBinType = "";
    String hBinNo = "";
    String hIdnoAcceptCallSell = "";
    String hIdnoIdPSeqno = "";
    String hIdnoChiName = "";
    String hIdnoId = "";
    String hIdnoIdCode = "";
    String hIdnoCellarPhone = "";
    String hIdnoBirthday = "";
    String hFstStmtCycle = "";
    String hIdRowid = "";
    String paccAtmCode = "";
    int paccCashadvLocMaxamt = 0;
    int paccCashadvLocRate = 0;
    int paccCashadvLocRateOld = 0;
    int paccBreachNumMonth = 0;
    int hPpltPercentC = 0;
    int hPpltLfixC = 0;

    String pCardNo = "";
    String pRegBankNo = "";
    String pRiskBankNo = "";
    String pAcctType = "";
    String pAcctKey = "";
    String pAcctGpNo = "";
    String pAcctHolderId = "";
    String pAcctHolderIdCode = "";
    String pSupFlag = "";
    String pClassCode = "";
    String pActNo = "";
    String pVipCode = "";
    String pAcceptDm = "";
    String pCorpAssureFlag = "";
    String pCorpActFlag = "";
    int pCreditAmt = 0;
    double pInstAuthLocAmt = 0;
    String pCorpNo = "";
    String pCorpNoCode = "";
    String pCreditActNo = "";
    String pIdPSeqno = "";
    String pCorpAcnoPSeqno = "";
    String pMailZip = "";
    String pMailAddr1 = "";
    String pMailAddr2 = "";
    String pMailAddr3 = "";
    String pMailAddr4 = "";
    String pMailAddr5 = "";
    String pStmtCycle = "";
    String pComboAcctNo = "";
    double pCashRate = 0;
    int validflag = 0;
    String hAcctKey = "";

    String pPSeqno = "";
    String pGpNo = "";
    String pOrgRiskBankNo = "";
    String pAcnoAcctKey = "";
    String pAcnoPSeqno = "";
    String pAcnoGpNo = "";
    String pAcnoRegBankNo = "";
    String pAcnoClassCode = "";
    String pAcnoVipCode = "";
    int pAcnoCreditAmt = 0;
    int pAcnoCreditAmtCash = 0;
    int pAcnoComboCashLimit = 0;
    int hAcnoComboCashLimit = 0;
    String pAcnoComboAcctNo = "";
    String pAcnoStatSendInternet = "";
    String pAcnoStmtCycle = "";
    String pAcnoCreditActNo = "";
    String pAcnoStopStatus = "";
    String hAcnoAutopayAcctBank = "";
    String hAcnoAutopayAcctNo = "";
    String hAcnoAutopayId = "";
    String pAcnoCorpActFlag = "";
    String pNewAcctFlag = "";
    String hAcnoAutopayIndicator = "";
    String hAcnoAutopayAcctEDate = "";
    String pAcnoRowid = "";
    String hCknoOldAcctBank = "";
    String hCknoOldAcctNo = "";
    String hCknoProcMark = "";
    String hCknoAutopayIndicator = "1";
    String hCknoModPgm = "";
    String hCknoPSeqno = "";
    int acnoExitFlag = 0;;
    int pComboCashLimit = 0;
    String pAutopayIndicator = "";
    String hOppostDate = "";
    int hTempLineOfCreditAmtCash = 0;
    String hFancyLimitFlag = "";
    double tempComboLimit = 0;
    String hTempActNo = "";
    String hTempValid = "";
    String hCknoOldAcctId = "";
    String hAadkFuncCode = "";
    String hAadkCurrCode = "";
    String hAadkCardNo = "";
    String hAadkAutopayAcctBank = "006";
    String hAadkAutopayAcctNo = "";
    String hTtttAutopayAcctNo = "";
    String hAcurAutopayDcFlag = "";
    String hCardOppostDate = "";
    String hCardActivateFlag = "";
    String hCardStmtCycle = "";
    String hCardPSeqno = "";
    String hCardGpNo = "";
    String hCardIdPSeqno = "";
    String hCardCardType = "";
    String hCardGroupCode = "";
    String hCardSourceCode = "";
    String hCardUnitCode = "";
    String hCardSupFlag = "";
    String hCardCurrentCode = "";
    String hCardMemberId = "";
    String hCardEngName = "";
    String hCardRegBankNo = "";
    String hCardPinBlock = "";
    String hCardNewBegDate = "";
    String hcardnewenddate = "";
    String hCardCardNo = "";
    String hCardCorpNo = "";
    String hCardCorpNoCode = "";
    String hCardCorpPSeqno = "";
    String hCardCorpActFlag = "";
    String hCardMajorIdPSeqno = "";
    String hOldMajorCardNo = "";
    String hCardIssueDate = "";
    String hCardCardFeeDate = "";
    String hCardMajorCardNo = "";
    String hCardPromoteEmpNo = "";
    String hCardApplyAtmFlag = "";
    double hCardJcicScore = 0;
    String hCardCardMoldFlag = "";
    String hCardSetCode = "";
    String hCardApplyNo = "";
    String hCardCvv2 = "";
    String hCardPvki = "";
    String hCardEmbossData = "";
    String hCardMajorRelation = "";
    String hCardAcctType = "";
    String hCardIntroduceId = "";
    String hCardIntroduceName = "";
    String hCardFeeCode = "";
    String hCardBatchno = "";
    double hCardRecno = 0;
    String hCardMailType = "";
    String hCardMailNo = "";
    String hCardComboIndicator = "";
    String hCardSonCardFlag = "";
    int hCardIndivCrdLmt = 0;
    String hCardIcFlag = "";
    String hCardBranch = "";
    String hCardMailAttach1 = "";
    String hCardMailAttach2 = "";
    String hCardCurrCode = "";
    String hCardBankActno = "";
    String hCardFancyLimitFlag = "";
    String hCardOldBankActno = "";
    String hCardMailBranch = "";
    String hCardStockNo = "";
    double hCardComboBegBal = 0;
    double hCardComboEndBal = 0;
    String hCardComboAcctNo = "";
    String hCardEmergentFlag = "";
    String hCardActivateDate = "";
    String hCardOldActivateDate = "";
    String hCardOldActivateFlag = "";
    String hCardOldActivateType = "";
    String hCardExpireChgDate = "";
    String hCardExpireChgFlag = "";
    String hCardExpireReason = "";
    String hCardCurrFeeCode = "";
    String hCardApplyChtFlag = "";
    String hCardNewCardNo = "";
    String hCardOldCardNo = "";
    String hCardOppostReason = "";
    String hCardTransCvv2 = "";
    String hCardCvv = "";
    double hCardIndivInstLmt = 0;
    String hCardPvv = "";
    String hCardProdNo = "";
    String hCardIntroduceEmpNo = "";
    String hCardPromoteDept = "";
    String hCardUpgradeDate = "";
    String hCardChangeDate = "";
    String hCardActivateType = "";
    String hCardReissueReason = "";
    String hCardReissueDate = "";
    String hCardOldBegDate = "";
    String hCardOldEndDate = "";
    String hCardAutoInstallment = "";
    String hCardUrgentFlag = "";
    String hCardChangeStatus = "";
    String hCardReissueStatus = "";
    String hCardUpgradeStatus = "";
    String hCardCardRefNum = "";
    String hCardSpecialCardRateFlag = "";
    String hCardSpecialCardRate = "";
    String hCardFlFlag = "";
    String hCardPaymentNoII = "";
    String hCardClerkId = "";
    String hCardMsgFlag = "";
    int hCardMsgPurchaseAmt = 0;

    String hIdnoFstStmtCycle = "";
    String hIdnoStaffFlag = "";
    String hIdnoSex = "";
    String hIdnoMarriage = "";
    String hIdnoEducation = "";
    String hIdnoStudent = "";
    String hIdnoNation = "";
    String hIdnoServiceYear = "";
    double hIdnoAnnualIncome = 0;
    double hIdnoAssetValue = 0;
    String hIdnoCreditFlag = "";
    String hIdnoCommFlag = "";
    String hIdnoSalaryCode = "";
    String hIdnoVoiceNum = "";
    String hIdnoCardSince = "";
    String hIdnoAnnualDate = "";

    String hIdnoOfficeAreaCode1 = "";
    String hIdnoOfficeTelNo1 = "";
    String hIdnoOfficeTelExt1 = "";
    String hIdnoOfficeAreaCode2 = "";
    String hIdnoOfficeTelNo2 = "";
    String hIdnoOfficeTelExt2 = "";
    String hIdnoHomeAreaCode1 = "";
    String hIdnoHomeTelNo1 = "";
    String hIdnoHomeTelExt1 = "";
    String hIdnoHomeAreaCode2 = "";
    String hIdnoHomeTelNo2 = "";
    String hIdnoHomeTelExt2 = "";
    String hIdnoResidentZip = "";
    String hIdnoResidentAddr1 = "";
    String hIdnoResidentAddr2 = "";
    String hIdnoResidentAddr3 = "";
    String hIdnoResidentAddr4 = "";
    String hIdnoResidentAddr5 = "";
    String hIdnoJobPosition = "";
    String hIdnoCompanyName = "";
    String hIdnoBusinessCode = "";
    String hIdnoEMailAddr = "";
    String hIdnoContactor1Name = "";
    String hIdnoContactor1Relation = "";
    String hIdnoContactor1AreaCode = "";
    String hIdnoContactor1Tel = "";
    String hIdnoContactor1Ext = "";
    String hIdnoContactor2Name = "";
    String hIdnoContactor2Relation = "";
    String hIdnoContactor2AreaCode = "";
    String hIdnoContactor2Tel = "";
    String hIdnoContactor2Ext = "";
    String hIdnoEstGraduateMonth = "";
    String hIdnoMarketAgreeBase = "";
    String hIdnoVacationCode = "";
    String hIdnoMarketAgreeAct = "";
    String hIdnoAcceptDm = "";
    String hIdnoCreateDate = "";
    String hIdnoModUser = "";
    String hIdnoOtherCntryCode = "";

    String acnoNoAdjLocHigh = "";
    String acnoNoAdjLocHighSDate = "";
    String acnoNoAdjLocHighEDate = "";

    String hCorpCorpPSeqno = "";
    String hCorpAcnoPSeqno = "";
    String hCorpCreditActNo = "";
    String hCorpStmtCycle = "";
    String hCorpCommZip = "";
    String hCorpCommAddr1 = "";
    String hCorpCommAddr2 = "";
    String hCorpCommAddr3 = "";
    String hCorpCommAddr4 = "";
    String hCorpCommAddr5 = "";
    String hCorpRegZip = "";
    String hCorpRegAddr1 = "";
    String hCorpRegAddr2 = "";
    String hCorpRegAddr3 = "";
    String hCorpRegAddr4 = "";
    String hCorpRegAddr5 = "";
    String hTempOldEndMonth = "";
    String hTempPSeqno = "";
    String hTempIdPSeqno = "";
    String hSmdlMsgPgm = "";
    String hSmidMsgId = "";
    String hSmidMsgDept = "";
    String hSmidMsgSendFlag = "";
    String hSmidMsgSelAcctType = "";
    String hSmidMsgSelAmt01 = "";
    String hSmidMsgUserid = "";
    double hSmidMsgAmt01 = 0;
    int hSmidMsgRunDay = 0;
    String szTmp = "";
    String hOldCardNo = "";
    String hThirdDataReissue = "N";
    int hIdnoCount = 0;
    int hEmplCount = 0;
    
    String actEMailEbill = "";
    Double actRevolveIntRate;
    String actBillApplyFlag = "";
    String actAutopayAcctBank = "";
    String actAutopayAcctNo = "";
    String actAutopayIndicator = "";
    String actAutopayId = "";
    String actAutopayIdCode = "";
    String actAutopayAcctSDate = "";
    String actBillSendingZip = "";
    String actBillSendingAddr1 = "";
    String actBillSendingAddr2 = "";
    String actBillSendingAddr3 = "";
    String actBillSendingAddr4 = "";
    String actBillSendingAddr5 = "";
    String actEMailEbillDate = "";
    Double actRevolveIntRateYear;
    String actRevolveRateSMonth = "";
    String actChgAddrDate = "";
    String emapCardType = "";
    String emapGroupCode = "";
    String emapApplyId = "";
    String emapCorpNo = "";
    String emapRowid = "";
    String emapNcccType = "";

    String idnoSpouseName = "";
    String idnoSpouseIdNo = "";
    String idnoResidentNoExpireDate = "";
    String idnoGraduationElementarty = "";
    String idnoUrFlag = "";
    String idnoBusinessCode = "";
    String idnoInstFlag = "";
    String idnoCreditLevelNew = "";
    String idnoCreditLevelOld = "";
    String idnoMailZip = "";
    String idnoMailAddr1 = "";
    String idnoMailAddr2 = "";
    String idnoMailAddr3 = "";
    String idnoMailAddr4 = "";
    String idnoMailAddr5 = "";
    String idnoCompanyZip = "";
    String idnoCompanyAddr1 = "";
    String idnoCompanyAddr2 = "";
    String idnoCompanyAddr3 = "";
    String idnoCompanyAddr4 = "";
    String idnoCompanyAddr5 = "";
    String idnoFeeCodeI = "";
    String idnoEMailAddr = "";
    String idnoResidentZip = "";
    String idnoResidentAddr1 = "";
    String idnoResidentAddr2 = "";
    String idnoResidentAddr3 = "";
    String idnoResidentAddr4 = "";
    String idnoResidentAddr5 = "";
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
    
    String tmpBillSendingZip   = ""; 
    String tmpBillSendingAddr1 = ""; 
    String tmpBillSendingAddr2 = ""; 
    String tmpBillSendingAddr3 = ""; 
    String tmpBillSendingAddr4 = ""; 
    String tmpBillSendingAddr5 = ""; 

    double tempCashUseBalance1 = 0;
    double tempCashUseBalanceO = 0;
    int chgPhoneFlag = 0;
    String hDd = "";
    String ncccFilename = "";
    String ncccFilename1 = "";
    int hRecCnt1 = 0;
    String tmpCheckCode = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdD011 proc = new CrdD011();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            checkhome = comc.getECSHOME();
            showLogMessage("I", "", javaProgram + " " + progname);

//            if (args.length > 3 || args.length < 1) {
//                String err1 = "CrdD011 0/1 [seq_no]\n";
//                String err2 = "CrdD011 0(一般)/1(online緊急製卡) [seq_no]";
//                System.out.println(err1);
//                comc.err_exit(err1, err2);
//            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

//            proc_type = args[0];
            showLogMessage("I", "", "參數=" + procType);

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

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
            selectPtrStatCoexist();

//            if (select_ecs_simp_ctl() != 0) {
//                String err1 = "瘦身移回作業尚未處理, 程式終止執行 !!" + h_busi_business_date;
//                String err2 = "";
//                comcr.err_rtn(err1, err2, comcr.h_call_batch_seqno);
//            }

            if (openTextFile() != 0) {
                String err1 = "open_text_file error !!";
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }

            totalCnt = 0;

            selectCrdEmboss();

            if (writeCnt1 > 0) {
                writeTailer();
                comc.writeReport(pathName1, lpar1);
            }
            if (writeCnt2 > 0) {
                writeTailer();
                comc.writeReport(pathName1, lpar2);
            }

            comcr.hCallErrorDesc = "程式執行結束,總處理筆數=[" + totalCnt + "],成功筆數=[" + sucCnt + "],失敗筆數=[" + failCnt + "]";
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
        selectSQL = "business_date   , " + "substr(business_date,1,6)    as SYSTEM_THIS , "
                + "to_char(add_months(to_date(business_date,'yyyymmdd'),1),'yyyymm') " + "        as SYSTEM_NEXT , "
                + "to_char(sysdate,'yyyymmdd')  as SYSTEM_DATE , " + "to_char(sysdate,'dd')        as SYSTEM_DD     ";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

//        h_busi_business_date = getValue("BUSINESS_DATE");
        hBusiBusinessDate = sysDate;
        hTempThisAcctMonth = getValue("SYSTEM_THIS");
        hTempNextAcctMonth = getValue("SYSTEM_NEXT");
//        long h_long_chi_date = Long.parseLong(h_busi_business_date) - 19110000;
//        h_busi_chi_date = Long.toString(h_long_chi_date);
        hDd = getValue("SYSTEM_DD");

        showLogMessage("I","","本日營業日:["+ hBusiBusinessDate + "]" + hTempNextAcctMonth
                + "," + hTempThisAcctMonth);
    }
    // ************************************************************************
    public void selectPtrStatCoexist() throws Exception {
        selectSQL = "paper2email_mm, email2paper_mm ";
        daoTable = "ptr_stat_coexist ";
        whereStr = "where apr_flag   = 'Y'  ";

        tmpInt = selectTable();

        psctPaper2EmailMm = getValueInt("paper2email_mm");
        psctEmail2PaperMm = getValueInt("email2paper_mm");

        showLogMessage("I", "", " stat_coexist="+ psctPaper2EmailMm +","+ psctEmail2PaperMm);
    }
    // ************************************************************************
    public int selectEcsSimpCtl() throws Exception {
        selectSQL = "proc_date, return_date ";
        daoTable  = "ecs_simp_ctl ";
        whereStr  = "where crt_date    = ? " + "  and crt_pgm     = 'EcsS005' ";

        setString( 1 , sysDate );
        //  setString(1, h_busi_business_date);

        int recordCnt = selectTable();

        showLogMessage("I", "", "simp_ctl[" + recordCnt + "] [" + getValue("proc_date") + "]");

        if (recordCnt == 0)
            return (1);
        if (getValue("proc_date").length() == 0 || getValue("return_date").length() == 0)
            return (1);

        return (0);
    }

    // ************************************************************************
    public int openTextFile() throws Exception {
        int val = 0;

        ncccFilename = "CRD_D011R1";
        pathName1 = checkhome + "/media/crd/NCCC/" + ncccFilename;
        ncccFilename1 = "CRD_D011R2";
        pathName2 = checkhome + "/media/crd/NCCC/" + ncccFilename1;

        /*
         * setConsoleMode("N"); fo = openOutputText(pathName1); if ( fi == -1 )
         * { String err1= "程式執行目錄下沒有權限讀寫資料 error !!"; String err2= pathName1;
         * comcr.err_rtn(err1 , err2 , comcr.h_call_batch_seqno); }
         * setConsoleMode("Y");
         */

        showLogMessage("I", "", " Process file=" + pathName1);

        return (0);
    }

    // ************************************************************************
    public void selectCrdEmboss() throws Exception {

        // ***************************************************************
        // check emboss_source
        // 1:新製卡 2:普昇金卡 4:換卡 5:毀損重製 6:掛失補發
        // 7:緊急補發卡 8:星座卡毀損重製9:重送件
        // ***************************************************************

        selectSQL = " DISTINCT  a.batchno   ,a.recno                 "
                + " , a.emboss_source       ,a.emboss_reason         "
                + " , a.source_batchno      ,a.source_recno          "
                + " , a.aps_batchno         ,a.aps_recno             "
                + " , a.to_nccc_date        ,a.seqno                 "
                + " , a.to_nccc_code        ,a.card_type             "
                + " , a.acct_type           ,a.acct_key              "
                + " , a.class_code          ,a.sup_flag              "
                + " , a.unit_code           ,a.pin_block             "
                + " , a.card_no             ,a.major_card_no         "
                + " , a.bin_no              ,a.major_card_no         "
                + " , a.major_valid_fm      ,a.major_valid_to        "
                + " , a.major_chg_flag      ,a.old_card_no           "
                + " , a.change_reason       ,a.status_code           "
                + " , a.apply_id            ,a.apply_id_code         "
                + " , a.pm_id               ,a.pm_id_code            "
                + " , a.group_code          ,a.source_code           "
                + " , a.corp_no             ,a.corp_no_code      ,a.corp_assure_flag      "
                + " , decode(a.corp_act_flag,'','N',a.corp_act_flag) as corp_act_flag  "
                + " , a.reg_bank_no         ,a.risk_bank_no          "
                + " , a.chi_name            ,a.eng_name              "
                + " , a.birthday            ,a.marriage              "
                + " , a.rel_with_pm         ,a.service_year          "
                + " , a.education           ,a.nation                "
                + " , a.salary              ,a.mail_zip              "
                + " , a.resident_no         ,a.passport_no           "
                + " , a.other_cntry_code    ,a.staff_flag            "
                + " , a.credit_flag         ,a.comm_flag             "
                + " , a.mail_addr1          ,a.mail_addr2            "
                + " , a.mail_addr3          ,a.mail_addr4            "
                + " , a.mail_addr5          ,a.resident_zip          "
                + " , a.resident_addr1      ,a.resident_addr2        "
                + " , a.resident_addr3      ,a.resident_addr4        "
                + " , a.resident_addr5      ,a.company_name          "
                + " , a.job_position        ,a.home_area_code1       "
                + " , a.home_tel_no1        ,a.home_tel_ext1         "
                + " , a.home_area_code2     ,a.home_tel_no2          "
                + " , a.home_tel_ext2       ,a.office_area_code1     "
                + " , a.office_tel_no1      ,a.office_tel_ext1       "
                + " , a.office_area_code2   ,a.office_tel_no2        "
                + " , a.office_tel_ext2     ,a.e_mail_addr           "
                + " , a.cellar_phone        ,a.act_no                "
                + " , a.vip                 ,a.fee_code              "
                + " , a.force_flag          ,a.business_code         "
                + " , a.introduce_no        ,a.valid_fm              "
                + " , a.valid_to            ,a.sex                   "
                + " , a.value               ,a.e_news                "
                + " , a.apply_no            ,a.electronic_code       "
                + " , a.mail_type           ,a.mail_no               "
                + " , a.introduce_id        ,a.introduce_name        "
                + " , a.salary_code         ,a.student               "
                + " , a.credit_lmt          ,a.police_no1            "
                + " , a.police_no2          ,a.police_no3            "
                + " , a.pm_cash             ,a.sup_cash              "
                + " , decode(a.online_mark,'','0',a.online_mark) as online_mark  "
                + " , a.reject_code         ,a.emboss_4th_data       "
                + " , a.member_id           ,a.pm_birthday           "
                + " , a.sup_birthday        ,a.standard_fee          "
                + " , a.final_fee_code      ,a.fee_reason_code       "
                + " , a.annual_fee          ,a.chg_addr_flag         "
                + " , a.pvv                 ,a.cvv                   "
                + " , a.trans_cvv2          ,a.pvki                  "
                + " , a.stmt_cycle          ,a.rtn_nccc_date         "
                + " , a.open_passwd         ,a.voice_passwd          "
                + " , a.service_code        ,a.cntl_area_code        "
                + " , a.stock_no            ,a.old_beg_date          "
                + " , a.old_end_date        ,a.emboss_result         "
                + " , a.diff_code           ,a.credit_error          "
                + " , a.auth_credit_lmt     ,a.bank_actno            "
                + " , a.cht_passwd          ,a.cht_date              "
                + " , a.son_card_flag       ,a.indiv_crd_lmt         "
                + " , a.ic_flag             ,a.branch                "
                + " , a.mail_attach1        ,a.mail_attach2          "
                + " , a.contactor1_name     ,a.contactor1_relation   "
                + " , a.contactor1_area_code,a.contactor1_tel        "
                + " , a.contactor1_ext      ,a.contactor2_name       "
                + " , a.contactor2_relation ,a.contactor2_area_code  "
                + " , a.contactor2_tel      ,a.contactor2_ext        "
                + " , a.est_graduate_month  ,a.market_agree_base     "
                + " , a.vacation_code       ,a.market_agree_act      "
                + " , a.fancy_limit_flag    ,a.combo_indicator       "
                + " , a.fh_flag             ,a.jcic_score            "
                + " , a.stat_send_internet  ,a.agree_l_ind           "
                + " , a.dc_indicator        ,a.curr_code             "
                + " , a.act_no_f            ,a.act_no_l              "
                + " , decode(a.act_no_f_ind,'','1',a.act_no_f_ind) as act_no_f_ind  "
                + " , a.act_no_l_ind        ,a.send_pwd_flag         "
                + " , b.cashadv_loc_rate    ,b.cashadv_loc_maxamt    "
                + " , b.cashadv_loc_rate_old,b.breach_num_month      "
                + " , b.card_indicator      ,b.atm_code              "
                + " , c.cash_limit_rate     ,c.card_mold_flag        "
                + " , decode(d.virtual_flag,'','N',d.virtual_flag) as virtual_flag  "
                + " , a.super_note                                   "
                + " , a.special_card_rate   ,a.card_ref_num          "
                + " , a.crt_bank_no         ,a.vd_bank_no            "
                + " , a.spouse_id_no                                 "
                + " , a.resident_no_expire_date	,a.graduation_elementarty "
                + " , a.ur_flag	            ,a.business_code         "
                + " , a.inst_flag	        ,a.credit_level_new      "
                + " , a.company_zip         ,a.company_addr1         "
                + " , a.company_addr2       ,a.company_addr3         "
                + " , a.company_addr4       ,a.company_addr5         "
                + " , a.fee_code_i          ,a.curr_change_accout    "
                + " , a.revolve_int_rate    ,a.fl_flag               "
                + " , a.autopay_acct_bank   ,a.bill_apply_flag       "
                + " , e.crd_makecard_fee_flag , e.crd_makecard_fee   "
                + " , a.mail_branch         ,a.revolve_int_rate_year "
                + " , a.sms_amt             ,a.clerk_id              "
                + " , a.rowid as rowid      " ;
        daoTable = "crd_emboss a, ptr_acct_type b, ptr_group_card c, crd_item_unit d, ptr_group_code e ";
        whereStr = "where a.in_main_date  = ''  "
                + "  and ((a.to_nccc_date <> ''  AND a.rtn_nccc_date <> '')  or "
                + "       (d.virtual_flag  = 'Y') or "
                +      "  (a.online_mark = '1' or a.online_mark = '2')" + "   ) "
                + "  and a.card_no      <> ''  "
                + "  and a.reject_code   = ''  "
                + "  and b.acct_type     = a.acct_type   "
                + "  and c.group_code    = a.group_code  "
                + "  and c.card_type     = a.card_type   "
                + "  and d.unit_code     = a.unit_code   "
//                + "  and d.card_type     = a.card_type   "
                + "  and e.group_code    = a.group_code  "
                + "order by a.card_type,a.group_code,a.sup_flag,a.batchno,a.recno ";

        openCursor();

        while (fetchTable()) {
            initRtn();

            mbosBatchno = getValue("batchno");
            mbosRecno = getValueDouble("recno");
            mbosEmbossSource = getValue("emboss_source");
            mbosEmbossReason = getValue("emboss_reason");
            mbosSupFlag = getValue("sup_flag");
            mbosVirtualFlag = getValue("virtual_flag");
            mbosOnlineMark    = getValue("online_mark");
            mbosrtnncccdate = getValue("rtn_nccc_date");
            mbosCvv2 = getValue("trans_cvv2");
            mbosPvki = getValue("pvki");
            mbosPinBlock = getValue("pin_block");
            mbosCardNo = getValue("card_no");
            mbosCorpNo = getValue("corp_no");
            mbosCorpNoCode = getValue("corp_no_code");
            mbosCorpActFlag = getValue("corp_act_flag");
            mbosCardIndicator = getValue("card_indicator");
            mbosMajorCardNo = getValue("major_card_no");
            mbosOldCardNo = getValue("old_card_no");
            mbosGroupCode = getValue("group_code");
            mbosSourceCode = getValue("source_code");
            mbosCardType = getValue("card_type");
            mbosBinNo = getValue("bin_no");
            mbosPmId = getValue("pm_id");
            mbosPmIdCode = getValue("pm_id_code");
            mbosPmBirthday = getValue("pm_birthday");
            mbosApplyId = getValue("apply_id");
            mbosApplyIdCode = getValue("apply_id_code");
            if (mbosPmIdCode.length() == 0)
                mbosPmIdCode = "0";
            if (mbosApplyIdCode.length() == 0)
                mbosApplyIdCode = "0";
            mbosBirthday = getValue("birthday");
            mbosUnitCode = getValue("unit_code");
            mbosToNcccDate = getValue("to_nccc_date");
            mbosVoiceNum = getValue("voice_passwd");
            mbosStmtCycle = getValue("stmt_cycle");
            mbosAcctType = getValue("acct_type");
            mbosActNo = getValue("act_no");
            mbosActNoL = getValue("act_no_l");
            if(!mbosActNoL.equals("")) {
            	mbosActNoL = String.format("%016d",Long.parseLong(mbosActNoL));
            }            
            mbosActNoF = getValue("act_no_f");
            if(!mbosActNoF.equals("")) {
            	mbosActNoF = String.format("%016d",Long.parseLong(mbosActNoF));
            }            
            mbosCurrCode = getValue("curr_code");
            mbosAgreeLInd = getValue("agree_l_ind");
            mbosActNoFInd = getValue("act_no_f_ind");
            mbosChiName = getValue("chi_name");
            mbosElectronicCode = getValue("electronic_code");
            mbosIntroduceNo = getValue("introduce_no");
            mbosIntroduceId = getValue("introduce_id");
            mbosIntroduceName = getValue("introduce_name");
            mbosSuperNote = getValue("super_note");
            mbosRowid = getValue("rowid");
            mbosActNoLInd = getValue("act_no_l_ind");
            mbosStudent = getValue("student");
            mbosEstGraduateMonth = getValue("est_graduate_month");
            mbosResidentZip = getValue("resident_zip");
            mbosResidentAddr1 = getValue("resident_addr1");
            mbosResidentAddr2 = getValue("resident_addr2");
            mbosResidentAddr3 = getValue("resident_addr3");
            mbosResidentAddr4 = getValue("resident_addr4");
            mbosResidentAddr5 = getValue("resident_addr5");
            mbosSpecialCardRate = getValue("special_card_rate");
            
            if (mbosSpecialCardRate.equals("0")) {
                tmpSpecialCardRateFlag = "N";
            }
            else {
            	tmpSpecialCardRateFlag = "Y";
            }

            mbosCardRefNum = getValue("card_ref_num");
            mbosCrtBankNo = getValue("crt_bank_no");
            mbosVdBankNo = getValue("vd_bank_no");
            mbosSpouseName = getValue("spouse_name");
            mbosSpouseIdNo = getValue("spouse_id_no");
            mbosSpouseIdNo = getValue("spouse_id_no");
            mbosResidentNoExpireDate = getValue("resident_no_expire_date");
            mbosGraduationElementarty = getValue("graduation_elementarty");
            mbosUrFlag = getValue("ur_flag");
            mbosBusinessCode = getValue("business_code");
            mbosInstFlag = getValue("inst_flag");
            mbosCreditLevelNew = getValue("credit_level_new");
            mbosMailZip = getValue("mail_zip");
            mbosMailAddr1 = getValue("mail_addr1");
            mbosMailAddr2 = getValue("mail_addr2");
            mbosMailAddr3 = getValue("mail_addr3");
            mbosMailAddr4 = getValue("mail_addr4");
            mbosMailAddr5 = getValue("mail_addr5");
            mbosCompanyZip = getValue("company_zip");
            mbosCompanyAddr1 = getValue("company_addr1");
            mbosCompanyAddr2 = getValue("company_addr2");
            mbosCompanyAddr3 = getValue("company_addr3");
            mbosCompanyAddr4 = getValue("company_addr4");
            mbosCompanyAddr5 = getValue("company_addr5");
            mbosFeeCodeI = getValue("fee_code_i");
            mbosRevolveIntRate = getValueDouble("revolve_int_rate");
            mbosEMailAddr = getValue("e_mail_addr");
            mbosFlFlag = getValue("fl_flag");
            mbosAutopayAcctBank = getValue("autopay_acct_bank");
            if(mbosAutopayAcctBank.equals("0000000")) {
            	mbosAutopayAcctBank = "";
            }
            else if(mbosAutopayAcctBank.equals("0000001")) {
        		mbosAutopayAcctBank = "0060567";
        	}
            mbosBillApplyFlag = getValue("bill_apply_flag");
            mbosCurrChangeAccout = getValue("curr_change_accout");
            if(!mbosCurrChangeAccout.equals("")) {
            	mbosCurrChangeAccout = String.format("%016d",Long.parseLong(mbosCurrChangeAccout));
            }                        

            pgcCrdMakecardFeeFlag = getValue("crd_makecard_fee_flag");
            pgcCrdMakecardFee = getValue("crd_makecard_fee");

            paccCashadvLocRate = getValueInt("cashadv_loc_rate");
            paccCashadvLocMaxamt = getValueInt("cashadv_loc_maxamt");
            paccCashadvLocRateOld = getValueInt("cashadv_loc_rate_old");
            paccBreachNumMonth = getValueInt("breach_num_month");
            paccAtmCode = getValue("atm_code");
            hComboIndicator = getValue("combo_indicator");
            mbosStatSendInternet = getValue("stat_send_internet");
            if (mbosStatSendInternet.length() == 0)
                mbosStatSendInternet = "N";
            hFancyLimitFlag = getValue("fancy_limit_flag");

            if (hComboIndicator.compareTo("Y") == 0)
                pCashRate = getValueDouble("cash_limit_rate");

            mbosMarketAgreeBase = getValue("market_agree_base");
            mbosENews = getValue("e_news");
            mbosClerkId = getValue("clerk_id");
            mbosSmsAmt = getValueInt("sms_amt");
            
            if(mbosSmsAmt == -1) {
            	tmpMsgFlag = "N";
            	tmpMsgPurchaseAmt = 0;
            }
            else if(mbosSmsAmt== 0){
            	tmpMsgFlag = "Y";
            	tmpMsgPurchaseAmt = 0;
            }
            else {
            	tmpMsgFlag = "Y";
            	tmpMsgPurchaseAmt = mbosSmsAmt;
            }
            
            mbosRevolveIntRateYear = getValueDouble("revolve_int_rate_year");
            mbosEngName = getValue("eng_name");
            mbosSonCardFlag = getValue("son_card_flag");

            processDisplay(5000); // every nnnnn display message
            if(debug == 1) {
                showLogMessage("I", "", "\n888 Card=["+ mbosCardNo +"]"+"s="+ mbosSupFlag +","+ mbosCardType);
                showLogMessage("I", "", "        id=["+ mbosApplyId +"]["+ mbosStmtCycle +"]");
                showLogMessage("I", "", "       src=["+ mbosEmbossSource +"]"+ mbosEmbossReason);
            }

            totalCnt++;
            // ****************************************************
            // 線上proc_type='1'(緊急新製卡),online_mark='2',
            // 不需有 rtn_nccc_date(回饋日期),
            // 但一般情形,需有rtn_nccc_date(回饋日期)
            // ******************************************************/

            tmpInt = selPtrBintable();

            if (debug == 1)
                showLogMessage("I", "", "  step 01 =[" + procType + "]" + mbosOnlineMark);
            if (procType.equals("1")) {
                if (!(mbosOnlineMark.equals("2")))
                    continue;
            } else // * 需有回饋日期
            {
                if (mbosrtnncccdate.length() <= 0 && !(mbosVirtualFlag.equals("Y")))
                    continue;
                if (mbosOnlineMark.equals("2"))
                    continue; // * 不做緊急新製卡
            }
            if (debug == 1)
                showLogMessage("I", "", "  step 02 =[" + mbosCardNo + "]" + mbosCardType);
            /*
             * lai test
            mbos_voice_passwd = "1";
            mbos_pin_block = "1";
            */
            
            if (mbosPinBlock.length() == 0) {
                tmpInt = getPinData();
                if (tmpInt != 0) {
                    rtnCode = 1;
                    tmpCheckCode = "E01";
                    errorMsg = "請先產生PVV";
                    showLogMessage("D", "", " Error : " + errorMsg);
                    updateErrorEmboss(errorMsg);
                    continue;
                }
            }

            if(mbosSuperNote.equals("Y"))
            {
                rtnCode = 1;
                tmpCheckCode = "E02";
                errorMsg = "需主管放行";
                showLogMessage("D", "", " Error : " + errorMsg);
                updateErrorEmboss(errorMsg);
                continue;
            }
            showLogMessage("I", "", "  step 02 =[" + mbosCardNo + "]" + mbosCardType);

            swNew = "Y";
            tmpInt = chkCrdMoveList();

            /* V1.44.01 marked */
//            tmp_int = select_ptr_group_code();
//            tmp_int = select_ptr_src_code();
//
//            if (mbos_sup_flag.equals("1")) {
//                // ** 附卡同正卡登錄狀態 ***
//                tmp_int = select_major_card();
//            }

            if (debug == 1)
                showLogMessage("D", "", " 888 source[" + mbosEmbossSource + "]PM="+ mbosPmId);
            switch (mbosEmbossSource.trim()) {
                // 新製卡
                case "1":
                    rtnCode = checkCardNo();
                    if (rtnCode == 0) {
                    	if(mbosCorpNo.length() == 0) {
                    		rtnCode = chkGenSameGroupType();
                    	}
                    	else {
                    		rtnCode = chkBusSameGroupType();
                    	}
                    	
                    	if (rtnCode == 0) {
                            rtnCode = procNewCard();
                            if (debug == 1)
                                showLogMessage("D", "", " 888 794 rtn =[" + rtnCode + "] ");
                            if (rtnCode == 0) {
                                /* mike for 簡訊 CRD_D011-1 新製卡 */
                                procSmsRtn("CRD_D011-1");
                                if (pgcCrdMakecardFeeFlag.equals("Y") && !mbosStatSendInternet.equals("Y")) {
                                	insertBilSysexp(mbosCardNo);
                                }                                    
                            }
                    	}
                    }
//                else {
//                	update_act_acno();
//                	update_crd_idno();
//                }
                    break;
                // 普升金卡
                case "2":
                    break;
                // 續卡
                case "3":
                case "4":
                    rtnCode = procChgCard();
                    if (rtnCode == 0) {
                        /* mike for 簡訊 CRD_D011-5 續卡 */
                        procSmsRtn("CRD_D011-5");
                    }
                    break;
                // 重製
                case "5":
                    if (mbosCardNo.compareTo(mbosOldCardNo) != 0) {
                    	rtnCode = checkCardNo();
                    	if (rtnCode == 0) {
                        	if(mbosCorpNo.length() == 0) {
                        		rtnCode = chkGenSameGroupType();
                        	}
                        	else {
                        		rtnCode = chkBusSameGroupType();
                        	}

                    		if (rtnCode == 0) {
                    			rtnCode = checkCrdBalance(); // 可用餘額為負數不可補發
                    			if (rtnCode == 0) {
                    				rtnCode = checkAcctStatus(); // 催收戶或呆帳戶不可補發
                    				if (rtnCode == 0) {
                    					rtnCode = checkBlockReason(); // 特定凍結碼不可補發
                    					if (rtnCode == 0) {
                    						rtnCode = checkSpecStatus(); // 特定特指碼不可補發
                    					}
                    				}
                    			}
                    		}
                    	}
                    }                       
                    if (rtnCode == 0) {
                    	rtnCode = procReissueCard();
                    	
                        if (mbosEmbossReason.equals("2")) {                                
                        	/* mike for 簡訊 CRD_D011-2 毀損 */                                
                        	procSmsRtn("CRD_D011-2");                            
                        }                            
                        if (mbosEmbossReason.equals("1")) {                                
                        	/* mike for 簡訊 CRD_D011-3 掛失 */                               
                        	procSmsRtn("CRD_D011-3");                            
                        }                            
                        if (mbosEmbossReason.equals("3")) {                               
                        	/* mike for 簡訊 CRD_D011-4 偽卡 */                              
                        	procSmsRtn("CRD_D011-4");                            
                        }                   	
                    }                    	                                                           
                    break;
                // 緊急補發卡
                case "7":
                    rtnCode = checkCardNo();
                    if (rtnCode == 0) {
                        rtnCode = procUrgentCard();
                    }
                    break;
                // 星座卡毀損重製
                default:
                    break;
            }
            if (debug == 1)
                showLogMessage("D", "", " 888 852 rtn =[" + rtnCode + "] ");
            if (rtnCode == 0) {
                if (mbosEmbossSource.compareTo("2") >= 0) {
                    if ((mbosEmbossReason.length()        > 0) &&
                            (mbosEmbossReason.compareTo("2") != 0)) {
                        rtnCode = updateCrdRela();
                    }
                }
                if (delPinFlag == 1) {
                    rtnCode = delPinBlock();
                }
            }
            if (debug == 1) showLogMessage("D", "", " 888 867 rtn =[" + rtnCode + "] ");

            if (rtnCode == 0 && hComboIndicator.compareTo("N") != 0) {
                selectSQL = " cash_lmt_balance  as cash_lmt_balance ";
                daoTable = "act_combo_m_jrnl ";
                whereStr = "WHERE acct_type = '05' " + "  and p_seqno   =  ?   ";
                setString(1, pPSeqno);

                tmpInt = selectTable();
                tempCashUseBalance1 = 0;
                if (tmpInt > 0)
                    tempCashUseBalance1 = getValueDouble("cash_lmt_balance");

                selectSQL = " line_of_credit_amt as line_of_credit_amt ";
                daoTable  = "act_acno   ";
                whereStr  = "WHERE acct_type = '05' "
                        + "  and acno_p_seqno   =  ?   ";
                setString(1, pPSeqno);

                tmpInt = selectTable();
                tempCashUseBalanceO = 0;
                if (tmpInt > 0)
                    tempCashUseBalanceO = getValueDouble("line_of_credit_amt");

                if ((tempCashUseBalance1 != 0 || tempCashUseBalanceO != 1000)
                        && hThirdDataReissue.equals("Y")) {
                    rptCnt++;
                    if (rptCnt == 1)
                        writeHeader();
                    tmpChar = comcr.commHiIdno(pAcctKey);
                    writeData();
                }
            }

            if(tmpCheckCode.length() == 0) {
            	sucCnt++;
            }
            else {
            	failCnt++;
            }
            
            if (getValue("fh_flag").equals("Y")) {
                updateEmboss2();
                if (pPSeqno.length() > 0)
                    updateActAcno2();
            } else {
                updateEmboss();
            }

//            if (mbos_in_main_error.compareTo("0") != 0) {
//            updateSQL = "in_main_date  = to_char(sysdate,'yyyymmdd')  , " 
            updateSQL = "in_main_date  =  ? , "
                    + "in_main_error =  ? , " + "mod_pgm       =  ? , "
                    + "mod_time      = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_nccc_stat ";
            whereStr  = "where batchno   =  ? "
                    + "  and card_no   =  ? ";

            setString(1, mbosInMainDate);
            setString(2, mbosInMainError);
            setString(3, javaProgram);
            setString(4, sysDate + sysTime);
            setString(5, mbosBatchno);
            setString(6, mbosCardNo);

            updateTable();
            if (notFound.equals("Y")) {
            }
            //           }

            commitDataBase();
        }

    }

    //***********************************************************************
    void insertBilSysexp(String cardNo) throws Exception {

        setValue("card_no"      , mbosCardNo);
        setValue("acct_type"    , mbosAcctType);
        setValue("p_seqno"      , pPSeqno);
        setValue("bill_type"    , "OSSG");
        setValue("txn_code"     , "MF");
        setValue("purchase_date", sysDate);
        setValue("src_type"     , "OS");
        setValue("dest_amt"     , pgcCrdMakecardFee);
        setValue("dest_curr"    , "901");
        setValue("src_amt"      , pgcCrdMakecardFee);
        setValue("post_flag"    , "N");
        setValue("mod_user"     , javaProgram);
        setValue("mod_time"     , sysDate + sysTime);
        setValue("mod_pgm"      , javaProgram);
        daoTable = "bil_sysexp";
        int recordCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_sysexp duplicate!", "", comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    public int procSmsRtn(String pgm) throws Exception {

        selectCrdIdno();
        selectCrdCard();
        hSmdlMsgPgm = pgm;
        if (selectSmsMsgId() == 0) {
            if (checkAcctType(pgm) == 0) {
                selectPtrGroupCode();
                switch (pgm.trim()) {
                    case "CRD_D011-1":
                    case "CRD_D011-2":
                    case "CRD_D011-3":
                    case "CRD_D011-4":
                        szTmp = hSmidMsgUserid + "," + hSmidMsgId + "," + hIdnoCellarPhone
                                + "," + hIdnoChiName + "," + pgcdGroupName;
                        break;
                    case "CRD_D011-5":
                        szTmp = hSmidMsgUserid +","+ hSmidMsgId +","+ hIdnoCellarPhone +","
                                + hIdnoChiName +","+ pgcdGroupName +","+ hTempOldEndMonth;
                        break;
                }
                insertSmsMsgDtl();
            }
        }

        return (0);
    }
    // ************************************************************************
    public int writeHeader() throws Exception {
        pageCnt++;
        String buf = "";
        buf = comcr.insertStr(buf, "CRD_D011R1", 1);
        buf = comcr.insertStr(buf, "已指撥餘額異常明細表", 30);
        buf = comcr.insertStr(buf, "頁  次 :", 60);
        buf = comcr.insertStr(buf, String.format("%7d", pageCnt), 70);
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "身分證號碼 金融帳號     已指撥餘額   舊卡額度  來源代號", 1);
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "========================================================", 1);
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "0", buf));

        return (0);
    }
    // ************************************************************************
    public int writeHeader2() throws Exception {
        pageCnt++;
        String buf = "";
        buf = comcr.insertStr(buf, "CRD_D011R2", 1);
        buf = comcr.insertStr(buf, "combo卡屆期轉01名單", 30);
        buf = comcr.insertStr(buf, "頁  次 :", 60);
        buf = comcr.insertStr(buf, String.format("%7d", pageCnt), 70);
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "姓      名 身分證號碼 原卡號           新卡號  ", 1);
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "========================================================", 1);
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "0", buf));

        return (0);
    }
    // ************************************************************************
    public int writeTailer() throws Exception {

        String buf = "";
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "2", buf));

        return (0);
    }
    // ************************************************************************
    public int writeData() throws Exception {
        writeCnt1++;
        String buf = "";
        buf = comcr.insertStr(buf, String.format("%10s", tmpChar), 1);
        buf = comcr.insertStr(buf, String.format("%11s", mbosActNo), 12);
        buf = comcr.insertStr(buf, String.format("%10.0f", tempCashUseBalance1), 24);
        buf = comcr.insertStr(buf, String.format("%10.0f", tempCashUseBalanceO), 35);
        buf = comcr.insertStr(buf, String.format("%6s", mbosSourceCode), 46);
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "1", buf));

        return (0);
    }

    // ************************************************************************
    public int writeData2() throws Exception {
        writeCnt1++;
        String buf = "";
        buf = comcr.insertStr(buf, String.format("%10s", tmpChar1), 1);
        buf = comcr.insertStr(buf, String.format("%10s", tmpChar2), 12);
        buf = comcr.insertStr(buf, String.format("%16s", moveCardNo), 23);
        buf = comcr.insertStr(buf, String.format("%16s", hCardCardNo), 40);
        buf = comcr.insertStr(buf, endflag, 79);
        lpar1.add(comcr.putReport(javaProgram, ncccFilename, sysDate, rptSeq++, "1", buf));

        return (0);
    }
    // ************************************************************************
    public int selPtrBintable() throws Exception
    {
        selectSQL = " b.bin_type   ";
        daoTable  = "ptr_bintable b ";
        whereStr  = "WHERE b.bin_no    = ? "
                + "FETCH FIRST 1 ROW ONLY";
        setString(1, mbosBinNo);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_bintable  error[not find]" + mbosBinNo;
            String err2 = mbosCardType;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        selectSQL = " a.card_note    ";
        daoTable  = " ptr_card_type a";
        whereStr  = "WHERE a.card_type  = ? ";
        setString(1, mbosCardType);

        recCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_card_type  error[not find]" + mbosCardType;
            String err2 = mbosCardType;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hBinType = getValue("bin_type");
        hBinNo = mbosBinNo;
        hCardNote = getValue("card_note");

        if (debug == 1) showLogMessage("D", "", " 888 select type =[" + hBinType + "] ");

        return (0);
    }
    // ************************************************************************
    public int chkGenSameGroupType() throws Exception {
    	
        int count = 0;
        
        sqlCmd = "select count(*) cnt";
        sqlCmd += " from crd_card a, crd_idno b ";
        sqlCmd += "where a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += "  and a.current_code = '0' ";
        sqlCmd += "  and b.id_no = ? ";
        sqlCmd += "  and a.group_code = ? ";
        sqlCmd += "  and a.card_type = ?  ";
        sqlCmd += "  and a.unit_code = ? ";
        setString(1, mbosApplyId);
        setString(2, mbosGroupCode);
        setString(3, mbosCardType);
        setString(4, mbosUnitCode);
        int recordCnt = selectTable();
        if(debug == 1) showLogMessage("I","", " 666 reject 0 cnt=["+recordCnt+"]"+ mbosCardType +","+ mbosGroupCode +","+ mbosApplyId);
        if (notFound.equals("Y")) {}
        else{
            count = getValueInt("cnt");
            if(debug == 1) showLogMessage("I","", " 666 reject 01 cnt=["+count+"]");
            if (count == 0) {
                sqlCmd = "select count(*) cnt";
                sqlCmd += " from crd_emboss  ";
                sqlCmd += "where apply_id = ? ";
                sqlCmd += "  and group_code = ? ";
                sqlCmd += "  and card_type = ? ";
                sqlCmd += "  and unit_code = ? ";
                sqlCmd += "  and card_no != ? ";
                sqlCmd += "  and in_main_date  = '' ";
                sqlCmd += "  and in_main_error = '' ";
                sqlCmd += "  and reject_code   = '' ";
                setString(1, mbosApplyId);
                setString(2, mbosGroupCode);
                setString(3, mbosCardType);
                setString(4, mbosUnitCode);
                setString(5, mbosCardNo);
                recordCnt = selectTable();
                if (recordCnt > 0) {
                    count = getValueInt("cnt");
                }
            }
        }

        if (recordCnt > 0) {
        	if (count != 0) {
        		tmpCheckCode = "E03";
                errorMsg = "ID向下有同團代卡種之活卡或製卡中的資料";
                showLogMessage("I", "", "  Warning: " + errorMsg + " =" + mbosCardNo);
                return (1);
        	}        	   
        }
        
        return(0);
    }
    // ************************************************************************
    public int chkBusSameGroupType() throws Exception {
    	
        int count = 0;
        
        sqlCmd = "select count(*) cnt";
        sqlCmd += " from crd_card a, crd_corp b, crd_idno c ";
        sqlCmd += "where a.corp_p_seqno = b.corp_p_seqno ";
        sqlCmd += "  and a.id_p_seqno = c.id_p_seqno ";
        sqlCmd += "  and a.current_code = '0' ";
        sqlCmd += "  and b.corp_no = ? ";
        sqlCmd += "  and c.id_no = ? ";
        sqlCmd += "  and a.group_code = ? ";
        sqlCmd += "  and a.card_type = ?  ";
        sqlCmd += "  and a.unit_code = ? ";
        setString(1, mbosCorpNo);
        setString(2, mbosApplyId);
        setString(3, mbosGroupCode);
        setString(4, mbosCardType);
        setString(5, mbosUnitCode);
        int recordCnt = selectTable();
        if(debug == 1) showLogMessage("I","", " 666 reject 0 cnt=["+recordCnt+"]"+ mbosCardType +","+ mbosGroupCode +","+ mbosCorpNo +","+ mbosApplyId);
        if (notFound.equals("Y")) {}
        else{
            count = getValueInt("cnt");
            if(debug == 1) showLogMessage("I","", " 666 reject 01 cnt=["+count+"]");
            if (count == 0) {
                sqlCmd = "select count(*) cnt";
                sqlCmd += " from crd_emboss  ";
                sqlCmd += "where corp_no  = ? ";
                sqlCmd += "  and apply_id = ? ";
                sqlCmd += "  and group_code = ? ";
                sqlCmd += "  and card_type = ? ";
                sqlCmd += "  and unit_code = ? ";
                sqlCmd += "  and card_no != ? ";
                sqlCmd += "  and in_main_date  = '' ";
                sqlCmd += "  and in_main_error = '' ";
                sqlCmd += "  and reject_code   = '' ";
                setString(1, mbosCorpNo);
                setString(2, mbosApplyId);
                setString(3, mbosGroupCode);
                setString(4, mbosCardType);
                setString(5, mbosUnitCode);
                setString(6, mbosCardNo);
                recordCnt = selectTable();
                if (recordCnt > 0) {
                    count = getValueInt("cnt");
                }
            }
        }

        if (recordCnt > 0) {
        	if (count != 0) {
        		tmpCheckCode = "E03";
                errorMsg = "統編向下有同團代卡種之活卡或製卡中的資料";
                showLogMessage("I", "", "  Warning: " + errorMsg + " =" + mbosCardNo);
                return (1);
        	}        	   
        }
        
        return(0);
    }
    // ************************************************************************
    public int checkCrdBalance() throws Exception {
    	
    	long tmpAmtBalance = 0;
    	
		if(mbosSonCardFlag.equals("Y")) {
			extendField = "CARDBALANCE.";
			sqlCmd = "SELECT CARD_AMT_BALANCE FROM CCA_CARD_BALANCE_CAL ";
			sqlCmd += " WHERE CARD_NO = ? ";
			setString(1,mbosOldCardNo);
			selectTable();
			tmpAmtBalance = getValueLong("CARDBALANCE.CARD_AMT_BALANCE");
		}else {
			extendField = "ACCTBALANCE.";
			sqlCmd = " SELECT ACCT_AMT_BALANCE FROM CCA_ACCT_BALANCE_CAL ";
			sqlCmd += " WHERE ACNO_P_SEQNO = (SELECT ACNO_P_SEQNO FROM CRD_CARD WHERE CARD_NO = ? ) ";
			setString(1,mbosOldCardNo);
			selectTable();
			tmpAmtBalance = getValueLong("ACCTBALANCE.ACCT_AMT_BALANCE");
		}
		
		if(tmpAmtBalance < 0) {
			tmpCheckCode = "E16";
            errorMsg = "可用餘額為負數";
            showLogMessage("I", "", "  Warning: " + errorMsg + " =" + mbosCardNo);
			return (1);
		}
		
		return (0);		 
    }
    
    // ************************************************************************
    public int checkAcctStatus() throws Exception {
    	String tmpAcctStatus = "1";
    	
		extendField = "ACCTSTATUS.";
		sqlCmd = "SELECT ACCT_STATUS FROM ACT_ACNO ";
		sqlCmd += " WHERE ACNO_P_SEQNO = (SELECT ACNO_P_SEQNO FROM CRD_CARD WHERE CARD_NO = ? ) ";
		setString(1,mbosOldCardNo);
		selectTable();
		tmpAcctStatus = getValue("ACCTSTATUS.ACCT_STATUS");
		
		if(tmpAcctStatus.equals("3")||tmpAcctStatus.equals("4")) {
			tmpCheckCode = "E17";
            errorMsg = "帳戶狀態為催收戶或呆帳戶";
            showLogMessage("I", "", "  Warning: " + errorMsg + " =" + mbosCardNo);
			return (1);
		}

		return (0);	
    }
    
    // ************************************************************************
    public int checkBlockReason() throws Exception {
    	String tmpBlockReason1 = "";
    	String tmpBlockReason2 = "";
    	String tmpBlockReason3 = "";
    	String tmpBlockReason4 = "";
    	String tmpBlockReason5 = "";
    	
		extendField = "BLOCKREASON.";
		sqlCmd = "SELECT BLOCK_REASON1,BLOCK_REASON2,BLOCK_REASON3,BLOCK_REASON4,BLOCK_REASON5 FROM CCA_CARD_ACCT ";
		sqlCmd += " WHERE ACNO_P_SEQNO = (SELECT ACNO_P_SEQNO FROM CRD_CARD WHERE CARD_NO = ? ) ";
		setString(1,mbosOldCardNo);
		selectTable();
		tmpBlockReason1 = getValue("BLOCKREASON.BLOCK_REASON1");
		tmpBlockReason2 = getValue("BLOCKREASON.BLOCK_REASON2");
		tmpBlockReason3 = getValue("BLOCKREASON.BLOCK_REASON3");
		tmpBlockReason4 = getValue("BLOCKREASON.BLOCK_REASON4");
		tmpBlockReason5 = getValue("BLOCKREASON.BLOCK_REASON5");
		
		if((commStr.pos(",01,T2,0A,0E,06,0C,0F,14,15,0X,0N,34,35,36,37,38", tmpBlockReason1) > 0) ||
			(commStr.pos(",01,T2,0A,0E,06,0C,0F,14,15,0X,0N,34,35,36,37,38", tmpBlockReason2) > 0) ||
			(commStr.pos(",01,T2,0A,0E,06,0C,0F,14,15,0X,0N,34,35,36,37,38", tmpBlockReason3) > 0) ||
			(commStr.pos(",01,T2,0A,0E,06,0C,0F,14,15,0X,0N,34,35,36,37,38", tmpBlockReason4) > 0) ||
			(commStr.pos(",01,T2,0A,0E,06,0C,0F,14,15,0X,0N,34,35,36,37,38", tmpBlockReason5) > 0)) {
			tmpCheckCode = "E18";
            errorMsg = "為特定凍結碼";
            showLogMessage("I", "", "  Warning: " + errorMsg + " =" + mbosCardNo);
			return (1);
		}

		return (0);	
    }
    
    // ************************************************************************
    public int checkSpecStatus() throws Exception {
    	String tmpSpecStatus = "";
    	
		extendField = "SPECSTATUS.";
		sqlCmd = "SELECT SPEC_STATUS FROM CCA_CARD_BASE ";
		sqlCmd += " WHERE CARD_NO = ? ";
		setString(1,mbosOldCardNo);
		selectTable();
		tmpSpecStatus = getValue("SPECSTATUS.SPEC_STATUS");
		
		if(commStr.pos(",T6,09,26,04,32", tmpSpecStatus) > 0) {
			tmpCheckCode = "E19";
            errorMsg = "為特定特指碼";
            showLogMessage("I", "", "  Warning: " + errorMsg + " =" + mbosCardNo);
			return (1);
		}

		return (0);	
    }
    
    // ************************************************************************
    public int getPinData() throws Exception {
        if (!mbosOnlineMark.equals("1") && !mbosOnlineMark.equals("2")) {
        	tmpCheckCode = "E04";
            errorMsg = "一般卡無PIN_BLOCK值";
            return (1);
        }

        hPinBlock = "";
        hCvv2 = "";
        hPvki = "";

        selectSQL = "pvki,trans_cvv2,pin_block ";
        daoTable = "crd_cardno_data     ";
        whereStr = "WHERE card_no   = ? ";

        setString(1, mbosCardNo);

        selectTable();

        if (notFound.equals("Y"))
            return (1);
        else {
            hPinBlock = getValue("pin_block");
            hCvv2 = getValue("cvv2");
            hPvki = getValue("pvki");
            if (hPinBlock.length() == 0)
                return (1);
        }

        delPinFlag = 1;

        if (debug == 1)
            showLogMessage("D", "", " 888 select pin  =[" + delPinFlag + "] ");
        return (0);
    }

    // ************************************************************************
    public int chkCrdMoveList() throws Exception {
        if (debug == 1)
            showLogMessage("D", "", " 888  move list  =[" + mbosCardType + "] ");
        swMove = "N";
        extendField = "move.";
        selectSQL   = "  chi_name  , card_no "
                + ", rowid    as rowid   ";
        daoTable    = " crd_move_list        ";
        whereStr    = "WHERE new_card_no   = ? ";

        setString(1, mbosCardNo);

        int recCnt = selectTable();

        moveChiName = getValue("move.chi_name");
        moveCardNo = getValue("move.card_no");
        moveRowid = getValue("move.rowid");

        if (!notFound.equals("Y")) {
            swMove = "Y";
            swNew = "N";
        }
        if (debug == 1)
            showLogMessage("D", "", " 888 sw_move=[" + swMove + "]");

        return (0);
    }

    // ************************************************************************
    public int getAcctType() throws Exception {
        if (debug == 1)
            showLogMessage("D", "", " group_code =[" + mbosGroupCode + "]" + mbosCardType);
        if ((mbosGroupCode.length() > 0) && (mbosGroupCode.compareTo("0000") != 0)) {
            selectSQL = "a.rc_use_flag, a.card_indicator ,   " + "a.stmt_cycle        ";
            daoTable  = "ptr_acct_type a,ptr_prod_type b ";
            whereStr  = "WHERE b.card_type  = '' "
                    + "  and a.acct_type  = b.acct_type "
                    + "  and b.group_code = ? ";

            setString(1, mbosGroupCode);

            int recCnt = selectTable();

            hStmtCycle = getValue("stmt_cycle");
            hRcUseFlag = getValue("rc_use_flag");
            hCardIndicator = getValue("card_indicator");

            if (!notFound.equals("Y")) // found
            {
                return (0);
            }
        }

        selectSQL = "a.rc_use_flag,  a.card_indicator,  " + "a.stmt_cycle        ";
        daoTable = "ptr_acct_type a,ptr_prod_type b ";
        whereStr = "WHERE b.card_type   = ?     " + "  and a.acct_type   = b.acct_type ";

        setString(1, mbosCardType);

        selectTable();

        if (notFound.equals("Y")) // found
        {
        	tmpCheckCode = "E05";
        	errorMsg = "找不到帳戶資料";
            return (1);
        }
        hStmtCycle = getValue("stmt_cycle");
        hRcUseFlag = getValue("rc_use_flag");
        hCardIndicator = getValue("card_indicator");

        if (debug == 1)
            showLogMessage("D", "", " 888 select acct =[" + hCardIndicator + "] ");
        return (0);
    }

    // ************************************************************************
    public int selectPtrGroupCode() throws Exception {

        selectSQL = "auto_installment, group_name   ";
        daoTable  = "ptr_group_code      ";
        whereStr  = "WHERE group_code  = ? ";

        setString(1, mbosGroupCode);

        selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_group_code  error[notFind]";
            String err2 = mbosGroupCode;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
        cardAutoInstallment = getValue("auto_installment");
        pgcdGroupName = getValue("group_name");

        return (0);
    }

    // ************************************************************************
//    public int selectPtrSrcCode() throws Exception {
//        selectSQL = "decode(third_data_reissue,'','N',third_data_reissue) as third_data_reissue ,"
//                + "decode(decode(not_auto_installment,'','N',not_auto_installment),'Y','N',?) "
//                + " as not_auto_installment ";
//        daoTable  = "ptr_src_code        ";
//        whereStr  = "WHERE source_code    = ? ";
//
//        setString(1, cardAutoInstallment);
//        setString(2, mbosSourceCode);
//
//        selectTable();
//        if (notFound.equals("Y")) {
//            String err1 = "select_ptr_src_code    error[" + mbosSourceCode +"]";
//            String err2 = mbosSourceCode;
//            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
//        }
//        cardAutoInstallment = getValue("not_auto_installment");
//        hThirdDataReissue = getValue("third_data_reissue");
//
//        return (0);
//    }

    // ************************************************************************
    public int selectMajorCard() throws Exception {
        selectSQL = "auto_installment as major_auto_installment  ";
        daoTable = "crd_card            ";
        whereStr = "WHERE card_no     = ? ";

        setString(1, mbosMajorCardNo);

        int recCnt = selectTable();

        cardAutoInstallment = getValue("major_auto_installment");

        return (0);
    }

    // ************************************************************************
//    public int updateReissueAcno() throws Exception {
//        extendField = "reiss.";
//        selectSQL = "line_of_credit_amt ";
//        daoTable  = "act_acno            ";
//        whereStr  = "WHERE acno_p_seqno     = ? ";
//
//        setString(1, hCardPSeqno);
//
//        int recCnt = selectTable();
//
//        if (notFound.equals("Y")) {
//            errorMsg = "掛失重製無卡戶";
//            return (1);
//        }
//
//        pCreditAmt = getValueInt("reiss.line_of_credit_amt");
//
//        double subPercent = (hPpltPercentC * 1.0) / 100;
//        int pMonthPurchaseLmt = (int) (pCreditAmt * subPercent);
//
//        if (pMonthPurchaseLmt < hPpltLfixC)
//            pMonthPurchaseLmt = hPpltLfixC;
//
//        updateSQL = "month_purchase_lmt = ? , " + "mod_pgm            = ? , "
//                + "mod_time           = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
//        daoTable  = "act_acno      ";
//        whereStr  = "WHERE acno_p_seqno     = ? ";
//
//        setInt(1, pMonthPurchaseLmt);
//        setString(2, javaProgram);
//        setString(3, sysDate + sysTime);
//        setString(4, hCardPSeqno);
//
//        return (0);
//    }

    // ************************************************************************
    public int updateMoveElse() throws Exception {
        updateSQL = "old_card_no     = ? ";
        daoTable = "crd_card ";
        whereStr = "where card_no   = ? ";

        setString(1, moveCardNo);
        setString(2, hCardCardNo);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_card new        error[notFind]";
            String err2 = moveCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        updateSQL = "new_card_no     = ?  , " + "change_date     = '' , "
                + "change_reason   = '' , " + "change_status   = ''   ";
        daoTable  = "crd_card ";
        whereStr  = "where card_no   = ? ";

        setString(1, hCardCardNo);
        setString(2, moveCardNo);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_card new1       error[notFind]";
            String err2 = moveCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        updateSQL = "move_status_old = move_status ";
        daoTable  = "crd_card_ext ";
        whereStr  = "where card_no   = ? ";

        setString(1, moveCardNo);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_card_ext new        error[notFind]";
            String err2 = moveCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        updateSQL = "move_status     = 'N' ";
        daoTable = "crd_card_ext ";
        whereStr = "where card_no   = ? ";

        setString(1, moveCardNo);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_card_ext new 1      error[notFind]";
            String err2 = moveCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        updateSQL = "in_main_flag = 'Y'  ";
        daoTable  = "crd_move_list ";
        whereStr  = "where rwoid  = ? ";

        setRowId(1, moveRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_move_list       error[notFind]";
            String err2 = moveRowid;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int updateActAcno() throws Exception {
    	tmpBillSendingZip   = ""; 
    	tmpBillSendingAddr1 = ""; 
    	tmpBillSendingAddr2 = ""; 
    	tmpBillSendingAddr3 = ""; 
    	tmpBillSendingAddr4 = ""; 
    	tmpBillSendingAddr5 = ""; 
    	
        if (debug == 1) showLogMessage("I", ""," update acno=["+ pAcnoStopStatus +"]"+ pAcnoRowid);

        updateSQL = "new_acct_flag   =  '" + swNew + "' ";
        if (pAcnoStopStatus.equals("Y"))
            updateSQL = updateSQL + ", stop_status   = ''  ";
        if (pAcnoCreditAmt != pCreditAmt) {
            if (validflag == 1) // 有效卡
            {
                if ((pCreditAmt > pAcnoCreditAmt) && swMove.equals("N")) {
                    updateSQL = updateSQL + ", line_of_credit_amt      = " + pCreditAmt + " ";
                    updateSQL = updateSQL + ", line_of_credit_amt_cash = " +
                            hTempLineOfCreditAmtCash + " ";
                }
            } else {
                updateSQL = updateSQL + ", line_of_credit_amt      = " + pCreditAmt + " ";
                updateSQL = updateSQL + ", line_of_credit_amt_cash = " +
                        hTempLineOfCreditAmtCash + " ";
            }
        }
        if (hComboIndicator.compareTo("N") != 0) {
            if (pComboAcctNo.compareTo(pAcnoComboAcctNo) != 0)
                updateSQL = updateSQL + ", combo_acct_no = '" + pComboAcctNo + "' ";

            if(((pAcnoCreditAmt > pCreditAmt) && (pAcnoComboCashLimit > pCreditAmt))||
                    (hFancyLimitFlag.equals("Y") && pAcnoComboCashLimit > 0)) {
                if (hFancyLimitFlag.equals("Y"))
                    updateSQL = updateSQL + ", combo_cash_limit  = " + tempComboLimit + " ";
                else
                    updateSQL = updateSQL + ", combo_cash_limit  = " + pCreditAmt + " ";
            }
        }
//        if (validflag == 0) {
//            double subPercent = (hPpltPercentC * 1.0) / 100;
//            int pMonthPurchaseLmt = (int) (pCreditAmt * subPercent);
//
//            if (pMonthPurchaseLmt < hPpltLfixC)
//                pMonthPurchaseLmt = hPpltLfixC;
//
//            updateSQL = updateSQL + ", month_purchase_lmt      = " + pMonthPurchaseLmt + " ";
//        }
//        int flag = 0;
//        if (p_mail_addr1.length() > 0)   flag = 1;
//        if (p_mail_addr2.length() > 0)   flag = 1;
//        if (p_mail_addr3.length() > 0)   flag = 1;
//        if (p_mail_addr4.length() > 0)   flag = 1;
//        if (p_mail_addr5.length() > 0)   flag = 1;
//        if (flag == 1) {
//            updateSQL = updateSQL + ", bill_sending_zip   = '" + p_mail_zip + "' ";
//            updateSQL = updateSQL + ", bill_sending_addr1 = '" + p_mail_addr1 + "' ";
//            updateSQL = updateSQL + ", bill_sending_addr2 = '" + p_mail_addr2 + "' ";
//            updateSQL = updateSQL + ", bill_sending_addr3 = '" + p_mail_addr3 + "' ";
//            updateSQL = updateSQL + ", bill_sending_addr4 = '" + p_mail_addr4 + "' ";
//            updateSQL = updateSQL + ", bill_sending_addr5 = '" + p_mail_addr5 + "' ";
//        }               

        if (pClassCode.compareTo(pAcnoClassCode) != 0)
            updateSQL = updateSQL + ", class_code  = '" + pClassCode + "' ";

        if (pVipCode.compareTo(pAcnoVipCode) != 0)
            updateSQL = updateSQL + ", vip_code  = '" + pVipCode + "' ";

        if (hCardIndicator.equals("1") && pRiskBankNo.length() > 0) {
            tmpChar = pRiskBankNo;
            tmpChar1 = pOrgRiskBankNo;
//            if (tmpChar.compareTo(tmpChar1) != 0)
//                updateSQL = updateSQL + ", risk_bank_no  = '" + pRiskBankNo + "' ";
        }

        selectSQL = "e_mail_ebill, revolve_int_rate, bill_apply_flag, autopay_acct_bank, "
                + "autopay_acct_no, autopay_indicator, autopay_id, autopay_id_code, autopay_acct_s_date, "
                + "bill_sending_zip, bill_sending_addr1, bill_sending_addr2, bill_sending_addr3, "
                + "bill_sending_addr4, bill_sending_addr5,e_mail_ebill_date,rcrate_year,"
                + "revolve_rate_s_month,chg_addr_date";
        daoTable  = "act_acno ";
        whereStr  = "where acct_key    = ?";
        setString( 1, pAcctKey);
//        whereStr  = "where rowid    = ?";
//        setRowId( 1, p_acno_rowid);

        if (selectTable() > 0) {
            actEMailEbill = getValue("e_mail_ebill");
            actRevolveIntRate = getValueDouble("revolve_int_rate");
            actBillApplyFlag = getValue("bill_apply_flag");
            actAutopayAcctBank = getValue("autopay_acct_bank");
            actAutopayAcctNo = getValue("autopay_acct_no");
            actAutopayIndicator = getValue("autopay_indicator");
            actAutopayId = getValue("autopay_id");
            actAutopayIdCode = getValue("autopay_id_code");
            actAutopayAcctSDate = getValue("autopay_acct_s_date");
            actBillSendingZip = getValue("bill_sending_zip");
            actBillSendingAddr1 = getValue("bill_sending_addr1");
            actBillSendingAddr2 = getValue("bill_sending_addr2");
            actBillSendingAddr3 = getValue("bill_sending_addr3");
            actBillSendingAddr4 = getValue("bill_sending_addr4");
            actBillSendingAddr5 = getValue("bill_sending_addr5");
            actEMailEbillDate   = getValue("e_mail_ebill_date");
            actRevolveIntRateYear = getValueDouble("rcrate_year");
            actRevolveRateSMonth = getValue("revolve_rate_s_month");
            actChgAddrDate = getValue("chg_addr_date");
        }

        updateSQL = updateSQL
                + ", mod_pgm      = ? "
                + ", mod_time     = sysdate "
                + ", e_mail_ebill        = ?, revolve_int_rate 		= ?, bill_apply_flag     	= ? "
                + ", autopay_acct_bank   = ?, autopay_acct_no  		= ?, autopay_indicator   	= ? "
                + ", autopay_id          = ?, autopay_id_code  		= ?, autopay_acct_s_date 	= ? "
                + ", bill_sending_zip    = ?, bill_sending_addr1 	= ?, bill_sending_addr2 	= ? "
                + ", bill_sending_addr3  = ?, bill_sending_addr4 	= ?, bill_sending_addr5 	= ? "
                + ", e_mail_ebill_date   = ?, rcrate_year	        = ?, revolve_rate_s_month   = ? "
                + ", chg_addr_date       = ?";
        daoTable  = "act_acno      ";
        whereStr  = "where acct_key    = ? ";
        setString(1, javaProgram);
        if(debug == 1) showLogMessage("I",""," update acno=[" + updateSQL + "]");

        if (mbosStatSendInternet.equals("Y") && !mbosEMailAddr.equals(""))
            setString(2, mbosEMailAddr);
        else
            setString(2, actEMailEbill);

        if (mbosRevolveIntRate == 0)
        	setDouble(3, actRevolveIntRate);
        else
            setDouble(3, mbosRevolveIntRate);        

        if (mbosBillApplyFlag.equals(""))
            setString(4, actBillApplyFlag);
        else
            setString(4, mbosBillApplyFlag);

        if (mbosAutopayAcctBank.equals("")) {
        	setString(5, actAutopayAcctBank);
        }            
        else {        
        	setString(5, mbosAutopayAcctBank);        	        	        	
        }            

        if (mbosActNoL.equals(""))
            setString(6, actAutopayAcctNo);
        else
            setString(6, mbosActNoL);

        if (mbosActNoLInd.equals(""))
            setString(7, actAutopayIndicator);
        else
            setString(7, mbosActNoLInd);

        if (mbosActNoL.equals(""))
            setString(8, actAutopayId);
        else
            setString(8, pAcctHolderId);

        if (mbosActNoL.equals(""))
            setString(9, actAutopayIdCode);
        else
            setString(9, pAcctHolderIdCode);

        if (!mbosActNoL.equals("") && !mbosActNoL.equals("act_autopay_acct_no"))
            setString(10, sysDate);
        else
            setString(10, actAutopayAcctSDate);

        if (actBillApplyFlag.equals(""))
            comcr.errRtn("", "Table:ACT_ACNO -> bill_apply_flag不可為空白 ! ", comcr.hCallBatchSeqno);        

        if (actBillApplyFlag.equals(mbosBillApplyFlag)) {
            switch (actBillApplyFlag) {
                case "1":
                    if (!mbosResidentZip.equals(""))
                    	tmpBillSendingZip = mbosResidentZip;
                     else
                    	tmpBillSendingZip = actBillSendingZip;

                    if (!mbosResidentAddr1.equals(""))
                    	tmpBillSendingAddr1 = mbosResidentAddr1;
                    else
                    	tmpBillSendingAddr1 = actBillSendingAddr1;

                    if (!mbosResidentAddr2.equals(""))
                    	tmpBillSendingAddr2 = mbosResidentAddr2;
                    else
                    	tmpBillSendingAddr2 = actBillSendingAddr2;

                    if (!mbosResidentAddr3.equals(""))
                    	tmpBillSendingAddr3 = mbosResidentAddr3;
                    else
                    	tmpBillSendingAddr3 = actBillSendingAddr3;

                    if (!mbosResidentAddr4.equals(""))
                    	tmpBillSendingAddr4 = mbosResidentAddr4;
                    else
                    	tmpBillSendingAddr4 = actBillSendingAddr4;

                    if (!mbosResidentAddr5.equals(""))
                    	tmpBillSendingAddr5 = mbosResidentAddr5;
                    else
                    	tmpBillSendingAddr5 = actBillSendingAddr5;
                    break;
                case "2":
                    if (!mbosMailZip.equals(""))
                    	tmpBillSendingZip = mbosMailZip;
                    else
                    	tmpBillSendingZip = actBillSendingZip;

                    if (!mbosMailAddr1.equals(""))
                    	tmpBillSendingAddr1 = mbosMailAddr1;
                    else
                    	tmpBillSendingAddr1 = actBillSendingAddr1;

                    if (!mbosMailAddr2.equals(""))
                    	tmpBillSendingAddr2 = mbosMailAddr2;
                    else
                    	tmpBillSendingAddr2 = actBillSendingAddr2;

                    if (!mbosMailAddr3.equals(""))
                    	tmpBillSendingAddr3 = mbosMailAddr3;
                    else
                    	tmpBillSendingAddr3 = actBillSendingAddr3;

                    if (!mbosMailAddr4.equals(""))
                    	tmpBillSendingAddr4 = mbosMailAddr4;
                    else
                    	tmpBillSendingAddr4 = actBillSendingAddr4;

                    if (!mbosMailAddr5.equals(""))
                    	tmpBillSendingAddr5 = mbosMailAddr5;
                    else
                    	tmpBillSendingAddr5 = actBillSendingAddr5;
                    break;
                case "3":
                    if (!mbosCompanyZip.equals(""))
                    	tmpBillSendingZip = mbosCompanyZip;
                    else
                    	tmpBillSendingZip = actBillSendingZip;

                    if (!mbosCompanyAddr1.equals(""))
                    	tmpBillSendingAddr1 = mbosCompanyAddr1;
                    else
                    	tmpBillSendingAddr1 = actBillSendingAddr1;

                    if (!mbosCompanyAddr2.equals(""))
                    	tmpBillSendingAddr2 = mbosCompanyAddr2;
                    else
                    	tmpBillSendingAddr2 = actBillSendingAddr2;

                    if (!mbosCompanyAddr3.equals(""))
                    	tmpBillSendingAddr3 = mbosCompanyAddr3;
                    else
                    	tmpBillSendingAddr3 = actBillSendingAddr3;

                    if (!mbosCompanyAddr4.equals(""))
                    	tmpBillSendingAddr4 = mbosCompanyAddr4;
                    else
                    	tmpBillSendingAddr4 = actBillSendingAddr4;

                    if (!mbosCompanyAddr5.equals(""))
                    	tmpBillSendingAddr5 = mbosCompanyAddr5;
                    else
                    	tmpBillSendingAddr5 = actBillSendingAddr5;
                    break;
                default:
                	tmpBillSendingZip = actBillSendingZip;
                	tmpBillSendingAddr1 = actBillSendingAddr1;
                	tmpBillSendingAddr2 = actBillSendingAddr2;
                	tmpBillSendingAddr3 = actBillSendingAddr3;
                	tmpBillSendingAddr4 = actBillSendingAddr4;
                	tmpBillSendingAddr5 = actBillSendingAddr5;
                    break;
            }
        }
        else {
            switch (actBillApplyFlag) {
                case "1":
                    if (!mbosResidentZip.equals(""))
                    	tmpBillSendingZip = mbosResidentZip;
                    else
                    	tmpBillSendingZip = idnoResidentZip;

                    if (!mbosResidentAddr1.equals(""))
                    	tmpBillSendingAddr1 = mbosResidentAddr1;
                    else
                    	tmpBillSendingAddr1 = idnoResidentAddr1;

                    if (!mbosResidentAddr2.equals(""))
                    	tmpBillSendingAddr2 = mbosResidentAddr2;
                    else
                    	tmpBillSendingAddr2 = idnoResidentAddr2;

                    if (!mbosResidentAddr3.equals(""))
                    	tmpBillSendingAddr3 = mbosResidentAddr3;
                    else
                    	tmpBillSendingAddr3 = idnoResidentAddr3;

                    if (!mbosResidentAddr4.equals(""))
                    	tmpBillSendingAddr4 = mbosResidentAddr4;
                    else
                    	tmpBillSendingAddr4 = idnoResidentAddr4;

                    if (!mbosResidentAddr5.equals(""))
                    	tmpBillSendingAddr5 = mbosResidentAddr5;
                    else
                    	tmpBillSendingAddr5 = idnoResidentAddr5;
                    break;
                case "2":
                    if (!mbosMailZip.equals(""))
                    	tmpBillSendingZip = mbosMailZip;
                    else
                    	tmpBillSendingZip = idnoMailZip;

                    if (!mbosMailAddr1.equals(""))
                    	tmpBillSendingAddr1 = mbosMailAddr1;
                    else
                    	tmpBillSendingAddr1 = idnoMailAddr1;

                    if (!mbosMailAddr2.equals(""))
                    	tmpBillSendingAddr2 = mbosMailAddr2;
                    else
                    	tmpBillSendingAddr2 = idnoMailAddr2;

                    if (!mbosMailAddr3.equals(""))
                    	tmpBillSendingAddr3 = mbosMailAddr3;
                    else
                    	tmpBillSendingAddr3 = idnoMailAddr3;

                    if (!mbosMailAddr4.equals(""))
                    	tmpBillSendingAddr4 = mbosMailAddr4;
                    else
                    	tmpBillSendingAddr4 = idnoMailAddr4;

                    if (!mbosMailAddr5.equals(""))
                    	tmpBillSendingAddr5 = mbosMailAddr5;
                    else
                    	tmpBillSendingAddr5 = idnoMailAddr5;
                    break;
                case "3":
                    if (!mbosCompanyZip.equals(""))
                    	tmpBillSendingZip = mbosCompanyZip;
                    else
                    	tmpBillSendingZip = idnoCompanyZip;

                    if (!mbosCompanyAddr1.equals(""))
                    	tmpBillSendingAddr1 = mbosCompanyAddr1;
                    else
                    	tmpBillSendingAddr1 = idnoCompanyAddr1;

                    if (!mbosCompanyAddr2.equals(""))
                    	tmpBillSendingAddr2 = mbosCompanyAddr2;
                    else
                    	tmpBillSendingAddr2 = idnoCompanyAddr2;

                    if (!mbosCompanyAddr3.equals(""))
                    	tmpBillSendingAddr3 = mbosCompanyAddr3;
                    else
                    	tmpBillSendingAddr3 = idnoCompanyAddr3;

                    if (!mbosCompanyAddr4.equals(""))
                    	tmpBillSendingAddr4 = mbosCompanyAddr4;
                    else
                    	tmpBillSendingAddr4 = idnoCompanyAddr4;

                    if (!mbosCompanyAddr5.equals(""))
                    	tmpBillSendingAddr5 = mbosCompanyAddr5;
                    else
                    	tmpBillSendingAddr5 = idnoCompanyAddr5;
                    break;
                default:
                	tmpBillSendingZip = actBillSendingZip;
                	tmpBillSendingAddr1 = actBillSendingAddr1;
                	tmpBillSendingAddr2 = actBillSendingAddr2;
                	tmpBillSendingAddr3 = actBillSendingAddr3;
                	tmpBillSendingAddr4 = actBillSendingAddr4;
                	tmpBillSendingAddr5 = actBillSendingAddr5;
                    break;
            }
        }
        
        setString(11, tmpBillSendingZip);
        setString(12, tmpBillSendingAddr1);
        setString(13, tmpBillSendingAddr2);
        setString(14, tmpBillSendingAddr3);
        setString(15, tmpBillSendingAddr4);
        setString(16, tmpBillSendingAddr5);
                
        if (mbosStatSendInternet.equals("Y") && !mbosEMailAddr.equals("")) {
        	setString(17, sysDate);
        }           
        else {
        	setString(17, actEMailEbillDate);
        }
        
        if (mbosRevolveIntRateYear == 0) {
        	setDouble(18, actRevolveIntRateYear);
        	setString(19, actRevolveRateSMonth);
        }        	
        else {
        	setDouble(18, mbosRevolveIntRateYear);
        	setString(19, comc.getSubString(sysDate,0,6));
        }        

        if(tmpBillSendingAddr5.equals(actBillSendingAddr5)) {
        	setString(20, actChgAddrDate);
        }
        else {
        	setString(20, sysDate);
        }
        
        setString(21, pAcctKey);        

        specialSQL = "R";
        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_act_acno            error[notFind]";
            String err2 = pAcnoRowid;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }
    // ************************************************************************
    public int updateActAcno1() throws Exception
    {
        if (debug == 1)
            showLogMessage("I", "", " update acno1=["+ pIdPSeqno +"]"+ validflag +","+ hCardIndicator);

        if (hCardIndicator.equals("2") || swMove.equals("Y"))
            return (0);

        if ((mbosStatSendInternet.length() == 0) && (validflag == 1))
            return (0);

        if ((mbosStatSendInternet.equals(pAcnoStatSendInternet)) && (validflag == 1))
            return (0);

        if (pAcnoStmtCycle.length() > 0)
            tmpInt = Integer.parseInt(hBusiBusinessDate.substring(6, 8))
                    - Integer.parseInt(pAcnoStmtCycle);
        else
            tmpInt = Integer.parseInt(hBusiBusinessDate.substring(6, 8));

        if (tmpInt < 0) // sign(substr(:h_busi_business_date,7,2)-stmt_cycle)
        {
            tmpChar = hTempNextAcctMonth;
            tmpChar1 = addMonth(hTempNextAcctMonth, psctEmail2PaperMm -1);
            tmpChar2 = addMonth(hTempNextAcctMonth, psctPaper2EmailMm -1);
        } else {
            tmpChar = hTempThisAcctMonth;
            tmpChar1 = addMonth(hTempThisAcctMonth, psctEmail2PaperMm -1);
            tmpChar2 = addMonth(hTempThisAcctMonth, psctPaper2EmailMm -1);
        }

        updateSQL = "stat_send_internet = decode(cast(? as varchar(1)) , 'Y', "
                +           " 'Y'                   ,decode(cast(? as int),0,'N',stat_send_paper)) , "
                + "stat_send_s_month2 = decode(cast(? as varchar(1)) , 'Y', "
                +           " cast(? as varchar(10)),decode(cast(? as int),0,'',stat_send_s_month2)) , "
                + "stat_send_e_month2 = decode(cast(? as varchar(1)) , 'Y', "
                +           " ''                    ,decode(cast(? as int),0,'', cast(? as varchar(10)))) , "
                + "internet_upd_user  = decode(cast(? as varchar(1)) , 'Y','CrdD011',internet_upd_user) , "
                + "internet_upd_date  = decode(cast(? as varchar(1)) , 'Y',?  ,internet_upd_date) , "
                + "stat_send_paper    = decode(cast(? as varchar(1)) , 'Y', "
                +           " decode(cast(? as int),0,'N',stat_send_paper),'Y')   , "
                + "stat_send_s_month  = decode(cast(? as varchar(1)) , 'N', "
                +           " cast(? as varchar(10)) ,decode(cast(? as int),0,'',stat_send_s_month)) , "
                + "stat_send_e_month  = decode(cast(? as varchar(1)) , 'Y', "
                +           " ''                    , decode(cast(? as int),0,'', cast(? as varchar(10)))) , "
                + " paper_upd_user    = decode(cast(? as varchar(1)) ,'Y',paper_upd_user,'CRD_D011'), "
                + " paper_upd_date    = decode(cast(? as varchar(1)) ,'Y',paper_upd_date, cast(? as varchar(8))), "
                + "mod_pgm   =  ? , "
                + "mod_time  = sysdate ";
        daoTable  = "act_acno ";
        whereStr  = "where acct_key       = ? "
                + "  and card_indicator = '1' ";

        setString(1, mbosStatSendInternet);
        setInt(   2, psctEmail2PaperMm);
        setString(3, mbosStatSendInternet);
        setString(4, tmpChar);
        setInt(   5, psctEmail2PaperMm);
        setString(6, mbosStatSendInternet);
        setInt(   7, psctEmail2PaperMm);
        setString(8, tmpChar1);

        setString(9, mbosStatSendInternet);
        setString(10, mbosStatSendInternet);
        setString(11, hBusiBusinessDate);
        setString(12, mbosStatSendInternet);
        setInt(   13, psctPaper2EmailMm);

        setString(14, mbosStatSendInternet);
        setString(15, tmpChar);
        setInt(   16, psctPaper2EmailMm);

        setString(17, mbosStatSendInternet);
        setInt(   18, psctPaper2EmailMm);
        setString(19, tmpChar2);

        setString(20, mbosStatSendInternet);
        setString(21, mbosStatSendInternet);
        setString(22, hBusiBusinessDate);
        setString(23, javaProgram);
        setString(24, pAcctKey);

//if(DEBUG == 1) showLogMessage("I",""," update acno1=[" + updateSQL + "]");

// lai test
//   displayParmData="Y";
//   specialSQL="R";

        updateTable();

//   displayParmData="N";

        if (notFound.equals("Y")) {
            String err1 = "update_act_acno_1    error[notFind]" + mbosCardNo;
            String err2 = pAcctKey;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }
    // ************************************************************************
    public int updateActAcno2() throws Exception {
        updateSQL = "class_code      = 'M' " + "mod_pgm         =  ? , "
                + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        daoTable  = "act_acno";
        whereStr  = "where acno_p_seqno =  ? "
                + "  and acct_type    =  ? ";

        setString(1, javaProgram);
        setString(2, sysDate + sysTime);
        setString(3, pPSeqno);
        setString(4, mbosAcctType);

        if (notFound.equals("Y")) {
            String err1 = "update_act_acno2           error[notFind]";
            String err2 = pPSeqno;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }
    // ************************************************************************
//public int update_crd_idno_0() throws Exception 
//{
//        updateSQL = "accept_dm  = ? ,"
//                  + "mod_pgm    = ? , "
//                  + "mod_time   = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
//        daoTable  = "crd_idno";
//        whereStr  = "where rowid          =  ? ";
//
//        setString(1, mbos_accept_dm);
//        setString(2, javaProgram);
//        setString(3, sysDate + sysTime);
//        setRowId( 4, h_id_rowid);
//
//        updateTable();
//if(DEBUG == 1) showLogMessage("I", "", " upd idno_0=[" + h_idno_id_p_seqno + "]");
//        if (notFound.equals("Y")) {
//            String err1 = "update_crd_idno 0          error[notFind]";
//            String err2 = mbos_card_no;
//            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
//        }
//
//        return (0);
//}
// ************************************************************************
    public int updateCrdIdno() throws Exception
    {
        if(debug == 1) showLogMessage("I", "", " upd idno=[" + hIdnoIdPSeqno + "]" + mbosChiName);

        if (mbosSupFlag.equals("1")) {
            updateSQL = "chi_name   = decode( cast(? as vargraphic(50)) ,'',chi_name , ?) , "
                    + "mod_pgm    =  ? , "
                    + "mod_time   = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_idno";
            whereStr  = "where rowid    =  ? ";

            setString(1, mbosChiName);
            setString(2, mbosChiName);
            setString(3, javaProgram);
            setString(4, sysDate + sysTime);
            setRowId( 5, hIdRowid);

            updateTable();
            if (notFound.equals("Y")) {
                String err1 = "update_crd_idno 1    error[notFind]";
                String err2 = mbosCardNo;
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }
        }

        selectSQL = "e_mail_addr,spouse_name,spouse_id_no,resident_no_expire_date,graduation_elementarty,ur_flag, "
        		+ "business_code,inst_flag,credit_level_new,credit_level_old, "
                + "resident_zip,resident_addr1,resident_addr2,resident_addr3,resident_addr4,resident_addr5, "
                + "mail_zip,mail_addr1,mail_addr2,mail_addr3,mail_addr4,mail_addr5, "
                + "company_zip,company_addr1,company_addr2,company_addr3,company_addr4,company_addr5,fee_code_i, "
                + "accept_call_sell,call_sell_from_mark,call_sell_chg_date,e_news,e_news_from_mark,e_news_chg_date, "
                + "accept_dm,dm_from_mark,dm_chg_date,accept_sms,sms_from_mark,sms_chg_date ";

        daoTable  = " CRD_IDNO ";
        whereStr  = " where id_no    = ?";
        setString( 1, mbosApplyId);

        if (selectTable() > 0) {
            idnoEMailAddr = getValue("e_mail_addr");
            idnoSpouseName = getValue("spouse_name");
            idnoSpouseIdNo = getValue("spouse_id_no");
            idnoResidentNoExpireDate = getValue("resident_no_expire_date");
            idnoGraduationElementarty = getValue("graduation_elementarty");
            idnoUrFlag = getValue("ur_flag");
            idnoBusinessCode = getValue("business_code");
            idnoInstFlag = getValue("inst_flag");
            idnoCreditLevelNew = getValue("credit_level_new");
            idnoCreditLevelOld = getValue("credit_level_old");
            idnoMailZip = getValue("mail_zip");
            idnoMailAddr1 = getValue("mail_addr1");
            idnoMailAddr2 = getValue("mail_addr2");
            idnoMailAddr3 = getValue("mail_addr3");
            idnoMailAddr4 = getValue("mail_addr4");
            idnoMailAddr5 = getValue("mail_addr5");
            idnoCompanyZip = getValue("company_zip");
            idnoCompanyAddr1 = getValue("company_addr1");
            idnoCompanyAddr2 = getValue("company_addr2");
            idnoCompanyAddr3 = getValue("company_addr3");
            idnoCompanyAddr4 = getValue("company_addr4");
            idnoCompanyAddr5 = getValue("company_addr5");
            idnoFeeCodeI = getValue("fee_code_i");
            idnoResidentZip = getValue("resident_zip");
            idnoResidentAddr1 = getValue("resident_addr1");
            idnoResidentAddr2 = getValue("resident_addr2");
            idnoResidentAddr3 = getValue("resident_addr3");
            idnoResidentAddr4 = getValue("resident_addr4");
            idnoResidentAddr5 = getValue("resident_addr5");
            idnoAcceptCallSell = getValue("accept_call_sell");
            idnoCallSellFromMark = getValue("call_sell_from_mark");
            idnoCallSellChgDate = getValue("call_sell_chg_date");
            idnoENews = getValue("e_news");
            idnoENewsFromMark = getValue("e_news_from_mark");
            idnoENewsChgDate = getValue("e_news_chg_date");
            idnoAcceptDm = getValue("accept_dm");
            idnoDmFromMark = getValue("dm_from_mark");
            idnoDmChgDate = getValue("dm_chg_date");
            idnoAcceptSms = getValue("accept_sms");
            idnoSmsFromMark = getValue("sms_from_mark");
            idnoSmsChgDate = getValue("sms_chg_date");
        }

        int addrLen = getValue("resident_addr1").length() + getValue("resident_addr2").length()
                + getValue("resident_addr3").length() + getValue("resident_addr4").length()
                + getValue("resident_addr5").length();

        updateSQL = "chi_name       = decode( cast(? as vargraphic(50)),'',chi_name    ,?),"
                + "spouse_name	  = ?, spouse_id_no	  = ?, "
                + "resident_no_expire_date            = ?, graduation_elementarty    = ?, "
                + "ur_flag        = ?, business_code  = ?, inst_flag                 = ?, "
                + "credit_level_new = ?, credit_level_old = ?, fee_code_i            = ?, "
                + "mail_zip       = ?, mail_addr1     = ?, mail_addr2                = ?, "
                + "mail_addr3     = ?, mail_addr4     = ?, mail_addr5                = ?, "
                + "company_zip    = ?, company_addr1  = ?, company_addr2             = ?, "
                + "company_addr3  = ?, company_addr4  = ?, company_addr5             = ?, "
                + "resident_zip   = decode( cast(? as varchar(10)),'',resident_addr1 ,?),"
                + "resident_addr1 = decode( cast(? as vargraphic(60)),'',resident_addr1 ,?), "
                + "resident_addr2 = decode( cast(? as vargraphic(60)),'',resident_addr2 ,?), "
                + "resident_addr3 = decode( cast(? as vargraphic(60)),'',resident_addr3 ,?), "
                + "resident_addr4 = decode( cast(? as vargraphic(60)),'',resident_addr4 ,?), "
                + "resident_addr5 = decode( cast(? as vargraphic(60)),'',resident_addr5 ,?), "
                + "e_mail_addr    = ?, "
                + "home_area_code1= decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,home_area_code1,''),?), "
                + "home_tel_no1   = decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,home_tel_no1   ,''),?), "
                + "home_tel_ext1  = decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,home_tel_ext1  ,''),?), "
                + "home_area_code2= decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,home_area_code2,''),?), "
                + "home_tel_no2   = decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,home_tel_no2   ,''),?), "
                + "home_tel_ext2  = decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,home_tel_ext2  ,''),?), "
                + "office_area_code1= decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,office_area_code1,''),?), "
                + "office_tel_no1   = decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,office_tel_no1   ,''),?), "
                + "office_tel_ext1  = decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,office_tel_ext1  ,''),?), "
                + "office_area_code2= decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,office_area_code2,''),?), "
                + "office_tel_no2   = decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,office_tel_no2   ,''),?), "
                + "office_tel_ext2  = decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,office_tel_ext2  ,''),?), "
                + "e_mail_from_mark=decode( cast(? as varchar(60)),'',e_mail_from_mark,?), "
                + "e_mail_chg_date= decode( cast(? as varchar(60)),'',e_mail_chg_date,?), "
                + "job_position   = decode( cast(? as varchar(60)),'',job_position   ,?), "
                + "cellar_phone     = decode( cast(? as varchar(10)),'', "
                + "    decode(cast(? as int ),0,cellar_phone     ,''),?), "
                + "company_name   = decode( cast(? as varchar(60)),'',company_name   ,?), "
                + "salary_code    = decode( cast(? as varchar(60)),'',salary_code    ,?), "
                + "asset_value    = decode( cast(? as double     ), 0,asset_value    ,?), "
                + "credit_flag    = decode( credit_flag ,'', cast(? as varchar(60)), credit_flag), "
                + "resident_no    = decode( cast(? as varchar(60)),'',resident_no    ,?), "
                + "passport_no    = decode( cast(? as varchar(60)),'',passport_no    ,?), "
                + "other_cntry_code=decode( cast(? as varchar(60)),'',other_cntry_code,?), "
                + "staff_flag     = decode( cast(? as varchar(60)),'',staff_flag     ,?), "
                + "comm_flag      = decode( cast(? as varchar(60)),'',comm_flag      ,?), "
                + "marriage       = decode( cast(? as varchar(60)),'',marriage       ,?), "
                + "education      = decode( cast(? as varchar(60)),'',education      ,?), "
                + "service_year   = decode( cast(? as varchar(4)), '0000',service_year   ,?), "
                + "annual_income  = decode( cast(? as double     ), 0,annual_income  ,?), "
                + "annual_date    = ? , "
                + "student  = decode( cast(? as varchar(60)),'',student  ,?), "
                + "nation   = decode( cast(? as varchar(60)),'',nation   ,?), "
                + "contactor1_name= decode( cast(? as varchar(60)),'',contactor1_name,?), "
                + "contactor1_relation = decode( cast(? as varchar(60)),'',contactor1_relation,?), "
                + "contactor1_area_code= decode( cast(? as varchar(60)),'',contactor1_area_code,?), "
                + "contactor1_tel = decode( cast(? as varchar(60)),'',contactor1_tel ,?), "
                + "contactor1_ext = decode( cast(? as varchar(60)),'',contactor1_ext ,?), "
                + "contactor2_name= decode( cast(? as varchar(60)),'',contactor2_name,?), "
                + "contactor2_relation = decode( cast(? as varchar(60)),'',contactor2_relation,?), "
                + "contactor2_area_code= decode( cast(? as varchar(60)),'',contactor2_area_code,?), "
                + "contactor2_tel = decode( cast(? as varchar(60)),'',contactor2_tel ,?), "
                + "contactor2_ext = decode( cast(? as varchar(60)),'',contactor2_ext ,?), "
                + "est_graduate_month  = decode( cast(? as varchar(60)),'',est_graduate_month ,?), "
                + "market_agree_base   = decode( cast(? as varchar(60)),'',market_agree_base  ,?), "
                + "market_agree_act    = decode( cast(? as varchar(60)),'',market_agree_act   ,?), "
                + "vacation_code       = decode( cast(? as varchar(60)),'',vacation_code  ,?), "
                + "fst_stmt_cycle      = decode( cast(? as varchar(10)),'1', "
                + "    decode(cast(? as varchar(10)),'', ?,fst_stmt_cycle),fst_stmt_cycle), "
                + "accept_call_sell    = decode( cast(? as varchar(60)),'',accept_call_sell  ,?), "
                + "call_sell_from_mark = ? , "
                + "call_sell_chg_date  = ? , "
                + "e_news              = decode( cast(? as varchar(60)),'',e_news  ,?), "
                + "e_news_from_mark    = ? , "
                + "e_news_chg_date     = ? , "
                + "accept_dm           = decode( cast(? as varchar(60)),'',accept_dm  ,?), "
                + "dm_from_mark        = ? , "
                + "dm_chg_date         = ? , "
                + "accept_sms          = decode( cast(? as varchar(60)),'',accept_sms  ,?), "
                + "sms_from_mark       = ? , "
                + "sms_chg_date        = ? , "
                + "msg_flag            = ? , "
                + "msg_purchase_amt    = ? , "
                + "eng_name            = decode( cast(? as vargraphic(25)),'',eng_name ,?), "
                + "mod_pgm   =  ?,  "
                + "mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";

        daoTable  = " CRD_IDNO ";
        whereStr  = " where id_no    = ?";

        int idxF = 1;
        setString(idxF++, mbosChiName);
        setString(idxF++, mbosChiName);
        if (mbosSpouseName.equals(""))
            setString(idxF++, idnoSpouseName);
        else
            setString(idxF++, mbosSpouseName);
        if (mbosSpouseIdNo.equals(""))
            setString(idxF++, idnoSpouseIdNo);
        else
            setString(idxF++, mbosSpouseIdNo);
        if (mbosResidentNoExpireDate.equals(""))
            setString(idxF++, idnoResidentNoExpireDate);
        else
            setString(idxF++, mbosResidentNoExpireDate);
        if (mbosGraduationElementarty.equals(""))
            setString(idxF++, idnoGraduationElementarty);
        else
            setString(idxF++, mbosGraduationElementarty);
        if (mbosUrFlag.equals(""))
            setString(idxF++, idnoUrFlag);
        else
            setString(idxF++, mbosUrFlag);
        if (mbosBusinessCode.equals(""))
            setString(idxF++, idnoBusinessCode);
        else
            setString(idxF++, mbosBusinessCode);
        if (mbosInstFlag.equals(""))
            setString(idxF++, idnoInstFlag);
        else
            setString(idxF++, mbosInstFlag);
        
        if (mbosCreditLevelNew.equals("")) {
        	setString(idxF++, idnoCreditLevelNew);
        	setString(idxF++, idnoCreditLevelOld);
        }            
        else {
        	setString(idxF++, mbosCreditLevelNew);
        	setString(idxF++, mbosCreditLevelNew);
        }
            
        if (mbosFeeCodeI.equals(""))
            setString(idxF++, idnoFeeCodeI);
        else
            setString(idxF++, mbosFeeCodeI);
        if (mbosMailZip.equals(""))
            setString(idxF++, idnoMailZip);
        else
            setString(idxF++, mbosMailZip);
        if (mbosMailAddr1.equals(""))
            setString(idxF++, idnoMailAddr1);
        else
            setString(idxF++, mbosMailAddr1);
        if (mbosMailAddr2.equals(""))
            setString(idxF++, idnoMailAddr2);
        else
            setString(idxF++, mbosMailAddr2);
        if (mbosMailAddr3.equals(""))
            setString(idxF++, idnoMailAddr3);
        else
            setString(idxF++, mbosMailAddr3);
        if (mbosMailAddr4.equals(""))
            setString(idxF++, idnoMailAddr4);
        else
            setString(idxF++, mbosMailAddr4);
        if (mbosMailAddr5.equals(""))
            setString(idxF++, idnoMailAddr5);
        else
            setString(idxF++, mbosMailAddr5);
        if (mbosCompanyZip.equals(""))
            setString(idxF++, idnoCompanyZip);
        else
            setString(idxF++, mbosCompanyZip);
        if (mbosCompanyAddr1.equals(""))
            setString(idxF++, idnoCompanyAddr1);
        else
            setString(idxF++, mbosCompanyAddr1);
        if (mbosCompanyAddr2.equals(""))
            setString(idxF++, idnoCompanyAddr2);
        else
            setString(idxF++, mbosCompanyAddr2);
        if (mbosCompanyAddr3.equals(""))
            setString(idxF++, idnoCompanyAddr3);
        else
            setString(idxF++, mbosCompanyAddr3);
        if (mbosCompanyAddr4.equals(""))
            setString(idxF++, idnoCompanyAddr4);
        else
            setString(idxF++, mbosCompanyAddr4);
        if (mbosCompanyAddr5.equals(""))
            setString(idxF++, idnoCompanyAddr5);
        else
            setString(idxF++, mbosCompanyAddr5);
        setString(idxF++, getValue("resident_zip"));
        setString(idxF++, getValue("resident_zip"));
        setString(idxF++, getValue("resident_addr1"));
        setString(idxF++, getValue("resident_addr1"));
        setString(idxF++, getValue("resident_addr2"));
        setString(idxF++, getValue("resident_addr2"));
        setString(idxF++, getValue("resident_addr3"));
        setString(idxF++, getValue("resident_addr3"));
        setString(idxF++, getValue("resident_addr4"));
        setString(idxF++, getValue("resident_addr4"));
        setString(idxF++, getValue("resident_addr5"));
        setString(idxF++, getValue("resident_addr5"));
////  setString(idx_f++, getValue("business_code"));
////  setString(idx_f++, getValue("business_code"));
        setString(idxF++, idnoEMailAddr);
        setString(idxF++, getValue("home_area_code1"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("home_area_code1"));
        setString(idxF++, getValue("home_tel_no1"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("home_tel_no1"));
        setString(idxF++, getValue("home_tel_ext1"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("home_tel_ext1"));
        setString(idxF++, getValue("home_area_code2"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("home_area_code2"));
        setString(idxF++, getValue("home_tel_no2"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("home_tel_no2"));
        setString(idxF++, getValue("home_tel_ext2"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("home_tel_ext2"));
        setString(idxF++, getValue("office_area_code1"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("office_area_code1"));
        setString(idxF++, getValue("office_tel_no1"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("office_tel_no1"));
        setString(idxF++, getValue("office_tel_ext1"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("office_tel_ext1"));
        setString(idxF++, getValue("office_area_code2"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("office_area_code2"));
        setString(idxF++, getValue("office_tel_no2"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("office_tel_no2"));
        setString(idxF++, getValue("office_tel_ext2"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("office_tel_ext2"));
        setString(idxF++, getValue("e_mail_from_mark"));
        setString(idxF++, getValue("e_mail_from_mark"));
        setString(idxF++, getValue("e_mail_chg_date"));
        setString(idxF++, getValue("e_mail_chg_date"));
        setString(idxF++, getValue("job_position"));
        setString(idxF++, getValue("job_position"));
        setString(idxF++, getValue("cellar_phone"));
        setInt(idxF++, chgPhoneFlag);
        setString(idxF++, getValue("cellar_phone"));
        setString(idxF++, getValue("company_name"));
        setString(idxF++, getValue("company_name"));
        setString(idxF++, getValue("salary_code"));
        setString(idxF++, getValue("salary_code"));
        setDouble(idxF++, getValueDouble("value"));
        setDouble(idxF++, getValueDouble("value"));
        setString(idxF++, getValue("credit_flag"));
//setString(idx_f++, getValue("credit_flag"));
        setString(idxF++, getValue("resident_no"));
        setString(idxF++, getValue("resident_no"));
        setString(idxF++, getValue("passport_no"));
        setString(idxF++, getValue("passport_no"));
        setString(idxF++, getValue("other_cntry_code"));
        setString(idxF++, getValue("other_cntry_code"));
        setString(idxF++, getValue("staff_flag"));
        setString(idxF++, getValue("staff_flag"));
        setString(idxF++, getValue("comm_flag"));
        setString(idxF++, getValue("comm_flag"));
        setString(idxF++, getValue("marriage"));
        setString(idxF++, getValue("marriage"));
        setString(idxF++, getValue("education"));
        setString(idxF++, getValue("education"));
        setString(idxF++, getValue("service_year"));
        setString(idxF++, getValue("service_year"));
        setDouble(idxF++, getValueDouble("salary"));
        setDouble(idxF++, getValueDouble("salary"));
        setString(idxF++, sysDate);
        setString(idxF++, getValue("student"));
        setString(idxF++, getValue("student"));
        setString(idxF++, getValue("nation"));
        setString(idxF++, getValue("nation"));
        setString(idxF++, getValue("contactor1_name"));
        setString(idxF++, getValue("contactor1_name"));
        setString(idxF++, getValue("contactor1_relation"));
        setString(idxF++, getValue("contactor1_relation"));
        setString(idxF++, getValue("contactor1_area_code"));
        setString(idxF++, getValue("contactor1_area_code"));
        setString(idxF++, getValue("contactor1_tel"));
        setString(idxF++, getValue("contactor1_tel"));
        setString(idxF++, getValue("contactor1_ext"));
        setString(idxF++, getValue("contactor1_ext"));
        setString(idxF++, getValue("contactor2_name"));
        setString(idxF++, getValue("contactor2_name"));
        setString(idxF++, getValue("contactor2_relation"));
        setString(idxF++, getValue("contactor2_relation"));
        setString(idxF++, getValue("contactor2_area_code"));
        setString(idxF++, getValue("contactor2_area_code"));
        setString(idxF++, getValue("contactor2_tel"));
        setString(idxF++, getValue("contactor2_tel"));
        setString(idxF++, getValue("contactor2_ext"));
        setString(idxF++, getValue("contactor2_ext"));
        setString(idxF++, getValue("est_graduate_month"));
        setString(idxF++, getValue("est_graduate_month"));
        setString(idxF++, getValue("market_agree_base"));
        setString(idxF++, getValue("market_agree_base"));
        setString(idxF++, getValue("market_agree_act"));
        setString(idxF++, getValue("market_agree_act"));
        setString(idxF++, getValue("vacation_code"));
        setString(idxF++, getValue("vacation_code"));
        setString(idxF++, hCardIndicator);
        setString(idxF++, hFstStmtCycle);
        setString(idxF++, pStmtCycle);

        if(mbosMarketAgreeBase.equals("Y")) {
            mbosAcceptCallSell = "N";
        }else{
            mbosAcceptCallSell = "Y";
        }

        setString(idxF++, mbosAcceptCallSell);
        setString(idxF++, mbosAcceptCallSell);

        if (mbosAcceptCallSell.equals("idno_accept_call_sell")) {
            setString(idxF++, idnoCallSellFromMark);
            setString(idxF++, idnoCallSellChgDate);
        }
        else {
            setString(idxF++, "A");
            setString(idxF++, sysDate);
        }

        setString(idxF++, getValue("e_news"));
        setString(idxF++, getValue("e_news"));

        if (mbosENews.equals("idno_e_news")) {
            setString(idxF++, idnoENewsFromMark);
            setString(idxF++, idnoENewsChgDate);
        }
        else {
            setString(idxF++, "A");
            setString(idxF++, sysDate);
        }

        if(mbosENews.equals("Y")) {
            mbosAcceptDm = "Y";
            mbosAcceptSms = "Y";
        }else{
            mbosAcceptDm = "N";
            mbosAcceptSms = "N";
        }

        setString(idxF++, mbosAcceptDm);
        setString(idxF++, mbosAcceptDm);

        if (mbosAcceptDm.equals("idno_accept_dm")) {
            setString(idxF++, idnoDmFromMark);
            setString(idxF++, idnoDmChgDate);
        }
        else {
            setString(idxF++, "A");
            setString(idxF++, sysDate);
        }

        setString(idxF++, mbosAcceptSms);
        setString(idxF++, mbosAcceptSms);

        if (mbosAcceptSms.equals("idno_accept_sms")) {
            setString(idxF++, idnoSmsFromMark);
            setString(idxF++, idnoSmsChgDate);
        }
        else {
            setString(idxF++, "A");
            setString(idxF++, sysDate);
        }
        
        setString(idxF++, tmpMsgFlag);
        setInt(idxF++, tmpMsgPurchaseAmt);
        setString(idxF++, mbosEngName);
        setString(idxF++, mbosEngName);

        setString(idxF++, javaProgram);
        setString(idxF++, sysDate + sysTime);

        setString(idxF++, mbosApplyId);

// if(DEBUG == 1) showLogMessage("I",""," update sql =[" + updateSQL + "]");

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_idno      error[notFind]";
            String err2 = mbosCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }
    // ************************************************************************
    public int updateErrorEmboss(String errorMsg) throws Exception {
        updateSQL = "in_main_error   =  ? , " + "in_main_msg     =  ? , "
        		+ "check_code      =  ? , "
                + "mod_pgm         =  ? , "
                + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";

        daoTable  = "crd_emboss";
        whereStr  = "where rowid   = ? ";

        setString(1, Integer.toString(rtnCode));
        setString(2, errorMsg);
        setString(3, tmpCheckCode);
        setString(4, javaProgram);
        setString(5, sysDate + sysTime);
        setRowId(6, mbosRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss_error    error[notFind]";
            String err2 = mbosCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        if (rtnCode != 0) {
            updateSQL = //"in_main_date    =  ? , " + 
                    "in_main_error   =  ? , "
                            + "mod_pgm         =  ? , "
                            + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";

            daoTable  = "crd_nccc_stat ";
            whereStr  = "where batchno = ? " + "  and card_no = ? ";

//            setString(1, h_busi_business_date);
            setString(1, Integer.toString(rtnCode));
            setString(2, javaProgram);
            setString(3, sysDate + sysTime);
            setString(4, mbosBatchno);
            setString(5, mbosCardNo);

            updateTable();
        }

        return 0;
    }

    // ************************************************************************
    public int updateOldCard(int type) throws Exception {
        /* 正卡換新卡號,要更新附卡major_card_no (不包括普申金卡) */
        if (hCardSupFlag.equals("0") && mbosEmbossReason.compareTo("2") != 0) {
            updateSupCard(hOldCardNo, mbosCardNo);
        }

        /********************************************
         * type = 1 為重製,type=2 為普申金
         ********************************************/

        if (type == 1) {
            updateSQL = "old_activate_type = ? , " + "old_activate_flag = ? , "
                    + "old_activate_date = ? , " + "activate_type     = ? , "
                    + "activate_flag     = ? , " + "activate_date     = ? , "
                    + "new_card_no       = ? , " + "reissue_date      = ? , "
                    + "reissue_status    = ? , " + "mod_pgm           = ? , "
                    + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_card ";
            whereStr  = "where card_no        =  ? ";
            setString(1, hCardOldActivateType);
            setString(2, hCardOldActivateFlag);
            setString(3, hCardOldActivateDate);
            setString(4, hCardActivateType);
            setString(5, hCardActivateFlag);
            setString(6, hCardActivateDate);
            setString(7, hCardNewCardNo);
            setString(8, hCardReissueDate);
            setString(9, hCardReissueStatus);
            setString(10, javaProgram);
            setString(11, sysDate + sysTime);
            setString(12, mbosOldCardNo);
        } else {
            updateSQL = "new_card_no       = ? , " + "upgrade_status    = ? , "
                    + "upgrade_date      = ? , " + "mod_pgm           = ? , "
                    + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_card ";
            whereStr  = "where card_no        =  ? ";
            setString(1, hCardNewCardNo);
            setString(2, hCardUpgradeStatus);
            setString(3, hCardUpgradeDate);
            setString(4, javaProgram);
            setString(5, sysDate + sysTime);
            setString(6, mbosOldCardNo);
        }

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_old crd_card 2      error[notFind]";
            String err2 = mbosCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return 0;
    }

    /***********************************************************
     * 將正卡新卡號,update到附卡之正卡卡號
     ***********************************************************/
    // ************************************************************************
    public int updateSupCard(String chkMajorCardno, String chkNewMajorCardno) throws Exception {
        String chkOldMajorCardno = "";
        String chkSupCardno = "";

        extendField = "sup.";
        selectSQL   = "major_card_no,card_no        ";
        daoTable    = "crd_card             ";
        whereStr    = "WHERE major_card_no = ?   "
                + "  and sup_flag      = '1' "
                + "  and current_code  = '0' ";

        setString(1, chkMajorCardno);

        if(debug == 1)
            showLogMessage("I", "", " update sup card=[" + chkMajorCardno);
        int recordCnt = selectTable();

        for(int i = 0 ; i < recordCnt ; i++) {
            chkOldMajorCardno = getValue("sup.major_card_no", i);
            chkSupCardno       = getValue("sup.card_no", i);
            if(debug == 1)
                showLogMessage("I", "", " update sup=[" + chkOldMajorCardno + "]" + chkNewMajorCardno);
            if (chkOldMajorCardno.compareTo(chkNewMajorCardno) != 0) {
                if (chkNewMajorCardno.length() > 0) {
                    updateSQL = "major_card_no   =  ? , " + "mod_pgm         =  ? , "
                            + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
                    daoTable  = "crd_card      ";
                    whereStr  = "where card_no      =  ?  " + "  and current_code = '0' ";

                    setString(1, chkNewMajorCardno);
                    setString(2, javaProgram);
                    setString(3, sysDate + sysTime);
                    setString(4, chkSupCardno);

                    updateTable();
                    if (notFound.equals("Y")) {
                        String err1 = "update_sup crd_card        error[notFind]";
                        String err2 = chkNewMajorCardno;
                        comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
                    }
                }
            }
        }

        return 0;
    }

    // ************************************************************************
    public int updateChgCard(int type) throws Exception {

        hCardCvv2 = mbosCvv2;
        hCardPvki = mbosPvki;
        hCardPinBlock = mbosPinBlock;
        hCardIcFlag = getValue("ic_flag");
        if (hCardPinBlock.length() == 0) {
            hCardPinBlock = hPinBlock;
            hCardCvv2 = hCvv2;
            hCardPvki = hPvki;
        }
        hCardBatchno = mbosBatchno;
        hCardRecno = mbosRecno;

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
                    + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') , "
                    + "current_code      = ? , " + "oppost_date       = ? , "
                    + "oppost_reason     = ? , " + "card_ref_num      = ?  ";
            daoTable = "crd_card ";
            whereStr = "where card_no        =  ? ";

            setString(1, hCardOldBegDate);
            setString(2, hCardOldEndDate);
            setString(3, hCardOldActivateType);
            setString(4, hCardOldActivateFlag);
            setString(5, hCardOldActivateDate);
            setString(6, hCardEngName);
            setString(7, hCardEmbossData);
            setString(8, hCardUnitCode);
            setString(9, hCardCvv2);
            setString(10, hCardPvki);
            setString(11, hCardPinBlock);
            setString(12, hCardActivateType);
            setString(13, hCardActivateFlag);
            setString(14, hCardActivateDate);
            setString(15, hCardUrgentFlag);
            setString(16, hCardNewBegDate);
            setString(17, hcardnewenddate);
            setString(18, hCardReissueDate);
            setString(19, hCardReissueStatus);
            setString(20, hCardBatchno);
            setDouble(21, hCardRecno);
            setString(22, hCardBranch);
            setString(23, hCardMailBranch);
            setString(24, hCardMailNo);
            setString(25, "");
            setString(26, hCardMailType);
            setString(27, hCardIcFlag);
            setString(28, hCardOldBankActno);
            setString(29, hCardBankActno);
            setString(30, javaProgram);
            setString(31, sysDate + sysTime);
            setString(32, hCardCurrentCode);
            setString(33, hCardOppostDate);
            setString(34, hCardOppostReason);
            setString(35, hCardCardRefNum);
            setString(36, mbosOldCardNo);
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
                    + "mod_pgm           = ? , "
                    + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), "
                    + "card_ref_num      = ? ";
            daoTable = "crd_card ";
            whereStr = "where card_no        =  ? ";

            setString(1, hCardOldBegDate);
            setString(2, hCardOldEndDate);
            setString(3, hCardOldActivateType);
            setString(4, hCardOldActivateFlag);
            setString(5, hCardOldActivateDate);
            setString(6, hCardEmbossData);
            setString(7, hCardCvv2);
            setString(8, hCardPvki);
            setString(9, hCardPinBlock);
            setString(10, hCardActivateType);
            setString(11, hCardActivateFlag);
            setString(12, hCardActivateDate);
            setString(13, hCardNewBegDate);
            setString(14, hcardnewenddate);
            setString(15, hCardChangeDate);
            setString(16, hCardChangeStatus);
            setString(17, hCardBatchno);
            setDouble(18, hCardRecno);
            setString(19, hCardBranch);
            setString(20, hCardMailBranch);
            setString(21, hCardMailNo);
            setString(22, "");
            setString(23, hCardMailType);
            setString(24, hCardIcFlag);
            setString(25, hCardOldBankActno);
            setString(26, hCardBankActno);
            setString(27, javaProgram);
            setString(28, sysDate + sysTime);
            setString(29, hCardCardRefNum);
            setString(30, mbosOldCardNo);
        }

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_old crd_card        error[notFind]";
            String err2 = mbosCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public int updateEmboss() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " update emboss =[" + rtnCode + "]");
        int changeCreditLimit = 0;

        // * 緊急新製卡需視為資料已回饋
        if (mbosrtnncccdate.length() <= 0)
            mbosrtnncccdate = sysDate;

        if (mbosApplyId.compareTo(mbosPmId) == 0)
            mbosPmIdCode = mbosApplyIdCode;

        // * 之前insert or update 不成功,不可做insert or update
        mbosInMainError = Integer.toString(rtnCode);

        if (debug == 1)
            showLogMessage("I", "", " update emboss 10 =[" + mbosInMainError + "]" + rtnCode);
        if (!mbosInMainError.equals("0")) {
            mbosInMainDate = "";
            mbosInMainMsg = errorMsg;
        } else {
            mbosInMainDate = sysDate;
            mbosInMainMsg = "";
        }

        if (mbosEmbossSource.equals("1")) {
            if ((pAcnoCreditAmt > 0) && (pAcnoCreditAmt != pCreditAmt)) {
                if (validflag == 1) {
                    if ((pCreditAmt < pAcnoCreditAmt) || swMove.equals("Y"))
                        changeCreditLimit = 1;
                }
            }
        }

        if (debug == 1) showLogMessage("I",""," update emboss 11 =[" + changeCreditLimit + "]"
                + mbosInMainDate + "," + mbosInMainMsg);
        if (changeCreditLimit == 1) {
            updateSQL = "apply_id_code   =  ? , " + "pm_id_code      =  ? , "
                    + "acct_type       =  ? , " + "cash_lmt        =  ? , "
                    + "in_main_error   =  ? , " + "in_main_date    =  ? , "
                    + "in_main_msg     =  ? , " + "rtn_nccc_date   =  ? , "
                    + "auth_credit_lmt =  ? , " + "check_code      =  ? , "
                    + "mod_pgm         =  ? , "
                    + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_emboss";
            whereStr  = "where rowid   = ? ";

            setString(1, mbosApplyIdCode);
            setString(2, mbosPmIdCode);
            setString(3, mbosAcctType);
            setInt(4, mbosCashLmt);
            setString(5, mbosInMainError);
            setString(6, mbosInMainDate);
            setString(7, mbosInMainMsg);
            setString(8, mbosrtnncccdate);
            setInt(9, pAcnoCreditAmt);
            setString(10, tmpCheckCode);
            setString(11, javaProgram);
            setString(12, sysDate + sysTime);
            setRowId(13, mbosRowid);
        } else {
            updateSQL = "apply_id_code   =  ? , " + "pm_id_code      =  ? , "
                    + "acct_type       =  ? , " + "cash_lmt        =  ? , "
                    + "in_main_error   =  ? , " + "in_main_date    =  ? , "
                    + "in_main_msg     =  ? , " + "rtn_nccc_date   =  ? , "
                    + "check_code      =  ? , "
                    + "mod_pgm         =  ? , "
                    + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_emboss";
            whereStr  = "where rowid     = ? ";

            setString(1, mbosApplyIdCode);
            setString(2, mbosPmIdCode);
            setString(3, mbosAcctType);
            setInt(4, mbosCashLmt);
            setString(5, mbosInMainError);
            setString(6, mbosInMainDate);
            setString(7, mbosInMainMsg);
            setString(8, mbosrtnncccdate);
            setString(9, tmpCheckCode);
            setString(10, javaProgram);
            setString(11, sysDate + sysTime);
            setRowId(12, mbosRowid);
        }

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss    error1[notFind]" ;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public int updateEmboss2() throws Exception {
        int changeCreditLimit = 0;

        if (debug == 1)
            showLogMessage("I", "", " update emboss2 =[" + 2 + "]");

        /* 緊急新製卡需視為資料已回饋 */
        if (mbosEmbossSource.equals("1")) {
            if ((pAcnoCreditAmt > 0) && (pAcnoCreditAmt != pCreditAmt)) {
                if (validflag == 1) {
                    if ((pCreditAmt < pAcnoCreditAmt) || swMove.equals("Y"))
                        changeCreditLimit = 1;
                }
            }
        }

        if (mbosrtnncccdate.length() <= 0)
            mbosrtnncccdate = sysDate;

        if (mbosApplyId.compareTo(mbosPmId) == 0)
            mbosPmIdCode = mbosApplyIdCode;

        // * 之前insert or update 不成功,不可做insert or update
        mbosInMainError = Integer.toString(rtnCode);

        if (!mbosInMainError.equals("0")) {
            mbosInMainDate = "";
            mbosInMainMsg = errorMsg;
        } else {
            mbosInMainDate = sysDate;
            mbosInMainMsg = "";
        }

        if (changeCreditLimit == 1) {
            updateSQL = "class_code      ='M' , " + "apply_id_code   =  ? , "
                    + "pm_id_code      =  ? , " + "acct_type       =  ? , "
                    + "cash_lmt        =  ? , " + "in_main_error   =  ? , "
                    + "in_main_date    =  ? , " + "in_main_msg     =  ? , "
                    + "rtn_nccc_date   =  ? , " + "auth_credit_lmt =  ? , "
                    + "check_code      =  ? , "
                    + "mod_pgm         =  ? , "
                    + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_emboss";
            whereStr  = "where rowid   = ? ";

            setString(1, mbosApplyIdCode);
            setString(2, mbosPmIdCode);
            setString(3, mbosAcctType);
            setInt(4, mbosCashLmt);
            setString(5, mbosInMainError);
            setString(6, mbosInMainDate);
            setString(7, mbosInMainMsg);
            setString(8, mbosrtnncccdate);
            setInt(9, pAcnoCreditAmt);
            setString(10, tmpCheckCode);
            setString(11, javaProgram);
            setString(12, sysDate + sysTime);
            setRowId( 13, mbosRowid);
        } else {
            updateSQL = "class_code      ='M' , " + "apply_id_code   =  ? , "
                    + "pm_id_code      =  ? , " + "acct_type       =  ? , "
                    + "cash_lmt        =  ? , " + "in_main_error   =  ? , "
                    + "in_main_date    =  ? , " + "in_main_msg     =  ? , "
                    + "rtn_nccc_date   =  ? , " + "check_code      =  ? , "
                    + "mod_pgm         =  ? , "
                    + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_emboss";
            whereStr  = "where rowid   = ? ";

            setString(1, mbosApplyIdCode);
            setString(2, mbosPmIdCode);
            setString(3, mbosAcctType);
            setInt(4, mbosCashLmt);
            setString(5, mbosInMainError);
            setString(6, mbosInMainDate);
            setString(7, mbosInMainMsg);
            setString(8, mbosrtnncccdate);
            setString(9, tmpCheckCode);
            setString(10, javaProgram);
            setString(11, sysDate + sysTime);
            setRowId(12, mbosRowid);
        }

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss    error2[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return 0;
    }
    // ************************************************************************
    public int procNewCard() throws Exception {
            	
        int rtn = getAcctType();
        if (debug == 1)
            showLogMessage("D", "", " proc_new =[" + hCardIndicator + "]maj=" + mbosMajorCardNo);

        if (hCardIndicator.equals("1"))
            rtn = procOrgNewCard();
        else
            rtn = procBusNewCard();

        if (rtn != 0)
            return (1);

        return 0;
    }

    // ************************************************************************
    public int procOrgNewCard() throws Exception {

        if (debug == 1)
            showLogMessage("D", "", " proc_org_new =[" + "] " + mbosPmId);

        // ****************************************************
        // 檢核附卡所屬正卡之acct_type,acct_key
        // ****************************************************
        if (mbosSupFlag.equals("1")) {
            tmpInt = checkPmCard();
            if (tmpInt != 0)
                return (tmpInt);
            hAcctKey = mbosPmId + mbosPmIdCode;
        }
        initCrdIdno();
        int idnoExitFlag = checkIdno();

        if (debug == 1)
            showLogMessage("I", "", "  888 org_new[" + mbosApplyId +"]" + idnoExitFlag);

        // *************************************************************
        // new crd_idno to get id_p_seqno before insert into act_acno
        // *************************************************************
        if (idnoExitFlag != 0) {
            tmpInt = getIdPSeqno();
        }

        moveIdnoVar();

        if (debug == 1)
            showLogMessage("I", "", "  888 idno_exit[" + idnoExitFlag + "]");
        // *********************************************************
        // 檢核ID_CODE是否存在
        // *********************************************************


        if (idnoExitFlag != 0) {
            int suc = 0;
            while (suc == 0) {
                if (mbosVoiceNum.length() == 0) {
                	tmpCheckCode = "E06";
                    errorMsg = "無語音密碼";
                    return (1);
                }
                // **********************************************
                // id_code已存在,id_code+1
                // **********************************************
                if (existedIdno() != 0) {
                    tmpInt = Integer.parseInt(mbosApplyIdCode) + 1;
                    mbosApplyIdCode = Integer.toString(tmpInt);
                    if (debug == 1)
                        showLogMessage("I", "", "  888 id_code [" + mbosApplyIdCode + "]");
                } else
                    suc = 1;
            }
        }

        // *******************************************************
        // 正卡之acct_type,acct_key(apply_id_code)可能會變更
        // *******************************************************
        if (mbosSupFlag.equals("0")) {
            hAcctKey = mbosApplyId + mbosApplyIdCode;
        }
        // **************************
        // move to data1
        // **************************
        initProcssData();
        moveToData1();
        // select_ptr_acct_type(); // 已抓
//        selectPtrPurLmt();
        tmpInt = subMainProcess();
        if (tmpInt != 0)
            return (1);

        mbosAcctType = pAcctType;
        mbosStmtCycle = pStmtCycle;
        /********************************
         * insert crd_idno
         ********************************/
        chgPhoneFlag = 0;
        /*
         * lai test idno_exit_flag = 0;
         */
        if (debug == 1)
            showLogMessage("I", "", "888  Step insert  =["+idnoExitFlag+"]"+ hIdnoIdPSeqno);
        if (idnoExitFlag == 0) {
            chgPhoneFlag = checkChgPhone();
            if ((rtn = updateCrdIdno()) != 0)
                return (rtn);

        }
        else {
//             str2var(h_idno_fst_stmt_cycle,p_stmt_cycle.arr);
            insertCrdIdno(1);
        }

        /********************/
        /* insert crd_card */
        /* 1. 注意:於insert_crd_card()前所做的str2var若有新增修改，視情況選(1)或(2) */
        /* (1)修正proc_org_new_card()、proc_bus_new_card()、proc_upgrade_card()、 */
        /* proc_reissue_card()、proc_urgent_card() */
        /* (2)直接放於insert_crd_card()處理 */
        /*****************************************************************************/

        mbosCashLmt = pComboCashLimit;
        hCardStmtCycle = pStmtCycle;
        hCardPSeqno = pPSeqno;
        hCardGpNo = pGpNo;
        hCardIdPSeqno = hIdnoIdPSeqno;
        hCardApplyAtmFlag = getValue("send_pwd_flag");
        hCardJcicScore = getValueDouble("jcic_score");
        hCardCardMoldFlag = getValue("card_mold_flag");

        if (debug == 1)
            showLogMessage("I", "", "888   new  card =[" + hIdnoIdPSeqno + "]");

        setValue("combo_acct_no", "");
        if (hComboIndicator.compareTo("N") != 0) /* 201504013 三合一卡改不等於N */
            hCardComboAcctNo = mbosActNo;

        moveCardVar();

        hCardActivateFlag = "1";
//        if (mbos_online_mark.equals("2"))
//            h_card_activate_flag = "2";
// 舊卡友且  accept_call_sell <> 'Y' 才能 update
//        if (idno_exit_flag == 0 && h_idno_accept_call_sell.compareTo("Y") !=0) {
//            tmp_int = update_crd_idno_0();
//        }

        if ((rtn = insertCrdCard(1)) != 0) {
            return (rtn);
        }

        return 0;
    }

    // ************************************************************************
    public int procBusNewCard() throws Exception {
        int busExitFlag = 0;

        busExitFlag = getCorpData();
        if (debug == 1)
            showLogMessage("D", "", " proc_bus_new =[" + busExitFlag + "] ");

        if (busExitFlag != 0)
            return (1);

        busExitFlag = checkIdno();
        if (debug == 1)
            showLogMessage("I", "", "  888 bus_idno=[" + busExitFlag + "]");

        /**************************************************************
         * new crd_idno to get id_p_seqno before insert into act_acno
         **************************************************************/
        if (busExitFlag != 0)
            getIdPSeqno();

        /*********************************************************
         * 檢核ID_CODE是否存在
         *********************************************************/

        moveIdnoVar();

        if (busExitFlag != 0) {
            int ccode = Integer.parseInt(mbosApplyIdCode);
            int suc = 0;
            while (suc == 0) {
                if (mbosVoiceNum.length() == 0) {
                	tmpCheckCode = "D06";
                    errorMsg = "無語音密碼";
                    return (1);
                }
                rtn = existedIdno();
                /***********************************************
                 * id_code已存在,id_code+1
                 ***********************************************/
                if (rtn != 0) {
                    ccode++;
                    mbosApplyIdCode = Integer.toString(ccode);
                    hIdnoIdCode = Integer.toString(ccode);
                } else {
                    suc = 1;
                }
            }
        }
        /***************************
         * move to data2
         ***************************/

        /* 需產生ACT_ACNO,個繳時AC_ACCT */
        initProcssData();
        moveToData2();
//        selectPtrPurLmt();
        if ((rtn = subBusProcess()) != 0)
            return (1);
        /***********************
         * insert_crd_idno
         ***********************/
        mbosCashLmt = pComboCashLimit;
        mbosAcctType = pAcctType;
        // mbos_acct_key      = p_acct_key;
        mbosStmtCycle = hCorpStmtCycle;
        mbosCorpActFlag = pCorpActFlag;
        if (busExitFlag == 0) {
            chgPhoneFlag = checkChgPhone();
            if ((rtn = updateCrdIdno()) != 0)
                return (rtn);
        } else {
            insertCrdIdno(2);
        }
        /********************/
        /* insert crd_card */
        /********************/
        hCardStmtCycle = hCorpStmtCycle;
        hCardPSeqno = pPSeqno;
        hCardGpNo = pGpNo;
        hCardIdPSeqno = hIdnoIdPSeqno;
        if (mbosCorpActFlag.equals("Y")) {
            hCardGpNo = hCorpAcnoPSeqno;
        } else {
            hCardPSeqno = pPSeqno;
            hCardGpNo = pGpNo;
        }
        moveCardVar();

        if ((rtn = insertCrdCard(2)) != 0) {
            return (rtn);
        }

        return 0;
    }

    // ************************************************************************
    void initCrdIdno()
    {
        hIdnoIdPSeqno = "";
        hIdnoId = "";
        hIdnoIdCode = "";
        hIdnoChiName = "";
        hIdnoVoiceNum = "";
        hIdnoSex = "";
        hIdnoMarriage = "";
        hIdnoBirthday = "";
        hIdnoEducation = "";
        hIdnoStudent = "";
        hIdnoNation = "";
        hIdnoAssetValue = 0;
        hIdnoServiceYear = "";
        hIdnoAnnualIncome = 0;
        hIdnoAnnualDate = "";
        hIdnoStaffFlag = "";
        hIdnoCreditFlag = "";
        hIdnoCommFlag = "";
        hIdnoOtherCntryCode = "";
        hIdnoCommFlag = "";
        hIdnoSalaryCode = "";
        hIdnoOfficeAreaCode1 = "";
        hIdnoOfficeTelNo1 = "";
        hIdnoOfficeTelExt1 = "";
        hIdnoOfficeAreaCode2 = "";
        hIdnoOfficeTelNo2 = "";
        hIdnoOfficeTelExt2 = "";
        hIdnoHomeAreaCode1 = "";
        hIdnoHomeTelNo1 = "";
        hIdnoHomeTelExt1 = "";
        hIdnoHomeAreaCode2 = "";
        hIdnoHomeTelNo2 = "";
        hIdnoHomeTelExt2 = "";
        hIdnoResidentZip = "";
        hIdnoResidentAddr1 = "";
        hIdnoResidentAddr2 = "";
        hIdnoResidentAddr3 = "";
        hIdnoResidentAddr4 = "";
        hIdnoResidentAddr5 = "";
        hIdnoJobPosition = "";
        hIdnoCompanyName = "";
        hIdnoBusinessCode = "";
        hIdnoCellarPhone = "";
        hIdnoCardSince = "";
        hIdnoVoiceNum = "";
        hIdnoCreateDate = "";
        hIdnoModUser = "";
        hIdnoFstStmtCycle = "";
        hIdnoContactor1Name = "";
        hIdnoContactor1Relation = "";
        hIdnoContactor1AreaCode = "";
        hIdnoContactor1Tel = "";
        hIdnoContactor1Ext = "";
        hIdnoContactor2Name = "";
        hIdnoContactor2Relation = "";
        hIdnoContactor2AreaCode = "";
        hIdnoContactor2Tel = "";
        hIdnoContactor2Ext = "";
        hIdnoEstGraduateMonth = "";
        hIdnoMarketAgreeBase = "";
        hIdnoVacationCode = "";
        hIdnoMarketAgreeAct =  "";
    }

    /***************************************************************************/
    public int moveIdnoVar() throws Exception {

        hIdnoId = mbosApplyId;
        hIdnoIdCode = mbosApplyIdCode;
        hIdnoChiName = mbosChiName;
        hIdnoStaffFlag = getValue("staff_flag");
        hIdnoSex = getValue("sex");
        hIdnoBirthday = mbosBirthday;
        hIdnoMarriage = getValue("marriage");
        hIdnoEducation = getValue("education");
        hIdnoStudent = mbosStudent;
        hIdnoNation = getValue("nation");
        hIdnoServiceYear = getValue("service_year");
        hIdnoAnnualIncome = getValueDouble("salary");

        hIdnoAssetValue = getValueDouble("value");
        hIdnoCreditFlag = getValue("credit_flag");
        hIdnoCommFlag = getValue("comm_flag");
        hIdnoSalaryCode = getValue("salary_code");
        hIdnoVoiceNum = mbosVoiceNum;
        if (mbosToNcccDate.length() > 0) {
            hIdnoCardSince = mbosToNcccDate;
            hIdnoAnnualDate = mbosToNcccDate;
        } else {
            hIdnoCardSince = sysDate;
            hIdnoAnnualDate = sysDate;
        }

        hIdnoOfficeAreaCode1 = getValue("office_area_code1");
        hIdnoOfficeTelNo1 = getValue("office_tel_no1");
        hIdnoOfficeTelExt1 = getValue("office_tel_ext1");
        hIdnoOfficeAreaCode2 = getValue("office_area_code2");
        hIdnoOfficeTelNo2 = getValue("office_tel_no2");
        hIdnoOfficeTelExt2 = getValue("office_tel_ext2");
        hIdnoHomeAreaCode1 = getValue("home_area_code1");
        hIdnoHomeTelNo1 = getValue("home_tel_no1");
        hIdnoHomeTelExt1 = getValue("home_tel_ext1");
        hIdnoHomeAreaCode2 = getValue("home_area_code2");
        hIdnoHomeTelNo2 = getValue("home_tel_no2");
        hIdnoHomeTelExt2 = getValue("home_tel_ext2");
        hIdnoResidentZip = getValue("resident_zip");
        hIdnoResidentAddr1 = getValue("resident_addr1");
        hIdnoResidentAddr2 = getValue("resident_addr2");
        hIdnoResidentAddr3 = getValue("resident_addr3");
        hIdnoResidentAddr4 = getValue("resident_addr4");
        hIdnoResidentAddr5 = getValue("resident_addr5");
        hIdnoJobPosition = getValue("job_position");
        hIdnoCompanyName = getValue("company_name");
        hIdnoBusinessCode = getValue("business_code");
        hIdnoCellarPhone = getValue("cellar_phone");
        hIdnoEMailAddr = getValue("e_mail_addr");
        hIdnoContactor1Name = getValue("contactor1_name");
        hIdnoContactor1Relation = getValue("contactor1_relation");
        hIdnoContactor1AreaCode = getValue("contactor1_area_code");
        hIdnoContactor1Tel = getValue("contactor1_tel");
        hIdnoContactor1Ext = getValue("contactor1_ext");
        hIdnoContactor2Name = getValue("contactor2_name");
        hIdnoContactor2Relation = getValue("contactor2_relation");
        hIdnoContactor2AreaCode = getValue("contactor2_area_code");
        hIdnoContactor2Tel = getValue("contactor2_tel");
        hIdnoContactor2Ext = getValue("contactor2_ext");
        hIdnoEstGraduateMonth = mbosEstGraduateMonth;
        hIdnoMarketAgreeBase = getValue("market_agree_base");
        hIdnoVacationCode = getValue("vacation_code");
        hIdnoMarketAgreeAct = getValue("market_agree_act");
        hIdnoAcceptDm = mbosAcceptDm;
        hIdnoCreateDate = sysDate;
        hIdnoModUser = hTempUser;
        hIdnoOtherCntryCode = getValue("other_cntry_code");

        return 0;
    }

    // ************************************************************************
    public int moveOldCardVar() throws Exception {

        hCardNewCardNo = "";
        hCardCardNo = mbosCardNo;
        hCardCurrentCode = "0";
        hCardEmergentFlag = "";
        hCardBatchno = mbosBatchno;
        hCardRecno = mbosRecno;
        if (hCardSupFlag.equals("0"))
            hCardMajorCardNo = hMajorCardNo;

        hCardCvv2 = getValue("cvv2");
        hCardPvki = getValue("pvki");
        hCardPinBlock = getValue("pin_block");
        hCardEmbossData = getValue("emboss_4th_data");

        hCardUpgradeStatus = "";
        hCardActivateType = "";
        hCardActivateFlag = "1";
        hCardActivateDate = "";
        hCardMailBranch = getValue("mail_branch");
        hCardMailType = getValue("mail_type");
        hCardMailNo = "";

        if (mbosToNcccDate.length() > 0)
            hCardIssueDate = mbosToNcccDate;
        else
            hCardIssueDate = sysDate;

        hCardIcFlag = getValue("ic_flag");
        hCardBranch = getValue("branch");
        hCardCurrCode = getValue("curr_code");

        return (0);
    }

    // ************************************************************************
    public int moveToData1() throws Exception {
        pCardNo = mbosCardNo;
        pRegBankNo = getValue("reg_bank_no");
        pRiskBankNo = getValue("risk_bank_no");
        pActNo = getValue("act_no");
        pCorpNo = getValue("corp_no");
        pCorpNoCode = getValue("corp_no_code");
        pSupFlag = getValue("sup_flag");
        pAcctType = getValue("acct_type");
        pClassCode = getValue("class_code");
        pVipCode = getValue("vip");
//        p_accept_dm        = getValue("accept_dm");
        pCorpAssureFlag = getValue("corp_assure_flag");
        pCorpActFlag = getValue("corp_act_flag");
        pCreditAmt = getValueInt("auth_credit_lmt");
        pAcctKey = hAcctKey;

        if (hComboIndicator.compareTo("N") != 0) // *20150413 三合一卡改不等於N
        {
            pComboAcctNo = getValue("act_no");
        }
        if (mbosApplyId.compareTo(mbosPmId) != 0) {
            pAcctHolderId = mbosPmId;
            pAcctHolderIdCode = mbosPmIdCode;
            pIdPSeqno = hMajorIdPSeqno;
        } else {
            pAcctHolderId = mbosApplyId;
            pAcctHolderIdCode = mbosApplyIdCode;
            pIdPSeqno = hIdnoIdPSeqno;
            if (mbosEmbossSource.equals("1")) {
                pMailZip = getValue("mail_zip");
                pMailAddr1 = getValue("mail_addr1");
                pMailAddr2 = getValue("mail_addr2");
                pMailAddr3 = getValue("mail_addr3");
                pMailAddr4 = getValue("mail_addr4");
                pMailAddr5 = getValue("mail_addr5");
            }
        }
        pStmtCycle = mbosStmtCycle;

        return 0;
    }

    // ************************************************************************
    public int moveToData2() throws Exception {
        pCardNo = mbosCardNo;
        pRiskBankNo = getValue("risk_bank_no");
        pActNo = getValue("act_no");
        pCorpNo = getValue("corp_no");
        pCorpNoCode = getValue("corp_no_code");
        pSupFlag = getValue("sup_flag");
        pAcctType = getValue("acct_type");
        pClassCode = getValue("class_code");
        pVipCode = getValue("vip");
//        p_accept_dm        = getValue("accept_dm");
        pCorpAssureFlag = getValue("corp_assure_flag");
        pCorpActFlag = getValue("corp_act_flag");
        // p_credit_act_no    = h_corp_credit_act_no;
        pCreditAmt = getValueInt("auth_credit_lmt");

        pCorpAcnoPSeqno = hCorpAcnoPSeqno;
        pAcctHolderId = mbosApplyId;
        pAcctHolderIdCode = mbosApplyIdCode;
        pIdPSeqno = hIdnoIdPSeqno;
        pMailZip = getValue("mail_zip");
        pMailAddr1 = getValue("mail_addr1");
        pMailAddr2 = getValue("mail_addr2");
        pMailAddr3 = getValue("mail_addr3");
        pMailAddr4 = getValue("mail_addr4");
        pMailAddr5 = getValue("mail_addr5");
        pStmtCycle = mbosStmtCycle;

        return 0;
    }

    // ************************************************************************
//    public int insert_act_chkno() throws Exception {
//        if (DEBUG == 1)
//            showLogMessage("I", "", "888 insert chkno=[" + mbos_card_no + "]");
//        selectSQL = "count(*)   as chkno_cnt        ";
//        daoTable  = "act_chkno           ";
//        whereStr  = "WHERE p_seqno      =  ?  " + "  and crt_date     =  ?  " 
//                  + "  and curr_code    = '901' ";
//
//        setString(1, p_p_seqno);
//        setString(2, sysDate);
//
//        int recCnt = selectTable();
//
//        if (getValueInt("chkno_cnt") > 0)
//            return (0);
//
//        setValue("p_seqno"   , p_p_seqno);
//        setValue("id_p_seqno", h_idno_id_p_seqno);
//        setValue("autopay_acct_bank", "0172015");
//        setValue("autopay_acct_no"  , mbos_act_no_l);
//        setValue("card_no"          , mbos_card_no);
//        setValue("autooay_indicator", mbos_act_no_l_ind);
//        setValueInt("process_status", 0);
//        setValue("valid_flag" , h_temp_valid);
//        setValue("from_mark"  , "1");
//        setValue("verify_flag", "Y");
//        setValue("verify_date", sysDate);
//        setValue("verify_return_code", "00");
//        setValue("exec_check_flag"   , "Y");
//        setValue("exec_check_date"   , sysDate);
//        setValue("ibm_check_flag"    , "N");
//        setValue("stmt_cycle"        , mbos_stmt_cycle);
//        setValue("autopay_id_p_seqno", h_idno_id_p_seqno);
//        setValue("autopay_id"        , mbos_apply_id);
//        setValue("autopay_id_code"   , mbos_apply_id_code);
//        setValue("proc_mark"         , h_ckno_proc_mark);
//        setValue("crt_date"          , sysDate);
//        setValue("mod_time"          , sysDate + sysTime);
//        setValue("mod_pgm"           , javaProgram);
//        setValue("ach_check_flag"    , "N");
//        setValue("ach_send_date"     , sysDate);
//        setValue("ach_rtn_date"      , sysDate);
//        setValue("old_acct_bank"     , h_ckno_old_acct_bank);
//        setValue("old_acct_no"       , h_ckno_old_acct_no);
//        setValue("old_acct_id"       , h_ckno_old_acct_id);
//        setValue("curr_code"         , "901");
//        setValue("batchno"           , mbos_batchno);
//
//        daoTable = "act_chkno ";
//
//        insertTable();
//
//        if (dupRecord.equals("Y")) {
//            String err1 = "insert_act_chkno         error[dupRecord]=";
//            String err2 = mbos_apply_id;
//            comcr.err_rtn(err1, err2, comcr.h_call_batch_seqno);
//        }
//
//        return 0;
//    }

    // ************************************************************************
    public int insertActAcctForeign(int idx) throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888 insert acct_forei=[" + idx + "]" + mbosCurrCode);
        if (!pSupFlag.equals("0"))
            return (0);

        /* 外幣 */
        if (mbosCurrCode.length() > 0 && mbosCurrCode.compareTo("901") != 0) {
            hAadkFuncCode = "A";
            hAadkCurrCode = mbosCurrCode;
            hAadkCardNo = mbosCardNo;
            hAadkAutopayAcctBank = "006";
            hAadkAutopayAcctNo = mbosActNoF;

            selectActAcctCurr(mbosCurrCode);
            if (tempInt < 1) {
                insertActAcctCurr(mbosCurrCode);
//                insert_act_acct_dclink();
            } else {
                if (!hAadkAutopayAcctNo.equals(hTtttAutopayAcctNo)) {
//                    insert_act_acct_dclink();

                    hAadkFuncCode = "D";
                    hAadkAutopayAcctNo = hTtttAutopayAcctNo;
//                    insert_act_acct_dclink();
                }
            }
        }
        if (debug == 1) showLogMessage("I", "", "888 insert onbat=[" + mbosCardNo + "]");

        extendField = "onbat_2ccas.";
        setValue(extendField+"trans_type"       , "19");
        setValueInt(extendField+"to_which"      , 2);
        setValue(extendField+"dog"              , sysDate + sysTime);
        setValue(extendField+"proc_mode"        , "B");
        setValueInt(extendField+"proc_status"   , 0);
        setValue(extendField+"card_acct_id"     , getValue("act_no"));
        setValue(extendField+"card_no"          , mbosCardNo);
        setValue(extendField+"trans_date"       , sysDate);
        setValueDouble(extendField+"trans_amt"  , (double) (pAcnoComboCashLimit - tempComboLimit));
        setValue(extendField+"mcc_code"         , "CASH");
        setValue(extendField+"card_valid_from"  , getValue("valid_fm"));
        setValue(extendField+"card_valid_to"    , getValue("valid_to"));
        setValue(extendField+"proc_date"        , sysDate);
        setValue(extendField+"trans_code"       , "03");

        daoTable = "onbat_2ccas ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_onbat_2ccas       error[dupRecord]=";
            String err2 = mbosApplyId;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public int insertActAcctCurr(String idxCurr) throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888 insert acct_curr=[" + mbosCardNo + "]" + idxCurr);

        // autopay_dc_flag 外幣存款不足轉扣台幣註記

        hAcurAutopayDcFlag = "Y";
//        if (comc.getSubString(mbos_agree_l_ind, 0, 1) == "1")
//            h_acur_autopay_dc_flag = "Y";

//        String temp_bank = "";
//        String temp_id = "";
//        String temp_id_code = "";
        pAutopayIndicator = "";
        if (idxCurr.equals("901") == false) {
//            temp_bank = "006";
//            temp_id = p_acct_holder_id;
//            temp_id_code = p_acct_holder_id_code;
            pAutopayIndicator = mbosActNoFInd;
        }
        else
            pAutopayIndicator = mbosActNoLInd;

        extendField = "act_acct_curr.";
        setValue(extendField+"p_seqno", pPSeqno);
        setValue(extendField+"acct_type", pAcctType);
        setValue(extendField+"curr_code", idxCurr);
        setValue(extendField+"card_no", mbosCardNo);
        setValue(extendField+"autopay_indicator", pAutopayIndicator);
        setValue(extendField+"autopay_acct_bank", mbosAutopayAcctBank);
        setValue(extendField+"autopay_acct_no", mbosActNoF);
        if (idxCurr.equals("901"))
            setValue(extendField+"autopay_acct_no", mbosActNoL);
        setValue(extendField+"autopay_id", pAcctHolderId);
        setValue(extendField+"autopay_id_code", pAcctHolderIdCode);
        setValue(extendField+"autopay_dc_flag", hAcurAutopayDcFlag);
        setValue(extendField+"curr_change_accout", mbosCurrChangeAccout);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_user", javaProgram);
        setValue(extendField+"apr_date", sysDate);
        setValue(extendField+"apr_user", javaProgram);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        daoTable = "act_acct_curr ";

        insertTable();

        if (dupRecord.equals("Y")) {
            return (0);
        }

        return 0;
    }

    // ************************************************************************
//    public int insert_act_acct_dclink() throws Exception {
//        if (DEBUG == 1)
//            showLogMessage("I", "", "888 insert dclink=[" + mbos_card_no + "]");
//
//        String h_holder_id = p_acct_holder_id;
//        if (getValue("corp_no").length() > 0)
//            h_holder_id = getValue("corp_no");
//
//        setValue("create_date", sysDate);
//        setValue("create_time", sysTime);
//        setValue("p_seqno", p_p_seqno);
//        setValue("card_no", mbos_card_no);
//        setValue("curr_code", h_aadk_curr_code);
//        setValue("autopay_acct_bank", h_aadk_autopay_acct_bank);
//        setValue("autopay_acct_no", h_aadk_autopay_acct_no);
//        setValue("from_mark", "1");
//        setValue("func_code", h_aadk_func_code);
//        setValue("mod_time", sysDate + sysTime);
//        setValue("mod_pgm", javaProgram);
//
//        daoTable = "act_acct_dclink ";
//
//        insertTable();
//
//        if (dupRecord.equals("Y")) {
//            return (0);
//        }
//
//        return (0);
//    }

    // ************************************************************************
    public int insertCrdIdno(int idx) throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888  insert idno=[" + idx + "]" + hIdnoId);
        if (debug == 1)
            showLogMessage("I", "", "888  insert idno1=[" + hIdnoIdPSeqno + "]" + hIdnoId);

        if (mbosToNcccDate.length() > 0) {
            setValue("crd_idno." + "card_since", mbosToNcccDate);
            setValue("crd_idno." + "annual_date", mbosToNcccDate);
        } else {
            setValue("crd_idno." + "card_since", sysDate);
            setValue("crd_idno." + "annual_date", sysDate);
        }

        setValue("crd_idno." + "id_p_seqno", hIdnoIdPSeqno);
        setValue("crd_idno." + "id_no"     , hIdnoId);
        setValue("crd_idno." + "id_no_code", hIdnoIdCode);
        setValue("crd_idno." + "chi_name", hIdnoChiName);
        setValue("crd_idno." + "staff_flag", hIdnoStaffFlag);
        setValue("crd_idno." + "staff_br_no", "");
        setValue("crd_idno." + "credit_flag", hIdnoCreditFlag);
        setValue("crd_idno." + "comm_flag", hIdnoCommFlag);
        setValue("crd_idno." + "salary_code", hIdnoSalaryCode);
        setValue("crd_idno." + "sex", hIdnoSex);
        setValue("crd_idno." + "birthday", hIdnoBirthday);
        setValue("crd_idno." + "marriage", hIdnoMarriage);
        setValue("crd_idno." + "education", hIdnoEducation);
        setValue("crd_idno." + "student", hIdnoStudent);
        setValue("crd_idno." + "nation", hIdnoNation);
        setValue("crd_idno." + "service_year", hIdnoServiceYear);
        setValueDouble("crd_idno." + "asset_value", hIdnoAssetValue);
        setValue("crd_idno." + "annual_date", hIdnoAnnualDate);
        setValueDouble("crd_idno." + "annual_income", hIdnoAnnualIncome);
        setValue("crd_idno." + "resident_no", "");
        setValue("crd_idno." + "passport_no", "");
        setValue("crd_idno." + "other_cntry_code", hIdnoOtherCntryCode);
        setValue("crd_idno." + "office_area_code1", hIdnoOfficeAreaCode1);
        setValue("crd_idno." + "office_tel_no1", hIdnoOfficeTelNo1);
        setValue("crd_idno." + "office_tel_ext1", hIdnoOfficeTelExt1);
        setValue("crd_idno." + "office_area_code2", hIdnoOfficeAreaCode2);
        setValue("crd_idno." + "office_tel_no2", hIdnoOfficeTelNo2);
        setValue("crd_idno." + "office_tel_ext2", hIdnoOfficeTelExt2);
        setValue("crd_idno." + "home_area_code1", hIdnoHomeAreaCode1);
        setValue("crd_idno." + "home_tel_no1", hIdnoHomeTelNo1);
        setValue("crd_idno." + "home_tel_ext1", hIdnoHomeTelExt1);
        setValue("crd_idno." + "home_area_code2", hIdnoHomeAreaCode2);
        setValue("crd_idno." + "home_tel_no2", hIdnoHomeTelNo2);
        setValue("crd_idno." + "home_tel_ext2", hIdnoHomeTelExt2);
        setValue("crd_idno." + "resident_zip", hIdnoResidentZip);
        setValue("crd_idno." + "resident_addr1", hIdnoResidentAddr1);
        setValue("crd_idno." + "resident_addr2", hIdnoResidentAddr2);
        setValue("crd_idno." + "resident_addr3", hIdnoResidentAddr3);
        setValue("crd_idno." + "resident_addr4", hIdnoResidentAddr4);
        setValue("crd_idno." + "resident_addr5", hIdnoResidentAddr5);
        setValue("crd_idno." + "job_position", hIdnoJobPosition);
        setValue("crd_idno." + "company_name", hIdnoCompanyName);
        setValue("crd_idno." + "business_code", hIdnoBusinessCode);
        setValue("crd_idno." + "cellar_phone", hIdnoCellarPhone);
        setValue("crd_idno." + "fax_no", "");
        setValue("crd_idno." + "e_mail_addr", hIdnoEMailAddr);
        setValue("crd_idno." + "card_since", hIdnoCardSince);
        setValue("crd_idno." + "e_mail_from_mark", hIdnoEMailAddr.length() == 0 ? "" : "A");
        setValue("crd_idno." + "e_mail_chg_date", hIdnoEMailAddr.length() == 0 ? "" : sysDate);
        setValue("crd_idno." + "voice_passwd", hIdnoVoiceNum);
        setValue("crd_idno." + "fst_stmt_cycle", hIdnoFstStmtCycle);
        setValue("crd_idno." + "contactor1_name", hIdnoContactor1Name);
        setValue("crd_idno." + "contactor1_relation", hIdnoContactor1Relation);
        setValue("crd_idno." + "contactor1_area_code", hIdnoContactor1AreaCode);
        setValue("crd_idno." + "contactor1_tel", hIdnoContactor1Tel);
        setValue("crd_idno." + "contactor1_ext", hIdnoContactor1Ext);
        setValue("crd_idno." + "contactor2_name", hIdnoContactor2Name);
        setValue("crd_idno." + "contactor2_relation", hIdnoContactor2Relation);
        setValue("crd_idno." + "contactor2_area_code", hIdnoContactor2AreaCode);
        setValue("crd_idno." + "contactor2_tel", hIdnoContactor2Tel);
        setValue("crd_idno." + "contactor2_ext", hIdnoContactor2Ext);
        setValue("crd_idno." + "est_graduate_month", hIdnoEstGraduateMonth);
        setValue("crd_idno." + "market_agree_base", hIdnoMarketAgreeBase);
        setValue("crd_idno." + "vacation_code", hIdnoVacationCode);
        setValue("crd_idno." + "market_agree_act", hIdnoMarketAgreeAct);
//        setValue("crd_idno." + "e_news"    , "N");
        setValue("crd_idno." + "accept_mbullet"  , "Y");
//        setValue("crd_idno." + "accept_call_sell", "Y");
        setValue("crd_idno." + "dm_from_mark"    , "A");
        setValue("dm_chg_date"     , sysDate);
//        setValue("crd_idno." + "accept_sms"      , "Y");
        setValue("crd_idno." + "spouse_name"     , mbosSpouseName);
        setValue("crd_idno." + "spouse_id_no"    , mbosSpouseIdNo);
        setValue("crd_idno." + "resident_no_expire_date"     , mbosResidentNoExpireDate);
        setValue("crd_idno." + "graduation_elementarty" , mbosGraduationElementarty);
        setValue("crd_idno." + "ur_flag"         , mbosUrFlag);
        setValue("crd_idno." + "business_code"   , mbosBusinessCode);
        setValue("crd_idno." + "inst_flag"       , mbosInstFlag);
        setValue("crd_idno." + "credit_level_new"       , mbosCreditLevelNew);
        setValue("crd_idno." + "credit_level_old"       , mbosCreditLevelNew);
        setValue("crd_idno." + "mail_zip"        , mbosMailZip);
        setValue("crd_idno." + "mail_addr1"      , mbosMailAddr1);
        setValue("crd_idno." + "mail_addr2"      , mbosMailAddr2);
        setValue("crd_idno." + "mail_addr3"      , mbosMailAddr3);
        setValue("crd_idno." + "mail_addr4"      , mbosMailAddr4);
        setValue("crd_idno." + "mail_addr5"      , mbosMailAddr5);
        setValue("crd_idno." + "company_zip"     , mbosCompanyZip);
        setValue("crd_idno." + "company_addr1"   , mbosCompanyAddr1);
        setValue("crd_idno." + "company_addr2"   , mbosCompanyAddr2);
        setValue("crd_idno." + "company_addr3"   , mbosCompanyAddr3);
        setValue("crd_idno." + "company_addr4"   , mbosCompanyAddr4);
        setValue("crd_idno." + "company_addr5"   , mbosCompanyAddr5);
        setValue("crd_idno." + "fee_code_i"      , mbosFeeCodeI);
        setValue("crd_idno." + "market_agree_base" , mbosMarketAgreeBase);
        setValue("crd_idno." + "e_news"          , mbosENews);
        setValue("crd_idno." + "e_news_from_mark", "A");
        setValue("crd_idno." + "e_news_chg_date" , sysDate);

        if(mbosMarketAgreeBase.equals("Y")) {
            setValue("crd_idno." + "accept_call_sell", "N");
        }else{
            setValue("crd_idno." + "accept_call_sell", "Y");
        }

        setValue("crd_idno." + "call_sell_from_mark", "A");
        setValue("crd_idno." + "call_sell_chg_date" , sysDate);

        if(mbosENews.equals("Y")) {
            setValue("crd_idno." + "accept_dm" , "Y");
            setValue("crd_idno." + "accept_sms", "Y");
        }else{
            setValue("crd_idno." + "accept_dm" , "N");
            setValue("crd_idno." + "accept_sms", "N");
        }

        setValue("crd_idno." + "sms_from_mark", "A");
        setValue("crd_idno." + "sms_chg_date" , sysDate);

        if (selectDbcIdno() == 0) {
            setValue("crd_idno." + "e_news"          , getValue("dcio_e_news"));
            setValue("crd_idno." + "accept_mbullet"  , getValue("dcio_accept_mbullet"));
            setValue("crd_idno." + "accept_call_sell", getValue("dcio_accept_call_sell"));
            setValue("crd_idno." + "accept_dm"       , getValue("dcio_accept_dm"));
            setValue("crd_idno." + "dm_from_mark"    , getValue("dcio_dm_from_mark"));
            setValue("crd_idno." + "dm_chg_date"     , getValue("dcio_dm_chg_date"));
            setValue("crd_idno." + "accept_sms"      , getValue("dcio_accept_sms"));
        }
        setValue("crd_idno." + "e_mail_from_mark", "A");
        setValue("crd_idno." + "e_mail_chg_date", sysDate);
        if (getValue("e_mail_addr").length() < 1) {
            setValue("crd_idno." + "e_mail_from_mark", "");
            setValue("crd_idno." + "e_mail_chg_date", sysDate);
        }

        setValueDouble("crd_idno." + "annual_income", getValueDouble("salary"));
        setValueDouble("crd_idno." + "asset_value"  , getValueDouble("value"));
        setValue("crd_idno." + "msg_flag", tmpMsgFlag);
        setValueInt("crd_idno." + "msg_purchase_amt", tmpMsgPurchaseAmt);
        setValue("crd_idno." + "eng_name", mbosEngName);

        setValue("crd_idno." + "crt_date", sysDate);
        setValue("crd_idno." + "crt_user", javaProgram);
        setValue("crd_idno." + "apr_date", sysDate);
        setValue("crd_idno." + "apr_user", javaProgram);
        setValue("crd_idno." + "mod_time", sysDate + sysTime);
        setValue("crd_idno." + "mod_pgm", javaProgram);
        setValueLong("crd_idno." + "mod_seqno", 0);
        extendField = "crd_idno.";
        daoTable = "crd_idno  ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_idno  error[dupRecord]=";
            String err2 = hIdnoId;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }
    // ************************************************************************
    public int moveCardVar() throws Exception {

        hCardCardNo = mbosCardNo;
        if (hCardIndicator.equals("2")) {
            hCardCorpNo = mbosCorpNo;
            hCardCorpNoCode = mbosCorpNoCode;
            hCardCorpPSeqno = hCorpCorpPSeqno;
            hCardCorpActFlag = mbosCorpActFlag;
        }
        hCardCardType = mbosCardType;
        hCardGroupCode = mbosGroupCode;
        hCardSourceCode = mbosSourceCode;
        hCardUnitCode = mbosUnitCode;
        hCardSupFlag = mbosSupFlag;
        hCardCurrentCode = "0";
        hCardMemberId = getValue("member_id");
        hCardEngName = getValue("eng_name");
        hCardRegBankNo = getValue("reg_bank_no");
        hCardPinBlock = getValue("pin_block");
        hCardNewBegDate = getValue("valid_fm");
        hcardnewenddate = getValue("valid_to");
        /* 正附卡 */
        if (mbosApplyId.compareTo(mbosPmId) != 0) {
            hCardMajorIdPSeqno = hMajorIdPSeqno;
            hCardMajorCardNo = hMajorCardNo;
            hCardSetCode = getValue("sup_cash");
        } else {
            hCardMajorIdPSeqno = hIdnoIdPSeqno;
            hCardMajorCardNo = mbosCardNo;
            hCardSetCode = getValue("pm_cash");
        }
        /* V1.44.01 add */
        tmpInt = selectPtrGroupCode();
//        tmpInt = selectPtrSrcCode();
        if (mbosSupFlag.equals("1")) {
            // ** 附卡同正卡登錄狀態 ***
            tmpInt = selectMajorCard();
        }

        /* new card */
        if (mbosToNcccDate.length() > 0)
            hCardIssueDate = mbosToNcccDate;
        else
            hCardIssueDate = sysDate;

        /* new card */
        hCardApplyNo = getValue("apply_no");
        hCardCvv2 = getValue("cvv2");
        hCardPvki = getValue("pvki");
        hCardPinBlock = getValue("pin_block");
        hCardEmbossData = getValue("emboss_4th_data");
        hCardMajorRelation = getValue("rel_with_pm");
        hCardAcctType = getValue("acct_type");
        hCardIntroduceId = getValue("introduce_id");
        hCardIntroduceName = getValue("introduce_name");

        /**** introduce_no promote_emp_no *************/
        if (getValue("introduce_no").length() > 0)
            hCardPromoteEmpNo = getValue("introduce_no");

        /*******************************/
        /* final really fee code value */
        /*******************************/
        hCardFeeCode = getValue("final_fee_code");
        hCardCurrFeeCode = getValue("final_fee_code");
        hCardBatchno = getValue("batchno");
        hCardRecno = mbosRecno;
        hCardMailType = getValue("mail_type");
        hCardMailNo = getValue("mail_no");
        /****************************************
         * combo卡-- son_card_flag,indiv_crd_lmt
         ****************************************/
        hCardComboIndicator = hComboIndicator;
        hCardSonCardFlag = getValue("son_card_flag");
        hCardIndivCrdLmt = getValueInt("indiv_crd_lmt");

        /*** adding ic_flag,branch,mail_attach1,mail_attach2 ***/
        hCardIcFlag = getValue("ic_flag");
        hCardBranch = getValue("branch");
        hCardMailBranch = getValue("mail_branch");
        hCardMailAttach1 = getValue("mail_attach1");
        hCardMailAttach2 = getValue("mail_attach2");
        hCardCurrCode = getValue("curr_code");

        /*** adding bank_actno ****/
        hCardBankActno = getValue("bank_actno");
        hCardFancyLimitFlag = getValue("fancy_limit_flag");
        
        hCardSpecialCardRateFlag = tmpSpecialCardRateFlag;
        hCardSpecialCardRate = mbosSpecialCardRate;
        hCardFlFlag = mbosFlFlag;        
        hCardClerkId = mbosClerkId;
        hCardMsgFlag = "Y";
        hCardMsgPurchaseAmt = 0;		

        return (0);
    }

    // ************************************************************************
    public int insertCrdCard(int idx) throws Exception {

        hCardOppostDate = "";
        hCardOppostReason = "";
        String tmpPaymentNoII = "";

        if (debug == 1)
            showLogMessage("I", "", "888 insert m_card ="+ hCardMajorCardNo +"[" + hCardCardNo + "]" + idx +","+ hMajorCardNo);

//        chk_promote_dept();

        if (hCardMailType.length() == 0)
//            setValue("mail_type", "1");
            hCardMailType = "1";

        if (hCardPinBlock.length() == 0) {
//            setValue("pin_block", h_pin_block);
//            setValue("cvv2", h_cvv2);
//            setValue("pvki", h_pvki);
            hCardPinBlock = hPinBlock;
            hCardCvv2 = hCvv2;
            hCardPvki = hPvki;
        }
        if (mbosOnlineMark.equals("1") || mbosOnlineMark.equals("2"))
//            setValue("emergent_flag", "Y");
            hCardEmergentFlag = "Y";

//        setValue("son_card_flag", "N");        
//        if (h_card_son_card_flag.equals("1"))
//            setValue("son_card_flag", "Y");
        if(hCardSonCardFlag.equals(""));
        hCardSonCardFlag = "N";

        if (debug == 1)
            showLogMessage("I", "", "888 insert old =[" + hCardOldCardNo + "]");
        if (hCardOldCardNo.length() > 0) {
            extendField = "oldc_.";
            selectSQL = "bank_actno,new_beg_date,new_end_date ";
            daoTable = "crd_card            ";
            whereStr = "WHERE card_no     = ? ";

            setString(1, hCardOldCardNo);

            int recCnt = selectTable();

            if (notFound.equals("Y")) {
                String err1 = "select_crd_card 2       error[dupRecord]=";
                String err2 = hCardOldCardNo;
                return (1);
            }
//            setValue("bank_actno", getValue("oldc_.bank_actno"));
//            setValue("new_beg_date", getValue("oldc_.new_beg_date"));
//            setValue("new_end_date", getValue("oldc_.new_end_date"));
            hCardSonCardFlag = getValue("oldc_.bank_actno");
            hCardNewBegDate = getValue("oldc_.new_beg_date");
            hcardnewenddate = getValue("oldc_.new_end_date");
        }

//        if (hCardGroupCode.equals("1599")) {
//            if (hCardCardNo.substring(0, 9).equals("540970999")) {
//                tmpPaymentNoII = "9961" + hCardCardNo.substring(8);
//                hCardPaymentNoII = tmpPaymentNoII;
//            }
//        }
        
        extendField = "crd_card.";
        setValue(extendField+"bin_no"          , hBinNo);
        setValue(extendField+"bin_type"        , hBinType);
        setValue(extendField+"card_note"       , hCardNote);
        setValue(extendField+"card_no"         , hCardCardNo);
        setValue(extendField+"id_p_seqno"      , hCardIdPSeqno);
        setValue(extendField+"corp_p_seqno"    , hCardCorpPSeqno);
        setValue(extendField+"corp_no"         , hCardCorpNo);
        setValue(extendField+"corp_no_code"    , hCardCorpNoCode);
        setValue(extendField+"auto_installment", hCardAutoInstallment);
        setValue(extendField+"card_type"       , hCardCardType);
        setValue(extendField+"urgent_flag"     , hCardUrgentFlag);
        setValue(extendField+"group_code"      , hCardGroupCode);
        setValue(extendField+"source_code"     , hCardSourceCode);
        setValue(extendField+"sup_flag"        , hCardSupFlag);
        setValue(extendField+"major_relation"  , hCardMajorRelation);
        setValue(extendField+"major_id_p_seqno", hCardMajorIdPSeqno);
        if(hCardMajorCardNo.length() == 0)
        {
            if(mbosApplyId.compareTo(mbosPmId) == 0) {
                hCardMajorCardNo = hCardCardNo;
            }
            else
                hCardMajorCardNo = hMajorCardNo;
        }
        setValue(extendField+"major_card_no"   , hCardMajorCardNo);
        setValue(extendField+"member_id"       , hCardMemberId);
        setValue(extendField+"current_code"    , hCardCurrentCode);
        setValue(extendField+"eng_name"        , hCardEngName);
        setValue(extendField+"reg_bank_no"     , hCardRegBankNo);
        setValue(extendField+"pin_block"       , hCardPinBlock);
        setValue(extendField+"unit_code"       , hCardUnitCode);
        setValue(extendField+"old_beg_date"    , hCardOldBegDate);
        setValue(extendField+"old_end_date"    , hCardOldEndDate);
        setValue(extendField+"new_beg_date"    , hCardNewBegDate);
        setValue(extendField+"new_end_date"    , hcardnewenddate);
        setValue(extendField+"issue_date"      , hCardIssueDate);
        setValue(extendField+"reissue_date"    , hCardReissueDate);
        setValue(extendField+"reissue_reason"  , hCardReissueReason);
        setValue(extendField+"change_date"     , hCardChangeDate);
        setValue(extendField+"upgrade_date"    , hCardUpgradeDate);
        setValue(extendField+"apply_no"        , hCardApplyNo);
        setValue(extendField+"promote_dept"    , hCardPromoteDept);
        setValue(extendField+"promote_emp_no"  , hCardPromoteEmpNo);
        setValue(extendField+"introduce_id"    , hCardIntroduceId);
        setValue(extendField+"introduce_emp_no", hCardIntroduceEmpNo);
        setValue(extendField+"introduce_name"  , hCardIntroduceName);
        setValue(extendField+"prod_no"         , hCardProdNo);
        setValueInt(extendField+"indiv_crd_lmt", hCardIndivCrdLmt);
        setValueDouble(extendField+"indiv_inst_lmt", hCardIndivInstLmt);
        setValue(extendField+"pvv", hCardPvv);
        setValue(extendField+"cvv", hCardCvv);
        setValue(extendField+"trans_cvv2", hCardTransCvv2);
        setValue(extendField+"cvv2", hCardCvv2);
        setValue(extendField+"pvki", hCardPvki);
        setValue(extendField+"emboss_data", hCardEmbossData);
        setValue(extendField+"batchno", hCardBatchno);
        setValueDouble(extendField+"recno", hCardRecno);
        setValue(extendField+"oppost_reason", hCardOppostReason);
        setValue(extendField+"oppost_date", hCardOppostDate);
        setValue(extendField+"new_card_no", hCardNewCardNo);
        setValue(extendField+"old_card_no", hCardOldCardNo);
        setValue(extendField+"acct_type"  , hCardAcctType);
        setValue(extendField+"acno_p_seqno", hCardPSeqno);
        setValue(extendField+"p_seqno"     , hCardGpNo);
        setValue(extendField+"stmt_cycle", hCardStmtCycle);
        setValue(extendField+"apply_cht_flag", hCardApplyChtFlag);
        setValue(extendField+"fee_code", hCardFeeCode);
        setValue(extendField+"curr_fee_code", hCardCurrFeeCode);
        setValue(extendField+"expire_reason", hCardExpireReason);
        setValue(extendField+"apply_atm_flag", hCardApplyAtmFlag);
        setValue(extendField+"expire_chg_flag", hCardExpireChgFlag);
        setValue(extendField+"expire_chg_date", hCardExpireChgDate);
        setValue(extendField+"corp_act_flag", hCardCorpActFlag);
        setValue(extendField+"acno_flag", hCardAcnoFlag);
        setValue(extendField+"old_activate_type", hCardOldActivateType);
        setValue(extendField+"old_activate_flag", hCardOldActivateFlag);
        setValue(extendField+"old_activate_date", hCardOldActivateDate);
        setValue(extendField+"activate_type", hCardActivateType);
        setValue(extendField+"activate_flag", hCardActivateFlag);
        setValue(extendField+"activate_date", hCardActivateDate);
        setValue(extendField+"son_card_flag", hCardSonCardFlag);
        setValue(extendField+"emergent_flag", hCardEmergentFlag);
        setValue(extendField+"set_code"     , hCardSetCode);
        setValue(extendField+"mail_type"    , hCardMailType);
        setValue(extendField+"combo_acct_no", hCardComboAcctNo);
        setValueDouble(extendField+"combo_beg_bal", hCardComboBegBal);
        setValueDouble(extendField+"combo_end_bal", hCardComboEndBal);
        setValue(extendField+"combo_indicator", hCardComboIndicator);
        setValue(extendField+"mail_no", hCardMailNo);
        setValue(extendField+"stock_no", hCardStockNo);
        setValue(extendField+"ic_flag", hCardIcFlag);
        setValue(extendField+"mail_branch", hCardMailBranch);
        setValue(extendField+"branch", hCardBranch);
        setValue(extendField+"fancy_limit_flag", hCardFancyLimitFlag);
        setValue(extendField+"old_bank_actno", hCardOldBankActno);
        setValue(extendField+"bank_actno", hCardBankActno);
        setValue(extendField+"mail_attach1", hCardMailAttach1);
        setValue(extendField+"mail_attach2", hCardMailAttach2);
        setValue(extendField+"curr_code", hCardCurrCode);
        setValueDouble(extendField+"jcic_score", hCardJcicScore);
        setValue(extendField+"special_card_rate_flag" , hCardSpecialCardRateFlag);
        setValue(extendField+"special_card_rate" , hCardSpecialCardRate);
        
        if (idx == 3 || idx == 4) {
            hCardCardFeeDate = comc.getSubString(hCardCardFeeDate, 0, 6);
            setValue(extendField+"card_fee_date" , hCardCardFeeDate);           
        }
        else {
            mbosToNcccDate = comc.getSubString(mbosToNcccDate, 0, 6);
            setValue(extendField+"card_fee_date" , mbosToNcccDate);
        }
        setValue(extendField+"fl_flag" , hCardFlFlag);
//        setValue(extendField+"payment_no_ii", hCardPaymentNoII);
        setValue(extendField+"clerk_id", hCardClerkId);
        setValue(extendField+"msg_flag", hCardMsgFlag);
        setValueInt(extendField+"msg_purchase_amt", hCardMsgPurchaseAmt);
        setValue(extendField+"electronic_code", mbosElectronicCode);
        setValue(extendField+"card_ref_num", mbosCardRefNum);

        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_user", javaProgram);
        setValue(extendField+"apr_date", sysDate);
        setValue(extendField+"apr_user", javaProgram);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        daoTable = "crd_card  ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_card         error[dupRecord]=";
            String err2 = mbosApplyId;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        if (swMove.equals("Y")) {
            rptCnt1++;
            if (rptCnt == 1)
                writeHeader2();
            tmpChar1 = comcr.commHiIdno(moveChiName);
            tmpChar2 = comcr.commHiIdno(mbosApplyId);
            writeData2();

            updateMoveElse();
        }

        return (0);
    }

    // ************************************************************************
    public int insertActAcct(int idx) throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888 insert acct =[" + idx + "]" + pSupFlag +","+ pPSeqno);

        if (pSupFlag.equals("1"))
            return (0);

        extendField = "act_acct.";
        setValue(extendField+"p_seqno", pPSeqno);
        setValue(extendField+"acct_type", pAcctType);
//        setValue(extendField+"acct_holder_id", p_acct_holder_id);
//        setValue(extendField+"acct_holder_id_code", p_acct_holder_id_code);
        setValue(extendField+"id_p_seqno", pIdPSeqno);
        setValue(extendField+"stmt_cycle", pStmtCycle);
        setValue(extendField+"corp_no", pCorpNo);
        setValue(extendField+"corp_no_code", pCorpNoCode);
        setValue(extendField+"corp_p_seqno", hCorpCorpPSeqno);
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_user", javaProgram);
        setValue(extendField+"apr_date", sysDate);
        setValue(extendField+"apr_user", javaProgram);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        daoTable = "act_acct  ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_act_acct         error[dupRecord]=";
            String err2 = mbosApplyId;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        if (debug == 1)
            showLogMessage("I", "", String.format(" 888 curr_code =[%s]", mbosCurrCode));

        /* 台幣 (外幣卡都要) */
        hAadkAutopayAcctNo = mbosActNoL;
        hAadkCardNo = mbosCardNo;
//          h_aadk_from_mark            = "1";
        hAadkFuncCode = "A";
        hAadkCurrCode = "901";
        hAadkAutopayAcctBank = "006";

        selectActAcctCurr("901");
        if (tempInt < 1) {
            insertActAcctCurr("901");
            /* insert_act_acct_dclink(); */
        } else {
            if (hAadkAutopayAcctNo.equals(hTtttAutopayAcctNo) == false) {
                hAadkFuncCode = "D";
                hAadkAutopayAcctNo = hTtttAutopayAcctNo;
                updateActAcctCurr();
            }
        }

        insertActAcctForeign(3);
        return (0);
    }

    // ************************************************************************
    void updateActAcctCurr() throws Exception {

        daoTable = "act_acct_curr";
        updateSQL = " autopay_acct_bank = decode(cast(? as varchar(8)) , '', autopay_acct_bank , ? ),";
        updateSQL += " autopay_acct_no   = decode(cast(? as varchar(16)) , '', autopay_acct_no  , ? ),";
        updateSQL += " autopay_indicator = decode(cast(? as varchar(8)) , '', autopay_indicator, ? ),";
        updateSQL += " autopay_id        = decode(cast(? as varchar(20)) , '', autopay_id       , ? ),";
        updateSQL += " autopay_id_code   = decode(cast(? as varchar(8)) , '', autopay_id_code  , ? ),";
        updateSQL += " curr_change_accout   = decode(cast(? as varchar(8)) , '', curr_change_accout  , ? ),";
        updateSQL += " mod_time          = sysdate,";
        updateSQL += " mod_pgm           =  ? ";
        whereStr = "where p_seqno      =  ? ";
        whereStr += "  and curr_code    = '901' ";
        setString(1, mbosAutopayAcctBank);
        setString(2, mbosAutopayAcctBank);
        setString(3, mbosActNoL);
        setString(4, mbosActNoL);
        setString(5, mbosActNoLInd);
        setString(6, mbosActNoLInd);
        setString(7, pAcctHolderId);
        setString(8, pAcctHolderId);
        setString(9, pAcctHolderIdCode);
        setString(10, pAcctHolderIdCode);
        setString(11, mbosCurrChangeAccout);
        setString(12, mbosCurrChangeAccout);
        setString(13, hCknoModPgm);
        setString(14, hCknoPSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_curr not found!", "", hCallBatchSeqno);
        }

    }

    // ************************************************************************
    public int insertActAcno(int idx) throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888 insert acno =[" + pAcctKey + "]" + idx);
        String pNoTelCollFlag = "";

        String hHolderId = pAcctHolderId;
        if (getValue("corp_no").length() > 0)
            hHolderId = getValue("corp_no");

        // 1.一般卡, 2.商務卡總繳(公司), 3.商務卡個繳, Y.商務卡總繳(個人)
        hCardAcnoFlag = "1";
        if (mbosCardIndicator.equals("2")) {
            if (mbosCorpActFlag.equals("Y"))
                hCardAcnoFlag = "Y";
            else
                hCardAcnoFlag = "3";
//                if (p_acct_key.substring(0, 8).compareTo(p_corp_no) == 0 && p_acct_key.substring(8, 11) == "000") {
//                    h_card_acno_flag = "2";
//                }
        }
        if(debug == 1)
            showLogMessage("I", "", "888 corp_p_seqno=["+ hCorpCorpPSeqno +"]"+ pPSeqno + "," + pGpNo);

//        double subPercent = (hPpltPercentC * 1.0) / 100;
//        int pMonthPurchaseLmt = (int) (pCreditAmt * subPercent);
//        if (pMonthPurchaseLmt < hPpltLfixC)
//            pMonthPurchaseLmt = hPpltLfixC;
        if (hCardIndicator.equals("2")) {
            if (pCorpActFlag.equals("Y"))
                pNoTelCollFlag = "Y";
        }

        //Write-off number
//        String pPmId = "";
//
//        sqlCmd  = "select pm_id  ";
//        sqlCmd += "from crd_emboss ";
//        sqlCmd += "where card_no = ? ";
//        setString(1, mbosCardNo);
//        if (selectTable() > 0)
//            pPmId = getValue("pm_id");
//
//        String[] pPaymentNo = new String[16];
//        String idSeqno = pPmId.substring(0, 1).toUpperCase();
//        char idSeqno1 = idSeqno.charAt(0);
//        if (idSeqno1 >= 65 && idSeqno1 <= 90) {
//            pPaymentNo[0] = "8";
//            pPaymentNo[1] = "0";
//            pPaymentNo[2] = "1";
//        }
//        idSeqno = pPmId.substring(8, 10).toUpperCase();
//        idSeqno1 = idSeqno.charAt(0);
//        if (idSeqno1 >= 65 && idSeqno1 <= 90) {
//            idSeqno1 = idSeqno.charAt(1);
//            if (idSeqno1 >= 65 && idSeqno1 <= 90) {
//                pPaymentNo[0] = "8";
//                pPaymentNo[1] = "0";
//                pPaymentNo[2] = "2";
//            }
//        }
//        idSeqno = pPmId.substring(0, 2).toUpperCase();
//        idSeqno1 = idSeqno.charAt(0);
//        if (idSeqno1 >= 65 && idSeqno1 <= 90) {
//            idSeqno1 = idSeqno.charAt(1);
//            if (idSeqno1 >= 65 && idSeqno1 <= 90) {
//                pPaymentNo[0] = "8";
//                pPaymentNo[1] = "0";
//                pPaymentNo[2] = "3";
//            }
//        }
//        pPaymentNo[8]  = pPmId.substring(0, 1);
//        pPaymentNo[9]  = pPmId.substring(1, 2);
//        pPaymentNo[3]  = pPmId.substring(2, 3);
//        pPaymentNo[10] = pPaymentNo[3];
//        pPaymentNo[11] = String.valueOf(9 - Integer.parseInt(pPmId.substring(3, 4)));
//        pPaymentNo[4]  = pPmId.substring(4, 5);
//        pPaymentNo[12] = String.valueOf(9 - Integer.parseInt(pPmId.substring(5, 6)));
//        pPaymentNo[5]  = pPmId.substring(6, 7);
//        pPaymentNo[13] = String.valueOf(9 - Integer.parseInt(pPmId.substring(7, 8)));
//        pPaymentNo[7]  = pPmId.substring(8, 9);
//        pPaymentNo[15] = pPmId.substring(9, 10);
//        pPaymentNo[6]  = String .valueOf((Integer.parseInt(pPaymentNo[3]) + Integer.parseInt(pPaymentNo[4]) + Integer.parseInt(pPaymentNo[5])) % 10);
//        pPaymentNo[14] = String .valueOf((Integer.parseInt(pPaymentNo[11]) + Integer.parseInt(pPaymentNo[12]) + Integer.parseInt(pPaymentNo[13])) % 10);
//        String sumPPaymentNo = "";
//        for (int i = 0 ; i < pPaymentNo.length ; i++)
//            sumPPaymentNo += pPaymentNo[i];

        //ID to virtual account
//        String IdNumber = "";
//        String account = "";
//        String idLetter1 = "";
//        String idLetter2 = "";
//        String idNum = "";
//
//        IdNumber = pPmId;
//
//        if (IdNumber.length() == 10) {
//            idNum = IdNumber.substring(0, 1);
//            char temp1 = idNum.charAt(0);
//            idNum = IdNumber.substring(1, 2);
//            char temp2 = idNum.charAt(0);
//            if (temp1 >= 65 && temp1 <= 90) {
//                if (temp2 < 65) {
//                    idLetter1 = getLetterToNo(String.valueOf(temp1));
//                    idNum = IdNumber.substring(1, 10);
//                    account = "99666" + idLetter1 + idNum;
//                }
//            }
//        }
//
//        if (IdNumber.length() == 10) {
//            idNum = IdNumber.substring(8, 10);
//            char temp1 = idNum.charAt(0);
//            char temp2 = idNum.charAt(1);
//            if (temp1 >= 65 && temp1 <= 90) {
//                if (temp2 >= 65 && temp2 <= 90) {
//                    idLetter1 = getLetterToNo(String.valueOf(temp1));
//                    idLetter2 = getLetterToNo(String.valueOf(temp2));
//                    idNum = IdNumber.substring(0, 8);
//                    account = "9965" + idNum + idLetter1 + idLetter2;
//                }
//            }
//        }
//
//        if (IdNumber.length() == 10) {
//            idNum = IdNumber.substring(0, 2);
//            char temp1 = idNum.charAt(0);
//            char temp2 = idNum.charAt(1);
//            if (temp1 >= 65 && temp1 <= 90) {
//                if (temp2 >= 65 && temp2 <= 90) {
//                    idNum = IdNumber.substring(9, 10);
//                    idLetter1 = getLetterToNo(String.valueOf(temp1));
//                    idLetter2 = getLetterToNo(String.valueOf(temp2));
//                    idNum = IdNumber.substring(2, 10);
//                    account = "9967" + idLetter1 + idLetter2  + idNum  ;
//                }
//            }
//        }
//
//        if (IdNumber.length() == 8) {
//            account = "99666000" + IdNumber;
//        }
//
//        if (IdNumber.length() == 10) {
//            idNum = IdNumber.substring(9, 10);
//            char temp1 = idNum.charAt(0);
//            idNum = IdNumber.substring(8, 9);
//            char temp2 = idNum.charAt(0);
//            if (temp1 >= 65 && temp1 <= 90) {
//                if (temp2 < 65) {
//                    idLetter1 = getLetterToNo(String.valueOf(temp1));
//                    idNum = IdNumber.substring(0, 9);
//                    account = "99667" + idNum + idLetter1;
//                }
//            }
//        }
//
//        //Card no. to virtual account
//        String CardNumber = "";
//
//        selectSQL = "CARD_NO  ";
//        daoTable  = "CRD_EMBOSS";
//        whereStr  = "where CARD_NO = ? ";
//        setString(1, mbosCardNo);
//        if (selectTable() > 0)
//            CardNumber = getValue("CARD_NO");
//
//        if (CardNumber.substring(0, 6).equals("540520")) {
//            if (Integer.parseInt((CardNumber.substring(7, 9))) >= 30 && Integer.parseInt((CardNumber.substring(7, 9))) <= 39) {
//                account = "9960" + CardNumber.substring(8);
//            }
//            else if (Integer.parseInt((CardNumber.substring(7, 9))) >= 20 && Integer.parseInt((CardNumber.substring(7, 9))) <= 29) {
//                account = "9962" + CardNumber.substring(8);
//            }
//            else if (Integer.parseInt((CardNumber.substring(7, 9))) >= 10 && Integer.parseInt((CardNumber.substring(7, 9))) <= 19) {
//                account = "9963" + CardNumber.substring(8);
//            }
//            else if (Integer.parseInt((CardNumber.substring(7, 9))) >= 40 && Integer.parseInt((CardNumber.substring(7, 9))) <= 44) {
//                account = "9956" + CardNumber.substring(8);
//            }
//            else {
//                account = "9964" + CardNumber.substring(8);
//            }
//
//        }
//
//        if (CardNumber.substring(0, 6).equals("540970")) {
//            if (CardNumber.substring(6, 9).equals("999")) {
//                account = "9961" + CardNumber.substring(8);
//            }
//            else {
//                account = "9958" + CardNumber.substring(8);
//            }
//        }
//
//        if (CardNumber.substring(0, 6).equals("486605")) {
//            if (Integer.parseInt((CardNumber.substring(7, 8))) >= 0 && Integer.parseInt((CardNumber.substring(7, 8))) <= 9) {
//                account = "9959" + CardNumber.substring(8);
//            }
//        }
//
//        if (CardNumber.substring(0, 6).equals("405430")) {
//            account = "9957" + CardNumber.substring(8);
//        }

        if (hFancyLimitFlag.equals("Y"))
            tempComboLimit = 0;
        else
            tempComboLimit = pComboCashLimit;

        if (hComboIndicator.compareTo("N") != 0)
            pComboCashLimit = (int) (pCreditAmt * pCashRate);
        
        if (mbosCurrCode.length() > 0 && mbosCurrCode.compareTo("901") != 0) {
        	hFCurrencyFlag = "Y"; 
        }
        else {
        	hFCurrencyFlag = "N"; 
        }

        extendField ="act_acno.";
        setValue(extendField+"crt_date"    , sysDate);
        setValue(extendField+"update_date" , sysDate);
        setValue(extendField+"update_user" , javaProgram);
        setValue(extendField+"acno_p_seqno", pPSeqno);
        setValue(extendField+"p_seqno"     , pGpNo);
        setValue(extendField+"acct_type"   , pAcctType);
        setValue(extendField+"acct_key"    , pAcctKey);
        setValue(extendField+"reg_bank_no" , "");
        setValue(extendField+"risk_bank_no", "");
        setValue(extendField+"acct_status" , "1");
        setValue(extendField+"stmt_cycle"     , pStmtCycle);
        setValue(extendField+"id_p_seqno"     , pIdPSeqno);
        setValue(extendField+"corp_p_seqno", hCorpCorpPSeqno);
        setValue(extendField+"card_indicator", hCardIndicator);
        setValue(extendField+"combo_indicator", hComboIndicator);
        setValue(extendField+"f_currency_flag", hFCurrencyFlag);
        setValueInt(extendField+"line_of_credit_amt", pCreditAmt);
        setValueInt(extendField+"line_of_credit_amt_cash", hTempLineOfCreditAmtCash);
//        setValueInt(extendField+"month_purchase_lmt", pMonthPurchaseLmt);
        setValue(extendField+"rc_use_b_adj", "1");
        setValue(extendField+"rc_use_indicator", hRcUseFlag);
        setValue(extendField+"vip_code", pVipCode);
        setValue(extendField+"class_code", pClassCode);
//        setValue(extendField+"accept_dm", p_accept_dm);
        setValue(extendField+"corp_assure_flag", pCorpAssureFlag);
        setValue(extendField+"corp_act_flag", pCorpActFlag);
        setValue(extendField+"autopay_indicator", mbosActNoLInd);
        setValue(extendField+"worse_mcode", "0");
        setValue(extendField+"legal_delay_code", "9");
        switch (mbosBillApplyFlag) {
            case "1":
                setValue(extendField+"bill_sending_zip", mbosResidentZip);
                setValue(extendField+"bill_sending_addr1", mbosResidentAddr1);
                setValue(extendField+"bill_sending_addr2", mbosResidentAddr2);
                setValue(extendField+"bill_sending_addr3", mbosResidentAddr3);
                setValue(extendField+"bill_sending_addr4", mbosResidentAddr4);
                setValue(extendField+"bill_sending_addr5", mbosResidentAddr5);
                break;
            case "2":
                setValue(extendField+"bill_sending_zip", mbosMailZip);
                setValue(extendField+"bill_sending_addr1", mbosMailAddr1);
                setValue(extendField+"bill_sending_addr2", mbosMailAddr2);
                setValue(extendField+"bill_sending_addr3", mbosMailAddr3);
                setValue(extendField+"bill_sending_addr4", mbosMailAddr4);
                setValue(extendField+"bill_sending_addr5", mbosMailAddr5);
                break;
            case "3":
                setValue(extendField+"bill_sending_zip", mbosCompanyZip);
                setValue(extendField+"bill_sending_addr1", mbosCompanyAddr1);
                setValue(extendField+"bill_sending_addr2", mbosCompanyAddr2);
                setValue(extendField+"bill_sending_addr3", mbosCompanyAddr3);
                setValue(extendField+"bill_sending_addr4", mbosCompanyAddr4);
                setValue(extendField+"bill_sending_addr5", mbosCompanyAddr5);
                break;
//            case "4":
//                setValue(extendField+"bill_sending_zip", hCorpCommZip);
//                setValue(extendField+"bill_sending_addr1", hCorpCommAddr1);
//                setValue(extendField+"bill_sending_addr2", hCorpCommAddr2);
//                setValue(extendField+"bill_sending_addr3", hCorpCommAddr3);
//                setValue(extendField+"bill_sending_addr4", hCorpCommAddr4);
//                setValue(extendField+"bill_sending_addr5", hCorpCommAddr5);
//                break;
//            case "5":
//                setValue(extendField+"bill_sending_zip", hCorpRegZip);
//                setValue(extendField+"bill_sending_addr1", hCorpRegAddr1);
//                setValue(extendField+"bill_sending_addr2", hCorpRegAddr2);
//                setValue(extendField+"bill_sending_addr3", hCorpRegAddr3);
//                setValue(extendField+"bill_sending_addr4", hCorpRegAddr4);
//                setValue(extendField+"bill_sending_addr5", hCorpRegAddr5);
//                break;
        }
        setValue(extendField+"no_tel_coll_flag", pNoTelCollFlag);
        setValueInt(extendField+"inst_auth_loc_amt", (int) pInstAuthLocAmt);
        setValue(extendField+"stat_send_paper", "Y");
        setValue(extendField+"paper_upd_user", javaProgram);
        setValue(extendField+"paper_upd_date", sysDate);
        setValue(extendField+"special_stat_code", "5");
        setValue(extendField+"online_update_date", sysDate);
        setValue(extendField+"combo_acct_no", pComboAcctNo);
        setValueInt(extendField+"combo_cash_limit", (int) tempComboLimit);
//        if (mbosCardIndicator.equals("1"))
//            setValue(extendField+"payment_no", sumPPaymentNo);
//        else
//            setValue(extendField+"payment_no", "");
        setValue(extendField+"new_acct_flag", swNew);
        setValue(extendField+"acno_flag", hCardAcnoFlag);
        setValueDouble(extendField+"revolve_int_rate", mbosRevolveIntRate);
        
        if (mbosStatSendInternet.equals("Y")) {
            setValue(extendField+"e_mail_ebill", mbosEMailAddr);
            setValue(extendField+"e_mail_ebill_date", sysDate);
        }
        else {
            setValue(extendField+"e_mail_ebill", "");
            setValue(extendField+"e_mail_ebill_date", "");
        }

        if (mbosFlFlag.equals("Y")) {
            setValue(extendField+"autopay_acct_no_fl", mbosActNoL);
            setValue(extendField+"autopay_acct_bank_fl", mbosAutopayAcctBank);
            setValue(extendField+"autopay_acct_no", "");
            setValue(extendField+"autopay_acct_bank", "");
        }
        else {
            setValue(extendField+"autopay_acct_no_fl", "");
            setValue(extendField+"autopay_acct_bank_fl", "");
            setValue(extendField+"autopay_acct_no", mbosActNoL);                   
            setValue(extendField+"autopay_acct_bank", mbosAutopayAcctBank);
        }
        setValue(extendField+"fl_flag", mbosFlFlag);
        setValue(extendField+"bill_apply_flag", mbosBillApplyFlag);
        if (!mbosActNoL.equals("")) {
            setValue(extendField+"autopay_acct_s_date", sysDate);
            setValue(extendField+"autopay_id", pAcctHolderId);
            setValue(extendField+"autopay_id_code", pAcctHolderIdCode);
        }
        else {
            setValue(extendField+"autopay_acct_s_date", "");
            setValue(extendField+"autopay_id_code", "");
            setValue(extendField+"autopay_acct_e_date", "");
        }
        
//        setValue(extendField+"payment_no_ii", account);
        
        setValue(extendField+"revolve_int_sign", "-");
        setValue(extendField+"revolve_rate_s_month", comc.getSubString(sysDate,0,6));
        setValueDouble(extendField+"rcrate_year", mbosRevolveIntRateYear);
                
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_user", javaProgram);
        setValue(extendField+"apr_date", sysDate);
        setValue(extendField+"apr_user", javaProgram);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        daoTable = "act_acno  ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_act_acno          error[dupRecord]="+ pPSeqno;
            String err2 = mbosApplyId;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
        if (debug == 1)
            showLogMessage("I", "", "999 insert acno =[" + pAcctKey + "]" + totalCnt);

        return (0);
    }

    // ************************************************************************
    public String getLetterToNo(String temp) throws Exception {
        String idLetter = "";
        for (int i = 1 ; i <= 26 ; i++) {
            String tempNum = String.valueOf((char)(i + 64));
            if (String.valueOf(i).length() < 2)
                idLetter = String.format("%02d", i);
            else
                idLetter = String.valueOf(i);
            if (tempNum.equals(temp))
                break;
        }
        return idLetter;
    }

    // ************************************************************************
    public int insertOnbat() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888 insert onbat=[" + mbosCardNo + "]");

        extendField = "onbat2.";
        setValue(extendField+"trans_type"       , "19");
        setValueInt(extendField+"to_which"      , 2);
        setValue(extendField+"dog"              , sysDate + sysTime);
        setValue(extendField+"proc_mode"        , "B");
        setValueInt(extendField+"proc_status"   , 0);
        setValue(extendField+"card_acct_id"     , getValue("act_no"));
        setValue(extendField+"card_no"          , mbosCardNo);
        setValue(extendField+"trans_date"       , sysDate);
        setValueDouble(extendField+"trans_amt"  , (double) (pAcnoComboCashLimit - tempComboLimit));
        setValue(extendField+"mcc_code"         , "CASH");
        setValue(extendField+"card_valid_from"  , getValue("valid_fm"));
        setValue(extendField+"card_valid_to"    , getValue("valid_to"));
        setValue(extendField+"proc_date"        , sysDate);
        setValue(extendField+"trans_code"       , "03");

        daoTable = "onbat_2ccas ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_onbat_2ccas       error[dupRecord]=";
            String err2 = mbosApplyId;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return 0;
    }

    // *************************************************************************
    // master and detail */
    // 參數 : 1: DB內之指撥額度 2: 差額額度 3: 借(1) or 貸(2) ****
    // 新製卡 -- 借 ****
    // 舊卡戶 -- 貸 ****
    // *************************************************************************
    public int insertComboJrnl(int cashAmt, int dCashAmt, int kind) throws Exception {
        int pComboSeqno = 0;

        selectSQL = "seq_no            , cash_use_balance, "
                + "rowid    as jrnl_rowid   ";
        daoTable  = "act_combo_m_jrnl    ";
        whereStr  = "WHERE p_seqno     = ? " + "fetch first 1 row only";

        setString(1, pPSeqno);

        int recCnt = selectTable();

        double tempCashUseBalance = getValueDouble("cash_use_balance");

        if (notFound.equals("Y")) // 新卡戶
        {
            if (hFancyLimitFlag.equals("Y"))
                tempComboLimit = 0;
            else
                tempComboLimit = cashAmt;
        } else {
            if (hFancyLimitFlag.equals("Y")) {
                if (tempCashUseBalance == 0)
                    tempComboLimit = 0;
                else
                    tempComboLimit = tempCashUseBalance;
            } else if (getValueDouble("cash_use_balance") > cashAmt)
                tempComboLimit = getValueDouble("cash_use_balance");
            else
                tempComboLimit = cashAmt;
        }
        pComboSeqno = getValueInt("seq_no");

        if (notFound.equals("Y")) // 新卡戶
        {
            if (hFancyLimitFlag.equals("Y"))
                tempComboLimit = 0;
            else
                tempComboLimit = cashAmt;

            pComboSeqno = 1;
            tmpInt = insertComboMJrnl(pComboSeqno);
            // **** insert detail ****
            if (insertComboDJrnl(dCashAmt, kind, pComboSeqno) != 0)
                return (1);
        } else {
            // *** insert detail ****
            pComboSeqno++;
            if (insertComboDJrnl(dCashAmt, kind, pComboSeqno) != 0)
                return (1);
            // *** update master ***
            updateSQL = "seq_no          =  ? , " + "cash_lmt_balance=  ? , "
                    + "mod_pgm         =  ? , "
                    + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "act_combo_m_jrnl ";
            whereStr  = "where rowid         = ? ";

            setInt(1, pComboSeqno);
            setDouble(2, tempComboLimit);
            setString(3, javaProgram);
            setString(4, sysDate + sysTime);
            setRowId( 5, getValue("jrnl_rowid"));

            updateTable();

            if (notFound.equals("Y")) {
                String err1 = "update_act_combo_m_jrnl  error[dupRecord]=";
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }
        }

        return 0;
    }

    // ************************************************************************
    public int insertComboMJrnl(int tSeqno) throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888 insert ,jr=[" + tSeqno + "]");
        extendField = "act_combo_m_jrnl.";
        setValue(extendField+"acct_type" , pAcctType);
        setValue(extendField+"id_p_seqno", pIdPSeqno);
        setValue(extendField+"p_seqno"   , pPSeqno);
        setValue(extendField+"acct_month", sysDate);
        setValueInt(extendField+"seq_no" , tSeqno);
        setValueDouble(extendField+"cash_lmt_balance", tempComboLimit);
        setValue(extendField+"acct_no"   , pComboAcctNo);
        setValue(extendField+"mod_time"  , sysDate + sysTime);
        setValue(extendField+"mod_pgm"   , javaProgram);

        daoTable = "act_combo_m_jrnl ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_act_combo_m_jrnl  error[dupRecord],p_seqno=" + pPSeqno;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public int insertComboDJrnl(int jCashAmt, int kind, int tSeqno) throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888 insert djr=[" + jCashAmt + "]" + kind);
        int pDrAmt = 0;
        int pCrAmt = 0;

        // *** 新製卡 ***
        if (kind == 1) {
            pDrAmt = 0;
            pCrAmt = 0;
        }
        // *** 差額 ***
        if (kind == 2) {
            pDrAmt = pAcnoComboCashLimit - jCashAmt;
            pCrAmt = 0;
        }

        extendField = "act_combo_jrnl.";
        setValue(extendField+"acct_type" , pAcctType);
        setValue(extendField+"id_p_seqno", pIdPSeqno);
        setValue(extendField+"p_seqno"   , pPSeqno);
        setValueInt(extendField+"seq_no" , tSeqno);
        setValue(extendField+"card_no"   , pCardNo);
        setValue(extendField+"acct_no"   , pComboAcctNo);
        setValue(extendField+"tran_class", "0");
        setValue(extendField+"acct_date" , sysDate);
        setValue(extendField+"tran_date" , sysDate);
        setValueDouble(extendField+"dr_amt", pDrAmt);
        setValueDouble(extendField+"cr_amt", pCrAmt);
        setValueDouble(extendField+"cash_lmt_balance", tempComboLimit);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm" , javaProgram);

        daoTable = "act_combo_jrnl ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_act_combo_jrnl  error[dupRecord]=" + pPSeqno;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
        return 0;
    }

    // ************************************************************************
    public int insertRskAcnolog() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888 insert rsk=[" + pCreditAmt + "]" + pAcnoCreditAmt);
        String pAdjLocFlag = "";

        if (pCreditAmt > pAcnoCreditAmt)
            pAdjLocFlag = "1";
        else
            pAdjLocFlag = "2";

        extendField = "rsk_acnolog.";
        setValue(extendField+"kind_flag", "A");
        setValue(extendField+"card_no", pCardNo);
        setValue(extendField+"acno_p_seqno", pPSeqno);
        setValue(extendField+"acct_type", pAcctType);
        setValue(extendField+"id_p_seqno", pIdPSeqno);
        setValue(extendField+"corp_p_seqno", hCorpCorpPSeqno);
        setValue(extendField+"log_date", sysDate);
        setValue(extendField+"log_mode", "2"); // batch
        setValue(extendField+"log_type", "1"); // 額度調整
        setValue(extendField+"log_reason", "H");
        setValueDouble(extendField+"bef_loc_amt", (double) pAcnoCreditAmt);
        setValueDouble(extendField+"aft_loc_amt", (double) pCreditAmt);
        setValue(extendField+"adj_loc_flag", pAdjLocFlag);
        setValue(extendField+"fit_cond", "N");
        setValueDouble(extendField+"bef_loc_cash", (double) pAcnoCreditAmtCash);
        setValueDouble(extendField+"aft_loc_cash", (double) hTempLineOfCreditAmtCash);
        setValue(extendField+"apr_flag", "Y");
        setValue(extendField+"apr_user", javaProgram);
        setValue(extendField+"apr_date", sysDate);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm" , javaProgram);

        daoTable = "rsk_acnolog ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_rsk_acnolog   error[dupRecord]=";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public int checkIdno() throws Exception {
        hIdnoAcceptCallSell = "";

        extendField = "idno.";
        selectSQL   = "id_p_seqno           ,id_no_code      , "
                + "chi_name             ,fst_stmt_cycle  , "
                + "accept_call_sell     ,rowid    as rowid ";
        daoTable    = "crd_idno             ";
        whereStr    = "WHERE id_no      = ? "
                + "  and birthday   = ? " + "fetch first 1 row only";

        setString(1, mbosApplyId);
        setString(2, mbosBirthday);

        selectTable();

        if (notFound.equals("Y")) {
            return (1);
        }

        mbosApplyIdCode = getValue("idno.id_no_code");
        hIdnoIdPSeqno = getValue("idno.id_p_seqno");
        hIdnoChiName = getValue("idno.chi_name");
        hFstStmtCycle = getValue("idno.fst_stmt_cycle");
        hIdRowid = getValue("idno.rowid");
        hIdnoAcceptCallSell = getValue("accept_call_sell");

        return 0;
    }

    // ************************************************************************
    public int selectDbcIdno() throws Exception {
        extendField = "dcio_";
        selectSQL   = "e_news   , accept_mbullet, accept_call_sell, "
                + "accept_dm, dm_from_mark  , dm_chg_date     , accept_sms , "
                + "rowid    as rowid    ";
        daoTable    = "dbc_idno             ";
        whereStr    = "WHERE id_no      = ? "
                + "  and id_no_code = ? "
                + "fetch first 1 row only";

        setString(1, mbosApplyId);
        setString(2, mbosApplyIdCode);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (1);
        }

        return 0;
    }
    // ************************************************************************
    public int checkAcno() throws Exception
    {
        extendField = "acno.";
        selectSQL   = "acno_p_seqno        ,p_seqno      , acct_key   ,"
                + "risk_bank_no   ,reg_bank_no      , "
                + "class_code     ,vip_code   , "
                + "line_of_credit_amt   ,line_of_credit_amt_cash, "
                + "combo_acct_no  ,combo_cash_limit , "
                + "decode(stat_send_internet,'Y', decode(stat_send_e_month2, '',decode(decode(stat_send_paper,'','N',stat_send_paper),'N','Y', "
                + "        decode(sign(decode(stat_send_e_month,'','999912',stat_send_e_month)-?),-1,'Y','N')),'N'),'N') as stat_send_internet, "
                + "stmt_cycle     ,credit_act_no    , "
                + "stop_status    ,autopay_acct_bank, "
                + "autopay_acct_no      ,autopay_id       , "
                + "decode(new_acct_flag      ,'','Y',new_acct_flag)     as new_acct_flag, "
                + "decode(autopay_indicator  ,'','1',autopay_indicator) as autopay_indicator, "
                + "decode(autopay_acct_e_date,'','29991231', autopay_acct_e_date) as autopay_acct_e_date, "
                + "acno_flag,   "
                + "rowid    as rowid    ";
        daoTable = "act_acno      ";
        whereStr = "WHERE acct_type   = ? " + "  and acct_key    = ? ";

        setString(1, hTempThisAcctMonth);
        setString(2, mbosAcctType);
        setString(3, pAcctKey);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (1);
        }

        pPSeqno = getValue("acno.acno_p_seqno");
        pGpNo = getValue("acno.p_seqno");
        pOrgRiskBankNo = getValue("acno.risk_bank_no");
        pAcnoRegBankNo = getValue("acno.reg_bank_no");
        pAcnoClassCode = getValue("acno.class_code");
        pAcnoVipCode = getValue("acno.vip_code");
        pAcnoCreditAmt = getValueInt("acno.line_of_credit_amt");
        pAcnoCreditAmtCash = getValueInt("acno.line_of_credit_amt_cash");
        pAcnoComboAcctNo = getValue("acno.combo_acct_no");
        pAcnoComboCashLimit = getValueInt("acno.combo_cash_limit");
        pAcnoStatSendInternet = getValue("acno.stat_send_internet");
        pAcnoStmtCycle = getValue("acno.stmt_cycle");
        pAcnoCreditActNo = getValue("acno.credit_act_no");
        pAcnoStopStatus = getValue("acno.stop_status");
        hAcnoAutopayAcctBank = getValue("acno.autopay_acct_bank");
        hAcnoAutopayAcctNo = getValue("acno.autopay_acct_no");
        hAcnoAutopayId = getValue("acno.autopay_id");
        pNewAcctFlag = getValue("acno.new_acct_flag");
        hAcnoAutopayIndicator = getValue("acno.autopay_indicator");
        hAcnoAutopayAcctEDate = getValue("acno.autopay_acct_e_date");
        hCardAcnoFlag = getValue("acno.acno_flag");
        pAcnoRowid = getValue("acno.rowid");

        /* 1.RECS-1040129-001 雙幣卡-autopay_acct_e_dat */
        /* 2.帳戶自動扣繳帳號生效迄日小於今天營業日,就將帳戶自動扣繳帳號清除 */
        /* 3.解決自動扣繳帳號失效,而新申請時並未將新的自動扣繳帳號寫入ACT_CHKNO */

        if (hAcnoAutopayAcctEDate.compareTo(hBusiBusinessDate) < 0)
            hAcnoAutopayAcctNo = "";

        if (pStmtCycle.length() == 0)
            pStmtCycle = pAcnoStmtCycle;

        return 0;
    }
    // ************************************************************************
    public int computeLocAmtcash() throws Exception {

        if (debug == 1)
            showLogMessage("I", "", "  888 loc_amtcash= [" + swNew + "]");
        if (swNew.equals("Y")) {
            rtnCode = checkCrdCardId();
        }

        hTempLineOfCreditAmtCash = pCreditAmt * paccCashadvLocRate / 100;
        if (swNew.equals("N"))
            hTempLineOfCreditAmtCash = pCreditAmt * paccCashadvLocRateOld / 100;

        if (hTempLineOfCreditAmtCash > paccCashadvLocMaxamt)
            hTempLineOfCreditAmtCash = paccCashadvLocMaxamt;

        return 0;
    }

    // ************************************************************************
    public int selectActAcctCurr(String idxCurr) throws Exception {
        hTtttAutopayAcctNo = "";

        tempInt = 0;
        selectSQL = "autopay_acct_no , 1 as cnt_int   ";
        daoTable = "act_acct_curr       ";
        whereStr = "WHERE p_seqno    =  ? " + "  and curr_code  =  ? ";

        setString(1, pPSeqno);
        setString(2, idxCurr);

        int recCnt = selectTable();
        if (debug == 1)
            showLogMessage("I", "", "888 select curr cnt=[" + recCnt + "]" + pPSeqno +","+idxCurr);

        if (recCnt > 0)
        {
            tempInt = getValueInt("cnt_int");
            hTtttAutopayAcctNo = getValue("autopay_acct_no");
        }

        return (0);
    }

    // ************************************************************************
//    public int selectPtrPurLmt() throws Exception {
//        selectSQL = "percent_c, lfix_c  ";
//        daoTable  = "ptr_pur_lmt         ";
//        whereStr  = "WHERE acct_type   = ? ";
//
//        setString(1, mbosAcctType);
//
//        int recCnt = selectTable();
//
//        if (notFound.equals("Y")) {
//            String err1 = "select_ptr_pur_lmt   error[dupRecord]=" + mbosAcctType;
//            String err2 = "";
//            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
//        }
//
//        hPpltPercentC = getValueInt("percent_c");
//        hPpltLfixC = getValueInt("lfix_c");
//
//        return (0);
//    }

    // ************************************************************************
    public int getClassCode() throws Exception {
        selectSQL = "class_code         ";
        daoTable  = "ptr_class          ";
        whereStr  = "WHERE ? between beg_credit_lmt " + "            and end_credit_lmt ";

        setInt(1, pCreditAmt);

        selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_class     error=" + pCreditAmt;
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        pClassCode = getValue("class_code");

        return (0);
    }

    // ************************************************************************
    public int getIndivAcctKey() throws Exception {
        extendField = "indiv.";
        selectSQL = "acct_key           ,acno_p_seqno  , p_seqno , "
                + "reg_bank_no        ,vip_code , line_of_credit_amt , "
                + "line_of_credit_amt ,corp_act_flag,class_code , "
                + "decode(stat_send_internet,'Y', decode(stat_send_e_month2, '',decode(decode(stat_send_paper,'','N',stat_send_paper),'N','Y', "
                + "        decode(sign(decode(stat_send_e_month,'','999912',stat_send_e_month)-?),-1,'Y','N')),'N'),'N') as stat_send_internet, "

                + "combo_acct_no      ,combo_cash_limit, rowid  as rowid";
        daoTable  = "act_acno           ";
        whereStr  = "WHERE acct_type    = ? "
                + "  and id_p_seqno   = ? "
                + "  and corp_p_seqno = ? "
                + "  and decode(corp_act_flag,'','N',corp_act_flag) = ? ";

        setString(1, hTempThisAcctMonth);
        setString(2, pAcctType);
        setString(3, pIdPSeqno);
        setString(4, hCorpCorpPSeqno);
        setString(5, pCorpActFlag);

        int recCnt = selectTable();

        if (!notFound.equals("Y")) {
            pAcnoAcctKey = getValue("indiv.acct_key");
            pAcnoPSeqno = getValue("indiv.acno_p_seqno");
            pAcnoGpNo = getValue("indiv.p_seqno");
            pAcnoRegBankNo = getValue("indiv.reg_bank_no");
            pAcnoVipCode = getValue("indiv.vip_code");
            pAcnoCreditAmt = getValueInt("indiv.credit_amt");
            pAcnoCreditAmtCash = getValueInt("indiv.credit_amt_cash");
            pAcnoCorpActFlag = getValue("indiv.corp_act_flag");
            pAcnoStatSendInternet = getValue("indiv.stat_send_internet");
            pAcnoComboAcctNo = getValue("indiv.combo_acct_no");
            pAcnoComboCashLimit = getValueInt("indiv.combo_cash_limit");
            pAcnoClassCode = getValue("indiv.class_code");
            pAcnoRowid = getValue("indiv.rowid");
            if(debug == 1) showLogMessage("I", "", " 999 777 acno=["+ pAcnoRowid +"]"+ pAcnoGpNo);

            return (0);
        } else {
            selectSQL = "substr(max(acct_key),9,3) as max_key ";
            daoTable  = "act_acno            ";
            whereStr  = "WHERE acct_type   = ? " + "  and acct_key    like ? ";

            setString(1, pAcctType);
            setString(2, pCorpNo + "%");

            tmpInt = selectTable();

            tmpChar = getValue("max_key");
            if (debug == 1)
                showLogMessage("I", "", " 999 777 key ="+totalCnt+"["+ tmpChar +"]"+ pCorpNo);
        }
//        int p_corp_crd_seqno = Integer.parseInt(tmp_char) + 1;
//        p_acno_acct_key = p_corp_no + String.format("%03d", p_corp_crd_seqno);
        if(mbosCorpActFlag.equals("Y")) {
            pAcnoAcctKey = pCorpNo;
        }
        else {        
        	//20221216商務卡(個人)的ACCT_KEY先放空白，後面取P_SEQNO時再塞入
            pAcnoAcctKey = "";  
        }

        if (debug == 1)
            showLogMessage("I", "", " 999 888 acct_key =["+ pAcnoAcctKey +"]");

        return (1);
    }

    // ************************************************************************
    public int existedIdno() throws Exception {
        selectSQL = "1  as h_cnt ";
        daoTable  = "crd_idno             ";
        whereStr  = "WHERE id_no      = ? "
                + "  and id_no_code = ? ";

        setString(1, mbosApplyId);
        setString(2, mbosApplyIdCode);

        int recCnt = selectTable();

        if (!notFound.equals("Y")) // found
        {
            return (1);
        }


        return (0);
    }

    // ************************************************************************
    public int getIdPSeqno() throws Exception {

        sqlCmd  = "select id_p_seqno";
        sqlCmd += " from CRD_IDNO_SEQNO";
        sqlCmd += " where id_no = ?";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, mbosApplyId);
        if (selectTable() > 0)
            hIdnoIdPSeqno = getValue("id_p_seqno");
        else {
            selectSQL = " substr(to_char(ecs_acno.nextval,'0000000000'), 2,10) as temp_x10 ";
            daoTable  = " SYSIBM.DUAL         ";
            tmpInt = selectTable();
            hIdnoIdPSeqno = getValue("temp_x10");

            if (notFound.equals("Y")) {
                String err1 = "select_ecs_acno      error[notFind]";
                String err2 = "";
                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            }
            insertCrdIdnoSeqno();
        }

        if (debug == 1)
            showLogMessage("I", "", "  888 idno id_p_seq[" + hIdnoIdPSeqno + "]");

        return (0);
    }

    // ************************************************************************
    public int getCorpData() throws Exception {

        if (debug == 1)
            showLogMessage("I", "", "  888 corp acct_key[" + mbosCorpNo + "]" + mbosAcctType);

        extendField = "corp.";
        selectSQL   = " a.corp_p_seqno  , b.acno_p_seqno  , b.credit_act_no , b.stmt_cycle, "
                + " a.comm_zip, a.comm_addr1, a.comm_addr2, a.comm_addr3, a.comm_addr4, a.comm_addr5, "
                + " a.reg_zip, a.reg_addr1, a.reg_addr2, a.reg_addr3, a.reg_addr4, a.reg_addr5";
        whereStr    = "WHERE a.corp_no   = ? "
                + "  and b.acct_type = ? "
                + "  and b.acct_key  = ? ";
        daoTable    = "crd_corp a,act_acno b ";

        setString(1, mbosCorpNo);
        setString(2, mbosAcctType);
        setString(3, mbosCorpNo);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
        	tmpCheckCode = "E07";
            errorMsg = "無法人資料/帳戶-" + mbosCorpNo;
            return (1);
        }

        hCorpCorpPSeqno = getValue("corp.corp_p_seqno");
        hCorpAcnoPSeqno = getValue("corp.acno_p_seqno");
        hCorpCreditActNo = getValue("corp.credit_act_no");
        hCorpStmtCycle = getValue("corp.stmt_cycle");
        hCorpCommZip = getValue("corp.comm_zip");
        hCorpCommAddr1 = getValue("corp.comm_addr1");
        hCorpCommAddr2 = getValue("corp.comm_addr2");
        hCorpCommAddr3 = getValue("corp.comm_addr3");
        hCorpCommAddr4 = getValue("corp.comm_addr4");
        hCorpCommAddr5 = getValue("corp.comm_addr5");
        hCorpRegZip = getValue("corp.reg_zip");
        hCorpRegAddr1 = getValue("corp.corp_reg_addr1");
        hCorpRegAddr2 = getValue("corp.corp_reg_addr2");
        hCorpRegAddr3 = getValue("corp.corp_reg_addr3");
        hCorpRegAddr4 = getValue("corp.corp_reg_addr4");
        hCorpRegAddr5 = getValue("corp.corp_reg_addr5");

        if (debug == 1)
            showLogMessage("I","","  888 stmt_cy=["+ hCorpStmtCycle +"]" + hCorpCorpPSeqno);

        if (hCorpStmtCycle.length() == 0) {
        	tmpCheckCode = "E08";
            errorMsg = "無法人關帳期";
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public int getPSeqno() throws Exception {

        selectSQL = " substr(to_char(ecs_acno.nextval,'0000000000'), 2,10) as temp_x10 ";
        daoTable = "SYSIBM.DUAL         ";

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ecs_acno      error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        pPSeqno = getValue("temp_x10");
        if(debug == 1)
            showLogMessage("I", "", "  888 acno p_seq[" + pPSeqno + "]");

        return (0);
    }

    // ************************************************************************
    public int assignStmtCycle() throws Exception {

        selectSQL = " min(stmt_cycle) as min_stmt_cycle ";
        whereStr  = "WHERE to_char(sysdate,'dd') between issue_s_day and issue_e_day "
                + "  and cycle_flag = 'Y' ";
        daoTable  = "ptr_workday         ";

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_workday   error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        pStmtCycle = getValue("min_stmt_cycle");
        if (debug == 1)
            showLogMessage("I", "", "  888 ptr stmt =[" + pStmtCycle + "]");

        return (0);
    }

    // ************************************************************************
    public int checkPmCard() throws Exception {
        selectSQL = "id_no_code as pm_id_code   ";
        daoTable  = "crd_idno            ";
        whereStr  = "WHERE id_no     = ? "
                + "  and birthday  = ? " + "fetch first 1 row only";

        setString(1, mbosPmId);
        setString(2, mbosPmBirthday);

        int recCnt = selectTable();

        mbosPmIdCode = getValue("pm_id_code");

        if (mbosPmIdCode.length() == 0)
            mbosPmIdCode = "0";

        selectSQL = "a.major_id_p_seqno , a.card_no  ";
        daoTable  = "crd_card a , crd_idno b ";
        whereStr  = "WHERE b.id_no        = ?  and b.id_no_code   = ?   "
                + "  and a.id_p_seqno   = b.id_p_seqno   "
                + "  and a.card_type    = ?   "
                + "  and a.current_code = '0' "
                + "  and a.sup_flag     = '0' "
                + "  and decode(a.group_code ,'' ,'0000', a.group_code) = ?  fetch first 1 row only";
        extendField = "check_pm_card.";

        setString(1, mbosPmId);
        setString(2, mbosPmIdCode);
        setString(3, mbosCardType);
        setString(4, mbosGroupCode);

        tmpInt = selectTable();

        hMajorIdPSeqno = getValue("check_pm_card.major_id_p_seqno");
        hMajorCardNo = getValue("check_pm_card.card_no");

        if(debug == 1)
            showLogMessage("I", "", "  chk pm id=[" + mbosPmId + "]"+ hMajorCardNo);
        if (hMajorCardNo.length() == 0) {
        	tmpCheckCode = "E09";
            errorMsg = "無正卡資料";
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public int checkChgPhone() throws Exception {
        selectSQL = "max(decode(oppost_date,'','99981231',oppost_date))  as max_oppost_date ";

        daoTable = "crd_card            ";
        whereStr = "WHERE id_p_seqno   =  ?  and sup_flag     = '0' ";

        setString(1, hIdnoIdPSeqno);

        int recCnt = selectTable();

        if (notFound.equals("Y"))
            return (1);

        tmpChar = getValue("max_oppost_date");
        if (tmpChar.length() == 0)
            tmpChar = "99981231";
        comr.increaseDays(tmpChar, 90);
        hCardOppostDate = comr.increaseNewDate;
        if (Long.parseLong(hCardOppostDate) > Long.parseLong(sysDate))
            return (0);

        selectSQL = "count(*)   as acct_cnt        ";
        daoTable  = "act_acno a,act_acct b ";
        whereStr  = "WHERE a.id_p_seqno    =  ?  " + "  and a.acno_p_seqno    = b.p_seqno "
                + "  and b.acct_jrnl_bal = 0   "
                + "  and a.sale_date     = ''  ";

        setString(1, hIdnoIdPSeqno);
        if (debug == 1)
            showLogMessage("I", "", "  chk chg 2[" + selectSQL + "]");

        recCnt = selectTable();
        if (debug == 1)
            showLogMessage("I", "", "  chk chg 3[" + selectSQL + "]");

        if (getValueInt("acct_cnt") > 0)
            return (1);

        return (0);
    }

    // ************************************************************************
    public int checkCrdCard() throws Exception {
        selectSQL = "count(*)   as card_cnt        ";
        daoTable  = "crd_card            ";
        whereStr  = "WHERE acno_p_seqno      =  ?  "
                + "  and current_code = '0'  and sup_flag     = '0' ";

        setString(1, pPSeqno);

        int recCnt = selectTable();

        if (getValueInt("card_cnt") > 0)
            return (1);

        return (0);
    }
    // ************************************************************************    
    public int selectActAcno() throws Exception {
    	extendField = "sacno.";
    	selectSQL   = "stmt_cycle,acno_flag ";
    	daoTable    = "act_acno    ";
    	whereStr    = "WHERE acno_p_seqno  = ? ";
    	setString(1, pPSeqno);

    	int recCnt = selectTable();

    	if (notFound.equals("Y")) {
    		String err1 = "select_act_acno not find error=" + pPSeqno;
    		String err2 = "";
    		comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
    	}

    	hCardStmtCycle  = getValue("sacno.stmt_cycle");
    	hCardAcnoFlag   = getValue("sacno.acno_flag");
    	if(debug == 1) {
    		showLogMessage("I","","  888 select_act_acno stmt_cycle=["+hCardStmtCycle+"]"+hCardAcnoFlag);
    	}

    	return 0;
    }
    // ************************************************************************
    public int updateCrdRela() throws Exception {

        if (mbosOldCardNo.length() == 0)
            return (0);

        selectSQL = "count(*)   as rela_cnt        ";
        daoTable = "crd_rela            ";
        whereStr = "WHERE card_no      =  ?  ";

        setString(1, mbosOldCardNo);

        int recCnt = selectTable();

        if (getValueInt("rela_cnt") > 0) {
            updateSQL = "card_no         =  ? , "
                    + "mod_pgm         =  ? , "
                    + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";

            daoTable = "crd_rela      ";
            whereStr = "where card_no = ? ";

            setString(1, mbosCardNo);
            setString(2, javaProgram);
            setString(3, sysDate + sysTime);
            setString(4, mbosOldCardNo);

            updateTable();
        }

        return (0);
    }

    // ************************************************************************
    public int checkCrdCardId() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "  chk c_id[" + pNewAcctFlag + "]");
        if (pNewAcctFlag.equals("Y")) {
            swNew = "Y";
            return (0);
        }

        selectSQL = "count(*)   as card_cnt        ";
        daoTable  = "crd_card            ";
        whereStr  = "WHERE id_p_seqno   =  ?     and acct_type    =  ?     "
                + "  and card_type    = 'CM'   and group_code   = '8669' "
                + "  and current_code = '0'  ";

        setString(1, hIdnoIdPSeqno);
        setString(2, mbosAcctType);

        tmpInt = selectTable();

        if (getValueInt("card_cnt") > 0) {
            swNew = "N";
            return (0);
        }

        selectSQL = "count(*)   as card_cnt        ";
        daoTable  = "crd_card            ";
        whereStr  = "WHERE id_p_seqno   =  ?  "
                + "  and acct_type    =  ?    and current_code = '0' ";

        setString(1, hIdnoIdPSeqno);
        setString(2, mbosAcctType);

        tmpInt = selectTable();

        if (!pNewAcctFlag.equals("Y")) {
            if (getValueInt("card_cnt") > 0) {
                swNew = "N";
                return (0);
            } else {
                selectSQL = "max(trim(oppost_date)) as max_oppost_date ";
                daoTable  = "crd_card            ";
                whereStr  = "WHERE id_p_seqno   =  ?  "
                        + "  and acct_type    =  ?    and current_code <> '0' ";

                setString(1, hIdnoIdPSeqno);
                setString(2, mbosAcctType);

                tmpInt = selectTable();
                tmpChar = getValue("max_oppost_date");

                if (tmpChar.length() == 0)
                    tmpChar = "19901231";
                selectSQL = "to_char(add_months(to_date( ?,'yyyymmdd') , ?),'yyyymmdd') as tmp_x08 ";
                daoTable = "dual           ";

                setString(1, tmpChar);
                setInt(2, paccBreachNumMonth);

                tmpInt = selectTable();
                tmpChar = getValue("tmp_x08");
                tmpLong = Long.parseLong(tmpChar);

                if (tmpLong > Long.parseLong(sysDate)) // n個月內
                {
                    swNew = "N";
                    return (0);
                }
            }
        }

        return (0);
    }
    // ************************************************************************
    public int checkCardNo() throws Exception {
        selectSQL = "count(*)   as card_cnt        ";
        daoTable = "crd_card            ";
        whereStr = "WHERE card_no     = ? ";

        setString(1, mbosCardNo);

        int recCnt = selectTable();

        // if ( recCnt > 0 )
        if (getValueInt("card_cnt") > 0) {
        	tmpCheckCode = "E10";
            errorMsg = "此卡號已存在卡檔";
            showLogMessage("I", "", "  Warning: " + errorMsg + " =" + mbosCardNo);
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public int selectCrdIdno() throws Exception {
        extendField = "sidno.";
        selectSQL = "chi_name      , birthday   , " + "cellar_phone  , id_p_seqno   ";
        daoTable = "crd_idno             ";
        whereStr = "WHERE id_no      = ? " + "  and id_no_code = ? ";

        setString(1, mbosApplyId);
        setString(2, mbosApplyIdCode);

        int recCnt = selectTable();
        if (recCnt < 1)
            return (0);

        hIdnoChiName = getValue("sidno.chi_name");
        hIdnoBirthday = getValue("sidno.birthday");
        hIdnoCellarPhone = getValue("sidno.cellar_phone");
        hTempIdPSeqno = getValue("sidno.id_p_seqno");

        return (0);
    }

    // ************************************************************************
    public int selectCrdCard() throws Exception {
        selectSQL = "substr(old_end_date,5,2) as h_temp_old_end_month , "
                + "acno_p_seqno                  as h_temp_p_seqno ";
        daoTable  = "crd_card            ";
        whereStr  = "WHERE card_no     = ? ";

        setString(1, mbosCardNo);

        int recCnt = selectTable();
        if (recCnt < 1)
            return (0);

        hTempOldEndMonth = getValue("h_temp_old_end_month");
        hTempPSeqno = getValue("h_temp_p_seqno");

        return (0);
    }

    // ************************************************************************
    public int getCardData() throws Exception {
        extendField = "old.";
        daoTable = "crd_card             ";
        whereStr = "WHERE card_no    = ? ";

        setString(1, hOldCardNo);

        int recCnt = selectTable();
        if (recCnt < 1) {
        	tmpCheckCode = "E11";
            errorMsg = "無卡片資料";
            return (1);
        }

        hCardIdPSeqno = getValue("old.id_p_seqno");
        hCardCorpPSeqno = getValue("old.corp_p_seqno");
        hCardCorpNo = getValue("old.corp_no");
        hCardCorpNoCode = getValue("old.corp_no_code");
        hCardCardType = getValue("old.card_type");
        hCardGroupCode = getValue("old.group_code");
        hCardSourceCode = getValue("old.source_code");
        hCardSupFlag = getValue("old.sup_flag");
        hCardSonCardFlag = getValue("old.son_card_flag");
        hCardMajorRelation = getValue("old.major_relation");
        hCardMajorIdPSeqno = getValue("old.major_id_p_seqno");
        hCardMajorCardNo = getValue("old.major_card_no");
        hCardMemberId = getValue("old.member_id");
        hCardEngName = getValue("old.eng_name");
        hCardRegBankNo = getValue("old.reg_bank_no");
        hCardUnitCode = getValue("old.unit_code");
        hCardNewBegDate = getValue("old.new_beg_date");
        hcardnewenddate = getValue("old.new_end_date");
        hCardEmbossData = getValue("old.emboss_data");
        hCardAcctType = getValue("old.acct_type");
        hCardPSeqno = getValue("old.acno_p_seqno");
        hCardGpNo = getValue("old.p_seqno");
        hCardFeeCode = getValue("old.fee_code");
        hCardCurrFeeCode = getValue("old.curr_fee_code");
        hCardIndivCrdLmt = getValueInt("old.indiv_crd_lmt");
        hCardIndivInstLmt = getValueDouble("old.indiv_inst_lmt");
        hCardExpireReason = getValue("old.expire_reason");
        hCardExpireChgFlag = getValue("old.expire_chg_flag");
        hCardExpireChgDate = getValue("old.expire_chg_date");
        hCardCorpActFlag = getValue("old.corp_act_flag");
        hCardActivateType = getValue("old.activate_type");
        hCardActivateFlag = getValue("old.activate_flag");
        hCardActivateDate = getValue("old.activate_date");
        hCardApplyChtFlag = getValue("old.apply_cht_flag");
        hCardApplyAtmFlag = getValue("old.apply_atm_flag");
        hCardStmtCycle = getValue("old.stmt_cycle");
        hCardComboAcctNo = getValue("old.combo_acct_no");
        hCardComboIndicator = getValue("old.combo_indicator");
        hCardOldBankActno = getValue("old.bank_actno");
        hCardAutoInstallment = getValue("old.auto_installment");
        hCardPromoteDept = getValue("old.promote_dept");
        hCardPromoteEmpNo = getValue("old.promote_emp_no");
        hCardIntroduceId = getValue("old.introduce_id");
        hCardIntroduceEmpNo = getValue("old.introduce_emp_no");
        hCardIntroduceName = getValue("old.introduce_name");
        hCardIssueDate = getValue("old.issue_date");
        hCardCardFeeDate = getValue("old.card_fee_date");
        hCardSpecialCardRateFlag = getValue("old.special_card_rate_flag");
        hCardSpecialCardRate = getValue("old.special_card_rate");       
        hCardFlFlag = getValue("old.fl_flag");
//        hCardPaymentNoII = getValue("old.payment_no_ii");
        hCardClerkId = getValue("old.clerk_id");
        hCardMsgFlag = getValue("old.msg_flag");
        hCardMsgPurchaseAmt = getValueInt("old.msg_purchase_amt");
        

        if(debug ==1)   showLogMessage("I", "", "  get card=["+ mbosEmbossSource +"]"+ mbosEmbossReason);
        if ((mbosEmbossSource.equals("3") || mbosEmbossSource.equals("4"))) {
            String ppCurrentCode = getValue("old.current_code");
            if (ppCurrentCode.equals("0") == false) {
            	tmpCheckCode = "E12";
                errorMsg = "舊卡片不為有效卡";
                return (1);
            }
        }

        /******************* 保留原先主卡卡號 ***********************/
        hOldMajorCardNo = hCardMajorCardNo;
        /********************************************************
         * 為附卡時,正卡需為有效卡 2001/07/19
         ********************************************************/
        if (hCardSupFlag.equals("1")) {
            extendField = "mcrd.";
            selectSQL = "current_code,card_no ";
            daoTable  = "crd_card              ";
            whereStr  = "WHERE major_id_p_seqno = ?  and card_type        = ? "
                    + "  and decode(group_code,'','0000',group_code) = ? "
                    + "  and sup_flag     = '0' "
                    + "  and current_code = '0' ";

            setString(1, hCardMajorIdPSeqno);
            setString(2, hCardCardType);
            setString(3, hCardGroupCode);

            tmpInt = selectTable();
            if (tmpInt < 1) {
            	tmpCheckCode = "E13";
                errorMsg = "正卡不為有效卡";
                return (1);
            }
            hCardMajorCardNo = getValue("mcrd.card_no");
        }

        return (0);
    }

    // ************************************************************************
    public int checkAcctType(String pgm) throws Exception {
        if (hSmidMsgSelAcctType.equals("0"))
            return (0);

        if (hSmidMsgSelAcctType.equals("1")) {
            sqlCmd = "select data_code " + "from sms_dtl_data "
                    + " where table_name='SMS_MSG_ID' " + "and data_key = ? "
                    + "   and data_type='1'";
            setString(1, pgm);
            int recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                String dataCode = getValue("data_code", i);
                if (dataCode.equals(mbosAcctType)) {
                    return 0;
                }
            }
        } else {
            sqlCmd = "select data_code "
                    + "  from sms_dtl_data "
                    + " where table_name = 'SMS_MSG_ID' "
                    + "   and data_key   = ? "
                    + "   and data_type  = '1'";
            setString(1, pgm);
            int recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                String dataCode = getValue("data_code", i);
                if (dataCode.equals(mbosAcctType)) {
                    return 1;
                }
            }
            return (0);
        }

        return (1);
    }

    // ************************************************************************
    public int selectSmsMsgId() throws Exception {
        hSmidMsgId = "";
        hSmidMsgDept = "";
        hSmidMsgSendFlag = "";
        hSmidMsgSelAcctType = "";
        hSmidMsgUserid = "";
        hSmidMsgSelAmt01 = "";
        hSmidMsgAmt01 = 0;
        hSmidMsgRunDay = 0;

        selectSQL = "msg_id        , msg_dept   , "
                + "decode(ACCT_TYPE_SEL,'','Y',ACCT_TYPE_SEL) as acct_type_sel , "
                + "msg_userid , "
                + "decode(msg_send_flag    ,'','N',msg_send_flag)     as msg_send_flag , "
                + "decode(msg_sel_amt01    ,'','N',msg_sel_amt01    ) as msg_sel_amt01     , "
                + "msg_amt01     , msg_run_day  ";
        daoTable  = "sms_msg_id          ";
        whereStr  = "WHERE msg_pgm     = ? ";

        setString(1, hSmdlMsgPgm);

        selectTable();
        if (notFound.equals("Y"))
            return (1);

        hSmidMsgId = getValue("msg_id");
        hSmidMsgDept = getValue("msg_dept");
        hSmidMsgSendFlag = getValue("msg_send_flag");
        hSmidMsgSelAcctType = getValue("acct_type_sel");
        hSmidMsgSelAmt01 = getValue("msg_sel_amt01");
        hSmidMsgUserid = getValue("msg_userid");
        hSmidMsgAmt01 = getValueDouble("msg_amt01");
        hSmidMsgRunDay = getValueInt("msg_run_day");

        if (hSmidMsgSendFlag.equals("Y"))
            return (0);

        return (1);
    }
    // ************************************************************************
    public int insertSmsMsgDtl() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888 insert sms =[" + mbosCardNo + "]");

        String hTempCellphoneCheckFlag = "Y";

        if (!comm.isNumber(hIdnoCellarPhone))
            hTempCellphoneCheckFlag = "N";

        extendField = "sms_msg_dtl.";
        setValue(extendField+"msg_pgm"             , hSmdlMsgPgm);
        setValue(extendField+"msg_dept"            , hSmidMsgDept);
        setValue(extendField+"msg_userid"          , hSmidMsgUserid);
        setValue(extendField+"msg_id"              , hSmidMsgId);
        setValue(extendField+"cellar_no"           , hIdnoCellarPhone);
        setValue(extendField+"cellphone_check_flag", hTempCellphoneCheckFlag);
        setValue(extendField+"chi_name"            , hIdnoChiName);
        setValue(extendField+"msg_desc"            , szTmp);
        setValue(extendField+"p_seqno"             , hTempPSeqno);
        setValue(extendField+"acct_type"           , mbosAcctType);
        setValue(extendField+"id_p_seqno"          , hTempIdPSeqno);
        //   setValue(extendField+"id_no"               , mbos_apply_id);
        setValue(extendField+"add_mode"            , "B");
        setValue(extendField+"crt_date"            , sysDate);
        setValue(extendField+"crt_user"            , "SYSTEM");
        setValue(extendField+"apr_date"            , sysDate);
        setValue(extendField+"apr_user"            , "SYSTEM");
        setValue(extendField+"apr_flag"            , "Y");
        setValue(extendField+"SEND_FLAG"           , "N"); // msg_status
        setValue(extendField+"proc_flag"           , "N"); //
        setValue(extendField+"mod_time"            , sysDate + sysTime);
        setValue(extendField+"mod_pgm"             , javaProgram);

        daoTable = "sms_msg_dtl     ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "select_add_month     error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int subMainProcess() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "  sub main[" + mbosApplyIdCode + "]");
        int rtn = 0;
        // 第一次分期付款額度 = 一般額度
        getClassCode();

        // if(strncmp(h_combo_indicator.arr,"Y",1) == 0) {get_cash_rate();} //
        // in 505

        acnoExitFlag = checkAcno();

        validflag = checkCrdCard();

        hCknoOldAcctBank = "";
        hCknoOldAcctNo = "";
        hCknoProcMark = "N";

        /*
         * lai test acno_exit_flag = 1;
         */

        if (debug == 1)
            showLogMessage("I", "", "888  acno exit flag=[" + acnoExitFlag + "]");

        // * 存在(舊卡戶)
        if (acnoExitFlag == 0) {
        	
        	selectActAcno();
        	
            selectSQL = "count(*)   as card_cnt        ";
            daoTable  = "crd_card            ";
            whereStr  = "WHERE acno_p_seqno =  ?  "
                    + "  and current_code = '0' ";

            setString(1, pPSeqno);

            tmpInt = selectTable();
            if (getValueInt("card_cnt") < 1) {
                selectSQL = "max(oppost_date)   as h_oppost_date ";
                daoTable  = "crd_card              ";
                whereStr  = "WHERE acno_p_seqno   =  ?  ";

                setString(1, pPSeqno);

                tmpInt = selectTable();
                hOppostDate = getValue("h_oppost_date");
                /*
                 * lai test
                h_oppost_date = "20170301";
                h_combo_indicator = "Y";
                 */

                if (hOppostDate.length() > 0) {
                    hOppostDate = comm.nextNDate(hOppostDate, 30);
                    tmpLong = Long.parseLong(hOppostDate);
                }
                if (tmpLong > Long.parseLong(sysDate))
                    pClassCode = pAcnoClassCode;
            } else
                pClassCode = pAcnoClassCode;

            if (pAcctKey.length() == 0)
                pAcctKey = pAcnoAcctKey;

            if (pSupFlag.equals("1") && (pAcnoStmtCycle.length() == 0)) {
            	tmpCheckCode = "E14";
                errorMsg = "正卡無關帳期";
                return (1);
            }
            // *************************************************
            // 附卡不可做UPDATE動作,若正卡存在即可 2001/09/05
            // *************************************************
            // **** 附卡無指撥額度 ****
            pComboCashLimit = 0;
            if (mbosSupFlag.equals("0")) {
                if (hComboIndicator.compareTo("N") != 0)
                    pComboCashLimit = pAcnoComboCashLimit;

                computeLocAmtcash(); // * mike
                if (pAcnoCreditAmt != pCreditAmt) {
                    if (validflag == 1) {
                        if ((pCreditAmt > pAcnoCreditAmt) && swMove.equals("N"))
                            rtn = insertRskAcnolog();
                    } else
                        rtn = insertRskAcnolog();
                }
                // ********** combo卡insert差額 *****
                // **** 原指撥額度與本次將入主檔之信用額度比較,取小值 *****
                /*
                 * lai test h_oppost_date = "20170301"; h_combo_indicator = "Y";
                 * p_acno_credit_amt =31000 ; p_credit_amt =30000;
                 * p_acno_combo_cash_limit = 31000; p_credit_amt = 20000;
                 */
                int tCashLimit = 0;
                if (hComboIndicator.compareTo("N") != 0) {
                    if (((pAcnoCreditAmt > pCreditAmt) &&
                            (pAcnoComboCashLimit > pCreditAmt)) ||
                            (hFancyLimitFlag.equals("Y") && pAcnoComboCashLimit > 0)) {
                        if (hFancyLimitFlag.equals("Y"))
                            tCashLimit = 0;
                        else
                            tCashLimit = pCreditAmt;

                        if ((rtn = insertComboJrnl(pCreditAmt, tCashLimit, 2)) != 0)
                            return (rtn);
                        insertOnbat();
                    }
                }
                rtn = updateActAcno();
                updateActAcno1();
                if (mbosActNoL.length() > 0) {
                    hTempValid = "2"; // * CYCLE生效
                    if (hAcnoAutopayAcctNo.length() > 0) {
                        if (!mbosActNoL.equals(hAcnoAutopayAcctNo)) {
                            hCknoOldAcctBank = hAcnoAutopayAcctBank;
                            hCknoOldAcctNo = hAcnoAutopayAcctNo;
                            hCknoOldAcctId = hAcnoAutopayId;

//                            insert_act_chkno();
                        }
                    }
//                  else
//                  	insert_act_chkno();
                }
            }
            insertActAcctForeign(1);

            pStmtCycle = pAcnoStmtCycle;
            // **** exit this function ****
            return (rtn);
        } else {
            getPSeqno();
            // mbos_acct_key = p_acct_key;
            if (mbosActNoL.length() > 0 && pSupFlag.equals("0")) {
                hTempActNo = mbosActNoL;
                hTempValid = "1"; /* 即時生效 */
//                insert_act_chkno();
            }
        }
        /********************************************
         * 正卡資料指定stmt_cycle及insert act_acno
         ********************************************/
        pGpNo = pPSeqno;
        /********************************************
         * 一般卡指定stmt_cycle順序: (2002/03/20) 
         * 1. idno之最早stmt_cycle 
         * 2. ptr_acct_type指定stmt_cycle 
         * 3. APS指定stmt_cycle 
         * 4. 系統自動指定
         ********************************************/
        rtn = 0;

        if (debug == 1) showLogMessage("I", "", "  888 p_stmt=["+ pStmtCycle + "]"+ mbosStmtCycle);

        /**********************************************
         * 1.檢核crd_idno之最早stmt_cycle
         **********************************************/
        if (hFstStmtCycle.length() > 0) {
            pStmtCycle = hFstStmtCycle;
        } else {
            /**********************************************
             * 2.ptr_acct_type參數檔內是否有指定之stmt_cycle
             **********************************************/
            if (hStmtCycle.length() > 0) {
                pStmtCycle = hStmtCycle;
            } else {
                /*********************************************
                 * 3.檢核APS是否有上傳,p_stmt_cycle(APS傳value) 4.p_stmt_cycle is
                 * null,系統自動指定
                 *********************************************/
                if (pStmtCycle.length() <= 0)
                    assignStmtCycle();
            }
        }
        if (debug == 1) showLogMessage("I", "", "  8881 stmt=["+ pStmtCycle + "]"+ mbosStmtCycle);

        pGpNo = pPSeqno;
        // get_credit_act_no();
        
        rtn = insertActAcno(1);
        if (rtn != 0)
            return (rtn);

        updateActAcno1();
        insertActAcct(2);
        if (hComboIndicator.compareTo("N") != 0) {
            rtn = insertComboJrnl(pComboCashLimit, pComboCashLimit, 1);
            if (rtn != 0)
                return (rtn);
        }

        return (0);
    }

    // ************************************************************************
    public int subBusProcess() throws Exception {
        if (debug == 1) showLogMessage("I", "", " 888 sub_bus_pro acno=[" + pAcnoRowid + "]");

        /* 第一次分期付款額度 = 一般額度 */
        getClassCode();
        /**********************************
         * 抓取個人序號 ,!= 0 -- 不存在
         **********************************/
        rtn = getIndivAcctKey();
        pAcctKey = pAcnoAcctKey;
        if (debug == 1)
            showLogMessage("I", "", " 999  acct_key =["+ pAcnoAcctKey +"]"+ hCorpCorpPSeqno);

        pPSeqno = pAcnoPSeqno;
        pGpNo   = pAcnoGpNo;
        validflag = 0;
        validflag = checkCrdCard();

        if (debug == 1)
            showLogMessage("I", "", " 999  1.1  =["+rtn+"]"+ hCorpCorpPSeqno +","+ pAcnoRowid);
        /********* 個繳帳戶已存在 *************/
//        if (rtn == 0) {
//            selectSQL = "count(*)   as card_cnt        ";
//            daoTable = "crd_card            ";
//            whereStr = "WHERE acno_p_seqno      =  ?  and current_code = '0' ";
//
//            setString(1, pAcnoPSeqno);
//
//            tmpInt = selectTable();
//
//            if (getValueInt("card_cnt") < 1) {
//                selectSQL = "max(oppost_date)   as h_oppost_date ";
//                daoTable = "crd_card              ";
//                whereStr = "WHERE acno_p_seqno   =  ?  ";
//
//                setString(1, pPSeqno);
//
//                tmpInt = selectTable();
//                hOppostDate = getValue("h_oppost_date");
//                /*  lai test
//                h_oppost_date = "20170301";
//                h_combo_indicator = "Y";
//                */
//
//                if (hOppostDate.length() > 0) {
//                    hOppostDate = comm.nextNDate(hOppostDate, 30);
//                    tmpLong = Long.parseLong(hOppostDate);
//                }
//                if (tmpLong > Long.parseLong(sysDate))
//                    pClassCode = pAcnoClassCode;
//            } else {
//                pClassCode = pAcnoClassCode;
//            }
//            if (pSupFlag.equals("0")) {
//                computeLocAmtcash();
//                hAcnoComboCashLimit = 0;
//                rtn = updateActAcno();
//                updateActAcno1();
//                if (pAcnoCreditAmt != pCreditAmt) {
//                    if (validflag == 1) {
//                        if ((pCreditAmt > pAcnoCreditAmt) && swMove.equals("N"))
//                            rtn = insertRskAcnolog();
//                    } else
//                        rtn = insertRskAcnolog();
//                }
//            }
//            insertActAcctForeign(2);
//
//            return (rtn);
//        }
        if (debug == 1) showLogMessage("I", "", " 999  1.2  =[" + hCorpCorpPSeqno);

        getPSeqno();
        
        //20221216商務卡(個人)的ACCT_KEY = P_SEQNO + 0
        if(!mbosCorpActFlag.equals("Y")) { 
        	pAcctKey = pPSeqno + "0";
        }
        
        if (debug == 1) showLogMessage("I", "", " 999  1.3  =[" + hCorpCorpPSeqno);

        // p_gp_no = p_acct_p_seqno
        if (pCorpActFlag.equals("Y"))
            pGpNo = pCorpAcnoPSeqno;
        else
            pGpNo = pPSeqno;

        /********************************************************
         * 商務卡個人defualt reg_bank,reg_bank_no (2001/07/15)
         *******************************************************/
        pRegBankNo = "";
        pRiskBankNo = "";
        
        rtn = insertActAcno(2);

        if (rtn != 0)
            return (rtn);

        updateActAcno1();

        if (pCorpActFlag.equals("N"))
            insertActAcct(1);

        return (0);
    }

    // ************************************************************************
    public int procChgCard() throws Exception {

        hOldCardNo = mbosOldCardNo;
        // init_crd_card();

        if ((rtn = getCardData()) != 0)
            return (rtn);

        hCardOldActivateType = hCardActivateType;
        hCardOldActivateFlag = hCardActivateFlag;
        hCardOldActivateDate = hCardActivateDate;
        hCardActivateType = "";
        hCardActivateFlag = "1";
        hCardActivateDate = "";
        hCardOldBegDate = hCardNewBegDate;
        hCardOldEndDate = hcardnewenddate;
        hCardNewBegDate = getValue("valid_fm");
        hcardnewenddate = getValue("valid_to");
        hCardChangeDate = sysDate;
        hCardChangeStatus = "3";
        hCardBatchno = mbosBatchno;
        hCardRecno = mbosRecno;
        hCardMailType = getValue("mail_type");
        hCardBankActno = getValue("bank_actno");
        hCardMailBranch = getValue("mail_branch");
        hCardBranch = getValue("branch");

        rtn = updateChgCard(2);
        if (rtn != 0)
            return (rtn);

        return (0);
    }

    // ************************************************************************
    public int procReissueCard() throws Exception {

        hOldCardNo = mbosOldCardNo;
        // init_crd_card();

        if ((rtn = getCardData()) != 0)
            return (rtn);

        /* 重製中 -- 不為毀損重製時,需寫舊卡及新卡號,並增加一筆新資料到卡片主檔 */
        hCardMailBranch = getValue("mail_branch");

        if (mbosEmbossReason.compareTo("2") != 0) {
            pPSeqno = hCardPSeqno;
            tmpInt = checkCrdCard();
            if (tmpInt == 0) {
                pAcctType = hCardAcctType;
//                selectPtrPurLmt();
//                if (updateReissueAcno() != 0)
//                    return (1);
            }
            hCardReissueDate = sysDate;
            hCardReissueStatus = "3"; /* 重製已完成 */
            hCardNewCardNo = mbosCardNo;
            /*** adding bank_actno ***/
            hCardBankActno = getValue("bank_actno");
            rtn = updateOldCard(1);
            if (rtn != 0)
                return (rtn);

            moveOldCardVar();
            hCardReissueDate = "";
            hCardReissueStatus = "";
            hCardNewBegDate = getValue("valid_fm");
            hcardnewenddate = getValue("valid_to");
            hCardOldCardNo = mbosOldCardNo;
            hCardEngName = getValue("eng_name");
            hCardUnitCode = mbosUnitCode;
            hCardEmbossData = getValue("emboss_4th_data");
            hCardMailType = getValue("mail_type");
            hCardMailBranch = getValue("mail_branch");
            hCardBranch = getValue("branch");
            hCardMailNo = "";
            hCardActivateFlag = "1";
            hCardCardMoldFlag = getValue("card_mold_flag");
            hCardCardRefNum = mbosCardRefNum;
            
            if ((rtn = insertCrdCard(3)) != 0)
                return (rtn);
            
			rtn = selectCrdCardPp();
			if (rtn == 0) {
				updateCrdCardPp();
			}	
        } else {
            /* 毀損重製 卡號不變 */
            hCardReissueDate = sysDate;
            hCardReissueStatus = "3"; /* 重製已完成 */
            hCardOldBegDate = hCardNewBegDate;
            hCardOldEndDate = hcardnewenddate;
            hCardNewBegDate = getValue("valid_fm");
            hcardnewenddate = getValue("valid_to");
            hCardMailType = getValue("mail_type");
            hCardEngName = getValue("eng_name");
            hCardUnitCode = mbosUnitCode;
            hCardEmbossData = getValue("emboss_4th_data");
            hCardOldActivateType = hCardActivateType;
            hCardOldActivateFlag = hCardActivateFlag;
            hCardOldActivateDate = hCardActivateDate;
            hCardActivateFlag = "1";
            hCardActivateType = "";
            hCardActivateDate = "";
            hCardBatchno = mbosBatchno;
            hCardRecno = mbosRecno;
            hCardMailBranch = getValue("mail_branch");
            hCardBranch = getValue("branch");
            hCardMailNo = "";
            hCardBankActno = getValue("bank_actno");
            hCardCurrentCode = "0";
            hCardOppostDate = "";
            hCardOppostReason = "";
            hCardCardRefNum = mbosCardRefNum;

            if (mbosOnlineMark.equals("1") || mbosOnlineMark.equals("2"))
                hCardUrgentFlag = "Y";

            hCardChangeDate = sysDate;
            hCardChangeStatus = "3";

            rtn = updateChgCard(1);
            if (rtn != 0)
                return (rtn);
        }

        return (0);
    }
    // ************************************************************************
    public int procUrgentCard() throws Exception {

        hOldCardNo = mbosOldCardNo;
        if ((rtn = getCardData()) != 0)
            return (rtn);

        hCardReissueDate = sysDate;
        hCardReissueStatus = "3";
        hCardNewCardNo = mbosCardNo;
        hCardBankActno = getValue("bank_actno");
        rtn = updateOldCard(1);
        if (rtn != 0)
            return (rtn);

        moveOldCardVar();
        hCardReissueDate = "";
        hCardReissueStatus = "";
        hCardNewBegDate = getValue("valid_fm");
        hcardnewenddate = getValue("valid_to");
        hCardEmergentFlag = "Y";
        hCardActivateFlag = "1";
        hCardOldCardNo = mbosOldCardNo;
        hCardStmtCycle = pStmtCycle;
        hCardBranch = getValue("branch");
        hCardMailBranch = getValue("mail_branch");

        hCardCardMoldFlag = getValue("card_mold_flag");

        if ((rtn = insertCrdCard(4)) != 0)
            return (rtn);

        return (0);
    }

    // ************************************************************************
    public String addMonth(String inputM, int addMon) throws Exception {

        selectSQL = "to_char(add_months(to_date( ?,'yyyymm'), ?) ,'yyyymm') as new_month ";
        daoTable = "dual                ";

        setString(1, inputM);
        setInt(2, addMon);

        tmpInt = selectTable();

        if (tmpInt < 1) {
            String err1 = "select_add_month     error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (getValue("new_month"));
    }
    // ************************************************************************
    public int delPinBlock() throws Exception {

        if (debug == 1) showLogMessage("I", "", " delete emap   =[" + " " + "]");

        daoTable = "crd_cardno_data ";
        whereStr = "WHERE card_no  = ? ";

        setRowId(1, mbosCardNo);

        deleteTable();

        if (notFound.equals("Y")) {
        	tmpCheckCode = "E15";
            errorMsg = "線上製卡PIN_BLOCK值抓取不到";
            return (1);
        }

        return (0);
    }
    // ************************************************************************
    public void initProcssData() throws Exception {
        pCardNo = "";
        pRegBankNo = "";
        pRiskBankNo = "";
        pAcctType = "";
        pAcctKey = "";
        pAcctGpNo = "";
        pGpNo = "";
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
        pCorpAcnoPSeqno = "";
        pPSeqno = "";
        pMailZip = "";
        pMailAddr1 = "";
        pMailAddr2 = "";
        pMailAddr3 = "";
        pMailAddr4 = "";
        pMailAddr5 = "";
        pStmtCycle = "";
        pComboAcctNo = "";
    }

    // ************************************************************************
    public void initRtn() throws Exception {

        mbosBatchno = "";
        mbosRecno = 0;
        mbosEmbossSource = "";
        mbosEmbossReason = "";
        mbosVirtualFlag = "";
        mbosOnlineMark = "";
        mbosrtnncccdate = "";
        mbosCvv2 = "";
        mbosPvki = "";
        mbosPinBlock = "";
        mbosCardNo = "";
        mbosOldCardNo = "";
        mbosGroupCode = "";
        mbosSourceCode = "";
        mbosCardType = "";
        mbosPmId = "";
        mbosPmIdCode = "";
        mbosPmBirthday = "";
        mbosApplyId = "";
        mbosApplyIdCode = "";
        mbosSupFlag = "";
        mbosBirthday = "";
        mbosMajorCardNo = "";
        mbosUnitCode = "";
        mbosToNcccDate = "";
        mbosVoiceNum = "";
        mbosStmtCycle = "";
        mbosAcctType = "";
        mbosInMainError = "";
        mbosInMainDate = "";
        mbosInMainMsg = "";
        mbosStatSendInternet = "";
        mbosActNo = "";
        mbosActNoL = "";
        mbosActNoF = "";
        mbosCurrCode = "";
        mbosAgreeLInd = "";
        mbosActNoFInd = "";
        mbosChiName = "";
        mbosElectronicCode = "";
        mbosCorpNo = "";
        mbosCorpNoCode = "";
        mbosCorpActFlag = "";
        mbosCardIndicator = "";
        mbosCashLmt = 0;
        mbosIntroduceNo = "";
        mbosIntroduceId = "";
        mbosIntroduceName = "";
        mbosAcceptDm = "";
        mbosRowid = "";
        mbosSuperNote = "";
        mbosActNoLInd = "";
        mbosResidentZip = "";
        mbosResidentAddr1 = "";
        mbosResidentAddr2 = "";
        mbosResidentAddr3 = "";
        mbosResidentAddr4 = "";
        mbosResidentAddr5 = "";
        mbosSpecialCardRate = "";
        mbosCardRefNum = "";
        mbosCrtBankNo = "";
        mbosVdBankNo = "";
        mbosSpouseName = "";
        mbosSpouseIdNo = "";
        mbosSpouseIdNo = "";
        mbosResidentNoExpireDate = "";
        mbosGraduationElementarty = "";
        mbosUrFlag = "";
        mbosBusinessCode = "";
        mbosInstFlag = "";
        mbosCreditLevelNew = "";
        mbosMailZip = "";
        mbosMailAddr1 = "";
        mbosMailAddr2 = "";
        mbosMailAddr3 = "";
        mbosMailAddr4 = "";
        mbosMailAddr5 = "";
        mbosCompanyZip = "";
        mbosCompanyAddr1 = "";
        mbosCompanyAddr2 = "";
        mbosCompanyAddr3 = "";
        mbosCompanyAddr4 = "";
        mbosCompanyAddr5 = "";
        mbosFeeCodeI = "";
        mbosRevolveIntRate = 0.0;
        mbosEMailAddr = "";
        mbosFlFlag = "";
        mbosClerkId = "";
        mbosAutopayAcctBank = "";
        mbosBillApplyFlag = "";
        mbosCurrChangeAccout = "";
        mbosMarketAgreeBase = "";
        mbosENews = "";
        mbosAcceptCallSell = "";
        mbosAcceptSms = "";
        mbosSmsAmt = 0;
        tmpSpecialCardRateFlag = "";
        tmpMsgFlag = "";
        tmpMsgPurchaseAmt = 0;
        mbosRevolveIntRateYear = 0.0;

        pgcCrdMakecardFeeFlag = "";
        pgcCrdMakecardFee = "";
        paccCashadvLocRate = 0;
        paccCashadvLocMaxamt = 0;
        paccCashadvLocRateOld = 0;
        paccBreachNumMonth = 0;
        paccAtmCode = "";
        mbosStatSendInternet = "";
        hFancyLimitFlag = "";
        pCashRate = 0;

        hComboIndicator = "";
        hFCurrencyFlag = "";
        hFancyLimitFlag = "";
        delPinFlag = 0;
        rtnCode = 0;
        errorMsg = "";
        hPinBlock = "";
        hCvv2 = "";
        hPvki = "";
        swNew = "";
        swMove = "";
        moveChiName = "";
        moveCardNo = "";
        moveRowid = "";

        cardAutoInstallment = "";
        pgcdGroupName = "";
        hStmtCycle = "";
        hRcUseFlag = "";
        hCardIndicator = "";
        hMajorIdPSeqno = "";
        hMajorCardNo = "";
        hCardNote = "";
        hBinType = "";
        hBinNo = "";
        hIdnoIdPSeqno = "";
        hIdnoChiName = "";
        hIdnoId = "";
        hIdnoIdCode = "";
        hIdnoCellarPhone = "";
        hIdnoBirthday = "";
        hFstStmtCycle = "";
        hIdRowid = "";
        paccCashadvLocMaxamt = 0;
        paccCashadvLocRate = 0;
        paccCashadvLocRateOld = 0;
        paccBreachNumMonth = 0;
        paccAtmCode = "";
        hPpltPercentC = 0;
        hPpltLfixC = 0;
        pCashRate = 0;
        validflag = 0;
        hAcctKey = "";
        hCorpCorpPSeqno = "";
        hCorpAcnoPSeqno = "";
        hCorpCreditActNo = "";
        hCorpStmtCycle = "";

        pGpNo = "";
        pOrgRiskBankNo = "";
        pAcnoAcctKey = "";
        pAcnoPSeqno = "";
        pAcnoGpNo = "";
        pAcnoRegBankNo = "";
        pAcnoClassCode = "";
        pAcnoVipCode = "";
        pAcnoCreditAmt = 0;
        pAcnoCreditAmtCash = 0;
        pAcnoComboCashLimit = 0;
        pAcnoComboAcctNo = "";
        pAcnoStatSendInternet = "";
        pAcnoStmtCycle = "";
        pAcnoCreditActNo = "";
        pAcnoStopStatus = "";
        hAcnoAutopayAcctBank = "";
        hAcnoAutopayAcctNo = "";
        hAcnoAutopayId = "";
        pAcnoCorpActFlag = "";
        pNewAcctFlag = "";
        pAutopayIndicator = "";
        hAcnoComboCashLimit = 0;
        hAcnoAutopayIndicator = "";
        hAcnoAutopayAcctEDate = "";
        pAcnoRowid = "";
        acnoExitFlag = 0;

        hOppostDate = "";
        hTempLineOfCreditAmtCash = 0;
        tempComboLimit = 0;
        hTempActNo = "";
        hTempValid = "";
        hAadkFuncCode = "";
        hAadkCurrCode = "";
        hAadkCardNo = "";
        hAadkAutopayAcctBank = "006";
        hAadkAutopayAcctNo = "";
        hCardOppostDate = "";
        hCardActivateFlag = "";
        hCardPSeqno = "";
        hCardStmtCycle = "";
        hCardGpNo = "";
        hCardIdPSeqno = "";
        hCardCardType = "";
        hCardGroupCode = "";
        hCardSourceCode = "";
        hCardUnitCode = "";
        hCardSupFlag = "";
        hCardCurrentCode = "";
        hCardMemberId = "";
        hCardEngName = "";
        hCardRegBankNo = "";
        hCardPinBlock = "";
        hCardNewBegDate = "";
        hcardnewenddate = "";
        hCardCardNo = "";
        hCardCorpNo = "";
        hCardCorpNoCode = "";
        hCardCorpPSeqno = "";
        hCardCorpActFlag = "";
        hCardAcnoFlag = "";
        hCardMajorIdPSeqno = "";
        hOldMajorCardNo = "";
        hCardIssueDate = "";
        hCardMajorCardNo = "";
        hCardPromoteEmpNo = "";
        hCardApplyAtmFlag = "";
        hCardJcicScore = 0;
        hCardCardMoldFlag = "";
        hCardSetCode = "";
        hCardApplyNo = "";
        hCardCvv2 = "";
        hCardPvki = "";
        hCardEmbossData = "";
        hCardMajorRelation = "";
        hCardAcctType = "";
        hCardIntroduceId = "";
        hCardIntroduceName = "";
        hCardFeeCode = "";
        hCardBatchno = "";
        hCardRecno = 0;
        hCardMailType = "";
        hCardMailNo = "";
        hCardOldBankActno = "";
        hCardMailBranch = "";
        hCardStockNo = "";
        hCardComboBegBal = 0;
        hCardComboEndBal = 0;
        hCardComboAcctNo = "";
        hCardEmergentFlag = "";
        hCardActivateDate = "";
        hCardOldActivateDate = "";
        hCardOldActivateFlag = "";
        hCardOldActivateType = "";
        hCardExpireChgDate = "";
        hCardExpireChgFlag = "";
        hCardExpireReason = "";
        hCardCurrFeeCode = "";
        hCardApplyChtFlag = "";
        hCardNewCardNo = "";
        hCardOldCardNo = "";
        hCardOppostReason = "";
        hCardTransCvv2 = "";
        hCardCvv = "";
        hCardIndivInstLmt = 0;
        hCardPvv = "";
        hCardProdNo = "";
        hCardIntroduceEmpNo = "";
        hCardPromoteDept = "";
        hCardUpgradeDate = "";
        hCardChangeDate = "";
        hCardActivateType = "";
        hCardReissueReason = "";
        hCardReissueDate = "";
        hCardOldBegDate = "";
        hCardOldEndDate = "";
        hCardAutoInstallment = "";
        hCardUrgentFlag = "";
        hCardChangeStatus = "";
        hCardReissueStatus = "";
        hCardUpgradeStatus = "";
        hCardCardRefNum = "";
        hCardCardFeeDate = "";
        hCardSpecialCardRateFlag = "";
        hCardSpecialCardRate = "";
        hCardFlFlag = "";        
        hCardPaymentNoII = "";   
        hCardClerkId = "";
        hCardMsgFlag = "";
        hCardMsgPurchaseAmt = 0;

        hTempOldEndMonth = "";
        hTempPSeqno = "";
        hTempIdPSeqno = "";
        hSmdlMsgPgm = "";
        hSmidMsgId = "";
        hSmidMsgDept = "";
        hSmidMsgSendFlag = "";
        hSmidMsgSelAcctType = "";
        hSmidMsgSelAmt01 = "";
        hSmidMsgUserid = "";
        hSmidMsgAmt01 = 0;
        hSmidMsgRunDay = 0;
        szTmp = "";
        hOldCardNo = "";
        hThirdDataReissue = "N";
        tmpCheckCode = "";
    }
    // ************************************************************************
    public int insertCrdIdnoSeqno() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", "888  insert crd_idno_seqno=[" + hIdnoIdPSeqno + "]" + mbosApplyId);

        setValue("id_no"             , mbosApplyId);
        setValue("id_p_seqno"        , hIdnoIdPSeqno);
        setValue("id_flag"           , "");
        setValue("bill_apply_flag"   , "");
        setValue("debit_idno_flag"   , "N");

        daoTable = "crd_idno_seqno   ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_idno_seqno    error[dupRecord]= ";
            String err2 = mbosApplyId;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    int selectCrdCardPp() throws Exception {
    	int tmpcnt = 0;

        sqlCmd  = "select count(*) as cnt ";
        sqlCmd += " from crd_card_pp ";
        sqlCmd += "where card_no     = ?   ";
        setString(1, hCardOldCardNo);
        selectTable();
        tmpcnt = getValueInt("cnt");
        if (tmpcnt > 0) {
        	return (0);
        } 
        else {
            return (1);
        }
    }

    // ************************************************************************
   void updateCrdCardPp() throws Exception {

    		daoTable   = "crd_card_pp";
            updateSQL  = " card_no = ?, ";
            updateSQL += " mod_pgm = ?, ";
            updateSQL += " mod_user = ?, ";
            updateSQL += " mod_time = sysdate ";
            whereStr   = " where card_no = ? ";
            setString(1, hCardCardNo);
            setString(2, javaProgram);
            setString(3, javaProgram);
            setString(4, hCardOldCardNo);
            updateTable();            
   }
    
   // ************************************************************************

} // End of class FetchSample
