package bank.authbatch.vo;

public class CardAcctIndexVo {

	public int getCardAcctIdx() {
		return CardAcctIdx;
	}

	public void setCardAcctIdx(int cardAcctIdx) {
		CardAcctIdx = cardAcctIdx;
	}

	public int getAcctParentIdx() {
		return AcctParentIdx;
	}

	public void setAcctParentIdx(int acctParentIdx) {
		AcctParentIdx = acctParentIdx;
	}

	public String getPSeqNo() {
		return PSeqNo;
	}

	public void setPSeqNo(String pSeqNo) {
		PSeqNo = pSeqNo;
	}



	public String getCardAcctId() {
		return CardAcctId;
	}

	public void setCardAcctId(String cardAcctId) {
		CardAcctId = cardAcctId;
	}

	public String getCardCorpId() {
		return CardCorpId;
	}

	public void setCardCorpId(String cardCorpId) {
		CardCorpId = cardCorpId;
	}


	public int CardAcctIdx=0;
	public int AcctParentIdx=0;
	public String CardAcctId="";
	public String CardCorpId="";
	//public String TransType1="";
	//public String TransType2="";
	//public String DebitFlag="";
	public String PSeqNo="";
	//public String IdPSeqNo="";
	
	public CardAcctIndexVo() {
		// TODO Auto-generated constructor stub
	}

}
