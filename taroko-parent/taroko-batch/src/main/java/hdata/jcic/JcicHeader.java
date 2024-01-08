package hdata.jcic;

public class JcicHeader {
	private String fileId = null;
	private String bankNo = null;
	private String filler1 = null;
	private String sendDate = null;
	private String fileExt = null;
	private String filler2 = null;
	private String contactTel = null;
	private String contactMsg = null;
	private String filler3 = null;
	private String len = null;

	public String produceStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(fileId);
		sb.append(bankNo);
		sb.append(filler1);
		sb.append(sendDate);
		sb.append(fileExt);
		sb.append(filler2);
		sb.append(contactTel);
		sb.append(contactMsg);
//		sb.append(filler3);
//		sb.append(len);
		return sb.toString();
	}
	
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	public String getFiller1() {
		return filler1;
	}
	public void setFiller1(String filler1) {
		this.filler1 = filler1;
	}
	public String getSendDate() {
		return sendDate;
	}
	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}
	public String getFileExt() {
		return fileExt;
	}
	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}
	public String getFiller2() {
		return filler2;
	}
	public void setFiller2(String filler2) {
		this.filler2 = filler2;
	}
	public String getContactTel() {
		return contactTel;
	}
	public void setContactTel(String contactTel) {
		this.contactTel = contactTel;
	}
	public String getContactMsg() {
		return contactMsg;
	}
	public void setContactMsg(String contactMsg) {
		this.contactMsg = contactMsg;
	}
	public String getFiller3() {
		return filler3;
	}
	public void setFiller3(String filler3) {
		this.filler3 = filler3;
	}
	public String getLen() {
		return len;
	}
	public void setLen(String len) {
		this.len = len;
	}
	


}
