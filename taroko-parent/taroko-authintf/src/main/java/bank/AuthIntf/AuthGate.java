//javadoc => https://www.tutorialspoint.com/java/java_documentation.htm
package bank.AuthIntf;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import bank.Auth.HpeUtil;

//import com.tcb.authProg.iso8583.TagObject;
/**
* AuthGate 存放單次交易會用到的所有變數
* 
*
* @author  Howard Chang
* @version 1.0
* @since   2017/12/19
*/

public class AuthGate {
  	

  public    Connection G_DbConn  = null;
  public double CurTotalUnpaidOfPersonal=0;
  public double CurTotalUnpaidOfComp=0;
  public boolean bG_Token06RealLengthIs52 = false;
  public String tokenIdC4="";
  public String tokenC4TxnStatInd="";//TokenC4.TxnStatInd
  public String tokenC4TermAttendInd = "", tokenC4TermLocInd = "", tokenC4ChPresetInd = "", tokenC4CrdPresetInd = "", tokenC4CrdCaptrInd = "";
  public String tokenC4TxnSecInd = "", tokenC4ChActvtInd = "", tokenC4TermInputCAP = "", tokenC4TxnRtnInd="";
  public String tokenC4Filter1="",tokenC4Filter2="";

  public double cardAcctTotAmtMonth=0;
  public double cardAcctTotAmtMonthOfComp=0;
  public String CardAcctIdxOfComp="";
  public double CcaConsumePaidAnnualFeeOfComp = 0;
  public double CcaConsumePaidSrvFeeOfComp = 0;
  public double CcaConsumePaidLawFeeOfComp = 0;
      		   
  public double CcaConsumePaidPunishFeeOfComp = 0;
  public double CcaConsumePaidInterestFeeOfComp = 0;
  public double CcaConsumePaidConsumeFeeOfComp = 0;
      		   
      		   
  public double CcaConsumePaidPrecashOfComp = 0;
  public double CcaConsumePaidCyclsOfComp = 0;
      		   
  public double CcaConsumePaidTotUnPayOfComp = 0;
  public double CcaConsumeUnPaidOfComp = 0;
  public double CcaConsumeUnPaidSrvFeeOfComp = 0;
  public double CcaConsumeUnPaidLawFeeOfComp = 0;
      		   
  public double CcaConsumeUnPaidInterestFeeOfComp = 0;
  public double CcaConsumeUnPaidConsumeFeeOfComp = 0;
  public double CcaConsumeUnPaidPrecashOfComp = 0;
  public double CcaConsumeArgueAmtOfComp = 0;
      		   
  public double CcaConsumePrePayAmtOfComp = 0;
  public double CcaConsumeTotUnPaidAmtOfComp = 0;
  public double CcaConsumeBillLowLimitOfComp = 0;
  public double CcaConsumeBillLowPayAmtOfComp = 0;
      		   
  public double CcaConsumeIbmReceiveAmtOfComp = 0;
  public double CcaConsumeUnPostInstFeeOfComp = 0;
  public double TotAmtConsumeOfComp = 0;
  public double TotAmtPreCashOfComp = 0;
      		   
  public double CcaConsumeTxTotAmtMonthOfComp = 0;
  public double CcaConsumeTxTotCntMonthOfComp = 0;
  public double CcaConsumeTxTotAmtDayOfComp = 0;
  public double CcaConsumeTxTotCntDayOfComp = 0;
      		   
  public double CcaConsumeFnTotAmtMonthOfComp = 0;
  public double CcaConsumeFnTotCntMonthOfComp = 0;
  public double CcaConsumeFnTotAmtDayOfComp = 0;
  public double CcaConsumeFnTotCntDayOfComp = 0;
      			   		 
  public double CcaConsumeFcTotAmtMonthOfComp = 0;
  public double CcaConsumeFcTotCntMonthOfComp = 0;
  public double CcaConsumeFcTotAmtDayOfComp = 0;
  public double CcaConsumeFcTotCntDayOfComp = 0;
    
  public double CcaConsumeRejAuthCntDayOfComp = 0;
  public double CcaConsumeRejAuthCntMonthOfComp = 0;
  public double CcaConsumeTrainTotAmtMonthOfComp = 0;
  public double CcaConsumeTrainTotAmtDayOfComp = 0;

  public double CcaConsumePaidAnnualFee = 0;
  public double CcaConsumePaidSrvFee = 0;
  public double CcaConsumePaidLawFee = 0;
      		   
  public double CcaConsumePaidPunishFee = 0;
  public double CcaConsumePaidInterestFee = 0;
  public double CcaConsumePaidConsumeFee = 0;
      		   
      		   
  public double CcaConsumePaidPrecash = 0;
  public double CcaConsumePaidCycls = 0;
      		   
  public double CcaConsumePaidTotUnPay = 0;
  public double CcaConsumeUnPaid = 0;
  public double CcaConsumeUnPaidSrvFee = 0;
  public double CcaConsumeUnPaidLawFee = 0;
      		   
  public double CcaConsumeUnPaidInterestFee = 0;
  public double CcaConsumeUnPaidConsumeFee = 0;
  public double CcaConsumeUnPaidPrecash = 0;
  public double CcaConsumeArgueAmt = 0;
      		   
  public double CcaConsumePrePayAmt = 0;
  public double CcaConsumeTotUnPaidAmt = 0;
  public double CcaConsumeBillLowLimit = 0;
  public double CcaConsumeBillLowPayAmt = 0;
      		   
  public double CcaConsumeIbmReceiveAmt = 0;
  public double CcaConsumeUnPostInstFee = 0;
  public double TotAmtConsume = 0;
  public double TotAmtPreCash = 0;
      		   
  public double CcaConsumeTxTotAmtMonth = 0;
  public double CcaConsumeTxTotCntMonth = 0;
  public double CcaConsumeTxTotAmtDay = 0;
  public double CcaConsumeTxTotCntDay = 0;
      		   
  public double CcaConsumeFnTotAmtMonth = 0;
  public double CcaConsumeFnTotCntMonth = 0;
  public double CcaConsumeFnTotAmtDay = 0;
  public double CcaConsumeFnTotCntDay = 0;
      			   		 
  public double CcaConsumeFcTotAmtMonth = 0;
  public double CcaConsumeFcTotCntMonth = 0;
  public double CcaConsumeFcTotAmtDay = 0;
  public double CcaConsumeFcTotCntDay = 0;
    
  public double CcaConsumeRejAuthCntDay = 0;
  public double CcaConsumeRejAuthCntMonth = 0;
  public double CcaConsumeTrainTotAmtMonth = 0;
  public double CcaConsumeTrainTotAmtDay = 0;
  
  public  String   sG_DefaultErrorIsoField39 = "99";
  public  int      initPnt=2;
  public  String[] isoField = new String[193];
  public  String[] visaF62  = new String[64];
  public  String[] visaF63  = new String[24];
  public  String[] visaF126 = new String[24];
  public  String   chtTpdu="" , chtProcCode="", chtAmount="", chtTraceNo="", chtNetId="", chtTerId="", chtSourceStr="", chtMsgType="", chtBitMap="";
  public  String   chtAccInfo="", chtExpDate="", chtEntryMode="", chtCondCode="", chtMerId="", chtAppData="";
  
  public  String   bicHead="",errNum="000",mesgType="",hcomTpdu="", isoString="", specialIsoString="";// smsMsgType="", smsMsgId="";
  //ArrayList G_TokenQ8ObjArrayList = null;
  public ArrayList<TagObject> G_TokenQ8ObjArrayList = null;
  public  String   otpValue = "";
  public  String   sG_TokenQ8SourceStr="", sG_TokenQ9SourceStr="", sG_TokenF1SourceStr="", sG_TokenCZSourceStr="", tokenQ8TagQ9="";
  public  String   tokenQ8TagQA="", tokenQ8Tag27="", tokenQ8Tag51="", tokenQ8Tag50="", tokenQ8Tag07=""; 
  public  boolean  bG_TokenQ9FormatIsVisa=false;
  public  boolean  bG_HasPersonalAdj=false, bG_HasCompAdj=false;  
  public  String    tokenQ9VisaMsgRsnCde="", tokenQ9Fiid="";
  public  String    tokenQ9VisaFiller="", tokenQ9VisaChipTxnInd="", tokenQ9VisaDevTyp="";
  public  String   EdcTradeFunctionCode="";
  public  String    tokenQ9MasterDevTyp="", tokenQ9MasterAdviceRsnCde="", tokenQ9MasterAdvcDetlCde="", tokenQ9MasterAuthAgentIdCde="",tokenQ9MasterOnBehalf="", tokenQ9MasterFiller="";
  public  String   tokenQrAdditionalData1 ="", tokenQrAdditionalData2 = "";
  public  String   tokenF4WalletIndFlg = "", tokenF4WalletIndData = "", tokenF4Filler = "";
  public  String   bmsHead="",bmsDestId="",bmsSourceId="",tokenS8AcVeryRslt="", tokenS8FraudChkRslt="", newPin1="", tokenS8AcctNum="",  tokenIdQR="";
  public  String   newPinFrmt="", newPinOfst="", pinCnt="", nwePinSize="", newPin2="", ncccStandinInd="", pvvOnCardFlg="";
  public  String   tokenS8Filler = "", tokenS8ExpDat = "", tokenS8AcctNumInd = "";
  public  String   sG_TransactionStartDateTime="";
  public  boolean  convertError=false,isoError=false, IsNewCard=false, isInstallmentTx=false, isRedeemTx=false;
  public  byte[]   isoData = new byte[2048];
  public  byte[]   specialIsoData = new byte[2048];
  public  int      totalLen=0,dataLen=0, specialDataTotalLen=0;
  public  String   orgiReserve="",stationId="",destStation="",srcStation="",terminalType="",termEntryCap="";
  public  String   sG_Key4OkTrans="", sG_CardProd="";
  public  String   sG_UsedCardProd="";   
  public  String   sG_IsoRespCode=""; // == proc.AuTxlog_ISO_RSP_CODE
  public  String   sG_OnLineRespCode=""; // == proc.ONL_RESP_CODE
  public  String   sG_Bit38ApprCode="", TransType=""; 
  public  String   TokenC4TxnStatInd="";//TokenC4.TxnStatInd
  public  String   authUnit="A"; 
  public boolean   ifStandIn=false;
  public boolean   ifCredit=true; // 是否要佔額度
  public  boolean  bmsTrans=true;       // �@�d�q
  public  boolean  visaDebit=false;     // VISA DEBIT �d
  public  boolean  purchaseCard=false;  // ���ʥd
  public  boolean  businessCard=false;  // 公司卡/商務卡
  public  boolean  childCard=false;     // �l�d
  public  boolean  comboCard=false;     // ���Υd
  public  boolean  urgentCard=false;    // �����N�d
  public  boolean  specialCard=false;   // �S���B�פ��Υd

  public  String   areaType="";         // "T" : �x�W�ꤺ��� , "F" : ��~���
  public  String   connCode="";         // "A" : BASE-24 �۰ʱ��v ,"W" : WEB�H�u���v, "V" : VISA ���v, "M" : MASTER ���v

  public  boolean  installTrans=false;  // �������
  public  boolean  redeemTrans=false;   // ���Q���
  public  boolean  isAcs = false;
  //public  boolean  batchAuth=false;     // ���ΨƷ~ �妸���v���
  
  public int nG_TxSession = 0;
  
  public String sG_StaTxUnNormalMccBinNo="", sG_StaTxUnNormalGroupCode="", sG_StaTxUnNormalRespCode="", sG_StaTxUnNormalRiskType="";
  public int nG_StaTxUnNormalTxCnt=0;
  public int nG_StaTxUnNormalTxAmt=0;
  
  public String sG_SDailyMccBinNo="",sG_SDailyMccGroupCode="";
  public String sG_StaDailyMccUnNormalFlag="N", tokenChPmntTypInd4Master="";
  //public int nG_StaDailyMccTxSession=0; //此值不知道該如何取得..?
  public int nG_StaDailyMccAuthCnt = 0;
  public int nG_StaDailyMccAuthAmt = 0;

  public int nG_StaDailyMccCallBankCnt = 0;
  public int nG_StaDailyMccCallBankAmt = 0;
  public int nG_StaDailyMccCallBankCntx = 0;
  public int nG_StaDailyMccCallBankAmtx = 0;

  public int nG_StaDailyMccDeclineCnt = 0;
  public int nG_StaDailyMccDeclineAmt = 0;

  public int nG_StaDailyMccPickupCnt = 0;
  public int nG_StaDailyMccPickupAmt = 0;

  public int nG_StaDailyMccExpiredCnt = 0;
  public int nG_StaDailyMccExpiredAmt = 0;

      	
  public int nG_StaDailyMccConsumeCnt = 0;
  public int nG_StaDailyMccConsumeAmt = 0;

  public int nG_StaDailyMccGenerCnt = 0;
  public int nG_StaDailyMccGenerAmt = 0;

  public int nG_StaDailyMccCashCnt = 0;
  public int nG_StaDailyMccCashAmt = 0;

  public int nG_StaDailyMccReturnCnt = 0;
  public int nG_StaDailyMccReturnAmt = 0;

      	
  public int nG_StaDailyMccAdjustCnt = 0;
  public int nG_StaDailyMccAdjustAmt = 0;

  public int nG_StaDailyMccReturnAdjCnt = 0;
  public int nG_StaDailyMccReturnAdjAmt = 0;

  public int nG_StaDailyMccForceCnt = 0;
  public int nG_StaDailyMccForceAmt  =0;

  public int nG_StaDailyMccMailCnt = 0;
  public int nG_StaDailyMccMailAmt = 0;

  public int nG_StaDailyMccPreauthCnt = 0;
  public int nG_StaDailyMccPreauthAmt = 0;

  public int nG_StaDailyMccPreauthOkCnt = 0;
  public int nG_StaDailyMccPreauthOkAmt = 0;

  public int nG_StaDailyMccCashAdjCnt = 0;
  public int nG_StaDailyMccCashAdjAmt = 0;

  public int nG_StaDailyMccReversalCnt = 0;
  public int nG_StaDailyMccReversalAmt = 0;

  public int nG_StaDailyMccEcCnt = 0;
  public int nG_StaDailyMccEcAmt = 0;

      	
  public int nG_StaDailyMccUnNormalCnt = 0;
  public int nG_StaDailyMccUnNormalAmt = 0;

  
  public String sG_SRskTypeBinNo ="", sG_SRskTypeGroupCode="";
  public String sG_SRskRiskType="", sG_SRskCurrRespCode="", sG_StaRiskTypeUnNormalFlag="";
  //public int nG_StaRiskTypeTxSession=0; //此值不知道該如何取得..?
  public int nG_StaRiskTypeAuthCnt = 0;
  public int nG_StaRiskTypeAuthAmt = 0;
      	
  public int nG_StaRiskTypeCallBankCnt = 0;
  public int nG_StaRiskTypeCallBankAmt = 0;

  public int nG_StaRiskTypeCallBankCntx = 0;
  public int nG_StaRiskTypeCallBankAmtx = 0;

  public int nG_StaRiskTypeDeclineCnt = 0;
  public int nG_StaRiskTypeDeclineAmt = 0;

  public int nG_StaRiskTypePickupCnt = 0;
  public int nG_StaRiskTypePickupAmt = 0;

  public int nG_StaRiskTypeExpiredCnt = 0;
  public int nG_StaRiskTypeExpiredAmt = 0;

      	
  public int nG_StaRiskTypeConsumeCnt = 0;
  public int nG_StaRiskTypeConsumeAmt = 0;

  public int nG_StaRiskTypeGenerCnt = 0;
  public int nG_StaRiskTypeGenerAmt = 0;

  public int nG_StaRiskTypeCashCnt = 0;
  public int nG_StaRiskTypeCashAmt = 0;

  public int nG_StaRiskTypeReturnCnt = 0;
  public int nG_StaRiskTypeReturnAmt = 0;

      	
  public int nG_StaRiskTypeAdjustCnt = 0;
  public int nG_StaRiskTypeAdjustAmt = 0;

  public int nG_StaRiskTypeReturnAdjCnt = 0;
  public int nG_StaRiskTypeReturnAdjAmt = 0;

  public int nG_StaRiskTypeForceCnt = 0;
  public int nG_StaRiskTypeForceAmt  =0;

  public int nG_StaRiskTypeMailCnt = 0;
  public int nG_StaRiskTypeMailAmt = 0;

  public int nG_StaRiskTypePreauthCnt = 0;
  public int nG_StaRiskTypePreauthAmt = 0;

  public int nG_StaRiskTypePreauthOkCnt = 0;
  public int nG_StaRiskTypePreauthOkAmt = 0;

  public int nG_StaRiskTypeCashAdjCnt = 0;
  public int nG_StaRiskTypeCashAdjAmt = 0;

  public int nG_StaRiskTypeReversalCnt = 0;
  public int nG_StaRiskTypeReversalAmt = 0;

  public int nG_StaRiskTypeEcCnt = 0;
  public int nG_StaRiskTypeEcAmt = 0;

      	
  public int nG_StaRiskTypeUnNormalCnt = 0;
  public int nG_StaRiskTypeUnNormalAmt = 0;

  public  boolean  bG_AbnormalResp = true;
 /* ������O */
  public  boolean  normalPurch=false,cashAdvance=false,balanceInquiry=false,changeAtmPin=false,selfGas=false;
  public  boolean  ecTrans=false,taxTrans=false,txVoice=false,speedTrain=false,creditAccount=false;
  public  boolean  preAuth=false,preAuthComp=false,reversalTrans=false,atmTrans=false,ecGamble=false;
  public  boolean  forcePosting=false,preAuthAdvice=false,cancelTrans=false,tipsTrans=false;//adviceTrans=false;
  
  public  boolean  requestTrans=false, updateSrcTxAfterAdjust=false, updateSrcTxAfterPreAuthComp=false, updateSrcTxAfterReversal=false;
  public  String   srcIntfName="",destIntfName="",intfName="",srcFormatType="",destFormatType="", reversalFlag="N";
  public  boolean  convertToken=false,emvTrans=false;
  public  String   originator="0",respondor="0";
  public  boolean  isEInvoice = false,  isVip=false, isSpecUse=false, isDebitCard=false, isSpecW0=false, isPrecash=true;// isCorp=false; //isCorp == proc.szCorpFlag;  isPrecash ==proc.szPrecashFlag
  public  boolean  easyAutoloadFlag = false,  easyAutoload=false, easyAutoloadChk=false, ipassAutoload=false, ipassAutoloadChk=false, icashAutoload=false, icashStandIn=false;//三大票證
  public  boolean  nonPurchaseTxn = false; 
  public  String ScSpecCode ="";
  public  String corpActFlag="";  //Y:總繳, N:個繳
  public  String cardAcctAcnoFlag=""; //"1":一般(個人卡，個繳) or "2":總繳公司() or "3":商務個繳(商務卡，個繳) or "Y":總繳個人(商務卡，公司總繳)
  //public  boolean hasAdj=false; //�O�_���{��

 /* ����`�B */
  public  double   monthTotalAmt=0,monthTotalCnt=0,dayTotalAmt=0,dayTotalCnt=0;
  public  double   supMonthAmt=0,supMonthCnt=0,supDayAmt=0,supDayCnt=0;
  public  double   finalTotLimit=0;//該等級之總額度(A)，proc name is tmpTotLimit1
  public  double   tmpTotLimit2=0;
  
  /* ������ */
  public  boolean  forceAuthPassed=false; //強制授權成功
  public  boolean  forceAuthRejected=false; //強制授權失敗
  public  boolean  writeToStatisticTable= true; //寫入統計檔
  public  String   connType="",txType="",fromAcctType="",toAcctType="",startTime="",binNo="",transCode="", logic_del="0", auth_type="";
  public  String   acctStatus="",riskLevel="",groupCode="";
  public  boolean  purchAdjust=false,cashAdjust=false,refund=false,refundAdjust=false;
  public  boolean  mailOrder=false,masterEC=false,accountVerify=false, visaEC=false;
  public  boolean  tokenProcess020092=false, tokenProcess020093=false;
  public  int      chanNum=0,transCnt=0;
  public  String   vmjType="";
  public  double	debitMakup=1, debitFee=0;
  public  String   imsLockSeqNo="";

  /*
  public String    cardAcctTotAmtDay="0"; //累積日消費額
  public String    cardAcctTotCntDay="0"; //累積日消費次數
  public String    cardAcctFnTotAmtDay="0";//國外一般消費日總額
  public String    cardAcctFnTotCntDay="0"; //國外一般消費日總次
  public String    cardAcctFcTotAmtDay ="0";//國外預借現金日總額
  public String    cardAcctFcTotCntDay="0"; //國外預借現金日總次
  public String    cardAcctTrainTotalAmtDay ="0";//高鐵累積日消費額
  
  public String    cardAcctTotAmtMonth="0"; //累積月消費額
  public String    cardAcctTotCntMonth="0"; //累積月消費次數
  public String    cardAcctFnTotAmtMonth="0"; //國外一般消費月總額
  public String    cardAcctFnTotCntMonth="0"; //國外一般消費月總次
  public String    cardAcctFcTotAmtMonth="0"; //國外預借現金月總額
  public String    cardAcctFcTotCntMonth="0"; //國外預借現金月總次
  public String    cardAcctTrainTotalAmtMonth="0"; //高鐵累積月消費額
  public String    cardAcctTotAmtConsume="0"; //總授權額(已消未請)
  
  public String    cardAcctTotAmtPrecash = "0";
  public String    cardAcctTxTotAmtMonth = "0";
  public String    cardAcctTxTotCntMonth = "0";
  public String    cardAcctTxTotAmtDay = "0";
  public String    cardAcctTxTotCntDay = "0";

  public String    cardAcctTotAmtDayOfComp="0"; //累積日消費額
  public String    cardAcctTotCntDayOfComp="0"; //累積日消費次數
  public String    cardAcctFnTotAmtDayOfComp="0";//國外一般消費日總額
  public String    cardAcctFnTotCntDayOfComp="0"; //國外一般消費日總次
  public String    cardAcctFcTotAmtDayOfComp="0";//國外預借現金日總額
  public String    cardAcctFcTotCntDayOfComp="0"; //國外預借現金日總次
  public String    cardAcctTrainTotalAmtDayOfComp="0";//高鐵累積日消費額
  
  public String    cardAcctTotAmtMonthOfComp="0"; //累積月消費額
  public String    cardAcctTotCntMonthOfComp="0"; //累積月消費次數
  public String    cardAcctFnTotAmtMonthOfComp="0"; //國外一般消費月總額
  public String    cardAcctFnTotCntMonthOfComp="0"; //國外一般消費月總次
  public String    cardAcctFcTotAmtMonthOfComp="0"; //國外預借現金月總額
  public String    cardAcctFcTotCntMonthOfComp="0"; //國外預借現金月總次
  public String    cardAcctTrainTotalAmtMonthOfComp="0"; //高鐵累積月消費額
  public String    cardAcctTotAmtConsumeOfComp="0"; //總授權額(已消未請)
  
  public String    cardAcctTotAmtPrecashOfComp= "0";
  public String    cardAcctTxTotAmtMonthOfComp= "0";
  public String    cardAcctTxTotCntMonthOfComp= "0";
  public String    cardAcctTxTotAmtDayOfComp= "0";
  public String    cardAcctTxTotCntDayOfComp= "0";
  */

  
  public String   cardBaseSpecStatus="", cardBaseSpecDelDate="";
  public String   cardBaseSpecFlag="";
  public String    cardBaseLastAmt = "0"; //最後消費金額 
  public int    cardBaseTotAmtDay=0; //日累積消費金額
  public int    cardBaseTotCntDay=0; //日累積消費次數
  public String    cardBaseLastConsumeDate=""; //最後消費日期
  public String    cardBaseLastConsumeTime=""; //最後消費時間
  public String    cardBaseLastAuthCode=""; //最後授權碼
  public String    cardBaseLastCurrency=""; //最後消費幣別
  public String    cardBaseLastCountry=""; //最後消費國家
  public String    cardBasePreAuthFlag="1"; //預先授權註記 => 表示 => /*非預先授權完成*/
  public String    cardBaseWriteOff1="0"; //預先授權沖消狀態(1) => 表示 /*非預先授權完成*/
  
  public int    riskTradeDayAmt=0; //本日累積交易金額
  public int    riskTradeDayCnt=0;  //本日累積交易筆數
  public int    riskTradeMonthAmt=0; //本月累積交易金額
  public int    riskTradeMonthCnt=0; //本月累積交易筆數
  
  public  String   MCode="", CardAcctIdx="", ClassCode="";
  public  String   traceNo="",termHotkeycontrol="";

  public  String   ecsBatchCode="";
  public  String   mccRiskType="", mccRiskAmountRule="", mccRiskNcccFtpCode="";
  public  String   MccRiskMccCode="" ,merchantNo="",terminalNo="",mccCode="",entryMode="",posConditionCode="", merchantCountry="", merchantName="", entryModeType="", merchantCityName="", merchantCity="";//,country=""
  
  public  String   cardAcctId="",idNo="",cardNo="",expireDate="", refNo="", oriAuthNo="", oriRespCode="";
  public  String   idPseqno="",cardCode="",cardType="",cardKind="",lastTxDate="";//rejectCode=""
  public  String   txDate="", txTime="", T24ErrorCodes="", T24ErrorCodesFromDb="";//T24ErrorCodesFromDb ��sample: "5005,5001,5008"
  public  String   CASH_CODE="CASH";
  public  boolean  resetCcaConsumeMonthData=true, resetCcaConsumeDayData=true, urgentFlag=false, comboTrade=false, ifCheckOpeningCard=false, isVirtualCard=false, isChildCard=false;
  public boolean   lowTradeCheck=false;
  //public  double   transAmount=0;
  public  double   adjustAmount=0,oriAmount=0,  balanceAmt=0, repl_amt=0, src_oriAmount=0;
  public  double   bank_tx_amt=0;
  public  double   totMonthLimit=0,totDayLimit=0,supMonthLimit=0,supDayLimit=0;
  public double    OtbAmt=0, ParentOtbAmt=0;//�d��l�B
  public double   balInqTotal=0;
  public double ccaConsumeBillLowLimit=0;
  public double ccaConsumeBillLowLimitOfComp=0;
  public double cbOtb=0;//
  //public double baseLimit=0;  /*帳戶循環信用額度*/  //Howard proc.CardAcct_LMT_TOT_CONSUME == java.ActAcnoLineOfCreditAmt
  //public double baseLimitOfComp=0;  /*帳戶循環信用額度-公司*/  //Howard proc.CardAcct_LMT_TOT_CONSUME == java.ActAcnoLineOfCreditAmt

  public double cashBase=0;   /*預借現金額度*/  //Howard proc.CardAcct_LMT_TOT_CASH == java.ActAcnoLineOfCreditAmtCash
  public double cashBaseOfComp=0;  //預借現金額度-公司
  //
  public double totalLimit=0; /**/
  public double cashLimit=0;  /**/
  public double monthLimit=0; /**/
  public double timesLimit=0; /**/
  public double monthCntLimit; /**/
  public double timesCntLimit=0;/**/
  public double monthLimitX=0;
  public double timesLimitX=0;
  public double paidPreCash=0;
  public double wkAdjAmt=0;
  public double wkAdjCnt=0;
  public double wkAdjTot=0;
  public double adjParmAmtRate=0; //
  public double adjParmCntRate=0; //
  public double adjTotAmt=0;
  public String wkCashCode = "";
  /*  */
  public  boolean  switchKeyExchange=false,bankKeyExchange=false, ReadFromIbmSuccessful=true, ReadFromAcerSuccessful=true;
  public  boolean  changeKey=false,newKey=false,repeatKey=false,verifyKey=false;// forcePost=false;
  public  String   keyType="",keyDirection="",keyExchangeKey="",workingKeyZPK="",workingKeyLMK="",checkValue;

  /* */
  public  String   servCode="",pvki="",pvv="",cvv="",cvv2="", cvdfld="";
  public  String   pinBlock="",arqc="",arpc="",tvr="",cvr="",arc="", newPinBlockFromHsm="", newPinFromHsm="";
  //kevin:tcb atm 交易新增
  public  String   atmHead="", atmType="", reqType="", respCode="", pCode="", fCode="", birthday="", arqcLen="", txnDate="";
  
  public  boolean  ifSendSms4Cond1=false, ifSendSms4Cond2=false, ifIgnoreSmsOfTrading=false; 
  public  boolean  is3DTranx=false; //是否為3D交易
  public  String   tokenData="", tokenC0="";
  public  String   eci="",cvv2token="",xid="",cavv="",ucafInd="",ucaf="", authnIndFlg="", cavvResult="";
  public  String   tccCode="",keyExchangeBlock="";

  /*  */
  public  String   divMark="",installTxInd="",installTxRespCde="";
  public  String   divNum="",firstAmt="",everyAmt="",procAmt="";

  /*  */
  public  String   loyaltyTxId="",loyaltyTxResp="", c5TxFlag="";
  public  String   pointRedemption="",signBalance="",pointBalance="",paidCreditAmt="";

  /* Field_55: EMV Chip Data */
  public  String   emv57="",emv5A="",emv5F24="",emv5F2A="",emv5F34="",emv71="",emv72="",emv82="",emv84="",emv8A="";
  public  String   emv91="",emv95="",emv9A="",emv9B="",emv9C="",emv9F02="",emv9F03="",emv9F09="",emv9F10="",emv9F1A="";
  public  String   tokenB2IssApplDataLen="", tokenB2IssApplData="";
  public  String   emv9F1E="",emv9F26="",emv9F27="",emv9F33="",emv9F34="",emv9F35="",emv9F36="",emv9F37="";
  public  String   emv9F74="",emv9F63="";
  public  String   emv9F41="",emv9F53="",emv9F5B="",emvDF31="",emvDFED="",emvDFEE="",emvDFEF="",emvD6="";
  public  String   termSerNum="";

  /* Field_58: FISC MESSAGE DATA ELEMENT #58 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200304 , ##START## */
  public  String   f58t21="",f58t28="",f58t30="",f58t31="",f58t32="",f58t33="",f58t49="",f58t50="",f58t51="",f58t53="",f58t56="";
  public  String   f58t60="",f58t61="",f58t62="",f58t63="",f58t64="",f58t65="",f58t66="",f58t67="",f58t68="",f58t69="";
  public  String   f58t70="",f58t71="",f58t72="",f58t73="",f58t80="",f58t81="",f58t82="",f58t83="",f58t84="",f58t85="";
  public  String   f58t86="",f58t90="";
  /* Field_58: FISC MESSAGE DATA ELEMENT #58 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200304 , ##END## */

  /* SHARE MEMORY BUUFER  */
  HashMap parmHash = null;

  //down, add by JH

  public int debitParmTableFlag=1;//等於1表示從 table CCA_DEBIT_PARM 取得 debit card 參數; 等於2表示從 table CCA_DEBIT_PARM2 取得 debit card 參數
  public int sendSmsLimitAmt=-1; //
  public boolean ifHaveRiskTradeInfo=true; //是否已經有舊的交易資料了
  public boolean ifSendSmsWhenOverLimit=true; //

  public boolean smsSupLimit=false;	//
  public boolean depositNotEnough=false;	//
  public boolean dayLimitNotEnough=false;	//
  public boolean monthLimitNotEnough=false;	//
  public boolean is_sup_card=false;	//-���d-
  public String binType=""; //CRD_CARD.bin_type
  public String effDateEnd = "";//CRD_CARD.eff_date_end
  public String cardStatus = "";//

  public double  curr_tot_lmt_amt=0;
  //public double  curr_tot_std_amt=0;
  public double  curr_tot_tx_amt=0;
  public double  curr_dd_lmt_amt=0;
  public double curr_dd_tot_amt=0;
  public double curr_tot_cash_amt=0;

  public double curr_tot_unpaid=0;

  //public String src_cacu_amount="Y";
  //public String src_cacu_cash="Y";

  public String stand_in_reason="";
  public String cacu_amount="N"; /* 計入OTB註記            */
  public String cacu_cash="N";  /* 計入OTB預現註記        */
//  public String bank_tx_amt="";
  //public String bank_tx_seqno="";
  //public String src_bank_tx_seqno="";
  public String unlock_flag="N";
  public String auth_err_id="";
  public String auth_status_code="00";
  public String auth_no="";
  public String AuthRemark="";
  //public String nt_amt="";
  public double nt_amt=0, isoFiled4Value=0;//�O��ISOField4����
  public double isoFiled4ValueAfterMarkup=0;//�O��ISOField4 �[�W����O�᪺��
  public double cardholderBillAmt=0, cardholderBillAmtMarkup=0;//來自於ISOField6 CARDHOLDER BILLING AMOUNT and MARKUP
  public double adjustAmountAfterMarkup=0;
  public String auth_seqno="";
  public String iso_bitMap="";
  public String cacu_flag="";
  public String stand_in_rspcode="";
  public String stand_in_onuscode="";
  public String tx_amt_pct="0";
  public String ae_trans_amt="";
  public String roc="";
  public String idno_vip="";
  public String idno_name="";
  public String cvd_present="";
  
  public String fallback="Y";
  public String authUser="";
  
  public String hce_rsn_code="";


  public String bank_bit33_code="";
  public String bank_bit39_code="";
  public String debt_flag="N";
  //kevin:chkeck FHM or NEG 
  public String chkFhmNeg="";
  
  //public String crt_user="system";
  //public String modUser="system";

  public double LastAmt=0; 
  public boolean ProdMode=true;
  public double SrcTxNtAmt=0;//
  public double LockAmt=0; //

  public double UnLockAmt=0; //
  
  public boolean ifSendTransToIbm=false;
  public boolean ifSendTransToIms=false;

  public boolean CallT24ToLock = false;//�O���O�_�ncall T24 �� ��s
  public boolean CallT24ToUnLock = false;//�O���O�_�ncall T24 �� �Ѱ�
  public boolean CallT24ToForceLock = false;//�O���O�_�ncall T24 �� �j���s
  public String MchtNameToT24 = "";// �O����s�P�Ѱ餧�S���W��
  public boolean UpdatePriorOfPriorBankTxSeqNo=false;

  //up, add by JH

  /*
    0:��PIN,
	1:��PIN+CVV�F
	2:��ͤ�F
	3:��ͤ�+CVV;
	4:��CVV
   */
  public String VrfyType="";//

  public String tokenId04="",tokenId06="",tokenId23="",tokenId25="",tokenIdB2="",tokenIdB3="",tokenIdB4="";
  public String tokenIdB5="",tokenIdB6="",tokenIdBJ="",tokenIdC0="",tokenIdC5="",tokenIdC6="";
  public String tokenIdCE="",tokenIdCI="",tokenIdQ2="",tokenIdQ3="",tokenIdW7="",tokenIdW8="",tokenIdWB="";
  public String tokenIdWV="", tokenIdS8="", tokenIdF4="", tokenIdQ8="", tokenIdQ9="", tokenIdF1="", tokenIdCZ="", tokenIdCH="";
  public String visaAddlData="";
  public String tokenDataB2="",tokenDataB3="",tokenDataB4="", tokenData04="";

  //kevin: 取得風險分數 risk_factor
  public int    riskFctorInd=0;
  public double riskFactorScore=0, mccRiskFactor=0, posRiskFactor=0, countryRiskFactor=0, mchtRiskFactor=0;
  public double cardRiskFactor=0, repeatRiskFactor=0, vipRiskFactor=0, amtRiskFactor=0;

  
  public AuthGate() {
	  for ( int k = 0; k < 128; k++) {
		  isoField[k] = "";  
	  }
	  try {
		  sG_TransactionStartDateTime = HpeUtil.getCurDateTimeStr(false, false);
	  } catch (Exception e) {
		// TODO: handle exception
	  }
	  

  }

}
