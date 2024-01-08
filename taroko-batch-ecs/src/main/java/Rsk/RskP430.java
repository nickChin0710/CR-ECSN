package Rsk;
/**
 * 2020-0709  V1.00.00	JH		--ptr_sys_parm.TONCCC-A
 * 2020-0507  V1.00.01	JH		bill_type in (NC,OI)
 * 2020-0423  V1.00.02  JH		ctrl_seqno.len(10)
 * 2020-0421  V1.00.03	JH		tran_code.tot_amt
 * 2020-0211  V1.00.04	JH		VM國外扣款不送
 * 2019-1105  V1.00.05  JH      dir=./nmip/O/001/...
 * 109-11-23  V1.00.06  tanwei  updated for project coding standard
*  109/12/30  V1.00.07  yanghan       修改了部分无意义的變量名稱          *
 * 112/03/28  V1.00.08  Alex    增加footer統計
 * 112/03/29  V1.00.09  Alex    檔案傳送到送給財金位置
 * 112/05/17  V1.00.10  Alex    表頭 TEST 改為 PROD
 * 112/05/18  V1.00.11  Alex    Header 序號修正
 * 112/06/13  V1.00.12  Alex    檔案欄位修正
 * 112/06/29  V1.00.13  Alex    檔案欄位修正
 * 112/07/05  V1.00.14  Alex    Microfilm 擷取修正
 * 112/07/06  V1.00.15  Alex    JCB src bin 修正
 * 112/08/24  V1.00.16  Alex    reference_no 讀不到 bil_fiscdtl 改以 card_no + film_no 讀取 bil_fiscdtl
 * */

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.CommFunction;
import com.CommRoutine;
import com.BaseBatch;
import com.CommCrd;
import com.CommFTP;

public class RskP430 extends BaseBatch {
private String progname = "調單/扣款-待傳送資料製作處理程式  111/08/24 V1.00.16";
CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
CommFTP commFTP = null;
CommRoutine comr = null;
private int iiFileNum;
//===============================================
hdata.BilBill hBBill=new hdata.BilBill();

//--------------------------------
Map<String, Double> cntMap=new HashMap<>();
Map<String, Double> amtMap=new HashMap<>();

private boolean ibDebit=false;
boolean ibVisa=false,ibMast=false,ibJcb=false,ibUcard=false;
private boolean ibOversea=false;
private String hCardNewEndDate;
String isNcccTranCode="";
private String isReferNo="", isCtrlSeqno="" , isReptType = "" , isReasonCode = "" , isFileName = "" , isFileNo = "";
private int iiTextCnt=0;
//----------------
private int tiReptU=-1;
private int tiChgbU=-1;
private int tiDbbill=-1;
private int tiContr=-1;
private int tiCard=-1;
private int tiNccc300=-1;
private int tiFiscDtl = -1;
private int tiFiscDtl2 = -1;
private int tiBill2=-1;
private int tiCurpost=-1;
private int nn = 0; //--01~99
private String twDate = ""; //--民國年
private String newLine = "\r\n";
private int dataCnt = 0 ;
private int dataRecordCnt = 0;
private double dataAmt = 0.0;

int commit=1;
//=****************************************************************************
public static void main(String[] args) {
	RskP430 proc = new RskP430();
//	proc.debug = true;
	proc.mainProcess(args);
	proc.systemExit();
}

//=****************************************************************************
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	int liArg =args.length;
	if (liArg > 2) {
		printf("Usage : RskP430 [business_date, nn]");
		okExit(0);
	}

	dbConnect();

	if (liArg >0) {
		setBusiDate(args[0]);
		nn = commString.ss2int(args[1]);
	}
	
	if(hBusiDate.isEmpty())
		hBusiDate = comc.getBusiDate();
	
//	callBatch(0,0,0);

	checkOpen();
	textfileHeading();
	
	selectRskReceipt();
	selectRskChgback();
	
	textfileFooter1();
//	textfileFooter2();
	if (iiFileNum >=0) {
		this.closeOutputText(iiFileNum);
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
	}
		
	
	printf("資料處理筆數[%s], 文字檔筆數[%s]",totalCnt, iiTextCnt);
	
	sqlCommit(commit);
	endProgram();
}

void procFTP() throws Exception {	
	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	commFTP.hEflgSystemId = "FISC_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	commFTP.hEflgModPgm = javaProgram;

	   // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
	int errCode = commFTP.ftplogName("FISC_FTP_PUT", "mput " + isFileName);
	
	if (errCode != 0) {
		showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
		insertEcsNotifyLog(isFileName);
	}	
}

//=****************************************************************************
void checkOpen() throws Exception {
	String lsFileName ="M00600000.ICM51QBD.", lsFile="";
	twDate = commDate.toTwDate(hBusiDate);
	nn++;
	if(nn<10) {
		lsFileName = lsFileName + commString.bbMid(twDate, 1)+"0"+nn;
		isFileNo = "0"+nn;
	}	else {
		lsFileName = lsFileName + commString.bbMid(twDate, 1)+nn;
		isFileNo = ""+nn;
	}
	isFileName = lsFileName;
	lsFile =String.format("%s/media/rsk/%s",getEcsHome(),lsFileName);
//	fileRename(lsFile,"");
	
	iiFileNum =this.openOutputText(lsFile);
	if (iiFileNum <0) {
		errmsg("無法產生文字檔, [%s]", lsFileName);
		okExit(0);
	}
}

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

//=****************************************************************************
void calcTrancode(String binType, String tranCode, double amt1) {
	//String kk =bin_type+tran_code;
	String tranCodeTmp =tranCode;
	double liCnt=0;
	double lmAmt=0;
	
	try {
		liCnt = cntMap.get(tranCodeTmp);
		liCnt++;
		cntMap.put(tranCodeTmp, liCnt);
	} catch(Exception ex) {
		cntMap.put(tranCodeTmp, (double)1);
	}

	try {
		lmAmt = amtMap.get(tranCodeTmp);
		lmAmt = lmAmt + amt1;
		amtMap.put(tranCodeTmp, lmAmt);
	} catch(Exception ex) {
		amtMap.put(tranCodeTmp, amt1);
	}
	
	ddd("tran_code="+tranCodeTmp+"; cnt="+cntMap.get(tranCodeTmp)+"; amt="+amtMap.get(tranCodeTmp));
	return;
}
//=****************************************************************************
void selectBilBill(String aRefno) throws Exception {
	hBBill.initData();

	if (tiContr <=0) {
		sqlCmd ="select A.reference_no , B.reference_no as org_refer_no , A.card_no , A.bin_type , A.film_no "
				+" from bil_bill A left join bil_contract B on A.contract_no =B.contract_no"
				+" where A.reference_no =?";
		tiContr =ppStmtCrt("ti_contr","");
	}
	ppp(1,aRefno);
	sqlSelect(tiContr);
	if (sqlNrow <=0) {
		printf("查無帳單資料[bil_bill], kk[%s], ctrl[%s]", aRefno, isCtrlSeqno);
		return;
	}
	
	hBBill.referenceNo = colSs("reference_no");
	hBBill.cardNo = colSs("card_no");
	hBBill.binType = colSs("bin_type");
	hBBill.filmNo = colSs("film_no");
	
	String lsOrgRefno=colSs("org_refer_no");
	if (empty(lsOrgRefno))
		lsOrgRefno =colSs("reference_no");
	if (empty(lsOrgRefno)) {
		printf("查無(原始)帳單流水號[bil_bill], kk[%s]", aRefno);
		return;
	}
	
	selectBilFiscDtl();
	
//	int liRc=selectBilBill2(lsOrgRefno);
//	if (liRc !=0) {
//		selectBilCurpost(lsOrgRefno);
//	}
//
//	if (empty(hBBill.referenceNo)) {
//		printf("查無(原始)帳單資料[bil_bill], kk[%s,%s]", aRefno,lsOrgRefno);
//		return;
//	}
	
//	selectBilNccc300Dtl();
	
}


//=****************************************************************************
int selectBilBill2(String aRefno) throws Exception {
	if (tiBill2 <=0) {
		sqlCmd ="select * from bil_bill"
				+" where reference_no =?"
				;
		tiBill2 =ppStmtCrt("ti_bill2","");
	}
	ppp(1,aRefno);
	
	daoTid ="bill.";
	sqlSelect(tiBill2);
	if (sqlNrow <=0) {
		return 1;
	}
	
	move2hdata();
	return 0;
}
//=****************************************************************************
int selectBilCurpost(String aRefno) throws Exception {
	if (tiCurpost <=0) {
		sqlCmd ="select * from bil_curpost"
				+" where reference_no =?"
				;
		tiCurpost =ppStmtCrt("ti_curpost","");
	}
	ppp(1,aRefno);
	
	daoTid ="bill.";
	sqlSelect(tiCurpost);
	if (sqlNrow <=0) {
		return 1;
	}
	
	move2hdata();
	return 0;
}

//=****************************************************************************
void selectDbbBill(String aRefno) throws Exception {
	hBBill.initData();
	
	if (tiDbbill <=0) {
		sqlCmd ="select *"
				+" from dbb_bill"
				+" where reference_no =?";
		tiDbbill =ppStmtCrt("ti_dbbill","");
	}
	ppp(1,aRefno);
	
	daoTid ="bill.";
	sqlSelect(tiDbbill);
	if (sqlNrow <=0) {
		printf("無法取得原始帳單資料[dbb_bill], kk[%s]",aRefno);
		return;
	}
	
	move2hdata();
	selectBilFiscDtl();
}
//=****************************************************************************
void selectBilNccc300Dtl() throws Exception {
	if (tiNccc300 <=0) {
		sqlCmd ="select 'xx' as xx"
				+", exchange_rate       "
				+", exchange_date       "
				+", mcs_num             "
				+", pos_term_capability "
				+", reimbursement_attr  "
				+", terminal_id         "
				+", bnet_ref_num        "
				+", chip_condition_code "
				+", transaction_type    "
				+", card_seq_num        "
				+", unpredic_num        "
				+", app_tran_count      "
				+", app_int_pro         "
				+", cryptogram          "
				+", terminal_ver_results"
				+", cry_info_data       "
				+", terminal_cap_pro    "
				+", life_cyc_sup_ind    "
				+", banknet_date        "
				+", inter_rate_des      "
				+", service_code        "
				+", term_type           "
				+", wallet_iden         "
				+", issue_s_r           "
				+", auth_response_code  "
				+", accept_term_ind     "
				+", add_curcy_code      "
				+", de22                "
				+", mcs_num             "
				+", mcs_cnt             "
         +", qr_flag, mcht_pan, onus_pf"
				+" from bil_nccc300_dtl"
				+" where reference_no =?"
				+" and batch_no =?";
		tiNccc300 =ppStmtCrt("ti_nccc300","");
	}
	ppp(1,hBBill.referenceNo);
	ppp(hBBill.batchNo);
	daoTid ="bill.";
	try {
		sqlSelect(tiNccc300);
	}
	catch(Exception ex) {;}
	
	if (sqlNrow <=0) {
		colSet("bill.EXCHANGE_RATE","");
		colSet("bill.EXCHANGE_DATE","");
		colSet("bill.MCS_NUM","");
		colSet("bill.POS_TERM_CAPABILITY","");
		colSet("bill.REIMBURSEMENT_ATTR","");
		colSet("bill.TERMINAL_ID","");
		colSet("bill.BNET_REF_NUM","");
		colSet("bill.CHIP_CONDITION_CODE","");
		colSet("bill.TRANSACTION_TYPE","");
		colSet("bill.CARD_SEQ_NUM","");
		colSet("bill.UNPREDIC_NUM","");
		colSet("bill.APP_TRAN_COUNT","");
		colSet("bill.APP_INT_PRO","");
		colSet("bill.CRYPTOGRAM","");
		colSet("bill.TERMINAL_VER_RESULTS","");
		colSet("bill.CRY_INFO_DATA","");
		colSet("bill.TERMINAL_CAP_PRO","");
		colSet("bill.LIFE_CYC_SUP_IND","");
		colSet("bill.BANKNET_DATE","");
		colSet("bill.INTER_RATE_DES","");
		colSet("bill.SERVICE_CODE","");
		colSet("bill.TERM_TYPE","");
		colSet("bill.WALLET_IDEN","");
		colSet("bill.ISSUE_S_R","");
		colSet("bill.AUTH_RESPONSE_CODE","");
		colSet("bill.ACCEPT_TERM_IND","");
		colSet("bill.ADD_CURCY_CODE","");
		colSet("bill.DE22","");
		colSet("bill.MCS_NUM","");
		colSet("bill.MCS_CNT","");
      colSet("bill.qr_flag","");
      colSet("bill.mcht_pan","");
      colSet("bill.onus_pf","");

      return;
	}
	
}

void selectBilFiscDtl() throws Exception {
	if (tiFiscDtl <=0) {
		sqlCmd = "select fisc_tx_code , ecs_tx_code , dest_bin , source_bin , substr(purchase_date,5,4)||substr(purchase_date,3,2) as purchase_date , purchase_time , "
				+"source_amt , source_curr , dest_amt , dest_curr , setl_amt , mcht_no , mcc_code , mcht_eng_name , mcht_city , replace(mcht_chi_name,'　','') as mcht_chi_name ,"
				+"mcht_state , mcht_zip , mcht_country , auth_code , terminal_id , process_day , film_no , usage_code , "
				+"issue_ctrl_num , settl_flag , muti_clearing_seq , retrieval_req_id , acq_resp_code , pan_token , acct_num_type , "
				+"token_assure_level , token_requestor_id , transaction_id , de22_card_in_cap , de22_ch_auth_cap , de22_capture_cap , "
				+"de22_te_op_env , de22_ch_data , de22_card_data , de22_input_mode , de22_ch_auth_method , de22_ch_auth_entity , "
				+"de22_card_out_cap , de22_te_out_cap , de22_pin_capture_cap , cat_ind , te_send_date , "
				+"te_batch_no , te_tx_num , pos_entry_mode , pos_te_cap , reason_code , cb_ref_no , "
				+"vcrfs_ind , doc_ind , service_code , ucaf , reimb_info , reimb_code , reimb_attr , "
				+"payment_type , nccc_bill_type , nccc_mcht_type , nccc_item , message_text , ac_tx_type , "
				+"pan_seq_num , te_tx_date , te_profile , te_country , interface_dev_num , "
				+"ac_unpred_num , ap_tx_num , ap_profile , ac , iad , tvr , cvm_result , cryptogram_info , "
				+"ap_expire_date , auth_resp_code , post_issue_result , chip_cond_code , "
				+"te_entry_cap , card_verfy_result , ffi , emv_iad2 , procure_uniform ,"
				+"procure_name , procure_voucher_no , procure_receipt_no , procure_orig_curr , procure_orig_amt , procure_plan , procure_level_1 , procure_level_2 ,"
				+"procure_tx_num , procure_bank_fee , procure_cht_fee , procure_pay_amt , procure_tot_term , install_type , install_tot_term , "
				+"install_first_amt , install_per_amt , install_last_amt , install_charges , install_atm_no , install_proj_no , install_supply_no , "
				+"bonus_trans_bp , bonus_pay_cash , "
				+"ird , prepaid_card_ind , cwb_ind , fraud_notify_date , fraud_cb_cnt , floor_limit_ind , card_product_id , auth_valid_code , muti_clearing_seq ,"
				+"qps_cb_ind ,addn_acct_type , dcc_ind , addn_amt_curr , addn_amt_sign , addn_amt , "
//				+ "assure_level , "
				+ "payment_fa_id , pcas_ind , vcind , orig_tx_amt ,"
				+"m_cross_ind , m_curr_ind , m_cca , fpi , sc_to_bc_rate , bc_to_dc_rate , charge_ind , par , vrol_case_num , ec_ind "
				+"from bil_fiscdtl where ecs_reference_no = ?  "
				;
	
		tiFiscDtl =ppStmtCrt("ti_fiscdtl","");
	}
	ppp(1,hBBill.referenceNo);
	daoTid ="bill.";
	
	sqlSelect(tiFiscDtl);
	
	if (sqlNrow > 0) {
		hBBill.fiscTxCode = colSs("bill.fisc_tx_code");
		hBBill.ecsTxCode = colSs("bill.ecs_tx_code");
		hBBill.destBin = colSs("bill.dest_bin");
		hBBill.srcBin = colSs("bill.source_bin");
		hBBill.purchaseDate = colSs("bill.purchase_date");
		hBBill.purchaseTime = colSs("bill.purchase_time");
		hBBill.sourceAmt = colNum("bill.source_amt");
		hBBill.sourceCurr = colSs("bill.source_curr");
		hBBill.destAmt = colNum("bill.dest_amt");
		hBBill.destCurr = colSs("bill.dest_curr");
		hBBill.settlAmt = colNum("bill.setl_amt");
		hBBill.mchtNo = colSs("bill.mcht_no");
		hBBill.mccCode = colSs("bill.mcc_code");
		hBBill.mchtEngName = colSs("bill.mcht_eng_name");
		hBBill.mchtChiName = colSs("bill.mcht_chi_name");
		hBBill.mchtCity = colSs("bill.mcht_city");
		hBBill.mchtState = colSs("bill.mcht_state");
		hBBill.mchtZip = colSs("bill.mcht_zip");
		hBBill.mchtCountry = colSs("bill.mcht_country");
		hBBill.authCode = colSs("bill.auth_code");
		hBBill.terminalId = colSs("bill.terminal_id");
		hBBill.processDay = colSs("bill.process_day");
		hBBill.fileNo = colSs("bill.film_no");
		hBBill.usageCode = colSs("bill.usage_code");
		hBBill.issueCtrlNum = colSs("bill.issue_ctrl_num");
		hBBill.settlFlag = colSs("bill.settl_flag");
		hBBill.mutiClearingSeq = colSs("bill.muti_clearing_seq");
		hBBill.retrievalReqId = colSs("bill.retrieval_req_id");
		hBBill.acqRespCode = colSs("bill.acq_resp_code");
		hBBill.panToken = colSs("bill.pan_token");		
		hBBill.acctNumType = colSs("bill.acct_num_type");
		hBBill.tokenAssureLevel = colSs("bill.token_assure_level");
		hBBill.tokenRequestorId = colSs("bill.token_requestor_id");		
		hBBill.transactionId = colSs("bill.transaction_id");
		hBBill.cardInCap = colSs("bill.de22_card_in_cap");
		hBBill.chAuthCap = colSs("bill.de22_ch_auth_cap");		
		hBBill.captureCap = colSs("bill.de22_capture_cap");		
		hBBill.teOpEnv = colSs("bill.de22_te_op_env");
		hBBill.chData = colSs("bill.de22_ch_data");
		hBBill.cardData = colSs("bill.de22_card_data");
		hBBill.inputMode = colSs("bill.de22_input_mode");
		hBBill.chAuthMethod = colSs("bill.de22_ch_auth_method");
		hBBill.chAuthEntity = colSs("bill.de22_ch_auth_entity");
		hBBill.cardOutCap = colSs("bill.de22_card_out_cap");
		hBBill.teOutCap = colSs("bill.de22_te_out_cap");
		hBBill.pinCaptureCap = colSs("bill.de22_pin_capture_cap");		
		hBBill.catInd = colSs("bill.cat_ind");
		hBBill.teSendDate = colSs("bill.te_send_date");
		hBBill.teSendDate = commDate.toTwDate(hBBill.teSendDate);
		hBBill.teBatchNo = colSs("bill.te_batch_no");
		hBBill.teTxNum = colSs("bill.te_tx_num");
		hBBill.posTeCap = colSs("bill.pos_te_cap");
		hBBill.posEntryMode = colSs("bill.pos_entry_mode");
		hBBill.reasonCode = colSs("bill.reason_code");
		hBBill.cbRefNo = colSs("bill.cb_ref_no");
		hBBill.vcrfsInd = colSs("bill.vcrfs_ind");
		hBBill.docInd = colSs("bill.doc_ind");
		hBBill.serviceCode = colSs("bill.service_code");
		hBBill.ucaf = colSs("bill.ucaf");
		hBBill.reimbInfo = colSs("bill.reimb_info");
		hBBill.reimbCode = colSs("bill.reimb_code");
		hBBill.reimbAttr = colSs("bill.reimb_attr");
		hBBill.paymentType = colSs("bill.payment_type");
		hBBill.ncccBillType = colSs("bill.nccc_bill_type");
		hBBill.ncccMchtType = colSs("bill.nccc_mcht_type");
		hBBill.ncccItem = colSs("bill.nccc_item");
		hBBill.messageText = colSs("bill.message_text");
		hBBill.acCryptoAmt = colNum("bill.ac_crypto_amt");
		hBBill.acTxType = colSs("bill.ac_tx_type");
		hBBill.panSeqNum = colSs("bill.pan_seq_num");
		hBBill.teTxDate = colSs("bill.te_tx_date");
		hBBill.teProfile = colSs("bill.te_profile");
		hBBill.teCountry = colSs("bill.te_country");
		hBBill.interfaceDevNum = colSs("bill.interface_dev_num");
		hBBill.acUnpredNum = colSs("bill.ac_unpred_num");
		hBBill.apTxNum = colSs("bill.ap_tx_num");
		hBBill.apProfile = colSs("bill.ap_profile");
		hBBill.ac = colSs("bill.ac");
		hBBill.iad = colSs("bill.iad");
		hBBill.tvr = colSs("bill.tvr");
		hBBill.cvmResult = colSs("bill.cvm_result");
		hBBill.cryptogramInfo = colSs("bill.cryptogram_info");
		hBBill.apExpireDate = colSs("bill.ap_expire_date");
		hBBill.authRespCode = colSs("bill.auth_resp_code");
		hBBill.postIssueResult = colSs("bill.post_issue_result");
		hBBill.chipCondCode = colSs("bill.chip_cond_code");
		hBBill.teEntryCap = colSs("bill.te_entry_cap");
		hBBill.cardVerifyResult = colSs("bill.card_verfy_result");
		hBBill.ffi = colSs("bill.ffi");
		hBBill.emvIad2 = colSs("bill.emv_iad2");
		hBBill.procureUniform = colSs("bill.procure_uniform");
		hBBill.procureName = colSs("bill.procure_name");
		hBBill.procureVoucherNo = colSs("bill.procure_voucher_no");
		hBBill.procureReceiptNo = colSs("bill.procure_receipt_no");
		hBBill.procureOrigCurr = colSs("bill.procure_orig_curr");
		hBBill.procureOrigAmt = colNum("bill.procure_orig_amt");
		hBBill.procurePlan = colSs("bill.procure_plan");
		hBBill.procureLevel1 = colSs("bill.procure_level_1");
		hBBill.procureLevel2 = colSs("bill.procure_level_2");
		hBBill.procureTxNum = colSs("bill.procure_tx_num");
		hBBill.procureBankFee = colInt("bill.procure_bank_fee");
		hBBill.procureChtFee = colInt("bill.procure_cht_fee");
		hBBill.procurePayAmt = colInt("bill.procure_pay_amt");
		hBBill.procureTotTerm = colInt("bill.procure_tot_term");
		hBBill.installType = colSs("bill.install_type");
		hBBill.installTotTerm = colInt("bill.install_tot_term");
		hBBill.installFirstAmt = colInt("bill.install_first_amt");
		hBBill.installPerAmt = colInt("bill.install_per_amt");
		hBBill.installLastAmt = colInt("bill.install_last_amt");
		hBBill.installCharges = colInt("bill.install_charges");
		hBBill.installAtmNo = colSs("bill.install_atm_no");
		hBBill.installProjNo = colSs("bill.install_proj_no");
		hBBill.installSupplyNo = colSs("bill.install_supply_no");
		hBBill.bonusTransBp = colSs("bill.bonus_trans_bp");
		hBBill.bonusPayCash = colNum("bill.bonus_pay_cash");
		hBBill.ird = colSs("bill.ird");
		hBBill.prepaidCardInd = colSs("bill.prepaid_card_ind");
		hBBill.cwbInd = colSs("bill.cwb_ind");
		hBBill.fraudNotifyDate = colSs("bill.fraud_notify_date");
		hBBill.fraudCbCnt = colSs("bill.fraud_cb_cnt");
		hBBill.floorLimitInd = colSs("bill.floor_limit_ind");
		hBBill.cardProductId = colSs("bill.card_product_id");
		hBBill.authValidCode = colSs("bill.auth_valid_code");
		hBBill.multiClearingSeq = colSs("bill.muti_clearing_seq");
		hBBill.qpsCbInd = colSs("bill.qps_cb_ind");
		hBBill.addnAcctType = colSs("bill.addn_acct_type");
		hBBill.dccInd = colSs("bill.dcc_ind");
		hBBill.addnAmtCurr = colSs("bill.addn_amt_curr");
		hBBill.addnAmtSign = colSs("bill.addn_amt_sign");
		hBBill.addnAmt = colNum("bill.addn_amt");
		hBBill.assureLevel = colSs("bill.assure_level");
		hBBill.paymentFaId = colSs("bill.payment_fa_id");
		hBBill.pcasInd = colSs("bill.pcas_ind");
		hBBill.vcind = colSs("bill.vcind");
		hBBill.origTxAmt = colInt("bill.orig_tx_amt");
		hBBill.mCrossInd = colSs("bill.m_cross_ind");
		hBBill.mCurrInd = colSs("bill.m_curr_ind");
		hBBill.mCca = colSs("bill.m_cca");
		hBBill.fpi = colSs("bill.fpi");
		hBBill.scToBcRate = colSs("bill.sc_to_bc_rate");
		hBBill.bcToDcRate = colSs("bill.bc_to_dc_rate");
		hBBill.chargeInd = colSs("bill.charge_ind");
		hBBill.par = colSs("bill.par");
		hBBill.vrolCaseNum = colSs("bill.vrol_case_num");
		hBBill.ecInd = colSs("bill.ec_ind");
		
		return;
	}	else	{
		if (tiFiscDtl2 <=0) {
			sqlCmd = "select fisc_tx_code , ecs_tx_code , dest_bin , source_bin , substr(purchase_date,5,4)||substr(purchase_date,3,2) as purchase_date , purchase_time , "
					+"source_amt , source_curr , dest_amt , dest_curr , setl_amt , mcht_no , mcc_code , mcht_eng_name , mcht_city , replace(mcht_chi_name,'　','') as mcht_chi_name ,"
					+"mcht_state , mcht_zip , mcht_country , auth_code , terminal_id , process_day , film_no , usage_code , "
					+"issue_ctrl_num , settl_flag , muti_clearing_seq , retrieval_req_id , acq_resp_code , pan_token , acct_num_type , "
					+"token_assure_level , token_requestor_id , transaction_id , de22_card_in_cap , de22_ch_auth_cap , de22_capture_cap , "
					+"de22_te_op_env , de22_ch_data , de22_card_data , de22_input_mode , de22_ch_auth_method , de22_ch_auth_entity , "
					+"de22_card_out_cap , de22_te_out_cap , de22_pin_capture_cap , cat_ind , te_send_date , "
					+"te_batch_no , te_tx_num , pos_entry_mode , pos_te_cap , reason_code , cb_ref_no , "
					+"vcrfs_ind , doc_ind , service_code , ucaf , reimb_info , reimb_code , reimb_attr , "
					+"payment_type , nccc_bill_type , nccc_mcht_type , nccc_item , message_text , ac_tx_type , "
					+"pan_seq_num , te_tx_date , te_profile , te_country , interface_dev_num , "
					+"ac_unpred_num , ap_tx_num , ap_profile , ac , iad , tvr , cvm_result , cryptogram_info , "
					+"ap_expire_date , auth_resp_code , post_issue_result , chip_cond_code , "
					+"te_entry_cap , card_verfy_result , ffi , emv_iad2 , procure_uniform ,"
					+"procure_name , procure_voucher_no , procure_receipt_no , procure_orig_curr , procure_orig_amt , procure_plan , procure_level_1 , procure_level_2 ,"
					+"procure_tx_num , procure_bank_fee , procure_cht_fee , procure_pay_amt , procure_tot_term , install_type , install_tot_term , "
					+"install_first_amt , install_per_amt , install_last_amt , install_charges , install_atm_no , install_proj_no , install_supply_no , "
					+"bonus_trans_bp , bonus_pay_cash , "
					+"ird , prepaid_card_ind , cwb_ind , fraud_notify_date , fraud_cb_cnt , floor_limit_ind , card_product_id , auth_valid_code , muti_clearing_seq ,"
					+"qps_cb_ind ,addn_acct_type , dcc_ind , addn_amt_curr , addn_amt_sign , addn_amt , "
//					+ "assure_level , "
					+ "payment_fa_id , pcas_ind , vcind , orig_tx_amt ,"
					+"m_cross_ind , m_curr_ind , m_cca , fpi , sc_to_bc_rate , bc_to_dc_rate , charge_ind , par , vrol_case_num , ec_ind "
					+"from bil_fiscdtl where card_no = ? and film_no = ? "
					;
		
			tiFiscDtl2 =ppStmtCrt("ti_fiscdtl2","");
		}
		ppp(1,hBBill.cardNo);
		ppp(2,hBBill.filmNo);
		daoTid ="bill.";
		
		sqlSelect(tiFiscDtl2);
		if (sqlNrow > 0) {
			hBBill.fiscTxCode = colSs("bill.fisc_tx_code");
			hBBill.ecsTxCode = colSs("bill.ecs_tx_code");
			hBBill.destBin = colSs("bill.dest_bin");
			hBBill.srcBin = colSs("bill.source_bin");
			hBBill.purchaseDate = colSs("bill.purchase_date");
			hBBill.purchaseTime = colSs("bill.purchase_time");
			hBBill.sourceAmt = colNum("bill.source_amt");
			hBBill.sourceCurr = colSs("bill.source_curr");
			hBBill.destAmt = colNum("bill.dest_amt");
			hBBill.destCurr = colSs("bill.dest_curr");
			hBBill.settlAmt = colNum("bill.setl_amt");
			hBBill.mchtNo = colSs("bill.mcht_no");
			hBBill.mccCode = colSs("bill.mcc_code");
			hBBill.mchtEngName = colSs("bill.mcht_eng_name");
			hBBill.mchtChiName = colSs("bill.mcht_chi_name");
			hBBill.mchtCity = colSs("bill.mcht_city");
			hBBill.mchtState = colSs("bill.mcht_state");
			hBBill.mchtZip = colSs("bill.mcht_zip");
			hBBill.mchtCountry = colSs("bill.mcht_country");
			hBBill.authCode = colSs("bill.auth_code");
			hBBill.terminalId = colSs("bill.terminal_id");
			hBBill.processDay = colSs("bill.process_day");
			hBBill.fileNo = colSs("bill.film_no");
			hBBill.usageCode = colSs("bill.usage_code");
			hBBill.issueCtrlNum = colSs("bill.issue_ctrl_num");
			hBBill.settlFlag = colSs("bill.settl_flag");
			hBBill.mutiClearingSeq = colSs("bill.muti_clearing_seq");
			hBBill.retrievalReqId = colSs("bill.retrieval_req_id");
			hBBill.acqRespCode = colSs("bill.acq_resp_code");
			hBBill.panToken = colSs("bill.pan_token");		
			hBBill.acctNumType = colSs("bill.acct_num_type");
			hBBill.tokenAssureLevel = colSs("bill.token_assure_level");
			hBBill.tokenRequestorId = colSs("bill.token_requestor_id");		
			hBBill.transactionId = colSs("bill.transaction_id");
			hBBill.cardInCap = colSs("bill.de22_card_in_cap");
			hBBill.chAuthCap = colSs("bill.de22_ch_auth_cap");		
			hBBill.captureCap = colSs("bill.de22_capture_cap");		
			hBBill.teOpEnv = colSs("bill.de22_te_op_env");
			hBBill.chData = colSs("bill.de22_ch_data");
			hBBill.cardData = colSs("bill.de22_card_data");
			hBBill.inputMode = colSs("bill.de22_input_mode");
			hBBill.chAuthMethod = colSs("bill.de22_ch_auth_method");
			hBBill.chAuthEntity = colSs("bill.de22_ch_auth_entity");
			hBBill.cardOutCap = colSs("bill.de22_card_out_cap");
			hBBill.teOutCap = colSs("bill.de22_te_out_cap");
			hBBill.pinCaptureCap = colSs("bill.de22_pin_capture_cap");		
			hBBill.catInd = colSs("bill.cat_ind");
			hBBill.teSendDate = colSs("bill.te_send_date");
			hBBill.teSendDate = commDate.toTwDate(hBBill.teSendDate);
			hBBill.teBatchNo = colSs("bill.te_batch_no");
			hBBill.teTxNum = colSs("bill.te_tx_num");
			hBBill.posTeCap = colSs("bill.pos_te_cap");
			hBBill.posEntryMode = colSs("bill.pos_entry_mode");
			hBBill.reasonCode = colSs("bill.reason_code");
			hBBill.cbRefNo = colSs("bill.cb_ref_no");
			hBBill.vcrfsInd = colSs("bill.vcrfs_ind");
			hBBill.docInd = colSs("bill.doc_ind");
			hBBill.serviceCode = colSs("bill.service_code");
			hBBill.ucaf = colSs("bill.ucaf");
			hBBill.reimbInfo = colSs("bill.reimb_info");
			hBBill.reimbCode = colSs("bill.reimb_code");
			hBBill.reimbAttr = colSs("bill.reimb_attr");
			hBBill.paymentType = colSs("bill.payment_type");
			hBBill.ncccBillType = colSs("bill.nccc_bill_type");
			hBBill.ncccMchtType = colSs("bill.nccc_mcht_type");
			hBBill.ncccItem = colSs("bill.nccc_item");
			hBBill.messageText = colSs("bill.message_text");
			hBBill.acCryptoAmt = colNum("bill.ac_crypto_amt");
			hBBill.acTxType = colSs("bill.ac_tx_type");
			hBBill.panSeqNum = colSs("bill.pan_seq_num");
			hBBill.teTxDate = colSs("bill.te_tx_date");
			hBBill.teProfile = colSs("bill.te_profile");
			hBBill.teCountry = colSs("bill.te_country");
			hBBill.interfaceDevNum = colSs("bill.interface_dev_num");
			hBBill.acUnpredNum = colSs("bill.ac_unpred_num");
			hBBill.apTxNum = colSs("bill.ap_tx_num");
			hBBill.apProfile = colSs("bill.ap_profile");
			hBBill.ac = colSs("bill.ac");
			hBBill.iad = colSs("bill.iad");
			hBBill.tvr = colSs("bill.tvr");
			hBBill.cvmResult = colSs("bill.cvm_result");
			hBBill.cryptogramInfo = colSs("bill.cryptogram_info");
			hBBill.apExpireDate = colSs("bill.ap_expire_date");
			hBBill.authRespCode = colSs("bill.auth_resp_code");
			hBBill.postIssueResult = colSs("bill.post_issue_result");
			hBBill.chipCondCode = colSs("bill.chip_cond_code");
			hBBill.teEntryCap = colSs("bill.te_entry_cap");
			hBBill.cardVerifyResult = colSs("bill.card_verfy_result");
			hBBill.ffi = colSs("bill.ffi");
			hBBill.emvIad2 = colSs("bill.emv_iad2");
			hBBill.procureUniform = colSs("bill.procure_uniform");
			hBBill.procureName = colSs("bill.procure_name");
			hBBill.procureVoucherNo = colSs("bill.procure_voucher_no");
			hBBill.procureReceiptNo = colSs("bill.procure_receipt_no");
			hBBill.procureOrigCurr = colSs("bill.procure_orig_curr");
			hBBill.procureOrigAmt = colNum("bill.procure_orig_amt");
			hBBill.procurePlan = colSs("bill.procure_plan");
			hBBill.procureLevel1 = colSs("bill.procure_level_1");
			hBBill.procureLevel2 = colSs("bill.procure_level_2");
			hBBill.procureTxNum = colSs("bill.procure_tx_num");
			hBBill.procureBankFee = colInt("bill.procure_bank_fee");
			hBBill.procureChtFee = colInt("bill.procure_cht_fee");
			hBBill.procurePayAmt = colInt("bill.procure_pay_amt");
			hBBill.procureTotTerm = colInt("bill.procure_tot_term");
			hBBill.installType = colSs("bill.install_type");
			hBBill.installTotTerm = colInt("bill.install_tot_term");
			hBBill.installFirstAmt = colInt("bill.install_first_amt");
			hBBill.installPerAmt = colInt("bill.install_per_amt");
			hBBill.installLastAmt = colInt("bill.install_last_amt");
			hBBill.installCharges = colInt("bill.install_charges");
			hBBill.installAtmNo = colSs("bill.install_atm_no");
			hBBill.installProjNo = colSs("bill.install_proj_no");
			hBBill.installSupplyNo = colSs("bill.install_supply_no");
			hBBill.bonusTransBp = colSs("bill.bonus_trans_bp");
			hBBill.bonusPayCash = colNum("bill.bonus_pay_cash");
			hBBill.ird = colSs("bill.ird");
			hBBill.prepaidCardInd = colSs("bill.prepaid_card_ind");
			hBBill.cwbInd = colSs("bill.cwb_ind");
			hBBill.fraudNotifyDate = colSs("bill.fraud_notify_date");
			hBBill.fraudCbCnt = colSs("bill.fraud_cb_cnt");
			hBBill.floorLimitInd = colSs("bill.floor_limit_ind");
			hBBill.cardProductId = colSs("bill.card_product_id");
			hBBill.authValidCode = colSs("bill.auth_valid_code");
			hBBill.multiClearingSeq = colSs("bill.muti_clearing_seq");
			hBBill.qpsCbInd = colSs("bill.qps_cb_ind");
			hBBill.addnAcctType = colSs("bill.addn_acct_type");
			hBBill.dccInd = colSs("bill.dcc_ind");
			hBBill.addnAmtCurr = colSs("bill.addn_amt_curr");
			hBBill.addnAmtSign = colSs("bill.addn_amt_sign");
			hBBill.addnAmt = colNum("bill.addn_amt");
			hBBill.assureLevel = colSs("bill.assure_level");
			hBBill.paymentFaId = colSs("bill.payment_fa_id");
			hBBill.pcasInd = colSs("bill.pcas_ind");
			hBBill.vcind = colSs("bill.vcind");
			hBBill.origTxAmt = colInt("bill.orig_tx_amt");
			hBBill.mCrossInd = colSs("bill.m_cross_ind");
			hBBill.mCurrInd = colSs("bill.m_curr_ind");
			hBBill.mCca = colSs("bill.m_cca");
			hBBill.fpi = colSs("bill.fpi");
			hBBill.scToBcRate = colSs("bill.sc_to_bc_rate");
			hBBill.bcToDcRate = colSs("bill.bc_to_dc_rate");
			hBBill.chargeInd = colSs("bill.charge_ind");
			hBBill.par = colSs("bill.par");
			hBBill.vrolCaseNum = colSs("bill.vrol_case_num");
			hBBill.ecInd = colSs("bill.ec_ind");
			
			return;
		}
	}
}

//=****************************************************************************
void move2hdata() {
	hBBill.referenceNo =colSs("bill.reference_no");
	hBBill.batchNo =colSs("bill.batch_no");
	hBBill.acqMemberId =colSs("bill.acq_member_id");
	hBBill.authCode  =colSs("bill.auth_code");
	hBBill.cardNo =colSs("bill.card_no");
	hBBill.binType =colSs("bill.bin_type");
	hBBill.destAmt =colNum("bill.dest_amt");
	hBBill.destCurr =colSs("bill.dest_curr");
	hBBill.filmNo =colSs("bill.film_no");
	hBBill.paymentType =colSs("bill.payment_type");
	hBBill.purchaseDate =colSs("bill.purchase_date");
	hBBill.processDate =colSs("bill.process_date");
	hBBill.sourceAmt =colNum("bill.source_amt");
	hBBill.sourceCurr =colSs("bill.source_curr");
	hBBill.settlAmt =colNum("bill.settl_amt");
	hBBill.mchtChiName =colSs("bill.mcht_chi_name");
	hBBill.mchtEngName =colSs("bill.mcht_eng_name");
	hBBill.mchtCity =colSs("bill.mcht_city");
	hBBill.mchtCountry =colSs("bill.mcht_country");
	hBBill.mchtCategory =colSs("bill.mcht_category");
	hBBill.mchtZip =colSs("bill.mcht_zip");
	hBBill.mchtState =colSs("bill.mcht_state");
	hBBill.mchtNo =colSs("bill.mcht_no");
	hBBill.posEntryMode =colSs("bill.pos_entry_mode");
	hBBill.mchtZipTw =colSs("bill.mcht_zip_tw");
	hBBill.mchtType =colSs("bill.mcht_type");
	hBBill.referenceNoFeeF =colSs("bill.reference_no_fee_f");
	hBBill.amtMccr =colSs("bill.amt_mccr"); 
	hBBill.amtIccr =colSs("bill.amt_iccr");
	hBBill.issueFee =colNum("bill.issue_fee"); 
	hBBill.includeFeeAmt =colNum("bill.include_fee_amt");
	hBBill.ucaf =colSs("bill.ucaf");
	hBBill.installTotTerm1 =colInt("bill.install_tot_term1");
	hBBill.installFirstAmt =colInt("bill.install_first_amt");
	hBBill.installPerAmt =colInt("bill.install_per_amt");
	hBBill.installFee =colInt("bill.install_fee");
	hBBill.deductBp =colInt("bill.deduct_bp");
	hBBill.cashPayAmt =colInt("bill.cash_pay_amt");
	if (!ibDebit) {
		hBBill.vCardNo =colSs("bill.v_card_no");
		hBBill.currCode =colSs("bill.curr_code");
		hBBill.dcDestAmt =colNum("bill.dc_dest_amt");
		hBBill.installmentKind =colSs("bill.installment_kind");
	}
	
}
//=****************************************************************************
void selectRskReceipt() throws Exception {
	
	sqlCmd ="select A.ctrl_seqno, A.debit_flag,"
			+" A.reference_no, A.bin_type,"
			+" A.rept_seqno,"
			+" A.rept_type,"
			+" A.reason_code,"
			+" A.add_date,"
			+" decode(A.rept_type,'1','52','51') as db_tran_code,"
			+" decode(reference_no_ori,'',reference_no,reference_no_ori) as reference_no_ori,"
			+" hex(A.rowid) as rowid"
			+" from rsk_receipt A"
			+" where A.send_flag ='1'"
			+" and A.send_apr_flag ='Y'"
//			+" and A.bill_type like 'NC%'"
			+" and substr(bill_type, 1, 2) IN ('NC','OI','FI')"
			+" order by A.card_no, A.film_no, A.bin_type, A.ctrl_seqno"
			;

	daoTable="rsk_receipt";
	this.fetchExtend ="rept.";
	openCursor();
	while(fetchTable()) {
		isCtrlSeqno =colSs("rept.ctrl_seqno");
		isReferNo =colSs("rept.reference_no_ori");
		if (empty(isReferNo)) {
			isReferNo =colSs("rept.reference_no");
		}
		ibDebit =colEq("rept.debit_flag","Y");
		isNcccTranCode =colSs("rept.db_tran_code");
		isReptType = colSs("rept.rept_type");
		isReasonCode = colSs("rept.reason_code");
		totalCnt++;
		
		ddd("-->%s. REPT.ctrl-refno[%s,%s]...",
				totalCnt,colSs("rept.ctrl_seqno"),isReferNo);
		
//		if (empty(isNcccTranCode)) {
//			errmsg("rsk_receipt.NCCC Transaction code is 空白, kk[%s]",colSs("rept.ctrl_seqno"));
//			continue;
//		}
		
		if (strIN(colSs("rept.bin_type"),",V,M,J,N")==false) {
			printf("組織別: 不是 V,M,J,N; kk[%s,%s]",isReferNo,colSs("rept.bin_type"));
			continue;
		}
		
		if (ibDebit) {
			selectDbbBill(isReferNo);
		}
		else {
			selectBilBill(isReferNo);
		}
		
		if (empty(hBBill.referenceNo))
			continue;
						
		iiTextCnt++;
		dataCnt++;
		//-bool:VMJ-
		bin2Type();
		
		if(ibVisa) {
			hBBill.srcBin = "490706";
			hBBill.destBin = commString.bbMid(hBBill.fileNo, 1,6);
		}	else if(ibMast) {
			hBBill.srcBin = "003768";
		}	else if(ibJcb) {
			hBBill.srcBin = "136201";
			hBBill.destBin = commString.bbMid(hBBill.fileNo, 1,6);
		}
		
		
		
//		calcTrancode(hBBill.binType, isNcccTranCode, hBBill.destAmt);		
		textfileRept();
		
		updateRskReceipt(colSs("rept.rowid"));
	}
	
	this.closeCursor();
}
//=****************************************************************************
void textfileRept() throws Exception {

	String tmpString="";
	StringBuffer tt=new StringBuffer();
	DecimalFormat df = new DecimalFormat("0");
	
	//--01:X4 6251:正本  6252:影本
	if("1".equals(isReptType)) {
		tt.append(commString.bbFixlen("6252",4));
	}	else {
		tt.append(commString.bbFixlen("6251",4));
	}
	
	//--02:X1 固定值 1
	tt.append(commString.bbFixlen("1",1));
	
	//--03:X1 bin_type V/M/J
	tt.append(commString.bbFixlen(hBBill.binType,1));
	
	//--04:X11 傳送端 BIN
	tt.append(commString.bbFixlen(hBBill.srcBin,11));
	
	//--05:X11 接收端 BIN
	tt.append(commString.bbFixlen(hBBill.destBin,11));
	
	//--06:X19 卡片號碼
	tt.append(commString.bbFixlen(hBBill.cardNo,19));
	
	//--07:X6 購貨日期
	tt.append(commString.bbFixlen(hBBill.purchaseDate,6));
	
	//--08:X6 購貨時間
	tt.append(commString.bbFixlen(hBBill.purchaseTime,6));
	
	//--09:X12 來源端金額
	tmpString = df.format(hBBill.sourceAmt*100);
	tt.append(commString.lpad(tmpString,12,"0"));
	
	//--10:X3 來源端幣別
	tt.append(commString.bbFixlen(hBBill.sourceCurr,3));
	
	//--11:X12 目的端金額
	tmpString = df.format(hBBill.destAmt*100);
	tt.append(commString.lpad(tmpString,12,"0"));
	
	//--12:X3 目的端幣別
	tt.append(commString.bbFixlen(hBBill.destCurr,3));
	
	//--13:X12 清算金額
	tmpString = df.format(hBBill.settlAmt*100);
	tt.append(commString.lpad(tmpString,12,"0"));
	
	//--14:X20 特店代號
	tt.append(commString.bbFixlen(hBBill.mchtNo,20));
	
	//--15:X4 Mcc Code
	tt.append(commString.bbFixlen(hBBill.mccCode,4));
	
	//--16:X25 特店英文名稱
	tt.append(commString.bbFixlen(hBBill.mchtEngName,25));
	
	//--17:X13 特約商店所在地/城市
	tt.append(commString.bbFixlen(hBBill.mchtCity,13));
	
	//--18:X3 特約商店所在省分
	tt.append(commString.bbFixlen(hBBill.mchtState,3));
	
	//--19:X5 特店郵遞區號
	tt.append(commString.bbFixlen(hBBill.mchtZip,5));
	
	//--20:X3 特店國家代號
	tt.append(commString.bbFixlen(hBBill.mchtCountry,3));
	
	//--21:X6 授權碼
	tt.append(commString.bbFixlen(hBBill.authCode,6));
	
	//--22:X15 端末機代碼
	tt.append(commString.bbFixlen(hBBill.terminalId,15));
	
	//--23:X4 資料處理日 (太陽日)
	tt.append(commString.bbFixlen(hBBill.processDay,4));
	
	//--24:X23 為縮影編號
	tt.append(commString.bbFixlen(hBBill.fileNo,23));
	
	//--25:X1 使用碼
	tt.append(commString.bbFixlen(hBBill.usageCode,1));
	
	//--26:X4 調單理由碼
	tt.append(commString.bbFixlen(isReasonCode,4));
	
	//--27:X70 訊息
	tt.append(commString.bbFixlen("RETRIEVAL REQUEST",70));
	
	//--28:X10 發卡控制單位 [V、M]
	if(ibJcb) {
		tt.append(commString.space(10));
	} else {
		tt.append(commString.bbFixlen(hBBill.issueCtrlNum,10));
	}
	
	//--29:X1 清算識別碼
	tt.append(commString.bbFixlen(hBBill.settlFlag,1));
	
	//--30:X2 Multiple Clearing Sequence Number [V]
	if(ibVisa) {		
		tt.append(commString.bbFixlen(hBBill.mutiClearingSeq,2));
	} else {
		tt.append(commString.space(2));
	}
		
	//--31:X12 Retrieval Request ID [V]
	if(ibVisa) {		
		tt.append(commString.bbFixlen(hBBill.retrievalReqId,12));
	} else {
		tt.append(commString.space(12));
	}
	
	//--32:X1 調單文件類 [M、J]
	if(ibVisa) {		
		tt.append(commString.space(1));
	} else {
		if("2".equals(isReptType)) {
			tt.append(commString.bbFixlen("1",1));
		} else {
			tt.append(commString.bbFixlen("2",1));
		}		
	}
	
	//--33:X1 代理單位回覆碼 [M]
	tt.append(commString.space(1));
	
	//--34:X19 PAN TOKEN [V]
	if(ibVisa) {		
		tt.append(commString.bbFixlen(hBBill.panToken,19));
	} else {
		tt.append(commString.space(19));
	}
	
	//--35:X2 保留欄位
	tt.append(commString.space(2));
	
	//--36:X2 Account Number Type [M]
	if(ibMast) {		
		tt.append(commString.bbFixlen(hBBill.acctNumType,2));
	} else {
		tt.append(commString.space(2));
	}
	
	//--37:X2 Token Assurance Level [M]
	if(ibMast) {		
		tt.append(commString.bbFixlen(hBBill.tokenAssureLevel,2));
	} else {
		tt.append(commString.space(2));
	}
	
	//--38:X11 Token Requestor ID [M]
	if(ibMast) {		
		tt.append(commString.bbFixlen(hBBill.tokenRequestorId,11));
	} else {
		tt.append(commString.space(11));
	}	
	
	//--39:X1 VISA 調單交易 [V]
	if(ibVisa) {		
		tt.append(commString.bbFixlen("0",1));
	} else {
		tt.append(commString.space(1));
	}		
	
	//--40:X15 Trace ID [M]
	if(ibMast) {		
		tt.append(commString.bbFixlen(hBBill.transactionId,15));
	} else {
		tt.append(commString.space(15));
	}
	
	//--41~52:X12 DE22 相關欄位 [M、J]
	if(ibVisa) {
		tt.append(commString.space(12));		
	} else {
		tt.append(commString.bbFixlen(hBBill.cardInCap,1));
		tt.append(commString.bbFixlen(hBBill.chAuthCap,1));
		tt.append(commString.bbFixlen(hBBill.captureCap,1));
		tt.append(commString.bbFixlen(hBBill.teOpEnv,1));
		tt.append(commString.bbFixlen(hBBill.chData,1));
		tt.append(commString.bbFixlen(hBBill.cardData,1));
		tt.append(commString.bbFixlen(hBBill.inputMode,1));
		tt.append(commString.bbFixlen(hBBill.chAuthMethod,1));
		tt.append(commString.bbFixlen(hBBill.chAuthEntity,1));
		tt.append(commString.bbFixlen(hBBill.cardOutCap,1));
		tt.append(commString.bbFixlen(hBBill.teOutCap,1));
		tt.append(commString.bbFixlen(hBBill.pinCaptureCap,1));
	}
	
	//--53:X60 保留欄位
	tt.append(commString.space(60));
	
	//--54:換行符號 0D0A
	tt.append(newLine);
	
	writeTextFile(iiFileNum,tt.toString());
	dataRecordCnt ++;
	dataAmt += hBBill.sourceAmt ;
//	//01[001:X2]
//	tt.append(commString.bbFixlen(isNcccTranCode,2));
//	//2[003:9(2)]
//	tt.append("01");
//	//03[005:X19]; jh(R104-024)
//	if ( !empty(hBBill.vCardNo)) 
//		tt.append(commString.bbFixlen(hBBill.vCardNo,19));
//	else tt.append(commString.bbFixlen(hBBill.cardNo,19));
//	//04[024:9(23)]
//	tt.append(commString.bbFixlen(hBBill.filmNo,23));
//	//5[047:X11]
//	if (eq(hBBill.binType,"V"))
//		tt.append(commString.bbFixlen("00493817",11));
//	else if (eq(hBBill.binType,"M"))
//		tt.append(commString.bbFixlen("003383",11));
//	else if (eq(hBBill.binType,"J"))
//		tt.append(commString.bbFixlen(commString.left(hBBill.cardNo,6),11));
//	else if (eq(hBBill.binType,"N"))
//		tt.append(commString.bbFixlen("",11));
//	//6[058:X11]
//	tmpString =hBBill.acqMemberId;
//	if (eq(hBBill.binType,"M")) {
//		if (tmpString.length()==4)
//			tmpString ="00"+tmpString;
//	}
//	else if (this.strIN(hBBill.binType,",V,N")) {
//		tmpString ="";
//	}
//	tt.append(commString.bbFixlen(commString.left(tmpString,8),11));
//	//7[069:X6,mmddyy]
//	tmpString =hBBill.purchaseDate;
//	tt.append(commString.right(tmpString,4)+commString.mid(tmpString,2,2));
//	//8[075:9(12)]
//	tmpString =commString.lpad(String.format("%.2f",hBBill.sourceAmt),13,"0");
//	tt.append(commString.left(tmpString, 10)+commString.right(tmpString,2));
//	//9[087:X3]
//	tt.append(commString.bbFixlen(hBBill.sourceCurr,3));
//	//10[090:x25]
//	tt.append(commString.bbFixlen(hBBill.mchtEngName,25));
//	//11[115:x13]
//	tt.append(commString.bbFixlen(hBBill.mchtCity,13));
//	//12[128:x3]
//	tmpString =hBBill.mchtCountry;
//	if (eq(tmpString,"TWN"))
//		tmpString ="TW ";
//	tt.append(commString.bbFixlen(tmpString,3));
//	//13:[131:x4]
//	tt.append(commString.rpad(hBBill.mchtCategory,4,"0"));
//	//14[135:x5]:101-08-28
//	if (eq(hBBill.binType,"V"))
//		tt.append(commString.bbFixlen(hBBill.mchtZip,5));
//	else tt.append("00000");
//	//15[140:x3]
//	tt.append(commString.bbFixlen(hBBill.mchtState,3));
//	//16[143:x4]:bit-map
//	tt.append("8000");
//	//17[147:x4]
//	tt.append(commString.space(4));
//	//18[151:x4]
//	tmpString =colSs("rept.reason_code");
//	if (ibMast)
//		tt.append(commString.bbFixlen(tmpString,4));
//	else if (ibJcb)  
//		tt.append(commString.bbFixlen(commString.mid(tmpString,0,3),4));
//	else if (ibVisa)  
//		tt.append(commString.bbFixlen(commString.bbMid(tmpString,0,2),4));
//	else if (ibUcard)
//		tt.append(commString.bbFixlen(commString.bbMid(tmpString,0,2),4));
//	//19[155:x12]
//	tt.append(commString.space(12));
//	//20[167:x6]
//	tmpString =colSs("rept.add_date");
//	tt.append(commString.right(tmpString,4)+commString.mid(tmpString,2,2));
//	//21[173:x15]
//	tt.append(commString.bbFixlen(hBBill.mchtNo,15));
//	//22[188:x6]
//	tt.append(commString.bbFixlen(hBBill.authCode,6));
//	//23[194:x15]
//	tt.append(commString.fill("0",15));
//	//24[209:9.12]
//	tmpString =commString.lpad(String.format("%.3f",hBBill.destAmt),13,"0");
//	tt.append(commString.mid(tmpString, 0,9)+commString.right(tmpString,3));
//	//25[221:x3]
//	tt.append(commString.bbFixlen(hBBill.destCurr,3));
//	//26[224:9.8]
//	tmpString =colSs("bill.EXCHANGE_RATE");
//	tt.append(commString.rpad(tmpString,8,"0"));
//	//27[232:x6]
//	tmpString =colSs("bill.EXCHANGE_DATE");
//	tt.append(wfMmddyy(tmpString));
//	//28[238:x1]--特店代號16--
//	tmpString =hBBill.mchtNo;
//	if (ibJcb && tmpString.length()>15)
//		tt.append(commString.bbFixlen(commString.right(tmpString,1),1));
//	else tt.append(" ");
//	//29[239:x10]
//	tmpString =ctrlSeqnoConv(colSs("rept.ctrl_seqno"));
//	tt.append(commString.bbFixlen(tmpString,10,'0'));
//	//30[249:x2]:Multiple Clearing Sequence Number
//	tt.append(commString.bbFixlen(colSs("bill.MCS_NUM"),2));
//	//31[251:x15]
//	tt.append(commString.space(15));
//	//32[266:x4]
//	tmpString =colSs("rept.add_date");
//	tt.append(commString.lpad(commString.mid(tmpString,3,1),1,"0"));
//	tmpString =""+commDate.toYddd(colSs("rept.add_date"));
//	tt.append(commString.lpad(tmpString,3,"0"));
//	//33[270:x]
//	if (ibUcard)
//		tt.append("U");
//	else tt.append(hBBill.binType);
//	
//	tt.append(newLine);
//
//	this.writeTextFile(iiFileNum,tt.toString());

}
//=****************************************************************************
void updateRskReceipt(String aRowid) throws Exception {
	if (tiReptU <=0) {
		sqlCmd ="update rsk_receipt set"
				+" send_apr_flag ='N' "
				+", send_flag ='0'"
				+", send_cnt =nvl(send_cnt,0) + 1"
				+", send_date ="+commSqlStr.sysYYmd
				+", "+this.modxxxSet()
				+" where rowid =?";
		tiReptU =ppStmtCrt("ti_rept_U","");
	}
	
	ppRowId(aRowid);
	sqlExec(tiReptU);
	if (sqlNrow <=0) {
		sqlerr("update rsk_receipt error, kk=[%s]",colSs("rept.ctrl_seqno"));
		errExit(1);
	}
}

//=****************************************************************************
void selectRskChgback() throws Exception {
   sqlCmd = "select ctrl_seqno, debit_flag,"
         + " reference_no, bin_type,"
         + " chg_times,"
         + " fst_reason_code,"
         + " fst_part_mark,"
         + " fst_doc_mark, fst_msg,"
         + " fst_add_date,"
         + " fst_twd_amt, fst_amount, fst_dc_amt,"
         + " fst_reverse_mark, curr_code,"
         + " hex(rowid) as rowid"
         + " from rsk_chgback"
         + " where send_flag ='1'"
         + " and send_apr_flag ='Y'"
//         + " and bill_type like 'NC%' and film_no<>''"
         + " order by card_no, film_no, bin_type, ctrl_seqno"
   ;
	daoTable ="rsk_chgback";
	this.fetchExtend ="chgb.";
	openCursor();

	int llSendCnt=0, llNoSend=0;
	while(fetchTable()) {
		isReferNo =colSs("chgb.reference_no");
		ibDebit =colEq("chgb.debit_flag","Y");
		isCtrlSeqno =colSs("chgb.ctrl_seqno");

		totalCnt++;
		ddd("-->%s.CHGB.ctrl-refno[%s,%s]...",
				totalCnt, colSs("chgb.ctrl_seqno"),isReferNo);

		llSendCnt++;
		if (strIN(colSs("chgb.bin_type"),",V,M,J,N")==false) {
			printf("bin_type: 不是 V,M,J,N; kk[%s,%s]",isReferNo,colSs("chgb.bin_type"));
			llNoSend++;
			continue;
		}
		
		if (ibDebit) {
			selectDbbBill(isReferNo);
		}
		else {
			selectBilBill(isReferNo);
		}
		
		if (empty(hBBill.referenceNo)) {
			llNoSend++;
			printf("-->reference_no is empty");
			continue;
		}

		//-VMJ-
		bin2Type();
		//-國外交易-
		ibOversea =(!eq(hBBill.mchtCountry,"TW") && !eq(hBBill.mchtCountry,"TWN"));
		//-VM國外扣款不送-
		if (ibOversea && (ibVisa || ibMast)) {
			updateRskChgback2(colSs("chgb.rowid"));
			printf("-->國外交易不送, kk[%s-%s]",hBBill.referenceNo,hBBill.mchtCountry);
			llNoSend++;
			continue;
		}
		
		//--訊息欄位
		hBBill.messageText = colSs("chgb.fst_msg");
		
		//--tx_type
		hBBill.txType = commString.bbMid(hBBill.fiscTxCode, 0,2);
		if("05".equals(hBBill.ecsTxCode)) {
			hBBill.txType += "15";
		}	else if("06".equals(hBBill.ecsTxCode)) {
			hBBill.txType += "16";
		}
		
		if(ibVisa) {
			hBBill.srcBin = "490706";
			hBBill.destBin = commString.bbMid(hBBill.fileNo, 1,6);
		}	else if(ibMast) {
			hBBill.srcBin = "003768";
		}	else if(ibJcb) {
			hBBill.srcBin = "136201";
			hBBill.destBin = commString.bbMid(hBBill.fileNo, 1,6);
		}
		
//		//-沖銷-
//		if (colEq("chgb.fst_reverse_mark","R")) {
//			if (colEq("bill.txn_code","05"))
//				isNcccTranCode ="35"; 
//			else if (colEq("bill.txn_code","06"))
//				isNcccTranCode ="36"; 
//			else if (colEq("bill.txn_code","07"))
//				isNcccTranCode ="37"; 
//			else if (colEq("bill.txn_code","0A"))
//				isNcccTranCode ="3A"; 
//			else {
//				isNcccTranCode =colSs("bill.txn_code"); 
//			}
//		}
//		else {
//			if (colEq("bill.txn_code","05"))
//				isNcccTranCode ="15"; 
//			else if (colEq("bill.txn_code","06"))
//				isNcccTranCode ="16"; 
//			else if (colEq("bill.txn_code","07"))
//				isNcccTranCode ="17"; 
//			else if (colEq("txn_code","0A"))
//				isNcccTranCode ="1A"; 
//			else {
//				isNcccTranCode =colSs("bill.txn_code"); 
//			}			
//		}
//		if (empty(isNcccTranCode)) {
//			errmsg("rsk_chgback.NCCC Transaction code is 空白, kk[%s]",colSs("chgb.ctrl_seqno"));
//			llNoSend++;
//			continue;
//		}

//		double lmCbAmt =0;
//		lmCbAmt =colNum("chgb.fst_twd_amt");
//		if (ibMast && !colEq("chgb.curr_code","901")) {
//			lmCbAmt =colNum("chgb.fst_dc_amt");
//		}
//
//		iiTextCnt++;
//		calcTrancode(hBBill.binType, isNcccTranCode, lmCbAmt);
		dataCnt ++;
		textfileChgback1();
		textfileChgback2();
//		textfileChgback3();
		textfileChgback4();
//		textfileChgback5();
		
	   updateRskChgback(colSs("chgb.rowid"));
	}
	
	closeCursor();
	printf("-->扣款處理筆數:%s, 未傳送筆數:%s ",llSendCnt,llNoSend);
}
//=****************************************************************************
void textfileChgback1() throws Exception {
	//扣款
	String tmpString="";
	StringBuffer tt=new StringBuffer();
	DecimalFormat df = new DecimalFormat("0");
	
	//--01:X4 交易代號		
	tt.append(commString.bbFixlen(hBBill.txType,4));
	//--02:X1 固定值 1
	tt.append(commString.bbFixlen("1",1));
	
	//--03:X1 卡片類型 V/M/J
	tt.append(commString.bbFixlen(hBBill.binType,1));
	
	//--04:X11 傳送端 Source Bin
	tt.append(commString.bbFixlen(hBBill.srcBin,11));
	
	//--05:X11 接收端 Dest Bin
	tt.append(commString.bbFixlen(hBBill.destBin,11));
	
	//--06:X2 MasterCard DE61 [M]
	if(ibMast) {
		//--?
		tt.append(commString.bbFixlen("",2));
	}	else	{
		tt.append(commString.space(2));
	}
	
	//--07:X8 代理單位代號 [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.acqBusinessId,8));
	}	else	{
		tt.append(commString.space(8));
	}
	
	//--08:X19 卡片代號
	tt.append(commString.bbFixlen(hBBill.cardNo,19));
	
	//--09:X12 來源端金額
	tmpString = df.format(hBBill.sourceAmt*100);
	tt.append(commString.lpad(tmpString,12,"0"));
	
	//--10:X3 來源端幣別
	tt.append(commString.bbFixlen(hBBill.sourceCurr,3));
	
	//--11:X12 目的端金額
	tmpString = df.format(hBBill.destAmt*100);
	tt.append(commString.lpad(tmpString,12,"0"));
	
	//--12:X3 目的端幣別
	tt.append(commString.bbFixlen(hBBill.destCurr,3));
	
	//--13:X12 清算金額 [M、J]
	//--14:X3 清算幣別 [M、J]
	if(ibVisa == false) {
		tmpString = df.format(hBBill.settlAmt*100);
		tt.append(commString.lpad(tmpString,12,"0"));
		tt.append(commString.space(3));
	}	else	{

		tt.append(commString.repeat("0", 12));
		tt.append(commString.bbFixlen(hBBill.settlCurr,3));
	}
	
	//--15:X8 清算匯率 [E] 
	tt.append(commString.space(8));
	
	//--16:X20 特店代號
	tt.append(commString.bbFixlen(hBBill.mchtNo,20));
	
	//--17:X4 Mcc code
	tt.append(commString.bbFixlen(hBBill.mccCode,4));
	
	//--18:X40 特店中文名稱
	tt.append(commString.bbFixlen(hBBill.mchtChiName,40));
	
	//--19:X25 特店英文名稱
	tt.append(commString.bbFixlen(hBBill.mchtEngName,25));
	
	//--20:X13 特店所在地/城市
	tt.append(commString.bbFixlen(hBBill.mchtCity,13));
	
	//--21:X3 特店所在省分
	tt.append(commString.bbFixlen(hBBill.mchtState,3));
	
	//--22:X5 特店郵遞區號
	tt.append(commString.bbFixlen(hBBill.mchtZip,5));
	
	//--23:X3 特店國家代號
	tt.append(commString.bbFixlen(hBBill.mchtCountry,3));
	
	//--24:X2 特店條件識別碼 [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.specialCondInd,2));
	}	else	{
		tt.append(commString.space(2));
	}
	
	//--25:X15 次特店代號
	tt.append(commString.bbFixlen(hBBill.submerchantId,15));
	
	//--26:X6 購貨日期
	tt.append(commString.bbFixlen(hBBill.purchaseDate,6));
	
	//--27:X6 購貨時間
	tt.append(commString.bbFixlen(hBBill.purchaseTime,6));
	
	//--28:X6 授權碼
	tt.append(commString.bbFixlen(hBBill.authCode,6));
	
	//--29:X1 電話/郵件識別碼 [V、J]
	if(ibMast) {
		tt.append(commString.space(1));
	} else {
		tt.append(commString.bbFixlen(hBBill.ecInd,1));
	}
		
	//--30:X1 MasterCard DE61 Indicator2 [M]
	if(ibMast) {
		tt.append(commString.space(1));
	} else {
		tt.append(commString.space(1));
	}
	
	//--31:X1 自助端末機識別碼
	tt.append(commString.bbFixlen(hBBill.catInd,1));
	
	//--32:X15 端末機代號
	tt.append(commString.bbFixlen(hBBill.terminalId,15));
	
	//--33:X6 端末機上傳請款資料日期 民國年
	tt.append(commString.bbFixlen(hBBill.teSendDate,6));
	
	//--34:X4 資料批次號碼
	tt.append(commString.bbFixlen(hBBill.teBatchNo,4));
	
	//--35:X6 端末機上傳序號
	tt.append(commString.bbFixlen(hBBill.teTxNum,6));
	
	//--36:X1 端末機性能POS
	tt.append(commString.bbFixlen(hBBill.posTeCap,1));
	
	//--37:X2 POS Entry Mode 
	tt.append(commString.bbFixlen(hBBill.posEntryMode,2));
	
	//--38:X4 資料處理日期
	tt.append(commString.bbFixlen(hBBill.processDay,4));
	
	//--39:X23 微縮影代號
	tt.append(commString.bbFixlen(hBBill.fileNo,23));
	
	//--40:X1 使用碼
	tt.append(commString.bbFixlen(hBBill.usageCode,1));
	
	//--41:X4 沖正駁回理由碼
	tt.append(commString.bbFixlen(hBBill.reasonCode,4));
	
	//--42:X10 沖正參考號碼
	tt.append(commString.bbFixlen(hBBill.cbRefNo,10));
	
	//--43:X1 特殊沖正識別碼
	tt.append(commString.bbFixlen(hBBill.vcrfsInd,1));
	
	//--44:X1 附寄文件識別碼
	tt.append(commString.bbFixlen(hBBill.docInd,1));
	
	//--45:X3 Service Code
	tt.append(commString.bbFixlen(hBBill.serviceCode,3));
	
	//--46:X1 UCAF[M]
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.ucaf,1));
	} else {
		tt.append(commString.space(1));
	}
	
	//--47:X45 訂單編號/銷帳編號/繳費平台交易資訊/繳稅年度及交易資訊 
	tt.append(commString.bbFixlen(hBBill.reimbInfo,45));
	
	//--48:X2 繳費平台交易種類代碼/繳稅種類代碼
	tt.append(commString.bbFixlen(hBBill.reimbCode,2));
	
	//--49:X1 交易處理屬性
	tt.append(commString.bbFixlen(hBBill.reimbAttr,1));
	
	//--50:X1 清算識別碼
	tt.append(commString.bbFixlen(hBBill.settlFlag,1));
	
	//--51:X4 原沖正交易訊息理由碼 [M]
	if(ibMast) {
		tt.append(commString.bbFixlen("0000",4));
	}	else	{
		tt.append(commString.space(4));
	}
	
	//--52:X2 保留欄位
	tt.append(commString.space(2));
	
	//--53:X1 支付型態
	tt.append(commString.bbFixlen(hBBill.paymentType,1));
	
	//--54:X1 跨境電子支付平台代碼
	tt.append(commString.space(1));
	
	//--55:X1 跨境電子支付平台交易狀態 
	tt.append(commString.space(1));
	
	//--56:X10 跨境電子支付申報性質別
	tt.append(commString.space(10));
	
	//--57:X2 帳單類別 
	tt.append(commString.bbFixlen(hBBill.ncccBillType,2));
	
	//--58:X2 特店類型
	tt.append(commString.bbFixlen(hBBill.ncccMchtType,2));
	
	//--59:X5 繳費項目/非促銷商品金額
	tt.append(commString.bbFixlen(hBBill.ncccItem,5));
	
	//--60:X16 MerchantPAN/信用卡被掃TPAN
	tt.append(commString.space(16));
	
	//--61:X1 電子化繳費稅註記
	tt.append(commString.space(1));
	
	//--62:X11 保留欄位
	tt.append(commString.space(11));
	
	//--63:X2 換行符號
	tt.append(newLine);
	
	writeTextFile(iiFileNum,tt.toString());	
	dataRecordCnt ++;
	dataAmt += hBBill.sourceAmt ;
}
void textfileChgback2() throws Exception {
	//扣款 Record2
	String tmpString="";
	StringBuffer tt=new StringBuffer();
	DecimalFormat df = new DecimalFormat("0");
	
	//--01:X4 交易代號	
	tt.append(commString.bbFixlen(hBBill.txType,4));
	//--02:X1 固定值 2
	tt.append(commString.bbFixlen("2",1));
	
	//--03:X1 卡片類型 V/M/J
	tt.append(commString.bbFixlen(hBBill.binType,1));
	
	//--04:X70 訊息 Message Text
	tt.append(commString.bbFixlen(hBBill.messageText,70));
	
	//--05:X12 Crypto Amount (9F02)
	tmpString = df.format(hBBill.acCryptoAmt*100);
	tt.append(commString.lpad(tmpString,12,"0"));
	
	//--06:X3 交易幣別碼
	tt.append(commString.bbFixlen(hBBill.sourceCurr,3));
	
	//--07:X2 Transaction Type (9C)
	tt.append(commString.bbFixlen(hBBill.acTxType,2));
	
	//--08:X3 PAN Sequence Number (5F34)
	tt.append(commString.bbFixlen(hBBill.panSeqNum,3));
	
	//--09:X6 Transaction Date (9A)
	tt.append(commString.bbFixlen(hBBill.teTxDate,6));
	
	//--10:X6 Terminal Capabilities profile (9F33) 
	tt.append(commString.bbFixlen(hBBill.teProfile,6));
	
	//--11:X3 Terminal Country Code (9F1A)
	tt.append(commString.bbFixlen(hBBill.teCountry,3));
	
	//--12:X8 Interface Device Serial Number(9F1E)  interface_dev_num
	tt.append(commString.bbFixlen(hBBill.interfaceDevNum,8));
	
	//--13:X8 Unpredictable Number (9F37)
	tt.append(commString.bbFixlen(hBBill.acUnpredNum,8));
	
	//--14:X4 Application Transaction Number (9F36)
	tt.append(commString.bbFixlen(hBBill.apTxNum,4));
	
	//--15:X4 Application Interchange Profile (82)
	tt.append(commString.bbFixlen(hBBill.apProfile,4));
	
	//--16:X16 Application Cryptogram(9F26)
	tt.append(commString.bbFixlen(hBBill.ac,16));
	
	//--17:X64 Issuer Application Data (IAD) (9F10)
	tt.append(commString.bbFixlen(hBBill.iad,64));
	
	//--18:X10 Terminal Verification Result(95)
	tt.append(commString.bbFixlen(hBBill.tvr,10));
	
	//--19:X6 Cardholder Verification Method (CVM) Result(9F34)
	tt.append(commString.bbFixlen(hBBill.cvmResult,6));
	
	//--20:X2 Cryptogram Information Data(9F27)
	tt.append(commString.bbFixlen(hBBill.cryptogramInfo,2));
	
	//--21:X8 Application Expiration Date(5F24)
	if(ibJcb) {
		tt.append(commString.bbFixlen(hBBill.apExpireDate,8));
	}	else	{
		tt.append(commString.space(8));
	}
	
	//--22:X2 AUTH Resp. Code(8A)
	tt.append(commString.bbFixlen(hBBill.authRespCode,2));
	
	//--23:X10 Post Issuance Result
	tt.append(commString.bbFixlen(hBBill.postIssueResult,10));
	
	//--24:X1 Chip Condition Code
	tt.append(commString.bbFixlen(hBBill.chipCondCode,1));
	
	//--25:X1 Terminal Entry Capability
	tt.append(commString.bbFixlen(hBBill.teEntryCap,1));
	
	//--26:X12 Card Verification Result
	tt.append(commString.bbFixlen(hBBill.cardVerifyResult,12));
	
	//--27:X12 Form Factor Indicator(FFI)(9F6E)
	tt.append(commString.bbFixlen(hBBill.ffi,12));
	
	//--28:X32 Dedicated File Name
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.emvIad2,32));
	}	else	{
		tt.append(commString.space(32));
	}
	
	
	//--29:X137 保留欄位
	tt.append(commString.space(137));
	
	//--30:X2 換行符號
	tt.append(newLine);
	
	writeTextFile(iiFileNum,tt.toString());		
	dataRecordCnt++;
}

void textfileChgback3() throws Exception {
	//扣款 Record3
	String tmpString="";
	StringBuffer tt=new StringBuffer();
	DecimalFormat df = new DecimalFormat("0");
	
	//--01:X4 交易代號	
	tt.append(commString.bbFixlen(hBBill.txType,4));
	//--02:X1 固定值 3
	tt.append(commString.bbFixlen("3",1));
	
	//--03:X1 卡片類型
	tt.append(commString.bbFixlen(hBBill.binType,1));
	
	//--04:X14 共同供應契約收單交易專用：立約商統一編號
	tt.append(commString.bbFixlen(hBBill.procureUniform,14));	
	
	//--05:X20 共同供應契約收單交易專用：立約商名稱
	tt.append(commString.bbFixlen(hBBill.procureName,20));
	
	//--06:X15 共同供應契約收單交易專用：傳票號碼
	tt.append(commString.bbFixlen(hBBill.procureVoucherNo,15));
	
	//--07:X10 共同供應契約收單交易專用：發票號碼
	tt.append(commString.bbFixlen(hBBill.procureReceiptNo,10));
	
	//--08:X3 共同供應契約收單交易專用：原始幣別
	tt.append(commString.bbFixlen(hBBill.procureOrigCurr,3));
	
	//--09:X16 共同供應契約收單交易專用：原始金額
	tmpString = df.format(hBBill.procureOrigAmt*10000);
	tt.append(commString.lpad(tmpString,16,"0"));	
	
	//--10:X50 共同供應契約收單交易專用：工作計劃或下訂機關購案編號
	tt.append(commString.bbFixlen(hBBill.procurePlan,50));
	
	//--11:X30 共同供應契約收單交易專用：一級用途別
	tt.append(commString.bbFixlen(hBBill.procureLevel1,30));
	
	//--12:X30 共同供應契約收單交易專用：二級用途別
	tt.append(commString.bbFixlen(hBBill.procureLevel2,30));
	
	//--13:X20 共同供應契約收單交易專用：交易編號
	tt.append(commString.bbFixlen(hBBill.procureTxNum,20));
	
	//--14:X10 共同供應契約收單交易專用：銀行手續費	
	tt.append(commString.lpad(commString.int2Str(hBBill.procureBankFee),10,"0"));	
	
	//--15:X10 共同供應契約收單交易專用：中華電信手續費
	tt.append(commString.lpad(commString.int2Str(hBBill.procureChtFee),10,"0"));	
	
	//--16:X10 共同供應契約收單交易專用：撥付金額
	tt.append(commString.lpad(commString.int2Str(hBBill.procurePayAmt),10,"0"));	
	
	//--17:X2 共同供應契約收單交易專用：訂單支付期數
	tt.append(commString.lpad(commString.int2Str(hBBill.procureTotTerm),2,"0"));	
	
	//--18:X1 分期付款收單交易專用：交易類型
	tt.append(commString.bbFixlen(hBBill.installType,1));
	
	//--19:X2 分期付款收單交易專用：分期期數
	tt.append(commString.lpad(commString.int2Str(hBBill.installTotTerm),10,"0"));	
	
	//--20:X10 分期付款收單交易專用：首期金額
	tt.append(commString.lpad(commString.int2Str(hBBill.installFirstAmt),10,"0"));	
	
	//--21:X10 分期付款收單交易專用：每期金額
	tt.append(commString.lpad(commString.int2Str(hBBill.installPerAmt),10,"0"));	
	
	//--22:X10 分期付款收單交易專用：末期金額
	tt.append(commString.lpad(commString.int2Str(hBBill.installLastAmt),10,"0"));	
	
	//--23:X10 分期付款收單交易專用：分期管理費
	tt.append(commString.lpad(commString.int2Str(hBBill.installCharges),10,"0"));	
	
	//--24:X6 分期付款收單交易專用：櫃員機台代碼
	tt.append(commString.bbFixlen(hBBill.installAtmNo,6));
	
	//--25:X6 分期付款收單交易專用：分期付款計劃專案代號
	tt.append(commString.bbFixlen(hBBill.installProjNo,6));
	
	//--26:X8 分期付款收單交易專用：來源供應商代碼
	tt.append(commString.bbFixlen(hBBill.installSupplyNo,8));
	
	//--27:X8 紅利折抵交易專用：紅利折抵點數
	tt.append(commString.bbFixlen(hBBill.bonusTransBp,8));
	
	//--28:X12 紅利折抵交易專用：紅利折抵後之支付金額
	tmpString = df.format(hBBill.procureOrigAmt*100);
	tt.append(commString.lpad(tmpString,12,"0"));	
	
	//--29:X125 保留欄位
	tt.append(commString.space(125));
	
	//--30:X2 換行符號
	tt.append(newLine);
	
	writeTextFile(iiFileNum,tt.toString());		
	dataRecordCnt++;

}
void textfileChgback4() throws Exception {
	//扣款 Record4
	//-----------------------------------
	String tmpString="";
	StringBuffer tt=new StringBuffer();
	DecimalFormat df = new DecimalFormat("0");
	
	//--01:X4 交易代號	
	tt.append(commString.bbFixlen(hBBill.txType,4));
	//--02:X1 固定值 4
	tt.append(commString.bbFixlen("4",1));
	
	//--03:X1 卡片類型
	tt.append(commString.bbFixlen(hBBill.binType,1));
	
	//--04:X2 Ingerchange Rate Designator [M]	
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.ird,2));
	}	else	{
		tt.append(commString.space(2));
	}
			
	//--05:X1 購買預付卡識別碼Prepaid Card Indicator [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.prepaidCardInd,1));
	}	else	{
		tt.append(commString.space(1));
	}

	//--06:X1 黑名單識別碼Card Warning Bulletin Indicator [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.cwbInd,1));
	}	else	{
		tt.append(commString.space(1));
	}		
	
	//--07:X4 Fraud Notification Date [M]
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.fraudNotifyDate,4));
	} 	else	{
		tt.append(commString.space(4));
	}	
	
	//--08:X2 Fraud Notification Service ChargebackCounter [M]
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.fraudCbCnt,2));
	}	else {
		tt.append(commString.space(2));
	}
	
	//--09:X1 特約商店限額識別碼Floor Limit Indicator [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.floorLimitInd,1));
	}	else {
		tt.append(commString.space(1));
	}
		
	//--10:X4 匯率轉換日期 Currency Conversion Date[V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.processDay,4));
	}	else {
		tt.append(commString.space(4));
	}

	//--11:X2 Reserved Field 保留欄位
	tt.append(commString.space(2));
	
	//--12:X15 交易識別碼Transaction ID [V][M]
	if(ibJcb) {
		tt.append(commString.space(15));
	}	else	{
		tt.append(commString.bbFixlen(hBBill.transactionId,15));
	}	
	
	//--13:X2 卡片級別識別碼 [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.cardProductId,2));
	}	else	{
		tt.append(commString.space(2));
	}

	//--14:X4 授權欄位驗證碼 [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.authValidCode,4));
	}	else	{
		tt.append(commString.space(4));
	}			

	//--15:X2 Multiple Clearing Sequence Number [V] 
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.multiClearingSeq,2));
	}	else	{
		tt.append(commString.space(2));
	}	

	//--16:X1 QPS/PayPass Chargeback Eligibility Indicator [M] qps_cb_ind
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.qpsCbInd,1));
	}	else	{
		tt.append(commString.space(1));
	}		

	//--17:X2 Additional Amount，Account Type [M] [J]
	if(ibVisa) {
		tt.append(commString.space(2));
	}	else	{
		tt.append(commString.bbFixlen(hBBill.addnAcctType,2));
	}	
	
	//--18:X2 Additional Amount Amount Type [M] [J]
	//--18:X2 DCC/Cashback/Surcharge 辨識碼 [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.dccInd,2));
	}	else	{
		tt.append(commString.space(2));
	}	
	
	//--19:X3 Additional Amount，Currency Code [M] [J]
	if(ibVisa) {
		tt.append(commString.space(3));
	}	else	{
		tt.append(commString.bbFixlen(hBBill.addnAmtCurr,3));
	}

	//--20:X1 Additional Amount，Amount Sign [M] [J]
	//--20:X1 Surcharge Credit/Debit Indicator [V]
	if(ibVisa) {
		tt.append(commString.space(1));
	}	else	{
		tt.append(commString.bbFixlen(hBBill.addnAmtSign,1));
	}

	//--21:X12 Additional Amount，Amount [M] [J]
	//--21:X12 Surcharge Amount/Cashback Amount  [V]	
	if(ibVisa) {
		tt.append(commString.space(12));
	}	else	{
		tmpString = df.format(hBBill.addnAmt*100);
		tt.append(commString.lpad(tmpString,12,"0"));
//		tt.append(commString.lpad(commString(hBBill.addnAmt),12,"0"));
//		tt.append(commString.bbFixlen(hBBill.addnAmt,12));
	}

	//--22:X8 Surcharge Amount in cardholder billing currency
	if(ibVisa) {
		tt.append(commString.space(8));
	}	else	{
		tt.append(commString.space(8));
	}

	//--23:X2 Token Assurance Level[V] [M]
	if(ibJcb) {
		tt.append(commString.space(2));
	}	else	{
		tt.append(commString.bbFixlen(hBBill.assureLevel,2));
	}

	//--24:X11 Token Requestor ID [V] [M]s
	if(ibJcb) {
		tt.append(commString.space(11));
	}	else	{
		tt.append(commString.bbFixlen(hBBill.tokenRequestorId,11));
	}
	//--25:X11 Payment Facilitator ID [M]
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.paymentFaId,11));
	}	else	{
		tt.append(commString.space(11));
	}	
	
	//--26:X1 DE22(Point of Service Data Code)：Card Data Input Capability [M] [J]
	//--27:X1 DE22(Point of Service Data Code)：Cardholder Authentication Capability [M] [J]
	//--28:X1 DE22(Point of Service Data Code)：Card Capture Capability [M] [J]
	//--29:X1 DE22(Point of Service Data Code)：Terminal Operating Environment [M] [J]
	//--30:X1 DE22(Point of Service Data Code)：Cardholder Present Data [M] [J]
	//--31:X1 DE22(Point of Service Data Code)：Card Present Data [M] [J]
	//--32:X1 DE22(Point of Service Data Code)：Input Mode [M] [J]
	//--33:X1 DE22(Point of Service Data Code)：Cardholder Authentication Method [M] [J]
	//--34:X1 DE22(Point of Service Data Code)：Cardholder Authentication Entity [M] [J]
	//--35:X1 DE22(Point of Service Data Code)：Card Data Output Capability [M] [J]
	//--36:X1 DE22(Point of Service Data Code)：Terminal Data Output Capability [M] [J]
	//--37:X1 DE22(Point of Service Data Code)：PIN Capture Capability [M] [J]
	if(ibVisa) {
		tt.append(commString.space(12));		
	}	else	{
		tt.append(commString.bbFixlen(hBBill.cardInCap,1));
		tt.append(commString.bbFixlen(hBBill.chAuthCap,1));
		tt.append(commString.bbFixlen(hBBill.captureCap,1));
		tt.append(commString.bbFixlen(hBBill.teOpEnv,1));
		tt.append(commString.bbFixlen(hBBill.chData,1));
		tt.append(commString.bbFixlen(hBBill.cardData,1));
		tt.append(commString.bbFixlen(hBBill.inputMode,1));
		tt.append(commString.bbFixlen(hBBill.chAuthMethod,1));
		tt.append(commString.bbFixlen(hBBill.chAuthEntity,1));
		tt.append(commString.bbFixlen(hBBill.cardOutCap,1));
		tt.append(commString.bbFixlen(hBBill.teOutCap,1));
		tt.append(commString.bbFixlen(hBBill.pinCaptureCap,1));
	}

	//--38:X1 PCAS 識別碼Positive Cardholder Authorization Service Indicator [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.pcasInd,1));
	}	else	{
		tt.append(commString.space(1));
	}	
	
	//--39:X5 VCIND(VISA Checkout Indicator) [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.vcind,5));
	}	else	{
		tt.append(commString.space(5));
	}

	//--40:X12 原始交易金額 [J]
	if(ibJcb) {
		tt.append(commString.lpad(commString.int2Str(hBBill.origTxAmt),12,"0"));	
	}	else	{
		tt.append(commString.space(12));
	}

	//--41:X19 PAN TOKEN/Account Number [V] [M]
	if(ibJcb) {
		tt.append(commString.space(19));
	}	else	{
		tt.append(commString.bbFixlen(hBBill.panToken,19));
	}

	//--42:X1 Mastercard Cross-Border Indicator [M]
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.mCrossInd,1));
	}	else	{
		tt.append(commString.space(1));
	}

	//--43:X1 Mastercard Currency Indicator [M]  m_curr_ind
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.mCurrInd,1));
	}	else	{
		tt.append(commString.space(1));
	}	
	
	//--44:X12 Mastercard Currency Conversion Assessment(CCA) [M]
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.mCca,12));
	}	else	{
		tt.append(commString.space(12));
	}	

	//--45:X8 保留欄位Reserved Field
	tt.append(commString.space(8));
		
	//--46:X3 Fee Program Indicator(FPI)
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.fpi,3));
	}	else	{
		tt.append(commString.space(3));
	}

	//--47:X8 Source Currency to Base Currency Exchange Rate [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.scToBcRate,8));
	}	else	{
		tt.append(commString.space(8));
	}

	//--48:X8 Base Currency to Destination Currency Exchange Rate [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.bcToDcRate,8));
	}	else	{
		tt.append(commString.space(8));
	}	
	
	//--49:X1 Charge Indicator [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.chargeInd,1));
	}	else	{
		tt.append(commString.space(1));
	}	

	//--50:X2 保留欄位Reserved Field [M]
	tt.append(commString.space(2));

	//--51:X2 Account Number Type
	if(ibMast) {
		tt.append(commString.bbFixlen(hBBill.acctNumType,2));
	}	else	{
		tt.append(commString.space(2));
	}	

	//--52:X29 Payment  Account Reference (PAR) [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.par,29));
	}	else	{
		tt.append(commString.space(29));
	}	

	//--53:X10 VROL Case Number [V]
	if(ibVisa) {
		tt.append(commString.bbFixlen(hBBill.vrolCaseNum,10));
	}	else	{
		tt.append(commString.space(10));
	}	

	//--54:X1 Acceptance Data [M]
	if(ibMast) {
		tt.append(commString.space(1));
	}	else	{
		tt.append(commString.space(1));
	}

	//--55:X6 Clearing Currency Conversion Identifier—Currency Conversion Date [M]
	if(ibMast) {
		tt.append(commString.space(6));
	}	else	{
		tt.append(commString.space(6));
	}

	//--56:X1 Clearing Currency Conversion Identifier— Currency Conversion Indicator [M]
	if(ibMast) {
		tt.append(commString.space(1));
	}	else	{
		tt.append(commString.space(1));
	}
	
	//--57:X1 Business Activity—Digital Wallet Interchange Override Indicator[M]
	if(ibMast) {
		tt.append(commString.space(1));
	}	else	{
		tt.append(commString.space(1));
	}
	
	//--58:X1 VISA Authorization Source Code[V]
	if(ibVisa) {
		tt.append(commString.space(1));
	}	else	{
		tt.append(commString.space(1));
	}
	
	//--59:X1 VISA Persistent FX Applied Indicator [V]
	if(ibVisa) {
		tt.append(commString.space(1));
	}	else	{
		tt.append(commString.space(1));
	}
	
	//--60:X5 VISA Rate Table ID [V]
	if(ibVisa) {
		tt.append(commString.space(5));
	}	else	{
		tt.append(commString.space(5));
	}

	//--61:X11 Resubmission Code [M]
	if(ibMast) {
		tt.append(commString.space(11));
	}	else	{
		tt.append(commString.space(11));
	}
	
	//--62:X4 Mastercard CIT/MIT 註記 [M]
	if(ibMast) {
		tt.append(commString.space(4));
	}	else	{
		tt.append(commString.space(4));
	}
	
	//--63:X1 VISA Additional Token Response Information [V]
	if(ibVisa) {
		tt.append(commString.space(1));
	}	else	{
		tt.append(commString.space(1));
	}
	
	//--64:X183 保留欄位 Reserved Field
	tt.append(commString.space(183));
	
	//--65:X2 換行符號
	tt.append(newLine);

	writeTextFile(iiFileNum,tt.toString());		
	dataRecordCnt++;
}
void textfileChgback5() throws Exception {
	//扣款 Record5
	String tmpString="";
	StringBuffer tt=new StringBuffer();
	//--01:X4 交易代號	
	tt.append(commString.bbFixlen(hBBill.txType,4));
	//--02:X1 固定值 5
	tt.append(commString.bbFixlen("5",1));
	
	//--03:X1 卡片類型
	tt.append(commString.bbFixlen(hBBill.binType,1));
	
	//--04:X84 Ancillary Service Charges [M]
	//--05:X82 Custom Identifier [M]
	//--06:X36 Directory Server Transaction ID [M]
	//--07:X32 Accountholder Authentication Value(AAV) [M]
	//--08:X1 Program Protocol [M]
	if(ibMast) {
		tt.append(commString.space(84));
		tt.append(commString.space(82));
		tt.append(commString.space(36));
		tt.append(commString.space(32));
		tt.append(commString.space(1));
	}	else	{
		tt.append(commString.space(235));
	}
	
	//--09:X207 保留欄位
	tt.append(commString.space(207));
	
	//--10:X2 換行符號
	tt.append(newLine);

	writeTextFile(iiFileNum,tt.toString());	
	dataRecordCnt++;
}
//=****************************************************************************
void updateRskChgback(String aRowid) throws Exception {
	if (tiChgbU <=0) {
		sqlCmd ="update rsk_chgback set"
				+" send_apr_flag ='N' "
				+", send_flag ='0'"
				+", send_cnt =nvl(send_cnt,0) + 1"
				+", send_date ="+commSqlStr.sysYYmd
				+", fst_send_cnt =fst_send_cnt + 1"
				+", fst_send_date ="+commSqlStr.sysYYmd
				+", "+this.modxxxSet()
				+" where rowid =?";
		tiChgbU =ppStmtCrt("ti_chgb_U","");
	}
	
	ppRowId(aRowid);
//	ddd_sql(ti_chgb_U);
	sqlExec(tiChgbU);
	if (sqlNrow <=0) {
		sqlerr("update rsk_chgback error, kk=[%s]",colSs("chgb.ctrl_seqno"));
		errExit(1);
	}
}

private int tiChgbU2=-1;
void updateRskChgback2(String aRowid) throws Exception {
	//-VM國外扣款不送--
	if (tiChgbU2 <=0) {
		sqlCmd ="update rsk_chgback set"
				+" send_apr_flag ='N' "
				+", send_flag ='0'"
				+", "+this.modxxxSet()
				+" where rowid =?";
		tiChgbU2 =ppStmtCrt("ti_chgb_U2","");
	}

	ppRowId(aRowid);
	sqlExec(tiChgbU2);
	if (sqlNrow <=0) {
		sqlerr("update rsk_chgback-2 error, kk=[%s]",colSs("chgb.ctrl_seqno"));
		errExit(1);
	}
}

//=****************************************************************************
void textfileHeading() throws Exception {
	String allStr="" ;
	
	allStr= "H" +
			"00600000" +
			"95000000" +
			"PROD" +
			sysDate +
			"ICM51QBD" +
			isFileNo +
//			nn +
			commString.bbFixlen("", 409) +
			newLine;
			
	if (writeTextFile(iiFileNum,allStr)==false) {
		errmsg("媒體表頭產生失敗");
		errExit(1);
	}
	
}

//=****************************************************************************
String cntGet(String cntNum) {
	double liCnt=0;
//	String fmt_cnt="000000";
	
	try {
		liCnt = cntMap.get(cntNum).doubleValue();
	}
	catch (Exception ex) {
		liCnt =0;
	}
	return commString.fixlenNum(String.format("%.0f",liCnt),6);
}
String amtGet(String amt) {
	double num=0;
//	String fmt_amt="0000000000000.00";
	
	try {
		num = amtMap.get(amt).doubleValue();
	}
	catch (Exception ex) {
		num =0;
	}
	String numRtn=commString.fixlenNum(String.format("%.2f",num),16);
	return numRtn.replace(".","");
}
void textfileFooter1() throws Exception {
	String tmpString="";
	StringBuffer tt=new StringBuffer();
	DecimalFormat df = new DecimalFormat("0");
	//--01:X1 Record 識別碼 固定為 T	
	tt.append(commString.bbFixlen("T",1));
	//--02:X8 檔案交易總筆數
	tt.append(commString.lpad(commString.int2Str(dataCnt),8,"0"));
	
	//--03:X8 檔案Record總筆數
	tt.append(commString.lpad(commString.int2Str(dataRecordCnt),8,"0"));
	
	//--04:X15 檔案交易總來源金額
	tmpString = df.format(dataAmt*100);
	tt.append(commString.lpad(tmpString,15,"0"));
	
	//--05:X416 保留欄位
	tt.append(commString.space(416));
	
	//--06:X2 換行符號
	tt.append(newLine);
	writeTextFile(iiFileNum,tt.toString());
	
}
void textfileFooter2() throws Exception {
	String col="";
	StringBuffer tt=new StringBuffer();
	
	tt.append("FT2");
	//[1]36,37,10,20,51,[6]52,71,72,0A,1A,[11]2A,3A
	col="36";
	tt.append(cntGet(col));
	tt.append(amtGet(col));
	col="37";
	tt.append(cntGet(col));
	tt.append(amtGet(col));
	col="10";
	tt.append(cntGet(col));
	tt.append(amtGet(col));
	col="20";
	tt.append(cntGet(col));
	tt.append(amtGet(col));
	col="51";
	tt.append(cntGet(col));
//	tt.append(amt_get(col));
//	tt.append(ss.replace(".", ""));
	col="52";
	tt.append(cntGet(col));
//	tt.append(amt_get(col));
//	tt.append(ss.replace(".", ""));
	col="71";
	tt.append(cntGet(col));
//	tt.append(amt_get(col));
//	tt.append(ss.replace(".", ""));
	col="72";
	tt.append(cntGet(col));
//	tt.append(amt_get(col));
//	tt.append(ss.replace(".", ""));
	col="0A";
	tt.append(cntGet(col));
	tt.append(amtGet(col));
	col="1A";
	tt.append(cntGet(col));
	tt.append(amtGet(col));
	col="2A";
	tt.append(cntGet(col));
	tt.append(amtGet(col));
	col="3A";
	tt.append(cntGet(col));
	tt.append(amtGet(col));
	
	tt.append(commString.space(75));
	tt.append(newLine);
	
	if (this.writeTextFile(iiFileNum,tt.toString())==false) {
		errmsg("FOOTER-2: 寫入媒體檔失敗");
		errExit(1);
	}
}

//=****************************************************************************
boolean fileRename(String filename, String newFilename) {
   File file = new File(filename);
   if (empty(newFilename)) {
   	if (file.exists()) {
   		file.delete();
   	}
   	return true;
   }
   
   File file2 = new File(newFilename);
   printf("-->檔案  : [%s] 移至  : [%s]", filename, newFilename);

   if (file2.exists()) {
      printf("-->file.rename: [%s] 檔案已存在",file2.getName());
      return false;
   }

   file2.getParentFile().mkdirs();
   if (file.renameTo(file2))
      return true;
   
   printf("-->file.rename fail. [%s]>>[%s]",file.getName(),file2.getName());
   return false;
}
//==***************************************************************************
void selectCrdCard() {
	hCardNewEndDate="";
	try {
		if (tiCard <=0) {
			sqlCmd ="select new_end_date"
					+" from crd_card"
					+" where card_no =?"
					+" union select new_end_date"
					+" from dbc_card"
					+" where card_no =?"
					+" union  select new_end_date"
					+" from hce_card"
					+" where v_card_no =?"
					;
			tiCard =ppStmtCrt("ti_card","");
		}
		if (noEmpty(hBBill.vCardNo)) {
			ppp(1,"");
			ppp("");
			ppp(hBBill.vCardNo);
		}
		else {
			ppp(1,hBBill.cardNo);
			ppp(hBBill.cardNo);
			ppp("");
		}
		daoTid ="card.";
		sqlSelect(tiCard);
		if (sqlNrow >0) {
			hCardNewEndDate =colSs("card.new_end_date");
		}
	}
	catch(Exception ex) {;}
}
void bin2Type() {
	ibVisa =eq(hBBill.binType,"V");
	ibMast =eq(hBBill.binType,"M");
	ibJcb =eq(hBBill.binType,"J");
	ibUcard =eq(hBBill.binType,"N");
}
boolean currIsTW(String aCurr) {
	if (empty(aCurr))
		return true;
	else if (strIN(aCurr,",901,TWD"))
		return true;
	
	return false;
}
String wfMmddyy(String string1) {
	if (string1.length()<=4) {
		return commString.bbFixlen(string1,6);
	}
	
	if (string1.length()>=8)
		return commString.mid(string1,4,4)+commString.mid(string1,2,2);
	else if (string1.length()==7)
		return commString.right(string1,4)+commString.mid(string1,1,2);
	else if (string1.length()==6)
		return commString.right(string1,4)+commString.mid(string1,0,2);

	return commString.bbFixlen(commString.right(string1,4)+commString.left(string1,string1.length()-4),6);
}
String wfMmddyyyy(String string1) {
	String dateString=commString.rpad(string1,8);
	return commString.bbFixlen(commString.right(dateString,4)+commString.left(dateString,4),8);
}
String ctrlSeqnoConv(String string1) {
	if (commString.isNumber(string1) || empty(string1)) {
		return string1;
//		return commString.lpad(s1,10,"0");
	}
	String c1 =commString.left(string1,1);
	if (commString.ssIn(c1,",V,M,J,U")) {
		return commString.mid(string1,1);
//		return commString.lpad(commString.mid(s1,1),10,"0");
	}
	return string1;
}
}
