package Rsk;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class RskP201 extends BaseBatch {

	private final String progname = "退票資料寫入不良紀錄通報 112/04/28 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	
	String idNo = "";
	String idPSeqno = "";	
	String corpNo = "";
	String corpPSeqno = "";
	String inputFile = "";
	String chiName = "";
	String hasSupFlag = "";
	String majorIdPSeqno = "";
	String upProcReason = "";
	String remindBankCode = "";
	String remindBankName = "";
	String refundUnProcReason = "";
	String refundRowId = "";
	final String blockReason4 = "38";
	final String bankNo = "006";
	final String bankName = "合作金庫商業銀行";
	
	public static void main(String[] args) {
		RskP201 proc = new RskP201();
		// proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP201 [business_date]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}

		if (empty(hBusiDate))
			hBusiDate = comc.getBusiDate();

		dateTime();								
		procData();
		endProgram();		
	}
	
	void procData() throws Exception {
		
		sqlCmd = " select id_no , id_p_seqno , corp_no , corp_p_seqno , input_file , substr(remind_bank ,3,3) as remind_bank , rowid as rowid from rsk_refund where 1=1 "
			   + " and refund_reason_no in ('01','02','03','04','05','06','07','51','52','53','56') and proc_flag <> 'Y' ";
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			idNo = colSs("id_no");
			idPSeqno = colSs("id_p_seqno");			
			corpNo = colSs("corp_no");
			corpPSeqno = colSs("corp_p_seqno");
			inputFile = colSs("input_file");
			remindBankCode = colSs("remind_bank");
			refundRowId = colSs("rowid");
			
			//--非本行卡友
			if(idPSeqno.isEmpty() && corpPSeqno.isEmpty()) {
				upProcReason = "非本行卡友";
				if(checkRskBadAnnou())
					insertRskBadAnnou(1);
				updateRskRefund();
				continue;
			}
			
			if(empty(idPSeqno) == false) {
				//--取中文名字
				selectChiName();
				
				//--取正卡流水號、是否有附卡
				selectCrdCard();
			}			
						
			//--取銀行中文名
			selectRemindBankName();
			
			if(checkRskBadAnnou())
				insertRskBadAnnou(0);
			updateRskRefund();			
			
			sqlCommit(1);
		}
		closeCursor();
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
		
		refundUnProcReason = "當日已有其他同來源退票資料";
		
		return false;		
	}
	
	int tiCrdIdno = -1;
	void selectChiName() throws Exception {		
		if (tiCrdIdno <=0) {			
		    sqlCmd = " select chi_name from crd_idno where id_p_seqno = ? ";
		    tiCrdIdno =ppStmtCrt("ti-S-crdIdno","");
		}
		
		setString(1,idPSeqno);
		
		sqlSelect(tiCrdIdno);
		if (sqlNrow <=0) {
			return ;
		}
		chiName = colSs("chi_name");		
	}
	
	int tiCrdCard = -1;
	void selectCrdCard() throws Exception {
		if(tiCrdCard <=0) {
			sqlCmd = " select distinct major_id_p_seqno from crd_card where id_p_seqno = ? and sup_flag ='1' and current_code ='0' ";
			tiCrdCard =ppStmtCrt("ti-S-crdCard","");			
		}
		
		setString(1,idPSeqno);
		sqlSelect(tiCrdCard);
		if (sqlNrow <=0) {
			return ;
		}
		
		hasSupFlag = "Y";
		majorIdPSeqno = colSs("major_id_p_seqno");		
	}
	
	int tiBank = -1;
	void selectRemindBankName() throws Exception {
		if(tiBank <=0) {
			sqlCmd = "select bc_abname from ptr_bankcode where bc_bankcode = ? ";
			tiBank =ppStmtCrt("ti-S-ptrBankcode","");
		}
		
		setString(1,remindBankCode);
		sqlSelect(tiBank);
		if (sqlNrow <=0) {
			return ;
		}
		
		remindBankName = colSs("bc_abname");
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
		setValue("imp_file",inputFile);
		setValue("has_sup_flag",hasSupFlag);
		setValue("annou_type","6");
		setValue("bank_no",remindBankCode);
		setValue("bank_name",remindBankName);
		setValue("block_reason4",blockReason4);		
		setValue("proc_flag",procFlag);
		setValue("proc_date",procDate);
		setValue("unproc_reason",upProcReason);
		setValue("crt_user","ecs");
		setValue("mod_user","ecs");
		setValue("mod_time",sysDate + sysTime);
		setValue("mod_pgm","RskP201");
		setValueDouble("mod_seqno",1);		
		insertTable();
		
	}
	
	void updateRskRefund() throws Exception {
		daoTable = "rsk_refund";
		updateSQL = "proc_flag = 'Y' , proc_date = to_char(sysdate,'yyyymmdd') , unproc_reason = ? ";
		whereStr = "where 1=1 and rowid = ? ";
		setString(1,refundUnProcReason);
		setRowId(2, refundRowId);
		updateTable();		
	}
	
	void initData() {
		idNo = "";
		idPSeqno = "";	
		corpNo = "";
		corpPSeqno = "";
		inputFile = "";
		chiName = "";
		hasSupFlag = "";
		majorIdPSeqno = "";
		upProcReason = "";
		remindBankCode = "";
		remindBankName = "";
		refundUnProcReason = "";
		refundRowId = "";
	}
	
}
