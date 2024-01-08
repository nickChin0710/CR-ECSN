/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112-03-24  V1.00.00  Ryan        initial                                  * 
*****************************************************************************/
package Cca;

import java.sql.Connection;

import com.AccessDAO;

public class CalBalanceBatch extends AccessDAO {
	
	public CalBalanceBatch(Connection conn[], String[] dbAlias) throws Exception {	
	    super.conn = conn;
	    setDBalias(dbAlias);
	    setSubParm(dbAlias);
	    return;
	}
	

	/**
	 * 查詢可用餘額及預借現金餘額-106個人-正卡人
	 * @param idNo (代入持卡人ID查詢)
	 * @return [可用餘額(ACCT_AMT_BALANCE)、預借現金餘額(ACCT_CASH_BALANCE)]
	 * @throws Exception
	 */
	public Long[] batchIdNoBalance(String idNo) throws Exception {
		Long[] balanceAmts = new Long[2];
		balanceAmts[0] = Long.valueOf(0);
		balanceAmts[1] = Long.valueOf(0);
		sqlCmd = "SELECT ACCT_AMT_BALANCE,ACCT_CASH_BALANCE FROM CCA_ACCT_BALANCE_CAL ";
		sqlCmd += " WHERE ACCT_TYPE='01' AND ID_P_SEQNO IN (SELECT ID_P_SEQNO FROM CRD_IDNO WHERE ID_NO = ? ) ";
		setString(1,idNo);
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			balanceAmts[0] = getValueLong("ACCT_AMT_BALANCE");
			balanceAmts[1] = getValueLong("ACCT_CASH_BALANCE");
		}
		return balanceAmts;
	}
	
	/**
	 * 商務卡要用此ID項下的所有商務卡卡號去查每張卡(同一個歸戶只能取一張)的可用餘額，取第一筆資訊即可(註：每一筆都會一樣) - 306個人
	 * @param idNo (代入crd_idno.id_no，即個人ID查詢)
	 * @return [可用餘額(ID_CORP_AMT_BALANCE)、預借現金餘額(ID_CORP_CASH_BALANCE)]
	 * @throws Exception
	 */
	public Long[] batchTotalCorpBalanceById(String idNo) throws Exception {
		Long[] balanceAmts = new Long[2];
		balanceAmts[0] = Long.valueOf(0);
		balanceAmts[1] = Long.valueOf(0);
		sqlCmd = "SELECT ID_CORP_AMT_BALANCE,ID_CORP_CASH_BALANCE FROM CCA_ACCT_BALANCE_CAL ";
		sqlCmd += " WHERE ACCT_TYPE <> '01' AND ACNO_FLAG = '3' ";
		sqlCmd += " AND ID_P_SEQNO IN (SELECT ID_P_SEQNO FROM CRD_IDNO WHERE ID_NO = ? ) LIMIT 1 ";
		setString(1,idNo);
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			balanceAmts[0] = getValueLong("ID_CORP_AMT_BALANCE");
			balanceAmts[1] = getValueLong("ID_CORP_CASH_BALANCE");
		}
		return balanceAmts;
	}
	
	/**
	 * by統編歸戶之商務卡(03+06)項下acct_type ，取第一筆資訊即可 (註：每一筆都會一樣) - 306公司
	 * @param corpNo (代入crd_corp.corp_no，即公司統編查詢)
	 * @return [可用餘額(CORP_AMT_BALANCE)、預借現金餘額(CORP_CASH_BALANCE)]
	 * @throws Exception
	 */
	public Long[] batchTotalCorpBalanceByCorp(String corpNo) throws Exception {
		Long[] balanceAmts = new Long[2];
		balanceAmts[0] = Long.valueOf(0);
		balanceAmts[1] = Long.valueOf(0);
		sqlCmd = "SELECT CORP_AMT_BALANCE,CORP_CASH_BALANCE FROM CCA_ACCT_BALANCE_CAL ";
		sqlCmd += " WHERE CORP_P_SEQNO IN (SELECT CORP_P_SEQNO FROM CRD_CORP WHERE CORP_NO = ? ) ";
		sqlCmd += " FETCH FIRST 1 ROWS ONLY ";
		setString(1,corpNo);
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			balanceAmts[0] = getValueLong("CORP_AMT_BALANCE");
			balanceAmts[1] = getValueLong("CORP_CASH_BALANCE");
		}
		return balanceAmts;
	}

}
