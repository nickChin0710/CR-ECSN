package Sec;
/**
 * 110-12-27   V1.00.01  Justin          initial
 * 110-12-29   V1.00.02  Justin          change to Controller architect.
 * */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
//import java.util.Scanner;


import Dxc.Util.PwdUtil;

public class SecA010 {
	/**
	 * The usage of IS_IN_IDE is to prevent the error when readPassword(), which hides passwords, is called.
	 */
//	final boolean IS_IN_IDE = false;
	
	final String MOD_LOG = "110-12-30   V1.00.02  Justin  change to Controller architect.";
	
	final String ecsAcdpPath = "/cr/ecs/conf/ecsAcdp.properties";
	final String A_PART_NAME = "A part";
	final String B_PART_NAME = "B part";
	

	public static void main(String[] args) {
		
		if (args.length <= 0) {
			System.out.println("未指定執行動作");
			System.exit(1);
		}
		
		SecA010 secA010 = new SecA010();
		boolean result = secA010.run(args);
		if (result) {
			System.exit(0);
		}else {
			System.exit(1);
		}
	}
	
	
	private boolean run(String[] args) {
		boolean result = false;
		
		try {
			switch (args[0]) {
			case "0":
				System.out.println(MOD_LOG);
				result = true;
				break;
			case "1":
				result = printPropertyNames();
				break;
			case "2":
				if (args.length == 2)
					result = doesPropertyNameNotExist(args[1]);
				break;
			case "3":
				if (args.length == 2)
					result = doesPropertyNameExist(args[1]);
				break;
			case "4":
				if (args.length == 4)
					result = insertOrUpdateProperty(args[1], encryptPwd(args[2], args[3]), Mode.INSERT);
				break;
			case "5":
				if (args.length == 4)
					result = insertOrUpdateProperty(args[1], encryptPwd(args[2], args[3]), Mode.UPDATE);
				break;
			case "6":
				if (args.length == 2)
					result = removeProperty(args[1]);
				break;
			default:
				System.out.println("無相符合動作");
				break;
			}
		}catch (Exception e) {
			e.printStackTrace();
			result = false;
		}

		return result;
	}


	private boolean printPropertyNames() throws IOException {	
		System.out.println("==========讀取參數名==========");
		Enumeration<?> propertyNames = listPropertyNames();
		while(propertyNames.hasMoreElements()) {
			System.out.println(propertyNames.nextElement());
		}
		System.out.println("==========讀取參數名結束==========");
		return true;
	}


//	private AcdpPwd readUserInput() throws IOException {
//		AcdpPwd acdpPwd = new AcdpPwd();
//		try(Scanner scanner = new Scanner(System.in);){
//			//===========================================================
//			while(true) {
//				
//				
//				System.out.println("請輸入要新增(i),更新(u),刪除(d)參數名,或離開(q)");
//	
//				acdpPwd.setMode(getMode(scanner.nextLine()));
//				if (acdpPwd.getMode() == Mode.UNKNOWN) {
//					System.out.println("輸入錯誤");
//					continue;
//				}
//				System.out.println(String.format("目前為%s模式", acdpPwd.getMode().getChiName()));
//				break;
//			}
//			
//			//===========================================================
//			
//			if (acdpPwd.getMode() == Mode.QUIT) {
//				return acdpPwd;
//			}
//			
//			//===========================================================
//			
//			while(true) {
//				System.out.println(String.format("請輸入要%s的property name", acdpPwd.getMode().getChiName()));
//				acdpPwd.setPropertyName(convertRC(scanner.nextLine()));
//				System.out.println(acdpPwd.getPropertyName());
//				
//				switch(acdpPwd.getMode()) {
//				case INSERT:
//					if (doesPropertyNameExist(acdpPwd.getPropertyName())) {
//						System.out.println("此property name已存在");
//						return null;
//					}
//					break;
//				case UPDATE:
//					if (doesPropertyNameExist(acdpPwd.getPropertyName()) == false) {
//						System.out.println("此property name不存在");
//						return null;
//					}
//					break;
//				case DELETE:
//					if (doesPropertyNameExist(acdpPwd.getPropertyName()) == false) {
//						System.out.println("此property name不存在");
//						return null;
//					}
//					System.out.println(String.format("確定要刪除%s嗎?(確定(y),取消(n))", acdpPwd.getPropertyName()));
//					String confirm = scanner.nextLine();
//					if ("Y".equalsIgnoreCase(confirm)) {
//						return acdpPwd;
//					}else {
//						acdpPwd.setMode(Mode.QUIT);
//						return acdpPwd;
//					}	
//				default:
//					break;
//				}	
//				break;
//			}
//	
//			//===========================================================
//			
//			while(true) {
//				System.out.println(String.format("請輸入密碼的%s", A_PART_NAME));
//				acdpPwd.setAPart(readPwd(scanner));
//			    
//				System.out.println(String.format("請再輸入一次密碼的%s", A_PART_NAME));
//				acdpPwd.setAPartConfirm(readPwd(scanner));
//			    
//			    if (isPasswordValid(acdpPwd.getAPart(), acdpPwd.getAPartConfirm(), A_PART_NAME) == false) {
//					continue;
//				}
//			    
//			    break;
//			}
//	
//		    //===========================================================
//		    while(true) {
//		    	System.out.println(String.format("請輸入密碼的%s", B_PART_NAME));
//				acdpPwd.setBPart(readPwd(scanner));
//			    
//				System.out.println(String.format("請再輸入一次密碼的%s", B_PART_NAME));
//				acdpPwd.setBPartConfirm(readPwd(scanner));
//			    
//			    if (isPasswordValid(acdpPwd.getBPart(), acdpPwd.getBPartConfirm(), B_PART_NAME) == false) {
//			    	continue;
//				}
//			    
//			    break;
//		    }
//		    
//		    //===========================================================
//		    return acdpPwd;
//		}
//	}
//
//
//	private boolean modifyProperty(AcdpPwd acdpPwd) throws IOException {
//		String pwd = encryptPwd(acdpPwd);
//		switch(acdpPwd.getMode()) {
//		case INSERT:
//		case UPDATE:
//			insertOrUpdateProperty(acdpPwd.getPropertyName(), pwd, acdpPwd.getMode());
//			break;
//		case DELETE:
//			removeProperty(acdpPwd.getPropertyName());
//			break;
//		default:
//			return false;
//		}
//		return true;
//	}
//	
//	private String readPwd(Scanner scanner) {
//		String pwd = "";
//		if (IS_IN_IDE) {
//			pwd = scanner.nextLine();
//		}else {
//			char[] pass = System.console().readPassword();
//			pwd = new String(pass);
//		}
////		System.out.println(pwd);
//		return pwd;
//	}
//	
//	private String encryptPwd(AcdpPwd acdpPwd) {
//		return encryptPwd(acdpPwd.getAPart(), acdpPwd.getBPart());
//	}
	
	private String encryptPwd(String aPart, String bPart) {
		String combinedAAndBPwd = aPart + bPart;
    	PwdUtil pwdUtil = new PwdUtil();
    	String newEncryptedPwd = null;
		try {
			newEncryptedPwd = pwdUtil.encrypt(combinedAAndBPwd);			
		} catch (Exception e) {
			System.out.println("執行密碼加密發生錯誤");
			System.out.println(e.toString());
			e.printStackTrace();
			newEncryptedPwd = null;
		}
		return newEncryptedPwd;
	}

//	private String convertRC(String str) {
//		StringBuffer sb = new StringBuffer();
//		for (int i = 0; i < str.length(); i++) {
//			if (str.charAt(i) == '\r' || str.charAt(i) == '\u0008') {
//				if (sb.length() > 0) {
//					sb.deleteCharAt(sb.length()-1);
//				}
//			}else {
//				sb.append(str.charAt(i));
//			}
//		}
//		return sb.toString();
//	}
//
//	private boolean isPasswordValid(String str, String strConfirm, String partName) {
//		if (isStrEmpty(str) || isStrEmpty(strConfirm)) {
//			System.out.println(String.format("%s所輸入密碼有空值", partName));
//			return false;
//		}
//		
//		if (isPasswordTheSame(str, strConfirm)) {
//			return true;
//		}else {
//			System.out.println(String.format("%s所輸入密碼不相符", partName));
//			return false;
//		}	
//	}
//
//
//	private boolean isStrEmpty(String str) {
//		if (str == null || str.isEmpty()) {
//			return true;
//		}
//		return false;
//	}
//	
//	private boolean isPasswordTheSame(String aPart, String aPartConfirm) {
//		if (aPart.equals(aPartConfirm)) {
//			return true;
//		}
//		return false;
//	}
	
	private boolean doesPropertyNameExist(String propertyName) throws IOException {
		Properties  prop = new Properties();
		try (FileInputStream fis = new FileInputStream(ecsAcdpPath);) {
			prop.load(fis);
			return prop.containsKey(propertyName);
		}
	}
	
	private boolean doesPropertyNameNotExist(String propertyName) throws IOException {
		return !doesPropertyNameExist(propertyName);
	}


	private Enumeration<?> listPropertyNames() throws IOException {
		Properties  prop = new Properties();
		Enumeration<?> propertyNames = null;
		try (FileInputStream fis = new FileInputStream(ecsAcdpPath);) {
			prop.load(fis);
			propertyNames = prop.propertyNames();
		}
		return propertyNames;
	}

	private boolean insertOrUpdateProperty(String key, String val, Mode mode) {
		boolean result = false;
		
		Properties  prop = new Properties();
		
		try (FileInputStream fis = new FileInputStream(ecsAcdpPath);) {
			prop.load(fis);
			prop.setProperty(key, val);
			try (FileOutputStream fos = new FileOutputStream(ecsAcdpPath);) {
				prop.store(fos, String.format("%s[%s]", mode.getEngName(), key));
				result = true;
				System.out.println(String.format("成功%sproperty[%s]", mode.getChiName(), key));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	private boolean removeProperty(String key) {
		boolean removeResult = false;
		
		Properties  prop = new Properties();
		
		try (FileInputStream fis = new FileInputStream(ecsAcdpPath);) {
			prop.load(fis);
			prop.remove(key);
			try (FileOutputStream fos = new FileOutputStream(ecsAcdpPath);) {
				prop.store(fos, String.format("%s[%s]", Mode.DELETE.getEngName(), key));
				removeResult = true;
				System.out.println(String.format("成功刪除property[%s]", key));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return removeResult;
	}
	
	public Mode getMode(String mode){
		if (mode == null) {
			return Mode.UNKNOWN;
		}
		
		switch (mode.toUpperCase(Locale.TAIWAN)) {
		case "I":
			return Mode.INSERT;
		case "U":
			return Mode.UPDATE;
		case "D":
			return Mode.DELETE;
		case "Q":
			return Mode.QUIT;
		default:
			return Mode.UNKNOWN;
		}
		
	}

}

class AcdpPwd{
	private Mode mode = Mode.UNKNOWN;
	private String propertyName = "";
	private String aPart = "";
	private String aPartConfirm = "";
	private String bPart = "";
	private String bPartConfirm = "";
	
	public Mode getMode() {
		return mode;
	}
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public String getAPart() {
		return aPart;
	}
	public void setAPart(String aPart) {
		this.aPart = aPart;
	}
	public String getAPartConfirm() {
		return aPartConfirm;
	}
	public void setAPartConfirm(String aPartConfirm) {
		this.aPartConfirm = aPartConfirm;
	}
	public String getBPart() {
		return bPart;
	}
	public void setBPart(String bPart) {
		this.bPart = bPart;
	}
	public String getBPartConfirm() {
		return bPartConfirm;
	}
	public void setBPartConfirm(String bPartConfirm) {
		this.bPartConfirm = bPartConfirm;
	}
}

enum Mode{
	INSERT("新增", "Insert"), 
	UPDATE("更新", "Update"), 
	DELETE("刪除", "Delete"), 
	QUIT("離開", "Quit"),
	UNKNOWN("未知", "Unknown");
	private String chiName = null;
	private String engName = null;
	
	Mode(String chiName, String engName){
		this.chiName = chiName;
		this.engName = engName;
	}

	public String getChiName() {
		return chiName;
	}
	public String getEngName() {
		return engName;
	}
}
