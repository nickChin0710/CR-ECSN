/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/09/26  V1.00.00    Ryan     program initial                            *
 *  112/09/28  V1.00.01    Ryan     batch_no 每批+1                                                                                 *
 *  112/11/06  V1.00.02    Ryan     ACT_PAY_ERROR2 改 ACT_PAY_ERROR  ,調整serialNo *                                                                               *
 ******************************************************************************/

package Act;

import java.text.Normalizer;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommString;

/*每天接收全國繳費網繳款作業*/
public class ActA330 extends AccessDAO {
    private final String PROGNAME = "每天接收QR CODE繳款作業 112/11/06 V1.00.02";
    private final String FILE_NAME = "F00600000.ICEMPQBD";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommString comStr = new CommString();
    CommDate comDate = new CommDate();
    String hModUser = "";
    long hModSeqno = 0;
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hLastBusinessDate = "";
    
    String fileType = "";
    String hMerchId = "";
    String trxnFlag = "";
    String merchId = "";
    String paymentNo = "";
	double payAmt = 0;
	double dcPayAmt = 0;
	String payDate = "";
	String payTime = "";
	String acctType = "";
	String idPSeqno = "";
	String pSeqno = "";
	String acnoPSeqno = "";
	String batchNo = "";
	String serialNo = "";
	String amtSign = "";   
    int maxSerialNo = 1;
    int totalCnt = 0;
    String errorFlag = ""; 
    int fi = 0;
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

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

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
    	int fileCnt = 0;
    	if (listOfFiles.size() > 0)
			for (String file : listOfFiles) {
				if(file.indexOf(FILE_NAME)<0) {
					continue;
				}
				String twDate1 = comDate.toTwDate(hBusiBusinessDate);
				String twDate2 = comDate.toTwDate(hLastBusinessDate);
				if(!comStr.mid(file,19,6).equals(comStr.right(twDate1, 6)) 
						&& !comStr.mid(file,19,6).equals(comStr.right(twDate2, 6)))  
				{
					continue;
				}
				fileCnt ++;
				inputFileName = Normalizer.normalize(String.format("/crdataupload/%s", file), java.text.Normalizer.Form.NFKD);
				renameFile = String.format("%s/media/act/backup/%s", comc.getECSHOME(), file);
				renameFile = Normalizer.normalize(renameFile, java.text.Normalizer.Form.NFKD);
				readFile(inputFileName);
		        renameFile(inputFileName,renameFile);
			}
    	if(fileCnt==0)
    		showLogMessage("I", "", "無符合檔案處理[" + FILE_NAME + "]");
    }
    
    void renameFile(String inputFileName,String renameFile) throws Exception {
    	String tmpstr1 = inputFileName;
    	String tmpstr2 = String.format("%s.%s", renameFile,sysDate+sysTime);
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
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_business_date");
            hLastBusinessDate = getValue("last_business_date");
        }

    }


    /***********************************************************************/
    void readFile(String inputFileName) throws Exception {
        boolean procFlag = false;
        if ((fi = openInputText(inputFileName,"MS950")) == -1) {
            comcr.errRtn("檔案不存在：" + inputFileName, "", "");
        }
        showLogMessage("I", "", "");
    	showLogMessage("I", "", String.format("正在處理檔案 = [%s]", inputFileName));
        int fileTotalCnt = 0;
        String readlen = "";
        while ((readlen = readTextFile(fi)) != null) {
        	if (readlen.length() == 0) {
                if (endFile[fi].equals("Y"))
                   break;
            }
        	fileTotalCnt++;
        	errorFlag = ""; 
        	byte[] bytes = readlen.getBytes("MS950");
        	  if (bytes.length < 220)
                  continue;
        	
            String str220 = new String(bytes,"MS950");
            
            initData();
            fileType = comc.rtrim(comStr.mid(str220,0,2));//EPMT1-DREC-TYPE2 資料表頭識別符號             
            hMerchId = comc.rtrim(comStr.mid(str220,1,15));// EPMT1-HMERCH-ID 特店代碼                       
            if("H".equals(comStr.left(fileType, 1))) {
            	procFlag = false;
            	if("006996295241002".equals(hMerchId)) {
            		procFlag = true;
            	}
            }
            
            if(procFlag == false)
            	continue;
            
            if(!"D2".equals(fileType)) 
            	continue;
     
    		payDate = comc.rtrim(comStr.mid(str220,21,8));//EPMT1-TRXN-DATE 交易日                       
    		payAmt = comStr.ss2Num(comc.rtrim(comStr.mid(str220,29,13)))/100;//EPMT1-TRXN-AMT 交易金額                   
    		amtSign = comc.rtrim(comStr.mid(str220,42,1));//EPMT1-TRXN-AMT-SIGN   	金額正負號                                 
    		paymentNo = comc.rtrim(comStr.mid(str220,59,19));//EPMT1-ORDER-NO 信用卡訂單編號(銷帳編號)
    		trxnFlag = comc.rtrim(comStr.mid(str220,113,1));//EPMT1-FEE-TRXN-FLAG	"信用卡繳費註記空白：一般交易f:信用卡繳費交易、VISA QR CODE掃碼支付
    		merchId = comc.rtrim(comStr.mid(str220,114,15));//EPMT1-MERCH-ID 信用卡繳費類商代(99開頭)
    		payTime = comc.rtrim(comStr.mid(str220,192,6));//EPMT1-TXN-TIME 交易時間 (消費扣款/信用卡/銀聯卡)
      
    		if(!"f".equals(trxnFlag) || !"991000017004001".equals(merchId) )
    			continue;

        	dcPayAmt = payAmt;
     	    serialNo = String.format("%07d", maxSerialNo++);

            if(!comStr.empty(amtSign)) {
            	errorFlag = "Y";
            }
            
            int selectResult =  selectActAcno();
            if(selectResult == 0) 
            	errorFlag = "Y";

            if(!"Y".equals(errorFlag))
            	insertActPayDetail();
            else
            	insertActPayError();
            
        	totalCnt++;
            
            if ((totalCnt % 1000) == 0) {
            	showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }
   
        }
        closeInputText(fi);
        showLogMessage("I", "", "");
    	showLogMessage("I", "", String.format("[%s]檔案已處理結束 ,筆數 = [%s]", inputFileName,fileTotalCnt));
    	commitDataBase();
    }
       
    int selectActAcno() throws Exception {
    	extendField = "ACNO.";
    	sqlCmd = "SELECT ACCT_TYPE,ID_P_SEQNO,P_SEQNO,ACNO_P_SEQNO FROM ACT_ACNO WHERE PAYMENT_NO = ? ";
    	setString(1,paymentNo);
    	int recordCnt = selectTable();
    	if(recordCnt > 0) {
    		acctType = getValue("ACNO.ACCT_TYPE");
    		idPSeqno = getValue("ACNO.ID_P_SEQNO");
    		pSeqno = getValue("ACNO.P_SEQNO");
    		acnoPSeqno = getValue("ACNO.ACNO_P_SEQNO");
    	}
    	return recordCnt;
    }
  
    void getBatchNo() throws Exception {
        batchNo = hBusiBusinessDate + "5558" ;
    	sqlCmd = "select (nvl(max(batch_no),?) + 1) as max_batch_no from ACT_PAY_DETAIL where batch_no like ? ";
    	setString(1,batchNo + "0000");
    	setString(2,batchNo + "%");
    	selectTable();
    	batchNo = getValue("max_batch_no");
        showLogMessage("I", "", "取得 MAX BATCH_NO = [" + batchNo + "]");
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
    
    /***********************************************************************/
    void insertActPayDetail() throws Exception {
    	
    	extendField = "INSERT_DETAIL.";
        setValue("INSERT_DETAIL.BATCH_NO", batchNo);
        setValue("INSERT_DETAIL.SERIAL_NO", serialNo);
        setValue("INSERT_DETAIL.CURR_CODE", "901");
        setValue("INSERT_DETAIL.PAYMENT_NO", paymentNo);
        setValueDouble("INSERT_DETAIL.PAY_AMT", payAmt );
        setValueDouble("INSERT_DETAIL.DC_PAY_AMT", dcPayAmt);
        setValue("INSERT_DETAIL.PAY_DATE", payDate);
        setValue("INSERT_DETAIL.PAY_TIME", payTime);
        setValue("INSERT_DETAIL.PAYMENT_TYPE2", "Q1");
        setValue("INSERT_DETAIL.PAYMENT_TYPE", "QRC1");
        setValue("INSERT_DETAIL.UNITE_MARK", "");
        setValue("INSERT_DETAIL.DEF_BRANCH", "3144");
        setValue("INSERT_DETAIL.PAY_BRANCH", "");
        setValue("INSERT_DETAIL.ACCT_TYPE", acctType);
        setValue("INSERT_DETAIL.ID_P_SEQNO", idPSeqno);
        setValue("INSERT_DETAIL.P_SEQNO", pSeqno);
        setValue("INSERT_DETAIL.ACNO_P_SEQNO", acnoPSeqno);
        setValue("INSERT_DETAIL.crt_user", "ecs"); // update_user
        setValue("INSERT_DETAIL.crt_date", sysDate); // update_date
        setValue("INSERT_DETAIL.crt_time", sysTime); // update_time
        setValue("INSERT_DETAIL.mod_time", sysDate + sysTime);
        setValue("INSERT_DETAIL.mod_user", "ecs");
        setValue("INSERT_DETAIL.mod_pgm", javaProgram);
        daoTable = "ACT_PAY_DETAIL";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_detail duplicate!", "", "");
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
    void insertActPayError() throws Exception {
    	String idNo = getIdNo(idPSeqno);
    	extendField = "INSERT_ERROR.";
        setValue("INSERT_ERROR.BATCH_NO", batchNo);
        setValue("INSERT_ERROR.SERIAL_NO", serialNo);
        setValue("INSERT_ERROR.CURR_CODE", "901");
        setValue("INSERT_ERROR.PAY_CARD_NO", paymentNo);
        setValueDouble("INSERT_ERROR.PAY_AMT", payAmt);
        setValueDouble("INSERT_ERROR.DC_PAY_AMT", dcPayAmt);
        setValue("INSERT_ERROR.PAY_DATE", payDate);
        setValue("INSERT_ERROR.PAY_TIME", payTime);
        setValue("INSERT_ERROR.PAYMENT_TYPE2", "Q1");
        setValue("INSERT_ERROR.PAYMENT_TYPE", "QRC1");
        setValue("INSERT_ERROR.UNITE_MARK", "");
        setValue("INSERT_ERROR.DEF_BRANCH", "3144");
        setValue("INSERT_ERROR.PAY_BRANCH", "");
        setValue("INSERT_ERROR.ACCT_TYPE", acctType);
        setValue("INSERT_ERROR.ID_NO", idNo);
        setValue("INSERT_ERROR.P_SEQNO", pSeqno);
        setValue("INSERT_ERROR.ACNO_P_SEQNO", acnoPSeqno);
        setValue("INSERT_ERROR.crt_user", "ecs"); // update_user
        setValue("INSERT_ERROR.crt_date", sysDate); // update_date
        setValue("INSERT_ERROR.crt_time", sysTime); // update_time
        setValue("INSERT_ERROR.mod_time", sysDate + sysTime);
        setValue("INSERT_ERROR.mod_user", "ecs");
        setValue("INSERT_ERROR.mod_pgm", javaProgram);
        daoTable = "ACT_PAY_ERROR";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_error duplicate!", "", "");
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
        setValue("INSERT_BATCH.mod_pgm", javaProgram);
        setValue("INSERT_BATCH.confirm_user", "ecs");
        setValue("INSERT_BATCH.confirm_date", hBusiBusinessDate);
        setValue("INSERT_BATCH.confirm_time", sysTime);
        daoTable = "ACT_PAY_BATCH";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_batch duplicate!", "", "");
        }
    }


	void initData() {
		fileType = "";
		paymentNo = "";
		payAmt = 0;
		dcPayAmt = 0;
		payDate = "";
		payTime = "";
		acctType = "";
		idPSeqno = "";
		pSeqno = "";
		acnoPSeqno = "";
		serialNo = "";
		hMerchId = "";
		trxnFlag = "";
		merchId = "";
		amtSign = "";
	}
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActA330 proc = new ActA330();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
