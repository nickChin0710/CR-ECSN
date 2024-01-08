/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/08/07 V1.01.07  Lai         program initial                            *
*  112/09/02 V1.01.08  Kirin       change EmplId                              *
*  112/11/24 V1.01.09  Kirin       每月2日執行 專案代碼=2023010001                  *
******************************************************************************/
package Mkt;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import java.sql.Connection;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommDate;

public class MktRF622A extends AccessDAO {
    private String PROGNAME = "員工推展┌ VISA無限金鑽卡 ┘獎勵統計表  112/08/07 V1.01.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;
    CommDate      comDate= new CommDate();

    int    DEBUG    = 0;
    int    DEBUG_F  = 0;
    String prgmId    = "MktRF622A";
    String hTempUser = "";

    int    Report_Page_Line = 45;
    String PgmCd     = "2023010002";

    String rptIdR1   = "CRF622A";
    String rptName1  = "員工推展┌ VISA無限金鑽卡 ┘獎勵統計表";
    int    pageCnt1  = 0, lineCnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int    totCnt = 0;

    String hBusiBusinessDate = "";
    String hCallBatchSeqno   = "";
    String h_chi_yymmdd      = "";
    String hBegDateCur       = "";
    String hEndDateCur       = "";
    String hBegDateBil       = "";
    String hEndDateBil       = "";
    String ApplyDateS        = "";
    String ApplyDateE        = "";

    String EmplId               = "";
    String EmplEmployNo         = "";
    String EmplIdPSeqno         = "";
    String CardCardNo           = "";
    String CardAcnoPSeqno       = "";
    String CardIdPSeqno         = "";
    String CardCurrentCode      = "";
    String CardIssueDate        = "";
    String CardIssueDatePrev    = "";
    String EmplUNitNo           = "";
    String EmplBrnChiName       = "";
    String EmplChiName          = "";
    int    BrnCnt               = 0;
    int    BrnApplyCnt          = 0;
    int    BrnNoApplyCnt        = 0;
    int    BrnEffcCnt           = 0;
    int    BrnNoConsumeCnt      = 0;
    int    chkCnt               = 0;
    int    bilCnt               = 0;

    int    All_1                = 0;
    int    All_2                = 0;
    int    All_3                = 0;
    int    All_0                = 0;
    int    sumAll_1             = 0;
    int    sumAll_2             = 0;
    int    sumAll_3             = 0;
    int    sumAll_4             = 0;
    String tempBrn              = "";
    String tempName             = "";
    int    load_bil_cnt         = 0;

    int    array_x              = 10000;
    int    array_s              = 11;
    int    array_i              = 11;

    String[][] allData_str = new String [array_x][array_s];
    int[][]    allData_int = new int [array_x][array_i];

    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    String tmpstr1 = "";

    buft htail = new buft();
    buf1 data  = new buf1();

    private int fptr1  = -1;
    String  filename_o = "";
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
    if(args.length > 3 ) {
       comc.errExit("Usage : MktRF622A [PROGRAM_CODE] [yyyymmdd] [seq_no] ", "");
      }
/*
    if(comm.isAppActive(javaProgram))
       comc.errExit("Error!! Someone is running this program now!! =["+javaProgram+"]" , "Please wait a moment to run again!!");
*/
 
    comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
    comr  = new CommRoutine(getDBconnect()   , getDBalias());
 
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
    hTempUser = "";
    if (comcr.hCallBatchSeqno.length() == 20) {
        comcr.callbatch(0, 0, 1);
        selectSQL = " user_id ";
        daoTable = "ptr_callbatch";
        whereStr = "WHERE batch_seqno   = ?  ";

        setString(1, comcr.hCallBatchSeqno);
        int recCnt = selectTable();
        hTempUser = getValue("user_id");
    }
    if (hTempUser.length() == 0) {
        hTempUser = comc.commGetUserID();
    }

    if (args.length >  0) {
        PgmCd = args[0];
    }
    if (args.length == 2 && args[1].length() == 8) {
    	hBusiBusinessDate = args[1];
    } 
    
    checkOpen();

    selectPtrBusinday();
	if (!"02".equals(comc.getSubString(hBusiBusinessDate,6))) {
		showLogMessage("I", "", "每月2日執行, 本日非執行日!!");
		return 0;
	}
    selectMktIntrFund();
 
    loadBilBill();

    initAllArray();

    selectCrdEmployee();
 
    if(totCnt > 0)
      {
        tempBrn  = EmplUNitNo;
        tempName = EmplBrnChiName;
        writeTail(2);
        finalTail();
      }

    String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), rptIdR1);
    filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
    comc.writeReport(filename, lpar1);
 
    closeOutputText(fptr1);
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
void  initAllArray() throws Exception
{
   BrnCnt           = 0;
   BrnApplyCnt      = 0;
   BrnNoApplyCnt    = 0;
   BrnEffcCnt       = 0;
   BrnNoConsumeCnt  = 0;
   sumAll_1  = 0;
   sumAll_2  = 0;
   sumAll_3  = 0;
   sumAll_4  = 0;
   for (int i = 0; i < array_x; i++)
       {
        for (int j = 0; j < array_s; j++)
            allData_str[i][j] = "";
        for (int k = 0; k < array_i; k++)
            allData_int[i][k] = 0;
       }
}
// ************************************************************************
void  loadBilBill() throws Exception
{
  extendField = "bill.";
  daoTable    = "bil_bill";
  selectSQL   = "card_no,       "
              + "id_p_seqno,    "
              + "acct_month,    "
              + "purchase_date, "
              + " case when a.txn_code in ('06','25','27','28','29') "
              + "      then a.dest_amt*-1 else a.dest_amt end  dest_amt ";
  daoTable    = "bil_bill a ";
  whereStr    = "where acct_month between ? and ? "
              + "  and acct_code  in ('BL','CA','IT','ID','AO','OT') "
              + "  and card_no in ( select card_no from   crd_card a , crd_idno b "
              +                    " where a.id_p_seqno  = b.id_p_seqno  "
              +                    "   and a.clerk_id   <> '') "
              + " order by id_p_seqno ";

  setString(1 , hBegDateBil.substring( 0, 6));
  setString(2 , hEndDateBil.substring( 0, 6));

  int  n = loadTable();
//setLoadData("bill.card_no");        // set key
  setLoadData("bill.id_p_seqno");     // set key

//int lind = getLoadIndex();
/* lai test
  for (int k = 0; k < n; k++) {
      showLogMessage("I","","   Load bil_bill card_no:["+n+"]"+getList("bill.card_no" ,  k)+","+getList("bill.id_p_seqno" ,  k)+","+getList("bill.acct_month" ,  k)+","+getListDouble("bill.dest_amt" ,  k));
    }
*/

  showLogMessage("I","","Load bil_bill end Count: ["+n+"]"+hBegDateBil.substring( 0, 6)+","+hEndDateBil.substring( 0, 6));
}
// ************************************************************************
void  chkBillMonth() throws Exception
{
  String bill_purchase_date = "";
  String bill_acct_month    = "";
  int    bill_dest_amt      = 0;

  bilCnt = 0;
  for (int g = 0; g < load_bil_cnt; g++) {
        bill_purchase_date = getValue("bill.purchase_date" ,  g);
        bill_acct_month    = getValue("bill.acct_month"    ,  g);
        bill_dest_amt      = (int) getValueDouble("bill.dest_amt" ,  g);
//  全新戶有效卡:6個月內沒有任一張有效卡       非全新戶有效卡:有任一張有效卡
//  有效卡是半年內有消費
        if(bill_acct_month.compareTo(CardIssueDatePrev.substring(0, 6)) >= 0 &&
           bill_acct_month.compareTo(CardIssueDate.substring(0, 6))     <= 0)
          {
           bilCnt++;
          }
if(DEBUG_F==1) showLogMessage("I","","    888 bill="+bill_acct_month+","+CardIssueDate+","+bill_dest_amt+","+bill_purchase_date+",消費="+bilCnt+","+getValue("bill.acct_month"    ,  g)+",G="+g); 
    }
}
/***********************************************************************/
public void selectMktIntrFund() throws Exception 
{
   sqlCmd  = "select apply_date_s ";
   sqlCmd += "     , apply_date_e ";
   sqlCmd += " from mkt_intr_fund ";
   sqlCmd += "where program_code = ? ";
   sqlCmd += "  and apr_flag     = 'Y' ";
   setString(1, PgmCd);

   int recordCnt = selectTable();
   if(recordCnt > 0) {
      ApplyDateS = getValue("apply_date_s");
      ApplyDateE = getValue("apply_date_e");
     }
   if (notFound.equals("Y")) {
       comcr.errRtn("select mkt_intr_fund not found!", PgmCd , hCallBatchSeqno);
   }
   showLogMessage("I", "", String.format("專案代碼=[%s][%s][%s]",PgmCd,ApplyDateS,ApplyDateE));
//   hBegDateBil = comDate.dateAdd(ApplyDateS , 0,-6, 0).substring(0,6) + "01";
//   hEndDateBil = ApplyDateE;
   hBegDateBil = comDate.dateAdd(hBusiBusinessDate , 0,-6, 0).substring(0,6) + "01";
   hEndDateBil = comDate.dateAdd(hBusiBusinessDate , 0,-1, 0).substring(0,6) + "31";
   
   showLogMessage("I", "", String.format("    帳單最大區間[%s][%s]",hBegDateBil,hEndDateBil));
}
/***********************************************************************/
void checkOpen() throws Exception 
{
   filename_o = String.format("%s/media/mkt/%s.txt",comc.getECSHOME(), prgmId);
   filename_o = Normalizer.normalize(filename_o, java.text.Normalizer.Form.NFKD);
   comc.mkdirsFromFilenameWithPath(filename_o);
   fptr1 = openOutputText(filename_o, "MS950");
   showLogMessage("I", "", String.format("Open file=[%s]",filename_o));
   if(fptr1 == -1) {
      comc.errExit("在程式執行目錄下沒有權限讀寫", filename_o);
     }
}
/***********************************************************************/
public int  selectPtrBusinday() throws Exception 
{

   sqlCmd  = "select to_char(sysdate,'yyyymmdd') as business_date";
   sqlCmd += "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
   }
   if (recordCnt > 0) {
       hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                            : hBusiBusinessDate;
   }

   sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymm')||'01' hBegDateBil ";
   sqlCmd += "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') hEndDateBil ";
   sqlCmd += "     , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm')||'01' h_beg_date ";
   sqlCmd += "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date ";
   sqlCmd += " from dual ";
   setString(1, hBusiBusinessDate);
   setString(2, hBusiBusinessDate);
   setString(3, hBusiBusinessDate);
   setString(4, hBusiBusinessDate);

   recordCnt = selectTable();
   if(recordCnt > 0) {
      hEndDateCur = getValue("h_end_date");
      hBegDateCur = hEndDateCur.substring(0, 4) + "0101";
      hBegDateBil = getValue("hBegDateBil");
      hEndDateBil = getValue("hEndDateBil");
     }

   h_chi_yymmdd         = getValue("h_chi_yymmdd");
   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]" , hBusiBusinessDate , h_chi_yymmdd, hBegDateCur, hEndDateCur , hBegDateBil, hEndDateBil));

   return 0;
}
/***********************************************************************/
void chkCrdCard() throws Exception 
{
  sqlCmd  = " select count(*) as chkCnt  ";
  sqlCmd += "   from crd_card a  ";
  sqlCmd += "  where a.id_p_seqno = ? ";
  sqlCmd += "    and a.card_no   != ? ";
  sqlCmd += "    and a.issue_date between ? and ? ";

  setString(1, EmplIdPSeqno);
  setString(2, CardCardNo);
  setString(3, CardIssueDate);
  setString(4, CardIssueDatePrev);

  int recordCnt = selectTable();

  chkCnt = getValueInt("chkCnt");

}
/***********************************************************************/
void selectCrdCard() throws Exception 
{

  sqlCmd  = " select ";
  sqlCmd += "  c.card_no             ,c.id_p_seqno        , ";
  sqlCmd += "  c.acno_p_seqno        ,c.issue_date         ";
  sqlCmd += "   from crd_card c  ";
  sqlCmd += "  where c.clerk_id     = ? ";
  sqlCmd += "    and c.issue_date   between ? and ?  ";
/* lai test
*/
  sqlCmd += "    and c.group_code   in ('1622') ";
  sqlCmd += "    and c.current_code = '0' ";
  sqlCmd += "  order by c.issue_date desc ";

//  setString(1, EmplEmployNo);
  setString(1, EmplId);
  setString(2, ApplyDateS);
  setString(3, ApplyDateE);

  int recordCnt = selectTable();
//if(DEBUG_F==1) showLogMessage("I",""," clerk="+EmplEmployNo+" ,"+recordCnt);
  if(DEBUG_F==1) showLogMessage("I",""," clerk="+EmplId+" ,"+recordCnt);

  for (int k = 0; k < recordCnt; k++) {
       CardCardNo          = getValue("card_no"      , k);
       CardAcnoPSeqno      = getValue("acno_p_seqno" , k);
       CardIdPSeqno        = getValue("id_p_seqno"   , k);
       CardCurrentCode     = getValue("current_code" , k);
       CardIssueDate       = getValue("issue_date"   , k);
       CardIssueDatePrev = comDate.dateAdd(CardIssueDate, 0,-6, 0).substring(0,6) + "01"; //6個月內

       chkCrdCard();

       load_bil_cnt = 0;
       setValue("bill.id_p_seqno"  , CardIdPSeqno);
       load_bil_cnt = getLoadData("bill.id_p_seqno");
if(DEBUG_F==1) showLogMessage("I","","   888 bill cnt="+CardIdPSeqno+","+load_bil_cnt);
       chkBillMonth();

if(DEBUG_F==1) showLogMessage("I","","  Card="+CardCardNo+" ,"+CardIdPSeqno+" , idx="+k+",C="+chkCnt+",B="+bilCnt);
//  全新戶有效卡:6個月內沒有任一張有效卡       非全新戶有效卡:有任一張有效卡
//  有效卡是半年內有消費
       if(chkCnt < 1 || bilCnt < 1)  allData_int[BrnCnt][1]++;  
       if(chkCnt > 0 && bilCnt > 0)  allData_int[BrnCnt][2]++;
    }

}
/***********************************************************************/
void selectCrdEmployee() throws Exception 
{
  fetchExtend = "main.";
  sqlCmd  = "select ";
  sqlCmd += "  a.unit_no             ,a.employ_no         , a.id  , ";
  sqlCmd += "  b.id_p_seqno          ,b.chi_name          , ";
  sqlCmd += "  c.brief_chi_name                             ";
  sqlCmd += "  from gen_brn c,  crd_employee a ";
  sqlCmd += "  full outer join crd_idno b on b.id_no  = a.id ";  //  and b.id_no_code  = a.id_code ";
  sqlCmd += " where c.branch       = a.unit_no ";
  sqlCmd += "   and a.employ_no   <> ''        ";
  sqlCmd += " order by a.unit_no ,b.id_p_seqno ";
  
  openCursor();

  while (fetchTable()) {
     initRtn();
     totCnt++;

     EmplId              = getValue("main.id"          );
     EmplEmployNo        = getValue("main.employ_no"   );
     EmplIdPSeqno        = getValue("main.id_p_seqno"  );
     EmplUNitNo          = getValue("main.unit_no"     );
     EmplChiName         = getValue("main.chi_name"); EmplBrnChiName      = getValue("main.brief_chi_name");
if(DEBUG==1) showLogMessage("I","","Read brn="+EmplUNitNo+",TMP="+tempBrn+",ID="+EmplId+","+EmplIdPSeqno+",C="+EmplChiName+",Cnt="+totCnt);

     if(totCnt % 1000 == 0 || totCnt == 1)
        showLogMessage("I","",String.format("R81A Process 1 record=[%d]\n", totCnt));

     if(totCnt == 1)   
       {
        tempBrn        = EmplUNitNo;
        tempName       = EmplBrnChiName;
        writeRptHead(0);
        writeHead(0);
       }
     if(tempBrn.compareTo(EmplUNitNo) != 0) 
       {
        writeTail(1);

        initAllArray();

        tempBrn        = EmplUNitNo;
        tempName       = EmplBrnChiName;
       }


     BrnCnt++;

     allData_str[BrnCnt][0] = EmplId;                          // 姓名
     allData_str[BrnCnt][1] = comc.fixLeft(EmplChiName, 20);   // 姓名

     selectCrdCard();

     allData_int[BrnCnt][3] =  (int) (allData_int[BrnCnt][1]/3) * 5 + allData_int[BrnCnt][2];
if(DEBUG==1) showLogMessage("I","","   Dtl="+BrnCnt+","+allData_int[BrnCnt][1]+","+allData_int[BrnCnt][2]+","+allData_int[BrnCnt][3]);
    }

  showLogMessage("I",""," Read end="+totCnt);

}
/***********************************************************************/
void initRtn() throws Exception 
{
     CardCardNo         = "";
     CardCurrentCode    = "";
     CardAcnoPSeqno     = "";
     EmplBrnChiName     = "";
     EmplChiName        = "";
     EmplIdPSeqno       = "";
     EmplUNitNo         = "";
}
/***********************************************************************/
void writeRptHead(int idx) throws Exception 
{
   buf = "";
   String tmpStr1 = "";
   String tmpStr2 = "";

   tmpStr1 = tempBrn;
   tmpStr1 = "3144"; 
   tmpStr2 = h_chi_yymmdd+rptName1;
  
   buf = comc.fixLeft(tmpStr1, 10) + comc.fixLeft(rptIdR1, 16) 
       + comc.fixLeft(tmpStr2, 88) + comc.fixLeft("R", 8);

   writeTextFile(fptr1, buf+"\n");
//if(DEBUG==1) showLogMessage("I","","888 Write Rpt Head =======[" +idx+"]"+buf );
}
/***********************************************************************/
void writeHead(int idx) throws Exception 
{
     String temp = "";

if(DEBUG_F==1) showLogMessage("I","","   Write HEAD=["+idx+"]");
     pageCnt1++;
     if(pageCnt1 > 1)
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));

     buf = "";
     buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"            ,  1);
     buf = comcr.insertStr(buf, ""              + rptName1                   , 50);
     buf = comcr.insertStr(buf, ""              + comc.fixLeft(rptName1,50)  , 45);
     buf = comcr.insertStr(buf, "保存年限: 二年"                          ,100);
     writeFile(idx,buf);

     buf = "";
     tmp = String.format("%3.3s年%2.2s月%2.2s日", h_chi_yymmdd.substring(0, 3),
                 h_chi_yymmdd.substring(3, 5), h_chi_yymmdd.substring(5));
     buf = comcr.insertStr(buf, "報表代號: CRF622A   科目代號:"   ,  1);
     buf = comcr.insertStr(buf, "中華民國 " + tmp                 , 52);
     temp = String.format("%4d", pageCnt1);
     buf = comcr.insertStr(buf, "頁    次:" + temp                ,100);
     writeFile(idx,buf);

     writeFile(idx," ");

     buf = "薪資單位代號 薪資單位名稱           禮券總份數";
     writeFile(idx,buf);

     buf = "============ ==================== ============";
     writeFile(idx,buf);

     lineCnt1 = 6;
}
/***********************************************************************/
void writeFile(int idx ,String buf) throws Exception
{
   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

   if(idx  != 2)
      writeTextFile(fptr1, buf+"\n");
if(DEBUG_F==1) showLogMessage("I","","     Write FILE=["+idx+"],"+buf);
}
/***********************************************************************/
 void writeTail(int idx) throws Exception 
{
  String tmp1     = "";
        
if(DEBUG_F==1) showLogMessage("I","","     Write TAIL=["+idx+"],"+lineCnt1);
     if(lineCnt1 > Report_Page_Line) {
        writeHead(2);
       }
     htail.str01     = String.format("%s"   , tempBrn   );
     htail.str02     = String.format("%s"   , tempName  );
     htail.int01     = comcr.commFormat("3z,3z,3z"   , sumAll_3);
     buf = htail.allText();
     writeFile(1,buf);
     lineCnt1  = lineCnt1 + 1;

//   writeFile(1," ");

     All_0 += BrnCnt;
     All_1 += sumAll_1;
     All_2 += sumAll_2;
     All_3 += sumAll_3;
}
/***********************************************************************/
 void finalTail() throws Exception 
{

     htail.str01       = "    總  計 : ";
     htail.str02       = "                    ";
     htail.int01     = comcr.commFormat("3z,3z,3z"   , All_3);
     buf = htail.allText();
     writeFile(1,buf);

     writeFile(1," ");


     buf = "說 明:";
     writeFile(1,buf);

     buf = " １、活動期間: " + ApplyDateS + " ～ " + ApplyDateE ;
     writeFile(1,buf);

     buf = " ２、符合資格條件:";
     writeFile(1,buf);

     buf = " （１） 全新戶: 每推廣３卡可獲全聯禮券５００元，且須為有效卡．";
     writeFile(1,buf);

     buf = " （２） 非全新戶: 每推廣１卡可獲全聯禮券１００元，且須為有效卡．";
     writeFile(1,buf);

}
/***********************************************************************/
void writeDetail() throws Exception 
{
     String tmp = "";

if(DEBUG_F==1) showLogMessage("I","","   Write Dtl="+BrnCnt+","+tempBrn+","+EmplUNitNo);

     for (int i = 1; i < BrnCnt+1; i++)
       {

        if(lineCnt1 > Report_Page_Line) {
           writeHead(2);
          }

        data = null;
        data = new buf1();

        data.str01   = String.format("%s"   , allData_str[i][0]);
        data.str02   = String.format("%s"   , allData_str[i][1]);
        data.int01   = comcr.commFormat("3z,3z,3z", allData_int[i][1]);
        data.int02   = comcr.commFormat("3z,3z,3z", allData_int[i][2]);
        data.int03   = comcr.commFormat("3z,3z,3z", allData_int[i][3]);

        buf = data.allText();
//      writeFile(1,buf);
//      lineCnt1  = lineCnt1 + 1;

        sumAll_1+= allData_int[i][1];
        sumAll_2+= allData_int[i][2];
        sumAll_3+= allData_int[i][3];
       }

    return;
}
/************************************************************************/
public static void main(String[] args) throws Exception 
{
       MktRF622A proc = new MktRF622A();
       int  retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/************************************************************************/
  class buft 
    {
        String filler01;
        String str01;
        String str02;
        String int01;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(str01        ,  12+1);
            rtn += fixLeft(str02        ,  20+1);
            rtn += fixLeft(int01        ,  16+1);
            return rtn;
        }

        
    }
  class buf1 
    {
        String str01;
        String str02;
        String int01;
        String int02;
        String int03;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(str01        ,  12+1);
            rtn += fixLeft(str02        ,  20+1);
            rtn += fixLeft(int01        ,  16+1);
            rtn += fixLeft(int02        ,  16+1);
            rtn += fixLeft(int03        ,  16+1);
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
