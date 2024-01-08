/*
 * 2019-0613:   JH                               p_xxx >>acno_p_xxx
 * 2019-0827:   JH                               ex_card_no: only-read卡號
 * 2019-1216:   Alex                           query bug fix
 * 2019-1219:   Alex                            ptr_branch -> gen_brn
 * 109-04-20  shiyuqi                          updated for project coding standard
 * 109-06-22  sunny                            修改act_acct_curr查無資料的問題
 * 109-06-29  V1.00.11  zuwei        「証」為「證」
 * 109-07-30  V1.00.12  JustinWu   remove db_web_card_member   
 * 109-08-17  V1.00.13  JustinWu   add 悠遊VD卡
 * 109-08-20  V1.00.14  JustinWu   修改查詢條件有身分證及中文姓名的bug
 * 109-08-20  V1.00.15  Sunny        disable selectPdrate
 * 109-08-28  V1.00.15  JustinWu   add autopay_id_and_desc   
 * 109-09-01  V1.00.16  JustinWu   modify  the source table of eng_name
 * 109-09-02  V1.00.17  JustinWu   add 愛金卡 currentCodeDesc, 調整取得統編及公司名方法
 * 109-09-03  V1.00.18  JustinWu   change to use common function to get the descriptions of marriage and education code
 * 109-09-03  V1.00.19  JustinWu   corpname 改回原本邏輯
 * 109-09-23  V1.00.20  JustinWu   帳戶資料卡人(法人)名稱修改
 * 109-10-20  V1.00.21   JustinWu   修改dot, dash display and 修改錯誤訊息
 * 109-11-06   V1.00.22  ryan  update function selectTscc() ,tsc_oppost_log change to cca_outgoing ,add function selectIpass() selectIcash()
 * 109-11-09   V1.00.23  JustinWu    add OEMPay and rename the card_type 
 * 109-12-02   V1.00.24 JustinWu     change uf_tt_idtab into uf_opp_status
 * 109-12-09   V1.00.25 JustinWu     change to select eng_name from dbc_idno instead of dbc_card, and remove 外幣卡
 * 109-12-23   V1.00.23 JustinWu     parameterize sql
 * 109-12-24   V1.00.24 JustinWu     移除sms_flag欄位
 * 109-12-31   V1.00.25 JustinWu     add OEMPay detail page
 * 110-01-05   V1.00.26  Tanwei        zzDate,zzStr,zzComm,zzCurr變量更改         * 
 * 110-01-27   V1.00.27 JustinWu     remove log functions
 * 110-02-08   V1.00.28  JustinWu     correct the wrong error conditions: crd and dbc
 * 110-03-25   V1.00.29  JustinWu     checkXXcard: add oempay
 * 110-10-07   V1.00.30  JustinWu    中止 -> 終止
 * 111-01-21   V1.00.31  Sunny       卡片資訊增加開卡欄位
 * 111-02-24   V1.00.32  JustinWu    增加查詢CMS_CHGCOLUMN_LOG
 * 111-06-16   V1.00.33  Justin      3.中止(實體卡停卡) => 3.中止(已停用)       * 
 * 111-11-02   V1.00.34  Ryan        新增EPAY主檔相關資訊       * 
 * 111-11-17   V1.00.35  Sunny       修正帳單註記顯示(調整變數)       * 
 * 112-02-02   V1.00.36  Sunny       增加顯示【自動結匯台幣帳號】【繳款編號】【繳款編號II】*
 * 112-06-09   V1.00.37  Ryan        關係卡增加其他頁籤*
 * 112-08-02   V1.00.37  Sunny       調整協商狀態顯示*
 * 112-08-07   V1.00.38  Ryan        調整債協清償筆數顯示問題*
 * 112-08-08   V1.00.39  Ryan        修正電子帳單專用郵件信箱, 最後異動日期未顯示問題 *
 * 112-08-09   V1.00.40  Ryan        mark selectCmsChgColumnLog, 郵件信箱, 最後異動日期改為讀取act_acno/dba_acno 
 * 112-08-14   V1.00.41  Machao      金控利害關係人 頁面error修复*
 * 112-08-17   V1.00.42  Ryan        貴賓卡頁籤增加信用卡號欄位*
 * 112-11-01   V1.00.43  Ryan        增加計算年利率*
 * 112-11-08   V1.00.44  Ryan        3560 修正 3650*
 * 112-11-10   V1.00.45  Ryan        年利率修改為四捨五入
 * 112-12-04   V1.00.46  Sunny       日利率減碼註記非+號(加碼者)則一律視同(-號)減碼
 * */
package cmsm01;

import java.math.BigDecimal;

import busi.func.CmsFunc;
import ecsfunc.DeCodeAct;
import ecsfunc.DeCodeCms;
import ecsfunc.DeCodeCrd;
import ofcapp.BaseAction;
import taroko.base.CommString;

public class Cmsq2100 extends BaseAction {
	final static private String CRD_CARD = "CRD_CARD", CRD_IDNO = "CRD_IDNO", 
			                            DBC_CARD = "DBC_CARD", DBC_IDNO = "DBC_IDNO",  
			                            TSC_CARD = "TSC_CARD", TSC_VD_CARD = "TSC_VD_CARD",
			                            IPS_CARD_NAME="一卡通",
			                            ICH_CARD_NAME="愛金卡", MOB_CARD_NAME="行動支付",
			                            HCE_CARD_NAME="HCE",TSC_VD_CARD_NAME = "悠遊VD卡", 
			                            TSC_CARD_NAME = "悠遊卡", OEMPAY_CARD_NAME = "OEM", 
			                            EPAY_CARD_NAME = "EPAY";  
	
  String dataKK1 = "", dataKK2 = "", dataKK3 = "", dataKK4 = "";
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  CommString commString = new CommString();
  @Override
  public void userAction() throws Exception {
	  
	  wp.pageRows = 900; //限制查詢筆數
	  
    if (eqIgno(wp.respHtml, "cmsq2100")) {
      if (wp.itemEmpty("ex_xx_card_no") == false) {
        checkXXcard();
      }
    }
    if (wp.buttonCode.equals("X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (wp.buttonCode.equals("Q1")) {
      /* 查詢功能 */
      strAction = "idno";
      wp.colSet("pageType", "idno");
      resetBtn();
      wp.colSet("bt_class1", "btOther2");
      if (wp.itemEmpty("ex_idno") == false) {
        zzVipColor(wp.itemStr2("ex_idno"));
      } else if (wp.itemEmpty("ex_card_no") == false) {
        zzVipColor(wp.itemStr2("ex_card_no"));
      }
      queryIdno();
    } else if (wp.buttonCode.equals("Q2")) {
      /* 查詢功能 */
      strAction = "acno";
      wp.colSet("pageType", "acno");
      resetBtn();
      wp.colSet("bt_class2", "btOther2");
      if (wp.itemEmpty("ex_idno") == false) {
        zzVipColor(wp.itemStr2("ex_idno"));
      } else if (wp.itemEmpty("ex_card_no") == false) {
        zzVipColor(wp.itemStr2("ex_card_no"));
      }
      queryFuncAcno();
    } else if (wp.buttonCode.equals("Q3")) {
      // -卡片資料--
      strAction = "card";
      wp.colSet("pageType", "card");
      resetBtn();
      wp.colSet("bt_class3", "btOther2");
      if (wp.itemEmpty("ex_idno") == false) {
        zzVipColor(wp.itemStr2("ex_idno"));
      } else if (wp.itemEmpty("ex_card_no") == false) {
        zzVipColor(wp.itemStr2("ex_card_no"));
      }
      queryFuncCard();
    } else if (wp.buttonCode.equals("Q4")) {
      /* 查詢功能 */
      strAction = "relcard";
      wp.colSet("pageType", "relcard");
      resetBtn();
      wp.colSet("bt_class4", "btOther2");
      if (wp.itemEmpty("ex_idno") == false) {
        zzVipColor(wp.itemStr2("ex_idno"));
      } else if (wp.itemEmpty("ex_card_no") == false) {
        zzVipColor(wp.itemStr2("ex_card_no"));
      }
      queryFuncRelcard();
    } else if (wp.buttonCode.equals("Q5")) {
      /* 查詢功能 */
      strAction = "relcard";
      wp.colSet("pageType", "relidno");
      resetBtn();
      wp.colSet("bt_class5", "btOther2");
      if (wp.itemEmpty("ex_idno") == false) {
        zzVipColor(wp.itemStr2("ex_idno"));
      } else if (wp.itemEmpty("ex_card_no") == false) {
        zzVipColor(wp.itemStr2("ex_card_no"));
      }
      queryFuncRelidno();
    } else if (wp.buttonCode.equals("Q6")) {
      /* 查詢功能 */
      strAction = "relbank";
      wp.colSet("pageType", "relbank");
      resetBtn();
      wp.colSet("bt_class6", "btOther2");
      if (wp.itemEmpty("ex_idno") == false) {
        zzVipColor(wp.itemStr2("ex_idno"));
      } else if (wp.itemEmpty("ex_card_no") == false) {
        zzVipColor(wp.itemStr2("ex_card_no"));
      }
      queryFuncRelbank();
    } else if (wp.buttonCode.equals("Q7")) {
      /* 查詢功能 */
      strAction = "tscc";
      wp.colSet("pageType", "xxcard");
      resetBtn();
      wp.colSet("bt_class7", "btOther2");
      if (wp.itemEmpty("ex_idno") == false) {
        zzVipColor(wp.itemStr2("ex_idno"));
      } else if (wp.itemEmpty("ex_card_no") == false) {
        zzVipColor(wp.itemStr2("ex_card_no"));
      }
      queryFuncXXcard();
    } else if (wp.buttonCode.equals("Q11")) {
      /* 查詢功能 */
      strAction = "rcv";
      wp.colSet("pageType", "rcv");
      resetBtn();
      wp.colSet("bt_class11", "btOther2");
      if (wp.itemEmpty("ex_idno") == false) {
        zzVipColor(wp.itemStr2("ex_idno"));
      } else if (wp.itemEmpty("ex_card_no") == false) {
        zzVipColor(wp.itemStr2("ex_card_no"));
      }
      queryFuncRCV();
    } else if (wp.buttonCode.equals("Q12")) {
      /* 查詢功能 */
      strAction = "ppcard";
      wp.colSet("pageType", "ppcard");
      resetBtn();
      wp.colSet("bt_class12", "btOther2");
      if (wp.itemEmpty("ex_idno") == false) {
        zzVipColor(wp.itemStr2("ex_idno"));
      } else if (wp.itemEmpty("ex_card_no") == false) {
        zzVipColor(wp.itemStr2("ex_card_no"));
      }
      queryFuncPPCARD();
    } else if (eqIgno(wp.buttonCode, "R")) {
      /* 資料讀取 */
      dataRead();
    } else if (wp.buttonCode.equals("M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (wp.buttonCode.equals("S")) {
      /* 動態查詢 */
      querySelect();
    } else if (wp.buttonCode.equals("S1")) {
      /* 動態查詢 */
      querySelectIDNO();
    } else if (wp.buttonCode.equals("S2")) {
      /* 動態查詢 */
      querySelectACNO();
    } else if (wp.buttonCode.equals("S3")) {
      /* 動態查詢 */
      querySelectCard();
    } else if (wp.buttonCode.equals("S4")) {
      /* 動態查詢 */
      querySelectRelcard();
    } else if (wp.buttonCode.equals("S5")) {
      /* 動態查詢 */
      querySelectRelidno();
    } else if (wp.buttonCode.equals("S7")) {
      /* 動態查詢 */
      querySelectTscc();
    } else if (wp.buttonCode.equals("S8")) {
      /* 動態查詢 */
      querySelectHce();
    } else if (wp.buttonCode.equals("S9")) {
      /* 動態查詢 */
      querySelectTpan();
    } else if (wp.buttonCode.equals("S10")) {
      /* 動態查詢 */
      querySelectIpass();
    } else if (wp.buttonCode.equals("S12")) {
      /* 動態查詢 */
      querySelectPpcard();
    } else if (wp.buttonCode.equals("S13")) {
      /* 動態查詢 */
      querySelectIchcard();
    } else if (wp.buttonCode.equals("S14")) {
        /* 動態查詢 */
    	querySelectTsccVDCard();
    } else if (wp.buttonCode.equals("S15")) {
        /* 動態查詢 */
    	querySelectOEMPay();
    } else if (wp.buttonCode.equals("S16")) {
        /* 動態查詢 */
    	querySelectEpay();
    } else if (wp.buttonCode.equals("L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (wp.buttonCode.equals("C")) {
      /* 啟動TWMP卡片狀態查詢 */
      wfCallBatch();
    } else if (wp.buttonCode.equals("C1")) {
      /* 查詢TWMP回覆碼 */
      wfCheckSirResp();
    }
	if (getIsQueryOverLimit()) {
		alertErr2("查詢筆數超出網頁可顯示筆數，請增加查詢條件");
	}

  }



@Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  void checkXXcard() {
	  StringBuffer sqlBf = new StringBuffer();
	  sqlBf.append(" select card_no from ich_card where ich_card_no = ?  union ")
	  		   .append(" select card_no from ips_card where ips_card_no = ?  union ")
	  		   .append(" select card_no from hce_card where v_card_no = ?  union ")
	  		   .append(" select card_no from tsc_card where tsc_card_no = ?  union ")
	  		   .append(" select vd_card_no as card_no from tsc_vd_card where tsc_card_no = ? union " )
	  		   .append(" select card_no from oempay_card where v_card_no = ? union ")
	  		   .append(" select card_no from epay_card where v_card_no = ? ")
	  		   .append(commSqlStr.rownum(1));


    sqlSelect(sqlBf.toString(), new Object[] {wp.itemStr2("ex_xx_card_no"), wp.itemStr2("ex_xx_card_no"),
        wp.itemStr2("ex_xx_card_no"), wp.itemStr2("ex_xx_card_no"),  wp.itemStr2("ex_xx_card_no"), wp.itemStr2("ex_xx_card_no")
        , wp.itemStr2("ex_xx_card_no")});

    if (sqlRowNum > 0) {
      wp.colSet("ex_card_no", sqlStr("card_no"));
      wp.itemSet("ex_card_no", sqlStr("card_no"));
    }
  }

  @Override
  public void queryFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  public void queryIdno() throws Exception {
    String lsWhere1 = "", lsWhere2 = "";
    if (!wp.itemEmpty("ex_idno")) {
        if (wp.itemStr("ex_idno").length() == 10) {
          lsWhere1 += " and A.id_no = :id_no ";
          lsWhere2 += " and A.id_no = :id_no ";
          setString("id_no", wp.itemStr("ex_idno"));
        } else if (wp.itemStr("ex_idno").length() >= 7 && wp.itemStr("ex_idno").length() <= 9) {
          lsWhere1 += " and A.id_no like :id_no ";
          lsWhere2 += " and A.id_no like :id_no ";
          setString("id_no", wp.itemStr("ex_idno")+"%");
        } else {
          alertErr2("身分證號 輸入不完整");
          return;
        }
      }

    if (!wp.itemEmpty("ex_chi_name")) {
        lsWhere1 += " and A.chi_name = :chi_name ";
        lsWhere2 += " and A.chi_name = :chi_name ";
        setString("chi_name", wp.itemStr("ex_chi_name"));
    }

    if (!wp.itemEmpty("ex_card_no")) {
        lsWhere1 += " and A.id_p_seqno in (select id_p_seqno from crd_card where card_no = :card_no ) ";
        lsWhere2 += " and A.id_p_seqno in (select id_p_seqno from dbc_card where card_no = :card_no ) ";
        // ls_where1 += " and B.card_no = '"+wp.item_ss("ex_card_no")+"' ";
        // ls_where2 += " and B.card_no = '"+wp.item_ss("ex_card_no")+"' ";
        setString("card_no", wp.itemStr("ex_card_no"));
      }

    if (!wp.itemEmpty("ex_acct_no")) {
        lsWhere1 += " and 1=2 ";
        lsWhere2 += " and A.id_p_seqno in (select id_p_seqno from dbc_card where acct_no = :acct_no ) ";
        // ls_where2 += " and B.acct_no ='"+wp.item_ss("ex_acct_no")+"' ";
        setString("acct_no", wp.itemStr("ex_acct_no"));
      }

    if (!wp.itemEmpty("ex_offi_area") && !wp.itemEmpty("ex_offi_telno")) {
        lsWhere1 += " and A.office_area_code1 = :office_area_code1 ";
        lsWhere2 += " and A.office_area_code1 = :office_area_code1 ";
        setString("office_area_code1", wp.itemStr("ex_offi_area"));
      }

    if (!wp.itemEmpty("ex_home_area") && !wp.itemEmpty("ex_home_telno")) {
        lsWhere1 += " and ("
        	+ "      (A.home_area_code1 = :home_area_code and A.home_tel_no1 = :home_tel_no ) "
            + "  or (A.home_area_code2 = :home_area_code and A.home_tel_no2 = :home_tel_no))";
        lsWhere2 += " and ("
            	+ "      (A.home_area_code1 = :home_area_code and A.home_tel_no1 = :home_tel_no ) "
                + "  or (A.home_area_code2 = :home_area_code and A.home_tel_no2 = :home_tel_no))";
        setString("home_tel_no", wp.itemStr("ex_home_telno"));
        setString("home_area_code", wp.itemStr("ex_home_area"));
      }

    if (!wp.itemEmpty("ex_cellar_no")) {
        lsWhere1 += " and A.cellar_phone = :cellar_phone ";
        lsWhere2 += " and A.cellar_phone = :cellar_phone ";
        setString("cellar_phone", wp.itemStr("ex_cellar_no"));
      }

    if (empty(lsWhere1) && empty(lsWhere2)) {
      alertErr2("請輸入查詢條件");
      return;
    }

    wp.setQueryMode();

    wp.sqlCmd = " select " + " 'N' as debit_flag , " + " A.id_no , " + " A.chi_name , "
        + " B.eng_name as db_ename , " + " A.birthday , " + " A.sex , " + " A.id_p_seqno "
        + " from crd_idno A LEFT JOIN crd_card B ON A.ID_P_SEQNO = B.ID_P_SEQNO AND B.eng_name <> '' " 
        + " where 1=1 " + lsWhere1 
        + " union " 
        + " select "
        + " 'Y' as debit_flag , " + " A.id_no , " + " A.chi_name , " + " A.eng_name as db_ename , "
        + " A.birthday , " + " A.sex , " + " A.id_p_seqno " 
        + " from dbc_idno A LEFT JOIN dbc_card B ON A.ID_P_SEQNO = B.ID_P_SEQNO AND B.eng_name <> '' " 
        + " where 1=1 "+ lsWhere2 
        + " order by 1 ";

    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
  }

  // public void queryRead_idno() throws Exception {
  // wp.pageControl();
  //
  // wp.selectSQL = "debit_flag,"
  // + " id_no,"
  // + " chi_name,"
  // + " '' as db_ename,"
  // + " birthday,"
  // + " sex,"
  // + " id_p_seqno"
  // ;
  // wp.daoTable = "vall_idno ";
  // if (empty(wp.whereStr)) {
  // wp.whereStr = " ORDER BY 1";
  // }
  // pageQuery();
  //
  // wp.setListCount(1);
  // if (sql_nrow <= 0) {
  // err_alert("此條件查無資料");
  // return;
  // }
  //
  // wp.setPageValue();
  //
  // }

  public void querySelectIDNO() throws Exception {
    dataKK1 = wp.itemStr("data_k1");
    dataKK2 = wp.itemStr("data_k2");
    dataReadIDNO();
  }

  public void dataReadIDNO() throws Exception {
    if (eqIgno(dataKK2, "N")) {
      wp.selectSQL = " *  ";
      wp.daoTable = "crd_idno";
      wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "id_p_seqno");
      logSql();
      pageSelect();
      if (sqlNotFind()) {
        alertErr("查無資料, key=" + dataKK1);
        return;
      }
      cmsm01.Cmsm2010Idno idno = new cmsm01.Cmsm2010Idno();
      idno.setConn(wp);
      String [] arr=new String[] {};
      arr=idno.selectCmsChgColumnLog("crd_idno","cellar_phone",wp.colStr("id_no"));
      wp.colSet("c_phone_chg_date", arr[0]);
      wp.colSet("c_phone_chg_time", arr[1]);
      decodeIdno();
      selectExt();
      selectLine();
    } else if (eqIgno(dataKK2, "Y")) {
      qqDebit = "Y";

      wp.selectSQL = " *  ";
      wp.daoTable = "dbc_idno";
      wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "id_p_seqno");

      pageSelect();
      if (sqlNotFind()) {
        alertErr("查無資料, key=" + dataKK1);
        return;
      }
      cmsm01.Cmsm2010Idno idno = new cmsm01.Cmsm2010Idno();
      idno.setConn(wp);
      String [] arr=new String[] {};
      arr=idno.selectCmsChgColumnLog("dbc_idno","cellar_phone",wp.colStr("id_no"));
      wp.colSet("c_phone_chg_date", arr[0]);
      wp.colSet("c_phone_chg_time", arr[1]);
      selectExtY();
      decodeDbIdno();
    }

  }

  void decodeDbIdno() {
	  CmsFunc cmsFunc = new CmsFunc(wp);
	  
	  if ( ! wp.colEmpty("education")) {
		  wp.colSet("tt_education", "." + cmsFunc.getEducationDesc(wp.colStr("education")));
	  }
	  if ( ! wp.colEmpty("business_code")) {
		  wp.colSet("tt_business_code", "." + cmsFunc.getBusinessDesc(wp.colStr("business_code")));
	  }
	  if ( ! wp.colEmpty("marriage")) {
		  wp.colSet("tt_marriage", "." + ecsfunc.DeCodeCms.marriageCode(wp.colStr("marriage")));
	  }
	  
    if (wp.colEq("nation", "1")) {
      wp.colSet("tt_nation", ".國內");
    } else if (wp.colEq("nation", "2")) {
      wp.colSet("tt_nation", ".國外");
    }

    if (wp.colEq("sex", "1")) {
      wp.colSet("tt_sex", ".男");
    } else if (wp.colEq("sex", "2")) {
      wp.colSet("tt_sex", ".女");
    }

    String sql6 = " select " + " fh_flag , " + " non_asset_balance , " + " non_credit_amt "
            + " from crd_correlate " 
    		+ " where correlate_id = ? ";

    sqlSelect(sql6, new Object[] {wp.colStr("id_no")});

    if (sqlRowNum > 0) {
      wp.colSet("db_fh_flag", sqlStr("fh_flag"));
      wp.colSet("db_non_asset_balance", sqlStr("non_asset_balance"));
      wp.colSet("db_non_credit_amt", sqlStr("non_credit_amt"));
    } else {
      wp.colSet("db_fh_flag", "N");
      wp.colSet("db_non_asset_balance", "0");
      wp.colSet("db_non_credit_amt", "0");
    }

  }

  void selectExt() {
    wp.selectSQL =
        " * , decode(spec_busi_code,'01','軍火業','02','虛擬貨幣業務','03','空殼公司','99','非以上特殊行業別') as tt_spec_busi_code  ";
    wp.daoTable = "crd_idno_ext";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "id_p_seqno");
    logSql();
    pageSelect();
    if (sqlNotFind()) {
      selectOK();
    }

//    String sql1 = " select " + " id_no as new_id_no , " + " chg_date , " + " post_jcic_flag "
//        + " from crd_chg_id " + " where old_id_no = ? ";
//
//    sqlSelect(sql1, new Object[] {wp.colStr("id_no")});
//    if (sqlRowNum > 0) {
//      wp.colSet("crd_chg_id_id", sqlStr("new_id_no"));
//      wp.colSet("crd_chg_id_chg_date", sqlStr("chg_date"));
//      wp.colSet("crd_chg_id_post_jcic_flag", sqlStr("post_jcic_flag"));
//    }
    
    String sql1 = " select " + " old_id_no , " + " chg_date "
            + " from crd_chg_id " + " where id_no = ? ";

        sqlSelect(sql1, new Object[] {wp.colStr("id_no")});
        if (sqlRowNum > 0) {
          wp.colSet("crd_chg_id_no", sqlStr("old_id_no"));
          wp.colSet("crd_chg_id_date", sqlStr("chg_date"));
        }
  }

  void selectLine() {
    String sql1 = " select line_id from mkt_line_cust where id_no = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("id_no")});
    if (sqlRowNum > 0) {
      wp.colSet("db_line_binding", "Y");
      wp.colSet("db_line_id", sqlStr("line_id"));

      // --消費提醒通知
      String sql2 = " select count(*) as db_cnt1 from mkt_line_subscribe "
          + " where subscribe = 'ConsumptionNotification' " + " and line_id = ? ";

      sqlSelect(sql2, new Object[] {sqlStr("line_id")});
      if (sqlNum("db_cnt1") > 0)
        wp.colSet("db_line_cn", "Y");

      // --繳費提醒通知
      String sql3 = " select count(*) as db_cnt2 from mkt_line_subscribe "
          + " where subscribe = 'PaymentReminder' " + " and line_id = ? ";

      sqlSelect(sql3, new Object[] {sqlStr("line_id")});
      if (sqlNum("db_cnt2") > 0)
        wp.colSet("db_line_pr", "Y");

      // --繳費入帳通知
      String sql4 = " select count(*) as db_cnt3 from mkt_line_subscribe "
          + " where subscribe = 'PaymentNotice' " + " and line_id = ? ";

      sqlSelect(sql4, new Object[] {sqlStr("line_id")});
      if (sqlNum("db_cnt3") > 0)
        wp.colSet("db_line_pn", "Y");

      // --額度異動通知
      String sql5 = " select count(*) as db_cnt4 from mkt_line_subscribe "
          + " where subscribe = 'QuotaTransaction' " + " and line_id = ? ";

      sqlSelect(sql5, new Object[] {sqlStr("line_id")});
      if (sqlNum("db_cnt4") > 0)
        wp.colSet("db_line_qt", "Y");

      // --紅利點數到期提醒
      String sql6 = " select count(*) as db_cnt5 from mkt_line_subscribe "
          + " where subscribe = 'PointsExpire' " + " and line_id = ? ";

      sqlSelect(sql6, new Object[] {sqlStr("line_id")});
      if (sqlNum("db_cnt5") > 0)
        wp.colSet("db_line_pe", "Y");

      // --申請信用卡補件通知
      String sql7 = " select count(*) as db_cnt6 from mkt_line_subscribe "
          + " where subscribe = 'ApplyCreditcard' " + " and line_id = ? ";

      sqlSelect(sql7, new Object[] {sqlStr("line_id")});
      if (sqlNum("db_cnt6") > 0)
        wp.colSet("db_line_ac", "Y");

      // --行動帳單
      String sql8 = " select count(*) as db_cnt7 from mkt_line_subscribe "
          + " where subscribe = 'MobileBill' " + " and line_id = ? ";

      sqlSelect(sql8, new Object[] {sqlStr("line_id")});
      if (sqlNum("db_cnt7") > 0)
        wp.colSet("db_line_mb", "Y");

    } else {
      wp.colSet("db_line_binding", "N");
    }
  }

  void selectExtY() {
    wp.selectSQL = " *  ";
    wp.daoTable = "dbc_idno_ext";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "id_p_seqno");
    logSql();
    pageSelect();
    wp.notFound = "N";
    
	String sql1 = " select " + " id , " + " crt_date " + " from dbc_chg_id " + " where aft_id = ? ";

	sqlSelect(sql1, new Object[] { wp.colStr("id_no") });
	if (sqlRowNum > 0) {
		wp.colSet("crd_chg_id_no", sqlStr("id"));
		wp.colSet("crd_chg_id_date", sqlStr("crt_date"));
	}
  }

  void decodeIdno() {
	  CmsFunc cmsFunc = new CmsFunc(wp);
	  
	  if ( ! wp.colEmpty("education")) {
		  wp.colSet("tt_education", "." + cmsFunc.getEducationDesc(wp.colStr("education")));
	  }
	  if ( ! wp.colEmpty("business_code")) {
		  wp.colSet("tt_business_code", "." + cmsFunc.getBusinessDesc(wp.colStr("business_code")));
	  }
	  if ( ! wp.colEmpty("marriage")) {
		  wp.colSet("tt_marriage", "." + ecsfunc.DeCodeCms.marriageCode(wp.colStr("marriage")));
	  }

    String sql2 = "select full_chi_name " + " from gen_brn" + " where branch =? ";
    sqlSelect(sql2, new Object[] {wp.colStr("staff_br_no")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_staff_br_no", "." + sqlStr("full_chi_name"));
    }

    String sql4 = " select " + " trial_type , " + " trial_date , " + " risk_group , "
        + " action_code " + " from rsk_trial_idno " + " where id_p_seqno = ? ";
    sqlSelect(sql4, new Object[] {wp.colStr("id_p_seqno")});
    if (sqlRowNum > 0) {
      if (eqIgno(sqlStr("trial_type"), "1")) {
        wp.colSet("db_trial_type", "1.人工");
      } else if (eqIgno(sqlStr("trial_type"), "2")) {
        wp.colSet("db_trial_type", "2.批次");
      }

      wp.colSet("db_trial_date", sqlStr("trial_date"));
      wp.colSet("db_trial_risk_group", sqlStr("risk_group"));
      wp.colSet("db_trial_action_code", sqlStr("action_code"));
    }

    String sql5 = " select " + " substr(rpad(income_score,12,''),1,12) as wk_income_score , "
        + " score_yymm " + " from mkt_contri_idno " + " where id_p_seqno = ? ";
    sqlSelect(sql5, new Object[] {wp.colStr("id_p_seqno")});
    if (sqlRowNum > 0) {
      int liScoreLen = sqlStr("wk_income_score").length();
      wp.colSet("db_contri_ym", sqlStr("score_yymm"));
      for (int aa = 1; aa <= liScoreLen; aa++) {
        if (aa <= 9) {
          wp.colSet("wk_income0" + aa, commString.mid(sqlStr("wk_income_score"), aa - 1, 1));
        } else {
          wp.colSet("wk_income" + aa, commString.mid(sqlStr("wk_income_score"), aa - 1, 1));
        }

      }
    }

    String sql6 = " select " + " fh_flag , " + " non_asset_balance , " + " non_credit_amt "
        + " from crd_correlate " + " where correlate_id = ? ";

    sqlSelect(sql6, new Object[] {wp.colStr("id_no")});

    if (sqlRowNum > 0) {
      wp.colSet("db_fh_flag", sqlStr("fh_flag"));
      wp.colSet("db_non_asset_balance", sqlStr("non_asset_balance"));
      wp.colSet("db_non_credit_amt", sqlStr("non_credit_amt"));
    } else {
      wp.colSet("db_fh_flag", "N");
      wp.colSet("db_non_asset_balance", "0");
      wp.colSet("db_non_credit_amt", "0");
    }

    String sql7 = " select acno_p_seqno from crd_card where id_p_seqno = ? " + commSqlStr.rownum(1);
    sqlSelect(sql7, new Object[] {wp.colStr("id_p_seqno")});

    if (sqlRowNum > 0) {
      String sql8 = " select " + " aum_flag " + " from crd_aum " + " where acno_p_seqno = ? ";

      sqlSelect(sql8, new Object[] {sqlStr("acno_p_seqno")});

      if (sqlRowNum > 0) {
        if (eqIgno(sqlStr("aum_flag"), "0")) {
          wp.colSet("db_card_aum", "為3000 萬(含) 以上");
        } else if (eqIgno(sqlStr("aum_flag"), "1")) {
          wp.colSet("db_card_aum", "為1000萬(含)~3000萬");
        } else if (eqIgno(sqlStr("aum_flag"), "2")) {
          wp.colSet("db_card_aum", "為300萬(含)~1000 萬");
        } else {
          wp.colSet("db_card_aum", "未達300萬或不為理財客戶");
        }
      }
    }

    if (wp.colEq("market_agree_base", "0")) {
      wp.colSet("tt_market_agree_base", "不同意");
    } else if (wp.colEq("market_agree_base", "1")) {
      wp.colSet("tt_market_agree_base", "同意共銷");
    } else if (wp.colEq("market_agree_base", "2")) {
      wp.colSet("tt_market_agree_base", "同意共享");
    }
  }

// ---acno
  public void queryFuncAcno() throws Exception {
    String lsWhere1 = "";
    String lsWhere2 = "";

    String lsIdno = wp.itemStr("ex_idno");
    if (lsIdno.length() == 10) {
      lsWhere1 +=
          " and aa.id_p_seqno in (select id_p_seqno from crd_idno where id_no = :id_no )";
      lsWhere2 +=
          " and id_p_seqno in (select id_p_seqno from dbc_idno where id_no = :id_no )";
      setString("id_no", lsIdno);
    } else if (lsIdno.length() >= 7 && lsIdno.length() <= 9) {
      lsWhere1 += " and aa.id_p_seqno in (select id_p_seqno from crd_idno where id_no like :id_no)";
      lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where id_no like :id_no )";
      setString("id_no", lsIdno + "%");
    } else if (lsIdno.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere1 += " and aa.id_p_seqno in (select id_p_seqno from crd_idno where chi_name = :chi_name)";
        lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where chi_name = :chi_name )";
        setString("chi_name", wp.itemStr("ex_chi_name"));
      }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere1 += " and aa.acno_p_seqno in (select acno_p_seqno from crd_card where card_no = :card_no )";
        lsWhere2 += " and p_seqno in (select p_seqno from dbc_card where card_no = :card_no )";
        setString("card_no", wp.itemStr("ex_card_no"));
      }

    if (!empty(wp.itemStr("ex_acct_no"))) {
        lsWhere1 = " ";
        lsWhere2 = " and p_seqno in (select p_seqno from dbc_card where acct_no = :acct_no )";
        setString("acct_no", wp.itemStr("ex_acct_no"));
      }

    if (empty(lsWhere1) && empty(lsWhere2)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    wp.setQueryMode();
    // -act_acno-
    wp.sqlCmd = "SELECT 'N' as debit_flag, aa.acno_p_seqno" + ", '信用卡' as tt_debit_flag"
        + ", aa.acct_type" + ", aa.acct_key " + ", aa.corp_p_seqno" + ", aa.id_p_seqno" + ", aa.card_indicator"
        + ",  aa.combo_acct_no" + ", aa.sale_flag" + ", uf_acno_name(aa.acno_p_seqno) as db_corpname"
        + ", aa.id_p_seqno" + ", hex(aa.rowid) as rowid" 
        + " FROM act_acno aa  " 
        + " where 1=1 " + lsWhere1;
    // -ecs_act_acno-
    wp.sqlCmd += " UNION " + "SELECT 'Z' as debit_flag, aa.acno_p_seqno " + ", '瘦身' as tt_debit_flag"
        + ", aa.acct_type" + ", aa.acct_key" + ", aa.corp_p_seqno" + ", aa.id_p_seqno" + ", aa.card_indicator"
        + ", aa.combo_acct_no" + ", aa.sale_flag" + ", uf_acno_name(aa.acno_p_seqno) as db_corpname"
        + ", aa.id_p_seqno" + ", hex(aa.rowid) as rowid" 
        + " FROM ecs_act_acno aa  " 
        + " where 1=1 "
        + lsWhere1;
    if (empty(lsWhere1)) {
      wp.sqlCmd = "";
    }
    if (!empty(lsWhere1) && !empty(lsWhere2)) {
      wp.sqlCmd += " union ";
    }
    if (!empty(lsWhere2)) {
      wp.sqlCmd += " SELECT 'Y' as debit_flag, p_seqno as acno_p_seqno"
          + ", 'debit卡' as tt_debit_flag" + ", acct_type" + ", acct_key" + ", corp_p_seqno"
          + ", id_p_seqno" + ", card_indicator" + ", acct_no as combo_acct_no "
          + ", 'N' as sale_flag" + ", uf_VD_acno_name(p_seqno) as db_corpname" + ", id_p_seqno"
          + ", hex(rowid) as rowid" 
          + " FROM dba_acno " 
          + " where 1=1 " + lsWhere2
          + " order by 1 , acct_type ";

      this.pageQuery();
      
      listTtDateAcno();
      wp.setListCount(2);
      if (sqlRowNum <= 0) {
        alertErr2("此條件查無資料");
        return;
      }
      
    }
    // -dbA_acno-

  }

  void listTtDateAcno() {
	  String sql1 = " select chi_name from crd_corp where corp_p_seqno = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "card_indicator", "1")) {
        wp.colSet(ii, "tt_card_indicator", "一般卡");
      } else if (wp.colEq(ii, "card_indicator", "2")) {
        wp.colSet(ii, "tt_card_indicator", "商務卡");
      } 
      
      if (wp.colEmpty(ii, "corp_p_seqno") == false) {
          sqlSelect(sql1, new Object[] {wp.colStr(ii, "corp_p_seqno")});
          if (sqlRowNum > 0) {
            wp.colSet(ii, "db_corpname", sqlStr("chi_name"));
          }
      }

    }
  }

  public void querySelectACNO() throws Exception {
    dataKK1 = wp.itemStr("data_k1");
    dataKK3 = wp.itemStr("data_k3");
    dataReadACNO();
  }

  public void dataReadACNO() throws Exception {
	  String chgTable = "";
    if (eqIgno(dataKK3, "N")) {
    	chgTable = "act_acno";
      selectCrdIdnoByIdPSeqno();
      daoTid = "A.";
      wp.selectSQL = " A.* , " + " B.block_status , " + " B.block_reason1 , "
          + " B.block_reason2 , " + " B.block_reason3 , " + " B.block_reason4 , "
          + " B.block_reason5 , " + " B.spec_status ";
      wp.daoTable = "act_acno A , cca_card_acct B ";
      wp.whereStr = " where A.acno_p_seqno = B.acno_p_seqno" + sqlCol(dataKK1, "A.acno_p_seqno");

      pageSelect();
      if (sqlNotFind()) {
        alertErr("查無資料, key=" + dataKK1);
        return;
      }
      dataReadWkdataAcno();
      selectActAcctCurr();
      wp.setListCount(1);
      queryAfterAcno();
    } else {
      qqDebit = "Y";
      chgTable = "dba_acno";
      selectDbcIdno();
      wp.selectSQL = " *  ";
      wp.daoTable = "dba_acno";
      wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "p_seqno");

      pageSelect();
      if (sqlNotFind()) {
        alertErr("查無資料, key=" + dataKK1);
        return;
      }
      dataReadWkdataDbaAcno();
    }
    
//	 cmsm01.Cmsm2010Idno idno = new cmsm01.Cmsm2010Idno();
//	    String [] arr=new String[] {};
//	    idno.setConn(wp);    
//	    arr=idno.selectCmsChgColumnLog(chgTable,"e_mail_ebill",wp.itemStr("ex_idno"));
//	    wp.colSet("e_mail_ebill_date", arr[0]);

  }

  void dataReadWkdataDbaAcno() {
    // --帳號
    wp.colSet("wk_acctkey", wp.colStr("acct_type") + "-" + wp.colStr("acct_key"));
    // --帳戶屬性
    if (wp.colEq("corp_act_flag", "Y")) {
      wp.colSet("tt_corp_act_flag", ".總繳");
    } else if (wp.colEq("corp_act_flag", "N")) {
      wp.colSet("tt_corp_act_flag", ".個繳");
    } else if (wp.colEq("corp_act_flag", "個繳")) {
      wp.colSet("tt_corp_act_flag", ".個繳");
    }
    // --往來狀態
    if ( ! wp.colEmpty("acct_status")) {
		wp.colSet("tt_acct_status", "." + DeCodeAct.acctStatus(wp.colStr("acct_status")));
    }

    // --凍結
    wp.colSet("tt_block_status", "." + DeCodeCms.getBlockStatusDesc(wp.colStr("block_status")));

    // --自動繳指示碼
    if (wp.colEq("autopay_indicator", "1")) {
      wp.colSet("tt_autopay_indicator", ".扣 TTL");
    } else if (wp.colEq("autopay_indicator", "2")) {
      wp.colSet("tt_autopay_indicator", ".扣 MP");
    }
    // --
    if (wp.colEq("rc_use_b_adj", "1")) {
      wp.colSet("tt_rc_use_b_adj", ".正常允用RC");
    } else if (wp.colEq("rc_use_b_adj", "2")) {
      wp.colSet("tt_rc_use_b_adj", ".例外允用 RC");
    } else if (wp.colEq("rc_use_b_adj", "3")) {
      wp.colSet("tt_rc_use_b_adj", ".不準允用 RC");
    }

    if (wp.colEq("special_stat_code", "1")) {
      wp.colSet("tt_special_stat_code", ".航空");
    } else if (wp.colEq("special_stat_code", "2")) {
      wp.colSet("tt_special_stat_code", ".掛號");
    } else if (wp.colEq("special_stat_code", "3")) {
      wp.colSet("tt_special_stat_code", ".人工處理");
    } else if (wp.colEq("special_stat_code", "4")) {
      wp.colSet("tt_special_stat_code", ".行員");
    } else if (wp.colEq("special_stat_code", "5")) {
      wp.colSet("tt_special_stat_code", ".其他");
    }
    // --風險行
    String sql1 = "select full_chi_name " + " from gen_brn" + " where branch =? ";
    sqlSelect(sql1, new Object[] {wp.colStr("risk_bank_no")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_risk_bank_no", "." + sqlStr("full_chi_name"));
    }

    // --有效正卡數 附卡數
    countValidCardDebit();

  }

  void selectDbcIdno() {
    String sql1 = "select id_p_seqno ," + " id_no ," + " id_no_code ," + " chi_name ," + " eng_name," + " sex ,"
        + " birthday ," + " resident_no ," + " other_cntry_code,"  + " passport_no "
        // + " tsc_market_flag as db_market_flag "
        + " from dbc_idno" + " where id_p_seqno =?";
    log("sql1:" + sql1);
    sqlSelect(sql1, new Object[] {wp.itemStr("data_k2")});
    wp.colSet("wk_idno", sqlStr("id_no") + "-" + sqlStr("id_no_code"));
    wp.colSet("birthday", sqlStr("birthday"));
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("idno.eng_name", sqlStr("eng_name"));
    wp.colSet("resident_no", sqlStr("resident_no"));
    wp.colSet("other_cntry_code", sqlStr("other_cntry_code"));
    wp.colSet("passport_no", sqlStr("passport_no"));

  }

  void dataReadWkdataAcno() {
    wp.colSet("A.wk_acctkey", wp.colStr("A.acct_type") + "-" + wp.colStr("A.acct_key"));
    if (wp.colEq("A.db_z13_tran_type", "A")) {
      wp.colSet("A.tt_db_z13_tran_type", "新增");
    } else if (wp.colEq("A.db_z13_tran_type", "U")) {
      wp.colSet("A.tt_db_z13_tran_type", "異動");
    } else if (wp.colEq("A.db_z13_tran_type", "D")) {
      wp.colSet("A.tt_db_z13_tran_type", "刪除");
    }

    wp.colSet("A.tt_bill_apply_flag", ecsfunc.DeCodeCms.billApplyFlagCode(wp.colStr("A.bill_apply_flag")));
    
    if (wp.colEq("A.rc_use_indicator", "1")) {
      wp.colSet("A.tt_rc_use_indicator", ".正常允用RC");
    } else if (wp.colEq("A.rc_use_indicator", "2")) {
      wp.colSet("A.tt_rc_use_indicator", ".例外允用RC");
    } else if (wp.colEq("A.rc_use_indicator", "3")) {
      wp.colSet("A.tt_rc_use_indicator", ".不得使用RC");
    }

    if (wp.colEq("A.rc_use_b_adj", "1")) {
      wp.colSet("A.tt_rc_use_b_adj", ".正常允用RC");
    } else if (wp.colEq("A.rc_use_b_adj", "2")) {
      wp.colSet("A.tt_rc_use_b_adj", ".例外允用 RC");
    } else if (wp.colEq("A.rc_use_b_adj", "3")) {
      wp.colSet("A.tt_rc_use_b_adj", ".不準允用 RC");
    }

    if (wp.colEq("A.special_stat_code", "1")) {
      wp.colSet("A.tt_special_stat_code", ".航空");
    } else if (wp.colEq("A.special_stat_code", "2")) {
      wp.colSet("A.tt_special_stat_code", ".掛號");
    } else if (wp.colEq("A.special_stat_code", "3")) {
      wp.colSet("A.tt_special_stat_code", ".人工處理");
    } else if (wp.colEq("A.special_stat_code", "4")) {
      wp.colSet("A.tt_special_stat_code", ".行員");
    } else if (wp.colEq("A.special_stat_code", "5")) {
      wp.colSet("A.tt_special_stat_code", ".其他");
    }

    // --帳戶屬性
    if (wp.colEq("A.corp_act_flag", "Y")) {
      wp.colSet("A.tt_corp_act_flag", ".總繳");
    } else if (wp.colEq("A.corp_act_flag", "N")) {
      wp.colSet("A.tt_corp_act_flag", ".個繳");
    } else if (wp.colEq("A.corp_act_flag", "個繳")) {
      wp.colSet("A.tt_corp_act_flag", ".個繳");
    }
    // --往來狀態
    if ( ! wp.colEmpty("A.acct_status")) {
		wp.colSet("A.tt_acct_status", "." + DeCodeAct.acctStatus(wp.colStr("A.acct_status")));
    }
    // --凍結
    wp.colSet("A.tt_block_status", "." + DeCodeCms.getBlockStatusDesc(wp.colStr("block_status")));
    

    String sql1 = "select full_chi_name " + " from gen_brn" + " where branch =? ";
    sqlSelect(sql1, new Object[] {wp.colStr("A.risk_bank_no")});
    if (sqlRowNum > 0) {
      wp.colSet("A.tt_risk_bank_no", "." + sqlStr("full_chi_name"));
    }

    // --異動經辦名字
    String sql2 = " select usr_cname from sec_user where usr_id = ? ";

    // --郵寄異動經辦
    sqlSelect(sql2, new Object[] {wp.colStr("A.paper_upd_user")});
    if (sqlRowNum > 0)
      wp.colSet("A.tt_paper_upd_user", "-" + sqlStr("usr_cname"));
    // --網路異動經辦
    sqlSelect(sql2, new Object[] {wp.colStr("A.internet_upd_user")});
    if (sqlRowNum > 0)
      wp.colSet("A.tt_internet_upd_user", "-" + sqlStr("usr_cname"));

  }

  //20230202 sunny add curr_change_accout
  void selectActAcctCurr() {
    daoTid = "C.";
    wp.selectSQL = "" + " p_seqno ," + " acct_type ," + " curr_code ," + " autopay_indicator ,"
        + " autopay_acct_bank ," + " autopay_acct_no ," + " autopay_id ," + " autopay_id_code ,"
        + " decode( nvl(autopay_id,''),'','', autopay_id || ' - ' || autopay_id_code) as autopay_id_and_desc , "
        + " autopay_dc_flag ," + " no_interest_flag ," + " no_interest_s_month ,"
        + " no_interest_e_month ," + " autopay_dc_indicator, " + " curr_change_accout ";
    wp.daoTable = "act_acct_curr";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "p_seqno");

    pageQuery();
    wp.notFound = "N"; //20200622 sunny 修改act_acct_curr查無資料的問題
    queryAfterAcctCurr();
  }

  void queryAfterAcctCurr() {
    String sql1 = "select curr_chi_name from ptr_currcode where curr_code = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "C.autopay_indicator", "1")) {
        wp.colSet(ii, "C.tt_autopay_indicator", ".扣 TTL");
      } else if (wp.colEq(ii, "C.autopay_indicator", "2")) {
        wp.colSet(ii, "C.tt_autopay_indicator", ".扣 MP");
      }

      if (wp.colEq(ii, "C.autopay_dc_indicator", "1")) {
        wp.colSet(ii, "C.tt_autopay_dc_indicator", ".扣 TTL");
      } else if (wp.colEq(ii, "C.autopay_dc_indicator", "2")) {
        wp.colSet(ii, "C.tt_autopay_dc_indicator", ".扣 MP");
      }

      sqlSelect(sql1, new Object[] {wp.colStr(ii, "C.curr_code")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "C.tt_curr_code", "." + sqlStr("curr_chi_name"));
      }

    }
  }

  void queryAfterAcno() {
    selectCorp();
    countValidCard();
  /*selectPdrate();   20200826 sunny disable */
    selectJCIC();
    
    if ( ! wp.colEmpty("acct_status")) {
		wp.colSet("tt_acct_status", "." + DeCodeAct.acctStatus(wp.colStr("acct_status")));
    }
    
    //年利率
    countYearRate();
  }

  public void selectCorp() {
    String sql1 = "select corp_no ," 
                          + " chi_name" 
                          + " from crd_corp" 
    		              + " where corp_p_seqno =?";
    sqlSelect(sql1, new Object[] {wp.colStr("A.corp_p_seqno")});
    if (sqlRowNum <= 0)
      return;
    
    wp.colSet("A.corp_no", sqlStr("corp_no"));
    wp.colSet("A.corp_chi_name", sqlStr("chi_name"));
  }

  public void countValidCard() {
    String sql1 =
        " select sum(decode(sup_flag,'0',1,0)) as valid_card_num," + " count(*) as valid_card_num2 "
            + " from crd_card" + " where acno_p_seqno =? and current_code = '0'";
    sqlSelect(sql1, new Object[] {wp.colStr("A.acno_p_seqno")});

    if (sqlRowNum <= 0)
      return;
    wp.colSet("A.valid_card_num", sqlStr("valid_card_num"));
    wp.colSet("A.valid_card_num2", sqlStr("valid_card_num2"));
  }

  public void countValidCardDebit() {
    String sql1 =
        " select sum(decode(sup_flag,'0',1,0)) as valid_card_num," + " count(*) as valid_card_num2 "
            + " from dbc_card" + " where p_seqno =? and current_code = '0'";
    sqlSelect(sql1, new Object[] {wp.colStr("acno_p_seqno")});

    if (sqlRowNum <= 0)
      return;
    wp.colSet("valid_card_num", sqlStr("valid_card_num"));
    wp.colSet("valid_card_num2", sqlStr("valid_card_num2"));
  }
  
  public void countYearRate() {
	  String acctType = wp.colStr("A.acct_type");
	  String pSeqno = wp.colStr("A.p_seqno");
	  
	  //取得標準利率
	  String sqlCmd = "SELECT REVOLVING_INTEREST1 FROM PTR_ACTGENERAL_N WHERE acct_type = :acctType ";
	  setString("acctType",acctType);
	  sqlSelect(sqlCmd);
	  double revolvingInterest1 = sqlNum("REVOLVING_INTEREST1"); 

	  //取得加減碼日利率
	  //20231204 當REVOLVE_INT_SIGN,REVOLVE_INT_SIGN2非+號(加碼者)則一律視同(-號)減碼。
	  sqlCmd = "SELECT DECODE(REVOLVE_INT_SIGN,'+',REVOLVE_INT_SIGN,'-') as REVOLVE_INT_SIGN, REVOLVE_INT_RATE ,"
	  		+ "DECODE(REVOLVE_INT_SIGN_2,'+',REVOLVE_INT_SIGN_2,'-') as REVOLVE_INT_SIGN_2,REVOLVE_INT_RATE_2 "
	  		+ "FROM act_acno WHERE p_seqno = :pSeqno ";
	  setString("pSeqno",pSeqno);
	  sqlSelect(sqlCmd);
	  String revolveIntSign = sqlStr("REVOLVE_INT_SIGN"); 
	  double revolveIntRate = sqlNum("REVOLVE_INT_RATE"); 
	  String revolveIntSign2 = sqlStr("REVOLVE_INT_SIGN_2"); 
	  double revolveIntRate2 = sqlNum("REVOLVE_INT_RATE_2"); 
	  
	  //計算年利率一
	  double rateTmp = 0;
	  if("-".equals(revolveIntSign))
		  rateTmp = new BigDecimal(revolvingInterest1).
	      subtract(BigDecimal.valueOf(revolveIntRate)).doubleValue();
	  else
		  rateTmp = new BigDecimal(revolvingInterest1).
		  add(BigDecimal.valueOf(revolveIntRate)).doubleValue();

	  double yearRate = new BigDecimal(rateTmp).	  
              multiply(BigDecimal.valueOf(3650)).
              divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
	  wp.colSet("year_rate", yearRate);

	  //計算年利率二
	  double rateTmp2 = 0;
	  if("-".equals(revolveIntSign2))
		  rateTmp2 = new BigDecimal(revolvingInterest1).
	      subtract(BigDecimal.valueOf(revolveIntRate2)).doubleValue();
	  else
		  rateTmp2 = new BigDecimal(revolvingInterest1).
		  add(BigDecimal.valueOf(revolveIntRate2)).doubleValue();
	  
	  double yearRate2 = new BigDecimal(rateTmp2).	  
              multiply(BigDecimal.valueOf(3650)).
              divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
	  wp.colSet("year_rate_2", yearRate2);
  }

  public void selectPdrate() {
    String sql1 = " select to_char(to_date(this_acct_month,'yyyymm') - 11 month,'yyyymm') as yymm01"
        + " from ptr_workday" + " where stmt_cycle =?";
    sqlSelect(sql1, new Object[] {wp.colStr("A.stmt_cycle")});
    wp.colSet("A.yymm01", sqlStr("yymm01"));
    int ss = 0;
    for (int ii = 11; ii >= 0; ii--) {
      String sql2 = " select uf_nvl(pdr_type,' ')||uf_nvl(pdr_class,' ') as wk_pd_rate" + ii
          + " from rsk_pdr_acno" + " where acno_p_seqno =?"
          + " and acct_month = to_char(to_date(?,'yyyymm') + " + ii + "  month,'yyyymm')";
      sqlSelect(sql2, new Object[] {wp.colStr("A.acno_p_seqno"), wp.colStr("A.yymm01")});
      ss++;
      if (ss >= 10) {
        wp.colSet("A.wk_pdr_rate" + ss, sqlStr("wk_pd_rate" + ii));
        if (wp.colEmpty("A.wk_pdr_rate" + ss))
          wp.colSet("A.wk_pdr_rate" + ss, "&nbsp;&nbsp;");
      } else {
        wp.colSet("A.wk_pdr_rate0" + ss, sqlStr("wk_pd_rate" + ii));
        if (wp.colEmpty("A.wk_pdr_rate0" + ss))
          wp.colSet("A.wk_pdr_rate0" + ss, "&nbsp;&nbsp;");
      }

    }

  }

  public void selectJCIC() {
    String sql1 = " select jcic_bad_debt_date as wk_jcic_bad_debt_date " + " from act_acno_ext"
        + " where p_seqno =?";
    sqlSelect(sql1, new Object[] {wp.colStr("A.acno_p_seqno")});
    wp.colSet("A.db_jcic_bad_debt_date", sqlStr("wk_jcic_bad_debt_date"));
  }

  public void selectZ13() {
    String sql1 = " select tran_type as db_z13_tran_type," + " inst_flag as db_z13_inst_flag  "
        + " from col_cs_instjcic" + " where p_seqno =? " + " and  nvl(proc_flag,'N') ='Y' ";
    sqlSelect(sql1, new Object[] {wp.colStr("A.acno_p_seqno")});
    wp.colSet("A.db_z13_tran_type", sqlStr("db_z13_inst_flag"));
  }

  // --------card
  public void queryFuncCard() throws Exception {
    String lsWhere1 = "";
    String lsWhere2 = "";

    String lsIdno = wp.itemStr("ex_idno");
    if (lsIdno.length() == 10) {
      lsWhere1 += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 and id_no = :id_no) ";
      lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where 1=1 and id_no = :id_no)";
      setString("id_no", lsIdno);
    } else if (lsIdno.length() >= 7 && lsIdno.length() <= 9) {
      lsWhere1 += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 and id_no like :id_no) ";
      lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where 1=1 and id_no like :id_no ) ";
      setString("id_no", lsIdno + "%");
    } else if (lsIdno.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere1 += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 and chi_name = :chi_name) ";
        lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where 1=1 and chi_name = :chi_name) ";
        setString("chi_name", wp.itemStr("ex_chi_name"));
      }

    String lsCardNo = wp.itemStr2("ex_card_no");
    if (!empty(lsCardNo)) {
      // ls_where1 +=" and id_p_seqno in (select id_p_seqno from
      // cca_card_base where 1=1 and debit_flag <>'Y'
      // "+sql_col(wp.item_ss("ex_card_no"),"card_no")+")";
      // ls_where2 +=" and id_p_seqno in (select id_p_seqno from
      // cca_card_base where 1=1 and debit_flag ='Y'
      // "+sql_col(wp.item_ss("ex_card_no"),"card_no")+")";
      lsWhere1 += " and card_no = :card_no ";
      lsWhere2 += " and card_no = :card_no ";
      setString("card_no", lsCardNo);
    }

    if (!empty(wp.itemStr("ex_acct_no"))) {
        lsWhere1 = " and combo_acct_no = :acct_no ";
        lsWhere2 = " and acct_no = :acct_no ";
        setString("acct_no", wp.itemStr("ex_acct_no"));
      }

    if (!empty(wp.itemStr("ex_tsc_card_no"))) {
        lsWhere1 = " and card_no in (select card_no from tsc_card where 1=1 and tsc_card_no = :tsc_card_no )";
        lsWhere2 = "";
        setString("tsc_card_no", wp.itemStr("ex_tsc_card_no"));
      }

    if (empty(lsWhere1) && empty(lsWhere2)) {
      alertErr2("請輸入查詢條件");
      return;
    }

    wp.setQueryMode();
    // -crd_card-
    wp.sqlCmd = " select " + " 'N' as debit_flag," + " acct_type ,"
        + " uf_acno_key(acno_p_seqno) as acct_key ," + " current_code ,"
        + " decode(current_code,'0',issue_date,oppost_date) ," + " card_no ," + " card_type ,"
        + " group_code ," + " oppost_reason ," + " issue_date , " + " oppost_date , "  + " activate_flag , "
        + " '' as qr_code ," + " uf_tt_card_type(card_type) as tt_card_type,"
        + " uf_tt_group_code(group_code) as group_name, " + " '信用卡' as tt_debit_flag ,"
        + " curr_code ," + " '' as qr_code," + " id_p_seqno ," + " hex(rowid) as rowid "
        + " FROM crd_card" + " where 1=1 " + lsWhere1;
    // -ECS_CRD_CARD-
    wp.sqlCmd += " UNION " + " select " + " 'Z' as debit_flag ," + " acct_type ,"
        + " (select acct_key from ecs_act_acno where acno_p_seqno = A.acno_p_seqno fetch first 1 rows only) as acct_key ,"
        + " current_code ," + " decode(current_code,'0',issue_date,oppost_date) ," + " card_no ,"
        + " card_type ," + " group_code ," + " oppost_reason ," + " issue_date , "
        + " oppost_date , " + " activate_flag , " + " '' as qr_code ," + " uf_tt_card_type(card_type) as tt_card_type,"
        + " uf_tt_group_code(group_code) as group_name," + " '瘦身' as tt_debit_flag ,"
        + " '' as curr_code, " + " '' as qr_code," + " id_p_seqno , " + " hex(rowid) as rowid"
        + " FROM ECS_CRD_CARD A " + " where 1=1 " + lsWhere1;
    if (empty(lsWhere1)) {
      wp.sqlCmd = "";
    }
    if (!empty(lsWhere1) && !empty(lsWhere2)) {
      wp.sqlCmd += " union ";
    }
    if (!empty(lsWhere2)) {
      wp.sqlCmd += " select" + " 'Y' as debit_flag," + " acct_type ,"
          + " uf_acno_key2(p_seqno,'Y') as acct_key ," + " current_code ,"
          + " decode(current_code,'0',issue_date,oppost_date) ," + " card_no ," + " card_type ,"
          + " group_code ," + " oppost_reason ," + " issue_date , " + " oppost_date , "  + " activate_flag , "
          + " '' as qr_code ," + " uf_tt_card_type(card_type) as tt_card_type,"
          + " uf_tt_group_code(group_code) as group_name," + " 'debit卡' as tt_debit_flag ,"
          + " '' as curr_code," + " '' as qr_code ," + " id_p_seqno , " + " hex(rowid) as rowid "
          + " FROM DBC_CARD " + " where 1=1 " + lsWhere2 + " order by 1,2,3,4,5 Desc ,6";

      this.pageQuery();

      if (sqlRowNum <= 0) {
        alertErr2("此條件查無資料");
        return;
      }
      wp.setListCount(3);
    }
    queryAfterCard();
    // -DBC_CARD-

  }

  void queryAfterCard() {
    String sql1 = " select opp_remark from cca_opp_type_reason where opp_status = ? ";
    String sql2 = " select curr_chi_name from ptr_currcode where curr_code = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (!empty(wp.colStr(ii, "oppost_reason"))) {
        sqlSelect(sql1, new Object[] {wp.colStr(ii, "oppost_reason")});
        if (sqlRowNum > 0) {
          wp.colSet(ii, "tt_oppost_reason", "." + sqlStr("opp_remark"));
        }
      }

      if (!empty(wp.colStr(ii, "curr_code"))) {
        sqlSelect(sql2, new Object[] {wp.colStr(ii, "curr_code")});
        if (sqlRowNum > 0) {
          wp.colSet(ii, "tt_curr_code", "." + sqlStr("curr_chi_name"));
        }
      }
      
      if (wp.colEq(ii, "activate_flag", "1")) {
			wp.colSet(ii, "tt_activate_flag", "未開");
		} else if (wp.colEq(ii, "activate_flag", "2")) {
			wp.colSet(ii, "tt_activate_flag", "已開");
		}
    }
  }

  public void querySelectCard() throws Exception {
    dataKK1 = wp.itemStr("data_k1");
    dataKK3 = wp.itemStr("data_k3");
    dataReadCard();
  }

  public void dataReadCard() throws Exception {
    if (eqIgno(dataKK3, "N")) {
      selectCrdIdnoByIdPSeqno();
      wp.selectSQL = " A.* ," + "uf_tt_card_type(A.card_type) as tt_card_type,"
          + " substr(A.web_acs_date,1,4) || substr(A.web_acs_date,5,2) || substr(A.web_acs_date,9,2) as web_acs_date ,"
          + " uf_acno_key(A.acno_p_seqno) as acct_key ,"
          + " uf_idno_id(A.major_id_p_seqno) as major_id,"
          + " uf_corp_name(A.corp_p_seqno) as corp_name,"
          + " uf_tt_group_code(A.group_code) as group_name, "
         // + "decode(act_acno.acct_status,'1','1.正常','2','2.逾放','3','3.催收','4','4.呆帳','5','5.結清',act_acno.acct_status) tt_acct_status, "
          + " decode(reissue_status,'1','已登錄待製卡','2','製卡中','3','製卡完成','4','製卡失敗') as tt_reissue_status ";
      wp.daoTable = "crd_card A ";
      wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "card_no");
      logSql();
      pageSelect();
      if (sqlNotFind()) {
        alertErr("查無資料, key=" + dataKK1);
        return;
      }
      wp.colSet("wk_acct", wp.colStr("acct_type") + "-" + wp.colStr("acct_key"));
      dataReadAfterCard();
    } else {
      selectDbcIdno();
      wp.selectSQL = " A.* , " + " A.acct_no as combo_acct_no ";
      wp.daoTable = "dbc_card A";
      wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "A.card_no");
      logSql();
      pageSelect();
      if (sqlNotFind()) {
        alertErr("查無資料, key=" + dataKK1);
        return;
      }
      wp.colSet("wk_acctkey", wp.colStr("acct_type") + "-" + wp.colStr("acct_key"));
      dataReadAfterCardY();
    }

  }

  public void dataReadAfterCard() {

    // --判斷電子票證
    if (wp.colEq("electronic_code", "00")) {
      // --非電子票證
    } else if (wp.colEq("electronic_code", "01")) {
      // --悠遊卡
      wp.colSet("db_tscc_flag", "Y");
    } else if (wp.colEq("electronic_code", "02")) {
      // --一卡通
      wp.colSet("db_ipass_flag", "Y");
    } else if (wp.colEq("electronic_code", "03")) {
      // --愛金卡icash
      wp.colSet("db_ich_flag", "Y");
    } else if (wp.colEq("electronic_code", "04")) {
      // --有錢卡Happy Cash
      // wp.col_set("db_tscc_flag", "Y");
    }

    // select_TSCC();
    // select_iPass();
    selectReturnCard();
    selectMobCard();
    selectNewCard();
    selectOldCard();
    selectExpireReason();
    selectMove();
    selectAddr();
    ttDataCard();
    selectReturn();
    selectOppreason();
    // select_ichCard();
  }

  void selectOppreason() {
    String sql1 = " select opp_remark from cca_opp_type_reason where opp_status = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("oppost_reason")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_oppost_reason", "_" + sqlStr("opp_remark"));
    }
  }

  void selectReturn() {
    String sql1 = " select " + " return_date , " + " reason_code , " + " proc_status "
        + " from crd_return " + " where 1=1 " + " and card_no = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});

    if (sqlRowNum <= 0)
      return;

    wp.colSet("return_date", sqlStr("return_date"));
    wp.colSet("reason_code", sqlStr("reason_code"));
    wp.colSet("proc_status", sqlStr("proc_status"));

    if (eqIgno(sqlStr("reason_code"), "01")) {
      wp.colSet("tt_reason_code", ".查無此公司");
    } else if (eqIgno(sqlStr("reason_code"), "02")) {
      wp.colSet("tt_reason_code", ".查無此人");
    } else if (eqIgno(sqlStr("reason_code"), "03")) {
      wp.colSet("tt_reason_code", ".遷移不明");
    } else if (eqIgno(sqlStr("reason_code"), "04")) {
      wp.colSet("tt_reason_code", ".地址欠詳");
    } else if (eqIgno(sqlStr("reason_code"), "05")) {
      wp.colSet("tt_reason_code", ".查無此址");
    } else if (eqIgno(sqlStr("reason_code"), "06")) {
      wp.colSet("tt_reason_code", ".收件人拒收");
    } else if (eqIgno(sqlStr("reason_code"), "07")) {
      wp.colSet("tt_reason_code", ".招領逾期");
    } else if (eqIgno(sqlStr("reason_code"), "08")) {
      wp.colSet("tt_reason_code", ".分行退件");
    } else if (eqIgno(sqlStr("reason_code"), "09")) {
      wp.colSet("tt_reason_code", ".其他");
    } else if (eqIgno(sqlStr("reason_code"), "10")) {
      wp.colSet("tt_reason_code", ".地址改變");
    }

    if (eqIgno(sqlStr("proc_status"), "1")) {
      wp.colSet("tt_proc_status", ".庫存");
    } else if (eqIgno(sqlStr("proc_status"), "2")) {
      wp.colSet("tt_proc_status", ".銷毀");
    } else if (eqIgno(sqlStr("proc_status"), "3")) {
      wp.colSet("tt_proc_status", ".寄出");
    } else if (eqIgno(sqlStr("proc_status"), "4")) {
      wp.colSet("tt_proc_status", ".申停");
    } else if (eqIgno(sqlStr("proc_status"), "5")) {
      wp.colSet("tt_proc_status", ".重製");
    } else if (eqIgno(sqlStr("proc_status"), "6")) {
      wp.colSet("tt_proc_status", ".寄出不封裝");
    }

  }

  public void dataReadAfterCardY() {

    String sql1 = " select " + " third_rsn , " + " third_rsn_ibm " + " from dbc_emboss "
        + " where card_no = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});
    wp.colSet("db_third_rsn", sqlStr("third_rsn"));
    wp.colSet("db_third_rsn_ibm", sqlStr("third_rsn_ibm"));

    /*--???
    ls_kind = this.of_getitem(1,'expire_chg_flag') 
    choose case ls_kind
    	case '1'
    		inv_columnextend.of_retrievedwc2("expire_reason","wf_type='NOTCHG_VD_S'")
    	case '2'
    		inv_columnextend.of_retrievedwc2("expire_reason","wf_type='NOTCHG_VD_O'")
    	case '3'
    		inv_columnextend.of_retrievedwc2("expire_reason","wf_type='NOTCHG_VD_M'")
    end choose
    --*/

    String sql2 = " select " + " decode(b.mail_zip,'',a.mail_zip,b.mail_zip) as ls_addr1 , "
        + " decode(b.mail_addr1,'',a.mail_addr1,b.mail_addr1) as ls_addr2 , "
        + " decode(b.mail_addr2,'',a.mail_addr2,b.mail_addr2) as ls_addr3 , "
        + " decode(b.mail_addr3,'',a.mail_addr3,b.mail_addr3) as ls_addr4 , "
        + " decode(b.mail_addr4,'',a.mail_addr4,b.mail_addr4) as ls_addr5 , "
        + " decode(b.mail_addr5,'',a.mail_addr5,b.mail_addr5) as ls_addr6 , "
        + " a.mail_type as ls_mail_type , " + " a.old_card_no as ls_old_card_no "
        + " from dbc_card_addr b , dbc_emboss a " + " where a.card_no = ? "
        + " and b.card_no = a.card_no " + " and a.emboss_source in ('3','4') ";
    sqlSelect(sql2, new Object[] {wp.colStr("card_no")});

    String sql3 = " select " + " return_date as ls_return1 , " + " proc_status as ls_return2 , "
        + " mail_no as ls_return3 , " + " mail_type as ls_return4 , "
        + " mail_date as ls_return5 , " + " mail_branch as ls_return6 , "
        + " reason_code as ls_return7 , " + " zip_code as ls_addr_ret1 , "
        + " mail_addr1 as ls_addr_ret2 , " + " mail_addr2 as ls_addr_ret3 , "
        + " mail_addr3 as ls_addr_ret4 , " + " mail_addr4 as ls_addr_ret5 , "
        + " mail_addr5 as ls_addr_ret6 " + " from dbc_return " + " where card_no = ? "
        + " order by mod_time desc " + commSqlStr.rownum(1);
    sqlSelect(sql3, new Object[] {wp.colStr("card_no")});

    wp.colSet("db_return_date", sqlStr("ls_return1"));
    wp.colSet("db_proc_status", sqlStr("ls_return2"));
    wp.colSet("db_mail_no", sqlStr("ls_return3"));
    wp.colSet("db_return_mail_type", sqlStr("ls_return4"));
    wp.colSet("db_mail_date", sqlStr("ls_return5"));
    wp.colSet("db_mail_branch", sqlStr("ls_return6"));
    wp.colSet("db_reason_code", sqlStr("ls_return7"));

    if (eqIgno(sqlStr("ls_mail_type"), "3") && eqIgno(sqlStr("ls_mail_type"), "4")
        && eqIgno(sqlStr("ls_mail_type"), "5")) {
      wp.colSet("db_mail_zip", sqlStr("ls_addr1"));
      wp.colSet("db_mail_addr1", sqlStr("ls_addr2"));
      wp.colSet("db_mail_addr2", sqlStr("ls_addr3"));
      wp.colSet("db_mail_addr3", sqlStr("ls_addr4"));
      wp.colSet("db_mail_addr4", sqlStr("ls_addr5"));
      wp.colSet("db_mail_addr5", sqlStr("ls_addr6"));
    }

    if (eqIgno(sqlStr("ls_return4"), "3") && eqIgno(sqlStr("ls_return4"), "4")
        && eqIgno(sqlStr("ls_return4"), "5")) {
      wp.colSet("db_retl_zip", sqlStr("ls_addr_ret1"));
      wp.colSet("db_ret_addr1", sqlStr("ls_addr_ret2"));
      wp.colSet("db_ret_addr2", sqlStr("ls_addr_ret3"));
      wp.colSet("db_ret_addr3", sqlStr("ls_addr_ret4"));
      wp.colSet("db_ret_addr4", sqlStr("ls_addr_ret5"));
      wp.colSet("db_ret_addr5", sqlStr("ls_addr_ret6"));
    }

    if (wp.colEq("sex", "1")) {
      wp.colSet("tt_sex", ".男");
    } else if (wp.colEq("sex", "2")) {
      wp.colSet("tt_sex", ".女");
    }

    String sql4 = " select opp_remark from cca_opp_type_reason where opp_status = ? ";
    sqlSelect(sql4, new Object[] {wp.colStr("oppost_reason")});
    wp.colSet("tt_opp_status", "." + sqlStr("opp_remark"));

    String sql5 = " select full_chi_name from gen_brn where branch = ? ";
    sqlSelect(sql5, new Object[] {wp.colStr("branch")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_branch", "." + sqlStr("full_chi_name"));
    }
    sqlSelect(sql5, new Object[] {wp.colStr("reg_bank_no")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_reg_bank_no", "." + sqlStr("full_chi_name"));
    }

    if (wp.colEq("reissue_reason", "1")) {
      wp.colSet("tt_reissue_reason", ".掛失重製");
    } else if (wp.colEq("reissue_reason", "2")) {
      wp.colSet("tt_reissue_reason", ".毀損重製");
    } else if (wp.colEq("reissue_reason", "3")) {
      wp.colSet("tt_reissue_reason", ".偽卡重製");
    } else if (wp.colEq("reissue_reason", "4")) {
      wp.colSet("tt_reissue_reason", ".星座卡重製");
    }

    if (wp.colEq("reissue_status", "1")) {
      wp.colSet("tt_reissue_status", ".已登錄待製卡");
    } else if (wp.colEq("reissue_status", "2")) {
      wp.colSet("tt_reissue_status", ".製卡中");
    } else if (wp.colEq("reissue_status", "3")) {
      wp.colSet("tt_reissue_status", ".製卡完成");
    } else if (wp.colEq("reissue_status", "4")) {
      wp.colSet("tt_reissue_status", ".製卡失敗");
    }

    if (wp.colEq("change_status", "1")) {
      wp.colSet("tt_change_status", ".續卡待製卡");
    } else if (wp.colEq("change_status", "2")) {
      wp.colSet("tt_change_status", ".製卡中");
    } else if (wp.colEq("change_status", "3")) {
      wp.colSet("tt_change_status", ".製卡完成");
    } else if (wp.colEq("change_status", "4")) {
      wp.colSet("tt_change_status", ".製卡失敗");
    }

    if (wp.colEq("change_reason", "1")) {
      wp.colSet("tt_change_reason", ".系統續卡");
    } else if (wp.colEq("change_reason", "2")) {
      wp.colSet("tt_change_reason", ".提前續卡");
    } else if (wp.colEq("change_reason", "3")) {
      wp.colSet("tt_change_reason", ".人工續卡");
    }

    if (wp.colEq("mail_type", "1")) {
      wp.colSet("tt_mail_type", ".普掛");
    } else if (wp.colEq("mail_type", "2")) {
      wp.colSet("tt_mail_type", ".限掛");
    } else if (wp.colEq("mail_type", "3")) {
      wp.colSet("tt_mail_type", ".卡務中心親取");
    } else if (wp.colEq("mail_type", "4")) {
      wp.colSet("tt_mail_type", ".分行");
    } else if (wp.colEq("mail_type", "5")) {
      wp.colSet("tt_mail_type", ".暫不寄");
    } else if (wp.colEq("mail_type", "6")) {
      wp.colSet("tt_mail_type", ".快捷");
    } else if (wp.colEq("mail_type", "7")) {
      wp.colSet("tt_mail_type", ".航空");
    } else if (wp.colEq("mail_type", "8")) {
      wp.colSet("tt_mail_type", ".快遞");
    } else if (wp.colEq("mail_type", "9")) {
      wp.colSet("tt_mail_type", ".其他");
    }

    String lsType = "";

    if (wp.colEq("expire_chg_flag", "1")) {
      wp.colSet("tt_expire_chg_flag", ".系統不續卡");
      lsType = "NOTCHG_KIND_S";
    } else if (wp.colEq("expire_chg_flag", "2")) {
      wp.colSet("tt_expire_chg_flag", ".預約不續卡");
      lsType = "NOTCHG_KIND_O";
    } else if (wp.colEq("expire_chg_flag", "3")) {
      wp.colSet("tt_expire_chg_flag", ".人工不續卡");
      lsType = "NOTCHG_KIND_M";
    }

    String sql6 = " select wf_desc as tt_expire_reason " + " from ptr_sys_idtab"
        + " where wf_type = ? " + "and wf_id = ? ";
    sqlSelect(sql6, new Object[] {lsType, wp.colStr("expire_reason")});
    wp.colSet("tt_expire_reason", "." + sqlStr("tt_expire_reason"));
    
    if ( ! wp.colEmpty("current_code")) {
		wp.colSet("tt_current_code", "." + ecsfunc.DeCodeCrd.currentCode(wp.colStr("current_code")));
	}

  }

  // void select_TSCC(){
  // String sql1 =" select count(*) as db_cnt"
  // +" from tsc_card"
  // +" where card_no =? "
  // +" and new_end_date =? ";
  // sqlSelect(sql1,new Object[]{
  // wp.col_ss("card_no"),wp.col_ss("new_end_date")
  // });
  // if(sql_num("db_cnt")>0)
  // wp.col_set("db_tscc_flag", "Y");
  // }
  //
  // void select_iPass(){
  // String sql1 =" select count(*) as db_cnt"
  // +" from ips_card"
  // +" where card_no =? "
  // +" and new_end_date =? ";
  // sqlSelect(sql1,new Object[]{
  // wp.col_ss("card_no"),wp.col_ss("new_end_date")
  // });
  // if(sql_num("db_cnt")>0)
  // wp.col_set("db_ipass_flag", "Y");
  // }
  //
  // void select_ichCard(){
  // String sql1 = "select count(*) as db_cnt from ich_card where card_no = ? and
  // new_end_date = ?";
  // sqlSelect(sql1,new Object[]{wp.col_ss("card_no"),wp.col_ss("new_end_date")});
  // if(sql_num("db_cnt")>0) wp.col_set("db_ich_flag", "Y");
  // }

  void selectReturnCard() {
    String sql1 = " select proc_status as db_return_proc ," + " return_date as db_return_date , "
        + " reason_code as db_return_reason , " + " package_date as db_package_date "
        + " from crd_return" + " where card_no =? "
        + " order by return_date desc fetch first 1 row only ";
    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});

    if (eqIgno(sqlStr("db_return_proc"), "1")) {
      wp.colSet("db_return_proc", "1.處理中");
    } else if (eqIgno(sqlStr("db_return_proc"), "2")) {
      wp.colSet("db_return_proc", "2.銷毀");
    } else if (eqIgno(sqlStr("db_return_proc"), "3")) {
      wp.colSet("db_return_proc", "3.寄出");
    } else if (eqIgno(sqlStr("db_return_proc"), "4")) {
      wp.colSet("db_return_proc", "4.申停");
    } else if (eqIgno(sqlStr("db_return_proc"), "5")) {
      wp.colSet("db_return_proc", "5.重製");
    } else if (eqIgno(sqlStr("db_return_proc"), "6")) {
      wp.colSet("db_return_proc", "6.寄出不封裝");
    }

    if (eqIgno(sqlStr("db_return_reason"), "01")) {
      wp.colSet("db_return_reason", "01.查無此公司");
    } else if (eqIgno(sqlStr("db_return_reason"), "02")) {
      wp.colSet("db_return_reason", "02.查無此人");
    } else if (eqIgno(sqlStr("db_return_reason"), "03")) {
      wp.colSet("db_return_reason", "03.遷移不明");
    } else if (eqIgno(sqlStr("db_return_reason"), "04")) {
      wp.colSet("db_return_reason", "04.地址欠詳");
    } else if (eqIgno(sqlStr("db_return_reason"), "05")) {
      wp.colSet("db_return_reason", "05.查無此址");
    } else if (eqIgno(sqlStr("db_return_reason"), "06")) {
      wp.colSet("db_return_reason", "06.收件人拒收");
    } else if (eqIgno(sqlStr("db_return_reason"), "07")) {
      wp.colSet("db_return_reason", "07.招領逾期");
    } else if (eqIgno(sqlStr("db_return_reason"), "08")) {
      wp.colSet("db_return_reason", "08.分行退件");
    } else if (eqIgno(sqlStr("db_return_reason"), "09")) {
      wp.colSet("db_return_reason", "09.其他");
    } else if (eqIgno(sqlStr("db_return_reason"), "10")) {
      wp.colSet("db_return_reason", "10.地址改變");
    }
    wp.colSet("db_package_date", sqlStr("db_package_date"));
    wp.colSet("db_return_date", sqlStr("db_return_date"));

  }

  void selectMobCard() {
    String sql1 = " select se_id as se_id ," + " mno_id as db_mno_id ,"
        + " mob_status as db_mob_status " + " from mob_card" + " where card_no =? "
        + " order by crt_date desc fetch first 1 row only ";
    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});
    wp.colSet("db_se_id", sqlStr("se_id"));
    wp.colSet("db_mno_id", sqlStr("db_mno_id"));
    wp.colSet("db_mob_status", sqlStr("db_mob_status"));

  }

  void selectNewCard() {
    if (empty(wp.colStr("new_card_no")))
      return;
    String sql1 = " select new_beg_date as db_new_beg_date ," + " new_end_date as db_new_end_date "
        + " from crd_card" + " where card_no =? ";
    sqlSelect(sql1, new Object[] {wp.colStr("new_card_no")});
    wp.colSet("db_new_beg_date", sqlStr("db_new_beg_date"));
    wp.colSet("db_new_end_date", sqlStr("db_new_end_date"));

  }

  void selectOldCard() {
    if (empty(wp.colStr("old_card_no")))
      return;
    String sql2 = " select new_beg_date as db_old_beg_date ," + " new_end_date as db_old_end_date "
        + " from crd_card" + " where card_no =? ";
    sqlSelect(sql2, new Object[] {wp.colStr("old_card_no")});
    wp.colSet("db_old_beg_date", sqlStr("db_old_beg_date"));
    wp.colSet("db_old_end_date", sqlStr("db_old_end_date"));
  }

  void selectExpireReason() {
    if (empty(wp.colStr("expire_chg_flag")) || empty(wp.colStr("expire_reason"))) {
      return;
    }
    String sql1 = " select wf_desc as tt_expire_reason " + " from ptr_sys_idtab"
        + " where wf_type = decode(?,'1','NOTCHG_KIND_S','2','NOTCHG_KIND_O','3','NOTCHG_KIND_M')"
        + "and wf_id = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("expire_chg_flag"), wp.colStr("expire_reason")});
    wp.colSet("tt_expire_reason", sqlStr("tt_expire_reason"));

  }

  void selectMove() {
    String sql1 = " select nomove_rsn as db_nomove_rsn," + " move_status as db_move_status"
        + " from crd_card_ext" + " where card_no = ?";
    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});
    wp.colSet("db_nomove_rsn", sqlStr("db_nomove_rsn"));
    wp.colSet("db_move_status", sqlStr("db_move_status"));
  }

  void selectAddr() {
    String sql1 = " select mail_zip , mail_addr1 , mail_addr2 , mail_addr3, "
        + " mail_addr4 , mail_addr5 , remark_mail " + " from crd_emboss a "
        + " where a.card_no = ? " + " order by a.mod_time desc " + " fetch first 1 row only ";
    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});
    wp.colSet("db_mail_zip", sqlStr("mail_zip"));
    wp.colSet("db_mail_addr1", sqlStr("mail_addr1"));
    wp.colSet("db_mail_addr2", sqlStr("mail_addr2"));
    wp.colSet("db_mail_addr3", sqlStr("mail_addr3"));
    wp.colSet("db_mail_addr4", sqlStr("mail_addr4"));
    wp.colSet("db_mail_addr5", sqlStr("mail_addr5"));
    wp.colSet("db_remark_mail", sqlStr("remark_mail"));
    if (empty(sqlStr("mail_addr1")) && (wp.colEq("mail_type", "0") || wp.colEq("mail_type", "1")
        || wp.colEq("mail_type", "2"))) {
      String sql2 = " select bill_sending_zip , bill_sending_addr1 , bill_sending_addr2 "
          + " , bill_sending_addr3 ,bill_sending_addr4 ,bill_sending_addr5 " + " from act_acno "
          + " where acno_p_seqno = ? ";
      sqlSelect(sql2, new Object[] {wp.colStr("acno_p_seqno")});

      wp.colSet("db_mail_zip", sqlStr("bill_sending_zip"));
      wp.colSet("db_mail_addr1", sqlStr("bill_sending_addr1"));
      wp.colSet("db_mail_addr2", sqlStr("bill_sending_addr2"));
      wp.colSet("db_mail_addr3", sqlStr("bill_sending_addr3"));
      wp.colSet("db_mail_addr4", sqlStr("bill_sending_addr4"));
      wp.colSet("db_mail_addr5", sqlStr("bill_sending_addr5"));
    }

    if (wp.colEq("mail_type", "3") || wp.colEq("mail_type", "4")) {
      wp.colSet("db_mail_zip", "");
      wp.colSet("db_mail_addr1", "");
      wp.colSet("db_mail_addr2", "");
      wp.colSet("db_mail_addr3", "");
      wp.colSet("db_mail_addr4", "");
      wp.colSet("db_mail_addr5", "");
    }

  }

  void ttDataCard() {
    // --重製原因
    if (wp.colEq("reissue_reason", "1")) {
      wp.colSet("tt_reissue_reason", ".掛失重製");
    } else if (wp.colEq("reissue_reason", "2")) {
      wp.colSet("tt_reissue_reason", ".毀損重製");
    } else if (wp.colEq("reissue_reason", "3")) {
      wp.colSet("tt_reissue_reason", ".偽卡重製");
    } else if (wp.colEq("reissue_reason", "4")) {
      wp.colSet("tt_reissue_reason", ".星座卡重製");
    }
    // --重製狀態
    if (wp.colEq("reissue_status", "1")) {
      wp.colSet("tt_reissue_status", ".已登錄待製卡");
    } else if (wp.colEq("reissue_status", "2")) {
      wp.colSet("tt_reissue_status", ".製卡中");
    } else if (wp.colEq("reissue_status", "3")) {
      wp.colSet("tt_reissue_status", ".製卡完成");
    } else if (wp.colEq("reissue_status", "4")) {
      wp.colSet("tt_reissue_status", ".製卡失敗");
    }
    // --續卡狀態
    if (wp.colEq("change_status", "1")) {
      wp.colSet("tt_change_status", ".續卡待製卡");
    } else if (wp.colEq("change_status", "2")) {
      wp.colSet("tt_change_status", ".製卡中");
    } else if (wp.colEq("change_status", "3")) {
      wp.colSet("tt_change_status", ".製卡完成");
    } else if (wp.colEq("change_status", "4")) {
      wp.colSet("tt_change_status", ".製卡失敗");
    }
    // --續卡註記
    if (wp.colEq("change_reason", "1")) {
      wp.colSet("tt_change_reason", ".系統續卡");
    } else if (wp.colEq("change_reason", "2")) {
      wp.colSet("tt_change_reason", ".提前續卡 - 客戶來電");
    } else if (wp.colEq("change_reason", "3")) {
      wp.colSet("tt_change_reason", ".提前續卡 - 本行調整");
    }
    // --不續卡註記
    if (wp.colEq("expire_chg_flag", "1")) {
      wp.colSet("tt_expire_chg_flag", ".系統不續卡");
    } else if (wp.colEq("expire_chg_flag", "2")) {
      wp.colSet("tt_expire_chg_flag", ".預約不續卡");
    } else if (wp.colEq("expire_chg_flag", "3")) {
      wp.colSet("tt_expire_chg_flag", ".人工不續卡");
    }
    // --卡片昇級狀態
    if (wp.colEq("upgrade_status", "1")) {
      wp.colSet("tt_upgrade_status", ".已登錄待製卡");
    } else if (wp.colEq("upgrade_status", "2")) {
      wp.colSet("tt_upgrade_status", ".製卡中");
    } else if (wp.colEq("upgrade_status", "3")) {
      wp.colSet("tt_upgrade_status", ".製卡完成");
    } else if (wp.colEq("upgrade_status", "4")) {
      wp.colSet("tt_upgrade_status", ".製卡失敗");
    }
    // --申請電子錢包
    if (wp.colEq("set_code", "A")) {
      wp.colSet("tt_set_code", ".新增");
    } else if (wp.colEq("set_code", "U")) {
      wp.colSet("tt_set_code", ".修改");
    } else if (wp.colEq("set_code", "R")) {
      wp.colSet("tt_set_code", ".重置");
    } else if (wp.colEq("set_code", "D")) {
      wp.colSet("tt_set_code", ".刪除");
    }
    // --寄件別
    if (wp.colEq("mail_type", "1")) {
      wp.colSet("tt_mail_type", ".普掛");
    } else if (wp.colEq("mail_type", "2")) {
      wp.colSet("tt_mail_type", ".限掛");
    } else if (wp.colEq("mail_type", "3")) {
      wp.colSet("tt_mail_type", ".卡務中心親領");
    } else if (wp.colEq("mail_type", "4")) {
      wp.colSet("tt_mail_type", ".分行");
    } else if (wp.colEq("mail_type", "5")) {
      wp.colSet("tt_mail_type", ".暫不寄");
    } else if (wp.colEq("mail_type", "6")) {
      wp.colSet("tt_mail_type", ".快捷");
    } else if (wp.colEq("mail_type", "7")) {
      wp.colSet("tt_mail_type", ".航空");
    } else if (wp.colEq("mail_type", "8")) {
      wp.colSet("tt_mail_type", ".快遞");
    } else if (wp.colEq("mail_type", "9")) {
      wp.colSet("tt_mail_type", ".其他");
    } else if (wp.colEq("mail_type", "N")) {
      wp.colSet("tt_mail_type", ".退件");
    } else if (wp.colEq("mail_type", "Q")) {
      wp.colSet("tt_mail_type", ".其他");
    }
    
    // --與正卡關係
    if ( ! wp.colEmpty("major_relation")) {
		wp.colSet("tt_major_relation", "." + ecsfunc.DeCodeCms.majorRelationCode(wp.colStr("major_relation")));
	}  

    // --使用狀態
    if (wp.colEq("current_code", "0")) {
      wp.colSet("tt_current_code", ".正常");
    } else if (wp.colEq("current_code", "1")) {
      wp.colSet("tt_current_code", ".申停");
    } else if (wp.colEq("current_code", "2")) {
      wp.colSet("tt_current_code", ".掛失");
    } else if (wp.colEq("current_code", "3")) {
      wp.colSet("tt_current_code", ".強停");
    } else if (wp.colEq("current_code", "4")) {
      wp.colSet("tt_current_code", ".其他停用");
    } else if (wp.colEq("current_code", "5")) {
      wp.colSet("tt_current_code", ".偽卡");
    }
    // --QR CODE
    if (wp.colEq("qr_code", "A")) {
      wp.colSet("tt_qr_code", ".正常");
    } else if (wp.colEq("qr_code", "U")) {
      wp.colSet("tt_qr_code", ".申停");
    } else if (wp.colEq("qr_code", "D")) {
      wp.colSet("tt_qr_code", ".掛失");
    }

    String sql1 = " select full_chi_name from gen_brn where branch = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("reg_bank_no")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_reg_bank_no", "." + sqlStr("full_chi_name"));
    }
    sqlSelect(sql1, new Object[] {wp.colStr("mail_branch")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_mail_branch", "." + sqlStr("full_chi_name"));
    }
    
	if (wp.colEq("sex", "1")) {
		wp.colSet("tt_sex", ".男");
	} else if (wp.colEq("sex", "2")) {
		wp.colSet("tt_sex", ".女");
	}

  }

  // ------------relcard
  public void queryFuncRelcard() throws Exception {
    String lsWhere = "";

    String lsIdno = wp.itemStr("ex_idno");
    if (lsIdno.length() == 10) {
      lsWhere += sqlCol(wp.itemStr("ex_idno"), "id_no");
    } else if (lsIdno.length() >= 7 && lsIdno.length() <= 9) {
      lsWhere += sqlCol(wp.itemStr("ex_idno"), "id_no", "like%");
    } else if (lsIdno.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere += sqlCol(wp.itemStr("ex_chi_name"), "chi_name");
      }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere += " and id_p_seqno in (select id_p_seqno from crd_card "
        		       + " where 1=1 "+ sqlCol(wp.itemStr("ex_card_no"), "card_no") + " ) ";    
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    wp.setQueryMode();
    wp.sqlCmd = " select " + " '信用卡' as debit_flag ," + " id_no ," + " chi_name ," + " birthday ,"
        + " sex ," + " uf_idno_ename(id_p_seqno) as db_ename," + " id_p_seqno , "
        + " decode(sex,'1','.男','2','.女') as tt_sex " + " FROM crd_idno" + " where 1=1 " + lsWhere;
    this.pageQuery();
    wp.setListCount(4);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
  }

  public void querySelectRelcard() throws Exception {
    dataKK1 = wp.itemStr("data_k1");
    dataReadRelcard();
  }

  public void dataReadRelcard() throws Exception {
    selectCrdIdnoByIdPSeqno();
    relcard1();
    relcard2();
    relcard3();
    relcard4();
    relcard5();
    relcard6();
    wp.notFound = "N";
  }

  void relcard1() throws Exception {
    daoTid = "A.";
    wp.sqlCmd += " select" + " card_no ," + " id_p_seqno ," + " card_type ," + " group_code ,"
        + " sup_flag ," + " major_card_no ," + " acct_type ," + " p_seqno, acno_p_seqno, "
        + " corp_act_flag," + " uf_idno_name(id_p_seqno) as chi_name ,"
        + " uf_idno_id(id_p_seqno) as id_no" 
        + " FROM crd_card " 
        + " where 1=1 "
        + sqlCol(wp.itemStr("data_k2"), "major_id_p_seqno") + " and current_code ='0' " 
        + " union "
        + " select" + " card_no ," + " id_p_seqno ," + " card_type ," + " group_code ,"
        + " sup_flag ," + " major_card_no ," + " acct_type ," + " p_seqno, p_seqno as acno_p_seqno,"
        + " corp_act_flag," + " uf_idno_name(id_p_seqno) as chi_name ,"
        + " uf_idno_id(id_p_seqno) as id_no " 
        + " FROM crd_card " + " where 1=1 "
        + " and id_p_seqno <> major_id_p_seqno " 
        + " and   current_code ='0' "
        + sqlCol(wp.itemStr("data_k2"), "id_p_seqno") 
        + " order by major_card_no Asc, sup_flag Asc";
    pageQuery();
    wp.setListCount(1);
  }

  void relcard2() throws Exception {
    daoTid = "B.";
    wp.sqlCmd += " select" + " card_no ," + " id_p_seqno ," + " oppost_date ," + " oppost_reason ,"
        + " sup_flag ," + " major_card_no ," + " uf_idno_name(id_p_seqno) as db_idno_name, "
        + " uf_opp_status(oppost_reason) as tt_oppost_reason " 
        + " FROM crd_card "
        + " where 1=1 " 
        + sqlCol(wp.itemStr("data_k2"), "major_id_p_seqno")
        + " and current_code ='1'" + " union " + " select" + " card_no ," + " id_p_seqno ,"
        + " oppost_date ," + " oppost_reason ," + " sup_flag ," + " major_card_no ,"
        + " uf_idno_name(id_p_seqno) as db_idno_name , "
        + " uf_opp_status(oppost_reason) as tt_oppost_reason " 
        + " FROM crd_card "
        + " where 1=1 " + " and id_p_seqno <> major_id_p_seqno " 
        + " and   current_code ='1' "
        + sqlCol(wp.itemStr("data_k2"), "id_p_seqno") 
        + " order by oppost_date ";
    pageQuery();
    wp.setListCount(2);
  }

  void relcard3() throws Exception {
    daoTid = "C.";
    wp.sqlCmd += " select" + " card_no ," + " id_p_seqno ," + " sup_flag ," + " oppost_date ,"
        + " oppost_reason ," + " new_card_no ," + " major_card_no ,"
        + " uf_idno_name(id_p_seqno) as db_idno_name ,"
        + " uf_opp_status(oppost_reason) as tt_oppost_reason " 
        + " FROM crd_card "
        + " where 1=1 " 
        + sqlCol(wp.itemStr("data_k2"), "major_id_p_seqno")
        + " and current_code ='2'" + " union " + " select" + " card_no ," + " id_p_seqno ,"
        + " sup_flag ," + " oppost_date ," + " oppost_reason ," + " new_card_no ,"
        + " major_card_no ," + " uf_idno_name(id_p_seqno) as db_idno_name ,"
        + " uf_opp_status(oppost_reason) as tt_oppost_reason " 
        + " FROM crd_card "
        + " where 1=1 " 
        + " and id_p_seqno <> major_id_p_seqno " 
        + " and   current_code ='2' "
        + sqlCol(wp.itemStr("data_k2"), "id_p_seqno");

    pageQuery();
    wp.setListCount(3);
  }

  void relcard4() throws Exception {
    daoTid = "D.";
    wp.sqlCmd += " select" + " card_no ," + " id_p_seqno ," + " sup_flag ," + " new_card_no ,"
        + " oppost_date ," + " oppost_reason ," + " major_card_no ,"
        + " uf_idno_name(id_p_seqno) as db_idno_name ,"
        + " uf_opp_status(oppost_reason) as tt_oppost_reason " 
        + " FROM crd_card "
        + " where 1=1 " 
        + sqlCol(wp.itemStr("data_k2"), "major_id_p_seqno")
        + " and current_code ='5' " + " union " + " select" + " card_no ," + " id_p_seqno ,"
        + " sup_flag ," + " new_card_no ," + " oppost_date ," + " oppost_reason ,"
        + " major_card_no ," + " uf_idno_name(id_p_seqno) as db_idno_name ,"
        + " uf_opp_status(oppost_reason) as tt_oppost_reason " 
        + " from  crd_card "
        + " where 1=1" + " and id_p_seqno <> major_id_p_seqno " + " and   current_code ='5'"
        + sqlCol(wp.itemStr("data_k2"), "id_p_seqno") + " order by card_no Asc ";
    this.pageQuery();
    wp.setListCount(4);
  }

  void relcard5() throws Exception {
    daoTid = "E.";
    wp.sqlCmd += " select" + " card_no ," + " id_p_seqno ," + " sup_flag ," + " oppost_date ,"
        + " oppost_reason ," + " major_card_no ," + " current_code ,"
        + " uf_idno_name(id_p_seqno) as db_idno_name,"
        + " uf_opp_status(oppost_reason) as tt_oppost_reason" 
        + " FROM crd_card "
        + " where 1=1 " 
        + sqlCol(wp.itemStr("data_k2"), "major_id_p_seqno")
        + " and current_code ='3' " + " union " + " select" + " card_no ," + " id_p_seqno ,"
        + " sup_flag ," + " oppost_date ," + " oppost_reason ," + " major_card_no ,"
        + " current_code ," + " uf_idno_name(id_p_seqno) as db_idno_name,"
        + "uf_opp_status(oppost_reason) as tt_oppost_reason" 
        + " from  crd_card "
        + " where 1=1 " 
        + " and id_p_seqno <> major_id_p_seqno " 
        + " and   current_code ='3'"
        + sqlCol(wp.itemStr("data_k2"), "id_p_seqno") 
        + " order by card_no Asc";
    this.pageQuery();
    wp.setListCount(5);
  }
  
  void relcard6() throws Exception {
	    daoTid = "F.";
	    wp.sqlCmd += " select" + " card_no ," + " id_p_seqno ," + " sup_flag ," + " oppost_date ,"
	        + " oppost_reason ," + " major_card_no ," + " current_code ,"
	        + " uf_idno_name(id_p_seqno) as db_idno_name,"
	        + " uf_opp_status(oppost_reason) as tt_oppost_reason" 
	        + " FROM crd_card "
	        + " where 1=1 " 
	        + sqlCol(wp.itemStr("data_k2"), "major_id_p_seqno")
	        + " and current_code ='4' " + " union " + " select" + " card_no ," + " id_p_seqno ,"
	        + " sup_flag ," + " oppost_date ," + " oppost_reason ," + " major_card_no ,"
	        + " current_code ," + " uf_idno_name(id_p_seqno) as db_idno_name,"
	        + "uf_opp_status(oppost_reason) as tt_oppost_reason" 
	        + " from  crd_card "
	        + " where 1=1 " 
	        + " and id_p_seqno <> major_id_p_seqno " 
	        + " and   current_code ='4'"
	        + sqlCol(wp.itemStr("data_k2"), "id_p_seqno") 
	        + " order by card_no Asc";
	    this.pageQuery();
	    wp.setListCount(6);
	  }

  // -----------relidno
  public void queryFuncRelidno() throws Exception {
    String lsWhere = "";

    String lsIdno = wp.itemStr("ex_idno");
    if (lsIdno.length() == 10) {
      lsWhere += sqlCol(wp.itemStr("ex_idno"), "id_no");
    } else if (lsIdno.length() >= 7 && lsIdno.length() <= 9) {
      lsWhere += sqlCol(wp.itemStr("ex_idno"), "id_no", "like%");
    } else if (lsIdno.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere += sqlCol(wp.itemStr("ex_chi_name"), "chi_name");
    }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere += " and id_p_seqno in "
        		+ " (select id_p_seqno from crd_card where 1=1 " + sqlCol(wp.itemStr("ex_card_no"), "card_no") + ") "; 
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    wp.setQueryMode();
    wp.sqlCmd = " select " + " '信用卡' as debit_flag ," + " id_no ," + " chi_name ," + " birthday ,"
        + " sex ," + " uf_idno_ename(id_p_seqno) as db_ename," + " id_p_seqno , "
        + " decode(sex,'1','.男','2','.女') as tt_sex " + " FROM crd_idno" + " where 1=1 " + lsWhere;
    pageQuery();
    wp.setListCount(5);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
  }

  public void querySelectRelidno() throws Exception {
    dataKK1 = wp.itemStr("data_k2");
    dataReadRelidno();
  }

  void dataReadRelidno() throws Exception {
    selectCrdIdnoByIdPSeqno();

    relidno1();
    relidno2();
    relidno3();
    wp.notFound = "N";
  }

  void relidno1() throws Exception {
    daoTid = "A.";
    wp.sqlCmd += " select" + " rela_id ," + " rela_name ,"
        + " nvl((select 'Y' from crd_idno B where B.id_no=rela_id),'N') as db_chcode"
        + " FROM crd_rela " + " where 1=1 " + " and rela_type = '2'" + " and id_p_seqno = ? ";
    setString(wp.itemStr("data_k2"));
    pageQuery();
    wp.setListCount(1);
  }

  void relidno2() throws Exception {
    daoTid = "B.";
    wp.sqlCmd += " select" + " rela_id ," + " rela_name ,"
        + " nvl((select 'Y' from crd_idno B where B.id_no=rela_id),'N') as db_chcode "
        + " FROM crd_rela " + " where 1=1 " + " and rela_type = '1'" + " and id_p_seqno = ? ";
    setString(wp.itemStr("data_k2"));
    pageQuery();
    wp.setListCount(2);
  }

  void relidno3() throws Exception {
    daoTid = "C.";
    wp.sqlCmd += " select" + " id_p_seqno ," + " uf_idno_id(id_p_seqno) as wk_idno ," + " card_no ,"
        + " uf_idno_name(id_p_seqno) as db_cname " + " FROM crd_rela " + " where 1=1 "
        + " and rela_type = '1'"
        + " and rela_id in (SELECT id_no FROM crd_idno WHERE id_p_seqno = ? )";
    setString(wp.itemStr("data_k2"));
    pageQuery();
    wp.setListCount(3);
  }

  // -----RELBank
  public void queryFuncRelbank() throws Exception {
    String lsWhere = "";

    String lsIdno = wp.itemStr("ex_idno");
    if (lsIdno.length() == 10) {
      lsWhere +=
          " and id_p_seqno in (select id_p_seqno from crd_idno where id_no = ? )";
      setString(lsIdno);
    } else if (lsIdno.length() >= 7 && lsIdno.length() <= 9) {
      lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where id_no like ? )";
      setString(lsIdno+"%");
    } else if (lsIdno.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where chi_name = ? )";
        setString(wp.itemStr("ex_chi_name"));
      }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere += " and acno_p_seqno in (select acno_p_seqno from crd_card where card_no = ? ) ";
        setString(wp.itemStr("ex_card_no"));
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    wp.setQueryMode();
    String sql1 = "select p_seqno, acno_p_seqno, " + " acct_type," + " acct_key," + " corp_p_seqno,"
        + " id_p_seqno," + " card_indicator," + " line_of_credit_amt," + " '' as db_cname,"
        + " '1' db_sort," + " substr(acct_key,1,10) as db_acct_key, " + " debt_close_date " + " from act_acno"
        + " where 1=1 " + lsWhere + " order by acct_type, db_sort, acct_key";
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      alertErr2("找不到資料");
      return;
    }
    int ss = sqlRowNum;
    // --
    int rr = 0;
    int tlAmt = 0;
    for (int ll = 0; ll < sqlRowNum; ll++) {
      rr++;
      // wp.col_set(rr,"ser_num",""+(rr+1));
      wp.colSet(rr, "wk_acct_key", sqlStr(ll, "acct_type") + "-" + sqlStr(ll, "acct_key"));
      wp.colSet(rr, "card_indicator", sqlStr(ll, "card_indicator"));
      wp.colSet(rr, "db_cname", "信用額度");
      wp.colSet(rr, "line_of_credit_amt", sqlStr(ll, "line_of_credit_amt"));
      tlAmt += sqlNum(ll, "line_of_credit_amt");
      if (this.eqIgno(sqlStr(ll, "card_indicator"), "1")) {
        wp.colSet(rr, "tt_card_indicator", ".一般卡");
      } else if (this.eqIgno(sqlStr(ll, "card_indicator"), "2")) {
        wp.colSet(rr, "tt_card_indicator", ".商務卡");
      } 
    }

    // --利害關係人
    String sql2 = "select non_asset_balance as non_asset_bal," + " bk_flag as ls_bk_flag ,"
        + " fh_flag as ls_fh_flag " + " from crd_correlate" + " where 1=1 "
        + " and correlate_id = ?";
    sqlSelect(sql2, new Object[] {sqlStr("db_acct_key")});
    if (empty(sqlStr("non_asset_bal"))) {
      this.sqlSet(0, "non_asset_bal", "0");
      log("nab:" + sqlStr("non_asset_bal"));
    }
    // rr++;
    // wp.col_set(rr,"ser_num",""+rr);
    if (eqIgno(sqlStr("ls_bk_flag"), "Y") || eqIgno(sqlStr("ls_fh_flag"), "Y")) {
      wp.colSet(0, "db_cname", "利害關係人: Y");
    } else
      wp.colSet(0, "db_cname", "利害關係人: N");
    // --擔保品
    String sql3 = "select asset_value as db_asset_value" + " from crd_idno " + " where 1=1 "
        + " and id_p_seqno = ?";
    sqlSelect(sql3, new Object[] {sqlStr("id_p_seqno")});

    // --最高額度 ---最高分期付款額度 ------------
    double value9 = 0;
    double value10 = 0;
    String sql4 = "select wf_value6 as value9 ," + " wf_value7 as value10" + " from ptr_sys_parm "
        + " where wf_parm='SYSPARM'" + " and wf_key ='CORRELATE' ";
    sqlSelect(sql4);
    value9 = sqlInt("value9");
    value10 = sqlNum("value10");
    if (sqlRowNum <= 0) {
      value9 = 1000000;
      value10 = 100000;
    }

    wp.colSet("high_amt", "" + value9);

    // --
    rr++;
    // wp.col_set(rr,"ser_num",""+rr);
    wp.colSet(rr, "db_cname", "最高分期付款額度");
    if (empty(sqlStr("value10")))
      wp.colSet(rr, "line_of_credit_amt", "" + value10);
    wp.colSet(rr, "line_of_credit_amt", sqlStr("value10"));
    tlAmt += sqlNum("value10");
    rr++;
    // wp.col_set(rr,"ser_num",""+rr);
    wp.colSet(rr, "db_cname", "無擔保授信餘額");
    log("BBB:" + sqlStr("non_asset_bal"));
    wp.colSet(rr, "line_of_credit_amt", sqlStr("non_asset_bal"));
    tlAmt += sqlNum("non_asset_bal");
    rr++;
    // wp.col_set(rr,"ser_num",""+rr);
    wp.colSet(rr, "db_cname", "擔保品放款值");
    int dav = (int) (sqlNum("db_asset_value") * -1);
    tlAmt += dav;
    wp.colSet(rr, "line_of_credit_amt", "" + dav);
    log("ccc:" + dav);
    wp.colSet("tl_amt", "" + tlAmt);
    wp.selectCnt = (rr + 1);
    wp.setListCount(6);
  }

  // -------xx_card
  //111-11-02   V1.00.34  Ryan        新增EPAY主檔相關資訊 
  public void queryFuncXXcard() throws Exception {
	    StringBuffer whereBfTscc = new StringBuffer();
		StringBuffer whereBfIpass = new StringBuffer();
		StringBuffer whereBfIch = new StringBuffer();
		StringBuffer whereBfMob = new StringBuffer();
		StringBuffer whereBfTsccVD = new StringBuffer();
		StringBuffer whereBfTpan = new StringBuffer();
		StringBuffer whereBfOEMpay = new StringBuffer();
		StringBuffer whereBfEpay = new StringBuffer();
		  
//	    String whereBfTscc = "", whereBfIpass = "", whereBfIch = "", whereBfMob = "", whereBfTsccVD = "",
//	        whereBfTpan = "", whereBfOEMpay = "";
	    
	    if (wp.itemEmpty("ex_xx_card_no") == false) {
	      whereBfTscc.append(" and tsc_card_no = :ex_xx_card_no ");
	      whereBfIpass.append(" and ips_card_no = :ex_xx_card_no ");
	      whereBfIch.append(" and ich_card_no = :ex_xx_card_no ");
	      whereBfTpan.append(" and v_card_no = :ex_xx_card_no ");
	      whereBfMob.append(" and 1=2 ");
	      whereBfTsccVD.append(" and tsc_card_no = :ex_xx_card_no ");
	      whereBfOEMpay.append(" and v_card_no = :ex_xx_card_no ");
	      whereBfEpay.append(" and A.v_card_no = :ex_xx_card_no ");
	      setString("ex_xx_card_no", wp.itemStr("ex_xx_card_no"));
	    }

	    if (wp.itemEmpty("ex_idno") == false) {
	      if (wp.itemStr("ex_idno").length() != 10) {
	        alertErr2("身分證ID:輸入不完整!");
	        return;
	      }
	      whereBfTscc.append(" and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	                              .append(" and B.id_no = :ex_idno ) ");
	      whereBfIpass.append(" and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	      						   .append(" and B.id_no = :ex_idno ) ");
	      whereBfIch.append(" and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
                                .append(" and B.id_no = :ex_idno ) ");
	      whereBfMob.append(" and A.card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	                               .append(" and B.id_no = :ex_idno ) ");
	      whereBfTpan.append(" and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	                               .append(" and B.id_no = :ex_idno ) ") ;
	      whereBfTsccVD.append(" and vd_card_no in (select A.card_no from dbc_card A join dbc_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	    		                       .append(" and B.id_no = :ex_idno ) ") ;
	      whereBfOEMpay.append(" and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	    		                          .append(" and B.id_no = :ex_idno ) ") ;
	      whereBfEpay.append(" and A.card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
          								.append(" and B.id_no = :ex_idno ) ") ;
	      
	      setString("ex_idno",wp.itemStr2("ex_idno"));
	    }

	    if (wp.itemEmpty("ex_chi_name") == false) {
	      whereBfTscc
	      .append(" and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	      .append(" and B.chi_name = :ex_chi_name ) ");
	      whereBfIpass
	      .append(" and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	      .append(" and B.chi_name = :ex_chi_name ) ");
	      whereBfIch
	      .append(" and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	      .append(" and B.chi_name = :ex_chi_name ) ");
	      whereBfMob
	      .append(" and A.card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	      .append(" and B.chi_name = :ex_chi_name ) ");
	      whereBfTpan
	      .append(" and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	      .append(" and B.chi_name = :ex_chi_name ) ");
	      whereBfTsccVD
	      .append(" and vd_card_no in (select A.card_no from dbc_card A join dbc_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	      .append(" and B.chi_name = :ex_chi_name ) ");
	      whereBfOEMpay
	      .append(" and card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	      .append(" and B.chi_name = :ex_chi_name ) ");
	      setString("ex_chi_name", wp.itemStr2("ex_chi_name"));
	      whereBfEpay
	      .append(" and A.card_no in (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 ")
	      .append(" and B.chi_name = :ex_chi_name ) ");
	      setString("ex_chi_name", wp.itemStr2("ex_chi_name"));
	    }

	    if (wp.itemEmpty("ex_card_no") == false) {
	      whereBfTscc.append(" and card_no = :ex_card_no ");
	      whereBfIpass.append(" and card_no = :ex_card_no ");
	      whereBfIch.append(" and card_no = :ex_card_no ");
	      whereBfMob.append(" and A.card_no = :ex_card_no ");
	      whereBfTpan.append(" and card_no = :ex_card_no ");
	      whereBfTsccVD.append(" and vd_card_no = :ex_card_no ");
	      whereBfOEMpay.append(" and card_no = :ex_card_no ");
	      whereBfEpay.append(" and A.card_no = :ex_card_no ");
	      setString("ex_card_no", wp.itemStr2("ex_card_no"));
	    }

	    if (wp.itemEmpty("ex_xx_card_no") && wp.itemEmpty("ex_idno") && wp.itemEmpty("ex_chi_name")
	        && wp.itemEmpty("ex_card_no")) {
	      alertErr2("請輸入查詢條件");
	      return;
	    }

	       StringBuffer sqlCmdBf = new StringBuffer();
	       sqlCmdBf.append(" select '")
	      .append(TSC_CARD_NAME)
	      .append("' as xx_card_type ,  current_code as xx_current_code , ")
	      .append(" tsc_card_no as xx_card_no ,  card_no ,  new_beg_date as xx_start_date , ")
	      .append(" new_end_date as xx_end_date ,  autoload_flag as xx_autoload_flag , ")
	      .append(" '' as xx_autoload_clo_date ,  return_flag as xx_return_flag , ")
	      .append(" lock_flag as xx_lock_flag ,  blacklt_flag as xx_blacklt_flag , ")
	      .append(" '' as xx_refuse_type ,  '' as xx_msisdn ,  '' as xx_stop_flag , ")
	      .append(" '' as xx_block_flag ,  '' as xx_crt_date ,  '' as xx_change_date, ")
	      .append("  '' as wallet_id ")
	      .append(" from tsc_card " )
	      .append(" where 1=1 " + whereBfTscc )
	      .append(" union ")
	      .append(" select '")
	      .append(IPS_CARD_NAME)
	      .append("' as xx_card_type ,  current_code as xx_current_code , ")
	      .append(" ips_card_no as xx_card_no ,  card_no ,  new_beg_date as xx_start_date , ")
	      .append(" new_end_date as xx_end_date ,  autoload_flag as xx_autoload_flag , ")
	      .append(" autoload_clo_date as xx_autoload_clo_date ,  return_flag as xx_return_flag , ")
	      .append(" lock_flag as xx_lock_flag ,  blacklt_flag as xx_blacklt_flag , ")
	      .append(" '' as xx_refuse_type ,  '' as xx_msisdn ,  '' as xx_stop_flag , ")
	      .append(" '' as xx_block_flag ,  '' as xx_crt_date ,  '' as xx_change_date, ")
	      .append("  '' as wallet_id ")
	      .append(" from ips_card ")
	      .append(" where 1=1 ").append(whereBfIpass)
	      .append(" union ")
	      .append(" select '" )
	      .append( ICH_CARD_NAME )
	      .append("' as xx_card_type ,  current_code as xx_current_code , ").append( " ich_card_no as xx_card_no , " )
	      .append( " card_no ,  new_beg_date as xx_start_date , ")
	      .append( " new_end_date as xx_end_date ,  autoload_flag as xx_autoload_flag , ")
	      .append( " '' as xx_autoload_clo_date ,  return_flag as xx_return_flag , ")
	      .append( " lock_flag as xx_lock_flag ,  blacklt_flag as xx_blacklt_flag , ")
	      .append( " refuse_type as xx_refuse_type ,  '' as xx_msisdn ,  '' as xx_stop_flag , ")
	      .append( " '' as xx_block_flag ,  '' as xx_crt_date ,  '' as xx_change_date, ")
	      .append("  '' as wallet_id ")
	      .append( " from ich_card " )
	      .append( " where 1=1 " ).append( whereBfIch )
	      .append( " union ")
	      .append( " select '" )
	      .append( MOB_CARD_NAME )
	      .append("' as xx_card_type ,  B.current_code as xx_current_code , ")
	      .append( " A.sir_no as xx_card_no ,  A.card_no ,  A.new_beg_date as xx_start_date , ")
	      .append( " A.new_end_date as xx_end_date ,  '' as xx_autoload_flag , ")
	      .append( " '' as xx_autoload_clo_date ,  '' as xx_return_flag ,  '' as xx_lock_flag , ")
	      .append( " '' as xx_blacklt_flag ,  '' as xx_refuse_type ,  A.msisdn as xx_msisdn , ")
	      .append( " A.stop_flag as xx_stop_flag ,  A.block_flag as xx_block_flag , ")
	      .append( " A.crt_date as xx_crt_date ,  '' as xx_change_date, " )
	      .append("  '' as wallet_id ")
	      .append( " from crd_card B join mob_card A on A.card_no = B.card_no " )
	      .append( " where 1=1 ").append( whereBfMob )
	      .append( " union " )
	      .append( " select '" )
	      .append( HCE_CARD_NAME  )
	      .append( "'as xx_card_type , ")
	      .append( " status_code as xx_current_code ,  v_card_no as xx_card_no ,  card_no , ")
	      .append( " '' as xx_start_date ,  new_end_date as xx_end_date , ")
	      .append( " '' as xx_autoload_flag ,  '' as xx_autoload_clo_date , ")
	      .append( " '' as xx_return_flag ,  '' as xx_lock_flag ,  '' as xx_blacklt_flag , ")
	      .append( " '' as xx_refuse_type ,  '' as xx_msisdn ,  '' as xx_stop_flag , ")
	      .append( " '' as xx_block_flag ,  crt_date as xx_crt_date , ")
	      .append( " change_date as xx_change_date, " )
	      .append("  '' as wallet_id ")
	      .append( " from hce_card ")
	      .append( " where 1=1 " ).append( whereBfTpan )
	      .append( " union ")
	      .append( " select '" )
	      .append( TSC_VD_CARD_NAME )
	      .append( "' as xx_card_type ,  current_code as xx_current_code , ")
	      .append( " tsc_card_no as xx_card_no ,  vd_card_no as card_no,  new_beg_date as xx_start_date , ")
	      .append( " new_end_date as xx_end_date ,  autoload_flag as xx_autoload_flag , ")
	      .append( " '' as xx_autoload_clo_date ,  return_flag as xx_return_flag , ")
	      .append( " lock_flag as xx_lock_flag ,  blacklt_flag as xx_blacklt_flag , ")
	      .append( " '' as xx_refuse_type ,  '' as xx_msisdn ,  '' as xx_stop_flag , ")
	      .append( " '' as xx_block_flag ,  '' as xx_crt_date ,  '' as xx_change_date, ")
	      .append("  '' as wallet_id ")
	      .append( " from tsc_vd_card ")
	      .append( " where 1=1 " ).append( whereBfTsccVD )
	      .append( " union " )
	      .append( " select '" )
	      .append( OEMPAY_CARD_NAME  )
	      .append( "'as xx_card_type , ")
	      .append( " status_code as xx_current_code ,  v_card_no as xx_card_no ,  card_no , ")
	      .append( " '' as xx_start_date ,  new_end_date as xx_end_date , ")
	      .append( " '' as xx_autoload_flag ,  '' as xx_autoload_clo_date , ")
	      .append( " '' as xx_return_flag ,  '' as xx_lock_flag ,  '' as xx_blacklt_flag , ")
	      .append( " '' as xx_refuse_type ,  '' as xx_msisdn ,  '' as xx_stop_flag , ")
	      .append( " '' as xx_block_flag ,  crt_date as xx_crt_date , ")
	      .append( " change_date as xx_change_date , " )
	      .append("  wallet_id as wallet_id ")
	      .append( " from OEMPAY_CARD ")
	      .append( " where 1=1 " ).append( whereBfOEMpay)
	      .append( " union " )
	      .append( " select '" )
	      .append( EPAY_CARD_NAME  )
	      .append( "'as xx_card_type , ")
	      .append( " B.current_code as xx_current_code ,  A.v_card_no as xx_card_no ,  A.card_no , ")
	      .append( " B.new_beg_date as xx_start_date ,  B.new_end_date as xx_end_date , ")
	      .append( " '' as xx_autoload_flag ,  '' as xx_autoload_clo_date , ")
	      .append( " '' as xx_return_flag ,  '' as xx_lock_flag ,  '' as xx_blacklt_flag , ")
	      .append( " '' as xx_refuse_type ,  '' as xx_msisdn ,  '' as xx_stop_flag , ")
	      .append( " '' as xx_block_flag ,  A.crt_date as xx_crt_date , ")
	      .append( " '' as xx_change_date , " )
	      .append("  '' as wallet_id ")
	      .append( " from EPAY_CARD A join CRD_CARD B on A.card_no = B.card_no ")
	      .append( " where 1=1 " ).append( whereBfEpay)
	      .append( " order by 1 ,2 ");
	    
	    wp.sqlCmd = sqlCmdBf.toString();

	    pageQuery();

	    if (sqlNotFind()) {
	      alertErr2("此條件查無資料");
	      return;
	    }

	    wp.setListCount(7);
	    selectIdPSeqnoXXcard();
  }

  void selectIdPSeqnoXXcard() {
	  DeCodeCms decodeCms =  new DeCodeCms(wp);

    for (int ii = 0; ii < wp.selectCnt; ii++) {
    	String xxCardType= wp.colStr(ii, "xx_card_type");
    	wp.colSet(ii, "xx_display_card_type", wp.colStr(ii, "xx_card_type"));
      
      sqlRowNum = selectIdPSeqnoAndChgDate(wp.colStr(ii, "card_no"), xxCardType);
      
      if (sqlRowNum > 0) {
				wp.colSet(ii, "id_p_seqno", sqlStr("id_p_seqno"));
				
				if (TSC_CARD_NAME.equalsIgnoreCase(xxCardType) || TSC_VD_CARD_NAME.equalsIgnoreCase(xxCardType)) {
					if ( !empty(sqlStr("change_date")) && wp.colEq(ii, "xx_current_code", "6")) {
						
						String tscDatabase = ( TSC_CARD_NAME.equalsIgnoreCase(xxCardType) ) ? TSC_CARD : TSC_VD_CARD;
						
						sqlRowNum = selectNewTscCardNo(tscDatabase, wp.colStr(ii, "xx_card_no"));
						if (sqlRowNum > 0) {
							
							sqlRowNum = selectTscEmbossRsn(tscDatabase, sqlStr("new_tsc_card_no"));
							if (sqlRowNum > 0) {
								if (eqIgno(sqlStr("ls_tsc_emboss_rsn"), "3") || eqIgno(sqlStr("ls_tsc_emboss_rsn"), "4")) {
									wp.colSet(ii, "xx_current_code", "7");
								}
							}	
						}	
					}		
				}	
      }
      
		switch (xxCardType) {
		case HCE_CARD_NAME:
		case OEMPAY_CARD_NAME:
			wp.colSet(ii, "tt_current_code", DeCodeCms.tpanStatusCode(wp.colStr(ii, "xx_current_code")));
			break;
		case MOB_CARD_NAME:
			wp.colSet(ii, "tt_current_code", DeCodeCrd.currentCode(wp.colStr(ii, "xx_current_code")));
			break;
		case EPAY_CARD_NAME:
			wp.colSet(ii, "tt_current_code", DeCodeCrd.currentCode(wp.colStr(ii, "xx_current_code")));
			break;
		default:
			wp.colSet(ii, "tt_current_code", DeCodeCrd.electronicCurrentCode(wp.colStr(ii, "xx_current_code") ));
			break;
		}
      
      if (OEMPAY_CARD_NAME.equalsIgnoreCase(xxCardType)) {
		wp.colSet(ii, "xx_card_type", "OEMPAY");
		wp.colSet(ii, "xx_display_card_type", decodeCms.getWalletIdDesc(wp.colStr( ii,"wallet_id")));
	  } 
      
    }
    wp.notFound = "N";
  }

	private int selectTscEmbossRsn(String tscDatabase, String tscCardNo) {

	    String sql3 = " select tsc_emboss_rsn as ls_tsc_emboss_rsn " 
	    		              + " from " + tscDatabase
	    		              + " where 1=1 "
	                          + " and tsc_card_no = ?";
		
		sqlSelect(sql3, new Object[] {  tscCardNo });
		
		return sqlRowNum;
	}


	private int selectNewTscCardNo(String tscDatabase, String tscCardNo) {
	    String sql2 = " select new_tsc_card_no "
	                          + " from " + tscDatabase
	                          + " where tsc_card_no = ? ";
		
		sqlSelect(sql2, new Object[] { tscCardNo });
		return sqlRowNum;
	}



	private int selectIdPSeqnoAndChgDate(String cardNo, String cardType) {
		String cardTable = "";
    	if (TSC_VD_CARD_NAME.equalsIgnoreCase(cardType)) {
    		cardTable = DBC_CARD;
		}else {
			cardTable = CRD_CARD;
		}
    	
		StringBuffer sqlSb = new StringBuffer();
		sqlSb.append(" select id_p_seqno , change_date  ")
		.append(" from ").append(cardTable)
		.append(" where 1=1 ")
		.append(" and card_no = ?");
	    sqlSelect(sqlSb.toString(), new Object[] { cardNo});
	    return sqlRowNum;
    }

	private void querySelectTsccVDCard() throws Exception {
		dataKK2 = wp.itemStr("data_k2");
	    dataKK4 = wp.itemStr("data_k4");
	    dataReadTsccVDCard();

	}

  private void dataReadTsccVDCard() {
	    selectIdnoByIdPSeqnoXXcard(DBC_IDNO);
	    wp.selectSQL = " *  ";
	    wp.daoTable = "tsc_vd_card";
	    wp.whereStr = " where 1=1 " + sqlCol(dataKK2, "tsc_card_no");
	    pageSelect();
	    if (sqlNotFind()) {
	      alertErr("查無資料, key=" + dataKK1);
	      return;
	    }
	    wp.colSet("card_no", wp.colStr("vd_card_no"));
	    wp.colSet("isTscVDCard", "Y");
	    dataReadTsccVDCardAfter();
		
	}



	private void dataReadTsccVDCardAfter() {
	    selectChageDateFromTscVDCard();
	    selectTscc();
	    selectMissTsccAmt();
	    dataReadWkdataTscc();

	}



private void selectChageDateFromTscVDCard() {
      String sql1 = "select id_p_seqno , p_seqno , change_date " 
            + " from dbc_card "
            + " where 1=1 " + " and card_no = ?";
        sqlSelect(sql1, new Object[] {wp.colStr("vd_card_no")});

        if (!empty(sqlStr("change_date")) && wp.colEq("current_code", "6")) {

          if (!empty(wp.colStr("new_tsc_card_no"))) {
            String sql2 = "select tsc_emboss_rsn as ls_tsc_emboss_rsn " 
                + " from tsc_vd_card "
                + " where 1=1  and tsc_card_no = ?";
            sqlSelect(sql2, new Object[] {wp.colStr("new_tsc_card_no")});
            if (this.eqIgno(sqlStr("ls_tsc_emboss_rsn"), "3")
                || this.eqIgno(sqlStr("ls_tsc_emboss_rsn"), "4")) {
              wp.colSet("current_code", "7");
            }
          }
        }
		
	}



public void querySelectTscc() throws Exception {
    dataKK2 = wp.itemStr("data_k2");
    dataKK4 = wp.itemStr("data_k4");
    dataReadTscc();
  }

  void dataReadTscc() throws Exception {
    selectIdnoByIdPSeqnoXXcard(CRD_IDNO);
    wp.selectSQL = " *  ";
    wp.daoTable = "tsc_card";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK2, "tsc_card_no");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + dataKK1);
      return;
    }
    dataReadTsccAfter();
  }

  void dataReadTsccAfter() {
    selectChageDateFromTscCard();
    selectTscc();
    selectMissTsccAmt();
    dataReadWkdataTscc();
  }

  void selectChageDateFromTscCard() {
    String sql1 = "select id_p_seqno , acno_p_seqno, p_seqno , change_date " + " from crd_card "
        + " where 1=1 " + " and card_no = ?";
    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});

    if (!empty(sqlStr("change_date")) && wp.colEq("current_code", "6")) {

      if (!empty(wp.colStr("new_tsc_card_no"))) {
        String sql2 = "select tsc_emboss_rsn as ls_tsc_emboss_rsn " + " from tsc_card "
            + " where 1=1 " + " and tsc_card_no = ?";
        sqlSelect(sql2, new Object[] {wp.colStr("new_tsc_card_no")});
        if (this.eqIgno(sqlStr("ls_tsc_emboss_rsn"), "3")
            || this.eqIgno(sqlStr("ls_tsc_emboss_rsn"), "4")) {
          wp.colSet("current_code", "7");
        }
      }
    }
  }

  void selectTscc() {
    String sql1 = "select crt_date as db_cre_date_1," + " crt_time as db_cre_time_1, "
        + " resp_code as db_resp_code_1, " + " proc_date as db_resp_date_1, "
        + " proc_time as db_resp_time_1 " 
        + " from cca_outgoing " 
        + " where 1=1 "
        + " and electronic_card_no = ? " + " and act_code = '1' " 
        + " and key_value = 'TSCC' "
        + " and key_table = 'OPPOSITION' "
        + " order by crt_date,crt_time "
        + " fetch first 1 row only ";
    log("AAAA:" + wp.colStr("tsc_card_no"));
    sqlSelect(sql1, new Object[] {wp.colStr("tsc_card_no")});
    wp.colSet("db_cre_date_1", sqlStr("db_cre_date_1"));
    wp.colSet("db_cre_time_1", sqlStr("db_cre_time_1"));
    wp.colSet("db_resp_date_1", sqlStr("db_resp_date_1"));
    wp.colSet("db_resp_time_1", sqlStr("db_resp_time_1"));
    wp.colSet("db_resp_code_1", sqlStr("db_resp_code_1"));

    String sql2 = "select crt_date as db_cre_date_2," + " crt_time as db_cre_time_2, "
        + " resp_code as db_resp_code_2, " + " proc_date as db_resp_date_2, "
        + " proc_time as db_resp_time_2 " 
        + " from cca_outgoing " 
        + " where 1=1 "
        + " and electronic_card_no = ? " + " and act_code = '3' " 
        + " and key_value = 'TSCC' "
        + " and key_table = 'OPPOSITION' "
        + " order by crt_date,crt_time "
        + " fetch first 1 row only ";
    sqlSelect(sql2, new Object[] {wp.colStr("tsc_card_no")});

    wp.colSet("db_cre_date_2", sqlStr("db_cre_date_2"));
    wp.colSet("db_cre_time_2", sqlStr("db_cre_time_2"));
    wp.colSet("db_resp_date_2", sqlStr("db_resp_date_2"));
    wp.colSet("db_resp_time_2", sqlStr("db_resp_time_2"));
    wp.colSet("db_resp_code_2", sqlStr("db_resp_code_2"));
  }

  void selectMissTsccAmt() {
    String sql1 = "select TRAN_AMT_6H as db_amt_1," + " TRAN_AMT_0H as db_amt_2, "
        + " crt_date as db_crt_date_3 " 
    	+ " from tsc_btrd_log " 
        + " where 1=1 "
        + " and tsc_card_no = ? " + " order by crt_date,crt_time " + " fetch first 1 row only ";
    sqlSelect(sql1, new Object[] {wp.colStr("tsc_card_no")});

    wp.colSet("db_amt_1", sqlStr("db_amt_1"));
    wp.colSet("db_amt_2", sqlStr("db_amt_2"));
    wp.colSet("db_crt_date_3", sqlStr("db_crt_date_3"));
  }

  void dataReadWkdataTscc() {
    if (wp.colEq("db_resp_code_1", "00")) {
      wp.colSet("db_resp_code_1", "00.成功");
    } else if (wp.colEq("db_resp_code_1", "C0")) {
      wp.colSet("db_resp_code_1", "C0.卡號不存在");
    } else if (wp.colEq("db_resp_code_1", "57")) {
      wp.colSet("db_resp_code_1", "57.卡片非有效卡");
    } else if (wp.colEq("db_resp_code_1", "19")) {
      wp.colSet("db_resp_code_1", "19.重複掛卡");
    } else if (wp.colEq("db_resp_code_1", "01")) {
      wp.colSet("db_resp_code_1", "01.其他");
    } else if (wp.colEq("db_resp_code_1", "02")) {
      wp.colSet("db_resp_code_1", "02.其他");
    }

    if (wp.colEq("db_resp_code_2", "00")) {
      wp.colSet("db_resp_code_2", "00.成功");
    } else if (wp.colEq("db_resp_code_2", "76")) {
      wp.colSet("db_resp_code_2", "76.無法找到原始掛卡交易");
    } else if (wp.colEq("db_resp_code_2", "93")) {
      wp.colSet("db_resp_code_2", "93.超過取消掛卡時限");
    } else if (wp.colEq("db_resp_code_2", "01")) {
      wp.colSet("db_resp_code_2", "01.其他");
    }

  }
  
  //IPASS
  void selectIpass(){
		String sql1 = "select crt_date as db_cre_date_1," 
				+ " crt_time as db_cre_time_1, "
				+ " resp_code as db_resp_code_1, " 
				+ " proc_date as db_resp_date_1, " 
				+ " proc_time as db_resp_time_1 "
				+ " from cca_outgoing " 
				+ " where 1=1 " 
				+ " and electronic_card_no = ? " 
				+ " and act_code = '1' "
				+ " and key_value = 'IPASS' " 
				+ " and key_table = 'OPPOSITION' "
				+ " order by crt_date,crt_time " + " fetch first 1 row only ";
		log("AAAA:" + wp.colStr("tsc_card_no"));
		sqlSelect(sql1, new Object[] { wp.colStr("ips_card_no") });
		wp.colSet("db_cre_date_1", sqlStr("db_cre_date_1"));
		wp.colSet("db_cre_time_1", sqlStr("db_cre_time_1"));
		wp.colSet("db_resp_date_1", sqlStr("db_resp_date_1"));
		wp.colSet("db_resp_time_1", sqlStr("db_resp_time_1"));
		wp.colSet("db_resp_code_1", sqlStr("db_resp_code_1"));
		
	    String sql2 = "select crt_date as db_cre_date_2," 
	    		+ " crt_time as db_cre_time_2, "
	            + " resp_code as db_resp_code_2, " 
	    		+ " proc_date as db_resp_date_2, "
	            + " proc_time as db_resp_time_2 " 
	            + " from cca_outgoing " 
	            + " where 1=1 "
	            + " and electronic_card_no = ? " 
	            + " and act_code = '3' " 
	            + " and key_value = 'IPASS' "
	            + " and key_table = 'OPPOSITION' "
	            + " order by crt_date,crt_time "
	            + " fetch first 1 row only ";
	        sqlSelect(sql2, new Object[] {wp.colStr("ips_card_no")});

	        wp.colSet("db_cre_date_2", sqlStr("db_cre_date_2"));
	        wp.colSet("db_cre_time_2", sqlStr("db_cre_time_2"));
	        wp.colSet("db_resp_date_2", sqlStr("db_resp_date_2"));
	        wp.colSet("db_resp_time_2", sqlStr("db_resp_time_2"));
	        wp.colSet("db_resp_code_2", sqlStr("db_resp_code_2"));
  }
  
  //ICASH
  void selectIcash(){
		String sql1 = "select crt_date as db_cre_date_1," 
				+ " crt_time as db_cre_time_1, "
				+ " resp_code as db_resp_code_1, " 
				+ " proc_date as db_resp_date_1, " 
				+ " proc_time as db_resp_time_1 "
				+ " from cca_outgoing " 
				+ " where 1=1 " 
				+ " and electronic_card_no = ? " 
				+ " and act_code = '1' "
				+ " and key_value = 'ICASH' " 
				+ " and key_table = 'OPPOSITION' "
				+ " order by crt_date,crt_time " + " fetch first 1 row only ";
		log("AAAA:" + wp.colStr("tsc_card_no"));
		sqlSelect(sql1, new Object[] { wp.colStr("ich_card_no") });
		wp.colSet("db_cre_date_1", sqlStr("db_cre_date_1"));
		wp.colSet("db_cre_time_1", sqlStr("db_cre_time_1"));
		wp.colSet("db_resp_date_1", sqlStr("db_resp_date_1"));
		wp.colSet("db_resp_time_1", sqlStr("db_resp_time_1"));
		wp.colSet("db_resp_code_1", sqlStr("db_resp_code_1"));
		
	    String sql2 = "select crt_date as db_cre_date_2," 
	    		+ " crt_time as db_cre_time_2, "
	            + " resp_code as db_resp_code_2, " 
	    		+ " proc_date as db_resp_date_2, "
	            + " proc_time as db_resp_time_2 " 
	            + " from cca_outgoing " 
	            + " where 1=1 "
	            + " and electronic_card_no = ? " 
	            + " and act_code = '3' " 
	            + " and key_value = 'ICASH' "
	            + " and key_table = 'OPPOSITION' "
	            + " order by crt_date,crt_time "
	            + " fetch first 1 row only ";
	        sqlSelect(sql2, new Object[] {wp.colStr("ich_card_no")});

	        wp.colSet("db_cre_date_2", sqlStr("db_cre_date_2"));
	        wp.colSet("db_cre_time_2", sqlStr("db_cre_time_2"));
	        wp.colSet("db_resp_date_2", sqlStr("db_resp_date_2"));
	        wp.colSet("db_resp_time_2", sqlStr("db_resp_time_2"));
	        wp.colSet("db_resp_code_2", sqlStr("db_resp_code_2"));
  }
  

  // --HCE
  public void queryFuncHCE() throws Exception {
    String lsWhere = "";

    String lsIdno = wp.itemStr("ex_idno");
    if (lsIdno.length() >= 7 && lsIdno.length() <= 10) {
      lsWhere += " and b.id_p_seqno in (select id_p_seqno from crd_idno where id_no like ? )";
      setString(lsIdno + "%");
    } else if (lsIdno.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere += " and b.id_p_seqno in (select c.id_p_seqno from crd_idno c where c.chi_name= ? )";
        setString( wp.itemStr("ex_chi_name") );
      }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere += " and b.card_no = ? ";
        setString( wp.itemStr("ex_card_no") );
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    wp.setQueryMode();
    wp.sqlCmd = " select " + " hex(a.rowid) as rowid ," + " a.card_no , " + " a.sir_no , "
        + " a.msisdn , " + " a.service_type , " + " a.mob_status , " + " a.crt_date , "
        + " a.new_beg_date , " + " a.new_end_date , " + " a.stop_proc_date , " + " a.block_flag , "
        + " a.spec_flag , " + " a.p_seqno , acno_p_seqno," + " a.stop_flag , "
        + " b.current_code , " + " b.card_type , " + " b.group_code , "
        + " uf_tt_group_code(b.group_code) as group_name" + " FROM crd_card b, mob_card a "
        + " where b.card_no = a.card_no " + lsWhere;

    this.pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    listWkdataHce();
    wp.setListCount(8);
  }

  void listWkdataHce() {
    String sql1 = "select id_p_seqno  " + " from crd_card " + " where 1=1 " + " and card_no = ?";
    for (int ii = 0; ii < sqlRowNum; ii++) {
      if (wp.colEq(ii, "service_type", "01")) {
        wp.colSet(ii, "tt_service_type", ".UICC");
      } else if (wp.colEq(ii, "service_type", "02")) {
        wp.colSet(ii, "tt_service_type", ".Micro SD卡");
      } else if (wp.colEq(ii, "service_type", "03")) {
        wp.colSet(ii, "tt_service_type", ".嵌入式");
      } else if (wp.colEq(ii, "service_type", "04")) {
        wp.colSet(ii, "tt_service_type", ".外接式");
      }
      if (wp.colEq(ii, "mob_status", "01")) {
        wp.colSet(ii, "tt_mob_status", ".製卡資料待客戶下載中");
      } else if (wp.colEq(ii, "mob_status", "02")) {
        wp.colSet(ii, "tt_mob_status", ".客戶下載製卡資料成功");
      } else if (wp.colEq(ii, "mob_status", "03")) {
        wp.colSet(ii, "tt_mob_status", ".客戶逾期未下載");
      } else if (wp.colEq(ii, "mob_status", "20")) {
        wp.colSet(ii, "tt_mob_status", ".卡片毀損重製");
      }
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_no")});
      wp.colSet(ii, "id_p_seqno", sqlStr("id_p_seqno"));
    }
  }

  public void querySelectHce() throws Exception {
    // kk1=card_no kk2=id_p_seqno kk3=sir_no
    dataKK3 = wp.itemStr("data_k3");
    dataKK2 = wp.itemStr("data_k2");
    dataReadHce();
  }

  void dataReadHce() throws Exception {
    selectCrdIdnoByCardNoXXcard();
    wp.selectSQL = " A.* ," + " 'XX' as sir_status,"
        + " to_char(A.app_card_time,'yyyymmddhh24miss') as wk_app_card_date , "
        + " lpad(' ',20) as db_batch_seqno";
    wp.daoTable = "mob_card A";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK3, "A.card_no") + sqlCol(dataKK2, "A.sir_no");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + dataKK1);
      return;
    }
    dataReadWkdataHce();
  }

  void dataReadWkdataHce() {
    if (wp.colEq("service_type", "01")) {
      wp.colSet("service_type", "01.UICC");
    } else if (wp.colEq("service_type", "02")) {
      wp.colSet("service_type", "02.Micro SD卡");
    } else if (wp.colEq("service_type", "03")) {
      wp.colSet("service_type", "03.嵌入式");
    } else if (wp.colEq("service_type", "04")) {
      wp.colSet("service_type", "04.外接式");
    }

    if (wp.colEq("mob_status", "01")) {
      wp.colSet("mob_status", "01.待客戶下載中");
    } else if (wp.colEq("mob_status", "02")) {
      wp.colSet("mob_status", "02.客戶已下載");
    } else if (wp.colEq("mob_status", "03")) {
      wp.colSet("mob_status", "03.客戶逾期未下載");
    } else if (wp.colEq("mob_status", "20")) {
      wp.colSet("mob_status", "20.卡片毀損重製");
    }

    if (wp.colEq("sir_status", "XX")) {
      wp.colSet("sir_status", "與TWMP無法連線");
    } else if (wp.colEq("sir_status", "1")) {
      wp.colSet("sir_status", "1.未佈署");
    } else if (wp.colEq("sir_status", "11")) {
      wp.colSet("sir_status", "11.完成安裝");
    } else if (wp.colEq("sir_status", "12")) {
      wp.colSet("sir_status", "12.完成個人化");
    } else if (wp.colEq("sir_status", "13")) {
      wp.colSet("sir_status", "13.不完全安裝");
    } else if (wp.colEq("sir_status", "21")) {
      wp.colSet("sir_status", "21.啟用(卡片已下載)");
    } else if (wp.colEq("sir_status", "22")) {
      wp.colSet("sir_status", "22.暫停");
    } else if (wp.colEq("sir_status", "23")) {
      wp.colSet("sir_status", "23.環境狀態有問題");
    }

    if (wp.colEq("mno_event_cd", "1")) {
      wp.colSet("mno_event_cd", "1.V1_電話暫停");
    } else if (wp.colEq("mno_event_cd", "2")) {
      wp.colSet("mno_event_cd", "2.V2_電話被電信業者永停");
    } else if (wp.colEq("mno_event_cd", "3")) {
      wp.colSet("mno_event_cd", "3.V3_電話終止");
    } else if (wp.colEq("mno_event_cd", "4")) {
      wp.colSet("mno_event_cd", "4.V4_更換合約人");
    } else if (wp.colEq("mno_event_cd", "12")) {
      wp.colSet("mno_event_cd", "12.V5_NFC服務終止");
    } else if (wp.colEq("mno_event_cd", "122")) {
      wp.colSet("mno_event_cd", "122.V6_安全元件重發");
    } else if (wp.colEq("mno_event_cd", "31")) {
      wp.colSet("mno_event_cd", "31.V7_手機失竊");
    } else if (wp.colEq("mno_event_cd", "32")) {
      wp.colSet("mno_event_cd", "32.V8_手機丟失");
    } else if (wp.colEq("mno_event_cd", "33")) {
      wp.colSet("mno_event_cd", "33.V9_手機丟失或失竊(通知者不能區分失竊或遺失)");
    } else if (wp.colEq("mno_event_cd", "33")) {
      wp.colSet("mno_event_cd", "33.V9_手機丟失或失竊(通知者不能區分失竊或遺失)");
    } else if (wp.colEq("mno_event_cd", "41")) {
      wp.colSet("mno_event_cd", "41.VA_SE手機失竊)");
    } else if (wp.colEq("mno_event_cd", "42")) {
      wp.colSet("mno_event_cd", "42.VB_SE手機遺失)");
    } else if (wp.colEq("mno_event_cd", "43")) {
      wp.colSet("mno_event_cd", "43.VC_SE手機失竊或遺失(如果通知者不能區分失竊或遺失))");
    }

    if (wp.colEq("app_card_flag", "4")) {
      wp.colSet("app_card_flag", "4.服務暫停");
    } else if (wp.colEq("app_card_flag", "5")) {
      wp.colSet("app_card_flag", "5.服務恢復");
    } else if (wp.colEq("app_card_flag", "6")) {
      wp.colSet("app_card_flag", "6.服務終止");
    }

    if (wp.colEq("service_op_code", "A2")) {
      wp.colSet("service_op_code", "A2.行動用戶終止");
    } else if (wp.colEq("service_op_code", "A3")) {
      wp.colSet("service_op_code", "A3.安全元件掛失");
    } else if (wp.colEq("service_op_code", "A4")) {
      wp.colSet("service_op_code", "A4.安全元件找回");
    }

    if (wp.colEq("card_op_code", "1")) {
      wp.colSet("card_op_code", "1.服務佈署");
    } else if (wp.colEq("card_op_code", "4")) {
      wp.colSet("card_op_code", "4.服務暫停");
    } else if (wp.colEq("card_op_code", "5")) {
      wp.colSet("card_op_code", "5.服務恢復");
    } else if (wp.colEq("card_op_code", "102")) {
      wp.colSet("card_op_code", "102.服務更新");
    } else if (wp.colEq("card_op_code", "6")) {
      wp.colSet("card_op_code", "6.服務終止");
    }
    String lsCardDate = "", lsCardTime = "";
    if (!wp.colEmpty("wk_app_card_date")) {
      lsCardDate = commDate.dspDate(commString.mid(wp.colStr("wk_app_card_date"), 0, 8));
      wp.colSet("app_card_date", lsCardDate);
      if (!empty(commString.mid(wp.colStr("wk_app_card_date"), 8, 6))) {
        lsCardTime = commString.mid(wp.colStr("wk_app_card_date"), 8, 6);
      }
      wp.colSet("app_card_time", lsCardTime);
    }

  }

  // ---TPAN-----------------------------------------------------------
  public void queryFuncTPAN() throws Exception {
    String lsWhere = "";

    String lsIdno = wp.itemStr("ex_idno");
    if (lsIdno.length() >= 7 && lsIdno.length() <= 10) {
      lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where id_no like ? )";
      setString(lsIdno + "%");
    } else if (lsIdno.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno  where chi_name= ? )";
        setString(wp.itemStr("ex_chi_name"));
    }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere += " and card_no = ? ";
        setString(wp.itemStr("ex_card_no"));
      }

    if (!empty(wp.itemStr("ex_vcard_no"))) {
        lsWhere += " and v_card_no = ? ";
        setString(wp.itemStr("ex_vcard_no"));
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    wp.setQueryMode();
    wp.sqlCmd = " select " + " v_card_no ," + " card_no , " + " status_code , " + " change_date , "
        + " crt_date , " + " new_end_date , " + " sir_status , " + " sms_flag , "
        + " sms_ver_code , " + " active_date , " + " uf_idno_id(id_p_seqno) as id_no,"
        + " id_p_seqno " + " FROM hce_card " + " where 1=1 " + lsWhere + " order by status_code";

    this.pageQuery();
    listWkdataTpan();
    wp.setListCount(9);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
  }

  void listWkdataTpan() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "status_code", "0")) {
        wp.colSet(ii, "tt_status_code", ".正常");
      } else if (wp.colEq(ii, "status_code", "1")) {
        wp.colSet(ii, "tt_status_code", ".暫停");
      } else if (wp.colEq(ii, "status_code", "2")) {
        wp.colSet(ii, "tt_status_code", ".終止(人工)");
      } else if (wp.colEq(ii, "status_code", "3")) {
        wp.colSet(ii, "tt_status_code", ".終止(已停用)");
      } else if (wp.colEq(ii, "status_code", "4")) {
        wp.colSet(ii, "tt_status_code", ".重複取消");
      } else if (wp.colEq(ii, "status_code", "5")) {
        wp.colSet(ii, "tt_status_code", ".終止(已過效期)");
      } 
    }
  }

  public void querySelectTpan() throws Exception {
    // kk1=v_card_no kk2=id_p_seqno
    dataKK2 = wp.itemStr("data_k2");
    dataKK4 = wp.itemStr("data_k4");
    dataReadTpan();
  }

  void dataReadTpan() throws Exception {
    selectIdnoByIdPSeqnoXXcard(CRD_IDNO);
    wp.selectSQL = " A.* , uf_idno_id(A.id_p_seqno) as id_no ";
    wp.daoTable = "hce_card A";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK2, "A.v_card_no");
    logSql();
    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料, key=" + dataKK1);
      return;
    }
    wkdataTpan();

  }

  void wkdataTpan() {
    if (wp.colEq("status_code", "0")) {
      wp.colSet("tt_status_code", "正常");
    } else if (wp.colEq("status_code", "1")) {
      wp.colSet("tt_status_code", "暫停");
    } else if (wp.colEq("status_code", "2")) {
      wp.colSet("tt_status_code", "終止(人工)");
    } else if (wp.colEq("status_code", "3")) {
      wp.colSet("tt_status_code", "終止(已停用)");
    } else if (wp.colEq("status_code", "4")) {
      wp.colSet("tt_status_code", "重複取消");
    } else if (wp.colEq("status_code", "5")) {
      wp.colSet("tt_status_code", "終止(已過效期)");
    } 
  }
  
	public void querySelectOEMPay() throws Exception {
		// kk1=v_card_no kk2=id_p_seqno
		dataKK2 = wp.itemStr("data_k2");
		dataKK4 = wp.itemStr("data_k4");
		dataReadOEMPay();
	}
	
	void dataReadOEMPay() throws Exception {
		selectIdnoByIdPSeqnoXXcard(CRD_IDNO);
		wp.selectSQL = " A.* , uf_idno_id(A.id_p_seqno) as id_no ";
		wp.daoTable = "OEMPAY_CARD A";
		wp.whereStr = " where 1=1 " + sqlCol(dataKK2, "A.v_card_no");
		logSql();
		pageSelect();

		if (sqlNotFind()) {
			alertErr("查無資料, key=" + dataKK1);
			return;
		}
		wkdataOEMPay();

	}
	
	void wkdataOEMPay() {
		DeCodeCms deCodeCms = new DeCodeCms(wp);
	
		wp.colSet("tt_status_code", DeCodeCms.tpanStatusCode(wp.colStr("status_code")));
		
		wp.colSet("tt_wallet_id", deCodeCms.getWalletIdDesc(wp.colStr("wallet_id")));
		
	}

  // --ipass
  public void queryFuncIPASS() throws Exception {
    String lsWhere = "";

    if (!empty(wp.itemStr("ex_ips_card_no"))) {
        lsWhere += " and ips_card_no = ? ";
        setString(wp.itemStr("ex_ips_card_no"));
      }

    String lsIdno = wp.itemStr("ex_idno");
    if (!empty(lsIdno)) {
      if (lsIdno.length() == 10) {
        lsWhere +=
            " and card_no in (select card_no from crd_card "
         + " where id_p_seqno in (select id_p_seqno from crd_idno where id_no = ? )) ";
        setString(lsIdno);
      } else if (lsIdno.length() > 0) {
        alertErr2("身分證號 輸入不完整");
        return;
      }
    }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere +=
            " and card_no in (select a.card_no from crd_card a, crd_idno b "
         + " where a.id_p_seqno = b.id_p_seqno and b.chi_name= ? )";
        setString(wp.itemStr("ex_chi_name"));
      }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere += " and card_no = ? ";
        setString(wp.itemStr("ex_card_no"));
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    wp.setQueryMode();
    wp.sqlCmd =
        " select " + " ips_card_no ," + " card_no , " + " current_code , " + " new_beg_date , "
            + " new_end_date , " + " autoload_flag , " + " autoload_clo_date , " + " return_flag , "
            + " lock_flag , " + " blacklt_flag " + " FROM ips_card " + " where 1=1 " + lsWhere;
    log("CMD:" + wp.sqlCmd);
    this.pageQuery();
    wp.setListCount(10);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
  }

  public void querySelectIpass() throws Exception {
    dataKK2 = wp.itemStr("data_k2");
    dataKK3 = wp.itemStr("data_k3");
    dataReadIpass();
    selectIpass(); //add 20201106 ryan
  }

  void dataReadIpass() throws Exception {
    selectCrdIdnoByCardNoXXcard();
    wp.selectSQL = " * ";
    wp.daoTable = "IPS_CARD ";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK2, "ips_card_no");
    this.logSql();
    pageSelect();
    dataReadWkdataIpass();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + dataKK1);
      return;
    }

  }
  
  //111-11-02   V1.00.34  Ryan        新增EPAY主檔相關資訊 
  public void querySelectEpay() throws Exception {
	 dataKK3 = wp.itemStr("data_k3");
	 dataKK4 = wp.itemStr("data_k4");
	 dataReadEpay();
  }
  
  //111-11-02   V1.00.34  Ryan        新增EPAY主檔相關資訊 
  void dataReadEpay() throws Exception {
	 selectIdnoByIdPSeqnoXXcard(CRD_IDNO);
	 wp.selectSQL = " A.* , uf_idno_id(A.id_p_seqno) as id_no ";
	 wp.daoTable = "epay_card A";
	 wp.whereStr = " where 1=1 " + sqlCol(dataKK3, "A.card_no") + sqlCol(dataKK4, "A.id_p_seqno");
	 logSql();
	 pageSelect();

	 if (sqlNotFind()) {
		alertErr("查無資料, key=" + dataKK1);
		return;
	 }
  }

  void dataReadWkdataIpass() {
    if (wp.colEq("autoload_from", "1")) {
      wp.colSet("autoload_from", "1.人工");
    } else if (wp.colEq("autoload_from", "2")) {
      wp.colSet("autoload_from", "2.批次");
    }

    if (wp.colEq("blacklt_from", "1")) {
      wp.colSet("blacklt_from", "1.停卡");
    } else if (wp.colEq("blacklt_from", "2")) {
      wp.colSet("blacklt_from", "2.人工");
    } else if (wp.colEq("blacklt_from", "3")) {
      wp.colSet("blacklt_from", "3.批次");
    }

  }

  // --rcv
  public void queryFuncRCV() throws Exception {
    String lsIdno = "", lsPSeqno = "";
    if (empty(wp.itemStr("ex_card_no")) && empty(wp.itemStr("ex_idno"))) {
      alertErr2("請輸入 卡號 or 身分證字號 ");
      return;
    }
    if (!empty(wp.itemStr("ex_card_no"))) {
      String sql1 = "select " + " uf_idno_id(id_p_seqno) as ls_idno , "
          + " acno_p_seqno as ls_pseqno " + " from crd_card" + " where card_no =?";
      sqlSelect(sql1, new Object[] {wp.itemStr("ex_card_no")});
      if (sqlRowNum <= 0) {
        alertErr2("查無此卡號");
        return;
      }
      lsIdno = sqlStr("ls_idno");
      lsPSeqno = sqlStr("ls_pseqno");          
    } else {
      if (!empty(wp.itemStr("ex_idno"))) {
        lsIdno = wp.itemStr("ex_idno");
        if (lsIdno.length() != 10) {
          alertErr2("身分證輸入錯誤");
          return;
        }
        String lsAk = lsIdno + "0";
        String sql2 = "select " + " acno_p_seqno as ls_pseqno " + " from act_acno"
            + " where acct_key =?" + commSqlStr.rownum(1);
        sqlSelect(sql2, new Object[] {lsAk});
        if (sqlRowNum <= 0) {
          alertErr2("查無持卡之帳戶帳號");
          return;
        }
      }
      lsPSeqno = sqlStr("ls_pseqno");
    }
    wp.setQueryMode();
    wfReadColLiac(lsIdno, lsPSeqno);//前協
    wfReadColCpbdue(lsIdno, lsPSeqno);//債協,個協,前調
    wfReadColLiadRenew(lsIdno, lsPSeqno);//更生
    wfReadColLiadLiquidate(lsIdno, lsPSeqno);//清算

//20230802 sunny 先保留
//    wfReadColLiab(lsIdno, lsPSeqno);//債協
//    wfReadColCpbdue2(lsIdno, lsPSeqno);//個協
//    wfReadColCpbdue3(lsIdno, lsPSeqno);//前調

    if (wp.selectCnt == 0) {
      alertErr2("查無資料");
      return;
    }
    listWkdateRcv();
    wp.setListCount(8);
  }

  void listWkdateRcv() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
//      if (ii > 9) {
//        wp.colSet(ii, "ser_num", (ii + 1));
//      } else {
        wp.colSet(ii, "ser_num",  String.format("%02d", (ii + 1)) );
//      }
      
      if (wp.colEq(ii, "wk_status", "LIAB-1")) {
        wp.colSet(ii, "tt_wk_status", "1.受理申請");
      } else if (wp.colEq(ii, "wk_status", "LIAB-2")) {
        wp.colSet(ii, "tt_wk_status", "2.停催通知");
      } else if (wp.colEq(ii, "wk_status", "LIAB-3")) {
        wp.colSet(ii, "tt_wk_status", "3.簽約成功");
      } else if (wp.colEq(ii, "wk_status", "LIAB-4")) {
          wp.colSet(ii, "tt_wk_status", "4.結案/復催");
      } else if (wp.colEq(ii, "wk_status", "LIAB-5")) {
            wp.colSet(ii, "tt_wk_status", "5.結案/毀諾");
      } else if (wp.colEq(ii, "wk_status", "LIAB-6")) {
          wp.colSet(ii, "tt_wk_status", "6.結案/結清");  
      } else if (wp.colEq(ii, "wk_status", "LIAC-1")) {
        wp.colSet(ii, "tt_wk_status", "1.受理申請");
      } else if (wp.colEq(ii, "wk_status", "LIAC-2")) {
        wp.colSet(ii, "tt_wk_status", "2.停催通知");
      } else if (wp.colEq(ii, "wk_status", "LIAC-3")) {
        wp.colSet(ii, "tt_wk_status", "3.簽約成功");
      } else if (wp.colEq(ii, "wk_status", "LIAC-4")) {
        wp.colSet(ii, "tt_wk_status", "4.結案/復催");
      } else if (wp.colEq(ii, "wk_status", "LIAC-5")) {
          wp.colSet(ii, "tt_wk_status", "5.結案/毀諾");
      } else if (wp.colEq(ii, "wk_status", "LIAC-6")) {
        wp.colSet(ii, "tt_wk_status", "6.結案/結清");
      } else if (wp.colEq(ii, "wk_status", "RENEW-1")) {
        wp.colSet(ii, "tt_wk_status", "1.更生開始");
      } else if (wp.colEq(ii, "wk_status", "RENEW-2")) {
        wp.colSet(ii, "tt_wk_status", "2.更生撤回");
      } else if (wp.colEq(ii, "wk_status", "RENEW-3")) {
        wp.colSet(ii, "tt_wk_status", "3.更生認可");
      } else if (wp.colEq(ii, "wk_status", "RENEW-4")) {
        wp.colSet(ii, "tt_wk_status", "4.更生履行完畢");
      } else if (wp.colEq(ii, "wk_status", "RENEW-5")) {
        wp.colSet(ii, "tt_wk_status", "5.更生裁定免責");
      } else if (wp.colEq(ii, "wk_status", "RENEW-6")) {
        wp.colSet(ii, "tt_wk_status", "6.更生調查程序");
      } else if (wp.colEq(ii, "wk_status", "RENEW-7")) {
        wp.colSet(ii, "tt_wk_status", "7.更生駁回");
      } else if (wp.colEq(ii, "wk_status", "LIAD-A")) {
        wp.colSet(ii, "tt_wk_status", "A.清算程序開始");
      } else if (wp.colEq(ii, "wk_status", "LIAD-B")) {
        wp.colSet(ii, "tt_wk_status", "B.清算程序終止(結)");
      } else if (wp.colEq(ii, "wk_status", "LIAD-C")) {
        wp.colSet(ii, "tt_wk_status", "C.清算程序開始同時終止");
      } else if (wp.colEq(ii, "wk_status", "LIAD-D")) {
        wp.colSet(ii, "tt_wk_status", "D.清算撤銷免責確定");
      } else if (wp.colEq(ii, "wk_status", "LIAD-E")) {
          wp.colSet(ii, "tt_wk_status", "E.清算調查程序");
      } else if (wp.colEq(ii, "wk_status", "LIAD-F")) {
    	  wp.colSet(ii, "tt_wk_status", "F.清算駁回");
      } else if (wp.colEq(ii, "wk_status", "LIAD-G")) {
    	  wp.colSet(ii, "tt_wk_status", "G.清算撤回");
      } else if (wp.colEq(ii, "wk_status", "LIAD-H")) {
    	  wp.colSet(ii, "tt_wk_status", "H.清算復權");
      }
    }
  }
  
  /*Cpbdue當前協商*/
  void wfReadColCpbdue(String lsIdno, String lsPseqno) {
	    String sql1 = "select " + " decode(cpbdue_type,'1','公會協商','2','個別協商','3','前置調解','') as cpbdue_type," + " cpbdue_curr_type as ls_status," + " decode(cpbdue_bank_type,'3',cpbdue_begin_date,cpbdue_upd_dte) as ls_rcv_date "
	        + " from col_cpbdue" + " where cpbdue_acct_type='01' "
	    	+ " and cpbdue_id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) ";
	       // + " order by decode(liab_status,'2','a','3','b','c') ";
	    
//	    log("Cpbdue3_aaa:" + lsIdno);
//	    log("Cpbdue3_bbb:" + lsPseqno);
	    
	    sqlSelect(sql1, new Object[] {lsIdno});

	    if (sqlRowNum <= 0 || empty(sqlStr("ls_status"))) {
	      return;
	    }

	    //wp.colSet(wp.selectCnt, "wk_type", "個別協商");
	    wp.colSet(wp.selectCnt, "wk_type", sqlStr("cpbdue_type"));
	    wp.colSet(wp.selectCnt, "wk_status", "LIAB-" + sqlStr("ls_status")); //定義與債協相同
	    wp.colSet(wp.selectCnt, "wk_rcv_date", sqlStr("ls_rcv_date"));
	    wp.colSet(wp.selectCnt, "wk_end_date", sqlStr("ls_end_date"));
	    wp.selectCnt++;
	  }
  
  /*債協--保留--現無使用*/
  void wfReadColLiab(String lsIdno, String lsPseqno) {
	  //cpbdue_type='1' and cpbdue_bank_type=’3’ and cpbdue_acct_type='01' and id_p_seqno = ?
	    String sql1 = "select " + " cpbdue_bank_type as ls_status," + " decode(cpbdue_bank_type,'3',cpbdue_begin_date,cpbdue_upd_dte) as ls_rcv_date "
	        + " from col_cpbdue" + " where cpbdue_type='1' and cpbdue_acct_type='01' "
	    	+ " and cpbdue_id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) ";
	       // + " order by decode(liab_status,'2','a','3','b','c') ";
	    
	    log("liab_aaa:" + lsIdno);
	    log("liab_bbb:" + lsPseqno);
	 
	    sqlSelect(sql1, new Object[] {lsIdno});
//	    if (sqlRowNum == 0 && !empty(lsPseqno)) {
//	      String sql2 = "select " + " liab_status as ls_status," + " liab_end_date as ls_end_date "
//	          + " from act_acno" + " where acno_p_seqno =?";
//	      sqlSelect(sql2, new Object[] {lsPseqno});
//	      if (sqlRowNum <= 0) {
//	        return;
//	      }
//	    }

	    if (empty(sqlStr("ls_status"))) {
	      return;
	    }

	    wp.colSet(wp.selectCnt, "wk_type", "公會協商");
	    wp.colSet(wp.selectCnt, "wk_status", "LIAB-" + sqlStr("ls_status"));
	    wp.colSet(wp.selectCnt, "wk_rcv_date", sqlStr("ls_rcv_date"));
	    wp.colSet(wp.selectCnt, "wk_end_date", sqlStr("ls_end_date"));
	    wp.selectCnt++;
	  }

  
  /*個協--保留--現無使用*/
  void wfReadColCpbdue2(String lsIdno, String lsPseqno) {
	    String sql1 = "select " + " cpbdue_bank_type as ls_status," + " decode(cpbdue_bank_type,'3',cpbdue_begin_date,cpbdue_upd_dte) as ls_rcv_date "
	        + " from col_cpbdue" + " where cpbdue_type='2' and cpbdue_acct_type='01' "
	    	+ " and cpbdue_id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) ";
	       // + " order by decode(liab_status,'2','a','3','b','c') ";
	    
//	    log("Cpbdue3_aaa:" + lsIdno);
//	    log("Cpbdue3_bbb:" + lsPseqno);
	    
	    sqlSelect(sql1, new Object[] {lsIdno});
	    
	    if (empty(sqlStr("ls_status"))) {
	      return;
	    }

	    wp.colSet(wp.selectCnt, "wk_type", "個別協商");
	    wp.colSet(wp.selectCnt, "wk_status", "LIAB-" + sqlStr("ls_status")); //定義與債協相同
	    wp.colSet(wp.selectCnt, "wk_rcv_date", sqlStr("ls_rcv_date"));
	    wp.colSet(wp.selectCnt, "wk_end_date", sqlStr("ls_end_date"));
	    wp.selectCnt++;
	  }
  
  /*前置調解--保留--現無使用*/
  void wfReadColCpbdue3(String lsIdno, String lsPseqno) {
	    String sql1 = "select " + " cpbdue_bank_type as ls_status," + " decode(cpbdue_bank_type,'3',cpbdue_begin_date,cpbdue_upd_dte) as ls_rcv_date "
	        + " from col_cpbdue" + " where cpbdue_type='3' and cpbdue_acct_type='01' "
	    	+ " and cpbdue_id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) ";
	    
//	    log("Cpbdue3_aaa:" + lsIdno);
//	    log("Cpbdue3_bbb:" + lsPseqno);
	    
	    sqlSelect(sql1, new Object[] {lsIdno});
	    
	    if (empty(sqlStr("ls_status"))) {
	      return;
	    }

	    wp.colSet(wp.selectCnt, "wk_type", "前置調解");
	    wp.colSet(wp.selectCnt, "wk_status", "LIAB-" + sqlStr("ls_status")); //定義與債協相同
	    wp.colSet(wp.selectCnt, "wk_rcv_date", sqlStr("ls_rcv_date"));
	    wp.colSet(wp.selectCnt, "wk_end_date", sqlStr("ls_end_date"));
	    wp.selectCnt++;
	  }

  /*
  void wfReadColLiab(String lsIdno, String lsPseqno) {
    String sql1 = "select " + " liab_status as ls_status," + " notify_date as ls_rcv_date "
        + " from col_liab_nego" + " where id_no =?"
        + " order by decode(liab_status,'2','a','3','b','c') ";
    log("aaa:" + lsIdno);
    log("bbb:" + lsPseqno);
    sqlSelect(sql1, new Object[] {lsIdno});
    if (sqlRowNum == 0 && !empty(lsPseqno)) {
      String sql2 = "select " + " liab_status as ls_status," + " liab_end_date as ls_end_date "
          + " from act_acno" + " where acno_p_seqno =?";
      sqlSelect(sql2, new Object[] {lsPseqno});
      if (sqlRowNum <= 0) {
        return;
      }
    }

    if (empty(sqlStr("ls_status"))) {
      return;
    }

    wp.colSet(wp.selectCnt, "wk_type", "債務協商");
    wp.colSet(wp.selectCnt, "wk_status", "LIAB-" + sqlStr("ls_status"));
    wp.colSet(wp.selectCnt, "wk_rcv_date", sqlStr("ls_rcv_date"));
    wp.colSet(wp.selectCnt, "wk_end_date", sqlStr("ls_end_date"));
    wp.selectCnt++;
  }
  */

  /*前協*/
  void wfReadColLiac(String lsIdno, String lsPSeqno) {
//  void wfReadColLiac(String lsIdPSeqno, String lsPSeqno) {
    String sql1 = "select " + " liac_status as ls_status," + " notify_date as ls_rcv_date,"
        + " end_date as ls_end_date " + " from col_liac_nego " 
    	+ " where id_p_seqno =(select id_p_seqno from crd_idno where id_no=? ) ";
//    	+ " where id_no =?";
    sqlSelect(sql1, new Object[] {lsIdno});
//    sqlSelect(sql1, new Object[] {lsIdPSeqno});

    log("liac_aaa:" + lsIdno);
    log("liac_bbb:" + lsPSeqno);
    
    if (sqlRowNum <= 0) {
      String sql2 = "select " + " max(notify_date) as ls_notify_date " + " from col_liac_nego_hst "
    		 + " where id_p_seqno= (select id_p_seqno from crd_idno where id_no=? ) " + " and liac_status ='6' ";
      sqlSelect(sql2, new Object[] {lsIdno});
//      sqlSelect(sql2, new Object[] {lsIdPSeqno});

      if (sqlRowNum <= 0 || empty(sqlStr("ls_notify_date"))) {
        return;
      }

      String sql3 = "select " + " liac_status as ls_status , " + " notify_date as ls_rcv_date , "
          + " end_date as ls_end_date " + " from col_liac_nego_hst " + " where 1=1 "
          //+ " and id_no=? ," + " and liac_status ='6' ," + " and notify_date = ? "
          + " and id_p_seqno= (select id_p_seqno from crd_idno where id_no=? )," 
          + " and liac_status ='6' ," + " and notify_date = ? "
          + " fetch first 1 rows only ";
//      sqlSelect(sql3, new Object[] {lsIdPSeqno, sqlStr("ls_notify_date")});      
      sqlSelect(sql3, new Object[] {lsIdno, sqlStr("ls_notify_date")});
      if (sqlRowNum <= 0)
        return;
    }

    if (empty(sqlStr("ls_status"))) {
      return;
    }
    wp.colSet(wp.selectCnt, "wk_type", "前置協商");
    wp.colSet(wp.selectCnt, "wk_status", "LIAC-" + sqlStr("ls_status"));
    wp.colSet(wp.selectCnt, "wk_rcv_date", sqlStr("ls_rcv_date"));
    if (!eqIgno(sqlStr("ls_status"), "6")) {
      wp.colSet(wp.selectCnt, "wk_end_date", "");
    } else {
      wp.colSet(wp.selectCnt, "wk_end_date", sqlStr("ls_end_date"));
    }
    wp.selectCnt++;
  }
  

  /*更生*/
  
  void wfReadColLiadRenew(String lsIdno, String lsPSeqno) {
	    String sql1 = "select " + " max(status_date) as ls_rcv_date  " + " from col_liad_renewliqui "
	        + " where 1=1 " + " and  liad_type='3' and id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) ";   
	    sqlSelect(sql1, new Object[] {lsIdno});
	    if (empty(sqlStr("ls_rcv_date"))) {
	      return;
	    }

//	    String sql2 = "select " + " a.court_status||'.'||b.wf_desc as ls_status ,"
//	        + " a.confirm_date as ls_end_date " + " from ptr_sys_idtab b, col_liad_renew a  "
//	        + " where 1=1 " + " and a.court_status = b.wf_id " + " and a.id_no = ? "
//	        + " and a.recv_date = ? " + " and b.wf_type='LIAD_RENEW_STATUS' "
//	        + " fetch first 1 rows only ";
	    String sql2 = "select liad_status as ls_status,status_date as ls_rcv_date from col_liad_renewliqui "
	    		+ "where liad_type='3' "
	    		+ "and id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) "
	    		+ "and status_date = ? "
	    		+ "order by status_date desc "
	            + " fetch first 1 rows only ";
	    
	    sqlSelect(sql2, new Object[] {lsIdno, sqlStr("ls_rcv_date")});

	    if (sqlRowNum <= 0) {
	      return;
	    }

	    wp.colSet(wp.selectCnt, "wk_type", "更生計劃");
	    wp.colSet(wp.selectCnt, "wk_status", "RENEW-" + sqlStr("ls_status"));
	    wp.colSet(wp.selectCnt, "wk_rcv_date", sqlStr("ls_rcv_date"));
	    wp.colSet(wp.selectCnt, "wk_end_date", sqlStr("ls_end_date"));
	    wp.selectCnt++;

	  }
  
/*清算*/
  
  void wfReadColLiadLiquidate(String lsIdno, String lsPSeqno) {
	    String sql1 = "select " + " max(status_date) as ls_rcv_date  " + " from col_liad_renewliqui "
	        + " where 1=1 " + " and  liad_type='4' and id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) ";   
	    sqlSelect(sql1, new Object[] {lsIdno});
	    if (empty(sqlStr("ls_rcv_date"))) {
	      return;
	    }

//	    String sql2 = "select " + " a.court_status||'.'||b.wf_desc as ls_status ,"
//	        + " a.confirm_date as ls_end_date " + " from ptr_sys_idtab b, col_liad_renew a  "
//	        + " where 1=1 " + " and a.court_status = b.wf_id " + " and a.id_no = ? "
//	        + " and a.recv_date = ? " + " and b.wf_type='LIAD_RENEW_STATUS' "
//	        + " fetch first 1 rows only ";
	    String sql2 = "select liad_status as ls_status,status_date as ls_rcv_date from col_liad_renewliqui "
	    		+ "where liad_type='4' "
	    		+ "and id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) "
	    		+ "and status_date = ? "
	    		+ "order by status_date desc "
	            + " fetch first 1 rows only ";
	    
	    sqlSelect(sql2, new Object[] {lsIdno, sqlStr("ls_rcv_date")});

	    if (sqlRowNum <= 0) {
	      return;
	    }

	    wp.colSet(wp.selectCnt, "wk_type", "清算");
	    wp.colSet(wp.selectCnt, "wk_status", "LIAD-" + sqlStr("ls_status"));
	    wp.colSet(wp.selectCnt, "wk_rcv_date", sqlStr("ls_rcv_date"));
	    wp.colSet(wp.selectCnt, "wk_end_date", sqlStr("ls_end_date"));
	    wp.selectCnt++;

	  }

  /*更生*/
  /*
  void wfReadColLiadRenew(String lsIdno, String lsPSeqno) {
    String sql1 = "select " + " max(recv_date) as ls_rcv_date  " + " from col_liad_renew "
        + " where 1=1 " + " and id_no=? ";   
    sqlSelect(sql1, new Object[] {lsIdno});
    if (empty(sqlStr("ls_rcv_date"))) {
      return;
    }

    String sql2 = "select " + " a.court_status||'.'||b.wf_desc as ls_status ,"
        + " a.confirm_date as ls_end_date " + " from ptr_sys_idtab b, col_liad_renew a  "
        + " where 1=1 " + " and a.court_status = b.wf_id " + " and a.id_no = ? "
        + " and a.recv_date = ? " + " and b.wf_type='LIAD_RENEW_STATUS' "
        + " fetch first 1 rows only ";
    sqlSelect(sql2, new Object[] {lsIdno, sqlStr("ls_rcv_date")});

    if (sqlRowNum <= 0) {
      return;
    }

    wp.colSet(wp.selectCnt, "wk_type", "更生計劃");
    wp.colSet(wp.selectCnt, "wk_status", sqlStr("ls_status"));
    wp.colSet(wp.selectCnt, "wk_rcv_date", sqlStr("ls_rcv_date"));
    wp.colSet(wp.selectCnt, "wk_end_date", sqlStr("ls_end_date"));
    wp.selectCnt++;

  }


  /*清算*/
  /*
  void wfReadColLiadLiquidate(String lsIdno, String lsPseqno) {
    String sql1 = "select " + " max(recv_date) as ls_rcv_date  " + " from col_liad_liquidate "
        + " where 1=1 " + " and id_no=? ";
    sqlSelect(sql1, new Object[] {lsIdno});

    if (empty(sqlStr("ls_rcv_date"))) {
      return;
    }

    String sql2 = "select " + " a.court_status||'.'||b.wf_desc as ls_status "
        + " from ptr_sys_idtab b, col_liad_liquidate a  " + " where 1=1 "
        + " and a.court_status = b.wf_id " + " and a.id_no = ? " + " and a.recv_date = ? "
        + " and b.wf_type='LIAD_LIQU_STATUS' " + " fetch first 1 rows only ";
    sqlSelect(sql2, new Object[] {lsIdno, sqlStr("ls_rcv_date")});

    if (sqlRowNum < 0) {
      return;
    }

    wp.colSet(wp.selectCnt, "wk_type", "清算");
    wp.colSet(wp.selectCnt, "wk_status", sqlStr("ls_status"));
    wp.colSet(wp.selectCnt, "wk_rcv_date", sqlStr("ls_rcv_date"));
    wp.colSet(wp.selectCnt, "wk_end_date", sqlStr("ls_end_date"));
    wp.selectCnt++;

  }
  */

  // ---ppcard
  public void queryFuncPPCARD() throws Exception {
    String lsWhere = "";

    if (!empty(wp.itemStr("ex_card_no"))) { //卡號 
        lsWhere += " and id_p_seqno in (select id_p_seqno from crd_card where card_no= ? )";
        setString(wp.itemStr("ex_card_no"));
      }
    String lsIidno = wp.itemStr("ex_idno");//身份證號
    if (!empty(lsIidno)) {
      if (lsIidno.length() == 10) {
        lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where id_no = ? )";
        setString(lsIidno);
      } else if (lsIidno.length() > 0) {
        alertErr2("身分證號 輸入不完整");
        return;
      }
    }

    if (!empty(wp.itemStr("ex_pp_card_no"))) {
        lsWhere += " and pp_card_no = ? ";
        setString(wp.itemStr("ex_pp_card_no"));
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    wp.setQueryMode();
    wp.sqlCmd = " select " + " pp_card_no ," + " current_code , " + " bin_type , "+"vip_kind, "
        +"decode(vip_kind,'1','新貴通','2','龍騰',vip_kind) tt_vip_kind, "
        + " issue_date , " + " new_end_date , " + " valid_to , " + " oppost_date , " + wp.sqlID
        + " uf_idno_id(id_p_seqno) as id_no " + " ,card_no "
        + " FROM crd_card_pp " + " where 1=1 " + lsWhere
        + " order by current_code , issue_date ";

    this.pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(9);
  }

  public void querySelectPpcard() throws Exception {
    dataKK1 = wp.itemStr("data_k1");
    dataReadPpcard();
  }

  void dataReadPpcard() throws Exception {
    wp.selectSQL = " A.* , " + " uf_idno_id(A.id_p_seqno) as id_no , "
        +"vip_kind, "
        +"decode(vip_kind,'1','新貴通','2','龍騰',vip_kind) tt_vip_kind, "
        + " uf_idno_name(A.id_p_seqno) as chi_name ";
    wp.daoTable = "crd_card_pp A ";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "A.pp_card_no");
    this.logSql();
    pageSelect();
    // wkdata_ppcard();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + dataKK1);
      return;
    }
    dataReadEmbossPp();
    dataReadReturnPp();
    dataReadWkdataPpcard();
  }

  void dataReadEmbossPp() {
    daoTid = "B.";
    wp.selectSQL = " mail_proc_date ," + " mail_type , " + " mail_branch ," + " mail_no ,"
        + " zip_code ," + " mail_addr1 ," + " mail_addr2 ," + " mail_addr3 ," + " mail_addr4 ,"
        + " mail_addr5  ";
    wp.daoTable = "crd_emboss_pp ";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "pp_card_no");
    wp.whereOrder = " fetch first 1 row only ";
    this.logSql();
    pageSelect();
    wp.notFound = "N";
  }

  void dataReadReturnPp() {
    daoTid = "C.";
    wp.selectSQL = " return_date ," + " proc_status ," + " mail_date ," + " mail_type ,"
        + " mail_branch ," + " mail_no ," + " reason_code ," + " zip_code ," + " mail_addr1 ,"
        + " mail_addr2 ," + " mail_addr3 ," + " mail_addr4 ," + " mail_addr5 ";
    wp.daoTable = "crd_return_pp ";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK1, "pp_card_no");
    wp.whereOrder = " order by return_date desc fetch first 1 row only";
    this.logSql();
    pageSelect();
    wp.notFound = "N";

  }

  void dataReadWkdataPpcard() {
    if (wp.colEq("change_status", "1")) {
      wp.colSet("change_status", "1.續卡待製卡");
    } else if (wp.colEq("change_status", "2")) {
      wp.colSet("change_status", "2.製卡中");
    } else if (wp.colEq("change_status", "3")) {
      wp.colSet("change_status", "3.製卡完成");
    } else if (wp.colEq("change_status", "4")) {
      wp.colSet("change_status", "4.製卡失敗");
    }

    if (wp.colEq("change_reason", "1")) {
      wp.colSet("change_reason", "1.系統續卡");
    } else if (wp.colEq("change_reason", "2")) {
      wp.colSet("change_reason", "2.提前續卡-客戶來電");
    } else if (wp.colEq("change_reason", "3")) {
      wp.colSet("change_reason", "3.提前續卡-本行調整");
    }

    if (wp.colEq("expire_chg_flag", "1")) {
      wp.colSet("expire_chg_flag", "1.預約不續卡");
    } else if (wp.colEq("expire_chg_flag", "4")) {
      wp.colSet("expire_chg_flag", "4.人工不續卡");
    } else if (wp.colEq("expire_chg_flag", "5")) {
      wp.colSet("expire_chg_flag", "5.系統不續卡");
    }

    if (wp.colEq("B.mail_type", "1")) {
      wp.colSet("B.mail_type", "1.普掛");
    } else if (wp.colEq("B.mail_type", "2")) {
      wp.colSet("B.mail_type", "2.限掛");
    } else if (wp.colEq("B.mail_type", "3")) {
      wp.colSet("B.mail_type", "3.自取");
    } else if (wp.colEq("B.mail_type", "4")) {
      wp.colSet("B.mail_type", "4.分行");
    }

    if (wp.colEq("C.proc_status", "1")) {
      wp.colSet("C.proc_status", "1.庫存");
    } else if (wp.colEq("C.proc_status", "2")) {
      wp.colSet("C.proc_status", "2.銷毀");
    } else if (wp.colEq("C.proc_status", "3")) {
      wp.colSet("C.proc_status", "3.寄出");
    } else if (wp.colEq("C.proc_status", "4")) {
      wp.colSet("C.proc_status", "4.申停");
    } else if (wp.colEq("C.proc_status", "5")) {
      wp.colSet("C.proc_status", "5.重製");
    } else if (wp.colEq("C.proc_status", "6")) {
      wp.colSet("C.proc_status", "6.寄出不封裝");
    } else if (wp.colEq("C.proc_status", "7")) {
      wp.colSet("C.proc_status", "7.庫存");
    }

    if (wp.colEq("C.mail_type", "1")) {
      wp.colSet("C.mail_type", "1.普掛");
    } else if (wp.colEq("C.mail_type", "2")) {
      wp.colSet("C.mail_type", "2.限掛");
    } else if (wp.colEq("C.mail_type", "3")) {
      wp.colSet("C.mail_type", "3.自取");
    } else if (wp.colEq("C.mail_type", "4")) {
      wp.colSet("C.mail_type", "4.分行");
    } else if (wp.colEq("C.mail_type", "5")) {
      wp.colSet("C.mail_type", "5.暫不寄");
    } else if (wp.colEq("C.mail_type", "6")) {
      wp.colSet("C.mail_type", "6.快捷");
    } else if (wp.colEq("C.mail_type", "7")) {
      wp.colSet("C.mail_type", "7.航空");
    } else if (wp.colEq("C.mail_type", "8")) {
      wp.colSet("C.mail_type", "8.快遞");
    } else if (wp.colEq("C.mail_type", "9")) {
      wp.colSet("C.mail_type", "9.其他");
    }

    if (wp.colEq("C.reason_code", "01")) {
      wp.colSet("C.reason_code", "01.查無此公司");
    } else if (wp.colEq("C.reason_code", "02")) {
      wp.colSet("C.reason_code", "02.查無此人");
    } else if (wp.colEq("C.reason_code", "03")) {
      wp.colSet("C.reason_code", "03.遷移不明");
    } else if (wp.colEq("C.reason_code", "04")) {
      wp.colSet("C.reason_code", "04.地址欠詳");
    } else if (wp.colEq("C.reason_code", "05")) {
      wp.colSet("C.reason_code", "05.查無此址");
    } else if (wp.colEq("C.reason_code", "06")) {
      wp.colSet("C.reason_code", "06.收件人拒收");
    } else if (wp.colEq("C.reason_code", "07")) {
      wp.colSet("C.reason_code", "07.招領逾期");
    } else if (wp.colEq("C.reason_code", "08")) {
      wp.colSet("C.reason_code", "08.分行退件");
    } else if (wp.colEq("C.reason_code", "09")) {
      wp.colSet("C.reason_code", "09.其他");
    } else if (wp.colEq("C.reason_code", "10")) {
      wp.colSet("C.reason_code", "10.地址改變");
    }

    String sql1 =
        " select wf_desc from ptr_sys_idtab where wf_type ='PPCARD_OPPOST_REASON' and wf_id = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("oppost_reason")});
    wp.colSet("tt_oppost_reason", sqlStr("wf_desc"));

  }

  void querySelectIchcard() throws Exception {
    dataKK2 = wp.itemStr2("data_k2");
    dataKK3 = wp.itemStr2("data_k3");
    dataReadXXcard();
    selectIcash(); //add 20201106 ryan
  }

  void dataReadXXcard() throws Exception {
    selectCrdIdnoByCardNoXXcard();

    wp.selectSQL = "" + " ich_card_no ," + " card_no ," + " current_code ," + " crt_date ,"
        + " new_beg_date ," + " new_end_date ," + " ich_amt ," + " ich_pledge_amt ,"
        + " ic_seq_no ," + " isam_seq_no ," + " isam_batch_no ," + " isam_batch_seq ,"
        + " autoload_amt ," + " autoload_flag ," + " balance_date ," + " balance_rtn_date ,"
        + " balance_amt ," + " balance_fee ," + " oppost_date ," + " return_flag ,"
        + " return_date ," + " return_time ," + " return_amt ," + " return_fee ,"
        + " agency_id as traffic_cd ," + " ' ' as traffic_abbr ," + " store_id as addr_cd ,"
        + " ' ' as addr_abbr ," + " oppost_source ," + " lock_flag ," + " lock_date ,"
        + " lock_time ," + " combine_flag ," + " blacklt_flag ," + " blacklt_s_date ,"
        + " blacklt_e_date ," + " autoload_date ," + " iden_code ," + " ich_sign_flag ,"
        + " ich_sign_date ," + " lpad(' ',1,' ')  as db_market_flag ,"
        + " lpad(' ',20,' ') as db_resp_code_1 ," + " lpad(' ',10,' ') as db_resp_date_1 ,"
        + " lpad(' ',10,' ') as db_resp_time_1 ," + " lpad(' ',20,' ') as db_resp_code_2 ,"
        + " lpad(' ',10,' ') as db_resp_date_2 ," + " lpad(' ',10,' ') as db_resp_time_2 ,"
        + " lpad(' ',10,' ') as db_cre_date_1 ," + " lpad(' ',10,' ') as db_cre_time_1 ,"
        + " lpad(' ',10,' ') as db_cre_date_2 ," + " lpad(' ',10,' ') as db_cre_time_2 ,"
        + " 0 as db_amt_1 ," + " 0 as db_amt_2 ," + " lpad(' ',10,' ') db_crt_date_3 ,"
        + " ich_emboss_rsn ," + " purchase_date ," + " purchase_time ," + " dest_amt ,"
        + " ' ' as traffic_abbr_neg ," + " ' ' as addr_abbr_neg ," + " new_ich_card_no ,"
        + " refuse_type ," + " refuse_send_date ," + " refuse_cancel_date ," + " format_ver ";

    wp.daoTable = " ich_card ";
    wp.whereStr = " where 1=1 " + sqlCol(dataKK2, "ich_card_no");

    pageSelect();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    
    wp.colSet("current_code_desc", DeCodeCrd.electronicCurrentCode(wp.colStr("current_code")));
  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  void resetBtn() {
    wp.colSet("bt_class1", "btQuery");
    wp.colSet("bt_class2", "btQuery");
    wp.colSet("bt_class3", "btQuery");
    wp.colSet("bt_class4", "btQuery");
    wp.colSet("bt_class5", "btQuery");
    wp.colSet("bt_class6", "btQuery");
    wp.colSet("bt_class7", "btQuery");
    wp.colSet("bt_class8", "btQuery");
    wp.colSet("bt_class9", "btQuery");
    wp.colSet("bt_class10", "btQuery");
    wp.colSet("bt_class11", "btQuery");
    wp.colSet("bt_class12", "btQuery");

  }

  @Override
  public void initPage() {
    resetBtn();
  }

  public void selectIdnoByIdPSeqnoXXcard(String idnoTable) {
    String sql1 = "select id_p_seqno ," + " id_no ," + " id_no_code ," + " chi_name ," + " sex ,"
        + " birthday ," + " resident_no ," + " other_cntry_code," + " passport_no,"
        + " tsc_market_flag as db_market_flag " 
        + " from " + idnoTable 
        + " where id_p_seqno =?";

    sqlSelect(sql1, new Object[] { dataKK4});
    wp.colSet("wk_idno", sqlStr("id_no") + "-" + sqlStr("id_no_code"));
    wp.colSet("birthday", sqlStr("birthday"));
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("resident_no", sqlStr("resident_no"));
    wp.colSet("other_cntry_code", sqlStr("other_cntry_code"));
    wp.colSet("passport_no", sqlStr("passport_no"));
    wp.colSet("db_market_flag", sqlStr("db_market_flag"));
  }

  public void selectCrdIdnoByIdPSeqno() {
    String sql1 = "select id_p_seqno ," + " id_no ," + " id_no_code ," + " chi_name ," + " sex ,"
        + " birthday ," + " resident_no ," + " other_cntry_code," + " passport_no,"
        + " tsc_market_flag as db_market_flag " + " from crd_idno" + " where id_p_seqno =?";

    sqlSelect(sql1, new Object[] {wp.itemStr("data_k2")});
    wp.colSet("wk_idno", sqlStr("id_no") + "-" + sqlStr("id_no_code"));
    wp.colSet("birthday", sqlStr("birthday"));
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("resident_no", sqlStr("resident_no"));
    wp.colSet("other_cntry_code", sqlStr("other_cntry_code"));
    wp.colSet("passport_no", sqlStr("passport_no"));
    wp.colSet("db_market_flag", sqlStr("db_market_flag"));
  }

  public void selectCrdIdnoByCardNoXXcard() {
    String sql1 = "select id_p_seqno ," + " id_no ," + " id_no_code ," + " chi_name ," + " sex ,"
        + " birthday ," + " resident_no ," + " other_cntry_code," + " passport_no,"
        + " tsc_market_flag as db_market_flag " + " from crd_idno"
        + " where id_p_seqno in (select id_p_seqno from crd_card where card_no =? )";
    log("sql1:" + sql1);
    sqlSelect(sql1, new Object[] {dataKK3});
    wp.colSet("wk_idno", sqlStr("id_no") + "-" + sqlStr("id_no_code"));
    wp.colSet("birthday", sqlStr("birthday"));
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("resident_no", sqlStr("resident_no"));
    wp.colSet("other_cntry_code", sqlStr("other_cntry_code"));
    wp.colSet("passport_no", sqlStr("passport_no"));

  }

  public void selectCrdIdnoByCardNo() {
    String sql1 = "select id_p_seqno ," + " id_no ," + " id_no_code ," + " chi_name ," + " sex ,"
        + " birthday ," + " resident_no ," + " other_cntry_code," + " passport_no,"
        + " tsc_market_flag as db_market_flag " + " from crd_idno"
        + " where id_p_seqno in (select id_p_seqno from crd_card where card_no =? )";

    sqlSelect(sql1, new Object[] {wp.itemStr("data_k1")});
    wp.colSet("wk_idno", sqlStr("id_no") + "-" + sqlStr("id_no_code"));
    wp.colSet("birthday", sqlStr("birthday"));
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("resident_no", sqlStr("resident_no"));
    wp.colSet("other_cntry_code", sqlStr("other_cntry_code"));
    wp.colSet("passport_no", sqlStr("passport_no"));
  }

  public void wfCallBatch() throws Exception {
    String lsSirNo = "", lsRun = "";
    lsSirNo = wp.itemStr("sir_no");

    if (empty(lsSirNo)) {
      errmsg("[Sir No]  不可空白");
      return;
    }

    wp.colSet("db_batch_seqno", "");
    wp.itemSet("db_batch_seqno", "");

    lsRun = "MobP080 " + lsSirNo;
    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    rc = batch.callBatch(lsRun);
    if (rc != 1) {
      alertErr2("callBatch error; " + batch.getMesg());
    } else {
      alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
    }

    wp.colSet("db_batch_seqno", batch.batchSeqno());
    wp.itemSet("db_batch_seqno", batch.batchSeqno());
    wfCheckSirResp();
  }

  public void wfCheckSirResp() {
    String lsBatchSeqno = "", lsSit = "";
    lsBatchSeqno = wp.itemStr("db_batch_seqno");

    if (empty(lsBatchSeqno)) {
      errmsg("未啟動 [查詢作業~], Batch_seqno is Empty");
      return;
    }

    String sql1 = " select " + " sit " + " from mob_txn_log " + " where batch_seqno = ? ";

    sqlSelect(sql1, new Object[] {lsBatchSeqno});

    if (sqlRowNum < 0) {
      errmsg("select mob_txn_log error, batch_seqno=" + lsBatchSeqno);
      return;
    } else if (sqlRowNum == 0) {
      errmsg("SIR狀態查詢未回覆");
      return;
    }
    lsSit = sqlStr("sit");
    wp.colSet("sir_status", lsSit);
  }

}
