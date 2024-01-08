/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  2021/01/27  V1.01.01  Eric              Initial                                 *   
*  2021/02/01  V1.01.02  Wilson       停用碼U -> update cca_card_base         *
*  2021/02/03  V1.01.03  Justin          change the method of opening the file from text to binary
*  110/02/04   V1.01.04  Wilson       讀檔路徑修改、檔名不用判斷日期                                                 *
*  111/02/14   V1.01.05    Ryan      big5 to MS950                                           *
****************************************************************************/

package Icu;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommString;

/*CARDLINK信用卡客戶資料處理程式*/
public class IcuD119 extends AccessDAO {
private String progname = "接收卡片狀態異動通知檔  111/02/14 V1.01.05";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommString zzStr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	int debug = 1;
	int datacnt = 0;
	int lenOfWord = 81;
	
	String prgmId = "IcuD119";
	
//	String fileFolderPath = comc.getECSHOME() + "/media/icu/";
	String fileFolderPath = "/crdataupload/";
	
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;
	Buf1 ncccData = new Buf1();
	protected final String dT1Str = "status_type,card_no,effect_period,card_status,"
			                      + "action_code,mod_date,mod_time,end_date,mod_code,"
			                      + "destition,dest_type,black_area,mod_type,mod_man,"
			                      + "rcode";

	protected final int[] dt1Length = { 1,16,4,1,2,
			                            8,6,8,1,1,
			                            1,9,1,20,2};

	int rptSeq1 = 0;
	String buf = "";
    String queryDate = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	String hNcccFilename = "";
	int hRecCnt1 = 0;
	int seq = 0;

	String getFileName;
	String outFileName;
	int totalInputFile;
	int totalOutputFile;

	String tmpStatusType;
	String tmpCardNo;
	String tmpEffectPeriod;
	String tmpCardStatus;
	String tmpActionCode;
	String tmpModDate;
	String tmpModTime;
	String tmpEndDate;
	String tmpModCode;
	String tmpDestition;
	String tmpDestType;
	String tmpBlackArea;
	String tmpModType;
	String tmpModMan;
	String tmpRcode;
    
    String firstnum;
    String hBusiBusinessDate = "";
    String errCode;

	protected String[] dT1 = new String[] {};
	Buf1 ncccData1 = new Buf1();

	public int mainProcess(String[] args) {

		try {
			dT1 = dT1Str.split(",");
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			comsecr = new CommSecr(getDBconnect(), getDBalias());

			hModUser = comc.commGetUserID();
			
            selectPtrBusinday();
			
            // 若沒有給定查詢日期，則查詢日期為系統日
//            if(args.length == 0) {
//                queryDate = hBusiBusinessDate;
//            }else
//            if(args.length == 1) {
//               if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
//                    showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
//                    return -1;
//                }
//                queryDate = args[0];
//            }else {
//                comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
//            }                       
			
			openFile();

			// ==============================================
			// 固定要做的
            showLogMessage("I", "", "執行結束,[ 總筆數 : "+ totalInputFile +"],[ 錯誤筆數 : "+ totalOutputFile +"]");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = " select business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		hBusiBusinessDate = getValue("business_date");
	}

	/************************************************************************/
	int openFile() throws Exception {
		int fileCount = 0;


		List<String> listOfFiles = comc.listFS(fileFolderPath, "", "");

//				final String fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", "F00600000", "ICCRSQND",
//				new CommDate().getLastTwoTWDate(queryDate), queryDate.substring(4, 8)); // 檔案正規表達式		
		
//		final String fileNameTemplate = String.format("%s\\.%s\\..*", "F00600000", "ICCRSQND"); // 檔案正規表達式		
		
		if (listOfFiles.size() > 0)
		for (String file : listOfFiles) {
			getFileName = file;

			if (getFileName.length() != 27)
				continue;
			
			if( ! getFileName.substring(0, 19).equals("F00600000.ICCRSQND."))  
			{
//				System.out.println(file+" NOT MAP "+fileNameTemplate);
				continue;
			}
			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(getFileName);
		}
		if (fileCount < 1) {
			showLogMessage("I", "", "無檔案可處理");
			
//			comcr.hCallErrorDesc = "無檔案可處理";
//            comcr.errRtn("無檔案可處理","處理日期 = " + queryDate  , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
	int readFile(String fileName) throws Exception {
//		String rec = "";
		String fileName2;
//		int fi;
		fileName2 = fileFolderPath + fileName;

//		int f = openInputText(fileName2);
//		if (f == -1) {
//			return 1;
//		}
//		closeInputText(f);
//
//		setConsoleMode("N");
//		fi = openInputText(fileName2, "big5");
		boolean isFileExist = openBinaryInput(fileName2);
		if (isFileExist == false) {
			return 1;
		}
//		setConsoleMode("Y");
//		if (fi == -1) {
//			return 1;
//		}

		showLogMessage("I", "", " Process file path =[" + fileFolderPath + " ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");
        
		int n=1;
		while (true) {
//			rec = readTextFile(fi); // read file data
//			if (endFile[fi].equals("Y"))
//				break;
			// 2021/02/03 Justin change the method of opening the file from text to binary
			byte[] bytes = new byte[lenOfWord];
			int lenOfRead = readBinFile(bytes);
			if (lenOfRead == -1) {
				break;
			}

			totalInputFile++;
		
			moveData(processDataRecord(getFieldValue( new String(bytes), dt1Length), dT1));
			processDisplay(1000);
			datacnt++;
			n++;
		}

		//if (totalOutputFile > 0) {
			
		if(totalInputFile > 0)	
		{	
			outPutTextFile();
			comc.writeReport(outFileName, lpar1, "MS950");
			insertFileCtl();
			lpar1.clear();
			
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
		    procFTP();
		    renameFile1(hNcccFilename);
		}

//		closeInputText(fi);
		closeBinaryInput();

		insertFileCtl1(fileName);

		renameFile(fileName);

		return 0;
	}

	/***********************************************************************/
    private void moveData(Map<String, Object> map) throws Exception {       
        String tmpChar1 = "";       
       
        tmpStatusType =   (String) map.get("status_type"); 
        tmpCardNo =       (String) map.get("card_no");
        tmpEffectPeriod = (String) map.get("effect_period");
        tmpCardStatus =   (String) map.get("card_status");
        tmpActionCode =   (String) map.get("action_code");
        tmpModDate =      (String) map.get("mod_date");
        tmpModTime =      (String) map.get("mod_time");
        tmpEndDate =      (String) map.get("end_date");
        tmpModCode =      (String) map.get("mod_code");
        tmpDestition =    (String) map.get("destition");
        tmpDestType =     (String) map.get("dest_type");
        tmpBlackArea =    (String) map.get("black_area");
        tmpModType =      (String) map.get("mod_type");
        tmpModMan =       (String) map.get("mod_man");
        tmpRcode =        (String) map.get("rcode");
        
        tmpStatusType =    tmpStatusType.trim();  
        tmpCardNo =        tmpCardNo.trim();      
        tmpEffectPeriod =  tmpEffectPeriod.trim();
        tmpCardStatus =    tmpCardStatus.trim();  
        tmpActionCode =    tmpActionCode.trim();  
        tmpModDate =       tmpModDate.trim();     
        tmpModTime =       tmpModTime.trim();     
        tmpEndDate =       tmpEndDate.trim();     
        tmpModCode =       tmpModCode.trim();     
        tmpDestition =     tmpDestition.trim();   
        tmpDestType =      tmpDestType.trim();    
        tmpBlackArea =     tmpBlackArea.trim();   
        tmpModType =       tmpModType.trim();     
        tmpModMan =        tmpModMan.trim();      
        tmpRcode =         tmpRcode.trim();         
    
        //檢核CARD_NO是否存在
        String checkno = tmpCardNo;
    
        int flag1 = checkCrdCard(checkno);
        int flag2 = checkCcaCardBase(checkno);
        int flag3 = checkCcaCardAcct(checkno);
        
      
        if(flag1 == 0 || flag2 == 0 || flag3 == 0)
        {
        	
        	String reason = "失敗,";
        	if(flag1 == 0)
        	{
        		reason = reason + "卡號不存在卡片資料檔 ";
        	}
        	if(flag2 == 0)
        	{
        		reason = reason + "卡號不存在授權卡片資料檔 ";
        	}
        	if(flag3 == 0)
        	{
        		reason = reason + "卡號不存在授權帳戶資料檔 ";
        	}     	
        	
        	createRecReport(reason);   	
        } 
        else {
        
          //實作依檔案狀態類別更新資料表
           if(tmpStatusType.equals("1"))
           {
           	switch(tmpCardStatus) {
           	  case "0":
           	
           		  String cardno = tmpCardNo;
           		  String actdate = sysDate;
           		  String actflag = "2";
           		  String acttype = "0";        		  
           		  updateCrdCard1(cardno, actdate, actflag, acttype);
           		  createRecReport("成功");
           	  break;
           	  case "1":
           		
           		  String cardno1 = tmpCardNo;
           		  String actdate1 = "";
           		  String actflag1 = "1";
           		  String acttype1 = "";        		  
           		  updateCrdCard1(cardno1, actdate1, actflag1, acttype1);
           		  createRecReport("成功");
           	  break;     	  
           	}
           }else if(tmpStatusType.equals("2")) 
           {
           	switch(tmpCardStatus) {
       	       case "U":
       	      	String cardno = tmpCardNo; 
       	      	updateCcaCardBase(cardno); 
       	        createRecReport("成功");
       	       break;
       	       case "L":
       	  	 
       	      	String cardno1 = tmpCardNo;
       	      	String curcode1 = "2";
       	      	String optdate1 = sysDate;
       	      	String optreason1 = "C1";
       	      	
       	      	updateCrdCard2(cardno1,curcode1,optdate1,optreason1);
       	      	
       	        createRecReport("成功");
       	       break;     
       	       case "C":
          	    	String cardno2 = tmpCardNo;
          	    	String curcode2 = "3";
          	    	String optdate2 = sysDate;
          	    	String optreason2 = "J2";
          	    	
          	    	updateCrdCard2(cardno2,curcode2,optdate2,optreason2);
          	    	createRecReport("成功");
       	       break;    
       	       case "F":
           	    	String cardno3 = tmpCardNo;
              	    	String curcode3 = "5";
              	    	String optdate3 = sysDate;
              	    	String optreason3 = "M1";
              	    	
              	    	updateCrdCard2(cardno3,curcode3,optdate3,optreason3);
              	    	createRecReport("成功");
       	       break;      
       	       case "Q":
      	      	String cardno4 = tmpCardNo;
          	    	String curcode4 = "1";
          	    	String optdate4 = sysDate;
          	    	String optreason4 = "Q2";
          	    	
          	    	updateCrdCard2(cardno4,curcode4,optdate4,optreason4);
          	    	createRecReport("成功");
       	       break;        	     
       	  }       	
           }else if(tmpStatusType.equals("3"))
           {
           	//不做動作
        	   createRecReport("成功");
           }else if(tmpStatusType.equals("4"))
           {
           	String cardno = tmpCardNo;
           	String cardstatus = tmpCardStatus;
           	updateCrdCard3(cardno,cardstatus);
           	createRecReport("成功");
           }else {
        	   createRecReport("失敗，狀態類別有誤");
           }
        
        }
        
      
        return;
    }

	/***********************************************************************/
	int outPutTextFile() throws Exception {
		int fileNo = 0;

        sqlCmd  = "select max(substr(file_name, 22, 2)) file_no";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += " where file_name like ?";
        sqlCmd += "  and crt_date  = to_char(sysdate,'yyyymmdd') ";
        setString(1, "ICCRSQND.REC." + "%" + ".TXT");

		if (selectTable() > 0)
			fileNo = getValueInt("file_no");

		hNcccFilename = String.format("ICCRSQND.REC.%s%02d.TXT", sysDate, fileNo + 1);
		
		
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

		//outFileName = String.format("%serror/%s", fileFolderPath, hNcccFilename);
		outFileName = String.format("%s%s", fileFolderPath, hNcccFilename);
		outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");

		return 0;
	}

	/***********************************************************************/
	int checkFileCtl() throws Exception {
		int totalCount = 0;

		sqlCmd = "select count(*) totalCount ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
//      sqlCmd += " and crt_date = to_char(?,'yyyymmdd') ";
      setString(1, getFileName);
//      setString(2, queryDate1);
		int recordCnt = selectTable();

		if (recordCnt > 0)
			totalCount = getValueInt("totalCount");

		if (totalCount > 0) {
            showLogMessage("I", "", String.format("此檔案 = [" + getFileName + "]已處理過不可重複處理(crd_file_ctl)"));
			return (1);
		}
		return (0);
	}

	/**********************************************************************
	 *                          產生報表檔
	 ********************************************************************/	
	void createRecReport(String reason) throws Exception {

		ncccData1 = new Buf1();
		
		seq++;
		
		ncccData1.statusType = tmpStatusType;
		ncccData1.cardNo = tmpCardNo;
		ncccData1.effectPerion = tmpEffectPeriod;
		ncccData1.catdStatus = tmpCardStatus;
		ncccData1.modDate = tmpModDate;
		ncccData1.modTime = tmpModTime;
		ncccData1.modCode = tmpModCode;	
		ncccData1.reason = String.format("%-200s", reason);
		

		buf = ncccData1.allText();
//        lpar1.clear();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}


	/****************************************************************************
	*                  第一項檢測:檢核卡號是否存在於卡片資料檔                              * 
	****************************************************************************/
	public int checkCrdCard(String cardno) throws Exception {
		

	   sqlCmd = "select count(*) cnt ";
	   sqlCmd += " from CRD_CARD ";
	   sqlCmd += " where CARD_NO = ? ";
	   setString(1, cardno);

		
		int totalCount = 0;


		int recordCnt = selectTable();

		totalCount = getValueInt("cnt");

		if (totalCount < 1) {
            showLogMessage("I", "", String.format("["+cardno+"]卡號不存在卡片資料檔"));
			return 0;
		}
		return 1;
	}


	/****************************************************************************
	*                  第二項檢測:檢核卡號是否存在於授權卡片資料檔                            * 
	****************************************************************************/
	public int checkCcaCardBase(String cardno) throws Exception {
		   sqlCmd = "select count(*) cnt ";
		   sqlCmd += " from CCA_CARD_BASE ";
		   sqlCmd += " where CARD_NO = ? ";
		   setString(1, cardno);

			
			int totalCount = 0;


			int recordCnt = selectTable();

			totalCount = getValueInt("cnt");

			if (totalCount < 1) {
	            showLogMessage("I", "", String.format("["+cardno+"]卡號不存在授權卡片資料檔"));
				return 0;
			}
			return 1;		
	}
	
	
	/****************************************************************************
	*                  第三項檢測:檢核卡號是否存在於授權帳戶資料檔                           * 
	****************************************************************************/	
	public int checkCcaCardAcct(String cardno) throws Exception {
		   sqlCmd = "select count(*) cnt ";
		   sqlCmd += " from CCA_CARD_ACCT ";
		   sqlCmd += " where ACNO_P_SEQNO in ";
		   sqlCmd += " (select ACNO_P_SEQNO from CRD_CARD where CARD_NO = ? ) ";
		   setString(1, cardno);

			
			int totalCount = 0;


			int recordCnt = selectTable();

			totalCount = getValueInt("cnt");

			if (totalCount < 1) {
	            showLogMessage("I", "", String.format("["+cardno+"]卡號不存在授權帳戶資料檔"));
				return 0;
			}
			return 1;						
	}
	
	/****************************************************************************
	*                UPDATE CRD_CARD   FRO 卡片狀態 0,1                            * 
	****************************************************************************/
	public void updateCrdCard1(String cardno,String actdate,String actflag,String acttype) throws Exception {
		
		
		daoTable = "CRD_CARD";
		updateSQL = " ACTIVATE_DATE = ? ,";
		updateSQL += " ACTIVATE_FLAG = ? ,";
		updateSQL += " ACTIVATE_TYPE = ? ,";
		updateSQL += " MOD_USER = 'IcuD119',";
		updateSQL += " MOD_TIME = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
		updateSQL += " MOD_PGM = 'IcuD119' ";
		whereStr = " where CARD_NO = ? "; 
		
        setString(1, actdate);
        setString(2, actflag);
        setString(3, acttype);
        setString(4, sysDate + sysTime);
        setString(5, cardno);
        updateTable();
   
        return;
	}
	
	/****************************************************************************
	*                UPDATE CRD_CARD   FRO 卡片狀態 L,C,F,Q                        * 
	****************************************************************************/
	public void updateCrdCard2(String cardno,String curcode,String optdate,String optreason) throws Exception {
		daoTable = "CRD_CARD";
		updateSQL = " CURRENT_CODE = ? ,";
		updateSQL += " OPPOST_DATE = ? ,";
		updateSQL += " OPPOST_REASON = ? ,";
		updateSQL += " MOD_USER = 'IcuD119',";
		updateSQL += " MOD_TIME = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
		updateSQL += " MOD_PGM = 'IcuD119' ";
		whereStr = " where CARD_NO = ? "; 
		
        setString(1, curcode);
        setString(2, optdate);
        setString(3, optreason);
        setString(4, sysDate + sysTime);
        setString(5, cardno);
        updateTable();
        
        return;		
		
	}


	/****************************************************************************
	*                UPDATE CRD_CARD   FRO 狀態類別 4                        * 
	****************************************************************************/
	public void updateCrdCard3(String cardno,String cardstatus) throws Exception {
		daoTable = "CRD_CARD";
		updateSQL = " E_INVOICE_DEPOSIT_ACCOUNT = ? ,";
		updateSQL += " MOD_USER = 'IcuD119',";
		updateSQL += " MOD_TIME = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
		updateSQL += " MOD_PGM = 'IcuD119' ";
		whereStr = " where CARD_NO = ? "; 
		
        setString(1, cardstatus);
        setString(2, sysDate + sysTime);
        setString(3, cardno);
        updateTable();
        
        return;				
		
		
	}
	
	/****************************************************************************
	*                        UPDATE CCA_CARD_BASE                               * 
	****************************************************************************/
	public void updateCcaCardBase(String cardno) throws Exception {
		daoTable = "CCA_CARD_BASE";
		updateSQL = " SPEC_FLAG = 'N',";
		updateSQL += " SPEC_STATUS  = '09',";
		updateSQL += " SPEC_DATE  = to_char(sysdate,'yyyymmdd'),";
		updateSQL += " SPEC_TIME  = to_char(sysdate,'hhmmss'),";
		updateSQL += " SPEC_USER  = 'IcuD119' ";
		whereStr = " where card_no = ? ";
		
		setString(1, cardno);
        updateTable();
        
        return;				
	}
	
	
	/***********************************************************************/
	void insertFileCtl1(String fileName) throws Exception {

		setValue("file_name", fileName);
		setValue("crt_date", sysDate);
		setValue("trans_in_date", sysDate);
		setValue("record_cnt",String.valueOf(datacnt));
		daoTable = "crd_file_ctl";
		insertTable();
	}

	/***********************************************************************/
	void insertFileCtl() throws Exception {
		setValue("file_name", hNcccFilename);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", hRecCnt1);
		setValueInt("record_cnt", hRecCnt1);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			daoTable = "crd_file_ctl";
			updateSQL = "head_cnt = ?,";
			updateSQL += " record_cnt = ?,";
			updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
			whereStr = "where file_name = ? ";
			setInt(1, hRecCnt1);
			setInt(2, hRecCnt1);
			setString(3, hNcccFilename);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
			}
		}
	}

	/***********************************************************************/
	void procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      //commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
	      commFTP.hEriaLocalDir = String.format("/crdataupload");
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + hNcccFilename + " 開始傳送....");
	      int err_code = commFTP.ftplogName("NCR2EMP", "mput " + hNcccFilename);
	      
	      if (err_code != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + hNcccFilename + " 資料"+" errcode:"+err_code);
	          insertEcsNotifyLog(hNcccFilename);          
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
	void renameFile1(String removeFileName) throws Exception {
		String tmpstr1 = "/crdataupload/" + removeFileName;
		String tmpstr2 = "/crdataupload/backup/" + removeFileName + "." + sysDate;
		
		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	/****************************************************************************/	
	public static void main(String[] args) throws Exception {
		
		
		IcuD119 proc = new IcuD119();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		
		String statusType;
		String cardNo;
		String effectPerion;
		String catdStatus;
		String modDate;
		String modTime;
		String modCode;	
		String reason;


		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(statusType, 1);
			rtn += fixLeft(cardNo, 16);
			rtn += fixLeft(effectPerion, 4);
			rtn += fixLeft(catdStatus, 1);
			rtn += fixLeft(modDate, 8);
			rtn += fixLeft(modTime, 6);
			rtn += fixLeft(modCode, 1);
			rtn += fixLeft(reason, 200);
			return rtn;
		}

		String fixLeft(String str, int len) throws UnsupportedEncodingException {
			String spc = "";
			for (int i = 0; i < 100; i++)
				spc += " ";
			if (str == null)
				str = "";
			str = str + spc;
			byte[] bytes = str.getBytes("MS950");
			byte[] vResult = new byte[len];
			System.arraycopy(bytes, 0, vResult, 0, len);

			return new String(vResult, "MS950");
		}
	}

	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		ncccData1.statusType = comc.subMS950String(bytes, 0, 1);
		ncccData1.cardNo = comc.subMS950String(bytes, 1, 17);
		ncccData1.effectPerion = comc.subMS950String(bytes, 17, 21);
		ncccData1.catdStatus = comc.subMS950String(bytes, 21, 22);
		ncccData1.modDate = comc.subMS950String(bytes, 22, 30); 
		ncccData1.modTime = comc.subMS950String(bytes, 30, 36); 
		ncccData1.modCode = comc.subMS950String(bytes, 36, 37); 
		ncccData1.reason = comc.subMS950String(bytes, 37, 237);
	}

	/****************************************************************************/
	String fixAllLeft(String str, int len) throws UnsupportedEncodingException {
		String spc = "";
		for (int i = 0; i < 100; i++)
			spc += "　";
		if (str == null)
			str = "";
		str = str + spc;
		byte[] bytes = str.getBytes("MS950");
		byte[] vResult = new byte[len];
		System.arraycopy(bytes, 0, vResult, 0, len);
		return new String(vResult, "MS950");
	}

	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = fileFolderPath + removeFileName;
		String tmpstr2 = fileFolderPath +"backup/" + removeFileName + "." + sysDate;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

	/****************************************************************************/
	String[] getFieldValue(String rec, int[] parm) {
		int x = 0;
		int y = 0;
		byte[] bt = null;
		String[] ss = new String[parm.length];
		try {
			bt = rec.getBytes("MS950");
		} catch (Exception e) {
			showLogMessage("I", "", comc.getStackTraceString(e));
		}
		for (int i : parm) {
			try {
				ss[y] = new String(bt, x, i, "MS950");
			} catch (Exception e) {
				showLogMessage("I", "", comc.getStackTraceString(e));
			}
			y++;
			x = x + i;
		}
		return ss;
	}

	/****************************************************************************/
	private Map<String,Object> processDataRecord(String[] row, String[] dt) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		for (String s : dt) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}
}
