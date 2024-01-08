
/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/03/06  V1.00.01    Wilson     program initial                          *
 *  112/04/14  V1.00.02    Wilson     讀參數判斷是否由新系統編列票證卡號                                                  *
 *  112/05/11  V1.00.03    Wilson     update ich_card add oppost_date          *
 *  112/05/24  V1.00.04    Wilson     add issue_date、issue_time、調整effc_date值    *
 *  112/06/14  V1.00.05    Wilson     可讀取多個檔案                                                                                      *
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
public class CrdD074 extends AccessDAO {
  private final String progname = "愛金卡製卡回饋檔處理程式 112/06/14  V1.00.05";

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
  String fileName1 = "";
  String iFileName = "";
  
  String imrtSearchCode = "";
  String imrtIssueStatus = "";
  String imrtRespCodeIssue = "";
  String imrtIssueTime = "";
  String imrtIcSeqIcah = "";
  String imrtFormatVer = "";
  String imrtIchCardNo = "";
  String imrtIsamUseCnt = "";
  String imrtCardNo = "";
  String imrtMakecardType = "";
  String imrtIsamNum = "";
  String imrtRowid = "";
  String tmpPostResultCode = "";
  String tmpPostResultDesc = "";
  
  String hCdrpCardNo = "";
  String hCdrpEmbossDate = "";
  String hCdrpVendorDateTo = "";
  String hCdrpRowid = "";
  String hCardCurrentCode  = "";
  String hCardId            = "";
  String hCardIdCode       = "";
  String hCardIdPSeqno       = "";
  String hMbosEmbossSource = "";
  String hMbosEmbossReason = "";
  String hCardNewBegDate  = "";
  String hCardNewEndDate  = "";
  String hIdnoIdPSeqno = "";
  
  String hOldIchCardNo = "";
  String hIcdrCardNo = "";
  String hIcdrRowid = "";
  int hIcdrAutoloadAmt = 0;
  String tmpWfValue = "";
  
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
//      
//      if (checkPtrHoliday() != 0) {
//			exceptExit = 0;
//			comc.errExit("今日為假日,不執行此程式", "");
//      }

      openFile();
      
      selectIchMakecardRtnTmp();

      // ==============================================
      // 固定要做的
      showLogMessage("I", "", String.format("程式執行結束, 檔案[%d]筆資料, 讀取[%d]筆資料", filecnt, readCnt));
      showLogMessage("I", "", String.format("愛金卡信用卡[%d]筆資料, 成功[%d]筆, 失敗[%d]筆", creditCnt, creditSucCnt, creditErrCnt));
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

			if (iFileName.length() != 23)
				continue;
			
			if(!comc.getSubString(iFileName,0,9).equals("B02B_0006"))  
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
            
      splitBuf1(readstr);
      
      if(!readData.firstCode.equals("D")) {
    	  continue;
      }
      
      filecnt++;

      insertIchMakecardTtnTmp();
            
    }
    closeInputText(in);
   
    renameFile(iFileName);
    
    commitDataBase();
    return 0;
  }

  /***********************************************************************/
  private void insertIchMakecardTtnTmp() throws Exception {	  

	    setValue("search_code", readData.searchCode);
	    setValue("issue_status", readData.issueStatus);
	    setValue("resp_code_issue", readData.respCodeIssue);
	    setValue("issue_time", readData.issueTime);
	    setValue("ic_seq_icah", readData.icSeqIcah);
	    setValue("format_ver", readData.formatVer);
	    setValue("ich_card_no", readData.ichCardNo);
	    setValue("isam_use_cnt", readData.isamUseCnt);
	    setValue("card_no", readData.cardNo);
	    setValue("makecard_type", readData.makecardType);
	    setValue("isam_num", readData.isamNum);
	    setValue("mod_user", javaProgram);
	    setValue("mod_time", sysDate + sysTime);
	    setValue("mod_pgm", javaProgram);
	    daoTable = "ich_makecard_rtn_tmp";
	    insertTable();
	  }

	  /***********************************************************************/
  private void selectIchMakecardRtnTmp() throws Exception {
	  	  
	  selectSQL = " search_code, "			  
			  + "  issue_status, "
			  + "  resp_code_issue, "
			  + "  issue_time, "
			  + "  ic_seq_icah, "
			  + "  format_ver, "
			  + "  ich_card_no, "
			  + "  isam_use_cnt, "
			  + "  card_no, "
			  + "  makecard_type, "
			  + "  isam_num, "
			  + "  rowid as rowid  ";			 
	  daoTable = " ich_makecard_rtn_tmp ";	 
	  whereStr = " where post_flag <> 'Y' "  
			  + " order by issue_time ";
		openCursor();
		while(fetchTable()) {
			initIchMakecardRtnTmp();
			imrtSearchCode    = getValue("search_code");    
			imrtIssueStatus   = getValue("issue_status");   
			imrtRespCodeIssue = getValue("resp_code_issue");
			imrtIssueTime     = getValue("issue_time");     
			imrtIcSeqIcah     = getValue("ic_seq_icah");    
			imrtFormatVer     = getValue("format_ver");     
			imrtIchCardNo     = getValue("ich_card_no");    
			imrtIsamUseCnt    = getValue("isam_use_cnt");   
			imrtCardNo        = getValue("card_no");        
			imrtMakecardType  = getValue("makecard_type");  
			imrtIsamNum       = getValue("isam_num");       
			imrtRowid        = getValue("rowid");
			
			readCnt++;
			
			showLogMessage("I", "", String.format("讀取愛金卡卡號[%s][%s]", imrtIchCardNo,imrtCardNo));
							
			creditCnt++;
									
			boolean checkSelect = checkSelect();
					
			if(checkSelect == false) {						
				creditErrCnt++;						
				updateIchMakecardRtnTmp();				        
				showLogMessage("E", "", String.format("該愛金卡卡號[%s][%s]", imrtIchCardNo, tmpPostResultDesc));				        
				continue;
			}
			          
			if(tmpWfValue.equals("Y")) {
				updateIchB07bCard();
			}
			else {
				insertIchB07bCard();
			}
            
            insertIchCard();
            
            insertIchB02bFback();
																					
			creditSucCnt++;			
			
			tmpPostResultCode = "00";
			updateIchMakecardRtnTmp();
		}	    
  }

	  /***********************************************************************/
  void initIchMakecardRtnTmp() throws Exception {
	  
	  imrtSearchCode = "";   
	  imrtIssueStatus = "";  
	  imrtRespCodeIssue = "";
	  imrtIssueTime = "";    
	  imrtIcSeqIcah = "";    
	  imrtFormatVer = "";    
	  imrtIchCardNo = "";    
	  imrtIsamUseCnt = "";   
	  imrtCardNo = "";       
	  imrtMakecardType = ""; 
	  imrtIsamNum = ""; 
	  imrtRowid = "";
	  tmpPostResultCode = "";
	  tmpPostResultDesc = "";
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
  private boolean checkSelect() throws Exception {
		
	  if(tmpWfValue.equals("Y")) {
		  boolean selectIchB07bCard = selectIchB07bCard();
			if(selectIchB07bCard == false) {
				return false;
			}
	  }
		
		boolean selectIchCard = selectIchCard();
		if(selectIchCard == false) {
			return false;
		}
		
		boolean selectCrdEmboss = selectCrdEmboss();
		if(selectCrdEmboss == false) {
			return false;
		}
		
		return true;
  }
  
  /***********************************************************************/
  private boolean selectIchB07bCard() throws Exception {

if(debug==1) showLogMessage("I", "", String.format( "   222 card_no=[%s]",imrtIchCardNo));
      hIcdrCardNo = "";
      hIcdrRowid = "";
      sqlCmd = "select card_no, " + "rowid as rowid " 
             + "  from ich_b07b_card " 
             + " where ich_card_no     = ? "
             + "   and vendor_date_rtn = ''";
      setString(1, imrtIchCardNo);
      selectTable();
      if (notFound.equals("Y")) {
    	  tmpPostResultCode = "D1";	        
    	  tmpPostResultDesc = "讀取ich_card資料已存在";
		  return false;

      }
      hIcdrCardNo = getValue("card_no");
      hIcdrRowid   = getValue("rowid");
      
      return true;
  }
      
  /***********************************************************************/
  private boolean selectIchCard() throws Exception {
	  int tmpCnt = 0;
	    
	  sqlCmd = "  select count(*) as cnt ";
	  sqlCmd += "   from ich_card   ";
	  sqlCmd += "  where ich_card_no  = ?   ";
	  setString(1, imrtIchCardNo);
	  selectTable();
	  tmpCnt =  getValueInt("cnt");

	  if(tmpCnt > 0) {
    	  tmpPostResultCode = "D2";	        
    	  tmpPostResultDesc = "讀取ich_card資料已存在";
		  return false;
	  }		  
	  
	  return true;
  }
  
  /***********************************************************************/
  private boolean selectCrdEmboss() throws Exception {

      hCardCurrentCode = "";
      hCardId = "";
      hCardIdCode = "";
      hMbosEmbossSource = "";
      hMbosEmbossReason = "";
      hCardNewBegDate = "";
      hCardNewEndDate = "";
      sqlCmd = "select decode(b.current_code, '', '0', b.current_code) as current_code, " 
             + "a.apply_id, "      + "a.apply_id_code, " 
             + "a.emboss_source, " + "a.emboss_reason, " 
             + "a.valid_fm, "      + "a.valid_to "
             + " from crd_emboss a " 
             + " left join crd_card b on b.card_no  = a.card_no " 
             + "where a.card_no     = ? "
             + "  and a.in_main_date in ( select max(in_main_date) from crd_emboss  where card_no = ?)";
      setString(1, imrtCardNo);
      setString(2, imrtCardNo);
      selectTable();
      if (notFound.equals("Y")) {
    	  tmpPostResultCode = "D3";	        
    	  tmpPostResultDesc = "讀取crd_emboss找不到資料";
		  return false;
      }
      hCardCurrentCode = getValue("current_code");
      hCardId = getValue("apply_id");
      hCardIdCode = getValue("apply_id_code");
      hMbosEmbossSource = getValue("emboss_source");
      hMbosEmbossReason = getValue("emboss_reason");
      hCardNewBegDate = getValue("valid_fm");
      hCardNewEndDate = getValue("valid_to");

      if (hCardCurrentCode.length() == 0) {
          hCardCurrentCode = "0";
      }
      
      return true;
  }
            
  /***********************************************************************/
  void updateIchB07bCard() throws Exception {
      daoTable = "ich_b07b_card";
      updateSQL = " isam_num            = ? , ";
      updateSQL += "online_add          = '1', ";
      updateSQL += "offline_add         = '0', ";
      updateSQL += "vendor_date_rtn     = ?, ";
      updateSQL += "mod_pgm             = ?, ";
      updateSQL += "mod_time            = sysdate ";
      whereStr = "WHERE rowid      = ? ";
      setString(1, imrtIsamNum);
      setString(2, sysDate);
      setString(3, javaProgram);
      setRowId(4, hIcdrRowid);
      updateTable();
  }

  /**
   * @throws Exception
   *************************************************************************/
  private void insertIchB07bCard() throws Exception {
	     daoTable = "ich_b07b_card";
	     setValue("ich_card_no", imrtIchCardNo);
	     setValue("card_no"    , imrtCardNo);
	     setValue("effc_date"  , hCardNewEndDate);
	     setValue("issue_date" , hBusiBusinessDate);
	     setValue("isam_num", imrtIsamNum);
	     setValue("online_add" , "1");
	     setValue("offline_add", "0");
	     setValue("vendor_date_rtn", sysDate);
	     setValue("mod_time"          , sysDate + sysTime);
	     setValue("mod_pgm"           , javaProgram);
	     insertTable();
		}
	 
	 /***********************************************************************/
  void insertIchCard() throws Exception {
      String hCurrentFlag = "";
      String hBusiBusinessDate1 = "";
      hOldIchCardNo = "";
      
      sqlCmd = "select ich_card_no " 
              + "  from ich_card " 
              + " where card_no = ? " 
              + "   and crt_date in ( select max(crt_date) from ich_card  where card_no  = ?)";
       setString(1, imrtCardNo);
       setString(2, imrtCardNo);
       if (selectTable() > 0) {
           hOldIchCardNo = getValue("ich_card_no");    
       }
       else {
           sqlCmd = "select old_card_no " + "from crd_card " + "where card_no = ? ";
           setString(1, imrtCardNo);
           if (selectTable() > 0) {
        	   String hCardOldCardNo = getValue("old_card_no");
                      
        	   sqlCmd = "select ich_card_no "                   
        			   + "  from ich_card "                    
        			   + " where card_no = ? "                    
        			   + "   and crt_date in ( select max(crt_date) from ich_card  where card_no  = ?)";                       
        	   setString(1, hCardOldCardNo);            
        	   setString(2, hCardOldCardNo);            
        	   if (selectTable() > 0) {               
        		   hOldIchCardNo = getValue("ich_card_no");                
        	   }       
           }                  
       }

       hIcdrAutoloadAmt = 500;
      
      daoTable = "ich_card";
      setValue("ich_card_no", imrtIchCardNo);
      setValue("card_no", imrtCardNo);
      setValue("ich_emboss_rsn", hMbosEmbossSource);
      setValue("current_code", hCardCurrentCode);
      setValue("crt_date", sysDate);
      setValue("new_beg_date", hCardNewBegDate);
      setValue("new_end_date", hCardNewEndDate);
      setValue("autoload_flag", "Y");
      setValue("autoload_date", sysDate);
      setValue("old_ich_card_no", hOldIchCardNo);
      setValue("mod_time", sysDate + sysTime);
      setValue("mod_pgm", javaProgram);
      setValueInt("autoload_amt", hIcdrAutoloadAmt);
      insertTable();

      if (hOldIchCardNo.length() > 0) {
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

          daoTable   = "ich_card";
          updateSQL  = "new_ich_card_no = ?,";
          updateSQL += "ich_oppost_date = decode(cast(? as varchar(10)),'',ich_oppost_date, ?),";
          updateSQL += "oppost_date = decode(cast(? as varchar(10)),'',oppost_date, ?),";
          updateSQL += "current_code = decode(cast(? as varchar(10)), '' ,current_code , ?) ";
          whereStr = " where ich_card_no = ?";
          setString(1, imrtIchCardNo);
          setString(2, hBusiBusinessDate1);
          setString(3, hBusiBusinessDate1);
          setString(4, hBusiBusinessDate1);
          setString(5, hBusiBusinessDate1);
          setString(6, hCurrentFlag);
          setString(7, hCurrentFlag);
          setString(8, hOldIchCardNo);
          updateTable();
      }
  }
   
  /***********************************************************************/
  void insertIchB02bFback() throws Exception {

      daoTable = "ich_b02b_fback";
      setValue("ich_card_no", imrtIchCardNo);
      setValue("card_no", imrtCardNo);
      setValue("search_code", imrtSearchCode);
      setValue("issue_status", imrtIssueStatus);
      setValue("resp_code_issue", imrtRespCodeIssue);
      setValue("issue_date", comc.getSubString(imrtIssueTime, 0, 10));
      setValue("issue_time", comc.getSubString(imrtIssueTime, 11, 19));
      setValue("ic_seq_icah", imrtIcSeqIcah);
      setValue("format_ver", imrtFormatVer);
      setValue("isam_use_cnt", imrtIsamUseCnt);
      setValue("PROC_FLAG", "N");
      setValue("mod_time", sysDate + sysTime);
      setValue("mod_pgm", javaProgram);

      insertTable();
  }

  /***********************************************************************/
  void updateIchMakecardRtnTmp() throws Exception {
	  
      daoTable   = "ich_makecard_rtn_tmp ";
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
      setRowId(6, imrtRowid);
      updateTable();
      
      commitDataBase();
  }
  
  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    CrdD074 proc = new CrdD074();
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
	  String firstCode;     /* 001_001 檔頭*/              
	  String searchCode;    /* 002_007 SEARCHCODE*/        
	  String filler1;       /* 008_008 FILLER*/            
	  String issueStatus;   /* 009_014 發卡狀態*/          
	  String filler2;       /* 015_015 FILLER*/            
	  String respCodeIssue; /* 016_020 製卡回應碼*/        
	  String filler3;       /* 021_021 FILLER*/            
	  String issueTime;     /* 022_040 發卡日期時間*/      
	  String filler4;       /* 041_041 FILLER*/            
	  String icSeqIcah;     /* 042_055 晶片序號*/          
	  String formatVer;     /* 056_063 格式版本*/          
	  String ichCardNo;     /* 064_079 愛金卡卡號*/        
	  String isamUseCnt;    /* 080_081 愛金卡基碼使用次數*/
	  String cardNo;        /* 082_097 信用卡卡號*/        
	  String makecardType;  /* 098_098 製卡註記*/          
	  String isamNum;       /* 099_112 ISAM號碼*/          
	  String filler5;       /* 113_120 FILLER*/                
  }

  /***********************************************************************/
  private void splitBuf1(String str) throws UnsupportedEncodingException {
    byte[] bytes = str.getBytes("MS950");
    readData.firstCode = comc.subMS950String(bytes, 0, 1).trim();
    readData.searchCode = comc.subMS950String(bytes, 1, 6).trim();
    readData.filler1 = comc.subMS950String(bytes, 7, 1).trim();
    readData.issueStatus = comc.subMS950String(bytes, 8, 6).trim();
    readData.filler2 = comc.subMS950String(bytes, 14, 1).trim();
    readData.respCodeIssue = comc.subMS950String(bytes, 15, 5).trim();
    readData.filler3 = comc.subMS950String(bytes, 20, 1).trim();
    readData.issueTime = comc.subMS950String(bytes, 21, 19).trim();
    readData.filler4 = comc.subMS950String(bytes, 40, 1).trim();
    readData.icSeqIcah = comc.subMS950String(bytes, 41, 14).trim();
    readData.formatVer = comc.subMS950String(bytes, 55, 8).trim();
    readData.ichCardNo = comc.subMS950String(bytes, 63, 16).trim();
    readData.isamUseCnt = comc.subMS950String(bytes, 79, 2).trim();
    readData.cardNo = comc.subMS950String(bytes, 81, 16).trim();
    readData.makecardType = comc.subMS950String(bytes, 97, 1).trim();
    readData.isamNum = comc.subMS950String(bytes, 98, 14).trim();
    readData.filler5 = comc.subMS950String(bytes, 112, 8).trim();
  }

}
