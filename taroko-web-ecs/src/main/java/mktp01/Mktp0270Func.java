/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/11/15  V1.00.07   Allen Ho      Initial                              *
 * 111/11/28  V1.00.08  jiangyigndong  updated for project coding standard  *
 * 113/01/02  V1.00.09  Ryan           修正DBA_ACAJ.dr_amt,cyc_fund_dtl.fund_amt,dbm_bonus_dtl .tran_pgm  *                                                                   *
 ***************************************************************************/
package mktp01;

import busi.DataSet;
import busi.SqlPrepare;
import busi.ecs.CommFunction;
import busi.ecs.CommRoutine;
import busi.ecs.DbmBonus;
import busi.ecs.MktBonus;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0270Func extends busi.FuncProc
{
  private final String PROGNAME = "紅利積點兌換贈品登錄檔覆核處理程式110/11/15 V1.00.01";
  String kk1,kk2,kk3,kk4,kk5,kk6,kk7;
  String approveTabName = "mkt_gift_bpexchg";
  String controlTabName = "mkt_gift_bpexchg_t";
  String cardTable = "";
  String vdFlag = "";
  String cardNote = "";
  String crdCardNo = "";
  String crdPSeqno = "";
  String dbcCardNo = "";
  String dbcPSeqno = "";
  boolean insertFlag = false;
  public Mktp0270Func(TarokoCommon wr)
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
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 1;
  }
  // ************************************************************************
  @Override
  public void dataCheck() {
  }
  // ************************************************************************
  @Override
  public int dataProc() {
    return rc;
  }
  // ************************************************************************
  public int dbInsertA4() throws Exception
  {
    rc = dbSelectS4();
    if (rc!=1) return rc;
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);

    busi.ecs.MktBonus comb = new busi.ecs.MktBonus();
    comb.setConn(wp);
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

    if(getCountDp()!=1)
		  return -1;
    
    double endTranBp = colNum("end_tran_bp");
    double totalPt = colNum("exchg_pt") * colNum("exchg_cnt");
    if (colStr("air_type").length()==0)
      colSet("status_flag"   ,"2");
    else
      colSet("status_flag"   ,"3");

    colSet("exg_apr_date",wp.sysDate);
    colSet("exg_apr_user",wp.loginUser);
    if (colStr("air_type").length()>0)
    {
      colSet("air_apr_date",wp.sysDate);
      colSet("air_apr_user",wp.loginUser);
    }
    else
    {
      colSet("air_apr_date","");
      colSet("air_apr_user","");
    }
    if (endTranBp < totalPt)
    {
      colSet("exchg_mark","[紅利類別："+colStr("bonus_type")+"][餘額："+String.format("%.0f",endTranBp)
              +"]小於兌換金額"+totalPt+"]!");
      colSet("proc_flag"   , "Y");
      colSet("status_flag" , "1");
      colSet("deduct_flag" , "E");
      colSet("deduct_date" , "");
    }
    else
    {
      colSet("status_flag" , "2");
      colSet("deduct_date" ,"");
      colSet("deduct_flag" ,"N");
      colSet("beg_tran_bp",String.format("%.0f",totalPt*-1));
      selectMktGift();
      colSet("tax_flag","");
      colSet("mod_reason","");
      colSet("effect_e_date","");
      colSet("active_name","紅利積點兌換贈品(人工)");
      colSet("mod_desc","兌換贈品["+colStr("gift_no")+"] 共 ["+String.format("%.0f",colNum("exchg_cnt"))+"]件");
      if (colStr("air_type").length()>0)
      {
        colSet("status_flag" , "3");
        colSet("deduct_flag" ,"R");
        colSet("beg_tran_bp",0);
        colSet("end_tran_bp",0);
        colSet("mod_memo",colStr("gift_name")+",待航空公司確認後才解圈扣點");
        colSet("tran_code","0");
        colSet("res_tran_bp",String.format("%.0f",totalPt*-1));
        colSet("res_e_date","");
      }
      else
      {
        colSet("deduct_flag" ,"Y");
        colSet("deduct_date" ,wp.sysDate);
        colSet("end_tran_bp",String.format("%.0f",totalPt*-1));
        colSet("mod_memo",colStr("gift_name"));
        colSet("tran_code","4");
        colSet("res_tran_bp","0");
        colSet("res_e_date","");
        if (!colStr("gift_type").equals("2"))
          insertMktGiftVouch("1",colStr("tran_seqno"),(int)(colNum("cash_value")*colNum("exchg_cnt")));
      }
      colSet("dtl_tran_date",colStr("tran_date"));
      colSet("dtl_tran_time",colStr("tran_time"));
      
      if("1".equals(colStr("gift_type")) || "2".equals(colStr("gift_type")) || "3".equals(colStr("gift_type")) ) {
    	  String vdtranSeqno = "";
    	  String tranSeqno = "";
    	  if(colInt("deduct_bp01")<0) {
    		  tranSeqno = comr.getSeqno("MKT_MODSEQ");
    	  	  insertMktBonusDtl(tranSeqno);
    	  	  colSet("tran_seqno",tranSeqno);
    	  }
    	  if(colInt("deduct_bp90")<0) {
    		  vdtranSeqno = comr.getSeqno("ECS_DBMSEQ");
    	  	  insertDbmBonusDtl(vdtranSeqno);
    	  	  colSet("vdtran_seqno",vdtranSeqno);
    	  }
    	  
    	  if(colInt("deduct_bp90")<0 && colInt("deduct_bp01") == 0) {
    		  insertDbaAcaj();
    		  insertCycFundDtl();
    	  }
      }

      if (colStr("air_type").length()==0)
      {
        if (!colStr("gift_type").equals("2"))
        {
          if (colStr("gift_type").equals("3"))
            updateMktGiftE();
          else
            updateMktGiftENo();
        }
        comb.bonusFunc(colStr("tran_seqno"));
      }

      if ((colStr("gift_type").equals("2"))&&
              (colStr("fund_code").length()>0))
      {
        colSet("tran_code","4");
        selectVmktFundName();
        getEffectMonths();
        if (colNum("effect_months")>0)
          colSet("effect_e_date",comm.nextMonthDate(wp.sysDate,(int)colNum("effect_months")));
        colSet("beg_tran_amt",String.format("%.0f",colNum("cash_value")*colNum("exchg_cnt")));
        colSet("mod_desc","兌換贈品["+colStr("gift_no")+"] 共 ["+String.format("%.0f",colNum("exchg_cnt"))+"]件");
        colSet("mod_reason","");
        if(colInt("deduct_bp01")<0) {
        	 insertMktCashbackDtl(colStr("tran_seqno"));
        }
     

        busi.ecs.MktCashback comc = new busi.ecs.MktCashback();
        comc.setConn(wp);
        comc.cashbackFunc(colStr("tran_seqno"));
        if(colInt("deduct_bp01")<0) {
//        	 insertCycFundDtl(0);
        }
        colSet("proc_flag"     , "B");
        colSet("status_flag"   ,"7");

        colSet("out_date"      , wp.sysDate);
        colSet("out_apr_date"  , wp.sysDate);
        colSet("out_apr_user"  , wp.loginUser);

        colSet("pay_date"      , wp.sysDate);
        colSet("pay_apr_date"  , wp.sysDate);
        colSet("pay_gl_date"  , wp.sysDate);
        colSet("pay_apr_user"  , wp.loginUser);
//          insert_mkt_gift_vouch("3",colStr("tran_seqno"),(int)(colNum("cash_value")*colNum("exchg_cnt")));
      }
    }
    colSet("end_tran_bp",String.format("%.0f",endTranBp));
    strSql= " insert into  " + approveTabName + " ("
            + " card_no, "
            + " gift_no, "
            + " vendor_no, "
            + " bonus_type, "
            + " tran_seqno, "
            + " proc_flag, "
            + " gift_type, "
            + " cash_value, "
            + " total_pt, "
            + " exchg_pt, "
            + " exchg_amt, "
            + " end_tran_bp, "
            + " end_tran_bp01, "
            + " end_tran_bp90, "
            + " deduct_bp01, "
            + " deduct_bp90, "
            + " fund_code, "
            + " tran_date, "
            + " from_mark, "
            + " exchg_cnt, "
            + " exchg_mark, "
            + " exg_gl_date, "
            + " exg_apr_user, "
            + " exg_apr_date, "
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
            + " ecoupon_bno, "
            + " ecoupon_date_s, "
            + " ecoupon_date_e, "
            + " ecoupon_date, "
            + " ecoupon_ret_date, "
            + " ecoupon_gl_date, "
            + " sms_flag, "
            + " sms_date, "
            + " unpay_cnt, "
            + " sms_resend_desc, "
            + " air_type, "
            + " cal_mile, "
            + " cal_dfpno, "
            + " passport_name, "
            + " passport_surname, "
            + " send_date, "
            + " send_time, "
            + " recv_date, "
            + " recv_time, "
            + " air_birthday, "
            + " air_id_no, "
            + " air_apr_user, "
            + " air_apr_date, "
            + " rjct_proc_code, "
            + " rjct_proc_remark, "
            + " out_date, "
            + " out_batchno, "
            + " register_no, "
            + " out_mark, "
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
            + " tran_time, "
            + " acct_date, "
            + " deduct_flag, "
            + " deduct_date, "
            + " p_seqno, "
            + " id_p_seqno, "
            + " acct_type, "
            + " status_flag, "
            + " ecoupon_status, "
            + " vdtran_seqno, "
            + " apr_flag, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " mod_time, "
            + " mod_user, "
            + " mod_seqno, "
            + " mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
            + "'Y',"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "?,"
            + " timestamp_format(?,'yyyymmddhh24miss'), "
            + "?,"
            + "?,"
            + " ?) ";

    Object[] param =new Object[]
            {
                    colStr("card_no"),
                    colStr("gift_no"),
                    colStr("vendor_no"),
                    colStr("bonus_type"),
                    colStr("tran_seqno"),
                    colStr("proc_flag"),
                    colStr("gift_type"),
                    colStr("cash_value"),
                    colStr("total_pt"),
                    colStr("exchg_pt"),
                    colStr("exchg_amt"),
                    colInt("end_tran_bp01") + colInt("end_tran_bp90"),
                    colInt("end_tran_bp01"),
                    colInt("end_tran_bp90"),
                    colInt("deduct_bp01"),
                    colInt("deduct_bp90"),
                    colStr("fund_code"),
                    colStr("tran_date"),
                    colStr("from_mark"),
                    colStr("exchg_cnt"),
                    colStr("exchg_mark"),
                    colStr("exg_gl_date"),
                    colStr("exg_apr_user"),
                    colStr("exg_apr_date"),
                    colStr("chi_name"),
                    colStr("cellar_phone"),
                    colStr("home_area_code1"),
                    colStr("home_tel_no1"),
                    colStr("home_tel_ext1"),
                    colStr("office_area_code1"),
                    colStr("office_tel_no1"),
                    colStr("office_tel_ext1"),
                    colStr("bill_sending_zip"),
                    colStr("bill_sending_addr1"),
                    colStr("bill_sending_addr2"),
                    colStr("bill_sending_addr3"),
                    colStr("bill_sending_addr4"),
                    colStr("bill_sending_addr5"),
                    colStr("ecoupon_bno"),
                    colStr("ecoupon_date_s"),
                    colStr("ecoupon_date_e"),
                    colStr("ecoupon_date"),
                    colStr("ecoupon_ret_date"),
                    colStr("ecoupon_gl_date"),
                    colStr("sms_flag"),
                    colStr("sms_date"),
                    colStr("unpay_cnt"),
                    colStr("sms_resend_desc"),
                    colStr("air_type"),
                    colStr("cal_mile"),
                    colStr("cal_dfpno"),
                    colStr("passport_name"),
                    colStr("passport_surname"),
                    colStr("send_date"),
                    colStr("send_time"),
                    colStr("recv_date"),
                    colStr("recv_time"),
                    colStr("air_birthday"),
                    colStr("air_id_no"),
                    colStr("air_apr_user"),
                    colStr("air_apr_date"),
                    colStr("rjct_proc_code"),
                    colStr("rjct_proc_remark"),
                    colStr("out_date"),
                    colStr("out_batchno"),
                    colStr("register_no"),
                    colStr("out_mark"),
                    colStr("out_apr_user"),
                    colStr("out_apr_date"),
                    colStr("pay_date"),
                    colStr("pay_gl_date"),
                    colStr("pay_apr_user"),
                    colStr("pay_apr_date"),
                    colStr("return_date"),
                    colStr("return_reason"),
                    colStr("return_mark"),
                    colStr("ret_apr_user"),
                    colStr("ret_apr_date"),
                    colStr("ret_gl_date"),
                    colStr("tran_time"),
                    comr.getBusinDate(),
                    colStr("deduct_flag"),
                    colStr("deduct_date"),
                    colStr("p_seqno"),
                    colStr("id_p_seqno"),
                    colStr("acct_type"),
                    colStr("status_flag"),
                    colStr("ecoupon_status"),
                    colStr("vdtran_seqno"),
                    wp.loginUser,
                    colStr("crt_date"),
                    colStr("crt_user"),
                    wp.sysDate + wp.sysTime,
                    wp.loginUser,
                    colStr("mod_seqno"),
                    wp.modPgm()
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("新增資料 "+ controlTabName +" 失敗");

    return rc;
  }
  // ************************************************************************
  public int dbSelectS4() throws Exception
  {
    String procTabName="";
    procTabName = controlTabName;
    strSql= " select "
            + " card_no, "
            + " gift_no, "
            + " vendor_no, "
            + " bonus_type, "
            + " tran_seqno, "
            + " proc_flag, "
            + " gift_type, "
            + " cash_value, "
            + " total_pt, "
            + " exchg_pt, "
            + " exchg_amt, "
            + " end_tran_bp, "
            + " end_tran_bp01, "
            + " end_tran_bp90, "
            + " deduct_bp01, "
            + " deduct_bp90, "
            + " vdtran_seqno, "
            + " fund_code, "
            + " tran_date, "
            + " from_mark, "
            + " exchg_cnt, "
            + " exchg_mark, "
            + " exg_gl_date, "
            + " exg_apr_user, "
            + " exg_apr_date, "
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
            + " ecoupon_bno, "
            + " ecoupon_date_s, "
            + " ecoupon_date_e, "
            + " ecoupon_date, "
            + " ecoupon_ret_date, "
            + " ecoupon_gl_date, "
            + " sms_flag, "
            + " sms_date, "
            + " unpay_cnt, "
            + " sms_resend_desc, "
            + " air_type, "
            + " cal_mile, "
            + " cal_dfpno, "
            + " passport_name, "
            + " passport_surname, "
            + " send_date, "
            + " send_time, "
            + " recv_date, "
            + " recv_time, "
            + " air_birthday, "
            + " air_id_no, "
            + " air_apr_user, "
            + " air_apr_date, "
            + " rjct_proc_code, "
            + " rjct_proc_remark, "
            + " out_date, "
            + " out_batchno, "
            + " register_no, "
            + " out_mark, "
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
            + " tran_time, "
            + " acct_date, "
            + " deduct_flag, "
            + " deduct_date, "
            + " p_seqno, "
            + " id_p_seqno, "
            + " acct_type, "
            + " status_flag, "
            + " ecoupon_status, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
            + " from " + procTabName
            + " where rowid = ? ";

    Object[] param =new Object[]
            {
                    wp.itemRowId("wprowid")
            };

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0) rc=0;else rc=1;
    if (rc!=1) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

    return rc;
  }
  // ************************************************************************
  public int dbUpdateU4() throws Exception
  {
    rc = dbSelectS4();
    if (rc!=1) return rc;
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);

    if(getCountDp()!=1)
		  return -1;
    String vouchType ="";
    if (colStr("proc_flag").equals("A"))
    {
      if (colStr("rjct_proc_code").equals("C"))
      {
        deleteMktBonusDtl();
        colSet("deduct_flag" , "C");
        colSet("status_flag" , "5");
        colSet("return_mark"  , colStr("rjct_proc_remark"));
        colSet("return_date"  , comr.getBusinDate());
        colSet("ret_apr_date" , comr.getBusinDate());
        colSet("ret_apr_user" , wp.loginUser);
        
        if(!"C".equals(colStr("vdtran_seqno"))) {
        	deleteDbmBonusDtl();
        }
      }
      else if (colStr("rjct_proc_code").equals("2"))
      {
        colSet("send_date" , "");
        colSet("send_time" , "");
        colSet("recv_date" , "");
        colSet("recv_time" , "");
      }
      colSet("air_apr_date" , comr.getBusinDate());
      colSet("air_apr_user" , wp.loginUser);
    }
    else
    if (colStr("proc_flag").equals("B"))
    {
      colSet("out_apr_date" , comr.getBusinDate());
      colSet("out_apr_user" , wp.loginUser);
      colSet("status_flag"  , "7");
      if (colStr("air_type").length()==0)
      {
        colSet("pay_date"     , comr.getBusinDate());
        colSet("pay_apr_date" , comr.getBusinDate());
        colSet("pay_apr_user" , wp.loginUser);
        insertMktGiftVouch("3",colStr("tran_seqno"),(int)(colNum("cash_value")*colNum("exchg_cnt")));
      }
      else
      {
        if (colStr("return_date").length()!=0)
          insertMktGiftVouch("6",colStr("tran_seqno"),(int)(colNum("cash_value")*colNum("exchg_cnt")));
        else
          insertMktGiftVouch("3",colStr("tran_seqno"),(int)(colNum("cash_value")*colNum("exchg_cnt")));
      }
    }
    else if (colStr("proc_flag").equals("C"))
    {
      colSet("ret_apr_date" , comr.getBusinDate());
      colSet("ret_apr_user" , wp.loginUser);
      if (colStr("deduct_flag").equals("Y"))
      {
        selectMktBonusBklog();
        if(empty(colStr("vdtran_seqno")) == false) {
        	selectDbmBonusBklog();
        }
        if ((colStr("fund_code").length()!=0)||
                (colStr("gift_type").equals("2")))
        {
          colSet("tran_code","7");
          selectVmktFundName();
          colSet("effect_e_date","");
          colSet("beg_tran_amt",String.format("%.0f",colNum("cash_value")*colNum("exchg_cnt")*-1));
          colSet("active_name"   , "紅利積點兌換贈品(退貨)");
          colSet("mod_desc","交易序號"+colStr("tran_seqno")+"] 退貨日期["+colStr("return_date")
                  +"]  兌換贈品["+colStr("gift_no")+"] 共 ["
                  +String.format("%.0f",colNum("exchg_cnt"))+"]件");


          String tranSeqno = comr.getSeqno("MKT_MODSEQ");
          if(colInt("deduct_bp01")<0) {
        	  insertMktCashbackDtl(tranSeqno);
          }
        

          busi.ecs.MktCashback comc = new busi.ecs.MktCashback();
          comc.setConn(wp);
          comc.cashbackFunc(tranSeqno);
          if(colInt("deduct_bp01")<0) {
//        	   insertCycFundDtl(1);
          }
        }
        else
        {
          if (colStr("pay_apr_date").length()==0)
            vouchType="2";
          else
            vouchType="5";

          if (colStr("gift_type").equals("1"))
          {
            insertMktGiftVouch(vouchType,colStr("tran_seqno"),(int)(colNum("cash_value")*colNum("exchg_cnt")));
          }
          else
          {
            if (colStr("out_apr_date").length()!=0)
              updateMktGiftEcouponOut(colStr("tran_seqno"));

            insertMktGiftVouch(vouchType,colStr("tran_seqno"),(int)(colNum("cash_value")*colNum("exchg_cnt")));
            if (colStr("ecoupon_date").length()!=0)
            {
              selectMktGift();
              selectMktGiftEcoupon();
            }
            else
            {
              updateMktGiftReEcoupon();
            }
          }
        }
      }
      else
      {
        if (colStr("gift_type").equals("3"))
          updateMktGiftReEcoupon();
        else
          updateMktGiftReEcouponNo();
      }
    }
    else if (colStr("proc_flag").equals("E"))
    {
      colSet("pay_apr_date" , comr.getBusinDate());
      colSet("pay_apr_user" , wp.loginUser);
      insertMktGiftVouch("7",colStr("tran_seqno"),(int)(colNum("cash_value")*colNum("exchg_cnt")));
    }
    colSet("apr_date" , comr.getBusinDate());
    colSet("apr_user" , wp.loginUser);
    String aprFlag = "Y";
    strSql= "update " + approveTabName + " set "
            + "proc_flag = ?, "
            + "gift_type = ?, "
            + "cash_value = ?, "
            + "total_pt = ?, "
            + "exchg_pt = ?, "
            + "exchg_amt = ?, "
            + "end_tran_bp = ?, "
            + "end_tran_bp01 = ?, "
            + "end_tran_bp90 = ?, "
            + "deduct_bp01 = ?, "
            + "deduct_bp90 = ?, "
            + "fund_code = ?, "
            + "tran_date = ?, "
            + "from_mark = ?, "
            + "exchg_cnt = ?, "
            + "exchg_mark = ?, "
            + "exg_gl_date = ?, "
            + "exg_apr_user = ?, "
            + "exg_apr_date = ?, "
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
            + "ecoupon_bno = ?, "
            + "ecoupon_date_s = ?, "
            + "ecoupon_date_e = ?, "
            + "ecoupon_date = ?, "
            + "ecoupon_ret_date = ?, "
            + "ecoupon_gl_date = ?, "
            + "sms_flag = ?, "
            + "sms_date = ?, "
            + "unpay_cnt = ?, "
            + "sms_resend_desc = ?, "
            + "air_type = ?, "
            + "cal_mile = ?, "
            + "cal_dfpno = ?, "
            + "passport_name = ?, "
            + "passport_surname = ?, "
            + "send_date = ?, "
            + "send_time = ?, "
            + "recv_date = ?, "
            + "recv_time = ?, "
            + "air_birthday = ?, "
            + "air_id_no = ?, "
            + "air_apr_user = ?, "
            + "air_apr_date = ?, "
            + "rjct_proc_code = ?, "
            + "rjct_proc_remark = ?, "
            + "out_date = ?, "
            + "out_batchno = ?, "
            + "register_no = ?, "
            + "out_mark = ?, "
            + "out_apr_user = ?, "
            + "out_apr_date = ?, "
            + "pay_date = ?, "
            + "pay_gl_date = ?, "
            + "pay_apr_user = ?, "
            + "pay_apr_date = ?, "
            + "return_date = ?, "
            + "return_reason = ?, "
            + "return_mark = ?, "
            + "ret_apr_user = ?, "
            + "ret_apr_date = ?, "
            + "ret_gl_date = ?, "
            + "status_flag = ?, "
            + "deduct_flag = ?, "
            + "crt_user  = ?, "
            + "crt_date  = ?, "
            + "apr_user  = ?, "
            + "apr_date  = to_char(sysdate,'yyyymmdd'), "
            + "apr_flag  = ?, "
            + "mod_user  = ?, "
            + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "mod_pgm   = ?, "
            + "mod_seqno = nvl(mod_seqno,0)+1 "
            + "where 1     = 1 "
            + "and   card_no  = ? "
            + "and   gift_no  = ? "
            + "and   vendor_no  = ? "
            + "and   tran_seqno  = ? "
    ;

    Object[] param =new Object[]
            {
                    colStr("proc_flag"),
                    colStr("gift_type"),
                    colStr("cash_value"),
                    colStr("total_pt"),
                    colStr("exchg_pt"),
                    colStr("exchg_amt"),
                    colInt("end_tran_bp01") + colInt("end_tran_bp90"),
                    colInt("end_tran_bp01"),
                    colInt("end_tran_bp90"),
                    colInt("deduct_bp01"),
                    colInt("deduct_bp90"),
                    colStr("fund_code"),
                    colStr("tran_date"),
                    colStr("from_mark"),
                    colStr("exchg_cnt"),
                    colStr("exchg_mark"),
                    colStr("exg_gl_date"),
                    colStr("exg_apr_user"),
                    colStr("exg_apr_date"),
                    colStr("chi_name"),
                    colStr("cellar_phone"),
                    colStr("home_area_code1"),
                    colStr("home_tel_no1"),
                    colStr("home_tel_ext1"),
                    colStr("office_area_code1"),
                    colStr("office_tel_no1"),
                    colStr("office_tel_ext1"),
                    colStr("bill_sending_zip"),
                    colStr("bill_sending_addr1"),
                    colStr("bill_sending_addr2"),
                    colStr("bill_sending_addr3"),
                    colStr("bill_sending_addr4"),
                    colStr("bill_sending_addr5"),
                    colStr("ecoupon_bno"),
                    colStr("ecoupon_date_s"),
                    colStr("ecoupon_date_e"),
                    colStr("ecoupon_date"),
                    colStr("ecoupon_ret_date"),
                    colStr("ecoupon_gl_date"),
                    colStr("sms_flag"),
                    colStr("sms_date"),
                    colStr("unpay_cnt"),
                    colStr("sms_resend_desc"),
                    colStr("air_type"),
                    colStr("cal_mile"),
                    colStr("cal_dfpno"),
                    colStr("passport_name"),
                    colStr("passport_surname"),
                    colStr("send_date"),
                    colStr("send_time"),
                    colStr("recv_date"),
                    colStr("recv_time"),
                    colStr("air_birthday"),
                    colStr("air_id_no"),
                    colStr("air_apr_user"),
                    colStr("air_apr_date"),
                    colStr("rjct_proc_code"),
                    colStr("rjct_proc_remark"),
                    colStr("out_date"),
                    colStr("out_batchno"),
                    colStr("register_no"),
                    colStr("out_mark"),
                    colStr("out_apr_user"),
                    colStr("out_apr_date"),
                    colStr("pay_date"),
                    colStr("pay_gl_date"),
                    colStr("pay_apr_user"),
                    colStr("pay_apr_date"),
                    colStr("return_date"),
                    colStr("return_reason"),
                    colStr("return_mark"),
                    colStr("ret_apr_user"),
                    colStr("ret_apr_date"),
                    colStr("ret_gl_date"),
                    colStr("status_flag"),
                    colStr("deduct_flag"),
                    colStr("crt_user"),
                    colStr("crt_date"),
                    wp.loginUser,
                    aprFlag,
                    colStr("mod_user"),
                    colStr("mod_time"),
                    colStr("mod_pgm"),
                    colStr("card_no"),
                    colStr("gift_no"),
                    colStr("vendor_no"),
                    colStr("tran_seqno")
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) rc=0;else rc=1;

    return rc;
  }
  // ************************************************************************
  public int dbUpdateMktUploadfileCtlProcFlag(String transSeqno) throws Exception
  {
    strSql= "update mkt_uploadfile_ctl set "
            + " proc_flag = 'Y', "
            + " proc_date = to_char(sysdate,'yyyymmdd') "
            + "where trans_seqno = ?";

    Object[] param =new Object[]
            {
                    transSeqno
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("更新 mkt_uploadfile_ctl 錯誤");

    return 1;
  }
  // ************************************************************************
  public int dbDeleteD4() throws Exception
  {
    rc = dbSelectS4();
    if (rc!=1) return rc;
    strSql = "delete " + approveTabName + " "
            + "where 1 = 1 "
            + "and card_no = ? "
            + "and gift_no = ? "
            + "and vendor_no = ? "
            + "and tran_seqno = ? "
    ;

    Object[] param =new Object[]
            {
                    colStr("card_no"),
                    colStr("gift_no"),
                    colStr("vendor_no"),
                    colStr("tran_seqno")
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) rc=0;else rc=1;
    if (rc!=1) errmsg("刪除 "+ approveTabName +" 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbDelete() throws Exception
  {
    strSql = "delete " + controlTabName + " "
            + "where rowid = ?";

    Object[] param =new Object[]
            {
                    wp.itemRowId("wprowid")
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
  // ************************************************************************
  public int insertMktBonusDtl(String tranSeqno) throws Exception
  {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);

    strSql= " insert into mkt_bonus_dtl("
            + " acct_type, "
            + " bonus_type, "
            + " active_code, "
            + " active_name, "
            + " tran_code, "
            + " beg_tran_bp, "
            + " end_tran_bp, "
            + " res_e_date, "
            + " res_tran_bp, "
            + " tax_flag, "
            + " effect_e_date, "
            + " mod_desc, "
            + " mod_reason, "
            + " mod_memo, "
            + " tran_date, "
            + " tran_time, "
            + " p_seqno, "
            + " id_p_seqno, "
            + " tran_pgm, "
            + " tran_seqno, "
            + " proc_month, "
            + " acct_date, "
            + " apr_flag, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " mod_time,mod_user,mod_pgm,mod_seqno "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"     // last: tran_time
            + "?,?,?,?,?,?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "?,"
            + "timestamp_format(?,'yyyymmddhh24miss'),?,?,?)";

    Object[] param =new Object[]
            {
                    "01",
                    colStr("bonus_type"),
                    "",                              // colStr("active_code"),
                    colStr("active_name"),           // colStr("active_name"),
                    colStr("tran_code"),
                    colInt("deduct_bp01"),
                    colInt("deduct_bp01"),
                    colStr("res_e_date"),
                    colNum("res_tran_bp"),
                    colStr("tax_flag"),       // colStr("tax_flag"),
                    colStr("effect_e_date"),                     // colStr("effect_e_date"),
                    colStr("mod_desc"),                          // mod_desc
                    colStr("mod_reason"),                        // mod_reason
                    colStr("mod_memo"),                          // mod_memo
                    colStr("dtl_tran_date"),
                    colStr("dtl_tran_time"),
                    crdPSeqno,
                    colStr("id_p_seqno"),
                    wp.modPgm(),
                    tranSeqno,
                    comr.getBusinDate().substring(0,6),
                    comr.getBusinDate(),
                    "Y",
                    wp.loginUser,
                    colStr("crt_date"),
                    colStr("crt_user"),
                    wp.sysDate + wp.sysTime,
                    wp.loginUser,
                    wp.modPgm(),
                    colStr("mod_seqno")
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg(sqlErrtext);
    insertFlag = true;
    return(1);
  }
  // ************************************************************************
  public int insertDbmBonusDtl(String tranSeqno) throws Exception
  {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    String businDate = comr.getBusinDate();
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("dbm_bonus_dtl");
    sp.ppstr("tran_seqno", tranSeqno);
    sp.ppstr("acct_type", "90");
    sp.ppstr("active_code", "");
    sp.ppstr("active_name", colStr("active_name"));
    sp.ppstr("tran_code", colStr("tran_code"));
    sp.ppint("beg_tran_bp", colInt("deduct_bp90"));
    sp.ppstr("tax_flag", colStr("tax_flag"));
    sp.ppstr("effect_e_date", colStr("effect_e_date"));
    sp.ppstr("mod_reason", colStr("mod_reason"));
    sp.ppstr("mod_desc", colStr("mod_desc"));
    sp.ppstr("mod_memo", colStr("mod_memo"));
    sp.ppstr("tran_date", wp.sysDate);
    sp.ppstr("tran_time", wp.sysTime);
    sp.ppstr("id_p_seqno", colStr("id_p_seqno"));
    sp.ppstr("tran_pgm", wp.modPgm());
    sp.ppint("end_tran_bp", colInt("deduct_bp90"));
    sp.ppstr("acct_date", businDate);
    sp.ppstr("acct_month", strMid(businDate,0,6));
    sp.ppstr("bonus_type", "BONU");
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("apr_date", wp.sysDate);
    sp.ppstr("apr_user", wp.loginUser);
    sp.ppstr("crt_date", colStr("crt_date"));
    sp.ppstr("crt_user", colStr("crt_user"));
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppint("mod_seqno", colInt("mod_seqno"));
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppstr("card_no", dbcCardNo);
    sp.ppstr("p_seqno", dbcPSeqno);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return(1);
  }
  // ************************************************************************
  public int insertCycFundDtl() throws Exception
  {

    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);

//    if (cd_type==0) colSet("cd_kind","A-36");
//    else colSet("cd_kind","A393");

    strSql= " insert into cyc_fund_dtl ("
            + " business_date, "
            + " curr_code, "
            + " create_date, "
            + " create_time, "
            + " id_p_seqno, "
            + " p_seqno, "
            + " acct_type, "
            + " card_no, "
            + " fund_code, "
            + " tran_code, "
            + " vouch_type, "
            + " cd_kind, "
            + " memo1_type, "
            + " fund_amt, "
            + " other_amt, "
            + " proc_flag, "
            + " proc_date, "
            + " execute_date, "
            + " fund_cnt, "
            + " mod_time, "
            + " mod_user, "
            + " mod_seqno, "
            + " mod_pgm "
            + " ) values ("
            + "?,?,to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),"
            + "?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,"
            + " timestamp_format(?,'yyyymmddhh24miss'), "
            + "?,"
            + "?,"
            + " ?) ";

    Object[] param =new Object[]
            {
                    comr.getBusinDate(),
                    "901",
                    colStr("id_p_seqno"),
                    dbcPSeqno,
                    "90",
                    dbcCardNo,
                    colStr("fund_code"),
                    "1",
                    "3",
//                    "H001",
                    "J003",
                    "1",
                    Math.abs(colNum("cash_value")*colNum("exchg_cnt")),
                    0,
                    "N",
                    "",
                    comr.getBusinDate(),
                    1,
                    wp.sysDate + wp.sysTime,
                    wp.loginUser,
                    colStr("mod_seqno"),
                    wp.modPgm()
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {errmsg(sqlErrtext);return(0);}

    return(1);
  }
  // ************************************************************************
  public int insertMktCashbackDtl(String tranSeqno) throws Exception
  {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    selectPtrFundp();
    
    strSql= " insert into mkt_cashback_dtl ("
            + " fund_code, "
            + " fund_name, "
            + " acct_type, "
            + " tran_code, "
            + " beg_tran_amt, "
            + " end_tran_amt, "
            + " effect_e_date, "
            + " mod_desc, "
            + " mod_reason, "
            + " mod_memo, "
            + " tran_date, "
            + " tran_time, "
            + " p_seqno, "
            + " id_p_seqno, "
            + " tran_pgm, "
            + " tran_seqno, "
            + " proc_month, "
            + " acct_date, "
            + " acct_month, "
            + " apr_flag, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " mod_time, "
            + " mod_user, "
            + " mod_seqno, "
            + " mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,"
            + "to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),?,?,?,?,?,?,?,"
            + "?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,?,?,"   // cpr_user rt_Date crt_user
            + " timestamp_format(?,'yyyymmddhh24miss'), "
            + "?,"
            + "?,"
            + " ?) ";

    Object[] param =new Object[]
            {
                    colStr("fund_code"),
                    colStr("fund_name"),
                    "01",
                    "1",
                    colNum("beg_tran_amt"),
                    colNum("beg_tran_amt"),
                    colStr("effect_e_date"),
                    colStr("mod_desc"),
                    colStr("mod_reason"),
                    colStr("mod_memo"),
                    crdPSeqno,
                    colStr("id_p_seqno"),
                    wp.modPgm(),
                    tranSeqno,
                    comr.getBusinDate().substring(0,6),
                    comr.getBusinDate(),
                    comr.getBusinDate().substring(0,6),
                    "Y",
                    wp.loginUser,
                    colStr("crt_date"),
                    colStr("crt_user"),
                    wp.sysDate + wp.sysTime,
                    wp.loginUser,
                    colStr("mod_seqno"),
                    wp.modPgm()
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {errmsg(sqlErrtext);return(0);}

    return(1);
  }
  
  /***
   * 全扣visa debit點數 ,沒有扣信用卡點數 ,需insert dba_acaj 存入回饋金額
   * @return
   * @throws Exception
   */
  public int insertDbaAcaj() throws Exception
  {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    
    String businDate = comr.getBusinDate();
    
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("dba_acaj");
    sp.ppstr("crt_date", businDate);
    sp.ppstr("crt_time", wp.sysTime);
    sp.ppstr("p_seqno", dbcPSeqno);
    sp.ppstr("acct_type", "90");
    sp.ppstr("adjust_type", "FD10");
    sp.ppstr("reference_no", "");
    sp.ppstr("post_date", businDate);
    sp.ppnum("orginal_amt", 0);
    sp.ppnum("dr_amt", Math.abs(colNum("cash_value")*colNum("exchg_cnt")));
    sp.ppnum("cr_amt", 0);
    sp.ppnum("bef_amt", 0);
    sp.ppnum("aft_amt", 0);
    sp.ppnum("bef_d_amt", 0);
    sp.ppnum("aft_d_amt", 0);
    sp.ppstr("acct_code", "");
    sp.ppstr("func_code", "U");
    sp.ppstr("card_no", dbcCardNo);
    sp.ppstr("cash_type", "");
    sp.ppstr("value_type", "1");
    sp.ppstr("trans_acct_type", "");
    sp.ppstr("trans_acct_key", "");
    sp.ppstr("interest_date", businDate);
    sp.ppstr("adj_reason_code", "");
    sp.ppstr("adj_comment", "紅利點數兌換刷卡金");
    sp.ppstr("c_debt_key", "");
    sp.ppstr("debit_item", "");
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("jrnl_date", "");
    sp.ppstr("jrnl_time", "");
    sp.ppstr("payment_type", "");
    sp.ppstr("chg_date", businDate);
    sp.ppstr("chg_user", wp.modPgm());
    sp.ppstr("acct_no", colStr("acct_no"));
    sp.ppstr("purchase_date", businDate);
    sp.ppstr("from_code", "1");
    sp.ppstr("proc_flag", "N");
    sp.ppstr("txn_code", "N");
    sp.ppstr("mcht_no", "");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate ");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }
  // ************************************************************************
  int selectVmktFundName() throws Exception
  {
    strSql = " select "
            + " fund_name,"
            + " table_name "
            + " from vmkt_fund_name "
            + " where fund_code = ?  ";

    Object[] param =new Object[]
            {
                    colStr("fund_code")
            };

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0) colSet("fund_name" , "");

    return(1);
  }
  // ************************************************************************
  int getEffectMonths() throws Exception
  {
    strSql = " select "
            + " effect_months "
            + " from "+ colStr("table_name")
            + " where fund_code = ?  ";

    Object[] param =new Object[]
            {
                    colStr("fund_code")
            };

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0) colSet("effect_months" , "36");

    return(1);
  }
  // ************************************************************************
  int selectMktGift() throws Exception
  {
    strSql = " select "
            + " gift_name, "
            + " vendor_no "
            + " from mkt_gift "
            + " where gift_no = ?  ";

    Object[] param =new Object[]
            {
                    colStr("gift_no")
            };

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0) colSet("gift_name" , "noname");

    return(1);
  }
  // ************************************************************************
  int selectCycBpid() throws Exception
  {
    strSql = " select "
            + " effect_months "
            + " from cyc_bpid "
            + " where years      = ?  "
            + " and   acct_type  = ?  "
            + " and   bonus_type = ?  "
            + " and   item_code  = '1'  ";

    Object[] param =new Object[]
            {
                    colStr("tran_date").substring(0,4),
                    colStr("acct_type"),
                    colStr("bonus_type")
            };

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0) colSet("effect_months" , "24");

    return(1);
  }
  
  int selectDbmSysparm() throws Exception
  {
    strSql = " select "
            + " effect_months "
            + " from dbm_sysparm"
            + " WHERE parm_type = '01'  "
            + "and   apr_date !='' ";

    sqlSelect(strSql);
    if (sqlRowNum <= 0) colSet("effect_months" , "24");
    return(1);
  }

  // ************************************************************************
  int selectMktBonusBklog() throws Exception
  {
    dateTime();
    String sqlStr="";
    String tranSeqno = colStr("tran_seqno");
    int    mbdkBegTranBp = 0;

    CommFunction comm = new CommFunction();
    CommRoutine comr = new CommRoutine();
    comr.setConn(wp);
    MktBonus comb = new MktBonus();
    comb.setConn(wp);

    if (colStr("tran_seqno").length()>0)
    {
      sqlStr = " select "
              + " beg_tran_bp as mbdk_beg_tran_bp "
              + " from mkt_bonus_dtl "
              + " where tran_seqno  = '" + colStr("tran_seqno") +"' "
      ;

      DataSet ds1 =new DataSet();
      ds1.colList = this.sqlQuery(sqlStr,new Object[]{});
      ds1.listFetch(0);
      mbdkBegTranBp = (int)ds1.colNum("mbdk_beg_tran_bp")*-1;
    }
    int rowNum= sqlRowNum;

    colSet("active_name"   , "紅利積點兌換贈品(退貨)");
    colSet("mod_desc","交易序號"+colStr("tran_seqno")+"] 退貨日期["+colStr("return_date")
            +"]  兌換贈品["+colStr("gift_no")+"] 共 ["
            +String.format("%.0f",colNum("exchg_cnt"))+"]件");
    colSet("mod_reason"    , colStr("return_reason"));
    colSet("mod_memo"      , colStr("return_mark"));
    colSet("res_tran_bp"   , "0");
    colSet("res_e_date"    , "");
    colSet("tran_code","4");
    colSet("dtl_tran_date"     , wp.sysDate);
    colSet("dtl_tran_time"     , wp.sysTime);

    if (rowNum>0)
    {
      comb.bonusReverse(colStr("tran_seqno"));
      updateMktBonusDtl();

      colSet("beg_tran_bp"   , String.format("%d",mbdkBegTranBp));
      colSet("end_tran_bp"   , "0");
      colSet("tax_flag"      , "N");
      colSet("effect_e_date" , "");

//      tranSeqno = comr.getSeqno("MKT_MODSEQ");
      insertMktBonusDtl(tranSeqno);
//  	  colSet("tran_seqno",tranSeqno);
    }
    else
    {
      selectCycBpid();
      if (colNum("deduct_bp01") != 0){
          colSet("beg_tran_bp",String.format("%.0f",colNum("deduct_bp01")*-1));
          colSet("end_tran_bp",String.format("%.0f",colNum("deduct_bp01")*-1));
       }else if (colNum("deduct_bp90") == 0){ 
          colSet("beg_tran_bp"   , String.format("%.0f",colNum("exchg_pt")));
          colSet("end_tran_bp"   , String.format("%.0f",colNum("exchg_pt")));
       }
      
      colSet("tax_flag"      , "N");
      if (colNum("effect_months")==0)
        colSet("effect_e_date" , "");
      else
        colSet("effect_e_date" , comm.nextMonthDate(colStr("exg_apr_date"),(int)colNum("effect_months")));

//      tranSeqno = comr.getSeqno("MKT_MODSEQ");
      insertMktBonusDtl(tranSeqno);
//  	  colSet("tran_seqno",tranSeqno);
      comb.bonusFunc(tranSeqno);
    }
    return(1);
  }
  
  int selectDbmBonusBklog() throws Exception
  {
    dateTime();
    String sqlStr="";
    String vdtranSeqno="";
    int    vdBegTranBp = 0;

    CommFunction comm = new CommFunction();
    CommRoutine comr = new CommRoutine();
    comr.setConn(wp);
    DbmBonus comb = new DbmBonus();
    comb.setConn(wp);

    if (colStr("vdtran_seqno").length()>0)
    {
      sqlStr = " select "
              + " beg_tran_bp as vd_beg_tran_bp "
              + " from dbm_bonus_dtl "
              + " where tran_seqno  = '" + colStr("vdtran_seqno") +"' "
      ;

      DataSet ds1 =new DataSet();
      ds1.colList = this.sqlQuery(sqlStr,new Object[]{});
      ds1.listFetch(0);
      vdBegTranBp = (int)ds1.colNum("vd_beg_tran_bp")*-1;
    }
    int rowNum= sqlRowNum;

    colSet("active_name"   , "紅利積點兌換贈品(退貨)");
    colSet("mod_desc","交易序號"+colStr("tran_seqno")+"] 退貨日期["+colStr("return_date")
            +"]  兌換贈品["+colStr("gift_no")+"] 共 ["
            +String.format("%.0f",colNum("exchg_cnt"))+"]件");
    colSet("mod_reason"    , colStr("return_reason"));
    colSet("mod_memo"      , colStr("return_mark"));
    colSet("res_tran_bp"   , "0");
    colSet("res_e_date"    , "");
    colSet("tran_code","4");
    colSet("dtl_tran_date"     , wp.sysDate);
    colSet("dtl_tran_time"     , wp.sysTime);

    if (rowNum>0)
    {
      comb.bonusReverse(colStr("vdtran_seqno"));
      updateDbmBonusDtl();

      colSet("beg_tran_bp"   , String.format("%d",vdBegTranBp));
      colSet("end_tran_bp"   , "0");
      colSet("tax_flag"      , "N");
      colSet("effect_e_date" , "");

      vdtranSeqno = comr.getSeqno("ECS_DBMSEQ");
      insertDbmBonusDtl(vdtranSeqno);
    }
    else
    {
      selectDbmSysparm();

      colSet("beg_tran_bp", String.format("%.0f", colNum("deduct_bp90") * -1));
      colSet("end_tran_bp", String.format("%.0f", colNum("deduct_bp90") * -1));

      colSet("tax_flag"      , "N");
      if (colNum("effect_months")==0)
        colSet("effect_e_date" , "");
      else
        colSet("effect_e_date" , comm.nextMonthDate(colStr("exg_apr_date"),(int)colNum("effect_months")));

      vdtranSeqno = comr.getSeqno("ECS_DBMSEQ");
      insertDbmBonusDtl(vdtranSeqno);
      comb.bonusFunc(vdtranSeqno);
    }
    return(1);
  }
  // ************************************************************************
  int selectMktBonusDtl() throws Exception
  {
    strSql = " select "
            + " res_tran_bp "
            + " from  mkt_bonus_dtl "
            + " where tran_seqno = ?  ";

    Object[] param =new Object[]
            {
                    colStr("tran_seqno")
            };

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0) colSet("res_tran_np" , "0");

    return((int)colNum("res_tran_bp"));
  }
  // ************************************************************************
  int deleteMktBonusDtl() throws Exception
  {
    strSql= "delete mkt_bonus_dtl "
            + "where tran_seqno  = ? "
    ;

    Object[] param =new Object[]
            {
                    colStr("tran_seqno")
            };

    rc = sqlExec(strSql, param);

    return rc;
  }
  
  int deleteDbmBonusDtl() throws Exception{
	    strSql= "delete dbm_bonus_dtl "
	            + "where tran_seqno  = ? "
	    ;

	    Object[] param =new Object[]
	            {
	            	colStr("vdtran_seqno")
	            };

	    rc = sqlExec(strSql, param);

	    return rc; 
  }
  
  // ************************************************************************
  int updateMktBonusDtl() throws Exception
  {
    strSql= "update mkt_bonus_dtl "
            + "set   end_tran_bp = 0, "
            + "      mod_user    = ?, "
            + "      mod_time    = sysdate,"
            + "      mod_pgm     = ?, "
            + "      mod_seqno   = nvl(mod_seqno,0)+1 "
            + "where tran_seqno  = ? "
    ;

    Object[] param =new Object[]
            {
                    wp.loginUser,
                    wp.modPgm(),
                    colStr("tran_seqno")
            };

    rc = sqlExec(strSql, param);

    return rc;
  }
  
  int updateDbmBonusDtl() throws Exception{
	    strSql= "update dbm_bonus_dtl "
	            + "set   end_tran_bp = 0, "
	            + "      mod_user    = ?, "
	            + "      mod_time    = sysdate,"
	            + "      mod_pgm     = ?, "
	            + "      mod_seqno   = nvl(mod_seqno,0)+1 "
	            + "where tran_seqno  = ? "   ;
	    Object[] param =new Object[]
	            {
	                    wp.loginUser,
	                    wp.modPgm(),
	                    colStr("vdtran_seqno")
	            };
	    rc = sqlExec(strSql, param);
	    return rc;
	}

  // ************************************************************************
  int updateMktGiftE() throws Exception
  {
    strSql = "update mkt_gift "
            + "set   use_limit_count = use_limit_count + ?,  "
            + "      net_limit_count = net_limit_count - ?,  "
            + "      limit_last_date = ?,  "
            + "      mod_user  = ?, "
            + "      mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "      mod_pgm   = ?, "
            + "      mod_seqno = nvl(mod_seqno,0)+1 "
            + "where gift_no  = ? "
    ;

    Object[] param =new Object[]
            {
                    colNum("exchg_cnt"),
                    colNum("exchg_cnt"),
                    wp.sysDate,
                    wp.loginUser,
                    wp.sysDate+wp.sysTime,
                    wp.modPgm(),
                    colStr("gift_no")
            };

    rc = sqlExec(strSql, param);

    return rc;
  }
  // ************************************************************************
  int updateMktGiftEcouponOut(String tranSeqno) throws Exception
  {
    strSql = "update mkt_gift_ecoupon "
            + "set   cancel_flag = 'Y', "
            + "      cancel_date = ?,  "
            + "      mod_user  = ?, "
            + "      mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "      mod_pgm   = ? "
            + "where tran_seqno  = ? "
    ;

    Object[] param =new Object[]
            {
                    wp.sysDate,
                    wp.loginUser,
                    wp.sysDate+wp.sysTime,
                    wp.modPgm(),
                    tranSeqno
            };

    rc = sqlExec(strSql, param);

    return rc;
  }
  // ************************************************************************
  int updateMktGiftReEcoupon() throws Exception
  {
    strSql = "update mkt_gift "
            + "set   use_limit_count = use_limit_count - ?,  "
            + "      net_limit_count = net_limit_count + ?,  "
            + "      limit_last_date = ?,  "
            + "      mod_user  = ?, "
            + "      mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "      mod_pgm   = ?, "
            + "      mod_seqno = nvl(mod_seqno,0)+1 "
            + "where gift_no  = ? "
    ;

    Object[] param =new Object[]
            {
                    colNum("exchg_cnt"),
                    colNum("exchg_cnt"),
                    wp.sysDate,
                    wp.loginUser,
                    wp.sysDate+wp.sysTime,
                    wp.modPgm(),
                    colStr("gift_no")
            };

    rc = sqlExec(strSql, param);

    return rc;
  }
  // ************************************************************************
  int updateMktGiftENo() throws Exception
  {
    strSql = "update mkt_gift "
            + "set   use_count = use_count + ?,  "
            + "      net_count = net_count - ?,  "
            + "      limit_last_date = ?,  "
            + "      mod_user  = ?, "
            + "      mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "      mod_pgm   = ?, "
            + "      mod_seqno = nvl(mod_seqno,0)+1 "
            + "where gift_no  = ? "
    ;

    Object[] param =new Object[]
            {
                    colNum("exchg_cnt"),
                    colNum("exchg_cnt"),
                    wp.sysDate,
                    wp.loginUser,
                    wp.sysDate+wp.sysTime,
                    wp.modPgm(),
                    colStr("gift_no")
            };

    rc = sqlExec(strSql, param);

    return rc;
  }
  // ************************************************************************
  int updateMktGiftReEcouponNo() throws Exception
  {
    strSql= "update mkt_gift "
            + "set   use_count = use_count - ?,  "
            + "      net_count = net_count + ?,  "
            + "      limit_last_date = ?,  "
            + "      mod_user  = ?, "
            + "      mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "      mod_pgm   = ?, "
            + "      mod_seqno = nvl(mod_seqno,0)+1 "
            + "where gift_no  = ? "
    ;

    Object[] param =new Object[]
            {
                    colNum("exchg_cnt"),
                    colNum("exchg_cnt"),
                    wp.sysDate,
                    wp.loginUser,
                    wp.sysDate+wp.sysTime,
                    wp.modPgm(),
                    colStr("gift_no")
            };

    rc = sqlExec(strSql, param);

    return rc;
  }
  // ************************************************************************
  int selectMktGiftEcoupon() throws Exception
  {
    String sqlStr="";

    sqlStr = " select "
            + " auth_code, "
            + " http_url "
            + " from mkt_gift_ecoupon "
            + " where tran_seqno  = '" + colStr("tran_seqno") +"' "
    ;

    busi.DataSet ds1 =new busi.DataSet();
    ds1.colList = this.sqlQuery(sqlStr,new Object[]{});

    int rowNum= sqlRowNum;

    for ( int inti=0; inti<rowNum; inti++ )
    {
      ds1.listFetch(inti);

      colSet("http_auth_code" , ds1.colStr("auth_code"));
      colSet("http_url"       , ds1.colStr("http_url"));

      insertMktGiftCancel();
      updateMktGiftEcouponHttp();
    }
    return(0);
  }
  // ************************************************************************
  public int insertMktGiftCancel() throws Exception
  {
    strSql= " insert into  mkt_gift_cancel ("
            + " card_no, "
            + " gift_no, "
            + " vendor_no, "
            + " tran_seqno, "
            + " tran_date, "
            + " ecoupon_bno, "
            + " ecoupon_date_s, "
            + " ecoupon_date_e, "
            + " ecoupon_date, "
            + " sms_flag, "
            + " sms_date, "
            + " return_date, "
            + " return_reason, "
            + " return_mark, "
            + " p_seqno, "
            + " id_p_seqno, "
            + " acct_type, "
            + " http_url, "
            + " auth_code, "
            + " crt_date, "
            + " mod_time, "
            + " mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,?,?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + " timestamp_format(?,'yyyymmddhh24miss'), "
            + " ?) ";

    Object[] param =new Object[]
            {
                    colStr("card_no"),
                    colStr("gift_no"),
                    colStr("vendor_no"),
                    colStr("tran_seqno"),
                    colStr("tran_date"),
                    colStr("ecoupon_bno"),
                    colStr("ecoupon_date_s"),
                    colStr("ecoupon_date_e"),
                    colStr("ecoupon_date"),
                    colStr("sms_flag"),
                    colStr("sms_date"),
                    colStr("return_date"),
                    colStr("return_reason"),
                    colStr("return_mark"),
                    colStr("p_seqno"),
                    colStr("id_p_seqno"),
                    colStr("acct_type"),
                    colStr("http_url"),
                    colStr("http_auth_code"),
                    wp.sysDate + wp.sysTime,
                    wp.modPgm()
            };

    sqlExec(strSql, param);

    return rc;
  }
  // ************************************************************************
  int updateMktGiftEcouponHttp() throws Exception
  {
    strSql = "update mkt_gift_ecoupon "
            + "set   cancel_flag   = 'Y',"
            + "      cancel_date   =  to_char(sysdate,'yyyymmdd'),  "
            + "      mod_user  = ?, "
            + "      mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "      mod_pgm   = ? "
            + "where http_url  = ? "
    ;

    Object[] param =new Object[]
            {
                    wp.loginUser,
                    wp.sysDate+wp.sysTime,
                    wp.modPgm(),
                    colStr("http_url")
            };

    rc = sqlExec(strSql, param);

    return rc;
  }
  // ************************************************************************
  public int insertMktGiftVouch(String vouchDataType, String tranSeqno, int vouchAmt) throws Exception
  {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);

    strSql= " insert into  mkt_gift_vouch ("
            + " create_date,"
            + " create_time,"
            + " p_seqno,"
            + " acct_type,"
            + " card_no,"
            + " tran_seqno,"
            + " gift_no,"
            + " vouch_seqno,"
            + " business_date,"
            + " vouch_amt,"
            + " d_vouch_amt,"
            + " vouch_data_type,"
            + " tran_pgm,"
            + " proc_flag,"
            + " mod_time,"
            + " mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,"
            + " timestamp_format(?,'yyyymmddhh24miss'), "
            + " ?) ";

    Object[] param =new Object[]
            {
                    wp.sysDate,
                    wp.sysTime,
                    colStr("p_seqno"),
                    colStr("acct_type"),
                    colStr("card_no"),
                    colStr("tran_seqno"),
                    colStr("gift_no"),
                    tranSeqno,
                    comr.getBusinDate(),
                    vouchAmt,
                    0,
                    vouchDataType,
                    wp.modPgm(),
                    "N",
                    wp.sysDate + wp.sysTime,
                    wp.modPgm()
            };

    sqlExec(strSql, param);

    return rc;
  }
  // ************************************************************************
  int selectPtrSysIdtab() throws Exception
  {
    strSql = "select "
            + " wf_desc "
            + " from ptr_sys_idtab "
            + " where 1 = 1 "
            + " and   wf_type = 'ADJMOD_REASON' "
            + " and   wf_id = ? "
    ;

    Object[] param =new Object[]
            {
                    colStr("return_reason")
            };

    sqlSelect(strSql, param);

    return(1);
  }
// ************************************************************************

  int getCountDp() {
	  if(getCardData(colStr("card_no"))==0) {
		  return -1;
	  }

	  if (colStr("deduct_date").length()==0)
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
       Object[] param = new Object[] {colStr("card_no"),colStr("gift_no")};
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
           param = new Object[] {colStr("card_no"),colStr("gift_no")};
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
        return -1;
       }
      }
	  int deductBp01 = 0;
      int deductBp90 = 0;
      double needPt = colNum("exchg_cnt")*colInt("exchg_pt");
      MktBonus comc = new MktBonus();
      comc.setConn(wp);
      int nowPt01 = comc.getEndTranBp01(colStr("id_no"));
      int nowPt90 = comc.getEndTranBp90(colStr("id_no"));
      int[] deductBp = comc.countDeductBp(nowPt01,nowPt90,(int)needPt);
      if(deductBp != null) {
   	   	deductBp01 = deductBp[0];
   	   	deductBp90 = deductBp[1];
      }
      colSet("end_tran_bp",String.format("%d", nowPt01 + nowPt90));
      colSet("end_tran_bp01",String.format("%d", nowPt01));
      colSet("end_tran_bp90",String.format("%d", nowPt90));
      colSet("deduct_bp01",String.format("%d", deductBp01));
      colSet("deduct_bp90",String.format("%d", deductBp90));
      
	  
	  return 1;
  }

	int getCardData(String cardNo) {
		crdCardNo = "";
		crdPSeqno = "";
		dbcCardNo = "";
		dbcPSeqno = "";
		strSql = "select '0' as vd_flag ,b.id_no ,a.p_seqno as card_p_seqno from crd_card a join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
		strSql += " where a.acct_type = '01' and a.current_code = '0' and a.sup_flag ='0' and a.card_no = :crd_card_no ";
		strSql += " union ";
		strSql += " select '1' as vd_flag ,b.id_no ,a.p_seqno as card_p_seqno from dbc_card a join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
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
			cardNote = "a.card_note ";
			crdCardNo = cardNo;
			crdPSeqno = colStr("card_p_seqno");
		} else if ("1".equals(vdFlag)) {
			cardTable = "dbc_Card ";
			cardNote = "substring(a.card_type,1,1) ";
			dbcCardNo = cardNo;
			dbcPSeqno = colStr("card_p_seqno");
		}
		
		if("0".equals(vdFlag))
			strSql = "select card_no as crd_card_no ,p_seqno as crd_p_seqno from crd_card  where acct_type = '01' and current_code = '0' and sup_flag ='0' and id_p_seqno = :id_p_seqno";
		if("1".equals(vdFlag))
			strSql = " select card_no as dbc_card_no ,p_seqno as dbc_p_seqno ,acct_no from dbc_card  where acct_type ='90' and current_code = '0' and id_p_seqno = :id_p_seqno ";
		setString("id_p_seqno",colStr("id_p_seqno"));
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			if("0".equals(vdFlag)) {
				crdCardNo = colStr(0,"crd_card_no");
				crdPSeqno = colStr(0,"crd_p_seqno");
			}
			if("1".equals(vdFlag)) {
				dbcCardNo = colStr(0,"dbc_card_no");
				dbcPSeqno = colStr(0,"dbc_p_seqno");
			}
		}
		
		return 1;
	}
	
	void selectPtrFundp() {
		CommDate commDate = new CommDate();
	    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
	    comr.setConn(wp);
	    String businDate = comr.getBusinDate();
		strSql = "select effect_type ,effect_years ,effect_months ,effect_fix_month from  ptr_fundp ";
		strSql += " where fund_code = :fund_code ";
		setString("fund_code",colStr("fund_code"));
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			if("1".equals(colStr("effect_type"))) {
				if(colInt("effect_months")>0) {
					colSet("effect_e_date",commDate.dateAdd(businDate, 0, colInt("effect_months"), 0));
				}
			}else if("2".equals(colStr("effect_type"))){
				if(colInt("effect_years") > 0 || colInt("effect_fix_month") > 0) {
					colSet("effect_e_date",commDate.dateAdd(businDate, 0, (colInt("effect_years") * 12) + colInt("effect_months"), 0));
				}
			}
		}else {
			colSet("effect_e_date","");
		}
	}
	

// ************************************************************************

}  // End of class
