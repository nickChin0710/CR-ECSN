/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
*  109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-22  V1.00.01  Zuwei       coding standard      *
*  109-09-04  V1.00.01  Zuwei       fix code scan issue      *
*  109-09-11   V1.00.02 JustinWu    comment for publishing to TCB TEST ENV    
*  109-09-15   V1.00.03 JustinWu    remove the comment of settimeout                                                                   *  
*  109-10-14  V1.00.01  Zuwei       字串”系統日志”改為”系統日誌”      *
*  109-10-22  V1.00.04 JustinWu   add response.setContentType
*  110-01-08  V1.00.02   shiyuqi       修改无意义命名
*  110-01-20  V1.00.04   JustinWu    fix login bugs
*  110-02-05  V1.00.05   JustinWu     modify for not allowing multiple users to login
*  110-03-09  V1.00.06   JustinWu    remove the setter which set the CSRF to the session
*  110-03-30  V1.00.07   JustinWu    wp.logger -> TarokoCommon.logger
*  110-04-13  V1.00.08   JustinWu    extract system parameters
*  110-12-07  V1.00.09   JutsinWu    setAutoCommit(false)
*  111-01-18  V1.00.10   JustinWu    prevent the error when wp is null       *
*  111-02-07  V1.00.11   JustinWu    fix Redundant Null Check
******************************************************************************/
package taroko.com;
/* 
 * 2019-0123:  JH       conn.timeout()
 * 2018-0827:	JH		   LOGIN_USER.toUpper()
 * 2018-0316:	Jack		connect DB
 * 2017-1214: connectClose()
 * 2019-05-31 Jack modfify download 
 * */
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.concurrent.Executors;
import javax.servlet.*;
import javax.servlet.http.*;


@SuppressWarnings({"unchecked"})
public class MainControl extends HttpServlet {

  @SuppressWarnings("rawtypes")
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    TarokoCommon wp = null;
    Object param = null;
    Object[] obj = null;
    Method boMethod = null;

    Class[] cls = null;
    Class boClass = null;
    
    boolean doesSkipRunJava = false;

    try {
      wp = new TarokoCommon();
      wp.request = request;

      wp.dateTime();
      String startTime = wp.sysTime + wp.millSecond;
      
      request.setCharacterEncoding("UTF-8");

	  /* 取得網頁資訊 */
      wp.putHTMLInputValues();
      
      /* 檢查換日以確認是否work及log需要刪除或壓縮 */
      wp.checkChgDateForWorkAndLog();
     
      /* 取得 SESSION 資訊 */
      HttpSession session = request.getSession(false);
      if (session == null) {
    	    session = request.getSession(true);
    	    // redirect to the login page if the request does not come from the login page
			if ("YES".equals(wp.itemStr("DXC_LOGIN")) == false) {
				response.sendRedirect(request.getContextPath());
				return;
			}	
	  }
      
      wp.session = session;
      
      /* 連接資料庫 */
	  // Use the ConnectionManager to get the connection
	  wp.conn[0] = ConnectionManager.getConnection();
	  wp.conn[0].setAutoCommit(false);
	  try {
		  // 120秒- 避免 SQL COMMAND 處理過久 強迫斷線
		  wp.conn[0].setNetworkTimeout(Executors.newFixedThreadPool(1), 120000);
	  } catch (Exception ex) {
		  wp.showLogMessage("E", "", "<<setNetworkTimeout>> err:" + ex.getMessage());
	  }
	  wp.showLogMessage("I", "MainControl", "dataBase " + TarokoParm.getInstance().getConnName()[0] + " connect success, timeOut[2min]");

      /* INPUT 控制處理 */
      boolean inputResult = wp.inputControl();
      if (inputResult == false) {
    	    wp.packageName = "taroko/com/";
			wp.respHtml = "ecs_login";
			doesSkipRunJava = true;
	  }
      
      if (wp.errCode.equals("Y")) {
//        wp.closeDataBase();
        try (PrintWriter out = response.getWriter()) {
          out.println("MainControl inputControl error");
          out.flush();
        }
//        wp = null;
        return;
      }
      
      if (wp.isNotFromLoginPage()) {
    	  Object loginUserObj = wp.session.getAttribute(EcsLogin.LOGIN_USER);
          if ( loginUserObj != null ) {
        	  if ( wp.loginUser.equalsIgnoreCase((String)loginUserObj) == false) {
        		    wp.log(String.format("the login user[%s] does not equal to the login user[%s] stored in session", 
        		    		wp.loginUser, (String)loginUserObj));
    				wp.packageName = "taroko/com/";
    				wp.respHtml = "ecs_login";
    				doesSkipRunJava = true;
    		  }
    	  }
	  }

      /* 程式序號 MAPPING 轉換 */
      /* "S" : 點選功能表 或 "S2" : 變更處理程式 */
      if (Arrays.asList("S", "S2").contains(wp.menuCode) && doesSkipRunJava == false) { 
        ManualBean mBean = ManualBean.getInstance();
        if (mBean.checkLoadManual(wp.sysDate) || ManualBean.reloadMode) { // 判斷需要是否 LOAD MAPPING TABLE
          TarokoApplControl ap = new TarokoApplControl();
          Map mHash = ap.loadManual(wp); // LOAD MAPPING TABLE
          mBean.setManualData(mHash); // 將 MAPPING TABLE 存至 share bean 'ManualBean'
          ap = null;
        }
        String[] manualData = mBean.getManualData(wp.menuSeq);
        if (manualData.length < 4) {
          return;
        }
        wp.packageName = manualData[0];
        wp.javaName = manualData[1];
        wp.methodName = manualData[2];
        wp.requHtml = manualData[3];
        wp.menuDesc = manualData[4];
        TarokoWaterMark.createWaterMark(wp); // 符水印 refresh
      }
      
      /* 查核 action form 是否均為 POST */
      if (wp.methodName.equals("actionFunction") && request.getMethod().equals("GET")) {
        wp.showLogMessage("E", "", "SECURITY VIOLATION Exception ");
        throw new Exception();
      }

      /* 啟動 JAVA 處理程式 */
      if (doesSkipRunJava == false) {
          cls = new Class[1];
          obj = new Object[1];
          cls[0] = TarokoCommon.class;
          obj[0] = wp;

          wp.programPackage = wp.packageName;
          boClass = Class.forName(wp.programPackage + "." + wp.javaName);
          boMethod = boClass.getMethod(wp.methodName, cls);
          param = boClass.newInstance();
          boMethod.invoke(param, obj);
	  }
      

      /* 輸出 TEXT FILE , PDF FILE或 EXCEL 報表 改為 file mode */
      if (wp.exportSrc.equals("Y") && !wp.errCode.equals("Y")) {
        try (ServletOutputStream outstr = response.getOutputStream()) {
          response.setHeader("Content-Disposition","attachment; filename=\"" + wp.exportFile + "\"");  // fix downloading file issues   
          if (wp.exportSrc.equals("Y")) {
            response.setContentType("text/plain");
          }
          response.setContentLength(wp.ba.size());
          wp.ba.writeTo(outstr);
          outstr.flush();
        }
        wp.ba.close();
        // wp.commitDataBase();
        wp.rollbackDataBase();
        wp.ba = null;
        return;
      }

      response.setContentType("text/html; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.setHeader("Expires", "-1");

      /* 網頁轉向處理 輸出 PDF , EXCEL */
      if (wp.linkMode.equals("Y")) {
        String linkURL = wp.linkURL;
        wp.commitDataBase();
        float txTime = wp.durationTime(startTime, "TRANS-TOTAL");
        wp.showLogMessage("I", "ENDED", "TIME : " + txTime + " " + wp.exportFile + wp.newLine);
        response.sendRedirect(linkURL);
        return;
      }

      TarokoParser pr = new TarokoParser();
      
      /* collect HTML element names and types, which will be used in parseOutput(wp) in the following code */
      pr.parseNameType(wp);

      /* OUTPUT 控制處理 */
      // 處理預設值及CHECKBOX, RADIO, SELECT欄位勾選
      wp.outputControl();
      
      // 設定 session 資料
      if (wp.menuSeq.length() >= 4 && !wp.specialFunc) {
        wp.setSessionData();
      }

      /* 回覆網頁輸出處理 */
      wp.out = response.getWriter();
      
      /* replace ${colName} with the value of colName */ 
      pr.parseOutput(wp);

      if (wp.errCode.equals("Y")) {
        wp.rollbackDataBase();
      } else {
        wp.commitDataBase();
      }

      float txTime = wp.durationTime(startTime, "TRANS-TOTAL");
      wp.showLogMessage("I", "ENDED", "TIME : " + txTime + wp.newLine);
    } catch (Exception ex) {
    	String exceptionMesg = "<caption><font color=red><B>MainControl.java ERR:000001 - TarokoException : "
				+ ex.getMessage() + " 處理錯誤</B></font></caption>";
    	wp.rollbackOnly();
		wp.closeDataBase();
		wp.expHandle(ex);
		wp.showLogMessage("E", "", "ERR:000001 - 系統異常" + ex.getLocalizedMessage());
		if (wp.jsonCode.equals("Y")) {
			exceptionMesg = "Er";
		} else {
			exceptionMesg = "<caption><font color=red><B>MainControl.java ERR:000001 - 系統異常，請查看系統日誌，處理錯誤</B></font></caption>";
		}
		response.setContentType("text/html; charset=UTF-8"); // 2020-10-21: JustinWu
		PrintWriter out = response.getWriter();
		out.println(exceptionMesg);
    } finally {
    	wp.closeDataBase();
		wp = null;
		param = null;
		cls = null;
		obj = null;
		boMethod = null;
		boClass = null;
    }

  }
}
