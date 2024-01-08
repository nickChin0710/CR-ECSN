package Rsk;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class RskP200 extends BaseBatch {
	private final String progname = "接收退票檔 TPSS.BOUNCE.SFILE.OUTPUT.L256 112/04/27 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	
	String fileName = "";	
	private int iiFileNum = 0;	
	
	String invoiceAcct = "";
	String invoiceType = "";
	String invoiceNo = "";
	double invoiceAmt = 0;
	String refundDate = "";
	String refundType = "";
	String remindBank = "";
	String reasonNo = "";
	String refundReasonNo = "";
	String organizationType = "";
	String idNo = "";
	String idPSeqno = "";
	String corpNo = "";
	String corpPSeqno = "";
	String birthday = "";
	String refundBank = "";
	String remindAcct = "";
	String outColNo = "";
	String notTodayFlag = "";
	String spare1 = "";
	String spare2 = "";
	String spare3 = "";
	String spare4 = "";
	String spare5 = "";
		
	public static void main(String[] args) {
		RskP200 proc = new RskP200();
		// proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP200 [business_date]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}

		if (empty(hBusiDate))
			hBusiDate = comc.getBusiDate();

		dateTime();
		
		fileName = "CRBOUNCE.DAT";
		checkOpen();
		processData();
		renameFile();		
		
		endProgram();

	}
	
	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
		String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName+"_"+hBusiDate);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");				
	}
	
	void processData() throws Exception {

		while (true) {
			String fileData = readTextFile(iiFileNum);
			if (endFile[iiFileNum].equals("Y")) {
				break;
			}
			if (empty(fileData))
				break;
			totalCnt++;
			initData();			
			splitData(fileData);
			if(idNo.isEmpty() == false)
				selectIdPSeqno();
			if(corpNo.isEmpty() == false)
				selectCorpPSeqno();
			insertRskReFund();
			sqlCommit(1);
		}
		closeInputText(iiFileNum);
	}
	
	int tiCrdIdNo = -1 ;
	void selectIdPSeqno() throws Exception {
		if (tiCrdIdNo <=0) {			
		    sqlCmd = " select id_p_seqno from crd_idno_seqno where id_no = ? " ;
		    tiCrdIdNo =ppStmtCrt("ti-S-crdIdno","");
		}
		
		setString(1,idNo);
		sqlSelect(tiCrdIdNo);
		
		if (sqlNrow > 0) {
			idPSeqno = colSs("id_p_seqno");
		}		
	}
	
	int tiCrdCorp = -1 ;
	void selectCorpPSeqno() throws Exception {
		if (tiCrdCorp <=0) {			
		    sqlCmd = " select corp_p_seqno from crd_corp where corp_no = ? " ;
		    tiCrdCorp =ppStmtCrt("ti-S-crdCorp","");
		}
		
		setString(1,corpNo);
		sqlSelect(tiCrdCorp);
		
		if (sqlNrow > 0) {
			corpPSeqno = colSs("corp_p_seqno");
		}		
	}
	
	void insertRskReFund() throws Exception {
		daoTable = "rsk_refund";
		setValue("invoice_acct",invoiceAcct);
		setValue("invoice_type",invoiceType);
		setValue("invoice_no",invoiceNo);
		setValueDouble("invoice_amt",invoiceAmt);
		setValue("refund_date",refundDate);
		setValue("refund_type",refundType);
		setValue("remind_bank",remindBank);
		setValue("reason_no",reasonNo);
		setValue("refund_reason_no",refundReasonNo);
		setValue("organization_type",organizationType);
		setValue("id_no",idNo);
		setValue("id_p_seqno",idPSeqno);
		setValue("corp_no",corpNo);
		setValue("corp_p_seqno",corpPSeqno);
		setValue("birthday",birthday);
		setValue("refund_bank",refundBank);
		setValue("remind_acct",remindAcct);
		setValue("out_col_no",outColNo);
		setValue("not_today_flag",notTodayFlag);
		setValue("spare_1",spare1);
		setValue("spare_2",spare2);
		setValue("spare_3",spare3);
		setValue("spare_4",spare4);
		setValue("spare_5",spare5);
		setValue("proc_flag","N");
		setValue("proc_date","");
		setValue("input_file",fileName);
		setValue("crt_user","ecs");
		setValue("crt_date",sysDate);
		setValue("crt_time",sysTime);
		setValue("mod_user","ecs");
		setValue("mod_time",sysDate + sysTime);
		setValue("mod_pgm","RskP200");
		setValueInt("mod_seqno",1);
		insertTable();
	}
	
	void checkOpen() throws Exception {
		String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);

		iiFileNum = openInputText(lsFile,"UTF-8");
		if (iiFileNum == -1) {
			this.showLogMessage("I", "", "無檔案可處理 !");
			okExit(0);	
		}
						
		return;
	}
	
	void splitData(String fileData) throws Exception {		
		byte[] bytes = fileData.getBytes("MS950");
		invoiceAcct = comc.subMS950String(bytes, 0, 9).trim();
		invoiceType = comc.subMS950String(bytes, 9, 2);
		invoiceNo = comc.subMS950String(bytes, 11, 7);
		invoiceAmt = commString.ss2Num(comc.subMS950String(bytes, 18, 15));
		refundDate = commDate.tw2adDate(comc.subMS950String(bytes, 33, 8));
//		refundDate = comc.subMS950String(bytes, 33, 8);
		refundType = comc.subMS950String(bytes, 41, 1);
		remindBank = comc.subMS950String(bytes, 42, 9);
		reasonNo = comc.subMS950String(bytes, 51, 8);
		refundReasonNo = comc.subMS950String(bytes, 59, 2);
		organizationType = comc.subMS950String(bytes, 61, 1);
		idNo = comc.subMS950String(bytes, 62, 10);
		corpNo = comc.subMS950String(bytes, 72, 8);
		birthday = commDate.tw2adDate(comc.subMS950String(bytes, 80, 8));
		refundBank = comc.subMS950String(bytes, 88, 9);
		remindAcct = comc.subMS950String(bytes, 97, 14);
		outColNo = comc.subMS950String(bytes, 111, 2);
		notTodayFlag = comc.subMS950String(bytes, 113, 1);
		spare1 = comc.subMS950String(bytes, 114, 6);	
		
	}
	
	void initData() {
		invoiceAcct = "";
		invoiceType = "";
		invoiceNo = "";
		invoiceAmt = 0;
		refundDate = "";
		refundType = "";
		remindBank = "";
		reasonNo = "";
		refundReasonNo = "";
		organizationType = "";
		idNo = "";
		idPSeqno = "";
		corpNo = "";
		corpPSeqno = "";
		birthday = "";
		refundBank = "";
		remindAcct = "";
		outColNo = "";
		notTodayFlag = "";
		spare1 = "";
		spare2 = "";
		spare3 = "";
		spare4 = "";
		spare5 = "";
	}
	
}
