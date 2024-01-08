/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-02-09  V1.00.00  Andy       program initial                            *
* 108-12-20  V1.00.01  Andy       Update                                     *
* 109-06-16  V1.00.03  Andy       Update  Mantis3635                         * 
* 111-10-24  V1.00.04  Yang Bo    sync code from mega                        *
******************************************************************************/

package actp01;


import java.text.SimpleDateFormat;
import java.util.Date;

import ecsfunc.EcsCallbatch;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFTP;
import taroko.com.TarokoParm;

public class Actp0080 extends BaseEdit {
	String mExBatchno = "";
	String mExEmbossSource = "";
	String mExEmbossReason = "";
	String msg = "";
	String businessDate = "";
	String callbatchSeqno = "";
	int lsSeqNo = 0;
	String hMaxBusinessDate = "";
	int hMaxSeqNo = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

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
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();
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
	  } else if (eqIgno(wp.buttonCode, "F")) {
		/* FTP */
	    strAction = "F";
	  	fileWriter();
		} else if (eqIgno(wp.buttonCode, "S4")) {
			/* 執行 */
			strAction = "S4";
		//call_batch();
			callProcess(); // 改成 call ecsfunc.ecsCallbatch
		}	

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		// 設定初始搜尋條件值
		String lsSql = "", lsMda = "";
		lsMda = "BANK700O";
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
			wp.colSet("ex_extfile", sqlStr("external_name"));
			wp.colSet("ex_frompath", sqlStr("from_path"));
			wp.colSet("ex_topath", sqlStr("to_path"));
			wp.colSet("ex_pgname", "ActB001");
			sqlSelect("select online_date from ptr_businday");
			wp.colSet("ex_busyymm1", strMid(sqlStr("online_date"),0,6));
			wp.colSet("ex_busyymm2", strMid(sqlStr("online_date"),0,6));
		} else {
			alertErr("初始化資料讀取錯誤!!");
		}
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
		wp.whereStr += sqlStrend(exBusyymm1+"01",exBusyymm2+"31","business_date");
		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		getWhereStr();
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {

		wp.pageControl();

		wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
				+ "media_name,   	"
				+ "business_date,   "
				+ "seq_no,          "
				+ "external_name,   "
				+ "program_name,    "
				+ "proc_date,       "
				+ "CASE proc_time WHEN '' THEN proc_time ELSE to_char(TO_DATE (proc_time,'HH24:Mi:SS'),'HH24:Mi:SS')  end proc_time, "
				//+ "proc_time,       "
				+ "trans_date,      "
				+ "CASE trans_time WHEN '' THEN trans_time ELSE to_char(TO_DATE (trans_time,'HH24:Mi:SS'),'HH24:Mi:SS')  end trans_time, "
				//+ "trans_time,      "
				+ "out_file_flag,   "
				+ "out_media_flag,  "
				+ "in_media_flag,   "
				+ "in_file_flag,    "
				+ "acct_bank,       "
				+ "acct_month,      "
				+ "s_stmt_cycle,    "
				+ "e_stmt_cycle,    "
				+ "value_date,      "
				+ "mod_user,        "
				+ "mod_time,   		"
				+ "mod_pgm,         "
				+ "mod_seqno        ";
		wp.daoTable = " ptr_media_cntl ";
		wp.whereOrder = "order by business_date desc ";		
		getWhereStr();

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

		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		getLastfileInfo();
		//若要允許產生一個以上不同的自動扣繳檔檔名，則建議於 ptr_media_cntl 新增一欄位，於產生自動扣繳檔時，
		//更新此欄位以記錄相同 external_name 下，何筆為最近產生自動扣繳檔
		listWkdata();
	}

	void getLastfileInfo() throws Exception {
		hMaxBusinessDate = "";
		hMaxSeqNo = 0;
		String exMdaname = wp.itemStr("ex_mdaname");
		String lsBusinessDate = "";

		String lsSql = "select max(business_date) as max_business_date "
				+ "from ptr_media_cntl "
				+ "where media_name = :ps_mdaname "
				+ "and out_file_flag='Y' and in_file_flag<>'Y' ";
		setString("ps_mdaname", exMdaname);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			hMaxBusinessDate = sqlStr("max_business_date");
		  lsSql = "select max(seq_no) as max_seq_no "
		    		+ "from ptr_media_cntl "
			    	+ "where media_name = :ps_mdaname "
			  	  + "and business_date = :ps_business_date "
				    + "and out_file_flag='Y' and in_file_flag<>'Y' ";
		  setString("ps_mdaname", exMdaname);
		  setString("ps_business_date", hMaxBusinessDate);
		  sqlSelect(lsSql);
		  if (sqlRowNum > 0) {
  			hMaxSeqNo = sqlInt("max_seq_no");
		  }
		}
    
  }

	void listWkdata() throws Exception {
//		int row_ct = 0;
//		String ls_sql = "";
		int liSeqNo = 0;
		for (int ii = 0; ii < wp.selectCnt; ii++) {
      liSeqNo = Integer.parseInt(wp.colStr(ii,"seq_no"));
		  if (wp.colStr(ii,"business_date").equals(hMaxBusinessDate) && 
		      (liSeqNo == hMaxSeqNo) ) {
          wp.colSet(ii,"dnload_available","Y");
          wp.colSet(ii,"btndownload_disable","");
		  } else {
          wp.colSet(ii,"dnload_available","N");
          wp.colSet(ii,"btndownload_disable","disabled style='background: lightgray;'");
		  }
		}

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
		String exBankid = wp.itemStr("ex_bankid");
		String exMdaname = wp.itemStr("ex_mdaname");
		String exActmm = wp.itemStr("ex_actmm");
		String exCycle1 = wp.itemStr("ex_cycle1");
		String exCycle2 = wp.itemStr("ex_cycle2");
		String exVdate = wp.itemStr("ex_vdate");
		String exExtfile = wp.itemStr("ex_extfile");
		
		String lsSql = "",isSql="",exProcTime="";
		String parm = "";
	  String[] aaBusinessDate = wp.itemBuff("business_date");
	  wp.listCount[0] = aaBusinessDate.length;
		int rr = 0;
		// check && update
		if (empty(exCycle1) || empty(exCycle2)) {
			alertErr("產生檔案作業-帳期起迄不可空白");
			return;
		}
		if (empty(exVdate)) {
			alertErr("產生檔案作業-扣帳日期不可空白");
			return;
		} else {
			lsSql = "select count(*) ct "
					+ "from ptr_holiday "
					+ "where holiday = :ex_vdate";
			setString("ex_vdate", exVdate);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				if (!sqlStr("ct").equals("0")) {
					alertErr("產生檔案作業-扣帳日期不可為例假日");
					return;
				}
			}
		}
		
		//business_date
		lsSql = "select business_date "
				+ " from ptr_businday "
				+ " fetch first 1 row only";
		sqlSelect(lsSql);
		businessDate = sqlStr("business_date");

		//seq_no
		lsSql = " select max(seq_no) seq_no  "
				+ "from ptr_media_cntl "
				+ "where ( media_name = :media_name ) and "
				+ "      ( business_date = :ls_busdate ) ";
		setString("media_name", exMdaname);
		setString("ls_busdate", businessDate);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			lsSeqNo = Integer.parseInt(sqlStr("seq_no")) + 1;
		}
		
		// ===============詢問訊息,目前無法處理
		// ls_sql = " SELECT count(*) ct "
		// + "FROM ptr_media_cntl "
		// + "WHERE 1=1 ";
		// ls_sql += sql_col(ex_mdaname, "media_name");
		// ls_sql += sql_col(ex_actmm, "acct_month");
		// ls_sql += sql_strend(ex_cycle1, ex_cycle2, "s_stmt_cycle");
		// System.out.println("ls_sql : " + ls_sql);
		// sqlSelect(ls_sql);
		// if (sqlRowNum > 0) {
		// javax.swing.JOptionPane.getRootFrame().setAlwaysOnTop(true);
		// if (JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
		// "帳務月份之帳期已產生過, 是否再重新產生", "訊息", JOptionPane.YES_NO_OPTION) != 0) {
		// return;
		// }
		// call_batch();
		// } else {
		// call_batch();
		// }
		// ============================
		
		//set trans_time
		String dateStr = "";
		Date currDate = new Date();
		SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHH24mmssSSS");
		dateStr = form1.format(currDate);
		exProcTime = strMid(dateStr,8,6);
		
		// insert ptr_media_cntl
		isSql = "insert into ptr_media_cntl ( "
				+ "media_name, "
				+ "business_date, "
				+ "seq_no, "
				+ "external_name, "
				+ "program_name, "
				+ "proc_date, "
				+ "proc_time, "
				+ "out_file_flag, "
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
				+ ":proc_date, "
				+ ":proc_time, "
				+ ":out_file_flag, "
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
		setString("business_date", businessDate);
		setString("seq_no", numToStr(lsSeqNo, "###"));		
		setString("external_name", exExtfile);
		setString("program_name", wp.itemStr("ex_pgname"));
		setString("proc_date", getSysDate());
		setString("proc_time", exProcTime);
		setString("out_file_flag", "N");
		setString("acct_bank", exBankid);
		setString("acct_month", exActmm);
		setString("s_stmt_cycle", exCycle1);
		setString("e_stmt_cycle", exCycle2);
		setString("value_date", wp.itemStr("ex_vdate"));
		setString("mod_user", wp.loginUser);
		setString("mod_pgm", "Actp0080");
		sqlExec(isSql);
		if (sqlRowNum <= 0) {
			alertErr("新增自動扣繳資料失敗 !");
			return;
		}
		sqlCommit(1);
		alertMsg("資料新增成功!!");
		queryFunc();
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

	void fileWriter() throws Exception {		
		String exMdaname = wp.itemStr("ex_mdaname");
		String fileName = wp.itemStr("ex_extfile");
	//String ex_frompath=wp.itemStr("ex_frompath");
		String msg = "";

		String[] aaDnloadAvailable = wp.itemBuff("dnload_available");
		String[] aaBusinessDate = wp.itemBuff("business_date");
		String[] aaSeqNo = wp.itemBuff("seq_no");
		wp.listCount[0] = aaBusinessDate.length;
		for (int ii = 0; ii < wp.listCount[0]; ii++) {
			if (!aaDnloadAvailable[ii].equals("Y")) {
				continue;
			}
  		try {
  			//ftp
  			TarokoFTP ftp = new TarokoFTP();	
  			//ftp.set_remotePath(ex_frompath);					//set_remotePath  完整路徑
  			ftp.setRemotePath2("media/act");					//set_remotePath2 : media...以後路徑
  			ftp.fileName = fileName;					
  			ftp.localPath = TarokoParm.getInstance().getWorkDir();
  			ftp.ftpMode = "BIN";
  			if (ftp.getFile(wp) != 0) {
  				alertErr("下載檔案失敗: ", ftp.fileName + "; err=" + ftp.getMesg());
  			}
  			else {
  			//檔案下載，一次只會有一個檔案下載
  				wp.setDownload(ftp.fileName);
  			//update ptr_media_cntl
    			String usSql = "update ptr_media_cntl "
    					+ "set OUT_MEDIA_FLAG  = 'Y', "
			   	  //+ "proc_date = to_char(sysdate,'yyyymmdd'), "
			   	  //+ "proc_time = to_char(sysdate,'hh24miss'), "
			   	    + "trans_date = to_char(sysdate,'yyyymmdd'), "
			   	    + "trans_time = to_char(sysdate,'hh24miss'), "
      			  + "mod_pgm =:mod_pgm, "
			        + "mod_user =:mod_user, "
				      + "mod_time = sysdate, "
				      + "mod_seqno =nvl(mod_seqno,0)+1 "
    					+ "Where media_name = :ps_mdaname "
    					+ "and business_date = :ps_business_date "
    					+ "and seq_no = :pi_seq_no ";
    			setString("mod_pgm", wp.modPgm());
    			setString("mod_user", wp.loginUser);
    			setString("ps_mdaname", exMdaname);
    			setString("ps_business_date", aaBusinessDate[ii]);
    			setInt("pi_seq_no", Integer.parseInt(aaSeqNo[ii]));
					//ddd("ii="+ii);
					//ddd("aa_business_date[ii]="+aa_business_date[ii]);
					//ddd("aa_seq_no[ii]="+aa_seq_no[ii]);
					//ddd("us_sql="+us_sql);
    			sqlExec(usSql);
					//ddd("sqlRowNum="+sqlRowNum);
    			if (sqlRowNum <= 0) {
    				msg = "Update ptr_media_cntl 1 Error!!";
    				sqlCommit(0);
    				continue;
    			} else {
    				msg = "Download OK and Update ptr_media_cntl successfully!!";
      		  sqlCommit(1);
    			}
  			}
  		//msg = ftp.getMesg();
  		}
  		catch (Exception ex) {
  			msg = ex.getMessage();
  		}
		}

		//以下執行於 MainControl 被 return 掉，不會執行TarokoParser.parseNameType(wp)、wp.outputControl()、TarokoParser.parseOutput(wp);
		//wp.colSet("proc_mesg", msg);
		
	}
	
	public void callProcess() throws Exception {
		String exPgname = wp.itemStr("ex_pgname");
		String sendData = "";
		String msg = "",pram="",parms="";
		String mediaName = itemKk("data_k1");
		String businessDate = itemKk("data_k2");
		String seqNo = itemKk("data_k3");
		String iaKey4 = itemKk("data_k4");	

		if (empty(exPgname)) {
			alertErr("請輸入 執行程式!");
			return;
		}
				
		if(iaKey4.equals("Y")){
			alertErr("處理程式不可重複執行!! ");
			return ;
		}
	//int rr = 0;
		 
		EcsCallbatch batch = new EcsCallbatch(wp);
		
		parms  = mediaName + " ";
		parms += businessDate + " ";
		parms += seqNo;
		
		pram = exPgname.substring(0, 3) + "." + exPgname + " " + parms;
	//rc = batch.call_Batch(pram,wp.loginUser);
		rc = batch.callBatch(pram);

		wp.colSet("proc_mesg", exPgname+batch.getMesg());
	  queryFunc();
	//return rr;
	}

//	public int call_batch() throws Exception {	
//		String ex_pgname = wp.itemStr("ex_pgname");
//		InetAddress sAddress= InetAddress.getByName(wp.request.getRemoteAddr());
//		String sendData = "";
//		String msg = "",pram="";
//		String media_name = item_kk("data_k1");
//		String business_date = item_kk("data_k2");
//		String seq_no = item_kk("data_k3");
//		String ia_key4 = item_kk("data_k4");	
//
//		if (empty(ex_pgname)) {
//			err_alert("請輸入 執行程式!");
//			return -1;
//		}
//				
//		if(ia_key4.equals("Y")){
//			alert_err("處理程式不可重複執行!! ");
//			return -1;
//		}
//		int rr = 0;
//		 
//    /***
//		String lsSql = " select wf_value,wf_value2 from PTR_sys_parm where wf_parm='SYSPARM' and wf_key = 'CALLBATCH' ";
//		sqlSelect(lsSql);
//      //ip and port
//		String host = sqlStr("wf_value");		
//		int port = (int) sql_num("wf_value2");
//    ***/
//
//		String host = "";		
//		int port = 0;
//		String lsSql = "select ref_ip, port_no "
//		             + "from ecs_ref_ip_addr where ref_ip_code = 'CALL-BATCH' ";
//		sqlSelect(lsSql);
//
//	  if(sqlRowNum>0){
//		  host = sqlStr("ref_ip");		
//		  port = (int) sql_num("port_no");
//	  }
//
//    //seqno
//		String ls_mod_seqno = " select ecs_modseq.nextval AS MOD_SEQNO from dual ";
//		sqlSelect(ls_mod_seqno);
//
//		String MOD_SEQNO = sqlStr("MOD_SEQNO");
//		
//		//參數
//		pram = media_name +" ";
//		pram += business_date +" ";
//		pram += seq_no +" ";
//
//		sendData=ex_pgname.substring(0,3)+"."+ex_pgname+" "
//	            +pram
//	            +String.format("%020d", Long.valueOf(MOD_SEQNO));
//		
//      //insert ptr_callbatch
//		String ls_ins = "insert into ptr_callbatch ( "
//				+ " batch_seqno,  "
//				+ "program_code, "
//				+ "start_date,"
//				+ "user_id,"
//				+ "workstation_name,"
//				+ "client_program,"
//				+ "parameter_data"
//				+ ")values(  "
//		        +" :batch_seqno,"
//				+" :program_code,"
//				+" :start_date,"
//				+" :user_id,"
//				+" :workstation_name,"
//				+" :client_program , "
//		        +" :parameter_data ) ";
//		setString("batch_seqno",String.format("%020d", Long.valueOf(MOD_SEQNO)));
//		setString("program_code", ex_pgname);
//		setString("start_date", get_sysDate());
//		setString("user_id", wp.loginUser);
//		setString("workstation_name", sAddress.getHostName());
//		setString("client_program", wp.itemStr("MOD_PGM"));
//		setString("parameter_data", sendData);
//		sqlExec(ls_ins);
//		if (sqlRowNum <= 0) {
//			msg =" ERROR:insert ptr_callbatch   \n";
//			return -1;
//		}else{
//			sql_commit(1);
//		}
//		Socket socket = null;
//
//	
//		try {
//			socket = new Socket(host, port);
//			DataInputStream input = null;
//			DataOutputStream output = null;
//
//			msg +="Starting...  \n";
//
//			try {
//				while (true) {	
//				
//					 output = new DataOutputStream( socket.getOutputStream() );
//					 msg+="Send data : [" + sendData + "] \n";
//					 output.write(sendData.getBytes());
//					   //output.writeUTF(sendData);
//					 output.flush();
//					
//					 input = new DataInputStream( socket.getInputStream() );
//					 int     inputLen = 0;
//					 byte[]  inData  = new byte[2048];
//					 
//					 inputLen  = input.read(inData, 0, inData.length);
//					 if(inputLen > 0){
//						 msg+="response data : [" + new String(inData, 0, inputLen) + "] \n";
//					 }else{
//						 msg+="無回傳資料   \n";
//					 }
//					 break;
//				}
//			} catch (Exception e) {
//				msg+="Exception : " + e.getMessage()+"  \n";
//			} finally {
//				if (input != null)
//					input.close();
//				if (output != null)
//					output.close();
//				 msg+="Terminated..\n";
//				 rr = 1;
//			}
//		} catch (IOException e) {
//			msg+="Exception2 : " + e.getMessage()+"  \n";
//			e.printStackTrace();
//		} finally {
//			if (socket != null)
//				socket.close();
//			// if(consoleInput != null ) consoleInput.close();
//			msg+="Socked Closed...  \n ";
//			//wp.colSet("proc_mesg", msg);
//		}
//
//		wp.colSet("proc_mesg", msg);
//		return rr;
//	}

}
