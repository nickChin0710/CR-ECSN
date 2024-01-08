/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/02/20 V1.01.01  Lai         program initial (report only one page)     *
*  112/07/05 V1.01.02  Bo Yang     update by naming rule                      *
*  112/07/17 V1.01.03  Wilson      刪除程式重複執行判斷                                                                                  *
*  112/08/07 V1.01.04  Wilson      讀取資料邏輯調整                                                                                        *
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

public class CrdR063 extends AccessDAO {
    private final String PROGNAME = "每月個人卡信用卡流通卡量彙總表  112/11/08 V1.01.06";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommDate commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;
    JSONObject     rptData = null;

    int    DEBUG  = 0;
    String hTempUser = "";

    int reportPageLine = 45;
    String prgmId    = "CrdR063";

    String rptIdR1 = "CRM63";
    String rptName1  = "每月個人卡信用卡流通卡量彙總表";
    int pageCnt1 = 0, lineCnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int totCnt = 0;

    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";
    String hChiYymmdd =  "";
    String hBegDate =  "";
    String hEndDate =  "";
    String hBegDateBil =  "";
    String hEndDateBil =  "";
    String hLastDay =  "";

    String cardCardNo = "";
    String cardCurrentCode = "";
    String cardSupFlag = "";
    String cardBinType = "";
    String typeCardNote = "";
    String cardGroupCode = "";
    String idnoBirthday = "";
    int idnoYearRank = 0;
    int vAllCnt = 0;
    int mAllCnt = 0;
    int jAllCnt = 0;

    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    String tmpstr1 = "";
    int arrayX = 24;
    int arrayY = 17;
    String[] allDataH = new String [arrayX];
    int[][] allData = new int [arrayX][arrayY];
    
    int genCardCurrentTotal = 0;

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
       comc.errExit("Usage : CrdR063 [yyyymmdd] [seq_no] ", "");
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
void initArray() throws Exception
{
   //  １２３４５６７８９０～
   showLogMessage("I", "", " Array-x="+ arrayX + "  Array-y="+ arrayY);
   for (int i = 0; i < arrayX; i++) 
       {
        allDataH[i] = "";
        switch (i) {
          case 0: 
            allDataH[i] = "正卡未滿２０歲";
            break;
          case 1:
            allDataH[i] = "  ２０～２４歲";
            break;
          case 2:
            allDataH[i] = "  ２５～２９歲";
            break;
          case 3:
            allDataH[i] = "  ３０～３９歲";
            break;
          case 4:
            allDataH[i] = "  ４０～４９歲";
            break;
          case 5:
            allDataH[i] = "  ５０～５９歲";
            break;
          case 6: 
            allDataH[i] = "其他(６０歲: 含)以上";
            break;
          case 8: 
            allDataH[i] = "     合  計:";
            break;
          case 10: 
            allDataH[i] = "附卡未滿２０歲";
            break;
          case 11:
            allDataH[i] = "  ２０～２４歲";
            break;
          case 12:
            allDataH[i] = "  ２５～２９歲";
            break;
          case 13:
            allDataH[i] = "  ３０～３９歲";
            break;
          case 14:
            allDataH[i] = "  ４０～４９歲";
            break;
          case 15:
            allDataH[i] = "  ５０～５９歲";
            break;
          case 16: 
            allDataH[i] = "其他(６０歲: 含)以上";
            break;
          case 18: 
            allDataH[i] = "       合計:";
            break;
          case 21: 
            allDataH[i] = "正附卡 總計:";
            break;
        }

        for (int j = 0; j < arrayY; j++) 
            allData[i][j] = 0;
       }
}
/***********************************************************************/
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
       hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                            : hBusiBusinessDate;
   }

   sqlCmd = "select to_char(add_months(to_date(?,'yyyymmdd'),-5),'yyyymm')||'01' h_beg_date_bil ";
   sqlCmd += "    , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date_bil ";
   sqlCmd += "    , to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01' h_beg_date ";
   sqlCmd +="     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
   sqlCmd += " from dual ";
   setString(1, hBusiBusinessDate);
   setString(2, hBusiBusinessDate);
   setString(3, hBusiBusinessDate);
   setString(4, hBusiBusinessDate);

   recordCnt = selectTable();
   if(recordCnt > 0) {
	  hBegDateBil = getValue("h_beg_date_bil");
	  hEndDateBil = getValue("h_end_date_bil");
      hBegDate = getValue("h_beg_date");
      hEndDate = getValue("h_end_date");
     }

   hChiYymmdd = commDate.toTwDate(hBusiBusinessDate);
   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]" , hBusiBusinessDate
           , hChiYymmdd, hBegDateBil, hEndDateBil, hBegDate, hEndDate));
   return 0;
}
/***********************************************************************/
void selectCrdCard() throws Exception 
{
  String tmp  = "";
  int    cnt1 = 0;
      
  selectSQL = " a.card_no             ,a.current_code        , "
            + " a.bin_type            ,a.sup_flag            , "
            + " b.birthday            ,c.card_note           , "
            + " a.group_code          , "
            + " case when ? - to_number(substr(decode(b.birthday,'','20000000',b.birthday),1,4)) < 20 then 0 "
            + "      when ? - to_number(substr(decode(b.birthday,'','20000000',b.birthday),1,4)) between 20 and 24 then 1 " 
            + "      when ? - to_number(substr(decode(b.birthday,'','20000000',b.birthday),1,4)) between 25 and 29 then 2 " 
            + "      when ? - to_number(substr(decode(b.birthday,'','20000000',b.birthday),1,4)) between 30 and 39 then 3 " 
            + "      when ? - to_number(substr(decode(b.birthday,'','20000000',b.birthday),1,4)) between 40 and 49 then 4 " 
            + "      when ? - to_number(substr(decode(b.birthday,'','20000000',b.birthday),1,4)) between 50 and 59 then 5 " 
            + "      else 6  end as year_rank ";
  daoTable = "ptr_card_type c, crd_idno b, crd_card a";
  whereStr = "where a.current_code = '0' "
		   + "  and a.acct_type = '01' "
           + "  and b.id_p_seqno = a.id_p_seqno "
           + "  and c.card_type  = a.card_type  "
           + "order by a.sup_flag  ";

  setInt(   1 , Integer.parseInt(sysDate.substring(0, 4)));
  setInt(   2 , Integer.parseInt(sysDate.substring(0, 4)));
  setInt(   3 , Integer.parseInt(sysDate.substring(0, 4)));
  setInt(   4 , Integer.parseInt(sysDate.substring(0, 4)));
  setInt(   5 , Integer.parseInt(sysDate.substring(0, 4)));
  setInt(   6 , Integer.parseInt(sysDate.substring(0, 4)));

  openCursor();

  while (fetchTable()) {
     initRtn();
     totCnt++;

     cardCardNo = getValue("card_no"     );
     cardCurrentCode = getValue("current_code");
     cardSupFlag = getValue("sup_flag"    );
     cardBinType = getValue("bin_type"    );
     typeCardNote = getValue("card_note"   );
     cardGroupCode = getValue("group_code");
     idnoBirthday = getValue("birthday"    );
     idnoYearRank = getValueInt("year_rank");

     if(!cardSupFlag.equals("0"))
       {
        idnoYearRank = getValueInt("year_rank") + 10;
       }

     if(totCnt % 5000 == 0 || totCnt == 1)
        showLogMessage("I","",String.format("R063 Process 1 record=[%d]\n", totCnt));

     if(DEBUG==1) showLogMessage("I","","Read card="+ cardCardNo +" Sup="+ cardSupFlag +" B="+ cardBinType +" B="+ idnoBirthday +" Rank="+ idnoYearRank +" Cnt="+ totCnt);

     if(totCnt == 1)    headFile();

     switch (cardBinType) 
       {
        case "V": visaRtn();   vAllCnt++;   break;
        case "M": masterRtn(); mAllCnt++;   break;
        case "J": jcbRtn();    jAllCnt++;   break;
       }
     allData[idnoYearRank][16]++; // 總計
     if(cardSupFlag.equals("0"))   allData[8][16]++; else allData[18][16]++; // 合計
     allData[21][16]++;             // 正附卡 總計
     if(DEBUG==1) showLogMessage("I","","  總計="+ allData[idnoYearRank][16]+" Rank="+ idnoYearRank);
    }

  if(totCnt > 0)
    {
     writeFile();
     tailFile();
    }
}
/***********************************************************************/
void visaRtn() throws Exception 
{

   switch (typeCardNote)
     {
      case "I": 
               allData[idnoYearRank][1]++;
               allData[21][1]++;
               if(cardSupFlag.equals("0"))   allData[8][1]++; else allData[18][1]++;
               break;
      case "S": 
               allData[idnoYearRank][2]++;
               allData[21][2]++;
               if(cardSupFlag.equals("0"))   allData[8][2]++; else allData[18][2]++;
               break;
      case "P": 
               allData[idnoYearRank][3]++;
               allData[21][3]++;
               if(cardSupFlag.equals("0"))   allData[8][3]++; else allData[18][3]++;
               break;
      case "G": 
               allData[idnoYearRank][4]++;
               allData[21][4]++;
               if(cardSupFlag.equals("0"))   allData[8][4]++; else allData[18][4]++;
               break;
      case "C": 
               allData[idnoYearRank][5]++;
               allData[21][5]++;
               if(cardSupFlag.equals("0"))   allData[8][5]++; else allData[18][5]++;
               break;
     }
}
/***********************************************************************/
void masterRtn() throws Exception 
{
// M-世界 M-鈦金 M-鈦商 M-白金 M-金卡 M-普卡
// CARD_CARD的BIN_TYPE = ‘M’ & PTR_CARD_TYPE的CARD_NOTE = ‘I’
// CARD_CARD的BIN_TYPE = ‘M’ & PTR_CARD_TYPE的CARD_NOTE = ‘S’ & PTR_ACCT_TYPE的CARD_INDICATOR <> ‘2’
// CARD_CARD的BIN_TYPE = ‘M’ & PTR_CARD_TYPE的CARD_NOTE = ‘S’ & PTR_ACCT_TYPE的CARD_INDICATOR  = ‘2’
   switch (typeCardNote)
     {
      case "I": 
               allData[idnoYearRank][6]++;
               allData[21][6]++;
               if(cardSupFlag.equals("0"))   allData[8][6]++; else allData[18][6]++;
               break;
      case "S": 
               if(!cardGroupCode.equals("6673") && !cardGroupCode.equals("6674"))   
                 {
                  allData[idnoYearRank][7]++;
                  allData[21][7]++;
                  if(cardSupFlag.equals("0")) allData[8][7]++; else allData[18][7]++;
                 }
               else
                 {
                  allData[idnoYearRank][8]++;
                  allData[21][8]++;
                  if(cardSupFlag.equals("0")) allData[8][8]++; else allData[18][8]++;
                 }
               break;
      case "P": 
               allData[idnoYearRank][9]++;
               allData[21][9]++;
               if(cardSupFlag.equals("0"))   allData[8][9]++; else allData[18][9]++;
               break;
      case "G": 
               allData[idnoYearRank][10]++;
               allData[21][10]++;
               if(cardSupFlag.equals("0"))   allData[8][10]++; else allData[18][10]++;
               break;
      case "C": 
               allData[idnoYearRank][11]++;
               allData[21][11]++;
               if(cardSupFlag.equals("0"))   allData[8][11]++; else allData[18][11]++;
               break;
     }
}
/***********************************************************************/
void jcbRtn() throws Exception 
{
   switch (typeCardNote)
     {
      case "P": 
               allData[idnoYearRank][12]++;
               allData[21][12]++;
               if(cardSupFlag.equals("0"))   allData[8][12]++; else allData[18][12]++;
               break;
      case "G": 
               allData[idnoYearRank][13]++;
               allData[21][13]++;
               if(cardSupFlag.equals("0"))   allData[8][13]++; else allData[18][13]++;
               break;
      case "C": 
               allData[idnoYearRank][14]++;
               allData[21][14]++;
               if(cardSupFlag.equals("0"))   allData[8][14]++; else allData[18][14]++;
               break;
      case "S": 
               allData[idnoYearRank][15]++;
               allData[21][15]++;
               if(cardSupFlag.equals("0"))   allData[8][15]++; else allData[18][15]++;
               break;
     }
}
/***********************************************************************/
void initRtn() throws Exception 
{
    cardSupFlag = "";
    cardCurrentCode = "";
    cardBinType = "";
    typeCardNote = "";
    cardGroupCode = "";
    idnoYearRank = 0;
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
        buf = comcr.insertStr(buf, ""              + rptName1                 , 48);
        buf = comcr.insertStr(buf, "保存年限: 五年"                           ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                       hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRM63     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp                         ,100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));

        buf = "";
        buf = "個人信用                 V-無限 V-御璽 V-白金 V-金卡 V-普卡 M-世界 M-鈦金 M-鈦商 M-白金 M-金卡 M-普卡 J-白金 J-金卡 J-普卡 J-晶緻 總  計 ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "======================== ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt1 = 6;
}
/***********************************************************************/
 void tailFile() throws UnsupportedEncodingException 
{
     if(DEBUG==1) showLogMessage("I","","END 總計="+ vAllCnt +" ,"+ mAllCnt +" ,"+ jAllCnt);
        htail.filler01    = "VISA    品牌總計 = ";
        tmp = String.format("%7d", vAllCnt);
        htail.issueAll = tmp;
        buf = htail.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        htail.filler01    = "MASTER  品牌總計 = ";
        tmp = String.format("%7d", mAllCnt);
        htail.issueAll = tmp;
        buf = htail.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        htail.filler01    = "JCB     品牌總計 = ";
        tmp = String.format("%7d", jAllCnt);
        htail.issueAll = tmp;
        buf = htail.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

}
/***********************************************************************/
void writeFile() throws Exception 
{
     String tmp = "";

     if(lineCnt1 == 1 || lineCnt1 > reportPageLine) {
        headFile();
       }

     for (int i = 0; i < arrayX; i++) 
       {
        data = null;
        data = new buf1();

        data.name        = allDataH[i];
        tmp = String.format("%6d", allData[i][1]);
        data.v1          = tmp;
        tmp = String.format("%6d", allData[i][2]);
        data.v2          = tmp;
        tmp = String.format("%6d", allData[i][3]);
        data.v3          = tmp;
        tmp = String.format("%6d", allData[i][4]);
        data.v4          = tmp;
        tmp = String.format("%6d", allData[i][5]);
        data.v5          = tmp;

        tmp = String.format("%6d", allData[i][6]);
        data.m1          = tmp;
        tmp = String.format("%6d", allData[i][7]);
        data.m2          = tmp;
        tmp = String.format("%6d", allData[i][8]);
        data.m3          = tmp;
        tmp = String.format("%6d", allData[i][9]);
        data.m4          = tmp;
        tmp = String.format("%6d", allData[i][10]);
        data.m5          = tmp;
        tmp = String.format("%6d", allData[i][11]);
        data.m6          = tmp;

        tmp = String.format("%6d", allData[i][12]);
        data.j1          = tmp;
        tmp = String.format("%6d", allData[i][13]);
        data.j2          = tmp;
        tmp = String.format("%6d", allData[i][14]);
        data.j3          = tmp;
        tmp = String.format("%6d", allData[i][15]);
        data.j4          = tmp;

        tmp = String.format("%6d", allData[i][16]);
        data.a1          = tmp;

        buf = data.allText();
        if(i == 7 || i == 9 || i == 17 || i == 19 || i == 20 || i == 22 || i == 23)    buf = "";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

//  if(DEBUG == 1) showLogMessage("I", "", " BUF="+ buf);


        lineCnt1 = lineCnt1 + 1;
       }

     return;
}
/************************************************************************/
void processAi501() throws Exception {

	rptData.put("GEN_MAIN_CARD_20UNDER_CURRENT_CNT", allData[0][16]);
	rptData.put("GEN_MAIN_CARD_20UP_CURRENT_CNT", allData[1][16]);
	rptData.put("GEN_MAIN_CARD_25UP_CURRENT_CNT", allData[2][16]);
	rptData.put("GEN_MAIN_CARD_30UP_CURRENT_CNT", allData[3][16]);
	rptData.put("GEN_MAIN_CARD_40UP_CURRENT_CNT", allData[4][16]);
	rptData.put("GEN_MAIN_CARD_50UP_CURRENT_CNT", allData[5][16]);
	rptData.put("GEN_MAIN_CARD_60UP_CURRENT_CNT", allData[6][16]);
	rptData.put("GEN_MAIN_CARD_CURRENT_SUM", allData[8][16]);
	
	rptData.put("GEN_ADDI_CARD_20UNDER_CURRENT_CNT", allData[10][16]);
	rptData.put("GEN_ADDI_CARD_20UP_CURRENT_CNT", allData[11][16]);
	rptData.put("GEN_ADDI_CARD_25UP_CURRENT_CNT", allData[12][16]);
	rptData.put("GEN_ADDI_CARD_30UP_CURRENT_CNT", allData[13][16]);
	rptData.put("GEN_ADDI_CARD_40UP_CURRENT_CNT", allData[14][16]);
	rptData.put("GEN_ADDI_CARD_50UP_CURRENT_CNT", allData[15][16]);
	rptData.put("GEN_ADDI_CARD_60UP_CURRENT_CNT", allData[16][16]);
	rptData.put("GEN_ADDI_CARD_CURRENT_SUM", allData[18][16]);
	
	genCardCurrentTotal = allData[8][16] + allData[18][16];
	
	insertMisReportData();
}

/***********************************************************************/
void insertMisReportData() throws Exception {
	
	//重跑時要先刪除上一次產生的資料
	deleteExistRptRecord();
	commitDataBase();
	
    setValue("DATA_MONTH", comc.getSubString(hBusiBusinessDate,0,6));
    setValue("DATA_FROM", "CRM63");
    setValue("DATA_DATE", hBusiBusinessDate);
    setValueDouble("SUM_FIELD1", genCardCurrentTotal);
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
	whereStr += " and data_from = 'CRM63' ";
	
	setString(1, comc.getSubString(hBusiBusinessDate,0,6));

	deleteTable();
	
}

/************************************************************************/
public static void main(String[] args) throws Exception 
{
       CrdR063 proc = new CrdR063();
       int  retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/************************************************************************/
  class buft 
    {
        String filler01;
        String fileValue;
        String issueAll;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(filler01   ,24);
            rtn += fixLeft(issueAll,11);
//          rtn += fixLeft(len, 1);
            return rtn;
        }

        
    }
  class buf1 
    {
        String name;
        String v1;
        String v2;
        String v3;
        String v4;
        String v5;
        String m1;
        String m2;
        String m3;
        String m4;
        String m5;
        String m6;
        String j1;
        String j2;
        String j3;
        String j4;
        String a1;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(name         ,  24+1);
            rtn += fixLeft(v1           ,   6+1);
            rtn += fixLeft(v2           ,   6+1);
            rtn += fixLeft(v3           ,   6+1);
            rtn += fixLeft(v4           ,   6+1);
            rtn += fixLeft(v5           ,   6+1);
            rtn += fixLeft(m1           ,   6+1);
            rtn += fixLeft(m2           ,   6+1);
            rtn += fixLeft(m3           ,   6+1);
            rtn += fixLeft(m4           ,   6+1);
            rtn += fixLeft(m5           ,   6+1);
            rtn += fixLeft(m6           ,   6+1);
            rtn += fixLeft(j1           ,   6+1);
            rtn += fixLeft(j2           ,   6+1);
            rtn += fixLeft(j3           ,   6+1);
            rtn += fixLeft(j4           ,   6+1);
            rtn += fixLeft(a1           ,   6+1);
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
