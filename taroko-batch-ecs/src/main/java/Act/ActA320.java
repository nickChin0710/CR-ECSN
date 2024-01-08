package Act;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112-07-28  V0.00.01     Ryan     initial                                   *
* 112-11-06  V0.00.02     Ryan     add insertActPayBatch                     *
*****************************************************************************/

import com.CommCrd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.AccessDAO;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import Dxc.Util.SecurityUtil;

public class ActA320 extends AccessDAO {
	private String PROGNAME = "接收人工上傳繳款檔作業  112/11/06 V0.00.01";
	CommFunction comm = new CommFunction();
	CommString commStr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommDate commDate = new CommDate();
	private static final String FOLDER = "/media/act";
	private static final String FROM_FOLDER = "/crdataupload";
	private static final String FILE_NAME = "ECSCREDITPAY_YYYYMMDD";
	int totalCnt = 0;

	private String lineLength = "";
	private String idCardNo = "";
	private String paymentDate = ""; 
	private long paymentAmt = 0;
	private String paymentType = "";
	private String acctType = "";
	private String idPSeqno = "";
	private String corpPSeqno = "";
	private String pSeqno = "";
	private String acnoPSeqno = "";
	private String busDate = "";
	private String batchNo = "";
	private String paymentType2 = "";
	private String serialNo = "";
	private int maxSerialNo = 1;
	
	List<String> listFileName = new ArrayList<String>();
	
//=**************************************************************************** 
	public static void main(String[] args) {
		ActA320 proc = new ActA320();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
	}

//=============================================================================
	public int mainProcess(String[] args){
		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			// =====================================

			// 固定要做的
			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			String procDate1 = this.getBusiDate();
			String parm1 = "";
			if (args.length == 1) {
				parm1 = args[0];
				if (parm1.length() != 8 || !commDate.isDate(parm1)) {
					showLogMessage("I", "", "請傳入參數合格值yyyymmdd[" + parm1 + "]");
					return 1;
				}
				procDate1 = parm1;
			}

			String procDate2 = commDate.dateAdd(procDate1, 0, 0, -1);
			
			showLogMessage("I", "", "傳入參數日期 = [" + parm1 + "]");
			showLogMessage("I", "", "取得營業日期 =  [" + procDate1 + "]");
			showLogMessage("I", "", "取得營業日期前一日 =  [" + procDate2 + "]");
			
			String fileName1 = FILE_NAME.replace("YYYYMMDD", procDate1);
			String fileName2 = FILE_NAME.replace("YYYYMMDD", procDate2);

			int rtnCode = 0;
			rtnCode = openFile(fileName1,fileName2);
			if (rtnCode  == 0) {
				return 0;
			}
			readFile();	
			renameFile();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}finally {
			finalProcess();
		}
	}

//=============================================================================
	int openFile(String fileName1,String fileName2) throws Exception {
	    String fileNameTemplate1 = String.format("%s\\d\\d.(txt|TXT)", fileName1);
	    String fileNameTemplate2 = String.format("%s\\d\\d.(txt|TXT)", fileName2);
		List<String> listOfFiles = comc.listFS(FROM_FOLDER, "", "");
		for (String file : listOfFiles) {
			if(!file.matches(fileNameTemplate1) && !file.matches(fileNameTemplate2)) {
				continue;
			}
			listFileName.add(file);
		}

        if (listFileName.size() == 0) {
        	showLogMessage("D", "", "無檔案可處理");
        	return 0;
        }
		return 1;
	}

//=============================================================================
	int readFile() throws Exception {
		for(String fileName : listFileName) {
			maxSerialNo = 1;
			showLogMessage("I", "", "");
			showLogMessage("I", "", "====Start Read File ["+fileName+"]====");
			busDate = commStr.mid(fileName, 13, 8);
			getBatchNo();
			BufferedReader br = null;
			FileInputStream fis = null;
			try {
				String tmpStr = Paths.get(FROM_FOLDER,fileName).toString();
				String tempPath = SecurityUtil.verifyPath(tmpStr);
				fis = new FileInputStream(new File(tempPath));
				br = new BufferedReader(new InputStreamReader(fis, "MS950"));

				System.out.println("   tempPath = [" + tempPath + "]");

			} catch (FileNotFoundException exception) {
				System.out.println("bufferedReader exception: " + exception.getMessage());
				return 1;
			}

			while ((lineLength = br.readLine()) != null) {
				if(lineLength.length()<37)
					continue;
				totalCnt ++;
				initData();
				byte[] bytes = lineLength.getBytes("MS950");
				idCardNo = comc.subMS950String(bytes, 0, 16).trim();//繳款入帳的鍵值，自然人ID或卡號
				paymentDate = comc.subMS950String(bytes, 16, 8).trim();//繳款日期
				paymentAmt = ss2Long(comc.subMS950String(bytes, 24, 11).trim());//繳款金額
				paymentType2 = comc.subMS950String(bytes, 35, 2).trim();//繳款方式
				serialNo = String.format("%07d", maxSerialNo++);
				int resultCnt = 0;
				if(idCardNo.length() != 10) {
					    resultCnt = selectCrdCard();
					if(resultCnt == 0)
						resultCnt = selectActAcno1();
					if(resultCnt == 0)
						resultCnt = selectActAcno2();
				}
				
				if(idCardNo.length() == 10) {
					resultCnt = selectActAcno3();
				}
				
				selectPtrSysIdtab();
				if(resultCnt == 0 || paymentAmt <= 0) {
					insertActPayError2();
				}else {
					insertActPayDetail();
				}
			}
			selectInsertDetail();
			commitDataBase();
			br.close();
			fis.close();
		}
		return 0;
	}
	
	private int selectCrdCard() throws Exception {
		sqlCmd = "select acct_type ,id_p_seqno ,corp_p_seqno , p_seqno ,acno_p_seqno from crd_card where card_no = ?";
		setString(1,idCardNo);
		int n = selectTable();
		if(n>0) {
			acctType = getValue("acct_type");
			idPSeqno = getValue("id_p_seqno");
			corpPSeqno = getValue("corp_p_seqno");
			pSeqno = getValue("p_seqno");
			acnoPSeqno = getValue("acno_p_seqno");
		}
		return n;
	}
	
	private int selectActAcno1() throws Exception {
		sqlCmd = "select acct_type ,id_p_seqno ,corp_p_seqno , p_seqno ,acno_p_seqno from act_acno where payment_no = ?";
		setString(1,idCardNo);
		int n = selectTable();
		if(n>0) {
			acctType = getValue("acct_type");
			idPSeqno = getValue("id_p_seqno");
			corpPSeqno = getValue("corp_p_seqno");
			pSeqno = getValue("p_seqno");
			acnoPSeqno = getValue("acno_p_seqno");
		}
		return n;
	}
	
	private int selectActAcno2() throws Exception {
		sqlCmd = "select acct_type ,id_p_seqno ,corp_p_seqno , p_seqno ,acno_p_seqno from act_acno where payment_no_ii = ?";
		setString(1,idCardNo);
		int n = selectTable();
		if(n>0) {
			acctType = getValue("acct_type");
			idPSeqno = getValue("id_p_seqno");
			corpPSeqno = getValue("corp_p_seqno");
			pSeqno = getValue("p_seqno");
			acnoPSeqno = getValue("acno_p_seqno");
		}
		return n;
	}
	
	private int selectActAcno3() throws Exception {
		sqlCmd = "select acct_type ,id_p_seqno ,corp_p_seqno , p_seqno ,acno_p_seqno from act_acno "
				+ " where acct_key = ? and acct_type = '01' and corp_p_seqno = ''";
		setString(1,idCardNo + "0");
		int n = selectTable();
		if(n>0) {
			acctType = getValue("acct_type");
			idPSeqno = getValue("id_p_seqno");
			corpPSeqno = getValue("corp_p_seqno");
			pSeqno = getValue("p_seqno");
			acnoPSeqno = getValue("acno_p_seqno");
		}
		return n;
	}
	
	private int selectPtrSysIdtab() throws Exception {
		sqlCmd = "select id_code from ptr_sys_idtab Where wf_type = 'PAYMENT_TYPE2' and wf_id = ? ";
		setString(1,paymentType2);
		int n = selectTable();
		if(n>0) {
			paymentType = getValue("id_code");
		}
		return n;
	}
	
    void getBatchNo() throws Exception {
        batchNo = busDate + "5557" ;
    	sqlCmd = "select (nvl(max(batch_no),?) + 1) as max_batch_no from ACT_PAY_DETAIL where batch_no like ? ";
    	setString(1,batchNo + "0000");
    	setString(2,batchNo + "%");
    	selectTable();
    	batchNo = getValue("max_batch_no");
        showLogMessage("I", "", "取得 MAX BATCH_NO = [" + batchNo + "]");
    }
    
	
	void insertActPayDetail() throws Exception{
	   	extendField = "DETAIL.";
		setValue("DETAIL.BATCH_NO", batchNo);
		setValue("DETAIL.SERIAL_NO", serialNo);
		setValue("DETAIL.CURR_CODE", "901");
		setValue("DETAIL.PAYMENT_NO", idCardNo);
		setValueDouble("DETAIL.PAY_AMT", paymentAmt);
		setValueDouble("DETAIL.DC_PAY_AMT", paymentAmt);
		setValue("DETAIL.PAY_DATE", paymentDate);
		setValue("DETAIL.PAY_TIME", "");
		setValue("DETAIL.PAYMENT_TYPE2", paymentType2);
		setValue("DETAIL.PAYMENT_TYPE", paymentType);
		setValue("DETAIL.UNITE_MARK", "");
		setValue("DETAIL.DEF_BRANCH", "");
		setValue("DETAIL.PAY_BRANCH", "");
		setValue("DETAIL.ACCT_TYPE", acctType);
		setValue("DETAIL.ID_P_SEQNO", idPSeqno);
		setValue("DETAIL.P_SEQNO", pSeqno);
		setValue("DETAIL.ACNO_P_SEQNO", acnoPSeqno);
        setValue("DETAIL.crt_user", javaProgram); // update_user
        setValue("DETAIL.crt_date", sysDate); // update_date
        setValue("DETAIL.crt_time", sysTime); // update_time
		setValue("DETAIL.mod_time", sysDate + sysTime);
		setValue("DETAIL.mod_pgm", javaProgram);
		setValue("DETAIL.mod_user", javaProgram);
		daoTable = "ACT_PAY_DETAIL";
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("I", "", String.format("insert ACT_PAY_DETAIL error ,BATCH_NO=[%s] ,SERIAL_NO=[%s] ,PAY_CARD_NO=[%s]"
					, batchNo ,serialNo ,idCardNo));
			return;
		}
		if("Y".equals(dupRecord)) {
			showLogMessage("I", "", String.format("insert ACT_PAY_DETAIL duplicate ,BATCH_NO=[%s] ,SERIAL_NO=[%s] ,PAY_CARD_NO=[%s]"
					, batchNo ,serialNo ,idCardNo));
			return;
		}
	}
    
	  String getIdNo(String idPSeqno) throws Exception {
	    	extendField = "idno.";
	    	sqlCmd = " select id_no from crd_idno where id_p_seqno = ?";
	    	setString(1,idPSeqno);
	    	selectTable();
	    	return getValue("idno.id_no");
	    }
	
	void insertActPayError2() throws Exception{
		String idNo = getIdNo(idPSeqno);
	   	extendField = "ERROR.";
		setValue("ERROR.BATCH_NO", batchNo);
		setValue("ERROR.SERIAL_NO", serialNo);
		setValue("ERROR.CURR_CODE", "901");
		setValue("ERROR.PAY_CARD_NO", idCardNo);
		setValueDouble("ERROR.PAY_AMT", paymentAmt);
		setValueDouble("ERROR.DC_PAY_AMT", paymentAmt);
		setValue("ERROR.PAY_DATE", paymentDate);
		setValue("ERROR.PAY_TIME", "");
		setValue("ERROR.PAYMENT_TYPE2", paymentType2);
		setValue("ERROR.PAYMENT_TYPE", paymentType);
		setValue("ERROR.UNITE_MARK", "");
		setValue("ERROR.DEF_BRANCH", "");
		setValue("ERROR.PAY_BRANCH", "");
		setValue("ERROR.ACCT_TYPE", acctType);
		setValue("ERROR.ID_NO", idNo);
		setValue("ERROR.P_SEQNO", pSeqno);
		setValue("ERROR.ACNO_P_SEQNO", acnoPSeqno);
        setValue("ERROR.crt_user", javaProgram); // update_user
        setValue("ERROR.crt_date", sysDate); // update_date
        setValue("ERROR.crt_time", sysTime); // update_time
		setValue("ERROR.mod_time", sysDate + sysTime);
		setValue("ERROR.mod_pgm", javaProgram);
		setValue("ERROR.mod_user", javaProgram);
		daoTable = "ACT_PAY_ERROR";
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("I", "", String.format("insert ACT_PAY_ERROR error ,BATCH_NO=[%s] ,SERIAL_NO=[%s] ,PAY_CARD_NO=[%s]"
					, batchNo ,serialNo ,idCardNo));
			return;
		}
		if("Y".equals(dupRecord)) {
			showLogMessage("I", "", String.format("insert ACT_PAY_ERROR duplicate ,BATCH_NO=[%s] ,SERIAL_NO=[%s] ,PAY_CARD_NO=[%s]"
					, batchNo ,serialNo ,idCardNo));
			return;
		}
	}
	
	void selectInsertDetail() throws Exception {
		sqlCmd = " SELECT COUNT(*) as BATCH_TOT_CNT," + "SUM(PAY_AMT) as BATCH_TOT_AMT " + "FROM ACT_PAY_DETAIL "
				+ "WHERE BATCH_NO = ? ";
		setString(1, batchNo);
		selectTable();
		int batchTotCnt = getValueInt("BATCH_TOT_CNT");
		double batchTotAmt = getValueDouble("BATCH_TOT_AMT");
		if (batchTotCnt > 0)
			insertActPayBatch(batchNo, batchTotCnt, batchTotAmt);
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
        setValue("INSERT_BATCH.confirm_date", busDate);
        setValue("INSERT_BATCH.confirm_time", sysTime);
        daoTable = "ACT_PAY_BATCH";
        insertTable();
        if (dupRecord.equals("Y")) {
            showLogMessage("I", "", "insert_act_pay_batch duplicate!");
            return;
        }
    }

//=============================================================================
	void renameFile() throws Exception {
		for(String fileName : listFileName) {
			String tmpStr1 = Paths.get(FROM_FOLDER,fileName).toString();
			String tempPath1 = SecurityUtil.verifyPath(tmpStr1);
			String tmpStr2 = String.format("%s%s/backup/%s_%s", comc.getECSHOME(), FOLDER ,fileName,sysDate+sysTime);
			String tempPath2 = SecurityUtil.verifyPath(tmpStr2);

			if (!comc.fileMove(tempPath1, tempPath2)) {
				showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]移動失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + fileName + "] 移動至 [" + tempPath2 + "]");
		}
	}
	
	long ss2Long(String param) {
		try {
			param = param.trim().replaceAll(",", "");
			if (commStr.empty(param) || !commStr.isNumber(param)) {
				return 0;
			}
			return Long.parseLong(param);
		} catch (Exception ex) {
			return 0;
		}
	}
	
	private void initData() {
		idCardNo = "";
		paymentDate = ""; 
		paymentAmt = 0;
		paymentType = "";
		acctType = "";
		idPSeqno = "";
		corpPSeqno = "";
		pSeqno = "";
		acnoPSeqno = "";
		paymentType2 = "";
	}
}
