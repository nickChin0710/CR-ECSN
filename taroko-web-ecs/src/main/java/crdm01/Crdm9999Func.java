/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-07-27  V1.00.01  Ryan       Initial                                  *
***************************************************************************/
package crdm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Crdm9999Func extends FuncEdit {

	public Crdm9999Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	// ************************************************************************
	@Override
	public int querySelect() {
		// TODO Auto-generated method
		return 0;
	}

	// ************************************************************************
	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 1;
	}

	// ************************************************************************
	@Override
	public void dataCheck() {

	}

	// ************************************************************************
	@Override
	public int dbInsert() {
		actionInit("A");
		this.msgOK();
		return rc;
	}

	// ************************************************************************
	@Override
	public int dbUpdate() {
		actionInit("U");
		this.msgOK();
		
		strSql = "UPDATE TSC_CARD set CURRENT_CODE = '0', OPPOST_DATE = '', TSC_OPPOST_DATE = '', ";
		strSql += "MOD_USER = ?, MOD_PGM = 'crdm9999', MOD_TIME  = sysdate ";
		strSql += "where TSC_CARD_NO = ? ";

		Object[] param = new Object[] { wp.loginUser,wp.itemStr("tsc_card_no") };
		sqlExec(strSql, param);
		
		if (rc != 1)
			return rc;
		
		return dbUpdate2();
	}

	// ************************************************************************
	@Override
	public int dbDelete() {
		actionInit("D");
		this.msgOK();

		return rc;
	}
	// ************************************************************************

	int dbUpdate2() {

		strSql = "UPDATE TSC_CARD set CURRENT_CODE = '1', OPPOST_DATE = to_char(sysdate,'yyyymmdd'), TSC_OPPOST_DATE = to_char(sysdate,'yyyymmdd'), ";
		strSql += "MOD_USER = ?, MOD_PGM = 'crdm9999', MOD_TIME  = sysdate ";
		strSql += "where CARD_NO = ? AND CURRENT_CODE = '0' AND TSC_CARD_NO <> ? ";

		Object[] param = new Object[] { wp.loginUser,wp.itemStr("card_no"),wp.itemStr("tsc_card_no") };
		sqlExec(strSql, param);
		
		if(rc < 0) {
			return -1;
		}
		return 1;
	}
	
} // End of class
