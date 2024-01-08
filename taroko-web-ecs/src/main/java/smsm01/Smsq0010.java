/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/22  V1.00.01   Allen Ho      Initial                              *
* 109-04-29  V1.00.02  Tanwei       updated for project coding standard
* 109-12-24   V1.00.03 Justin         parameterize sql
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
***************************************************************************/
package smsm01;

import smsm01.Smsq0010Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import java.util.StringTokenizer;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Smsq0010 extends BaseEdit {
	private String PROGNAME = "簡訊查詢處理程式109/12/24 V1.00.03";
	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	busi.ecs.CommRoutine comr = null;
	smsm01.Smsq0010Func func = null;
	String rowid;
	String orgTabName = "sms_msg_dtl";
	String controlTabName = "";
	int qFrom = 0;
	String tranSeqStr = "";
	String batchNo = "";
	int errorCnt = 0, rec_cnt = 0, notify_cnt = 0, colNum = 0;
	int[] datachkCnt = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	String[] uploadFileCol = new String[50];
	String[] uploadFileDat = new String[50];
	String[] logMsg = new String[20];

	// ************************************************************************
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

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
		} else if (eqIgno(wp.buttonCode, "NILL")) {/* nothing to do */
			strAction = "";
			wp.listCount[0] = wp.itemBuff("ser_num").length;
		}

		dddwSelect();
		initButton();
	}

	// ************************************************************************
	@Override
	public void queryFunc() throws Exception {
		if (queryCheck() != 0)
			return;
		wp.whereStr = "WHERE 1=1 " + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date")
				+ sqlCol(wp.itemStr("ex_msg_id"), "a.msg_id", "like%")
				+ sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%") + sqlChkEx(wp.itemStr("ex_id_no"), "1", "")
				+ sqlCol(wp.itemStr("ex_cellar_phone"), "a.cellar_phone", "like%")
				+ sqlCol(wp.itemStr("ex_cellphone_check_flag"), "a.cellphone_check_flag", "like%")
				+ sqlCol(wp.itemStr("ex_msg_dept"), "a.msg_dept", "like%")
				;
		
		if(wp.itemEq("ex_proc_flag", "Y")) {
			wp.whereStr += " and proc_flag = 'Y' ";
		}	else if(wp.itemEq("ex_proc_flag", "N"))	{
			wp.whereStr += " and proc_flag <> 'Y' ";
		}

		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	// ************************************************************************
	@Override
	public void queryRead() throws Exception {
		if (wp.colStr("org_tab_name").length() > 0)
			controlTabName = wp.colStr("org_tab_name");
		else
			controlTabName = orgTabName;

		wp.pageControl();

		wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, " + "'' as id_no,"
				+ "a.chi_name," + "a.msg_id," + "a.cellar_phone," + "a.crt_date," + "a.msg_dept," + "a.crt_user,"
				+ "a.cellphone_check_flag," + "a.msg_seqno," + "a.send_flag," + "a.id_p_seqno , a.proc_flag ";

		wp.daoTable = controlTabName + " a ";
		wp.whereOrder = " " + " order by crt_date desc";

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		commIdNo("comm_id_no");
		commDeptName("comm_msg_dept");

		// list_wkdata();
		wp.setPageValue();
	}

	// ************************************************************************
	@Override
	public void querySelect() throws Exception {

		rowid = itemKk("data_k1");
		qFrom = 1;
		dataRead();
	}

	// ************************************************************************
	@Override
	public void dataRead() throws Exception {
		if (controlTabName.length() == 0) {
			if (wp.colStr("control_tab_name").length() == 0)
				controlTabName = orgTabName;
			else
				controlTabName = wp.colStr("control_tab_name");
		} else {
			if (wp.colStr("control_tab_name").length() != 0)
				controlTabName = wp.colStr("control_tab_name");
		}
		wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.msg_dept," + "a.msg_userid,"
				+ "a.msg_pgm," + "'' as id_no," + "a.chi_name," + "a.acct_type," + "a.msg_seqno," + "a.msg_id,"
				+ "a.cellar_phone," + "a.cellphone_check_flag," + "a.msg_desc," + "a.resend_flag," + "a.send_flag,"
				+ "a.create_txt_date," + "a.create_txt_time," + "a.chi_name_flag," + "a.sms24_flag," + "a.crt_user,"
				+ "a.crt_date," + "a.apr_user," + "a.apr_date," + "a.mod_pgm,"
				+ "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.id_p_seqno , a.proc_flag";

		wp.daoTable = controlTabName + " a ";
		wp.whereStr = "where 1=1 ";
		if (qFrom == 0) {
			wp.whereStr = wp.whereStr;
		} else if (qFrom == 1) {
			wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
		}

		pageSelect();
		if (sqlNotFind()) {
			alertErr2("查無資料, key= " + "[" + rowid + "]");
			return;
		}
		commDeptName("comm_msg_dept");
		commAcctType("comm_acct_type");
		checkButtonOff();
		dataReadWkdata();
		composeMsgDesc();
	}

	// ************************************************************************
	void dataReadWkdata() {
		int ii = 0;
		String sql1 = "";

		if (wp.colStr("id_p_seqno").length() != 0) {
			sql1 = "select " + " id_no as id_no, " + " chi_name as chi_name " + " from crd_idno "
					+ " where id_p_seqno = ? ";
			setString(wp.colStr("id_p_seqno"));
		}
		
		sqlSelect(sql1);
		wp.colSet("id_no", sqlStr("id_no"));

	}

	void composeMsgDesc() {
		if (wp.colEmpty("msg_id"))
			return;
		String msgDesc = "", sql1 = "", msgParm = "" , lsTemp = "";
		int i = 0, p = 0;
		msgParm = wp.colStr("msg_desc");
		sql1 = " select msg_content from sms_msg_content where msg_id = ? ";
		sqlSelect(sql1, new Object[] { wp.colStr("msg_id") });
		if (sqlRowNum <= 0)
			return;
		msgDesc = sqlStr("msg_content");
		String[] lsParm = new String[9];
//		StringTokenizer st = new StringTokenizer(msgParm, ",");
		String[] st = msgParm.split(",");	
//		while (st.hasMoreTokens()) {
		while (i<st.length) {
//			lsTemp = st.nextToken();
			lsTemp = st[i];
			i++;
			if (i < 4)
				continue;
			lsParm[p] = lsTemp;
			p++;
			continue;
		}
		
		for(int ii=0;ii<p;ii++) {
			msgDesc = msgDesc.replace("<>#"+ii+"<>", lsParm[ii]).replace("<#"+ii+">", lsParm[ii]);
		}
		
		wp.colSet("msg_desc", msgDesc);
	}

	// ************************************************************************
	public void saveFunc() throws Exception {
		smsm01.Smsq0010Func func = new smsm01.Smsq0010Func(wp);

		rc = func.dbSave(strAction);
		if (rc != 1)
			alertErr2(func.getMsg());
		log(func.getMsg());
		this.sqlCommit(rc);
	}

	// ************************************************************************
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	// ************************************************************************
	@Override
	public void dddwSelect() {
		String lsSql = "";
		try {
			if ((wp.respHtml.equals("smsq0010"))) {
				wp.initOption = "--";
				wp.optionKey = "";
				if (wp.colStr("ex_msg_dept").length() > 0) {
					wp.optionKey = wp.colStr("ex_msg_dept");
				}
				this.dddwList("dddw_dept_code", "ptr_dept_code", "trim(dept_code)", "trim(dept_name)", " where 1 = 1 ");
			}
		} catch (Exception ex) {
		}
	}

	// ************************************************************************
	public int queryCheck() throws Exception {
		if ((itemKk("ex_crt_date_s").length() == 0) && (itemKk("ex_id_no").length() == 0)
				&& (itemKk("ex_crt_date_e").length() == 0)) {
			alertErr2("身份證字號與新增日期 不可都為空白");
			return (1);
		}

		if ((itemKk("ex_crt_date_s").length() != 0) && (itemKk("ex_crt_date_e").length() == 0)) {
			wp.itemSet("ex_crt_date_e", "30001231");
		}

		if ((itemKk("ex_crt_date_e").length() != 0) && (itemKk("ex_crt_date_s").length() == 0)) {
			wp.itemSet("ex_crt_date_s", "10001231");
		}
		String sql1 = "";
		if (wp.itemStr("ex_id_no").length() == 10) {
			sql1 = "select id_p_seqno " + "from crd_idno " + "where  id_no  =  ? and    id_no_code   = '0' ";
			setString(wp.itemStr("ex_id_no").toUpperCase());
			sqlSelect(sql1);
			if (sqlRowNum <= 0) {
				alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
				return (1);
			}
			wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
			return (0);
		}

		return (0);
	}

	// ************************************************************************
	public String sqlChkEx(String exCol, String sqCond, String fileExt) {
		if (sqCond.equals("1")) {
			if (empty(wp.itemStr("ex_id_no")))
				return "";
			setString(wp.colStr("ex_id_p_seqno"));
			return " and id_p_seqno = ? ";
		}

		return "";
	}

	// ************************************************************************
	public void commDeptName(String columnData1) throws Exception {
		String columnData = "";
		String sql1 = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			if (wp.colStr(ii, "msg_dept").length() == 0)
				continue;
			columnData = "";
			sql1 = "select " + " dept_name as column_dept_name " + " from ptr_dept_code " + " where 1 = 1 "
					+ " and   dept_code = ? ";
			setString(wp.colStr(ii, "msg_dept"));
			
			sqlSelect(sql1);

			if (sqlRowNum > 0)
				columnData = columnData + sqlStr("column_dept_name");
			wp.colSet(ii, columnData1, columnData);
		}
		return;
	}

	// ************************************************************************
	public void commAcctType(String columnData1) throws Exception {
		String columnData = "";
		String sql1 = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			if (wp.colStr(ii, "acct_type").length() == 0)
				continue;
			columnData = "";
			sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type " + " where 1 = 1 "
					+ " and   acct_type = ? ";
			setString(wp.colStr(ii, "acct_type"));
			sqlSelect(sql1);

			if (sqlRowNum > 0)
				columnData = columnData + sqlStr("column_chin_name");
			wp.colSet(ii, columnData1, columnData);
		}
		return;
	}

	// ************************************************************************
	public void commIdNo(String columnData1) throws Exception {
		String columnData = "";
		String sql1 = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			if (wp.colStr(ii, "id_p_seqno").length() == 0)
				continue;
			columnData = "";
			sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 " + " and   id_p_seqno = ? ";
			setString(wp.colStr(ii, "id_p_seqno"));
			sqlSelect(sql1);

			if (sqlRowNum > 0)
				columnData = columnData + sqlStr("column_id_no");
			wp.colSet(ii, columnData1, columnData);
		}
		return;
	}

	// ************************************************************************
	public void checkButtonOff() throws Exception {
		return;
	}

	// ************************************************************************
	@Override
	public void initPage() {
		return;
	}
	// ************************************************************************

} // End of class
