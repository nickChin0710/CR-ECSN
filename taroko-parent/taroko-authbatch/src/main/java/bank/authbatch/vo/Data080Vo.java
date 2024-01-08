package bank.authbatch.vo;

public class Data080Vo {

	public String getAcnoPSeqNo() {
		return AcnoPSeqNo;
	}
	public void setAcnoPSeqNo(String acnoPSeqNo) {
		AcnoPSeqNo = acnoPSeqNo;
	}
	public String getAcnoFlag() {
		return AcnoFlag;
	}
	public void setAcnoFlag(String acnoFlag) {
		AcnoFlag = acnoFlag;
	}
	public String getCorpPSeqN0() {
		return CorpPSeqN0;
	}
	public void setCorpPSeqN0(String corpPSeqN0) {
		CorpPSeqN0 = corpPSeqN0;
	}
	public String getMajorIdPSeqNo() {
		return MajorIdPSeqNo;
	}
	public void setMajorIdPSeqNo(String majorIdPSeqNo) {
		MajorIdPSeqNo = majorIdPSeqNo;
	}
	public String getDcCurrCode() {
		return DcCurrCode;
	}
	public void setDcCurrCode(String dcCurrCode) {
		DcCurrCode = dcCurrCode;
	}
	public String getCardHolderId() {
		return CardHolderId;
	}
	public void setCardHolderId(String cardHolderId) {
		CardHolderId = cardHolderId;
	}
	public String getCardAcctId() {
		return CardAcctId;
	}
	public void setCardAcctId(String cardAcctId) {
		CardAcctId = cardAcctId;
	}
	public String getCardNo() {
		return CardNo;
	}
	public void setCardNo(String cardNo) {
		CardNo = cardNo;
	}
	public String getPSeqNo() {
		return PSeqNo;
	}
	public void setPSeqNo(String pSeqNo) {
		PSeqNo = pSeqNo;
	}
	public String getIdPSqno() {
		return IdPSqno;
	}
	public void setIdPSqno(String idPSqno) {
		IdPSqno = idPSqno;
	}
	public String getDebitFlag() {
		return DebitFlag;
	}
	public void setDebitFlag(String debitFlag) {
		DebitFlag = debitFlag;
	}
	public String getRule() {
		return Rule;
	}
	public void setRule(String rule) {
		Rule = rule;
	}
	public String getPaymentRule() {
		return PaymentRule;
	}
	public void setPaymentRule(String paymentRule) {
		PaymentRule = paymentRule;
	}
	public String getAccountType() {
		return AccountType;
	}
	public void setAccountType(String accountType) {
		AccountType = accountType;
	}
	public String getValidFrom() {
		return ValidFrom;
	}
	public void setValidFrom(String validFrom) {
		ValidFrom = validFrom;
	}
	public String getValidTo() {
		return ValidTo;
	}
	public void setValidTo(String validTo) {
		ValidTo = validTo;
	}
	public String getOldCardNo() {
		return OldCardNo;
	}
	public void setOldCardNo(String oldCardNo) {
		OldCardNo = oldCardNo;
	}
	public String getCvc2() {
		return Cvc2;
	}
	public void setCvc2(String cvc2) {
		Cvc2 = cvc2;
	}
	public String getSource() {
		return Source;
	}
	public void setSource(String source) {
		Source = source;
	}
	public String getEngName() {
		return EngName;
	}
	public void setEngName(String engName) {
		EngName = engName;
	}
	public String getBusinessCard() {
		return BusinessCard;
	}
	public void setBusinessCard(String businessCard) {
		BusinessCard = businessCard;
	}
	public String getMemberSince() {
		return MemberSince;
	}
	public void setMemberSince(String memberSince) {
		MemberSince = memberSince;
	}
	public String getCardType() {
		return CardType;
	}
	public void setCardType(String cardType) {
		CardType = cardType;
	}
	public int getCreditLimit() {
		return CreditLimit;
	}
	public void setCreditLimit(int creditLimit) {
		CreditLimit = creditLimit;
	}
	public String getPinOfActive() {
		return PinOfActive;
	}
	public void setPinOfActive(String pinOfActive) {
		PinOfActive = pinOfActive;
	}
	public String getPinOfVoice() {
		return PinOfVoice;
	}
	public void setPinOfVoice(String pinOfVoice) {
		PinOfVoice = pinOfVoice;
	}
	public String getGroupCode() {
		return GroupCode;
	}
	public void setGroupCode(String groupCode) {
		GroupCode = groupCode;
	}
	public String getPvki() {
		return Pvki;
	}
	public void setPvki(String pvki) {
		Pvki = pvki;
	}
	public String getPinBlock() {
		return PinBlock;
	}
	public void setPinBlock(String pinBlock) {
		PinBlock = pinBlock;
	}
	public String getBankActNo() {
		return BankActNo;
	}
	public void setBankActNo(String bankActNo) {
		BankActNo = bankActNo;
	}
	public String getAcctNo() {
		return AcctNo;
	}
	public void setAcctNo(String acctNo) {
		AcctNo = acctNo;
	}
	public String getComboIndicator() {
		return ComboIndicator;
	}
	public void setComboIndicator(String comboIndicator) {
		ComboIndicator = comboIndicator;
	}
	public String getBinType() {
		return BinType;
	}
	public void setBinType(String binType) {
		BinType = binType;
	}
	public Data080Vo() {
		// TODO Auto-generated constructor stub
	}

	
	
	public String AcnoPSeqNo; //gp_no => p_seqno
	public String PSeqNo=""; //p_seqno=>acno_p_seqno
	
	public String AcnoFlag;
	public String CorpPSeqN0;
	public String MajorIdPSeqNo;
	public String DcCurrCode;
	public String CardHolderId;
	public String CardAcctId;
	public String CardNo="";
	
	public String IdPSqno="";
	public String DebitFlag="";
	public String Rule="";
	public String PaymentRule="";
	public String AccountType="";

	public String ValidFrom="";
	public String ValidTo="";
	public String OldCardNo="";
	public String Cvc2="";
	public String Source="";  
	public String EngName="";
	public String BusinessCard="";
	public String MemberSince="";
	public String CardType="";
	public int CreditLimit=0;
	public String PinOfActive="";
	public String PinOfVoice="";
	public String GroupCode="";
	public String Pvki="";
	public String PinBlock="";
	public String BankActNo="";
	public String AcctNo="";
	public String ComboIndicator="";
	public String BinType="";

}
