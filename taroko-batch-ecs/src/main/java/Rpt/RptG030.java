/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/06/08 V1.01.01  Lai         program initial                            *
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
public class RptG030 extends AccessDAO {
    private String progname = "每月信用卡利率分佈情形表  112/12/27 V1.01.04";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommDate    commDate = new CommDate();

    int    DEBUG    = 1;

    String prgmId   = "RptG030";
    String rptName  = "每月信用卡實質有效利率分佈表";
    String rptId    = "CRM69C";
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

    String hCardType      = "";
    String hTypeName      = "";
    double hRcrateYear    = 0;
    double FlowCountA     = 0;
    double FlowCountC     = 0;
    double Sub_A          = 0;
    double Sub_C          = 0;
    String tempName       = "";
    String prtTypeName    = "";

/***********************************************************************/
public static void main(String[] args) throws Exception {
   RptG030 proc = new RptG030();
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
void selectPtrRcrateAnal() throws Exception {

   // 動用循環 利息金額-RCRATE_RI_AMTselect c.RCRATE_YEAR,a.curr_code,  
   sqlCmd  = "select a.is_legal_person ";
   sqlCmd += "     , a.rcrate_year     ";
   sqlCmd += "     , a.card_type       ";
   sqlCmd += "     , c.name            ";
   sqlCmd += "     , sum(a.flow_count_a) as flow_count_a ";
   sqlCmd += "     , sum(a.flow_count_c) as flow_count_c ";
   sqlCmd += "  from ptr_card_type c, ptr_rcrate_anal a ";
   sqlCmd += " where a.proc_month      = ? ";
   sqlCmd += "   and a.rpt_id          = ? ";
   sqlCmd += "   and c.card_type       = a.card_type ";
/* sqlCmd += "   and a.is_select       = 'Y' ";  */
   sqlCmd += "   and a.is_disable_c    = 'N' ";
   sqlCmd += "   and a.is_legal_person = 'Y' ";
   sqlCmd += " group by a.is_legal_person, a.card_type, c.name, a.rcrate_year ";
   sqlCmd += " order by a.is_legal_person, a.card_type, c.name, a.rcrate_year ";

   setString(1, hPrevMonth);
   setString(2, "CRM69"  );

   int cursorIndex = openCursor();
   while (fetchTable(cursorIndex)) {

      hTypeName         = getValue("is_legal_person");
      hRcrateYear       = getValueDouble("rcrate_year");
      hCardType         = getValue("card_type");
      hTypeName         = getValue("name")+"("+hCardType+")";
      FlowCountA        = getValueDouble("flow_count_a");
      FlowCountC        = getValueDouble("flow_count_c");

/* test
      if(hTypeName.equals("Y") && (hRcrateYear != 6 && hRcrateYear != 14.6) )  continue;
*/

      totalCnt++;
      if (totalCnt % 10000 == 0 || totalCnt==1) showLogMessage("I","","Process record=["+totalCnt+"]");
if(DEBUG==1) showLogMessage("I","","Read Year=["+hTypeName+"]"+hRcrateYear+","+FlowCountA);

      if (indexCnt == 0) {
          printHeader();
          tempName     = hTypeName;
          prtTypeName  = hTypeName;
      }
      
/*
      if(!tempName.equals(hTypeName))
        {
         printFooter();
        }
*/

      Sub_A   += FlowCountA;
      Sub_C   += FlowCountC;

      if (indexCnt > 50) { //分頁控制
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
   for (int i = 0; i < 120; i++)     buf += "-";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   buf = "";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));

   buf = "卡  別                                        利   率    流 通 卡 卡 數    流 通 卡 戶 數";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "============================================  =======    ==============    ==============";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
}
/***********************************************************************/
void printFooter() {
   buf = "";
   for (int i = 0; i < 120; i++) buf += "-";
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
   buf = "";
   buf = comcr.insertStr(buf, "合  計:"  ,  1);
   szTmp = comcr.commFormat("2z,3z,3z,3z", Sub_C);
   buf = comcr.insertStr(buf, szTmp      , 57);
   szTmp = comcr.commFormat("2z,3z,3z,3z", Sub_A);
   buf = comcr.insertStr(buf, szTmp      , 75);
   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

}
/***********************************************************************/
void printDetail() throws Exception {
   lineCnt++;
   indexCnt++;

   if(totalCnt > 1)
     {
      if(hTypeName.equals(prtTypeName)) prtTypeName = "";
      else                              prtTypeName = hTypeName;
     }

   buf = "";
   buf = comcr.insertStr(buf, prtTypeName, 1);
   szTmp = comcr.commFormat("3z.2z"      , hRcrateYear)+"%";
   buf = comcr.insertStr(buf, szTmp      , 46);
   szTmp = comcr.commFormat("2$,3$,3$,3$", FlowCountC);
   buf = comcr.insertStr(buf, szTmp      , 57);
   szTmp = comcr.commFormat("2$,3$,3$,3$", FlowCountA);
   buf = comcr.insertStr(buf, szTmp      , 75);
   
   prtTypeName = hTypeName;
if(DEBUG==1) showLogMessage("I","","  step 2=["+hTypeName+"]"+prtTypeName);

   lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

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

