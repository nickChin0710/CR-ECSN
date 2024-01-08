/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei      updated for project coding standard      *
*  109-04-21  V1.00.01  Zuwei      code format                              *
*  109-06-12  V1.00.02  JUSTIN    ecsLogin -> adEcsLogin                                               *  
*  109-07-13  V1.00.03  JUSTIN    adEcsLogin -> adLogin
*  109-07-27  V1.00.04  Zuwei     coding standard      *
*  109-08-03  V1.00.05  Zuwei     fix code scan issue                       *
*  109-08-04  V1.00.06  JUSTIN    remove useless code
*  109-08-21  V1.00.07  JUSTIN    modify the content of the error message
*  109-09-25  V1.00.08  Justin    add errmsg when using online approve    *
*  109-09-28  V1.00.09  Alex      remove do not check DXC                   *
*  109-12-24  V1.00.10  Justin    parameterize sql and add set errorMsg
*  110-02-23  V1.00.11  Justin    ldapUrl -> ldapUrlArr 
*  111-01-04  V1.00.12  Justin    rename some variables and methods         *
******************************************************************************/
package ofcapp;
//主管覆核公用程式

import taroko.base.BaseSQL;
import taroko.com.TarokoParm;
import busi.func.EcsLoginFunc;

@SuppressWarnings({ "unchecked", "deprecation" })
public class EcsApprove extends BaseSQL {
	taroko.base.CommString commString = new taroko.base.CommString();

	taroko.com.TarokoCommon wp = null;

	public EcsApprove(taroko.com.TarokoCommon wr) {
		wp = wr;
	}

	public boolean getAuthRun(String aPgm, String aUser) {
		if (empty(aPgm) || empty(aUser))
			return false;

		// -權限-
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT sum(decode(A.aut_query,'Y',1,0)) as xx_cnt ")
		  .append(" FROM  sec_authority A, sec_user B")
		  .append(" WHERE A.user_level = B.usr_level ")
		  .append(" AND   lcase(A.wf_winid) = ? " )
		  .append(" AND   LOCATE(A.group_id,ucase(B.usr_group)) >0 ")
		  .append(" AND   B.usr_id = ? ");

		try {
			double cnt = getNumber(wp.getConn(), sb.toString(), aPgm.toLowerCase(), aUser);
			if (cnt > 0)
				return true;
		} catch (Exception ex) {
			errmsg("error: " + ex.getMessage());
			return false;
		}

		return false;
	}

	private boolean isBankUnitOfUserAndApproverTheSame(String aUser, String aAppr) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(*) as unit_cnt")
		  .append(" FROM sec_user A JOIN sec_user B")
		  .append("    ON decode(A.bank_unitno,'109','Z09',A.bank_unitno) ")
		  .append("     = decode(B.bank_unitno,'109','Z09',B.bank_unitno) ")
		  .append(" where A.usr_id =? " + " and B.usr_id =?");

		try {
			// sqlSelect(wp.getConn(), sql1, new Object[]{a_user, a_appr});
			double llCnt = getNumber(wp.getConn(), sb.toString(), aUser, aAppr);
			return (llCnt > 0);
		} catch (Exception ex) {
			errmsg("error: " + ex.getMessage());
			return false;
		}
	}

	private boolean checkAuthority(String approveProgram, String approverId, String approverPawd) {
		if (checkApproveRule(approveProgram, approverId, approverPawd) == false)
			return false;

		// -權限-
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT sum(decode(A.aut_approve,'Y',1,0)) as cnt_approve ")
		  .append(" FROM  sec_authority A, sec_user B")
		  .append(" WHERE A.user_level = B.usr_level ")
		  .append(" AND   A.wf_winid = ? ")
		  .append(" AND   LOCATE(A.group_id,ucase(B.usr_group)) >0 ")
		  .append(" AND   B.usr_id = ? ");

		try {
			double cnt = getNumber(wp.conn[0], sb.toString(), approveProgram.toLowerCase(), approverId);
			if (cnt <= 0) {
				errmsg("覆核主管: 無覆核權限; [%s]", approverId);
				return false;
			}
		} catch (Exception ex) {
			errmsg("error: " + ex.getMessage());
			return false;
		}

		return true;
	}

	private boolean checkApproveRule(String approveProgram, String approverId, String approverPwd) {
		if (empty(approveProgram) || empty(approverId) || empty(approverPwd)) {
			errmsg("[程式代碼/覆核主管/密碼]  不可空白");
			return false;
		}

		// --
		if (commString.eqIgno(approverId, wp.loginUser)) {
			errmsg("[覆核主管/維護經辦] 不可同一人");
			return false;
		}

		if (isBankUnitOfUserAndApproverTheSame(wp.loginUser, approverId) == false) {
			errmsg("經辦及主管 不是同一單位");
			return false;
		}

		return true;
	}

	public boolean checkAuthRun(String approveProgram, String approverId, String approverPwd) {
		if (checkApproveRule(approveProgram, approverId, approverPwd) == false)
			return false;

		// -檢查此覆核ID是否在此程式有覆核權限-
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT sum(decode(A.aut_query,'Y',1,0)) as cnt_approve ")
		  .append(" FROM  sec_authority A, sec_user B")
		  .append(" WHERE A.user_level = B.usr_level ")
		  .append(" AND   A.wf_winid = ? ")
		  .append(" AND   LOCATE(A.group_id,ucase(B.usr_group)) >0 ")
		  .append(" AND   B.usr_id = ? ");

		try {
			double llCnt = getNumber(wp.getConn(), sb.toString(), approveProgram.toLowerCase(), approverId);
			if (llCnt <= 0) {
				errmsg("覆核主管: 無覆核權限; [%s]", approverId);
				return false;
			}
		} catch (Exception ex) {
			errmsg("error: " + ex.getMessage());
			return false;
		}

		return checkPwd(approverId, approverPwd);
	}

	public boolean onlineApprove(String approveProgram, String approverId, String approverPawd) {

		if (!checkAuthority(approveProgram, approverId, approverPawd))
			return false;

		return checkPwd(approverId, approverPawd);

	}

	public boolean onlineApprove(String approverId, String approverPwd) {
		return onlineApprove(wp.modPgm(), approverId, approverPwd);
	}

	public boolean onlineApprove() {
		return onlineApprove(wp.modPgm(), wp.itemStr2("approval_user"), wp.itemStr2("approval_passwd"));
	}

	public boolean reportApprove(String pgm, String aprid, String passwd) {
		if (!checkAuthority(pgm, aprid, passwd))
			return false;

		return checkPwd(aprid, passwd);
	}

	public boolean reportApprove(String aprid, String passwd) {
		return reportApprove(wp.modPgm(), aprid, passwd);
	}

	public boolean reportApprove() {
		return onlineApprove(wp.modPgm(), wp.itemStr2("approval_user"), wp.itemStr2("approval_passwd"));
	}

	public boolean checkPwd(String approverId, String approverPwd) {
		boolean checkResult = false;
		
		// 如果系統設定為AD登入(adLogin = Y)，則使用LDAP驗證帳號密碼
		// 否則直接由ECS的TABLE做帳號密碼驗證
		if (TarokoParm.getInstance().getAdLogin().equalsIgnoreCase("Y")) {
			LdapAuth ldapAuth = new LdapAuth(wp);
			checkResult = ldapAuth.checkPasswordByLdap(TarokoParm.getInstance().getLdapUrlArr(), approverId, approverPwd);
			errmsg(ldapAuth.mesg());
		} else {
			EcsLoginFunc ecsLoginFunc = new EcsLoginFunc(wp);
			checkResult = ecsLoginFunc.checkEcsUserLogin(approverId, approverPwd);
			errmsg(ecsLoginFunc.getErrorMsg());
		}
		
		return checkResult;

	}

	public int specApprove(String approveUser, String specStatus) throws Exception {
		if (commString.eqIgno(specStatus, "91") == false) {
			return 1;
		}
		if (empty(approveUser)) {
			errmsg("覆核主管: 不可空白");
			return -1;
		}

		String sql1 = " select count(*) as xx_cnt from sec_user where usr_id =? and usr_level in ('A')";

		sqlSelect(wp.getConn(), sql1, new Object[] { approveUser });
		if (sqlRowNum > 0 && sqlNum("xx_cnt") > 0) {
			return 1;
		}

		errmsg("戶特指(91) 須甲級主管以上才可放行");
		return rc;
	}

	public int adjLimitApprove(String approveProgram, String approveUser, String apprpvePawd, double approveAmount)
			throws Exception {
		if (!checkAuthority(approveProgram, approveUser, apprpvePawd))
			return -1;

		if (!checkAmtLevelAdj(approveUser, approveAmount)) {
			return -1;
		}

		if (!checkPassList(approveUser, apprpvePawd))
			return -1;

		return 1;
	}

	public int adjLimitApprove(String approveUser, double approveAmount) throws Exception {
		if (!checkAmtLevelAdj(approveUser, approveAmount)) {
			return -1;
		}
		return 1;
	}

	private boolean checkAmtLevelAdj(String approveUser, double approveAmount) throws Exception {
		// --
		String sql1 = "select al_amt03 from sec_amtlimit"
				+ " where al_level in (select usr_amtlevel from sec_user where usr_id =?)" + " and al_level<>''";
		sqlSelect(wp.getConn(), sql1, new Object[] { approveUser });
		if (sqlRowNum <= 0) {
			errmsg("查無[%s]臨時額度層級", approveUser);
			return false;
		}

		if (approveAmount > sqlNum("al_amt03")) {
			errmsg("覆核人員[%s], 臨時額度層級不足[amt=%s,amt03=%s]", approveUser, approveAmount, sqlNum("al_amt03"));
			return false;
		}

		return true;
	}

	public int cardLimitApprove(String approveProgram, String approveUser, String approvePawd) throws Exception {
		if (!checkAuthority(approveProgram, approveUser, approvePawd))
			return -1;
		if (!checkPassList(approveUser, approvePawd))
			return -1;

		return 1;
	}

	private boolean checkAmtLevel(String approveUser, double approveAmount) throws Exception {
		// --
		String sql1 = "select al_amt, al_amt02 " + " from sec_amtlimit "
				+ " where al_level in (select usr_amtlevel from sec_user where usr_id =?)" + " and al_level<>''";
		sqlSelect(wp.getConn(), sql1, new Object[] { approveUser });
		if (sqlRowNum <= 0) {
			errmsg("查無[%s]額度層級", approveUser);
			return false;
		}

		if (approveAmount > sqlNum("al_amt")) {
			errmsg("覆核人員[%s], 額度層級不足", approveUser);
			return false;
		}

		return true;
	}

	private boolean checkPassList(String approveUser, String approvePawd) throws Exception {

		String sql1 = " select seq_no, chker_passwd, hex(rowid) as rowid " + " from cca_passwd_list"
				+ " where user_id =?" + " order by seq_no" + commSqlStr.rownum(1);

		sqlSelect(wp.getConn(), sql1, new Object[] { approveUser });
		if (sqlRowNum <= 0) {
			errmsg("主管未產生放行密碼..無法作業");
			return false;
		}
		if (!commString.eqIgno(approvePawd, sqlStr("chker_passwd"))) {
			errmsg("主管放行密碼[%s]錯誤..無法作業", sqlInt("seq_no"));
			return false;
		}

		sql1 = "delete cca_passwd_list where rowid =?";
		sqlExec(wp.getConn(), sql1, new Object[] { commSqlStr.strToRowid(sqlStr("rowid")) });
		if (sqlRowNum != 1) {
			errmsg("密碼使用刪除失敗");
			wp.rollbackOnly();
			return false;
		}
		wp.commitOnly();

		return true;
	}
}
