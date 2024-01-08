/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql  *
*  110-01-22  V1.00.02  Justin       fix unreleased connection               *
*  110-12-23  V1.00.03  Justin     log4j1 -> log4j2                          *
*  111-01-20  V1.00.04  Justin     fix (Code Correctness: Hidden Method)     * 
******************************************************************************/
package bank.authbatch.main;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Properties;

import org.apache.logging.log4j.core.Logger;

import Dxc.Util.SecurityUtil;


public abstract class BatchProgBase implements BatchProgBaseIntf{

	//public    String   sysHome="C:/Howard/App/MegaBank/Howard/AuthBatch/Prog/MegaAuthBatchSystem/src/main/java/mega/AuthBatch";
								 
	//public    String   sysHome="C:/DXC/AuthBatch";

	public String getBankSocketServerIp() {
		return BankSocketServerIp;
	}



	public void setBankSocketServerIp(String bankSocketServerIp) {
		BankSocketServerIp = bankSocketServerIp;
	}



	public String getBankSocketServerPort() {
		return BankSocketServerPort;
	}



	public void setBankSocketServerPort(String bankSocketServerPort) {
		BankSocketServerPort = bankSocketServerPort;
	}
	public static final String G_ECS050IDFor17 = "Ecs050For17";
	public static final String G_ECS060ID = "Ecs060";
	public static final String G_ECS080ID = "Ecs080";
	public static final String G_ECS100ID = "Ecs100";
	
	protected Timestamp G_CurTimestamp = null;
	protected String sG_CurDate="", sG_CurTime="";
	
	protected String sG_ProjHome="", sG_ProgId="";
	protected String sG_CcasWorkDir="",sG_CcasInDir="",sG_CcasOutDir="",sG_CcasErrDir="";
	
	
	private static int SleepTime;
	public static int getSleepTime() {
		return SleepTime;
	}



	public static void setSleepTime(int sleepTime) {
		SleepTime = sleepTime;
	}



	public static int getPauseRun() {
		return PauseRun;
	}



	public static void setPauseRun(int pauseRun) {
		PauseRun = pauseRun;
	}



	public static int getStopRun() {
		return StopRun;
	}



	public static void setStopRun(int stopRun) {
		StopRun = stopRun;
	}
	private static int PauseRun;
	private static int StopRun;

	public BatchProgBase() throws Exception {
		// TODO Auto-generated constructor stub
		initProc();
		
	}

	
	private String getEnvParameterValue(String sP_EnvParameterName, boolean bP_RemoveLastSlash) {

		String sL_Result = System.getenv(sP_EnvParameterName);
		//String sL_ProjHome = System.getenv("PROJ_HOME");
		if (null==sL_Result)
			return "";

		if (bP_RemoveLastSlash) {
			String sL_LastChar = sL_Result.substring(sL_Result.length()-1 ,sL_Result.length());
			if (("/".equals(sL_LastChar)) || ("\\".equals(sL_LastChar)) )
				sL_Result = sL_Result.substring(0,sL_Result.length()-1);
		}
		return sL_Result;
		
	}
	
	private void InitEnvParameters() { //Proc is ccas_initRtn()
		sG_ProjHome = getEnvParameterValue("PROJ_HOME", true);
		   
		sG_CcasWorkDir = getEnvParameterValue("CCAS_WORK_DIR", true);
		if("".equals(sG_CcasWorkDir))
			sG_CcasWorkDir = sG_ProjHome + "/CcasWorkDir";
		
		sG_CcasInDir = getEnvParameterValue("CCAS_IN_DIR", true);
		if("".equals(sG_CcasInDir))
			sG_CcasInDir = sG_ProjHome + "/CcasInDir";

		
		sG_CcasOutDir = getEnvParameterValue("CCAS_OUT_DIR", true);
		if("".equals(sG_CcasOutDir))
			sG_CcasOutDir = sG_ProjHome + "/CcasOutDir";

		
		sG_CcasErrDir = getEnvParameterValue("CCAS_ERR_DIR", true);
		if("".equals(sG_CcasErrDir))
			sG_CcasErrDir = sG_ProjHome + "/CcasErrDir";

	}

	public void initProc() throws Exception {
		
		System.out.println("child class is =>" + this.getClass().getName()); //bank.AuthBatch.AuthBatch_080
		System.out.println("child class simple name is =>" + this.getClass().getSimpleName()); //AuthBatch_080
		setChildClassName(this.getClass().getName());

		
		//sG_ProjHome = getEnvParameterValue("PROJ_HOME", true);
		InitEnvParameters();
		
		// 2021/01/22: Justin: use the the connection passed from the batch program 
//		loadTextParm(sG_ProjHome);
		
		if (logger == null) {
			createLogger(sG_ProjHome);
		}
		
		G_CurTimestamp = HpeUtil.getCurTimestamp();
		sG_CurDate = HpeUtil.getCurDateStr("");
		sG_CurTime = HpeUtil.getCurTimeStr();
		
		
		
	}
	private  String OsIsWindows;
	public  String getOsIsWindows() {
		return OsIsWindows;
	}

	public  void setOsIsWindows(String osIsWindows) {
		OsIsWindows = osIsWindows;
	}

	private static  String childClassName;
	
	public  static String getChildClassName() {
		return childClassName;
	}

	public void setChildClassName(String childClassName) {
		this.childClassName = childClassName;
	}

	private static Logger   logger    = null;
	public static Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	//private Connection Db2Connection;
	private  String Db2ServerIp;
	private  String Db2ServerPort;
	private  String Db2DbName;
	private  String BankSocketServerIp="", BankSocketServerPort="";
	
	public  String getDb2DbName() {
		return Db2DbName;
	}

	public  void setDb2DbName(String db2DbName) {
		Db2DbName = db2DbName;
	}

	public  String getDb2ServerPort() {
		return Db2ServerPort;
	}

	public  void setDb2ServerPort(String db2ServerPort) {
		Db2ServerPort = db2ServerPort;
	}

	public  String getDb2UserId() {
		return Db2UserId;
	}

	public  void setDb2UserId(String db2UserId) {
		System.out.println("Db2UserId=>" + Db2UserId);
		Db2UserId = db2UserId;
	}

	public  String getDb2UserPasswd() {
		return Db2UserPasswd;
	}

	public  void setDb2UserPasswd(String db2UserPasswd) {
		Db2UserPasswd = db2UserPasswd;
	}
	private  String Db2UserId;
	private  String Db2UserPasswd;
	
	public  String getDb2ServerIp() {
		return Db2ServerIp;
	}

	public  void setDb2ServerIp(String db2ServerIp) {
		Db2ServerIp = db2ServerIp;
	}


//	private void loadTextParm(String sP_HomeDir) throws Exception {
//		
//	    String sL_AuthBatchPropertyFileFullPathName = sP_HomeDir + "/parm/AuthBatch_Parm.txt";
//	    System.out.println("-----" + sL_AuthBatchPropertyFileFullPathName + "====") ;
//	    
//	    
//
//		  // verify path
//	    sL_AuthBatchPropertyFileFullPathName = SecurityUtil.verifyPath(sL_AuthBatchPropertyFileFullPathName);
//	    Properties       props  =  new Properties();
//	    FileInputStream  fis    =  new FileInputStream(sL_AuthBatchPropertyFileFullPathName);
//	    props.load(fis);
//	    fis.close();
//
//	    
//	    setOsIsWindows(props.getProperty("OS_IS_WINDOWS").trim());
//	    setDb2ServerIp(props.getProperty("DB2_SERVER_IP").trim());
//	    setDb2ServerPort(props.getProperty("DB2_SERVER_PORT").trim());
//	    setDb2DbName(props.getProperty("DB2_DB_NAME").trim());
//	    setDb2UserId(props.getProperty("DB2_USER_ID").trim());
//	    setDb2UserPasswd(props.getProperty("DB2_USER_PASSWD").trim());
//	       
//	    setBankSocketServerIp(props.getProperty("BANK_SOCKET_SERVER_IP").trim());
//	    setBankSocketServerPort(props.getProperty("BANK_SOCKET_SERVER_PORT").trim());
//	    
//	    
//	    fis   = null;
//	    props = null;
//
//
//	}
	
	private  String refinePath(String sP_SrcPath) {
		return sP_SrcPath.replace("\\", "//");
	}
	
	private void createLogger(String sP_HomeDir) throws Exception {
		/*
		URL L_Url = Thread.currentThread().getContextClassLoader().getResource("");
		String sL_BinPath = new File(L_Url.getFile()).getCanonicalPath(); 
		String sL_ParmFilePath = sL_BinPath + "\\mega\\AuthBatch\\Parm";

	    //System.out.println("****" + sL_ParmFilePath + "---");
	    
	    String sL_AuthBatchLogPropertyFileFullPathName = sL_ParmFilePath + "\\AuthBatch_Log4j.properties";
	    */
//	    String sL_AuthBatchLogPropertyFileFullPathName = sP_HomeDir + "/parm/AuthBatch_Log4j.properties";
//
//	    if (!"TRUE".equals(getOsIsWindows().toUpperCase().trim()))
//	    	sL_AuthBatchLogPropertyFileFullPathName = refinePath(sL_AuthBatchLogPropertyFileFullPathName);
//	    
//	    PropertyConfigurator.configure(sL_AuthBatchLogPropertyFileFullPathName);
	    
	    
	    //abcd setLogger(Logger.getLogger(this.getClass()));
	       
	   return;
	}
	
	
	
	
	public  static void writeLog(String sP_ActCode, String sP_Msg) {
		String sL_ProgName = getChildClassName();
		
		Logger L_Logger = getLogger();

		try {
			if (L_Logger == null) {
				System.out.println("L_Logger is null");
				throw new Exception("L_Logger is null");
			}
		
			if ( sP_ActCode.equals("D") ) {
//				L_Logger.debug("Debug message => Program name.");
				L_Logger.debug("Program name :"+sL_ProgName+", message:" +sP_Msg); 
		    }
		    else if ( sP_ActCode.equals("I") ) {
		    	L_Logger.info("Program name :"+sL_ProgName+", message:" +sP_Msg); 
		    }
		    else if ( sP_ActCode.equals("W") ) {
		    	L_Logger.warn("Program name :"+sL_ProgName+", message:" +sP_Msg); 
		    }
		    else if ( sP_ActCode.equals("E") ) {
		    	L_Logger.error("Program name :"+sL_ProgName+", message:" +sP_Msg); 
		    }
		}
		catch ( Exception ex ) {
		    	
		}

		return;

	}
	public  boolean closeDb() {
		return AuthBatchDbHandler.closeDatabase();
	}
	
	public  boolean commitDb() {
		return AuthBatchDbHandler.commitDatabase();
	}

	public  boolean rollbackDb() {
		return AuthBatchDbHandler.rollbackDatabase();
	}
	
	public static void initialPrepareStatement(String sP_ProgId) {
		AuthBatchDbHandler.initPrepareStatement(sP_ProgId);
	}

	public  boolean setDbConn(Connection P_DbConn) {
		return AuthBatchDbHandler.setDatabaseConn(P_DbConn);
		
	}

	public static Connection getDbConnection() {
		// return AuthBatchDbHandler.Db2Connection;
	    return AuthBatchDbHandler.connections.get();
	}
	public  boolean connDb() {
		return AuthBatchDbHandler.connDatabase(getDb2ServerIp(), getDb2ServerPort(), getDb2DbName(), getDb2UserId(), getDb2UserPasswd());
	}

}
