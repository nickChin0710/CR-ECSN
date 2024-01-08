
/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/03/06  V1.00.01    Wilson     program initial                          *
 *  112/03/07  V1.00.02    Wilson     新增procFTP給AP1                           *
 *  112/04/14  V1.00.03    Wilson     讀參數判斷是否由新系統編列票證卡號                                                  *
 *  112/05/09  V1.00.04    Wilson     delete id_no_code                        *
 *  112/05/10  V1.00.05    Wilson     調整updateTscCard、updateTscVdCard條件                  *
 *  112/05/11  V1.00.06    Wilson     update tsc_card add oppost_date          *
 *  112/06/14  V1.00.07    Wilson     可讀取多個檔案                                                                                      *
 ******************************************************************************/
package Crd;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/* CARDLINK悠遊信用卡製卡回饋檔處理程式 */
public class CrdD072 extends AccessDAO {
  private final String progname = "悠遊卡製卡回饋檔處理程式 112/06/14  V1.00.07";

  int debug = 0;
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;
  CommFTP commFTP = null;
  CommRoutine comr = null;

  Buf1 readData = new Buf1();
  int linecnt = 0;
  int filecnt = 0;
  int readCnt = 0;
  int creditCnt = 0;
  int debitCnt = 0;
  int creditSucCnt = 0;
  int debitSucCnt = 0;
  int creditErrCnt = 0;
  int errorVdCnt = 0;
  String queryDate = "";
  String hBusiBusinessDate = "";
  String fileName = "";
  String fileName1 = "";
  String iFileName = "";
  
  String tmrtPersonalId = "";
  String tmrtTscCardNo = "";
  String tmrtTscValidFrom = "";
  String tmrtGroupcode = "";
  String tmrtCardNo = "";
  String tmrtAcctNo = "";
  String tmrtEmbossDate = "";
  String tmrtIcSeqNo = "";
  int tmrtTscAmt = 0;
  int tmrtTscPledgeAmt = 0;
  String tmrtIsamSeqNo = "";
  String tmrtIsamBatchNo = "";
  String tmrtIsamBatchSeq = "";
  int tmrtAutoloadAmt = 0;
  String tmrtTscVendorCd = "";
  String tmrtMakecardstatus = "";
  String tmrtCardStatus = "";
  String tmrtMakecardType = "";
  String tmrtPersonalName = "";
  String tmrtAutoloadFlag = "";
  String tmrtRowid = "";
  String tmpDebitFlag = "";
  String tmpOkFlag = "";
  String tmpPostResultCode = "";
  String tmpPostResultDesc = "";
  
  String hCdrpCardNo = "";
  String hCdrpEmbossDate = "";
  String hCdrpVendorDateTo = "";
  String hCdrpTscEmbossRsn = "";
  String hCdrpTscVendorCd = "";
  String hCdrpRowid = "";
  
  String hCardCurrentCode  = "";
  String hCardId            = "";
  String hCardIdCode       = "";
  String hMbosEmbossSource = "";
  String hMbosEmbossReason = "";
  String hCardNewBegDate  = "";
  String hCardNewEndDate  = "";
  String hCardInMainDate  = "";
  String hCardToVendorDate  = "";
  String hIdnoBirthday  = "";
  String hIdnoIdPSeqno = "";
  String tmpWfValue = "";
  String tmpTscEmbossRsn = "";
  String tmpAutoloadFlag = "";
  String tmpOldTscCardNo = "";
  int tempInt = 0;
  
  String fileFolderPath = comc.getECSHOME() + "/media/crd/";

  public int mainProcess(String[] args) {

    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname + "," + args.length);
      // =====================================
      
      // 固定要做的

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      
      selectPtrBusinday();
      
//      // 若沒有給定查詢日期，則查詢日期為系統日
//      if(args.length == 0) {
//          queryDate = hBusiBusinessDate;
//      }else
//      if(args.length == 1) {
//          if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
//              showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
//              return -1;
//          }
//          queryDate = args[0];
//      }else {
//          comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
//      }           
      
//      if (checkPtrHoliday() != 0) {
//			exceptExit = 0;
//			comc.errExit("今日為假日,不執行此程式", "");
//      }

      openFile();
      		      
      selectTscMakecardRtnTmp();

      // ==============================================
      // 固定要做的
      showLogMessage("I", "", String.format("程式執行結束, 檔案[%d]筆資料, 讀取[%d]筆資料", filecnt, readCnt));
      showLogMessage("I", "", String.format("悠遊信用卡[%d]筆資料, 成功[%d]筆, 失敗[%d]筆", creditCnt, creditSucCnt, creditErrCnt));
      showLogMessage("I", "", String.format("悠遊VD卡[%d]筆資料, 成功[%d]筆, 失敗[%d]筆", debitCnt, debitSucCnt, errorVdCnt));
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

		sqlCmd = " select to_char(sysdate,'yyyymmdd') as sysdate ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		hBusiBusinessDate = getValue("sysdate");
	}

	/************************************************************************/
//  int checkPtrHoliday() throws Exception {
//      int holidayCount = 0;
//
//      sqlCmd = "select count(*) holidayCount ";
//      sqlCmd += " from ptr_holiday  ";
//      sqlCmd += "where holiday = ? ";
//      setString(1, queryDate);
//      int recordCnt = selectTable();      
//      if (notFound.equals("Y")) {
//          comc.errExit("select_ptr_holiday not found!", "");
//      }
//      if (recordCnt > 0) {
//          holidayCount = getValueInt("holidayCount");
//      }
//
//      if (holidayCount > 0) {
//          return 1;
//      } else {
//          return 0;
//      }
//  }

  /***********************************************************************/
	int openFile() throws Exception {
		int fileCount = 0;
		
		List<String> listOfFiles = comc.listFS(fileFolderPath, "", "");

//				final String fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", "F00600000", "ICCRSQND",
//				new CommDate().getLastTwoTWDate(queryDate), queryDate.substring(4, 8)); // 檔案正規表達式		
		
//		final String fileNameTemplate = String.format("%s\\.%s\\..*", "F00600000", "ICCRSQND"); // 檔案正規表達式		
		
		if (listOfFiles.size() > 0)
		for (String file : listOfFiles) {
			iFileName = file;

			if (iFileName.length() != 19)
				continue;
			
			if(!comc.getSubString(iFileName,8,15).equals("_TSCCFB"))  
			{
//				System.out.println(file+" NOT MAP "+fileNameTemplate);
				continue;
			}
			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(iFileName);
		}
		if (fileCount < 1) {
			showLogMessage("I", "", "無檔案可處理");
			
//			comcr.hCallErrorDesc = "無檔案可處理";
//            comcr.errRtn("無檔案可處理","處理日期 = " + queryDate  , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
    int checkFileCtl() throws Exception {
        sqlCmd = "select count(*) as all_cnt ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name like ?  ";
        setString(1, iFileName);
        selectTable();
        if (debug == 1)
            showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") > 0) {
            showLogMessage("D", "", " 此檔案已存在,不可重複轉入 =[" + iFileName + "]");
            return 1;
        }

        return 0;
    }    
    
 // ************************************************************************
    int readFile(String fileName) throws Exception {

    String readstr = "";
    String errRsn = "";
    String fileName2;
    fileName2 = fileFolderPath + fileName;
    
    int in = openInputText(fileName2);
    if (in == -1) {
    	return 1;  
	}

	showLogMessage("I", "", " Process file path =[" + fileFolderPath + " ]");
	showLogMessage("I", "", " Process file =[" + fileName + "]");
    
    /* read input file */
       
    while (true) {
      readstr =  readTextFile(in);
      
      if (endFile[in].equals("Y"))
			break; // break while loop
      
      if (debug == 1)
        showLogMessage("I", "", "readstr=[" + readstr + "]" + linecnt);

      linecnt++;
      
      /* HEADER */
      if (linecnt == 1) {
    	  continue;
      }
      
      splitBuf1(readstr);
      
      filecnt++;

      insertTscMakecardTtnTmp();      
    }
    
    closeInputText(in);
   
    commFTP = new CommFTP(getDBconnect(), getDBalias());	    
    comr = new CommRoutine(getDBconnect(), getDBalias());	    
    procFTP();	    
    renameFile(iFileName);
    
    commitDataBase(); 
    return 0;
  }

  /***********************************************************************/
  private void insertTscMakecardTtnTmp() throws Exception {	  

	    setValue("personal_id", readData.personalId);
	    setValue("tsc_card_no", readData.tscCardNo);
	    setValue("tsc_valid_from", readData.tscValidFrom);
	    setValue("group_code", readData.groupCode);
	    setValue("card_no", readData.cardNo);
	    setValue("acct_no", readData.acctNo);
	    setValue("emboss_date", readData.embossDate);
	    setValue("ic_seq_no", readData.icSeqNo);
	    setValue("tsc_amt", readData.tscAmt);
	    setValue("tsc_pledge_amt", readData.tscPledgeAmt);
	    setValue("isam_seq_no", readData.isamSeqNo);
	    setValue("isam_batch_no", readData.isamBatchNo);
	    setValue("isam_batch_seq", readData.isamBatchSeq);
	    setValue("autoload_amt", readData.autoloadAmt);
	    setValue("tsc_vendor_cd", readData.tscVendorCd);
	    setValue("makecard_status", readData.makecardStatus);
	    setValue("card_status", readData.cardStatus);
	    setValue("makecard_type", readData.makecardType);
	    setValue("personal_name", readData.personalName);
	    setValue("autoload_flag", readData.autoloadFlag);
	    setValue("mod_user", javaProgram);
	    setValue("mod_time", sysDate + sysTime);
	    setValue("mod_pgm", javaProgram);
	    daoTable = "tsc_makecard_rtn_tmp";
	    insertTable();

	  }
  
  /***********************************************************************/
  void procFTP() throws Exception {  	
	  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
      commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
      commFTP.hEriaLocalDir = String.format("%s/media/crd", comc.getECSHOME());
      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
      commFTP.hEflgModPgm = javaProgram;
      

      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
      showLogMessage("I", "", "mput " + iFileName + " 開始傳送....");
      int errCode = commFTP.ftplogName("NCR2TCB", "mput " + iFileName);
      
      if (errCode != 0) {
          showLogMessage("I", "", "ERROR:無法傳送 " + iFileName + " 資料"+" errcode:"+errCode);
          insertEcsNotifyLog(iFileName);          
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
  private void selectTscMakecardRtnTmp() throws Exception {
	  	  
	  selectSQL = " personal_id, "			  
			  + "  tsc_card_no, "
			  + "  tsc_valid_from, "
			  + "  group_code, "
			  + "  card_no, "
			  + "  acct_no, "
			  + "  emboss_date, "
			  + "  ic_seq_no, "
			  + "  tsc_amt, "
			  + "  tsc_pledge_amt, "
			  + "  isam_seq_no, "
			  + "  isam_batch_no, "
			  + "  isam_batch_seq, "
			  + "  autoload_amt, "
			  + "  tsc_vendor_cd, "
			  + "  makecard_status, "
			  + "  card_status, "
			  + "  makecard_type, "
			  + "  personal_name, "
			  + "  autoload_flag,  "
			  + "  rowid as rowid  ";			 
	  daoTable = " tsc_makecard_rtn_tmp ";	 
	  whereStr = " where post_flag <> 'Y' "  
			  + " order by emboss_date ";
		openCursor();
		while(fetchTable()) {
			initTscMakecardRtnTmp();
			tmrtPersonalId = getValue("personal_id");   
			tmrtTscCardNo = getValue("tsc_card_no");   
			tmrtTscValidFrom = getValue("tsc_valid_from");
			tmrtGroupcode = getValue("group_code");    
			tmrtCardNo = getValue("card_no");       
			tmrtAcctNo = getValue("acct_no");       
			tmrtEmbossDate = getValue("emboss_date");   
			tmrtIcSeqNo = getValue("ic_seq_no");     
			tmrtTscAmt = getValueInt("tsc_amt");       
			tmrtTscPledgeAmt = getValueInt("tsc_pledge_amt");
			tmrtIsamSeqNo = getValue("isam_seq_no");   
			tmrtIsamBatchNo = getValue("isam_batch_no"); 
			tmrtIsamBatchSeq = getValue("isam_batch_seq");
			tmrtAutoloadAmt = getValueInt("autoload_amt");  
			tmrtTscVendorCd = getValue("tsc_vendor_cd"); 
			tmrtMakecardstatus  = getValue("makecard_status");
			
			if(tmrtMakecardstatus.equals("0")) {
				tmpOkFlag = "Y";
			}
			else {
				tmpOkFlag = "N";
			}
			
			tmrtCardStatus  = getValue("card_status");			
			tmrtMakecardType  = getValue("makecard_type"); 
			tmrtPersonalName  = getValue("personal_name");
			tmrtAutoloadFlag  = getValue("autoload_flag");
			tmrtRowid  = getValue("rowid");
			
			readCnt++;
			
			showLogMessage("I", "", String.format("讀取悠遊卡號[%s],信用卡號[%s]", tmrtTscCardNo,tmrtCardNo));
			
			selectPtrSysParm();
			
			if(comc.getSubString(tmrtCardNo,0,6).equals("460199")) {
				tmpDebitFlag = "Y";
			}
			else {
				tmpDebitFlag = "N";
			}
					     		     
		     /* tsc_emboss_rsn */
		     if (tmrtMakecardType.equals("N")) {
		       tmpTscEmbossRsn = "1";
		     } else if (tmrtMakecardType.equals("C")) {
		       tmpTscEmbossRsn = "3";
		     } else if (tmrtMakecardType.equals("R")) {
		       tmpTscEmbossRsn = "5";
		     }
		     /* autoload_flag */
		     if (tmrtAutoloadFlag.equals("2")) {
		    	 tmpAutoloadFlag = "N";
		     } else if (tmrtAutoloadFlag.equals("4")) {
		    	 tmpAutoloadFlag = "Y";
		     }
			
			if(tmpDebitFlag.equals("Y")) {
				debitCnt++;
				
				boolean checkDbcEmboss = checkDbcEmboss();
				if(checkDbcEmboss == false) {
					errorVdCnt++;
					updateTscMakecardRtnTmp();
			        showLogMessage("E", "", String.format("該悠遊卡號[%s][%s]", tmrtTscCardNo, tmpPostResultDesc));
			        continue;
				}
				
				if(tmpOkFlag.equals("Y")) {					  					  
					boolean checkDebitOkSelect = checkDebitOkSelect();
					if(checkDebitOkSelect == false) {
						errorVdCnt++;
						updateTscMakecardRtnTmp();
				        showLogMessage("E", "", String.format("該悠遊卡號[%s][%s]", tmrtTscCardNo, tmpPostResultDesc));
				        continue;
					}
								
					if(tmpWfValue.equals("Y")) {
						updateTscDcrpLog();
					}
					else {
						insertTscDcrpLog();
					}

					insertTscVdCard();
					
					insertTscDcnrLog();
					
					insertTscDcpfLog();
		                   
		            if(tmpAutoloadFlag.equals("Y")) {
		            	insertTscDcaeLog();
		            }		               	  		            
				}
				else {					
					insertTscDcrpLogErr();
				}
				
				debitSucCnt++;
			}
			else {
				creditCnt++;
				
				boolean checkCrdEmboss = checkCrdEmboss();
				if(checkCrdEmboss == false) {
					creditErrCnt++;
					updateTscMakecardRtnTmp();
			        showLogMessage("E", "", String.format("該悠遊卡號[%s][%s]", tmrtTscCardNo, tmpPostResultDesc));
			        continue;
				}
				
				if(tmpOkFlag.equals("Y")) {
					boolean checkCreditOkSelect = checkCreditOkSelect();
					if(checkCreditOkSelect == false) {
						creditErrCnt++;
						updateTscMakecardRtnTmp();
				        showLogMessage("E", "", String.format("該悠遊卡號[%s][%s]", tmrtTscCardNo, tmpPostResultDesc));
				        continue;
					}
					
					if(tmpWfValue.equals("Y")) {
						updateTscCdrpLog();
					}
					else {
						insertTscCdrpLog();
					}
																				
					insertTscCard();
					
					insertTscCdnrLog();
					
					insertTscCdpfLog();
		                   
		            if(tmpAutoloadFlag.equals("Y")) {
		            	insertTscAecfLog();
		            }		            
				}
				else {					
					insertTscCdrpLogErr();					
				}
				
				creditSucCnt++;
			}
			
			tmpPostResultCode = "00";
			updateTscMakecardRtnTmp();
		}	    
  }

	  /***********************************************************************/
  void initTscMakecardRtnTmp() throws Exception {

	  tmrtPersonalId = "";
	  tmrtTscCardNo = "";
	  tmrtTscValidFrom = "";
	  tmrtGroupcode = "";
	  tmrtCardNo = "";
	  tmrtAcctNo = "";
	  tmrtEmbossDate = "";
	  tmrtIcSeqNo = "";
	  tmrtTscAmt = 0;
	  tmrtTscPledgeAmt = 0;
	  tmrtIsamSeqNo = "";
	  tmrtIsamBatchNo = "";
	  tmrtIsamBatchSeq = "";
	  tmrtAutoloadAmt = 0;
	  tmrtTscVendorCd = "";
	  tmrtMakecardstatus = "";
	  tmrtCardStatus = "";
	  tmrtMakecardType = "";
	  tmrtPersonalName = "";
	  tmrtAutoloadFlag = "";
	  tmrtRowid = "";
	  tmpDebitFlag = "";
	  tmpOkFlag = "";
	  tmpPostResultCode = "";
	  tmpPostResultDesc = "";
	  tmpTscEmbossRsn = "";
	  tmpAutoloadFlag = "";

  }

  /***********************************************************************/
  void selectPtrSysParm() throws Exception 
  {
    tmpWfValue = "N";
    
    sqlCmd  = "select wf_value ";
    sqlCmd += "  from ptr_sys_parm   ";
    sqlCmd += " where wf_parm = 'SYSPARM'  ";
    sqlCmd += "   and wf_key = 'ELEC_CARD_NO' ";
    int recordCnt = selectTable();
    if (recordCnt > 0) {
  	  tmpWfValue = getValue("wf_value");
    }
    return;
  }
  /***********************************************************************/
  private boolean checkDbcEmboss() throws Exception {
		
	  boolean selectDbcEmboss = selectDbcEmboss();
	  if(selectDbcEmboss == false) {
		  return false;
	  }

	  return true;
  }
  
  /***********************************************************************/
  private boolean checkDebitOkSelect() throws Exception {
		
	  if(tmpWfValue.equals("Y")) {
		  boolean selectTscDcrpLog = selectTscDcrpLog();
		  if(selectTscDcrpLog == false) {
			return false;
		  }
	  }
		
	  boolean selectTscVdCard = selectTscVdCard();		
	  if(selectTscVdCard == false) {
			return false;
	  }
		
	  return true;
  }
  
  /***********************************************************************/
  private boolean selectTscDcrpLog() throws Exception {

      hCdrpCardNo = "";
      hCdrpEmbossDate = "";
      hCdrpVendorDateTo = "";
      hCdrpTscEmbossRsn = "";
      hCdrpTscVendorCd = "";
      hCdrpRowid = "";
      sqlCmd  = "select card_no,";
      sqlCmd += "tsc_emboss_rsn,";
      sqlCmd += "tsc_vendor_cd,";
      sqlCmd += "emboss_date,";
      sqlCmd += "vendor_date_to,";
      sqlCmd += "rowid as rowid ";
      sqlCmd += " from tsc_dcrp_log  ";
      sqlCmd += "where tsc_card_no     = ?  ";
      sqlCmd += "  and vendor_date_rtn = '' ";
      setString(1, tmrtTscCardNo);
      int recordCnt = selectTable();
      if (notFound.equals("Y")) {  
    	  tmpPostResultCode = "B1";	        
    	  tmpPostResultDesc = "讀取tsc_dcrp_log找不到資料";
          return false;
      }
      if (recordCnt > 0) {
          hCdrpCardNo = getValue("card_no");
          hCdrpTscEmbossRsn = getValue("tsc_emboss_rsn");
          hCdrpTscVendorCd = getValue("tsc_vendor_cd");
          hCdrpEmbossDate = getValue("emboss_date");
          hCdrpVendorDateTo = getValue("vendor_date_to");
          hCdrpRowid = getValue("rowid");
      }
      
      return true;
 }
      /***********************************************************************/
  private boolean selectTscVdCard() throws Exception {
	  int tmpCnt = 0;
	    
	  sqlCmd = "  select count(*) as cnt ";
	  sqlCmd += "   from tsc_vd_card   ";
	  sqlCmd += "  where tsc_card_no  = ?   ";
	  setString(1, tmrtTscCardNo);
	  selectTable();
	  tmpCnt =  getValueInt("cnt");

	  if(tmpCnt > 0) {
    	  tmpPostResultCode = "B2";	        
    	  tmpPostResultDesc = "讀取tsc_vd_card資料已存在";
		  return false;
	  }		  
	  
	  return true;
  }
  
  /***********************************************************************/
 private boolean selectDbcEmboss() throws Exception {

      hCardCurrentCode  = "";
      hCardId            = "";
      hCardIdCode       = "";
      hMbosEmbossSource = "";
      hMbosEmbossReason = "";
      hCardNewBegDate  = "";
      hCardNewEndDate  = "";
      hCardInMainDate  = "";
      hCardToVendorDate  = "";
      sqlCmd  = "select ";
      sqlCmd += "apply_id,";
      sqlCmd += "apply_id_code,";
      sqlCmd += "emboss_source,";
      sqlCmd += "emboss_reason,";
      sqlCmd += "valid_fm,";
      sqlCmd += "valid_to, ";
      sqlCmd += "in_main_date, ";
      sqlCmd += "to_nccc_date ";
      sqlCmd += " from dbc_emboss  ";
      sqlCmd += "where card_no  = ?  ";
      sqlCmd += "  and in_main_date in ( select max(in_main_date) from dbc_emboss where card_no  = ?) ";
      setString(1, tmrtCardNo);
      setString(2, tmrtCardNo);
      int recordCnt = selectTable();
      if (notFound.equals("Y")) {
	        tmpPostResultCode = "B3";
	        tmpPostResultDesc = "讀取dbc_emboss找不到資料";
	        return false;
      }
      if (recordCnt > 0) {
          hCardCurrentCode  = "0";
          hCardId            = getValue("apply_id");
          hCardIdCode       = getValue("apply_id_code");
          hMbosEmbossSource = getValue("emboss_source");
          hMbosEmbossReason = getValue("emboss_reason");
          hCardNewBegDate  = getValue("valid_fm");
          hCardNewEndDate  = getValue("valid_to");
          hCardInMainDate  = getValue("in_main_date");
          hCardToVendorDate  = getValue("to_nccc_date");
      }
      return true;
  }

  /***********************************************************************/
 private void updateTscDcrpLog() throws Exception {

      daoTable   = "tsc_dcrp_log";
      updateSQL  = " ok_flag         = ?,";
      updateSQL += " tsc_amt         = ?,";
      updateSQL += " tsc_pledge_amt  = ?,";
      updateSQL += " ic_seq_no       = ?,";
      updateSQL += " isam_seq_no     = ?,";
      updateSQL += " isam_batch_no   = ?,";
      updateSQL += " isam_batch_seq  = ?,";
      updateSQL += " autoload_amt    = ?,";
      updateSQL += " autoload_flag   = ?,";
      updateSQL += " vendor_date_rtn = ?,";
      updateSQL += " vendor_emboss_date   = ?,";
      updateSQL += " mod_pgm              = ?,";
      updateSQL += " mod_time             = sysdate";
      whereStr   = "where tsc_card_no     = ?  ";
      whereStr  += "  and vendor_date_rtn = '' ";
      setString(1, tmpOkFlag);
      setDouble(2, tmrtTscAmt);
      setDouble(3, tmrtTscPledgeAmt);
      setString(4, tmrtIcSeqNo);
      setString(5, tmrtIsamSeqNo);
      setString(6, tmrtIsamBatchNo);
      setString(7, tmrtIsamBatchSeq);
      setDouble(8, tmrtAutoloadAmt);
      setString(9, tmpAutoloadFlag);
      setString(10, sysDate);
      setString(11, tmrtEmbossDate);
      setString(12, javaProgram);
      setString(13, tmrtTscCardNo);
      updateTable();
      
  }
 
  /***********************************************************************/
 private void insertTscDcrpLog() throws Exception {
     daoTable = "tsc_dcrp_log";
     setValue("tsc_card_no"       , tmrtTscCardNo);
     setValue("card_no"           , tmrtCardNo);
     setValue("tsc_emboss_rsn"    , tmpTscEmbossRsn);
     setValue("tsc_vendor_cd"     , tmrtTscVendorCd);
     setValue("emboss_date"       , hCardInMainDate);
     setValueLong("upper_lmt"     , 0);
     setValueLong("upper_lmt_acmm", 0);
     setValue("ok_flag"           , tmpOkFlag);
     setValueInt("tsc_amt"        , tmrtTscAmt);
     setValueInt("tsc_pledge_amt" , tmrtTscPledgeAmt);
     setValue("ic_seq_no"         , tmrtIcSeqNo);
     setValue("isam_seq_no"       , tmrtIsamSeqNo);
     setValue("isam_batch_no"     , tmrtIsamBatchNo);
     setValue("isam_batch_seq"    , tmrtIsamBatchSeq);
     setValueInt("autoload_amt"   , tmrtAutoloadAmt);
     setValue("autoload_flag"     , tmpAutoloadFlag);
     setValue("vendor_date_rtn"   , sysDate);
     setValue("vendor_emboss_date", tmrtEmbossDate);
     setValue("mod_time"          , sysDate + sysTime);
     setValue("mod_pgm"           , javaProgram);
     insertTable();

	}
 
 /***********************************************************************/
 private void insertTscVdCard() throws Exception {
	 String hCurrentFlag = "";
     String hBusiBusinessDate1 = "";
     tmpOldTscCardNo = "";
     
     sqlCmd = "  select tsc_card_no ";
      sqlCmd += "   from tsc_vd_card   ";
     sqlCmd += "  where vd_card_no  = ?   ";
     sqlCmd += "    and crt_date in ( select max(crt_date) from tsc_vd_card  ";
     sqlCmd += "                       where vd_card_no  = ?) ";
     setString(1, tmrtCardNo);
     setString(2, tmrtCardNo);
     if (selectTable() > 0) {
    	 tmpOldTscCardNo = getValue("tsc_card_no");
     } else {
     
       sqlCmd = "   select old_card_no ";
       sqlCmd += "    from dbc_card  ";
       sqlCmd += "   where card_no  = ? ";
       setString(1, tmrtCardNo);
       if (selectTable() > 0) {
         String tmpOldCardNo = getValue("old_card_no");
     
         sqlCmd = "  select tsc_card_no ";
         sqlCmd += "   from tsc_vd_card   ";
         sqlCmd += "  where vd_card_no  = ?  ";
         sqlCmd += "    and crt_date in ( select max(crt_date)  ";
         sqlCmd += "                        from tsc_vd_card where vd_card_no  = ?) ";
         setString(1, tmpOldCardNo);
         setString(2, tmpOldCardNo);
         if (selectTable() > 0) {
        	 tmpOldTscCardNo = getValue("tsc_card_no");
         }
       }
     }
     
     String tmpNewEndDate = "";
     
     /* new_end_date */
     tmpNewEndDate = comm.lastdateOfmonth("20" + tmrtTscValidFrom);
     
     setValue("tsc_card_no", tmrtTscCardNo);
     setValue("vd_card_no", tmrtCardNo);
     setValue("tsc_emboss_rsn", tmpTscEmbossRsn);
     setValue("current_code", "0");
     setValue("crt_date", sysDate);
     setValue("new_beg_date", sysDate.substring(0, 6) + "01");
     setValue("new_end_date", tmpNewEndDate);
     setValueInt("tsc_amt", tmrtTscAmt);
     setValueInt("tsc_pledge_amt", tmrtTscPledgeAmt);
     setValue("ic_seq_no", tmrtIcSeqNo);
     setValue("isam_seq_no", tmrtIsamSeqNo);
     setValue("isam_batch_no", tmrtIsamBatchNo);
     setValue("isam_batch_seq", tmrtIsamBatchSeq);
     setValueInt("autoload_amt", tmrtAutoloadAmt);
     setValue("autoload_flag", tmpAutoloadFlag);
     
     if(tmpAutoloadFlag.equals("Y")) {
    	 setValue("autoload_date", sysDate);
     }
     else {
    	 setValue("autoload_date", "");
     }
     
     setValue("old_tsc_card_no", tmpOldTscCardNo);
     setValue("combine_flag", "Y");
     setValue("tsc_sign_flag", "Y");
     setValue("tsc_sign_date", sysDate);
     setValue("mod_time", sysDate + sysTime);
     setValue("mod_pgm", javaProgram);
     daoTable = "tsc_vd_card";
     insertTable();
     
     if (tmpOldTscCardNo.length() > 0) {
         /****************************************************************/
         /* 1:新製卡 2:普昇金卡 3:換卡 4:緊急新製卡 5:毀損重製 6:掛失補發 */
         /* 7:緊急補發卡 8:星座卡毀損重製9:重送件 */
         /****************************************************************/
         hCurrentFlag = "";
         hBusiBusinessDate1 = "";
         if (hMbosEmbossSource.equals("5")) {
             if (hMbosEmbossReason.equals("2")) { /* 1:掛失 2:毀損 3:偽卡 */
                 hCurrentFlag = "6";
                 hBusiBusinessDate1 = hBusiBusinessDate;
             }
         }    	 
    	 
         daoTable  = "tsc_vd_card";
         updateSQL = "new_tsc_card_no = ?,";
         updateSQL += "tsc_oppost_date = decode(cast(? as varchar(10)),'',tsc_oppost_date, ?),";
         updateSQL += "oppost_date = decode(cast(? as varchar(10)),'',oppost_date, ?),";
         updateSQL += "current_code = decode(cast(? as varchar(10)), '' ,current_code , ?) ";  
         whereStr  = "where tsc_card_no  = ? ";
         setString(1, tmrtTscCardNo);
         setString(2, hBusiBusinessDate1);
         setString(3, hBusiBusinessDate1);
         setString(4, hBusiBusinessDate1);
         setString(5, hBusiBusinessDate1);
         setString(6, hCurrentFlag);
         setString(7, hCurrentFlag);
         setString(8, tmpOldTscCardNo);
         updateTable();
     }
 }
    
     /***********************************************************************/    
 private void insertTscDcnrLog() throws Exception {
     daoTable = "tsc_dcnr_log";
     setValue("tsc_card_no"       , tmrtTscCardNo);
     setValue("card_no"           , tmrtCardNo);
     setValue("tsc_emboss_rsn"    , tmpTscEmbossRsn);
     setValue("tsc_vendor_cd"     , tmrtTscVendorCd);
     setValue("emboss_date"       , hCardInMainDate);
     setValue("vendor_emboss_date", tmrtEmbossDate);
     setValueLong("upper_lmt"     , 0);
     setValueLong("upper_lmt_acmm", 0);
     setValue("vendor_date_to"    , hCardToVendorDate);
     setValue("vendor_date_rtn"   , sysDate);
     setValue("ok_flag"           , tmpOkFlag);
     setValueInt("tsc_amt"        , tmrtTscAmt);
     setValueInt("tsc_pledge_amt" , tmrtTscPledgeAmt);
     setValue("ic_seq_no"         , tmrtIcSeqNo);
     setValue("isam_seq_no"       , tmrtIsamSeqNo);
     setValue("isam_batch_no"     , tmrtIsamBatchNo);
     setValue("isam_batch_seq"    , tmrtIsamBatchSeq);
     setValueInt("autoload_amt"   , tmrtAutoloadAmt);
     setValue("autoload_flag"     , tmpAutoloadFlag);
     setValue("mod_time"          , sysDate + sysTime);
     setValue("mod_pgm"           , javaProgram);
     insertTable();          
 }

 /***********************************************************************/
 private void insertTscDcpfLog() throws Exception {
     String hTempX01 = "";

     hIdnoBirthday   = "";
     hIdnoIdPSeqno = "";

     sqlCmd += " SELECT birthday , id_p_seqno ";
     sqlCmd += "   FROM dbc_idno ";
     sqlCmd += "  WHERE id_no      = ? ";
     setString(1, hCardId);
     selectTable();

     hIdnoBirthday   = getValue("birthday");
     hIdnoIdPSeqno = getValue("id_p_seqno");


     switch (hMbosEmbossSource) {
     case "1":
     case "2":
         hTempX01 = "N";
         break;
     case "3":
     case "4":
         hTempX01 = "C";
         break;
     case "5":
         hTempX01 = "R";
         break;
     default:
         hTempX01 = "N";
         break;
     }
     setValue("crt_date", sysDate);
     setValue("crt_time", sysTime);
     setValue("tsc_card_no", tmrtTscCardNo);
     setValue("card_no"    , tmrtCardNo);
     setValue("tx_type"    , "A");
     setValue("tx_rsn"     , hTempX01);
//   setValue("id"         , h_card_id);
//   setValue("id_code"    , h_card_id_code);
     setValue("id_p_seqno" , hIdnoIdPSeqno);
     setValue("proc_flag"  , "N");
     setValue("mod_time"   , sysDate + sysTime);
     setValue("mod_pgm"    , javaProgram);
     daoTable = "tsc_dcpf_log";
     insertTable();
 }

 /***********************************************************************/
 private void insertTscDcaeLog() throws Exception {
	 
     daoTable = "tsc_dcae_log";
     setValue("tsc_card_no", tmrtTscCardNo);
     setValue("card_no", tmrtCardNo);
     setValue("vendor_emboss_date", tmrtEmbossDate);
     setValue("ic_seq_no", tmrtIcSeqNo);
     setValue("birthday", hIdnoBirthday);
     setValue("mod_time", sysDate + sysTime);
     setValue("mod_pgm", javaProgram);

     insertTable();
 }

 /***********************************************************************/
 private boolean insertTscDcrpLogErr() throws Exception {

     setValue("tsc_card_no", tmrtTscCardNo);
     setValue("card_no", tmrtCardNo);
     setValue("tsc_emboss_rsn", tmpTscEmbossRsn);
     setValue("tsc_vendor_cd", tmrtTscVendorCd);
     setValue("emboss_date", hCardInMainDate);
     setValue("vendor_emboss_date", tmrtEmbossDate);
     setValueDouble("tsc_amt", tmrtTscAmt);
     setValueDouble("tsc_pledge_amt", tmrtTscPledgeAmt);
     setValue("ic_seq_no", tmrtIcSeqNo);
     setValue("isam_seq_no", tmrtIsamSeqNo);
     setValue("isam_batch_no", tmrtIsamBatchNo);
     setValue("isam_batch_seq", tmrtIsamBatchSeq);
     setValueDouble("autoload_amt", tmrtAutoloadAmt);
     setValue("autoload_flag", tmpAutoloadFlag);
     setValue("vendor_date_to", hCardToVendorDate);
     setValue("vendor_date_rtn", sysDate);
     setValue("ok_flag", "N");
     setValue("mod_time", sysDate + sysTime);
     setValue("mod_pgm", javaProgram);
     daoTable = "tsc_dcrp_log";
     insertTable();
     if (dupRecord.equals("Y")) {
   	  return false;
     }
     
     return true;
 }

 /***********************************************************************/
 private boolean checkCrdEmboss() throws Exception {
					
	 boolean selectCrdEmboss = selectCrdEmboss();	
	 if(selectCrdEmboss == false) {	
		 return false;	
	 }
	
	 return true;
 }
 
 /***********************************************************************/
 private boolean checkCreditOkSelect() throws Exception {
		
	 if(tmpWfValue.equals("Y")) { 
		 boolean selectTscCdrpLog = selectTscCdrpLog();
		 if(selectTscCdrpLog == false) {	
			 return false;
		 }
	 }
		
	 boolean selectTscCard = selectTscCard();	
	 if(selectTscCard == false) {
		 return false;
	 }
				
	 return true;
 }
 
 /***********************************************************************/
  private boolean selectTscCdrpLog() throws Exception {

       hCdrpCardNo = "";
       hCdrpEmbossDate = "";
       hCdrpVendorDateTo = "";
       hCdrpTscEmbossRsn = "";
       hCdrpTscVendorCd = "";
       hCdrpRowid = "";
       sqlCmd  = "select card_no,";
       sqlCmd += "tsc_emboss_rsn,";
       sqlCmd += "tsc_vendor_cd,";
       sqlCmd += "emboss_date,";
       sqlCmd += "vendor_date_to,";
       sqlCmd += "rowid as rowid ";
       sqlCmd += " from tsc_cdrp_log  ";
       sqlCmd += "where tsc_card_no     = ?  ";
       sqlCmd += "  and vendor_date_rtn = '' ";
       setString(1, tmrtTscCardNo);
       int recordCnt = selectTable();
       if (notFound.equals("Y")) {
	       tmpPostResultCode = "A1";
	       tmpPostResultDesc = "讀取tsc_cdrp_log找不到資料";
           return false;
       }
       if (recordCnt > 0) {
           hCdrpCardNo = getValue("card_no");
           hCdrpTscEmbossRsn = getValue("tsc_emboss_rsn");
           hCdrpTscVendorCd = getValue("tsc_vendor_cd");
           hCdrpEmbossDate = getValue("emboss_date");
           hCdrpVendorDateTo = getValue("vendor_date_to");
           hCdrpRowid = getValue("rowid");
       }
       
       return true;
  }
       /***********************************************************************/
  private boolean selectTscCard() throws Exception {
	  int tmpCnt = 0;
	    
	  sqlCmd = "  select count(*) as cnt ";
	  sqlCmd += "   from tsc_card   ";
	  sqlCmd += "  where tsc_card_no  = ?   ";
	  setString(1, tmrtTscCardNo);
	  selectTable();
	  tmpCnt =  getValueInt("cnt");

	  if(tmpCnt > 0) {
    	  tmpPostResultCode = "A2";	        
    	  tmpPostResultDesc = "讀取tsc_card資料已存在";
		  return false;
	  }		  
	  
	  return true;
  }
  
  /***********************************************************************/
  private boolean selectCrdEmboss() throws Exception {

       hCardCurrentCode  = "";
       hCardId            = "";
       hCardIdCode       = "";
       hMbosEmbossSource = "";
       hCardNewBegDate  = "";
       hCardNewEndDate  = "";
       hCardInMainDate = "";
       hCardToVendorDate = "";
       sqlCmd  = "select ";
       sqlCmd += "apply_id,";
       sqlCmd += "apply_id_code,";
       sqlCmd += "emboss_source,";
       sqlCmd += "emboss_reason,";
       sqlCmd += "valid_fm,";
       sqlCmd += "valid_to, ";
       sqlCmd += "in_main_date, ";
       sqlCmd += "to_vendor_date ";
       sqlCmd += " from crd_emboss  ";
       sqlCmd += "where card_no  = ?  ";
       sqlCmd += "  and in_main_date in ( select max(in_main_date) from crd_emboss where card_no  = ?) ";
       setString(1, tmrtCardNo);
       setString(2, tmrtCardNo);
       int recordCnt = selectTable();
       if (notFound.equals("Y")) {
	       tmpPostResultCode = "A3";
	       tmpPostResultDesc = "讀取crd_emboss找不到資料";
    	   return false;
       }
       if (recordCnt > 0) {
           hCardCurrentCode  = "0";
           hCardId            = getValue("apply_id");
           hCardIdCode       = getValue("apply_id_code");
           hMbosEmbossSource = getValue("emboss_source");
           hMbosEmbossReason = getValue("emboss_reason");
           hCardNewBegDate  = getValue("valid_fm");
           hCardNewEndDate  = getValue("valid_to");
           hCardInMainDate  = getValue("in_main_date");
           hCardToVendorDate  = getValue("to_vendor_date");
       }
       
       return true;
   }

   /***********************************************************************/
  private void updateTscCdrpLog() throws Exception {

       daoTable   = "tsc_cdrp_log";
       updateSQL  = " ok_flag         = ?,";
       updateSQL += " tsc_amt         = ?,";
       updateSQL += " tsc_pledge_amt  = ?,";
       updateSQL += " ic_seq_no       = ?,";
       updateSQL += " isam_seq_no     = ?,";
       updateSQL += " isam_batch_no   = ?,";
       updateSQL += " isam_batch_seq  = ?,";
       updateSQL += " autoload_amt    = ?,";
       updateSQL += " autoload_flag   = ?,";
       updateSQL += " vendor_date_rtn = ?,";
       updateSQL += " vendor_emboss_date   = ?,";
       updateSQL += " mod_pgm              = ?,";
       updateSQL += " mod_time             = sysdate";
       whereStr   = "where tsc_card_no     = ?  ";
       whereStr  += "  and vendor_date_rtn = '' ";
       setString(1, tmpOkFlag);
       setDouble(2, tmrtTscAmt);
       setDouble(3, tmrtTscPledgeAmt);
       setString(4, tmrtIcSeqNo);
       setString(5, tmrtIsamSeqNo);
       setString(6, tmrtIsamBatchNo);
       setString(7, tmrtIsamBatchSeq);
       setDouble(8, tmrtAutoloadAmt);
       setString(9, tmpAutoloadFlag);
       setString(10, sysDate);
       setString(11, tmrtEmbossDate);
       setString(12, javaProgram);
       setString(13, tmrtTscCardNo);
       updateTable();
  }
   /***********************************************************************/
  private void insertTscCdrpLog() throws Exception {
	     daoTable = "tsc_cdrp_log";
	     setValue("tsc_card_no"       , tmrtTscCardNo);
	     setValue("card_no"           , tmrtCardNo);
	     setValue("tsc_emboss_rsn"    , tmpTscEmbossRsn);
	     setValue("tsc_vendor_cd"     , tmrtTscVendorCd);
	     setValue("emboss_date"       , hCardInMainDate);
	     setValueLong("upper_lmt"     , 0);
	     setValueLong("upper_lmt_acmm", 0);
	     setValue("ok_flag"           , tmpOkFlag);
	     setValueInt("tsc_amt"        , tmrtTscAmt);
	     setValueInt("tsc_pledge_amt" , tmrtTscPledgeAmt);
	     setValue("ic_seq_no"         , tmrtIcSeqNo);
	     setValue("isam_seq_no"       , tmrtIsamSeqNo);
	     setValue("isam_batch_no"     , tmrtIsamBatchNo);
	     setValue("isam_batch_seq"    , tmrtIsamBatchSeq);
	     setValueInt("autoload_amt"   , tmrtAutoloadAmt);
	     setValue("autoload_flag"     , tmpAutoloadFlag);
	     setValue("vendor_date_rtn"   , sysDate);
	     setValue("vendor_emboss_date", tmrtEmbossDate);
	     setValue("mod_time"          , sysDate + sysTime);
	     setValue("mod_pgm"           , javaProgram);
	     insertTable();

		}
	 
	 /***********************************************************************/
  private void insertTscCard() throws Exception {
	  String hCurrentFlag = "";
      String hBusiBusinessDate1 = "";
      tmpOldTscCardNo = "";
      
      sqlCmd = "  select tsc_card_no ";
      sqlCmd += "   from tsc_card   ";
      sqlCmd += "  where card_no  = ?   ";
      sqlCmd += "    and crt_date in ( select max(crt_date) from tsc_card  ";
      sqlCmd += "                       where card_no  = ?) ";
      setString(1, tmrtCardNo);
      setString(2, tmrtCardNo);
      if (selectTable() > 0) {
        tmpOldTscCardNo = getValue("tsc_card_no");
      } else {
      
        sqlCmd = "   select old_card_no ";
        sqlCmd += "    from crd_card  ";
        sqlCmd += "   where card_no  = ? ";
        setString(1, tmrtCardNo);
        if (selectTable() > 0) {
          String tmpOldCardNo = getValue("old_card_no");
      
          sqlCmd = "  select tsc_card_no ";
          sqlCmd += "   from tsc_card   ";
          sqlCmd += "  where card_no  = ?  ";
          sqlCmd += "    and crt_date in ( select max(crt_date)  ";
          sqlCmd += "                        from tsc_card where card_no  = ?) ";
          setString(1, tmpOldCardNo);
          setString(2, tmpOldCardNo);
          if (selectTable() > 0) {
            tmpOldTscCardNo = getValue("tsc_card_no");
          }
        }
      }
      
      String tmpNewEndDate = "";
      
      /* new_end_date */
      tmpNewEndDate = comm.lastdateOfmonth("20" + tmrtTscValidFrom);
      
      setValue("tsc_card_no", tmrtTscCardNo);
      setValue("card_no", tmrtCardNo);
      setValue("tsc_emboss_rsn", tmpTscEmbossRsn);
      setValue("current_code", "0");
      setValue("crt_date", sysDate);
      setValue("new_beg_date", sysDate.substring(0, 6) + "01");
      setValue("new_end_date", tmpNewEndDate);
      setValueInt("tsc_amt", tmrtTscAmt);
      setValueInt("tsc_pledge_amt", tmrtTscPledgeAmt);
      setValue("ic_seq_no", tmrtIcSeqNo);
      setValue("isam_seq_no", tmrtIsamSeqNo);
      setValue("isam_batch_no", tmrtIsamBatchNo);
      setValue("isam_batch_seq", tmrtIsamBatchSeq);
      setValueInt("autoload_amt", tmrtAutoloadAmt);

      if(tmpAutoloadFlag.equals("Y")) {
     	 setValue("autoload_date", sysDate);
      }
      else {
     	 setValue("autoload_date", "");
      }

      setValue("old_tsc_card_no", tmpOldTscCardNo);
      setValue("combine_flag", "Y");
      setValue("tsc_sign_flag", "Y");
      setValue("tsc_sign_date", sysDate);
      setValue("mod_time", sysDate + sysTime);
      setValue("mod_pgm", javaProgram);
      daoTable = "tsc_card";
      insertTable();
      
      if (tmpOldTscCardNo.length() > 0) {
          /****************************************************************/
          /* 1:新製卡 2:普昇金卡 3:換卡 4:緊急新製卡 5:毀損重製 6:掛失補發 */
          /* 7:緊急補發卡 8:星座卡毀損重製9:重送件 */
          /****************************************************************/
          hCurrentFlag = "";
          hBusiBusinessDate1 = "";
          if (hMbosEmbossSource.equals("5")) {
              if (hMbosEmbossReason.equals("2")) { /* 1:掛失 2:毀損 3:偽卡 */
                  hCurrentFlag = "6";
                  hBusiBusinessDate1 = hBusiBusinessDate;
              }
          }
    	  
          daoTable  = "tsc_card";
          updateSQL = "new_tsc_card_no = ?,";
          updateSQL += "tsc_oppost_date = decode(cast(? as varchar(10)),'',tsc_oppost_date, ?),";
          updateSQL += "oppost_date = decode(cast(? as varchar(10)),'',oppost_date, ?),";
          updateSQL += "current_code = decode(cast(? as varchar(10)), '' ,current_code , ?) ";  
          whereStr  = "where tsc_card_no  = ? ";
          setString(1, tmrtTscCardNo);
          setString(2, hBusiBusinessDate1);
          setString(3, hBusiBusinessDate1);
          setString(4, hBusiBusinessDate1);
          setString(5, hBusiBusinessDate1);
          setString(6, hCurrentFlag);
          setString(7, hCurrentFlag);
          setString(8, tmpOldTscCardNo);
          updateTable();
      }
  }
      /***********************************************************************/    
  private void insertTscCdnrLog() throws Exception {
      
	     daoTable = "tsc_cdnr_log";
	     setValue("tsc_card_no"       , tmrtTscCardNo);
	     setValue("card_no"           , tmrtCardNo);
	     setValue("tsc_emboss_rsn"    , tmpTscEmbossRsn);
	     setValue("tsc_vendor_cd"     , tmrtTscVendorCd);
	     setValue("emboss_date"       , hCardInMainDate);
	     setValue("vendor_emboss_date", tmrtEmbossDate);
	     setValueLong("upper_lmt"     , 0);
	     setValueLong("upper_lmt_acmm", 0);
	     setValue("vendor_date_to"    , hCardToVendorDate);
	     setValue("vendor_date_rtn"   , sysDate);
	     setValue("ok_flag"           , tmpOkFlag);
	     setValueInt("tsc_amt"        , tmrtTscAmt);
	     setValueInt("tsc_pledge_amt" , tmrtTscPledgeAmt);
	     setValue("ic_seq_no"         , tmrtIcSeqNo);
	     setValue("isam_seq_no"       , tmrtIsamSeqNo);
	     setValue("isam_batch_no"     , tmrtIsamBatchNo);
	     setValue("isam_batch_seq"    , tmrtIsamBatchSeq);
	     setValueInt("autoload_amt"   , tmrtAutoloadAmt);
	     setValue("autoload_flag"     , tmpAutoloadFlag);
	     setValue("mod_time"          , sysDate + sysTime);
	     setValue("mod_pgm"           , javaProgram);
	     insertTable();	          
	 }

  /***********************************************************************/
  private void insertTscAecfLog() throws Exception {

      daoTable = "tsc_aecf_log";
      setValue("tsc_card_no", tmrtTscCardNo);
      setValue("card_no", tmrtCardNo);
      setValue("vendor_emboss_date", tmrtEmbossDate);
      setValue("ic_seq_no", tmrtIcSeqNo);
      setValue("birthday", hIdnoBirthday);
      setValue("mod_time", sysDate + sysTime);
      setValue("mod_pgm", javaProgram);

      insertTable();      
  }

  /***********************************************************************/
  private boolean insertTscCdpfLog() throws Exception {
      String hTempX01 = "";

      hIdnoBirthday   = "";
      hIdnoIdPSeqno = "";

      sqlCmd += " SELECT birthday , id_p_seqno ";
      sqlCmd += "   FROM crd_idno ";
      sqlCmd += "  WHERE id_no      = ? ";
      setString(1, hCardId);
      selectTable();

      hIdnoBirthday   = getValue("birthday");
      hIdnoIdPSeqno = getValue("id_p_seqno");

      switch (hMbosEmbossSource) {
      case "1":
      case "2":
          hTempX01 = "N";
          break;
      case "3":
      case "4":
          hTempX01 = "C";
          break;
      case "5":
          hTempX01 = "R";
          break;
      default:
          hTempX01 = "N";
          break;
      }
      setValue("crt_date", sysDate);
      setValue("crt_time", sysTime);
      setValue("tsc_card_no", tmrtTscCardNo);
      setValue("card_no"    , tmrtCardNo);
      setValue("tx_type"    , "A");
      setValue("tx_rsn"     , hTempX01);
//    setValue("id"         , h_card_id);
//    setValue("id_code"    , h_card_id_code);
      setValue("id_p_seqno" , hIdnoIdPSeqno);
      setValue("proc_flag"  , "N");
      setValue("mod_time"   , sysDate + sysTime);
      setValue("mod_pgm"    , javaProgram);
      daoTable = "tsc_cdpf_log";
      insertTable();
      if (dupRecord.equals("Y")) {
          return false;
      }

      return true;
  }

  /***********************************************************************/
  private boolean insertTscCdrpLogErr() throws Exception {

      setValue("tsc_card_no", tmrtTscCardNo);
      setValue("card_no", tmrtCardNo);
      setValue("tsc_emboss_rsn", tmpTscEmbossRsn);
      setValue("tsc_vendor_cd", tmrtTscVendorCd);
      setValue("emboss_date", hCardInMainDate);
      setValue("vendor_emboss_date", tmrtEmbossDate);
      setValueDouble("tsc_amt", tmrtTscAmt);
      setValueDouble("tsc_pledge_amt", tmrtTscPledgeAmt);
      setValue("ic_seq_no", tmrtIcSeqNo);
      setValue("isam_seq_no", tmrtIsamSeqNo);
      setValue("isam_batch_no", tmrtIsamBatchNo);
      setValue("isam_batch_seq", tmrtIsamBatchSeq);
      setValueDouble("autoload_amt", tmrtAutoloadAmt);
      setValue("autoload_flag", tmpAutoloadFlag);
      setValue("vendor_date_to", hCardToVendorDate);
      setValue("vendor_date_rtn", sysDate);
      setValue("ok_flag", "N");
      setValue("mod_time", sysDate + sysTime);
      setValue("mod_pgm", javaProgram);
      daoTable = "tsc_cdrp_log";
      insertTable();
      if (dupRecord.equals("Y")) {
    	  return false;
      }
      
      return true;
  }

  /***********************************************************************/
  void updateTscMakecardRtnTmp() throws Exception {
	  
      daoTable   = "tsc_makecard_rtn_tmp ";
      updateSQL += "post_flag = 'Y', ";
      updateSQL += "post_date = ?, ";
      updateSQL += "post_result_code = ?, ";
      updateSQL += "post_result_desc = ?, ";
      updateSQL += "mod_user = ?, ";
      updateSQL += "mod_time = sysdate, ";
      updateSQL += "mod_pgm = ? ";
      whereStr   = "where rowid = ? ";
      setString(1, sysDate);
      setString(2, tmpPostResultCode);
      setString(3, tmpPostResultDesc);
      setString(4, javaProgram);
      setString(5, javaProgram);
      setRowId(6, tmrtRowid);
      updateTable();
      
      commitDataBase();
  }
  
  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    CrdD072 proc = new CrdD072();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

  /***********************************************************************/  
  void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = fileFolderPath + removeFileName;
		String tmpstr2 = fileFolderPath +"backup/" + removeFileName;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

  /****************************************************************************/
  class Buf1 {
    String personalId;/* 001_011 持卡人ID */
    String tscCardNo;/* 012_027 悠遊卡號 */
    String tscValidFrom;/* 028_031 悠遊卡有效期(信用卡有效期 YYMM) */
    String groupCode;/* 032_035 信用卡團體代碼 */
    String cardNo;/* 036_051 信用卡號 */
    String acctNo;/* 052_066 金融卡帳號 */
    String embossDate;/* 067_074 製卡日 */
    String icSeqNo;/* 075_094 悠遊卡晶片序號 */
    String tscAmt;/* 095_099 悠遊卡錢包金額 */
    String tscPledgeAmt;/* 100_104 悠遊卡押金 */
    String isamSeqNo;/* 105_112 ISAM晶片序號 */
    String isamBatchNo;/* 113_122 ISAM製卡批號 */
    String isamBatchSeq;/* 123_127 ISAM製卡序號 */
    String autoloadAmt;/* 128_131 悠遊卡AUTOLOAD金額 */
    String tscVendorCd;/* 132_141 卡廠名稱代碼 */
    String makecardStatus;/* 142_142 製卡狀態 */
    String cardStatus;/* 143_152 卡片狀態 */
    String makecardType;/* 153_153 製卡類別 */
    String personalName;/* 154_173 持卡人姓名 */
    String autoloadFlag;/* 174_174 自動加值功能開啟預設值(Y: 開啟, N: 關閉) */
  }

  /***********************************************************************/
  private void splitBuf1(String str) throws UnsupportedEncodingException {
    byte[] bytes = str.getBytes("MS950");
    readData.personalId = comc.subMS950String(bytes, 0, 11).trim();
    readData.tscCardNo = comc.subMS950String(bytes, 11, 16).trim();
    readData.tscValidFrom = comc.subMS950String(bytes, 27, 4).trim();
    readData.groupCode = comc.subMS950String(bytes, 31, 3).trim();
    readData.cardNo = comc.subMS950String(bytes, 34, 16).trim();
    readData.acctNo = comc.subMS950String(bytes, 50, 15).trim();
    readData.embossDate = comc.subMS950String(bytes, 65, 8).trim();
    readData.icSeqNo = comc.subMS950String(bytes, 73, 20).trim();
    readData.tscAmt = comc.subMS950String(bytes, 93, 5).trim();
    readData.tscPledgeAmt = comc.subMS950String(bytes, 98, 5).trim();
    readData.isamSeqNo = comc.subMS950String(bytes, 103, 8).trim();
    readData.isamBatchNo = comc.subMS950String(bytes, 111, 10).trim();
    readData.isamBatchSeq = comc.subMS950String(bytes, 121, 5).trim();
    readData.autoloadAmt = comc.subMS950String(bytes, 126, 4).trim();
    readData.tscVendorCd = comc.subMS950String(bytes, 130, 10).trim();
    readData.makecardStatus = comc.subMS950String(bytes, 140, 1).trim();
    readData.cardStatus = comc.subMS950String(bytes, 141, 10).trim();
    readData.makecardType = comc.subMS950String(bytes, 151, 1).trim();
    readData.personalName = comc.subMS950String(bytes, 152, 20).trim();
    readData.autoloadFlag = comc.subMS950String(bytes, 172, 1).trim();
  }

}
