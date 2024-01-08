/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-27  V1.00.01  ryan       program initial                            *
* 111-10-24  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package actp01;


import ecsfunc.EcsCallbatch;
import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFTP;
import taroko.com.TarokoParm;


public class Actp0070 extends BaseProc {
	AppMsg appMsg = new AppMsg();
	int rr = -1;
	String msg = "";
	String kk1 = "",kk2="";
	int ilOk = 0;
	int ilErr = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		// ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
		// wp.respCode + ",rHtml=" + wp.respHtml);
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			dataProcess();
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
		} else if (eqIgno(wp.buttonCode, "UPLOAD")) {
			/* UPLOAD */
			strAction = "UPLOAD";
			procFunc();
		} else if (eqIgno(wp.buttonCode, "S2")) {
			strAction = "S2";
			initPage();
		} 

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		String[] rowid = wp.itemBuff("rowid");
		if(empty(rowid[0])){
			wp.listCount[0] = 0;
			wp.pageRows=999;
		}else{
			wp.listCount[0] = rowid.length;
		}
    /***
		String sql_select = "select from_path, to_path from ptr_media where media_name = :media_name";
		String ex_media_name = wp.itemStr("ex_media_name");
		if(empty(ex_media_name)){
			ex_media_name = "POST001I";
		}
		setString("media_name",ex_media_name);
		sqlSelect(sql_select);	
		String to_path = sqlStr("to_path");
		wp.colSet("ex_serverpath", to_path);
    ***/

		String toPath = "";
		String sqlSelectRefip2 = "select remote_dir "
		                         + "from ecs_ref_ip_addr where ref_ip_code = 'TAROKO_FTP' ";
		sqlSelect(sqlSelectRefip2);

	  if(sqlRowNum>0){
     	toPath   =sqlStr("remote_dir") + "/" + "media/act";
	  }
		wp.colSet("ex_serverpath", toPath);

	}

	@Override
	public void dddwSelect() {

	}

	//for query use only
		private void getWhereStr() throws Exception {

			wp.whereStr = " where 1=1 and in_media_flag = 'Y' ";
			if (!empty(wp.itemStr("ex_bankid"))) {
				wp.whereStr += " and  acct_bank = :ex_bankid ";
				setString("ex_bankid",wp.itemStr("ex_bankid"));
			}
			
			if (wp.itemStr("ex_bankid").equals("700")) {
				wp.whereStr += " and  media_name = 'POST001I' ";
			}
			if (wp.itemStr("ex_bankid").equals("711")) {
			  wp.whereStr += " and  media_name = 'POST711I' ";
			//wp.whereStr += " and  media_name in ('POST711I', 'POST711L') ";
			}
			if (wp.itemStr("ex_bankid").equals("716")) {
			  wp.whereStr += " and  media_name = 'POST716I' ";
			}
			if (wp.itemStr("ex_bankid").equals("712")) {
				wp.whereStr += " and  media_name = 'POST712I' ";
			}
			if (wp.itemStr("ex_bankid").equals("713")) {
				wp.whereStr += " and  media_name = 'POST713I' ";
			}
			if (wp.itemStr("ex_bankid").equals("714")) {
				wp.whereStr += " and  media_name = 'POST714I' ";
			}
			if (wp.itemStr("ex_bankid").equals("715")) {
				wp.whereStr += " and  media_name = 'POST715I' ";
			}
		}
	
	@Override
	public void queryFunc() throws Exception {

		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " hex(rowid) as rowid, " 
		             + " media_name, "
		             + " business_date, "
		             + " (case when business_date > '99999999' then '00000000' else business_date end) as sort_date, "
		             + " seq_no, "
		             + " external_name, "
		             + " program_name, "
		             + " total_rec, "
		             + " pgm_memo, "
				     + " proc_date,"
				     + " proc_time,"
				     + " trans_date,"
				     + " trans_time,"
				     + " out_file_flag,"
				     + " out_media_flag,"
				     + " in_media_flag,"
				     + " in_file_flag, "
				     + " acct_bank,"
				     + " acct_month,"
				     + " s_stmt_cycle,"
				     + " e_stmt_cycle, "
				     + " value_date, "
				     + " mod_user, "
				     + " mod_time, "
				     + " mod_pgm, "
				     + " mod_seqno "
				     ;
		wp.daoTable = " ptr_media_cntl ";
		wp.whereOrder = " order by sort_date desc,seq_no desc ";
		getWhereStr();
		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		//wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		listWkdata();
	}


	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		
	}

	@Override
	public void dataProcess() throws Exception {

		String[] aaRowid = wp.itemBuff("rowid");
		String[] aaOpt = wp.itemBuff("opt");
		String[] aaMediaName = wp.itemBuff("media_name");
		String[] aaBusinessDate = wp.itemBuff("business_date");
		String[] aaInFileFlag = wp.itemBuff("in_file_flag");
		String[] aaSeqNo = wp.itemBuff("seq_no");
		String lsBpgm = wp.itemStr("ex_bpgm");
		wp.listCount[0] = aaMediaName.length;
		if(aaOpt.length>1){
			alertErr("一次只能勾選一個");
			return;
		}
		// -update-
		for ( rr = 0; rr < aaMediaName.length; rr++) {
			if (!checkBoxOptOn(rr, aaOpt)) {
				continue;
			}
			if(!aaInFileFlag[rr].equals("N")){
				wp.colSet(rr, "ok_flag", "!");
				alertErr("處理碼為N才可執行");
				return;
			}
			if(fCallBatch(aaMediaName[rr],aaBusinessDate[rr],aaSeqNo[rr])!=1){
				wp.colSet(rr, "ok_flag", "!");
				ilErr++;
				continue;
			}	
			String sqlUpdate="update ptr_media_cntl set "
							+ " in_file_flag = 'R',"
							+ " program_name = :ls_bpgm "
							+ " where hex(rowid) = :rowid ";
			setString("ls_bpgm",lsBpgm);
			setString("rowid",aaRowid[rr]);
			sqlExec(sqlUpdate);
			ilOk++;
			wp.colSet(rr, "ok_flag", "V");
		}
		if(ilErr >0)
			alertMsg("啟動批次處理失敗 !");
		else
			alertMsg("啟動批次處理成功 !");
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}
	
	void listWkdata(){
	}
	
	int wfUpdMediaCntl(String fileName) throws Exception{
		String sqlSelect=" select business_date from ptr_businday ";
		sqlSelect(sqlSelect);
		String lsBusdate = sqlStr("business_date");
		String lsMediaName = wp.itemStr("ex_media_name");
		if(empty(lsBusdate)){
			lsBusdate = this.getSysDate();
		}
		sqlSelect ="select max(seq_no)+1 as li_seqno "
				+ " from ptr_media_cntl "
				+ " where media_name = :ls_media_name "
				+ " and business_date = :ls_busdate ";
		setString("ls_media_name",lsMediaName);
		setString("ls_busdate",lsBusdate);
		sqlSelect(sqlSelect);
		String liSeqno = sqlStr("li_seqno");
		if(empty(liSeqno)||liSeqno.equals("0")){
			liSeqno = "1";
		}
		String sqlInsert=" insert into ptr_media_cntl( "
						+ " media_name, "
						+ " business_date, "
						+ " seq_no, "
						+ " external_name, "
						+ " trans_date, "
						+ " trans_time, "
						+ " in_media_flag, "
						+ " in_file_flag, "
						+ " acct_bank "
						+ " )values( "
						+ " :ls_media_name, "
						+ " :ls_busdate, "
						+ " :li_seqno, "
						+ " :ex_file, "
						+ " to_char(sysdate,'yyyymmdd'), "
						+ " to_char(sysdate,'hh24miss'), "
						+ " 'Y', "
						+ " 'N', "
						+ " :ex_bankid) ";
		setString("ls_media_name",lsMediaName);
		setString("ls_busdate",lsBusdate);
		setString("li_seqno",liSeqno);
		setString("ex_file",fileName);
		setString("ex_bankid",wp.itemStr("ex_bankid"));
		sqlExec(sqlInsert);

		if(sqlRowNum<=0){
			return -1;
		}
		return 1;
	}
	int fCallBatch(String mediaName , String businessDate , String seqNo) throws Exception{

		EcsCallbatch batch = new EcsCallbatch(wp);
		
		// --callbatch
		rc = batch.callBatch(wp.itemStr("ex_bpgm")+" " + mediaName + " " + businessDate+" " + seqNo);
		if (rc != 1) {
			//err_alert("帳戶 Action 處理: callbatch 失敗");
			wp.colSet("proc_mesg", "帳戶 Action 處理: callbatch 失敗");
			wp.colSet(rr, "ok_flag","!");
			return -1;
		}
		//alert_msg("callBatch OK; Batch-seqno=" + batch.batch_Seqno());
		wp.colSet("proc_mesg", "callBatch OK; Batch-seqno=" + batch.batchSeqno());
		wp.colSet(rr, "ok_flag","V");
		return 1;
	}
	
	public void procFunc() throws Exception {
		if (itemIsempty("zz_file_name")) {
			alertErr("上傳檔名: 不可空白");
			return;
		}
		if(wfUpdMediaCntl(wp.itemStr("zz_file_name"))!=1){
			alertMsg("Update 媒體控制檔失敗 ");
			return;
		}
		wp.colSet("ex_fromfile",  wp.itemStr("zz_file_name"));
		fCallFtp();
	}
	
	int fCallFtp(){
		String inputFile = wp.itemStr("zz_file_name");
		try {
			TarokoFTP ftp = new TarokoFTP();
			//ftp.localPath = wp.workDir; //下載檔案位置
			ftp.localPath = TarokoParm.getInstance().getDataRoot() + "/upload";//從本機上傳
			//ftp.localPath = "/ECS/EcsWeb/data/upload"; // 10.1.109.1上傳檔案位置
			//ftp.remotePath = "/BANK/MEGA/ecs/media/act";
			// ftp.fileName = "POST0001.DAT";
			//ftp.set_remotePath(ex_topath);						//set_remotePath  :完整路徑
			ftp.setRemotePath2("media/act");						//set_remotePath2 :media以後路徑			
			ftp.fileName = inputFile;

			ftp.ftpMode = "BIN";
		//if (ftp.put_File(wp) != 0) {, temp change
			if (ftp.putFile(wp,false) != 0) {
				alertErr("上傳檔案失敗: ", ftp.fileName + "; err=" + ftp.getMesg());
				return -1;
			}
			msg = ftp.getMesg();
		} catch (Exception ex) {
			msg = ex.getMessage();
			alertErr("上傳檔案失敗: ,"+msg);
			return -1;
		}
		alertMsg("上傳檔案成功"+msg);
		//wp.colSet("proc_mesg", msg);
		return 1;
	}
}
