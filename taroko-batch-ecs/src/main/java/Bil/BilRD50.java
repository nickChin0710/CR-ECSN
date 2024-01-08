/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/05/11 V1.01.01  Lai         program initial (load && one page)         *
*  112/10/12 V1.01.02  JeffKung    增加個人御璽卡                                                            *
*  112/11/08 V1.01.03  JeffKung    condition changed                          *                                                                           *
******************************************************************************/
package Bil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

public class BilRD50 extends AccessDAO {
    private String PROGNAME = "每月信用卡累計交易量彙總表  112/11/08 V1.01.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;
    JSONObject     rptData = null;

    int    DEBUG  = 0;
    String h_temp_user = "";

    int    rptPageLine = 45;
    String prgmId    = "BilRD50";

    String rptIdR1   = "CRD50A";
    String rptName1  = "每月信用卡累計交易量彙總表";
    int    page_cnt1 = 0, line_cnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int    totCnt = 0;
    int    totCnt1= 0;

    String hBusinssDate       = "";
    String hLastDateOfMonth   = "";
    String hCallBatchSeqno    = "";
    String hChiDate           = "";
    String hBegDate           = "";
    String hEndDate           = "";
    String hBegDate_bil       = "";
    String hEndDate_bil       = "";

    String cardGroupCode      = "";
    String cardCardType       = "";
    String cardCardNote       = "";
    String cardBinType        = "";
    String cardBin            = "";
    long   hPurchaseC         = 0;
    double hPurchaseA         = 0;
    long   hPurchaseC1        = 0;
    double hPurchaseA1        = 0;
    long   hPurchaseC2        = 0;
    double hPurchaseA2        = 0;
    String hSettlFlag         = "";
    String hAcctCode          = "";
    String hAcctType          = "";
    double gSumAll            = 0;
    double gSumAllCnt         = 0;
    double gSum1              = 0;
    double gSum2              = 0;
    double gSum3              = 0;
    double gSum4              = 0;

    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    String szTmp   = "";
    int arrayX        = 100;
    int arrayY        = 7;
    String[]   allDataH = new String [arrayX];
    String[]   allDataHText = new String [arrayX];
    double[][] allData  = new double [arrayX][arrayY];   
    int      currSettl = 0;

    buft htail = new buft();
    buf1 data  = new buf1();

/***********************************************************************/
public int mainProcess(String[] args) 
{
 try 
   {
    // ====================================
    // 固定要做的
    dateTime();
    setConsoleMode("Y");
    javaProgram = this.getClass().getName();
    showLogMessage("I", "", javaProgram + " " + PROGNAME + " Args=["+args.length+"]");
 
    // 固定要做的
    if(!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }
    // =====================================
    if(args.length > 3) {
       comc.errExit("Usage : BilRD50 [yyyymmdd] [seq_no] ", "");
      }
 
    comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
    comr  = new CommRoutine(getDBconnect()   , getDBalias());
    
    rptData = new JSONObject();
 
    hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
    if(comc.getSubString(hCallBatchSeqno,0,8).equals(comc.getSubString(comc.getECSHOME(),0,8)))
      { hCallBatchSeqno = "no-call"; 
      }
 
    String checkHome = comc.getECSHOME();
    if(hCallBatchSeqno.length() > 6) {
       if(comc.getSubString(hCallBatchSeqno,0,6).equals(comc.getSubString(checkHome,0,6))) 
         {
          comcr.hCallBatchSeqno = "no-call";
         }
      }

    comcr.hCallRProgramCode = javaProgram;
    h_temp_user = "";
    if (comcr.hCallBatchSeqno.length() == 20) {
        comcr.callbatch(0, 0, 1);
        selectSQL = " user_id ";
        daoTable = "ptr_callbatch";
        whereStr = "WHERE batch_seqno   = ?  ";

        setString(1, comcr.hCallBatchSeqno);
        int recCnt = selectTable();
        h_temp_user = getValue("user_id");
    }
    if (h_temp_user.length() == 0) {
        h_temp_user = comc.commGetUserID();
    }

    if (args.length >  0) {
        hBusinssDate = "";
        if(args[0].length() == 8) {
           hBusinssDate = args[0];
          } else {
           String ErrMsg = String.format("指定營業日[%s]", args[0]);
           comcr.errRtn(ErrMsg, "營業日長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
          }
    }
    selectPtrBusinday();
 
    hLastDateOfMonth = comm.lastdateOfmonth(hBusinssDate);
    loadBilBill(comc.getSubString(hBusinssDate,0,6));

    initArray();

    for (int k = 0; k < 3; k++)
      {
       currSettl = k;

       initArray();
       selectPtrGroupCard(currSettl);
  
       headFile(currSettl);
       writeFile(k);
      }
    tailFile();
    
    //若是月底要寫入統計報表來源資料table
    if (hBusinssDate.equals(hLastDateOfMonth)) {
   	 	insertMisReportData();
    }

    showLogMessage("I","","Read END="+totCnt);

    String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), rptIdR1);
    filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
    if(DEBUG==1) showLogMessage("I",""," Open ="+ filename);
    //comc.writeReport(filename, lpar1);
    comcr.insertPtrBatchRpt(lpar1);
 
    // ==============================================
    // 固定要做的
    comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
    showLogMessage("I", "", comcr.hCallErrorDesc);
    if (comcr.hCallBatchSeqno.length() == 20)   comcr.callbatch(1, 0, 1); // 1: 結束

    finalProcess();
    return 0;
  } catch (Exception ex) { expMethod = "mainProcess"; expHandle(ex); return exceptExit;
                         }
}
// ************************************************************************
public int  selectPtrBusinday() throws Exception 
{

   sqlCmd  = "select business_date ";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
   }
   if (recordCnt > 0) {
       if(hBusinssDate.length() < 1)
               hBusinssDate = getValue("business_date");
   }

   sqlCmd  = "select to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01'       beg_date ";
   sqlCmd += "     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') end_date ";
   sqlCmd += "     , trim(to_char(to_number(to_char(sysdate,'yyyymmdd')-19110000) ,'0000000')) chi_date ";
   sqlCmd += " from dual ";
   setString(1, hBusinssDate);
   setString(2, hBusinssDate);
   recordCnt = selectTable();
   if(recordCnt > 0) {
      hBegDate = getValue("beg_date");
      hEndDate = getValue("end_date");
      hChiDate = getValue("chi_date");
   }
   
   //資料區間改成截至當日營業日(20230817)
   hEndDate = hBusinssDate;
   
   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s]" , hBusinssDate
                         , hChiDate, hBegDate, hEndDate));

   return 0;
}
/***********************************************************************/
void  loadBilBill(String rptMonth) throws Exception
{
  extendField = "bill.";
  daoTable    = "bil_curpost";
  selectSQL   = "group_code,acct_code,settl_flag,acct_type, "; 
  selectSQL  += "sum(decode(sign_flag,'+',1,-1))                 AS purchase_cnt,";
  selectSQL  += "sum(decode(sign_flag,'+',dest_amt,dest_amt*-1)) AS purchase_amt";
  whereStr    = "where 1=1 ";
  whereStr   += "  AND this_close_date LIKE ? ";
  whereStr   += "  AND this_close_date <= ? ";
  whereStr   += "  AND acct_code  IN ('BL','CA') ";
  whereStr   += "  AND group_code <> '' ";
  whereStr   += "GROUP BY group_code,acct_code,settl_flag,acct_type ";

  setString(1 , (rptMonth + "%"));
  setString(2 , hEndDate);

  showLogMessage("I","","Begin Load bil_bill date="+rptMonth);
  int  n = loadTable();
  setLoadData("bill.group_code");  // set key

  showLogMessage("I","","Load bil_bill end Count: ["+n+"]");
}
// ************************************************************************
void  initArray() throws Exception
{
   for (int i = 0; i < arrayX; i++)
       {
        for (int j = 0; j < arrayY; j++)
            allData[i][j] = 0;
       
        allDataH[i] = "";
        allDataHText[i] = "N";  //列印出資料內容
        switch (i) 
         {
          case 0 :
            allDataH[i] = "ＶＩＳＡ    ";
            allDataHText[i] = "Y";  //不列印出資料內容;只有說明
            break;
          case 2 :
            allDataH[i] = "  個人卡無限";
            allDataHText[i] = "D";  //有資料
            break;
          case 4 :
            allDataH[i] = "      御璽卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 6 :
            allDataH[i] = "  個人御璽卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 8 :
            allDataH[i] = "        白金";
            allDataHText[i] = "D";  //有資料
            break;
          case 10 :
            allDataH[i] = "        金卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 12 :
            allDataH[i] = "        普卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 20 :
            allDataH[i] = "        小計";
            allDataHText[i] = "D";  //有資料
            break;
          case 25 :
            allDataH[i] = "  法人卡    ";
            allDataHText[i] = "D";  //有資料
            break;
          case 29 :
            allDataH[i] = "        合計";
            allDataHText[i] = "D";  //有資料
            break;
          case 30 :
            allDataH[i] = "ＭＡＳＴＥＲ  ";
            allDataHText[i] = "Y";  //不列印出資料內容;只有說明
            break;
          case 32:
            allDataH[i] = "        世界卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 34:
            allDataH[i] = "    ＣＢ鈦金卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 36:
            allDataH[i] = "    一般鈦金卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 38:
            allDataH[i] = "    鈦金商務卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 40:
            allDataH[i] = "          白金";
            allDataHText[i] = "D";  //有資料
            break;
          case 42:
            allDataH[i] = "        商旅卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 44:
            allDataH[i] = "          金卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 46:
            allDataH[i] = "          普卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 50:
            allDataH[i] = "          小計";
            allDataHText[i] = "D";  //有資料
            break;
          case 55:
            allDataH[i] = "  法人卡      ";
            allDataHText[i] = "D";  //有資料
            break;
          case 59:
            allDataH[i] = "          合計";
            allDataHText[i] = "D";  //有資料
            break;
          case 60:
            allDataH[i] = "ＪＣＢ        ";
            allDataHText[i] = "Y";  //不列印出資料內容;只有說明
            break;
          case 62:
            allDataH[i] = "        晶緻卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 64:
            allDataH[i] = "        白金卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 66:
            allDataH[i] = "          金卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 68:
            allDataH[i] = "          普卡";
            allDataHText[i] = "D";  //有資料
            break;
          case 80:
            allDataH[i] = "          小計";
            allDataHText[i] = "D";  //有資料
            break;
          case 85:
            allDataH[i] = "  法人卡      ";
            allDataHText[i] = "D";  //有資料
            break;
          case 89:
            allDataH[i] = "          合計";
            allDataHText[i] = "D";  //有資料
            break;
          case 98:
            allDataH[i] = "          總計";
            allDataHText[i] = "D";  //有資料
            break;
         }
       }
}
/***********************************************************************/
void selectPtrGroupCard(int sIdx) throws Exception 
{
  sqlCmd =  "SELECT a.group_code, a.card_type, c.card_note, ";
  sqlCmd += " (select b1.bin_no from crd_cardno_range b1 ";
  sqlCmd +=	"   where a.GROUP_CODE = b1.GROUP_CODE"; 
  sqlCmd +=	"   and   a.card_type = b1.card_type "; 
  sqlCmd +=	"   fetch first 1 rows only) as bin_no,";
  sqlCmd += " (SELECT d.bin_type FROM  ptr_bintable d, crd_cardno_range b ";
  sqlCmd += "   WHERE d.bin_no     = b.bin_no     ";
  sqlCmd += "     and a.GROUP_CODE = b.GROUP_CODE ";
  sqlCmd += "     AND a.CARD_TYPE  = b.CARD_TYPE ";
  sqlCmd += "   FETCH FIRST 1 ROWS only ) AS bin_type ";
  sqlCmd += " from ptr_card_type c, PTR_GROUP_CARD a ";
  sqlCmd += "WHERE a.card_type NOT IN ('VD') ";
  sqlCmd += "  and c.card_type    = a.card_type  ";
  sqlCmd += "  AND substr(a.card_type,1,1) IN ('V','M','J') ";
  sqlCmd += "order by a.group_code, a.card_type, c.card_note,bin_type ";

 //if(DEBUG==1) showLogMessage("I",""," SQL="+sqlCmd+" T="+hBegDate+" N="+hEndDate);

  int cursorIndex = openCursor();
  while (fetchTable(cursorIndex)) {

     initRtn();
     totCnt++;

     cardGroupCode     = getValue("group_code"  );
     cardCardType      = getValue("card_type"   );
     cardCardNote      = getValue("card_note"   );
     cardBinType       = getValue("bin_type"    );
     cardBin           = getValue("bin_no"      );

 if(DEBUG==1) showLogMessage("I","","Read IDX="+sIdx+",G="+cardGroupCode+ " B="+cardBinType+" T="+cardCardType+" Set="+sIdx);

     if(DEBUG==0 && (totCnt % 5000 == 0 || totCnt == 1))
        showLogMessage("I","",String.format("RD50 Process 1 record=[%d]", totCnt));

     setValue("bill.group_code", cardGroupCode);
     int loadBilCnt = getLoadData("bill.group_code");
if(DEBUG==1) showLogMessage("I","","  Read Group=" + cardGroupCode + ", Bil_c="+loadBilCnt);

     for ( int i=0; i<loadBilCnt; i++ ) 
       {
        hSettlFlag   = getValue("bill.settl_flag" ,i);
        hAcctCode    = getValue("bill.acct_code"  ,i);
        hAcctType    = getValue("bill.acct_type"  ,i);
        hPurchaseC   = getValueLong("bill.purchase_cnt"   ,i);
        hPurchaseA   = getValueDouble("bill.purchase_amt" ,i);
        if(sIdx == 2 && hSettlFlag.compareTo("0")   != 0)   continue;
        else if(sIdx == 1 && (hSettlFlag.compareTo("6")!= 0)&&(hSettlFlag.compareTo("8")!= 0)) continue;
        else if(sIdx == 0 && (hSettlFlag.compareTo("9")!= 0)&&(hSettlFlag.length() != 0)) continue;
              
if(DEBUG==1) showLogMessage("I","","  get bill="+hSettlFlag+","+ getValueLong("bill.purchase_cnt",i));

        switch (cardBinType)
           {
            case "V": visaRtn();      break;
            case "M": masterRtn();    break;
            case "J": jcbRtn();       break;
           }
       }
    }

  closeCursor();
}
/***********************************************************************/
public void mapRtn(int idx, String acctCode) throws Exception 
{
  hPurchaseC1 = 0; hPurchaseA1 = 0; hPurchaseC2 = 0; hPurchaseA2 = 0;
  if(hAcctCode.equals("CA"))
    {
     hPurchaseC1 = hPurchaseC;
     hPurchaseA1 = hPurchaseA;
    }
  else
    {
     hPurchaseC2 = hPurchaseC;
     hPurchaseA2 = hPurchaseA;
    }

   allData[idx][1] = allData[idx][1] + hPurchaseA2;
   allData[idx][2] = allData[idx][2] + hPurchaseC2;
   allData[idx][3] = allData[idx][3] + hPurchaseA1;
   allData[idx][4] = allData[idx][4] + hPurchaseC1;
   allData[idx][5] = allData[idx][1] + allData[idx][3];
   allData[idx][6] = allData[idx][2] + allData[idx][4];
  
   switch (cardBinType)
     {
      case "V": allData[20][1]  = allData[20][1]  + hPurchaseA2; 
                allData[20][2]  = allData[20][2]  + hPurchaseC2; 
                allData[20][3]  = allData[20][3]  + hPurchaseA1; 
                allData[20][4]  = allData[20][4]  + hPurchaseC1; 
                allData[20][5]  = allData[20][1]  + allData[20][3];
                allData[20][6]  = allData[20][2]  + allData[20][4];
                break;
      case "M": allData[50][1] = allData[50][1] + hPurchaseA2; 
                allData[50][2] = allData[50][2] + hPurchaseC2; 
                allData[50][3] = allData[50][3] + hPurchaseA1; 
                allData[50][4] = allData[50][4] + hPurchaseC1; 
                allData[50][5] = allData[50][1] + allData[50][3];
                allData[50][6] = allData[50][2] + allData[50][4];
                break;
      case "J": allData[80][1] = allData[80][1] + hPurchaseA2; 
                allData[80][2] = allData[80][2] + hPurchaseC2; 
                allData[80][3] = allData[80][3] + hPurchaseA1; 
                allData[80][4] = allData[80][4] + hPurchaseC1; 
                allData[80][5] = allData[80][1] + allData[80][3];
                allData[80][6] = allData[80][2] + allData[80][4];
                break;
     }
}
/***********************************************************************/
void visaRtn() throws Exception
{
 //  allData[6][i]++;      // 小計
 //  allData[8][i]++;      // 合計

if(DEBUG==1) showLogMessage("I","","  visa="+" note="+cardCardNote);

   if(hAcctType.equals("03") || hAcctType.equals("06"))
     {
      mapRtn(25 , hAcctCode); 
      return;
     }

   switch (cardCardNote)
     {
      case "I": mapRtn(2 , hAcctCode); break;
      case "S":
    	  if ("472228".equals(cardBin) || "472229".equals(cardBin)) {
    		  mapRtn(6 , hAcctCode);     //個人御璽卡
    	  } else {
    		  mapRtn(4 , hAcctCode);     //御璽卡
    	  }
    	  break;
      case "P": mapRtn(8 , hAcctCode); break;
      case "G": mapRtn(10 , hAcctCode); break;
      case "C": mapRtn(12 , hAcctCode); break;
     }

}
/***********************************************************************/
void masterRtn() throws Exception
{
// allData[18][2]++;
// allData[20][2]++;

if(DEBUG==1) showLogMessage("I","","  m/cd="+" note="+cardCardNote);
   if(hAcctType.equals("03") || hAcctType.equals("06"))
     {
      mapRtn( 55, hAcctCode);
      return;
     }


   switch (cardCardNote)
     {
      case "I": mapRtn(32 , hAcctCode); break;
      case "S": 
    	  if("MS".equals(cardCardType)) {
    		  mapRtn(34 , hAcctCode);     //CB鈦金卡  (combo)
    	  } else if ("6673".equals(cardGroupCode) || "6674".equals(cardGroupCode)) {
    		  mapRtn(38 , hAcctCode);     //鈦金商務卡 (雙幣)
    	  } else {
    		  mapRtn(36 , hAcctCode);     //一般鈦金卡
    	  }
    	  break;  
      case "P": 
    	  if ("1670".equals(cardGroupCode) || "1671".equals(cardGroupCode)) {
    		  mapRtn(42 , hAcctCode);  //商旅卡
    	  } else {
    		  mapRtn(40 , hAcctCode);
    	  }
    	  break;
      case "G": mapRtn(44 , hAcctCode); break;
      case "C": mapRtn(46 , hAcctCode); break;
     }
}
/**********************************************************************/
void jcbRtn() throws Exception
{
// allData[26][2]++;
// allData[28][2]++;

   if(hAcctType.equals("03") || hAcctType.equals("06"))
     {
      mapRtn( 85, hAcctCode); 
      return;
     }

   switch (cardCardNote)
     {
      case "S": mapRtn(62 , hAcctCode); break;
      case "P": mapRtn(64 , hAcctCode); break;
      case "G": mapRtn(66 , hAcctCode); break;
      case "C": mapRtn(68 , hAcctCode); break;
    }
}
/***********************************************************************/
void initRtn() throws Exception 
{
     cardGroupCode      = "";
     cardCardNote       = "";
     cardCardType       = "";
     cardBinType        = "";

     hSettlFlag         = "";
     hAcctCode          = "";
     hAcctType          = "";
}
/***********************************************************************/
void headFile(int idx) throws Exception 
{
        String temp    = "";
        String headStr = "";
        if(idx == 0)       headStr = " 國內消費 - 自行";
        else if(idx == 1)  headStr = " 國內消費 - 他行";
        else if(idx == 2)  headStr = " 國外消費";

        page_cnt1++;
        if(page_cnt1 > 1)
           lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));

        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
        buf = comcr.insertStr(buf, ""              + rptName1                 , 50);
        buf = comcr.insertStr(buf, "保存年限: 五年"                           ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiDate.substring(0, 3),
                       hChiDate.substring(3, 5), hChiDate.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRD50A    科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
        temp = String.format("%4d", page_cnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp                         ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        buf = "";
        buf = headStr + "     累計起訖日: " + hBegDate +" - " + hEndDate;
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = "                                     簽帳金額    簽帳筆數    預現金額    預現筆數     金額合計    筆數合計";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

//        buf = "================================= =========== ============ =========== =========== =========== =========== ";
//        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        line_cnt1 = 4;
}
/***********************************************************************/
 void tailFile() throws UnsupportedEncodingException 
{
   buf = "";
   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
   buf = "";
   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

     szTmp            = "(國內外金額合計) = ";
     data.name        = szTmp;
     
     data.onlyName    = "D";   //有資料

        szTmp = comcr.commFormat("3z,3z,3z"  , gSum1);
        data.data01      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z"  , gSum2);
        data.data02      = szTmp;

        szTmp = comcr.commFormat("3z,3z,3z"  , gSum3);
        data.data03      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z"  , gSum4);
        data.data04      = szTmp;

     szTmp = comcr.commFormat("zz,3z,3z,3z", gSumAll);
     data.data05      = szTmp;
     
     szTmp = comcr.commFormat("2z,3z,3z"  , gSumAllCnt);
     data.data06      = szTmp;

     buf = data.allText();
     lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

}
/***********************************************************************/
void writeFile(int k) throws Exception 
{
     String tmp   = "";
     String szTmp = "";

     if(line_cnt1 > rptPageLine) {
        headFile(currSettl);
       }

     int vCnt = 29;
     int mCnt = 59;
     int jCnt = 89;
     int tCnt = 98;
     for (int i = 0; i < 100; i++)
       {
    	 
    	 //非資料列不處理
    	 if ("N".equals(allDataHText[i])) {
    		 continue;
    	 }
    	 
    	 if (i==vCnt) {
    		 //visa
    		 allData[vCnt][1]  = allData[25][1]  + allData[20][1];
    		 allData[vCnt][2]  = allData[25][2]  + allData[20][2];
    		 allData[vCnt][3]  = allData[25][3]  + allData[20][3];
    		 allData[vCnt][4]  = allData[25][4]  + allData[20][4];
    		 allData[vCnt][5]  = allData[vCnt][1]    + allData[vCnt][3];
    		 allData[vCnt][6]  = allData[vCnt][2]    + allData[vCnt][4];
    	 }
    	 
    	 if (i==mCnt) {
    		 //master
    		 allData[mCnt][1] = allData[55][1] + allData[50][1];
    		 allData[mCnt][2] = allData[55][2] + allData[50][2];
    		 allData[mCnt][3] = allData[55][3] + allData[50][3];
    		 allData[mCnt][4] = allData[55][4] + allData[50][4];
    		 allData[mCnt][5] = allData[mCnt][1]   + allData[mCnt][3];
    		 allData[mCnt][6] = allData[mCnt][2]   + allData[mCnt][4];
    	 }
    	 
    	 if (i==jCnt) {
    		 //jcb 
    		 allData[jCnt][1] = allData[85][1] + allData[80][1];
    		 allData[jCnt][2] = allData[85][2] + allData[80][2];
    		 allData[jCnt][3] = allData[85][3] + allData[80][3];
    		 allData[jCnt][4] = allData[85][4] + allData[80][4];
    		 allData[jCnt][5] = allData[jCnt][1]   + allData[jCnt][3];
    		 allData[jCnt][6] = allData[jCnt][2]   + allData[jCnt][4];
    	 }
    	 
        // 總計
        if (i == tCnt) {
           allData[tCnt][1] = allData[vCnt][1] + allData[mCnt][1] + allData[jCnt][1];
           allData[tCnt][2] = allData[vCnt][2] + allData[mCnt][2] + allData[jCnt][2];
           allData[tCnt][3] = allData[vCnt][3] + allData[mCnt][3] + allData[jCnt][3];
           allData[tCnt][4] = allData[vCnt][4] + allData[mCnt][4] + allData[jCnt][4];
           allData[tCnt][5] = allData[tCnt][1] + allData[tCnt][3];
           allData[tCnt][6] = allData[tCnt][2] + allData[tCnt][4];
          }

        szTmp            = allDataH[i];
        data.name        = szTmp;
        
        data.onlyName    = allDataHText[i];

        szTmp = comcr.commFormat("3z,3z,3z"  , allData[i][1]);
        data.data01      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z"  , allData[i][2]);
        data.data02      = szTmp;

        szTmp = comcr.commFormat("3z,3z,3z"  , allData[i][3]);
        data.data03      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z"  , allData[i][4]);
        data.data04      = szTmp;

        szTmp = comcr.commFormat("z,3z,3z,3z", allData[i][5]);
        data.data05      = szTmp;
        
        szTmp = comcr.commFormat("2z,3z,3z"  , allData[i][6]);
        data.data06      = szTmp;

        buf = data.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
   
        line_cnt1 = line_cnt1 + 1;
       }

     gSum1   = gSum1   + allData[tCnt][1];
     gSum2   = gSum2   + allData[tCnt][2];
     gSum3   = gSum3   + allData[tCnt][3];
     gSum4   = gSum4   + allData[tCnt][4];
     gSumAll = gSumAll + allData[tCnt][5];
     gSumAllCnt = gSumAllCnt + allData[tCnt][6];

     //若是月底要寫入統計報表來源資料table
     if (hBusinssDate.equals(hLastDateOfMonth)) {
    	 if (k==0) {
    		 rptData.put("NAT_V_ONUS_RTL_AMT", allData[vCnt][1]);
    		 rptData.put("NAT_M_ONUS_RTL_AMT", allData[mCnt][1]);
    		 rptData.put("NAT_J_ONUS_RTL_AMT", allData[jCnt][1]);
    		 rptData.put("NAT_V_ONUS_CSH_AMT", allData[vCnt][3]);
    		 rptData.put("NAT_M_ONUS_CSH_AMT", allData[mCnt][3]);
    		 rptData.put("NAT_J_ONUS_CSH_AMT", allData[jCnt][3]);
    		 rptData.put("NAT_V_ONUS_RTL_CNT", allData[vCnt][2]);
    		 rptData.put("NAT_M_ONUS_RTL_CNT", allData[mCnt][2]);
    		 rptData.put("NAT_J_ONUS_RTL_CNT", allData[jCnt][2]);
    		 rptData.put("NAT_V_ONUS_CSH_CNT", allData[vCnt][4]);
    		 rptData.put("NAT_M_ONUS_CSH_CNT", allData[mCnt][4]);
    		 rptData.put("NAT_J_ONUS_CSH_CNT", allData[jCnt][4]);
    	 } else if (k==1) {
    		 rptData.put("NAT_V_OFUS_RTL_AMT", allData[vCnt][1]);
    		 rptData.put("NAT_M_OFUS_RTL_AMT", allData[mCnt][1]);
    		 rptData.put("NAT_J_OFUS_RTL_AMT", allData[jCnt][1]);
    		 rptData.put("NAT_V_OFUS_CSH_AMT", allData[vCnt][3]);
    		 rptData.put("NAT_M_OFUS_CSH_AMT", allData[mCnt][3]);
    		 rptData.put("NAT_J_OFUS_CSH_AMT", allData[jCnt][3]);
    		 rptData.put("NAT_V_OFUS_RTL_CNT", allData[vCnt][2]);
    		 rptData.put("NAT_M_OFUS_RTL_CNT", allData[mCnt][2]);
    		 rptData.put("NAT_J_OFUS_RTL_CNT", allData[jCnt][2]);
    		 rptData.put("NAT_V_OFUS_CSH_CNT", allData[vCnt][4]);
    		 rptData.put("NAT_M_OFUS_CSH_CNT", allData[mCnt][4]);
    		 rptData.put("NAT_J_OFUS_CSH_CNT", allData[jCnt][4]);
    	 } else if (k==2) {
    		 rptData.put("INT_V_RTL_AMT", allData[vCnt][1]);
    		 rptData.put("INT_M_RTL_AMT", allData[mCnt][1]);
    		 rptData.put("INT_J_RTL_AMT", allData[jCnt][1]);
    		 rptData.put("INT_V_CSH_AMT", allData[vCnt][3]);
    		 rptData.put("INT_M_CSH_AMT", allData[mCnt][3]);
    		 rptData.put("INT_J_CSH_AMT", allData[jCnt][3]);
    		 rptData.put("INT_V_RTL_CNT", allData[vCnt][2]);
    		 rptData.put("INT_M_RTL_CNT", allData[mCnt][2]);
    		 rptData.put("INT_J_RTL_CNT", allData[jCnt][2]);
    		 rptData.put("INT_V_CSH_CNT", allData[vCnt][4]);
    		 rptData.put("INT_M_CSH_CNT", allData[mCnt][4]);
    		 rptData.put("INT_J_CSH_CNT", allData[jCnt][4]);
    	 }
     }
     
     return;
}

void deleteExistRptRecord() throws Exception {
	
	daoTable  = " mis_report_data ";
	whereStr  = " where 1=1 "; 
	whereStr += " and data_month = ? ";
	whereStr += " and data_from in ('CRD50A','CRM68') ";
	
	setString(1, comc.getSubString(hBusinssDate,0,6));

	deleteTable();
	
}
void insertMisReportData() throws Exception {
	
	//重跑時要先刪除上一次產生的資料
	deleteExistRptRecord();
	commitDataBase();
	
    setValue("DATA_MONTH", comc.getSubString(hBusinssDate,0,6));
    setValue("DATA_FROM", "CRD50A");
    setValue("DATA_DATE", hBusinssDate);
    setValueDouble("SUM_FIELD1", gSum1);
    setValueDouble("SUM_FIELD2", gSum3);
    setValueDouble("SUM_FIELD3", 0);
    setValueDouble("SUM_FIELD4", 0);
    setValueDouble("SUM_FIELD5", 0);
    setValue("DATA_CONTENT", rptData.toString());
    setValue("MOD_TIME", sysDate+sysTime);
    setValue("MOD_PGM", javaProgram);

    daoTable = "mis_report_data";
    insertTable();
    
    
    //先initialize一筆0的資料
    JSONObject rptDataCRM68 = new JSONObject();
    
    setValue("DATA_MONTH", comc.getSubString(hBusinssDate,0,6));
    setValue("DATA_FROM", "CRM68");
    setValue("DATA_DATE", hBusinssDate);
    setValueDouble("SUM_FIELD1", 0);
    setValueDouble("SUM_FIELD2", 0);
    setValueDouble("SUM_FIELD3", 0);
    setValueDouble("SUM_FIELD4", 0);
    setValueDouble("SUM_FIELD5", 0);
    rptDataCRM68.put("REVOLVE_BAL", 0);
    setValue("DATA_CONTENT", rptDataCRM68.toString());
    setValue("MOD_TIME", sysDate+sysTime);
    setValue("MOD_PGM", javaProgram);

    daoTable = "mis_report_data";
    insertTable();

    
}

/************************************************************************/
public static void main(String[] args) throws Exception 
{
       BilRD50 proc = new BilRD50();
       int  retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/************************************************************************/
  class buft 
    {
        String file_value;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(file_value ,140);
//          rtn += fixLeft(len, 1);
            return rtn;
        }

        
    }
  class buf1 
    {
        String name;
        String onlyName;
        String data01;
        String data02;
        String data03;
        String data04;
        String data05;
        String data06;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            
            if ("Y".equals(onlyName)) {
            	data01 = "";
            	data02 = "";
            	data03 = "";
            	data04 = "";
            	data05 = "";
            	data06 = "";
            }

            rtn += fixLeft(name         , 32+1);
            rtn += fixLeft(data01       ,  12+1);
            rtn += fixLeft(data02       ,  10+1);
            rtn += fixLeft(data03       ,  12+1);
            rtn += fixLeft(data04       ,  10+1);
            rtn += fixLeft(data05       ,  13+1);
            rtn += fixLeft(data06       ,  10+1);
 //         rtn += fixLeft(len          ,  1);
            return rtn;
        }

       
    }
String fixLeft(String str, int len) throws UnsupportedEncodingException {
        int size = (Math.floorDiv(len, 100) + 1) * 100;
        String spc = "";
        for (int i = 0; i < size; i++)    spc += " ";
        if (str == null)                  str  = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);

        return new String(vResult, "MS950");
    }

}
/*
　	第2碼	C	普	C:普卡
　		G	金	G:金卡
　		P	白金卡	P:白金卡
　		T	晶緻卡/御璽卡/鈦金	S:卓越卡
　		B	商務卡	　
　		E	電子採購卡	　
　		I	無限/世界卡	I:頂級卡
　		O	COMBO 普	　
　		Q	COMBO 金	　
　		R	COMBO 白金卡	　
　		S	COMBO 鈦金/御璽卡	　
　		D	VD	　
*/
