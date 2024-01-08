/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/03/02 V1.01.01  Lai         program initial (load && one page)         *
*  112/07/05 V1.01.02  Bo Yang     update by naming rule                      *
*  112/07/17 V1.01.03  Wilson      刪除程式重複執行判斷                       *
*  112/08/04 V1.01.04  lai         modify                                     *
*  112/10/26 V1.01.05  Wilson      增加處理AI501                                 *
*  112/11/08 V1.01.06  Wilson      日期減一天                                                                                                    *
******************************************************************************/
package Crd;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommRoutine;

public class CrdR067 extends AccessDAO {
    private final String PROGNAME = "信用卡當月停卡量彙總表  112/11/08 V1.01.06";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommDate    commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;
    JSONObject     rptData = null;

    int    DEBUG  = 0;
    String hTempUser = "";

    int reportPageLine = 450;
    String prgmId      = "CrdR067";

    String rptIdR1   = "CRM67";
    String rptName1  = "信用卡當月停卡量彙總表";
    int pageCnt1 = 0, lineCnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int totCnt = 0;
    int totCnt1 = 0;

    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";
    String hChiYymmdd =  "";
    String hBegDate =  "";
    String hEndDate =  "";
    String hBegDateBil =  "";
    String hEndDateBil =  "";
    String hLastDay =  "";
    String hLastMonth =  "";

    String cardCardNo = "";
    String cardCurrentCode = "";
    String cardTableFlag = "";
    String cardOppostDate = "";
    String cardGroupCode = "";
    String cardCardType = "";
    String cardBinType = "";
    String cardSupFlag = "";
    String cardComboIndicator = "";
    int issueCnt = 0;
    int oppostCnt = 0;
    int currentCnt = 0;
    int vAllCnt = 0;
    int mAllCnt = 0;
    int jAllCnt = 0;
    String typeCardNote = "";
    String acctCardIndicator = "";
    String grouName = "";
    String idnoBirthday = "";
    String tempGroupCode = "";
    String tempName = "";
    int idxCurr = 0;
    int idxGroup = 0;
    int idxAll = 0;
    int vdCnt1 = 0;
    int vdCnt2 = 0;
    int vdCnt3 = 0;
    int vdCnt4 = 0;
    int vdCnt5 = 0;
    int hceCnt = 0;

    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    String szTmp   = "";
    int arrayX = 200;
    int arrayY = 6;
    String[] allDataH = new String [arrayX];
    int[][] allData = new int [arrayX][arrayY];   
    int gSum1 = 0;
    int gSum2 = 0;
    int gSum3 = 0;
    int gSum4 = 0;
    int gSum5 = 0;
    
    int currentCode5Total = 0;
    int tmpSumField1 = 0;

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
       comc.errExit("Usage : CrdR067 [yyyymmdd] [seq_no] ", "");
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
        hBusiBusinessDate = "";
        if(args[0].length() == 8) {
           hBusiBusinessDate = args[0];
          } else {
           String errMsg = String.format("指定營業日[%s]", args[0]);
           comcr.errRtn(errMsg, "營業日長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
          }
    }
    selectPtrBusinday();
    
    hLastDay = hEndDate;
    
    if (!hBusiBusinessDate.equals(hLastDay)) {
		showLogMessage("E", "", "報表日不為該月最後一天,不執行此程式");
		return 0;
    }
 
    initArray();

    selectCrdCard();

    showLogMessage("I","","Read END="+ totCnt +",v="+ vAllCnt);
    selectCrdCardG();

    showLogMessage("I","","Read END Group="+ totCnt1);

    totCnt = totCnt1 + totCnt;
    if(totCnt > 0)
      {
        writeFile();
        tailFile();
      }
    
    processAi501();

    comcr.insertPtrBatchRpt(lpar1);
//改為線上報表
if(DEBUG==1) {
   String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), prgmId);
   showLogMessage("I", "", " 報表檔=["+filename+"]");
   filename = Normalizer.normalize(filename, Normalizer.Form.NFKD);
   comc.writeReport(filename, lpar1);
}

 
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
public int selectPtrBusinday() throws Exception 
{

   sqlCmd  = "select to_char(add_days(sysdate,-1),'yyyymmdd') as business_date";
   sqlCmd += "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
   }
   if (recordCnt > 0) {
       String dateTmp    = getValue("business_date");
       hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                                                           : hBusiBusinessDate;
   }

   sqlCmd = "select to_char(add_months(to_date(?,'yyyymmdd'),-5),'yyyymm')||'01' h_beg_date_bil ";
   sqlCmd += "    , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date_bil ";
   sqlCmd += "    , to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01' h_beg_date ";
   sqlCmd +="     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
   sqlCmd +="     , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm') h_last_month ";
   sqlCmd += " from dual ";
   setString(1, hBusiBusinessDate);
   setString(2, hBusiBusinessDate);
   setString(3, hBusiBusinessDate);
   setString(4, hBusiBusinessDate);
   setString(5, hBusiBusinessDate);

   recordCnt = selectTable();
   if(recordCnt > 0) {
      hBegDateBil = getValue("h_beg_date_bil");
      hEndDateBil = getValue("h_end_date_bil");
      hBegDate = getValue("h_beg_date");
      hEndDate = getValue("h_end_date");
      hLastMonth = getValue("h_last_month");
     }

   hChiYymmdd = commDate.toTwDate(hBusiBusinessDate);
   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s][%s]" , hBusiBusinessDate
           , hChiYymmdd, hBegDateBil, hEndDateBil, hBegDate, hEndDate, hLastMonth));
   return 0;
}
/***********************************************************************/
public int checkPtrGroupCode() throws Exception 
{
   sqlCmd  = "select group_name ";
   sqlCmd += "  from ptr_group_code   ";
   sqlCmd += " where group_code = ?   ";
   sqlCmd += "   and group_name like '%白金商旅%' ";
   setString(1, cardGroupCode);

   int recordCnt = selectTable();

   return recordCnt;
}
// ************************************************************************
void initArray() throws Exception
{
   for (int i = 0; i < arrayX; i++)
       {
        for (int j = 0; j < arrayY; j++)
            allData[i][j] = 0;
       
        allDataH[i] = "";
        switch (i) 
         {
          case 0 :
            allDataH[i] = "個人卡";
            break;
          case 1 :
            allDataH[i] = "  ＶＩＳＡ無限卡";
            break;
          case 2 :
            allDataH[i] = "  ＶＩＳＡ御璽商旅卡";;
            break;
          case 3 :
            allDataH[i] = "  ＶＩＳＡ御璽卡";;
            break;
          case 4 :
            allDataH[i] = "  ＶＩＳＡ白金卡";
            break;
          case 5 :
            allDataH[i] = "  Ｖ  ＣＯＭＢＯ白金卡";
            break;
          case 6 :
            allDataH[i] = "  ＶＩＳＡ金卡";;
            break;
          case 7 :
            allDataH[i] = "  ＶＩＳＡ普卡";
            break;
          case 8 :
            allDataH[i] = "  ＶＩＳＡ金融卡";;
            break;
          case 9 :
            allDataH[i] = "  小計 :";;
            break;
          case 10:
            allDataH[i] = "  Ｍ／Ｃ世界卡";
            break;
          case 11:
            allDataH[i] = "  Ｍ／Ｃ ＨＣＥ卡";
            break;
          case 12:
            allDataH[i] = "  Ｍ／Ｃ鈦金卡";
            break;
          case 13 :
            allDataH[i] = "  Ｍ／Ｃ白金卡";
            break;
          case 14:
            allDataH[i] = "  Ｍ／Ｃ金卡";
            break;
          case 15:
            allDataH[i] = "  Ｍ／Ｃ普卡";
            break;
          case 16:
            allDataH[i] = "  小計 :";;
            break;
          case 17:
            allDataH[i] = "  ＪＣＢ晶緻";
            break;
          case 18:
            allDataH[i] = "  ＪＣＢ白金卡";
            break;
          case 19:
            allDataH[i] = "  ＪＣＢ金卡";
            break;
          case 20:
            allDataH[i] = "  ＪＣＢ普卡";
            break;
          case 21:
            allDataH[i] = "  小計 :";
            break;
          case 22:
            allDataH[i] = "  個人合計 :";
            break;
          case 23:
            allDataH[i] = "                     "  ;
            break;
          case 24:
            allDataH[i] = "  法人卡";
            break;
         }
       }
}
/***********************************************************************/
void selectCrdCard() throws Exception 
{
        
  sqlCmd  = " select  ";
  sqlCmd += " a.card_no             ,a.current_code        , ";
  sqlCmd += " '1' as table_flag     ,a.sup_flag            , ";
  sqlCmd += " a.group_code          ,a.card_type           , ";
  sqlCmd += " a.bin_type            ,a.combo_indicator     , ";
  sqlCmd += " c.card_note             ";
  sqlCmd += "  from ptr_card_type c, crd_card a ";
  sqlCmd += " where a.oppost_date  between ? and ? ";
  sqlCmd += "   and a.acct_type = '01' ";
  sqlCmd += "   and c.card_type    = a.card_type  ";
  sqlCmd += " order by a.bin_type,c.card_note ";

  setString(1 , hBegDate);
  setString(2 , hEndDate);

 //if(DEBUG==1) showLogMessage("I",""," SQL="+sqlCmd+" T="+h_beg_date+" N="+h_end_date);

  openCursor();

  while (fetchTable()) {
     initRtn();
     totCnt++;

     cardCardNo = getValue("card_no"     );
     cardCurrentCode = getValue("current_code");
     cardTableFlag = getValue("table_flag"  );
     cardGroupCode = getValue("group_code"  );
     cardCardType = getValue("card_type"   );
     cardBinType = getValue("bin_type"    );
     cardSupFlag = getValue("sup_flag "   );
     cardComboIndicator = getValue("combo_indicator");
     typeCardNote = getValue("card_note"   );

     idxCurr = Integer.parseInt(cardCurrentCode);
 if(DEBUG==1) showLogMessage("I","","Read card="+ cardCardNo +" T="+ cardBinType +" N="+ typeCardNote +" I="+ cardTableFlag +" Cnt="+ totCnt);

     if(totCnt % 5000 == 0 || totCnt == 1)
        showLogMessage("I","",String.format("R067 Process 1 record=[%d]\n", totCnt));


     if(totCnt == 1 )   headFile(); 

     switch (cardBinType)
       {
        case "V": visaRtn();   vAllCnt++;   break;
        case "M": masterRtn(); mAllCnt++;   break;
        case "J": jcbRtn();    jAllCnt++;   break;
       }
    }

  closeCursor();
}
/***********************************************************************/
void selectCrdCardG() throws Exception 
{
        
  sqlCmd  = " select  ";
  sqlCmd += " a.card_no             ,a.current_code        , ";
  sqlCmd += " b.name                ,a.sup_flag            , ";
  sqlCmd += " a.group_code          ,a.card_type           , ";
  sqlCmd += " a.bin_type            ,a.combo_indicator     , ";
  sqlCmd += " a.oppost_date         ,c.card_note          ";
  sqlCmd += "  from ptr_card_type c, ptr_group_card b, crd_card a ";
  sqlCmd += " where 1=1                            ";
  sqlCmd += "   and b.group_code     = a.group_code ";
  sqlCmd += "   and c.card_type      = a.card_type  ";
  sqlCmd += "        and (a.group_code,acct_type) in (select distinct h.group_code , h.acct_type ";
  sqlCmd +=                         "  from ptr_acct_type d, crd_card h     ";
  sqlCmd +=                         " where d.card_indicator = '2'          ";
  sqlCmd +=                         "   and d.acct_type      = h.acct_type) ";
  sqlCmd += " order by a.group_code ";

  openCursor();

  while (fetchTable()) {
     initRtn();
     totCnt1++;

     cardCardNo = getValue("card_no"     );
     cardCurrentCode = getValue("current_code");
     cardOppostDate = getValue("oppost_date" );
     cardGroupCode = getValue("group_code"  );
     cardCardType  = getValue("card_type"   );
     cardBinType   = getValue("bin_type"    );
     cardSupFlag   = getValue("sup_flag "   );
     cardComboIndicator = getValue("combo_indicator");
     grouName      = getValue("name"        );
     typeCardNote  = getValue("card_note"   );
     acctCardIndicator = getValue("card_indicator");

     idxCurr = Integer.parseInt(cardCurrentCode);
 if(DEBUG==1) showLogMessage("I","","Read card2="+ cardCardNo +" B="+ cardBinType +" G="+ cardGroupCode +" Curr="+ idxCurr +" Cnt="+ totCnt1);

     if(totCnt1 % 10000 == 0 || totCnt1 == 1)
        showLogMessage("I","",String.format("R067 Process 1 record=[%d]\n", totCnt));

      if((totCnt1 == 1) || tempGroupCode.compareTo(cardGroupCode) != 0)
       {
        tempGroupCode = cardGroupCode;
        tempName = grouName;
        idxGroup++;
        idxAll = 24 + idxGroup;
        allDataH[idxAll] = String.format("  %s %s", comc.fixLeft(tempName, 40), tempGroupCode);
       }
     if(cardCurrentCode.compareTo("0") ==0 )       continue;
     if(cardOppostDate.compareTo(hBegDate) < 0 &&
        cardOppostDate.compareTo(hEndDate) > 0)    continue;

     switch (idxCurr)
       {
        case 1: allData[idxAll][1]++; gSum1++;
                break;
        case 2: allData[idxAll][2]++; gSum2++;
                break;
        case 3: allData[idxAll][3]++; gSum3++;
                break;
        case 4: allData[idxAll][4]++; gSum4++;
                break;
        case 5: allData[idxAll][5]++; gSum5++;
                break;
       }
if(DEBUG==1) showLogMessage("I","","  WRITE Goverment ="+ idxAll +" data="+allData[idxAll][1]+","+allData[idxAll][2]+",sum1="+ gSum1+","+gSum2+","+gSum3+","+gSum4+","+gSum5);
     }

  closeCursor();
}
/***********************************************************************/
int selectHceCard() throws Exception 
{

  hceCnt = 0;
  extendField = "hcec.";
  sqlCmd  = "select ";
  sqlCmd += " count(*) as hceCnt  ";
  sqlCmd += " from hce_card a     ";
  sqlCmd += " where a.card_no = ? ";

  setString(1 , cardCardNo);

  int recCnt = selectTable();
  hceCnt = getValueInt("hcec.hceCnt");

if(DEBUG==1) showLogMessage("I","","  HCE  cnt="+hceCnt);
  return  hceCnt;
}
/***********************************************************************/
int selectDbcCard() throws Exception 
{

  extendField = "dcrd.";
  sqlCmd  = "select ";
  sqlCmd += " sum(decode(current_code , '1' , 1 , 0)) as c1 ";
  sqlCmd += ",sum(decode(current_code , '2' , 1 , 0)) as c2 ";
  sqlCmd += ",sum(decode(current_code , '3' , 1 , 0)) as c3 ";
  sqlCmd += ",sum(decode(current_code , '4' , 1 , 0)) as c4 ";
  sqlCmd += ",sum(decode(current_code , '5' , 1 , 0)) as c5 ";
  sqlCmd += " from dbc_card a ";
  sqlCmd += " where a.oppost_date  between ? and ? ";

  setString(1 , hBegDate);
  setString(2 , hEndDate);

  int recCnt = selectTable();
  vdCnt1 = getValueInt("dcrd.c1");
  vdCnt2 = getValueInt("dcrd.c2");
  vdCnt3 = getValueInt("dcrd.c3");
  vdCnt4 = getValueInt("dcrd.c4");
  vdCnt5 = getValueInt("dcrd.c5");

if(DEBUG==1) showLogMessage("I","","  DBC  cnt="+getValueInt("dcrd.dbc_cnt"));
  return  getValueInt("dcrd.dbc_cnt");
}
/***********************************************************************/
void visaRtn() throws Exception
{
 //  all_data[9][i]++;      // 小計
 //  all_data[22][i]++;     // 合計

if(DEBUG==1) showLogMessage("I","","  visa="+ cardComboIndicator +" note="+ typeCardNote +",I="+ idxCurr + " ALL="+ idxAll);


   switch (typeCardNote)
     {
      case "I":
               allData[1][idxCurr]++; allData[9][idxCurr]++; allData[22][idxCurr]++;
               break;
      case "S":
               if(cardGroupCode.equals("1610")){
            	   allData[2][idxCurr]++; allData[9][idxCurr]++; allData[22][idxCurr]++;
               }                  
               else{
            	   allData[3][idxCurr]++; allData[9][idxCurr]++; allData[22][idxCurr]++;
               }               
               break;
      case "P":
    	       if(!cardComboIndicator.equals("Y")) {
    	    	   allData[4][idxCurr]++; allData[9][idxCurr]++; allData[22][idxCurr]++;
    	       }
    	       else {
    	    	   allData[5][idxCurr]++; allData[9][idxCurr]++; allData[22][idxCurr]++;
    	       }               
               break;
      case "G":
               allData[6][idxCurr]++; allData[9][idxCurr]++; allData[22][idxCurr]++;
               break;
      case "C":
               allData[7][idxCurr]++; allData[9][idxCurr]++; allData[22][idxCurr]++;
               break;
     }
// VD 在 writeRtn
if(DEBUG==1) showLogMessage("I","","  visa end="+ acctCardIndicator +",data="+ allData[idxAll][1]+" ,"+ " IDX="+ idxAll);
}
/***********************************************************************/
void masterRtn() throws Exception
{
// all_data[16][2]++;
// all_data[22][2]++;

// int group_cnt =  check_ptr_group_code();   // 白金商旅
if(DEBUG==1) showLogMessage("I","","  m/cd="+ cardComboIndicator +" note="+ typeCardNote +","+ cardTableFlag +",I="+ idxCurr);

   selectHceCard();
   if(hceCnt > 0) allData[11][idxCurr]++; allData[16][idxCurr]++; allData[22][idxCurr]++;

   switch (typeCardNote)
     {
      // 11:  HCE
      case "I":
               allData[10][idxCurr]++; allData[16][idxCurr]++; allData[22][idxCurr]++;
               break;
      case "S":
               allData[12][idxCurr]++; allData[16][idxCurr]++; allData[22][idxCurr]++;
               break;
      case "P":
               allData[13][idxCurr]++; allData[16][idxCurr]++; allData[22][idxCurr]++;
               break;
      case "G":
               allData[14][idxCurr]++; allData[16][idxCurr]++; allData[22][idxCurr]++;
               break;
      case "C":
               allData[15][idxCurr]++; allData[16][idxCurr]++; allData[22][idxCurr]++;
               break;
     }
}
/**********************************************************************/
void jcbRtn() throws Exception
{

   switch (typeCardNote)
     {
      case "S":
               allData[17][idxCurr]++; allData[21][idxCurr]++; allData[22][idxCurr]++;
               break;
      case "P":
               allData[18][idxCurr]++; allData[21][idxCurr]++; allData[22][idxCurr]++;
               break;
      case "G":
               allData[19][idxCurr]++; allData[21][idxCurr]++; allData[22][idxCurr]++;
               break;
      case "C":
               allData[20][idxCurr]++; allData[21][idxCurr]++; allData[22][idxCurr]++;
               break;
    }
}
/***********************************************************************/
void initRtn() throws Exception 
{
     cardCardNo = "";
     cardCurrentCode = "";
     cardTableFlag = "";
     cardOppostDate = "";
     cardGroupCode = "";
     cardCardType = "";
     cardBinType = "";
     cardSupFlag = "";
     cardComboIndicator = "";
     typeCardNote = "";
     acctCardIndicator = "";
     grouName = "";
     idnoBirthday = "";
}
/***********************************************************************/
void headFile() throws Exception 
{
        String temp = "";

        pageCnt1++;
        if(pageCnt1 > 1)
           lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));

        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "    + "3144 信月卡部"          ,  1);
        buf = comcr.insertStr(buf, ""              + rptName1                 , 50);
        buf = comcr.insertStr(buf, "保存年限: 五年"                           ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                       hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRM67     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp                         ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));

        buf = "";
        buf = "                                          停用碼－１ 停用碼－２ 停用碼－３ 停用碼－４ 停用碼－５";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "========================================= ========== ========== ========== ========== ==========";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = 6;
}
/***********************************************************************/
 void tailFile() throws UnsupportedEncodingException 
{
   buf = "";
   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
   buf = "";
   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

     szTmp            = "總計 :個人卡+法人卡 ";
     data.name        = szTmp;

     szTmp = comcr.commFormat("2z,3z,3z", gSum1 + allData[22][1]);
     data.data01      = szTmp;
     szTmp = comcr.commFormat("2z,3z,3z", gSum2 + allData[22][2]);
     data.data02      = szTmp;
     szTmp = comcr.commFormat("2z,3z,3z", gSum3 + allData[22][3]);
     data.data03      = szTmp;
     szTmp = comcr.commFormat("2z,3z,3z", gSum4 + allData[22][4]);
     data.data04      = szTmp;
     szTmp = comcr.commFormat("2z,3z,3z", gSum5 + allData[22][5]);
     data.data05      = szTmp;

     buf = data.allText();
     lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

   buf = "";
   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
   buf = "";
   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

//   htail.fileValue = "備 註: １、當月停卡張數:係指控管碼日期為當月之卡片，即指新增停卡部分";
//   buf = htail.allText();
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
//
//   htail.fileValue = "     : ２、個人卡為ＯＲＧ１０６，不含ＴＹＰＥ為５９９、９９７、９９８之卡片";
//   buf = htail.allText();
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
//
//   htail.fileValue = "       ３、控管碼 １:一般停用 ２:掛失 ３:強停 ４:其他 ５:偽卡";
//   buf = htail.allText();
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

}
/***********************************************************************/
void writeTl2() throws Exception 
{
     szTmp            = "  法人卡合計 : ";
     data.name        = szTmp;

     szTmp = comcr.commFormat("2z,3z,3z", gSum1);
     data.data01      = szTmp;
     szTmp = comcr.commFormat("2z,3z,3z", gSum2);
     data.data02      = szTmp;
     szTmp = comcr.commFormat("2z,3z,3z", gSum3);
     data.data03      = szTmp;
     szTmp = comcr.commFormat("2z,3z,3z", gSum4);
     data.data04      = szTmp;
     szTmp = comcr.commFormat("2z,3z,3z", gSum5);
     data.data05      = szTmp;

     buf = data.allText();
     lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
}
/***********************************************************************/
void writeFile() throws Exception 
{
     String tmp   = "";
     String szTmp = "";

     if(lineCnt1 > reportPageLine) {
        headFile();
       }
if(DEBUG==1) showLogMessage("I",""," write="+ allData[5][1]+","+ allData[5][2]+","+ allData[5][3]);

     int n = selectDbcCard();
     allData[8][1] = vdCnt1; allData[9][1]= allData[9][1]+ vdCnt1; allData[22][1]= allData[22][1]+ vdCnt1;
     allData[8][2] = vdCnt2; allData[9][2]= allData[9][2]+ vdCnt2; allData[22][2]= allData[22][2]+ vdCnt2;
     allData[8][3] = vdCnt3; allData[9][3]= allData[9][3]+ vdCnt3; allData[22][3]= allData[22][3]+ vdCnt3;
     allData[8][4] = vdCnt4; allData[9][4]= allData[9][4]+ vdCnt4; allData[22][4]= allData[22][4]+ vdCnt4;
     allData[8][5] = vdCnt5; allData[9][5]= allData[9][5]+ vdCnt5; allData[22][5]= allData[22][5]+ vdCnt5;

if(DEBUG==1) showLogMessage("I","","  WRITE ALL cnt ="+ idxAll +",Group="+ idxGroup);

     for (int i = 0; i < idxAll +1; i++)
       {

        szTmp            = allDataH[i];
        data.name        = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][1]);
        data.data01      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][2]);
        data.data02      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][3]);
        data.data03      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][4]);
        data.data04      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][5]);
        data.data05      = szTmp;

        buf = data.allText();
        if(i ==  0)    buf = "個人卡";
        if(i == 23)    buf = "";
        if(i == 24)    buf = "法人卡";

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = lineCnt1 + 1;
       }

     writeTl2();

     return;
}
/************************************************************************/
void processAi501() throws Exception {

	selectMisReportData();
	
	rptData.put("THIS_MONTH_CURRENT_CODE5_CNT", allData[22][5] + gSum5);
	
	currentCode5Total =  tmpSumField1 + allData[22][5] + gSum5 ;
	
	insertMisReportData();
}

/***********************************************************************/
void selectMisReportData() throws Exception {
        sqlCmd = "select sum_field1 ";
        sqlCmd += " from mis_report_data ";
        sqlCmd += " where data_month = ?   ";
        sqlCmd += "   and data_from = 'CRM67' ";
        setString(1, hLastMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tmpSumField1 = getValueInt("sum_field1");
        }
}

/***********************************************************************/
void insertMisReportData() throws Exception {
	
	//重跑時要先刪除上一次產生的資料
	deleteExistRptRecord();
	commitDataBase();
	
    setValue("DATA_MONTH", comc.getSubString(hBusiBusinessDate,0,6));
    setValue("DATA_FROM", "CRM67");
    setValue("DATA_DATE", hBusiBusinessDate);
    setValueDouble("SUM_FIELD1", currentCode5Total);
    setValueDouble("SUM_FIELD2", 0);
    setValueDouble("SUM_FIELD3", 0);
    setValueDouble("SUM_FIELD4", 0);
    setValueDouble("SUM_FIELD5", 0);
    setValue("DATA_CONTENT", rptData.toString());
    setValue("MOD_TIME", sysDate+sysTime);
    setValue("MOD_PGM", javaProgram);

    daoTable = "mis_report_data";
    insertTable();    
}

/************************************************************************/
void deleteExistRptRecord() throws Exception {
	
	daoTable  = " mis_report_data ";
	whereStr  = " where 1=1 "; 
	whereStr += " and data_month = ? ";
	whereStr += " and data_from = 'CRM67' ";
	
	setString(1, comc.getSubString(hBusiBusinessDate,0,6));

	deleteTable();
	
}

/************************************************************************/
public static void main(String[] args) throws Exception 
{
       CrdR067 proc = new CrdR067();
       int  retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/************************************************************************/
  class buft 
    {
        String fileValue;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(fileValue,140);
//          rtn += fixLeft(len, 1);
            return rtn;
        }

        
    }
  class buf1 
    {
        String name;
        String data01;
        String data02;
        String data03;
        String data04;
        String data05;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";

            rtn += fixLeft(name         , 40+1);
            rtn += fixLeft(data01       ,  10+1);
            rtn += fixLeft(data02       ,  10+1);
            rtn += fixLeft(data03       ,  10+1);
            rtn += fixLeft(data04       ,  10+1);
            rtn += fixLeft(data05       ,  10+1);
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
