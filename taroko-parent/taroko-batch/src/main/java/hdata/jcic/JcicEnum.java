package hdata.jcic;

public enum JcicEnum {
	JCIC_KK1("JCIC_KK1", "JCIC-DAT-KK01-V01-"),
	JCIC_KK2("JCIC_KK2", "JCIC-DAT-KK02-V01-"),
	JCIC_KK3("JCIC_KK3", "JCIC-DAT-KK03-V01-"),
	JCIC_KK4("JCIC_KK4", "JCIC-DAT-KK04-V01-"),
	JCIC_KK5("JCIC_KK5", "JCIC-DAT-KK05-V01-"),
	JCIC_KK8("JCIC_KK8", "JCIC-DAT-KK08-V01-"),
	JCIC_KKS1("JCIC_KKS1", "JCIC-DAT-KKS1-V01-"),
	JCIC_S01("JCIC_S01", "JCIC-DAT-S001-V01-"),
	JCIC_S02("JCIC_S02", "JCIC-DAT-S002-V01-"),
	JCIC_Z13("JCIC_Z13", "JCIC-DAT-ZA02-V01-"),
	JCIC_Z55("JCIC_Z55", "JCIC-DAT-Z055-V01-"),
	JCIC_Z56("JCIC_Z56", "JCIC-DAT-Z056-V01-"),
	JCIC_Z582("JCIC_Z582", "JCIC-DAT-Z582-V01-"),
	JCIC_J10("JCIC_J10", "JCIC-INQ-BARE-J10-"),
	JCIC_901("JCIC_901", "JCIC-DAT-901-V01-"),
	JCIC_902("JCIC_902", "JCIC-DAT-902-V01-"),
	JCIC_APK1("JCIC_APK1", "JCIC-DAT-APK1-V01-")
	;
	private String jcicType = null;
	private String jcicId = null;
	JcicEnum(String jcicType, String jcicId){
		this.jcicType = jcicType;
		this.jcicId  = jcicId;
	}
	public String getJcicType() {
		return jcicType;
	}
	public void setJcicType(String jcicType) {
		this.jcicType = jcicType;
	}
	public String getJcicId() {
		return jcicId;
	}
	public void setJcicId(String jcicId) {
		this.jcicId = jcicId;
	}
	
}
