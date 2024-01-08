/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 110-01-08  V1.00.01  Justin       initial      *
* 110-01-12  V1.00.02  Justin       fix a unreleased resource bug
* 110-01-14  V1.00.03  Justin       chg interface into IBMSessionListener
* 110-03-30  V1.00.04  Justin       add insertion of serverIp and userIp, and update sessionId of SEC_USER and 
* 111-01-17  V1.00.05  Justin       logger -> getNormalLogger()
******************************************************************************/
package taroko.com;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;

import javax.naming.NamingException;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import com.ibm.websphere.servlet.session.IBMSessionListener;

import taroko.base.BaseData;


@WebListener
public class SessionListener extends PageDAO implements IBMSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent se) {
//		System.out.println("Session create success");
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		Object loginUserObj = session.getAttribute("loginUser");
		if (loginUserObj == null) {
			return;
		}
		String loginUser = (String)loginUserObj;
		
		try {
			try (Connection conn = getDBConnection()) {
				if (conn == null) {
					throw new Exception("無法取得DB連線");
				}
				String sql = "INSERT INTO SEC_APLOG "
						+ " (LOG_DATE, LOG_TIME, USER_ID, WS_NAME, APL_TYPE, APL_PGM_ID, APL_DESC) "
						+ " VALUES ( to_char(sysdate, 'yyyyMMdd') , to_char(sysdate, 'hh24miss'), ? ,'','2','LOGOUT','登出系統') ";

				this.sqlExec(conn, sql, new Object[] { loginUser });
				if (rc == 1) {
					conn.commit();
				} else {
					conn.rollback();
				}
			}
		} catch (Exception ex) {
			BaseData.getNormalLogger().error(" >    SessionListener.sessionDestroyed error", ex);
		} 
		BaseData.getNormalLogger().info(" >    SessionListener.sessionDestroyed " + String.format("Session destroy %s", session.getAttribute("loginUser")));
	}

	private Connection getDBConnection() throws NamingException, SQLException {
		Connection conn = null;
		if (TarokoParm.getInstance().getConnCount()  > 0) {
			try {
				conn = ConnectionManager.getConnection();
				conn.setAutoCommit(false);
				conn.setNetworkTimeout(Executors.newFixedThreadPool(1), 120000);
			} catch (Exception ex) {
				BaseData.getNormalLogger().error(" >    SessionListener.getDBConnection <<getDBConnection>> err:" + ex.getMessage());
			}
			BaseData.getNormalLogger().info(" >    SessionListener.getDBConnection dataBase "
					+ TarokoParm.getInstance().getConnName()[0] + " connect success, timeOut[2min]");

		}
		
		return conn;
		
	}

	@Override
	public void sessionRemovedFromCache(String arg0) {
		
	}

}
