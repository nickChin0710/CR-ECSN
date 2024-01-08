/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/03/05  V1.00.01    Simon     program initial                           *
 *  112/03/07  V1.00.02    Simon     fixed refund txn_type & zero_amt control  *
 *                                   fixed CF、PF control                      *
 *                                   ftp to /crdatacrea/CREDITALL              *
 *  112/05/29  V1.00.03    Simon     add bil_bill.mcht_country                 *
 *  112/08/21  V1.00.04    Simon     1.改以ptr_businday.this_close_date判斷當日是否為關帳日*
 *                                   2.新增判斷 print_type="06" & acct_code="PF" 為 Txn_type "16"*
 *  112/08/22  V1.00.05    Simon     取代 hDestAmt 改以 hDcDestAmt 判斷 in figureTxnType()*
 *  112/09/25  V1.00.06    Simon     1.shell cyc002、cyc003並行執行日期控制    *
 *                                   2.區別寫入 hTxnType="24"(負餘額調整轉出)、 *
 *                                     "27"(繳款沖正類)                        *
 *  112/10/06  V1.00.07     Simon    調整 txn-21 為繳款沖正類(payment_reversal)或調整金額合計為正項金額，*
 *                                   txn-27 為溢付款轉出，txn-22 為調整金額合計為負項金額*
 *  112/10/25  V1.00.08     Simon    每隔 1,000 筆顯示處理筆數更改為每隔 100,000 筆顯示*
 ******************************************************************************/

package Cyc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
import java.util.*;
import java.text.Normalizer;

import com.*;

public class CycA700 extends AccessDAO {

  public final boolean DEBUG = false;

  private final String PROGNAME = "帳單CTRAN下傳檔處理程式 "
                                + "112/10/25  V1.00.08";
	private final static String FILE_NAME_TEMPLATE = "CTRAN_YYYYMMDD.TXT";
	private final static String MEDIA_FOLDER = "/media/cyc/";

  CommFunction   comm     = new CommFunction();
  CommCrd        comc     = new CommCrd();
  CommCrdRoutine comcr    = null;

  String prgmId             = "CycA700";
  String hCallBatchSeqno    = "";
  String buf                = "";

	String hCurrBusinessDate  = "";
	String hInputExeDateFlag  = "";
  String hPtrBusiRowid      = "";
  String hBusiBusinessDate  = "";
  String hWdayStmtCycle     = "";
  String hPseqno            = "";
  String hIdno              = "";
  String hCardNo            = "";
  String hAcctCode          = "";
  String hPrintType         = "";
  String hPrintSeq          = "";
  String hDummyCode         = "";
  String hTransDate         = "";
  String hEntryDate         = "";
  String hCurrCode          = "";
  String hTxnCode           = "";
  double hDcDestAmt         = 0.0;
  int    hDestAmt           = 0;
  String hDcDestAmtStr      = "";
  String hDestAmtStr        = "";
  String hReferenceNo       = "";
  String hTxnType           = "";
  String hTxnSummary        = "";
  String hAuthCode          = "";
  String hPosEntryMode      = "";
  String hMccCode           = "";
  String hMerchantId        = "";
  String hMchtCountry       = "";
  String hMicroNo           = "";
  int    readCycAcmmCnt = 0; 
  int    readCycAbemCnt = 0; 
  int    readBilBilldCnt = 0; 
  int    notfndBilBilldCnt = 0; 
  int    writeCtranCnt  = 0; 

  Buf1   sendData = new Buf1();
  int    fileWriter = -1;

  public int mainProcess(String[] args) {

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

      hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

    //comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

			selectPtrBusinday();
			showLogMessage("I", "", "本日營業日 : [" + hCurrBusinessDate + "]");

			if (args.length >= 1 && args[0].length() == 8) {
				hBusiBusinessDate = args[0];
				hInputExeDateFlag = "Y";
			} 

			if (hInputExeDateFlag.equals("Y")) {
  			showLogMessage("I", "", "人工執行關帳日 : ["+hBusiBusinessDate+"]");
			} else {
  			showLogMessage("I", "", "系統執行關帳日 : ["+hBusiBusinessDate+"]");
			}
			
      if (selectPtrWorkday()!=0)
      {
 			 showLogMessage("I", "", "本日非符合執行關帳日, 程式結束");
       return(0);
      }

      showLogMessage("I","","STMT_CYCLE["+hWdayStmtCycle+"]");
      showLogMessage("I","","====================================");

			/** Get file name and path **/
			String fileName = FILE_NAME_TEMPLATE.replace("YYYYMMDD", 
			hBusiBusinessDate);
			String filePath = String.format("%s%s%s", comc.getECSHOME(), 
			MEDIA_FOLDER, fileName);
			
			/** Start to process **/
	  	fileWriter = openOutput(filePath);
      showLogMessage("I","","... Reading data, be patient ...");
      selectCycAcmm();

		  closeOutputText(fileWriter);
		
		  if (writeCtranCnt > 0) {
		    procFTP(Paths.get(filePath).getFileName().toString(), filePath);
		  }
		  else {
		  	comc.fileDelete(filePath);
		  }
	
      if (Arrays.asList("20","25").contains(hWdayStmtCycle)) {
        updatePtrBusinday();//cycle-01在DbaA130清空this_close_date
		  }

  	  showLogMessage("I", "", " cyc_acmm_"+hWdayStmtCycle+" total read count : "+
  	  readCycAcmmCnt);
  	  showLogMessage("I", "", " cyc_abem_"+hWdayStmtCycle+" total read count : "+
  	  readCycAbemCnt);
  	  showLogMessage("I", "", " bil_bill total read count : "+
  	  readBilBilldCnt);
  	  showLogMessage("I", "", " bil_bill total notFound count : "+
  	  notfndBilBilldCnt);
  	  showLogMessage("I", "", " CTRAN total write count : "+writeCtranCnt);

      //==============================================
      // 固定要做的
      comcr.hCallErrorDesc = "程式執行結束";
      comcr.callbatchEnd();
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
		selectSQL = " BUSINESS_DATE,THIS_CLOSE_DATE,rowid as rowid  ";
		daoTable = "PTR_BUSINDAY";
		whereStr = "FETCH FIRST 1 ROW ONLY";

		selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select ptr_businday error!");
			exitProgram(1);
		}

    hPtrBusiRowid = getValue("rowid");
	  hCurrBusinessDate = getValue("BUSINESS_DATE");
		hBusiBusinessDate = getValue("THIS_CLOSE_DATE");
		return;
	}

  //************************************************************************
  public int  selectPtrWorkday() throws Exception
  {
	  extendField = "wday.";
    selectSQL = "";
    daoTable  = "PTR_WORKDAY";
    whereStr  = "WHERE THIS_CLOSE_DATE = ? ";
    setString(1,hBusiBusinessDate);

    int recCnt = selectTable();

    if ( notFound.equals("Y") ) return(1);
 
    hWdayStmtCycle  =  getValue("wday.STMT_CYCLE");

    return(0);
  }

//************************************************************************
  void updatePtrBusinday() throws Exception
  {
    updateSQL = "this_close_date = '',"
              + "mod_pgm         = ?,"
              + "mod_time        = timestamp_format(?,'yyyymmddhh24miss')";
    daoTable  = "ptr_businday";
    whereStr  = "WHERE ROWID = ? ";
  
    setString(1 , javaProgram);
    setString(2 , sysDate+sysTime);
    setRowId(3  , hPtrBusiRowid);
  
    int recCnt = updateTable();
  
    if ( notFound.equals("Y") )
       {
        showLogMessage("I","","update_ptr_businday error!" );
        showLogMessage("I","","rowid=["+hPtrBusiRowid+"]");
        exitProgram(1);
       }
    return;
  }

  //************************************************************************	
	private int openOutput(String filePath) throws Exception {

    int openOutResp = openOutputText(filePath, "MS950");

    return openOutResp;

  }

  /***********************************************************************/
  void selectCycAcmm() throws Exception {

    daoTable    = "cyc_acmm";
		fetchExtend = "cyc_acmm.";
		sqlCmd  = " select "
		+ "(select id_no from crd_idno where crd_idno.id_p_seqno = a.id_p_seqno) as" 
		+ " crdIdno_id_no ";
/***
		sqlCmd += " ,a.* , b.curr_code from cyc_acmm_"+hWdayStmtCycle+" a"  
		sqlCmd += " left join cyc_acmm_curr_"+hWdayStmtCycle+" b";
		sqlCmd += " on a.p_seqno = b.p_seqno ";
		sqlCmd += " where 1=1 and a.acct_status in('1','2') ";
		sqlCmd += " order by a.acct_type, a.acct_key, b.curr_code ";
***/

	//Temporary change 
	//sqlCmd += " ,a.* from za0301_cyc_acmm_"+hWdayStmtCycle+" a";  
	  sqlCmd += " ,a.* from cyc_acmm_"+hWdayStmtCycle+" a";  
  //sqlCmd += " where 1=1 and a.acct_status in('1','2') ";
		sqlCmd += " where 1=1 ";
		sqlCmd += " order by a.acct_type, a.acct_key ";

    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {

			readCycAcmmCnt++;
      hIdno   = getValue("cyc_acmm.crdIdno_id_no");
      hPseqno = getValue("cyc_acmm.p_seqno");
    //hCurrCode = getValue("cyc_acmm.curr_code");
      selectCycAbem();

    }
    closeCursor(cursorIndex);

  }

  /***********************************************************************/
  void selectCycAbem() throws Exception {

    daoTable    = "cyc_abem";
	  extendField = "cyc_abem.";
		sqlCmd  = " select a.* "; 
	//Temporary change 
	//sqlCmd += " from za0301_cyc_abem_"+hWdayStmtCycle+" a";  
	  sqlCmd += " from cyc_abem_"+hWdayStmtCycle+" a";  
		sqlCmd += " where 1=1 and a.p_seqno = ? ";
		sqlCmd += "   and a.dummy_code not in ('Y','S') ";
		sqlCmd += " order by a.curr_code, a.print_type, a.print_seq ";
    setString(1,hPseqno);

    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
			readCycAbemCnt++;
		  if ((readCycAbemCnt % 100000) == 0) {
  			showLogMessage("I", "", "  Read cyc_abem row count : "+readCycAbemCnt);
		  }

      sendData.clearBuf1();
      initData();
      hCardNo       = getValue("cyc_abem.card_no",i);
      hCurrCode     = getValue("cyc_abem.curr_code",i);
      hAcctCode     = getValue("cyc_abem.acct_code",i);
      hTxnCode      = getValue("cyc_abem.txn_code",i);
      hPrintType    = getValue("cyc_abem.print_type",i);
      hPrintSeq     = getValue("cyc_abem.print_seq",i);
      hDummyCode    = getValue("cyc_abem.dummy_code",i);
      hTransDate    = getValue("cyc_abem.purchase_date",i);
      hEntryDate    = getValue("cyc_abem.post_date",i);
      hDcDestAmt    = getValueDouble("cyc_abem.dc_dest_amt",i);
      hDestAmt      = getValueInt("cyc_abem.dest_amt",i);
      hReferenceNo  = getValue("cyc_abem.reference_no",i);
      hTxnSummary   = getValue("cyc_abem.description",i);
			figureTxnType(); 
			tramsAmtStr(); 

      if (hReferenceNo.length() != 0) {
  		  getBilBillInfo();
      }

      sendData.billDate     = hBusiBusinessDate;
      sendData.idn          = hIdno;
      sendData.cardNo       = hCardNo;
      sendData.transDate    = hTransDate;
      sendData.entryDate    = hEntryDate;
      sendData.tx           = hTxnType;
      sendData.entryAmount  = hDestAmtStr;
      sendData.foreignCurr  = hCurrCode;
      sendData.foreignAmount= hDcDestAmtStr;
      sendData.authCode     = hAuthCode;
      sendData.potEntry     = hPosEntryMode;
      sendData.mocCode      = hMccCode;
      sendData.merchantId   = hMerchantId;
      sendData.txnSummary   = hTxnSummary;
      sendData.mircono      = hMicroNo;
      sendData.mchtCountry  = hMchtCountry;
      sendData.fileDate     = sysDate;
      buf = sendData.allText();

			if (readCycAbemCnt != 1) {
        writeTextFile(fileWriter, "\n");
			}
      writeTextFile(fileWriter, buf);
			writeCtranCnt++;

    }
  }

  /***********************************************************************/
  void  initData() {
	  hTxnType            = "";
	  hAuthCode           = "";
	  hPosEntryMode       = "";
	  hMccCode            = "";
	  hMerchantId         = "";
	  hMchtCountry        = "";
	  hMicroNo            = "";
  }

  /***********************************************************************/
	void figureTxnType() throws Exception {
		
		if (hPrintType.equals("01")) {
		  hTxnType  = "QQ";
//前期現欠/上期待繳總額
			return;
		}

		if (hPrintType.equals("07")) {
		  hTxnType  = "BQ";
//本期應繳
			return;
		}

		if (hPrintType.equals("02") && (hDcDestAmt < 0))  {
		  hTxnType  = "20";
//繳款類
			return;
		}

		if (hPrintType.equals("03") && (hDcDestAmt < 0))  {
		  hTxnType  = "22";
//調整金額合計為負項金額：如 DE08 + DE09 + DE13 + DE14 + DR06 + DR07 + DR08 + DR09
			return;
		}
//調整 txn-21 為繳款沖正類(payment_reversal)或調整金額合計為正項金額，
//txn-27 為溢付款轉出，txn-22 為調整金額合計為負項金額*

		if (hPrintType.equals("03") && (hDcDestAmt > 0))  {
		  if (hTxnCode.equals("27"))  {
		    hTxnType  = "27";
//溢付款轉出, 如 OP02、OP03
		  } else if (hTxnCode.equals("21"))  {
		    hTxnType  = "21";
//繳款沖正類, 如 DR11
		  } else {
		    hTxnType  = "21";
//調整金額合計為正項金額：如 DE08 + DE09 + DE13 + DE14 + DR06 + DR07 + DR08 + DR09
		  } 
			return;
		}

		if (hPrintType.equals("04") && hAcctCode.equals("CF")) {
		  hTxnType  = "14";
//預借現金手續費
			return;
		}

		if (hPrintType.equals("04") && hAcctCode.equals("PF")) {
		  hTxnType  = "16";
//交易手續費
			return;
		}

		if (hPrintType.equals("04") && (hDcDestAmt >= 0) && hAcctCode.equals("CA")) {
		  hTxnType  = "30";
//預借現金
			return;
		}

		if (hPrintType.equals("04") && (hDcDestAmt >= 0) && !hAcctCode.equals("CA")) {
		  hTxnType  = "40";
//一般簽帳款或分期款項
			return;
		}

		if (hPrintType.equals("04") && (hDcDestAmt < 0) ) {
		  hTxnType  = "41";
//退貨
			return;
		}

		if (hPrintType.equals("06") && (hDcDestAmt < 0) && hDummyCode.equals("A")) {
		  hTxnType  = "43";
//現金回饋折抵
			return;
		}

		if (hPrintType.equals("06") && (hDcDestAmt > 0) && hDummyCode.equals("A")) {
		  hTxnType  = "42";
//現金回饋沖正
			return;
		}

		if (hPrintType.equals("06") && hAcctCode.equals("RI")) {
		  hTxnType  = "12";
//利息
			return;
		}

		if (hPrintType.equals("06") && hAcctCode.equals("PF")) {
		  hTxnType  = "16";
//雜項手續費
			return;
		}

		if (hPrintType.equals("06") && hAcctCode.equals("PN")) {
		  hTxnType  = "60";
//違約金
			return;
		}

		if (hPrintType.equals("06") && hAcctCode.equals("LF")) {
		  hTxnType  = "60";
//掛失費
			return;
		}

		if (hPrintType.equals("06") && (hDcDestAmt >= 0)  && hAcctCode.equals("AF")) {
		  hTxnType  = "60";
//年費
			return;
		}

		if (hPrintType.equals("06") && (hDcDestAmt < 0)  && hAcctCode.equals("AF")) {
		  hTxnType  = "61";
//年費減免
			return;
		}

	}

  /***********************************************************************/
	void tramsAmtStr() throws Exception {
		
/***
		if (hDestAmt < 0) {
		  hDestAmtStr  = String.format("-%09d", hDestAmt);
		} else {
		  hDestAmtStr  = String.format("%010d", hDestAmt);
		}

		if (hDcDestAmt < 0) {
		  hDcDestAmtStr  = String.format("-%011.2f", hDcDestAmt);
		} else {
		  hDcDestAmtStr  = String.format("%012.2f", hDcDestAmt);
		}
***/
		hDestAmtStr    = String.format("%010d", hDestAmt);
		hDcDestAmtStr  = String.format("%012.2f", hDcDestAmt);

	}

  /***********************************************************************/
	void getBilBillInfo() throws Exception {
		
    daoTable    = "bil_bill";
		extendField = "bill.";
		sqlCmd  = "SELECT * ";
		sqlCmd += " FROM bil_bill WHERE reference_no = ? ";

		setString(1, hReferenceNo);
		selectTable();
		readBilBilldCnt++;

		if (notFound.equals("Y")) {
	  //skip_cnt_flag = "Y";
			notfndBilBilldCnt++;
			showLogMessage("I", "", "** not found in bil_bill, reference_no=[" + 
			hReferenceNo + "]!");
			return;
		}

		hAuthCode      = getValue("bill.auth_code");
		hPosEntryMode  = getValue("bill.pos_entry_mode");
		hMccCode       = getValue("bill.mcht_category");
		hMerchantId    = getValue("bill.mcht_no");
		hMchtCountry   = getValue("bill.mcht_country");
		hMicroNo       = getValue("bill.film_no");
	}

  //************************************************************************	
	void procFTP(String fileName, String filePath) throws Exception {
	  CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
		  
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	  commFTP.hEflgSystemId = "CREDITALL"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	  commFTP.hEriaLocalDir = String.format("%s%s", comc.getECSHOME(), 
	  MEDIA_FOLDER);
	  commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	  commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	  commFTP.hEflgModPgm = javaProgram;
	      
	//Temporary don't execute ftp
	//int errCode = 0;
	//showLogMessage("I", "", "put "+fileName+" 開始傳送....");複製檔案到 /crdatacrea/CREDITALL/ 
	  int errCode = commFTP.ftplogName("CREDITALL", "put " + fileName);
	      
	  if (errCode != 0) {
	    showLogMessage("I", "", "ERROR:無法傳送 " + fileName + " 資料"+" errcode:"+errCode);
	    insertEcsNotifyLog(fileName, commFTP, comr);          
	  } else {
	 	  moveTxtToBackup(filePath, fileName);
	  }
	}

  //************************************************************************		  
	public int insertEcsNotifyLog(String fileName, CommFTP commFTP, 
	CommRoutine comr) throws Exception {
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
	
  //************************************************************************		
	private void moveTxtToBackup(String filePath, String fileName) 
	throws IOException, Exception {
		// media/dba/backup
		Path backupFileFolderPath = Paths.get(comc.getECSHOME(), MEDIA_FOLDER,
		 "backup");
		// create the parent directory if parent the directory is not exist
		Files.createDirectories(backupFileFolderPath);
		// get output file path
		String backupFilePath = Paths.get(backupFileFolderPath.toString(), 
		fileName + "." + sysDate + sysTime).toString();
		
		moveFile(filePath, backupFilePath);
	}
	
  //************************************************************************		
	private void moveFile(String srcFilePath, String targetFilePath) 
	throws Exception {
		
		if (comc.fileMove(srcFilePath, targetFilePath) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + srcFilePath + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + srcFilePath + "] 已移至 [" + targetFilePath + "]");
	}

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    CycA700 proc = new CycA700();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

  /***********************************************************************/
  String removeDot(String myStr) {
      return myStr.replaceAll("\\.", "");
  }

  /*======================================================================*/
  class Buf1 {
    String billDate;
    String idn;
    String cardNo;
    String transDate;
    String entryDate;
    String tx;
    String entryAmount;
    String foreignCurr;
    String foreignAmount;
    String authCode;
    String potEntry;
    String mocCode;
    String merchantId;
    String txnSummary;
    String mircono;
    String mchtCountry;
    String fileDate;
    String filler1;

    void clearBuf1() throws UnsupportedEncodingException {
      billDate     = "";
      idn          = "";
      cardNo       = "";
      transDate    = "";
      entryDate    = "";
      tx           = "";
      entryAmount  = "";
      foreignCurr  = "";
      foreignAmount= "";
      authCode     = "";
      potEntry     = "";
      mocCode      = "";
      merchantId   = "";
      txnSummary   = "";
      mircono      = "";
      mchtCountry  = "";
      fileDate     = "";
      filler1      = "|";
    }

    String allText() throws UnsupportedEncodingException {
      String rtn = "";
      rtn += fixLeft(billDate,      8);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(idn,          10);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(cardNo,       16);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(transDate,     8);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(entryDate,     8);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(tx,            2);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(entryAmount,  10);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(foreignCurr,   3);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(foreignAmount,12);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(authCode,      6);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(potEntry,      3);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(mocCode,       4);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(merchantId,   15);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(txnSummary,   40);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(mircono,      23);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(mchtCountry,   3);
      rtn += fixLeft(filler1,       1);
      rtn += fixLeft(fileDate,      8);
      return rtn;
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
      byte[] bytes = str.getBytes("MS950");
      billDate     = comc.subMS950String(bytes,  0,  8);
    //split01      = comc.subMS950String(bytes,  8,  1);
      idn          = comc.subMS950String(bytes,  9, 10);
    //split02      = comc.subMS950String(bytes, 19,  1);
      cardNo       = comc.subMS950String(bytes, 20, 16);
    //split03      = comc.subMS950String(bytes, 36,  1);
      transDate    = comc.subMS950String(bytes, 37,  8);
    //split04      = comc.subMS950String(bytes, 45,  1);
      entryDate    = comc.subMS950String(bytes, 46,  8);
    //split05      = comc.subMS950String(bytes, 54,  1);
      tx           = comc.subMS950String(bytes, 55,  2);
    //split06      = comc.subMS950String(bytes, 57,  1);
      entryAmount  = comc.subMS950String(bytes, 58, 10);
    //split07      = comc.subMS950String(bytes, 68,  1);
      foreignCurr  = comc.subMS950String(bytes, 69,  3);
    //split08      = comc.subMS950String(bytes, 72,  1);
      foreignAmount= comc.subMS950String(bytes, 73, 12);
    //split09      = comc.subMS950String(bytes, 85,  1);
      authCode     = comc.subMS950String(bytes, 86,  6);
    //split10      = comc.subMS950String(bytes, 92,  1);
      potEntry     = comc.subMS950String(bytes, 93,  3);
    //split11      = comc.subMS950String(bytes, 96,  1);
      mocCode      = comc.subMS950String(bytes, 97,  4);
    //split12      = comc.subMS950String(bytes,101,  1);
      merchantId   = comc.subMS950String(bytes,102, 15);
    //split13      = comc.subMS950String(bytes,117,  1);
      txnSummary   = comc.subMS950String(bytes,118, 40);
    //split14      = comc.subMS950String(bytes,158,  1);
      mircono      = comc.subMS950String(bytes,159, 23);
    //split15      = comc.subMS950String(bytes,182,  1);
      mchtCountry  = comc.subMS950String(bytes,183,  3);
    //split16      = comc.subMS950String(bytes,186,  1);
      fileDate     = comc.subMS950String(bytes,187,  8);
    }
  }

  String fixLeft(String str, int len) throws UnsupportedEncodingException {
    int size = (Math.floorDiv(len, 100) + 1) * 100;
    String spc = "";
    for (int i = 0; i < size; i++)
        spc += " ";
    if (str == null)
        str = "";
    str = str + spc;
    byte[] bytes = str.getBytes("MS950");
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, 0, vResult, 0, len);
    return new String(vResult, "MS950");
  }

  String fixRight(String str, int len) throws UnsupportedEncodingException {
    String spc = "";
    for (int i = 0; i < 100; i++)
        spc += " ";
    if (str == null)
        str = "";
    str = spc + str;
    byte[] bytes = str.getBytes("MS950");
    int offset = bytes.length - len;
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, offset, vResult, 0, len);
    return new String(vResult, "MS950");
  }

}

