/********************************************************************************************
*                                                                                           *
*                              MODIFICATION LOG                                             *
*                                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION                          *
*  ---------  --------- ----------- ------------------------------------------------------  *            
*  106/10/31  V1.00.00    phopho     program initial                                        *
*  109/07/09  V1.00.01    phopho     CR add log to DB: ptr_batch_rpt                        *
*  109/12/09  V1.00.02    shiyuqi       updated for project coding standard                 *
*  112/06/20  V1.00.03    sunny      for Tcb media use                                      *
*  112/06/27  V1.00.04    sunny      調整LAYOUT格式，增加ID_NO                                  *
*  112/08/07  V1.00.05    sunny      修正selectCrdIdpSeqno名稱                                                                *
*  112/09/20  V1.00.06    sunny      檔案不存在，不跳ERROR                                      *
********************************************************************************************/

package Col;

import java.text.Normalizer;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColA410 extends AccessDAO {
    private String progname = "前置協商協客戶繳款清算媒體轉入處理程式  112/09/20  V1.00.06";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptName1 = "ColA410R1";
    String rptDesc1 = "前置協商客戶繳款清算錯誤報表";
    int rptSeq1 = 0;
    String buf = "";
    String szTmp = "";
    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTempSystime = "";
    String hEflgProcCode = "";
    String hEflgRowid = "";
    String hEflgFileName = "";
    String hEflgProcDesc = "";
    String hClnoLiacSeqno = "";
    String hClpyId = "";
    String hClpyIdPSeqno = "";
    String hClpyPaySeqno = "";
    String hClpyFileType = "";
    String hClpyFileTypeAcctNo = "";
    String hEflgFileDate = "";
    String hClpyApplyDate = "";
    String hClpyLiacRemark ="";
    String hClpyBankCode = "";
    String hClpyRegBankNo = "";
    String hClpyAllocateDate = "";
    double hClpyAllocateAmt = 0;

    int forceFlag = 0;
    int totalCnt = 0;
    int errorCnt = 0;
    int warningCnt = 0;
    int addColumnInt = 0;
    int errFlag = 0;
    int warnFlag = 0;
    int lineCnt = 0;
    int pageCnt = 0;
    int recCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String temstr1 = "";
    String temstr2 = "";
    String errStr = "";
    String cmdStr = "";
    
    private int fptr1 = 0;

    public int mainProcess(String[] args) {
        try {
        	dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            
            // 檢查參數
//            if (args.length != 1 && args.length != 2 && args.length != 3) {
//                comc.errExit("Usage : ColA410 file_type file_date [force_flag] ", 
//                							"1.file_type : 'E':本行最大債權行 'F':他行最大債權行");
//            	 comc.errExit("Usage : ColA410 file_date [force_flag]","force_flag: '1' 表強迫轉入 :");
//            	 
//            	 }
            
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

/*
            forceFlag = 0;
            String sGArgs0 = "";
            sGArgs0 = args[0];
            sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
            hClpyFileType = sGArgs0;
            if ((!hClpyFileType.equals("E")) && (!hClpyFileType.equals("F"))) {
                comcr.errRtn( String.format("[%s]無此格式之檔名資料", tmpstr), "", hCallBatchSeqno);
            }
            if ((args.length == 3) && (args[2].equals("Y")))
                forceFlag = 1;
            hEflgFileDate = "";
            if ((args.length >= 2) && (args[1].length() == 8)) {
                String sGArgs1 = "";
                sGArgs1 = args[1];
                sGArgs1 = Normalizer.normalize(sGArgs1, java.text.Normalizer.Form.NFKD);
                hEflgFileDate = sGArgs1;
            }
 */
   
            forceFlag = 0;
            if ((args.length == 2) && (args[1].equals("Y")))
                forceFlag = 1;
            	hEflgFileDate = "";
            if ((args.length >= 1) && (args[0].length() == 8)) {
                String sGArgs1 = "";
                sGArgs1 = args[0];
                sGArgs1 = Normalizer.normalize(sGArgs1, java.text.Normalizer.Form.NFKD);
                hEflgFileDate = sGArgs1;
            }
            selectPtrBusinday();
            if (hEflgFileDate.length() == 0)
                hEflgFileDate = hBusiBusinessDate;
            
            showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "]");
//            tmpstr = String.format("%4.4s.%2.2s.%2.2sLLDLN98%1.1s.csv", hEflgFileDate, hEflgFileDate.substring(4),
//                    hEflgFileDate.substring(6), hClpyFileType);
//            hEflgFileName = tmpstr;
            
            hEflgFileName="PaymentC.txt";

            //sunny test 先mark
            //selectEcsFtpLog();
            showLogMessage("I", "", String.format("處理檔案[%s]...", hEflgFileName));
            totalCnt = 0;
            fileOpen();
            errorCnt = 0;
            readFile();
            printTailer();
            comc.writeReport(temstr2, lpar1);
            if (addColumnInt == 1)
                tmpstr1 = String.format("(注意:格式變更)");
            if (errorCnt == 0) {
                tmpstr = String.format("%s媒體共[%d]筆已轉入暫存檔 ,無任何錯誤!", tmpstr1, totalCnt);  //mark:原totalCnt - 2
                showLogMessage("I", "", String.format("%s", tmpstr));
            } else {
                tmpstr = String.format("%s媒體共[%d]筆, 有[%d]筆錯誤", tmpstr1, totalCnt, errorCnt);  //mark:原totalCnt - 2
                showLogMessage("I", "", String.format("%s", tmpstr));
                tmpstr = String.format("報表[COL_A410_%s.txt]", hEflgFileName);
                showLogMessage("I", "", String.format("%s", tmpstr));
            }
            hEflgProcDesc = tmpstr;
            
            //sunny test
            //updateEcsFtpLog();
            comcr.insertPtrBatchRpt(lpar1);  //CR-insert_ptr_batch_rpt

            /*處理完畢後搬檔 SUNNY-TEST*/
            /*
            cmdStr = String.format("mv %s/media/col/%s %s/media/col/backup/%s.%s", comc.getECSHOME(), hEflgFileName,
            		comc.getECSHOME(), hEflgFileName, hBusiBusinessDate+hTempSystime);
            cmdStr = Normalizer.normalize(cmdStr, java.text.Normalizer.Form.NFKD);
            String fs = String.format("%s/media/col/%s", comc.getECSHOME(), hEflgFileName);
            String ft = String.format("%s/media/col/backup/%s.%s", comc.getECSHOME(), hEflgFileName, hTempSystime);
            showLogMessage("I", "", String.format("檔案  : [%s]", hEflgFileName));
            showLogMessage("I", "", String.format("  移至: [%s]", ft));
            if (comc.fileRename(fs, ft) == false) {
                showLogMessage("I", "", String.format("無法搬移[%s]", cmdStr));
            }
            */

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
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
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_systime ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempSystime = getValue("h_temp_systime");
        }

    }

    /***********************************************************************/
    void selectEcsFtpLog() throws Exception {
        hEflgProcCode = "";
        sqlCmd = "select proc_code,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from ecs_ftp_log  ";
        sqlCmd += "where system_id  = 'COL_LIAC'  ";
        sqlCmd += "and trans_resp_code = 'Y'  ";
        sqlCmd += "and proc_code  in ('0','1','9','Y')  ";
        sqlCmd += "and file_name  = ? ";
        setString(1, hEflgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hEflgProcCode = getValue("proc_code");
            hEflgRowid = getValue("rowid");
        } else {
        	exceptExit = 0;
            comcr.errRtn(String.format("[%s]無轉入記錄可處理", hEflgFileName), "", hCallBatchSeqno);
        }

        if (hEflgProcCode.equals("9")) {
            showLogMessage("I", "", String.format("[%s]資料重轉入處理", hEflgFileName));
            return;
        }

        if (hEflgProcCode.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn(String.format("[%s]資料已處理完畢, 不可再轉入", hEflgFileName), "", hCallBatchSeqno);
        }

        if (hEflgProcCode.equals("1")) {
            if (forceFlag == 0) {
            	exceptExit = 0;
                comcr.errRtn(String.format("[%s]資料已轉入,不可重複轉入", hEflgFileName), "", hCallBatchSeqno);
            } else {
                showLogMessage("I", "", String.format("[%s]資料強制轉入處理", hEflgFileName));
                deleteColLiacPay();
                return;
            }
        }

    }

    /***********************************************************************/
    void deleteColLiacPay() throws Exception {
        daoTable = "col_liac_pay";
        whereStr = "where file_date = ? ";
        setString(1, hEflgFileDate);
        deleteTable();

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        temstr1 = String.format("%s/media/col/%s", comc.getECSHOME(), hEflgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        //File f = new File(temstr1);
        //if (f.exists() == false) {
        //    comcr.err_rtn("檔案不存在：" + temstr1, "", h_call_batch_seqno);
        //}
        fptr1 = openInputText(temstr1, "MS950");
        if (fptr1 == -1) {
        	exceptExit = 0;
            comcr.errRtn(String.format("error: [%s] 檔案不存在", temstr1), "", hCallBatchSeqno);
        }

        temstr2 = String.format("%s/reports/COL_A410_%s.txt", comc.getECSHOME(), hBusiBusinessDate);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
    }
    
    /***********************************************************************/   
    /*
        * 前置協商繳款分配款入帳檔案(PaymentC.TXT)--總長度80 BYTES
        01 IN-REC.                           
	    02 IN-ACTNO.                      
	       05 IN-BRNO          PIC X(04). ==>分行代號
	       05 IN-CARDNO        PIC 9(16). ==>信用卡號
	    02 IN-TX-DATE          PIC 9(07). ==>交易日期    yyymmdd轉yyyymmdd
	    02 IN-AMT              PIC 9(08). ==>分配款金額
	    02 IN-PERIOD           PIC 9(03). ==>繳款期數
	    02 IN-OTHER-FEE        PIC 9(08). ==>其它費用
	    02 IN-DIS-DATE.                   ==>繳款日期    實際分配繳款日期
	       05 IN-DIS-DATE-YY   PIC 9(03). 
	       05 IN-DIS-DATE-MM   PIC 9(02). 
	       05 IN-DIS-DATE-DD   PIC 9(02). 
	    02 IN-FILE-MARK        PIC X(01). 
	    02 IN-FILE-ACNO        PIC X(13). ==>最大債權銀行006，會給0030717293700,如不是最大債權銀行，就會0030717293718
	    02 IN-PAYMENT-CNT      PIC 9(03). ==>已繳期數(依ACTNO，非依ID)
	    02 IN-FIL              PIC X(10). ==>身份證字號X(10)
	    
	  原始範例:12215140231021384111112031000000297001000000001120309N0030717293700025
	 資料分析：1221|5140231021384111|1120310|00000297|001|00000000|1120309|N|0030717293700|025
     * */
    
    
    void readFile() throws Exception {
        String str600 = "";
        String stra = "";

        printHeader();
        totalCnt = 0;
 
        while(true) {
            str600 = readTextFile(fptr1);
            if (endFile[fptr1].equals("Y"))
                break;

            errFlag = 0;
            if (str600.length() < 80) continue;
                        
            hClpyRegBankNo = comc.subMS950String(str600.getBytes("MS950"), 0, 4);     //ACTNO 4(20)取前4碼           
			hClpyLiacRemark = comc.subMS950String(str600.getBytes("MS950"), 4, 16);    //ACTNO 4(20) 取後16碼 呆帳以前：分行x(4)+卡號x(16) 呆帳以後：呆帳帳號x(13)
			hClpyAllocateDate = comc.subMS950String(str600.getBytes("MS950"), 20, 7); //交易日期轉民國年
            hClpyAllocateAmt = Integer.parseInt(comc.subMS950String(str600.getBytes("MS950"), 27, 8));   //繳款金額
            hClpyFileTypeAcctNo = comc.subMS950String(str600.getBytes("MS950"), 57, 13); //取最大債權判斷
            hClpyId = comc.subMS950String(str600.getBytes("MS950"), 70, 10); //ID
                       
            if (hClpyFileTypeAcctNo.trim().equals("0030717293700")==true)					
            	hClpyFileType = "E"; //本行為最大債權銀行
            else
            	hClpyFileType = "F"; //本行非最大債權銀行
            	
            
//			if (hClpyLiacRemark.trim().equals("")==true)
//			{						
//	                errStr = "[繳款資訊(卡號)為空白，格式錯誤]";
//	                errFlag = 1;
//			}
			
			//用id查詢id_p_seqno
			hClpyIdPSeqno = selectCrdIdpSeqno(hClpyId);
			
			//用卡號查詢主卡人id_p_seqno
//			hClpyIdPSeqno = selectCrdCard(hClpyLiacRemark);
//			hClpyId = selectCrdIdno(hClpyIdPSeqno);		
			
			//Debug-Log
            tmpstr = String.format("majorId[%s],id_p_seqno[%s],regBankNo[%s],remark[%s],payDate[%s],PayAmt[%8.0f]", hClpyId, hClpyIdPSeqno, hClpyRegBankNo,hClpyLiacRemark,hClpyAllocateDate,hClpyAllocateAmt);
            showLogMessage("I", "", String.format("%s", tmpstr));

			if (hClpyAllocateDate.trim().equals("")==false)
			{		
				/* 繳款(交易)日期, 民國-->西元(有值再處理日期格式化) */
				hClpyAllocateDate = String.valueOf((Integer.parseInt(hClpyAllocateDate) + 19110000));
	            if (!comm.checkDateFormat(hClpyAllocateDate, "yyyyMMdd")) {
	                errStr = "[交易日期(繳款日期)格式錯誤]";
	                errFlag = 1;
	            } 
			}				

            totalCnt++;
//            if (totalCnt == 2)
//                continue;
//            if (totalCnt == 1) {
//                stra = comm.getStr(str600, 2, ",");
//                recCnt = comcr.str2int(stra);
//                continue;
//            }

//            if (str600.length() < 10)
//                continue;
//            stra = comm.getStr(str600, 1, ",");            
//            hClpyId = stra;
//            hClpyIdPSeqno = selectCrdIdno(stra);
//            stra = comm.getStr(str600, 2, ",");
//            stra = comm.getDateFreeFormat(stra);  //phopho for free format date
//            if (stra.trim().equals("")==false)
//            if (!comm.checkDateFormat(stra, "yyyyMMdd")) {
//                errStr = "[協商日期格式錯誤]";
//                errFlag = 1;
//            }
//            hClpyApplyDate = stra;
//            stra = comm.getStr(str600, 3, ",");
//            hClpyBankCode = stra;
//            stra = comm.getStr(str600, 4, ",");
//            hClpyRegBankNo = stra;
//            stra = comm.getStr(str600, 5, ",");
//            stra = comm.getDateFreeFormat(stra);  //phopho for free format date
//            if (stra.trim().equals("")==false)
//            if (!comm.checkDateFormat(stra, "yyyyMMdd")) {
//                errStr = "[還款分配日期格式錯誤]";
//                errFlag = 1;
//            }
//            hClpyAllocateDate = stra;
//            stra = comm.getStr(str600, 6, ",");
//            hClpyAllocateAmt = comcr.str2double(stra);
//            stra = comm.getStr(str600, 7, ",");
            if (stra.length() != 0)
                addColumnInt = 1;
            if (selectColLiacNego() != 0) {
                errStr = "[前協主檔無此ID]";
                errFlag = 1;
            }

            if (errFlag == 1) {
                printDetail();
                errorCnt++;
                continue;
            }
            insertColLiacPay();
        }
//        if (recCnt != totalCnt - 2) {
//            buf = String.format("資料筆數[%d]與實際筆數不符[%d]", recCnt, totalCnt - 2);
//            lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
//            errorCnt++;
//        }
        //br.close();
        closeInputText(fptr1);
    }
    
    /***********************************************************************/
    /*
    void readFile_bak() throws Exception {
        String str600 = "";
        String stra = "";

        printHeader();
        totalCnt = 0;
        
        //FileInputStream fis = new FileInputStream(new File(temstr1));
        //BufferedReader br = new BufferedReader(new InputStreamReader(fis, "MS950"));
        //while ((str600 = br.readLine()) != null) {
        while(true) {
            str600 = readTextFile(fptr1);
            if (endFile[fptr1].equals("Y"))
                break;

            errFlag = 0;

            totalCnt++;
            if (totalCnt == 2)
                continue;
            if (totalCnt == 1) {
                stra = comm.getStr(str600, 2, ",");
                recCnt = comcr.str2int(stra);
                continue;
            }

            if (str600.length() < 10)
                continue;
            stra = comm.getStr(str600, 1, ",");
            hClpyId = stra;
            hClpyIdPSeqno = selectCrdIdno(stra);
            stra = comm.getStr(str600, 2, ",");
            stra = comm.getDateFreeFormat(stra);  //phopho for free format date
            if (stra.trim().equals("")==false)
            if (!comm.checkDateFormat(stra, "yyyyMMdd")) {
                errStr = "[協商日期格式錯誤]";
                errFlag = 1;
            }
            hClpyApplyDate = stra;
            stra = comm.getStr(str600, 3, ",");
            hClpyBankCode = stra;
            stra = comm.getStr(str600, 4, ",");
            hClpyRegBankNo = stra;
            stra = comm.getStr(str600, 5, ",");
            stra = comm.getDateFreeFormat(stra);  //phopho for free format date
            if (stra.trim().equals("")==false)
            if (!comm.checkDateFormat(stra, "yyyyMMdd")) {
                errStr = "[還款分配日期格式錯誤]";
                errFlag = 1;
            }
            hClpyAllocateDate = stra;
            stra = comm.getStr(str600, 6, ",");
            hClpyAllocateAmt = comcr.str2double(stra);
            stra = comm.getStr(str600, 7, ",");
            if (stra.length() != 0)
                addColumnInt = 1;
            if (selectColLiacNego() != 0) {
                errStr = "[主檔無此ID]";
                errFlag = 1;
            }

            if (errFlag == 1) {
                printDetail();
                errorCnt++;
                continue;
            }
            insertColLiacPay();
        }
        if (recCnt != totalCnt - 2) {
            buf = String.format("資料筆數[%d]與實際筆數不符[%d]", recCnt, totalCnt - 2);
            lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
            errorCnt++;
        }
        //br.close();
        closeInputText(fptr1);
    }
    */
    
    /***********************************************************************/
    String selectCrdCard(String asCardNo) throws Exception {
    	String asIdPSeqno = "";
    	//用卡號取得正卡人ID
        sqlCmd = "select major_id_p_seqno from crd_card where card_no = ? ";
        setString(1, asCardNo);
        
        if (selectTable() > 0) {
        	asIdPSeqno = getValue("major_id_p_seqno");
        }

        return asIdPSeqno;
    }
    /***********************************************************************/
    String selectCrdIdno(String asIdPSeqno) throws Exception {
    	String outIdNo = "";
        sqlCmd = "select id_no from crd_idno where id_p_seqno = ? ";
        setString(1, asIdPSeqno);
        
        if (selectTable() > 0) {
        	outIdNo = getValue("id_no");
        }

        return outIdNo;
    }
    /***********************************************************************/
    String selectCrdIdpSeqno(String asIdNo) throws Exception {
    	String outIdPSeqno = "";
        sqlCmd = "select id_p_seqno from crd_idno where id_no = ? ";
        setString(1, asIdNo);
        
        if (selectTable() > 0) {
        	outIdPSeqno = getValue("id_p_seqno");
        }

        return outIdPSeqno;
    }
    
    /***********************************************************************/
    int selectColLiacNego() throws Exception {
        hClnoLiacSeqno = "";
        sqlCmd = "select liac_seqno ";
        sqlCmd += " from col_liac_nego  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hClpyIdPSeqno);
        //sqlCmd += "where id_no = ? ";
        //setString(1, h_clpy_id);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClnoLiacSeqno = getValue("liac_seqno");
        } else
            return (1);

        return (0);
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        String tmpstr = "";

        lineCnt++;
        if (lineCnt >= 28) { //每28筆一頁
         	lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", "##PPP"));
            printHeader();
            lineCnt = 0;
        }
        
        /*
         * 
         *   buf = comcr.insertStr(buf, "身份證號", 1);  //歸屬ID
        buf = comcr.insertStr(buf, "分配日期", 12);
        buf = comcr.insertStr(buf, "分配金額", 22);
        buf = comcr.insertStr(buf, "備      註", 34); //分行、卡號
        buf = comcr.insertStr(buf, "錯誤原因", 56);
         */

        buf = "";
        buf = comcr.insertStr(buf, hClpyId, 1);
        buf = comcr.insertStr(buf, hClpyAllocateDate, 12);
        tmpstr = String.format("%10.0f", hClpyAllocateAmt);
        buf = comcr.insertStr(buf, tmpstr, 22);
        buf = comcr.insertStr(buf, hClpyRegBankNo+"-"+hClpyLiacRemark, 34); //分行、卡號
        buf = comcr.insertStr(buf, errStr, 57);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }
    /***********************************************************************/
    /*
    void printDetailBak() throws Exception {
        String tmpstr = "";

        lineCnt++;
        if (lineCnt >= 50) {
            printHeader();
            lineCnt = 0;
        }

        buf = "";
        buf = comcr.insertStr(buf, hClpyId, 1);
        buf = comcr.insertStr(buf, hClpyApplyDate, 12);
        buf = comcr.insertStr(buf, hClpyBankCode, 23);
        buf = comcr.insertStr(buf, hClpyAllocateDate, 34);
        tmpstr = String.format("%10.0f", hClpyAllocateAmt);
        buf = comcr.insertStr(buf, tmpstr, 45);
        buf = comcr.insertStr(buf, errStr, 56);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }
    */

    /***********************************************************************/
    void printHeader() throws Exception {
    	
        buf = "";
        pageCnt++;
        buf = comcr.insertStr(buf, "報表名稱: ColA410R1", 1);
        //buf = comcr.insertStr(buf, "前置協商(OA)客戶繳款清算", 47);
        buf = comcr.insertStr(buf, rptDesc1, 47);
        buf = comcr.insertStr(buf, "頁    次:", 93);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 101);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日期:", 93);
        buf = comcr.insertStr(buf, chinDate, 101);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "轉入日期:", 1);
        szTmp = String.format("%8d", comcr.str2long(hBusiBusinessDate));
        buf = comcr.insertStr(buf, szTmp, 10);
        buf = comcr.insertStr(buf, "檔案名稱:", 25);
        buf = comcr.insertStr(buf, hEflgFileName, 36);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "還    款", 12);
        buf = comcr.insertStr(buf, "還    款", 22);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "身份證號", 1);  //歸屬ID
        buf = comcr.insertStr(buf, "分配日期", 12);
        buf = comcr.insertStr(buf, "分配金額", 22);
        buf = comcr.insertStr(buf, "備      註", 34); //分行、卡號
        buf = comcr.insertStr(buf, "錯誤原因", 57);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = "\n";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }
    
    /***********************************************************************/
    /*
    void printHeaderBak() throws Exception {
    	
        buf = "";
        pageCnt++;
        buf = comcr.insertStr(buf, "報表名稱: ColA410R1", 1);
        buf = comcr.insertStr(buf, "客戶繳款清算", 47);
        buf = comcr.insertStr(buf, "頁    次:", 93);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 101);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日期:", 93);
        buf = comcr.insertStr(buf, chinDate, 101);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "轉入日期:", 1);
        szTmp = String.format("%8d", comcr.str2long(hBusiBusinessDate));
        buf = comcr.insertStr(buf, szTmp, 10);
        buf = comcr.insertStr(buf, "檔案名稱:", 25);
        buf = comcr.insertStr(buf, hEflgFileName, 36);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "主要債權", 23);
        buf = comcr.insertStr(buf, "還    款", 34);
        buf = comcr.insertStr(buf, "還    款", 45);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "身份證號", 1);
        buf = comcr.insertStr(buf, "協商日期", 12);
        buf = comcr.insertStr(buf, "銀行代號", 23);
        buf = comcr.insertStr(buf, "分配日期", 34);
        buf = comcr.insertStr(buf, "分配金額", 45);
        buf = comcr.insertStr(buf, "錯誤原因", 56);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = "\n";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }
    */
    /***********************************************************************/
    void insertColLiacPay() throws Exception {
        tmpstr = String.format("%010.0f", comcr.getCOLSeq());
        hClpyPaySeqno = tmpstr;

        daoTable = "col_liac_pay";
        extendField = daoTable + ".";
        setValue(extendField+"liac_seqno", hClnoLiacSeqno);
        setValue(extendField+"pay_seqno", hClpyPaySeqno);
        setValue(extendField+"file_type", hClpyFileType);
        setValue(extendField+"file_date", hEflgFileDate);
        setValue(extendField+"id_p_seqno", hClpyIdPSeqno);
        setValue(extendField+"id_no", hClpyId);
        setValue(extendField+"apply_date", hClpyApplyDate);
        setValue(extendField+"bank_code", hClpyBankCode);
        setValue(extendField+"reg_bank_no", hClpyRegBankNo);
        setValue(extendField+"allocate_date", hClpyAllocateDate);
        setValueDouble(extendField+"allocate_amt", hClpyAllocateAmt);
        setValue(extendField+"liac_remark",hClpyLiacRemark );
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"proc_flag", "0");
        setValue(extendField+"proc_date", sysDate);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liac_pay duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void printTailer() throws Exception {
        buf = "\n";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "失  敗: ", 10);
        szTmp = comcr.commFormat("3z,3z,3z", errorCnt);
        buf = comcr.insertStr(buf, szTmp, 20);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void updateEcsFtpLog() throws Exception {
        daoTable = "ecs_ftp_log";
        updateSQL = "proc_code = decode(cast(? as integer),0,'1','9'),";
        updateSQL += " proc_desc = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm   = ? ";
        whereStr = "where rowid = ? ";
        setInt(1, errorCnt);
        setString(2, hEflgProcDesc);
        setString(3, javaProgram);
        setRowId(4, hEflgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ecs_ftp_log not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA410 proc = new ColA410();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
