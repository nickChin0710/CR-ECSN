/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/08/02 V1.01.01  JeffKung    program initial                            *
*                                                                             *
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

/*國際卡ＱＲ　Ｃｏｄｅ下載交易情形表*/
public class BilRD52F extends AccessDAO {
    private String progname = "國際卡ＱＲ　Ｃｏｄｅ下載交易情形表  112/08/02 V1.01.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId   = "BilRD52F";
    String prgmName = "國際卡ＱＲ　Ｃｏｄｅ下載交易情形表";
    
    int DEBUG = 0;

    String rptNameD52F = "國際卡ＱＲ　Ｃｏｄｅ下載交易情形表";
    String rptIdD52F   = "CRD52F";
    int rptSeqD52F     = 0;
    List<Map<String, Object>> lparD52F = new ArrayList<Map<String, Object>>();
    
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
    long   hVCardCnt       = 0;
    long   hPurchaseC1     = 0;
    double hPurchaseA1     = 0;
    long   hPurchaseC2     = 0;
    double hPurchaseA2     = 0;
    long   totPurchaseC1   = 0;
    double totPurchaseA1   = 0;
    long   totPurchaseC2   = 0;
    double totPurchaseA2   = 0;
    long   totVCardCnt     = 0;

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
                String ftpName = String.format("%s.%s", rptIdD52F, sysDate);
if(DEBUG == 1) 
  {
                String filename = String.format("%s/reports/%s.%s",comc.getECSHOME(),rptIdD52F,sysDate);
                comc.writeReport(filename, lparD52F);
  }
                comcr.insertPtrBatchRpt(lparD52F);
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

    printHeaderD52F();
    
	while (fetchTable(cursorIndex)) {

            totalCnt++;

            cardGroupCode     = getValue("group_code"  );
            cardCardType      = getValue("card_type"   );
            cardBinNo         = getValue("bin_no"      );
            cardName          = getValue("name"        );

            if (indexCnt > 25) {
            	//分頁控制
                lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", "##PPP"));
                printHeaderD52F();
                indexCnt = 0;
            }
            
            hPurchaseC1 = 0; hPurchaseA1 = 0; hPurchaseC2 = 0; hPurchaseA2 = 0;
            hVCardCnt = 0;
            String keepVCardNo = "";
            
            setValue("bill.group_code", cardGroupCode);
            int loadBilCnt = getLoadData("bill.group_code");
                
            for ( int i=0; i<loadBilCnt; i++ ) {
            	  if (keepVCardNo.equals(getValue("bill.v_card_no",i))==false) {
            		  keepVCardNo = getValue("bill.v_card_no",i);
            		  hVCardCnt ++;
            	  }
            	  
                  hPurchaseC1  = hPurchaseC1 + getValueLong("bill.purchase_cnt",i);
                  hPurchaseA1  = hPurchaseA1 + getValueDouble("bill.purchase_amt",i);
            }

            if ((hPurchaseC1+hPurchaseC1)>0 ) {
                printDetailD52F();
            }

        }

        printFooterD52F();
    }

    /***********************************************************************/
    void printHeaderD52F() {

        pageCnt++;

        buf = "";
        buf = comcr.insertStr(buf, "分行代號:"      ,   1);
        buf = comcr.insertStr(buf, "3144 信用卡部"  ,  11);
        buf = comcr.insertStrCenter(buf, rptNameD52F , 132);
        buf = comcr.insertStr(buf, "保存年限:"      , 111);
        buf = comcr.insertStr(buf, "五年"           , 121);
        lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表代號:"      ,   1);
        buf = comcr.insertStr(buf, rptIdD52F         ,  11);
        tmp = String.format("%s年%2.2s月%2.2s日", hChiDate.substring(0,3)
                            ,hChiDate.substring(3, 5),hChiDate.substring(5));
        buf = comcr.insertStrCenter(buf, "中華民國"+tmp , 132);
        buf = comcr.insertStr(buf, "頁    次:"      , 111);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp            , 121);
        lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "累計起訖日:"    ,   1);
        buf = comcr.insertStr(buf, hBegDate         ,  13);
        buf = comcr.insertStr(buf, " - "            ,  22);
        buf = comcr.insertStr(buf, hEndDate         ,  25);
        lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));
        
    	buf = "";
        lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "卡別"           ,   1);
        buf = comcr.insertStr(buf, "BIN_NO"         ,   6);
        buf = comcr.insertStr(buf, "簽 帳 筆 數"    ,  19);
        buf = comcr.insertStr(buf, "簽  帳  金  額" ,  31);
        buf = comcr.insertStr(buf, "    "           ,  46);
        buf = comcr.insertStr(buf, "當月簽帳卡數"    ,  50);
        buf = comcr.insertStr(buf, "卡片名稱      " ,  77);
        lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "===="           ,   1);
        buf = comcr.insertStr(buf, "============"   ,   6);
        buf = comcr.insertStr(buf, "==========="    ,  19);
        buf = comcr.insertStr(buf, "==============" ,  31);
        buf = comcr.insertStr(buf, "    "           ,  46);
        buf = comcr.insertStr(buf, "============"   ,  50);
        buf = comcr.insertStr(buf, "================================================== " ,  77);
        lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));
    }

    /***********************************************************************/
    void printFooterD52F() {

    	buf = "";
        lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));
        
    	buf = "";
    	lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));
    	
        buf = "";
        for (int i = 0; i < 126; i++)
            buf += "-";
        lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));

    	buf = "";
        buf = comcr.insertStr(buf, "合  計 :", 10);
        tmp = comcr.commFormat("2z,3z,3#"    , totPurchaseC1);
        buf = comcr.insertStr(buf, tmp       , 19);
        tmp = comcr.commFormat("$z,3z,3z,3z" , totPurchaseA1);
        buf = comcr.insertStr(buf, tmp       , 32);
        tmp = comcr.commFormat("2z,3z,3#"    , totVCardCnt);
        buf = comcr.insertStr(buf, tmp       , 50);

    	lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));
    }
    /***********************************************************************/
    void printDetailD52F() throws Exception {
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
        szTmp = comcr.commFormat("2z,3z,3#", hVCardCnt);
        buf = comcr.insertStr(buf, szTmp          , 50);
        buf = comcr.insertStr(buf, cardName       , 77);
        lparD52F.add(comcr.putReport(rptIdD52F, rptNameD52F, sysDate, ++rptSeqD52F, "0", buf));

        totPurchaseC1 = totPurchaseC1 + hPurchaseC1;
        totPurchaseA1 = totPurchaseA1 + hPurchaseA1;
        totVCardCnt = totVCardCnt + hVCardCnt;
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
      extendField = "bill.";
      daoTable    = "bil_curpost";
      selectSQL   = "group_code,v_card_no,sum(decode(sign_flag,'+',1,-1)) AS purchase_cnt,";
      selectSQL  += "sum(decode(sign_flag,'+',dest_amt,dest_amt*-1))      AS purchase_amt";
      whereStr    = "where 1=1 ";
      whereStr   += "  AND this_close_date LIKE ? ";
      whereStr   += "  AND acct_code IN ('BL','CA') ";
      whereStr   += "  AND payment_type = 'Q' ";  //qr code交易
      whereStr   += "  AND group_code <> '' ";
      whereStr   += "  AND v_card_no <> '' ";
      whereStr   += "GROUP BY group_code,v_card_no ";
      whereStr   += "order BY group_code,v_card_no ";

      setString(1 , (rptMonth + "%"));

      int  n = loadTable();
      setLoadData("bill.group_code"); //set key

      showLogMessage("I","","Load bil_curpost end Count: ["+n+"]" + rptMonth);
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRD52F proc = new BilRD52F();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
