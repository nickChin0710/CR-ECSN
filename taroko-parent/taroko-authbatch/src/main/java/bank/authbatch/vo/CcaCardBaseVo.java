package bank.authbatch.vo;

public class CcaCardBaseVo {


	public String getAcnoPSeqNo() {
		return AcnoPSeqNo;
	}

	public void setAcnoPSeqNo(String acnoPSeqNo) {
		AcnoPSeqNo = acnoPSeqNo;
	}

	public int getCardAcctIdx() {
		return CardAcctIdx;
	}

	public void setCardAcctIdx(int cardAcctIdx) {
		CardAcctIdx = cardAcctIdx;
	}

	public String getCardNo() {
		return CardNo;
	}

	public void setCardNo(String cardNo) {
		CardNo = cardNo;
	}

	public String getIdPSeqNo() {
		return IdPSeqNo;
	}

	public void setIdPSeqNo(String idPSeqNo) {
		IdPSeqNo = idPSeqNo;
	}

	public String getPSeqNo() {
		return PSeqNo;
	}

	public void setPSeqNo(String pSeqNo) {
		PSeqNo = pSeqNo;
	}

	public String getRowId() {
		return RowId;
	}

	public void setRowId(String rowId) {
		RowId = rowId;
	}

	public String getDebitFlag() {
		return DebitFlag;
	}

	public void setDebitFlag(String debitFlag) {
		DebitFlag = debitFlag;
	}

	public CcaCardBaseVo() {
		// TODO Auto-generated constructor stub
	}
	
	public String AcnoPSeqNo="";
	public String DebitFlag="";
	public String RowId="";
	public String IdPSeqNo="";
	public String PSeqNo="";
	public int CardAcctIdx=0; 
	public String CardNo="";
	
}
