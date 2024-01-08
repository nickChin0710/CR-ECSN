/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112-06-19  V1.00.00  Alex        initial
 *  2023-1120 V1.00.01  JH    bisu-date isRun
 *****************************************************************************/
package Rsk;

import java.math.BigDecimal;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class RskP181 extends BaseBatch {
	private final String progname = "CRM36A資料統計 2023-1120 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	
	String branch = "";
	String branchName = "";
	int corpHasCardCnt = 0;
	int chargeHasCardCnt = 0;
	int creditCorpCnt = 0;
	int corpCnt = 0;
	double corpCardRatio = 0.0;
	double chargeCardRatio = 0.0;
	double creditCorpRatio = 0.0;
	
	public static void main(String[] args) {
		RskP181 proc = new RskP181();
		proc.mainProcess(args);
		proc.systemExit();
	}	
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP181 [business_date]");
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

		//--清除資料
		deleteCrm36aRpt();
		//--處理資料
		processData();
		
		dateTime();								
		endProgram();
		
	}
	
	void deleteCrm36aRpt() throws Exception {
		daoTable = "rsk_crm36a_rpt";
		whereStr = "where 1=1 and data_date = ? ";
		setString(1,hBusiDate);
		
		deleteTable();
	}
	
	void processData() throws Exception {
		
		sqlCmd = " select distinct branch "			   
			   + " from rsk_crm36a_data where data_date = ? "
			   ;
		
		setString(1,hBusiDate);
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			branch = colSs("branch");			
			
			//--取得分行名稱
			selectGenBrn();
			//--取得公司戶數、公司持有商務卡數
			selectCorpCnt();
			//--取得負責人卡數
			selectChargeCnt();
			//--取得公司或負責人申辦商務卡或信用卡數
			selectCreidtCorpCnt();
						
			//--商務卡滲透率
			if(corpCnt == 0) {
				corpCardRatio = 0;
				chargeCardRatio = 0;
				creditCorpRatio = 0;
			}	else	{								
				corpCardRatio = divide((double) corpHasCardCnt / (double) corpCnt*100);
				chargeCardRatio = divide((double) chargeHasCardCnt / (double) corpCnt*100);
				creditCorpRatio = divide((double) creditCorpCnt / (double) corpCnt*100);
			}
			
			insertRskCrm36aRpt();			
		}
		
	}
	
	void insertRskCrm36aRpt() throws Exception {
		daoTable = "rsk_crm36a_rpt";	
		setValue("data_date",hBusiDate);
		setValue("branch",branch);
		setValue("branch_name",branchName);
		setValueInt("corp_cnt",corpCnt);
		setValueInt("corp_card_cnt",corpHasCardCnt);
		setValueDouble("corp_card_ratio",corpCardRatio);
		setValueInt("charge_card_cnt",chargeHasCardCnt);
		setValueDouble("charge_card_ratio",chargeCardRatio);
		setValueInt("credit_corp_cnt",creditCorpCnt);
		setValueDouble("credit_corp_ratio",creditCorpRatio);
		setValue("crt_user","ecs");
		setValue("crt_date",sysDate);
		setValue("crt_time",sysTime);
		setValue("mod_user","ecs");
		setValue("mod_time",sysDate + sysTime);
		setValue("mod_pgm","Rskp181");
		setValueInt("mod_seqno",1);
		insertTable();
	}
	
	int tiGenBrn = -1;
	void selectGenBrn() throws Exception {
		if(tiGenBrn <=0) {
			sqlCmd = " select full_chi_name from gen_brn where branch = ? ";
			tiGenBrn =ppStmtCrt("ti-S-genBrn","");
		}
		
		setString(1,branch);
		sqlSelect(tiGenBrn);
		
		if(sqlNrow <= 0) {
			corpCnt = 0;
			return ;
		}
		
		branchName = colSs("full_chi_name");
		
	}
	
	int tiCorpCnt = -1;
	void selectCorpCnt() throws Exception {
		if (tiCorpCnt <=0) {			
			sqlCmd = " select count(*) as corp_cnt , sum(corp_has_card_cnt) as tl_corp_has_card_cnt "
				   + " from (select distinct corp_p_seqno , decode(corp_has_card,'Y',1,0) as corp_has_card_cnt "
				   + " from rsk_crm36a_data where data_date = ? and branch = ? and corp_p_seqno <> '') ";
			tiCorpCnt =ppStmtCrt("ti-S-crm36aCorpCnt","");
		}
		
		setString(1,hBusiDate);
		setString(2,branch);
		
		sqlSelect(tiCorpCnt);
		
		if(sqlNrow <= 0) {
			corpCnt = 0;
			corpHasCardCnt = 0;
			return ;
		}
		
		corpCnt = colInt("corp_cnt");		
		corpHasCardCnt = colInt("tl_corp_has_card_cnt");
	}
	
	int tiChargeCnt = -1;
	void selectChargeCnt() throws Exception {
		if (tiChargeCnt <=0) {			
			sqlCmd = " select sum(charge_has_card_cnt) as tl_charge_has_card_cnt "
				   + " from (select distinct id_p_seqno , decode(charge_has_card,'Y',1,0) as charge_has_card_cnt "
				   + " from rsk_crm36a_data where data_date = ? and branch = ? and id_p_seqno <> '') ";
			tiChargeCnt =ppStmtCrt("ti-S-crm36aChargeCnt","");
		}
		
		setString(1,hBusiDate);
		setString(2,branch);
		
		sqlSelect(tiChargeCnt);
		
		if(sqlNrow <= 0) {
			chargeHasCardCnt = 0;
			return ;
		}
		
		chargeHasCardCnt = colInt("tl_charge_has_card_cnt");				
	}
	
	
	int tiCreditCorpCnt = -1;
	void selectCreidtCorpCnt() throws Exception {
		if (tiCreditCorpCnt <=0) {			
			sqlCmd = " select count(*) as credit_corp_cnt from (select distinct corp_p_seqno , id_p_seqno from rsk_crm36a_data where data_date = ? and branch = ? and (corp_has_card ='Y' or charge_has_card ='Y') group by corp_p_seqno , id_p_seqno) ";
			tiCreditCorpCnt =ppStmtCrt("ti-S-crm36aCreditCorpCnt","");
		}
		
		setString(1,hBusiDate);
		setString(2,branch);
		
		sqlSelect(tiCreditCorpCnt);
		
		if(sqlNrow <= 0) {
			creditCorpCnt = 0;
			return ;
		}
		
		creditCorpCnt = colInt("credit_corp_cnt");		
	}
	
	void initData() {
		branch = "";
		branchName = "";
		corpHasCardCnt = 0;
		chargeHasCardCnt = 0;
		creditCorpCnt = 0;
		corpCnt = 0;
		corpCardRatio = 0.0;
		chargeCardRatio = 0.0;
		creditCorpRatio = 0.0;
	}
	
//	public static double divide(double a, double b, int scale){
//		BigDecimal bd1 = new BigDecimal(Double.toString(a));
//		BigDecimal bd2 = new BigDecimal(Double.toString(b));
//		return bd1.divide(bd2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
//	}
	
	public static double divide(double c) {
		BigDecimal b = new BigDecimal(c);
		return b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
}
