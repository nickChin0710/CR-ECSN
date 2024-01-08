/************************************************************************************************
 *                                                                                              *
 *                              MODIFICATION LOG                                                *
 *                                                                                              *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                 *
 *  ---------  --------- ----------- ----------------------------------------------------       *
 *  112/10/04  V1.00.00    castor               program initial                                 *
 *  112/10/05  V1.00.01    castor               Modify FTP-folder                               *
 ************************************************************************************************/
package Mkt;

import Dxc.Util.SecurityUtil;
import com.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MktW006 extends AccessDAO {
    private boolean DEBUG = false;
    private final String PROGNAME = "漢來美食世界卡電子餐券收檔及回饋資料處理 112/10/05 V1.00.01";
    private CommCrd comc = new CommCrd();
    private CommDate commDate = new CommDate();
    private CommString commString = new CommString();
    private CommCrd commCrd = new CommCrd();
    private CommRoutine comr;
    private CommCrdRoutine comcr;

    //    private final String CRM_FOLDER = "D:/TCBMFT/MKTMPP/FromMPP/RD";
    private final String DATA_FORM_GIFT = "HILAIGIFT_YYYYMM.TXT";
    private final String DATA_FORM_WORLD = "HILAIWORLD_YYYYMM.TXT";
    private final String FILE_FOLDER = "/media/mkt";;
//    private final String FILE_NAME_GIFT_INFO = "MktW006_gift";
//    private final String FILE_NAME_WORLD_INFO = "MktW006_world";
    private final String FILE_NAME_GIFT_RESPONSE = "R_HILAIGIFT_YYYYMM.TXT";
    private final String FILE_NAME_WORLD_RESPONSE = "R_HILAIWORLD_YYYYMM.TXT";
    private final String lineSeparator = System.lineSeparator();

    private String searchDate = ""; 
    int reportPageLine = 34;
    int pageCnt1 = 1, lineCnt1 = 0;
    
    String lineLength; 
    
    String h_card_no        = "";
    String h_id_no          = "";
    String h_purchase_cnt   = "";
    String h_purchase_amt   = "";

    String h_group_code        = "";  //crd_card
    String h_cellar_phone      = "";  //crd_idno
    String h_e_mail_addr       = "";  //crd_idno
    String h_chi_name          = "";  //crd_idno
    String h_bill_sending_zip  = "";  //act_acno + crd_card
    String h_bill_sending_addr = "";  //act_acno + crd_card      
    
    List<Map<String, Object>> lparR1 = new ArrayList<>();
    List<Map<String, Object>> lparR2 = new ArrayList<>();
    String rptIdR1 = "MKTW006GIFT";      
    String rptIdR2 = "MKTW006WORLD";       
    String rptNameR1 = "漢來美食聯名卡促銷活動-滿額禮明細表";
    String rptNameR2 = "漢來美食世界卡促銷活動-滿額禮明細表";
    int rptSeqR1 = 0;
    int rptSeqR2 = 0;

    int totCnt = 0;
    int infoCnt = 0;


    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            showLogMessage("I", "", "Usage MktW006 [business_date]");

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            // =====================================
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            // get searchDate
            selectPtrBusinday();

            if (args.length >= 1) {
                searchDate = args[0];
                showLogMessage("I", "", String.format("程式參數1: [%s]", searchDate));
            } else {
                searchDate = businessDate;
            }

            if (!commDate.isDate(searchDate)) {
                showLogMessage("I", "", "請傳入參數合格值: YYYYMMDD");
                return -1;
            }
            
            showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
            String fileName = "";
            for (int i = 1; i < 3 ; i++) { 
            	
                infoCnt = 0;
               if (i== 1) {
                  fileName = DATA_FORM_GIFT.replace("YYYYMM", searchDate.substring(0, 6));
               }else {
            	  fileName = DATA_FORM_WORLD.replace("YYYYMM", searchDate.substring(0, 6));
               }
               // get the name and the path of the .DAT file
               if (openFile(fileName) == 0) {
                   readFile(fileName);
               }

            }
               showLogMessage("I", "", "                               ");
               showLogMessage("I", "", "執行結束, 總計筆數=[" + totCnt + "]");
               return 0;
        } catch (Exception e) {
            expMethod = "mainProcess";
            expHandle(e);
            return exceptExit;
        } finally {
            finalProcess();
        }
    }

    private void selectPtrBusinday() throws Exception {
        sqlCmd = "select BUSINESS_DATE from PTR_BUSINDAY ";
        selectTable();

        if (notFound.equals("Y")) {
            comc.errExit("執行結束, 營業日為空!!", "");
        }

        businessDate = getValue("BUSINESS_DATE");
    }

    //=============================================================================
    int openFile(String filename) {
        String path = String.format("%s%s/%s", comc.getECSHOME(), FILE_FOLDER, filename);
        if (DEBUG) {
            path = String.format("%s%s/%s", comc.getECSHOME(), FILE_FOLDER, filename);
        }
        path = Normalizer.normalize(path, Normalizer.Form.NFKD);

        int rec = openInputText(path);
        if (rec == -1) {
            showLogMessage("D", "", "無檔案可處理  " + "");
            return 1;
        }

        closeInputText(rec);
        return (0);
    }

    //=============================================================================
    void readFile(String filename) throws Exception {
    	
    	 HashMap<String, String> map = new HashMap<String, String>();
         ValueComparator bvc = new ValueComparator(map);
         TreeMap<String, String> sorted_map = new TreeMap<String, String>(bvc);
    	
        showLogMessage("I", "", "                                 ");
        showLogMessage("I", "", "======== Start Read File ========");
        BufferedReader bufferedReader;
        try {
            String tmpStr = String.format("%s%s/%s", comc.getECSHOME(), FILE_FOLDER, filename);

            if (DEBUG) {
                tmpStr = String.format("%s%s/%s", comc.getECSHOME(), FILE_FOLDER, filename);
            }
            String tempPath = SecurityUtil.verifyPath(tmpStr);
            FileInputStream fileInputStream = new FileInputStream(tempPath);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "MS950"));

            showLogMessage("I", "", "  tempPath = [" + tempPath + "]");
        } catch (FileNotFoundException exception) {
            showLogMessage("I", "", "bufferedReader exception: " + exception.getMessage());
            return;
        }


        String bufGift = ""; //漢來美食聯名卡
        String bufWorld = ""; // 漢來美食世界卡
        String bufGift1 = ""; //漢來美食聯名卡(初表頭)
        String bufWorld1 = ""; // 漢來美食世界卡(初表頭)
        
        String bufInfo = ""; // 未排序報表明細
        String sbufInfo = ""; // 排序後報表明細
        
        String bufResponse = ""; // 兌換抵用回覆檔
        int sum = 0; // 當天折抵金額小計
        String acctMonth = null;
        pageCnt1 = 1;
    
//        if (filename.substring(0, 9).equals("HILAIGIFT")) {
//        	bufGift = rptGift(bufGift);
//        	bufGift1 = bufGift;
//        }else {
//        	bufWorld = rptWorld(bufWorld);
//        	bufWorld1 = bufWorld;
//        }

//        System.out.println("== readFile start ==");
        while ((lineLength = bufferedReader.readLine()) != null) {
            if (lineLength.length() < 39) {
                continue;
            }
            initialField();
            getFileData();
            selectCrdCard();
            selectCrdIdno();
            selectActAcno();
       
//            System.out.println("       ["+infoCnt+"] , group_code["+h_group_code+"] , cellar_phone["+h_cellar_phone+"] , e_mail_addr["+h_e_mail_addr+"] , chi_name["+h_chi_name+"]");
//            System.out.println("                     , bill_sending_zip["+h_bill_sending_zip+"] , bill_sending_addr["+h_bill_sending_addr+"]");


                // 產出回覆檔
                infoCnt++;
//                String temp = "";
//                String stemp = "";
//                temp += commCrd.fixLeft(h_id_no, 17);
//                temp += commCrd.fixLeft(h_chi_name, 19);
//                temp += commCrd.fixLeft(h_group_code, 13);
//                temp += commCrd.fixLeft(h_card_no, 17);
//                temp += commCrd.fixRight(String.valueOf(Integer.valueOf(h_purchase_cnt)), 14);
//                temp += commCrd.fixRight(String.format("%, d",Integer.valueOf(h_purchase_amt)), 21);
//                stemp = temp;
//                temp += lineSeparator;
//                bufInfo += temp;
                
//                map.put(h_id_no+h_group_code+h_card_no,stemp);
////                map.put(h_id_no+h_card_no+h_group_code,stemp);

//              for (String s : temp.split(lineSeparator)) {
//            	  if (filename.substring(0, 9).equals("HILAIGIFT")) {
//                     lparR1.add(comcr.putReport(rptIdR1, rptNameR1, sysDate + sysTime, rptSeqR1++, "1", s));
//            	  }else {
//                     lparR2.add(comcr.putReport(rptIdR2, rptNameR2, sysDate + sysTime, rptSeqR1++, "1", s));
//            	  }
//              }
                bufResponse += commCrd.fixLeft(h_id_no, 10)+",";
                bufResponse += commCrd.fixLeft(h_group_code, 3)+",";
                bufResponse += commCrd.fixLeft(h_card_no, 16)+",";
                bufResponse += commCrd.fixRight(String.valueOf(Integer.valueOf(h_purchase_cnt)), 3)+",";
                bufResponse += commCrd.fixRight(String.valueOf(Integer.valueOf(h_purchase_amt)), 7)+",";
                bufResponse += commCrd.fixLeft(h_cellar_phone, 10)+",";
                bufResponse += commCrd.fixLeft(h_e_mail_addr,30)+",";
                bufResponse += commCrd.fixLeft(h_bill_sending_zip, 3)+",";
                bufResponse += commCrd.fixLeft(h_chi_name, 20)+",";
                bufResponse += commCrd.fixLeft(h_bill_sending_addr, 90);
                bufResponse += lineSeparator;


        }
//        System.out.println("== readFile end ==");
        showLogMessage("I", "", "========  End Read File  ========");
//        showLogMessage("I", "", "                               ");
        
//        showLogMessage("I", "", "======== sort   File ========");
//        System.out.println("unsorted map: " + map);
//        for (String key : map.keySet()) {
////	        System.out.println("unsorted map: " + map.get(key)); 
//	    }
//        sorted_map.putAll(map);
////        System.out.println("results: " + sorted_map); 
//                
//        for (String key : sorted_map.keySet()) {
////            System.out.println("results:  " + sorted_map.get(key));  
//        	sbufInfo += sorted_map.get(key);
//        	sbufInfo += lineSeparator ;
//       	   
//        	if(lineCnt1 > reportPageLine) {
//        		
//        		pageCnt1++;        	      
//        	        
//          	    if (filename.substring(0, 9).equals("HILAIGIFT")) {
//          	      if(pageCnt1 > 1) 	 lparR1.add(comcr.putReport(rptIdR1, rptNameR1, sysDate + sysTime, ++rptSeqR1, "0", "##PPP"));
//                     bufGift = rptGift(bufGift);
//                 }else {
//                	 if(pageCnt1 > 1)  lparR2.add(comcr.putReport(rptIdR2, rptNameR2, sysDate + sysTime, ++rptSeqR2, "0", "##PPP"));
//                     bufWorld = rptWorld(bufWorld);
//                 }
//          	     lineCnt1 = 6;
//          	 }
//            if (filename.substring(0, 9).equals("HILAIGIFT")) {
//                lparR1.add(comcr.putReport(rptIdR1, rptNameR1, sysDate + sysTime, ++rptSeqR1, "1", sorted_map.get(key)));
//       	    }else {
//                lparR2.add(comcr.putReport(rptIdR2, rptNameR2, sysDate + sysTime, ++rptSeqR2, "1", sorted_map.get(key)));
//       	    }
//            
//                      
//            lineCnt1 = lineCnt1 + 1;
//    }
//        showLogMessage("I", "", "======== sort   File ========");
     
        
        if (totCnt == 0) {
        	 if (filename.substring(0, 9).equals("HILAIGIFT")) {
               showLogMessage("I", "", filename + " 檔案內容空檔 !! ");
        	 }else {
        	   showLogMessage("I", "", filename + " 檔案內容空檔 !! ");
        	 }
        }
//        if (infoCnt == 0) {
//            String temp = "";
//            temp += "*** 查無資料 ***";
//            temp += lineSeparator;
//            bufInfo += temp;
//            sbufInfo += temp;
//            if (filename.substring(0, 9).equals("HILAIGIFT")) {
//               bufGift += temp;
//               lparR1.add(comcr.putReport(rptIdR1, rptNameR1, sysDate + sysTime, ++rptSeqR1, "1", temp));
//            }else {
//               bufWorld += temp;
//               lparR2.add(comcr.putReport(rptIdR2, rptNameR2, sysDate + sysTime, ++rptSeqR2, "1", temp));            	
//            }
//        }     

        bufferedReader.close();

        // 寫入[報表檔]
        String datFileInfoPath = "";
        
//        if (filename.substring(0, 9).equals("HILAIGIFT")) {
//           datFileInfoPath = Paths.get(comc.getECSHOME() + FILE_FOLDER, FILE_NAME_GIFT_INFO).toString();
//        }else {
//        	datFileInfoPath = Paths.get(comc.getECSHOME() + FILE_FOLDER, FILE_NAME_WORLD_INFO).toString();
//        }
//        boolean isOpenInfo = openBinaryOutput(datFileInfoPath);
//        if (!isOpenInfo) {
//            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFileInfoPath));
//            return;
//        }
//        byte[] tmpBytes;
        String filenameResponse ="";
        String card_name ="";
        if (filename.substring(0, 9).equals("HILAIGIFT")) {
//            tmpBytes = bufGift1.getBytes("MS950");
            filenameResponse = FILE_NAME_GIFT_RESPONSE.replace("YYYYMM", searchDate.substring(0, 6));
            card_name="聯名";
        }else {
//        	tmpBytes = bufWorld1.getBytes("MS950");
        	filenameResponse = FILE_NAME_WORLD_RESPONSE.replace("YYYYMM", searchDate.substring(0, 6));
        	card_name="世界";
        }
//        writeBinFile(tmpBytes, tmpBytes.length);
//
////        byte[] tmpBytesInfo = bufInfo.getBytes("MS950");
//        byte[] tmpBytesInfo = sbufInfo.getBytes("MS950");
//        writeBinFile(tmpBytesInfo, tmpBytesInfo.length);
//
//        closeBinaryOutput();
        
        showLogMessage("I", "", String.format("寫入漢來美食"+card_name+"卡電子餐券回饋檔結束, 總計筆數[%s]", infoCnt));
        showLogMessage("I", "", "                               ");
        // 寫入ptr_batch_rpt
//        if (filename.substring(0, 9).equals("HILAIGIFT")) {
//           comcr.deletePtrBatchRpt(rptIdR1, sysDate);
//           comcr.insertPtrBatchRpt(lparR1);
//        }else {
//            comcr.deletePtrBatchRpt(rptIdR2, sysDate);
//            comcr.insertPtrBatchRpt(lparR2);        	
//        }
       
        // 產出回饋檔
//        String filenameResponse = FILE_NAME_GIFT_RESPONSE.replace("YYYYMM", searchDate.substring(0, 6));
        String datFileResponsePath = Paths.get(comc.getECSHOME() + FILE_FOLDER, filenameResponse).toString();
        boolean isOpenResponse = openBinaryOutput(datFileResponsePath);
        if (!isOpenResponse) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFileResponsePath));
            return;
        }
        byte[] tmpBytesResponse = bufResponse.getBytes("MS950");
        writeBinFile(tmpBytesResponse, tmpBytesResponse.length);
        closeBinaryOutput();
        ftpProc(filenameResponse, "CREDITCARD", "CREDITCARD");
        showLogMessage("I", "", "============================================================");
        showLogMessage("I", "", "寫入漢來美食"+card_name+"卡電子餐券回饋檔結束!");

    }

    private String rptGift(String str) throws Exception {
        str = "分行代號: 3144 信用卡部" + commCrd.fixRight("漢來美食聯名卡促銷活動-滿額禮明細表", 61) + commCrd.fixRight("保存年限: 五年", 48) + lineSeparator;
        str += "報表代號: MKTW006GIFT      科目代號:" + commCrd.fixLeft(String.format("               中 華 民 國: %s 年 %2$s 月 %3$s 日", commDate.toTwDate(searchDate).substring(0, 3), searchDate.substring(4, 6), searchDate.substring(6, 8)), 79) + commCrd.fixRight("第 "+String.format("%04d", pageCnt1)+" 頁", 16) + lineSeparator;
        str += lineSeparator;
        str += "====================================================================================================================================" + lineSeparator;
        str += "持卡人ID         持卡人姓名         TYPE         信用卡卡號         消費累積次數         消費累積金額                         " + lineSeparator;
        str += "====================================================================================================================================" + lineSeparator;

        for (String s : str.split(lineSeparator)) {
            lparR1.add(comcr.putReport(rptIdR1, rptNameR1, sysDate + sysTime, ++rptSeqR1, "0", s));
        }
        lineCnt1 = 6;
        return str;
    }
    
    private String rptWorld(String str) throws Exception {
        str = "分行代號: 3144 信用卡部" + commCrd.fixRight("漢來美食世界卡促銷活動-滿額禮明細表", 61) + commCrd.fixRight("保存年限: 五年", 48) + lineSeparator;
        str += "報表代號: MKTW006WORLD      科目代號:" + commCrd.fixLeft(String.format("               中 華 民 國: %s 年 %2$s 月 %3$s 日", commDate.toTwDate(searchDate).substring(0, 3), searchDate.substring(4, 6), searchDate.substring(6, 8)), 79) + commCrd.fixRight("第 "+String.format("%04d", pageCnt1)+" 頁",16) + lineSeparator;
        str += lineSeparator;
        str += "====================================================================================================================================" + lineSeparator;
        str += "持卡人ID         持卡人姓名         TYPE         信用卡卡號         消費累積次數         消費累積金額                         " + lineSeparator;
        str += "====================================================================================================================================" + lineSeparator;

        for (String s : str.split(lineSeparator)) {
            lparR2.add(comcr.putReport(rptIdR2, rptNameR2, sysDate + sysTime, ++rptSeqR2, "0", s));
        }
        lineCnt1 = 6;
        return str;
    }
       
    // ************************************************************************
    private void ftpProc(String filename, String systemId, String refIpCode) throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = systemId; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = comc.getECSHOME() + FILE_FOLDER;    //相關目錄皆同步
        commFTP.hEflgModPgm = javaProgram;

        String hEflgRefIpCode = refIpCode; 

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        showLogMessage("I", "", "put %s " + filename + " 開始上傳....");

        String tmpChar = "put " + filename;

        int errCode = commFTP.ftplogName(hEflgRefIpCode, tmpChar);

        if (errCode != 0) {
            showLogMessage("I", "", "檔案傳送 " + hEflgRefIpCode + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "MktW006 執行完成 傳送EMP失敗[" + filename + "]");
            commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
            return;
        }

        showLogMessage("I", "", "FTP完成.....");

        // 刪除檔案 put 不用刪除
        backup(filename);
    }

    /****************************************************************************/
    private void backup(String removeFileName) {
        String tmpStr1 = comc.getECSHOME() + FILE_FOLDER + "/" + removeFileName;
        String tempPath1 = SecurityUtil.verifyPath(tmpStr1);
        String tmpStr2 = comc.getECSHOME() + FILE_FOLDER + "/backup/" + String.format(removeFileName + "_%s", sysDate + sysTime);
        String tempPath2 = SecurityUtil.verifyPath(tmpStr2);

        if (!comc.fileCopy(tempPath1, tempPath2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]備份失敗!");
            return;
        }
        comc.fileDelete(tmpStr1);
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 備份至 [" + tempPath2 + "]");
    }

    public static void main(String[] args) {
        MktW006 proc = new MktW006();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }

    void initialField() {		
		h_card_no           = "";	
		h_id_no             = "";
		h_purchase_cnt      = "";
		h_purchase_amt      = "";
	    h_group_code        = "";  
	    h_cellar_phone      = "";  
	    h_e_mail_addr       = ""; 
	    h_chi_name          = "";  
	    h_bill_sending_zip  = ""; 
	    h_bill_sending_addr = "";  
	}
    void getFileData() throws Exception {

        byte[] bytes = lineLength.getBytes("MS950");
        h_card_no = comc.subMS950String(bytes, 0, 16).trim();
        h_id_no = comc.subMS950String(bytes, 17, 10).trim();
        h_purchase_cnt = comc.subMS950String(bytes,28 , 3).trim();
        h_purchase_amt = comc.subMS950String(bytes,32, 7).trim();
        totCnt++;
//        System.out.println("["+infoCnt+"] , card_no["+h_card_no+"] , id_no["+h_id_no+"] , purchase_cnt["+h_purchase_cnt+"] , purchase_amt["+h_purchase_amt+"]");
      
	}
    
    void selectCrdCard() throws Exception {		
		
		sqlCmd  = "SELECT Substr(group_code,2, 3) AS group_code ";
		sqlCmd += "FROM crd_card ";
		sqlCmd += "WHERE card_no = ? ";
		setString(1, h_card_no);
	    selectTable();
	         
	    if (!notFound.equals("Y")) {
            h_group_code = getValue("group_code");
	    }
	     
	}
    
    void selectCrdIdno() throws Exception {		
				
		sqlCmd  = "SELECT cellar_phone,e_mail_addr,chi_name ";
		sqlCmd += "FROM crd_idno ";
		sqlCmd += "WHERE id_no = ? ";
		setString(1, h_id_no);
	    selectTable();
	    

	    if (!notFound.equals("Y")) {
	       h_cellar_phone = getValue("cellar_phone");
	       h_e_mail_addr = getValue("e_mail_addr");
	       h_chi_name = getValue("chi_name");	 
	    }
	     
	}
    void selectActAcno() throws Exception {		
		
		sqlCmd  = "SELECT a.BILL_SENDING_ZIP ,a.BILL_SENDING_ADDR1 || a.BILL_SENDING_ADDR2 || a.BILL_SENDING_ADDR3 || a.BILL_SENDING_ADDR4 || a.BILL_SENDING_ADDR5 AS bill_sending_addr ";
		sqlCmd += "FROM act_acno a,crd_card b ";
		sqlCmd += "WHERE a.p_seqno = b.p_seqno ";
		sqlCmd += "AND card_no = ? ";
		setString(1, h_card_no);
	    selectTable();
	    

	    if (!notFound.equals("Y")) {
	       h_bill_sending_zip = getValue("bill_sending_zip");
	       h_bill_sending_addr = getValue("bill_sending_addr");	   
	    }
	     
	}
    
    class ValueComparator implements Comparator<String> {
        Map<String, String> base;

        public ValueComparator(Map<String, String> base) {
            this.base = base;
          
        }

        public ValueComparator(HashMap<String, String> map) {
			// TODO Auto-generated constructor stub
		}

		// Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(String a, String b) {
        	return a.compareTo(b) ;
        }
    }
    
}
