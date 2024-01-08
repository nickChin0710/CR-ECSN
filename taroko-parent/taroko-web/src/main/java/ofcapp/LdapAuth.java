/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei         updated for project coding standard      *
*  109-04-21  V1.00.01  Zuwei         code format                              *
*  109-06-17  V1.00.02  JustinWu   add some methods checking the user and the password using LDAP    
*  109-07-02  V1.00.03  JustinWu   add get bank_unitno              * 
*  109-07-14  V1.00.04  JustinWu   adEcsLogin -> adLogin 
*  109-07-15  V1.00.05  JustinWu   change the String of memberOf from AD to upper case
*  109-07-16  V1.00.06  JustinWu   declare some variables static
*  109-07-22  V1.00.07  JustinWu   add a parameter adDomainName for switching different environments
*  109-07-27  V1.00.08  JustinWu   add getDepartmentNo()
*  109-08-03  V1.00.09  Zuwei       fix code scan issue                       *
*  109-08-07  V1.00.10  JustinWu   trim displayName from AD and fix some bugs of getting groups
*  109-09-15  V1.00.11  JustinWu   cancel update group 
*  109-09-16  V1.00.12  JustinWu   add log
*  110-02-23  V1.00.13  JustinWu   ldapUrl -> ldapUrlArr
*  110-11-10  V1.00.14  JustinWu   extract some variables
*  110-11-30  V1.00.15  JustinWu   reload AD Server IP when they are empty
*  110-12-06  V1.00.16  JustinWu   display more specific error messages
*  110-12-17  V1.00.17  JustinWu   change the methods of encryption and decryption
******************************************************************************/
package ofcapp;

import javax.naming.Context;
import javax.naming.NamingEnumeration;

import busi.DbAccess;
import busi.SqlPrepare;
import busi.func.EcsLoginFunc;
import taroko.com.TarokoParm;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;


public class LdapAuth extends DbAccess{
	taroko.com.TarokoCommon wp = null;
	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	
	// AD node
	static private final String USER_NODE = "OU=TCBUsers";
	static private final String GROUP_NODE = "OU=TCBGroups";
	
	// the columns of the mapping table
	static private final String MEMBER_OF_GROUP = "memberOfGroup";
	static private final String USER_GROUP = "userGroup";
	static private final String USER_LEVEL = "userLevel";
	
	// AD selected node attributes
	static private final String AD_USER_KEY = "sAMAccountName", DISPLAY_NAME = "displayName";
	static private final String MEMBER_OF = "memberOf";
	static private final String DISTINGUISHED_NAME = "distinguishedName";
	static private final String DEPARTMENT_NUMBER = "departmentNumber";
	
	static private final String[] SELECTED_ATTRS_ARR = 
		{ AD_USER_KEY, DISPLAY_NAME, MEMBER_OF, DISTINGUISHED_NAME, DEPARTMENT_NUMBER};
	
	private String domainNameNode = "";  
	
	private String errmsg = "";

	public LdapAuth(taroko.com.TarokoCommon wp) {
		this.wp = wp;
		setConn(wp.getConn());
		
		domainNameNode = getDomainNameNodeStr(TarokoParm.getInstance().getAdDomainName());
		
	}

	/**
	 * if adDomainName = tcbt.com => return  ",DC=tcbt,DC=com"
	 * @param adDomainName
	 * @return
	 */
	private String getDomainNameNodeStr(String adDomainName) {
		
		String[] domainNameArr = adDomainName.split("\\.");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < domainNameArr.length; i++) {
			sb.append(String.format(",DC=%s", domainNameArr[i]));
		}
		return sb.toString();
	}

	private void okMsg() {
		errmsg = "";
	}

  public String mesg() {
    return errmsg;
  }

	/**
	 *  1. 若指定帳號密碼經ldap有認證成功，則
	 *      (1) 若sec_user資料表中無此使用者資料，則新增此筆資料
	 *      (2) 若sec_user資料表已有此使用者資料，則更新此筆資料
	 * @param user
	 * @param password
	 * @return
	 */
	public boolean checkLoginPasswd(String user, String password) {
		boolean isLogin = true;
		
		wp.log("-->LDAP: start to check the user and the password in AD .....");

		if (commString.empty(user) || commString.empty(password)) {
			wp.alertMesg("使用者代碼, 密碼: 不可空白");
			return false;
		}

		// 使用LDAP認證使用者帳號及密碼。 若認證成功，則需要新增或更新資料；若認證、新增或更新失敗，則會回傳false。
		return checkPasswordByLdap(TarokoParm.getInstance().getLdapUrlArr(), user, password, isLogin);

  }
	
	/**
	 * 使用LDAP認證使用者帳號及密碼。 <br>
	 * 若認證成功，則(1)如果isLogin為true，則要新增或更新資料(2)否則直接回傳true<br>
	 * 若認證、新增或更新失敗，則會回傳false。
	 * @param ldapUrlArr
	 * @param acct
	 * @param secret
	 * @param isLogin 是否是登入時的帳號密碼認證。(true:登入認證，false:覆核認證)
	 * @return
	 */
	public boolean checkPasswordByLdap(String[] ldapUrlArr, String acct, String secret, boolean isLogin, int noOfLdap) {
		if (ldapUrlArr[noOfLdap-1] == null || ldapUrlArr[noOfLdap-1].length() == 0) {
			try {
				// 重新查詢AD Server IP以及Log參數(因若AD IP在伺服器開啟時查詢不成功，通常Log參數查詢也不會成功)
				TarokoParm.getInstance().setLdapAndLogParm();
				if (ldapUrlArr[noOfLdap-1] == null || ldapUrlArr[noOfLdap-1].length() == 0) {
					throw new Exception("AD Server IP 為空白，重新查詢後仍為空白");
				}
			} catch (Exception e) {
				e.printStackTrace();
				errmsg = "AD Server 無法連線( AD Server IP 為空白 )";
				return false;
			}
		}
		boolean isSucced = false;
	
		String ldapAcct = String.format("%s@%s", acct, TarokoParm.getInstance().getAdDomainName());
		
//        // test in local AD SERVER
//		String ldapAcct = "JOHNC".equals(acct) ? "cn=JOHNC,ou=AP4,ou=A01419,ou=TCBUsers,dc=tcb,dc=com" 
//				                 : String.format("cn=%s,ou=3144,ou=TCBUsers,dc=tcb,dc=com", acct);
	
		InitialLdapContext ctx = null;
		
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, String.format("ldap://%s:389", ldapUrlArr[noOfLdap-1]));
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, ldapAcct);
		env.put(Context.SECURITY_CREDENTIALS, secret);
		env.put("com.sun.jndi.ldap.connect.timeout", "10000"); // set timeout 10 seconds
	
		try {
			// search for the user by user principal name(UPN)
			ctx = new InitialLdapContext(env, null);
			wp.showLogMessage("I", "", String.format("%s successfully logs in with AD Server_%s[%s]", acct, noOfLdap, ldapUrlArr[noOfLdap-1]));
	
			if (isLogin) {
				insertOrUpdateSecUser(acct, secret, ctx);
			}
	
			isSucced = true;
	
		} catch (javax.naming.AuthenticationException e) {
			isSucced = false;
			wp.showLogMessage("I", "",String.format("Successfully connect AD Server_%s[%s]", noOfLdap, ldapUrlArr[noOfLdap-1]));
			errmsg = "行員AD帳號 [ " + acct + " ]或密碼錯誤";
			wp.showLogMessage("I", "",errmsg);
		} catch (javax.naming.CommunicationException e) {
			isSucced = false;
			wp.showLogMessage("I", "", String.format("Unsuccessfully connect AD Server_%s[%s] ", noOfLdap, ldapUrlArr[noOfLdap-1]));
			if ("Y".equals(TarokoParm.getInstance().getDbSwitch2Dr())) {
				if (noOfLdap < TarokoParm.TAUCHUNG_AD_NUM) {
					return checkPasswordByLdap(ldapUrlArr, acct, secret, isLogin, noOfLdap+1);
				}
			}else {
				if (noOfLdap < (TarokoParm.TAIPEI_AD_NUM + TarokoParm.TAUCHUNG_AD_NUM) ) {
					return checkPasswordByLdap(ldapUrlArr, acct, secret, isLogin, noOfLdap+1);
				}
			}
			errmsg = "連線AD Server失敗";
		} catch (Exception e) {
			isSucced = false;
			errmsg = "系統錯誤，error=" + e.getMessage();
			wp.showLogMessage("I", "", errmsg);
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					isSucced = false;
					errmsg = "系統錯誤，error=" + e.getMessage();
					wp.showLogMessage("I", "", errmsg);
				}
			}
		}
		return isSucced;
	}

	/**
	 * 使用LDAP認證使用者帳號及密碼。 <br>
	 * @param ldapUrlArr
	 * @param acct
	 * @param secret
	 * @param isLogin 是否是登入時的帳號密碼認證。(true:登入認證，false:覆核認證)
	 * @return
	 */
	public boolean checkPasswordByLdap(String[] ldapUrlArr, String acct, String secret, boolean isLogin) {
		return checkPasswordByLdap(ldapUrlArr, acct, secret, isLogin, 1);
	}
	
	/**
	 * 單純使用LDAP確認帳號密碼。
	 * @param ldapUrlArr
	 * @param acct
	 * @param secret
	 * @return
	 */
	public boolean checkPasswordByLdap(String[] ldapUrlArr, String acct, String secret) {
		return checkPasswordByLdap(ldapUrlArr, acct, secret, false);
	}

	private SearchControls getSearchControls() {
		SearchControls searchControl = new SearchControls();
		searchControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControl.setReturningAttributes( SELECTED_ATTRS_ARR );
		return searchControl;
	}

	private String getHideRefCode() throws UnsupportedEncodingException {
		return comm.generateSaltStr();
	}
	
	/**
	 * 抓取AD中的USER資料
	 * @param userName
	 * @param ctx
	 * @param searchControls
	 * @return
	 * @throws Exception 
	 * @throws NamingException 
	 */
	private UserData getUserInfo(String userName, InitialLdapContext ctx, SearchControls searchControls)
			throws NamingException, Exception {
		
		UserData userData = new UserData();

		NamingEnumeration<SearchResult> answer = 
				ctx.search(USER_NODE + domainNameNode, String.format("%s=%s", AD_USER_KEY, userName), searchControls);

		if (answer.hasMore()) {
			Attributes attrs = answer.next().getAttributes();

			userData.userChineseName = getDisplayName(attrs.get(DISPLAY_NAME));
			userData.userType = "4"; // 一般使用者
			
			boolean isSetSuccess = false;
			
			isSetSuccess = setLevelAndGroup(attrs.get(MEMBER_OF), userData);	
			if ( ! isSetSuccess) 
				throw new Exception(errmsg);		

			isSetSuccess = getAndSetDepNoAndBankUnitNo(attrs.get(DISTINGUISHED_NAME), attrs.get(DEPARTMENT_NUMBER),userData,  USER_NODE + domainNameNode);
			if ( ! isSetSuccess) {
				if (userData.bankUnitNo != null && userData.bankUnitNo.isEmpty() == false) {
					throw new Exception(String.format("AD單位[%s]比對本系統分行代碼不存在", userData.bankUnitNo));
				}else {
					throw new Exception("AD單位比對本系統分行代碼不存在");
				}	
			}
	
		} else {
			throw new Exception(String.format("AD使用者代號不存在; user= %s", userName));
		}

		userData.user = userName;
		
		return userData;
	}

	/**
	 * 從AD取得ECS使用者group, level。
	 * @param memberOfAttr
	 * @param groupSet 
	 * @return
	 */
	private ArrayList<String> getGroupListByMatching(Attribute memberOfAttr, Set<String> groupSet) {
		
		if (memberOfAttr != null) {
			
			// memberOf: CN=ADMIN,OU=cr,OU=TCBGroups,DC=tcbd,DC=com,CN=USER,OU=STOCKSYS,OU=TCBGroups,DC=tcbd,DC=com
			String memberOfStr = memberOfAttr.toString().toUpperCase(); 
			String groupDomainNode = GROUP_NODE  + domainNameNode;
			groupDomainNode = groupDomainNode.toUpperCase();
			ArrayList<String> arrayList = new ArrayList<String>();
			for (String group : groupSet) {
				if (memberOfStr.matches(String.format(".*%s,%s.*", group.toUpperCase(), groupDomainNode))) {
					arrayList.add(group);
				}
			}
			
			return arrayList;
			
		}else
			return null;
	}
	
	/**
	 * 從AD取出的displayName格式為displayName : 陳XX， 因此需要做字串的解析才可取得名字。
	 * @param displayNameAttr
	 * @return
	 */
	private String getDisplayName(Attribute displayNameAttr) {
		if (displayNameAttr != null) {
			String tempStr  = displayNameAttr.toString();
			int beginPos = tempStr.indexOf(":");
			return tempStr.substring(beginPos+1).trim();
		}else
			return "";
	}

	/**
	 * 先分割從AD取出的字串。
	 * 若分割後的字串只有一個OU，則此OU為銀行單位，departmentNumber為科組。
	 * 否則如果OU有1個以上，則分割後的最後一個OU為銀行單位，倒數第二個OU為科組。
	 * @param distinguishedNameAttr
	 * @param departmentNoAttr 
	 * @param userData 
	 * @param baseNode 
	 * @return
	 */
	 private boolean getAndSetDepNoAndBankUnitNo(Attribute distinguishedNameAttr, Attribute departmentNoAttr, UserData userData, String baseNode) {
		if (distinguishedNameAttr != null) {
	        String tempStr = distinguishedNameAttr.toString();
			
			int lastNameInd = tempStr.indexOf(",");
			int startBaseNodeInd = tempStr.indexOf(baseNode);
			String tokenStr = tempStr.substring(lastNameInd+1, startBaseNodeInd-1);
			
			String[] splitStrArr = tokenStr.split(",");
			// 若分割後的字串只有一個OU，則此OU為銀行單位，departmentNumber為科組。
			// 否則如果OU有1個以上，則分割後的最後一個OU為銀行單位，倒數第二個OU為科組。
			if (splitStrArr.length == 1) {
				userData.userDepno = getDepartmentNo(departmentNoAttr);
				userData.bankUnitNo = splitStrArr[0].substring(splitStrArr[0].indexOf("=")+1);
			}else {
				userData.userDepno = splitStrArr[splitStrArr.length-2].substring(splitStrArr[splitStrArr.length-2].indexOf("=")+1);
				userData.bankUnitNo = splitStrArr[splitStrArr.length-1].substring(splitStrArr[splitStrArr.length-1].indexOf("=")+1);
			}
			
			// 取前四碼，因為AD有些使用者的銀行單位顯示為會計代號(超過4碼)，
			// 但為符合目前DB設計(最大四碼)，因此目前會將會計代號縮減為4碼。
			if (userData.bankUnitNo.length() > 4)
				userData.bankUnitNo =  userData.bankUnitNo.substring(0, 4);
			
		}else {
			return false;
		}
		
		return new EcsLoginFunc(wp).doesBankUnitNumberExist(userData.bankUnitNo);
		
	}

	 /**
	  * 從ptr_sys_idtab取出AD的參數(id_code, wf_id,和 id_code2)
	  * @return
	  */
	private Map<String,Map<String,String>> getLevelAndGroupFromPtrSysIdtab() {
		wp.logSql = false;
		StringBuilder sb = new StringBuilder();
		sb.append(" select ")
			.append("  id_code  as ").append(MEMBER_OF_GROUP)
			.append(", wf_id as ").append(USER_LEVEL )
			.append(", id_code2 as ").append(USER_GROUP)
			.append(" from ptr_sys_idtab")
			.append(" where 1 = 1")
			.append(" and wf_type = 'SEC_USRLVL' ")
			.append(" and wf_id in ('A', 'C')");

		sqlSelect(sb.toString());
		
		if (sqlRowNum <= 0) {
			errmsg = "系統未設定ptr_sys_idtab中wf_type = 'SEC_USRLVL'的參數";
			return null;
		}
		
		HashMap<String,Map<String,String>> map = new HashMap<String,Map<String,String>>();
		
		for (int i = 0; i < sqlRowNum; i++) {
			HashMap<String,String> tempMap = new HashMap<String,String>();
			tempMap.put(USER_LEVEL, colStr( i,USER_LEVEL));
			tempMap.put(USER_GROUP, colStr( i,USER_GROUP));
			map.put(colStr( i,MEMBER_OF_GROUP), tempMap);
		}
		
		return map;
	}
	
	private String getDepartmentNo(Attribute departmentNoAttr) {
		if(departmentNoAttr == null) return "";
		
		String departmentNo = departmentNoAttr.toString();

		departmentNo = departmentNo.substring(departmentNo.indexOf(":") + 1);

		return departmentNo.trim();
	}

	/**
	 * 從AD的使用者節點中取出memberOf<br>
	 * 再來比對ptr_sys_idtab資料表的wf_type = 'SEC_USRLVL'的參數是否有對到<br>
	 * 如果對不到或對到多筆level，則錯誤。
	 * @param memberOfAttr
	 * @param userData 
	 * @return
	 */
	private boolean setLevelAndGroup(Attribute memberOfAttr, UserData userData) {
		
		Map<String,Map<String,String>> groupAndLevelMap = getLevelAndGroupFromPtrSysIdtab();
		
		ArrayList<String> groupList = getGroupListByMatching(memberOfAttr, groupAndLevelMap.keySet());
		
		if (groupList == null || groupList.size() == 0) {
			errmsg = "查無使用本系統業務群組權限";
			return false;
		}else
		if (groupList.size() > 1) {
			errmsg = String.format("檢核AD業務群組[%s]大於1組，本系統無法正確判斷使用者層級。", getGroupListString(groupList));
			return false;
		}
		
		userData.userLevel = groupAndLevelMap.get(groupList.get(0)).get(USER_LEVEL);
		userData.userGroup = groupAndLevelMap.get(groupList.get(0)).get(USER_GROUP);
		
		return true;

	}

	private String getGroupListString(ArrayList<String> groupList) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < groupList.size(); i++) {
			sb.append(groupList.get(i));
			if (i != groupList.size()-1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	private void setHidePasswordAndHideRefCode(UserData userData) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException {
		userData.hideRefCode = getHideRefCode();
		userData.hidePawd = comm.encrytPassword(userData.unhidePawd, userData.hideRefCode);
	}

	/**
	 * 從AD取出使用者資料，再將這些資料新增或更新sec_user資料表
	 * @param user
	 * @param password
	 * @param ldapContext
	 * @throws NamingException 
	 * @throws Exception 
	 */
	public void insertOrUpdateSecUser(String user, String unhidePassword, InitialLdapContext ldapContext) throws NamingException, Exception  {
		
		UserData userData = getUserInfo(user, ldapContext, getSearchControls());
		
		userData.unhidePawd = unhidePassword;
		
		if (isUserInSecUserAndSetUserData(userData)) {
			if ( ! areLevelAndUnitNoTheSame(userData) ) {
				throw new Exception(errmsg);
			}
			updateSecUser(userData);
		}else {
			insertSecUser(userData);
		}

	}

	private void insertSecUser(UserData userData) throws Exception {
			
			setHidePasswordAndHideRefCode(userData);
			
	        busi.SqlPrepare sp = new SqlPrepare();
			
			sp.sql2Insert("SEC_USER");
			
			sp.ppstr("USR_ID", userData.user );
			sp.ppstr("USR_CNAME",  userData.userChineseName);
			sp.ppstr("USR_PASSWORD",  userData.hidePawd);
			sp.ppstr("HIDE_REF_CODE",  userData.hideRefCode);
			sp.ppstr("USR_TYPE",  userData.userType);
			sp.ppstr("USR_DEPTNO",  userData.userDepno);
			sp.ppstr("USR_LEVEL",  userData.userLevel);
			sp.ppstr("USR_GROUP",  userData.userGroup);
			sp.ppstr("BANK_UNITNO",  userData.bankUnitNo);
			sp.addsql(", CRT_DATE", ", to_char(SYSDATE,'yyyyMMdd')");
			sp.ppstr("CRT_USER",  "System");
			sp.ppstr("MOD_USER",  "System");
			sp.addsql(", MOD_TIME", ", sysdate");
			sp.ppstr("MOD_PGM",  "LdapAuth");
			sp.ppint("MOD_SEQNO",  0);
			
			int execResult = sqlExec(sp.sqlStmt(), sp.sqlParm());
			
			if (execResult == -1) 
				throw new Exception("新增系統使用者主檔失敗，請重新登入");
			
			
		}

	private void updateSecUser(UserData userData) throws Exception {

			busi.SqlPrepare sp = new SqlPrepare();
			
			sp.sql2Update("SEC_USER");
			
			sp.ppstr("USR_CNAME",  userData.userChineseName);		
			
			// 若密碼有改變，則要產生新的hide_ref_code，並用此hide_ref_code對密碼加密
			// 否則不用更改密碼
			if ( isPasswordChanged(userData.hidePawd, userData.hidePasswordFromDB)) {
				setHidePasswordAndHideRefCode(userData);
				sp.ppstr("USR_PASSWORD",  userData.hidePawd);
				sp.ppstr("HIDE_REF_CODE",  userData.hideRefCode);
			}
			
			sp.ppstr("USR_DEPTNO",  userData.userDepno);
			sp.ppstr("USR_LEVEL",  userData.userLevel);
//			sp.ppstr("USR_GROUP",  userData.userGroup);
			sp.ppstr("BANK_UNITNO",  userData.bankUnitNo);
			sp.ppstr("MOD_USER",  "System");
			sp.addsql(", MOD_TIME = sysdate", "");
			sp.ppstr("MOD_PGM",  "LdapAuth");
			sp.addsql(", MOD_SEQNO =nvl(mod_seqno,0)+1", "");
			sp.sql2Where(" where USR_ID = ?", userData.user);
			
			int execResult = sqlExec(sp.sqlStmt(), sp.sqlParm());
			
			if (execResult == -1) 
				throw new Exception("更新系統使用者主檔失敗，請重新登入");
			
	
		}



	private boolean areLevelAndUnitNoTheSame(UserData userData) {
		if ( ! userData.userLevel.equals(userData.userLevelFromDB) ) {
			errmsg = String.format("檢核AD業務群組%s與本系統留存之【使用者層級%s】不一致。", 
					userData.userLevel, userData.userLevelFromDB);
			return false;
		}
		if ( ! userData.bankUnitNo.equals(userData.bankUnitNoFromDB)) {
			errmsg = String.format("檢核AD所屬單位%s與本系統留存之【使用者單位%s】不一致。", 
					userData.bankUnitNo, userData.bankUnitNoFromDB);
			return false;
		}
	
		return true;
	}

	/**
	 * check if the user is already in sec_user, and set userData some value.
	 * @param user
	 * @return
	 * @throws Exception 
	 */
	private boolean isUserInSecUserAndSetUserData(UserData userData) throws Exception {
		try {
			wp.logSql = false;
			StringBuilder sb = new StringBuilder();		
			sb.append(" select ")
				.append(" usr_password as passwordFromDB, ")
				.append(" hide_ref_code as hideRefCodeFromDB, ")
				.append(" usr_level as userLevelFromDB, ")
				.append(" bank_unitno as bankUnitNoFromDB, ")
				.append(" usr_group, usr_type")
				.append(" from sec_user" )
				.append(" where 1 = 1")
				.append(" and usr_id = ?");

			setString(1, userData.user);		
			
			sqlSelect(sb.toString());

			if (sqlRowNum <= 0) {
				return false;
			}else {
				// 因為sec_user資料表已經有使用者資料，所以會進行更新，
				// 因此從sec_user資料表取出usr_password和hide_ref_code，以利後續確認AD中的密碼是否已改變，以此判斷是否要update密碼
				userData.hidePasswordFromDB = colStr("passwordFromDB");
				userData.hideRefCodeFromDB =  colStr("hideRefCodeFromDB");	
				userData.hidePawd = comm.encrytPassword(userData.unhidePawd, userData.hideRefCodeFromDB);
				
				userData.userLevelFromDB = colStr("userLevelFromDB");
				userData.bankUnitNoFromDB = colStr("bankUnitNoFromDB");
				
				return true;
			}
		} catch (Exception ex) {
			throw ex;
		}

	}

	private boolean isPasswordChanged(String hidePassword, String hidePasswordFromDB) {
		return ! hidePassword.equals(hidePasswordFromDB);
	}

}

class UserData{
	String user;
	String unhidePawd;
	String hidePawd;
	String hideRefCode; 
	String hidePasswordFromDB;
	String hideRefCodeFromDB;
	
	String userLevelFromDB;
	String bankUnitNoFromDB;
	
	String userChineseName;
	String userGroup;
	String userType;
	String userLevel;
	String userDepno;
	String bankUnitNo;
}

