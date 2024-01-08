/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/11/10  V1.00.03  Allen Ho                                              *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-12-25   V1.00.02 Justin        parameterize sql
* 111-11-25  V1.00.03  Zuwei       Sync from mega                              *
******************************************************************************/
package busi.ecs;

// import taroko.com.TarokoCommon;
// import java.sql.Connection;
// import busi.sqlcond;
import java.lang.Math;
import java.math.BigDecimal;

public class MktDCCashback extends busi.FuncBase {
  // 已下3欄位只有需要知充抵應稅即免稅多少時用

  String sqlStr = "";
  public double hCommTranAmt = 0;
  String hMcdlPseqno = "";
  String hMcdlCurrCode = "";
  String hMcdlFundCode  = "";
  double hMcdlEndTranAmt = 0;
  double hMcdlTranAmt = 0;
  double linkTranAmt     = 0;
  double mainTranAmt     = 0;
  String linkSeqno = "";
  String mainSeqno = "";

  String[] dbname = new String[10];

  // ************************************************************************
  public double cashbackFunc() {
    sqlStr =
        "select max(tran_seqno) as tran_seqno  " 
			+ "from cyc_dc_fund_dtl "
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
 public  double cashbackFuncP(String pSeqno) throws Exception {
  sqlStr = "select tran_seqno  "
         + "from  cyc_dc_fund_dtl "
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
 void initData()
 {
  hCommTranAmt      = 0;

  return;
 }
  // ************************************************************************
  public double cashbackFunc(String tranSeqno) {
    sqlStr =
        "select " + "end_tran_amt as end_tran_amt_q, " 
		+ "p_seqno as p_seqno, "
        + "fund_code as fund_code, "
        + "curr_code as curr_code, " 
		+ "mod_seqno as mod_seqno_q, " 
		+ "hex(rowid) as rowid_q "
        + "from cyc_dc_fund_dtl " 
		+ "where  tran_seqno = ? ";

    this.sqlSelect(sqlStr, new Object[] {tranSeqno});


    if (sqlRowNum == 0)
      return (1);

    hMcdlPseqno = colStr("p_seqno");
    hMcdlCurrCode = colStr("curr_code");
    hMcdlFundCode    =  colStr("fund_code");
    hMcdlEndTranAmt = (int) colNum("end_tran_amt_q");

    mainSeqno = tranSeqno;
    mainTranAmt = 0;
    hMcdlTranAmt = selectCycDcFundDtlM(hMcdlEndTranAmt);

  linkSeqno = "";
  linkTranAmt = 0;
   if ((mainSeqno.length()!=0)&&
       (mainTranAmt!=0))
      {
       linkSeqno = mainSeqno;
       linkTranAmt = mainTranAmt;
      }

    if (hMcdlTranAmt != hMcdlEndTranAmt)
    	updateCycDcFundDtl(colStr("rowid_q"),colNum("mod_seqno_q"));

    return (0);
  }

  // ************************************************************************
  public double selectCycDcFundDtlM(double tempTranAmt) {
    sqlStr =
        " select "
            + " mod_seqno, "
            + " end_tran_amt, "
            + " hex(rowid) as rowid_a "
            + " from cyc_dc_fund_dtl "
            + " where  end_tran_amt  != 0 "
            + " and    curr_code     = ? "
            + " and    p_seqno       = ? "
            + " and    decode(sign(end_tran_amt),1,1,0,3,-1) = decode(sign(?),-1,1,0,4,-1) "
            + " order by  decode(effect_e_date,'','99999999',effect_e_date),tran_date,tran_time,tran_code desc";

    // wp.ddd("STEP 001-001 =["+sqlStr+"]");
    busi.DataSet ds1 = new busi.DataSet();
    ds1.colList = this.sqlQuery(sqlStr, new Object[] {hMcdlCurrCode, hMcdlPseqno, tempTranAmt});

    if (sqlRowNum == 0)
      return (tempTranAmt);

    hCommTranAmt = 0;
    double hMcdlPTranAmt = 0;
    double hMcdlMTranAmt = 0;

    // wp.ddd("STEP 001-002 =["+sql_nrow+"]");
    int rowNum = sqlRowNum;
    for (int inti = 0; inti < rowNum; inti++) {
      // wp.ddd("STEP 001-003 =["+inti+"]");
      ds1.listFetch(inti);
      // wp.ddd("STEP 001-004 =["+inti+"]");

     hCommTranAmt = commCurrAmt(hMcdlCurrCode, 
                                     hCommTranAmt
                                   + tempTranAmt 
                                   + ds1.colNum("end_tran_amt"),0);

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

     hMcdlTranAmt =  commCurrAmt(hMcdlCurrCode,hMcdlTranAmt,0);

     if (hMcdlTranAmt==0)
        {
         linkTranAmt = commCurrAmt(hMcdlCurrCode,
                                       ds1.colNum("end_tran_amt")
                                      - hMcdlTranAmt,0);
         linkSeqno = mainSeqno;
        }
     else
        {
         linkTranAmt = 0;
         linkSeqno    = "";
         mainTranAmt = commCurrAmt(hMcdlCurrCode, 
                                       0.0 
                                    - (ds1.colNum("end_tran_amt") 
                                    - hMcdlTranAmt),0);
         mainSeqno = ds1.colStr("tran_seqno");
        }

      updateCycDcFundDtl(ds1.colStr("rowid_a"), (int) ds1.colNum("mod_seqno"));

      // wp.ddd("STEP 001-005 =["+temp_tran_amt+"]["+h_mcdl_tran_amt+"]");
      if (tempTranAmt == 0)
        break;
      // wp.ddd("STEP 001-006 =["+temp_tran_amt+"]["+h_mcdl_tran_amt+"]");
    }

    return (tempTranAmt);
  }

  // ************************************************************************
 public int updateCycDcFundDtl(String tempRowid,double tempModSeqno) {
  dateTime();

  sqlStr = "update cyc_dc_fund_dtl set " 
         + "end_tran_amt  = ?, "
         + "link_seqno    = ?, "
         + "link_tran_amt = ?, "
         + "mod_pgm       = ?, "
         + "mod_seqno     = mod_seqno + 1 , "
         + "mod_time      = sysdate "
         + "where rowid   = x'" + tempRowid +"'"
         + "AND mod_seqno = ? "; 

  Object[] param =new Object[]
    {
     hMcdlTranAmt,
     linkSeqno,
     linkTranAmt,
     wp.modPgm(),
     tempModSeqno
    };

  sqlExec(sqlStr , param);
  if (sqlRowNum <= 0) errmsg(this.sqlErrtext);

  return(0);
 }
// ************************************************************************
 double  commCurrAmt(String currCode,double val,int rnd) {
  sqlStr = "select "
         + "curr_amt_dp as pcde_curr_amt_dp "
         + "from ptr_currcode "
         + "where  curr_code = '" + currCode +"' ";

  this.sqlSelect(sqlStr);

  val = val * 10000.0;
  val = Math.round(val);

  BigDecimal currAmt = new BigDecimal(val).divide(new BigDecimal("10000"));

  if (sqlRowNum==0)return(currAmt.doubleValue()); 

  double retNum = 0.0;
  if (rnd>0)  retNum = currAmt.setScale((int)colNum("pcde_curr_amt_dp"),BigDecimal.ROUND_UP).doubleValue(); 
  if (rnd==0) retNum = currAmt.setScale((int)colNum("pcde_curr_amt_dp"),BigDecimal.ROUND_HALF_UP).doubleValue(); 
  if (rnd<0)  retNum = currAmt.setScale((int)colNum("pcde_curr_amt_dp"),BigDecimal.ROUND_DOWN).doubleValue(); 

  return(retNum);
 }
// ************************************************************************
 public int cashbackReverse(String tranSeqno) throws Exception
 {
  int intq=0;
  String[] tranQueue = new String[10];
  tranQueue[0] = tranSeqno;
  for (;;)
    {
     tranQueue[intq+1] = selectCycDcFundDtlQueue(tranQueue[intq]);
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
         + "curr_code     as r_curr_code,"
         + "fund_code     as r_fund_code,"
         + "fund_name     as r_fund_name,"
         + "acct_date     as r_acct_date,"
         + "beg_tran_amt  as r_beg_tran_amt,"
         + "end_tran_amt  as r_end_tran_amt,"
         + "tran_seqno    as r_tran_seqno,"
         + "acct_type     as r_acct_type, "
         + "mod_desc      as r_mod_desc, "
         + "p_seqno       as r_p_seqno, "
         + "id_p_seqno    as r_id_p_seqno, "
         + "link_seqno    as r_link_seqno, "
         + "link_tran_amt as r_link_tran_amt,"
         + "mod_seqno     as r_mod_seqno, "
         + "hex(rowid)    as r_rowid "
         + "from cyc_dc_fund_dtl "
         + "where  tran_seqno = '" + tranSeqno +"' "
         ;

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) return(0);

  mainTranAmt     = 0;

  if (colStr("r_link_seqno").length()>0)
     {
      if (colNum("r_beg_tran_amt")<0)
         {
          if (colNum("r_beg_tran_amt")>colNum("r_link_tran_amt"))
              colSet("r_link_tran_amt", colStr("r_beg_tran_amt"));
         }
      else
         {
          if (colNum("r_beg_tran_amt")<colNum("r_link_tran_amt"))
              colSet("r_link_tran_amt",colStr("r_beg_tran_amt"));
         }
     }

  if (commCurrAmt(colStr("r_curr_code"),colNum("r_beg_tran_amt"),0) == 
      commCurrAmt(colStr("r_curr_code"),colNum("r_end_tran_amt"),0)) return(0);
  selectCycDcFundDtlR1();

  if (colStr("r_link_seqno").length()!=0)
      selectCycDcFundDtlR2();

  updateCycDcFundDtlR2(commCurrAmt(colStr("r_curr_code"), 
                                          colNum("r_beg_tran_amt"),0));

  if (commCurrAmt(colStr("r_curr_code"), 
                    colNum("r_beg_tran_amt") 
                  - colNum("r_end_tran_amt"),0) != commCurrAmt(colStr("r_curr_code"),  
                                                                  mainTranAmt,0))
     {
      selectVmktFundName();

      double begTranAmt = commCurrAmt(colStr("r_curr_code"),  
                                          colNum("r_beg_tran_amt")
                                        - colNum("r_end_tran_amt")
                                        - mainTranAmt,0);

      if (begTranAmt!=0)  
         insertCycDcFundDtl(begTranAmt*-1);
     }
  return(0);
 }
// ************************************************************************
 int selectCycDcFundDtlR1() throws Exception
 {
  sqlStr = "select "
         + "mod_seqno     as com_mod_seqno, "
         + "tran_seqno    as com_tran_seqno,"
         + "link_seqno    as com_link_seqno,"
         + "link_tran_amt as com_link_tran_amt,"
         + "beg_tran_amt  as com_beg_tran_amt, "
         + "end_tran_amt   as com_end_tran_amt, "
         + "hex(rowid)   as com_rowid "
         + "from cyc_dc_fund_dtl "
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
         if (ds1.colNum("com_beg_tran_amt")<0)
            {
             if (ds1.colNum("com.beg_tran_amt")>ds1.colNum("com_link_tran_amt"))
                colSet(inti,"com_link_tran_amt",ds1.colStr("com_beg_tran_amt"));
            }
         else
            {
             if (ds1.colNum("com_beg_tran_amt")<ds1.colNum("com_link_tran_amt"))
                colSet(inti,"com_link_tran_amt",ds1.colStr("com_beg_tran_amt"));
            }
        }

//   wp.ddd("tran_seqno ["+ ds1.col_ss("com_tran_seqno") +"]");
//   wp.ddd("link_amt   ["+ ds1.col_num("com_link_tran_amt") +"]");
     mainTranAmt = commCurrAmt(colStr("r_curr_code"), 
                                   mainTranAmt 
                                 - ds1.colNum("com_link_tran_amt"),0);

     colSet("end_tran_amt"  , commCurrAmt(colStr("r_curr_code"),  
                                             ds1.colNum("com_end_tran_amt")
                                           + ds1.colNum("com_link_tran_amt"),0));

     updateCycDcFundDtlR1(ds1.colStr("com_rowid"),ds1.colNum("com_mod_seqno"));
    }

  return(0); 
 }
// ************************************************************************
 int selectCycDcFundDtlR2() throws Exception
 {
  sqlStr = "select "
         + "mod_seqno     as com_mod_seqno, "
         + "link_seqno    as com_link_seqno,"
         + "link_tran_amt as com_link_tran_amt,"
         + "beg_tran_amt  as com_beg_tran_amt, "
         + "end_tran_amt  as com_end_tran_amt, "
         + "hex(rowid)    as com_rowid "
         + "from cyc_dc_fund_dtl "
         + "where  tran_seqno = '" + colStr("r_link_seqno") +"' "
         + "and    p_seqno    = '" + colStr("r_p_seqno") +"' "
         ;

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) return(0);
  int recCnt = sqlRowNum;

  if (colStr("com_link_seqno").length()>0)
     {
      if (colNum("com_beg_tran_amt")<0)
         {
          if (colNum("com.beg_tran_amt")>colNum("com_link_tran_amt"))
              colSet("com_link_tran_amt", colStr("com_beg_tran_amt"));
         }
      else
         {
          if (colNum("com_beg_tran_amt")<colNum("com_link_tran_amt"))
              colSet("com_link_tran_amt", colStr("com_beg_tran_amt"));
         }
     }
  mainTranAmt = commCurrAmt(colStr("r_curr_code"), 
                                 mainTranAmt 
                               + colNum("r_link_tran_amt"),0);

  colSet(0,"end_tran_amt" , commCurrAmt(colStr("r_curr_code"), 
                                           colNum("com_end_tran_amt")
                                         - colNum("r_link_tran_amt") ,0));

  updateCycDcFundDtlR1(colStr("com_rowid"),colNum("com_mod_seqno"));

  return(0); 
 }
// ************************************************************************
 int updateCycDcFundDtlR1(String tempRowid,double modSeqno) throws Exception
 {
  sqlStr = "update cyc_dc_fund_dtl set " 
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
     colNum("end_tran_amt"),
     wp.modPgm(),
    };

  sqlExec(sqlStr , param);
  if (sqlRowNum <= 0) errmsg(this.sqlErrtext);

  return(0);
 }
// ************************************************************************
 int updateCycDcFundDtlR2(double endTranAmt) throws Exception
 {
  sqlStr = "update cyc_dc_fund_dtl set " 
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
 int insertCycDcFundDtl(double endTranAmt) throws Exception
 {
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

  String effectEDate = "";

  if (colNum("effc_effect_months")>0)
     effectEDate = comm.nextMonthDate(colStr("r_acct_date")
                   , (int)colNum("effc_effect_months"));

  String lsIns = "";
  strSql= " insert into cyc_dc_fund_dtl("
          + " acct_type, "
          + " curr_code, "
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
          + "?,?,?,?,?,?,?,?,?,"       // last:apr_flag 
          + "?,?,?,?,"
          + "timestamp_format(?,'yyyymmddhh24miss'),?,?,?)";

  Object[] param =new Object[]
       {
        colStr("r_acct_type"),
        colStr("r_curr_code"),
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

  sqlExec(strSql, param);
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
      colSet("effc_effect_months" ,"0");
      return(1);
     }
      
  selectFundTable();

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

  if (sqlRowNum==0) 
      colSet("effc_effect_months" ,"0");

  return(0); 
 }
// ************************************************************************
 String selectCycDcFundDtlQueue(String tranSeqno) throws Exception
 {
  sqlStr = "select "
         + "link_seqno as queu_link_seqno "
         + "from cyc_dc_fund_dtl "
         + "where  tran_seqno = '" + tranSeqno +"' ";

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) return("");

  return(colStr("queu_link_seqno"));
 }
// ************************************************************************

}   // End of class CommCashback

