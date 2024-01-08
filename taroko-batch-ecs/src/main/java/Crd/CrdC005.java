/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/12/23  V1.00.00    Pino      program initial                           *
*  109/02/25  V1.00.01    Pino      program initial                           *
*  109/05/15  V1.00.02    Wilson    新增update ap1_apply_date                  *
*  109/12/17  V1.00.03    shiyuqi       updated for project coding standard   *
*  112/02/08  V1.00.04    Wilson    讀取檔案路徑調整                                                                                      *
*  112/04/25  V1.00.05    Wilson    hCombRtnCode = "000" -> hRejectCode = ""  *
*  112/05/18  V1.00.06    Wilson    新增日期參數                                                                                             *
*  112/06/13  V1.00.07    Wilson    無檔案不當掉                                                                                             *
*  112/06/19  V1.00.08    Wilson    hRejectCode chg to hCheckCode             *
*  112/07/03  V1.00.09    Wilson    假日不執行                                                                                                 *
******************************************************************************/

package Crd;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*接收*/
public class CrdC005 extends AccessDAO {
    private String progname = "讀取COMBO續卡清單回覆檔作業112/07/03  V1.00.09  ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debugD = 0;
    long totalCnt = 0;
    int tmpInt = 0;
    String checkHome = "";
    String hCallErrorDesc = "";
    String hBusinessDate = "";
    String pathName1 = "";

    String prgmId = "CrdC005";
    String rptName1 = "";
    int recordCnt = 0;
    int actCnt = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq1 = 0;
    int errCnt = 0;
    String errMsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hModWs = "";
    String hModLog = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String hTempUser = "";
    String hBusinssDate = "";
    String hSystemDate = "";
    String hCombCardNo = "";
    String hCombRtnIbmDate = "";
    String hCombBatchno = "";
    double hCombRecno = 0;
    String hCombRowid = "";
    String hCombRtnCode = "";
    String hProcDate = "";
    String hCombCard = "";
    String hCombPin = "";
    String hCombIdNo = "";
    String hCombBirth = "";
    String hCombMemo = "";
    String hCheckCode = "";
    String hInMainError = "";
    String hInMainMsg = "";
    String hMailSeqno = "";
    String hCombSavingActno = "";
    String hCombCardKind = "";
    String hCombCardSource = "";
    String hTmpOldCardNo = "";
    String hTmpCardRefNum = "";
    String hTmpAp1ApplyDate = "";
    String ifil = "";
    String fileName2 = "";
    int totalFile ;

    String hOAct1 = "";
    String hStatus = "";
    String temstr1 = "";
    int recCount = 0;
    int allCount = 0;
    int hCount = 0;
    int hNn = 0;

    Buf1 detailSt = new Buf1();
    String hBusiBusinessDate = "";
    String queryDate = "";
	String fileFolderPath = comc.getECSHOME() + "/media/crd/";
    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : CrdC005 callbatch_seqno", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                comcr.hCallBatchSeqno = "no-call";
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                if (selectTable() > 0)
                    hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            hModPgm = javaProgram;
            
//            // 若沒有給定查詢日期，則查詢日期為系統日
//            if(args.length == 0) {
//                queryDate = hBusiBusinessDate;
//            }else
//            if(args.length == 1) {
//                if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
//                    showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
//                    return -1;
//                }
//                queryDate = args[0];
//            }else {
//                comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
//            }               
            
            commonRtn();
            
            showLogMessage("I", "", String.format("今日營業日 = [%s]", hBusinessDate));
            
            if (checkPtrHoliday() != 0) {
				showLogMessage("E", "", "今日為假日,不執行此程式");
				return 0;
            }
            
            openFile();
            
            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=["+ totalCnt + "][" + hCount + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commonRtn() throws Exception {
        sqlCmd = "select business_date,to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += " from ptr_businday ";
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hBusinessDate = getValue("business_date");
            hSystemDate = getValue("h_system_date");
        }
    }

    /***********************************************************************/
    int checkPtrHoliday() throws Exception {
        int hCount = 0;

        sqlCmd = "select count(*) h_count ";
        sqlCmd += " from ptr_holiday  ";
        sqlCmd += "where holiday = ? ";
        setString(1, hBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_holiday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCount = getValueInt("h_count");
        }

        if (hCount > 0) {
            return 1;
        } else {
            return 0;
        }
    }

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

			if (iFileName.length() != 27)
				continue;
			
			if(!comc.getSubString(iFileName,0,13).equals("resp_combo_t_"))  
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
        if (debugD == 1)
            showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") > 0) {
            showLogMessage("D", "", " 此檔案已存在,不可重複轉入 =[" + iFileName + "]");
            return 1;
        }

        return 0;
    }    
    
 // ************************************************************************
    int readFile(String fileName) throws Exception {
        String str900 = "";
		String fileName2;
		fileName2 = fileFolderPath + fileName;
        hCount = 0;

        if (openBinaryInput(fileName2) == false) {
        	return 1;        	
        }
        
    	showLogMessage("I", "", " Process file path =[" + fileFolderPath + " ]");
    	showLogMessage("I", "", " Process file =[" + fileName + "]");

        byte[] bytes = new byte[detailSt.allText().length()];
        while (readBinFile(bytes) > 0) {
            str900 = new String(bytes, "MS950");
                    splitBuf1(str900);
                    moveData();
                    totalCnt++;
        }
        closeBinaryInput();

        String cmdStr = String.format("mv %s %s/media/crd/backup/%s", fileName2, comc.getECSHOME(), iFileName);
        String target = String.format("%s/media/crd/backup/%s", comc.getECSHOME(), iFileName);
        comc.fileRename2(fileName2, target);
        showLogMessage("I", "", String.format("[%s]", cmdStr));
        
        insertFileCtl();
        
        return 0;
    }
    /***********************************************************************/
    public int selectCrdFileCtl() throws Exception {
        selectSQL = "count(*) as all_cnt";
        daoTable = "crd_file_ctl";
        whereStr = "WHERE file_name  = ? ";

        setString(1, iFileName);
        selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") > 0) {
            showLogMessage("D", "", " 此檔案已存在,不可重複轉入 =[" + iFileName + "]");
            return 1;
        }

        return 0;
    }

    /***********************************************************************/
    int moveData() throws Exception {
        String tmpStr = "";

        hCombSavingActno = "";
        hCombCardKind = "";
        hCombCardSource = "";
        hTmpOldCardNo = "";
        hTmpCardRefNum = "";
        hTmpAp1ApplyDate = "";
        
        tmpStr = detailSt.cardKind;
        hCombCardKind = tmpStr;
//        if (!hCombCardKind.equals("C"))
//        	hCheckCode = "D51";
        	
        tmpStr = detailSt.cardSource;
        hCombCardSource = tmpStr;
//        if (!hCombCardSource.equals("T"))
//        	hCheckCode = "D52";

        tmpStr = detailSt.savingActno;
        hCombSavingActno = tmpStr;
        
        tmpStr = detailSt.oldCardNo;
        hTmpOldCardNo = tmpStr;
        
        tmpStr = detailSt.newCardNo;
        hCombCardNo = tmpStr;
        if (debug == 1)
          showLogMessage("I", "", " Read card=[" + hCombCardNo + "]");
    	 
        tmpStr = detailSt.cardRefNum;
        hTmpCardRefNum = tmpStr;
    	 
        tmpStr = detailSt.responseDate;
        hTmpAp1ApplyDate = tmpStr;
        hProcDate = hTmpAp1ApplyDate;
    	 
        tmpStr = detailSt.responseCode;
        hCombRtnCode = tmpStr;
 
        if (debug == 1)
            showLogMessage("I", "", " RTN_CODE=[" + hCombRtnCode + "]");
        if (hCombRtnCode.length() == 0) {
            showLogMessage("I", "", "card_no [" + hCombCardNo + "] 回饋無告知結果");
            return (0);
        }

        if ((hCombCardNo.length() > 0) && (hCombSavingActno.length() > 0)) {
            hCombRtnIbmDate = "";
            hCombBatchno = "";
            hCombRecno = 0;
            hCombRowid = "";
            sqlCmd = "select rtn_ibm_date,";
            sqlCmd += "batchno,";
            sqlCmd += "recno,";
            sqlCmd += "rowid as rowid ";
            sqlCmd += " from crd_combo  ";
            sqlCmd += "where card_no = ? ";
            setString(1, hCombCardNo);
            recordCnt = selectTable();
            if (recordCnt < 1) {
                comcr.errRtn("Error: select crd_combo !", hCombCardNo, comcr.hCallBatchSeqno);
                return (1);
            }
            hCombRtnIbmDate = getValue("rtn_ibm_date");
            hCombBatchno = getValue("batchno");
            hCombRecno = getValueDouble("recno");
            hCombRowid = getValue("rowid");
            if (debug == 1)
                showLogMessage("I", "", " COMBO=[" + hCombBatchno + "]" + hCombRecno);

            if (hCombRtnIbmDate.length() >= 8) {
                comcr.errRtn("此筆資料IBM已回饋,不可重複寫入", hCombCardNo, comcr.hCallBatchSeqno);
                return (1);
            }
            
            updateCrdCombo();
            
if(debug == 1) showLogMessage("I", " 8881 rtn_code=", hCombRtnCode);

            hCheckCode = "";
            if (hCombRtnCode.substring(0, 3).equals("000")) {
            	hCheckCode = "";
            	hInMainError = "";
            	hInMainMsg = "";
                updateCrdEmboss();
                hCount++;
            }
            else if (!hCombRtnCode.equals("000")) {
            	hCheckCode = hCombRtnCode;
            	hInMainError = "1";
            	selectCrdMessage();
                updateCrdEmboss();
            }
        }

        return (0);
    }
    
    /**********************************************************************/    
    void updateCrdCombo() throws Exception{
    	
    	daoTable   = "crd_combo ";
    	updateSQL += " rtn_ibm_date    = ?,";
    	updateSQL += " rtn_code    = ?,";
    	updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm    = ? ";
        whereStr   = "where rowid = ? ";
        setString(1, hProcDate);
        setString(2, hCombRtnCode);
        setString(3, javaProgram);
        setRowId(4, hCombRowid);

        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_combo not found 2 !","",comcr.hCallBatchSeqno);
        }

    }
    
    /**********************************************************************/
    void updateCrdEmboss() throws Exception {
if(debug == 1)
  showLogMessage("I", " update_crd_emboss=", hCombBatchno +","+ hCombRecno +","+ hCheckCode);
        daoTable   = "crd_emboss";
        updateSQL += " check_code = ?,";
        updateSQL += " in_main_error = ?,";
        updateSQL += " in_main_msg = ?,";
        updateSQL += " ap1_apply_date  = ?,";
        updateSQL += " card_ref_num = ?,";
        updateSQL += " mod_time    = sysdate,";
        updateSQL += " mod_pgm     = ? ";
        whereStr   = "where batchno = ? ";
        whereStr  += "  and recno   = ? ";
        setString(1, hCheckCode);
        setString(2, hInMainError);
        setString(3, hInMainMsg);
        setString(4, hTmpAp1ApplyDate);
        setString(5, hTmpCardRefNum);
        setString(6, javaProgram);
        setString(7, hCombBatchno);
        setDouble(8, hCombRecno);
        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_emboss not found 3!", hCombBatchno +","+ hCombRecno
                                                          , comcr.hCallBatchSeqno);
        }    
    }

    /**********************************************************************/
    void selectCrdMessage() throws Exception { 
    	
   	sqlCmd = "select msg ";
       sqlCmd += " from crd_message  ";
       sqlCmd += "where msg_type = 'NEW_CARD' ";
       sqlCmd += "and msg_value = ? ";
       setString(1, hCheckCode);
       recordCnt = selectTable();
       if (recordCnt > 0) {
       	hInMainMsg = getValue("msg");
       }       
   }      
   
   /**********************************************************************/
    void insertFileCtl() throws Exception {
        setValue("file_name", iFileName);
        setValue("crt_date", sysDate);
        setValue("trans_in_date", sysDate);
        daoTable = "crd_file_ctl";
        insertTable();
        if (dupRecord.equals("Y")) {
            daoTable = "crd_file_ctl";
            updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
            whereStr = "where file_name = ? ";
            setString(1, iFileName);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
            }
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
    	CrdC005 proc = new CrdC005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
    	String cardKind;
    	String cardSource;
    	String savingActno;
    	String oldCardNo;
    	String newCardNo;
    	String cardRefNum;
    	String responseDate;
    	String responseCode;
    	String enter;
    	
    	String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(cardKind, 1);
            rtn += comc.fixLeft(cardSource, 1);
            rtn += comc.fixLeft(savingActno, 13);
            rtn += comc.fixLeft(oldCardNo, 16);
            rtn += comc.fixLeft(newCardNo, 16);
            rtn += comc.fixLeft(cardRefNum, 2);
            rtn += comc.fixLeft(responseDate, 8);
            rtn += comc.fixLeft(responseCode, 3);
            rtn += comc.fixLeft(enter, 2);
            return rtn;
        }
    }

    // ************************************************************************
    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        detailSt.cardKind = comc.subMS950String(bytes, 0, 1);
        detailSt.cardSource = comc.subMS950String(bytes, 1, 1);
        detailSt.savingActno = comc.subMS950String(bytes, 2, 13);
        detailSt.oldCardNo = comc.subMS950String(bytes, 15, 16);
        detailSt.newCardNo = comc.subMS950String(bytes, 31, 16);
        detailSt.cardRefNum = comc.subMS950String(bytes, 47, 2);
        detailSt.responseDate = comc.subMS950String(bytes, 49, 8);
        detailSt.responseCode = comc.subMS950String(bytes, 57, 3);
        detailSt.enter = comc.subMS950String(bytes, 60, 3);
    }
    // ************************************************************************
}
