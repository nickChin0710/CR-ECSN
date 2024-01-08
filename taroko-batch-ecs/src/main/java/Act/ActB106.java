/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/02/12  V1.00.01  Simon       program initial                           *
 *  112/06/04  V1.00.02  Simon       1.不限定只收昨日及今日代收檔              *
 *                                   2.copy from R6 dir:/crdataupload/ & delete it*
 *                                   3.本日無檔案顯示                          *
 *  112/09/19  V1.00.03  Simon       1.批次自動覆核                            *
 *                                   2.act_pay_detail.serial_no varchar(5)變更為varchar(7)*
 *                                   3.新增以'000'+13碼act_acno.payment_no_ii比對繳款編號*
 *                                   4.act_b003r1.print_date 改抓 business_date*
 *                                   5.檔名判斷由 "TCyymmdd.TXT" 更改為 "TCyyymmdd.TXT"*
 *  112/09/21  V1.00.04  Simon       1.改從 %s[comc.getECSHOME()]/media/act/ 讀取檔案*
 *                                   2.新增寫入信合社+分社代號、製檔日期       *
 *  112/09/22  V1.00.05  Simon       新增寫入繳款編號(payment_no)              *
 *  112/09/29  V1.00.06  Simon       新增寫入代收單位別(def_branch=act_b003r1.bank_no)*
 ******************************************************************************/

package Act;

import java.io.UnsupportedEncodingException;
import java.io.File;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import Dxc.Util.SecurityUtil;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;


/*接收信聯社代收檔處理程式*/
public class ActB106 extends AccessDAO {

  public static final boolean DEBUG_MODE = false;

  private final String PROGNAME = "接收信聯社代收檔處理程式  112/09/29 "
                                + "V1.00.06";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;
	CommString commString = new com.CommString();

  long hModSeqno = 0;
  String hCallBatchSeqno = "";

  String hPtclBusinessDate = "";
  String hTempSysdate = "";
  String hTempSystime = "";
  String hTempPresysdate = "";
  String hPtclMediaName = "";
  int hPtclSeqNo = 0;
  String hPtclExternalName = "";
  String hPtclAcctBank = "";
  int hPtclTotalRec = 0;
  String hPtclPgmMemo = "";
  int count = 0;
  int    hDetlAmt = 0;
  int    hTotAmt = 0;
  int    hTotCnt = 0;

  String tmpstr1 = "";
  String tmpstr2 = "";

  String hAcctKey = "";
  String hAcctType = "";
  String hAcnoAcctPSeqno = "";
  String hAcnoIdPSeqno = "";
  String hAcnoAcctHolderId = "";
  String hAcnoAcctHolderIdCode = "";
  String hAcnoCorpPSeqno = "";
  String hAcnoCardIndicator = "";
  String hAcnoCorpActFlag = "";
  String hB031ConsumerNo = "";
  String hB031ConsumerNoFlag = "";
  String hIdnoChiName = "";
  String hCorpCorpNo = "";
  String hCorpChiName = "";
  String hApbtBatchNo = "";
  String hApdlSerialNo = "";
  double hApdlPayAmt = 0;
  String hApdlPayDate = "";
  String hPaymentType = "";
  String hAperErrorReason = "";
  String hAperErrorRemark = "";
  String hApbtModUser = "";
  String hApbtModPgm = "";
  String hB031PrintDate = "";
  String hB031AllotAccount = "";
  String hB031TransDate = "";
  String hB031TransDateFlag = "";
  String hB031TransStation = "";
  String hB031TransNo = "";
  String hB03TransSeq = "";
  String hB031CollStoreNo = "";
  String hB031TransFlag = "";
  double hB031TransAmt = 0.0;
  String hB031TransAmtFlag = "";
  String hB031TelephoneNo = "";
  String hTempChiName = "";
  int hApbtBatchTotCnt = 0;
  double hApbtBatchTotAmt = 0;
  long hTempBatchNoSeq = 0;
  String hTempBatchNo = "";
  String hPtclAcctMonth = "";
  String hPtclSStmtCycle = "";
  String hPtclEStmtCycle = "";
  String hPtclValueDate = "";
  String hPtclRowid = "";
  String hFromDesc = "";
  String hChiDate  = "";
  String hChiDate6 = "";
  long rightNo = 0;
  long errorNo = 0;
  String wsInFileFlag = "";
  String fullFileName1 = "";
  String fullFileName2 = "";
  int hFileProcCnt = 0;

  String str600 = "";
  int nSerialNo = 0;

//contentBuf content = new contentBuf();

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

      comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

      selectPtrBusinday();

      if (args.length >= 1 && args[0].length() == 8) {
          hPtclBusinessDate = args[0];
          hTempSysdate = args[0];
          sqlCmd  = " select ";
          sqlCmd += " to_char(to_date(?,'yyyymmdd') - 1 days,'yyyymmdd') h_temp_presysdate ";
          sqlCmd += " from dual ";
          setString(1, hTempSysdate);
          int recordCnt = selectTable();
          if (recordCnt <= 0) {
             comcr.errRtn("select_pre_date error!", "", hCallBatchSeqno);
          }
          hTempPresysdate = getValue("h_temp_presysdate");
      }

      /***
      showLogMessage("I", "", String.format("temp_sysdate    [%s]", hTempSysdate));
      showLogMessage("I", "", String.format("temp_presysdate [%s]", hTempPresysdate));

	  	String filePath = String.format("%s/media/act", comc.getECSHOME());
	  	filePath = SecurityUtil.verifyPath(filePath);
	  	File checkFilePath = new File(filePath);
	  	filePath = Normalizer.normalize(filePath, java.text.Normalizer.Form.NFKD);
	  	if (!checkFilePath.isDirectory())
	  		comcr.errRtn(String.format("[%s]目錄不存在", filePath), "", hCallBatchSeqno);
      ***/
    
      /*** 抓日期為前一天及當天之信聯社代收檔 TCyymmdd.txt ***/

      /***
      hChiDate  = String.format("%07d", comcr.str2long(hTempPresysdate) - 19110000);
 			hChiDate6 = commString.mid(hChiDate, 1, 6); // 檔名日期
      if (checkDateFile(hChiDate6) == 0) {
  	  	procInputFile(hChiDate6);
      }

      hChiDate  = String.format("%07d", comcr.str2long(hTempSysdate) - 19110000);
 			hChiDate6 = commString.mid(hChiDate, 1, 6); // 檔名日期
      if (checkDateFile(hChiDate6) == 0) {
  	  	procInputFile(hChiDate6);
      }
      ***/

    //String filePath = String.format("/crdataupload/");
    	String filePath = String.format("%s/media/act/",comc.getECSHOME());
	  	filePath = SecurityUtil.verifyPath(filePath);
	  	List<String> listOfFiles = comc.listFS(filePath, "", "");
	  	showLogMessage("I", "", "Process file Path =" + filePath);
	    String getFileDate = "";
	  	for (String file : listOfFiles) {
	  		if (commString.mid(file, 0, 2).equals("TC") && 
	  		    commString.mid(file, 5, 2).compareTo("01") >= 0 &&
	  		    commString.mid(file, 5, 2).compareTo("12") <= 0 &&
	  		    commString.mid(file, 7, 2).compareTo("01") >= 0 &&
	  		    commString.mid(file, 7, 2).compareTo("31") <= 0 )
	  		{
    			showLogMessage("I", "", "File = [" + file + "]");
				//fullFileName1 = String.format("/crdataupload/%s", file);
				  fullFileName1 = String.format("%s/media/act/%s",comc.getECSHOME(),file);
				  fullFileName1 = Normalizer.normalize(fullFileName1,
				                  java.text.Normalizer.Form.NFKD);
	  		  procInputFile(file);
				  fullFileName2 = String.format("%s/media/act/backup/%s.%s", 
				                  comc.getECSHOME(), file, sysDate+sysTime);
				  fullFileName2 = Normalizer.normalize(fullFileName2, 
				                  java.text.Normalizer.Form.NFKD);
		      renameFile(fullFileName1,fullFileName2);
          hFileProcCnt++;
	  		} else {
    			continue;
	  		}
	  	}

      if (hFileProcCnt == 0) {
        String likeFile = "TCyyymmdd.txt";
        exceptExit = 0;
      //comc.errExit(String.format("本日[%s]無信聯社代收檔[/crdataupload/%s]",
        comc.errExit(String.format("本日[%s]無信聯社代收檔[%s/media/act/%s]",
        hPtclBusinessDate,comc.getECSHOME(),likeFile),hCallBatchSeqno);
      }

      // ==============================================
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
        hPtclBusinessDate = "";
        hTempSysdate = "";
        hTempSystime = "";
        hTempPresysdate = "";

        sqlCmd = "select business_date,";
        sqlCmd += " to_char(to_date(business_date, 'yyyymmdd')-1 days,'yyyymmdd') h_temp_prebusidate, ";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_sysdate,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_systime,";
        sqlCmd += " to_char(sysdate-1 days,'yyyymmdd') h_temp_presysdate ";
        sqlCmd += "  from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPtclBusinessDate = getValue("business_date");
            hTempSystime = getValue("h_temp_systime");
          //hTempSysdate = getValue("h_temp_sysdate");
          //hTempPresysdate = getValue("h_temp_presysdate");
            hTempSysdate = getValue("business_date");
            hTempPresysdate = getValue("h_temp_prebusidate");
        }

    }

  /***********************************************************************/
	void procInputFile(String fileName) throws Exception {
    hPtclMediaName = "TC";
    hPtclAcctBank = "780";
  //hPtclExternalName = String.format("TC%s.TXT", chiDate6);
    hPtclExternalName = fileName;

    if (selectPtrMediaCntl() == 0) { //cnt=0表示未處理過
      if (openFile() == 0) { //0表示開啟檔案成功
        readFile();
        hPtclSeqNo = getPmtlSeqno() + 1;
        insertPtrMediaCntl();
      } 
    } else {
      showLogMessage("I", "",
           //String.format("*** 檔案[/crdataupload/%s]已處理過，本次不處理，請檢核！", 
             String.format("*** 檔案[%s/media/act/%s]已處理過，本次不處理，請檢核！", 
             comc.getECSHOME(),hPtclExternalName));
    }

	}

  /***********************************************************************/
    void renameFile(String inputFileName,String backupFileName) throws Exception {
    	String tmpstr1 = inputFileName;
    	String tmpstr2 = backupFileName;

    //if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
    	if (comc.fileCopy(tmpstr1, tmpstr2) == false) {
    		showLogMessage("I", "", "檔案 [" + inputFileName + "] 備份失敗!");
    		return;
    	}
    	showLogMessage("I", "", "檔案 [" + inputFileName + "] 已備份至 [" + tmpstr2 + "]");
    	if (comc.fileDelete(tmpstr1) == false) {
    		showLogMessage("I", "", "來源檔案 [" + inputFileName + "] 刪除失敗!");
    	}
    }

  /***********************************************************************/
  int selectPtrMediaCntl() throws Exception {

    sqlCmd = "select count(*) as count ";
    sqlCmd += "  from ptr_media_cntl  ";
    sqlCmd += " where external_name = ?  ";
  //sqlCmd += "   and business_date = ? ";
    sqlCmd += "   and in_file_flag = 'Y' ";
    setString(1, hPtclExternalName);
  //setString(2, hPtclBusinessDate);
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_media_cntl count(*) not found!", "", 
        hCallBatchSeqno);
    }
    if (recordCnt > 0) {
        count = getValueInt("count");
    }

    return count;
  }

  /***********************************************************************/
  int openFile() throws Exception {
    
    int br = openInputText(fullFileName1, "MS950");
    if (br == -1) {
      showLogMessage("I", "", String.format("檔案[%s]找不到", fullFileName1));
      return -1;
    }
    closeInputText(br);
    
    return 0;
  }

  /***********************************************************************/
  void readFile() throws Exception {
    hPtclTotalRec = 0;
    hPtclPgmMemo = "";

    int br = openInputText(fullFileName1, "MS950");
    if (br == -1) {
      comcr.errRtn(fullFileName1, "檔案開啓失敗！", hCallBatchSeqno);
    }
    /*** 逐筆讀取 一次讀入80 Bytes長 ***/
    while (true) {
      str600 = readTextFile(br);
      if (endFile[br].equals("Y")) break;
      
      if (str600.length() < 10)
        continue;

		  getFieldD(); // 讀取資料
      hTotAmt += hDetlAmt;
      hTotCnt++;
    }

    closeInputText(br);

    /*** 取得總金額 ***/
    hPtclPgmMemo = "" + hTotAmt;
    /*** 取得總筆數 ***/ 
    hPtclTotalRec = hTotCnt;
  }

    /***********************************************************************/
    int getPmtlSeqno() throws Exception {
        int lmaxSeqno = 0;

        sqlCmd = "select max(seq_no) as max_seq_no ";
        sqlCmd += "  from ptr_media_cntl  ";
        sqlCmd += " where media_name = ?  ";
        sqlCmd += "   and business_date = ? ";
        setString(1, hPtclMediaName);
        setString(2, hPtclBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_media_cntl not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
          lmaxSeqno = getValueInt("max_seq_no");
        }

        return lmaxSeqno;
    }

    /***********************************************************************/
    void insertPtrMediaCntl() throws Exception {
      //hPtclSeqNo++;

        daoTable = "ptr_media_cntl";
        setValue("media_name", hPtclMediaName);
        setValue("business_date", hPtclBusinessDate); 
        setValueInt("seq_no", hPtclSeqNo);
        setValue("external_name", hPtclExternalName);
        setValue("trans_date", sysDate);
        setValue("trans_time", hTempSystime);
        setValue("out_media_flag", "N");
        setValue("out_file_flag", "N");
        setValue("in_media_flag", "Y");
        setValue("in_file_flag", "N");
        setValue("acct_bank", hPtclAcctBank);
        setValueInt("total_rec", hPtclTotalRec);
        setValue("pgm_memo", hPtclPgmMemo);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            showLogMessage("I", "", String.format("MEDIA NAME[%s]\nBUSINESS DATE[%s]\nSEQ no[%s]", hPtclMediaName,
                    hPtclBusinessDate, hPtclSeqNo));
            comcr.errRtn("insert_ptr_media_cntl duplicate!", "", hCallBatchSeqno);
        }
        
        commitDataBase();
        //執行原ActB003
        runActB003();

    }
    /***********************************************************************/
    private boolean runActB003() throws Exception {
        boolean bFlag = true;

        hApbtModUser = comc.commGetUserID();;
        hApbtModPgm = javaProgram;
        hApbtBatchNo = "";

        selectPtrMediaCntl2();
        wsInFileFlag = "R";
        updatePtrMediaCntl();
        commitDataBase();

        checkF2open();

        readFile2();

/***
        String fs = String.format("%s/media/act/%s", comc.getECSHOME(), hPtclExternalName);
        fs = Normalizer.normalize(fs, java.text.Normalizer.Form.NFKD);
        String ft = String.format("%s/media/act/backup/%s_%s.DAT", comc.getECSHOME(), hPtclExternalName,
                    hPtclBusinessDate);
        ft = Normalizer.normalize(ft, java.text.Normalizer.Form.NFKD);

        if (comc.fileRename(fs, ft) == false) {
        	comc.fileDelete(ft);
        	comc.fileRename(fs, ft);
        }
***/

        showLogMessage("I", "", String.format("批號=[%s]", hApbtBatchNo));
        showLogMessage("I", "", String.format("Total 筆數=[%d]", hApbtBatchTotCnt));

        wsInFileFlag = "Y";
        updatePtrMediaCntl();

        return bFlag;
    }

    /***********************************************************************/
    void selectPtrMediaCntl2() throws Exception {
        hPtclExternalName = "";
        hPtclAcctBank = "";
        hPtclAcctMonth = "";
        hPtclSStmtCycle = "";
        hPtclEStmtCycle = "";
        hPtclValueDate = "";
        hPtclRowid = "";

        sqlCmd = "select external_name,";
        sqlCmd += " acct_bank,";
        sqlCmd += " acct_month,";
        sqlCmd += " s_stmt_cycle,";
        sqlCmd += " e_stmt_cycle,";
        sqlCmd += " value_date,";
        sqlCmd += " rowid  as  rowid ";
        sqlCmd += "  from ptr_media_cntl  ";
        sqlCmd += " where media_name     = ? ";
        sqlCmd += "   and  business_date = ? ";
        sqlCmd += "   and  seq_no        = ? ";
        setString(1, hPtclMediaName);
        setString(2, hPtclBusinessDate);
        setInt(3, hPtclSeqNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
                comcr.errRtn("select_ptr_media_cntl not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPtclExternalName = getValue("external_name");
            hPtclAcctBank = getValue("acct_bank");
            hPtclAcctMonth = getValue("acct_month");
            hPtclSStmtCycle = getValue("s_stmt_cycle");
            hPtclEStmtCycle = getValue("e_stmt_cycle");
            hPtclValueDate = getValue("value_date");
            hPtclRowid = getValue("rowid");
        }
    }

    /***********************************************************************/
    void updatePtrMediaCntl() throws Exception {
        /* for debug */

        daoTable = "ptr_media_cntl";
        updateSQL = "proc_date    = ?,";
        updateSQL += "proc_time    = ?,";
        updateSQL += "batch_no     = ?,";
        updateSQL += "total_rec    = ? + ? ,";
        updateSQL += "right_rec    = ?,";
        updateSQL += "in_file_flag = ? ";
        whereStr = "where rowid  = ? ";
        setString(1, sysDate);
        setString(2, sysTime);
        setString(3, hApbtBatchNo);
        setLong(4, rightNo);
        setLong(5, errorNo);
        setLong(6, rightNo);
        setString(7, wsInFileFlag);
        setRowId(8, hPtclRowid);
        updateTable();
        if (notFound.equals("Y")) {
                comcr.errRtn("update_ptr_media_cntl not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void checkF2open() throws Exception {

        int br = openInputText(fullFileName1, "MS950");
        if (br == -1) {
            wsInFileFlag = "N";
            updatePtrMediaCntl();
            comcr.errRtn("檔案不存在：" + fullFileName1, "", hCallBatchSeqno);
        }
        closeInputText(br);
    }

    /***********************************************************************/
    void readFile2() throws Exception {

      nSerialNo = 1;
      rightNo = 0;
      errorNo = 0;

      selectActPayBatch();
      hApbtBatchTotCnt = 0;
      hApbtBatchTotAmt = 0;
      
      int br = openInputText(fullFileName1, "MS950");
      if (br == -1) {
        comcr.errRtn(fullFileName1, "檔案開啓失敗！", hCallBatchSeqno);
      }
      while (true) {
        str600 = readTextFile(br);
        if (endFile[br].equals("Y")) break;
        
        if (str600.length() < 10)
            continue;

        hPaymentType = "";

        /* 780(信聯社)  */

        hPaymentType = "COU8";

        getField780();

        if ((hB031TransDateFlag.length() == 0) && (hB031TransAmtFlag.length() == 0)
            && (hB031ConsumerNoFlag.length() == 0)) {
          insertActPayDetail();
          hApbtBatchTotCnt++;
          hApbtBatchTotAmt = hApbtBatchTotAmt + hApdlPayAmt;
        } else {
          insertActPayError();
          insertActB003r1();
        } 
      }
      closeInputText(br);

      if (nSerialNo > 1)
          insertActPayBatch();
    }

    /***********************************************************************/
    void selectActPayBatch() throws Exception {
        String tempstr = "";
        hTempBatchNoSeq = 0;

      //hTempBatchNo = String.format("%s1003%c", sysDate, '%');
        hTempBatchNo = String.format("%s1003%c", hPtclBusinessDate, '%');

        sqlCmd = "select to_number(substr(max(batch_no),13,4))+1 h_temp_batch_no_seq ";
        sqlCmd += "  from act_pay_batch  ";
        sqlCmd += " where batch_no like ? ";
        setString(1, hTempBatchNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempBatchNoSeq = getValueLong("h_temp_batch_no_seq");
        }

        if (hTempBatchNoSeq != 0) {
            tempstr = String.format("%12.12s%04d", hTempBatchNo, hTempBatchNoSeq);
        } else {
            tempstr = String.format("%12.12s0001", hTempBatchNo);
        }
        hApbtBatchNo = tempstr;

    }

  /***********************************************************************/
  int getField780() throws Exception {
    String stra = "";

    hB031TransDate = "";
    hB031TransDateFlag = "";
    hApdlPayDate = "";
    hB031TransAmt = 0.0;
    hB031TransAmtFlag = "";
    hB031ConsumerNoFlag = "";
    hAcctType = "";
    hAcctKey = "";

    hAcnoAcctPSeqno = "";
    hAcnoIdPSeqno = "";
    hAcnoAcctHolderId = "";
    hAcnoAcctHolderIdCode = "";
    hAcnoCorpPSeqno = "";
    hAcnoCardIndicator = "";
    hAcnoCorpActFlag = "";

    hB031TransDate = str600.substring(19, 19 + 6).trim();
    hB031TransDate = String.format("%08d", comcr.str2long(hB031TransDate) 
    + 1000000 + 19110000);
    hB031CollStoreNo = str600.substring(44, 44 + 8).trim();
    hB031TransFlag = "+";
    hApdlPayDate = str600.substring(37, 37 + 6).trim();
    hApdlPayDate = String.format("%08d", comcr.str2long(hApdlPayDate) 
    + 1000000 + 19110000);
    hB031ConsumerNo = str600.substring(0, 0 + 19);
    hB031ConsumerNo = hB031ConsumerNo.substring(0, 16).trim();
    chkPayment();
    
    stra = str600.substring(25, 25 + 8).trim();
    hB031TransAmt = comcr.str2long(stra);

    // if (val_date(h_apdl_pay_date) != 0)
    if (comcr.str2Date(hApdlPayDate) == null) {
        hB031TransDateFlag = "Y";
        hAperErrorReason = "801";
        hAperErrorRemark = String.format("[%s] trans date error", hPaymentType);
    }

    if (hB031TransAmt <= 0) {
        hB031TransAmtFlag = "Y";
        hAperErrorReason = "802";
        hAperErrorRemark = String.format("[%s] trans amt error", hPaymentType);
    } else {
        hApdlPayAmt = hB031TransAmt;
    }

    if (!hB031ConsumerNoFlag.equals("Y")) {
      selectChiNameRtn();
    } 

    return (0);
  }

  /***********************************************************************/
  void chkPayment() throws Exception {

    sqlCmd  = "select p_seqno,"; 
    sqlCmd += " id_p_seqno,";
    sqlCmd += " acct_type,";
    sqlCmd += " acct_key,";
    sqlCmd += " corp_p_seqno,";
    sqlCmd += " card_indicator,";
    sqlCmd += " corp_act_flag ";
    sqlCmd += "  from act_acno  ";
    sqlCmd += " where payment_no = ? ";
    setString(1, hB031ConsumerNo);
    int recordCnt = selectTable();
    if (recordCnt <= 0) {
      sqlCmd  = "select p_seqno,"; 
      sqlCmd += " id_p_seqno,";
      sqlCmd += " acct_type,";
      sqlCmd += " acct_key,";
      sqlCmd += " corp_p_seqno,";
      sqlCmd += " card_indicator,";
      sqlCmd += " corp_act_flag ";
      sqlCmd += "  from act_acno  ";
      sqlCmd += " where payment_no_ii = ? ";
      sqlCmd += "    or ('000'||payment_no_ii) = ? ";
      setString(1, hB031ConsumerNo);
      setString(2, hB031ConsumerNo);
      int recordCnt2 = selectTable();
      if (recordCnt2 <= 0) {
        showLogMessage("I", "", String.format("COU8 繳款代號錯誤[%s]!", hB031ConsumerNo));
        hAperErrorReason = "803";
        hAperErrorRemark = "COU8 consumer_no error";
        hB031ConsumerNoFlag = "Y";
      } else {
        hAcnoAcctPSeqno = getValue("p_seqno");
        hAcnoIdPSeqno = getValue("id_p_seqno");
        hAcctType = getValue("acct_type");
        hAcctKey = getValue("acct_key");
        hAcnoCorpPSeqno = getValue("corp_p_seqno");
        hAcnoCardIndicator = getValue("card_indicator");
        hAcnoCorpActFlag = getValue("corp_act_flag");
      } 
    } else { 
      hAcnoAcctPSeqno = getValue("p_seqno");
      hAcnoIdPSeqno = getValue("id_p_seqno");
      hAcctType = getValue("acct_type");
      hAcctKey = getValue("acct_key");
      hAcnoCorpPSeqno = getValue("corp_p_seqno");
      hAcnoCardIndicator = getValue("card_indicator");
      hAcnoCorpActFlag = getValue("corp_act_flag");
    }

  }

    /***********************************************************************/
    void selectChiNameRtn() throws Exception {
        hIdnoChiName = "";

        /* select business card */
        if ((hAcnoCardIndicator.equals("2")) && (hAcnoCorpActFlag.equals("Y"))) {
            selectCrdCorp();
            return;
        }

        sqlCmd = "select chi_name, id_no, id_no_code ";
        sqlCmd += "  from crd_idno  ";
        sqlCmd += " where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIdnoChiName = getValue("chi_name");
            hAcnoAcctHolderId = getValue("id_no");
            hAcnoAcctHolderIdCode = getValue("id_no_code");
        } else {
            wsInFileFlag = "N";
            updatePtrMediaCntl();
          //comcr.err_rtn("select_crd_idno error", "", h_call_batch_seqno);
            comcr.errRtn("select_crd_idno error", "id_p_seqno : "+hAcnoIdPSeqno, hCallBatchSeqno);
        }
        hTempChiName = hIdnoChiName;
    }

    /***********************************************************************/
    void selectCrdCorp() throws Exception {
        hCorpCorpNo = "";
        hCorpChiName = "";

        sqlCmd = "select corp_no,";
        sqlCmd += " chi_name ";
        sqlCmd += "  from crd_corp ";
        sqlCmd += " where corp_p_seqno = ? ";
        setString(1, hAcnoCorpPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCorpCorpNo = getValue("corp_no");
            hCorpChiName = getValue("chi_name");
        } else {
            wsInFileFlag = "N";
            updatePtrMediaCntl();
            comcr.errRtn("select_crd_corp error", "", hCallBatchSeqno);
        }
        hTempChiName = hCorpChiName;
    }

    /***********************************************************************/
    void insertActPayDetail() throws Exception {

        hApdlSerialNo = String.format("%07d", nSerialNo);
        nSerialNo++;
        rightNo++;

        daoTable = "act_pay_detail";
        extendField = "apyd.";

        setValue("apyd.batch_no", hApbtBatchNo);
        setValue("apyd.serial_no", hApdlSerialNo);
        setValue("apyd.p_seqno", hAcnoAcctPSeqno);
        setValue("apyd.acno_p_seqno", hAcnoAcctPSeqno);
        setValue("apyd.acct_type", hAcctType);
        // setValue ("apyd.acct_key" , hAcctKey);
        setValue("apyd.id_p_seqno", hAcnoIdPSeqno);
        setValue("apyd.pay_card_no", "");
        setValue("apyd.payment_no", hB031ConsumerNo);
        setValueDouble("apyd.pay_amt", hApdlPayAmt);
        setValue("apyd.pay_date", hApdlPayDate);
        setValue("apyd.payment_type", hPaymentType);
      //setValue("apyd.crt_user", hApbtModUser); // update_user
        setValue("apyd.crt_user", hB031CollStoreNo); //信合社+分社代號
      //setValue("apyd.crt_date", sysDate); // update_date
        setValue("apyd.crt_date", hB031TransDate); //製檔日期
        setValue("apyd.crt_time", sysTime); // update_time
        setValue("apyd.mod_time", sysDate + sysTime);
        setValue("apyd.mod_user", hApbtModUser);
        setValue("apyd.mod_pgm", hApbtModPgm);
        setValue("apyd.def_branch", hPtclAcctBank);//代收單位別
        setValue("apyd.collection_store_no", hB031CollStoreNo);//信合社+分社代號
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_detail duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActPayError() throws Exception {

        hApdlSerialNo = String.format("%07d", nSerialNo);
        nSerialNo++;
        errorNo++;

        daoTable = "act_pay_error";
        extendField = "apye.";

        setValue("apye.batch_no", hApbtBatchNo);
        setValue("apye.serial_no", hApdlSerialNo);
        setValue("apye.p_seqno", hAcnoAcctPSeqno);
        setValue("apye.acno_p_seqno", hAcnoAcctPSeqno);
        setValue("apye.acct_type", hAcctType);
/***    
        if (hB031ConsumerNoFlag.equals("Y")) {
          setValue("apye.pay_card_no", hB031ConsumerNo);
        } else {
          setValue("apye.pay_card_no", "");
        }
***/
        setValue("apye.payment_no", hB031ConsumerNo);

        setValue("apye.id_no", hAcnoAcctHolderId);
        setValueDouble("apye.pay_amt", hApdlPayAmt);
        setValue("apye.pay_date", hApdlPayDate);
        setValue("apye.payment_type", hPaymentType);
        setValue("apye.error_reason", hAperErrorReason);
        setValue("apye.error_remark", hAperErrorRemark);
        setValue("apye.crt_user", hApbtModUser);
      //setValue("apye.crt_user", hApbtModUser);
        setValue("apye.crt_user", hB031CollStoreNo);//信合社+分社代號
      //setValue("apye.crt_date", sysDate);
        setValue("apye.crt_date", hB031TransDate);//製檔日期
        setValue("apye.crt_time", sysTime);
        setValue("apye.mod_user", hApbtModUser);
        setValue("apye.mod_time", sysDate + sysTime);
        setValue("apye.mod_pgm", hApbtModPgm);
        insertTable();
        if (dupRecord.equals("Y")) {
            wsInFileFlag = "N";
            updatePtrMediaCntl();
            comcr.errRtn("insert_act_pay_error error", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActB003r1() throws Exception {

      //hB031PrintDate = sysDate;
        hB031PrintDate = hPtclBusinessDate;

        daoTable = "act_b003r1";
        extendField = "ab31.";

        setValue("ab31.batch_no", hApbtBatchNo);
        setValue("ab31.bank_no", hPtclAcctBank);
        setValue("ab31.print_date", hB031PrintDate);
      //setValue("ab31.allot_account", "");//劃撥帳號
        setValue("ab31.allot_account", hB031CollStoreNo);//信合社+分社代號
        setValue("ab31.trans_date", hApdlPayDate);//繳款日期
        setValue("ab31.trans_date_flag", hB031TransDateFlag);//交易日期錯誤註記
        setValue("ab31.trans_station", "");//交易局號
        setValue("ab31.trans_no", "");//交易代號
        setValue("ab31.trans_seq", "");//交易序號
        setValue("ab31.trans_flag", hB031TransFlag);//正、負項(目前無負項)
        setValueDouble("ab31.trans_amt", hB031TransAmt);//交易金額
        setValue("ab31.trans_amt_flag", hB031TransAmtFlag);//交易金額錯誤代碼
        setValue("ab31.consumer_no", hB031ConsumerNo);//繳款代號
      //setValue("ab31.telephone_no", "");
        setValue("ab31.telephone_no", hB031TransDate);//製檔日期
        setValue("ab31.id_p_seqno", hAcnoIdPSeqno);
        setValue("ab31.chi_name", hTempChiName);
        insertTable();
        if (dupRecord.equals("Y")) {
            wsInFileFlag = "N";
            updatePtrMediaCntl();
            comcr.errRtn("insert_act_b003r1 error", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActPayBatch() throws Exception {
      //hFromDesc = "BI006";
        hFromDesc = "信聯社";
        
        daoTable = "act_pay_batch";
        extendField = "apyb.";

        setValue("apyb.batch_no", hApbtBatchNo);
        setValue("apyb.from_desc", hFromDesc);
        setValueInt("apyb.batch_tot_cnt", hApbtBatchTotCnt);
        setValueDouble("apyb.batch_tot_amt", hApbtBatchTotAmt);
        setValue("apyb.crt_user", hApbtModUser);
      //setValue("apyb.crt_date", sysDate);
        setValue("apyb.crt_date", hPtclBusinessDate);
        setValue("apyb.crt_time", sysTime);
        setValue("apyb.trial_user", hApbtModUser);
      //setValue("apyb.trial_date", sysDate);
        setValue("apyb.trial_date", hPtclBusinessDate);
        setValue("apyb.trial_time", sysTime);
        setValue("apyb.confirm_user", hApbtModUser);
      //setValue("apyb.confirm_date", sysDate);
        setValue("apyb.confirm_date", hPtclBusinessDate);
        setValue("apyb.confirm_time", sysTime);
        setValue("apyb.mod_time", sysDate + sysTime);
        setValue("apyb.mod_user", hApbtModUser);
        setValue("apyb.mod_pgm", hApbtModPgm);
        insertTable();

        if (!dupRecord.equals("Y"))
            return;

      //hModSeqno = comcr.getModSeq();

        daoTable = "act_pay_batch";
        updateSQL = "batch_tot_cnt  = batch_tot_cnt + ?,";
        updateSQL += " batch_tot_amt = batch_tot_amt + ?,";
        updateSQL += " from_desc = ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_user      = ?,";
        updateSQL += " mod_pgm       = ?,";
        updateSQL += " mod_seqno     = mod_seqno + 1 ";
        whereStr = "where batch_no = ? ";
        setInt(1, hApbtBatchTotCnt);
        setDouble(2, hApbtBatchTotAmt);
        setString(3, hFromDesc);
        setString(4, hApbtModUser);
        setString(5, hApbtModPgm);
      //setString(6, hModSeqno);
        setString(6, hApbtBatchNo);
        updateTable();

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActB106 proc = new ActB106();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

  /***********************************************************************/
	void getFieldD() throws Exception {
	  byte[] bytes = str600.getBytes("MS950");
	  hDetlAmt = commString.ss2int(comc.subMS950String(bytes, 25, 8));
	}

}
