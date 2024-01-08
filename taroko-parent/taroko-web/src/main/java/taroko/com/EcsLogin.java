/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
*  109-04-21  V1.00.01  Zuwei       code format                              
*  109-07-22  V1.00.01  Zuwei       coding standard      *
*  110-01-08  V1.00.02  shiyuqi     修改无意义命名
*  110-11-30  V1.00.03  Justin      create water mark only when the request is for login
*  111-01-06  V1.00.04  Justin      do some optimization                     *
******************************************************************************/
package taroko.com;
/** ECS登入作業
 * 2021-1029   JUSTIN add TarokoWaterMark.createWaterMark(wp) when logging in
 * 2020-0113   JUSTIN add a static variable for the session management
 * 2020-1229   JUSTIN fix a bug causing the error message not to show 
 * 2020-1225   JUSTIN parameterize sql
 * 2020-1016   JUSTIN remove getSiteName()
 * 2020-0916   JUSTIN 修改無群組使用者登入錯誤訊息
 * 2020-0915   JUSTIN add temp account
 * 2020-0807   JUSTIN add updateUserLoginTime
 * 2020-0714   JUSTIN canUserUseAnyProgram -> doesUserGroupExistSystem
 * 2020-0713   JUSTIN adEcsLogin -> adLogin
 * 2020-0709   JUSTIN add canUserUseAnyProgram
 * 2020-0624   JUSTIN add the method verifying userId and password by LDAP
 * 2020-0612   JUSTIN add AD Authentication 
 * 2020-0520   KEVIN PRIMANY_DB2 for DR IP check 
 * 2019-1202   JH    UAT-bug
 * 2019-1128   JH    bank_unitno
 * 2019-1016   JH    bugfix
 * 2019-0729   JH    default_auth()
 * 2019-0521:  JH    ecs_menuSelect_4()
 * */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpSession;

import busi.FuncBase;
import busi.SqlPrepare;
import busi.func.EcsLoginFunc;
import ofcapp.LdapAuth;

public class EcsLogin extends PageDAO {

  public static final String LOGIN_USER = "loginUser";
  public static final String LOGIN_ERROR_CNT = "loginErrorCnt";
  private String menuData = "";
  private String tigraDir = "";
  private String groupName = "";
  private String newLine = "";
  private String userGroup = "";
  private String userType = "";
  private String userLevel = "";

  public void showScreen(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.showLogMessage("D", "showScreen", "started");
    
    wp.respHtml = wp.requHtml;
    wp.setValue("HIDE_DATA", wp.hideData, 0);

    // 2021/11/30 Justin: If the request is from a login request, 
    // then the system should create water mark
    if (wp.requHtml != null && "FrameFirst".equals(wp.requHtml) ) {
    	TarokoWaterMark.createWaterMark(wp);
	}

    // 2020-10-16 Justin remove getSiteName()
//    wp.colSet("test_site", this.getSiteName());

    wp.showLogMessage("D", "showScreen", "ended");
    return;
  }

  public void ecsMainTree(TarokoCommon wr) throws Exception {
        super.wp = wr;
        try {
			Object loginUserObj = wp.session.getAttribute(LOGIN_USER);
//          if ( loginUserObj != null && ! wp.loginUser.equalsIgnoreCase(loginUserObj.toString())) {
			if (loginUserObj != null) {
				int loginErrorCnt = getLogoinErrorCnt(wp.session);
				loginErrorCnt++;
				wp.session.setAttribute(LOGIN_ERROR_CNT, loginErrorCnt);
				if (loginErrorCnt >= 2) {
					wp.alertMesg("您可能已在其他分頁登入，若無法正常登入，請關閉瀏覽器後重新登入。");
				} else {
					wp.alertMesg("同時間只能登入一個帳號");
				}
				wp.respHtml = "ecs_login";
				return;
			} else {
				wp.session.setAttribute(LOGIN_ERROR_CNT, 0);
			}
		} catch (IllegalStateException e) {
			wp.alertMesg("Session異常，請重新登入");
			wp.respHtml = "ecs_login";
			return;
		}
        
        // ==============================
        // 如果 adLogin == "Y"，則進行AD帳號密碼認證
        // 否則在 sec_user進行帳號密碼認證
        boolean isCheckSucced = false;
        
        //=======Justin Wu: temp account for test env================================================== 
        if(wp.loginUser.equals("0000") || wp.loginUser.equals("DXC")) {
        	EcsLoginFunc ecsLoginFunc = new EcsLoginFunc(wp);
			isCheckSucced = ecsLoginFunc.checkEcsUserLogin(wp.loginUser, wp.itemStr("PASSWD"));
			wp.colClear(0, "PASSWD");
			
			// 若無法通過帳號密碼認證，則回ecs_login登入畫面
			if ( ! isCheckSucced) {
				wp.alertMesg(ecsLoginFunc.getErrorMsg());
				wp.respHtml = "ecs_login";
				// wp.createHideData();
				return;
			}	
		//========================================================
        }else if (TarokoParm.getInstance().getAdLogin().equalsIgnoreCase("Y")) {

			LdapAuth ldapAuth = new LdapAuth(wp);

			isCheckSucced = ldapAuth.checkLoginPasswd(wp.loginUser, wp.itemStr("PASSWD"));
			wp.colClear(0, "PASSWD");
			
			// 若無法通過帳號密碼認證，則回ecs_login登入畫面
			if (!isCheckSucced) {
				wp.alertMesg("登入失敗 " + ldapAuth.mesg());
				wp.respHtml = "ecs_login";
				// wp.createHideData();
				return;
			}
		} else {
			EcsLoginFunc ecsLoginFunc = new EcsLoginFunc(wp);
			isCheckSucced = ecsLoginFunc.checkEcsUserLogin(wp.loginUser, wp.itemStr("PASSWD"));
			wp.colClear(0, "PASSWD");
			
			// 若無法通過帳號密碼認證，則回ecs_login登入畫面
			if ( ! isCheckSucced) {
				wp.alertMesg(ecsLoginFunc.getErrorMsg());
				wp.respHtml = "ecs_login";
				// wp.createHideData();
				return;
			}	
		}
		
		// ==============================
		// 確認使用者group以及deptno是否有存在系統中
		if ( ! checkUserGroupAndDeptno()) {
			return;
		}
		
		try {
			wp.session.setAttribute(LOGIN_USER, wp.loginUser);
		}catch (IllegalStateException  e) {
			wp.alertMesg("Session異常，請重新登入");
			wp.respHtml = "ecs_login";
			return;
		}
		
		// 更新登入日期及時間
		updateUserLoginTime(wp.loginUser);

		tigraDir = "ecs_tree_unit3";
		ecsInitTree();

		// -查詢理由-
		StringBuilder sb = new StringBuilder();
		wp.logSql = false;
		sb.append("select wf_id as db_code, ")
		  .append(" wf_id||'_'||wf_desc as db_text")
		  .append(" from ptr_sys_idtab")
		  .append(" where wf_type ='IDNO-QUERY-REASON'")
		  .append(" fetch first 999 row only");
		
		wp.sqlCmd = sb.toString();
		selectNoLimit();
		this.pageQuery();
		if (sqlRowNum > 0) {
			wp.multiOptionList(0, "dddw_idno_reason", "db_code", "db_text");
		}

		return;
	}

	private int getLogoinErrorCnt(HttpSession session) {
		Object loginErrorCntObj = session.getAttribute(LOGIN_ERROR_CNT);
		return loginErrorCntObj != null ? (int)loginErrorCntObj : 0;
	}

public boolean ecsInitTree() throws Exception {
    wp.showLogMessage("D", "bank_initTree", "started");

    // wp.ddd("user-menu_data="+menu_data);

    String treeLink = "";
    // the length of the scripts combined is 42505
    StringBuffer scriptBuf = new StringBuffer(42505); 
    processScript(scriptBuf);
    wp.setValue("SYS_SCRIPT", scriptBuf.toString(), 0);

    wp.javaName = "EcsLogin"; // "TarokoLogin";
    // 2020/04/20 方法名規範化修改 ecs_loginTree -> ecsLoginTree
    wp.methodName = "ecsLoginTree"; 
    wp.packageName = "taroko.com";
    wp.respHtml = tigraDir;
    wp.createHideData();
    treeLink = "MainControl?HIDE=" + wp.hideData + "&DXC_LOGIN=TREE";
    wp.setValue("TREE_LINK", treeLink, 0);
    createMainLink();
    wp.respHtml = "ecs_tree_main"; // "MainTreeTigra";

    // 2020-10-16 Justin remove getSiteName()
//    wp.colSet("site_name", this.getSiteName()); 
    wp.colSet("userId", wp.loginUser);

    getUserDeptno(wp.loginUser);
    wp.colSet("top_user_deptno", wp.loginDeptNo);

    return true;
  } // End of initialTigraTree

  /* TREE_LINK 啟動 */
  public void ecsLoginTree(TarokoCommon wr) {
    super.wp = wr;

    InputStreamReader fr = null;
    BufferedReader br = null;
    try {

      wp.setValue("HIDE_DATA", wp.hideData, 0);

      newLine = wp.newLine;

      selectUserData();

      String[] aaGroup = userGroup.split(",");
      aaGroup = getUserGroup(aaGroup);
      // if (Arrays.asList(aa_group).contains("sec000") ||
      // Arrays.asList(aa_group).contains("SEC000")) {
      // default_auth();
      // }

      wp.menuBuf = new StringBuffer();
      wp.menuBuf.append("<script language='javascript'>").append(newLine);
      wp.menuBuf.append("var TREE_ITEMS = [").append(newLine);
      wp.menuBuf.append("[ \"--信用卡管理系統\",\"\", ").append(newLine);
      // -範例-
      ecsMenuSample(); 
      // -sub-system-
      if (userType.equals("T") == false) {
        ecsMenuSelect4(aaGroup);
      } else {
        ecsMenuSelectT(aaGroup);
      }

      // -預設安控權限-
      // if (Arrays.asList(aa_group).contains("sec000") ||
      // Arrays.asList(aa_group).contains("SEC000")) {
      // default_auth();
      // }

      wp.menuBuf.append(" ], ").append(newLine);
      wp.menuBuf.append(" ]; ").append(newLine);
      wp.menuBuf.append(" </script>").append(newLine);
    } catch (Exception ex) {
      wp.expMethod = "loginTreeTigra";
      wp.expHandle(ex);
      return;
    } finally {
      try {
        if (fr != null) {
          fr.close();
        }
        fr = null;
        if (br != null) {
          br.close();
        }
        br = null;
      } catch (Exception ex2) {
      }
    }
  } // End of TarokoLogin

  // MenuSample()-------------------------------------------------------------
  void ecsMenuSample() {
    wp.menuBuf.append(" [\"--公告事項\",\"javascript:top.menuControl('secq1010,156')\"], ").append(newLine);
    wp.menuBuf.append(" [\"--待覆核事項查詢\",\"javascript:top.menuControl('secq1020,156')\"], ").append(newLine);
    wp.menuBuf.append(" [\"--callBatch查詢\",\"javascript:top.menuControl('ptrm0777,156')\"], ").append(newLine);
    wp.menuBuf.append(" [\"--批次報表\",\"javascript:top.menuControl('ptrr0000,156')\"], ").append(newLine);
  }

  void ecsMenuSelectT(String[] aaGroup) {
    if (aaGroup.length == 0)
      return;
    
    StringBuilder sb = new StringBuilder();
    
    sb.append("select A.pkg_name, '' as pkg_desc")
      .append(", A.wf_winid as id_num, A.wf_winid, A.wf_name")
      .append(", '156' as aut_update")
      .append(" from sec_window A")
      .append(" where A.menu_flag ='Y'");

    if("Y".equals(TarokoParm.getInstance().getDbSwitch2Dr())) {
    	sb.append(" and db4Dr_flag ='Y' ");
    }	else	{
    	sb.append(" and db4Dr_flag >= '' ");
    }
    sb.append(sqlColIn("A.pkg_name", aaGroup) )
      .append(" order by A.pkg_name, A.wf_winid");

    setSelectLimit(0);
    sqlSelect(sb.toString());
    int llRow = sqlRowNum, kkSeq = 0;
    String pkgName = "";
    for (int ii = 0; ii < llRow; ii++) {
      if (!commString.eqIgno(pkgName, sqlStr(ii, "pkg_name"))) {
        if (empty(pkgName) == false) {
          wp.menuBuf.append("   ],").append(newLine);
        }
        kkSeq++;
        pkgName = sqlStr(ii, "pkg_name");
        
        wp.menuBuf.append("   [\"--")
                  .append(commString.lpad("" + kkSeq, 2, "0"))
                  .append("-")
                  .append(pkgName.toUpperCase())
                  .append(sqlStr(ii, "pkg_desc") )
                  .append(" \",\"\", " )
                  .append(newLine);
      }

      wp.menuBuf.append(" [\"--")
                .append(sqlStr(ii, "id_num"))
                .append("-")
                .append(sqlStr(ii, "wf_name"))
                .append("\",\"javascript:top.menuControl(")
                .append("'")
                .append(sqlStr(ii, "wf_winid"))
                .append(",")
                .append(commString.rpad(sqlStr(ii, "aut_update"), 3, "0") )
                .append("'")
                .append(")\"], " )
                .append(newLine);
      
    }
    if (llRow > 0) {
      wp.menuBuf.append("   ]," + newLine);
    }
  }

  void ecsMenuSelect4(String[] aaGroup) {
    if (aaGroup.length == 0)
      return;
    
    for (int ii = 0; ii < aaGroup.length; ii++) {
      aaGroup[ii] = aaGroup[ii].toUpperCase();
    }
    
    StringBuilder sb = new StringBuilder();
    sb.append("select A.pkg_name, '_'||uf_tt_idtab('SEC-PKG-NAME',A.pkg_name) as pkg_desc")
      .append(", A.wf_winid as id_num, A.wf_winid, A.wf_name")
      .append(", decode(max(B.aut_update),'Y',1,0)")
      .append("||decode(max(B.aut_approve),'Y',5,0)")
      .append("||'0' as aut_update")
//      .append("||decode(max(B.aut_print),'Y',6,0) as aut_update")
      .append(" from sec_window A join sec_authority B on A.wf_winid =B.wf_winid")
      .append(" where B.aut_query ='Y' and A.menu_flag ='Y'");
    
    if("Y".equals(TarokoParm.getInstance().getDbSwitch2Dr())) {
    	sb.append(" and db4Dr_flag ='Y' ");
    }	else	{
    	sb.append(" and db4Dr_flag >= '' ");
    }
    
    sb.append(sqlColIn("B.group_id", aaGroup))
      .append(" and B.user_level = ? ")
      .append(" group by A.pkg_name, A.wf_winid, A.wf_name")
      .append(" order by A.pkg_name, A.wf_winid");

    setString(userLevel);
    setSelectLimit(0);
    sqlSelect(sb.toString());
    int llRow = sqlRowNum;
    String pkgName = "";
    for (int ii = 0; ii < llRow; ii++) {
      if (!commString.eqIgno(pkgName, sqlStr(ii, "pkg_name"))) {
        if (empty(pkgName) == false) {
          wp.menuBuf.append("   ],").append(newLine);
        }

        pkgName = sqlStr(ii, "pkg_name");
        wp.menuBuf.append("   [\"--")
                  .append(pkgName.toUpperCase())
                  .append(sqlStr(ii, "pkg_desc"))
                  .append(" \",\"\", ")
                  .append(newLine);
      }

      wp.menuBuf.append(" [\"--")
                .append(sqlStr(ii, "id_num"))
                .append("-")
                .append(sqlStr(ii, "wf_name"))
                .append("\",\"javascript:top.menuControl(")
                .append("'").append(sqlStr(ii, "wf_winid")).append(",")
                .append(commString.rpad(sqlStr(ii, "aut_update"), 3, "0")).append("'")
                .append(")\"], ")
                .append(newLine);

    }
    if (llRow > 0) {
      wp.menuBuf.append("   ],").append(newLine);
    }
  }

  void ecsMenuSelect(String sGroup, String sLevel) {
    wp.logSql = false;

    groupName = "";
    String sql1 = "select group_name from sec_workgroup where group_id =?";
    setString(1, sGroup);
    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      groupName = sqlStr("group_name");
    }

    if (userType.equalsIgnoreCase("T")) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(" select")
    	  .append(" wf_winid as id_num,")
    	  .append(" wf_winid, wf_name, '156' as aut_update")
    	  .append(" from sec_window")
    	  .append(" where pkg_name = ? ")
    	  .append(" and menu_flag ='Y'");

      if("Y".equals(TarokoParm.getInstance().getDbSwitch2Dr())) {
    	  sb.append(" and db4Dr_flag ='Y' ");
      }	else	{
    	  sb.append(" and db4Dr_flag >= '' ");
      }
      sb.append(" order by wf_winid ");
      
      wp.sqlCmd = sb.toString();

      setString(sGroup);

    } else {
		StringBuilder sb = new StringBuilder();
		sb.append("select")
		  .append(" A.wf_winid as id_num")
		  .append(", A.wf_winid")
		  .append(", A.wf_name")
		  .append(", case when sum(decode(B.aut_update,'Y',1,0))>0 then '1' else '0' end")
		  .append("||case when sum(decode(B.aut_approve,'Y',1,0))>0 then '5' else '0' end")
		  .append("||case when sum(decode(B.aut_print,'Y',1,0))>0 then '6' else '0' end as aut_update")
		  .append(", sum(nvl(C.sort_seqno,999999)) as win_sort")
		  .append(" from sec_window A join sec_authority B on A.wf_winid =B.wf_winid")
		  .append(" left join sec_authpgm_sort C on C.group_id=B.group_id and C.wf_winid=B.wf_winid")
		  .append(" where 1=1 ")
		  .append(" and B.group_id = ? ")
		  .append(" and B.user_level = ? ")
		  .append(" and A.menu_flag ='Y' and B.aut_query='Y' ");

		if ("Y".equals(TarokoParm.getInstance().getDbSwitch2Dr())) {
			sb.append(" and A.db4Dr_flag ='Y' ");
		} else {
			sb.append(" and A.db4Dr_flag >= '' ");
		}
		
		sb.append(" group by A.wf_winid, A.wf_name" + " order by win_sort,A.wf_winid");

		wp.sqlCmd = sb.toString();
		setString(sGroup);
		setString(sLevel);
	}
	setSelectLimit(0);
	sqlSelect();
  }

  void defaultAuthHtml() {
    try {
      if (commString.eqIgno(userLevel, "A"))
        readScriptFile(wp.menuBuf, "sec000_a.html");
      else if (commString.eqIgno(userLevel, "B"))
        readScriptFile(wp.menuBuf, "sec000_b.html");
      else if (commString.eqIgno(userLevel, "C"))
        readScriptFile(wp.menuBuf, "sec000_c.html");
    } catch (Exception ex) {
    }
  }

  void processScript(StringBuffer menuBuf) throws Exception {
    menuBuf.append(("<script language='javascript'>" + wp.newLine));

    readScriptFile(menuBuf, "encryption.js");
    readScriptFile(menuBuf, "systemControl_12.js");
    readScriptFile(menuBuf, "buttonControl_12.js");
    readScriptFile(menuBuf, "validateFormat_12.js");

    menuBuf.append(("</script>" + wp.newLine));

  } // End of processScript

  void readScriptFile(StringBuffer menuBuf, String scriptFile) throws Exception {
    // FileReader fr = null;
    InputStreamReader fr = null;
    BufferedReader br = null;
    try {
      scriptFile = TarokoParm.getInstance().getRootDir() + "/js/" + scriptFile;
      // fr = new FileReader(scriptFile);
      fr = new InputStreamReader(new FileInputStream(scriptFile), "UTF-8");
      br = new BufferedReader(fr);
      while (br.ready()) {
        menuBuf.append((br.readLine() + wp.newLine));
      }
    } catch (Exception ex) {
      wp.expMethod = "readScriptFile";
      wp.expHandle(ex);
      return;
    } finally {
      try {
        if (fr != null) {
          fr.close();
        }
        fr = null;
        if (br != null) {
          br.close();
        }
        br = null;
      } catch (Exception ex2) {
      }
    }
  } // End of processScript

  boolean createMainLink() throws Exception {
    wp.javaName = "EcsLogin"; // "TarokoLogin";
    wp.methodName = "firstLink";
    wp.packageName = "taroko.com";
    wp.respHtml = "ecs_frameset"; // "FramesetTaroko";
    wp.createHideData();
    String urlParm = "MainControl?HIDE=" + wp.hideData + "&DXC_LOGIN=MAIN";
    wp.setValue("MAIN_LINK", urlParm, 0);

    return true;
  } // End of createMainLink

  public void firstLink(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.showLogMessage("D", "firstLink", "started");

    wp.packageName = "taroko.com";
    wp.javaName = "EcsLogin"; // "TarokoLogin";
    wp.methodName = "showScreen";
    wp.requHtml = "FrameFirst";
    wp.respHtml = "FrameFirst";
    wp.createHideData();
    String urlParm = "MainControl?HIDE=" + wp.hideData + "&DXC_LOGIN=FIRST";
    wp.setValue("FIRST_LINK", urlParm, 0);
    wp.respHtml = "ecs_frameset"; // "FramesetTaroko";

    wp.showLogMessage("D", "firstLink", "ended");
    return;
  }

  boolean selectUserData() {
    wp.showLogMessage("D", "selectUserData", "started");

    wp.daoTable = "sec_user";
    if (wp.loginUser.length() == 0) {
      wp.alertMesg("使用者代號: 不可空白");
      return false;
    }
    wp.loginUser = wp.loginUser.toUpperCase();
    try {
      wp.sqlCmd = " select usr_level, usr_group, usr_type, usr_deptno from sec_user where usr_id =?";
      setString2(1, wp.loginUser);
      sqlSelect();
      if (sqlRowNum <= 0) {
        wp.alertMesg("使用者代號: 不存在; kk[" + wp.loginUser + "]");
        return false;
      }
      if (empty(sqlStr("usr_group")) == false) {
        menuData = "db";
      }
      userGroup = sqlStr("usr_group");
      userType = sqlStr("usr_type");
      userLevel = sqlStr("usr_level");
      wp.loginDeptNo = sqlStr("usr_deptno");
    } catch (Exception ex) {
      return false;
    }

    // -logIn.log-
    insertSecAplog(1);

	// -all usr_group-
	if (commString.eqIgno(userGroup, "*") && commString.eqIgno(userType, "T")) {
		wp.sqlCmd = "select distinct pkg_name from sec_window where pkg_name<>'' order by pkg_name";

		setSelectLimit(0);
		sqlSelect();
		int liNrow = sqlRowNum;
		StringBuilder sb = new StringBuilder();
		for (int ii = 0; ii < liNrow; ii++) {
			sb.append(sqlStr(ii, "pkg_name")).append(",");
		}
		if (sb.toString().length() > 0)
			userGroup = sb.toString();
	}

    return true;
  } // End of selectUserData

	private boolean selectUserGroupAndUserType() {
		wp.sqlCmd = " select usr_group, usr_type, usr_deptno, bank_unitno from sec_user where usr_id =?";
		setString2(1, wp.loginUser);
		sqlSelect();
		if (sqlRowNum <= 0) {
			return false;
		}
		return true;
	}

void insertSecAplog(int aiInout) {
	StringBuilder sb = new StringBuilder();
	sb.append("INSERT into sec_aplog ( ")
	  .append("log_date, log_time, user_id, apl_type, apl_pgm_id, apl_desc ) values ( ")
	  .append("to_char(sysdate,'yyyymmdd'), to_char(sysdate,'hh24miss'), ?, ?, ?, ?)");
    wp.sqlCmd = sb.toString();
    setString(1, wp.loginUser);
    if (aiInout == 1) {
      setString(2, "1");
      setString(3, "LOGIN");
      setString(4, "進入系統");
    } else {
      setString(2, "2");
      setString(3, "LOGOUT");
      setString(4, "退出系統");
    }
    try {
      this.sqlExec("");
      wp.commitOnly();
    } catch (Exception ex) {
      wp.rollbackOnly();
    }
  }

  /**
   * 更新登入日期及時間
   * @param loginUser
   * @throws Exception 
   */
  private void updateUserLoginTime(String loginUser) throws Exception {
	    busi.SqlPrepare sp = new SqlPrepare();
		
		sp.sql2Update("SEC_USER");
		
		sp.ppstr("USR_INDATE",  wp.sysDate);	
		sp.ppstr("USR_INTIME",  wp.sysTime);	
		sp.sql2Where(" where USR_ID = ?", loginUser);
	
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		
		if (sqlRowNum != 1) 
			throw new Exception("更新使用者登入日期及時間錯誤");
  }

void getUserDeptno(String aUserId) {
    wp.sqlCmd = "select usr_deptno, usr_cname from sec_user where usr_id =?";
    setString2(1, aUserId);
    sqlSelect();
    if (sqlRowNum > 0) {
      wp.loginDeptNo = sqlStr("usr_deptno");
      wp.colSet("top_user_name", sqlStr("usr_cname"));
    }
  }
  
  String[] getUserGroup(String[] args) {
    if (userType.equalsIgnoreCase("T") == false)
      return args;

    String[] aaug = new String[100];
    for (int ii = 0; ii < aaug.length; ii++) {
      aaug[ii] = "";
    }

    int jj = -1;
    for (int ii = 0; ii < args.length; ii++) {
      if (args[ii].indexOf("%") < 0) {
        jj++;
        aaug[jj] = args[ii];
        continue;
      }

      // --
      wp.logSql = false;
      String sql1 ="select distinct pkg_name from sec_window where 1=1 and pkg_name like ? order by 1";
      setString(args[ii]);
      this.sqlSelect(sql1);
      for (int ll = 0; ll < sqlRowNum; ll++) {
        jj++;
        aaug[jj] = sqlStr(ll, "pkg_name");
      }
    }

    return aaug;
  }

/**
   * 確認使用者group以及deptno是否有存在系統中(bank_unitno已經在前面流程驗證)
   * @return true if the user have any group and deptno matching sec's groups; else return false
   */
  boolean checkUserGroupAndDeptno() {
	  try {
		  boolean isCheckSuccess = false;
		  
		  isCheckSuccess = selectUserGroupAndUserType();
		  if ( ! isCheckSuccess) {
			  wp.alertMesg("登入失敗，查無使用者資訊");
			  return false;
		  }
		  
		  isCheckSuccess = checkUserGroup(sqlStr("usr_group"),sqlStr("usr_type"));
		  if ( ! isCheckSuccess) {
			  wp.alertMesg("登入失敗，使用者無使用本系統權限，請向主管設定權限。");
			  return false;
		  }
		  
//		  isCheckSuccess = checkUserDeptno(sqlStr("usr_deptno"));
//		  if ( ! isCheckSuccess) {
//			  wp.alertMesg("登入失敗，使用者科組(部門)代號不存在系統");
//			  return false;
//		  }
 
	    } catch (Exception ex) {
	      wp.alertMesg("登入失敗" + ex.getMessage());
	      return false;
	    }
		    
		return true;
  }

private boolean checkUserDeptno(String userDeptno) {
	if(userDeptno ==null || userDeptno.trim().length()==0)
		return false;
	
	String sql1 =
             " select 1 "
         + " from dual "
         + " where EXISTS ( "
         + " select pdc.dept_code "
         + " from ptr_dept_code as pdc "
         + " where 1= 1 "
         + " and pdc.dept_code = ? "
         + " ) ";
	    setString(userDeptno);
		sqlSelect(sql1);

		if (sqlRowNum <= 0) {
			return false;
		}

		return true;
}

private boolean checkUserGroup(String userGroup, String userType) {
		// Justin for Test
		if (userType.equals("T") || userGroup.contains("*"))
			return true;
		// Justin for Test

       String[] groupArr = userGroup.split(",");
       groupArr = getUserGroup(groupArr);
       
       StringBuilder sb = new StringBuilder();
       sb.append(" select 1 ")
         .append(" from dual ")
         .append(" where EXISTS ( ")
         .append(" select group_id ")
         .append(" from sec_workgroup ")
         .append(" where 1= 1 ")
         .append(sqlColIn("group_id", groupArr))
         .append(" ) ");
 	  
 	    String sql1 = sb.toString();

		sqlSelect(sql1);

		if (sqlRowNum <= 0) {
			return false;
		}

		return true;
}

public String sqlColIn(String col, String[] args) {
	if (args == null || args.length == 0)
		return "";

	StringBuilder sb = new StringBuilder();
	sb.append(" and ").append(col).append(" in (''");
	for (int ii = 0; ii < args.length; ii++) {
		if (empty(args[ii]))
			continue;
		sb.append(", ? ");
		setString(args[ii]);
	}
	sb.append(")");
	return sb.toString();
}

}
