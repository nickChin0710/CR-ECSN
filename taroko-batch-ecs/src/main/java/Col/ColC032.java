/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/05/07  V1.00.00    Rou        program initial                          *
 *  109/12/14  V1.00.01    shiyuqi    updated for project coding standard      *
 *  111/07/15  V1.00.02    Sunny      /media/col/LIAC Fix to /media/col/       *
 *  111/07/29  V1.00.03    Sunny      增加參數設定，若為all則先做delete              *
 *  112/04/12  V1.00.04    Ryan       報表修正                                                                      *
 *  112/04/20  V1.00.05    Ryan       報表筆數顯示修正                                                          *
 *  112/06/17  V1.00.06    Sunny      調整副檔名為大寫.TXT                          *
 *  112/10/05  V1.00.07    Sunny      增加參數all，區分轉檔時處理的檔名                               *                                  
 *  112/10/05  V1.00.08    Ryan       第2檔案處理修正,報表修正                                               *                             
 *  112/11/22  V1.00.09    Ryan       無檔案程式不當掉,調整副檔名為小寫.txt             *     
 *  112/11/28  V1.00.10    Sunny      調整副檔名為小寫.txt                          *
 *  112/12/07  V1.00.11    Sunny      異動檔，調整FILE_NAME每日副檔名為大寫.TXT         *                           
 ******************************************************************************/

package Col;

import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sound.midi.Patch;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC032 extends AccessDAO {
	public final boolean debug = false;
	private static final String FILE_NAME = "ReNewLiqui.TXT";
	private static final String FILE_NAME2 = "ReNewLiqui_ALL.txt";
	private String progname = "處理OA科更生清算資料檔  112/12/07  V1.00.11 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";

    String hTempSysdate = "";
    String temstr1 = "";
    String tmpidNo;
    String tmptansType;
    String tmpliadType;
    String tmpliadStatus;
    String tmpstatusDate;
    String tmpapplyDate;
    String tmpfileDate;
    String tmpdataDate;
    

	int errorCnt = 0;
    String getFileName;
	String tmpIdPSeqno;
    int totalCnt = 0;
    int insertCnt = 0;
    int warnCnt = 0;
    
    /* error report */
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	String rptName1 = "ColC032";
	String rptDesc1 = "更生清算轉入處理錯誤報表";
	String buf = "";
	String szTmp = "";
	String errStr = "";
	int rptSeq1 = 0;
	int pageCnt = 0;
	int lineCnt = 0;
	int warnFlag = 0;
    
    private int fptr1 = 0;
    String hEflgFileName = "";
    String debugFlag     = "";
    
    protected final String dt1Str = "id, tans_type, liad_status, status_date, apply_date, file_date, data_date";

    protected final int[] dt1Length = {10, 1, 1, 7, 13, 7, 7};
    protected  String[] dt1 = new String[] {};
    
    public int mainProcess(String[] args) {

        try {
        	dt1 = dt1Str.split(",");
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (comm.isAppActive(javaProgram)) {
            	exceptExit = 0;
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            if (args.length > 2) {
                comc.errExit("Usage : ColC032 [business_date]/[all(表處理全檔)/alldel(表全檔delete)]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;
            comcr.callbatch(0, 0, 0);

            hBusiBusinessDate = "";
            if ((args.length >=1 )&&( args[0].length() == 8)) {
                hBusiBusinessDate = args[0];
            }
            selectPtrBusinday();
            
            
          //增加執行參數，all代表轉入全檔,delall表轉全檔前先刪除
		    if(args.length>0) {
		    	debugFlag = args[0].toLowerCase(); //參數value一律轉小寫
		    	System.out.println(String.format("有開啟參數 debugFlag = [%s]",debugFlag));
		    }
            		
            hEflgFileName = FILE_NAME;
            
          //如果參數1為all，則令檔名為FILE_NAME2的值
			if ((args.length >= 1) && (args[0].equals("all")|| args[0].equals("alldel"))) {				
				hEflgFileName = FILE_NAME2;
				System.out.println(String.format("本次處理檔案名稱 = [%s]",hEflgFileName));
			}
            
           selectFile();                    
            //fileOpen();
           if(lpar1.size()>0){
	       		comcr.insertPtrBatchRpt(lpar1); /* 寫入ptr_batch_rpt online報表 */
	        }
            // ==============================================
            // 固定要做的1,12,22,32,47,57,67,77
            comcr.callbatch(1, 0, 0);
            showLogMessage("I", "", "程式執行結束, 總筆數 = [" + totalCnt + "],處理=[" + insertCnt + "],未處理=[" + warnCnt + "]");
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
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) busi_business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("busi_business_date");
    }

    /***********************************************************************/
    void deleteColLiadRenewliqui() throws Exception {
    	
        daoTable = "col_liad_renewliqui";
        deleteTable();
    }

    /***********************************************************************/
    void selectFile() throws Exception {
    	
    	String tmpStr = String.format("%s/media/col/", comc.getECSHOME());
        tmpStr = Normalizer.normalize(tmpStr, java.text.Normalizer.Form.NFKD);
//        String file = Paths.get(tmpStr,hEflgFileName).toString();
//        showLogMessage("I","", "File Path = "+ file);
//        List<String> listOfFiles = comc.listFS(tmpStr, "", "");
//        int fileCnt = 0;
//        if (listOfFiles.size() == 0) {
//            comcr.errRtn(String.format("[%s]無檔案可處理!!", tmpStr), "", hCallBatchSeqno);
//        }
//        for (String file : listOfFiles) {
//        	showLogMessage("I","", String.format("File name = [" + file + "]檔名錯誤不予處理 !"));
//            if (file.length() != 14 || file.length() != 18){
////            	showLogMessage("I","", String.format("File name = [" + file + "]檔名錯誤不予處理 !"));
//            	continue; 
//            }
//           if(hEflgFileName.equals(FILE_NAME)) {
//        	   if(file.length() < 10) 
//        		   continue; 
//        	   if (!file.toUpperCase(Locale.TAIWAN).substring(0, 10).equals(FILE_NAME.toUpperCase(Locale.TAIWAN).substring(0,10))) {
////               	showLogMessage("I","", String.format("File name = [" + file + "]檔案不存在 !"));
//                   continue;
//               }
//           }
//          
//           if(hEflgFileName.equals(FILE_NAME2)) {
//        	   if(file.length() < 14) 
//        		   continue; 
//        	   if (!file.toUpperCase(Locale.TAIWAN).substring(0, 14).equals(FILE_NAME2.toUpperCase(Locale.TAIWAN).substring(0,14))) {
////                  	showLogMessage("I","", String.format("File name = [" + file + "]檔案不存在 !"));
//                      continue;
//                  }
//           }
//           fileCnt++;
            getFileName = hEflgFileName;
            showLogMessage("I","", String.format("02 : 開始處理檔案 [%s] ",getFileName));
//            showLogMessage("I","", "Process File = "+ getFileName);
                        
          //-------------------------------------------------------------
		  //sunny增加判斷DEBUG決定是否轉換ID，del代表要處理全檔前先刪除
		  //-------------------------------------------------------------
       	 if (debugFlag.equals("alldel")) {
		  //只有第一次轉檔給全檔，上線之後都是給異動檔            
           showLogMessage("I","", String.format("處理全檔前先刪除【col_liad_renewliqui】all data"));
           deleteColLiadRenewliqui();
       	}
       	
            readFile();
            if(totalCnt == 0)
            	printHeader();
    		printTailer(); // 寫出錯誤報表檔尾
//			comcr.insertPtrBatchRpt(lpar1); /* 寫入ptr_batch_rpt online報表 */
			
//        }
//        if(fileCnt == 0) {
//        	 showLogMessage("I","", String.format("[%s%s]無檔案可處理!!", tmpStr,hEflgFileName));
//        }
    }

    /***********************************************************************/
    
//    void fileOpen() throws Exception {
//		temstr1 = String.format("%s/media/col/%s", comc.getECSHOME(), hEflgFileName);
//		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
//
//		fptr1 = openInputText(temstr1, "MS950");
//		if (fptr1 == -1) {
//			comcr.errRtn(String.format("error: [%s] 檔案不存在", temstr1), "", hCallBatchSeqno);
//		}
//	}

    /***********************************************************************/
    int readFile() throws Exception {
        String str600 = "";
        String filePathName = comc.getECSHOME() + "/media/col/" + getFileName;
        
        int fi = openInputText(filePathName, "MS950");
        if (fi == -1) {
        	showLogMessage("I","", String.format("[%s]無檔案可處理!!", filePathName));
            return 0;
        }

        while(true) {
            str600 = readTextFile(fi);
            if (endFile[fi].equals("Y"))
                break;
            if(str600.length() < 46)
            	continue;   
            totalCnt ++;
            if (moveData(processDataRecord(getFieldValue(str600, dt1Length), dt1)) == 1)            
            	continue;                      
        }
        closeInputText(fi);
        renameFile(getFileName);
        
        return 0;
    }
    
   /************************************************************************/
    int moveData(Map<String, Object> map) throws Exception {
        dateTime();                
        String tmpidNo;
        String tmpTime;
        String tmpDate;
        
        tmpidNo = (String) map.get("id");
      //  showLogMessage("I","", "id_no = "+ tmpidNo);
        if (selectCrdIdno(tmpidNo) == 1)
        	return 1;
        
        setValue("id_p_seqno", tmpIdPSeqno.trim());
        if(debug)showLogMessage("I","", "ID["+ tmpidNo + "]；id_p_seqno["+ tmpIdPSeqno+"]");
        
        tmptansType = (String) map.get("tans_type"); //交易代碼
    	setValue("tans_type", tmptansType.trim());
    	
    	tmpliadType = (String) map.get("liad_status"); //協商類型
    	switch (tmpliadType) {
	    	case "1":
	    	case "2":
	    	case "3":
	    	case "4":
	    	case "5":
	    	case "6":
	    	case "7":
	    		tmpliadType="3";//更生
	    		setValue("liad_type", "3");
	    		break;
	    	case "A":
	    	case "B":
	    	case "C":
	    	case "D":
	    	case "E":
	    	case "F":
	    	case "G":
	    	case "H":
	    		tmpliadType="4"; //清算
	    		setValue("liad_type", "4");
	    		break;
    	}    	
    	
    	tmpliadStatus = (String) map.get("liad_status"); //協商狀態
    	setValue("liad_status", tmpliadStatus.trim());
    	
    	tmpstatusDate = (String) map.get("status_date"); //狀態日期
    	tmpstatusDate = String.valueOf(Integer.parseInt(tmpstatusDate) + 19110000);   	
    	setValue("status_date", tmpstatusDate.trim());
    	
    	tmpapplyDate = (String) map.get("apply_date"); //申請日期時間
    	tmpDate = String.valueOf(Integer.parseInt(tmpapplyDate.substring(0, 7)) + 19110000);
    	tmpTime = tmpapplyDate.substring(7, 13);
    	tmpapplyDate = tmpDate + tmpTime;
    	setValue("apply_date", tmpapplyDate.trim());
    	
    	tmpfileDate = (String) map.get("file_date"); //產檔日期
    	tmpfileDate = String.valueOf(Integer.parseInt(tmpfileDate) + 19110000); 
    	setValue("file_date", tmpfileDate.trim());
    	
    	tmpdataDate = (String) map.get("data_date"); //資料產生日
    	tmpdataDate = String.valueOf(Integer.parseInt(tmpdataDate) + 19110000); 
    	setValue("data_date", tmpdataDate.trim());
    	
    	setValue("mod_time", sysDate + sysTime);
    	setValue("mod_pgm", "ColC032");         
    	
    	insertColLiadRenewliqui();
    	
        return 0;
    }
    
    /***********************************************************************/
    int selectCrdIdno(String tmpIdNo) throws Exception {
    	String idnoIdPSeqno;
    	
    	sqlCmd  = "select id_no, id_p_seqno ";
    	sqlCmd += "from crd_idno ";
    	sqlCmd += "where id_no = ? ";
    	setString(1, tmpIdNo);
    	int recordCnt = selectTable();
    	if (recordCnt > 0) {
    		idnoIdPSeqno = getValue("id_p_seqno");
    		tmpIdPSeqno = idnoIdPSeqno;
    	}
    	else {
    		tmpIdPSeqno = selectCrdChgId(tmpIdNo);
    		if (tmpIdPSeqno.equals(""))
    			return 1;
    	}
    	
    	return 0;
    }
    
    /***********************************************************************/
    int selectCrdCard(String tmpidnoIdPSeqno) throws Exception {
    	int CntSupCard = 0;
    	
    	//查詢是否持有正卡
    	
    	sqlCmd  = "select count(*) as cnt ";
    	sqlCmd += "from crd_card ";
    	sqlCmd += "where id_p_seqno = ? ";
    	sqlCmd += "and   sup_flag='0' ";
    	
    	setString(1, tmpidnoIdPSeqno);
    	int recordCnt = selectTable();
    	if (recordCnt > 0) {
    	
    		CntSupCard = getValueInt("cnt");    		
    		if( CntSupCard > 0) return 1;
    	}    	
    	
    	return 0;
    }
    
    /***********************************************************************/
    //檢核是否為變更ID
    String selectCrdChgId(String tmpIdNo) throws Exception {
    	String chgidIdPSeqno = "";
    	
    	sqlCmd  = "select a.old_id_no, a.id_p_seqno ";
    	sqlCmd += "from crd_chg_id a, crd_idno b ";
    	sqlCmd += "where a.id_no = b.id_no ";
    	sqlCmd += "and a.old_id_no = ? ";
    	setString(1, tmpIdNo);
    	int recordCnt = selectTable();
    	if (recordCnt > 0) 
    		chgidIdPSeqno = getValue("id_p_seqno");   		

    	else {
    		errStr = "[卡人檔無此ID,非本行卡友，跳過不處理]";
    		showLogMessage("I", "", String.format("WARNING X:ID[%s] 非本行卡友，跳過不處理", tmpIdNo));
    		printDetail(tmpIdNo); // 寫入錯誤報表明細
    		warnCnt ++;
    	}
    	
    	return chgidIdPSeqno;
    }
    
    /***********************************************************************/
	void printDetail(String tmpIdNo) throws Exception {
		//tmpIdNo = "";

		if (lineCnt > 25) {
			lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", "##PPP"));
			lineCnt = 0;
		}
		if (lineCnt == 0) {
			printHeader();
		}

		lineCnt++;
		/*均顯示檔案裡的原值-10, 1, 1, 7, 13, 7, 7*/
		//1,12,22,32,47,57,67,77
		buf = "";
		buf = comcr.insertStr(buf, tmpIdNo, 1);        /* 身份證字號 */
		buf = comcr.insertStr(buf, tmptansType, 12);   /* 交易代碼 */
		buf = comcr.insertStr(buf, tmpliadType, 22);   /* 協商類型 */
		buf = comcr.insertStr(buf, tmpliadStatus, 32); /* 協商狀態 */
		buf = comcr.insertStr(buf, tmpapplyDate, 47);  /* 申請日期 */
		buf = comcr.insertStr(buf, tmpfileDate, 57);   /* 產檔日期 */
		buf = comcr.insertStr(buf, tmpdataDate, 67);   /* 資料日期 */
		buf = comcr.insertStr(buf, errStr, 77);        /* 錯誤原因 */
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
	}

	/***********************************************************************/
	void printHeader() throws Exception {

		buf = "";
		pageCnt++;
		buf = comcr.insertStr(buf, "報表名稱: " + rptName1, 1);
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
		buf = comcr.insertStr(buf, "身份證字號", 1);
		buf = comcr.insertStr(buf, "交易代碼", 12);
		buf = comcr.insertStr(buf, "協商類型", 22);
		buf = comcr.insertStr(buf, "協商狀態", 32);
		buf = comcr.insertStr(buf, "申請日期", 42);
		buf = comcr.insertStr(buf, "產檔日期", 52);
		buf = comcr.insertStr(buf, "資料日期", 62);
		buf = comcr.insertStr(buf, "錯誤原因", 80);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
		// buf = "\n";

		// 表頭分隔線=====
		buf = "";
		for (int i = 0; i < 80; i++) {
			buf += "=";
		}
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
	}

	/***********************************************************************/
	void printTailer() throws Exception {
		buf = "\n";
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "失  敗: ", 10);
		szTmp = comcr.commFormat("3z,3z,3z", warnCnt);
		buf = comcr.insertStr(buf, szTmp, 20);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));	    
	}

    /***********************************************************************/
    void insertColLiadRenewliqui() throws Exception {
   	
    	daoTable = "col_liad_renewliqui";    	
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liad_renewliqui!", "", hCallBatchSeqno);
        }
        else
        	insertCnt ++;
    }
    
    /************************************************************************/
    void renameFile(String removeFileName) {
    	String pathStr1 = comc.getECSHOME() + "/media/col/" + removeFileName;
        String pathStr2 = comc.getECSHOME() + "/media/col/backup/" + removeFileName + "." + sysDate;
        
        if (comc.fileRename2(pathStr1, pathStr2) == false) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + pathStr2 + "]");
        return;
    }
    
    /************************************************************************/
    private Map processDataRecord(String[] row, String[] dt) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        int j = 0;
        for (String s : dt) {
            map.put(s.trim(), row[i]);
            i++;
        }
        return map;       
    }
    
    /************************************************************************/
    public String[] getFieldValue(String rec, int[] parm) {
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

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC032 proc = new ColC032();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    
}
