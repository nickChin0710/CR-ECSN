/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/02/22 V1.01.01  Lai         program initial (load && one page)         *
*  112/07/05 V1.01.02  Bo Yang     update by naming rule                      *
*  112/07/12 V1.01.03  Wilson      寫入報表紀錄檔                                                                                            *
*  112/07/17 V1.01.04  Wilson      刪除程式重複執行判斷                                                                                 *
*  112/07/21 V1.01.05  Wilson      指定一般消費科目                                                                                         *
*  112/08/26 V1.01.06  Wilson      調整歸戶數資料讀取邏輯                                                                             * 
*  112/10/26 V1.01.07  Wilson      增加處理AI501                                 *
*  112/11/13 V1.01.08  Wilson      歸戶數改讀crd_idno                            *
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

public class CrdR065 extends AccessDAO {
    private final String PROGNAME = "每月個人信用卡卡量彙總表  112/11/13 V1.01.08";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommDate commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;
    JSONObject     rptData = null;

    int    DEBUG  = 0;
    int loadF = 0;
    String hTempUser = "";

    int reportPageLine = 45;
    String prgmId    = "CrdR065";

    String rptIdR1 = "CRM65";
    String rptName1  = "每月個人信用卡卡量彙總表";
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
    String hFirstDay =  "";
    String h20YearsOldDate =  "";
    String hLastMonth =  "";

    String cardCardNo = "";
    String cardAcnoPSeqno = "";
    String cardPSeqno = "";
    String cardIdPSeqno = "";
    String cardOriIssueDate = "";
    String cardGroupCode = "";
    String cardCardType = "";
    String cardBinType = "";
    String cardSupFlag = "";
    String cardComboIndicator = "";
    String cardCurrentCode = "";
    String cardLastConsumeDate = "";
    int issueCnt = 0;
    int oppostCnt = 0;
    int currentCnt = 0;
    int vAllCnt = 0;
    int mAllCnt = 0;
    int jAllCnt = 0;
    int loadBilCnt = 0;
    int loadCrdCnt = 0;
    String typeCardNote = "";
    String acctCardIndicator = "";
    String idnoStudent = "";
    String idnoBirthday = "";
    String idno20Flag = "";


    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    int arrayX = 43;
    int arrayY = 4;
    String[] allDataH = new String [arrayX];
    int[][] allData = new int [arrayX][arrayY];   
    
    int vCardIssueTotal = 0;
    int mCardIssueTotal = 0;
    int jCardIssueTotal = 0;
    int stuCardAcctTotal = 0;
    int tmpSumField1 = 0;
    int tmpSumField2 = 0;
    int tmpSumField3 = 0;

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
       comc.errExit("Usage : CrdR065 [yyyymmdd] [seq_no] ", "");
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
    
    if (!hBusiBusinessDate.equals(hFirstDay)) {
		showLogMessage("E", "", "今日不為該月第一天,不執行此程式");
		return 0;
    }
 
    initArray();

    selectCrdCard();

    showLogMessage("I","","Read END="+ totCnt +",v="+ vAllCnt);
    showLogMessage("I","","*********");

    selectCrdIdno();
    
    selectDbcIdno();
    
    selectCrdCorp();

    writeFile();
    tailFile();
    
    processAi501();

//改為線上報表 
//    String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), prgmId);
//    filename = Normalizer.normalize(filename, Normalizer.Form.NFKD);
//    comc.writeReport(filename, lpar1);
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
public int selectPtrBusinday() throws Exception 
{

   sqlCmd  = "select to_char(sysdate,'yyyymmdd') as business_date";
   sqlCmd += "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
   }
   if (recordCnt > 0) {
       hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? comcr.increaseDays(getValue("business_date"),1)
                           : hBusiBusinessDate;
   }
   
   hFirstDay = hBusiBusinessDate.substring(0, 6) + "01";

   sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymm')||'01' h_beg_date_bil ";
   sqlCmd += "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date_bil ";
   sqlCmd += "     , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm')||'01' h_beg_date ";
   sqlCmd += "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date ";
   sqlCmd += "     , to_char(add_months(to_date(?,'yyyymmdd'),-240),'yyyymmdd') h_20_years_old_date ";
   sqlCmd +="      , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm') h_last_month ";
   sqlCmd += " from dual ";
   setString(1, hBusiBusinessDate);
   setString(2, hBusiBusinessDate);
   setString(3, hBusiBusinessDate);
   setString(4, hBusiBusinessDate);
   setString(5, hBusiBusinessDate);
   setString(6, hBusiBusinessDate);

   recordCnt = selectTable();
   if(recordCnt > 0) {
      hBegDateBil = getValue("h_beg_date_bil");
      hEndDateBil = getValue("h_end_date_bil");	   
      hBegDate = getValue("h_beg_date");
      hEndDate = getValue("h_end_date");
      h20YearsOldDate = getValue("h_20_years_old_date");
      hLastMonth = getValue("h_last_month");
     }

   hChiYymmdd = commDate.toTwDate(hBusiBusinessDate);
   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s][%s][%s]" , hBusiBusinessDate
           , hChiYymmdd, hBegDateBil, hEndDateBil, hBegDate, hEndDate, h20YearsOldDate, hLastMonth));
   return 0;
}
/***********************************************************************/
void initArray() throws Exception
{
   for (int i = 0; i < arrayX; i++)
       {
        allDataH[i] = "";
        switch (i) 
         {
          case 0+1 :
            allDataH[i] = "ＶＩＳＡ無限卡";
            break;
          case 1+1 :
            allDataH[i] = "ＶＩＳＡ御璽卡";;
            break;
          case 2+1 :
            allDataH[i] = "ＶＩＳＡ白金卡";
            break;
          case 3+1 :
            allDataH[i] = "ＶＩＳＡ金卡";;
            break;
          case 4+1 :
            allDataH[i] = "ＶＩＳＡ普卡";
            break;
          case 5+1 :
            allDataH[i] = "ＶＩＳＡ ＣＯＭＢＯ白金御璽";;
            break;
          case 6+1 :
            allDataH[i] = "ＶＩＳＡ小計 :";;
            break;
          case 7+1 :
            allDataH[i] = "ＭＡＳＴＥＲ世界卡";
            break;
          case 8+1 :
            allDataH[i] = "ＭＡＳＴＥＲ鈦金卡";
            break;
          case 9+1 :
            allDataH[i] = "ＭＡＳＴＥＲ鈦金商務卡";
            break;
          case 10+1 :
            allDataH[i] = "ＭＡＳＴＥＲ一般白金卡";
            break;
          case 11+1 :
            allDataH[i] = "ＭＡＳＴＥＲ個人白金商旅卡";
            break;
          case 12+1 :
            allDataH[i] = "ＭＡＳＴＥＲ一般金卡";
            break;
          case 13+1 :
            allDataH[i] = "ＭＡＳＴＥＲ一般普卡";
            break;
          case 14+1 :
            allDataH[i] = "ＣＯＭＢＯ鈦金卡";
            break;
          case 15+1 :
            allDataH[i] = "ＣＯＭＢＯ一般白金卡";
            break;
          case 16+1 :
            allDataH[i] = "ＣＯＭＢＯ一般金卡";
            break;
          case 17+1 :
            allDataH[i] = "ＣＯＭＢＯ一般普卡";
            break;
          case 18+1 :
            allDataH[i] = "ＭＡＳＴＥＲ ＣＡＲＤ小計 :";;
            break;
          case 19+1 :
            allDataH[i] = "ＪＣＢ白金卡";
            break;
          case 20+1 :
            allDataH[i] = "ＪＣＢ金卡";
            break;
          case 21+1 :
            allDataH[i] = "ＪＣＢ普卡";
            break;
          case 22+1 :
            allDataH[i] = "ＪＣＢ晶緻";
            break;
          case 23+1 :
            allDataH[i] = "ＪＣＢ小計 :";
            break;
          case 25+1 :
            allDataH[i] = "合計   :";
            break;
          case 27+1 :
            allDataH[i] = "歸戶數 :                                流 通 卡    有 效 卡";
            break;
          case 28+1 :
            allDataH[i] = "個人卡正卡歸戶數";
            break;
          case 29+1 :
            allDataH[i] = "個人卡附卡歸戶數";
            break;
          case 30+1 :
            allDataH[i] = "學生卡正卡歸戶數(未滿２０歲)";
            break;
          case 31+1 :
            allDataH[i] = "學生卡正卡歸戶數(２０歲(含)以上)";
            break;
          case 32+1 :
            allDataH[i] = "學生卡附卡歸戶數(未滿２０歲)";
            break;
          case 33+1 :
            allDataH[i] = "學生卡附卡歸戶數(２０歲(含)以上)";
            break;
          case 34+1 :
            allDataH[i] = "＊卡種分類歸戶數 :";
            break;
          case 35+1 :
            allDataH[i] = "歸戶數 :                                流 通 卡    有 效 卡";
            break;
          case 36+1 :
            allDataH[i] = "頂級卡";
            break;
          case 37+1 :
            allDataH[i] = "白金卡(包含鈦金,晶緻,御璽,商旅)";
            break;
          case 38+1 :
            allDataH[i] = "金卡";
            break;
          case 39+1 :
            allDataH[i] = "普卡";
            break;
          case 40+1 :
            allDataH[i] = "ＶＩＳＡ金融卡";
            break;
          case 41+1 :
            allDataH[i] = "法人卡";
            break;
         }
       }
}
/***********************************************************************/
void selectCrdCard() throws Exception {
        
	sqlCmd =  " select a.card_no      ,a.acno_p_seqno        ,a.id_p_seqno        , ";
	sqlCmd += " a.ori_issue_date      ,a.sup_flag            , ";
	sqlCmd += " a.group_code          ,a.card_type           , ";
	sqlCmd += " a.bin_type            ,a.combo_indicator     , ";
	sqlCmd += " a.current_code        ,a.last_consume_date   , ";
	sqlCmd += " b.student             ,b.birthday            , ";
	sqlCmd += " case when ? - to_number(substr(decode(birthday,'','20000000',birthday),1,4)) < 20 then 'N' ";
	sqlCmd += " else 'Y' end as bir_flag , ";
	sqlCmd += " d.card_indicator      ,c.card_note            ";            
	sqlCmd += " from ptr_acct_type d, ptr_card_type c, crd_idno b, crd_card a ";
	sqlCmd += "where b.id_p_seqno   = a.id_p_seqno ";
	sqlCmd += "  and c.card_type    = a.card_type  ";
	sqlCmd += "  and d.acct_type    = a.acct_type  ";
	sqlCmd += "  and a.acct_type = '01' ";
	sqlCmd += "  and a.current_code = '0' ";
	sqlCmd += "  union   ";
	sqlCmd += " select a.card_no      ,a.acno_p_seqno        ,a.id_p_seqno         , ";
	sqlCmd += " a.ori_issue_date      ,a.sup_flag            , ";
	sqlCmd += " a.group_code          ,a.card_type           , ";
	sqlCmd += " a.bin_type            ,a.combo_indicator     , ";
	sqlCmd += " a.current_code        ,a.last_consume_date   , ";
	sqlCmd += " b.student             ,b.birthday            , ";
	sqlCmd += " case when ? - to_number(substr(decode(birthday,'','20000000',birthday),1,4)) < 20 then 'N' ";
	sqlCmd += " else 'Y' end as bir_flag , ";
	sqlCmd += " d.card_indicator      ,c.card_note            ";            
	sqlCmd += " from ptr_acct_type d, ptr_card_type c, crd_idno b, crd_card a ";
	sqlCmd += "where b.id_p_seqno   = a.id_p_seqno ";
	sqlCmd += "  and c.card_type    = a.card_type  ";
	sqlCmd += "  and d.acct_type    = a.acct_type  ";
	sqlCmd += "  and a.acct_type = '01' ";
	sqlCmd += "  and a.ori_issue_date between ? and ?  ";	
	sqlCmd += "order by bin_type,card_note ";
  setInt(1 , Integer.parseInt(sysDate.substring(0, 4)));
  setInt(2 , Integer.parseInt(sysDate.substring(0, 4)));
  setString(3 , hBegDate);
  setString(4 , hEndDate);

  openCursor();

  while (fetchTable()) {
     initRtn();
     totCnt++;

     cardCardNo = getValue("card_no"     );
     cardAcnoPSeqno = getValue("acno_p_seqno");
     cardIdPSeqno = getValue("id_p_seqno");
     cardOriIssueDate = getValue("ori_issue_date"  );
     cardGroupCode = getValue("group_code"  );
     cardCardType = getValue("card_type"   );
     cardBinType = getValue("bin_type"    );
     cardSupFlag = getValue("sup_flag "   );
     cardComboIndicator = getValue("combo_indicator");
     cardCurrentCode = getValue("current_code");
     cardLastConsumeDate = getValue("last_consume_date");
     typeCardNote = getValue("card_note"   );
     acctCardIndicator = getValue("card_indicator");
     idnoStudent = getValue("student"     );
     idnoBirthday = getValue("birthday"    );
     idno20Flag = getValue("bir_flag"    );

 if(DEBUG==1) showLogMessage("I","","Read card="+ cardCardNo +" T="+ cardBinType +" N="+ typeCardNote +" I="+ cardOriIssueDate +" Cnt="+ totCnt);

     if(totCnt % 5000 == 0 || totCnt == 1)
        showLogMessage("I","",String.format("R065 Process 1 record=[%d]\n", totCnt));


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

void visaRtn() throws Exception {
	
	if(cardCurrentCode.equals("0")) {
		   allData[7][2]++;      // 小計
		   allData[26][2]++;     // 合計
	}

if(DEBUG==1) showLogMessage("I","","  visa="+ cardComboIndicator +" note="+ typeCardNote +","+ cardOriIssueDate);

   if(cardComboIndicator.equals("Y") && cardGroupCode.equals("1653")) {	   	  
	   if(cardCurrentCode.equals("0")) {
		   allData[6][2]++;
	   } 
      
      if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
         comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
      {allData[6][1]++; allData[7][1]++; allData[26][1]++;}
      
      if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
      {allData[6][3]++; allData[7][3]++; allData[26][3]++;}
      return;
     }

   switch (typeCardNote)
     {
      // all_data[0][1]  array_x:   0 不用
      case "I":
    	       if(cardCurrentCode.equals("0")) {
    	    	   allData[1][2]++;
    	       }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[1][1]++; allData[7][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[1][3]++; allData[7][3]++; allData[26][3]++;}
               break;
      case "S":
    	       if(cardCurrentCode.equals("0")) {
    	    	   allData[2][2]++;
    	       }
    	       
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[2][1]++; allData[7][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[2][3]++; allData[7][3]++; allData[26][3]++;}
               break;
      case "P":
    	      if(cardCurrentCode.equals("0")) {
    	    	  allData[3][2]++;
    	      }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[3][1]++; allData[7][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[3][3]++; allData[7][3]++; allData[26][3]++;}
               break;
      case "G":
    	       if(cardCurrentCode.equals("0")) {
    	    	   allData[4][2]++;
    	       }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[4][1]++; allData[7][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[4][3]++; allData[7][3]++; allData[26][3]++;}
               break;
      case "C":
    	       if(cardCurrentCode.equals("0")) {
    	    	   allData[5][2]++;
    	       }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[5][1]++; allData[7][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[5][3]++; allData[7][3]++; allData[26][3]++;}
               break;
     }
}
/***********************************************************************/
void masterRtn() throws Exception {
	if(cardCurrentCode.equals("0")) {
		   allData[19][2]++;
		   allData[26][2]++;
	}

if(DEBUG==1) showLogMessage("I","","  master="+ cardComboIndicator +" note="+ typeCardNote);

  if(cardComboIndicator.equals("Y"))
    {
     switch (typeCardNote)
     {
      case "S":
    	       if(cardCurrentCode.equals("0")) {
    		       allData[15][2]++;
    	       }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[15][1]++; allData[19][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[15][3]++; allData[19][3]++; allData[26][3]++;}
               break;
      case "P":
    	       if(cardCurrentCode.equals("0")) {
    	    	   allData[16][2]++;
    	       }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[16][1]++; allData[19][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[16][3]++; allData[19][3]++; allData[26][3]++;}
               break;
      case "G":
	           if(cardCurrentCode.equals("0")) {
	        	   allData[17][2]++;
	           }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[17][1]++; allData[19][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[17][3]++; allData[19][3]++; allData[26][3]++;}
               break;
      case "C":
	           if(cardCurrentCode.equals("0")) {
	        	   allData[18][2]++;
	           }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[18][1]++; allData[19][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[18][3]++; allData[19][3]++; allData[26][3]++;}
               break;
     }
     return;
    }

   switch (typeCardNote)
     {
      // all_data[0][1]  array_x:   0 不用
      case "I":
	           if(cardCurrentCode.equals("0")) {
	        	   allData[8][2]++;
	           }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[8][1]++; allData[19][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[8][3]++; allData[19][3]++;  allData[26][3]++;}
               break;
      case "S":
    	  
    	       if(!cardGroupCode.equals("6673") && !cardGroupCode.equals("6674")){
        	       if(cardCurrentCode.equals("0")) {
        	    	   allData[9][2]++;
        	       }
                  
                  if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                     comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                     { allData[9][1]++; allData[19][1]++; allData[26][1]++;}
                  
                  if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                     { allData[9][3]++; allData[19][3]++; allData[26][3]++;}  
    	       }
               else{
        	       if(cardCurrentCode.equals("0")) {
        	    	   allData[10][2]++;
        	       }
                  
                  if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                     comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                     { allData[10][1]++; allData[19][1]++; allData[26][1]++;}
                  
                  if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                     { allData[10][3]++; allData[19][3]++; allData[26][3]++;}  
               }
               break;
      case "P":
    	      if(!cardGroupCode.equals("1670") && !cardGroupCode.equals("1671")) {
        	       if(cardCurrentCode.equals("0")) {
        	    	   allData[11][2]++;
        	       }
                  
                  if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                     comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                     { allData[11][1]++; allData[19][1]++; allData[26][1]++;}
                  
                  if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                     { allData[11][3]++; allData[19][3]++; allData[26][3]++;}
                 }
               else
                 {
        	       if(cardCurrentCode.equals("0")) {
        	    	   allData[12][2]++;
        	       }
                  
                  if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                     comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                     { allData[12][1]++; allData[19][1]++; allData[26][1]++;}
                  
                  if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                     { allData[12][3]++; allData[19][3]++; allData[26][3]++;}
                 }
               break;
      case "G":
	            if(cardCurrentCode.equals("0")) {
	            	allData[13][2]++;
	            }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[13][1]++; allData[19][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[13][3]++; allData[19][3]++; allData[26][3]++;}
               break;
      case "C":
	            if(cardCurrentCode.equals("0")) {
	            	allData[14][2]++;
	            }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[14][1]++; allData[19][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))
                  { allData[14][3]++; allData[19][3]++; allData[26][3]++;}
               break;
     }
}
/**********************************************************************/
void jcbRtn() throws Exception {
    if(cardCurrentCode.equals("0")) {
    	allData[24][2]++;     // 小計   
    	allData[26][2]++;     // 合計
    }

   switch (typeCardNote)
     {
      case "P":
    	       if(cardCurrentCode.equals("0")) {
    	    	   allData[20][2]++;
    	       }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[20][1]++; allData[24][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))    
                  { allData[20][3]++; allData[24][3]++; allData[26][3]++;}
               break;
      case "G":
    	       if(cardCurrentCode.equals("0")) {
    	    	   allData[21][2]++;
    	       }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[21][1]++; allData[24][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))    
                  { allData[21][3]++; allData[24][3]++; allData[26][3]++;}
               break;
      case "C":
    	       if(cardCurrentCode.equals("0")) {
    	    	   allData[22][2]++;
    	       }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[22][1]++; allData[24][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))    
                  { allData[22][3]++; allData[24][3]++; allData[26][3]++;}
               break;
      case "S":
    	       if(cardCurrentCode.equals("0")) {
    		       allData[23][2]++;
    	       }
               
               if(comcr.str2long(cardOriIssueDate) >= comcr.str2long(hBegDate)  &&
                  comcr.str2long(cardOriIssueDate) <= comcr.str2long(hEndDate))
                  { allData[23][1]++; allData[24][1]++; allData[26][1]++;}
               
               if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil))    
                  { allData[23][3]++; allData[24][3]++; allData[26][3]++;}
               break;
    }
}
/***********************************************************************/
void initRtn() throws Exception 
{
     cardCardNo = "";
     cardAcnoPSeqno = "";
     cardPSeqno = "";
     cardOriIssueDate = "";
     cardGroupCode = "";
     cardCardType = "";
     cardBinType = "";
     cardSupFlag = "";
     cardComboIndicator = "";
     cardCurrentCode = "";
     cardLastConsumeDate = "";
     typeCardNote = "";
     acctCardIndicator = "";
     idnoStudent = "";
     idnoBirthday = "";
     idno20Flag = "";
}
/***********************************************************************/

void selectCrdIdno() throws Exception {
	
	//個人卡正卡歸戶數
	allData[29][2] = selectPerPriCurr();
	showLogMessage("I","","Read END selectPerPriCurr ="+ allData[29][2]);
	
	allData[29][3] = selectPerPriCardEffi();
	showLogMessage("I","","Read END selectPerPriCardEffi ="+ allData[29][3]);
	
	//個人卡附卡歸戶數
	allData[30][2] = selectPerAddiCurr();
	showLogMessage("I","","Read END selectPerAddiCurr ="+ allData[30][2]);
	
	allData[30][3] = selectPerAddiEffi();
	showLogMessage("I","","Read END selectPerAddiEffi ="+ allData[30][3]);
	
	//學生卡正卡歸戶數(未滿20歲)
	allData[31][2] = selectStuPri20DownCurr();
	showLogMessage("I","","Read END selectStuPri20DownCurr ="+ allData[31][2]);
	
	allData[31][3] = selectStuPri20DownEffi();
	showLogMessage("I","","Read END selectStuPri20DownEffi ="+ allData[31][3]);
	
	//學生卡正卡歸戶數(20歲(含)以上)
	allData[32][2] = selectStuPri20UpCurr();
	showLogMessage("I","","Read END selectStuPri20UpCurr ="+ allData[32][2]);
	
	allData[32][3] = selectStuPri20UpEffi();
	showLogMessage("I","","Read END selectStuPri20UpEffi ="+ allData[32][3]);

	//學生卡附卡歸戶數(未滿20歲)
	allData[33][2] = selectStuAddi20DownCurr();
	showLogMessage("I","","Read END selectStuAddi20DownCurr ="+ allData[33][2]);
	
	allData[33][3] = selectStuAddi20DownEffi();
	showLogMessage("I","","Read END selectStuAddi20DownEffi ="+ allData[33][3]);
	
	//學生卡附卡歸戶數(20歲(含)以上)
	allData[34][2] = selectStuAddi20UpCurr();
	showLogMessage("I","","Read END selectStuAddi20UpCurr ="+ allData[34][2]);
	
	allData[34][3] = selectStuAddi20UpEffi();
	showLogMessage("I","","Read END selectStuAddi20UpEffi ="+ allData[34][3]);
	
	//頂級卡
	allData[37][2] = selectTopCurr();
	showLogMessage("I","","Read END selectTopCurr ="+ allData[37][2]);
	
	allData[37][3] = selectTopEffi();
	showLogMessage("I","","Read END selectTopEffi ="+ allData[37][3]);
	
	//白金卡(包括鈦金、晶緻、御璽、商旅)
	allData[38][2] = selectPlaCurr();
	showLogMessage("I","","Read END selectPlaCurr ="+ allData[38][2]);
	
	allData[38][3] = selectPlaEffi();
	showLogMessage("I","","Read END selectPlaEffi ="+ allData[38][3]);
	
	//金卡
	allData[39][2] = selectGolCurr();
	showLogMessage("I","","Read END selectGolCurr ="+ allData[39][2]);
	
	allData[39][3] = selectGolEffi();
	showLogMessage("I","","Read END selectGolEffi ="+ allData[39][3]);
	
	//普卡
	allData[40][2] = selectClaCurr();
	showLogMessage("I","","Read END selectClaCurr ="+ allData[40][2]);
	
	allData[40][3] = selectClaEffi();
	showLogMessage("I","","Read END selectClaEffi ="+ allData[40][3]);
}

/***********************************************************************/
int selectPerPriCurr() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno ";
  sqlCmd += " where id_p_seqno in ";
  sqlCmd += " (select id_p_seqno ";
  sqlCmd += "    from crd_card ";
  sqlCmd += "   where acct_type = '01' ";
  sqlCmd += "     and sup_flag = '0' ";
  sqlCmd += "     and current_code = '0' ";
  sqlCmd += "   group by id_p_seqno) ";
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectPerPriCardEffi() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno ";
  sqlCmd += " where id_p_seqno in ";
  sqlCmd += " (select id_p_seqno ";
  sqlCmd += "    from crd_card ";
  sqlCmd += "   where acct_type = '01' ";
  sqlCmd += "     and sup_flag = '0' ";
  sqlCmd += "     and current_code = '0' ";
  sqlCmd += "     and last_consume_date >= ? ";
  sqlCmd += "   group by id_p_seqno) ";
  setString(1 , hBegDateBil);
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectPerAddiCurr() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno ";
  sqlCmd += " where id_p_seqno in ";
  sqlCmd += " (select id_p_seqno ";
  sqlCmd += "    from crd_card ";
  sqlCmd += "   where acct_type = '01' ";
  sqlCmd += "     and sup_flag = '1' ";
  sqlCmd += "     and current_code = '0' ";
  sqlCmd += "   group by id_p_seqno) ";
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectPerAddiEffi() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno ";
  sqlCmd += " where id_p_seqno in ";
  sqlCmd += " (select id_p_seqno ";
  sqlCmd += "    from crd_card ";
  sqlCmd += "   where acct_type = '01' ";
  sqlCmd += "     and sup_flag = '1' ";
  sqlCmd += "     and current_code = '0' ";
  sqlCmd += "     and last_consume_date >= ? ";
  sqlCmd += "   group by id_p_seqno) ";
  setString(1 , hBegDateBil);
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectStuPri20DownCurr() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno a,crd_card b ";
  sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
  sqlCmd += "   and b.acct_type = '01' ";
  sqlCmd += "   and b.sup_flag = '0' ";
  sqlCmd += "   and b.current_code = '0' ";
  sqlCmd += "   and a.student = 'Y' ";
  sqlCmd += "   and a.birthday > ? ";
  setString(1 , h20YearsOldDate);
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectStuPri20DownEffi() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno a,crd_card b ";
  sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
  sqlCmd += "   and b.acct_type = '01' ";
  sqlCmd += "   and b.sup_flag = '0' ";
  sqlCmd += "   and b.current_code = '0' ";
  sqlCmd += "   and b.last_consume_date >= ? ";
  sqlCmd += "   and a.student = 'Y' ";
  sqlCmd += "   and a.birthday > ? ";
  setString(1 , hBegDateBil);
  setString(2 , h20YearsOldDate);
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectStuPri20UpCurr() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno a,crd_card b ";
  sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
  sqlCmd += "   and b.acct_type = '01' ";
  sqlCmd += "   and b.sup_flag = '0' ";
  sqlCmd += "   and b.current_code = '0' ";
  sqlCmd += "   and a.student = 'Y' ";
  sqlCmd += "   and a.birthday <= ? ";
  setString(1 , h20YearsOldDate);
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectStuPri20UpEffi() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno a,crd_card b ";
  sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
  sqlCmd += "   and b.acct_type = '01' ";
  sqlCmd += "   and b.sup_flag = '0' ";
  sqlCmd += "   and b.current_code = '0' ";
  sqlCmd += "   and b.last_consume_date >= ? ";
  sqlCmd += "   and a.student = 'Y' ";
  sqlCmd += "   and a.birthday <= ? ";
  setString(1 , hBegDateBil);
  setString(2 , h20YearsOldDate);
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectStuAddi20DownCurr() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno a,crd_card b ";
  sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
  sqlCmd += "   and b.acct_type = '01' ";
  sqlCmd += "   and b.sup_flag = '1' ";
  sqlCmd += "   and b.current_code = '0' ";
  sqlCmd += "   and a.student = 'Y' ";
  sqlCmd += "   and a.birthday > ? ";
  setString(1 , h20YearsOldDate);
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectStuAddi20DownEffi() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno a,crd_card b ";
  sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
  sqlCmd += "   and b.acct_type = '01' ";
  sqlCmd += "   and b.sup_flag = '1' ";
  sqlCmd += "   and b.current_code = '0' ";
  sqlCmd += "   and b.last_consume_date >= ? ";
  sqlCmd += "   and a.student = 'Y' ";
  sqlCmd += "   and a.birthday > ? ";
  setString(1 , hBegDateBil);
  setString(2 , h20YearsOldDate);
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectStuAddi20UpCurr() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno a,crd_card b ";
  sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
  sqlCmd += "   and b.acct_type = '01' ";
  sqlCmd += "   and b.sup_flag = '1' ";
  sqlCmd += "   and b.current_code = '0' ";
  sqlCmd += "   and a.student = 'Y' ";
  sqlCmd += "   and a.birthday <= ? ";
  setString(1 , h20YearsOldDate);
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectStuAddi20UpEffi() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno a,crd_card b ";
  sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
  sqlCmd += "   and b.acct_type = '01' ";
  sqlCmd += "   and b.sup_flag = '1' ";
  sqlCmd += "   and b.current_code = '0' ";
  sqlCmd += "   and b.last_consume_date >= ? ";
  sqlCmd += "   and a.student = 'Y' ";
  sqlCmd += "   and a.birthday <= ? ";
  setString(1 , hBegDateBil);
  setString(2 , h20YearsOldDate);
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectTopCurr() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno ";
  sqlCmd += " where id_p_seqno in ";
  sqlCmd += " (select a.id_p_seqno ";
  sqlCmd += "    from crd_card a,ptr_card_type b ";
  sqlCmd += "   where a.card_type = b.card_type ";
  sqlCmd += "     and a.acct_type = '01' ";
  sqlCmd += "     and a.current_code = '0' ";
  sqlCmd += "     and b.card_note = 'I' ";
  sqlCmd += "   group by a.id_p_seqno) ";
  int recCnt = selectTable();

  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectTopEffi() throws Exception {	
  sqlCmd = " select count(*) as crd_cnt ";
  sqlCmd += "  from crd_idno ";
  sqlCmd += " where id_p_seqno in ";
  sqlCmd += " (select a.id_p_seqno ";
  sqlCmd += "    from crd_card a,ptr_card_type b ";
  sqlCmd += "   where a.card_type = b.card_type ";
  sqlCmd += "     and a.acct_type = '01' ";
  sqlCmd += "     and a.current_code = '0' ";
  sqlCmd += "     and b.card_note = 'I' ";
  sqlCmd += "     and last_consume_date >= ? ";
  sqlCmd += "   group by a.id_p_seqno) ";
  setString(1 , hBegDateBil);
  int recCnt = selectTable();
  
  return  getValueInt("crd_cnt");
}
/***********************************************************************/
int selectPlaCurr() throws Exception {	
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note in ('S','P') ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectPlaEffi() throws Exception {	
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note in ('S','P') ";
	  sqlCmd += "     and last_consume_date >= ? ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  setString(1 , hBegDateBil);
	  int recCnt = selectTable();
	  
	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	int selectGolCurr() throws Exception {	
      sqlCmd = " select count(*) as crd_cnt ";
      sqlCmd += "  from crd_idno ";
      sqlCmd += " where id_p_seqno in ";
      sqlCmd += " (select a.id_p_seqno ";
      sqlCmd += "    from crd_card a,ptr_card_type b ";
      sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.acct_type = '01' ";
      sqlCmd += "     and a.current_code = '0' ";
      sqlCmd += "     and b.card_note = 'G' ";
      sqlCmd += "   group by a.id_p_seqno) ";
      int recCnt = selectTable();
      
      return  getValueInt("crd_cnt");
	}
    /***********************************************************************/
    int selectGolEffi() throws Exception {	
      sqlCmd = " select count(*) as crd_cnt ";
      sqlCmd += "  from crd_idno ";
      sqlCmd += " where id_p_seqno in ";
      sqlCmd += " (select a.id_p_seqno ";
      sqlCmd += "    from crd_card a,ptr_card_type b ";
      sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.acct_type = '01' ";
      sqlCmd += "     and a.current_code = '0' ";
      sqlCmd += "     and b.card_note = 'G' ";
      sqlCmd += "     and last_consume_date >= ? ";
      sqlCmd += "   group by a.id_p_seqno) ";
      setString(1 , hBegDateBil);
      int recCnt = selectTable();
      
      return  getValueInt("crd_cnt");
    }
    /***********************************************************************/
	int selectClaCurr() throws Exception {	
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'C' ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  int recCnt = selectTable();
	  
	  return  getValueInt("crd_cnt");
	}	
	/***********************************************************************/
	int selectClaEffi() throws Exception {	
	  sqlCmd = " select count(*) as crd_cnt ";
	  sqlCmd += "  from crd_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select a.id_p_seqno ";
	  sqlCmd += "    from crd_card a,ptr_card_type b ";
	  sqlCmd += "   where a.card_type = b.card_type ";
	  sqlCmd += "     and a.acct_type = '01' ";
	  sqlCmd += "     and a.current_code = '0' ";
	  sqlCmd += "     and b.card_note = 'C' ";
	  sqlCmd += "     and last_consume_date >= ? ";
	  sqlCmd += "   group by a.id_p_seqno) ";
	  setString(1 , hBegDateBil);
	  int recCnt = selectTable();
	  
	  return  getValueInt("crd_cnt");
	}
	/***********************************************************************/
	void selectDbcIdno() throws Exception {
		
		//VISA金融卡
	    allData[41][2] = selectDbcCardCurr();
	    showLogMessage("I","","Read END selectDbcCardCurr ="+ allData[41][2]);

	    allData[41][3] = selectDbcCardEffc();
	    showLogMessage("I","","Read END selectDbcCardEffc ="+ allData[41][3]);
	}
	/***********************************************************************/
	int selectDbcCardCurr() throws Exception 
	{
	  idnoStudent = "";
	  idnoBirthday = ""; 
	  idno20Flag = "";  
	  
	  sqlCmd = " select count(*) as dbc_cnt ";
	  sqlCmd += "  from dbc_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select id_p_seqno ";
	  sqlCmd += "    from dbc_card";
	  sqlCmd += "   where current_code = '0' ";
	  sqlCmd += "   group by id_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("dbc_cnt");
	}
	/***********************************************************************/
	int selectDbcCardEffc() throws Exception 
	{
	  idnoStudent = "";
	  idnoBirthday = ""; 
	  idno20Flag = ""; 

	  sqlCmd = " select count(*) as dbc_cnt ";
	  sqlCmd += "  from dbc_idno ";
	  sqlCmd += " where id_p_seqno in ";
	  sqlCmd += " (select id_p_seqno ";
	  sqlCmd += "    from dbc_card";
	  sqlCmd += "   where current_code = '0' ";
	  sqlCmd += "     and last_consume_date >= ? ";
	  sqlCmd += "   group by id_p_seqno) ";
	  setString(1 , hBegDateBil);
	  int recCnt = selectTable();

	  return  getValueInt("dbc_cnt");
	}
	/***********************************************************************/
	void selectCrdCorp() throws Exception {
		
		//法人卡
	    allData[42][2] = selectBusCardCurr();
	    showLogMessage("I","","Read END selectBusCardCurr ="+ allData[42][2]);

	    allData[42][3] = selectBusCardEffc();
	    showLogMessage("I","","Read END selectBusCardEffc ="+ allData[42][3]);
	}
	/***********************************************************************/
	int selectBusCardCurr() throws Exception 
	{
	  idnoStudent = "";
	  idnoBirthday = ""; 
	  idno20Flag = "";  
	  
	  sqlCmd = " select count(*) as dbc_cnt ";
	  sqlCmd += "  from crd_corp ";
	  sqlCmd += " where corp_p_seqno in ";
	  sqlCmd += " (select corp_p_seqno ";
	  sqlCmd += "    from crd_card";
	  sqlCmd += "   where acct_type in ('03','06') ";
	  sqlCmd += "     and current_code = '0' ";
	  sqlCmd += "   group by corp_p_seqno) ";
	  int recCnt = selectTable();

	  return  getValueInt("dbc_cnt");
	}
	/***********************************************************************/
	int selectBusCardEffc() throws Exception 
	{
	  idnoStudent = "";
	  idnoBirthday = ""; 
	  idno20Flag = ""; 

	  sqlCmd = " select count(*) as dbc_cnt ";
	  sqlCmd += "  from crd_corp ";
	  sqlCmd += " where corp_p_seqno in ";
	  sqlCmd += " (select corp_p_seqno ";
	  sqlCmd += "    from crd_card";
	  sqlCmd += "   where acct_type in ('03','06') ";
	  sqlCmd += "     and current_code = '0' ";
	  sqlCmd += "     and last_consume_date >= ? ";
	  sqlCmd += "   group by corp_p_seqno) ";
	  setString(1 , hBegDateBil);
	  int recCnt = selectTable();

	  return  getValueInt("dbc_cnt");
	}
	/***********************************************************************/
void headFile() throws Exception 
{
        String temp = "";

        pageCnt1++;
        if(pageCnt1 > 1)
           lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));

        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
        buf = comcr.insertStr(buf, ""              + rptName1                 , 50);
        buf = comcr.insertStr(buf, "保存年限: 五年"                           ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                       hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRM65     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp                         ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));

        buf = "";
        buf = "卡數                              當月發卡數   流通卡數   有效卡數 ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "123456789012345678901234567890=== ========== ---------- ---------- ";
        buf = "================================= =========- ========== ========== ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = 6;
}
/***********************************************************************/
 void tailFile() throws UnsupportedEncodingException 
{
//   buf = "";
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
//
//   htail.fileValue = "備 註: １、本表為ＯＲＧ１０６下所有ＴＹＰＥ，排除ＴＹＰＥ為５９９、９９７、９９８者";
//   buf = htail.allText();
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
//
//   htail.fileValue = "       ２、流通卡為截至目前未停用之卡片，即控管碼不為Ａ、Ｂ、Ｅ、Ｆ、Ｋ、Ｌ、Ｍ、Ｎ、Ｏ、Ｓ、Ｘ者";
//   buf = htail.allText();
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
//
//   htail.fileValue = "       ３、有效卡為最近６個月有消費紀錄，且控管碼不為Ａ、Ｂ、Ｅ、Ｆ、Ｋ、Ｌ、Ｍ、Ｎ、Ｏ、Ｓ、Ｘ者";
//   buf = htail.allText();
//   lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

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

     for (int i = 0; i < arrayX; i++)
       {
        if(i == 0)   continue;

        data = null;
        data = new buf1();

        data.name        = allDataH[i];
        szTmp = comcr.commFormat("2z,3z,3z", allData[i][1]);
        switch (i) 
         {
          case 29: case 30: case 31: case 32: case 33: 
          case 34: case 37: case 38: case 39: case 40: 
          case 41: 
          case 42: szTmp = "          ";
                   break;
         }
        data.data01      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][2]);
        data.data02      = szTmp;

        szTmp = comcr.commFormat("2z,3z,3z", allData[i][3]);
        data.data03      = szTmp;

        buf = data.allText();
        switch (i) 
         {
          case 25:
          case 27: buf = "";
                   break;
          case 28: buf = "歸戶數 :                                         流通卡     有效卡";
                   break;
          case 35: buf = "＊卡種分類歸戶數 :";
                   break;
          case 36: buf = "歸戶數 :                                         流通卡     有效卡";
                   break;
         }
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = lineCnt1 + 1;
       }

     return;
}
/************************************************************************/
void processAi501() throws Exception {

	selectMisReportData();
	
	vCardIssueTotal = tmpSumField1 + allData[7][1];
	mCardIssueTotal = tmpSumField2 + allData[19][1];
	jCardIssueTotal = tmpSumField3 + allData[24][1];

	rptData.put("STU_20UNDER_MAIN_ACCT_CNT", allData[31][2]);
	rptData.put("STU_20UP_MAIN_ACCT_CNT", allData[32][2]);	
	rptData.put("STU_MAIN_CARD_ACCT_SUM", allData[31][2] + allData[32][2]);
	
	rptData.put("STU_20UNDER_ADDI_ACCT_CNT", allData[33][2]);
	rptData.put("STU_20UP_ADDI_ACCT_CNT", allData[34][2]);
	rptData.put("STU_ADDI_CARD_ACCT_SUM", allData[33][2] + allData[34][2]);
	
	stuCardAcctTotal =  allData[31][2] + allData[32][2] + allData[33][2] + allData[34][2];
	
	insertMisReportData();
}

/***********************************************************************/
void selectMisReportData() throws Exception {
        sqlCmd = "select sum_field1, ";
        sqlCmd += " sum_field2, ";
        sqlCmd += " sum_field3 ";
        sqlCmd += " from mis_report_data ";
        sqlCmd += " where data_month = ?   ";
        sqlCmd += "   and data_from = 'CRM65' ";
        setString(1, hLastMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tmpSumField1 = getValueInt("sum_field1");
            tmpSumField2 = getValueInt("sum_field2");
            tmpSumField3 = getValueInt("sum_field3");
        }
}

/***********************************************************************/
void insertMisReportData() throws Exception {
	
	//重跑時要先刪除上一次產生的資料
	deleteExistRptRecord();
	commitDataBase();
	
    setValue("DATA_MONTH", comc.getSubString(hBusiBusinessDate,0,6));
    setValue("DATA_FROM", "CRM65");
    setValue("DATA_DATE", hBusiBusinessDate);
    setValueDouble("SUM_FIELD1", vCardIssueTotal);
    setValueDouble("SUM_FIELD2", mCardIssueTotal);
    setValueDouble("SUM_FIELD3", jCardIssueTotal);
    setValueDouble("SUM_FIELD4", stuCardAcctTotal);
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
	whereStr += " and data_from = 'CRM65' ";
	
	setString(1, comc.getSubString(hBusiBusinessDate,0,6));

	deleteTable();
	
}

/************************************************************************/
public static void main(String[] args) throws Exception 
{
       CrdR065 proc = new CrdR065();
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
            rtn += fixLeft(fileValue,110);
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
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";

            rtn += fixLeft(name         , 32+1);
            rtn += fixLeft(data01       ,  10+1);
            rtn += fixLeft(data02       ,  10+1);
            rtn += fixLeft(data03       ,  10+1);
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
