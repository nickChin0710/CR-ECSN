/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/03/22  V1.00.01    JeffKung  program initial                           *
******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*國際信用卡清算明細表-待查帳款*/
public class BilRD09X extends AccessDAO {
    private String progname = "列印國際信用卡清算明細表-待查帳款程式  112/03/22  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD09X";
    String prgmName = "列印國際信用卡清算明細表-待查帳款程式";
    String rptName = "國際信用卡清算明細表-待查帳款";
    String rptId = "BILR_D09_RSK";
    int rptSeq = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    
    String rptNameD02TWD = "國際信用卡清算彙計表-信用卡TWD";
    String rptIdD02TWD = "CRD02_TWD";
    int rptSeqD02TWD = 0;
    List<Map<String, Object>> lparD02TWD = new ArrayList<Map<String, Object>>();
    
    String rptNameD02USD = "國際信用卡清算彙計表-信用卡USD";
    String rptIdD02USD = "CRD02_USD";
    int rptSeqD02USD = 0;
    List<Map<String, Object>> lparD02USD = new ArrayList<Map<String, Object>>();
    
    String rptNameD02JPY = "國際信用卡清算彙計表-信用卡JPY";
    String rptIdD02JPY = "CRD02_JPY";
    int rptSeqD02JPY = 0;
    List<Map<String, Object>> lparD02JPY = new ArrayList<Map<String, Object>>();
    
    String rptNameD02VD = "國際信用卡清算彙計表-VISA金融卡";
    String rptIdD02VD = "CRD02_VD";
    int rptSeqD02VD = 0;
    List<Map<String, Object>> lparD02VD = new ArrayList<Map<String, Object>>();
    
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hIdnoChiName = "";
    String hPrintName = "";
    String hRptName = "";
    String hBusinssDate = "";

    int totalCnt = 0;
    int indexCnt = 0;
    int pageCnt = 0;

    double totalVDDestAmtCR = 0;
    double totalVDDestAmtDR = 0;
    double totalTWDDestAmtCR = 0;
    double totalTWDDestAmtDR = 0;
    double totalUSDDestAmtCR = 0;
    double totalUSDDestAmtDR = 0;
    double totalJPYDestAmtCR = 0;
    double totalJPYDestAmtDR = 0;
    
    int totalVDCntCR = 0;
    int totalVDCntDR = 0;
    int totalTWDCntCR = 0;
    int totalTWDCntDR = 0;
    int totalUSDCntCR = 0;
    int totalUSDCntDR = 0;
    int totalJPYCntCR = 0;
    int totalJPYCntDR = 0;

    int lineCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            
            showLogMessage("I", "", "營業日期=[" + hBusinssDate + "]");
            
            if (args.length == 1 && args[0].length() == 8) {
            	hBusinssDate = args[0];
            }
            
            showLogMessage("I", "", "資料日期=[" + hBusinssDate + "]");

            selectBilCurpost();

            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "]"+ pageCnt);

            if (pageCnt > 0) {

                String ftpName = String.format("%s.%s_%s", rptId, sysDate, hBusinssDate);
                String filename = String.format("%s/reports/%s.%s_%s", comc.getECSHOME(), rptId, sysDate, hBusinssDate);
                //改為線上報表
                comc.writeReport(filename, lpar1);
                //comcr.insertPtrBatchRpt(lpar1);
                
                ftpMput(ftpName);
            }
            
            commitDataBase();
            
            //處理CRD02_TWD報表
            printBilRD02TWD();
            
            //處理CRD02_USD報表
            printBilRD02USD();
            
            //處理CRD02_JPY報表
            printBilRD02JPY();
            
            //處理CRD02_VD報表
            printBilRD02VD();

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
    void commonRtn() throws Exception {
        hBusinssDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectBilCurpost() throws Exception {

    	sqlCmd =  "select card_no,settl_flag,ecs_sign_code as sign_flag, ";
    	sqlCmd += " ecs_tx_code as txn_code,purchase_date,dest_curr, ";
    	sqlCmd += " decode(dest_curr,'901',round(dest_amt),'392', dest_amt,dest_amt) AS dest_amt, ";
    	sqlCmd += " decode(ecs_debit_flag,'N','CC','VD') as card_group ";
		sqlCmd += " from bil_fiscdtl ";
		sqlCmd += "where batch_date = ? ";
		sqlCmd += " and  batch_flag = 'Y' ";
		sqlCmd += " and  ecs_tx_code in ('65','66','67','85','86','87') ";   //再提示交易
		sqlCmd += " order by card_group,dest_curr desc,sign_flag,txn_code ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeader();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
                printHeader();
                indexCnt = 0;
            }

            printDetail();
        }

        if (indexCnt != 0)
            printFooter();
    }

    /***********************************************************************/
    void printHeader() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptId, 1);
        buf = comcr.insertStrCenter(buf, rptName, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        buf = comcr.insertStr(buf, "入帳日 :", 20);
        buf = comcr.insertStr(buf, hBusinssDate, 30);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "卡片種類", 1);
        buf = comcr.insertStr(buf, "清算幣別", 12);
        buf = comcr.insertStr(buf, "交易日期", 24);
        buf = comcr.insertStr(buf, "交易摘要", 36);
        buf = comcr.insertStr(buf, "卡號", 56);
        buf = comcr.insertStr(buf, "交易金額/本金", 76);
        buf = comcr.insertStr(buf, "入帳科子目", 96);
        buf = comcr.insertStr(buf, "備__註", 121);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    }

    /***********************************************************************/
    void printFooter() {
    	buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
    	buf = "";
    	lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	buf = "";
    	buf = comcr.insertStr(buf, "待查帳款二次", 1);
    	buf = comcr.insertStr(buf, "借", 40);
    	buf = comcr.insertStr(buf, "筆數", 65);
    	buf = comcr.insertStr(buf, "貸", 75);
    	buf = comcr.insertStr(buf, "筆數", 100);
    	lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
    	buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
    	buf = "";
        buf = comcr.insertStr(buf, "VD帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalVDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalVDCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalVDDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalVDCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
        buf = "";
        buf = comcr.insertStr(buf, "TWD帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalTWDCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalTWDCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "USD帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalUSDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalUSDCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalUSDDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalUSDCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "JPY帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalJPYDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalJPYCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalJPYDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalJPYCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

    }

    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        indexCnt++;

        buf = "";
        if ("VD".equals(getValue("card_group"))) {
        	buf = comcr.insertStr(buf, "VISA金融卡", 1);
        } else {
        	buf = comcr.insertStr(buf, "信用卡", 1);
        }
        
        if ("901".equals(getValue("dest_curr"))) {
        	buf = comcr.insertStr(buf, "TWD", 12);
        } else if ("840".equals(getValue("dest_curr"))) {
        	buf = comcr.insertStr(buf, "USD", 12);
        } else {
        	buf = comcr.insertStr(buf, "JPY", 12);
        }
        
        buf = comcr.insertStr(buf, getValue("purchase_date"), 24);
        
        String signFlag = "+";
        
        if ("65".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "65-二次購貨", 36);
        } else if ("66".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "66-二次退貨", 36);
        	signFlag = "-";
        } else if ("67".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "67-二次預現", 36);
        } else if ("85".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "85-二次購貨沖銷", 36);
        	signFlag = "-";
        } else if ("86".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "86-二次退貨沖銷", 36);
        } else if ("87".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "87-二次預現沖銷", 36);
        	signFlag = "-";
        }
        
        buf = comcr.insertStr(buf, getValue("card_no"), 56);
        
        szTmp = comcr.commFormat("3$,3$,3$.2$", getValueDouble("dest_amt"));
        buf = comcr.insertStr(buf, szTmp, 76);
        if ("6".equals(getValue("settl_flag"))) {
        	if ("-".equals(signFlag)) {
        		buf = comcr.insertStr(buf, "金資帳款__貸", 96);
        	} else {
        		buf = comcr.insertStr(buf, "金資帳款__借", 96);
        	}
        } else if ("8".equals(getValue("settl_flag"))) {
        	if ("-".equals(signFlag)) {
        		buf = comcr.insertStr(buf, "NCCC帳款__貸", 96);
        	} else {
        		buf = comcr.insertStr(buf, "NCCC帳款__借", 96);
        	}
        } else if ("0".equals(getValue("settl_flag"))) {
        	if ("-".equals(signFlag)) {
        		buf = comcr.insertStr(buf, "國外帳款__貸", 96);
        	} else {
        		buf = comcr.insertStr(buf, "國外帳款__借", 96);
        	}
        }
        
        if ("-".equals(signFlag)) {
        	if ("VD".equals(getValue("card_group"))) {
        		totalVDDestAmtCR = totalVDDestAmtCR + getValueDouble("dest_amt");
        		totalVDCntCR = totalVDCntCR + 1;
        	} else if ("901".equals(getValue("dest_curr"))) {
        		totalTWDDestAmtCR = totalTWDDestAmtCR + getValueDouble("dest_amt");
        		totalTWDCntCR = totalTWDCntCR + 1;
            } else if ("840".equals(getValue("dest_curr"))) {
            	totalUSDDestAmtCR = totalUSDDestAmtCR + getValueDouble("dest_amt");
        		totalUSDCntCR = totalUSDCntCR + 1;
            } else {
            	totalJPYDestAmtCR = totalJPYDestAmtCR + getValueDouble("dest_amt");
        		totalJPYCntCR = totalJPYCntCR + 1;
            }
        } else {
        	if ("VD".equals(getValue("card_group"))) {
        		totalVDDestAmtDR = totalVDDestAmtDR + getValueDouble("dest_amt");
        		totalVDCntDR = totalVDCntDR + 1;
        	} else if ("901".equals(getValue("dest_curr"))) {
        		totalTWDDestAmtDR = totalTWDDestAmtDR + getValueDouble("dest_amt");
        		totalTWDCntDR = totalTWDCntDR + 1;
            } else if ("840".equals(getValue("dest_curr"))) {
            	totalUSDDestAmtDR = totalUSDDestAmtDR + getValueDouble("dest_amt");
        		totalUSDCntCR = totalUSDCntCR + 1;
            } else {
            	totalJPYDestAmtDR = totalJPYDestAmtDR + getValueDouble("dest_amt");
        		totalJPYCntDR = totalJPYCntDR + 1;
            }
        }
        
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

    }
    
    /***********************************************************************/
    void printBilRD02TWD() throws Exception {
    	
    	//先將保留的資料取出
    	double totalRecvBalCR = selectKeepRptRecord(rptIdD02TWD,991); 
    	double totalRecvBalDR = selectKeepRptRecord(rptIdD02TWD,992);
    	
        buf = "";
        buf = comcr.insertStr(buf, "其他應收款—國際卡待查帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "二次提示", 110);
        lparD02TWD.add(comcr.putReport(rptIdD02TWD, rptNameD02TWD, hBusinssDate, 21, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應付款—國際卡待查帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalTWDDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "二次提示", 110);
        lparD02TWD.add(comcr.putReport(rptIdD02TWD, rptNameD02TWD, hBusinssDate, 22, "0", buf));
        
        //應收帳款-信用卡墊款(先暫放在table中,後續的程式再拿出來計算)  
        totalRecvBalDR = totalRecvBalDR - totalTWDDestAmtDR;
        totalRecvBalCR = totalRecvBalCR - totalTWDDestAmtCR;
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalRecvBalDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalRecvBalCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02TWD.add(comcr.putReport(rptIdD02TWD, rptNameD02TWD, hBusinssDate, 31, "0", buf));
        
        insertPtrBatchRpt(lparD02TWD);
        
    	deleteExistRptRecord(rptIdD02TWD);
    	commitDataBase();
    }
    
    /***********************************************************************/
    void printBilRD02USD() throws Exception {
    	
    	//先將保留的資料取出
    	double totalRecvBalCR = selectKeepRptRecord(rptIdD02USD,991); 
    	double totalRecvBalDR = selectKeepRptRecord(rptIdD02USD,992);
    	
        buf = "";
        buf = comcr.insertStr(buf, "其他應收款—國際卡待查帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalUSDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "二次提示", 110);
        lparD02USD.add(comcr.putReport(rptIdD02USD, rptNameD02USD, hBusinssDate, 21, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應付款—國際卡待查帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalUSDDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "二次提示", 110);
        lparD02USD.add(comcr.putReport(rptIdD02USD, rptNameD02USD, hBusinssDate, 22, "0", buf));
        
        //應收帳款-信用卡墊款(先暫放在table中,後續的程式再拿出來計算)  
        totalRecvBalDR = totalRecvBalDR - totalUSDDestAmtDR;
        totalRecvBalCR = totalRecvBalCR - totalUSDDestAmtCR;
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalRecvBalDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalRecvBalCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02USD.add(comcr.putReport(rptIdD02USD, rptNameD02USD, hBusinssDate, 31, "0", buf));
        
        insertPtrBatchRpt(lparD02USD);
        
    	deleteExistRptRecord(rptIdD02USD);
    	commitDataBase();
    }
    
    /***********************************************************************/
    void printBilRD02JPY() throws Exception {
    	
    	//先將保留的資料取出
    	double totalRecvBalCR = selectKeepRptRecord(rptIdD02JPY,991); 
    	double totalRecvBalDR = selectKeepRptRecord(rptIdD02JPY,992);
    	
        buf = "";
        buf = comcr.insertStr(buf, "其他應收款—國際卡待查帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalJPYDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "二次提示", 110);
        lparD02JPY.add(comcr.putReport(rptIdD02JPY, rptNameD02JPY, hBusinssDate, 21, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應付款—國際卡待查帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalJPYDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "二次提示", 110);
        lparD02JPY.add(comcr.putReport(rptIdD02JPY, rptNameD02JPY, hBusinssDate, 22, "0", buf));
        
        //應收帳款-信用卡墊款(先暫放在table中,後續的程式再拿出來計算)  
        totalRecvBalDR = totalRecvBalDR - totalJPYDestAmtDR;
        totalRecvBalCR = totalRecvBalCR - totalJPYDestAmtCR;
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalRecvBalDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalRecvBalCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02JPY.add(comcr.putReport(rptIdD02JPY, rptNameD02JPY, hBusinssDate, 31, "0", buf));
        
        insertPtrBatchRpt(lparD02JPY);
        
    	deleteExistRptRecord(rptIdD02JPY);
    	commitDataBase();
    }
    
    /***********************************************************************/
    void printBilRD02VD() throws Exception {
    	
    	//先將保留的資料取出
    	double totalRecvBalCR = selectKeepRptRecord(rptIdD02VD,991); 
    	double totalRecvBalDR = selectKeepRptRecord(rptIdD02VD,992);
    	
        buf = "";
        buf = comcr.insertStr(buf, "其他應收款—國際卡待查帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalVDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "二次提示", 110);
        lparD02VD.add(comcr.putReport(rptIdD02VD, rptNameD02VD, hBusinssDate, 21, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應付款—國際卡待查帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalVDDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "二次提示", 110);
        lparD02VD.add(comcr.putReport(rptIdD02VD, rptNameD02VD, hBusinssDate, 22, "0", buf));
        
        //應收帳款-信用卡墊款(先暫放在table中,後續的程式再拿出來計算)  
        totalRecvBalDR = totalRecvBalDR - totalVDDestAmtDR;
        totalRecvBalCR = totalRecvBalCR - totalVDDestAmtCR;
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalRecvBalDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalRecvBalCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02VD.add(comcr.putReport(rptIdD02VD, rptNameD02VD, hBusinssDate, 31, "0", buf));
        
        insertPtrBatchRpt(lparD02VD);
        
    	deleteExistRptRecord(rptIdD02VD);
    	commitDataBase();
    }
    
    double selectKeepRptRecord(String rptIdD02,int seq) throws Exception {
    	
    	double totalRecvBal = 0;
    	sqlCmd =  "select report_content ";
		sqlCmd += " from ptr_batch_rpt ";
		sqlCmd += "where program_code = ? ";
		sqlCmd += " and  start_date = ? ";
		sqlCmd += " and  seq = ? ";   
		
		setString(1, rptIdD02);
		setString(2, hBusinssDate);
		setInt(3, seq);
	
		int readCnt = selectTable();
		if (readCnt > 0) {
			totalRecvBal = comc.str2double(getValue("report_content").trim());
		}
		
		return totalRecvBal;
    	
    }
    
    void deleteExistRptRecord(String rptIdD02) throws Exception {
    	
    	daoTable  = " ptr_batch_rpt ";
		whereStr  = " where 1=1 "; 
		whereStr += " and program_code = ? ";
		whereStr += " and start_date = ? ";
		whereStr += " and seq >= 990 ";
		
		setString(1, rptIdD02);
		setString(2, hBusinssDate);
	
		deleteTable();
    	
    }
    
    int insertPtrBatchRpt(List<Map<String, Object>> lpar) throws Exception {
        int actCnt = 0;
        noTrim = "Y";
        String tmpStr = hBusinssDate + "000002";
        for (int i = 0; i < lpar.size(); i++) {
            if (tmpStr.length() > 8) {
                setValue("start_date", tmpStr.substring(0, 8));
                setValue("start_time", tmpStr.substring(8));
            } else {
                setValue("start_date", tmpStr.substring(0));
                setValue("start_time", "");
            }
            setValue("program_code", lpar.get(i).get("prgmId").toString());
            setValue("rptname", lpar.get(i).get("prgmName").toString());
            setValue("seq", lpar.get(i).get("seq").toString());
            setValue("kind", lpar.get(i).get("kind").toString());
            setValue("report_content", lpar.get(i).get("content").toString());

            daoTable = "ptr_batch_rpt";
            insertTable();
            if (dupRecord.equals("Y")) {
                return 0;
            }
        }
        noTrim = "";
        return actCnt;
    }
    
    /***********************************************************************/
    int ftpMput(String filename) throws Exception {
        String procCode = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "CREDITCARD"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/reports/", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "CREDITCARD";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            comcr.errRtn(String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode), "", hCallBatchSeqno);
        }
        return (0);
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRD09X proc = new BilRD09X();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
