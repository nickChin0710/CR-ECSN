/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/09/15  V1.00.37  Allen Ho   mkt_C110                                   *
* 110/11/01  V1.01.09  Allen Ho   CR-1339                                    *
* 111/01/01  V1.02.01  Allen Ho   id_p_seqno=0002058474 id_no len =11        *
* 111/11/11  V1.02.02  Yang Bo    sync code from mega                        *
* 112/01/19  V1.02.03  Zuwei Su   Mktm0850 勾選[簽帳款(BL)] 且 [交易平台種類]*
*                                 勾選 “排除”者, 得檢核 mkt_bn_data資料,   *
*                                 剔除bil_bill/dbb_bill記錄                  *            
* 112/03/17  v1.02.04  machao     增 [非一般消費群組]、[通路類別]資料檢核                                                                                                                      *
* 112/04/07  v1.02.05  grace      bil_bill.acct_type not in ('03','05','06')                        *
* 112/06/01  v1.02.05  Zuwei Su   調整變數parmArr下標順序                                                                                                                                                  *
* 112/06/14  v1.02.06  Bo Yang    依[非一般消費群組]欄位的“指定”、“排除”進行資料檢核                                                                                              *
* 112/06/19  v1.02.07  Bo Yang    依[非一般消費群組]欄位的“指定”、“排除”進行資料檢核 fix                            *
* 112/06/20  v1.02.08  Grace Huang [特店群組]檢核調整                                                                                                                                                                *
* 112/07/17  v1.02.09  Ryan        修訂 “按月回饋”者, bil_bill/dbb_bill 之 資料篩選條件                                                                           *
* 112/08/18  V1.02.11  Allen Ho   Modify for refund                                                   *
*                      Grace      取消按月回饋(M)的 purchase_date 條件再設定 (cal_def_date, minDate2/maxDate2) *
* 112/09/13  V1.02.12  Grace      變更一般消費判讀                                                                                                                                                                           *
* 112/10/02  V1.02.13  Grace      增一般消費判讀, 含$, # 一碼者                                                                                                                                          *                    
* 112/10/16  V1.02.14  Ryan       增加(當期帳單(年月)) 之判斷處理                                                                                                                                          *
* 112/10/17  V1.02.15  Grace      檢核匯入名單時, 改以id_p_seqno欄位比對, 因部份名單包含有附卡ID資料                                                          *                    
* 112/11/20  V1.02.16  Zuwei Su   通路類別判斷邏輯修改                                                          *                    
*******************************************************************************************************/
package Mkt;

import com.*;
import java.util.ArrayList;

public class MktC110 extends AccessDAO
{
 private final String PROGNAME = "通路活動-消費篩選處理程式  112/11/20  V1.02.16";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommDate comDate = new CommDate();
 CommString comStr = new CommString();
 CommCol comCol = null;
 String hBusinessDate = "";
 String minDate = "";
 String maxDate = "";
 String minDate2 = "";
 String maxDate2 = "";
 boolean purchaseDateflag = false;
 String activeCode = "";
 String idPSeqno = "";
 ArrayList<String> acctMonths = new ArrayList<String>();

 long    totalCnt=0,updateCnt=0;
 int parmCnt =0;
 int vdFlag =0;
 int listFlag =0;
 int oppostFlag =0;
 int blockFlag =0;
 int[] acctTypeSel = new int [20];
 int cnt1 =0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC110 proc = new MktC110();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
//   setConsoleMode("N");
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
       showLogMessage("I","","PARM 3 : [id_p_seqno]");
       return(1);
      }

   if ( args.length >= 1 )
      { hBusinessDate = args[0]; 
      	showLogMessage("I","","輸入參數1資料(business_date)=["+hBusinessDate+"]");
      }

   if ( args.length >= 2 ) {
       activeCode = args[1];
       showLogMessage("I","","輸入參數2資料(active_code)=["+activeCode+"]");
   }
   if ( args.length >= 3 ) {
	   idPSeqno = args[2];
	   showLogMessage("I","","輸入參數3資料(id_p_seqno)=["+idPSeqno+"]");
   }
   
   if ( !connectDataBase() ) 
       return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());
   comCol = new CommCol(getDBconnect(),getDBalias());
   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料(mkt_channel_parm)");
   selectMktChannelParm();
   if (parmCnt==0)
      {
       showLogMessage("I","","本日非分析日期, 程式不執行分析處理 !");
       showLogMessage("I","","=========================================");
       return(0);
      }
   
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數暫存資料");
   loadMktMchtgpData();         //讀取特店群組資料
   loadMktBnData();
   loadMktBnCdata();
   loadMktImchannelList();
   loadMktChantypeData();

   if ((vdFlag ==1)||(vdFlag ==3)) 
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","處理 DBB_BILL 資料");
       loadMktImchannelListVid();
       selectDbbBill();
      }
   if ((vdFlag ==2)||(vdFlag ==3)) 
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","處理 BIL_BILL 資料");
       loadMktImchannelListCid();
       selectBilBill();
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入主檔暫存資料");
   if (oppostFlag ==1)
      {
       loadCrdCard();
       loadDbcCard();
      }
   if (blockFlag ==1) loadCcaCardAcct();
// load_act_acno();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 MKT_CHANNEL_BILL 資料");
   selectMktChannelBill();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理變更卡號資料");
   selectMktChannelBill1();
   if (idPSeqno.length()==0) commitDataBase();          //輸入idPSeqno者, 僅做程式trace, 不寫檔

   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 ID_NO 資料");

   for (int inti=0;inti<parmCnt;inti++)
       {
        showLogMessage("I","","=========================================");
        showLogMessage("I","","處理參數, 活動代號 :["+getValue("parm.active_code",inti)+"] (ID_NO) 資料");
        showLogMessage("I","","  acct_type選項  :["+ acctTypeSel[inti] +"] ");
//      if (getValue("parm.feedback_key_sel",inti).equals("1"))
           {
            if (acctTypeSel[inti] == 3)
               {
                loadCrdIdno(inti);
                loadDbcIdno(inti);
               }
            else if (acctTypeSel[inti] == 2)
                 loadCrdIdno(inti);
            else if (acctTypeSel[inti] == 1)
                 loadDbcIdno(inti);

           updateCnt = 0;
           showLogMessage("I","","=========================================");
           showLogMessage("I","","處理寫入 id_no 資料");
           selectMktChannelBill2(inti);
           showLogMessage("I","","更新筆數 :["+ updateCnt  +"]");
           showLogMessage("I","","=========================================");
           showLogMessage("I","","處理 退貨資料 (selectMktChannelBill3())");
           selectMktChannelBill3(inti);
           showLogMessage("I","","更新筆數 :["+ updateCnt  +"]");
           showLogMessage("I","","=========================================");
          }
      }

   showLogMessage("I","","處理參數每日限量數");
   for (int inti=0;inti<parmCnt;inti++)
       {
        if (!getValue("parm.perday_cnt_cond",inti).equals("Y")) continue;
        showLogMessage("I","","=========================================");
        showLogMessage("I","","活動代號 :["+getValue("parm.active_code",inti)+"]");
        selectMktChannelBillA(inti);

        showLogMessage("I","","更新筆數 :["+ updateCnt  +"]");
        showLogMessage("I","","=========================================");
      }
   showLogMessage("I","","處理參數限量數");
   for (int inti=0;inti<parmCnt;inti++)
       {
        if (!getValue("parm.max_cnt_cond",inti).equals("Y")) continue;
        showLogMessage("I","","=========================================");
        showLogMessage("I","","活動代號 :["+getValue("parm.active_code",inti)+"]");
        selectMktChannelBillB(inti);

        showLogMessage("I","","更新筆數 :["+ updateCnt  +"]");
        showLogMessage("I","","=========================================");
      }

   if (idPSeqno.length()==0) finalProcess();    //輸入idPSeqno者, 僅做程式trace, 不寫檔
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void selectPtrBusinday() throws Exception
 {
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (hBusinessDate.length()==0)
      hBusinessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ hBusinessDate +"]");
 }
// ************************************************************************
 int selectMktChannelParm() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_channel_parm";
  whereStr  = "WHERE apr_flag      = 'Y' "
            + "AND     ((stop_flag = 'N') "
            + " or      (stop_flag = 'Y' "
            + "  and     stop_date > ? )) "
            + "AND     feedback_apr_date = '' "
            + "AND     cal_def_date      = ?  "
            ;

  setString(1 , hBusinessDate); 
  setString(2 , hBusinessDate); 

  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";  

      setString(3 , activeCode);
     }

  parmCnt = selectTable();

  minDate = "99999999";
  maxDate = "00000000";
  for (int inti=0;inti<parmCnt;inti++)
    {
     showLogMessage("I","","符合之參數:["+getValue("parm.active_code",inti)+"]["+getValue("parm.active_name",inti)+"]");
     if (getValue("parm.acct_type_sel").equals("0"))
        {
         acctTypeSel[inti] = 3;
        } 
     else if (getValue("parm.acct_type_sel").equals("1"))
        {
         acctTypeSel[inti] = selectMktBnDataVd(inti);
        }
     else
        {
         if (selectMktBnDataVd(inti)!=2)
            acctTypeSel[inti] = 2;
        }
     if (vdFlag < acctTypeSel[inti]) vdFlag = acctTypeSel[inti];

     if (getValue("parm.list_cond",inti).equals("Y")) listFlag =1;
     if (getValue("parm.oppost_cond",inti).equals("Y")) oppostFlag =1;
     if (getValue("parm.block_cond",inti).equals("Y")) blockFlag =1;
     
     deleteMktChannelBill(inti);
     deleteMktChannelRank(inti);

     if("2".equals(getValue("parm.accumulate_term_sel",inti))) {
        	 acctMonths.add(getValue("parm.acct_month",inti));
     }else {
         if (minDate.compareTo(getValue("parm.purchase_date_s",inti))>0)
             minDate = getValue("parm.purchase_date_s",inti);

          if (maxDate.compareTo(getValue("parm.purchase_date_e",inti))<0)
             maxDate = getValue("parm.purchase_date_e",inti);
          
          if (maxDate.compareTo(getValue("parm.cal_def_date",inti))<0)
             maxDate = getValue("parm.cal_def_date",inti);
          
          purchaseDateflag = true;
     }

     //因cal_def_date已由mkp0850計算出, 故不再重複取得purchase_date 條件(20230818, grace)
     /*
     if ("M".equals(getValue("parm.feedback_cycle",inti)) && getValue("parm.cal_def_date",inti).length()>0) {
        String calDefDate = comDate.monthAdd(getValue("parm.cal_def_date",inti), -1);
        minDate2 = calDefDate + "01";
        maxDate2 = comCol.lastdateOfmonth(calDefDate);
        showLogMessage("I","","minDate2 : ["+ minDate2 +"] ~ maxDate2["+ maxDate2 +"]");
     }
     */

     if (idPSeqno.length()==0) commitDataBase();
    }
  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
 }
// ************************************************************************
 int selectBilBill() throws Exception
 {
  selectSQL = "dest_amt,"
            + "curr_adjust_amt,"
            + "purchase_date,"
            + "sign_flag,"
            + "acct_type,"
            + "acct_code,"
            + "install_curr_term,"
            + "contract_amt,"
            + "install_tot_term,"
            + "install_first_amt,"
            + "install_per_amt,"
            + "p_seqno,"
            + "id_p_seqno,"
            + "major_id_p_seqno,"
            + "group_code,"
            + "acq_member_id,"
            + "mcht_no,"
            + "mcht_category,"
            + "terminal_id,"
            + "ecs_platform_kind,"
            + "major_card_no,"
            + "card_no,"
            + "contract_no,"
            + "install_tot_term,"
            + "auth_code,"
            + "pos_entry_mode,"
            + "payment_type,"
            + "cash_pay_amt,"
            + "mcht_eng_name," 
            + "mcht_chi_name," 
            + "reference_no,"
  			+ "acct_month ";
  daoTable  = "bil_bill";
  whereStr  = "where acct_code in ('BL','IT','ID','CA','OT','AO') "
            + "and   rsk_type not in ('1','2','3') "
            + "and   merge_flag       != 'Y'  "
//            + "and   purchase_date between ? and ? "
            + "and   dest_amt != 0 "
            + "and   post_date <= ? "
            + "and   acct_type not in ('03','05','06') "  //TCB's 03.商務卡; 05.商務(電子採購); 06.政府網路採購卡            
//                       + "and   acct_type not in ('02','03') "  //MEGA's 02.商務卡; 03.VISA採購卡
            ;
  
  int index = 1; 
  setString(index++ , hBusinessDate);
  
  if (idPSeqno.length()>0)
  { 
   whereStr  = whereStr 
             + "and major_id_p_seqno = ? ";  

   setString(index++ , idPSeqno);
  }
  
  getbillWhere(index);

  //20230818, grace
  /*
  if(minDate2.length()>0 && maxDate2.length()>0) {
          whereStr  += " and purchase_date between ? and ? " ;
          setString(index++ , minDate2);
          setString(index++ , maxDate2);
  }
  */

  openCursor();

  totalCnt=0;

  double[][] parmArr = new double [parmCnt][30];
  for (int inti=0;inti<parmCnt;inti++)
     for (int intk=0;intk<30;intk++) parmArr[inti][intk]=0;

  if(purchaseDateflag == true)
	  showLogMessage("I","","消費日日期 : ["+ minDate +"] ~ ["+ maxDate +"]");
  if(acctMonths.size()>0) {
	  String months = "";
	  for(int i = 0;i<acctMonths.size();i++) {
		  months += acctMonths.get(i) + ",";
	  }
	  showLogMessage("I","","當期帳單(年月) : ["+ months +"]");
  }
  String acqId ="";
  int okFlag=0;
  int indexInt = 0;
  while( fetchTable() ) 
   {
    totalCnt++;
    setValue("vd_flag" , "N");

    if (getValueInt("dest_amt") + getValueInt("curr_adjust_amt")==0) continue;

    if (!getValue("sign_flag").equals("+"))
       {
        setValueDouble("dest_amt" , getValueDouble("dest_amt")*-1);
       }
    else
       {
        if ((getValue("payment_type").compareTo("1")>=0)&&
            (getValue("payment_type").compareTo("6")<=0))
            setValueDouble("dest_amt"    , getValueDouble("cash_pay_amt"));
       }
    //for test --------------------------------------------------------------------------------------
    /*
    if (idPSeqno.length()>0) {
    	showLogMessage("I","","parm.accumulate_term_sel : ["+ getValue("parm.accumulate_term_sel") +"]");
    }
    */
    
    for (int inti=0;inti<parmCnt;inti++)
        {
        if (!getValue("sign_flag").equals("+")) {
        	if ("2".equals(getValue("parm.accumulate_term_sel",inti))) {
        		if (getValue("acct_month").compareTo(comStr.left(hBusinessDate, 6))>0) continue;
        	}
        	else {
        		if (getValue("purchase_date").compareTo(hBusinessDate)>0) continue;
        	}
        }
        
         parmArr[inti][0]++;
         if ("2".equals(getValue("parm.accumulate_term_sel",inti))) {
        	 if (getValue("acct_month").equals(getValue("parm.acct_month",inti))==false) continue;
         }
         else {
        	 if (getValue("purchase_date").compareTo(getValue("parm.purchase_date_s",inti))<0) continue;
         }
         
         parmArr[inti][1]++;
         if (getValue("sign_flag").equals("+")) {
        	 if("1".equals(getValue("parm.accumulate_term_sel",inti))) {
        		 if (getValue("purchase_date").compareTo(getValue("parm.purchase_date_e",inti))>0) continue;
        	 }
         }

//         else
//            if (getValue("purchase_date").compareTo(hBusinessDate)>0) continue;
         parmArr[inti][2]++;

         if (getValue("parm.minus_txn_cond",inti).equals("Y")) {
        	 if (!getValue("sign_flag").equals("+")) continue;
         }
         parmArr[inti][3]++;

         if ((!getValue("parm.bl_cond",inti).equals("Y"))&&(getValue("acct_code").equals("BL"))) continue;
         if ((!getValue("parm.it_cond",inti).equals("Y"))&&(getValue("acct_code").equals("IT"))) continue;
         if ((!getValue("parm.ca_cond",inti).equals("Y"))&&(getValue("acct_code").equals("CA"))) continue;
         if ((!getValue("parm.id_cond",inti).equals("Y"))&&(getValue("acct_code").equals("ID"))) continue;
         if ((!getValue("parm.ao_cond",inti).equals("Y"))&&(getValue("acct_code").equals("AO"))) continue;
         if ((!getValue("parm.ot_cond",inti).equals("Y"))&&(getValue("acct_code").equals("OT"))) continue;

         parmArr[inti][4]++;
    
         if ((!getValue("parm.mcht_cname_sel",inti).equals("0"))||
             (!getValue("parm.mcht_ename_sel",inti).equals("0")))
             {
              if (idPSeqno.length()>0)
                 {
            	  showLogMessage("I","","-------------------------------------------------------------");
            	  showLogMessage("I","","mcht_cname_sel ["+ getValue("parm.mcht_cname_sel",inti) +"]");
                  showLogMessage("I","","mcht_ename_sel ["+ getValue("parm.mcht_ename_sel",inti) +"]");
                 }
              okFlag=0;
              if ((!getValue("parm.mcht_cname_sel",inti).equals("0"))&&
                  (getValue("mcht_chi_name").length()!=0))
                 {
                  setValue("datc.data_key"   , getValue("parm.active_code",inti));
                  setValue("datc.data_type"  , "A");
                  cnt1 =  getLoadData("datc.data_key,datc.data_type");

                  if (idPSeqno.length()>0)
                     {
                      showLogMessage("I","","mcht_chi_name ["+ getValue("mcht_chi_name") +"]");
                      showLogMessage("I","","cnt1 ["+ cnt1 +"]");
                     }

                  for (int intk=0;intk<cnt1;intk++)
                      {
                       indexInt = getValue("mcht_chi_name").indexOf(getValue("datc.data_code",intk));

                       if (idPSeqno.length()>0)
                          {
                           showLogMessage("I","","mcht_chi_name ["+ getValue("datc.data_code",intk) +"]");
                           showLogMessage("I",""," indexint ["+ indexInt +"]");
                          }
                       if (indexInt!=-1)
                          {
                           if (getValue("parm.mcht_cname_sel",inti).equals("1"))
                               okFlag=1;
                           else 
                               okFlag=2;
                           break;
                          }
                      }
                 }
              //parmArr[inti][19]++;
              if (okFlag==2) continue;
              if (okFlag==0)
              if ((!getValue("parm.mcht_ename_sel",inti).equals("0"))&&
                  (getValue("mcht_eng_name").length()!=0))
                 {
                  setValue("datc.data_key"   , getValue("parm.active_code",inti));
                  setValue("datc.data_type"  , "B");
                  cnt1 =  getLoadData("datc.data_key,datc.data_type");
                  for (int intk=0;intk<cnt1;intk++)
                      {
                       indexInt = getValue("mcht_eng_name").toUpperCase().indexOf(getValue("datc.data_code",intk).toUpperCase());
                       if (indexInt!=-1)
                          {
                           if (getValue("parm.mcht_ename_sel",inti).equals("1"))
                               okFlag=1;
                           else 
                               okFlag=2;
                           break;
                          }
                      }
                 }
              //parmArr[inti][20]++;
              if (okFlag!=1) continue;
             }
         //parmArr[inti][19]++;
         //分期(IT)---------------------------------
         if ((getValue("acct_code").equals("IT"))&&(getValue("parm.it_flag",inti).equals("1")))
            {
             if (getValueInt("install_curr_term")!=1) continue;         //分期付款目前期數
             
             //parmArr[inti][21]++;
             if (selectBilContract(inti)!=0) continue;                          //檢核有退貨者, 不處理
             //parmArr[inti][22]++;
             if ((getValue("payment_type").compareTo("1")>=0)&&
                 (getValue("payment_type").compareTo("6")<=0))
                setValueInt("dest_amt" , getValueInt("install_first_amt")
                                       + (getValueInt("install_tot_term")-1)*getValueInt("install_per_amt"));
             else
                setValue("dest_amt" , getValue("contract_amt"));
            }
         else
            setValueInt("dest_amt" , getValueInt("dest_amt")+getValueInt("curr_adjust_amt"));

         //單筆最低消費金額 ----------------------------------
         if (getValue("parm.per_amt_cond",inti).equals("Y"))		
            if (getValue("sign_flag").equals("+")) 
               if (getValueDouble("dest_amt")<getValueDouble("parm.per_amt",inti)) continue;

         parmArr[inti][5]++;
         //(匯入)名單檢核 ----------------------------------------------------
         if (getValue("parm.list_cond",inti).equals("Y"))
            {
             if (getValue("parm.list_use_sel",inti).equals("1"))	//1.指定, 2.排除
                {
                 if (getValue("parm.list_flag",inti).equals("1"))	//名單類別 (1.ID,2.卡號,3.一卡通,4.悠遊卡,5.愛金卡)
                    {
                     //setValue("milc.id_p_seqno" , getValue("major_id_p_seqno"));
                     setValue("milc.id_p_seqno" , getValue("id_p_seqno"));	//grace, 20231017                     
                     setValue("milc.active_code", getValue("parm.active_code",inti));
                     cnt1 =  getLoadData("milc.id_p_seqno,milc.active_code");
                     if (cnt1==0) continue;
                    }
                 else 
                    {
                     setValue("milt.card_no"    , getValue("card_no"));
                     setValue("milt.active_code", getValue("parm.active_code",inti));
                     cnt1 =  getLoadData("milt.card_no,milt.active_code");
                     if (cnt1==0) continue;
                    }
                }
             else	//排除 --
                {
                 if (getValue("parm.list_flag",inti).equals("1"))
                    {
                     //setValue("milc.id_p_seqno" , getValue("major_id_p_seqno"));
                     setValue("milc.id_p_seqno" , getValue("id_p_seqno"));		//grace, 20231017
                     setValue("milc.active_code", getValue("parm.active_code",inti));
                     cnt1 =  getLoadData("milc.id_p_seqno,milc.active_code");
                     if (cnt1!=0) continue;
                    }
                 else 
                    {
                     setValue("milt.card_no"    , getValue("card_no"));
                     setValue("milt.active_code", getValue("parm.active_code",inti));
                     cnt1 =  getLoadData("milt.card_no,milt.active_code");
                     if (cnt1!=0) continue;
                    }
                }
            }

         if (getValue("group_code").length()==0)
            setValue("group_code" , "0000");

         setValue("data_key" , getValue("parm.active_code",inti));

         parmArr[inti][6]++;
         if (selectMktBnData(getValue("acct_type"),
                                getValue("parm.acct_type_sel",inti),"1",3)!=0) continue;

         parmArr[inti][7]++;
         if (selectMktBnData(getValue("group_code"),
                                getValue("parm.group_code_sel",inti),"2",3)!=0) continue;

         parmArr[inti][8]++;
         if (getValue("sign_flag").equals("+")) 
         if (selectMktBnData(getValue("pos_entry_mode"),
                                getValue("parm.pos_entry_sel",inti),"8",3)!=0) continue;

         parmArr[inti][9]++;
         acqId = "";
         if (getValue("acq_member_id").length()!=0)
            acqId = comm.fillZero(getValue("acq_member_id"),8);

         if (getValue("sign_flag").equals("+")) 
         if (selectMktBnData(getValue("mcht_no"), acqId,
                                getValue("parm.merchant_sel",inti),"3",3)!=0) continue;

         parmArr[inti][10]++;
         if (getValue("sign_flag").equals("+")) 
         if (getValue("acct_code").equals("IT"))
            if (selectMktBnData(String.format("%d",getValueInt("install_tot_term")),"",
                                  getValue("parm.it_term_sel",inti),"4",3)!=0) continue;

         parmArr[inti][11]++;
         if (getValue("sign_flag").equals("+")) 
         if (selectMktBnData(getValue("mcht_category"),
                              getValue("parm.mcc_code_sel",inti),"5",3)!=0) continue;
         parmArr[inti][12]++;

         parmArr[inti][13]++;
         if (getValue("sign_flag").equals("+")) 
         if (selectMktMchtgpData(getValue("mcht_no"),acqId,
                                   getValue("parm.mcht_group_sel",inti),"6")!=0) continue;

         parmArr[inti][14]++;
         if (getValue("sign_flag").equals("+")) 
         if (selectMktBnData(getValue("terminal_id"),
                                getValue("parm.terminal_id_sel",inti),"7",3)!=0) continue;

         parmArr[inti][15]++;
         if (getValue("sign_flag").equals("+")) 
         if (selectMktBnData(getValue("ecs_platform_kind"),
                 "Y".equals(getValue("parm.bl_cond",inti)) ? getValue("parm.platform_kind_sel",inti) : "0","9",3)!=0) continue;
         
         //非一般消費判讀
         parmArr[inti][16]++;
         //1.指定
         if (getValue("parm.platform_group_sel",inti).equals("1")) {
        	 //if ((!selectMktGpDataDataCode(getValue("mcht_no")) && !selectBilBillChiName(getValue("mcht_chi_name")))) {
        	 if (!selectMktGpDataDataCode(getValue("mcht_no"))) {
                 continue;
        	 }
        	 //AND substr(mcht_chi_name, 1, 2) in ('f%', 'G%', 'd%', 'M%', 'b%', 'e%', 'V%', 'A%', '$%', '#%','Ｖ％')
        	 if (",f%,ｆ％,G%,Ｇ％,d%,ｄ％,M%,Ｍ％,b%,ｂ％,e%,ｅ％,V%,Ｖ％,U%,Ｕ％,A%,Ａ％,$%,＄％,#%,＃％".indexOf(
        			 //getValue("mcht_chi_name",inti).substring(0,2))<0
        			 comStr.left(getValue("mcht_chi_name"), 2))<0
        			 ) {
        		 if (idPSeqno.length()>0) {
        			 if("2".equals(getValue("parm.platform_group_sel",inti)))
        				 showLogMessage("I","","*** 特店中文2碼[指定], bil_bill.acct_month: ["+getValue("acct_month")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
        			 else
        				 showLogMessage("I","","*** 特店中文2碼[指定], bil_bill.purchase_date: ["+getValue("purchase_date")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
        		 }
        		 continue;
        	 }     
        	 if (",$,＄,#,＃".indexOf(
        			 comStr.left(getValue("mcht_chi_name"), 1))<0
        			 ) {
        		 if (idPSeqno.length()>0) {
        			 if("2".equals(getValue("parm.platform_group_sel",inti)))
        				 showLogMessage("I","","*** 特店中文1碼[指定], bil_bill.acct_month: ["+getValue("acct_month")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
        			 else
        				 showLogMessage("I","","*** 特店中文1碼[指定], bil_bill.purchase_date: ["+getValue("purchase_date")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
        		 }
        		 continue;
        	 }     
        	 //稅
          	 //if  (!getValue("mcht_category",inti).equals("9311")) {
          	 if  (!getValue("mcht_category").equals("9311")) {
          		 if (idPSeqno.length()>0) {
        			 showLogMessage("I","","*** 特店類別/9311[指定], bil_bill.mcht_category: ["+getValue("mcht_category")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
        		 }
                 continue;
        	 }
         //2.排除
         } else if (getValue("parm.platform_group_sel",inti).equals("2")) {
             //if (selectMktGpDataDataCode(getValue("mcht_no")) || selectBilBillChiName(getValue("mcht_chi_name"))) {
             if (selectMktGpDataDataCode(getValue("mcht_no"))) { 
                 continue;
             }
             if(",f%,ｆ％,G%,Ｇ％,d%,ｄ％,M%,Ｍ％,b%,ｂ％,e%,ｅ％,V%,Ｖ％,U%,Ｕ％,A%,Ａ％,$%,＄％,#%,＃％".indexOf(
        			 //getValue("mcht_chi_name",inti).substring(0,2))>=0
            		 comStr.left(getValue("mcht_chi_name"), 2))>=0
        			 ) {
            	 	if (idPSeqno.length()>0) {
            	 		if("2".equals(getValue("parm.platform_group_sel",inti)))
            	 			showLogMessage("I","","*** 特店中文2碼[排除], bil_bill.acct_month: ["+getValue("acct_month")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
            	 		else	 
            	 			showLogMessage("I","","*** 特店中文2碼[排除], bil_bill.purchase_date: ["+getValue("purchase_date")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
            		 }
        		 continue;
        	 }
             if(",$,＄,#,＃".indexOf(
            		 comStr.left(getValue("mcht_chi_name"), 1))>=0
        			 ) {
            	 	if (idPSeqno.length()>0) {
            	 		if("2".equals(getValue("parm.platform_group_sel",inti)))
            	 			showLogMessage("I","","*** 特店中文2碼[排除], bil_bill.acct_month: ["+getValue("acct_month")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
            	 		else
            	 			showLogMessage("I","","*** 特店中文2碼[排除], bil_bill.purchase_date: ["+getValue("purchase_date")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");	
            	 	}
        		 continue;
        	 }
             //稅
             //if  (getValue("mcht_category",inti).equals("9311")) {
             if  (getValue("mcht_category").equals("9311")) {   
            	 if (idPSeqno.length()>0) {
        			 showLogMessage("I","","*** 特店類別/9311[排除], bil_bill.mcht_category: ["+getValue("mcht_category")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
        		 }
                 continue;
        	 }
         }

         parmArr[inti][17]++;
         setValue("chan.data_key", getValue("parm.active_code", inti));
         int cnt1 = getLoadData("chan.data_key");
         if (getValue("parm.channel_type_sel", inti).equals("1")) {
             boolean exists = false;
             for (int intk = 0; intk < cnt1; intk++) {
                 if (getValue("chan.txcode_sel", intk).equals("1")) {
                     if (getValue("chan.tx_desc_type", intk).equals("A")) {
                         if (getValue("chan.tx_desc_name", intk)
                                 .equals(getValue("mcht_chi_name"))) {
                             exists = true;
                             break;
                         }
                     }
                     if (getValue("chan.tx_desc_type", intk).equals("B")) {
                         if (getValue("chan.tx_desc_name", intk)
                                 .equals(getValue("mcht_eng_name"))) {
                             exists = true;
                             break;
                         }
                     }
                 }
                 if (getValue("chan.txcode_sel", intk).equals("2")) {
                     if (getValue("chan.tx_desc_type", intk).equals("A")) {
                         if (!getValue("chan.tx_desc_name", intk)
                                 .equals(getValue("mcht_chi_name"))) {
                             exists = true;
                             break;
                         }
                     }
                     if (getValue("chan.tx_desc_type", intk).equals("B")) {
                         if (!getValue("chan.tx_desc_name", intk)
                                 .equals(getValue("mcht_eng_name"))) {
                             exists = true;
                             break;
                         }
                     }
                 }
                 if (getValue("chan.mccc_sel", intk).equals("1")) {
                     if (getValue("mcht_category").equals(getValue("chan.mccc_code"))) {
                         exists = true;
                         break;
                     }
                 }
                 if (getValue("chan.mccc_sel", intk ).equals("2")) {
                     if (!getValue("mcht_category").equals(getValue("chan.mccc_code"))) {
                         exists = true;
                         break;
                     }
                 }
             }
             if (!exists) {
                 continue;
             }
         }
         if (getValue("parm.channel_type_sel", inti).equals("2")) {
             boolean exists = false;
             for (int intk = 0; intk < cnt1; intk++) {
                 if (getValue("chan.txcode_sel", intk).equals("1")) {
                     if (getValue("chan.tx_desc_type", intk).equals("A")) {
                         if (!getValue("chan.tx_desc_name", intk)
                                 .equals(getValue("mcht_chi_name"))) {
                             exists = true;
                             break;
                         }
                     }
                     if (getValue("chan.tx_desc_type", intk).equals("B")) {
                         if (!getValue("chan.tx_desc_name", intk)
                                 .equals(getValue("mcht_eng_name"))) {
                             exists = true;
                             break;
                         }
                     }
                 }
                 if (getValue("chan.txcode_sel", intk).equals("2")) {
                     if (getValue("chan.tx_desc_type", intk).equals("A")) {
                         if (getValue("chan.tx_desc_name", intk)
                                 .equals(getValue("mcht_chi_name"))) {
                             exists = true;
                             break;
                         }
                     }
                     if (getValue("chan.tx_desc_type", intk).equals("B")) {
                         if (getValue("chan.tx_desc_name", intk)
                                 .equals(getValue("mcht_eng_name"))) {
                             exists = true;
                             break;
                         }
                     }
                 }
                 if (getValue("chan.mccc_sel", intk).equals("1")) {
                     if (!getValue("mcht_category").equals(getValue("chan.mccc_code"))) {
                         exists = true;
                         break;
                     }
                 }
                 if (getValue("chan.mccc_sel", intk ).equals("2")) {
                     if (getValue("mcht_category").equals(getValue("chan.mccc_code"))) {
                         exists = true;
                         break;
                     }
                 }
             }
             if (!exists) {
                 continue;
             }
         }
         parmArr[inti][18]++;
         insertMktChannelBill(inti);
        }

    processDisplay(300000); // every 10000 display message
   } 
  closeCursor();

  if (idPSeqno.length()>0)
     {
      showLogMessage("I","","=========================================");
      showLogMessage("I","","處理筆數(selectBilBill) ["+ totalCnt + "] 筆" );
      for (int inti=0;inti<parmCnt;inti++)
         {
          showLogMessage("I","","    ["+String.format("%03d",inti)
                               + "] 活動代號 [" + getValue("parm.active_code",inti) +"]");
   
          for (int intk=0;intk<30;intk++)
            {
             if (parmArr[inti][intk]==0) continue;
             showLogMessage("I",""," 測試絆腳石 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
           }
         }
      showLogMessage("I","","=========================================");
     }

  return(0);
 }
// ************************************************************************
 int selectDbbBill() throws Exception
 {
  selectSQL = "dest_amt,"
            + "curr_adjust_amt,"
            + "purchase_date,"
            + "sign_flag,"
            + "acct_type,"
            + "acct_code,"
            + "install_curr_term,"
            + "contract_amt,"
            + "p_seqno,"
            + "id_p_seqno as major_id_p_seqno,"
            + "id_p_seqno,"
            + "group_code,"
            + "acq_member_id,"
            + "mcht_no,"
            + "mcht_category,"
            + "terminal_id,"
            + "ecs_platform_kind,"
            + "major_card_no,"
            + "card_no,"
            + "contract_no,"
            + "mcht_eng_name," 
            + "mcht_chi_name," 
            + "reference_no,"
            + "acct_month ";
  daoTable  = "dbb_bill";
  whereStr  = "where acct_code in ('BL','IT','ID','CA','OT','AO') "
            + "and   rsk_type not in ('1','2','3') "
//            + "and   purchase_date between ? and ? "
            + "and   dest_amt != 0 "
            + "and   post_date <= ? "
            ;
  int index = 1;     
  setString(index++ , hBusinessDate); 

  if (idPSeqno.length()>0)
     { 
      whereStr  = whereStr 
                + "and major_id_p_seqno = ? ";  

      setString(index++ , idPSeqno);
     }
  
  getbillWhere(index);
  
  //20230818, grace
  /*
  if(minDate2.length()>0 && maxDate2.length()>0) {
          whereStr  += " and purchase_date between ? and ? " ;
          setString(index++ , minDate2);
          setString(index++ , maxDate2);
  }
  */
  
  openCursor();

  totalCnt=0;

  double[][] parmArr = new double [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
     for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;

  if(purchaseDateflag == true)
	  showLogMessage("I","","消費日日期 : ["+ minDate +"] ~ ["+ maxDate +"]");
  if(acctMonths.size()>0) {
	  String months = "";
	  for(int i = 0;i<acctMonths.size();i++) {
		  months += acctMonths.get(i) + ",";
	  }
	  showLogMessage("I","","當期帳單(年月) : ["+ months +"]");
  }
  String acqId ="";
  int okFlag=0;
  int indexInt = 0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    setValue("vd_flag" , "Y");

    for (int inti=0;inti<parmCnt;inti++)
        {
    	if("2".equals(getValue("parm.accumulate_term_sel",inti)))
    		if (getValue("acct_month").equals(getValue("parm.acct_month",inti))==false) continue;
    	else {
            if (getValue("purchase_date").compareTo(getValue("parm.purchase_date_s",inti))<0) continue;
            if (getValue("purchase_date").compareTo(getValue("parm.purchase_date_e",inti))>0) continue;
    	}
         if (getValue("parm.minus_txn_cond",inti).equals("Y"))
            if (!getValue("sign_flag").equals("+")) continue;

         if ((!getValue("parm.bl_cond",inti).equals("Y"))&&(getValue("acct_code").equals("BL"))) continue;
         if ((!getValue("parm.it_cond",inti).equals("Y"))&&(getValue("acct_code").equals("IT"))) continue;
         if ((!getValue("parm.ca_cond",inti).equals("Y"))&&(getValue("acct_code").equals("CA"))) continue;
         if ((!getValue("parm.id_cond",inti).equals("Y"))&&(getValue("acct_code").equals("ID"))) continue;
         if ((!getValue("parm.ao_cond",inti).equals("Y"))&&(getValue("acct_code").equals("AO"))) continue;
         if ((!getValue("parm.ot_cond",inti).equals("Y"))&&(getValue("acct_code").equals("OT"))) continue;

         if (getValue("parm.list_cond",inti).equals("Y"))
            {
             if (getValue("parm.list_use_sel",inti).equals("1"))
                {
                 if (getValue("parm.list_flag",inti).equals("1"))
                    {
                     setValue("milv.id_p_seqno" , getValue("id_p_seqno"));
                     setValue("milv.active_code", getValue("parm.active_code",inti));
                     cnt1 =  getLoadData("milv.id_p_seqno,milv.active_code");
                     if (cnt1==0) continue;
                    }
                 else 
                    {
                     setValue("milt.card_no"    , getValue("card_no"));
                     setValue("milt.active_code", getValue("parm.active_code",inti));
                     cnt1 =  getLoadData("milt.card_no,milt.active_code");
                     if (cnt1==0) continue;
                    }
                }
             else
                {
                 if (getValue("parm.list_flag",inti).equals("1"))
                    {
                     setValue("milv.id_p_seqno" , getValue("id_p_seqno"));
                     setValue("milv.active_code", getValue("parm.active_code",inti));
                     cnt1 =  getLoadData("milv.id_p_seqno,milv.active_code");
                     if (cnt1!=0) continue;
                    }
                 else 
                    {
                     setValue("milt.card_no"    , getValue("card_no"));
                     setValue("milt.active_code", getValue("parm.active_code",inti));
                     cnt1 =  getLoadData("milt.card_no,milt.active_code");
                     if (cnt1!=0) continue;
                    }
                }
            }

         if (getValue("parm.per_amt_cond",inti).equals("Y"))
            if (getValue("sign_flag").equals("+")) 
               if (getValueDouble("dest_amt")<getValueDouble("parm.per_amt",inti)) continue;

         if (!getValue("sign_flag").equals("+"))
            setValueDouble("dest_amt" , getValueDouble("dest_amt")*-1);
       
         if (getValue("group_code").length()==0)
            setValue("group_code" , "0000");

         setValue("data_key" , getValue("parm.active_code",inti));

         if (selectMktBnData(getValue("acct_type"),
                                getValue("parm.acct_type_sel",inti),"1",3)!=0) continue;

         if (selectMktBnData(getValue("group_code"),
                                getValue("parm.group_code_sel",inti),"2",3)!=0) continue;

         acqId = "";
         if (getValue("acq_member_id").length()!=0)
            acqId = comm.fillZero(getValue("acq_member_id"),8);

       if (selectMktBnData(getValue("mcht_no"), acqId,
                              getValue("parm.merchant_sel",inti),"3",3)!=0) continue;

       if (selectMktBnData(getValue("mcht_category"),
                              getValue("parm.mcc_code_sel",inti),"5",3)!=0) continue;
       parmArr[inti][4]++;

       if (selectMktMchtgpData(getValue("mcht_no"), acqId,
                                   getValue("parm.mcht_group_sel",inti),"6")!=0) continue;

       if (selectMktBnData(getValue("terminal_id"),
                              getValue("parm.terminal_id_sel",inti),"7",3)!=0) continue;

       if (selectMktBnData(getValue("ecs_platform_kind"),
               "Y".equals(getValue("parm.bl_cond",inti)) ? getValue("parm.platform_kind_sel",inti) : "0","9",3)!=0) continue;
       //-ori --------------------------------------------------------------
       /*
       if (getValue("parm.platform_group_sel",inti).equals("1")) {
           if ((!selectMktGpDataDataCode(getValue("mcht_no")) && !selectBilBillChiName(getValue("mcht_chi_name")))) {
               continue;
           }
       } else if (getValue("parm.platform_group_sel",inti).equals("2")) {
           if (selectMktGpDataDataCode(getValue("mcht_no")) || selectBilBillChiName(getValue("mcht_chi_name"))) {
               continue;
           }
       }
       */
       //--調整[非一般消費]、[特店中文]判讀 ------------------------------------------------------
       //1.指定[非一般消費]
       if (getValue("parm.platform_group_sel",inti).equals("1")) {
      	 if (!selectMktGpDataDataCode(getValue("mcht_no"))) {
               continue;
      	 }
      	 if (",f%,ｆ％,G%,Ｇ％,d%,ｄ％,M%,Ｍ％,b%,ｂ％,e%,ｅ％,V%,Ｖ％,U%,Ｕ％',A%,Ａ％,$%,＄％,#%,＃％".indexOf(
      			 comStr.left(getValue("mcht_chi_name"), 2))<0
      			 ) {
      		 if (idPSeqno.length()>0) {
      			 if("2".equals(getValue("parm.accumulate_term_sel",inti)))
      				 showLogMessage("I","","*** VD特店中文2碼[指定], dbb_bill.acct_month: ["+getValue("acct_month")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
      			 else
      				 showLogMessage("I","","*** VD特店中文2碼[指定], dbb_bill.purchase_date: ["+getValue("purchase_date")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
      		 }
      		 continue;
      	 }     
      	 if (",$,＄,#,＃".indexOf(
      			 comStr.left(getValue("mcht_chi_name"), 1))<0
      			 ) {
      		 if (idPSeqno.length()>0) {
      			 if("2".equals(getValue("parm.accumulate_term_sel",inti)))
      				 showLogMessage("I","","*** VD特店中文1碼[指定], dbb_bill.acct_month: ["+getValue("acct_month")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
      			 else
      				 showLogMessage("I","","*** VD特店中文1碼[指定], dbb_bill.purchase_date: ["+getValue("purchase_date")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
      		 }
      		 continue;
      	 }     
      	 //稅
        	 if  (!getValue("mcht_category").equals("9311")) {
        		 if (idPSeqno.length()>0) {
      			 showLogMessage("I","","*** 特店類別/9311[指定], dbb_bill.mcht_category: ["+getValue("mcht_category")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
      		 }
               continue;
      	 }
       //2.排除[非一般消費]
       } else if (getValue("parm.platform_group_sel",inti).equals("2")) {
           if (selectMktGpDataDataCode(getValue("mcht_no"))) { 
               continue;
           }
           if(",f%,ｆ％,G%,Ｇ％,d%,ｄ％,M%,Ｍ％,b%,ｂ％,e%,ｅ％,V%,Ｖ％,U%,Ｕ％,A%,Ａ％,$%,＄％,#%,＃％".indexOf(
      			 comStr.left(getValue("mcht_chi_name"), 2))>=0
      			 ) {
          	 	if (idPSeqno.length()>0) {
          	 		if("2".equals(getValue("parm.accumulate_term_sel",inti)))
          	 			showLogMessage("I","","*** VD特店中文2碼[排除], dbb_bill.acct_month: ["+getValue("acct_month")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
          	 		else
          	 			showLogMessage("I","","*** VD特店中文2碼[排除], dbb_bill.purchase_date: ["+getValue("purchase_date")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
          		 }
      		 continue;
      	 }
           if(",$,＄,#,＃".indexOf(
      			 comStr.left(getValue("mcht_chi_name"), 1))>=0
      			 ) {
          	 	if (idPSeqno.length()>0) {
          	 		if("2".equals(getValue("parm.accumulate_term_sel",inti)))
          	 			showLogMessage("I","","*** VD特店中文1碼[排除], dbb_bill.acct_month: ["+getValue("acct_month")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
          	 		else
          	 			showLogMessage("I","","*** VD特店中文1碼[排除], dbb_bill.purchase_date: ["+getValue("purchase_date")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
          		 }
      		 continue;
      	 }
           //稅
           if  (getValue("mcht_category").equals("9311")) {   
          	 if (idPSeqno.length()>0) {
      			 showLogMessage("I","","*** VD特店類別/9311[排除], dbb_bill.mcht_category: ["+getValue("mcht_category")+"], mcht_chi_name: ["+getValue("mcht_chi_name")+"]");
      		 }
               continue;
      	 }
       }

       setValue("chan.data_key", getValue("parm.active_code", inti));
       int cnt1 = getLoadData("chan.data_key");
       if (getValue("parm.channel_type_sel", inti).equals("1")) {
           boolean exists = false;
           for (int intk = 0; intk < cnt1; intk++) {
               if (getValue("chan.txcode_sel", intk).equals("1")) {
                   if (getValue("chan.tx_desc_type", intk).equals("A")) {
                       if (getValue("chan.tx_desc_name", intk)
                               .equals(getValue("mcht_chi_name"))) {
                           exists = true;
                           break;
                       }
                   }
                   if (getValue("chan.tx_desc_type", intk).equals("B")) {
                       if (getValue("chan.tx_desc_name", intk)
                               .equals(getValue("mcht_eng_name"))) {
                           exists = true;
                           break;
                       }
                   }
               }
               if (getValue("chan.txcode_sel", intk).equals("2")) {
                   if (getValue("chan.tx_desc_type", intk).equals("A")) {
                       if (!getValue("chan.tx_desc_name", intk)
                               .equals(getValue("mcht_chi_name"))) {
                           exists = true;
                           break;
                       }
                   }
                   if (getValue("chan.tx_desc_type", intk).equals("B")) {
                       if (!getValue("chan.tx_desc_name", intk)
                               .equals(getValue("mcht_eng_name"))) {
                           exists = true;
                           break;
                       }
                   }
               }
               if (getValue("chan.mccc_sel", intk).equals("1")) {
                   if (getValue("mcht_category").equals(getValue("chan.mccc_code"))) {
                       exists = true;
                       break;
                   }
               }
               if (getValue("chan.mccc_sel", intk ).equals("2")) {
                   if (!getValue("mcht_category").equals(getValue("chan.mccc_code"))) {
                       exists = true;
                       break;
                   }
               }
           }
           if (exists) {
               continue;
           }
       }
       
         if ((!getValue("parm.mcht_cname_sel",inti).equals("0"))||
             (!getValue("parm.mcht_ename_sel",inti).equals("0")))
             {
              if (idPSeqno.length()>0)
                 {
                  showLogMessage("I","","mcht_cname_sel ["+ getValue("parm.mcht_cname_sel",inti) +"]");
                  showLogMessage("I","","mcht_ename_sel ["+ getValue("parm.mcht_ename_sel",inti) +"]");
                 }
              okFlag=0;
              if ((!getValue("parm.mcht_cname_sel",inti).equals("0"))&&
                  (getValue("mcht_chi_name").length()!=0))
                 {
                  setValue("datc.data_key"   , getValue("parm.active_code",inti));
                  setValue("datc.data_type"  , "A");
                  cnt1 =  getLoadData("datc.data_key,datc.data_type");

                  if (idPSeqno.length()>0)
                     {
                      showLogMessage("I","","mcht_chi_name ["+ getValue("mcht_chi_name") +"]");
                      showLogMessage("I","","cnt1 ["+ cnt1 +"]");
                     }

                  for (int intk=0;intk<cnt1;intk++)
                      {
                       indexInt = getValue("mcht_chi_name").indexOf(getValue("datc.data_code",intk));

                       if (idPSeqno.length()>0)
                          {
                           showLogMessage("I","","mcht_ehi_name ["+ getValue("datc.data_code",intk) +"]");
                           showLogMessage("I",""," indexint ["+ indexInt +"]");
                          }
                       if (indexInt!=-1)
                          {
                           if (getValue("parm.mcht_cname_sel",inti).equals("1"))
                               okFlag=1;
                           else 
                               okFlag=2;
                           break;
                          }
                      }
                 }
              if (okFlag==2) continue;
              if (okFlag==0)
              if ((!getValue("parm.mcht_ename_sel",inti).equals("0"))&&
                  (getValue("mcht_eng_name").length()!=0))
                 {
                  setValue("datc.data_key"   , getValue("parm.active_code",inti));
                  setValue("datc.data_type"  , "B");
                  cnt1 =  getLoadData("datc.data_key,datc.data_type");
                  for (int intk=0;intk<cnt1;intk++)
                      {
                       indexInt = getValue("mcht_eng_name").toUpperCase().indexOf(getValue("datc.data_code",intk).toUpperCase());
                       if (indexInt!=-1)
                          {
                           if (getValue("parm.mcht_ename_sel",inti).equals("1"))
                               okFlag=1;
                           else 
                               okFlag=2;
                           break;
                          }
                      }
                 }
              if (okFlag!=1) continue;
             }

       insertMktChannelBill(inti);
      }

    processDisplay(300000); // every 10000 display message
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectMktChannelBill() throws Exception
 {
  selectSQL = "p_seqno,"
            + "vd_flag,"
            + "max(oppost_cond) as oppost_cond,"
            + "max(block_cond)  as block_cond,"
            + "max(payment_rate_cond)  as payment_rate_cond";
  daoTable  = "mkt_channel_bill";
  whereStr  = "where proc_date    = ? "
            + "and   (oppost_cond = 'Y' "
            + " or    payment_rate_cond  = 'Y' "
            + " or    block_cond  = 'Y') "
            + "group by p_seqno,vd_flag "
            ;
            
  setString(1, hBusinessDate);

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    if (getValue("oppost_cond").equals("Y"))
       {
        if (getValue("vd_flag").equals("N"))
           {
            setValue("card.p_seqno",getValue("p_seqno"));
            cnt1 =  getLoadData("card.p_seqno");
            if (cnt1==0)
               {
                updateMktChannelBill("01");
                continue;
               }
           }
        else
           {
            setValue("dcrd.p_seqno",getValue("p_seqno"));
            cnt1 =  getLoadData("dcrd.p_seqno");
            if (cnt1==0)
               {
                updateMktChannelBill("01");
                continue;
               }
           }
       }
    if (getValue("block_cond").equals("Y"))
       {
        setValue("ccat.p_seqno",getValue("p_seqno"));
        setValue("ccat.debit_flag",getValue("vd_flag"));
        cnt1 = getLoadData("ccat.p_seqno,ccar.debit_flag");
        if (cnt1!=0) 
           {
            updateMktChannelBill("02");
            continue;
           }
       }

    if (getValue("payment_rate_cond").equals("Y"))
       {
        setValue("acno.p_seqno",getValue("p_seqno"));
        cnt1 = getLoadData("acno.p_seqno");
        if (cnt1!=0) 
           {
            updateMktChannelBill("03");
            continue;
           }
       }

    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectMktChannelBill1() throws Exception
 {
  selectSQL = "card_no,"
            + "major_card_no,"
            + "rowid as rowid";
  daoTable  = "mkt_channel_bill";
  whereStr  = "where proc_date    = ? "
            + "and   error_code   = '00' "
            + "and   ori_card_no  = '' "
            ;
            
  setString(1, hBusinessDate);

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    setValue("cori.card_no",getValue("card_no"));
    cnt1 =  getLoadData("cori.card_no");
    if (cnt1==0) continue;
    if (getValue("card_no").equals(getValue("cori.ori_card_no"))) continue;
    setValue("ori_card_no" , getValue("cori.ori_card_no"));
    setValue("cori.card_no",getValue("major_card_no"));
    cnt1 =  getLoadData("cori.card_no");

    if (cnt1==0) setValue("ori_major_card_no" , getValue("major_card_no"));
    else setValue("ori_major_card_no" , getValue("cori.ori_card_no"));
    updateMktChannelBill1();

    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectMktChannelBill2(int inti) throws Exception
 {
  selectSQL = "id_p_seqno,"
            + "vd_flag";
  daoTable  = "mkt_channel_bill";
  whereStr  = "where proc_date    = ? "
            + "and   error_code   = '00' "
//          + "and   id_p_seqno   = '0001740658'  "  //debug 
            + "and   active_code  = ? "
            + "group by id_p_seqno,vd_flag "
            ;
            
  setString(1, hBusinessDate);
  setString(2, getValue("parm.active_code",inti));

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    if (getValue("vd_flag").equals("Y"))
       {
        setValue("dcio.id_p_seqno",getValue("id_p_seqno"));
        cnt1 = getLoadData("dcio.id_p_seqno");
        if (cnt1==0) 
           {
            showLogMessage("I","","active_code["+getValue("parm.active_code",inti)+"] VD id_p_seqno : ["+ getValue("id_p_seqno") +"]");
            continue;
           }
        setValue("id_no" , getValue("dcio.id_no"));
       }
     else
       {
        setValue("idno.id_p_seqno",getValue("id_p_seqno"));
        cnt1 = getLoadData("idno.id_p_seqno");
        if (cnt1==0) 
           {
            showLogMessage("I","","id_p_seqno : ["+ getValue("id_p_seqno") +"]");
            continue;
           }
        setValue("id_no" , getValue("idno.id_no"));
       }
    if (getValue("id_no").length()>10) 
       setValue("id_no" , comStr.left(getValue("id_no"), 10));
    updateMktChannelBill2();

    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectMktChannelBill3(int inti) throws Exception
 {
  selectSQL = "p_seqno,"
            + "mcht_no,"
            + "auth_code,"
            + "vd_flag,"
            + "purchase_date,"
            + "acct_month, "
            + "dest_amt,"
            + "rowid as rowid";
  daoTable  = "mkt_channel_bill";
  whereStr  = "where proc_date    = ? "
            + "and   error_code   = '00' "
            + "and   active_code  = ? "
            + "and   dest_amt     < 0 "
            ;
            
  setString(1, hBusinessDate);
  setString(2, getValue("parm.active_code",inti));

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   {
    if (selectMktChannelBill3A(inti)!=0)
       {
        updateMktChannelBill3A("06");  // 非本活動退貨
        continue;
       }
    updateMktChannelBill3A("07"); // 本活動退貨
     
    int destAmt = getValueInt("bil3.ori_dest_amt")
                 + getValueInt("bil3.refund_amt")
                 + getValueInt("dest_amt");
    if (destAmt<0) destAmt = 0;
    int refundAmt =  getValueInt("bil3.refund_amt")
                 + getValueInt("dest_amt");
    if (refundAmt+getValueInt("bil3.ori_dest_amt")<0)
       refundAmt = getValueInt("bil3.ori_dest_amt")*-1; 
    if (destAmt==0)
       updateMktChannelBill3B("08",destAmt,refundAmt);
    else
       updateMktChannelBill3B("00",destAmt,refundAmt);
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 void loadMktMchtgpData() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "b.data_key,"
            + "b.data_type,"
            + "a.data_code,"
            + "a.data_code2";
  daoTable  = "mkt_mchtgp_data a,mkt_bn_data b";
  //whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GROUP' "
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "               
            + "and   b.TABLE_NAME = 'MKT_CHANNEL_PARM' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            //+ "and   b.data_type  = '2' "
            + "and   b.data_type  = '6' "       //特店群組      
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();
  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");

  showLogMessage("I","","Load mkt_mchtgp_data Count: ["+n+"]");
 }
// ************************************************************************
 void loadMktBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_CHANNEL_PARM' "
            + "order by data_key,data_type,data_code,data_code2 ";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");
  setLoadData("data.data_key,data.data_type,data.data_code,data.data_code2");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
 }
// ************************************************************************
 void loadMktBnCdata() throws Exception
 {
  extendField = "datc.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code";
  daoTable  = "mkt_bn_cdata";
  whereStr  = "WHERE TABLE_NAME = 'MKT_CHANNEL_PARM' "
            + "order by data_key,data_type,data_code ";

  int  n = loadTable();

  setLoadData("datc.data_key,datc.data_type");

  showLogMessage("I","","Load mkt_bn_cdata Count: ["+n+"]");
 }
 
//************************************************************************
        void loadMktChantypeData() throws Exception
        {
        extendField = "chan.";
        selectSQL = "a.data_key," 
                  + "d.txcode_sel,"
                  + "d.tx_desc_type,"
                  + "d.tx_desc_name,"
                  + "d.mccc_sel,"
                  + "d.mccc_code";
        daoTable  = "mkt_bn_data a "
                + "inner join mkt_channel_parm b on a.data_key=b.active_code "
                + "inner join mkt_chantype_parm c on c.channel_type_id=a.data_code "
                + "inner join mkt_chantype_data d on c.channel_type_id=d.channel_type_id ";
        whereStr  = "WHERE a.TABLE_NAME='MKT_CHANNEL_PARM' "
                + "AND a.DATA_TYPE='15' "
                  + "order by d.channel_type_id ";
        
        int  n = loadTable();
        
        setLoadData("chan.data_key,chan.txcode_sel,chan.tx_desc_type,chan.tx_desc_name,chan.mccc_sel,chan.mccc_code");
        setLoadData("chan.data_key");
        
        showLogMessage("I","","Load mkt_chantype_data Count: ["+n+"]");
        }
         
// ************************************************************************
 int selectMktBnData(String col1, String sel, String dataType, int dataNum) throws Exception
 {
  return selectMktBnData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktBnData(String col1, String col2, String sel, String dataType, int dataNum) throws Exception
 {
  return selectMktBnData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktBnData(String col1, String col2, String col3, String sel, String dataType, int dataNum) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("data.data_key" , getValue("data_key"));
  setValue("data.data_type",dataType);

  int cnt1=0;
  if (dataNum==2)
     {
      cnt1 = getLoadData("data.data_key,data.data_type");
     }
  else if (dataNum==3)
     {
      setValue("data.data_code",col1);
      cnt1 = getLoadData("data.data_key,data.data_type,data.data_code");
     }
  else if (dataNum==4)
     {
      setValue("data.data_code",col1);
      setValue("data.data_code2",col2);
      cnt1 = getLoadData("data.data_key,data.data_type,data.data_code,data.data_code2");
     }


  int okFlag=0;
  for (int intm=0;intm<cnt1;intm++)
    {
     if (dataNum==2)
        {
         if (getValue("data.data_code",intm).length()!=0)
            {
             if (col1.length()!=0)
                {
                 if (!getValue("data.data_code",intm).equals(col1)) continue;
                }
              else
                {
                 if (sel.equals("1")) continue;
                }
            }
        }
     if (getValue("data.data_code2",intm).length()!=0)
        {
         if (col2.length()!=0)
            {
             if (!getValue("data.data_code2",intm).equals(col2)) continue;
            }
          else
            {
             continue;
            }
        }

     if (getValue("data.data_code3",intm).length()!=0)
        {
         if (col3.length()!=0)
            {
             if (!getValue("data.data_code3",intm).equals(col3)) continue;
            }
          else
            {
             continue;
            }
        }
     okFlag=1;
     break;
    }

  if (sel.equals("1"))
     {
      if (okFlag==0) return(1);
      return(0);
     }
  else
     {
      if (okFlag==0) return(0);
      return(1);
     }
 }
// ************************************************************************
 int selectMktMchtgpData(String col1, String col2, String sel, String dataType) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("mcht.data_key" , getValue("data_key"));
  setValue("mcht.data_type",dataType);
  setValue("mcht.data_code",col1);

  int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
  int okFlag=0;
  for (int inti=0;inti<cnt1;inti++)
    {
     if ((getValue("mcht.data_code2",inti).length()==0)||
         ((getValue("mcht.data_code2",inti).length()!=0)&&
          (getValue("mcht.data_code2",inti).equals(col2))))
        {
         okFlag=1;
         break;
        }
    }

  if (sel.equals("1"))
     {
      if (okFlag==0) return(1);
      return(0);
     }
  else
     {
      if (okFlag==0) return(0);
      return(1);
     }
 }
// ************************************************************************
 int deleteMktChannelBill(int inti) throws Exception
 {
  daoTable  = "mkt_channel_bill";
  whereStr  = "where  active_code   = ? ";

  setString(1 , getValue("parm.active_code",inti)); 

  int recCnt = deleteTable();

  showLogMessage("I","","刪除 mkt_channel_bill 筆數 :["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 int deleteMktChannelRank(int inti) throws Exception
 {
  daoTable  = "mkt_channel_rank";
  whereStr  = "where  active_code   = ? ";

  setString(1 , getValue("parm.active_code",inti)); 

  int recCnt = deleteTable();

  showLogMessage("I","","刪除 mkt_channel_rank 筆數 :["+ recCnt +"]");
  return(0);
 }
// ************************************************************************
 int selectMktChannelBill3A(int inti) throws Exception
 {
  extendField = "bil3.";
  selectSQL = "dest_amt,"
            + "ori_dest_amt,"
            + "refund_amt,"
            + "rowid as rowid";
  daoTable  = "mkt_channel_bill";
  whereStr  = "where active_code       = ? "
            + "and   vd_flag           = ? "
            + "and   p_seqno           = ? "
            + "and   mcht_no           = ? "
            + "and   auth_code         = ? "
  			+ "and   dest_amt          > 0  "
            + "and   error_code        = '00'  "
            ;

  setString(1 , getValue("parm.active_code",inti));
  setString(2 , getValue("vd_flag"));
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("mcht_no"));
  setString(5 , getValue("auth_code"));
  
  if("2".equals(getValue("parm.accumulate_term_sel",inti))){
	  whereStr += "and acct_month <= ? ";
	  setString(6 , getValue("acct_month"));
  }else {
	  whereStr += "and   purchase_date <= ?  ";
	  setString(6 , getValue("purchase_date"));
  }

  selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int insertMktChannelBill(int inti) throws Exception
 {
  extendField = "mcbl.";

  setValue("mcbl.active_code"          , getValue("parm.active_code",inti));
  setValue("mcbl.acct_type"            , getValue("acct_type"));
  setValue("mcbl.p_seqno"              , getValue("p_seqno"));
  setValue("mcbl.id_p_seqno"           , getValue("major_id_p_seqno"));
  setValue("mcbl.major_card_no"        , getValue("major_card_no")); 
  setValue("mcbl.ori_major_card_no"    , getValue("major_card_no")); 
  setValue("mcbl.card_no"              , getValue("card_no")); 
  setValue("mcbl.ori_card_no"          , getValue("card_no")); 
  setValue("mcbl.reference_no"         , getValue("reference_no")); 
  setValue("mcbl.acct_code"            , getValue("acct_code")); 
  if("2".equals(getValue("parm.accumulate_term_sel",inti)))
	  setValue("mcbl.acct_month"        , getValue("acct_month")); 
  else
	  setValue("mcbl.purchase_date"     , getValue("purchase_date")); 
  setValue("mcbl.dest_amt"             , getValue("dest_amt")); 
  setValue("mcbl.ori_dest_amt"         , getValue("dest_amt")); 
  setValue("mcbl.vd_flag"              , getValue("vd_flag")); 
  setValue("mcbl.mcht_no"              , getValue("mcht_no")); 
  setValue("mcbl.auth_code"            , getValue("auth_code")); 
  setValue("mcbl.block_cond"           , getValue("parm.block_cond",inti)); 
  setValue("mcbl.oppost_cond"          , getValue("parm.oppost_cond",inti)); 
  setValue("mcbl.payment_rate_cond"    , getValue("parm.payment_rate_cond",inti)); 
  setValue("mcbl.feedback_key_sel"     , getValue("parm.feedback_key_sel",inti)); 
  setValue("mcbl.platform_group_sel"   , getValue("parm.platform_group_sel",inti));
  setValue("mcbl.channel_type_sel"     , getValue("parm.channel_type_sel",inti));
  setValue("mcbl.error_code"           , "00"); 
  setValue("mcbl.proc_date"            , hBusinessDate); 
  setValue("mcbl.mod_time"             , sysDate+sysTime);
  setValue("mcbl.mod_pgm"              , javaProgram);

  daoTable  = "mkt_channel_bill";

  insertTable();

  return(0);
 }
// ************************************************************************
 int loadCrdCardOri() throws Exception
 {
  extendField = "cori.";
  selectSQL = "card_no,"
            + "ori_card_no,"
            + "major_card_no";
  daoTable  = "crd_card";
  whereStr  = "where card_no in ( "
            + "      select card_no "
            + "      from   mkt_channel_bill "
            + "      where  proc_date   = ? "
            + "      where  vd_flag     = 'N') "
            ;

  setString(1 , hBusinessDate);
 
  int  n = loadTable();
  setLoadData("cori.card_no");

  showLogMessage("I","","Load crd_card_ori Count: ["+n+"]");
  return(n);
 }
// ************************************************************************
 void loadCcaCardAcct() throws Exception
 {
  extendField = "ccat.";
  selectSQL = "p_seqno,"
            + "debit_flag";
  daoTable  = "cca_card_acct";
  whereStr  = "where block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 != '' "
            + "and   p_seqno in ( "
            + "      select distinct p_seqno " 
            + "      from  mkt_channel_bill "
            + "      where proc_date  = ?   "
            + "      and   block_cond = 'Y') "
            ;

  setString(1 , hBusinessDate);

  int  n = loadTable();
  setLoadData("ccat.p_seqno,ccar_debit_flag");

  showLogMessage("I","","Load cca_card_acct Count: ["+n+"]");
 }
// ************************************************************************
 void loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "distinct p_seqno as p_seqno";
  daoTable  = "crd_card";
  whereStr  = "where current_code = '0' "
            + "and   p_seqno in ( "
            + "      select distinct p_seqno " 
            + "      from  mkt_channel_bill "
            + "      where proc_date  = ?   "
            + "      and   vd_flag    = 'N'   "
            + "      and   oppost_cond = 'Y') "
            ;

  setString(1 , hBusinessDate);

  int  n = loadTable();
  setLoadData("card.p_seqno");

  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************
 void loadDbcCard() throws Exception
 {
  extendField = "dcrd.";
  selectSQL = "distinct p_seqno as p_seqno";
  daoTable  = "dbc_card";
  whereStr  = "where current_code = '0' "
            + "and   p_seqno in ( "
            + "      select distinct p_seqno " 
            + "      from  mkt_channel_bill "
            + "      where proc_date  = ?   "
            + "      and   vd_flag    = 'Y'   "
            + "      and   oppost_cond = 'Y') "
            ;

  setString(1 , hBusinessDate);

  int  n = loadTable();
  setLoadData("dcrd.p_seqno");

  showLogMessage("I","","Load dbc_card Count: ["+n+"]");
 }
// ************************************************************************
 void loadActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "p_seqno,"
            + "payment_rate1";
  daoTable  = "act_acno";
  whereStr  = "where payment_rate1 not in ('','0A','0B','0C','0D','0E') "
            + "and   p_seqno in ( "
            + "      select distinct p_seqno " 
            + "      from  mkt_channel_bill "
            + "      where proc_date  = ?   "
            + "      and   vd_flag    = 'N'   "
            + "      and   payment_rate_cond = 'Y') "
            ;

  setString(1 , hBusinessDate);

  int  n = loadTable();
  setLoadData("acno.p_seqno");

  showLogMessage("I","","Load act_acno Count: ["+n+"]");
 }
// ************************************************************************
 void loadCrdIdno(int inti) throws Exception
 {
  extendField = "idno.";
  selectSQL = "id_p_seqno,"
            + "id_no";
  daoTable  = "crd_idno";
  whereStr  = "where id_p_seqno in ( "
            + "      select distinct id_p_seqno " 
            + "      from  mkt_channel_bill "
            + "      where proc_date   = ?   "
            + "      and   active_code = ?  "
            + "      and   vd_flag     = 'N'  "
            + "      and   error_code  = '00')  "
            ;

  setString(1 , hBusinessDate);
  setString(2 , getValue("parm.active_code",inti));

  int  n = loadTable();
  setLoadData("idno.id_p_seqno");

  showLogMessage("I","","Load crd_idno Count: ["+n+"]");
 }
// ************************************************************************
 void loadDbcIdno(int inti) throws Exception
 {
  extendField = "dcio.";
  selectSQL = "id_p_seqno,"
            + "id_no";
  daoTable  = "dbc_idno";
  whereStr  = "where id_p_seqno in ( "
            + "      select distinct id_p_seqno " 
            + "      from  mkt_channel_bill "
            + "      where proc_date   = ?   "
            + "      and   active_code = ?  "
            + "      and   vd_flag     = 'Y'  "
            + "      and   error_code  = '00')  "
            ;

  setString(1 , hBusinessDate);
  setString(2 , getValue("parm.active_code",inti));

  int  n = loadTable();
  setLoadData("dcio.id_p_seqno");

  showLogMessage("I","","Load dbc_idno Count: ["+n+"]");
 }
// ************************************************************************
 int updateMktChannelBill(String errorCode) throws Exception
 {
  daoTable  = "mkt_channel_bill";
  updateSQL = "error_code        = ?";
  whereStr  = "where p_seqno  = ? "
            + "and   vd_flag  = ? "  
            ;

  setString(1 , errorCode);
  setString(2 , getValue("p_seqno"));
  setString(3 , getValue("vd_flag"));

  int n = updateTable();
  return n;
 }
// ************************************************************************
 int updateMktChannelBill2() throws Exception
 {
  daoTable  = "mkt_channel_bill";
  updateSQL = "id_no             = ?";
  whereStr  = "where id_p_seqno  = ? "
            + "and   vd_flag     = ? "
            ;

  setString(1 , getValue("id_no"));
  setString(2 , getValue("id_p_seqno"));
  setString(3 , getValue("vd_flag"));

  int n = updateTable();

  if (n==0)
     {
      showLogMessage("I","","UPS id_p_seqno : ["+ getValue("id_p_seqno") +"]");
      showLogMessage("I","","    id_no : ["+ getValue("id_no") +"]");
     }

  updateCnt++;
  return n;
 }
// ************************************************************************
 int updateMktChannelBill1() throws Exception
 {
  daoTable  = "mkt_channel_bill";
  updateSQL = "ori_card_no       = ?,"
            + "ori_major_card_no = ? "; 
  whereStr  = "where rowid    = ? "
            ;

  setString(1 , getValue("ori_card_no")); 
  setString(2 , getValue("ori_major_card_no")); 
  setRowId(3  , getValue("rowid"));

  int n = updateTable();
  return n;
 }
// ************************************************************************
 int updateMktChannelBill3A(String errorCode) throws Exception
 {
  daoTable  = "mkt_channel_bill";
  updateSQL = "error_code   = ? ";
  whereStr  = "where rowid  = ? "
            ;

  setString(1 , errorCode);
  setRowId(2  , getValue("rowid"));

  int n = updateTable();

  if (n==0) 
     {
      showLogMessage("I","","reference_no : ["+ getValue("reference") +"]");
     }
  return n;
 }
// ************************************************************************
 int updateMktChannelBill3B(String errorCode, int destAmt, int refundAmt) throws Exception
 {
  daoTable  = "mkt_channel_bill";
  updateSQL = "error_code   = ?,"
            + "dest_amt     = ?,"
            + "refund_amt   = ?";
  whereStr  = "where rowid  = ? "
            ;

  setString(1 , errorCode);
  setInt(2    , destAmt);
  setInt(3    , refundAmt);
  setRowId(4  , getValue("bil3.rowid"));

  int n = updateTable();

  if (n==0) 
     {
      showLogMessage("I","","reference_no : ["+ getValue("reference") +"]");
     }
  return n;
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
 void loadMktImchannelList() throws Exception
 {
  extendField = "milt.";
  selectSQL = "active_code,"
            + "id_p_seqno,"
            + "card_no ";
  daoTable  = "mkt_imchannel_list";
  whereStr  = "where active_code in ( "
            + "      select active_code "
            + "      from mkt_channel_parm "
            + "      where apr_flag      = 'Y' "
            + "      and     ((stop_flag = 'N') "
            + "       or      (stop_flag = 'Y' "
            + "        and     stop_date > ? )) "
            + "      and     feedback_apr_date = '' "
            + "      and     cal_def_date      = ?  "
            + "      and     list_cond         = 'Y' )  "
            + "and   list_flag != '1'    "
            ;

  setString(1 , hBusinessDate); 
  setString(2 , hBusinessDate); 
 
  int  n = loadTable();
  setLoadData("milt.id_p_seqno,milt.active_code");
  setLoadData("milt.card_no,milt.active_code");

  showLogMessage("I","","Load mkt_imchannel_list Count: ["+n+"]");
 }
// ************************************************************************
 void loadMktImchannelListCid() throws Exception
 {
  extendField = "milc.";
  selectSQL = "a.active_code,"
            + "b.id_p_seqno";
  daoTable  = "mkt_imchannel_list a,crd_idno b";
  whereStr  = "where active_code in ( "
            + "      select active_code "
            + "      from mkt_channel_parm "
            + "      where apr_flag      = 'Y' "
            + "      and     ((stop_flag = 'N') "
            + "       or      (stop_flag = 'Y' "
            + "        and     stop_date > ? )) "
            + "      and     feedback_apr_date = '' "
            + "      and     cal_def_date      = ?  "
            + "      and     list_cond         = 'Y' )  "
            + "and   a.list_data = b.id_no   "
            + "and   a.list_flag = '1'    "
            ;

  setString(1 , hBusinessDate); 
  setString(2 , hBusinessDate); 
 
  int  n = loadTable();
  setLoadData("milc.id_p_seqno,milc.active_code");

  showLogMessage("I","","Load mkt_imchannel_list_cid Count: ["+n+"]");
 }
// ************************************************************************
 void loadMktImchannelListVid() throws Exception
 {
  extendField = "milv.";
  selectSQL = "a.active_code,"
            + "b.id_p_seqno";
  daoTable  = "mkt_imchannel_list a,dbc_idno b";
  whereStr  = "where active_code in ( "
            + "      select active_code "
            + "      from mkt_channel_parm "
            + "      where apr_flag      = 'Y' "
            + "      and     ((stop_flag = 'N') "
            + "       or      (stop_flag = 'Y' "
            + "        and     stop_date > ? )) "
            + "      and     feedback_apr_date = '' "
            + "      and     cal_def_date      = ?  "
            + "      and     list_cond         = 'Y' )  "
            + "and   a.list_data = b.id_no  "
            + "and   a.list_flag = '1'    "
            ;

  setString(1 , hBusinessDate); 
  setString(2 , hBusinessDate); 
 
  int  n = loadTable();
  setLoadData("milv.id_p_seqno,milv.active_code");

  showLogMessage("I","","Load mkt_imchannel_list_vid Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktChannelBillA(int inti) throws Exception
 {
  daoTable  = "mkt_channel_bill";
  if (getValue("parm.feedback_key_sel",inti).equals("1"))
      selectSQL = "id_no,";
  else if (getValue("parm.feedback_key_sel",inti).equals("2"))
      selectSQL = "p_seqno,"
                + "vd_flag,";
  else if (getValue("parm.feedback_key_sel",inti).equals("3"))
      selectSQL = "major_card_no,";
  else if (getValue("parm.feedback_key_sel",inti).equals("4"))
      selectSQL = "card_no,";

  selectSQL += "2".equals(getValue("parm.accumulate_term_sel",inti))? "acct_month,":"purchase_date,";
  selectSQL += "dest_amt,"
            + "rowid as rowid";

  whereStr  = "where proc_date    = ? "
            + "and   error_code   = '00' "
            + "and   active_code  = ? "
            + "and   dest_amt     > 0 "
            ;

  if (getValue("parm.feedback_key_sel",inti).equals("1"))
     whereStr += "2".equals(getValue("parm.accumulate_term_sel",inti))
     	?"order by id_no,acct_month,dest_amt desc ":"order by id_no,purchase_date,dest_amt desc ";
  else if (getValue("parm.feedback_key_sel",inti).equals("2"))
     whereStr += "2".equals(getValue("parm.accumulate_term_sel",inti))
     	?"order by p_seqno,vd_flag,acct_month,dest_amt desc ":"order by p_seqno,vd_flag,purchase_date,dest_amt desc "; 
  else if (getValue("parm.feedback_key_sel",inti).equals("3"))
     whereStr += "2".equals(getValue("parm.accumulate_term_sel",inti)) 
        ?"order by major_card_no,acct_month,dest_amt desc ":"order by major_card_no,purchase_date,dest_amt desc "; 
  else if (getValue("parm.feedback_key_sel",inti).equals("4"))
     whereStr += "2".equals(getValue("parm.accumulate_term_sel",inti))
     	?"order by card_no,acct_month,dest_amt desc ":"order by card_no,purchase_date,dest_amt desc "; 
            
  setString(1, hBusinessDate);
  setString(2, getValue("parm.active_code",inti));

  openCursor();

  totalCnt=0;

  String idNo="";
  String pSeqno="";
  String vdFlag="";
  String cardNo="";
  String purchaseDate="";
  String acctMonth = "";
  int  calCnt=0;

  while( fetchTable() ) 
   { 
	if("2".equals(getValue("parm.accumulate_term_sel",inti)))
		if (!getValue("acct_month").equals(acctMonth)) 
		{
			calCnt=0;
			acctMonth = getValue("acct_month");
		}
	else
		if (!getValue("purchase_date").equals(purchaseDate)) 
		{
			calCnt=0;
			purchaseDate = getValue("purchase_date");
		}
    if (getValue("parm.feedback_key_sel",inti).equals("1"))
       {
        if (!getValue("id_no").equals(idNo)) 
           {
            calCnt=0;
            idNo = getValue("id_no");
           }
       }
    else if (getValue("parm.feedback_key_sel",inti).equals("2"))
       {
        if ((!getValue("p_seqno").equals(pSeqno))|| 
            (!getValue("vd_flag").equals(vdFlag)))
           {
            calCnt=0;
            pSeqno = getValue("p_seqno");
            vdFlag = getValue("vd_flag");
           }
       }
    else if (getValue("parm.feedback_key_sel",inti).equals("3"))
       {
        if (!getValue("major_card_no").equals(cardNo)) 
           {
            calCnt=0;
            cardNo = getValue("major_card_no");
           }
       }
    else if (getValue("parm.feedback_key_sel",inti).equals("4"))
       {
        if (!getValue("card_no").equals(cardNo)) 
           {
            calCnt=0;
            cardNo = getValue("card_no");
           }
       }

    calCnt++;
    if (getValueInt("parm.perday_cnt",inti)>= calCnt) continue;

    updateMktChannelBillA("04");
    totalCnt++;
    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int updateMktChannelBillA(String errorCode) throws Exception
 {
  daoTable  = "mkt_channel_bill";
  updateSQL = "error_code    = ?";
  whereStr  = "where rowid   = ? "
            ;

  setString(1 , errorCode);
  setRowId(2  , getValue("rowid"));

  int n = updateTable();
  return n;
 }
// ************************************************************************
 int selectMktChannelBillB(int inti) throws Exception
 {
  daoTable  = "mkt_channel_bill";
  if (getValue("parm.feedback_key_sel",inti).equals("1"))
      selectSQL = "id_no,";
  else if (getValue("parm.feedback_key_sel",inti).equals("2"))
      selectSQL = "p_seqno,"
                + "vd_flag,";
  else if (getValue("parm.feedback_key_sel",inti).equals("3"))
      selectSQL = "major_card_no,";
  else if (getValue("parm.feedback_key_sel",inti).equals("4"))
      selectSQL = "card_no,";

  selectSQL = selectSQL
            + "dest_amt,"
            + "rowid as rowid";

  whereStr  = "where proc_date    = ? "
            + "and   error_code   = '00' "
            + "and   active_code  = ? "
            + "and   dest_amt     > 0 "
            ;

  if (getValue("parm.feedback_key_sel",inti).equals("1"))
     whereStr  = whereStr 
               + "order by id_no,dest_amt desc ";
  else if (getValue("parm.feedback_key_sel",inti).equals("2"))
     whereStr  = whereStr 
               + "order by p_seqno,vd_flag,dest_amt desc "; 
  else if (getValue("parm.feedback_key_sel",inti).equals("3"))
     whereStr  = whereStr 
               + "order by major_card_no,dest_amt desc "; 
  else if (getValue("parm.feedback_key_sel",inti).equals("4"))
     whereStr  = whereStr 
               + "order by card_no,dest_amt desc "; 
            
  setString(1, hBusinessDate);
  setString(2, getValue("parm.active_code",inti));

  openCursor();

  totalCnt=0;

  String idNo="";
  String pSeqno="";
  String vdFlag="";
  String cardNo="";
  int  calCnt=0;

  while( fetchTable() ) 
   { 
    if (getValue("parm.feedback_key_sel",inti).equals("1"))
       {
        if (!getValue("id_no").equals(idNo)) 
           {
            calCnt=0;
            idNo = getValue("id_no");
           }
       }
    else if (getValue("parm.feedback_key_sel",inti).equals("2"))
       {
        if ((!getValue("p_seqno").equals(pSeqno))|| 
            (!getValue("vd_flag").equals(vdFlag)))
           {
            calCnt=0;
            pSeqno = getValue("p_seqno");
            vdFlag = getValue("vd_flag");
           }
       }
    else if (getValue("parm.feedback_key_sel",inti).equals("3"))
       {
        if (!getValue("major_card_no").equals(cardNo)) 
           {
            calCnt=0;
            cardNo = getValue("major_card_no");
           }
       }
    else if (getValue("parm.feedback_key_sel",inti).equals("4"))
       {
        if (!getValue("card_no").equals(cardNo)) 
           {
            calCnt=0;
            cardNo = getValue("card_no");
           }
       }

    calCnt++;
    if (getValueInt("parm.max_cnt",inti)>= calCnt) continue;

    updateMktChannelBillA("05");
    totalCnt++;
    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectBilContract(int inti) throws Exception
 {
  selectSQL = "1 as check_flag";
  daoTable  = "bil_contract";
  whereStr  = "WHERE contract_no = ? "
            + "and   refund_apr_date !='' "
            + "and   refund_apr_date <= ? "
            ;

  setString(1 , getValue("contract_no"));
  setString(2 , getValue("parm.cal_def_date",inti));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return 0;

  return 1;
 }
// ************************************************************************
    private boolean selectMktGpDataDataCode(String mchtNo) throws Exception {
        sqlCmd = " select data_code ";
        sqlCmd += " from mkt_mchtgp_data ";
        sqlCmd += " where table_name = 'MKT_MCHT_GP' ";
        sqlCmd += "   and data_key = 'MKTNCUS00' ";
        sqlCmd += "   and data_code = ? ";
        setString(1, mchtNo);
        int recordCnt = selectTable();
        return recordCnt > 0;
    }
// ************************************************************************
    private boolean selectBilBillChiName(String mchtChiName) throws Exception {
        sqlCmd = " select distinct substr(mcht_chi_name, 1, 4) ";
        sqlCmd += " from bil_bill ";
        sqlCmd += " where mcht_chi_name = ? ";
        sqlCmd += "   and substr(mcht_chi_name, 1, 2) in ('f%', 'G%', 'd%', 'M%', 'b%', 'e%', 'V%', 'A%', '$%', '#%') ";
        setString(1, mchtChiName);
        int recordCnt = selectTable();
        return recordCnt > 0;
    }
    
    private void getbillWhere(int index) throws Exception {
    	  if(purchaseDateflag == true && acctMonths.size() > 0) {
    		  whereStr += " and (purchase_date between ? and ? ";
    		  setString(index++ , minDate);
    		  setString(index++ , maxDate);
    		  whereStr += " or acct_month in ( ";
    		  for(int i=0;i<acctMonths.size();i++) {
    			  if(i == 0)
    				  whereStr += "?";
    			  else
    				  whereStr += ",?";
    			  setString(index++ , acctMonths.get(i));
    		  }
    		  whereStr += " )) ";
    	  }
    	  
    	  if(purchaseDateflag == true && acctMonths.size() < 0) {
    		  whereStr += " and purchase_date between ? and ? ";
    		  setString(index++ , minDate);
    		  setString(index++ , maxDate);
    	  }
    	  
      	  if(purchaseDateflag == false && acctMonths.size() > 0) {
    		  whereStr += " and acct_month in ( ";
    		  for(int i=0;i<acctMonths.size();i++) {
    			  if(i == 0)
    				  whereStr += "?";
    			  else
    				  whereStr += ",?";
    			  setString(index++ , acctMonths.get(i));
    		  }
    		  whereStr += " ) ";
    	  }
    }

}  // End of class FetchSample