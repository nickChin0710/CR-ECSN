package Inf;
/** 
 * V00.01	2020-1214	Ryan		initial
 * 109/12/30  V1.00.03  yanghan       修改了部分无意义的變量名稱          *
 * 112/03/13  V1.00.04  wilson      檔案格式調整                                         *
 * 112/03/18  V1.00.05  wilson      卡種改為正附卡註記                              *
 * 112/03/20  V1.00.06  wilson      卡種還原&控管碼改為正附卡註記         *
 * 112/04/26  V1.00.07  wilson      持卡人中文姓名取13碼                         *
 * 112/05/01  V1.00.08  wilson      暫停卡增加凍結                                      *
 * */

import com.CommCrd;

import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;

public class InfC001 extends BaseBatch {
	private final String progname = "產生送CRDB 01新增卡片資料檔程式   112/05/01 V1.00.08";

	CommCrd comc = new CommCrd();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;

	String isFileName = "";
	private int iiFileNum = -1;
	String allStr = "";
	String hChgDate = "";
	String hOpenDate = "";
	int commit = 1;

	// =****************************************************************************
	public static void main(String[] args) {
		InfC001 proc = new InfC001();
		proc.mainProcess(args);
		proc.systemExit();
	}

	// =============================================================================
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			errExit(1);
		}
		dbConnect();

		readSysdate();

		if (liArg == 0) {
			hChgDate = hOpenDate;
		}
		if (liArg == 1) {
			hChgDate = args[0];
		}
		// printf("傳送日期 =[%s]", crtDate);
		
		isFileName = "CRU23B1_TYPE_01_" + hChgDate + ".txt";

		writeText();

		checkOpen();
		this.writeTextFile(iiFileNum, allStr);
		this.closeCursor();

		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		renameFile(isFileName);

		sqlCommit(commit);
		printf("==>程式執行結束, 處理筆數=[%s]==============", "" + totalCnt);
		endProgram();
	}

	/* = ************************************************************************/
	public void checkOpen() throws Exception {

		String lsTemp = "";
		lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
		printf("Open File =[%s]", lsTemp);
		iiFileNum = this.openOutputText(lsTemp);
		if (iiFileNum < 0) {
			printf("[%s]在程式執行目錄下沒有權限讀寫資料", lsTemp);
			errExit(1);
		}
	}

	// =============================================================================
	public void readSysdate() throws Exception {

		sqlCmd = " select to_char(sysdate-1,'yyyymmdd') as sysdate1 ";
		sqlCmd += "from dual";
		sqlSelect();
		hOpenDate = colSs("sysdate1");

	}

	
	
	// =============================================================================
	void writeText() throws Exception {
		sqlCmd = " select 'N' as is_vd,a.card_no,a.major_id_p_seqno,a.group_code,a.id_p_seqno ";
		sqlCmd += " ,a.new_end_date,a.current_code,a.reg_bank_no,a.combo_acct_no as h_acct_no ";
		sqlCmd += " ,a.acno_p_seqno as h_p_seqno,a.indiv_crd_lmt ";
		sqlCmd += " ,a.issue_date,a.activate_flag,a.eng_name,a.oppost_reason ";
		sqlCmd += " ,decode(a.sup_flag,'1','N','P') as tmp_sup_flag,a.card_ref_num,a.son_card_flag ";
		sqlCmd += " ,UF_IDNO_ID(a.id_p_seqno) as h_id_no ";
		sqlCmd += " ,UF_IDNO_NAME(a.id_p_seqno) as h_chi_name ";
		sqlCmd += " ,(CASE WHEN a.major_id_p_seqno != a.id_p_seqno ";
		sqlCmd += " THEN UF_IDNO_ID(a.major_id_p_seqno) ";
		sqlCmd += " ELSE UF_IDNO_ID(a.id_p_seqno) END) as majoridno ";
		sqlCmd += " ,nvl(b.line_of_credit_amt,0) as line_of_credit_amt,nvl(b.line_of_credit_amt_cash,0) as line_of_credit_amt_cash ";
		sqlCmd += " ,b.payment_rate1 as acno_payment_rate1,c.spec_flag,a.card_type ";
		sqlCmd += " ,d.block_reason1||d.block_reason2||d.block_reason3||d.block_reason4||d.block_reason5 as h_block_reason ";
		sqlCmd += " ,a.acct_type ";
		sqlCmd += " ,a.corp_no ";
		sqlCmd += " from crd_card a left join act_acno b on a.acno_p_seqno = b.acno_p_seqno ";
		sqlCmd += " left join cca_card_base c on a.card_no = c.card_no ";
		sqlCmd += " left join cca_card_acct d on a.acno_p_seqno = d.acno_p_seqno ";
		sqlCmd += " where a.crt_date = ? ";
		sqlCmd += " union ";
		sqlCmd += " select 'Y' as is_vd,a.card_no,a.major_id_p_seqno,a.group_code,a.id_p_seqno ";
		sqlCmd += " ,a.new_end_date,a.current_code,a.reg_bank_no,a.acct_no as h_acct_no ";
		sqlCmd += " ,a.p_seqno as h_p_seqno,0 as indiv_crd_lmt ";
		sqlCmd += " ,a.issue_date,'' as activate_flag,a.eng_name,a.oppost_reason ";
		sqlCmd += " ,decode(a.sup_flag,'1','N','P') as tmp_sup_flag,a.card_ref_num,'' as son_card_flag ";
		sqlCmd += " ,UF_VD_IDNO_ID(a.id_p_seqno) as h_id_no ";
		sqlCmd += " ,UF_VD_IDNO_NAME(a.id_p_seqno) as h_chi_name ";
		sqlCmd += " ,'' as majoridno ";
		sqlCmd += " ,0 as line_of_credit_amt,0 as line_of_credit_amt_cash ";
		sqlCmd += " ,'' as acno_payment_rate1,c.spec_flag,a.card_type ";
		sqlCmd += " ,d.block_reason1||d.block_reason2||d.block_reason3||d.block_reason4||d.block_reason5 as h_block_reason ";
		sqlCmd += " ,a.acct_type ";
		sqlCmd += " ,'' as corp_no ";
		sqlCmd += " from dbc_card a left join cca_card_base c on a.card_no = c.card_no ";
		sqlCmd += " left join cca_card_acct d on a.p_seqno = d.acno_p_seqno ";
		sqlCmd += " where a.crt_date = ? ";

		setString(1,hChgDate);
		setString(2,hChgDate);
		this.openCursor();

		while (fetchTable()) {

			allStr += "01";
			allStr += commString.fixlenNum(colSs("card_no"), 16);//卡號
			if(colSs("is_vd").equals("Y")){
				allStr += commString.rpad(colSs("h_id_no"), 11);//主卡身份證
			}else{
				if(!colSs("acct_type").equals("01")) {
					allStr += commString.rpad(colSs("corp_no"), 11);//主卡身份證
				}
				else {
					allStr += commString.rpad(colSs("majoridno"), 11);//主卡身份證
				}				
			}
			allStr += commString.rpad(colSs("group_code"), 4);//團體代號
			allStr += commString.rpad(colSs("h_id_no"), 10);//持卡人身份證
			allStr += commString.fixlenNum(commString.mid(colSs("new_end_date"),2,4), 4);//到期日
			
			if(colSs("current_code").equals("1")||colSs("current_code").equals("2")||colSs("current_code").equals("3")||
				colSs("current_code").equals("4")||colSs("current_code").equals("5")){
				allStr += commString.rpad(colSs("current_code"), 1);//卡片狀況
			}
			else if(colSs("spec_flag").equals("Y")||!colSs("h_block_reason").equals("")) {
				allStr += commString.rpad("T", 1);//卡片狀況
			}
//			else if(!colSs("acno_payment_rate1").equals("")&&!colSs("acno_payment_rate1").equals("0A")&&
//					!colSs("acno_payment_rate1").equals("0B")&&!colSs("acno_payment_rate1").equals("0C")&&
//					!colSs("acno_payment_rate1").equals("0D")&&!colSs("acno_payment_rate1").equals("0E")) {
//				allStr += commString.rpad("P", 1);//卡片狀況
//			}
			else {
				allStr += commString.rpad("0", 1);//卡片狀況
			}
			
			allStr += commString.rpad(colSs("reg_bank_no"), 4);//發卡單位
			allStr += commString.rpad(colSs("h_acct_no"), 13);//存款帳號
			if(colSs("is_vd").equals("Y")){
				allStr += commString.space(9);//卡片額度
			}else{
				if(colSs("son_card_flag").equals("Y")){
					allStr += commString.fixlenNum(colSs("indiv_crd_lmt"), 9);//卡片額度
				}else{
					allStr += commString.fixlenNum(colSs("line_of_credit_amt"), 9);//卡片額度
				}
			}
			if(colSs("is_vd").equals("Y")){
				allStr += commString.space(2);//預借現金成數
			}else{
				if(colInt("line_of_credit_amt")==0){
					allStr += commString.space(2);//預借現金成數
				}else{
					double amt = Math.round((this.colNum("line_of_credit_amt_cash")/colNum("line_of_credit_amt"))*100);
					allStr += commString.rpad((amt>0&&amt<100)?commString.left(String.format("%s", (int)amt),2):"",2);//預借現金成數
				}		
			}
			if(colSs("issue_date").length()==8){
				allStr += commString.fixlenNum(String.format("%s",colInt("issue_date")-19110000), 7);//開戶日
			}else{
				allStr += commString.space(7);//開戶日
			}
			if(colSs("is_vd").equals("Y")){
				allStr += commString.space(1);//開卡狀態
			}else{
				allStr += commString.rpad(commString.decode(colSs("activate_flag"), ",1,2", ",0,1"), 1);//開卡狀態
			}
			
			allStr += commString.rpad(colSs("reg_bank_no"), 4);//原申辦分行
			allStr += commString.rpad(colSs("tmp_sup_flag"), 1);//卡片正附卡註記
			allStr += commString.rpad(colSs("oppost_reason"), 2);//掛失原因碼
			allStr += commString.rpad(colSs("card_type"), 2);//卡種
			allStr += commString.rpad(colSs("card_ref_num"), 2);//卡片序號
			allStr += comc.fixLeft(commString.left(colSs("eng_name"),26), 26);//持卡人英文戶名
			allStr += comc.fixLeft(commString.left(colSs("h_chi_name"), 6), 12);//持卡人中文戶名
			allStr += commString.space(15);
			allStr += "\r\n";
			totalCnt++;
		
		}
	}

	/***********************************************************************/
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr
				.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crdb/", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + isFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(isFileName);
		}
	}

	/****************************************************************************/
	public int insertEcsNotifyLog(String fileName) throws Exception {
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("unit_code", comr.getObjectOwner("3", javaProgram));
		setValue("obj_type", "3");
		setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_name", "媒體檔名:" + fileName);
		setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_desc2", "");
		setValue("trans_seqno", commFTP.hEflgTransSeqno);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "ecs_notify_log";

		insertTable();

		return (0);
	}

	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/crdb/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/crdb/backup/" + removeFileName;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

}
