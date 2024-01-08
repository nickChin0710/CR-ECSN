package bank.authbatch.vo;

public class CrdIdnoVo {

	public String getCardSince() {
		return CardSince;
	}

	public void setCardSince(String cardSince) {
		CardSince = cardSince;
	}

	public String getJobposition() {
		return Jobposition;
	}

	public void setJobposition(String jobposition) {
		Jobposition = jobposition;
	}

	public String CardSince=""; /* 首次持卡日期           */
	public String Jobposition="";//職稱
	
	public CrdIdnoVo() {
		// TODO Auto-generated constructor stub
	}

}
