/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/15  V1.00.00               program initial                          *
*  108/12/27  V1.00.01    phopho     add int f_auth_query(String)             *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 112-02-17  V1.00.02  Ryan        增加 【年利率】【日利率】計算方法                                 *
* 112-03-30  V1.00.03  Ryan       getYearRateInterest,getDayRateInterest 改為四捨五入    *
******************************************************************************/

package busi.func;

import java.math.BigDecimal;

// import java.sql.Connection;
import busi.FuncBase;

public class ColFunc extends FuncBase {

  // public ColFunc(Connection con1) {
  // this.conn = con1;
  // }

  public int fAuthQuery(String asKey) throws Exception {
    return fAuthQuery(wp.modPgm(), asKey);
  }

  public int fAuthQuery(String asWinId, String asKey) throws Exception {
    // -win_id-
    String lsIdno = "", lsAcctKey = "", lsPaAcct = "", lsPaStop = "", lsPaCond = "", lsPaMcode =
        "";
    String liAcct2 = "", liAcct3 = "", liAcct4 = "", liStop = "";
    String lsUserId = wp.loginUser;

    // String sql_select = " select count(*) as ll_cnt "
    // + " from col_qry_data_auth "
    // + " where user_id = :ls_user_id "
    // + " and win_id = :ls_win_id "
    // + " and   run_flag='Y' ";
    // setString("ls_user_id",ls_user_id);
    // setString("ls_win_id",as_win_id);
    // sqlSelect(sql_select);
    // if(sql_nrow<=0){
    // alert_err("資料查詢權限: select COL_QRY_DATA_AUTH error; win_id="+as_win_id);
    // return -1;
    // }
    // if(sql_ss("ll_cnt").equals("0")){
    // return 1;
    // }

    String sqlSelect =
        " select count(*) as ll_cnt " + " from col_qry_data_auth "
            + " where user_id = :ls_user_id " + " and   run_flag='Y' ";
    setString("ls_user_id", lsUserId);
    sqlSelect(sqlSelect);
    if (sqlRowNum <= 0) {
      errmsg("資料查詢權限: select COL_QRY_DATA_AUTH error; user_id=" + lsUserId);
      return -1;
    }
    if (colStr("ll_cnt").equals("0")) {
      return 1;
    }

    sqlSelect =
        " select count(*) as ll_cnt " + " from ptr_sys_idtab " + " where wf_type = 'COLM0920' "
            + " and   wf_id like :ls_win_id ";
    setString("ls_win_id", "%" + asWinId);
    sqlSelect(sqlSelect);
    if (sqlRowNum <= 0) {
      errmsg("資料查詢權限: select PTR_SYS_IDTAB error; win_id=" + asWinId);
      return -1;
    }
    if (colStr("ll_cnt").equals("0")) {
      return 1;
    }

    // id_no //此段判待詢問 andy 20180115
    // if(empty(as_key)){
    // alert_err("資料查詢權限:身分證ID[統編] 不可空白");
    // return -1;
    // }
    if (empty(asKey)) {
      return 1;
    }

    String lsKey = asKey;
    String sqlSelect2 =
        " select decode(acct_status,'','NNNNN',acct_status) as ls_pa_acct "
            + " ,decode(stop_status,'','N',stop_status) as ls_pa_stop "
            + " ,decode(mcode_cond,'','N',mcode_cond) as ls_pa_cond "
            + " ,decode(mcode,'','00',mcode) as ls_pa_mcode " + " from ptr_comm_data "
            + " where parm_code ='COLM0910' " + " and seq_no =1 " + " fetch first 1 rows only ";
    sqlSelect(sqlSelect2);
    lsPaAcct = colStr("ls_pa_acct");
    lsPaStop = colStr("ls_pa_stop");
    lsPaCond = colStr("ls_pa_cond");
    lsPaMcode = colStr("ls_pa_mcode");
    if (sqlRowNum < 0) {
      errmsg("資料查詢權限: select PTR_COMM_DATA error; parm_code=COLM0910");
      return -1;
    }
    if (sqlRowNum == 0) {
      errmsg("資料查詢權限: 未指定查詢條件 [w_colm0920], 不允許查詢");
      return -1;
    }
    if (lsKey.length() >= 14) {
      String sqlSelect3 =
          " select uf_acno_key(acno_p_seqno) as ls_idno " + " from crd_card "
              + " where card_no like :ls_key " + " fetch first 1 rows only ";
      setString("ls_key", lsKey + "%");
      sqlSelect(sqlSelect3);
      lsIdno = colStr("ls_idno");
    } else {
      lsIdno = lsKey;
    }
    if (empty(lsIdno)) {
      errmsg("ERROR,資料查詢權限: 無法取得 卡友ID[統編]");
      return -1;
    }
    if (wp.isNumber(lsIdno)) {
      if (lsIdno.length() < 8) {
        errmsg("ERROR,資料查詢權限: 統編 輸入錯誤; CORO-NO=" + lsIdno);
        return -1;
      }
      lsAcctKey = strMid(lsIdno, 0, 8);
    } else {
      if (lsIdno.length() < 10) {
        errmsg("ERROR,資料查詢權限: 卡友ID 輸入錯誤;  ID=" + lsIdno);
        return -1;
      }
      lsAcctKey = strMid(lsIdno, 0, 10);
    }

    if (lsPaAcct.indexOf("Y") > 0 || lsPaStop.equals("Y")) {
      if (wp.isNumber(lsIdno)) {
        // -CORP-
        String sqlSelect3 =
            " select nvl(sum(decode(decode(acct_status,'','0',acct_status),'2',1,0)),0) li_acct2 "
                + " , nvl(sum(decode(decode(acct_status,'','0',acct_status),'3',1,0)),0) li_acct3 "
                + " , nvl(sum(decode(decode(acct_status,'','0',acct_status),'4',1,0)),0) li_acct4 "
                + " , nvl(sum(decode(decode(stop_status,'','N',stop_status),'Y',1,0)),0) li_stop "
                + "  from ecs_act_acno " + " where acct_key like :ls_acct_key ";
        setString("ls_acct_key", lsAcctKey + "%");
        sqlSelect(sqlSelect3);
        liAcct2 = colStr("li_acct2");
        liAcct3 = colStr("li_acct3");
        liAcct4 = colStr("li_acct4");
        liStop = colStr("li_stop");
      } else {
        // -ID-
        String sqlSelect3 =
            " select nvl(sum(decode(decode(a.acct_status,'','0',a.acct_status),'2',1,0)),0) li_acct2 "
                + " , nvl(sum(decode(decode(a.acct_status,'','0',a.acct_status),'3',1,0)),0) li_acct3 "
                + " , nvl(sum(decode(decode(a.acct_status,'','0',a.acct_status),'4',1,0)),0) li_acct4 "
                + " , nvl(sum(decode(decode(a.stop_status,'','N',a.stop_status),'Y',1,0)),0) li_stop "
                + "  from ecs_act_acno a, crd_idno b " + " where a.acno_flag <> 'Y' "
                + " and a.id_p_seqno=b.id_p_seqno " + " and b.id_no = :ls_acct_key ";
        setString("ls_acct_key", lsAcctKey);
        sqlSelect(sqlSelect3);
        liAcct2 = colStr("li_acct2");
        liAcct3 = colStr("li_acct3");
        liAcct4 = colStr("li_acct4");
        liStop = colStr("li_stop");
        if (sqlRowNum < 0) {
          errmsg("ERROR, select ecs_act_acno.acct_status error;  ID=" + lsIdno);
          return -1;
        }
        if (strMid(lsPaAcct, 1, 1).equals("Y") && Integer.parseInt(liAcct2) > 0) {
          return 1;
        }
        if (strMid(lsPaAcct, 2, 1).equals("Y") && Integer.parseInt(liAcct3) > 0) {
          return 1;
        }
        if (strMid(lsPaAcct, 3, 1).equals("Y") && Integer.parseInt(liAcct4) > 0) {
          return 1;
        }
        if (lsPaStop.equals("Y") && Integer.parseInt(liStop) > 0) {
          return 1;
        }
      }
    }
    if (lsPaCond.equals("Y")) {
      if (wp.isNumber(lsIdno)) {
        // -corp-
        String sqlSelect3 =
            " select count(*) as ll_cnt "
                + " from ecs_act_acno "
                + " where acct_key like :ls_acct_key "
                + " and decode(payment_rate1,'',' ',payment_rate1) >= :ls_pa_mcode "
                + " and decode(payment_rate1,'','xx',payment_rate1) not in ('xx','0A','0B','0C','0D','0E') ";
        setString("ls_acct_key", lsAcctKey + "%");
        setString("ls_pa_mcode", lsPaMcode);
        sqlSelect(sqlSelect3);
      } else {
        // -ID-
        String sqlSelect3 =
            " select count(*) as ll_cnt "
                + " from ecs_act_acno a, crd_idno b "
                + " where a.id_p_seqno=b.id_p_seqno "
                + " and b.id_no = :ls_acct_key "
                + " and a.acno_flag <> 'Y' "
                + " and decode(a.payment_rate1,'',' ',a.payment_rate1) >= :ls_pa_mcode "
                + " and decode(a.payment_rate1,'','xx',a.payment_rate1) not in ('xx','0A','0B','0C','0D','0E') ";
        setString("ls_acct_key", lsAcctKey);
        setString("ls_pa_mcode", lsPaMcode);
        sqlSelect(sqlSelect3);
        if (sqlRowNum < 0) {
          errmsg("資料查詢權限: select ecs_act_acno.payment_rate1 error; ID[CORP]=" + lsIdno);
          return -1;
        }
        if (Double.parseDouble(colStr("ll_cnt")) > 0) {
          return 1;
        }
      }
    }
    errmsg("ERROR,資料查詢權限: 卡友帳務狀況未達 [參數條件] 不可查詢; ID[CORP]=" + lsIdno);
    return -1;
  }

	/**
	 *依【年利率】套用以下公式計算
	 */
	public double getYearRateInterest(double amt ,double yearRate ,double days) {
		BigDecimal rcrateDay = BigDecimal.ZERO;
	    Double rcrateYear = new BigDecimal(yearRate).doubleValue();
	    rcrateDay = new BigDecimal(rcrateYear).
              multiply(BigDecimal.valueOf(amt)).
              multiply(BigDecimal.valueOf(days)).
              divide(BigDecimal.valueOf(100)).
              divide(BigDecimal.valueOf(365), 0, BigDecimal.ROUND_HALF_UP)
              ;
	    return rcrateDay.doubleValue();
	}
	
	/**
	 *依【日利率】套用以下公式計算
	 */
	public double getDayRateInterest(double amt ,double daysRate ,double days) {
		BigDecimal rcrateDay = BigDecimal.ZERO;
	    Double rcrateYear = new BigDecimal(daysRate).doubleValue();
	    rcrateDay = new BigDecimal(rcrateYear).
              multiply(BigDecimal.valueOf(amt)).
              multiply(BigDecimal.valueOf(days)).
              divide(BigDecimal.valueOf(10000), 0, BigDecimal.ROUND_HALF_UP)
              ;
	    return rcrateDay.doubleValue();
	}
	
	public Double numAdd(Double v1, Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.add(b2).doubleValue();

	}
	
	public Double numSub(Double v1, Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.subtract(b2).doubleValue();

	}
}
