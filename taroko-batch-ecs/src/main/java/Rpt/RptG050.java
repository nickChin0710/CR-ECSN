/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/06/19 V1.01.01  Lai         program initial                            *
*  112/07/28 V1.01.02  Lai         alter array_x 30 -> 50                     *
*  112/08/02 V1.01.03  Lai         select Rpt_id = 'CRM68'                    *
*  112/12/25 V1.01.04  Lai         modify is_select = 'Y'                     *
*  112/12/27 V1.01.05  Lai         modify hPrevMonth                          *
*  112/12/27 V1.01.06  Ryan        調整報表金額長度                                                                                         *
******************************************************************************/
package Rpt;

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
import com.*;

/*國際信用卡清算明細表-信用卡*/
public class RptG050 extends AccessDAO {
    private String progname = "每月信用卡循環信用利率分佈情形表  112/12/27 V1.01.05";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommDate    commDate = new CommDate();

    int    DEBUG    = 0;

    String prgmId   = "RptG050";
    String rptId    = "CRM68";
    String rptName  = "每月信用卡循環信用利率分佈情形表";
    int    rptSeq   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    
    String buf    = "";
    String tmp    = "";
    String szTmp  = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hIdnoChiName = "";
    String hPrintName   = "";
    String hRptName     = "";
    String hBusinssDate = "";
    String hChiDate     = "";
    String hPrevMonth   = "";

    int totalCnt = 0;
    int indexCnt = 0;
    int pageCnt  = 0;
    int lineCnt  = 0;

    String hIsLegalPerson = "";
    String hIsDisableC    = "";
    String hCardType      = "";
    String hCardNote      = "";
    double hRcrateYear    = 0;
    double hRcrateCpAmt   = 0;
    double hRcrateCountC  = 0;
    String tempPerson     = "";
    String tempDisable    = "";
    double tempRcrateYear = 0;

    int array_x         = 50;
    int array_y         = 19;
    String[] all_data_h = new String [array_x];
    int[][]  all_data   = new int [array_x][array_y];
    int array_idx       = 0;
    int sum1            = 0;
    int sum2            = 0;
    int sum3            = 0;
    int sum4            = 0;
    int sum5            = 0;
    int sum6            = 0;
    int sum7            = 0;
    int sum8            = 0;
    int sum9            = 0;
    int sum10           = 0;
    int sum11           = 0;
    int sum12           = 0;
    int sum13           = 0;
    int sum14           = 0;
    int sum15           = 0;
    int sum16           = 0;
    int sum17           = 0;
    int sum18           = 0;
    int rptPnt          = 2;

/***********************************************************************/
public static void main(String[] args) throws Exception {
   RptG050 proc = new RptG050();
   int retCode = proc.mainProcess(args);
   proc.programEnd(retCode);
}
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
   showLogMessage("I", "", javaProgram + " " + progname);

   if (!connectDataBase()) {
       comc.errExit("connect DataBase error", "");
   }

   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   commonRtn();
   
   showLogMessage("I", "", "營業日期=[" + hBusinssDate + "]");
   
   if (args.length == 1 && args[0].length() == 8) {
   	hBusinssDate = args[0];
   //	hPrevMonth   = hBusinssDate.substring(0, 6);
   	hPrevMonth   = commDate.dateAdd(hBusinssDate, 0,-1, 0).substring(0, 6);
   	hChiDate     = String.format("%07d",comcr.str2long(hBusinssDate)-19110000);
   }

   showLogMessage("I", "", "資料處理日期=[" + hBusinssDate + "]" + hPrevMonth);

   init_array();

   selectPtrRcrateAnal();

   showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "]");

   if (pageCnt > 0) {

       String ftpName = String.format("%s.%s", rptId, sysDate);
       String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptId, sysDate);
       //改為線上報表
       comcr.insertPtrBatchRpt(lpar1);
       comc.writeReport(filename, lpar1);
   }
   
   commitDataBase();
   
   // ==============================================
   // 固定要做的
   showLogMessage("I", "", "執行結束");
   finalProcess();
   return 0;
  }  catch (Exception ex) {
            expMethod = "mainProcess"; expHandle(ex); return exceptExit;
       }
}
/***********************************************************************/
void commonRtn() throws Exception {
   hBusinssDate = "";
   sqlCmd  = "select business_date ";
   sqlCmd += "     , substr(to_char(to_number(business_date) - 19110000,'0000000'),2,7)  h_chi_date ";
   sqlCmd += "     , to_char(add_months(to_date(business_date,'yyyymmdd'), -1),'yyyymm') h_prev_month ";
   sqlCmd += "  from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select_ptr_businday not found!", "", "");
   }
   if (recordCnt > 0) {
       hBusinssDate  = getValue("business_date");
       hPrevMonth    = getValue("h_prev_month");
       hChiDate      = getValue("h_chi_date");
   }
}
// ************************************************************************
void  initData() throws Exception
{
    sum1            = 0;
    sum2            = 0;
    sum3            = 0;
    sum4            = 0;
    sum5            = 0;
    sum6            = 0;
    sum7            = 0;
    sum8            = 0;
    sum9            = 0;
    sum10           = 0;
    sum11           = 0;
    sum12           = 0;
    sum13           = 0;
    sum14           = 0;
    sum15           = 0;
    sum16           = 0;
    sum17           = 0;
    sum18           = 0;
}
// ************************************************************************
void  init_array() throws Exception
{
   for (int i = 0; i < array_x; i++)
       {
        all_data_h[i] = "";
        for (int j = 0; j < array_y; j++)
            all_data[i][j] = 0;
       }

}
/***********************************************************************/
void selectPtrRcrateAnal() throws Exception {

   // 動用循環 利息金額-RCRATE_RI_AMTselect c.RCRATE_YEAR,a.curr_code,  
   sqlCmd  = "select a.is_disable_c    ";
   sqlCmd += "     , a.is_legal_person ";
   sqlCmd += "     , a.rcrate_year     ";
   sqlCmd += "     , a.card_type       ";
   sqlCmd += "     , c.card_note       ";
   sqlCmd += "     , sum(a.rcrate_cp_amt)  as rcrate_cp_amt ";
   sqlCmd += "     , sum(a.rcrate_count_c) as rcrate_count_c";
   sqlCmd += "  from ptr_card_type c, ptr_rcrate_anal a ";
   sqlCmd += " where a.proc_month   = ? ";
   sqlCmd += "   and a.rpt_id       = ? ";
   sqlCmd += "   and a.is_select    = 'Y' ";
   sqlCmd += "   and c.card_type    = a.card_type  ";
   sqlCmd += " group by a.is_disable_c, a.is_legal_person, a.rcrate_year, a.card_type, c.card_note ";
   sqlCmd += " order by a.is_disable_c, a.is_legal_person, a.rcrate_year, a.card_type, c.card_note ";

   setString(1, hPrevMonth);
   setString(2, "CRM141");

   int cursorIndex = openCursor();
   while (fetchTable(cursorIndex)) {

      hIsDisableC       = getValue("is_disable_c");
      hIsLegalPerson    = getValue("is_legal_person");
      hRcrateYear       = getValueDouble("rcrate_year");
      hCardType         = getValue("card_type");
      hCardNote         = getValue("card_note");
      hRcrateCpAmt      = getValueDouble("rcrate_cp_amt");
      hRcrateCountC     = getValueDouble("rcrate_count_c");

      totalCnt++;
      if (totalCnt % 10000 == 0 || totalCnt==1) showLogMessage("I","","Process record=["+totalCnt+"]");
if(DEBUG==1) showLogMessage("I","","Read Year=["+hIsDisableC+"]["+hIsLegalPerson+"]"+hRcrateYear+","+hRcrateCpAmt+" indexCnt="+ indexCnt+ ","+ tempDisable+",Note="+ hCardNote);

/* test  
      if(hIsLegalPerson.equals("Y") && (hRcrateYear != 6 && hRcrateYear != 14.6) )  continue;
*/

      if(totalCnt == 1 || (!tempDisable.equals(hIsDisableC))) {
         if((!tempDisable.equals(hIsDisableC) && totalCnt > 1))    printFooter();
         printHeader();
         tempPerson     = hIsLegalPerson;
         tempDisable    = hIsDisableC;
         tempRcrateYear = hRcrateYear;
         initData();
      }
      if(!tempPerson.equals(hIsLegalPerson) || (!tempDisable.equals(hIsDisableC)))
        {
         printFooter();

         buf = "";
         buf = comcr.insertStr(buf, "法人卡"      , 1);
         lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
         tempPerson  = hIsLegalPerson;
        }

      if (indexCnt > 50) { //分頁控制
          printHeader();
          indexCnt = 0;
      }
     switch (comc.getSubString(hCardType,0,1))
       {
        case "V": visaRtn();    break;
        case "M": masterRtn();  break;
        case "J": jcbRtn();     break;
       }

     if(tempRcrateYear != hRcrateYear)
       {
        printDetail();
        tempRcrateYear = hRcrateYear;
       }
   }

   if (indexCnt != 0)
      {
       printDetail();
       printFooter();
      }
}
/***********************************************************************/
void visaRtn() throws Exception
{
   switch (hCardNote)
     {
      case "I":
               all_data[array_idx][2]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][11] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "S":
               all_data[array_idx][4]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][10] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "P":
               all_data[array_idx][5]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][14] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "G":
               all_data[array_idx][6]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][15] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "C":
               all_data[array_idx][7]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][16] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;

     }
}
/***********************************************************************/
void masterRtn() throws Exception
{

   switch (hCardNote)
     {
      case "I":
               all_data[array_idx][1]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][10] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "S":
               all_data[array_idx][3]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][12] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "P":
               all_data[array_idx][5]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][14] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "G":
               all_data[array_idx][6]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][15] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "C":
               all_data[array_idx][7]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][16] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
     }

}
/***********************************************************************/
void jcbRtn() throws Exception
{
   switch (hCardNote)
     {
      case "S":
               all_data[array_idx][8]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][17] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "P":
               all_data[array_idx][5]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][14] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "G":
               all_data[array_idx][6]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][15] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
      case "C":
               all_data[array_idx][7]  += hRcrateCountC; all_data[array_idx][9]  += hRcrateCountC;
               all_data[array_idx][16] += hRcrateCpAmt;  all_data[array_idx][18] += hRcrateCpAmt;
               break;
     }
}
/***********************************************************************/
void printHeader() {
   pageCnt++;

   if(pageCnt > 1) lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));

   buf = "";
   buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
   buf = comcr.insertStrCenter(buf, rptName                              ,110);
   buf = comcr.insertStr(buf, "保存年限: 五年"                           , 90);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   buf = "";
   tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiDate.substring(0, 3),
                 hChiDate.substring(3, 5), hChiDate.substring(5));
   buf = comcr.insertStr(buf, "報表代號:" + rptId              ,  1);
   buf = comcr.insertStrCenter(buf, "中華民國 " + tmp          ,110);
   buf = comcr.insertStr(buf, "頁    次:"                      , 90);
   szTmp = String.format("%4d", pageCnt);
   buf = comcr.insertStr(buf, szTmp                            ,100);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   buf = "";
   for (int i = 0; i < 169; i++)     buf += "-";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   tmp = "     流通卡       ";
   if(hIsDisableC.equals("Y"))     tmp = "     已停用卡     ";
   buf = tmp+"----- 動用循環卡數 -----                                       ----- 動用循環金額 -----";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   buf = "利   率 世界卡 無限卡 鈦金卡 御璽卡 白金卡 金  卡 普  卡 晶緻卡 小  計 世  界  卡 無  限  卡 鈦  金  卡 御  璽  卡 白  金  卡 金      卡 普      卡 晶  緻  卡 小      計";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   buf = "======= ====== ====== ====== ====== ====== ====== ====== ====== ====== ========== ========== ========== ========== ========== ========== ========== ========== ==========";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));
}
/***********************************************************************/
void printFooter() throws Exception {
   buf = "";
   for (int i = 0; i < 169; i++) buf += "-";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
   sum9  = sum1  + sum2  + sum3  + sum4  + sum5  + sum6  + sum7  + sum8 ;
   sum18 = sum10 + sum11 + sum12 + sum13 + sum14 + sum15 + sum16 + sum17;
   buf = "";
   buf = comcr.insertStr(buf, "小  計:"    ,  1);
   szTmp = String.format("%6d"         , sum1);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+7);
   szTmp = String.format("%6d"         , sum2);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+14);
   szTmp = String.format("%6d"         , sum3);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+21);
   szTmp = String.format("%6d"         , sum4);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+28);
   szTmp = String.format("%6d"         , sum5);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+35);
   szTmp = String.format("%6d"         , sum6);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+42);
   szTmp = String.format("%6d"         , sum7);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+49);
   szTmp = String.format("%6d"         , sum8);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+56);
   szTmp = String.format("%6d"         , sum9);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+63);
   szTmp = String.format("%10d"         , sum10);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+70);
   szTmp = String.format("%10d"         , sum11);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+81);
   szTmp = String.format("%10d"         , sum12);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+92);
   szTmp = String.format("%10d"         , sum13);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+103);
   szTmp = String.format("%10d"         , sum14);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+114);
   szTmp = String.format("%10d"         , sum15);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+125);
   szTmp = String.format("%10d"         , sum16);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+136);
   szTmp = String.format("%10d"         , sum17);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+147);
   szTmp = String.format("%10d"         , sum18);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+158);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   buf = "";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   initData();
}
/***********************************************************************/
void printDetail() throws Exception {
   lineCnt++;
   indexCnt++;

   buf = "";
   szTmp = comcr.commFormat("2z.2z"      , tempRcrateYear)+"%";
   buf = comcr.insertStr(buf, szTmp, 1);
   szTmp = String.format("%6d"         , all_data[array_idx][1]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+7);
   szTmp = String.format("%6d"         , all_data[array_idx][2]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+14);
   szTmp = String.format("%6d"         , all_data[array_idx][3]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+21);
   szTmp = String.format("%6d"         , all_data[array_idx][4]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+28);
   szTmp = String.format("%6d"         , all_data[array_idx][5]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+35);
   szTmp = String.format("%6d"         , all_data[array_idx][6]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+42);
   szTmp = String.format("%6d"         , all_data[array_idx][7]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+49);
   szTmp = String.format("%6d"         , all_data[array_idx][8]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+56);
   szTmp = String.format("%6d"         , all_data[array_idx][9]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 6), rptPnt+63);
   szTmp = String.format("%10d"         , all_data[array_idx][10]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+70);
   szTmp = String.format("%10d"         , all_data[array_idx][11]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+81);
   szTmp = String.format("%10d"         , all_data[array_idx][12]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+92);
   szTmp = String.format("%10d"         , all_data[array_idx][13]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+103);
   szTmp = String.format("%10d"         , all_data[array_idx][14]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+114);
   szTmp = String.format("%10d"         , all_data[array_idx][15]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+125);
   szTmp = String.format("%10d"         , all_data[array_idx][16]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+136);
   szTmp = String.format("%10d"         , all_data[array_idx][17]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+147);
   szTmp = String.format("%10d"         , all_data[array_idx][18]);
   buf = comcr.insertStr(buf, comc.fixRight(szTmp, 10), rptPnt+158);

   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

     sum1  += all_data[array_idx][1];
     sum2  += all_data[array_idx][2];
     sum3  += all_data[array_idx][3];
     sum4  += all_data[array_idx][4];
     sum5  += all_data[array_idx][5];
     sum6  += all_data[array_idx][6];
     sum7  += all_data[array_idx][7];
     sum8  += all_data[array_idx][8];
     sum10 += all_data[array_idx][10];
     sum11 += all_data[array_idx][11];
     sum12 += all_data[array_idx][12];
     sum13 += all_data[array_idx][13];
     sum14 += all_data[array_idx][14];
     sum15 += all_data[array_idx][15];
     sum16 += all_data[array_idx][16];
     sum17 += all_data[array_idx][17];
     sum9   = all_data[array_idx][1]  + all_data[array_idx][2]  + all_data[array_idx][3]
            + all_data[array_idx][4]  + all_data[array_idx][5]  + all_data[array_idx][6]
            + all_data[array_idx][7]  + all_data[array_idx][8];
     sum18  = all_data[array_idx][10] + all_data[array_idx][11] + all_data[array_idx][12]
            + all_data[array_idx][13] + all_data[array_idx][14] + all_data[array_idx][15]
            + all_data[array_idx][16] + all_data[array_idx][17];

if(DEBUG==1) showLogMessage("I","","   3333=["+sum3       +"]["+all_data[array_idx][3]+"]");
   array_idx++;
if(DEBUG==1) showLogMessage("I","","  Dtl idx=["+array_idx+"]");
}
/***********************************************************************/
}
/* V-無限 V-御璽 V-白金 V-金卡 V-普卡
   M-世界 M-鈦金 M-鈦商 M-白金 M-金卡 M-普卡
   J-白金 J-金卡 J-普卡 J-晶緻  */
