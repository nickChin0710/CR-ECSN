/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112-06-17  V1.00.00  Alex        initial
 *  2023-1120 V1.00.01  JH    busi_date isRun
 *****************************************************************************/
package Rsk;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class RskP180 extends BaseBatch {
	private final String progname = "CRM36A資料處理 2023-1120 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	
	String fileName = "";
	private int iiFileNum = 0;
	private final String interestRateItemCond = "PB|CK|MB";
	private final String accCond = "031|035|038|040|042|044|046|069";
	
	//--檔案資料
	String branch = "";
	String acc = "";
	String seqNo = "";
	String fileIdCorp = "";
	String loanDetail = "";
	String acctStatus = "";
	String interestRateItem = "";
	double overDraft = 0.0;
	String acctName = "";
	String corpNo = "";
	String idNo = "";
	String chargeId = "";
	String corpHasCard = "";
	String chargeHasCard = "";
	String idPSeqno = "";
	String corpPSeqno = "";
	
	public static void main(String[] args) {
		RskP180 proc = new RskP180();
		proc.mainProcess(args);
		proc.systemExit();
	}	
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP180 [business_date]");
			okExit(0);
		}
		
		dbConnect();

		String ls_runDate="";
		if (liArg == 1) {
			this.setBusiDate(args[0]);
			ls_runDate =hBusiDate;
		}
		else ls_runDate =sysDate;
      if (checkWorkDate(ls_runDate)) {
         printf("-- [%s]非營業日, 不執行", ls_runDate);
         okExit(0);
      }
		
		fileName = "LAP360."+hBusiDate;
		
		//--吃檔
		checkOpen();
		
		//--逐筆處理
		processData();
		
		dateTime();								
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
			
			//--檢核條件一:現欠大於 0 , 故剔除小於 0 或等於 0 的資料
			if(overDraft <=0)
				continue;
			
			//--檢核條件二:帳戶狀態等於 0 , 故剔除狀態不是 0 的資料
			if("0".equals(acctStatus) == false)
				continue;
			
			//--檢核條件三: (利率項目 = PB or CK or MB) 或 ACC in ('031', '035', '038', '040', '042', '044', 046', '069')
			if(commString.ssIn(interestRateItem, interestRateItemCond) == false && commString.ssIn(acc, accCond) == false)
				continue;
			
			checkIdCorp();	
			
			//--確認公司有無商務卡
			if(corpPSeqno.isEmpty() == false) {
				checkCorpCard();
				//--確認負責人有無信用卡
				if(chargeId.isEmpty() == false) {
					checkChargeCard();
				}	else	{
					chargeHasCard = "N";
				}
			}	else	{
				corpHasCard = "N";
			}
			
			if(corpNo.isEmpty())
				continue;
			
			insertRskCrm36A();			
		}
		closeInputText(iiFileNum);
	}
	
	void insertRskCrm36A() throws Exception {
		daoTable = "rsk_crm36a_data";
		setValue("data_date",hBusiDate);
		setValue("branch",branch);
		setValue("acc",acc);
		setValue("seq_no",seqNo);
		setValue("file_id_corp",fileIdCorp);
		setValue("loan_detail",loanDetail);
		setValue("acct_status",acctStatus);
		setValue("interest_rate_item",interestRateItem);
		setValueDouble("over_draft",overDraft);
		setValue("acct_name",acctName);
		setValue("corp_no",corpNo);
		setValue("corp_p_seqno",corpPSeqno);
		setValue("charge_id",chargeId);
		setValue("id_p_seqno",idPSeqno);
		setValue("corp_has_card",corpHasCard);
		setValue("charge_has_card",chargeHasCard);
		setValue("crt_user","ecs");
		setValue("crt_date",sysDate);
		setValue("crt_time",sysTime);
		setValue("mod_user","ecs");
		setValue("mod_time",sysDate + sysTime);
		setValue("mod_pgm","Rskp180");
		setValueInt("mod_seqno",1);
		insertTable();
	}
	
	void checkIdCorp() throws Exception {
		
		if(fileIdCorp.length() == 8) {
			selectCrdCorp(fileIdCorp);
			if(chargeId.isEmpty() == false)
				selectCrdIdNo(chargeId);
		}	else	{
			//--確認是否為公司負責人
			selectCrdCorpCharge(fileIdCorp);
			if(corpPSeqno.isEmpty() == false) {
				//--是公司負責人才處理若不是公司負責人則不處理
				selectCrdCorp(corpNo);
				if(chargeId.isEmpty() == false)
					selectCrdIdNo(chargeId);
			}	else	{
				selectCrdCorp(fileIdCorp);
				if(chargeId.isEmpty() == false)
					selectCrdIdNo(chargeId);
			}
		}
		
	}
	
	int tiCorpCard = -1;
	void checkCorpCard() throws Exception {
		if (tiCorpCard <=0) {			
			sqlCmd = " select count(*) as db_corp_card from crd_card where corp_p_seqno = ? ";
			tiCorpCard =ppStmtCrt("ti-S-crdCardCorp","");
		}
		
		setString(1,corpPSeqno);
		
		sqlSelect(tiCorpCard);
		if (sqlNrow <=0) {	
			corpHasCard = "N";
			return ;
		}
		
		if(colNum("db_corp_card") > 0)
			corpHasCard = "Y";
		else
			corpHasCard = "N";
		
	}
	
	int tiChargeCard = -1;
	void checkChargeCard() throws Exception {
		if (tiChargeCard <=0) {			
			sqlCmd = " select count(*) as db_charge_card from crd_card where id_p_seqno = ? and acct_type ='01' ";
			tiChargeCard =ppStmtCrt("ti-S-crdCardId","");
		}
		
		setString(1,idPSeqno);
		
		sqlSelect(tiChargeCard);
		if (sqlNrow <=0) {	
			chargeHasCard = "N";
			return ;
		}
		
		if(colNum("db_charge_card") > 0)
			chargeHasCard = "Y";
		else
			chargeHasCard = "N";		
	}
	
	int tiCorp = -1;
	void selectCrdCorp(String aCorp) throws Exception {
		if (tiCorp <=0) {			
			sqlCmd = " select corp_p_seqno , charge_id from crd_corp where corp_no = ? ";
		    tiCorp =ppStmtCrt("ti-S-crdCorp","");
		}
		
		setString(1,aCorp);
		
		sqlSelect(tiCorp);
		if (sqlNrow <=0) {			
			return ;
		}
		
		corpNo = aCorp;
		corpPSeqno = colSs("corp_p_seqno");
		chargeId = colSs("charge_id");		
	}
	
	int tiCharge = -1;
	void selectCrdCorpCharge(String aChargeId) throws Exception {
		if (tiCharge <=0) {			
			sqlCmd = " select corp_p_seqno , corp_no from crd_corp where charge_id = ? ";
			tiCharge =ppStmtCrt("ti-S-crdCorpCharge","");
		}
		
		setString(1,aChargeId);
		
		sqlSelect(tiCharge);
		if (sqlNrow <=0) {			
			return ;
		}
		
		corpNo = colSs("corp_no");;
		corpPSeqno = colSs("corp_p_seqno");			
	}
	
	int tiId = -1;
	void selectCrdIdNo(String aId) throws Exception {
		if (tiId <=0) {			
			sqlCmd = " select id_p_seqno from crd_idno where id_no = ? ";
			tiId =ppStmtCrt("ti-S-crdIdno","");
		}
		
		setString(1,aId);
		
		sqlSelect(tiId);
		if (sqlNrow <=0) {			
			return ;
		}
		
		idNo = aId ;
		idPSeqno = colSs("id_p_seqno");				
	}
	
	void checkOpen() throws Exception {		
		String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);		
		
		iiFileNum = openInputText(lsFile,"MS950");
		if (iiFileNum < 0) {
//			showLogMessage("I", "", "LAP360  吃檔失敗 !");
//			errExit(1);
			printf("無檔案可處理 !, file=[%s]", fileName);
			okExit(0);
		}		
		
		return;
	}
	
	void splitData(String fileData) throws Exception {		
		byte[] bytes = fileData.getBytes("MS950");
		branch = comc.subMS950String(bytes, 0, 4).trim();
		acc = comc.subMS950String(bytes, 4, 3).trim();		
		seqNo = comc.subMS950String(bytes, 7, 6).trim();
		fileIdCorp = comc.subMS950String(bytes, 13, 10).trim();
		loanDetail = comc.subMS950String(bytes, 23, 6).trim();
		acctStatus = comc.subMS950String(bytes, 29, 1).trim();
		interestRateItem = comc.subMS950String(bytes, 30, 2).trim();
		overDraft = commString.ss2Num(comc.subMS950String(bytes, 32, 13).trim()) / 100;		
		acctName = comc.subMS950String(bytes, 45, 42).trim();
	}
	
	void initData() {
		branch = "";
		acc = "";
		seqNo = "";
		fileIdCorp = "";
		loanDetail = "";
		acctStatus = "";
		interestRateItem = "";
		overDraft = 0.0;
		acctName = "";
		corpNo = "";
		idNo = "";		
		chargeId = "";
		idPSeqno = "";
		corpPSeqno = "";
	}
	
}
