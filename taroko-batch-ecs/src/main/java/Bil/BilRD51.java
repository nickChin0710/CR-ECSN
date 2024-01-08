/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/05/09 V1.01.01  JeffKung    program initial                            *
*  112/11/08 V1.01.02  JeffKung    condition changed                          *
******************************************************************************/
package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*列印全部信用卡交易量彙總表*/
public class BilRD51 extends AccessDAO {
    private String progname = "全部信用卡交易量彙總表  112/11/08 V1.01.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId   = "BilRD51";
    String prgmName = "全部信用卡交易量彙總表";
    
    int DEBUG = 0;

    String rptNameD51 = "全部信用卡交易量彙總表";
    String rptIdD51   = "CRD51";
    int rptSeqD51     = 0;
    List<Map<String, Object>> lparD51 = new ArrayList<Map<String, Object>>();
    
    String buf    = "";
    String tmp    = "";
    String szTmp  = "";
    String hCallBatchSeqno = "";

    String hIdnoChiName = "";
    String hPrintName   = "";
    String hRptName     = "";
    String hBusinssDate = "";
    String hBegDate     = "";
    String hEndDate     = "";
    String hChiDate     = "";

    int totalCnt = 0;
    int indexCnt = 0;
    int pageCnt  = 0;

    String cardGroupCode   = "";
    String cardCardType    = "";
    String cardBinNo       = "";
    String cardName        = "";
    String hAcctCode       = "";
    long   hPurchaseC1     = 0;
    double hPurchaseA1     = 0;
    long   hPurchaseC2     = 0;
    double hPurchaseA2     = 0;
    long   totPurchaseC1   = 0;
    double totPurchaseA1   = 0;
    long   totPurchaseC2   = 0;
    double totPurchaseA2   = 0;

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

            if (args.length  > 0 && args[0].length() == 8) {
            	hBusinssDate = args[0];
            }
            showLogMessage("I", "", "參數日期=[" + hBusinssDate + "]");

            commonRtn();
           
            showLogMessage("I", "", "資料日期=[" + hBusinssDate + "]");
            
            loadBilCurpost(comc.getSubString(hBusinssDate,0,6));

            initCnt();
            selectPtrGroupCard();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            if (pageCnt > 0) {
                String ftpName = String.format("%s.%s", rptIdD51, sysDate);
if(DEBUG == 1) 
  {
                String filename = String.format("%s/reports/%s.%s",comc.getECSHOME(),rptIdD51,sysDate);
                comc.writeReport(filename, lparD51);
  }
                comcr.insertPtrBatchRpt(lparD51);
                //ftpMput(ftpName);
            }

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
    
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            if(hBusinssDate.length() < 1)
               hBusinssDate = getValue("business_date");
        }

        sqlCmd  = "select to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01'       h_beg_date ";
        sqlCmd += "     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
        sqlCmd += "     , trim(to_char(to_number(to_char(sysdate,'yyyymmdd')-19110000) ,'0000000')) h_chi_date ";
        sqlCmd += " from dual ";
        setString(1, hBusinssDate);
        setString(2, hBusinssDate);
        setString(2, hBusinssDate);
        recordCnt = selectTable();
        if(recordCnt > 0) {
           hBegDate = getValue("h_beg_date");
           hEndDate = getValue("h_end_date");
           hChiDate = getValue("h_chi_date");
        }
        
        //資料區間改成截至當日營業日(20230817)
        hEndDate = hBusinssDate;
    }

    /***********************************************************************/
    void selectPtrGroupCard() throws Exception {

    	sqlCmd =  "SELECT a.group_code, a.card_type, a.NAME, ";
    	sqlCmd += " (SELECT bin_no FROM crd_cardno_range b ";
    	sqlCmd += "   WHERE a.GROUP_CODE = b.GROUP_CODE ";
    	sqlCmd += "     AND a.CARD_TYPE  = b.CARD_TYPE ";
    	sqlCmd += "   FETCH FIRST 1 ROWS only ) AS bin_no ";
    	sqlCmd += " from PTR_GROUP_CARD a ";
    	sqlCmd += "WHERE a.card_type NOT IN ('VD') ";
    	sqlCmd += "  AND substr(a.card_type,1,1) IN ('V','M','J') ";
    	sqlCmd += "order by a.group_code ";

	int cursorIndex = openCursor();
	while (fetchTable(cursorIndex)) {

            totalCnt++;

            cardGroupCode     = getValue("group_code"  );
            cardCardType      = getValue("card_type"   );
            cardBinNo         = getValue("bin_no"      );
            cardName          = getValue("name"        );

            if (indexCnt == 0) {
                printHeaderD51();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", "##PPP"));
                printHeaderD51();
                indexCnt = 0;
            }
            
            hPurchaseC1 = 0; hPurchaseA1 = 0; hPurchaseC2 = 0; hPurchaseA2 = 0;
            
            setValue("bill.group_code", cardGroupCode);
            int loadBilCnt = getLoadData("bill.group_code");
if(DEBUG==1) showLogMessage("I","","Read Group=" + cardGroupCode + ","+cardCardType+","+cardBinNo+","+loadBilCnt);
                
            for ( int i=0; i<loadBilCnt; i++ ) {
                  hAcctCode    = getValue("bill.acct_code",i);
if(DEBUG==1) showLogMessage("I","","  get bill=" + hAcctCode +","+ getValueLong("bill.purchase_cnt",i));
                  if(hAcctCode.equals("CA")) 
                    {
                     hPurchaseC2  = hPurchaseC2 + getValueLong("bill.purchase_cnt",i);
                     hPurchaseA2  = hPurchaseA2 + getValueDouble("bill.purchase_amt",i);
                    }
                  else
                    {
                     hPurchaseC1  = hPurchaseC1 + getValueLong("bill.purchase_cnt",i);
                     hPurchaseA1  = hPurchaseA1 + getValueDouble("bill.purchase_amt",i);
                    }
              }

            printDetailD51();
        }

        if (indexCnt != 0)
            printFooterD51();
    }

    /***********************************************************************/
    void printHeaderD51() {

        pageCnt++;

        buf = "";
        buf = comcr.insertStr(buf, "分行代號:"      ,   1);
        buf = comcr.insertStr(buf, "3144 信用卡部"  ,  11);
        buf = comcr.insertStrCenter(buf, rptNameD51 , 132);
        buf = comcr.insertStr(buf, "保存年限:"      , 111);
        buf = comcr.insertStr(buf, "五年"           , 121);
        lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表代號:"      ,   1);
        buf = comcr.insertStr(buf, rptIdD51         ,  11);
        tmp = String.format("%s年%2.2s月%2.2s日", hChiDate.substring(0,3)
                            ,hChiDate.substring(3, 5),hChiDate.substring(5));
        buf = comcr.insertStrCenter(buf, "中華民國"+tmp , 132);
        buf = comcr.insertStr(buf, "頁    次:"      , 111);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp            , 121);
        lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "累計起訖日:"    ,   1);
        buf = comcr.insertStr(buf, hBegDate         ,  13);
        buf = comcr.insertStr(buf, " - "            ,  22);
        buf = comcr.insertStr(buf, hEndDate         ,  25);
        lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));
        
    	buf = "";
        lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "卡別"           ,   1);
        buf = comcr.insertStr(buf, "BIN_NO"         ,   6);
        buf = comcr.insertStr(buf, "簽 帳 筆 數"    ,  19);
        buf = comcr.insertStr(buf, "簽  帳  金  額" ,  31);
        buf = comcr.insertStr(buf, "    "           ,  46);
        buf = comcr.insertStr(buf, "預 現 筆 數"    ,  50);
        buf = comcr.insertStr(buf, "預  現  金  額" ,  62);
        buf = comcr.insertStr(buf, "卡片名稱      " ,  77);
        lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "===="           ,   1);
        buf = comcr.insertStr(buf, "============"   ,   6);
        buf = comcr.insertStr(buf, "==========="    ,  19);
        buf = comcr.insertStr(buf, "==============" ,  31);
        buf = comcr.insertStr(buf, "    "           ,  46);
        buf = comcr.insertStr(buf, "==========="    ,  50);
        buf = comcr.insertStr(buf, "==============" ,  62);
        buf = comcr.insertStr(buf, "================================================== " ,  77);
        lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));
    }

    /***********************************************************************/
    void printFooterD51() {

    	buf = "";
        lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));
        
    	buf = "";
    	lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));
    	
        buf = "";
        for (int i = 0; i < 126; i++)
            buf += "-";
        lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));

    	buf = "";
        buf = comcr.insertStr(buf, "合  計 :", 10);
        tmp = comcr.commFormat("2z,3z,3#"    , totPurchaseC1);
        buf = comcr.insertStr(buf, tmp       , 19);
        tmp = comcr.commFormat("$z,3z,3z,3z", totPurchaseA1);
        buf = comcr.insertStr(buf, tmp       , 32);
        tmp = comcr.commFormat("2z,3z,3#"    , totPurchaseC2);
        buf = comcr.insertStr(buf, tmp       , 50);
        tmp = comcr.commFormat("$z,3z,3z,3z", totPurchaseA2);
        buf = comcr.insertStr(buf, tmp       , 63);
    	lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));
    }
    /***********************************************************************/
    void printDetailD51() throws Exception {
        lineCnt++;
        indexCnt++;
        //card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code
        buf = "";
        buf = comcr.insertStr(buf, cardGroupCode, 1);
        
        buf = comcr.insertStr(buf, cardBinNo      ,  6);
        szTmp = comcr.commFormat("2z,3z,3#", hPurchaseC1);
        buf = comcr.insertStr(buf, szTmp          , 19);
        szTmp = comcr.commFormat("z,3z,3z,3#", hPurchaseA1);
        buf = comcr.insertStr(buf, szTmp          , 32);
        szTmp = comcr.commFormat("2z,3z,3#", hPurchaseC2);
        buf = comcr.insertStr(buf, szTmp          , 50);
        szTmp = comcr.commFormat("z,3z,3z,3#", hPurchaseA2);
        buf = comcr.insertStr(buf, szTmp          , 63);
        buf = comcr.insertStr(buf, cardName       , 77);
        lparD51.add(comcr.putReport(rptIdD51, rptNameD51, sysDate, ++rptSeqD51, "0", buf));

        totPurchaseC1 = totPurchaseC1 + hPurchaseC1;
        totPurchaseA1 = totPurchaseA1 + hPurchaseA1;
        totPurchaseC2 = totPurchaseC2 + hPurchaseC2;
        totPurchaseA2 = totPurchaseA2 + hPurchaseA2;
    }
    
    void initCnt() {
    	totalCnt = 0;
        indexCnt = 0;
        pageCnt = 0;
        lineCnt = 0;
    }
    
 // ************************************************************************
    void  loadBilCurpost(String rptMonth) throws Exception
    {
    	
      //雙幣卡台外幣都算抓台幣金額	
      extendField = "bill.";
      daoTable    = "bil_curpost";
      selectSQL   = "group_code,acct_code,sum(decode(sign_flag,'+',1,-1)) AS purchase_cnt,";
      selectSQL  += "sum(decode(sign_flag,'+',dest_amt,dest_amt*-1))      AS purchase_amt";
      whereStr    = "where 1=1 ";
      whereStr   += "  AND this_close_date LIKE ? ";
      whereStr   += "  AND this_close_date <= ? ";
      whereStr   += "  AND acct_code IN ('BL','CA') ";
      whereStr   += "  AND group_code <> '' ";
      whereStr   += "GROUP BY group_code,acct_code ";

      setString(1 , (rptMonth + "%"));
      setString(2 , hEndDate);

      int  n = loadTable();
      setLoadData("bill.group_code"); //set key

      showLogMessage("I","","Load bil_curpost end Count: ["+n+"]" + rptMonth);
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRD51 proc = new BilRD51();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
