package taroko.com;
/**
 * 2020-0211   JH    getInstance()
 * 2020-0612 JUSTIN  ecs_login -> ad_ecs_login and ecsLogin -> adEcsLogin
 * 2020-0713 JUSTIN  ++adSysName, adEcsLogin -> adLogin, remove ssoURL
 * 2020-0722 JUSTIN  ++adDomainName, remove adSysName, and change adEcsLogin into adLogin
 * 109-08-14  V1.00.01  Zuwei        fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
 * 109-09-03 V1.00.02 yanghan 	  修改了關閉流的方式  -86行
 * 109-09-04 V1.00.03 Zuwei 	      變數instance增加volatile關鍵字約束
 * 109-09-29  V1.00.04  JustinWu     fix bugs of port and ssl 
 * 109-10-06  V1.00.10  JustinWu     fix the compatibility of HTTP and HTTPS  
 * 109-10-28  V1.00.11  JustinWu     add LOG_ENCRYPTED
 * 110-02-22  V1.00.12  JustinWu     fix a code scan issue: JNDI Reference Injection
 * 110-02-24  V1.00.13  JustinWu     select AD Server value from DB 
 * 110-02-26  V1.00.14  JustinWu     add the function of deleting log files and fix a synchronized problem
 * 110-03-01  V1.00.15  JustinWu     check if the AD IP and the domain name are not empty 
 * 110-03-02  V1.00.16  JustinWu     fix a unreleased resource problem
 * 110-03-08  V1.00.17  JustinWu     process PDPA log files
 * 110-03-26  V1.00.18  JustinWu     add resourceNameWhiteList
 * 110-04-08  V1.00.19  JustinWu     add dupLoginFlag
 * 110-04-13  V1.00.20  JustinWu     add system path
 * 110-07-12  V1.01.00  JustinWu     rewrite this most parts of this program
 * 110-09-30  V1.01.01  JustinWu     fix Double-Checked Locking
 * 110-10-21  V1.01.02  JustinWu     fix the bugs about workDir
 * 110-11-10  V1.01.03  JustinWu     extract AD Server Number as parameters
 * 110-11-30  V1.01.04  JustinWu     make setLdapAndLogParm() public, and setWrokDir() before getProperties()
 * 110-12-14  V1.01.05  JustinWu     增加data source白名單，以及輸出訊息
 * */
import java.util.*;
import java.util.concurrent.Executors;

import javax.servlet.ServletContext;

import Dxc.Util.SecurityUtil;
import taroko.base.BaseData;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class TarokoParm {

//  private static TarokoParm instance = new TarokoParm();
	
  /* SINGLETON INSTANCE*/
  private static TarokoParm instance =null;
  
  /* CONSTANT PARAMETERS */
  static public final String LOG_FOLDER_PATH = "/waslog/EcsWeb";
  static public final String LDAP_URL_WF_TYPE = "SEC_LDAP_URL";
  static public final String AD_DOMAIN_WF_KEY = "SEC_AD_DOMAIN";
  static public final String DB_SWITCH_TO_DR = "dbSwitch2Dr";
  
  /* RESOURCE NAME WHITE LIST */
  static private final HashSet<String> RESOURCE_NAME_WHITELIST = 
		  new HashSet<String>(Arrays.asList(
				  "jdbc/crdbconn", "jdbc/dcdbconn", 
				  "java:comp/env/jdbc/crdbconn", 
				  "jdbc/Taroko_SIT", "java:comp/env/jdbc/Taroko_SIT", 
				  "java:comp/env/jdbc/crdbconn_cr", "java:comp/env/jdbc/crdbconn_mg"));

  /* SYSTEM PATH PARAMETERS */
  private String rootDir = null;
  private String htmlDir = null;
  private String dataRoot = null;
  private String workDir = null;
  
  /* RELOAD PARAMETER */
  private String  processWorkAndLogDate   = "";

  /* LOG PARAMETERS */
//  private int logReserveDay = 0;       // 0: 不刪檔
//  private String logDeleteFlag = "N";  // Y: 開啟刪除檔功能
  private String logEncrypted = "";    // 是否log檔要加密
  
  /* LOGIN RELATED PARAMETERS */
  static public final int TAIPEI_AD_NUM = 3;
  static public final int TAUCHUNG_AD_NUM = 2;
  private String adLogin = null;
  private String adDomainName="";
  private String[] ldapUrlArr = new String[TAIPEI_AD_NUM + TAUCHUNG_AD_NUM]; // ldapUrl: Taipei 1, 2, 3 and Taichung 1, 2, 3
  private boolean dupLoginFlag = true;
  
  /* DB SWITCH TO DR PARAMETER */
  private String dbSwitch2Dr = "";

  /* DB related parameters */
  private  int connCount = 0;
  private  String[] connName = {"", ""};
  private  String[] resourceName = {"", ""};
  private  String[] dbType = {"", ""};
  
  /* OTHER PARAMETERS  */
  private  String debugMode = "";
  private  String cssVersion = "";
  private  String jsVersion = "";
  private  float  warningSec = 6;
  
  private TarokoParm() {}

  public static TarokoParm getInstance() {
	  synchronized (TarokoParm.class) {
		  if (instance == null) {
			  instance = new TarokoParm();
		  }
	  }
	  return instance;
  }

/**
 * set system parameters
 * @param servletContext
 * @throws Exception
 */
public void setSystemParm(ServletContext servletContext) throws Exception {
	setSystemPath(servletContext);
	setDbSwitch2Dr(servletContext);
	setWorkDir();
	getProperties(dataRoot);
	new BaseData().initialLogger();
}

/**
 * get properties and then set parameters 
 * @param dataRoot
 * @throws Exception
 */
public void getProperties(String dataRoot) throws Exception {
	Properties props = loadProperty(dataRoot);
	setWarningSec(props);
	setDebugMode(props);
	setCssVersion(props);
	setJsVersion(props);
	setDupLoginFlag(props);
	setLogEncrypted(props);
	setDbRelatedInfo(props);
	setAdLogin(props);
	setAdDomainName(props);
	setLdapAndLogParm();
}

private void setAdDomainName(Properties props) {
	adDomainName = props.getProperty("AD_DOMAIN_NAME");
      if (adDomainName==null) adDomainName = "";
}

private void setAdLogin(Properties props) {
	adLogin = props.getProperty("AD_LOGIN");
      if ( adLogin == null ) adLogin = "N";
}

/**
 * set resource names, connection names, the count of connections, and database types
 * @param props
 * @throws Exception
 */
private void setDbRelatedInfo(Properties props) throws Exception {
	int connCnt = 0;
	for (int i = 1; i <= 10; i++) {
		resourceName[i - 1] = checkResourceName(props.getProperty("RESOURCE_NAME_" + i));
		connName[i - 1] = props.getProperty("CONN_NAME_" + i);
		dbType[i - 1] = props.getProperty("DB_TYPE_" + i);
		if (resourceName[i - 1] == null) {
			break;
		}
		connCnt++;
	}

	connCount = connCnt;

	if (resourceName[0] == null || resourceName[0].trim().length() == 0) {
		throw new Exception("TarokoParameter.txt的RESOURCE_NAME未被設定或不在白名單內");
	}
}

private void setLogEncrypted(Properties props) {
	logEncrypted = props.getProperty("LOG_ENCRYPTED");
	if (logEncrypted == null) {
		logEncrypted = "";
	}
}

public void setLogEncrypted(String logEncrypted) {
	this.logEncrypted = logEncrypted;
}

private void setDupLoginFlag(Properties props) {
	String tempDupLoginFlag = props.getProperty("DUP_LOGIN_FLAG");
      if (tempDupLoginFlag != null && "N".equals(tempDupLoginFlag)) {
    	  dupLoginFlag  = false;
	  }else {
		  dupLoginFlag = true;
	  }
}

private void setJsVersion(Properties props) {
	jsVersion = props.getProperty("JS_VERSION");
	if (jsVersion == null) {
		jsVersion = "1.0";
	}
}

private void setWarningSec(Properties props) {
	warningSec = Float.parseFloat(props.getProperty("IO_WARN_SECOND"));
}

private void setDebugMode(Properties props) {
	debugMode  = props.getProperty("LOG_DEBUG");
}

private void setCssVersion(Properties props) {
	cssVersion = props.getProperty("CSS_VERSION");
		if (cssVersion == null) {
			cssVersion = "1.0";
		}
}

private Properties loadProperty(String dataRoot) throws IOException, FileNotFoundException {
	Properties props = new Properties();
	String parmFile = dataRoot + "/parm/TarokoParameter.txt";
	// verify path
	parmFile = SecurityUtil.verifyPath(parmFile);
	try (FileInputStream fis = new FileInputStream(parmFile);) {
		props.load(fis);
	}
	return props;
}

/**
 * set LDAP and logs parameters selected from the database
 * @throws Exception
 */
public void setLdapAndLogParm() throws Exception {
	boolean isSelectADSuccessful = false;
	if (connCount > 0) {
		try {
			try (Connection conn = ConnectionManager.getConnection();) {
				if (conn == null) {
					throw new Exception("無法取得DB連線");
				}
				conn.setAutoCommit(false);
				conn.setNetworkTimeout(Executors.newFixedThreadPool(1), 120000);
				
				// set LDAP server IPs and domain name
				if ("Y".equals(adLogin)) {
					isSelectADSuccessful = setLdapIPAndDomain(conn);
				}
				
//				//set log parameters
//				setLogParm(conn);
				
			}
		} catch (Exception ex) {
			System.out.println("<<setLdapAndLogParm>> err:" + ex.getMessage());
		}
	}
	
	if ("Y".equalsIgnoreCase(adLogin) && (adDomainName.trim().length() == 0 || isSelectADSuccessful == false)) {
		throw new Exception("目前為AD登入模式，請設定AD domain name及AD Server IP參數 ");
	}

}

//	private boolean setLogParm(Connection conn) {
//		boolean isSelect = false;
//		try {
//			StringBuilder sb = new StringBuilder();
//			sb.append(" SELECT WF_VALUE, WF_VALUE2  ")
//			  .append(" FROM PTR_SYS_PARM ")
//			  .append(" WHERE WF_PARM='SYSPARM' AND upper(WF_KEY) ='TAROKOPARM_LOG4J' ");
//
//			String sql = sb.toString() ;
//			String logDeleteFlag = "N";
//			int logReserveDay = 0;
//			try (PreparedStatement ps = conn.prepareStatement(sql); ) {
//				try(ResultSet rs = ps.executeQuery();){
//					int selectCnt = 0;	
//					while (rs.next()) {
//						logDeleteFlag = rs.getString("WF_VALUE");
//						logReserveDay = rs.getInt("WF_VALUE2");
//						isSelect = true;
//						selectCnt++;
//						if (selectCnt == 1) {
//							break;
//						}
//					}
//					System.out.println(String.format("logDeleteFlag[%s], logReserveDay[%s]", logDeleteFlag, logReserveDay));	
//				}
//			}
//			
//			this.logDeleteFlag = logDeleteFlag;
//			this.logReserveDay = logReserveDay;
//			
//			if (isSelect) {
//				conn.commit();
//			} else {
//				conn.rollback();
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				e1.printStackTrace();
//			}
//		} 
//		return isSelect;
//
//	}
	
	/**
	 * 取得 AP-SERVER 根目錄及參數根目錄
	 * @param servletConfig
	 */
	public void setSystemPath(ServletContext servletContext) {
		  String tmpRootDir = servletContext.getRealPath("/");
	      String tmpHtmlDir = tmpRootDir + "/html/";
	      String tmpDataRoot = tmpRootDir + "/WebData";      
//	      dataRoot = getInitParameter("web-data-file"); /* 取得 WEB-INF 參數 */
	      
//	      // Justin 2021/08/05 Not differentiate between local and server environments
//		  if (tmpRootDir.substring(1, 2).equals(":")) {
//			  tmpDataRoot = servletContext.getInitParameter("web-data-file");
//			  tmpDataRoot = (tmpDataRoot == null) ? "/cr/EcsWeb/data": tmpDataRoot;
//			  tmpDataRoot = tmpRootDir.substring(0, 2) + tmpDataRoot;
//		  }
	      
	      rootDir = tmpRootDir;
	      htmlDir = tmpHtmlDir;
	      dataRoot = tmpDataRoot;
		
	}

/**
   * 確認resource name是否在白名單內
   * @param resourceNameFromTxt
   * @return
   */
private String checkResourceName(String resourceNameFromTxt) {
	 if (resourceNameFromTxt != null) {
		if (RESOURCE_NAME_WHITELIST.contains(resourceNameFromTxt) == false) {
			System.out.println(String.format("resourceName[%s]不存在白名單內", resourceNameFromTxt));
			resourceNameFromTxt = null;
		}
	 }
	return resourceNameFromTxt;
}

public boolean setLdapIPAndDomain(Connection conn) {
	boolean isSelect = false;
	try {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT WF_DESC , ")
			.append(" (SELECT WF_VALUE  ")
			.append(" FROM PTR_SYS_PARM ")
			.append(" WHERE WF_PARM = 'SYSPARM'  ")
			.append(String.format(" AND WF_KEY ='%s' ) as WF_VALUE ", AD_DOMAIN_WF_KEY))
			.append(" FROM PTR_SYS_IDTAB ")
			.append(String.format(" WHERE WF_TYPE ='%s' ", LDAP_URL_WF_TYPE));
		if ("Y".equals(getDbSwitch2Dr())) {
			sb.append(" AND ID_CODE = ? ");
		}
		sb.append(" ORDER BY WF_ID ");
		String sql = sb.toString() ;
		
		try (PreparedStatement ps = conn.prepareStatement(sql); ) {
			if ("Y".equals(getDbSwitch2Dr())) {
				ps.setString(1, getDbSwitch2Dr());
			}
			int selectCnt = 0;
			try(ResultSet rs = ps.executeQuery();){
				while (rs.next()) {
					String ldapUrl = rs.getString("WF_DESC");
					isSelect = true;
					selectCnt++;
					adDomainName = rs.getString("WF_VALUE");
					ldapUrlArr[selectCnt-1] = ldapUrl;
					if (selectCnt == (TAIPEI_AD_NUM + TAUCHUNG_AD_NUM)) {
						break;
					}
				}
				if (ldapUrlArr[0] == null || ldapUrlArr[0].trim().length() == 0 
					 ||	adDomainName == null || adDomainName.trim().length() == 0 ) {
					isSelect = false;
				}else {
					for (int i = 1; i <= selectCnt; i++) {
						System.out.println(String.format("AD SERVER_%s IP[%s]", i, ldapUrlArr[i-1]));
					}
					System.out.println(String.format("AD Domain[%s]", adDomainName));
				}
				
				
			}

		}
		
		if (isSelect) {
			conn.commit();
		} else {
			conn.rollback();
		}
		
	} catch (Exception e) {
		e.printStackTrace();
		try {
			conn.rollback();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	} 
	return isSelect;
  }
	
	public void setDataRoot(String dataRoot) {
		this.dataRoot = dataRoot;
	}
	
	public String getDataRoot() {
		return dataRoot;
	}
	
	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}
	
	private void setWorkDir() {
		String tmpWorkDir = dataRoot + "/work/";
//	    if (!resourceName[0].substring(0, 4).equals("java")) {
//	      tmpWorkDir = dataRoot + "/WebData/work/";
//	    }
	    workDir = tmpWorkDir;
	}
	
	public String getWorkDir() {
		return workDir;
	}
	
	/**
	 * 取得 DB是否已經切換到DR Site的參數
	 * @param servletContext
	 */
	private void setDbSwitch2Dr(ServletContext servletContext) {
		dbSwitch2Dr =(String) servletContext.getAttribute(TarokoParm.DB_SWITCH_TO_DR);	
	}
	
	public void setDbSwitch2Dr(String dbSwitch2Dr) {
		this.dbSwitch2Dr = dbSwitch2Dr;
	}
	
	public String getDbSwitch2Dr() {
		return dbSwitch2Dr;
	}
	
	public String getAdLogin() {
		return adLogin;
	}
	
	public String getAdDomainName() {
		return adDomainName;
	}
	
	public void setAdDomainName(String adDomainName) {
		this.adDomainName = adDomainName;
	}
	
	public String[] getLdapUrlArr() {
		return ldapUrlArr;
	}
	
	public boolean getDupLoginFlag() {
		return dupLoginFlag;
	}
	
	public String getRootDir() {
		return rootDir;
	}

	public String getHtmlDir() {
		return htmlDir;
	}

	public String getProcessWorkAndLogDate() {
		return processWorkAndLogDate;
	}

//	public int getLogReserveDay() {
//		return logReserveDay;
//	}

//	public String getLogDeleteFlag() {
//		return logDeleteFlag;
//	}

	public String getLogEncrypted() {
		return logEncrypted;
	}

	public int getConnCount() {
		return connCount;
	}

	public String[] getConnName() {
		return connName;
	}

	public String[] getResourceName() {
		return resourceName;
	}

	public String[] getDbType() {
		return dbType;
	}

	public String getDebugMode() {
		return debugMode;
	}

	public String getCssVersion() {
		return cssVersion;
	}

	public String getJsVersion() {
		return jsVersion;
	}

	public float getWarningSec() {
		return warningSec;
	}

	public void setProcessWorkAndLogDate(String processWorkAndLogDate) {
		this.processWorkAndLogDate = processWorkAndLogDate;
		
	}

}
