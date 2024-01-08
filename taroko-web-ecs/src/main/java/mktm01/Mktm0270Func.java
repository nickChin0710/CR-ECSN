/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/11/15  V1.01.01   Allen Ho      Initial                              *
* 111/11/30  V1.00.02   Yang Bo    sync code from mega                     *
* 113/01/03  V1.00.03   Ryan          增加選擇卡號的檢核條件                                                           *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import busi.ecs.CommRoutine;
import busi.ecs.MktBonus;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0270Func extends FuncEdit
{
 private final String PROGNAME = "紅利積點兌換贈品登錄檔維護作業處理程式111/11/30  V1.00.02";
  String kk1,kk2,kk3;
  String orgControlTabName = "mkt_gift_bpexchg";
  String controlTabName = "mkt_gift_bpexchg_t";
  String cardTable = "";
  String idnoTable = "";
  String acnoTable = "";
  String vdFlag = "";
  String cardNote = "";
 public Mktm0270Func(TarokoCommon wr)
 {
  wp = wr;
  this.conn = wp.getConn();
 }
// ************************************************************************
 @Override
 public int querySelect()
 {
  // TODO Auto-generated method
  return 0;
 }
// ************************************************************************
 @Override
 public int dataSelect()
 {
  // TODO Auto-generated method stub
  String procTabName="";
  procTabName = wp.itemStr("control_tab_name");
  if (procTabName.length()==0) return(1);
  strSql= " select "
          + " apr_flag, "
          + " acct_type, "
          + " id_p_seqno, "
          + " p_seqno, "
          + " chi_name, "
          + " cellar_phone, "
          + " home_area_code1, "
          + " home_tel_no1, "
          + " home_tel_ext1, "
          + " office_area_code1, "
          + " office_tel_no1, "
          + " office_tel_ext1, "
          + " bill_sending_zip, "
          + " bill_sending_addr1, "
          + " bill_sending_addr2, "
          + " bill_sending_addr3, "
          + " bill_sending_addr4, "
          + " bill_sending_addr5, "
          + " gift_type, "
          + " from_mark, "
          + " exchg_cnt, "
          + " cash_value, "
          + " bonus_type, "
          + " exchg_pt, "
          + " exchg_amt, "
          + " total_pt, "
          + " end_tran_bp, "
          + " end_tran_bp01, "
          + " end_tran_bp90, "
          + " deduct_bp01, "
          + " deduct_bp90, "
          + " exchg_mark, "
          + " tran_date, "
          + " proc_flag, "
          + " status_flag, "
          + " deduct_flag, "
          + " deduct_date, "
          + " exg_apr_user, "
          + " exg_apr_date, "
          + " exg_gl_date, "
          + " ecoupon_bno, "
          + " ecoupon_date_s, "
          + " ecoupon_date_e, "
          + " ecoupon_date, "
          + " unpay_cnt, "
          + " ecoupon_ret_date, "
          + " ecoupon_gl_date, "
          + " sms_flag, "
          + " sms_date, "
          + " sms_resend_desc, "
          + " air_type, "
          + " cal_dfpno, "
          + " air_id_no, "
          + " air_birthday, "
          + " passport_name, "
          + " passport_surname, "
          + " send_date, "
          + " send_time, "
          + " recv_date, "
          + " recv_time, "
          + " rjct_flag, "
          + " rjct_code, "
          + " rjct_msg, "
          + " rjct_proc_code, "
          + " rjct_proc_remark, "
          + " air_apr_user, "
          + " air_apr_date, "
          + " out_date, "
          + " out_batchno, "
          + " out_mark, "
          + " register_no, "
          + " out_apr_user, "
          + " out_apr_date, "
          + " pay_date, "
          + " pay_gl_date, "
          + " pay_apr_user, "
          + " pay_apr_date, "
          + " return_date, "
          + " return_reason, "
          + " return_mark, "
          + " ret_apr_user, "
          + " ret_apr_date, "
          + " ret_gl_date, "
          + " fund_code, "
          + " from_mark, "
          + " cal_mile, "
          + " acct_type, "
          + " auth_code, "
          + " tran_time, "
          + " sms_flag, "
          + " sms_date, "
          + " process_code, "
          + " appr_err_code, "
          + " ret_tran_seqno, "
          + " ret_fund_tran_seqno, "
          + " exchg_seqno, "
          + " ecoupon_bno, "
          + " ecoupon_date_s, "
          + " ecoupon_date_e, "
          + " ecoupon_date, "
          + " ecoupon_gl_date, "
          + " vendor_no, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
          + " from " + procTabName 
          + " where rowid = ? ";

  Object[] param =new Object[]
       {
        wp.itemRowId("rowid")
       };

  sqlSelect(strSql, param);
   if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck()
 {
  if (!this.ibDelete)
     {
      if (wp.colStr("storetype").equals("Y"))
        {
         errmsg("[查原資料]模式中, 請按[還原異動] 才可儲存 !");
         return;
        }
     }
  if (this.ibAdd)
     {
      kk1 = wp.itemStr("card_no");
      kk2 = wp.itemStr("gift_no");
      kk3 = wp.itemStr("tran_seqno");
     }
  else
     {
      kk1 = wp.itemStr("card_no");
      kk2 = wp.itemStr("gift_no");
      kk3 = wp.itemStr("tran_seqno");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where card_no = ? "
             +"and   gift_no = ? "
             +"and   tran_seqno = ? "
             ;
      Object[] param = new Object[] {kk1,kk2,kk3};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[卡　　號][商品代碼][交易序號] 不可重複("+ orgControlTabName +"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where card_no = ? "
             + " and   gift_no = ? "
             + " and   tran_seqno = ? "
             ;
      Object[] param = new Object[] {kk1,kk2,kk3};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[卡　　號][商品代碼][交易序號] 不可重複("+ controlTabName +") ,請重新輸入!");
          return;
         }
     }


// wp.dddSql_log = false;

   if (wp.itemStr("aud_type").equals("A"))
      {
       if (wp.itemStr("apr_flag").equals("Y"))
          {
           wp.colSet("apr_flag" , "N");
           wp.itemSet("apr_flag" , "N");
          }
      }
   else
      {
       if (wp.itemStr("apr_flag").equals("Y"))
          {
           wp.colSet("apr_flag" , "N");
           wp.itemSet("apr_flag" , "N");
          }
      }

   if ((this.ibDelete)||
       (wp.itemStr("aud_type").equals("D"))) return;

 if ((!wp.itemStr("control_tab_name").equals(orgControlTabName))&&
      (wp.itemStr("aud_type").equals("A")))
    {
      if (wp.itemStr("p_seqno").length()==0)
         {
          errmsg("資格不符 , 兌換作業無法進行!");
          return;
         }

      strSql = "select "
             + "       disable_flag, "
             + "       decode(gift_type,'3',0,supply_count-use_count-web_sumcnt) as net_count,"
             + "       decode(gift_type,'3',max_limit_count-use_limit_count-web_sumcnt,0) as net_limit_cnt,"
             + "       gift_type, "
             + "       air_type, "
             + "       vendor_no, "
             + "       mod_seqno "
             + " from mkt_gift "
             + " where gift_no =  ? "
             ;
      Object[] param = new Object[] {wp.itemStr("gift_no")};
      sqlSelect(strSql,param);

      if (sqlRowNum <= 0)
         {
          errmsg("商品資料讀取不到!");
          return;
         }
      if (colStr("disable_flag").equals("Y"))
         {
          errmsg("該商品已停用,不可兌換!");
          return;
         }
      if ((!colStr("gift_type").equals("2"))&&
          (colStr("air_type").length()==0))
         if (checkGiftCount() != 0) return;
    }
  if (this.ibAdd)
     {
      if (wp.itemStr("tran_seqno").length()==0)
         {
          CommRoutine comr = new CommRoutine();
          comr.setConn(wp);
          wp.itemSet("tran_seqno" , comr.getSeqno("MKT_MODSEQ"));
          kk3 = wp.itemStr("tran_seqno");
          dateTime();
          colSet("tran_date",wp.sysDate);
          wp.itemSet("tran_time",wp.sysTime);
         }

      if ((wp.itemStr("deduct_date").length()!=0)&&
          (wp.itemStr("aud_type").equals("D"))&&
          (wp.itemStr("control_tab_name").equals(orgControlTabName)))
         {
          errmsg("已覆核資料, 只可修改不可刪除 !");
          return;
         }

      if (!wp.itemStr("control_tab_name").equals(orgControlTabName))
         wp.itemSet("from_mark" , "1");
      if (wp.itemStr("deduct_flag").length()==0)
         wp.itemSet("deduct_flag" , "N");
      if (wp.itemStr("proc_flag").length()==0)
         wp.itemSet("proc_flag" , "N");
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemStr("gift_type").equals("3"))
         {
          if (!wp.itemStr("sms_flag").equals(colStr("sms_flag")))
             {
              errmsg("非電子商品, 不可變更簡訊旗標!");
              return;
             }
         }

      if (wp.itemStr("deduct_date").length()==0)
         {
          if ((wp.itemStr("return_date").length()!=0)||
              (wp.itemStr("return_reason").length()!=0))
             {
              errmsg("新兌換未覆核資料, 若要取消請直接刪除 !");
              wp.itemSet("return_date","");
              wp.itemSet("return_reason","");
              return;
             }
          if ((wp.itemStr("air_type").length()!=0)&&
              (wp.itemStr("rjct_proc_code").equals("C")))
             {
              errmsg("新兌換未覆核資料, 若要取消請直接刪除 !");
              wp.itemSet("rjct_proc_code","");
              return;
             }
         }
     }


  if ((this.ibUpdate))
     {
	  if("Y".equals(colStr("apr_flag"))) {
		  if (wp.itemNum("exchg_cnt")!=Integer.valueOf(colStr("exchg_cnt")))
          {
           errmsg("已覆核資料, 不可變更兌換數量!");
           return;
          }
	  }
	  
      if (!wp.itemStr("deduct_flag").equals("Y"))
         {
          if (wp.itemStr("out_date").length()!=0)
             {
              errmsg("兌換扣點未完成, 不可出貨!");
              return;
             }
         }
      else
         {
          if (wp.itemNum("exchg_cnt")!=Integer.valueOf(colStr("exchg_cnt")))
             {
              errmsg("兌換扣點已完成, 不可變更兌換數量!");
              return;
             }
         }

      if (wp.itemStr("air_type").length()!=0)
      if (wp.itemStr("control_tab_name").equals(orgControlTabName))
         {
          if ((wp.itemStr("send_date").length()!=0)&&
              (wp.itemStr("recv_date").length()==0))
             {
              if (!wp.itemStr("cal_dfpno").equals(colStr("cal_dfpno")))
                 {
                  errmsg("已送檔未回覆案件, 會員編號不可修改!");
                  return;
                 }
             }
         }
      if(wp.itemStr("gift_type").equals("2")) {
    	  if ((wp.itemStr("return_date").length()>0)&& (wp.itemStr("ret_apr_date").length()==0)) {
    		  String sqlCmd = "select a.WF_VALUE , b.gift_no , b.gift_type , b.fund_code ";
    		  sqlCmd += "from PTR_SYS_PARM a , mkt_gift b ";
    		  sqlCmd += "where  a.WF_PARM = 'IDTAB'  and a.WF_KEY = 'REDBILGOOD' ";
    		  sqlCmd += "and a.WF_VALUE = b.gift_no ";
    		  sqlCmd += "and b.gift_type ='2' ";
    		  sqlCmd += "and b.gift_no = ? ";
    		  sqlCmd += "and b.fund_code = ? ";
    		  Object[] param = new Object[] {wp.itemStr("gift_no"),wp.itemStr("fund_code")};
    		  sqlSelect(sqlCmd,param);
    		  if(sqlRowNum<=0) {
    		       errmsg("本項刷卡金一經兌換覆核後,無法進行退貨! 要退點與人工追回回存款項,請由紅利點數調整作業退還點數與相關作業!!");
                   return;
    		  }
    	  }
      }
      }


  if ((this.ibAdd)||(this.ibUpdate))
     {
	  int nowPt01 = 0;
	  int nowPt90 = 0;
      MktBonus comc = new MktBonus();
      comc.setConn(wp);
      
      if (wp.itemStr("gift_type").equals("3"))
         {
          if ((wp.itemStr("out_date").length()>0)&&
              (!wp.itemStr("out_date").equals(colStr("out_date"))))
             {
              errmsg("電子商品不可輸入出貨日期!");
              return;
             }
          if (wp.itemStr("cellar_phone").length()==0)
             {
              errmsg("電子商品, 手機號碼必須輸入!");
              return;
             }
          else
             {
              if (!wp.itemStr("cellar_phone").matches("[0-9]+"))
                 {
                  errmsg("電子商品, 手機號碼必須均為數值!");
                  return;
                 }
             }
          if ((wp.itemStr("sms_date").length()!=0)&&
              (wp.itemStr("sms_flag").equals("N")))
             {
              errmsg("電子商品簡訊旗標, 已發送簡訊不可更改未發送!");
              return;
             }
          if ((wp.colStr("sms_flag").equals("N"))&&
              (wp.itemStr("sms_flag").equals("R")))
             {
              errmsg("電子商品簡訊旗標, 尚未發送簡訊不可更改重新發送!");
              return;
             }
         }

      if ((wp.itemStr("sms_flag").equals("R"))&&
          (wp.itemStr("sms_resend_desc").length()==0))
         {
          errmsg("電子商品簡訊重送時, 簡訊重送備註必須輸入!");
          return;
         }

      if (wp.itemStr("ret_apr_date").length()!=0)
         {
          errmsg("已退貨覆核, 不可再更改資料!");
          return;
         }
      if (wp.itemStr("deduct_date").length()==0)
         {
          nowPt01 = comc.getEndTranBp01(strMid(wp.itemStr("id_no"),0,10));
          nowPt90 = comc.getEndTranBp90(strMid(wp.itemStr("id_no"),0,10));
          wp.itemSet("end_tran_bp",String.format("%d", nowPt01 + nowPt90));
          wp.itemSet("end_tran_bp01",String.format("%d", nowPt01));
          wp.itemSet("end_tran_bp90",String.format("%d", nowPt90));
          colSet("end_tran_bp",String.format("%d", nowPt01 + nowPt90));
          colSet("end_tran_bp01",String.format("%d", nowPt01));
          colSet("end_tran_bp90",String.format("%d", nowPt90));
          wp.colSet("end_tran_bp",String.format("%d", nowPt01 + nowPt90));
          wp.colSet("end_tran_bp01",String.format("%d", nowPt01));
          wp.colSet("end_tran_bp90",String.format("%d", nowPt90));
         }
      else
         { 
    	  wp.itemSet("end_tran_bp",wp.itemStr("end_tran_bp").replace(",",""));
    	  wp.itemSet("end_tran_bp01",wp.itemStr("end_tran_bp01"));
    	  wp.itemSet("end_tran_bp90",wp.itemStr("end_tran_bp90"));
          colSet("end_tran_bp",wp.itemStr("end_tran_bp").replace(",",""));
          colSet("end_tran_bp01",wp.itemStr("end_tran_bp01"));
          colSet("end_tran_bp90",wp.itemStr("end_tran_bp90"));
          wp.colSet("end_tran_bp",wp.itemStr("end_tran_bp").replace(",",""));
          wp.colSet("end_tran_bp01",wp.itemStr("end_tran_bp01"));
          wp.colSet("end_tran_bp90",wp.itemStr("end_tran_bp90"));

          if (wp.itemStr("out_date").length()>0) 
          if (wp.itemStr("out_apr_date").length()==0) 
          if (wp.itemStr("out_date").compareTo(wp.itemStr("deduct_date"))<0) 
             {
              errmsg("出貨日期不可小於兌換覆核日!");
              return;
             }
          if (wp.itemStr("pay_date").length()>0) 
             {
              if (wp.itemStr("pay_apr_date").length()==0) 
                 {
                  if (wp.itemStr("pay_date").compareTo(wp.itemStr("out_apr_date"))<0) 
                     {
                      errmsg("請款日期不可小於出貨覆核日!");
                      return;
                     }
                  if (wp.itemStr("gift_type").equals("3")) 
                     {
                      errmsg("電子商品不可人工輸入請款日期!");
                      return;
                     }
                 }
              if (wp.itemStr("out_apr_date").length()==0) 
                 {
                  errmsg("未出貨請款不可請款!");
                  return;
                 }
             }
          if ((wp.itemStr("return_date").length()>0)&& 
              (wp.itemStr("return_apr_date").length()==0))
             {
              if (wp.itemStr("gift_type").equals("3")) 
                 {
                  if (wp.itemStr("sms_flag").equals("R"))
                     {
                      errmsg("電子商品退貨不可設定重發簡訊!");
                      return;
                     }
                  if (wp.itemStr("ecoupon_ret_date").length()!=0)
                     {
                      errmsg("電子商品已屆期回沖,不可退貨!");
                      return;
                     }
/*
                  if (wp.itemStr("pay_date").length()!=0)
                     {
                      errmsg("電子商品請款,不可退貨!");
                      return;
                     }
*/
                 }
              if (wp.itemStr("return_date").compareTo(wp.itemStr("out_apr_date"))<0) 
                 {
                  errmsg("退貨日期不可小於出貨覆核日!");
                  return;
                 }
              if ((wp.itemStr("air_type").length()!=0)&&
                  (wp.itemStr("pay_apr_date").length()!=0))
                 {
                  errmsg("航空里程已請款覆核, 退貨必須由人工處理!");
                  return;
                 }
             }
         }

      if ((wp.itemStr("out_apr_date").length()>0)&& 
          (wp.itemStr("out_date").length()>0)&&
          (!wp.itemStr("out_date").equals(colStr("out_date"))))
         {
          errmsg("已退貨覆核, 不可更改退貨日期!");
          return;
         }
      if (wp.itemNum("exchg_cnt")==0)
         {
          errmsg("兌換數量, 不可為 0!");
          return;
         }

      if (wp.itemStr("air_type").length()==0)
         {
          if (wp.itemStr("rjct_proc_code").length()!=0)
             {
              errmsg("非航空哩程, 不可退件處理!");
              return;
             }
          wp.itemSet("passport_name","");
          wp.itemSet("passport_surname","");
          wp.itemSet("air_id_no","");
          wp.itemSet("air_birthday","");
          wp.itemSet("cal_dfpno","");
         }
      else
         {
          if (wp.itemStr("out_apr_date").length()!=0)
             {
              if (wp.itemStr("rjct_proc_code").length()!=0)
                 {
                  errmsg("已接受(出貨)案件, 不可進行任何退件處理!");
                  return;
                 }
             }
          else
             {
              if (wp.itemStr("rjct_proc_code").length()!=0)
                 {
                  if ((wp.itemStr("send_date").length()!=0)&&
                      (wp.itemStr("recv_date").length()==0))
                     {
                      errmsg("已送檔未回覆案件, 不可結案取消或重送!");
                      return;
                     }
                  if (wp.itemStr("rjct_proc_remark").length()==0)
                     {
                      errmsg("退件處理說明不可空白!");
                      return;
                     }
                  if ((wp.itemStr("rjct_proc_code").equals("2"))&&
                      (wp.itemStr("rjct_flag").equals("W")))
                     {
                      errmsg("航空公司等待未確認,不可重送");
                      return;
                     }
                  }
              }

          if (wp.itemStr("air_type").equals("03"))
             {
              if (wp.itemStr("passport_surname").length()==0)
                 {
                  errmsg("航空哩程, 國泰航空必須輸入護照姓!");
                  return;
                 }
              if (checkCalDfpno3(wp.itemStr("cal_dfpno"))!=0)
                 {
                  errmsg("航空會員編號編碼錯誤 !");
                  return;
                 }
             }
          if ((wp.itemStr("out_apr_date").length()==0)&&
              (wp.itemStr("out_date").length()!=0))
             {
              errmsg("航空哩程, 出貨日期由系統處理!");
              return;
             }
          if ((wp.itemStr("passport_name").length()==0)||
              (wp.itemStr("air_id_no").length()==0)||
              (wp.itemStr("air_birthday").length()==0)||
              (wp.itemStr("cal_dfpno").length()==0))
             {
              errmsg("航空哩程, 相關資料均需輸入!");
              return;
             }
         }

      if ((wp.itemStr("return_reason").length()==0)&&
          (wp.itemStr("return_date").length()!=0))
         {
          errmsg("退貨原因必須輸入!");
          return;
         }
      if ((wp.itemStr("out_apr_date").length()!=0)&&
          (wp.itemStr("out_date").length()==0))
         {
          errmsg("已出貨覆核, 不可更改出貨日期!");
          return;
         }
      if ((wp.itemStr("pay_apr_date").length()!=0)&&
          (wp.itemStr("pay_date").length()==0))
         {
          errmsg("已請款覆核, 不可更改請款日期!");
          return;
         }
      if ((wp.itemStr("ret_apr_date").length()!=0)&&
          (wp.itemStr("out_apr_date").length()==0)&&
          (wp.itemStr("out_date").length()!=0))
         {
          errmsg("已退貨覆核, 不可出貨!");
          return;
         }

/*
      wp.ddd("============================================");
      wp.ddd("exg_apr_date    = ["+wp.itemStr("exg_apr_date")+"]");
      wp.ddd("rjct_proc_code  = ["+wp.itemStr("rjct_proc_code")+"]");
      wp.ddd("out_apr_date    = ["+wp.itemStr("out_apr_date")+"]");
      wp.ddd("out_date        = ["+wp.itemStr("out_date")+"]");
      wp.ddd("ret_apr_date    = ["+wp.itemStr("ret_apr_date")+"]");
      wp.ddd("return_date     = ["+wp.itemStr("return_date")+"]");
*/
      if ((wp.itemStr("out_apr_date").length()==0)&&
          (wp.itemStr("out_date").length()!=0))
         {
          if ((wp.itemStr("out_batchno").length()==0)||
              (wp.itemStr("register_no").length()==0))
             {
              errmsg("出貨單號及掛號號碼必須輸入!");
              return;
             }
          if (wp.itemStr("pay_date").length()!=0)
             {
              errmsg("非航空里程之請款日期由系統設定, 不可人工輸入!");
              return;
             }
          if (wp.itemStr("return_date").length()!=0)
             {
              errmsg("不可同時出貨和退貨!");
              return;
             }
          wp.itemSet("return_reason"   ,"");
          wp.itemSet("return_mark"     ,"");
         }


      if (wp.itemStr("deduct_date").length()==0)
         {
          strSql = "select exchange_bp as exchg_pt, "
                 + "       exchange_amt as exchg_amt "
                 + "from "
                 + cardTable
                 + " a,mkt_gift_exchgdata b" 
                 + " where "
                 + cardNote
                 + " = b.card_note "
                 +"and   b.group_code = a.group_code  "
                 +"and   b.group_code != ''  "
                 +"and   a.card_no = ? "
                 +"and   b.gift_no = ? "
                 ;
          Object[] param = new Object[] {wp.itemStr("card_no"),wp.itemStr("gift_no")};
          sqlSelect(strSql,param);
      
          if (sqlRowNum <= 0)
             {
              strSql = "select exchange_bp as exchg_pt, "
                     + "       exchange_amt as exchg_amt "
                     + "from "
                     + cardTable
                     + " a,mkt_gift_exchgdata b" 
                     + " where "
                     + cardNote
                     + " = b.card_note "
                     +"and   b.group_code = ''  "
                     +"and   a.card_no = ? "
                     +"and   b.gift_no = ? "
                     ;
              param = new Object[] {wp.itemStr("card_no"),wp.itemStr("gift_no")};
              sqlSelect(strSql,param);

             }
          
          if (sqlRowNum <= 0)
          {
        	strSql = "select exchange_bp as exchg_pt, "
    	           + "       exchange_amt as exchg_amt "
    	           + " from "
    	           + cardTable
    	           + " a,mkt_gift_exchgdata b "
    	   		   + " where "
    	   		   + cardNote
    	   		   + " = b.card_note "
    	           + " and   b.group_code = ''  "
    	           + " and   "
    	           + cardNote
    	           + "  = ''     " ;   
           sqlSelect(strSql);
          }
          
          if (sqlRowNum <= 0)
          {
           errmsg("贈品代號["+kk2+"]未在4紅利贈品資料檔維定義兌換點數 !");
           return;
          }

          wp.itemSet("exchg_pt",colStr("exchg_pt"));
          wp.colSet("exchg_pt",colStr("exchg_pt"));
          wp.itemSet("exchg_amt",colStr("exchg_amt"));
          wp.colSet("exchg_amt",colStr("exchg_amt"));
          wp.colSet("total_pt",String.format("%.0f",colNum("exchg_pt")*wp.itemNum("exchg_cnt")));
          colSet("total_pt",String.format("%.0f",colNum("exchg_pt")*wp.itemNum("exchg_cnt")));

          strSql = "select gift_type,fund_code,cal_mile "
                 + "from mkt_gift "
                 + " where gift_no = ? "
                 ;
          param = new Object[] {kk2};
          sqlSelect(strSql,param);

          if (sqlRowNum <= 0)
             {
              errmsg("贈品代號["+kk2+"]未在贈品資料檔 !");
              return;
             }
          wp.itemSet("gift_type",colStr("gift_type"));
          wp.itemSet("fund_code",colStr("fund_code"));

/*
      wp.ddd("STEP 00001 total_pt =["+wp.itemStr("total_pt")+"]");
      wp.ddd("STEP 00002 total_pt =["+wp.colStr("total_pt")+"]");
      wp.ddd("STEP 00003 total_pt =["+colStr("total_pt")+"]");
      wp.ddd("STEP 00004 end_tran_pt =["+wp.colStr("end_tran_bp")+"]");
*/

          if (wp.colStr("total_pt").length()==0)  wp.itemSet("total_pt","0");
          if (wp.colStr("end_tran_bp").length()==0) 
             {
              errmsg("請先查詢[兌換時點數餘額] !");
              return;
             }
          if (wp.colNum("end_tran_bp")==0) 
             {
              errmsg("無紅利點數餘額可兌換 !");
              return;
             }
      wp.log("STEP 00000 end_tran_pt =["+wp.colNum("end_tran_bp")+"]");

          strSql = "select sum(total_pt) as in_total_pt "
                 + "from mkt_gift_bpexchg_t "
                 + " where  id_p_Seqno = ? "
                 + " and    tran_Seqno != ? "
                 ;

          param = new Object[] {wp.itemStr("id_p_seqno"),wp.itemStr("tran_seqno")};
          sqlSelect(strSql,param);
          double inTotalPt = 0;
          if (sqlRowNum > 0) inTotalPt = colNum("in_total_pt"); 

          String dspStr ="";
          if (inTotalPt>0)
             dspStr = "(兌換中 "+ String.format("%,.0f",inTotalPt)+" 點) ";
          
          int deductBp01 = 0;
          int deductBp90 = 0;
          double needPt = wp.itemNum("exchg_cnt")*colInt("exchg_pt");
          int[] deductBp = comc.countDeductBp(nowPt01,nowPt90,(int)needPt);
          if(deductBp != null) {
       	   	deductBp01 = deductBp[0];
       	   	deductBp90 = deductBp[1];
          }
          wp.itemSet("deduct_bp01",String.format("%d", deductBp01));
          wp.itemSet("deduct_bp90",String.format("%d", deductBp90));
          colSet("deduct_bp01",String.format("%d", deductBp01));
          colSet("deduct_bp90",String.format("%d", deductBp90));
          wp.colSet("deduct_bp01",String.format("%d", deductBp01));
          wp.colSet("deduct_bp90",String.format("%d", deductBp90));
          
          if(deductBp01 < 0 ) {
       	   if("1".equals(vdFlag)) {
       		errmsg("請選信用卡號");
       		return;
       	   }
          }
          
          if(deductBp90 < 0 && deductBp01 == 0) {
       	   if("0".equals(vdFlag)) {
       		errmsg("回存帳戶金額請選Visa Debit卡號");
       		return;
       	   }
          }
/*
      wp.ddd("STEP 00001 in_total_pt =["+in_total_pt+"]");
      wp.ddd("STEP 00002 total_pt =["+wp.colNum("total_pt")+"]");
      wp.ddd("STEP 00003 end_tran_pt =["+wp.colNum("end_tran_bp")+"]");
*/

          if (wp.colNum("end_tran_bp")<wp.colNum("total_pt")+inTotalPt) 
             {
              errmsg(dspStr
                     + "紅利點數餘額("
                     + String.format("%,.0f",(wp.colNum("end_tran_bp")-inTotalPt))
                     + ")不足, 不可兌換("
                     + String.format("%,.0f",wp.itemNum("total_pt"))
                     + ") !");
              return;
             }
         }
      else
         {
          wp.colSet("total_pt",wp.itemStr("total_pt").replace(",",""));
          wp.colSet("exchg_pt",wp.itemStr("exchg_pt").replace(",",""));
          wp.colSet("cash_value",wp.itemStr("cash_value").replace(",",""));
          colSet("total_pt",wp.itemStr("total_pt").replace(",",""));
          colSet("exchg_pt",wp.itemStr("exchg_pt").replace(",",""));
          colSet("cash_value",wp.itemStr("cash_value").replace(",",""));
         }
      
     }

  if (wp.itemStr("status_flag").length()==0)
     wp.itemSet("status_flag","0");

  colSet("proc_flag","");
  
  if ((wp.itemStr("out_apr_date").length()==0)&&
      (wp.itemStr("out_date").length()!=0))
     colSet("proc_flag","B");
  else if ((wp.itemStr("return_date").length()!=0)&&
      (wp.itemStr("ret_apr_date").length()==0))
     colSet("proc_flag","C");
  else if ((wp.itemStr("pay_date").length()!=0)&&
      (wp.itemStr("pay_apr_date").length()==0))
     {
      if (wp.itemStr("air_type").length()!=0)
         colSet("proc_flag","E");
      else
         colSet("proc_flag","G");
     }
  else if ((colStr("proc_flag").length()==0)||
           (colStr("proc_flag").equals("N")))
     {       
      if (wp.itemStr("deduct_date").length()==0)
         {
          colSet("proc_flag","N");
         }
      else 
         {
          colSet("proc_flag","D");
          if (wp.itemStr("air_apr_date").length()!=0) 
             {
              if (wp.itemStr("rjct_proc_code").length()!=0)
                 colSet("proc_flag","A");
             }
          else
             {
              if (wp.itemStr("sms_flag").equals("R")) 
                 colSet("proc_flag","F");
             }
          
         }
     }

  wp.itemSet("proc_flag"  ,colStr("proc_flag"));

  if (wp.itemStr("deduct_date").length()==0)
     {
      if (wp.itemStr("air_type").length()==0)
         {
          wp.itemSet("cal_dfpno"       ,"");
          wp.itemSet("air_id_no"       ,"");
          wp.itemSet("air_birthday"    ,"");
          wp.itemSet("passport_name"   ,"");
          wp.itemSet("passport_surname","");
         }
      wp.itemSet("rjct_proc_code"  ,"");
      wp.itemSet("rjct_proc_remark","");
      wp.itemSet("out_date"        ,"");
      wp.itemSet("out_batchno"     ,"");
      wp.itemSet("out_mark"        ,"");
      wp.itemSet("register_no"     ,"");
      wp.itemSet("pay_date"        ,"");
      wp.itemSet("return_mark"     ,"");
     }
  wp.itemSet("end_tran_bp" , wp.colStr("end_tran_bp"));
  wp.itemSet("end_tran_bp01" , wp.colStr("end_tran_bp01"));
  wp.itemSet("end_tran_bp90" , wp.colStr("end_tran_bp90"));
  wp.itemSet("deduct_bp01" , wp.colStr("deduct_bp01"));
  wp.itemSet("deduct_bp90" , wp.colStr("deduct_bp90"));
  wp.itemSet("total_pt"    , wp.colStr("total_pt"));
  wp.itemSet("exchg_pt"    , wp.colStr("exchg_pt"));
  strSql = "select cash_value "
         + "from mkt_gift "
         + " where gift_no = ? "
         ;
   Object[] param = new Object[] {kk2};
   sqlSelect(strSql,param);

   if (sqlRowNum <= 0)
      {
       errmsg("贈品代號["+kk2+"]未在贈品資料檔 !");
       return;
      }
  wp.itemSet("cash_value"  , colStr("cash_value"));

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
      return;
     }


  if (this.isAdd()) return;

  if (this.ibDelete)
     {
      wp.colSet("storetype" , "N");
     }
 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  rc = getVdFlag(wp.itemStr("card_no"));
  if (rc!=1) return rc;
  actionInit("A");
  dataCheck();
  if (rc!=1) return rc;


  strSql= " insert into  " + controlTabName + " ("
          + " card_no, "
          + " apr_flag, "
          + " aud_type, "
          + " gift_no, "
          + " tran_seqno, "
          + " id_p_seqno, "
          + " p_seqno, "
          + " chi_name, "
          + " cellar_phone, "
          + " home_area_code1, "
          + " home_tel_no1, "
          + " home_tel_ext1, "
          + " office_area_code1, "
          + " office_tel_no1, "
          + " office_tel_ext1, "
          + " bill_sending_zip, "
          + " bill_sending_addr1, "
          + " bill_sending_addr2, "
          + " bill_sending_addr3, "
          + " bill_sending_addr4, "
          + " bill_sending_addr5, "
          + " gift_type, "
          + " exchg_cnt, "
          + " cash_value, "
          + " bonus_type, "
          + " exchg_pt, "
          + " exchg_amt, "
          + " total_pt, "
          + " end_tran_bp, "
          + " end_tran_bp01, "
          + " end_tran_bp90, "
          + " deduct_bp01, "
          + " deduct_bp90, "
          + " exchg_mark, "
          + " tran_date, "
          + " proc_flag, "
          + " status_flag, "
          + " deduct_flag, "
          + " deduct_date, "
          + " exg_apr_user, "
          + " exg_apr_date, "
          + " exg_gl_date, "
          + " unpay_cnt, "
          + " ecoupon_ret_date, "
          + " sms_resend_desc, "
          + " air_type, "
          + " cal_dfpno, "
          + " air_id_no, "
          + " air_birthday, "
          + " passport_name, "
          + " passport_surname, "
          + " send_date, "
          + " send_time, "
          + " recv_date, "
          + " recv_time, "
          + " rjct_flag, "
          + " rjct_code, "
          + " rjct_msg, "
          + " rjct_proc_code, "
          + " rjct_proc_remark, "
          + " air_apr_user, "
          + " air_apr_date, "
          + " out_date, "
          + " out_batchno, "
          + " out_mark, "
          + " register_no, "
          + " out_apr_user, "
          + " out_apr_date, "
          + " pay_date, "
          + " pay_gl_date, "
          + " pay_apr_user, "
          + " pay_apr_date, "
          + " return_date, "
          + " return_reason, "
          + " return_mark, "
          + " ret_apr_user, "
          + " ret_apr_date, "
          + " ret_gl_date, "
          + " fund_code, "
          + " from_mark, "
          + " cal_mile, "
          + " acct_type, "
          + " auth_code, "
          + " tran_time, "
          + " sms_flag, "
          + " sms_date, "
          + " process_code, "
          + " appr_err_code, "
          + " ret_tran_seqno, "
          + " ret_fund_tran_seqno, "
          + " exchg_seqno, "
          + " ecoupon_bno, "
          + " ecoupon_date_s, "
          + " ecoupon_date_e, "
          + " ecoupon_date, "
          + " ecoupon_gl_date, "
          + " vendor_no, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr("apr_flag"),
        wp.itemStr("aud_type"),
        kk2,
        kk3,
        wp.itemStr("id_p_seqno"),
        wp.itemStr("p_seqno"),
        wp.itemStr("chi_name"),
        wp.itemStr("cellar_phone"),
        wp.itemStr("home_area_code1"),
        wp.itemStr("home_tel_no1"),
        wp.itemStr("home_tel_ext1"),
        wp.itemStr("office_area_code1"),
        wp.itemStr("office_tel_no1"),
        wp.itemStr("office_tel_ext1"),
        wp.itemStr("bill_sending_zip"),
        wp.itemStr("bill_sending_addr1"),
        wp.itemStr("bill_sending_addr2"),
        wp.itemStr("bill_sending_addr3"),
        wp.itemStr("bill_sending_addr4"),
        wp.itemStr("bill_sending_addr5"),
        wp.itemStr("gift_type"),
        wp.itemNum("exchg_cnt"),
        wp.itemNum("cash_value"),
        wp.itemStr("bonus_type"),
        colNum("exchg_pt"),
        colNum("exchg_amt"),
        colNum("total_pt"),
        colNum("end_tran_bp"),
        colNum("end_tran_bp01"),
        colNum("end_tran_bp90"),
        colNum("deduct_bp01"),
        colNum("deduct_bp90"),
        wp.itemStr("exchg_mark"),
        colStr("tran_date"),
        wp.itemStr("proc_flag"),
        wp.itemStr("status_flag"),
        wp.itemStr("deduct_flag"),
        colStr("deduct_date"),
        wp.itemStr("exg_apr_user"),
        colStr("exg_apr_date"),
        colStr("exg_gl_date"),
        wp.itemNum("unpay_cnt"),
        colStr("ecoupon_ret_date"),
        wp.itemStr("sms_resend_desc"),
        wp.itemStr("air_type"),
        wp.itemStr("cal_dfpno"),
        wp.itemStr("air_id_no"),
        wp.itemStr("air_birthday"),
        wp.itemStr("passport_name"),
        wp.itemStr("passport_surname"),
        colStr("send_date"),
        colStr("send_time"),
        colStr("recv_date"),
        colStr("recv_time"),
        wp.itemStr("rjct_flag"),
        wp.itemStr("rjct_code"),
        wp.itemStr("rjct_msg"),
        wp.itemStr("rjct_proc_code"),
        wp.itemStr("rjct_proc_remark"),
        wp.itemStr("air_apr_user"),
        colStr("air_apr_date"),
        wp.itemStr("out_date"),
        wp.itemStr("out_batchno"),
        wp.itemStr("out_mark"),
        wp.itemStr("register_no"),
        wp.itemStr("out_apr_user"),
        colStr("out_apr_date"),
        wp.itemStr("pay_date"),
        colStr("pay_gl_date"),
        wp.itemStr("pay_apr_user"),
        colStr("pay_apr_date"),
        wp.itemStr("return_date"),
        wp.itemStr("return_reason"),
        wp.itemStr("return_mark"),
        wp.itemStr("ret_apr_user"),
        colStr("ret_apr_date"),
        colStr("ret_gl_date"),
        wp.itemStr("fund_code"),
        wp.itemStr("from_mark"),
        colNum("cal_mile"),
        wp.itemStr("acct_type"),
        colStr("auth_code"),
        wp.itemStr("tran_time"),
        wp.itemStr("sms_flag"),
        colStr("sms_date"),
        colStr("process_code"),
        colStr("appr_err_code"),
        colStr("ret_tran_seqno"),
        colStr("ret_fund_tran_seqno"),
        colNum("exchg_seqno"),
        colStr("ecoupon_bno"),
        colStr("ecoupon_date_s"),
        colStr("ecoupon_date_e"),
        colStr("ecoupon_date"),
        colStr("ecoupon_gl_date"),
        colStr("vendor_no"),
        wp.loginUser,
        wp.modSeqno(),
        wp.loginUser,
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增 "+ controlTabName +" 重複錯誤");

  return rc;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  rc = getVdFlag(wp.itemStr("card_no"));
  if (rc!=1) return rc;
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " + controlTabName + " set "
         + "apr_flag = ?, "
         + "chi_name = ?, "
         + "cellar_phone = ?, "
         + "home_area_code1 = ?, "
         + "home_tel_no1 = ?, "
         + "home_tel_ext1 = ?, "
         + "office_area_code1 = ?, "
         + "office_tel_no1 = ?, "
         + "office_tel_ext1 = ?, "
         + "bill_sending_zip = ?, "
         + "bill_sending_addr1 = ?, "
         + "bill_sending_addr2 = ?, "
         + "bill_sending_addr3 = ?, "
         + "bill_sending_addr4 = ?, "
         + "bill_sending_addr5 = ?, "
         + "exchg_cnt = ?, "
         + "cash_value = ?, "
         + "exchg_pt = ?, "
         + "exchg_amt = ?, "
         + "total_pt = ?, "
         + "end_tran_bp = ?, "
         + "end_tran_bp01 = ?, "
         + "end_tran_bp90 = ?, "
         + "deduct_bp01 = ?, "
         + "deduct_bp90 = ?, "
         + "exchg_mark = ?, "
         + "sms_flag = ?, "
         + "sms_resend_desc = ?, "
         + "cal_dfpno = ?, "
         + "air_id_no = ?, "
         + "air_birthday = ?, "
         + "passport_name = ?, "
         + "passport_surname = ?, "
         + "rjct_proc_code = ?, "
         + "rjct_proc_remark = ?, "
         + "out_date = ?, "
         + "out_batchno = ?, "
         + "out_mark = ?, "
         + "register_no = ?, "
         + "pay_date = ?, "
         + "return_date = ?, "
         + "return_reason = ?, "
         + "return_mark = ?, "
         + "fund_code = ?, "
         + "proc_flag = ?, "
         + "status_flag = ?, "
         + "crt_user  = ?, "
         + "crt_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr("apr_flag"),
     wp.itemStr("chi_name"),
     wp.itemStr("cellar_phone"),
     wp.itemStr("home_area_code1"),
     wp.itemStr("home_tel_no1"),
     wp.itemStr("home_tel_ext1"),
     wp.itemStr("office_area_code1"),
     wp.itemStr("office_tel_no1"),
     wp.itemStr("office_tel_ext1"),
     wp.itemStr("bill_sending_zip"),
     wp.itemStr("bill_sending_addr1"),
     wp.itemStr("bill_sending_addr2"),
     wp.itemStr("bill_sending_addr3"),
     wp.itemStr("bill_sending_addr4"),
     wp.itemStr("bill_sending_addr5"),
     wp.itemNum("exchg_cnt"),
     wp.itemNum("cash_value"),
     wp.itemNum("exchg_pt"),
     wp.itemNum("exchg_amt"),
     wp.itemNum("total_pt"),
     wp.itemNum("end_tran_bp"),
     wp.itemNum("end_tran_bp01"),
     wp.itemNum("end_tran_bp90"),
     wp.itemNum("deduct_bp01"),
     wp.itemNum("deduct_bp90"),
     wp.itemStr("exchg_mark"),
     wp.itemStr("sms_flag"),
     wp.itemStr("sms_resend_desc"),
     wp.itemStr("cal_dfpno"),
     wp.itemStr("air_id_no"),
     wp.itemStr("air_birthday"),
     wp.itemStr("passport_name"),
     wp.itemStr("passport_surname"),
     wp.itemStr("rjct_proc_code"),
     wp.itemStr("rjct_proc_remark"),
     wp.itemStr("out_date"),
     wp.itemStr("out_batchno"),
     wp.itemStr("out_mark"),
     wp.itemStr("register_no"),
     wp.itemStr("pay_date"),
     wp.itemStr("return_date"),
     wp.itemStr("return_reason"),
     wp.itemStr("return_mark"),
     wp.colStr("fund_code"),
     colStr("proc_flag"),
     wp.itemStr("status_flag"),
     wp.loginUser,
     wp.loginUser,
     wp.itemStr("mod_pgm"),
     wp.itemRowId("rowid"),
     wp.itemNum("mod_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 "+ controlTabName +" 錯誤");

  if (sqlRowNum <= 0) rc=0;else rc=1;
  return rc;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("D");
  dataCheck();
  if (rc!=1)return rc;

  strSql = "delete " + controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("rowid")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (sqlRowNum <= 0) 
     {
      errmsg("刪除 "+ controlTabName +" 錯誤");
      return(-1);
     }

  return rc;
 }
 int checkCalDfpno3(String ckString)
 {
    if ( ckString.length() != 10 )
        return -1; 

    byte[] checkData = ckString.getBytes();

    for( int i=0; i<checkData.length; i++ )
        if ( checkData[i] < '0' || checkData[i] > '9' ) return -1;

    int sumInt=0;
    for( int i=0; i<9; i++ )
        sumInt = sumInt + (ckString.charAt(i)- '0')*(i+1);

  if ((sumInt%10)!= (ckString.charAt(9)- '0')) return -1;

  return 0;
 }
// ************************************************************************
 int checkGiftCount()
 {
  strSql= " select "
        + " sum(exchg_cnt) as exchg_cnt  "
        + " from mkt_gift_bpexchg_t "
        + " where gift_no = ? "
        + " and   tran_seqno != ? "
        ;

  Object[] param =new Object[]
       {
        wp.itemStr("gift_no"),
        wp.itemStr("tran_seqno")
       };

  sqlSelect(strSql, param);
  if (sqlRowNum <= 0) colSet("exchg_cnt","0");

  if (!colStr("gift_type").equals("3"))
     {
      if (colNum("net_count")<(colNum("exchg_cnt")+wp.itemNum("exchg_cnt")))
         {
          errmsg("兌換數量超過庫存待兌數量["
                + String.format("%d",(int)colNum("net_count"))
                + "] - 待覆核數量[" 
                + String.format("%d",(int)colNum("exchg_cnt"))
                + "] -本次兌換數量[" 
                + String.format("%d",(int)wp.itemNum("exchg_cnt"))
                + "] 無法兌換");
          return(1);
         }
     }
  else
     {
      if (colNum("net_limit_cnt")<(colNum("exchg_cnt")+wp.itemNum("exchg_cnt")))
         {
          errmsg("兌換數量超過待兌數量["
                + String.format("%d",(int)colNum("net_limit_cnt"))
                + "] - 待覆核數量[" 
                + String.format("%d",(int)colNum("exchg_cnt"))
                + "] -本次兌換數量[" 
                + String.format("%d",(int)wp.itemNum("exchg_cnt"))
                + "] 無法兌換");
          return(1);
         }
     }

  return 0;
 }
// ************************************************************************
	int getVdFlag(String cardNo) {
		strSql = "select '0' as vd_flag from crd_card a join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
		strSql += " where a.acct_type = '01' and a.current_code = '0' and a.sup_flag ='0' and a.card_no = :crd_card_no ";
		strSql += " union ";
		strSql += " select '1' as vd_flag from dbc_card a join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
		strSql += " where a.acct_type ='90' and a.current_code = '0' and a.card_no = :dbc_card_no ";
		setString("crd_card_no",cardNo);
		setString("dbc_card_no",cardNo);
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			errmsg("查無卡號");
			return 0;
		}
		vdFlag = colStr("vd_flag");
		if ("0".equals(vdFlag)) {
			cardTable = "crd_card ";
			idnoTable = "crd_idno ";
			acnoTable = "act_acno ";
			cardNote = "a.card_note ";
		} else if ("1".equals(vdFlag)) {
			cardTable = "dbc_Card ";
			idnoTable = "dbc_idno ";
			acnoTable = "dba_acno ";
			cardNote = "substring(a.card_type,1,1) ";
		}
		return 1;
	}


// ************************************************************************

}  // End of class
