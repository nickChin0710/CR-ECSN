/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/03/19  V1.00.12  Allen Ho   new                                        *
* 110/04/14  V1.00.13  Yinchao    organize imports                           *
* 110/05/21  V1.00.07  Allen Ho   CR1313                                      *
* 110/09/03  V1.01.01  Allen Ho   check vendor_no                             *
* 111/11/06  V1.01.02  Zuwei Su   sync from mega & coding standard update    *   
******************************************************************************/
package com;

import java.sql.Connection;
@SuppressWarnings({"unchecked", "deprecation"})
public class CommExchg extends AccessDAO
{
  CommFunction comm = new CommFunction();
  CommRoutine  comr = null;
  CommBonus    comb = null;
  CommCashback comc = null;
//  CommCCASLink comk = null;

  final int DEBUG_S = 1;
  // Input
  public String checkKey    = ""; 
  public String cardNo      = ""; 
  public String giftNo      = ""; 
  public int    exchgCnt    = 0;
  public String fromMark    = "";
  public String modPgm      = "";
  public String cellarPhone = "";
   
  //Output 
  public String authCode  = "";
  public String procCode4 = "";
  public String procCode  = "";
  public String procMsg   = "";

  private double bonusBp   = 0;
  private String tranSeqno = "";
  private String hBusiBusinessDate = "";
  private int    retInt    = 0;

  String[] DBNAME = new String[10];
// ************************************************************************
 public  CommExchg(Connection conn[],String[] dbAlias) throws Exception
 {
   super.conn  = conn;
   setDBalias(dbAlias);
   setSubParm(dbAlias);

   DBNAME[0]=dbAlias[0];

   comr = new CommRoutine(conn,dbAlias);
   comb = new CommBonus(conn,dbAlias);
   comc = new CommCashback(conn,dbAlias);
//   comk = new CommCCASLink(conn,getDBalias());

   return;
 }
// ************************************************************************
 public int bonusExchange() throws Exception
 {
  if (cardNo.length()==0)
     { 
      procMsg   = "沒有卡號";
      procCode  = "01";
      procCode4 = "7001";
      return(1);  
     }
  if (giftNo.length()==0) 
     { 
      procMsg = "沒有商品代號";
      procCode  = "02";
      procCode4 = "7002";
      return(1);  
     }
  if (exchgCnt==0) 
     { 
      procMsg = "兌換數量為0";
      procCode  = "03";
      procCode4 = "7003";
      return(1);  
     }
  if ((fromMark.length()==0)||
      (modPgm.length()==0))
     { 
      procMsg = "來源或啟動程式代碼為空值";
      procCode  = "99";
      procCode4 = "9999";
      return(1);  
     }
  if ((!fromMark.equals("1"))&&
      (!fromMark.equals("2"))&&
      (!fromMark.equals("3"))) 
     { 
      procMsg = "來源 1-(人工) 2-(語音) 3-(網路)";
      procCode  = "99";
      procCode4 = "9999";
      return(1);  
     }
// ************* 檢核卡片  start **************
  if (selectCrdCard()!=0)
     {
      procMsg = "卡號不存在";
      procCode  = "21";
      procCode4 = "7021";
      return(2);  
     }
  if (!cardNo.equals(getValue("card.major_card_no")))
     {
      procMsg = "卡片必須為正卡";
      procCode  = "22";
      procCode4 = "7022";
      return(2);  
     }
  if (!getValue("card.current_code").equals("0"))
     {
      procMsg = "卡號非有效卡";
      procCode  = "23";
      procCode4 = "7023";
      return(2);  
     }
// ************* 檢核商品  start **************
  if (selectMktGift()!=0)
     { 
      procMsg = "商品代號錯誤 ";
      procCode  = "11";
      procCode4 = "7011";
      return(2);  
     }
  if (getValue("gift.air_type").length()!=0)
     { 
      procMsg = " 航空里程商品只能人工兌換";
      procCode  = "12";
      procCode4 = "7012";
      return(2);  
     }

  if (getValue("gift.disable_flag").equals("Y"))
     { 
      procMsg = "商品已下架";
      procCode  = "13";
      procCode4 = "7013";
      return(2);  
     }

  if (getValue("gift.gift_type").equals("1"))
     {
      if (getValueInt("gift.supply_count")-
          getValueInt("gift.use_count")-
          getValueInt("gift.web_sumcnt")-
          exchgCnt<=0)
         { 
          procMsg = "商品已無數量可兌換";
          procCode  = "14";
          procCode4 = "7014";
          return(2);  
         }
     }
  else if (getValue("gift.gift_type").equals("3"))
     {
      if (getValueInt("gift.max_limit_count")-
          getValueInt("gift.use_limit_count")-
          getValueInt("gift.web_sumcnt")-
          exchgCnt<=0)
         { 
          procMsg = "商品已無數量可兌換";
          procCode  = "14";
          procCode4 = "7014";
          return(2);  
         }
     }

  if (selectMktGiftExchgdata1()!=0)
     if (selectMktGiftExchgdata2()!=0)
        {
         procMsg = "商品未提供該卡片兌換 ";
         procCode  = "15";
         procCode4 = "7015";
         return(2);  
        }
//  ************** 檢核商品  end   **************


//  ************** 檢核卡人資料 beg**************
  if (selectCrdIdno()!=0)
     {
      procMsg = "系統錯誤";
      procCode  = "51";
      procCode4 = "9999";
      return(3);  
     }

  if (getValue("gift.gift_type").equals("3"))
     {
      if (cellarPhone.length()!=0)
         setValue("idno.cellar_phone" , cellarPhone);

      if (getValue("idno.cellar_phone").length()==0)
         { 
          procMsg = "兌換電子商品必須有手機號碼";
          procCode  = "41";
          procCode4 = "7041";
          return(2);
         }   
     }

  if (selectActAcno()!=0)
     {
      procMsg = "系統錯誤";
      procCode  = "52";
      procCode4 = "9999";
      return(3);  
     }
//  ************** 檢核卡人資料 end**************
  if (selectPtrBusinday()!=0)
     {
      procMsg = "系統錯誤";
      procCode  = "51";
      procCode4 = "9999";
      return(3);  
     }
  if (getValueInt("data.exchg_amt")>0)
     {
      if (fromMark.equals("2"))
         {
          procMsg = "語音不可有自付額兌換";
          procCode  = "16";
          procCode4 = "7016";
          return(1);
         }
      else
         {
          dateTime();
//          comk.transType   = "1";                              // * 1: regular 2:refund 3:reversal
//          comk.typeFlag    = "";                               //   A: install B: mail '':none
//          comk.cardNo      = card_no;                          // *
//          comk.newEndDate = getValue("card.new_end_date");    //   YYYYMMDD
//          comk.transAmt    = getValueInt("data.exchg_amt")*exchg_cnt;   // *
//          comk.mccCode     = "5964";                           //   default 5964 bit18 mcc code
//          comk.mchtNo      = "7850655226";                     //   default 7850655226  bit42 acceptor_id=mcht_no
//          comk.orgAuthNo  = "";                               // V when trans_type=2 need this
//          comk.orgRefNo   = sysDate.substring(2,8)+sysTime;                               // V when trans_type=3
//          retInt = comk.callCCASLink();
//
//          if (retInt != 0)
//             {
//              procMsg   = "授權失敗";
//              procCode  = "42";
//              procCode4 = "7042";
//              return(2);
//             }
//           authCode = comk.auth_code;
         }
     }

  setValue("order_flag" , "N");
//  ************** 檢核紅利點數 beg**************
  bonusBp = comb.bonusSum(getValue("card.p_seqno"),getValue("gift.bonus_type"));
  if (bonusBp <=0)
     {
      procMsg = "無紅利點數可兌換";
      procCode  = "31";
      procCode4 = "7031";
      if (insertMktVoice()!=0)
         {
          procMsg = "系統錯誤";
          procCode  = "67";
          procCode4 = "9999";
          return(3);  
         }
      return(3);  
     }
  setValue("tchg.total_bp" , "0");
  selectMktGiftBpexchgT();

  if (bonusBp < (getValueInt("data.exchg_pt")*exchgCnt + getValueInt("tchg.total_bp")))
     {
      procMsg = "紅利點數不足";
      procCode  = "32";
      procCode4 = "7032";
      if (insertMktVoice()!=0)
         {
          procMsg = "系統錯誤";
          procCode  = "68";
          procCode4 = "9999";
          return(3);  
         }
      return(3);  
     }
//  ************** 檢核紅利點數 end**************

  tranSeqno = comr.getSeqno("MKT_MODSEQ");
  if (getValue("gift.gift_type").equals("2"))
     {
      dateTime();
      setValue("excg.proc_flag"          , "B");
      setValue("excg.status_flag"        , "7");

      setValue("excg.out_date"           , sysDate);
      setValue("excg.out_apr_date"       , sysDate);
      setValue("excg.out_apr_user"       , "SYSTEM");

      setValue("excg.pay_date"           , sysDate);
      setValue("excg.pay_apr_date"       , sysDate);
      setValue("excg.pay_gl_date"        , hBusiBusinessDate);
      setValue("excg.pay_apr_user"       , "SYSTEM");
     }
  else
     {
      setValue("excg.proc_flag"          , "N");
      setValue("excg.status_flag"        , "2");

      setValue("excg.out_date"           , "");
      setValue("excg.out_apr_date"       , "");
      setValue("excg.out_apr_user"       , "");

      setValue("excg.pay_date"           , "");
      setValue("excg.pay_apr_date"       , "");
      setValue("excg.pay_gl_date"        , "");
      setValue("excg.pay_apr_user"       , "");
     }
  if (insertMktGiftBpexchg()!=0)
     {
      if (reverseCcas()!=0) rollbackDataBase();
      procMsg = "系統錯誤";
      procCode  = "55";
      procCode4 = "9999";
      return(3);  
     }

  if (insertMktBonusDtl()!=0)
     {
      if (reverseCcas()!=0) rollbackDataBase();
      procMsg = "系統錯誤";
      procCode  = "56";
      procCode4 = "9999";
      return(3);  
     }
  comb.modPgm = modPgm;
  comb.bonusFunc(tranSeqno);

  if (getValue("gift.gift_type").equals("2"))
     {
      if (selectVmktFundName()!=0) 
         {
          if (reverseCcas()!=0) rollbackDataBase();
          procMsg = "系統錯誤";
          procCode  = "57";
          procCode4 = "9999";
          return(3);  
         }
      if (selectFundParm()!=0) 
         {
          if (reverseCcas()!=0) rollbackDataBase();
          procMsg = "系統錯誤";
          procCode  = "58";
          procCode4 = "9999";
          return(3);  
         }
      if (selectPtrWorkday()!=0) 
         {
          if (reverseCcas()!=0) rollbackDataBase();
          procMsg = "系統錯誤";
          procCode  = "59";
          procCode4 = "9999";
          return(3);  
         }
      if (insertMktCashbackDtl()!=0) 
         {
          if (reverseCcas()!=0) rollbackDataBase();
          procMsg = "系統錯誤";
          procCode  = "60";
          procCode4 = "9999";
          return(3);  
         }
//      comc.modPgm = modPgm;
//      comc.cashbackFunc(tranSeqno);

      if (insertCycFundDtl()!=0) 
         {
          if (reverseCcas()!=0) rollbackDataBase();
          procMsg = "系統錯誤";
          procCode  = "61";
          procCode4 = "9999";
          return(3);  
         }
     }

  if (!getValue("gift.gift_type").equals("2"))
  if (insertMktGiftVouch("1")!=0)
     {
      if (reverseCcas()!=0) rollbackDataBase();  
      procMsg = "系統錯誤";
      procCode  = "62";
      procCode4 = "9999";
      return(3);  
     }

  if (getValue("gift.gift_type").equals("3"))
     if (updateMktGiftE()!=0)
        {
         if (reverseCcas()!=0) rollbackDataBase();  ;
         procMsg = "系統錯誤";
         procCode  = "65";
         procCode4 = "9999";
         return(3);  
        }
  else
     if (updateMktGift()!=0)
        {
         if (reverseCcas()!=0)  rollbackDataBase(); 
         procMsg = "系統錯誤";
         procCode  = "64";
         procCode4 = "9999";
         return(3);  
        }
  if (getValueInt("data.exchg_amt")>0)
     {
      if (insertBilSysexp()!=0)
         {
          if (reverseCcas()!=0)  rollbackDataBase(); 
          procMsg = "系統錯誤";
          procCode  = "65";
          procCode4 = "9999";
          return(3);  
         }
     }

  setValue("order_flag" , "Y");
  if (insertMktVoice()!=0)
     {
      procMsg = "系統錯誤";
      procCode  = "64";
      procCode4 = "9999";
      return(3);
     }

  procCode  = "00";
  procCode4 = "0000";
  procMsg = "紅利兌換成功";
  return(0);
 }
// ************************************************************************ 
 int selectPtrBusinday() throws Exception
 {
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  hBusiBusinessDate   =  getValue("BUSINESS_DATE");

  return(0);
 }
// ************************************************************************ 
 int selectCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "p_seqno,"
            + "new_end_date,"
            + "current_code,"
            + "id_p_seqno,"
            + "acct_type,"
            + "major_card_no,"
            + "group_code,"
            + "card_note";
  daoTable  = "crd_card";
  whereStr  = "WHERE card_no = ? "
            ;

  setString(1 , cardNo);

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktGift() throws Exception
 {
  extendField = "gift.";
  selectSQL = "gift_type,"
            + "gift_name,"
            + "vendor_no,"
            + "air_type,"
            + "fund_code,"
            + "cash_value,"
            + "bonus_type,"
            + "supply_count,"
            + "use_count,"
            + "web_sumcnt,"
            + "max_limit_count,"
            + "use_limit_count,"
            + "disable_flag";
  daoTable  = "mkt_gift";
  whereStr  = "WHERE gift_no = ? "
            + "and disable_flag !='Y' "
            ;

  setString(1 , giftNo);

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktGiftExchgdata1() throws Exception
 {
  extendField = "data.";
  selectSQL = "exchange_bp as exchg_pt,"
            + "exchange_amt as exchg_amt";
  daoTable  = "mkt_gift_exchgdata";
  whereStr  = "WHERE gift_no    = ? "
            + "and   group_code = ? "
            + "and   card_note  = ? "
            + "and   group_code!= '' "
            ;

  setString(1 , giftNo);
  setString(2 , getValue("card.group_code"));
  setString(3 , getValue("card.card_note"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktGiftExchgdata2() throws Exception
 {
  extendField = "data.";
  selectSQL = "exchange_bp as exchg_pt,"
            + "exchange_amt as exchg_amt";
  daoTable  = "mkt_gift_exchgdata";
  whereStr  = "WHERE gift_no    = ? "
            + "and   card_note  = ? "
            + "and   group_code = '' "
            ;

  setString(1 , giftNo);
  setString(2 , getValue("card.card_note"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktGiftBpexchgT() throws Exception
 {
  extendField = "tchg.";
  selectSQL = "sum(exchg_pt) total_bp";
  daoTable  = "mkt_gift_bpexchg_t";
  whereStr  = "WHERE p_seqno    = ? "
            ;

  setString(1 , getValue("card.p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectCrdIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "chi_name ,"
            + "cellar_phone ,"
            + "home_area_code1 ,"
            + "home_tel_no1 ,"
            + "home_tel_ext1 ,"
            + "office_area_code1 ,"
            + "office_tel_no1 ,"
            + "office_tel_ext1 ";
  daoTable  = "crd_idno";
  whereStr  = "WHERE id_p_seqno    = ? "
            ;

  setString(1 , getValue("card.id_p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "vip_code,"
            + "stmt_cycle,"
            + "bill_sending_zip ,"
            + "bill_sending_addr1 ,"
            + "bill_sending_addr2 ,"
            + "bill_sending_addr3 ,"
            + "bill_sending_addr4 ,"
            + "bill_sending_addr5 ";
  daoTable  = "act_acno";
  whereStr  = "WHERE p_seqno    = ? "
            ;

  setString(1 , getValue("card.p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int  insertMktGiftBpexchg() throws Exception
 {
  extendField = "excg.";

  dateTime();
  setValue("excg.card_no"            , cardNo);
  setValue("excg.gift_no"            , giftNo);
  setValue("excg.tran_seqno"         , tranSeqno);
  setValue("excg.id_p_seqno"         , getValue("card.id_p_seqno"));
  setValue("excg.p_seqno"            , getValue("card.p_seqno"));
  setValue("excg.chi_name"           , getValue("idno.chi_name"));
  setValue("excg.cellar_phone"       , getValue("idno.cellar_phone")); 
  setValue("excg.home_area_code1"    , getValue("idno.home_area_code1")); 
  setValue("excg.home_tel_no1"       , getValue("idno.home_tel_no1")); 
  setValue("excg.home_tel_ext1"      , getValue("idno.home_tel_ext1")); 
  setValue("excg.office_area_code1"  , getValue("idno.office_area_code1")); 
  setValue("excg.office_tel_no1"     , getValue("idno.office_tel_no1")); 
  setValue("excg.office_tel_ext1"    , getValue("idno.office_tel_ext1")); 
  setValue("excg.bill_sending_zip"   , getValue("acno.bill_sending_zip")); 
  setValue("excg.bill_sending_addr1" , getValue("acno.bill_sending_addr1"));
  setValue("excg.bill_sending_addr2" , getValue("acno.bill_sending_addr2"));
  setValue("excg.bill_sending_addr3" , getValue("acno.bill_sending_addr3"));
  setValue("excg.bill_sending_addr4" , getValue("acno.bill_sending_addr4"));
  setValue("excg.bill_sending_addr5" , getValue("acno.bill_sending_addr5"));
  setValue("excg.gift_type"          , getValue("gift.gift_type")); 
  setValue("excg.vendor_no"          , getValue("gift.vendor_no")); 
  setValue("excg.fund_code"          , getValue("gift.fund_code"));
  setValueInt("excg.exchg_cnt"       , exchgCnt);
  setValue("excg.cash_value"         , getValue("gift.cash_value")); 
  setValueInt("excg.total_pt"        , (int)getValueInt("data.exchg_pt")*exchgCnt);
  setValue("excg.exchg_pt"           , getValue("data.exchg_pt")); 
  setValueDouble("excg.end_tran_bp"  , bonusBp);
  setValue("excg.bonus_type"         , getValue("gift.bonus_type"));  
  setValue("excg.vip_code"           , getValue("acno.vip_code"));  
  setValue("excg.acct_date"          , hBusiBusinessDate);
  setValue("excg.tran_date"          , sysDate);
  setValue("excg.deduct_flag"        , "Y");
  setValue("excg.deduct_date"        , sysDate); 
  setValue("excg.exg_gl_date"        , hBusiBusinessDate);
  setValue("excg.exg_apr_user"       , "SYSTEM");
  setValue("excg.exg_apr_date"       , sysDate);
  setValue("excg.from_mark"          , fromMark);
  setValue("excg.acct_type"          , getValue("card.acct_type"));  
  setValue("excg.auth_code"          , authCode);
  setValue("excg.tran_time"          , sysTime);
  setValue("excg.crt_date"           , sysDate);
  setValue("excg.crt_user"           , modPgm);
  setValue("excg.apr_user"           , "SYSTEM");
  setValue("excg.apr_flag"           , "Y");
  setValue("excg.apr_date"           , sysDate);
  setValue("excg.mod_seqno"          , "0");
  setValue("excg.mod_time"           , sysTime);
  setValue("excg.mod_user"           , "SYSTEM");
  setValue("excg.mod_pgm"            , modPgm);

  daoTable  = "mkt_gift_bpexchg";

  int n = insertTable();
  if (n==0) return(1);

  return(0);
 }
// ************************************************************************
 int insertMktBonusDtl() throws Exception
 {
  extendField = "mbdl.";
  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);
  setValue("mbdl.tran_seqno"           , tranSeqno);
  setValue("mbdl.bonus_type"           , getValue("gift.bonus_type"));
  setValue("mbdl.tran_code"            , "4");
  setValue("mbdl.mod_memo"             , getValue("gift.gift_name"));
  setValue("mbdl.mod_desc"             , "兌換贈品["+ giftNo +"] 共 ["+String.format("%d", exchgCnt)+"]件");
  setValue("mbdl.active_name"          , "");
  if (fromMark.equals("1"))
     setValue("mbdl.active_name"       , "紅利積點兌換贈品(人工)");
  else if (fromMark.equals("2")) 
     setValue("mbdl.active_name"       , "紅利積點兌換贈品(語音)");
  else if (fromMark.equals("3")) 
     setValue("mbdl.active_name"       , "紅利積點兌換贈品(網路)");
  setValueInt("mbdl.beg_tran_bp"       , (int)getValueInt("data.exchg_pt")*exchgCnt*-1);
  setValueInt("mbdl.end_tran_bp"       , (int)getValueInt("data.exchg_pt")*exchgCnt*-1);
  setValue("mbdl.acct_date"            , hBusiBusinessDate);
  setValue("mbdl.acct_type"            , getValue("card.acct_type"));
  setValue("mbdl.p_seqno"              , getValue("card.p_seqno"));
  setValue("mbdl.id_p_seqno"           , getValue("card.id_p_seqno"));
  setValue("mbdl.tran_pgm"             , modPgm);
  setValue("mbdl.effect_e_date"        , "");
  setValue("mbdl.apr_user"             , "SYSTEM");
  setValue("mbdl.apr_flag"             , "Y");
  setValue("mbdl.apr_date"             , sysDate);
  setValue("mbdl.crt_user"             , "SYSTEM");
  setValue("mbdl.crt_date"             , sysDate);
  setValue("mbdl.mod_user"             , "SYSTEM");
  setValue("mbdl.mod_time"             , sysDate+sysTime);
  setValue("mbdl.mod_pgm"              , modPgm);

  daoTable  = "mkt_bonus_dtl";

  int n = insertTable();
  if (n==0) return(1);

  return(0);
 }
// ************************************************************************
 int selectVmktFundName() throws Exception 
 {
  extendField = "fund.";
  selectSQL = "fund_name,"
            + " table_name ";
  daoTable  = "vmkt_fund_name";
  whereStr  = "WHERE fund_code  = ? "
            ;

  setString(1 , getValue("gift.fund_code"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectFundParm() throws Exception 
 {
  extendField = "parm.";
  selectSQL = "effect_months";
  daoTable  = getValue("fund.table_name");
  whereStr  = "WHERE fund_code  = ? "
            ;

  setString(1 , getValue("gift.fund_code"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int insertCycFundDtl() throws Exception
 {
  extendField = "fund.";
  setValue("fund.create_date"          , sysDate);
  setValue("fund.create_time"          , sysTime);
  setValue("fund.business_date"        , hBusiBusinessDate);
  setValue("fund.p_seqno"              , getValue("card.p_seqno"));
  setValue("fund.acct_type"            , getValue("card.acct_type"));
  setValue("fund.id_p_seqno"           , getValue("card.id_p_seqno"));
  setValue("fund.curr_code"            , "901");
  setValue("fund.execute_date"         , hBusiBusinessDate);
  setValue("fund.fund_code"            , getValue("gift.fund_code"));
  setValue("fund.tran_code"            , "4");
  setValueInt("fund.fund_amt"          , getValueInt("gift.cash_value")*exchgCnt);
  setValueInt("fund.fund_amt"          , getValueInt("gift.cash_value")*exchgCnt);
  setValue("fund.vouch_type"           , "3");
  setValue("fund.cd_kind"              , "A-36");
  setValue("fund.memo1_type"           , "1");
  setValue("fund.src_pgm"              , modPgm); 
  setValue("fund.proc_flag"            , "N");
  setValue("fund.mod_time"             , sysDate+sysTime);
  setValue("fund.mod_pgm"              , modPgm);

  daoTable  = "cyc_fund_dtl";

  int n = insertTable();
  if (n==0) return(1);

  return(0);
 }
// ************************************************************************
 int insertMktGiftVouch(String vouch_data_type) throws Exception
 {
  extendField = "vouc.";
  setValue("vouc.create_date"          , sysDate);
  setValue("vouc.create_time"          , sysTime);
  setValue("vouc.p_seqno"              , getValue("card.p_seqno"));
  setValue("vouc.acct_type"            , getValue("card.acct_type"));
  setValue("vouc.card_no"              , cardNo);
  setValue("vouc.tran_seqno"           , tranSeqno);
  setValue("vouc.gift_no"              , giftNo);
  setValue("vouc.vouch_seqno"          , tranSeqno);
  setValue("vouc.business_date"        , hBusiBusinessDate);
  setValueInt("vouc.vouch_amt"         , getValueInt("gift.cash_value")*exchgCnt);
  setValueInt("vouc.d_vouch_amt"       , 0);
  setValue("vouc.vouch_data_type"      , vouch_data_type);  
  setValue("vouc.tran_pgm"             , modPgm);
  setValue("vouc.status_code"          , "N");
  setValue("vouc.proc_flag"            , "N");
  setValue("vouc.mod_time"             , sysDate+sysTime);
  setValue("vouc.mod_pgm"              , modPgm);

  daoTable  = "mkt_gift_vouch";

  int n = insertTable();
  if (n==0) return(1);

  return(0);
 }
// ************************************************************************
 int updateMktGiftE() throws Exception
 {
  updateSQL = "use_limit_count = use_limit_count + ?,  "
            + "limit_last_date = ?,  "
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";   
  daoTable  = "mkt_gift";
  whereStr  = "WHERE gift_no = ?";

  setInt(1 , exchgCnt);
  setString(2 , sysDate);
  setString(3 , modPgm);
  setString(4 , giftNo);

  updateTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int updateMktGift() throws Exception
 {
  updateSQL = "use_count       = use_count + ?,  "
            + "limit_last_date = ?,  "
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";   
  daoTable  = "mkt_gift";
  whereStr  = "WHERE gift_no = ?";

  setInt(1 , exchgCnt);
  setString(2 , sysDate);
  setString(3 , modPgm);
  setString(4 , giftNo);

  updateTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectPtrWorkday()  throws Exception 
 {
  extendField = "wday.";
  selectSQL = "next_acct_month";
  daoTable  = "ptr_workday";
  whereStr  = "WHERE stmt_cycle = ? "
            ;

  setString(1 , getValue("acno.stmt_cycle"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int insertMktCashbackDtl() throws Exception
 {
  dateTime();
  extendField = "cash.";
  setValue("cash.tran_date"            , sysDate);
  setValue("cash.tran_time"            , sysTime);
  setValue("cash.tran_seqno"           , tranSeqno);
  setValue("cash.fund_code"            , getValue("gift.fund_code"));
  setValue("cash.fund_name"            , getValue("fund.fund_name"));
  setValue("cash.acct_type"            , getValue("card.acct_type"));
  setValue("cash.tran_code"            , "1");
  setValueInt("cash.beg_tran_amt"      , getValueInt("gift.cash_value")*exchgCnt);
  setValueInt("cash.end_tran_amt"      , getValueInt("gift.cash_value")*exchgCnt);
  setValue("cash.tax_flag"             , "N");
  setValue("cash.mod_desc"             , "兌換贈品["+ giftNo +"] 共 ["+String.format("%d", exchgCnt )+"]件");
  setValue("cash.mod_memo"             , getValue("gift.gift_name"));
  setValue("cash.p_seqno"              , getValue("card.p_seqno"));
  setValue("cash.id_p_seqno"           , getValue("card.id_p_seqno"));
  setValue("cash.acct_date"            , hBusiBusinessDate);
  setValue("cash.tran_pgm"             , modPgm);
  setValue("cash.effect_e_date"        , "");
  if (getValueInt("parm.effect_months")>0)
     setValue("cash.effect_e_date"     ,  comm.nextMonthDate(hBusiBusinessDate,getValueInt("parm.effect_months")));
  setValue("cash.acct_month"           , getValue("wday.next_acct_month"));
  setValue("cash.apr_user"             , "SYSTEM");
  setValue("cash.apr_flag"             , "Y");
  setValue("cash.apr_date"             , sysDate);
  setValue("cash.crt_user"             , "SYSTEM");
  setValue("cash.crt_date"             , sysDate);
  setValue("cash.mod_user"             , "SYSTEM");
  setValue("cash.mod_time"             , sysDate+sysTime);
  setValue("cash.mod_pgm"              , modPgm);

  daoTable  = "mkt_cashback_dtl";

  int n = insertTable();

  if (n==0) return(1);

  return(0);
 }
// ************************************************************************
 int reverseCcas() throws Exception
 {
   if (getValueInt("data.exchg_amt")>0)
      {
//       comk.transType   = "3";                              // * 1: regular 2:refund 3:reversal
//       comk.orgAuthNo  = auth_code;                        // V when trans_type=2 need this
//       retInt = comk.callCCASLink();

       if (retInt != 0) return(1);
      }

  return(0);
 }
// ************************************************************************
 int insertBilSysexp() throws Exception
 {
  dateTime();
  extendField = "sysp.";
  setValue("sysp.card_no"            , cardNo);
  setValue("sysp.p_seqno"            , getValue("card.p_seqno"));
  setValue("sysp.acct_type"          , getValue("card.acct_type"));
  setValue("sysp.bill_type"          , "OSSG");
  setValue("sysp.txn_code"           , "05");
  setValue("sysp.purchase_date"      , hBusiBusinessDate);
  setValueInt("sysp.src_amt"         , getValueInt("data.exchg_amt")*exchgCnt);
  setValue("sysp.dest_amt"           , getValue("sysp.src_amt"));
  setValue("sysp.dc_dest_amt"        , getValue("sysp.src_amt")); 
  setValue("sysp.dest_curr"          , "901");
  setValue("sysp.curr_code"          , "901");
  setValue("sysp.bill_desc"          , "");
  if (fromMark.equals("1"))
     setValue("sysp.bill_desc"       , "紅利積點兌換贈品(人工)");
  else if (fromMark.equals("2")) 
     setValue("sysp.bill_desc"       , "紅利積點兌換贈品(語音)");
  else if (fromMark.equals("3")) 
     setValue("sysp.bill_desc"       , "紅利積點兌換贈品(網路)");
  setValue("sysp.post_flag"          , "N");
  setValue("sysp.ref_key"            , tranSeqno);
  setValue("sysp.mod_user"           , modPgm);
  setValue("sysp.mod_time"           , sysDate+sysTime);
  setValue("sysp.mod_pgm"            , modPgm);

  daoTable  = "bil_sysexp";

  int n = insertTable();

  if (n==0) return(1);

  return(0);
 }
// ************************************************************************
 int insertMktVoice() throws Exception
 {
  dateTime();
  extendField = "voic.";

  setValue("voic.p_seqno"            , getValue("card.p_seqno"));
  setValue("voic.document"           , "0");
  if (fromMark.equals("2"))
     setValue("voic.document"        , "7030");
  setValue("voic.input_date"         , sysDate);
  setValue("voic.input_time"         , sysTime);
  setValue("voic.function_code"      , "7030");
  setValue("voic.card_no"            , cardNo);
  setValue("voic.id_no"              , getValue("idno.id_no"));
  setValue("voic.acct_type"          , getValue("card.acct_type"));
  setValue("voic.acct_key"           , getValue("acno.acct_key"));
  setValue("voic.stmt_cycle"         , getValue("acno.stmt_cycle"));
  setValue("voic.gift_no"            , giftNo);
  setValueInt("voic.exchange_cnt"    , exchgCnt);
  setValue("voic.process_code"       , "");
  setValue("voic.exchange_type"      , getValue("gift.gift_type"));

  setValue("voic.exchange_point"     , "0");
  setValue("voic.exchange_amt"       , "0");
  if (getValue("order_flag").equals("Y"))
     {
      setValueInt("voic.exchange_point"   , (int)getValueInt("data.exchg_pt")*exchgCnt*-1);
      if (getValueInt("data.exchg_amt")>0)
         setValueInt("voic.exchange_amt"     , getValueInt("data.exchg_amt")*exchgCnt); 
     }
      
  setValue("voic.authorization"      , authCode);
  setValue("voic.exchg_seqno"        , "0");
  setValue("voic.web_rrn"            , checkKey);
  setValue("voic.order_flag"         , getValue("order_flag"));
  setValue("voic.tran_seqno"         , tranSeqno);
  setValue("voic.mod_time"           , sysDate+sysTime);
  setValue("voic.mod_pgm"            , modPgm);

  daoTable  = "mkt_voice";

  int n = insertTable();

  if (n==0) return(1);

  return(0);
 }
// ************************************************************************
/*
  4碼  4碼錯誤碼 訊息說明           備註
===============================================
  0000   00      紅利兌換成功
V 7001   01      沒有卡號
V 7002   02      沒有商品代號
V 7003   03      兌換數量為0
V 7021   21      卡號不存在
V 7022   22      卡片必須為正卡
V 7023   23      卡號非有效卡
V 7011   11      商品代號錯誤
V 7012   12      航空里程商品只能人工兌換
V 7013   13      商品已下架
V 7014   14      商品已無數量可兌換
V 7015   15      商品未提供該卡片兌換
V 7016   16      語音不可有自付額兌換
V 7031   31      無紅利點數可兌換
V 7032   32      紅利點數不足
V 7041   41      必須有手機號碼
  7042   42      授權失敗
  9999   51      系統錯誤 select_crd_idno error
  9999   52      系統錯誤 select_act_acno error
  9999   53      系統錯誤 select_ptr_businday error
  9999   54      系統錯誤 insert_mkt_gift_vouch("3")
  9999   55      系統錯誤 insert_mkt_gift_bpexchg
  9999   56      系統錯誤 insert_mkt_bonus_dtl
  9999   57      系統錯誤 select_vmkt_fund_name
  9999   58      系統錯誤 select_fund_parm
  9999   59      系統錯誤 select_ptr_workday
  9999   60      系統錯誤 insert_mkt_cashback_dtl
  9999   61      系統錯誤 insert_cyc_fund_dtl
  9999   62      系統錯誤 insert_mkt_gift_vouch("0")
  9999   63      系統錯誤 update_mkt_gift_e
  9999   64      系統錯誤 update_mkt_gift
  9999   65      系統錯誤 insert_bil_sysexp   
V 9999   99      來源或啟動程式代碼為空值update_mkt_gift
*/

}   // End of class CommExchg
