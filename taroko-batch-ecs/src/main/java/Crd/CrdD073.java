
/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/03/06  V1.00.01    Wilson     program initial                          *
 *  112/04/14  V1.00.02    Wilson     讀參數判斷是否由新系統編列票證卡號                                                  *
 *  112/05/11  V1.00.03    Wilson     update ips_card add oppost_date          *
 *  112/06/14  V1.00.04    Wilson     可讀取多個檔案                                                                                      *
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
public class CrdD073 extends AccessDAO {
  private final String progname = "一卡通製卡回饋檔處理程式 112/06/14  V1.00.04";

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
  double hIcdrAutoloadAmt = 0;
  
  String imrtIcCardSeqno = "";
  String imrtIpsCardNo = "";
  String imrtIssueCode = "";
  String imrtRwId = "";
  String imrtRwTime = "";
  String imrtEffcTime = "";
  String imrtCardVersion = "";
  String imrtRwStatus = "";
  String imrtAutopayFlag = "";
  String imrtMaxAutopay = "";
  String imrtMaxAmt = "";
  String imrtMaxAmtM = "";
  String imrtPersonalType = "";
  String imrtExpiryTime = "";
  String imrtPersonalId = "";
  String imrtCardNo = "";
  String imrtBankId = "";
  String imrtTicketType = "";
  String imrtAreaCode = "";
  String imrtIpsTime = "";
  String imrtIpsCumTime = "";
  int imrtAddPointTot = 0;
  String imrtS8TicketType = "";
  String imrtS8Unit = "";
  String imrtS8Sid = "";
  String imrtS8Amt = "";
  String imrtLocId = "";
  String imrtMentuuId = "";
  String imrtRwIsstc = "";
  String imrtTac = "";
  String imrtTacR = "";
  String imrtMakecardType = "";
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
  String hOldIpsCardNo = "";  
  String hIcdrCardNo = "";
  String hIcdrRowid = "";
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
      
//      if (checkPtrHoliday() != 0) {
//			exceptExit = 0;
//			comc.errExit("今日為假日,不執行此程式", "");
//      }

      openFile();
      
      selectIpsMakecardRtnTmp();

      // ==============================================
      // 固定要做的
      showLogMessage("I", "", String.format("程式執行結束, 檔案[%d]筆資料, 讀取[%d]筆資料", filecnt, readCnt));
      showLogMessage("I", "", String.format("一卡通信用卡[%d]筆資料, 成功[%d]筆, 失敗[%d]筆", creditCnt, creditSucCnt, creditErrCnt));
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

			if (iFileName.length() != 25)
				continue;
			
			if(!comc.getSubString(iFileName,0,11).equals("B2I001_0006"))  
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

      insertIpsMakecardTtnTmp();
            
    }
    closeInputText(in);
   
    renameFile(iFileName);
    
    commitDataBase();
    return 0;
  }

  /***********************************************************************/
  private void insertIpsMakecardTtnTmp() throws Exception {	  

	    setValue("ic_card_seqno", readData.icCardSeqno);
	    setValue("ips_card_no", readData.ipsCardNo);
	    setValue("issue_code", readData.issueCode);
	    setValue("rw_id", readData.rwId);
	    setValue("rw_time", readData.rwTime);
	    setValue("effc_time", readData.effcTime);
	    setValue("card_version", readData.cardVersion);
	    setValue("rw_status", readData.rwStatus);
	    setValue("autopay_flag", readData.autopayFlag);
	    setValue("max_autopay", readData.maxAutopay);
	    setValue("max_amt", readData.maxAmt);
	    setValue("max_amt_m", readData.maxAmtM);
	    setValue("personal_type", readData.personalType);
	    setValue("expiry_time", readData.expiryTime);
	    setValue("personal_id", readData.personalId);
	    setValue("personal_name", "");
	    setValue("card_no", readData.cardNo);
	    setValue("bank_id", readData.bankId);
	    setValue("ticket_type", readData.ticketType);
	    setValue("area_code", readData.areaCode);
	    setValue("ips_time", readData.ipsTime);
	    setValue("ips_cum_time", readData.ipsCumTime);
	    setValue("add_point_tot", readData.addPointTot);
	    setValue("s8_ticket_type", readData.s8TicketType);
	    setValue("s8_unit", readData.s8Unit);
	    setValue("s8_sid", readData.s8Sid);
	    setValue("s8_amt", readData.s8Amt);
	    setValue("loc_id", readData.locId);
	    setValue("mentuu_id", readData.mentuuId);
	    setValue("rw_isstc", readData.rwIsstc);
	    setValue("tac", readData.tac);
	    setValue("tac_r", readData.tacR);
	    setValue("makecard_type", readData.makecardType);	    
	    setValue("mod_user", javaProgram);
	    setValue("mod_time", sysDate + sysTime);
	    setValue("mod_pgm", javaProgram);
	    daoTable = "ips_makecard_rtn_tmp";
	    insertTable();

	  }

	  /***********************************************************************/
  private void selectIpsMakecardRtnTmp() throws Exception {
	  	  
	  selectSQL = " ic_card_seqno, "			  
			  + "  ips_card_no, "
			  + "  issue_code, "
			  + "  rw_id, "
			  + "  rw_time, "
			  + "  effc_time, "
			  + "  card_version, "
			  + "  rw_status, "
			  + "  autopay_flag, "
			  + "  max_autopay, "
			  + "  max_amt, "
			  + "  max_amt_m, "
			  + "  personal_type, "
			  + "  expiry_time, "
			  + "  personal_id, "
			  + "  personal_name, "
			  + "  card_no, "
			  + "  bank_id, "
			  + "  ticket_type, "
			  + "  area_code,  "
			  + "  ips_time, "
			  + "  ips_cum_time, "
			  + "  add_point_tot, "
			  + "  s8_ticket_type, "
			  + "  s8_unit, "
			  + "  s8_sid, "
			  + "  s8_amt, "
			  + "  loc_id, "
			  + "  mentuu_id,  "
			  + "  rw_isstc, "
			  + "  tac, "
			  + "  tac_r, "
			  + "  makecard_type,  "
			  + "  rowid as rowid  ";			 
	  daoTable = " ips_makecard_rtn_tmp ";	 
	  whereStr = " where post_flag <> 'Y' "  
			  + " order by rw_time ";
		openCursor();
		while(fetchTable()) {
			initIpsMakecardRtnTmp();
			imrtIcCardSeqno  = getValue("ic_card_seqno"); 
			imrtIpsCardNo    = getValue("ips_card_no");   
			imrtIssueCode    = getValue("issue_code"); 
			imrtRwId         = getValue("rw_id");
			imrtRwTime       = getValue("rw_time");     
			imrtEffcTime     = getValue("effc_time");     
			imrtCardVersion  = getValue("card_version");  
			imrtRwStatus     = getValue("rw_status");     
			imrtAutopayFlag  = getValue("autopay_flag");  
			imrtMaxAutopay   = getValue("max_autopay");   
			imrtMaxAmt       = getValue("max_amt");       
			imrtMaxAmtM      = getValue("max_amt_m");     
			imrtPersonalType = getValue("personal_type"); 
			imrtExpiryTime   = getValue("expiry_time");   
			imrtPersonalId   = getValue("personal_id");   
			imrtCardNo       = getValue("card_no");       
			imrtBankId       = getValue("bank_id");       
			imrtTicketType   = getValue("ticket_type");   
			imrtAreaCode     = getValue("area_code");     
			imrtIpsTime      = getValue("ips_time");      
			imrtIpsCumTime   = getValue("ips_cum_time");  
			imrtAddPointTot  = getValueInt("add_point_tot");   
			imrtS8TicketType = getValue("s8_ticket_type");
			imrtS8Unit       = getValue("s8_unit");       
			imrtS8Sid        = getValue("s8_sid");        
			imrtS8Amt        = getValue("s8_amt");        
			imrtLocId        = getValue("loc_id");        
			imrtMentuuId     = getValue("mentuu_id");     
			imrtRwIsstc      = getValue("rw_isstc");      
			imrtTac          = getValue("tac");           
			imrtTacR         = getValue("tac_r");         
			imrtMakecardType = getValue("makecard_type"); 			
			imrtRowid        = getValue("rowid");
			
			readCnt++;
			
			showLogMessage("I", "", String.format("讀取一卡通卡號[%s],信用卡號[%s]", imrtIpsCardNo,imrtCardNo));
							
			creditCnt++;
			
			selectPtrSysParm();
			
			getLittleEndian();
									
			boolean checkSelect = checkSelect();
					
			if(checkSelect == false) {						
				creditErrCnt++;						
				updateIpsMakecardRtnTmp();				        
				showLogMessage("E", "", String.format("該一卡通卡號[%s][%s]", imrtIpsCardNo, tmpPostResultDesc));				        
				continue;
			}
			
			if(tmpWfValue.equals("Y")) {
				updateIpsCdrpLog();
			}
			else {
				insertIpsCdrpLog();
			}			
			            				
			insertIpsCard();
			            				
			insertIpsB2i005Log();
																					
			creditSucCnt++;			
			
			tmpPostResultCode = "00";
			updateIpsMakecardRtnTmp();
		}	    
  }

	  /***********************************************************************/
  void initIpsMakecardRtnTmp() throws Exception {

	  imrtIcCardSeqno = ""; 
	  imrtIpsCardNo = "";   
	  imrtIssueCode = ""; 
	  imrtRwId = "";
	  imrtRwTime = "";    
	  imrtEffcTime = "";    
	  imrtCardVersion = ""; 
	  imrtRwStatus = "";    
	  imrtAutopayFlag = ""; 
	  imrtMaxAutopay = "";  
	  imrtMaxAmt = "";      
	  imrtMaxAmtM = "";     
	  imrtPersonalType = "";
	  imrtExpiryTime = "";  
	  imrtPersonalId = "";  
	  imrtCardNo = "";      
	  imrtBankId = "";      
	  imrtTicketType = "";  
	  imrtAreaCode = "";    
	  imrtIpsTime = "";     
	  imrtIpsCumTime = "";  
	  imrtAddPointTot = 0;  
	  imrtS8TicketType = "";
	  imrtS8Unit = "";      
	  imrtS8Sid = "";       
	  imrtS8Amt = "";       
	  imrtLocId = "";       
	  imrtMentuuId = "";    
	  imrtRwIsstc = "";     
	  imrtTac = "";         
	  imrtTacR = "";        
	  imrtMakecardType = "";
	  imrtRowid = "";
	  tmpPostResultCode = "";
	  tmpPostResultDesc = "";
  }

  /***********************************************************************/
  void getLittleEndian() throws Exception {
      hIcdrAutoloadAmt = 0;

      String a = "";

      a = imrtMaxAutopay;

      int n = 0;
      int sum1 = 0;
      int sum2 = 0;
      int plus = 16;
      int i;

      for (i = 0; i < 2; i++) {
          if (a.toCharArray()[i] == 'A')
              n = 10;
          else if (a.toCharArray()[i] == 'B')
              n = 11;
          else if (a.toCharArray()[i] == 'C')
              n = 12;
          else if (a.toCharArray()[i] == 'D')
              n = 13;
          else if (a.toCharArray()[i] == 'E')
              n = 14;
          else if (a.toCharArray()[i] == 'F')
              n = 15;
          else
              n = a.toCharArray()[i] - '0';

          sum1 = sum1 + n * plus;
          plus = plus / 16;
      }

      plus = 16;

      for (i = 2; i < 4; i++) {
          if (a.toCharArray()[i] == 'A')
              n = 10;
          else if (a.toCharArray()[i] == 'B')
              n = 11;
          else if (a.toCharArray()[i] == 'C')
              n = 12;
          else if (a.toCharArray()[i] == 'D')
              n = 13;
          else if (a.toCharArray()[i] == 'E')
              n = 14;
          else if (a.toCharArray()[i] == 'F')
              n = 15;
          else
              n = a.toCharArray()[i] - '0';
          sum2 = sum2 + n * plus;
          plus = plus / 16;
      }
      sum2 = sum2 << 8;

      hIcdrAutoloadAmt = sum1 + sum2;
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
		  boolean selectIpsCdrpLog = selectIpsCdrpLog();
			if(selectIpsCdrpLog == false) {
				return false;
			}
	  }
		
		boolean selectIpsCard = selectIpsCard();
		if(selectIpsCard == false) {
			return false;
		}
		
		boolean selectCrdEmboss = selectCrdEmboss();
		if(selectCrdEmboss == false) {
			return false;
		}
		
		return true;
  }
  
  /***********************************************************************/
  private boolean selectIpsCdrpLog() throws Exception {
	  	  	  
	  hIcdrCardNo = "";
	  hIcdrRowid = "";
      sqlCmd = "select card_no, " + "rowid as rowid " 
             + " from ips_cdrp_log " 
             + "where ips_card_no     = ? "
             + "  and vendor_date_rtn = ''";
      setString(1, imrtIpsCardNo);
      selectTable();
      if (notFound.equals("Y")) {
    	  tmpPostResultCode = "C1";	        
    	  tmpPostResultDesc = "讀取ips_cdrp_log找不到資料";
          return false;
      }
      hIcdrCardNo = getValue("card_no");
      hIcdrRowid   = getValue("rowid");
     
      return true;
  }
            
  /***********************************************************************/
  private boolean selectIpsCard() throws Exception {
	  int tmpCnt = 0;
	    
	  sqlCmd = "  select count(*) as cnt ";
	  sqlCmd += "   from ips_card   ";
	  sqlCmd += "  where ips_card_no  = ?   ";
	  setString(1, imrtIpsCardNo);
	  selectTable();
	  tmpCnt =  getValueInt("cnt");

	  if(tmpCnt > 0) {
    	  tmpPostResultCode = "C2";	        
    	  tmpPostResultDesc = "讀取ips_card資料已存在";
		  return false;
	  }		  
	  
	  return true;
  }
  
  /***********************************************************************/
  private boolean selectCrdEmboss() throws Exception {
      hCardCurrentCode = "";
      hCardId = "";
      hCardIdCode = "";
      hCardIdPSeqno = "";
      hMbosEmbossSource = "";
      hMbosEmbossReason = "";
      hCardNewBegDate = "";
      hCardNewEndDate = "";
      sqlCmd = "select decode(b.current_code, '', '0', b.current_code) as current_code, " 
             + "(select id_p_seqno from crd_idno where id_no = a.apply_id) as id_p_seqno, "
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
    	  tmpPostResultCode = "C3";	        
    	  tmpPostResultDesc = "讀取crd_emboss找不到資料";
          return false;
      }
      hCardCurrentCode = getValue("current_code");
      hCardId = getValue("apply_id");
      hCardIdCode = getValue("apply_id_code");
      hCardIdPSeqno = getValue("id_p_seqno");
      hMbosEmbossSource = getValue("emboss_source");
      hMbosEmbossReason = getValue("emboss_reason");
      hCardNewBegDate = getValue("valid_fm");
      hCardNewEndDate = getValue("valid_to");
      
      return true;
  }
   
  /***********************************************************************/
  void updateIpsCdrpLog() throws Exception {
      daoTable = "ips_cdrp_log";
      updateSQL = "ic_card_seqno       = ?,";
      updateSQL += "issue_code          = ?,";
      updateSQL += "rw_id               = ?,";
      updateSQL += "rw_time             = ?,";
      updateSQL += "effc_time           = ?,";
      updateSQL += "rw_status           = ?,";
      updateSQL += "autopay_flag        = ?,";
      updateSQL += "max_autopay         = ?,";
      updateSQL += "personal_type       = ?,";
      updateSQL += "expiry_time         = ?,";
      updateSQL += "bank_id             = ?,";
      updateSQL += "ticket_type         = ?,";
      updateSQL += "area_code           = ?,";
      updateSQL += "ips_time            = ?,";
      updateSQL += "ips_cum_time        = ?,";
      updateSQL += "add_point_tot       = ?,";
      updateSQL += "s8_ticket_type      = ?,";
      updateSQL += "s8_unit             = ?,";
      updateSQL += "s8_sid              = ?,";
      updateSQL += "s8_amt              = ?,";
      updateSQL += "loc_id              = ?,";
      updateSQL += "mentuu_id           = ?,";
      updateSQL += "rw_isstc            = ?,";
      updateSQL += "tac                 = ?,";
      updateSQL += "tac_r               = ?,";
      updateSQL += "vendor_date_rtn     = ?,";
      updateSQL += "mod_pgm             = ?,";
      updateSQL += "mod_time            = sysdate ";
      whereStr = "WHERE rowid      = ? ";
      setString(1, imrtIcCardSeqno);
      setString(2, imrtIssueCode);
      setString(3, imrtRwId);
      setString(4, imrtRwTime);
      setString(5, imrtEffcTime);
      setString(6, imrtRwStatus);
      setString(7, imrtAutopayFlag);
      setString(8, imrtMaxAutopay);
      setString(9, imrtPersonalType);
      setString(10, imrtExpiryTime);
      setString(11, imrtBankId);
      setString(12, imrtTicketType);
      setString(13, imrtAreaCode);
      setString(14, imrtIpsTime);
      setString(15, imrtIpsCumTime);
      setLong(16, imrtAddPointTot);
      setString(17, imrtS8TicketType);
      setString(18, imrtS8Unit);
      setString(19, imrtS8Sid);
      setString(20, imrtS8Amt);
      setString(21, imrtLocId);
      setString(22, imrtMentuuId);
      setString(23, imrtRwIsstc);
      setString(24, imrtTac);
      setString(25, imrtTacR);
      setString(26, sysDate);
      setString(27, javaProgram);
      setRowId(28, hIcdrRowid);
      updateTable();
  }

  /*************************************************************************/
  private void insertIpsCdrpLog() throws Exception {
	     daoTable = "ips_cdrp_log";
	     setValue("ips_card_no"       , imrtIpsCardNo);
	     setValue("card_no"           , imrtCardNo);
	     setValue("personal_id"       , imrtPersonalId);
	     setValue("personal_name"     , "");
	     setValue("ic_card_seqno"     , imrtIcCardSeqno); 
	     setValue("issue_code"        , imrtIssueCode);   
	     setValue("rw_id"             , imrtRwId);        
	     setValue("rw_time"           , imrtRwTime);      
	     setValue("effc_time"         , imrtEffcTime);    
	     setValue("rw_status"         , imrtRwStatus);    
	     setValue("autopay_flag"      , imrtAutopayFlag); 
	     setValue("max_autopay"       , imrtMaxAutopay);  
	     setValue("personal_type"     , imrtPersonalType);
	     setValue("expiry_time"       , imrtExpiryTime);  
	     setValue("bank_id"           , imrtBankId);      
	     setValue("ticket_type"       , imrtTicketType);  
	     setValue("area_code"         , imrtAreaCode);    
	     setValue("ips_time"          , imrtIpsTime);     
	     setValue("ips_cum_time"      , imrtIpsCumTime);  
	     setValueInt("add_point_tot"  , imrtAddPointTot); 
	     setValue("s8_ticket_type"    , imrtS8TicketType);
	     setValue("s8_unit"           , imrtS8Unit);      
	     setValue("s8_sid"            , imrtS8Sid);       
	     setValue("s8_amt"            , imrtS8Amt);       
	     setValue("loc_id"            , imrtLocId);       
	     setValue("mentuu_id"         , imrtMentuuId);    
	     setValue("rw_isstc"          , imrtRwIsstc);     
	     setValue("tac"               , imrtTac);         
	     setValue("tac_r"             , imrtTacR);        
	     setValue("vendor_date_rtn"   , sysDate);        
	     setValue("mod_time"          , sysDate + sysTime);
	     setValue("mod_pgm"           , javaProgram);
	     insertTable();

		}
	 
	 /***********************************************************************/
   void insertIpsCard() throws Exception {
      String hCurrentFlag = "";
      String hBusiBusinessDate1 = "";
      hOldIpsCardNo = "";

      sqlCmd = "select ips_card_no " 
              + "from ips_card " 
              + "where card_no = ? " 
              + "  and crt_date in ( select max(crt_date) from ips_card  where card_no  = ?)";
       setString(1, imrtCardNo);
       setString(2, imrtCardNo);
       if (selectTable() > 0) {
           hOldIpsCardNo = getValue("ips_card_no");
       }
       else {
            sqlCmd = "select old_card_no " + "from crd_card " + "where card_no = ? ";          
            setString(1, imrtCardNo);                     
            if (selectTable() > 0) {
        	   String hCardOldCardNo = getValue("old_card_no");
        	                  
        	   sqlCmd = "select ips_card_no " 
                       + "from ips_card " 
                       + "where card_no = ? " 
                       + "  and crt_date in ( select max(crt_date) from ips_card  where card_no  = ?)";
                setString(1, hCardOldCardNo);
                setString(2, hCardOldCardNo);
                if (selectTable() > 0) {
                    hOldIpsCardNo = getValue("ips_card_no");               
                }      
            }     
       }      

      daoTable = "ips_card";
      setValue("ips_card_no"    , imrtIpsCardNo);
      setValue("card_no"        , imrtCardNo);
      setValue("ic_card_seqno"  , imrtIcCardSeqno);
      setValue("ips_emboss_rsn" , hMbosEmbossSource);
      setValue("current_code"   , hCardCurrentCode);
      setValue("crt_date"       , sysDate);
      setValue("new_beg_date"   , hCardNewBegDate);
      setValue("new_end_date"   , hCardNewEndDate);
      setValue("autoload_flag"  , "Y");
      setValue("autoload_date", sysDate);
      setValue("old_ips_card_no", hOldIpsCardNo);
      setValue("mod_time"       , sysDate + sysTime);
      setValue("mod_pgm"        , javaProgram);
      setValueDouble("autoload_amt" , hIcdrAutoloadAmt);
      insertTable();

      if (hOldIpsCardNo.length() > 0) {
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

          daoTable   = "ips_card";
          updateSQL  = "new_ips_card_no = ?,";
          updateSQL += "ips_oppost_date = decode(cast(? as varchar(10)),'',ips_oppost_date, ?),";
          updateSQL += "oppost_date = decode(cast(? as varchar(10)),'',oppost_date, ?),";
          updateSQL += "current_code = decode(cast(? as varchar(10)), '' ,current_code , ?) ";         
          whereStr = " where ips_card_no = ?";
          setString(1, imrtIpsCardNo);
          setString(2, hBusiBusinessDate1);
          setString(3, hBusiBusinessDate1);
          setString(4, hBusiBusinessDate1);
          setString(5, hBusiBusinessDate1);
          setString(6, hCurrentFlag);
          setString(7, hCurrentFlag);
          setString(8, hOldIpsCardNo);
          updateTable();
      }
  }

  /***********************************************************************/
   void insertIpsB2i005Log() throws Exception {

       daoTable = "ips_b2i005_log";
       setValue("crt_date", sysDate);
       setValue("crt_time", sysTime);
       setValue("ips_card_no", imrtIpsCardNo);
       setValue("card_no", imrtCardNo);
       setValue("id_p_seqno", hCardIdPSeqno); 
       setValue("trans_type", "A");
       setValue("proc_flag", "N");
       setValue("mod_time", sysDate + sysTime);
       setValue("mod_pgm", javaProgram);
       insertTable();
   }

   /***********************************************************************/
  void updateIpsMakecardRtnTmp() throws Exception {
	  
      daoTable   = "ips_makecard_rtn_tmp ";
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
    CrdD073 proc = new CrdD073();
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
	  String firstCode;     /* 001_001 資料本文*/                                         
	  String icCardSeqno;   /* 002_033 晶片序號*/                                         
	  String ipsCardNo;     /* 034_044 一卡通卡號*/                                       
	  String issueCode;     /* 045_046 發卡單位編號*/                                     
	  String rwId;          /* 047_054 錄碼讀寫器編號*/                                   
	  String rwTime;        /* 055_068 錄碼日期時間*/                                     
	  String effcTime;      /* 069_082 卡片有效日期時間 */                                
	  String cardVersion;   /* 083_084 卡片版本*/                                         
	  String rwStatus;      /* 085_086 卡片狀態 */                                        
	  String autopayFlag;   /* 087_088 自動加值旗標   01:開啟  其他:關閉 */              
	  String maxAutopay;    /* 089_092 每次自動加值金額 */                               
	  String maxAmt;        /* 093_096 最大容許儲值金額 */                               
	  String maxAmtM;       /* 097_100 最高扣值交易容許金額 */                           
	  String personalType;  /* 101_102 個人身份別 */                                     
	  String expiryTime;    /* 103_116 個人化效期 */                                     
	  String personalId;    /* 117_128 持卡人ID */                                       
	  String cardNo;        /* 129_144 信用卡號 */	                                     
	  String bankId;        /* 145_146 卡片所屬業者代碼 */                               
	  String ticketType;    /* 147_148 高雄捷運票種 */                                   
	  String areaCode;      /* 149_150 高雄捷運區域代碼 */                               
	  String ipsTime;       /* 151_164 高雄捷運票卡使用期限 */                           
	  String ipsCumTime;    /* 165_178 高雄捷運積點有效期限 */                           
	  String addPointTot;   /* 179_184 高雄捷運積點 */                                   
	  String s8TicketType;  /* 185_186 公路客運票種 */                                   
	  String s8Unit;        /* 187_188 公路客運識別單位 */                               
	  String s8Sid;         /* 189_190 公路客運識別身份 */                               
	  String s8Amt;         /* 191_194 公路客運優惠上限 */                               
	  String locId;         /* 195_196 錄碼卡廠代碼 */                                   
	  String mentuuId;      /* 197_206 錄碼設備代碼 */                                   
	  String rwIsstc;       /* 207_214 錄碼序號 */                                       
	  String filler;        /* 215_242 預留*/                                            
	  String tac;           /* 243_250 TAC */                                            
	  String tacR;          /* 251_251 TAC 驗證結果 */                                   
	  String makecardType;  /* 252_252 製卡類別   N: 新卡  C: 年度續卡  R: 其他補發卡 */ 
  }

  /***********************************************************************/
  private void splitBuf1(String str) throws UnsupportedEncodingException {
    byte[] bytes = str.getBytes("MS950");
    readData.firstCode = comc.subMS950String(bytes, 0, 1).trim();
    readData.icCardSeqno = comc.subMS950String(bytes, 1, 32).trim();
    readData.ipsCardNo = comc.subMS950String(bytes, 33, 11).trim();
    readData.issueCode = comc.subMS950String(bytes, 44, 2).trim();
    readData.rwId = comc.subMS950String(bytes, 46, 8).trim();
    readData.rwTime = comc.subMS950String(bytes, 54, 14).trim();
    readData.effcTime = comc.subMS950String(bytes, 68, 14).trim();
    readData.cardVersion = comc.subMS950String(bytes, 82, 2).trim();
    readData.rwStatus = comc.subMS950String(bytes, 84, 2).trim();
    readData.autopayFlag = comc.subMS950String(bytes, 86, 2).trim();
    readData.maxAutopay = comc.subMS950String(bytes, 88, 4).trim();
    readData.maxAmt = comc.subMS950String(bytes, 92, 4).trim();
    readData.maxAmtM = comc.subMS950String(bytes, 96, 4).trim();
    readData.personalType = comc.subMS950String(bytes, 100, 2).trim();
    readData.expiryTime = comc.subMS950String(bytes, 102, 14).trim();
    readData.personalId = comc.subMS950String(bytes, 116, 12).trim();
    readData.cardNo = comc.subMS950String(bytes, 128, 16).trim();
    readData.bankId = comc.subMS950String(bytes, 144, 2).trim();
    readData.ticketType = comc.subMS950String(bytes, 146, 2).trim();
    readData.areaCode = comc.subMS950String(bytes, 148, 2).trim();
    readData.ipsTime = comc.subMS950String(bytes, 150, 14).trim();
    readData.ipsCumTime = comc.subMS950String(bytes, 164, 14).trim();
    readData.addPointTot = comc.subMS950String(bytes, 178, 6).trim();
    readData.s8TicketType = comc.subMS950String(bytes, 184, 2).trim();
    readData.s8Unit = comc.subMS950String(bytes, 186, 2).trim();
    readData.s8Sid = comc.subMS950String(bytes, 188, 2).trim();
    readData.s8Amt = comc.subMS950String(bytes, 190, 4).trim();
    readData.locId = comc.subMS950String(bytes, 194, 2).trim();
    readData.mentuuId = comc.subMS950String(bytes, 196, 10).trim();
    readData.rwIsstc = comc.subMS950String(bytes, 206, 8).trim();
    readData.filler = comc.subBIG5String(bytes, 214, 28).trim();
    readData.tac = comc.subMS950String(bytes, 242, 8).trim();
    readData.tacR = comc.subMS950String(bytes, 250, 1).trim();
    readData.makecardType = comc.subMS950String(bytes, 251, 1).trim();
  }

}
