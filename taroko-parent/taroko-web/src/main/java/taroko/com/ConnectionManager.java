package taroko.com;
/**
 * 110-07-07  V1.00.01  JustinWu     initial
 * 110-11-10  V1.00.02  JustinWu     setAutoCommit(false)
 * 110-12-07  V1.00.03  JustinWu     fix Unreleased Resource: Database
 * */
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionManager {

	private static DataSource ds;

	static {
		try {
			InitialContext envCtx = new InitialContext();
			ds = (DataSource) envCtx.lookup(TarokoParm.getInstance().getResourceName()[0]);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
}
