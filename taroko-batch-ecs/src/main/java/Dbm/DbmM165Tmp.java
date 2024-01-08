/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/12/23  V1.00.00             DbmM165   tcb New Visa Debit 請款每月給點  *
* 112/04/17  V1.00.01    Ryan     新增 p_seqno                                      *
******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM165Tmp extends AccessDAO
{
 private final String PROGNAME = "Debit紅利-請款新增點數處理程式 112/04/17  V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String hbusinessDate = "";
 String acctDate = "";
 String dataKey = "";
 String idPSeqno = "";

 long    totalCnt=0;
 int parmCnt=0,cnt1=0;
 int[] runInt = {0,0,0,0,0,0,0,0,0,0};
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM165Tmp proc = new DbmM165Tmp();
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
       showLogMessage("I","","本程式已有另依程序啟動中, 不執行..");
       return(0);
      }

//   if (args.length > 2)
//      {
//       showLogMessage("I","","請輸入參數:");
//       showLogMessage("I","","PARM 1 : [business_date]");
//       showLogMessage("I","","PARM 2 : [id_p_seqno]");
//       return(1);
//      }

//   if ( args.length >= 1 )
//      { businessDate = args[0]; }
   hbusinessDate = "20240101";
   acctDate = "20240102";
//
//   if ( args.length ==2 )
//      { idPSeqno = args[1]; }

   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
   setValue("wday.this_acct_month","202402");
//   if (selectPtrWorkday()!=0)
//   {
//    showLogMessage("I","","本日非關帳日, 不需執行");
//    return(0);
//   }
   showLogMessage("I","","this_acct_month["+getValue("wday.this_acct_month")+"]");
   
   showLogMessage("I","","=========================================");
//   if (!businessDate.substring(6,8).equals("01"))
//   {
//    showLogMessage("I","","本日非關帳日1號,本日為"+ businessDate +"日..");
//    showLogMessage("I","","=========================================");
//    return(0);
//   }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadDbmBnData();
   loadMktMchtgpData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectDbmBpid();
   selectDbmSysparm();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理請款資料");
   selectDbbBill();
   showLogMessage("I","","=========================================");

   if (idPSeqno.length()==0) finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
 {
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (hbusinessDate.length()==0)
      hbusinessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ hbusinessDate +"]");
 }
 
 int  selectPtrWorkday() throws Exception
 {
//  extendField = "wday.";
//  selectSQL = "";
//  daoTable  = "ptr_workday";
//  whereStr  = "where this_close_date = ? ";
//
//  setString(1,businessDate);
//
//  int recCnt = selectTable();

//  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************ 
 public void selectDbbBill() throws Exception
 {
  selectSQL = "a.mcht_no,"
            + "a.dest_amt as deduct_amt," 
            + "a.acct_type,"
            + "a.reference_no,"
            + "a.id_p_seqno,"
            + "a.mcht_category,"
            + "a.pos_entry_mode,"  
            + "decode(a.mcht_category,'6010','3','6011','3', "
            + "decode(a.mcht_country,'TW','1',  decode(a.mcht_country,'TWN','1' , decode(a.mcht_country,'','1','2') ) ) "
            + ") as item_code, "       
            + "a.group_code,"
            + "a.purchase_date,"
            + "a.card_no,"
            + "a.rowid as rowid,"
            + "a.acq_member_id,"
            + "a.sign_flag, "
            + "a.ecs_cus_mcht_no "
            + ",(case when a.p_seqno = '' or a.p_seqno = '0' "
            + " then nvl((select p_seqno from dbc_card where card_no = a.card_no Fetch first row only),'') "
            + " else a.p_seqno end ) as p_seqno ";
  daoTable  = "dbb_bill a";
  whereStr  = "where 1=1 "
            + "and   post_date  = ? "
            ;

  setString(1,this.hbusinessDate);
  
// idPSeqno ReDo :   
   if (idPSeqno.length()==0)
      {
//       whereStr  = whereStr  
//                 + "and    bonus_date = '' ";
      }
   else
      {
       whereStr  = whereStr  
                 + "and    id_p_seqno = ? "   		   
//               + "and    bonus_date = ? "
                 ;
       setString(3 , idPSeqno);       
//     setString(4 , businessDate);
      }

  openCursor();

  String acqId ="";
  while( fetchTable() ) 
   {
	//System.out.println("sign_flag = " +getValue("sign_flag"));
	  
    if (getValue("sign_flag").equals("-"))
		setValueDouble("deduct_amt", getValueDouble("deduct_amt") * -1);
	else
//		setValueDouble("deduct_amt", getValueDouble("deduct_amt") + getValueDouble("curr_adjust_amt"));
		setValueDouble("deduct_amt", getValueDouble("deduct_amt"));
    
    //System.out.println("deduct_amt = " +getValue("deduct_amt"));
    //System.out.println();
    //System.out.println();
    if (getValue("group_code").length()==0) setValue("group_code", "0000");

    for (int inti=0;inti<parmCnt;inti++)
        {
         if (!getValue("item_code").equals(getValue("parm.item_code",inti))) continue;
         if (!getValue("acct_type").equals(getValue("parm.acct_type",inti))) continue;

         setValue("data_key" , getValue("parm.years",inti)
                             + getValue("parm.acct_type",inti)
                             + getValue("parm.item_code",inti));

         acqId = "";
         if (getValue("acq_member_id").length()!=0)
            acqId = comm.fillZero(getValue("acq_member_id"),8);

         if (selectDbmBnData(getValue("mcht_no"),acqId,
                                getValue("parm.merchant_sel",inti),"1",3)!=0) 
            {
//           updateDbbBill();
             continue;
            }

         if (selectDbmBnData(getValue("group_code"),
                                getValue("parm.group_code_sel",inti),"2",3)!=0)
            {
//           updateDbbBill();
             continue;
            }

         if (selectDbmBnData(getValue("pos_entry_mode"),
                                getValue("parm.pos_entry_sel",inti),"4",3)!=0)
            {
//           updateDbbBill();
             continue;
            }

/*
         if (select_dbm_bn_data(getValue("mcht_category"),
                                getValue("parm.mcc_code_sel",inti),"5",3)!=0)
            {
             update_dbb_bill();
             continue;
            }
*/

         if (selectMktMchtgpData(getValue("mcht_no"),acqId,
                                    getValue("parm.mcht_group_sel",inti),"6")!=0)
            {
//           updateDbbBill();
             continue;
            }
         

         if (selectMktMchtgpData(getValue("ecs_cus_mcht_no"),"",
                                    getValue("parm.platform_kind_sel",inti),"P")!=0)
            {
//           updateDbbBill();
             continue;
            }

         setValue("bdtl.active_code" ,  getValue("data_key"));
//         setValue("bdtl.active_name" , " VD消費新增紅利點數");
         CommString comms = new CommString();
         setValue("bdtl.active_name" , String.format("%s每滿新臺幣 %d 元自動累積 %d 點"
        		 ,comms.decode(getValue("item_code"), ",1,2,3", ",國內消費,國外消費,國外提款")
        		 ,getValueLong("parm.bp_amt"),getValueLong("parm.bp_pnt")));

         setValue("bdtl.tran_code" , "1");
//         setValue("bdtl.mod_desc"  , "VD紅利消費新增贈送");
         if(getValueInt("deduct_amt") < 0) {
        	 setValue("bdtl.mod_memo"  , "退貨調整");
         }else {
        	 setValue("bdtl.mod_memo"  , "VD紅利消費新增贈送");
         }
          setValue("bdtl.tax_flag"  , "N");

         int deductBp = 0;
         if (getValue("parm.bp_type",inti).equals("1"))
            {
              deductBp = getValueInt("parm.give_bp",inti);
              if(getValueInt("deduct_amt") < 0) {
            	  deductBp = deductBp * -1;
              }
            }
         else
            {
              if (getValueInt("deduct_amt") >= getValueInt("parm.bp_amt",inti) )
                 {
                  //int deductBpInt = (int)Math.ceil(getValueInt("deduct_amt")
                  //                                 /getValueInt("parm.bp_amt",inti));
                  int deductBpInt = (int)Math.floor(getValueInt("deduct_amt")
                                                   /getValueInt("parm.bp_amt",inti));            	  
                  deductBp = (int)Math.round(deductBpInt*getValueInt("parm.bp_pnt",inti));
                 }
              //if(getValueInt("deduct_amt") < 0 && getValueInt("deduct_amt") <= getValueInt("parm.bp_amt",inti)) {
              if(getValueInt("deduct_amt") < 0 && Math.abs(getValueInt("deduct_amt")) >= getValueInt("parm.bp_amt",inti)) {          	  
            	  //int deductBpInt = (int)Math.ceil(getValueInt("deduct_amt")
                  //        /getValueInt("parm.bp_amt",inti));
            	  int deductBpInt = (int)Math.floor(Math.abs(getValueInt("deduct_amt"))
                            /getValueInt("parm.bp_amt",inti));            	  
            	  //deductBp = (int)Math.round(deductBpInt*getValueInt("parm.bp_pnt",inti));
            	  deductBp = deductBpInt*getValueInt("parm.bp_pnt",inti) * -1 ;
              }
            }

         if (deductBp==0)
            {
//           updateDbbBill();
             continue;
            }

         setValueInt("bdtl.beg_tran_bp"  , deductBp);
         setValueInt("bdtl.end_tran_bp"  , deductBp);
         insertDbmBonusDtl();
//       updateDbbBill();

        }
    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();

//for (int inti=0;inti<5;inti++)
//    showLogMessage("I","","inti ["+inti+"] ["+data_cnt[inti]+"]");

 }
// ************************************************************************
 public int updateDbbBill() throws Exception
 {
  updateSQL = "bonus_date  = ?,"
            + "mod_pgm     = ?,"
            + "mod_time    = sysdate";
  daoTable  = "dbb_bill";
  whereStr  = "WHERE rowid = ? ";

  setString(1 , hbusinessDate);
  setString(2 , javaProgram);
  setRowId(3  , getValue("rowid"));

  int cnt = updateTable();

  return(0);
 }
// ************************************************************************
 void loadDbmBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "dbm_bn_data b";
  whereStr  = "WHERE TABLE_NAME = 'DBM_BPID' "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();
  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load dbm_bn_data: ["+n+"]");
 }
// ************************************************************************
 int selectDbmBnData(String col1, String sel, String dataType, int dataNum) throws Exception
 {
  return selectDbmBnData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectDbmBnData(String col1, String col2, String sel, String dataType, int dataNum) throws Exception
 {
  return selectDbmBnData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectDbmBnData(String col1, String col2, String col3, String sel, String dataType, int dataNum) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("data.data_key" , getValue("data_key"));
  setValue("data.data_type",dataType);

  int cnt1=0;
  if (dataNum==2)
     {
      cnt1 = getLoadData("data.data_key,data.data_type");
     }
  else
     {
      setValue("data.data_code",col1);
      cnt1 = getLoadData("data.data_key,data.data_type,data.data_code");
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
 int selectDbmBpid() throws Exception
 {
  extendField = "parm.";
  daoTable  = "dbm_bpid";
  whereStr  = "WHERE apr_flag = 'Y' "
            + "and   years = ? ";

  setString(1, hbusinessDate.substring(0,4));              

  parmCnt = selectTable();

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
 }
// ************************************************************************
public int insertDbmBonusDtl() throws Exception
 {
  setValue("bdtl.tran_seqno"     , comr.getSeqno("ECS_DBMSEQ"));

  extendField = "bdtl.";
  setValue("bdtl.acct_date"            , acctDate);
  setValue("bdtl.tran_date"            , sysDate);
  setValue("bdtl.tran_time"            , sysTime);
  setValue("bdtl.crt_date"             , sysDate);
  setValue("bdtl.crt_user"             , javaProgram);
  setValue("bdtl.apr_date"             , sysDate);
  setValue("bdtl.apr_user"             , javaProgram);
  setValue("bdtl.apr_flag"             , "Y");
//  setValue("bdtl.acct_month"           , businessDate.substring(0,6));
  setValue("bdtl.acct_month"           , getValue("wday.this_acct_month"));
  setValue("bdtl.bonus_type"           , "BONU");
  setValue("bdtl.batch_no"             , getValue("reference_no"));
  setValue("bdtl.acct_type"            , getValue("acct_type"));
  setValue("bdtl.card_no"              , getValue("card_no"));
  setValue("bdtl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("bdtl.tran_pgm"             , javaProgram);
  setValue("bdtl.effect_e_date"        , "");
  setValue("bdtl.effect_flag"          , "");
  setValue("bdtl.tax_flag"        , "N");
 
  if (getValueInt("bdtl.end_tran_bp")>0)
     {
      setValue("bdtl.effect_e_date"    , comm.nextMonthDate(hbusinessDate,getValueInt("dbmp.effect_months")));
     }else
      setValue("bdtl.effect_e_date" ,"");
  setValue("bdtl.p_seqno", getValue("p_seqno"));
  setValue("bdtl.mod_time"             , sysDate+sysTime);
  setValue("bdtl.mod_pgm"              , javaProgram);
  daoTable  = "dbm_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectDbmSysparm() throws Exception
 {
  extendField = "dbmp.";
  selectSQL = "effect_months";
  daoTable  = "dbm_sysparm";
  whereStr  = "WHERE parm_type = '01' "
            + "and   apr_date !='' ";

  int recCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select dbm_sysparm error!" );
      exitProgram(0);
     }
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
  daoTable  = "mkt_mchtgp_data a,dbm_bn_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'DBM_BPID' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type  in ('6','P') "
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();

  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");

  showLogMessage("I","","Load mkt_mchtgp_data Count: ["+n+"]");
 }

// ************************************************************************
	int selectMktMchtgpData(String col1, String col2, String sel, String dataType) throws Exception {
		if (sel.equals("0"))
			return (0);

		setValue("mcht.data_key", getValue("data_key"));
		setValue("mcht.data_type", dataType);
		setValue("mcht.data_code", col1);

		int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
		int okFlag = 0;
		for (int inti = 0; inti < cnt1; inti++) {
			if ("P".equals(dataType)) {
				okFlag = 1;
				break;
			} else {
				if ((getValue("mcht.data_code2", inti).length() == 0)
						|| ((getValue("mcht.data_code2", inti).length() != 0)
								&& (getValue("mcht.data_code2", inti).equals(col2)))) {
					okFlag = 1;
					break;
				}
			}

		}

		if (sel.equals("1")) {
			if (okFlag == 0)
				return (1);
			return (0);
		} else {
			if (okFlag == 0)
				return (0);
			return (1);
		}
	}
// ************************************************************************

/*
  1. update dbb_bill
     update dbb_bill a
     set bonus_date = ''
     where  bonus_date = '20201024'
     and    acct_date  != ''
     and    acct_month > '201202' 
     and    mcht_category = '5542'
     and    exists  (select ica_no
                    from   mkt_rcv_bin
                    where  bank_no != '300'
                    and    ica_no   = decode(a.acq_member_id,'','x',a.acq_member_id))
     and    a.sign_flag  != '-'

  2.delete dbm_bonus_dtl
    delete db_bonus_dtl
    where acct_date = '20201024'
    and   tran_pgm = 'DbmM160'

*/

}  // End of class FetchSample
