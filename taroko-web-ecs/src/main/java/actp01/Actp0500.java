/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-02-06  V1.00.00  ryan       program initial                            *
* 109/03/10  V1.00.01  phopho     change file_name.length: 20 -> 50          *
* 111-10-25  v1.00.02  Yang Bo    Sync code from mega                        *
*****************************************************************************/
package actp01;
import ecsfunc.EcsCallbatch;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFTP;
import taroko.com.TarokoParm;


public class Actp0500 extends BaseProc {

	int rr = -1;
	int ilOk = 0;
	int ilErr = 0;
	String msg="";

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
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
			/* FTP */
			strAction = "UPLOAD";
			procFunc();
		} 

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		wp.colSet("ex_pgname", "ActA500");
		wp.colSet("ex_date1", wp.sysDate.substring(0,6));
	}

	@Override
	public void dddwSelect() {

	}

	@Override
	public void queryFunc() throws Exception {
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		
	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		String ss[] = wp.itemStr("db_list").split("\n",-1);
		String lsKind="",lsBankNo="",lsTmpKind="",lsTmpBankNo="";
		long liAmt=0,liAmtTot=0,sumCnt=0,sumAmt=0,liCntTot=0;
		long tolDbCnt =0,tolDbAmt=0,tolDbCntAll=0,tolDbAmtAll=0;
		int liCntBank =0;
		//int li_cnt_bank=0;
		for(int L = 0; L <ss.length; L++){
			wp.colSet(L,"SER_NUM", String.format("%02d",L+1));
			
			lsKind = strMid(ss[L],0,1);
			lsBankNo = strMid(ss[L],1,8);
			
			liAmt = (parseLong(strMid(ss[L],39,13)))/100;
			if(!lsKind.equals("1")&&!lsKind.equals("2")&&!lsKind.equals("3")){
				continue;
			}
			if(lsKind.equals("2")){
				liAmtTot =  liAmtTot+ liAmt;
				liCntTot++;
			}
			if(L==0){
				liCntBank =0;
				lsTmpKind = lsKind;
				lsTmpBankNo = lsBankNo;
			}
			if(lsKind.equals("3")){
				wp.colSet(liCntBank,"db_amt_all", parseLong(strMid(ss[L],29,15))/100);
				wp.colSet(liCntBank,"db_cnt_all", parseLong(strMid(ss[L],44,10)));
				tolDbAmtAll += (parseLong(strMid(ss[L],29,15))/100);
				tolDbCntAll += parseLong(strMid(ss[L],44,10));
				sumCnt = parseLong(strMid(ss[L],44,10));
				sumAmt = (parseLong(strMid(ss[L],29,15)))/100;
			}
			if(L > 0 && lsTmpKind.equals(lsKind)){
				wp.colSet(liCntBank,"db_bank_no", lsTmpBankNo);
				wp.colSet(liCntBank,"db_cnt", liCntTot);
				wp.colSet(liCntBank,"db_amt", liAmtTot);
				wp.colSet(liCntBank,"db_eq","<>");
				tolDbCnt += liCntTot;
				tolDbAmt += liAmtTot;
				if(sumCnt==liCntTot && sumAmt==liAmtTot){
					wp.colSet(liCntBank,"db_eq","==");
				}
				liCntBank++;
				lsTmpKind = lsKind;
				lsTmpBankNo = lsBankNo;
				liCntTot = 0;
				liAmtTot = 0;
			}
		}
		wp.listCount[0]=liCntBank;
		tolDbCnt += liCntTot;
		tolDbAmt += liAmtTot;
		wp.colSet("tol_db_cnt",tolDbCnt);
		wp.colSet("tol_db_amt",tolDbAmt);
		wp.colSet("tol_db_cnt_all",tolDbCntAll);
		wp.colSet("tol_db_amt_all",tolDbAmtAll);
	}
	
	public void procFunc() throws Exception {
		if (itemIsempty("zz_file_name")) {
			alertErr("上傳檔名: 不可空白");
			return;
		}
		if (wp.itemStr("zz_file_name").length()>50) {  //phopho mod 2020.3.10 change file_name.length: 20 -> 50
			alertErr("上傳檔名: 長度不可超過50碼");
			return;
		}
		wp.colSet("ex_fromfile",  wp.itemStr("zz_file_name"));
		fCallFtp();
	}

	void fCallFtp(){
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
			if (ftp.putFile(wp) != 0) {
				alertErr("上傳檔案失敗: ", ftp.fileName + "; err=" + ftp.getMesg());
				return;
			}
			msg = ftp.getMesg();
		} catch (Exception ex) {
			msg = ex.getMessage();
			alertErr("上傳檔案失敗: ,"+msg);
			return;
		}
		alertMsg("上傳檔案成功"+msg);
		return;
	}
	@Override
	public void dataProcess() throws Exception {
		if (!checkApprove(wp.itemStr("zz_apr_user"), wp.itemStr("zz_apr_passwd"))) {
			return;
		}
		//call_batch
		fCallBatch();
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	void fCallBatch() throws Exception{
		EcsCallbatch batch = new EcsCallbatch(wp);
		String pram ="";
		// 參數
		
		pram = wp.itemStr("ex_bpgm");
		rc = batch.callBatch(pram);
		if (rc != 1) {
			wp.colSet("proc_mesg","啟動批次程式失敗, "+pram);
			return ;
		}
		
		wp.colSet("proc_mesg"," 啟動批次程式成功, "+pram+" 處理序號: "+batch.batchSeqno() );
	}
	
	void fileDataImp1(){
		
	}

	String subString(String value, int a, int b){
		String value2 ="";
		try {
			value2 = value.substring(a,b);
		}catch (Exception e) {
			value2 = "0";
		}
		return value2;
	}
	
	long parseLong(String val){
		long val2=0;
		try {
			val2 = Long.parseLong(val);
		}catch (Exception e) {
			val2 = 0;
		}
		return val2;
	}
}
