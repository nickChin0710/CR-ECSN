/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/04/12  V1.00.04  Allen Ho                                              *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-12-25   V1.00.02 Justin        parameterize sql
* 111-11-25  V1.00.02  Zuwei       Sync from mega                              *
******************************************************************************/
package busi.ecs;

// import taroko.com.TarokoCommon;
// import java.sql.Connection;
// import busi.sqlcond;
import java.lang.Math;

public class MktCashback extends busi.FuncBase {
  // 已下3欄位只有需要知充抵應稅即免稅多少時用

  static String sqlStr = "";
  public int hCommTranAmt = 0;
   
  String hMcdlPseqno = "";
  String hMcdlFundCode = "";
  int hMcdlEndTranAmt = 0;
  int hMcdlTranAmt = 0;
  int    linkTranAmt     = 0;
  int    mainTranAmt     = 0;
  String linkSeqno = "";
  String mainSeqno = "";

  String[] dbname = new String[10];

  // ************************************************************************
  public int cashbackFunc() {
    sqlStr =
        "select max(tran_seqno) as tran_seqno  " 
			+ "from mkt_cashback_dtl "
            + "where  end_tran_amt != 0 " 
			+ "group by p_seqno,fund_code "
            + "having sum(decode(sign(end_tran_amt),1,end_tran_amt,0))!=0 "
            + "and    sum(decode(sign(end_tran_amt),-1,end_tran_amt,0))!=0 ";

    busi.DataSet ds1 = new busi.DataSet();
    ds1.colList = this.sqlQuery(sqlStr, new Object[] {});

    for (int inti = 0; inti < ds1.listRows(); inti++) {
      ds1.listFetch(inti);

      cashbackFunc(ds1.colStr("tran_seqno"));
    }

    return (0);
  }
// ************************************************************************
 public  int cashbackFuncP(String pSeqno) throws Exception {
  sqlStr = "select tran_seqno  "
         + "from mkt_cashback_dtl "
         + "where  end_tran_amt != 0 "
         + "and   p_seqno = '" + pSeqno +"' "
         + "order by end_tran_amt "
         ;

  busi.DataSet dsf =new busi.DataSet();
  dsf.colList =this.sqlQuery(sqlStr, new Object[]{});

  for (int inti=0; inti<dsf.listRows(); inti++) 
   {
     dsf.listFetch(inti);

     cashbackFunc(dsf.colStr("tran_seqno"));
   }

  return(0);
 }
  // ************************************************************************
 void init_data()
 {
  hCommTranAmt  = 0;

  return;
 }
// ************************************************************************
  public int cashbackFunc(String tranSeqno) {
    sqlStr =
        "select " + "end_tran_amt as end_tran_amt_q, " 
			+ "p_seqno as p_seqno, "
            + "fund_code as fund_code, " 
			+ "mod_seqno as mod_seqno_q, " 
			+ "hex(rowid) as rowid_q "
            + "from mkt_cashback_dtl " 
			+ "where  tran_seqno = ? ";

    this.sqlSelect(sqlStr, new Object[] {tranSeqno});

    if (sqlRowNum == 0)
      return (1);

    hMcdlPseqno = colStr("p_seqno");
    hMcdlFundCode = colStr("fund_code");
    hMcdlEndTranAmt = (int) colNum("end_tran_amt_q");

    wp.log("common 11111111111111 beg_amt  [" + hMcdlEndTranAmt + "]");
    mainSeqno = tranSeqno;
    mainTranAmt = 0;
    hMcdlTranAmt = selectMktCashbackDtlM(hMcdlEndTranAmt);

    wp.log("common 4444444444444444 end_amt  [" + hMcdlTranAmt + "]");
    linkSeqno = "";
    linkTranAmt = 0;
    if ((mainSeqno.length()!=0)&&
       (mainTranAmt!=0))
      {
       linkSeqno = mainSeqno;
       linkTranAmt = mainTranAmt;
      }

    if (hMcdlTranAmt != hMcdlEndTranAmt)
      updateMktCashbackDtl(colStr("rowid_q"), (int) colNum("mod_seqno_q"));

    return (0);
  }

  // ************************************************************************
  public int selectMktCashbackDtlM(int tempTranAmt) {
  sqlStr = " select "
         + " mod_seqno, "
         + " tran_seqno, "
		+ " end_tran_amt, " 
		+ " hex(rowid) as rowid_a "
        + " from mkt_cashback_dtl " 
		+ " where  end_tran_amt  != 0 "
        + " and    fund_code     = ? " 
        + " and    p_seqno       = ? " 
        + " and    decode(sign(end_tran_amt),1,1,0,3,-1) = decode(sign(?),-1,1,0,4,-1) "
        + " order by  decode(effect_e_date,'','99999999',effect_e_date),tran_date,tran_time ";

    // wp.ddd("STEP 001-001 =["+sqlStr+"]");
    busi.DataSet ds1 = new busi.DataSet();
    ds1.colList = this.sqlQuery(sqlStr, new Object[] {hMcdlFundCode, hMcdlPseqno, tempTranAmt});

    if (sqlRowNum == 0)
      return (tempTranAmt);

    hCommTranAmt = 0;
    int hMcdlPTranAmt = 0;
    int hMcdlMTranAmt = 0;

    // wp.ddd("STEP 001-002 =["+sql_nrow+"]");
    int rowNum = sqlRowNum;
    for (int inti = 0; inti < rowNum; inti++) {
      // wp.ddd("STEP 001-003 =["+inti+"]");
      ds1.listFetch(inti);
      // wp.ddd("STEP 001-004 =["+inti+"]");

      wp.log("common 2222222222222 proc_amt  [" + ds1.colNum("end_tran_amt") + "]");
      hCommTranAmt = hCommTranAmt + Math.abs(tempTranAmt + (int) ds1.colNum("end_tran_amt"));

      if (tempTranAmt > 0) {
        hMcdlPTranAmt = tempTranAmt;
        hMcdlMTranAmt = (int) ds1.colNum("end_tran_amt");
      } else {
        hMcdlMTranAmt = tempTranAmt;
        hMcdlPTranAmt = (int) ds1.colNum("end_tran_amt");
      }

      if (tempTranAmt > 0) {
        if (hMcdlPTranAmt + hMcdlMTranAmt > 0) {
          hMcdlTranAmt = 0;
          tempTranAmt = hMcdlPTranAmt + hMcdlMTranAmt;
        } else {
          hMcdlTranAmt = hMcdlPTranAmt + hMcdlMTranAmt;
          tempTranAmt = 0;
        }
      } else {
        if (hMcdlPTranAmt + hMcdlMTranAmt < 0) {
          hMcdlTranAmt = 0;
          tempTranAmt = hMcdlPTranAmt + hMcdlMTranAmt;
        } else {
          hMcdlTranAmt = hMcdlPTranAmt + hMcdlMTranAmt;
          tempTranAmt = 0;
        }
      }

     if (hMcdlTranAmt==0)
        {
         linkTranAmt = (int)ds1.colNum("end_tran_amt")
                       - hMcdlTranAmt;
         linkSeqno = mainSeqno;
        }
     else
        {
         linkTranAmt = 0;
         linkSeqno    = "";
         mainTranAmt = 0 - ((int)ds1.colNum("end_tran_amt") - hMcdlTranAmt);
         mainSeqno = ds1.colStr("tran_seqno");
        }

      wp.log("common 3333333333333 tran_amt  [" + hMcdlTranAmt + "]");
      wp.log("common 3333333333333 last_amt  [" + tempTranAmt + "]");
      updateMktCashbackDtl(ds1.colStr("rowid_a"), (int) ds1.colNum("mod_seqno"));

      // wp.ddd("STEP 001-005 =["+temp_tran_amt+"]["+h_mcdl_tran_amt+"]");
      if (tempTranAmt == 0)
        break;
      // wp.ddd("STEP 001-006 =["+temp_tran_amt+"]["+h_mcdl_tran_amt+"]");
    }

    return (tempTranAmt);
  }

  // ************************************************************************
  public int updateMktCashbackDtl(String tempRowid, double tempModSeqno) {
    dateTime();

    sqlStr =
        " update mkt_cashback_dtl set " 
		+ " end_tran_amt = ?, " 
         + "link_seqno    = ?, "
         + "link_tran_amt = ?, "
		+ " mod_pgm     = ?, "
        + " mod_seqno   = mod_seqno + 1 , " 
		+ " mod_time    = sysdate " 
		+ " where hex(rowid) = ? " 
		+ " AND mod_seqno   = ? ";

    Object[] param = new Object[] {
		hMcdlTranAmt, 
     	linkSeqno,
     	linkTranAmt,
		wp.modPgm(), 
		tempRowid, 
		tempModSeqno
	};

    sqlExec(sqlStr, param);
    if (sqlRowNum <= 0)
      errmsg(this.sqlErrtext);

    return (0);
  }
// ************************************************************************
 public int cashbackReverse(String tranSeqno) throws Exception
 {
  int intq=0;
  String[] tranQueue = new String[10];
  tranQueue[0] = tranSeqno;
  for (;;)
    {
     tranQueue[intq+1] = selectMktCashbackDtlQueue(tranQueue[intq]);
     if (tranQueue[intq+1].length()==0) break;
     intq++;
    }

  for (int inti=intq;inti>=0;inti--)
    { 
     cashbackReverseM(tranQueue[inti]);
    }

  return(0);
 }
// ************************************************************************
 public int cashbackReverseM(String tranSeqno) throws Exception
 {
  sqlStr = "select "
         + "fund_code     as r_fund_code,"
         + "fund_name     as r_fund_name,"
         + "acct_date     as r_acct_date,"
         + "beg_tran_amt  as r_beg_tran_amt,"
         + "end_tran_amt  as r_end_tran_amt,"
         + "res_tran_amt  as r_res_tran_amt,"
         + "tran_seqno    as r_tran_seqno,"
         + "acct_type     as r_acct_type, "
         + "mod_desc      as r_mod_desc, "
         + "p_seqno       as r_p_seqno, "
         + "id_p_seqno    as r_id_p_seqno, "
         + "link_seqno    as r_link_seqno, "
         + "link_tran_amt as r_link_tran_amt,"
         + "mod_seqno     as r_mod_seqno, "
         + "hex(rowid)    as r_rowid "
         + "from mkt_cashback_dtl "
         + "where  tran_seqno = '" + tranSeqno +"' "
         ;

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) return(0);

  mainTranAmt     = 0;

  if (colStr("r_link_seqno").length()>0)
     {
      if ((int)colNum("r_beg_tran_amt")<0)
         {
          if ((int)colNum("r_beg_tran_amt")>(int)colNum("r_link_tran_amt"))
              colSet("r_link_tran_amt",String.format("%d",(int)colNum("r_beg_tran_amt")));
         }
      else
         {
          if ((int)colNum("r_beg_tran_amt")<(int)colNum("r_link_tran_amt"))
              colSet("r_link_tran_amt",String.format("%d",(int)colNum("r_beg_tran_amt")));
         }
     }

  if ((int)colNum("r_beg_tran_amt") == 
      (int)colNum("r_end_tran_amt") + (int)colNum("r_res_tran_amt")) return(0);
  selectMktCashbackDtlR1();

  if (colStr("r_link_seqno").length()!=0)
      selectMktCashbackDtlR2();

  updateMktCashbackDtlR2((int)colNum("r_beg_tran_amt")-(int)colNum("r_res_tran_amt"));

  if ((int)colNum("r_beg_tran_amt") - (int)colNum("r_end_tran_amt") 
                                     - (int)colNum("r_res_tran_amt") != mainTranAmt)
     {
      selectVmktFundName();

      int begTranAmt = (int)colNum("r_beg_tran_amt")
                       - (int)colNum("r_end_tran_amt")
                       - (int)colNum("r_res_tran_amt")
                       - mainTranAmt;

      if (begTranAmt!=0)  
         insertMktCashbackDtl(begTranAmt*-1);
     }
  return(0);
 }
// ************************************************************************
 int selectMktCashbackDtlR1() throws Exception
 {
  sqlStr = "select "
         + "mod_seqno     as com_mod_seqno, "
         + "tran_seqno    as com_tran_seqno,"
         + "link_seqno    as com_link_seqno,"
         + "link_tran_amt as com_link_tran_amt,"
         + "beg_tran_amt  as com_beg_tran_amt, "
         + "end_tran_amt   as com_end_tran_amt, "
         + "hex(rowid)   as com_rowid "
         + "from mkt_cashback_dtl "
         + "where  link_seqno = '" + colStr("r_tran_seqno") +"' "
         + "and    p_seqno    = '" + colStr("r_p_seqno") +"' "
         ;

  busi.DataSet ds1 =new busi.DataSet();
  ds1.colList = this.sqlQuery(sqlStr,new Object[]{});

  if (sqlRowNum<=0) return(0);

  int recCnt = sqlRowNum;

  for ( int inti=0; inti<recCnt; inti++ )
    {
     ds1.listFetch(inti);

     if (ds1.colStr("com_link_seqno").length()>0)
        {
         if ((int)ds1.colNum("com_beg_tran_amt")<0)
            {
             if ((int)ds1.colNum("com.beg_tran_amt")>(int)ds1.colNum("com_link_tran_amt"))
                 colSet(inti,"com_link_tran_amt",String.format("%d",(int)ds1.colNum("com_beg_tran_amt")));
            }
         else
            {
             if ((int)ds1.colNum("com_beg_tran_amt")<(int)ds1.colNum("com_link_tran_amt"))
                 colSet(inti,"com_link_tran_amt",String.format("%d",(int)ds1.colNum("com_beg_tran_amt")));
            }
        }

     mainTranAmt = mainTranAmt - (int)ds1.colNum("com_link_tran_amt");

     colSet(0,"end_tran_amt"  , (int)ds1.colNum("com_end_tran_amt")
                               + (int)ds1.colNum("com_link_tran_amt"));

     updateMktCashbackDtlR1(ds1.colStr("com_rowid"),ds1.colNum("com_mod_seqno"));
    }

  return(0); 
 }
// ************************************************************************
 int selectMktCashbackDtlR2() throws Exception
 {
  sqlStr = "select "
         + "mod_seqno     as com_mod_seqno, "
         + "link_seqno    as com_link_seqno,"
         + "link_tran_amt as com_link_tran_amt,"
         + "beg_tran_amt  as com_beg_tran_amt, "
         + "end_tran_amt  as com_end_tran_amt, "
         + "hex(rowid)    as com_rowid "
         + "from mkt_cashback_dtl "
         + "where  tran_seqno = '" + colStr("r_link_seqno") +"' "
         + "and    p_seqno    = '" + colStr("r_p_seqno") +"' "
         ;

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) return(0);
  int recCnt = sqlRowNum;

  if (colStr("com_link_seqno").length()>0)
     {
      if ((int)colNum("com_beg_tran_amt")<0)
         {
          if ((int)colNum("com.beg_tran_amt")>(int)colNum("com_link_tran_amt"))
              colSet("com_link_tran_amt",String.format("%d",(int)colNum("com_beg_tran_amt")));
         }
      else
         {
          if ((int)colNum("com_beg_tran_amt")<(int)colNum("com_link_tran_amt"))
              colSet("com_link_tran_amt",String.format("%d",(int)colNum("com_beg_tran_amt")));
         }
     }
  mainTranAmt = mainTranAmt + (int)colNum("r_link_tran_amt");

  colSet(0,"end_tran_amt"  , String.format("%d"
                           , (int)colNum("com_end_tran_amt")
                           - (int)colNum("r_link_tran_amt")));

  updateMktCashbackDtlR1(colStr("com_rowid"),colNum("com_mod_seqno"));

  return(0); 
 }
// ************************************************************************
 int updateMktCashbackDtlR1(String tempRowid,double modSeqno) throws Exception
 {
  sqlStr = "update mkt_cashback_dtl set " 
         + "end_tran_amt  = ?, "
         + "link_seqno    = '',"
         + "link_tran_amt = 0, "
         + "mod_pgm       = ?, "
         + "mod_seqno     = mod_seqno + 1 , "
         + "mod_time      = sysdate " 
         + "where rowid   = x'" + tempRowid +"' "
         + "AND mod_seqno = "+ modSeqno+" "; 

  Object[] param =new Object[]
    {
     (int)colNum("end_tran_amt"),
     wp.modPgm(),
    };

  sqlExec(sqlStr , param);
  if (sqlRowNum <= 0) errmsg(this.sqlErrtext);

  return(0);
 }
// ************************************************************************
 int updateMktCashbackDtlR2(int endTranAmt) throws Exception
 {
  sqlStr = "update mkt_cashback_dtl set " 
         + "end_tran_amt  = ?, "
         + "link_seqno    = '',"
         + "link_tran_amt = 0, "
         + "mod_pgm       = ?, "
         + "mod_seqno     = mod_seqno + 1 , "
         + "mod_time      = sysdate " 
         + "where rowid   = x'" + colStr("r_rowid") +"' "
         + "AND mod_seqno = " +  colStr("r_mod_seqno") + " "; 

  Object[] param =new Object[]
    {
     endTranAmt,
     wp.modPgm(),
    };

  sqlExec(sqlStr , param);
  if (sqlRowNum <= 0) errmsg(this.sqlErrtext);

  return(0);
 }
// ************************************************************************
 int insertMktCashbackDtl(int endTranAmt) throws Exception
 {
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

  String effectEDate = "";
  if (endTranAmt>0)
     {
      if (colStr("effc_effect_type").equals("1"))
         {
          if (colNum("effc_effect_months")>0)
              effectEDate = comm.nextMonthDate(colStr("r_acct_date")
                            , (int)colNum("effc_effect_months"));
         }
      else if (colStr("effc_effect_type").equals("2"))
         {
          String[]  stra = new String[10];
          stra[0] = colStr("r_acct_date");
          int[] inta = new int[10];
          inta[0] = (int)colNum("effc_effect_years")-1;
          inta[1] = (int)colNum("effc_effect_fix_month");
          stra[1] = String.format("%02d", inta[1]);

          effectEDate  = comm.lastdateOfmonth(
                           comm.nextMonth(stra[0],inta[0]*12).substring(0,4)+stra[1]+"01");
         }
     }

  String lsIns = "";
  sqlStr= " insert into mkt_cashback_dtl("
          + " acct_type, "
          + " fund_code, "
          + " fund_name, "
          + " tran_code, "
          + " beg_tran_amt, "
          + " end_tran_amt, "
          + " effect_e_date, "
          + " mod_desc, "
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
          + "?,?,?,?,?,?,?,?,?,?,"     // last: tran_date
          + "?,?,?,?,?,?,?,?,"         // last:apr_flag 
          + "?,?,?,?,"
          + "timestamp_format(?,'yyyymmddhh24miss'),?,?,?)";

  Object[] param =new Object[]
       {
        colStr("r_acct_type"),
        colStr("r_fund_code"),
        colStr("r_fund_name")+ "沖回調整",           
        "3",
        endTranAmt,
        endTranAmt,
        effectEDate,   
        colStr("r_mod_desc"),        
        "tran_seqno:["+colStr("r_tran_seqno")+"]",
        wp.sysDate ,
        wp.sysTime,
        colStr("r_p_seqno"),
        colStr("r_id_p_seqno"),
        wp.modPgm(),
        comr.getSeqno("MKT_MODSEQ"),
        comr.getBusinDate().substring(0,6),
        comr.getBusinDate(),
        "Y",
        wp.sysDate,
        wp.loginUser,
        wp.sysDate,
        wp.loginUser,
        wp.sysDate+wp.sysTime,
        wp.loginUser,
        wp.modPgm(),
        0
       };

  sqlExec(sqlStr, param);
  if (sqlRowNum <= 0) errmsg(sqlErrtext);

  return(1);
 }
// ************************************************************************
 int selectVmktFundName() throws Exception
 {
  sqlStr = "select "
         + "table_name " 
         + "from vmkt_fund_name "
         + "where  fund_code   = '" + colStr("fund_code") +"' ";

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) 
     {
      colSet("effc_effect_type"   ,"0");
      colSet("effc_effect_months" ,"0");
      return(1);
     }
      
  if (colStr("table_name").toUpperCase().equals("PTR_FUNDP"))
     {
      selectPtrFundp();
     }
  else
     {
      selectFundTable();
     }

  return(0); 
 }
// ************************************************************************
 int selectFundTable() throws Exception
 {
  sqlStr = "select "
         + "effect_months as effc_effect_months "
         + "from "+colStr("table_name") +" "
         + "where  fund_code   = '" + colStr("fund_code") +"' ";

  this.sqlSelect(sqlStr);

  colSet("effc_effect_type" ,"0");
  if (sqlRowNum==0) 
      colSet("effc_effect_months" ,"0");

  if (colNum("effc_effect_months")>0)
     colSet("effc_effect_type" ,"1");

  return(0); 
 }
// ************************************************************************
 int selectPtrFundp() throws Exception
 {
  sqlStr = "select "
         + "effect_type   as effc_effect_typehs, "
         + "effect_year   as effc_effect_typehs, "
         + "effect_months as effc_effect_months,"
         + "effect_fix_months as effc_effect_fix_month "
         + "from ptr_fundp "
         + "where  fund_code   = '" + colStr("fund_code") +"' ";

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) 
     {
      colSet("effc_effect_type" ,"0");
      colSet("effc_effect_months" ,"0");
     } 

  return(0); 
 }
// ************************************************************************
 String selectMktCashbackDtlQueue(String tranSeqno) throws Exception
 {
  sqlStr = "select "
         + "link_seqno as queu_link_seqno "
         + "from mkt_cashback_dtl "
         + "where  tran_seqno = '" + tranSeqno +"' ";

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) return("");

  return(colStr("queu_link_seqno"));
 }
  // ************************************************************************

} // End of class CommCashback

