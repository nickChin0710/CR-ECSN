package bank.authbatch.vo;

public class Data060Vo {

	public String getCardNo() {
		return CardNo;
	}
	public void setCardNo(String cardNo) {
		CardNo = cardNo;
	}
	public String getTransDate() {
		return TransDate;
	}
	public void setTransDate(String transDate) {
		TransDate = transDate;
	}
	public int getTransAmt() {
		return TransAmt;
	}
	public void setTransAmt(int transAmt) {
		TransAmt = transAmt;
	}
	public String getAuthNo() {
		return AuthNo;
	}
	public void setAuthNo(String authNo) {
		AuthNo = authNo;
	}
	public String getCardAcctId() {
		return CardAcctId;
	}
	public void setCardAcctId(String cardAcctId) {
		CardAcctId = cardAcctId;
	}
	public String getTransCode() {
		return TransCode;
	}
	public void setTransCode(String transCode) {
		TransCode = transCode;
	}
	public String getRefeNo() {
		return RefeNo;
	}
	public void setRefeNo(String refeNo) {
		RefeNo = refeNo;
	}
	public String getDebitFlag() {
		return DebitFlag;
	}
	public void setDebitFlag(String debitFlag) {
		DebitFlag = debitFlag;
	}
	public String getMccCode() {
		return MccCode;
	}
	public void setMccCode(String mccCode) {
		MccCode = mccCode;
	}
	public Data060Vo() {
		// TODO Auto-generated constructor stub
	}

	public String CardNo;
	public String TransDate;
	public int TransAmt;
	public String AuthNo;
	public String CardAcctId;
	public String TransCode;
	public String RefeNo; //RefeNo == AuthTxLog.BIL_REFERENCE_NO
	public String DebitFlag;
	public String MccCode;
}
