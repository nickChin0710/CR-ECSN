/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/01/12  V1.00.03   Allen Ho      Initial                              *
 * 111-12-07  V1.00.04 Yanghan sync from mega & updated for project coding standard *
 * 111-12-13  V1.00.05   Zuwei         fix compile issue *
 * 111/12/16  V1.00.06   Machao        命名规则调整后测试修改                                                                     *
 * 111/12/22  V1.00.07   Zuwei         輸出sql log                                                                     *
 ***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0170Func extends FuncEdit
{
  private final String PROGNAME = "紅利特惠參數檔維護處理程式111-12-16 V1.00.06";
  String kk1;
  String orgControlTabName = "mkt_bpmh2";
  String controlTabName = "mkt_bpmh2_t";

  public Mktm0170Func(TarokoCommon wr)
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
            + " active_name, "
            + " bonus_type, "
            + " active_month_s, "
            + " active_month_e, "
            + " give_flag, "
            + " stop_flag, "
            + " stop_date, "
            + " stop_desc, "
            + " effect_months, "
            + " issue_cond, "
            + " issue_date_s, "
            + " issue_date_e, "
            + " re_months, "
//            + " new_hldr_cond, "
//            + " new_hldr_days, "
//            + " new_group_cond, "
//            + " new_hldr_card, "
//            + " new_hldr_sup, "
            + " purch_cond, "
            + " purch_s_date, "
            + " purch_e_date, "
            + " pre_filter_flag, "
            + " run_time_amt, "
            + " acct_type_sel, "
            + " group_code_sel, "
            + " card_type_sel, "
            + " limit_amt, "
            + " currency_sel, "
            + " merchant_sel, "
            + " mcht_group_sel, "
            + " mcc_code_sel, "
            + " pos_entry_sel, "
            + " currencyb_sel, "
            + " bl_cond, "
            + " ca_cond, "
            + " it_cond, "
            + " id_cond, "
            + " ao_cond, "
            + " ot_cond, "
            + " bill_type_sel, "
            + " add_times, "
            + " add_point, "
            + " per_point_amt, "
            + " feedback_lmt, "
          + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
          + " from " + procTabName 
            + " where rowid = ? ";

    Object[] param =new Object[]
            {
                    wp.itemRowId("rowid")
            };

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

    return 1;
  }
  // ************************************************************************
  @Override
  public void dataCheck()
  {
    try {
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
        kk1 = wp.itemStr("active_code");
      }
      else
      {
        kk1 = wp.itemStr("active_code");
      }
      if (wp.respHtml.indexOf("_nadd") > 0)
        if (this.ibAdd)
          if (kk1.length()>0)
          {
            strSql = "select count(*) as qua "
                    + "from " + orgControlTabName
                    + " where active_code = ? "
            ;
            Object[] param = new Object[] {kk1};
            sqlSelect(strSql,param);
            int qua =  Integer.parseInt(colStr("qua"));
            if (qua > 0)
            {
              errmsg("[活動代號] 不可重複("+orgControlTabName+"), 請重新輸入!");
              return;
            }
          }

      if (this.ibAdd)
        if (kk1.length()>0)
        {
          strSql = "select count(*) as qua "
                  + "from " + controlTabName
                  + " where active_code = ? "
          ;
          Object[] param = new Object[] {kk1};
          sqlSelect(strSql,param);
          int qua =  Integer.parseInt(colStr("qua"));
          if (qua > 0)
          {
            errmsg("[活動代號] 不可重複("+controlTabName+") ,請重新輸入!");
            return;
          }
        }

      if (this.ibUpdate)
      {
        if ((wp.itemStr("acct_type_sel").equals("1"))||
                (wp.itemStr("acct_type_sel").equals("2")))
        {
          if (listParmDataCnt("mkt_bn_data_t"
                  ,"MKT_BPMH2"
                  ,wp.colStr("active_code")
                  ,"3")==0)
          {
            errmsg("[B.帳戶類別] 明細沒有設定, 筆數不可為 0  !");
            return;
          }
        }
        if ((wp.itemStr("group_code_sel").equals("1"))||
                (wp.itemStr("group_code_sel").equals("2")))
        {
          if (listParmDataCnt("mkt_bn_data_t"
                  ,"MKT_BPMH2"
                  ,wp.colStr("active_code")
                  ,"2")==0)
          {
            errmsg("[C.團體代號] 明細沒有設定, 筆數不可為 0  !");
            return;
          }
        }
        if ((wp.itemStr("card_type_sel").equals("1"))||
                (wp.itemStr("card_type_sel").equals("2")))
        {
          if (listParmDataCnt("mkt_bn_data_t"
                  ,"MKT_BPMH2"
                  ,wp.colStr("active_code")
                  ,"8")==0)
          {
            errmsg("[N.卡種] 明細沒有設定, 筆數不可為 0  !");
            return;
          }
        }
        if ((wp.itemStr("currency_sel").equals("1"))||
                (wp.itemStr("currency_sel").equals("2")))
        {
          if (listParmDataCnt("mkt_bn_data_t"
                  ,"MKT_BPMH2"
                  ,wp.colStr("active_code")
                  ,"7")==0)
          {
            errmsg("[K.交易幣別] 明細沒有設定, 筆數不可為 0  !");
            return;
          }
        }
        if ((wp.itemStr("merchant_sel").equals("1"))||
                (wp.itemStr("merchant_sel").equals("2")))
        {
          if (listParmDataCnt("mkt_bn_data_t"
                  ,"MKT_BPMH2"
                  ,wp.colStr("active_code")
                  ,"1")==0)
          {
            errmsg("[O.特店代號] 明細沒有設定, 筆數不可為 0  !");
            return;
          }
        }
        if ((wp.itemStr("mcht_group_sel").equals("1"))||
                (wp.itemStr("mcht_group_sel").equals("2")))
        {
          if (listParmDataCnt("mkt_bn_data_t"
                  ,"MKT_BPMH2"
                  ,wp.colStr("active_code")
                  ,"B")==0)
          {
            errmsg("[P.特店群組] 明細沒有設定, 筆數不可為 0  !");
            return;
          }
        }
        if ((wp.itemStr("mcc_code_sel").equals("1"))||
                (wp.itemStr("mcc_code_sel").equals("2")))
        {
          if (listParmDataCnt("mkt_bn_data_t"
                  ,"MKT_BPMH2"
                  ,wp.colStr("active_code")
                  ,"5")==0)
          {
            errmsg("[E.特店類別] 明細沒有設定, 筆數不可為 0  !");
            return;
          }
        }
        if ((wp.itemStr("pos_entry_sel").equals("1"))||
                (wp.itemStr("pos_entry_sel").equals("2")))
        {
          if (listParmDataCnt("mkt_bn_data_t"
                  ,"MKT_BPMH2"
                  ,wp.colStr("active_code")
                  ,"L")==0)
          {
            errmsg("[L.POS ENTRY] 明細沒有設定, 筆數不可為 0  !");
            return;
          }
        }
        if ((wp.itemStr("currencyb_sel").equals("1"))||
                (wp.itemStr("currencyb_sel").equals("2")))
        {
          if (listParmDataCnt("mkt_bn_data_t"
                  ,"MKT_BPMH2"
                  ,wp.colStr("active_code")
                  ,"M")==0)
          {
            errmsg("[M.交易幣別] 明細沒有設定, 筆數不可為 0  !");
            return;
          }
        }
        if ((wp.itemStr("bill_type_sel").equals("1"))||
                (wp.itemStr("bill_type_sel").equals("2")))
        {
          if (listParmDataCnt("mkt_bn_data_t"
                  ,"MKT_BPMH2"
                  ,wp.colStr("active_code")
                  ,"6")==0)
          {
            errmsg("[I.帳單來源] 明細沒有設定, 筆數不可為 0  !");
            return;
          }
        }
      }
      if (!wp.itemStr("give_flag").equals("Y")) wp.itemSet("give_flag","N");
      if (!wp.itemStr("stop_flag").equals("Y")) wp.itemSet("stop_flag","N");
      if (!wp.itemStr("issue_cond").equals("Y")) wp.itemSet("issue_cond","N");
//      if (!wp.itemStr("new_hldr_cond").equals("Y")) wp.itemSet("new_hldr_cond","N");
//      if (!wp.itemStr("new_group_cond").equals("Y")) wp.itemSet("new_group_cond","N");
//      if (!wp.itemStr("new_hldr_card").equals("Y")) wp.itemSet("new_hldr_card","N");
//      if (!wp.itemStr("new_hldr_sup").equals("Y")) wp.itemSet("new_hldr_sup","N");
      if (!wp.itemStr("purch_cond").equals("Y")) wp.itemSet("purch_cond","N");
      if (!wp.itemStr("bl_cond").equals("Y")) wp.itemSet("bl_cond","N");
      if (!wp.itemStr("ca_cond").equals("Y")) wp.itemSet("ca_cond","N");
      if (!wp.itemStr("it_cond").equals("Y")) wp.itemSet("it_cond","N");
      if (!wp.itemStr("id_cond").equals("Y")) wp.itemSet("id_cond","N");
      if (!wp.itemStr("ao_cond").equals("Y")) wp.itemSet("ao_cond","N");
      if (!wp.itemStr("ot_cond").equals("Y")) wp.itemSet("ot_cond","N");

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

      if (wp.itemEmpty("apr_flag")) {
          wp.colSet("apr_flag", "N");
          wp.itemSet("apr_flag", "N");
      }
      if ((this.ibDelete)||
              (wp.itemStr("aud_type").equals("D"))) return;

//      if (this.ibUpdate)
//      {
//        if ((wp.itemStr("new_hldr_cond").equals("Y"))&&
//                (wp.itemStr("new_group_cond").equals("Y")))
//        {
//          if (listParmDataCnt("mkt_bn_data_t"
//                  ,"MKT_BPMH2"
//                  ,wp.colStr("active_code")
//                  ,"4")==0)
//          {
//            errmsg("[新卡友-未持有團代] 明細沒有設定, 筆數不可為 0  !");
//            return;
//          }
//        }
//      }
//      if (wp.itemStr("new_hldr_cond").equals("Y"))
//      {
//        if (wp.itemStr("new_hldr_days").length()==0) wp.itemSet("new_hldr_days","0");
//        if (wp.colNum("new_hldr_days")==0)
//        {
//          errmsg("[新卡友][核卡日期N日] N必須輸入 !");
//          return;
//        }
//        if ((!wp.colStr("new_hldr_card").equals("Y"))&&
//                (!wp.colStr("new_hldr_sup").equals("Y")))
//        {
//          errmsg("[新卡友][正卡或附卡] 至少要選取一項 !");
//          return;
//        }
//      }
      if (wp.itemStr("stop_flag").equals("Y"))
      {
        if ((wp.itemStr("stop_date").length()==0)||
                (wp.itemStr("stop_desc").length()==0))
        {
          errmsg("[取消日期與取消說明], 必須輸入 !");
          return;
        }
      }
      if ((this.ibAdd)||(this.ibUpdate))
      {
        wp.itemSet("pre_filter_flag" , "2");
        if (wp.itemStr("issue_cond").equals("Y"))
        {
          if ((wp.itemStr("issue_date_s").length()==0)||
                  (wp.itemStr("issue_date_e").length()==0))
          {
            errmsg("發卡條件 發卡日期必須輸入!");
            return;
          }
          if (wp.itemStr("re_months").length()==0) wp.itemSet("re_months" , "0");
          if (wp.itemNum("re_months")<=0)
          {
            errmsg("發卡條件 發卡後回饋月數必須輸入!");
            return;
          }
        }
        if (wp.itemStr("purch_cond").equals("Y"))
        {
          if ((wp.itemStr("purch_s_date").length()==0)||
                  (wp.itemStr("purch_e_date").length()==0))
          {
            errmsg(" 消費期間必須輸入!");
            return;
          }
        }
        if ((!wp.itemStr("bl_cond").equals("Y"))&&
                (!wp.itemStr("ot_cond").equals("Y"))&&
                (!wp.itemStr("it_cond").equals("Y"))&&
                (!wp.itemStr("ca_cond").equals("Y"))&&
                (!wp.itemStr("id_cond").equals("Y"))&&
                (!wp.itemStr("ao_cond").equals("Y")))
        {
          errmsg("[消費本金類] 至少要選一個!");
          return;
        }
        if (wp.itemStr("add_times").length()==0) wp.itemSet("add_times" , "0");
        if (wp.itemStr("add_point").length()==0) wp.itemSet("add_point" , "0");
        if ((wp.itemNum("add_times")==0)&&
                (wp.itemNum("add_point")==0))
        {
          errmsg("[加贈點數] 倍數與點數至少一個不為0!");
          return;
        }
        if (wp.itemStr("per_point_amt").length()==0) wp.itemSet("per_point_amt" , "0");
        if (wp.itemNum("per_point_amt")==0)
        {
          errmsg("[元兌換一點] 不可為0!");
          return;
        }

      }

      if ((this.ibAdd)||(this.ibUpdate))
      {
        if (!wp.itemEmpty("issue_date_s")&&(!wp.itemEmpty("issue_DATE_e")))
          if (wp.itemStr("issue_date_s").compareTo(wp.itemStr("issue_DATE_e"))>0)
          {
            errmsg("發卡日期:["+wp.itemStr("issue_date_s")+"]>["+wp.itemStr("issue_DATE_e")+"] 起迄值錯誤!");
            return;
          }
      }

      if ((this.ibAdd)||(this.ibUpdate))
      {
        if (!wp.itemEmpty("purch_s_date")&&(!wp.itemEmpty("purch_e_DATE")))
          if (wp.itemStr("purch_s_date").compareTo(wp.itemStr("purch_e_DATE"))>0)
          {
            errmsg("["+wp.itemStr("purch_s_date")+"]>["+wp.itemStr("purch_e_DATE")+"] 起迄值錯誤!");
            return;
          }
      }

      int checkInt = checkDecnum(wp.itemStr("add_times"),3,4);
      if (checkInt!=0)
      {
        if (checkInt==1)
          errmsg("F.加贈點數： 格式超出範圍 : 整數[3]位 小數[4]位");
        if (checkInt==2)
          errmsg("F.加贈點數： 格式超出範圍 : 不可有小數位");
        if (checkInt==3)
          errmsg("F.加贈點數： 非數值");
        return;
      }

      checkInt = checkDecnum(wp.itemStr("feedback_lmt"),9,0);
      if (checkInt!=0)
      {
        if (checkInt==1)
          errmsg("J.點數回饋上限： 格式超出範圍 : 整數[9]位");
        if (checkInt==2)
          errmsg("J.點數回饋上限： 格式超出範圍 : 不可有小數位");
        if (checkInt==3)
          errmsg("J.點數回饋上限： 非數值");
        return;
      }

      if ((this.ibAdd)||(this.ibUpdate))
        if (wp.itemEmpty("apr_flag"))
        {
          errmsg("覆核狀態: 不可空白");
          return;
        }

      if ((this.ibAdd)||(this.ibUpdate))
        if (wp.itemEmpty("active_name"))
        {
          errmsg("活動說明： 不可空白");
          return;
        }


      if (this.isAdd()) return;

      if (this.ibDelete)
      {
        wp.colSet("storetype" , "N");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  // ************************************************************************
  @Override
  public int dbInsert(){
    rc = dataSelect();
    if (rc!=1) return rc;
    actionInit("A");
    try {
      dataCheck();
      if (rc!=1) return rc;
      dbInsertD2T();
      dbInsertI2T();
    } catch (Exception e) {
      e.printStackTrace();
    }


    strSql= " insert into  " + controlTabName+ " ("
            + " active_code, "
            + " apr_flag, "
            + " aud_type, "
            + " active_name, "
            + " bonus_type, "
            + " active_month_s, "
            + " active_month_e, "
            + " give_flag, "
            + " stop_flag, "
            + " stop_date, "
            + " stop_desc, "
            + " effect_months, "
            + " issue_cond, "
            + " issue_date_s, "
            + " issue_date_e, "
            + " re_months, "
//            + " new_hldr_cond, "
//            + " new_hldr_days, "
//            + " new_group_cond, "
//            + " new_hldr_card, "
//            + " new_hldr_sup, "
            + " purch_cond, "
            + " purch_s_date, "
            + " purch_e_date, "
            + " pre_filter_flag, "
            + " run_time_amt, "
            + " acct_type_sel, "
            + " group_code_sel, "
            + " card_type_sel, "
            + " limit_amt, "
            + " currency_sel, "
            + " merchant_sel, "
            + " mcht_group_sel, "
            + " mcc_code_sel, "
            + " pos_entry_sel, "
            + " currencyb_sel, "
            + " bl_cond, "
            + " ca_cond, "
            + " it_cond, "
            + " id_cond, "
            + " ao_cond, "
            + " ot_cond, "
            + " bill_type_sel, "
            + " add_times, "
            + " add_point, "
            + " per_point_amt, "
            + " feedback_lmt, "
            + " crt_date, "
            + " crt_user, "
          + " mod_seqno, "
            + " mod_user, "
          + " mod_time,mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
//            + "?,?,?,?,?,"
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
                    wp.itemStr("active_name"),
                    wp.itemStr("bonus_type"),
                    wp.itemStr("active_month_s"),
                    wp.itemStr("active_month_e"),
                    wp.itemStr("give_flag"),
                    wp.itemStr("stop_flag"),
                    wp.itemStr("stop_date"),
                    wp.itemStr("stop_desc"),
                    wp.itemNum("effect_months"),
                    wp.itemStr("issue_cond"),
                    wp.itemStr("issue_date_s"),
                    wp.itemStr("issue_date_e"),
                    wp.itemNum("re_months"),
//                    wp.itemStr("new_hldr_cond"),
//                    wp.itemNum("new_hldr_days"),
//                    wp.itemStr("new_group_cond"),
//                    wp.itemStr("new_hldr_card"),
//                    wp.itemStr("new_hldr_sup"),
                    wp.itemStr("purch_cond"),
                    wp.itemStr("purch_s_date"),
                    wp.itemStr("purch_e_date"),
                    wp.itemStr("pre_filter_flag"),
                    wp.itemNum("run_time_amt"),
                    wp.itemStr("acct_type_sel"),
                    wp.itemStr("group_code_sel"),
                    wp.itemStr("card_type_sel"),
                    wp.itemNum("limit_amt"),
                    wp.itemStr("currency_sel"),
                    wp.itemStr("merchant_sel"),
                    wp.itemStr("mcht_group_sel"),
                    wp.itemStr("mcc_code_sel"),
                    wp.itemStr("pos_entry_sel"),
                    wp.itemStr("currencyb_sel"),
                    wp.itemStr("bl_cond"),
                    wp.itemStr("ca_cond"),
                    wp.itemStr("it_cond"),
                    wp.itemStr("id_cond"),
                    wp.itemStr("ao_cond"),
                    wp.itemStr("ot_cond"),
                    wp.itemStr("bill_type_sel"),
                    wp.itemNum("add_times"),
                    wp.itemNum("add_point"),
                    wp.itemNum("per_point_amt"),
                    wp.itemNum("feedback_lmt"),
                    wp.loginUser,
                    wp.modSeqno(),
                    wp.loginUser,
                    wp.modPgm()
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("新增 "+controlTabName+" 重複錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbInsertI2T() throws Exception
  {
    msgOK();

    strSql = "insert into MKT_BN_DATA_T "
            + "select * "
            + "from MKT_BN_DATA "
            + "where table_name  =  'MKT_BPMH2' "
            + "and   data_key = ? "
            + "";

    Object[] param =new Object[]
            {
                    wp.itemStr("active_code"),
            };

    wp.dupRecord = "Y";
    sqlExec(strSql, param);


    return 1;
  }
  // ************************************************************************
  @Override
  public int dbUpdate() {
    rc = dataSelect();
    if (rc!=1) return rc;
    actionInit("U");
    try {
      dataCheck();
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (rc!=1) return rc;

    strSql= "update " +controlTabName + " set "
            + "apr_flag = ?, "
            + "active_name = ?, "
            + "bonus_type = ?, "
            + "active_month_s = ?, "
            + "active_month_e = ?, "
            + "give_flag = ?, "
            + "stop_flag = ?, "
            + "stop_date = ?, "
            + "stop_desc = ?, "
            + "effect_months = ?, "
            + "issue_cond = ?, "
            + "issue_date_s = ?, "
            + "issue_date_e = ?, "
            + "re_months = ?, "
//            + "new_hldr_cond = ?, "
//            + "new_hldr_days = ?, "
//            + "new_group_cond = ?, "
//            + "new_hldr_card = ?, "
//            + "new_hldr_sup = ?, "
            + "purch_cond = ?, "
            + "purch_s_date = ?, "
            + "purch_e_date = ?, "
            + "pre_filter_flag = ?, "
            + "run_time_amt = ?, "
            + "acct_type_sel = ?, "
            + "group_code_sel = ?, "
            + "card_type_sel = ?, "
            + "limit_amt = ?, "
            + "currency_sel = ?, "
            + "merchant_sel = ?, "
            + "mcht_group_sel = ?, "
            + "mcc_code_sel = ?, "
            + "pos_entry_sel = ?, "
            + "currencyb_sel = ?, "
            + "bl_cond = ?, "
            + "ca_cond = ?, "
            + "it_cond = ?, "
            + "id_cond = ?, "
            + "ao_cond = ?, "
            + "ot_cond = ?, "
            + "bill_type_sel = ?, "
            + "add_times = ?, "
            + "add_point = ?, "
            + "per_point_amt = ?, "
            + "feedback_lmt = ?, "
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
                    wp.itemStr("active_name"),
                    wp.itemStr("bonus_type"),
                    wp.itemStr("active_month_s"),
                    wp.itemStr("active_month_e"),
                    wp.itemStr("give_flag"),
                    wp.itemStr("stop_flag"),
                    wp.itemStr("stop_date"),
                    wp.itemStr("stop_desc"),
                    wp.itemNum("effect_months"),
                    wp.itemStr("issue_cond"),
                    wp.itemStr("issue_date_s"),
                    wp.itemStr("issue_date_e"),
                    wp.itemNum("re_months"),
//                    wp.itemStr("new_hldr_cond"),
//                    wp.itemNum("new_hldr_days"),
//                    wp.itemStr("new_group_cond"),
//                    wp.itemStr("new_hldr_card"),
//                    wp.itemStr("new_hldr_sup"),
                    wp.itemStr("purch_cond"),
                    wp.itemStr("purch_s_date"),
                    wp.itemStr("purch_e_date"),
                    wp.itemStr("pre_filter_flag"),
                    wp.itemNum("run_time_amt"),
                    wp.itemStr("acct_type_sel"),
                    wp.itemStr("group_code_sel"),
                    wp.itemStr("card_type_sel"),
                    wp.itemNum("limit_amt"),
                    wp.itemStr("currency_sel"),
                    wp.itemStr("merchant_sel"),
                    wp.itemStr("mcht_group_sel"),
                    wp.itemStr("mcc_code_sel"),
                    wp.itemStr("pos_entry_sel"),
                    wp.itemStr("currencyb_sel"),
                    wp.itemStr("bl_cond"),
                    wp.itemStr("ca_cond"),
                    wp.itemStr("it_cond"),
                    wp.itemStr("id_cond"),
                    wp.itemStr("ao_cond"),
                    wp.itemStr("ot_cond"),
                    wp.itemStr("bill_type_sel"),
                    wp.itemNum("add_times"),
                    wp.itemNum("add_point"),
                    wp.itemNum("per_point_amt"),
                    wp.itemNum("feedback_lmt"),
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
  public int dbDelete() {
    rc = dataSelect();
    if (rc!=1) return rc;
    actionInit("D");
    try {
      dataCheck();
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (rc!=1)return rc;

    try {
      dbInsertD2T();
    } catch (Exception e) {
      e.printStackTrace();
    }

    strSql = "delete " +controlTabName + " "
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
  // ************************************************************************
  public int dbInsertD2T() throws Exception
  {
    msgOK();

    strSql = "delete MKT_BN_DATA_T "
            + " where table_name  =  'MKT_BPMH2' "
            + "and   data_key = ? "
            + "";
    //如果沒有資料回傳成功1
    Object[] param = new Object[]
            {
                    wp.itemStr("active_code"),
            };

    sqlExec(strSql,param);
    if (sqlRowNum <= 0) rc=0;else rc=1;

    if (rc!=1) errmsg("刪除 MKT_BN_DATA_T 錯誤");

    return rc;

  }
  // ************************************************************************
  public int checkDecnum(String decStr, int col_length, int col_scale)
  {
    if (decStr.length()==0) return(0);
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    if (!comm.isNumber(decStr.replace("-","").replace(".",""))) return(3);
    decStr = decStr.replace("-","");
    if ((col_scale==0)&&(decStr.toUpperCase().indexOf(".")!=-1)) return(2);
    String[]  parts = decStr.split("[.^]");
    if ((parts.length==1&&parts[0].length()>col_length)||
            (parts.length==2&&
                    (parts[0].length()>col_length||parts[1].length()>col_scale)))
      return(1);
    return(0);
  }
  // ************************************************************************
  public int dbInsertI2() throws Exception
  {
    msgOK();

    String dataType="";
    if (wp.respHtml.equals("mktm0170_gncd"))
      dataType = "4" ;
    if (wp.respHtml.equals("mktm0170_actp"))
      dataType = "3" ;
    if (wp.respHtml.equals("mktm0170_gpcd"))
      dataType = "2" ;
    if (wp.respHtml.equals("mktm0170_caty"))
      dataType = "8" ;
    if (wp.respHtml.equals("mktm0170_aaa1"))
      dataType = "B" ;
    if (wp.respHtml.equals("mktm0170_mccc"))
      dataType = "5" ;
    if (wp.respHtml.equals("mktm0170_pose"))
      dataType = "L" ;
    if (wp.respHtml.equals("mktm0170_bisr"))
      dataType = "6" ;
    strSql = "insert into MKT_BN_DATA_T ( "
            + "table_name, "
            + "data_type, "
            + "data_key,"
            + "data_code,"
            + "crt_date, "
            + "crt_user, "
            + " mod_time, "
            + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
            + ") values ("
            + "'MKT_BPMH2', "
            + "?, "
            + "?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + " sysdate, "
            + "?,"
            + "1,"
            + " ? "
            + ")";

    Object[] param =new Object[]
            {
                    dataType,
                    wp.itemStr("active_code"),
                    varsStr("data_code"),
                    wp.loginUser,
                    wp.loginUser,
                    wp.modPgm()
            };

    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) rc=0;else rc=1;

    if (rc!=1) errmsg("新增8 MKT_BN_DATA_T 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbDeleteD2() throws Exception
  {
    msgOK();

    String dataType="";
    if (wp.respHtml.equals("mktm0170_gncd"))
      dataType = "4" ;
    if (wp.respHtml.equals("mktm0170_actp"))
      dataType = "3" ;
    if (wp.respHtml.equals("mktm0170_gpcd"))
      dataType = "2" ;
    if (wp.respHtml.equals("mktm0170_caty"))
      dataType = "8" ;
    if (wp.respHtml.equals("mktm0170_aaa1"))
      dataType = "B" ;
    if (wp.respHtml.equals("mktm0170_mccc"))
      dataType = "5" ;
    if (wp.respHtml.equals("mktm0170_pose"))
      dataType = "L" ;
    if (wp.respHtml.equals("mktm0170_bisr"))
      dataType = "6" ;
    //如果沒有資料回傳成功2
    Object[] param = new Object[]
            {
                    dataType,
                    wp.itemStr("active_code")
            };
    if (sqlRowcount("MKT_BN_DATA_T"
            , "where data_type = ? "
                    + "and   data_key = ? "
                    + "and   table_name = 'MKT_BPMH2' "
            , param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T "
            + "where data_type = ? "
            + "and   data_key = ?  "
            + "and   table_name = 'MKT_BPMH2'  "
    ;
    sqlExec(strSql,param);


    return 1;

  }
  // ************************************************************************
  public int dbInsertI3() throws Exception
  {
    msgOK();

    String dataType="";
    if (wp.respHtml.equals("mktm0170_cocd"))
      dataType = "7" ;
    if (wp.respHtml.equals("mktm0170_mrch"))
      dataType = "1" ;
    if (wp.respHtml.equals("mktm0170_mccd"))
      dataType = "M" ;
    strSql = "insert into MKT_BN_DATA_T ( "
            + "table_name, "
            + "data_type, "
            + "data_key,"
            + "data_code,"
            + "data_code2,"
            + "crt_date, "
            + "crt_user, "
            + " mod_time, "
            + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
            + ") values ("
            + "'MKT_BPMH2', "
            + "?, "
            + "?,?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + " sysdate, "
            + "?,"
            + "1,"
            + " ? "
            + ")";

    Object[] param =new Object[]
            {
                    dataType,
                    wp.itemStr("active_code"),
                    varsStr("data_code"),
                    varsStr("data_code2"),
                    wp.loginUser,
                    wp.loginUser,
                    wp.modPgm()
            };

    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) rc=0;else rc=1;

    if (rc!=1) errmsg("新增8 MKT_BN_DATA_T 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbDeleteD3() throws Exception
  {
    msgOK();

    String dataType="";
    if (wp.respHtml.equals("mktm0170_cocd"))
      dataType = "7" ;
    if (wp.respHtml.equals("mktm0170_mrch"))
      dataType = "1" ;
    if (wp.respHtml.equals("mktm0170_mccd"))
      dataType = "M" ;
    //如果沒有資料回傳成功2
    Object[] param = new Object[]
            {
                    dataType,
                    wp.itemStr("active_code")
            };
    if (sqlRowcount("MKT_BN_DATA_T"
            , "where data_type = ? "
                    + "and   data_key = ? "
                    + "and   table_name = 'MKT_BPMH2' "
            , param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T "
            + "where data_type = ? "
            + "and   data_key = ?  "
            + "and   table_name = 'MKT_BPMH2'  "
    ;
    sqlExec(strSql,param);


    return 1;

  }
  // ************************************************************************
  public int dbInsertI2Aaa1(String tableName, String[] columnCol, String[] columnDat) throws Exception
  {
    String[] columnData = new String[50];
    String   stra="",strb="";
    int      skipLine= 0;
    long     listCnt   = 50;
    strSql= " insert into  " + tableName + " (";
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql
            + " mod_user, "
          + " mod_time,mod_pgm "
            + " ) values (";
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      strSql = strSql + "?," ;
    }
    strSql = strSql
            + "?,"
            + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 =new Object[50];
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      stra = columnDat[inti];
      param1[skipLine]= stra ;
      skipLine++;
    }
    param1[skipLine++]= wp.loginUser;
    param1[skipLine++]= wp.sysDate + wp.sysTime;
    param1[skipLine++]= wp.modPgm();
    Object[] param = Arrays.copyOf(param1,skipLine);
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) rc=0;else rc=1;

    return rc;
  }
  // ************************************************************************
  public int dbDeleteD2Aaa1(String table_name) throws Exception
  {
    strSql = "delete  "+table_name+" "
            + "where table_name = ? "
            + "and   data_key = ? "
            + "and   data_type = ? "
    ;

    Object[] param =new Object[]
            {
                    "MKT_BPMH2",
                    wp.itemStr("active_code"),
                    "1"
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) rc=0;else rc=1;
    if (rc!=1) errmsg("刪除 "+ table_name +" 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbInsertEcsMediaErrlog(String tranSeqStr, String[] errMsg ) throws Exception
  {
    dateTime();
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    comr.setConn(wp);

    if (!comm.isNumber(errMsg[10])) errMsg[10]="0";
    if (!comm.isNumber(errMsg[1])) errMsg[1]="0";
    if (!comm.isNumber(errMsg[2])) errMsg[2]="0";

    strSql= " insert into ecs_media_errlog ("
            + " crt_date, "
            + " crt_time, "
            + " file_name, "
            + " unit_code, "
            + " main_desc, "
            + " error_seq, "
            + " error_desc, "
            + " line_seq, "
            + " column_seq, "
            + " column_data, "
            + " trans_seqno, "
            + " column_desc, "
            + " program_code, "
            + " mod_time, "
          + " mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?," // 10 record
            + "?,?,?,"               // 4 trvotfd
            + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param =new Object[]
            {
                    wp.sysDate,
                    wp.sysTime,
                    wp.itemStr("zz_file_name"),
                    comr.getObjectOwner("3",wp.modPgm()),
                    errMsg[0],
                    Integer.valueOf(errMsg[1]),
                    errMsg[4],
                    Integer.valueOf(errMsg[10]),
                    Integer.valueOf(errMsg[2]),
                    errMsg[3],
                    tranSeqStr,
                    errMsg[5],
                    wp.modPgm(),
                    wp.sysDate + wp.sysTime,
                    wp.modPgm()
            };

    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("新增4 ecs_media_errlog 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbInsertEcsNotifyLog(String tranSeqStr, int errorCnt ) throws Exception
  {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    dateTime();
    strSql= " insert into ecs_notify_log ("
            + " crt_date, "
            + " crt_time, "
            + " unit_code, "
            + " obj_type, "
            + " notify_head, "
            + " notify_name, "
            + " notify_desc1, "
            + " notify_desc2, "
            + " trans_seqno, "
            + " mod_time, "
          + " mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?," // 9 record
            + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param =new Object[]
            {
                    wp.sysDate,
                    wp.sysTime,
                    comr.getObjectOwner("3",wp.modPgm()),
                    "3",
                    "媒體檔轉入資料有誤(只記錄前100筆)",
                    "媒體檔名:"+wp.itemStr("zz_file_name"),
                    "程式 "+wp.modPgm()+" 轉 "+wp.itemStr("zz_file_name")+" 有"+errorCnt+" 筆錯誤",
                    "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤",
                    tranSeqStr,
                    wp.sysDate + wp.sysTime,
                    wp.modPgm()
            };

    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("新增5 ecs_modify_log 錯誤");
    return rc;
  }
  // ************************************************************************
  int listParmDataCnt(String s1, String s2, String s3, String s4) throws Exception
  {
    String strSql = "select count(*) as data_cnt "
            + "from  " + s1 +" "
            + " where table_name = ? "
            + " and   data_key   = ? "
            + " and   data_type  = ? "
            ;
    Object[] param = new Object[] {s2,s3,s4};
    sqlSelect(strSql,param);

    return(Integer.parseInt(colStr("data_cnt")));
  }

// ************************************************************************

}  // End of class
