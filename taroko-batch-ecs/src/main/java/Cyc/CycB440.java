/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 107/11/13  V1.00.00  Allen Ho   cyc_a440                                   *
 * 109-12-21  V1.00.01  tanwei      updated for project coding standard       *
 * 110/02/20  V1.00.02  JeffKung   fulfilled TCB requirement             *
 ******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycB440 extends AccessDAO
{
    private  String progname = "免年費- 卡友免年費處理程式  110/02/20 V1.00.02";
    CommFunction comm = new CommFunction();

    String hBusiBusinessDate   = "";
    String hWdayStmtCycle      = "";
    String hWdayThisAcctMonth = "";
    boolean debugSw = false;

    double[] totalAmt = new double[3]; // 0:major_sub 1:major 2:sub
    double[][] monthAmt = new double[13][3]; // 0:major_sub 1:major 2:sub && acct_month
    int[] procCnt =  {0,0,0,0,0};

    long    totalCnt=0,updateCnt=0;
    int inti,parmCnt=0,cnt1=0,cnt2=0;
    String reasonCode = "";
    // ************************************************************************
    public static void main(String[] args) throws Exception
    {
        CycB440 proc = new CycB440();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
    }
    // ************************************************************************
    public int mainProcess(String[] args) {
        try
        {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I","",javaProgram+" "+progname);

            if (comm.isAppActive(javaProgram))
            {
                showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
                return(0);
            }

            if (args.length > 2)
            {
                showLogMessage("I","","請輸入參數:");
                showLogMessage("I","","PARM 1 : [business_date]");
                return(1);
            }
            
            //set DEBUG mode
            for (int argi=0; argi < args.length ; argi++ ) {
          	  if (args[argi].equalsIgnoreCase("debug")) {
          		  debugSw=true;
          	  }
            }

            if ( args.length > 0 && args[0].length() == 8  )
            { hBusiBusinessDate = args[0]; }

            if ( !connectDataBase() ) exitProgram(1);

            selectPtrBusinday();

            if (selectPtrWorkday()!=0)
            {
                showLogMessage("I","","本日非關帳日次一日, 不需執行");
                return(0);
            }

            showLogMessage("I","","this_acct_month["+hWdayThisAcctMonth+"]");
            showLogMessage("I","","處理月份: ["+ comm.nextMonth(hWdayThisAcctMonth,-12)+"]["+comm.nextMonth(hWdayThisAcctMonth,-1)+"]");
            showLogMessage("I","","=========================================");

            selectCycAnulGp();
            //loadMktCardConsume();
            loadMktGpcardExp();
            loadMktBnData();
            loadMktMchtgpData();

            selectActAcno();

            showLogMessage("I","","處理 ["+totalCnt+"] 筆, 更新 ["+updateCnt+"] 筆");

            finalProcess();

            return 0;
        }

        catch ( Exception ex )
        { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

    } // End of mainProcess
    // ************************************************************************
    public void  selectPtrBusinday() throws Exception
    {
        daoTable  = "PTR_BUSINDAY";
        whereStr  = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if ( notFound.equals("Y") )
        {
            showLogMessage("I","","select ptr_businday error!" );
            exitProgram(1);
        }

        if (hBusiBusinessDate.length()==0)
            hBusiBusinessDate   =  getValue("BUSINESS_DATE");
        showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
    }
    // ************************************************************************
    public int  selectPtrWorkday() throws Exception
    {
        selectSQL = "";
        daoTable  = "ptr_workday";
        whereStr  = "where this_close_date = ? ";

        setString(1,comm.lastDate(hBusiBusinessDate));

        int recCnt = selectTable();

        if ( notFound.equals("Y") ) return(1);

        hWdayStmtCycle      =  getValue("STMT_CYCLE");
        hWdayThisAcctMonth =  getValue("this_acct_month");

        return(0);
    }
    // ************************************************************************
    void  selectActAcno() throws Exception
    {
    	selectSQL = "a.p_seqno,"
                + "a.acno_p_seqno,"
                + "a.id_p_seqno,"
    			+ "a.payment_rate1,"
                + "b.card_no,"
                + "b.group_code,"
                + "b.card_type,"
                + "b.sup_flag,"
                + "b.purch_review_month_beg, "
                + "b.purch_review_month_end, "
                + "case when a.stat_send_internet = 'Y' and decode(a.STAT_SEND_E_MONTH2,'','99991231',a.STAT_SEND_E_MONTH2) >= ? then 'Y' else 'N' end "
                + " as email_flag, " 
                + "b.rowid as rowid";
        daoTable  = "act_acno a ,cyc_afee b";
        whereStr  = "where  a.p_seqno         = b.p_seqno "
                + "AND   exists (select 1 from cyc_anul_gp h"
                + "              where  h.group_code = b.group_code "
                + "              and    h.card_type = b.card_type "
                + "              fetch first 1 rows only) "
                + "AND    b.maintain_code  != 'Y' "
                + "AND    b.rcv_annual_fee !=  0 "
                + "AND    (a.stmt_cycle = ?  "
                + " or     b.stmt_cycle = ? ) "
        ;

        setString(1 , hWdayThisAcctMonth);
        setString(2 , hWdayStmtCycle);
        setString(3 , hWdayStmtCycle);

        openCursor();

        totalCnt=0;

        while( fetchTable() )
        {
            totalCnt++;
            reasonCode = "";

             if (debugSw)
            {
            	showLogMessage("I","","Input_1 - card_no : ["+getValue("card_no")+"]" + "group_code : ["+getValue("group_code")+"]" 
            			+ "card_type : ["+getValue("card_type")+"]" + "sup_flag : ["+getValue("sup_flag")+"]");
            }
            	
            for (int inti=0;inti<parmCnt;inti++)
                {
                 if (!getValue("parm.group_code",inti).equals(getValue("group_code"))) continue;

                 if (getValue("parm.card_type",inti).length()!=0)
                    if (!getValue("parm.card_type",inti).equals(getValue("card_type"))) continue;
                 
                 if (debugSw)
                 {
                 	showLogMessage("I","","parm - mcode : ["+getValue("parm.mcode",inti)+"]" 
                 			+ " major_sub : ["+getValue("parm.major_sub",inti)+"]" 
                 			+ " major_flag : ["+getValue("parm.major_flag",inti)+"]"
                 			+ " sub_flag : ["+getValue("parm.sub_flag",inti)+"]");
                 }

                 if (getValue("parm.mcode",inti).length()!=0)
                    {
                     if ((getValue("payment_rate1").compareTo("0A")>=0)&&
                         (getValue("payment_rate1").compareTo("0E")<=0))
                        {
                        }
                     else
                        {
                    	  if (getValue("payment_rate1").compareTo(
                              String.format("%02d", Integer.parseInt(getValue("parm.mcode",inti))))>=0) {
                    		  reasonCode = "D1";
                    		  updateCycAfee(inti);
                    		  continue;
                    	  }
                        }
                    }

                 cnt1 = selectMktCardConsume();
                 
                 if (checkBProc(inti)!=0)
                 {
                	 if (debugSw)
                     {
                     	showLogMessage("I","","check_b_proc not pass");
                     }
                	 if (checkCProc(inti)!=0)
                	 {
                		 if (debugSw)
                         {
                         	showLogMessage("I","","check_c_proc not pass");
                         }
                		 if (checkAProc(inti)!=0) 
                		 {
                			 if (debugSw)
                             {
                             	showLogMessage("I","","check_a_proc not pass");
                             }
                			 if (getValue("parm.email_nopaper_flag",inti).equals("Y") &&
                                 getValue("email_flag").equals("Y"))
                			 {
                				 reasonCode = "E1";
                			 } else {
                				 if (debugSw)
                                 {
                                 	showLogMessage("I","","check_email_nopaper not pass");
                                 }
                				 continue;
                			 }
                		 } else {
                			 if (debugSw)
                             {
                             	showLogMessage("I","","check_a_proc pass,reason_code=["+reasonCode+"]");
                             }
                		 }
                			 
                	 }
                	 else {
                		 if (debugSw)
                         {
                         	showLogMessage("I","","check_c_proc pass,reason_code=["+reasonCode+"]");
                         }        		 
                	 }
                 } else {
                	 if (debugSw)
                     {
                     	showLogMessage("I","","check_b_proc pass,reason_code=["+reasonCode+"]");
                     }
                 }
                 
                updateCycAfee(inti);
            }
            
            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
                commitDataBase();
            }
            
        }
        closeCursor();
        return;
    }
    // ************************************************************************
    int selectCycAnulGp() throws Exception
    {
        extendField = "parm.";
        daoTable  = "cyc_anul_gp";
        whereStr  = "order by group_code,card_type desc";

        parmCnt = selectTable();

        showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
        return(0);
    }
    // ************************************************************************
    int checkAProc(int inti) throws Exception
    {
    	if (!getValue("parm.mer_cond",inti).equals("Y")) return(1);   /* A:前十二個月歷史消費紀錄中曾於下列特店刷卡者免年費 */
    	  
    	if ((getValue("parm.a_merchant_sel",inti).equals("0") || getValue("parm.a_merchant_sel",inti).equals("") ) &&
    	      (getValue("parm.a_mcht_group_sel",inti).equals("0") || getValue("parm.a_mcht_group_sel",inti).equals("") ) )
    		   return(1);     /* 沒有指定特店或特店群組 */

    	for ( int intk=0; intk<3; intk++ ) totalAmt[intk]=0;
    	  
    	  extendField = "bill.";
    	  selectSQL = "a.card_no,"
    	            + "a.major_card_no,"
    	            + "a.acct_month,"
    	            + "a.acct_code,"
    				+ "a.acq_member_id,"
    				+ "a.mcht_no ";
    	  daoTable  = "bil_bill a ";
    	  whereStr  = "WHERE a.rsk_type not in ('1','2','3') "
    	            + "and   a.acno_p_seqno  = ? "
    	            + "and   a.acct_month between ? and  ? "
    	            + "and   a.group_code  = ? "
    	            + "and   a.card_type   = ? "
    	            + "and   a.txn_code    not in ('25','27','28','29','06') "
    				+ "and   a.acct_code   in ('BL','CA','IT','AO','ID','OT') "
    				+ "and   a.merge_flag  != 'Y'  "
    	            ;

    	  setString(1,getValue("acno_p_seqno"));
    	  setString(2,getValue("purch_review_month_beg"));
    	  setString(3,getValue("purch_review_month_end"));
    	  setString(4,getValue("group_code"));
    	  setString(5,getValue("card_type"));
    	  
    	  int billCnt = selectTable();

    	  setValue("data_key"  , getValue("group_code")+getValue("card_type"));

    	  for ( int intb=0; intb<billCnt ; intb++)
    	  {
    		  if ((!getValue("parm.mer_bl_flag",inti).equals("Y"))&&
    			      (getValue("bill.acct_code",intb).equals("BL"))) continue;

    		  if ((!getValue("parm.mer_ca_flag",inti).equals("Y"))&&
    				  (getValue("bill.acct_code",intb).equals("CA"))) continue;

    		  if ((!getValue("parm.mer_id_flag",inti).equals("Y"))&&
    				  (getValue("bill.acct_code",intb).equals("ID"))) continue;

    		  if ((!getValue("parm.mer_it_flag",inti).equals("Y"))&&
    				  (getValue("bill.acct_code",intb).equals("IT"))) continue;

    		  if ((!getValue("parm.mer_ao_flag",inti).equals("Y"))&&
    				  (getValue("bill.acct_code",intb).equals("AO"))) continue;

    		  if ((!getValue("parm.mer_ot_flag",inti).equals("Y"))&&
    				  (getValue("bill.acct_code",intb).equals("OT"))) continue;
    	  
    		String acqId = "";
    	    if (getValue("bill.acq_member_id",intb).length()!=0)
    	    	acqId = comm.fillZero(getValue("bill.acq_member_id",intb),8);
    		
    	    if (!getValue("parm.a_merchant_sel",inti).equals("0") && !getValue("parm.a_merchant_sel",inti).equals("") )
    	        if (selectMktBnData(getValue("bill.mcht_no",intb), acqId,
    	              getValue("parm.a_merchant_sel",inti),"1",3)!=0) continue;	
    	    
    	    if (!getValue("parm.a_mcht_group_sel",inti).equals("0") && !getValue("parm.a_mcht_group_sel",inti).equals("") )
    	        if (selectMktMchtgpData(getValue("bill.mcht_no",intb), acqId,
    	              getValue("parm.a_mcht_group_sel",inti),"2")!=0) continue;
    	    
    	    if (getValue("bill.major_card_no",intb).equals(getValue("bill.card_no",intb)) ) {
    	    	totalAmt[0]++;  //正附卡合計
    	    	totalAmt[1]++;  //正卡合計
    	    } else {
    	    	totalAmt[2]++;  //附卡合計
    	    }
    	  }

    	    if (totalAmt[0]>0 && getValue("parm.major_sub",inti).equals("Y")) {
    	    	reasonCode ="A1";
    	    	return(0);
    	    }
    	    
    	    if (totalAmt[1]>0 && getValue("parm.major_flag",inti).equals("Y")) {
    	    	reasonCode = "A2";
    	    	return(0);
    	    }
    	    
    	    if (totalAmt[2]>0 && getValue("parm.sub_flag",inti).equals("Y")) {
    	    	reasonCode = "A3";
    	    	return(0);
    	    }

        return(1);
    }
    // ************************************************************************
    void addMerAmt(int inti,int intk,int intm) throws Exception
    {
        if (getValue("parm.mer_bl_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_bl_cnt",intk);
        if (getValue("parm.mer_ca_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_ca_cnt",intk);
        if (getValue("parm.mer_id_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_id_cnt",intk);
        if (getValue("parm.mer_it_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_it_cnt",intk);
        if (getValue("parm.mer_ao_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_ao_cnt",intk);
        if (getValue("parm.mer_ot_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_ot_cnt",intk);
    }
    // ************************************************************************
    void subMerAmt(int inti,int intk,int intm) throws Exception
    {
        if (getValue("parm.mer_bl_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.a_bl_cnt",intk);
        if (getValue("parm.mer_ca_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.a_ca_cnt",intk);
        if (getValue("parm.mer_id_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.a_id_cnt",intk);
        if (getValue("parm.mer_it_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.a_it_cnt",intk);
        if (getValue("parm.mer_ao_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.a_ao_cnt",intk);
        if (getValue("parm.mer_ot_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.a_ot_cnt",intk);
    }
    // ************************************************************************
    int checkBProc(int inti) throws Exception
    {
    	if (!getValue("parm.cnt_cond",inti).equals("Y")) return(1);   /* B:刷卡次數類別 */

        int monthInt = 0;

        int[] okFlag = new int[3];
        for ( int intk=0; intk<3; intk++ )
        {
            for (int intm=0; intm<13; intm++ ) monthAmt[intm][intk]=0;
            totalAmt[intk]=0;
            okFlag[intk] = 0;
        }

        for ( int intk=0; intk<cnt1; intk++ )
        {
        	//if (!getValue("parm.group_code",inti).equals(getValue("come.group_code",intk)) ) continue;
       	 
       	 	//if (!getValue("parm.card_type",inti).equals(getValue("come.card_type",intk)) ) continue;
       	 	
            monthInt = (int)comm.monthBetween(getValue("come.acct_month",intk),getValue("purch_review_month_end"));

             if ((monthInt<0)||(monthInt>13)) {
                showLogMessage("I","","月份有問題: ["+ getValue("come.acct_month",intk) +"]["
                        + getValue("purch_review_month_end") +"] = ["+monthInt+"]");
                continue;
            }

            if (getValue("parm.cnt_major_sub",inti).equals("Y")) addCntAmt(inti,intk,0,monthInt);

            if (getValue("come.major_card_no",intk).equals(getValue("come.card_no",intk))) //正卡消費
            {
                if ((getValue("parm.cnt_major_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("0"))) addCntAmt(inti,intk,1,monthInt); 
            }
            else   //附卡消費
            {
                if ((getValue("parm.cnt_sub_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("1"))) addCntAmt(inti,intk,2,monthInt);
            }  
         }

        setValue("gexp.p_seqno",getValue("p_seqno"));
        cnt2 = getLoadData("gexp.p_seqno");
        int intk,inth;
        for (intk=0; intk<cnt2; intk++ )
        {
        	//if (!getValue("parm.group_code",inti).equals(getValue("gexp.group_code",intk)) ) continue;
   		 
            //if (!getValue("parm.card_type",inti).equals(getValue("gexp.card_type",intk)) ) continue;
            
            monthInt = (int)comm.monthBetween(getValue("gexp.acct_month",intk),getValue("purch_review_month_end"));
            
            if ((monthInt<0)||(monthInt>13)) {
                showLogMessage("I","","月份有問題: ["+ getValue("gexp.acct_month",intk) +"]["
                        + getValue("purch_review_month_end") +"] = ["+monthInt+"]");
                continue;
            }

            if (getValue("parm.cnt_major_sub",inti).equals("Y")) subCntAmt(inti,intk,0,monthInt);
            if (getValue("gexp.major_card_no",intk).equals(getValue("gexp.card_no",intk)))  //正卡消費
            {
                if ((getValue("parm.cnt_major_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("0"))) subCntAmt(inti,intk,1,monthInt); 
            }
            else  //附卡消費
            {
                if ((getValue("parm.cnt_sub_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("1"))) subCntAmt(inti,intk,2,monthInt);
            }  
         }

        if (getValue("parm.cnt_select",inti).equals("1"))
        {
            if (getValue("parm.cnt_major_sub",inti).equals("Y"))
                for (inth=0;inth<13;inth++)
                    if (monthAmt[inth][0]<getValueInt("parm.month_cnt",inti))
                    {
                        okFlag[0]+=1;
                    }

            if ((getValue("parm.cnt_major_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("0")))
                {
                    for (inth=0;inth<13;inth++) 
                       if (monthAmt[inth][1]>=getValueInt("parm.month_cnt",inti))
                          {
                    	   okFlag[1]+=1;
                          }
                }
            
            if ((getValue("parm.cnt_sub_flag",inti).equals("Y"))&&
                        (getValue("sup_flag").equals("1")))
                {
                    for (inth=0;inth<13;inth++)
                        if (monthAmt[inth][2]>=getValueInt("parm.month_cnt",inti))
                        {
                            okFlag[2]+=1;
                        }
                }
            
            if (okFlag[0]+okFlag[1]+okFlag[2]>=12) {
            	reasonCode = "B0";
            	return(0);
            }
        }
        else
        {
            int parmAccumlateCnt = 0;
            
            //附卡減半
            if (getValue("parm.miner_half_flag",inti).equals("Y") && 
            		getValue("sup_flag").equals("1") ) {
            	parmAccumlateCnt = getValueInt("parm.accumlate_cnt",inti) / 2;
            } else {
            	parmAccumlateCnt = getValueInt("parm.accumlate_cnt",inti);
            }
            
            if (getValue("parm.cnt_major_sub",inti).equals("Y"))
            {
                if (totalAmt[0]>=parmAccumlateCnt) {
                    okFlag[0]=1;
                    reasonCode = "B1";
                }
            }

            if ((getValue("parm.cnt_major_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("0")))
            {
              	 if (totalAmt[1]>=parmAccumlateCnt) 
              	 {
              		okFlag[1]=1;
              		reasonCode = "B2";
              	 }
            }

            if ((getValue("parm.cnt_sub_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("1")))
            {
                    if (totalAmt[2]>=parmAccumlateCnt) 
                    {
                    	okFlag[2]=1;
                    	reasonCode = "B3";
                    }
            }

            if (okFlag[0]+okFlag[1]+okFlag[2]>0) return(0);
        }

        return(1);
    }
    // ************************************************************************
    void addCntAmt(int inti,int intk,int intm,int inth) throws Exception
    {
        if (getValue("parm.cnt_bl_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_bl_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] + getValueInt("come.consume_bl_cnt",intk);
        }
        if (getValue("parm.cnt_ca_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_ca_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] + getValueInt("come.consume_ca_cnt",intk);
        }
        if (getValue("parm.cnt_id_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_id_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] + getValueInt("come.consume_id_cnt",intk);
        }
        if (getValue("parm.cnt_it_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_it_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] + getValueInt("come.consume_it_cnt",intk);
        }
        if (getValue("parm.cnt_ao_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_ao_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] + getValueInt("come.consume_ao_cnt",intk);
        }
        if (getValue("parm.cnt_ot_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] + getValueInt("come.consume_ot_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] + getValueInt("come.consume_ot_cnt",intk);
        }
    }
    // ************************************************************************
    void subCntAmt(int inti,int intk,int intm,int inth) throws Exception
    {
        if (getValue("parm.cnt_bl_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.b_bl_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] - getValueInt("gexp.b_bl_cnt",intk);
        }
        if (getValue("parm.cnt_ca_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.b_ca_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] - getValueInt("gexp.b_ca_cnt",intk);
        }
        if (getValue("parm.cnt_id_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.b_id_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] - getValueInt("gexp.b_id_cnt",intk);
        }
        if (getValue("parm.cnt_it_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.b_it_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] - getValueInt("gexp.b_it_cnt",intk);
        }
        if (getValue("parm.cnt_ao_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.b_ao_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] - getValueInt("gexp.b_ao_cnt",intk);
        }
        if (getValue("parm.cnt_ot_flag",inti).equals("Y"))
        {
            totalAmt[intm] = totalAmt[intm] - getValueInt("gexp.b_ot_cnt",intk);
            monthAmt[inth][intm] = monthAmt[inth][intm] - getValueInt("gexp.b_ot_cnt",intk);
        }
    }
    // ************************************************************************
    int checkCProc(int inti) throws Exception
    {
    	if (!getValue("parm.amt_cond",inti).equals("Y")) return(1);   /* C:累積消費類別 */
    	
        int monthInt = 0;

        int[] okFlag = new int[3];
        for ( int intk=0; intk<3; intk++ )
        {
            totalAmt[intk]=0;
            okFlag[intk] = 0;
        }

        for ( int intk=0; intk<cnt1; intk++ )
        {
        	//if (!getValue("parm.group_code",inti).equals(getValue("come.group_code",intk)) ) continue;
       	 
       	 	//if (!getValue("parm.card_type",inti).equals(getValue("come.card_type",intk)) ) continue;
       	 	
       	    monthInt = (int)comm.monthBetween(getValue("come.acct_month",intk),getValue("purch_review_month_end"));

       	    if ((monthInt<0)||(monthInt>13)) {
               showLogMessage("I","","月份有問題: ["+ getValue("come.acct_month",intk) +"]["
                                                     + getValue("purch_review_month_end") +"] = ["+monthInt+"]");
               continue;
            }

            if (getValue("parm.amt_major_sub",inti).equals("Y")) addAmtAmt(inti,intk,0);

            if (getValue("come.major_card_no",intk).equals(getValue("come.card_no",intk))) //正卡消費
            {
                if ((getValue("parm.amt_major_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("0"))) addAmtAmt(inti,intk,1);  
            }
            else   //附卡消費
            {
                if ((getValue("parm.amt_sub_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("1"))) addAmtAmt(inti,intk,2);
            }
        }

        setValue("gexp.p_seqno",getValue("p_seqno"));
        cnt2 = getLoadData("gexp.p_seqno");
        int intk,inth;
        for (intk=0; intk<cnt2; intk++ )
        {
        	//if (!getValue("parm.group_code",inti).equals(getValue("gexp.group_code",intk)) ) continue;
   		 
            //if (!getValue("parm.card_type",inti).equals(getValue("gexp.card_type",intk)) ) continue;
    	    
            monthInt = (int)comm.monthBetween(getValue("gexp.acct_month",intk),getValue("purch_review_month_end"));
            if ((monthInt<0)||(monthInt>13)) {
               showLogMessage("I","","月份有問題: ["+ getValue("gexp.acct_month",intk) +"]["
                                                     + getValue("purch_review_month_end") +"] = ["+monthInt+"]");
               continue;
            }

            if (getValue("parm.amt_major_sub",inti).equals("Y")) subAmtAmt(inti,intk,0);
            
            if (getValue("gexp.major_card_no",intk).equals(getValue("gexp.card_no",intk)))  //正卡消費
            {
                if ((getValue("parm.amt_major_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("0"))) subAmtAmt(inti,intk,1);
            }
            else  //附卡消費
            {
                if ((getValue("parm.amt_sub_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("1"))) subAmtAmt(inti,intk,2);
            }
        }
        
        int parmAccumlateAmt = 0;
        
        //附卡減半
        if (getValue("parm.miner_half_flag",inti).equals("Y") && 
        		getValue("sup_flag").equals("1") ) {
        	parmAccumlateAmt = getValueInt("parm.accumlate_amt",inti) / 2;
        } else {
        	parmAccumlateAmt = getValueInt("parm.accumlate_amt",inti);
        }

        if (getValue("parm.amt_major_sub",inti).equals("Y")) {
            if (totalAmt[0]>=parmAccumlateAmt) {
                okFlag[0]=1;
                reasonCode = "C1";
            } else if ("Y".equals(getValue("parm.g_cond_flag",inti)) ||   //優質客戶
            		   "Y".equals(getValue("parm.h_cond_flag",inti))) {   //行員
            	if(selectCrdIdno()>0) {
            		if ("Y".equals(getValue("parm.h_cond_flag",inti)) && 
            			"Y".equals(getValue("idno.staff_flag")) &&
            			totalAmt[0]>=getValueInt("parm.h_accumlate_amt",inti)) {
            				okFlag[0]=1;
            				reasonCode = "H1";
            		} else if ("Y".equals(getValue("parm.g_cond_flag",inti)) &&    		
            			"Y".equals(getValue("idno.fee_code_i")) &&
            			totalAmt[0]>=getValueInt("parm.g_accumlate_amt",inti)) {
                			okFlag[0]=1;
                			reasonCode = "G1";
                	}
            		
            	}
            }
        }

        if (getValue("come.major_card_no",intk).equals(getValue("come.card_no",intk)))
        {
            if ((getValue("parm.amt_major_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("0")))
                if (totalAmt[1]>=parmAccumlateAmt) {
                    okFlag[1]=1;
                    reasonCode = "C2";
                }

            if ((getValue("parm.amt_sub_flag",inti).equals("Y"))&&
                    (getValue("sup_flag").equals("1")))
                if (totalAmt[2]>=parmAccumlateAmt) {
                    okFlag[2]=1;
                    reasonCode = "C3";
                }
        }
        if (okFlag[0]+okFlag[1]+okFlag[2]>0) return(0);


        return(1);
    }
    //************************************************************************
    void addAmtAmt(int inti,int intk,int intm) throws Exception
    {
        if (getValue("parm.amt_bl_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueDouble("come.consume_bl_amt",intk);
        if (getValue("parm.amt_ca_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueDouble("come.consume_ca_amt",intk);
        if (getValue("parm.amt_id_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueDouble("come.consume_id_amt",intk);
        if (getValue("parm.amt_it_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueDouble("come.consume_it_amt",intk);
        if (getValue("parm.amt_ao_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueDouble("come.consume_ao_amt",intk);
        if (getValue("parm.amt_ot_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] + getValueDouble("come.consume_ot_amt",intk);
    }
    // ************************************************************************
    void subAmtAmt(int inti,int intk,int intm) throws Exception
    {
        if (getValue("parm.amt_bl_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueDouble("gexp.c_bl_amt",intk);
        if (getValue("parm.amt_ca_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueDouble("gexp.c_ca_amt",intk);
        if (getValue("parm.amt_id_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueDouble("gexp.c_id_amt",intk);
        if (getValue("parm.amt_it_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueDouble("gexp.c_it_amt",intk);
        if (getValue("parm.amt_ao_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueDouble("gexp.c_ao_amt",intk);
        if (getValue("parm.amt_ot_flag",inti).equals("Y"))
            totalAmt[intm] = totalAmt[intm] - getValueDouble("gexp.c_ot_amt",intk);
    }
    // ************************************************************************
    void updateCycAfee(int inti) throws Exception
    {
        dateTime();
        updateSQL = "rcv_annual_fee = ?,"
                + "reason_code    = ?,"
                + "mod_pgm        = ?,"
                + "mod_time       = timestamp_format(?,'yyyymmddhh24miss')";
        daoTable  = "cyc_afee";
        whereStr  = "WHERE  rowid   = ? ";

        if (getValue("sup_flag").equals("0"))
            setInt(1 , getValueInt("parm.card_fee",inti));
        else
            setInt(1 , getValueInt("parm.sup_card_fee",inti));
        setString(2 , reasonCode);
        setString(3 , javaProgram);
        setString(4 , sysDate+sysTime);
        setRowId(5  , getValue("rowid"));

        updateTable();

        updateCnt++;
        return;
    }
    // ************************************************************************
/*
    void loadMktCardConsume() throws Exception
    {
        extendField = "come.";
        selectSQL = "card_no,"
                + "major_card_no,"
                + "acct_month,"
                + "a.p_seqno,"
                + "a.group_code,"
                + "a.card_type,"
                + "sum(consume_bl_cnt) as consume_bl_cnt,"
                + "sum(consume_ca_cnt) as consume_ca_cnt,"
                + "sum(consume_it_cnt) as consume_it_cnt,"
                + "sum(consume_id_cnt) as consume_id_cnt,"
                + "sum(consume_ao_cnt) as consume_ao_cnt,"
                + "sum(consume_ot_cnt) as consume_ot_cnt,"
                + "sum(consume_bl_amt) as consume_bl_amt,"
                + "sum(consume_it_amt) as consume_it_amt,"
                + "sum(consume_id_amt) as consume_id_amt,"
                + "sum(consume_ao_amt) as consume_ao_amt,"
                + "sum(consume_ot_amt) as consume_ot_amt,"
                + "sum(consume_ca_amt) as consume_ca_amt ";
        daoTable  = "mkt_card_consume a,act_acno b";
        whereStr  = "WHERE a.p_seqno        = b.p_seqno "
                + "AND   acct_month between ? and  ? "
                + "AND   exists (select 1 from cyc_anul_gp h"
                + "              where  h.group_code = a.group_code "
                + "              and    h.card_type  = a.card_type "
                + "              fetch first 1 rows only) "
                + "AND   b.stmt_cycle = ? "
                + "group by a.p_seqno,a.group_code,a.card_type,a.card_no,a.major_card_no,acct_month "
                + "order by a.p_seqno "
        ;

        setString(1,comm.nextMonth(hWdayThisAcctMonth,-13));
        setString(2,comm.nextMonth(hWdayThisAcctMonth,-1));
        setString(3,hWdayStmtCycle);

        int  n = loadTable();
        setLoadData("come.card_no");

        showLogMessage("I","","Load mkt_card_consume: ["+n+"]");
    }
 */
    
 // ************************************************************************
    int selectMktCardConsume() throws Exception
    {
        extendField = "come.";
        selectSQL = "card_no,"
                + "major_card_no,"
                + "acct_month,"
                + "a.p_seqno,"
                + "a.group_code,"
                + "a.card_type,"
                + "sum(consume_bl_cnt) as consume_bl_cnt,"
                + "sum(consume_ca_cnt) as consume_ca_cnt,"
                + "sum(consume_it_cnt) as consume_it_cnt,"
                + "sum(consume_id_cnt) as consume_id_cnt,"
                + "sum(consume_ao_cnt) as consume_ao_cnt,"
                + "sum(consume_ot_cnt) as consume_ot_cnt,"
                + "sum(consume_bl_amt) as consume_bl_amt,"
                + "sum(consume_it_amt) as consume_it_amt,"
                + "sum(consume_id_amt) as consume_id_amt,"
                + "sum(consume_ao_amt) as consume_ao_amt,"
                + "sum(consume_ot_amt) as consume_ot_amt,"
                + "sum(consume_ca_amt) as consume_ca_amt ";
        daoTable  = "mkt_card_consume a";
        whereStr  = "WHERE a.p_seqno        = ? "
                + "AND   acct_month between ? and  ? "
                + "group by a.p_seqno,a.group_code,a.card_type,a.card_no,a.major_card_no,acct_month "
                ;

        setString(1,getValue("p_seqno"));
        setString(2,getValue("purch_review_month_beg"));
        setString(3,getValue("purch_review_month_end"));

        int  n = selectTable();
        
        return n;

    }
    
    // ************************************************************************
    int selectCrdIdno() throws Exception
    {
        extendField = "idno.";
        selectSQL = " staff_flag, fee_code_i ";
        daoTable  = "crd_idno";
        whereStr  = "WHERE id_p_seqno        = ? ";
                ;

        setString(1,getValue("id_p_seqno"));

        int  n = selectTable();
        
        return n;

    }
    
    // ************************************************************************
    void loadMktGpcardExp() throws Exception
    {
        extendField = "gexp.";
        selectSQL = "card_no,"
                + "major_card_no,"
                + "acct_month,"
                + "a.p_seqno,"
                + "a.group_code,"
                + "a.card_type,"
                + "sum(a_bl_cnt) as a_bl_cnt,"
                + "sum(a_ca_cnt) as a_ca_cnt,"
                + "sum(a_it_cnt) as a_it_cnt,"
                + "sum(a_id_cnt) as a_id_cnt,"
                + "sum(a_ao_cnt) as a_ao_cnt,"
                + "sum(a_ot_cnt) as a_ot_cnt,"
                + "sum(a_bl_amt) as a_bl_amt,"
                + "sum(a_it_amt) as a_it_amt,"
                + "sum(a_id_amt) as a_id_amt,"
                + "sum(a_ao_amt) as a_ao_amt,"
                + "sum(a_ot_amt) as a_ot_amt,"
                + "sum(a_ca_amt) as a_ca_amt,"
                + "sum(b_bl_cnt) as b_bl_cnt,"
                + "sum(b_ca_cnt) as b_ca_cnt,"
                + "sum(b_it_cnt) as b_it_cnt,"
                + "sum(b_id_cnt) as b_id_cnt,"
                + "sum(b_ao_cnt) as b_ao_cnt,"
                + "sum(b_ot_cnt) as b_ot_cnt,"
                + "sum(b_bl_amt) as b_bl_amt,"
                + "sum(b_it_amt) as b_it_amt,"
                + "sum(b_id_amt) as b_id_amt,"
                + "sum(b_ao_amt) as b_ao_amt,"
                + "sum(b_ot_amt) as b_ot_amt,"
                + "sum(b_ca_amt) as b_ca_amt,"
                + "sum(c_bl_cnt) as c_bl_cnt,"
                + "sum(c_ca_cnt) as c_ca_cnt,"
                + "sum(c_it_cnt) as c_it_cnt,"
                + "sum(c_id_cnt) as c_id_cnt,"
                + "sum(c_ao_cnt) as c_ao_cnt,"
                + "sum(c_ot_cnt) as c_ot_cnt,"
                + "sum(c_bl_amt) as c_bl_amt,"
                + "sum(c_it_amt) as c_it_amt,"
                + "sum(c_id_amt) as c_id_amt,"
                + "sum(c_ao_amt) as c_ao_amt,"
                + "sum(c_ot_amt) as c_ot_amt,"
                + "sum(c_ca_amt) as c_ca_amt";
        daoTable  = "mkt_gpcard_exp a,act_acno b";
        whereStr  = "WHERE a.p_seqno        = b.p_seqno "
                + "AND   acct_month between ? and  ? "
                + "AND   b.stmt_cycle = ? "
                + "group by a.p_seqno,a.group_code,a.card_type,a.card_no,a.major_card_no,acct_month "
                + "order by a.p_seqno ";
 
        setString(1,getValue("purch_review_month_beg"));
        setString(2,getValue("purch_review_month_end"));
        setString(3,hWdayStmtCycle);

        int  n = loadTable();
        setLoadData("gexp.p_seqno");

        showLogMessage("I","","Load mkt_gpcard_exp: ["+n+"]");
    }
    
 // ************************************************************************
    void  loadMktBnData() throws Exception
    {
     extendField = "data.";
     selectSQL = "data_key,"
               + "data_type,"
               + "data_code,"
               + "data_code2";
     daoTable  = "mkt_bn_data b";
     whereStr  = "WHERE TABLE_NAME = 'CYC_ANUL_GP' "
               + "order by data_key,data_type,data_code,data_code2";

     int  n = loadTable();
     setLoadData("data.data_key,data.data_type,data.data_code");

     showLogMessage("I","","Load mkt_bn_data: ["+n+"]");
    }
   // ************************************************************************
    int selectMktBnData(String col1,String sel,String dataType,int dataNum) throws Exception
    {
     return selectMktBnData(col1,"","",sel,dataType,dataNum);
    }
   // ************************************************************************
    int selectMktBnData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
    {
     return selectMktBnData(col1,col2,"",sel,dataType,dataNum);
    }
   // ************************************************************************
    int selectMktBnData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
    {
    if (sel.equals("0")) return(0);  //全部: 不累加排除金額

     setValue("data.data_key" , getValue("data_key"));
     setValue("data.data_type",dataType);

     int cnt3=0;
     if (dataNum==2)
        {
         cnt3 = getLoadData("data.data_key,data.data_type");
        }
     else
        {
         setValue("data.data_code",col1);
         cnt3 = getLoadData("data.data_key,data.data_type,data.data_code");
        }

     int okFlag=0;
     for (int intm=0;intm<cnt3;intm++)
       {
        if (dataNum==2)
           {
            if ((col1.length()!=0)&&
                (getValue("data.data_code",intm).length()!=0)&&
             (!getValue("data.data_code",intm).equals(col1))) continue;

            if ((col2.length()!=0)&&
                (getValue("data.data_code2",intm).length()!=0)&&
             (!getValue("data.data_code2",intm).equals(col2))) continue;

            if ((col3.length()!=0)&&
                (getValue("data.data_code3",intm).length()!=0)&&
             (!getValue("data.data_code3",intm).equals(col3))) continue;
           }
        else
           {
            if (col2.length()!=0)
               {
                if ((getValue("data.data_code2",intm).length()!=0)&&
                    (!getValue("data.data_code2",intm).equals(col2))) continue;
               }
            if (col3.length()!=0)
               {
                if ((getValue("data.data_code3",intm).length()!=0)&&
                    (!getValue("data.data_code3",intm).equals(col3))) continue;
               }
           }

        okFlag=1;
        break;
       }

     if (sel.equals("1"))
        {
         if (okFlag==0) return(1);   //指定: 沒有比對到 
         return(0);
        }
     else
        {
         if (okFlag==0) return(0);   
         return(1);                   //排除: 比對到 
        }
    }
   // ************************************************************************
    void  loadMktMchtgpData() throws Exception
    {
     extendField = "mcht.";
     selectSQL = "b.data_key,"
               + "b.data_type,"
               + "a.data_code,"
               + "a.data_code2";
     daoTable  = "mkt_mchtgp_data a,mkt_bn_data b";
     whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
               + "and   b.TABLE_NAME = 'CYC_ANUL_GP' "
               + "and   a.data_key   = b.data_code "
               + "and   a.data_type  = '1' ";

     int  n = loadTable();
     setLoadData("mcht.data_key,mcht.data_type");
     showLogMessage("I","","Load mkt_mchtgp_data Count: ["+n+"]");
    }
   // ************************************************************************
    int selectMktMchtgpData(String col1,String col2,String sel,String dataType) throws Exception
    {
     if (sel.equals("0")) return(0);  //全部:  

     setValue("mcht.data_key"  , getValue("group_code")+getValue("card_type"));
     setValue("mcht.data_type" , dataType);
     int cnt4 = getLoadData("mcht.data_key,mcht.data_type");

     int okFlag=0;
     for (int intm=0;intm<cnt4;intm++)
       {
        if ((getValue("mcht.data_code",intm).length()!=0)&&
            (!getValue("mcht.data_code",intm).equals(col1))) continue;

        if ((col2.length()!=0)&&
            (getValue("mcht.data_code2",intm).length()!=0)&&
            (!getValue("mcht.data_code2",intm).equals(col2))) continue;

        okFlag=1;
        break;
       }
     if (okFlag==0) return(1);  
     return(0);      //排除:  比對到
    }
    
// ************************************************************************

}  // End of class FetchSample

