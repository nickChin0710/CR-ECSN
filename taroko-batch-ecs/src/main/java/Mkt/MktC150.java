/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/11/13  V1.00.25  Allen Ho   New                                        *
* 111/11/10  V1.00.26  Zuwei Su   sync from mega & coding standard update & 誤植MKT_CHANNEL_PARM、MKT_CHANREC_PARM欄位名稱為g_feedback_limit    *
* 112/04/26  V1.00.27  Han Yang   add new logic 通路群組活動回饋歸戶 (insertMktChannelgpList())                             *
* 112/05/10  V1.00.28  Han Yang    增’回饋類別’                                                                         *
* 112/05/12  V1.00.29  Han Yang    fix 字段err                                                                        *
* 112/05/16  V1.00.30   Ryan       新增 回饋類別處理邏輯                                                                                                                                                                                                            *
* 112/06/08  V1.00.31  Bo Yang    新增 '群組月累績最低消費金額' 欄位檢核                                                                                                                                                                          *
* 112/07/03  V1.00.32  Grace Huang  修訂 selectMktChannelRank1(), parm.feedback_key_sel="3", 以處理selectCrdCard3()      *
* 112/07/18  V1.00.33  Ryan       modify selectMktChannelParm02()                                                    *
* 112/08/09  V1.00.34  Ryan       insert mkt_channelgp_list之前先delete                                                *
* 112/08/30  V1.00.35  Grace Huang ins/del mkt_channel_list 增 cal_def_date, 以為mktC200 判讀                                                                                     *
* 112/09/11  V1.00.36  Grace Huang insert mkt_channelgp_list 之前先 delete, 依ACTIVE_GROUP_ID, fund_date                 *                
*                                  並增feedback_type寫入                                                                                                                                                                                                  *
* 112/09/27  V1.00.37  Ryan         selectMktChannelList() 增加left join MKT_CHANNEL_BILL 取得sum(dest_amt)              *
* 112/11/16  V1.00.38  Grace Huang remark insert dup message                                                          *
* 112/11/16  V1.00.39  Ryan  modify selectMktChannelList not found ==> continue
* 112/12/26  V1.00.40  Ryan  modify selectMktChannelList left join MKT_CHANNEL_BILL
***********************************************************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.math.BigDecimal;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktC150 extends AccessDAO
{
 private final String PROGNAME = "通路活動-回饋歸屬帳戶累計處理程式 112/12/26 V1.00.40";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";
 String tranSeqno  = "";
 String pSeqno     = "";
 String activeCode = "";

 double[] feedbackLimit   =  new double [4];

 int[]    activeSeqCnt   =  new int [4];;
 double[] activeSeqAmt   =  new double [4];

 double[] purchaseAmtS   =  new double [5];
 double[] purchaseAmtE   =  new double [5];
 String[] activeType      =  new String [5];
 double[] feedbackRate    =  new double [5];
 double[] feedbackAmt     =  new double [5];
 double[] feedbackLmtAmt =  new double [5];
 double[] feedbackLmtCnt =  new double [5];

 HashMap<String,String> activeGroupIdMap = new HashMap<String,String>();
 int  parmCnt  = 0,precCnt=0;
 int[] acctTypeSel = new int [20];

 String activeSeq = "";
 String recordGroupNo = "";
 int  totalCnt=0;
 int gpListCnt = 0;

// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC150 proc = new MktC150();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 3)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [active_code]");
       showLogMessage("I","","PARM 3 : [id_no/p_seqno/card_no]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }
   if ( args.length >= 2 )
      { activeCode = args[1]; }
   if ( args.length == 3 )
      { pSeqno = args[2]; }

   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectMktChannelParm();
   if (parmCnt==0)
      {
       showLogMessage("I","","今日["+businessDate+"]無活動回饋");
       return(0);
      }
   if (parmCnt > 0 ) {
	   showLogMessage("I",""," ");
	   showLogMessage("I","","======================================================");
	   showLogMessage("I",""," 判讀活動群組條件 selectMktChannelParm02()");
	   showLogMessage("I","","今日["+businessDate+"]有活動回饋 ");
       selectMktChannelParm02();
       showLogMessage("I","","selectMktChannelgpList skip cnt = " + gpListCnt);
   }

   if (pSeqno.length()==0) finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void  selectPtrBusinday() throws Exception
 {
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
 
 int selectMktChannelParm02() throws Exception
 {
  extendField = "parm2.";
  daoTable  = "mkt_channel_parm";
  whereStr  = "WHERE feedback_date     = '' "
            + "and   cal_def_date      = ? "
            + "and   feedback_apr_date = '' "
            ;

  setString(1 , businessDate);

  parmCnt = selectTable();

  for (int inti=0;inti<parmCnt;inti++)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I",""," 符合之活動(parm2.):["+getValue("parm2.active_code",inti)
                            + "] 名稱:["+getValue("parm2.active_name",inti)
                            + "] 回饋方式:["+getValue("parm2.feedback_key_sel",inti)+"]");
       selectMktChannelListForAcCode(getValue("parm2.active_code",inti), getValue("parm2.feedback_key_sel", inti));
       }
return 0;
 }
 
// int  selectIdNo(int inti) throws Exception
// {
//  extendField = "spec.";
//  selectSQL = "cash_value";
//  daoTable  = "crd_idno";
//  whereStr  = "where id_p_seqno     = ? "
//            ;
//
//  setString(1 , getValue("parm.active_code",inti));
//
//  int recCnt = selectTable();
//
//  if ( notFound.equals("Y") ) return (1);
//
//  return(0);
// }
 
 int deleteMktChannelgpList(String  activeGroupId) throws Exception {
	 int deleteCnt = 0;
	 //sqlCmd = "select count(*) list_cnt from MKT_CHANNELGP_LIST where ACTIVE_GROUP_ID = ? and P_SEQNO = ? and FUND_DATE = ? and ID_P_SEQNO = ? and CARD_NO = ? ";
	  sqlCmd = "select count(*) list_cnt from MKT_CHANNELGP_LIST where ACTIVE_GROUP_ID = ? and FUND_DATE = ? ";
	  setString(1,activeGroupId);
	  //setString(2,pSeqno); 
	  setString(2,businessDate); 
	  //setString(4,idPSeqno); 
	  //setString(5,cardNo); 
	  selectTable();
	  if(getValueInt("list_cnt") > 0) {
	      daoTable = "MKT_CHANNELGP_LIST";
	      //whereStr = "where ACTIVE_GROUP_ID = ? and P_SEQNO = ? and FUND_DATE = ? and ID_P_SEQNO = ? and CARD_NO = ? ";
	      whereStr = "where ACTIVE_GROUP_ID = ? and FUND_DATE = ? ";      
	      setString(1,activeGroupId);
	      //setString(2,pSeqno); 
	      setString(2,businessDate); 
	      //setString(4,idPSeqno); 
	      //setString(5,cardNo); 
	      deleteCnt = deleteTable();
	  }
	  return deleteCnt;
 }

 //************************************************************************
 int insertMktChannelgpList(String  pSeqno,String  idPSeqno, String  cardNo, String feedbackType, String  fundCode, String  activeGroupId,
 String  idNo,double fundAmtFinal) throws Exception
 {
	 
  dateTime();
//  showLogMessage("I","","新增 Mkt Channelgp List 數據=========================================");
//  showLogMessage("I",""," 通路活動群組代號:["+activeGroupId
//                       + "] 金額:["+fundAmtFinal+"]");
  setValue("mcli.ACTIVE_GROUP_ID", activeGroupId);
  setValue("mcli.P_SEQNO", pSeqno);
  setValue("mcli.ID_P_SEQNO", idPSeqno);
  setValue("mcli.CARD_NO", cardNo);
  setValue("mcli.ID_NO", idNo);
  setValue("mcli.FUND_CODE", fundCode);
  setValueDouble("mcli.FUND_AMT", fundAmtFinal);
  setValue("mcli.FUND_DATE", businessDate);
  setValue("mcli.PROC_DATE", "");
  setValue("mcli.PROC_FLAG", "");
  setValue("mcli.MOD_TIME", sysDate+sysTime);
  setValue("mcli.MOD_PGM", javaProgram);
  setValue("mcli.FEEDBACK_TYPE", feedbackType);	//增feedbacktype
  extendField = "mcli.";
  daoTable  = "MKT_CHANNELGP_LIST";

  insertTable();
  //showLogMessage("I","","新增 Mkt Channelgp List 數據 done=========================================");
  if ( dupRecord.equals("Y") ) return(1);

  return(0);
 }
 
//************************************************************************
int selectMktChannelListForAcCode(String activeCode , String feedbackKeySel) throws Exception
{
	extendField = "channel_list.";
    sqlCmd = "select ";
    sqlCmd += " a.p_seqno, a.id_p_seqno, a.card_no, a.fund_code, a.fund_amt ";
    sqlCmd += " ,b.active_group_id, b.limit_amt, b.feedback_type ,b.sum_amt ";
    sqlCmd += " ,c.id_no ";
    sqlCmd += "from mkt_channel_list a ";
    sqlCmd += "inner join ( select e.ACTIVE_GROUP_ID,e.feedback_type , e.ACTIVE_GROUP_DESC,e.LIMIT_AMT ,f.ACTIVE_CODE ,e.sum_amt "
    		+ "FROM MKT_CHANNELGP_PARM e, MKT_CHANNELGP_DATA f "
    		+ "WHERE e.ACTIVE_GROUP_ID=f.ACTIVE_GROUP_ID "
    		+ ") b on a.active_code = b.active_code  ";
    sqlCmd += "left join crd_idno  c on a.id_p_seqno = c.id_p_seqno  ";
    sqlCmd += "where a.active_code =  ? ";


//   extendField = "channellgpdata.";
//   selectSQL  = " a.p_seqno, a.id_p_seqno, a.card_no, a.fund_code, a.fund_amt "
//    		  + " ,b.active_group_id, b.limit_amt "
//   			  + " ,c.id_no ";
//   daoTable  = " mkt_channel_list a, mkt_channelgp_parm b, crd_idno c, MKT_CHANNELGP_DATA e ";
//   whereStr  = "where a.active_code  = ?  "
//		   	 + "and a.id_p_seqno = c.id_p_seqno  "
//    		 + "and (b.ACTIVE_GROUP_ID=e.ACTIVE_GROUP_ID   and e.active_code=a.active_code)";

    setString(1 , activeCode);
    int parmCnt =  selectTable();
    showLogMessage("I","","selectMktChannelListForAcCode Cnt = " + parmCnt);
//    BigDecimal  sum = new BigDecimal (0);
//    BigDecimal  limitAmt = new BigDecimal (0); 
//    BigDecimal  maxAmt = new BigDecimal (0); 
    double fundAmtFinal = 0; 
    String  pSeqno = "";
    String  idPSeqno =  "";
    String  cardNo = "";
    String  fundCode =  "";
    String  activeGroupId = "";
    String  idNo =  "";
    String  feedbackType = "";
    int sumAmt = 0;
    double limitAmt = 0;
    double sumDestAmt = 0;
    double maxFundAmt = 0;
    double sumFundAmt = 0;
    double[] arrayAmt = new double[3];
//    for (int i = 0; i <parmCnt ; i++) {
//    	String  fundAmt = getValue("fund_amt", i);
//    	if (maxAmt.compareTo(new BigDecimal (fundAmt)) == -1) {
//    		maxAmt = new BigDecimal (fundAmt);
//    	}
//    	sum = sum.add(new BigDecimal (fundAmt));
//    }
    for (int i = 0; i <parmCnt ; i++) {
 
//    	limitAmt = new BigDecimal (getValueInt("limit_amt", i) );
    	limitAmt = getValueDouble("channel_list.limit_amt",i);
        pSeqno = getValue("channel_list.p_seqno", i);
        idPSeqno = getValue("channel_list.id_p_seqno", i);
        cardNo = getValue("channel_list.card_no", i);
        fundCode = getValue("channel_list.fund_code", i);
        activeGroupId = getValue("channel_list.active_group_id", i);
        idNo = getValue("channel_list.id_no", i);
        feedbackType = getValue("channel_list.feedback_type", i);
        sumAmt = getValueInt("channel_list.sum_amt", i);
        //依參數各別取得不同累計金額
        arrayAmt = selectMktChannelList(activeGroupId ,idPSeqno ,pSeqno ,cardNo ,feedbackKeySel);
        if(arrayAmt == null)
        	continue;
        sumDestAmt = arrayAmt[0];
        sumFundAmt = arrayAmt[1];
        maxFundAmt = arrayAmt[2];
        if (sumAmt > 0) {
            if (sumDestAmt < sumAmt) {
                continue;
            }
        }
    	if ("1".equals(feedbackType)) {		//擇優回饋
    		fundAmtFinal = maxFundAmt;
    	}
    	if ("2".equals(feedbackType)) {		//回饋上限總金額
    		if (sumFundAmt > limitAmt) {
    	      	fundAmtFinal = limitAmt;
    	       }else {
    	        fundAmtFinal = sumFundAmt;
            }
    	}
    	if ("3".equals(feedbackType)) {		//擇一回饋
    		if(selectMktChannelgpListCnt(activeGroupId,feedbackKeySel,idNo,pSeqno,cardNo)>0) {
    			gpListCnt ++;
    			continue;
    		}
    	}
       if(activeGroupIdMap.get(activeGroupId)==null) {
    	   deleteMktChannelgpList(activeGroupId);
       }
       activeGroupIdMap.put(activeGroupId, activeGroupId);
       //int ii =  insertMktChannelgpList(pSeqno,idPSeqno,cardNo,fundCode,activeGroupId,idNo,fundAmtFinal);
       /*
       if (fundAmtFinal>0) {
    	   continue;	//以避免寫入mkt_channelgp_list 含有0者--> 造成擇優未寫入mkt_channelgp_list    	   
       }
       */
       int ii =  insertMktChannelgpList(pSeqno,idPSeqno,cardNo,feedbackType, fundCode,activeGroupId,idNo,fundAmtFinal);	//增feedbacktype
       
       /*
       if (ii>0) {
           showLogMessage("I","","insertMktChannelgpList dupRecord ,ACTIVE_GROUP_ID = [" + activeGroupId + "] ,P_SEQNO = [" + pSeqno + "]");
       }
       */
    }
return 0;
}

/***
 * 
 * @param activeGroupId
 * @param idPSeqno
 * @param pSeqno
 * @param cardNo
 * @param feedbackKeySel
 * @return sum_dest_amt,sum_fund_amt,max_fund_amt
 * @throws Exception
 */
    private double[] selectMktChannelList(String activeGroupId, String idPSeqno ,String pSeqno ,String cardNo ,String feedbackKeySel) throws Exception {
    	extendField = "list_data.";
    	selectSQL = " sum(c.sum_dest_amt) as sum_dest_amt , sum(a.fund_amt) as sum_fund_amt ,max(a.fund_amt) as max_fund_amt ";
    	daoTable = " mkt_channel_list a, MKT_CHANNELGP_DATA b ";
    	daoTable += " left join ( select sum(dest_amt) as sum_dest_amt ,active_code from  MKT_CHANNEL_BILL ";
    	whereStr = " where a.active_code = b.active_code ";
    	whereStr += " and b.active_group_id = ? ";
    	
    	
        //回饋方式 (1.ID, 2.帳戶, 3.正卡, 4.卡號)
    	switch(feedbackKeySel) {	
    	case "1" :
    		whereStr += " and a.id_p_seqno = ? ";
    		daoTable += " where id_p_seqno = ? ";
    		setString(1 , idPSeqno);
    		setString(2 , activeGroupId);
    		setString(3 , idPSeqno);
    		break;
    	case "2" :
    		whereStr += " and a.p_seqno = ? ";
    		daoTable += " where p_seqno = ? ";
    		setString(1 , pSeqno);
    		setString(2 , activeGroupId);
    		setString(3 , pSeqno);
    		break;	
    	case "3" :
    	case "4" :
    		whereStr += " and a.card_no = ? ";
    		daoTable += " where card_no = ? ";
    		setString(1 , cardNo);
    		setString(2 , activeGroupId);
    		setString(3 , cardNo);
    		break;
    	default:
    		setString(1 , activeGroupId);
    		break;
    	}

    	daoTable += " group by active_code ) c on a.active_code = c.ACTIVE_CODE ";
        int n = selectTable();
        
        if(n==0) {
        	return null;
        }

        double[] arrayAmt = {getValueDouble("list_data.sum_dest_amt"),getValueDouble("list_data.sum_fund_amt"),getValueDouble("list_data.max_fund_amt")};
        
        return arrayAmt;
    }
    

// ************************************************************************
 int selectMktChannelParm() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_channel_parm";
  whereStr  = "WHERE feedback_date     = '' "
            + "and   cal_def_date      = ? "
            + "and   feedback_apr_date = '' "
            ;

  setString(1 , businessDate);

  if (activeCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and  active_code = ? "; 
      setString(2 , activeCode);
     }

  parmCnt = selectTable();


  for (int inti=0;inti<parmCnt;inti++)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","符合之活動:["+getValue("parm.active_code",inti)
                            + "] 名稱:["+getValue("parm.active_name",inti)+"]");
       showLogMessage("I","","=========================================");
       showLogMessage("I","","  重複執行刪除資料");
       deleteMktChannelList(inti);
       deleteMktChannelAnal(inti);
       showLogMessage("I","","=========================================");
       showLogMessage("I","","  判斷是否有VD");
       acctTypeSel[inti] = selectMktBnDataVd(inti);
       if (acctTypeSel[inti]==0) acctTypeSel[inti]=3;
       showLogMessage("I","","=========================================");
       if (getValue("parm.record_cond",inti).equals("Y"))
          {
           selectMktChanrecParm(inti);
           continue;
          }

       activeSeq      = "00";
       recordGroupNo = "";
       feedbackLimit[0] = getValueDouble("parm.b_feedback_limit",inti);
       feedbackLimit[1] = getValueDouble("parm.f_feedback_limit",inti);
       feedbackLimit[2] = getValueDouble("parm.s_feedback_limit",inti);
       feedbackLimit[3] = getValueDouble("parm.l_feedback_limit",inti);

       purchaseAmtS[0] = getValueDouble("parm.purchase_amt_s1",inti);
       purchaseAmtS[1] = getValueDouble("parm.purchase_amt_s2",inti);
       purchaseAmtS[2] = getValueDouble("parm.purchase_amt_s3",inti);
       purchaseAmtS[3] = getValueDouble("parm.purchase_amt_s4",inti);
       purchaseAmtS[4] = getValueDouble("parm.purchase_amt_s5",inti);

       purchaseAmtE[0] = getValueDouble("parm.purchase_amt_e1",inti);
       purchaseAmtE[1] = getValueDouble("parm.purchase_amt_e2",inti);
       purchaseAmtE[2] = getValueDouble("parm.purchase_amt_e3",inti);
       purchaseAmtE[3] = getValueDouble("parm.purchase_amt_e4",inti);
       purchaseAmtE[4] = getValueDouble("parm.purchase_amt_e5",inti);

       feedbackRate[0]  = getValueDouble("parm.feedback_rate_1",inti);
       feedbackRate[1]  = getValueDouble("parm.feedback_rate_2",inti);
       feedbackRate[2]  = getValueDouble("parm.feedback_rate_3",inti);
       feedbackRate[3]  = getValueDouble("parm.feedback_rate_4",inti);
       feedbackRate[4]  = getValueDouble("parm.feedback_rate_5",inti);

       feedbackAmt[0]   = getValueDouble("parm.feedback_amt_1",inti);
       feedbackAmt[1]   = getValueDouble("parm.feedback_amt_2",inti);
       feedbackAmt[2]   = getValueDouble("parm.feedback_amt_3",inti);
       feedbackAmt[3]   = getValueDouble("parm.feedback_amt_4",inti);
       feedbackAmt[4]   = getValueDouble("parm.feedback_amt_5",inti);

       feedbackLmtAmt[0]   = getValueDouble("parm.feedback_lmt_amt_1",inti);
       feedbackLmtAmt[1]   = getValueDouble("parm.feedback_lmt_amt_2",inti);
       feedbackLmtAmt[2]   = getValueDouble("parm.feedback_lmt_amt_3",inti);
       feedbackLmtAmt[3]   = getValueDouble("parm.feedback_lmt_amt_4",inti);
       feedbackLmtAmt[4]   = getValueDouble("parm.feedback_lmt_amt_5",inti);

       feedbackLmtCnt[0]   = getValueDouble("parm.feedback_lmt_cnt_1",inti);
       feedbackLmtCnt[1]   = getValueDouble("parm.feedback_lmt_cnt_2",inti);
       feedbackLmtCnt[2]   = getValueDouble("parm.feedback_lmt_cnt_3",inti);
       feedbackLmtCnt[3]   = getValueDouble("parm.feedback_lmt_cnt_4",inti);
       feedbackLmtCnt[4]   = getValueDouble("parm.feedback_lmt_cnt_5",inti);

       procInitRank(inti);
      }

  return(0);
 }
// ************************************************************************
 int selectMktChanrecParm(int inti) throws Exception
 {
  extendField = "pseq.";
  daoTable  = "mkt_chanrec_parm";
  whereStr  = "WHERE active_code   = ?  "
            + "order by active_seq "
            ;

  setString(1 , getValue("parm.active_code",inti));

  precCnt = selectTable();

  for (int intm=0;intm<precCnt;intm++)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","  活動序號 ["+getValue("pseq.active_seq"  ,intm)+"]");
       activeSeq      = getValue("pseq.active_seq"  ,intm);
       recordGroupNo = getValue("pseq.record_group_no" ,intm);
        
       feedbackLimit[0] = getValueDouble("pseq.b_feedback_limit",intm);
       feedbackLimit[1] = getValueDouble("pseq.f_feedback_limit",intm);
       feedbackLimit[2] = getValueDouble("pseq.s_feedback_limit",intm);
       feedbackLimit[3] = getValueDouble("pseq.l_feedback_limit",intm);

       purchaseAmtS[0] = getValueDouble("pseq.purchase_amt_s1",intm);
       purchaseAmtS[1] = getValueDouble("pseq.purchase_amt_s2",intm);
       purchaseAmtS[2] = getValueDouble("pseq.purchase_amt_s3",intm);
       purchaseAmtS[3] = getValueDouble("pseq.purchase_amt_s4",intm);
       purchaseAmtS[4] = getValueDouble("pseq.purchase_amt_s5",intm);

       purchaseAmtE[0] = getValueDouble("pseq.purchase_amt_e1",intm);
       purchaseAmtE[1] = getValueDouble("pseq.purchase_amt_e2",intm);
       purchaseAmtE[2] = getValueDouble("pseq.purchase_amt_e3",intm);
       purchaseAmtE[3] = getValueDouble("pseq.purchase_amt_e4",intm);
       purchaseAmtE[4] = getValueDouble("pseq.purchase_amt_e5",intm);

       feedbackRate[0]  = getValueDouble("pseq.feedback_rate_1",intm);
       feedbackRate[1]  = getValueDouble("pseq.feedback_rate_2",intm);
       feedbackRate[2]  = getValueDouble("pseq.feedback_rate_3",intm);
       feedbackRate[3]  = getValueDouble("pseq.feedback_rate_4",intm);
       feedbackRate[4]  = getValueDouble("pseq.feedback_rate_5",intm);

       feedbackAmt[0]   = getValueDouble("pseq.feedback_amt_1",intm);
       feedbackAmt[1]   = getValueDouble("pseq.feedback_amt_2",intm);
       feedbackAmt[2]   = getValueDouble("pseq.feedback_amt_3",intm);
       feedbackAmt[3]   = getValueDouble("pseq.feedback_amt_4",intm);
       feedbackAmt[4]   = getValueDouble("pseq.feedback_amt_5",intm);

       feedbackLmtAmt[0]   = getValueDouble("pseq.feedback_lmt_amt_1",intm);
       feedbackLmtAmt[1]   = getValueDouble("pseq.feedback_lmt_amt_2",intm);
       feedbackLmtAmt[2]   = getValueDouble("pseq.feedback_lmt_amt_3",intm);
       feedbackLmtAmt[3]   = getValueDouble("pseq.feedback_lmt_amt_4",intm);
       feedbackLmtAmt[4]   = getValueDouble("pseq.feedback_lmt_amt_5",intm);

       feedbackLmtCnt[0]   = getValueDouble("pseq.feedback_lmt_cnt_1",intm);
       feedbackLmtCnt[1]   = getValueDouble("pseq.feedback_lmt_cnt_2",intm);
       feedbackLmtCnt[2]   = getValueDouble("pseq.feedback_lmt_cnt_3",intm);
       feedbackLmtCnt[3]   = getValueDouble("pseq.feedback_lmt_cnt_4",intm);
       feedbackLmtCnt[4]   = getValueDouble("pseq.feedback_lmt_cnt_5",intm);

       procInitRank(inti);
      }

  return(0);
 }
// ************************************************************************
 int procInitRank(int inti) throws Exception
 {
  activeSeqCnt[0] = 0;
  activeSeqCnt[1] = 0;
  activeSeqCnt[2] = 0;
  activeSeqCnt[3] = 0;

  activeSeqAmt[0] = 0;
  activeSeqAmt[1] = 0;
  activeSeqAmt[2] = 0;
  activeSeqAmt[3] = 0;

  showLogMessage("I","","=========================================");
  showLogMessage("I","","  處理 ins [mkt_channel_list]");
  selectMktChannelRank1(inti);
  showLogMessage("I","","  處理 ins [mkt_channel_anal]");
  selectMktChannelRank2(inti);  

  return(0);
 }
// ************************************************************************
 void selectMktChannelRank1(int inti) throws Exception
 {
  if (getValue("parm.feedback_key_sel",inti).equals("1"))
      selectSQL  = "id_no,"
                 + "sum(decode(active_type,'1',rank_cnt,0)) as bonus_cnt,"
                 + "sum(decode(active_type,'1',rank_amt,0)) as bonus_pnt,"
                 + "sum(decode(active_type,'2',rank_cnt,0)) as fund_cnt,"
                 + "sum(decode(active_type,'2',rank_amt,0)) as fund_amt,"
                 + "sum(decode(active_type,'3',rank_cnt,0)) as gift_cnt,"
                 + "sum(decode(active_type,'3',rank_amt,0)) as gift_int,"
                 + "sum(decode(active_type,'4',rank_cnt,0)) as lottery_cnt,"
                 + "sum(decode(active_type,'4',rank_amt,0)) as lottery_int";
  else
  if (getValue("parm.feedback_key_sel",inti).equals("2"))
      selectSQL  = "p_seqno,"
                 + "vd_flag,"
                 + "sum(decode(active_type,'1',rank_cnt,0)) as bonus_cnt,"
                 + "sum(decode(active_type,'1',rank_amt,0)) as bonus_pnt,"
                 + "sum(decode(active_type,'2',rank_cnt,0)) as fund_cnt,"
                 + "sum(decode(active_type,'2',rank_amt,0)) as fund_amt,"
                 + "sum(decode(active_type,'3',rank_cnt,0)) as gift_cnt,"
                 + "sum(decode(active_type,'3',rank_amt,0)) as gift_int,"
                 + "sum(decode(active_type,'4',rank_cnt,0)) as lottery_cnt,"
                 + "sum(decode(active_type,'4',rank_amt,0)) as lottery_int";
  else
  if ((getValue("parm.feedback_key_sel",inti).equals("3"))||
      (getValue("parm.feedback_key_sel",inti).equals("4")))
      selectSQL  = "card_no,"
                 + "max(p_seqno) as p_seqno,"
                 + "sum(decode(active_type,'1',rank_cnt,0)) as bonus_cnt,"
                 + "sum(decode(active_type,'1',rank_amt,0)) as bonus_pnt,"
                 + "sum(decode(active_type,'2',rank_cnt,0)) as fund_cnt,"
                 + "sum(decode(active_type,'2',rank_amt,0)) as fund_amt,"
                 + "sum(decode(active_type,'3',rank_cnt,0)) as gift_cnt,"
                 + "sum(decode(active_type,'3',rank_amt,0)) as gift_int,"
                 + "sum(decode(active_type,'4',rank_cnt,0)) as lottery_cnt,"
                 + "sum(decode(active_type,'4',rank_amt,0)) as lottery_int";

  daoTable  = "mkt_channel_rank";
  whereStr  = "where active_code  = ? "
            + "and   active_seq   = ? "
            ;

  setString(1 , getValue("parm.active_code",inti));
  setString(2 , activeSeq);

  if (getValue("parm.feedback_key_sel",inti).equals("1"))
     {
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and  id_no      = ? "; 
          setString(3 , pSeqno);
         }
      whereStr  = whereStr
                + "group by id_no "
                ;
     }
  if (getValue("parm.feedback_key_sel",inti).equals("2"))
     {
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and  p_seqno    = ? "; 
          setString(3 , pSeqno);
         }
      whereStr  = whereStr
                + "group by vd_flag,p_seqno "
                ;
     }
  if ((getValue("parm.feedback_key_sel",inti).equals("3"))||
      (getValue("parm.feedback_key_sel",inti).equals("4")))
     {
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and  card_no    = ? "; 
          setString(3 , pSeqno);
         }
      whereStr  = whereStr
                + "group by card_no "
                ;
     }

  openCursor();

  int cnt1=0;
  int retFlag=0;
  int retTest=0;
  while( fetchTable() ) 
   {
    totalCnt++;
    retFlag=0;

    setValue("list.vd_flag"    , "N");
    setValue("list.card_no"    , "");

    if (getValue("parm.feedback_key_sel",inti).equals("1"))
       {
        if (selectCrdCard1()!=0)
           if (selectDbcCard1()!=0) retFlag=1; 
       }
    else if (getValue("parm.feedback_key_sel",inti).equals("2"))
       {
        if (getValue("vd_flag").equals("N"))
           if (selectCrdCard2()!=0) retFlag=1;

        if (getValue("vd_flag").equals("Y"))
           if (selectDbcCard2()!=0) retFlag=1;
       }
    //else if (getValue("parm.feedback_key_sel",inti).equals("3"))	//20230703, allen指導
    else
       {
        if (selectCrdCard3()!=0)
           if (selectDbcCard3()!=0) retFlag=1;
       }
    if (retFlag==1)
       {
/*
        if (getValue("parm.feedback_key_sel",inti).equals("1"))
            showLogMessage("I","","無活卡  id_no :["+ getValue("ic_no")+"]");
        else if (getValue("parm.feedback_key_sel",inti).equals("2"))
            showLogMessage("I","","無活卡  p_seqno  :["+ getValue("p_seqno") +"]-["+acct_type_sel[inti]+"]");   
        else if (getValue("parm.feedback_key_sel",inti).equals("3"))
            showLogMessage("I","","無活卡  card_no "+ getValue("card_no")+"]");
*/
        deleteMktChannelRank(inti);   
        continue;
       }
    if ((getValue("parm.feedback_key_sel",inti).equals("3"))||
        (getValue("parm.feedback_key_sel",inti).equals("4")))
       setValue("list.card_no"    , getValue("card_no"));

    setValue("list.acct_no"    , getValue("card.acct_no"));
    setValue("list.acct_type"  , getValue("card.acct_type"));
    setValue("list.p_seqno"    , getValue("card.p_seqno"));
    setValue("list.id_p_seqno" , getValue("card.id_p_seqno"));
    setValue("list.stmt_cycle" , getValue("card.stmt_cycle"));
    
    //showLogMessage("I","","Before selectVmktAcctType(), card_no: ["+ getValue("card_no")+"], card.acct_type: ["+getValue("card.acct_type")+"]");
    selectVmktAcctType();
     
    setValue("vd_flag" , getValue("vmkt.vd_flag"));
   
    if ((getValueDouble("bonus_pnt") > feedbackLimit[0])&&
        (feedbackLimit[0] !=0))
        setValueDouble("bonus_pnt" , feedbackLimit[0]);

    if ((getValueDouble("fund_amt")  > feedbackLimit[1])&&
        (feedbackLimit[1] !=0))
        setValueDouble("fund_amt"  , feedbackLimit[1]);
         
    if ((getValueDouble("gift_int")  > feedbackLimit[2])&&
        (feedbackLimit[2] !=0))
        setValueDouble("gift_int"  , feedbackLimit[2]);
         
    if ((getValueDouble("lottery_cnt")  > feedbackLimit[3])&&
        (feedbackLimit[3] !=0))
        setValueDouble("lottery_cnt"  , feedbackLimit[3]); 
   
    if (getValueDouble("bonus_pnt")<=0)
       {
        setValue("bonus_pnt","0");
        setValue("bonus_cnt","0");
       }
    if (getValueDouble("fund_amt")<0)
       {
        setValue("fund_amt","0");
        setValue("fund_cnt","0");
       }
    if (getValueDouble("gift_int")<0)
       {
        setValue("gift_amt","0");
        setValue("gift_cnt","0");
       }
    if (getValueDouble("lottery_int")<0)
       {
        setValue("lottery_int","0");
        setValue("lottery_cnt","0");
       }

    activeSeqCnt[0] = activeSeqCnt[0] + getValueInt("bonus_cnt");
    activeSeqCnt[1] = activeSeqCnt[1] + getValueInt("fund_cnt");
    activeSeqCnt[2] = activeSeqCnt[2] + getValueInt("gift_cnt");
    activeSeqCnt[3] = activeSeqCnt[3] + getValueInt("lottery_cnt");

    activeSeqAmt[0] = activeSeqAmt[0] + getValueDouble("bonus_pnt");
    activeSeqAmt[1] = activeSeqAmt[1] + getValueDouble("fund_amt");
    activeSeqAmt[2] = activeSeqAmt[2] + getValueDouble("gift_int");
    activeSeqAmt[3] = activeSeqAmt[3] + getValueDouble("lottery_int");

    setValue("list.bonus_pnt"            , getValue("bonus_pnt"));
    setValue("list.fund_amt"             , getValue("fund_amt"));
    setValue("list.gift_int"             , getValue("gift_int"));
    setValue("list.lottery_int"          , getValue("lottery_int"));

    setValue("list.bonus_type"           , "");
    if (getValueInt("bonus_pnt")!=0)
       setValue("list.bonus_type"        , getValue("parm.bonus_type",inti));
    setValue("list.fund_code"            , "");
    if (getValueInt("fund_amt")!=0)
       setValue("list.fund_code"         , getValue("parm.fund_code",inti));
    setValue("list.lottery_type"        , "");
    if (getValueInt("lottery_int")!=0)
       setValue("list.lottery_type"      , getValue("parm.lottery_type",inti));

    setValue("list.gift_type"        , "");
    setValue("list.vendor_no"        , "");
    setValue("list.gift_amt"         , "0");
    setValue("list.spec_gift_no"     , "");
    if (getValueInt("gift_int")!=0)
       {
        selectMktSpecGift(inti);
        setValue("list.gift_type"        , getValue("spec.gift_type"));
        setValue("list.vendor_no"        , getValue("spec.vendor_no"));
        setValueDouble("list.gift_amt"   , getValueInt("gift_int")*getValueDouble("spec.cash_value"));
        setValue("list.spec_gift_no"     , getValue("parm.spec_gift_no",inti));
       }

    if (insertMktChannelList(inti)!=0)
       {
    	//showLogMessage("I","","insertMktChannelList(inti)!=0 ==> parm.feedback_key_sel: ["+getValue("parm.feedback_key_sel",inti)+ "], card.p_seqno: ["+getValue("card.p_seqno")+"], card_no: ["+ getValue("card_no")+"], card.acct_type: ["+getValue("card.acct_type")+"]");
        deleteMktChannelRank(inti);

        activeSeqCnt[0] = activeSeqCnt[0] - getValueInt("bonus_cnt");
        activeSeqCnt[1] = activeSeqCnt[1] - getValueInt("fund_cnt");
        activeSeqCnt[2] = activeSeqCnt[2] - getValueInt("gift_cnt");
        activeSeqCnt[3] = activeSeqCnt[3] - getValueInt("lottery_cnt");

        activeSeqAmt[0] = activeSeqAmt[0] - getValueDouble("bonus_pnt");
        activeSeqAmt[1] = activeSeqAmt[1] - getValueDouble("fund_amt");
        activeSeqAmt[2] = activeSeqAmt[2] - getValueDouble("gift_int");
        activeSeqAmt[3] = activeSeqAmt[3] - getValueDouble("lottery_int");
       }


    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 void selectMktChannelRank2(int inti) throws Exception
 {
  for (int intm=1;intm<=5;intm++)
     {
      setValue("anal.active_type_"+intm       , "");
      setValueDouble("anal.purchase_amt_s"+intm     , purchaseAmtS[intm-1]);
      setValueDouble("anal.purchase_amt_e"+intm     , purchaseAmtE[intm-1]);
      setValueDouble("anal.feedback_rate_"+intm     , feedbackRate[intm-1]);
      setValueDouble("anal.feedback_amt_"+intm      , feedbackAmt[intm-1]);
      setValueDouble("anal.feedback_lmt_cnt_"+intm  , feedbackLmtCnt[intm-1]);
      setValueDouble("anal.feedback_lmt_amt_"+intm  , feedbackLmtAmt[intm-1]);
      setValue("anal.rank_cnt_"+intm          , "0");
      setValue("anal.rank_amt_"+intm          , "0");
      setValue("anal.feedback_value_"+intm    , "0");

     }

  selectSQL  = "rank_seq,"
//selectSQL  = "active_seq,"
             + "max(active_type) as active_type,"
             + "sum(rank_cnt) as rank_cnt,"
             + "sum(rank_amt) as rank_amt";
  daoTable  = "mkt_channel_rank";
  whereStr  = "where active_code  = ?  "
            + "and   active_seq   = ? "
//          + "group by aactibe_seq "
            + "group by rank_seq "
            ;

  setString(1 , getValue("parm.active_code",inti));
  setString(2 , activeSeq);

  openCursor();

  int cnt1=0;
  while( fetchTable() ) 
   {
    setValue("anal.active_type_"+(getValueInt("rank_seq")+1)  , getValue("active_type"));
    setValue("anal.rank_cnt_"+(getValueInt("rank_seq")+1)     , getValue("rank_cnt"));
    setValue("anal.rank_amt_"+(getValueInt("rank_seq")+1)     , getValue("rank_amt"));
    if (getValue("active_type").equals("3"))
       {
        selectMktSpecGift(inti);
        setValue("anal.feedback_value_"+(getValueInt("rank_seq")+1)  , getValue("spec.cash_value"));
       }
   }

  setValueInt("anal.bonus_cnt"        , activeSeqCnt[0]);
  setValueInt("anal.fund_cnt"         , activeSeqCnt[1]);
  setValueInt("anal.gift_cnt"         , activeSeqCnt[2]);
  setValueInt("anal.lottery_cnt"      , activeSeqCnt[3]);

  setValueDouble("anal.bonus_pnt"     , activeSeqAmt[0]);
  setValueDouble("anal.fund_amt"      , activeSeqAmt[1]);
  setValueDouble("anal.gift_int"      , activeSeqAmt[2]);
  setValueDouble("anal.lottery_int"   , activeSeqAmt[3]);

  if (getValueInt("anal.gift_int")!=0)
     {
      selectMktSpecGift(inti);
//    setValue("anal.feedback_cacsh_value"  , getValue("spec.cash_value"));
      setValueDouble("anal.gift_amt"        , getValueInt("anal.gift_int")*getValueDouble("spec.cash_value"));
     }
  insertMktChannelAnal(inti);
  closeCursor();
 }
// ************************************************************************
 int insertMktChannelList(int inti) throws Exception
 {
  dateTime();

  setValue("list.ecoupon_date_s"       , getValue("parm.ecoupon_date_s",inti));
  setValue("list.ecoupon_date_e"       , comm.nextMonthDate(getValue("parm.ecoupon_date_s",inti)
                                       , getValueInt("spec.effect_months")));
  setValue("list.active_code"          , getValue("parm.active_code",inti));
  setValue("list.cal_def_date"          , getValue("parm.cal_def_date",inti));	//add by grace
  setValue("list.mod_time"             , sysDate+sysTime);
  setValue("list.mod_pgm"              , javaProgram);

  extendField = "list.";
  daoTable  = "mkt_channel_list";

  insertTable();

  if ( dupRecord.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int deleteMktChannelRank(int inti) throws Exception
 {
  daoTable  = "mkt_channel_rank";
  whereStr  = "where active_code  = ? "
            + "and   active_seq   = ? "
            ;
  setString(1 , getValue("parm.active_code",inti));
  setString(2 , activeSeq);

  if (getValue("parm.feedback_key_sel",inti).equals("1"))
     {
      whereStr  = whereStr
                + "and id_no = ? ";
      setString(3 , getValue("id_no"));
     }
  if (getValue("parm.feedback_key_sel",inti).equals("2"))
     {
      whereStr  = whereStr
                + "and  vd_flag = ? "
                + "and  p_seqno = ? ";
      setString(3 , getValue("vd_flag"));
      setString(4 , getValue("p_seqno"));
     }
  if ((getValue("parm.feedback_key_sel",inti).equals("3"))||
      (getValue("parm.feedback_key_sel",inti).equals("4")))
     {
      whereStr  = whereStr
                + "and  card_no = ? ";
      setString(3 , getValue("card_no"));
     }

  int recCnt = deleteTable();

  if (recCnt==0)
     {
      showLogMessage("I","","delet mkt_channel_rank error: ["+ getValue("parm.active_code",inti) +"]");
      exitProgram(1);
     }

  return(0);
 }
// ************************************************************************
 int insertMktChannelAnal(int inti) throws Exception
 {
  extendField = "anal.";

  setValue("anal.active_code"          , getValue("parm.active_code",inti));
  setValue("anal.active_seq"           , activeSeq); 
  setValue("anal.record_group_no"      , recordGroupNo); 
  setValue("anal.crt_date"             , getValue("parm.cal_def_date",inti)); 
  setValue("anal.crt_user"             , javaProgram);  
  setValue("anal.apr_flag"             , "N"); 
  setValue("anal.mod_time"             , sysDate+sysTime);
  setValue("anal.mod_pgm"              , javaProgram);

  daoTable  = "mkt_channel_anal";

  insertTable();

  return(0);
 }
// ************************************************************************
 int  selectMktSpecGift(int inti) throws Exception
 {
  extendField = "spec.";
  selectSQL = "cash_value,"
            + "vendor_no,"
            + "effect_months,"
            + "gift_type";
  daoTable  = "mkt_spec_gift";
  whereStr  = "where gift_group     = '2' "
            + "and  gift_no         = ? "
            ;

  setString(1 , getValue("parm.spec_gift_no",inti));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return (1);

  return(0);
 }
// ************************************************************************
 int  selectCrdCard1() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.p_seqno,"
            + "acct_type,"
            + "max('' ) as acct_no,"
            + "max(a.major_id_p_seqno) as id_p_seqno,"
            + "max(a.stmt_cycle) as stmt_cycle,"
            + "a.acct_type";
  daoTable  = "crd_card a,crd_idno b";
  whereStr  = "where a.current_code = '0' "
            + "and   a.major_id_p_seqno   = b.id_p_seqno "
            + "and   b.id_no        = ? "
            + "and   a.acct_type not in ('05','06','03') "
            + "group by a.p_seqno,a.acct_type "  
            + "order by  a.acct_type " 
            ;

  setString(1 , getValue("id_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return (1);

  setValue("list.vd_flag"    , "N");
  return(0);
 }
// ************************************************************************
 int  selectDbcCard1() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.p_seqno,"
            + "a.acct_type,"
            + "max(a.acct_no) as acct_no,"
            + "max(a.id_p_seqno) as id_p_seqno,"
            + "max(a.stmt_cycle) as stmt_cycle,"
            + "a.acct_type";
  daoTable  = "dbc_card a,dbc_idno b";
  whereStr  = "where a.current_code = '0' "
            + "and   a.id_p_seqno   = b.id_p_seqno "
            + "and   b.id_no        = ? "
            + "group by a.p_seqno,a.acct_type "  
            ;

  setString(1 , getValue("id_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return (1);

  setValue("list.vd_flag"    , "Y");
  return(0);
 }
// ************************************************************************
 int  selectCrdCard2() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.p_seqno,"
            + "acct_type,"
            + "max('' ) as acct_no,"
            + "max(a.major_id_p_seqno) as id_p_seqno,"
            + "max(a.stmt_cycle) as stmt_cycle,"
            + "a.acct_type";
  daoTable  = "crd_card a";
  whereStr  = "where a.current_code = '0' "
            + "and   a.p_seqno      = ? "
            + "and   a.acct_type not in ('05','06','03') "
            + "group by a.p_seqno,a.acct_type "  
            + "order by  a.acct_type "; 

  setString(1 , getValue("p_seqno"));

  selectTable();

  if ( notFound.equals("Y") ) return (1);

  setValue("list.vd_flag"    , "N");
  return(0);
 }
// ************************************************************************
 int  selectDbcCard2() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.p_seqno,"
            + "a.acct_type,"
            + "max(a.acct_no) as acct_no,"
            + "max(a.id_p_seqno) as id_p_seqno,"
            + "max(a.stmt_cycle) as stmt_cycle,"
            + "a.acct_type";
  daoTable  = "dbc_card a";
  whereStr  = "where a.current_code = '0' "
            + "and   a.p_seqno      = ? "
            + "group by a.p_seqno,a.acct_type "  
            ;

  setString(1 , getValue("p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return (1);
  setValue("list.vd_flag"    , "Y");

  return(0);
 }
// ************************************************************************
 int  selectCrdCard3() throws Exception
 {
  extendField = "card.";
  selectSQL = "p_seqno,"
            + "acct_type,"
            + "''  as acct_no,"
            + "major_id_p_seqno as id_p_seqno,"
            + "stmt_cycle,"
            + "acct_type";
  daoTable  = "crd_card";
  whereStr  = "where current_code = '0' "
            + "and   card_no      = ? "
            + "and   acct_type not in ('05','06','03') "
            ;

  setString(1 , getValue("card_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return (1);

  setValue("list.vd_flag"    , "N");

  return(0);
 }
// ************************************************************************
 int  selectDbcCard3() throws Exception
 {
  extendField = "card.";
  selectSQL = "p_seqno,"
            + "acct_no,"
            + "acct_no,"
            + "major_id_p_seqno as id_p_seqno,"
            + "stmt_cycle,"
            + "acct_type";
  daoTable  = "dbc_card a";
  whereStr  = "where current_code = '0' "
            + "and   card_no      = ? "
            ;

  setString(1 , getValue("card_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return (1);

  setValue("list.vd_flag"    , "Y");

  return(0);
 }
// ************************************************************************
 int selectMktBnDataVd(int inti) throws Exception
 {
  selectSQL = "max(decode(data_code,'90',1,0)) as vd_cnt1,"
            + "max(decode(data_code,'90',0,2)) as vd_cnt2";
  daoTable  = "mkt_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_CHANNEL_PARM' "
            + "and   data_key   = ?   "
            + "and   data_type  = '1' "
            ;

  setString(1 , getValue("parm.active_code",inti));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select mkt_bn_data_vd error!" );
      exitProgram(1);
     }

  return getValueInt("vd_cnt1") + getValueInt("vd_cnt2");
 }
// ************************************************************************
 int selectVmktAcctType() throws Exception
 {
  extendField = "vmkt.";
  selectSQL = "vd_flag";
  daoTable  = "vmkt_acct_type";
  whereStr  = "WHERE acct_type  = ? "; 

  setString(1 , getValue("card.acct_type"));

  selectTable();

  if ( notFound.equals("Y") )
     showLogMessage("I","","select_vmkt_acct_type error! p_awqno:[" + getValue("p_awqno")+"] --> card.acct_type: ["+getValue("card.acct_type")+"]" );

  return 0;
 }
// ************************************************************************
 int updateMktChannelList(int inti) throws Exception
 {
  daoTable  = "mkt_channel_list";
  updateSQL = "bonus_pnt      = bonus_pnt + ?,"
            + "fund_amt       = fund_amt  + ?,"
            + "gift_int       = gift_int  + ?,"
            + "lottery_int    = lottery_int  + ?,"
            + "gift_amt       = gift_amt  + ?";
  whereStr  = "where p_seqno  = ? "
            + "and   vd_flag  = ? "  
            + "and   active_code  = ? "  
            ;

  setInt(1    , getValueInt("list.bonus_pnt"));
  setDouble(2 , getValueDouble("list.fund_amt"));
  setInt(3    , getValueInt("list.gift_int"));
  setInt(4    , getValueInt("list.lottery_int"));
  setDouble(5 , getValueDouble("list.gift_amt"));
  setString(6 , getValue("p_seqno")); 
  setString(7 , getValue("vd_flag"));
  setString(8 , getValue("parm.active_code",inti));

  int n = updateTable();

  if ( notFound.equals("Y") ) return(1);

  return n;
 }
// ************************************************************************
 int deleteMktChannelList(int inti) throws Exception
 {
  daoTable  = "mkt_channel_list";
  whereStr  = "where  active_code   = ? "
		  	+ "and cal_def_date = ? ";

  setString(1 , getValue("parm.active_code",inti));
  setString(2 , getValue("parm.cal_def_date",inti));	//add by grace

  int recCnt = deleteTable();

  showLogMessage("I","","刪除 mkt_channel_list 筆數  : ["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 int deleteMktChannelAnal(int inti) throws Exception
 {
  daoTable  = "mkt_channel_anal";
  whereStr  = "where  active_code   = ? ";

  setString(1 , getValue("parm.active_code",inti)); 

  int recCnt = deleteTable();

  showLogMessage("I","","刪除 mkt_channel_anal 筆數  : ["+ recCnt +"]");

  return(0);
 }
 /***
  * 
  * @param activeGroupId 活動群組代號
  * @param feedbackKeySel 回饋方式
  * @param idNo
  * @param pSeqno
  * @param cardNo
  * @return
  * @throws Exception
  */
int selectMktChannelgpListCnt(String activeGroupId ,String feedbackKeySel,String idNo , String pSeqno ,String cardNo) throws Exception
{
extendField = "CHANNELGP_LIST.";
selectSQL = "count(*) as list_cnt ";
daoTable  = "MKT_CHANNELGP_LIST";
whereStr  = "WHERE active_group_id  = ? "; 
setString(1 , activeGroupId);

switch(feedbackKeySel) {
case "1":
	whereStr += " and id_no = ? ";
	setString(2,idNo);
	break;
case "2":
	whereStr += " and p_seqno = ? ";
	setString(2,pSeqno);
	break;
case "3":
case "4":
	whereStr += " and card_no = ? ";
	setString(2,cardNo);
	break;
}


selectTable();

return getValueInt("CHANNELGP_LIST.list_cnt");
}
}  // End of class FetchSample
