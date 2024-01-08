/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/03/23  V1.00.00    Ryan     program initial                            *
 *  112/04/14  V1.00.01    Ryan     INSERT ACT_PAY_ERROR 改為 ACT_PAY_ERROR2   *
 *  112/06/29  V1.00.02    Ryan     payment_type AUT6 ==> COU4                 *
 *  112/09/28  V1.00.03    Ryan     batch_no 每批+1                                                                                 *
 *  112/11/06  V1.00.04    Ryan     ACT_PAY_ERROR2 -->ACT_PAY_ERROR                                                                                 *
 ******************************************************************************/

package Act;

import java.text.Normalizer;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

/*每天接收全國繳費網繳款作業*/
public class ActA310 extends AccessDAO {
    private final String PROGNAME = "每天接收全國繳費網繳款作業  112/11/06 V1.00.04";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommString comStr = new CommString();
    String fileName = "ECSEBILL_yyyymmddnn.TXT";
    String prgmId = "ActA310";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hLastBusinessDate = "";

    String hAcctType = "";
    String hIdPSeqno = "";
    String hCorpPSeqno = "";
    String hPSeqno = "";
    String hAcnoPSeqno = "";
    String hApdlSerialNo = "";
    String batchNo = "";
    int totalCnt = 0;
    String errorFlag = ""; 
    int maxSerialNo = 1;
    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : ActA310 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

//            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hBusiBusinessDate = "";
            if ((args.length == 1) && (args[0].length() == 8)) {
                String sGArgs0 = "";
                sGArgs0 = args[0];
                sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
                hBusiBusinessDate = sGArgs0;
            }
            selectPtrBusinday();
            
        	showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "]");
            getBatchNo();
            checkFopen();
            selectInsertDetail();
           
            showLogMessage("I", "", "");
            showLogMessage("I", "", String.format("程式處理筆數 [%d]", totalCnt));

            // ==============================================
            // 固定要做的
//            comcr.hCallErrorDesc = "程式執行結束";
//            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void checkFopen() throws Exception {

    	String inputFilepath = String.format("/crdataupload/");
    	String inputFileName = "" ,renameFile ="";
    	List<String> listOfFiles = comc.listFS(inputFilepath, "", "");
    	if (listOfFiles.size() > 0)
			for (String file : listOfFiles) {
				if(file.indexOf("ECSEBILL_")<0) {
					continue;
				}
				if(file.length()<23) {
					continue;
				}
				if(!comStr.mid(file,9,8).equals(hBusiBusinessDate) && !comStr.mid(file,9,8).equals(hLastBusinessDate))  
				{
					continue;
				}
				
				
				inputFileName = Normalizer.normalize(String.format("/crdataupload/%s", file), java.text.Normalizer.Form.NFKD);
				renameFile = String.format("%s/media/act/backup/%s", comc.getECSHOME(), file);
				renameFile = Normalizer.normalize(renameFile, java.text.Normalizer.Form.NFKD);
				readFile(inputFileName);
		        renameFile(inputFileName,renameFile);
			}
    }
    
    void renameFile(String inputFileName,String renameFile) throws Exception {
    	String tmpstr1 = inputFileName;
    	String tmpstr2 = String.format("%s.%s", renameFile,sysDate+sysTime);

//    	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
    	if (comc.fileMove(tmpstr1, tmpstr2) == false) {
    		showLogMessage("I", "", "檔案 [" + inputFileName + "] 備份失敗!");
    		return;
    	}
    	showLogMessage("I", "", "檔案 [" + inputFileName + "] 已備份至 [" + tmpstr2 + "]");
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,?) as h_business_date ";
        sqlCmd += ", to_char(to_date(decode(cast(? as varchar(8)),'',business_date,?),'YYYYMMDD') -1 day,'YYYYMMDD') as last_business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1,hBusiBusinessDate);
        setString(2,hBusiBusinessDate);
        setString(3,hBusiBusinessDate);
        setString(4,hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_business_date");
            hLastBusinessDate = getValue("last_business_date");
        }

    }


    /***********************************************************************/
    void readFile(String inputFileName) throws Exception {
        
        if (openBinaryInput(inputFileName) == false) {
            comcr.errRtn("檔案不存在：" + inputFileName, "", "");
        }
        showLogMessage("I", "", "");
    	showLogMessage("I", "", String.format("正在處理檔案 = [%s]", inputFileName));
        int fileTotalCnt = 0;
        int readlen = 0;
        byte[] bytes = new byte[84];
        while ((readlen = readBinFile(bytes)) > 0) {
        	totalCnt++;
        	fileTotalCnt++;
        	errorFlag = ""; 
            String str82 = new String(bytes, 0, readlen, "MS950");
            if (str82.length() < 82)
                continue;
            String signStr1 = "",signStr2 = "";
            EcsEBill ecsEBill = new EcsEBill();
            initData();
            ecsEBill.inTxDate = comc.rtrim(comStr.mid(str82,0,8));//交易日
            ecsEBill.inTime = comc.rtrim(comStr.mid(str82,8,6));//交易時間
            ecsEBill.inOutBankNo = comc.rtrim(comStr.mid(str82,16,3));//銀行代號
            ecsEBill.inOutAccNo = comc.rtrim(comStr.mid(str82,19,16));//無用
            ecsEBill.inCardNo = comc.rtrim(comStr.mid(str82,35,16));//繳款虛擬帳號
            ecsEBill.inAmt = comStr.ss2Num(comc.rtrim(comStr.mid(str82,51,13)));//繳款金額
            signStr1 = comc.rtrim(comStr.mid(str82,64,1));
            signStr2 = comc.rtrim(comStr.mid(str82,65,1));
            ecsEBill.inFee = comStr.ss2Num( comc.rtrim(comStr.mid(str82,66,13)));//手續費
            ecsEBill.inRvsMark = comc.rtrim(comStr.mid(str82,81,1));//EC註記   
            
            int selectResult =  selectCrdCard(ecsEBill.inCardNo);
            if(selectResult == 0) 
            	selectResult =  selectActAcct1(ecsEBill.inCardNo);
            if(selectResult == 0) 
            	selectResult =  selectActAcct2(ecsEBill.inCardNo);
            if(selectResult == 0) 
            	errorFlag = "Y";
            
            if(!"0".equals(signStr1)) {
            	String signStr = "0";
            	signStr = comStr.decode(signStr2, ",{,A,B,C,D,E,F,G,H,I,},J,K,L,M,N,O,P,Q,R", ",0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9");
            	ecsEBill.inAmt = comStr.ss2Num(String.format("%s.%s%s",(long)ecsEBill.inAmt,signStr1,signStr));
            	errorFlag = "Y";
            }
            
            if(comStr.pos(",},J,K,L,M,N,O,P,Q,R", signStr2)>0) {
            	ecsEBill.inAmt = ecsEBill.inAmt * -1;
            	errorFlag = "Y";
            }
            
            hApdlSerialNo = String.format("%07d", maxSerialNo++);

            if(!"Y".equals(errorFlag))
            	insertActPayDetail(ecsEBill);
            else
            	insertActPayError(ecsEBill);
            
            if ((totalCnt % 1000) == 0) {
//            	commitDataBase();
            	showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }
        }
        closeBinaryInput();
        showLogMessage("I", "", "");
    	showLogMessage("I", "", String.format("[%s]檔案已處理結束 ,筆數 = [%s]", inputFileName,fileTotalCnt));
    	commitDataBase();
    }
       
    int selectCrdCard(String cardNo) throws Exception {
    	
    	extendField = "CRD_CARD.";
    	sqlCmd = "SELECT ACCT_TYPE,ID_P_SEQNO,CORP_P_SEQNO,P_SEQNO,ACNO_P_SEQNO FROM CRD_CARD WHERE CARD_NO = ? ";
    	setString(1,cardNo);
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		hAcctType = getValue("CRD_CARD.ACCT_TYPE");
    		hIdPSeqno = getValue("CRD_CARD.ID_P_SEQNO");
    		hCorpPSeqno = getValue("CRD_CARD.CORP_P_SEQNO");
    		hPSeqno = getValue("CRD_CARD.P_SEQNO");
    		hAcnoPSeqno = getValue("CRD_CARD.ACNO_P_SEQNO");
    	}
    	
    	return recordCnt;
    }
    
    int selectActAcct1(String cardNo) throws Exception {
 
    	extendField = "ACT_ACNO1.";
    	sqlCmd = "SELECT ACCT_TYPE,ID_P_SEQNO,CORP_P_SEQNO,P_SEQNO,ACNO_P_SEQNO FROM ACT_ACNO WHERE PAYMENT_NO = ? ";
    	setString(1,cardNo);
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		hAcctType = getValue("ACT_ACNO1.ACCT_TYPE");
    		hIdPSeqno = getValue("ACT_ACNO1.ID_P_SEQNO");
    		hCorpPSeqno = getValue("ACT_ACNO1.CORP_P_SEQNO");
    		hPSeqno = getValue("ACT_ACNO1.P_SEQNO");
    		hAcnoPSeqno = getValue("ACT_ACNO1.ACNO_P_SEQNO");
    	}
    	
    	return recordCnt;
    }
    
    int selectActAcct2(String cardNo) throws Exception {
  
    	extendField = "ACT_ACNO2.";
    	sqlCmd = "SELECT ACCT_TYPE,ID_P_SEQNO,CORP_P_SEQNO,P_SEQNO,ACNO_P_SEQNO FROM ACT_ACNO WHERE PAYMENT_NO_II = ? ";
    	setString(1,cardNo);
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		hAcctType = getValue("ACT_ACNO2.ACCT_TYPE");
    		hIdPSeqno = getValue("ACT_ACNO2.ID_P_SEQNO");
    		hCorpPSeqno = getValue("ACT_ACNO2.CORP_P_SEQNO");
    		hPSeqno = getValue("ACT_ACNO2.P_SEQNO");
    		hAcnoPSeqno = getValue("ACT_ACNO2.ACNO_P_SEQNO");
    	}
    	
    	return recordCnt;
    }
    
     
    void selectInsertDetail() throws Exception {
    	sqlCmd = " SELECT COUNT(*) as BATCH_TOT_CNT,"
				   + "SUM(PAY_AMT) as BATCH_TOT_AMT "
				   + "FROM ACT_PAY_DETAIL "
				   + "WHERE BATCH_NO = ? ";
    	setString(1,batchNo);
		selectTable();
		int batchTotCnt = getValueInt("BATCH_TOT_CNT");
		double batchTotAmt = getValueDouble("BATCH_TOT_AMT");
		if(batchTotCnt > 0)
			insertActPayBatch(batchNo,batchTotCnt,batchTotAmt);
    }
    
    void getBatchNo() throws Exception {
        batchNo = hBusiBusinessDate + "5556" ;
    	sqlCmd = "select (nvl(max(batch_no),?) + 1) as max_batch_no from ACT_PAY_DETAIL where batch_no like ? ";
    	setString(1,batchNo + "0000");
    	setString(2,batchNo + "%");
    	selectTable();
    	batchNo = getValue("max_batch_no");
        showLogMessage("I", "", "取得 MAX BATCH_NO = [" + batchNo + "]");
    }
    
    /***********************************************************************/
    void insertActPayDetail(EcsEBill ecsEBill) throws Exception {
    	
    	extendField = "INSERT_DETAIL.";
        setValue("INSERT_DETAIL.BATCH_NO", batchNo);
        setValue("INSERT_DETAIL.SERIAL_NO", hApdlSerialNo);
        setValue("INSERT_DETAIL.CURR_CODE", "901");
        setValue("INSERT_DETAIL.PAYMENT_NO", ecsEBill.inCardNo);
        setValueDouble("INSERT_DETAIL.PAY_AMT", ecsEBill.inAmt);
        setValueDouble("INSERT_DETAIL.DC_PAY_AMT", ecsEBill.inAmt);
        setValue("INSERT_DETAIL.PAY_DATE", ecsEBill.inTxDate);
        setValue("INSERT_DETAIL.PAY_TIME", ecsEBill.inTime);
        setValue("INSERT_DETAIL.PAYMENT_TYPE2", "E1");
        setValue("INSERT_DETAIL.PAYMENT_TYPE", "COU4");
        setValue("INSERT_DETAIL.UNITE_MARK", "");
        setValue("INSERT_DETAIL.DEF_BRANCH", "3144");
        setValue("INSERT_DETAIL.PAY_BRANCH", "");
        setValue("INSERT_DETAIL.ACCT_TYPE", hAcctType);
        setValue("INSERT_DETAIL.ID_P_SEQNO", hIdPSeqno);
        setValue("INSERT_DETAIL.P_SEQNO", hPSeqno);
        setValue("INSERT_DETAIL.ACNO_P_SEQNO", hAcnoPSeqno);
        setValue("INSERT_DETAIL.crt_user", "ecs"); // update_user
        setValue("INSERT_DETAIL.crt_date", sysDate); // update_date
        setValue("INSERT_DETAIL.crt_time", sysTime); // update_time
        setValue("INSERT_DETAIL.mod_time", sysDate + sysTime);
        setValue("INSERT_DETAIL.mod_user", "ecs");
        setValue("INSERT_DETAIL.mod_pgm", prgmId);
        daoTable = "ACT_PAY_DETAIL";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_detail duplicate!", "", hCallBatchSeqno);
        }
    }
    
    String getIdNo(String idPSeqno) throws Exception {
    	extendField = "idno.";
    	sqlCmd = " select id_no from crd_idno where id_p_seqno = ?";
    	setString(1,idPSeqno);
    	selectTable();
    	return getValue("idno.id_no");
    }
    
    /***********************************************************************/
    void insertActPayError(EcsEBill ecsEBill) throws Exception {
		String idNo = getIdNo(hIdPSeqno);
    	extendField = "INSERT_ERROR.";
        setValue("INSERT_ERROR.BATCH_NO", batchNo);
        setValue("INSERT_ERROR.SERIAL_NO", hApdlSerialNo);
        setValue("INSERT_ERROR.CURR_CODE", "901");
        setValue("INSERT_ERROR.PAY_CARD_NO", ecsEBill.inCardNo);
        setValueDouble("INSERT_ERROR.PAY_AMT", ecsEBill.inAmt);
        setValueDouble("INSERT_ERROR.DC_PAY_AMT", ecsEBill.inAmt);
        setValue("INSERT_ERROR.PAY_DATE", ecsEBill.inTxDate);
        setValue("INSERT_ERROR.PAY_TIME", ecsEBill.inTime);
        setValue("INSERT_ERROR.PAYMENT_TYPE2", "E1");
        setValue("INSERT_ERROR.PAYMENT_TYPE", "COU4");
        setValue("INSERT_ERROR.UNITE_MARK", "");
        setValue("INSERT_ERROR.DEF_BRANCH", "3144");
        setValue("INSERT_ERROR.PAY_BRANCH", "");
        setValue("INSERT_ERROR.ACCT_TYPE", hAcctType);
        setValue("INSERT_ERROR.ID_NO", idNo);
        setValue("INSERT_ERROR.P_SEQNO", hPSeqno);
        setValue("INSERT_ERROR.ACNO_P_SEQNO", hAcnoPSeqno);
        setValue("INSERT_ERROR.crt_user", "ecs"); // update_user
        setValue("INSERT_ERROR.crt_date", sysDate); // update_date
        setValue("INSERT_ERROR.crt_time", sysTime); // update_time
        setValue("INSERT_ERROR.mod_time", sysDate + sysTime);
        setValue("INSERT_ERROR.mod_user", "ecs");
        setValue("INSERT_ERROR.mod_pgm", prgmId);
        daoTable = "ACT_PAY_ERROR";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_error duplicate!", "", hCallBatchSeqno);
        }
    }
    
    void insertActPayBatch(String batchNo,int batchTotCnt,double batchTotAmt) throws Exception {
    	extendField = "INSERT_BATCH.";
        setValue("INSERT_BATCH.BATCH_NO", batchNo);
        setValueInt("INSERT_BATCH.BATCH_TOT_CNT", batchTotCnt);
        setValueDouble("INSERT_BATCH.BATCH_TOT_AMT", batchTotAmt);
        setValue("INSERT_BATCH.crt_user", "ecs"); // update_user
        setValue("INSERT_BATCH.crt_date", sysDate); // update_date
        setValue("INSERT_BATCH.crt_time", sysTime); // update_time
        setValue("INSERT_BATCH.mod_time", sysDate + sysTime);
        setValue("INSERT_BATCH.mod_user", "ecs");
        setValue("INSERT_BATCH.mod_pgm", prgmId);
        setValue("INSERT_BATCH.confirm_user", "ecs");
        setValue("INSERT_BATCH.confirm_date", hBusiBusinessDate);
        setValue("INSERT_BATCH.confirm_time", sysTime);
        daoTable = "ACT_PAY_BATCH";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_batch duplicate!", "", hCallBatchSeqno);
        }
    }

    void initData() {
		hAcctType = "";
		hIdPSeqno = "";
		hCorpPSeqno = "";
		hPSeqno = "";
		hAcnoPSeqno = "";
		hApdlSerialNo = "";
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActA310 proc = new ActA310();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}

class EcsEBill {
//	String batchNo = "";
	String inTxDate = "";
	String inTx = "";
	String inTime = "";
	String inOutBankNo = "";
	String inOutAccNo = "";
	String inCardNo = "";
	double inAmt = 0;
	double inFee = 0;
	String inRvsMark = "";
}
