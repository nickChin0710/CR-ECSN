package Rsk;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class RskP203 extends BaseBatch {
	private final String progname = "接收票交拒往檔案寫入風管不良紀錄檔 112/06/07 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();		
	
	String fileName = "";
	private int iiFileNum = 0;
	String fileIdCorp = "";
	String idNo = "";
	String corpNo = "";
	String idPSeqno = "";
	String majorIdPSeqno = "";
	String corpPSeqno = "";
	String chiName = "";
	final String oppostReason = "F1";
	final String bankNo = "006";
	final String bankName = "合作金庫商業銀行";
	String unProcReason = "";
	String hasSupFlag = "";
	
	public static void main(String[] args) {
		RskP203 proc = new RskP203();
		// proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP203 [business_date]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}

		if (empty(hBusiDate))
			hBusiDate = comc.getBusiDate();

		dateTime();
		
		fileName = "CRW00CK.DAT";
		checkOpen();
		processData();
//		renameFile();		
		
		endProgram();
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
			selectIdPSeqno();
			if(idPSeqno.isEmpty() == false) {
				//--個人戶
				//--檢查是否有附卡		
				selectCardSup();
			}	else	{
				//--公司戶
				fileIdCorp = fileIdCorp.substring(2);
				selectCorpPSeqno();
				if(corpPSeqno.isEmpty())
					continue ;
				//--公司戶一卡一戶沒有附卡
				hasSupFlag = "N";
			}
			
			if(idNo.isEmpty() && corpNo.isEmpty()) {
				//--代表非本行卡友
				unProcReason = "非本行卡友";
				insertRskBadAnnou(1);
				continue;
			}
			
			if(checkRskBadAnnou())
				insertRskBadAnnou(0);
			sqlCommit(1);
		}
		closeInputText(iiFileNum);
	}
	
	void insertRskBadAnnou(int i) throws Exception {
		String procDate = "" , procFlag = "";
		if(i ==1) {
			procDate = hBusiDate ;
			procFlag = "Y";
		}
		
		daoTable = "rsk_bad_annou";
		setValue("crt_date",hBusiDate);
		setValue("from_type","1");
		setValue("id_no",idNo);
		setValue("corp_no",corpNo);
		setValue("id_p_seqno",idPSeqno);
		setValue("corp_p_seqno",corpPSeqno);
		setValue("major_id_p_seqno",majorIdPSeqno);
		setValue("chi_name",chiName);
		setValue("imp_file",fileName);
		setValue("has_sup_flag",hasSupFlag);
		setValue("annou_type","2");
		setValue("bank_no",bankNo);
		setValue("bank_name",bankName);
		setValue("block_reason4",oppostReason);		
		setValue("proc_flag",procFlag);
		setValue("proc_date",procDate);
		setValue("unproc_reason",unProcReason);
		setValue("crt_user","ecs");
		setValue("mod_user","ecs");
		setValue("mod_time",sysDate + sysTime);
		setValue("mod_pgm","RskP203");
		setValueDouble("mod_seqno",1);		
		insertTable();
		
	}
	
	boolean checkRskBadAnnou() throws Exception {
		
		String sql1 = " select count(*) as db_cnt "
					+ " from rsk_bad_annou "
					+ " where crt_date = ? and from_type = ? and id_no = ? and corp_no = ? and major_id_p_seqno = ? ";
		
		sqlSelect(sql1,new Object[] {hBusiDate,"1",idNo,corpNo,majorIdPSeqno});
		
		if(sqlNrow <=0)
			return true;
		
		if(colNum("db_cnt") <=0)
			return true;
				
		return false;		
	}
	
	int tiCrdCard = -1;
	void selectCardSup() throws Exception {
		if(tiCrdCard <=0) {
			sqlCmd = " select distinct major_id_p_seqno from crd_card where id_p_seqno = ? and sup_flag ='1' and current_code ='0' ";
			tiCrdCard =ppStmtCrt("ti-S-crdCard","");			
		}
		
		setString(1,idPSeqno);
		sqlSelect(tiCrdCard);
		if (sqlNrow <=0) {
			hasSupFlag = "N";
			return ;
		}
		
		hasSupFlag = "Y";
		majorIdPSeqno = colSs("major_id_p_seqno");				
	}
	
	int tiCrdIdno = -1;
	void selectIdPSeqno() throws Exception {
		
		if(tiCrdIdno <=0) {
			sqlCmd = " select id_no , id_p_seqno , chi_name from crd_idno where id_no = ? ";
			tiCrdIdno =ppStmtCrt("ti-S-crdIdno","");			
		}
		
		setString(1,fileIdCorp);
		sqlSelect(tiCrdIdno);
		if (sqlNrow <=0) {
			return ;
		}
		
		idNo = colSs("id_no");
		idPSeqno = colSs("id_p_seqno");
		chiName = colSs("chi_name");
	}
	
	int tiCrdCorp = -1;	
	void selectCorpPSeqno() throws Exception {
		
		if(tiCrdCorp <=0) {
			sqlCmd = "select corp_no , corp_p_seqno , chi_name from crd_corp where corp_no = ?";
			tiCrdCorp =ppStmtCrt("ti-S-crdCorp","");			
		}
		
		setString(1,fileIdCorp);
		sqlSelect(tiCrdCorp);
		if (sqlNrow <=0) {
			return ;
		}
		
		corpNo = colSs("corp_no");
		corpPSeqno = colSs("corp_p_seqno");		
		chiName = colSs("chi_name");		
	}
	
	void checkOpen() throws Exception {
		String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);

		iiFileNum = openInputText(lsFile,"MS950");
		if (iiFileNum == -1) {
			this.showLogMessage("I", "", "無檔案可處理 !");
			okExit(0);	
		}
						
		return;
	}
	
	void initData() {
		fileIdCorp = "";
		idNo = "";
		corpNo = "";
		idPSeqno = "";
		corpPSeqno = "";
		majorIdPSeqno = "";
		hasSupFlag = "";
		unProcReason = "";
		chiName = "";
	}
	
	void splitData(String fileData) throws Exception {
		byte[] bytes = fileData.getBytes("MS950");
		fileIdCorp = comc.subMS950String(bytes, 0, 10).trim();
	}
	
}
