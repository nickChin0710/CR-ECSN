package bank.authbatch.vo;

public class CcaUnmatchVo {

	public int getAmtNt() {
		return AmtNt;
	}
	public void setAmtNt(int amtNt) {
		AmtNt = amtNt;
	}
	public int getAuthAmt() {
		return AuthAmt;
	}
	public void setAuthAmt(int authAmt) {
		AuthAmt = authAmt;
	}
	public String getUDate() {
		return UDate;
	}
	public void setUDate(String uDate) {
		UDate = uDate;
	}
	public String getCardNo() {
		return CardNo;
	}
	public void setCardNo(String cardNo) {
		CardNo = cardNo;
	}
	public String getTxDate() {
		return TxDate;
	}
	public void setTxDate(String txDate) {
		TxDate = txDate;
	}
	public String getAuthNo() {
		return AuthNo;
	}
	public void setAuthNo(String authNo) {
		AuthNo = authNo;
	}
	public String getTransType() {
		return TransType;
	}
	public void setTransType(String transType) {
		TransType = transType;
	}
	public String getRefNo() {
		return RefNo;
	}
	public void setRefNo(String refNo) {
		RefNo = refNo;
	}
	public String getProcCode() {
		return ProcCode;
	}
	public void setProcCode(String procCode) {
		ProcCode = procCode;
	}
	public String getMccCode() {
		return MccCode;
	}
	public void setMccCode(String mccCode) {
		MccCode = mccCode;
	}
	public String getMchtNo() {
		return MchtNo;
	}
	public void setMchtNo(String mchtNo) {
		MchtNo = mchtNo;
	}
	public String getMessageHead5() {
		return MessageHead5;
	}
	public void setMessageHead5(String messageHead5) {
		MessageHead5 = messageHead5;
	}
	public String getMessageHead6() {
		return MessageHead6;
	}
	public void setMessageHead6(String messageHead6) {
		MessageHead6 = messageHead6;
	}
	public String getBit127RecData() {
		return Bit127RecData;
	}
	public void setBit127RecData(String bit127RecData) {
		Bit127RecData = bit127RecData;
	}
	public String getUTime() {
		return UTime;
	}
	public void setUTime(String uTime) {
		UTime = uTime;
	}
	public String getAuthDate() {
		return AuthDate;
	}
	public void setAuthDate(String authDate) {
		AuthDate = authDate;
	}
	public CcaUnmatchVo() {
		// TODO Auto-generated constructor stub
	}

	public String UDate;
	public String CardNo;
	public String TxDate;
	public String AuthNo;
	public int AmtNt;
	public String TransType;
	public String RefNo;
	public String ProcCode;
	public String MccCode;
	public String MchtNo;
	public String MessageHead5;
	public String MessageHead6;
	public String Bit127RecData;
	public String UTime;
	public String AuthDate;
	public int AuthAmt;
}
