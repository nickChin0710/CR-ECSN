/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 109-09-30  V1.00.02 JustinWu   getSystemUrl: get host, port, and appName from request 
* 109-10-06   V1.00.10  JustinWu   fix the compatibility of HTTP and HTTPS  
******************************************************************************/
package taroko.com;

import java.util.HashMap;
import java.util.Map;

/* 啟動程式 V.2018-0924.jh */

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoApplControl extends PageDAO {

  public Map loadManual(TarokoCommon wr) throws Exception {

    super.wp = wr;
    wp.logSql = false;
    wp.showLogMessage("D", "loadManual", "started");

    Map<String, String> manuHash = new HashMap<String, String>();

    selectLimit = false;
    wp.sqlCmd =
        "select " + "wf_winid" + ", req_html" + ", pgm_id"
            + ", nvl(method_name,'showScreen') as method_name " + ", pkg_name" + ", wf_name"
            + " from sec_window";
    setSelectLimit(0);
    pageQuery();
    for (int i = 0; i < wp.selectCnt; i++) {
      String manualData =
          wp.getValue("pkg_name", i) + "#" + wp.getValue("pgm_id", i) + "#"
              + wp.getValue("method_name", i) + "#" + wp.getValue("req_html", i) + "#"
              + wp.getValue("wf_winid", i) + " (" + wp.getValue("wf_name", i) + ")" + "#";
      manuHash.put(wp.getValue("wf_winid", i), manualData);
    }
    wp.showLogMessage("D", "loadManual", "load count " + sqlRowNum + " ended");
    return manuHash;
  }

  void insertLogEcsUsing() {

    String sql1 =
        "insert into log_ecs_using (" + " use_date" + ", use_time " + ", user_id " + ", dept_no "
            + ", use_type " + ", win_id " + ", ws_name " + " ) values ( "
            + " to_char(sysdate,'yyyymmdd') " + ", to_char(sysdate,'hh24miss') " + ", ? " // user_id
            + ", ? " // dept_no
            + ", '3' " + ", ? " // win_id
            + ", '' " // ws_name
            + " )";
    this.sqlExec(sql1, new Object[] {wp.loginUser, wp.loginDeptNo, wp.menuSeq});
    if (sqlRowNum > 0) {
      wp.commitOnly();
    } else {
      wp.rollbackOnly();
    }

  }

  public void getSystemUrl(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.ajaxInfo = wp.request.getScheme() + "," +wp.request.getServerName() + "," + wp.request.getServerPort() + "," + wp.request.getContextPath() + ",";
    return;
  }

  public void logout(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.showLogMessage("D", "logout", "started");
    wp.respHtml = "TarokoLogout";
    
    // if the loginUser is equal to the loginUser in the session, then invalidate the session
    Object loginUserObj = wp.session.getAttribute(EcsLogin.LOGIN_USER);
    if (loginUserObj != null && wp.loginUser.equalsIgnoreCase((String)loginUserObj) ) {
    	wr.session.invalidate();
	}

  }

} // End of class TarokoApplControl
