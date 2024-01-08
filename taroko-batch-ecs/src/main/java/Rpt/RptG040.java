/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/06/21 V1.01.01  Lai         program initial                            *
*  112/08/02 V1.01.02  Lai         select Rpt_id = 'CRM69'                    *
*  112/12/25 V1.01.03  Lai         modify is_select = 'Y'                     *
*  112/12/27 V1.01.04  Lai         modify hPrevMonth                          *
*                                                                             *
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
public class RptG040 extends AccessDAO {
    private String progname = "信用卡各循環利率區間、分布之揭露彙總表  112/12/27 V1.01.04";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommDate    commDate = new CommDate();

    int    DEBUG    = 0;

    String prgmId   = "RptG040";
    String rptName  = "信用卡各循環利率區間、分布之揭露彙總表";
    String rptId    = "CRM69D";
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

    String swMid          = "Y";
    double hRcrateYear    = 0;
    double hFlowCountA    = 0;
    double allFlowCountA  = 0;
    double All_1          = 0;
    double All_2          = 0;
    double tDouble        = 0;
    double Fin_1          = 0;
    double Fin_2          = 0;
    double Fin_3          = 0;
    double allCnt         = 0;
    double maxCnt         = 0;
    double maxCntRate     = 0;
    double midCntRate     = 0;
    double midFlowA       = 0;


/***********************************************************************/
public static void main(String[] args) throws Exception {
   RptG040 proc = new RptG040();
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
// 	hPrevMonth   = hBusinssDate.substring(0, 6);
        hPrevMonth   = commDate.dateAdd(hBusinssDate, 0,-1, 0).substring(0, 6);
        hChiDate     = String.format("%07d",comcr.str2long(hBusinssDate)-19110000);
   }

   showLogMessage("I", "", "資料處理日期=[" + hBusinssDate + "]" + hPrevMonth);

   selectPtrRcrateAnal_0();

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
/***********************************************************************/
void selectPtrRcrateAnal_0() throws Exception {

   sqlCmd  = "select count(DISTINCT(rcrate_year)) as all_cnt        ";
   sqlCmd += "     , sum(flow_count_a)            as flow_count_a   ";
   sqlCmd += "  from ptr_rcrate_anal a  ";
   sqlCmd += " where a.proc_month   = ? ";
   sqlCmd += "   and a.rpt_id       = ? ";
   sqlCmd += "   and a.is_select    = 'Y' ";
   sqlCmd += "   and a.is_disable_c = 'N' ";
   sqlCmd += "   and a.rcrate_year  <> 0   ";

   setString(1, hPrevMonth);
   setString(2, "CRM69");

   int recordCnt = selectTable();
   allFlowCountA = getValueDouble("flow_count_a");
   allCnt        = getValueDouble("all_cnt");
   midFlowA      = allFlowCountA/2;
   if(DEBUG == 1) showLogMessage("I","","ALL Cnt=["+allCnt+"] sum="+allFlowCountA+","+midFlowA);

}
/***********************************************************************/
void selectPtrRcrateAnal() throws Exception {

   // 動用循環 利息金額-RCRATE_RI_AMTselect c.RCRATE_YEAR,a.curr_code,  
   sqlCmd  = "select a.rcrate_year     ";
   sqlCmd += "     , sum(a.flow_count_a) as flow_count_a ";
   sqlCmd += "  from ptr_rcrate_anal a ";
   sqlCmd += " where a.proc_month   = ? ";
   sqlCmd += "   and a.rpt_id       = ? ";
   sqlCmd += "   and a.is_select    = 'Y' ";
   sqlCmd += "   and a.is_disable_c <> 'Y' ";
   sqlCmd += "   and a.rcrate_year  <> 0   ";
   sqlCmd += " group by a.rcrate_year ";
   sqlCmd += " order by a.rcrate_year ";

   setString(1, hPrevMonth);
   setString(2, "CRM69");

   int cursorIndex = openCursor();
   while (fetchTable(cursorIndex)) {

      hRcrateYear       = getValueDouble("rcrate_year");
      hFlowCountA       = getValueDouble("flow_count_a");

      totalCnt++;
      if (totalCnt % 10000 == 0 || totalCnt==1) showLogMessage("I","","Process record=["+totalCnt+"]");
if(DEBUG==1) showLogMessage("I","","Read Year=["+hRcrateYear+"],"+hFlowCountA);

      if(hFlowCountA  < 1)  tDouble = 0;
      else                  tDouble = conv_amt_dp2r(hFlowCountA*100/allFlowCountA);

      if(totalCnt == allCnt)   tDouble = 100 - All_2;

      All_1   += hFlowCountA;
      All_2   += tDouble;
      Fin_1   += hRcrateYear * tDouble;
if(DEBUG==1) showLogMessage("I","","  fin=["+All_1+"],"+hRcrateYear+ ", mid="+midFlowA);

      if (All_1 > midFlowA && swMid.equals("Y")) {
          midCntRate  = hRcrateYear;
          swMid          = "N";
      }
      if (indexCnt == 0) {
          printHeader();
          maxCnt     = hFlowCountA;
          maxCntRate = hRcrateYear;
      }

      if(hFlowCountA > maxCnt) {
          maxCnt     = hFlowCountA;
          maxCntRate = hRcrateYear;
      }

      if (indexCnt > 50) { //分頁控制
          lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
          printHeader();
          indexCnt = 0;
      }

      printDetail();
   }

   if (indexCnt != 0)
      {
       printFooter();
       printFinal();
      }
}
/***********************************************************************/
void printHeader() {
   pageCnt++;

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
   for (int i = 0; i < 110; i++)     buf += "-";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   buf = "";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   buf = "";
   buf = comcr.insertStr(buf, "循環利率"       , 10);
   buf = comcr.insertStr(buf, "流通卡正卡戶數" , 32);
   buf = comcr.insertStr(buf, "分布比率(%)"    , 60);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   buf = comcr.insertStr(buf, "========"       , 10);
   buf = comcr.insertStr(buf, "==============" , 32);
   buf = comcr.insertStr(buf, "==========="    , 60);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
}
/***********************************************************************/
void printFooter()  throws Exception {

   buf = "";
   for (int i = 0; i < 110; i++) buf += "-";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
   buf = "";
   buf = comcr.insertStr(buf, "合  計:"  , 10);
   szTmp = comcr.commFormat("3z,3z,3z,3z", All_1);
   buf = comcr.insertStr(buf, szTmp      , 30);
   szTmp = comcr.commFormat("3z,3z.2z"   , All_2);
   buf = comcr.insertStr(buf, szTmp      , 60);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

}
/***********************************************************************/
void printFinal() throws Exception {

   buf = "";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
   buf = "";
   buf = comcr.insertStr(buf, "平均數:"  , 10);
   szTmp = comcr.commFormat("3z,3z.2z"    , conv_amt_dp2r(Fin_1 / 100));
   buf = comcr.insertStr(buf, szTmp+"%"  , 18);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   buf = comcr.insertStr(buf, "中位數:"  , 10);
   szTmp = comcr.commFormat("3z,3z.2z"   , midCntRate);
   buf = comcr.insertStr(buf, szTmp+"%"  , 18);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   buf = comcr.insertStr(buf, "眾  數:"  , 10);
   szTmp = comcr.commFormat("3z,3z.2z"   , maxCntRate);
   buf = comcr.insertStr(buf, szTmp+"%"  , 18);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

}
/***********************************************************************/
void printDetail() throws Exception {
   lineCnt++;
   indexCnt++;

   buf = "";
   szTmp = comcr.commFormat("3z.2z"      , hRcrateYear)+"%";
   buf = comcr.insertStr(buf, szTmp, 10);
   szTmp = comcr.commFormat("3z,3z,3z,3z", hFlowCountA);
   buf = comcr.insertStr(buf, szTmp, 30);
   szTmp = comcr.commFormat("3z,3z.2z"   , tDouble);
   buf = comcr.insertStr(buf, szTmp, 60);
   
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

}
/***********************************************************************/
public double  conv_amt_dp2r(double cvt_amt) throws Exception
{
  long   cvtLong   = (long) Math.round(cvt_amt * 100.0 + 0.00001);
  double cvtDouble =  ((double) cvtLong) / 100;
  return cvtDouble;
}
/***********************************************************************/
}
/*
   V-無限 V-御璽 V-白金 V-金卡 V-普卡
   M-世界 M-鈦金 M-鈦商 M-白金 M-金卡 M-普卡
   J-白金 J-金卡 J-普卡 J-晶緻
　      第2碼   C       普      C:普卡
　              G       金      G:金卡
　              P       白金卡  P:白金卡
　              T       晶緻卡/御璽卡/鈦金      S:卓越卡
　              B       商務卡  　
　              E       電子採購卡      　
　              I       無限/世界卡     I:頂級卡
　              O       COMBO 普        　
　              Q       COMBO 金        　
　              R       COMBO 白金卡    　
　              S       COMBO 鈦金/御璽卡       　
　              D       VD      　
*/

