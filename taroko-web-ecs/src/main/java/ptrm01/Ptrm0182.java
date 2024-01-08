/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-09-13  V1.00.01  Ryan       Initial                              *
***************************************************************************/
package ptrm01;

import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Ptrm0182 extends BaseEdit {
	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	busi.ecs.CommRoutine comr = null;
	ecsm01.Ecsm0620Func func = null;

	// ************************************************************************
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;
		wp.colSet("str_action", "");
		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
			strAction = "A";
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
			strAction = "U";
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "S1")) {
			updateCrdMailnoRange();
			if(strAction.equals("A")) {
				insertFunc();
			}
			if(strAction.equals("U")) {
				updateFunc();
			}
		} else if (eqIgno(wp.buttonCode, "A1")) {// 新增功能 -/
			strAction = "A";
			if(selectCrdMailnoRange()!=1) {
				insertFunc();
			}
		} else if (eqIgno(wp.buttonCode, "U1")) {// 修改功能 -/
			strAction = "U";
			if(selectCrdMailnoRange()!=1) {
				updateFunc();
			}
		} 
		dddwSelect();
		initButton();
	}

	// ************************************************************************
	@Override
	public void queryFunc() throws Exception {
		wp.whereStr = "WHERE 1=1 and B.msg_type ='MAIL_TYPE'" 
				+ sqlCol(wp.itemStr("ex_mail_type"), "A.mail_type")
				+ sqlCol(wp.itemStr("ex_inuse_flag"), "A.inuse_flag");

		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	// ************************************************************************
	@Override
	public void queryRead() throws Exception {

		wp.pageControl();

		wp.selectSQL = "hex(A.rowid) as rowid, " + "A.mail_type," + "A.min_mail_no," + "A.max_mail_no,"
				+ "A.inuse_flag," + "A.used_max_mail_no," + "B.msg";

		wp.daoTable = "crd_mailno_range A left join crd_message B on A.mail_type = B.msg_value ";
		wp.whereOrder = "order by A.mail_type,A.min_mail_no";

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.setPageValue();
	}

	// ************************************************************************
	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	// ************************************************************************
	@Override
	public void dataRead() throws Exception {
		String kkMailType = itemKk("data_k1");
		String kkMinMailNo = itemKk("data_k2");
		String kkMaxMailNo = itemKk("data_k3");
		if(empty(kkMailType))
			kkMailType = wp.itemStr("kk_mail_type");
		if(empty(kkMinMailNo))
			kkMinMailNo = wp.itemStr("kk_min_mail_no");
		if(empty(kkMaxMailNo))
			kkMaxMailNo = wp.itemStr("kk_max_mail_no");
		
		wp.selectSQL = "hex(A.rowid) as rowid, " + "A.mail_type," + "A.min_mail_no," + "A.max_mail_no,"
				+ "A.inuse_flag," + "A.used_max_mail_no," + "A.mod_user,"
				+ "to_char(A.mod_time,'yyyymmdd') as mod_date," + "B.msg";

		wp.daoTable = "crd_mailno_range A left join crd_message B on A.mail_type = B.msg_value ";
		wp.whereStr = "where 1=1 and B.msg_type ='MAIL_TYPE' ";
		wp.whereStr += sqlCol(kkMailType, "A.mail_type");
		wp.whereStr += sqlCol(kkMinMailNo, "A.min_mail_no");
		wp.whereStr += sqlCol(kkMaxMailNo, "A.max_mail_no");

		pageSelect();
		if (sqlNotFind()) {
			alertErr2("查無資料");
			return;
		}
	}

	// ************************************************************************
	public void saveFunc() throws Exception {
		ptrm01.Ptrm0182Func func = new ptrm01.Ptrm0182Func(wp);

		rc = func.dbSave(strAction);
		if (rc != 1) {
			alertErr2(func.getMsg());
		}else {
			if(strAction.equals("A"))
				alertMsg("新增成功");
			if(strAction.equals("U"))
				alertMsg("修改成功");
		}
		log(func.getMsg());
		this.sqlCommit(rc);
	}

	int selectCrdMailnoRange() throws Exception {
		String mailType = "";
		mailType = wp.itemStr("mail_type");
		if(empty(mailType))
			mailType = wp.itemStr("kk_mail_type");
		String lsSql = "select count(*) as tot_cnt from crd_mailno_range where mail_type = ? and inuse_flag ='Y' ";
		Object[] param = new Object[] { mailType };
		sqlSelect(lsSql, param);
		if (this.sqlInt("tot_cnt") > 0 && wp.itemEq("inuse_flag", "Y")) {
			wp.colSet("chk_inuse_flag", "Y");
			return 1;
		}
		return 0;
	}
	
	void updateCrdMailnoRange() throws Exception {
		busi.SqlPrepare sp = new SqlPrepare();
		String mailType = wp.itemStr("mail_type");
		if(empty(mailType))
			mailType = wp.itemStr("kk_mail_type");
		sp.sql2Update("crd_mailno_range");
		sp.ppstr("inuse_flag", "N");
		sp.sql2Where(" where mail_type = ? and inuse_flag = 'Y' ", mailType);
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if(sqlRowNum<0) {
			errmsg("update crd_mailno_range not found ,inuse_flag ='Y' ");
			return;
		}
		strAction = wp.itemStr("str_action");
	}

	// ************************************************************************
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
		wp.colSet("str_action", strAction);
	}

	// ************************************************************************
	@Override
	public void dddwSelect() {
		try {
			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_mail_type");
			if (wp.respHtml.indexOf("_detl") > 0) {
				wp.optionKey = wp.itemStr("kk_mail_type");
			}
			dddwList("dddw_mail_type", "crd_message", "msg_value", "msg", "where 1=1 and msg_type = 'MAIL_TYPE'");

		} catch (Exception e) {
		}

	}

	// ************************************************************************
	@Override
	public void initPage() {
		return;
	}
	// ************************************************************************

} // End of class
