/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 108/07/25  V1.00.07  Allen Ho                                              *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-09-23  V1.00.02  Zuwei       修改SQL Injection Issue
* 109-12-25   V1.00.03 Justin         parameterize sql
* 111-11-25  V1.00.04  Zuwei       Sync from mega                              *
* 111-12-29  V1.00.05  Zuwei       updateMktBonusDtl 參數不匹配 
******************************************************************************/
package busi.ecs;

// import taroko.com.TarokoCommon;
// import java.sql.Connection;
// import busi.sqlcond;
import java.lang.Math;
import java.lang.Math;

public class MktBonus extends busi.FuncBase {
  // 已下3欄位只有需要知充抵應稅即免稅多少時用

  public int hCommTranBp = 0;
  public int hCommTranBpTax = 0;
  public int tranBpTax = 0;
  public String aprUser     = "";    
  public int tranBpNotax = 0;

  String sqlStr = "";
  String hMbdlPseqno = "";
  String hMbdlBonusType = "";
  String hBklgTaxFlag = "";
  String hBklgTranSeqno = "";
  String hMbdlTaxFlag = "";
  String hMbdlTranCode = "";
  int hMbdlEndTranBp = 0;
  int hMbdlTranBp = 0;
  int hMbdlPTranBp = 0;
  int hMbdlMTranBp = 0;
  int hBklgTranBp = 0;
  int    linkTranBp      = 0;
  int    linkTaxTranBp  = 0;
  int    mainTranBp      = 0;
  int    rTaxTranBp     = 0;
  String linkSeqno = "";
  String mainSeqno = "";

  String[] dbname = new String[10];
  int intq=0;
  // ************************************************************************
  public int bonusFunc() {
    sqlStr =
        "select max(tran_seqno) as tran_seqno  " 
			+ "from mkt_bonus_dtl "
            + "where  end_tran_bp != 0 " 
			+ "group by p_seqno,bonus_type "
            + "having sum(decode(sign(end_tran_bp),1,end_tran_bp,0))!=0 "
            + "and    sum(decode(sign(end_tran_bp),-1,end_tran_bp,0))!=0 ";

    busi.DataSet dsf = new busi.DataSet();
    dsf.colList = this.sqlQuery(sqlStr, new Object[] {});

    for (int inti = 0; inti < dsf.listRows(); inti++) {
      dsf.listFetch(inti);

      bonusFunc(dsf.colStr("tran_seqno"));
    }

    return (0);
  }
// ************************************************************************
 public  int bonusFuncP(String pSeqno) throws Exception {
  sqlStr = "select tran_seqno  "
         + "from mkt_bonus_dtl "
         + "where  end_tran_bp != 0 "
         + "and    p_seqno = '" + pSeqno +"' "
         + "order by end_tran_bp "
         ;

  busi.DataSet dsf =new busi.DataSet();
  dsf.colList =this.sqlQuery(sqlStr, new Object[]{});

  for (int inti=0; inti<dsf.listRows(); inti++) 
   {

     dsf.listFetch(inti);

     bonusFunc(dsf.colStr("tran_seqno"));
   }

  return(0);
 }
// ************************************************************************
 void initData() 
 {
  hCommTranBp      = 0;
  hCommTranBpTax  = 0;
  tranBpTax  = 0;
  tranBpNotax = 0;

  return;
 }

  // ************************************************************************
  public int bonusFunc(String tranSeqno) {
    sqlStr =
        "select "
	        + "end_tran_bp as end_tran_bp_q, "
	     	+ "tax_tran_bp as tax_tran_bp_q, "
	        + "p_seqno as p_seqno, "
	        + "tran_code as tran_code, "
	        + "bonus_type as bonus_type, "
	        + "tax_flag as tax_flag, "
	        + "mod_seqno as mod_seqno_q, "
	        + "hex(rowid) as rowid_q "
	        + "from mkt_bonus_dtl "
	        + "where  tran_seqno = ? ";

    this.sqlSelect(sqlStr, new Object[] {tranSeqno});

    if (sqlRowNum == 0)
      return (1);

    hBklgTranSeqno = tranSeqno;
    hMbdlPseqno = colStr("p_seqno");
    hMbdlTranCode = colStr("tran_code");
    hMbdlBonusType = colStr("bonus_type");
    hMbdlTaxFlag = colStr("tax_flag");
    hMbdlEndTranBp = (int) colNum("end_tran_bp_q");

  mainSeqno = tranSeqno;
  mainTranBp = 0;
    if (hMbdlEndTranBp > 0)
      hBklgTaxFlag = hMbdlTaxFlag;
    hMbdlTranBp = selectMktBonusDtlM(hMbdlEndTranBp);

  linkSeqno = "";
  linkTranBp = 0;
   if ((mainSeqno.length()!=0)&&
       (mainTranBp!=0))
      {
       linkSeqno = mainSeqno;
       linkTranBp = mainTranBp;
      }

    if (hMbdlTranBp != hMbdlEndTranBp) {
      if (hMbdlEndTranBp < 0)
         updateMktBonusDtl(colStr("rowid_q"),colNum("mod_seqno_q"),
                              (int)colNum("tax_tran_bp_q")+tranBpTax*-1);
      else
        updateMktBonusDtl(colStr("rowid_q"), colNum("mod_seqno_q"));
    }

    return (0);
  }

  // ************************************************************************
  public int selectMktBonusDtlM(int tempTranBp) {
    int oriTempTranBp = tempTranBp;
    sqlStr = " select "
        + " mod_seqno, "
        + " tran_seqno, "
        + " tran_code, "
        + " end_tran_bp, "
        + " effect_e_date, "
        + " tax_flag, "
        + " hex(rowid) as rowid_a "
        + " from mkt_bonus_dtl "
        + " where end_tran_bp  != 0 "
        + "and    tran_seqno   != ? "
        + "and    apr_flag      = 'Y' "
        + " and   bonus_type    = ? "
        + " and   p_seqno       = ? "
        + " and   decode(sign(end_tran_bp),1,1,0,3,-1) = decode(sign(?),-1,1,0,4,-1) ";

    if (tempTranBp < 0) {
      if (hMbdlTaxFlag.equals("Y"))
        sqlStr =
            sqlStr + " order by  decode(tax_flag,'Y',1,2),"
                + "decode(effect_e_date,'','99999999',effect_e_date)," 
				+ "tran_date,tran_time ";
      else if (hMbdlTaxFlag.equals("N"))
        sqlStr =
            sqlStr + " order by  decode(tax_flag,'Y',2,1),"
                + "decode(effect_e_date,'','99999999',effect_e_date)," 
				+ "tran_date,tran_time ";
      else
        sqlStr =
            sqlStr + " order by  decode(effect_e_date,'','99999999',effect_e_date),"
                + "tax_flag,tran_date,tran_time ";
    } else {
      if (hMbdlTaxFlag.equals("Y"))
        sqlStr =
            sqlStr + " order by  decode(tax_flag,'Y',1,2),"
                + "decode(effect_e_date,'','99999999',effect_e_date)," 
				+ "tran_date,tran_time ";
      else
        sqlStr =
            sqlStr + " order by  decode(effect_e_date,'','99999999',effect_e_date),"
                + "tax_flag,tran_date,tran_time ";
    }

    // wp.ddd("STEP 001-001 =["+sqlStr+"]");
    busi.DataSet ds1 = new busi.DataSet();
    ds1.colList = this.sqlQuery(sqlStr, new Object[] {hBklgTranSeqno, hMbdlBonusType, hMbdlPseqno, tempTranBp});

    if (sqlRowNum == 0)
      return (tempTranBp);

    hCommTranBp = 0;
    hCommTranBpTax = 0;
    hMbdlPTranBp = 0;
    hMbdlMTranBp = 0;
    int thisTaxBp = 0;

    int rowNum = sqlRowNum;
    for (int inti = 0; inti < rowNum; inti++) {
      thisTaxBp = 0;
      ds1.listFetch(inti);

      if (tempTranBp < 0)
        hBklgTaxFlag = ds1.colStr("tax_flag");

      if (hBklgTaxFlag.equals("Y")) {
        hCommTranBpTax = hCommTranBpTax 
						+ Math.abs(tempTranBp + (int) ds1.colNum("end_tran_bp"));
      } else {
        hCommTranBp = hCommTranBp 
						+ Math.abs(tempTranBp + (int) ds1.colNum("end_tran_bp"));
      }

      if (tempTranBp > 0) {
        hMbdlPTranBp = tempTranBp;
        hMbdlMTranBp = (int) ds1.colNum("end_tran_bp");
      } else {
        hMbdlMTranBp = tempTranBp;
        hMbdlPTranBp = (int) ds1.colNum("end_tran_bp");
      }

      if (tempTranBp > 0) {
        if (hMbdlPTranBp + hMbdlMTranBp > 0) {
          if (hMbdlTaxFlag.equals("Y")) {
            tranBpTax = tranBpTax + hMbdlTranBp;
            thisTaxBp = hMbdlTranBp;
          } else
            tranBpNotax = tranBpNotax + hMbdlTranBp;
          hMbdlTranBp = 0;
          tempTranBp = hMbdlPTranBp + hMbdlMTranBp;
        } else {
          if (hMbdlTaxFlag.equals("Y")) {
            tranBpTax = tranBpTax + tempTranBp;
            thisTaxBp = tempTranBp;
          } else
            tranBpNotax = tranBpNotax + tempTranBp;
          hMbdlTranBp = hMbdlPTranBp + hMbdlMTranBp;
          tempTranBp = 0;
        }
      } else {
        if (hMbdlPTranBp + hMbdlMTranBp < 0) {
          if (ds1.colStr("tax_flag").equals("Y")) {
            tranBpTax = tranBpTax - hMbdlPTranBp;
            thisTaxBp = hMbdlPTranBp;
          } else
            tranBpNotax = tranBpNotax - hMbdlPTranBp;
          hMbdlTranBp = 0;
          tempTranBp = hMbdlPTranBp + hMbdlMTranBp;
        } else {
          if (ds1.colStr("tax_flag").equals("Y")) {
            tranBpTax = tranBpTax + hMbdlMTranBp;
            thisTaxBp = hMbdlMTranBp;
          } else
            tranBpNotax = tranBpNotax + hMbdlMTranBp;

          hMbdlTranBp = hMbdlPTranBp + hMbdlMTranBp;
          tempTranBp = 0;
        }

        /*
         * this web version , skip ,because i don't what version is right if (h_mbdl_p_tran_bp +
         * h_mbdl_m_tran_bp<0) { if (ds1.col_ss("tax_flag").equals("Y")) { tran_bp_tax = tran_bp_tax
         * + h_mbdl_tran_bp; this_tax_bp = h_mbdl_tran_bp; } else tran_bp_notax = tran_bp_notax +
         * h_mbdl_tran_bp; h_mbdl_tran_bp = 0; temp_tran_bp = h_mbdl_p_tran_bp + h_mbdl_m_tran_bp; }
         * else { if (ds1.col_ss("tax_flag").equals("Y")) { tran_bp_tax = tran_bp_tax +
         * temp_tran_bp; this_tax_bp = temp_tran_bp; } else tran_bp_notax = tran_bp_notax +
         * temp_tran_bp; h_mbdl_tran_bp = h_mbdl_p_tran_bp + h_mbdl_m_tran_bp; temp_tran_bp = 0; }
         */
      }

     if (hMbdlTranBp==0)
        {
         linkTranBp = (int)ds1.colNum("end_tran_bp")
                      - hMbdlTranBp;
         linkSeqno = mainSeqno;
        }
     else
        {
         linkTranBp = 0;
         linkSeqno = "";
         mainTranBp = 0 - ((int)ds1.colNum("end_tran_bp") - hMbdlTranBp);
         mainSeqno = ds1.colStr("tran_seqno");
        }

     if (oriTempTranBp>0) 
          updateMktBonusDtl(ds1.colStr("rowid_a"),(int)ds1.colNum("mod_seqno"),thisTaxBp);
     else
          updateMktBonusDtl(ds1.colStr("rowid_a"),(int)ds1.colNum("mod_seqno"));

     if (tempTranBp==0) break;
    }

    return (tempTranBp);
  }

  // ************************************************************************
  public int updateMktBonusDtl(String tempRowid, double tempModSeqno) {
    return (updateMktBonusDtl(tempRowid, tempModSeqno, 0));
  }

  // ************************************************************************
  public int updateMktBonusDtl(String tempRowid, double tempModSeqno, int taxBp) {
    dateTime();

    sqlStr =
        "update mkt_bonus_dtl set "
        + "end_tran_bp = ?, "
        + "tax_tran_bp = ?, "
         + "link_seqno   = ?, "
         + "link_tran_bp = ?, "
        + "mod_pgm     = ?, "
        + "mod_seqno   = mod_seqno + 1 , "
        + "mod_time    = timestamp_format(?,'yyyymmddhh24miss') "
        + "where hex(rowid) = ? "
        + " AND mod_seqno   = ? ";

    Object[] param =
        new Object[] {
        hMbdlTranBp, 
        taxBp, 
     linkSeqno,
     linkTranBp,
        wp.modPgm(), 
        wp.sysDate + wp.sysTime, 
        tempRowid,
        tempModSeqno
        };

    sqlExec(sqlStr, param);
    if (sqlRowNum <= 0)
      errmsg(this.sqlErrtext);

    return (0);
  }

  // ************************************************************************
  public double bonusSum(String pSeqno) {
    return (bonusSum(pSeqno, "BONU"));
  }

  // ************************************************************************
  public double bonusSum(String pSeqno, String bonusType) {
    sqlStr =
        "select "
        + "sum(end_tran_bp + res_tran_bp) as end_tran_bp "
        + "from mkt_bonus_dtl "
        + "where  p_seqno    = ? "
        + "and    bonus_type = ? ";;

    this.sqlSelect(sqlStr, new Object[] {pSeqno, bonusType});

    wp.log("STEP C =[" + sqlRowNum + "]");
    if (sqlRowNum == 0)
      return (0);
    return (colNum("end_tran_bp"));
  }
  
  // ************************************************************************
  public int checkCurrentCode0(String idNo , String type) {
	if("01".equals(type)) {
	    sqlStr = "select count(*) card_cnt from crd_card a join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
	    sqlStr += " where a.acct_type = '01' and a.current_code = '0' and a.sup_flag ='0' and b.id_no = ? ";
	}
	if("90".equals(type)) {
		sqlStr = " select count(*) card_cnt from dbc_card a join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
		sqlStr += " where a.acct_type ='90' and a.current_code = '0' and b.id_no = ? ";
	}
    this.sqlSelect(sqlStr, new Object[] {idNo});
    
    return colInt("card_cnt");
  }
  
  /**
   * 取得acct_type(01)點數
   * @param idNo
   * @return
   */
  public int getEndTranBp01(String idNo) {
	  int sumBp01 = 0;
	  if(empty(idNo)) {
		  return sumBp01;
	  }

    sqlStr =
        "select "
        + "sum(end_tran_bp + res_tran_bp) as end_tran_bp01 "
        + "FROM mkt_bonus_dtl a , crd_idno b "
        + "where  a.id_p_seqno = b.id_p_seqno "
        + "AND a.acct_type = '01' "
        + "and b.id_no    = ? "
        + "AND a.BONUS_TYPE ='BONU' and a.APR_FLAG = 'Y' "
    	+ "and decode(effect_e_date,'','99999999',effect_e_date) >= to_char(sysdate,'yyyymmdd') ";

    this.sqlSelect(sqlStr, new Object[] {idNo});

    wp.log("STEP C =[" + sqlRowNum + "]");
    if (sqlRowNum == 0)
      return sumBp01;
    sumBp01 = colInt("end_tran_bp01");

    int crdCardCnt = checkCurrentCode0(idNo,"01");
	if(crdCardCnt == 0) {
		if(sumBp01 >= 0)
			return 0;
	}
	
    return sumBp01;
  }
  
  /****
   * 取得acct_type(90)點數
   * @param idNo
   * @return
   */
  public int getEndTranBp90(String idNo) {
	int sumBp90 = 0;
	if(empty(idNo)) {
	   return sumBp90;
	}
    sqlStr =
        "select "
        + "sum(end_tran_bp) as end_tran_bp90 "
        + "FROM dbm_bonus_dtl a , dbc_idno b "
        + "where  a.id_p_seqno = b.id_p_seqno "
        + "AND a.acct_type = '90' "
        + "and b.id_no    = ? "
        + "AND a.BONUS_TYPE ='BONU' and a.APR_FLAG = 'Y' "
    	+ "and decode(effect_e_date,'','99999999',effect_e_date) >= to_char(sysdate,'yyyymmdd') ";

    this.sqlSelect(sqlStr, new Object[] {idNo});

    wp.log("STEP C =[" + sqlRowNum + "]");
    if (sqlRowNum == 0)
      return sumBp90;
    sumBp90 = colInt("end_tran_bp90");

	int dbcCardCnt = checkCurrentCode0(idNo,"90");
	
	if(dbcCardCnt == 0) {
		if(sumBp90 >=0 )
			return 0;
	}
    
    return sumBp90;
  }

  /***
   * @param nowPt01 Credit Card點數
   * @param nowPt90 VisaDebit 點數
   * @param needPt 兌換點數
   * @return DEDUCT_BP01,DEDUCT_BP90
   */
	public int[] countDeductBp(int nowPt01, int nowPt90, int needPt) {
		int[] deductBp = { 0, 0 };
		int nowPt = nowPt01 + nowPt90;// 可用點數
		int needPtTmp = 0;
		if (nowPt < needPt) {
			return null;
		}
		if (nowPt01 >= 0 && nowPt90 >= 0) {
			needPtTmp = needPt - nowPt01;
			if (needPtTmp >= 0) {
				deductBp[0] = nowPt01 * -1;
				deductBp[1] = needPtTmp * -1;
			} else {
				deductBp[0] = needPt * -1;
				deductBp[1] = 0;
			}
		}
		if (nowPt01 > 0 && nowPt90 < 0) {
			deductBp[0] = needPt * -1;
			deductBp[1] = 0;
		}
		if (nowPt01 < 0 && nowPt90 > 0) {
			deductBp[0] = 0;
			deductBp[1] = needPt * -1;
		}
		if (nowPt01 < 0 && nowPt90 < 0) {
			return null;
		}
		return deductBp;
	}
  
  // ************************************************************************
  public int insertMktBonusBklog(int bklgTranBp, String pTranSeqno, String pTranCode,
      String effectEDate) {
        String lsIns = "insert into mkt_bonus_bklog ( "
                + " p_tran_seqno,  "
                + " m_tran_seqno, "
                + " p_seqno,"
                + " p_tran_bp,"
                + " m_tran_bp,"
                + " tran_bp,"
                + " tax_flag,"
                + " p_tran_code,"
                + " m_tran_code,"
                + " effect_e_date,"
                + " mod_time,"
                + " mod_pgm"
                + ")values(  "
                + " :p_tran_seqno,  "
                + " :m_tran_seqno, "
                + " :p_seqno,"
                + " :p_tran_bp,"
                + " :m_tran_bp,"
                + " :tran_bp,"
                + " :tax_flag,"
                + " :p_tran_code,"
                + " :m_tran_code,"
                + " :effect_e_date,"
                + " sysdate,"
                + " :mod_pgm)";

    setString("p_tran_seqno", pTranSeqno);
    setString("m_tran_seqno", hBklgTranSeqno);
    setString("p_seqno", hMbdlPseqno);
    setInt("p_tran_bp", hMbdlPTranBp);
    setInt("m_tran_bp", hMbdlMTranBp);
    setInt("tran_bp", bklgTranBp);
    setString("tax_flag", hBklgTaxFlag);
    setString("p_tran_code", pTranCode);
    setString("m_tran_code", hMbdlTranCode);
    setString("effect_e_date", effectEDate);
    setString("mod_pgm", wp.modPgm());

    sqlExec(lsIns);

    return (0);
  }
  // ************************************************************************
 public int bonusReverse(String tranSeqno) throws Exception
 {
  intq=0;
  String[] tranQueue = new String[10];
  tranQueue[0] = tranSeqno;
  for (;;)
    {
     tranQueue[intq+1] = selectMktBonusDtlQueue(tranQueue[intq]);
     if (tranQueue[intq+1].length()==0) break;
     intq++;
    }

  for (int inti=intq;inti>=0;inti--)
    { 
     bonusReverseM(tranQueue[inti],inti);
    }

  return(0);
 }
// ************************************************************************
 public int bonusReverseM(String tranSeqno,int qCnt) throws Exception
 {
  sqlStr = "select "
         + "active_code as r_active_code,"
         + "active_name as r_active_name,"
         + "acct_date   as r_acct_date,"
         + "beg_tran_bp as r_beg_tran_bp,"
         + "end_tran_bp as r_end_tran_bp,"
         + "tax_tran_bp as r_tax_tran_bp,"
         + "res_tran_bp as r_res_tran_bp,"
         + "tran_seqno  as r_tran_seqno,"
         + "acct_type   as r_acct_type, "
         + "mod_desc    as r_mod_desc, "
         + "p_seqno     as r_p_seqno, "
         + "id_p_seqno  as r_id_p_seqno, "
         + "bonus_type  as r_bonus_type, "
         + "tax_flag    as r_tax_flag, "
         + "link_seqno  as r_link_seqno, "
         + "link_tran_bp as r_link_tran_bp,"
         + "mod_seqno    as r_mod_seqno, "
         + "hex(rowid) as r_rowid "
         + "from mkt_bonus_dtl "
         + "where  tran_seqno = '" + tranSeqno +"' "
         ;

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) return(0);

  mainTranBp = 0;
  linkTaxTranBp = 0;

  if (colStr("r_link_seqno").length()>0)
     {
      if ((int)colNum("r_beg_tran_bp")<0)
         {
          if ((int)colNum("r_beg_tran_bp")>(int)colNum("r_link_tran_bp"))
              colSet("r_link_tran_bp",String.format("%d",(int)colNum("r_beg_tran_bp")));
         }
      else
         {
          if ((int)colNum("r_beg_tran_bp")<(int)colNum("r_link_tran_bp"))
              colSet("r_link_tran_bp",String.format("%d",(int)colNum("r_beg_tran_bp")));
         }
     }

  if ((int)colNum("r_beg_tran_bp") == 
      (int)colNum("r_end_tran_bp") + (int)colNum("r_res_tran_bp")) return(0);
  selectMktBonusDtlR1();

  if (colStr("r_link_seqno").length()!=0)
      selectMktBonusDtlR2();

  if (intq==qCnt)
     updateMktBonusDtlR2((int)colNum("r_end_tran_bp")+mainTranBp,
                          (int)colNum("r_tax_tran_bp")-linkTaxTranBp);
  else
     updateMktBonusDtlR2((int)colNum("r_beg_tran_bp"),
                          (int)colNum("r_tax_tran_bp")-linkTaxTranBp);

  if (intq!=qCnt)
  if ((int)colNum("r_beg_tran_bp") - (int)colNum("r_end_tran_bp") 
                                    - (int)colNum("r_res_tran_bp") != mainTranBp)
     {
     if ((int)colNum("r_beg_tran_bp") <0)
        {
         if (colStr("r_active_code").length()==0)
             colSet(0,"active_code", colStr("r_acct_date").substring(0,4)
                                    + colStr("r_bonus_type")
                                    + colStr("r_acct_type")
                                    + "1");
         else colSet(0,"active_code", colStr("r_active_code"));

         selectVmktBonusActiveName();
        }

      int begTranBp = (int)colNum("r_beg_tran_bp")
                      - (int)colNum("r_end_tran_bp")
                      - mainTranBp;

      int taxTranBp =  (int)colNum("r_tax_tran_bp")
                      - linkTaxTranBp;

      colSet(0,"tax_flag" , "N");
      if (taxTranBp!=0) colSet(0,"tax_flag" , "Y");

      if ((begTranBp==0)||  
          (begTranBp + taxTranBp==0))
         {
          insertMktBonusDtl(begTranBp*-1,taxTranBp);
         }
      else
         {
          if ((int)colNum("r_beg_tran_bp")>0)
             {
              insertMktBonusDtl(begTranBp*-1,0);
             }
          else
             {
              if (taxTranBp!=0)
                 insertMktBonusDtl(taxTranBp,taxTranBp);
              colSet(0,"tax_flag"    , "N");
              insertMktBonusDtl((begTranBp + taxTranBp)*-1,0);
             }
         }
     }
  return(0);
 }
// ************************************************************************
 int selectMktBonusDtlR1() throws Exception
 {
  sqlStr = "select "
         + "mod_seqno    as com_mod_seqno, "
         + "tran_seqno   as com_tran_seqno,"
         + "link_seqno   as com_link_seqno,"
         + "link_tran_bp as com_link_tran_bp,"
         + "tax_flag     as com_tax_flag, "
         + "tax_tran_bp  as  com_tax_tran_bp, "
         + "beg_tran_bp  as com_beg_tran_bp, "
         + "end_tran_bp  as com_end_tran_bp, "
         + "hex(rowid)   as com_rowid "
         + "from mkt_bonus_dtl "
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
         if ((int)ds1.colNum("com_beg_tran_bp")<0)
            {
             if ((int)ds1.colNum("com.beg_tran_bp")>(int)ds1.colNum("com_link_tran_bp"))
                 colSet(inti,"com_link_tran_bp",String.format("%d",(int)ds1.colNum("com_beg_tran_bp")));
            }
         else
            {
             if ((int)ds1.colNum("com_beg_tran_bp")<(int)ds1.colNum("com_link_tran_bp"))
                 colSet(inti,"com_link_tran_bp",String.format("%d",(int)ds1.colNum("com_beg_tran_bp")));
            }
        }

     mainTranBp = mainTranBp - (int)ds1.colNum("com_link_tran_bp");

     if (colNum("r_beg_tran_bp")<0)
     if (ds1.colStr("com_tax_flag").equals("Y"))
        {
         linkTaxTranBp = linkTaxTranBp 
                          + (int)ds1.colNum("com_link_tran_bp");
        }
     colSet(0,"end_tran_bp"  , (int)ds1.colNum("com_end_tran_bp")
                              + (int)ds1.colNum("com_link_tran_bp"));

     rTaxTranBp = 0;
     if ((int)ds1.colNum("com_beg_tran_bp")<0)
     if (colStr("r_tax_flag").equals("Y"))
        {
         rTaxTranBp = (int)ds1.colNum("com_tax_tran_bp") 
                       + (int)ds1.colNum("com_link_tran_bp");  
        }
                                 
     updateMktBonusDtlR1(ds1.colStr("com_rowid"),ds1.colNum("com_mod_seqno"));
    }

  return(0); 
 }
// ************************************************************************
 int selectMktBonusDtlR2() throws Exception
 {
  sqlStr = "select "
         + "mod_seqno    as com_mod_seqno, "
         + "link_seqno   as com_link_seqno,"
         + "link_tran_bp as com_link_tran_bp,"
         + "tax_flag     as com_tax_flag, "
         + "tax_tran_bp  as com_tax_tran_bp, "
         + "beg_tran_bp  as com_beg_tran_bp, "
         + "end_tran_bp  as com_end_tran_bp, "
         + "hex(rowid)   as com_rowid "
         + "from mkt_bonus_dtl "
         + "where  tran_seqno = '" + colStr("r_link_seqno") +"' "
         + "and    p_seqno    = '" + colStr("r_p_seqno") +"' "
         ;

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) return(0);
  int recCnt = sqlRowNum;

  if (colStr("com_link_seqno").length()>0)
     {
      if ((int)colNum("com_beg_tran_bp")<0)
         {
          if ((int)colNum("com.beg_tran_bp")>(int)colNum("com_link_tran_bp"))
              colSet("com_link_tran_bp",String.format("%d",(int)colNum("com_beg_tran_bp")));
         }
      else
         {
          if ((int)colNum("com_beg_tran_bp")<(int)colNum("com_link_tran_bp"))
              colSet("com_link_tran_bp",String.format("%d",(int)colNum("com_beg_tran_bp")));
         }
     }
  mainTranBp = mainTranBp + (int)colNum("r_link_tran_bp");

  if (colNum("r_beg_tran_bp")<0)
  if (colStr("com_tax_flag").equals("Y"))
     {
      linkTaxTranBp = linkTaxTranBp 
                       - (int)colNum("r_link_tran_bp");
     }
  colSet(0,"end_tran_bp"  , String.format("%d"
                           , (int)colNum("com_end_tran_bp")
                           - (int)colNum("r_link_tran_bp")));

  rTaxTranBp = 0;
  if ((int)colNum("com_beg_tran_bp")<0)
  if (colStr("r_tax_flag").equals("Y"))
     {
      rTaxTranBp = (int)colNum("com_tax_tran_bp") 
                    + (int)colNum("com_link_tran_bp");  
     }
  updateMktBonusDtlR1(colStr("com_rowid"),colNum("com_mod_seqno"));

  return(0); 
 }
// ************************************************************************
 int updateMktBonusDtlR1(String tempRowid,double modSeqno) throws Exception
 {
  sqlStr = "update mkt_bonus_dtl set " 
         + "end_tran_bp  = ?, "
         + "link_seqno   = '',"
         + "tax_tran_bp  = ?, "
         + "link_tran_bp = 0, "
         + "mod_pgm      = ?, "
         + "mod_seqno    = mod_seqno + 1 , "
         + "mod_time     = sysdate " 
         + "where rowid = x'" + tempRowid +"' "
         + "AND mod_seqno   = "+ modSeqno+" "; 

  Object[] param =new Object[]
    {
     (int)colNum("end_tran_bp"),
     rTaxTranBp,
     wp.modPgm(),
    };

  sqlExec(sqlStr , param);
  if (sqlRowNum <= 0) errmsg(this.sqlErrtext);

  return(0);
 }
// ************************************************************************
 int updateMktBonusDtlR2(int endTranBp,int taxTranBp) throws Exception
 {
  sqlStr = "update mkt_bonus_dtl set " 
         + "end_tran_bp  = ?, "
         + "tax_tran_bp  = ?, "
         + "link_seqno   = '',"
         + "link_tran_bp = 0, "
         + "mod_pgm      = ?, "
         + "mod_seqno    = mod_seqno + 1 , "
         + "mod_time     = sysdate " 
         + "where rowid  = x'" + colStr("r_rowid") +"' "
         + "AND mod_seqno = " +  colStr("r_mod_seqno") + " "; 

  Object[] param =new Object[]
    {
     endTranBp,
     taxTranBp,
     wp.modPgm(),
    };

  sqlExec(sqlStr , param);
  if (sqlRowNum <= 0) errmsg(this.sqlErrtext);

  return(0);
 }
// ************************************************************************
 int insertMktBonusDtl(int endTranBp,int taxTranBp) throws Exception
 {
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

  String effectEDate = "";
  if ((int)colNum("parm_effect_months")>0)
     effectEDate = comm.nextMonthDate(colStr("r_acct_date")
                   , (int)colNum("parm_effect_months"));
  String lsIns = "";
  sqlStr= " insert into mkt_bonus_dtl("
          + " acct_type, "
          + " bonus_type, "
          + " active_code, "
          + " active_name, "
          + " tran_code, "
          + " beg_tran_bp, "
          + " end_tran_bp, "
          + " tax_tran_bp, "
          + " tax_flag, "
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
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,"     // last: tran_time
          + "?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "timestamp_format(?,'yyyymmddhh24miss'),?,?,?)";

  Object[] param =new Object[]
       {
        colStr("r_acct_type"),
        colStr("r_bonus_type"),
        colStr("r_active_code"),
        colStr("r_active_name")+ "沖回調整",           
        "3",
        endTranBp,
        endTranBp,
        taxTranBp,
        colStr("tax_flag"),      
        effectEDate,   
        colStr("r_mod_desc"),        
        "刪除交易序號:["+colStr("r_tran_seqno")+"]",
        wp.sysDate ,
        wp.sysTime,
        colStr("r_p_seqno"),
        colStr("r_id_p_seqno"),
        wp.modPgm(),
        comr.getSeqno("MKT_MODSEQ"),
        comr.getBusinDate().substring(0,6),
        comr.getBusinDate(),
        "Y",
        aprUser,
        wp.sysDate,
        wp.loginUser,
        wp.sysDate + wp.sysTime,
        wp.loginUser,
        wp.modPgm(),
        0
       };

  sqlExec(sqlStr, param);
  if (sqlRowNum <= 0) errmsg(sqlErrtext);

  return(1);
 }
// ************************************************************************
 int selectVmktBonusActiveName() throws Exception
 {
  sqlStr = "select "
         + "active_name   as parm_active_name,"
         + "effect_months as parm_effect_months,"
         + "from vmkt_bonus_active_name "
         + "where  active_code = '" + colStr("active_code") +"' ";

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) 
     {
      colSet("parm.effect_months" ,"36");
     } 

  return(0); 
 }
// ************************************************************************
 String selectMktBonusDtlQueue(String tranSeqno) throws Exception
 {
  sqlStr = "select "
         + "link_seqno as queu_link_seqno "
         + "from mkt_bonus_dtl "
         + "where  tran_seqno = '" + tranSeqno +"' ";

  this.sqlSelect(sqlStr);

  if (sqlRowNum==0) return("");

  return(colStr("queu_link_seqno"));
 }
// ************************************************************************


}   // End of class Commbonus