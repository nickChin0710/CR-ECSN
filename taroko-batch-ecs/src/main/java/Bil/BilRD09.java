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

/*國際信用卡清算明細表-信用卡*/
public class BilRD09 extends AccessDAO {
    private String progname = "列印國際信用卡清算明細表-信用卡TWD程式  112/03/22  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD09";
    String prgmName = "列印國際信用卡清算明細表-信用卡TWD程式";
    String rptName = "國際信用卡清算明細表-信用卡TWD";
    String rptId = "BILR_D09_TWD";
    int rptSeq = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    
    String rptNameD02 = "國際信用卡清算彙計表-信用卡TWD";
    String rptIdD02 = "CRD02_TWD";
    int rptSeqD02 = 0;
    List<Map<String, Object>> lparD02 = new ArrayList<Map<String, Object>>();
    
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

    double totalFISCDestAmtCR = 0;
    double totalFISCDestAmtDR = 0;
    double totalNCCCDestAmtCR = 0;
    double totalNCCCDestAmtDR = 0;
    double totalINTLDestAmtCR = 0;
    double totalINTLDestAmtDR = 0;
    double totalOnusSBDestAmtCR = 0;
    double totalOnusSBDestAmtDR = 0;
    double totalOnusATMDestAmtCR = 0;
    double totalOnusATMDestAmtDR = 0;
    double totalOnusCashDestAmtCR = 0;
    double totalOnusCashDestAmtDR = 0;
    
    int totalFISCCntCR = 0;
    int totalFISCCntDR = 0;
    int totalNCCCCntCR = 0;
    int totalNCCCCntDR = 0;
    int totalINTLCntCR = 0;
    int totalINTLCntDR = 0;
    int totalOnusSBCntCR = 0;
    int totalOnusSBCntDR = 0;
    int totalOnusATMCntCR = 0;
    int totalOnusATMCntDR = 0;
    int totalOnusCashCntCR = 0;
    int totalOnusCashCntDR = 0;

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

    	sqlCmd =  "select card_no,settl_flag,dest_amt,purchase_date,txn_code,sign_flag,mcht_category ";
		sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and curr_code = '901' ";
		sqlCmd += " and settl_flag in ('0','6','8','9') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type <> 'I' ";
		sqlCmd += " order by sign_flag,txn_code,settl_flag ";
		
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
        buf = comcr.insertStrCenter(buf, "國際信用卡清算明細表-信用卡TWD", 132);
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
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 15);
        buf = comcr.insertStr(buf, "卡號", 35);
        buf = comcr.insertStr(buf, "交易金額/本金", 55);
        buf = comcr.insertStr(buf, "入帳科子目", 75);
        buf = comcr.insertStr(buf, "備__註", 100);
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
        buf = comcr.insertStr(buf, "金資____帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalFISCDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalFISCCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalFISCDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalFISCCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "ＮＣＣＣ帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalNCCCCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalNCCCCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "國外＿＿帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalINTLDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalINTLCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalINTLDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalINTLCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "自行(SB)帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusSBDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalOnusSBCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusSBDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalOnusSBCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "自行(ATM預借)帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusATMDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalOnusATMCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusATMDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalOnusATMCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "自行(櫃檯預借)帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusCashDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalOnusCashCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusCashDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalOnusCashCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, getValue("purchase_date"), 1);
        
        if ("05".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "05-購貨", 15);
        } else if ("06".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "06-退貨", 15);
        } else if ("07".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "07-預現", 15);
        } else if ("25".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "25-購貨沖銷", 15);
        } else if ("26".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "26-退貨沖銷", 15);
        } else if ("27".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "27-預現沖銷", 15);
        }
        
        buf = comcr.insertStr(buf, getValue("card_no"), 35);
        
        szTmp = comcr.commFormat("3$,3$,3$.2$", getValueDouble("dest_amt"));
        buf = comcr.insertStr(buf, szTmp, 55);
        if ("6".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		buf = comcr.insertStr(buf, "金資帳款__借", 75);
        		totalFISCDestAmtDR = totalFISCDestAmtDR + getValueDouble("dest_amt");
        		totalFISCCntDR = totalFISCCntDR + 1; 
        	} else {
        		buf = comcr.insertStr(buf, "金資帳款__貸", 75);
        		totalFISCDestAmtCR = totalFISCDestAmtCR + getValueDouble("dest_amt");
        		totalFISCCntCR = totalFISCCntCR + 1;
        	}
        } else if ("8".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		buf = comcr.insertStr(buf, "NCCC帳款__借", 75);
        		totalNCCCDestAmtDR = totalNCCCDestAmtDR + getValueDouble("dest_amt");
        		totalNCCCCntDR = totalNCCCCntDR + 1;
        	} else {
        		buf = comcr.insertStr(buf, "NCCC帳款__貸", 75);
        		totalNCCCDestAmtCR = totalNCCCDestAmtCR + getValueDouble("dest_amt");
        		totalNCCCCntCR = totalNCCCCntCR + 1;
        	}
        } else if ("0".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		buf = comcr.insertStr(buf, "國外帳款__借", 75);
        		totalINTLDestAmtDR = totalINTLDestAmtDR + getValueDouble("dest_amt");
        		totalINTLCntDR = totalINTLCntDR + 1;
        	} else {
        		buf = comcr.insertStr(buf, "國外帳款__貸", 75);
        		totalINTLDestAmtCR = totalINTLDestAmtCR + getValueDouble("dest_amt");
        		totalINTLCntCR = totalINTLCntCR + 1;
        	}
        } else if ("9".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		if ("27".equals(getValue("txn_code")) && "6011".equals(getValue("mcht_category"))) {
        			buf = comcr.insertStr(buf, "自行ATM預借__貸", 75);
            		totalOnusATMDestAmtCR = totalOnusATMDestAmtCR + getValueDouble("dest_amt");
            		totalOnusATMCntCR = totalOnusATMCntCR + 1;
        		} else if ("27".equals(getValue("txn_code")) ) {
        			buf = comcr.insertStr(buf, "自行櫃檯預借__貸", 75);
            		totalOnusCashDestAmtCR = totalOnusCashDestAmtCR + getValueDouble("dest_amt");
            		totalOnusCashCntCR = totalOnusCashCntCR + 1;
        		} else {
        			buf = comcr.insertStr(buf, "自行ＳＢ__貸", 75);
        			totalOnusSBDestAmtCR = totalOnusSBDestAmtCR + getValueDouble("dest_amt");
        			totalOnusSBCntCR = totalOnusSBCntCR + 1;
        		}
        	} else {
        		if ("07".equals(getValue("txn_code")) && "6011".equals(getValue("mcht_category"))) {
        			buf = comcr.insertStr(buf, "自行ATM預借__借", 75);
            		totalOnusATMDestAmtDR = totalOnusATMDestAmtDR + getValueDouble("dest_amt");
            		totalOnusATMCntDR = totalOnusATMCntDR + 1;
        		} else if ("07".equals(getValue("txn_code")) ) {
        			buf = comcr.insertStr(buf, "自行櫃檯預借__借", 75);
            		totalOnusCashDestAmtDR = totalOnusCashDestAmtDR + getValueDouble("dest_amt");
            		totalOnusCashCntDR = totalOnusCashCntDR + 1;
        		} else {
        			buf = comcr.insertStr(buf, "自行ＳＢ__借", 75);
        			totalOnusSBDestAmtDR = totalOnusSBDestAmtDR + getValueDouble("dest_amt");
        			totalOnusSBCntDR = totalOnusSBCntDR + 1;
        		}
        	}
        }
        
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

    }
    
    /***********************************************************************/
    void printBilRD02TWD() throws Exception {
    	
    	//重跑時要先刪除上一次產生的資料
    	deleteExistRptRecord();
    	commitDataBase();
    	
    	double totalRecvBalCR = 0; 
    	double totalRecvBalDR = 0;
    	
        buf = "";
        buf = comcr.insertStr(buf, rptIdD02, 1);
        buf = comcr.insertStrCenter(buf, rptNameD02, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", 1);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        buf = comcr.insertStr(buf, "入帳日 :", 20);
        buf = comcr.insertStr(buf, hBusinssDate, 30);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "入__帳__科__子__目", 1);
        buf = comcr.insertStr(buf, "____借__方__金__額", 50);
        buf = comcr.insertStr(buf, "____貸__方__金__額", 80);
        buf = comcr.insertStr(buf, "備__註", 110);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應收款—待收金資帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalFISCDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應付款—待付金資帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalFISCDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應收款—待收ＮＣＣＣ帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應付款—待付ＮＣＣＣ帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應收款—待收國外帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalINTLDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "其他應付款—待付國外帳款", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", 0);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalINTLDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        //onus交易從序號48開始
        rptSeqD02=47;
        
        buf = "";
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "＊以下為ＯＮ＿ＵＳ交易＊", 1);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款（ＡＴＭ預借現金）", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusATMDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusATMDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款（櫃台預借現金）", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusCashDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusCashDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款（ＳＢ）", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusSBDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusSBDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, ++rptSeqD02, "0", buf));
        
        //應收帳款-信用卡墊款(先暫放在table中,後續的程式再拿出來計算)  
        totalRecvBalCR = totalFISCDestAmtDR + totalNCCCDestAmtDR + totalINTLDestAmtDR;
        buf = "";
        szTmp = String.format("%014.2f", totalRecvBalCR);
        buf = comcr.insertStr(buf, szTmp, 1);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, 991, "0", buf));
        
        totalRecvBalDR = totalFISCDestAmtCR + totalNCCCDestAmtCR + totalINTLDestAmtCR;
        buf = "";
        szTmp = String.format("%014.2f", totalRecvBalDR);
        buf = comcr.insertStr(buf, szTmp, 1);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, 992, "0", buf));
        
        insertPtrBatchRpt(lparD02);
    }
    
    void deleteExistRptRecord() throws Exception {
    	
    	daoTable  = " ptr_batch_rpt ";
		whereStr  = " where 1=1 "; 
		whereStr += " and program_code = ? ";
		whereStr += " and start_date = ? ";
		whereStr += " and seq < 54 ";
		
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
        BilRD09 proc = new BilRD09();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
