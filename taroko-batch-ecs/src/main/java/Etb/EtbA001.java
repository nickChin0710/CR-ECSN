/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE          Version    AUTHOR                       DESCRIPTION      *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/04/21  V1.00.00    JustinWu         program initial                          
*  109/06/03  V1.00.01    JustinWu         修改英文姓名最後執行方法改為update crd_card及insert onbat_2ecs
*  109/06/05  V1.00.02    JustinWu         fix a bug of checkDataExistsIn()
*  109-07-03  V1.00.03    shiyuqi          updated for project coding standard     *
*  109-07-23  V1.00.04    shiyuqi          coding standard, rename field method & format  
*  109-08-19  V1.00.05    JustinWu         add mod_user, mod_time ,and mod_pgm
*  110-01-25  V1.00.06    JustinWu         add no change report
*  111/05/20  V1.00.07    JustinWu         調整變更客戶基本資料檔處理方式          *
*  111/05/26  V1.00.08    JustinWu         mark export noChgReport   *
*                                          民國年-> 西元年           *
*  111/05/27  V1.00.09    JustinWu         修正檔案含中文資料縮短問題*
*                                          調整地址更新邏輯          *
*  111/05/30  V1.00.10    JustinWu         修正變更英文姓名new_end_date > sysdate *
*  111/06/01  V1.00.11    ryan             新增變更項目代號06、44       *
*  111/06/02  V1.00.12    JustinWu         修正檔案含中文資料縮短問題*
*  111/06/16  V1.00.13    JustinWu         修正弱點                  *
*  111/08/04  V1.00.14    ryan             修正欄位長度                  *
*  111/08/16  V1.00.15    ryan             移除變更項目代碼3，修改變更項目代碼16、17、21檢核邏輯  *
*  112/07/11  V1.00.16    Wilson           異動email增加異動管道                                                 *
*  113/01/04  V1.00.17    Wilson           mainProcess宣告改為public           *
*****************************************************************************/
package Etb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommTxBill;



public class EtbA001 extends AccessDAO {
  private final String dbcIdno = "DBC_IDNO";
  private final String crdIdno = "CRD_IDNO";
  private final String dbcCard = "DBC_CARD";
  private final String crdCard = "CRD_CARD";
  private final String actAcno = "ACT_ACNO";
  private final String dbaAcno = "DBA_ACNO";
  private final String PROGNAME = "ETABS 0CJ0變更客戶基本資料檔處理程式 113/01/04 V1.00.17";
  private final String prgmId = "EtbA001";
  private final String modUser = "system";
  
  private final static int VALID_BYTES_LENGTH = 150;
  private final static int COMMIT_NUM = 1000;
  
  private final String RESIDENT_ADDR_BILL_APPLY_FLAG = "1";
  private final String MAIL_ADDR_BILL_APPLY_FLAG = "2";
  private final String COMPANY_ADDR_BILL_APPLY_FLAG = "3";
  
  private final byte[] lineSeparatorBytes = System.lineSeparator().getBytes();
  private final byte emptyByte = " ".getBytes()[0];
  
  CommCrdRoutine comcr = null;
  CommCrd comc = new CommCrd();
  CommDate commDate = new CommDate();
  String tempUser = "";

  private final String FILE_PATH = "media/crd";
  private final String FILE_NAME_HEAD = "CTMCHGW1";
  
  
  private int noChgCnt = 0;

  public int mainProcess(String[] args) {
    String callBatchSeqno = "";

    try {
      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + PROGNAME);

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }
      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
      tempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, callBatchSeqno);
      // =====================================
      /** 2022/05/18 Justin
      String queryDate = "";
	  // 若沒有給定查詢日期，則查詢日期為系統日
      if(args.length == 0) {
    	  queryDate = selectPtrBusinday();
    	  log(String.format("QueryDate = %s",queryDate));
      }else
      if(args.length == 1) {
          // 檢查參數(查詢日期)
          if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
              showLogMessage("E", "", String.format("日期格式[%s]錯誤，日期格式應為西元年yyyyMMdd", args[0]));
              return -1;
          }
          queryDate = args[0];
      }else {
          comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
      }  
      **/
      // =====================================
      String fileName = "";
//      fileName = String.format("%s_%s", fileNameHead, queryDate);
      fileName = FILE_NAME_HEAD;
      
      // get the fileFolderPath such as C:\EcsWeb\media\crd
      String filePath = getFilePath(comc.getECSHOME(), FILE_PATH, fileName);

      // open CTMCHGW1 file ==================
      int insertDataNum = 0;
      int inputFile = openInputText(filePath, "MS950");
      if (inputFile == -1) {
        showLogMessage("I", "", String.format("檔案不存在: %s", filePath));
      }else {
    	  ChgInfoObj chgInfoObj = null;
          String text = "";
          int uncommitCnt = 0;
          while (true) {
            text = readTextFile(inputFile);
            if (text.trim().length() != 0) {
            	// 從檔案取得物件
    			chgInfoObj = getChangingInfoObjFromText(text);
    			
    			// 將物件insert至Crd_0cj0
    			insertCrd0cj0Post(chgInfoObj);
    			
    			uncommitCnt++;
    			insertDataNum++;
            }

            if (endFile[inputFile].equals("Y")) {
            	closeInputText(inputFile);
            	if (uncommitCnt > 0) {
            		commitDataBase();
            	}
            	break;
            }
            
            if (uncommitCnt == COMMIT_NUM) {
            	commitDataBase();
            	uncommitCnt = 0;
            }
          }
          
          // move the file to backup
          moveTxtToBackup(filePath, fileName);
      }

//      List<NoChangeReport> noChgReportList = new ArrayList<NoChangeReport>();
      
      // start processing data
      showLogMessage("I", "", "開始讀取CRD_0CJ0_POST並處理......");
      int processNum = modifyCustormerInfo();
      if (processNum == 0) showLogMessage("I", "", "CRD_0CJ0_POST無資料需處理");

      /** 2022/05/26 Justin
      // 輸出未更新報表
      if (noChgReportList.size() != 0) {
			String noChgReportFileName = String.format("%s_%s", "0CJ0_NOCHANGE", sysDate);
			
			// media/crd/error
			Path outputFileFolderPath = Paths.get(comc.getECSHOME(), FILE_PATH, "error");
			
			// create the parent directory if parent the directory is not exist
			Files.createDirectories(outputFileFolderPath);
			
			// get output file path
			String outputFilePath = Paths.get(outputFileFolderPath.toString(), noChgReportFileName).toString();
    	  
			outputNoChgReport(noChgReportList, outputFilePath);
			procFTP(noChgReportFileName, outputFileFolderPath.toString());
			
	  }
	  **/
      
      showLogMessage("I", "", String.format("處理日期: %s, 檔案資料新增筆數: %d筆", sysDate, insertDataNum));
      showLogMessage("I", "", String.format("總處理筆數: %d筆, 未更新筆數: %d筆", processNum, noChgCnt));
      showLogMessage("I", "", "執行結束");
      comcr.hCallErrorDesc = "程式執行結束";
      comcr.callbatchEnd();
      return 0;
    } catch (Exception e) {
      expMethod = "mainProcess";
      expHandle(e);
      return exceptExit;
    } finally {
      finalProcess();
    }
  }

private void insertCrd0cj0Post(ChgInfoObj chgInfoObj) throws Exception {
	daoTable = "CRD_0CJ0_POST";

    setValue("TX_DATE", commDate.tw2adDate(chgInfoObj.date));
    setValue("TX_TIME", chgInfoObj.time);
    setValue("ID_NO", chgInfoObj.id);
    setValue("CHANGE_CODE", chgInfoObj.chgCode);
    setValue("CHANGE_CONTAIN", chgInfoObj.chgContain);
    setValue("ADDR_CHG_FLAG", chgInfoObj.crdCardAddrChgFlag);
    setValue("TX_BRANCH", chgInfoObj.opBrh);
    setValue("TX_ID_NUM", chgInfoObj.opId);
    setValue("ENG_CHG_RIS_FLAG", chgInfoObj.chgCrdCardFlag);
    setValue("DEAD_FLAG", chgInfoObj.deadFlag);
    setValue("MOD_USER", prgmId);
    setValue("MOD_TIME", sysDate + sysTime);
    setValue("MOD_PGM", prgmId);

    insertTable();
}

private int modifyCustormerInfo() throws Exception {
	int processNumber = 0;
	StringBuilder sb = new StringBuilder();
	sb.append("SELECT TX_DATE, TX_TIME, ID_NO, CHANGE_CODE, CHANGE_CONTAIN, ")
	  .append("ADDR_CHG_FLAG, ENG_CHG_RIS_FLAG, DEAD_FLAG, rowid as rowidid ")
	  .append("FROM CRD_0CJ0_POST ")
	  .append("WHERE POST_FLAG <> 'Y' ")
	  .append("AND CHANGE_CODE IN ('04','06','07','16','17','21','22','40','42','44','78') ");
	sqlCmd = sb.toString();
	int cursorIndex = openCursor();
	while (fetchTable(cursorIndex)) {
		ChgInfoObj chgInfoObj = getChInfoObjFromDB();
		processModification(chgInfoObj);
		updateCrd0cj0Post(chgInfoObj);
		commitDataBase();
		processNumber++;
	}
	closeCursor(cursorIndex);
	return processNumber;
}

private void updateCrd0cj0Post(ChgInfoObj chgInfoObj) throws Exception {
	StringBuffer sb = new StringBuffer();
	sb.append(" POST_FLAG = 'Y' , POST_DATE = to_char(sysdate,'yyyyMMdd'),  ")
	  .append(" POST_RESULT_CODE = ? , POST_RESULT_DESC = ?,  ")
	  .append(" MOD_USER = ? , MOD_TIME = sysdate , MOD_PGM = ? ");
	
	updateSQL = sb.toString();
	whereStr = " WHERE rowid = ? ";
	
	setString(1, chgInfoObj.isNoChg ? chgInfoObj.noChgReason.getCode() : "00");
	setString(2, chgInfoObj.isNoChg ? chgInfoObj.noChgReason.getReason() : "");
	setString(3, prgmId);
	setString(4, prgmId);
	setRowId(5, chgInfoObj.rowId);
	
	daoTable = "CRD_0CJ0_POST";
	
	int returnInt = updateTable();
    if (returnInt == 0) {
      String errorlog = String.format("%s %s, chgCode[%s], id[%s], date[%s], time[%s]", 
    		  "fail to update ", "CRD_0CJ0_POST", chgInfoObj.chgCode, chgInfoObj.id, chgInfoObj.date, chgInfoObj.time);
      log(errorlog) ;
      throw new Exception("Fail to update CRD_0CJ0_POST");
    }

}

private ChgInfoObj getChInfoObjFromDB() throws Exception {
	ChgInfoObj chgInfoObj = new ChgInfoObj();
	chgInfoObj.date = getValue("TX_DATE");
	chgInfoObj.time = getValue("TX_TIME");
	chgInfoObj.id = getValue("ID_NO");
	chgInfoObj.chgCode = getValue("CHANGE_CODE");
	chgInfoObj.chgContain = getValue("CHANGE_CONTAIN");
	chgInfoObj.crdCardAddrChgFlag = getValue("ADDR_CHG_FLAG");
	chgInfoObj.chgCrdCardFlag = getValue("ENG_CHG_RIS_FLAG");
	chgInfoObj.deadFlag = getValue("DEAD_FLAG");
	chgInfoObj.rowId = getValue("rowidid");
	return chgInfoObj;
}

private boolean processModification(ChgInfoObj chgInfoObj) throws Exception {
	Map<String, String> changeDataMap = new HashMap<String, String>();
	String changingColumn = "";
	int count = 0;
	
	String idPSeqno = getIdPSeqno(chgInfoObj.id);
	if (idPSeqno==null || idPSeqno.trim().length() == 0) {
		log(String.format("fail to find id_p_seqno [id_no=%s]", chgInfoObj.id));
//		noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason9.getReason(), chgInfoObj));
		chgInfoObj.setNoChgReason(ErrorReason.noChgReason9);
		noChgCnt++;
		return true;
	}
	
	// 「變更項目代碼」
    switch (chgInfoObj.chgCode) {
//      case "03": // 繼承事故停用
//      	if ("Y".equalsIgnoreCase(chgInfoObj.deadFlag)) {
//			String idPSeqno = getIdPSeqno(chgInfoObj.id);
//			if (idPSeqno==null || idPSeqno.trim().length() == 0) {
//				log(String.format("fail to find id_p_seqno [id_no=%s]", chgInfoObj.id));
////				noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason9.getReason(), chgInfoObj));
//				chgInfoObj.setNoChgReason(ErrorReason.noChgReason9);
//				noChgCnt++;
//			}else {
//				updateCard(crdCard, idPSeqno);
//				updateCard(dbcCard, idPSeqno);	
//			}
//		}else {
////			noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason1.getReason(), chgInfoObj));
//			chgInfoObj.setNoChgReason(ErrorReason.noChgReason1);
//			noChgCnt++;
//		}
//      	break;
      	
      	
      case "04": // 住家電話
        changingColumn = "home_tel_no1";
        count = getCountFromCmsChgcolumnLog(chgInfoObj.id, chgInfoObj.date, chgInfoObj.time,
            changingColumn);

        if (count == 0) {
          changeDataMap = getSplitedTel(chgInfoObj.chgContain);
          if (changeDataMap == null) {
//        	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason8.getReason(), chgInfoObj));
        	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason8);
        	  noChgCnt++;
          }else {
        	  boolean doesIdnoExist = false;
              
              if (checkDataExistsIn(crdIdno, chgInfoObj.id)) {
                updateCrdIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
                doesIdnoExist = true;
              }

              if (checkDataExistsIn(dbcIdno, chgInfoObj.id)) {
                updateDbcIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
                doesIdnoExist = true;
              }
              
              if (doesIdnoExist == false) {
//              	noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason3.getReason(), chgInfoObj));
              	chgInfoObj.setNoChgReason(ErrorReason.noChgReason3);
              	noChgCnt++;
    		  } 
          }
        }else {
//      	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason2.getReason(), chgInfoObj));
      	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
      	  noChgCnt++;
        }
        break;

        
      case "07": // 中文姓名
        changingColumn = "chi_name";
        count = getCountFromCmsChgcolumnLog(chgInfoObj.id, chgInfoObj.date, chgInfoObj.time,
            changingColumn);

        if (count == 0) {
          changeDataMap.put("CHI_NAME", chgInfoObj.chgContain);
          
          boolean doesIdnoExist = false;
          
          if (checkDataExistsIn(crdIdno, chgInfoObj.id)) {
            updateCrdIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
            doesIdnoExist = true;
          }

          if (checkDataExistsIn(dbcIdno, chgInfoObj.id)) {
            updateDbcIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
            doesIdnoExist = true;
          }
          
          if (doesIdnoExist == false) {
//          	noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason3.getReason(), chgInfoObj));
          	chgInfoObj.setNoChgReason(ErrorReason.noChgReason3);
          	noChgCnt++;
		  }
          
        }else {
//      	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason2.getReason(), chgInfoObj));
      	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
      	  noChgCnt++;
        }
        break;

        
        
      case "22": // 電子郵件

        changingColumn = "e_mail_addr";
        count = getCountFromCmsChgcolumnLog(chgInfoObj.id, chgInfoObj.date, chgInfoObj.time,
            changingColumn);

        if (count == 0) {
          changeDataMap.put("E_MAIL_ADDR", chgInfoObj.chgContain);
          
          boolean doesIdnoExist = false;

          if (checkDataExistsIn(crdIdno, chgInfoObj.id)) {
            updateCrdIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
            doesIdnoExist = true;
          }

          if (checkDataExistsIn(dbcIdno, chgInfoObj.id)) {
            updateDbcIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
            doesIdnoExist = true;
          }
          
          if (doesIdnoExist == false) {
//          	noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason3.getReason(), chgInfoObj));
          	chgInfoObj.setNoChgReason(ErrorReason.noChgReason3);
          	noChgCnt++;
		  }

        }else {
//      	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason2.getReason(), chgInfoObj));
      	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
      	  noChgCnt++;
        }

        break;

        
        
        
      case "40": // 公司號碼
        changingColumn = "office_tel_no1";
        count = getCountFromCmsChgcolumnLog(chgInfoObj.id, chgInfoObj.date, chgInfoObj.time,
            changingColumn);

        if (count == 0) {
          changeDataMap = getSplitedTel(chgInfoObj.chgContain);
          if (changeDataMap == null) {
//        	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason8.getReason(), chgInfoObj));
        	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason8);
        	  noChgCnt++;
          }else {
        	  boolean doesIdnoExist = false;

              if (checkDataExistsIn(crdIdno, chgInfoObj.id)) {
                updateCrdIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
                doesIdnoExist = true;
              }

              if (checkDataExistsIn(dbcIdno, chgInfoObj.id)) {
                updateDbcIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
                doesIdnoExist = true;
              }
              
              if (doesIdnoExist == false) {
//              	noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason3.getReason(), chgInfoObj));
              	chgInfoObj.setNoChgReason(ErrorReason.noChgReason3);
              	noChgCnt++;
    		  }
          }
          
        }else {
//      	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason2.getReason(), chgInfoObj));
      	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
      	  noChgCnt++;
        }
        break;

        
        
        
      case "42": // 手機號碼
        changingColumn = "cellar_phone";
        count = getCountFromCmsChgcolumnLog(chgInfoObj.id, chgInfoObj.date, chgInfoObj.time,
            changingColumn);

        if (count == 0) {
          changeDataMap.put("CELLAR_PHONE", chgInfoObj.chgContain);
          
          boolean doesIdnoExist = false;

          if (checkDataExistsIn(crdIdno, chgInfoObj.id)) {
            updateCrdIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
            doesIdnoExist = true;
          }

          if (checkDataExistsIn(dbcIdno, chgInfoObj.id)) {
            updateDbcIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
            doesIdnoExist = true;
          }
          
          if (doesIdnoExist == false) {
//          	noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason3.getReason(), chgInfoObj));
          	chgInfoObj.setNoChgReason(ErrorReason.noChgReason3);
          	noChgCnt++;
		  }

        }else {
//      	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason2.getReason(), chgInfoObj));
      	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
      	  noChgCnt++;
        }
        break;

        
        
        
      case "16": // 戶籍地址
        changingColumn = "resident_addr";
        count = getCountFromCmsChgcolumnLog(chgInfoObj.id, chgInfoObj.date, chgInfoObj.time,
            changingColumn);

        if (count == 0) {
          
          boolean isAddrModified = false;

          // 若bill_apply_flag不等於1(戶籍地址)或「信用卡地址變更註記」等於Y，則須更新database
//          if (shouldModifyAddrFrom(actAcno, RESIDENT_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
//              chgInfoObj.crdCardAddrChgFlag) && shouldModifyAddrFrom(dbaAcno, RESIDENT_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
//                      chgInfoObj.crdCardAddrChgFlag)) {
//            updateCrdIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain),
//                chgInfoObj.id);
//            updateDbcIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain),
//                    chgInfoObj.id);
//            isAddrModified = true;
//          }
          
			if (shouldModifyAddrFrom(actAcno, RESIDENT_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
					chgInfoObj.crdCardAddrChgFlag)) {
				updateCrdIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain), chgInfoObj.id);
				isAddrModified = true;
			}

			if (shouldModifyAddrFrom(dbaAcno, RESIDENT_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
					chgInfoObj.crdCardAddrChgFlag)) {
				updateDbcIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain), chgInfoObj.id);
				isAddrModified = true;
			}
          
          if (isAddrModified == false) {
//          	noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason4.getReason(), chgInfoObj));
          	chgInfoObj.setNoChgReason(ErrorReason.noChgReason4);
          	noChgCnt++;
		  }

        }else {
//      	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason2.getReason(), chgInfoObj));
      	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
      	  noChgCnt++;
        }
        break;

        
        
        
      case "17": // 通訊地址
        changingColumn = "mail_addr";
        count = getCountFromCmsChgcolumnLog(chgInfoObj.id, chgInfoObj.date, chgInfoObj.time,
            changingColumn);

        if (count == 0) {
          
          boolean isAddrModified = false;

          // 若bill_apply_flag不等於2(通訊地址)或「信用卡地址變更註記」等於Y，則須更新database
//          if (shouldModifyAddrFrom(actAcno, MAIL_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
//              chgInfoObj.crdCardAddrChgFlag) && shouldModifyAddrFrom(dbaAcno, MAIL_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
//                      chgInfoObj.crdCardAddrChgFlag)) {
//            updateCrdIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain),
//                chgInfoObj.id);
//            updateDbcIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain),
//                    chgInfoObj.id);
//            isAddrModified = true;
//          }
          
			if (shouldModifyAddrFrom(actAcno, MAIL_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
					chgInfoObj.crdCardAddrChgFlag)) {
				updateCrdIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain), chgInfoObj.id);
				isAddrModified = true;
			}

			if (shouldModifyAddrFrom(dbaAcno, MAIL_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
					chgInfoObj.crdCardAddrChgFlag)) {
				updateDbcIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain), chgInfoObj.id);
				isAddrModified = true;
			}

//          
          if (isAddrModified == false) {
//          	noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason5.getReason(), chgInfoObj));
          	chgInfoObj.setNoChgReason(ErrorReason.noChgReason5);
          	noChgCnt++;
		  }

        }else {
//      	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason2.getReason(), chgInfoObj));
      	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
      	  noChgCnt++;
        }
        break;

        
        
        
      case "21": // 公司地址
        changingColumn = "company_addr";
        count = getCountFromCmsChgcolumnLog(chgInfoObj.id, chgInfoObj.date, chgInfoObj.time,
            changingColumn);

        if (count == 0) {
          
          boolean isAddrModified = false;

          // 若bill_apply_flag不等於3(公司地址)或「信用卡地址變更註記」等於Y，則須更新database
//          if (shouldModifyAddrFrom(actAcno, COMPANY_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
//              chgInfoObj.crdCardAddrChgFlag) && shouldModifyAddrFrom(dbaAcno, COMPANY_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
//                      chgInfoObj.crdCardAddrChgFlag)) {
//            updateCrdIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain),
//                chgInfoObj.id);
//            updateDbcIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain),
//                    chgInfoObj.id);
//            isAddrModified = true;
//          }
          
			if (shouldModifyAddrFrom(actAcno, COMPANY_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
					chgInfoObj.crdCardAddrChgFlag)) {
				updateCrdIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain), chgInfoObj.id);
				isAddrModified = true;
			}

			if (shouldModifyAddrFrom(dbaAcno, COMPANY_ADDR_BILL_APPLY_FLAG, chgInfoObj.id,
					chgInfoObj.crdCardAddrChgFlag)) {
				updateDbcIdno(chgInfoObj.chgCode, getSplitedAddress(chgInfoObj.chgContain), chgInfoObj.id);
				isAddrModified = true;
			}

          
          if (isAddrModified == false) {
//          	noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason6.getReason(), chgInfoObj));
          	chgInfoObj.setNoChgReason(ErrorReason.noChgReason6);
          	noChgCnt++;
		  }

        }else {
//      	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason2.getReason(), chgInfoObj));
      	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
      	  noChgCnt++;
        }
        break;

        
        
        
      case "78": // 英文姓名
        String changeEngName = comc.getSubString(chgInfoObj.chgContain,0,25);
        changeDataMap.put("ENG_NAME", changeEngName);
        boolean isUpdateSuc = false;

        String engNameFromDbcIdno = getEngNameFromDbcIdno(chgInfoObj.id);
//        if (engNameFromDbcIdno == null) {
////        	noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason9.getReason(), chgInfoObj));
//        	chgInfoObj.setNoChgReason(ErrorReason.noChgReason9);
//        	noChgCnt++;
//        }
        
        if ( ! changeEngName.equals(engNameFromDbcIdno) && engNameFromDbcIdno != null) {
          boolean isUpdate = updateDbcIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
          if (isUpdate) isUpdateSuc = true;
        }

        List<CardObj> cardObjListFromDB = null;

        // ============================

        cardObjListFromDB =
            getChgEngNameCardObjListFrom(crdCard, chgInfoObj.id, changeEngName);

        if (cardObjListFromDB != null) {
          updateDiffEngNameDataFrom(crdCard, cardObjListFromDB, chgInfoObj, changeEngName);
          boolean isUpdate = true;
          if (chgInfoObj.chgCrdCardFlag.equalsIgnoreCase("Y")) {

            cardObjListFromDB = getCurrentCode0List(cardObjListFromDB);
            if (cardObjListFromDB.size() != 0) {
              isUpdate = updateCard(crdCard, cardObjListFromDB);
              
              insertOnbatToEcs(cardObjListFromDB);
            }
          }
          if (isUpdate) isUpdateSuc = true;
        }

        // ============================

        cardObjListFromDB =
            getChgEngNameCardObjListFrom(dbcCard, chgInfoObj.id, changeEngName);

        if (cardObjListFromDB != null) {
          updateDiffEngNameDataFrom(dbcCard, cardObjListFromDB, chgInfoObj, changeEngName);
          boolean isUpdate = true;
          if (chgInfoObj.chgCrdCardFlag.equalsIgnoreCase("Y")) {
            cardObjListFromDB = getCurrentCode0List(cardObjListFromDB);
            if (cardObjListFromDB.size() != 0) {
          	  isUpdate = updateCard(dbcCard, cardObjListFromDB);
          	
              insertOnbatToEcs(cardObjListFromDB);
            }
          }
          if (isUpdate) isUpdateSuc = true;
        }
        
        if (isUpdateSuc == false) {
//      	  noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason7.getReason(), chgInfoObj));
      	  chgInfoObj.setNoChgReason(ErrorReason.noChgReason7);
      	  noChgCnt++;
		}

        break;

   //   case 06,44 20220601 add ryan
      case "06":// 出生年月日
    	  changingColumn = "birthday";
    	  count = getCountFromCmsChgcolumnLog(chgInfoObj.id, chgInfoObj.date, chgInfoObj.time,
    	    changingColumn);
    	  if (count == 0) {
    		changeDataMap.put("BIRTHDAY", commDate.tw2adDate(chgInfoObj.chgContain));
    		  
    		boolean doesIdnoExist = false;

    		if (checkDataExistsIn(crdIdno, chgInfoObj.id)) {
				updateCrdIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
				doesIdnoExist = true;
			}

			if (checkDataExistsIn(dbcIdno, chgInfoObj.id)) {
				updateDbcIdno(chgInfoObj.chgCode, changeDataMap, chgInfoObj.id);
				doesIdnoExist = true;
			}

			if (doesIdnoExist == false) {
				chgInfoObj.setNoChgReason(ErrorReason.noChgReason3);
				noChgCnt++;
			}
    	  }else {
    		  	chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
    		  	noChgCnt++;
    	  }
    	  
    	  break;
    	  
      case "44":// 居留證起迄日
    	  	String passportDate = "",residentNoExpireDate = "";
    	  	String regex = "[^0-9-]";
    	  	String inputStr = Pattern.compile(regex).matcher(chgInfoObj.chgContain).replaceAll("").trim();
    	  	String[] arrayDates = inputStr.split("-");
    	
    		passportDate = (arrayDates.length>0)?arrayDates[0]:"";
    		residentNoExpireDate = (arrayDates.length>1)?arrayDates[1]:"";
  
    		changeDataMap.put("PASSPORT_DATE", passportDate);
    		changeDataMap.put("RESIDENT_NO_EXPIRE_DATE", residentNoExpireDate); 
    		boolean doesIdnoExist = false;

    		if (checkDataExistsIn(crdIdno, chgInfoObj.id)) {
				updateCrdIdno("441", changeDataMap, chgInfoObj.id);
				doesIdnoExist = true;
			}

			if (checkDataExistsIn(dbcIdno, chgInfoObj.id)) {
				updateDbcIdno("442", changeDataMap, chgInfoObj.id);
				doesIdnoExist = true;
			}

			if (doesIdnoExist == false) {
				chgInfoObj.setNoChgReason(ErrorReason.noChgReason3);
				noChgCnt++;
			}
    	  
    	  break;

        
      default:
        throw new Exception("變更項目代碼錯誤");

    }
	
    return true;
}

private void moveTxtToBackup(String filePath, String fileName) throws IOException, Exception {
	// media/crd/backup
	Path backupFileFolderPath = Paths.get(comc.getECSHOME(), FILE_PATH, "backup");
	// create the parent directory if parent the directory is not exist
	Files.createDirectories(backupFileFolderPath);
	// get output file path
	String backupFilePath = Paths.get(backupFileFolderPath.toString(), fileName + "." + sysDate + sysTime).toString();
	
	moveFile(filePath, backupFilePath);
}
  
  private void procFTP(String fileName, String fileFolder) throws Exception {
	  CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
	  CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
	  
	  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
      commFTP.hEriaLocalDir = fileFolder;
      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
      commFTP.hEflgModPgm = javaProgram;     

      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
      showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
      int errCode = commFTP.ftplogName("NCR2EMP", "mput " + fileName);
      
      if (errCode != 0) {
          showLogMessage("I", "", "ERROR:無法傳送 " + fileName + " 資料"+" errcode:"+errCode);
          insertEcsNotifyLog(fileName, comr, commFTP);          
      }
  }
  
	private int insertEcsNotifyLog(String fileName, CommRoutine comr, CommFTP commFTP) throws Exception {
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
	
	private void moveFile(String srcFilePath, String targetFilePath) throws Exception {
		
		if (comc.fileMove(srcFilePath, targetFilePath) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + srcFilePath + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + srcFilePath + "] 已移至 [" + targetFilePath + "]");
	}

	/****************************************************************************/

  private void outputNoChgReport(List<NoChangeReport> noChgReportList, String outputFilePath) throws Exception {

	int outFileIndex = openBinaryOutput2(outputFilePath);

	writeFile(noChgReportList, outFileIndex);

	showLogMessage("I", "", String.format("產出未更新報表檔: %s", outputFilePath));

	closeBinaryOutput2(outFileIndex);
	
  }

private void writeFile(List<NoChangeReport> noChgReportList, int outFileIndex) throws Exception {
	int size = noChgReportList.size();
	for (int i = 0; i < size; i++) {
		NoChangeReport noChangeReport = noChgReportList.get(i);
		writeFileInCertainLength(outFileIndex, noChangeReport.chgKey, 10);
		writeFileInCertainLength(outFileIndex, noChangeReport.chgDate, 7);
		writeFileInCertainLength(outFileIndex, noChangeReport.crdCardTransTime, 6);
		writeFileInCertainLength(outFileIndex, noChangeReport.chgCode, 2);
		writeFileInCertainLength(outFileIndex, noChangeReport.chgContain, 110);
		writeFileInCertainLength(outFileIndex, noChangeReport.crdCardAddrChgFlag, 1);
		writeFileInCertainLength(outFileIndex, noChangeReport.chgCrdCardFlag, 1);
		writeFileInCertainLength(outFileIndex, noChangeReport.deadFlag, 1);
		writeFileInCertainLength(outFileIndex, noChangeReport.noChgReason, 200);
		writeFileInCertainLength(outFileIndex, System.lineSeparator(), lineSeparatorBytes.length);
	}
	
}

private void writeFileInCertainLength(int outFileIndex, String str, int targetLength) throws Exception {

	byte[] byteArr = str.getBytes("MS950");

	writeBinFile2(outFileIndex, byteArr, byteArr.length);

	int emptyLength = targetLength - byteArr.length;

	if (emptyLength == 0)
		return;

	byte[] emptyByteArr = new byte[emptyLength];
	for (int i = 0; i < emptyLength; i++) {
		emptyByteArr[i] = emptyByte;
	}

	writeBinFile2(outFileIndex, emptyByteArr, emptyLength);
	
}

/**
   * update xxx_card by id_p_seqno
   * @param tableName
   * @param idPSeqno
   * @return
   * @throws Exception
   */
	private boolean updateCard(String tableName, String idPSeqno) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" CURRENT_CODE = '1' ,  OPPOST_REASON = 'R1',  OPPOST_DATE = to_char(sysdate,'yyyyMMdd'),  ")
			.append(" MOD_USER = ? , MOD_TIME = sysdate , MOD_PGM = ? ");
		updateSQL = sb.toString();
		whereStr = " WHERE CURRENT_CODE = '0' AND ID_P_SEQNO = ? ";
		setString(1,prgmId);
		setString(2, prgmId);
		setString(3, idPSeqno);
		
		daoTable = tableName;
		
		int returnInt = updateTable();
	    if (returnInt == 0) {
	      log(String.format("%s %s [id_p_seqno = %s]", "fail to update ", tableName, idPSeqno)) ;
	    }
		
		return true;
	}

private String getIdPSeqno(String id) throws Exception {
	String idPSeqno = "";
	selectSQL = " ID_P_SEQNO ";
    daoTable = "CRD_IDNO";
    whereStr = " where id_no = ? FETCH FIRST 1 ROWS ONLY ";

    setString(1, id);
    
    int selectCnt = selectTable();
    
    if (selectCnt > 0 ) {
    	idPSeqno = getValue("ID_P_SEQNO");
	}
    
	selectSQL = " ID_P_SEQNO ";
    daoTable = "DBC_IDNO";
    whereStr = " where id_no = ? FETCH FIRST 1 ROWS ONLY ";

    setString(1, id);
    
    selectCnt = selectTable();
    
    if (selectCnt > 0 ) {
    	idPSeqno = getValue("ID_P_SEQNO");
	}

    return idPSeqno;
}

/**
   * 若取得的billApplyFlag為null，則return false。 若billApplyFlag等於targetBillApplyFlag 或 信用卡地址變更註記( ciufg
   * )==Y，則return true。
   * 
   * @param tableName
   * @param targetBillApplyFlag
   * @param id
   * @param crdCardAddrChgFlag
   * @return billApplyFlag
   * @throws Exception
   */
  private boolean shouldModifyAddrFrom(String tableName, String targetBillApplyFlag, String id,
      String crdCardAddrChgFlag) throws Exception {
    String billApplyFlag = null;
    billApplyFlag = getBillApplyFlagFrom(tableName, id);
    if (billApplyFlag != null) {
      if (!billApplyFlag.equals(targetBillApplyFlag) || crdCardAddrChgFlag.equalsIgnoreCase("Y")) {
        return true;
      }
    }
    return false;
  }

  private boolean checkDataExistsIn(String tableName, String id) throws Exception {
    selectSQL = " count(*) count ";
    daoTable = tableName;
    whereStr = " where 1=1 " + " and id_no = ? ";

    setString(1, id);

    selectTable();

    if (getValueInt("count") <= 0)
      return false;

    return true;
  }

  /**
   * update all cards whose English names are different from changeEngName
   * 
   * @param tableName : table name
   * @param cardObjList
   * @param changeEngName
   * @throws Exception
   */
  private void updateEngName(String tableName, List<CardObj> cardObjList, String changeEngName)
      throws Exception {
    boolean isFirstParameter = true;
    int i = 1;

    daoTable = tableName;

    updateSQL = " ENG_NAME = ? , MOD_USER = ? , MOD_TIME = SYSDATE , MOD_PGM = ? ";

    setString(i++, changeEngName);
    setString(i++, modUser);
    setString(i++, prgmId);

    StringBuffer sb = new StringBuffer();
    sb.append(" where card_no in (  ? ");
    for (CardObj cardObj : cardObjList) {
      if (isFirstParameter) {
        setString(i++, cardObj.cardNo);
        isFirstParameter = false;
      } else {
        sb.append(" ,? ");
        setString(i++, cardObj.cardNo);
      }
    }
    sb.append(" ) ");
    
    whereStr = sb.toString();

    int returnInt = updateTable();
    if (returnInt == 0) {
      throw new Exception("fail to update " + tableName);
    }

  }

  /**
   * update all cards whose English names are different from changeEngName
   * 
   * @param tableName : table name
   * @param cardObjList
   * @throws Exception
   */
  private boolean updateCard(String tableName, List<CardObj> cardObjList) throws Exception {
    boolean isFirstParameter = true;
    int i = 1;

    daoTable = tableName;
    
    updateSQL = " CURRENT_CODE = '4',  OPPOST_DATE = TO_CHAR(SYSDATE, 'yyyyMMdd' ), "
        + " OPPOST_REASON = 'S1',  MOD_USER = ?,  MOD_PGM = ?,  MOD_TIME = SYSDATE ";

    setString(i++, modUser);
    setString(i++, prgmId);
    
    StringBuffer bf= new StringBuffer();

    bf.append(" where card_no in (  ? ");
    for (CardObj cardObj : cardObjList) {
      if (isFirstParameter) {
        setString(i++, cardObj.cardNo);
        isFirstParameter = false;
      } else {
        bf.append(" ,? ");
        setString(i++, cardObj.cardNo);
      }
    }
    bf.append(whereStr += " ) ");
    whereStr = bf.toString();

    int returnInt = updateTable();
    if (returnInt == 0) {
      log(String.format("fail to update %s", tableName) );
      return false;
    }
    return true;
  }

  /**
   * 
   * @param changeCode
   * @param updatedDataMap
   * @param id
   * @throws Exception
   */
  private void updateCrdIdno(String changeCode, Map<String, String> updatedDataMap, String id)
      throws Exception {
    daoTable = crdIdno;

    int nthInsertValue = setCrdOrDbcIdnoUpdateSql(changeCode, updatedDataMap);

    whereStr = " where id_no = ?";
    setString(nthInsertValue++, id);

    updateTable();

  }

  /**
   * 
   * @param changeCode
   * @param updatedDataMap
   * @param id
   * @throws Exception
   */
  private boolean updateDbcIdno(String changeCode, Map<String, String> updatedDataMap, String id)
      throws Exception {
    daoTable = dbcIdno;

    int nthInsertValue = setCrdOrDbcIdnoUpdateSql(changeCode, updatedDataMap);

    whereStr = " where id_no = ?";
    setString(nthInsertValue++, id);

    int updateCnt = updateTable();
    return updateCnt > 0;

  }

  /**
   * update different English name data from the table
   * 
   * @param tableName
   * @param cardObjListFromDB
   * @param chgInfoObj
   * @param changeEngName
   * @throws Exception
   */
  private void updateDiffEngNameDataFrom(String tableName, List<CardObj> cardObjListFromDB,
      ChgInfoObj chgInfoObj, String changeEngName) throws Exception {

    if (cardObjListFromDB.size() != 0) {
      // update all cards whose English names are different from changeEngName
      updateEngName(tableName, cardObjListFromDB, changeEngName);
    }
  }

  /**
   * insert into onbat_2exc
   * 
   * @param cardObjListFromDB
   * @throws Exception
   */
  private void insertOnbatToEcs(List<CardObj> cardObjListFromDB) throws Exception {

    for (CardObj cardObj : cardObjListFromDB) {
      daoTable = "onbat_2ecs";

      setValue("trans_type", "6");
      setValue("to_which", "1");
      setValue("dog", sysDate + sysTime);
      setValue("proc_mode", "0");
      setValue("card_no", cardObj.cardNo);
      setValue("acct_type", cardObj.acctType);
      setValue("acno_p_sqeno", cardObj.acnoPSeqno);
      setValue("id_p_seqno", cardObj.idPSeqno);
      setValue("opp_type", "4");
      setValue("opp_reason", "S1");
      setValue("opp_date", sysDate);
      setValue("is_renew", "Y");
      setValueInt("curr_tot_lost_amt", 100);
      setValue("debit_flag", cardObj.debitFlag);

      insertTable();

    }

  }

  /**
   * set a SQL script for updating table and return a next setting order i
   * 
   * @param changeCode
   * @param updatedDataMap
   * @return
   * @throws Exception
   */
  private int setCrdOrDbcIdnoUpdateSql(String changeCode, Map<String, String> updatedDataMap)
      throws Exception {
    int i = 1;
    switch (changeCode) {
      case "04":
        updateSQL = " HOME_AREA_CODE1 = ? , HOME_TEL_NO1 = ?, HOME_TEL_EXT1 = ? ";
        setString(i++, updatedDataMap.get("AREA_CODE1"));
        setString(i++, updatedDataMap.get("TEL_NO1"));
        setString(i++, updatedDataMap.get("TEL_EXT1"));
        break;
      case "07":
        updateSQL = " CHI_NAME = ?  ";
        setString(i++, comc.getSubString(updatedDataMap.get("CHI_NAME"), 0,50));
        break;
      case "22":
        updateSQL = " E_MAIL_ADDR = ? ,E_MAIL_FROM_MARK = 'J' , E_MAIL_CHG_DATE = to_char(sysdate,'yyyyMMdd') ";
        setString(i++, comc.getSubString(updatedDataMap.get("E_MAIL_ADDR"), 0,50));
        break;
      case "40":
        updateSQL = " OFFICE_AREA_CODE1 = ? , OFFICE_TEL_NO1 = ?, OFFICE_TEL_EXT1 = ? ";
        setString(i++, updatedDataMap.get("AREA_CODE1"));
        setString(i++, updatedDataMap.get("TEL_NO1"));
        setString(i++, updatedDataMap.get("TEL_EXT1"));
        break;
      case "42":
        updateSQL = " CELLAR_PHONE = ? ";
        setString(i++, comc.subMS950String(updatedDataMap.get("CELLAR_PHONE").getBytes("MS950"), 0, 15));
        break;
      case "16":
        updateSQL = " RESIDENT_ZIP = ? , RESIDENT_ADDR1 = ?, RESIDENT_ADDR2 = ? , "
            + " RESIDENT_ADDR3 = ?, RESIDENT_ADDR4 = ?, RESIDENT_ADDR5 = ?  ";
        setString(i++, updatedDataMap.get("zipcode") == null ? "" : updatedDataMap.get("zipcode"));
        setString(i++, updatedDataMap.get("city") == null ? "" : comc.getSubString(updatedDataMap.get("city"),0,10));
        setString(i++, updatedDataMap.get("region") == null ? "" : comc.getSubString(updatedDataMap.get("region"),0,10));
        setString(i++, updatedDataMap.get("village") == null ? "" : comc.getSubString(updatedDataMap.get("village"),0,12));
        setString(i++,
            updatedDataMap.get("neighbor") == null ? "" : comc.getSubString(updatedDataMap.get("neighbor"),0,12));
        setString(i++, updatedDataMap.get("others") == null ? "" : comc.getSubString(updatedDataMap.get("others"),0,56));
        break;
      case "17":
        updateSQL = " MAIL_ZIP = ? , MAIL_ADDR1 = ?, MAIL_ADDR2 = ? , "
            + " MAIL_ADDR3 = ?, MAIL_ADDR4 = ?, MAIL_ADDR5 = ?  ";
        setString(i++, updatedDataMap.get("zipcode") == null ? "" : updatedDataMap.get("zipcode"));
        setString(i++, updatedDataMap.get("city") == null ? "" : comc.getSubString(updatedDataMap.get("city"),0,10));
        setString(i++, updatedDataMap.get("region") == null ? "" : comc.getSubString(updatedDataMap.get("region"),0,10));
        setString(i++, updatedDataMap.get("village") == null ? "" : comc.getSubString(updatedDataMap.get("village"),0,12));
        setString(i++,
            updatedDataMap.get("neighbor") == null ? "" : comc.getSubString(updatedDataMap.get("neighbor"),0,12));
        setString(i++, updatedDataMap.get("others") == null ? "" : comc.getSubString(updatedDataMap.get("others"),0,56));
        break;
      case "21":
        updateSQL = " COMPANY_ZIP = ? , COMPANY_ADDR1 = ?, COMPANY_ADDR2 = ? , "
            + " COMPANY_ADDR3 = ?, COMPANY_ADDR4 = ?, COMPANY_ADDR5 = ?  ";
        setString(i++, updatedDataMap.get("zipcode") == null ? "" : updatedDataMap.get("zipcode"));
        setString(i++, updatedDataMap.get("city") == null ? "" : comc.getSubString(updatedDataMap.get("city"),0,10));
        setString(i++, updatedDataMap.get("region") == null ? "" : comc.getSubString(updatedDataMap.get("region"),0,10));
        setString(i++, updatedDataMap.get("village") == null ? "" : comc.getSubString(updatedDataMap.get("village"),0,12));
        setString(i++,
            updatedDataMap.get("neighbor") == null ? "" : comc.getSubString(updatedDataMap.get("neighbor"),0,12));
        setString(i++, updatedDataMap.get("others") == null ? "" : comc.getSubString(updatedDataMap.get("others"),0,56));
        break;
      case "78":
        updateSQL = " ENG_NAME = ?  ";
        setString(i++, comc.getSubString(updatedDataMap.get("ENG_NAME"), 0,25));
        break;
   //   case 06 441 442 20220601 add ryan
      case "06":
    	  updateSQL = " BIRTHDAY = ?  ";
          setString(i++, comc.subMS950String(updatedDataMap.get("BIRTHDAY").getBytes("MS950"), 0, 8));
    	  break;
      case "441":
    	  updateSQL = " PASSPORT_DATE = ? , RESIDENT_NO_EXPIRE_DATE = ? ";
          setString(i++, updatedDataMap.get("PASSPORT_DATE"));
          setString(i++, updatedDataMap.get("RESIDENT_NO_EXPIRE_DATE"));
    	  break;
      case "442":
    	  updateSQL = " RESIDENT_NO_EXPIRE_DATE = ?  ";
          setString(i++, updatedDataMap.get("RESIDENT_NO_EXPIRE_DATE"));
    	  break;
      default:
    	  throw new Exception(String.format("找不到changeCode[%s]", changeCode));
    }
    updateSQL += " , MOD_USER = ? , MOD_TIME = SYSDATE , MOD_PGM = ? ";
    setString(i++, modUser);
    setString(i++, prgmId);

    return i;
  }

  /**
   * 從指定table取得一個裝著所有查詢出來的CardObj物件的List
   * 
   * @param tableName
   * @param id
   * @param changeEngName : 更改的英文名
   * @return
   * @throws Exception
   */
  private List<CardObj> getChgEngNameCardObjListFrom(String tableName, String id,
      String changeEngName) throws Exception {
    String debitFlag = "";

    selectSQL = " CARD_NO, CURRENT_CODE, ACCT_TYPE, ID_P_SEQNO, ";

    // 2022/06/16 Justin 修正弱點
    switch (tableName.toUpperCase(Locale.TAIWAN)) {
      case crdCard:
        selectSQL += " ACNO_P_SEQNO AS ACNO_P_SEQNO ";
        debitFlag = "";
        break;
      case dbcCard:
        selectSQL += " P_SEQNO AS ACNO_P_SEQNO ";
        debitFlag = "Y";
        break;
    }

    daoTable = tableName;
    whereStr = " where 1=1 " + " and id_p_seqno = uf_idno_pseqno(?) " + " and eng_name != ? "
        + " and new_end_date > to_char(sysdate, 'yyyyMMdd') ";

    setString(1, id);
    setString(2, changeEngName);

    int selectCount = selectTable();

    if (selectCount == 0) {
      return null;
    }

    List<CardObj> cardObjList = new ArrayList<CardObj>();

    for (int i = 0; i < selectCount; i++) {
      cardObjList.add(
          new CardObj(getValue("CARD_NO", i), getValue("CURRENT_CODE", i), getValue("ACCT_TYPE", i),
              getValue("ID_P_SEQNO", i), getValue("ACNO_P_SEQNO", i), debitFlag));
    }

    return cardObjList;
  }

  private String getEngNameFromDbcIdno(String id) throws Exception {
    selectSQL = " ENG_NAME ";
    daoTable = " dbc_idno";
    whereStr = " where 1=1 " + " and id_no = ? ";

    setString(1, id);

    int selectCount = selectTable();

    if (selectCount == 0) {
//      showLogMessage("E", "", String.format("找不到DBC_IDNO的ENG_NAME，其中id_no = %s ", id));
      return null;
    }

    return getValue("ENG_NAME");
  }

  /**
   * 取得帳單申請註記
   * 
   * @param tableName : 要查詢的資料表
   * @param id : 身分證字號
   * @return bill_apply_flag
   * @throws Exception
   */
  private String getBillApplyFlagFrom(String tableName, String id) throws Exception {
    selectSQL = " bill_apply_flag ";
    daoTable = tableName;
    whereStr = " where 1=1 " + " and acct_key = ? ";

    setString(1, id + "0");

    int selectCount = selectTable();

    if (selectCount == 0) {
      return null;
    }

    return getValue("bill_apply_flag");
  }

  /**
   * zipcode 為郵遞區號，city 為縣市，region為鄉鎮市區，village為村里，neighbor為鄰，others為其他
   * 
   * @param changeAddress
   * @return map
   */
  private Map<String, String> getSplitedAddress(String changeAddress) {
    return comc.parseByAddress(changeAddress);
  }

  /**
   * split changingTelephone into three numbers
   * 
   * @param changingTelephone : for example, (02)-1234-5678
   * @return map that contains HOME_AREA_CODE1, HOME_TEL_NO1, HOME_TEL_EXT1
   */
  private Map<String, String> getSplitedTel(String changingTelephone) {
	/** 2022/05/18 Justin
    if (checkTelPhoneFormat(changingTelephone) == false) {
    	showLogMessage("E", "", String.format("電話格式錯誤[%s]", changingTelephone));
    	return null;
    }
	  
    Map<String, String> map = new HashMap<String, String>();
    String[] splitedTelArr = changingTelephone.split("-");
    splitedTelArr[0] = splitedTelArr[0].substring(1, splitedTelArr[0].length() - 1); // (02) -> 02

    map.put("AREA_CODE1", splitedTelArr[0]);
    map.put("TEL_NO1", splitedTelArr[1]);
    map.put("TEL_EXT1", splitedTelArr[2]);
    **/
	
	/** 2022/05/18 Justin **/
	Map<String, String> map = null;
	try {
		String[] telArr = comc.transTelNo(changingTelephone);
		map = new HashMap<String, String>();
		map.put("AREA_CODE1", telArr[0]);
	    map.put("TEL_NO1", telArr[1]);
	    map.put("TEL_EXT1", telArr[2]);
	} catch (Exception e) {
		showLogMessage("W", "", String.format("電話[%s]格式異常", changingTelephone));
		return null;
	}
    return map;
  }

	private boolean checkTelPhoneFormat(String changingTelephone) {
		//========================
		if (changingTelephone == null || changingTelephone.trim().length() == 0) {
			return false;
		}
		//========================
		// check whether the string has two dash
		int dashCnt = 0;
		for (int i = 0; i < changingTelephone.length(); i++) {
			if (changingTelephone.charAt(i) == '-') {
				dashCnt++;
			}
		}
		if (dashCnt != 2) {
			return false;
		}
		// ========================
		
		return true;
	}

/**
   * get the count by the given arguments
   * 
   * @param id
   * @param taiwaneseChangingDate
   * @param changingTime
   * @param changingColumn
   * @return count
   * @throws Exception
   */
  private int getCountFromCmsChgcolumnLog(String id, String date,
      String changingTime, String changingColumn) throws Exception {

    selectSQL = " count(*) as count ";
    daoTable = " CMS_CHGColumn_Log ";
    whereStr = " where  id_p_seqno = uf_idno_pseqno(?) and chg_date = ? "
                      + " and chg_time > ?  and Lower(chg_column) = ? ";

    setString(1, id);
    setString(2, date);
    setString(3, changingTime);
    setString(4, changingColumn);

    selectTable();

    return getValueInt("count");
  }

  /**
   * get file folder path by the project path and the file path selected from database
   * 
   * @param projectPath
   * @param filePathFromDb
   * @param fileNameAndTxt
   * @return
   * @throws Exception
   */
  private String getFilePath(String projectPath, String filePathFromDb, String fileNameAndTxt)
      throws Exception {
    String fileFolderPath = null;

    if (filePathFromDb.isEmpty() || filePathFromDb == null) {
      throw new Exception("file path selected from database is error");
    }
    String[] arrFilePathFromDb = filePathFromDb.split("/");

    fileFolderPath = Paths.get(projectPath).toString();

    for (int i = 0; i < arrFilePathFromDb.length; i++) {
      fileFolderPath = Paths.get(fileFolderPath, arrFilePathFromDb[i]).toString();
    }

    return Paths.get(fileFolderPath, fileNameAndTxt).toString();
  }

  private ChgInfoObj getChangingInfoObjFromText(String text) throws UnsupportedEncodingException {
    return splitBytesArr(text.getBytes("MS950"));
  }

  /**
   * 取出currentCode等於0的List
   * 
   * @param cardObjListFromDB
   * @return
   */
  private List<CardObj> getCurrentCode0List(List<CardObj> cardObjListFromDB) {
    return cardObjListFromDB.stream().filter(cardObj -> cardObj.currentCode.equals("0"))
        .collect(Collectors.toList());
  }

  /**
   * separate columns from the byte array of the given txt file .
   * 
   * @param bytesArr : the byte array of the given txt file
   * @return
   */
  private ChgInfoObj splitBytesArr(byte[] bytesArr) {
    ChgInfoObj changingInfoFile = new ChgInfoObj();
	changingInfoFile.id = CommTxBill.subByteToStr(bytesArr, 0, 10);
//  changingInfoFile.fil1 = CommTxBill.subByteToStr(bytesArr, 10, 11);
	changingInfoFile.date = CommTxBill.subByteToStr(bytesArr, 11, 18); // 民國年
//  changingInfoFile.fil2 = CommTxBill.subByteToStr(bytesArr, 18, 19);
	changingInfoFile.chgCode = CommTxBill.subByteToStr(bytesArr, 19, 21);
//  changingInfoFile.fil3 = CommTxBill.subByteToStr(bytesArr, 21, 22);
	if (bytesArr.length == VALID_BYTES_LENGTH) {
		/** 此為檔案正確格式 **/
		changingInfoFile.chgContain = comc.eraseSpace(CommTxBill.subByteToStr(bytesArr, 22, 132));
		changingInfoFile.crdCardAddrChgFlag = CommTxBill.subByteToStr(bytesArr, 132, 133);
		changingInfoFile.fundSystemChgFlag = CommTxBill.subByteToStr(bytesArr, 133, 134);
		changingInfoFile.opBrh = CommTxBill.subByteToStr(bytesArr, 134, 138);
		changingInfoFile.opId = CommTxBill.subByteToStr(bytesArr, 138, 140);
		changingInfoFile.chgCrdCardFlag = CommTxBill.subByteToStr(bytesArr, 140, 141);
		changingInfoFile.deadFlag = CommTxBill.subByteToStr(bytesArr, 141, 142);
//  	changingInfoFile.fil5 = CommTxBill.subByteToStr(bytesArr, 142, 144);
		changingInfoFile.time = CommTxBill.subByteToStr(bytesArr, 144, 150);
	}else {
		/** TCB檔案的chgContain欄位的全型字會造成資料長度不一致(目前已知長度144 bytes, 148 bytes) **/
		int lostBytes = VALID_BYTES_LENGTH - bytesArr.length;
		changingInfoFile.chgContain = comc.eraseSpace(CommTxBill.subByteToStr(bytesArr, 22, 132-lostBytes));
		changingInfoFile.crdCardAddrChgFlag = CommTxBill.subByteToStr(bytesArr, 132-lostBytes, 133-lostBytes);
		changingInfoFile.fundSystemChgFlag = CommTxBill.subByteToStr(bytesArr, 133-lostBytes, 134-lostBytes);
		changingInfoFile.opBrh = CommTxBill.subByteToStr(bytesArr, 134-lostBytes, 138-lostBytes);
		changingInfoFile.opId = CommTxBill.subByteToStr(bytesArr, 138-lostBytes, 140-lostBytes);
		changingInfoFile.chgCrdCardFlag = CommTxBill.subByteToStr(bytesArr, 140-lostBytes, 141-lostBytes);
		changingInfoFile.deadFlag = CommTxBill.subByteToStr(bytesArr, 141-lostBytes, 142-lostBytes);
//  	changingInfoFile.fil5 = CommTxBill.subByteToStr(bytesArr, 142-lostBytes, 144-lostBytes);
		changingInfoFile.time = CommTxBill.subByteToStr(bytesArr, 144-lostBytes, 150-lostBytes);
	}
    
    return changingInfoFile;
  }
  
  private String selectPtrBusinday() throws Exception {

		sqlCmd = " select business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		return getValue("business_date");
	}

  public static void main(String[] args) {
    EtbA001 proc = new EtbA001();
    int retCode = proc.mainProcess(args);
    System.exit(retCode);
  }

  class ChgInfoObj {
    String id = ""; // 身份證號 or 變更key
    String fil1 = ""; // 空白
    String date = ""; // 變更日期 民國年YYYMMDD
    String fil2 = ""; // 空白
    String chgCode = ""; // 變更代號
    String fil3 = ""; // 空白
    String chgContain = ""; // 變更後內容
    String crdCardAddrChgFlag = ""; // 信用卡地址變更註記 Y:需一併變更信用卡地址 N:不需變更信用卡地址
    String fundSystemChgFlag = ""; // 基金系統變更註記
    String opBrh = ""; // 支庫代號
    String opId = ""; // 櫃員代碼
    String chgCrdCardFlag = ""; // 更換信用卡註記
    String deadFlag = ""; // 繼承事故停用信用卡註記 已無使用
    String fil5 = ""; // 空白
//    String countCnt = ""; // 筆數
    String time = ""; // 信用卡交易時間 hhmmss
//    String fil6 = ""; // 空白
    
    String rowId = "";
    boolean isNoChg = false;
    ErrorReason noChgReason = null;
    
	public void setNoChgReason(ErrorReason nochgreason8) {
		this.isNoChg = true;
		this.noChgReason = nochgreason8;
	}
	
  }
  class CardObj {

    String cardNo; // 卡號
    String currentCode;
    String acctType;
    String idPSeqno;
    String acnoPSeqno; // acno_p_seqno, if table = crd_card while p_seqno, if table = dbc_card
    String debitFlag;

    /**
     * 
     * @param cardNo : 卡號
     * @param currentCode
     * @param acctType
     * @param idPSeqno
     * @param acnoPSeqno : acnoPSeqno = acno_p_seqno, if table = crd_card, while acnoPSeqno =
     *        p_seqno, if table = dbc_card
     * @param debitFlag
     */
    public CardObj(String cardNo, String currentCode, String acctType, String idPSeqno,
        String acnoPSeqno, String debitFlag) {
      this.cardNo = cardNo;
      this.currentCode = currentCode;
      this.acctType = acctType;
      this.idPSeqno = idPSeqno;
      this.acnoPSeqno = acnoPSeqno;
      this.debitFlag = debitFlag;
    }

  }
  
  class NoChangeReport{
	  String chgKey; //	  變更KEY
	  String chgDate; //	  變更日期
	  String crdCardTransTime; //	  信用卡交易時間
	  String chgCode; //	  變更代號
	  String chgContain; //	  變更後內容
	  String crdCardAddrChgFlag; //	  信用卡地址變更註記
	  String chgCrdCardFlag; //	  更換信用卡註記
	  String deadFlag; //	  繼承事故告知信用卡停卡
	  String noChgReason; //	  未更新原因
	  
	  NoChangeReport(String noChgReason, ChgInfoObj chgInfoObj){
		  this.chgKey = chgInfoObj.id;
		  this.chgDate = chgInfoObj.date;
		  this.crdCardTransTime = chgInfoObj.time;
		  this.chgCode = chgInfoObj.chgCode;
		  this.chgContain = chgInfoObj.chgContain;
		  this.crdCardAddrChgFlag = chgInfoObj.crdCardAddrChgFlag;
		  this.chgCrdCardFlag = chgInfoObj.chgCrdCardFlag;
		  this.deadFlag = chgInfoObj.deadFlag;
		  this.noChgReason = noChgReason;
	  }
  }
  
  enum ErrorReason{
	  
	  noChgReason1("繼承事故告知信用卡停卡欄位不為Y", "A1"),
	  noChgReason2("當日已有最新資料異動", "A2"),
	  noChgReason3("身份證號不存在卡人資料檔", "A3"),
	  noChgReason4("帳單地址為戶籍地址且信用卡地址變更註記不為Y", "A4"),
	  noChgReason5("帳單地址為通訊地址且信用卡地址變更註記不為Y", "A5"),
	  noChgReason6("帳單地址為公司地址且信用卡地址變更註記不為Y", "A6"),
	  noChgReason7("英文姓名無不同", "A7"),
	  noChgReason8("電話號碼格式有誤", "A8"),
	  noChgReason9("此ID不存在卡人資料檔", "A9"),
	  ;
	  
	  String reason = "";
	  String code = "";
	  private ErrorReason(String reason, String code) {
		  this.reason = reason;
		  this.code = code;
	  }
	  
	  public String getReason() {
		  return reason;
	  }
	  
	  public String getCode() {
		  return code;
	  }
  }
  
}
