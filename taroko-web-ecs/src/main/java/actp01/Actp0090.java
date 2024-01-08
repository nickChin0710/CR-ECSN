/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-02-08  V1.00.00  Andy       program initial                            *
* 108-12-24  V1.00.01  Andy       Update  sysdate => online_date             *
* 108-12-26  V1.00.02  Andy       Update  add upload file check              *
* 109-06-16  V1.00.03  Andy       Update  Mantis3636                         *
* 109-10-12  V1.00.04  Amber      Update  Mantis:0004313                     *
* 111-10-24  V1.00.05  Yang Bo    sync code from mega                        *
******************************************************************************/

package actp01;

import ecsfunc.EcsCallbatch;
import taroko.com.TarokoFTP;
import taroko.com.TarokoFileAccess;

import java.text.SimpleDateFormat;
import java.util.Date;

import ofcapp.BaseAction;
import taroko.com.TarokoParm;

public class Actp0090 extends BaseAction {
	String mExBatchno = "";
	String mExEmbossSource = "";
	String mExEmbossReason = "";
	String msg = "";
	String mainSql = "";

	@Override
	public void userAction() throws Exception {

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
			// } else if (eq_igno(wp.buttonCode, "A")) {
			// /* 新增功能 */
			// insertFunc();
			// } else if (eq_igno(wp.buttonCode, "U")) {
			// /* 更新功能 */
			// updateFunc();
			// } else if (eq_igno(wp.buttonCode, "D")) {
			// /* 刪除功能 */
			// deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "S2")) {
			/* 執行 */
			strAction = "S2";
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "S3")) {
			/* 執行 */
			strAction = "S3";
			queryRead1();
		} else if (eqIgno(wp.buttonCode, "S4")) {
			/* 執行 */
			strAction = "S4";
			callProcess(); // Call Batch
//			call_batch();
		} else if (eqIgno(wp.buttonCode, "UPLOAD")) {
			procFunc();
		}
	}

	@Override
	public void initPage() {
		// 設定初始搜尋條件值
		String lsSql = "", lsMda = "";
		lsMda = "BANK700I";
		lsSql = "select media_name, "
				+ "from_path, "
				+ "to_path, "
				+ "external_name "
				+ "from ptr_media "
				+ "where media_name =:media_name ";
		setString("media_name", lsMda);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("ex_bankid", "700");
			wp.colSet("ex_mdaname", lsMda);
		//wp.colSet("ex_extfile", sqlStr("external_name"));
			wp.colSet("ex_frompath", sqlStr("from_path"));
			wp.colSet("ex_topath", sqlStr("to_path"));
			wp.colSet("ex_pgm", "ActB002");
			sqlSelect("select online_date from ptr_businday");
			wp.colSet("ex_busyymm1", strMid(sqlStr("online_date"), 0, 6));
			wp.colSet("ex_busyymm2", strMid(sqlStr("online_date"), 0, 6));
		} else {
			alertErr("初始化資料讀取錯誤!!");
		}
		// tab-init
		wp.colSet("optname", "aopt");
		wp.colSet("id_tab1", "id='tab_active'");
		wp.colSet("id_tab2", "");
	}

	// for query use only
	private boolean getWhereStr() throws Exception {
		String exMdaname = wp.itemStr("ex_mdaname");
		String exBusyymm1 = wp.itemStr("ex_busyymm1");
		String exBusyymm2 = wp.itemStr("ex_busyymm2");
		wp.whereStr = " where 1=1  ";
		// 固定條件

		// 自選條件
		wp.whereStr += sqlCol(exMdaname, "media_name");
		wp.whereStr += sqlStrend(exBusyymm1 + "01", exBusyymm2 + "31", "business_date");
		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
		queryRead1();
	}

	@Override
	public void queryRead() throws Exception {
		daoTid = "A-";
		// String[] aa_rowid = wp.item_buff("a-rowid");
		// wp.listCount[0] = aa_rowid.length;
		String exBusyymm1 = wp.itemStr("ex_busyymm1");
		String exBusyymm2 = wp.itemStr("ex_busyymm2");
		String lsFind = strMid(wp.itemStr("ex_mdaname"), 0, 7) + "O";

		wp.selectSQL = " hex(rowid) as rowid, mod_seqno,"
				+ "media_name, "
				+ "business_date, "
				+ "seq_no, "
				+ "external_name, "
				+ "program_name, "
				+ "proc_date, "
				+ "CASE proc_time WHEN '' THEN proc_time ELSE to_char(TO_DATE (proc_time,'HH24:Mi:SS'),'HH24:Mi:SS')  end proc_time, "
				+ "trans_date, "
				+ "CASE trans_time WHEN '' THEN trans_time ELSE to_char(TO_DATE (trans_time,'HH24:Mi:SS'),'HH24:Mi:SS')  end trans_time, "
				+ "out_file_flag, "
				+ "out_media_flag,  "
				+ "in_media_flag,   "
				+ "in_file_flag,    "
				+ "acct_bank,       "
				+ "acct_month,      "
				+ "s_stmt_cycle,    "
				+ "e_stmt_cycle,    "
				+ "value_date,      "
				+ "mod_user,        "
				+ "mod_time,        "
				+ "mod_pgm,         "
				+ "mod_seqno,       "
				+ "rowid,           "
				+ "batch_no,        "
				+ "total_rec,       "
				+ "right_rec,       "
				+ "recover_flag     ";
		wp.daoTable = " ptr_media_cntl ";
		wp.whereStr = " where 1=1 ";
		wp.whereStr += sqlCol(lsFind, "media_name");
		wp.whereStr += sqlStrend(exBusyymm1 + "01", exBusyymm2 + "31", "business_date");
		wp.whereOrder = " order by business_date desc ";

		// System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
		// + wp.whereStr + wp.whereOrder);
		// wp.pageCount_sql = "select count(*) from ( select " + wp.selectSQL
		// +"from" + wp.daoTable + wp.whereStr + wp.whereOrder + " )";

		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		// wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		// list_wkdata();
		daoTid = "a-";
	}

	void listWkdata() throws Exception {
		int rowCt = 0;
		String lsSql = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			// 計算欄位
			rowCt += 1;
		}
		wp.colSet("row_ct", intToStr(rowCt));
	}

	public void queryRead1() throws Exception {
		daoTid = "B-";
		wp.selectSQL = "hex(rowid) as rowid, mod_seqno,"
				+ "media_name, "
				+ "business_date, "
				+ "seq_no, "
				+ "external_name, "
				+ "program_name, "
				+ "proc_date, "
				+ "CASE proc_time WHEN '' THEN proc_time ELSE to_char(TO_DATE (proc_time,'HH24:Mi:SS'),'HH24:Mi:SS')  end proc_time, "
				+ "trans_date, "
				+ "CASE trans_time WHEN '' THEN trans_time ELSE to_char(TO_DATE (trans_time,'HH24:Mi:SS'),'HH24:Mi:SS')  end trans_time, "
				+ "out_file_flag, "
				+ "out_media_flag,  "
				+ "in_media_flag,   "
				+ "in_file_flag,    "
				+ "acct_bank,       "
				+ "acct_month,      "
				+ "s_stmt_cycle,    "
				+ "e_stmt_cycle,    "
				+ "value_date,      "
				+ "mod_user,        "
				+ "mod_time,        "
				+ "mod_pgm,         "
				+ "mod_seqno,       "
				+ "rowid,           "
				+ "batch_no,        "
				+ "total_rec,       "
				+ "right_rec,       "
				+ "recover_flag,    "
				+ "'0' db_optcode,  "
				+ "'0' db_btncheck, "
				+ "'' wk_mesg  ";
		wp.daoTable = " ptr_media_cntl  ";
		wp.whereOrder = "order by business_date desc ";
		getWhereStr();

		// System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
		// + wp.whereStr + wp.whereOrder);
		pageQuery();
		wp.setListCount(2);
		wp.notFound = "N";
		// for (int ii = 0; ii < wp.selectCnt; ii++) {
		// wp.colSet(ii,"cbc_mcht",wp.col_ss(ii,"dtl_value")+"["+wp.col_ss(ii,"mcht_chi_name")+"]");
		// }
		daoTid = "b-";
	}

	@Override
	public void querySelect() throws Exception {
		// m_ex_mcht_no = wp.itemStr("mcht_no");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {

	}

	@Override
	public void saveFunc() throws Exception {
		// -check approve-
		if (!checkApprove(wp.itemStr("zz_apr_user"),wp.itemStr("zz_apr_passwd"))) {
			return;
		}

		String[] opt1 = wp.itemBuff("opt1");
		String[] aaBatchNo = wp.itemBuff("batch_no");
		String dsSql = "", usSql = "";

		wp.listCount[0] = aaBatchNo.length;
		// check && update
		int llOk = 0, llErr = 0, rr = 0;
		// for (int ii = 0; ii < aa_batch_no.length; ii++) {
		// if (!checkBox_opt_on(ii, opt)) {
		// continue;
		// }
		// System.out.println("batchno : "+aa_batch_no[ii]);
		for (int ii = 0; ii < opt1.length; ii++) {
			rr = (int) this.toNum(opt1[ii]) - 1;
			if (rr < 0) {
				continue;
			}
			// delete act_pay_error
			dsSql = "delete act_pay_error  Where batch_no = :ls_batch_no ";
			setString("ls_batch_no", aaBatchNo[rr]);
			sqlExec(dsSql);
			if (sqlRowNum < 0) {
				msg = "DELETE act_pay_error Error!!";
				wp.colSet(rr, "wk_mesg", msg);
				wp.colSet(rr, "ok_flag", "X");
				llErr++;
				sqlCommit(0);
				continue;
			}

			// delete act_pay_batch
			dsSql = "DELETE act_pay_batch  Where batch_no = :ls_batch_no ";
			setString("ls_batch_no", aaBatchNo[rr]);
			sqlExec(dsSql);
			if (sqlRowNum < 0) {
				msg = "DELETE act_pay_batch Error!!";
				wp.colSet(rr, "wk_mesg", msg);
				wp.colSet(rr, "ok_flag", "X");
				llErr++;
				sqlCommit(0);
				continue;
			}

			// delete act_pay_detail
			dsSql = "DELETE act_pay_detail Where batch_no = :ls_batch_no ";
			setString("ls_batch_no", aaBatchNo[rr]);
			sqlExec(dsSql);
			if (sqlRowNum < 0) {
				msg = "DELETE act_pay_detail Error!!";
				wp.colSet(rr, "wk_mesg", msg);
				wp.colSet(rr, "ok_flag", "X");
				llErr++;
				sqlCommit(0);
				continue;
			}

			// update act_other_apay
			usSql = "update act_other_apay "
					+ "set status_code = '99' "
					+ "Where batch_no = :ls_batch_no ";
			setString("ls_batch_no", aaBatchNo[rr]);
			sqlExec(usSql);
			if (sqlRowNum < 0) {
				msg = "Update act_other_apay Error!!";
				wp.colSet(rr, "wk_mesg", msg);
				wp.colSet(rr, "ok_flag", "X");
				llErr++;
				sqlCommit(0);
				continue;
			}
			// delete act_b002r1
			dsSql = "DELETE act_b002r1 Where batch_no = :ls_batch_no ";
			setString("ls_batch_no", aaBatchNo[rr]);
			sqlExec(dsSql);
			if (sqlRowNum < 0) {
				msg = "DELETE act_b002r1 Error!!";
				wp.colSet(rr, "wk_mesg", msg);
				wp.colSet(rr, "ok_flag", "X 還原失敗");
				llErr++;
				sqlCommit(0);
				continue;
			}

			// update ptr_media_cntl
			usSql = "update ptr_media_cntl "
					+ "set recover_flag  = 'Y', "
					+ "in_file_flag  = 'N', "
					+ "total_rec     = 0, "
					+ "right_rec     = 0 "
					+ "Where batch_no = :ls_batch_no "
					+ "and substr(media_name,8,1) = 'O'";
			setString("ls_batch_no", aaBatchNo[rr]);
			sqlExec(usSql);
			if (sqlRowNum < 0) {
				msg = "Update ptr_media_cntl 1 Error!!";
				wp.colSet(rr, "wk_mesg", msg);
				wp.colSet(rr, "ok_flag", "X 還原失敗");
				llErr++;
				sqlCommit(0);
				continue;
			}

			// update ptr_media_cntl
			usSql = "update ptr_media_cntl "
					+ "set recover_flag  = 'Y', "
					+ "total_rec = 0, "
					+ "right_rec = 0 "
					+ "Where batch_no = :ls_batch_no "
					+ "and substr(media_name,8,1) = 'I'";
			setString("ls_batch_no", aaBatchNo[rr]);
			sqlExec(usSql);
			if (sqlRowNum < 0) {
				msg = "Update ptr_media_cntl 2 Error!!";
				wp.colSet(rr, "wk_mesg", msg);
				wp.colSet(rr, "ok_flag", "X");
				llErr++;
				sqlCommit(0);
				continue;
			} else {
				// System.out.println("sqlRowNum = "+sqlRowNum+" Update
				// ptr_media_cntl 2 OK!!");
				msg = "Recovery Success !!";
				wp.colSet(rr, "recover_flag", "Y");
				wp.colSet(rr, "ok_flag", "V");
				wp.colSet(rr, "total_rec", "0");
				wp.colSet(rr, "right_rec", "0");
				llOk++;
				sqlCommit(1);
			}
		}
		// alert_msg(msg);
		alertMsg("還原處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr + ";");
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	@Override
	public void dddwSelect() {
		try {
			// wp.initOption = "--";
			// wp.optionKey = wp.itemStr("ex_batchno");
			// this.dddw_list("dddw_batchno", "act_pay_error", "batch_no", "",
			// "where 1=1 and confirm_flag = 'Y' order by batch_no ");
			//
			// wp.initOption = "--";
			// wp.optionKey = wp.itemStr("ex_crtuser");
			// this.dddw_list("dddw_crtuser", "sec_user", "usr_id", "usr_cname",
			// "where 1=1 order by usr_id ");

		} catch (Exception ex) {
		}
	}

	@Override
	public void procFunc() throws Exception {
		if (itemIsempty("zz_file_name")) {
			alertErr("上傳檔名: 不可空白");
			return;
		}

		fileDataImp();
	}

	void fileDataImp() throws Exception {
		String lsSql = "", isSql = "", exBusinessDate = "", exTransTime = "";
		double exSeqno = 0;
		String exMediaName = wp.itemStr("ex_mdaname");
		String exActmm = wp.itemStr("ex_actmm");
		String exBankid = wp.itemStr("ex_bankid");
		String exCycle1 = wp.itemStr("ex_cycle1");
		String exCycle2 = wp.itemStr("ex_cycle2");
		String inputFile = wp.itemStr("zz_file_name");
		String exTopath = wp.itemStr("ex_topath");

		if (empty(exActmm)) {
			alertErr("請先點選它行原始檔案的資料!!");
			return;
		}
		// 上傳檔案檢核
		TarokoFileAccess tf = new TarokoFileAccess(wp);
		String lsActmm = "", lsOrigin = "";
		int llOk = 0, llErr = 0, llCnt = 0, llErrFormat = 0;
		double llAmt=0,llFamt=0;
		// int fi = tf.openInputText(inputFile,"UTF-8"); //決定上傳檔內碼
		int fi = tf.openInputText(inputFile, "MS950");
		if (fi == -1) {
			alertErr("上傳檔案錯誤!!");
			return;
		}
		lsActmm = numToStr(toNum(exActmm) - 191100, "###");
		lsOrigin = lsActmm + exCycle1 + exCycle2;
		while (true) {
			String ss = tf.readTextFile(fi);
			if (tf.endFile[fi].equals("Y")) {
				break;
			}
			if (ss.length() < 2) {
				continue;
			}
			String dataType = strMid(ss, 0, 1);
			//帳務月份與2 cycle值,新格式無法檢核停用  20191230 update
//			if (data_type.equals("1")) {
//				String ls_disk = strMid(ss, 55, 9);
//				if (!ls_disk.equals(ls_origin)) {
//					alertErr("上傳檔案標號不符1:" + ls_disk + " <> " + ls_origin);
//					return;
//				}
//			}
			if (dataType.equals("1")) {
				String dataStatus = strMid(ss,78,2);
				if(empty(dataStatus)){
					llOk++;
					llAmt += toNum(strMid(ss,43,11))/100;
				}
				llCnt ++;				
			}
			if (dataType.equals("2")) {
				String lsDisk = strMid(ss, 19, 7);
				int dataCt = Integer.parseInt(lsDisk);
				if (dataCt != llCnt) {
					alertErr("尾檔筆數與明細筆數不符   明細:" + llCnt + " <> 尾檔:" + dataCt);
					return;
				}
				String lsDisk1 = strMid(ss, 55, 7);
				int dataSuccess = Integer.parseInt(lsDisk1);
				if(dataSuccess != llOk ){
					alertErr("尾檔成功筆數與明細筆數不符   明細:" + llOk + " <> 尾檔:" + dataSuccess);
					return;
				}
				llFamt = toNum(strMid(ss,62,13))/100;
				if(llAmt != llFamt){
					alertErr("尾檔成功金額與明細筆金額散總不符   明細:" + llAmt + " <> 尾檔:" + llFamt);
					return;
				}
			}			
		}
		tf.closeInputText(fi);
		tf.deleteFile(inputFile);
		//
		try {
			TarokoFTP ftp = new TarokoFTP();
			// ftp.localPath = wp.workDir; //下載檔案位置
			ftp.localPath = TarokoParm.getInstance().getDataRoot() + "/upload";
			// ftp.fileName = "POST0001.DAT";
			ftp.fileName = inputFile;
		  wp.colSet("ex_extfile", inputFile);
			// ftp.set_remotePath(ex_topath); //set_remotePath :完整路徑
		//ftp.set_remotePath2("media/act"); // set_remotePath2 :media以後路徑
			ftp.setRemotePath2(""); // set_remotePath2 :media以後路徑

			ftp.ftpMode = "BIN";
			if (ftp.putFile(wp) != 0) {
				alertErr("上傳檔案失敗: ", ftp.fileName + "; err=" + ftp.getMesg());
			}
			msg = ftp.getMesg();
		} catch (Exception ex) {
			msg = ex.getMessage();
		}
		// online_date==>business_date
		lsSql = "SELECT NVL(online_date, ' ') online_date "
				+ "FROM ptr_businday ";
		sqlSelect(lsSql);
		exBusinessDate = sqlStr("online_date");
		//
		// ls_sql = "SELECT count(*) ct "
		// + "FROM ptr_media_cntl "
		// + "WHERE acct_bank = :ls_val1 "
		// + " and acct_month = :ls_val2 "
		// + " and s_stmt_cycle between :ls_val3 and :ls_val4 ) ";
		// setString("ls_val1",ex_bankid);
		// setString("ls_val2",ex_actmm);
		// setString("ls_val3",ex_cycle1);
		// setString("ls_val4",ex_cycle2);
		// sqlSelect(ls_sql);
		// if(sql_num("ct")==0){
		// alertErr("行庫在此帳期未產生自動扣繳資料 !");
		// return;
		// }
		// seq_no
		lsSql = "SELECT max(seq_no) as seq_no "
				+ " FROM ptr_media_cntl "
				+ " WHERE media_name = :ls_val1 AND "
				+ " business_date = :ls_busdate ";
		setString("ls_val1", exMediaName);
		setString("ls_busdate", exBusinessDate);
		sqlSelect(lsSql);
		if (empty(sqlStr("seq_no"))) {
			exSeqno = 1;
		} else {
			exSeqno = sqlNum("seq_no") + 1;
		}
		String dateStr = "";
		Date currDate = new Date();
		SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHH24mmssSSS");
		dateStr = form1.format(currDate);
		exTransTime = strMid(dateStr, 8, 6);

		// insert ptr_media_cntl
		isSql = "insert into ptr_media_cntl ( "
				+ "media_name, "
				+ "business_date, "
				+ "seq_no, "
				+ "external_name, "
				+ "program_name, "
				+ "trans_date, "
				+ "trans_time, "
				+ "in_media_flag, "
				+ "acct_bank, "
				+ "acct_month, "
				+ "s_stmt_cycle, "
				+ "e_stmt_cycle, "
				+ "value_date, "
				+ "mod_user, "
				+ "mod_time, "
				+ "mod_pgm, "
				+ "mod_seqno "
				+ ") values ( "
				+ ":media_name, "
				+ ":business_date, "
				+ ":seq_no, "
				+ ":external_name, "
				+ ":program_name, "
				+ ":trans_date, "
				+ ":trans_time, "
				+ ":in_media_flag, "
				+ ":acct_bank, "
				+ ":acct_month, "
				+ ":s_stmt_cycle, "
				+ ":e_stmt_cycle, "
				+ ":value_date, "
				+ ":mod_user, "
				+ "sysdate, "
				+ ":mod_pgm, "
				+ "1 "
				+ ")";
		setString("media_name", wp.itemStr("ex_mdaname"));
		setString("business_date", exBusinessDate);
		setString("seq_no", numToStr(exSeqno, "###"));
  //setString("external_name", wp.itemStr("ex_extfile"));
		setString("external_name", wp.colStr("ex_extfile"));
		setString("program_name", wp.itemStr("ex_pgm"));
		setString("trans_date", getSysDate());
		setString("trans_time", exTransTime);
		setString("in_media_flag", "Y");
		setString("acct_bank", exBankid);
		setString("acct_month", exActmm);
		setString("s_stmt_cycle", exCycle1);
		setString("e_stmt_cycle", exCycle2);
		setString("value_date", wp.itemStr("ex_valdate"));
		setString("mod_user", wp.loginUser);
		setString("mod_pgm", "Actp0090");
		sqlExec(isSql);
		if (sqlRowNum <= 0) {
			alertErr("新增自動扣繳資料失敗 !");
			return;
		}
		sqlCommit(1);
		// queryFunc();
		alertMsg("資料新增/上載成功!!");

	}

	//2020/10/12 call_batch改寫為新方法
	public void callProcess() throws Exception {
		String exPgname = wp.itemStr("ex_pgm");
		String msg = "", pram = "", prams = "";
		String mediaName = strMid(wp.itemStr("ex_mdaname"), 0, 7) + "O";
		String businessDate = itemkk("ex_business_date");
		String seqNo = wp.itemStr("ex_seqno");
		String iaKey1 = wp.itemStr("ex_mdaname");
		String iaKey2 = itemkk("data_k2");
		String iaKey3 = itemkk("data_k3");
		String iaKey4 = itemkk("data_k4");
		EcsCallbatch batch = new EcsCallbatch(wp);
		
		if (empty(exPgname)) {
			alertErr("請輸入 執行程式 !");
			return;
		}
		if (iaKey4.equals("Y")) {
			alertErr("處理程式不可重複執行!!");
			return;
		}

		prams = iaKey1 + " ";
		prams += iaKey2 + " ";
		prams += iaKey3 + " ";
		prams += mediaName + " ";
		prams += businessDate + " ";
		prams += seqNo;
		
		pram = exPgname.substring(0, 3) + "." + exPgname + " " + prams;
		rc = batch.callBatch(pram,wp.loginUser);

		wp.colSet("proc_mesg", exPgname+batch.getMesg());
		queryFunc();
	}

//	public int call_batch() throws Exception {
//		String ex_pgname = wp.itemStr("ex_pgm");
//		InetAddress sAddress = InetAddress.getByName(wp.request.getRemoteAddr());
//		String sendData = "";
//		String msg = "", pram = "";
//		// String media_name = item_kk("data_k1");
//		String media_name = strMid(wp.itemStr("ex_mdaname"), 0, 7) + "O";
//		String business_date = item_kk("ex_business_date");
//		// String seq_no = item_kk("data_k3");
//		String seq_no = wp.itemStr("ex_seqno");
//		String ia_key1 = wp.itemStr("ex_mdaname");
//		String ia_key2 = item_kk("data_k2");
//		String ia_key3 = item_kk("data_k3");
//		String ia_key4 = item_kk("data_k4");
//
//		if (empty(ex_pgname)) {
//			err_alert("請輸入 執行程式 !");
//			return -1;
//		}
//		if (ia_key4.equals("Y")) {
//			alertErr("處理程式不可重複執行!!");
//			return -1;
//		}
//		int rr = 0;
//
//		String lsSql = " select wf_value,wf_value2 from PTR_sys_parm where wf_parm='SYSPARM' and wf_key = 'CALLBATCH' ";
//		sqlSelect(lsSql);
//		// ip and port
//		String host = sqlStr("wf_value");
//		int port = (int) sql_num("wf_value2");
//
//		// seqno
//		String ls_mod_seqno = " select ecs_modseq.nextval AS MOD_SEQNO from dual ";
//		sqlSelect(ls_mod_seqno);
//
//		String MOD_SEQNO = sqlStr("MOD_SEQNO");
//
//		// 參數
//
//		// if(!empty(wp.itemStr("ex_pra"))){
//		// pram=wp.itemStr("ex_pra")+" ";
//		// }
//		pram = ia_key1 + " ";
//		pram += ia_key2 + " ";
//		pram += ia_key3 + " ";
//		pram += media_name + " ";
//		pram += business_date + " ";
//		pram += seq_no + " ";
//
//		// 傳送的參數
//		// sendData = "BilA001 1061101 NUATMTP10";
//		// sendData=wp.loginUser+" "+sAddress.getHostName()+"
//		// "+wp.itemStr("MOD_PGM")+" "+"/usr/bin/ksh"+"
//		// "+System.getenv("PROJ_HOME")+"/shell/"+wp.itemStr("ex_pgname")+"
//		// "+String.format("%020d", Long.valueOf(MOD_SEQNO));
//		// sendData=wp.itemStr("ex_pgname").substring(0,3)+"."+wp.itemStr("ex_pgname")+"
//		// "
//		// +pram
//		// +String.format("%020d", Long.valueOf(MOD_SEQNO));
//		sendData = ex_pgname.substring(0, 3) + "." + ex_pgname + " "
//				+ pram
//				+ String.format("%020d", Long.valueOf(MOD_SEQNO));
//
//		// insert ptr_callbatch
//		String ls_ins = "insert into ptr_callbatch ( "
//				+ " batch_seqno,  "
//				+ "program_code, "
//				+ "start_date,"
//				+ "user_id,"
//				+ "workstation_name,"
//				+ "client_program,"
//				+ "parameter_data"
//				+ ")values(  "
//				+ " :batch_seqno,"
//				+ " :program_code,"
//				+ " :start_date,"
//				+ " :user_id,"
//				+ " :workstation_name,"
//				+ " :client_program , "
//				+ " :parameter_data ) ";
//		setString("batch_seqno", String.format("%020d", Long.valueOf(MOD_SEQNO)));
//		setString("program_code", ex_pgname);
//		setString("start_date", get_sysDate());
//		setString("user_id", wp.loginUser);
//		setString("workstation_name", sAddress.getHostName());
//		setString("client_program", wp.itemStr("MOD_PGM"));
//		setString("parameter_data", sendData);
//		sqlExec(ls_ins);
//		if (sqlRowNum <= 0) {
//			msg = " ERROR:insert ptr_callbatch   \n";
//			return -1;
//		} else {
//			sql_commit(1);
//		}
//		Socket socket = null;
//
//		try {
//			socket = new Socket(host, port);
//			DataInputStream input = null;
//			DataOutputStream output = null;
//
//			msg += "Starting...  \n";
//
//			try {
//				while (true) {
//
//					output = new DataOutputStream(socket.getOutputStream());
//					msg += "Send data : [" + sendData + "] \n";
//					output.write(sendData.getBytes());
//					// output.writeUTF(sendData);
//					output.flush();
//
//					input = new DataInputStream(socket.getInputStream());
//					int inputLen = 0;
//					byte[] inData = new byte[2048];
//
//					inputLen = input.read(inData, 0, inData.length);
//					if (inputLen > 0) {
//						msg += "response data : [" + new String(inData, 0, inputLen) + "] \n";
//					} else {
//						msg += "無回傳資料   \n";
//					}
//					break;
//				}
//			} catch (Exception e) {
//				msg += "Exception : " + e.getMessage() + "  \n";
//			} finally {
//				if (input != null)
//					input.close();
//				if (output != null)
//					output.close();
//				msg += "Terminated..\n";
//				rr = 1;
//			}
//		} catch (IOException e) {
//			msg += "Exception2 : " + e.getMessage() + "  \n";
//			e.printStackTrace();
//		} finally {
//			if (socket != null)
//				socket.close();
//			// if(consoleInput != null ) consoleInput.close();
//			msg += "Socked Closed...  \n ";
//			// wp.colSet("proc_mesg", msg);
//		}
//		wp.colSet("proc_mesg", msg);
//		queryFunc();
//		return rr;
//	}
}
